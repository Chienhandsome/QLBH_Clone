package kazuko.ex.ui.system;

import kazuko.ex.config.DatabaseConfig;
import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class KhoiPhucCSDLDialog extends JDialog {
    private JTextField txtFileSaoLuu;
    private JTextField txtFileKhoiPhuc;

    public KhoiPhucCSDLDialog(Window owner) {
        super(owner, "Khôi phục CSDL", ModalityType.APPLICATION_MODAL);
        initUI();
        setSize(480, 270);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));
        headerPanel.setPreferredSize(new Dimension(480, 52));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("tsbDatabase.Image.png", 32, 32));
        if (lblIcon.getIcon() == null) {
            lblIcon.setIcon(IconHelper.getIcon("database1.png", 32, 32));
        }
        lblIcon.setBorder(new EmptyBorder(0, 15, 0, 0));

        JLabel lblTitle = new JLabel("Khôi phục cơ sở dữ liệu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 45, 80));

        headerPanel.add(lblIcon, BorderLayout.WEST);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(218, 232, 248));
        centerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Note: Red italic
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel lblNote = new JLabel("Chú ý: Bạn chỉ có thể khôi phục khi ứng dụng chạy trên máy chủ");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(new Color(200, 30, 30));
        centerPanel.add(lblNote, gbc);

        // Row 1: File sao lưu
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel lblSaoLuu = new JLabel("File sao lưu:");
        lblSaoLuu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        centerPanel.add(lblSaoLuu, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtFileSaoLuu = new JTextField();
        txtFileSaoLuu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFileSaoLuu.setPreferredSize(new Dimension(240, 24));
        txtFileSaoLuu.setBorder(new LineBorder(new Color(170, 195, 225)));
        centerPanel.add(txtFileSaoLuu, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton btnBrowseBackup = createBrowseButton();
        btnBrowseBackup.addActionListener(e -> onBrowseBackupFile());
        centerPanel.add(btnBrowseBackup, gbc);

        // Row 2: File khôi phục
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblKhoiPhuc = new JLabel("File khôi phục:");
        lblKhoiPhuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        centerPanel.add(lblKhoiPhuc, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtFileKhoiPhuc = new JTextField();
        txtFileKhoiPhuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFileKhoiPhuc.setPreferredSize(new Dimension(240, 24));
        txtFileKhoiPhuc.setBorder(new LineBorder(new Color(170, 195, 225)));
        DatabaseConfig config = DatabaseConnection.getConfig();
        if (config != null && config.getDatabasePath() != null) {
            txtFileKhoiPhuc.setText(config.getDatabasePath());
        } else {
            txtFileKhoiPhuc.setText("D:/THUCTAP/QLBH_Clone/data/DEMO.FDB");
        }
        centerPanel.add(txtFileKhoiPhuc, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton btnBrowseRestore = createBrowseButton();
        btnBrowseRestore.addActionListener(e -> onBrowseRestoreFile());
        centerPanel.add(btnBrowseRestore, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Footer Buttons Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(new Color(218, 232, 248));

        JButton btnExecute = new JButton("Thực hiện");
        btnExecute.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExecute.setPreferredSize(new Dimension(85, 26));
        btnExecute.addActionListener(e -> onExecuteRestore());
        footerPanel.add(btnExecute);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.setPreferredSize(new Dimension(75, 26));
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createBrowseButton() {
        JButton btn = new JButton();
        Icon icon = IconHelper.getIcon("Folder2.png", 16, 16);
        if (icon == null) icon = IconHelper.getIcon("btnOpen.Image.png", 16, 16);
        btn.setIcon(icon);
        btn.setPreferredSize(new Dimension(26, 24));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(170, 195, 225)));
        return btn;
    }

    private void onBrowseBackupFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file sao lưu CSDL");
        chooser.setFileFilter(new FileNameExtensionFilter("File sao lưu (*.fbk, *.bak, *.fdb, *.zip)", "fbk", "bak", "fdb", "zip"));
        if (!txtFileSaoLuu.getText().trim().isEmpty()) {
            chooser.setSelectedFile(new File(txtFileSaoLuu.getText().trim()));
        } else {
            chooser.setCurrentDirectory(new File("D:/THUCTAP/QLBH_Clone/data"));
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFileSaoLuu.setText(chooser.getSelectedFile().getAbsolutePath().replace('\\', '/'));
        }
    }

    private void onBrowseRestoreFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn vị trí file CSDL đích cần khôi phục (.FDB)");
        chooser.setFileFilter(new FileNameExtensionFilter("Cơ sở dữ liệu Firebird (*.fdb)", "fdb"));
        if (!txtFileKhoiPhuc.getText().trim().isEmpty()) {
            chooser.setSelectedFile(new File(txtFileKhoiPhuc.getText().trim()));
        } else {
            chooser.setCurrentDirectory(new File("D:/THUCTAP/QLBH_Clone/data"));
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath().replace('\\', '/');
            if (!path.toLowerCase().endsWith(".fdb")) {
                path += ".FDB";
            }
            txtFileKhoiPhuc.setText(path);
        }
    }

    private void onExecuteRestore() {
        String srcPath = txtFileSaoLuu.getText().trim();
        String destPath = txtFileKhoiPhuc.getText().trim();

        if (srcPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn File sao lưu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File srcFile = new File(srcPath);
        if (!srcFile.exists() || !srcFile.isFile()) {
            JOptionPane.showMessageDialog(this, "File sao lưu không tồn tại hoặc đường dẫn không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (destPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chỉ định File khôi phục (.FDB)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn khôi phục CSDL từ file sao lưu?\n\nFile nguồn: " + srcFile.getName() +
                        "\nFile đích: " + destPath +
                        "\n\nLưu ý: Dữ liệu hiện tại trong file đích sẽ bị ghi đè hoàn toàn!",
                "Xác nhận khôi phục CSDL",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Đóng kết nối CSDL hiện tại để giải phóng file
            DatabaseConnection.closeConnection();

            File destFile = new File(destPath);
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Copy file sao lưu sang file đích
            try (InputStream in = new FileInputStream(srcFile);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }

            // Cập nhật cấu hình CSDL nếu cần
            DatabaseConfig config = DatabaseConnection.getConfig();
            if (config != null) {
                config.setDatabasePath(destPath);
                config.saveConfig();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Khôi phục cơ sở dữ liệu thành công!\n\nĐường dẫn CSDL hiện tại: " + destPath,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi trong quá trình khôi phục: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
