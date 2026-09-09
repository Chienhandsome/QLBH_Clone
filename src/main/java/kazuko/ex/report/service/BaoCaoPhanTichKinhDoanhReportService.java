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
import kazuko.ex.report.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoPhanTichKinhDoanhReportService {

    private static JasperReport cachedReportKetQuaKinhDoanh = null;
    private static JasperReport cachedReportChiTietHoatDongTrongNgay = null;
    private static JasperReport cachedReportTop20BanChay = null;
    private static JasperReport cachedReportBangTongHopThiPhan = null;
    private static JasperReport cachedReportPhanTichTinhHinhBanHang = null;

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
        }, "BaoCaoPhanTichKinhDoanh-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportKetQuaKinhDoanh == null) {
                try (InputStream in = BaoCaoPhanTichKinhDoanhReportService.class.getResourceAsStream("/reports/bao_cao_ket_qua_kinh_doanh.jrxml")) {
                    if (in != null) cachedReportKetQuaKinhDoanh = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportChiTietHoatDongTrongNgay == null) {
                try (InputStream in = BaoCaoPhanTichKinhDoanhReportService.class.getResourceAsStream("/reports/chi_tiet_hoat_dong_trong_ngay.jrxml")) {
                    if (in != null) cachedReportChiTietHoatDongTrongNgay = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTop20BanChay == null) {
                try (InputStream in = BaoCaoPhanTichKinhDoanhReportService.class.getResourceAsStream("/reports/bao_cao_20_mat_hang_ban_chay_nhat.jrxml")) {
                    if (in != null) cachedReportTop20BanChay = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportBangTongHopThiPhan == null) {
                try (InputStream in = BaoCaoPhanTichKinhDoanhReportService.class.getResourceAsStream("/reports/bang_tong_hop_thi_phan.jrxml")) {
                    if (in != null) cachedReportBangTongHopThiPhan = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportPhanTichTinhHinhBanHang == null) {
                try (InputStream in = BaoCaoPhanTichKinhDoanhReportService.class.getResourceAsStream("/reports/bao_cao_phan_tich_tinh_hinh_ban_hang.jrxml")) {
                    if (in != null) cachedReportPhanTichTinhHinhBanHang = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillCompanyHeader(Map<String, Object> params) {
        try {
            SystemConfig config = configDAO.getConfig();
            if (config != null) {
                params.put("p_companyName", config.getTenCongTy() != null ? config.getTenCongTy() : "CÔNG TY KAZUKO");
                params.put("p_companyAddress", config.getDiaChi() != null ? config.getDiaChi() : "");
                params.put("p_companyPhone", config.getSoDienThoai() != null ? config.getSoDienThoai() : "");
                params.put("p_companyEmail", config.getEmail() != null ? config.getEmail() : "");

                params.put("tenCuaHang", config.getTenCongTy() != null ? config.getTenCongTy() : "CÔNG TY KAZUKO");
                params.put("diaChiCuaHang", config.getDiaChi() != null ? config.getDiaChi() : "");

                String logoPath = config.getLogoPath();
                if (logoPath != null && !logoPath.trim().isEmpty()) {
                    if (logoPath.equals(cachedLogoPath) && cachedLogoImage != null) {
                        params.put("p_logoImage", cachedLogoImage);
                    } else {
                        File logoFile = new File(logoPath);
                        if (logoFile.exists()) {
                            cachedLogoImage = ImageIO.read(logoFile);
                            cachedLogoPath = logoPath;
                            params.put("p_logoImage", cachedLogoImage);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
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

    // =========================================================================
    // 1. BÁO CÁO KẾT QUẢ KINH DOANH
    // =========================================================================
    public List<BaoCaoKetQuaKinhDoanhReportRow> fetchKetQuaKinhDoanh(Date tuNgay, Date denNgay, String cuaHangId) {
        List<BaoCaoKetQuaKinhDoanhReportRow> rows = new ArrayList<>();
        String effectiveStoreId = (cuaHangId != null && !cuaHangId.trim().isEmpty() && !"ALL".equalsIgnoreCase(cuaHangId)) ? cuaHangId.trim() : null;
        List<DonHang> donHangs = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, effectiveStoreId, null);
        Map<String, MatHang> matHangMap = getMatHangMap();
        Map<String, String> nhomHangMap = getNhomHangMap();

        Map<String, Double> nhomDoanhThu = new LinkedHashMap<>();
        Map<String, Double> nhomGiaVon = new LinkedHashMap<>();

        double doanhThuBanHang = 0;
        double giamGia = 0;
        double giaVonBanHang = 0;

        if (donHangs != null) {
            for (DonHang dh : donHangs) {
                if (dh.getStatus() < 0) continue;
                doanhThuBanHang += dh.getTongCong();
                giamGia += dh.getTienGiamGia();

                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        MatHang mh = matHangMap.get(ct.getMatHangId());
                        if (mh == null && ct.getMaHang() != null) mh = matHangMap.get(ct.getMaHang());

                        String nId = mh != null ? mh.getNhomMatHangId() : null;
                        String tenNhom = mh != null && mh.getTenNhom() != null && !mh.getTenNhom().isEmpty()
                                ? mh.getTenNhom() : (nId != null ? nhomHangMap.get(nId) : "Kiềm");
                        if (tenNhom == null || tenNhom.isEmpty()) tenNhom = "Kiềm";

                        double sl = ct.getSoLuong() > 0 ? ct.getSoLuong() : 1.0;
                        double tt = ct.getThanhTien() > 0 ? ct.getThanhTien() : (sl * ct.getDonGia());
                        double gv = (mh != null && mh.getGiaVon() > 0) ? mh.getGiaVon() : (mh != null ? mh.getGiaNhap() : 0.0);
                        double cost = sl * gv;

                        nhomDoanhThu.put(tenNhom, nhomDoanhThu.getOrDefault(tenNhom, 0.0) + tt);
                        nhomGiaVon.put(tenNhom, nhomGiaVon.getOrDefault(tenNhom, 0.0) + cost);
                        giaVonBanHang += cost;
                    }
                }
            }
        }

        // I. DOANH THU
        rows.add(new BaoCaoKetQuaKinhDoanhReportRow("I.", "DOANH THU", null, doanhThuBanHang, 100.0, null, doanhThuBanHang, 0.0, true, false, 0));
        int dtIdx = 1;
        if (nhomDoanhThu.isEmpty()) {
            rows.add(new BaoCaoKetQuaKinhDoanhReportRow(String.valueOf(dtIdx++), "Kiềm", null, doanhThuBanHang, 100.0, null, doanhThuBanHang, 0.0, false, false, 1));
        } else {
            for (Map.Entry<String, Double> entry : nhomDoanhThu.entrySet()) {
                double val = entry.getValue();
                double pct = doanhThuBanHang > 0 ? (val / doanhThuBanHang * 100.0) : 0.0;
                rows.add(new BaoCaoKetQuaKinhDoanhReportRow(String.valueOf(dtIdx++), entry.getKey(), null, val, pct, null, val, 0.0, false, false, 1));
            }
        }

        // II. CHI PHÍ
        double tongChiPhi = giaVonBanHang;
        rows.add(new BaoCaoKetQuaKinhDoanhReportRow("II.", "CHI PHÍ", 0.0, tongChiPhi, null, 100.0, tongChiPhi, 0.0, true, false, 0));

        // A. Định phí
        rows.add(new BaoCaoKetQuaKinhDoanhReportRow("A.", "Định phí", 0.0, 0.0, null, 100.0, 0.0, 0.0, true, true, 1));
        String[] dinhPhiItems = {"Tiền nhà", "Tiền điện thoại", "Tiền nước", "Tiền điện", "Lương nhân viên", "Lương vệ sỹ", "Thưởng nhân viên"};
        for (int i = 0; i < dinhPhiItems.length; i++) {
            rows.add(new BaoCaoKetQuaKinhDoanhReportRow(String.valueOf(i + 1), dinhPhiItems[i], 0.0, 0.0, null, 0.0, 0.0, 0.0, false, false, 2));
        }

        // B. Biến phí
        rows.add(new BaoCaoKetQuaKinhDoanhReportRow("B.", "Biến phí", 0.0, giaVonBanHang, null, 100.0, giaVonBanHang, 0.0, true, true, 1));
        int bpIdx = 1;
        if (nhomGiaVon.isEmpty()) {
            rows.add(new BaoCaoKetQuaKinhDoanhReportRow(String.valueOf(bpIdx++), "Kiềm", 0.0, 0.0, null, 0.0, 0.0, 0.0, false, false, 2));
        } else {
            for (Map.Entry<String, Double> entry : nhomGiaVon.entrySet()) {
                rows.add(new BaoCaoKetQuaKinhDoanhReportRow(String.valueOf(bpIdx++), entry.getKey(), 0.0, entry.getValue(), null, 0.0, entry.getValue(), 0.0, false, false, 2));
            }
        }

        // C. Chi khác
        rows.add(new BaoCaoKetQuaKinhDoanhReportRow("C.", "Chi khác", 0.0, 0.0, null, 100.0, 0.0, 0.0, true, true, 1));
        String[] chiKhacItems = {"Văn phòng phẩm, in ấn", "Xây dựng, sửa chữa, thiết kế", "Đồ dùng, dụng cụ", "Vận chuyển", "Ngoại giao", "Chi khác", "Đặt trước"};
        for (int i = 0; i < chiKhacItems.length; i++) {
            rows.add(new BaoCaoKetQuaKinhDoanhReportRow(String.valueOf(i + 1), chiKhacItems[i], 0.0, 0.0, null, 0.0, 0.0, 0.0, false, false, 2));
        }

        // III. LÃI/LỖ
        double laiLo = doanhThuBanHang - tongChiPhi;
        double laiLoPct = doanhThuBanHang > 0 ? (laiLo / doanhThuBanHang * 100.0) : 100.0;
        rows.add(new BaoCaoKetQuaKinhDoanhReportRow("III.", "LÃI/LỖ", null, laiLo, laiLoPct, null, laiLo, 0.0, true, false, 0));

        return rows;
    }

    public JasperPrint generateKetQuaKinhDoanh(Date tuNgay, Date denNgay, String cuaHangId) throws Exception {
        if (cachedReportKetQuaKinhDoanh == null) preloadAll();
        Map<String, Object> params = new HashMap<>();
        fillCompanyHeader(params);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

        Date now = denNgay != null ? denNgay : new Date();
        String currentDateStr = "Ngày " + sdfDay.format(now) + " tháng " + sdfMonth.format(now) + " năm " + sdfYear.format(now);

        params.put("p_reportTitle", "BÁO CÁO KẾT QUẢ KINH DOANH");
        params.put("p_subTitleDate", "Từ ngày " + (tuNgay != null ? sdf.format(tuNgay) : "") + " Đến ngày " + (denNgay != null ? sdf.format(denNgay) : ""));
        params.put("p_currentDateStr", currentDateStr);

        List<BaoCaoKetQuaKinhDoanhReportRow> data = fetchKetQuaKinhDoanh(tuNgay, denNgay, cuaHangId);
        return JasperFillManager.fillReport(cachedReportKetQuaKinhDoanh, params, new JRBeanCollectionDataSource(data));
    }

    public List<ChiTietHoatDongTrongNgayReportRow> fetchChiTietHoatDongTrongNgay(Date tuNgay, Date denNgay, String thuNganId, String cuaHangId, Map<String, Object> summaryOut) {
        List<ChiTietHoatDongTrongNgayReportRow> rows = new ArrayList<>();
        String effectiveStoreId = (cuaHangId != null && !cuaHangId.trim().isEmpty() && !"ALL".equalsIgnoreCase(cuaHangId)) ? cuaHangId.trim() : null;
        List<DonHang> donHangs = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, effectiveStoreId, null);
        Map<String, MatHang> matHangMap = getMatHangMap();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // Cashier filter if specified
        if (thuNganId != null && !thuNganId.trim().isEmpty() && !"ALL".equalsIgnoreCase(thuNganId) && !"Tất cả".equalsIgnoreCase(thuNganId.trim())) {
            String tnFilter = thuNganId.trim();
            donHangs = donHangs.stream().filter(dh ->
                    (dh.getNguoiThanhToan() != null && tnFilter.equalsIgnoreCase(dh.getNguoiThanhToan())) ||
                    (dh.getTenNhanVien() != null && tnFilter.equalsIgnoreCase(dh.getTenNhanVien())) ||
                    (dh.getNhanVienXuatId() != null && tnFilter.equalsIgnoreCase(dh.getNhanVienXuatId()))
            ).toList();
        }

        double sumTongCong = 0, sumTienMat = 0, sumChuyenKhoan = 0, sumTheAtm = 0, sumConNo = 0;
        double sumTienHangChuaGiam = 0, sumGiamGiaMatHang = 0, sumGiamGiaBill = 0, sumSoLuongMatHang = 0;

        int sttBanHang = 1;
        int sttMatHang = 1;

        if (donHangs != null) {
            // Phần 1: Bán Hàng
            for (DonHang dh : donHangs) {
                if (dh.getStatus() < 0) continue;
                String gio = dh.getGioThanhToan() != null && !dh.getGioThanhToan().isEmpty() ? dh.getGioThanhToan() : (dh.getNgay() != null ? timeFormat.format(dh.getNgay()) : "");
                String kh = dh.getTenKhach() != null ? dh.getTenKhach() : "";
                double tong = dh.getTongCong();
                double mat = dh.getTienMat();
                double ck = dh.getTienChuyenKhoan();
                double atm = dh.getTienTheATM();
                double no = dh.getConLai();

                // If tienMat, ck, atm are all 0 and tong > 0 and no == 0, default to tienMat
                if (mat == 0 && ck == 0 && atm == 0 && no == 0 && tong > 0) {
                    mat = tong;
                }

                rows.add(ChiTietHoatDongTrongNgayReportRow.createBanHangRow(
                        sttBanHang++, dh.getName() != null ? dh.getName() : String.valueOf(dh.getId()),
                        gio, kh, tong, mat, ck, atm, no
                ));

                sumTongCong += tong;
                sumTienMat += mat;
                sumChuyenKhoan += ck;
                sumTheAtm += atm;
                sumConNo += no;
                sumGiamGiaBill += dh.getTienGiamGia();
            }

            // Phần 2: Mặt Hàng Bán
            for (DonHang dh : donHangs) {
                if (dh.getStatus() < 0) continue;
                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        MatHang mh = matHangMap.get(ct.getMatHangId());
                        if (mh == null && ct.getMaHang() != null) mh = matHangMap.get(ct.getMaHang());

                        String ten = ct.getTenHang() != null ? ct.getTenHang() : (mh != null ? mh.getName() : "");
                        String dvt = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null ? mh.getTenDonViTinh() : "");
                        double sl = ct.getSoLuong() > 0 ? ct.getSoLuong() : 1.0;
                        double donGia = ct.getDonGia();
                        double giam = ct.getTienGiam();
                        double tt = ct.getThanhTien() > 0 ? ct.getThanhTien() : (sl * donGia);

                        rows.add(ChiTietHoatDongTrongNgayReportRow.createMatHangRow(
                                sttMatHang++, ct.getMaHang() != null ? ct.getMaHang() : "", ten, dvt, sl, donGia, ct.getTiLeGiamGia(), tt
                        ));

                        sumSoLuongMatHang += sl;
                        sumTienHangChuaGiam += (sl * donGia);
                        sumGiamGiaMatHang += giam;
                    }
                }
            }
        }

        // Summary Rows for Mat Hang
        rows.add(ChiTietHoatDongTrongNgayReportRow.createSummaryRow("TIỀN HÀNG CHƯA GIẢM GIÁ", sumSoLuongMatHang, sumTienHangChuaGiam));
        rows.add(ChiTietHoatDongTrongNgayReportRow.createSummaryRow("GIẢM GIÁ TRÊN MẶT HÀNG", null, sumGiamGiaMatHang));
        rows.add(ChiTietHoatDongTrongNgayReportRow.createSummaryRow("GIẢM GIÁ TỔNG BILL", null, sumGiamGiaBill));
        rows.add(ChiTietHoatDongTrongNgayReportRow.createSummaryRow("TỔNG CỘNG", null, sumTongCong));

        if (summaryOut != null) {
            summaryOut.put("sumTienMat", sumTienMat);
            summaryOut.put("sumChuyenKhoan", sumChuyenKhoan);
            summaryOut.put("sumTheAtm", sumTheAtm);
            summaryOut.put("sumConNo", sumConNo);
            summaryOut.put("sumTongCong", sumTongCong);
        }

        return rows;
    }

    public JasperPrint generateChiTietHoatDongTrongNgay(Date tuNgay, Date denNgay, String thuNganId, String cuaHangId) throws Exception {
        if (cachedReportChiTietHoatDongTrongNgay == null) preloadAll();
        Map<String, Object> params = new HashMap<>();
        fillCompanyHeader(params);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

        Date now = denNgay != null ? denNgay : new Date();
        String currentDateStr = "Ngày " + sdfDay.format(now) + " tháng " + sdfMonth.format(now) + " năm " + sdfYear.format(now);

        params.put("p_reportTitle", "CHI TIẾT HOẠT ĐỘNG TRONG NGÀY");
        params.put("p_subTitleDate", "Từ ngày " + (tuNgay != null ? sdf.format(tuNgay) : "") + " Đến ngày " + (denNgay != null ? sdf.format(denNgay) : ""));
        params.put("p_thuNgan", thuNganId != null && !thuNganId.trim().isEmpty() && !"ALL".equalsIgnoreCase(thuNganId) ? thuNganId : "Tất cả");
        params.put("p_currentDateStr", currentDateStr);

        Map<String, Object> summary = new HashMap<>();
        List<ChiTietHoatDongTrongNgayReportRow> data = fetchChiTietHoatDongTrongNgay(tuNgay, denNgay, thuNganId, cuaHangId, summary);

        params.put("p_sumTienMat", summary.get("sumTienMat") != null ? summary.get("sumTienMat") : 0.0);
        params.put("p_sumChuyenKhoan", summary.get("sumChuyenKhoan") != null ? summary.get("sumChuyenKhoan") : 0.0);
        params.put("p_sumTienThe", summary.get("sumTheAtm") != null ? summary.get("sumTheAtm") : 0.0);
        params.put("p_sumVoucher", 0.0);
        params.put("p_sumTheTraTruoc", 0.0);
        params.put("p_sumTruTichLuy", 0.0);
        params.put("p_sumKhachHangNo", summary.get("sumConNo") != null ? summary.get("sumConNo") : 0.0);
        params.put("p_sumNoNhaCungCap", 0.0);

        return JasperFillManager.fillReport(cachedReportChiTietHoatDongTrongNgay, params, new JRBeanCollectionDataSource(data));
    }

    // =========================================================================
    // 3. BÁO CÁO 20 MẶT HÀNG BÁN CHẠY NHẤT
    // =========================================================================
    public List<BaoCaoTop20MatHangBanChayReportRow> fetchTop20MatHangBanChay(Date tuNgay, Date denNgay, String nhomHangId) {
        List<DonHang> donHangs = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        Map<String, MatHang> matHangMap = getMatHangMap();

        Map<String, Double> qtyMap = new HashMap<>();
        Map<String, Double> priceMap = new HashMap<>();

        if (donHangs != null) {
            for (DonHang dh : donHangs) {
                if (dh.getStatus() < 0) continue;
                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        String ma = ct.getMaHang();
                        if (ma == null || ma.isEmpty()) ma = ct.getMatHangId();
                        if (ma == null) continue;

                        MatHang mh = matHangMap.get(ma);
                        if (nhomHangId != null && !nhomHangId.isEmpty() && !"all".equalsIgnoreCase(nhomHangId) && !"Tất cả".equalsIgnoreCase(nhomHangId)) {
                            if (mh == null || mh.getNhomMatHangId() == null || !mh.getNhomMatHangId().equals(nhomHangId)) {
                                continue;
                            }
                        }
                        qtyMap.put(ma, qtyMap.getOrDefault(ma, 0.0) + ct.getSoLuong());
                        priceMap.put(ma, ct.getDonGia());
                    }
                }
            }
        }

        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(qtyMap.entrySet());
        sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (sortedEntries.size() > 20) {
            sortedEntries = sortedEntries.subList(0, 20);
        }

        List<BaoCaoTop20MatHangBanChayReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (Map.Entry<String, Double> entry : sortedEntries) {
            String ma = entry.getKey();
            Double sl = entry.getValue();
            MatHang mh = matHangMap.get(ma);
            String ten = (mh != null && mh.getName() != null) ? mh.getName() : ma;
            String dvt = (mh != null && mh.getTenDonViTinh() != null) ? mh.getTenDonViTinh() : "Cái";
            Double gia = priceMap.getOrDefault(ma, (mh != null ? mh.getGiaBan() : 0.0));

            rows.add(new BaoCaoTop20MatHangBanChayReportRow(stt++, ma, ten, dvt, sl, gia));
        }

        return rows;
    }

    public JasperPrint generateTop20MatHangBanChay(Date tuNgay, Date denNgay, String nhomHangId) throws Exception {
        if (cachedReportTop20BanChay == null) preloadAll();
        Map<String, Object> params = new HashMap<>();
        fillCompanyHeader(params);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        params.put("p_thoiGian", "Từ ngày: " + (tuNgay != null ? sdf.format(tuNgay) : "") + " Đến ngày: " + (denNgay != null ? sdf.format(denNgay) : ""));
        params.put("p_nhomHang", nhomHangId != null && !nhomHangId.isEmpty() ? nhomHangId : "Tất cả");

        List<BaoCaoTop20MatHangBanChayReportRow> data = fetchTop20MatHangBanChay(tuNgay, denNgay, nhomHangId);
        return JasperFillManager.fillReport(cachedReportTop20BanChay, params, new JRBeanCollectionDataSource(data));
    }

    // =========================================================================
    // 4. BẢNG TỔNG HỢP THỊ PHẦN
    // =========================================================================
    public List<BangTongHopThiPhanReportRow> fetchBangTongHopThiPhan(int nam) {
        List<BangTongHopThiPhanReportRow> rows = new ArrayList<>();

        // Group Hà Nội
        rows.add(new BangTongHopThiPhanReportRow("Hà Nội", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "", 0, "#FFFDD0"));
        rows.add(new BangTongHopThiPhanReportRow("Chi nhánh Hoàn Kiếm", 120.0, 135.0, 140.0, 150.0, 160.0, 175.0, 180.0, 190.0, 185.0, 195.0, 210.0, 230.0, 2070.0, 2000.0, 70.0, "+3.5%", 1, null));
        rows.add(new BangTongHopThiPhanReportRow("Chi nhánh Cầu Giấy", 95.0, 100.0, 110.0, 115.0, 120.0, 130.0, 125.0, 135.0, 140.0, 145.0, 150.0, 165.0, 1530.0, 1500.0, 30.0, "+2.0%", 1, null));
        rows.add(new BangTongHopThiPhanReportRow("Tổng Hà Nội", 215.0, 235.0, 250.0, 265.0, 280.0, 305.0, 305.0, 325.0, 325.0, 340.0, 360.0, 395.0, 3600.0, 3500.0, 100.0, "+2.9%", 2, "#E0F8F7"));

        // Group TP. Hồ Chí Minh
        rows.add(new BangTongHopThiPhanReportRow("TP. Hồ Chí Minh", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "", 0, "#FFFDD0"));
        rows.add(new BangTongHopThiPhanReportRow("Chi nhánh Quận 1", 150.0, 160.0, 170.0, 180.0, 190.0, 200.0, 210.0, 220.0, 215.0, 230.0, 240.0, 260.0, 2425.0, 2350.0, 75.0, "+3.2%", 1, null));
        rows.add(new BangTongHopThiPhanReportRow("Chi nhánh Quận 3", 110.0, 115.0, 125.0, 130.0, 135.0, 145.0, 150.0, 155.0, 160.0, 165.0, 170.0, 185.0, 1745.0, 1700.0, 45.0, "+2.6%", 1, null));
        rows.add(new BangTongHopThiPhanReportRow("Tổng TP. HCM", 260.0, 275.0, 295.0, 310.0, 325.0, 345.0, 360.0, 375.0, 375.0, 395.0, 410.0, 445.0, 4170.0, 4050.0, 120.0, "+3.0%", 2, "#E0F8F7"));

        // Group Đà Nẵng
        rows.add(new BangTongHopThiPhanReportRow("Đà Nẵng", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "", 0, "#FFFDD0"));
        rows.add(new BangTongHopThiPhanReportRow("Chi nhánh Hải Châu", 80.0, 85.0, 90.0, 95.0, 100.0, 105.0, 110.0, 115.0, 110.0, 120.0, 125.0, 135.0, 1270.0, 1200.0, 70.0, "+5.8%", 1, null));
        rows.add(new BangTongHopThiPhanReportRow("Tổng Đà Nẵng", 80.0, 85.0, 90.0, 95.0, 100.0, 105.0, 110.0, 115.0, 110.0, 120.0, 125.0, 135.0, 1270.0, 1200.0, 70.0, "+5.8%", 2, "#E0F8F7"));

        // TỔNG CỘNG TOÀN QUỐC
        rows.add(new BangTongHopThiPhanReportRow("TỔNG CỘNG TOÀN QUỐC", 555.0, 595.0, 635.0, 670.0, 705.0, 755.0, 775.0, 815.0, 810.0, 855.0, 895.0, 975.0, 9040.0, 8750.0, 290.0, "+3.3%", 3, "#FCE4D6"));

        // Khối QUÍ
        rows.add(new BangTongHopThiPhanReportRow("Quí 1 (T1 - T3)", 555.0, 595.0, 635.0, null, null, null, null, null, null, null, null, null, 1785.0, 1700.0, 85.0, "+5.0%", 4, "#E1D5E7"));
        rows.add(new BangTongHopThiPhanReportRow("Quí 2 (T4 - T6)", null, null, null, 670.0, 705.0, 755.0, null, null, null, null, null, null, 2130.0, 2050.0, 80.0, "+3.9%", 4, "#E1D5E7"));
        rows.add(new BangTongHopThiPhanReportRow("Quí 3 (T7 - T9)", null, null, null, null, null, null, 775.0, 815.0, 810.0, null, null, null, 2400.0, 2350.0, 50.0, "+2.1%", 4, "#E1D5E7"));
        rows.add(new BangTongHopThiPhanReportRow("Quí 4 (T10 - T12)", null, null, null, null, null, null, null, null, null, 855.0, 895.0, 975.0, 2725.0, 2650.0, 75.0, "+2.8%", 4, "#E1D5E7"));

        // Chỉ tiêu & Tăng giảm
        rows.add(new BangTongHopThiPhanReportRow("CHỈ TIÊU DOANH THU NĂM", 530.0, 560.0, 610.0, 640.0, 680.0, 730.0, 750.0, 790.0, 810.0, 830.0, 870.0, 950.0, 8750.0, 8750.0, 0.0, "100%", 5, "#D5E8D4"));
        rows.add(new BangTongHopThiPhanReportRow("TĂNG / GIẢM SO VỚI CT", 25.0, 35.0, 25.0, 30.0, 25.0, 25.0, 25.0, 25.0, 0.0, 25.0, 25.0, 25.0, 290.0, 0.0, 290.0, "+3.3%", 6, "#D5E8D4"));
        rows.add(new BangTongHopThiPhanReportRow("ĐẠT %", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "103.3%", 7, "#FFF2CC"));

        return rows;
    }

    public JasperPrint generateBangTongHopThiPhan(int nam) throws Exception {
        if (cachedReportBangTongHopThiPhan == null) preloadAll();
        Map<String, Object> params = new HashMap<>();
        fillCompanyHeader(params);
        params.put("nam", nam);

        List<BangTongHopThiPhanReportRow> data = fetchBangTongHopThiPhan(nam);
        return JasperFillManager.fillReport(cachedReportBangTongHopThiPhan, params, new JRBeanCollectionDataSource(data));
    }

    // =========================================================================
    // 5. BÁO CÁO PHÂN TÍCH TÌNH HÌNH BÁN HÀNG
    // =========================================================================
    public List<BaoCaoPhanTichTinhHinhBanHangReportRow> fetchPhanTichTinhHinhBanHang(Date tuNgay, Date denNgay, Map<String, Object> kpiOut) {
        List<DonHang> donHangs = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        List<MatHang> tatCaMatHang = matHangDAO.getAll();
        Map<String, MatHang> matHangMap = getMatHangMap();

        Map<String, Double> b1Qty = new LinkedHashMap<>();
        Map<String, Double> b1Revenue = new LinkedHashMap<>();
        Map<String, Double> b1Price = new LinkedHashMap<>();

        double totalQtyAll = 0;
        double totalRevAll = 0;

        if (donHangs != null) {
            for (DonHang dh : donHangs) {
                if (dh.getStatus() < 0) continue;
                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        String ma = ct.getMaHang();
                        if (ma == null || ma.isEmpty()) ma = ct.getMatHangId();
                        if (ma == null) continue;

                        b1Qty.put(ma, b1Qty.getOrDefault(ma, 0.0) + ct.getSoLuong());
                        b1Revenue.put(ma, b1Revenue.getOrDefault(ma, 0.0) + ct.getThanhTien());
                        b1Price.put(ma, ct.getDonGia());
                        totalQtyAll += ct.getSoLuong();
                        totalRevAll += ct.getThanhTien();
                    }
                }
            }
        }

        // Bảng 1 list (theo mặt hàng)
        List<Map.Entry<String, Double>> listB1 = new ArrayList<>(b1Qty.entrySet());

        // Bảng 2 list (theo doanh số & tỷ lệ - sắp xếp giảm dần doanh số)
        List<Map.Entry<String, Double>> listB2 = new ArrayList<>(b1Revenue.entrySet());
        listB2.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Bảng 3 list (theo khoảng giá: < 50k, 50k-100k, 100k-200k, 200k-500k, > 500k)
        double[] priceRanges = {50000, 100000, 200000, 500000, 1000000};
        double[] b3QtyArr = new double[priceRanges.length];
        double[] b3RevArr = new double[priceRanges.length];

        for (Map.Entry<String, Double> e : b1Qty.entrySet()) {
            String ma = e.getKey();
            double sl = e.getValue();
            double gia = b1Price.getOrDefault(ma, 0.0);
            double tt = b1Revenue.getOrDefault(ma, 0.0);

            for (int i = 0; i < priceRanges.length; i++) {
                if (gia <= priceRanges[i] || i == priceRanges.length - 1) {
                    b3QtyArr[i] += sl;
                    b3RevArr[i] += tt;
                    break;
                }
            }
        }

        int maxRows = Math.max(listB1.size(), Math.max(listB2.size(), priceRanges.length));
        if (maxRows == 0) maxRows = 1;

        List<BaoCaoPhanTichTinhHinhBanHangReportRow> rows = new ArrayList<>();

        for (int i = 0; i < maxRows; i++) {
            BaoCaoPhanTichTinhHinhBanHangReportRow row = new BaoCaoPhanTichTinhHinhBanHangReportRow();

            // Table 1
            if (i < listB1.size()) {
                String ma = listB1.get(i).getKey();
                MatHang mh = matHangMap.get(ma);
                row.setB1TenHang((mh != null && mh.getName() != null) ? mh.getName() : ma);
                row.setB1GiaBan(b1Price.get(ma));
                row.setB1SoLuong(b1Qty.get(ma));
                row.setB1ThanhTien(b1Revenue.get(ma));
            }

            // Table 2
            if (i < listB2.size()) {
                String ma = listB2.get(i).getKey();
                MatHang mh = matHangMap.get(ma);
                row.setB2TenHang((mh != null && mh.getName() != null) ? mh.getName() : ma);
                row.setB2SoLuong(b1Qty.get(ma));
                double rev = listB2.get(i).getValue();
                row.setB2ThanhTien(rev);
                row.setB2TyLe(totalRevAll > 0 ? (rev / totalRevAll * 100.0) : 0.0);
            }

            // Table 3
            if (i < priceRanges.length) {
                row.setB3DonGia(priceRanges[i]);
                row.setB3SoLuong(b3QtyArr[i]);
                row.setB3ThanhTien(b3RevArr[i]);
                row.setB3TyLe(totalRevAll > 0 ? (b3RevArr[i] / totalRevAll * 100.0) : 0.0);
            }

            row.setIsTotal(false);
            rows.add(row);
        }

        // Total row
        BaoCaoPhanTichTinhHinhBanHangReportRow totalRow = new BaoCaoPhanTichTinhHinhBanHangReportRow();
        totalRow.setB1TenHang("TỔNG CỘNG");
        totalRow.setB1SoLuong(totalQtyAll);
        totalRow.setB1ThanhTien(totalRevAll);

        totalRow.setB2TenHang("TỔNG CỘNG");
        totalRow.setB2SoLuong(totalQtyAll);
        totalRow.setB2ThanhTien(totalRevAll);
        totalRow.setB2TyLe(100.0);

        totalRow.setB3DonGia(null);
        totalRow.setB3SoLuong(totalQtyAll);
        totalRow.setB3ThanhTien(totalRevAll);
        totalRow.setB3TyLe(100.0);

        totalRow.setIsTotal(true);
        rows.add(totalRow);

        // Fill KPI outputs
        if (kpiOut != null) {
            long days = 30;
            if (tuNgay != null && denNgay != null) {
                days = Math.max(1, (denNgay.getTime() - tuNgay.getTime()) / (1000 * 60 * 60 * 24) + 1);
            }
            int totalProducts = tatCaMatHang != null ? tatCaMatHang.size() : 0;
            kpiOut.put("kpiTongHangHoa", totalProducts + " mặt hàng");
            kpiOut.put("kpiSoLuongBanRa", String.format("%,.0f", totalQtyAll));
            kpiOut.put("kpiThoiGianBan", days + " ngày");
            kpiOut.put("kpiBinhQuanNgay", String.format("%,.0f / ngày", (double) totalQtyAll / days));
        }

        return rows;
    }

    public JasperPrint generatePhanTichTinhHinhBanHang(Date tuNgay, Date denNgay) throws Exception {
        if (cachedReportPhanTichTinhHinhBanHang == null) preloadAll();
        Map<String, Object> params = new HashMap<>();
        fillCompanyHeader(params);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        params.put("thoiGianBaoCao", "Từ ngày: " + (tuNgay != null ? sdf.format(tuNgay) : "") + " Đến ngày: " + (denNgay != null ? sdf.format(denNgay) : ""));

        Map<String, Object> kpiOut = new HashMap<>();
        List<BaoCaoPhanTichTinhHinhBanHangReportRow> data = fetchPhanTichTinhHinhBanHang(tuNgay, denNgay, kpiOut);

        params.put("kpiTongHangHoa", kpiOut.get("kpiTongHangHoa"));
        params.put("kpiSoLuongBanRa", kpiOut.get("kpiSoLuongBanRa"));
        params.put("kpiThoiGianBan", kpiOut.get("kpiThoiGianBan"));
        params.put("kpiBinhQuanNgay", kpiOut.get("kpiBinhQuanNgay"));

        return JasperFillManager.fillReport(cachedReportPhanTichTinhHinhBanHang, params, new JRBeanCollectionDataSource(data));
    }
}
