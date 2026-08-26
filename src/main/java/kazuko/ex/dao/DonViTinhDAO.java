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
        MEMORY_UNITS.add(new DonViTinh("DVT1", "C", "Chiếc"));
        MEMORY_UNITS.add(new DonViTinh("DVT2", "CAI", "Cái"));
        MEMORY_UNITS.add(new DonViTinh("DVT3", "HOP", "Hộp"));
        MEMORY_UNITS.add(new DonViTinh("DVT4", "M", "m"));
        MEMORY_UNITS.add(new DonViTinh("DVT5", "BO", "Bộ"));
        MEMORY_UNITS.add(new DonViTinh("DVT6", "KG", "Kg"));
        MEMORY_UNITS.add(new DonViTinh("DVT7", "THUNG", "Thùng"));
        MEMORY_UNITS.add(new DonViTinh("DVT8", "GOI", "Gói"));
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
