package kazuko.ex.ui.touch;

import kazuko.ex.model.banhang.DonHang;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class CaptureTouchScreenshotsTest {

    @Test
    void captureScreenshots() throws Exception {
        System.setProperty("java.awt.headless", "false");
        String outDir = "C:/Users/Windows/.gemini/antigravity/brain/e854641a-868f-4f57-98e5-3c4d42b5061a";

        // 1. Nhap So Luong (Image 2)
        TouchNumberInputDialog dlgNum = new TouchNumberInputDialog(null, "NHẬP SỐ LƯỢNG - áp suất cuckchen", 1.0, 0.01, 999999);
        saveComponentImage(dlgNum.getContentPane(), 320, 460, new File(outDir, "preview_soluong_popup.png"));

        // 2. Mo Dat Hang (Image 3)
        TouchMoDatHangDialog dlgMoDatHang = new TouchMoDatHangDialog(null);
        saveComponentImage(dlgMoDatHang.getContentPane(), 660, 480, new File(outDir, "preview_modathang_refined.png"));

        // 3. Setting Ban Hang (Default mode)
        TouchThietLapCotBanHangDialog dlgSettingDefault = new TouchThietLapCotBanHangDialog(null, null);
        saveComponentImage(dlgSettingDefault.getContentPane(), 980, 580, new File(outDir, "preview_setting_banhang_default.png"));

        // 4. Setting Ban Hang (Formatting mode)
        TouchThietLapCotBanHangDialog dlgSettingFormat = new TouchThietLapCotBanHangDialog(null, null);
        dlgSettingFormat.toggleFormattingMode();
        saveComponentImage(dlgSettingFormat.getContentPane(), 980, 580, new File(outDir, "preview_setting_banhang_formatted.png"));

        // 5. Quản Lý Bán Hàng - Hình 1: Màn hình chính
        TouchQuanLyBanHangPanel qlbhPanel = new TouchQuanLyBanHangPanel(() -> {});
        DonHang dhSample = createSampleDonHang();
        java.util.List<DonHang> sampleList = new java.util.ArrayList<>();
        sampleList.add(dhSample);
        for (int i = 2; i <= 6; i++) {
            DonHang dhOther = new DonHang();
            dhOther.setName("2600000" + i);
            dhOther.setNgay(new java.util.Date());
            dhOther.setTenKhach("Khách hàng 0" + i);
            dhOther.setNguoiThanhToan("Administrator");
            dhOther.setTongCong(150000 * i);
            dhOther.setTienThanhToan(150000 * i);
            sampleList.add(dhOther);
        }
        qlbhPanel.setSampleBillsForTest(sampleList);
        saveComponentImage(qlbhPanel, 1100, 650, new File(outDir, "preview_qlbh_main.png"));

        // 6. Quản Lý Bán Hàng - Hình 2: Thiết lập cột hiển thị
        TouchThietLapCotQuanLyBanHangDialog dlgSettingQlbh = new TouchThietLapCotQuanLyBanHangDialog(null, null);
        saveComponentImage(dlgSettingQlbh.getContentPane(), 1080, 620, new File(outDir, "preview_qlbh_setting.png"));

        // 7. Quản Lý Bán Hàng - Hình 3: Danh sách bill hủy
        TouchDanhSachBillHuyDialog dlgBillHuy = new TouchDanhSachBillHuyDialog(null);
        saveComponentImage(dlgBillHuy.getContentPane(), 1080, 620, new File(outDir, "preview_qlbh_billhuy.png"));

        // 8. Quản Lý Bán Hàng - Hình 4: Thêm hàng / Tìm kiếm cảm ứng
        TouchThemHangDialog dlgThemHang = new TouchThemHangDialog(null, (mh, sl) -> {});
        saveComponentImage(dlgThemHang.getContentPane(), 1080, 640, new File(outDir, "preview_qlbh_themhang.png"));

        // 9. Quản Lý Bán Hàng - Hình 5: Tab Giao Hàng
        qlbhPanel.switchTabForTest(true);
        saveComponentImage(qlbhPanel, 1100, 650, new File(outDir, "preview_qlbh_giaohang.png"));
    }

    @Test
    void testBillSelection() {
        // 1. Test TouchQuanLyBanHangPanel
        TouchQuanLyBanHangPanel qlbh = new TouchQuanLyBanHangPanel(() -> {});
        java.util.List<DonHang> list = new java.util.ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            DonHang dh = new DonHang();
            dh.setId("ID" + i);
            dh.setName("2600000" + i);
            dh.setTongCong(100000 * i);
            list.add(dh);
        }
        qlbh.setSampleBillsForTest(list);
        org.junit.jupiter.api.Assertions.assertEquals("ID1", qlbh.getCurrentBillForTest().getId());

        // Click on the second card's child component (a JLabel inside card 2)
        JPanel grid = qlbh.getBillsGridPanelForTest();
        JPanel card2 = (JPanel) grid.getComponent(1);
        Component child = card2.getComponent(0);
        for (java.awt.event.MouseListener ml : child.getMouseListeners()) {
            ml.mousePressed(new java.awt.event.MouseEvent(child, java.awt.event.MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 5, 5, 1, false));
        }
        org.junit.jupiter.api.Assertions.assertEquals("ID2", qlbh.getCurrentBillForTest().getId());

        // 2. Test TouchDanhSachBillHuyDialog
        TouchDanhSachBillHuyDialog dlgHuy = new TouchDanhSachBillHuyDialog(null);
        dlgHuy.setSampleBillsForTest(list);
        org.junit.jupiter.api.Assertions.assertEquals("ID1", dlgHuy.getSelectedBillForTest().getId());

        JPanel cardsPanel = dlgHuy.getCardsListPanelForTest();
        Component comp2 = cardsPanel.getComponent(2);
        org.junit.jupiter.api.Assertions.assertTrue(comp2 instanceof JPanel);
        JPanel huyCard2 = (JPanel) comp2;
        Component deepChild = huyCard2.getComponent(0);
        for (java.awt.event.MouseListener ml : deepChild.getMouseListeners()) {
            ml.mousePressed(new java.awt.event.MouseEvent(deepChild, java.awt.event.MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 5, 5, 1, false));
        }
        org.junit.jupiter.api.Assertions.assertEquals("ID2", dlgHuy.getSelectedBillForTest().getId());
    }

    private DonHang createSampleDonHang() {
        DonHang dh = new DonHang();
        dh.setName("26000001");
        dh.setNgay(new java.util.Date());
        dh.setTenKhach("Anh Tuấn");
        dh.setNguoiThanhToan("Administrator");
        dh.setTenNhanVien("Administrator");
        dh.setDiaChiKhach("123 Lê Lợi, Quận 1, TP.HCM");
        dh.setDiaChiGiaoHang("123 Lê Lợi, Quận 1, TP.HCM");
        dh.setNhanVienXuatId("NV01");
        dh.setGhiChu("Giao trước 17h, gọi trước khi đến");
        dh.setTienHang(320000);
        dh.setTiLeGiamGia(0);
        dh.setTienGiamGia(0);
        dh.setTongCong(320000);
        dh.setTienThanhToan(320000);

        kazuko.ex.model.banhang.DonHangChiTiet ct1 = new kazuko.ex.model.banhang.DonHangChiTiet();
        ct1.setTenHang("Trà sữa trân châu hoàng gia");
        ct1.setSoLuong(2);
        ct1.setDonGia(50000);
        ct1.setThanhTien(100000);

        kazuko.ex.model.banhang.DonHangChiTiet ct2 = new kazuko.ex.model.banhang.DonHangChiTiet();
        ct2.setTenHang("Cà phê muối thượng hạng");
        ct2.setSoLuong(4);
        ct2.setDonGia(55000);
        ct2.setThanhTien(220000);

        java.util.List<kazuko.ex.model.banhang.DonHangChiTiet> list = new java.util.ArrayList<>();
        list.add(ct1);
        list.add(ct2);
        dh.setChiTietLayHang(list);
        return dh;
    }

    private void saveComponentImage(Container comp, int w, int h, File file) throws Exception {
        comp.setSize(w, h);
        comp.doLayout();
        layoutRecursive(comp);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        comp.printAll(g2);
        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    private void layoutRecursive(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                layoutRecursive((Container) child);
            }
        }
    }
}