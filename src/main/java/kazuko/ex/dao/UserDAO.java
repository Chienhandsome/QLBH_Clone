package kazuko.ex.dao;

import kazuko.ex.model.system.SUser;
import kazuko.ex.model.system.UserStoreAccess;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO extends BaseDAO {

    public SUser findByUsername(String username) {
        try {
            return findByUsernameChecked(username);
        } catch (SQLException e) {
            System.err.println("Lỗi tìm người dùng theo username: " + e.getMessage());
            return null;
        }
    }

    public SUser findByUsernameChecked(String username) throws SQLException {
        String sql = "SELECT u.ID, u.USERNAME, u.NAME, u.PASSWORD, u.EMAIL, u.ISADMIN, u.SGROUPUSERID, " +
                     "u.DNHANVIENID, u.USERID, u.CARDCODE, u.NOTE, u.SIMAGEID, u.STATUS, " +
                     "g.NAME AS GROUP_NAME, nv.NAME AS NV_NAME " +
                     "FROM SUSER u " +
                     "LEFT JOIN SGROUPUSER g ON u.SGROUPUSERID = g.ID " +
                     "LEFT JOIN DNHANVIEN nv ON u.DNHANVIENID = nv.ID " +
                     "WHERE UPPER(TRIM(u.USERNAME)) = UPPER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public SUser findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT u.ID, u.USERNAME, u.NAME, u.PASSWORD, u.EMAIL, u.ISADMIN, u.SGROUPUSERID, " +
                     "u.DNHANVIENID, u.USERID, u.CARDCODE, u.NOTE, u.SIMAGEID, u.STATUS, " +
                     "g.NAME AS GROUP_NAME, nv.NAME AS NV_NAME " +
                     "FROM SUSER u " +
                     "LEFT JOIN SGROUPUSER g ON u.SGROUPUSERID = g.ID " +
                     "LEFT JOIN DNHANVIEN nv ON u.DNHANVIENID = nv.ID " +
                     "WHERE u.ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm người dùng theo ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updatePassword(String userId, String newPassword) {
        String sql = "UPDATE SUSER SET PASSWORD = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật mật khẩu: " + e.getMessage());
            return false;
        }
    }

    public List<SUser> getAll() {
        List<SUser> list = new ArrayList<>();
        String sql = "SELECT u.ID, u.USERNAME, u.NAME, u.PASSWORD, u.EMAIL, u.ISADMIN, u.SGROUPUSERID, " +
                     "u.DNHANVIENID, u.USERID, u.CARDCODE, u.NOTE, u.SIMAGEID, u.STATUS, " +
                     "g.NAME AS GROUP_NAME, nv.NAME AS NV_NAME " +
                     "FROM SUSER u " +
                     "LEFT JOIN SGROUPUSER g ON u.SGROUPUSERID = g.ID " +
                     "LEFT JOIN DNHANVIEN nv ON u.DNHANVIENID = nv.ID " +
                     "WHERE u.STATUS >= 0 " +
                     "ORDER BY u.NAME, u.USERNAME";
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

    /**
     * Lấy danh sách người dùng không bao gồm tài khoản Admin (đúng theo quy tắc UI)
     */
    public List<SUser> getAllNonAdminUsers() {
        List<SUser> list = new ArrayList<>();
        String sql = "SELECT u.ID, u.USERNAME, u.NAME, u.PASSWORD, u.EMAIL, u.ISADMIN, u.SGROUPUSERID, " +
                     "u.DNHANVIENID, u.USERID, u.CARDCODE, u.NOTE, u.SIMAGEID, u.STATUS, " +
                     "g.NAME AS GROUP_NAME, nv.NAME AS NV_NAME " +
                     "FROM SUSER u " +
                     "LEFT JOIN SGROUPUSER g ON u.SGROUPUSERID = g.ID " +
                     "LEFT JOIN DNHANVIEN nv ON u.DNHANVIENID = nv.ID " +
                     "WHERE (u.ISADMIN IS NULL OR u.ISADMIN <> 1) " +
                     "  AND UPPER(TRIM(COALESCE(u.USERNAME, ''))) <> 'ADMIN' " +
                     "  AND u.STATUS >= 0 " +
                     "ORDER BY u.NAME, u.USERNAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách người dùng không admin: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách người dùng theo Nhóm (loại trừ Admin)
     */
    public List<SUser> getUsersByGroupId(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return getAllNonAdminUsers();
        }
        List<SUser> list = new ArrayList<>();
        String sql = "SELECT u.ID, u.USERNAME, u.NAME, u.PASSWORD, u.EMAIL, u.ISADMIN, u.SGROUPUSERID, " +
                     "u.DNHANVIENID, u.USERID, u.CARDCODE, u.NOTE, u.SIMAGEID, u.STATUS, " +
                     "g.NAME AS GROUP_NAME, nv.NAME AS NV_NAME " +
                     "FROM SUSER u " +
                     "LEFT JOIN SGROUPUSER g ON u.SGROUPUSERID = g.ID " +
                     "LEFT JOIN DNHANVIEN nv ON u.DNHANVIENID = nv.ID " +
                     "WHERE u.SGROUPUSERID = ? " +
                     "  AND (u.ISADMIN IS NULL OR u.ISADMIN <> 1) " +
                     "  AND UPPER(TRIM(COALESCE(u.USERNAME, ''))) <> 'ADMIN' " +
                     "  AND u.STATUS >= 0 " +
                     "ORDER BY u.NAME, u.USERNAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách người dùng theo nhóm: " + e.getMessage());
        }
        return list;
    }

    public boolean insertUser(SUser user, List<String> storeIds) {
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "INSERT INTO SUSER (ID, USERNAME, NAME, PASSWORD, EMAIL, SGROUPUSERID, DNHANVIENID, " +
                     "USERID, CARDCODE, NOTE, SIMAGEID, ISADMIN, STATUS, TIMECREATED, USERCREATEDID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user.getId());
                    ps.setString(2, user.getUsername());
                    ps.setString(3, user.getName());
                    ps.setString(4, user.getPassword());
                    ps.setString(5, user.getEmail());
                    ps.setString(6, user.getGroupUserId());
                    ps.setString(7, user.getdNhanVienId());
                    ps.setString(8, user.getUserId());
                    ps.setString(9, user.getCardCode());
                    ps.setString(10, user.getNote());
                    ps.setString(11, user.getSimageId());
                    ps.setObject(12, user.getIsAdmin() != null ? user.getIsAdmin() : 0);
                    ps.setInt(13, user.getStatus() != null ? user.getStatus() : 30);
                    ps.setTimestamp(14, now);
                    ps.setString(15, currentUserId);
                    ps.executeUpdate();
                }

                // Lưu quyền cửa hàng
                saveUserStoreAccessInternal(conn, user.getId(), storeIds, currentUserId, now);

                conn.commit();
                audit("Tài khoản", "THÊM", user.getUsername(),
                        "Thêm tài khoản: " + (user.getName() == null ? "" : user.getName()));
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thêm tài khoản người dùng: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(SUser user, List<String> storeIds) {
        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE SUSER SET USERNAME = ?, NAME = ?, PASSWORD = ?, EMAIL = ?, SGROUPUSERID = ?, " +
                     "DNHANVIENID = ?, USERID = ?, CARDCODE = ?, NOTE = ?, SIMAGEID = ?, " +
                     "TIMEMODIFIED = ?, USERMODIFIEDID = ? WHERE ID = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getName());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, user.getEmail());
                    ps.setString(5, user.getGroupUserId());
                    ps.setString(6, user.getdNhanVienId());
                    ps.setString(7, user.getUserId());
                    ps.setString(8, user.getCardCode());
                    ps.setString(9, user.getNote());
                    ps.setString(10, user.getSimageId());
                    ps.setTimestamp(11, now);
                    ps.setString(12, currentUserId);
                    ps.setString(13, user.getId());
                    ps.executeUpdate();
                }

                // Cập nhật quyền cửa hàng
                saveUserStoreAccessInternal(conn, user.getId(), storeIds, currentUserId, now);

                conn.commit();
                audit("Tài khoản", "SỬA", user.getUsername(),
                        "Cập nhật tài khoản: " + (user.getName() == null ? "" : user.getName()));
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật tài khoản người dùng: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) return false;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Xóa quyền truy cập cửa hàng
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TNGUOIDUNGTHEOCUAHANG WHERE SUSERID = ?")) {
                    ps.setString(1, userId);
                    ps.executeUpdate();
                }

                // Xóa tài khoản
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SUSER WHERE ID = ?")) {
                    ps.setString(1, userId);
                    ps.executeUpdate();
                }

                conn.commit();
                audit("Tài khoản", "XÓA", userId, "Xóa tài khoản người dùng");
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xóa tài khoản người dùng: " + e.getMessage());
            return false;
        }
    }

    public List<UserStoreAccess> getUserStoreAccess(String userId) {
        List<UserStoreAccess> list = new ArrayList<>();
        String sql = "SELECT c.ID AS STORE_ID, c.NAME AS STORE_NAME, c.CODE AS STORE_CODE, " +
                     "       t.ID AS ACCESS_ID, t.STATUS AS ACCESS_STATUS " +
                     "FROM DCUAHANG c " +
                     "LEFT JOIN TNGUOIDUNGTHEOCUAHANG t ON c.ID = t.DCUAHANGID AND t.SUSERID = ? " +
                     "WHERE c.STATUS >= 0 " +
                     "ORDER BY c.SORTORDER, c.NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId != null ? userId : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String storeId = rs.getString("STORE_ID");
                    String storeName = rs.getString("STORE_NAME");
                    String storeCode = rs.getString("STORE_CODE");
                    String accessId = rs.getString("ACCESS_ID");
                    boolean accessed = accessId != null;

                    UserStoreAccess usa = new UserStoreAccess(storeId, storeName, storeCode, accessed);
                    usa.setId(accessId);
                    usa.setUserId(userId);
                    list.add(usa);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách quyền cửa hàng của người dùng: " + e.getMessage());
        }
        return list;
    }

    private void saveUserStoreAccessInternal(Connection conn, String userId, List<String> storeIds, String currentUserId, Timestamp now) throws SQLException {
        // Xóa các quyền cửa hàng cũ
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TNGUOIDUNGTHEOCUAHANG WHERE SUSERID = ?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }

        if (storeIds != null && !storeIds.isEmpty()) {
            String insertSql = "INSERT INTO TNGUOIDUNGTHEOCUAHANG (ID, SUSERID, DCUAHANGID, STATUS, TIMECREATED, USERCREATEDID) " +
                               "VALUES (?, ?, ?, 30, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String sId : storeIds) {
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, userId);
                    ps.setString(3, sId);
                    ps.setTimestamp(4, now);
                    ps.setString(5, currentUserId);
                    ps.executeUpdate();
                }
            }
        }
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
        user.setdNhanVienId(rs.getString("DNHANVIENID"));
        user.setUserId(rs.getString("USERID"));
        user.setCardCode(rs.getString("CARDCODE"));
        user.setNote(rs.getString("NOTE"));
        user.setSimageId(rs.getString("SIMAGEID"));
        user.setStatus(rs.getObject("STATUS") != null ? rs.getInt("STATUS") : 30);

        try {
            user.setGroupUserName(rs.getString("GROUP_NAME"));
        } catch (Exception ignored) {}
        try {
            user.setdNhanVienName(rs.getString("NV_NAME"));
        } catch (Exception ignored) {}

        return user;
    }
}
