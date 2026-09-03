package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.ExcelExporter;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QuanLyKiemKePanel extends JPanel {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    // Left Filter controls
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<KhoHang> cbFilterKho;
    private JTree treeKhoHang;

    // Master list controls
    private JTextField txtFilterMaster;
    private JTable tableMaster;
    private DefaultTableModel masterModel;
    private final List<DonHang> listMaster = new ArrayList<>();

    // Detail tabs controls
    private JTabbedPane tabbedDetail;
    private JTable tableChiTiet;
    private DefaultTableModel chiTietModel;
    private final List<DonHangChiTiet> listChiTiet = new ArrayList<>();

    // Tab Thong tin (Audit)
    private JLabel lblNgayTao;
    private JLabel lblNguoiTao;
    private JLabel lblNgaySua;
    private JLabel lblNguoiSua;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    public QuanLyKiemKePanel() {
        setLayout(new BorderLayout(0, 0));
        initComponents();
        loadDropdownData();
        loadTreeData();
        reloadMasterData();
        setupKeyBindings();
    }

    private void initComponents() {
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(230);
        mainSplit.setDividerSize(4);
        mainSplit.setBorder(null);

        mainSplit.setLeftComponent(createLeftFilterPanel());
        mainSplit.setRightComponent(createRightContentPanel());

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createLeftFilterPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setBackground(new Color(245, 248, 253));
        pnl.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, new Color(195, 215, 238)),
                new EmptyBorder(6, 6, 6, 6)
        ));

        // Header: Kho hàng + Bánh răng
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblHeader = new JLabel("Kho hàng");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(new Color(20, 60, 120));

        JButton btnSettings = new JButton(IconHelper.getIcon("cog.png", 14, 14));
        btnSettings.setPreferredSize(new Dimension(20, 20));
        btnSettings.setContentAreaFilled(false);
        btnSettings.setBorderPainted(false);
        btnSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        pnlHeader.add(lblHeader, BorderLayout.WEST);
        pnlHeader.add(btnSettings, BorderLayout.EAST);

        // Date filter box
        JPanel pnlDates = new JPanel(new GridBagLayout());
        pnlDates.setOpaque(false);
        pnlDates.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(185, 205, 230), 1), "Ngày", 0, 0, new Font("Segoe UI", Font.PLAIN, 11)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        pnlDates.add(new JLabel("Từ:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        dpkTuNgay = new DatePickerField();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        dpkTuNgay.setDate(cal.getTime());
        dpkTuNgay.addPropertyChangeListener("date", e -> reloadMasterData());
        pnlDates.add(dpkTuNgay, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        pnlDates.add(new JLabel("Đến:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(new Date());
        dpkDenNgay.addPropertyChangeListener("date", e -> reloadMasterData());
        pnlDates.add(dpkDenNgay, gbc);

        // Warehouse dropdown
        JPanel pnlCb = new JPanel(new BorderLayout(4, 2));
        pnlCb.setOpaque(false);
        pnlCb.setBorder(new EmptyBorder(4, 2, 4, 2));
        pnlCb.add(new JLabel("Kho nhập"), BorderLayout.NORTH);

        cbFilterKho = new JComboBox<>();
        cbFilterKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbFilterKho.addActionListener(e -> reloadMasterData());
        pnlCb.add(cbFilterKho, BorderLayout.CENTER);

        // Mini buttons toolbar (+, sửa, xóa, reload)
        JPanel pnlMiniTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        pnlMiniTools.setOpaque(false);

        JButton btnMiniAdd = createMiniButton("plus.png", "Thêm kho");
        JButton btnMiniEdit = createMiniButton("pencil.png", "Sửa kho");
        JButton btnMiniDelete = createMiniButton("cross.png", "Xóa kho");
        JButton btnMiniReload = createMiniButton("arrow_refresh.png", "Tải lại danh sách kho");
        btnMiniReload.addActionListener(e -> {
            loadDropdownData();
            loadTreeData();
            reloadMasterData();
        });

        pnlMiniTools.add(btnMiniAdd);
        pnlMiniTools.add(btnMiniEdit);
        pnlMiniTools.add(btnMiniDelete);
        pnlMiniTools.add(btnMiniReload);

        // Warehouse tree
        treeKhoHang = new JTree();
        treeKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeKhoHang.setRootVisible(true);
        treeKhoHang.setShowsRootHandles(true);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconHelper.getIcon("package.png", 14, 14));
        renderer.setClosedIcon(IconHelper.getIcon("folder.png", 14, 14));
        renderer.setOpenIcon(IconHelper.getIcon("folder_page.png", 14, 14));
        treeKhoHang.setCellRenderer(renderer);

        treeKhoHang.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeKhoHang.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof KhoHang) {
                KhoHang kh = (KhoHang) node.getUserObject();
                for (int i = 0; i < cbFilterKho.getItemCount(); i++) {
                    if (cbFilterKho.getItemAt(i).getId() != null && cbFilterKho.getItemAt(i).getId().equals(kh.getId())) {
                        cbFilterKho.setSelectedIndex(i);
                        break;
                    }
                }
            } else if (node != null && "Tất cả".equals(node.getUserObject().toString())) {
                if (cbFilterKho.getItemCount() > 0) cbFilterKho.setSelectedIndex(0);
            }
        });

        JPanel pnlTop = new JPanel(new BorderLayout(0, 2));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlHeader, BorderLayout.NORTH);
        pnlTop.add(pnlDates, BorderLayout.CENTER);
        pnlTop.add(pnlCb, BorderLayout.SOUTH);

        pnl.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlTreeContainer = new JPanel(new BorderLayout(0, 2));
        pnlTreeContainer.setOpaque(false);
        pnlTreeContainer.add(pnlMiniTools, BorderLayout.NORTH);
        pnlTreeContainer.add(new JScrollPane(treeKhoHang), BorderLayout.CENTER);

        pnl.add(pnlTreeContainer, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createRightContentPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(350);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        // Top: Master Table Panel
        JPanel pnlMaster = new JPanel(new BorderLayout());
        pnlMaster.setOpaque(false);

        pnlMaster.add(createMasterToolbar(), BorderLayout.NORTH);
        pnlMaster.add(createMasterTablePanel(), BorderLayout.CENTER);

        // Bottom: Detail Tabs Panel
        JPanel pnlDetail = createDetailTabsPanel();

        split.setTopComponent(pnlMaster);
        split.setBottomComponent(pnlDetail);

        pnl.add(split, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createMasterToolbar() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(230, 240, 252));
        pnl.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(195, 215, 238)),
                new EmptyBorder(4, 6, 4, 6)
        ));

        // Tab indicator + Search
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlLeft.setOpaque(false);

        JLabel lblTabTitle = new JLabel("Phiếu kiểm kê");
        lblTabTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTabTitle.setForeground(new Color(20, 60, 120));
        lblTabTitle.setIcon(IconHelper.getIcon("table_tab.png", 16, 16));

        JLabel lblFilter = new JLabel("Lọc (F3):");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtFilterMaster = new JTextField(12);
        txtFilterMaster.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilterMaster.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { reloadMasterData(); }
            @Override public void removeUpdate(DocumentEvent e) { reloadMasterData(); }
            @Override public void changedUpdate(DocumentEvent e) { reloadMasterData(); }
        });

        pnlLeft.add(lblTabTitle);
        pnlLeft.add(new JSeparator(SwingConstants.VERTICAL));
        pnlLeft.add(lblFilter);
        pnlLeft.add(txtFilterMaster);

        // Action Buttons: + Thêm mới (Insert), Chỉnh sửa (F4), Xóa (Del), Thêm excel, Xuất excel, In, Tổng, P.tích
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlRight.setOpaque(false);

        JButton btnAdd = createActionButton("+ Thêm mới (Insert)", "plus.png");
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnEdit = createActionButton("Chỉnh sửa (F4)", "pencil.png");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnDelete = createActionButton("Xóa (Del)", "cross.png");
        btnDelete.addActionListener(e -> deleteSelectedOrder());

        JButton btnImportExcel = createActionButton("Thêm excel", "page_excel.png");
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng thêm từ Excel đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        JButton btnExportExcel = createActionButton("Xuất excel", "export-excel.png");
        btnExportExcel.addActionListener(e -> ExcelExporter.exportTable(tableMaster, "Danh_Sach_Phieu_Kiem_Ke", "Kiểm kê", this));

        JButton btnPrint = createActionButton("In", "printer.png");
        btnPrint.addActionListener(e -> printCurrentOrder());

        JButton btnSum = createActionButton("∑ Tổng", "sum.png");
        btnSum.addActionListener(e -> showSummaryDialog());

        JButton btnAnalyze = createActionButton("P.tích", "chart_bar.png");
        btnAnalyze.addActionListener(e -> JOptionPane.showMessageDialog(this, "Tính năng phân tích đang được cập nhật!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        pnlRight.add(btnAdd);
        pnlRight.add(btnEdit);
        pnlRight.add(btnDelete);
        pnlRight.add(new JSeparator(SwingConstants.VERTICAL));
        pnlRight.add(btnImportExcel);
        pnlRight.add(btnExportExcel);
        pnlRight.add(btnPrint);
        pnlRight.add(btnSum);
        pnlRight.add(btnAnalyze);

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    private JPanel createMasterTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        // Master columns matching Image 1: [STT], Số phiếu, Ngày, Kho hàng, Diễn giải, Nhân viên, Ghi chú, Cửa hàng, Còn lại, Thanh toán, Tài khoản ngân hàng, Mã voucher, Thẻ tt
        String[] masterCols = {
                "", "Số phiếu", "Ngày", "Kho hàng", "Diễn giải", "Nhân viên",
                "Ghi chú", "Cửa hàng", "Còn lại", "Thanh toán", "Tài khoản ngân hàng", "Mã voucher", "Thẻ tt"
        };
        masterModel = new DefaultTableModel(masterCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMaster = new JTable(masterModel);
        styleMasterTable(tableMaster);
        tableMaster.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {40, 110, 85, 140, 140, 120, 100, 100, 75, 75, 110, 80, 70};
        for (int i = 0; i < widths.length && i < tableMaster.getColumnCount(); i++) {
            tableMaster.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Row indicator: "▶ 1" on selected row
        tableMaster.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    setText("▶ " + (row + 1));
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setBackground(new Color(40, 150, 45)); // Green highlight as in Image 1
                    setForeground(Color.WHITE);
                } else {
                    setText(String.valueOf(row + 1));
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });

        // Warehouse column with package icon
        tableMaster.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setIcon(IconHelper.getIcon("package.png", 14, 14));
                return this;
            }
        });

        tableMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMasterRowSelected();
            }
        });

        tableMaster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });

        pnl.add(new JScrollPane(tableMaster), BorderLayout.CENTER);
        return pnl;
    }

    private void styleMasterTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 238, 248));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(40, 150, 45)); // Green highlight as seen in Image 1
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(235, 242, 252));
        header.setForeground(new Color(40, 50, 70));
        header.setPreferredSize(new Dimension(100, 24));
    }

    private JPanel createDetailTabsPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        tabbedDetail = new JTabbedPane(JTabbedPane.TOP);
        tabbedDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Thông tin (Audit)
        tabbedDetail.addTab("Thông tin", createAuditTab());

        // Tab 2: Phiếu thu
        tabbedDetail.addTab("Phiếu thu", createEmptyTab("Không có phiếu thu đính kèm"));

        // Tab 3: Phiếu chi
        tabbedDetail.addTab("Phiếu chi", createEmptyTab("Không có phiếu chi đính kèm"));

        // Tab 4: Phiếu thu công nợ
        tabbedDetail.addTab("Phiếu thu công nợ", createEmptyTab("Không có phiếu thu công nợ đính kèm"));

        // Tab 5: Kiểm kê chi tiết (Selected)
        tabbedDetail.addTab("Kiểm kê chi tiết", createDetailTablePanel());
        tabbedDetail.setSelectedIndex(4); // Default to tab Kiểm kê chi tiết as in Image 1

        pnl.add(tabbedDetail, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createAuditTab() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        pnl.add(new JLabel("Ngày tạo:"), gbc);
        gbc.gridx = 1;
        lblNgayTao = new JLabel("-");
        lblNgayTao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblNgayTao, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        pnl.add(new JLabel("Người tạo:"), gbc);
        gbc.gridx = 3;
        lblNguoiTao = new JLabel("-");
        lblNguoiTao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblNguoiTao, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnl.add(new JLabel("Ngày sửa:"), gbc);
        gbc.gridx = 1;
        lblNgaySua = new JLabel("-");
        lblNgaySua.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblNgaySua, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        pnl.add(new JLabel("Người sửa:"), gbc);
        gbc.gridx = 3;
        lblNguoiSua = new JLabel("-");
        lblNguoiSua.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblNguoiSua, gbc);

        return pnl;
    }

    private JPanel createEmptyTab(String message) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(message, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl.setForeground(Color.GRAY);
        pnl.add(lbl, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createDetailTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        // Columns matching Image 1: [STT], Mặt hàng, Số lượng thực tế, Số lượng hệ thống, SL nhập điều chỉnh, SL xuất điều chỉnh, Ghi chú, Đơn vị tính, Mã hàng, Tiền giảm, Kích thước, Hạn sử dụng
        String[] detailCols = {
                "", "Mặt hàng", "Số lượng thực tế", "Số lượng hệ thống",
                "SL nhập điều chỉnh", "SL xuất điều chỉnh", "Ghi chú", "Đơn vị tính", "Mã hàng", "Tiền giảm", "Kích thước", "Hạn sử dụng"
        };
        chiTietModel = new DefaultTableModel(detailCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableChiTiet = new JTable(chiTietModel);
        styleDetailTable(tableChiTiet);
        tableChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {40, 180, 110, 110, 120, 120, 100, 80, 90, 80, 80, 90};
        for (int i = 0; i < widths.length && i < tableChiTiet.getColumnCount(); i++) {
            tableChiTiet.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Numeric alignment
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableChiTiet.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);

        // Unit column with tag icon
        tableChiTiet.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setIcon(IconHelper.getIcon("tag_blue.png", 14, 14));
                return this;
            }
        });

        pnl.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);
        return pnl;
    }

    private void styleDetailTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 238, 248));
        table.setSelectionBackground(new Color(200, 230, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(235, 242, 252));
        header.setForeground(new Color(40, 50, 70));
        header.setPreferredSize(new Dimension(100, 24));
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMiniButton(String iconName, String toolTip) {
        JButton btn = new JButton(IconHelper.getIcon(iconName, 14, 14));
        btn.setToolTipText(toolTip);
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(190, 210, 235), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDropdownData() {
        cbFilterKho.removeAllItems();
        cbFilterKho.addItem(new KhoHang("", "-- Tất cả kho --"));
        List<KhoHang> khoList = khoHangDAO.getAll();
        for (KhoHang k : khoList) {
            cbFilterKho.addItem(k);
        }
    }

    private void loadTreeData() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        List<KhoHang> khoList = khoHangDAO.getAll();

        DefaultMutableTreeNode nodeChuaThietLap = new DefaultMutableTreeNode("Chưa thiết lập");
        root.add(nodeChuaThietLap);

        for (KhoHang k : khoList) {
            root.add(new DefaultMutableTreeNode(k));
        }

        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");
        root.add(nodeTrash);

        treeKhoHang.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < treeKhoHang.getRowCount(); i++) {
            treeKhoHang.expandRow(i);
        }
    }

    public void reloadMasterData() {
        Date tuNgay = dpkTuNgay.getDate();
        Date denNgay = dpkDenNgay.getDate();
        KhoHang selKho = (KhoHang) cbFilterKho.getSelectedItem();
        String khoId = (selKho != null && selKho.getId() != null && !selKho.getId().isEmpty()) ? selKho.getId() : null;
        String kw = txtFilterMaster.getText().trim();

        listMaster.clear();
        listMaster.addAll(donHangDAO.getAllPhieuKiemKe(tuNgay, denNgay, khoId, null, kw));

        masterModel.setRowCount(0);
        for (int i = 0; i < listMaster.size(); i++) {
            DonHang dh = listMaster.get(i);
            masterModel.addRow(new Object[]{
                    (i + 1),
                    dh.getName() != null ? dh.getName() : "",
                    dh.getNgay() != null ? sdfDate.format(dh.getNgay()) : "",
                    dh.getTenKhoXuat() != null ? dh.getTenKhoXuat() : (dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : ""),
                    dh.getDienGiai() != null ? dh.getDienGiai() : "Kiểm kê",
                    dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "",
                    dh.getGhiChu() != null ? dh.getGhiChu() : "",
                    dh.getTenCuaHang() != null ? dh.getTenCuaHang() : "",
                    "0",
                    "0",
                    "",
                    "",
                    ""
            });
        }

        if (!listMaster.isEmpty()) {
            tableMaster.setRowSelectionInterval(0, 0);
        } else {
            clearDetailPanel();
        }
    }

    private void onMasterRowSelected() {
        int row = tableMaster.getSelectedRow();
        if (row < 0 || row >= listMaster.size()) {
            clearDetailPanel();
            return;
        }

        DonHang dh = listMaster.get(row);

        // Update Audit info
        lblNgayTao.setText(dh.getTimeCreated() != null ? sdfDateTime.format(dh.getTimeCreated()) : "-");
        lblNguoiTao.setText(dh.getTenNguoiTao() != null ? dh.getTenNguoiTao() : "-");
        lblNgaySua.setText(dh.getTimeModified() != null ? sdfDateTime.format(dh.getTimeModified()) : "-");
        lblNguoiSua.setText(dh.getTenNguoiSua() != null ? dh.getTenNguoiSua() : "-");

        // Load details
        listChiTiet.clear();
        listChiTiet.addAll(donHangDAO.getChiTietPhieuKiemKe(dh.getId()));

        chiTietModel.setRowCount(0);
        for (int i = 0; i < listChiTiet.size(); i++) {
            DonHangChiTiet ct = listChiTiet.get(i);
            chiTietModel.addRow(new Object[]{
                    (i + 1),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    df.format(ct.getSlThucTe()),
                    df.format(ct.getSlHeThong()),
                    df.format(ct.getSlNhap()),
                    df.format(ct.getSlXuat()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    "0",
                    ct.getKichThuoc() != null ? ct.getKichThuoc() : "",
                    ct.getHanSuDung() != null ? ct.getHanSuDung() : ""
            });
        }
    }

    private void clearDetailPanel() {
        lblNgayTao.setText("-");
        lblNguoiTao.setText("-");
        lblNgaySua.setText("-");
        lblNguoiSua.setText("-");
        chiTietModel.setRowCount(0);
        listChiTiet.clear();
    }

    private void openAddDialog() {
        Window win = SwingUtilities.getWindowAncestor(this);
        PhieuKiemKeDialog dlg = new PhieuKiemKeDialog(win, this::reloadMasterData);
        dlg.setVisible(true);
    }

    private void openEditDialog() {
        int row = tableMaster.getSelectedRow();
        if (row < 0 || row >= listMaster.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu kiểm kê để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DonHang dh = listMaster.get(row);
        Window win = SwingUtilities.getWindowAncestor(this);
        PhieuKiemKeDialog dlg = new PhieuKiemKeDialog(win, dh, this::reloadMasterData);
        dlg.setVisible(true);
    }

    private void deleteSelectedOrder() {
        int row = tableMaster.getSelectedRow();
        if (row < 0 || row >= listMaster.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu kiểm kê để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DonHang dh = listMaster.get(row);
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu kiểm kê [" + dh.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (donHangDAO.deletePhieuKiemKe(dh.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu kiểm kê thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                reloadMasterData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa phiếu kiểm kê này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printCurrentOrder() {
        int row = tableMaster.getSelectedRow();
        if (row < 0 || row >= listMaster.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu kiểm kê để in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DonHang dh = listMaster.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                          PHIẾU KIỂM KÊ                                 \n");
        sb.append("========================================================================\n");
        sb.append("Số phiếu: ").append(dh.getName()).append("    Ngày: ").append(dh.getNgay() != null ? sdfDate.format(dh.getNgay()) : "").append("\n");
        sb.append("Kho hàng: ").append(dh.getTenKhoXuat() != null ? dh.getTenKhoXuat() : "").append("\n");
        sb.append("Nhân viên: ").append(dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "").append("\n");
        sb.append("Diễn giải: ").append(dh.getDienGiai() != null ? dh.getDienGiai() : "").append("\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append(String.format("%-4s %-12s %-24s %-6s %-10s %-10s %-8s %-8s\n", "STT", "Mã hàng", "Tên hàng", "ĐVT", "SL Hệ thống", "SL Thực tế", "Đ/C Xuất", "Đ/C Nhập"));
        for (int i = 0; i < listChiTiet.size(); i++) {
            DonHangChiTiet ct = listChiTiet.get(i);
            sb.append(String.format("%-4d %-12s %-24s %-6s %-10.1f %-10.1f %-8.1f %-8.1f\n",
                    (i + 1),
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? (ct.getTenHang().length() > 24 ? ct.getTenHang().substring(0, 22) + ".." : ct.getTenHang()) : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getSlHeThong(),
                    ct.getSlThucTe(),
                    ct.getSlXuat(),
                    ct.getSlNhap()
            ));
        }
        sb.append("========================================================================\n");

        JTextArea ta = new JTextArea(sb.toString(), 22, 60);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "In phiếu kiểm kê [" + dh.getName() + "]", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSummaryDialog() {
        double tongSlThucTe = 0.0;
        double tongSlHeThong = 0.0;
        double tongDcNhap = 0.0;
        double tongDcXuat = 0.0;

        for (DonHangChiTiet ct : listChiTiet) {
            tongSlThucTe += ct.getSlThucTe();
            tongSlHeThong += ct.getSlHeThong();
            tongDcNhap += ct.getSlNhap();
            tongDcXuat += ct.getSlXuat();
        }

        String msg = String.format("TỔNG HỢP KIỂM KÊ HIỆN TẠI:\n\n" +
                "• Tổng số mặt hàng kiểm kê: %d dòng\n" +
                "• Tổng SL hệ thống: %s\n" +
                "• Tổng SL thực tế: %s\n" +
                "• Tổng SL điều chỉnh xuất (thiếu): %s\n" +
                "• Tổng SL điều chỉnh nhập (thừa): %s",
                listChiTiet.size(),
                df.format(tongSlHeThong),
                df.format(tongSlThucTe),
                df.format(tongDcXuat),
                df.format(tongDcNhap)
        );
        JOptionPane.showMessageDialog(this, msg, "Tổng cộng kiểm kê", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupKeyBindings() {
        registerKeyboardAction(e -> openAddDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> openEditDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> deleteSelectedOrder(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> txtFilterMaster.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }
}
