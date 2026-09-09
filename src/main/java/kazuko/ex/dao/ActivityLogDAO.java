package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.system.ActivityLog;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityLogDAO {
    private static final String TABLE_NAME = "KZ_ACTIVITY_LOG";
    private static volatile boolean schemaReady;

    public boolean insert(ActivityLog log) {
        if (log == null) return false;
        try (Connection connection = openConnection()) {
            ensureSchema(connection);
            String sql = "INSERT INTO " + TABLE_NAME
                    + " (ID, EVENT_TIME, USER_ID, USER_NAME, ACTION_NAME, MODULE_NAME, REFERENCE_NO, DETAIL_TEXT, DEVICE_NAME)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, log.getId());
                statement.setTimestamp(2, new Timestamp(log.getEventTime().getTime()));
                statement.setString(3, log.getUserId());
                statement.setString(4, log.getUserName());
                statement.setString(5, log.getAction());
                statement.setString(6, log.getModule());
                statement.setString(7, log.getReferenceNo());
                statement.setString(8, log.getContent());
                statement.setString(9, log.getDeviceName());
                return statement.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Không thể ghi lưu vết hoạt động: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> search(Date fromDate, Date toDate, String userName,
                                    String referenceNo, String content) {
        List<ActivityLog> result = new ArrayList<>();
        try (Connection connection = openConnection()) {
            ensureSchema(connection);
            StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE 1=1 ");
            List<Object> parameters = new ArrayList<>();
            if (fromDate != null) {
                sql.append("AND EVENT_TIME >= ? ");
                parameters.add(new Timestamp(fromDate.getTime()));
            }
            if (toDate != null) {
                sql.append("AND EVENT_TIME < ? ");
                parameters.add(new Timestamp(toDate.getTime()));
            }
            if (userName != null && !userName.trim().isEmpty()) {
                sql.append("AND LOWER(USER_NAME) = ? ");
                parameters.add(userName.trim().toLowerCase());
            }
            if (referenceNo != null && !referenceNo.trim().isEmpty()) {
                sql.append("AND LOWER(REFERENCE_NO) LIKE ? ");
                parameters.add("%" + referenceNo.trim().toLowerCase() + "%");
            }
            if (content != null && !content.trim().isEmpty()) {
                sql.append("AND (LOWER(DETAIL_TEXT) LIKE ? OR LOWER(MODULE_NAME) LIKE ? OR LOWER(ACTION_NAME) LIKE ?) ");
                String keyword = "%" + content.trim().toLowerCase() + "%";
                parameters.add(keyword);
                parameters.add(keyword);
                parameters.add(keyword);
            }
            sql.append("ORDER BY EVENT_TIME DESC");

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) statement.setObject(i + 1, parameters.get(i));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) result.add(map(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể đọc lưu vết hoạt động: " + e.getMessage());
        }
        return result;
    }

    public List<String> getUserNames() {
        List<String> result = new ArrayList<>();
        try (Connection connection = openConnection()) {
            ensureSchema(connection);
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT DISTINCT USER_NAME FROM " + TABLE_NAME + " WHERE USER_NAME IS NOT NULL ORDER BY USER_NAME")) {
                while (rs.next()) result.add(rs.getString(1));
            }
        } catch (Exception e) {
            System.err.println("Không thể đọc danh sách tài khoản lưu vết: " + e.getMessage());
        }
        return result;
    }

    public int deleteByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        int deleted = 0;
        try (Connection connection = openConnection()) {
            ensureSchema(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE ID = ?")) {
                for (String id : ids) {
                    statement.setString(1, id);
                    statement.addBatch();
                }
                for (int count : statement.executeBatch()) {
                    if (count > 0) deleted += count;
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể xóa lưu vết hoạt động: " + e.getMessage());
        }
        return deleted;
    }

    private ActivityLog map(ResultSet rs) throws Exception {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getString("ID"));
        log.setEventTime(rs.getTimestamp("EVENT_TIME"));
        log.setUserId(rs.getString("USER_ID"));
        log.setUserName(rs.getString("USER_NAME"));
        log.setAction(rs.getString("ACTION_NAME"));
        log.setModule(rs.getString("MODULE_NAME"));
        log.setReferenceNo(rs.getString("REFERENCE_NO"));
        log.setContent(rs.getString("DETAIL_TEXT"));
        log.setDeviceName(rs.getString("DEVICE_NAME"));
        return log;
    }

    private Connection openConnection() throws Exception {
        return DriverManager.getConnection(
                DatabaseConnection.getConfig().getJdbcUrl(),
                DatabaseConnection.getConfig().getUsername(),
                DatabaseConnection.getConfig().getPassword());
    }

    private void ensureSchema(Connection connection) throws Exception {
        if (schemaReady) return;
        synchronized (ActivityLogDAO.class) {
            if (schemaReady) return;
            DatabaseMetaData metadata = connection.getMetaData();
            boolean exists;
            try (ResultSet tables = metadata.getTables(null, null, TABLE_NAME, new String[]{"TABLE"})) {
                exists = tables.next();
            }
            if (!exists) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE TABLE " + TABLE_NAME + " ("
                            + "ID VARCHAR(36) NOT NULL PRIMARY KEY, "
                            + "EVENT_TIME TIMESTAMP NOT NULL, "
                            + "USER_ID VARCHAR(36), USER_NAME VARCHAR(100), "
                            + "ACTION_NAME VARCHAR(50), MODULE_NAME VARCHAR(100), "
                            + "REFERENCE_NO VARCHAR(100), DETAIL_TEXT VARCHAR(1000), DEVICE_NAME VARCHAR(100))");
                } catch (Exception createFailure) {
                    try (ResultSet tables = metadata.getTables(null, null, TABLE_NAME, new String[]{"TABLE"})) {
                        if (!tables.next()) throw createFailure;
                    }
                }
            }
            schemaReady = true;
        }
    }
}
