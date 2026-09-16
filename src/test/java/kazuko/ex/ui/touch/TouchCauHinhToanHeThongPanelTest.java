package kazuko.ex.ui.touch;

import kazuko.ex.model.system.SystemConfig;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class TouchCauHinhToanHeThongPanelTest {

    @Test
    void panelInitializesAndLoadsConfig() {
        TouchCauHinhToanHeThongPanel panel = new TouchCauHinhToanHeThongPanel(() -> {});
        assertNotNull(panel);
        assertNotNull(panel.getCurrentConfig());
        assertEquals("Thông tin chung", panel.getCurrentActiveTab());
    }

    @Test
    void testTabSwitching() {
        TouchCauHinhToanHeThongPanel panel = new TouchCauHinhToanHeThongPanel(() -> {});
        assertEquals("Thông tin chung", panel.getCurrentActiveTab());

        panel.switchTab("Bán hàng");
        assertEquals("Bán hàng", panel.getCurrentActiveTab());

        panel.switchTab("Thanh toán");
        assertEquals("Thanh toán", panel.getCurrentActiveTab());

        panel.switchTab("Tích điểm");
        assertEquals("Tích điểm", panel.getCurrentActiveTab());

        panel.switchTab("Kho hàng");
        assertEquals("Kho hàng", panel.getCurrentActiveTab());

        panel.switchTab("Mặt hàng");
        assertEquals("Mặt hàng", panel.getCurrentActiveTab());

        panel.switchTab("Cây hiển thị giá");
        assertEquals("Cây hiển thị giá", panel.getCurrentActiveTab());

        panel.switchTab("Cân điện tử");
        assertEquals("Cân điện tử", panel.getCurrentActiveTab());

        panel.switchTab("Cảnh báo");
        assertEquals("Cảnh báo", panel.getCurrentActiveTab());

        panel.switchTab("Bảo mật");
        assertEquals("Bảo mật", panel.getCurrentActiveTab());

        panel.switchTab("Cảm ứng");
        assertEquals("Cảm ứng", panel.getCurrentActiveTab());

        panel.switchTab("Quản trị");
        assertEquals("Quản trị", panel.getCurrentActiveTab());

        panel.switchTab("Thông tin chung");
        assertEquals("Thông tin chung", panel.getCurrentActiveTab());
    }

    @Test
    void capturePreviewScreenshots() throws Exception {
        TouchCauHinhToanHeThongPanel panel = new TouchCauHinhToanHeThongPanel(() -> {});
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setContentPane(panel);
        frame.setSize(1024, 600);
        frame.addNotify();
        frame.validate();
        layoutRecursive(panel);

        // 1. Capture tab "Thông tin chung" (matching media_1789540444537.png)
        panel.switchTab("Thông tin chung");
        layoutRecursive(panel);
        BufferedImage imgChung = new BufferedImage(1024, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D gChung = imgChung.createGraphics();
        panel.printAll(gChung);
        gChung.dispose();
        File outChung = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_cauhinh_chung.png");
        ImageIO.write(imgChung, "png", outChung);
        assertTrue(outChung.exists(), "preview_touch_cauhinh_chung.png must be created");

        // 2. Capture tab "Bán hàng" (matching media_1789540472604.png)
        panel.switchTab("Bán hàng");
        layoutRecursive(panel);
        BufferedImage imgBanHang = new BufferedImage(1024, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D gBanHang = imgBanHang.createGraphics();
        panel.printAll(gBanHang);
        gBanHang.dispose();
        File outBanHang = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_cauhinh_banhang.png");
        ImageIO.write(imgBanHang, "png", outBanHang);
        assertTrue(outBanHang.exists(), "preview_touch_cauhinh_banhang.png must be created");

        frame.dispose();
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
