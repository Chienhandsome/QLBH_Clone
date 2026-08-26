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

public class TaoMoiCSDLDialog extends JDialog {
    private JTextField txtNewDbFile;
    private JCheckBox chkSwitchToNew;

    public TaoMoiCSDLDialog(Window owner) {
        super(owner, "Tạo mới CSDL", ModalityType.APPLICATION_MODAL);
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

        JLabel lblTitle = new JLabel("Tạo mới cơ sở dữ liệu");
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
        JLabel lblNote = new JLabel("Hệ thống sẽ tạo một cơ sở dữ liệu mới sạch từ mẫu (Template) chuẩn.");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(new Color(30, 80, 150));
        centerPanel.add(lblNote, gbc);

        // Row 1: File CSDL mới
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel lblNew = new JLabel("File CSDL mới (.FDB):");
        lblNew.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        centerPanel.add(lblNew, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNewDbFile = new JTextField("D:/THUCTAP/QLBH_Clone/data/DATA_NEW.FDB");
        txtNewDbFile.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNewDbFile.setPreferredSize(new Dimension(240, 24));
        txtNewDbFile.setBorder(new LineBorder(new Color(170, 195, 225)));
        centerPanel.add(txtNewDbFile, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton btnBrowse = createBrowseButton();
        btnBrowse.addActionListener(e -> onBrowseNewDbPath());
        centerPanel.add(btnBrowse, gbc);

        // Row 2: Checkbox switch to new DB
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        chkSwitchToNew = new JCheckBox("Sử dụng CSDL mới này ngay sau khi tạo xong");
        chkSwitchToNew.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSwitchToNew.setOpaque(false);
        chkSwitchToNew.setSelected(true);
        centerPanel.add(chkSwitchToNew, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Footer Buttons Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(new Color(218, 232, 248));

        JButton btnExecute = new JButton("Thực hiện");
        btnExecute.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExecute.setPreferredSize(new Dimension(85, 26));
        btnExecute.addActionListener(e -> onExecuteCreate());
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

    private void onBrowseNewDbPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn vị trí lưu file CSDL mới (.FDB)");
        chooser.setFileFilter(new FileNameExtensionFilter("Cơ sở dữ liệu Firebird (*.fdb)", "fdb"));
        if (!txtNewDbFile.getText().trim().isEmpty()) {
            chooser.setSelectedFile(new File(txtNewDbFile.getText().trim()));
        } else {
            chooser.setCurrentDirectory(new File("D:/THUCTAP/QLBH_Clone/data"));
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath().replace('\\', '/');
            if (!path.toLowerCase().endsWith(".fdb")) {
                path += ".FDB";
            }
            txtNewDbFile.setText(path);
        }
    }

    private void onExecuteCreate() {
        String destPath = txtNewDbFile.getText().trim();
        if (destPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đường dẫn file CSDL mới!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File destFile = new File(destPath);
        if (destFile.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    this,
                    "File CSDL đã tồn tại:\n" + destPath + "\n\nBạn có muốn ghi đè lên file này không?",
                    "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) return;
        }

        // Tìm file template
        File templateFile = new File("D:/THUCTAP/QLBH_Clone/data/TEMPLATE.FDB");
        if (!templateFile.exists()) {
            templateFile = new File("D:/THUCTAP/v5.0/Data/TEMPLATE.FDB");
        }
        if (!templateFile.exists()) {
            templateFile = new File("D:/THUCTAP/QLBH_Clone/data/DEMO.FDB");
        }

        if (!templateFile.exists()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy file mẫu CSDL (TEMPLATE.FDB) để khởi tạo!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Copy template
            try (InputStream in = new FileInputStream(templateFile);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }

            if (chkSwitchToNew.isSelected()) {
                DatabaseConnection.closeConnection();
                DatabaseConfig config = DatabaseConnection.getConfig();
                if (config != null) {
                    config.setDatabasePath(destPath);
                    config.saveConfig();
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Tạo mới cơ sở dữ liệu thành công!\n\nĐường dẫn: " + destPath,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo mới CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
