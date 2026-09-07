package kazuko.ex.dao;

import kazuko.ex.model.system.SReportRole;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;

public class ReportRoleDAO extends BaseDAO {

    public Map<String, SReportRole> getReportRolesMapByGroupId(String groupId) {
        Map<String, SReportRole> map = new HashMap<>();
        if (groupId == null || groupId.trim().isEmpty()) return map;

        String sql = "SELECT ID, SGROUPUSERID, SREPORTID, MODE, STATUS FROM SREPORTROLE WHERE SGROUPUSERID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SReportRole role = new SReportRole();
                    role.setId(rs.getString("ID"));
                    role.setGroupUserId(rs.getString("SGROUPUSERID"));
                    role.setReportId(rs.getString("SREPORTID"));
                    role.setMode(rs.getInt("MODE"));
                    role.setStatus(rs.getInt("STATUS"));
                    map.put(role.getReportId(), role);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy quyền báo cáo SREPORTROLE: " + e.getMessage());
        }
        return map;
    }

    public boolean saveOrUpdateReportRole(String groupId, String reportId, int mode) {
        if (groupId == null || reportId == null) return false;

        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String checkSql = "SELECT ID FROM SREPORTROLE WHERE SGROUPUSERID = ? AND SREPORTID = ?";
        try (Connection conn = getConnection()) {
            String existingId = null;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, groupId);
                psCheck.setString(2, reportId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getString("ID");
                    }
                }
            }

            if (existingId != null) {
                String updateSql = "UPDATE SREPORTROLE SET MODE = ?, STATUS = 30, TIMEMODIFIED = ?, USERMODIFIEDID = ? WHERE ID = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, mode);
                    psUpdate.setTimestamp(2, now);
                    psUpdate.setString(3, currentUserId);
                    psUpdate.setString(4, existingId);
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO SREPORTROLE (ID, SGROUPUSERID, SREPORTID, MODE, STATUS, TIMECREATED, USERCREATEDID) " +
                                   "VALUES (?, ?, ?, ?, 30, ?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setString(1, UUID.randomUUID().toString());
                    psInsert.setString(2, groupId);
                    psInsert.setString(3, reportId);
                    psInsert.setInt(4, mode);
                    psInsert.setTimestamp(5, now);
                    psInsert.setString(6, currentUserId);
                    return psInsert.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lưu quyền báo cáo SREPORTROLE: " + e.getMessage());
            return false;
        }
    }

    public boolean setAllReportRoles(String groupId, int mode, List<String> reportIds) {
        if (groupId == null || reportIds == null || reportIds.isEmpty()) return false;

        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String repId : reportIds) {
                    String checkSql = "SELECT ID FROM SREPORTROLE WHERE SGROUPUSERID = ? AND SREPORTID = ?";
                    String existingId = null;
                    try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                        ps.setString(1, groupId);
                        ps.setString(2, repId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                existingId = rs.getString("ID");
                            }
                        }
                    }

                    if (existingId != null) {
                        try (PreparedStatement ps = conn.prepareStatement("UPDATE SREPORTROLE SET MODE = ?, STATUS = 30, TIMEMODIFIED = ?, USERMODIFIEDID = ? WHERE ID = ?")) {
                            ps.setInt(1, mode);
                            ps.setTimestamp(2, now);
                            ps.setString(3, currentUserId);
                            ps.setString(4, existingId);
                            ps.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO SREPORTROLE (ID, SGROUPUSERID, SREPORTID, MODE, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, ?, 30, ?, ?)")) {
                            ps.setString(1, UUID.randomUUID().toString());
                            ps.setString(2, groupId);
                            ps.setString(3, repId);
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
            System.err.println("Lỗi cập nhật hàng loạt SREPORTROLE: " + e.getMessage());
            return false;
        }
    }
}
