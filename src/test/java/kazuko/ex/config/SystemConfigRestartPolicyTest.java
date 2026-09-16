package kazuko.ex.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemConfigRestartPolicyTest {

    @Test
    void businessSettingChangesDoNotRequireRestart() {
        assertFalse(SystemConfigRestartPolicy.requiresRestart(
                "Nhỏ (110%)", "Office 2010 - Blue",
                "Nhỏ (110%)", "Office 2010 - Blue"));
    }

    @Test
    void fontSizeChangeRequiresRestart() {
        assertTrue(SystemConfigRestartPolicy.requiresRestart(
                "Nhỏ (110%)", "Office 2010 - Blue",
                "Lớn (130%)", "Office 2010 - Blue"));
    }

    @Test
    void systemThemeChangeRequiresRestart() {
        assertTrue(SystemConfigRestartPolicy.requiresRestart(
                "Nhỏ (110%)", "Office 2010 - Blue",
                "Nhỏ (110%)", "Office 2010 - Black"));
    }

    @Test
    void surroundingWhitespaceDoesNotCreateFalseRestartRequirement() {
        assertFalse(SystemConfigRestartPolicy.requiresRestart(
                " Nhỏ (110%) ", " Office 2010 - Blue ",
                "Nhỏ (110%)", "Office 2010 - Blue"));
    }
}
