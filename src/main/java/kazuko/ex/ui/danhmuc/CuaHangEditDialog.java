package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.util.AppColors;
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

public class CuaHangEditDialog extends JDialog {
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();

    private CuaHang currentCuaHang;
    private final List<CuaHang> storeList;
    private int currentIndex;
    private final Runnable onDataChanged;

    private JTextField txtCode;
    private JComboBox<String> cbIcon;
    private JTextField txtName;
    private JTextField txtDiaChi;
    private JTextField txtDienThoai;
    private JComboBox<KhoHang> cbKhoHang;
    private JComboBox<String> cbMauHoaDon;
    private JTextArea txtGhiChu;

    public CuaHangEditDialog(Dialog parent, CuaHang cuaHang, List<CuaHang> storeList, Runnable onDataChanged) {
        super(parent, cuaHang != null && cuaHang.getId() != null ? "CỬA HÀNG - SỬA" : "CỬA HÀNG - THÊM", true);
        this.currentCuaHang = cuaHang;
        this.storeList = storeList;
        this.currentIndex = (storeList != null && cuaHang != null) ? storeList.indexOf(cuaHang) : -1;
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

        // 1. Header Panel: Nền vàng kem + Icon 1282529873_objects.png
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        headerPanel.setBackground(new Color(254, 249, 231));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 215, 180)));

        JLabel lblShopIcon = new JLabel(IconHelper.getIcon("1282529873_objects.png", 28, 28));
        JLabel lblHeaderTitle = new JLabel("Cửa hàng");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(new Color(40, 40, 40));

        headerPanel.add(lblShopIcon);
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

        // Form Fields Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Row 0: Mã cửa hàng & Ảnh icon
        JLabel lblCode = new JLabel("Mã cửa hàng");
        lblCode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(lblCode, gbc);

        JPanel codeIconPanel = new JPanel(new BorderLayout(8, 0));
        codeIconPanel.setOpaque(false);

        txtCode = createStyledTextField();
        txtCode.setPreferredSize(new Dimension(150, 24));
        codeIconPanel.add(txtCode, BorderLayout.CENTER);

        JPanel iconBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        iconBox.setOpaque(false);
        iconBox.add(new JLabel("Ảnh:"));
        cbIcon = new JComboBox<>(new String[]{"Chi nhánh", "Trụ sở chính", "Thư mục", "Cửa hàng"});
        cbIcon.setPreferredSize(new Dimension(120, 24));
        cbIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbIcon.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if ("Chi nhánh".equals(value) || "Cửa hàng".equals(value)) {
                    label.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                } else if ("Trụ sở chính".equals(value)) {
                    label.setIcon(IconHelper.getIcon("globe-network.png", 16, 16));
                } else if ("Thư mục".equals(value)) {
                    label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                }
                return label;
            }
        });
        iconBox.add(cbIcon);
        codeIconPanel.add(iconBox, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(codeIconPanel, gbc);

        // Row 1: Tên cửa hàng
        JLabel lblName = new JLabel("Tên cửa hàng");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblName, gbc);

        txtName = createStyledTextField();
        txtName.setBackground(new Color(238, 248, 255));
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtName, gbc);

        // Row 2: Địa chỉ
        JLabel lblDiaChi = new JLabel("Địa chỉ");
        lblDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblDiaChi, gbc);

        txtDiaChi = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtDiaChi, gbc);

        // Row 3: Điện thoại
        JLabel lblPhone = new JLabel("Điện thoại");
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblPhone, gbc);

        txtDienThoai = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(txtDienThoai, gbc);

        // Row 4: Kho mặc định
        JLabel lblKho = new JLabel("Kho mặc định");
        lblKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(lblKho, gbc);

        cbKhoHang = new JComboBox<>();
        cbKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoHang.setBackground(Color.WHITE);
        loadKhoHangList();
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(cbKhoHang, gbc);

        // Row 5: Mẫu hóa đơn
        JLabel lblMauHD = new JLabel("Mẫu hóa đơn");
        lblMauHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(lblMauHD, gbc);

        cbMauHoaDon = new JComboBox<>(new String[]{
                "Mẫu 54 mm x 2 dòng",
                "Mẫu 58 mm",
                "Mẫu 80 mm (K80)",
                "Mẫu A4 chuẩn",
                "Mẫu A5 chuẩn"
        });
        cbMauHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbMauHoaDon.setBackground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 5;
        formPanel.add(cbMauHoaDon, gbc);

        // Row 6: Ghi chú
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(lblGhiChu, gbc);

        txtGhiChu = new JTextArea(3, 24);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        JScrollPane spGhiChu = new JScrollPane(txtGhiChu);
        spGhiChu.setBorder(new CompoundBorder(
                new LineBorder(new Color(150, 180, 220), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        gbc.gridx = 1; gbc.gridy = 6; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(spGhiChu, gbc);

        centerPanel.add(formPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons Panel: Lưu, Lưu & Mới, Lưu & Thoát, Thoát
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottomPanel.setBackground(new Color(218, 230, 245));
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(180, 200, 225)));

        JButton btnSave = createActionButton("Lưu", "Save1.png");
        JButton btnSaveNew = createActionButton("Lưu & Mới", null);
        JButton btnSaveExit = createActionButton("Lưu & thoát", null);
        JButton btnExit = createActionButton("Thoát", null);

        btnSave.addActionListener(e -> {
            if (saveCurrentStore()) {
                JOptionPane.showMessageDialog(this, "Đã lưu thông tin cửa hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSaveNew.addActionListener(e -> {
            if (saveCurrentStore()) {
                clearFormForNew();
            }
        });

        btnSaveExit.addActionListener(e -> {
            if (saveCurrentStore()) {
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
        JTextField tf = new JTextField(22);
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

    private void loadKhoHangList() {
        try {
            List<KhoHang> khoList = khoHangDAO.getAll();
            if (khoList != null && !khoList.isEmpty()) {
                for (KhoHang k : khoList) {
                    cbKhoHang.addItem(k);
                }
            } else {
                KhoHang defaultKho = new KhoHang();
                defaultKho.setId("1");
                defaultKho.setName("KHO BÁN HÀNG");
                cbKhoHang.addItem(defaultKho);
            }
        } catch (Exception e) {
            KhoHang defaultKho = new KhoHang();
            defaultKho.setId("1");
            defaultKho.setName("KHO BÁN HÀNG");
            cbKhoHang.addItem(defaultKho);
        }
    }

    private void loadDataToForm() {
        if (currentCuaHang != null) {
            txtCode.setText(currentCuaHang.getCode() != null ? currentCuaHang.getCode() : "");
            txtName.setText(currentCuaHang.getName() != null ? currentCuaHang.getName() : "");
            txtDiaChi.setText(currentCuaHang.getDiaChi() != null ? currentCuaHang.getDiaChi() : "");
            txtDienThoai.setText(currentCuaHang.getDienThoai() != null ? currentCuaHang.getDienThoai() : "");
            txtGhiChu.setText(currentCuaHang.getGhiChu() != null ? currentCuaHang.getGhiChu() : "");

            if ("headquarter".equalsIgnoreCase(currentCuaHang.getIconName())) {
                cbIcon.setSelectedIndex(1);
            } else if ("folder".equalsIgnoreCase(currentCuaHang.getIconName())) {
                cbIcon.setSelectedIndex(2);
            } else {
                cbIcon.setSelectedIndex(0);
            }

            if (currentCuaHang.getMauHoaDon() != null) {
                for (int i = 0; i < cbMauHoaDon.getItemCount(); i++) {
                    if (cbMauHoaDon.getItemAt(i).contains(currentCuaHang.getMauHoaDon())) {
                        cbMauHoaDon.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private boolean saveCurrentStore() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên cửa hàng!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        if (currentCuaHang == null) {
            currentCuaHang = new CuaHang();
        }

        currentCuaHang.setCode(txtCode.getText().trim());
        currentCuaHang.setName(name);
        currentCuaHang.setDiaChi(txtDiaChi.getText().trim());
        currentCuaHang.setDienThoai(txtDienThoai.getText().trim());
        currentCuaHang.setGhiChu(txtGhiChu.getText().trim());

        if (cbKhoHang.getSelectedItem() instanceof KhoHang) {
            KhoHang selectedKho = (KhoHang) cbKhoHang.getSelectedItem();
            currentCuaHang.setKhoHangId(selectedKho.getId());
            currentCuaHang.setKhoHangName(selectedKho.getName());
        }

        if (cbMauHoaDon.getSelectedItem() != null) {
            currentCuaHang.setMauHoaDon(cbMauHoaDon.getSelectedItem().toString());
        }

        int iconIdx = cbIcon.getSelectedIndex();
        if (iconIdx == 1) currentCuaHang.setIconName("headquarter");
        else if (iconIdx == 2) currentCuaHang.setIconName("folder");
        else currentCuaHang.setIconName("branch");

        boolean ok;
        if (currentCuaHang.getId() == null || currentCuaHang.getId().isEmpty()) {
            ok = cuaHangDAO.insert(currentCuaHang);
        } else {
            ok = cuaHangDAO.update(currentCuaHang);
        }

        if (ok && onDataChanged != null) {
            onDataChanged.run();
        }
        return ok;
    }

    private void clearFormForNew() {
        currentCuaHang = new CuaHang();
        txtCode.setText("CH" + (System.currentTimeMillis() % 1000));
        txtName.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtGhiChu.setText("");
        txtName.requestFocus();
        setTitle("CỬA HÀNG - THÊM");
    }

    private void navigatePrev() {
        if (storeList != null && !storeList.isEmpty() && currentIndex > 0) {
            currentIndex--;
            currentCuaHang = storeList.get(currentIndex);
            loadDataToForm();
            setTitle("CỬA HÀNG - SỬA");
        }
    }

    private void navigateNext() {
        if (storeList != null && !storeList.isEmpty() && currentIndex < storeList.size() - 1) {
            currentIndex++;
            currentCuaHang = storeList.get(currentIndex);
            loadDataToForm();
            setTitle("CỬA HÀNG - SỬA");
        }
    }
}
