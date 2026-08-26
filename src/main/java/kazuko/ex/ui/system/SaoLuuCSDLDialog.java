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
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaoLuuCSDLDialog extends JDialog {
    private JTextField txtCurrentDb;
    private JTextField txtBackupFile;

    public SaoLuuCSDLDialog(Window owner) {
        super(owner, "Sao lưu CSDL", ModalityType.APPLICATION_MODAL);
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

        JLabel lblTitle = new JLabel("Sao lưu cơ sở dữ liệu");
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

        // Note: Blue italic
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel lblNote = new JLabel("Hệ thống sẽ sao lưu toàn bộ dữ liệu hiện tại ra một file dự phòng an toàn.");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(new Color(30, 80, 150));
        centerPanel.add(lblNote, gbc);

        // Row 1: CSDL hiện tại
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel lblCur = new JLabel("CSDL hiện tại:");
        lblCur.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        centerPanel.add(lblCur, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        txtCurrentDb = new JTextField();
        txtCurrentDb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCurrentDb.setEditable(false);
        txtCurrentDb.setBackground(new Color(245, 245, 245));
        txtCurrentDb.setPreferredSize(new Dimension(240, 24));
        txtCurrentDb.setBorder(new LineBorder(new Color(170, 195, 225)));
        DatabaseConfig config = DatabaseConnection.getConfig();
        String currentPath = config != null && config.getDatabasePath() != null ? config.getDatabasePath() : "D:/THUCTAP/QLBH_Clone/data/DEMO.FDB";
        txtCurrentDb.setText(currentPath);
        centerPanel.add(txtCurrentDb, gbc);

        // Row 2: File sao lưu
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel lblBackup = new JLabel("File sao lưu:");
        lblBackup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        centerPanel.add(lblBackup, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtBackupFile = new JTextField();
        txtBackupFile.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBackupFile.setPreferredSize(new Dimension(240, 24));
        txtBackupFile.setBorder(new LineBorder(new Color(170, 195, 225)));

        String timeStr = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(new Date());
        File currentFile = new File(currentPath);
        String dir = currentFile.getParent() != null ? currentFile.getParent().replace('\\', '/') : "D:/THUCTAP/QLBH_Clone/data";
        txtBackupFile.setText(dir + "/BACKUP_" + timeStr + ".FBK");
        centerPanel.add(txtBackupFile, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton btnBrowse = createBrowseButton();
        btnBrowse.addActionListener(e -> onBrowseBackupDest());
        centerPanel.add(btnBrowse, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Footer Buttons Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(new Color(218, 232, 248));

        JButton btnExecute = new JButton("Thực hiện");
        btnExecute.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExecute.setPreferredSize(new Dimension(85, 26));
        btnExecute.addActionListener(e -> onExecuteBackup());
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

    private void onBrowseBackupDest() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn vị trí lưu file sao lưu CSDL");
        chooser.setFileFilter(new FileNameExtensionFilter("File sao lưu Firebird (*.fbk, *.bak, *.fdb)", "fbk", "bak", "fdb"));
        if (!txtBackupFile.getText().trim().isEmpty()) {
            chooser.setSelectedFile(new File(txtBackupFile.getText().trim()));
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath().replace('\\', '/');
            if (!path.toLowerCase().endsWith(".fbk") && !path.toLowerCase().endsWith(".bak") && !path.toLowerCase().endsWith(".fdb")) {
                path += ".FBK";
            }
            txtBackupFile.setText(path);
        }
    }

    private void onExecuteBackup() {
        String srcPath = txtCurrentDb.getText().trim();
        String destPath = txtBackupFile.getText().trim();

        if (srcPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy đường dẫn CSDL nguồn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File srcFile = new File(srcPath);
        if (!srcFile.exists() || !srcFile.isFile()) {
            JOptionPane.showMessageDialog(this, "File CSDL nguồn không tồn tại: " + srcPath, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (destPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn File sao lưu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            File destFile = new File(destPath);
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Copy an toàn
            try (InputStream in = new FileInputStream(srcFile);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Sao lưu cơ sở dữ liệu thành công!\n\nFile lưu tại: " + destPath,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi trong quá trình sao lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
