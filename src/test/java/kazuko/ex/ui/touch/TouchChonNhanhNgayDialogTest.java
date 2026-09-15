package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TouchChonNhanhNgayDialogTest {

    @Test
    void testPresetRanges() {
        AtomicReference<Date> fromRef = new AtomicReference<>();
        AtomicReference<Date> toRef = new AtomicReference<>();

        TouchChonNhanhNgayDialog dlg = new TouchChonNhanhNgayDialog(null, (f, t) -> {
            fromRef.set(f);
            toRef.set(t);
        });

        // Test Hôm nay
        dlg.applyPreset("HÔM NAY");
        assertNotNull(fromRef.get());
        assertNotNull(toRef.get());
        assertTrue(toRef.get().getTime() >= fromRef.get().getTime());

        // Test Tháng này
        dlg.applyPreset("THÁNG NÀY");
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(fromRef.get());
        assertEquals(1, calStart.get(Calendar.DAY_OF_MONTH));

        // Test Năm nay
        dlg.applyPreset("NĂM NAY");
        calStart.setTime(fromRef.get());
        assertEquals(1, calStart.get(Calendar.DAY_OF_YEAR));
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(toRef.get());
        assertEquals(Calendar.DECEMBER, calEnd.get(Calendar.MONTH));
        assertEquals(31, calEnd.get(Calendar.DAY_OF_MONTH));

        // Test Xóa trắng
        dlg.applyClear();
        assertNull(fromRef.get());
        assertNull(toRef.get());
    }
}
