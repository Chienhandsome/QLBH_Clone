package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.LoaiHinhKhuyenMai;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoaiHinhKhuyenMaiDAO extends BaseDAO {
    private static final List<LoaiHinhKhuyenMai> MEMORY_CATEGORIES = new CopyOnWriteArrayList<>();
    private static final Map<String, LoaiHinhKhuyenMai> TRASH_CATEGORIES = new ConcurrentHashMap<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<LoaiHinhKhuyenMai> getAll() {
        List<LoaiHinhKhuyenMai> dbList = tryLoadFromDb(1);
        if (dbList != null && !dbList.isEmpty()) {
            return dbList;
        }

        if (MEMORY_CATEGORIES.isEmpty()) {
            initDefaultMemoryCategories();
        }

        List<LoaiHinhKhuyenMai> result = new ArrayList<>();
        for (LoaiHinhKhuyenMai mem : MEMORY_CATEGORIES) {
            if (mem.getStatus() >= 0 && !TRASH_CATEGORIES.containsKey(mem.getId())) {
                result.add(mem);
            }
        }
        return result;
    }

    public List<LoaiHinhKhuyenMai> getTrashItems() {
        List<LoaiHinhKhuyenMai> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }

        List<LoaiHinhKhuyenMai> trashList = new ArrayList<>();
        for (LoaiHinhKhuyenMai mem : MEMORY_CATEGORIES) {
            if (mem.getStatus() < 0 && mem.getId() != null) {
                trashList.add(mem);
            }
        }
        for (LoaiHinhKhuyenMai tr : TRASH_CATEGORIES.values()) {
            if (!trashList.contains(tr)) {
                trashList.add(tr);
            }
        }
        return trashList;
    }

    public LoaiHinhKhuyenMai findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (LoaiHinhKhuyenMai l : getAll()) {
            if (id.equals(l.getId())) return l;
        }
        for (LoaiHinhKhuyenMai l : getTrashItems()) {
            if (id.equals(l.getId())) return l;
        }
        return null;
    }

    public boolean update(LoaiHinhKhuyenMai item) {
        if (item == null || item.getId() == null) return false;
        boolean dbSuccess = tryUpdateToDb(item);

        for (int i = 0; i < MEMORY_CATEGORIES.size(); i++) {
            if (item.getId().equals(MEMORY_CATEGORIES.get(i).getId())) {
                MEMORY_CATEGORIES.set(i, item);
                break;
            }
        }
        return dbSuccess || true;
    }

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, -1);
        LoaiHinhKhuyenMai found = findById(id);
        if (found != null) {
            found.setStatus(-1);
            TRASH_CATEGORIES.put(id, found);
        }
        return dbSuccess;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, 30);
        LoaiHinhKhuyenMai found = findById(id);
        if (found != null) {
            found.setStatus(30);
            TRASH_CATEGORIES.remove(id);
        }
        return dbSuccess;
    }

    private List<LoaiHinhKhuyenMai> tryLoadFromDb(int statusFilter) {
        List<LoaiHinhKhuyenMai> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, TIMEMODIFIED, USERMODIFIEDID, SORTORDER, ITEMTYPE, SIMAGEID " +
                     "FROM DLOAIHINHKHUYENMAI " +
                     "WHERE " + (statusFilter >= 0 ? "(STATUS IS NULL OR STATUS >= 0)" : "STATUS < 0") + " " +
                     "ORDER BY SORTORDER, NAME";
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoaiHinhKhuyenMai l = new LoaiHinhKhuyenMai();
                    l.setId(rs.getString("ID"));
                    l.setName(rs.getString("NAME"));
                    l.setNote(rs.getString("NOTE"));
                    l.setStatus(rs.getInt("STATUS"));
                    l.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    l.setUserCreatedId(rs.getString("USERCREATEDID"));
                    l.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                    l.setUserModifiedId(rs.getString("USERMODIFIEDID"));
                    l.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                    String img = rs.getString("SIMAGEID");
                    l.setIconName(img != null && !img.trim().isEmpty() ? img : "asterisk-yellow.png");
                    list.add(l);
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi tryLoadFromDb DLOAIHINHKHUYENMAI: " + e.getMessage());
            return null;
        }
    }

    private boolean tryUpdateToDb(LoaiHinhKhuyenMai item) {
        String sql = "UPDATE DLOAIHINHKHUYENMAI SET " +
                     "NAME=?, NOTE=?, SIMAGEID=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? " +
                     "WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getNote() != null ? item.getNote() : "");
            ps.setString(3, item.getIconName() != null ? item.getIconName() : "asterisk-yellow.png");
            ps.setString(4, getEffectiveUserId());
            ps.setString(5, item.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryUpdateToDb DLOAIHINHKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private boolean trySetStatusDb(String id, int status) {
        String sql = "UPDATE DLOAIHINHKHUYENMAI SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, getEffectiveUserId());
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi trySetStatusDb DLOAIHINHKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private void initDefaultMemoryCategories() {
        MEMORY_CATEGORIES.clear();
        String[][] defaults = {
                {"8a96fc6b-d34c-4386-b421-400372a0b536", "Giảm giá tổng bill"},
                {"7c566d6f-6386-4e93-a01c-3645e1aba5e7", "Giảm giá theo nhóm"},
                {"8402c931-8a13-4d19-a693-53f9f683647e", "Giảm giá theo sản phẩm"},
                {"8be106e5-37b5-4523-8eec-a7c6ea0d40f9", "Mặt hàng đồng giá"},
                {"bba9545a-33c5-40a1-9779-25e07337ad6b", "Giảm theo số lượng hàng mua"},
                {"131e78bc-e11a-461e-bcb9-7f46df4258e1", "Giảm theo giá trị đơn hàng"},
                {"e5d39bb6-41a4-43ff-bc4d-e170ae4aeba2", "Mua 1 tặng 1"},
                {"d1f6cebb-6c43-4fc2-b447-2d3f4967a109", "Tặng hàng theo giá trị đơn hàng"}
        };
        for (String[] def : defaults) {
            LoaiHinhKhuyenMai l = new LoaiHinhKhuyenMai(def[0], def[1]);
            l.setIconName("asterisk-yellow.png");
            MEMORY_CATEGORIES.add(l);
        }
    }
}
