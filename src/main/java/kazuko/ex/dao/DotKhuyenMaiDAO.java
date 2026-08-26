package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.DotKhuyenMai;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DotKhuyenMaiDAO extends BaseDAO {
    private static final List<DotKhuyenMai> MEMORY_PROMOTIONS = new CopyOnWriteArrayList<>();
    private static final Map<String, DotKhuyenMai> TRASH_PROMOTIONS = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> PROMOTION_STORES = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Double>> PROMOTION_GROUP_DISCOUNTS = new ConcurrentHashMap<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<DotKhuyenMai> getAll() {
        List<DotKhuyenMai> dbList = tryLoadFromDb(1);
        if (dbList != null) {
            return dbList;
        }

        List<DotKhuyenMai> result = new ArrayList<>();
        for (DotKhuyenMai p : MEMORY_PROMOTIONS) {
            if (p.getStatus() >= 0 && !TRASH_PROMOTIONS.containsKey(p.getId())) {
                result.add(p);
            }
        }
        return result;
    }

    public List<DotKhuyenMai> getTrashItems() {
        List<DotKhuyenMai> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }

        List<DotKhuyenMai> trashList = new ArrayList<>();
        for (DotKhuyenMai p : MEMORY_PROMOTIONS) {
            if (p.getStatus() < 0 && p.getId() != null) {
                trashList.add(p);
            }
        }
        for (DotKhuyenMai tr : TRASH_PROMOTIONS.values()) {
            if (!trashList.contains(tr)) {
                trashList.add(tr);
            }
        }
        return trashList;
    }

    public List<DotKhuyenMai> search(String keyword, String loaiKmName, String loaiKmId) {
        List<DotKhuyenMai> all = getAll();
        List<DotKhuyenMai> result = new ArrayList<>();
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";

        for (DotKhuyenMai p : all) {
            boolean matchGroup = true;
            if (loaiKmId != null && !loaiKmId.trim().isEmpty()) {
                matchGroup = loaiKmId.equalsIgnoreCase(p.getdLoaiHinhKhuyenMaiId());
            } else if (loaiKmName != null && !loaiKmName.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(loaiKmName.trim())) {
                matchGroup = loaiKmName.equalsIgnoreCase(p.getTenLoaiHinhKhuyenMai());
            }

            if (!matchGroup) continue;

            if (kw.isEmpty()) {
                result.add(p);
            } else {
                boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(kw);
                boolean matchLoai = p.getTenLoaiHinhKhuyenMai() != null && p.getTenLoaiHinhKhuyenMai().toLowerCase().contains(kw);
                boolean matchNote = p.getNote() != null && p.getNote().toLowerCase().contains(kw);
                if (matchName || matchLoai || matchNote) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    public DotKhuyenMai findActiveById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (DotKhuyenMai p : getAll()) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    public DotKhuyenMai findTrashById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (DotKhuyenMai p : getTrashItems()) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    public DotKhuyenMai findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        DotKhuyenMai active = findActiveById(id);
        if (active != null) return active;
        return findTrashById(id);
    }

    public boolean insert(DotKhuyenMai km) {
        if (km == null) return false;
        if (km.getId() == null || km.getId().trim().isEmpty()) {
            km.setId(UUID.randomUUID().toString());
        }
        km.setStatus(30);
        km.setTimeCreated(new Timestamp(System.currentTimeMillis()));
        km.setUserCreatedId(getEffectiveUserId());

        boolean dbSuccess = tryInsertToDb(km);
        MEMORY_PROMOTIONS.add(km);
        if (km.getListCuaHangApDungIds() != null) {
            saveStoresForPromotion(km.getId(), km.getListCuaHangApDungIds());
        }
        if (km.getMapNhomGiamGia() != null) {
            saveGroupDiscounts(km.getId(), km.getMapNhomGiamGia());
        }
        return dbSuccess || true;
    }

    public boolean update(DotKhuyenMai km) {
        if (km == null || km.getId() == null) return false;
        km.setTimeModified(new Timestamp(System.currentTimeMillis()));
        km.setUserModifiedId(getEffectiveUserId());

        boolean dbSuccess = tryUpdateToDb(km);
        for (int i = 0; i < MEMORY_PROMOTIONS.size(); i++) {
            if (km.getId().equals(MEMORY_PROMOTIONS.get(i).getId())) {
                MEMORY_PROMOTIONS.set(i, km);
                break;
            }
        }
        if (km.getListCuaHangApDungIds() != null) {
            saveStoresForPromotion(km.getId(), km.getListCuaHangApDungIds());
        }
        if (km.getMapNhomGiamGia() != null) {
            saveGroupDiscounts(km.getId(), km.getMapNhomGiamGia());
        }
        return dbSuccess || true;
    }

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, -1);
        DotKhuyenMai found = findById(id);
        if (found != null) {
            found.setStatus(-1);
            TRASH_PROMOTIONS.put(id, found);
        }
        return dbSuccess;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, 30);
        DotKhuyenMai found = findById(id);
        if (found != null) {
            found.setStatus(30);
            TRASH_PROMOTIONS.remove(id);
        }
        return dbSuccess;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        boolean dbSuccess = tryDeleteFromDb(id);
        TRASH_PROMOTIONS.remove(id);
        MEMORY_PROMOTIONS.removeIf(p -> id.equals(p.getId()));
        PROMOTION_STORES.remove(id);
        return dbSuccess;
    }

    public List<String> getStoresForPromotion(String dotKmId) {
        if (dotKmId == null) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        String sql = "SELECT DCUAHANGID FROM DDOTKHUYENMAIAPDUNG WHERE DDOTKHUYENMAIID=? AND (STATUS IS NULL OR STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dotKmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String chId = rs.getString("DCUAHANGID");
                    if (chId != null) list.add(chId);
                }
            }
            return list;
        } catch (Exception e) {
            if (PROMOTION_STORES.containsKey(dotKmId)) {
                return PROMOTION_STORES.get(dotKmId);
            }
            return new ArrayList<>();
        }
    }

    public boolean saveStoresForPromotion(String dotKmId, List<String> cuaHangIds) {
        if (dotKmId == null) return false;
        PROMOTION_STORES.put(dotKmId, new ArrayList<>(cuaHangIds != null ? cuaHangIds : Collections.emptyList()));

        String sqlDelete = "DELETE FROM DDOTKHUYENMAIAPDUNG WHERE DDOTKHUYENMAIID=?";
        String sqlInsert = "INSERT INTO DDOTKHUYENMAIAPDUNG (ID, DDOTKHUYENMAIID, DCUAHANGID, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, dotKmId);
                psDel.executeUpdate();
            }
            if (cuaHangIds != null && !cuaHangIds.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    for (String chId : cuaHangIds) {
                        psIns.setString(1, UUID.randomUUID().toString());
                        psIns.setString(2, dotKmId);
                        psIns.setString(3, chId);
                        psIns.setString(4, getEffectiveUserId());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi saveStoresForPromotion: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Double> getGroupDiscounts(String dotKmId) {
        if (dotKmId == null) return new HashMap<>();
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT DNHOMMATHANGID, TILEGIAMGIA FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DNHOMMATHANGID IS NOT NULL AND (STATUS IS NULL OR STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dotKmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nhomId = rs.getString("DNHOMMATHANGID");
                    double tile = rs.getDouble("TILEGIAMGIA");
                    if (nhomId != null) {
                        map.put(nhomId, tile);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            if (PROMOTION_GROUP_DISCOUNTS.containsKey(dotKmId)) {
                return new HashMap<>(PROMOTION_GROUP_DISCOUNTS.get(dotKmId));
            }
            return map;
        }
    }

    public boolean saveGroupDiscounts(String dotKmId, Map<String, Double> groupDiscounts) {
        if (dotKmId == null) return false;
        PROMOTION_GROUP_DISCOUNTS.put(dotKmId, new HashMap<>(groupDiscounts != null ? groupDiscounts : Collections.emptyMap()));

        String sqlDelete = "DELETE FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DNHOMMATHANGID IS NOT NULL";
        String sqlInsert = "INSERT INTO DDOTKHUYENMAICHITIET (ID, DDOTKHUYENMAIID, DNHOMMATHANGID, TILEGIAMGIA, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, dotKmId);
                psDel.executeUpdate();
            }
            if (groupDiscounts != null && !groupDiscounts.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    for (Map.Entry<String, Double> entry : groupDiscounts.entrySet()) {
                        psIns.setString(1, UUID.randomUUID().toString());
                        psIns.setString(2, dotKmId);
                        psIns.setString(3, entry.getKey());
                        psIns.setDouble(4, entry.getValue() != null ? entry.getValue() : 0.0);
                        psIns.setString(5, getEffectiveUserId());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi saveGroupDiscounts: " + e.getMessage());
            return false;
        }
    }

    private List<DotKhuyenMai> tryLoadFromDb(int statusFilter) {
        List<DotKhuyenMai> list = new ArrayList<>();
        String sql = "SELECT k.ID, k.NAME, k.NOTE, k.STATUS, k.USERMODIFIEDID, k.TIMEMODIFIED, k.TIMECREATED, k.USERCREATEDID, " +
                     "k.DLOAIHINHKHUYENMAIID, l.NAME as TENLOAIKM, k.TUNGAY, k.DENNGAY, k.NGUNGAPDUNG, k.TILEGIAMGIA, k.TUGIO, k.DENGIO " +
                     "FROM DDOTKHUYENMAI k " +
                     "LEFT JOIN DLOAIHINHKHUYENMAI l ON k.DLOAIHINHKHUYENMAIID = l.ID " +
                     "WHERE " + (statusFilter >= 0 ? "(k.STATUS IS NULL OR k.STATUS >= 0)" : "k.STATUS < 0") + " " +
                     "ORDER BY k.NAME";
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DotKhuyenMai km = new DotKhuyenMai();
                    km.setId(rs.getString("ID"));
                    km.setName(rs.getString("NAME"));
                    km.setNote(rs.getString("NOTE"));
                    km.setStatus(rs.getInt("STATUS"));
                    km.setUserModifiedId(rs.getString("USERMODIFIEDID"));
                    km.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                    km.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    km.setUserCreatedId(rs.getString("USERCREATEDID"));
                    km.setdLoaiHinhKhuyenMaiId(rs.getString("DLOAIHINHKHUYENMAIID"));
                    km.setTenLoaiHinhKhuyenMai(rs.getString("TENLOAIKM"));
                    km.setTuNgay(rs.getTimestamp("TUNGAY"));
                    km.setDenNgay(rs.getTimestamp("DENNGAY"));
                    int ngung = rs.getInt("NGUNGAPDUNG");
                    km.setNgungApDung(ngung != 0);
                    km.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    km.setTuGio(rs.getTimestamp("TUGIO"));
                    km.setDenGio(rs.getTimestamp("DENGIO"));
                    list.add(km);
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi tryLoadFromDb DDOTKHUYENMAI: " + e.getMessage());
            return null;
        }
    }

    private boolean tryInsertToDb(DotKhuyenMai km) {
        String sql = "INSERT INTO DDOTKHUYENMAI (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, DLOAIHINHKHUYENMAIID, TUNGAY, DENNGAY, NGUNGAPDUNG, TILEGIAMGIA, TUGIO, DENGIO) " +
                     "VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, km.getId());
            ps.setString(2, km.getName());
            ps.setString(3, km.getNote() != null ? km.getNote() : "");
            ps.setString(4, getEffectiveUserId());
            ps.setString(5, km.getdLoaiHinhKhuyenMaiId());
            ps.setTimestamp(6, km.getTuNgay());
            ps.setTimestamp(7, km.getDenNgay());
            ps.setInt(8, km.isNgungApDung() ? 30 : 0);
            ps.setDouble(9, km.getTiLeGiamGia());
            ps.setTimestamp(10, km.getTuGio());
            ps.setTimestamp(11, km.getDenGio());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryInsertToDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private boolean tryUpdateToDb(DotKhuyenMai km) {
        String sql = "UPDATE DDOTKHUYENMAI SET NAME=?, NOTE=?, DLOAIHINHKHUYENMAIID=?, TUNGAY=?, DENNGAY=?, NGUNGAPDUNG=?, TILEGIAMGIA=?, TUGIO=?, DENGIO=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, km.getName());
            ps.setString(2, km.getNote() != null ? km.getNote() : "");
            ps.setString(3, km.getdLoaiHinhKhuyenMaiId());
            ps.setTimestamp(4, km.getTuNgay());
            ps.setTimestamp(5, km.getDenNgay());
            ps.setInt(6, km.isNgungApDung() ? 30 : 0);
            ps.setDouble(7, km.getTiLeGiamGia());
            ps.setTimestamp(8, km.getTuGio());
            ps.setTimestamp(9, km.getDenGio());
            ps.setString(10, getEffectiveUserId());
            ps.setString(11, km.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryUpdateToDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private boolean trySetStatusDb(String id, int status) {
        String sql = "UPDATE DDOTKHUYENMAI SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, getEffectiveUserId());
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi trySetStatusDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        String sql = "DELETE FROM DDOTKHUYENMAI WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryDeleteFromDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }
}
