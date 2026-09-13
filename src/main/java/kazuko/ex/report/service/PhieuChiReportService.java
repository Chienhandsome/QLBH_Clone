package kazuko.ex.report.service;

import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.PhieuChiReportRow;
import kazuko.ex.report.model.PhieuChiTheoLyDoReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class PhieuChiReportService {

    private static JasperReport cachedJasperReport = null;
    private static JasperReport cachedJasperReportByReason = null;
    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;
    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        // Pre-load và compile sẵn report ngầm trong nền ngay khi ứng dụng khởi chạy
        new Thread(() -> {
            try {
                preload();
                preloadByReason();
            } catch (Exception ignored) {}
        }, "ReportChi-Preload-Thread").start();
    }

    public static synchronized void preload() {
        if (cachedJasperReport == null) {
            try (InputStream in = PhieuChiReportService.class.getResourceAsStream("/reports/danh_sach_phieu_chi_theo_ngay.jrxml")) {
                if (in != null) {
                    cachedJasperReport = ReportTemplateCompiler.compile(in);
                }
            } catch (Exception ignored) {}
        }
    }

    public static synchronized void preloadByReason() {
        if (cachedJasperReportByReason == null) {
            try (InputStream in = PhieuChiReportService.class.getResourceAsStream("/reports/danh_sach_phieu_chi_theo_ly_do.jrxml")) {
                if (in != null) {
                    cachedJasperReportByReason = ReportTemplateCompiler.compile(in);
                }
            } catch (Exception ignored) {}
        }
    }

    public synchronized JasperReport getCompiledReport() throws JRException {
        if (cachedJasperReport != null) {
            return cachedJasperReport;
        }

        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_chi_theo_ngay.jrxml")) {
            if (in == null) {
                throw new JRException("Không tìm thấy file mẫu báo cáo /reports/danh_sach_phieu_chi_theo_ngay.jrxml trong resources!");
            }
            cachedJasperReport = ReportTemplateCompiler.compile(in);
            return cachedJasperReport;
        } catch (Exception e) {
            throw new JRException("Lỗi khi biên dịch mẫu báo cáo JRXML: " + e.getMessage(), e);
        }
    }

    public synchronized JasperReport getCompiledReportByReason() throws JRException {
        if (cachedJasperReportByReason != null) {
            return cachedJasperReportByReason;
        }

        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_chi_theo_ly_do.jrxml")) {
            if (in == null) {
                throw new JRException("Không tìm thấy file mẫu báo cáo /reports/danh_sach_phieu_chi_theo_ly_do.jrxml trong resources!");
            }
            cachedJasperReportByReason = ReportTemplateCompiler.compile(in);
            return cachedJasperReportByReason;
        } catch (Exception e) {
            throw new JRException("Lỗi khi biên dịch mẫu báo cáo JRXML: " + e.getMessage(), e);
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

    public JasperPrint generateReportPrint(Date fromDate, Date toDate, String lyDoThuChiId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReport();

        // 1. Lấy thông tin công ty và Logo từ Cấu hình hệ thống -> Thông tin chung
        SystemConfig sysConfig = configDAO.loadConfig();
        String companyName = sysConfig.getTenCongTy();
        String companyAddress = sysConfig.getDiaChi();
        String companyPhone = sysConfig.getSoDienThoai();
        String companyEmail = sysConfig.getEmail();
        Object logoImage = getCompanyLogo(sysConfig);

        // 2. Truy vấn dữ liệu phiếu chi theo ngày và điều kiện lọc
        List<PhieuChi> phieuChiList = phieuChiDAO.getReportPhieuChiTheoNgay(fromDate, toDate, lyDoThuChiId, cuaHangId);

        // 3. Map sang danh sách dòng dữ liệu PhieuChiReportRow
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        List<PhieuChiReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (PhieuChi pc : phieuChiList) {
            String dateGroupStr = "Ngày: " + (pc.getNgay() != null ? sdfDate.format(pc.getNgay()) : "");
            String soPhieu = pc.getSoPhieu() != null ? pc.getSoPhieu() : "";
            String tenDoiTuong = pc.getTenDoiTuong() != null ? pc.getTenDoiTuong() : "";
            String diaChi = pc.getDiaChi() != null ? pc.getDiaChi() : "";
            String lyDoThuChi = (pc.getTenPhanLoai() != null && !pc.getTenPhanLoai().isEmpty()) ? pc.getTenPhanLoai() : (pc.getLyDoChi() != null ? pc.getLyDoChi() : "");
            String dienGiai = pc.getGhiChu() != null ? pc.getGhiChu() : "";
            String chungTuGoc = pc.getChungTuGoc() != null ? pc.getChungTuGoc() : "";
            Double soTien = pc.getSoTien();

            PhieuChiReportRow row = new PhieuChiReportRow(stt++, soPhieu, dateGroupStr, tenDoiTuong, diaChi, lyDoThuChi, dienGiai, chungTuGoc, soTien);
            rows.add(row);
        }

        // 4. Thiết lập tham số Report Parameters
        Map<String, Object> params = new HashMap<>();
        params.put("p_companyName", (companyName != null && !companyName.isEmpty()) ? companyName : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        params.put("p_companyAddress", (companyAddress != null && !companyAddress.isEmpty()) ? companyAddress : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        params.put("p_companyPhone", (companyPhone != null && !companyPhone.isEmpty()) ? companyPhone : "(08) 6264 5788");
        params.put("p_companyEmail", companyEmail != null ? companyEmail : "info@ngocchauau.vn");
        params.put("p_logoImage", logoImage);

        String fromDateStr = fromDate != null ? sdfDate.format(fromDate) : "";
        String toDateStr = toDate != null ? sdfDate.format(toDate) : "";
        params.put("p_dateRangeText", "Ngày từ " + fromDateStr + " đến " + toDateStr);
        params.put("p_reportTitle", "DANH SÁCH PHIẾU CHI THEO NGÀY");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        return JasperFillManager.fillReport(jasperReport, params, dataSource);
    }

    public JasperPrint generateReportByReasonPrint(Date fromDate, Date toDate, String lyDoThuChiId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportByReason();

        // 1. Lấy thông tin công ty và Logo từ Cấu hình hệ thống -> Thông tin chung
        SystemConfig sysConfig = configDAO.loadConfig();
        String companyName = sysConfig.getTenCongTy();
        String companyAddress = sysConfig.getDiaChi();
        String companyPhone = sysConfig.getSoDienThoai();
        String companyEmail = sysConfig.getEmail();
        Object logoImage = getCompanyLogo(sysConfig);

        // 2. Truy vấn dữ liệu phiếu chi
        List<PhieuChi> phieuChiList = phieuChiDAO.getReportPhieuChiTheoNgay(fromDate, toDate, lyDoThuChiId, cuaHangId);

        // 3. Sắp xếp theo lý do thu chi để JasperReports gom nhóm chính xác
        phieuChiList.sort((o1, o2) -> {
            String r1 = (o1.getTenPhanLoai() != null && !o1.getTenPhanLoai().trim().isEmpty())
                    ? o1.getTenPhanLoai().trim()
                    : ((o1.getLyDoChi() != null && !o1.getLyDoChi().trim().isEmpty()) ? o1.getLyDoChi().trim() : "Khác");
            String r2 = (o2.getTenPhanLoai() != null && !o2.getTenPhanLoai().trim().isEmpty())
                    ? o2.getTenPhanLoai().trim()
                    : ((o2.getLyDoChi() != null && !o2.getLyDoChi().trim().isEmpty()) ? o2.getLyDoChi().trim() : "Khác");
            int c = r1.compareToIgnoreCase(r2);
            if (c != 0) return c;
            String p1 = o1.getSoPhieu() != null ? o1.getSoPhieu() : "";
            String p2 = o2.getSoPhieu() != null ? o2.getSoPhieu() : "";
            return p1.compareTo(p2);
        });

        // 4. Map sang danh sách dòng dữ liệu PhieuChiTheoLyDoReportRow
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        List<PhieuChiTheoLyDoReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (PhieuChi pc : phieuChiList) {
            String lyDo = (pc.getTenPhanLoai() != null && !pc.getTenPhanLoai().trim().isEmpty())
                    ? pc.getTenPhanLoai().trim()
                    : ((pc.getLyDoChi() != null && !pc.getLyDoChi().trim().isEmpty()) ? pc.getLyDoChi().trim() : "Khác");
            String lyDoGroup = "Lý do thu chi: " + lyDo;
            String soPhieu = pc.getSoPhieu() != null ? pc.getSoPhieu() : "";
            String ngayStr = pc.getNgay() != null ? sdfDate.format(pc.getNgay()) : "";
            String tenDoiTuong = pc.getTenDoiTuong() != null ? pc.getTenDoiTuong() : "";
            String diaChi = pc.getDiaChi() != null ? pc.getDiaChi() : "";
            String dienGiai = pc.getGhiChu() != null ? pc.getGhiChu() : "";
            String chungTuGoc = pc.getChungTuGoc() != null ? pc.getChungTuGoc() : "";
            Double soTien = pc.getSoTien();

            PhieuChiTheoLyDoReportRow row = new PhieuChiTheoLyDoReportRow(
                    stt++, soPhieu, ngayStr, lyDoGroup, tenDoiTuong, diaChi, dienGiai, chungTuGoc, soTien
            );
            rows.add(row);
        }

        // 5. Thiết lập tham số Report Parameters
        Map<String, Object> params = new HashMap<>();
        params.put("p_companyName", (companyName != null && !companyName.isEmpty()) ? companyName : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        params.put("p_companyAddress", (companyAddress != null && !companyAddress.isEmpty()) ? companyAddress : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        params.put("p_companyPhone", (companyPhone != null && !companyPhone.isEmpty()) ? companyPhone : "(08) 6264 5788");
        params.put("p_companyEmail", companyEmail != null ? companyEmail : "info@ngocchauau.vn");
        params.put("p_logoImage", logoImage);

        String fromDateStr = fromDate != null ? sdfDate.format(fromDate) : "";
        String toDateStr = toDate != null ? sdfDate.format(toDate) : "";
        params.put("p_dateRangeText", "Ngày từ " + fromDateStr + " đến " + toDateStr);
        params.put("p_reportTitle", "DANH SÁCH PHIẾU CHI THEO LÝ DO THU CHI");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        return JasperFillManager.fillReport(jasperReport, params, dataSource);
    }
}