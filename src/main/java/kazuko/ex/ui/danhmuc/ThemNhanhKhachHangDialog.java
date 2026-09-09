package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.IconHelper;
import kazuko.ex.util.KhachHangExcelUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.DefaultCellEditor;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ThemNhanhKhachHangDialog extends JDialog {
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final Runnable onSuccessCallback;

    private static final String[] GRID_COLUMNS = {
            "", // Pointer
            "Mã khách",
            "Tên khách hàng",
            "Địa chỉ",
            "Điện thoại",
            "Email",
            "Nhóm khách hàng",
            "Mã số thuế",
            "Nhân viên",
            "Ngày thành lập/sinh nhật",
            "Tỉnh thành",
            "Facebook",
            "Ghi chú",
            "Điểm tích lũy ban đầu"
    };

    private JTable tableGrid;
    private DefaultTableModel tableModel;
    private final List<TableColumn> allTableColumns = new ArrayList<>();
    private JSpinner spSoDong;

    // Right Panel: Các thuộc tính chung
    private JCheckBox chkMaKhach;
    private JTextField txtChungMaKhach;

    private JCheckBox chkTenKhach;
    private JTextField txtChungTenKhach;

    private JCheckBox chkDiaChi;
    private JTextField txtChungDiaChi;

    private JCheckBox chkDienThoai;
    private JTextField txtChungDienThoai;

    private JCheckBox chkEmail;
    private JTextField txtChungEmail;

    private JCheckBox chkNhomKhach;
    private JComboBox<String> cbChungNhomKhach;

    private JCheckBox chkMaSoThue;
    private JTextField txtChungMaSoThue;

    private JCheckBox chkNhanVien;
    private JComboBox<String> cbChungNhanVien;

    private JCheckBox chkNgaySinh;
    private JTextField txtChungNgaySinh;

    private JCheckBox chkTinhThanh;
    private JComboBox<String> cbChungTinhThanh;

    private JCheckBox chkFacebook;
    private JTextField txtChungFacebook;

    private JCheckBox chkGhiChu;
    private JTextField txtChungGhiChu;

    private JCheckBox chkDiemBanDau;
    private JTextField txtChungDiemBanDau;

    private final Map<String, String> staffMap = new LinkedHashMap<>();
    private final Map<String, String> cityMap = new LinkedHashMap<>();
    private final List<NhomKhachHang> groupList = new ArrayList<>();

    public ThemNhanhKhachHangDialog(Window owner, Runnable onSuccess) {
        super(owner, "Thêm nhanh", ModalityType.APPLICATION_MODAL);
        this.onSuccessCallback = onSuccess;

        loadMetaData();
        initUI();
        addNewRows(1);

        setSize(1180, 680);
        setLocationRelativeTo(owner);
    }

    private void loadMetaData() {
        staffMap.clear();
        staffMap.putAll(khachHangDAO.getAllNhanVienMap());

        cityMap.clear();
        cityMap.putAll(khachHangDAO.getAllTinhThanhMap());

        groupList.clear();
        groupList.addAll(nhomKhachHangDAO.getAll());
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(235, 240, 248));

        // 1. Top Banner
        JPanel topBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBanner.setBackground(new Color(220, 232, 248));
        topBanner.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("Folder.png", 22, 22));
        if (lblIcon.getIcon() == null) lblIcon.setIcon(IconHelper.getIcon("table_column.png", 20, 20));
        JLabel lblBannerTitle = new JLabel("Mời bạn điền danh sách Khách hàng vào lưới phía dưới.");
        lblBannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBannerTitle.setForeground(new Color(30, 45, 75));

        topBanner.add(lblIcon);
        topBanner.add(lblBannerTitle);
        add(topBanner, BorderLayout.NORTH);

        // 2. Main Center Split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(820);
        splitPane.setResizeWeight(0.72);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        // LEFT COMPONENT: Grid + Top toolbar
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);

        // Toolbar
        JPanel gridToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        gridToolbar.setBackground(new Color(242, 246, 252));
        gridToolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(200, 215, 230)));

        JLabel lblSoDong = new JLabel("Số dòng");
        lblSoDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spSoDong = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
        spSoDong.setPreferredSize(new Dimension(55, 25));
        spSoDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnAddRows = createToolButton("+ Thêm dữ liệu", "plus.png", e -> {
            int count = (int) spSoDong.getValue();
            addNewRows(count);
        });

        JButton btnDelRows = createToolButton("X Xóa dữ liệu", "cross.png", e -> deleteSelectedRows());
        JButton btnPaste = createToolButton("📋 Dán dữ liệu", "page_copy.png", e -> pasteFromClipboard());
        JButton btnChooseExcel = createToolButton("📊 Chọn file Excel", "page_white_excel.png", e -> openExcelImportDialog());

        gridToolbar.add(lblSoDong);
        gridToolbar.add(spSoDong);
        gridToolbar.add(btnAddRows);
        gridToolbar.add(btnDelRows);
        gridToolbar.add(btnPaste);
        gridToolbar.add(btnChooseExcel);
        leftPanel.add(gridToolbar, BorderLayout.NORTH);

        // Table Grid
        tableModel = new DefaultTableModel(GRID_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };

        tableGrid = new JTable(tableModel);
        tableGrid.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableGrid.setRowHeight(24);
        tableGrid.setShowGrid(true);
        tableGrid.setGridColor(new Color(225, 232, 240));
        tableGrid.setSelectionBackground(new Color(210, 230, 250));
        tableGrid.setSelectionForeground(Color.BLACK);
        tableGrid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader th = tableGrid.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(230, 238, 248));
        th.setPreferredSize(new Dimension(0, 28));

        // RIGHT COMPONENT: Các thuộc tính chung
        JPanel rightPanel = createRightCommonPanel();
        splitPane.setRightComponent(rightPanel);

        // Setup columns and visibility
        setupTableRenderersAndEditors();
        updateGridColumnsVisibility();

        JScrollPane gridScroll = new JScrollPane(tableGrid);
        gridScroll.setBorder(null);
        leftPanel.add(gridScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        add(splitPane, BorderLayout.CENTER);

        // 3. Bottom Actions Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(230, 238, 248));
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(200, 215, 230)));
        bottomPanel.setPreferredSize(new Dimension(0, 48));

        JPanel pnlBottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlBottomLeft.setOpaque(false);
        JButton btnDelError = new JButton("Xóa mặt hàng lỗi");
        btnDelError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDelError.setBackground(Color.WHITE);
        btnDelError.setFocusPainted(false);
        btnDelError.addActionListener(e -> deleteInvalidRows());
        pnlBottomLeft.add(btnDelError);

        JPanel pnlBottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        pnlBottomRight.setOpaque(false);

        JButton btnAccept = new JButton("Chấp nhận");
        btnAccept.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAccept.setPreferredSize(new Dimension(105, 30));
        btnAccept.setBackground(Color.WHITE);
        btnAccept.setFocusPainted(false);
        btnAccept.setIcon(IconHelper.getIcon("accept.png", 14, 14));
        btnAccept.addActionListener(e -> onSaveData());

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(90, 30));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setIcon(IconHelper.getIcon("cross.png", 14, 14));
        btnCancel.addActionListener(e -> dispose());

        pnlBottomRight.add(btnAccept);
        pnlBottomRight.add(btnCancel);

        bottomPanel.add(pnlBottomLeft, BorderLayout.WEST);
        bottomPanel.add(pnlBottomRight, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnAccept);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel createRightCommonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 248, 253));
        panel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(200, 215, 230)));

        JLabel lblTitle = new JLabel("Các thuộc tính chung");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(200, 20, 20));
        lblTitle.setBorder(new EmptyBorder(10, 14, 8, 14));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(4, 14, 10, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 0, 3, 0);

        int row = 0;

        // 1. Mã khách
        chkMaKhach = new JCheckBox("Mã khách:");
        txtChungMaKhach = new JTextField();
        addFormRow(form, gbc, row++, chkMaKhach, txtChungMaKhach, false, "");

        // 2. Tên khách hàng
        chkTenKhach = new JCheckBox("Tên khách hàng:");
        txtChungTenKhach = new JTextField();
        addFormRow(form, gbc, row++, chkTenKhach, txtChungTenKhach, false, "");

        // 3. Địa chỉ
        chkDiaChi = new JCheckBox("Địa chỉ:");
        txtChungDiaChi = new JTextField();
        addFormRow(form, gbc, row++, chkDiaChi, txtChungDiaChi, false, "");

        // 4. Điện thoại (checked)
        chkDienThoai = new JCheckBox("Điện thoại:");
        txtChungDienThoai = new JTextField();
        addFormRow(form, gbc, row++, chkDienThoai, txtChungDienThoai, true, "");

        // 5. Email (checked)
        chkEmail = new JCheckBox("Email:");
        txtChungEmail = new JTextField();
        addFormRow(form, gbc, row++, chkEmail, txtChungEmail, true, "");

        // 6. Nhóm khách hàng (checked)
        chkNhomKhach = new JCheckBox("Nhóm khách hàng:");
        cbChungNhomKhach = new JComboBox<>();
        cbChungNhomKhach.addItem("");
        for (NhomKhachHang g : groupList) {
            if (!g.isSeparator() && !g.isFolder() && g.getName() != null) {
                cbChungNhomKhach.addItem(g.getName());
            }
        }
        addFormRow(form, gbc, row++, chkNhomKhach, cbChungNhomKhach, true, null);

        // 7. Mã số thuế (checked)
        chkMaSoThue = new JCheckBox("Mã số thuế:");
        txtChungMaSoThue = new JTextField();
        addFormRow(form, gbc, row++, chkMaSoThue, txtChungMaSoThue, true, "");

        // 8. Nhân viên (checked)
        chkNhanVien = new JCheckBox("Nhân viên:");
        cbChungNhanVien = new JComboBox<>();
        cbChungNhanVien.addItem("");
        for (String staffName : staffMap.values()) {
            cbChungNhanVien.addItem(staffName);
        }
        addFormRow(form, gbc, row++, chkNhanVien, cbChungNhanVien, true, null);

        // 9. Ngày thành lập/sinh nhật (checked)
        chkNgaySinh = new JCheckBox("Ngày thành lập/sinh nhật:");
        txtChungNgaySinh = new JTextField();
        txtChungNgaySinh.setToolTipText("Định dạng: dd/MM/yyyy");
        addFormRow(form, gbc, row++, chkNgaySinh, txtChungNgaySinh, true, "");

        // 10. Tỉnh thành (checked)
        chkTinhThanh = new JCheckBox("Tỉnh thành:");
        cbChungTinhThanh = new JComboBox<>();
        cbChungTinhThanh.addItem("");
        for (String cityName : cityMap.values()) {
            cbChungTinhThanh.addItem(cityName);
        }
        addFormRow(form, gbc, row++, chkTinhThanh, cbChungTinhThanh, true, null);

        // 11. Facebook (checked)
        chkFacebook = new JCheckBox("Facebook:");
        txtChungFacebook = new JTextField();
        addFormRow(form, gbc, row++, chkFacebook, txtChungFacebook, true, "");

        // 12. Ghi chú (checked)
        chkGhiChu = new JCheckBox("Ghi chú:");
        txtChungGhiChu = new JTextField();
        addFormRow(form, gbc, row++, chkGhiChu, txtChungGhiChu, true, "");

        // 13. Điểm tích lũy ban đầu (checked, default "0")
        chkDiemBanDau = new JCheckBox("Điểm tích lũy ban đầu:");
        txtChungDiemBanDau = new JTextField("0");
        addFormRow(form, gbc, row++, chkDiemBanDau, txtChungDiemBanDau, true, "0");

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(null);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        panel.add(scrollForm, BorderLayout.CENTER);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, JCheckBox chk, JComponent field, boolean isChecked, String defaultVal) {
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chk.setOpaque(false);
        chk.setSelected(isChecked);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(140, 24));
        field.setEnabled(isChecked);
        if (field instanceof JTextField) {
            ((JTextField) field).setEditable(isChecked);
            if (defaultVal != null) {
                ((JTextField) field).setText(defaultVal);
            }
        }

        chk.addActionListener(e -> {
            boolean selected = chk.isSelected();
            field.setEnabled(selected);
            if (field instanceof JTextField) {
                ((JTextField) field).setEditable(selected);
            }
            updateGridColumnsVisibility();
        });

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.55;
        panel.add(chk, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.45;
        panel.add(field, gbc);
    }

    private void setupTableRenderersAndEditors() {
        final Icon pointerIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 65, 135));
                int cx = x + 2;
                int cy = y + 1;
                int[] xPoints = {cx, cx + 7, cx};
                int[] yPoints = {cy, cy + 5, cy + 10};
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 10; }
            @Override public int getIconHeight() { return 12; }
        };

        DefaultTableCellRenderer pointerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setText("");
                lbl.setIcon(isSelected ? pointerIcon : null);
                lbl.setBackground(isSelected ? new Color(210, 230, 250) : Color.WHITE);
                return lbl;
            }
        };

        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        };

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        };

        allTableColumns.clear();
        for (int i = 0; i < GRID_COLUMNS.length; i++) {
            TableColumn tc = new TableColumn(i);
            tc.setHeaderValue(GRID_COLUMNS[i]);
            String colName = GRID_COLUMNS[i];

            if (i == 0) {
                tc.setCellRenderer(pointerRenderer);
                tc.setPreferredWidth(26);
                tc.setMaxWidth(30);
            } else if ("Mã khách".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(100);
            } else if ("Tên khách hàng".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(180);
            } else if ("Địa chỉ".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(180);
            } else if ("Điện thoại".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(100);
            } else if ("Email".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(120);
            } else if ("Nhóm khách hàng".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(120);
                JComboBox<String> cbGridNhom = new JComboBox<>();
                cbGridNhom.addItem("");
                for (NhomKhachHang g : groupList) {
                    if (!g.isSeparator() && !g.isFolder() && g.getName() != null) {
                        cbGridNhom.addItem(g.getName());
                    }
                }
                cbGridNhom.setEditable(true);
                tc.setCellEditor(new DefaultCellEditor(cbGridNhom));
            } else if ("Mã số thuế".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(90);
            } else if ("Nhân viên".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(120);
                JComboBox<String> cbGridNV = new JComboBox<>();
                cbGridNV.addItem("");
                for (String staffName : staffMap.values()) {
                    cbGridNV.addItem(staffName);
                }
                cbGridNV.setEditable(true);
                tc.setCellEditor(new DefaultCellEditor(cbGridNV));
            } else if ("Ngày thành lập/sinh nhật".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(110);
            } else if ("Tỉnh thành".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(110);
                JComboBox<String> cbGridTinh = new JComboBox<>();
                cbGridTinh.addItem("");
                for (String cityName : cityMap.values()) {
                    cbGridTinh.addItem(cityName);
                }
                cbGridTinh.setEditable(true);
                tc.setCellEditor(new DefaultCellEditor(cbGridTinh));
            } else if ("Facebook".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(110);
            } else if ("Ghi chú".equals(colName)) {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(140);
            } else if ("Điểm tích lũy ban đầu".equals(colName)) {
                tc.setCellRenderer(rightRenderer);
                tc.setPreferredWidth(110);
            } else {
                tc.setCellRenderer(textRenderer);
                tc.setPreferredWidth(100);
            }
            allTableColumns.add(tc);
        }
    }

    private boolean isFieldCheckedInCommonProperties(int modelIndex) {
        switch (modelIndex) {
            case 1: return chkMaKhach != null && chkMaKhach.isSelected();
            case 2: return chkTenKhach != null && chkTenKhach.isSelected();
            case 3: return chkDiaChi != null && chkDiaChi.isSelected();
            case 4: return chkDienThoai != null && chkDienThoai.isSelected();
            case 5: return chkEmail != null && chkEmail.isSelected();
            case 6: return chkNhomKhach != null && chkNhomKhach.isSelected();
            case 7: return chkMaSoThue != null && chkMaSoThue.isSelected();
            case 8: return chkNhanVien != null && chkNhanVien.isSelected();
            case 9: return chkNgaySinh != null && chkNgaySinh.isSelected();
            case 10: return chkTinhThanh != null && chkTinhThanh.isSelected();
            case 11: return chkFacebook != null && chkFacebook.isSelected();
            case 12: return chkGhiChu != null && chkGhiChu.isSelected();
            case 13: return chkDiemBanDau != null && chkDiemBanDau.isSelected();
            default: return false;
        }
    }

    public void updateGridColumnsVisibility() {
        if (tableGrid == null || allTableColumns.isEmpty()) return;

        if (tableGrid.isEditing()) {
            TableCellEditor editor = tableGrid.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        TableColumnModel cm = tableGrid.getColumnModel();
        while (cm.getColumnCount() > 0) {
            cm.removeColumn(cm.getColumn(0));
        }

        // Pointer column (0) always shown
        if (!allTableColumns.isEmpty()) {
            cm.addColumn(allTableColumns.get(0));
        }

        // Columns 1..13: If NOT checked in common properties -> shown on grid!
        for (int i = 1; i < allTableColumns.size(); i++) {
            if (!isFieldCheckedInCommonProperties(i)) {
                cm.addColumn(allTableColumns.get(i));
            }
        }

        tableGrid.revalidate();
        tableGrid.repaint();
    }

    private void addNewRows(int count) {
        for (int i = 0; i < count; i++) {
            Object[] row = new Object[GRID_COLUMNS.length];
            Arrays.fill(row, "");
            tableModel.addRow(row);
        }
    }

    private void deleteSelectedRows() {
        int[] selectedRows = tableGrid.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn các dòng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            tableModel.removeRow(selectedRows[i]);
        }
        if (tableModel.getRowCount() == 0) {
            addNewRows(1);
        }
    }

    private void deleteInvalidRows() {
        if (tableGrid.isEditing()) {
            TableCellEditor editor = tableGrid.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        int colTen = getColumnIndex("Tên khách hàng");
        int removed = 0;

        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            Object val = tableModel.getValueAt(i, colTen);
            String ten = val != null ? val.toString().trim() : "";
            if (ten.isEmpty() && (!chkTenKhach.isSelected() || txtChungTenKhach.getText().trim().isEmpty())) {
                tableModel.removeRow(i);
                removed++;
            }
        }

        if (tableModel.getRowCount() == 0) {
            addNewRows(1);
        }

        JOptionPane.showMessageDialog(this, "Đã xóa " + removed + " dòng không hợp lệ (trống tên khách hàng)!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void pasteFromClipboard() {
        try {
            String clipData = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (clipData == null || clipData.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Clipboard không có dữ liệu văn bản!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] lines = clipData.split("\r?\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] tokens = line.split("\t");

                Object[] row = new Object[GRID_COLUMNS.length];
                Arrays.fill(row, "");

                int visibleCount = tableGrid.getColumnCount();
                if (tokens.length <= visibleCount - 1) {
                    for (int t = 0; t < tokens.length && (t + 1) < visibleCount; t++) {
                        int modelCol = tableGrid.convertColumnIndexToModel(t + 1);
                        row[modelCol] = tokens[t].trim();
                    }
                } else {
                    for (int t = 0; t < tokens.length && (t + 1) < GRID_COLUMNS.length; t++) {
                        row[t + 1] = tokens[t].trim();
                    }
                }
                tableModel.addRow(row);
            }
            JOptionPane.showMessageDialog(this, "Đã dán thành công " + lines.length + " dòng dữ liệu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi dán dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openExcelImportDialog() {
        XuatMauVaChonExcelKhachHangDialog dialog = new XuatMauVaChonExcelKhachHangDialog(
                this,
                KhachHangExcelUtil.DEFAULT_VISIBLE_COLUMNS,
                parsedRows -> {
                    if (parsedRows != null && !parsedRows.isEmpty()) {
                        loadParsedRowsToGrid(parsedRows);
                    }
                }
        );
        dialog.setVisible(true);
    }

    public void loadParsedRowsToGrid(List<Map<String, Object>> parsedRows) {
        if (parsedRows == null || parsedRows.isEmpty()) return;

        // Xóa các dòng trống hiện tại nếu có
        boolean onlyEmpty = true;
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            for (int c = 1; c < tableModel.getColumnCount(); c++) {
                Object v = tableModel.getValueAt(r, c);
                if (v != null && !v.toString().trim().isEmpty()) {
                    onlyEmpty = false;
                    break;
                }
            }
        }
        if (onlyEmpty) {
            tableModel.setRowCount(0);
        }

        for (Map<String, Object> map : parsedRows) {
            Object[] row = new Object[GRID_COLUMNS.length];
            row[0] = "";
            for (int c = 1; c < GRID_COLUMNS.length; c++) {
                String col = GRID_COLUMNS[c];
                Object val = map.get(col);
                row[c] = val != null ? val.toString() : "";
            }
            tableModel.addRow(row);
        }

        if (tableGrid.getRowCount() > 0) {
            tableGrid.setRowSelectionInterval(0, 0);
        }
    }

    private int getColumnIndex(String columnName) {
        for (int i = 0; i < GRID_COLUMNS.length; i++) {
            if (GRID_COLUMNS[i].equalsIgnoreCase(columnName)) return i;
        }
        return -1;
    }

    private void onSaveData() {
        if (tableGrid.isEditing()) {
            TableCellEditor editor = tableGrid.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        int totalRows = tableModel.getRowCount();
        if (totalRows == 0) {
            JOptionPane.showMessageDialog(this, "Không có dòng dữ liệu nào để lưu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int colMaKhach = getColumnIndex("Mã khách");
        int colTen = getColumnIndex("Tên khách hàng");
        int colDiaChi = getColumnIndex("Địa chỉ");
        int colDienThoai = getColumnIndex("Điện thoại");
        int colEmail = getColumnIndex("Email");
        int colNhom = getColumnIndex("Nhóm khách hàng");
        int colMST = getColumnIndex("Mã số thuế");
        int colNV = getColumnIndex("Nhân viên");
        int colDOB = getColumnIndex("Ngày thành lập/sinh nhật");
        int colTinh = getColumnIndex("Tỉnh thành");
        int colFB = getColumnIndex("Facebook");
        int colGhiChu = getColumnIndex("Ghi chú");
        int colDiem = getColumnIndex("Điểm tích lũy ban đầu");

        List<KhachHang> toSaveList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Lookup maps
        Map<String, String> staffNameToId = new HashMap<>();
        for (Map.Entry<String, String> e : staffMap.entrySet()) {
            staffNameToId.put(e.getValue().toLowerCase().trim(), e.getKey());
        }

        Map<String, String> cityNameToId = new HashMap<>();
        for (Map.Entry<String, String> e : cityMap.entrySet()) {
            cityNameToId.put(e.getValue().toLowerCase().trim(), e.getKey());
        }

        for (int r = 0; r < totalRows; r++) {
            String ten = getStringVal(r, colTen, chkTenKhach, txtChungTenKhach.getText());
            if (ten.isEmpty()) {
                continue; // Skip empty customer name rows
            }

            String maKhach = getStringVal(r, colMaKhach, chkMaKhach, txtChungMaKhach.getText());
            String diaChi = getStringVal(r, colDiaChi, chkDiaChi, txtChungDiaChi.getText());
            String dienThoai = getStringVal(r, colDienThoai, chkDienThoai, txtChungDienThoai.getText());
            String email = getStringVal(r, colEmail, chkEmail, txtChungEmail.getText());
            String tenNhom = getStringVal(r, colNhom, chkNhomKhach, (String) cbChungNhomKhach.getSelectedItem());
            String mst = getStringVal(r, colMST, chkMaSoThue, txtChungMaSoThue.getText());
            String tenNV = getStringVal(r, colNV, chkNhanVien, (String) cbChungNhanVien.getSelectedItem());
            String strDob = getStringVal(r, colDOB, chkNgaySinh, txtChungNgaySinh.getText());
            String tinhThanh = getStringVal(r, colTinh, chkTinhThanh, (String) cbChungTinhThanh.getSelectedItem());
            String facebook = getStringVal(r, colFB, chkFacebook, txtChungFacebook.getText());
            String ghiChu = getStringVal(r, colGhiChu, chkGhiChu, txtChungGhiChu.getText());
            String strDiem = getStringVal(r, colDiem, chkDiemBanDau, txtChungDiemBanDau.getText());

            KhachHang kh = new KhachHang();
            kh.setMaKhach(maKhach);
            kh.setName(ten);
            kh.setDiaChi(diaChi);
            kh.setDienThoai(dienThoai);
            kh.setEmail(email);
            kh.setMaSoThue(mst);
            kh.setFacebook(facebook);
            kh.setGhiChu(ghiChu);
            kh.setTinhThanh(tinhThanh);

            // Handle Group
            if (!tenNhom.isEmpty()) {
                kh.setTenNhomKhachHang(tenNhom);
                NhomKhachHang groupObj = nhomKhachHangDAO.findByName(tenNhom);
                if (groupObj == null) {
                    groupObj = new NhomKhachHang();
                    groupObj.setName(tenNhom);
                    groupObj.setIconName("users.png");
                    nhomKhachHangDAO.insert(groupObj);
                }
                kh.setNhomKhachHangId(groupObj.getId());
            }

            // Handle Staff
            if (!tenNV.isEmpty()) {
                kh.setTenNhanVien(tenNV);
                String staffId = staffNameToId.get(tenNV.toLowerCase().trim());
                if (staffId != null) {
                    kh.setNhanVienId(staffId);
                }
            }

            // Handle Birthday
            if (!strDob.isEmpty()) {
                try {
                    Date parsedDate = sdf.parse(strDob.trim());
                    kh.setNgaySinhNhat(new Timestamp(parsedDate.getTime()));
                } catch (Exception ignored) {}
            }

            // Handle Initial Points
            if (!strDiem.isEmpty()) {
                try {
                    double pts = Double.parseDouble(strDiem.replace(",", "").replace(" ", "").trim());
                    kh.setDiemTichLuyBanDau(pts);
                    kh.setDiemTichLuy(pts);
                } catch (Exception ignored) {}
            }

            toSaveList.add(kh);
        }

        if (toSaveList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dòng dữ liệu hợp lệ (tất cả các dòng đều thiếu Tên khách hàng)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SystemConfig systemConfig = new SystemConfigDAO().loadConfig();
        if (!systemConfig.isChoPhepTrungTenKhachHang()) {
            Set<String> namesInBatch = new HashSet<>();
            for (KhachHang item : toSaveList) {
                KhachHang existing = item.getMaKhach() != null && !item.getMaKhach().trim().isEmpty()
                        ? khachHangDAO.findByMaKhach(item.getMaKhach()) : null;
                String normalizedName = item.getName().trim().toLowerCase(Locale.ROOT);
                boolean duplicatedInBatch = !namesInBatch.add(normalizedName);
                boolean duplicatedInDatabase = khachHangDAO.existsByName(
                        item.getName(), existing != null ? existing.getId() : null);
                if (duplicatedInBatch || duplicatedInDatabase) {
                    JOptionPane.showMessageDialog(this,
                            "Tên khách hàng '" + item.getName() + "' bị trùng!\n"
                                    + "Hệ thống đang cấu hình không cho phép trùng tên khách hàng.",
                            "Trùng tên khách hàng", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        int inserted = 0;
        int updated = 0;

        for (KhachHang item : toSaveList) {
            String ma = item.getMaKhach();
            if (ma != null && !ma.trim().isEmpty()) {
                KhachHang existing = khachHangDAO.findByMaKhach(ma);
                if (existing != null) {
                    // Update existing
                    existing.setName(item.getName());
                    if (item.getDiaChi() != null && !item.getDiaChi().isEmpty()) existing.setDiaChi(item.getDiaChi());
                    if (item.getDienThoai() != null && !item.getDienThoai().isEmpty()) existing.setDienThoai(item.getDienThoai());
                    if (item.getEmail() != null && !item.getEmail().isEmpty()) existing.setEmail(item.getEmail());
                    if (item.getTenNhomKhachHang() != null && !item.getTenNhomKhachHang().isEmpty()) {
                        existing.setTenNhomKhachHang(item.getTenNhomKhachHang());
                        existing.setNhomKhachHangId(item.getNhomKhachHangId());
                    }
                    if (item.getMaSoThue() != null && !item.getMaSoThue().isEmpty()) existing.setMaSoThue(item.getMaSoThue());
                    if (item.getTenNhanVien() != null && !item.getTenNhanVien().isEmpty()) {
                        existing.setTenNhanVien(item.getTenNhanVien());
                        existing.setNhanVienId(item.getNhanVienId());
                    }
                    if (item.getNgaySinhNhat() != null) existing.setNgaySinhNhat(item.getNgaySinhNhat());
                    if (item.getTinhThanh() != null && !item.getTinhThanh().isEmpty()) existing.setTinhThanh(item.getTinhThanh());
                    if (item.getFacebook() != null && !item.getFacebook().isEmpty()) existing.setFacebook(item.getFacebook());
                    if (item.getGhiChu() != null && !item.getGhiChu().isEmpty()) existing.setGhiChu(item.getGhiChu());
                    if (item.getDiemTichLuyBanDau() != 0) {
                        existing.setDiemTichLuyBanDau(item.getDiemTichLuyBanDau());
                        existing.setDiemTichLuy(item.getDiemTichLuyBanDau());
                    }

                    khachHangDAO.update(existing);
                    updated++;
                    continue;
                }
            }

            // Insert new customer
            khachHangDAO.insert(item);
            inserted++;
        }

        JOptionPane.showMessageDialog(
                this,
                "Đã lưu thành công " + (inserted + updated) + " khách hàng!\n- Thêm mới: " + inserted + "\n- Cập nhật: " + updated,
                "Lưu thành công",
                JOptionPane.INFORMATION_MESSAGE
        );

        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }
        dispose();
    }

    private String getStringVal(int row, int col, JCheckBox chkCommon, String commonVal) {
        String cellVal = "";
        if (col >= 0 && col < tableModel.getColumnCount()) {
            Object v = tableModel.getValueAt(row, col);
            cellVal = v != null ? v.toString().trim() : "";
        }
        if (cellVal.isEmpty() && chkCommon != null && chkCommon.isSelected() && commonVal != null) {
            cellVal = commonVal.trim();
        }
        return cellVal;
    }

    private JButton createToolButton(String text, String iconName, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 6, 2, 6));
        if (al != null) btn.addActionListener(al);
        return btn;
    }
}
