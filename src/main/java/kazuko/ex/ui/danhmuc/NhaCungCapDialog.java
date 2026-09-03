package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.NhomNhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class NhaCungCapDialog extends JDialog {

    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final NhomNhaCungCapDAO nhomNhaCungCapDAO = new NhomNhaCungCapDAO();

    private NhaCungCap currentNcc;
    private final Runnable onDataSaved;

    // Form fields
    private JTextField txtMaNCC;
    private JTextField txtTenNCC;
    private JTextField txtDiaChi;
    private JTextField txtDienThoai;
    private JTextField txtEmail;
    private JTextField txtWebsite;
    private JComboBox<NhomNhaCungCap> cbNhomNCC;
    private JTextField txtGhiChu;

    public NhaCungCapDialog(Window parent, Runnable onDataSaved) {
        this(parent, null, onDataSaved);
    }

    public NhaCungCapDialog(Window parent, NhaCungCap ncc, Runnable onDataSaved) {
        super(parent, ncc == null ? "NHÀ CUNG CẤP - THÊM MỚI" : "NHÀ CUNG CẤP - SỬA", ModalityType.APPLICATION_MODAL);
        this.currentNcc = ncc;
        this.onDataSaved = onDataSaved;

        initComponents();
        loadNhomData();

        if (ncc != null) {
            populateExistingData();
        } else {
            resetForNew();
        }

        setupKeyBindings();
        setSize(480, 420);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(235, 242, 250));

        // Header + Navigation Bar
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        // Header Banner (Icon + "Nhà cung cấp")
        JPanel pnlBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlBanner.setBackground(new Color(245, 249, 254));
        pnlBanner.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 222, 242)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("user.png", 32, 32));
        JLabel lblTitle = new JLabel("Nhà cung cấp");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(25, 45, 75));

        pnlBanner.add(lblIcon);
        pnlBanner.add(lblTitle);
        pnlTop.add(pnlBanner, BorderLayout.NORTH);

        // Navigation Toolbar
        JPanel pnlNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlNav.setBackground(new Color(225, 237, 250));
        pnlNav.setBorder(new MatteBorder(0, 0, 1, 0, new Color(195, 215, 238)));

        JButton btnShortcuts = createToolButton("Phím tắt", "help.png");
        btnShortcuts.addActionListener(e -> showShortcutHelp());

        JButton btnPrev = createToolButton("◀ Trước (F10)", "resultset_previous.png");
        btnPrev.addActionListener(e -> navigateRecord(-1));

        JButton btnNext = createToolButton("Sau (F11) ▶", "resultset_next.png");
        btnNext.addActionListener(e -> navigateRecord(1));

        JButton btnNew = createToolButton("+ Tạo mới", "plus.png");
        btnNew.addActionListener(e -> resetForNew());

        pnlNav.add(btnShortcuts);
        pnlNav.add(btnPrev);
        pnlNav.add(btnNext);
        pnlNav.add(btnNew);
        pnlTop.add(pnlNav, BorderLayout.CENTER);

        add(pnlTop, BorderLayout.NORTH);

        // Center Form
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(10, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Color highlightBg = new Color(230, 247, 255); // Nền xanh nhạt matching Image 2 & 3

        // Mã nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Mã nhà cung cấp"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtMaNCC = new JTextField();
        txtMaNCC.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMaNCC.setBackground(highlightBg);
        pnlCenter.add(txtMaNCC, gbc);

        // Tên nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Tên nhà cung cấp"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTenNCC = new JTextField();
        txtTenNCC.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtTenNCC.setBackground(highlightBg);
        pnlCenter.add(txtTenNCC, gbc);

        // Địa chỉ
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Địa chỉ"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDiaChi = new JTextField();
        txtDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(txtDiaChi, gbc);

        // Điện thoại
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Điện thoại"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDienThoai = new JTextField();
        txtDienThoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(txtDienThoai, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Email"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(txtEmail, gbc);

        // Website
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Website"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtWebsite = new JTextField();
        txtWebsite.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(txtWebsite, gbc);

        // Nhóm nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Nhóm nhà cung cấp"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel pnlNhomCombo = new JPanel(new BorderLayout(3, 0));
        pnlNhomCombo.setOpaque(false);
        cbNhomNCC = new JComboBox<>();
        cbNhomNCC.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlNhomCombo.add(cbNhomNCC, BorderLayout.CENTER);

        JButton btnQuickAddNhom = new JButton(IconHelper.getIcon("plus.png", 12, 12));
        btnQuickAddNhom.setPreferredSize(new Dimension(24, 22));
        btnQuickAddNhom.setToolTipText("Thêm nhóm mới");
        btnQuickAddNhom.addActionListener(e -> promptAddNewGroup());
        pnlNhomCombo.add(btnQuickAddNhom, BorderLayout.EAST);
        pnlCenter.add(pnlNhomCombo, gbc);

        // Ghi chú
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.0;
        pnlCenter.add(new JLabel("Ghi chú"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(txtGhiChu, gbc);

        add(pnlCenter, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        pnlBottom.setBackground(new Color(225, 237, 250));
        pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(195, 215, 238)));

        JButton btnLuu = createPrimaryButton("Lưu", "disk.png");
        btnLuu.addActionListener(e -> saveNcc(false, false));

        JButton btnLuuMoi = createPrimaryButton("Lưu & Mới", "add.png");
        btnLuuMoi.addActionListener(e -> saveNcc(true, false));

        JButton btnLuuThoat = createPrimaryButton("Lưu & thoát", "door_in.png");
        btnLuuThoat.addActionListener(e -> saveNcc(false, true));

        JButton btnThoat = createSecondaryButton("Thoát", "cancel.png");
        btnThoat.addActionListener(e -> dispose());

        pnlBottom.add(btnLuu);
        pnlBottom.add(btnLuuMoi);
        pnlBottom.add(btnLuuThoat);
        pnlBottom.add(btnThoat);

        add(pnlBottom, BorderLayout.SOUTH);
    }

    private JButton createToolButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPrimaryButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(245, 250, 255));
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(150, 185, 225), 1), new EmptyBorder(4, 10, 4, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSecondaryButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(4, 8, 4, 8)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadNhomData() {
        cbNhomNCC.removeAllItems();
        cbNhomNCC.addItem(new NhomNhaCungCap("", "-- Chưa thiết lập --"));
        List<NhomNhaCungCap> nhomList = nhomNhaCungCapDAO.getAll();
        for (NhomNhaCungCap n : nhomList) {
            cbNhomNCC.addItem(n);
        }
    }

    private void promptAddNewGroup() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên nhóm nhà cung cấp mới:", "Thêm nhóm nhà cung cấp", JOptionPane.QUESTION_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            NhomNhaCungCap n = new NhomNhaCungCap();
            n.setName(name.trim());
            if (nhomNhaCungCapDAO.insert(n)) {
                loadNhomData();
                for (int i = 0; i < cbNhomNCC.getItemCount(); i++) {
                    NhomNhaCungCap item = cbNhomNCC.getItemAt(i);
                    if (name.trim().equalsIgnoreCase(item.getName())) {
                        cbNhomNCC.setSelectedIndex(i);
                        break;
                    }
                }
                JOptionPane.showMessageDialog(this, "Thêm nhóm nhà cung cấp thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể thêm nhóm nhà cung cấp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void resetForNew() {
        currentNcc = null;
        setTitle("NHÀ CUNG CẤP - THÊM MỚI");
        txtMaNCC.setText("");
        txtTenNCC.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtEmail.setText("");
        txtWebsite.setText("");
        txtGhiChu.setText("");
        if (cbNhomNCC.getItemCount() > 0) cbNhomNCC.setSelectedIndex(0);
        txtMaNCC.requestFocus();
    }

    private void populateExistingData() {
        if (currentNcc == null) return;
        setTitle("NHÀ CUNG CẤP - SỬA [" + currentNcc.getName() + "]");
        txtMaNCC.setText(currentNcc.getMaNhaCungCap() != null ? currentNcc.getMaNhaCungCap() : "");
        txtTenNCC.setText(currentNcc.getName() != null ? currentNcc.getName() : "");
        txtDiaChi.setText(currentNcc.getDiaChi() != null ? currentNcc.getDiaChi() : "");
        txtDienThoai.setText(currentNcc.getDienThoai() != null ? currentNcc.getDienThoai() : "");
        txtEmail.setText(currentNcc.getEmail() != null ? currentNcc.getEmail() : "");
        txtWebsite.setText(currentNcc.getWebsite() != null ? currentNcc.getWebsite() : "");
        txtGhiChu.setText(currentNcc.getNote() != null ? currentNcc.getNote() : "");

        if (currentNcc.getNhomNhaCungCapId() != null) {
            for (int i = 0; i < cbNhomNCC.getItemCount(); i++) {
                NhomNhaCungCap item = cbNhomNCC.getItemAt(i);
                if (currentNcc.getNhomNhaCungCapId().equals(item.getId())) {
                    cbNhomNCC.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private boolean saveNcc(boolean createNewAfter, boolean closeAfter) {
        String ten = txtTenNCC.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên nhà cung cấp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTenNCC.requestFocus();
            return false;
        }

        String ma = txtMaNCC.getText().trim();
        if (ma.isEmpty()) {
            ma = "ncc_" + System.currentTimeMillis() % 100000;
        }

        boolean isNew = (currentNcc == null || currentNcc.getId() == null);
        NhaCungCap ncc = isNew ? new NhaCungCap() : currentNcc;

        ncc.setMaNhaCungCap(ma);
        ncc.setName(ten);
        ncc.setDiaChi(txtDiaChi.getText().trim());
        ncc.setDienThoai(txtDienThoai.getText().trim());
        ncc.setEmail(txtEmail.getText().trim());
        ncc.setWebsite(txtWebsite.getText().trim());
        ncc.setNote(txtGhiChu.getText().trim());

        NhomNhaCungCap selNhom = (NhomNhaCungCap) cbNhomNCC.getSelectedItem();
        if (selNhom != null && selNhom.getId() != null && !selNhom.getId().isEmpty()) {
            ncc.setNhomNhaCungCapId(selNhom.getId());
            ncc.setTenNhomNhaCungCap(selNhom.getName());
        } else {
            ncc.setNhomNhaCungCapId(null);
            ncc.setTenNhomNhaCungCap(null);
        }

        boolean success;
        if (isNew) {
            success = nhaCungCapDAO.insert(ncc);
        } else {
            success = nhaCungCapDAO.update(ncc);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu nhà cung cấp thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();

            if (createNewAfter) {
                resetForNew();
            } else if (closeAfter) {
                dispose();
            } else {
                currentNcc = ncc;
                setTitle("NHÀ CUNG CẤP - SỬA [" + ncc.getName() + "]");
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu nhà cung cấp. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void navigateRecord(int offset) {
        if (currentNcc == null || currentNcc.getId() == null) return;
        NhaCungCap target = nhaCungCapDAO.getOffset(currentNcc.getId(), offset);
        if (target != null) {
            currentNcc = target;
            populateExistingData();
        } else {
            JOptionPane.showMessageDialog(this, offset < 0 ? "Đã ở nhà cung cấp đầu tiên!" : "Đã ở nhà cung cấp cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showShortcutHelp() {
        String msg = "DANH SÁCH PHÍM TẮT:\n\n" +
                "• Ctrl + S: Lưu & Tạo mới\n" +
                "• Ctrl + L: Lưu thông tin\n" +
                "• Ctrl + N: Tạo mới\n" +
                "• F10: Xem nhà cung cấp trước\n" +
                "• F11: Xem nhà cung cấp sau\n" +
                "• ESC: Đóng màn hình";
        JOptionPane.showMessageDialog(this, msg, "Hướng dẫn phím tắt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupKeyBindings() {
        JRootPane rp = getRootPane();
        rp.registerKeyboardAction(e -> saveNcc(true, false), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> saveNcc(false, false), KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> resetForNew(), KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> navigateRecord(-1), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> navigateRecord(1), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
