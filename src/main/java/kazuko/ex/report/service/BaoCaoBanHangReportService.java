package kazuko.ex.report.service;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.MatHang;
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

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

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
}