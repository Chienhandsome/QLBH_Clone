package kazuko.ex.ui.touch;

import kazuko.ex.model.congno.CongNoKhachHangItem;
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

class TouchCongNoKhachHangPanelTest {

    @Test
    void panelInitializesAndLoadsCustomerDebtData() throws Exception {
        TouchCongNoKhachHangPanel panel = new TouchCongNoKhachHangPanel(() -> {});
        assertNotNull(panel);

        JLabel lblTitle = getField(panel, "lblTitle");
        assertNotNull(lblTitle);
        assertTrue(lblTitle.getText().contains("CÔNG NỢ KHÁCH HÀNG"), "Tiêu đề phải chứa tên màn hình");

        List<CongNoKhachHangItem> allCustomers = getField(panel, "allCustomers");
        assertNotNull(allCustomers);
        System.out.println("Total customers loaded: " + allCustomers.size());

        List<CongNoKhachHangItem> filteredCustomers = getField(panel, "filteredCustomers");
        assertNotNull(filteredCustomers);
        System.out.println("Filtered customers count: " + filteredCustomers.size());

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
        TouchCongNoKhachHangPanel panel = new TouchCongNoKhachHangPanel(() -> {});
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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_congnokhachhang.png");
        ImageIO.write(image, "png", out);
        assertTrue(out.exists());
    }

    @Test
    void testOpenPaymentOpensTouchPhieuThuPanelWithPreselectedCustomer() throws Exception {
        TouchCongNoKhachHangPanel panel = new TouchCongNoKhachHangPanel(() -> {});
        List<CongNoKhachHangItem> customers = getField(panel, "filteredCustomers");
        assertFalse(customers.isEmpty());

        CongNoKhachHangItem target = customers.stream()
                .filter(c -> c.getConNo() > 0)
                .findFirst().orElse(customers.get(0));
        setField(panel, "selectedCustomer", target);

        Method m = panel.getClass().getDeclaredMethod("openPayment");
        m.setAccessible(true);
        m.invoke(panel);

        JPanel formContainer = getField(panel, "formContainer");
        assertEquals(1, formContainer.getComponentCount());
        Component comp = formContainer.getComponent(0);
        assertTrue(comp instanceof TouchPhieuThuPanel, "Thanh toán phải mở TouchPhieuThuPanel");

        TouchPhieuThuPanel ptPanel = (TouchPhieuThuPanel) comp;
        JTextField customerField = getField(ptPanel, "customerField");
        assertEquals(target.getTenKhach(), customerField.getText(), "Tên khách hàng phải được chọn sẵn");

        JButton subjectTypeButton = getField(ptPanel, "subjectTypeButton");
        assertEquals("KHÁCH HÀNG", subjectTypeButton.getText(), "Loại đối tượng phải là KHÁCH HÀNG");

        if (target.getConNo() > 0) {
            JTextField amountField = getField(ptPanel, "amountField");
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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_congnokhach_thanhtoan.png");
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
