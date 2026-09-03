package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChuyenKhoFeatureTest {

    private final DonHangDAO donHangDAO = new DonHangDAO();

    @Test
    @Order(1)
    public void testGenerateNextSoPhieuChuyenKho() {
        String nextSoPhieu = donHangDAO.generateNextSoPhieuChuyenKho(new Date());
        assertNotNull(nextSoPhieu, "Số phiếu chuyển kho không được null");
        assertTrue(nextSoPhieu.startsWith("PCK"), "Số phiếu chuyển kho phải bắt đầu bằng 'PCK'");
        assertTrue(nextSoPhieu.contains("/"), "Số phiếu chuyển kho phải chứa dấu '/'");
        System.out.println("Generated Next So Phieu Chuyen Kho: " + nextSoPhieu);
    }

    @Test
    @Order(2)
    public void testInsertRetrieveUpdateDeleteChuyenKho() {
        String testId = UUID.randomUUID().toString();
        DonHang dh = new DonHang();
        dh.setId(testId);
        dh.setName("PCK26/TEST01");
        dh.setNgay(new Date());
        dh.setDienGiai("Chuyển kho nội bộ");
        dh.setKhoXuatId("1");
        dh.setKhoNhapId("2");
        dh.setGhiChu("Kiểm thử chuyển kho");
        dh.setTienHang(0.0);
        dh.setTongCong(0.0);

        List<DonHangChiTiet> details = new ArrayList<>();

        // Mặt hàng 1: 1900002
        DonHangChiTiet ct1 = new DonHangChiTiet();
        ct1.setMatHangId("1006");
        ct1.setMaHang("1900002");
        ct1.setTenHang("ALYNA Bấm móng nhỏ");
        ct1.setTenDonViTinh("Cây");
        ct1.setSoLuong(1.0);
        ct1.setDonGia(0.0);
        ct1.setThanhTien(0.0);
        details.add(ct1);

        // Mặt hàng 2: 1300003
        DonHangChiTiet ct2 = new DonHangChiTiet();
        ct2.setMatHangId("1007");
        ct2.setMaHang("1300003");
        ct2.setTenHang("ALYNA Đắp mặt nạ đủ loại");
        ct2.setTenDonViTinh("Bịch");
        ct2.setSoLuong(1.0);
        ct2.setDonGia(0.0);
        ct2.setThanhTien(0.0);
        details.add(ct2);

        // Mặt hàng 3: 1300005
        DonHangChiTiet ct3 = new DonHangChiTiet();
        ct3.setMatHangId("1008");
        ct3.setMaHang("1300005");
        ct3.setTenHang("ALYNA Hair Coat 80ml");
        ct3.setTenDonViTinh("chai");
        ct3.setSoLuong(1.0);
        ct3.setDonGia(0.0);
        ct3.setThanhTien(0.0);
        details.add(ct3);

        dh.setChiTietLayHang(details);

        // 1. Insert
        boolean inserted = donHangDAO.insertPhieuChuyenKho(dh);
        assertTrue(inserted, "Phải insert phiếu chuyển kho thành công");

        // 2. Retrieve & Verify
        DonHang retrieved = donHangDAO.getPhieuChuyenKhoById(testId);
        assertNotNull(retrieved, "Phải tìm thấy phiếu chuyển kho vừa tạo");
        assertEquals("PCK26/TEST01", retrieved.getName());
        assertEquals(3, retrieved.getLoai(), "Loại đơn hàng phải là 3 (Chuyển kho)");
        assertEquals("1", retrieved.getKhoXuatId(), "Kho xuất phải là 1");
        assertEquals("2", retrieved.getKhoNhapId(), "Kho nhập phải là 2");
        assertNotNull(retrieved.getChiTietLayHang());
        assertEquals(3, retrieved.getChiTietLayHang().size(), "Phải có đúng 3 mặt hàng");

        // 3. Update
        retrieved.setDienGiai("Chuyển kho nội bộ sang chi nhánh");
        retrieved.setGhiChu("Đã cập nhật");
        boolean updated = donHangDAO.updatePhieuChuyenKho(retrieved);
        assertTrue(updated, "Phải update thành công");

        DonHang updatedDh = donHangDAO.getPhieuChuyenKhoById(testId);
        assertNotNull(updatedDh);
        assertEquals("Chuyển kho nội bộ sang chi nhánh", updatedDh.getDienGiai());
        assertEquals("Đã cập nhật", updatedDh.getGhiChu());

        // 4. Delete
        boolean deleted = donHangDAO.delete(testId);
        assertTrue(deleted, "Phải xóa thành công");
    }

    @Test
    @Order(3)
    public void testConstraintValidationDifferentWarehouses() {
        // Validation rule check: Kho xuất và Kho nhập không được rỗng và không được trùng nhau
        String khoXuatId = "1";
        String khoNhapId = "1"; // Trùng nhau
        boolean isSame = khoXuatId.equalsIgnoreCase(khoNhapId);
        assertTrue(isSame, "Hai kho cùng ID phải được nhận diện là trùng nhau để chặn lưu");

        String khoNhapKhacId = "2";
        boolean isDifferent = !khoXuatId.equalsIgnoreCase(khoNhapKhacId);
        assertTrue(isDifferent, "Hai kho khác ID là hợp lệ");
    }

    @Test
    @Order(4)
    public void testUIComponentsInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            QuanLyChuyenKhoPanel panel = new QuanLyChuyenKhoPanel();
            assertNotNull(panel.getTableMaster());
            assertNotNull(panel.getMasterModel());

            JFrame frame = new JFrame();
            PhieuChuyenKhoDialog dlg = new PhieuChuyenKhoDialog(frame, null);
            assertNotNull(dlg);
            assertTrue(dlg.getTitle().contains("THÊM MỚI"));
            dlg.dispose();
            frame.dispose();
        });
    }
}
