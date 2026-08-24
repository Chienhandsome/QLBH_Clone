package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.MatHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatHangDAO extends BaseDAO {

    public List<MatHang> getAll() {
        List<MatHang> list = new ArrayList<>();
        String sql = "SELECT m.ID, m.CODE, m.NAME, m.GIANHAP, m.GIABAN, m.GIAVON, m.DNHOMMATHANGID, m.DDONVITINHID, " +
                     "n.NAME as TENNHOM, d.NAME as TENDONVITINH " +
                     "FROM DMATHANG m " +
                     "LEFT JOIN DNHOMMATHANG n ON m.DNHOMMATHANGID = n.ID " +
                     "LEFT JOIN DDONVITINH d ON m.DDONVITINHID = d.ID " +
                     "ORDER BY m.NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MatHang mh = new MatHang();
                mh.setId(rs.getString("ID"));
                mh.setCode(rs.getString("CODE"));
                mh.setName(rs.getString("NAME"));
                mh.setGiaNhap(rs.getDouble("GIANHAP"));
                mh.setGiaBan(rs.getDouble("GIABAN"));
                mh.setGiaVon(rs.getDouble("GIAVON"));
                mh.setNhomMatHangId(rs.getString("DNHOMMATHANGID"));
                mh.setDonViTinhId(rs.getString("DDONVITINHID"));
                mh.setTenNhom(rs.getString("TENNHOM"));
                mh.setTenDonViTinh(rs.getString("TENDONVITINH"));
                list.add(mh);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách mặt hàng: " + e.getMessage());
        }
        return list;
    }
}
