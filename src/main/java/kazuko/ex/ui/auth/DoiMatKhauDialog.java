package kazuko.ex.ui.auth;

import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.util.AppColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DoiMatKhauDialog extends JDialog {
    private final SUser currentUser;
    private final AuthService authService = new AuthService();

    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    public DoiMatKhauDialog(Frame parent, SUser user) {
        super(parent, "Đổi mật khẩu", true);
        this.currentUser = user;
        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Header Panel: Icon ổ khóa + Tên tài khoản (Nền trắng)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));

        JLabel lblLockIcon = new JLabel("🔒");
        lblLockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        headerPanel.add(lblLockIcon);

        String accountName = (currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty())
                ? currentUser.getName()
                : (currentUser != null ? currentUser.getUsername() : "Admin");

        JLabel lblTitle = new JLabel("Đổi mật khẩu cho tài khoản: " + accountName);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(20, 20, 20));
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Body Panel (Nền xanh pastel #CADDF3)
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(new Color(202, 221, 243));
        bodyPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Dòng chú thích
        JLabel lblDesc = new JLabel("<html><body style='width: 320px; font-family: Segoe UI; font-size: 12px; color: #1a1a1a;'>" +
                "Để thay đổi mật khẩu, mời bạn nhập mật khẩu cũ, điền mật khẩu mới và nhắc lại mật khẩu mới một lần nữa" +
                "</body></html>");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(lblDesc);
        bodyPanel.add(Box.createVerticalStrut(14));

        // Khung nhập Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Mật khẩu cũ
        JLabel lblOld = new JLabel("Mật khẩu cũ:");
        lblOld.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(lblOld, gbc);

        txtOldPassword = new JPasswordField(18);
        txtOldPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtOldPassword.setBorder(new CompoundBorder(
                new LineBorder(new Color(120, 160, 210), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(txtOldPassword, gbc);

        // Mật khẩu mới
        JLabel lblNew = new JLabel("Mật khẩu mới:");
        lblNew.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblNew, gbc);

        txtNewPassword = new JPasswordField(18);
        txtNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNewPassword.setBorder(new CompoundBorder(
                new LineBorder(new Color(120, 160, 210), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtNewPassword, gbc);

        // Nhập lại
        JLabel lblConfirm = new JLabel("Nhập lại:");
        lblConfirm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblConfirm, gbc);

        txtConfirmPassword = new JPasswordField(18);
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtConfirmPassword.setBorder(new CompoundBorder(
                new LineBorder(new Color(120, 160, 210), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtConfirmPassword, gbc);

        bodyPanel.add(formPanel);
        bodyPanel.add(Box.createVerticalStrut(14));

        // Hàng nút bấm phía dưới (Ghi dữ liệu / Thoát)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = new JButton("Ghi dữ liệu");
        JButton btnCancel = new JButton("Thoát");

        btnSave.setPreferredSize(new Dimension(95, 26));
        btnCancel.setPreferredSize(new Dimension(75, 26));

        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnSave.setBackground(Color.WHITE);
        btnCancel.setBackground(Color.WHITE);

        btnSave.addActionListener(e -> onChangePassword());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        bodyPanel.add(btnPanel);

        add(bodyPanel, BorderLayout.CENTER);

        // Nhấn Enter để gửi form
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onChangePassword();
                }
            }
        };
        txtOldPassword.addKeyListener(enterListener);
        txtNewPassword.addKeyListener(enterListener);
        txtConfirmPassword.addKeyListener(enterListener);

        // Phím ESC để đóng dialog
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().setDefaultButton(btnSave);
    }

    private void onChangePassword() {
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập mật khẩu mới!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                    "Mật khẩu nhắc lại không trùng khớp!",
                    "Lỗi xác nhận",
                    JOptionPane.ERROR_MESSAGE);
            txtConfirmPassword.requestFocus();
            return;
        }

        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy phiên người dùng hiện tại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra mật khẩu cũ hiện tại
        String currentPass = currentUser.getPassword() != null ? currentUser.getPassword().trim() : "";
        if (!currentPass.isEmpty() && !currentPass.equals(oldPass)) {
            JOptionPane.showMessageDialog(this,
                    "Mật khẩu cũ không chính xác! Vui lòng kiểm tra lại.",
                    "Lỗi đổi mật khẩu",
                    JOptionPane.ERROR_MESSAGE);
            txtOldPassword.requestFocus();
            return;
        }

        boolean success = authService.changePassword(currentUser, oldPass, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Đổi mật khẩu thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cập nhật mật khẩu thất bại! Vui lòng thử lại sau.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
