package kazuko.ex.config;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.AppColors;

import javax.swing.*;
import java.awt.*;

public class AppTheme {
    public static void setup() {
        try {
            SystemConfig config = new SystemConfigDAO().loadConfig();
            applyTheme(config.getGiaoDienChuongTrinh(), config.getDoToChuHienThi());
        } catch (Exception e) {
            System.err.println("Không thể đọc cấu hình giao diện: " + e.getMessage());
            applyTheme("Office 2010 - Blue", "Nhỏ (110%)");
        }
    }

    public static void applyTheme(String themeName, String fontScale) {
        String selectedTheme = themeName != null ? themeName.trim() : "Office 2010 - Blue";
        int fontSize = resolveFontSize(fontScale);
        Color accentColor;

        try {
            switch (selectedTheme) {
                case "Office 2010 - Black" -> {
                    FlatDarkLaf.setup();
                    accentColor = new Color(0x4A88C7);
                    AppColors.applyThemeMode(true);
                }
                case "Office 2010 - Silver" -> {
                    FlatLightLaf.setup();
                    accentColor = new Color(0x69737D);
                    AppColors.applyThemeMode(false);
                }
                case "Windows Standard" -> {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    accentColor = new Color(0x0066CC);
                    AppColors.applyThemeMode(false);
                }
                case "FlatLaf Light" -> {
                    FlatLightLaf.setup();
                    accentColor = new Color(0x0078D7);
                    AppColors.applyThemeMode(false);
                }
                case "Office 2010 - Blue" -> {
                    FlatLightLaf.setup();
                    accentColor = new Color(0x0066CC);
                    AppColors.applyThemeMode(false);
                }
                default -> {
                    FlatLightLaf.setup();
                    accentColor = new Color(0x0066CC);
                    AppColors.applyThemeMode(false);
                }
            }

            applyCommonDefaults(accentColor, fontSize);
        } catch (Exception e) {
            System.err.println("Không thể thiết lập giao diện '" + selectedTheme + "': " + e.getMessage());
            FlatLightLaf.setup();
            AppColors.applyThemeMode(false);
            applyCommonDefaults(AppColors.PRIMARY_BLUE, fontSize);
        }
    }

    private static void applyCommonDefaults(Color accentColor, int fontSize) {
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, fontSize));
        UIManager.put("Component.accentColor", accentColor);
        UIManager.put("@accentColor", accentColor);
        UIManager.put("Button.arc", 6);
        UIManager.put("Component.arc", 6);
        UIManager.put("ProgressBar.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabHeight", 32);
        UIManager.put("TableHeader.height", 28);
        UIManager.put("Table.rowHeight", 24);
    }

    private static int resolveFontSize(String fontScale) {
        if (fontScale == null) {
            return 12;
        }
        return switch (fontScale.trim()) {
            case "Nhỏ (110%)" -> 13;
            case "Vừa (120%)" -> 14;
            case "Lớn (130%)" -> 16;
            default -> 12;
        };
    }
}
