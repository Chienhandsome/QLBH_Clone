package kazuko.ex.report.service;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoNhapKhoReportService {

    private static JasperReport cachedReportDsNgay = null;
    private static JasperReport cachedReportDsNcc = null;
    private static JasperReport cachedReportDsNhanVien = null;
    private static JasperReport cachedReportThMhNcc = null;
    private static JasperReport cachedReportThMhNhanVien = null;
    private static JasperReport cachedReportThNhapNgay = null;
    private static JasperReport cachedReportThNhapNcc = null;
    private static JasperReport cachedReportThNhapNhanVien = null;
    private static JasperReport cachedReportThMhNhapNgay = null;

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoNhapKho-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportDsNgay == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/danh_sach_phieu_nhap_theo_ngay.jrxml")) {
                    if (in != null) cachedReportDsNgay = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportDsNcc == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/danh_sach_phieu_nhap_theo_nha_cung_cap.jrxml")) {
                    if (in != null) cachedReportDsNcc = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportDsNhanVien == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/danh_sach_phieu_nhap_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportDsNhanVien = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNcc == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_nhap_theo_nha_cung_cap.jrxml")) {
                    if (in != null) cachedReportThMhNcc = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNhanVien == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_nhap_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportThMhNhanVien = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThNhapNgay == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/tong_hop_nhap_theo_ngay.jrxml")) {
                    if (in != null) cachedReportThNhapNgay = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThNhapNcc == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/tong_hop_nhap_theo_nha_cung_cap.jrxml")) {
                    if (in != null) cachedReportThNhapNcc = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThNhapNhanVien == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/tong_hop_nhap_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportThNhapNhanVien = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportThMhNhapNgay == null) {
                try (InputStream in = BaoCaoNhapKhoReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_nhap_theo_ngay.jrxml")) {
                    if (in != null) cachedReportThMhNhapNgay = ReportTemplateCompiler.compile(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportDsNgay() throws JRException {
        if (cachedReportDsNgay != null) return cachedReportDsNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_nhap_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_nhap_theo_ngay.jrxml không tồn tại!");
            cachedReportDsNgay = ReportTemplateCompiler.compile(in);
            return cachedReportDsNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportDsNcc() throws JRException {
        if (cachedReportDsNcc != null) return cachedReportDsNcc;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_nhap_theo_nha_cung_cap.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_nhap_theo_nha_cung_cap.jrxml không tồn tại!");
            cachedReportDsNcc = ReportTemplateCompiler.compile(in);
            return cachedReportDsNcc;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportDsNhanVien() throws JRException {
        if (cachedReportDsNhanVien != null) return cachedReportDsNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_nhap_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_phieu_nhap_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportDsNhanVien = ReportTemplateCompiler.compile(in);
            return cachedReportDsNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNcc() throws JRException {
        if (cachedReportThMhNcc != null) return cachedReportThMhNcc;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_nhap_theo_nha_cung_cap.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_nhap_theo_nha_cung_cap.jrxml không tồn tại!");
            cachedReportThMhNcc = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNcc;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNhanVien() throws JRException {
        if (cachedReportThMhNhanVien != null) return cachedReportThMhNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_nhap_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_nhap_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportThMhNhanVien = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThNhapNgay() throws JRException {
        if (cachedReportThNhapNgay != null) return cachedReportThNhapNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_nhap_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_nhap_theo_ngay.jrxml không tồn tại!");
            cachedReportThNhapNgay = ReportTemplateCompiler.compile(in);
            return cachedReportThNhapNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThNhapNcc() throws JRException {
        if (cachedReportThNhapNcc != null) return cachedReportThNhapNcc;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_nhap_theo_nha_cung_cap.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_nhap_theo_nha_cung_cap.jrxml không tồn tại!");
            cachedReportThNhapNcc = ReportTemplateCompiler.compile(in);
            return cachedReportThNhapNcc;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThNhapNhanVien() throws JRException {
        if (cachedReportThNhapNhanVien != null) return cachedReportThNhapNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_nhap_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_nhap_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportThNhapNhanVien = ReportTemplateCompiler.compile(in);
            return cachedReportThNhapNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportThMhNhapNgay() throws JRException {
        if (cachedReportThMhNhapNgay != null) return cachedReportThMhNhapNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_nhap_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_nhap_theo_ngay.jrxml không tồn tại!");
            cachedReportThMhNhapNgay = ReportTemplateCompiler.compile(in);
            return cachedReportThMhNhapNgay;
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

    public List<DanhSachPhieuNhapTheoNgayReportRow> fetchDanhSachTheoNgay(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<DanhSachPhieuNhapTheoNgayReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            vouchers.sort((a, b) -> {
                Date d1 = a.getNgay() != null ? a.getNgay() : new Date(0);
                Date d2 = b.getNgay() != null ? b.getNgay() : new Date(0);
                return d1.compareTo(d2);
            });

            int stt = 1;
            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                    String gc = dh.getGhiChu() != null ? dh.getGhiChu().toLowerCase() : "";
                    String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                    if (!name.contains(kw) && !gc.contains(kw) && !dg.contains(kw)) continue;
                }

                String groupNgay = dh.getNgay() != null ? ("Ngày: " + sdf.format(dh.getNgay())) : "Ngày: Chưa xác định";
                String tenKho = dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : "";
                String tenNcc = dh.getTenNhaCungCap() != null ? dh.getTenNhaCungCap() : "";
                String dienGiai = dh.getDienGiai() != null && !dh.getDienGiai().isEmpty() ? dh.getDienGiai() : "Nhập mua hàng";

                rows.add(new DanhSachPhieuNhapTheoNgayReportRow(
                        stt++,
                        groupNgay,
                        dh.getName() != null ? dh.getName() : "",
                        tenNcc,
                        tenKho,
                        dienGiai,
                        dh.getTienHang(),
                        dh.getTienGiamGia(),
                        dh.getPhiVanChuyen(),
                        dh.getTongCong()
                ));
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<DanhSachPhieuNhapTheoNhaCungCapReportRow> fetchDanhSachTheoNhaCungCap(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<DanhSachPhieuNhapTheoNhaCungCapReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            vouchers.sort((a, b) -> {
                String n1 = a.getTenNhaCungCap() != null ? a.getTenNhaCungCap() : "";
                String n2 = b.getTenNhaCungCap() != null ? b.getTenNhaCungCap() : "";
                int c = n1.compareToIgnoreCase(n2);
                if (c != 0) return c;
                Date d1 = a.getNgay() != null ? a.getNgay() : new Date(0);
                Date d2 = b.getNgay() != null ? b.getNgay() : new Date(0);
                return d1.compareTo(d2);
            });

            int stt = 1;
            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                    String gc = dh.getGhiChu() != null ? dh.getGhiChu().toLowerCase() : "";
                    String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                    if (!name.contains(kw) && !gc.contains(kw) && !dg.contains(kw)) continue;
                }

                String tenNcc = dh.getTenNhaCungCap() != null ? dh.getTenNhaCungCap() : "";
                String groupNcc = "Nhà cung cấp: " + tenNcc;
                String ngay = dh.getNgay() != null ? sdf.format(dh.getNgay()) : "";
                String tenKho = dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : "";
                String tenNv = dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "";
                String dienGiai = dh.getDienGiai() != null && !dh.getDienGiai().isEmpty() ? dh.getDienGiai() : "Nhập mua hàng";

                rows.add(new DanhSachPhieuNhapTheoNhaCungCapReportRow(
                        stt++,
                        groupNcc,
                        ngay,
                        dh.getName() != null ? dh.getName() : "",
                        tenKho,
                        tenNv,
                        dienGiai,
                        dh.getTienHang(),
                        dh.getTienGiamGia(),
                        dh.getPhiVanChuyen(),
                        dh.getTongCong()
                ));
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<DanhSachPhieuNhapTheoNhanVienReportRow> fetchDanhSachTheoNhanVien(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<DanhSachPhieuNhapTheoNhanVienReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            vouchers.sort((a, b) -> {
                String n1 = a.getTenNhanVienNhap() != null ? a.getTenNhanVienNhap() : "";
                String n2 = b.getTenNhanVienNhap() != null ? b.getTenNhanVienNhap() : "";
                int c = n1.compareToIgnoreCase(n2);
                if (c != 0) return c;
                Date d1 = a.getNgay() != null ? a.getNgay() : new Date(0);
                Date d2 = b.getNgay() != null ? b.getNgay() : new Date(0);
                return d1.compareTo(d2);
            });

            int stt = 1;
            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                    String gc = dh.getGhiChu() != null ? dh.getGhiChu().toLowerCase() : "";
                    String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                    if (!name.contains(kw) && !gc.contains(kw) && !dg.contains(kw)) continue;
                }

                String tenNv = dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "";
                String groupNv = "Nhân viên nhập: " + tenNv;
                String ngay = dh.getNgay() != null ? sdf.format(dh.getNgay()) : "";
                String dienGiai = dh.getDienGiai() != null && !dh.getDienGiai().isEmpty() ? dh.getDienGiai() : "Nhập mua hàng";

                rows.add(new DanhSachPhieuNhapTheoNhanVienReportRow(
                        stt++,
                        groupNv,
                        ngay,
                        dh.getName() != null ? dh.getName() : "",
                        dienGiai,
                        dh.getTienHang(),
                        dh.getTienGiamGia(),
                        dh.getPhiVanChuyen(),
                        dh.getTongCong()
                ));
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<TongHopMatHangNhapTheoNhaCungCapReportRow> fetchTongHopMatHangTheoNhaCungCap(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<TongHopMatHangNhapTheoNhaCungCapReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHangChiTiet>> nccMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }

                String tenNcc = dh.getTenNhaCungCap() != null ? dh.getTenNhaCungCap() : "";
                List<DonHangChiTiet> cts = donHangDAO.getChiTietPhieuNhap(dh.getId());
                if (cts != null) {
                    for (DonHangChiTiet ct : cts) {
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String kw = keyword.trim().toLowerCase();
                            String th = ct.getTenHang() != null ? ct.getTenHang().toLowerCase() : "";
                            String mh = ct.getMaHang() != null ? ct.getMaHang().toLowerCase() : "";
                            if (!th.contains(kw) && !mh.contains(kw)) continue;
                        }
                        nccMap.computeIfAbsent(tenNcc, k -> new ArrayList<>()).add(ct);
                    }
                }
            }

            for (Map.Entry<String, List<DonHangChiTiet>> entry : nccMap.entrySet()) {
                String groupHeader = "Nhà cung cấp: " + entry.getKey();
                int stt = 1;
                for (DonHangChiTiet ct : entry.getValue()) {
                    rows.add(new TongHopMatHangNhapTheoNhaCungCapReportRow(
                            stt++,
                            groupHeader,
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

    public List<TongHopMatHangNhapTheoNhanVienReportRow> fetchTongHopMatHangTheoNhanVien(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<TongHopMatHangNhapTheoNhanVienReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHangChiTiet>> nvMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }

                String tenNv = dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "";
                List<DonHangChiTiet> cts = donHangDAO.getChiTietPhieuNhap(dh.getId());
                if (cts != null) {
                    for (DonHangChiTiet ct : cts) {
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String kw = keyword.trim().toLowerCase();
                            String th = ct.getTenHang() != null ? ct.getTenHang().toLowerCase() : "";
                            String mh = ct.getMaHang() != null ? ct.getMaHang().toLowerCase() : "";
                            if (!th.contains(kw) && !mh.contains(kw)) continue;
                        }
                        nvMap.computeIfAbsent(tenNv, k -> new ArrayList<>()).add(ct);
                    }
                }
            }

            for (Map.Entry<String, List<DonHangChiTiet>> entry : nvMap.entrySet()) {
                String groupHeader = "Nhân viên nhập: " + entry.getKey();
                int stt = 1;
                for (DonHangChiTiet ct : entry.getValue()) {
                    rows.add(new TongHopMatHangNhapTheoNhanVienReportRow(
                            stt++,
                            groupHeader,
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

    public List<TongHopNhapTheoNgayReportRow> fetchTongHopNhapTheoNgay(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<TongHopNhapTheoNgayReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, double[]> dateMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                    String gc = dh.getGhiChu() != null ? dh.getGhiChu().toLowerCase() : "";
                    String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                    if (!name.contains(kw) && !gc.contains(kw) && !dg.contains(kw)) continue;
                }

                String ngayStr = dh.getNgay() != null ? sdf.format(dh.getNgay()) : "Chưa xác định";
                double[] sums = dateMap.computeIfAbsent(ngayStr, k -> new double[4]);
                sums[0] += dh.getTienHang();
                sums[1] += dh.getTienGiamGia();
                sums[2] += dh.getPhiVanChuyen();
                sums[3] += dh.getTongCong();
            }

            int stt = 1;
            for (Map.Entry<String, double[]> entry : dateMap.entrySet()) {
                double[] s = entry.getValue();
                rows.add(new TongHopNhapTheoNgayReportRow(
                        stt++,
                        entry.getKey(),
                        s[0],
                        s[1],
                        s[2],
                        s[3]
                ));
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<TongHopNhapTheoNhaCungCapReportRow> fetchTongHopNhapTheoNhaCungCap(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<TongHopNhapTheoNhaCungCapReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, double[]> nccMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                    String gc = dh.getGhiChu() != null ? dh.getGhiChu().toLowerCase() : "";
                    String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                    if (!name.contains(kw) && !gc.contains(kw) && !dg.contains(kw)) continue;
                }

                String tenNcc = dh.getTenNhaCungCap() != null ? dh.getTenNhaCungCap() : "";
                double[] sums = nccMap.computeIfAbsent(tenNcc, k -> new double[4]);
                sums[0] += dh.getTienHang();
                sums[1] += dh.getTienGiamGia();
                sums[2] += dh.getPhiVanChuyen();
                sums[3] += dh.getTongCong();
            }

            int stt = 1;
            for (Map.Entry<String, double[]> entry : nccMap.entrySet()) {
                double[] s = entry.getValue();
                rows.add(new TongHopNhapTheoNhaCungCapReportRow(
                        stt++,
                        entry.getKey(),
                        s[0],
                        s[1],
                        s[2],
                        s[3]
                ));
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<TongHopNhapTheoNhanVienReportRow> fetchTongHopNhapTheoNhanVien(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<TongHopNhapTheoNhanVienReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, double[]> nvMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                    String gc = dh.getGhiChu() != null ? dh.getGhiChu().toLowerCase() : "";
                    String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                    if (!name.contains(kw) && !gc.contains(kw) && !dg.contains(kw)) continue;
                }

                String tenNv = dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "";
                double[] sums = nvMap.computeIfAbsent(tenNv, k -> new double[4]);
                sums[0] += dh.getTienHang();
                sums[1] += dh.getTienGiamGia();
                sums[2] += dh.getPhiVanChuyen();
                sums[3] += dh.getTongCong();
            }

            int stt = 1;
            for (Map.Entry<String, double[]> entry : nvMap.entrySet()) {
                double[] s = entry.getValue();
                rows.add(new TongHopNhapTheoNhanVienReportRow(
                        stt++,
                        entry.getKey(),
                        s[0],
                        s[1],
                        s[2],
                        s[3]
                ));
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    public List<TongHopMatHangNhapTheoNgayReportRow> fetchTongHopMatHangNhapTheoNgay(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) {
        List<TongHopMatHangNhapTheoNgayReportRow> rows = new ArrayList<>();
        List<DonHang> vouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (vouchers != null && !vouchers.isEmpty()) {
            Map<String, List<DonHangChiTiet>> dateMap = new TreeMap<>();

            for (DonHang dh : vouchers) {
                if (khoId != null && !khoId.trim().isEmpty() && !khoId.equalsIgnoreCase(dh.getKhoNhapId())) {
                    continue;
                }
                if (nccId != null && !nccId.trim().isEmpty() && !nccId.equalsIgnoreCase(dh.getNhaCungCapId()) && !nccId.equalsIgnoreCase(dh.getKhachHangId())) {
                    continue;
                }
                if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equalsIgnoreCase(dh.getNhanVienNhapId())) {
                    continue;
                }

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
                        dateMap.computeIfAbsent(ngayStr, k -> new ArrayList<>()).add(ct);
                    }
                }
            }

            for (Map.Entry<String, List<DonHangChiTiet>> entry : dateMap.entrySet()) {
                int stt = 1;
                for (DonHangChiTiet ct : entry.getValue()) {
                    rows.add(new TongHopMatHangNhapTheoNgayReportRow(
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

    public JasperPrint generateDanhSachTheoNgayReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsNgay();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU NHẬP KHO THEO NGÀY", tuNgay, denNgay);
        List<DanhSachPhieuNhapTheoNgayReportRow> data = fetchDanhSachTheoNgay(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateDanhSachTheoNhaCungCapReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsNcc();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU NHẬP KHO THEO NHÀ CUNG CẤP", tuNgay, denNgay);
        List<DanhSachPhieuNhapTheoNhaCungCapReportRow> data = fetchDanhSachTheoNhaCungCap(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateDanhSachTheoNhanVienReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportDsNhanVien();
        Map<String, Object> params = createDefaultParameters("DANH SÁCH PHIẾU NHẬP KHO THEO NHÂN VIÊN NHẬP", tuNgay, denNgay);
        List<DanhSachPhieuNhapTheoNhanVienReportRow> data = fetchDanhSachTheoNhanVien(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopMatHangTheoNhaCungCapReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNcc();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÀ CUNG CẤP", tuNgay, denNgay);
        List<TongHopMatHangNhapTheoNhaCungCapReportRow> data = fetchTongHopMatHangTheoNhaCungCap(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopMatHangTheoNhanVienReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNhanVien();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÂN VIÊN NHẬP", tuNgay, denNgay);
        List<TongHopMatHangNhapTheoNhanVienReportRow> data = fetchTongHopMatHangTheoNhanVien(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopNhapTheoNgayReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThNhapNgay();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP NHẬP THEO NGÀY", tuNgay, denNgay);
        List<TongHopNhapTheoNgayReportRow> data = fetchTongHopNhapTheoNgay(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopNhapTheoNhaCungCapReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThNhapNcc();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP NHẬP THEO NHÀ CUNG CẤP", tuNgay, denNgay);
        List<TongHopNhapTheoNhaCungCapReportRow> data = fetchTongHopNhapTheoNhaCungCap(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopNhapTheoNhanVienReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThNhapNhanVien();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP NHẬP THEO NHÂN VIÊN NHẬP", tuNgay, denNgay);
        List<TongHopNhapTheoNhanVienReportRow> data = fetchTongHopNhapTheoNhanVien(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateTongHopMatHangNhapTheoNgayReport(Date tuNgay, Date denNgay, String khoId, String nccId, String nhanVienId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportThMhNhapNgay();
        Map<String, Object> params = createDefaultParameters("TỔNG HỢP MẶT HÀNG NHẬP KHO THEO NGÀY", tuNgay, denNgay);
        List<TongHopMatHangNhapTheoNgayReportRow> data = fetchTongHopMatHangNhapTheoNgay(tuNgay, denNgay, khoId, nccId, nhanVienId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }
}
