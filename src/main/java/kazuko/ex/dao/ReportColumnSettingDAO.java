package kazuko.ex.dao;

import kazuko.ex.model.system.ReportColumnSetting;
import kazuko.ex.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Lưu cấu hình cột đang hoạt động của từng báo cáo. */
public class ReportColumnSettingDAO extends BaseDAO {
    public static final String TABLE_NAME = "SREPORTCOLUMNSETTING";
    private static final Object DB_LOCK = new Object();
    private static volatile boolean schemaChecked;

    public List<ReportColumnSetting> findByReportKey(String reportKey) throws SQLException {
        if (blank(reportKey)) return List.of();
        synchronized (DB_LOCK) {
            try (Connection conn = getConnection()) {
                ensureSchema(conn);
                List<ReportColumnSetting> settings = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT REPORTKEY, TEMPLATENAME, FIELDNAME, DISPLAYNAME, ISVISIBLE, GROUPLEVEL, " +
                                "TOTALGROUP1, TOTALGROUP2, GRANDTOTAL, ALIGNMENT, SORTORDER " +
                                "FROM " + TABLE_NAME + " WHERE REPORTKEY = ? ORDER BY SORTORDER")) {
                    ps.setString(1, reportKey.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            settings.add(new ReportColumnSetting(
                                    rs.getString("REPORTKEY"),
                                    rs.getString("TEMPLATENAME"),
                                    rs.getString("FIELDNAME"),
                                    rs.getString("DISPLAYNAME"),
                                    rs.getInt("ISVISIBLE") != 0,
                                    rs.getInt("GROUPLEVEL"),
                                    rs.getInt("TOTALGROUP1") != 0,
                                    rs.getInt("TOTALGROUP2") != 0,
                                    rs.getInt("GRANDTOTAL") != 0,
                                    rs.getString("ALIGNMENT"),
                                    rs.getInt("SORTORDER")));
                        }
                    }
                }
                return settings;
            }
        }
    }

    public void save(String reportKey, String templateName, List<ReportColumnSetting> settings)
            throws SQLException {
        if (blank(reportKey)) throw new IllegalArgumentException("Thiếu khóa báo cáo");
        if (settings == null || settings.isEmpty()) throw new IllegalArgumentException("Báo cáo chưa có cột");

        synchronized (DB_LOCK) {
            try (Connection conn = getConnection()) {
                ensureSchema(conn);
                boolean oldAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement delete = conn.prepareStatement(
                            "DELETE FROM " + TABLE_NAME + " WHERE REPORTKEY = ?")) {
                        delete.setString(1, reportKey.trim());
                        delete.executeUpdate();
                    }

                    try (PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO " + TABLE_NAME +
                                    " (ID, REPORTKEY, TEMPLATENAME, FIELDNAME, DISPLAYNAME, ISVISIBLE, GROUPLEVEL, " +
                                    "TOTALGROUP1, TOTALGROUP2, GRANDTOTAL, ALIGNMENT, " +
                                    "SORTORDER, STATUS, TIMECREATED, TIMEMODIFIED, USERMODIFIEDID) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)")) {
                        for (int i = 0; i < settings.size(); i++) {
                            ReportColumnSetting setting = settings.get(i);
                            insert.setString(1, UUID.randomUUID().toString());
                            insert.setString(2, reportKey.trim());
                            insert.setString(3, safeTemplateName(templateName));
                            insert.setString(4, setting.getFieldName());
                            insert.setString(5, setting.getDisplayName());
                            insert.setInt(6, setting.isVisible() ? 1 : 0);
                            insert.setInt(7, setting.getGroupLevel());
                            insert.setInt(8, setting.isTotalGroup1() ? 1 : 0);
                            insert.setInt(9, setting.isTotalGroup2() ? 1 : 0);
                            insert.setInt(10, setting.isGrandTotal() ? 1 : 0);
                            insert.setString(11, normalizeAlignment(setting.getAlignment()));
                            insert.setInt(12, i);
                            insert.setString(13, currentUserId());
                            insert.addBatch();
                        }
                        insert.executeBatch();
                    }
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    if (e instanceof SQLException) throw (SQLException) e;
                    throw new SQLException("Không lưu được thiết lập cột báo cáo", e);
                } finally {
                    conn.setAutoCommit(oldAutoCommit);
                }
            }
        }
    }

    private static synchronized void ensureSchema(Connection conn) throws SQLException {
        if (schemaChecked && tableExists(conn, TABLE_NAME)
                && columnExists(conn, TABLE_NAME, "GROUPLEVEL")
                && columnExists(conn, TABLE_NAME, "TOTALGROUP1")
                && columnExists(conn, TABLE_NAME, "TOTALGROUP2")
                && columnExists(conn, TABLE_NAME, "GRANDTOTAL")) return;
        if (!tableExists(conn, TABLE_NAME)) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("CREATE TABLE " + TABLE_NAME + " (" +
                        "ID VARCHAR(36) NOT NULL, REPORTKEY VARCHAR(100) NOT NULL, " +
                        "TEMPLATENAME VARCHAR(255), FIELDNAME VARCHAR(100) NOT NULL, " +
                        "DISPLAYNAME VARCHAR(255), ISVISIBLE SMALLINT DEFAULT 1 NOT NULL, " +
                        "GROUPLEVEL SMALLINT DEFAULT 0 NOT NULL, TOTALGROUP1 SMALLINT DEFAULT 0 NOT NULL, " +
                        "TOTALGROUP2 SMALLINT DEFAULT 0 NOT NULL, GRANDTOTAL SMALLINT DEFAULT 0 NOT NULL, " +
                        "ALIGNMENT VARCHAR(10) DEFAULT 'LEFT' NOT NULL, SORTORDER INTEGER DEFAULT 0 NOT NULL, " +
                        "STATUS INTEGER DEFAULT 30 NOT NULL, TIMECREATED TIMESTAMP, TIMEMODIFIED TIMESTAMP, " +
                        "USERMODIFIEDID VARCHAR(36), CONSTRAINT PK_SRCSETTING PRIMARY KEY (ID))");
                statement.executeUpdate("CREATE INDEX IX_SRCSETTING_REPORT ON " + TABLE_NAME + " (REPORTKEY)");
            }
        }
        addColumnIfMissing(conn, TABLE_NAME, "GROUPLEVEL", "SMALLINT DEFAULT 0 NOT NULL");
        addColumnIfMissing(conn, TABLE_NAME, "TOTALGROUP1", "SMALLINT DEFAULT 0 NOT NULL");
        addColumnIfMissing(conn, TABLE_NAME, "TOTALGROUP2", "SMALLINT DEFAULT 0 NOT NULL");
        addColumnIfMissing(conn, TABLE_NAME, "GRANDTOTAL", "SMALLINT DEFAULT 0 NOT NULL");
        schemaChecked = true;
    }

    private static void addColumnIfMissing(Connection conn, String tableName, String columnName,
                                           String definition) throws SQLException {
        if (columnExists(conn, tableName, columnName)) return;
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD " + columnName + " " + definition);
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName)
            throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static String normalizeAlignment(String alignment) {
        if ("Giữa".equalsIgnoreCase(alignment) || "CENTER".equalsIgnoreCase(alignment)) return "CENTER";
        if ("Phải".equalsIgnoreCase(alignment) || "RIGHT".equalsIgnoreCase(alignment)) return "RIGHT";
        return "LEFT";
    }

    private static String safeTemplateName(String name) {
        return blank(name) ? "Mẫu A4" : name.trim();
    }

    private static String currentUserId() {
        return AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null
                ? AuthService.getCurrentUser().getId() : "";
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
