package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.config.DatabaseConfig;
import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import kazuko.ex.service.AuthService;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Uses the original Firebird schema. Failures propagate so the UI never reports a false save. */
public class ChiTieuDoanhThuDAO {
    protected Connection connect() throws SQLException {
        DatabaseConfig config = DatabaseConnection.getConfig();
        return DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
    }

    protected String userId() {
        if (AuthService.getCurrentUser() == null || AuthService.getCurrentUser().getId() == null)
            throw new IllegalStateException("Vui lòng đăng nhập trước khi lưu chỉ tiêu doanh thu.");
        return AuthService.getCurrentUser().getId();
    }

    public List<Group> groups() throws SQLException {
        List<Group> result = new ArrayList<>();
        try (Connection c = connect(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM DNHOMCHITIEUDOANHTHU ORDER BY SORTORDER, NAME")) {
            while (r.next()) result.add(new Group(r.getString("ID"), r.getString("NAME"), r.getString("NOTE"),
                    r.getString("PARENTID"), r.getInt("ITEMTYPE") == 1, r.getString("SIMAGEID"), r.getInt("STATUS")));
        }
        return result;
    }

    public Map<String, String> provinces() throws SQLException {
        return lookup("SELECT ID, NAME FROM DTINHTHANH WHERE STATUS IS NULL OR STATUS >= 0 ORDER BY NAME");
    }

    public Map<String, String> images() throws SQLException {
        return lookup("SELECT ID, NAME FROM SIMAGE ORDER BY NAME");
    }

    private Map<String, String> lookup(String sql) throws SQLException {
        Map<String, String> result = new LinkedHashMap<>();
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) result.put(r.getString(1), r.getString(2));
        }
        return result;
    }

    public List<ChiTieuDoanhThu> list(boolean trash) throws SQLException {
        List<ChiTieuDoanhThu> result = new ArrayList<>();
        String sql = "SELECT t.*, g.NAME AS GROUPNAME, uc.NAME AS CREATEDBY, um.NAME AS MODIFIEDBY "
                + "FROM DCHITIEUDOANHTHU t LEFT JOIN DNHOMCHITIEUDOANHTHU g ON g.ID=t.DNHOMCHITIEUDOANHTHUID "
                + "LEFT JOIN SUSER uc ON uc.ID=t.USERCREATEDID LEFT JOIN SUSER um ON um.ID=t.USERMODIFIEDID "
                + "WHERE " + (trash ? "t.STATUS < 0" : "(t.STATUS IS NULL OR t.STATUS >= 0)")
                + " ORDER BY t.NAM DESC, t.NAME DESC, g.NAME";
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                ChiTieuDoanhThu t = new ChiTieuDoanhThu();
                t.id = r.getString("ID");
                t.year = r.getInt("NAM");
                if (t.year == 0) {
                    try { t.year = Integer.parseInt(r.getString("NAME")); }
                    catch (NumberFormatException ignored) { /* Display legacy records without changing them. */ }
                }
                t.groupId = r.getString("DNHOMCHITIEUDOANHTHUID");
                t.groupName = r.getString("GROUPNAME");
                t.note = r.getString("NOTE");
                t.status = r.getInt("STATUS");
                t.createdAt = r.getTimestamp("TIMECREATED");
                t.modifiedAt = r.getTimestamp("TIMEMODIFIED");
                t.createdBy = r.getString("CREATEDBY");
                t.modifiedBy = r.getString("MODIFIEDBY");
                for (int i = 0; i < 12; i++) t.months[i] = zero(r.getBigDecimal("THANG" + (i + 1)));
                result.add(t);
            }
        }
        return result;
    }

    public List<ProvinceTarget> details(String id) throws SQLException {
        List<ProvinceTarget> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement s = c.prepareStatement(
                "SELECT d.*, p.NAME AS PROVINCENAME FROM DCHITIEUCHITIET d LEFT JOIN DTINHTHANH p ON p.ID=d.DTINHTHANHID "
                        + "WHERE d.DCHITIEUDOANHTHUID=? AND (d.STATUS IS NULL OR d.STATUS >= 0) ORDER BY p.NAME")) {
            s.setString(1, id);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) result.add(new ProvinceTarget(r.getString("DTINHTHANHID"),
                        r.getString("PROVINCENAME"), zero(r.getBigDecimal("CHITIEU")), r.getString("NOTE")));
            }
        }
        return result;
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    public void save(ChiTieuDoanhThu target) throws SQLException { saveAll(List.of(target)); }

    /** Import and parent/child edits are atomic, including validation failures. */
    public void saveAll(List<ChiTieuDoanhThu> targets) throws SQLException {
        for (ChiTieuDoanhThu target : targets) target.validate();
        List<String> ids = new ArrayList<>();
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                for (ChiTieuDoanhThu target : targets) {
                    String id = target.id == null ? UUID.randomUUID().toString() : target.id;
                    write(c, target, id);
                    ids.add(id);
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
        for (int i = 0; i < targets.size(); i++) targets.get(i).id = ids.get(i);
    }

    private void write(Connection c, ChiTieuDoanhThu t, String id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT ID FROM DCHITIEUDOANHTHU WHERE (NAM=? OR NAME=?) "
                + "AND COALESCE(DNHOMCHITIEUDOANHTHUID,'')=COALESCE(CAST(? AS VARCHAR(36)),'') AND ID<>?")) {
            s.setInt(1, t.year); s.setString(2, String.valueOf(t.year)); s.setString(3, t.groupId); s.setString(4, id);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) throw new SQLException("Đã có kế hoạch năm " + t.year + " cho nhóm này (kể cả thùng rác). Hãy chỉnh sửa hoặc khôi phục kế hoạch cũ.");
            }
        }
        if (t.groupId != null) {
            try (PreparedStatement s = c.prepareStatement("SELECT ID FROM DNHOMCHITIEUDOANHTHU WHERE ID=? "
                    + "AND (STATUS IS NULL OR STATUS >= 0) AND (ITEMTYPE IS NULL OR ITEMTYPE <> 1)")) {
                s.setString(1, t.groupId);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) throw new SQLException("Nhóm chỉ tiêu không còn hoạt động. Hãy chọn lại nhóm.");
                }
            }
        }
        try (PreparedStatement s = c.prepareStatement("SELECT ID FROM DTINHTHANH WHERE ID=? AND (STATUS IS NULL OR STATUS >= 0)")) {
            for (ProvinceTarget p : t.provinces) {
                s.setString(1, p.provinceId());
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) throw new SQLException("Tỉnh thành không còn hoạt động: " + p.provinceName());
                }
            }
        }
        String fields = "NAME, NAM, DNHOMCHITIEUDOANHTHUID, NOTE";
        for (int i = 1; i <= 12; i++) fields += ", THANG" + i;
        String sql;
        if (t.id == null) sql = "INSERT INTO DCHITIEUDOANHTHU (" + fields
                + ", ID, STATUS, TIMECREATED, USERCREATEDID) VALUES (" + "?,".repeat(16)
                + "?,30,CURRENT_TIMESTAMP,?)";
        else sql = "UPDATE DCHITIEUDOANHTHU SET " + String.join("=?, ", fields.split(", "))
                + "=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=? AND (STATUS IS NULL OR STATUS >= 0)";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, String.valueOf(t.year));
            s.setInt(2, t.year);
            s.setString(3, t.groupId);
            s.setString(4, t.note);
            for (int i = 0; i < 12; i++) s.setBigDecimal(5 + i, t.months[i]);
            s.setString(17, t.id == null ? id : userId());
            s.setString(18, t.id == null ? userId() : id);
            if (s.executeUpdate() != 1) throw new SQLException("Kế hoạch không còn tồn tại hoặc đã vào thùng rác.");
        }
        try (PreparedStatement s = c.prepareStatement("DELETE FROM DCHITIEUCHITIET WHERE DCHITIEUDOANHTHUID=?")) {
            s.setString(1, id);
            s.executeUpdate();
        }
        try (PreparedStatement s = c.prepareStatement("INSERT INTO DCHITIEUCHITIET "
                + "(ID,DCHITIEUDOANHTHUID,DTINHTHANHID,CHITIEU,NOTE,STATUS,TIMECREATED,USERCREATEDID) "
                + "VALUES (?,?,?,?,?,30,CURRENT_TIMESTAMP,?)")) {
            for (ProvinceTarget p : t.provinces) {
                s.setString(1, UUID.randomUUID().toString());
                s.setString(2, id);
                s.setString(3, p.provinceId());
                s.setBigDecimal(4, p.amount());
                s.setString(5, p.note());
                s.setString(6, userId());
                s.addBatch();
            }
            s.executeBatch();
        }
    }

    public String saveGroup(Group g) throws SQLException {
        if (g.name() == null || g.name().isBlank() || g.name().length() > 255)
            throw new IllegalArgumentException("Tên nhóm bắt buộc, tối đa 255 ký tự.");
        if (g.note() != null && g.note().length() > 255)
            throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
        String id = g.id() == null ? UUID.randomUUID().toString() : g.id();
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement("SELECT ID FROM DNHOMCHITIEUDOANHTHU "
                        + "WHERE UPPER(NAME)=UPPER(?) AND ID<>?")) {
                    s.setString(1, g.name().trim()); s.setString(2, id);
                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) throw new SQLException("Tên nhóm đã tồn tại (có thể trong thùng rác).");
                    }
                }
                String sql = g.id() == null
                        ? "INSERT INTO DNHOMCHITIEUDOANHTHU (NAME,NOTE,PARENTID,ITEMTYPE,SIMAGEID,ID,STATUS,TIMECREATED,USERCREATEDID) VALUES (?,?,?,?,?,?,30,CURRENT_TIMESTAMP,?)"
                        : "UPDATE DNHOMCHITIEUDOANHTHU SET NAME=?,NOTE=?,PARENTID=?,ITEMTYPE=?,SIMAGEID=?,TIMEMODIFIED=CURRENT_TIMESTAMP,USERMODIFIEDID=? WHERE ID=?";
                try (PreparedStatement s = c.prepareStatement(sql)) {
                    s.setString(1, g.name().trim()); s.setString(2, g.note()); s.setString(3, g.parentId());
                    s.setInt(4, g.folder() ? 1 : 0); s.setString(5, g.imageId());
                    s.setString(6, g.id() == null ? id : userId()); s.setString(7, g.id() == null ? userId() : id);
                    if (s.executeUpdate() != 1) throw new SQLException("Nhóm không còn tồn tại.");
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
        return id;
    }

    public Set<String> getAllDescendantGroupIds(Connection c, String rootId) throws SQLException {
        Set<String> allIds = new LinkedHashSet<>();
        if (rootId == null || rootId.trim().isEmpty()) return allIds;
        allIds.add(rootId.trim());

        List<Group> allGroups = new ArrayList<>();
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT ID, PARENTID FROM DNHOMCHITIEUDOANHTHU")) {
            while (r.next()) {
                allGroups.add(new Group(r.getString("ID"), null, null, r.getString("PARENTID"), false, null, 0));
            }
        }

        boolean added;
        do {
            added = false;
            for (Group g : allGroups) {
                if (g.parentId() != null && !g.parentId().trim().isEmpty() && g.id() != null) {
                    if (allIds.contains(g.parentId().trim())) {
                        if (allIds.add(g.id().trim())) {
                            added = true;
                        }
                    }
                }
            }
        } while (added);

        return allIds;
    }

    public void setTrash(String id, boolean group, boolean trash) throws SQLException {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                int status = trash ? -1 : 30;
                String user = userId();
                if (group) {
                    Set<String> groupIds = getAllDescendantGroupIds(c, id);
                    if (!groupIds.isEmpty()) {
                        StringBuilder inClause = new StringBuilder();
                        for (int i = 0; i < groupIds.size(); i++) {
                            if (i > 0) inClause.append(",");
                            inClause.append("?");
                        }

                        // 1. Update groups status
                        String updateGroupSql = "UPDATE DNHOMCHITIEUDOANHTHU SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID IN (" + inClause + ")";
                        try (PreparedStatement s = c.prepareStatement(updateGroupSql)) {
                            s.setInt(1, status);
                            s.setString(2, user);
                            int idx = 3;
                            for (String gid : groupIds) {
                                s.setString(idx++, gid);
                            }
                            s.executeUpdate();
                        }

                        // 2. Update plans status in those groups
                        String updatePlanSql = "UPDATE DCHITIEUDOANHTHU SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE DNHOMCHITIEUDOANHTHUID IN (" + inClause + ")";
                        try (PreparedStatement s = c.prepareStatement(updatePlanSql)) {
                            s.setInt(1, status);
                            s.setString(2, user);
                            int idx = 3;
                            for (String gid : groupIds) {
                                s.setString(idx++, gid);
                            }
                            s.executeUpdate();
                        }
                    }
                } else {
                    String updatePlanSql = "UPDATE DCHITIEUDOANHTHU SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
                    try (PreparedStatement s = c.prepareStatement(updatePlanSql)) {
                        s.setInt(1, status);
                        s.setString(2, user);
                        s.setString(3, id);
                        if (s.executeUpdate() != 1) throw new SQLException("Dữ liệu không còn tồn tại.");
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void deletePermanent(String id, boolean group) throws SQLException {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                if (group) {
                    Set<String> groupIds = getAllDescendantGroupIds(c, id);
                    if (!groupIds.isEmpty()) {
                        StringBuilder inClause = new StringBuilder();
                        for (int i = 0; i < groupIds.size(); i++) {
                            if (i > 0) inClause.append(",");
                            inClause.append("?");
                        }

                        // 1. Delete details in DCHITIEUCHITIET
                        String delDetailsSql = "DELETE FROM DCHITIEUCHITIET WHERE DCHITIEUDOANHTHUID IN "
                                + "(SELECT ID FROM DCHITIEUDOANHTHU WHERE DNHOMCHITIEUDOANHTHUID IN (" + inClause + "))";
                        try (PreparedStatement s = c.prepareStatement(delDetailsSql)) {
                            int idx = 1;
                            for (String gid : groupIds) {
                                s.setString(idx++, gid);
                            }
                            s.executeUpdate();
                        }

                        // 2. Delete plans in DCHITIEUDOANHTHU
                        String delPlansSql = "DELETE FROM DCHITIEUDOANHTHU WHERE DNHOMCHITIEUDOANHTHUID IN (" + inClause + ")";
                        try (PreparedStatement s = c.prepareStatement(delPlansSql)) {
                            int idx = 1;
                            for (String gid : groupIds) {
                                s.setString(idx++, gid);
                            }
                            s.executeUpdate();
                        }

                        // 3. Delete groups in DNHOMCHITIEUDOANHTHU
                        String delGroupsSql = "DELETE FROM DNHOMCHITIEUDOANHTHU WHERE ID IN (" + inClause + ")";
                        try (PreparedStatement s = c.prepareStatement(delGroupsSql)) {
                            int idx = 1;
                            for (String gid : groupIds) {
                                s.setString(idx++, gid);
                            }
                            s.executeUpdate();
                        }
                    }
                } else {
                    // 1. Delete details
                    try (PreparedStatement s = c.prepareStatement("DELETE FROM DCHITIEUCHITIET WHERE DCHITIEUDOANHTHUID=?")) {
                        s.setString(1, id);
                        s.executeUpdate();
                    }
                    // 2. Delete plan
                    try (PreparedStatement s = c.prepareStatement("DELETE FROM DCHITIEUDOANHTHU WHERE ID=?")) {
                        s.setString(1, id);
                        s.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
