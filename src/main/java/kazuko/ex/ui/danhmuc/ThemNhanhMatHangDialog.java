package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.DonViTinhDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.DonViTinh;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThemNhanhMatHangDialog extends JDialog {
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final DonViTinhDAO donViTinhDAO = new DonViTinhDAO();
    private final SystemConfig systemConfig;

    private final Runnable onSuccessCallback;

    private String labelGia2 = "Đơn giá 2";
    private String labelGia3 = "Giá bán 3";
    private String labelGia4 = "Giá bán 4";
    private String[] gridColumns;

    private JTable tableGrid;
    private DefaultTableModel tableModel;
    private JSpinner spSoDong;

    // Các thuộc tính chung (Right Panel)
    private JCheckBox chkMaHang;
    private JTextField txtChungMaHang;

    private JCheckBox chkTenHang;
    private JTextField txtChungTenHang;

    private JCheckBox chkNhomHang;
    private JComboBox<String> cbChungNhomHang;

    private JCheckBox chkDVT;
    private JComboBox<String> cbChungDVT;

    private JCheckBox chkGiaBan;
    private JTextField txtChungGiaBan;

    private JCheckBox chkGiaNhap;
    private JTextField txtChungGiaNhap;

    private JCheckBox chkGiamGiaPt;
    private JTextField txtChungGiamGiaPt;

    private JCheckBox chkGiamGiaTien;
    private JTextField txtChungGiamGiaTien;

    private JCheckBox chkGhiChu;
    private JTextField txtChungGhiChu;

    private JCheckBox chkKhongIn;
    private JCheckBox chkChungKhongIn;

    private JCheckBox chkBaoHanh;
    private JTextField txtChungBaoHanh;

    private JCheckBox chkDVTChan;
    private JComboBox<String> cbChungDVTChan;

    private JCheckBox chkQuyDoi;
    private JTextField txtChungQuyDoi;

    private JCheckBox chkGiaBanChan;
    private JTextField txtChungGiaBanChan;

    private JCheckBox chkGia2;
    private JTextField txtChungGia2;

    private JCheckBox chkGia3;
    private JTextField txtChungGia3;

    private JCheckBox chkGia4;
    private JTextField txtChungGia4;

    private JCheckBox chkGiaVon;
    private JTextField txtChungGiaVon;

    private JCheckBox chkHangSX;
    private JComboBox<String> cbChungHangSX;

    private JCheckBox chkCoHan;
    private JCheckBox chkChungCoHan;

    private JCheckBox chkSize;
    private JTextField txtChungSize;

    private JCheckBox chkTonMin;
    private JTextField txtChungTonMin;

    private JCheckBox chkTonMax;
    private JTextField txtChungTonMax;

    private JCheckBox chkAnh;
    private JTextField txtChungAnh;

    private JCheckBox chkHoaHong;
    private JTextField txtChungHoaHong;

    private JCheckBox chkDoiTac;
    private JComboBox<String> cbChungDoiTac;

    public ThemNhanhMatHangDialog(Window owner, Runnable onSuccess) {
        super(owner, "Thêm nhanh", ModalityType.APPLICATION_MODAL);
        this.onSuccessCallback = onSuccess;
        this.systemConfig = new SystemConfigDAO().loadConfig();

        initGridColumns();
        initUI();
        loadComboData();
        addNewRows(1);

        setSize(1180, 680);
        setLocationRelativeTo(owner);
    }

    private void initGridColumns() {
        if (systemConfig != null) {
            if (systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().trim().isEmpty()) {
                labelGia2 = systemConfig.getDienGiaiGia2().trim();
            }
            if (systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().trim().isEmpty()) {
                labelGia3 = systemConfig.getDienGiaiGia3().trim();
            }
            if (systemConfig.getDienGiaiGia4() != null && !systemConfig.getDienGiaiGia4().trim().isEmpty()) {
                labelGia4 = systemConfig.getDienGiaiGia4().trim();
            }
        }

        List<String> list = new ArrayList<>();
        list.add("Mã hàng");
        list.add("Tên mặt hàng");
        list.add("Nhóm mặt hàng");
        list.add("Đơn vị tính");
        list.add("Giá bán");
        list.add("Giá nhập");
        list.add("Mặc định giảm giá");
        list.add("Mặc định giảm tiền");
        list.add("Ghi chú");
        list.add("Không in chế biến");
        list.add("Bảo hành");
        list.add("Đơn vị tính chẵn");
        list.add("Quy đổi");
        list.add("Giá bán chẵn");
        list.add(labelGia2);
        list.add(labelGia3);
        if (systemConfig != null && systemConfig.isSuDungGia4()) {
            list.add(labelGia4);
        }
        list.add("Giá vốn");
        list.add("Hãng sản xuất");
        list.add("Có hạn sử dụng");
        list.add("Size");
        list.add("Tồn tối thiểu");
        list.add("Tồn tối đa");
        list.add("Ảnh");
        list.add("Hoa hồng");
        list.add("Đối tác ký gửi");

        gridColumns = list.toArray(new String[0]);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 244, 250));

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        headerPanel.setBackground(new Color(215, 230, 248));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("Bookmark.png", 18, 18));
        if (lblIcon.getIcon() == null) lblIcon.setIcon(IconHelper.getIcon("1282529873_objects.png", 18, 18));
        JLabel lblBannerText = new JLabel("Mời bạn điền danh sách Mặt hàng vào lưới phía dưới.");
        lblBannerText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBannerText.setForeground(new Color(30, 40, 60));

        headerPanel.add(lblIcon);
        headerPanel.add(lblBannerText);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Main Body: SplitPane (Left: Toolbar + Table, Right: Common Properties)
        JPanel leftMainPanel = new JPanel(new BorderLayout());
        leftMainPanel.setOpaque(false);

        // 2.1 Left Toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolBar.setBackground(new Color(225, 236, 250));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(195, 212, 235)));

        JLabel lblSoDong = new JLabel("Số dòng");
        lblSoDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolBar.add(lblSoDong);

        spSoDong = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spSoDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spSoDong.setPreferredSize(new Dimension(50, 24));
        toolBar.add(spSoDong);

        JButton btnAdd = createToolButton("+ Thêm dữ liệu", "edit_add.png", new Color(0, 120, 50));
        btnAdd.addActionListener(e -> addNewRows((Integer) spSoDong.getValue()));
        toolBar.add(btnAdd);

        JButton btnDelete = createToolButton("Xóa dữ liệu", "cross.png", new Color(180, 20, 20));
        btnDelete.addActionListener(e -> deleteSelectedRows());
        toolBar.add(btnDelete);

        JButton btnPaste = createToolButton("Dán dữ liệu", "Report.png", new Color(20, 50, 100));
        btnPaste.addActionListener(e -> onPasteFromClipboard());
        toolBar.add(btnPaste);

        JButton btnExcel = createToolButton("Chọn file Excel", "Bookmark.png", new Color(20, 100, 40));
        btnExcel.addActionListener(e -> onOpenExcelChooser());
        toolBar.add(btnExcel);

        leftMainPanel.add(toolBar, BorderLayout.NORTH);

        // 2.2 Table Grid
        tableModel = new DefaultTableModel(gridColumns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                String cName = getColumnName(columnIndex);
                if ("Không in chế biến".equals(cName) || "Có hạn sử dụng".equals(cName)) {
                    return Boolean.class;
                }
                return Object.class;
            }
        };

        tableGrid = new JTable(tableModel);
        tableGrid.setRowHeight(22);
        tableGrid.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableGrid.setShowGrid(true);
        tableGrid.setGridColor(new Color(220, 228, 238));
        tableGrid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader th = tableGrid.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(230, 238, 248));
        th.setPreferredSize(new Dimension(100, 26));

        // Setup column widths
        for (int i = 0; i < tableGrid.getColumnCount(); i++) {
            String name = tableGrid.getColumnName(i);
            if ("Tên mặt hàng".equals(name)) tableGrid.getColumnModel().getColumn(i).setPreferredWidth(180);
            else if ("Nhóm mặt hàng".equals(name) || "Ghi chú".equals(name)) tableGrid.getColumnModel().getColumn(i).setPreferredWidth(130);
            else if ("Mã hàng".equals(name) || "Đơn vị tính".equals(name)) tableGrid.getColumnModel().getColumn(i).setPreferredWidth(90);
            else if (name.contains("Giá") || name.contains("tiền") || name.contains("giá") || name.contains("haha")) tableGrid.getColumnModel().getColumn(i).setPreferredWidth(95);
            else tableGrid.getColumnModel().getColumn(i).setPreferredWidth(100);
        }

        JScrollPane scrollTable = new JScrollPane(tableGrid);
        scrollTable.setBorder(new LineBorder(new Color(190, 205, 225)));
        leftMainPanel.add(scrollTable, BorderLayout.CENTER);

        // 2.3 Right Panel: Các thuộc tính chung
        JPanel rightPanel = createRightPropertiesPanel();
        JScrollPane scrollRight = new JScrollPane(rightPanel);
        scrollRight.setPreferredSize(new Dimension(320, 500));
        scrollRight.setBorder(new MatteBorder(0, 1, 0, 0, new Color(190, 205, 225)));
        scrollRight.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMainPanel, scrollRight);
        splitPane.setResizeWeight(0.72);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // 3. Footer Buttons Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(new Color(230, 238, 248));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(200, 215, 230)));

        JButton btnAccept = new JButton("Chấp nhận");
        btnAccept.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAccept.setPreferredSize(new Dimension(95, 26));
        btnAccept.addActionListener(e -> onSaveAll());
        footerPanel.add(btnAccept);

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(85, 26));
        btnCancel.addActionListener(e -> dispose());
        footerPanel.add(btnCancel);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createRightPropertiesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 248, 253));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        // Header Title in Red Bold
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel lblTitle = new JLabel("Các thuộc tính chung");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(210, 30, 30));
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        // 1. Mã hàng (Unchecked by default)
        chkMaHang = new JCheckBox();
        txtChungMaHang = new JTextField();
        addPropertyRow(panel, gbc, row++, chkMaHang, false, "Mã hàng:", txtChungMaHang);

        // 2. Tên mặt hàng (Checked, light yellow)
        chkTenHang = new JCheckBox();
        txtChungTenHang = new JTextField();
        txtChungTenHang.setBackground(new Color(255, 255, 220));
        addPropertyRow(panel, gbc, row++, chkTenHang, true, "Tên mặt hàng:", txtChungTenHang);

        // 3. Nhóm mặt hàng
        chkNhomHang = new JCheckBox();
        cbChungNhomHang = new JComboBox<>();
        addPropertyRow(panel, gbc, row++, chkNhomHang, true, "Nhóm mặt hàng:", cbChungNhomHang);

        // 4. Đơn vị tính
        chkDVT = new JCheckBox();
        cbChungDVT = new JComboBox<>();
        addPropertyRow(panel, gbc, row++, chkDVT, true, "Đơn vị tính:", cbChungDVT);

        // 5. Giá bán
        chkGiaBan = new JCheckBox();
        txtChungGiaBan = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGiaBan, true, "Giá bán:", txtChungGiaBan);

        // 6. Giá nhập
        chkGiaNhap = new JCheckBox();
        txtChungGiaNhap = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGiaNhap, true, "Giá nhập:", txtChungGiaNhap);

        // 7. Mặc định giảm giá
        chkGiamGiaPt = new JCheckBox();
        txtChungGiamGiaPt = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGiamGiaPt, true, "Mặc định giảm giá:", txtChungGiamGiaPt);

        // 8. Mặc định giảm tiền
        chkGiamGiaTien = new JCheckBox();
        txtChungGiamGiaTien = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGiamGiaTien, true, "Mặc định giảm tiền:", txtChungGiamGiaTien);

        // 9. Ghi chú
        chkGhiChu = new JCheckBox();
        txtChungGhiChu = new JTextField();
        addPropertyRow(panel, gbc, row++, chkGhiChu, true, "Ghi chú:", txtChungGhiChu);

        // 10. Không in chế biến
        chkKhongIn = new JCheckBox();
        chkChungKhongIn = new JCheckBox();
        chkChungKhongIn.setOpaque(false);
        addPropertyRow(panel, gbc, row++, chkKhongIn, true, "Không in chế biến:", chkChungKhongIn);

        // 11. Bảo hành
        chkBaoHanh = new JCheckBox();
        txtChungBaoHanh = new JTextField();
        addPropertyRow(panel, gbc, row++, chkBaoHanh, true, "Bảo hành:", txtChungBaoHanh);

        // 12. Đơn vị tính chẵn
        chkDVTChan = new JCheckBox();
        cbChungDVTChan = new JComboBox<>();
        addPropertyRow(panel, gbc, row++, chkDVTChan, true, "Đơn vị tính chẵn:", cbChungDVTChan);

        // 13. Quy đổi
        chkQuyDoi = new JCheckBox();
        txtChungQuyDoi = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkQuyDoi, true, "Quy đổi:", txtChungQuyDoi);

        // 14. Giá bán chẵn
        chkGiaBanChan = new JCheckBox();
        txtChungGiaBanChan = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGiaBanChan, true, "Giá bán chẵn:", txtChungGiaBanChan);

        // 15. Giá 2
        chkGia2 = new JCheckBox();
        txtChungGia2 = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGia2, true, labelGia2 + ":", txtChungGia2);

        // 16. Giá 3
        chkGia3 = new JCheckBox();
        txtChungGia3 = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGia3, true, labelGia3 + ":", txtChungGia3);

        // 17. Giá 4 (nếu có)
        if (systemConfig != null && systemConfig.isSuDungGia4()) {
            chkGia4 = new JCheckBox();
            txtChungGia4 = new JTextField("0");
            addPropertyRow(panel, gbc, row++, chkGia4, true, labelGia4 + ":", txtChungGia4);
        }

        // Giá vốn
        chkGiaVon = new JCheckBox();
        txtChungGiaVon = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkGiaVon, true, "Giá vốn:", txtChungGiaVon);

        // Hãng sản xuất
        chkHangSX = new JCheckBox();
        cbChungHangSX = new JComboBox<>();
        cbChungHangSX.setEditable(true);
        addPropertyRow(panel, gbc, row++, chkHangSX, true, "Hãng sản xuất:", cbChungHangSX);

        // Có hạn sử dụng
        chkCoHan = new JCheckBox();
        chkChungCoHan = new JCheckBox();
        chkChungCoHan.setOpaque(false);
        addPropertyRow(panel, gbc, row++, chkCoHan, true, "Có hạn sử dụng:", chkChungCoHan);

        // Size
        chkSize = new JCheckBox();
        txtChungSize = new JTextField();
        addPropertyRow(panel, gbc, row++, chkSize, true, "Size:", txtChungSize);

        // Tồn tối thiểu
        chkTonMin = new JCheckBox();
        txtChungTonMin = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkTonMin, true, "Tồn tối thiểu:", txtChungTonMin);

        // Tồn tối đa
        chkTonMax = new JCheckBox();
        txtChungTonMax = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkTonMax, true, "Tồn tối đa:", txtChungTonMax);

        // Ảnh
        chkAnh = new JCheckBox();
        txtChungAnh = new JTextField();
        addPropertyRow(panel, gbc, row++, chkAnh, true, "Ảnh:", txtChungAnh);

        // Hoa hồng
        chkHoaHong = new JCheckBox();
        txtChungHoaHong = new JTextField("0");
        addPropertyRow(panel, gbc, row++, chkHoaHong, true, "Hoa hồng:", txtChungHoaHong);

        // Đối tác ký gửi
        chkDoiTac = new JCheckBox();
        cbChungDoiTac = new JComboBox<>();
        cbChungDoiTac.setEditable(true);
        addPropertyRow(panel, gbc, row++, chkDoiTac, true, "Đối tác ký gửi:", cbChungDoiTac);

        return panel;
    }

    private void addPropertyRow(JPanel panel, GridBagConstraints gbc, int row, JCheckBox chk, boolean isChecked, String labelText, JComponent inputComp) {
        chk.setSelected(isChecked);
        chk.setOpaque(false);

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(chk, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(lbl, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        inputComp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        inputComp.setPreferredSize(new Dimension(110, 22));
        panel.add(inputComp, gbc);
    }

    private JButton createToolButton(String text, String iconName, Color fg) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            Icon icon = IconHelper.getIcon(iconName, 16, 16);
            if (icon != null) btn.setIcon(icon);
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(fg);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(185, 205, 230)));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 8, 26));
        return btn;
    }

    private void loadComboData() {
        // Nhóm mặt hàng
        cbChungNhomHang.removeAllItems();
        cbChungNhomHang.addItem("");
        List<NhomMatHang> nhomList = nhomMatHangDAO.getAll();
        if (nhomList != null) {
            for (NhomMatHang n : nhomList) {
                if (!n.isFolder() && n.getName() != null) {
                    cbChungNhomHang.addItem(n.getName());
                }
            }
        }

        // Đơn vị tính
        cbChungDVT.removeAllItems();
        cbChungDVT.addItem("");
        cbChungDVTChan.removeAllItems();
        cbChungDVTChan.addItem("");
        List<DonViTinh> dvtList = donViTinhDAO.getAll();
        if (dvtList != null) {
            for (DonViTinh d : dvtList) {
                if (d.getName() != null) {
                    cbChungDVT.addItem(d.getName());
                    cbChungDVTChan.addItem(d.getName());
                }
            }
        }
    }

    private void addNewRows(int count) {
        for (int i = 0; i < count; i++) {
            Object[] row = new Object[gridColumns.length];
            for (int c = 0; c < gridColumns.length; c++) {
                String col = gridColumns[c];
                row[c] = getDefaultPropertyForColumn(col);
            }
            tableModel.addRow(row);
        }
    }

    private void deleteSelectedRows() {
        int[] rows = tableGrid.getSelectedRows();
        if (rows.length > 0) {
            for (int i = rows.length - 1; i >= 0; i--) {
                tableModel.removeRow(rows[i]);
            }
        } else if (tableModel.getRowCount() > 0) {
            tableModel.removeRow(tableModel.getRowCount() - 1);
        }
    }

    private void onPasteFromClipboard() {
        try {
            String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (data == null || data.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bộ nhớ tạm (Clipboard) không có dữ liệu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] lines = data.split("\r?\n");
            int addedCount = 0;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] cells = line.split("\t", -1);
                Object[] row = new Object[gridColumns.length];

                for (int c = 0; c < gridColumns.length; c++) {
                    if (c < cells.length && !cells[c].trim().isEmpty()) {
                        String val = cells[c].trim();
                        if ("Không in chế biến".equals(gridColumns[c]) || "Có hạn sử dụng".equals(gridColumns[c])) {
                            row[c] = "true".equalsIgnoreCase(val) || "1".equals(val) || "x".equalsIgnoreCase(val) || "có".equalsIgnoreCase(val);
                        } else {
                            row[c] = val;
                        }
                    } else {
                        row[c] = getDefaultPropertyForColumn(gridColumns[c]);
                    }
                }
                tableModel.addRow(row);
                addedCount++;
            }

            JOptionPane.showMessageDialog(this, "Đã dán thành công " + addedCount + " dòng từ bộ nhớ tạm!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi dán dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object getDefaultPropertyForColumn(String col) {
        if ("Mã hàng".equals(col)) return chkMaHang.isSelected() ? txtChungMaHang.getText().trim() : "";
        if ("Tên mặt hàng".equals(col)) return chkTenHang.isSelected() ? txtChungTenHang.getText().trim() : "";
        if ("Nhóm mặt hàng".equals(col)) return chkNhomHang.isSelected() && cbChungNhomHang.getSelectedItem() != null ? cbChungNhomHang.getSelectedItem().toString() : "";
        if ("Đơn vị tính".equals(col)) return chkDVT.isSelected() && cbChungDVT.getSelectedItem() != null ? cbChungDVT.getSelectedItem().toString() : "";
        if ("Giá bán".equals(col)) return chkGiaBan.isSelected() ? txtChungGiaBan.getText().trim() : "0";
        if ("Giá nhập".equals(col)) return chkGiaNhap.isSelected() ? txtChungGiaNhap.getText().trim() : "0";
        if ("Mặc định giảm giá".equals(col)) return chkGiamGiaPt.isSelected() ? txtChungGiamGiaPt.getText().trim() : "0";
        if ("Mặc định giảm tiền".equals(col)) return chkGiamGiaTien.isSelected() ? txtChungGiamGiaTien.getText().trim() : "0";
        if ("Ghi chú".equals(col)) return chkGhiChu.isSelected() ? txtChungGhiChu.getText().trim() : "";
        if ("Không in chế biến".equals(col)) return chkKhongIn.isSelected() && chkChungKhongIn.isSelected();
        if ("Bảo hành".equals(col)) return chkBaoHanh.isSelected() ? txtChungBaoHanh.getText().trim() : "";
        if ("Đơn vị tính chẵn".equals(col)) return chkDVTChan.isSelected() && cbChungDVTChan.getSelectedItem() != null ? cbChungDVTChan.getSelectedItem().toString() : "";
        if ("Quy đổi".equals(col)) return chkQuyDoi.isSelected() ? txtChungQuyDoi.getText().trim() : "0";
        if ("Giá bán chẵn".equals(col)) return chkGiaBanChan.isSelected() ? txtChungGiaBanChan.getText().trim() : "0";
        if (labelGia2.equals(col)) return chkGia2.isSelected() ? txtChungGia2.getText().trim() : "0";
        if (labelGia3.equals(col)) return chkGia3.isSelected() ? txtChungGia3.getText().trim() : "0";
        if (labelGia4.equals(col) && chkGia4 != null) return chkGia4.isSelected() ? txtChungGia4.getText().trim() : "0";
        if ("Giá vốn".equals(col)) return chkGiaVon.isSelected() ? txtChungGiaVon.getText().trim() : "0";
        if ("Hãng sản xuất".equals(col)) return chkHangSX.isSelected() && cbChungHangSX.getSelectedItem() != null ? cbChungHangSX.getSelectedItem().toString() : "";
        if ("Có hạn sử dụng".equals(col)) return chkCoHan.isSelected() && chkChungCoHan.isSelected();
        if ("Size".equals(col)) return chkSize.isSelected() ? txtChungSize.getText().trim() : "";
        if ("Tồn tối thiểu".equals(col)) return chkTonMin.isSelected() ? txtChungTonMin.getText().trim() : "0";
        if ("Tồn tối đa".equals(col)) return chkTonMax.isSelected() ? txtChungTonMax.getText().trim() : "0";
        if ("Ảnh".equals(col)) return chkAnh.isSelected() ? txtChungAnh.getText().trim() : "";
        if ("Hoa hồng".equals(col)) return chkHoaHong.isSelected() ? txtChungHoaHong.getText().trim() : "0";
        if ("Đối tác ký gửi".equals(col)) return chkDoiTac.isSelected() && cbChungDoiTac.getSelectedItem() != null ? cbChungDoiTac.getSelectedItem().toString() : "";
        return "";
    }

    private void onOpenExcelChooser() {
        XuatMauVaChonExcelDialog dialog = new XuatMauVaChonExcelDialog(
                this,
                this::applyImportedMappedRows
        );
        dialog.setVisible(true);
    }

    public void applyImportedMappedRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return;

        int count = 0;
        for (Map<String, Object> map : rows) {
            Object[] row = new Object[gridColumns.length];
            for (int c = 0; c < gridColumns.length; c++) {
                String col = gridColumns[c];
                if (map.containsKey(col)) {
                    row[c] = map.get(col);
                } else if (labelGia2.equals(col) && map.containsKey("Đơn giá 2")) {
                    row[c] = map.get("Đơn giá 2");
                } else if (labelGia3.equals(col) && map.containsKey("giá blabla")) {
                    row[c] = map.get("giá blabla");
                } else if (labelGia3.equals(col) && map.containsKey("Giá bán 3")) {
                    row[c] = map.get("Giá bán 3");
                } else {
                    row[c] = getDefaultPropertyForColumn(col);
                }
            }
            tableModel.addRow(row);
            count++;
        }
        JOptionPane.showMessageDialog(this, "Đã nạp thành công " + count + " dòng từ file Excel vào lưới!", "Nạp Excel thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSaveAll() {
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu mặt hàng nào để thêm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<MatHang> toInsert = new ArrayList<>();

        for (int r = 0; r < rowCount; r++) {
            String name = getCellValue(r, "Tên mặt hàng");
            if (name.isEmpty()) {
                continue;
            }

            MatHang mh = new MatHang();
            mh.setName(name);

            String code = getCellValue(r, "Mã hàng");
            if (code.isEmpty()) {
                code = matHangDAO.generateNextProductCode("MH");
            }
            mh.setCode(code);

            mh.setTenNhom(getCellValue(r, "Nhóm mặt hàng"));
            mh.setTenDonViTinh(getCellValue(r, "Đơn vị tính"));
            mh.setTenDonViTinhChan(getCellValue(r, "Đơn vị tính chẵn"));

            mh.setGiaBan(parseDouble(getCellValue(r, "Giá bán")));
            mh.setGiaNhap(parseDouble(getCellValue(r, "Giá nhập")));
            mh.setGiaVon(parseDouble(getCellValue(r, "Giá vốn")));
            mh.setGiamGiaPt(parseDouble(getCellValue(r, "Mặc định giảm giá")));
            mh.setGiamGiaTien(parseDouble(getCellValue(r, "Mặc định giảm tiền")));
            mh.setQuyDoi(parseDouble(getCellValue(r, "Quy đổi")));
            mh.setGiaBanChan(parseDouble(getCellValue(r, "Giá bán chẵn")));
            mh.setGiaBan2(parseDouble(getCellValue(r, labelGia2)));
            mh.setGiaBan3(parseDouble(getCellValue(r, labelGia3)));
            if (systemConfig != null && systemConfig.isSuDungGia4()) {
                mh.setGiaBan4(parseDouble(getCellValue(r, labelGia4)));
            }

            mh.setGhiChu(getCellValue(r, "Ghi chú"));
            mh.setBaoHanh(getCellValue(r, "Bảo hành"));
            mh.setHangSanXuat(getCellValue(r, "Hãng sản xuất"));
            mh.setKichThuoc(getCellValue(r, "Size"));
            mh.setImagePath(getCellValue(r, "Ảnh"));
            mh.setHoaHong(parseDouble(getCellValue(r, "Hoa hồng")));
            mh.setTonToiThieu(parseDouble(getCellValue(r, "Tồn tối thiểu")));
            mh.setTonToiDa(parseDouble(getCellValue(r, "Tồn tối đa")));
            mh.setDoiTacKyGui(getCellValue(r, "Đối tác ký gửi"));

            Object khongInObj = getRawCellValue(r, "Không in chế biến");
            mh.setKhongInCheBien(Boolean.TRUE.equals(khongInObj) || "true".equalsIgnoreCase(String.valueOf(khongInObj)));

            Object coHanObj = getRawCellValue(r, "Có hạn sử dụng");
            mh.setCoHanSuDung(Boolean.TRUE.equals(coHanObj) || "true".equalsIgnoreCase(String.valueOf(coHanObj)));

            toInsert.add(mh);
        }

        if (toInsert.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên mặt hàng cho ít nhất một dòng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int successCount = 0;
        for (MatHang mh : toInsert) {
            if (matHangDAO.insert(mh)) {
                successCount++;
            }
        }

        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }

        JOptionPane.showMessageDialog(
                this,
                "Đã thêm thành công " + successCount + " mặt hàng vào hệ thống!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
        );
        dispose();
    }

    private String getCellValue(int row, String colName) {
        int col = getColumnIndex(colName);
        if (col < 0) return "";
        Object val = tableModel.getValueAt(row, col);
        return val != null ? val.toString().trim() : "";
    }

    private Object getRawCellValue(int row, String colName) {
        int col = getColumnIndex(colName);
        if (col < 0) return null;
        return tableModel.getValueAt(row, col);
    }

    private int getColumnIndex(String colName) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (colName.equalsIgnoreCase(tableModel.getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }

    private double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.replace(",", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
