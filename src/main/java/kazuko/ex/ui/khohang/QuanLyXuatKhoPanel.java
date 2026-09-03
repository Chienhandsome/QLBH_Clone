package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.hoadon.ChonCotHienThiDialog;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
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
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyXuatKhoPanel extends JPanel {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    // Left Panel Components
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<NhanVien> cbNhanVienFilter;
    private JComboBox<KhoHang> cbKhoXuatFilter;
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
    private JTable tablePhieuXuat;
    private DefaultTableModel masterModel;
    private List<String> visibleColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_XUATKHO_COLUMNS);
    private List<DonHang> allVouchers = new ArrayList<>();
    private List<DonHang> filteredVouchers = new ArrayList<>();
    private DonHang selectedVoucher = null;

    public JTable getTablePhieuXuat() { return tablePhieuXuat; }
    public DefaultTableModel getMasterModel() { return masterModel; }
    public List<DonHang> getAllVouchers() { return allVouchers; }
    public List<DonHang> getFilteredVouchers() { return filteredVouchers; }
    public JComboBox<KhoHang> getCbKhoXuatFilter() { return cbKhoXuatFilter; }
    public JComboBox<NhanVien> getCbNhanVienFilter() { return cbNhanVienFilter; }

    // Bottom Tabs
    private JTabbedPane bottomTabs;
    private JLabel lblAuditKhoiTao;
    private JLabel lblAuditNguoiTao;
    private JLabel lblAuditSuaDoi;
    private JLabel lblAuditNguoiSua;

    // Bottom Tab 5: Xuất kho chi tiết
    private JTable tableChiTietBottom;
    private DefaultTableModel chiTietBottomModel;

    private final DecimalFormat df = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

    public QuanLyXuatKhoPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(230, 240, 252));

        initComponents();
        setupKeyBindings();
        loadInitialData();
    }

    private void initComponents() {
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(230);
        splitMain.setDividerSize(5);
        splitMain.setBorder(null);

        // ==================== 1. KHUNG TRÁI: CÂY KHO HÀNG & BỘ LỌC ====================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(new Color(240, 246, 254));
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(195, 215, 235)));

        // Header "Kho hàng"
        JPanel pnlLeftHeader = new JPanel(new BorderLayout());
        pnlLeftHeader.setBackground(new Color(215, 230, 248));
        pnlLeftHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JLabel lblLeftTitle = new JLabel("Kho hàng");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(25, 55, 100));

        JButton btnConfigLeft = new JButton(IconHelper.getIcon("btnConfig.png", 16, 16));
        if (btnConfigLeft.getIcon() == null) {
            btnConfigLeft.setIcon(IconHelper.getIcon("report--pencil.png", 16, 16));
        }
        btnConfigLeft.setBorderPainted(false);
        btnConfigLeft.setContentAreaFilled(false);
        btnConfigLeft.setFocusPainted(false);
        btnConfigLeft.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        pnlLeftHeader.add(lblLeftTitle, BorderLayout.WEST);
        pnlLeftHeader.add(btnConfigLeft, BorderLayout.EAST);
        leftPanel.add(pnlLeftHeader, BorderLayout.NORTH);

        // Center: Date & Employee & Warehouse Filter + Mini Toolbar + Tree
        JPanel pnlLeftCenter = new JPanel(new BorderLayout());
        pnlLeftCenter.setOpaque(false);

        // Filter Controls Panel
        JPanel pnlFilters = new JPanel(new GridBagLayout());
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Từ ngày
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Từ:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkTuNgay = new DatePickerField();
        // Default to beginning of August 2026 to see demo data
        Calendar calTu = Calendar.getInstance();
        calTu.set(2026, Calendar.AUGUST, 4);
        dpkTuNgay.setDate(calTu.getTime());
        dpkTuNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkTuNgay, gbc);

        // Đến ngày
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Đến:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkDenNgay = new DatePickerField();
        Calendar calDen = Calendar.getInstance();
        calDen.set(2026, Calendar.SEPTEMBER, 3);
        dpkDenNgay.setDate(calDen.getTime());
        dpkDenNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkDenNgay, gbc);

        // Nhân viên xuất
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Nhân viên:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhanVienFilter = new JComboBox<>();
        cbNhanVienFilter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbNhanVienFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbNhanVienFilter, gbc);

        // Kho xuất
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Kho xuất:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbKhoXuatFilter = new JComboBox<>();
        cbKhoXuatFilter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbKhoXuatFilter.addActionListener(e -> {
            KhoHang sel = (KhoHang) cbKhoXuatFilter.getSelectedItem();
            if (sel != null && sel.getId() != null && !sel.getId().isEmpty()) {
                selectTreeNodeForWarehouse(sel.getId());
            }
            applyFilter();
        });
        pnlFilters.add(cbKhoXuatFilter, gbc);

        // Mini action buttons row (+, RecycleBin, Refresh, Layout)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel pnlMiniAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnlMiniAction.setOpaque(false);

        JButton btnMiniAdd = createMiniIconButton("plus.png");
        JButton btnMiniTrash = createMiniIconButton("RecycleBin.png");
        JButton btnMiniRefresh = createMiniIconButton("Refresh.png");
        JButton btnMiniLayout = createMiniIconButton("edit-column.png");

        btnMiniRefresh.addActionListener(e -> loadData());
        btnMiniAdd.addActionListener(e -> openAddPhieuXuatDialog());

        pnlMiniAction.add(btnMiniAdd);
        pnlMiniAction.add(btnMiniTrash);
        pnlMiniAction.add(btnMiniRefresh);
        pnlMiniAction.add(btnMiniLayout);
        pnlFilters.add(pnlMiniAction, gbc);

        pnlLeftCenter.add(pnlFilters, BorderLayout.NORTH);

        // Tree Kho Hàng
        treeKho = new JTree();
        treeKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeKho.setRootVisible(true);
        treeKho.setShowsRootHandles(true);
        treeKho.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                    if ("Tất cả".equals(obj)) {
                        label.setText("Tất cả");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                    } else if ("Thùng rác".equals(obj)) {
                        label.setText("Thùng rác");
                        label.setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                    } else if (obj instanceof KhoHang) {
                        KhoHang kh = (KhoHang) obj;
                        label.setText(kh.getName());
                        String iconName = kh.getAvatarIcon();
                        if ("lightbulb".equals(iconName)) {
                            label.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                        } else if ("red_house".equals(iconName) || "purple_house".equals(iconName)) {
                            label.setIcon(IconHelper.getIcon("navigation-180-frame.png", 16, 16));
                        } else {
                            label.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                        }
                    }
                }
                return label;
            }
        });

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

        JScrollPane treeScroll = new JScrollPane(treeKho);
        treeScroll.setBorder(new MatteBorder(1, 0, 0, 0, new Color(205, 220, 238)));
        pnlLeftCenter.add(treeScroll, BorderLayout.CENTER);

        leftPanel.add(pnlLeftCenter, BorderLayout.CENTER);
        splitMain.setLeftComponent(leftPanel);

        // ==================== 2. KHUNG PHẢI: TOOLBAR, BẢNG MASTER & BOTTOM TABS ====================
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(new Color(230, 240, 252));

        // Top: Tab header "Phiếu xuất" + Toolbar
        JPanel pnlRightTop = new JPanel(new BorderLayout());
        pnlRightTop.setBackground(new Color(230, 240, 252));

        // Tab Header Button Bar
        JPanel pnlTabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlTabHeader.setBackground(new Color(215, 230, 248));
        pnlTabHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));

        JButton btnTabPhieuXuat = new JButton("Phiếu xuất");
        btnTabPhieuXuat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTabPhieuXuat.setBackground(Color.WHITE);
        btnTabPhieuXuat.setForeground(new Color(20, 50, 95));
        btnTabPhieuXuat.setFocusPainted(false);
        btnTabPhieuXuat.setBorder(new CompoundBorder(new LineBorder(new Color(175, 200, 230), 1), new EmptyBorder(4, 12, 4, 12)));
        pnlTabHeader.add(btnTabPhieuXuat);
        pnlRightTop.add(pnlTabHeader, BorderLayout.NORTH);

        // Action Toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        toolBar.setBackground(new Color(235, 243, 253));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(200, 218, 238)));

        JLabel lblLoc = new JLabel("Lọc (F3):");
        lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolBar.add(lblLoc);

        txtLoc = new JTextField(11);
        txtLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLoc.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        toolBar.add(txtLoc);

        btnAdd = createToolbarButton("Thêm mới (Insert)", "plus.png", new Color(20, 120, 40));
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit = createToolbarButton("Chỉnh sửa (F4)", "pencil.png", Color.DARK_GRAY);
        btnDelete = createToolbarButton("Xóa (Del)", "cross.png", new Color(180, 40, 40));
        btnAddExcel = createToolbarButton("Thêm excel", "export-excel.png", Color.DARK_GRAY);
        btnExcel = createToolbarButton("Xuất excel", "export-excel.png", Color.DARK_GRAY);
        btnPrint = createToolbarButton("In", "printer.png", Color.DARK_GRAY);
        btnTong = createToolbarButton("∑ Tổng", "sum.png", Color.DARK_GRAY);
        btnPhanTich = createToolbarButton("P.tích", "chart.png", Color.DARK_GRAY);

        btnAdd.addActionListener(e -> openAddPhieuXuatDialog());
        btnEdit.addActionListener(e -> openEditPhieuXuatDialog());
        btnDelete.addActionListener(e -> deleteSelectedVoucher());
        btnExcel.addActionListener(e -> ExcelExporter.exportTable(tablePhieuXuat, "Danh_Sach_Xuat_Kho", "Xuất kho", this));
        btnPrint.addActionListener(e -> printMasterTable());
        btnTong.addActionListener(e -> showTotalSummaryDialog());

        toolBar.add(btnAdd);
        toolBar.add(btnEdit);
        toolBar.add(btnDelete);
        toolBar.add(btnAddExcel);
        toolBar.add(btnExcel);
        toolBar.add(btnPrint);
        toolBar.add(btnTong);
        toolBar.add(btnPhanTich);

        pnlRightTop.add(toolBar, BorderLayout.SOUTH);
        rightPanel.add(pnlRightTop, BorderLayout.NORTH);

        // Center: SplitPane for Master Table (Top) & Bottom Details Pane (Bottom)
        JSplitPane splitCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitCenter.setDividerLocation(360);
        splitCenter.setDividerSize(6);
        splitCenter.setBorder(null);

        // --- Master Table ---
        masterModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablePhieuXuat = new JTable(masterModel);
        tablePhieuXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePhieuXuat.setRowHeight(22);
        tablePhieuXuat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePhieuXuat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablePhieuXuat.setShowGrid(true);
        tablePhieuXuat.setGridColor(new Color(225, 235, 245));
        tablePhieuXuat.setSelectionBackground(new Color(200, 242, 205)); // Soft green highlight matching Screenshot 1
        tablePhieuXuat.setSelectionForeground(Color.BLACK);

        JTableHeader header = tablePhieuXuat.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(235, 243, 252));
        header.setPreferredSize(new Dimension(100, 25));

        tablePhieuXuat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablePhieuXuat.getSelectedRow();
                if (row >= 0 && row < filteredVouchers.size()) {
                    selectedVoucher = filteredVouchers.get(row);
                    onVoucherSelected(selectedVoucher);
                }
            }
        });

        tablePhieuXuat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openEditPhieuXuatDialog();
                }
            }
        });

        // Master Context Menu (Right Click)
        setupMasterContextMenu();

        JScrollPane masterScroll = new JScrollPane(tablePhieuXuat);
        masterScroll.setBorder(null);
        splitCenter.setTopComponent(masterScroll);

        // --- Bottom JTabbedPane ---
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
        // Tab 5: Xuất kho chi tiết (Image 4)
        bottomTabs.addTab("Xuất kho chi tiết", createDetailItemsTab());

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

        String[] detailCols = {
                "", "Mặt hàng", "Đơn giá", "Giảm giá %", "Thành tiền", "Ghi chú",
                "Mã hàng", "Đơn vị tính", "Số lượng", "Tiền giảm", "Kích thước", "Hạn sử dụng"
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

        int[] w = {35, 180, 85, 75, 95, 80, 80, 70, 70, 80, 75, 80};
        for (int i = 0; i < w.length; i++) {
            tableChiTietBottom.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableChiTietBottom.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(tableChiTietBottom);
        scroll.setBorder(null);
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createEmptyTab(String msg) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 25));
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl.setForeground(Color.GRAY);
        pnl.add(lbl);
        return pnl;
    }

    private JButton createToolbarButton(String text, String iconName, Color textColor) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(textColor);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(195, 215, 238), 1), new EmptyBorder(3, 8, 3, 8)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMiniIconButton(String iconName) {
        JButton btn = new JButton(IconHelper.getIcon(iconName, 14, 14));
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(190, 210, 235), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupKeyBindings() {
        registerKeyboardAction(e -> openAddPhieuXuatDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> openEditPhieuXuatDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> deleteSelectedVoucher(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> txtLoc.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> loadData(), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void setupMasterContextMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 14, 14));
        miAdd.addActionListener(e -> openAddPhieuXuatDialog());

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 14, 14));
        miEdit.addActionListener(e -> openEditPhieuXuatDialog());

        JMenuItem miDelete = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 14, 14));
        miDelete.addActionListener(e -> deleteSelectedVoucher());

        JMenuItem miExcel = new JMenuItem("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        miExcel.addActionListener(e -> ExcelExporter.exportTable(tablePhieuXuat, "Danh_Sach_Xuat_Kho", "Xuất kho", this));

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

        tablePhieuXuat.setComponentPopupMenu(popup);
    }

    private void loadInitialData() {
        // Load tree warehouses
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        DefaultMutableTreeNode nodeUnset = new DefaultMutableTreeNode("Chưa thiết lập");
        root.add(nodeUnset);

        List<KhoHang> khoList = khoHangDAO.getAll();
        cbKhoXuatFilter.removeAllItems();
        cbKhoXuatFilter.addItem(new KhoHang("", "-- Tất cả kho --", ""));

        for (KhoHang k : khoList) {
            root.add(new DefaultMutableTreeNode(k));
            cbKhoXuatFilter.addItem(k);
        }
        root.add(new DefaultMutableTreeNode("Thùng rác"));
        treeKho.setModel(new DefaultTreeModel(root));
        treeKho.setSelectionRow(0);

        // Load employees into filter combo
        cbNhanVienFilter.removeAllItems();
        NhanVien nvAll = new NhanVien("", "-- Tất cả nhân viên --", "");
        cbNhanVienFilter.addItem(nvAll);
        List<NhanVien> nvList = nhanVienDAO.getAll();
        for (NhanVien nv : nvList) {
            cbNhanVienFilter.addItem(nv);
        }

        buildMasterTableStructure();
        loadData();
    }

    private void syncWarehouseCombo(String khoId) {
        for (int i = 0; i < cbKhoXuatFilter.getItemCount(); i++) {
            KhoHang k = cbKhoXuatFilter.getItemAt(i);
            if (khoId.isEmpty() && (k.getId() == null || k.getId().isEmpty())) {
                cbKhoXuatFilter.setSelectedIndex(i);
                break;
            } else if (khoId.equals(k.getId())) {
                cbKhoXuatFilter.setSelectedIndex(i);
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

        // Không hạn chế cứng kho ở query DB để giữ allVouchers đầy đủ
        allVouchers = donHangDAO.getPhieuXuatKho(tuNgay, denNgay, null, null, null);
        applyFilter();
    }

    private void applyFilter() {
        String kw = txtLoc.getText().trim().toLowerCase();
        KhoHang selKho = (KhoHang) cbKhoXuatFilter.getSelectedItem();
        String filterKhoId = (selKho != null && selKho.getId() != null) ? selKho.getId().trim() : "";
        String filterKhoName = (selKho != null && selKho.getName() != null) ? selKho.getName().trim().toLowerCase() : "";

        NhanVien selNv = (NhanVien) cbNhanVienFilter.getSelectedItem();
        String filterNvId = (selNv != null && selNv.getId() != null) ? selNv.getId().trim() : "";
        String filterNvName = (selNv != null && selNv.getName() != null) ? selNv.getName().trim().toLowerCase() : "";

        filteredVouchers.clear();
        for (DonHang dh : allVouchers) {
            if (!filterKhoId.isEmpty()) {
                boolean matchId = filterKhoId.equals(dh.getKhoXuatId());
                boolean matchName = dh.getTenKho() != null && dh.getTenKho().trim().equalsIgnoreCase(filterKhoName);
                if (!matchId && !matchName) {
                    continue;
                }
            }
            if (!filterNvId.isEmpty()) {
                boolean matchNvId = filterNvId.equals(dh.getNhanVienXuatId());
                boolean matchNvName = dh.getTenNhanVien() != null && dh.getTenNhanVien().trim().equalsIgnoreCase(filterNvName);
                if (!matchNvId && !matchNvName) {
                    continue;
                }
            }
            if (!kw.isEmpty()) {
                String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
                String kho = dh.getTenKho() != null ? dh.getTenKho().toLowerCase() : "";
                String nv = dh.getTenNhanVien() != null ? dh.getTenNhanVien().toLowerCase() : "";
                String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
                if (!name.contains(kw) && !kho.contains(kw) && !nv.contains(kw) && !dg.contains(kw)) {
                    continue;
                }
            }
            filteredVouchers.add(dh);
        }

        populateMasterTable();
    }

    private void buildMasterTableStructure() {
        Vector<String> colNames = new Vector<>();
        colNames.add(""); // STT / Indicator column
        for (String col : visibleColumns) {
            colNames.add(col);
        }

        masterModel.setDataVector(new Vector<>(), colNames);

        // STT indicator column width
        tablePhieuXuat.getColumnModel().getColumn(0).setPreferredWidth(35);

        // Adjust widths & alignment
        for (int i = 0; i < visibleColumns.size(); i++) {
            int tableColIdx = i + 1;
            String colName = visibleColumns.get(i);
            int width = getColumnDefaultWidth(colName);
            tablePhieuXuat.getColumnModel().getColumn(tableColIdx).setPreferredWidth(width);

            if (isNumericColumn(colName)) {
                DefaultTableCellRenderer r = new DefaultTableCellRenderer();
                r.setHorizontalAlignment(SwingConstants.RIGHT);
                tablePhieuXuat.getColumnModel().getColumn(tableColIdx).setCellRenderer(r);
            }
        }
    }

    private int getColumnDefaultWidth(String col) {
        switch (col) {
            case "Ngày": return 85;
            case "Kho xuất": return 120;
            case "Nhân viên xuất": return 110;
            case "Tổng cộng": return 110;
            case "Số phiếu": return 90;
            case "Tiền giảm giá": return 85;
            case "Tỉ lệ giảm giá": return 75;
            case "Tiền hàng": return 110;
            case "Cửa hàng": return 110;
            case "Còn lại": return 100;
            case "Thanh toán": return 85;
            case "Tài khoản ngân hàng": return 120;
            default: return 90;
        }
    }

    private boolean isNumericColumn(String col) {
        return "Tổng cộng".equals(col) || "Tiền giảm giá".equals(col) ||
                "Tỉ lệ giảm giá".equals(col) || "Tiền hàng".equals(col) ||
                "Còn lại".equals(col) || "Thanh toán".equals(col) ||
                "Tiền mặt".equals(col) || "Chuyển khoản".equals(col);
    }

    private void populateMasterTable() {
        masterModel.setRowCount(0);
        int selIndex = -1;

        for (int r = 0; r < filteredVouchers.size(); r++) {
            DonHang dh = filteredVouchers.get(r);
            Vector<Object> rowData = new Vector<>();
            // Row Indicator matching Screenshot 1 (▶ STT or STT)
            rowData.add(String.valueOf(r + 1));

            for (String col : visibleColumns) {
                rowData.add(getValueForColumn(dh, col));
            }
            masterModel.addRow(rowData);

            if (selectedVoucher != null && selectedVoucher.getId() != null && selectedVoucher.getId().equals(dh.getId())) {
                selIndex = r;
            }
        }

        if (selIndex >= 0 && selIndex < tablePhieuXuat.getRowCount()) {
            tablePhieuXuat.setRowSelectionInterval(selIndex, selIndex);
        } else if (tablePhieuXuat.getRowCount() > 0) {
            tablePhieuXuat.setRowSelectionInterval(0, 0);
        } else {
            selectedVoucher = null;
            onVoucherSelected(null);
        }
    }

    private Object getValueForColumn(DonHang dh, String col) {
        switch (col) {
            case "Ngày":
                return dh.getNgay() != null ? DateUtil.formatDate(dh.getNgay()) : "";
            case "Kho xuất":
                return dh.getTenKho() != null ? dh.getTenKho() : "KHO TỔNG";
            case "Nhân viên xuất":
                return dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "Administrator";
            case "Tổng cộng":
                return CurrencyUtil.formatMoney(dh.getTongCong());
            case "Số phiếu":
                return dh.getName() != null ? dh.getName() : "";
            case "Tiền giảm giá":
                return CurrencyUtil.formatMoney(dh.getTienGiamGia());
            case "Tỉ lệ giảm giá":
                return df.format(dh.getTiLeGiamGia());
            case "Tiền hàng":
                return CurrencyUtil.formatMoney(dh.getTienHang());
            case "Cửa hàng":
                return dh.getTenCuaHang() != null ? dh.getTenCuaHang() : "TRỤ SỞ CHÍNH";
            case "Còn lại":
                return CurrencyUtil.formatMoney(dh.getConLai());
            case "Thanh toán":
                return CurrencyUtil.formatMoney(dh.getTienThanhToan());
            case "Tài khoản ngân hàng":
                return dh.getTaiKhoanNganHangId() != null ? dh.getTaiKhoanNganHangId() : "";
            case "Phí vận chuyển":
                return CurrencyUtil.formatMoney(dh.getPhiVanChuyen());
            case "Mã voucher":
                return dh.getMaVoucher() != null ? dh.getMaVoucher() : "";
            case "Thẻ tt":
                return CurrencyUtil.formatMoney(dh.getTienTheATM());
            case "Thẻ trả trước":
                return CurrencyUtil.formatMoney(dh.getTienTheTraTruoc());
            case "Trừ tích lũy":
                return CurrencyUtil.formatMoney(dh.getTienTruTichLuy());
            case "Điểm giảm":
                return "0";
            case "Tiền mặt":
                return CurrencyUtil.formatMoney(dh.getTienMat());
            case "Chuyển khoản":
                return CurrencyUtil.formatMoney(dh.getTienChuyenKhoan());
            case "Thẻ":
                return "0";
            case "Bàn":
                return "";
            case "Ghi chú":
                return dh.getGhiChu() != null ? dh.getGhiChu() : "";
            default:
                return "";
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

        // Tab 1: Update Audit Info
        Date tCreated = dh.getTimeCreated() != null ? dh.getTimeCreated() : dh.getNgay();
        String strCreated = tCreated != null ? sdfDateTime.format(tCreated) : "--";
        lblAuditKhoiTao.setText("Khởi tạo: " + strCreated);
        lblAuditNguoiTao.setText("Khởi tạo bởi: " + (dh.getTenNguoiTao() != null ? dh.getTenNguoiTao() : "Administrator"));

        if (dh.getTimeModified() != null) {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: " + sdfDateTime.format(dh.getTimeModified()));
            lblAuditNguoiSua.setText("Sửa đổi bởi: " + (dh.getTenNguoiSua() != null ? dh.getTenNguoiSua() : "Administrator"));
        } else {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
        }

        // Tab 5: Update Detail Items Table (Image 4)
        List<DonHangChiTiet> details = dh.getChiTietLayHang();
        if (details == null || details.isEmpty()) {
            details = donHangDAO.getChiTietPhieuXuat(dh.getId());
            dh.setChiTietLayHang(details);
        }

        chiTietBottomModel.setRowCount(0);
        for (int i = 0; i < details.size(); i++) {
            DonHangChiTiet ct = details.get(i);
            chiTietBottomModel.addRow(new Object[]{
                    (i + 1),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    CurrencyUtil.formatMoney(ct.getDonGia()),
                    df.format(ct.getTiLeGiamGia()) + "%",
                    CurrencyUtil.formatMoney(ct.getThanhTien()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    df.format(ct.getSoLuong()),
                    CurrencyUtil.formatMoney(ct.getTienGiam()),
                    ct.getKichThuoc() != null ? ct.getKichThuoc() : "",
                    ct.getHanSuDung() != null ? ct.getHanSuDung() : ""
            });
        }
    }

    private void openAddPhieuXuatDialog() {
        PhieuXuatKhoDialog dlg = new PhieuXuatKhoDialog(SwingUtilities.getWindowAncestor(this), this::loadData);
        dlg.setVisible(true);
    }

    private void openEditPhieuXuatDialog() {
        if (selectedVoucher == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu xuất kho cần chỉnh sửa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DonHang fullDh = donHangDAO.getPhieuXuatById(selectedVoucher.getId());
        PhieuXuatKhoDialog dlg = new PhieuXuatKhoDialog(SwingUtilities.getWindowAncestor(this), fullDh != null ? fullDh : selectedVoucher, this::loadData);
        dlg.setVisible(true);
    }

    private void deleteSelectedVoucher() {
        if (selectedVoucher == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu xuất kho cần xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu xuất kho: " + selectedVoucher.getName() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (donHangDAO.deletePhieuXuatKho(selectedVoucher.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu xuất kho thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa phiếu xuất kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openColumnChooserDialog() {
        ChonCotHienThiDialog dlg = new ChonCotHienThiDialog(
                SwingUtilities.getWindowAncestor(this),
                visibleColumns,
                ChonCotHienThiDialog.ALL_POSSIBLE_XUATKHO_COLUMNS,
                ChonCotHienThiDialog.DEFAULT_VISIBLE_XUATKHO_COLUMNS
        );
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            visibleColumns = dlg.getResultVisibleColumns();
            buildMasterTableStructure();
            populateMasterTable();
        }
    }

    private void printMasterTable() {
        try {
            tablePhieuXuat.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi in bảng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTotalSummaryDialog() {
        double tongTienHang = 0.0;
        double tongTienGiam = 0.0;
        double tongCong = 0.0;
        double tongThanhToan = 0.0;
        double tongConLai = 0.0;

        for (DonHang dh : filteredVouchers) {
            tongTienHang += dh.getTienHang();
            tongTienGiam += dh.getTienGiamGia();
            tongCong += dh.getTongCong();
            tongThanhToan += dh.getTienThanhToan();
            tongConLai += dh.getConLai();
        }

        String msg = String.format(
                "<html><b>TỔNG HỢP XUẤT KHO (%d chứng từ)</b><br><br>" +
                "Tổng tiền hàng: <b>%s</b> đ<br>" +
                "Tổng giảm giá: <b>%s</b> đ<br>" +
                "Tổng cộng: <b style='color:red;'>%s</b> đ<br>" +
                "Tổng thanh toán: <b>%s</b> đ<br>" +
                "Tổng còn lại: <b>%s</b> đ</html>",
                filteredVouchers.size(),
                CurrencyUtil.formatMoney(tongTienHang),
                CurrencyUtil.formatMoney(tongTienGiam),
                CurrencyUtil.formatMoney(tongCong),
                CurrencyUtil.formatMoney(tongThanhToan),
                CurrencyUtil.formatMoney(tongConLai)
        );

        JOptionPane.showMessageDialog(this, msg, "∑ Tổng hợp phiếu xuất kho", JOptionPane.INFORMATION_MESSAGE);
    }
}
