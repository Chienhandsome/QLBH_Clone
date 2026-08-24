package kazuko.ex;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Cấu hình kết nối tới file DEMO.FDB trong thư mục data của dự án
    private static final String DB_PATH = "D:/THUCTAP/QLBH_Clone/data/DEMO.FDB";
    private static final String DB_URL = "jdbc:firebirdsql://localhost:3050/" + DB_PATH + "?charSet=utf-8";
    private static final String DB_USER = "SYSDBA";
    private static final String DB_PASS = "masterkey";

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   ĐANG KẾT NỐI TỚI CƠ SỞ DỮ LIỆU FIREBIRD (DEMO.FDB)...   ");
        System.out.println("==========================================================");

        try {
            // Nạp Driver Firebird Jaybird
            Class.forName("org.firebirdsql.jdbc.FBDriver");

            // Mở kết nối
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                System.out.println("✅ KẾT NỐI THÀNH CÔNG TỚI FIREBIRD SERVER!");

                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("• Database: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
                System.out.println("• Driver: " + meta.getDriverName() + " " + meta.getDriverVersion());

                // 1. Quét toàn bộ danh sách bảng
                ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (!tableName.startsWith("RDB$") && !tableName.startsWith("MON$") && !tableName.startsWith("IBE$")) {
                        tables.add(tableName.trim());
                    }
                }
                rs.close();

                System.out.println("\n📊 TỔNG SỐ BẢNG DỮ LIỆU TÌM THẤY: " + tables.size() + " bảng");
                System.out.println("Danh sách bảng: " + tables);

                // 2. Đọc thử bảng Người dùng / Nhân viên (SUSER)
                if (tables.contains("SUSER")) {
                    System.out.println("\n----------------------------------------------------------");
                    System.out.println("👤 DỮ LIỆU MẪU BẢNG NGƯỜI DÙNG / NHÂN VIÊN (SUSER):");
                    System.out.println("----------------------------------------------------------");
                    try (Statement stmt = conn.createStatement();
                         ResultSet userRs = stmt.executeQuery("SELECT FIRST 10 * FROM SUSER")) {
                        int cols = userRs.getMetaData().getColumnCount();
                        for (int i = 1; i <= cols; i++) {
                            System.out.printf("%-18s", userRs.getMetaData().getColumnName(i));
                        }
                        System.out.println();

                        while (userRs.next()) {
                            for (int i = 1; i <= cols; i++) {
                                Object val = userRs.getObject(i);
                                System.out.printf("%-18s", val != null ? val.toString().trim() : "NULL");
                            }
                            System.out.println();
                        }
                    }
                }

                // 3. Tìm và đọc thử các bảng hàng hóa / kho hàng khác
                for (String tbl : tables) {
                    if (tbl.contains("HANG") || tbl.contains("PRODUCT") || tbl.contains("KHO") || tbl.contains("HOADON")) {
                        System.out.println("\n----------------------------------------------------------");
                        System.out.println("📦 DỮ LIỆU MẪU TỪ BẢNG: " + tbl);
                        System.out.println("----------------------------------------------------------");
                        try (Statement stmt = conn.createStatement();
                             ResultSet dataRs = stmt.executeQuery("SELECT FIRST 5 * FROM " + tbl)) {
                            int cols = dataRs.getMetaData().getColumnCount();
                            for (int i = 1; i <= cols; i++) {
                                System.out.printf("%-20s", dataRs.getMetaData().getColumnName(i));
                            }
                            System.out.println();
                            while (dataRs.next()) {
                                for (int i = 1; i <= cols; i++) {
                                    Object val = dataRs.getObject(i);
                                    System.out.printf("%-20s", val != null ? val.toString().trim() : "NULL");
                                }
                                System.out.println();
                            }
                        } catch (Exception e) {
                            System.out.println("(Không thể đọc bảng " + tbl + ": " + e.getMessage() + ")");
                        }
                    }
                }

                System.out.println("\n==========================================================");
                System.out.println("🎉 HOÀN THÀNH KIỂM TRA: DỮ LIỆU ĐÃ SẴN SÀNG ĐỂ XÂY DỰNG GIAO DIỆN!");
                System.out.println("==========================================================");

            }
        } catch (Exception e) {
            System.err.println("❌ LỖI KẾT NỐI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}