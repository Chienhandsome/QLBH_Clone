package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.KhoHang;
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

public class KhoHangEditDialog extends JDialog {
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    private KhoHang currentKhoHang;
    private final List<KhoHang> warehouseList;
    private int currentIndex;
    private final Runnable onDataChanged;

    private JTextField txtName;
    private JComboBox<String> cbAvatar;
    private JComboBox<CuaHang> cbCuaHang;
    private JCheckBox chkChoPhepAmKho;
    private JTextArea txtGhiChu;

    public KhoHangEditDialog(Dialog parent, KhoHang khoHang, List<KhoHang> warehouseList, Runnable onDataChanged) {
        super(parent, khoHang != null && khoHang.getId() != null ? "KHO HÀNG - SỬA" : "KHO HÀNG - THÊM MỚI", true);
        this.currentKhoHang = khoHang;
        this.warehouseList = warehouseList;
        this.currentIndex = (warehouseList != null && khoHang != null) ? warehouseList.indexOf(khoHang) : -1;
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

        // 1. Header Panel: Nền vàng kem + Icon Nhà / Kho hàng + Tiêu đề (Khớp 100% Ảnh 2)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        headerPanel.setBackground(new Color(254, 249, 231));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 215, 180)));

        JLabel lblHouseIcon = new JLabel(getWarehouseIcon(30, 30));
        JLabel lblHeaderTitle = new JLabel("Kho hàng");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(new Color(40, 40, 40));

        headerPanel.add(lblHouseIcon);
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

        // Row 1: Tên kho hàng & Ảnh
        JPanel rowName = new JPanel(new BorderLayout(8, 0));
        rowName.setOpaque(false);
        rowName.setMaximumSize(new Dimension(460, 28));

        JLabel lblName = new JLabel("Tên kho hàng");
        lblName.setPreferredSize(new Dimension(95, 24));
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

        String[] avatarKeys = {"warehouse", "red_house", "purple_house", "lightbulb", "Folder.png", "Home.png"};
        cbAvatar = new JComboBox<>(avatarKeys);
        cbAvatar.setPreferredSize(new Dimension(65, 24));
        cbAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbAvatar.setBackground(Color.WHITE);
        cbAvatar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setText("");
                if ("warehouse".equals(value)) {
                    lbl.setIcon(getWarehouseIcon(16, 16));
                } else if ("red_house".equals(value)) {
                    lbl.setIcon(getStoreColoredIcon(new Color(220, 50, 50), 16, 16));
                } else if ("purple_house".equals(value)) {
                    lbl.setIcon(getStoreColoredIcon(new Color(110, 80, 180), 16, 16));
                } else if ("lightbulb".equals(value)) {
                    lbl.setIcon(getLightbulbIcon(16, 16));
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

        // Row 2: Cửa hàng (Màu vàng nhạt #FFFEC8 theo Ảnh 2)
        JPanel rowCuaHang = new JPanel(new BorderLayout(8, 0));
        rowCuaHang.setOpaque(false);
        rowCuaHang.setMaximumSize(new Dimension(460, 28));

        JLabel lblCuaHang = new JLabel("Cửa hàng");
        lblCuaHang.setPreferredSize(new Dimension(95, 24));
        lblCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowCuaHang.add(lblCuaHang, BorderLayout.WEST);

        cbCuaHang = new JComboBox<>();
        cbCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCuaHang.setBackground(new Color(255, 254, 200)); // Vàng nhạt chuẩn Ảnh 2
        cbCuaHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CuaHang) {
                    CuaHang ch = (CuaHang) value;
                    lbl.setText(ch.getName());
                    lbl.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                }
                return lbl;
            }
        });
        cbCuaHang.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                javax.swing.plaf.basic.BasicComboPopup popup = new javax.swing.plaf.basic.BasicComboPopup(comboBox) {
                    @Override
                    protected void configurePopup() {
                        super.configurePopup();
                        setBorder(new LineBorder(new Color(180, 200, 225), 1));
                    }
                };

                JPanel bottomToolbar = new JPanel(new GridLayout(1, 3, 2, 0));
                bottomToolbar.setBackground(new Color(245, 248, 252));
                bottomToolbar.setBorder(new CompoundBorder(
                        new MatteBorder(1, 0, 0, 0, new Color(200, 215, 235)),
                        new EmptyBorder(2, 3, 2, 3)
                ));

                JButton btnThem = createPopupToolButton("Thêm", "plus.png", new Color(0, 120, 50));
                JButton btnTai = createPopupToolButton("Tải", "Refresh.png", new Color(0, 80, 180));
                JButton btnDanhMuc = createPopupToolButton("Danh mục", null, new Color(40, 40, 40));

                btnThem.addActionListener(e -> {
                    popup.hide();
                    CuaHang newCH = new CuaHang();
                    CuaHangEditDialog dialog = new CuaHangEditDialog(KhoHangEditDialog.this, newCH, null, () -> {
                        loadCuaHangList();
                        for (int i = 0; i < cbCuaHang.getItemCount(); i++) {
                            if (cbCuaHang.getItemAt(i).getId().equals(newCH.getId())) {
                                cbCuaHang.setSelectedIndex(i);
                                break;
                            }
                        }
                    });
                    dialog.setVisible(true);
                });

                btnTai.addActionListener(e -> {
                    loadCuaHangList();
                });

                btnDanhMuc.addActionListener(e -> {
                    popup.hide();
                    CuaHangDialog dialog = new CuaHangDialog(KhoHangEditDialog.this);
                    dialog.setVisible(true);
                    loadCuaHangList();
                });

                bottomToolbar.add(btnThem);
                bottomToolbar.add(btnTai);
                bottomToolbar.add(btnDanhMuc);

                popup.add(bottomToolbar, BorderLayout.SOUTH);
                return popup;
            }
        });

        loadCuaHangList();
        rowCuaHang.add(cbCuaHang, BorderLayout.CENTER);

        formPanel.add(rowCuaHang);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 3: Checkbox Cho phép âm kho (Khớp 100% Ảnh 2)
        JPanel rowAmKho = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rowAmKho.setOpaque(false);
        rowAmKho.setMaximumSize(new Dimension(460, 24));

        JLabel lblSpacer = new JLabel("");
        lblSpacer.setPreferredSize(new Dimension(103, 20));
        rowAmKho.add(lblSpacer);

        chkChoPhepAmKho = new JCheckBox("Cho phép âm kho");
        chkChoPhepAmKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepAmKho.setOpaque(false);
        rowAmKho.add(chkChoPhepAmKho);

        formPanel.add(rowAmKho);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 4: Ghi chú
        JPanel rowGhiChu = new JPanel(new BorderLayout(8, 0));
        rowGhiChu.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setPreferredSize(new Dimension(95, 24));
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
            if (saveCurrentWarehouse()) {
                JOptionPane.showMessageDialog(this, "Đã lưu thông tin kho hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSaveNew.addActionListener(e -> {
            if (saveCurrentWarehouse()) {
                clearFormForNew();
            }
        });

        btnSaveExit.addActionListener(e -> {
            if (saveCurrentWarehouse()) {
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

    private JButton createPopupToolButton(String text, String iconName, Color fgColor) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(fgColor != null ? fgColor : Color.BLACK);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 220, 240), 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadCuaHangList() {
        try {
            CuaHang prevSelected = (CuaHang) cbCuaHang.getSelectedItem();
            cbCuaHang.removeAllItems();
            List<CuaHang> stores = cuaHangDAO.getAllActive();
            if (stores != null && !stores.isEmpty()) {
                CuaHang toSelect = null;
                for (CuaHang ch : stores) {
                    if (!ch.isFolder() && !ch.isSeparator()) {
                        cbCuaHang.addItem(ch);
                        if (prevSelected != null && ch.getId().equals(prevSelected.getId())) {
                            toSelect = ch;
                        } else if (toSelect == null && currentKhoHang != null && ch.getId().equals(currentKhoHang.getCuaHangId())) {
                            toSelect = ch;
                        }
                    }
                }
                if (toSelect != null) {
                    cbCuaHang.setSelectedItem(toSelect);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Icon getWarehouseIcon(int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = width;
                int h = height;

                // Brown Roof
                g2.setColor(new Color(180, 80, 40));
                int[] rx = {x + w / 2, x + w - 2, x + 2};
                int[] ry = {y + 2, y + h / 2, y + h / 2};
                g2.fillPolygon(rx, ry, 3);

                // Body
                g2.setColor(new Color(245, 240, 235));
                g2.fillRect(x + 4, y + h / 2, w - 8, h / 2 - 2);
                g2.setColor(new Color(140, 130, 120));
                g2.drawRect(x + 4, y + h / 2, w - 8, h / 2 - 2);

                // Door
                g2.setColor(new Color(190, 100, 50));
                g2.fillRect(x + w / 2 - 3, y + h / 2 + 3, 6, h / 2 - 5);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    public static Icon getStoreColoredIcon(Color roofColor, int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = width;
                int h = height;

                // Colored roof
                g2.setColor(roofColor);
                int[] rx = {x + w / 2, x + w - 1, x + 1};
                int[] ry = {y + 2, y + h / 2 - 1, y + h / 2 - 1};
                g2.fillPolygon(rx, ry, 3);

                // House body
                g2.setColor(new Color(240, 245, 255));
                g2.fillRect(x + 3, y + h / 2 - 1, w - 6, h / 2);
                g2.setColor(new Color(130, 140, 160));
                g2.drawRect(x + 3, y + h / 2 - 1, w - 6, h / 2);

                // Door
                g2.setColor(roofColor.darker());
                g2.fillRect(x + w / 2 - 2, y + h / 2 + 2, 5, h / 2 - 3);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    public static Icon getLightbulbIcon(int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = width;
                int h = height;

                // Bulb
                g2.setColor(new Color(255, 210, 20));
                g2.fillOval(x + 3, y + 1, w - 6, (int) (h * 0.65));
                g2.setColor(new Color(210, 160, 0));
                g2.drawOval(x + 3, y + 1, w - 6, (int) (h * 0.65));

                // Base
                g2.setColor(new Color(150, 150, 150));
                g2.fillRect(x + w / 2 - 3, y + (int) (h * 0.65) - 1, 6, 5);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    private void loadDataToForm() {
        if (currentKhoHang != null) {
            txtName.setText(currentKhoHang.getName() != null ? currentKhoHang.getName() : "");
            chkChoPhepAmKho.setSelected(currentKhoHang.isChoPhepAmKho());
            txtGhiChu.setText(currentKhoHang.getNote() != null ? currentKhoHang.getNote() : "");

            if (currentKhoHang.getAvatarIcon() != null) {
                cbAvatar.setSelectedItem(currentKhoHang.getAvatarIcon());
            }

            if (currentKhoHang.getCuaHangId() != null) {
                for (int i = 0; i < cbCuaHang.getItemCount(); i++) {
                    CuaHang ch = cbCuaHang.getItemAt(i);
                    if (currentKhoHang.getCuaHangId().equals(ch.getId())) {
                        cbCuaHang.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private boolean saveCurrentWarehouse() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên kho hàng!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        if (currentKhoHang == null) {
            currentKhoHang = new KhoHang();
        }

        currentKhoHang.setName(name);
        currentKhoHang.setChoPhepAmKho(chkChoPhepAmKho.isSelected());
        currentKhoHang.setNote(txtGhiChu.getText().trim());

        if (cbCuaHang.getSelectedItem() instanceof CuaHang) {
            currentKhoHang.setCuaHangId(((CuaHang) cbCuaHang.getSelectedItem()).getId());
        }

        if (cbAvatar.getSelectedItem() != null) {
            currentKhoHang.setAvatarIcon(cbAvatar.getSelectedItem().toString());
        }

        boolean ok;
        if (currentKhoHang.getId() == null || currentKhoHang.getId().isEmpty()) {
            ok = khoHangDAO.insert(currentKhoHang);
        } else {
            ok = khoHangDAO.update(currentKhoHang);
        }

        if (ok && onDataChanged != null) {
            onDataChanged.run();
        }
        return ok;
    }

    private void clearFormForNew() {
        currentKhoHang = new KhoHang();
        txtName.setText("");
        chkChoPhepAmKho.setSelected(false);
        txtGhiChu.setText("");
        txtName.requestFocus();
        setTitle("KHO HÀNG - THÊM MỚI");
    }

    private void navigatePrev() {
        if (warehouseList != null && !warehouseList.isEmpty() && currentIndex > 0) {
            currentIndex--;
            currentKhoHang = warehouseList.get(currentIndex);
            loadDataToForm();
            setTitle("KHO HÀNG - SỬA");
        }
    }

    private void navigateNext() {
        if (warehouseList != null && !warehouseList.isEmpty() && currentIndex < warehouseList.size() - 1) {
            currentIndex++;
            currentKhoHang = warehouseList.get(currentIndex);
            loadDataToForm();
            setTitle("KHO HÀNG - SỬA");
        }
    }
}
