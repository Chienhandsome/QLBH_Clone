package kazuko.ex.ui.touch;

import kazuko.ex.model.congno.CongNoNhaCungCapItem;
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

class TouchCongNoNhaCungCapPanelTest {

    @Test
    void panelInitializesAndLoadsSupplierDebtData() throws Exception {
        TouchCongNoNhaCungCapPanel panel = new TouchCongNoNhaCungCapPanel(() -> {});
        assertNotNull(panel);

        JLabel lblTitle = getField(panel, "lblTitle");
        assertNotNull(lblTitle);
        assertTrue(lblTitle.getText().contains("CÔNG NỢ NHÀ CUNG CẤP"), "Tiêu đề phải chứa tên màn hình");

        List<CongNoNhaCungCapItem> allSuppliers = getField(panel, "allSuppliers");
        assertNotNull(allSuppliers);
        System.out.println("Total suppliers loaded: " + allSuppliers.size());

        List<CongNoNhaCungCapItem> filteredSuppliers = getField(panel, "filteredSuppliers");
        assertNotNull(filteredSuppliers);
        System.out.println("Filtered suppliers count: " + filteredSuppliers.size());

        JButton btnToggleAll = getField(panel, "btnToggleAll");
        assertNotNull(btnToggleAll);
        assertEquals("[X] XEM TẤT CẢ", btnToggleAll.getText());

        // Test toggle Xem tất cả vs Còn nợ
        btnToggleAll.doClick();
        assertEquals("[  ] XEM TẤT CẢ", btnToggleAll.getText());
        boolean showAll = getField(panel, "showAll");
        assertFalse(showAll);

        // Click again to toggle back
        btnToggleAll.doClick();
        assertEquals("[X] XEM TẤT CẢ", btnToggleAll.getText());
        showAll = getField(panel, "showAll");
        assertTrue(showAll);
    }

    @Test
    void capturePreviewScreenshot() throws Exception {
        TouchCongNoNhaCungCapPanel panel = new TouchCongNoNhaCungCapPanel(() -> {});
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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_congnonhacungcap.png");
        ImageIO.write(image, "png", out);
        assertTrue(out.exists());
    }

    @Test
    void testOpenPaymentOpensTouchPhieuChiPanelWithPreselectedSupplier() throws Exception {
        TouchCongNoNhaCungCapPanel panel = new TouchCongNoNhaCungCapPanel(() -> {});
        List<CongNoNhaCungCapItem> suppliers = getField(panel, "filteredSuppliers");
        assertFalse(suppliers.isEmpty(), "Danh sách nhà cung cấp không được rỗng");

        CongNoNhaCungCapItem target = suppliers.stream()
                .filter(s -> s.getConNo() > 0)
                .findFirst().orElse(suppliers.get(0));
        setField(panel, "selectedSupplier", target);

        Method m = panel.getClass().getDeclaredMethod("openPayment");
        m.setAccessible(true);
        m.invoke(panel);

        JPanel formContainer = getField(panel, "formContainer");
        assertEquals(1, formContainer.getComponentCount());
        Component comp = formContainer.getComponent(0);
        assertTrue(comp instanceof TouchPhieuChiPanel, "Thanh toán phải mở TouchPhieuChiPanel");

        TouchPhieuChiPanel pcPanel = (TouchPhieuChiPanel) comp;
        JTextField supplierField = getField(pcPanel, "supplierField");
        assertEquals(target.getTenNCC(), supplierField.getText(), "Tên nhà cung cấp phải được chọn sẵn");

        JButton subjectTypeButton = getField(pcPanel, "subjectTypeButton");
        assertEquals("NHÀ CUNG CẤP", subjectTypeButton.getText(), "Loại đối tượng phải là NHÀ CUNG CẤP");

        if (target.getConNo() > 0) {
            JTextField amountField = getField(pcPanel, "amountField");
            assertFalse(amountField.getText().isBlank());
            assertNotEquals("0", amountField.getText());
        }

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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_congnoncc_thanhtoan.png");
        ImageIO.write(image, "png", out);
        assertTrue(out.exists());

        Method retMethod = panel.getClass().getDeclaredMethod("returnToDebtScreen");
        retMethod.setAccessible(true);
        retMethod.invoke(panel);
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

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
