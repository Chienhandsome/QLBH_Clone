package kazuko.ex.config;

import com.formdev.flatlaf.FlatLightLaf;
import kazuko.ex.util.AppColors;

import javax.swing.*;
import java.awt.*;

public class AppTheme {
    public static void setup() {
        try {
            // Thiết lập Look and Feel hiện đại FlatLaf
            FlatLightLaf.setup();

            // Tinh chỉnh màu sắc chủ đạo xanh dương (Office Blue) giống Krypton Toolkit
            UIManager.put("Component.accentColor", AppColors.PRIMARY_BLUE);
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("ProgressBar.arc", 6);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabHeight", 32);
            UIManager.put("TableHeader.height", 28);
            UIManager.put("Table.rowHeight", 24);

            // Font chữ chuẩn hiển thị tiếng Việt mượt mà
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
            UIManager.put("defaultFont", defaultFont);

        } catch (Exception e) {
            System.err.println("Không thể thiết lập FlatLaf theme: " + e.getMessage());
        }
    }
}
