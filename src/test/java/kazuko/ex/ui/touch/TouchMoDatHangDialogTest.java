package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TouchMoDatHangDialogTest {

    @Test
    void testInitialization() {
        TouchMoDatHangDialog dlg = new TouchMoDatHangDialog(null);
        assertNotNull(dlg);
        assertFalse(dlg.isConfirmed());
    }
}