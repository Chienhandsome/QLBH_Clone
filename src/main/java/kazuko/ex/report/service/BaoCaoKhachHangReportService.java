package kazuko.ex.report.service;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.dao.TangGiamDiemDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhachHangLoyaltySummary;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.KhachHangReportRow;
import kazuko.ex.report.model.KhachHangSinhNhatReportRow;
import kazuko.ex.report.model.KhachHangThanThietReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoKhachHangReportService {

    private static JasperReport cachedReportNhom = null;
    private static JasperReport cachedReportNhanVien = null;
    private static JasperReport cachedReportSinhNhat = null;
    private static JasperReport cachedReportThanThiet = null;
    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final TangGiamDiemDAO tangGiamDiemDAO = new TangGiamDiemDAO();

    static {
        // Preload and compile in background
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoKhachHang-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportNhom == null) {
                try (InputStream in = BaoCaoKhachHangReportService.class.getResourceAsStream("/reports/danh_sach_khach_hang_theo_nhom.jrxml")) {
                    if (in != null) cachedReportNhom = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportNhanVien == null) {
                try (InputStream in = BaoCaoKhachHangReportService.class.getResourceAsStream("/reports/danh_sach_khach_hang_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportNhanVien = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportSinhNhat == null) {
                try (InputStream in = BaoCaoKhachHangReportService.class.getResourceAsStream("/reports/khach_hang_den_ngay_sinh_nhat.jrxml")) {
                    if (in != null) cachedReportSinhNhat = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportThanThiet == null) {
                try (InputStream in = BaoCaoKhachHangReportService.class.getResourceAsStream("/reports/khach_hang_than_thiet.jrxml")) {
                    if (in != null) cachedReportThanThiet = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportThanThiet() throws JRException {
        if (cachedReportThanThiet != null) return cachedReportThanThiet;
        try (InputStream in = getClass().getResourceAsStream("/reports/khach_hang_than_thiet.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/khach_hang_than_thiet.jrxml không tồn tại!");
            cachedReportThanThiet = JasperCompileManager.compileReport(in);
            return cachedReportThanThiet;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportNhom() throws JRException {
        if (cachedReportNhom != null) return cachedReportNhom;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_khach_hang_theo_nhom.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_khach_hang_theo_nhom.jrxml không tồn tại!");
            cachedReportNhom = JasperCompileManager.compileReport(in);
            return cachedReportNhom;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportNhanVien() throws JRException {
        if (cachedReportNhanVien != null) return cachedReportNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_khach_hang_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_khach_hang_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportNhanVien = JasperCompileManager.compileReport(in);
            return cachedReportNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportSinhNhat() throws JRException {
        if (cachedReportSinhNhat != null) return cachedReportSinhNhat;
        try (InputStream in = getClass().getResourceAsStream("/reports/khach_hang_den_ngay_sinh_nhat.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/khach_hang_den_ngay_sinh_nhat.jrxml không tồn tại!");
            cachedReportSinhNhat = JasperCompileManager.compileReport(in);
            return cachedReportSinhNhat;
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

    public JasperPrint generateKhachHangTheoNhom(String nhomKhachHangId, String nhanVienId) throws JRException {
        JasperReport jasperReport = getCompiledReportNhom();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachHang> allKh = khachHangDAO.getAll();
        List<KhachHangReportRow> rows = new ArrayList<>();

        for (KhachHang kh : allKh) {
            if (kh.getStatus() < 0) continue;

            if (nhomKhachHangId != null && !nhomKhachHangId.trim().isEmpty()) {
                if (!nhomKhachHangId.equals(kh.getNhomKhachHangId())) {
                    continue;
                }
            }

            if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
                if (!nhanVienId.equals(kh.getNhanVienId())) {
                    continue;
                }
            }

            String groupHeader = "Nhóm khách hàng: " + (kh.getTenNhomKhachHang() != null && !kh.getTenNhomKhachHang().trim().isEmpty() ? kh.getTenNhomKhachHang() : "Khác");
            
            KhachHangReportRow row = new KhachHangReportRow(
                    0,
                    groupHeader,
                    kh.getMaKhach() != null ? kh.getMaKhach() : "",
                    kh.getName() != null ? kh.getName() : "",
                    kh.getDiaChi() != null ? kh.getDiaChi() : "",
                    kh.getDienThoai() != null ? kh.getDienThoai() : "",
                    kh.getEmail() != null ? kh.getEmail() : ""
            );
            rows.add(row);
        }

        // Sort by groupHeader, then maKhach / name
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaKhach().compareToIgnoreCase(r2.getMaKhach());
        });

        // Set STT
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "DANH SÁCH KHÁCH HÀNG THEO NHÓM");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateKhachHangTheoNhanVien(String nhomKhachHangId, String nhanVienId) throws JRException {
        JasperReport jasperReport = getCompiledReportNhanVien();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachHang> allKh = khachHangDAO.getAll();
        List<KhachHangReportRow> rows = new ArrayList<>();

        for (KhachHang kh : allKh) {
            if (kh.getStatus() < 0) continue;

            if (nhomKhachHangId != null && !nhomKhachHangId.trim().isEmpty()) {
                if (!nhomKhachHangId.equals(kh.getNhomKhachHangId())) {
                    continue;
                }
            }

            if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
                if (!nhanVienId.equals(kh.getNhanVienId())) {
                    continue;
                }
            }

            String groupHeader = "Nhân viên: " + (kh.getTenNhanVien() != null && !kh.getTenNhanVien().trim().isEmpty() ? kh.getTenNhanVien() : "Chưa phân công");
            
            KhachHangReportRow row = new KhachHangReportRow(
                    0,
                    groupHeader,
                    kh.getMaKhach() != null ? kh.getMaKhach() : "",
                    kh.getName() != null ? kh.getName() : "",
                    kh.getDiaChi() != null ? kh.getDiaChi() : "",
                    kh.getDienThoai() != null ? kh.getDienThoai() : "",
                    kh.getEmail() != null ? kh.getEmail() : ""
            );
            rows.add(row);
        }

        // Sort by groupHeader, then maKhach / name
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaKhach().compareToIgnoreCase(r2.getMaKhach());
        });

        // Set STT
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateKhachHangSinhNhat(String nhomKhachHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportSinhNhat();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachHang> allKh = khachHangDAO.getAll();
        List<KhachHangSinhNhatReportRow> rows = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        int curDay = today.get(Calendar.DAY_OF_MONTH);
        int curMonth = today.get(Calendar.MONTH);

        for (KhachHang kh : allKh) {
            if (kh.getStatus() < 0) continue;

            if (nhomKhachHangId != null && !nhomKhachHangId.trim().isEmpty()) {
                if (!nhomKhachHangId.equals(kh.getNhomKhachHangId())) {
                    continue;
                }
            }

            // Check if birthday is today
            if (kh.getNgaySinhNhat() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(kh.getNgaySinhNhat());
                if (cal.get(Calendar.MONTH) == curMonth && cal.get(Calendar.DAY_OF_MONTH) == curDay) {
                    KhachHangSinhNhatReportRow row = new KhachHangSinhNhatReportRow(
                            0,
                            kh.getName() != null ? kh.getName() : "",
                            kh.getMaKhach() != null ? kh.getMaKhach() : "",
                            kh.getDiaChi() != null ? kh.getDiaChi() : "",
                            kh.getDienThoai() != null ? kh.getDienThoai() : "",
                            kh.getEmail() != null ? kh.getEmail() : ""
                    );
                    rows.add(row);
                }
            }
        }

        // Sort by name
        rows.sort((r1, r2) -> r1.getTenKhachHang().compareToIgnoreCase(r2.getTenKhachHang()));

        // Set STT
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "KHÁCH HÀNG ĐẾN NGÀY SINH NHẬT");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateKhachHangThanThiet(Date fromDate, Date toDate, String nhomKhachHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportThanThiet();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<KhachHangLoyaltySummary> loyaltyList = tangGiamDiemDAO.getLoyaltySummaryList(100000.0);
        List<KhachHangThanThietReportRow> rows = new ArrayList<>();

        for (KhachHangLoyaltySummary item : loyaltyList) {
            KhachHang kh = item.getKhachHang();
            if (kh == null || kh.getStatus() < 0) continue;

            if (nhomKhachHangId != null && !nhomKhachHangId.trim().isEmpty()) {
                if (!nhomKhachHangId.equals(kh.getNhomKhachHangId())) {
                    continue;
                }
            }

            String groupHeader = "Nhóm khách hàng: " + (kh.getTenNhomKhachHang() != null && !kh.getTenNhomKhachHang().trim().isEmpty() ? kh.getTenNhomKhachHang() : "Khác");

            KhachHangThanThietReportRow row = new KhachHangThanThietReportRow(
                    0,
                    groupHeader,
                    kh.getName() != null ? kh.getName() : "",
                    kh.getDiaChi() != null ? kh.getDiaChi() : "",
                    kh.getDienThoai() != null ? kh.getDienThoai() : "",
                    item.getDoanhSo(),
                    item.getDiemTichLuy(),
                    0.0,
                    item.getDiemTichLuy()
            );
            rows.add(row);
        }

        // Sort by groupHeader, then tenKhachHang
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getTenKhachHang().compareToIgnoreCase(r2.getTenKhachHang());
        });

        // Set STT
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "KHÁCH HÀNG THÂN THIẾT");
        parameters.put("p_tuNgay", fromDate != null ? sdf.format(fromDate) : "");
        parameters.put("p_denNgay", toDate != null ? sdf.format(toDate) : "");
        parameters.put("p_nhomKhach", nhomKhachHangId != null && !nhomKhachHangId.trim().isEmpty() ? nhomKhachHangId : "Tất cả");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}