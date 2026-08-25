package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.DonViTinhDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.DonViTinh;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class MatHangEditDialog extends JDialog {
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final DonViTinhDAO donViTinhDAO = new DonViTinhDAO();

    private final MatHang currentItem;
    private final Runnable onSaveCallback;

    private JTextField txtCode;
    private JTextField txtName;
    private JComboBox<NhomMatHang> cbNhomHang;
    private JComboBox<DonViTinh> cbDonViTinh;
    private JTextField txtGiaBan;
    private JTextField txtGiaNhap;
    private JTextField txtGiaVon;
    private JTextField txtGiamGiaPt;
    private JTextField txtGiamGiaTien;
    private JTextField txtNote;
    private JCheckBox chkKhongInCheBien;
    private JCheckBox chkBanNhanh;

    public MatHangEditDialog(Component parent, MatHang item, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), item.getId() == null ? "Thêm Mặt Hàng" : "Chỉnh Sửa Mặt Hàng", ModalityType.APPLICATION_MODAL);
        this.currentItem = item;
        this.onSaveCallback = onSaveCallback;

        initUI();
        loadData();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        setPreferredSize(new Dimension(480, 480));

        // 1. Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));
        JLabel lblIcon = new JLabel(IconHelper.getIcon("1282529873_objects.png", 28, 28));
        JLabel lblTitle = new JLabel(currentItem.getId() == null ? "Thêm mới Mặt Hàng" : "Chỉnh sửa thông tin Mặt Hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 55, 105));
        header.add(lblIcon);
        header.add(lblTitle);
        add(header, BorderLayout.NORTH);

        // 2. Form Content
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 18, 12, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Row 0: Mã hàng
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Mã hàng (*):"), gbc);
        txtCode = new JTextField(15);
        txtCode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        form.add(txtCode, gbc);

        // Row 1: Tên hàng
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Tên mặt hàng (*):"), gbc);
        txtName = new JTextField(20);
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        form.add(txtName, gbc);

        // Row 2: Nhóm hàng
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("Nhóm mặt hàng:"), gbc);
        cbNhomHang = new JComboBox<>();
        cbNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        form.add(cbNhomHang, gbc);

        // Row 3: Đơn vị tính
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Đơn vị tính:"), gbc);
        cbDonViTinh = new JComboBox<>();
        cbDonViTinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        form.add(cbDonViTinh, gbc);

        // Row 4: Giá bán
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        form.add(new JLabel("Giá bán (VNĐ):"), gbc);
        txtGiaBan = new JTextField("0", 15);
        txtGiaBan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtGiaBan.setForeground(new Color(0, 100, 40));
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        form.add(txtGiaBan, gbc);

        // Row 5: Giá nhập & Giá vốn
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        form.add(new JLabel("Giá nhập (VNĐ):"), gbc);
        txtGiaNhap = new JTextField("0", 15);
        txtGiaNhap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        form.add(txtGiaNhap, gbc);

        // Row 6: Mặc định giảm giá (% và Tiền)
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        form.add(new JLabel("Giảm giá (% / Tiền):"), gbc);

        JPanel discountPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        discountPanel.setOpaque(false);
        txtGiamGiaPt = new JTextField("0");
        txtGiamGiaPt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiamGiaTien = new JTextField("0");
        txtGiamGiaTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        discountPanel.add(txtGiamGiaPt);
        discountPanel.add(txtGiamGiaTien);

        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        form.add(discountPanel, gbc);

        // Row 7: Ghi chú
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        form.add(new JLabel("Ghi chú:"), gbc);
        txtNote = new JTextField(20);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 1.0;
        form.add(txtNote, gbc);

        // Row 8: Checkboxes (Không in chế biến, Bán nhanh)
        gbc.gridx = 1; gbc.gridy = 8; gbc.weightx = 1.0;
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        checkPanel.setOpaque(false);
        chkKhongInCheBien = new JCheckBox("Không in chế biến");
        chkKhongInCheBien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKhongInCheBien.setOpaque(false);

        chkBanNhanh = new JCheckBox("Bán nhanh");
        chkBanNhanh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkBanNhanh.setOpaque(false);

        checkPanel.add(chkKhongInCheBien);
        checkPanel.add(chkBanNhanh);
        form.add(checkPanel, gbc);

        add(form, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(240, 244, 250));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 225, 240)));

        JButton btnSave = new JButton("Lưu dữ liệu (F10)");
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
        // Load ComboBox Nhóm hàng
        cbNhomHang.removeAllItems();
        List<NhomMatHang> nhomList = nhomMatHangDAO.getAll();
        NhomMatHang selectedNhom = null;
        for (NhomMatHang nh : nhomList) {
            cbNhomHang.addItem(nh);
            if (currentItem.getNhomMatHangId() != null && currentItem.getNhomMatHangId().equals(nh.getId())) {
                selectedNhom = nh;
            } else if (currentItem.getTenNhom() != null && currentItem.getTenNhom().equalsIgnoreCase(nh.getName())) {
                selectedNhom = nh;
            }
        }
        if (selectedNhom != null) {
            cbNhomHang.setSelectedItem(selectedNhom);
        }

        // Load ComboBox Đơn vị tính
        cbDonViTinh.removeAllItems();
        List<DonViTinh> dvtList = donViTinhDAO.getAll();
        DonViTinh selectedDvt = null;
        for (DonViTinh dv : dvtList) {
            cbDonViTinh.addItem(dv);
            if (currentItem.getDonViTinhId() != null && currentItem.getDonViTinhId().equals(dv.getId())) {
                selectedDvt = dv;
            } else if (currentItem.getTenDonViTinh() != null && currentItem.getTenDonViTinh().equalsIgnoreCase(dv.getName())) {
                selectedDvt = dv;
            }
        }
        if (selectedDvt != null) {
            cbDonViTinh.setSelectedItem(selectedDvt);
        }

        // Event khi đổi Nhóm hàng -> Tự gen mã theo quy tắc và áp dụng quy định giảm giá
        cbNhomHang.addActionListener(e -> {
            NhomMatHang nh = (NhomMatHang) cbNhomHang.getSelectedItem();
            if (nh != null) {
                if (currentItem.getId() == null) {
                    String prefix = nh.getCode() != null && !nh.getCode().trim().isEmpty() ? nh.getCode().trim() : "MH";
                    txtCode.setText(matHangDAO.generateNextProductCode(prefix));
                }
                if (nh.isKhongChoPhepGiamGia()) {
                    txtGiamGiaPt.setText("0");
                    txtGiamGiaPt.setEnabled(false);
                    txtGiamGiaTien.setText("0");
                    txtGiamGiaTien.setEnabled(false);
                    txtGiamGiaPt.setToolTipText("Nhóm này không cho phép giảm giá khi bán hàng");
                } else {
                    txtGiamGiaPt.setEnabled(true);
                    txtGiamGiaTien.setEnabled(true);
                    txtGiamGiaPt.setToolTipText(null);
                }
            }
        });

        // Load fields
        if (currentItem.getId() != null) {
            txtCode.setText(currentItem.getCode());
            txtName.setText(currentItem.getName());
            txtGiaBan.setText(new DecimalFormat("#,##0.##").format(currentItem.getGiaBan()));
            txtGiaNhap.setText(new DecimalFormat("#,##0.##").format(currentItem.getGiaNhap()));
            txtGiamGiaPt.setText(String.valueOf(currentItem.getGiamGiaPt()));
            txtGiamGiaTien.setText(new DecimalFormat("#,##0.##").format(currentItem.getGiamGiaTien()));
            txtNote.setText(currentItem.getGhiChu());
            chkKhongInCheBien.setSelected(currentItem.isKhongInCheBien());
            chkBanNhanh.setSelected(currentItem.isBanNhanh());
        } else {
            NhomMatHang nh = (NhomMatHang) cbNhomHang.getSelectedItem();
            String prefix = nh != null && nh.getCode() != null && !nh.getCode().trim().isEmpty() ? nh.getCode().trim() : "MH";
            txtCode.setText(matHangDAO.generateNextProductCode(prefix));
        }
    }

    private void onSave() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã mặt hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtCode.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên mặt hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        double giaBan = parseNumber(txtGiaBan.getText());
        double giaNhap = parseNumber(txtGiaNhap.getText());
        double giamPt = parseNumber(txtGiamGiaPt.getText());
        double giamTien = parseNumber(txtGiamGiaTien.getText());

        currentItem.setCode(code);
        currentItem.setName(name);
        currentItem.setGiaBan(giaBan);
        currentItem.setGiaNhap(giaNhap);
        currentItem.setGiaVon(giaNhap);
        currentItem.setGiamGiaPt(giamPt);
        currentItem.setGiamGiaTien(giamTien);
        currentItem.setGhiChu(txtNote.getText().trim());
        currentItem.setKhongInCheBien(chkKhongInCheBien.isSelected());
        currentItem.setBanNhanh(chkBanNhanh.isSelected());

        NhomMatHang nh = (NhomMatHang) cbNhomHang.getSelectedItem();
        if (nh != null) {
            currentItem.setNhomMatHangId(nh.getId());
            currentItem.setTenNhom(nh.getName());
        }

        DonViTinh dv = (DonViTinh) cbDonViTinh.getSelectedItem();
        if (dv != null) {
            currentItem.setDonViTinhId(dv.getId());
            currentItem.setTenDonViTinh(dv.getName());
        }

        boolean ok;
        if (currentItem.getId() == null || currentItem.getId().isEmpty()) {
            ok = matHangDAO.insert(currentItem);
        } else {
            ok = matHangDAO.update(currentItem);
        }

        if (ok) {
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu mặt hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parseNumber(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.replaceAll("[,. ]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
