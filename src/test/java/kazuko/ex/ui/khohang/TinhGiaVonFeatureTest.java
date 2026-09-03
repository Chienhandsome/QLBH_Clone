package kazuko.ex.ui.khohang;

import kazuko.ex.dao.MatHangDAO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TinhGiaVonFeatureTest {

    private final MatHangDAO matHangDAO = new MatHangDAO();

    @Test
    @Order(1)
    public void testPhuongPhapBinhQuanCuoiKy() {
        int updatedCount = matHangDAO.tinhLaiGiaVon(1, null, null);
        assertTrue(updatedCount >= 0, "Số lượng mặt hàng cập nhật phải >= 0");
        System.out.println("Cập nhật giá vốn bình quân cuối kỳ cho " + updatedCount + " mặt hàng.");
    }

    @Test
    @Order(2)
    public void testPhuongPhapBinhQuanTheoGiaiDoan() {
        Calendar cal = Calendar.getInstance();
        Date denNgay = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date tuNgay = cal.getTime();

        int updatedCount = matHangDAO.tinhLaiGiaVon(2, tuNgay, denNgay);
        assertTrue(updatedCount >= 0, "Số lượng mặt hàng cập nhật phải >= 0");
        System.out.println("Cập nhật giá vốn theo giai đoạn cho " + updatedCount + " mặt hàng.");
    }

    @Test
    @Order(3)
    public void testPhuongPhapGiaNhapGanNhat() {
        int updatedCount = matHangDAO.tinhLaiGiaVon(3, null, null);
        assertTrue(updatedCount >= 0, "Số lượng mặt hàng cập nhật phải >= 0");
        System.out.println("Cập nhật giá vốn theo giá nhập gần nhất cho " + updatedCount + " mặt hàng.");
    }

    @Test
    @Order(4)
    public void testDialogUIComponents() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        SwingUtilities.invokeAndWait(() -> {
            TinhLaiGiaVonDialog dlg = new TinhLaiGiaVonDialog(null);
            try {
                Field fldRb1 = TinhLaiGiaVonDialog.class.getDeclaredField("rbBinhQuanCuoiKy");
                fldRb1.setAccessible(true);
                JRadioButton rb1 = (JRadioButton) fldRb1.get(dlg);
                assertTrue(rb1.isSelected(), "Mặc định 'Bình quân cuối kỳ' phải được chọn");

                Field fldRb2 = TinhLaiGiaVonDialog.class.getDeclaredField("rbBinhQuanGiaiDoan");
                fldRb2.setAccessible(true);
                JRadioButton rb2 = (JRadioButton) fldRb2.get(dlg);

                Field fldTu = TinhLaiGiaVonDialog.class.getDeclaredField("dpkTuNgay");
                fldTu.setAccessible(true);
                Component dpkTu = (Component) fldTu.get(dlg);
                assertFalse(dpkTu.isEnabled(), "Khi chưa chọn radio giai đoạn, picker ngày phải disabled");

                // Select radio 2
                rb2.setSelected(true);
                for (java.awt.event.ActionListener al : rb2.getActionListeners()) {
                    al.actionPerformed(new java.awt.event.ActionEvent(rb2, 0, "click"));
                }
                assertTrue(dpkTu.isEnabled(), "Khi chọn radio giai đoạn, picker ngày phải enabled");
            } catch (Exception e) {
                fail(e.getMessage());
            } finally {
                dlg.dispose();
            }
        });
    }
}
