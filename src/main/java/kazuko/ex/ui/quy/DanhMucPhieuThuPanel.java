package kazuko.ex.ui.quy;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.LyDoThuChi;
import kazuko.ex.model.quy.PhieuThu;
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

public class DanhMucPhieuThuPanel extends JPanel {

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
            "Là phiếu thu công nợ",
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
            "Là phiếu thu công nợ",
            "Không thay đổi công nợ",
            "Tài khoản ngân hàng",
            "Thẻ trả trước",
            "Đặt hàng nhà cung cấp"
    );

    private List<String> visibleColumns = new ArrayList<>(DEFAULT_VISIBLE_COLUMNS);

    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final LyDoThuChiDAO lyDoThuChiDAO = new LyDoThuChiDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    // Khung Trai: Bo loc & Cay Ly do thu chi
    private JCheckBox chkLocNgay;
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<Object> cbCuaHangFilter;
    private JTree treeLyDoThuChi;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;
    private LyDoThuChi currentSelectedReason = null;
    private String currentSelectedTreeCategory = "Tất cả";

    // Khung Phai: Toolbar & Bang Phieu thu
    private JTextField txtSearch;
    private JButton btnAddPhieuThu;
    private JButton btnEditPhieuThu;
    private JButton btnDeletePhieuThu;
    private JButton btnImportExcel;
    private JButton btnExportExcel;
    private JButton btnPrint;
    private JButton btnTotal;
    private JButton btnAnalytics;

    private JTable tablePhieuThu;
    private DefaultTableModel tableModel;
    private List<PhieuThu> currentDisplayedList = new ArrayList<>();
    private PhieuThu currentSelectedPhieuThu = null;

    // Khung Duoi: Tabs Thong tin & Chi tiet thanh toan
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

    public DanhMucPhieuThuPanel() {
        initComponents();
        loadStoreFilterData();
        loadTreeData();
        loadReceiptsTable();
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

        JButton btnGear = createMiniToolButton("wrench-screwdriver.png", "Quản lý danh mục lý do thu chi");
        btnGear.addActionListener(e -> {
            LyDoThuChiDialog dlg = new LyDoThuChiDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            loadTreeData();
        });
        JPanel pnlGear = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        pnlGear.setOpaque(false);
        pnlGear.add(btnGear);
        leftHeader.add(pnlGear, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Khung bộ lọc ngày & cửa hàng trên cây
        JPanel leftFilterBox = new JPanel();
        leftFilterBox.setLayout(new BoxLayout(leftFilterBox, BoxLayout.Y_AXIS));
        leftFilterBox.setBackground(new Color(245, 249, 254));
        leftFilterBox.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(200, 218, 240)),
                new EmptyBorder(4, 6, 4, 6)
        ));

        // Hàng 1: [x] Ngày | Từ: dd/MM/yyyy
        JPanel rowNgayTu = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rowNgayTu.setOpaque(false);
        chkLocNgay = new JCheckBox("Ngày");
        chkLocNgay.setSelected(true);
        chkLocNgay.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkLocNgay.setOpaque(false);
        chkLocNgay.addActionListener(e -> loadReceiptsTable());

        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        dpkTuNgay = new DatePickerField();
        dpkTuNgay.setDate(cal.getTime());
        dpkTuNgay.setPreferredSize(new Dimension(105, 22));
        dpkTuNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadReceiptsTable(); }
            @Override public void removeUpdate(DocumentEvent e) { loadReceiptsTable(); }
            @Override public void changedUpdate(DocumentEvent e) { loadReceiptsTable(); }
        });

        rowNgayTu.add(chkLocNgay);
        rowNgayTu.add(lblTu);
        rowNgayTu.add(dpkTuNgay);
        leftFilterBox.add(rowNgayTu);

        // Hàng 2: Đến: dd/MM/yyyy
        JPanel rowNgayDen = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rowNgayDen.setOpaque(false);
        JLabel lblDen = new JLabel("         Đến:");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        dpkDenNgay = new DatePickerField();
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        dpkDenNgay.setDate(calEnd.getTime());
        dpkDenNgay.setPreferredSize(new Dimension(105, 22));
        dpkDenNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadReceiptsTable(); }
            @Override public void removeUpdate(DocumentEvent e) { loadReceiptsTable(); }
            @Override public void changedUpdate(DocumentEvent e) { loadReceiptsTable(); }
        });

        rowNgayDen.add(lblDen);
        rowNgayDen.add(dpkDenNgay);
        leftFilterBox.add(rowNgayDen);

        // Hàng 3: Cửa hàng Dropdown
        JPanel rowCuaHang = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rowCuaHang.setOpaque(false);
        JLabel lblCuaHang = new JLabel("Cửa hàng:");
        lblCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbCuaHangFilter = new JComboBox<>();
        cbCuaHangFilter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbCuaHangFilter.setPreferredSize(new Dimension(150, 22));
        cbCuaHangFilter.addActionListener(e -> loadReceiptsTable());
        rowCuaHang.add(lblCuaHang);
        rowCuaHang.add(cbCuaHangFilter);
        leftFilterBox.add(rowCuaHang);

        // Hàng 4: Toolbar mini (Thêm +, Sửa bút chì, Xóa x, Tải lại, Thùng rác)
        JPanel rowMiniBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        rowMiniBar.setOpaque(false);

        JButton btnThemReason = createMiniToolButton("plus.png", "Thêm mới lý do thu chi");
        JButton btnSuaReason = createMiniToolButton("pencil.png", "Chỉnh sửa lý do thu chi đang chọn");
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
                            iconName = tc.isLaLyDoThu() ? "1282529873_objects.png" : "congno.png";
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
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object obj = node.getUserObject();
                if ("Tất cả".equals(obj)) {
                    currentSelectedTreeCategory = "Tất cả";
                    currentSelectedReason = null;
                } else if ("Chưa thiết lập".equals(obj)) {
                    currentSelectedTreeCategory = "Chưa thiết lập";
                    currentSelectedReason = null;
                } else if ("Thùng rác".equals(obj)) {
                    currentSelectedTreeCategory = "Thùng rác";
                    currentSelectedReason = null;
                } else if (obj instanceof LyDoThuChi) {
                    currentSelectedTreeCategory = "LyDo";
                    currentSelectedReason = (LyDoThuChi) obj;
                }
                loadReceiptsTable();
            }
        });

        // Double click trên cây để sửa lý do thu chi
        treeLyDoThuChi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    TreePath path = treeLyDoThuChi.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof LyDoThuChi) {
                            onEditSelectedReason();
                        }
                    }
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(treeLyDoThuChi);
        treeScroll.setBorder(null);

        JPanel leftCenter = new JPanel(new BorderLayout());
        leftCenter.add(leftFilterBox, BorderLayout.NORTH);
        leftCenter.add(treeScroll, BorderLayout.CENTER);
        leftPanel.add(leftCenter, BorderLayout.CENTER);

        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: PHIẾU THU ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Toolbar Khung Phải
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        topBar.setBackground(new Color(225, 235, 248));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JLabel lblTag = new JLabel("Phiếu thu");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTag.setForeground(new Color(40, 70, 120));
        lblTag.setBorder(new EmptyBorder(0, 4, 0, 6));
        topBar.add(lblTag);

        JLabel lblLoc = new JLabel("Lọc (F3):");
        lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topBar.add(lblLoc);

        txtSearch = new JTextField(11);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setPreferredSize(new Dimension(130, 24));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onFilterSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { onFilterSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { onFilterSearch(); }
        });
        topBar.add(txtSearch);

        btnAddPhieuThu = createToolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        btnEditPhieuThu = createToolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        btnDeletePhieuThu = createToolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        btnImportExcel = createToolButton("Thêm excel", "page_white_excel.png", Color.BLACK, false);
        btnExportExcel = createToolButton("Xuất excel", "export_excel.png", Color.BLACK, false);
        btnPrint = createToolButton("In", "Preview.png", Color.BLACK, false);
        btnTotal = createToolButton("Tổng", "tsbGenTotal.Image.png", Color.BLACK, false);
        btnAnalytics = createToolButton("P.tích", "Report.png", Color.BLACK, false);

        btnAddPhieuThu.addActionListener(e -> onAddNewPhieuThu());
        btnEditPhieuThu.addActionListener(e -> onEditSelectedPhieuThu());
        btnDeletePhieuThu.addActionListener(e -> onDeleteSelectedPhieuThu());
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Thêm từ Excel đang được hoàn thiện!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnExportExcel.addActionListener(e -> onExportExcel());
        btnPrint.addActionListener(e -> onPrint());
        btnTotal.addActionListener(e -> showTotalSummary());
        btnAnalytics.addActionListener(e -> showAnalyticsSummary());

        topBar.add(btnAddPhieuThu);
        topBar.add(btnEditPhieuThu);
        topBar.add(btnDeletePhieuThu);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnTotal);
        topBar.add(btnAnalytics);

        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Phiếu thu
        List<String> colsWithStt = new ArrayList<>();
        colsWithStt.add("");
        colsWithStt.addAll(visibleColumns);

        tableModel = new DefaultTableModel(colsWithStt.toArray(new String[0]), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePhieuThu = new JTable(tableModel);
        tablePhieuThu.setRowHeight(22);
        tablePhieuThu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePhieuThu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePhieuThu.setShowGrid(true);
        tablePhieuThu.setGridColor(new Color(225, 232, 240));
        tablePhieuThu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setupColumnWidths();

        // Table Header Styling & Quick Menu
        JTableHeader header = tablePhieuThu.getTableHeader();
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
        tablePhieuThu.setDefaultRenderer(Object.class, new TableCellRenderer() {
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

        tablePhieuThu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablePhieuThu.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < currentDisplayedList.size()) {
                    currentSelectedPhieuThu = currentDisplayedList.get(selectedRow);
                    updateBottomDetailPanels(currentSelectedPhieuThu);
                } else {
                    currentSelectedPhieuThu = null;
                    clearBottomDetailPanels();
                }
            }
        });

        tablePhieuThu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelectedPhieuThu();
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

        JScrollPane tableScroll = new JScrollPane(tablePhieuThu);
        tableScroll.setBorder(null);

        // 3. Khung Dưới: Tabs Thông tin & Chi tiết thanh toán
        JPanel bottomContainer = createBottomInfoContainer();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottomContainer);
        rightSplit.setResizeWeight(0.75);
        rightSplit.setDividerSize(4);

        rightPanel.add(rightSplit, BorderLayout.CENTER);
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createBottomInfoContainer() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(240, 245, 252));
        pnl.setPreferredSize(new Dimension(600, 110));
        pnl.setMinimumSize(new Dimension(600, 80));

        bottomTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        bottomTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Thông tin
        JPanel pnlThongTin = new JPanel(new GridLayout(2, 2, 20, 6));
        pnlThongTin.setBackground(Color.WHITE);
        pnlThongTin.setBorder(new EmptyBorder(8, 14, 8, 14));

        lblCreatedTime = new JLabel("Khởi tạo: ");
        lblCreatedTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblCreatedUser = new JLabel("Khởi tạo bởi: ");
        lblCreatedUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblModifiedTime = new JLabel("Sửa đổi gần nhất: ");
        lblModifiedTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblModifiedUser = new JLabel("Sửa đổi bởi: ");
        lblModifiedUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlThongTin.add(lblCreatedTime);
        pnlThongTin.add(lblCreatedUser);
        pnlThongTin.add(lblModifiedTime);
        pnlThongTin.add(lblModifiedUser);

        bottomTabbedPane.addTab("Thông tin", pnlThongTin);

        // Tab 2: Chi tiết thanh toán
        JPanel pnlChiTiet = new JPanel(new GridLayout(2, 3, 15, 6));
        pnlChiTiet.setBackground(Color.WHITE);
        pnlChiTiet.setBorder(new EmptyBorder(8, 14, 8, 14));

        lblPaymentMethod = new JLabel("Hình thức: ");
        lblPaymentMethod.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblPaymentAccount = new JLabel("Tài khoản nhận: ");
        lblPaymentAccount.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblPaymentAmount = new JLabel("Số tiền: ");
        lblPaymentAmount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPaymentAmount.setForeground(new Color(0, 100, 50));

        lblPaymentStore = new JLabel("Cửa hàng: ");
        lblPaymentStore.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblPaymentCustomer = new JLabel("Đối tượng: ");
        lblPaymentCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblPaymentNoDebtChange = new JLabel("Thay đổi công nợ: ");
        lblPaymentNoDebtChange.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlChiTiet.add(lblPaymentMethod);
        pnlChiTiet.add(lblPaymentAccount);
        pnlChiTiet.add(lblPaymentAmount);
        pnlChiTiet.add(lblPaymentStore);
        pnlChiTiet.add(lblPaymentCustomer);
        pnlChiTiet.add(lblPaymentNoDebtChange);

        bottomTabbedPane.addTab("Chi tiết thanh toán", pnlChiTiet);

        pnl.add(bottomTabbedPane, BorderLayout.CENTER);
        return pnl;
    }

    private JButton createToolButton(String text, String iconName, Color textColor, boolean isBold) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(textColor);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 210, 235), 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMiniToolButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadStoreFilterData() {
        cbCuaHangFilter.removeAllItems();
        cbCuaHangFilter.addItem("Tất cả cửa hàng");
        List<CuaHang> list = cuaHangDAO.getAll();
        for (CuaHang ch : list) {
            cbCuaHangFilter.addItem(ch);
        }
        cbCuaHangFilter.setSelectedIndex(0);
    }

    private void loadTreeData() {
        rootTreeNode.removeAllChildren();

        // 1. Node Tất cả
        DefaultMutableTreeNode nodeTatCa = new DefaultMutableTreeNode("Tất cả");
        rootTreeNode.add(nodeTatCa);

        // 2. Node Chưa thiết lập
        DefaultMutableTreeNode nodeChuaThietLap = new DefaultMutableTreeNode("Chưa thiết lập");
        rootTreeNode.add(nodeChuaThietLap);

        // 3. Các lý do thu chi đang hoạt động
        List<LyDoThuChi> allActive = lyDoThuChiDAO.getAllActive();
        for (LyDoThuChi tc : allActive) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(tc);
            rootTreeNode.add(node);
        }

        // 4. Node Thùng rác
        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");
        rootTreeNode.add(nodeTrash);

        treeModel.reload();

        // Mặc định chọn node Tất cả
        treeLyDoThuChi.setSelectionPath(new TreePath(nodeTatCa.getPath()));
    }

    public void loadReceiptsTable() {
        List<PhieuThu> all = "Thùng rác".equals(currentSelectedTreeCategory) ?
                phieuThuDAO.getTrashItems() : phieuThuDAO.getAll();

        Date tuNgay = (chkLocNgay.isSelected() && dpkTuNgay.getDate() != null) ? getStartOfDay(dpkTuNgay.getDate()) : null;
        Date denNgay = (chkLocNgay.isSelected() && dpkDenNgay.getDate() != null) ? getEndOfDay(dpkDenNgay.getDate()) : null;

        Object selectedStoreObj = cbCuaHangFilter.getSelectedItem();
        String selectedStoreId = (selectedStoreObj instanceof CuaHang) ? ((CuaHang) selectedStoreObj).getId() : null;

        List<PhieuThu> filtered = all.stream().filter(pt -> {
            // Lọc ngày
            if (tuNgay != null && pt.getNgay() != null && pt.getNgay().before(tuNgay)) return false;
            if (denNgay != null && pt.getNgay() != null && pt.getNgay().after(denNgay)) return false;

            // Lọc cửa hàng
            if (selectedStoreId != null && pt.getCuaHangId() != null && !selectedStoreId.equals(pt.getCuaHangId())) {
                return false;
            }

            // Lọc theo cây Lý do thu chi
            if ("LyDo".equals(currentSelectedTreeCategory) && currentSelectedReason != null) {
                if (pt.getPhanLoaiId() != null && !pt.getPhanLoaiId().equals(currentSelectedReason.getId())) {
                    if (pt.getTenPhanLoai() == null || !pt.getTenPhanLoai().equalsIgnoreCase(currentSelectedReason.getName())) {
                        return false;
                    }
                } else if (pt.getPhanLoaiId() == null) {
                    if (pt.getTenPhanLoai() == null || !pt.getTenPhanLoai().equalsIgnoreCase(currentSelectedReason.getName())) {
                        return false;
                    }
                }
            } else if ("Chưa thiết lập".equals(currentSelectedTreeCategory)) {
                if (pt.getPhanLoaiId() != null && !pt.getPhanLoaiId().trim().isEmpty()) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());

        // Lọc theo từ khóa tìm kiếm nhanh txtSearch
        String searchKey = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        if (!searchKey.isEmpty()) {
            filtered = filtered.stream().filter(pt -> {
                String sp = pt.getSoPhieu() != null ? pt.getSoPhieu().toLowerCase() : "";
                String ten = pt.getTenDoiTuong() != null ? pt.getTenDoiTuong().toLowerCase() : "";
                String dc = pt.getDiaChi() != null ? pt.getDiaChi().toLowerCase() : "";
                String pl = pt.getTenPhanLoai() != null ? pt.getTenPhanLoai().toLowerCase() : "";
                String ld = pt.getLyDoThu() != null ? pt.getLyDoThu().toLowerCase() : "";
                String note = pt.getGhiChu() != null ? pt.getGhiChu().toLowerCase() : "";
                String ctg = pt.getChungTuGoc() != null ? pt.getChungTuGoc().toLowerCase() : "";
                return sp.contains(searchKey) || ten.contains(searchKey) || dc.contains(searchKey)
                        || pl.contains(searchKey) || ld.contains(searchKey) || note.contains(searchKey) || ctg.contains(searchKey);
            }).collect(Collectors.toList());
        }

        currentDisplayedList = filtered;

        // Render table
        tableModel.setRowCount(0);
        for (PhieuThu pt : currentDisplayedList) {
            List<Object> rowData = new ArrayList<>();
            rowData.add(""); // STT
            for (String col : visibleColumns) {
                switch (col) {
                    case "Số phiếu":
                        rowData.add(pt.getSoPhieu() != null ? pt.getSoPhieu() : "");
                        break;
                    case "Ngày":
                        rowData.add(pt.getNgay() != null ? sdfDate.format(pt.getNgay()) : "");
                        break;
                    case "Tên đối tượng":
                        rowData.add(pt.getTenDoiTuong() != null ? pt.getTenDoiTuong() : "");
                        break;
                    case "Địa chỉ":
                        rowData.add(pt.getDiaChi() != null ? pt.getDiaChi() : "");
                        break;
                    case "Lý do thu chi":
                        rowData.add(pt.getTenPhanLoai() != null ? pt.getTenPhanLoai() : "");
                        break;
                    case "Diễn giải":
                        rowData.add(pt.getLyDoThu() != null ? pt.getLyDoThu() : "");
                        break;
                    case "Chứng từ gốc":
                        rowData.add(pt.getChungTuGoc() != null ? pt.getChungTuGoc() : "");
                        break;
                    case "Số tiền":
                        rowData.add(CurrencyUtil.format(pt.getSoTien()));
                        break;
                    case "Ghi chú":
                        rowData.add(pt.getGhiChu() != null ? pt.getGhiChu() : "");
                        break;
                    case "Chuyển khoản":
                        rowData.add(pt.isChuyenVaoTaiKhoan() ? (pt.getTenTaiKhoanNganHang() != null ? pt.getTenTaiKhoanNganHang() : "Có") : "Tiền mặt");
                        break;
                    case "Đặt hàng":
                        rowData.add("");
                        break;
                    case "Cửa hàng":
                        rowData.add(pt.getTenCuaHang() != null ? pt.getTenCuaHang() : "");
                        break;
                    case "Là phiếu thu công nợ":
                        rowData.add("Thu công nợ".equalsIgnoreCase(pt.getTenPhanLoai()) ? "Có" : "Không");
                        break;
                    case "Không thay đổi công nợ":
                        rowData.add(pt.isKhongThayDoiCongNo() ? "Không" : "Có");
                        break;
                    case "Tài khoản ngân hàng":
                        rowData.add(pt.getTenTaiKhoanNganHang() != null ? pt.getTenTaiKhoanNganHang() : "");
                        break;
                    case "Thẻ trả trước":
                        rowData.add("");
                        break;
                    case "Đặt hàng nhà cung cấp":
                        rowData.add(pt.getTenNhaCungCap() != null ? pt.getTenNhaCungCap() : "");
                        break;
                    default:
                        rowData.add("");
                        break;
                }
            }
            tableModel.addRow(rowData.toArray());
        }

        if (!currentDisplayedList.isEmpty()) {
            tablePhieuThu.setRowSelectionInterval(0, 0);
        } else {
            clearBottomDetailPanels();
        }
    }

    private void setupColumnWidths() {
        for (int i = 0; i < tablePhieuThu.getColumnModel().getColumnCount(); i++) {
            TableColumn tc = tablePhieuThu.getColumnModel().getColumn(i);
            String colName = tablePhieuThu.getColumnName(i);
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
                case "Là phiếu thu công nợ":
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
        for (int column = 0; column < tablePhieuThu.getColumnCount(); column++) {
            TableColumn tableColumn = tablePhieuThu.getColumnModel().getColumn(column);
            int preferredWidth = 40;

            // Header width
            TableCellRenderer headerRenderer = tablePhieuThu.getTableHeader().getDefaultRenderer();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        tablePhieuThu, tableColumn.getHeaderValue(), false, false, 0, column);
                if (headerComp != null) {
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
                }
            }

            // Sample rows width
            int rowCount = Math.min(tablePhieuThu.getRowCount(), 100);
            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer cellRenderer = tablePhieuThu.getCellRenderer(row, column);
                if (cellRenderer != null) {
                    Component c = tablePhieuThu.prepareRenderer(cellRenderer, row, column);
                    if (c != null) {
                        int width = c.getPreferredSize().width + tablePhieuThu.getIntercellSpacing().width + 16;
                        preferredWidth = Math.max(preferredWidth, width);
                    }
                }
            }

            tableColumn.setPreferredWidth(Math.min(preferredWidth, 400));
        }
        tablePhieuThu.getTableHeader().repaint();
    }

    private void showTableHeaderPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm Phiếu thu", IconHelper.getIcon("plus.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNewPhieuThu());
        popup.add(miAdd);

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa Phiếu thu", IconHelper.getIcon("pencil.png", 16, 16));
        miEdit.addActionListener(ev -> onEditSelectedPhieuThu());
        popup.add(miEdit);

        JMenuItem miDel = new JMenuItem("Xóa Phiếu thu", IconHelper.getIcon("cross.png", 16, 16));
        miDel.addActionListener(ev -> onDeleteSelectedPhieuThu());
        popup.add(miDel);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miChooseCols = new JMenuItem("Chọn cột hiển thị", IconHelper.getIcon("edit-column.png", 16, 16));
        if (miChooseCols.getIcon() == null) miChooseCols.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
        miChooseCols.addActionListener(ev -> onOpenColumnChooser());
        popup.add(miChooseCols);

        popup.show(tablePhieuThu.getTableHeader(), e.getX(), e.getY());
    }

    private void showTableBodyPopup(MouseEvent e) {
        int row = tablePhieuThu.rowAtPoint(e.getPoint());
        if (row >= 0 && row < tablePhieuThu.getRowCount()) {
            tablePhieuThu.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNewPhieuThu());
        popup.add(miAdd);

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        miEdit.addActionListener(ev -> onEditSelectedPhieuThu());
        popup.add(miEdit);

        JMenuItem miDel = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 16, 16));
        miDel.addActionListener(ev -> onDeleteSelectedPhieuThu());
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
            loadReceiptsTable();
        });
        popup.add(miRefresh);

        popup.show(tablePhieuThu, e.getX(), e.getY());
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
        loadReceiptsTable();
    }

    private void onFilterSearch() {
        loadReceiptsTable();
    }

    private void updateBottomDetailPanels(PhieuThu pt) {
        if (pt == null) {
            clearBottomDetailPanels();
            return;
        }

        // Tab Thông tin
        String timeCreatedStr = pt.getTimeCreated() != null ? sdfDateTime.format(pt.getTimeCreated()) :
                (pt.getNgay() != null ? sdfDateTime.format(pt.getNgay()) : "");
        lblCreatedTime.setText("Khởi tạo: " + timeCreatedStr);
        lblCreatedUser.setText("Khởi tạo bởi: " + (pt.getUserCreatedId() != null ? pt.getUserCreatedId() : "Administrator"));
        lblModifiedTime.setText("Sửa đổi gần nhất: ");
        lblModifiedUser.setText("Sửa đổi bởi: ");

        // Tab Chi tiết thanh toán
        lblPaymentMethod.setText("Hình thức: " + (pt.isChuyenVaoTaiKhoan() ? "Chuyển khoản" : "Tiền mặt"));
        lblPaymentAccount.setText("Tài khoản nhận: " + (pt.getTenTaiKhoanNganHang() != null ? pt.getTenTaiKhoanNganHang() : "--"));
        lblPaymentAmount.setText("Số tiền: " + CurrencyUtil.format(pt.getSoTien()) + " đ");
        lblPaymentStore.setText("Cửa hàng: " + (pt.getTenCuaHang() != null ? pt.getTenCuaHang() : "--"));
        lblPaymentCustomer.setText("Đối tượng: [" + pt.getLoaiDoiTuong() + "] " + (pt.getTenDoiTuong() != null ? pt.getTenDoiTuong() : ""));
        lblPaymentNoDebtChange.setText("Thay đổi công nợ: " + (pt.isKhongThayDoiCongNo() ? "Không thay đổi" : "Có tính vào công nợ"));
    }

    private void clearBottomDetailPanels() {
        lblCreatedTime.setText("Khởi tạo: ");
        lblCreatedUser.setText("Khởi tạo bởi: ");
        lblModifiedTime.setText("Sửa đổi gần nhất: ");
        lblModifiedUser.setText("Sửa đổi bởi: ");

        lblPaymentMethod.setText("Hình thức: ");
        lblPaymentAccount.setText("Tài khoản nhận: ");
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
            loadReceiptsTable();
        }
    }

    private void onAddNewPhieuThu() {
        LyDoThuChi preSelected = currentSelectedReason;
        TaoPhieuThuDialog dlg = new TaoPhieuThuDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                preSelected,
                this::loadReceiptsTable
        );
        dlg.setVisible(true);
    }

    private void onEditSelectedPhieuThu() {
        if (currentSelectedPhieuThu == null) {
            int row = tablePhieuThu.getSelectedRow();
            if (row >= 0 && row < currentDisplayedList.size()) {
                currentSelectedPhieuThu = currentDisplayedList.get(row);
            }
        }
        if (currentSelectedPhieuThu == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu thu trên bảng để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaoPhieuThuDialog dlg = new TaoPhieuThuDialog(
                SwingUtilities.getWindowAncestor(this),
                currentSelectedPhieuThu,
                this::loadReceiptsTable
        );
        dlg.setVisible(true);
    }

    private void onDeleteSelectedPhieuThu() {
        if (currentSelectedPhieuThu == null) {
            int row = tablePhieuThu.getSelectedRow();
            if (row >= 0 && row < currentDisplayedList.size()) {
                currentSelectedPhieuThu = currentDisplayedList.get(row);
            }
        }
        if (currentSelectedPhieuThu == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu thu trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phiếu thu [" + currentSelectedPhieuThu.getSoPhieu() + "] không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            phieuThuDAO.delete(currentSelectedPhieuThu.getId());
            loadReceiptsTable();
            JOptionPane.showMessageDialog(this, "Đã xóa phiếu thu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onExportExcel() {
        ExcelExporter.exportTable(tablePhieuThu, "Danh_Muc_Phieu_Thu", "Phiếu thu", this);
    }

    private void onPrint() {
        if (tablePhieuThu.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu phiếu thu để in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean complete = tablePhieuThu.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "In danh sách phiếu thu hoàn tất!", "In ấn", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTotalSummary() {
        double totalMoney = currentDisplayedList.stream().mapToDouble(PhieuThu::getSoTien).sum();
        int totalCount = currentDisplayedList.size();
        JOptionPane.showMessageDialog(
                this,
                "<html><b>TỔNG KẾT PHIẾU THU:</b><br>" +
                        "- Tổng số phiếu: <b>" + totalCount + "</b><br>" +
                        "- Tổng số tiền thu: <b style='color:green; font-size:13px;'>" + CurrencyUtil.format(totalMoney) + " đ</b></html>",
                "Tổng kết phiếu thu",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAnalyticsSummary() {
        Map<String, Double> byReason = new HashMap<>();
        for (PhieuThu pt : currentDisplayedList) {
            String name = (pt.getTenPhanLoai() != null && !pt.getTenPhanLoai().trim().isEmpty()) ? pt.getTenPhanLoai() : "Khác";
            byReason.put(name, byReason.getOrDefault(name, 0.0) + pt.getSoTien());
        }

        StringBuilder sb = new StringBuilder("<html><b>PHÂN TÍCH THU THEO LÝ DO:</b><br><br>");
        for (Map.Entry<String, Double> entry : byReason.entrySet()) {
            sb.append("• <b>").append(entry.getKey()).append("</b>: ")
              .append(CurrencyUtil.format(entry.getValue())).append(" đ<br>");
        }
        sb.append("</html>");

        JOptionPane.showMessageDialog(this, sb.toString(), "Phân tích phiếu thu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupShortcuts() {
        // F3: Lọc
        registerKeyboardAction(e -> {
            txtSearch.requestFocusInWindow();
            txtSearch.selectAll();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Insert: Thêm mới
        registerKeyboardAction(e -> onAddNewPhieuThu(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F4: Chỉnh sửa
        registerKeyboardAction(e -> onEditSelectedPhieuThu(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Delete: Xóa
        registerKeyboardAction(e -> onDeleteSelectedPhieuThu(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F5: Tải lại
        registerKeyboardAction(e -> {
            loadTreeData();
            loadReceiptsTable();
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
