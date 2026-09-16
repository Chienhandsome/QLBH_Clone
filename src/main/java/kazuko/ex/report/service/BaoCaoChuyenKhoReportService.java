package kazuko.ex.report.service;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.report.model.DanhSachPhieuChuyenKhoReportRow;
import kazuko.ex.report.model.TongHopMatHangChuyenKhoReportRow;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoChuyenKhoReportService {

    private static JasperReport cachedReportThMhNvNhan = null;
    private static JasperReport cachedReportThMhNvChuyen = null;
    private static JasperReport cachedReportThMhNgay = null;
    private static JasperReport cachedReportDsPhieuNvXuat = null;
    private static JasperReport cachedReportDsPhieuNvNhan = null;
    private static JasperReport cachedReportDsPhieuNgay = null;

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoChuyenKho-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportThMhNvNhan == null) {
                try (InputStream in = BaoCaoChuyenKhoReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_chuyen_kho_theo_nhan_vien_nhan.jrxml")) {
                    if (in != null) cachedReportThMhNvNhan = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNvChuyen == null) {
                try (InputStream in = BaoCaoChuyenKhoReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_chuyen_kho_theo_nhan_vien_chuyen.jrxml")) {
                    if (in != null) cachedReportThMhNvChuyen = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNgay == null) {
                try (InputStream in = BaoCaoChuyenKhoReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_chuyen_kho_theo_ngay.jrxml")) {
                    if (in != null) cachedReportThMhNgay = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportDsPhieuNvXuat == null) {
                try (InputStream in = BaoCaoChuyenKhoReportService.class.getResourceAsStream("/reports/danh_sach_phieu_chuyen_kho_theo_nhan_vien_xuat.jrxml")) {
                    if (in != null) cachedReportDsPhieuNvXuat = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportDsPhieuNvNhan == null) {
                try (InputStream in = BaoCaoChuyenKhoReportService.class.getResourceAsStream("/reports/danh_sach_phieu_chuyen_kho_theo_nhan_vien_nhan.jrxml")) {
                    if (in != null) cachedReportDsPhieuNvNhan = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportDsPhieuNgay == null) {
                try (InputStream in = BaoCaoChuyenKhoReportService.class.getResourceAsStream("/reports/danh_sach_phieu_chuyen_kho_theo_ngay.jrxml")) {
                    if (in != null) cachedReportDsPhieuNgay = ReportTemplateCompiler.compile(in);
                }
            }
        } catch (Exception e) {
            System.err.println("BaoCaoChuyenKhoReportService preload error: " + e.getMessage());
        }
    }

    public synchronized JasperReport getCompiledReportThMhNvNhan() throws JRException {
        if (cachedReportThMhNvNhan != null) return cachedReportThMhNvNhan;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_chuyen_kho_theo_nhan_vien_nhan.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_chuyen_kho_theo_nhan_vien_nhan.jrxml không tồn tại!");
            cachedReportThMhNvNhan = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNvNhan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNvChuyen() throws JRException {
        if (cachedReportThMhNvChuyen != null) return cachedReportThMhNvChuyen;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_chuyen_kho_theo_nhan_vien_chuyen.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_chuyen_kho_theo_nhan_vien_chuyen.jrxml không tồn tại!");
            cachedReportThMhNvChuyen = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNvChuyen;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNgay() throws JRException {
        if (cachedReportThMhNgay != null) return cachedReportThMhNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_chuyen_kho_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_chuyen_kho_theo_ngay.jrxml không tồn tại!");
            cachedReportThMhNgay = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportDsPhieuNvXuat() throws JRException {
        if (cachedReportDsPhieuNvXuat != null) return cachedReportDsPhieuNvXuat;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_chuyen_kho_theo_nhan_vien_xuat.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_chuyen_kho_theo_nhan_vien_xuat.jrxml không tồn tại!");
            cachedReportDsPhieuNvXuat = ReportTemplateCompiler.compile(in);
            return cachedReportDsPhieuNvXuat;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportDsPhieuNvNhan() throws JRException {
        if (cachedReportDsPhieuNvNhan != null) return cachedReportDsPhieuNvNhan;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_chuyen_kho_theo_nhan_vien_nhan.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_chuyen_kho_theo_nhan_vien_nhan.jrxml không tồn tại!");
            cachedReportDsPhieuNvNhan = ReportTemplateCompiler.compile(in);
            return cachedReportDsPhieuNvNhan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportDsPhieuNgay() throws JRException {
        if (cachedReportDsPhieuNgay != null) return cachedReportDsPhieuNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_chuyen_kho_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_chuyen_kho_theo_ngay.jrxml không tồn tại!");
            cachedReportDsPhieuNgay = ReportTemplateCompiler.compile(in);
            return cachedReportDsPhieuNgay;
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
            try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
                if (in != null) {
                    cachedLogoImage = ImageIO.read(in);
                    return cachedLogoImage;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public Map<String, Object> createDefaultParameters(String reportTitle, Date tuNgay, Date denNgay) {
        Map<String, Object> params = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

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
            params.put("p_companyEmail", "info@ngocchauau.vn");
        }

        params.put("p_logoImage", getLogoImage());
        params.put("p_reportTitle", reportTitle);
        params.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        params.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return params;
    }

    // ==========================================
    // 1. TỔNG HỢP MẶT HÀNG
    // ==========================================
    public List<TongHopMatHangChuyenKhoReportRow> fetchTongHopMatHangTheoNhanVienNhan(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) {
        List<TongHopMatHangChuyenKhoReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getAllPhieuChuyenKho(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHangChiTiet>> groupMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                String nvNhap = dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "";
                String groupKey = "Nhân viên nhập: " + nvNhap;

                List<DonHangChiTiet> cts = donHangDAO.getChiTietPhieuNhap(dh.getId());
                if (cts != null) {
                    for (DonHangChiTiet ct : cts) {
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String kw = keyword.trim().toLowerCase();
                            String th = ct.getTenHang() != null ? ct.getTenHang().toLowerCase() : "";
                            String mh = ct.getMaHang() != null ? ct.getMaHang().toLowerCase() : "";
                            if (!th.contains(kw) && !mh.contains(kw)) continue;
                        }
                        groupMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(ct);
                    }
                }
            }

            for (Map.Entry<String, List<DonHangChiTiet>> entry : groupMap.entrySet()) {
                int stt = 1;
                for (DonHangChiTiet ct : entry.getValue()) {
                    rows.add(new TongHopMatHangChuyenKhoReportRow(
                            stt++,
                            entry.getKey(),
                            ct.getMaHang() != null ? ct.getMaHang() : "",
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Chiếc",
                            ct.getSoLuong(),
                            ct.getDonGia(),
                            ct.getThanhTien()
                    ));
                }
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<TongHopMatHangChuyenKhoReportRow> fetchTongHopMatHangTheoNhanVienChuyen(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) {
        List<TongHopMatHangChuyenKhoReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getAllPhieuChuyenKho(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHangChiTiet>> groupMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                String nvXuat = dh.getTenNhanVienXuat() != null ? dh.getTenNhanVienXuat() : "";
                String groupKey = "Nhân viên xuất: " + nvXuat;

                List<DonHangChiTiet> cts = donHangDAO.getChiTietPhieuNhap(dh.getId());
                if (cts != null) {
                    for (DonHangChiTiet ct : cts) {
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String kw = keyword.trim().toLowerCase();
                            String th = ct.getTenHang() != null ? ct.getTenHang().toLowerCase() : "";
                            String mh = ct.getMaHang() != null ? ct.getMaHang().toLowerCase() : "";
                            if (!th.contains(kw) && !mh.contains(kw)) continue;
                        }
                        groupMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(ct);
                    }
                }
            }

            for (Map.Entry<String, List<DonHangChiTiet>> entry : groupMap.entrySet()) {
                int stt = 1;
                for (DonHangChiTiet ct : entry.getValue()) {
                    rows.add(new TongHopMatHangChuyenKhoReportRow(
                            stt++,
                            entry.getKey(),
                            ct.getMaHang() != null ? ct.getMaHang() : "",
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Chiếc",
                            ct.getSoLuong(),
                            ct.getDonGia(),
                            ct.getThanhTien()
                    ));
                }
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<TongHopMatHangChuyenKhoReportRow> fetchTongHopMatHangTheoNgay(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) {
        List<TongHopMatHangChuyenKhoReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getAllPhieuChuyenKho(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHangChiTiet>> groupMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                String ngayStr = dh.getNgay() != null ? ("Ngày: " + sdf.format(dh.getNgay())) : "Ngày: Chưa xác định";

                List<DonHangChiTiet> cts = donHangDAO.getChiTietPhieuNhap(dh.getId());
                if (cts != null) {
                    for (DonHangChiTiet ct : cts) {
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String kw = keyword.trim().toLowerCase();
                            String th = ct.getTenHang() != null ? ct.getTenHang().toLowerCase() : "";
                            String mh = ct.getMaHang() != null ? ct.getMaHang().toLowerCase() : "";
                            if (!th.contains(kw) && !mh.contains(kw)) continue;
                        }
                        groupMap.computeIfAbsent(ngayStr, k -> new ArrayList<>()).add(ct);
                    }
                }
            }

            for (Map.Entry<String, List<DonHangChiTiet>> entry : groupMap.entrySet()) {
                int stt = 1;
                for (DonHangChiTiet ct : entry.getValue()) {
                    rows.add(new TongHopMatHangChuyenKhoReportRow(
                            stt++,
                            entry.getKey(),
                            ct.getMaHang() != null ? ct.getMaHang() : "",
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Chiếc",
                            ct.getSoLuong(),
                            ct.getDonGia(),
                            ct.getThanhTien()
                    ));
                }
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    // ==========================================
    // 2. DANH SÁCH PHIẾU CHUYỂN KHO
    // ==========================================
    public List<DanhSachPhieuChuyenKhoReportRow> fetchDanhSachPhieuTheoNhanVienXuat(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) {
        List<DanhSachPhieuChuyenKhoReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getAllPhieuChuyenKho(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHang>> groupMap = new TreeMap<>();
            for (DonHang dh : vouchers) {
                String nvXuat = dh.getTenNhanVienXuat() != null ? dh.getTenNhanVienXuat() : "";
                String groupKey = "Nhân viên xuất: " + nvXuat;
                groupMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(dh);
            }

            for (Map.Entry<String, List<DonHang>> entry : groupMap.entrySet()) {
                int stt = 1;
                for (DonHang dh : entry.getValue()) {
                    rows.add(new DanhSachPhieuChuyenKhoReportRow(
                            stt++,
                            entry.getKey(),
                            dh.getName() != null ? dh.getName() : "",
                            dh.getNgay() != null ? sdf.format(dh.getNgay()) : "",
                            dh.getTenKhoXuat() != null ? dh.getTenKhoXuat() : "",
                            dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : "",
                            dh.getTenNhanVienXuat() != null ? dh.getTenNhanVienXuat() : "",
                            dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "",
                            dh.getDienGiai() != null && !dh.getDienGiai().isEmpty() ? dh.getDienGiai() : "Chuyển kho nội bộ",
                            dh.getTongCong(),
                            dh.getGhiChu() != null ? dh.getGhiChu() : ""
                    ));
                }
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<DanhSachPhieuChuyenKhoReportRow> fetchDanhSachPhieuTheoNhanVienNhan(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) {
        List<DanhSachPhieuChuyenKhoReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getAllPhieuChuyenKho(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHang>> groupMap = new TreeMap<>();
            for (DonHang dh : vouchers) {
                String nvNhap = dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "";
                String groupKey = "Nhân viên nhập: " + nvNhap;
                groupMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(dh);
            }

            for (Map.Entry<String, List<DonHang>> entry : groupMap.entrySet()) {
                int stt = 1;
                for (DonHang dh : entry.getValue()) {
                    rows.add(new DanhSachPhieuChuyenKhoReportRow(
                            stt++,
                            entry.getKey(),
                            dh.getName() != null ? dh.getName() : "",
                            dh.getNgay() != null ? sdf.format(dh.getNgay()) : "",
                            dh.getTenKhoXuat() != null ? dh.getTenKhoXuat() : "",
                            dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : "",
                            dh.getTenNhanVienXuat() != null ? dh.getTenNhanVienXuat() : "",
                            dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "",
                            dh.getDienGiai() != null && !dh.getDienGiai().isEmpty() ? dh.getDienGiai() : "Chuyển kho nội bộ",
                            dh.getTongCong(),
                            dh.getGhiChu() != null ? dh.getGhiChu() : ""
                    ));
                }
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<DanhSachPhieuChuyenKhoReportRow> fetchDanhSachPhieuTheoNgay(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) {
        List<DanhSachPhieuChuyenKhoReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getAllPhieuChuyenKho(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            int stt = 1;
            for (DonHang dh : vouchers) {
                rows.add(new DanhSachPhieuChuyenKhoReportRow(
                        stt++,
                        "",
                        dh.getName() != null ? dh.getName() : "",
                        dh.getNgay() != null ? sdf.format(dh.getNgay()) : "",
                        dh.getTenKhoXuat() != null ? dh.getTenKhoXuat() : "",
                        dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : "",
                        dh.getTenNhanVienXuat() != null ? dh.getTenNhanVienXuat() : "",
                        dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "",
                        dh.getDienGiai() != null && !dh.getDienGiai().isEmpty() ? dh.getDienGiai() : "Chuyển kho nội bộ",
                        dh.getTongCong(),
                        dh.getGhiChu() != null ? dh.getGhiChu() : ""
                ));
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    // ==========================================
    // 3. FILL / GENERATE METHODS
    // ==========================================
    public JasperPrint generateTongHopMatHangTheoNhanVienNhanReport(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNvNhan();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN NHẬN", tuNgay, denNgay);
        List<TongHopMatHangChuyenKhoReportRow> data = fetchTongHopMatHangTheoNhanVienNhan(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopMatHangTheoNhanVienChuyenReport(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNvChuyen();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN CHUYỂN", tuNgay, denNgay);
        List<TongHopMatHangChuyenKhoReportRow> data = fetchTongHopMatHangTheoNhanVienChuyen(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopMatHangTheoNgayReport(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNgay();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NGÀY", tuNgay, denNgay);
        List<TongHopMatHangChuyenKhoReportRow> data = fetchTongHopMatHangTheoNgay(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateDanhSachPhieuTheoNhanVienXuatReport(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsPhieuNvXuat();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN XUẤT", tuNgay, denNgay);
        List<DanhSachPhieuChuyenKhoReportRow> data = fetchDanhSachPhieuTheoNhanVienXuat(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateDanhSachPhieuTheoNhanVienNhanReport(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsPhieuNvNhan();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN NHẬP", tuNgay, denNgay);
        List<DanhSachPhieuChuyenKhoReportRow> data = fetchDanhSachPhieuTheoNhanVienNhan(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateDanhSachPhieuTheoNgayReport(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsPhieuNgay();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU CHUYỂN KHO THEO NGÀY", tuNgay, denNgay);
        List<DanhSachPhieuChuyenKhoReportRow> data = fetchDanhSachPhieuTheoNgay(tuNgay, denNgay, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }
}
