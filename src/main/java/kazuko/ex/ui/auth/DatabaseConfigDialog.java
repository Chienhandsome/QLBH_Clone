package kazuko.ex.ui.auth;

import kazuko.ex.config.DatabaseConfig;
import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;

public class DatabaseConfigDialog extends JDialog {
    private JTextField txtServerIp;
    private JTextField txtPort;
    private JTextField txtDatabasePath;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JRadioButton rbLocal;
    private JRadioButton rbServer;
    private JLabel lblStatusIcon;
    private JLabel lblStatusText;
    private JPanel statusPanel;

    public DatabaseConfigDialog(Frame parent) {
        super(parent, "Cấu hình kết nối Máy chủ Cơ sở dữ liệu", true);
        initUI();
        loadCurrentConfig();
        pack();
        setLocationRelativeTo(parent);
    }

    public DatabaseConfigDialog(Dialog parent) {
        super(parent, "Cấu hình kết nối Máy chủ Cơ sở dữ liệu", true);
        initUI();
        loadCurrentConfig();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(245, 247, 250));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));
        headerPanel.setPreferredSize(new Dimension(560, 65));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        leftHeader.setOpaque(false);
        JLabel lblHeaderIcon = new JLabel(IconHelper.getIcon("database1.png", 36, 36));
        if (lblHeaderIcon.getIcon() == null) {
            lblHeaderIcon = new JLabel(IconHelper.getIcon("tsbDatabase.Image.png", 36, 36));
        }

        JPanel textHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        textHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Thiết lập kết nối Máy chủ Cơ sở dữ liệu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(25, 55, 105));

        JLabel lblSubTitle = new JLabel("Cấu hình CSDL Firebird cho mô hình 1 máy cục bộ hoặc nhiều máy qua mạng LAN");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubTitle.setForeground(new Color(110, 120, 135));

        textHeader.add(lblTitle);
        textHeader.add(lblSubTitle);
        leftHeader.add(lblHeaderIcon);
        leftHeader.add(textHeader);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Content
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(new EmptyBorder(12, 16, 12, 16));
        mainContent.setOpaque(false);

        // Khung 1: Chế độ kết nối
        JPanel modeBox = createSectionPanel("1. Chế độ hoạt động");
        JPanel modeRadioPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        modeRadioPanel.setOpaque(false);

        rbLocal = new JRadioButton("<html><b>Máy đơn / Cục bộ (Local)</b> - <i>Dữ liệu lưu trên chính máy tính này (localhost)</i></html>");
        rbServer = new JRadioButton("<html><b>Máy trạm qua mạng LAN (Client)</b> - <i>Kết nối về máy chủ khác trong cùng mạng nội bộ</i></html>");
        rbLocal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbServer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbLocal.setOpaque(false);
        rbServer.setOpaque(false);

        ButtonGroup bgMode = new ButtonGroup();
        bgMode.add(rbLocal);
        bgMode.add(rbServer);
        modeRadioPanel.add(rbLocal);
        modeRadioPanel.add(rbServer);
        modeBox.add(modeRadioPanel);
        mainContent.add(modeBox);
        mainContent.add(Box.createVerticalStrut(10));

        // Khung 2: Thông tin máy chủ & File CSDL
        JPanel connBox = createSectionPanel("2. Thông tin Máy chủ & Dữ liệu");
        JPanel connGrid = new JPanel(new GridBagLayout());
        connGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Row 0: IP & Port
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblIp = new JLabel("Địa chỉ Máy chủ (IP):");
        lblIp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        connGrid.add(lblIp, gbc);

        JPanel ipPortPanel = new JPanel(new BorderLayout(8, 0));
        ipPortPanel.setOpaque(false);
        txtServerIp = new JTextField("localhost", 18);
        txtServerIp.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        portPanel.setOpaque(false);
        JLabel lblPort = new JLabel("Port:");
        lblPort.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPort = new JTextField("3050", 5);
        txtPort.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        portPanel.add(lblPort);
        portPanel.add(txtPort);

        ipPortPanel.add(txtServerIp, BorderLayout.CENTER);
        ipPortPanel.add(portPanel, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        connGrid.add(ipPortPanel, gbc);
        gbc.gridwidth = 1;

        // Row 1: File CSDL
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblDb = new JLabel("File CSDL (.FDB):");
        lblDb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        connGrid.add(lblDb, gbc);

        txtDatabasePath = new JTextField("D:/THUCTAP/QLBH_Clone/data/DEMO.FDB", 20);
        txtDatabasePath.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        connGrid.add(txtDatabasePath, gbc);

        JPanel pathButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pathButtons.setOpaque(false);
        JButton btnBrowse = new JButton("Chọn file...");
        btnBrowse.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnBrowse.setIcon(IconHelper.getIcon("Folder.png", 14, 14));
        btnBrowse.addActionListener(e -> browseFile());

        JButton btnDefaultPath = new JButton("Mặc định");
        btnDefaultPath.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDefaultPath.addActionListener(e -> resetDefaultPath());

        pathButtons.add(btnBrowse);
        pathButtons.add(btnDefaultPath);
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
        connGrid.add(pathButtons, gbc);

        connBox.add(connGrid);
        mainContent.add(connBox);
        mainContent.add(Box.createVerticalStrut(10));

        // Khung 3: Tài khoản xác thực
        JPanel authBox = createSectionPanel("3. Tài khoản xác thực Firebird");
        JPanel authGrid = new JPanel(new GridBagLayout());
        authGrid.setOpaque(false);
        GridBagConstraints gbcA = new GridBagConstraints();
        gbcA.fill = GridBagConstraints.HORIZONTAL;
        gbcA.insets = new Insets(4, 4, 4, 4);

        gbcA.gridx = 0; gbcA.gridy = 0; gbcA.weightx = 0;
        JLabel lblUser = new JLabel("Tài khoản:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        authGrid.add(lblUser, gbcA);

        txtUsername = new JTextField("SYSDBA", 12);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbcA.gridx = 1; gbcA.gridy = 0; gbcA.weightx = 0.5;
        authGrid.add(txtUsername, gbcA);

        gbcA.gridx = 2; gbcA.gridy = 0; gbcA.weightx = 0;
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        authGrid.add(lblPass, gbcA);

        JPanel passPanel = new JPanel(new BorderLayout(4, 0));
        passPanel.setOpaque(false);
        txtPassword = new JPasswordField("masterkey", 10);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword = new JCheckBox("Hiện");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkShowPassword.setOpaque(false);
        chkShowPassword.addItemListener(e -> {
            txtPassword.setEchoChar(e.getStateChange() == ItemEvent.SELECTED ? (char) 0 : '•');
        });
        passPanel.add(txtPassword, BorderLayout.CENTER);
        passPanel.add(chkShowPassword, BorderLayout.EAST);

        gbcA.gridx = 3; gbcA.gridy = 0; gbcA.weightx = 0.5;
        authGrid.add(passPanel, gbcA);

        authBox.add(authGrid);
        mainContent.add(authBox);
        mainContent.add(Box.createVerticalStrut(10));

        // Khung 4: Trạng thái kiểm tra
        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        statusPanel.setBackground(new Color(238, 242, 248));
        statusPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 222, 238), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        lblStatusIcon = new JLabel(IconHelper.getIcon("Refresh.png", 16, 16));
        lblStatusText = new JLabel("Nhấn 'Kiểm tra kết nối' để kiểm tra đường truyền tới CSDL.");
        lblStatusText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusText.setForeground(new Color(80, 90, 105));
        statusPanel.add(lblStatusIcon);
        statusPanel.add(lblStatusText);
        mainContent.add(statusPanel);

        add(mainContent, BorderLayout.CENTER);

        // 3. Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(235, 240, 248));
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));
        bottomPanel.setPreferredSize(new Dimension(560, 48));

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        leftBottom.setOpaque(false);
        JButton btnRestoreAllDefaults = new JButton("Khôi phục mặc định");
        btnRestoreAllDefaults.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRestoreAllDefaults.setBackground(Color.WHITE);
        btnRestoreAllDefaults.addActionListener(e -> restoreAllDefaults());
        leftBottom.add(btnRestoreAllDefaults);
        bottomPanel.add(leftBottom, BorderLayout.WEST);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightBottom.setOpaque(false);

        JButton btnTest = new JButton("Kiểm tra kết nối");
        btnTest.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTest.setIcon(IconHelper.getIcon("Refresh.png", 16, 16));
        btnTest.setBackground(Color.WHITE);

        JButton btnSave = new JButton("Lưu & Áp dụng");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(25, 135, 84)); // Green
        btnSave.setFocusPainted(false);

        JButton btnCancel = new JButton("Đóng");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setBackground(Color.WHITE);

        btnTest.addActionListener(e -> onTestConnection());
        btnSave.addActionListener(e -> onSaveConfig());
        btnCancel.addActionListener(e -> dispose());

        rightBottom.add(btnTest);
        rightBottom.add(btnSave);
        rightBottom.add(btnCancel);
        bottomPanel.add(rightBottom, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Radio Action Listeners
        rbLocal.addActionListener(e -> {
            txtServerIp.setText("localhost");
            txtPort.setText("3050");
        });
        rbServer.addActionListener(e -> {
            if ("localhost".equalsIgnoreCase(txtServerIp.getText().trim())) {
                txtServerIp.setText("192.168.1.");
                txtServerIp.requestFocus();
            }
        });
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(215, 225, 238), 1, true),
                new EmptyBorder(8, 12, 10, 12)
        ));

        JLabel lblSecTitle = new JLabel(title);
        lblSecTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSecTitle.setForeground(new Color(30, 70, 130));
        lblSecTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblSecTitle);
        panel.add(Box.createVerticalStrut(6));
        return panel;
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser("D:/THUCTAP/QLBH_Clone/data");
        chooser.setDialogTitle("Chọn file Cơ sở dữ liệu Firebird (.FDB)");
        chooser.setFileFilter(new FileNameExtensionFilter("Firebird Database (*.fdb, *.gdb)", "fdb", "gdb", "db"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDatabasePath.setText(chooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
        }
    }

    private void resetDefaultPath() {
        File defaultFile = new File("data/DEMO.FDB");
        if (defaultFile.exists()) {
            txtDatabasePath.setText(defaultFile.getAbsolutePath().replace("\\", "/"));
        } else {
            txtDatabasePath.setText("D:/THUCTAP/QLBH_Clone/data/DEMO.FDB");
        }
    }

    private void restoreAllDefaults() {
        rbLocal.setSelected(true);
        txtServerIp.setText("localhost");
        txtPort.setText("3050");
        resetDefaultPath();
        txtUsername.setText("SYSDBA");
        txtPassword.setText("masterkey");
        setStatusIdle();
    }

    private void loadCurrentConfig() {
        DatabaseConfig cfg = DatabaseConnection.getConfig();
        txtServerIp.setText(cfg.getServerIp());
        txtPort.setText(String.valueOf(cfg.getPort()));
        txtDatabasePath.setText(cfg.getDatabasePath());
        txtUsername.setText(cfg.getUsername());
        txtPassword.setText(cfg.getPassword());

        if ("localhost".equalsIgnoreCase(cfg.getServerIp()) || "127.0.0.1".equals(cfg.getServerIp())) {
            rbLocal.setSelected(true);
        } else {
            rbServer.setSelected(true);
        }
        setStatusIdle();
    }

    private void setStatusIdle() {
        statusPanel.setBackground(new Color(238, 242, 248));
        lblStatusText.setText("Nhấn 'Kiểm tra kết nối' để thử kết nối tới máy chủ.");
        lblStatusText.setForeground(new Color(80, 90, 105));
    }

    private void onTestConnection() {
        String ip = txtServerIp.getText().trim();
        String portStr = txtPort.getText().trim();
        String dbPath = txtDatabasePath.getText().trim();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (ip.isEmpty() || portStr.isEmpty() || dbPath.isEmpty()) {
            statusPanel.setBackground(new Color(254, 237, 237));
            lblStatusText.setText("Vui lòng nhập đầy đủ IP, Port và Đường dẫn file CSDL!");
            lblStatusText.setForeground(new Color(200, 30, 30));
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            statusPanel.setBackground(new Color(254, 237, 237));
            lblStatusText.setText("Cổng (Port) phải là số nguyên hợp lệ (mặc định 3050)!");
            lblStatusText.setForeground(new Color(200, 30, 30));
            return;
        }

        lblStatusText.setText("Đang kiểm tra kết nối tới máy chủ...");
        lblStatusText.setForeground(new Color(30, 70, 140));

        SwingUtilities.invokeLater(() -> {
            boolean ok = DatabaseConnection.testConnection(ip, port, dbPath, user, pass);
            if (ok) {
                statusPanel.setBackground(new Color(235, 250, 240));
                lblStatusText.setText("Kết nối tới Cơ sở dữ liệu Firebird THÀNH CÔNG!");
                lblStatusText.setForeground(new Color(20, 130, 60));
                JOptionPane.showMessageDialog(this,
                        "Kết nối tới Cơ sở dữ liệu Firebird thành công!\n" +
                        "Máy chủ: " + ip + ":" + port + "\n" +
                        "File CSDL: " + dbPath,
                        "Kiểm tra kết nối", JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusPanel.setBackground(new Color(254, 237, 237));
                lblStatusText.setText("Không thể kết nối! Vui lòng kiểm tra lại IP/Port/File CSDL.");
                lblStatusText.setForeground(new Color(190, 20, 20));
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối tới Cơ sở dữ liệu Firebird!\n\n" +
                        "Nguyên nhân có thể do:\n" +
                        "1. Dịch vụ Firebird Server chưa được khởi động trên máy chủ.\n" +
                        "2. Địa chỉ IP hoặc Cổng (3050) chưa chính xác hoặc bị Tường lửa (Firewall) chặn.\n" +
                        "3. Đường dẫn file .FDB không tồn tại trên máy chủ.",
                        "Lỗi kết nối CSDL", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void onSaveConfig() {
        String ip = txtServerIp.getText().trim();
        String portStr = txtPort.getText().trim();
        String dbPath = txtDatabasePath.getText().trim();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (ip.isEmpty() || portStr.isEmpty() || dbPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin kết nối!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cổng (Port) phải là số nguyên (mặc định 3050)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseConfig cfg = DatabaseConnection.getConfig();
        cfg.setServerIp(ip);
        cfg.setPort(port);
        cfg.setDatabasePath(dbPath);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.saveConfig();
        DatabaseConnection.setConfig(cfg);

        JOptionPane.showMessageDialog(this, "Đã lưu và áp dụng cấu hình CSDL thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
