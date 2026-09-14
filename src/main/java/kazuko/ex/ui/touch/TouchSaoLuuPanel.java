package kazuko.ex.ui.touch;

import kazuko.ex.config.DatabaseConfig;
import kazuko.ex.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Màn hình Sao lưu dữ liệu chuẩn cảm ứng POS theo phong cách TAP Shop TS. */
public final class TouchSaoLuuPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN_BTN = new Color(0, 119, 9);
    private static final Color RED_BTN = new Color(165, 10, 10);

    private final JTextField txtBackupPath = new JTextField();

    public TouchSaoLuuPanel(Runnable backAction) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(5, 5, 15, 5));

        // 1. Header Banner
        JLabel title = new JLabel("SAO LƯU DỮ LIỆU", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(HEADER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));
        title.setPreferredSize(new Dimension(0, 58));
        add(title, BorderLayout.NORTH);

        // 2. Body Container
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel note = new JLabel("CHÚ Ý: BẠN CHỈ CÓ THỂ SAO LƯU KHI PHẦN MỀM CHẠY TRÊN MÁY CHỦ");
        note.setForeground(Color.WHITE);
        note.setFont(new Font("Segoe UI", Font.BOLD, 17));
        body.add(note, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        JLabel lblPath = new JLabel("SAO LƯU TỚI");
        lblPath.setForeground(Color.WHITE);
        lblPath.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPath.setPreferredSize(new Dimension(170, 48));

        txtBackupPath.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtBackupPath.setPreferredSize(new Dimension(500, 48));
        txtBackupPath.setBackground(Color.WHITE);
        txtBackupPath.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 220, 235), 2, true),
                new EmptyBorder(4, 10, 4, 10)));

        String defaultName = "BACKUP_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".FBK";
        txtBackupPath.setText("D:/THUCTAP/QLBH_Clone/data/" + defaultName);

        JButton btnBrowse = createActionButton("CHỌN ĐƯỜNG DẪN", GREEN_BTN, 210, 48);
        btnBrowse.addActionListener(e -> onBrowseBackupPath());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(lblPath, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(txtBackupPath, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(btnBrowse, gbc);

        JPanel centerHolder = new JPanel(new BorderLayout());
        centerHolder.setOpaque(false);
        centerHolder.add(form, BorderLayout.NORTH);
        body.add(centerHolder, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        // 3. Bottom Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 0, 10, 20));

        JButton btnExecute = createActionButton("THỰC HIỆN", GREEN_BTN, 190, 52);
        btnExecute.addActionListener(e -> onExecuteBackup());

        JButton btnExit = createActionButton("THOÁT", RED_BTN, 190, 52);
        btnExit.addActionListener(e -> backAction.run());

        footer.add(btnExecute);
        footer.add(btnExit);
        add(footer, BorderLayout.SOUTH);
    }

    private void onBrowseBackupPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn vị trí lưu file sao lưu");
        chooser.setFileFilter(new FileNameExtensionFilter("File sao lưu Firebird (*.fbk, *.fdb)", "fbk", "fdb"));
        String current = txtBackupPath.getText().trim();
        if (!current.isEmpty()) {
            chooser.setSelectedFile(new File(current));
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath().replace('\\', '/');
            if (!path.toLowerCase().endsWith(".fbk") && !path.toLowerCase().endsWith(".fdb")) {
                path += ".FBK";
            }
            txtBackupPath.setText(path);
        }
    }

    private void onExecuteBackup() {
        String destPath = txtBackupPath.getText().trim();
        if (destPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đường dẫn file sao lưu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseConfig config = DatabaseConnection.getConfig();
        String currentDbPath = config != null && config.getDatabasePath() != null ? config.getDatabasePath() : "D:/THUCTAP/QLBH_Clone/data/DEMO.FDB";
        File srcFile = new File(currentDbPath);
        if (!srcFile.exists()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy file CSDL nguồn:\n" + currentDbPath, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File destFile = new File(destPath);
        if (destFile.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    this,
                    "File sao lưu đã tồn tại:\n" + destPath + "\n\nBạn có muốn ghi đè lên file này không?",
                    "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) return;
        }

        try {
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

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
                    "Sao lưu cơ sở dữ liệu thành công!\n\nFile sao lưu: " + destPath,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi sao lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createActionButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 235, 245), 2, true),
                new EmptyBorder(4, 10, 4, 10)));
        return button;
    }
}
