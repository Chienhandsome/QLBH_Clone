package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.NhanVien;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class TouchChonNhanVienDialogTest {

    @Test
    void testChonNhanVienDialogInitialization() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        NhanVien initial = new NhanVien("nv-test", "Nguyễn Văn Test", "user_female_1");
        TouchChonNhanVienDialog dlg = new TouchChonNhanVienDialog(null, initial);

        Field fEmp = TouchChonNhanVienDialog.class.getDeclaredField("selectedEmployee");
        fEmp.setAccessible(true);
        assertEquals(initial, fEmp.get(dlg));

        Field fConfirmed = TouchChonNhanVienDialog.class.getDeclaredField("confirmed");
        fConfirmed.setAccessible(true);
        assertFalse((boolean) fConfirmed.get(dlg));
    }
}
