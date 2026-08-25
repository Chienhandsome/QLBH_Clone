package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class NhomMatHangDialog extends JDialog {
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final NhomMatHang currentNhom;
    private final Runnable onSaveCallback;

    private JTextField txtCode;
    private JTextField txtName;
    private JTextField txtNote;
    private JComboBox<String> cbIcon;

    public NhomMatHangDialog(Component parent, NhomMatHang nhom, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), nhom.getId() == null ? "Thêm Nhóm Mặt Hàng" : "Chỉnh Sửa Nhóm Mặt Hàng", ModalityType.APPLICATION_MODAL);
        this.currentNhom = nhom;
        this.onSaveCallback = onSaveCallback;

        initUI();
        loadData();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);

        // 1. Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));
        JLabel lblIcon = new JLabel(IconHelper.getIcon("Folder.png", 28, 28));
        JLabel lblTitle = new JLabel(currentNhom.getId() == null ? "Thêm mới Nhóm Mặt Hàng" : "Chỉnh sửa thông tin Nhóm Mặt Hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 55, 105));
        header.add(lblIcon);
        header.add(lblTitle);
        add(header, BorderLayout.NORTH);

        // 2. Form Content
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Mã nhóm
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Mã nhóm:"), gbc);
        txtCode = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 0;
        form.add(txtCode, gbc);

        // Tên nhóm
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Tên nhóm (*):"), gbc);
        txtName = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 1;
        form.add(txtName, gbc);

        // Biểu tượng
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Biểu tượng:"), gbc);
        cbIcon = new JComboBox<>(new String[]{"Thư mục (Folder)", "Ngôi sao / Thẻ vàng (Star)"});
        gbc.gridx = 1; gbc.gridy = 2;
        form.add(cbIcon, gbc);

        // Ghi chú
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Ghi chú:"), gbc);
        txtNote = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 3;
        form.add(txtNote, gbc);

        add(form, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(240, 244, 250));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 225, 240)));

        JButton btnSave = new JButton("Lưu dữ liệu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(new Color(25, 135, 84));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Bỏ qua");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadData() {
        if (currentNhom.getId() != null) {
            txtCode.setText(currentNhom.getCode());
            txtName.setText(currentNhom.getName());
            txtNote.setText(currentNhom.getGhiChu());
            if ("asterisk-yellow.png".equalsIgnoreCase(currentNhom.getIconName())) {
                cbIcon.setSelectedIndex(1);
            } else {
                cbIcon.setSelectedIndex(0);
            }
        } else {
            txtCode.setText("NH" + (System.currentTimeMillis() % 1000));
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên nhóm mặt hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        currentNhom.setCode(txtCode.getText().trim());
        currentNhom.setName(name);
        currentNhom.setGhiChu(txtNote.getText().trim());
        currentNhom.setIconName(cbIcon.getSelectedIndex() == 1 ? "asterisk-yellow.png" : "Folder.png");

        boolean ok;
        if (currentNhom.getId() == null || currentNhom.getId().isEmpty()) {
            ok = nhomMatHangDAO.insert(currentNhom);
        } else {
            ok = nhomMatHangDAO.update(currentNhom);
        }

        if (ok) {
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu nhóm mặt hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
