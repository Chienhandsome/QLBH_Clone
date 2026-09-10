package kazuko.ex.report.service;

import kazuko.ex.dao.BaseDAO;
import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.congno.CongNoKhachHangItem;
import kazuko.ex.model.congno.CongNoNhaCungCapItem;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.BaoCaoCongNoKhachHangReportRow;
import kazuko.ex.report.model.BaoCaoCongNoNhaCungCapReportRow;
import kazuko.ex.report.model.TongHopCongNoKhachHangReportRow;
import kazuko.ex.report.model.TongHopCongNoNhaCungCapReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class BaoCaoCongNoReportService extends BaseDAO {

    private static JasperReport cachedReportBaoCaoNCC = null;
    private static JasperReport cachedReportTongHopNCC = null;
    private static JasperReport cachedReportBaoCaoKhach = null;
    private static JasperReport cachedReportTongHopKhach = null;
    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final CongNoKhachHangDAO congNoKhachDAO = new CongNoKhachHangDAO();
    private final CongNoNhaCungCapDAO congNoNCCDAO = new CongNoNhaCungCapDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        // Preload in background thread
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoCongNo-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportBaoCaoNCC == null) {
                try (InputStream in = BaoCaoCongNoReportService.class.getResourceAsStream("/reports/bao_cao_cong_no_nha_cung_cap.jrxml")) {
                    if (in != null) cachedReportBaoCaoNCC = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopNCC == null) {
                try (InputStream in = BaoCaoCongNoReportService.class.getResourceAsStream("/reports/tong_hop_cong_no_nha_cung_cap.jrxml")) {
                    if (in != null) cachedReportTongHopNCC = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportBaoCaoKhach == null) {
                try (InputStream in = BaoCaoCongNoReportService.class.getResourceAsStream("/reports/bao_cao_cong_no_khach_hang.jrxml")) {
                    if (in != null) cachedReportBaoCaoKhach = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopKhach == null) {
                try (InputStream in = BaoCaoCongNoReportService.class.getResourceAsStream("/reports/tong_hop_cong_no_khach_hang.jrxml")) {
                    if (in != null) cachedReportTongHopKhach = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception e) {
            System.err.println("Preload BaoCaoCongNo reports failed: " + e.getMessage());
        }
    }

    private synchronized JasperReport getOrCompileReport(String resourcePath, JasperReport cached) throws JRException {
        if (cached != null) return cached;
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new JRException("Không tìm thấy report template: " + resourcePath);
            return JasperCompileManager.compileReport(in);
        } catch (Exception e) {
            throw new JRException("Lỗi compile report template: " + resourcePath, e);
        }
    }

    private Map<String, Object> createBaseParameters() {
        Map<String, Object> params = new HashMap<>();
        SystemConfig config = configDAO.getConfig();
        if (config != null) {
            params.put("p_companyName", config.getTenCongTy() != null && !config.getTenCongTy().trim().isEmpty() ? config.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
            params.put("p_companyAddress", config.getDiaChi() != null && !config.getDiaChi().trim().isEmpty() ? config.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
            params.put("p_companyPhone", config.getSoDienThoai() != null && !config.getSoDienThoai().trim().isEmpty() ? config.getSoDienThoai() : "(08) 6264 5788");
            params.put("p_companyEmail", config.getEmail() != null && !config.getEmail().trim().isEmpty() ? config.getEmail() : "info@ngocchauau.vn");

            String logoPath = config.getLogoPath();
            if (logoPath != null && !logoPath.trim().isEmpty()) {
                if (cachedLogoImage != null && logoPath.equals(cachedLogoPath)) {
                    params.put("p_logoImage", cachedLogoImage);
                } else {
                    try {
                        File logoFile = new File(logoPath);
                        if (logoFile.exists()) {
                            cachedLogoImage = ImageIO.read(logoFile);
                            cachedLogoPath = logoPath;
                            params.put("p_logoImage", cachedLogoImage);
                        }
                    } catch (Exception ignored) {}
                }
            }
        } else {
            params.put("p_companyName", "Công ty TNHH Ngọc Châu Âu");
            params.put("p_companyAddress", "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
            params.put("p_companyPhone", "(08) 6264 5788");
            params.put("p_companyEmail", "info@ngocchauau.vn");
        }

        if (params.get("p_logoImage") == null) {
            try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
                if (is != null) {
                    params.put("p_logoImage", ImageIO.read(is));
                }
            } catch (Exception ignored) {}
        }

        Calendar cal = Calendar.getInstance();
        params.put("p_printDate", String.format("Ngày %02d tháng %02d năm %d",
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));

        return params;
    }

    private Date getStartOfDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    // 1. BÁO CÁO CÔNG NỢ NHÀ CUNG CẤP (Từ ngày -> Đến ngày)
    public JasperPrint generateBaoCaoCongNoNhaCungCap(Date tuNgay, Date denNgay, String nhomNCCId) throws JRException {
        Date start = getStartOfDay(tuNgay);
        Date end = getEndOfDay(denNgay);

        List<BaoCaoCongNoNhaCungCapReportRow> rows = new ArrayList<>();

        // Map nccId -> [noDau, mua, thanhToan, noCuoi]
        Map<String, double[]> nccAmountMap = new HashMap<>();

        // a. Nợ đầu: Đơn nhập LOAI=1 trước start
        String sqlNoDauDon = "SELECT d.DNHACUNGCAPID, SUM(d.TONGCONG) AS TONGMUA, SUM(d.TIENTHANHTOAN) AS TONGDATT " +
                "FROM TDONHANG d " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DNHACUNGCAPID IS NOT NULL AND (d.LOAI = 1 OR d.NAME LIKE 'PN%') " +
                (start != null ? "AND d.NGAY < ? " : "AND 1=0 ") +
                "GROUP BY d.DNHACUNGCAPID";

        // b. Nợ đầu: Phiếu chi trước start
        String sqlNoDauChi = "SELECT pc.DNHACUNGCAPID, SUM(pc.SOTIEN) AS TONGCHI " +
                "FROM TPHIEUCHI pc " +
                "WHERE (pc.STATUS IS NULL OR pc.STATUS >= 0) AND pc.DNHACUNGCAPID IS NOT NULL " +
                "AND (pc.KHONGTHAYDOICONGNO IS NULL OR pc.KHONGTHAYDOICONGNO = 0) " +
                (start != null ? "AND pc.NGAY < ? " : "AND 1=0 ") +
                "GROUP BY pc.DNHACUNGCAPID";

        // c. Trong kỳ: Mua & Thanh toán từ đơn nhập [start, end]
        String sqlTrongKyDon = "SELECT d.DNHACUNGCAPID, SUM(d.TONGCONG) AS TONGMUA, SUM(d.TIENTHANHTOAN) AS TONGDATT " +
                "FROM TDONHANG d " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DNHACUNGCAPID IS NOT NULL AND (d.LOAI = 1 OR d.NAME LIKE 'PN%') " +
                (start != null ? "AND d.NGAY >= ? " : "") +
                (end != null ? "AND d.NGAY <= ? " : "") +
                "GROUP BY d.DNHACUNGCAPID";

        // d. Trong kỳ: Phiếu chi [start, end]
        String sqlTrongKyChi = "SELECT pc.DNHACUNGCAPID, SUM(pc.SOTIEN) AS TONGCHI " +
                "FROM TPHIEUCHI pc " +
                "WHERE (pc.STATUS IS NULL OR pc.STATUS >= 0) AND pc.DNHACUNGCAPID IS NOT NULL " +
                "AND (pc.KHONGTHAYDOICONGNO IS NULL OR pc.KHONGTHAYDOICONGNO = 0) " +
                (start != null ? "AND pc.NGAY >= ? " : "") +
                (end != null ? "AND pc.NGAY <= ? " : "") +
                "GROUP BY pc.DNHACUNGCAPID";

        try (Connection conn = getConnection()) {
            if (start != null) {
                try (PreparedStatement ps = conn.prepareStatement(sqlNoDauDon)) {
                    ps.setTimestamp(1, new Timestamp(start.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String id = rs.getString("DNHACUNGCAPID");
                            if (id != null) {
                                double[] arr = nccAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                                arr[0] += (rs.getDouble("TONGMUA") - rs.getDouble("TONGDATT"));
                            }
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlNoDauChi)) {
                    ps.setTimestamp(1, new Timestamp(start.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String id = rs.getString("DNHACUNGCAPID");
                            if (id != null) {
                                double[] arr = nccAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                                arr[0] -= rs.getDouble("TONGCHI");
                            }
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTrongKyDon)) {
                int pIdx = 1;
                if (start != null) ps.setTimestamp(pIdx++, new Timestamp(start.getTime()));
                if (end != null) ps.setTimestamp(pIdx++, new Timestamp(end.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("DNHACUNGCAPID");
                        if (id != null) {
                            double[] arr = nccAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                            arr[1] += rs.getDouble("TONGMUA");
                            arr[2] += rs.getDouble("TONGDATT");
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTrongKyChi)) {
                int pIdx = 1;
                if (start != null) ps.setTimestamp(pIdx++, new Timestamp(start.getTime()));
                if (end != null) ps.setTimestamp(pIdx++, new Timestamp(end.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("DNHACUNGCAPID");
                        if (id != null) {
                            double[] arr = nccAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                            arr[2] += rs.getDouble("TONGCHI");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính toán Báo cáo công nợ NCC: " + e.getMessage());
        }

        // Lấy danh sách NCC
        String sqlNCC = "SELECT n.ID, n.MANHACUNGCAP AS MANCC, n.NAME AS TENNCC, n.DNHOMNHACUNGCAPID, nh.NAME AS TENNHOM, n.DIENTHOAI " +
                "FROM DNHACUNGCAP n " +
                "LEFT JOIN DNHOMNHACUNGCAP nh ON n.DNHOMNHACUNGCAPID = nh.ID " +
                "WHERE (n.STATUS IS NULL OR n.STATUS >= 0) " +
                "ORDER BY nh.NAME ASC, n.NAME ASC";

        int stt = 1;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlNCC);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("ID");
                String ma = rs.getString("MANCC");
                String ten = rs.getString("TENNCC");
                String nhomId = rs.getString("DNHOMNHACUNGCAPID");
                String tenNhom = rs.getString("TENNHOM");
                String dt = rs.getString("DIENTHOAI");

                if (nhomNCCId != null && !nhomNCCId.trim().isEmpty() && !nhomNCCId.equalsIgnoreCase("Tất cả")) {
                    if (nhomId == null || !nhomId.trim().equalsIgnoreCase(nhomNCCId.trim())) {
                        continue;
                    }
                }

                double[] arr = nccAmountMap.getOrDefault(id != null ? id.trim() : "", new double[]{0.0, 0.0, 0.0, 0.0});
                double noDau = arr[0];
                double mua = arr[1];
                double thanhToan = arr[2];
                double noCuoi = noDau + mua - thanhToan;

                rows.add(new BaoCaoCongNoNhaCungCapReportRow(
                        stt++,
                        tenNhom != null && !tenNhom.trim().isEmpty() ? tenNhom : "Khác",
                        ma, ten, dt, noDau, mua, thanhToan, noCuoi
                ));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load NCC: " + e.getMessage());
        }

        Map<String, Object> params = createBaseParameters();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String strTuNgay = start != null ? sdf.format(start) : "01/01/1970";
        String strDenNgay = end != null ? sdf.format(end) : sdf.format(new Date());
        params.put("p_dateRangeText", "Từ ngày " + strTuNgay + " Đến ngày " + strDenNgay);
        params.put("p_reportTitle", "BÁO CÁO CÔNG NỢ NHÀ CUNG CẤP");

        cachedReportBaoCaoNCC = getOrCompileReport("/reports/bao_cao_cong_no_nha_cung_cap.jrxml", cachedReportBaoCaoNCC);
        return JasperFillManager.fillReport(cachedReportBaoCaoNCC, params, new JRBeanCollectionDataSource(rows));
    }

    // 2. TỔNG HỢP CÔNG NỢ NHÀ CUNG CẤP
    public JasperPrint generateTongHopCongNoNhaCungCap(String nhomNCCId) throws JRException {
        List<CongNoNhaCungCapItem> nccList = congNoNCCDAO.getDanhSachCongNoNCC(
                (nhomNCCId != null && !nhomNCCId.equalsIgnoreCase("Tất cả")) ? nhomNCCId : null,
                null, false
        );

        List<TongHopCongNoNhaCungCapReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (CongNoNhaCungCapItem item : nccList) {
            rows.add(new TongHopCongNoNhaCungCapReportRow(
                    stt++,
                    item.getTenNhomNCC() != null && !item.getTenNhomNCC().trim().isEmpty() ? item.getTenNhomNCC() : "Khác",
                    item.getMaNCC(),
                    item.getTenNCC(),
                    item.getDiaChi(),
                    item.getDienThoai(),
                    item.getEmail(),
                    item.getConNo()
            ));
        }

        Map<String, Object> params = createBaseParameters();
        params.put("p_reportTitle", "TỔNG HỢP CÔNG NỢ NHÀ CUNG CẤP");
        params.put("p_filterText", (nhomNCCId != null && !nhomNCCId.trim().isEmpty() && !nhomNCCId.equalsIgnoreCase("Tất cả"))
                ? "Nhóm nhà cung cấp: " + nhomNCCId : "Nhóm nhà cung cấp: Tất cả");

        cachedReportTongHopNCC = getOrCompileReport("/reports/tong_hop_cong_no_nha_cung_cap.jrxml", cachedReportTongHopNCC);
        return JasperFillManager.fillReport(cachedReportTongHopNCC, params, new JRBeanCollectionDataSource(rows));
    }

    // 3. ĐỐI CHIẾU CÔNG NỢ CỦA MỘT KHÁCH HÀNG (Từ ngày -> Đến ngày)
    public JasperPrint generateBaoCaoCongNoKhachHang(Date tuNgay, Date denNgay, String khachHangId) throws JRException {
        Date start = getStartOfDay(tuNgay);
        Date end = getEndOfDay(denNgay);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<BaoCaoCongNoKhachHangReportRow> transactionRows = new ArrayList<>();

        KhachHang khachHang = khachHangId != null && !khachHangId.trim().isEmpty()
                ? new KhachHangDAO().findById(khachHangId.trim()) : null;

        double noDauKy = 0.0;
        double tongTangNoTrongKy = 0.0;
        double tongGiamNoTrongKy = 0.0;
        DebtTotals cumulativeTotals = new DebtTotals();
        List<CustomerDebtEvent> events = new ArrayList<>();

        if (khachHang != null) {
            String sqlOrders = "SELECT d.ID, d.NAME AS SOPHIEU, d.NGAY, d.TIMECREATED, d.TONGCONG, " +
                    "d.TIENTHANHTOAN, d.TIENGIAMGIA, d.TIENTHUE, d.PHIVANCHUYEN, " +
                    "d.NOTE, d.DIENGIAI FROM TDONHANG d " +
                    "WHERE d.DKHACHHANGID = ? AND d.LOAI = 0 " +
                    "AND (d.DATHANHTOAN = 30 OR d.STATUS = 1) AND (d.STATUS IS NULL OR d.STATUS >= 0) " +
                    (start != null ? "AND d.NGAY >= ? " : "") +
                    (end != null ? "AND d.NGAY <= ? " : "") +
                    "ORDER BY d.NGAY, d.TIMECREATED, d.ID";
            String sqlDetails = "SELECT c.TDONHANGID, m.CODE AS MAHANG, m.NAME AS TENHANG, " +
                    "dv.NAME AS DVT, c.SLXUAT, c.SLNHAP, c.SLXUATCHUAQUYDOI, c.SLNHAPCHUAQUYDOI, " +
                    "c.TDONHANGTRAID, c.DONGIA, c.THANHTIEN, c.NOTE, c.TIMECREATED, c.ID AS CHITIETID " +
                    "FROM TDONHANGCHITIET c " +
                    "LEFT JOIN DMATHANG m ON c.DMATHANGID = m.ID " +
                    "LEFT JOIN DDONVITINH dv ON COALESCE(c.DDONVITINHID, m.DDONVITINHID) = dv.ID " +
                    "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                    "WHERE d.DKHACHHANGID = ? AND d.LOAI = 0 " +
                    "AND (d.DATHANHTOAN = 30 OR d.STATUS = 1) " +
                    "AND (d.STATUS IS NULL OR d.STATUS >= 0) AND (c.STATUS IS NULL OR c.STATUS >= 0) " +
                    "AND COALESCE(c.XUATVATTU, 0) = 0 " +
                    (start != null ? "AND d.NGAY >= ? " : "") +
                    (end != null ? "AND d.NGAY <= ? " : "") +
                    "ORDER BY d.NGAY, d.TIMECREATED, c.TIMECREATED, c.ID";
            String sqlCash = "SELECT tc.NAME AS SOPHIEU, tc.NGAY, tc.TIMECREATED, tc.DIENGIAI, " +
                    "tc.THU, tc.CHI, tc.NOTE FROM TTHUCHI tc WHERE tc.DKHACHHANGID = ? " +
                    "AND (tc.STATUS IS NULL OR tc.STATUS >= 0) " +
                    "AND (tc.KHONGTHAYDOICONGNO IS NULL OR tc.KHONGTHAYDOICONGNO = 0) " +
                    (start != null ? "AND tc.NGAY >= ? " : "") +
                    (end != null ? "AND tc.NGAY <= ? " : "") +
                    "ORDER BY tc.NGAY, tc.TIMECREATED, tc.NAME";

            try (Connection conn = getConnection()) {
                DebtTotals openingTotals = start != null
                        ? loadCustomerDebtTotals(conn, khachHang.getId(), start, false)
                        : new DebtTotals();
                cumulativeTotals = loadCustomerDebtTotals(conn, khachHang.getId(), end, true);
                noDauKy = openingTotals.getDebt();

                Map<String, List<CustomerDebtDetail>> detailMap = new HashMap<>();
                try (PreparedStatement ps = conn.prepareStatement(sqlDetails)) {
                    int idx = 1;
                    ps.setString(idx++, khachHang.getId());
                    if (start != null) ps.setTimestamp(idx++, new Timestamp(start.getTime()));
                    if (end != null) ps.setTimestamp(idx++, new Timestamp(end.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            CustomerDebtDetail detail = new CustomerDebtDetail();
                            detail.maHang = rs.getString("MAHANG");
                            detail.tenHang = rs.getString("TENHANG");
                            detail.donViTinh = rs.getString("DVT");
                            double slXuat = firstNonZero(rs.getDouble("SLXUATCHUAQUYDOI"), rs.getDouble("SLXUAT"));
                            double slNhap = firstNonZero(rs.getDouble("SLNHAPCHUAQUYDOI"), rs.getDouble("SLNHAP"));
                            detail.donGia = rs.getDouble("DONGIA");
                            detail.thanhTien = Math.abs(rs.getDouble("THANHTIEN"));
                            detail.ghiChu = rs.getString("NOTE");
                            detail.isReturn = isReturnDetail(
                                    slNhap, rs.getString("TDONHANGTRAID"), detail.ghiChu);
                            detail.soLuong = Math.abs(detail.isReturn ? slNhap : slXuat);
                            if (detail.soLuong < 0.0001) {
                                detail.soLuong = Math.abs(firstNonZero(slXuat, slNhap));
                            }
                            detailMap.computeIfAbsent(rs.getString("TDONHANGID"), key -> new ArrayList<>()).add(detail);
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlOrders)) {
                    int idx = 1;
                    ps.setString(idx++, khachHang.getId());
                    if (start != null) ps.setTimestamp(idx++, new Timestamp(start.getTime()));
                    if (end != null) ps.setTimestamp(idx++, new Timestamp(end.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            CustomerDebtEvent event = new CustomerDebtEvent();
                            event.type = "DONHANG";
                            event.documentId = rs.getString("ID");
                            event.documentNo = rs.getString("SOPHIEU");
                            event.date = rs.getTimestamp("NGAY");
                            event.createdAt = rs.getTimestamp("TIMECREATED");
                            event.orderTotal = rs.getDouble("TONGCONG");
                            event.payment = rs.getDouble("TIENTHANHTOAN");
                            event.discount = rs.getDouble("TIENGIAMGIA");
                            event.tax = rs.getDouble("TIENTHUE");
                            event.shipping = rs.getDouble("PHIVANCHUYEN");
                            event.note = rs.getString("NOTE");
                            event.description = rs.getString("DIENGIAI");
                            event.details = detailMap.getOrDefault(event.documentId, Collections.emptyList());
                            events.add(event);
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlCash)) {
                    int idx = 1;
                    ps.setString(idx++, khachHang.getId());
                    if (start != null) ps.setTimestamp(idx++, new Timestamp(start.getTime()));
                    if (end != null) ps.setTimestamp(idx++, new Timestamp(end.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            CustomerDebtEvent event = new CustomerDebtEvent();
                            event.type = "PHIEUTHU";
                            event.documentNo = rs.getString("SOPHIEU");
                            event.date = rs.getTimestamp("NGAY");
                            event.createdAt = rs.getTimestamp("TIMECREATED");
                            event.payment = Math.max(0.0, rs.getDouble("THU"));
                            event.orderTotal = Math.max(0.0, rs.getDouble("CHI"));
                            event.description = firstNonBlank(rs.getString("DIENGIAI"), "Thanh toán công nợ");
                            event.note = rs.getString("NOTE");
                            events.add(event);
                        }
                    }
                }
            } catch (Exception e) {
                throw new JRException("Không thể tải dữ liệu đối chiếu công nợ khách hàng", e);
            }
        }

        events.sort(Comparator
                .comparing((CustomerDebtEvent e) -> e.date, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(e -> e.createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(e -> e.type));

        double runningDebt = noDauKy;
        for (CustomerDebtEvent event : events) {
            if ("DONHANG".equals(event.type)) {
                double returnCredit = event.details.stream()
                        .filter(detail -> detail.isReturn)
                        .mapToDouble(detail -> Math.abs(detail.thanhTien))
                        .sum();
                double outgoingAmount = event.details.stream()
                        .filter(detail -> !detail.isReturn)
                        .mapToDouble(detail -> Math.abs(detail.thanhTien))
                        .sum();
                double orderIncrease = calculateOrderIncrease(
                        event.orderTotal, returnCredit, outgoingAmount,
                        event.discount, event.tax, event.shipping);
                double immediatePayment = Math.max(0.0, event.payment);
                tongTangNoTrongKy += orderIncrease;
                tongGiamNoTrongKy += immediatePayment + returnCredit;

                if (event.details.isEmpty()) {
                    runningDebt += orderIncrease - immediatePayment;
                    transactionRows.add(new BaoCaoCongNoKhachHangReportRow(
                            formatDate(event.date, sdf), firstNonBlank(event.description, safe(event.documentNo)),
                            null, null, null, nullableAmount(orderIncrease), nullableAmount(immediatePayment),
                            runningDebt, safe(event.note), "DONHANG"));
                    continue;
                }

                boolean firstDetail = true;
                boolean hasReturn = returnCredit > 0.0001;
                boolean hasOutgoingDetail = event.details.stream().anyMatch(detail -> !detail.isReturn);
                boolean orderIncreaseApplied = false;
                boolean immediatePaymentApplied = false;
                for (CustomerDebtDetail detail : event.details) {
                    boolean applyOrderIncrease = !orderIncreaseApplied
                            && (!detail.isReturn || !hasOutgoingDetail);
                    double rowIncrease = applyOrderIncrease ? orderIncrease : 0.0;
                    if (applyOrderIncrease) orderIncreaseApplied = true;

                    boolean applyImmediatePayment = !immediatePaymentApplied
                            && (applyOrderIncrease || !hasOutgoingDetail);
                    double rowPayment = (applyImmediatePayment ? immediatePayment : 0.0)
                            + (detail.isReturn ? Math.abs(detail.thanhTien) : 0.0);
                    if (applyImmediatePayment) immediatePaymentApplied = true;
                    runningDebt += rowIncrease - rowPayment;
                    String note;
                    if (detail.isReturn) {
                        note = formatReturnNote(detail.ghiChu);
                    } else if (applyOrderIncrease && hasReturn) {
                        note = formatReplacementNote(event.note, event.discount);
                    } else if (firstDetail) {
                        note = firstNonBlank(event.note, detail.ghiChu);
                    } else {
                        note = safe(detail.ghiChu);
                    }
                    transactionRows.add(new BaoCaoCongNoKhachHangReportRow(
                            firstDetail ? formatDate(event.date, sdf) : "",
                            firstNonBlank(detail.tenHang, safe(event.documentNo)),
                            nullableAmount(detail.soLuong), detail.donGia,
                            detail.thanhTien, applyOrderIncrease ? rowIncrease : null,
                            nullableAmount(rowPayment), runningDebt, safe(note),
                            detail.isReturn ? "DOITRA" : "HANGHOA"));
                    firstDetail = false;
                }
            } else {
                runningDebt += event.orderTotal - event.payment;
                tongTangNoTrongKy += event.orderTotal;
                tongGiamNoTrongKy += event.payment;
                transactionRows.add(new BaoCaoCongNoKhachHangReportRow(
                        formatDate(event.date, sdf), event.description,
                        null, null, null, nullableAmount(event.orderTotal), nullableAmount(event.payment),
                        runningDebt, firstNonBlank(event.note, safe(event.documentNo)), "PHIEUTHU"));
            }
        }

        List<BaoCaoCongNoKhachHangReportRow> rows = new ArrayList<>();
        rows.add(new BaoCaoCongNoKhachHangReportRow(
                "", "Tổng", null, null, null,
                cumulativeTotals.orderValue, cumulativeTotals.payment,
                cumulativeTotals.getDebt(), "", "TONG"));
        rows.add(new BaoCaoCongNoKhachHangReportRow(
                "", openingDebtLabel(start, sdf), null, null, null,
                noDauKy, null, noDauKy, "", "NODAU"));
        rows.addAll(transactionRows);

        Map<String, Object> params = createBaseParameters();
        String strTuNgay = start != null ? sdf.format(start) : "";
        String strDenNgay = end != null ? sdf.format(end) : sdf.format(new Date());
        params.put("p_dateRangeText", "Từ ngày " + strTuNgay + " đến ngày " + strDenNgay);
        params.put("p_customerText", khachHang != null
                ? formatCustomerName(khachHang) : "Chưa chọn khách hàng");
        params.put("p_customerContact", khachHang != null
                ? buildCustomerContact(khachHang) : "");
        params.put("p_reportTitle", "ĐỐI CHIẾU CÔNG NỢ KHÁCH HÀNG");
        params.put("p_openingDebt", noDauKy);
        params.put("p_periodPurchases", tongTangNoTrongKy);
        params.put("p_periodPayments", tongGiamNoTrongKy);
        params.put("p_closingDebt", runningDebt);

        cachedReportBaoCaoKhach = getOrCompileReport("/reports/bao_cao_cong_no_khach_hang.jrxml", cachedReportBaoCaoKhach);
        return JasperFillManager.fillReport(cachedReportBaoCaoKhach, params, new JRBeanCollectionDataSource(rows));
    }

    private static String formatDate(Date date, SimpleDateFormat sdf) {
        return date != null ? sdf.format(date) : "";
    }

    private static Double nullableAmount(double amount) {
        return Math.abs(amount) >= 0.0001 ? amount : null;
    }

    private static double firstNonZero(double preferred, double fallback) {
        return Math.abs(preferred) >= 0.0001 ? preferred : fallback;
    }

    private static boolean containsReturnKeyword(String value) {
        if (value == null) return false;
        String normalized = value.toLowerCase(new Locale("vi", "VN"));
        return normalized.contains("trả") || normalized.contains("tra hang")
                || normalized.contains("đổi trả") || normalized.contains("doi tra");
    }

    private static boolean isReturnDetail(double receivedQuantity, String returnOrderId, String note) {
        return Math.abs(receivedQuantity) >= 0.0001
                || returnOrderId != null
                || containsReturnKeyword(note);
    }

    public static double calculateOrderIncrease(double storedOrderTotal, double returnCredit,
                                                double outgoingAmount, double discount,
                                                double tax, double shipping) {
        double netOrderTotal = Math.max(0.0, storedOrderTotal);
        if (returnCredit < 0.0001) return netOrderTotal;
        if (netOrderTotal > 0.0001) return netOrderTotal + returnCredit;

        // Hệ thống chặn TONGCONG tối thiểu bằng 0. Khi hàng trả có giá trị cao hơn
        // hàng đổi mới, phải dựng lại giá trị hàng mới để giữ được phần chênh lệch âm.
        return Math.max(0.0, outgoingAmount - Math.max(0.0, discount)
                + Math.max(0.0, tax) + Math.max(0.0, shipping));
    }

    private static String formatReturnNote(String note) {
        if (note == null || note.trim().isEmpty()) return "Đổi trả";
        String cleaned = note.trim().replaceFirst("(?i)^\\[TRẢ HÀNG\\]\\s*", "");
        return cleaned.isEmpty() ? "Đổi trả" : "Đổi trả - " + cleaned;
    }

    private static String formatReplacementNote(String orderNote, double discount) {
        StringBuilder note = new StringBuilder("Hàng đổi mới");
        if (orderNote != null && !orderNote.trim().isEmpty()) {
            note.append(" - ").append(orderNote.trim());
        }
        if (discount > 0.0001) {
            note.append(" - Giảm giá HĐ: ")
                    .append(String.format(Locale.US, "%,.0f", discount));
        }
        return note.toString();
    }

    /**
     * Tính tổng lũy kế để dòng Tổng luôn mang đúng nghĩa:
     * tổng giá trị đơn hàng - tổng thanh toán = công nợ còn lại.
     */
    private DebtTotals loadCustomerDebtTotals(Connection conn, String customerId,
                                               Date boundary, boolean inclusive) throws Exception {
        DebtTotals totals = new DebtTotals();
        String dateCondition = boundary != null ? "AND d.NGAY " + (inclusive ? "<= ? " : "< ? ") : "";
        String orderSql = "SELECT d.ID, d.TONGCONG, d.TIENTHANHTOAN, d.TIENGIAMGIA, " +
                "d.TIENTHUE, d.PHIVANCHUYEN FROM TDONHANG d " +
                "WHERE d.DKHACHHANGID = ? AND d.LOAI = 0 " +
                "AND (d.DATHANHTOAN = 30 OR d.STATUS = 1) " +
                "AND (d.STATUS IS NULL OR d.STATUS >= 0) " + dateCondition;
        String detailSql = "SELECT c.TDONHANGID, c.SLXUAT, c.SLNHAP, " +
                "c.SLXUATCHUAQUYDOI, c.SLNHAPCHUAQUYDOI, c.TDONHANGTRAID, " +
                "c.THANHTIEN, c.NOTE FROM TDONHANGCHITIET c " +
                "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "WHERE d.DKHACHHANGID = ? AND d.LOAI = 0 " +
                "AND (d.DATHANHTOAN = 30 OR d.STATUS = 1) " +
                "AND (d.STATUS IS NULL OR d.STATUS >= 0) AND (c.STATUS IS NULL OR c.STATUS >= 0) " +
                "AND COALESCE(c.XUATVATTU, 0) = 0 " + dateCondition;
        String cashDateCondition = boundary != null
                ? "AND tc.NGAY " + (inclusive ? "<= ? " : "< ? ") : "";
        String cashSql = "SELECT COALESCE(SUM(COALESCE(tc.THU, 0) - COALESCE(tc.CHI, 0)), 0) AS THANHTOAN " +
                "FROM TTHUCHI tc WHERE tc.DKHACHHANGID = ? " +
                "AND (tc.STATUS IS NULL OR tc.STATUS >= 0) " +
                "AND (tc.KHONGTHAYDOICONGNO IS NULL OR tc.KHONGTHAYDOICONGNO = 0) " +
                cashDateCondition;

        Map<String, DebtOrderTotals> orderTotals = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
            ps.setString(1, customerId);
            if (boundary != null) ps.setTimestamp(2, new Timestamp(boundary.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DebtOrderTotals order = new DebtOrderTotals();
                    order.orderTotal = Math.max(0.0, rs.getDouble("TONGCONG"));
                    order.immediatePayment = Math.max(0.0, rs.getDouble("TIENTHANHTOAN"));
                    order.discount = Math.max(0.0, rs.getDouble("TIENGIAMGIA"));
                    order.tax = Math.max(0.0, rs.getDouble("TIENTHUE"));
                    order.shipping = Math.max(0.0, rs.getDouble("PHIVANCHUYEN"));
                    orderTotals.put(rs.getString("ID"), order);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
            ps.setString(1, customerId);
            if (boundary != null) ps.setTimestamp(2, new Timestamp(boundary.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DebtOrderTotals order = orderTotals.get(rs.getString("TDONHANGID"));
                    if (order == null) continue;
                    double outgoingQuantity = firstNonZero(
                            rs.getDouble("SLXUATCHUAQUYDOI"), rs.getDouble("SLXUAT"));
                    double receivedQuantity = firstNonZero(
                            rs.getDouble("SLNHAPCHUAQUYDOI"), rs.getDouble("SLNHAP"));
                    double amount = Math.abs(rs.getDouble("THANHTIEN"));
                    boolean isReturn = isReturnDetail(
                            receivedQuantity, rs.getString("TDONHANGTRAID"), rs.getString("NOTE"));
                    if (isReturn) {
                        order.returnCredit += amount;
                    } else if (Math.abs(outgoingQuantity) >= 0.0001) {
                        order.outgoingAmount += amount;
                    }
                }
            }
        }

        for (DebtOrderTotals order : orderTotals.values()) {
            totals.orderValue += calculateOrderIncrease(
                    order.orderTotal, order.returnCredit, order.outgoingAmount,
                    order.discount, order.tax, order.shipping);
            totals.payment += order.immediatePayment + order.returnCredit;
        }

        try (PreparedStatement ps = conn.prepareStatement(cashSql)) {
            ps.setString(1, customerId);
            if (boundary != null) ps.setTimestamp(2, new Timestamp(boundary.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totals.payment += rs.getDouble("THANHTOAN");
            }
        }
        return totals;
    }

    private static String openingDebtLabel(Date start, SimpleDateFormat sdf) {
        if (start == null) return "Nợ cũ";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1 && calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
            return "Nợ cũ " + (calendar.get(Calendar.YEAR) - 1);
        }
        return "Nợ cũ trước " + sdf.format(start);
    }

    private String buildCustomerContact(KhachHang khachHang) {
        List<String> parts = new ArrayList<>();
        if (khachHang.getDiaChi() != null && !khachHang.getDiaChi().trim().isEmpty()) {
            parts.add("Địa chỉ: " + khachHang.getDiaChi().trim());
        }
        if (khachHang.getDienThoai() != null && !khachHang.getDienThoai().trim().isEmpty()) {
            parts.add("Điện thoại: " + khachHang.getDienThoai().trim());
        }
        return String.join("   ", parts);
    }

    private String formatCustomerName(KhachHang khachHang) {
        String maKhach = safe(khachHang.getMaKhach()).trim();
        String tenKhach = safe(khachHang.getName()).trim();
        return maKhach.isEmpty() ? tenKhach : maKhach + " - " + tenKhach;
    }

    private static String firstNonBlank(String first, String fallback) {
        return first != null && !first.trim().isEmpty() ? first : fallback;
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static class CustomerDebtEvent {
        private String type;
        private String documentId;
        private String documentNo;
        private Date date;
        private Date createdAt;
        private double orderTotal;
        private double payment;
        private double discount;
        private double tax;
        private double shipping;
        private String description;
        private String note;
        private List<CustomerDebtDetail> details = Collections.emptyList();
    }

    private static class CustomerDebtDetail {
        private String maHang;
        private String tenHang;
        private String donViTinh;
        private double soLuong;
        private double donGia;
        private double thanhTien;
        private String ghiChu;
        private boolean isReturn;
    }

    private static class DebtOrderTotals {
        private double orderTotal;
        private double immediatePayment;
        private double returnCredit;
        private double outgoingAmount;
        private double discount;
        private double tax;
        private double shipping;
    }

    private static class DebtTotals {
        private double orderValue;
        private double payment;

        private double getDebt() {
            return orderValue - payment;
        }
    }

    // 4. TỔNG HỢP CÔNG NỢ KHÁCH HÀNG
    public JasperPrint generateTongHopCongNoKhachHang(String nhomKhachId) throws JRException {
        List<CongNoKhachHangItem> khList = congNoKhachDAO.getDanhSachCongNoKhachHang(
                (nhomKhachId != null && !nhomKhachId.equalsIgnoreCase("Tất cả")) ? nhomKhachId : null,
                null, false
        );

        List<TongHopCongNoKhachHangReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (CongNoKhachHangItem item : khList) {
            rows.add(new TongHopCongNoKhachHangReportRow(
                    stt++,
                    item.getTenNhomKhach() != null && !item.getTenNhomKhach().trim().isEmpty() ? item.getTenNhomKhach() : "Khác",
                    item.getMaKhach(),
                    item.getTenKhach(),
                    item.getDiaChi(),
                    item.getDienThoai(),
                    item.getEmail(),
                    item.getConNo()
            ));
        }

        Map<String, Object> params = createBaseParameters();
        params.put("p_reportTitle", "TỔNG HỢP CÔNG NỢ KHÁCH HÀNG");
        params.put("p_filterText", (nhomKhachId != null && !nhomKhachId.trim().isEmpty() && !nhomKhachId.equalsIgnoreCase("Tất cả"))
                ? "Nhóm khách: " + nhomKhachId : "Nhóm khách: Tất cả");

        cachedReportTongHopKhach = getOrCompileReport("/reports/tong_hop_cong_no_khach_hang.jrxml", cachedReportTongHopKhach);
        return JasperFillManager.fillReport(cachedReportTongHopKhach, params, new JRBeanCollectionDataSource(rows));
    }
}
