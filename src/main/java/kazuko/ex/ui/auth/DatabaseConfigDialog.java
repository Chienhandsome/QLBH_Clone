package kazuko.ex.ui.auth;

import kazuko.ex.config.DatabaseConfig;
import kazuko.ex.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class DatabaseConfigDialog extends JDialog {
    private JTextField txtServerIp;
    private JTextField txtPort;
    private JTextField txtDatabasePath;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JRadioButton rbLocal;
    private JRadioButton rbServer;

    public DatabaseConfigDialog(Frame parent) {
        super(parent, "Cấu hình Cơ sở dữ liệu", true);
        initComponents();
        loadCurrentConfig();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Chế độ kết nối
        JLabel lblMode = new JLabel("Chế độ kết nối:");
        lblMode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(lblMode, gbc);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rbLocal = new JRadioButton("Cục bộ (1 máy)");
        rbServer = new JRadioButton("Máy chủ (Nhiều máy / Online)");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbLocal);
        bg.add(rbServer);
        modePanel.add(rbLocal);
        modePanel.add(rbServer);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(modePanel, gbc);
        gbc.gridwidth = 1;

        // Server IP
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Địa chỉ Máy chủ (IP):"), gbc);
        txtServerIp = new JTextField("localhost", 22);
        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(txtServerIp, gbc);

        // Port
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Cổng (Port):"), gbc);
        txtPort = new JTextField("3050", 22);
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(txtPort, gbc);

        // Đường dẫn file FDB
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Đường dẫn dữ liệu (.FDB):"), gbc);
        txtDatabasePath = new JTextField("D:/THUCTAP/QLBH_Clone/data/DEMO.FDB", 22);
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(txtDatabasePath, gbc);

        JButton btnBrowse = new JButton("...");
        btnBrowse.addActionListener(e -> browseFile());
        gbc.gridx = 2; gbc.gridy = 3;
        mainPanel.add(btnBrowse, gbc);

        // Tài khoản CSDL
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Tài khoản CSDL:"), gbc);
        txtUsername = new JTextField("SYSDBA", 22);
        gbc.gridx = 1; gbc.gridy = 4;
        mainPanel.add(txtUsername, gbc);

        // Mật khẩu CSDL
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Mật khẩu CSDL:"), gbc);
        txtPassword = new JPasswordField("masterkey", 22);
        gbc.gridx = 1; gbc.gridy = 5;
        mainPanel.add(txtPassword, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Nút bấm phía dưới
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnTest = new JButton("Kiểm tra kết nối");
        JButton btnSave = new JButton("Lưu cấu hình");
        JButton btnCancel = new JButton("Đóng");

        btnTest.addActionListener(e -> onTestConnection());
        btnSave.addActionListener(e -> onSaveConfig());
        btnCancel.addActionListener(e -> dispose());

        bottomPanel.add(btnTest);
        bottomPanel.add(btnSave);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        rbLocal.addActionListener(e -> txtServerIp.setText("localhost"));
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser("D:/THUCTAP/QLBH_Clone/data");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDatabasePath.setText(chooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
        }
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
    }

    private void onTestConnection() {
        String ip = txtServerIp.getText().trim();
        int port = Integer.parseInt(txtPort.getText().trim());
        String dbPath = txtDatabasePath.getText().trim();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        boolean ok = DatabaseConnection.testConnection(ip, port, dbPath, user, pass);
        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Kết nối tới CSDL thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Không thể kết nối tới CSDL! Vui lòng kiểm tra lại IP/Port/File CSDL.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSaveConfig() {
        DatabaseConfig cfg = DatabaseConnection.getConfig();
        cfg.setServerIp(txtServerIp.getText().trim());
        cfg.setPort(Integer.parseInt(txtPort.getText().trim()));
        cfg.setDatabasePath(txtDatabasePath.getText().trim());
        cfg.setUsername(txtUsername.getText().trim());
        cfg.setPassword(new String(txtPassword.getPassword()));
        cfg.saveConfig();
        DatabaseConnection.setConfig(cfg);

        JOptionPane.showMessageDialog(this, "Đã lưu cấu hình CSDL!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
