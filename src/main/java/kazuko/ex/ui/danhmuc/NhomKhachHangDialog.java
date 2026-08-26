package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.model.danhmuc.NhomKhachHangGiamGia;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NhomKhachHangDialog extends JDialog {
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

    private NhomKhachHang currentNhom;
    private final Runnable onSaveCallback;
    private List<NhomKhachHang> allCategories;
    private int currentIndex = -1;

    private JTextField txtName;
    private JComboBox<String> cbIcon;
    private JTextField txtDiemTichLuy;
    private JTextField txtTiLeGiamGia;
    private JTextField txtGhiChu;

    private JTable tableGiamGia;
    private DefaultTableModel modelGiamGia;
    private JTextField txtLocNhomHang;

    private static final Color COLOR_HEADER_BG = new Color(255, 254, 230);
    private static final Color COLOR_HEADER_BORDER = new Color(225, 220, 185);
    private static final Color COLOR_TOOLBAR_BG = new Color(210, 225, 244);
    private static final Color COLOR_TOOLBAR_BORDER = new Color(175, 198, 226);
    private static final Color COLOR_FORM_BG = new Color(190, 211, 236);
    private static final Color COLOR_TEXT_DARK = new Color(20, 45, 85);

    public NhomKhachHangDialog(Component parent, NhomKhachHang nhom, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), "", ModalityType.APPLICATION_MODAL);
        this.currentNhom = nhom != null ? nhom : new NhomKhachHang();
        this.onSaveCallback = onSaveCallback;

        this.allCategories = nhomKhachHangDAO.getAll();
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
        setPreferredSize(new Dimension(530, 490));

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_HEADER_BORDER));
        headerPanel.setPreferredSize(new Dimension(530, 52));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftHeader.setOpaque(false);

        JLabel lblBoxIcon = new JLabel(IconHelper.getIcon("Folder.png", 34, 34));
        JLabel lblHeaderTitle = new JLabel("Nhóm khách hàng");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(COLOR_TEXT_DARK);

        leftHeader.add(lblBoxIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Middle Container: Ribbon Toolbar + Form Body
        JPanel centerContainer = new JPanel(new BorderLayout());

        // 2.1 Sub Toolbar
        JPanel ribbonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        ribbonBar.setBackground(COLOR_TOOLBAR_BG);
        ribbonBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_TOOLBAR_BORDER));

        JButton btnShortcuts = createRibbonButton("Phím tắt ▾", "btnConfig.Icon.png");
        JButton btnPrev = createRibbonButton("Trước (F10)", "arrow-180-medium.png");
        JButton btnNext = createRibbonButton("Sau (F11)", "arrow-000-medium.png");
        JButton btnCreateNew = createRibbonButton("Tạo mới", "btnNew.Image.png");
        JButton btnCopy = createRibbonButton("Sao chép", "Copy.png");
        JButton btnDelete = createRibbonButton("Xóa", "cross.png");

        btnShortcuts.addActionListener(e -> showShortcutMenu(btnShortcuts));
        btnPrev.addActionListener(e -> navigatePrevious());
        btnNext.addActionListener(e -> navigateNext());
        btnCreateNew.addActionListener(e -> onNewCategory());
        btnCopy.addActionListener(e -> onCopyCategory());
        btnDelete.addActionListener(e -> onDeleteCategory());

        ribbonBar.add(btnShortcuts);
        ribbonBar.add(btnPrev);
        ribbonBar.add(btnNext);
        ribbonBar.add(btnCreateNew);
        ribbonBar.add(btnCopy);
        ribbonBar.add(btnDelete);
        centerContainer.add(ribbonBar, BorderLayout.NORTH);

        // 2.2 Main Form Fields
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Row 1: Tên nhóm khách hàng & Ảnh icon
        JLabel lblName = new JLabel("Tên nhóm khách hàng");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(15, 12, 130, 24);
        formPanel.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBounds(150, 12, 220, 24);
        formPanel.add(txtName);

        JLabel lblAnh = new JLabel("Ảnh:");
        lblAnh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAnh.setBounds(380, 12, 30, 24);
        formPanel.add(lblAnh);

        String[] iconOptions = {"Folder.png", "users.png", "1282529873_objects.png", "asterisk-yellow.png", "Tree.png"};
        cbIcon = new JComboBox<>(iconOptions);
        cbIcon.setBounds(415, 12, 85, 24);
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

        // Row 2: Điểm tích lũy
        JLabel lblDiem = new JLabel("Điểm tích lũy");
        lblDiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiem.setBounds(15, 42, 130, 24);
        formPanel.add(lblDiem);

        txtDiemTichLuy = new JTextField("0");
        txtDiemTichLuy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiemTichLuy.setBounds(150, 42, 220, 24);
        formPanel.add(txtDiemTichLuy);

        // Row 3: Tỉ lệ giảm giá %
        JLabel lblGiamGia = new JLabel("Tỉ lệ giảm giá");
        lblGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiamGia.setBounds(15, 72, 130, 24);
        formPanel.add(lblGiamGia);

        txtTiLeGiamGia = new JTextField("0");
        txtTiLeGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTiLeGiamGia.setBounds(150, 72, 220, 24);
        formPanel.add(txtTiLeGiamGia);

        JLabel lblPercent = new JLabel("%");
        lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPercent.setBounds(380, 72, 20, 24);
        formPanel.add(lblPercent);

        // Row 4: Ghi chú
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setBounds(15, 102, 130, 24);
        formPanel.add(lblGhiChu);

        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setBounds(150, 102, 220, 24);
        formPanel.add(txtGhiChu);

        // Row 5: Giảm theo nhóm hàng button
        JLabel lblGiamTheoNhom = new JLabel("Giảm theo nhóm hàng");
        lblGiamTheoNhom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiamTheoNhom.setBounds(15, 135, 130, 24);
        formPanel.add(lblGiamTheoNhom);

        JButton btnAddAllNhom = new JButton("Thêm tất cả nhóm hàng");
        btnAddAllNhom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAddAllNhom.setBounds(150, 135, 175, 26);
        btnAddAllNhom.setBackground(Color.WHITE);
        btnAddAllNhom.addActionListener(e -> onAddAllNhomMatHang());
        formPanel.add(btnAddAllNhom);

        // Grid Table Chiết khấu theo nhóm hàng
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBounds(15, 170, 485, 170);
        tableContainer.setBorder(new LineBorder(new Color(180, 200, 225), 1));

        JPanel tableToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        tableToolbar.setBackground(new Color(225, 235, 248));
        tableToolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnAddRow = createMiniButton("+ Thêm dòng (F4)", "plus.png");
        JButton btnDeleteRow = createMiniButton("Xóa dòng (F8)", "cross.png");
        JButton btnExport = createMiniButton("Xuất excel", "Report.png");

        JLabel lblLoc = new JLabel("Lọc");
        lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLocNhomHang = new JTextField(8);
        txtLocNhomHang.setPreferredSize(new Dimension(80, 22));

        btnAddRow.addActionListener(e -> onAddDiscountRow());
        btnDeleteRow.addActionListener(e -> onDeleteDiscountRow());
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "Xuất excel thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        tableToolbar.add(btnAddRow);
        tableToolbar.add(btnDeleteRow);
        tableToolbar.add(btnExport);
        tableToolbar.add(Box.createHorizontalStrut(10));
        tableToolbar.add(lblLoc);
        tableToolbar.add(txtLocNhomHang);
        tableContainer.add(tableToolbar, BorderLayout.NORTH);

        String[] cols = {"", "Nhóm hàng", "Tỉ lệ giảm"};
        modelGiamGia = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2;
            }
        };

        tableGiamGia = new JTable(modelGiamGia);
        tableGiamGia.setRowHeight(22);
        tableGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableGiamGia.getColumnModel().getColumn(0).setPreferredWidth(30);
        tableGiamGia.getColumnModel().getColumn(0).setMaxWidth(35);
        tableGiamGia.getColumnModel().getColumn(1).setPreferredWidth(280);
        tableGiamGia.getColumnModel().getColumn(2).setPreferredWidth(120);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableGiamGia.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollTable = new JScrollPane(tableGiamGia);
        scrollTable.setBorder(null);
        tableContainer.add(scrollTable, BorderLayout.CENTER);

        formPanel.add(tableContainer);
        centerContainer.add(formPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footerPanel.setBackground(new Color(235, 242, 252));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        JButton btnSave = createActionButton("Lưu", "Save1.png");
        JButton btnSaveNew = createActionButton("Lưu & Mới", null);
        JButton btnSaveExit = createActionButton("Lưu & thoát", null);
        JButton btnExit = createActionButton("Thoát", null);

        btnSave.addActionListener(e -> onSave(false, false));
        btnSaveNew.addActionListener(e -> onSave(true, false));
        btnSaveExit.addActionListener(e -> onSave(false, true));
        btnExit.addActionListener(e -> dispose());

        footerPanel.add(btnSave);
        footerPanel.add(btnSaveNew);
        footerPanel.add(btnSaveExit);
        footerPanel.add(btnExit);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createRibbonButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private JButton createMiniButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(1, 4, 1, 4));
        return btn;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(text.length() > 6 ? 100 : 75, 26));
        btn.setBackground(Color.WHITE);
        return btn;
    }

    private void loadData() {
        if (currentNhom.getId() != null) {
            setTitle("NHÓM KHÁCH HÀNG - SỬA");
            txtName.setText(currentNhom.getName() != null ? currentNhom.getName() : "");
            txtDiemTichLuy.setText(String.valueOf((int) currentNhom.getDiemTichLuy()));
            txtTiLeGiamGia.setText(String.valueOf((int) currentNhom.getTiLeGiamGia()));
            txtGhiChu.setText(currentNhom.getGhiChu() != null ? currentNhom.getGhiChu() : "");
            if (currentNhom.getIconName() != null) {
                cbIcon.setSelectedItem(currentNhom.getIconName());
            }

            modelGiamGia.setRowCount(0);
            int rowIdx = 1;
            if (currentNhom.getListGiamGiaNhomHang() != null) {
                for (NhomKhachHangGiamGia gg : currentNhom.getListGiamGiaNhomHang()) {
                    modelGiamGia.addRow(new Object[]{rowIdx++, gg.getTenNhomMatHang(), (int) gg.getTiLeGiam()});
                }
            }
        } else {
            setTitle("NHÓM KHÁCH HÀNG - THÊM MỚI");
            txtName.setText("");
            txtDiemTichLuy.setText("0");
            txtTiLeGiamGia.setText("0");
            txtGhiChu.setText("");
            cbIcon.setSelectedIndex(0);
            modelGiamGia.setRowCount(0);
        }
    }

    private void onAddAllNhomMatHang() {
        List<NhomMatHang> allMhGroups = nhomMatHangDAO.getAll();
        modelGiamGia.setRowCount(0);
        int idx = 1;
        for (NhomMatHang nmh : allMhGroups) {
            if (!nmh.isSeparator()) {
                modelGiamGia.addRow(new Object[]{idx++, nmh.getName(), 0});
            }
        }
    }

    private void onAddDiscountRow() {
        int nextStt = modelGiamGia.getRowCount() + 1;
        modelGiamGia.addRow(new Object[]{nextStt, "Nhóm mới", 0});
    }

    private void onDeleteDiscountRow() {
        int selected = tableGiamGia.getSelectedRow();
        if (selected >= 0) {
            modelGiamGia.removeRow(selected);
            for (int i = 0; i < modelGiamGia.getRowCount(); i++) {
                modelGiamGia.setValueAt(i + 1, i, 0);
            }
        }
    }

    private void onSave(boolean isSaveAndNew, boolean isSaveAndExit) {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhóm khách hàng không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        currentNhom.setName(name);
        currentNhom.setIconName((String) cbIcon.getSelectedItem());

        try {
            currentNhom.setDiemTichLuy(Double.parseDouble(txtDiemTichLuy.getText().trim()));
        } catch (Exception e) {
            currentNhom.setDiemTichLuy(0);
        }

        try {
            currentNhom.setTiLeGiamGia(Double.parseDouble(txtTiLeGiamGia.getText().trim()));
        } catch (Exception e) {
            currentNhom.setTiLeGiamGia(0);
        }

        currentNhom.setGhiChu(txtGhiChu.getText().trim());

        List<NhomKhachHangGiamGia> listGg = new ArrayList<>();
        for (int i = 0; i < modelGiamGia.getRowCount(); i++) {
            String tenNhomHang = String.valueOf(modelGiamGia.getValueAt(i, 1));
            double tiLe = 0;
            try {
                tiLe = Double.parseDouble(String.valueOf(modelGiamGia.getValueAt(i, 2)));
            } catch (Exception ignored) {}
            listGg.add(new NhomKhachHangGiamGia("", tenNhomHang, tiLe));
        }
        currentNhom.setListGiamGiaNhomHang(listGg);

        boolean success;
        if (currentNhom.getId() == null) {
            success = nhomKhachHangDAO.insert(currentNhom);
        } else {
            success = nhomKhachHangDAO.update(currentNhom);
        }

        if (!success) {
            JOptionPane.showMessageDialog(this, "Không thể lưu thông tin nhóm vào CSDL! Vui lòng kiểm tra lại kết nối.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return;
        }

        allCategories = nhomKhachHangDAO.getAll();
        findCurrentIndex();

        if (onSaveCallback != null) onSaveCallback.run();

        if (isSaveAndExit) {
            dispose();
        } else if (isSaveAndNew) {
            onNewCategory();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu thông tin nhóm khách hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onNewCategory() {
        this.currentNhom = new NhomKhachHang();
        this.currentIndex = -1;
        loadData();
        txtName.requestFocus();
    }

    private void onCopyCategory() {
        NhomKhachHang copy = new NhomKhachHang();
        copy.setName(txtName.getText().trim() + " (Bản sao)");
        copy.setIconName((String) cbIcon.getSelectedItem());
        try { copy.setDiemTichLuy(Double.parseDouble(txtDiemTichLuy.getText().trim())); } catch (Exception ignored) {}
        try { copy.setTiLeGiamGia(Double.parseDouble(txtTiLeGiamGia.getText().trim())); } catch (Exception ignored) {}
        copy.setGhiChu(txtGhiChu.getText().trim());

        this.currentNhom = copy;
        this.currentIndex = -1;
        loadData();
    }

    private void onDeleteCategory() {
        if (currentNhom.getId() == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn chuyển nhóm '" + currentNhom.getName() + "' vào thùng rác?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            nhomKhachHangDAO.moveToTrash(currentNhom.getId());
            if (onSaveCallback != null) onSaveCallback.run();
            dispose();
        }
    }

    private void navigatePrevious() {
        if (allCategories.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            currentNhom = allCategories.get(currentIndex);
            loadData();
        }
    }

    private void navigateNext() {
        if (allCategories.isEmpty()) return;
        if (currentIndex < allCategories.size() - 1) {
            currentIndex++;
            currentNhom = allCategories.get(currentIndex);
            loadData();
        }
    }

    private void setupShortcuts() {
        getRootPane().registerKeyboardAction(e -> onSave(false, true), KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigatePrevious(), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigateNext(), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void showShortcutMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("Ctrl + S: Lưu & thoát"));
        menu.add(new JMenuItem("F10: Xem mục trước"));
        menu.add(new JMenuItem("F11: Xem mục tiếp theo"));
        menu.add(new JMenuItem("Esc: Thoát"));
        menu.show(invoker, 0, invoker.getHeight());
    }
}
