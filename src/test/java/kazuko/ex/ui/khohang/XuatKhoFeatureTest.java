package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.KhoHang;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class XuatKhoFeatureTest {

    private final DonHangDAO donHangDAO = new DonHangDAO();

    @Test
    public void testLoadPhieuXuatKhoAndDetails() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date tuNgay = sdf.parse("2026-08-01");
        Date denNgay = sdf.parse("2026-09-03");

        List<DonHang> list = donHangDAO.getPhieuXuatKho(tuNgay, denNgay, null);
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() >= 1, "Cần có ít nhất 1 phiếu xuất kho trong DB demo");

        DonHang px1 = null;
        for (DonHang dh : list) {
            if ("PX26/00001".equalsIgnoreCase(dh.getName())) {
                px1 = dh;
                break;
            }
        }

        Assertions.assertNotNull(px1, "Cần tìm thấy phiếu PX26/00001");
        Assertions.assertEquals(10384000000.0, px1.getTongCong(), 0.01, "Tổng cộng phải bằng 10,384,000,000");

        // Chi tiết phiếu
        List<DonHangChiTiet> details = donHangDAO.getChiTietPhieuXuat(px1.getId());
        Assertions.assertEquals(5, details.size(), "Phiếu PX26/00001 phải có đúng 5 mặt hàng chi tiết");

        // Auto generate next voucher code
        String nextCode = donHangDAO.generateNextSoPhieuXuat(new Date());
        Assertions.assertNotNull(nextCode);
        Assertions.assertTrue(nextCode.startsWith("PX26/"), "Số phiếu phải bắt đầu bằng PX26/");
        System.out.println("Generated Next So Phieu: " + nextCode);
    }

    @Test
    public void testQuanLyXuatKhoPanelInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            QuanLyXuatKhoPanel panel = new QuanLyXuatKhoPanel();
            Assertions.assertNotNull(panel);
        });
    }

    @Test
    public void testPhieuXuatKhoDialogInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame dummyFrame = new JFrame();
            PhieuXuatKhoDialog dialogAdd = new PhieuXuatKhoDialog(dummyFrame, () -> {});
            Assertions.assertNotNull(dialogAdd);
            Assertions.assertEquals("PHIẾU XUẤT KHO - THÊM MỚI", dialogAdd.getTitle());
            dialogAdd.dispose();
            dummyFrame.dispose();
        });
    }

    @Test
    public void testInsertAndUpdateAndDeletePhieuXuatKho() {
        DonHang dh = new DonHang();
        dh.setName("PX26/TEST99");
        dh.setNgay(new Date());
        dh.setDienGiai("Xuất khác test");
        dh.setKhoXuatId("fe4bb481-5d0a-41d7-8cc4-4baa198ba466");
        dh.setTienHang(500000);
        dh.setTongCong(500000);
        dh.setTienThanhToan(500000);

        DonHangChiTiet ct = new DonHangChiTiet();
        ct.setMatHangId("1012");
        ct.setMaHang("1012");
        ct.setTenHang("Bếp từ SANAKY 1012");
        ct.setSoLuong(2);
        ct.setDonGia(250000);
        ct.setThanhTien(500000);
        dh.setChiTietLayHang(List.of(ct));

        // 1. Insert
        boolean inserted = donHangDAO.insertPhieuXuatKho(dh);
        Assertions.assertTrue(inserted, "Phải insert phiếu xuất kho thành công");

        // 2. Query back
        DonHang loaded = donHangDAO.getPhieuXuatById(dh.getId());
        Assertions.assertNotNull(loaded, "Phải query được phiếu vừa insert");
        Assertions.assertEquals("PX26/TEST99", loaded.getName());
        Assertions.assertEquals(1, loaded.getChiTietLayHang().size(), "Phải có 1 dòng chi tiết");

        // 3. Update
        loaded.setDienGiai("Xuất khác đã cập nhật");
        loaded.setTongCong(600000);
        boolean updated = donHangDAO.updatePhieuXuatKho(loaded);
        Assertions.assertTrue(updated, "Phải update phiếu xuất kho thành công");

        DonHang reloaded = donHangDAO.getPhieuXuatById(dh.getId());
        Assertions.assertEquals("Xuất khác đã cập nhật", reloaded.getDienGiai());

        // 4. Delete
        boolean deleted = donHangDAO.deletePhieuXuatKho(dh.getId());
        Assertions.assertTrue(deleted, "Phải delete (đổi status = -1) thành công");
    }
}
