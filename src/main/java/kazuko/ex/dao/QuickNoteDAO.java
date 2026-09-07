package kazuko.ex.dao;

import kazuko.ex.model.system.SQuickNote;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuickNoteDAO extends BaseDAO {

    public List<SQuickNote> getAllNotes() {
        List<SQuickNote> list = new ArrayList<>();
        String sql = "SELECT ID, STATUS, USERCREATEDID, TIMECREATED, USERMODIFIEDID, TIMEMODIFIED, SORTORDER, NOTEDATA " +
                     "FROM SQUICKNOTE WHERE (STATUS IS NULL OR STATUS >= 0) ORDER BY SORTORDER, TIMECREATED";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SQuickNote note = new SQuickNote();
                note.setId(rs.getString("ID"));
                note.setStatus(rs.getObject("STATUS") != null ? rs.getInt("STATUS") : 30);
                note.setUserCreatedId(rs.getString("USERCREATEDID"));
                note.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                note.setUserModifiedId(rs.getString("USERMODIFIEDID"));
                note.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                note.setSortOrder(rs.getObject("SORTORDER") != null ? rs.getInt("SORTORDER") : 0);

                byte[] data = rs.getBytes("NOTEDATA");
                note.setNoteData(data);
                if (data != null && data.length > 0) {
                    note.setContent(new String(data, java.nio.charset.StandardCharsets.UTF_8));
                } else {
                    note.setContent("");
                }
                list.add(note);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách ghi chú: " + e.getMessage());
        }
        return list;
    }

    public boolean insertNote(SQuickNote note) {
        if (note.getId() == null || note.getId().trim().isEmpty()) {
            note.setId(UUID.randomUUID().toString());
        }
        if (note.getStatus() == null) {
            note.setStatus(30);
        }
        if (note.getUserCreatedId() == null || note.getUserCreatedId().trim().isEmpty()) {
            if (kazuko.ex.service.AuthService.getCurrentUser() != null) {
                note.setUserCreatedId(kazuko.ex.service.AuthService.getCurrentUser().getId());
            } else {
                note.setUserCreatedId("1");
            }
        }
        if (note.getUserModifiedId() == null) {
            note.setUserModifiedId(note.getUserCreatedId());
        }
        if (note.getTimeModified() == null) {
            note.setTimeModified(note.getTimeCreated());
        }

        String sql = "INSERT INTO SQUICKNOTE (ID, STATUS, USERCREATEDID, TIMECREATED, USERMODIFIEDID, TIMEMODIFIED, SORTORDER, NOTEDATA) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note.getId());
            ps.setInt(2, note.getStatus());
            ps.setString(3, note.getUserCreatedId());
            ps.setTimestamp(4, note.getTimeCreated());
            ps.setString(5, note.getUserModifiedId());
            ps.setTimestamp(6, note.getTimeModified());
            if (note.getSortOrder() != null) {
                ps.setInt(7, note.getSortOrder());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            byte[] data = note.getNoteData();
            if (data == null && note.getContent() != null) {
                data = note.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
            if (data != null && data.length > 0) {
                ps.setBinaryStream(8, new ByteArrayInputStream(data), data.length);
            } else {
                ps.setNull(8, Types.BLOB);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm ghi chú: " + e.getMessage());
            return false;
        }
    }

    public boolean updateNote(SQuickNote note) {
        if (note.getId() == null) return false;
        note.setTimeModified(new Timestamp(System.currentTimeMillis()));
        if (note.getUserModifiedId() == null || note.getUserModifiedId().trim().isEmpty()) {
            if (kazuko.ex.service.AuthService.getCurrentUser() != null) {
                note.setUserModifiedId(kazuko.ex.service.AuthService.getCurrentUser().getId());
            } else {
                note.setUserModifiedId(note.getUserCreatedId() != null ? note.getUserCreatedId() : "1");
            }
        }

        String sql = "UPDATE SQUICKNOTE SET NOTEDATA = ?, SORTORDER = ?, USERMODIFIEDID = ?, TIMEMODIFIED = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            byte[] data = note.getNoteData();
            if (data == null && note.getContent() != null) {
                data = note.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
            if (data != null && data.length > 0) {
                ps.setBinaryStream(1, new ByteArrayInputStream(data), data.length);
            } else {
                ps.setNull(1, Types.BLOB);
            }

            if (note.getSortOrder() != null) {
                ps.setInt(2, note.getSortOrder());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, note.getUserModifiedId());
            ps.setTimestamp(4, note.getTimeModified());
            ps.setString(5, note.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật ghi chú: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteNote(String id) {
        if (id == null) return false;
        String sql = "DELETE FROM SQUICKNOTE WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa ghi chú: " + e.getMessage());
            return false;
        }
    }

    public boolean saveOrUpdate(SQuickNote note) {
        if (note.getId() == null) {
            return insertNote(note);
        }
        String checkSql = "SELECT COUNT(*) FROM SQUICKNOTE WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, note.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return updateNote(note);
                } else {
                    return insertNote(note);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra ghi chú: " + e.getMessage());
            return false;
        }
    }
}
