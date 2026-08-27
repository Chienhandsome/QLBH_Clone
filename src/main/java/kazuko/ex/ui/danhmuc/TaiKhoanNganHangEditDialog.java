package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.TaiKhoanNganHangDAO;
import kazuko.ex.model.danhmuc.TaiKhoanNganHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class TaiKhoanNganHangEditDialog extends JDialog {
    private final TaiKhoanNganHangDAO taiKhoanNganHangDAO = new TaiKhoanNganHangDAO();

    private TaiKhoanNganHang currentAccount;
    private final Runnable onDataChanged;

    private JTextField txtName;
    private JComboBox<String> cbAvatar;
    private JTextArea txtGhiChu;

    public TaiKhoanNganHangEditDialog(Dialog parent, TaiKhoanNganHang account, List<TaiKhoanNganHang> list, Runnable onDataChanged) {
        super(parent, "Tài khoản ngân hàng", true);
        this.currentAccount = account;
        this.onDataChanged = onDataChanged;

        initComponents();
        loadDataToForm();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(235, 242, 250));

        // 1. Header Panel: Nền trắng + Icon Comment Pencil + Tiêu đề (Khớp 100% Ảnh 2)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 235)));

        JLabel lblBubbleIcon = new JLabel(getChatPencilIcon(28, 28));
        String titleText = (currentAccount != null && currentAccount.getId() != null) ? "Chỉnh sửa tài khoản ngân hàng" : "Thêm mới tài khoản ngân hàng";
        JLabel lblHeaderTitle = new JLabel(titleText);
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderTitle.setForeground(new Color(30, 30, 30));

        headerPanel.add(lblBubbleIcon);
        headerPanel.add(lblHeaderTitle);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel: Form nhập liệu
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Row 1: Tên tài khoản ngân hàng & Ảnh
        JPanel rowName = new JPanel(new BorderLayout(8, 0));
        rowName.setOpaque(false);
        rowName.setMaximumSize(new Dimension(460, 28));

        JLabel lblName = new JLabel("Tên tài khoản ngân h...");
        lblName.setPreferredSize(new Dimension(140, 24));
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowName.add(lblName, BorderLayout.WEST);

        txtName = createStyledTextField();
        txtName.setBackground(new Color(238, 248, 255));
        rowName.add(txtName, BorderLayout.CENTER);

        JPanel pnlAvatar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlAvatar.setOpaque(false);
        JLabel lblAnh = new JLabel("Ảnh:");
        lblAnh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlAvatar.add(lblAnh);

        String[] avatarKeys = {"database1.png", "Folder.png", "1282529873_objects.png"};
        cbAvatar = new JComboBox<>(avatarKeys);
        cbAvatar.setPreferredSize(new Dimension(65, 24));
        cbAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbAvatar.setBackground(Color.WHITE);
        cbAvatar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setText("");
                if (value != null) {
                    lbl.setIcon(IconHelper.getIcon(value.toString(), 16, 16));
                }
                return lbl;
            }
        });
        pnlAvatar.add(cbAvatar);
        rowName.add(pnlAvatar, BorderLayout.EAST);

        formPanel.add(rowName);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 2: Ghi chú
        JPanel rowGhiChu = new JPanel(new BorderLayout(8, 0));
        rowGhiChu.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setPreferredSize(new Dimension(140, 24));
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowGhiChu.add(lblGhiChu, BorderLayout.WEST);

        txtGhiChu = new JTextArea(4, 24);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        JScrollPane spGhiChu = new JScrollPane(txtGhiChu);
        spGhiChu.setBorder(new CompoundBorder(
                new LineBorder(new Color(150, 180, 220), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        rowGhiChu.add(spGhiChu, BorderLayout.CENTER);

        formPanel.add(rowGhiChu);
        add(formPanel, BorderLayout.CENTER);

        // 3. Bottom Buttons Panel: Ghi dữ liệu, Thoát (Khớp 100% Ảnh 2)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomPanel.setBackground(new Color(218, 230, 245));
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(180, 200, 225)));

        JButton btnSave = new JButton("Ghi dữ liệu");
        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSave.setFocusPainted(false);
        btnSave.setBackground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(95, 26));

        JButton btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setFocusPainted(false);
        btnExit.setBackground(Color.WHITE);
        btnExit.setPreferredSize(new Dimension(75, 26));

        btnSave.addActionListener(e -> {
            if (saveCurrentAccount()) {
                dispose();
            }
        });

        btnExit.addActionListener(e -> dispose());

        bottomPanel.add(btnSave);
        bottomPanel.add(btnExit);
        add(bottomPanel, BorderLayout.SOUTH);

        // Phím nóng ESC
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(150, 180, 220), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBackground(new Color(235, 248, 255));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBackground(Color.WHITE);
            }
        });
        return tf;
    }

    public static Icon getChatPencilIcon(int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = width;
                int h = height;

                // Speech bubble
                g2.setColor(new Color(225, 235, 250));
                g2.fillOval(x + 1, y + 1, w - 6, h - 8);
                g2.setColor(new Color(100, 130, 190));
                g2.drawOval(x + 1, y + 1, w - 6, h - 8);

                // Small pencil across bubble
                g2.setColor(new Color(235, 90, 80)); // Red pencil
                g2.fillRoundRect(x + w / 2 - 2, y + 4, 10, 4, 2, 2);
                g2.setColor(new Color(90, 200, 120)); // Green eraser/top
                g2.fillOval(x + w / 2 + 6, y + 3, 5, 5);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    private void loadDataToForm() {
        if (currentAccount != null) {
            txtName.setText(currentAccount.getName() != null ? currentAccount.getName() : "");
            txtGhiChu.setText(currentAccount.getGhiChu() != null ? currentAccount.getGhiChu() : "");
            if (currentAccount.getAvatarIcon() != null) {
                cbAvatar.setSelectedItem(currentAccount.getAvatarIcon());
            }
        }
    }

    private boolean saveCurrentAccount() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên tài khoản ngân hàng!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        if (currentAccount == null) {
            currentAccount = new TaiKhoanNganHang();
        }

        currentAccount.setName(name);
        currentAccount.setGhiChu(txtGhiChu.getText().trim());

        if (cbAvatar.getSelectedItem() != null) {
            currentAccount.setAvatarIcon(cbAvatar.getSelectedItem().toString());
        }

        boolean ok;
        if (currentAccount.getId() == null || currentAccount.getId().isEmpty()) {
            ok = taiKhoanNganHangDAO.insert(currentAccount);
        } else {
            ok = taiKhoanNganHangDAO.update(currentAccount);
        }

        if (ok && onDataChanged != null) {
            onDataChanged.run();
        }
        return ok;
    }
}
