package kazuko.ex.ui.auth;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.ui.main.MainFrame;
import kazuko.ex.util.AppColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

public class LoginForm extends JFrame {
    private JComboBox<String> cbUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkRememberPass;
    private JCheckBox chkAutoLogin;
    private JLabel lblDbName;
    private final AuthService authService = new AuthService();

    public LoginForm() {
        initComponents();
        loadDatabaseName();
    }

    private void initComponents() {
        setTitle("Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // 1. Icon cửa sổ
        setWindowIcon();

        // 2. Banner trên đầu
        JPanel topBannerPanel = new JPanel(new BorderLayout());
        topBannerPanel.setBackground(AppColors.WHITE);
        topBannerPanel.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.BORDER_BLUE));

        URL bannerUrl = getClass().getResource("/banner.png");
        if (bannerUrl != null) {
            JLabel lblBanner = new JLabel(new ImageIcon(bannerUrl));
            lblBanner.setHorizontalAlignment(SwingConstants.CENTER);
            topBannerPanel.add(lblBanner, BorderLayout.CENTER);
        } else {
            JLabel lblBannerText = new JLabel("PHẦN MỀM QUẢN LÝ CỬA HÀNG", SwingConstants.CENTER);
            lblBannerText.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblBannerText.setForeground(AppColors.PRIMARY_BLUE);
            lblBannerText.setPreferredSize(new Dimension(460, 90));
            topBannerPanel.add(lblBannerText, BorderLayout.CENTER);
        }
        add(topBannerPanel, BorderLayout.NORTH);

        // 3. Khung thân chính
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(12, 18, 10, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);

        // --- CỘT TRÁI: Logo Tân An Phát & Link CSDL ---
        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));
        leftBox.setOpaque(false);

        URL logoUrl = getClass().getResource("/logo.png");
        JLabel lblLogo;
        if (logoUrl != null) {
            lblLogo = new JLabel(new ImageIcon(logoUrl));
        } else {
            lblLogo = new JLabel("TÂN AN PHÁT", SwingConstants.CENTER);
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblLogo.setForeground(AppColors.WARNING_ORANGE);
        }
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftBox.add(lblLogo);

        leftBox.add(Box.createVerticalStrut(18));

        // Dòng "Cơ sở dữ liệu: DEMO"
        JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dbPanel.setOpaque(false);
        dbPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDbLink = new JLabel("<html><u>Cơ sở dữ liệu:</u></html>");
        lblDbLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDbLink.setForeground(AppColors.TEXT_LINK);
        lblDbLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblDbLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DatabaseConfigDialog dialog = new DatabaseConfigDialog(LoginForm.this);
                dialog.setVisible(true);
                loadDatabaseName();
            }
        });

        lblDbName = new JLabel("DEMO");
        lblDbName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDbName.setForeground(AppColors.TEXT_DARK);

        dbPanel.add(lblDbLink);
        dbPanel.add(lblDbName);
        leftBox.add(dbPanel);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        centerPanel.add(leftBox, gbc);

        // Khoảng cách giữa 2 cột
        gbc.gridheight = 1;
        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(Box.createHorizontalStrut(10), gbc);

        // --- CỘT PHẢI: Form nhập Người dùng & Mật khẩu ---
        // 1. Người dùng
        JLabel lblUser = new JLabel("Người dùng:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(AppColors.TEXT_DARK);
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(lblUser, gbc);

        cbUsername = new JComboBox<>(new String[]{"Admin", "thungan", "ketoan"});
        cbUsername.setEditable(true);
        cbUsername.setPreferredSize(new Dimension(175, 26));
        cbUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 3; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(cbUsername, gbc);

        // 2. Mật khẩu
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(AppColors.TEXT_DARK);
        gbc.gridx = 2; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField(13);
        txtPassword.setPreferredSize(new Dimension(175, 26));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 3; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(txtPassword, gbc);

        // 3. Checkbox Ghi nhớ mật khẩu
        chkRememberPass = new JCheckBox("Ghi nhớ mật khẩu", true);
        chkRememberPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkRememberPass.setOpaque(false);
        gbc.gridx = 3; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(chkRememberPass, gbc);

        // 4. Checkbox Tự động đăng nhập
        chkAutoLogin = new JCheckBox("Tự động đăng nhập", false);
        chkAutoLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkAutoLogin.setOpaque(false);
        gbc.gridx = 3; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(chkAutoLogin, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 4. Khung chân trang (Buttons)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, AppColors.BORDER_SEPARATOR),
                new EmptyBorder(10, 20, 14, 20)
        ));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);

        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnCancel = new JButton("Hủy bỏ");

        btnLogin.setPreferredSize(new Dimension(100, 30));
        btnCancel.setPreferredSize(new Dimension(90, 30));

        btnLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btnLogin.setBackground(AppColors.WHITE);
        btnCancel.setBackground(AppColors.WHITE);

        btnLogin.addActionListener(e -> onLogin());
        btnCancel.addActionListener(e -> System.exit(0));

        // Nhấn Enter để đăng nhập
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onLogin();
                }
            }
        });

        btnGroup.add(btnLogin);
        btnGroup.add(btnCancel);
        bottomPanel.add(btnGroup, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void setWindowIcon() {
        URL logoUrl = getClass().getResource("/logo.png");
        if (logoUrl != null) {
            setIconImage(new ImageIcon(logoUrl).getImage());
        }
    }

    private void loadDatabaseName() {
        String dbPath = DatabaseConnection.getConfig().getDatabasePath();
        String dbName = "DEMO";
        if (dbPath != null && !dbPath.isEmpty()) {
            File f = new File(dbPath);
            String fileName = f.getName();
            if (fileName.contains(".")) {
                dbName = fileName.substring(0, fileName.lastIndexOf('.')).toUpperCase();
            } else {
                dbName = fileName.toUpperCase();
            }
        }
        lblDbName.setText(dbName);
    }

    private void onLogin() {
        String username = cbUsername.getSelectedItem() != null ? cbUsername.getSelectedItem().toString().trim() : "";
        String password = new String(txtPassword.getPassword());

        SUser user = authService.login(username, password);
        if (user != null) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "Tên đăng nhập hoặc mật khẩu không chính xác!",
                    "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
