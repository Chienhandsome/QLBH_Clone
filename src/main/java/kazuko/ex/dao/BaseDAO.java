package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}
