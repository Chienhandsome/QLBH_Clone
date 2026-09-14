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

/** Màn hình Phục hồi dữ liệu chuẩn cảm ứng POS theo phong cách TAP Shop TS. */
public final class TouchKhoiPhucPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN_BTN = new Color(0, 119, 9);
    private static final Color RED_BTN = new Color(165, 10, 10);

    private final JTextField txtSourceFile = new JTextField();
    private final JTextField txtTargetFile = new JTextField();

    public TouchKhoiPhucPanel(Runnable backAction) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(5, 5, 15, 5));

        // 1. Header Banner
        JLabel title = new JLabel("PHỤC HỒI DỮ LIỆU", SwingConstants.CENTER);
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

        JLabel note = new JLabel("CHÚ Ý: BẠN CHỈ CÓ THỂ PHỤC HỒI KHI PHẦN MỀM CHẠY TRÊN MÁY CHỦ");
        note.setForeground(Color.WHITE);
        note.setFont(new Font("Segoe UI", Font.BOLD, 17));
        body.add(note, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        // Row 1: File sao lưu
        JLabel lblSrc = new JLabel("FILE SAO LƯU");
        lblSrc.setForeground(Color.WHITE);
        lblSrc.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSrc.setPreferredSize(new Dimension(180, 48));

        styleField(txtSourceFile);
        JButton btnBrowseSrc = createActionButton("CHỌN FILE", GREEN_BTN, 190, 48);
        btnBrowseSrc.addActionListener(e -> onBrowseSource());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(lblSrc, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(txtSourceFile, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(btnBrowseSrc, gbc);

        // Row 2: File khôi phục
        JLabel lblDst = new JLabel("FILE KHÔI PHỤC");
        lblDst.setForeground(Color.WHITE);
        lblDst.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDst.setPreferredSize(new Dimension(180, 48));

        styleField(txtTargetFile);
        DatabaseConfig config = DatabaseConnection.getConfig();
        String currentDbPath = config != null && config.getDatabasePath() != null ? config.getDatabasePath() : "D:/THUCTAP/QLBH_Clone/data/DEMO.FDB";
        txtTargetFile.setText(currentDbPath);

        JButton btnBrowseDst = createActionButton("CHỌN FILE", GREEN_BTN, 190, 48);
        btnBrowseDst.addActionListener(e -> onBrowseTarget());

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(lblDst, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(txtTargetFile, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(btnBrowseDst, gbc);

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
        btnExecute.addActionListener(e -> onExecuteRestore());

        JButton btnExit = createActionButton("THOÁT", RED_BTN, 190, 52);
        btnExit.addActionListener(e -> backAction.run());

        footer.add(btnExecute);
        footer.add(btnExit);
        add(footer, BorderLayout.SOUTH);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        field.setPreferredSize(new Dimension(500, 48));
        field.setBackground(Color.WHITE);
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 220, 235), 2, true),
                new EmptyBorder(4, 10, 4, 10)));
    }

    private void onBrowseSource() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file sao lưu nguồn (.FBK, .FDB)");
        chooser.setFileFilter(new FileNameExtensionFilter("File sao lưu (*.fbk, *.fdb)", "fbk", "fdb"));
        chooser.setCurrentDirectory(new File("D:/THUCTAP/QLBH_Clone/data"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtSourceFile.setText(chooser.getSelectedFile().getAbsolutePath().replace('\\', '/'));
        }
    }

    private void onBrowseTarget() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file CSDL đích cần khôi phục ghi đè (.FDB)");
        chooser.setFileFilter(new FileNameExtensionFilter("Cơ sở dữ liệu Firebird (*.fdb)", "fdb"));
        String current = txtTargetFile.getText().trim();
        if (!current.isEmpty()) {
            chooser.setSelectedFile(new File(current));
        } else {
            chooser.setCurrentDirectory(new File("D:/THUCTAP/QLBH_Clone/data"));
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath().replace('\\', '/');
            if (!path.toLowerCase().endsWith(".fdb")) {
                path += ".FDB";
            }
            txtTargetFile.setText(path);
        }
    }

    private void onExecuteRestore() {
        String srcPath = txtSourceFile.getText().trim();
        String dstPath = txtTargetFile.getText().trim();

        if (srcPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file sao lưu nguồn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dstPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file CSDL đích!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            JOptionPane.showMessageDialog(this, "File sao lưu nguồn không tồn tại:\n" + srcPath, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "CẢNH BÁO: Quá trình phục hồi sẽ GHI ĐÈ toàn bộ dữ liệu hiện tại bằng dữ liệu từ file sao lưu:\n\n"
                        + "Nguồn: " + srcPath + "\n"
                        + "Đích: " + dstPath + "\n\n"
                        + "Bạn có chắc chắn muốn thực hiện không?",
                "Xác nhận phục hồi CSDL",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            DatabaseConnection.closeConnection();

            File dstFile = new File(dstPath);
            File parentDir = dstFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (InputStream in = new FileInputStream(srcFile);
                 OutputStream out = new FileOutputStream(dstFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }

            DatabaseConfig config = DatabaseConnection.getConfig();
            if (config != null) {
                config.setDatabasePath(dstPath);
                config.saveConfig();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Phục hồi cơ sở dữ liệu thành công!\n\nCSDL hiện tại đã được chuyển tới: " + dstPath,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi phục hồi CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
