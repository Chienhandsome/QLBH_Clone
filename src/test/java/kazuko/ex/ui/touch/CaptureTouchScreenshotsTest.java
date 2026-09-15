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

        // 1. Thong Ke
        TouchThongKeBanHangDialog dlgThongKe = new TouchThongKeBanHangDialog(null);
        dlgThongKe.setSize(1024, 720);
        saveComponentImage(dlgThongKe.getContentPane(), 1024, 720, new File(outDir, "preview_thongke.png"));

        // 2. Thanh Toan
        DonHang dh = new DonHang();
        dh.setTongCong(1668000);
        TouchXacNhanThanhToanDialog dlgThanhToan = new TouchXacNhanThanhToanDialog(null, dh, true);
        dlgThanhToan.setSize(980, 560);
        saveComponentImage(dlgThanhToan.getContentPane(), 980, 560, new File(outDir, "preview_thanhtoan.png"));

        // 3. Mo Dat Hang
        TouchMoDatHangDialog dlgMoDatHang = new TouchMoDatHangDialog(null);
        dlgMoDatHang.setSize(980, 600);
        saveComponentImage(dlgMoDatHang.getContentPane(), 980, 600, new File(outDir, "preview_modathang.png"));
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