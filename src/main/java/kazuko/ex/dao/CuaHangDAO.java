package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        // Cập nhật CSDL Firebird: chuyển STATUS = -1
        tryUpdateStatusInDb(id, -1);

        // Cập nhật bộ nhớ cache
        for (CuaHang ch : MEMORY_STORES) {
            if (id.equals(ch.getId()) || id.equals(ch.getParentId())) {
                ch.setStatus(-1);
            }
        }
        return true;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        // Cập nhật CSDL Firebird: khôi phục STATUS = 30
        tryUpdateStatusInDb(id, 30);

        // Cập nhật bộ nhớ cache
        for (CuaHang ch : MEMORY_STORES) {
            if (id.equals(ch.getId())) {
                ch.setStatus(1);
                if (ch.getParentId() != null) {
                    CuaHang parent = findById(ch.getParentId());
                    if (parent != null && parent.getStatus() < 0) {
                        parent.setStatus(1);
                        tryUpdateStatusInDb(parent.getId(), 30);
                    }
                }
            }
        }
        return true;
    }

    public boolean deletePermanently(String id) throws SQLException {
        if (id == null) return false;
        // Xóa khỏi CSDL Firebird (ném ngoại lệ nếu vi phạm ràng buộc khóa ngoại)
        tryDeleteFromDb(id);

        // Xóa khỏi bộ nhớ cache
        MEMORY_STORES.removeIf(ch -> id.equals(ch.getId()) || id.equals(ch.getParentId()));
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
            ps.setString(2, ch.getCode());
            ps.setString(3, ch.getName());
            ps.setString(4, ch.getDiaChi());
            ps.setString(5, ch.getDienThoai());
            ps.setString(6, ch.getKhoHangId());
            ps.setString(7, ch.getGhiChu());
            ps.setInt(8, ch.getStatus() >= 0 ? 30 : -1);
            ps.setString(9, userId);
            ps.setTimestamp(10, now);
            ps.setString(11, ch.getParentId() != null ? ch.getParentId() : "");
            ps.setObject(12, ch.isFolder() ? 1 : 0);
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

            ps.setString(1, ch.getCode());
            ps.setString(2, ch.getName());
            ps.setString(3, ch.getDiaChi());
            ps.setString(4, ch.getDienThoai());
            ps.setString(5, ch.getKhoHangId());
            ps.setString(6, ch.getGhiChu());
            ps.setInt(7, ch.getStatus() >= 0 ? 30 : -1);
            ps.setString(8, ch.getParentId() != null ? ch.getParentId() : "");
            ps.setObject(9, ch.isFolder() ? 1 : 0);
            ps.setString(10, userId);
            ps.setTimestamp(11, now);
            ps.setString(12, ch.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi update DCUAHANG: " + e.getMessage());
            return false;
        }
    }

    private boolean tryUpdateStatusInDb(String id, int status) {
        String sql = "UPDATE DCUAHANG SET STATUS = ?, USERMODIFIEDID = ?, TIMEMODIFIED = ? WHERE ID = ? OR PARENTID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String userId = getEffectiveUserId();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            ps.setInt(1, status >= 0 ? 30 : -1);
            ps.setString(2, userId);
            ps.setTimestamp(3, now);
            ps.setString(4, id);
            ps.setString(5, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật STATUS DCUAHANG: " + e.getMessage());
            return false;
        }
    }

    private void tryDeleteFromDb(String id) throws SQLException {
        String sql = "DELETE FROM DCUAHANG WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }
}
