package kazuko.ex.ui.touch;

import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.model.quy.PhieuChi;
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

class TouchDanhMucPhieuChiPanelTest {

    private final PhieuChiDAO dao = new PhieuChiDAO();

    @BeforeEach
    void setUp() {
        PhieuChi pc = new PhieuChi();
        pc.setId("test-pc-unit");
        pc.setSoPhieu("PC26/00088");
        pc.setNgay(new Date());
        pc.setLoaiDoiTuong("Nhà cung cấp");
        pc.setTenDoiTuong("Nhà cung cấp Test");
        pc.setLyDoChi("Chi tiền mua hàng");
        pc.setSoTien(450000.0);
        pc.setStatus(1);
        dao.insert(pc);
    }

    @AfterEach
    void tearDown() {
        dao.delete("test-pc-unit");
    }

    @Test
    void panelInitializesWithDefaultDatesAndLoadsExpenses() throws Exception {
        TouchDanhMucPhieuChiPanel panel = new TouchDanhMucPhieuChiPanel(() -> {});
        assertNotNull(panel);

        JTextField from = getField(panel, "fromField");
        JTextField to = getField(panel, "toField");
        assertFalse(from.getText().isBlank(), "Từ ngày không được rỗng");
        assertFalse(to.getText().isBlank(), "Đến ngày không được rỗng");

        List<?> expenses = getField(panel, "expenses");
        assertNotNull(expenses);
        assertFalse(expenses.isEmpty(), "Danh sách phiếu chi nạp thành công");
    }

    @Test
    void editAndDeleteButtonsReflectSelectionState() throws Exception {
        TouchDanhMucPhieuChiPanel panel = new TouchDanhMucPhieuChiPanel(() -> {});
        JButton edit = getField(panel, "editButton");
        JButton delete = getField(panel, "deleteButton");

        // Khi chưa chọn phiếu nào
        assertEquals(new Color(145, 145, 145), edit.getBackground(), "Nút Chỉnh sửa màu xám khi chưa chọn");
        assertEquals(new Color(145, 145, 145), delete.getBackground(), "Nút Xóa màu xám khi chưa chọn");

        // Chọn một phiếu
        List<PhieuChi> expenses = getField(panel, "expenses");
        assertFalse(expenses.isEmpty());
        PhieuChi first = expenses.get(0);

        setField(panel, "selectedExpense", first);
        Method updateActions = TouchDanhMucPhieuChiPanel.class.getDeclaredMethod("updateActions");
        updateActions.setAccessible(true);
        updateActions.invoke(panel);

        assertEquals(new Color(0, 90, 20), edit.getBackground(), "Nút Chỉnh sửa chuyển xanh khi đã chọn");
        assertEquals(new Color(165, 10, 10), delete.getBackground(), "Nút Xóa chuyển đỏ khi đã chọn");
    }

    @Test
    void openCreateExpenseSwitchesToForm() throws Exception {
        TouchDanhMucPhieuChiPanel panel = new TouchDanhMucPhieuChiPanel(() -> {});
        Method openCreate = TouchDanhMucPhieuChiPanel.class.getDeclaredMethod("openCreateExpense");
        openCreate.setAccessible(true);
        openCreate.invoke(panel);

        JPanel formContainer = getField(panel, "formContainer");
        assertEquals(1, formContainer.getComponentCount(), "Form container có chứa TouchPhieuChiPanel");
        assertTrue(formContainer.getComponent(0) instanceof TouchPhieuChiPanel);
    }

    @Test
    void openEditExpenseLoadsSelectedExpense() throws Exception {
        TouchDanhMucPhieuChiPanel panel = new TouchDanhMucPhieuChiPanel(() -> {});
        PhieuChi pc = dao.findById("test-pc-unit");
        assertNotNull(pc);

        Method openEdit = TouchDanhMucPhieuChiPanel.class.getDeclaredMethod("openEditExpense", PhieuChi.class);
        openEdit.setAccessible(true);
        openEdit.invoke(panel, pc);

        JPanel formContainer = getField(panel, "formContainer");
        assertEquals(1, formContainer.getComponentCount());
        TouchPhieuChiPanel form = (TouchPhieuChiPanel) formContainer.getComponent(0);

        Field expenseField = TouchPhieuChiPanel.class.getDeclaredField("currentExpense");
        expenseField.setAccessible(true);
        PhieuChi loaded = (PhieuChi) expenseField.get(form);
        assertNotNull(loaded);
        assertEquals("PC26/00088", loaded.getSoPhieu());
        assertEquals("Nhà cung cấp Test", loaded.getTenDoiTuong());
        assertEquals(450000.0, loaded.getSoTien());
    }

    @Test
    void capturePreviewScreenshot() throws Exception {
        TouchDanhMucPhieuChiPanel panel = new TouchDanhMucPhieuChiPanel(() -> {});
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

        File out = new File("C:/Users/Windows/.gemini/antigravity/brain/c0738db6-8c1f-43c4-89fb-69168aa332c2/preview_touch_danhmuc_phieuchi.png");
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

    private static void setField(Object target, String fieldName, Object val) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, val);
    }
}
