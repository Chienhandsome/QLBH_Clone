package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.config.DatabaseConfig;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.*;

public class InspectUserPermissionDbTest {

    @Test
    public void inspectTables() throws Exception {
        DatabaseConfig config = DatabaseConnection.getConfig();
        try (Connection c = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword())) {
            DatabaseMetaData meta = c.getMetaData();
            System.out.println("=== SFUNCTION DISTINCT NOTE / GROUPNAME ===");
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT DISTINCT GROUPNAME, NOTE FROM SFUNCTION")) {
                while (rs.next()) {
                    System.out.println("GROUPNAME: " + rs.getString(1) + ", NOTE: " + rs.getString(2));
                }
            }

            System.out.println("=== SGROUPROLE DISTINCT MODE ===");
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT DISTINCT MODE FROM SGROUPROLE")) {
                while (rs.next()) {
                    System.out.println("MODE: " + rs.getInt(1));
                }
            }

            System.out.println("=== SREPORTROLE DISTINCT MODE ===");
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT DISTINCT MODE FROM SREPORTROLE")) {
                while (rs.next()) {
                    System.out.println("MODE: " + rs.getInt(1));
                }
            }




        }
    }
}
