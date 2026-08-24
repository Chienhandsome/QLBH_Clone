package kazuko.ex.dao;

import kazuko.ex.model.banhang.DonHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DonHangDAO extends BaseDAO {

    public List<DonHang> getPhieuXuatKho(Date tuNgay, Date denNgay, String khoXuatId) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT d.ID, d.NAME, d.NGAY, d.DIENGIAI, d.TIENHANG, d.TIENGIAMGIA, " +
                "d.TILEGIAMGIA, d.TONGCONG, d.TIENTHANHTOAN, d.CONLAI, d.DKHOXUATID, d.DKHONHAPID, " +
                "d.DNHANVIENXUATID, d.DKHACHHANGID, d.DCUAHANGID, d.LOAI, d.STATUS " +
                "FROM TDONHANG d WHERE 1=1 ");

        if (khoXuatId != null && !khoXuatId.isEmpty()) {
            sql.append("AND d.DKHOXUATID = ? ");
        }
        sql.append("ORDER BY d.TIMECREATED DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (khoXuatId != null && !khoXuatId.isEmpty()) {
                ps.setString(paramIndex++, khoXuatId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonHang dh = new DonHang();
                    dh.setId(rs.getString("ID"));
                    dh.setName(rs.getString("NAME"));
                    dh.setNgay(rs.getTimestamp("NGAY"));
                    dh.setDienGiai(rs.getString("DIENGIAI"));
                    dh.setTienHang(rs.getDouble("TIENHANG"));
                    dh.setTienGiamGia(rs.getDouble("TIENGIAMGIA"));
                    dh.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    dh.setTongCong(rs.getDouble("TONGCONG"));
                    dh.setTienThanhToan(rs.getDouble("TIENTHANHTOAN"));
                    dh.setConLai(rs.getDouble("CONLAI"));
                    dh.setKhoXuatId(rs.getString("DKHOXUATID"));
                    dh.setKhoNhapId(rs.getString("DKHONHAPID"));
                    dh.setNhanVienXuatId(rs.getString("DNHANVIENXUATID"));
                    dh.setKhachHangId(rs.getString("DKHACHHANGID"));
                    dh.setCuaHangId(rs.getString("DCUAHANGID"));
                    dh.setLoai(rs.getInt("LOAI"));
                    dh.setStatus(rs.getInt("STATUS"));
                    list.add(dh);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách phiếu xuất kho: " + e.getMessage());
        }
        return list;
    }
}
