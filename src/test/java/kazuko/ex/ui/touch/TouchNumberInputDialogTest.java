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
}
