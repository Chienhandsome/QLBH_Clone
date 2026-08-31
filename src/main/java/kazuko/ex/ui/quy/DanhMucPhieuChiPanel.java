package kazuko.ex.ui.quy;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.LyDoThuChi;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.danhmuc.ChonCotHienThiDialog;
import kazuko.ex.ui.danhmuc.LyDoThuChiDialog;
import kazuko.ex.ui.danhmuc.LyDoThuChiEditDialog;
import kazuko.ex.ui.danhmuc.LyDoThuChiTrashDialog;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DanhMucPhieuChiPanel extends JPanel {

    public static final List<String> ALL_POSSIBLE_COLUMNS = Arrays.asList(
            "Số phiếu",
            "Ngày",
            "Tên đối tượng",
            "Địa chỉ",
            "Lý do thu chi",
            "Diễn giải",
            "Chứng từ gốc",
            "Số tiền",
            "Ghi chú",
            "Chuyển khoản",
            "Đặt hàng",
            "Cửa hàng",
            "Là phiếu chi công nợ",
            "Không thay đổi công nợ",
            "Tài khoản ngân hàng",
            "Thẻ trả trước",
            "Đặt hàng nhà cung cấp"
    );

    public static final List<String> DEFAULT_VISIBLE_COLUMNS = Arrays.asList(
            "Số phiếu",
            "Ngày",
            "Tên đối tượng",
            "Địa chỉ",
            "Lý do thu chi",
            "Diễn giải",
            "Chứng từ gốc",
            "Số tiền",
            "Ghi chú",
            "Chuyển khoản",
            "Đặt hàng",
            "Cửa hàng",
            "Là phiếu chi công nợ",
            "Không thay đổi công nợ",
            "Tài khoản ngân hàng",
            "Thẻ trả trước",
            "Đặt hàng nhà cung cấp"
    );

    private List<String> visibleColumns = new ArrayList<>(DEFAULT_VISIBLE_COLUMNS);

    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final LyDoThuChiDAO lyDoThuChiDAO = new LyDoThuChiDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    // Khung Trái: Bộ lọc & Cây Lý do thu chi
    private JCheckBox chkLocNgay;
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<Object> cbCuaHangFilter;
    private JTree treeLyDoThuChi;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;
    private LyDoThuChi currentSelectedReason = null;
    private String currentSelectedTreeCategory = "Tất cả";

    // Khung Phải: Toolbar & Bảng Phiếu chi
    private JTextField txtSearch;
    private JButton btnAddPhieuChi;
    private JButton btnEditPhieuChi;
    private JButton btnDeletePhieuChi;
    private JButton btnImportExcel;
    private JButton btnExportExcel;
    private JButton btnPrint;
    private JButton btnTotal;
    private JButton btnAnalytics;

    private JTable tablePhieuChi;
    private DefaultTableModel tableModel;
    private List<PhieuChi> currentDisplayedList = new ArrayList<>();
    private PhieuChi currentSelectedPhieuChi = null;

    // Khung Dưới: Tabs Thông tin & Chi tiết thanh toán
    private JTabbedPane bottomTabbedPane;
    private JLabel lblCreatedTime;
    private JLabel lblCreatedUser;
    private JLabel lblModifiedTime;
    private JLabel lblModifiedUser;

    private JLabel lblPaymentMethod;
    private JLabel lblPaymentAccount;
    private JLabel lblPaymentAmount;
    private JLabel lblPaymentStore;
    private JLabel lblPaymentCustomer;
    private JLabel lblPaymentNoDebtChange;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm a");

    public DanhMucPhieuChiPanel() {
        initComponents();
        loadStoreFilterData();
        loadTreeData();
        loadPaymentsTable();
        setupShortcuts();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 252));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(260);
        mainSplit.setDividerSize(5);

        // ==================== KHUNG TRÁI: LÝ DO THU CHI ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 0, new Color(185, 205, 230)));

        // Header Trái: "Lý do thu chi" + Icon Cài đặt bánh răng
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(225, 235, 248));
        leftHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        leftHeader.setPreferredSize(new Dimension(260, 28));

        JLabel lblLeftTitle = new JLabel(" Lý do thu chi");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(25, 55, 105));
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);

        JButton btnSetting = new JButton(IconHelper.getIcon("Settings.png", 16, 16));
        btnSetting.setToolTipText("Quản lý danh mục lý do thu chi");
        btnSetting.setBorderPainted(false);
        btnSetting.setContentAreaFilled(false);
        btnSetting.setFocusPainted(false);
        btnSetting.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSetting.addActionListener(e -> {
            LyDoThuChiDialog dlg = new LyDoThuChiDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            loadTreeData();
            loadPaymentsTable();
        });
        leftHeader.add(btnSetting, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Filter Box Trái: Ngày + Cửa hàng + Mini Tool bar
        JPanel leftFilterBox = new JPanel();
        leftFilterBox.setLayout(new BoxLayout(leftFilterBox, BoxLayout.Y_AXIS));
        leftFilterBox.setBackground(new Color(242, 247, 253));
        leftFilterBox.setBorder(new EmptyBorder(6, 6, 6, 6));

        // Row 1: Checkbox Ngày + Từ ngày
        JPanel rowNgay1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rowNgay1.setOpaque(false);
        chkLocNgay = new JCheckBox("Ngày");
        chkLocNgay.setSelected(true);
        chkLocNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkLocNgay.setOpaque(false);
        chkLocNgay.addActionListener(e -> onFilterSearch());

        JLabel lblTu = new JLabel("Từ");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        dpkTuNgay = new DatePickerField();
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        dpkTuNgay.setDate(calStart.getTime());
        dpkTuNgay.setPreferredSize(new Dimension(105, 22));
        dpkTuNgay.addPropertyChangeListener("date", evt -> onFilterSearch());

        rowNgay1.add(chkLocNgay);
        rowNgay1.add(lblTu);
        rowNgay1.add(dpkTuNgay);
        leftFilterBox.add(rowNgay1);

        // Row 2: Đến ngày
        JPanel rowNgay2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rowNgay2.setOpaque(false);
        JLabel lblDen = new JLabel("       Đến");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(new Date());
        dpkDenNgay.setPreferredSize(new Dimension(105, 22));
        dpkDenNgay.addPropertyChangeListener("date", evt -> onFilterSearch());

        rowNgay2.add(lblDen);
        rowNgay2.add(dpkDenNgay);
        leftFilterBox.add(rowNgay2);

        // Row 3: Cửa hàng
        JPanel rowStore = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        rowStore.setOpaque(false);
        JLabel lblStore = new JLabel("Cửa hàng");
        lblStore.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cbCuaHangFilter = new JComboBox<>();
        cbCuaHangFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCuaHangFilter.setPreferredSize(new Dimension(160, 22));
        cbCuaHangFilter.addActionListener(e -> onFilterSearch());

        rowStore.add(lblStore);
        rowStore.add(cbCuaHangFilter);
        leftFilterBox.add(rowStore);

        // Row 4: Mini Tool bar: +, Sửa, Xóa, Làm mới, Thùng rác
        JPanel rowMiniBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 4));
        rowMiniBar.setOpaque(false);

        JButton btnThemReason = createMiniToolButton("plus.png", "Thêm mới lý do thu chi");
        JButton btnSuaReason = createMiniToolButton("pencil.png", "Sửa lý do thu chi");
        JButton btnXoaReason = createMiniToolButton("cross.png", "Chuyển lý do thu chi vào thùng rác");
        JButton btnRefreshTree = createMiniToolButton("Refresh.png", "Tải lại danh mục lý do thu chi");
        JButton btnTrashReason = createMiniToolButton("RecycleBin.png", "Mở thùng rác lý do thu chi");

        btnThemReason.addActionListener(e -> onAddNewReason());
        btnSuaReason.addActionListener(e -> onEditSelectedReason());
        btnXoaReason.addActionListener(e -> onDeleteSelectedReason());
        btnRefreshTree.addActionListener(e -> loadTreeData());
        btnTrashReason.addActionListener(e -> {
            LyDoThuChiTrashDialog dlg = new LyDoThuChiTrashDialog(SwingUtilities.getWindowAncestor(this), this::loadTreeData);
            dlg.setVisible(true);
        });

        rowMiniBar.add(btnThemReason);
        rowMiniBar.add(btnSuaReason);
        rowMiniBar.add(btnXoaReason);
        rowMiniBar.add(btnRefreshTree);
        rowMiniBar.add(btnTrashReason);
        leftFilterBox.add(rowMiniBar);

        // Center Trái: Cây danh mục Lý do thu chi
        rootTreeNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeLyDoThuChi = new JTree(treeModel);
        treeLyDoThuChi.setRootVisible(false);
        treeLyDoThuChi.setShowsRootHandles(true);
        treeLyDoThuChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeLyDoThuChi.setRowHeight(22);
        treeLyDoThuChi.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if ("Tất cả".equals(userObj)) {
                        label.setText("Tất cả");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        label.setForeground(new Color(25, 55, 105));
                    } else if ("Chưa thiết lập".equals(userObj)) {
                        label.setText("Chưa thiết lập");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        label.setForeground(Color.DARK_GRAY);
                    } else if ("Thùng rác".equals(userObj)) {
                        label.setText("Thùng rác");
                        label.setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        label.setForeground(new Color(30, 60, 110));
                    } else if (userObj instanceof LyDoThuChi) {
                        LyDoThuChi tc = (LyDoThuChi) userObj;
                        label.setText(tc.getName());
                        String iconName = tc.getAvatarIcon();
                        if (iconName == null || iconName.trim().isEmpty()) {
                            iconName = tc.isLaLyDoChi() ? "congno.png" : "1282529873_objects.png";
                        }
                        label.setIcon(IconHelper.getIcon(iconName, 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }
                return label;
            }
        });

        treeLyDoThuChi.addTreeSelectionListener(e -> {
            TreePath path = treeLyDoThuChi.getSelectionPath();
            if (path == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObj = node.getUserObject();

            if ("Tất cả".equals(userObj)) {
                currentSelectedTreeCategory = "Tất cả";
                currentSelectedReason = null;
            } else if ("Chưa thiết lập".equals(userObj)) {
                currentSelectedTreeCategory = "Chưa thiết lập";
                currentSelectedReason = null;
            } else if ("Thùng rác".equals(userObj)) {
                currentSelectedTreeCategory = "Thùng rác";
                currentSelectedReason = null;
            } else if (userObj instanceof LyDoThuChi) {
                currentSelectedTreeCategory = "LyDo";
                currentSelectedReason = (LyDoThuChi) userObj;
            }
            loadPaymentsTable();
        });

        treeLyDoThuChi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    if (currentSelectedReason != null) {
                        onEditSelectedReason();
                    }
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(treeLyDoThuChi);
        treeScroll.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 225, 240)));

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.add(leftFilterBox, BorderLayout.NORTH);
        leftContainer.add(treeScroll, BorderLayout.CENTER);
        leftPanel.add(leftContainer, BorderLayout.CENTER);

        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: PHIẾU CHI ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Top Bar: Search + Action Buttons
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        topBar.setBackground(new Color(225, 238, 252));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JLabel lblSearch = new JLabel("Lọc (F3):");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSearch.setForeground(new Color(20, 40, 80));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(150, 24));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onFilterSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onFilterSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onFilterSearch(); }
        });

        btnAddPhieuChi = createTopBarButton("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 14, 14));
        btnEditPhieuChi = createTopBarButton("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 14, 14));
        btnDeletePhieuChi = createTopBarButton("Xóa (Del)", IconHelper.getIcon("cross.png", 14, 14));
        btnImportExcel = createTopBarButton("Thêm excel", IconHelper.getIcon("document-excel-table.png", 14, 14));
        btnExportExcel = createTopBarButton("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        btnPrint = createTopBarButton("In", IconHelper.getIcon("printer.png", 14, 14));
        btnTotal = createTopBarButton("Tổng", IconHelper.getIcon("sum.png", 14, 14));
        btnAnalytics = createTopBarButton("P.tích", IconHelper.getIcon("chart.png", 14, 14));

        btnAddPhieuChi.addActionListener(e -> onAddNewPhieuChi());
        btnEditPhieuChi.addActionListener(e -> onEditSelectedPhieuChi());
        btnDeletePhieuChi.addActionListener(e -> onDeleteSelectedPhieuChi());
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Thêm từ Excel đang được hỗ trợ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnExportExcel.addActionListener(e -> onExportExcel());
        btnPrint.addActionListener(e -> onPrint());
        btnTotal.addActionListener(e -> showTotalSummary());
        btnAnalytics.addActionListener(e -> showAnalyticsSummary());

        topBar.add(lblSearch);
        topBar.add(txtSearch);
        topBar.add(btnAddPhieuChi);
        topBar.add(btnEditPhieuChi);
        topBar.add(btnDeletePhieuChi);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnTotal);
        topBar.add(btnAnalytics);

        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Phiếu chi
        List<String> colsWithStt = new ArrayList<>();
        colsWithStt.add("");
        colsWithStt.addAll(visibleColumns);

        tableModel = new DefaultTableModel(colsWithStt.toArray(new String[0]), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePhieuChi = new JTable(tableModel);
        tablePhieuChi.setRowHeight(22);
        tablePhieuChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePhieuChi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePhieuChi.setShowGrid(true);
        tablePhieuChi.setGridColor(new Color(225, 232, 240));
        tablePhieuChi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setupColumnWidths();

        // Table Header Styling & Quick Menu
        JTableHeader header = tablePhieuChi.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(230, 240, 252));
        header.setForeground(new Color(30, 60, 110));
        header.setReorderingAllowed(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 26));

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTableHeaderPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTableHeaderPopup(e);
            }
        });

        // Custom Cell Renderer
        tablePhieuChi.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String colName = table.getColumnName(column);
                if (column == 0) {
                    // Cột STT kèm con trỏ ▶
                    JLabel lbl = (JLabel) c;
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    if (isSelected) {
                        lbl.setText("▶ " + (row + 1));
                        lbl.setBackground(new Color(220, 235, 255));
                        lbl.setForeground(new Color(20, 50, 120));
                    } else {
                        lbl.setText(String.valueOf(row + 1));
                        lbl.setBackground(new Color(245, 248, 252));
                        lbl.setForeground(Color.DARK_GRAY);
                    }
                    return lbl;
                }

                if ("Số phiếu".equals(colName) && isSelected) {
                    // Cột Số phiếu khi được chọn có màu cam đất đặc trưng
                    c.setBackground(new Color(188, 104, 52));
                    c.setForeground(Color.WHITE);
                    return c;
                }

                if ("Số tiền".equals(colName)) {
                    // Cột Số tiền căn phải
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else if ("Ngày".equals(colName)) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (isSelected) {
                    c.setBackground(new Color(220, 235, 255));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 254));
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        tablePhieuChi.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablePhieuChi.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < currentDisplayedList.size()) {
                    currentSelectedPhieuChi = currentDisplayedList.get(selectedRow);
                    updateBottomDetailPanels(currentSelectedPhieuChi);
                } else {
                    currentSelectedPhieuChi = null;
                    clearBottomDetailPanels();
                }
            }
        });

        tablePhieuChi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelectedPhieuChi();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTableBodyPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTableBodyPopup(e);
            }
        });

        JScrollPane tableScroll = new JScrollPane(tablePhieuChi);
        tableScroll.setBorder(null);

        // 3. Khung Dưới: Tabs Thông tin & Chi tiết thanh toán
        JPanel bottomContainer = createBottomInfoContainer();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottomContainer);
        rightSplit.setDividerLocation(380);
        rightSplit.setDividerSize(5);
        rightSplit.setResizeWeight(0.75);

        rightPanel.add(rightSplit, BorderLayout.CENTER);
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createBottomInfoContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setPreferredSize(new Dimension(800, 160));

        bottomTabbedPane = new JTabbedPane();
        bottomTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Thông tin
        JPanel tabThongTin = new JPanel(new GridLayout(4, 1, 4, 4));
        tabThongTin.setBackground(Color.WHITE);
        tabThongTin.setBorder(new EmptyBorder(8, 15, 8, 15));

        lblCreatedTime = createDetailLabel("Khởi tạo: ");
        lblCreatedUser = createDetailLabel("Khởi tạo bởi: ");
        lblModifiedTime = createDetailLabel("Sửa đổi gần nhất: ");
        lblModifiedUser = createDetailLabel("Sửa đổi bởi: ");

        tabThongTin.add(lblCreatedTime);
        tabThongTin.add(lblCreatedUser);
        tabThongTin.add(lblModifiedTime);
        tabThongTin.add(lblModifiedUser);

        bottomTabbedPane.addTab("Thông tin", tabThongTin);

        // Tab 2: Chi tiết thanh toán
        JPanel tabChiTiet = new JPanel(new GridLayout(3, 2, 8, 4));
        tabChiTiet.setBackground(Color.WHITE);
        tabChiTiet.setBorder(new EmptyBorder(8, 15, 8, 15));

        lblPaymentMethod = createDetailLabel("Hình thức: ");
        lblPaymentAccount = createDetailLabel("Tài khoản chuyển: ");
        lblPaymentAmount = createDetailLabel("Số tiền: ");
        lblPaymentStore = createDetailLabel("Cửa hàng: ");
        lblPaymentCustomer = createDetailLabel("Đối tượng: ");
        lblPaymentNoDebtChange = createDetailLabel("Thay đổi công nợ: ");

        tabChiTiet.add(lblPaymentMethod);
        tabChiTiet.add(lblPaymentAccount);
        tabChiTiet.add(lblPaymentAmount);
        tabChiTiet.add(lblPaymentStore);
        tabChiTiet.add(lblPaymentCustomer);
        tabChiTiet.add(lblPaymentNoDebtChange);

        bottomTabbedPane.addTab("Chi tiết thanh toán", tabChiTiet);

        container.add(bottomTabbedPane, BorderLayout.CENTER);
        return container;
    }

    private JLabel createDetailLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(40, 40, 40));
        return lbl;
    }

    private JButton createMiniToolButton(String iconName, String toolTip) {
        JButton btn = new JButton(IconHelper.getIcon(iconName, 14, 14));
        btn.setToolTipText(toolTip);
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(190, 210, 235), 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createTopBarButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadStoreFilterData() {
        cbCuaHangFilter.removeAllItems();
        cbCuaHangFilter.addItem("Tất cả cửa hàng");
        List<CuaHang> stores = cuaHangDAO.getAll();
        for (CuaHang ch : stores) {
            cbCuaHangFilter.addItem(ch);
        }
        cbCuaHangFilter.setSelectedIndex(0);
    }

    public void loadTreeData() {
        rootTreeNode.removeAllChildren();

        DefaultMutableTreeNode nodeTatCa = new DefaultMutableTreeNode("Tất cả");
        rootTreeNode.add(nodeTatCa);

        DefaultMutableTreeNode nodeChuaThietLap = new DefaultMutableTreeNode("Chưa thiết lập");
        rootTreeNode.add(nodeChuaThietLap);

        List<LyDoThuChi> all = lyDoThuChiDAO.getAllActive();
        for (LyDoThuChi tc : all) {
            if (tc.getStatus() >= 0 && tc.isLaLyDoChi()) {
                DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(tc);
                rootTreeNode.add(itemNode);
            }
        }

        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");
        rootTreeNode.add(nodeTrash);

        treeModel.reload();
        treeLyDoThuChi.setSelectionPath(new TreePath(nodeTatCa.getPath()));
    }

    public void loadPaymentsTable() {
        List<PhieuChi> all = "Thùng rác".equals(currentSelectedTreeCategory) ?
                phieuChiDAO.getTrashItems() : phieuChiDAO.getAll();

        Date tuNgay = (chkLocNgay.isSelected() && dpkTuNgay.getDate() != null) ? getStartOfDay(dpkTuNgay.getDate()) : null;
        Date denNgay = (chkLocNgay.isSelected() && dpkDenNgay.getDate() != null) ? getEndOfDay(dpkDenNgay.getDate()) : null;

        Object selectedStoreObj = cbCuaHangFilter.getSelectedItem();
        String selectedStoreId = (selectedStoreObj instanceof CuaHang) ? ((CuaHang) selectedStoreObj).getId() : null;

        List<PhieuChi> filtered = all.stream().filter(pc -> {
            // Lọc ngày
            if (tuNgay != null && pc.getNgay() != null && pc.getNgay().before(tuNgay)) return false;
            if (denNgay != null && pc.getNgay() != null && pc.getNgay().after(denNgay)) return false;

            // Lọc cửa hàng
            if (selectedStoreId != null && pc.getCuaHangId() != null && !selectedStoreId.equals(pc.getCuaHangId())) {
                return false;
            }

            // Lọc theo cây Lý do thu chi
            if ("LyDo".equals(currentSelectedTreeCategory) && currentSelectedReason != null) {
                if (pc.getPhanLoaiId() != null && !pc.getPhanLoaiId().equals(currentSelectedReason.getId())) {
                    if (pc.getTenPhanLoai() == null || !pc.getTenPhanLoai().equalsIgnoreCase(currentSelectedReason.getName())) {
                        return false;
                    }
                } else if (pc.getPhanLoaiId() == null) {
                    if (pc.getTenPhanLoai() == null || !pc.getTenPhanLoai().equalsIgnoreCase(currentSelectedReason.getName())) {
                        return false;
                    }
                }
            } else if ("Chưa thiết lập".equals(currentSelectedTreeCategory)) {
                if (pc.getPhanLoaiId() != null && !pc.getPhanLoaiId().trim().isEmpty()) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());

        // Lọc theo từ khóa tìm kiếm nhanh txtSearch
        String searchKey = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        if (!searchKey.isEmpty()) {
            filtered = filtered.stream().filter(pc -> {
                String sp = pc.getSoPhieu() != null ? pc.getSoPhieu().toLowerCase() : "";
                String ten = pc.getTenDoiTuong() != null ? pc.getTenDoiTuong().toLowerCase() : "";
                String dc = pc.getDiaChi() != null ? pc.getDiaChi().toLowerCase() : "";
                String pl = pc.getTenPhanLoai() != null ? pc.getTenPhanLoai().toLowerCase() : "";
                String ld = pc.getLyDoChi() != null ? pc.getLyDoChi().toLowerCase() : "";
                String note = pc.getGhiChu() != null ? pc.getGhiChu().toLowerCase() : "";
                String ctg = pc.getChungTuGoc() != null ? pc.getChungTuGoc().toLowerCase() : "";
                return sp.contains(searchKey) || ten.contains(searchKey) || dc.contains(searchKey)
                        || pl.contains(searchKey) || ld.contains(searchKey) || note.contains(searchKey) || ctg.contains(searchKey);
            }).collect(Collectors.toList());
        }

        currentDisplayedList = filtered;

        // Render table
        tableModel.setRowCount(0);
        for (PhieuChi pc : currentDisplayedList) {
            List<Object> rowData = new ArrayList<>();
            rowData.add(""); // STT
            for (String col : visibleColumns) {
                switch (col) {
                    case "Số phiếu":
                        rowData.add(pc.getSoPhieu() != null ? pc.getSoPhieu() : "");
                        break;
                    case "Ngày":
                        rowData.add(pc.getNgay() != null ? sdfDate.format(pc.getNgay()) : "");
                        break;
                    case "Tên đối tượng":
                        rowData.add(pc.getTenDoiTuong() != null ? pc.getTenDoiTuong() : "");
                        break;
                    case "Địa chỉ":
                        rowData.add(pc.getDiaChi() != null ? pc.getDiaChi() : "");
                        break;
                    case "Lý do thu chi":
                        rowData.add(pc.getTenPhanLoai() != null ? pc.getTenPhanLoai() : "");
                        break;
                    case "Diễn giải":
                        rowData.add(pc.getLyDoChi() != null ? pc.getLyDoChi() : "");
                        break;
                    case "Chứng từ gốc":
                        rowData.add(pc.getChungTuGoc() != null ? pc.getChungTuGoc() : "");
                        break;
                    case "Số tiền":
                        rowData.add(CurrencyUtil.format(pc.getSoTien()));
                        break;
                    case "Ghi chú":
                        rowData.add(pc.getGhiChu() != null ? pc.getGhiChu() : "");
                        break;
                    case "Chuyển khoản":
                        rowData.add(pc.isChuyenTuTaiKhoan() ? (pc.getTenTaiKhoanNganHang() != null ? pc.getTenTaiKhoanNganHang() : "Có") : "Tiền mặt");
                        break;
                    case "Đặt hàng":
                        rowData.add("");
                        break;
                    case "Cửa hàng":
                        rowData.add(pc.getTenCuaHang() != null ? pc.getTenCuaHang() : "");
                        break;
                    case "Là phiếu chi công nợ":
                        rowData.add("Chi công nợ".equalsIgnoreCase(pc.getTenPhanLoai()) || "Thanh toán công nợ NCC".equalsIgnoreCase(pc.getTenPhanLoai()) ? "Có" : "Không");
                        break;
                    case "Không thay đổi công nợ":
                        rowData.add(pc.isKhongThayDoiCongNo() ? "Không" : "Có");
                        break;
                    case "Tài khoản ngân hàng":
                        rowData.add(pc.getTenTaiKhoanNganHang() != null ? pc.getTenTaiKhoanNganHang() : "");
                        break;
                    case "Thẻ trả trước":
                        rowData.add("");
                        break;
                    case "Đặt hàng nhà cung cấp":
                        rowData.add(pc.getTenNhaCungCap() != null ? pc.getTenNhaCungCap() : "");
                        break;
                    default:
                        rowData.add("");
                        break;
                }
            }
            tableModel.addRow(rowData.toArray());
        }

        if (!currentDisplayedList.isEmpty()) {
            tablePhieuChi.setRowSelectionInterval(0, 0);
        } else {
            clearBottomDetailPanels();
        }
    }

    private void setupColumnWidths() {
        for (int i = 0; i < tablePhieuChi.getColumnModel().getColumnCount(); i++) {
            TableColumn tc = tablePhieuChi.getColumnModel().getColumn(i);
            String colName = tablePhieuChi.getColumnName(i);
            int w = 110;
            switch (colName) {
                case "":
                    w = 35; break;
                case "Số phiếu":
                    w = 110; break;
                case "Ngày":
                    w = 85; break;
                case "Tên đối tượng":
                    w = 150; break;
                case "Địa chỉ":
                    w = 140; break;
                case "Lý do thu chi":
                    w = 130; break;
                case "Diễn giải":
                    w = 140; break;
                case "Chứng từ gốc":
                    w = 90; break;
                case "Số tiền":
                    w = 100; break;
                case "Ghi chú":
                    w = 100; break;
                case "Chuyển khoản":
                    w = 100; break;
                case "Đặt hàng":
                    w = 100; break;
                case "Cửa hàng":
                    w = 120; break;
                case "Là phiếu chi công nợ":
                    w = 130; break;
                case "Không thay đổi công nợ":
                    w = 130; break;
                case "Tài khoản ngân hàng":
                    w = 140; break;
                case "Thẻ trả trước":
                    w = 100; break;
                case "Đặt hàng nhà cung cấp":
                    w = 140; break;
                default:
                    w = 100; break;
            }
            tc.setPreferredWidth(w);
        }
    }

    private void autoResizeColumnWidths() {
        for (int column = 0; column < tablePhieuChi.getColumnCount(); column++) {
            TableColumn tableColumn = tablePhieuChi.getColumnModel().getColumn(column);
            int preferredWidth = 40;

            // Header width
            TableCellRenderer headerRenderer = tablePhieuChi.getTableHeader().getDefaultRenderer();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        tablePhieuChi, tableColumn.getHeaderValue(), false, false, 0, column);
                if (headerComp != null) {
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
                }
            }

            // Sample rows width
            int rowCount = Math.min(tablePhieuChi.getRowCount(), 100);
            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer cellRenderer = tablePhieuChi.getCellRenderer(row, column);
                if (cellRenderer != null) {
                    Component c = tablePhieuChi.prepareRenderer(cellRenderer, row, column);
                    if (c != null) {
                        int width = c.getPreferredSize().width + tablePhieuChi.getIntercellSpacing().width + 16;
                        preferredWidth = Math.max(preferredWidth, width);
                    }
                }
            }

            tableColumn.setPreferredWidth(Math.min(preferredWidth, 400));
        }
        tablePhieuChi.getTableHeader().repaint();
    }

    private void showTableHeaderPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm Phiếu chi", IconHelper.getIcon("plus.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNewPhieuChi());
        popup.add(miAdd);

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa Phiếu chi", IconHelper.getIcon("pencil.png", 16, 16));
        miEdit.addActionListener(ev -> onEditSelectedPhieuChi());
        popup.add(miEdit);

        JMenuItem miDel = new JMenuItem("Xóa Phiếu chi", IconHelper.getIcon("cross.png", 16, 16));
        miDel.addActionListener(ev -> onDeleteSelectedPhieuChi());
        popup.add(miDel);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miChooseCols = new JMenuItem("Chọn cột hiển thị", IconHelper.getIcon("edit-column.png", 16, 16));
        if (miChooseCols.getIcon() == null) miChooseCols.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
        miChooseCols.addActionListener(ev -> onOpenColumnChooser());
        popup.add(miChooseCols);

        popup.show(tablePhieuChi.getTableHeader(), e.getX(), e.getY());
    }

    private void showTableBodyPopup(MouseEvent e) {
        int row = tablePhieuChi.rowAtPoint(e.getPoint());
        if (row >= 0 && row < tablePhieuChi.getRowCount()) {
            tablePhieuChi.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNewPhieuChi());
        popup.add(miAdd);

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        miEdit.addActionListener(ev -> onEditSelectedPhieuChi());
        popup.add(miEdit);

        JMenuItem miDel = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 16, 16));
        miDel.addActionListener(ev -> onDeleteSelectedPhieuChi());
        popup.add(miDel);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miChooseCols = new JMenuItem("Chọn cột hiển thị", IconHelper.getIcon("edit-column.png", 16, 16));
        if (miChooseCols.getIcon() == null) miChooseCols.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
        miChooseCols.addActionListener(ev -> onOpenColumnChooser());
        popup.add(miChooseCols);

        popup.addSeparator();

        JMenuItem miRefresh = new JMenuItem("Làm mới (F5)", IconHelper.getIcon("Refresh.png", 16, 16));
        miRefresh.addActionListener(ev -> {
            loadTreeData();
            loadPaymentsTable();
        });
        popup.add(miRefresh);

        popup.show(tablePhieuChi, e.getX(), e.getY());
    }

    private void onOpenColumnChooser() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChonCotHienThiDialog dialog = new ChonCotHienThiDialog(
                owner,
                visibleColumns,
                ALL_POSSIBLE_COLUMNS,
                DEFAULT_VISIBLE_COLUMNS,
                this::rebuildTableColumns
        );
        dialog.setVisible(true);
    }

    private void rebuildTableColumns(List<String> newVisibleColumns) {
        this.visibleColumns = new ArrayList<>(newVisibleColumns);
        List<String> colsWithStt = new ArrayList<>();
        colsWithStt.add("");
        colsWithStt.addAll(visibleColumns);

        tableModel.setColumnIdentifiers(colsWithStt.toArray(new String[0]));
        setupColumnWidths();
        loadPaymentsTable();
    }

    private void onFilterSearch() {
        loadPaymentsTable();
    }

    private void updateBottomDetailPanels(PhieuChi pc) {
        if (pc == null) {
            clearBottomDetailPanels();
            return;
        }

        // Tab Thông tin
        String timeCreatedStr = pc.getTimeCreated() != null ? sdfDateTime.format(pc.getTimeCreated()) :
                (pc.getNgay() != null ? sdfDateTime.format(pc.getNgay()) : "");
        lblCreatedTime.setText("Khởi tạo: " + timeCreatedStr);
        lblCreatedUser.setText("Khởi tạo bởi: " + (pc.getUserCreatedId() != null ? pc.getUserCreatedId() : "Administrator"));
        lblModifiedTime.setText("Sửa đổi gần nhất: ");
        lblModifiedUser.setText("Sửa đổi bởi: ");

        // Tab Chi tiết thanh toán
        lblPaymentMethod.setText("Hình thức: " + (pc.isChuyenTuTaiKhoan() ? "Chuyển khoản" : "Tiền mặt"));
        lblPaymentAccount.setText("Tài khoản chuyển: " + (pc.getTenTaiKhoanNganHang() != null ? pc.getTenTaiKhoanNganHang() : "--"));
        lblPaymentAmount.setText("Số tiền: " + CurrencyUtil.format(pc.getSoTien()) + " đ");
        lblPaymentStore.setText("Cửa hàng: " + (pc.getTenCuaHang() != null ? pc.getTenCuaHang() : "--"));
        lblPaymentCustomer.setText("Đối tượng: [" + pc.getLoaiDoiTuong() + "] " + (pc.getTenDoiTuong() != null ? pc.getTenDoiTuong() : ""));
        lblPaymentNoDebtChange.setText("Thay đổi công nợ: " + (pc.isKhongThayDoiCongNo() ? "Không thay đổi" : "Có tính vào công nợ"));
    }

    private void clearBottomDetailPanels() {
        lblCreatedTime.setText("Khởi tạo: ");
        lblCreatedUser.setText("Khởi tạo bởi: ");
        lblModifiedTime.setText("Sửa đổi gần nhất: ");
        lblModifiedUser.setText("Sửa đổi bởi: ");

        lblPaymentMethod.setText("Hình thức: ");
        lblPaymentAccount.setText("Tài khoản chuyển: ");
        lblPaymentAmount.setText("Số tiền: ");
        lblPaymentStore.setText("Cửa hàng: ");
        lblPaymentCustomer.setText("Đối tượng: ");
        lblPaymentNoDebtChange.setText("Thay đổi công nợ: ");
    }

    // ==================== ACTIONS ====================

    private void onAddNewReason() {
        LyDoThuChiEditDialog dlg = new LyDoThuChiEditDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                lyDoThuChiDAO.getAllActive(),
                this::loadTreeData
        );
        dlg.setVisible(true);
    }

    private void onEditSelectedReason() {
        if (currentSelectedReason == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lý do thu chi trên cây để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LyDoThuChiEditDialog dlg = new LyDoThuChiEditDialog(
                SwingUtilities.getWindowAncestor(this),
                currentSelectedReason,
                lyDoThuChiDAO.getAllActive(),
                this::loadTreeData
        );
        dlg.setVisible(true);
    }

    private void onDeleteSelectedReason() {
        if (currentSelectedReason == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lý do thu chi trên cây để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn chuyển lý do [" + currentSelectedReason.getName() + "] vào thùng rác không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            lyDoThuChiDAO.moveToTrash(currentSelectedReason.getId());
            currentSelectedReason = null;
            loadTreeData();
            loadPaymentsTable();
        }
    }

    private void onAddNewPhieuChi() {
        LyDoThuChi preSelected = currentSelectedReason;
        TaoPhieuChiDialog dlg = new TaoPhieuChiDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                preSelected,
                this::loadPaymentsTable
        );
        dlg.setVisible(true);
    }

    private void onEditSelectedPhieuChi() {
        if (currentSelectedPhieuChi == null) {
            int row = tablePhieuChi.getSelectedRow();
            if (row >= 0 && row < currentDisplayedList.size()) {
                currentSelectedPhieuChi = currentDisplayedList.get(row);
            }
        }
        if (currentSelectedPhieuChi == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu chi trên bảng để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaoPhieuChiDialog dlg = new TaoPhieuChiDialog(
                SwingUtilities.getWindowAncestor(this),
                currentSelectedPhieuChi,
                this::loadPaymentsTable
        );
        dlg.setVisible(true);
    }

    private void onDeleteSelectedPhieuChi() {
        if (currentSelectedPhieuChi == null) {
            int row = tablePhieuChi.getSelectedRow();
            if (row >= 0 && row < currentDisplayedList.size()) {
                currentSelectedPhieuChi = currentDisplayedList.get(row);
            }
        }
        if (currentSelectedPhieuChi == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu chi trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phiếu chi [" + currentSelectedPhieuChi.getSoPhieu() + "] không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            phieuChiDAO.delete(currentSelectedPhieuChi.getId());
            loadPaymentsTable();
            JOptionPane.showMessageDialog(this, "Đã xóa phiếu chi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onExportExcel() {
        ExcelExporter.exportTable(tablePhieuChi, "Danh_Muc_Phieu_Chi", "Phiếu chi", this);
    }

    private void onPrint() {
        if (tablePhieuChi.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu phiếu chi để in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean complete = tablePhieuChi.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "In danh sách phiếu chi hoàn tất!", "In ấn", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTotalSummary() {
        double totalMoney = currentDisplayedList.stream().mapToDouble(PhieuChi::getSoTien).sum();
        int totalCount = currentDisplayedList.size();
        JOptionPane.showMessageDialog(
                this,
                "<html><b>TỔNG KẾT PHIẾU CHI:</b><br>" +
                        "- Tổng số phiếu: <b>" + totalCount + "</b><br>" +
                        "- Tổng số tiền chi: <b style='color:red; font-size:13px;'>" + CurrencyUtil.format(totalMoney) + " đ</b></html>",
                "Tổng kết phiếu chi",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAnalyticsSummary() {
        Map<String, Double> byReason = new HashMap<>();
        for (PhieuChi pc : currentDisplayedList) {
            String name = (pc.getTenPhanLoai() != null && !pc.getTenPhanLoai().trim().isEmpty()) ? pc.getTenPhanLoai() : "Khác";
            byReason.put(name, byReason.getOrDefault(name, 0.0) + pc.getSoTien());
        }

        StringBuilder sb = new StringBuilder("<html><b>PHÂN TÍCH CHI THEO LÝ DO:</b><br><br>");
        for (Map.Entry<String, Double> entry : byReason.entrySet()) {
            sb.append("• <b>").append(entry.getKey()).append("</b>: ")
              .append(CurrencyUtil.format(entry.getValue())).append(" đ<br>");
        }
        sb.append("</html>");

        JOptionPane.showMessageDialog(this, sb.toString(), "Phân tích phiếu chi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupShortcuts() {
        // F3: Lọc
        registerKeyboardAction(e -> {
            txtSearch.requestFocusInWindow();
            txtSearch.selectAll();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Insert: Thêm mới
        registerKeyboardAction(e -> onAddNewPhieuChi(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F4: Chỉnh sửa
        registerKeyboardAction(e -> onEditSelectedPhieuChi(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Delete: Xóa
        registerKeyboardAction(e -> onDeleteSelectedPhieuChi(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F5: Tải lại
        registerKeyboardAction(e -> {
            loadTreeData();
            loadPaymentsTable();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
