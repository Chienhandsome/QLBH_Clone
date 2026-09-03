package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.hoadon.ChonCotHienThiDialog;
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

public class QuanLyNhapKhoPanel extends JPanel {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final kazuko.ex.dao.NhaCungCapDAO nhaCungCapDAO = new kazuko.ex.dao.NhaCungCapDAO();

    // Left Filters
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<KhoHang> cbKhoNhapFilter;
    private JComboBox<NhanVien> cbNhanVienFilter;
    private JComboBox<KhachHang> cbNhaCungCapFilter;
    private JTree treeKho;

    // Right Top Toolbar
    private JTextField txtLoc;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnAddExcel;
    private JButton btnExcel;
    private JButton btnPrint;
    private JButton btnTong;
    private JButton btnPhanTich;

    // Master Table
    private JTable tablePhieuNhap;
    private DefaultTableModel masterModel;
    private List<String> visibleColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_NHAPKHO_COLUMNS);
    private List<DonHang> allVouchers = new ArrayList<>();
    private List<DonHang> filteredVouchers = new ArrayList<>();
    private DonHang selectedVoucher = null;

    public JTable getTablePhieuNhap() { return tablePhieuNhap; }
    public DefaultTableModel getMasterModel() { return masterModel; }
    public List<DonHang> getAllVouchers() { return allVouchers; }
    public List<DonHang> getFilteredVouchers() { return filteredVouchers; }
    public JComboBox<KhoHang> getCbKhoNhapFilter() { return cbKhoNhapFilter; }
    public JComboBox<NhanVien> getCbNhanVienFilter() { return cbNhanVienFilter; }
    public JComboBox<KhachHang> getCbNhaCungCapFilter() { return cbNhaCungCapFilter; }

    // Bottom Tabs
    private JTabbedPane bottomTabs;
    private JLabel lblAuditKhoiTao;
    private JLabel lblAuditNguoiTao;
    private JLabel lblAuditSuaDoi;
    private JLabel lblAuditNguoiSua;

    // Detail Items in Bottom Tab (Image 3)
    private JTable tableChiTietBottom;
    private DefaultTableModel chiTietBottomModel;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    public QuanLyNhapKhoPanel() {
        setLayout(new BorderLayout(0, 0));
        initComponents();
        loadInitialData();
        setupKeyBindings();
    }

    private void initComponents() {
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(205);
        splitMain.setDividerSize(4);
        splitMain.setBorder(null);

        // ==========================================
        // 1. LEFT PANEL: Filters & Tree
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(new Color(245, 248, 253));
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(195, 215, 238)));

        // Header "Kho hàng" with config icon
        JPanel pnlLeftHeader = new JPanel(new BorderLayout());
        pnlLeftHeader.setBackground(new Color(230, 240, 252));
        pnlLeftHeader.setBorder(new EmptyBorder(4, 8, 4, 6));

        JLabel lblLeftTitle = new JLabel("Kho hàng");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(30, 60, 110));

        JButton btnConfigKho = new JButton(IconHelper.getIcon("table_tab.png", 14, 14));
        btnConfigKho.setPreferredSize(new Dimension(20, 20));
        btnConfigKho.setFocusPainted(false);
        btnConfigKho.setBorderPainted(false);
        btnConfigKho.setContentAreaFilled(false);
        btnConfigKho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        pnlLeftHeader.add(lblLeftTitle, BorderLayout.WEST);
        pnlLeftHeader.add(btnConfigKho, BorderLayout.EAST);
        leftPanel.add(pnlLeftHeader, BorderLayout.NORTH);

        // Left Center: Filters and Warehouse Tree
        JPanel pnlLeftCenter = new JPanel(new BorderLayout(0, 0));
        pnlLeftCenter.setOpaque(false);

        // Filter Controls Box
        JPanel pnlFilters = new JPanel(new GridBagLayout());
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Từ ngày (mặc định 01/09/2026 theo Hình 1)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Từ:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkTuNgay = new DatePickerField();
        Calendar calTu = Calendar.getInstance();
        calTu.set(2026, Calendar.SEPTEMBER, 1);
        dpkTuNgay.setDate(calTu.getTime());
        dpkTuNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkTuNgay, gbc);

        // Đến ngày (mặc định 30/09/2026 theo Hình 1)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Đến:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkDenNgay = new DatePickerField();
        Calendar calDen = Calendar.getInstance();
        calDen.set(2026, Calendar.SEPTEMBER, 30);
        dpkDenNgay.setDate(calDen.getTime());
        dpkDenNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkDenNgay, gbc);

        // Kho nhập
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Kho nhập"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbKhoNhapFilter = new JComboBox<>();
        cbKhoNhapFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoNhapFilter.addActionListener(e -> {
            KhoHang sel = (KhoHang) cbKhoNhapFilter.getSelectedItem();
            if (sel != null && sel.getId() != null && !sel.getId().isEmpty()) {
                selectTreeNodeForWarehouse(sel.getId());
            }
            applyFilter();
        });
        pnlFilters.add(cbKhoNhapFilter, gbc);

        // Nhân viên nhập
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Nhân viên"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhanVienFilter = new JComboBox<>();
        cbNhanVienFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVienFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbNhanVienFilter, gbc);

        // Nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Nhà CC"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhaCungCapFilter = new JComboBox<>();
        cbNhaCungCapFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhaCungCapFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbNhaCungCapFilter, gbc);

        pnlLeftCenter.add(pnlFilters, BorderLayout.NORTH);

        // Mini action toolbar (+, Trash, Refresh, Layout)
        JPanel pnlMiniToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        pnlMiniToolbar.setOpaque(false);
        pnlMiniToolbar.setBorder(new EmptyBorder(0, 4, 2, 4));

        JButton btnMiniAdd = createMiniIconButton("plus.png");
        JButton btnMiniTrash = createMiniIconButton("RecycleBin.png");
        JButton btnMiniRefresh = createMiniIconButton("Refresh.png");
        JButton btnMiniLayout = createMiniIconButton("edit-column.png");

        btnMiniRefresh.addActionListener(e -> loadData());

        pnlMiniToolbar.add(btnMiniAdd);
        pnlMiniToolbar.add(btnMiniTrash);
        pnlMiniToolbar.add(btnMiniRefresh);
        pnlMiniToolbar.add(btnMiniLayout);

        // Warehouse Tree
        treeKho = new JTree();
        treeKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeKho.setRowHeight(20);
        treeKho.setRootVisible(true);
        treeKho.setShowsRootHandles(true);

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof KhoHang) {
                        KhoHang k = (KhoHang) userObj;
                        setText(k.getName());
                        setIcon(getWarehouseIcon(k));
                    } else if (userObj instanceof String) {
                        String s = (String) userObj;
                        setText(s);
                        if ("Tất cả".equalsIgnoreCase(s)) {
                            setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        } else if ("Thùng rác".equalsIgnoreCase(s)) {
                            setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                        } else {
                            setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        }
                    }
                }
                return this;
            }
        };
        treeKho.setCellRenderer(treeRenderer);

        treeKho.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeKho.getLastSelectedPathComponent();
            if (node != null) {
                Object obj = node.getUserObject();
                if (obj instanceof KhoHang) {
                    KhoHang k = (KhoHang) obj;
                    syncWarehouseCombo(k.getId());
                } else {
                    syncWarehouseCombo("");
                }
            }
            applyFilter();
        });

        JPanel pnlTreeHolder = new JPanel(new BorderLayout());
        pnlTreeHolder.setOpaque(false);
        pnlTreeHolder.add(pnlMiniToolbar, BorderLayout.NORTH);
        pnlTreeHolder.add(new JScrollPane(treeKho), BorderLayout.CENTER);

        pnlLeftCenter.add(pnlTreeHolder, BorderLayout.CENTER);
        leftPanel.add(pnlLeftCenter, BorderLayout.CENTER);
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

        // Tab indicator "Phiếu nhập"
        JPanel pnlTabTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTabTitle.setBackground(Color.WHITE);
        pnlTabTitle.setBorder(new MatteBorder(1, 1, 0, 1, new Color(195, 215, 238)));
        JLabel lblTabNhap = new JLabel("Phiếu nhập");
        lblTabNhap.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTabNhap.setForeground(new Color(20, 60, 120));
        pnlTabTitle.add(lblTabNhap);

        // Toolbar Buttons
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlToolbar.setOpaque(false);

        pnlToolbar.add(new JLabel("Lọc (F3):"));
        txtLoc = new JTextField(11);
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
        btnAdd.addActionListener(e -> openAddPhieuNhapDialog());

        btnEdit = createToolbarButton("Chỉnh sửa (F4)", "pencil.png");
        btnEdit.addActionListener(e -> openEditPhieuNhapDialog());

        btnDelete = createToolbarButton("Xóa (Del)", "cross.png");
        btnDelete.addActionListener(e -> deleteSelectedVoucher());

        btnAddExcel = createToolbarButton("Thêm excel", "page_excel.png");
        btnAddExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng thêm từ Excel đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        btnExcel = createToolbarButton("Xuất excel", "export-excel.png");
        btnExcel.addActionListener(e -> ExcelExporter.exportTable(tablePhieuNhap, "Danh_Sach_Nhap_Kho", "Nhập kho", this));

        btnPrint = createToolbarButton("In", "printer.png");
        btnPrint.addActionListener(e -> printMasterTable());

        btnTong = createToolbarButton("∑ Tổng", "calculator.png");
        btnTong.addActionListener(e -> showSummaryDialog());

        btnPhanTich = createToolbarButton("P.tích", "chart_bar.png");
        btnPhanTich.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng phân tích số liệu đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        pnlToolbar.add(btnAdd);
        pnlToolbar.add(btnEdit);
        pnlToolbar.add(btnDelete);
        pnlToolbar.add(btnAddExcel);
        pnlToolbar.add(btnExcel);
        pnlToolbar.add(btnPrint);
        pnlToolbar.add(btnTong);
        pnlToolbar.add(btnPhanTich);

        pnlRightTop.add(pnlTabTitle, BorderLayout.WEST);
        pnlRightTop.add(pnlToolbar, BorderLayout.CENTER);
        rightPanel.add(pnlRightTop, BorderLayout.NORTH);

        // Center Split: Master Table on top, Tabs on bottom
        JSplitPane splitCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitCenter.setResizeWeight(0.65);
        splitCenter.setDividerSize(4);
        splitCenter.setBorder(null);

        // Master Table Panel
        JPanel pnlMasterTable = new JPanel(new BorderLayout());
        pnlMasterTable.setBackground(Color.WHITE);

        masterModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablePhieuNhap = new JTable(masterModel);
        tablePhieuNhap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePhieuNhap.setRowHeight(22);
        tablePhieuNhap.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablePhieuNhap.setShowGrid(true);
        tablePhieuNhap.setGridColor(new Color(230, 238, 248));
        tablePhieuNhap.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Soft green highlight matching Image 3
        tablePhieuNhap.setSelectionBackground(new Color(200, 242, 205));
        tablePhieuNhap.setSelectionForeground(Color.BLACK);

        JTableHeader masterHeader = tablePhieuNhap.getTableHeader();
        masterHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        masterHeader.setBackground(new Color(245, 248, 253));
        masterHeader.setForeground(new Color(40, 50, 70));
        masterHeader.setPreferredSize(new Dimension(100, 24));

        tablePhieuNhap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditPhieuNhapDialog();
                }
            }
        });

        tablePhieuNhap.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablePhieuNhap.getSelectedRow();
                if (row >= 0 && row < filteredVouchers.size()) {
                    selectedVoucher = filteredVouchers.get(row);
                    onVoucherSelected(selectedVoucher);
                }
            }
        });

        setupMasterContextMenu();
        pnlMasterTable.add(new JScrollPane(tablePhieuNhap), BorderLayout.CENTER);
        splitCenter.setTopComponent(pnlMasterTable);

        // Bottom JTabbedPane (5 Tabs)
        bottomTabs = new JTabbedPane();
        bottomTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomTabs.setPreferredSize(new Dimension(100, 180));

        // Tab 1: Thông tin (Audit Log)
        bottomTabs.addTab("Thông tin", createAuditInfoTab());
        // Tab 2: Phiếu thu
        bottomTabs.addTab("Phiếu thu", createEmptyTab("Chưa có phiếu thu cho chứng từ này"));
        // Tab 3: Phiếu chi
        bottomTabs.addTab("Phiếu chi", createEmptyTab("Chưa có phiếu chi cho chứng từ này"));
        // Tab 4: Phiếu thu công nợ
        bottomTabs.addTab("Phiếu thu công nợ", createEmptyTab("Chưa có phiếu thu công nợ"));
        // Tab 5: Nhập kho chi tiết (Image 3)
        bottomTabs.addTab("Nhập kho chi tiết", createDetailItemsTab());

        splitCenter.setBottomComponent(bottomTabs);
        rightPanel.add(splitCenter, BorderLayout.CENTER);

        splitMain.setRightComponent(rightPanel);
        add(splitMain, BorderLayout.CENTER);
    }

    private JPanel createAuditInfoTab() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
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

        // Columns matching Image 3: [STT], Mặt hàng, Đơn vị tính, Số lượng, Đơn giá, Giảm giá %, Thành tiền, Ghi chú, Tiền giảm, Mã hàng, Kích thước, Hạn sử dụng
        String[] detailCols = {
                "", "Mặt hàng", "Đơn vị tính", "Số lượng", "Đơn giá", "Giảm giá %",
                "Thành tiền", "Ghi chú", "Tiền giảm", "Mã hàng", "Kích thước", "Hạn sử dụng"
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

        int[] w = {35, 180, 70, 60, 85, 75, 95, 80, 80, 75, 75, 80};
        for (int i = 0; i < w.length; i++) {
            tableChiTietBottom.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableChiTietBottom.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableChiTietBottom.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        pnl.add(new JScrollPane(tableChiTietBottom), BorderLayout.CENTER);
        return pnl;
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

    private JButton createToolbarButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMiniIconButton(String iconName) {
        JButton btn = new JButton(IconHelper.getIcon(iconName, 14, 14));
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(190, 210, 235), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Icon getWarehouseIcon(KhoHang k) {
        if (k == null) return IconHelper.getIcon("Folder.png", 16, 16);
        String avatar = k.getAvatarIcon();
        if (avatar != null) {
            switch (avatar) {
                case "lightbulb": return IconHelper.getIcon("lightbulb.png", 16, 16);
                case "purple_house": return IconHelper.getIcon("house.png", 16, 16);
                case "red_house": return IconHelper.getIcon("application_home.png", 16, 16);
            }
        }
        String name = k.getName() != null ? k.getName().toUpperCase() : "";
        if (name.contains("TỔNG")) {
            return IconHelper.getIcon("lightbulb.png", 16, 16);
        } else if (name.contains("CHI NHÁNH")) {
            return IconHelper.getIcon("house.png", 16, 16);
        } else if (name.contains("BÁN HÀNG")) {
            return IconHelper.getIcon("application_home.png", 16, 16);
        }
        return IconHelper.getIcon("Folder.png", 16, 16);
    }

    private void setupMasterContextMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 14, 14));
        miAdd.addActionListener(e -> openAddPhieuNhapDialog());

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 14, 14));
        miEdit.addActionListener(e -> openEditPhieuNhapDialog());

        JMenuItem miDelete = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 14, 14));
        miDelete.addActionListener(e -> deleteSelectedVoucher());

        JMenuItem miExcel = new JMenuItem("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        miExcel.addActionListener(e -> ExcelExporter.exportTable(tablePhieuNhap, "Danh_Sach_Nhap_Kho", "Nhập kho", this));

        JMenuItem miPrint = new JMenuItem("In", IconHelper.getIcon("printer.png", 14, 14));
        miPrint.addActionListener(e -> printMasterTable());

        JMenuItem miCol = new JMenuItem("Chọn cột hiển thị...", IconHelper.getIcon("edit-column.png", 14, 14));
        miCol.addActionListener(e -> openColumnChooserDialog());

        popup.add(miAdd);
        popup.add(miEdit);
        popup.add(miDelete);
        popup.addSeparator();
        popup.add(miExcel);
        popup.add(miPrint);
        popup.addSeparator();
        popup.add(miCol);

        tablePhieuNhap.setComponentPopupMenu(popup);
    }

    private void loadInitialData() {
        // Load tree warehouses
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        DefaultMutableTreeNode nodeUnset = new DefaultMutableTreeNode("Chưa thiết lập");
        root.add(nodeUnset);

        List<KhoHang> khoList = khoHangDAO.getAll();
        cbKhoNhapFilter.removeAllItems();
        cbKhoNhapFilter.addItem(new KhoHang("", "-- Tất cả kho --", ""));

        for (KhoHang k : khoList) {
            root.add(new DefaultMutableTreeNode(k));
            cbKhoNhapFilter.addItem(k);
        }
        root.add(new DefaultMutableTreeNode("Thùng rác"));
        treeKho.setModel(new DefaultTreeModel(root));
        treeKho.setSelectionRow(0);

        // Load employees into filter combo
        cbNhanVienFilter.removeAllItems();
        cbNhanVienFilter.addItem(new NhanVien("", "-- Tất cả nhân viên --", ""));
        List<NhanVien> nvList = nhanVienDAO.getAll();
        for (NhanVien nv : nvList) {
            cbNhanVienFilter.addItem(nv);
        }

        // Load suppliers into filter combo
        cbNhaCungCapFilter.removeAllItems();
        cbNhaCungCapFilter.addItem(new KhachHang("", "-- Tất cả nhà cung cấp --", ""));
        List<kazuko.ex.model.danhmuc.NhaCungCap> nccList = nhaCungCapDAO.getAll();
        for (kazuko.ex.model.danhmuc.NhaCungCap ncc : nccList) {
            cbNhaCungCapFilter.addItem(new KhachHang(ncc.getId(), ncc.getName(), ncc.getMaNhaCungCap()));
        }
        List<KhachHang> khList = khachHangDAO.getAll();
        for (KhachHang kh : khList) {
            cbNhaCungCapFilter.addItem(kh);
        }

        buildMasterTableStructure();
        loadData();
    }

    private void syncWarehouseCombo(String khoId) {
        for (int i = 0; i < cbKhoNhapFilter.getItemCount(); i++) {
            KhoHang k = cbKhoNhapFilter.getItemAt(i);
            if (khoId.isEmpty() && (k.getId() == null || k.getId().isEmpty())) {
                cbKhoNhapFilter.setSelectedIndex(i);
                break;
            } else if (khoId.equals(k.getId())) {
                cbKhoNhapFilter.setSelectedIndex(i);
                break;
            }
        }
    }

    private void selectTreeNodeForWarehouse(String khoId) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeKho.getModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            Object obj = child.getUserObject();
            if (obj instanceof KhoHang && khoId.equals(((KhoHang) obj).getId())) {
                treeKho.setSelectionPath(new javax.swing.tree.TreePath(child.getPath()));
                break;
            }
        }
    }

    public void loadData() {
        Date tuNgay = dpkTuNgay.getDate();
        Date denNgay = dpkDenNgay.getDate();

        // Không hạn chế cứng kho ở query DB để giữ dữ liệu đầy đủ
        allVouchers = donHangDAO.getPhieuNhapKho(tuNgay, denNgay, null, null, null, null);
        applyFilter();
    }

    private void applyFilter() {
        String kw = txtLoc.getText().trim().toLowerCase();
        KhoHang selKho = (KhoHang) cbKhoNhapFilter.getSelectedItem();
        String filterKhoId = (selKho != null && selKho.getId() != null) ? selKho.getId().trim() : "";
        String filterKhoName = (selKho != null && selKho.getName() != null) ? selKho.getName().trim().toLowerCase() : "";

        NhanVien selNv = (NhanVien) cbNhanVienFilter.getSelectedItem();
        String filterNvId = (selNv != null && selNv.getId() != null) ? selNv.getId().trim() : "";
        String filterNvName = (selNv != null && selNv.getName() != null) ? selNv.getName().trim().toLowerCase() : "";

        KhachHang selNcc = (KhachHang) cbNhaCungCapFilter.getSelectedItem();
        String filterNccId = (selNcc != null && selNcc.getId() != null) ? selNcc.getId().trim() : "";
        String filterNccName = (selNcc != null && selNcc.getName() != null) ? selNcc.getName().trim().toLowerCase() : "";

        filteredVouchers.clear();
        for (DonHang dh : allVouchers) {
            if (!filterKhoId.isEmpty()) {
                boolean matchId = filterKhoId.equals(dh.getKhoNhapId());
                boolean matchName = dh.getTenKhoNhap() != null && dh.getTenKhoNhap().trim().equalsIgnoreCase(filterKhoName);
                if (!matchId && !matchName) {
                    continue;
                }
            }
            if (!filterNvId.isEmpty()) {
                boolean matchNvId = filterNvId.equals(dh.getNhanVienNhapId());
                boolean matchNvName = dh.getTenNhanVienNhap() != null && dh.getTenNhanVienNhap().trim().equalsIgnoreCase(filterNvName);
                if (!matchNvId && !matchNvName) {
                    continue;
                }
            }
            if (!filterNccId.isEmpty()) {
                boolean matchNccId = filterNccId.equals(dh.getNhaCungCapId()) || filterNccId.equals(dh.getKhachHangId());
                boolean matchNccName = dh.getTenNhaCungCap() != null && dh.getTenNhaCungCap().trim().equalsIgnoreCase(filterNccName);
                if (!matchNccId && !matchNccName) {
                    continue;
                }
            }
            if (!kw.isEmpty()) {
                String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                String kho = dh.getTenKhoNhap() != null ? dh.getTenKhoNhap().toLowerCase() : "";
                String nv = dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap().toLowerCase() : "";
                String ncc = dh.getTenNhaCungCap() != null ? dh.getTenNhaCungCap().toLowerCase() : "";
                String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                if (!name.contains(kw) && !kho.contains(kw) && !nv.contains(kw) && !ncc.contains(kw) && !dg.contains(kw)) {
                    continue;
                }
            }
            filteredVouchers.add(dh);
        }

        populateMasterTable();
    }

    private void buildMasterTableStructure() {
        Vector<String> colNames = new Vector<>();
        colNames.add(""); // Col 0: row indicator (STT)
        colNames.addAll(visibleColumns);

        masterModel.setDataVector(new Vector<>(), colNames);

        tablePhieuNhap.getColumnModel().getColumn(0).setPreferredWidth(35);
        tablePhieuNhap.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    setText("▶ " + (row + 1));
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setForeground(new Color(0, 100, 0));
                } else {
                    setText(String.valueOf(row + 1));
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    setForeground(Color.GRAY);
                }
                return this;
            }
        });

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 0; i < visibleColumns.size(); i++) {
            String colName = visibleColumns.get(i);
            int colIdx = i + 1;
            int w = getColumnDefaultWidth(colName);
            tablePhieuNhap.getColumnModel().getColumn(colIdx).setPreferredWidth(w);

            if (isNumericColumn(colName)) {
                tablePhieuNhap.getColumnModel().getColumn(colIdx).setCellRenderer(rightRenderer);
            }
        }
    }

    private int getColumnDefaultWidth(String col) {
        switch (col) {
            case "Số phiếu": return 90;
            case "Ngày": return 85;
            case "Nhà cung cấp": return 140;
            case "Kho nhập": return 120;
            case "Nhân viên nhập": return 110;
            case "Tiền hàng": return 100;
            case "Tiền giảm giá": return 85;
            case "Tỉ lệ giảm giá": return 75;
            case "Tài khoản ngân hàng": return 120;
            case "Tổng cộng": return 105;
            case "Còn lại": return 95;
            case "Tiền thanh toán": return 95;
            default: return 90;
        }
    }

    private boolean isNumericColumn(String col) {
        return "Tổng cộng".equals(col) || "Tiền giảm giá".equals(col) ||
                "Tỉ lệ giảm giá".equals(col) || "Tiền hàng".equals(col) ||
                "Còn lại".equals(col) || "Tiền thanh toán".equals(col) ||
                "Tiền thuế".equals(col) || "Tỉ lệ thuế".equals(col) ||
                "Phí vận chuyển".equals(col);
    }

    private void populateMasterTable() {
        masterModel.setRowCount(0);
        int selIndex = -1;

        for (int r = 0; r < filteredVouchers.size(); r++) {
            DonHang dh = filteredVouchers.get(r);
            Vector<Object> rowData = new Vector<>();
            rowData.add(String.valueOf(r + 1));

            for (String col : visibleColumns) {
                rowData.add(getValueForColumn(dh, col));
            }
            masterModel.addRow(rowData);

            if (selectedVoucher != null && selectedVoucher.getId() != null && selectedVoucher.getId().equals(dh.getId())) {
                selIndex = r;
            }
        }

        if (selIndex >= 0 && selIndex < tablePhieuNhap.getRowCount()) {
            tablePhieuNhap.setRowSelectionInterval(selIndex, selIndex);
        } else if (tablePhieuNhap.getRowCount() > 0) {
            tablePhieuNhap.setRowSelectionInterval(0, 0);
        } else {
            selectedVoucher = null;
            onVoucherSelected(null);
        }
    }

    private Object getValueForColumn(DonHang dh, String col) {
        switch (col) {
            case "Số phiếu": return dh.getName();
            case "Ngày": return dh.getNgay() != null ? sdfDate.format(dh.getNgay()) : "";
            case "Nhà cung cấp": return dh.getTenNhaCungCap() != null ? dh.getTenNhaCungCap() : "";
            case "Kho nhập": return dh.getTenKhoNhap() != null ? dh.getTenKhoNhap() : "";
            case "Nhân viên nhập": return dh.getTenNhanVienNhap() != null ? dh.getTenNhanVienNhap() : "";
            case "Tiền hàng": return CurrencyUtil.format(dh.getTienHang());
            case "Tiền giảm giá": return CurrencyUtil.format(dh.getTienGiamGia());
            case "Tỉ lệ giảm giá": return df.format(dh.getTiLeGiamGia());
            case "Tài khoản ngân hàng": return "";
            case "Tổng cộng": return CurrencyUtil.format(dh.getTongCong());
            case "Còn lại": return CurrencyUtil.format(dh.getConLai());
            case "Tiền thuế": return CurrencyUtil.format(dh.getTienThue());
            case "Tỉ lệ thuế": return df.format(dh.getTiLeThue());
            case "Phí vận chuyển": return CurrencyUtil.format(dh.getPhiVanChuyen());
            case "Tiền thanh toán": return CurrencyUtil.format(dh.getTienThanhToan());
            case "Cửa hàng": return dh.getTenCuaHang() != null ? dh.getTenCuaHang() : "";
            case "Ghi chú": return dh.getGhiChu() != null ? dh.getGhiChu() : "";
            default: return "";
        }
    }

    private void onVoucherSelected(DonHang dh) {
        if (dh == null) {
            lblAuditKhoiTao.setText("Khởi tạo: --/--/---- --:--");
            lblAuditNguoiTao.setText("Khởi tạo bởi: --");
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
            chiTietBottomModel.setRowCount(0);
            return;
        }

        // Cập nhật tab Thông tin
        String timeCreatedStr = dh.getTimeCreated() != null ? sdfDateTime.format(dh.getTimeCreated()) : (dh.getNgay() != null ? sdfDateTime.format(dh.getNgay()) : "--/--/----");
        String userCreatedStr = dh.getTenNguoiTao() != null ? dh.getTenNguoiTao() : "Administrator";
        lblAuditKhoiTao.setText("Khởi tạo: " + timeCreatedStr);
        lblAuditNguoiTao.setText("Khởi tạo bởi: " + userCreatedStr);

        if (dh.getTimeModified() != null) {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: " + sdfDateTime.format(dh.getTimeModified()));
            lblAuditNguoiSua.setText("Sửa đổi bởi: " + (dh.getTenNguoiSua() != null ? dh.getTenNguoiSua() : "--"));
        } else {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
        }

        // Cập nhật tab Nhập kho chi tiết (Image 3)
        List<DonHangChiTiet> details = dh.getChiTietLayHang();
        if (details == null || details.isEmpty()) {
            details = donHangDAO.getChiTietPhieuNhap(dh.getId());
            dh.setChiTietLayHang(details);
        }

        chiTietBottomModel.setRowCount(0);
        for (int i = 0; i < details.size(); i++) {
            DonHangChiTiet ct = details.get(i);
            chiTietBottomModel.addRow(new Object[]{
                    (i + 1),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    df.format(ct.getSoLuong()),
                    CurrencyUtil.format(ct.getDonGia()),
                    df.format(ct.getTiLeGiamGia()),
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    CurrencyUtil.format(ct.getTienGiam()),
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getKichThuoc() != null ? ct.getKichThuoc() : "",
                    ct.getHanSuDung() != null ? ct.getHanSuDung() : ""
            });
        }
    }

    private void openAddPhieuNhapDialog() {
        PhieuNhapKhoDialog dlg = new PhieuNhapKhoDialog(SwingUtilities.getWindowAncestor(this), () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openEditPhieuNhapDialog() {
        if (selectedVoucher == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu nhập kho cần chỉnh sửa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DonHang fullDh = donHangDAO.getPhieuNhapById(selectedVoucher.getId());
        PhieuNhapKhoDialog dlg = new PhieuNhapKhoDialog(SwingUtilities.getWindowAncestor(this), fullDh != null ? fullDh : selectedVoucher, () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void deleteSelectedVoucher() {
        if (selectedVoucher == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu nhập kho cần xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu nhập kho [" + selectedVoucher.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            boolean ok = donHangDAO.deletePhieuNhapKho(selectedVoucher.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu nhập kho thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa phiếu nhập kho này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printMasterTable() {
        try {
            tablePhieuNhap.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("DANH SÁCH PHIẾU NHẬP KHO"),
                    new java.text.MessageFormat("Trang {0}"));
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSummaryDialog() {
        double totalTienHang = 0;
        double totalGiamGia = 0;
        double totalTongCong = 0;
        double totalConLai = 0;

        for (DonHang dh : filteredVouchers) {
            totalTienHang += dh.getTienHang();
            totalGiamGia += dh.getTienGiamGia();
            totalTongCong += dh.getTongCong();
            totalConLai += dh.getConLai();
        }

        String msg = String.format(
                "TỔNG KẾT PHIẾU NHẬP KHO (%d chứng từ đang lọc):\n\n" +
                "• Tổng tiền hàng: %s đ\n" +
                "• Tổng tiền giảm: %s đ\n" +
                "• Tổng cộng tiền: %s đ\n" +
                "• Tổng còn nợ:   %s đ",
                filteredVouchers.size(),
                CurrencyUtil.format(totalTienHang),
                CurrencyUtil.format(totalGiamGia),
                CurrencyUtil.format(totalTongCong),
                CurrencyUtil.format(totalConLai)
        );
        JOptionPane.showMessageDialog(this, msg, "Tổng kết", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openColumnChooserDialog() {
        ChonCotHienThiDialog dlg = new ChonCotHienThiDialog(
                SwingUtilities.getWindowAncestor(this),
                visibleColumns,
                ChonCotHienThiDialog.ALL_POSSIBLE_NHAPKHO_COLUMNS,
                ChonCotHienThiDialog.DEFAULT_VISIBLE_NHAPKHO_COLUMNS
        );
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            visibleColumns = dlg.getResultVisibleColumns();
            buildMasterTableStructure();
            populateMasterTable();
        }
    }

    private void setupKeyBindings() {
        registerKeyboardAction(e -> openAddPhieuNhapDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> openEditPhieuNhapDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> deleteSelectedVoucher(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> txtLoc.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> loadData(), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
