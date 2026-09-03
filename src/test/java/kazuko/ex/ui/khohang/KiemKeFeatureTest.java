package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.MatHang;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KiemKeFeatureTest {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private static String createdPhieuKiemKeId;

    @Test
    @Order(1)
    public void testGenerateSoPhieuKiemKe() {
        String soPhieu = donHangDAO.generateNextSoPhieuKiemKe(new Date());
        assertNotNull(soPhieu, "Số phiếu kiểm kê không được null");
        assertTrue(soPhieu.startsWith("PKK"), "Số phiếu kiểm kê phải có tiền tố PKK");
        assertTrue(soPhieu.matches("PKK\\d{2}/\\d{5}"), "Số phiếu kiểm kê phải có định dạng PKKyy/00001, thực tế: " + soPhieu);
        System.out.println("Generated Next So Phieu Kiem Ke: " + soPhieu);
    }

    @Test
    @Order(2)
    public void testCalculationChenhLechKiemKe() {
        // Case 1: Thừa hàng (Thực tế 10 > Hệ thống 8) -> Điều chỉnh nhập = 2, Điều chỉnh xuất = 0
        DonHangChiTiet ct1 = new DonHangChiTiet();
        ct1.setSlHeThong(8.0);
        ct1.setSlThucTe(10.0);
        assertEquals(2.0, ct1.getSlNhap(), 0.001, "Khi thực tế > hệ thống: Điều chỉnh nhập = 2");
        assertEquals(0.0, ct1.getSlXuat(), 0.001, "Khi thực tế > hệ thống: Điều chỉnh xuất = 0");

        // Case 2: Thiếu hàng (Thực tế 1 < Hệ thống 1000) -> Điều chỉnh xuất = 999, Điều chỉnh nhập = 0 (Như Ảnh 2)
        DonHangChiTiet ct2 = new DonHangChiTiet();
        ct2.setSlHeThong(1000.0);
        ct2.setSlThucTe(1.0);
        assertEquals(999.0, ct2.getSlXuat(), 0.001, "Khi thực tế < hệ thống: Điều chỉnh xuất = 999");
        assertEquals(0.0, ct2.getSlNhap(), 0.001, "Khi thực tế < hệ thống: Điều chỉnh nhập = 0");

        // Case 3: Âm hệ thống (Thực tế 1 > Hệ thống -1) -> Điều chỉnh nhập = 2, Điều chỉnh xuất = 0 (Như Ảnh 2 dòng 3)
        DonHangChiTiet ct3 = new DonHangChiTiet();
        ct3.setSlHeThong(-1.0);
        ct3.setSlThucTe(1.0);
        assertEquals(2.0, ct3.getSlNhap(), 0.001, "Khi thực tế 1 > hệ thống -1: Điều chỉnh nhập = 2");
        assertEquals(0.0, ct3.getSlXuat(), 0.001, "Khi thực tế 1 > hệ thống -1: Điều chỉnh xuất = 0");

        // Case 4: Khớp tồn (Thực tế 5 == Hệ thống 5) -> Điều chỉnh nhập = 0, Điều chỉnh xuất = 0
        DonHangChiTiet ct4 = new DonHangChiTiet();
        ct4.setSlHeThong(5.0);
        ct4.setSlThucTe(5.0);
        assertEquals(0.0, ct4.getSlNhap(), 0.001, "Khi thực tế == hệ thống: Điều chỉnh nhập = 0");
        assertEquals(0.0, ct4.getSlXuat(), 0.001, "Khi thực tế == hệ thống: Điều chỉnh xuất = 0");
    }

    @Test
    @Order(3)
    public void testInsertAndGetPhieuKiemKe() {
        List<KhoHang> khoList = khoHangDAO.getAll();
        assertFalse(khoList.isEmpty(), "Phải có ít nhất 1 kho hàng");
        KhoHang kho = khoList.get(0);

        List<MatHang> mhList = matHangDAO.getAll();
        assertFalse(mhList.isEmpty(), "Phải có ít nhất 1 mặt hàng");
        MatHang mh = mhList.get(0);

        DonHang dh = new DonHang();
        dh.setId(UUID.randomUUID().toString());
        dh.setName(donHangDAO.generateNextSoPhieuKiemKe(new Date()));
        dh.setNgay(new Date());
        dh.setDienGiai("Kiểm kê định kỳ Test");
        dh.setGhiChu("Ghi chú kiểm kê tự động");
        dh.setKhoXuatId(kho.getId());
        dh.setKhoNhapId(kho.getId());
        dh.setTenKhoXuat(kho.getName());
        dh.setTenKhoNhap(kho.getName());

        List<DonHangChiTiet> details = new ArrayList<>();
        DonHangChiTiet ct = new DonHangChiTiet();
        ct.setMatHangId(mh.getId());
        ct.setMaHang(mh.getCode());
        ct.setTenHang(mh.getName());
        ct.setDonViTinhId(mh.getDonViTinhId());
        ct.setTenDonViTinh(mh.getTenDonViTinh());
        ct.setDonGia(mh.getGiaVon());
        ct.setSlHeThong(10.0);
        ct.setSlThucTe(12.0); // Thừa 2 -> Nhập điều chỉnh = 2
        details.add(ct);

        dh.setChiTietLayHang(details);

        boolean inserted = donHangDAO.insertPhieuKiemKe(dh);
        assertTrue(inserted, "Phải thêm phiếu kiểm kê thành công");
        createdPhieuKiemKeId = dh.getId();

        // Query by ID
        DonHang reloaded = donHangDAO.getPhieuKiemKeById(createdPhieuKiemKeId);
        assertNotNull(reloaded, "Phải tìm thấy phiếu kiểm kê vừa tạo");
        assertEquals(4, reloaded.getLoai(), "LOAI phải là 4 (Kiểm kê kho)");
        assertEquals(kho.getId(), reloaded.getKhoXuatId(), "Kho kiểm kê phải khớp");
        assertNotNull(reloaded.getChiTietLayHang(), "Chi tiết kiểm kê không được rỗng");
        assertEquals(1, reloaded.getChiTietLayHang().size(), "Chi tiết phải có 1 dòng");

        DonHangChiTiet detailReloaded = reloaded.getChiTietLayHang().get(0);
        assertEquals(10.0, detailReloaded.getSlHeThong(), 0.001);
        assertEquals(12.0, detailReloaded.getSlThucTe(), 0.001);
        assertEquals(2.0, detailReloaded.getSlNhap(), 0.001);
        assertEquals(0.0, detailReloaded.getSlXuat(), 0.001);

        // Update
        reloaded.setGhiChu("Ghi chú đã cập nhật");
        detailReloaded.setSlThucTe(7.0); // Đổi thực tế thành 7 < 10 -> Thiếu 3 -> Xuất điều chỉnh = 3
        boolean updated = donHangDAO.updatePhieuKiemKe(reloaded);
        assertTrue(updated, "Phải cập nhật phiếu kiểm kê thành công");

        DonHang updatedDh = donHangDAO.getPhieuKiemKeById(createdPhieuKiemKeId);
        assertEquals("Ghi chú đã cập nhật", updatedDh.getGhiChu());
        assertEquals(3.0, updatedDh.getChiTietLayHang().get(0).getSlXuat(), 0.001, "Điều chỉnh xuất phải là 3");
        assertEquals(0.0, updatedDh.getChiTietLayHang().get(0).getSlNhap(), 0.001, "Điều chỉnh nhập phải là 0");

        // Delete
        boolean deleted = donHangDAO.deletePhieuKiemKe(createdPhieuKiemKeId);
        assertTrue(deleted, "Phải xóa phiếu kiểm kê thành công");
    }

    @Test
    @Order(4)
    public void testUserConstraintWarehouseEditable() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        SwingUtilities.invokeAndWait(() -> {
            // Case 1: Khi tạo mới phiếu kiểm kê -> cbKhoHang phải ENABLED (cho phép chọn kho)
            PhieuKiemKeDialog dlgNew = new PhieuKiemKeDialog(null, null);
            try {
                Field fldKho = PhieuKiemKeDialog.class.getDeclaredField("cbKhoHang");
                fldKho.setAccessible(true);
                JComboBox<?> cbKho = (JComboBox<?>) fldKho.get(dlgNew);
                assertTrue(cbKho.isEnabled(), "Khi tạo mới phiếu kiểm kê, trường kho hàng phải ENABLED");
            } catch (Exception e) {
                fail(e.getMessage());
            } finally {
                dlgNew.dispose();
            }

            // Case 2: Khi chỉnh sửa phiếu kiểm kê -> cbKhoHang phải DISABLED (không cho editable)
            DonHang existingDh = new DonHang();
            existingDh.setId("EXISTING-TEST-ID");
            existingDh.setName("PKK26/99999");
            existingDh.setNgay(new Date());
            existingDh.setKhoXuatId("1");
            existingDh.setDienGiai("Kiểm kê");

            PhieuKiemKeDialog dlgEdit = new PhieuKiemKeDialog(null, existingDh, null);
            try {
                Field fldKho = PhieuKiemKeDialog.class.getDeclaredField("cbKhoHang");
                fldKho.setAccessible(true);
                JComboBox<?> cbKho = (JComboBox<?>) fldKho.get(dlgEdit);
                assertFalse(cbKho.isEnabled(), "Khi chỉnh sửa phiếu kiểm kê, trường kho hàng KHÔNG ĐƯỢC editable (phải disabled)");
            } catch (Exception e) {
                fail(e.getMessage());
            } finally {
                dlgEdit.dispose();
            }

            // Case 3: Panel QuanLyKiemKePanel khởi tạo thành công
            QuanLyKiemKePanel panel = new QuanLyKiemKePanel();
            assertNotNull(panel, "QuanLyKiemKePanel phải khởi tạo thành công");
        });
    }
}
