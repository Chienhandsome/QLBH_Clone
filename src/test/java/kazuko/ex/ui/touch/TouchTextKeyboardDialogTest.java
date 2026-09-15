package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TouchTextKeyboardDialogTest {

    @Test
    void testKeyboardInputAndShift() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        TouchTextKeyboardDialog dlg = new TouchTextKeyboardDialog(null, "TEST KEYBOARD", "ABC");

        Field fInput = TouchTextKeyboardDialog.class.getDeclaredField("txtInput");
        fInput.setAccessible(true);
        JTextField txtInput = (JTextField) fInput.get(dlg);
        assertEquals("ABC", txtInput.getText());

        // Test handleChar
        Method mChar = TouchTextKeyboardDialog.class.getDeclaredMethod("handleChar", String.class);
        mChar.setAccessible(true);
        mChar.invoke(dlg, "D");
        assertEquals("ABCD", txtInput.getText());

        // Test handleBackspace
        Method mBack = TouchTextKeyboardDialog.class.getDeclaredMethod("handleBackspace");
        mBack.setAccessible(true);
        mBack.invoke(dlg);
        assertEquals("ABC", txtInput.getText());

        // Test toggleShift
        Field fShift = TouchTextKeyboardDialog.class.getDeclaredField("isShift");
        fShift.setAccessible(true);
        boolean before = (boolean) fShift.get(dlg);

        Method mShift = TouchTextKeyboardDialog.class.getDeclaredMethod("toggleShift");
        mShift.setAccessible(true);
        mShift.invoke(dlg);
        assertEquals(!before, fShift.get(dlg));
    }
}
