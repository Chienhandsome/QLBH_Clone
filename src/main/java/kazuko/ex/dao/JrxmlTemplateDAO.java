package kazuko.ex.dao;

import kazuko.ex.service.AuthService;

import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Lưu draft và lịch sử JRXML tách biệt với FastReport XML legacy. */
public class JrxmlTemplateDAO extends BaseDAO {
    public static final String TABLE_NAME = "SJAVAJRXMLTEMPLATE";
    public static final String VERSION_TABLE_NAME = "SJAVAJRXMLVERSION";
    private static final int DRAFT = 10;
    private static final int PUBLISHED = 30;
    private static volatile boolean schemaChecked;

    public record TemplateState(String jrxml, int draftVersion, int publishedVersion,
                                boolean hasUnpublishedChanges) {}

    public record PublishedVersion(int versionNo, Timestamp publishedAt, String publishedBy) {}

    /** Bản đang mở trong editor, có thể là draft chưa xuất bản. */
    public String findByLegacyTemplateId(String legacyTemplateId) throws SQLException {
        TemplateState state = getState(legacyTemplateId);
        return state == null ? null : state.jrxml();
    }

    public TemplateState getState(String legacyTemplateId) throws SQLException {
        if (blank(legacyTemplateId)) return null;
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            Current current = findCurrent(conn, legacyTemplateId.trim());
            if (current == null) return null;
            int publishedVersion = latestPublishedVersion(conn, legacyTemplateId.trim());
            if (publishedVersion == 0 && current.status == PUBLISHED) publishedVersion = current.versionNo;
            return new TemplateState(current.jrxml, current.versionNo, publishedVersion,
                    current.status != PUBLISHED || current.versionNo != publishedVersion);
        }
    }

    /** Luồng in chỉ đọc bản đã xuất bản. */
    public String findPublishedByLegacyTemplateId(String legacyTemplateId) throws SQLException {
        if (blank(legacyTemplateId)) return null;
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT FIRST 1 JRXML FROM " + VERSION_TABLE_NAME +
                            " WHERE STEMPLATEID = ? AND STATUS = 30 ORDER BY VERSIONNO DESC")) {
                ps.setString(1, legacyTemplateId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString(1);
                }
            }
            Current current = findCurrent(conn, legacyTemplateId.trim());
            return current != null && current.status == PUBLISHED ? current.jrxml : null;
        }
    }

    /** API tương thích cũ: lưu nhưng chưa tác động mẫu đang in. */
    public void save(String legacyTemplateId, String name, String jrxml) throws SQLException {
        saveDraft(legacyTemplateId, name, jrxml);
    }

    public TemplateState saveDraft(String legacyTemplateId, String name, String jrxml) throws SQLException {
        validate(legacyTemplateId, jrxml);
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Current current = findCurrent(conn, legacyTemplateId.trim());
                if (current == null) {
                    insertDraft(conn, legacyTemplateId.trim(), safeName(name), jrxml);
                } else if (!jrxml.equals(current.jrxml) || !safeName(name).equals(current.name)) {
                    snapshotPublishedBaseline(conn, legacyTemplateId.trim(), current);
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE " + TABLE_NAME + " SET NAME = ?, JRXML = ?, STATUS = 10, " +
                                    "VERSIONNO = VERSIONNO + 1, TIMEMODIFIED = CURRENT_TIMESTAMP WHERE STEMPLATEID = ?")) {
                        ps.setString(1, safeName(name));
                        ps.setCharacterStream(2, new StringReader(jrxml));
                        ps.setString(3, legacyTemplateId.trim());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                conn.setAutoCommit(oldAutoCommit);
            } catch (Exception ex) {
                conn.rollback();
                conn.setAutoCommit(oldAutoCommit);
                throw ex;
            }
        }
        return getState(legacyTemplateId);
    }

    public TemplateState publish(String legacyTemplateId) throws SQLException {
        if (blank(legacyTemplateId)) throw new IllegalArgumentException("Mẫu thiếu STEMPLATEID");
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Current current = findCurrent(conn, legacyTemplateId.trim());
                if (current == null) throw new SQLException("Chưa có bản nháp JRXML để xuất bản");
                if (!versionExists(conn, legacyTemplateId.trim(), current.versionNo)) {
                    insertVersion(conn, legacyTemplateId.trim(), current.versionNo, current.name, current.jrxml);
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + TABLE_NAME + " SET STATUS = 30, TIMEMODIFIED = CURRENT_TIMESTAMP " +
                                "WHERE STEMPLATEID = ?")) {
                    ps.setString(1, legacyTemplateId.trim());
                    ps.executeUpdate();
                }
                conn.commit();
                conn.setAutoCommit(oldAutoCommit);
            } catch (Exception ex) {
                conn.rollback();
                conn.setAutoCommit(oldAutoCommit);
                throw ex;
            }
        }
        return getState(legacyTemplateId);
    }

    public TemplateState restoreAsDraft(String legacyTemplateId, int versionNo) throws SQLException {
        if (blank(legacyTemplateId) || versionNo < 1) throw new IllegalArgumentException("Phiên bản không hợp lệ");
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            String jrxml;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT JRXML FROM " + VERSION_TABLE_NAME + " WHERE STEMPLATEID = ? AND VERSIONNO = ?")) {
                ps.setString(1, legacyTemplateId.trim());
                ps.setInt(2, versionNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        jrxml = rs.getString(1);
                    } else {
                        Current current = findCurrent(conn, legacyTemplateId.trim());
                        if (current == null || current.status != PUBLISHED || current.versionNo != versionNo) {
                            throw new SQLException("Không tìm thấy phiên bản " + versionNo);
                        }
                        jrxml = current.jrxml;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE " + TABLE_NAME + " SET JRXML = ?, STATUS = 10, VERSIONNO = VERSIONNO + 1, " +
                            "TIMEMODIFIED = CURRENT_TIMESTAMP WHERE STEMPLATEID = ?")) {
                ps.setCharacterStream(1, new StringReader(jrxml));
                ps.setString(2, legacyTemplateId.trim());
                if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy mẫu JRXML hiện tại");
            }
        }
        return getState(legacyTemplateId);
    }

    public List<PublishedVersion> listPublishedVersions(String legacyTemplateId) throws SQLException {
        if (blank(legacyTemplateId)) return List.of();
        List<PublishedVersion> result = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT VERSIONNO, PUBLISHEDAT, PUBLISHEDBY FROM " + VERSION_TABLE_NAME +
                            " WHERE STEMPLATEID = ? AND STATUS = 30 ORDER BY VERSIONNO DESC")) {
                ps.setString(1, legacyTemplateId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) result.add(new PublishedVersion(rs.getInt(1), rs.getTimestamp(2), rs.getString(3)));
                }
            }
            if (result.isEmpty()) {
                Current current = findCurrent(conn, legacyTemplateId.trim());
                if (current != null && current.status == PUBLISHED) {
                    result.add(new PublishedVersion(current.versionNo, current.modifiedAt, null));
                }
            }
        }
        return result;
    }

    private static void insertDraft(Connection conn, String templateId, String name, String jrxml) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + TABLE_NAME +
                        " (ID, STEMPLATEID, NAME, JRXML, STATUS, VERSIONNO, TIMECREATED, TIMEMODIFIED) " +
                        "VALUES (?, ?, ?, ?, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, templateId);
            ps.setString(3, name);
            ps.setCharacterStream(4, new StringReader(jrxml));
            ps.executeUpdate();
        }
    }

    private static void snapshotPublishedBaseline(Connection conn, String templateId, Current current) throws SQLException {
        if (current.status == PUBLISHED && !versionExists(conn, templateId, current.versionNo)) {
            insertVersion(conn, templateId, current.versionNo, current.name, current.jrxml);
        }
    }

    private static void insertVersion(Connection conn, String templateId, int versionNo,
                                      String name, String jrxml) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + VERSION_TABLE_NAME +
                        " (ID, STEMPLATEID, VERSIONNO, NAME, JRXML, STATUS, PUBLISHEDAT, PUBLISHEDBY) " +
                        "VALUES (?, ?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)")) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, templateId);
            ps.setInt(3, versionNo);
            ps.setString(4, name);
            ps.setCharacterStream(5, new StringReader(jrxml));
            ps.setString(6, currentUserId());
            ps.executeUpdate();
        }
    }

    private static Current findCurrent(Connection conn, String templateId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT NAME, JRXML, STATUS, VERSIONNO, TIMEMODIFIED FROM " + TABLE_NAME +
                        " WHERE STEMPLATEID = ? AND STATUS >= 0")) {
            ps.setString(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Current(rs.getString(1), rs.getString(2), rs.getInt(3),
                        rs.getInt(4), rs.getTimestamp(5)) : null;
            }
        }
    }

    private static int latestPublishedVersion(Connection conn, String templateId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(VERSIONNO) FROM " + VERSION_TABLE_NAME + " WHERE STEMPLATEID = ? AND STATUS = 30")) {
            ps.setString(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static boolean versionExists(Connection conn, String templateId, int versionNo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ID FROM " + VERSION_TABLE_NAME + " WHERE STEMPLATEID = ? AND VERSIONNO = ?")) {
            ps.setString(1, templateId);
            ps.setInt(2, versionNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static synchronized void ensureSchema(Connection conn) throws SQLException {
        if (schemaChecked && tableExists(conn, TABLE_NAME) && tableExists(conn, VERSION_TABLE_NAME)) return;
        if (!tableExists(conn, TABLE_NAME)) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("CREATE TABLE " + TABLE_NAME + " (" +
                        "ID VARCHAR(36) NOT NULL, STEMPLATEID VARCHAR(36) NOT NULL, " +
                        "NAME VARCHAR(255) NOT NULL, JRXML BLOB SUB_TYPE TEXT CHARACTER SET UTF8, " +
                        "STATUS INTEGER DEFAULT 10 NOT NULL, VERSIONNO INTEGER DEFAULT 1 NOT NULL, " +
                        "TIMECREATED TIMESTAMP, TIMEMODIFIED TIMESTAMP, " +
                        "CONSTRAINT PK_SJAVAJRXMLTEMPLATE PRIMARY KEY (ID))");
                statement.executeUpdate("CREATE UNIQUE INDEX UQ_SJJT_STEMPLATE ON " + TABLE_NAME + " (STEMPLATEID)");
            }
        }
        if (!tableExists(conn, VERSION_TABLE_NAME)) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("CREATE TABLE " + VERSION_TABLE_NAME + " (" +
                        "ID VARCHAR(36) NOT NULL, STEMPLATEID VARCHAR(36) NOT NULL, VERSIONNO INTEGER NOT NULL, " +
                        "NAME VARCHAR(255) NOT NULL, JRXML BLOB SUB_TYPE TEXT CHARACTER SET UTF8, " +
                        "STATUS INTEGER DEFAULT 30 NOT NULL, PUBLISHEDAT TIMESTAMP, PUBLISHEDBY VARCHAR(36), " +
                        "CONSTRAINT PK_SJAVAJRXMLVERSION PRIMARY KEY (ID))");
                statement.executeUpdate("CREATE UNIQUE INDEX UQ_SJJV_TEMPLATE_VER ON " +
                        VERSION_TABLE_NAME + " (STEMPLATEID, VERSIONNO)");
            }
        }
        schemaChecked = true;
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void validate(String templateId, String jrxml) {
        if (blank(templateId)) throw new IllegalArgumentException("Mẫu thiếu STEMPLATEID");
        if (blank(jrxml)) throw new IllegalArgumentException("JRXML không được trống");
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String safeName(String name) { return blank(name) ? "Mẫu JRXML" : name.trim(); }

    private static String currentUserId() {
        return AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null
                ? AuthService.getCurrentUser().getId() : "";
    }

    private record Current(String name, String jrxml, int status, int versionNo, Timestamp modifiedAt) {}
}
