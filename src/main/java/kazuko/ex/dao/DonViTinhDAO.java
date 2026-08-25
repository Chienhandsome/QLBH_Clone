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
        if (dbList != null) {
            return dbList;
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

    private List<DonViTinh> tryLoadFromDb() {
        List<DonViTinh> list = new ArrayList<>();
        String sql = "SELECT ID, CODE, NAME, NOTE FROM DDONVITINH ORDER BY NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
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
        } catch (Exception e) {
            return null;
        }
    }
}
