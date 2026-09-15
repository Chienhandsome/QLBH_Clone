package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TouchThongKeBanHangDialogTest {

    @Test
    void testInitialization() {
        TouchThongKeBanHangDialog dlg = new TouchThongKeBanHangDialog(null);
        assertNotNull(dlg);
        assertDoesNotThrow(() -> dlg.loadDataForDate(new Date()));
    }
}