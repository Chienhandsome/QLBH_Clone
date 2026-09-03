package kazuko.ex.ui.dathang;

import kazuko.ex.dao.DatHangNCCDAO;
import kazuko.ex.model.dathang.DatHangNCC;
import kazuko.ex.model.dathang.DatHangNCCChiTiet;
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
public class DatHangNCCFeatureTest {

    private final DatHangNCCDAO datHangNCCDAO = new DatHangNCCDAO();

    @Test
    @Order(1)
    public void testGenerateNextSoPhieu() {
        String nextSoPhieu = datHangNCCDAO.generateNextSoPhieu(new Date());
        assertNotNull(nextSoPhieu, "Số phiếu đặt hàng không được null");
        assertTrue(nextSoPhieu.startsWith("DMH"), "Số phiếu đặt hàng phải bắt đầu bằng 'DMH'");
        assertTrue(nextSoPhieu.contains("/"), "Số phiếu đặt hàng phải chứa dấu '/'");
        System.out.println("Generated Next So Phieu Dat Hang: " + nextSoPhieu);
    }

    @Test
    @Order(2)
    public void testInsertRetrieveUpdateDeleteOrder() {
        String testId = UUID.randomUUID().toString();
        DatHangNCC order = new DatHangNCC();
        order.setId(testId);
        order.setName("DMH26/TEST01");
        order.setNgay(new Date());
        order.setTienHang(127120.0);
        order.setTiLeGiamGia(0.0);
        order.setTienGiamGia(0.0);
        order.setTiLeThue(0.0);
        order.setTienThue(0.0);
        order.setPhiVanChuyen(0.0);
        order.setTongCong(127120.0);
        order.setNote("Đơn đặt hàng thử nghiệm");

        List<DatHangNCCChiTiet> details = new ArrayList<>();

        // Mặt hàng 1: 4k0
        DatHangNCCChiTiet ct1 = new DatHangNCCChiTiet();
        ct1.setMatHangId("1012");
        ct1.setMaHang("4k0");
        ct1.setTenHang("4K0");
        ct1.setSoLuong(1.0);
        ct1.setDonGia(40000.0);
        ct1.setThanhTien(40000.0);
        details.add(ct1);

        // Mặt hàng 2: 4k9
        DatHangNCCChiTiet ct2 = new DatHangNCCChiTiet();
        ct2.setMatHangId("1013");
        ct2.setMaHang("4k9");
        ct2.setTenHang("4K9");
        ct2.setSoLuong(1.0);
        ct2.setDonGia(87120.0);
        ct2.setThanhTien(87120.0);
        details.add(ct2);

        // Mặt hàng 3: 1900002
        DatHangNCCChiTiet ct3 = new DatHangNCCChiTiet();
        ct3.setMatHangId("1006");
        ct3.setMaHang("1900002");
        ct3.setTenHang("ALYNA Bấm móng nhỏ");
        ct3.setSoLuong(1.0);
        ct3.setDonGia(0.0);
        ct3.setThanhTien(0.0);
        details.add(ct3);

        order.setChiTiet(details);

        // 1. Insert
        boolean inserted = datHangNCCDAO.insert(order);
        assertTrue(inserted, "Phải insert đơn đặt hàng thành công");

        // 2. Retrieve & Verify
        DatHangNCC retrieved = datHangNCCDAO.getById(testId);
        assertNotNull(retrieved, "Phải tìm thấy đơn đặt hàng vừa thêm");
        assertEquals("DMH26/TEST01", retrieved.getName());
        assertEquals(127120.0, retrieved.getTongCong(), 0.01);
        assertNotNull(retrieved.getChiTiet());
        assertEquals(3, retrieved.getChiTiet().size(), "Đơn đặt hàng phải có đúng 3 mặt hàng");

        // 3. Update
        retrieved.setNote("Đã cập nhật ghi chú đơn đặt hàng");
        retrieved.setPhiVanChuyen(10000.0);
        retrieved.setTongCong(137120.0);
        boolean updated = datHangNCCDAO.update(retrieved);
        assertTrue(updated, "Phải update thành công");

        DatHangNCC updatedOrder = datHangNCCDAO.getById(testId);
        assertNotNull(updatedOrder);
        assertEquals("Đã cập nhật ghi chú đơn đặt hàng", updatedOrder.getNote());
        assertEquals(137120.0, updatedOrder.getTongCong(), 0.01);

        // 4. Delete (Soft delete)
        boolean deleted = datHangNCCDAO.delete(testId);
        assertTrue(deleted, "Phải soft delete thành công");

        List<DatHangNCC> activeList = datHangNCCDAO.getAll(null, null, null, "TEST01");
        assertTrue(activeList.isEmpty(), "Đơn đặt hàng đã xóa không được xuất hiện trong danh sách active");
    }

    @Test
    @Order(3)
    public void testDatHangNhaCungCapPanelInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DatHangNhaCungCapPanel panel = new DatHangNhaCungCapPanel();
            assertNotNull(panel.getTableMaster(), "Bảng master tableMaster phải được khởi tạo");
            assertNotNull(panel.getMasterModel(), "masterModel phải được khởi tạo");
        });
    }

    @Test
    @Order(4)
    public void testDatHangNhaCungCapDialogInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            DatHangNhaCungCapDialog dlg = new DatHangNhaCungCapDialog(frame, null);
            assertNotNull(dlg);
            assertTrue(dlg.getTitle().contains("THÊM MỚI"));
            dlg.dispose();
            frame.dispose();
        });
    }
}
