package kazuko.ex.ui.system;

import kazuko.ex.dao.GroupUserDAO;
import kazuko.ex.model.system.SGroupUser;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NhomNguoiDungDialog extends JDialog {
    private final GroupUserDAO groupUserDAO = new GroupUserDAO();
    private SGroupUser groupUser;
    private boolean isEditMode = false;
    private boolean isSaved = false;

    private JTextField txtName;
    private JComboBox<IconItem> cbIcon;
    private JTextArea txtNote;

    public static class IconItem {
        private final String code;
        private final String label;
        private final ImageIcon icon;

        public IconItem(String code, String label, String iconName) {
            this.code = code;
            this.label = label;
            this.icon = IconHelper.getIcon(iconName, 16, 16);
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }
        public ImageIcon getIcon() { return icon; }

        @Override
        public String toString() { return label; }
    }

    public NhomNguoiDungDialog(Window parent, SGroupUser groupUser) {
        super(parent, "Nhóm người dùng", ModalityType.APPLICATION_MODAL);
        this.groupUser = groupUser;
        this.isEditMode = (groupUser != null && groupUser.getId() != null);
        initComponents(parent);
    }

    private void initComponents(Window parent) {
        setSize(430, 270);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 235)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("users.png", 32, 32));
        String titleText = isEditMode ? "Chỉnh sửa nhóm người dùng" : "Thêm mới nhóm người dùng";
        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(20, 20, 20));

        headerPanel.add(lblIcon);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(12, 16, 8, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên nhóm người dùng
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblName = new JLabel("Tên nhóm người dùng");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblName, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setPreferredSize(new Dimension(220, 24));
        formPanel.add(txtName, gbc);

        // Ảnh
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel lblImg = new JLabel("Ảnh:");
        lblImg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblImg, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cbIcon = new JComboBox<>(new IconItem[]{
                new IconItem("users", "Người dùng", "users.png"),
                new IconItem("star", "Sao vàng", "asterisk-yellow.png"),
                new IconItem("ticket", "Vé / Phiếu", "btnCard.Icon.png"),
                new IconItem("package", "Hàng hóa / Kho", "Folder.png"),
                new IconItem("admin", "Quản lý", "property.png"),
                new IconItem("tag", "Nhãn", "bookmark-import.png")
        });
        cbIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbIcon.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof IconItem) {
                    IconItem item = (IconItem) value;
                    l.setText(item.getLabel());
                    l.setIcon(item.getIcon());
                }
                return l;
            }
        });
        cbIcon.setPreferredSize(new Dimension(220, 24));
        formPanel.add(cbIcon, gbc);

        // Ghi chú
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblNote = new JLabel("Ghi chú:");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblNote, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtNote = new JTextArea(3, 20);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        JScrollPane spNote = new JScrollPane(txtNote);
        spNote.setPreferredSize(new Dimension(220, 60));
        formPanel.add(spNote, gbc);

        add(formPanel, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footerPanel.setBackground(new Color(240, 244, 250));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 220, 235)));

        JButton btnSave = new JButton("Ghi dữ liệu", IconHelper.getIcon("Save1.png", 16, 16));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setPreferredSize(new Dimension(110, 28));
        btnSave.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Thoát", IconHelper.getIcon("Cancelled.png", 16, 16));
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(90, 28));
        btnCancel.addActionListener(e -> dispose());

        footerPanel.add(btnSave);
        footerPanel.add(btnCancel);
        add(footerPanel, BorderLayout.SOUTH);

        // Populate data if edit
        if (isEditMode && groupUser != null) {
            txtName.setText(groupUser.getName());
            txtNote.setText(groupUser.getNote());
            if (groupUser.getSimageId() != null) {
                for (int i = 0; i < cbIcon.getItemCount(); i++) {
                    if (cbIcon.getItemAt(i).getCode().equalsIgnoreCase(groupUser.getSimageId())) {
                        cbIcon.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhóm người dùng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        if (groupUser == null) {
            groupUser = new SGroupUser();
        }
        groupUser.setName(name);
        groupUser.setNote(txtNote.getText().trim());
        IconItem selIcon = (IconItem) cbIcon.getSelectedItem();
        groupUser.setSimageId(selIcon != null ? selIcon.getCode() : "users");

        boolean ok;
        if (isEditMode) {
            ok = groupUserDAO.updateGroup(groupUser);
        } else {
            ok = groupUserDAO.insertGroup(groupUser);
        }

        if (ok) {
            isSaved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu nhóm người dùng vào CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return isSaved;
    }

    public SGroupUser getGroupUser() {
        return groupUser;
    }
}
