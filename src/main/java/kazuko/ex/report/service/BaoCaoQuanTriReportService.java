package kazuko.ex.report.service;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.BaoCaoLaiGopTheoMatHangReportRow;
import kazuko.ex.report.model.ChiTietLaiTheoHoaDonReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoQuanTriReportService {

    private static JasperReport cachedReportTongHopLaiGopGiaVon = null;
    private static JasperReport cachedReportTongHopLaiGopGiaNhap = null;
    private static JasperReport cachedReportChiTietLaiHoaDonGiaVon = null;
    private static JasperReport cachedReportChiTietLaiHoaDonGiaNhap = null;

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoQuanTri-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportTongHopLaiGopGiaVon == null) {
                try (InputStream in = BaoCaoQuanTriReportService.class.getResourceAsStream("/reports/tong_hop_lai_gop_theo_mat_hang_gia_von.jrxml")) {
                    if (in != null) cachedReportTongHopLaiGopGiaVon = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportTongHopLaiGopGiaNhap == null) {
                try (InputStream in = BaoCaoQuanTriReportService.class.getResourceAsStream("/reports/tong_hop_lai_gop_theo_mat_hang_gia_nhap.jrxml")) {
                    if (in != null) cachedReportTongHopLaiGopGiaNhap = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportChiTietLaiHoaDonGiaVon == null) {
                try (InputStream in = BaoCaoQuanTriReportService.class.getResourceAsStream("/reports/chi_tiet_lai_theo_hoa_don_gia_von.jrxml")) {
                    if (in != null) cachedReportChiTietLaiHoaDonGiaVon = ReportTemplateCompiler.compile(in);
                }
            }
            if (cachedReportChiTietLaiHoaDonGiaNhap == null) {
                try (InputStream in = BaoCaoQuanTriReportService.class.getResourceAsStream("/reports/chi_tiet_lai_theo_hoa_don_gia_nhap.jrxml")) {
                    if (in != null) cachedReportChiTietLaiHoaDonGiaNhap = ReportTemplateCompiler.compile(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportTongHopLaiGopGiaVon() throws JRException {
        if (cachedReportTongHopLaiGopGiaVon != null) return cachedReportTongHopLaiGopGiaVon;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_lai_gop_theo_mat_hang_gia_von.jrxml")) {
            if (in == null) throw new JRException("Mau /reports/tong_hop_lai_gop_theo_mat_hang_gia_von.jrxml khong ton tai!");
            cachedReportTongHopLaiGopGiaVon = ReportTemplateCompiler.compile(in);
            return cachedReportTongHopLaiGopGiaVon;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopLaiGopGiaNhap() throws JRException {
        if (cachedReportTongHopLaiGopGiaNhap != null) return cachedReportTongHopLaiGopGiaNhap;
        try (InputStream in = getClass().getResourceAsStream("/reports/tong_hop_lai_gop_theo_mat_hang_gia_nhap.jrxml")) {
            if (in == null) throw new JRException("Mau /reports/tong_hop_lai_gop_theo_mat_hang_gia_nhap.jrxml khong ton tai!");
            cachedReportTongHopLaiGopGiaNhap = ReportTemplateCompiler.compile(in);
            return cachedReportTongHopLaiGopGiaNhap;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportChiTietLaiHoaDonGiaVon() throws JRException {
        if (cachedReportChiTietLaiHoaDonGiaVon != null) return cachedReportChiTietLaiHoaDonGiaVon;
        try (InputStream in = getClass().getResourceAsStream("/reports/chi_tiet_lai_theo_hoa_don_gia_von.jrxml")) {
            if (in == null) throw new JRException("Mau /reports/chi_tiet_lai_theo_hoa_don_gia_von.jrxml khong ton tai!");
            cachedReportChiTietLaiHoaDonGiaVon = ReportTemplateCompiler.compile(in);
            return cachedReportChiTietLaiHoaDonGiaVon;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportChiTietLaiHoaDonGiaNhap() throws JRException {
        if (cachedReportChiTietLaiHoaDonGiaNhap != null) return cachedReportChiTietLaiHoaDonGiaNhap;
        try (InputStream in = getClass().getResourceAsStream("/reports/chi_tiet_lai_theo_hoa_don_gia_nhap.jrxml")) {
            if (in == null) throw new JRException("Mau /reports/chi_tiet_lai_theo_hoa_don_gia_nhap.jrxml khong ton tai!");
            cachedReportChiTietLaiHoaDonGiaNhap = ReportTemplateCompiler.compile(in);
            return cachedReportChiTietLaiHoaDonGiaNhap;
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

    private Map<String, MatHang> getMatHangMap() {
        Map<String, MatHang> map = new HashMap<>();
        try {
            List<MatHang> list = matHangDAO.getAll();
            if (list != null) {
                for (MatHang m : list) {
                    if (m.getId() != null) map.put(m.getId(), m);
                    if (m.getCode() != null) map.put(m.getCode(), m);
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    private Map<String, String> getNhomHangMap() {
        Map<String, String> map = new HashMap<>();
        try {
            List<NhomMatHang> list = nhomMatHangDAO.getAll();
            if (list != null) {
                for (NhomMatHang n : list) {
                    if (n.getId() != null) map.put(n.getId(), n.getName());
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    public List<BaoCaoLaiGopTheoMatHangReportRow> fetchTongHopLaiGop(Date tuNgay, Date denNgay, String nhomHangId, boolean isGiaNhap) {
        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        Map<String, MatHang> productMap = getMatHangMap();
        Map<String, String> nhomMap = getNhomHangMap();

        Map<String, BaoCaoLaiGopTheoMatHangReportRow> rowMap = new LinkedHashMap<>();

        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String mhId = ct.getMatHangId();
                    MatHang mh = productMap.get(mhId);
                    if (mh == null && ct.getMaHang() != null) {
                        mh = productMap.get(ct.getMaHang());
                    }

                    String nId = mh != null ? mh.getNhomMatHangId() : null;
                    if (nhomHangId != null && !nhomHangId.trim().isEmpty() && !"all".equalsIgnoreCase(nhomHangId)) {
                        if (nId == null || !nId.equalsIgnoreCase(nhomHangId)) {
                            continue;
                        }
                    }

                    String tenNhom = mh != null && mh.getTenNhom() != null ? mh.getTenNhom() : (nId != null ? nhomMap.get(nId) : "Khac");
                    if (tenNhom == null || tenNhom.trim().isEmpty()) tenNhom = "Khac";
                    String groupHeader = "Nhom hang hoa: " + tenNhom;

                    String key = mhId != null ? mhId : (ct.getMaHang() != null ? ct.getMaHang() : ct.getTenHang());
                    BaoCaoLaiGopTheoMatHangReportRow row = rowMap.get(key);
                    if (row == null) {
                        double donGiaCost = 0.0;
                        if (mh != null) {
                            donGiaCost = isGiaNhap ? mh.getGiaNhap() : (mh.getGiaVon() > 0 ? mh.getGiaVon() : mh.getGiaNhap());
                        }
                        row = new BaoCaoLaiGopTheoMatHangReportRow(
                                0,
                                groupHeader,
                                ct.getMaHang() != null ? ct.getMaHang() : (mh != null ? mh.getCode() : ""),
                                ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : ""),
                                ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null ? mh.getTenDonViTinh() : ""),
                                0.0,
                                0.0,
                                ct.getDonGia(),
                                0.0,
                                0.0,
                                0.0,
                                donGiaCost,
                                0.0,
                                0.0
                        );
                        rowMap.put(key, row);
                    }

                    double sl = ct.getSoLuong() > 0 ? ct.getSoLuong() : 1.0;
                    double tt = ct.getThanhTien() > 0 ? ct.getThanhTien() : (sl * ct.getDonGia());
                    double giamGia = (ct.getTiLeGiamGia() > 0 ? (sl * ct.getDonGia() * ct.getTiLeGiamGia() / 100.0) : 0.0);

                    row.setSoLuong(row.getSoLuong() + sl);
                    row.setGiaBanThanhTien(row.getGiaBanThanhTien() + tt);
                    row.setGiaBanGiamGia(row.getGiaBanGiamGia() + giamGia);
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    String mhId = ct.getMatHangId();
                    MatHang mh = productMap.get(mhId);
                    if (mh == null && ct.getMaHang() != null) {
                        mh = productMap.get(ct.getMaHang());
                    }

                    String nId = mh != null ? mh.getNhomMatHangId() : null;
                    if (nhomHangId != null && !nhomHangId.trim().isEmpty() && !"all".equalsIgnoreCase(nhomHangId)) {
                        if (nId == null || !nId.equalsIgnoreCase(nhomHangId)) {
                            continue;
                        }
                    }

                    String tenNhom = mh != null && mh.getTenNhom() != null ? mh.getTenNhom() : (nId != null ? nhomMap.get(nId) : "Khac");
                    if (tenNhom == null || tenNhom.trim().isEmpty()) tenNhom = "Khac";
                    String groupHeader = "Nhom hang hoa: " + tenNhom;

                    String key = mhId != null ? mhId : (ct.getMaHang() != null ? ct.getMaHang() : ct.getTenHang());
                    BaoCaoLaiGopTheoMatHangReportRow row = rowMap.get(key);
                    if (row == null) {
                        double donGiaCost = 0.0;
                        if (mh != null) {
                            donGiaCost = isGiaNhap ? mh.getGiaNhap() : (mh.getGiaVon() > 0 ? mh.getGiaVon() : mh.getGiaNhap());
                        }
                        row = new BaoCaoLaiGopTheoMatHangReportRow(
                                0,
                                groupHeader,
                                ct.getMaHang() != null ? ct.getMaHang() : (mh != null ? mh.getCode() : ""),
                                ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : ""),
                                ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null ? mh.getTenDonViTinh() : ""),
                                0.0,
                                0.0,
                                ct.getDonGia(),
                                0.0,
                                0.0,
                                0.0,
                                donGiaCost,
                                0.0,
                                0.0
                        );
                        rowMap.put(key, row);
                    }

                    double slTra = ct.getSoLuong() > 0 ? ct.getSoLuong() : 1.0;
                    row.setDoiTra(row.getDoiTra() + slTra);
                }
            }
        }

        List<BaoCaoLaiGopTheoMatHangReportRow> rows = new ArrayList<>(rowMap.values());
        for (BaoCaoLaiGopTheoMatHangReportRow r : rows) {
            double netQty = r.getSoLuong() - r.getDoiTra();
            double tongCongBan = r.getGiaBanThanhTien() - r.getGiaBanGiamGia();
            r.setGiaBanTongCong(tongCongBan);

            if (netQty > 0) {
                r.setGiaBanDonGia(tongCongBan / netQty);
            }

            double giaTriVon = netQty * r.getGiaVonDonGia();
            r.setGiaVonGiaTri(giaTriVon);
            r.setLoiNhuan(tongCongBan - giaTriVon);
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupName().compareToIgnoreCase(r2.getGroupName());
            if (cmp != 0) return cmp;
            return r1.getMaHang().compareToIgnoreCase(r2.getMaHang());
        });

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        return rows;
    }

    public List<ChiTietLaiTheoHoaDonReportRow> fetchChiTietLaiHoaDon(Date tuNgay, Date denNgay, boolean isGiaNhap) {
        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        Map<String, MatHang> productMap = getMatHangMap();

        List<ChiTietLaiTheoHoaDonReportRow> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        int stt = 1;
        for (DonHang dh : allHoaDon) {
            if (dh.getStatus() < 0) continue;

            String groupHeader = "Số hóa đơn: " + (dh.getName() != null ? dh.getName() : "HĐ") +
                    "  -  Ngày: " + (dh.getNgay() != null ? sdf.format(dh.getNgay()) : "") +
                    (dh.getTenKhach() != null && !dh.getTenKhach().trim().isEmpty() ? "  -  KH: " + dh.getTenKhach() : "");

            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    String mhId = ct.getMatHangId();
                    MatHang mh = productMap.get(mhId);
                    if (mh == null && ct.getMaHang() != null) {
                        mh = productMap.get(ct.getMaHang());
                    }

                    double donGiaCost = 0.0;
                    if (mh != null) {
                        donGiaCost = isGiaNhap ? mh.getGiaNhap() : (mh.getGiaVon() > 0 ? mh.getGiaVon() : mh.getGiaNhap());
                    }

                    double sl = ct.getSoLuong() > 0 ? ct.getSoLuong() : 1.0;
                    double tt = ct.getThanhTien() > 0 ? ct.getThanhTien() : (sl * ct.getDonGia());
                    double giamGia = (ct.getTiLeGiamGia() > 0 ? (sl * ct.getDonGia() * ct.getTiLeGiamGia() / 100.0) : 0.0);
                    double tongCong = tt - giamGia;
                    double giaTriVon = sl * donGiaCost;
                    double loiNhuan = tongCong - giaTriVon;

                    ChiTietLaiTheoHoaDonReportRow row = new ChiTietLaiTheoHoaDonReportRow(
                            stt++,
                            groupHeader,
                            ct.getMaHang() != null ? ct.getMaHang() : (mh != null ? mh.getCode() : ""),
                            ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : ""),
                            ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null ? mh.getTenDonViTinh() : ""),
                            sl,
                            0.0,
                            ct.getDonGia(),
                            tt,
                            giamGia,
                            tongCong,
                            donGiaCost,
                            giaTriVon,
                            loiNhuan
                    );
                    rows.add(row);
                }
            }

            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    String mhId = ct.getMatHangId();
                    MatHang mh = productMap.get(mhId);
                    if (mh == null && ct.getMaHang() != null) {
                        mh = productMap.get(ct.getMaHang());
                    }

                    double donGiaCost = 0.0;
                    if (mh != null) {
                        donGiaCost = isGiaNhap ? mh.getGiaNhap() : (mh.getGiaVon() > 0 ? mh.getGiaVon() : mh.getGiaNhap());
                    }

                    double slTra = ct.getSoLuong() > 0 ? ct.getSoLuong() : 1.0;
                    double tt = -(ct.getThanhTien() > 0 ? ct.getThanhTien() : (slTra * ct.getDonGia()));
                    double giamGia = 0.0;
                    double tongCong = tt;
                    double giaTriVon = -slTra * donGiaCost;
                    double loiNhuan = tongCong - giaTriVon;

                    ChiTietLaiTheoHoaDonReportRow row = new ChiTietLaiTheoHoaDonReportRow(
                            stt++,
                            groupHeader,
                            ct.getMaHang() != null ? ct.getMaHang() : (mh != null ? mh.getCode() : ""),
                            ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : ""),
                            ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null ? mh.getTenDonViTinh() : ""),
                            0.0,
                            slTra,
                            ct.getDonGia(),
                            tt,
                            giamGia,
                            tongCong,
                            donGiaCost,
                            giaTriVon,
                            loiNhuan
                    );
                    rows.add(row);
                }
            }
        }

        // Giữ danh sách rỗng; Jasper hiển thị khung báo cáo không có dòng chi tiết.
        return rows;
    }

    private double calculateTongGiamGiaBill(Date tuNgay, Date denNgay) {
        List<DonHang> allHoaDon = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        double totalGiamGiaBill = 0.0;
        if (allHoaDon != null) {
            for (DonHang dh : allHoaDon) {
                if (dh.getStatus() >= 0) {
                    totalGiamGiaBill += dh.getTienGiamGia();
                }
            }
        }
        return totalGiamGiaBill;
    }

    private String getSubTitleDate(Date tuNgay, Date denNgay) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String sTu = tuNgay != null ? sdf.format(tuNgay) : "Đầu kỳ";
        String sDen = denNgay != null ? sdf.format(denNgay) : sdf.format(new Date());
        return "Từ ngày: " + sTu + "   Đến ngày: " + sDen;
    }

    private String getCurrentDateStr() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        return String.format("Ngày %02d tháng %02d năm %d", day, month, year);
    }

    public JasperPrint generateTongHopLaiGopGiaVon(Date tuNgay, Date denNgay, String nhomHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopLaiGopGiaVon();
        SystemConfig sysConfig = configDAO.loadConfig();
        List<BaoCaoLaiGopTheoMatHangReportRow> rows = fetchTongHopLaiGop(tuNgay, denNgay, nhomHangId, false);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ VỐN)");
        parameters.put("p_subTitleDate", getSubTitleDate(tuNgay, denNgay));
        parameters.put("p_currentDateStr", getCurrentDateStr());
        parameters.put("p_giamGiaTongBill", calculateTongGiamGiaBill(tuNgay, denNgay));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateTongHopLaiGopGiaNhap(Date tuNgay, Date denNgay, String nhomHangId) throws JRException {
        JasperReport jasperReport = getCompiledReportTongHopLaiGopGiaNhap();
        SystemConfig sysConfig = configDAO.loadConfig();
        List<BaoCaoLaiGopTheoMatHangReportRow> rows = fetchTongHopLaiGop(tuNgay, denNgay, nhomHangId, true);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ NHẬP)");
        parameters.put("p_subTitleDate", getSubTitleDate(tuNgay, denNgay));
        parameters.put("p_currentDateStr", getCurrentDateStr());
        parameters.put("p_giamGiaTongBill", calculateTongGiamGiaBill(tuNgay, denNgay));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateChiTietLaiHoaDonGiaVon(Date tuNgay, Date denNgay) throws JRException {
        JasperReport jasperReport = getCompiledReportChiTietLaiHoaDonGiaVon();
        SystemConfig sysConfig = configDAO.loadConfig();
        List<ChiTietLaiTheoHoaDonReportRow> rows = fetchChiTietLaiHoaDon(tuNgay, denNgay, false);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ VỐN)");
        parameters.put("p_subTitleDate", getSubTitleDate(tuNgay, denNgay));
        parameters.put("p_currentDateStr", getCurrentDateStr());
        parameters.put("p_giamGiaTongBill", calculateTongGiamGiaBill(tuNgay, denNgay));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateChiTietLaiHoaDonGiaNhap(Date tuNgay, Date denNgay) throws JRException {
        JasperReport jasperReport = getCompiledReportChiTietLaiHoaDonGiaNhap();
        SystemConfig sysConfig = configDAO.loadConfig();
        List<ChiTietLaiTheoHoaDonReportRow> rows = fetchChiTietLaiHoaDon(tuNgay, denNgay, true);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ NHẬP)");
        parameters.put("p_subTitleDate", getSubTitleDate(tuNgay, denNgay));
        parameters.put("p_currentDateStr", getCurrentDateStr());
        parameters.put("p_giamGiaTongBill", calculateTongGiamGiaBill(tuNgay, denNgay));

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}
