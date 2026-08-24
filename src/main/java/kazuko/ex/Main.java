package kazuko.ex;

import kazuko.ex.config.AppTheme;
import kazuko.ex.ui.auth.LoginForm;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. Thiết lập giao diện FlatLaf (Office Blue)
        AppTheme.setup();

        // 2. Khởi chạy Màn hình Đăng nhập
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}