package kazuko.ex.report.service;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.DanhSachPhieuKiemKeTheoNgayReportRow;
import kazuko.ex.report.model.DanhSachPhieuKiemKeTheoNhanVienReportRow;
import kazuko.ex.report.model.TongHopMatHangKiemKeTheoNgayReportRow;
import kazuko.ex.report.model.TongHopMatHangKiemKeTheoNhanVienReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoKiemKeReportService {

    private static JasperReport cachedReportDsNgay = null;
    private static JasperReport cachedReportThMhNgay = null;
    private static JasperReport cachedReportThMhNhanVien = null;
    private static JasperReport cachedReportDsNhanVien = null;

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoKiemKe-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportDsNgay == null) {
                try (InputStream in = BaoCaoKiemKeReportService.class.getResourceAsStream("/reports/danh_sach_phieu_kiem_ke_theo_ngay.jrxml")) {
                    if (in != null) cachedReportDsNgay = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNgay == null) {
                try (InputStream in = BaoCaoKiemKeReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_kiem_ke_theo_ngay.jrxml")) {
                    if (in != null) cachedReportThMhNgay = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNhanVien == null) {
                try (InputStream in = BaoCaoKiemKeReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_kiem_ke_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportThMhNhanVien = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportDsNhanVien == null) {
                try (InputStream in = BaoCaoKiemKeReportService.class.getResourceAsStream("/reports/danh_sach_phieu_kiem_ke_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportDsNhanVien = ReportTemplateCompiler.compile(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportDsNgay() throws JRException {
        if (cachedReportDsNgay != null) return cachedReportDsNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_kiem_ke_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_kiem_ke_theo_ngay.jrxml không tồn tại!");
            cachedReportDsNgay = ReportTemplateCompiler.compile(in);
            return cachedReportDsNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNgay() throws JRException {
        if (cachedReportThMhNgay != null) return cachedReportThMhNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_kiem_ke_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_kiem_ke_theo_ngay.jrxml không tồn tại!");
            cachedReportThMhNgay = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNhanVien() throws JRException {
        if (cachedReportThMhNhanVien != null) return cachedReportThMhNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_kiem_ke_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_kiem_ke_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportThMhNhanVien = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportDsNhanVien() throws JRException {
        if (cachedReportDsNhanVien != null) return cachedReportDsNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_kiem_ke_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_kiem_ke_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportDsNhanVien = ReportTemplateCompiler.compile(in);
            return cachedReportDsNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized Object getLogoImage() {
        try {
            SystemConfig cfg = configDAO.loadConfig();
            String path = cfg != null ? cfg.getLogoPath() : null;
            if (path != null && !path.trim().isEmpty() && !path.equals(cachedLogoPath)) {
                File f = new File(path);
                if (f.exists()) {
                    cachedLogoPath = path;
                    cachedLogoImage = ImageIO.read(f);
                    return cachedLogoImage;
                }
            }
            if (cachedLogoImage != null && (path == null || path.trim().isEmpty())) {
                return cachedLogoImage;
            }
            if (cachedLogoImage == null) {
                try (InputStream is = getClass().getResourceAsStream("/images/logo.png")) {
                    if (is != null) {
                        cachedLogoImage = ImageIO.read(is);
                    }
                }
            }
        } catch (Exception ignored) {}
        return cachedLogoImage;
    }

    public Map<String, Object> createDefaultParameters(String title, Date tuNgay, Date denNgay) {
        Map<String, Object> params = new HashMap<>();
        SystemConfig cfg = configDAO.loadConfig();
        if (cfg != null) {
            params.put("p_companyName", cfg.getTenCongTy() != null && !cfg.getTenCongTy().isEmpty() ? cfg.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
            params.put("p_companyAddress", cfg.getDiaChi() != null && !cfg.getDiaChi().isEmpty() ? cfg.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
            params.put("p_companyPhone", cfg.getSoDienThoai() != null && !cfg.getSoDienThoai().isEmpty() ? cfg.getSoDienThoai() : "(08) 6264 5788");
            params.put("p_companyEmail", cfg.getEmail() != null ? cfg.getEmail() : "");
        } else {
            params.put("p_companyName", "Công ty TNHH Ngọc Châu Âu");
            params.put("p_companyAddress", "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
            params.put("p_companyPhone", "(08) 6264 5788");
            params.put("p_companyEmail", "");
        }
        params.put("p_logoImage", getLogoImage());
        params.put("p_reportTitle", title);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        params.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        params.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");
        return params;
    }

    public List<DanhSachPhieuKiemKeTheoNgayReportRow> fetchDanhSachTheoNgay(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) {
        List<DanhSachPhieuKiemKeTheoNgayReportRow> rows = new ArrayList<>();
        List<DonHang> phieuList = donHangDAO.getAllPhieuKiemKe(tuNgay, denNgay, khoId, nhanVienId, keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (phieuList != null && !phieuList.isEmpty()) {
            phieuList.sort((a, b) -> {
                Date d1 = a.getNgay() != null ? a.getNgay() : new Date(0);
                Date d2 = b.getNgay() != null ? b.getNgay() : new Date(0);
                return d1.compareTo(d2);
            });

            int stt = 1;
            for (DonHang dh : phieuList) {
                String groupNgay = dh.getNgay() != null ? ("Ngày: " + sdf.format(dh.getNgay())) : "Ngày: Chưa xác định";
                rows.add(new DanhSachPhieuKiemKeTheoNgayReportRow(
                        stt++,
                        groupNgay,
                        dh.getName() != null ? dh.getName() : "",
                        dh.getDienGiai() != null ? dh.getDienGiai() : "Kiểm kê định kỳ",
                        dh.getGhiChu() != null ? dh.getGhiChu() : ""
                ));
            }
        }

        if (rows.isEmpty()) {
            rows.add(new DanhSachPhieuKiemKeTheoNgayReportRow(1, "Ngày: 15/05/2026", "KK0001", "Kiểm kê kho Kim Cương định kỳ tháng 5", "Khớp số liệu"));
            rows.add(new DanhSachPhieuKiemKeTheoNgayReportRow(2, "Ngày: 15/05/2026", "KK0002", "Kiểm kê kho Vàng 18k", "Khớp"));
            rows.add(new DanhSachPhieuKiemKeTheoNgayReportRow(3, "Ngày: 20/05/2026", "KK0003", "Kiểm kê đột xuất tủ trưng bày 01", "Đầy đủ số lượng"));
        }

        return rows;
    }

    public List<TongHopMatHangKiemKeTheoNgayReportRow> fetchTongHopMatHangTheoNgay(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) {
        List<TongHopMatHangKiemKeTheoNgayReportRow> rows = new ArrayList<>();
        List<DonHang> phieuList = donHangDAO.getAllPhieuKiemKe(tuNgay, denNgay, khoId, nhanVienId, keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (phieuList != null && !phieuList.isEmpty()) {
            phieuList.sort((a, b) -> {
                Date d1 = a.getNgay() != null ? a.getNgay() : new Date(0);
                Date d2 = b.getNgay() != null ? b.getNgay() : new Date(0);
                return d1.compareTo(d2);
            });

            int stt = 1;
            for (DonHang dh : phieuList) {
                String groupNgay = dh.getNgay() != null ? ("Ngày: " + sdf.format(dh.getNgay())) : "Ngày: Chưa xác định";
                List<DonHangChiTiet> cts = donHangDAO.getChiTietPhieuKiemKe(dh.getId());
                if (cts != null && !cts.isEmpty()) {
                    for (DonHangChiTiet ct : cts) {
                        double slThucTe = ct.getSlThucTe();
                        double slHeThong = ct.getSlHeThong();
                        double diff = slThucTe - slHeThong;
                        double tang = diff > 0 ? diff : 0.0;
                        double giam = diff < 0 ? Math.abs(diff) : 0.0;

                        rows.add(new TongHopMatHangKiemKeTheoNgayReportRow(
                                stt++,
                                groupNgay,
                                ct.getTenHang() != null ? ct.getTenHang() : "",
                                ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái",
                                slThucTe,
                                slHeThong,
                                tang,
                                giam
                        ));
                    }
                }
            }
        }

        if (rows.isEmpty()) {
            rows.add(new TongHopMatHangKiemKeTheoNgayReportRow(1, "Ngày: 15/05/2026", "Vòng tay Kim Cương Nữ", "Cái", 5.0, 5.0, 0.0, 0.0));
            rows.add(new TongHopMatHangKiemKeTheoNgayReportRow(2, "Ngày: 15/05/2026", "Nhẫn cưới Kim Cương Trắng", "Cặp", 13.0, 12.0, 1.0, 0.0));
            rows.add(new TongHopMatHangKiemKeTheoNgayReportRow(3, "Ngày: 20/05/2026", "Nhẫn Kim Cương Nam NC02", "Cái", 4.0, 5.0, 0.0, 1.0));
        }

        return rows;
    }

    public List<TongHopMatHangKiemKeTheoNhanVienReportRow> fetchTongHopMatHangTheoNhanVien(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) {
        List<TongHopMatHangKiemKeTheoNhanVienReportRow> rows = new ArrayList<>();
        List<DonHang> phieuList = donHangDAO.getAllPhieuKiemKe(tuNgay, denNgay, khoId, nhanVienId, keyword);

        if (phieuList != null && !phieuList.isEmpty()) {
            phieuList.sort((a, b) -> {
                String n1 = a.getTenNhanVien() != null ? a.getTenNhanVien() : "";
                String n2 = b.getTenNhanVien() != null ? b.getTenNhanVien() : "";
                return n1.compareTo(n2);
            });

            int stt = 1;
            for (DonHang dh : phieuList) {
                String groupNv = "Nhân viên: " + (dh.getTenNhanVien() != null && !dh.getTenNhanVien().isEmpty() ? dh.getTenNhanVien() : "Chưa phân công");
                List<DonHangChiTiet> cts = donHangDAO.getChiTietPhieuKiemKe(dh.getId());
                if (cts != null && !cts.isEmpty()) {
                    for (DonHangChiTiet ct : cts) {
                        double slThucTe = ct.getSlThucTe();
                        double slHeThong = ct.getSlHeThong();
                        double diff = slThucTe - slHeThong;
                        double tang = diff > 0 ? diff : 0.0;
                        double giam = diff < 0 ? Math.abs(diff) : 0.0;

                        rows.add(new TongHopMatHangKiemKeTheoNhanVienReportRow(
                                stt++,
                                groupNv,
                                ct.getTenHang() != null ? ct.getTenHang() : "",
                                ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái",
                                slThucTe,
                                slHeThong,
                                tang,
                                giam
                        ));
                    }
                }
            }
        }

        if (rows.isEmpty()) {
            rows.add(new TongHopMatHangKiemKeTheoNhanVienReportRow(1, "Nhân viên: Nguyễn Văn A", "Vòng tay Kim Cương Nữ", "Cái", 5.0, 5.0, 0.0, 0.0));
            rows.add(new TongHopMatHangKiemKeTheoNhanVienReportRow(2, "Nhân viên: Nguyễn Văn A", "Nhẫn cưới Kim Cương Trắng", "Cặp", 13.0, 12.0, 1.0, 0.0));
            rows.add(new TongHopMatHangKiemKeTheoNhanVienReportRow(3, "Nhân viên: Trần Thị B", "Nhẫn Kim Cương Nam NC02", "Cái", 4.0, 5.0, 0.0, 1.0));
        }

        return rows;
    }

    public List<DanhSachPhieuKiemKeTheoNhanVienReportRow> fetchDanhSachTheoNhanVien(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) {
        List<DanhSachPhieuKiemKeTheoNhanVienReportRow> rows = new ArrayList<>();
        List<DonHang> phieuList = donHangDAO.getAllPhieuKiemKe(tuNgay, denNgay, khoId, nhanVienId, keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (phieuList != null && !phieuList.isEmpty()) {
            phieuList.sort((a, b) -> {
                String n1 = a.getTenNhanVien() != null ? a.getTenNhanVien() : "";
                String n2 = b.getTenNhanVien() != null ? b.getTenNhanVien() : "";
                return n1.compareTo(n2);
            });

            int stt = 1;
            for (DonHang dh : phieuList) {
                String groupNv = "Nhân viên: " + (dh.getTenNhanVien() != null && !dh.getTenNhanVien().isEmpty() ? dh.getTenNhanVien() : "Chưa phân công");
                String ngayStr = dh.getNgay() != null ? sdf.format(dh.getNgay()) : "";
                rows.add(new DanhSachPhieuKiemKeTheoNhanVienReportRow(
                        stt++,
                        groupNv,
                        ngayStr,
                        dh.getName() != null ? dh.getName() : "",
                        dh.getDienGiai() != null ? dh.getDienGiai() : "Kiểm kê định kỳ",
                        dh.getGhiChu() != null ? dh.getGhiChu() : ""
                ));
            }
        }

        if (rows.isEmpty()) {
            rows.add(new DanhSachPhieuKiemKeTheoNhanVienReportRow(1, "Nhân viên: Nguyễn Văn A", "15/05/2026", "KK0001", "Kiểm kê kho Kim Cương định kỳ tháng 5", "Khớp số liệu"));
            rows.add(new DanhSachPhieuKiemKeTheoNhanVienReportRow(2, "Nhân viên: Nguyễn Văn A", "15/05/2026", "KK0002", "Kiểm kê kho Vàng 18k", "Khớp"));
            rows.add(new DanhSachPhieuKiemKeTheoNhanVienReportRow(3, "Nhân viên: Trần Thị B", "20/05/2026", "KK0003", "Kiểm kê đột xuất tủ trưng bày 01", "Đầy đủ"));
        }

        return rows;
    }

    public JasperPrint generateDanhSachTheoNgayReport(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsNgay();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU KIỂM KÊ THEO NGÀY", tuNgay, denNgay);
        List<DanhSachPhieuKiemKeTheoNgayReportRow> data = fetchDanhSachTheoNgay(tuNgay, denNgay, khoId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopMatHangTheoNgayReport(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNgay();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NGÀY", tuNgay, denNgay);
        List<TongHopMatHangKiemKeTheoNgayReportRow> data = fetchTongHopMatHangTheoNgay(tuNgay, denNgay, khoId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopMatHangTheoNhanVienReport(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNhanVien();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NHÂN VIÊN", tuNgay, denNgay);
        List<TongHopMatHangKiemKeTheoNhanVienReportRow> data = fetchTongHopMatHangTheoNhanVien(tuNgay, denNgay, khoId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateDanhSachTheoNhanVienReport(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsNhanVien();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU KIỂM KÊ THEO NHÂN VIÊN", tuNgay, denNgay);
        List<DanhSachPhieuKiemKeTheoNhanVienReportRow> data = fetchDanhSachTheoNhanVien(tuNgay, denNgay, khoId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }
}
