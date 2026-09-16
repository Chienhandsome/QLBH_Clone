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

    private JTextField textField(TouchPhieuThuPanel panel, String name) throws Exception {
        Field field = TouchPhieuThuPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JTextField) field.get(panel);
    }
}
