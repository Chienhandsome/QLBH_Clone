package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.List;

public class NhanVienEditDialog extends JDialog {
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private NhanVien currentNhanVien;
    private final List<NhanVien> employeeList;
    private int currentIndex;
    private final Runnable onDataChanged;

    private JTextField txtName;
    private JComboBox<String> cbAvatar;
    private JTextField txtDiaChi;
    private JTextField txtDienThoai;

    private JRadioButton radLuongTheoCa;
    private JRadioButton radLuongThangTheoCa;
    private JRadioButton radLuongThangTheoNgay;
    private JTextField txtLuongCa;
    private JTextField txtLuongThang;
    private JCheckBox chkNghiThu7;
    private JCheckBox chkNghiChuNhat;

    private JTextField txtGhiChu;

    public NhanVienEditDialog(Dialog parent, NhanVien nhanVien, List<NhanVien> employeeList, Runnable onDataChanged) {
        super(parent, nhanVien != null && nhanVien.getId() != null ? "NHÂN VIÊN - SỬA" : "NHÂN VIÊN - THÊM", true);
        this.currentNhanVien = nhanVien;
        this.employeeList = employeeList;
        this.currentIndex = (employeeList != null && nhanVien != null) ? employeeList.indexOf(nhanVien) : -1;
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

        // 1. Header Panel: Nền vàng kem + Icon Avatar + Tiêu đề
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        headerPanel.setBackground(new Color(254, 249, 231));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 215, 180)));

        JLabel lblAvatarIcon = new JLabel(getAvatarIcon("user_female_1", 32, 32));
        JLabel lblHeaderTitle = new JLabel("Nhân viên");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(new Color(40, 40, 40));

        headerPanel.add(lblAvatarIcon);
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

        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Row 1: Tên nhân viên & Ảnh
        JPanel rowName = new JPanel(new BorderLayout(8, 0));
        rowName.setOpaque(false);
        rowName.setMaximumSize(new Dimension(460, 28));

        JLabel lblName = new JLabel("Tên nhân viên");
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

        String[] avatarKeys = {"user_female_1", "user_female_2", "user_female_3", "user_female_4", "user_female_5", "user_female_6", "user_male_1"};
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
                    lbl.setIcon(getAvatarIcon(value.toString(), 16, 16));
                }
                return lbl;
            }
        });
        pnlAvatar.add(cbAvatar);
        rowName.add(pnlAvatar, BorderLayout.EAST);

        formPanel.add(rowName);
        formPanel.add(Box.createVerticalStrut(6));

        // Row 2: Địa chỉ
        JPanel rowDiaChi = createFieldRow("Địa chỉ", txtDiaChi = createStyledTextField());
        formPanel.add(rowDiaChi);
        formPanel.add(Box.createVerticalStrut(6));

        // Row 3: Điện thoại
        JPanel rowPhone = createFieldRow("Điện thoại", txtDienThoai = createStyledTextField());
        formPanel.add(rowPhone);
        formPanel.add(Box.createVerticalStrut(8));

        // Group Box: Cách tính lương (Khớp chuẩn 100% Ảnh 3)
        JPanel salaryPanel = new JPanel();
        salaryPanel.setLayout(new BoxLayout(salaryPanel, BoxLayout.Y_AXIS));
        salaryPanel.setOpaque(false);
        salaryPanel.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(185, 205, 230), 1), "Cách tính lương", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.PLAIN, 12), new Color(30, 30, 30)),
                new EmptyBorder(4, 8, 8, 8)
        ));

        ButtonGroup bgSalary = new ButtonGroup();
        radLuongTheoCa = new JRadioButton("Lương theo ca");
        radLuongThangTheoCa = new JRadioButton("Lương tháng theo ca");
        radLuongThangTheoNgay = new JRadioButton("Lương tháng theo ngày");

        radLuongTheoCa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radLuongThangTheoCa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radLuongThangTheoNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        radLuongTheoCa.setOpaque(false);
        radLuongThangTheoCa.setOpaque(false);
        radLuongThangTheoNgay.setOpaque(false);

        bgSalary.add(radLuongTheoCa);
        bgSalary.add(radLuongThangTheoCa);
        bgSalary.add(radLuongThangTheoNgay);

        // Section 1: Lương theo ca
        JPanel sec1 = new JPanel(new BorderLayout());
        sec1.setOpaque(false);
        sec1.add(radLuongTheoCa, BorderLayout.WEST);

        JPanel pnlLuongCa = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlLuongCa.setOpaque(false);
        JLabel lblLca = new JLabel("Lương ca");
        lblLca.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLuongCa = createStyledTextField();
        txtLuongCa.setPreferredSize(new Dimension(100, 22));
        txtLuongCa.setHorizontalAlignment(JTextField.RIGHT);
        txtLuongCa.setText("0");
        pnlLuongCa.add(lblLca);
        pnlLuongCa.add(txtLuongCa);
        sec1.add(pnlLuongCa, BorderLayout.EAST);
        salaryPanel.add(sec1);

        JLabel sub1 = new JLabel("       (Cách tính: Lương = Tổng ca làm việc x Lương ca)");
        sub1.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub1.setForeground(new Color(60, 60, 60));
        salaryPanel.add(sub1);
        salaryPanel.add(Box.createVerticalStrut(8));

        // Section 2: Lương tháng theo ca
        JPanel sec2 = new JPanel(new BorderLayout());
        sec2.setOpaque(false);
        sec2.add(radLuongThangTheoCa, BorderLayout.WEST);

        JPanel pnlLuongThang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlLuongThang.setOpaque(false);
        JLabel lblLThang = new JLabel("Lương tháng");
        lblLThang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLuongThang = createStyledTextField();
        txtLuongThang.setPreferredSize(new Dimension(100, 22));
        txtLuongThang.setHorizontalAlignment(JTextField.RIGHT);
        txtLuongThang.setText("0");
        pnlLuongThang.add(lblLThang);
        pnlLuongThang.add(txtLuongThang);
        sec2.add(pnlLuongThang, BorderLayout.EAST);
        salaryPanel.add(sec2);

        JLabel sub2 = new JLabel("       (Cách tính: Lương = Tổng ca làm việc x Lương tháng / số ngày tháng)");
        sub2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub2.setForeground(new Color(60, 60, 60));
        salaryPanel.add(sub2);
        salaryPanel.add(Box.createVerticalStrut(8));

        // Section 3: Lương tháng theo ngày
        JPanel sec3 = new JPanel(new BorderLayout());
        sec3.setOpaque(false);
        sec3.add(radLuongThangTheoNgay, BorderLayout.WEST);

        JPanel pnlDaysOff = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlDaysOff.setOpaque(false);
        chkNghiThu7 = new JCheckBox("Nghỉ thứ 7");
        chkNghiChuNhat = new JCheckBox("Nghỉ chủ nhật");
        chkNghiThu7.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkNghiChuNhat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkNghiThu7.setOpaque(false);
        chkNghiChuNhat.setOpaque(false);
        pnlDaysOff.add(chkNghiThu7);
        pnlDaysOff.add(chkNghiChuNhat);

        salaryPanel.add(sec3);

        JPanel pnlIndentDays = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 2));
        pnlIndentDays.setOpaque(false);
        pnlIndentDays.add(pnlDaysOff);
        salaryPanel.add(pnlIndentDays);

        JLabel sub3 = new JLabel("       (Cách tính: Lương = Tổng ngày làm việc x Lương tháng / số ngày tháng)");
        sub3.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub3.setForeground(new Color(60, 60, 60));
        salaryPanel.add(sub3);

        formPanel.add(salaryPanel);
        formPanel.add(Box.createVerticalStrut(8));

        // Row 4: Ghi chú
        JPanel rowGhiChu = createFieldRow("Ghi chú", txtGhiChu = createStyledTextField());
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

        // Focus & Border cho Lưu & thoát (khớp viền xanh trên ảnh 3)
        btnSaveExit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 215), 2),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        btnSave.addActionListener(e -> {
            if (saveCurrentEmployee()) {
                JOptionPane.showMessageDialog(this, "Đã lưu thông tin nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSaveNew.addActionListener(e -> {
            if (saveCurrentEmployee()) {
                clearFormForNew();
            }
        });

        btnSaveExit.addActionListener(e -> {
            if (saveCurrentEmployee()) {
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

    private JPanel createFieldRow(String labelText, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(460, 28));

        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new Dimension(95, 24));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
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

    public static Icon getAvatarIcon(String avatarKey, int width, int height) {
        // Return avatar icon based on key or generate customized colored avatar icon
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color shirtColor;
                Color hairColor = new Color(130, 80, 40); // Brown hair

                if ("user_female_1".equals(avatarKey)) shirtColor = new Color(20, 90, 160); // Blue
                else if ("user_female_2".equals(avatarKey)) shirtColor = new Color(230, 160, 30); // Yellow
                else if ("user_female_3".equals(avatarKey)) shirtColor = new Color(210, 120, 20); // Orange
                else if ("user_female_4".equals(avatarKey)) shirtColor = new Color(30, 140, 210); // Cyan
                else if ("user_female_5".equals(avatarKey)) shirtColor = new Color(70, 160, 50); // Green
                else if ("user_female_6".equals(avatarKey)) shirtColor = new Color(180, 40, 40); // Red
                else shirtColor = new Color(220, 130, 30); // Male orange

                // Draw Head & Hair
                g2.setColor(hairColor);
                g2.fillOval(x + width / 4, y + 1, width / 2, height / 2);

                // Face
                g2.setColor(new Color(255, 215, 175));
                g2.fillOval(x + width / 4 + 2, y + 3, width / 2 - 4, height / 2 - 3);

                // Hair fringe
                g2.setColor(hairColor);
                g2.fillArc(x + width / 4, y + 1, width / 2, height / 3, 0, 180);

                // Body / Shirt
                g2.setColor(shirtColor);
                g2.fillRoundRect(x + 2, y + height / 2 + 1, width - 4, height / 2 - 1, 6, 6);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
        };
    }

    private void loadDataToForm() {
        if (currentNhanVien != null) {
            txtName.setText(currentNhanVien.getName() != null ? currentNhanVien.getName() : "");
            txtDiaChi.setText(currentNhanVien.getDiaChi() != null ? currentNhanVien.getDiaChi() : "");
            txtDienThoai.setText(currentNhanVien.getDienThoai() != null ? currentNhanVien.getDienThoai() : "");
            txtGhiChu.setText(currentNhanVien.getGhiChu() != null ? currentNhanVien.getGhiChu() : "");

            if (currentNhanVien.getAvatarIcon() != null) {
                cbAvatar.setSelectedItem(currentNhanVien.getAvatarIcon());
            }

            DecimalFormat df = new DecimalFormat("#,##0.##");
            txtLuongCa.setText(df.format(currentNhanVien.getLuongCa()));
            txtLuongThang.setText(df.format(currentNhanVien.getLuongThang()));

            int salaryType = currentNhanVien.getCachTinhLuong();
            if (salaryType == 0) {
                radLuongTheoCa.setSelected(true);
            } else if (salaryType == 1) {
                radLuongThangTheoCa.setSelected(true);
            } else {
                radLuongThangTheoNgay.setSelected(true);
            }

            chkNghiThu7.setSelected(currentNhanVien.isNghiThu7());
            chkNghiChuNhat.setSelected(currentNhanVien.isNghiChuNhat());
        } else {
            radLuongThangTheoNgay.setSelected(true);
        }
    }

    private boolean saveCurrentEmployee() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên nhân viên!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        if (currentNhanVien == null) {
            currentNhanVien = new NhanVien();
        }

        currentNhanVien.setName(name);
        currentNhanVien.setDiaChi(txtDiaChi.getText().trim());
        currentNhanVien.setDienThoai(txtDienThoai.getText().trim());
        currentNhanVien.setGhiChu(txtGhiChu.getText().trim());

        if (cbAvatar.getSelectedItem() != null) {
            currentNhanVien.setAvatarIcon(cbAvatar.getSelectedItem().toString());
        }

        int salaryType = 2;
        if (radLuongTheoCa.isSelected()) salaryType = 0;
        else if (radLuongThangTheoCa.isSelected()) salaryType = 1;
        currentNhanVien.setCachTinhLuong(salaryType);

        try {
            String caStr = txtLuongCa.getText().replace(",", "").replace(" ", "").trim();
            currentNhanVien.setLuongCa(caStr.isEmpty() ? 0.0 : Double.parseDouble(caStr));
        } catch (Exception ignored) {
            currentNhanVien.setLuongCa(0.0);
        }

        try {
            String thangStr = txtLuongThang.getText().replace(",", "").replace(" ", "").trim();
            currentNhanVien.setLuongThang(thangStr.isEmpty() ? 0.0 : Double.parseDouble(thangStr));
        } catch (Exception ignored) {
            currentNhanVien.setLuongThang(0.0);
        }

        currentNhanVien.setNghiThu7(chkNghiThu7.isSelected());
        currentNhanVien.setNghiChuNhat(chkNghiChuNhat.isSelected());

        boolean ok;
        if (currentNhanVien.getId() == null || currentNhanVien.getId().isEmpty()) {
            ok = nhanVienDAO.insert(currentNhanVien);
        } else {
            ok = nhanVienDAO.update(currentNhanVien);
        }

        if (ok && onDataChanged != null) {
            onDataChanged.run();
        }
        return ok;
    }

    private void clearFormForNew() {
        currentNhanVien = new NhanVien();
        txtName.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtGhiChu.setText("");
        txtLuongCa.setText("0");
        txtLuongThang.setText("0");
        radLuongThangTheoNgay.setSelected(true);
        chkNghiThu7.setSelected(false);
        chkNghiChuNhat.setSelected(false);
        txtName.requestFocus();
        setTitle("NHÂN VIÊN - THÊM");
    }

    private void navigatePrev() {
        if (employeeList != null && !employeeList.isEmpty() && currentIndex > 0) {
            currentIndex--;
            currentNhanVien = employeeList.get(currentIndex);
            loadDataToForm();
            setTitle("NHÂN VIÊN - SỬA");
        }
    }

    private void navigateNext() {
        if (employeeList != null && !employeeList.isEmpty() && currentIndex < employeeList.size() - 1) {
            currentIndex++;
            currentNhanVien = employeeList.get(currentIndex);
            loadDataToForm();
            setTitle("NHÂN VIÊN - SỬA");
        }
    }
}
