package kazuko.ex.ui.dathang;

import kazuko.ex.dao.DatHangNCCDAO;
import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.dathang.DatHangNCC;
import kazuko.ex.model.dathang.DatHangNCCChiTiet;
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

public class DatHangNhaCungCapPanel extends JPanel {

    private final DatHangNCCDAO datHangNCCDAO = new DatHangNCCDAO();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();

    // Left filter controls
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<NhaCungCap> cbNhaCungCapFilter;

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
    private final List<DatHangNCC> allOrders = new ArrayList<>();
    private final List<DatHangNCC> filteredOrders = new ArrayList<>();
    private DatHangNCC selectedOrder = null;

    // Bottom Tabs
    private JTabbedPane bottomTabs;
    private JLabel lblAuditKhoiTao;
    private JLabel lblAuditNguoiTao;
    private JLabel lblAuditSuaDoi;
    private JLabel lblAuditNguoiSua;

    // Bottom Detail Table
    private JTable tableChiTietBottom;
    private DefaultTableModel chiTietBottomModel;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    public JTable getTableMaster() { return tableMaster; }
    public DefaultTableModel getMasterModel() { return masterModel; }
    public List<DatHangNCC> getAllOrders() { return allOrders; }
    public List<DatHangNCC> getFilteredOrders() { return filteredOrders; }

    public DatHangNhaCungCapPanel() {
        setLayout(new BorderLayout(0, 0));
        initComponents();
        loadInitialData();
        setupKeyBindings();
    }

    private void initComponents() {
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(180);
        splitMain.setDividerSize(4);
        splitMain.setBorder(null);

        // ==========================================
        // 1. LEFT PANEL: Lọc dữ liệu
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(new Color(245, 248, 253));
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(195, 215, 238)));

        // Header "Lọc dữ liệu"
        JPanel pnlLeftHeader = new JPanel(new BorderLayout());
        pnlLeftHeader.setBackground(new Color(230, 240, 252));
        pnlLeftHeader.setBorder(new EmptyBorder(4, 8, 4, 6));

        JLabel lblLeftTitle = new JLabel("Lọc dữ liệu");
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
        JPanel pnlFilters = new JPanel(new GridBagLayout());
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(8, 6, 6, 6));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 2, 3, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Ngày Từ
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Từ:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkTuNgay = new DatePickerField();
        Calendar calTu = Calendar.getInstance();
        calTu.set(2026, Calendar.SEPTEMBER, 1);
        dpkTuNgay.setDate(calTu.getTime());
        dpkTuNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkTuNgay, gbc);

        // Ngày Đến
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Đến:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        dpkDenNgay = new DatePickerField();
        Calendar calDen = Calendar.getInstance();
        calDen.set(2026, Calendar.SEPTEMBER, 30);
        dpkDenNgay.setDate(calDen.getTime());
        dpkDenNgay.addPropertyChangeListener("date", e -> loadData());
        pnlFilters.add(dpkDenNgay, gbc);

        // Nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlFilters.add(new JLabel("Nhà CC:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cbNhaCungCapFilter = new JComboBox<>();
        cbNhaCungCapFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhaCungCapFilter.addActionListener(e -> applyFilter());
        pnlFilters.add(cbNhaCungCapFilter, gbc);

        leftPanel.add(pnlFilters, BorderLayout.CENTER);
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

        // Tab indicator "Dữ liệu"
        JPanel pnlTabTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTabTitle.setBackground(Color.WHITE);
        pnlTabTitle.setBorder(new MatteBorder(1, 1, 0, 1, new Color(195, 215, 238)));
        JLabel lblTabTitle = new JLabel("Dữ liệu");
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
        btnExportExcel.addActionListener(e -> ExcelExporter.exportTable(tableMaster, "Dat_Hang_Nha_Cung_Cap", "Đặt hàng NCC", this));

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

        // Columns matching Image 1: [STT], Số phiếu, Ngày, Nhà cung cấp, Tổng cộng, Phí vận chuyển, Tiền giảm giá, Tỉ lệ giảm giá, Tiền thuế, Tỉ lệ thuế, Tiền hàng, Ghi chú
        String[] cols = {
                "", "Số phiếu", "Ngày", "Nhà cung cấp", "Tổng cộng", "Phí vận chuyển",
                "Tiền giảm giá", "Tỉ lệ giảm giá", "Tiền thuế", "Tỉ lệ thuế", "Tiền hàng", "Ghi chú"
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
        // Soft green highlight matching Image 1
        tableMaster.setSelectionBackground(new Color(210, 238, 215));
        tableMaster.setSelectionForeground(Color.BLACK);

        JTableHeader masterHeader = tableMaster.getTableHeader();
        masterHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        masterHeader.setBackground(new Color(245, 248, 253));
        masterHeader.setForeground(new Color(40, 50, 70));
        masterHeader.setPreferredSize(new Dimension(100, 24));

        int[] widths = {40, 110, 90, 160, 100, 90, 85, 75, 80, 70, 100, 100};
        for (int i = 0; i < widths.length && i < tableMaster.getColumnCount(); i++) {
            tableMaster.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
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

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{4, 5, 6, 7, 8, 9, 10}) {
            tableMaster.getColumnModel().getColumn(c).setCellRenderer(rightRenderer);
        }

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

        // Bottom JTabbedPane (11 Tabs matching Image 1)
        bottomTabs = new JTabbedPane();
        bottomTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomTabs.setPreferredSize(new Dimension(100, 160));

        // Tab 1: Thông tin
        bottomTabs.addTab("Thông tin", createAuditInfoTab());
        // Tab 2: Đặt hàng nhà cung cấp chi tiết
        bottomTabs.addTab("Đặt hàng nhà cung cấp chi tiết", createDetailItemsTab());
        // Tabs 3-11
        bottomTabs.addTab("Đơn hàng", createEmptyTab("Chưa có đơn hàng liên kết"));
        bottomTabs.addTab("Phiếu nhập kho", createEmptyTab("Chưa có phiếu nhập kho"));
        bottomTabs.addTab("Phiếu xuất kho", createEmptyTab("Chưa có phiếu xuất kho"));
        bottomTabs.addTab("Phiếu chuyển kho", createEmptyTab("Chưa có phiếu chuyển kho"));
        bottomTabs.addTab("Phiếu kiểm kê", createEmptyTab("Chưa có phiếu kiểm kê"));
        bottomTabs.addTab("Sản xuất", createEmptyTab("Chưa có sản xuất"));
        bottomTabs.addTab("Phiếu thu", createEmptyTab("Chưa có phiếu thu"));
        bottomTabs.addTab("Phiếu chi", createEmptyTab("Chưa có phiếu chi"));
        bottomTabs.addTab("Phiếu thu công nợ", createEmptyTab("Chưa có phiếu thu công nợ"));

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
        lblAuditKhoiTao = new JLabel("Khởi tạo: 03/09/2026 08:46 AM");
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
                "", "Mã hàng", "Tên hàng", "Ghi chú", "ĐVT",
                "Giảm giá %", "Đơn giá", "Thành tiền", "Số lượng"
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

        int[] w = {35, 80, 180, 100, 60, 75, 90, 95, 70};
        for (int i = 0; i < w.length; i++) {
            tableChiTietBottom.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableChiTietBottom.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableChiTietBottom.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableChiTietBottom.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
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

    private void setupMasterContextMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 14, 14));
        miAdd.addActionListener(e -> openAddDialog());

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 14, 14));
        miEdit.addActionListener(e -> openEditDialog());

        JMenuItem miDelete = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 14, 14));
        miDelete.addActionListener(e -> deleteSelectedOrder());

        JMenuItem miExcel = new JMenuItem("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        miExcel.addActionListener(e -> ExcelExporter.exportTable(tableMaster, "Dat_Hang_Nha_Cung_Cap", "Đặt hàng NCC", this));

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
        cbNhaCungCapFilter.removeAllItems();
        cbNhaCungCapFilter.addItem(new NhaCungCap("", "", "-- Tất cả nhà cung cấp --", "", "", ""));
        List<NhaCungCap> nccList = nhaCungCapDAO.getAll();
        for (NhaCungCap ncc : nccList) {
            cbNhaCungCapFilter.addItem(ncc);
        }

        loadData();
    }

    public void loadData() {
        Date tuNgay = dpkTuNgay.getDate();
        Date denNgay = dpkDenNgay.getDate();

        allOrders.clear();
        allOrders.addAll(datHangNCCDAO.getAll(tuNgay, denNgay, null, null));
        applyFilter();
    }

    private void applyFilter() {
        String kw = txtLoc.getText().trim().toLowerCase();
        NhaCungCap selNcc = (NhaCungCap) cbNhaCungCapFilter.getSelectedItem();
        String filterNccId = (selNcc != null && selNcc.getId() != null) ? selNcc.getId().trim() : "";
        String filterNccName = (selNcc != null && selNcc.getName() != null) ? selNcc.getName().trim().toLowerCase() : "";

        filteredOrders.clear();
        for (DatHangNCC o : allOrders) {
            if (!filterNccId.isEmpty()) {
                boolean matchId = filterNccId.equals(o.getNhaCungCapId());
                boolean matchName = o.getTenNhaCungCap() != null && o.getTenNhaCungCap().trim().equalsIgnoreCase(filterNccName);
                if (!matchId && !matchName) continue;
            }
            if (!kw.isEmpty()) {
                String name = o.getName() != null ? o.getName().toLowerCase() : "";
                String ncc = o.getTenNhaCungCap() != null ? o.getTenNhaCungCap().toLowerCase() : "";
                String note = o.getNote() != null ? o.getNote().toLowerCase() : "";
                if (!name.contains(kw) && !ncc.contains(kw) && !note.contains(kw)) {
                    continue;
                }
            }
            filteredOrders.add(o);
        }

        populateMasterTable();
    }

    private void populateMasterTable() {
        masterModel.setRowCount(0);
        int selIndex = -1;

        for (int r = 0; r < filteredOrders.size(); r++) {
            DatHangNCC o = filteredOrders.get(r);
            Vector<Object> rowData = new Vector<>();
            rowData.add(String.valueOf(r + 1));
            rowData.add(o.getName() != null ? o.getName() : "");
            rowData.add(o.getNgay() != null ? sdfDate.format(o.getNgay()) : "");
            rowData.add(o.getTenNhaCungCap() != null ? o.getTenNhaCungCap() : "");
            rowData.add(CurrencyUtil.format(o.getTongCong()));
            rowData.add(CurrencyUtil.format(o.getPhiVanChuyen()));
            rowData.add(CurrencyUtil.format(o.getTienGiamGia()));
            rowData.add(df.format(o.getTiLeGiamGia()));
            rowData.add(CurrencyUtil.format(o.getTienThue()));
            rowData.add(df.format(o.getTiLeThue()));
            rowData.add(CurrencyUtil.format(o.getTienHang()));
            rowData.add(o.getNote() != null ? o.getNote() : "");

            masterModel.addRow(rowData);

            if (selectedOrder != null && selectedOrder.getId() != null && selectedOrder.getId().equals(o.getId())) {
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

    private void onOrderSelected(DatHangNCC order) {
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
        lblAuditNguoiTao.setText("Khởi tạo bởi: " + (order.getUserCreatedName() != null ? order.getUserCreatedName() : "Administrator"));

        if (order.getTimeModified() != null) {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: " + sdfDateTime.format(order.getTimeModified()));
            lblAuditNguoiSua.setText("Sửa đổi bởi: " + (order.getUserModifiedName() != null ? order.getUserModifiedName() : "--"));
        } else {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
        }

        List<DatHangNCCChiTiet> details = order.getChiTiet();
        if (details == null || details.isEmpty()) {
            details = datHangNCCDAO.getChiTiet(order.getId());
            order.setChiTiet(details);
        }

        chiTietBottomModel.setRowCount(0);
        for (int i = 0; i < details.size(); i++) {
            DatHangNCCChiTiet ct = details.get(i);
            chiTietBottomModel.addRow(new Object[]{
                    (i + 1),
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getNote() != null ? ct.getNote() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    df.format(ct.getTiLeGiamGia()),
                    CurrencyUtil.format(ct.getDonGia()),
                    CurrencyUtil.format(ct.getThanhTien()),
                    df.format(ct.getSoLuong())
            });
        }
    }

    private void openAddDialog() {
        DatHangNhaCungCapDialog dlg = new DatHangNhaCungCapDialog(SwingUtilities.getWindowAncestor(this), () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog() {
        if (selectedOrder == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn đặt hàng cần chỉnh sửa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DatHangNCC full = datHangNCCDAO.getById(selectedOrder.getId());
        DatHangNhaCungCapDialog dlg = new DatHangNhaCungCapDialog(SwingUtilities.getWindowAncestor(this), full != null ? full : selectedOrder, () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void deleteSelectedOrder() {
        if (selectedOrder == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn đặt hàng cần xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đơn đặt hàng [" + selectedOrder.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (datHangNCCDAO.delete(selectedOrder.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa đơn đặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa đơn đặt hàng này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printMasterTable() {
        try {
            tableMaster.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("DANH SÁCH ĐẶT HÀNG NHÀ CUNG CẤP"),
                    new java.text.MessageFormat("Trang {0}"));
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSummaryDialog() {
        double totalTienHang = 0;
        double totalGiamGia = 0;
        double totalTongCong = 0;

        for (DatHangNCC o : filteredOrders) {
            totalTienHang += o.getTienHang();
            totalGiamGia += o.getTienGiamGia();
            totalTongCong += o.getTongCong();
        }

        String msg = String.format(
                "TỔNG KẾT ĐẶT HÀNG NHÀ CUNG CẤP (%d đơn hàng):\n\n" +
                "• Tổng tiền hàng: %s đ\n" +
                "• Tổng giảm giá:  %s đ\n" +
                "• Tổng cộng:      %s đ",
                filteredOrders.size(),
                CurrencyUtil.format(totalTienHang),
                CurrencyUtil.format(totalGiamGia),
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
