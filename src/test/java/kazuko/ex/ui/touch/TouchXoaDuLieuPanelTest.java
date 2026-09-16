package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TouchXoaDuLieuPanelTest {

    @Test
    void panelInitializesWithAllNineteenCheckboxes() {
        TouchXoaDuLieuPanel panel = new TouchXoaDuLieuPanel(() -> {});
        assertNotNull(panel);

        List<JCheckBox> checkboxes = panel.getAllDataCheckboxes();
        assertEquals(19, checkboxes.size(), "Phải có đầy đủ 19 checkboxes dữ liệu");

        // Verify checkbox labels
        assertTrue(checkboxes.stream().anyMatch(cb -> "SAO LƯU TRƯỚC KHI XÓA".equals(cb.getText())));
        assertTrue(checkboxes.stream().anyMatch(cb -> "DANH SÁCH ĐƠN HÀNG".equals(cb.getText())));
        assertTrue(checkboxes.stream().anyMatch(cb -> "DANH SÁCH PHIẾU THU".equals(cb.getText())));
        assertTrue(checkboxes.stream().anyMatch(cb -> "DANH SÁCH PHIẾU CHI".equals(cb.getText())));
        assertTrue(checkboxes.stream().anyMatch(cb -> "DỮ LIỆU BẢNG LƯƠNG".equals(cb.getText())));
        assertTrue(checkboxes.stream().anyMatch(cb -> "DỮ LIỆU ĐẶT HÀNG NHÀ CUNG CẤP".equals(cb.getText())));

        // Default: SAO LƯU TRƯỚC KHI XÓA is selected
        JCheckBox cbBackup = checkboxes.stream()
                .filter(cb -> "SAO LƯU TRƯỚC KHI XÓA".equals(cb.getText()))
                .findFirst().orElse(null);
        assertNotNull(cbBackup);
        assertTrue(cbBackup.isSelected());
    }

    @Test
    void masterCheckboxTogglesAllDataCheckboxes() {
        TouchXoaDuLieuPanel panel = new TouchXoaDuLieuPanel(() -> {});
        JCheckBox cbAll = panel.getCbAll();
        assertNotNull(cbAll);

        // Click to select all
        cbAll.setSelected(true);
        for (java.awt.event.ActionListener al : cbAll.getActionListeners()) {
            al.actionPerformed(null);
        }
        for (JCheckBox cb : panel.getAllDataCheckboxes()) {
            assertTrue(cb.isSelected(), "Tất cả checkbox phải được tích khi chọn TẤT CẢ: " + cb.getText());
        }

        // Click to deselect all
        cbAll.setSelected(false);
        for (java.awt.event.ActionListener al : cbAll.getActionListeners()) {
            al.actionPerformed(null);
        }
        for (JCheckBox cb : panel.getAllDataCheckboxes()) {
            assertFalse(cb.isSelected(), "Tất cả checkbox phải bị bỏ tích: " + cb.getText());
        }
    }

    @Test
    void passwordVerificationAndDefaults() {
        TouchXoaDuLieuPanel panel = new TouchXoaDuLieuPanel(() -> {});
        assertTrue(panel.testVerifyPassword("123"), "Mã mặc định 123 phải được chấp nhận");
        assertTrue(panel.testVerifyPassword("admin"), "Tài khoản admin phải được chấp nhận");
        assertFalse(panel.testVerifyPassword(""), "Mật khẩu rỗng không được chấp nhận");
        assertFalse(panel.testVerifyPassword("wrong_password_xyz"), "Mật khẩu sai phải bị từ chối");

        assertNotNull(panel.getTxtDate().getText());
        assertFalse(panel.getTxtDate().getText().isBlank());
    }

    @Test
    void capturePreviewScreenshot() throws Exception {
        TouchXoaDuLieuPanel panel = new TouchXoaDuLieuPanel(() -> {});
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setContentPane(panel);
        frame.setSize(1200, 680);
        frame.addNotify();
        frame.validate();
        layoutRecursive(panel);

        BufferedImage image = new BufferedImage(1200, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        panel.printAll(g2);
        g2.dispose();
        frame.dispose();

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_xoadulieu.png");
        ImageIO.write(image, "png", out);
        assertTrue(out.exists());
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
