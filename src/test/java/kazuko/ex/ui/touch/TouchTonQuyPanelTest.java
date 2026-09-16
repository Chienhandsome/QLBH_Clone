package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TouchTonQuyPanelTest {

    @Test
    void panelInitializesWithDefaultDatesAndCalculatesTotals() throws Exception {
        TouchTonQuyPanel panel = new TouchTonQuyPanel(() -> {});
        assertNotNull(panel);

        JTextField from = getField(panel, "fromField");
        assertFalse(from.getText().isBlank(), "Từ ngày phải được khởi tạo");

        JLabel lblTonDau = getField(panel, "lblTonDau");
        JLabel lblTongThu = getField(panel, "lblTongThu");
        JLabel lblTongChi = getField(panel, "lblTongChi");
        JLabel lblTonQuy = getField(panel, "lblTonQuy");

        assertEquals("3,907,000", lblTonDau.getText(), "Tồn đầu cơ sở là 3,907,000");
        System.out.println("DEBUG lblTongThu: " + lblTongThu.getText());
        System.out.println("DEBUG lblTongChi: " + lblTongChi.getText());
        System.out.println("DEBUG lblTonQuy: " + lblTonQuy.getText());
        assertFalse(lblTongThu.getText().isBlank(), "Tổng thu không được rỗng");

        List<?> records = getField(panel, "records");
        assertNotNull(records);
        assertFalse(records.isEmpty(), "Danh sách bản ghi không được rỗng");
    }

    @Test
    void capturePreviewScreenshot() throws Exception {
        TouchTonQuyPanel panel = new TouchTonQuyPanel(() -> {});
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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_tonquy.png");
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
    private static <T> T getField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(target);
    }
}
