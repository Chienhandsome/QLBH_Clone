package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TouchNumberInputDialogTest {

    @Test
    void testNumericKeypadInputs() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;
        TouchNumberInputDialog dialog = new TouchNumberInputDialog(null, "NHẬP SỐ DÒNG HIỂN THỊ TRÊN LƯỚI", 8, 1, 50);

        Field fVal = TouchNumberInputDialog.class.getDeclaredField("currentValue");
        fVal.setAccessible(true);
        assertEquals("8", fVal.get(dialog));

        Method append = TouchNumberInputDialog.class.getDeclaredMethod("appendInput", String.class);
        append.setAccessible(true);

        Method backspace = TouchNumberInputDialog.class.getDeclaredMethod("backspace");
        backspace.setAccessible(true);

        // Backspace xóa 8 -> thành 0
        backspace.invoke(dialog);
        assertEquals("0", fVal.get(dialog));

        // Nhập 3
        append.invoke(dialog, "3");
        assertEquals("3", fVal.get(dialog));

        // Nhập thêm 5 -> 35
        append.invoke(dialog, "5");
        assertEquals("35", fVal.get(dialog));

        // Backspace xóa 5 -> 3
        backspace.invoke(dialog);
        assertEquals("3", fVal.get(dialog));

        // Nhập . -> 3.
        append.invoke(dialog, ".");
        assertEquals("3.", fVal.get(dialog));

        // Nhập 5 -> 3.5
        append.invoke(dialog, "5");
        assertEquals("3.5", fVal.get(dialog));

        // Nhập thêm . -> vẫn là 3.5 không trùng lặp
        append.invoke(dialog, ".");
        assertEquals("3.5", fVal.get(dialog));
    }

    @Test
    void testScrollableRowsPanel() {
        TouchMatHangPanel.ScrollableRowsPanel panel = new TouchMatHangPanel.ScrollableRowsPanel(new GridLayout(0, 3, 7, 7));
        assertTrue(panel.getScrollableTracksViewportWidth(), "Panel phải luôn khớp với chiều rộng viewport để không tràn màn hình");
        assertFalse(panel.getScrollableTracksViewportHeight());
    }

    @Test
    void configurableGridReservesExactlyConfiguredColumnsAndRows() {
        TouchScrollableRowsPanel panel = new TouchScrollableRowsPanel();
        panel.configureGrid(4, 7, 8, 8);
        for (int i = 0; i < 6; i++) panel.add(new JLabel("Khách " + i));

        panel.completeGrid();

        assertEquals(4, panel.getConfiguredColumns());
        assertEquals(7, panel.getConfiguredVisibleRows());
        assertEquals(28, panel.getComponentCount(), "Lưới 4 x 7 phải giữ đủ 28 vị trí");
        GridLayout layout = (GridLayout) panel.getLayout();
        assertEquals(4, layout.getColumns());

        JViewport viewport = new JViewport();
        viewport.setExtentSize(new Dimension(800, 700));
        viewport.setView(panel);
        assertEquals(700, panel.getPreferredSize().height,
                "Bảy dòng cấu hình phải vừa đúng chiều cao vùng hiển thị");

        panel.setSize(panel.getPreferredSize());
        panel.doLayout();
        assertTrue(panel.getComponent(0).getHeight() <= 94,
                "Item không được kéo cao hơn một dòng trong lưới bảy dòng");
    }

    @Test
    void configurableGridAddsScrollableRowsAfterViewportCapacity() {
        TouchScrollableRowsPanel panel = new TouchScrollableRowsPanel();
        panel.configureGrid(4, 7, 8, 8);
        for (int i = 0; i < 29; i++) panel.add(new JLabel("Khách " + i));

        panel.completeGrid();

        assertEquals(32, panel.getComponentCount(), "Dòng thứ tám phải hoàn chỉnh trước khi cuộn");
    }

    @Test
    void everyCardConfigUsesTheSharedGridSettingsContract() {
        TouchGridSettingsModel[] configs = {
                TouchMatHangColumnConfig.createDefault(),
                TouchKhachHangColumnConfig.createDefault(),
                TouchKhachHangThanThietColumnConfig.createDefault(),
                TouchQuanLyBanHangBillColumnConfig.createDefault(),
                TouchLuuVetColumnConfig.createDefault(),
                TouchNhaCungCapColumnConfig.createDefault(),
                TouchNhapKhoColumnConfig.createDefault(),
                TouchTonKhoColumnConfig.createDefault()
        };

        for (TouchGridSettingsModel config : configs) {
            JButton columns = new JButton();
            JButton rows = new JButton();
            TouchGridSettingsSupport.refreshLabels(columns, rows, config);
            assertEquals("SỐ CỘT: " + config.getGridColumns(), columns.getText());
            assertEquals("SỐ DÒNG: " + config.getGridRows(), rows.getText());
        }

        assertEquals(10, TouchGridSettingsSupport.MAX_COLUMNS);
        assertEquals(50, TouchGridSettingsSupport.MAX_ROWS);
    }
}
