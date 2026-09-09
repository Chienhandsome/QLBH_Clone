package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.service.ActivityLogService;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    protected void audit(String module, String action, String referenceNo, String content) {
        ActivityLogService.log(module, action, referenceNo, content);
    }
}
