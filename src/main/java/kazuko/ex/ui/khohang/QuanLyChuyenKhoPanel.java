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
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class QuanLyChuyenKhoPanel extends JPanel {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    // Left filter controls
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<KhoHang> cbKhoNhapFilter;
    private JComboBox<KhoHang> cbKhoXuatFilter;
    private JComboBox<NhanVien> cbNhanVienNhapFilter;
    private JComboBox<NhanVien> cbNhanVienXuatFilter;
    private JTree treeKho;
    private DefaultMutableTreeNode rootTreeNode;
    private DefaultTreeModel treeModel;

    // Right top toolbar
    private JTextField txtLoc;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnAddExcel;
    private JButton btnExportExcel;
    private JButton btnPrint;
    private JButton btnSummary;
    private JButton btnPhanTich;

    // Master Table
    private JTable tableMaster;
    private DefaultTableModel masterModel;
    private final List<DonHang> allOrders = new ArrayList<>();
    private final List<DonHang> filteredOrders = new ArrayList<>();
    private DonHang selectedOrder = null;

    // Bottom Tabs
    private JTabbedPane bottomTabs;
    private JLabel lblAuditKhoiTao;
    private JLabel lblAuditNguoiTao;
    private JLabel lblAuditSuaDoi;
    private JLabel lblAuditNguoiSua;

    // Bottom Detail Table (Tab Chuyển kho chi tiết)
    private JTable tableChiTietBottom;
    private DefaultTableModel chiTietBottomModel;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    public JTable getTableMaster() { return tableMaster; }
    public DefaultTableModel getMasterModel() { return masterModel; }
    public List<DonHang> getAllOrders() { return allOrders; }
    public List<DonHang> getFilteredOrders() { return filteredOrders; }

    public QuanLyChuyenKhoPanel() {
        setLayout(new BorderLayout(0, 0));
        initComponents();
        loadInitialData();
        setupKeyBindings();
    }

    private void initComponents() {
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(195);
        splitMain.setDividerSize(4);
        splitMain.setBorder(null);

        // ==========================================
        // 1. LEFT PANEL: Bộ lọc Kho hàng & Cây kho
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(new Color(245, 248, 253));
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(195, 215, 238)));

        // Header "Kho hàng"
        JPanel pnlLeftHeader = new JPanel(new BorderLayout());
        pnlLeftHeader.setBackground(new Color(230, 240, 252));
        pnlLeftHeader.setBorder(new EmptyBorder(4, 8, 4, 6));

        JLabel lblLeftTitle = new JLabel("Kho hàng");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(30, 60, 110));

        JButton btnConfig = new JButton(IconHelper.getIcon("table_tab.png", 14, 14));
        btnConfig.setPreferredSize(new Dimension(20, 20));
        btnConfig.setFocusPainted(false);
        btnConfig.setBorderPainted(false);
        btnConfig.setContentAreaFilled(false);

        pnlLeftHeader.add(lblLeftTitle, BorderLayout.WEST);
        pnlLeftHeader.add(btnConfig, BorderLayout.EAST);
        leftPanel.add(pnlLeftHeader, BorderLayout.NORTH);

        // Filter Fields
        JPanel pnlFiltersWrapper = new JPanel(new BorderLayout());
        pnlFiltersWrapper.setOpaque(false);

        JPanel pnlFilters = new JPanel(new GridBagLayout());
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(4, 6, 4, 6));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Ngày Từ
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Từ:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkTuNgay = new DatePickerField();
        Calendar calTu = Calendar.getInstance();
        calTu.set(2026, Calendar.AUGUST, 4); // 04/08/2026
        dpkTuNgay.setDate(calTu.getTime());
        dpkTuNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkTuNgay, gbc);

        // Ngày Đến
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Đến:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkDenNgay = new DatePickerField();
        Calendar calDen = Calendar.getInstance();
        calDen.set(2026, Calendar.SEPTEMBER, 3); // 03/09/2026
        dpkDenNgay.setDate(calDen.getTime());
        dpkDenNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkDenNgay, gbc);

        // Kho nhập
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Kho nhập:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbKhoNhapFilter = new JComboBox<>();
        cbKhoNhapFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoNhapFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbKhoNhapFilter, gbc);

        // Kho xuất
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Kho xuất:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbKhoXuatFilter = new JComboBox<>();
        cbKhoXuatFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoXuatFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbKhoXuatFilter, gbc);

        // Nhân viên nhập
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("NV nhập:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhanVienNhapFilter = new JComboBox<>();
        cbNhanVienNhapFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVienNhapFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbNhanVienNhapFilter, gbc);

        // Nhân viên xuất
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("NV xuất:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhanVienXuatFilter = new JComboBox<>();
        cbNhanVienXuatFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVienXuatFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbNhanVienXuatFilter, gbc);

        pnlFiltersWrapper.add(pnlFilters, BorderLayout.NORTH);

        // Action mini toolbar below filters
        JPanel pnlTreeBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        pnlTreeBar.setOpaque(false);
        pnlTreeBar.setBorder(new MatteBorder(1, 0, 1, 0, new Color(215, 230, 248)));

        JButton btnAddKho = createMiniButton("plus.png");
        JButton btnEditKho = createMiniButton("folder_edit.png");
        JButton btnDeleteKho = createMiniButton("cross.png");
        JButton btnRefreshTree = createMiniButton("arrow_refresh.png");
        btnRefreshTree.addActionListener(e -> loadInitialData());

        pnlTreeBar.add(btnAddKho);
        pnlTreeBar.add(btnEditKho);
        pnlTreeBar.add(btnDeleteKho);
        pnlTreeBar.add(btnRefreshTree);
        pnlFiltersWrapper.add(pnlTreeBar, BorderLayout.CENTER);

        // Tree panel
        rootTreeNode = new DefaultMutableTreeNode("Tất cả");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeKho = new JTree(treeModel);
        treeKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeKho.setRootVisible(true);
        treeKho.setRowHeight(20);

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        treeRenderer.setLeafIcon(IconHelper.getIcon("table_tab.png", 14, 14));
        treeRenderer.setOpenIcon(IconHelper.getIcon("folder_page.png", 14, 14));
        treeRenderer.setClosedIcon(IconHelper.getIcon("folder.png", 14, 14));
        treeKho.setCellRenderer(treeRenderer);

        treeKho.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeKho.getLastSelectedPathComponent();
            if (node != null) {
                String nodeText = node.getUserObject().toString();
                if ("Tất cả".equals(nodeText)) {
                    cbKhoNhapFilter.setSelectedIndex(0);
                    cbKhoXuatFilter.setSelectedIndex(0);
                } else if ("Thùng rác".equals(nodeText)) {
                    // Show trash
                } else {
                    for (int i = 0; i < cbKhoXuatFilter.getItemCount(); i++) {
                        KhoHang k = cbKhoXuatFilter.getItemAt(i);
                        if (k != null && nodeText.equalsIgnoreCase(k.getName())) {
                            cbKhoXuatFilter.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });

        pnlFiltersWrapper.add(new JScrollPane(treeKho), BorderLayout.SOUTH);
        leftPanel.add(pnlFiltersWrapper, BorderLayout.CENTER);
        splitMain.setLeftComponent(leftPanel);

        // ==========================================
        // 2. RIGHT PANEL: Toolbar, Master Table, Bottom Tabs
        // ==========================================
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(Color.WHITE);

        // Top Toolbar
        JPanel pnlRightTop = new JPanel(new BorderLayout(0, 0));
        pnlRightTop.setBackground(new Color(238, 244, 252));
        pnlRightTop.setBorder(new MatteBorder(0, 0, 1, 0, new Color(195, 215, 238)));

        // Tab indicator "Chuyển kho"
        JPanel pnlTabTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTabTitle.setBackground(Color.WHITE);
        pnlTabTitle.setBorder(new MatteBorder(1, 1, 0, 1, new Color(195, 215, 238)));
        JLabel lblTabTitle = new JLabel("Chuyển kho");
        lblTabTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTabTitle.setForeground(new Color(20, 60, 120));
        pnlTabTitle.add(lblTabTitle);

        // Action Toolbar
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlToolbar.setOpaque(false);

        pnlToolbar.add(new JLabel("Lọc (F3):"));
        txtLoc = new JTextField(12);
        txtLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLoc.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        pnlToolbar.add(txtLoc);

        btnAdd = createToolbarButton("Thêm mới (Insert)", "plus.png");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setForeground(new Color(0, 120, 50));
        btnAdd.addActionListener(e -> openAddDialog());

        btnEdit = createToolbarButton("Chỉnh sửa (F4)", "pencil.png");
        btnEdit.addActionListener(e -> openEditDialog());

        btnDelete = createToolbarButton("Xóa (Del)", "cross.png");
        btnDelete.addActionListener(e -> deleteSelectedOrder());

        btnAddExcel = createToolbarButton("Thêm excel", "page_excel.png");
        btnAddExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng thêm từ Excel đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        btnExportExcel = createToolbarButton("Xuất excel", "export-excel.png");
        btnExportExcel.addActionListener(e -> ExcelExporter.exportTable(tableMaster, "Quan_Ly_Chuyen_Kho", "Chuyển kho", this));

        btnPrint = createToolbarButton("In", "printer.png");
        btnPrint.addActionListener(e -> printMasterTable());

        btnSummary = createToolbarButton("∑ Tổng", "calculator.png");
        btnSummary.addActionListener(e -> showSummaryDialog());

        btnPhanTich = createToolbarButton("P.tích", "chart_bar.png");
        btnPhanTich.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng phân tích đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        pnlToolbar.add(btnAdd);
        pnlToolbar.add(btnEdit);
        pnlToolbar.add(btnDelete);
        pnlToolbar.add(btnAddExcel);
        pnlToolbar.add(btnExportExcel);
        pnlToolbar.add(btnPrint);
        pnlToolbar.add(btnSummary);
        pnlToolbar.add(btnPhanTich);

        pnlRightTop.add(pnlTabTitle, BorderLayout.WEST);
        pnlRightTop.add(pnlToolbar, BorderLayout.CENTER);
        rightPanel.add(pnlRightTop, BorderLayout.NORTH);

        // Center Split: Master Table on top, Bottom Tabs below
        JSplitPane splitCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitCenter.setResizeWeight(0.65);
        splitCenter.setDividerSize(4);
        splitCenter.setBorder(null);

        // Master Table Panel
        JPanel pnlMasterTable = new JPanel(new BorderLayout());
        pnlMasterTable.setBackground(Color.WHITE);

        // Columns matching Image 1 & 3: [STT], Số phiếu, Ngày, Diễn giải, Kho xuất, Kho nhập, Nhân viên xuất, Nhân viên nhập, Tổng cộng, Ghi chú, Cửa hàng, Thanh toán, Còn lại, Tài khoản ngân hàng
        String[] cols = {
                "", "Số phiếu", "Ngày", "Diễn giải", "Kho xuất", "Kho nhập",
                "Nhân viên xuất", "Nhân viên nhập", "Tổng cộng", "Ghi chú",
                "Cửa hàng", "Thanh toán", "Còn lại", "Tài khoản ngân hàng"
        };
        masterModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMaster = new JTable(masterModel);
        tableMaster.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMaster.setRowHeight(22);
        tableMaster.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableMaster.setShowGrid(true);
        tableMaster.setGridColor(new Color(230, 238, 248));
        tableMaster.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMaster.setSelectionBackground(new Color(210, 238, 215)); // Light green highlight matching Image 3
        tableMaster.setSelectionForeground(Color.BLACK);

        JTableHeader masterHeader = tableMaster.getTableHeader();
        masterHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        masterHeader.setBackground(new Color(245, 248, 253));
        masterHeader.setForeground(new Color(40, 50, 70));
        masterHeader.setPreferredSize(new Dimension(100, 24));

        int[] widths = {40, 110, 85, 140, 130, 130, 110, 110, 95, 100, 80, 85, 85, 120};
        for (int i = 0; i < widths.length && i < tableMaster.getColumnCount(); i++) {
            tableMaster.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // STT renderer with indicator ▶ 1
        tableMaster.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    setText("▶ " + (row + 1));
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setForeground(new Color(0, 110, 0));
                } else {
                    setText(String.valueOf(row + 1));
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    setForeground(Color.GRAY);
                }
                return this;
            }
        });

        // Kho xuất / Kho nhập renderer with warehouse icon
        DefaultTableCellRenderer khoRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setIcon(IconHelper.getIcon("table_tab.png", 14, 14));
                return this;
            }
        };
        tableMaster.getColumnModel().getColumn(4).setCellRenderer(khoRenderer);
        tableMaster.getColumnModel().getColumn(5).setCellRenderer(khoRenderer);

        // Right aligned numbers
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableMaster.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
        tableMaster.getColumnModel().getColumn(11).setCellRenderer(rightRenderer);
        tableMaster.getColumnModel().getColumn(12).setCellRenderer(rightRenderer);

        tableMaster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });

        tableMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableMaster.getSelectedRow();
                if (row >= 0 && row < filteredOrders.size()) {
                    selectedOrder = filteredOrders.get(row);
                    onOrderSelected(selectedOrder);
                }
            }
        });

        setupMasterContextMenu();
        pnlMasterTable.add(new JScrollPane(tableMaster), BorderLayout.CENTER);
        splitCenter.setTopComponent(pnlMasterTable);

        // Bottom JTabbedPane (5 Tabs matching Images 1 & 3)
        bottomTabs = new JTabbedPane();
        bottomTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomTabs.setPreferredSize(new Dimension(100, 175));

        // Tab 1: Thông tin
        bottomTabs.addTab("Thông tin", createAuditInfoTab());
        // Tab 2: Phiếu thu
        bottomTabs.addTab("Phiếu thu", createEmptyTab("Chưa có phiếu thu"));
        // Tab 3: Phiếu chi
        bottomTabs.addTab("Phiếu chi", createEmptyTab("Chưa có phiếu chi"));
        // Tab 4: Phiếu thu công nợ
        bottomTabs.addTab("Phiếu thu công nợ", createEmptyTab("Chưa có phiếu thu công nợ"));
        // Tab 5: Chuyển kho chi tiết
        bottomTabs.addTab("Chuyển kho chi tiết", createDetailItemsTab());

        splitCenter.setBottomComponent(bottomTabs);
        rightPanel.add(splitCenter, BorderLayout.CENTER);

        splitMain.setRightComponent(rightPanel);
        add(splitMain, BorderLayout.CENTER);
    }

    private JPanel createAuditInfoTab() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        pnl.setBackground(new Color(248, 251, 255));

        JPanel pnlBox1 = new JPanel(new GridLayout(2, 1, 2, 4));
        pnlBox1.setOpaque(false);
        lblAuditKhoiTao = new JLabel("Khởi tạo: --/--/---- --:--");
        lblAuditKhoiTao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAuditNguoiTao = new JLabel("Khởi tạo bởi: Administrator");
        lblAuditNguoiTao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBox1.add(lblAuditKhoiTao);
        pnlBox1.add(lblAuditNguoiTao);

        JPanel pnlBox2 = new JPanel(new GridLayout(2, 1, 2, 4));
        pnlBox2.setOpaque(false);
        lblAuditSuaDoi = new JLabel("Sửa đổi gần nhất: --/--/---- --:--");
        lblAuditSuaDoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAuditNguoiSua = new JLabel("Sửa đổi bởi: --");
        lblAuditNguoiSua.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBox2.add(lblAuditSuaDoi);
        pnlBox2.add(lblAuditNguoiSua);

        pnl.add(pnlBox1);
        pnl.add(new JSeparator(SwingConstants.VERTICAL));
        pnl.add(pnlBox2);
        return pnl;
    }

    private JPanel createDetailItemsTab() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        // Columns matching Image 3: [STT], Mặt hàng, Đơn giá, Thành tiền, Ghi chú, Đơn vị tính, Số lượng, Mã hàng, Tiền giảm, Kích thước, Hạn sử dụng
        String[] detailCols = {
                "", "Mặt hàng", "Đơn giá", "Thành tiền", "Ghi chú",
                "Đơn vị tính", "Số lượng", "Mã hàng", "Tiền giảm", "Kích thước", "Hạn sử dụng"
        };
        chiTietBottomModel = new DefaultTableModel(detailCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tableChiTietBottom = new JTable(chiTietBottomModel);
        tableChiTietBottom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableChiTietBottom.setRowHeight(22);
        tableChiTietBottom.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableChiTietBottom.setShowGrid(true);
        tableChiTietBottom.setGridColor(new Color(230, 238, 248));

        int[] w = {35, 180, 80, 85, 90, 80, 65, 80, 75, 75, 80};
        for (int i = 0; i < w.length; i++) {
            tableChiTietBottom.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        // STT renderer
        tableChiTietBottom.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (row == 0) {
                    setText("▶ " + (row + 1));
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else {
                    setText(String.valueOf(row + 1));
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                }
                return this;
            }
        });

        // Brown highlight for first row or selected item as seen in Image 3
        tableChiTietBottom.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (row == 0 && !isSelected) {
                    setBackground(new Color(185, 85, 40));
                    setForeground(Color.WHITE);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else if (!isSelected) {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
                return this;
            }
        });

        // DVT tag renderer with little icon
        tableChiTietBottom.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setIcon(IconHelper.getIcon("tag_purple.png", 12, 12));
                return this;
            }
        });

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableChiTietBottom.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        pnladdChuyenKhoChiTietHeader(pnl);
        pnl.add(new JScrollPane(tableChiTietBottom), BorderLayout.CENTER);
        return pnl;
    }

    private void pnladdChuyenKhoChiTietHeader(JPanel pnl) {
        // Option to add mini header if needed
    }

    private JPanel createEmptyTab(String msg) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl.setForeground(Color.GRAY);
        pnl.add(lbl);
        return pnl;
    }

    private JButton createMiniButton(String iconName) {
        JButton btn = new JButton(IconHelper.getIcon(iconName, 14, 14));
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(200, 220, 240), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createToolbarButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupMasterContextMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 14, 14));
        miAdd.addActionListener(e -> openAddDialog());

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 14, 14));
        miEdit.addActionListener(e -> openEditDialog());

        JMenuItem miDelete = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 14, 14));
        miDelete.addActionListener(e -> deleteSelectedOrder());

        JMenuItem miExcel = new JMenuItem("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        miExcel.addActionListener(e -> ExcelExporter.exportTable(tableMaster, "Quan_Ly_Chuyen_Kho", "Chuyển kho", this));

        JMenuItem miPrint = new JMenuItem("In", IconHelper.getIcon("printer.png", 14, 14));
        miPrint.addActionListener(e -> printMasterTable());

        popup.add(miAdd);
        popup.add(miEdit);
        popup.add(miDelete);
        popup.addSeparator();
        popup.add(miExcel);
        popup.add(miPrint);

        tableMaster.setComponentPopupMenu(popup);
    }

    private void loadInitialData() {
        // Load dropdowns
        cbKhoNhapFilter.removeAllItems();
        cbKhoNhapFilter.addItem(new KhoHang("", "-- Tất cả kho nhập --"));
        List<KhoHang> khoList = khoHangDAO.getAll();
        for (KhoHang k : khoList) {
            cbKhoNhapFilter.addItem(k);
        }

        cbKhoXuatFilter.removeAllItems();
        cbKhoXuatFilter.addItem(new KhoHang("", "-- Tất cả kho xuất --"));
        for (KhoHang k : khoList) {
            cbKhoXuatFilter.addItem(k);
        }

        cbNhanVienNhapFilter.removeAllItems();
        cbNhanVienNhapFilter.addItem(new NhanVien("", "-- Tất cả NV nhập --"));
        List<NhanVien> nvList = nhanVienDAO.getAll();
        for (NhanVien nv : nvList) {
            cbNhanVienNhapFilter.addItem(nv);
        }

        cbNhanVienXuatFilter.removeAllItems();
        cbNhanVienXuatFilter.addItem(new NhanVien("", "-- Tất cả NV xuất --"));
        for (NhanVien nv : nvList) {
            cbNhanVienXuatFilter.addItem(nv);
        }

        // Build warehouse tree
        rootTreeNode.removeAllChildren();
        rootTreeNode.add(new DefaultMutableTreeNode("Chưa thiết lập"));
        for (KhoHang k : khoList) {
            if (k.getName() != null && !k.getName().isEmpty()) {
                rootTreeNode.add(new DefaultMutableTreeNode(k.getName()));
            }
        }
        rootTreeNode.add(new DefaultMutableTreeNode("Thùng rác"));
        treeModel.reload();
        for (int i = 0; i < treeKho.getRowCount(); i++) {
            treeKho.expandRow(i);
        }

        loadData();
    }

    public void loadData() {
        Date tuNgay = dpkTuNgay.getDate();
        Date denNgay = dpkDenNgay.getDate();

        allOrders.clear();
        allOrders.addAll(donHangDAO.getAllPhieuChuyenKho(tuNgay, denNgay, null, null, null, null, null));
        applyFilter();
    }

    private void applyFilter() {
        String kw = txtLoc.getText().trim().toLowerCase();

        KhoHang selKn = (KhoHang) cbKhoNhapFilter.getSelectedItem();
        String filterKnId = (selKn != null && selKn.getId() != null) ? selKn.getId().trim() : "";

        KhoHang selKx = (KhoHang) cbKhoXuatFilter.getSelectedItem();
        String filterKxId = (selKx != null && selKx.getId() != null) ? selKx.getId().trim() : "";

        NhanVien selNvn = (NhanVien) cbNhanVienNhapFilter.getSelectedItem();
        String filterNvnId = (selNvn != null && selNvn.getId() != null) ? selNvn.getId().trim() : "";

        NhanVien selNvx = (NhanVien) cbNhanVienXuatFilter.getSelectedItem();
        String filterNvxId = (selNvx != null && selNvx.getId() != null) ? selNvx.getId().trim() : "";

        filteredOrders.clear();
        for (DonHang dh : allOrders) {
            if (!filterKnId.isEmpty() && !filterKnId.equals(dh.getKhoNhapId())) {
                continue;
            }
            if (!filterKxId.isEmpty() && !filterKxId.equals(dh.getKhoXuatId())) {
                continue;
            }
            if (!filterNvnId.isEmpty() && !filterNvnId.equals(dh.getNhanVienNhapId())) {
                continue;
            }
            if (!filterNvxId.isEmpty() && !filterNvxId.equals(dh.getNhanVienXuatId())) {
                continue;
            }
            if (!kw.isEmpty()) {
                String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                String kx = dh.getTenKhoXuat() != null ? dh.getTenKhoXuat().toLowerCase() : "";
                String kn = dh.getTenKhoNhap() != null ? dh.getTenKhoNhap().toLowerCase() : "";
                String note = dh.getGhiChu() != null ? dh.getGhiChu().toLowerCase() : "";
                if (!name.contains(kw) && !dg.contains(kw) && !kx.contains(kw) && !kn.contains(kw) && !note.contains(kw)) {
                    continue;
                }
            }
            filteredOrders.add(dh);
        }

        populateMasterTable();
    }

    private void populateMasterTable() {
        masterModel.setRowCount(0);
        int selIndex = -1;

        for (int r = 0; r < filteredOrders.size(); r++) {
            DonHang dh = filteredOrders.get(r);
            Vector<Object> rowData = new Vector<>();
            rowData.add(String.valueOf(r + 1));
            rowData.add(dh.getName() != null ? dh.getName() : "");
            rowData.add(dh.getNgay() != null ? sdfDate.format(dh.getNgay()) : "");
            rowData.add(dh.getDienGiai() != null ? dh.getDienGiai() : "Chuyển kho nội bộ");
            rowData.add(dh.getTenKhoXuat() != null ? dh.getTenKhoXuat() : "");
            rowData.add(dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : "");
            rowData.add(dh.getTenNhanVienXuat() != null ? dh.getTenNhanVienXuat() : "");
            rowData.add(dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "");
            rowData.add(CurrencyUtil.format(dh.getTongCong()));
            rowData.add(dh.getGhiChu() != null ? dh.getGhiChu() : "");
            rowData.add(dh.getTenCuaHang() != null ? dh.getTenCuaHang() : "");
            rowData.add(CurrencyUtil.format(dh.getTienThanhToan()));
            rowData.add(CurrencyUtil.format(dh.getConLai()));
            rowData.add(""); // Tài khoản ngân hàng

            masterModel.addRow(rowData);

            if (selectedOrder != null && selectedOrder.getId() != null && selectedOrder.getId().equals(dh.getId())) {
                selIndex = r;
            }
        }

        if (selIndex >= 0 && selIndex < tableMaster.getRowCount()) {
            tableMaster.setRowSelectionInterval(selIndex, selIndex);
        } else if (tableMaster.getRowCount() > 0) {
            tableMaster.setRowSelectionInterval(0, 0);
        } else {
            selectedOrder = null;
            onOrderSelected(null);
        }
    }

    private void onOrderSelected(DonHang order) {
        if (order == null) {
            lblAuditKhoiTao.setText("Khởi tạo: --/--/---- --:--");
            lblAuditNguoiTao.setText("Khởi tạo bởi: --");
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
            chiTietBottomModel.setRowCount(0);
            return;
        }

        String timeCreatedStr = order.getTimeCreated() != null ? sdfDateTime.format(order.getTimeCreated()) : (order.getNgay() != null ? sdfDateTime.format(order.getNgay()) : "--/--/----");
        lblAuditKhoiTao.setText("Khởi tạo: " + timeCreatedStr);
        lblAuditNguoiTao.setText("Khởi tạo bởi: " + (order.getTenNguoiTao() != null ? order.getTenNguoiTao() : "Administrator"));

        if (order.getTimeModified() != null) {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: " + sdfDateTime.format(order.getTimeModified()));
            lblAuditNguoiSua.setText("Sửa đổi bởi: " + (order.getTenNguoiSua() != null ? order.getTenNguoiSua() : "--"));
        } else {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
        }

        List<DonHangChiTiet> details = order.getChiTietLayHang();
        if (details == null || details.isEmpty()) {
            details = donHangDAO.getChiTietPhieuNhap(order.getId());
            order.setChiTietLayHang(details);
        }

        chiTietBottomModel.setRowCount(0);
        for (int i = 0; i < details.size(); i++) {
            DonHangChiTiet ct = details.get(i);
            chiTietBottomModel.addRow(new Object[]{
                    (i + 1),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getDonGia() > 0 ? CurrencyUtil.format(ct.getDonGia()) : "",
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    df.format(ct.getSoLuong()),
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    CurrencyUtil.format(ct.getTienGiam()),
                    ct.getKichThuoc() != null ? ct.getKichThuoc() : "",
                    ct.getHanSuDung() != null ? ct.getHanSuDung() : ""
            });
        }

        // Auto select tab "Chuyển kho chi tiết" when order selected
        if (bottomTabs.getTabCount() >= 5) {
            bottomTabs.setSelectedIndex(4);
        }
    }

    private void openAddDialog() {
        PhieuChuyenKhoDialog dlg = new PhieuChuyenKhoDialog(SwingUtilities.getWindowAncestor(this), () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog() {
        if (selectedOrder == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu chuyển kho cần chỉnh sửa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DonHang full = donHangDAO.getPhieuChuyenKhoById(selectedOrder.getId());
        PhieuChuyenKhoDialog dlg = new PhieuChuyenKhoDialog(SwingUtilities.getWindowAncestor(this), full != null ? full : selectedOrder, () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void deleteSelectedOrder() {
        if (selectedOrder == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu chuyển kho cần xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu chuyển kho [" + selectedOrder.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (donHangDAO.delete(selectedOrder.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu chuyển kho thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa phiếu chuyển kho này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printMasterTable() {
        try {
            tableMaster.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("DANH SÁCH PHIẾU CHUYỂN KHO"),
                    new java.text.MessageFormat("Trang {0}"));
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSummaryDialog() {
        double totalTongCong = 0;
        double totalTienHang = 0;

        for (DonHang dh : filteredOrders) {
            totalTongCong += dh.getTongCong();
            totalTienHang += dh.getTienHang();
        }

        String msg = String.format(
                "TỔNG KẾT PHIẾU CHUYỂN KHO (%d phiếu):\n\n" +
                "• Tổng tiền hàng:  %s đ\n" +
                "• Tổng cộng:       %s đ",
                filteredOrders.size(),
                CurrencyUtil.format(totalTienHang),
                CurrencyUtil.format(totalTongCong)
        );
        JOptionPane.showMessageDialog(this, msg, "Tổng kết", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupKeyBindings() {
        registerKeyboardAction(e -> openAddDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> openEditDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> deleteSelectedOrder(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> txtLoc.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> loadData(), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
