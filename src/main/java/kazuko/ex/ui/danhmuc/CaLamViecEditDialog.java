package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.CaLamViecDAO;
import kazuko.ex.model.danhmuc.CaLamViec;
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
import java.text.DecimalFormat;
import java.util.List;

public class CaLamViecEditDialog extends JDialog {
    private final CaLamViecDAO caLamViecDAO = new CaLamViecDAO();

    private CaLamViec currentCaLamViec;
    private final List<CaLamViec> shiftList;
    private int currentIndex;
    private final Runnable onDataChanged;

    private JTextField txtName;
    private JComboBox<String> cbAvatar;
    private JTextField txtTiLeLuong;
    private JTextArea txtGhiChu;

    public CaLamViecEditDialog(Dialog parent, CaLamViec caLamViec, List<CaLamViec> shiftList, Runnable onDataChanged) {
        super(parent, caLamViec != null && caLamViec.getId() != null ? "CA LÀM VIỆC - SỬA" : "CA LÀM VIỆC - THÊM MỚI", true);
        this.currentCaLamViec = caLamViec;
        this.shiftList = shiftList;
        this.currentIndex = (shiftList != null && caLamViec != null) ? shiftList.indexOf(caLamViec) : -1;
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

        // 1. Header Panel: Nền vàng kem + Icon Donut Chart + Tiêu đề
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        headerPanel.setBackground(new Color(254, 249, 231));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 215, 180)));

        JLabel lblDonutIcon = new JLabel(getDonutChartIcon(28, 28));
        JLabel lblHeaderTitle = new JLabel("Ca làm việc");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(new Color(40, 40, 40));

        headerPanel.add(lblDonutIcon);
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

        // Row 1: Tên ca làm việc & Ảnh
        JPanel rowName = new JPanel(new BorderLayout(8, 0));
        rowName.setOpaque(false);
        rowName.setMaximumSize(new Dimension(460, 28));

        JLabel lblName = new JLabel("Tên ca làm việc");
        lblName.setPreferredSize(new Dimension(100, 24));
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

        String[] avatarKeys = {"chart_donut", "Folder.png", "1282529873_objects.png"};
        cbAvatar = new JComboBox<>(avatarKeys);
        cbAvatar.setPreferredSize(new Dimension(65, 24));
        cbAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbAvatar.setBackground(Color.WHITE);
        cbAvatar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setText("");
                if ("chart_donut".equals(value)) {
                    lbl.setIcon(getDonutChartIcon(16, 16));
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

        // Row 2: Tỉ lệ lương
        JPanel rowTiLe = new JPanel(new BorderLayout(8, 0));
        rowTiLe.setOpaque(false);
        rowTiLe.setMaximumSize(new Dimension(460, 28));

        JLabel lblTiLe = new JLabel("Tỉ lệ lương");
        lblTiLe.setPreferredSize(new Dimension(100, 24));
        lblTiLe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowTiLe.add(lblTiLe, BorderLayout.WEST);

        JPanel pnlTiLeInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTiLeInput.setOpaque(false);

        txtTiLeLuong = createStyledTextField();
        txtTiLeLuong.setPreferredSize(new Dimension(90, 24));
        txtTiLeLuong.setText("100");
        txtTiLeLuong.setHorizontalAlignment(JTextField.LEFT);

        JLabel lblPercent = new JLabel("%");
        lblPercent.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        pnlTiLeInput.add(txtTiLeLuong);
        pnlTiLeInput.add(lblPercent);
        rowTiLe.add(pnlTiLeInput, BorderLayout.CENTER);

        formPanel.add(rowTiLe);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 3: Ghi chú
        JPanel rowGhiChu = new JPanel(new BorderLayout(8, 0));
        rowGhiChu.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setPreferredSize(new Dimension(100, 24));
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
            if (saveCurrentShift()) {
                JOptionPane.showMessageDialog(this, "Đã lưu ca làm việc thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSaveNew.addActionListener(e -> {
            if (saveCurrentShift()) {
                clearFormForNew();
            }
        });

        btnSaveExit.addActionListener(e -> {
            if (saveCurrentShift()) {
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

    public static Icon getDonutChartIcon(int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = Math.min(width, height) - 2;
                int ox = x + (width - d) / 2;
                int oy = y + (height - d) / 2;

                // Segments
                g2.setColor(new Color(40, 175, 215)); // Cyan / Blue arc
                g2.fillArc(ox, oy, d, d, 45, 180);

                g2.setColor(new Color(215, 210, 190)); // Light cream / beige arc
                g2.fillArc(ox, oy, d, d, 225, 180);

                // Inner hole
                int holeD = d / 2;
                int holeX = ox + (d - holeD) / 2;
                int holeY = oy + (d - holeD) / 2;
                g2.setColor(c != null && c.getBackground() != null ? c.getBackground() : new Color(254, 249, 231));
                g2.fillOval(holeX, holeY, holeD, holeD);

                // Thin border
                g2.setColor(new Color(160, 160, 160));
                g2.drawOval(ox, oy, d, d);
                g2.drawOval(holeX, holeY, holeD, holeD);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    private void loadDataToForm() {
        if (currentCaLamViec != null) {
            txtName.setText(currentCaLamViec.getName() != null ? currentCaLamViec.getName() : "");
            txtGhiChu.setText(currentCaLamViec.getGhiChu() != null ? currentCaLamViec.getGhiChu() : "");
            DecimalFormat df = new DecimalFormat("#,##0.##");
            txtTiLeLuong.setText(df.format(currentCaLamViec.getTiLeLuong()));
            if (currentCaLamViec.getAvatarIcon() != null) {
                cbAvatar.setSelectedItem(currentCaLamViec.getAvatarIcon());
            }
        }
    }

    private boolean saveCurrentShift() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên ca làm việc!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        if (currentCaLamViec == null) {
            currentCaLamViec = new CaLamViec();
        }

        currentCaLamViec.setName(name);
        currentCaLamViec.setGhiChu(txtGhiChu.getText().trim());

        try {
            String rateStr = txtTiLeLuong.getText().replace(",", "").replace(" ", "").trim();
            currentCaLamViec.setTiLeLuong(rateStr.isEmpty() ? 100.0 : Double.parseDouble(rateStr));
        } catch (Exception ignored) {
            currentCaLamViec.setTiLeLuong(100.0);
        }

        if (cbAvatar.getSelectedItem() != null) {
            currentCaLamViec.setAvatarIcon(cbAvatar.getSelectedItem().toString());
        }

        boolean ok;
        if (currentCaLamViec.getId() == null || currentCaLamViec.getId().isEmpty()) {
            ok = caLamViecDAO.insert(currentCaLamViec);
        } else {
            ok = caLamViecDAO.update(currentCaLamViec);
        }

        if (ok && onDataChanged != null) {
            onDataChanged.run();
        }
        return ok;
    }

    private void clearFormForNew() {
        currentCaLamViec = new CaLamViec();
        txtName.setText("");
        txtTiLeLuong.setText("100");
        txtGhiChu.setText("");
        txtName.requestFocus();
        setTitle("CA LÀM VIỆC - THÊM MỚI");
    }

    private void navigatePrev() {
        if (shiftList != null && !shiftList.isEmpty() && currentIndex > 0) {
            currentIndex--;
            currentCaLamViec = shiftList.get(currentIndex);
            loadDataToForm();
            setTitle("CA LÀM VIỆC - SỬA");
        }
    }

    private void navigateNext() {
        if (shiftList != null && !shiftList.isEmpty() && currentIndex < shiftList.size() - 1) {
            currentIndex++;
            currentCaLamViec = shiftList.get(currentIndex);
            loadDataToForm();
            setTitle("CA LÀM VIỆC - SỬA");
        }
    }
}
