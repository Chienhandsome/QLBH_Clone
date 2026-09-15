package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TouchThietLapCotDialogTest {

    @Test
    void testDialogComponentsAndEnableDisableState() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        TouchThietLapCotDialog dialog = new TouchThietLapCotDialog(null, () -> {});

        // Lấy các trường private qua reflection để kiểm thử logic UI
        Field fAlignLeft = TouchThietLapCotDialog.class.getDeclaredField("btnAlignLeft");
        fAlignLeft.setAccessible(true);
        JButton btnAlignLeft = (JButton) fAlignLeft.get(dialog);

        Field fAlignRight = TouchThietLapCotDialog.class.getDeclaredField("btnAlignRight");
        fAlignRight.setAccessible(true);
        JButton btnAlignRight = (JButton) fAlignRight.get(dialog);

        Field fBold = TouchThietLapCotDialog.class.getDeclaredField("btnBold");
        fBold.setAccessible(true);
        JButton btnBold = (JButton) fBold.get(dialog);

        Field fItalic = TouchThietLapCotDialog.class.getDeclaredField("btnItalic");
        fItalic.setAccessible(true);
        JButton btnItalic = (JButton) fItalic.get(dialog);

        Field fFontSize = TouchThietLapCotDialog.class.getDeclaredField("btnFontSize");
        fFontSize.setAccessible(true);
        JButton btnFontSize = (JButton) fFontSize.get(dialog);

        Field fTextColor = TouchThietLapCotDialog.class.getDeclaredField("btnTextColor");
        fTextColor.setAccessible(true);
        JButton btnTextColor = (JButton) fTextColor.get(dialog);

        Field fSelectedUsed = TouchThietLapCotDialog.class.getDeclaredField("selectedUsed");
        fSelectedUsed.setAccessible(true);

        // Ban đầu chưa chọn cột nào -> Các nút căn lề, đậm, nghiêng, cỡ chữ, màu chữ phải DISABLED
        assertNull(fSelectedUsed.get(dialog), "Ban đầu chưa có cột sử dụng nào được chọn");
        assertFalse(btnAlignLeft.isEnabled(), "btnAlignLeft phải bị disabled khi chưa chọn cột");
        assertFalse(btnAlignRight.isEnabled(), "btnAlignRight phải bị disabled khi chưa chọn cột");
        assertFalse(btnBold.isEnabled(), "btnBold phải bị disabled khi chưa chọn cột");
        assertFalse(btnItalic.isEnabled(), "btnItalic phải bị disabled khi chưa chọn cột");
        assertFalse(btnFontSize.isEnabled(), "btnFontSize phải bị disabled khi chưa chọn cột");
        assertFalse(btnTextColor.isEnabled(), "btnTextColor phải bị disabled khi chưa chọn cột");

        // Mô phỏng người dùng click chọn 1 cột trong CÁC CỘT SỬ DỤNG
        Field fConfig = TouchThietLapCotDialog.class.getDeclaredField("config");
        fConfig.setAccessible(true);
        TouchMatHangColumnConfig config = (TouchMatHangColumnConfig) fConfig.get(dialog);
        assertFalse(config.getUsedColumns().isEmpty());

        TouchMatHangColumnConfig.ColumnItem col = config.getUsedColumns().get(0);
        fSelectedUsed.set(dialog, col);

        // Gọi hàm cập nhật giao diện
        java.lang.reflect.Method updateFormatControls = TouchThietLapCotDialog.class.getDeclaredMethod("updateFormatControls");
        updateFormatControls.setAccessible(true);
        updateFormatControls.invoke(dialog);

        // Sau khi chọn cột -> Các nút phải ENABLED
        assertTrue(btnAlignLeft.isEnabled(), "btnAlignLeft phải enable khi đã chọn cột");
        assertTrue(btnAlignRight.isEnabled(), "btnAlignRight phải enable khi đã chọn cột");
        assertTrue(btnBold.isEnabled(), "btnBold phải enable khi đã chọn cột");
        assertTrue(btnItalic.isEnabled(), "btnItalic phải enable khi đã chọn cột");
        assertTrue(btnFontSize.isEnabled(), "btnFontSize phải enable khi đã chọn cột");
        assertTrue(btnTextColor.isEnabled(), "btnTextColor phải enable khi đã chọn cột");
    }

    @Test
    void testToggleFormatting() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;
        TouchThietLapCotDialog dialog = new TouchThietLapCotDialog(null, () -> {});

        Field fFormatOpen = TouchThietLapCotDialog.class.getDeclaredField("isFormattingOpen");
        fFormatOpen.setAccessible(true);
        assertFalse((boolean) fFormatOpen.get(dialog));

        java.lang.reflect.Method toggle = TouchThietLapCotDialog.class.getDeclaredMethod("toggleFormatting");
        toggle.setAccessible(true);
        toggle.invoke(dialog);

        assertTrue((boolean) fFormatOpen.get(dialog), "Bấm Định dạng phải chuyển trạng thái mở khung");
    }

    @Test
    void testChonMauDialogPalette() {
        if (GraphicsEnvironment.isHeadless()) return;
        TouchChonMauDialog dialog = new TouchChonMauDialog(null, Color.WHITE);
        assertNotNull(dialog);
        assertEquals(880, dialog.getWidth());
        assertEquals(560, dialog.getHeight());
    }
}
