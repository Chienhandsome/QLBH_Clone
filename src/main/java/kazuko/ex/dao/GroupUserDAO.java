package kazuko.ex.dao;

import kazuko.ex.model.system.SGroupUser;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupUserDAO extends BaseDAO {

    public List<SGroupUser> getAllGroups() {
        List<SGroupUser> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, SIMAGEID, SORTORDER, PARENTID, ITEMTYPE, STATUS FROM SGROUPUSER WHERE STATUS >= 0 ORDER BY SORTORDER, NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SGroupUser group = new SGroupUser();
                group.setId(rs.getString("ID"));
                group.setName(rs.getString("NAME"));
                group.setNote(rs.getString("NOTE"));
                group.setSimageId(rs.getString("SIMAGEID"));
                group.setSortOrder(rs.getString("SORTORDER"));
                group.setParentId(rs.getString("PARENTID"));
                group.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                group.setStatus(rs.getInt("STATUS"));
                list.add(group);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách nhóm người dùng: " + e.getMessage());
        }
        return list;
    }

    public SGroupUser getGroupById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT ID, NAME, NOTE, SIMAGEID, SORTORDER, PARENTID, ITEMTYPE, STATUS FROM SGROUPUSER WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SGroupUser group = new SGroupUser();
                    group.setId(rs.getString("ID"));
                    group.setName(rs.getString("NAME"));
                    group.setNote(rs.getString("NOTE"));
                    group.setSimageId(rs.getString("SIMAGEID"));
                    group.setSortOrder(rs.getString("SORTORDER"));
                    group.setParentId(rs.getString("PARENTID"));
                    group.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                    group.setStatus(rs.getInt("STATUS"));
                    return group;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm nhóm người dùng: " + e.getMessage());
        }
        return null;
    }

    public boolean insertGroup(SGroupUser group) {
        if (group.getId() == null || group.getId().trim().isEmpty()) {
            group.setId(UUID.randomUUID().toString());
        }
        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "INSERT INTO SGROUPUSER (ID, NAME, NOTE, SIMAGEID, SORTORDER, PARENTID, ITEMTYPE, STATUS, TIMECREATED, USERCREATEDID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group.getId());
            ps.setString(2, group.getName());
            ps.setString(3, group.getNote());
            ps.setString(4, group.getSimageId());
            ps.setString(5, group.getSortOrder());
            ps.setString(6, group.getParentId());
            ps.setObject(7, group.getItemType());
            ps.setInt(8, group.getStatus() != null ? group.getStatus() : 30);
            ps.setTimestamp(9, now);
            ps.setString(10, currentUserId);
            boolean success = ps.executeUpdate() > 0;
            if (success) audit("Nhóm người dùng", "THÊM", group.getName(), "Thêm nhóm người dùng");
            return success;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm nhóm người dùng: " + e.getMessage());
            return false;
        }
    }

    public boolean updateGroup(SGroupUser group) {
        String currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : "";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE SGROUPUSER SET NAME = ?, NOTE = ?, SIMAGEID = ?, SORTORDER = ?, TIMEMODIFIED = ?, USERMODIFIEDID = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getNote());
            ps.setString(3, group.getSimageId());
            ps.setString(4, group.getSortOrder());
            ps.setTimestamp(5, now);
            ps.setString(6, currentUserId);
            ps.setString(7, group.getId());
            boolean success = ps.executeUpdate() > 0;
            if (success) audit("Nhóm người dùng", "SỬA", group.getName(), "Cập nhật nhóm người dùng");
            return success;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật nhóm người dùng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa nhóm người dùng và toàn bộ dữ liệu phụ thuộc (Cascade Delete):
     * - Xóa phân quyền trong SGROUPROLE & SREPORTROLE của nhóm
     * - Xóa tài khoản người dùng trong SUSER thuộc nhóm (và liên kết TNGUOIDUNGTHEOCUAHANG)
     * - Xóa nhóm trong SGROUPUSER
     */
    public boolean deleteGroupCascade(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) return false;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Tìm tất cả user thuộc nhóm này để xóa store access
                List<String> userIds = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement("SELECT ID FROM SUSER WHERE SGROUPUSERID = ?")) {
                    ps.setString(1, groupId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            userIds.add(rs.getString("ID"));
                        }
                    }
                }

                // 2. Xóa store access của các user thuộc nhóm
                if (!userIds.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TNGUOIDUNGTHEOCUAHANG WHERE SUSERID = ?")) {
                        for (String uid : userIds) {
                            ps.setString(1, uid);
                            ps.executeUpdate();
                        }
                    }
                    // 3. Xóa các user thuộc nhóm
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SUSER WHERE SGROUPUSERID = ?")) {
                        ps.setString(1, groupId);
                        ps.executeUpdate();
                    }
                }

                // 4. Xóa phân quyền SGROUPROLE của nhóm
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SGROUPROLE WHERE SGROUPUSERID = ?")) {
                    ps.setString(1, groupId);
                    ps.executeUpdate();
                }

                // 5. Xóa phân quyền SREPORTROLE của nhóm
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SREPORTROLE WHERE SGROUPUSERID = ?")) {
                    ps.setString(1, groupId);
                    ps.executeUpdate();
                }

                // 6. Xóa nhóm người dùng trong SGROUPUSER
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SGROUPUSER WHERE ID = ?")) {
                    ps.setString(1, groupId);
                    ps.executeUpdate();
                }

                conn.commit();
                audit("Nhóm người dùng", "XÓA", groupId, "Xóa nhóm người dùng và quyền liên quan");
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xóa nhóm người dùng cascade: " + e.getMessage());
            return false;
        }
    }
}
