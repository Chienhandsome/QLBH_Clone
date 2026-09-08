package kazuko.ex.report.service;

import kazuko.ex.dao.BaseDAO;
import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.dao.SystemConfigDAO;
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

    // 3. BÁO CÁO CÔNG NỢ KHÁCH HÀNG (Từ ngày -> Đến ngày)
    public JasperPrint generateBaoCaoCongNoKhachHang(Date tuNgay, Date denNgay, String nhomKhachId) throws JRException {
        Date start = getStartOfDay(tuNgay);
        Date end = getEndOfDay(denNgay);

        List<BaoCaoCongNoKhachHangReportRow> rows = new ArrayList<>();

        // Map khachId -> [noDau, mua, thanhToan, noCuoi]
        Map<String, double[]> khAmountMap = new HashMap<>();

        // a. Nợ đầu: Đơn bán LOAI=0 trước start
        String sqlNoDauDon = "SELECT d.DKHACHHANGID, SUM(d.TONGCONG) AS TONGMUA, SUM(d.TIENTHANHTOAN) AS TONGDATT " +
                "FROM TDONHANG d " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DKHACHHANGID IS NOT NULL AND d.LOAI = 0 " +
                (start != null ? "AND d.NGAY < ? " : "AND 1=0 ") +
                "GROUP BY d.DKHACHHANGID";

        // b. Nợ đầu: Phiếu thu trước start
        String sqlNoDauThu = "SELECT pt.DKHACHHANGID, SUM(pt.SOTIEN) AS TONGTHU " +
                "FROM TPHIEUTHU pt " +
                "WHERE (pt.STATUS IS NULL OR pt.STATUS >= 0) AND pt.DKHACHHANGID IS NOT NULL " +
                "AND (pt.KHONGTHAYDOICONGNO IS NULL OR pt.KHONGTHAYDOICONGNO = 0) " +
                (start != null ? "AND pt.NGAY < ? " : "AND 1=0 ") +
                "GROUP BY pt.DKHACHHANGID";

        // c. Trong kỳ: Mua & Thanh toán từ đơn bán [start, end]
        String sqlTrongKyDon = "SELECT d.DKHACHHANGID, SUM(d.TONGCONG) AS TONGMUA, SUM(d.TIENTHANHTOAN) AS TONGDATT " +
                "FROM TDONHANG d " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DKHACHHANGID IS NOT NULL AND d.LOAI = 0 " +
                (start != null ? "AND d.NGAY >= ? " : "") +
                (end != null ? "AND d.NGAY <= ? " : "") +
                "GROUP BY d.DKHACHHANGID";

        // d. Trong kỳ: Phiếu thu [start, end]
        String sqlTrongKyThu = "SELECT pt.DKHACHHANGID, SUM(pt.SOTIEN) AS TONGTHU " +
                "FROM TPHIEUTHU pt " +
                "WHERE (pt.STATUS IS NULL OR pt.STATUS >= 0) AND pt.DKHACHHANGID IS NOT NULL " +
                "AND (pt.KHONGTHAYDOICONGNO IS NULL OR pt.KHONGTHAYDOICONGNO = 0) " +
                (start != null ? "AND pt.NGAY >= ? " : "") +
                (end != null ? "AND pt.NGAY <= ? " : "") +
                "GROUP BY pt.DKHACHHANGID";

        try (Connection conn = getConnection()) {
            if (start != null) {
                try (PreparedStatement ps = conn.prepareStatement(sqlNoDauDon)) {
                    ps.setTimestamp(1, new Timestamp(start.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String id = rs.getString("DKHACHHANGID");
                            if (id != null) {
                                double[] arr = khAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                                arr[0] += (rs.getDouble("TONGMUA") - rs.getDouble("TONGDATT"));
                            }
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlNoDauThu)) {
                    ps.setTimestamp(1, new Timestamp(start.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String id = rs.getString("DKHACHHANGID");
                            if (id != null) {
                                double[] arr = khAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                                arr[0] -= rs.getDouble("TONGTHU");
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
                        String id = rs.getString("DKHACHHANGID");
                        if (id != null) {
                            double[] arr = khAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                            arr[1] += rs.getDouble("TONGMUA");
                            arr[2] += rs.getDouble("TONGDATT");
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTrongKyThu)) {
                int pIdx = 1;
                if (start != null) ps.setTimestamp(pIdx++, new Timestamp(start.getTime()));
                if (end != null) ps.setTimestamp(pIdx++, new Timestamp(end.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("DKHACHHANGID");
                        if (id != null) {
                            double[] arr = khAmountMap.computeIfAbsent(id.trim(), k -> new double[4]);
                            arr[2] += rs.getDouble("TONGTHU");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính toán Báo cáo công nợ Khách hàng: " + e.getMessage());
        }

        // Lấy danh sách Khách hàng
        String sqlKh = "SELECT k.ID, k.MAKHACH, k.NAME AS TENKHACH, k.DNHOMKHACHHANGID, nh.NAME AS TENNHOM, k.DIENTHOAI " +
                "FROM DKHACHHANG k " +
                "LEFT JOIN DNHOMKHACHHANG nh ON k.DNHOMKHACHHANGID = nh.ID " +
                "WHERE (k.STATUS IS NULL OR k.STATUS >= 0) " +
                "ORDER BY nh.NAME ASC, k.NAME ASC";

        int stt = 1;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlKh);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("ID");
                String ma = rs.getString("MAKHACH");
                String ten = rs.getString("TENKHACH");
                String nhomId = rs.getString("DNHOMKHACHHANGID");
                String tenNhom = rs.getString("TENNHOM");
                String dt = rs.getString("DIENTHOAI");

                if (nhomKhachId != null && !nhomKhachId.trim().isEmpty() && !nhomKhachId.equalsIgnoreCase("Tất cả")) {
                    if (nhomId == null || !nhomId.trim().equalsIgnoreCase(nhomKhachId.trim())) {
                        continue;
                    }
                }

                double[] arr = khAmountMap.getOrDefault(id != null ? id.trim() : "", new double[]{0.0, 0.0, 0.0, 0.0});
                double noDau = arr[0];
                double mua = arr[1];
                double thanhToan = arr[2];
                double noCuoi = noDau + mua - thanhToan;

                rows.add(new BaoCaoCongNoKhachHangReportRow(
                        stt++,
                        tenNhom != null && !tenNhom.trim().isEmpty() ? tenNhom : "Khác",
                        ma, ten, dt, noDau, mua, thanhToan, noCuoi
                ));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load Khách hàng: " + e.getMessage());
        }

        Map<String, Object> params = createBaseParameters();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String strTuNgay = start != null ? sdf.format(start) : "01/01/1970";
        String strDenNgay = end != null ? sdf.format(end) : sdf.format(new Date());
        params.put("p_dateRangeText", "Từ ngày " + strTuNgay + " Đến ngày " + strDenNgay);
        params.put("p_reportTitle", "BÁO CÁO CÔNG NỢ KHÁCH HÀNG");

        cachedReportBaoCaoKhach = getOrCompileReport("/reports/bao_cao_cong_no_khach_hang.jrxml", cachedReportBaoCaoKhach);
        return JasperFillManager.fillReport(cachedReportBaoCaoKhach, params, new JRBeanCollectionDataSource(rows));
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
