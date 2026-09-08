package kazuko.ex.report.service;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SUser;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoBanHangReportService {

    private static JasperReport cachedReportTheoGio = null;
    private static JasperReport cachedReportTheoKichThuoc = null;
    private static JasperReport cachedReportTongHopGiaTri = null;
    private static JasperReport cachedReportTongHopSoLuong = null;
    private static JasperReport cachedReportTheoNgay = null;
    private static JasperReport cachedReportChiTietTheoNgay = null;
    private static JasperReport cachedReportTheoKhachHang = null;
    private static JasperReport cachedReportTheoNhanVien = null;
    private static JasperReport cachedReportTongHopMatHangTheoKhachHang = null;
    private static JasperReport cachedReportTongHopMatHangTheoNgay = null;
    private static JasperReport cachedReportTongHopMatHangTheoNhanVien = null;
    private static JasperReport cachedReportTongHopBanHangTheoNgay = null;
    private static JasperReport cachedReportTongHopBanTheoKhachHang = null;
    private static JasperReport cachedReportTongHopBanTheoNhanVienBan = null;
    private static JasperReport cachedReportTongHopBanTheoThuNgan = null;
    private static JasperReport cachedReportChiTietTheoThuNgan = null;
    private static JasperReport cachedReportBanHangTheoThuNgan = null;
    private static JasperReport cachedReportTongHopMatHangTheoThuNgan = null;
    private static JasperReport cachedReportTienVanChuyenTheoNhanVien = null;
    private static JasperReport cachedReportChiTietHangBanTheoDoiTac = null;
    private static JasperReport cachedReportTongHopHoaHongTheoNVKD = null;
    private static JasperReport cachedReportChiTietBanHangTheoHoaDon = null;
    private static JasperReport cachedReportChiTietHangKhuyenMai = null;

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final UserDAO userDAO = new UserDAO();

    static {
        // Preload and compile in background
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoBanHang-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportTheoGio == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_ban_hang_theo_gio.jrxml")) {
                    if (in != null) cachedReportTheoGio = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTheoKichThuoc == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_ban_hang_theo_kich_thuoc.jrxml")) {
                    if (in != null) cachedReportTheoKichThuoc = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopGiaTri == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_tong_hop_gia_tri_ban_theo_thang.jrxml")) {
                    if (in != null) cachedReportTongHopGiaTri = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopSoLuong == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_tong_hop_so_luong_ban_theo_thang.jrxml")) {
                    if (in != null) cachedReportTongHopSoLuong = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTheoNgay == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_ban_hang_theo_ngay.jrxml")) {
                    if (in != null) cachedReportTheoNgay = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportChiTietTheoNgay == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_chi_tiet_ban_hang_theo_ngay.jrxml")) {
                    if (in != null) cachedReportChiTietTheoNgay = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTheoKhachHang == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_ban_hang_theo_khach_hang.jrxml")) {
                    if (in != null) cachedReportTheoKhachHang = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTheoNhanVien == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_ban_hang_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportTheoNhanVien = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopMatHangTheoKhachHang == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_khach_hang.jrxml")) {
                    if (in != null) cachedReportTongHopMatHangTheoKhachHang = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopMatHangTheoNgay == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_ngay.jrxml")) {
                    if (in != null) cachedReportTongHopMatHangTheoNgay = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopMatHangTheoNhanVien == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportTongHopMatHangTheoNhanVien = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopBanHangTheoNgay == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_ban_hang_theo_ngay.jrxml")) {
                    if (in != null) cachedReportTongHopBanHangTheoNgay = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopBanTheoKhachHang == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_ban_theo_khach_hang.jrxml")) {
                    if (in != null) cachedReportTongHopBanTheoKhachHang = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopBanTheoNhanVienBan == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_ban_theo_nhan_vien_ban.jrxml")) {
                    if (in != null) cachedReportTongHopBanTheoNhanVienBan = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopBanTheoThuNgan == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_ban_theo_thu_ngan.jrxml")) {
                    if (in != null) cachedReportTongHopBanTheoThuNgan = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportChiTietTheoThuNgan == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_chi_tiet_ban_hang_theo_thu_ngan.jrxml")) {
                    if (in != null) cachedReportChiTietTheoThuNgan = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportBanHangTheoThuNgan == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_ban_hang_theo_thu_ngan.jrxml")) {
                    if (in != null) cachedReportBanHangTheoThuNgan = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopMatHangTheoThuNgan == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_thu_ngan.jrxml")) {
                    if (in != null) cachedReportTongHopMatHangTheoThuNgan = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTienVanChuyenTheoNhanVien == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_tien_van_chuyen_theo_nhan_vien.jrxml")) {
                    if (in != null) cachedReportTienVanChuyenTheoNhanVien = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportChiTietHangBanTheoDoiTac == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/chi_tiet_hang_ban_theo_doi_tac.jrxml")) {
                    if (in != null) cachedReportChiTietHangBanTheoDoiTac = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopHoaHongTheoNVKD == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/tong_hop_hoa_hong_theo_nvkd.jrxml")) {
                    if (in != null) cachedReportTongHopHoaHongTheoNVKD = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportChiTietBanHangTheoHoaDon == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/chi_tiet_ban_hang_theo_hoa_don.jrxml")) {
                    if (in != null) cachedReportChiTietBanHangTheoHoaDon = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportChiTietHangKhuyenMai == null) {
                try (InputStream in = BaoCaoBanHangReportService.class.getResourceAsStream("/reports/bao_cao_chi_tiet_hang_khuyen_mai.jrxml")) {
                    if (in != null) cachedReportChiTietHangKhuyenMai = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportTheoGio() throws JRException {
        if (cachedReportTheoGio != null) return cachedReportTheoGio;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ban_hang_theo_gio.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ban_hang_theo_gio.jrxml không tồn tại!");
            cachedReportTheoGio = JasperCompileManager.compileReport(in);
            return cachedReportTheoGio;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTheoKichThuoc() throws JRException {
        if (cachedReportTheoKichThuoc != null) return cachedReportTheoKichThuoc;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ban_hang_theo_kich_thuoc.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ban_hang_theo_kich_thuoc.jrxml không tồn tại!");
            cachedReportTheoKichThuoc = JasperCompileManager.compileReport(in);
            return cachedReportTheoKichThuoc;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopGiaTri() throws JRException {
        if (cachedReportTongHopGiaTri != null) return cachedReportTongHopGiaTri;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_tong_hop_gia_tri_ban_theo_thang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_tong_hop_gia_tri_ban_theo_thang.jrxml không tồn tại!");
            cachedReportTongHopGiaTri = JasperCompileManager.compileReport(in);
            return cachedReportTongHopGiaTri;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopSoLuong() throws JRException {
        if (cachedReportTongHopSoLuong != null) return cachedReportTongHopSoLuong;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_tong_hop_so_luong_ban_theo_thang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_tong_hop_so_luong_ban_theo_thang.jrxml không tồn tại!");
            cachedReportTongHopSoLuong = JasperCompileManager.compileReport(in);
            return cachedReportTongHopSoLuong;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTheoNgay() throws JRException {
        if (cachedReportTheoNgay != null) return cachedReportTheoNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ban_hang_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ban_hang_theo_ngay.jrxml không tồn tại!");
            cachedReportTheoNgay = JasperCompileManager.compileReport(in);
            return cachedReportTheoNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportChiTietTheoNgay() throws JRException {
        if (cachedReportChiTietTheoNgay != null) return cachedReportChiTietTheoNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_chi_tiet_ban_hang_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_chi_tiet_ban_hang_theo_ngay.jrxml không tồn tại!");
            cachedReportChiTietTheoNgay = JasperCompileManager.compileReport(in);
            return cachedReportChiTietTheoNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTheoKhachHang() throws JRException {
        if (cachedReportTheoKhachHang != null) return cachedReportTheoKhachHang;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ban_hang_theo_khach_hang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ban_hang_theo_khach_hang.jrxml không tồn tại!");
            cachedReportTheoKhachHang = JasperCompileManager.compileReport(in);
            return cachedReportTheoKhachHang;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTheoNhanVien() throws JRException {
        if (cachedReportTheoNhanVien != null) return cachedReportTheoNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ban_hang_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ban_hang_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportTheoNhanVien = JasperCompileManager.compileReport(in);
            return cachedReportTheoNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopMatHangTheoKhachHang() throws JRException {
        if (cachedReportTongHopMatHangTheoKhachHang != null) return cachedReportTongHopMatHangTheoKhachHang;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_khach_hang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_ban_theo_khach_hang.jrxml không tồn tại!");
            cachedReportTongHopMatHangTheoKhachHang = JasperCompileManager.compileReport(in);
            return cachedReportTongHopMatHangTheoKhachHang;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopMatHangTheoNgay() throws JRException {
        if (cachedReportTongHopMatHangTheoNgay != null) return cachedReportTongHopMatHangTheoNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_ban_theo_ngay.jrxml không tồn tại!");
            cachedReportTongHopMatHangTheoNgay = JasperCompileManager.compileReport(in);
            return cachedReportTongHopMatHangTheoNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopMatHangTheoNhanVien() throws JRException {
        if (cachedReportTongHopMatHangTheoNhanVien != null) return cachedReportTongHopMatHangTheoNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_ban_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportTongHopMatHangTheoNhanVien = JasperCompileManager.compileReport(in);
            return cachedReportTongHopMatHangTheoNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopBanHangTheoNgay() throws JRException {
        if (cachedReportTongHopBanHangTheoNgay != null) return cachedReportTongHopBanHangTheoNgay;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_ban_hang_theo_ngay.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_ban_hang_theo_ngay.jrxml không tồn tại!");
            cachedReportTongHopBanHangTheoNgay = JasperCompileManager.compileReport(in);
            return cachedReportTongHopBanHangTheoNgay;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopBanTheoKhachHang() throws JRException {
        if (cachedReportTongHopBanTheoKhachHang != null) return cachedReportTongHopBanTheoKhachHang;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_ban_theo_khach_hang.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_ban_theo_khach_hang.jrxml không tồn tại!");
            cachedReportTongHopBanTheoKhachHang = JasperCompileManager.compileReport(in);
            return cachedReportTongHopBanTheoKhachHang;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopBanTheoNhanVienBan() throws JRException {
        if (cachedReportTongHopBanTheoNhanVienBan != null) return cachedReportTongHopBanTheoNhanVienBan;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_ban_theo_nhan_vien_ban.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_ban_theo_nhan_vien_ban.jrxml không tồn tại!");
            cachedReportTongHopBanTheoNhanVienBan = JasperCompileManager.compileReport(in);
            return cachedReportTongHopBanTheoNhanVienBan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopBanTheoThuNgan() throws JRException {
        if (cachedReportTongHopBanTheoThuNgan != null) return cachedReportTongHopBanTheoThuNgan;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_ban_theo_thu_ngan.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_ban_theo_thu_ngan.jrxml không tồn tại!");
            cachedReportTongHopBanTheoThuNgan = JasperCompileManager.compileReport(in);
            return cachedReportTongHopBanTheoThuNgan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportChiTietTheoThuNgan() throws JRException {
        if (cachedReportChiTietTheoThuNgan != null) return cachedReportChiTietTheoThuNgan;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_chi_tiet_ban_hang_theo_thu_ngan.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_chi_tiet_ban_hang_theo_thu_ngan.jrxml không tồn tại!");
            cachedReportChiTietTheoThuNgan = JasperCompileManager.compileReport(in);
            return cachedReportChiTietTheoThuNgan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportBanHangTheoThuNgan() throws JRException {
        if (cachedReportBanHangTheoThuNgan != null) return cachedReportBanHangTheoThuNgan;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ban_hang_theo_thu_ngan.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ban_hang_theo_thu_ngan.jrxml không tồn tại!");
            cachedReportBanHangTheoThuNgan = JasperCompileManager.compileReport(in);
            return cachedReportBanHangTheoThuNgan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopMatHangTheoThuNgan() throws JRException {
        if (cachedReportTongHopMatHangTheoThuNgan != null) return cachedReportTongHopMatHangTheoThuNgan;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_mat_hang_ban_theo_thu_ngan.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_mat_hang_ban_theo_thu_ngan.jrxml không tồn tại!");
            cachedReportTongHopMatHangTheoThuNgan = JasperCompileManager.compileReport(in);
            return cachedReportTongHopMatHangTheoThuNgan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTienVanChuyenTheoNhanVien() throws JRException {
        if (cachedReportTienVanChuyenTheoNhanVien != null) return cachedReportTienVanChuyenTheoNhanVien;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_tien_van_chuyen_theo_nhan_vien.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_tien_van_chuyen_theo_nhan_vien.jrxml không tồn tại!");
            cachedReportTienVanChuyenTheoNhanVien = JasperCompileManager.compileReport(in);
            return cachedReportTienVanChuyenTheoNhanVien;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportChiTietHangBanTheoDoiTac() throws JRException {
        if (cachedReportChiTietHangBanTheoDoiTac != null) return cachedReportChiTietHangBanTheoDoiTac;
        try (InputStream in = getClass().getResourceAsStream("/reports/chi_tiet_hang_ban_theo_doi_tac.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/chi_tiet_hang_ban_theo_doi_tac.jrxml không tồn tại!");
            cachedReportChiTietHangBanTheoDoiTac = JasperCompileManager.compileReport(in);
            return cachedReportChiTietHangBanTheoDoiTac;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopHoaHongTheoNVKD() throws JRException {
        if (cachedReportTongHopHoaHongTheoNVKD != null) return cachedReportTongHopHoaHongTheoNVKD;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_hoa_hong_theo_nvkd.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/tong_hop_hoa_hong_theo_nvkd.jrxml không tồn tại!");
            cachedReportTongHopHoaHongTheoNVKD = JasperCompileManager.compileReport(in);
            return cachedReportTongHopHoaHongTheoNVKD;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportChiTietBanHangTheoHoaDon() throws JRException {
        if (cachedReportChiTietBanHangTheoHoaDon != null) return cachedReportChiTietBanHangTheoHoaDon;
        try (InputStream in = getClass().getResourceAsStream("/reports/chi_tiet_ban_hang_theo_hoa_don.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/chi_tiet_ban_hang_theo_hoa_don.jrxml không tồn tại!");
            cachedReportChiTietBanHangTheoHoaDon = JasperCompileManager.compileReport(in);
            return cachedReportChiTietBanHangTheoHoaDon;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportChiTietHangKhuyenMai() throws JRException {
        if (cachedReportChiTietHangKhuyenMai != null) return cachedReportChiTietHangKhuyenMai;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_chi_tiet_hang_khuyen_mai.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_chi_tiet_hang_khuyen_mai.jrxml không tồn tại!");
            cachedReportChiTietHangKhuyenMai = JasperCompileManager.compileReport(in);
            return cachedReportChiTietHangKhuyenMai;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    private Map<String, String> getUserMap() {
        Map<String, String> map = new HashMap<>();
        try {
            List<SUser> list = userDAO.getAll();
            if (list != null) {
                for (SUser u : list) {
                    if (u.getId() != null) map.put(u.getId(), u.getName() != null && !u.getName().isEmpty() ? u.getName() : u.getUsername());
                    if (u.getUsername() != null) map.put(u.getUsername(), u.getName() != null && !u.getName().isEmpty() ? u.getName() : u.getUsername());
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    private String getThuNganName(DonHang dh, Map<String, String> userMap) {
        if (dh == null) return "Administrator";
        if (dh.getNguoiThanhToan() != null && !dh.getNguoiThanhToan().isEmpty()) {
            return dh.getNguoiThanhToan();
        }
        if (dh.getUserCreatedId() != null && userMap.containsKey(dh.getUserCreatedId())) {
            return userMap.get(dh.getUserCreatedId());
        }
        if (dh.getTenNguoiTao() != null && !dh.getTenNguoiTao().isEmpty()) {
            return dh.getTenNguoiTao();
        }
        return "Administrator";
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

    private Map<String, String> getProductGroupMap() {
        Map<String, String> map = new HashMap<>();
        try {
            List<MatHang> list = matHangDAO.getAll();
            for (MatHang mh : list) {
                String tenNhom = mh.getTenNhom() != null && !mh.getTenNhom().trim().isEmpty() ? mh.getTenNhom() : "Khác";
                if (mh.getId() != null) map.put(mh.getId(), tenNhom);
                if (mh.getCode() != null) map.put(mh.getCode(), tenNhom);
            }
        } catch (Exception ignored) {}
        return map;
    }

    private String getNhomHangName(DonHangChiTiet ct, Map<String, String> mhGroupMap) {
        if (ct == null) return "Khác";
        if (ct.getMatHangId() != null && mhGroupMap.containsKey(ct.getMatHangId())) {
            return mhGroupMap.get(ct.getMatHangId());
        }
        if (ct.getMaHang() != null && mhGroupMap.containsKey(ct.getMaHang())) {
            return mhGroupMap.get(ct.getMaHang());
        }
        return "Khác";
    }

    public JasperPrint generateBanHangTheoGio(Date tuGio, Date denGio) throws JRException {
        JasperReport jasperReport = getCompiledReportTheoGio();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(null, null, null, null);
        List<BanHangTheoGioReportRow> rows = new ArrayList<>();

        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
        String tuGioStr = tuGio != null ? timeFmt.format(tuGio) : "08:00";
        String denGioStr = denGio != null ? timeFmt.format(denGio) : "22:00";

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            if (dDate == null) continue;

            if (tuGio != null && denGio != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(dDate);
                int orderHour = c.get(Calendar.HOUR_OF_DAY);
                int orderMinute = c.get(Calendar.MINUTE);
                int orderMinutes = orderHour * 60 + orderMinute;

                Calendar cFrom = Calendar.getInstance();
                cFrom.setTime(tuGio);
                int fromMinutes = cFrom.get(Calendar.HOUR_OF_DAY) * 60 + cFrom.get(Calendar.MINUTE);

                Calendar cTo = Calendar.getInstance();
                cTo.setTime(denGio);
                int toMinutes = cTo.get(Calendar.HOUR_OF_DAY) * 60 + cTo.get(Calendar.MINUTE);

                if (orderMinutes < fromMinutes || orderMinutes > toMinutes) {
                    continue;
                }
            }

            Calendar c = Calendar.getInstance();
            c.setTime(dDate);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            String groupHeader = String.format("Từ %02dh00 đến %02dh00", hour, hour + 1);

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    rows.add(new BanHangTheoGioReportRow(
                            0,
                            groupHeader,
                            ct.getMaHang() != null ? ct.getMaHang() : "",
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ct.getSoLuong(),
                            ct.getDonGia()
                    ));
                }
            }
        }

        // Sort by groupHeader, then maHang
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaHang().compareToIgnoreCase(r2.getMaHang());
        });

        // Set STT
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_reportTitle", "BÁO CÁO BÁN HÀNG THEO GIỜ");
        parameters.put("p_tuGio", tuGioStr);
        parameters.put("p_denGio", denGioStr);
        parameters.put("p_nguoiIn", "Admin");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateBanHangTheoKichThuoc(Date tuNgay, Date denNgay) throws JRException {
        JasperReport jasperReport = getCompiledReportTheoKichThuoc();
        SystemConfig sysConfig = configDAO.loadConfig();
        Map<String, String> mhGroupMap = getProductGroupMap();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        List<BanHangTheoKichThuocReportRow> rows = new ArrayList<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String groupHeader = "Nhóm mặt hàng: " + getNhomHangName(ct, mhGroupMap);
                    String sizeMau = ct.getKichThuoc() != null && !ct.getKichThuoc().isEmpty() ? ct.getKichThuoc() : (ct.getGhiChu() != null && !ct.getGhiChu().isEmpty() ? ct.getGhiChu() : "Tiêu chuẩn");

                    rows.add(new BanHangTheoKichThuocReportRow(
                            0,
                            groupHeader,
                            ct.getMaHang() != null ? ct.getMaHang() : "",
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái",
                            ct.getSoLuong(),
                            sizeMau,
                            ct.getDonGia(),
                            ct.getThanhTien()
                    ));
                }
            }
        }

        // Sort by groupHeader, then maHang
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaHang().compareToIgnoreCase(r2.getMaHang());
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
        parameters.put("p_reportTitle", "BÁO CÁO BÁN HÀNG THEO KÍCH THƯỚC");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");
        parameters.put("p_ngayLap", sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopGiaTriBanTheoThang(int thang, int nam) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopGiaTri();
        SystemConfig sysConfig = configDAO.loadConfig();
        Map<String, String> mhGroupMap = getProductGroupMap();

        Calendar calStart = Calendar.getInstance();
        calStart.set(nam, thang - 1, 1, 0, 0, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(nam, thang - 1, calStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(calStart.getTime(), calEnd.getTime(), null, null);
        Map<String, TongHopBanTheoThangReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            if (dDate == null) continue;

            Calendar c = Calendar.getInstance();
            c.setTime(dDate);
            int day = c.get(Calendar.DAY_OF_MONTH);

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String maSP = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    if (maSP.isEmpty()) continue;

                    TongHopBanTheoThangReportRow row = rowMap.get(maSP);
                    if (row == null) {
                        row = new TongHopBanTheoThangReportRow();
                        row.setMaSP(maSP);
                        row.setTenSP(ct.getTenHang() != null ? ct.getTenHang() : "");
                        row.setDonViTinh(ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái");
                        row.setGroupHeader("Nhóm mặt hàng: " + getNhomHangName(ct, mhGroupMap));
                        row.setTongBan(0.0);
                        rowMap.put(maSP, row);
                    }

                    double amount = ct.getThanhTien();
                    row.setTongBan(row.getTongBan() + amount);

                    double curDayVal = getDayVal(row, day);
                    row.setDayValue(day, curDayVal + amount);
                }
            }
        }

        List<TongHopBanTheoThangReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaSP().compareToIgnoreCase(r2.getMaSP());
        });

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
        parameters.put("p_reportTitle", "BÁO CÁO TỔNG HỢP GIÁ TRỊ BÁN THEO THÁNG");
        parameters.put("p_thangNam", String.format("%02d/%04d", thang, nam));
        parameters.put("p_ngayLap", sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopSoLuongBanTheoThang(int thang, int nam) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopSoLuong();
        SystemConfig sysConfig = configDAO.loadConfig();
        Map<String, String> mhGroupMap = getProductGroupMap();

        Calendar calStart = Calendar.getInstance();
        calStart.set(nam, thang - 1, 1, 0, 0, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(nam, thang - 1, calStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(calStart.getTime(), calEnd.getTime(), null, null);
        Map<String, TongHopBanTheoThangReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            if (dDate == null) continue;

            Calendar c = Calendar.getInstance();
            c.setTime(dDate);
            int day = c.get(Calendar.DAY_OF_MONTH);

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String maSP = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    if (maSP.isEmpty()) continue;

                    TongHopBanTheoThangReportRow row = rowMap.get(maSP);
                    if (row == null) {
                        row = new TongHopBanTheoThangReportRow();
                        row.setMaSP(maSP);
                        row.setTenSP(ct.getTenHang() != null ? ct.getTenHang() : "");
                        row.setDonViTinh(ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái");
                        row.setGroupHeader("Nhóm mặt hàng: " + getNhomHangName(ct, mhGroupMap));
                        row.setTongBan(0.0);
                        rowMap.put(maSP, row);
                    }

                    double qty = ct.getSoLuong();
                    row.setTongBan(row.getTongBan() + qty);

                    double curDayVal = getDayVal(row, day);
                    row.setDayValue(day, curDayVal + qty);
                }
            }
        }

        List<TongHopBanTheoThangReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaSP().compareToIgnoreCase(r2.getMaSP());
        });

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
        parameters.put("p_reportTitle", "BÁO CÁO TỔNG HỢP SỐ LƯỢNG BÁN THEO THÁNG");
        parameters.put("p_thangNam", String.format("%02d/%04d", thang, nam));
        parameters.put("p_ngayLap", sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateBanHangTheoNgay(Date tuNgay, Date denNgay, String nhanVienId, String khachHangId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTheoNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, cuaHangId, null);
        List<BanHangTheoNgayReportRow> rows = new ArrayList<>();
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy");

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equals(dh.getNhanVienXuatId())) {
                continue;
            }
            if (khachHangId != null && !khachHangId.trim().isEmpty() && !khachHangId.equals(dh.getKhachHangId())) {
                continue;
            }

            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            String groupNgay = "Ngày: " + (dDate != null ? dayFmt.format(dDate) : "Chưa rõ");

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            double tienHang = dh.getTienHang();
            double giamGia = dh.getTienGiamGia();
            double tongCong = dh.getTongCong();
            double thanhToan = dh.getTienThanhToan();

            rows.add(new BanHangTheoNgayReportRow(
                    0,
                    groupNgay,
                    dh.getName() != null ? dh.getName() : "",
                    dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "",
                    "Administrator",
                    tienHang,
                    doiTra,
                    giamGia,
                    tongCong,
                    thanhToan,
                    0.0
            ));
        }

        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupNgay().compareToIgnoreCase(r2.getGroupNgay());
            if (cmp != 0) return cmp;
            return r1.getSoPhieu().compareToIgnoreCase(r2.getSoPhieu());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "BÁO CÁO BÁN HÀNG THEO NGÀY");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateChiTietBanHangTheoNgay(Date tuNgay, Date denNgay) throws JRException {
        JasperReport jasperReport = getCompiledReportChiTietTheoNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        List<ChiTietBanHangTheoNgayReportRow> rows = new ArrayList<>();
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy");

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            String ngayStr = dDate != null ? dayFmt.format(dDate) : "";
            String tenKhach = dh.getTenKhach() != null ? dh.getTenKhach() : "";

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    rows.add(new ChiTietBanHangTheoNgayReportRow(
                            ngayStr,
                            dh.getName() != null ? dh.getName() : "",
                            tenKhach,
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ct.getSoLuong(),
                            ct.getDonGia(),
                            ct.getTiLeGiamGia(),
                            ct.getTienGiam(),
                            ct.getThanhTien(),
                            dh.getTienHang(),
                            dh.getTienGiamGia(),
                            dh.getTongCong(),
                            dh.getTienThanhToan()
                    ));
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    rows.add(new ChiTietBanHangTheoNgayReportRow(
                            ngayStr,
                            dh.getName() != null ? dh.getName() : "",
                            tenKhach,
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            -ct.getSoLuong(),
                            ct.getDonGia(),
                            ct.getTiLeGiamGia(),
                            ct.getTienGiam(),
                            -ct.getThanhTien(),
                            0.0,
                            0.0,
                            -ct.getThanhTien(),
                            -ct.getThanhTien()
                    ));
                }
            }
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "BÁO CÁO CHI TIẾT BÁN HÀNG THEO NGÀY");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");
        parameters.put("p_ngayLap", sdf.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateBanHangTheoKhachHang(Date tuNgay, Date denNgay, String khachHangId, String nhanVienId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTheoKhachHang();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, cuaHangId, null);
        List<BanHangTheoKhachHangReportRow> rows = new ArrayList<>();
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy");

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (khachHangId != null && !khachHangId.trim().isEmpty() && !khachHangId.equals(dh.getKhachHangId())) {
                continue;
            }
            if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equals(dh.getNhanVienXuatId())) {
                continue;
            }

            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            String ngayStr = dDate != null ? dayFmt.format(dDate) : "";
            String groupKhach = "Khách hàng: " + (dh.getTenKhach() != null && !dh.getTenKhach().isEmpty() ? dh.getTenKhach() : "Khách lẻ");

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            rows.add(new BanHangTheoKhachHangReportRow(
                    0,
                    groupKhach,
                    ngayStr,
                    dh.getName() != null ? dh.getName() : "",
                    dh.getTienHang(),
                    doiTra,
                    dh.getTienGiamGia(),
                    0.0,
                    dh.getTongCong()
            ));
        }

        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupKhachHang().compareToIgnoreCase(r2.getGroupKhachHang());
            if (cmp != 0) return cmp;
            return r1.getSoPhieu().compareToIgnoreCase(r2.getSoPhieu());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "BÁO CÁO BÁN HÀNG THEO KHÁCH HÀNG");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateBanHangTheoNhanVien(Date tuNgay, Date denNgay, String khachHangId, String nhanVienId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTheoNhanVien();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, cuaHangId, null);
        List<BanHangTheoNhanVienReportRow> rows = new ArrayList<>();
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy");

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (khachHangId != null && !khachHangId.trim().isEmpty() && !khachHangId.equals(dh.getKhachHangId())) {
                continue;
            }
            if (nhanVienId != null && !nhanVienId.trim().isEmpty() && !nhanVienId.equals(dh.getNhanVienXuatId())) {
                continue;
            }

            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            String ngayStr = dDate != null ? dayFmt.format(dDate) : "";
            String groupNV = "Nhân viên bán: " + (dh.getTenNhanVien() != null && !dh.getTenNhanVien().isEmpty() ? dh.getTenNhanVien() : "Chưa phân công");

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            rows.add(new BanHangTheoNhanVienReportRow(
                    0,
                    groupNV,
                    ngayStr,
                    dh.getName() != null ? dh.getName() : "",
                    dh.getTienHang(),
                    dh.getTienGiamGia(),
                    doiTra,
                    0.0,
                    dh.getTongCong()
            ));
        }

        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupNhanVien().compareToIgnoreCase(r2.getGroupNhanVien());
            if (cmp != 0) return cmp;
            return r1.getSoPhieu().compareToIgnoreCase(r2.getSoPhieu());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN KINH DOANH");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopMatHangBanTheoKhachHang(Date tuNgay, Date denNgay, String nhomKhachHangId, String khachHangId, String matHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopMatHangTheoKhachHang();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        Map<String, TongHopMatHangBanTheoKhachHangReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (khachHangId != null && !khachHangId.trim().isEmpty() && !khachHangId.equals(dh.getKhachHangId())) {
                continue;
            }

            String tenKhach = dh.getTenKhach() != null && !dh.getTenKhach().isEmpty() ? dh.getTenKhach() : "Khách lẻ";
            String groupKhach = "Khách hàng: " + tenKhach;

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    if (matHangId != null && !matHangId.trim().isEmpty() && !matHangId.equals(maHang) && !matHangId.equals(ct.getMatHangId())) {
                        continue;
                    }
                    String key = groupKhach + "___" + maHang;
                    TongHopMatHangBanTheoKhachHangReportRow row = rowMap.get(key);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoKhachHangReportRow(0, groupKhach, maHang, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(key, row);
                    }
                    row.setSoLuongBan(row.getSoLuongBan() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() + ct.getThanhTien());
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    if (matHangId != null && !matHangId.trim().isEmpty() && !matHangId.equals(maHang) && !matHangId.equals(ct.getMatHangId())) {
                        continue;
                    }
                    String key = groupKhach + "___" + maHang;
                    TongHopMatHangBanTheoKhachHangReportRow row = rowMap.get(key);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoKhachHangReportRow(0, groupKhach, maHang, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(key, row);
                    }
                    row.setSoLuongTra(row.getSoLuongTra() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() - ct.getThanhTien());
                }
            }
        }

        List<TongHopMatHangBanTheoKhachHangReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupKhachHang().compareToIgnoreCase(r2.getGroupKhachHang());
            if (cmp != 0) return cmp;
            return r1.getMaHang().compareToIgnoreCase(r2.getMaHang());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP MẶT HÀNG BÁN THEO KHÁCH HÀNG");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopMatHangBanTheoNgay(Date tuNgay, Date denNgay, String khoXuatId, String nhanVienXuatId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopMatHangTheoNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, khoXuatId, null);
        Map<String, TongHopMatHangBanTheoNgayReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (nhanVienXuatId != null && !nhanVienXuatId.trim().isEmpty() && !nhanVienXuatId.equals(dh.getNhanVienXuatId())) {
                continue;
            }

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    TongHopMatHangBanTheoNgayReportRow row = rowMap.get(maHang);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoNgayReportRow(0, maHang, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(maHang, row);
                    }
                    row.setSoLuongBan(row.getSoLuongBan() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() + ct.getThanhTien());
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    TongHopMatHangBanTheoNgayReportRow row = rowMap.get(maHang);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoNgayReportRow(0, maHang, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(maHang, row);
                    }
                    row.setSoLuongTra(row.getSoLuongTra() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() - ct.getThanhTien());
                }
            }
        }

        List<TongHopMatHangBanTheoNgayReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> r1.getMaHang().compareToIgnoreCase(r2.getMaHang()));

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String ngayText;
        if (tuNgay != null && denNgay != null && sdf.format(tuNgay).equals(sdf.format(denNgay))) {
            ngayText = "Ngày: " + sdf.format(tuNgay);
        } else {
            ngayText = "Ngày từ " + (tuNgay != null ? sdf.format(tuNgay) : "") + " đến " + (denNgay != null ? sdf.format(denNgay) : "");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP MẶT HÀNG BÁN THEO NGÀY");
        parameters.put("p_ngayText", ngayText);

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopMatHangBanTheoNhanVien(Date tuNgay, Date denNgay, String khoXuatId, String nhanVienBanId, String nhanVienXuatId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopMatHangTheoNhanVien();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, khoXuatId, null);
        Map<String, TongHopMatHangBanTheoNhanVienReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (nhanVienBanId != null && !nhanVienBanId.trim().isEmpty() && !nhanVienBanId.equals(dh.getNhanVienXuatId())) {
                continue;
            }
            if (nhanVienXuatId != null && !nhanVienXuatId.trim().isEmpty() && !nhanVienXuatId.equals(dh.getNhanVienXuatId())) {
                continue;
            }

            String groupNV = "Nhân viên bán: " + (dh.getTenNhanVien() != null && !dh.getTenNhanVien().isEmpty() ? dh.getTenNhanVien() : "Chưa phân công");

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    String key = groupNV + "___" + maHang;
                    TongHopMatHangBanTheoNhanVienReportRow row = rowMap.get(key);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoNhanVienReportRow(0, groupNV, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(key, row);
                    }
                    row.setSoLuongBan(row.getSoLuongBan() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() + ct.getThanhTien());
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    String key = groupNV + "___" + maHang;
                    TongHopMatHangBanTheoNhanVienReportRow row = rowMap.get(key);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoNhanVienReportRow(0, groupNV, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(key, row);
                    }
                    row.setSoLuongTra(row.getSoLuongTra() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() - ct.getThanhTien());
                }
            }
        }

        List<TongHopMatHangBanTheoNhanVienReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupNhanVien().compareToIgnoreCase(r2.getGroupNhanVien());
            if (cmp != 0) return cmp;
            return r1.getMatHang().compareToIgnoreCase(r2.getMatHang());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP MẶT HÀNG BÁN THEO NHÂN VIÊN KINH DOANH");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopBanHangTheoNgay(Date tuNgay, Date denNgay, String khoXuatId, String nhanVienXuatId, String khachHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopBanHangTheoNgay();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, khoXuatId, null);
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, TongHopBanHangTheoNgayReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (nhanVienXuatId != null && !nhanVienXuatId.trim().isEmpty() && !nhanVienXuatId.equals(dh.getNhanVienXuatId())) {
                continue;
            }
            if (khachHangId != null && !khachHangId.trim().isEmpty() && !khachHangId.equals(dh.getKhachHangId())) {
                continue;
            }

            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            String ngayStr = dDate != null ? dayFmt.format(dDate) : "Chưa rõ";

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            TongHopBanHangTheoNgayReportRow row = rowMap.get(ngayStr);
            if (row == null) {
                row = new TongHopBanHangTheoNgayReportRow(0, ngayStr, 0.0, 0.0, 0.0, 0.0, 0.0);
                rowMap.put(ngayStr, row);
            }
            row.setTienHang(row.getTienHang() + dh.getTienHang());
            row.setTienGiamGia(row.getTienGiamGia() + dh.getTienGiamGia());
            row.setDoiTra(row.getDoiTra() + doiTra);
            row.setTongCong(row.getTongCong() + dh.getTongCong());
        }

        List<TongHopBanHangTheoNgayReportRow> rows = new ArrayList<>(rowMap.values());
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP BÁN HÀNG THEO NGÀY");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopBanTheoKhachHang(Date tuNgay, Date denNgay, String khachHangId, String khoXuatId, String nhanVienXuatId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopBanTheoKhachHang();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, khoXuatId, null);
        Map<String, TongHopBanTheoKhachHangReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (khachHangId != null && !khachHangId.trim().isEmpty() && !khachHangId.equals(dh.getKhachHangId())) {
                continue;
            }
            if (nhanVienXuatId != null && !nhanVienXuatId.trim().isEmpty() && !nhanVienXuatId.equals(dh.getNhanVienXuatId())) {
                continue;
            }

            String maKhach = dh.getKhachHangId() != null ? dh.getKhachHangId() : "";
            String tenKhach = dh.getTenKhach() != null && !dh.getTenKhach().isEmpty() ? dh.getTenKhach() : "Khách lẻ";
            String diaChi = dh.getDiaChiKhach() != null ? dh.getDiaChiKhach() : "";
            String key = maKhach.isEmpty() ? tenKhach : maKhach;

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            TongHopBanTheoKhachHangReportRow row = rowMap.get(key);
            if (row == null) {
                row = new TongHopBanTheoKhachHangReportRow(0, maKhach, tenKhach, diaChi, 0.0, 0.0, 0.0, 0.0, 0.0);
                rowMap.put(key, row);
            }
            row.setTienHang(row.getTienHang() + dh.getTienHang());
            row.setTienGiamGia(row.getTienGiamGia() + dh.getTienGiamGia());
            row.setDoiTra(row.getDoiTra() + doiTra);
            row.setTongCong(row.getTongCong() + dh.getTongCong());
        }

        List<TongHopBanTheoKhachHangReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> r1.getTenKhachHang().compareToIgnoreCase(r2.getTenKhachHang()));

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP BÁN THEO KHÁCH HÀNG");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    private double getDayVal(TongHopBanTheoThangReportRow row, int day) {
        Double val = null;
        switch (day) {
            case 1: val = row.getD01(); break;
            case 2: val = row.getD02(); break;
            case 3: val = row.getD03(); break;
            case 4: val = row.getD04(); break;
            case 5: val = row.getD05(); break;
            case 6: val = row.getD06(); break;
            case 7: val = row.getD07(); break;
            case 8: val = row.getD08(); break;
            case 9: val = row.getD09(); break;
            case 10: val = row.getD10(); break;
            case 11: val = row.getD11(); break;
            case 12: val = row.getD12(); break;
            case 13: val = row.getD13(); break;
            case 14: val = row.getD14(); break;
            case 15: val = row.getD15(); break;
            case 16: val = row.getD16(); break;
            case 17: val = row.getD17(); break;
            case 18: val = row.getD18(); break;
            case 19: val = row.getD19(); break;
            case 20: val = row.getD20(); break;
            case 21: val = row.getD21(); break;
            case 22: val = row.getD22(); break;
            case 23: val = row.getD23(); break;
            case 24: val = row.getD24(); break;
            case 25: val = row.getD25(); break;
            case 26: val = row.getD26(); break;
            case 27: val = row.getD27(); break;
            case 28: val = row.getD28(); break;
            case 29: val = row.getD29(); break;
            case 30: val = row.getD30(); break;
            case 31: val = row.getD31(); break;
        }
        return val != null ? val : 0.0;
    }

    public JasperPrint generateTongHopBanTheoNhanVienBan(Date tuNgay, Date denNgay, String khoXuatId, String nhanVienXuatId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopBanTheoNhanVienBan();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, khoXuatId, null);
        Map<String, TongHopBanTheoNhanVienBanReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (nhanVienXuatId != null && !nhanVienXuatId.trim().isEmpty() && !nhanVienXuatId.equals(dh.getNhanVienXuatId())) {
                continue;
            }

            String nvBan = dh.getTenNhanVien() != null && !dh.getTenNhanVien().isEmpty() ? dh.getTenNhanVien() : "Chưa phân công";

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            TongHopBanTheoNhanVienBanReportRow row = rowMap.get(nvBan);
            if (row == null) {
                row = new TongHopBanTheoNhanVienBanReportRow(0, nvBan, 0.0, 0.0, 0.0, 0.0, 0.0);
                rowMap.put(nvBan, row);
            }
            row.setTienHang(row.getTienHang() + dh.getTienHang());
            row.setTienGiamGia(row.getTienGiamGia() + dh.getTienGiamGia());
            row.setDoiTra(row.getDoiTra() + doiTra);
            row.setPhiVanChuyen(row.getPhiVanChuyen() + dh.getPhiVanChuyen());
            row.setTongCong(row.getTongCong() + dh.getTongCong());
        }

        List<TongHopBanTheoNhanVienBanReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> r1.getNhanVienBan().compareToIgnoreCase(r2.getNhanVienBan()));

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP BÁN THEO NHÂN VIÊN BÁN");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopBanTheoThuNgan(Date tuNgay, Date denNgay, String khoXuatId, String nhanVienXuatId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopBanTheoThuNgan();
        SystemConfig sysConfig = configDAO.loadConfig();
        Map<String, String> userMap = getUserMap();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, khoXuatId, null);
        Map<String, TongHopBanTheoThuNganReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (nhanVienXuatId != null && !nhanVienXuatId.trim().isEmpty() && !nhanVienXuatId.equals(dh.getNhanVienXuatId())) {
                continue;
            }

            String thuNgan = getThuNganName(dh, userMap);

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            TongHopBanTheoThuNganReportRow row = rowMap.get(thuNgan);
            if (row == null) {
                row = new TongHopBanTheoThuNganReportRow(0, thuNgan, 0.0, 0.0, 0.0, 0.0, 0.0);
                rowMap.put(thuNgan, row);
            }
            row.setTienHang(row.getTienHang() + dh.getTienHang());
            row.setTienGiamGia(row.getTienGiamGia() + dh.getTienGiamGia());
            row.setPhiVanChuyen(row.getPhiVanChuyen() + dh.getPhiVanChuyen());
            row.setDoiTra(row.getDoiTra() + doiTra);
            row.setTongCong(row.getTongCong() + dh.getTongCong());
        }

        List<TongHopBanTheoThuNganReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> r1.getThuNgan().compareToIgnoreCase(r2.getThuNgan()));

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP BÁN THEO THU NGÂN");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateChiTietBanHangTheoThuNgan(Date tuNgay, Date denNgay, String thuNganId) throws JRException {
        JasperReport jasperReport = getCompiledReportChiTietTheoThuNgan();
        SystemConfig sysConfig = configDAO.loadConfig();
        Map<String, String> userMap = getUserMap();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        List<ChiTietBanHangTheoThuNganReportRow> rows = new ArrayList<>();
        SimpleDateFormat itemDtFmt = new SimpleDateFormat("dd/MM HH:mm");

        String thuNganFilterName = null;
        if (thuNganId != null && !thuNganId.trim().isEmpty()) {
            thuNganFilterName = userMap.get(thuNganId);
            if (thuNganFilterName == null) thuNganFilterName = thuNganId;
        }

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            String tnName = getThuNganName(dh, userMap);

            if (thuNganId != null && !thuNganId.trim().isEmpty()) {
                if (!thuNganId.equals(dh.getUserCreatedId()) && !thuNganId.equalsIgnoreCase(tnName)) {
                    continue;
                }
            }

            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            String ngayGioStr = dDate != null ? itemDtFmt.format(dDate) : "";

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    rows.add(new ChiTietBanHangTheoThuNganReportRow(
                            0,
                            maHang,
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ngayGioStr,
                            ct.getDonGia(),
                            ct.getSoLuong(),
                            ct.getTiLeGiamGia(),
                            ct.getThanhTien()
                    ));
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    rows.add(new ChiTietBanHangTheoThuNganReportRow(
                            0,
                            maHang,
                            ct.getTenHang() != null ? ct.getTenHang() : "",
                            ngayGioStr,
                            ct.getDonGia(),
                            -ct.getSoLuong(),
                            ct.getTiLeGiamGia(),
                            -ct.getThanhTien()
                    ));
                }
            }
        }

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat ngayInFmt = new SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "BÁO CÁO CHI TIẾT BÁN HÀNG THEO THU NGÂN");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");
        parameters.put("p_thuNganText", thuNganFilterName != null ? thuNganFilterName : "Tất cả");
        parameters.put("p_ngayInText", ngayInFmt.format(new Date()));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateBanHangTheoThuNgan(Date tuNgay, Date denNgay, String khachHangId, String nhanVienXuatId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportBanHangTheoThuNgan();
        SystemConfig sysConfig = configDAO.loadConfig();
        Map<String, String> userMap = getUserMap();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        List<BanHangTheoThuNganReportRow> rows = new ArrayList<>();
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy");

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            if (khachHangId != null && !khachHangId.trim().isEmpty() && !khachHangId.equals(dh.getKhachHangId())) {
                continue;
            }
            if (nhanVienXuatId != null && !nhanVienXuatId.trim().isEmpty() && !nhanVienXuatId.equals(dh.getNhanVienXuatId())) {
                continue;
            }
            if (cuaHangId != null && !cuaHangId.trim().isEmpty() && !cuaHangId.equals(dh.getCuaHangId())) {
                continue;
            }

            String tnName = getThuNganName(dh, userMap);
            String groupThuNgan = "Thu ngân: " + tnName;

            Date dDate = dh.getNgay() != null ? dh.getNgay() : dh.getTimeCreated();
            String ngayStr = dDate != null ? dayFmt.format(dDate) : "";

            double doiTra = 0.0;
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    doiTra += ct.getThanhTien();
                }
            }

            rows.add(new BanHangTheoThuNganReportRow(
                    0,
                    groupThuNgan,
                    ngayStr,
                    dh.getName() != null ? dh.getName() : "",
                    dh.getTenKhach() != null ? dh.getTenKhach() : "Khách lẻ",
                    dh.getTienHang(),
                    doiTra,
                    dh.getTienGiamGia(),
                    dh.getPhiVanChuyen(),
                    dh.getTongCong()
            ));
        }

        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupThuNgan().compareToIgnoreCase(r2.getGroupThuNgan());
            if (cmp != 0) return cmp;
            return r1.getNgay().compareToIgnoreCase(r2.getNgay());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "BÁO CÁO BÁN HÀNG THEO THU NGÂN");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopMatHangBanTheoThuNgan(Date tuNgay, Date denNgay, String khoXuatId, String thuNganId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopMatHangTheoThuNgan();
        SystemConfig sysConfig = configDAO.loadConfig();
        Map<String, String> userMap = getUserMap();

        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, khoXuatId, null);
        Map<String, TongHopMatHangBanTheoThuNganReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;
            String tnName = getThuNganName(dh, userMap);
            if (thuNganId != null && !thuNganId.trim().isEmpty() && !thuNganId.equals(dh.getUserCreatedId()) && !thuNganId.equalsIgnoreCase(tnName)) {
                continue;
            }

            String groupThuNgan = "Thu ngân: " + tnName;

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    String key = groupThuNgan + "___" + maHang;
                    TongHopMatHangBanTheoThuNganReportRow row = rowMap.get(key);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoThuNganReportRow(0, groupThuNgan, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(key, row);
                    }
                    row.setSoLuongBan(row.getSoLuongBan() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() + ct.getThanhTien());
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    String maHang = ct.getMaHang() != null ? ct.getMaHang() : (ct.getMatHangId() != null ? ct.getMatHangId() : "");
                    String key = groupThuNgan + "___" + maHang;
                    TongHopMatHangBanTheoThuNganReportRow row = rowMap.get(key);
                    if (row == null) {
                        row = new TongHopMatHangBanTheoThuNganReportRow(0, groupThuNgan, ct.getTenHang() != null ? ct.getTenHang() : "", ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái", 0.0, 0.0, ct.getDonGia(), 0.0);
                        rowMap.put(key, row);
                    }
                    row.setSoLuongTra(row.getSoLuongTra() + ct.getSoLuong());
                    row.setThanhTien(row.getThanhTien() - ct.getThanhTien());
                }
            }
        }

        List<TongHopMatHangBanTheoThuNganReportRow> rows = new ArrayList<>(rowMap.values());
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupThuNgan().compareToIgnoreCase(r2.getGroupThuNgan());
            if (cmp != 0) return cmp;
            return r1.getMatHang().compareToIgnoreCase(r2.getMatHang());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP MẶT HÀNG BÁN THEO THU NGÂN");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopTienVanChuyenTheoNhanVien(Date tuNgay, Date denNgay, String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportTienVanChuyenTheoNhanVien();
        SystemConfig sysConfig = configDAO.loadConfig();
        if (sysConfig == null) sysConfig = new SystemConfig();

        List<DonHang> allDh = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        Map<String, String> userMap = getUserMap();
        List<TienVanChuyenTheoNhanVienReportRow> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String fText = filterText != null ? filterText.trim().toLowerCase() : "";

        if (allDh != null) {
            for (DonHang dh : allDh) {
                if (dh.getNgay() == null) continue;
                if (dh.getStatus() < 0) continue;

                String name = dh.getTenNhanVien();
                if (name == null || name.trim().isEmpty()) {
                    name = getThuNganName(dh, userMap);
                }
                String ngayStr = sdf.format(dh.getNgay());
                double tongCong = dh.getPhiVanChuyen() > 0 ? dh.getPhiVanChuyen() : dh.getTongCong();
                double trichNV = dh.getPhiVanChuyen() > 0 ? dh.getPhiVanChuyen() * (dh.getTiLePhiVanChuyen() > 0 ? dh.getTiLePhiVanChuyen() / 100.0 : 1.0) : 0.0;

                if (!fText.isEmpty()) {
                    if (!name.toLowerCase().contains(fText) && !ngayStr.toLowerCase().contains(fText)) {
                        continue;
                    }
                }

                rows.add(new TienVanChuyenTheoNhanVienReportRow(0, ngayStr, name, tongCong, trichNV));
            }
        }

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP TIỀN VẬN CHUYỂN THEO NHÂN VIÊN");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateChiTietHangBanTheoDoiTac(Date tuNgay, Date denNgay, String selectedDoiTac, String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportChiTietHangBanTheoDoiTac();
        SystemConfig sysConfig = configDAO.loadConfig();
        if (sysConfig == null) sysConfig = new SystemConfig();

        List<MatHang> matHangs = matHangDAO.getAll();
        Map<String, MatHang> mhMap = new HashMap<>();
        if (matHangs != null) {
            for (MatHang mh : matHangs) {
                if (mh.getId() != null) mhMap.put(mh.getId(), mh);
                if (mh.getCode() != null) mhMap.put(mh.getCode(), mh);
                if (mh.getName() != null) mhMap.put(mh.getName(), mh);
            }
        }

        List<DonHang> allDh = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        List<ChiTietHangBanTheoDoiTacReportRow> rows = new ArrayList<>();
        String fText = filterText != null ? filterText.trim().toLowerCase() : "";

        if (allDh != null) {
            for (DonHang dh : allDh) {
                if (dh.getNgay() == null) continue;
                if (dh.getStatus() < 0) continue;

                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        MatHang mh = ct.getMatHangId() != null ? mhMap.get(ct.getMatHangId()) : null;
                        if (mh == null && ct.getMaHang() != null) mh = mhMap.get(ct.getMaHang());
                        if (mh == null && ct.getTenHang() != null) mh = mhMap.get(ct.getTenHang());

                        String doiTac = (mh != null && mh.getDoiTacKyGui() != null && !mh.getDoiTacKyGui().trim().isEmpty())
                                ? mh.getDoiTacKyGui() : "Cửa hàng";

                        if (selectedDoiTac != null && !selectedDoiTac.trim().isEmpty()
                                && !"Tất cả".equalsIgnoreCase(selectedDoiTac.trim())
                                && !selectedDoiTac.equalsIgnoreCase(doiTac)) {
                            continue;
                        }

                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : "");
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null && mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "Cái");
                        double sl = ct.getSoLuong();
                        double donGia = ct.getDonGia();
                        double giamGia = ct.getTienGiam();
                        double thanhTien = ct.getThanhTien();
                        double hoaHong = (mh != null && mh.getHoaHong() > 0) ? (mh.getHoaHong() * sl) : thanhTien;
                        double traDoiTac = thanhTien - hoaHong;

                        if (!fText.isEmpty()) {
                            if (!tenHang.toLowerCase().contains(fText) && !doiTac.toLowerCase().contains(fText) && !dvt.toLowerCase().contains(fText)) {
                                continue;
                            }
                        }

                        rows.add(new ChiTietHangBanTheoDoiTacReportRow(0, doiTac, tenHang, dvt, sl, donGia, giamGia, thanhTien, hoaHong, traDoiTac));
                    }
                }

                if (dh.getChiTietTraHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                        MatHang mh = ct.getMatHangId() != null ? mhMap.get(ct.getMatHangId()) : null;
                        if (mh == null && ct.getMaHang() != null) mh = mhMap.get(ct.getMaHang());
                        if (mh == null && ct.getTenHang() != null) mh = mhMap.get(ct.getTenHang());

                        String doiTac = (mh != null && mh.getDoiTacKyGui() != null && !mh.getDoiTacKyGui().trim().isEmpty())
                                ? mh.getDoiTacKyGui() : "Cửa hàng";

                        if (selectedDoiTac != null && !selectedDoiTac.trim().isEmpty()
                                && !"Tất cả".equalsIgnoreCase(selectedDoiTac.trim())
                                && !selectedDoiTac.equalsIgnoreCase(doiTac)) {
                            continue;
                        }

                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : "");
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null && mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "Cái");
                        double sl = -ct.getSoLuong();
                        double donGia = ct.getDonGia();
                        double giamGia = ct.getTienGiam();
                        double thanhTien = -ct.getThanhTien();
                        double hoaHong = (mh != null && mh.getHoaHong() > 0) ? (-mh.getHoaHong() * ct.getSoLuong()) : thanhTien;
                        double traDoiTac = thanhTien - hoaHong;

                        if (!fText.isEmpty()) {
                            if (!tenHang.toLowerCase().contains(fText) && !doiTac.toLowerCase().contains(fText) && !dvt.toLowerCase().contains(fText)) {
                                continue;
                            }
                        }

                        rows.add(new ChiTietHangBanTheoDoiTacReportRow(0, doiTac, tenHang, dvt, sl, donGia, giamGia, thanhTien, hoaHong, traDoiTac));
                    }
                }
            }
        }

        rows.sort((r1, r2) -> {
            int cmp = r1.getDoiTac().compareToIgnoreCase(r2.getDoiTac());
            if (cmp != 0) return cmp;
            return r1.getMatHang().compareToIgnoreCase(r2.getMatHang());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "CHI TIẾT HÀNG BÁN THEO ĐỐI TÁC");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopHoaHongTheoNVKD(Date tuNgay, Date denNgay, String khoXuat, String nvBanFilter, String nvXuatFilter, String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopHoaHongTheoNVKD();
        SystemConfig sysConfig = configDAO.loadConfig();
        if (sysConfig == null) sysConfig = new SystemConfig();

        List<MatHang> matHangs = matHangDAO.getAll();
        Map<String, MatHang> mhMap = new HashMap<>();
        if (matHangs != null) {
            for (MatHang mh : matHangs) {
                if (mh.getId() != null) mhMap.put(mh.getId(), mh);
                if (mh.getCode() != null) mhMap.put(mh.getCode(), mh);
                if (mh.getName() != null) mhMap.put(mh.getName(), mh);
            }
        }

        List<DonHang> allDh = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        Map<String, TongHopHoaHongTheoNVKDReportRow> rowMap = new LinkedHashMap<>();
        String fText = filterText != null ? filterText.trim().toLowerCase() : "";

        if (allDh != null) {
            for (DonHang dh : allDh) {
                if (dh.getNgay() == null) continue;
                if (dh.getStatus() < 0) continue;

                if (khoXuat != null && !khoXuat.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(khoXuat.trim()) && !khoXuat.equalsIgnoreCase(dh.getKhoXuatId()) && !khoXuat.equalsIgnoreCase(dh.getTenKhoXuat())) {
                    continue;
                }
                if (nvBanFilter != null && !nvBanFilter.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(nvBanFilter.trim()) && !nvBanFilter.equalsIgnoreCase(dh.getTenNhanVien())) {
                    continue;
                }
                if (nvXuatFilter != null && !nvXuatFilter.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(nvXuatFilter.trim()) && !nvXuatFilter.equalsIgnoreCase(dh.getTenNhanVienXuat())) {
                    continue;
                }

                String nvBan = dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "";

                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        MatHang mh = ct.getMatHangId() != null ? mhMap.get(ct.getMatHangId()) : null;
                        if (mh == null && ct.getMaHang() != null) mh = mhMap.get(ct.getMaHang());
                        if (mh == null && ct.getTenHang() != null) mh = mhMap.get(ct.getTenHang());

                        String maHang = ct.getMaHang() != null ? ct.getMaHang() : (mh != null && mh.getCode() != null ? mh.getCode() : "");
                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : "");
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null && mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "Cái");
                        double hoaHong = mh != null ? mh.getHoaHong() : 0.0;

                        String key = nvBan + "___" + maHang;
                        TongHopHoaHongTheoNVKDReportRow row = rowMap.get(key);
                        if (row == null) {
                            row = new TongHopHoaHongTheoNVKDReportRow(0, nvBan, maHang, tenHang, dvt, 0.0, 0.0, hoaHong, 0.0);
                            rowMap.put(key, row);
                        }
                        row.setSlBan(row.getSlBan() + ct.getSoLuong());
                        row.setTongHoaHong((row.getSlBan() - row.getSlTra()) * row.getHoaHong());
                    }
                }

                if (dh.getChiTietTraHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                        MatHang mh = ct.getMatHangId() != null ? mhMap.get(ct.getMatHangId()) : null;
                        if (mh == null && ct.getMaHang() != null) mh = mhMap.get(ct.getMaHang());
                        if (mh == null && ct.getTenHang() != null) mh = mhMap.get(ct.getTenHang());

                        String maHang = ct.getMaHang() != null ? ct.getMaHang() : (mh != null && mh.getCode() != null ? mh.getCode() : "");
                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : "");
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null && mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "Cái");
                        double hoaHong = mh != null ? mh.getHoaHong() : 0.0;

                        String key = nvBan + "___" + maHang;
                        TongHopHoaHongTheoNVKDReportRow row = rowMap.get(key);
                        if (row == null) {
                            row = new TongHopHoaHongTheoNVKDReportRow(0, nvBan, maHang, tenHang, dvt, 0.0, 0.0, hoaHong, 0.0);
                            rowMap.put(key, row);
                        }
                        row.setSlTra(row.getSlTra() + ct.getSoLuong());
                        row.setTongHoaHong((row.getSlBan() - row.getSlTra()) * row.getHoaHong());
                    }
                }
            }
        }

        List<TongHopHoaHongTheoNVKDReportRow> rows = new ArrayList<>();
        for (TongHopHoaHongTheoNVKDReportRow r : rowMap.values()) {
            if (!fText.isEmpty()) {
                if (!r.getNhanVien().toLowerCase().contains(fText) && !r.getMaHang().toLowerCase().contains(fText) && !r.getMatHang().toLowerCase().contains(fText)) {
                    continue;
                }
            }
            rows.add(r);
        }

        rows.sort((r1, r2) -> {
            int cmp = r1.getNhanVien().compareToIgnoreCase(r2.getNhanVien());
            if (cmp != 0) return cmp;
            return r1.getMaHang().compareToIgnoreCase(r2.getMaHang());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP HOA HỒNG THEO NVKD");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateChiTietBanHangTheoHoaDon(Date tuNgay, Date denNgay, String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportChiTietBanHangTheoHoaDon();
        SystemConfig sysConfig = configDAO.loadConfig();
        if (sysConfig == null) sysConfig = new SystemConfig();

        List<DonHang> allDh = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        Map<String, String> userMap = getUserMap();
        List<ChiTietBanHangTheoHoaDonReportRow> rows = new ArrayList<>();
        String fText = filterText != null ? filterText.trim().toLowerCase() : "";

        if (allDh != null) {
            for (DonHang dh : allDh) {
                if (dh.getNgay() == null) continue;
                if (dh.getStatus() < 0) continue;

                String soPhieu = dh.getName() != null ? dh.getName() : "";
                double giamGiaPhieu = dh.getTienGiamGia();
                double tongCongPhieu = dh.getTongCong();
                String gio = dh.getGioThanhToan() != null && !dh.getGioThanhToan().isEmpty() ? dh.getGioThanhToan() : "07:54";
                String thuNgan = getThuNganName(dh, userMap);
                String nvBan = dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "";

                int itemStt = 1;
                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : "";
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái";
                        double soLuong = ct.getSoLuong();
                        double donGia = ct.getDonGia();
                        double ckPt = ct.getTiLeGiamGia();
                        double thanhTien = ct.getThanhTien();

                        if (!fText.isEmpty()) {
                            if (!soPhieu.toLowerCase().contains(fText) && !tenHang.toLowerCase().contains(fText) && !thuNgan.toLowerCase().contains(fText)) {
                                continue;
                            }
                        }

                        rows.add(new ChiTietBanHangTheoHoaDonReportRow(itemStt++, soPhieu, giamGiaPhieu, tongCongPhieu, gio, thuNgan, nvBan, tenHang, dvt, soLuong, donGia, ckPt, thanhTien));
                    }
                }

                if (dh.getChiTietTraHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                        String tenHang = ct.getTenHang() != null ? ct.getTenHang() : "";
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "Cái";
                        double soLuong = -ct.getSoLuong();
                        double donGia = ct.getDonGia();
                        double ckPt = ct.getTiLeGiamGia();
                        double thanhTien = -ct.getThanhTien();

                        if (!fText.isEmpty()) {
                            if (!soPhieu.toLowerCase().contains(fText) && !tenHang.toLowerCase().contains(fText) && !thuNgan.toLowerCase().contains(fText)) {
                                continue;
                            }
                        }

                        rows.add(new ChiTietBanHangTheoHoaDonReportRow(itemStt++, soPhieu, giamGiaPhieu, tongCongPhieu, gio, thuNgan, nvBan, tenHang, dvt, soLuong, donGia, ckPt, thanhTien));
                    }
                }
            }
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "CHI TIẾT BÁN HÀNG THEO HÓA ĐƠN");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateChiTietHangKhuyenMai(Date tuNgay, Date denNgay, String thanhToanBoi, String filterText) throws JRException {
        JasperReport jasperReport = getCompiledReportChiTietHangKhuyenMai();
        SystemConfig sysConfig = configDAO.loadConfig();
        if (sysConfig == null) sysConfig = new SystemConfig();

        List<DonHang> allDh = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        List<ChiTietHangKhuyenMaiReportRow> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String fText = filterText != null ? filterText.trim().toLowerCase() : "";

        if (allDh != null) {
            for (DonHang dh : allDh) {
                if (dh.getNgay() == null) continue;
                if (dh.getStatus() < 0) continue;

                if (thanhToanBoi != null && !thanhToanBoi.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(thanhToanBoi.trim())) {
                    boolean matchPay = false;
                    if ("Tiền mặt".equalsIgnoreCase(thanhToanBoi) && dh.getTienMat() > 0) matchPay = true;
                    else if ("Chuyển khoản".equalsIgnoreCase(thanhToanBoi) && dh.getTienChuyenKhoan() > 0) matchPay = true;
                    else if ("Thẻ ATM".equalsIgnoreCase(thanhToanBoi) && dh.getTienTheATM() > 0) matchPay = true;
                    else if ("Voucher".equalsIgnoreCase(thanhToanBoi) && dh.getTienVoucher() > 0) matchPay = true;
                    if (!matchPay) continue;
                }

                String soPhieu = dh.getName() != null ? dh.getName() : "";
                String ngayStr = sdf.format(dh.getNgay());
                String tenKhach = dh.getTenKhach() != null ? dh.getTenKhach() : "Khách lẻ";

                if (dh.getTienVoucher() > 0 || (dh.getMaVoucher() != null && !dh.getMaVoucher().isEmpty())) {
                    String km = "Voucher: " + (dh.getMaVoucher() != null ? dh.getMaVoucher() : "Giảm giá");
                    rows.add(new ChiTietHangKhuyenMaiReportRow(0, soPhieu, ngayStr, tenKhach, km, 1.0, "Voucher"));
                }

                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        if (ct.getTiLeGiamGia() > 0 || ct.getTienGiam() > 0 || ct.getDonGia() == 0 || (ct.getGhiChu() != null && ct.getGhiChu().toLowerCase().contains("khuyến mại"))) {
                            String km = ct.getTenHang() != null ? ct.getTenHang() : "Hàng khuyến mại";
                            rows.add(new ChiTietHangKhuyenMaiReportRow(0, soPhieu, ngayStr, tenKhach, km, ct.getSoLuong(), ""));
                        }
                    }
                }
            }
        }

        List<ChiTietHangKhuyenMaiReportRow> filteredRows = new ArrayList<>();
        for (ChiTietHangKhuyenMaiReportRow r : rows) {
            if (!fText.isEmpty()) {
                if (!r.getSoPhieu().toLowerCase().contains(fText) && !r.getTenKhachHang().toLowerCase().contains(fText) && !r.getKhuyenMai().toLowerCase().contains(fText)) {
                    continue;
                }
            }
            filteredRows.add(r);
        }

        for (int i = 0; i < filteredRows.size(); i++) {
            filteredRows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(filteredRows);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "BÁO CÁO CHI TIẾT HÀNG KHUYẾN MẠI");
        parameters.put("p_tuNgay", tuNgay != null ? sdf.format(tuNgay) : "");
        parameters.put("p_denNgay", denNgay != null ? sdf.format(denNgay) : "");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}