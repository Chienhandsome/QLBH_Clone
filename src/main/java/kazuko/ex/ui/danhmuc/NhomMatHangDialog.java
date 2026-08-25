package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class NhomMatHangDialog extends JDialog {
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private NhomMatHang currentNhom;
    private final Runnable onSaveCallback;
    private List<NhomMatHang> allCategories;
    private int currentIndex = -1;

    private JTextField txtName;
    private JTextField txtCode;
    private JComboBox<String> cbIcon;
    private JCheckBox chkKhongGiamGia;

    private static final Color COLOR_HEADER_BG = new Color(255, 254, 230); // Vàng kem
    private static final Color COLOR_HEADER_BORDER = new Color(225, 220, 185);
    private static final Color COLOR_TOOLBAR_BG = new Color(210, 225, 244); // Xanh nhạt
    private static final Color COLOR_TOOLBAR_BORDER = new Color(175, 198, 226);
    private static final Color COLOR_FORM_BG = new Color(190, 211, 236);    // Xanh form đặc trưng
    private static final Color COLOR_TEXT_DARK = new Color(20, 45, 85);

    public NhomMatHangDialog(Component parent, NhomMatHang nhom, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), "", ModalityType.APPLICATION_MODAL);
        this.currentNhom = nhom != null ? nhom : new NhomMatHang();
        this.onSaveCallback = onSaveCallback;

        this.allCategories = nhomMatHangDAO.getAll();
        findCurrentIndex();

        initUI();
        loadData();
        setupShortcuts();
        pack();
        setLocationRelativeTo(parent);
    }

    private void findCurrentIndex() {
        if (currentNhom != null && currentNhom.getId() != null) {
            for (int i = 0; i < allCategories.size(); i++) {
                if (currentNhom.getId().equals(allCategories.get(i).getId())) {
                    currentIndex = i;
                    break;
                }
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        setPreferredSize(new Dimension(460, 310));

        // 1. Top Header Banner (Nền vàng kem, icon hộp lớn, chữ "Nhóm mặt hàng")
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_HEADER_BORDER));
        headerPanel.setPreferredSize(new Dimension(460, 52));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftHeader.setOpaque(false);

        JLabel lblBoxIcon = new JLabel(IconHelper.getIcon("1282529873_objects.png", 34, 34));
        if (lblBoxIcon.getIcon() == null) {
            lblBoxIcon = new JLabel(IconHelper.getIcon("Folder.png", 34, 34));
        }

        JLabel lblHeaderTitle = new JLabel("Nhóm mặt hàng");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(COLOR_TEXT_DARK);

        leftHeader.add(lblBoxIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Middle Container: Ribbon Toolbar + Form Body
        JPanel centerContainer = new JPanel(new BorderLayout());

        // 2.1. Sub Toolbar (Phím tắt, Trước F10, Sau F11, Tạo mới)
        JPanel ribbonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        ribbonBar.setBackground(COLOR_TOOLBAR_BG);
        ribbonBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_TOOLBAR_BORDER));

        JButton btnShortcuts = createRibbonButton("Phím tắt ▾", "btnConfig.Icon.png");
        JButton btnPrev = createRibbonButton("Trước (F10)", "arrow-180-medium.png");
        JButton btnNext = createRibbonButton("Sau (F11)", "arrow-000-medium.png");
        JButton btnCreateNew = createRibbonButton("Tạo mới", "btnNew.Image.png");

        btnShortcuts.addActionListener(e -> showShortcutMenu(btnShortcuts));
        btnPrev.addActionListener(e -> navigatePrevious());
        btnNext.addActionListener(e -> navigateNext());
        btnCreateNew.addActionListener(e -> onNewCategory());

        ribbonBar.add(btnShortcuts);
        ribbonBar.add(btnPrev);
        ribbonBar.add(btnNext);
        ribbonBar.add(btnCreateNew);
        centerContainer.add(ribbonBar, BorderLayout.NORTH);

        // 2.2. Main Form Fields (Nền xanh đặc trưng)
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Row 1: Tên nhóm mặt hàng & Ảnh icon
        JLabel lblName = new JLabel("Tên nhóm mặt hàng");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(15, 12, 120, 24);
        formPanel.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBounds(140, 12, 175, 24);
        formPanel.add(txtName);

        JLabel lblAnh = new JLabel("Ảnh:");
        lblAnh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAnh.setBounds(325, 12, 30, 24);
        formPanel.add(lblAnh);

        String[] iconOptions = {"asterisk-yellow.png", "Folder.png", "Tree.png", "Barcode.png"};
        cbIcon = new JComboBox<>(iconOptions);
        cbIcon.setBounds(358, 12, 70, 24);
        cbIcon.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
                if (value != null) {
                    lbl.setIcon(IconHelper.getIcon(value.toString(), 16, 16));
                }
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });
        formPanel.add(cbIcon);

        // Row 2: Đánh mã sản phẩm
        JLabel lblCode = new JLabel("Đánh mã sản phẩm");
        lblCode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCode.setBounds(15, 44, 120, 24);
        formPanel.add(lblCode);

        txtCode = new JTextField();
        txtCode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCode.setBounds(140, 44, 175, 24);
        formPanel.add(txtCode);

        // Row 3: Checkbox "Không cho phép giảm giá khi bán hàng"
        chkKhongGiamGia = new JCheckBox("Không cho phép giảm giá khi bán hàng");
        chkKhongGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKhongGiamGia.setOpaque(false);
        chkKhongGiamGia.setBounds(138, 74, 280, 22);
        formPanel.add(chkKhongGiamGia);

        // Row 4: Ghi chú hướng dẫn tự sinh mã
        JLabel lblNote = new JLabel("<html><body style='width: 260px; font-size: 10px; color: #1E3C70;'>" +
                "(Ví dụ bạn quy định mã: <b>AO</b> cho nhóm Áo, thì các sản phẩm trong nhóm này sẽ có mã tự sinh bắt đầu bằng AO: AO001, AO002...)" +
                "</body></html>");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNote.setBounds(140, 98, 290, 42);
        formPanel.add(lblNote);

        centerContainer.add(formPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Buttons (Lưu, Lưu & Mới, Lưu & thoát, Thoát)
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        bottomBar.setBackground(COLOR_FORM_BG);
        bottomBar.setBorder(new MatteBorder(1, 0, 0, 0, COLOR_TOOLBAR_BORDER));

        JButton btnSave = createActionButton("Lưu", "saveAll.png");
        JButton btnSaveAndNew = createActionButton("Lưu & Mới", null);
        JButton btnSaveAndExit = createActionButton("Lưu & thoát", null);
        JButton btnExit = createActionButton("Thoát", null);

        btnSave.addActionListener(e -> onSaveOnly());
        btnSaveAndNew.addActionListener(e -> onSaveAndNew());
        btnSaveAndExit.addActionListener(e -> onSaveAndExit());
        btnExit.addActionListener(e -> dispose());

        bottomBar.add(btnSave);
        bottomBar.add(btnSaveAndNew);
        bottomBar.add(btnSaveAndExit);
        bottomBar.add(btnExit);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JButton createRibbonButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(text.length() > 6 ? 92 : 72, 26));
        return btn;
    }

    private void showShortcutMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem miF10 = new JMenuItem("F10: Nhóm trước đó");
        JMenuItem miF11 = new JMenuItem("F11: Nhóm kế tiếp");
        JMenuItem miF12 = new JMenuItem("F12 / Enter: Lưu dữ liệu");
        miF10.addActionListener(e -> navigatePrevious());
        miF11.addActionListener(e -> navigateNext());
        miF12.addActionListener(e -> onSaveAndExit());
        menu.add(miF10);
        menu.add(miF11);
        menu.add(miF12);
        menu.show(invoker, 0, invoker.getHeight());
    }

    private String originalName;

    private void loadData() {
        boolean isEdit = currentNhom != null && currentNhom.getId() != null;
        setTitle(isEdit ? "NHÓM MẶT HÀNG - SỬA" : "NHÓM MẶT HÀNG - THÊM");
        originalName = isEdit ? currentNhom.getName() : null;

        if (isEdit) {
            txtName.setText(currentNhom.getName() != null ? currentNhom.getName() : "");
            txtCode.setText(currentNhom.getCode() != null ? currentNhom.getCode() : "");
            chkKhongGiamGia.setSelected(currentNhom.isKhongChoPhepGiamGia());

            String icon = currentNhom.getIconName();
            if (icon != null) {
                cbIcon.setSelectedItem(icon);
            }
        } else {
            txtName.setText("");
            txtCode.setText("");
            chkKhongGiamGia.setSelected(false);
            cbIcon.setSelectedIndex(0);
        }
        txtName.requestFocus();
    }

    private void navigatePrevious() {
        if (allCategories.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            currentNhom = allCategories.get(currentIndex);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Đây là nhóm đầu tiên trong danh mục!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void navigateNext() {
        if (allCategories.isEmpty()) return;
        if (currentIndex < allCategories.size() - 1) {
            currentIndex++;
            currentNhom = allCategories.get(currentIndex);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Đây là nhóm cuối cùng trong danh mục!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onNewCategory() {
        currentNhom = new NhomMatHang();
        currentIndex = -1;
        loadData();
    }

    private boolean doSave() {
        String name = txtName.getText().trim();
        String code = txtCode.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên nhóm mặt hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        String oldName = originalName;
        currentNhom.setName(name);
        currentNhom.setCode(code.isEmpty() ? name.substring(0, Math.min(name.length(), 3)).toUpperCase() : code);
        currentNhom.setIconName((String) cbIcon.getSelectedItem());
        currentNhom.setKhongChoPhepGiamGia(chkKhongGiamGia.isSelected());

        boolean ok;
        if (currentNhom.getId() == null || currentNhom.getId().isEmpty()) {
            ok = nhomMatHangDAO.insert(currentNhom);
        } else {
            ok = nhomMatHangDAO.update(currentNhom);
            if (oldName != null && !oldName.equalsIgnoreCase(name)) {
                new kazuko.ex.dao.MatHangDAO().updateNhomName(currentNhom.getId(), oldName, name);
            }
        }

        if (ok) {
            originalName = name;
            allCategories = nhomMatHangDAO.getAll();
            findCurrentIndex();
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu nhóm mặt hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void onSaveOnly() {
        if (doSave()) {
            setTitle("NHÓM MẶT HÀNG - SỬA");
        }
    }

    private void onSaveAndNew() {
        if (doSave()) {
            onNewCategory();
        }
    }

    private void onSaveAndExit() {
        if (doSave()) {
            dispose();
        }
    }

    private void setupShortcuts() {
        // F10 -> Trước
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "prev");
        getRootPane().getActionMap().put("prev", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigatePrevious();
            }
        });

        // F11 -> Sau
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "next");
        getRootPane().getActionMap().put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateNext();
            }
        });

        // F12 or Ctrl+S -> Save & Exit
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "saveExit");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "saveExit");
        getRootPane().getActionMap().put("saveExit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSaveAndExit();
            }
        });

        // Escape -> Close
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
