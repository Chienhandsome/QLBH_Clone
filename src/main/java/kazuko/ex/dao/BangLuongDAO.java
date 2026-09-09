package kazuko.ex.dao;

import kazuko.ex.model.nhansu.BangLuong;
import kazuko.ex.model.nhansu.ChamCongChiTiet;
import kazuko.ex.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class BangLuongDAO extends BaseDAO {
    private static final List<BangLuong> MEMORY_LIST = new CopyOnWriteArrayList<>();
    // Matrix key: bangLuongId + "_" + nhanVienId + "_" + caId + "_" + day -> int status
    private static final Map<String, Integer> ATTENDANCE_MATRIX = new ConcurrentHashMap<>();
    private static final Set<String> LOADED_BANGLUONG_IDS = Collections.synchronizedSet(new HashSet<>());

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!MEMORY_LIST.isEmpty()) return;

        BangLuong bl2 = new BangLuong("bl-002", "Tháng 8/2026", 8, 2026);
        bl2.setTimeCreated(new Date());
        bl2.setUserCreatedId("4f1466a0-0756-4ba9-afa8-053b96ca7569");
        MEMORY_LIST.add(bl2);

        BangLuong bl1 = new BangLuong("c3c4015a-433a-40f0-862c-90100fdd3615", "Tháng 7/2018", 7, 2018);
        bl1.setTimeCreated(new Date());
        bl1.setUserCreatedId("4f1466a0-0756-4ba9-afa8-053b96ca7569");
        MEMORY_LIST.add(bl1);
    }

    private static void setCell(String bangLuongId, String nhanVienId, String caId, int day, int status) {
        String key = bangLuongId + "_" + nhanVienId + "_" + caId + "_" + day;
        if (status == 0) {
            ATTENDANCE_MATRIX.remove(key);
        } else {
            ATTENDANCE_MATRIX.put(key, status);
        }
    }

    private List<BangLuong> tryLoadFromDb() {
        List<BangLuong> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM TBANGLUONG WHERE STATUS IS NULL OR STATUS >= 0 ORDER BY NAM DESC, THANG DESC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BangLuong bl = new BangLuong();
                bl.setId(rs.getString("ID"));
                bl.setSoPhieu(rs.getString("NAME"));
                bl.setThang(rs.getInt("THANG"));
                bl.setNam(rs.getInt("NAM"));
                bl.setGhiChu(rs.getString("NOTE"));
                bl.setStatus(rs.getInt("STATUS") >= 0 ? 1 : -1);
                bl.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                bl.setUserCreatedId(rs.getString("USERCREATEDID"));
                list.add(bl);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    public List<BangLuong> getAll() {
        List<BangLuong> dbList = tryLoadFromDb();
        if (dbList != null && !dbList.isEmpty()) {
            return dbList;
        }
        return MEMORY_LIST.stream()
                .filter(bl -> bl.getStatus() == 1)
                .sorted(Comparator.comparing(BangLuong::getNam).thenComparing(BangLuong::getThang).reversed())
                .collect(Collectors.toList());
    }

    public BangLuong findById(String id) {
        if (id == null) return null;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM TBANGLUONG WHERE ID = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BangLuong bl = new BangLuong();
                    bl.setId(rs.getString("ID"));
                    bl.setSoPhieu(rs.getString("NAME"));
                    bl.setThang(rs.getInt("THANG"));
                    bl.setNam(rs.getInt("NAM"));
                    bl.setGhiChu(rs.getString("NOTE"));
                    bl.setStatus(rs.getInt("STATUS") >= 0 ? 1 : -1);
                    bl.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    bl.setUserCreatedId(rs.getString("USERCREATEDID"));
                    return bl;
                }
            }
        } catch (Exception ignored) {}

        return MEMORY_LIST.stream()
                .filter(bl -> id.equals(bl.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(BangLuong bl) {
        if (bl == null) return false;
        if (bl.getId() == null || bl.getId().trim().isEmpty()) {
            bl.setId(UUID.randomUUID().toString());
        }
        if (bl.getSoPhieu() == null || bl.getSoPhieu().trim().isEmpty()) {
            bl.setSoPhieu("Tháng " + bl.getThang() + "/" + bl.getNam());
        }
        if (bl.getTimeCreated() == null) {
            bl.setTimeCreated(new Date());
        }
        bl.setStatus(1);

        String userId = getEffectiveUserId();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO TBANGLUONG (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, THANG, NAM) VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?, ?, ?)"
             )) {
            ps.setString(1, bl.getId());
            ps.setString(2, bl.getSoPhieu());
            ps.setString(3, bl.getGhiChu());
            ps.setString(4, userId);
            ps.setInt(5, bl.getThang());
            ps.setInt(6, bl.getNam());
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception e) {
            System.err.println("Lỗi insert TBANGLUONG: " + e.getMessage());
        }

        MEMORY_LIST.add(0, bl);
        audit("Bảng lương", "THÊM", bl.getSoPhieu(), "Lập bảng lương");
        return true;
    }

    public boolean update(BangLuong bl) {
        if (bl == null || bl.getId() == null) return false;

        String userId = getEffectiveUserId();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE TBANGLUONG SET NAME = ?, NOTE = ?, THANG = ?, NAM = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?"
             )) {
            ps.setString(1, bl.getSoPhieu());
            ps.setString(2, bl.getGhiChu());
            ps.setInt(3, bl.getThang());
            ps.setInt(4, bl.getNam());
            ps.setString(5, userId);
            ps.setString(6, bl.getId());
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception e) {
            System.err.println("Lỗi update TBANGLUONG: " + e.getMessage());
        }

        for (int i = 0; i < MEMORY_LIST.size(); i++) {
            if (bl.getId().equals(MEMORY_LIST.get(i).getId())) {
                MEMORY_LIST.set(i, bl);
                audit("Bảng lương", "SỬA", bl.getSoPhieu(), "Cập nhật bảng lương");
                return true;
            }
        }
        audit("Bảng lương", "SỬA", bl.getSoPhieu(), "Cập nhật bảng lương");
        return true;
    }

    public boolean delete(String id) {
        if (id == null) return false;

        try (Connection conn = getConnection()) {
            try (PreparedStatement psDetails = conn.prepareStatement("DELETE FROM TBANGLUONGCHITIET WHERE TBANGLUONGID = ?")) {
                psDetails.setString(1, id);
                psDetails.executeUpdate();
            }
            try (PreparedStatement psBL = conn.prepareStatement("DELETE FROM TBANGLUONG WHERE ID = ?")) {
                psBL.setString(1, id);
                psBL.executeUpdate();
            }
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception e) {
            System.err.println("Lỗi delete TBANGLUONG: " + e.getMessage());
        }

        LOADED_BANGLUONG_IDS.remove(id);
        boolean removed = MEMORY_LIST.removeIf(bl -> id.equals(bl.getId()));
        audit("Bảng lương", "XÓA", id, "Xóa bảng lương");
        return removed;
    }

    public synchronized void loadAttendanceForBangLuong(String bangLuongId) {
        if (bangLuongId == null || LOADED_BANGLUONG_IDS.contains(bangLuongId)) return;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DNHANVIENID, DCALAMVIECID, NGAY, TRANGTHAI FROM TBANGLUONGCHITIET WHERE TBANGLUONGID = ? AND (STATUS IS NULL OR STATUS >= 0)"
             )) {
            ps.setString(1, bangLuongId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nvId = rs.getString("DNHANVIENID");
                    String caId = rs.getString("DCALAMVIECID");
                    Timestamp ts = rs.getTimestamp("NGAY");
                    int trangThai = rs.getInt("TRANGTHAI");

                    if (ts != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(ts);
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        setCell(bangLuongId, nvId, caId, day, trangThai);
                    }
                }
            }
            LOADED_BANGLUONG_IDS.add(bangLuongId);
        } catch (Exception e) {
            LOADED_BANGLUONG_IDS.add(bangLuongId);
        }
    }

    public int getAttendanceStatus(String bangLuongId, String nhanVienId, String caId, int day) {
        if (bangLuongId == null) return 0;
        if (!LOADED_BANGLUONG_IDS.contains(bangLuongId)) {
            loadAttendanceForBangLuong(bangLuongId);
        }
        String key = bangLuongId + "_" + nhanVienId + "_" + caId + "_" + day;
        return ATTENDANCE_MATRIX.getOrDefault(key, 0);
    }

    public void setAttendanceStatus(String bangLuongId, String nhanVienId, String caId, int day, int status) {
        setCell(bangLuongId, nhanVienId, caId, day, status);
    }

    public boolean saveAllAttendance(String bangLuongId, int thang, int nam, List<ChamCongChiTiet> records) {
        if (bangLuongId == null) return false;

        // 1. Update memory matrix
        for (ChamCongChiTiet ct : records) {
            setCell(bangLuongId, ct.getNhanVienId(), ct.getCaLamViecId(), ct.getNgay(), ct.getTrangThai());
        }
        LOADED_BANGLUONG_IDS.add(bangLuongId);

        String userId = getEffectiveUserId();

        // 2. Persist to DB TBANGLUONGCHITIET
        try (Connection conn = getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM TBANGLUONGCHITIET WHERE TBANGLUONGID = ?")) {
                psDel.setString(1, bangLuongId);
                psDel.executeUpdate();
            }

            String sqlIns = "INSERT INTO TBANGLUONGCHITIET (ID, TBANGLUONGID, DNHANVIENID, DCALAMVIECID, NGAY, TRANGTHAI, STATUS, TIMECREATED, USERCREATEDID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
            try (PreparedStatement psIns = conn.prepareStatement(sqlIns)) {
                for (ChamCongChiTiet ct : records) {
                    if (ct.getTrangThai() <= 0) continue; // Only persist attendance entries

                    Calendar cal = Calendar.getInstance();
                    cal.clear();
                    cal.set(nam, thang - 1, ct.getNgay(), 0, 0, 0);
                    Timestamp ts = new Timestamp(cal.getTimeInMillis());

                    psIns.setString(1, UUID.randomUUID().toString());
                    psIns.setString(2, bangLuongId);
                    psIns.setString(3, ct.getNhanVienId());
                    psIns.setString(4, ct.getCaLamViecId());
                    psIns.setTimestamp(5, ts);
                    psIns.setInt(6, ct.getTrangThai());
                    psIns.setString(7, userId);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            conn.commit();
            conn.setAutoCommit(autoCommit);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi lưu TBANGLUONGCHITIET: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
