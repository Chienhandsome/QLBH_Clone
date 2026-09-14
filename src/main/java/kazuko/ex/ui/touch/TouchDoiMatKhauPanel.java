package kazuko.ex.ui.touch;

import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Full-screen touch password change workflow backed by the existing AuthService. */
final class TouchDoiMatKhauPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 145, 0);
    private static final Color RED = new Color(165, 5, 12);

    private final SUser user;
    private final Runnable back;
    private final AuthService authService = new AuthService();
    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private final JPasswordField oldPassword = passwordField();
    private final JPasswordField newPassword = passwordField();
    private final JPasswordField confirmPassword = passwordField();
    private final StringBuilder keyboardBuffer = new StringBuilder();
    private final JLabel keyboardDisplay = new JLabel("", SwingConstants.CENTER);
    private JPasswordField keyboardTarget;

    TouchDoiMatKhauPanel(SUser user, Runnable back) {
        this.user = user;
        this.back = back;
        setLayout(new BorderLayout());
        screens.add(createForm(), "form");
        screens.add(createKeyboard(), "keyboard");
        add(screens, BorderLayout.CENTER);
        navigation.show(screens, "form");
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new BorderLayout(0, 14));
        form.setBackground(BACKGROUND);
        form.setBorder(new EmptyBorder(5, 5, 15, 5));
        String account = user != null && user.getUsername() != null ? user.getUsername().toUpperCase() : "ADMIN";
        JLabel title = header("ĐỔI MẬT KHẨU CHO TÀI KHOẢN: " + account);
        form.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addInstruction(content, gbc, 0, "ĐỂ THAY ĐỔI MẬT KHẨU, MỜI BẠN NHẬP MẬT KHẨU CŨ");
        addPasswordRow(content, gbc, 1, "MẬT KHẨU CŨ:", oldPassword);
        addInstruction(content, gbc, 2, "SAU ĐÓ NHẬP MẬT KHẨU MỚI VÀ NHẬP LẠI MỘT LẦN NỮA ĐỂ ĐẢM BẢO KHÔNG BỊ NHẦM");
        addPasswordRow(content, gbc, 3, "MẬT KHẨU MỚI:", newPassword);
        addPasswordRow(content, gbc, 4, "NHẬP LẠI:", confirmPassword);
        gbc.gridy = 5;
        gbc.weighty = 1;
        content.add(Box.createVerticalGlue(), gbc);
        form.add(content, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(0, 0, 0, 12));
        JButton accept = actionButton("CHẤP NHẬN", GREEN, 280);
        accept.addActionListener(e -> changePassword());
        JButton cancel = actionButton("HỦY BỎ", RED, 235);
        cancel.addActionListener(e -> back.run());
        actions.add(accept);
        actions.add(cancel);
        form.add(actions, BorderLayout.SOUTH);
        return form;
    }

    private void addInstruction(JPanel panel, GridBagConstraints gbc, int row, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.YELLOW);
        label.setFont(new Font("Segoe UI", Font.BOLD, 19));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        panel.add(label, gbc);
    }

    private void addPasswordRow(JPanel panel, GridBagConstraints gbc, int row,
                                String labelText, JPasswordField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setPreferredSize(new Dimension(280, 52));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        JButton keyboard = actionButton("⌨", new Color(0, 90, 92), 72);
        keyboard.setFont(new Font("Segoe UI Symbol", Font.BOLD, 30));
        keyboard.addActionListener(e -> showKeyboard(field));
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(keyboard, gbc);
    }

    private JPanel createKeyboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 9));
        panel.setBackground(HEADER);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(header("MỜI BẠN NHẬP MẬT KHẨU"), BorderLayout.NORTH);
        keyboardDisplay.setOpaque(true);
        keyboardDisplay.setBackground(Color.WHITE);
        keyboardDisplay.setForeground(Color.BLACK);
        keyboardDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 38));
        keyboardDisplay.setPreferredSize(new Dimension(0, 155));

        JPanel center = new JPanel(new BorderLayout(0, 9));
        center.setOpaque(false);
        center.add(keyboardDisplay, BorderLayout.NORTH);
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        addKeyboardRow(rows, "1234567890-=⌫");
        addKeyboardRow(rows, "QWERTYUIOP\\@");
        addKeyboardRow(rows, "ASDFGHJKL;'");
        addKeyboardRow(rows, "ZXCVBNM,.");
        center.add(rows, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        JButton cancel = actionButton("HỦY BỎ", RED, 285);
        cancel.setPreferredSize(new Dimension(285, 100));
        cancel.addActionListener(e -> navigation.show(screens, "form"));
        JButton space = actionButton("", Color.DARK_GRAY, 700);
        space.setPreferredSize(new Dimension(700, 100));
        space.addActionListener(e -> appendKey(" "));
        JButton accept = actionButton("➜", GREEN, 285);
        accept.setPreferredSize(new Dimension(285, 100));
        accept.setFont(new Font("Segoe UI Symbol", Font.BOLD, 55));
        accept.addActionListener(e -> acceptKeyboard());
        actions.add(cancel);
        actions.add(space);
        actions.add(accept);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void addKeyboardRow(JPanel rows, String keys) {
        JPanel row = new JPanel(new GridLayout(1, keys.length(), 6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        for (int i = 0; i < keys.length(); i++) {
            String key = String.valueOf(keys.charAt(i));
            JButton button = actionButton(key, new Color(0, 105, 130), 90);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 26));
            button.addActionListener(e -> {
                if ("⌫".equals(key)) backspaceKey();
                else appendKey(key);
            });
            row.add(button);
        }
        rows.add(row);
        rows.add(Box.createVerticalStrut(6));
    }

    private void showKeyboard(JPasswordField target) {
        keyboardTarget = target;
        keyboardBuffer.setLength(0);
        keyboardBuffer.append(target.getPassword());
        refreshKeyboardDisplay();
        navigation.show(screens, "keyboard");
    }

    private void appendKey(String key) {
        keyboardBuffer.append(key);
        refreshKeyboardDisplay();
    }

    private void backspaceKey() {
        if (!keyboardBuffer.isEmpty()) keyboardBuffer.deleteCharAt(keyboardBuffer.length() - 1);
        refreshKeyboardDisplay();
    }

    private void refreshKeyboardDisplay() {
        keyboardDisplay.setText("•".repeat(keyboardBuffer.length()));
    }

    private void acceptKeyboard() {
        if (keyboardTarget != null) keyboardTarget.setText(keyboardBuffer.toString());
        navigation.show(screens, "form");
    }

    private void changePassword() {
        String oldValue = new String(oldPassword.getPassword());
        String newValue = new String(newPassword.getPassword());
        String confirmValue = new String(confirmPassword.getPassword());
        if (newValue.isBlank()) {
            showWarning("Vui lòng nhập mật khẩu mới!");
            return;
        }
        if (!newValue.equals(confirmValue)) {
            showWarning("Mật khẩu nhập lại không trùng khớp!");
            return;
        }
        if (!authService.changePassword(user, oldValue, newValue)) {
            showWarning("Mật khẩu cũ không chính xác hoặc không thể cập nhật dữ liệu!");
            return;
        }
        JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        back.run();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        field.setPreferredSize(new Dimension(700, 52));
        field.setBorder(new CompoundBorder(new LineBorder(new Color(190, 215, 230), 2),
                new EmptyBorder(4, 10, 4, 10)));
        return field;
    }

    private JLabel header(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(HEADER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 25));
        label.setPreferredSize(new Dimension(0, 70));
        label.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));
        return label;
    }

    private JButton actionButton(String text, Color color, int width) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, 72));
        button.setBorder(new LineBorder(new Color(205, 230, 240), 2, true));
        return button;
    }
}
