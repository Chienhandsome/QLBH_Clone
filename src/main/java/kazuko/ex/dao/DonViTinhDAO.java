package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.DonViTinh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DonViTinhDAO extends BaseDAO {
    private static final List<DonViTinh> MEMORY_UNITS = new CopyOnWriteArrayList<>();

    static {
        String[] sampleNames = {
                "bao", "Bịch", "Bộ", "Cái", "Can", "Cặp", "Cây", "chai", "Chiếc", "Chục",
                "Cục", "Dây", "đĩa", "Đôi", "gói", "Gói", "Gram", "Hộp", "Hộp nhỏ", "Hũ",
                "kg", "Lá", "Lọ", "lốc", "ly", "m", "Ml", "phong", "quả", "Sợi", "Suất",
                "Thỏi", "Thư mục 1", "Túi", "Tuýp", "Tuýt"
        };
        int idx = 1;
        for (String name : sampleNames) {
            DonViTinh dv = new DonViTinh("DVT" + idx, "DVT" + idx, name);
            MEMORY_UNITS.add(dv);
            idx++;
        }
    }

    public List<DonViTinh> getAll() {
        List<DonViTinh> dbList = tryLoadFromDb();
        if (dbList != null && !dbList.isEmpty()) {
            return dbList;
        }
        if (dbList != null) {
            return dbList; // Trả về danh sách rỗng từ DB nếu DB đã kết nối nhưng chưa có bản ghi
        }
        return new ArrayList<>(MEMORY_UNITS);
    }

    public DonViTinh findById(String id) {
        if (id == null) return null;
        for (DonViTinh dv : getAll()) {
            if (id.equals(dv.getId())) return dv;
        }
        return null;
    }

    public DonViTinh findByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String trimmed = name.trim().toLowerCase();
        for (DonViTinh dv : getAll()) {
            if (dv.getName() != null && dv.getName().trim().equalsIgnoreCase(trimmed)) {
                return dv;
            }
        }
        return null;
    }

    public boolean existsByName(String name, String excludeId) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmed = name.trim().toLowerCase();
        for (DonViTinh dv : getAll()) {
            if (excludeId != null && excludeId.equalsIgnoreCase(dv.getId())) continue;
            if (dv.getName() != null && dv.getName().trim().equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }

    public boolean insert(DonViTinh dv) {
        if (dv == null || dv.getName() == null || dv.getName().trim().isEmpty()) return false;
        if (dv.getId() == null || dv.getId().trim().isEmpty()) {
            dv.setId("DVT_" + System.currentTimeMillis());
        }
        MEMORY_UNITS.add(dv);
        String sql = "INSERT INTO DDONVITINH (ID, CODE, NAME, NOTE, STATUS) VALUES (?, ?, ?, ?, 1)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dv.getId());
            ps.setString(2, dv.getCode() != null ? dv.getCode() : dv.getId());
            ps.setString(3, dv.getName().trim());
            ps.setString(4, dv.getGhiChu() != null ? dv.getGhiChu() : "");
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception ignored) {
            return true;
        }
    }

    public boolean update(DonViTinh dv) {
        if (dv == null || dv.getId() == null) return false;
        for (DonViTinh item : MEMORY_UNITS) {
            if (dv.getId().equals(item.getId())) {
                item.setName(dv.getName());
                item.setCode(dv.getCode());
                item.setGhiChu(dv.getGhiChu());
                break;
            }
        }
        String sql = "UPDATE DDONVITINH SET NAME = ?, NOTE = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dv.getName());
            ps.setString(2, dv.getGhiChu() != null ? dv.getGhiChu() : "");
            ps.setString(3, dv.getId());
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception ignored) {
            return true;
        }
    }

    public boolean delete(String idOrName) {
        if (idOrName == null) return false;
        MEMORY_UNITS.removeIf(u -> idOrName.equalsIgnoreCase(u.getId()) || idOrName.equalsIgnoreCase(u.getName()));
        String sql = "DELETE FROM DDONVITINH WHERE ID = ? OR NAME = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idOrName);
            ps.setString(2, idOrName);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception ignored) {
            return true;
        }
    }

    private List<DonViTinh> tryLoadFromDb() {
        List<DonViTinh> list = new ArrayList<>();
        
        // 1. Thử truy vấn đầy đủ kèm lọc STATUS
        String sqlFull = "SELECT ID, CODE, NAME, NOTE, STATUS FROM DDONVITINH WHERE (STATUS IS NULL OR STATUS >= 0) ORDER BY NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFull);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DonViTinh dv = new DonViTinh();
                dv.setId(rs.getString("ID"));
                dv.setCode(rs.getString("CODE"));
                dv.setName(rs.getString("NAME"));
                dv.setGhiChu(rs.getString("NOTE"));
                if (rs.getObject("STATUS") != null) {
                    dv.setStatus(rs.getInt("STATUS"));
                }
                list.add(dv);
            }
            return list;
        } catch (Exception e1) {
            // 2. Thử truy vấn cơ bản: ID, CODE, NAME, NOTE
            String sqlMid = "SELECT ID, CODE, NAME, NOTE FROM DDONVITINH ORDER BY NAME";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlMid);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonViTinh dv = new DonViTinh();
                    dv.setId(rs.getString("ID"));
                    dv.setCode(rs.getString("CODE"));
                    dv.setName(rs.getString("NAME"));
                    dv.setGhiChu(rs.getString("NOTE"));
                    list.add(dv);
                }
                return list;
            } catch (Exception e2) {
                // 3. Thử truy vấn tối thiểu: ID, NAME
                String sqlMin = "SELECT ID, NAME FROM DDONVITINH ORDER BY NAME";
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlMin);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DonViTinh dv = new DonViTinh();
                        dv.setId(rs.getString("ID"));
                        dv.setName(rs.getString("NAME"));
                        list.add(dv);
                    }
                    return list;
                } catch (Exception e3) {
                    System.err.println("Không thể tải danh sách ĐVT từ bảng DDONVITINH: " + e3.getMessage());
                    return null;
                }
            }
        }
    }
}
