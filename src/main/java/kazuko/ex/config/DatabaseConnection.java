package kazuko.ex.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConfig config = new DatabaseConfig();
    private static Connection connection = null;

    static {
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Firebird JDBC Driver: " + e.getMessage());
        }
    }

    public static DatabaseConfig getConfig() {
        return config;
    }

    public static void setConfig(DatabaseConfig newConfig) {
        config = newConfig;
        closeConnection();
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    config.getJdbcUrl(),
                    config.getUsername(),
                    config.getPassword()
            );
        }
        return connection;
    }

    public static boolean testConnection(String serverIp, int port, String dbPath, String user, String pass) {
        String testUrl = "jdbc:firebirdsql://" + serverIp + ":" + port + "/" + dbPath + "?charSet=UTF8";
        try (Connection testConn = DriverManager.getConnection(testUrl, user, pass)) {
            return testConn != null && !testConn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test kết nối thất bại: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
        connection = null;
    }
}
