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
public class NhapKhoFeatureTest {

    private final DonHangDAO donHangDAO = new DonHangDAO();

    @Test
    @Order(1)
    public void testGenerateNextSoPhieuNhap() {
        String nextSoPhieu = donHangDAO.generateNextSoPhieuNhap(new Date());
        assertNotNull(nextSoPhieu, "Số phiếu nhập không được null");
        assertTrue(nextSoPhieu.startsWith("PN"), "Số phiếu nhập phải bắt đầu bằng 'PN'");
        assertTrue(nextSoPhieu.contains("/"), "Số phiếu nhập phải chứa dấu '/'");
        System.out.println("Generated Next So Phieu Nhap: " + nextSoPhieu);
    }

    @Test
    @Order(2)
    public void testInsertRetrieveUpdateDeletePhieuNhap() {
        // Tạo phiếu mẫu tương tự Hình 2 & Hình 3
        DonHang dh = new DonHang();
        String testId = UUID.randomUUID().toString();
        dh.setId(testId);
        dh.setName("PN26/TEST01");
        dh.setNgay(new Date());
        dh.setDienGiai("Nhập mua hàng");
        dh.setKhoNhapId("k-2");
        dh.setTienHang(70000.0);
        dh.setTiLeGiamGia(0.0);
        dh.setTienGiamGia(0.0);
        dh.setTiLeThue(0.0);
        dh.setTienThue(0.0);
        dh.setPhiVanChuyen(0.0);
        dh.setTongCong(70000.0);
        dh.setTienThanhToan(0.0);
        dh.setConLai(70000.0);
        dh.setGhiChu("Đơn hàng kiểm thử tự động");

        List<DonHangChiTiet> details = new ArrayList<>();

        // Mặt hàng 1: 4K0
        DonHangChiTiet ct1 = new DonHangChiTiet();
        ct1.setMatHangId("1012");
        ct1.setMaHang("4k0");
        ct1.setTenHang("4K0");
        ct1.setSoLuong(1.0);
        ct1.setDonGia(40000.0);
        ct1.setThanhTien(40000.0);
        details.add(ct1);

        // Mặt hàng 2: 1900001
        DonHangChiTiet ct2 = new DonHangChiTiet();
        ct2.setMatHangId("1002");
        ct2.setMaHang("1900001");
        ct2.setTenHang("ALYNA Bấm móng lớn");
        ct2.setSoLuong(1.0);
        ct2.setDonGia(0.0);
        ct2.setThanhTien(0.0);
        details.add(ct2);

        // Mặt hàng 3: 1300002
        DonHangChiTiet ct3 = new DonHangChiTiet();
        ct3.setMatHangId("1003");
        ct3.setMaHang("1300002");
        ct3.setTenHang("ALYNA BB Cream 2 màu kem puff hộp");
        ct3.setSoLuong(1.0);
        ct3.setDonGia(0.0);
        ct3.setThanhTien(0.0);
        details.add(ct3);

        // Mặt hàng 4: 3K0
        DonHangChiTiet ct4 = new DonHangChiTiet();
        ct4.setMatHangId("1011");
        ct4.setMaHang("3k0");
        ct4.setTenHang("3K0");
        ct4.setSoLuong(1.0);
        ct4.setDonGia(30000.0);
        ct4.setThanhTien(30000.0);
        details.add(ct4);

        dh.setChiTietLayHang(details);

        // 1. Insert
        boolean inserted = donHangDAO.insertPhieuNhapKho(dh);
        assertTrue(inserted, "Phải insert phiếu nhập kho thành công");

        // 2. Retrieve & Verify
        DonHang retrieved = donHangDAO.getPhieuNhapById(testId);
        assertNotNull(retrieved, "Phải tìm thấy phiếu nhập vừa insert");
        assertEquals("PN26/TEST01", retrieved.getName());
        assertEquals("Nhập mua hàng", retrieved.getDienGiai());
        assertEquals(70000.0, retrieved.getTongCong(), 0.01);
        assertNotNull(retrieved.getChiTietLayHang());
        assertEquals(4, retrieved.getChiTietLayHang().size(), "Phải có đúng 4 mặt hàng trong chi tiết");

        // 3. Update
        retrieved.setGhiChu("Đã cập nhật ghi chú");
        retrieved.setPhiVanChuyen(15000.0);
        retrieved.setTongCong(85000.0);
        boolean updated = donHangDAO.updatePhieuNhapKho(retrieved);
        assertTrue(updated, "Phải update phiếu nhập thành công");

        DonHang updatedDh = donHangDAO.getPhieuNhapById(testId);
        assertNotNull(updatedDh);
        assertEquals("Đã cập nhật ghi chú", updatedDh.getGhiChu());
        assertEquals(85000.0, updatedDh.getTongCong(), 0.01);

        // 4. Delete (Soft delete)
        boolean deleted = donHangDAO.deletePhieuNhapKho(testId);
        assertTrue(deleted, "Phải xóa thành công phiếu nhập");

        List<DonHang> activeList = donHangDAO.getPhieuNhapKho(null, null, null, null, null, "TEST01");
        assertTrue(activeList.isEmpty(), "Phiếu bị xóa không được xuất hiện trong danh sách active");
    }

    @Test
    @Order(3)
    public void testQuanLyNhapKhoPanelInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            QuanLyNhapKhoPanel panel = new QuanLyNhapKhoPanel();
            assertNotNull(panel.getTablePhieuNhap(), "Bảng master tablePhieuNhap phải được khởi tạo");
            assertNotNull(panel.getMasterModel(), "masterModel phải được khởi tạo");
            assertNotNull(panel.getCbKhoNhapFilter(), "Dropdown bộ lọc kho nhập phải được khởi tạo");
            assertNotNull(panel.getCbNhanVienFilter(), "Dropdown bộ lọc nhân viên phải được khởi tạo");
            assertNotNull(panel.getCbNhaCungCapFilter(), "Dropdown bộ lọc nhà cung cấp phải được khởi tạo");
        });
    }

    @Test
    @Order(4)
    public void testPhieuNhapKhoDialogInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            PhieuNhapKhoDialog dlg = new PhieuNhapKhoDialog(frame, null);
            assertNotNull(dlg, "PhieuNhapKhoDialog phải được khởi tạo thành công");
            assertTrue(dlg.getTitle().contains("THÊM MỚI"), "Tiêu đề dialog phải là THÊM MỚI");
            dlg.dispose();
            frame.dispose();
        });
    }
}
