package kazuko.ex;

import kazuko.ex.config.AppTheme;
import kazuko.ex.ui.auth.LoginForm;
import kazuko.ex.ui.touch.TouchSalesFrame;

import javax.swing.*;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // 1. Thiết lập giao diện FlatLaf (Office Blue)
        AppTheme.setup();

        // 2. Khởi chạy Màn hình Đăng nhập
        boolean touchMode = Arrays.stream(args)
                .anyMatch(arg -> "--touch".equalsIgnoreCase(arg) || "--ts".equalsIgnoreCase(arg));
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = touchMode
                    ? new LoginForm(false, user -> new TouchSalesFrame(user).setVisible(true))
                    : new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
