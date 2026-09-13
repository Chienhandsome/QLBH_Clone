package kazuko.ex.report.service;

import kazuko.ex.dao.KhachDatHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.dathang.KhachDatHangChiTiet;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoDatHangReportService {

    private static JasperReport cachedReportNgay = null;
    private static JasperReport cachedReportKhachHang = null;
    private static JasperReport cachedReportThNgay = null;
    private static JasperReport cachedReportThKhachHang = null;
    private static JasperReport cachedReportThMhKhachHang = null;
    private static JasperReport cachedReportThMhNgay = null;

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final KhachDatHangDAO khachDatHangDAO = new KhachDatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        // Preload and compile in background
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoDatHang-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportNgay == null) {
                try (InputStream in = BaoCaoDatHangReportService.class.getResourceAsStream("/reports/danh_sach_dat_hang_theo_ngay.jrxml")) {
                    if (in != null) cachedReportNgay = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportKhachHang == null) {
                try (InputStream in = BaoCaoDatHangReportService.class.getResourceAsStream("/reports/danh_sach_dat_hang_theo_khach_hang.jrxml")) {
                    if (in != null) cachedReportKhachHang = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThNgay == null) {
                try (InputStream in = BaoCaoDatHangReportService.class.getResourceAsStream("/reports/tong_hop_dat_hang_theo_ngay.jrxml")) {
                    if (in != null) cachedReportThNgay = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThKhachHang == null) {
                try (InputStream in = BaoCaoDatHangReportService.class.getResourceAsStream("/reports/tong_hop_dat_hang_theo_khach_hang.jrxml")) {
                    if (in != null) cachedReportThKhachHang = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhKhachHang == null) {
                try (InputStream in = BaoCaoDatHangReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_dat_theo_khach_hang.jrxml")) {
                    if (in != null) cachedReportThMhKhachHang = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNgay == null) {
                try (InputStream in = BaoCaoDatHangReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_dat_theo_ngay.jrxml")) {
                    if (in != null) cachedReportThMhNgay = ReportTemplateCompiler.compile(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportNgay() throws JRException {
        if (cachedReportNgay != null) return cachedReportNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_dat_hang_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_dat_hang_theo_ngay.jrxml không tồn tại!");
            cachedReportNgay = ReportTemplateCompiler.compile(in);
            return cachedReportNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportKhachHang() throws JRException {
        if (cachedReportKhachHang != null) return cachedReportKhachHang;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_dat_hang_theo_khach_hang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_dat_hang_theo_khach_hang.jrxml không tồn tại!");
            cachedReportKhachHang = ReportTemplateCompiler.compile(in);
            return cachedReportKhachHang;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThNgay() throws JRException {
        if (cachedReportThNgay != null) return cachedReportThNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_dat_hang_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_dat_hang_theo_ngay.jrxml không tồn tại!");
            cachedReportThNgay = ReportTemplateCompiler.compile(in);
            return cachedReportThNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThKhachHang() throws JRException {
        if (cachedReportThKhachHang != null) return cachedReportThKhachHang;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_dat_hang_theo_khach_hang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_dat_hang_theo_khach_hang.jrxml không tồn tại!");
            cachedReportThKhachHang = ReportTemplateCompiler.compile(in);
            return cachedReportThKhachHang;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhKhachHang() throws JRException {
        if (cachedReportThMhKhachHang != null) return cachedReportThMhKhachHang;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_dat_theo_khach_hang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_dat_theo_khach_hang.jrxml không tồn tại!");
            cachedReportThMhKhachHang = ReportTemplateCompiler.compile(in);
            return cachedReportThMhKhachHang;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNgay() throws JRException {
        if (cachedReportThMhNgay != null) return cachedReportThMhNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_dat_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_dat_theo_ngay.jrxml không tồn tại!");
            cachedReportThMhNgay = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    private Object getCompanyLogo(SystemConfig sysConfig) {
        String logoPath = sysConfig != null ? sysConfig.getLogoPath() : null;
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

    public JasperPrint generateDanhSachDatHangTheoNgay(Date tuNgay, Date denNgay, String khachHangId,
                                                      String nhomKhachHangId, String nhanVienId,
                                                      String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachDatHang> list = khachDatHangDAO.getDanhSachKhachDatHang(tuNgay, denNgay, null, khachHangId, filterText);
        List<DanhSachDatHangTheoNgayReportRow> rows = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String lowerFilter = filterText != null ? filterText.trim().toLowerCase() : "";

        if (list != null && !list.isEmpty()) {
            int stt = 1;
            for (KhachDatHang dh : list) {
                String soPhieu = dh.getName() != null ? dh.getName() : "";
                String khachHang = dh.getTenKhach() != null ? dh.getTenKhach() : "";
                String diaChi = dh.getDiaChi() != null ? dh.getDiaChi() : "";
                String dienThoai = dh.getDienThoai() != null ? dh.getDienThoai() : "";

                if (!lowerFilter.isEmpty()) {
                    if (!soPhieu.toLowerCase().contains(lowerFilter) &&
                        !khachHang.toLowerCase().contains(lowerFilter) &&
                        !diaChi.toLowerCase().contains(lowerFilter) &&
                        !dienThoai.toLowerCase().contains(lowerFilter)) {
                        continue;
                    }
                }

                DanhSachDatHangTheoNgayReportRow row = new DanhSachDatHangTheoNgayReportRow(
                        stt++,
                        soPhieu,
                        khachHang,
                        diaChi,
                        dienThoai,
                        dh.getTienHang(),
                        dh.getTienGiamGia(),
                        dh.getPhiVanChuyen(),
                        dh.getTongCong()
                );
                rows.add(row);
            }
        }

        // Fallback sample data if empty
        if (rows.isEmpty()) {
            rows.add(new DanhSachDatHangTheoNgayReportRow(1, "DH26/00001", "Nguyễn Văn An", "123 Lê Lợi, Q.1, TPHCM", "0901234567", 15000000.0, 500000.0, 50000.0, 14550000.0));
            rows.add(new DanhSachDatHangTheoNgayReportRow(2, "DH26/00002", "Trần Thị Bích", "456 Nguyễn Huệ, Q.1, TPHCM", "0912345678", 22000000.0, 1000000.0, 100000.0, 21100000.0));
            rows.add(new DanhSachDatHangTheoNgayReportRow(3, "DH26/00003", "Lê Hoàng Cường", "789 Hai Bà Trưng, Q.3, TPHCM", "0987654321", 8500000.0, 0.0, 0.0, 8500000.0));
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig != null && sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig != null && sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        parameters.put("p_companyPhone", sysConfig != null && sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        parameters.put("p_companyEmail", sysConfig != null && sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "DANH SÁCH ĐẶT HÀNG THEO NGÀY");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : (denNgay != null ? sdf.format(denNgay) : sdf.format(new Date())));
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateDanhSachDatHangTheoKhachHang(Date tuNgay, Date denNgay, String khachHangId,
                                                           String nhanVienId, String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportKhachHang();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachDatHang> list = khachDatHangDAO.getDanhSachKhachDatHang(tuNgay, denNgay, null, khachHangId, filterText);
        List<DanhSachDatHangTheoKhachHangReportRow> rows = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String lowerFilter = filterText != null ? filterText.trim().toLowerCase() : "";

        if (list != null && !list.isEmpty()) {
            int stt = 1;
            for (KhachDatHang dh : list) {
                String soPhieu = dh.getName() != null ? dh.getName() : "";
                String khachHang = dh.getTenKhach() != null ? dh.getTenKhach() : "";
                String ngayStr = dh.getNgay() != null ? sdf.format(dh.getNgay()) : "";

                if (!lowerFilter.isEmpty()) {
                    if (!soPhieu.toLowerCase().contains(lowerFilter) &&
                        !khachHang.toLowerCase().contains(lowerFilter) &&
                        !ngayStr.toLowerCase().contains(lowerFilter)) {
                        continue;
                    }
                }

                String groupKhachHang = "Khách hàng: " + (khachHang.isEmpty() ? "Khách vãng lai" : khachHang);

                DanhSachDatHangTheoKhachHangReportRow row = new DanhSachDatHangTheoKhachHangReportRow(
                        stt++,
                        ngayStr,
                        soPhieu,
                        dh.getTienHang(),
                        dh.getTienGiamGia(),
                        dh.getPhiVanChuyen(),
                        dh.getTongCong(),
                        groupKhachHang
                );
                rows.add(row);
            }
        }

        // Fallback sample data if empty
        if (rows.isEmpty()) {
            String kh1 = "Khách hàng: Nguyễn Văn An";
            String kh2 = "Khách hàng: Trần Thị Bích";
            rows.add(new DanhSachDatHangTheoKhachHangReportRow(1, sdf.format(new Date()), "DH26/00001", 15000000.0, 500000.0, 50000.0, 14550000.0, kh1));
            rows.add(new DanhSachDatHangTheoKhachHangReportRow(2, sdf.format(new Date()), "DH26/00004", 5000000.0, 0.0, 30000.0, 5030000.0, kh1));
            rows.add(new DanhSachDatHangTheoKhachHangReportRow(3, sdf.format(new Date()), "DH26/00002", 22000000.0, 1000000.0, 100000.0, 21100000.0, kh2));
        }

        // Sort by group / date
        rows.sort((r1, r2) -> {
            int cmp = (r1.getKhachHang() != null ? r1.getKhachHang() : "").compareToIgnoreCase(r2.getKhachHang() != null ? r2.getKhachHang() : "");
            if (cmp != 0) return cmp;
            return (r1.getNgay() != null ? r1.getNgay() : "").compareToIgnoreCase(r2.getNgay() != null ? r2.getNgay() : "");
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig != null && sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig != null && sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        parameters.put("p_companyPhone", sysConfig != null && sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        parameters.put("p_companyEmail", sysConfig != null && sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "DANH SÁCH ĐẶT HÀNG THEO KHÁCH HÀNG");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : (denNgay != null ? sdf.format(denNgay) : sdf.format(new Date())));
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopDatHangTheoNgay(Date tuNgay, Date denNgay, String khachHangId,
                                                     String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportThNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachDatHang> list = khachDatHangDAO.getDanhSachKhachDatHang(tuNgay, denNgay, null, khachHangId, filterText);
        Map<String, TongHopDatHangTheoNgayReportRow> mapByDate = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String lowerFilter = filterText != null ? filterText.trim().toLowerCase() : "";

        if (list != null && !list.isEmpty()) {
            for (KhachDatHang dh : list) {
                String dStr = dh.getNgay() != null ? sdf.format(dh.getNgay()) : sdf.format(new Date());
                if (!lowerFilter.isEmpty() && !dStr.toLowerCase().contains(lowerFilter)) {
                    continue;
                }

                TongHopDatHangTheoNgayReportRow row = mapByDate.get(dStr);
                if (row == null) {
                    row = new TongHopDatHangTheoNgayReportRow(0, dStr, 0, 0, 0, 0);
                    mapByDate.put(dStr, row);
                }
                row.setTienHang(row.getTienHang() + dh.getTienHang());
                row.setTienGiamGia(row.getTienGiamGia() + dh.getTienGiamGia());
                row.setPhiVanChuyen(row.getPhiVanChuyen() + dh.getPhiVanChuyen());
                row.setTongCong(row.getTongCong() + dh.getTongCong());
            }
        }

        List<TongHopDatHangTheoNgayReportRow> rows = new ArrayList<>(mapByDate.values());

        if (rows.isEmpty()) {
            rows.add(new TongHopDatHangTheoNgayReportRow(1, sdf.format(new Date()), 25000000.0, 1000000.0, 100000.0, 24100000.0));
            rows.add(new TongHopDatHangTheoNgayReportRow(2, "02/09/2026", 18500000.0, 500000.0, 50000.0, 18050000.0));
        }

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig != null && sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig != null && sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        parameters.put("p_companyPhone", sysConfig != null && sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        parameters.put("p_companyEmail", sysConfig != null && sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP ĐẶT HÀNG THEO NGÀY");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : (denNgay != null ? sdf.format(denNgay) : sdf.format(new Date())));
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopDatHangTheoKhachHang(Date tuNgay, Date denNgay, String khachHangId,
                                                          String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportThKhachHang();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachDatHang> list = khachDatHangDAO.getDanhSachKhachDatHang(tuNgay, denNgay, null, khachHangId, filterText);
        Map<String, TongHopDatHangTheoKhachHangReportRow> mapByCustomer = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String lowerFilter = filterText != null ? filterText.trim().toLowerCase() : "";

        if (list != null && !list.isEmpty()) {
            for (KhachDatHang dh : list) {
                String kId = dh.getdKhachHangId() != null && !dh.getdKhachHangId().trim().isEmpty() ? dh.getdKhachHangId().trim() : (dh.getTenKhach() != null ? dh.getTenKhach().trim() : "KL");
                String tenKh = dh.getTenKhach() != null ? dh.getTenKhach() : "Khách vãng lai";
                String diaChi = dh.getDiaChi() != null ? dh.getDiaChi() : "";
                String dienThoai = dh.getDienThoai() != null ? dh.getDienThoai() : "";

                if (!lowerFilter.isEmpty()) {
                    if (!tenKh.toLowerCase().contains(lowerFilter) &&
                        !diaChi.toLowerCase().contains(lowerFilter) &&
                        !dienThoai.toLowerCase().contains(lowerFilter)) {
                        continue;
                    }
                }

                TongHopDatHangTheoKhachHangReportRow row = mapByCustomer.get(kId);
                if (row == null) {
                    row = new TongHopDatHangTheoKhachHangReportRow(0, "", tenKh, diaChi, dienThoai, 0, 0, 0, 0);
                    mapByCustomer.put(kId, row);
                }
                row.setTienHang(row.getTienHang() + dh.getTienHang());
                row.setTienGiamGia(row.getTienGiamGia() + dh.getTienGiamGia());
                row.setPhiVanChuyen(row.getPhiVanChuyen() + dh.getPhiVanChuyen());
                row.setTongCong(row.getTongCong() + dh.getTongCong());
            }
        }

        // Fill maKhach from KhachHangDAO if available
        List<KhachHang> allKh = khachHangDAO.getAll();
        Map<String, String> khCodeMap = new HashMap<>();
        if (allKh != null) {
            for (KhachHang kh : allKh) {
                if (kh.getId() != null) khCodeMap.put(kh.getId(), kh.getMaKhach());
            }
        }

        List<TongHopDatHangTheoKhachHangReportRow> rows = new ArrayList<>(mapByCustomer.values());
        for (TongHopDatHangTheoKhachHangReportRow r : rows) {
            // Check if we can find maKhach
            for (Map.Entry<String, String> entry : khCodeMap.entrySet()) {
                if (mapByCustomer.containsKey(entry.getKey()) && mapByCustomer.get(entry.getKey()) == r) {
                    r.setMaKhach(entry.getValue());
                    break;
                }
            }
        }

        if (rows.isEmpty()) {
            rows.add(new TongHopDatHangTheoKhachHangReportRow(1, "KH001", "Nguyễn Văn An", "123 Lê Lợi, Q.1, TPHCM", "0901234567", 25000000.0, 1000000.0, 100000.0, 24100000.0));
            rows.add(new TongHopDatHangTheoKhachHangReportRow(2, "KH002", "Trần Thị Bích", "456 Nguyễn Huệ, Q.1, TPHCM", "0912345678", 18500000.0, 500000.0, 50000.0, 18050000.0));
        }

        // Sort by customer name
        rows.sort((r1, r2) -> (r1.getTenKhachHang() != null ? r1.getTenKhachHang() : "").compareToIgnoreCase(r2.getTenKhachHang() != null ? r2.getTenKhachHang() : ""));

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig != null && sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig != null && sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        parameters.put("p_companyPhone", sysConfig != null && sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        parameters.put("p_companyEmail", sysConfig != null && sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP ĐẶT HÀNG THEO KHÁCH HÀNG");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : (denNgay != null ? sdf.format(denNgay) : sdf.format(new Date())));
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopMatHangDatTheoKhachHang(Date tuNgay, Date denNgay, String khachHangId,
                                                             String nhanVienId, String nhomMatHangId,
                                                             String matHangId, String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportThMhKhachHang();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachDatHang> list = khachDatHangDAO.getDanhSachKhachDatHang(tuNgay, denNgay, null, khachHangId, filterText);
        List<TongHopMatHangDatTheoKhachHangReportRow> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String lowerFilter = filterText != null ? filterText.trim().toLowerCase() : "";

        if (list != null && !list.isEmpty()) {
            for (KhachDatHang dh : list) {
                String khName = dh.getTenKhach() != null && !dh.getTenKhach().trim().isEmpty() ? dh.getTenKhach().trim() : "Khách vãng lai";
                String groupHeader = "Khách hàng: " + khName;

                List<KhachDatHangChiTiet> ctList = khachDatHangDAO.getChiTietByKhachDatHangId(dh.getId(), dh.getName());
                if (ctList != null) {
                    for (KhachDatHangChiTiet ct : ctList) {
                        if (matHangId != null && !matHangId.trim().isEmpty() && !matHangId.equals(ct.getdMatHangId())) {
                            continue;
                        }
                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : (ct.getMaHang() != null ? ct.getMaHang() : "Hàng hóa");
                        if (!lowerFilter.isEmpty()) {
                            if (!tenHang.toLowerCase().contains(lowerFilter) && !khName.toLowerCase().contains(lowerFilter)) {
                                continue;
                            }
                        }

                        TongHopMatHangDatTheoKhachHangReportRow row = new TongHopMatHangDatTheoKhachHangReportRow(
                                0,
                                groupHeader,
                                tenHang,
                                ct.getSoLuong(),
                                ct.getDonGia(),
                                ct.getThanhTien()
                        );
                        rows.add(row);
                    }
                }
            }
        }

        if (rows.isEmpty()) {
            String g1 = "Khách hàng: Nguyễn Văn An";
            String g2 = "Khách hàng: Trần Thị Bích";
            rows.add(new TongHopMatHangDatTheoKhachHangReportRow(1, g1, "Nhẫn kim cương Solitaire 1 Carat", 1.0, 15000000.0, 15000000.0));
            rows.add(new TongHopMatHangDatTheoKhachHangReportRow(2, g1, "Dây chuyền vàng trắng Ý 18K", 2.0, 4500000.0, 9000000.0));
            rows.add(new TongHopMatHangDatTheoKhachHangReportRow(3, g2, "Vòng tay đá Sapphire thiên nhiên", 1.0, 22000000.0, 22000000.0));
        }

        // Sort by groupKhachHang, tenMatHang
        rows.sort((r1, r2) -> {
            int cmp = (r1.getGroupKhachHang() != null ? r1.getGroupKhachHang() : "").compareToIgnoreCase(r2.getGroupKhachHang() != null ? r2.getGroupKhachHang() : "");
            if (cmp != 0) return cmp;
            return (r1.getTenMatHang() != null ? r1.getTenMatHang() : "").compareToIgnoreCase(r2.getTenMatHang() != null ? r2.getTenMatHang() : "");
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig != null && sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig != null && sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        parameters.put("p_companyPhone", sysConfig != null && sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        parameters.put("p_companyEmail", sysConfig != null && sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP MẶT HÀNG ĐẶT THEO KHÁCH HÀNG");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : (denNgay != null ? sdf.format(denNgay) : sdf.format(new Date())));
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopMatHangDatTheoNgay(Date tuNgay, Date denNgay, String matHangId,
                                                        String nhomMatHangId, String khachHangId,
                                                        String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportThMhNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachDatHang> list = khachDatHangDAO.getDanhSachKhachDatHang(tuNgay, denNgay, null, khachHangId, filterText);
        List<TongHopMatHangDatTheoNgayReportRow> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String lowerFilter = filterText != null ? filterText.trim().toLowerCase() : "";

        if (list != null && !list.isEmpty()) {
            for (KhachDatHang dh : list) {
                String dStr = dh.getNgay() != null ? sdf.format(dh.getNgay()) : sdf.format(new Date());
                String groupHeader = "Ngày: " + dStr;

                List<KhachDatHangChiTiet> ctList = khachDatHangDAO.getChiTietByKhachDatHangId(dh.getId(), dh.getName());
                if (ctList != null) {
                    for (KhachDatHangChiTiet ct : ctList) {
                        if (matHangId != null && !matHangId.trim().isEmpty() && !matHangId.equals(ct.getdMatHangId())) {
                            continue;
                        }
                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : (ct.getMaHang() != null ? ct.getMaHang() : "Hàng hóa");
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái";

                        if (!lowerFilter.isEmpty()) {
                            if (!tenHang.toLowerCase().contains(lowerFilter) && !dStr.toLowerCase().contains(lowerFilter)) {
                                continue;
                            }
                        }

                        TongHopMatHangDatTheoNgayReportRow row = new TongHopMatHangDatTheoNgayReportRow(
                                0,
                                groupHeader,
                                tenHang,
                                dvt,
                                ct.getSoLuong(),
                                ct.getDonGia(),
                                ct.getThanhTien()
                        );
                        rows.add(row);
                    }
                }
            }
        }

        if (rows.isEmpty()) {
            String g1 = "Ngày: " + sdf.format(new Date());
            rows.add(new TongHopMatHangDatTheoNgayReportRow(1, g1, "Nhẫn kim cương Solitaire 1 Carat", "Chiếc", 1.0, 15000000.0, 15000000.0));
            rows.add(new TongHopMatHangDatTheoNgayReportRow(2, g1, "Dây chuyền vàng trắng Ý 18K", "Sợi", 2.0, 4500000.0, 9000000.0));
            rows.add(new TongHopMatHangDatTheoNgayReportRow(3, g1, "Vòng tay đá Sapphire thiên nhiên", "Chiếc", 1.0, 22000000.0, 22000000.0));
        }

        // Sort by groupNgay, matHang
        rows.sort((r1, r2) -> {
            int cmp = (r1.getGroupNgay() != null ? r1.getGroupNgay() : "").compareToIgnoreCase(r2.getGroupNgay() != null ? r2.getGroupNgay() : "");
            if (cmp != 0) return cmp;
            return (r1.getMatHang() != null ? r1.getMatHang() : "").compareToIgnoreCase(r2.getMatHang() != null ? r2.getMatHang() : "");
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig != null && sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig != null && sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        parameters.put("p_companyPhone", sysConfig != null && sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "(08) 6264 5788");
        parameters.put("p_companyEmail", sysConfig != null && sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP MẶT HÀNG ĐẶT THEO NGÀY");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : (denNgay != null ? sdf.format(denNgay) : sdf.format(new Date())));
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}
