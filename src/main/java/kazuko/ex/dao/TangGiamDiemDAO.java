package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.danhmuc.DiemHoaDon;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhachHangLoyaltySummary;
import kazuko.ex.model.danhmuc.TangGiamDiem;
import kazuko.ex.service.ActivityLogService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class TangGiamDiemDAO {
    private static final String DEFAULT_USER_ID = "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private void audit(String action, String reference, String content) {
        ActivityLogService.log("Tăng giảm điểm", action, reference, content);
    }

    // In-memory cache fallback
    private static final List<TangGiamDiem> MEMORY_TANG_GIAM_DIEM = new ArrayList<>();

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private String getEffectiveUserId() {
        return DEFAULT_USER_ID;
    }

    public List<TangGiamDiem> getAll() {
        List<TangGiamDiem> list = new ArrayList<>();
        String sql = "SELECT t.ID, t.NAME, t.NOTE, t.STATUS, t.TIMECREATED, t.USERCREATEDID, " +
                     "t.TIMEMODIFIED, t.USERMODIFIEDID, t.NGAY, t.DKHACHHANGID, t.DIEMTANG, t.DIEMGIAM, t.LYDO, " +
                     "k.NAME as TENKHACHHANG " +
                     "FROM TTANGGIAMDIEM t " +
                     "LEFT JOIN DKHACHHANG k ON t.DKHACHHANGID = k.ID " +
                     "WHERE (t.STATUS IS NULL OR t.STATUS >= 0) " +
                     "ORDER BY t.NGAY DESC, t.TIMECREATED DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi load TTANGGIAMDIEM từ DB: " + e.getMessage());
            return new ArrayList<>(MEMORY_TANG_GIAM_DIEM);
        }
    }

    public List<TangGiamDiem> getByKhachHangId(String khachHangId) {
        if (khachHangId == null) return new ArrayList<>();
        List<TangGiamDiem> list = new ArrayList<>();
        String sql = "SELECT t.ID, t.NAME, t.NOTE, t.STATUS, t.TIMECREATED, t.USERCREATEDID, " +
                     "t.TIMEMODIFIED, t.USERMODIFIEDID, t.NGAY, t.DKHACHHANGID, t.DIEMTANG, t.DIEMGIAM, t.LYDO, " +
                     "k.NAME as TENKHACHHANG " +
                     "FROM TTANGGIAMDIEM t " +
                     "LEFT JOIN DKHACHHANG k ON t.DKHACHHANGID = k.ID " +
                     "WHERE (t.STATUS IS NULL OR t.STATUS >= 0) AND t.DKHACHHANGID = ? " +
                     "ORDER BY t.NGAY DESC, t.TIMECREATED DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi getByKhachHangId TTANGGIAMDIEM: " + e.getMessage());
            List<TangGiamDiem> result = new ArrayList<>();
            for (TangGiamDiem item : MEMORY_TANG_GIAM_DIEM) {
                if (khachHangId.equals(item.getKhachHangId()) && item.getStatus() >= 0) {
                    result.add(item);
                }
            }
            return result;
        }
    }

    public TangGiamDiem findById(String id) {
        if (id == null) return null;
        String sql = "SELECT t.ID, t.NAME, t.NOTE, t.STATUS, t.TIMECREATED, t.USERCREATEDID, " +
                     "t.TIMEMODIFIED, t.USERMODIFIEDID, t.NGAY, t.DKHACHHANGID, t.DIEMTANG, t.DIEMGIAM, t.LYDO, " +
                     "k.NAME as TENKHACHHANG " +
                     "FROM TTANGGIAMDIEM t " +
                     "LEFT JOIN DKHACHHANG k ON t.DKHACHHANGID = k.ID " +
                     "WHERE t.ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi findById TTANGGIAMDIEM: " + e.getMessage());
        }
        for (TangGiamDiem item : MEMORY_TANG_GIAM_DIEM) {
            if (id.equals(item.getId())) return item;
        }
        return null;
    }

    public boolean insert(TangGiamDiem tg) {
        if (tg == null) return false;
        if (tg.getId() == null || tg.getId().trim().isEmpty()) {
            tg.setId(UUID.randomUUID().toString());
        }
        if (tg.getNgay() == null) {
            tg.setNgay(new Timestamp(System.currentTimeMillis()));
        }
        if (tg.getSoPhieu() == null && tg.getLyDo() != null && !"Reset điểm".equalsIgnoreCase(tg.getLyDo())) {
            tg.setSoPhieu(generateNextSoPhieu());
        }

        String sql = "INSERT INTO TTANGGIAMDIEM (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, NGAY, DKHACHHANGID, DIEMTANG, DIEMGIAM, LYDO) " +
                     "VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tg.getId());
            ps.setString(2, tg.getSoPhieu() != null ? tg.getSoPhieu() : "");
            ps.setString(3, tg.getNote() != null ? tg.getNote() : "");
            ps.setString(4, getEffectiveUserId());
            ps.setTimestamp(5, tg.getNgay());
            ps.setString(6, tg.getKhachHangId());
            ps.setDouble(7, tg.getDiemTang());
            ps.setDouble(8, tg.getDiemGiam());
            ps.setString(9, tg.getLyDo() != null ? tg.getLyDo() : "");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                MEMORY_TANG_GIAM_DIEM.add(0, tg);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Lỗi insert TTANGGIAMDIEM: " + e.getMessage());
        }
        MEMORY_TANG_GIAM_DIEM.add(0, tg);
        audit("THÊM", tg.getSoPhieu(),
                "Lập phiếu tăng/giảm điểm; tăng: " + tg.getDiemTang() + "; giảm: " + tg.getDiemGiam());
        return true;
    }

    public boolean update(TangGiamDiem tg) {
        if (tg == null || tg.getId() == null) return false;
        String sql = "UPDATE TTANGGIAMDIEM SET NAME=?, NOTE=?, NGAY=?, DKHACHHANGID=?, DIEMTANG=?, DIEMGIAM=?, LYDO=?, " +
                     "TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tg.getSoPhieu() != null ? tg.getSoPhieu() : "");
            ps.setString(2, tg.getNote() != null ? tg.getNote() : "");
            ps.setTimestamp(3, tg.getNgay());
            ps.setString(4, tg.getKhachHangId());
            ps.setDouble(5, tg.getDiemTang());
            ps.setDouble(6, tg.getDiemGiam());
            ps.setString(7, tg.getLyDo() != null ? tg.getLyDo() : "");
            ps.setString(8, getEffectiveUserId());
            ps.setString(9, tg.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                updateMemory(tg);
                audit("SỬA", tg.getSoPhieu(), "Cập nhật phiếu tăng/giảm điểm");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Lỗi update TTANGGIAMDIEM: " + e.getMessage());
        }
        updateMemory(tg);
        audit("SỬA", tg.getSoPhieu(), "Cập nhật phiếu tăng/giảm điểm (bộ nhớ tạm)");
        return true;
    }

    public boolean delete(String id) {
        if (id == null) return false;
        String sql = "DELETE FROM TTANGGIAMDIEM WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Lỗi delete TTANGGIAMDIEM: " + e.getMessage());
        }
        MEMORY_TANG_GIAM_DIEM.removeIf(item -> id.equals(item.getId()));
        audit("XÓA", id, "Xóa phiếu tăng/giảm điểm");
        return true;
    }

    public String generateNextSoPhieu() {
        String year2Digits = new SimpleDateFormat("yy").format(new Date());
        String prefix = "TG" + year2Digits + "/";
        int maxSeq = 0;

        String sql = "SELECT NAME FROM TTANGGIAMDIEM WHERE NAME LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("NAME");
                    if (name != null && name.startsWith(prefix)) {
                        String numPart = name.substring(prefix.length()).trim();
                        try {
                            int seq = Integer.parseInt(numPart);
                            if (seq > maxSeq) maxSeq = seq;
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi generateNextSoPhieu DB: " + e.getMessage());
        }

        for (TangGiamDiem item : MEMORY_TANG_GIAM_DIEM) {
            if (item.getSoPhieu() != null && item.getSoPhieu().startsWith(prefix)) {
                String numPart = item.getSoPhieu().substring(prefix.length()).trim();
                try {
                    int seq = Integer.parseInt(numPart);
                    if (seq > maxSeq) maxSeq = seq;
                } catch (Exception ignored) {}
            }
        }

        return String.format("%s%05d", prefix, maxSeq + 1);
    }

    public List<DiemHoaDon> getHoaDonByKhachHangId(String khachHangId, double doanhSo1Diem) {
        if (khachHangId == null) return new ArrayList<>();
        List<DiemHoaDon> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NGAY, THANHTOAN, TONGCONG, DIEM, NOTE " +
                     "FROM TDONHANG " +
                     "WHERE DKHACHHANGID = ? AND (STATUS IS NULL OR STATUS >= 0) " +
                     "ORDER BY NGAY DESC, TIMECREATED DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DiemHoaDon hd = new DiemHoaDon();
                    hd.setId(rs.getString("ID"));
                    hd.setSoHoaDon(rs.getString("NAME"));
                    hd.setNgay(rs.getTimestamp("NGAY"));
                    double tt = rs.getDouble("THANHTOAN");
                    if (tt <= 0) tt = rs.getDouble("TONGCONG");
                    hd.setDoanhSo(tt);

                    double diem = rs.getDouble("DIEM");
                    if (diem <= 0 && doanhSo1Diem > 0 && tt > 0) {
                        diem = Math.floor(tt / doanhSo1Diem);
                    }
                    hd.setDiemTichLuy(diem);
                    hd.setGhiChu(rs.getString("NOTE"));
                    list.add(hd);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi getHoaDonByKhachHangId: " + e.getMessage());
        }
        return list;
    }

    public List<KhachHangLoyaltySummary> getLoyaltySummaryList(double doanhSo1Diem) {
        List<KhachHang> allKhach = khachHangDAO.getAll();
        List<KhachHangLoyaltySummary> result = new ArrayList<>();

        // 1. Calculate orders aggregate by DKHACHHANGID
        Map<String, Double> orderSalesMap = new HashMap<>();
        Map<String, Integer> orderCountMap = new HashMap<>();
        Map<String, Double> orderPointsMap = new HashMap<>();

        String sqlOrders = "SELECT DKHACHHANGID, COUNT(ID) as SO_HD, SUM(COALESCE(THANHTOAN, TONGCONG, 0)) as TONG_DOANH_SO, SUM(COALESCE(DIEM, 0)) as TONG_DIEM " +
                           "FROM TDONHANG " +
                           "WHERE (STATUS IS NULL OR STATUS >= 0) AND DKHACHHANGID IS NOT NULL " +
                           "GROUP BY DKHACHHANGID";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOrders);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String kId = rs.getString("DKHACHHANGID");
                int count = rs.getInt("SO_HD");
                double sales = rs.getDouble("TONG_DOANH_SO");
                double points = rs.getDouble("TONG_DIEM");
                if (points <= 0 && doanhSo1Diem > 0 && sales > 0) {
                    points = Math.floor(sales / doanhSo1Diem);
                }
                orderSalesMap.put(kId, sales);
                orderCountMap.put(kId, count);
                orderPointsMap.put(kId, points);
            }
        } catch (Exception e) {
            System.err.println("Lỗi getLoyaltySummaryList orders: " + e.getMessage());
        }

        // 2. Calculate TangGiamDiem aggregate by DKHACHHANGID
        Map<String, Double> pointIncMap = new HashMap<>();
        Map<String, Double> pointDecMap = new HashMap<>();

        String sqlTangGiam = "SELECT DKHACHHANGID, SUM(COALESCE(DIEMTANG, 0)) as TONG_TANG, SUM(COALESCE(DIEMGIAM, 0)) as TONG_GIAM " +
                             "FROM TTANGGIAMDIEM " +
                             "WHERE (STATUS IS NULL OR STATUS >= 0) AND DKHACHHANGID IS NOT NULL " +
                             "GROUP BY DKHACHHANGID";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlTangGiam);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String kId = rs.getString("DKHACHHANGID");
                pointIncMap.put(kId, rs.getDouble("TONG_TANG"));
                pointDecMap.put(kId, rs.getDouble("TONG_GIAM"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi getLoyaltySummaryList tanggiam: " + e.getMessage());
            for (TangGiamDiem tg : MEMORY_TANG_GIAM_DIEM) {
                if (tg.getKhachHangId() != null && tg.getStatus() >= 0) {
                    pointIncMap.put(tg.getKhachHangId(), pointIncMap.getOrDefault(tg.getKhachHangId(), 0.0) + tg.getDiemTang());
                    pointDecMap.put(tg.getKhachHangId(), pointDecMap.getOrDefault(tg.getKhachHangId(), 0.0) + tg.getDiemGiam());
                }
            }
        }

        // 3. Build summary items
        for (KhachHang kh : allKhach) {
            double sales = orderSalesMap.getOrDefault(kh.getId(), 0.0);
            int count = orderCountMap.getOrDefault(kh.getId(), 0);
            double orderPts = orderPointsMap.getOrDefault(kh.getId(), 0.0);
            double ptInc = pointIncMap.getOrDefault(kh.getId(), 0.0);
            double ptDec = pointDecMap.getOrDefault(kh.getId(), 0.0);

            double totalPoints = kh.getDiemTichLuyBanDau() + orderPts + ptInc - ptDec;
            if (totalPoints < 0) totalPoints = 0;

            result.add(new KhachHangLoyaltySummary(kh, sales, count, totalPoints));
        }

        return result;
    }

    public boolean resetDiem(String khachHangId, double diemHienTai) {
        if (khachHangId == null) return false;
        if (diemHienTai <= 0) return true;

        TangGiamDiem tg = new TangGiamDiem();
        tg.setId(UUID.randomUUID().toString());
        tg.setKhachHangId(khachHangId);
        tg.setNgay(new Timestamp(System.currentTimeMillis()));
        tg.setSoPhieu(""); // Reset point doesn't have a voucher number per screenshot
        tg.setDiemTang(0);
        tg.setDiemGiam(diemHienTai);
        tg.setLyDo("Reset điểm");
        tg.setNote("");
        return insert(tg);
    }

    private void updateMemory(TangGiamDiem tg) {
        for (int i = 0; i < MEMORY_TANG_GIAM_DIEM.size(); i++) {
            if (tg.getId().equals(MEMORY_TANG_GIAM_DIEM.get(i).getId())) {
                MEMORY_TANG_GIAM_DIEM.set(i, tg);
                return;
            }
        }
        MEMORY_TANG_GIAM_DIEM.add(0, tg);
    }

    private TangGiamDiem mapResultSet(ResultSet rs) throws SQLException {
        TangGiamDiem tg = new TangGiamDiem();
        tg.setId(rs.getString("ID"));
        tg.setSoPhieu(rs.getString("NAME"));
        tg.setNote(rs.getString("NOTE"));
        tg.setStatus(rs.getInt("STATUS"));
        tg.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        tg.setUserCreatedId(rs.getString("USERCREATEDID"));
        tg.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        tg.setUserModifiedId(rs.getString("USERMODIFIEDID"));
        tg.setNgay(rs.getTimestamp("NGAY"));
        tg.setKhachHangId(rs.getString("DKHACHHANGID"));
        tg.setDiemTang(rs.getDouble("DIEMTANG"));
        tg.setDiemGiam(rs.getDouble("DIEMGIAM"));
        tg.setLyDo(rs.getString("LYDO"));
        tg.setTenKhachHang(rs.getString("TENKHACHHANG"));
        return tg;
    }
}
