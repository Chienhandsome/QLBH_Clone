package kazuko.ex.ui.touch;

import kazuko.ex.model.quy.PhieuThu;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TouchPhieuThuPanelTest {

    @Test
    void formMapsTouchInputToReceiptModel() throws Exception {
        TouchPhieuThuPanel panel = new TouchPhieuThuPanel(() -> {});
        textField(panel, "dateField").setText("16/09/2026");
        textField(panel, "voucherField").setText("PT26/00999");
        textField(panel, "purposeField").setText("Thu công nợ");
        textField(panel, "subjectNameField").setText("Khách thử nghiệm");
        textField(panel, "addressField").setText("Đồng Nai");
        textField(panel, "amountField").setText("1,250,000");
        textField(panel, "noteField").setText("Kiểm tra cảm ứng");

        Method collect = TouchPhieuThuPanel.class.getDeclaredMethod("collectFormToModel");
        collect.setAccessible(true);
        assertTrue((Boolean) collect.invoke(panel));

        Field receiptField = TouchPhieuThuPanel.class.getDeclaredField("currentReceipt");
        receiptField.setAccessible(true);
        PhieuThu receipt = (PhieuThu) receiptField.get(panel);
        assertNotNull(receipt);
        assertEquals("PT26/00999", receipt.getSoPhieu());
        assertEquals("Thu công nợ", receipt.getLyDoThu());
        assertEquals("Khách thử nghiệm", receipt.getTenDoiTuong());
        assertEquals("Đồng Nai", receipt.getDiaChi());
        assertEquals(1_250_000d, receipt.getSoTien());
        assertEquals("Kiểm tra cảm ứng", receipt.getGhiChu());
    }

    @Test
    void subjectTypeControlsEditableFieldAndButtonStates() throws Exception {
        TouchPhieuThuPanel panel = new TouchPhieuThuPanel(() -> {});
        Method chooseType = TouchPhieuThuPanel.class.getDeclaredMethod("chooseSubjectType", String.class);
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
    void customerFieldInputSyncsToSubjectNameAndReceiptModel() throws Exception {
        TouchPhieuThuPanel panel = new TouchPhieuThuPanel(() -> {});
        Method chooseType = TouchPhieuThuPanel.class.getDeclaredMethod("chooseSubjectType", String.class);
        chooseType.setAccessible(true);
        chooseType.invoke(panel, "Khách hàng");

        JTextField customerField = textField(panel, "customerField");
        customerField.setText("Khách Hàng Công Ty VIP");

        JTextField subjectNameField = textField(panel, "subjectNameField");
        assertEquals("Khách Hàng Công Ty VIP", subjectNameField.getText());

        Method collect = TouchPhieuThuPanel.class.getDeclaredMethod("collectFormToModel");
        collect.setAccessible(true);
        assertTrue((Boolean) collect.invoke(panel));

        Field receiptField = TouchPhieuThuPanel.class.getDeclaredField("currentReceipt");
        receiptField.setAccessible(true);
        PhieuThu receipt = (PhieuThu) receiptField.get(panel);
        assertEquals("Khách hàng", receipt.getLoaiDoiTuong());
        assertEquals("Khách Hàng Công Ty VIP", receipt.getTenKhachHang());
        assertEquals("Khách Hàng Công Ty VIP", receipt.getTenDoiTuong());
    }

    private JTextField textField(TouchPhieuThuPanel panel, String name) throws Exception {
        Field field = TouchPhieuThuPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JTextField) field.get(panel);
    }

    private JButton buttonField(TouchPhieuThuPanel panel, String name) throws Exception {
        Field field = TouchPhieuThuPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JButton) field.get(panel);
    }
}
