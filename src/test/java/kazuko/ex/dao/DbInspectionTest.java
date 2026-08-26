package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class DbInspectionTest {

    @Test
    public void testKhachHangQueries() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("\n--- Testing DKHACHHANG select with JOIN ---");
            String sql = "SELECT k.ID, k.MAKHACH, k.NAME, k.DIACHI, k.DIENTHOAI, k.EMAIL, k.NGAYSINH, " +
                         "k.DIEMTICHLUYBANDAU, k.GIABAN, k.MASOTHUE, k.DNHANVIENID, nv.NAME as TENNHANVIEN, " +
                         "k.DNHOMKHACHHANGID, n.NAME as TENNHOM, k.DTINHTHANHID, tt.NAME as TENTINHTHANH, " +
                         "k.FACEBOOK, k.NOTE, k.STATUS, k.TIMECREATED, k.USERCREATEDID, k.TIMEMODIFIED, k.USERMODIFIEDID " +
                         "FROM DKHACHHANG k " +
                         "LEFT JOIN DNHOMKHACHHANG n ON k.DNHOMKHACHHANGID = n.ID " +
                         "LEFT JOIN DNHANVIEN nv ON k.DNHANVIENID = nv.ID " +
                         "LEFT JOIN DTINHTHANH tt ON k.DTINHTHANHID = tt.ID " +
                         "WHERE (k.STATUS IS NULL OR k.STATUS >= 0) " +
                         "ORDER BY k.MAKHACH, k.NAME";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    if (count <= 5) {
                        System.out.println("Row " + count + ": MaKhach=" + rs.getString("MAKHACH") + ", Name=" + rs.getString("NAME") + ", Nhom=" + rs.getString("TENNHOM") + ", NV=" + rs.getString("TENNHANVIEN") + ", Tinh=" + rs.getString("TENTINHTHANH"));
                    }
                }
                System.out.println("Total DKHACHHANG active rows in DB: " + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail("SQL query failed: " + e.getMessage());
        }
    }

    @Test
    public void testLoadAllNhomKhachHang() {
        NhomKhachHangDAO dao = new NhomKhachHangDAO();
        List<NhomKhachHang> groups = dao.getAll();
        org.junit.jupiter.api.Assertions.assertNotNull(groups);
        org.junit.jupiter.api.Assertions.assertFalse(groups.isEmpty(), "Should load groups from DEMO.FDB");
        System.out.println("Loaded " + groups.size() + " groups from DB:");
        for (NhomKhachHang g : groups) {
            System.out.println(" - " + g.getName() + " (ID=" + g.getId() + ", Icon=" + g.getIconName() + ")");
        }
    }

    @Test
    public void testCRUDOperations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String testNhomId = "TEST_NHOM_" + System.currentTimeMillis();
            String testKhachId = "TEST_KH_" + System.currentTimeMillis();

            // 1. Insert NhomKhachHang
            String sqlInsertNhom = "INSERT INTO DNHOMKHACHHANG (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, SORTORDER, ITEMTYPE, TILEGIAMGIA, DIEMTICHLUY) " +
                                   "VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?, '1', 0, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertNhom)) {
                ps.setString(1, testNhomId);
                ps.setString(2, "Nhóm Test JUnit");
                ps.setString(3, "Ghi chú test");
                ps.setString(4, "4f1466a0-0756-4ba9-afa8-053b96ca7569");
                ps.setDouble(5, 5.0);
                ps.setDouble(6, 100.0);
                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                System.out.println("Insert Nhom: rows=" + rows);
            }

            // 2. Insert KhachHang
            String sqlInsertKhach = "INSERT INTO DKHACHHANG (" +
                                    "ID, MAKHACH, NAME, DIACHI, DIENTHOAI, EMAIL, NGAYSINH, " +
                                    "DIEMTICHLUYBANDAU, GIABAN, MASOTHUE, DNHANVIENID, DNHOMKHACHHANGID, " +
                                    "DTINHTHANHID, FACEBOOK, NOTE, STATUS, TIMECREATED, USERCREATEDID" +
                                    ") VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertKhach)) {
                ps.setString(1, testKhachId);
                ps.setString(2, "KH_TEST");
                ps.setString(3, "Khách hàng Test");
                ps.setString(4, "Địa chỉ test");
                ps.setString(5, "0912345678");
                ps.setString(6, "test@gmail.com");
                ps.setInt(7, 50);
                ps.setInt(8, 0);
                ps.setString(9, "0101234567");
                ps.setString(10, null);
                ps.setString(11, testNhomId);
                ps.setString(12, null);
                ps.setString(13, "fb.com/test");
                ps.setString(14, "Ghi chú test");
                ps.setString(15, "4f1466a0-0756-4ba9-afa8-053b96ca7569");
                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                System.out.println("Insert Khach: rows=" + rows);
            }

            // 3. Update KhachHang
            String sqlUpdateKhach = "UPDATE DKHACHHANG SET " +
                                    "MAKHACH=?, NAME=?, DIACHI=?, DIENTHOAI=?, EMAIL=?, " +
                                    "DIEMTICHLUYBANDAU=?, GIABAN=?, MASOTHUE=?, DNHOMKHACHHANGID=?, " +
                                    "NOTE=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? " +
                                    "WHERE ID=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateKhach)) {
                ps.setString(1, "KH_TEST_UPDATED");
                ps.setString(2, "Khách hàng Test Đã Cập Nhật");
                ps.setString(3, "Địa chỉ mới");
                ps.setString(4, "0988888888");
                ps.setString(5, "new@gmail.com");
                ps.setInt(6, 150);
                ps.setInt(7, 1);
                ps.setString(8, "MST_NEW");
                ps.setString(9, testNhomId);
                ps.setString(10, "Ghi chú mới");
                ps.setString(11, "4f1466a0-0756-4ba9-afa8-053b96ca7569");
                ps.setString(12, testKhachId);
                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                System.out.println("Update Khach: rows=" + rows);
            }

            // 4. Soft delete
            try (PreparedStatement ps = conn.prepareStatement("UPDATE DKHACHHANG SET STATUS = -1 WHERE ID = ?")) {
                ps.setString(1, testKhachId);
                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                System.out.println("Soft delete Khach: rows=" + rows);
            }

            // 5. Cleanup
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM DKHACHHANG WHERE ID = ?")) {
                ps.setString(1, testKhachId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM DNHOMKHACHHANG WHERE ID = ?")) {
                ps.setString(1, testNhomId);
                ps.executeUpdate();
            }
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail("CRUD test failed: " + e.getMessage());
        }
    }

    @Test
    public void testDAOPersistence() {
        KhachHangDAO khDAO = new KhachHangDAO();
        NhomKhachHangDAO nkhDAO = new NhomKhachHangDAO();

        String testMaKhach = "TEST_DAO_" + System.currentTimeMillis();
        String testTenKhach = "Khách Hàng Test DAO";

        // 1. Test NhomKhachHangDAO insert
        NhomKhachHang testNhom = new NhomKhachHang();
        testNhom.setName("Nhóm Test DAO " + System.currentTimeMillis());
        testNhom.setTiLeGiamGia(3.5);
        testNhom.setDiemTichLuy(50.0);
        testNhom.setGhiChu("Ghi chú nhóm test");
        boolean insertNhomOk = nkhDAO.insert(testNhom);
        org.junit.jupiter.api.Assertions.assertTrue(insertNhomOk, "Insert NhomKhachHang should succeed in DB");
        System.out.println("Nhom created with ID: " + testNhom.getId());

        // 2. Test KhachHangDAO insert
        KhachHang testKh = new KhachHang();
        testKh.setMaKhach(testMaKhach);
        testKh.setName(testTenKhach);
        testKh.setDiaChi("123 Đường Test");
        testKh.setDienThoai("0900000999");
        testKh.setEmail("testdao@example.com");
        testKh.setNhomKhachHangId(testNhom.getId());
        testKh.setTenNhomKhachHang(testNhom.getName());
        testKh.setDiemTichLuyBanDau(100);
        testKh.setDiemTichLuy(100);
        testKh.setGiaBanIndex(1);
        testKh.setMaSoThue("MST9999");
        testKh.setTinhThanh("DONG NAI");
        testKh.setFacebook("fb.com/testdao");
        testKh.setGhiChu("Ghi chú khách test");

        boolean insertKhOk = khDAO.insert(testKh);
        org.junit.jupiter.api.Assertions.assertTrue(insertKhOk, "Insert KhachHang should succeed in DB");
        System.out.println("KhachHang created with ID: " + testKh.getId());

        // 3. Verify KhachHang is in DB by creating a NEW instance of KhachHangDAO (simulating restart)
        KhachHangDAO newKhDAO = new KhachHangDAO();
        KhachHang loadedKh = newKhDAO.findById(testKh.getId());
        org.junit.jupiter.api.Assertions.assertNotNull(loadedKh, "KhachHang should be loaded from DB on new DAO instance");
        org.junit.jupiter.api.Assertions.assertEquals(testTenKhach, loadedKh.getName());
        org.junit.jupiter.api.Assertions.assertEquals("0900000999", loadedKh.getDienThoai());

        // 4. Test Update
        loadedKh.setName(testTenKhach + " (Updated)");
        loadedKh.setDiaChi("456 Đường Mới");
        boolean updateOk = newKhDAO.update(loadedKh);
        org.junit.jupiter.api.Assertions.assertTrue(updateOk, "Update KhachHang should succeed in DB");

        // Verify update in another fresh DAO instance
        KhachHangDAO freshDAO = new KhachHangDAO();
        KhachHang reloadedKh = freshDAO.findById(testKh.getId());
        org.junit.jupiter.api.Assertions.assertNotNull(reloadedKh);
        org.junit.jupiter.api.Assertions.assertEquals(testTenKhach + " (Updated)", reloadedKh.getName());
        org.junit.jupiter.api.Assertions.assertEquals("456 Đường Mới", reloadedKh.getDiaChi());

        // 5. Test Move to trash & restore
        freshDAO.moveToTrash(testKh.getId());
        org.junit.jupiter.api.Assertions.assertNull(freshDAO.findActiveById(testKh.getId()), "Should not be in active list after move to trash");
        org.junit.jupiter.api.Assertions.assertNotNull(freshDAO.findTrashById(testKh.getId()), "Should be in trash list after move to trash");

        List<KhachHang> trash = freshDAO.getTrashItems();
        boolean foundInTrash = trash.stream().anyMatch(k -> testKh.getId().equals(k.getId()));
        org.junit.jupiter.api.Assertions.assertTrue(foundInTrash, "Should be found in trash list");

        freshDAO.restoreFromTrash(testKh.getId());
        org.junit.jupiter.api.Assertions.assertNotNull(freshDAO.findActiveById(testKh.getId()), "Should be back in active list after restore");

        // 6. Cleanup test data
        freshDAO.deletePermanently(testKh.getId());
        nkhDAO.deletePermanently(testNhom.getId());
        System.out.println("End-to-end DAO DB Persistence Test Passed Successfully!");
    }
}
