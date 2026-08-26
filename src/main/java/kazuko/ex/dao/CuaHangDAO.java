package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class CuaHangDAO extends BaseDAO {
    // Bộ nhớ đệm mô phỏng sẵn dữ liệu mặc định
    private static final List<CuaHang> MEMORY_STORES = new CopyOnWriteArrayList<>();
    private static String defaultStoreId = "CH2";

    static {
        CuaHang cn1 = new CuaHang("CH1", "CH1", "chi nhánh 1");
        cn1.setDiaChi("123 abc");
        cn1.setDienThoai("0979123456");
        cn1.setKhoHangId("1");
        cn1.setKhoHangName("KHO BÁN HÀNG");
        cn1.setMauHoaDon("Mẫu 54 mm x 2 dòng");
        cn1.setGhiChu("note");
        cn1.setIconName("branch");
        cn1.setStatus(1);

        CuaHang testStore = new CuaHang("CH2", "TEST", "TEST");
        testStore.setDiaChi("Hà Nội");
        testStore.setDienThoai("0967041111");
        testStore.setKhoHangId("1");
        testStore.setKhoHangName("KHO BÁN HÀNG");
        testStore.setMauHoaDon("Mẫu 54 mm x 2 dòng");
        testStore.setIconName("branch");
        testStore.setDefault(true);
        testStore.setStatus(1);

        CuaHang truSo = new CuaHang("CH3", "TS", "TRỤ SỞ CHÍNH");
        truSo.setDiaChi("TP. Hồ Chí Minh");
        truSo.setDienThoai("19001234");
        truSo.setKhoHangId("1");
        truSo.setKhoHangName("KHO TỔNG");
        truSo.setMauHoaDon("Mẫu 80 mm (K80)");
        truSo.setIconName("headquarter");
        truSo.setStatus(1);

        CuaHang folderTrash1 = new CuaHang("TR1", "TM1", "Thư mục 1");
        folderTrash1.setFolder(true);
        folderTrash1.setStatus(-1);

        CuaHang folderTrash2 = new CuaHang("TR2", "TM2", "Thư mục 2");
        folderTrash2.setFolder(true);
        folderTrash2.setStatus(-1);

        CuaHang childTrash = new CuaHang("TR3", "ABC", "abc");
        childTrash.setParentId("TR2");
        childTrash.setStatus(-1);

        MEMORY_STORES.add(cn1);
        MEMORY_STORES.add(testStore);
        MEMORY_STORES.add(truSo);
        MEMORY_STORES.add(folderTrash1);
        MEMORY_STORES.add(folderTrash2);
        MEMORY_STORES.add(childTrash);
    }

    public List<CuaHang> getAll() {
        return getAllActive();
    }

    public List<CuaHang> getRealStores() {
        List<CuaHang> all = getAllActive();
        List<CuaHang> result = new ArrayList<>();
        for (CuaHang ch : all) {
            if (!ch.isFolder() && !ch.isSeparator()) {
                result.add(ch);
            }
        }
        return result;
    }

    public List<CuaHang> getAllActive() {
        List<CuaHang> dbList = tryLoadFromDb(1);
        if (dbList != null) {
            for (CuaHang ch : dbList) {
                ch.setDefault(ch.getId().equals(defaultStoreId));
            }
            return dbList;
        }
        List<CuaHang> activeList = new ArrayList<>();
        for (CuaHang ch : MEMORY_STORES) {
            if (ch.getStatus() >= 0) {
                ch.setDefault(ch.getId().equals(defaultStoreId));
                activeList.add(ch);
            }
        }
        return activeList;
    }

    public List<CuaHang> getTrashItems() {
        List<CuaHang> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }
        List<CuaHang> trashList = new ArrayList<>();
        for (CuaHang ch : MEMORY_STORES) {
            if (ch.getStatus() < 0) {
                trashList.add(ch);
            }
        }
        return trashList;
    }

    public CuaHang findById(String id) {
        if (id == null) return null;
        for (CuaHang ch : getAllActive()) {
            if (id.equals(ch.getId())) return ch;
        }
        for (CuaHang ch : getTrashItems()) {
            if (id.equals(ch.getId())) return ch;
        }
        for (CuaHang ch : MEMORY_STORES) {
            if (id.equals(ch.getId())) {
                return ch;
            }
        }
        return null;
    }

    public boolean insert(CuaHang ch) {
        if (ch == null) return false;
        if (ch.getId() == null || ch.getId().trim().isEmpty() || ch.getId().startsWith("CH_")) {
            ch.setId(UUID.randomUUID().toString());
        }
        ch.setStatus(30);
        MEMORY_STORES.add(ch);
        return tryInsertToDb(ch);
    }

    public boolean update(CuaHang ch) {
        if (ch == null || ch.getId() == null) return false;
        boolean dbSuccess = tryUpdateToDb(ch);
        boolean memFound = false;
        for (int i = 0; i < MEMORY_STORES.size(); i++) {
            if (ch.getId().equals(MEMORY_STORES.get(i).getId())) {
                MEMORY_STORES.set(i, ch);
                memFound = true;
                break;
            }
        }
        if (!memFound) {
            MEMORY_STORES.add(ch);
        }
        return dbSuccess || memFound;
    }

    public Set<String> getAllDescendantIds(String rootId) {
        Set<String> allIds = new HashSet<>();
        if (rootId == null || rootId.trim().isEmpty()) return allIds;
        allIds.add(rootId.trim());

        List<CuaHang> allItems = new ArrayList<>();
        List<CuaHang> dbItems = tryLoadAllFromDb();
        if (dbItems != null && !dbItems.isEmpty()) {
            allItems.addAll(dbItems);
        } else {
            allItems.addAll(MEMORY_STORES);
        }

        boolean added;
        do {
            added = false;
            for (CuaHang ch : allItems) {
                if (ch.getParentId() != null && !ch.getParentId().trim().isEmpty() && ch.getId() != null) {
                    if (allIds.contains(ch.getParentId().trim())) {
                        if (allIds.add(ch.getId().trim())) {
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

        List<CuaHang> allItems = new ArrayList<>();
        List<CuaHang> dbItems = tryLoadAllFromDb();
        if (dbItems != null && !dbItems.isEmpty()) {
            allItems.addAll(dbItems);
        } else {
            allItems.addAll(MEMORY_STORES);
        }

        Map<String, CuaHang> itemMap = new HashMap<>();
        for (CuaHang ch : allItems) {
            if (ch.getId() != null) {
                itemMap.put(ch.getId().trim(), ch);
            }
        }

        String currParent = null;
        CuaHang current = itemMap.get(childId.trim());
        if (current != null) {
            currParent = current.getParentId();
        }

        while (currParent != null && !currParent.trim().isEmpty() && !ancestors.contains(currParent.trim())) {
            String pId = currParent.trim();
            ancestors.add(pId);
            CuaHang parentObj = itemMap.get(pId);
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

        // Cập nhật CSDL Firebird: chuyển STATUS = -1 cho toàn bộ cây con
        tryUpdateStatusInDb(idsToTrash, -1);

        // Cập nhật bộ nhớ cache
        for (CuaHang ch : MEMORY_STORES) {
            if (idsToTrash.contains(ch.getId())) {
                ch.setStatus(-1);
            }
        }
        return true;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        Set<String> idsToRestore = getAllDescendantIds(id);

        // Khi khôi phục một mục, khôi phục cả các mục cha/tổ tiên nếu chúng đang trong thùng rác
        Set<String> ancestorIds = getAllAncestorIds(id);
        idsToRestore.addAll(ancestorIds);

        // Cập nhật CSDL Firebird: khôi phục STATUS = 30
        tryUpdateStatusInDb(idsToRestore, 30);

        // Cập nhật bộ nhớ cache
        for (CuaHang ch : MEMORY_STORES) {
            if (idsToRestore.contains(ch.getId())) {
                ch.setStatus(1);
            }
        }
        return true;
    }

    public boolean deletePermanently(String id) throws SQLException {
        if (id == null) return false;
        Set<String> idsToDelete = getAllDescendantIds(id);

        // Xóa khỏi CSDL Firebird
        tryDeleteFromDb(idsToDelete);

        // Xóa khỏi bộ nhớ cache
        MEMORY_STORES.removeIf(ch -> idsToDelete.contains(ch.getId()));
        return true;
    }

    public String getDefaultStoreId() {
        return defaultStoreId;
    }

    public void setDefaultStoreId(String id) {
        defaultStoreId = id;
        for (CuaHang ch : MEMORY_STORES) {
            ch.setDefault(ch.getId().equals(id));
        }
    }

    private String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569"; // Admin mặc định trong CSDL Firebird DEMO.FDB
    }

    private List<CuaHang> tryLoadFromDb(int statusFilter) {
        List<CuaHang> list = new ArrayList<>();
        String sql = "SELECT ID, CODE, NAME, DIACHI, DIENTHOAI, DKHOHANGID, NOTE, STATUS, PARENTID, ITEMTYPE FROM DCUAHANG";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CuaHang ch = new CuaHang();
                ch.setId(rs.getString("ID"));
                ch.setCode(rs.getString("CODE"));
                ch.setName(rs.getString("NAME"));
                ch.setDiaChi(rs.getString("DIACHI"));
                ch.setDienThoai(rs.getString("DIENTHOAI"));
                ch.setKhoHangId(rs.getString("DKHOHANGID"));
                ch.setGhiChu(rs.getString("NOTE"));
                int statusVal = rs.getObject("STATUS") != null ? rs.getInt("STATUS") : 30;
                ch.setStatus(statusVal >= 0 ? 1 : -1);
                ch.setParentId(rs.getString("PARENTID"));
                Integer itemType = rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : null;
                if (itemType != null && itemType == 1) {
                    ch.setFolder(true);
                    ch.setIconName("folder");
                } else if (itemType != null && itemType == 2) {
                    ch.setSeparator(true);
                }

                if ((statusFilter >= 0 && ch.getStatus() >= 0) || (statusFilter < 0 && ch.getStatus() < 0)) {
                    list.add(ch);
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi nạp cửa hàng từ DB: " + e.getMessage());
            return null;
        }
    }

    private boolean tryInsertToDb(CuaHang ch) {
        String sql = "INSERT INTO DCUAHANG (ID, CODE, NAME, DIACHI, DIENTHOAI, DKHOHANGID, NOTE, STATUS, USERCREATEDID, TIMECREATED, PARENTID, ITEMTYPE) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String userId = getEffectiveUserId();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            ps.setString(1, ch.getId());
            ps.setString(2, ch.getCode() != null && !ch.getCode().trim().isEmpty() ? ch.getCode().trim() : (ch.isSeparator() ? "SEP" : (ch.isFolder() ? "TM" : "")));
            ps.setString(3, ch.getName() != null && !ch.getName().trim().isEmpty() ? ch.getName().trim() : (ch.isSeparator() ? "--------------------" : "Thư mục"));
            ps.setString(4, ch.getDiaChi());
            ps.setString(5, ch.getDienThoai());
            ps.setString(6, ch.getKhoHangId());
            ps.setString(7, ch.getGhiChu());
            ps.setInt(8, ch.getStatus() >= 0 ? 30 : -1);
            ps.setString(9, userId);
            ps.setTimestamp(10, now);
            ps.setString(11, ch.getParentId() != null ? ch.getParentId() : "");
            ps.setObject(12, ch.isFolder() ? 1 : (ch.isSeparator() ? 2 : 0));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi insert DCUAHANG: " + e.getMessage());
            return false;
        }
    }

    private boolean tryUpdateToDb(CuaHang ch) {
        String sql = "UPDATE DCUAHANG SET CODE=?, NAME=?, DIACHI=?, DIENTHOAI=?, DKHOHANGID=?, NOTE=?, STATUS=?, PARENTID=?, ITEMTYPE=?, USERMODIFIEDID=?, TIMEMODIFIED=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String userId = getEffectiveUserId();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            ps.setString(1, ch.getCode() != null && !ch.getCode().trim().isEmpty() ? ch.getCode().trim() : "");
            ps.setString(2, ch.getName() != null && !ch.getName().trim().isEmpty() ? ch.getName().trim() : (ch.isSeparator() ? "--------------------" : "Thư mục"));
            ps.setString(3, ch.getDiaChi());
            ps.setString(4, ch.getDienThoai());
            ps.setString(5, ch.getKhoHangId());
            ps.setString(6, ch.getGhiChu());
            ps.setInt(7, ch.getStatus() >= 0 ? 30 : -1);
            ps.setString(8, ch.getParentId() != null ? ch.getParentId() : "");
            ps.setObject(9, ch.isFolder() ? 1 : (ch.isSeparator() ? 2 : 0));
            ps.setString(10, userId);
            ps.setTimestamp(11, now);
            ps.setString(12, ch.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi update DCUAHANG: " + e.getMessage());
            return false;
        }
    }

    private List<CuaHang> tryLoadAllFromDb() {
        List<CuaHang> list = new ArrayList<>();
        String sql = "SELECT ID, CODE, NAME, DIACHI, DIENTHOAI, DKHOHANGID, NOTE, STATUS, PARENTID, ITEMTYPE FROM DCUAHANG";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CuaHang ch = new CuaHang();
                ch.setId(rs.getString("ID"));
                ch.setCode(rs.getString("CODE"));
                ch.setName(rs.getString("NAME"));
                ch.setDiaChi(rs.getString("DIACHI"));
                ch.setDienThoai(rs.getString("DIENTHOAI"));
                ch.setKhoHangId(rs.getString("DKHOHANGID"));
                ch.setGhiChu(rs.getString("NOTE"));
                int statusVal = rs.getObject("STATUS") != null ? rs.getInt("STATUS") : 30;
                ch.setStatus(statusVal >= 0 ? 1 : -1);
                ch.setParentId(rs.getString("PARENTID"));
                Integer itemType = rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : null;
                if (itemType != null && itemType == 1) {
                    ch.setFolder(true);
                    ch.setIconName("folder");
                } else if (itemType != null && itemType == 2) {
                    ch.setSeparator(true);
                }
                list.add(ch);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean tryUpdateStatusInDb(Set<String> ids, int status) {
        if (ids == null || ids.isEmpty()) return false;
        StringBuilder sql = new StringBuilder("UPDATE DCUAHANG SET STATUS = ?, USERMODIFIEDID = ?, TIMEMODIFIED = ? WHERE ID IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            String userId = getEffectiveUserId();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            ps.setInt(1, status >= 0 ? 30 : -1);
            ps.setString(2, userId);
            ps.setTimestamp(3, now);
            int idx = 4;
            for (String itemId : ids) {
                ps.setString(idx++, itemId);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật STATUS DCUAHANG: " + e.getMessage());
            return false;
        }
    }

    public boolean updateParentId(String id, String newParentId) {
        if (id == null) return false;
        String parentIdVal = (newParentId != null && !newParentId.trim().isEmpty()) ? newParentId.trim() : "";

        // Cập nhật bộ nhớ cache
        boolean foundInMem = false;
        for (CuaHang ch : MEMORY_STORES) {
            if (id.equals(ch.getId())) {
                ch.setParentId(parentIdVal);
                foundInMem = true;
                break;
            }
        }

        // Cập nhật CSDL Firebird
        String sql = "UPDATE DCUAHANG SET PARENTID = ?, USERMODIFIEDID = ?, TIMEMODIFIED = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String userId = getEffectiveUserId();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setString(1, parentIdVal);
            ps.setString(2, userId);
            ps.setTimestamp(3, now);
            ps.setString(4, id);
            boolean ok = ps.executeUpdate() > 0;
            return ok || foundInMem;
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật PARENTID DCUAHANG: " + e.getMessage());
            return foundInMem;
        }
    }

    private void tryDeleteFromDb(Set<String> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return;
        StringBuilder sql = new StringBuilder("DELETE FROM DCUAHANG WHERE ID IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (String itemId : ids) {
                ps.setString(idx++, itemId);
            }
            ps.executeUpdate();
        }
    }
}
