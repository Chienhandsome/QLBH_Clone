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

            // Try selecting one row from DMATHANG
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT FIRST 1 * FROM DMATHANG")) {
                java.sql.ResultSetMetaData rsmd = rs.getMetaData();
                System.out.println("\n=== DMATHANG RESULT SET COLUMNS (" + rsmd.getColumnCount() + ") ===");
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    System.out.println(i + ". " + rsmd.getColumnName(i) + " (" + rsmd.getColumnTypeName(i) + ")");
                }
            } catch (Exception e) {
                System.out.println("Could not select from DMATHANG: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
