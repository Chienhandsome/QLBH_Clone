package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NhomMatHangDAO extends BaseDAO {
    private static final List<NhomMatHang> MEMORY_CATEGORIES = new CopyOnWriteArrayList<>();
    private static final Map<String, NhomMatHang> TRASH_CATEGORIES = new ConcurrentHashMap<>();
    private static final Set<String> DELETED_CATEGORY_IDS = ConcurrentHashMap.newKeySet();
    private static final Set<String> DELETED_CATEGORY_NAMES = ConcurrentHashMap.newKeySet();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<NhomMatHang> getAll() {
        List<NhomMatHang> dbList = tryLoadFromDb(1);
        if (dbList != null) {
            List<NhomMatHang> result = new ArrayList<>();
            for (NhomMatHang dbNh : dbList) {
                if (!isCategoryInTrash(dbNh)) {
                    result.add(dbNh);
                }
            }
            return result;
        }

        List<NhomMatHang> result = new ArrayList<>();
        for (NhomMatHang memNh : MEMORY_CATEGORIES) {
            if (!isCategoryInTrash(memNh)) {
                result.add(memNh);
            }
        }
        return result;
    }

    public List<NhomMatHang> getTrashItems() {
        Map<String, NhomMatHang> trashMap = new LinkedHashMap<>();

        // 1. Tải từ CSDL các mục có STATUS < 0
        List<NhomMatHang> allDb = tryLoadAllFromDb();
        if (allDb != null) {
            for (NhomMatHang dbNh : allDb) {
                if (isCategoryInTrash(dbNh)) {
                    if (dbNh.getId() != null) {
                        trashMap.put(dbNh.getId(), dbNh);
                    }
                }
            }
        }

        // 2. Thêm từ TRASH_CATEGORIES bộ nhớ
        for (Map.Entry<String, NhomMatHang> entry : TRASH_CATEGORIES.entrySet()) {
            if (!trashMap.containsKey(entry.getKey())) {
                trashMap.put(entry.getKey(), entry.getValue());
            }
        }

        // 3. Thêm từ MEMORY_CATEGORIES nếu có status < 0
        for (NhomMatHang memNh : MEMORY_CATEGORIES) {
            if (isCategoryInTrash(memNh)) {
                if (memNh.getId() != null && !trashMap.containsKey(memNh.getId())) {
                    trashMap.put(memNh.getId(), memNh);
                }
            }
        }

        return new ArrayList<>(trashMap.values());
    }

    private boolean isCategoryInTrash(NhomMatHang nh) {
        if (nh == null) return false;
        if (nh.getStatus() < 0) return true;
        if (nh.getId() != null && DELETED_CATEGORY_IDS.contains(nh.getId())) return true;
        if (nh.getName() != null && DELETED_CATEGORY_NAMES.contains(nh.getName().trim().toLowerCase())) return true;
        if (nh.getId() != null && TRASH_CATEGORIES.containsKey(nh.getId())) return true;
        return false;
    }

    public NhomMatHang findById(String id) {
        if (id == null) return null;
        for (NhomMatHang nh : getAll()) {
            if (id.equals(nh.getId())) return nh;
        }
        for (NhomMatHang nh : getTrashItems()) {
            if (id.equals(nh.getId())) return nh;
        }
        if (TRASH_CATEGORIES.containsKey(id)) {
            return TRASH_CATEGORIES.get(id);
        }
        return null;
    }

    public Set<String> getAllDescendantIds(String rootId) {
        Set<String> allIds = new LinkedHashSet<>();
        if (rootId == null || rootId.trim().isEmpty()) return allIds;
        allIds.add(rootId.trim());

        List<NhomMatHang> allItems = new ArrayList<>();
        List<NhomMatHang> dbItems = tryLoadAllFromDb();
        if (dbItems != null && !dbItems.isEmpty()) {
            allItems.addAll(dbItems);
        } else {
            allItems.addAll(MEMORY_CATEGORIES);
        }
        allItems.addAll(TRASH_CATEGORIES.values());

        boolean added;
        do {
            added = false;
            for (NhomMatHang nh : allItems) {
                if (nh.getParentId() != null && !nh.getParentId().trim().isEmpty() && nh.getId() != null) {
                    if (allIds.contains(nh.getParentId().trim())) {
                        if (allIds.add(nh.getId().trim())) {
                            added = true;
                        }
                    }
                }
            }
        } while (added);

        return allIds;
    }

    public Set<String> getAllAncestorIds(String childId) {
        Set<String> ancestors = new HashSet<>();
        if (childId == null || childId.trim().isEmpty()) return ancestors;

        List<NhomMatHang> allItems = new ArrayList<>();
        List<NhomMatHang> dbItems = tryLoadAllFromDb();
        if (dbItems != null && !dbItems.isEmpty()) {
            allItems.addAll(dbItems);
        } else {
            allItems.addAll(MEMORY_CATEGORIES);
        }
        allItems.addAll(TRASH_CATEGORIES.values());

        Map<String, NhomMatHang> itemMap = new HashMap<>();
        for (NhomMatHang nh : allItems) {
            if (nh.getId() != null) {
                itemMap.put(nh.getId().trim(), nh);
            }
        }

        String currParent = null;
        NhomMatHang current = itemMap.get(childId.trim());
        if (current != null) {
            currParent = current.getParentId();
        }

        while (currParent != null && !currParent.trim().isEmpty() && !ancestors.contains(currParent.trim())) {
            String pId = currParent.trim();
            ancestors.add(pId);
            NhomMatHang parentObj = itemMap.get(pId);
            if (parentObj != null) {
                currParent = parentObj.getParentId();
            } else {
                break;
            }
        }

        return ancestors;
    }

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        Set<String> idsToTrash = getAllDescendantIds(id);

        for (String tid : idsToTrash) {
            DELETED_CATEGORY_IDS.add(tid);
            NhomMatHang found = findById(tid);
            if (found != null) {
                found.setStatus(-1);
                TRASH_CATEGORIES.put(tid, found);
                if (found.getName() != null) {
                    DELETED_CATEGORY_NAMES.add(found.getName().trim().toLowerCase());
                }
            }
        }

        // Cập nhật CSDL
        tryUpdateStatusInDb(idsToTrash, -1);

        for (NhomMatHang nh : MEMORY_CATEGORIES) {
            if (idsToTrash.contains(nh.getId())) {
                nh.setStatus(-1);
            }
        }
        audit("Nhóm mặt hàng", "XÓA", id, "Chuyển nhóm mặt hàng và các nhóm con vào thùng rác");
        return true;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        Set<String> idsToRestore = getAllDescendantIds(id);
        Set<String> ancestorIds = getAllAncestorIds(id);
        idsToRestore.addAll(ancestorIds);

        for (String rid : idsToRestore) {
            DELETED_CATEGORY_IDS.remove(rid);
            NhomMatHang item = TRASH_CATEGORIES.remove(rid);
            if (item == null) {
                item = findById(rid);
            }
            if (item != null) {
                item.setStatus(1);
                if (item.getName() != null) {
                    DELETED_CATEGORY_NAMES.remove(item.getName().trim().toLowerCase());
                }
            }
        }

        tryUpdateStatusInDb(idsToRestore, 30);

        for (NhomMatHang nh : MEMORY_CATEGORIES) {
            if (idsToRestore.contains(nh.getId())) {
                nh.setStatus(1);
            }
        }
        audit("Nhóm mặt hàng", "KHÔI PHỤC", id, "Khôi phục nhóm mặt hàng và các nhóm liên quan");
        return true;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        Set<String> idsToDelete = getAllDescendantIds(id);

        tryDeleteFromDb(idsToDelete);

        for (String did : idsToDelete) {
            TRASH_CATEGORIES.remove(did);
            DELETED_CATEGORY_IDS.remove(did);
            NhomMatHang nh = findById(did);
            if (nh != null && nh.getName() != null) {
                DELETED_CATEGORY_NAMES.remove(nh.getName().trim().toLowerCase());
            }
        }
        MEMORY_CATEGORIES.removeIf(nh -> idsToDelete.contains(nh.getId()));
        audit("Nhóm mặt hàng", "XÓA VĨNH VIỄN", id, "Xóa vĩnh viễn nhóm mặt hàng và các nhóm con");
        return true;
    }

    public boolean updateParentAndOrder(String id, String newParentId, int newSortOrder) {
        if (id == null) return false;
        for (NhomMatHang nh : MEMORY_CATEGORIES) {
            if (id.equals(nh.getId())) {
                nh.setParentId(newParentId);
                nh.setSortOrder(newSortOrder);
                break;
            }
        }
        NhomMatHang tr = TRASH_CATEGORIES.get(id);
        if (tr != null) {
            tr.setParentId(newParentId);
            tr.setSortOrder(newSortOrder);
        }

        String sql = "UPDATE DNHOMMATHANG SET PARENTID = ?, SORTORDER = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newParentId);
            ps.setInt(2, newSortOrder);
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            String sqlFallback = "UPDATE DNHOMMATHANG SET PARENTID = ? WHERE ID = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlFallback)) {
                ps.setString(1, newParentId);
                ps.setString(2, id);
                return ps.executeUpdate() > 0;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    public boolean insert(NhomMatHang nh) {
        if (nh == null) return false;
        if (nh.getId() == null || nh.getId().trim().isEmpty()) {
            nh.setId(UUID.randomUUID().toString());
        }
        if (nh.getCode() == null || nh.getCode().trim().isEmpty()) {
            nh.setCode(nh.getName() != null && nh.getName().length() >= 2 ? nh.getName().substring(0, 2).toUpperCase() : "NH");
        }

        if (nh.getId() != null) {
            DELETED_CATEGORY_IDS.remove(nh.getId());
            TRASH_CATEGORIES.remove(nh.getId());
        }
        if (nh.getName() != null) DELETED_CATEGORY_NAMES.remove(nh.getName().trim().toLowerCase());

        boolean exists = false;
        for (int i = 0; i < MEMORY_CATEGORIES.size(); i++) {
            if (nh.getId().equals(MEMORY_CATEGORIES.get(i).getId())) {
                MEMORY_CATEGORIES.set(i, nh);
                exists = true;
                break;
            }
        }
        if (!exists) {
            MEMORY_CATEGORIES.add(0, nh);
        }

        tryInsertToDb(nh);
        audit("Nhóm mặt hàng", "THÊM", nh.getName(), "Thêm nhóm mặt hàng");
        return true;
    }

    public boolean update(NhomMatHang nh) {
        if (nh == null || nh.getId() == null) return false;
        for (int i = 0; i < MEMORY_CATEGORIES.size(); i++) {
            if (nh.getId().equals(MEMORY_CATEGORIES.get(i).getId())) {
                MEMORY_CATEGORIES.set(i, nh);
                break;
            }
        }
        tryUpdateToDb(nh);
        audit("Nhóm mặt hàng", "SỬA", nh.getName(), "Cập nhật nhóm mặt hàng");
        return true;
    }

    public boolean delete(String id) {
        return moveToTrash(id);
    }

    public boolean delete(String id, String name) {
        if (name != null) {
            DELETED_CATEGORY_NAMES.add(name.trim().toLowerCase());
        }
        return moveToTrash(id);
    }

    private NhomMatHang mapResultSet(ResultSet rs, boolean hasExtraColumns) throws SQLException {
        NhomMatHang nh = new NhomMatHang();
        nh.setId(rs.getString("ID"));
        nh.setCode(rs.getString("CODE"));
        nh.setName(rs.getString("NAME"));
        nh.setGhiChu(rs.getString("NOTE"));

        int statusVal = rs.getObject("STATUS") != null ? rs.getInt("STATUS") : 30;
        nh.setStatus(statusVal >= 0 ? 1 : -1);

        if (hasExtraColumns) {
            try {
                nh.setParentId(rs.getString("PARENTID"));
            } catch (Exception ignored) {}
            try {
                Integer itemType = rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0;
                nh.setItemType(itemType);
            } catch (Exception ignored) {}
            try {
                Integer sortOrder = rs.getObject("SORTORDER") != null ? rs.getInt("SORTORDER") : 0;
                nh.setSortOrder(sortOrder);
            } catch (Exception ignored) {}
        }

        if (nh.isFolder()) {
            nh.setIconName("Folder.png");
        } else if (nh.isSeparator()) {
            nh.setIconName(null);
        } else {
            nh.setIconName("1282529873_objects.png");
        }
        return nh;
    }

    private List<NhomMatHang> tryLoadFromDb(int statusFilter) {
        List<NhomMatHang> list = new ArrayList<>();
        String sqlFull = "SELECT ID, CODE, NAME, NOTE, STATUS, PARENTID, ITEMTYPE, SORTORDER FROM DNHOMMATHANG";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFull);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhomMatHang nh = mapResultSet(rs, true);
                if ((statusFilter >= 0 && nh.getStatus() >= 0) || (statusFilter < 0 && nh.getStatus() < 0)) {
                    list.add(nh);
                }
            }
            return list;
        } catch (Exception e1) {
            String sqlMid = "SELECT ID, CODE, NAME, NOTE, STATUS, PARENTID FROM DNHOMMATHANG";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlMid);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhomMatHang nh = mapResultSet(rs, true);
                    if ((statusFilter >= 0 && nh.getStatus() >= 0) || (statusFilter < 0 && nh.getStatus() < 0)) {
                        list.add(nh);
                    }
                }
                return list;
            } catch (Exception e2) {
                String sqlBasic = "SELECT ID, CODE, NAME, NOTE, STATUS FROM DNHOMMATHANG";
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlBasic);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        NhomMatHang nh = mapResultSet(rs, false);
                        if ((statusFilter >= 0 && nh.getStatus() >= 0) || (statusFilter < 0 && nh.getStatus() < 0)) {
                            list.add(nh);
                        }
                    }
                    return list;
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    private List<NhomMatHang> tryLoadAllFromDb() {
        List<NhomMatHang> list = new ArrayList<>();
        String sqlFull = "SELECT ID, CODE, NAME, NOTE, STATUS, PARENTID, ITEMTYPE, SORTORDER FROM DNHOMMATHANG";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFull);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs, true));
            }
            return list;
        } catch (Exception e1) {
            String sqlMid = "SELECT ID, CODE, NAME, NOTE, STATUS, PARENTID FROM DNHOMMATHANG";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlMid);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs, true));
                }
                return list;
            } catch (Exception e2) {
                String sqlBasic = "SELECT ID, CODE, NAME, NOTE, STATUS FROM DNHOMMATHANG";
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlBasic);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs, false));
                    }
                    return list;
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    private boolean tryInsertToDb(NhomMatHang nh) {
        String sqlFull = "INSERT INTO DNHOMMATHANG (ID, CODE, NAME, NOTE, STATUS, USERCREATEDID, TIMECREATED, PARENTID, ITEMTYPE, SORTORDER) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFull)) {
            ps.setString(1, nh.getId());
            ps.setString(2, nh.getCode() != null ? nh.getCode() : (nh.isSeparator() ? "SEP" : (nh.isFolder() ? "TM" : "NH")));
            ps.setString(3, nh.getName() != null ? nh.getName() : (nh.isSeparator() ? "--------------------" : "Thư mục"));
            ps.setString(4, nh.getGhiChu() != null ? nh.getGhiChu() : "");
            ps.setInt(5, nh.getStatus() >= 0 ? 30 : -1);
            ps.setString(6, getEffectiveUserId());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.setString(8, nh.getParentId());
            ps.setInt(9, nh.getItemType() != null ? nh.getItemType() : 0);
            ps.setInt(10, nh.getSortOrder());
            int affected = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return affected > 0;
        } catch (Exception e1) {
            String sqlBasic = "INSERT INTO DNHOMMATHANG (ID, CODE, NAME, NOTE, STATUS, USERCREATEDID, TIMECREATED) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlBasic)) {
                ps.setString(1, nh.getId());
                ps.setString(2, nh.getCode());
                ps.setString(3, nh.getName());
                ps.setString(4, nh.getGhiChu() != null ? nh.getGhiChu() : "");
                ps.setInt(5, nh.getStatus() >= 0 ? 30 : -1);
                ps.setString(6, getEffectiveUserId());
                ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                int affected = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                return affected > 0;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    private boolean tryUpdateToDb(NhomMatHang nh) {
        String sql = "UPDATE DNHOMMATHANG SET CODE=?, NAME=?, NOTE=?, PARENTID=?, ITEMTYPE=?, SORTORDER=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nh.getCode());
            ps.setString(2, nh.getName());
            ps.setString(3, nh.getGhiChu() != null ? nh.getGhiChu() : "");
            ps.setString(4, nh.getParentId());
            ps.setInt(5, nh.getItemType() != null ? nh.getItemType() : 0);
            ps.setInt(6, nh.getSortOrder());
            ps.setString(7, nh.getId());
            int affected = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return affected > 0;
        } catch (Exception e) {
            String sqlBasic = "UPDATE DNHOMMATHANG SET CODE=?, NAME=?, NOTE=? WHERE ID=?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlBasic)) {
                ps.setString(1, nh.getCode());
                ps.setString(2, nh.getName());
                ps.setString(3, nh.getGhiChu() != null ? nh.getGhiChu() : "");
                ps.setString(4, nh.getId());
                int affected = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                return affected > 0;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    private void tryUpdateStatusInDb(Set<String> ids, int newStatus) {
        if (ids == null || ids.isEmpty()) return;
        StringBuilder sql = new StringBuilder("UPDATE DNHOMMATHANG SET STATUS = ? WHERE ID IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, newStatus);
            int p = 2;
            for (String id : ids) {
                ps.setString(p++, id);
            }
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật STATUS DNHOMMATHANG: " + e.getMessage());
        }

        // Đồng thời cập nhật trạng thái trong DMATHANG cho các mặt hàng thuộc nhóm
        StringBuilder sqlMH = new StringBuilder("UPDATE DMATHANG SET STATUS = ? WHERE DNHOMMATHANGID IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sqlMH.append(",");
            sqlMH.append("?");
        }
        sqlMH.append(")");
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMH.toString())) {
            ps.setInt(1, newStatus);
            int p = 2;
            for (String id : ids) {
                ps.setString(p++, id);
            }
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật STATUS DMATHANG: " + e.getMessage());
        }
    }

    private void tryDeleteFromDb(Set<String> ids) {
        if (ids == null || ids.isEmpty()) return;

        // 1. Xóa mặt hàng con trong DMATHANG trước
        StringBuilder sqlMH = new StringBuilder("DELETE FROM DMATHANG WHERE DNHOMMATHANGID IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sqlMH.append(",");
            sqlMH.append("?");
        }
        sqlMH.append(")");
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMH.toString())) {
            int p = 1;
            for (String id : ids) {
                ps.setString(p++, id);
            }
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        // 2. Xóa nhóm mặt hàng trong DNHOMMATHANG
        StringBuilder sql = new StringBuilder("DELETE FROM DNHOMMATHANG WHERE ID IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int p = 1;
            for (String id : ids) {
                ps.setString(p++, id);
            }
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}
    }
}
