package kazuko.ex.report.service;

import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.TonQuyReportRow;
import kazuko.ex.report.model.TongHopThuChiTheoLyDoReportRow;
import kazuko.ex.report.model.TongHopThuChiTheoNgayReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoQuyReportService {

    private static JasperReport cachedReportNgay = null;
    private static JasperReport cachedReportLyDo = null;
    private static JasperReport cachedReportTonQuy = null;
    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        // Preload and compile in background
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoQuy-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportNgay == null) {
                try (InputStream in = BaoCaoQuyReportService.class.getResourceAsStream("/reports/tong_hop_thu_chi_theo_ngay.jrxml")) {
                    if (in != null) cachedReportNgay = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportLyDo == null) {
                try (InputStream in = BaoCaoQuyReportService.class.getResourceAsStream("/reports/tong_hop_thu_chi_theo_ly_do.jrxml")) {
                    if (in != null) cachedReportLyDo = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTonQuy == null) {
                try (InputStream in = BaoCaoQuyReportService.class.getResourceAsStream("/reports/bao_cao_ton_quy.jrxml")) {
                    if (in != null) cachedReportTonQuy = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportNgay() throws JRException {
        if (cachedReportNgay != null) return cachedReportNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_thu_chi_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_thu_chi_theo_ngay.jrxml không tồn tại!");
            cachedReportNgay = JasperCompileManager.compileReport(in);
            return cachedReportNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportLyDo() throws JRException {
        if (cachedReportLyDo != null) return cachedReportLyDo;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_thu_chi_theo_ly_do.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_thu_chi_theo_ly_do.jrxml không tồn tại!");
            cachedReportLyDo = JasperCompileManager.compileReport(in);
            return cachedReportLyDo;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTonQuy() throws JRException {
        if (cachedReportTonQuy != null) return cachedReportTonQuy;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ton_quy.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ton_quy.jrxml không tồn tại!");
            cachedReportTonQuy = JasperCompileManager.compileReport(in);
            return cachedReportTonQuy;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    private Object getCompanyLogo(SystemConfig sysConfig) {
        String logoPath = sysConfig.getLogoPath();
        Object logoImage = cachedLogoImage;
        if (logoPath != null && !logoPath.trim().isEmpty()) {
            if (!logoPath.equals(cachedLogoPath)) {
                File f = new File(logoPath);
                if (f.exists() && f.isFile()) {
                    try {
                        logoImage = ImageIO.read(f);
                        cachedLogoImage = logoImage;
                        cachedLogoPath = logoPath;
                    } catch (Exception ignored) {}
                }
            }
        }
        if (logoImage == null) {
            if (cachedLogoImage != null) {
                logoImage = cachedLogoImage;
            } else {
                try {
                    InputStream logoStream = getClass().getResourceAsStream("/logo.png");
                    if (logoStream != null) {
                        logoImage = ImageIO.read(logoStream);
                        cachedLogoImage = logoImage;
                    }
                } catch (Exception ignored) {}
            }
        }
        return logoImage;
    }

    public JasperPrint generateTongHopThuChiTheoNgay(Date fromDate, Date toDate, String lyDoId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<PhieuThu> listThu = phieuThuDAO.getReportPhieuThuTheoNgay(fromDate, toDate, lyDoId, cuaHangId);
        List<PhieuChi> listChi = phieuChiDAO.getReportPhieuChiTheoNgay(fromDate, toDate, lyDoId, cuaHangId);

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, double[]> dateMap = new TreeMap<>((d1, d2) -> {
            try {
                return sdfDate.parse(d1).compareTo(sdfDate.parse(d2));
            } catch (Exception e) {
                return d1.compareTo(d2);
            }
        });

        for (PhieuChi pc : listChi) {
            if (pc.getNgay() == null) continue;
            String dStr = sdfDate.format(pc.getNgay());
            dateMap.putIfAbsent(dStr, new double[]{0.0, 0.0});
            dateMap.get(dStr)[0] += pc.getSoTien();
        }

        for (PhieuThu pt : listThu) {
            if (pt.getNgay() == null) continue;
            String dStr = sdfDate.format(pt.getNgay());
            dateMap.putIfAbsent(dStr, new double[]{0.0, 0.0});
            dateMap.get(dStr)[1] += pt.getSoTien();
        }

        List<TongHopThuChiTheoNgayReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (Map.Entry<String, double[]> entry : dateMap.entrySet()) {
            rows.add(new TongHopThuChiTheoNgayReportRow(stt++, entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        Map<String, Object> params = new HashMap<>();
        params.put("p_companyName", sysConfig.getTenCongTy() != null ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        params.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        params.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        params.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "info@ngocchauau.vn");
        params.put("p_logoImage", getCompanyLogo(sysConfig));

        String fromDateStr = fromDate != null ? sdfDate.format(fromDate) : "";
        String toDateStr = toDate != null ? sdfDate.format(toDate) : "";
        params.put("p_dateRangeText", "Ngày từ " + fromDateStr + " đến " + toDateStr);
        params.put("p_reportTitle", "TỔNG HỢP THU CHI THEO NGÀY");

        return JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(rows));
    }

    public JasperPrint generateTongHopThuChiTheoLyDo(Date fromDate, Date toDate, String lyDoId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportLyDo();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<PhieuThu> listThu = phieuThuDAO.getReportPhieuThuTheoNgay(fromDate, toDate, lyDoId, cuaHangId);
        List<PhieuChi> listChi = phieuChiDAO.getReportPhieuChiTheoNgay(fromDate, toDate, lyDoId, cuaHangId);

        Map<String, double[]> reasonMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (PhieuChi pc : listChi) {
            String lyDo = (pc.getTenPhanLoai() != null && !pc.getTenPhanLoai().trim().isEmpty())
                    ? pc.getTenPhanLoai().trim()
                    : ((pc.getLyDoChi() != null && !pc.getLyDoChi().trim().isEmpty()) ? pc.getLyDoChi().trim() : "Khác");
            reasonMap.putIfAbsent(lyDo, new double[]{0.0, 0.0});
            reasonMap.get(lyDo)[0] += pc.getSoTien();
        }

        for (PhieuThu pt : listThu) {
            String lyDo = (pt.getTenPhanLoai() != null && !pt.getTenPhanLoai().trim().isEmpty())
                    ? pt.getTenPhanLoai().trim()
                    : ((pt.getLyDoThu() != null && !pt.getLyDoThu().trim().isEmpty()) ? pt.getLyDoThu().trim() : "Khác");
            reasonMap.putIfAbsent(lyDo, new double[]{0.0, 0.0});
            reasonMap.get(lyDo)[1] += pt.getSoTien();
        }

        List<TongHopThuChiTheoLyDoReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (Map.Entry<String, double[]> entry : reasonMap.entrySet()) {
            rows.add(new TongHopThuChiTheoLyDoReportRow(stt++, entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> params = new HashMap<>();
        params.put("p_companyName", sysConfig.getTenCongTy() != null ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        params.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        params.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        params.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "info@ngocchauau.vn");
        params.put("p_logoImage", getCompanyLogo(sysConfig));

        String fromDateStr = fromDate != null ? sdfDate.format(fromDate) : "";
        String toDateStr = toDate != null ? sdfDate.format(toDate) : "";
        params.put("p_dateRangeText", "Ngày từ " + fromDateStr + " đến " + toDateStr);
        params.put("p_reportTitle", "TỔNG HỢP THU CHI THEO LÝ DO");

        return JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(rows));
    }

    public JasperPrint generateBaoCaoTonQuy(Date fromDate, Date toDate, String loaiQuy, String taiKhoanId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTonQuy();
        SystemConfig sysConfig = configDAO.loadConfig();

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        // 1. Tính tồn đầu kỳ (tất cả thu/chi trước fromDate)
        Date minDate = new Date(0);
        Date beforeFromDate = new Date(fromDate != null ? fromDate.getTime() - 1 : 0);

        List<PhieuThu> thuTruoc = phieuThuDAO.getReportPhieuThuTheoNgay(minDate, beforeFromDate, "ALL", cuaHangId);
        List<PhieuChi> chiTruoc = phieuChiDAO.getReportPhieuChiTheoNgay(minDate, beforeFromDate, "ALL", cuaHangId);

        double tonDauKy = 0.0;
        for (PhieuThu pt : thuTruoc) {
            tonDauKy += pt.getSoTien();
        }
        for (PhieuChi pc : chiTruoc) {
            tonDauKy -= pc.getSoTien();
        }

        // 2. Lấy dữ liệu trong kỳ
        List<PhieuThu> listThu = phieuThuDAO.getReportPhieuThuTheoNgay(fromDate, toDate, "ALL", cuaHangId);
        List<PhieuChi> listChi = phieuChiDAO.getReportPhieuChiTheoNgay(fromDate, toDate, "ALL", cuaHangId);

        // Gom các giao dịch lại
        class GiaoDich {
            final Date ngay;
            final Date timeCreated;
            final String soPhieu;
            final String dienGiai;
            final String taoBoi;
            final Double thu;
            final Double chi;

            GiaoDich(Date ngay, Date timeCreated, String soPhieu, String dienGiai, String taoBoi, Double thu, Double chi) {
                this.ngay = ngay;
                this.timeCreated = timeCreated;
                this.soPhieu = soPhieu;
                this.dienGiai = dienGiai;
                this.taoBoi = taoBoi;
                this.thu = thu;
                this.chi = chi;
            }
        }

        List<GiaoDich> gdList = new ArrayList<>();
        for (PhieuThu pt : listThu) {
            String dg = pt.getTenPhanLoai() != null && !pt.getTenPhanLoai().isEmpty() ? pt.getTenPhanLoai() : (pt.getLyDoThu() != null ? pt.getLyDoThu() : "Thu tiền");
            gdList.add(new GiaoDich(pt.getNgay(), pt.getTimeCreated(), pt.getSoPhieu(), dg, pt.getUserCreatedId() != null ? pt.getUserCreatedId() : "Administrator", pt.getSoTien(), 0.0));
        }
        for (PhieuChi pc : listChi) {
            String dg = pc.getTenPhanLoai() != null && !pc.getTenPhanLoai().isEmpty() ? pc.getTenPhanLoai() : (pc.getLyDoChi() != null ? pc.getLyDoChi() : "Chi tiền");
            gdList.add(new GiaoDich(pc.getNgay(), pc.getTimeCreated(), pc.getSoPhieu(), dg, pc.getUserCreatedId() != null ? pc.getUserCreatedId() : "Administrator", 0.0, pc.getSoTien()));
        }

        gdList.sort(Comparator.comparing((GiaoDich g) -> g.ngay != null ? g.ngay : new Date())
                .thenComparing(g -> g.timeCreated != null ? g.timeCreated : new Date())
                .thenComparing(g -> g.soPhieu != null ? g.soPhieu : ""));

        // 3. Build danh sách dòng báo cáo TonQuyReportRow
        List<TonQuyReportRow> rows = new ArrayList<>();

        // Dòng TỒN ĐẦU KỲ
        rows.add(new TonQuyReportRow("", "", "", "TỒN ĐẦU KỲ", "", "", null, null, tonDauKy, true));

        double runningTon = tonDauKy;
        int tt = 1;
        for (GiaoDich gd : gdList) {
            runningTon += (gd.thu - gd.chi);
            String ngayStr = gd.ngay != null ? sdfDate.format(gd.ngay) : "";
            String timeStr = gd.timeCreated != null ? sdfTime.format(gd.timeCreated) : "";
            rows.add(new TonQuyReportRow(String.valueOf(tt++), gd.soPhieu, ngayStr, gd.dienGiai, gd.taoBoi, timeStr, gd.thu, gd.chi, runningTon, false));
        }

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        String footerDate = String.format("Ngày %02d tháng %02d năm %d", day, month, year);

        Map<String, Object> params = new HashMap<>();
        params.put("p_companyName", sysConfig.getTenCongTy() != null ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        params.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        params.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        params.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "info@ngocchauau.vn");
        params.put("p_logoImage", getCompanyLogo(sysConfig));

        String fromDateStr = fromDate != null ? sdfDate.format(fromDate) : "";
        String toDateStr = toDate != null ? sdfDate.format(toDate) : "";
        params.put("p_dateRangeText", "Từ ngày: " + fromDateStr + " đến ngày " + toDateStr);
        params.put("p_reportTitle", "BÁO CÁO TỒN QUỸ");
        params.put("p_reportDateFooter", footerDate);

        return JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(rows));
    }
}