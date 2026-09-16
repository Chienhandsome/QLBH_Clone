package kazuko.ex.ui.touch;

import kazuko.ex.model.system.SGroupUser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TouchNguoiDungVaPhanQuyenPanelTest {

    @Test
    void panelInitializesAndLoadsGroupsAndUsers() {
        TouchNguoiDungVaPhanQuyenPanel panel = new TouchNguoiDungVaPhanQuyenPanel(() -> {});
        assertNotNull(panel);

        List<SGroupUser> groups = panel.getGroupList();
        assertNotNull(groups);
        assertFalse(groups.isEmpty(), "Phải có ít nhất 1 nhóm người dùng");

        // Verify default group selection
        assertNotNull(panel.getSelectedGroup());
        assertFalse(panel.isReportTab());
    }

    @Test
    void testTabSwitching() {
        TouchNguoiDungVaPhanQuyenPanel panel = new TouchNguoiDungVaPhanQuyenPanel(() -> {});

        // Default tab is Quyền sử dụng
        assertFalse(panel.isReportTab());

        // Switch to Quyền báo cáo
        panel.setReportTab(true);
        assertTrue(panel.isReportTab());

        // Switch back to Quyền sử dụng
        panel.setReportTab(false);
        assertFalse(panel.isReportTab());
    }

    @Test
    void capturePreviewScreenshots() throws Exception {
        TouchNguoiDungVaPhanQuyenPanel panel = new TouchNguoiDungVaPhanQuyenPanel(() -> {});
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setContentPane(panel);
        frame.setSize(1200, 680);
        frame.addNotify();
        frame.validate();
        layoutRecursive(panel);

        // 1. Screenshot tab QUYỀN SỬ DỤNG (khớp ảnh media_1789534697121.png)
        panel.setReportTab(false);
        layoutRecursive(panel);
        BufferedImage imgFunc = new BufferedImage(1200, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D gFunc = imgFunc.createGraphics();
        panel.printAll(gFunc);
        gFunc.dispose();
        File outFunc = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_phanquyen_sudung.png");
        ImageIO.write(imgFunc, "png", outFunc);
        assertTrue(outFunc.exists());

        // 2. Screenshot tab QUYỀN BÁO CÁO (khớp ảnh media_1789534757584.png)
        // Select Kho group if exists
        SGroupUser khoGroup = panel.getGroupList().stream()
                .filter(g -> g.getName() != null && g.getName().toLowerCase().contains("kho"))
                .findFirst().orElse(null);
        if (khoGroup != null) {
            panel.setSelectedGroup(khoGroup);
        }
        panel.setReportTab(true);
        layoutRecursive(panel);
        BufferedImage imgReport = new BufferedImage(1200, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D gReport = imgReport.createGraphics();
        panel.printAll(gReport);
        gReport.dispose();
        File outReport = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_phanquyen_baocao.png");
        ImageIO.write(imgReport, "png", outReport);
        assertTrue(outReport.exists());

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
