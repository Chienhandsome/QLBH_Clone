package kazuko.ex.ui.touch;

import kazuko.ex.model.quy.PhieuChi;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TouchPhieuChiPanelTest {

    @Test
    void formMapsTouchInputToExpenseModel() throws Exception {
        TouchPhieuChiPanel panel = new TouchPhieuChiPanel(() -> {});
        textField(panel, "dateField").setText("16/09/2026");
        textField(panel, "voucherField").setText("PC26/00999");
        textField(panel, "purposeField").setText("Chi tiền hàng");
        textField(panel, "subjectNameField").setText("Nhà cung cấp thử nghiệm");
        textField(panel, "addressField").setText("TP. Hồ Chí Minh");
        textField(panel, "amountField").setText("3,500,000");
        textField(panel, "noteField").setText("Kiểm tra phiếu chi cảm ứng");

        Method collect = TouchPhieuChiPanel.class.getDeclaredMethod("collectFormToModel");
        collect.setAccessible(true);
        assertTrue((Boolean) collect.invoke(panel));

        Field expenseField = TouchPhieuChiPanel.class.getDeclaredField("currentExpense");
        expenseField.setAccessible(true);
        PhieuChi expense = (PhieuChi) expenseField.get(panel);
        assertNotNull(expense);
        assertEquals("PC26/00999", expense.getSoPhieu());
        assertEquals("Chi tiền hàng", expense.getLyDoChi());
        assertEquals("Nhà cung cấp thử nghiệm", expense.getTenDoiTuong());
        assertEquals("TP. Hồ Chí Minh", expense.getDiaChi());
        assertEquals(3_500_000d, expense.getSoTien());
        assertEquals("Kiểm tra phiếu chi cảm ứng", expense.getGhiChu());
    }

    @Test
    void subjectTypeControlsEditableFieldAndButtonStates() throws Exception {
        TouchPhieuChiPanel panel = new TouchPhieuChiPanel(() -> {});
        Method chooseType = TouchPhieuChiPanel.class.getDeclaredMethod("chooseSubjectType", String.class);
        chooseType.setAccessible(true);

        JTextField customerField = textField(panel, "customerField");
        JTextField employeeField = textField(panel, "employeeField");
        JTextField supplierField = textField(panel, "supplierField");
        JTextField subjectNameField = textField(panel, "subjectNameField");
        JTextField addressField = textField(panel, "addressField");

        JButton customerSearch = buttonField(panel, "customerSearch");
        JButton employeeSearch = buttonField(panel, "employeeSearch");
        JButton supplierSearch = buttonField(panel, "supplierSearch");

        // 1. Khách hàng
        chooseType.invoke(panel, "Khách hàng");
        assertTrue(customerField.isEditable());
        assertTrue(customerSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(employeeField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(employeeSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(supplierField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(supplierSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(subjectNameField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(addressField.isEditable());

        // 2. Nhân viên
        chooseType.invoke(panel, "Nhân viên");
        assertTrue(employeeField.isEditable());
        assertTrue(employeeSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(customerField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(customerSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(supplierField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(supplierSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(subjectNameField.isEditable());

        // 3. Nhà cung cấp
        chooseType.invoke(panel, "Nhà cung cấp");
        assertTrue(supplierField.isEditable());
        assertTrue(supplierSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(customerField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(customerSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(employeeField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(employeeSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(subjectNameField.isEditable());

        // 4. Đối tượng khác
        chooseType.invoke(panel, "Đối tượng khác");
        assertTrue(subjectNameField.isEditable());
        assertTrue(addressField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(customerField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(customerSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(employeeField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(employeeSearch.isEnabled());
        org.junit.jupiter.api.Assertions.assertFalse(supplierField.isEditable());
        org.junit.jupiter.api.Assertions.assertFalse(supplierSearch.isEnabled());
    }

    @Test
    void supplierFieldInputSyncsToSubjectNameAndExpenseModel() throws Exception {
        TouchPhieuChiPanel panel = new TouchPhieuChiPanel(() -> {});
        Method chooseType = TouchPhieuChiPanel.class.getDeclaredMethod("chooseSubjectType", String.class);
        chooseType.setAccessible(true);
        chooseType.invoke(panel, "Nhà cung cấp");

        JTextField supplierField = textField(panel, "supplierField");
        supplierField.setText("Công Ty TNHH Phân Phối Toàn Cầu");

        JTextField subjectNameField = textField(panel, "subjectNameField");
        assertEquals("Công Ty TNHH Phân Phối Toàn Cầu", subjectNameField.getText());

        Method collect = TouchPhieuChiPanel.class.getDeclaredMethod("collectFormToModel");
        collect.setAccessible(true);
        assertTrue((Boolean) collect.invoke(panel));

        Field expenseField = TouchPhieuChiPanel.class.getDeclaredField("currentExpense");
        expenseField.setAccessible(true);
        PhieuChi expense = (PhieuChi) expenseField.get(panel);
        assertEquals("Nhà cung cấp", expense.getLoaiDoiTuong());
        assertEquals("Công Ty TNHH Phân Phối Toàn Cầu", expense.getTenNhaCungCap());
        assertEquals("Công Ty TNHH Phân Phối Toàn Cầu", expense.getTenDoiTuong());
    }

    @Test
    void capturePreview() throws Exception {
        System.setProperty("java.awt.headless", "false");
        TouchPhieuChiPanel panel = new TouchPhieuChiPanel(() -> {});
        panel.setSize(1200, 750);
        panel.doLayout();
        layoutRecursive(panel);

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1200, 750, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        panel.printAll(g2);
        g2.dispose();

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_phieuchi.png");
        javax.imageio.ImageIO.write(img, "png", out);
    }

    private void layoutRecursive(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                layoutRecursive((Container) child);
            }
        }
    }

    private JTextField textField(TouchPhieuChiPanel panel, String name) throws Exception {
        Field field = TouchPhieuChiPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JTextField) field.get(panel);
    }

    private JButton buttonField(TouchPhieuChiPanel panel, String name) throws Exception {
        Field field = TouchPhieuChiPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JButton) field.get(panel);
    }
}
