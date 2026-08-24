package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.KhoHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhoHangDAO extends BaseDAO {

    public List<KhoHang> getAll() {
        List<KhoHang> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, DCUAHANGID, STATUS FROM DKHOHANG ORDER BY SORTORDER, NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KhoHang kho = new KhoHang();
                kho.setId(rs.getString("ID"));
                kho.setName(rs.getString("NAME"));
                kho.setNote(rs.getString("NOTE"));
                kho.setCuaHangId(rs.getString("DCUAHANGID"));
                kho.setStatus(rs.getInt("STATUS"));
                list.add(kho);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách kho hàng: " + e.getMessage());
        }
        return list;
    }
}
