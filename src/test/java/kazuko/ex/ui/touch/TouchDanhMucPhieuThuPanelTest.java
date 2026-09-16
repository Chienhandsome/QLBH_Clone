package kazuko.ex.ui.touch;

import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.model.quy.PhieuThu;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TouchDanhMucPhieuThuPanelTest {

    private final PhieuThuDAO dao = new PhieuThuDAO();

    @BeforeEach
    void setUp() {
        PhieuThu pt = new PhieuThu();
        pt.setId("test-pt-unit");
        pt.setSoPhieu("PT26/00088");
        pt.setNgay(new Date());
        pt.setLoaiDoiTuong("Khách hàng");
        pt.setTenDoiTuong("Khách hàng Test");
        pt.setLyDoThu("Thu tiền mặt");
        pt.setSoTien(350000.0);
        pt.setStatus(1);
        dao.insert(pt);
    }

    @AfterEach
    void tearDown() {
        dao.delete("test-pt-unit");
    }

    @Test
    void panelInitializesWithDefaultDatesAndLoadsReceipts() throws Exception {
        TouchDanhMucPhieuThuPanel panel = new TouchDanhMucPhieuThuPanel(() -> {});
        assertNotNull(panel);

        JTextField from = getField(panel, "fromField");
        JTextField to = getField(panel, "toField");
        assertFalse(from.getText().isBlank(), "Từ ngày không được rỗng");
        assertFalse(to.getText().isBlank(), "Đến ngày không được rỗng");

        List<?> receipts = getField(panel, "receipts");
        assertNotNull(receipts);
        assertFalse(receipts.isEmpty(), "Danh sách phiếu thu nạp thành công");
    }

    @Test
    void editAndDeleteButtonsReflectSelectionState() throws Exception {
        TouchDanhMucPhieuThuPanel panel = new TouchDanhMucPhieuThuPanel(() -> {});
        JButton edit = getField(panel, "editButton");
        JButton delete = getField(panel, "deleteButton");

        // Khi chưa chọn phiếu nào
        assertEquals(new Color(145, 145, 145), edit.getBackground(), "Nút Chỉnh sửa màu xám khi chưa chọn");
        assertEquals(new Color(145, 145, 145), delete.getBackground(), "Nút Xóa màu xám khi chưa chọn");

        // Chọn một phiếu
        List<PhieuThu> receipts = getField(panel, "receipts");
        assertFalse(receipts.isEmpty());
        PhieuThu first = receipts.get(0);

        setField(panel, "selectedReceipt", first);
        Method updateActions = TouchDanhMucPhieuThuPanel.class.getDeclaredMethod("updateActions");
        updateActions.setAccessible(true);
        updateActions.invoke(panel);

        assertEquals(new Color(0, 90, 20), edit.getBackground(), "Nút Chỉnh sửa chuyển xanh khi đã chọn");
        assertEquals(new Color(165, 10, 10), delete.getBackground(), "Nút Xóa chuyển đỏ khi đã chọn");
    }

    @Test
    void openCreateReceiptSwitchesToForm() throws Exception {
        TouchDanhMucPhieuThuPanel panel = new TouchDanhMucPhieuThuPanel(() -> {});
        Method openCreate = TouchDanhMucPhieuThuPanel.class.getDeclaredMethod("openCreateReceipt");
        openCreate.setAccessible(true);
        openCreate.invoke(panel);

        JPanel formContainer = getField(panel, "formContainer");
        assertEquals(1, formContainer.getComponentCount(), "Form container có chứa TouchPhieuThuPanel");
        assertTrue(formContainer.getComponent(0) instanceof TouchPhieuThuPanel);
    }

    @Test
    void openEditReceiptLoadsSelectedReceipt() throws Exception {
        TouchDanhMucPhieuThuPanel panel = new TouchDanhMucPhieuThuPanel(() -> {});
        PhieuThu pt = dao.findById("test-pt-unit");
        assertNotNull(pt);

        Method openEdit = TouchDanhMucPhieuThuPanel.class.getDeclaredMethod("openEditReceipt", PhieuThu.class);
        openEdit.setAccessible(true);
        openEdit.invoke(panel, pt);

        JPanel formContainer = getField(panel, "formContainer");
        assertEquals(1, formContainer.getComponentCount());
        TouchPhieuThuPanel form = (TouchPhieuThuPanel) formContainer.getComponent(0);

        Field receiptField = TouchPhieuThuPanel.class.getDeclaredField("currentReceipt");
        receiptField.setAccessible(true);
        PhieuThu loaded = (PhieuThu) receiptField.get(form);
        assertNotNull(loaded);
        assertEquals("PT26/00088", loaded.getSoPhieu());
        assertEquals("Khách hàng Test", loaded.getTenDoiTuong());
        assertEquals(350000.0, loaded.getSoTien());
    }

    @Test
    void capturePreviewScreenshot() throws Exception {
        TouchDanhMucPhieuThuPanel panel = new TouchDanhMucPhieuThuPanel(() -> {});
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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_danhmuc_phieuthu.png");
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
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
