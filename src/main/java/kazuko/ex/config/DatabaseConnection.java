package kazuko.ex.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseConnection {
    private static volatile DatabaseConfig config = new DatabaseConfig();
    private static final Set<Connection> OPEN_CONNECTIONS = ConcurrentHashMap.newKeySet();

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

    public static synchronized void setConfig(DatabaseConfig newConfig) {
        closeConnection();
        config = newConfig;
    }

    /**
     * Opens an independent connection for one DAO operation.
     *
     * DAO methods in this application close their connection with
     * try-with-resources. Returning a shared static connection here lets one
     * concurrent operation close the transaction, statements and result sets
     * that another operation is still using.
     */
    public static synchronized Connection getConnection() throws SQLException {
        removeClosedConnections();
        DatabaseConfig currentConfig = config;
        Connection connection = DriverManager.getConnection(
                currentConfig.getJdbcUrl(),
                currentConfig.getUsername(),
                currentConfig.getPassword()
        );
        OPEN_CONNECTIONS.add(connection);
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

    public static synchronized void closeConnection() {
        for (Connection connection : OPEN_CONNECTIONS) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Best effort: callers use this before replacing/restoring a DB.
            }
        }
        OPEN_CONNECTIONS.clear();
    }

    private static void removeClosedConnections() {
        OPEN_CONNECTIONS.removeIf(connection -> {
            try {
                return connection.isClosed();
            } catch (SQLException e) {
                return true;
            }
        });
    }
}
