package kazuko.ex.dao;

import kazuko.ex.model.system.SUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends BaseDAO {

    public SUser findByUsername(String username) {
        String sql = "SELECT ID, USERNAME, NAME, PASSWORD, EMAIL, ISADMIN, SGROUPUSERID FROM SUSER WHERE UPPER(TRIM(USERNAME)) = UPPER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm người dùng theo username: " + e.getMessage());
        }
        return null;
    }

    public List<SUser> getAll() {
        List<SUser> list = new ArrayList<>();
        String sql = "SELECT ID, USERNAME, NAME, PASSWORD, EMAIL, ISADMIN, SGROUPUSERID FROM SUSER ORDER BY NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách người dùng: " + e.getMessage());
        }
        return list;
    }

    private SUser mapResultSetToUser(ResultSet rs) throws SQLException {
        SUser user = new SUser();
        user.setId(rs.getString("ID"));
        user.setUsername(rs.getString("USERNAME"));
        user.setName(rs.getString("NAME"));
        user.setPassword(rs.getString("PASSWORD"));
        user.setEmail(rs.getString("EMAIL"));
        user.setIsAdmin(rs.getObject("ISADMIN") != null ? rs.getInt("ISADMIN") : 0);
        user.setGroupUserId(rs.getString("SGROUPUSERID"));
        return user;
    }
}
