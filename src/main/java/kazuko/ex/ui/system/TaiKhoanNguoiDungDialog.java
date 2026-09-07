package kazuko.ex.ui.system;

import kazuko.ex.dao.GroupUserDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.system.SGroupUser;
import kazuko.ex.model.system.SUser;
import kazuko.ex.model.system.UserStoreAccess;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanNguoiDungDialog extends JDialog {
    private final UserDAO userDAO = new UserDAO();
    private final GroupUserDAO groupUserDAO = new GroupUserDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private SUser currentUserData;
    private boolean isEditMode = false;
    private boolean isSaved = false;
    private String defaultGroupId;

    private List<SUser> navigationList;
    private int currentIndex = -1;

    // UI Fields
    private JComboBox<NhanVienComboItem> cbNhanVien;
    private JTextField txtHoTen;
    private JTextField txtTaiKhoan;
    private JPasswordField txtMatKhau;
    private JTextField txtEmail;
    private JComboBox<GroupComboItem> cbNhomNguoiDung;
    private JTable tableCuaHang;
    private StoreTableModel modelCuaHang;
    private JTextField txtIdTaiKhoan;
    private JTextField txtMaThe;

    private JButton btnPrev;
    private JButton btnNext;

    public static class NhanVienComboItem {
        private final String id;
        private final String name;

        public NhanVienComboItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }

    public static class GroupComboItem {
        private final SGroupUser group;

        public GroupComboItem(SGroupUser group) {
            this.group = group;
        }

        public SGroupUser getGroup() { return group; }

        @Override
        public String toString() { return group != null ? group.getName() : ""; }
    }

    public TaiKhoanNguoiDungDialog(Window parent, SUser user, String defaultGroupId, List<SUser> userList) {
        super(parent, "Tài khoản người dùng", ModalityType.APPLICATION_MODAL);
        this.currentUserData = user;
        this.defaultGroupId = defaultGroupId;
        this.navigationList = userList != null ? userList : new ArrayList<>();
        this.isEditMode = (user != null && user.getId() != null);

        if (isEditMode && navigationList != null) {
            for (int i = 0; i < navigationList.size(); i++) {
                if (navigationList.get(i).getId().equals(user.getId())) {
                    currentIndex = i;
                    break;
                }
            }
        }

        initComponents(parent);
        loadCombos();
        if (currentUserData != null) {
            bindData(currentUserData);
        } else {
            initNewForm();
        }
    }

    private void initComponents(Window parent) {
        setSize(540, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Keyboard shortcuts: F10 = Prev, F11 = Next
        getRootPane().registerKeyboardAction(e -> navigatePrev(), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigateNext(), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // 1. Top Ribbon Toolbar
        JPanel ribbon = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        ribbon.setBackground(new Color(235, 240, 248));
        ribbon.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnShortcuts = createRibbonBtn("Phím tắt ▾", "btnKeyboard.Icon.png");
        btnPrev = createRibbonBtn("Trước (F10)", "btnBack.Icon.png");
        btnNext = createRibbonBtn("Sau (F11)", "btnNext.Icon.png");
        JButton btnNew = createRibbonBtn("Tạo mới", "btnNew.Image.png");

        btnPrev.addActionListener(e -> navigatePrev());
        btnNext.addActionListener(e -> navigateNext());
        btnNew.addActionListener(e -> initNewForm());

        ribbon.add(btnShortcuts);
        ribbon.add(btnPrev);
        ribbon.add(btnNext);
        ribbon.add(btnNew);
        add(ribbon, BorderLayout.NORTH);

        // 2. Main Form
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 16, 8, 16));

        JPanel formFields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nhân viên
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblNv = new JLabel("Nhân viên");
        lblNv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblNv, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhanVien = new JComboBox<>();
        cbNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVien.setPreferredSize(new Dimension(340, 24));
        cbNhanVien.addActionListener(e -> {
            NhanVienComboItem item = (NhanVienComboItem) cbNhanVien.getSelectedItem();
            if (item != null && item.getId() != null && (txtHoTen.getText().trim().isEmpty() || !isEditMode)) {
                txtHoTen.setText(item.getName());
            }
        });
        formFields.add(cbNhanVien, gbc);

        // Họ và tên
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblHoTen = new JLabel("Họ và tên");
        lblHoTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblHoTen, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtHoTen = new JTextField();
        txtHoTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtHoTen.setPreferredSize(new Dimension(340, 24));
        formFields.add(txtHoTen, gbc);

        // Tài khoản (Cyan bg)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblTaiKhoan = new JLabel("Tài khoản");
        lblTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblTaiKhoan, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTaiKhoan = new JTextField();
        txtTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTaiKhoan.setBackground(new Color(221, 245, 247)); // cyan bg matching Screenshot 3
        txtTaiKhoan.setPreferredSize(new Dimension(340, 24));
        formFields.add(txtTaiKhoan, gbc);

        // Mật khẩu
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblPass, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtMatKhau = new JPasswordField();
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMatKhau.setPreferredSize(new Dimension(340, 24));
        formFields.add(txtMatKhau, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtEmail.setPreferredSize(new Dimension(340, 24));
        formFields.add(txtEmail, gbc);

        // Nhóm người dùng
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        JLabel lblNhom = new JLabel("Nhóm người dùng");
        lblNhom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblNhom, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhomNguoiDung = new JComboBox<>();
        cbNhomNguoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhomNguoiDung.setPreferredSize(new Dimension(340, 24));
        cbNhomNguoiDung.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GroupComboItem) {
                    GroupComboItem item = (GroupComboItem) value;
                    if (item.getGroup() != null) {
                        l.setText(item.getGroup().getName());
                        l.setIcon(IconHelper.getIcon("users.png", 16, 16));
                    }
                }
                return l;
            }
        });
        formFields.add(cbNhomNguoiDung, gbc);

        // Cửa hàng truy cập (Table)
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        JPanel pnlStoreAccess = new JPanel(new BorderLayout(0, 4));
        pnlStoreAccess.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(190, 210, 235)), "Cửa hàng truy cập",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), new Color(50, 70, 100)));

        modelCuaHang = new StoreTableModel();
        tableCuaHang = new JTable(modelCuaHang);
        tableCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCuaHang.setRowHeight(22);
        tableCuaHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableCuaHang.getTableHeader().setBackground(new Color(235, 240, 248));

        // Column widths
        tableCuaHang.getColumnModel().getColumn(0).setPreferredWidth(320);
        tableCuaHang.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableCuaHang.getColumnModel().getColumn(1).setMaxWidth(90);

        JScrollPane spCuaHang = new JScrollPane(tableCuaHang);
        spCuaHang.setPreferredSize(new Dimension(460, 110));
        pnlStoreAccess.add(spCuaHang, BorderLayout.CENTER);
        formFields.add(pnlStoreAccess, gbc);

        // ID tài khoản & Mã thẻ cảm ứng (2 cols)
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID tài khoản
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.0;
        JLabel lblIdTk = new JLabel("ID tài khoản");
        lblIdTk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblIdTk, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtIdTaiKhoan = new JTextField();
        txtIdTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtIdTaiKhoan.setPreferredSize(new Dimension(340, 24));
        formFields.add(txtIdTaiKhoan, gbc);

        // Mã thẻ cảm ứng
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.0;
        JLabel lblMaThe = new JLabel("Mã thẻ cảm ứng");
        lblMaThe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(lblMaThe, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtMaThe = new JTextField();
        txtMaThe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMaThe.setPreferredSize(new Dimension(340, 24));
        formFields.add(txtMaThe, gbc);

        centerPanel.add(formFields, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Footer Toolbar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBackground(new Color(240, 244, 250));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 220, 235)));

        JButton btnSave = new JButton("Lưu", IconHelper.getIcon("Save1.png", 16, 16));
        JButton btnSaveNew = new JButton("Lưu & Mới", IconHelper.getIcon("btnNew.Image.png", 16, 16));
        JButton btnSaveClose = new JButton("Lưu & thoát", IconHelper.getIcon("btnOK.Icon.png", 16, 16));
        JButton btnExit = new JButton("Thoát", IconHelper.getIcon("Cancelled.png", 16, 16));

        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSaveNew.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSaveClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnSave.addActionListener(e -> saveUser(false, false));
        btnSaveNew.addActionListener(e -> saveUser(true, false));
        btnSaveClose.addActionListener(e -> saveUser(false, true));
        btnExit.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnSaveNew);
        footer.add(btnSaveClose);
        footer.add(btnExit);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createRibbonBtn(String text, String iconName) {
        JButton b = new JButton(text, IconHelper.getIcon(iconName, 16, 16));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(3, 6, 3, 6));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadCombos() {
        // Load Employees
        cbNhanVien.removeAllItems();
        cbNhanVien.addItem(new NhanVienComboItem(null, "--- Chọn nhân viên ---"));
        List<NhanVien> employees = nhanVienDAO.getAll();
        for (NhanVien nv : employees) {
            if (nv.getStatus() >= 0 && !nv.isFolder()) {
                cbNhanVien.addItem(new NhanVienComboItem(nv.getId(), nv.getName()));
            }
        }

        // Load Groups
        cbNhomNguoiDung.removeAllItems();
        List<SGroupUser> groups = groupUserDAO.getAllGroups();
        for (SGroupUser g : groups) {
            cbNhomNguoiDung.addItem(new GroupComboItem(g));
        }
    }

    private void bindData(SUser user) {
        if (user == null) return;
        this.currentUserData = user;
        this.isEditMode = (user.getId() != null);

        txtHoTen.setText(user.getName() != null ? user.getName() : "");
        txtTaiKhoan.setText(user.getUsername() != null ? user.getUsername() : "");
        txtMatKhau.setText(user.getPassword() != null ? user.getPassword() : "");
        txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        txtIdTaiKhoan.setText(user.getUserId() != null ? user.getUserId() : "");
        txtMaThe.setText(user.getCardCode() != null ? user.getCardCode() : "");

        // Select employee
        if (user.getdNhanVienId() != null) {
            for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                NhanVienComboItem item = cbNhanVien.getItemAt(i);
                if (item != null && user.getdNhanVienId().equals(item.getId())) {
                    cbNhanVien.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cbNhanVien.setSelectedIndex(0);
        }

        // Select group
        if (user.getGroupUserId() != null) {
            for (int i = 0; i < cbNhomNguoiDung.getItemCount(); i++) {
                GroupComboItem item = cbNhomNguoiDung.getItemAt(i);
                if (item != null && item.getGroup() != null && user.getGroupUserId().equals(item.getGroup().getId())) {
                    cbNhomNguoiDung.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Load Store access
        List<UserStoreAccess> accesses = userDAO.getUserStoreAccess(user.getId());
        modelCuaHang.setData(accesses);

        updateNavButtons();
    }

    private void initNewForm() {
        this.currentUserData = new SUser();
        this.isEditMode = false;
        txtHoTen.setText("");
        txtTaiKhoan.setText("");
        txtMatKhau.setText("");
        txtEmail.setText("");
        txtIdTaiKhoan.setText("");
        txtMaThe.setText("");
        cbNhanVien.setSelectedIndex(0);

        if (defaultGroupId != null) {
            for (int i = 0; i < cbNhomNguoiDung.getItemCount(); i++) {
                GroupComboItem item = cbNhomNguoiDung.getItemAt(i);
                if (item != null && item.getGroup() != null && defaultGroupId.equals(item.getGroup().getId())) {
                    cbNhomNguoiDung.setSelectedIndex(i);
                    break;
                }
            }
        }

        List<UserStoreAccess> accesses = userDAO.getUserStoreAccess(null);
        // Default check the first store if new
        if (!accesses.isEmpty()) {
            accesses.get(0).setAccessed(true);
        }
        modelCuaHang.setData(accesses);
        txtTaiKhoan.requestFocus();
    }

    private void navigatePrev() {
        if (navigationList != null && currentIndex > 0) {
            currentIndex--;
            bindData(navigationList.get(currentIndex));
        }
    }

    private void navigateNext() {
        if (navigationList != null && currentIndex < navigationList.size() - 1) {
            currentIndex++;
            bindData(navigationList.get(currentIndex));
        }
    }

    private void updateNavButtons() {
        btnPrev.setEnabled(navigationList != null && currentIndex > 0);
        btnNext.setEnabled(navigationList != null && currentIndex >= 0 && currentIndex < navigationList.size() - 1);
    }

    private void saveUser(boolean isNewAfter, boolean isCloseAfter) {
        String username = txtTaiKhoan.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên tài khoản!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTaiKhoan.requestFocus();
            return;
        }

        // Check Admin duplicate
        if ("admin".equalsIgnoreCase(username) && !isEditMode) {
            JOptionPane.showMessageDialog(this, "Tên tài khoản 'Admin' là tài khoản hệ thống đã tồn tại!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTaiKhoan.requestFocus();
            return;
        }

        if (currentUserData == null) {
            currentUserData = new SUser();
        }
        currentUserData.setUsername(username);
        currentUserData.setName(txtHoTen.getText().trim());
        currentUserData.setPassword(new String(txtMatKhau.getPassword()).trim());
        currentUserData.setEmail(txtEmail.getText().trim());
        currentUserData.setUserId(txtIdTaiKhoan.getText().trim());
        currentUserData.setCardCode(txtMaThe.getText().trim());

        NhanVienComboItem selNv = (NhanVienComboItem) cbNhanVien.getSelectedItem();
        currentUserData.setdNhanVienId(selNv != null ? selNv.getId() : null);

        GroupComboItem selGroup = (GroupComboItem) cbNhomNguoiDung.getSelectedItem();
        currentUserData.setGroupUserId(selGroup != null && selGroup.getGroup() != null ? selGroup.getGroup().getId() : null);

        List<String> selectedStoreIds = modelCuaHang.getSelectedStoreIds();

        boolean ok;
        if (isEditMode) {
            ok = userDAO.updateUser(currentUserData, selectedStoreIds);
        } else {
            ok = userDAO.insertUser(currentUserData, selectedStoreIds);
        }

        if (ok) {
            isSaved = true;
            if (isNewAfter) {
                initNewForm();
            } else if (isCloseAfter) {
                dispose();
            } else {
                isEditMode = true;
                JOptionPane.showMessageDialog(this, "Lưu thông tin tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu tài khoản vào CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return isSaved;
    }

    public SUser getCurrentUserData() {
        return currentUserData;
    }

    // Table Model for Stores
    private static class StoreTableModel extends DefaultTableModel {
        private final String[] columnNames = {"Cửa hàng", "Truy cập"};
        private List<UserStoreAccess> list = new ArrayList<>();

        public void setData(List<UserStoreAccess> data) {
            this.list = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        public List<String> getSelectedStoreIds() {
            List<String> res = new ArrayList<>();
            for (UserStoreAccess usa : list) {
                if (usa.isAccessed()) {
                    res.add(usa.getStoreId());
                }
            }
            return res;
        }

        @Override
        public int getRowCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 1 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            UserStoreAccess item = list.get(rowIndex);
            if (columnIndex == 0) return item.getStoreName();
            if (columnIndex == 1) return item.isAccessed();
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1 && aValue instanceof Boolean) {
                list.get(rowIndex).setAccessed((Boolean) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
