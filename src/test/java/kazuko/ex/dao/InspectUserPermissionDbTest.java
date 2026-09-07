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

            List<String> tables = Arrays.asList(
                    "SFUNCTION", "SGROUPROLE"
            );
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

            System.out.println("=== ALL SFUNCTION ROWS ===");
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT ID, NAME, GROUPNAME, NOTE, SORTORDER FROM SFUNCTION ORDER BY GROUPNAME, SORTORDER")) {
                while (rs.next()) {
                    System.out.println("ID=" + rs.getString(1) + " | NAME=" + rs.getString(2) + " | GROUP=" + rs.getString(3) + " | NOTE=" + rs.getString(4) + " | ORDER=" + rs.getInt(5));
                }
            }

        }
    }
}
