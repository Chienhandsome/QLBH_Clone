package kazuko.ex;

import kazuko.ex.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbInspector {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("--- TEMPLATES IN STEMPLATE ---");
            String sql = "SELECT ID, NAME, SFORMID, REPORTBASE, NOTE FROM STEMPLATE";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("ID: " + rs.getString("ID") +
                            " | NAME: " + rs.getString("NAME") +
                            " | SFORMID: " + rs.getString("SFORMID") +
                            " | REPORTBASE: " + rs.getInt("REPORTBASE") +
                            " | NOTE: " + rs.getString("NOTE"));
                }
            }

            System.out.println("\n--- EXISTING RECORDS IN TBAOGIA ---");
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM TBAOGIA");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("BG: " + rs.getString("ID") + " | " + rs.getString("NAME") + " | " + rs.getString("TENKHACH") + " | " + rs.getDouble("TONGCONG"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
