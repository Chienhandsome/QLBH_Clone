package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.MatHang;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TouchChonGiaBanDialogTest {

    @Test
    void testDialogInitialization() {
        MatHang mh = new MatHang();
        mh.setCode("02B");
        mh.setName("Bếp ga 02B123");
        mh.setGiaBan(63800002);
        mh.setGiaBan2(600000);
        mh.setGiaBan3(200000);
        mh.setGiaBan4(230000);

        TouchChonGiaBanDialog dlg = new TouchChonGiaBanDialog(null, mh, 63800002);
        assertNotNull(dlg);
        assertFalse(dlg.isConfirmed());
    }
}
