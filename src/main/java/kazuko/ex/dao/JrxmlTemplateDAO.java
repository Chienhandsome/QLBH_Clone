package kazuko.ex.dao;

import java.io.StringReader;
import java.sql.*;
import java.util.UUID;

/** Lưu JRXML của canvas thử nghiệm tách biệt với XML FastReport legacy. */
public class JrxmlTemplateDAO extends BaseDAO {
    public static final String TABLE_NAME = "SJAVAJRXMLTEMPLATE";
    private static volatile boolean schemaChecked;

    public String findByLegacyTemplateId(String legacyTemplateId) throws SQLException {
        if (legacyTemplateId == null || legacyTemplateId.isBlank()) return null;
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            String sql = "SELECT JRXML FROM " + TABLE_NAME + " WHERE STEMPLATEID = ? AND STATUS >= 0";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, legacyTemplateId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("JRXML") : null;
                }
            }
        }
    }

    public void save(String legacyTemplateId, String name, String jrxml) throws SQLException {
        if (legacyTemplateId == null || legacyTemplateId.isBlank()) {
            throw new IllegalArgumentException("Mẫu thử nghiệm thiếu STEMPLATEID");
        }
        if (jrxml == null || jrxml.isBlank()) throw new IllegalArgumentException("JRXML không được trống");
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            if (exists(conn, legacyTemplateId)) {
                String sql = "UPDATE " + TABLE_NAME + " SET NAME = ?, JRXML = ?, VERSIONNO = VERSIONNO + 1, " +
                        "TIMEMODIFIED = CURRENT_TIMESTAMP WHERE STEMPLATEID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, safeName(name));
                    ps.setCharacterStream(2, new StringReader(jrxml));
                    ps.setString(3, legacyTemplateId);
                    ps.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO " + TABLE_NAME +
                        " (ID, STEMPLATEID, NAME, JRXML, STATUS, VERSIONNO, TIMECREATED, TIMEMODIFIED) " +
                        "VALUES (?, ?, ?, ?, 30, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, legacyTemplateId);
                    ps.setString(3, safeName(name));
                    ps.setCharacterStream(4, new StringReader(jrxml));
                    ps.executeUpdate();
                }
            }
        }
    }

    private static boolean exists(Connection conn, String legacyTemplateId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ID FROM " + TABLE_NAME + " WHERE STEMPLATEID = ?")) {
            ps.setString(1, legacyTemplateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static synchronized void ensureSchema(Connection conn) throws SQLException {
        if (schemaChecked && tableExists(conn)) return;
        if (!tableExists(conn)) {
            String ddl = "CREATE TABLE " + TABLE_NAME + " (" +
                    "ID VARCHAR(36) NOT NULL, STEMPLATEID VARCHAR(36) NOT NULL, " +
                    "NAME VARCHAR(255) NOT NULL, JRXML BLOB SUB_TYPE TEXT CHARACTER SET UTF8, " +
                    "STATUS INTEGER DEFAULT 30 NOT NULL, VERSIONNO INTEGER DEFAULT 1 NOT NULL, " +
                    "TIMECREATED TIMESTAMP, TIMEMODIFIED TIMESTAMP, " +
                    "CONSTRAINT PK_SJAVAJRXMLTEMPLATE PRIMARY KEY (ID))";
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(ddl);
                statement.executeUpdate("CREATE UNIQUE INDEX UQ_SJJT_STEMPLATE ON " +
                        TABLE_NAME + " (STEMPLATEID)");
            }
        }
        schemaChecked = true;
    }

    private static boolean tableExists(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, TABLE_NAME, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static String safeName(String name) {
        return name == null || name.isBlank() ? "Mẫu JRXML" : name.trim();
    }
}
