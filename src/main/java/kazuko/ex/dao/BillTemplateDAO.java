package kazuko.ex.dao;

import kazuko.ex.model.bill.BillTemplate;
import kazuko.ex.model.bill.BillTemplateDefinition;
import kazuko.ex.service.AuthService;
import kazuko.ex.util.BillTemplateJson;

import java.io.StringReader;
import java.sql.*;
import java.util.UUID;

/**
 * Lưu mẫu bill Java trong bảng riêng. Không đọc/ghi STEMPLATE.TEMPLATE để giữ
 * nguyên toàn bộ FastReport XML legacy.
 */
public class BillTemplateDAO extends BaseDAO {
    public static final String TABLE_NAME = "SJAVABILLTEMPLATE";
    private static volatile boolean schemaChecked;

    public BillTemplate findByLegacyTemplateId(String legacyTemplateId) throws SQLException {
        if (legacyTemplateId == null || legacyTemplateId.isBlank()) return null;
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            String sql = "SELECT ID, STEMPLATEID, NAME, PAPERWIDTH, DEFINITION, STATUS, VERSIONNO, " +
                    "TIMECREATED, TIMEMODIFIED FROM " + TABLE_NAME + " WHERE STEMPLATEID = ? AND STATUS >= 0";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, legacyTemplateId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? map(rs) : null;
                }
            }
        }
    }

    public BillTemplate newForLegacyTemplate(String legacyTemplateId, String name) {
        BillTemplate template = new BillTemplate();
        template.setId(UUID.randomUUID().toString());
        template.setLegacyTemplateId(legacyTemplateId);
        template.setName(name != null && !name.isBlank() ? name.trim() : "Mẫu bill Java");
        template.setPaperWidth(detectPaperWidth(name));
        template.setDefinition(BillTemplateDefinition.createDefault80mm());
        template.setVersionNo(1);
        return template;
    }

    public BillTemplate findOrCreateDraft(String legacyTemplateId, String name) throws SQLException {
        BillTemplate existing = findByLegacyTemplateId(legacyTemplateId);
        return existing != null ? existing : newForLegacyTemplate(legacyTemplateId, name);
    }

    /**
     * Insert hoặc update có optimistic locking bằng VERSIONNO.
     */
    public void save(BillTemplate template) throws SQLException {
        validate(template);
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
            if (exists(conn, template.getId())) {
                update(conn, template);
            } else {
                insert(conn, template);
            }
        }
    }

    private void insert(Connection conn, BillTemplate template) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (ID, STEMPLATEID, NAME, PAPERWIDTH, DEFINITION, " +
                "STATUS, VERSIONNO, TIMECREATED, TIMEMODIFIED, USERCREATEDID, USERMODIFIEDID) " +
                "VALUES (?, ?, ?, ?, ?, 30, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
        String userId = currentUserId();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, template.getId());
            ps.setString(2, template.getLegacyTemplateId());
            ps.setString(3, template.getName());
            ps.setInt(4, template.getPaperWidth());
            ps.setCharacterStream(5, new StringReader(BillTemplateJson.toJson(template.getDefinition())));
            ps.setString(6, userId);
            ps.setString(7, userId);
            ps.executeUpdate();
            template.setVersionNo(1);
        }
    }

    private void update(Connection conn, BillTemplate template) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET NAME = ?, PAPERWIDTH = ?, DEFINITION = ?, " +
                "VERSIONNO = VERSIONNO + 1, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? " +
                "WHERE ID = ? AND VERSIONNO = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, template.getName());
            ps.setInt(2, template.getPaperWidth());
            ps.setCharacterStream(3, new StringReader(BillTemplateJson.toJson(template.getDefinition())));
            ps.setString(4, currentUserId());
            ps.setString(5, template.getId());
            ps.setInt(6, template.getVersionNo());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Mẫu đã được người khác cập nhật. Hãy đóng cửa sổ và mở lại mẫu.");
            }
            template.setVersionNo(template.getVersionNo() + 1);
        }
    }

    private BillTemplate map(ResultSet rs) throws SQLException {
        BillTemplate template = new BillTemplate();
        template.setId(rs.getString("ID"));
        template.setLegacyTemplateId(rs.getString("STEMPLATEID"));
        template.setName(rs.getString("NAME"));
        template.setPaperWidth(rs.getInt("PAPERWIDTH"));
        template.setDefinition(BillTemplateJson.fromJson(rs.getString("DEFINITION")));
        template.setStatus(rs.getInt("STATUS"));
        template.setVersionNo(rs.getInt("VERSIONNO"));
        template.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        template.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        return template;
    }

    private boolean exists(Connection conn, String id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT ID FROM " + TABLE_NAME + " WHERE ID = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static synchronized void ensureSchema(Connection conn) throws SQLException {
        if (schemaChecked && tableExists(conn)) return;
        if (!tableExists(conn)) {
            String ddl = "CREATE TABLE " + TABLE_NAME + " (" +
                    "ID VARCHAR(36) NOT NULL, " +
                    "STEMPLATEID VARCHAR(36), " +
                    "NAME VARCHAR(255) NOT NULL, " +
                    "PAPERWIDTH INTEGER DEFAULT 80 NOT NULL, " +
                    "DEFINITION BLOB SUB_TYPE TEXT CHARACTER SET UTF8, " +
                    "STATUS INTEGER DEFAULT 30 NOT NULL, " +
                    "VERSIONNO INTEGER DEFAULT 1 NOT NULL, " +
                    "TIMECREATED TIMESTAMP, TIMEMODIFIED TIMESTAMP, " +
                    "USERCREATEDID VARCHAR(36), USERMODIFIEDID VARCHAR(36), " +
                    "CONSTRAINT PK_SJAVABILLTEMPLATE PRIMARY KEY (ID))";
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(ddl);
            }
        }
        ensureLegacyTemplateIndex(conn);
        schemaChecked = true;
    }

    private static void ensureLegacyTemplateIndex(Connection conn) throws SQLException {
        String check = "SELECT RDB$INDEX_NAME FROM RDB$INDICES WHERE RDB$INDEX_NAME = 'UQ_SJBT_STEMPLATE'";
        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(check)) {
            if (rs.next()) return;
        }
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("CREATE UNIQUE INDEX UQ_SJBT_STEMPLATE ON " + TABLE_NAME + " (STEMPLATEID)");
        }
    }

    private static boolean tableExists(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, TABLE_NAME, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void validate(BillTemplate template) {
        if (template == null) throw new IllegalArgumentException("Mẫu bill không được null");
        if (template.getId() == null || template.getId().isBlank()) throw new IllegalArgumentException("Mẫu bill thiếu ID");
        if (template.getName() == null || template.getName().isBlank()) throw new IllegalArgumentException("Tên mẫu bill không được trống");
        if (template.getPaperWidth() < 48 || template.getPaperWidth() > 210) {
            throw new IllegalArgumentException("Khổ giấy phải nằm trong khoảng 48-210 mm");
        }
        if (template.getDefinition() == null) throw new IllegalArgumentException("Mẫu bill thiếu định nghĩa layout");
    }

    private static int detectPaperWidth(String name) {
        if (name == null) return 80;
        String lower = name.toLowerCase();
        if (lower.contains("54")) return 54;
        if (lower.contains("58")) return 58;
        if (lower.contains("a4")) return 210;
        if (lower.contains("a5")) return 148;
        return 80;
    }

    private static String currentUserId() {
        return AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null
                ? AuthService.getCurrentUser().getId() : "";
    }
}
