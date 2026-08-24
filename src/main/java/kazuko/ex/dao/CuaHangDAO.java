package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.CuaHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class CuaHangDAO extends BaseDAO {
    // Bộ nhớ đệm mô phỏng sẵn dữ liệu mặc định như hình ảnh
    private static final List<CuaHang> MEMORY_STORES = new CopyOnWriteArrayList<>();
    private static String defaultStoreId = "CH2";

    static {
        // Dữ liệu ban đầu tương ứng như ảnh 1:
        // - chi nhánh 1
        // - TEST (thư mục / store)
        // - TRỤ SỞ CHÍNH
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

        // Dữ liệu trong thùng rác mẫu (tương ứng ảnh 4)
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
        if (dbList != null && !dbList.isEmpty()) {
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
        if (dbList != null && !dbList.isEmpty()) {
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
        for (CuaHang ch : MEMORY_STORES) {
            if (id.equals(ch.getId())) {
                return ch;
            }
        }
        return null;
    }

    public boolean insert(CuaHang ch) {
        if (ch == null) return false;
        if (ch.getId() == null || ch.getId().trim().isEmpty()) {
            ch.setId("CH_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        ch.setStatus(1);
        MEMORY_STORES.add(ch);
        tryInsertToDb(ch);
        return true;
    }

    public boolean update(CuaHang ch) {
        if (ch == null || ch.getId() == null) return false;
        for (int i = 0; i < MEMORY_STORES.size(); i++) {
            if (ch.getId().equals(MEMORY_STORES.get(i).getId())) {
                MEMORY_STORES.set(i, ch);
                tryUpdateToDb(ch);
                return true;
            }
        }
        return false;
    }

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        for (CuaHang ch : MEMORY_STORES) {
            if (id.equals(ch.getId()) || id.equals(ch.getParentId())) {
                ch.setStatus(-1);
            }
        }
        return true;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        for (CuaHang ch : MEMORY_STORES) {
            if (id.equals(ch.getId())) {
                ch.setStatus(1);
                // Nếu có parent trong thùng rác, đưa ra root hoặc khôi phục parent
                if (ch.getParentId() != null) {
                    CuaHang parent = findById(ch.getParentId());
                    if (parent != null && parent.getStatus() < 0) {
                        parent.setStatus(1);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        return MEMORY_STORES.removeIf(ch -> id.equals(ch.getId()) || id.equals(ch.getParentId()));
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

    private List<CuaHang> tryLoadFromDb(int statusFilter) {
        List<CuaHang> list = new ArrayList<>();
        String sql = "SELECT ID, CODE, NAME, DIACHI, DIENTHOAI, DKHOHANGID, NOTE, STATUS FROM DCUAHANG";
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
                ch.setStatus(rs.getObject("STATUS") != null ? rs.getInt("STATUS") : 1);

                if ((statusFilter >= 0 && ch.getStatus() >= 0) || (statusFilter < 0 && ch.getStatus() < 0)) {
                    list.add(ch);
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return list;
    }

    private void tryInsertToDb(CuaHang ch) {
        String sql = "INSERT INTO DCUAHANG (ID, CODE, NAME, DIACHI, DIENTHOAI, DKHOHANGID, NOTE, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ch.getId());
            ps.setString(2, ch.getCode());
            ps.setString(3, ch.getName());
            ps.setString(4, ch.getDiaChi());
            ps.setString(5, ch.getDienThoai());
            ps.setString(6, ch.getKhoHangId());
            ps.setString(7, ch.getGhiChu());
            ps.setInt(8, ch.getStatus());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void tryUpdateToDb(CuaHang ch) {
        String sql = "UPDATE DCUAHANG SET CODE=?, NAME=?, DIACHI=?, DIENTHOAI=?, DKHOHANGID=?, NOTE=?, STATUS=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ch.getCode());
            ps.setString(2, ch.getName());
            ps.setString(3, ch.getDiaChi());
            ps.setString(4, ch.getDienThoai());
            ps.setString(5, ch.getKhoHangId());
            ps.setString(6, ch.getGhiChu());
            ps.setInt(7, ch.getStatus());
            ps.setString(8, ch.getId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}
