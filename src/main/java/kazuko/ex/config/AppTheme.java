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
            // Màu hệ thống chỉ áp dụng cho chrome (menu, toolbar, khung ngoài).
            // Vùng dữ liệu luôn sáng giống ứng dụng POS mẫu, kể cả khi dùng theme Black.
            applyLightDataSurfaceDefaults();
        } catch (Exception e) {
            System.err.println("Không thể thiết lập giao diện '" + selectedTheme + "': " + e.getMessage());
            FlatLightLaf.setup();
            AppColors.applyThemeMode(false);
            applyCommonDefaults(AppColors.PRIMARY_BLUE, fontSize);
            applyLightDataSurfaceDefaults();
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

    /**
     * Giữ bảng, cây, danh sách và các ô nhập liệu ở bảng màu sáng.
     * Không đặt màu Panel/Menu/ToolBar tại đây để theme vẫn đổi được vùng ngoài.
     */
    static void applyLightDataSurfaceDefaults() {
        Color surface = Color.WHITE;
        Color foreground = new Color(30, 30, 30);
        Color header = new Color(235, 242, 252);
        Color border = new Color(190, 205, 220);
        Color selection = new Color(10, 132, 214);
        Color alternateRow = new Color(226, 244, 250);

        UIManager.put("Table.background", surface);
        UIManager.put("Table.foreground", foreground);
        UIManager.put("Table.gridColor", border);
        UIManager.put("Table.alternateRowColor", alternateRow);
        UIManager.put("Table.selectionBackground", selection);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.focusCellBackground", selection);
        UIManager.put("Table.focusCellForeground", Color.WHITE);
        UIManager.put("TableHeader.background", header);
        UIManager.put("TableHeader.foreground", foreground);

        UIManager.put("Tree.background", surface);
        UIManager.put("Tree.foreground", foreground);
        UIManager.put("Tree.selectionBackground", selection);
        UIManager.put("Tree.selectionForeground", Color.WHITE);
        UIManager.put("Tree.textBackground", surface);
        UIManager.put("Tree.textForeground", foreground);

        UIManager.put("List.background", surface);
        UIManager.put("List.foreground", foreground);
        UIManager.put("List.selectionBackground", selection);
        UIManager.put("List.selectionForeground", Color.WHITE);

        for (String prefix : new String[]{
                "TextField", "FormattedTextField", "PasswordField",
                "TextArea", "TextPane", "EditorPane"
        }) {
            UIManager.put(prefix + ".background", surface);
            UIManager.put(prefix + ".foreground", foreground);
            UIManager.put(prefix + ".caretForeground", foreground);
            UIManager.put(prefix + ".selectionBackground", selection);
            UIManager.put(prefix + ".selectionForeground", Color.WHITE);
        }

        UIManager.put("ComboBox.background", surface);
        UIManager.put("ComboBox.foreground", foreground);
        UIManager.put("ComboBox.buttonBackground", header);
        UIManager.put("ComboBox.selectionBackground", selection);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Spinner.background", surface);
        UIManager.put("Spinner.foreground", foreground);
        UIManager.put("Spinner.buttonBackground", header);
        UIManager.put("ScrollPane.background", surface);
        UIManager.put("Viewport.background", surface);
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
