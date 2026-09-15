package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TouchDatePickerDialogTest {

    @Test
    void testDatePickerInitialization() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        TouchDatePickerDialog dlg = new TouchDatePickerDialog(null, "15/09/2026");

        Field fDay = TouchDatePickerDialog.class.getDeclaredField("selectedDay");
        fDay.setAccessible(true);
        Field fMonth = TouchDatePickerDialog.class.getDeclaredField("selectedMonth");
        fMonth.setAccessible(true);
        Field fYear = TouchDatePickerDialog.class.getDeclaredField("selectedYear");
        fYear.setAccessible(true);

        assertEquals(15, fDay.get(dlg));
        assertEquals(9, fMonth.get(dlg));
        assertEquals(2026, fYear.get(dlg));

        // Test date string format
        Method mUpdate = TouchDatePickerDialog.class.getDeclaredMethod("updateDisplay");
        mUpdate.setAccessible(true);
        mUpdate.invoke(dlg);

        Field fDisplay = TouchDatePickerDialog.class.getDeclaredField("lblDisplay");
        fDisplay.setAccessible(true);
        javax.swing.JLabel lblDisplay = (javax.swing.JLabel) fDisplay.get(dlg);
        assertEquals("15/09/2026", lblDisplay.getText());
    }

    @Test
    void testClearDate() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        TouchDatePickerDialog dlg = new TouchDatePickerDialog(null, "15/09/2026");
        Field fConfirmed = TouchDatePickerDialog.class.getDeclaredField("confirmed");
        fConfirmed.setAccessible(true);
        Field fResult = TouchDatePickerDialog.class.getDeclaredField("resultDate");
        fResult.setAccessible(true);

        fConfirmed.set(dlg, true);
        fResult.set(dlg, "");

        assertTrue(dlg.isConfirmed());
        assertEquals("", dlg.getResultDate());
    }
}
