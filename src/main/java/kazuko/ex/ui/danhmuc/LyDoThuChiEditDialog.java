package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.model.danhmuc.LyDoThuChi;
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

public class LyDoThuChiEditDialog extends JDialog {
    private final LyDoThuChiDAO lyDoThuChiDAO = new LyDoThuChiDAO();

    private LyDoThuChi currentLyDoThuChi;
    private final List<LyDoThuChi> reasonList;
    private int currentIndex;
    private final Runnable onDataChanged;

    private JTextField txtName;
    private JComboBox<String> cbAvatar;
    private JCheckBox chkLaLyDoThu;
    private JCheckBox chkLaLyDoChi;
    private JComboBox<String> cbLoai;
    private JTextArea txtGhiChu;

    public LyDoThuChiEditDialog(Dialog parent, LyDoThuChi lyDoThuChi, List<LyDoThuChi> reasonList, Runnable onDataChanged) {
        super(parent, lyDoThuChi != null && lyDoThuChi.getId() != null ? "LÝ DO THU CHI - SỬA" : "LÝ DO THU CHI - THÊM", true);
        this.currentLyDoThuChi = lyDoThuChi;
        this.reasonList = reasonList;
        this.currentIndex = (reasonList != null && lyDoThuChi != null) ? reasonList.indexOf(lyDoThuChi) : -1;
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

        // 1. Header Panel: Nền vàng kem + Icon Tiền tệ + Tiêu đề (Khớp 100% Ảnh 2)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        headerPanel.setBackground(new Color(254, 249, 231));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 215, 180)));

        JLabel lblMoneyIcon = new JLabel(getMoneyIcon(28, 28));
        JLabel lblHeaderTitle = new JLabel("Lý do thu chi");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(new Color(40, 40, 40));

        headerPanel.add(lblMoneyIcon);
        headerPanel.add(lblHeaderTitle);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel: Toolbar con + Form nhập liệu
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        // Toolbar con: Phím tắt ▾, Trước (F10), Sau (F11), Tạo mới
        JPanel subToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        subToolBar.setBackground(new Color(218, 230, 245));
        subToolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnShortcuts = createSmallButton("Phím tắt ▾", "shortcut.png");
        JButton btnPrev = createSmallButton("Trước (F10)", "navigation-180-frame.png");
        JButton btnNext = createSmallButton("Sau (F11)", "navigation-000-frame.png");
        JButton btnNew = createSmallButton("Tạo mới", "new.png");

        btnPrev.addActionListener(e -> navigatePrev());
        btnNext.addActionListener(e -> navigateNext());
        btnNew.addActionListener(e -> clearFormForNew());

        subToolBar.add(btnShortcuts);
        subToolBar.add(btnPrev);
        subToolBar.add(btnNext);
        subToolBar.add(btnNew);
        centerPanel.add(subToolBar, BorderLayout.NORTH);

        // Form fields Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Row 1: Tên lý do & Ảnh
        JPanel rowName = new JPanel(new BorderLayout(8, 0));
        rowName.setOpaque(false);
        rowName.setMaximumSize(new Dimension(460, 28));

        JLabel lblName = new JLabel("Tên lý do");
        lblName.setPreferredSize(new Dimension(85, 24));
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

        String[] avatarKeys = {"money_dollar", "user_female_1", "user_female_5", "receipt_red", "Folder.png", "1282529873_objects.png"};
        cbAvatar = new JComboBox<>(avatarKeys);
        cbAvatar.setPreferredSize(new Dimension(65, 24));
        cbAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbAvatar.setBackground(Color.WHITE);
        cbAvatar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setText("");
                if ("money_dollar".equals(value)) {
                    lbl.setIcon(getMoneyIcon(16, 16));
                } else if ("user_female_1".equals(value) || "user_female_5".equals(value)) {
                    lbl.setIcon(NhanVienEditDialog.getAvatarIcon(value.toString(), 16, 16));
                } else if ("receipt_red".equals(value)) {
                    lbl.setIcon(getReceiptRedIcon(16, 16));
                } else if (value != null) {
                    lbl.setIcon(IconHelper.getIcon(value.toString(), 16, 16));
                }
                return lbl;
            }
        });
        pnlAvatar.add(cbAvatar);
        rowName.add(pnlAvatar, BorderLayout.EAST);

        formPanel.add(rowName);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 2: Checkboxes Là lý do thu, Là lý do chi (Khớp 100% Ảnh 2)
        JPanel rowCheckboxes = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        rowCheckboxes.setOpaque(false);
        rowCheckboxes.setMaximumSize(new Dimension(460, 26));

        JLabel lblSpacer = new JLabel("");
        lblSpacer.setPreferredSize(new Dimension(75, 20));
        rowCheckboxes.add(lblSpacer);

        chkLaLyDoThu = new JCheckBox("Là lý do thu");
        chkLaLyDoChi = new JCheckBox("Là lý do chi");
        chkLaLyDoThu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkLaLyDoChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkLaLyDoThu.setOpaque(false);
        chkLaLyDoChi.setOpaque(false);
        chkLaLyDoThu.setSelected(true);

        rowCheckboxes.add(chkLaLyDoThu);
        rowCheckboxes.add(chkLaLyDoChi);
        formPanel.add(rowCheckboxes);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 3: Loại
        JPanel rowLoai = new JPanel(new BorderLayout(8, 0));
        rowLoai.setOpaque(false);
        rowLoai.setMaximumSize(new Dimension(460, 28));

        JLabel lblLoai = new JLabel("Loại:");
        lblLoai.setPreferredSize(new Dimension(85, 24));
        lblLoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowLoai.add(lblLoai, BorderLayout.WEST);

        cbLoai = new JComboBox<>(new String[]{"", "Tiền mặt", "Ngân hàng", "Công nợ", "Nội bộ", "Khác"});
        cbLoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLoai.setBackground(Color.WHITE);
        rowLoai.add(cbLoai, BorderLayout.CENTER);

        formPanel.add(rowLoai);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 4: Ghi chú
        JPanel rowGhiChu = new JPanel(new BorderLayout(8, 0));
        rowGhiChu.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setPreferredSize(new Dimension(85, 24));
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

        centerPanel.add(formPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons Panel: Lưu, Lưu & Mới, Lưu & thoát, Thoát
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottomPanel.setBackground(new Color(218, 230, 245));
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(180, 200, 225)));

        JButton btnSave = createActionButton("Lưu", "Save1.png");
        JButton btnSaveNew = createActionButton("Lưu & Mới", null);
        JButton btnSaveExit = createActionButton("Lưu & thoát", null);
        JButton btnExit = createActionButton("Thoát", null);

        btnSave.addActionListener(e -> {
            if (saveCurrentReason()) {
                JOptionPane.showMessageDialog(this, "Đã lưu lý do thu chi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSaveNew.addActionListener(e -> {
            if (saveCurrentReason()) {
                clearFormForNew();
            }
        });

        btnSaveExit.addActionListener(e -> {
            if (saveCurrentReason()) {
                dispose();
            }
        });

        btnExit.addActionListener(e -> dispose());

        bottomPanel.add(btnSave);
        bottomPanel.add(btnSaveNew);
        bottomPanel.add(btnSaveExit);
        bottomPanel.add(btnExit);
        add(bottomPanel, BorderLayout.SOUTH);

        // Phím nóng F10 (Trước), F11 (Sau), ESC (Thoát)
        getRootPane().registerKeyboardAction(e -> navigatePrev(), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigateNext(), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
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

    private JButton createSmallButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setPreferredSize(new Dimension(text.length() > 8 ? 98 : 78, 26));
        return btn;
    }

    public static Icon getMoneyIcon(int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = width;
                int h = height;

                // Cyan house/arrow shape
                g2.setColor(new Color(0, 160, 210));
                int[] xPoints = {x + w / 2, x + w - 2, x + w - 4, x + w - 4, x + 4, x + 4, x + 2};
                int[] yPoints = {y + 2, y + h / 2 - 2, y + h / 2 - 2, y + h - 2, y + h - 2, y + h / 2 - 2, y + h / 2 - 2};
                g2.fillPolygon(xPoints, yPoints, 7);

                // Circle Coin in center
                int coinD = w / 2;
                int coinX = x + (w - coinD) / 2;
                int coinY = y + (h - coinD) / 2 + 1;
                g2.setColor(new Color(245, 235, 190));
                g2.fillOval(coinX, coinY, coinD, coinD);
                g2.setColor(new Color(170, 140, 70));
                g2.drawOval(coinX, coinY, coinD, coinD);

                // Dollar sign '$'
                g2.setColor(new Color(90, 70, 20));
                g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (height * 0.4)));
                FontMetrics fm = g2.getFontMetrics();
                int textX = coinX + (coinD - fm.stringWidth("$")) / 2;
                int textY = coinY + (coinD - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("$", textX, textY);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    public static Icon getReceiptRedIcon(int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = width;
                int h = height;

                g2.setColor(new Color(235, 70, 70));
                g2.fillRoundRect(x + 1, y + 2, w - 2, h - 4, 3, 3);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (height * 0.4)));
                g2.drawString("THU", x + 2, y + h - 5);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    private void loadDataToForm() {
        if (currentLyDoThuChi != null) {
            txtName.setText(currentLyDoThuChi.getName() != null ? currentLyDoThuChi.getName() : "");
            chkLaLyDoThu.setSelected(currentLyDoThuChi.isLaLyDoThu());
            chkLaLyDoChi.setSelected(currentLyDoThuChi.isLaLyDoChi());
            txtGhiChu.setText(currentLyDoThuChi.getGhiChu() != null ? currentLyDoThuChi.getGhiChu() : "");

            if (currentLyDoThuChi.getLoai() != null && !currentLyDoThuChi.getLoai().isEmpty()) {
                cbLoai.setSelectedItem(currentLyDoThuChi.getLoai());
            }

            if (currentLyDoThuChi.getAvatarIcon() != null) {
                cbAvatar.setSelectedItem(currentLyDoThuChi.getAvatarIcon());
            }
        }
    }

    private boolean saveCurrentReason() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên lý do!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        if (currentLyDoThuChi == null) {
            currentLyDoThuChi = new LyDoThuChi();
        }

        currentLyDoThuChi.setName(name);
        currentLyDoThuChi.setLaLyDoThu(chkLaLyDoThu.isSelected());
        currentLyDoThuChi.setLaLyDoChi(chkLaLyDoChi.isSelected());
        currentLyDoThuChi.setLoai(cbLoai.getSelectedItem() != null ? cbLoai.getSelectedItem().toString() : "");
        currentLyDoThuChi.setGhiChu(txtGhiChu.getText().trim());

        if (cbAvatar.getSelectedItem() != null) {
            currentLyDoThuChi.setAvatarIcon(cbAvatar.getSelectedItem().toString());
        }

        boolean ok;
        if (currentLyDoThuChi.getId() == null || currentLyDoThuChi.getId().isEmpty()) {
            ok = lyDoThuChiDAO.insert(currentLyDoThuChi);
        } else {
            ok = lyDoThuChiDAO.update(currentLyDoThuChi);
        }

        if (ok && onDataChanged != null) {
            onDataChanged.run();
        }
        return ok;
    }

    private void clearFormForNew() {
        currentLyDoThuChi = new LyDoThuChi();
        txtName.setText("");
        chkLaLyDoThu.setSelected(true);
        chkLaLyDoChi.setSelected(false);
        cbLoai.setSelectedIndex(0);
        txtGhiChu.setText("");
        txtName.requestFocus();
        setTitle("LÝ DO THU CHI - THÊM");
    }

    private void navigatePrev() {
        if (reasonList != null && !reasonList.isEmpty() && currentIndex > 0) {
            currentIndex--;
            currentLyDoThuChi = reasonList.get(currentIndex);
            loadDataToForm();
            setTitle("LÝ DO THU CHI - SỬA");
        }
    }

    private void navigateNext() {
        if (reasonList != null && !reasonList.isEmpty() && currentIndex < reasonList.size() - 1) {
            currentIndex++;
            currentLyDoThuChi = reasonList.get(currentIndex);
            loadDataToForm();
            setTitle("LÝ DO THU CHI - SỬA");
        }
    }
}
