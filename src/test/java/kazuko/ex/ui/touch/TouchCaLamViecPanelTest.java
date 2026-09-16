package kazuko.ex.ui.touch;

import kazuko.ex.dao.CaLamViecDAO;
import kazuko.ex.model.danhmuc.CaLamViec;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TouchCaLamViecPanelTest {

    @Test
    void panelInitializesAndLoadsShiftDataWithoutItemAddButton() throws Exception {
        TouchCaLamViecPanel panel = new TouchCaLamViecPanel(() -> {});
        assertNotNull(panel);

        JPanel listScreen = getField(panel, "listScreen");
        assertNotNull(listScreen);

        // Find scroll pane and rows panel
        JScrollPane scroll = findComponent(listScreen, JScrollPane.class);
        assertNotNull(scroll);
        JPanel rows = (JPanel) scroll.getViewport().getView();
        assertNotNull(rows);

        int rowCount = 0;
        for (Component c : rows.getComponents()) {
            if (c instanceof JPanel p && p.getComponentCount() == 2) {
                rowCount++;
                Component c0 = p.getComponent(0);
                Component c1 = p.getComponent(1);

                JLabel nameLabel = (c0 instanceof JLabel) ? (JLabel) c0 : (JLabel) c1;
                JPanel actionsPanel = (c0 instanceof JPanel) ? (JPanel) c0 : (JPanel) c1;

                assertNotNull(nameLabel.getText());
                assertFalse(nameLabel.getText().isBlank());

                // Assert actionsPanel has exactly 2 buttons: XÓA and SỬA (NO THÊM)
                assertEquals(2, actionsPanel.getComponentCount(), "Mỗi dòng chỉ được có đúng 2 nút [XÓA] và [SỬA]");
                JButton b1 = (JButton) actionsPanel.getComponent(0);
                JButton b2 = (JButton) actionsPanel.getComponent(1);

                assertTrue("XÓA".equals(b1.getText()) || "SỬA".equals(b1.getText()));
                assertTrue("XÓA".equals(b2.getText()) || "SỬA".equals(b2.getText()));
                assertNotEquals("THÊM", b1.getText(), "Không được có nút THÊM trên từng item");
                assertNotEquals("THÊM", b2.getText(), "Không được có nút THÊM trên từng item");
            }
        }
        assertTrue(rowCount > 0, "Phải có ít nhất 1 ca làm việc hiển thị");
    }

    @Test
    void testBeginAddAndBeginEditNavigation() throws Exception {
        TouchCaLamViecPanel panel = new TouchCaLamViecPanel(() -> {});
        Method beginAdd = panel.getClass().getDeclaredMethod("beginAdd");
        beginAdd.setAccessible(true);
        beginAdd.invoke(panel);

        JLabel editHeader = getField(panel, "editHeader");
        assertEquals("THÊM MỚI CA LÀM VIỆC", editHeader.getText());

        JTextField nameField = getField(panel, "nameField");
        nameField.setText("Ca kiểm thử");

        JTextField tiLeLuongField = getField(panel, "tiLeLuongField");
        assertEquals("100", tiLeLuongField.getText());

        Method beginEdit = panel.getClass().getDeclaredMethod("beginEdit", CaLamViec.class);
        beginEdit.setAccessible(true);
        CaLamViec ca = new CaLamViec("test-id", "Ca hành chính", 120.0);
        beginEdit.invoke(panel, ca);

        assertEquals("CHỈNH SỬA CA LÀM VIỆC", editHeader.getText());
        assertEquals("Ca hành chính", nameField.getText());
        assertEquals("120", tiLeLuongField.getText());
    }

    @Test
    void capturePreviewScreenshot() throws Exception {
        TouchCaLamViecPanel panel = new TouchCaLamViecPanel(() -> {});
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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_calamviec.png");
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

    @SuppressWarnings("unchecked")
    private static <T> T findComponent(Container parent, Class<T> clazz) {
        for (Component c : parent.getComponents()) {
            if (clazz.isInstance(c)) return (T) c;
            if (c instanceof Container child) {
                T found = findComponent(child, clazz);
                if (found != null) return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(target);
    }
}
