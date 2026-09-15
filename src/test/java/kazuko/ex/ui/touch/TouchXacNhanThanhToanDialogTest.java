package kazuko.ex.ui.touch;

import kazuko.ex.model.banhang.DonHang;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TouchXacNhanThanhToanDialogTest {

    @Test
    void testInitializationAndDefaults() {
        DonHang dh = new DonHang();
        dh.setTongCong(1668000.0);

        TouchXacNhanThanhToanDialog dlg = new TouchXacNhanThanhToanDialog(null, dh, true);
        assertNotNull(dlg);
        assertEquals(1668000.0, dlg.getTienKhachDua(), 0.001);
        assertEquals(0.0, dlg.getTienATM(), 0.001);
        assertEquals(0.0, dlg.getTienTraLai(), 0.001);
        assertEquals(TouchXacNhanThanhToanDialog.ResultAction.CANCEL, dlg.getResultAction());
    }
}