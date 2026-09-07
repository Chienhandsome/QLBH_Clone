package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class InspectMatHangDbTest {

    @Test
    public void testInspectDatabaseSchema() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("=== Connected to Database: " + meta.getDatabaseProductName() + " ===");

            // List product-related tables
            ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"});
            System.out.println("=== TABLES IN DB ===");
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("Table: " + tableName);
            }

            // Inspect DMATHANG or similar
            String[] targetTables = {"DMATHANG", "MATHANG", "DHANGHOA", "DSANPHAM"};
            for (String tbl : targetTables) {
                try (ResultSet cols = meta.getColumns(null, null, tbl, "%")) {
                    boolean found = false;
                    while (cols.next()) {
                        if (!found) {
                            System.out.println("\n=== COLUMNS FOR TABLE: " + tbl + " ===");
                            found = true;
                        }
                        String colName = cols.getString("COLUMN_NAME");
                        String typeName = cols.getString("TYPE_NAME");
                        int colSize = cols.getInt("COLUMN_SIZE");
                        System.out.println(" - " + colName + " (" + typeName + "[" + colSize + "])");
                    }
                }
            }

            String[] checkTables = {"TDONHANG", "TLUUTAM", "DDINHLUONG", "TDATHANG"};
            for (String tbl : checkTables) {
                try (ResultSet cols = meta.getColumns(null, null, tbl, "%")) {
                    System.out.println("\n=== COLUMNS FOR TABLE: " + tbl + " ===");
                    while (cols.next()) {
                        System.out.println(" - " + cols.getString("COLUMN_NAME") + " (" + cols.getString("TYPE_NAME") + "[" + cols.getInt("COLUMN_SIZE") + "])");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
