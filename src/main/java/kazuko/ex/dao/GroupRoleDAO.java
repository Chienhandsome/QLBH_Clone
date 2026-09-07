package kazuko.ex.dao;

import kazuko.ex.model.system.SGroupRole;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;

public class GroupRoleDAO extends BaseDAO {

    public Map<String, SGroupRole> getRolesMapByGroupId(String groupId) {
        Map<String, SGroupRole> map = new HashMap<>();
        if (groupId == null || groupId.trim().isEmpty()) return map;

        String sql = "SELECT ID, SGROUPUSERID, SFUNCTIONID, MODE, STATUS FROM SGROUPROLE WHERE SGROUPUSERID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SGroupRole role = new SGroupRole();
                    role.setId(rs.getString("ID"));
                    role.setGroupUserId(rs.getString("SGROUPUSERID"));
                    role.setFunctionId(rs.getString("SFUNCTIONID"));
                    role.setMode(rs.getInt("MODE"));
                    role.setStatus(rs.getInt("STATUS"));
                    map.put(role.getFunctionId(), role);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy ma trận quyền SGROUPROLE: " + e.getMessage());
        }
        return map;
    }

    public boolean saveOrUpdateRole(String groupId, String functionId, int mode) {
        if (groupId == null || functionId == null) return false;

        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String checkSql = "SELECT ID FROM SGROUPROLE WHERE SGROUPUSERID = ? AND SFUNCTIONID = ?";
        try (Connection conn = getConnection()) {
            String existingId = null;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, groupId);
                psCheck.setString(2, functionId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getString("ID");
                    }
                }
            }

            if (existingId != null) {
                String updateSql = "UPDATE SGROUPROLE SET MODE = ?, STATUS = 30, TIMEMODIFIED = ?, USERMODIFIEDID = ? WHERE ID = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, mode);
                    psUpdate.setTimestamp(2, now);
                    psUpdate.setString(3, currentUserId);
                    psUpdate.setString(4, existingId);
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO SGROUPROLE (ID, SGROUPUSERID, SFUNCTIONID, MODE, STATUS, TIMECREATED, USERCREATEDID) " +
                                   "VALUES (?, ?, ?, ?, 30, ?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setString(1, UUID.randomUUID().toString());
                    psInsert.setString(2, groupId);
                    psInsert.setString(3, functionId);
                    psInsert.setInt(4, mode);
                    psInsert.setTimestamp(5, now);
                    psInsert.setString(6, currentUserId);
                    return psInsert.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lưu quyền SGROUPROLE: " + e.getMessage());
            return false;
        }
    }

    public boolean setAllRolesForGroup(String groupId, int mode, List<String> functionIds) {
        if (groupId == null || functionIds == null || functionIds.isEmpty()) return false;

        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String funcId : functionIds) {
                    String checkSql = "SELECT ID FROM SGROUPROLE WHERE SGROUPUSERID = ? AND SFUNCTIONID = ?";
                    String existingId = null;
                    try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                        ps.setString(1, groupId);
                        ps.setString(2, funcId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                existingId = rs.getString("ID");
                            }
                        }
                    }

                    if (existingId != null) {
                        try (PreparedStatement ps = conn.prepareStatement("UPDATE SGROUPROLE SET MODE = ?, STATUS = 30, TIMEMODIFIED = ?, USERMODIFIEDID = ? WHERE ID = ?")) {
                            ps.setInt(1, mode);
                            ps.setTimestamp(2, now);
                            ps.setString(3, currentUserId);
                            ps.setString(4, existingId);
                            ps.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO SGROUPROLE (ID, SGROUPUSERID, SFUNCTIONID, MODE, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, ?, 30, ?, ?)")) {
                            ps.setString(1, UUID.randomUUID().toString());
                            ps.setString(2, groupId);
                            ps.setString(3, funcId);
                            ps.setInt(4, mode);
                            ps.setTimestamp(5, now);
                            ps.setString(6, currentUserId);
                            ps.executeUpdate();
                        }
                    }
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật hàng loạt SGROUPROLE: " + e.getMessage());
            return false;
        }
    }
}
