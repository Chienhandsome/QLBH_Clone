package kazuko.ex.ui.touch;

import kazuko.ex.service.LicenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TouchDangKyBanQuyenAndAboutTest {

    @Test
    void panelDangKyBanQuyenLoadsInitialDataAndActions(@TempDir Path tempDir) {
        Path tempLicense = tempDir.resolve("test_license.properties");
        LicenseService service = new LicenseService(tempLicense);

        TouchDangKyBanQuyenPanel panel = new TouchDangKyBanQuyenPanel(() -> {}, service);
        assertNotNull(panel);
        assertNotNull(panel.getTxtMachineId().getText());
        assertFalse(panel.getTxtMachineId().getText().trim().isEmpty());

        // Test load from config
        panel.onLoadFromConfig();
        assertNotNull(panel.getTxtHoVaTen().getText());
        assertNotNull(panel.getTxtDonVi().getText());

        // Test lay ma server
        panel.onLayMaDangKyTuServer();
        assertNotNull(panel.getTxtRegistrationKey().getText());
        assertFalse(panel.getTxtRegistrationKey().getText().trim().isEmpty());

        // Test registration
        panel.onCapNhat();
        assertTrue(service.getStatus().registered());
    }

    @Test
    void panelThongTinPhanMemDisplaysStatusAndNavigates(@TempDir Path tempDir) {
        Path tempLicense = tempDir.resolve("test_license.properties");
        LicenseService service = new LicenseService(tempLicense);

        AtomicBoolean navigated = new AtomicBoolean(false);
        TouchThongTinPhanMemPanel panel = new TouchThongTinPhanMemPanel(() -> {}, () -> navigated.set(true), service);
        assertNotNull(panel);

        // Before registration: trial status
        assertEquals("Dùng thử", panel.getLblEditionVal().getText());

        // Register valid key
        String mId = service.getMachineId();
        String key = LicenseService.expectedKeyForMachine(mId);
        service.register("Công ty TNHH Ngọc Châu Âu", "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM", key);

        panel.refreshLicenseData();
        assertEquals("Bản quyền chính thức", panel.getLblEditionVal().getText());
        assertEquals("Công ty TNHH Ngọc Châu Âu", panel.getLblOwnerVal().getText());
    }

    @Test
    void capturePreviewScreenshots() throws Exception {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setSize(1024, 580);

        // 1. Capture ĐĂNG KÝ BẢN QUYỀN (matching media_1789542171194.png)
        TouchDangKyBanQuyenPanel dangKyPanel = new TouchDangKyBanQuyenPanel(() -> {});
        // Pre-fill like in user reference image:
        dangKyPanel.getTxtHoVaTen().setText("Công ty TNHH Ngọc Châu Âu");
        dangKyPanel.getTxtDonVi().setText("455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        dangKyPanel.getTxtMachineId().setText("59078-41242-8051-29110-12594");
        dangKyPanel.getTxtRegistrationKey().setText("");

        frame.setContentPane(dangKyPanel);
        frame.addNotify();
        frame.validate();
        layoutRecursive(dangKyPanel);

        BufferedImage imgDangKy = new BufferedImage(1024, 580, BufferedImage.TYPE_INT_RGB);
        Graphics2D gDK = imgDangKy.createGraphics();
        dangKyPanel.printAll(gDK);
        gDK.dispose();
        File outDK = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_dangkybanquyen.png");
        ImageIO.write(imgDangKy, "png", outDK);
        assertTrue(outDK.exists(), "preview_touch_dangkybanquyen.png must be created");

        // 2. Capture THÔNG TIN PHẦN MỀM (matching media_1789542181771.png)
        TouchThongTinPhanMemPanel aboutPanel = new TouchThongTinPhanMemPanel(() -> {}, () -> {});
        // Ensure values match screenshot for preview:
        aboutPanel.getLblOwnerVal().setText("Chưa đăng ký");
        aboutPanel.getLblOrgVal().setText("Chưa đăng ký");
        aboutPanel.getLblKeyVal().setText("Bạn còn 18 ngày dùng thử.");
        aboutPanel.getLblEditionVal().setText("Dùng thử");

        frame.setContentPane(aboutPanel);
        frame.validate();
        layoutRecursive(aboutPanel);

        BufferedImage imgAbout = new BufferedImage(1024, 580, BufferedImage.TYPE_INT_RGB);
        Graphics2D gAbout = imgAbout.createGraphics();
        aboutPanel.printAll(gAbout);
        gAbout.dispose();
        File outAbout = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_thongtinphanmem.png");
        ImageIO.write(imgAbout, "png", outAbout);
        assertTrue(outAbout.exists(), "preview_touch_thongtinphanmem.png must be created");

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
