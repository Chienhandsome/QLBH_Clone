package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NhomNhaCungCapDAO {

    public List<NhomNhaCungCap> getAll() {
        List<NhomNhaCungCap> list = new ArrayList<>();
        String sql = "SELECT * FROM DNHOMNHACUNGCAP WHERE (STATUS IS NULL OR STATUS >= 0) ORDER BY NAME ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NhomNhaCungCap n = new NhomNhaCungCap();
                n.setId(rs.getString("ID"));
                n.setName(rs.getString("NAME"));
                n.setNote(rs.getString("NOTE"));
                n.setStatus(rs.getInt("STATUS"));
                n.setParentId(rs.getString("PARENTID"));
                n.setSortOrder(rs.getString("SORTORDER"));
                list.add(n);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAll NhomNhaCungCap: " + e.getMessage());
        }
        return list;
    }

    private String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null) {
            return AuthService.getCurrentUser().getId();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public boolean insert(NhomNhaCungCap nhom) {
        if (nhom == null) return false;
        if (nhom.getId() == null || nhom.getId().trim().isEmpty()) {
            nhom.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO DNHOMNHACUNGCAP (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhom.getId());
            ps.setString(2, nhom.getName());
            ps.setString(3, nhom.getNote());
            ps.setString(4, getEffectiveUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insert NhomNhaCungCap: " + e.getMessage());
            return false;
        }
    }

    public boolean update(NhomNhaCungCap nhom) {
        if (nhom == null || nhom.getId() == null) return false;
        String sql = "UPDATE DNHOMNHACUNGCAP SET NAME = ?, NOTE = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhom.getName());
            ps.setString(2, nhom.getNote());
            ps.setString(3, getEffectiveUserId());
            ps.setString(4, nhom.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi update NhomNhaCungCap: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        if (id == null) return false;
        String sql = "UPDATE DNHOMNHACUNGCAP SET STATUS = -1, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getEffectiveUserId());
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi delete NhomNhaCungCap: " + e.getMessage());
            return false;
        }
    }
}
