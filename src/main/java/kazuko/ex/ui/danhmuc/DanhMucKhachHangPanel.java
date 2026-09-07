package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.BaoGiaDAO;
import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhachDatHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.dao.TangGiamDiemDAO;
import kazuko.ex.model.baogia.BaoGia;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.congno.ChiTietCongNoKhachHangEntry;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.model.danhmuc.TangGiamDiem;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.util.ExcelExporter;
import kazuko.ex.util.IconHelper;
import kazuko.ex.util.KhachHangExcelUtil;

import javax.swing.*;
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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DanhMucKhachHangPanel extends JPanel {
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final BaoGiaDAO baoGiaDAO = new BaoGiaDAO();
    private final KhachDatHangDAO khachDatHangDAO = new KhachDatHangDAO();
    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final TangGiamDiemDAO tangGiamDiemDAO = new TangGiamDiemDAO();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final CongNoKhachHangDAO congNoKhachHangDAO = new CongNoKhachHangDAO();

    private JTree treeNhomKhachHang;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;

    private JTable tableKhachHang;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    private JLabel lblCreatedTime;
    private JLabel lblCreatedUser;
    private JLabel lblModifiedTime;
    private JLabel lblModifiedUser;

    // Tab 2: Báo giá
    private JTable tableBaoGia;
    private DefaultTableModel modelBaoGia;
    private List<String> visibleColsBaoGia;
    private final List<String> allColsBaoGia = Arrays.asList("Ngày", "Số phiếu", "Tên khách", "Địa chỉ", "Điện thoại", "Tiền hàng", "Tiền giảm giá", "Tổng cộng", "Tỉ lệ thuế", "Tiền thuế", "Phí vận chuyển", "Email", "Ghi chú", "Khách hàng", "Tỉ lệ giảm giá");
    private final List<String> defaultColsBaoGia = new ArrayList<>(allColsBaoGia);
    private List<BaoGia> currentBaoGiaList = new ArrayList<>();

    // Tab 3: Đặt hàng
    private JTable tableDatHang;
    private DefaultTableModel modelDatHang;
    private List<String> visibleColsDatHang;
    private final List<String> allColsDatHang = Arrays.asList("Ngày", "Số phiếu", "Tên khách", "Địa chỉ", "Điện thoại", "Email", "Tổng cộng", "Tỉ lệ thuế", "Tiền thuế", "Phí vận chuyển", "Ghi chú", "Khách hàng", "Tỉ lệ giảm giá", "Tiền giảm giá", "Tiền hàng");
    private final List<String> defaultColsDatHang = Arrays.asList("Ngày", "Số phiếu", "Tên khách", "Địa chỉ", "Điện thoại", "Email", "Tổng cộng", "Tỉ lệ thuế", "Tiền thuế", "Phí vận chuyển");
    private List<KhachDatHang> currentDatHangList = new ArrayList<>();

    // Tab 4: Đơn hàng
    private JTable tableDonHang;
    private DefaultTableModel modelDonHang;
    private List<String> visibleColsDonHang;
    private final List<String> allColsDonHang = Arrays.asList("Số phiếu", "Ngày", "Khách hàng", "Tiền hàng", "Giảm giá", "Đổi trả", "Tổng cộng", "Giờ thanh toán", "Thanh toán bởi", "Ghi chú");
    private final List<String> defaultColsDonHang = Arrays.asList("Số phiếu", "Ngày", "Khách hàng", "Tiền hàng", "Giảm giá", "Đổi trả", "Tổng cộng", "Giờ thanh toán", "Thanh toán bởi", "Ghi chú");
    private List<DonHang> currentDonHangList = new ArrayList<>();

    // Tab 5: Sản xuất
    private JTable tableSanXuat;
    private DefaultTableModel modelSanXuat;
    private List<String> visibleColsSanXuat;
    private final List<String> allColsSanXuat = Arrays.asList("Số phiếu", "Ngày", "Tổng cộng", "Kho thành phẩm", "Kho vật tư", "Bàn", "Số khách", "Đặt hàng nhà cung cấp");
    private final List<String> defaultColsSanXuat = Arrays.asList("Số phiếu", "Ngày", "Tổng cộng", "Kho thành phẩm", "Kho vật tư", "Bàn", "Số khách", "Đặt hàng nhà cung cấp");
    private List<DonHang> currentSanXuatList = new ArrayList<>();

    // Tab 6: Tăng giảm điểm
    private JTable tableTangGiamDiem;
    private DefaultTableModel modelTangGiamDiem;
    private List<String> visibleColsTangGiamDiem;
    private final List<String> allColsTangGiamDiem = Arrays.asList("Số phiếu", "Ngày", "Khách hàng", "Điểm tăng", "Điểm giảm", "Lý do", "Ghi chú");
    private final List<String> defaultColsTangGiamDiem = Arrays.asList("Số phiếu", "Ngày", "Khách hàng", "Điểm tăng", "Điểm giảm", "Lý do", "Ghi chú");
    private List<TangGiamDiem> currentTangGiamDiemList = new ArrayList<>();

    // Tab 7: Phiếu thu
    private JTable tablePhieuThu;
    private DefaultTableModel modelPhieuThu;
    private List<String> visibleColsPhieuThu;
    private final List<String> allColsPhieuThu = Arrays.asList("Số phiếu", "Ngày", "Phân loại thu", "Người nộp", "Địa chỉ", "Số tiền", "Lý do thu", "Chứng từ gốc", "Ghi chú");
    private final List<String> defaultColsPhieuThu = Arrays.asList("Số phiếu", "Ngày", "Phân loại thu", "Người nộp", "Địa chỉ", "Số tiền", "Lý do thu", "Chứng từ gốc", "Ghi chú");
    private List<PhieuThu> currentPhieuThuList = new ArrayList<>();

    // Tab 8: Phiếu chi
    private JTable tablePhieuChi;
    private DefaultTableModel modelPhieuChi;
    private List<String> visibleColsPhieuChi;
    private final List<String> allColsPhieuChi = Arrays.asList("Số phiếu", "Ngày", "Phân loại chi", "Người nhận", "Địa chỉ", "Số tiền", "Lý do chi", "Chứng từ gốc", "Ghi chú");
    private final List<String> defaultColsPhieuChi = Arrays.asList("Số phiếu", "Ngày", "Phân loại chi", "Người nhận", "Địa chỉ", "Số tiền", "Lý do chi", "Chứng từ gốc", "Ghi chú");
    private List<PhieuChi> currentPhieuChiList = new ArrayList<>();

    // Tab 9: Phiếu thu công nợ
    private JTable tablePhieuThuCongNo;
    private DefaultTableModel modelPhieuThuCongNo;
    private List<String> visibleColsPhieuThuCongNo;
    private final List<String> allColsPhieuThuCongNo = Arrays.asList("Số phiếu", "Ngày", "Số tiền", "Diễn giải", "Loại chứng từ", "Còn nợ");
    private final List<String> defaultColsPhieuThuCongNo = Arrays.asList("Số phiếu", "Ngày", "Số tiền", "Diễn giải", "Loại chứng từ", "Còn nợ");
    private List<ChiTietCongNoKhachHangEntry> currentCongNoList = new ArrayList<>();

    private JButton btnAddKhachHang;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRestore;
    private List<KhachHang> currentList = new ArrayList<>();
    private List<String> visibleColumns = new ArrayList<>(getDefaultVisibleColumns());
    private String currentSelectedNhom = "Tất cả";
    private String currentSelectedNhomId = null;
    private String currentSortField = "Mã khách";
    private boolean currentSortAsc = true;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

    public DanhMucKhachHangPanel() {
        initUI();
        loadNhomTree();
        loadTableData("Tất cả", null, "");
        setupShortcuts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 243, 248));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(220);
        mainSplit.setDividerSize(5);

        // ==================== KHUNG TRÁI: NHÓM KHÁCH HÀNG ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 0, new Color(185, 205, 230)));

        // Title Header Khung Trái
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(225, 235, 248));
        leftHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        leftHeader.setPreferredSize(new Dimension(240, 28));

        JLabel lblLeftTitle = new JLabel(" Nhóm khách hàng");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(25, 55, 105));
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);

        // Toolbar nhỏ cho Nhóm khách hàng
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        leftToolbar.setOpaque(false);

        JButton btnAddMenu = createMiniToolButton("plus.png", "Thêm nhóm khách hàng");
        JButton btnEditNhom = createMiniToolButton("pencil.png", "Sửa nhóm khách hàng");
        JButton btnDeleteNhom = createMiniToolButton("cross.png", "Chuyển vào thùng rác");
        JButton btnTrash = createMiniToolButton("RecycleBin.png", "Thùng rác");
        JButton btnRefreshNhom = createMiniToolButton("Refresh.png", "Làm mới danh mục nhóm");

        btnAddMenu.addActionListener(e -> onAddNhom());
        btnEditNhom.addActionListener(e -> onEditNhom());
        btnDeleteNhom.addActionListener(e -> onDeleteNhom());
        btnTrash.addActionListener(e -> onOpenNhomTrash());
        btnRefreshNhom.addActionListener(e -> loadNhomTree());

        leftToolbar.add(btnAddMenu);
        leftToolbar.add(btnEditNhom);
        leftToolbar.add(btnDeleteNhom);
        leftToolbar.add(btnTrash);
        leftToolbar.add(btnRefreshNhom);
        leftHeader.add(leftToolbar, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Tree Nhóm khách hàng
        rootTreeNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeNhomKhachHang = new JTree(treeModel);
        treeNhomKhachHang.setRootVisible(false);
        treeNhomKhachHang.setShowsRootHandles(true);
        treeNhomKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomKhachHang.setRowHeight(22);

        // Drag and drop sắp xếp / phân cấp
        treeNhomKhachHang.setDragEnabled(true);
        treeNhomKhachHang.setDropMode(DropMode.ON_OR_INSERT);
        treeNhomKhachHang.setTransferHandler(new TreeTransferHandler(nhomKhachHangDAO, () -> loadNhomTree(currentSelectedNhom)));

        treeNhomKhachHang.setCellRenderer(new DefaultTreeCellRenderer() {
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
                    } else if (userObj instanceof NhomKhachHang) {
                        NhomKhachHang nh = (NhomKhachHang) userObj;
                        if (nh.isSeparator()) {
                            label.setText("----------------------------------------------");
                            label.setForeground(Color.LIGHT_GRAY);
                            label.setIcon(null);
                        } else if (nh.isFolder()) {
                            label.setText(nh.getName());
                            label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        } else {
                            label.setText(nh.getName());
                            String iconName = nh.getIconName();
                            if (iconName == null || iconName.trim().isEmpty() || "Folder.png".equalsIgnoreCase(iconName.trim())) {
                                iconName = "users.png";
                            }
                            label.setIcon(IconHelper.getIcon(iconName, 16, 16));
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        }
                    }
                }
                return label;
            }
        });

        treeNhomKhachHang.addTreeSelectionListener(e -> {
            TreePath path = treeNhomKhachHang.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObj = node.getUserObject();
                if ("Tất cả".equals(userObj)) {
                    currentSelectedNhom = "Tất cả";
                    currentSelectedNhomId = null;
                    updateToolbarState();
                    loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
                } else if ("Chưa thiết lập".equals(userObj)) {
                    currentSelectedNhom = "Chưa thiết lập";
                    currentSelectedNhomId = null;
                    updateToolbarState();
                    loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
                } else if ("Thùng rác".equals(userObj)) {
                    currentSelectedNhom = "Thùng rác";
                    currentSelectedNhomId = null;
                    updateToolbarState();
                    loadTrashTableData(txtSearch.getText().trim());
                } else if (userObj instanceof NhomKhachHang) {
                    NhomKhachHang nh = (NhomKhachHang) userObj;
                    if (nh.isSeparator()) return;
                    currentSelectedNhom = nh.getName();
                    currentSelectedNhomId = nh.getId();
                    updateToolbarState();

                    if (nh.isFolder()) {
                        Set<String> descIds = nhomKhachHangDAO.getAllDescendantIds(nh.getId());
                        Set<String> descNames = new HashSet<>();
                        for (NhomKhachHang item : nhomKhachHangDAO.getAll()) {
                            if (descIds.contains(item.getId()) && item.getName() != null) {
                                descNames.add(item.getName());
                            }
                        }
                        loadTableDataByGroupSet(descIds, descNames, txtSearch.getText().trim());
                    } else {
                        loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
                    }
                }
            }
        });

        // Double click vào nhóm khách hàng để sửa, Right click mở popup menu
        treeNhomKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    TreePath path = treeNhomKhachHang.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if ("Thùng rác".equals(node.getUserObject())) {
                            onOpenNhomTrash();
                        } else if (node.getUserObject() instanceof NhomKhachHang) {
                            NhomKhachHang nh = (NhomKhachHang) node.getUserObject();
                            if (!nh.isSeparator()) {
                                NhomKhachHangDialog dialog = new NhomKhachHangDialog(DanhMucKhachHangPanel.this, nh, () -> {
                                    loadNhomTree(nh.getName());
                                });
                                dialog.setVisible(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTreePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTreePopup(e);
            }
        });

        treeNhomKhachHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    onDeleteNhom();
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    onAddNhom();
                } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                    onEditNhom();
                } else if (e.getKeyCode() == KeyEvent.VK_F2) {
                    onRenameNhom();
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(treeNhomKhachHang);
        treeScroll.setBorder(null);
        leftPanel.add(treeScroll, BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG KHÁCH HÀNG ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Toolbar Khung Phải
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topBar.setBackground(new Color(225, 235, 248));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JLabel lblTag = new JLabel("Khách hàng");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTag.setForeground(new Color(40, 70, 120));
        lblTag.setBorder(new EmptyBorder(0, 4, 0, 6));
        topBar.add(lblTag);

        JLabel lblLoc = new JLabel("Lọc (F3):");
        lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topBar.add(lblLoc);

        txtSearch = new JTextField(12);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setPreferredSize(new Dimension(140, 24));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onSearch(); }
        });
        topBar.add(txtSearch);

        btnAddKhachHang = createToolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        btnEdit = createToolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        btnDelete = createToolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        btnRestore = createToolButton("Khôi phục", "Undo.png", new Color(0, 100, 180), false);
        btnRestore.setVisible(false);
        JButton btnImportExcel = createToolButton("Thêm excel", "page_white_excel.png", Color.BLACK, false);
        JButton btnExportExcel = createToolButton("Xuất excel", "export_excel.png", Color.BLACK, false);
        JButton btnPrint = createToolButton("In", "Preview.png", Color.BLACK, false);

        btnAddKhachHang.addActionListener(e -> onAddNewCustomer());
        btnEdit.addActionListener(e -> onEditSelectedCustomer());
        btnDelete.addActionListener(e -> onDeleteSelectedCustomer());
        btnRestore.addActionListener(e -> onRestoreSelectedCustomer());
        btnImportExcel.addActionListener(e -> onOpenThemNhanhExcel());
        btnExportExcel.addActionListener(e -> onExportExcel());
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));

        topBar.add(btnAddKhachHang);
        topBar.add(btnEdit);
        topBar.add(btnDelete);
        topBar.add(btnRestore);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Khách hàng
        List<String> fullColList = new ArrayList<>();
        fullColList.add(""); // 0: Pointer
        fullColList.addAll(visibleColumns);
        String[] columns = fullColList.toArray(new String[0]);

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableKhachHang = new JTable(tableModel);
        tableKhachHang.setRowHeight(22);
        tableKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableKhachHang.setShowGrid(true);
        tableKhachHang.setGridColor(new Color(225, 232, 240));

        JTableHeader th = tableKhachHang.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(230, 238, 248));
        th.setForeground(new Color(30, 30, 30));
        th.setPreferredSize(new Dimension(100, 26));

        // Click sort header & right click popup header
        th.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int colIndex = tableKhachHang.columnAtPoint(e.getPoint());
                    if (colIndex >= 0) {
                        int modelCol = tableKhachHang.convertColumnIndexToModel(colIndex);
                        String colName = tableKhachHang.getModel().getColumnName(modelCol);
                        if (colName != null && !colName.trim().isEmpty()) {
                            if (colName.equalsIgnoreCase(currentSortField)) {
                                currentSortAsc = !currentSortAsc;
                            } else {
                                currentSortField = colName;
                                currentSortAsc = true;
                            }
                            applyCurrentSort();
                            renderTableData();
                            th.repaint();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableHeaderPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableHeaderPopup(e);
                }
            }
        });

        setupTableRenderers();

        tableKhachHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBottomInfo();
            }
        });

        tableKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelectedCustomer();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTablePopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTablePopup(e);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableKhachHang);
        scrollPane.setBorder(null);

        // 3. Bottom Panel (9 Tabs chi tiết)
        JPanel bottomDetails = createBottomTabbedPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, bottomDetails);
        rightSplit.setResizeWeight(0.72);
        rightSplit.setDividerSize(4);
        rightSplit.setBorder(null);

        rightPanel.add(rightSplit, BorderLayout.CENTER);
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createBottomTabbedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(100, 180));
        panel.setMinimumSize(new Dimension(100, 110));

        JTabbedPane tabInfo = new JTabbedPane(JTabbedPane.TOP);
        tabInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel pnlInfo = new JPanel(new GridLayout(2, 2, 20, 4));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblCreatedTime = new JLabel("Khởi tạo: --");
        lblCreatedTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblCreatedUser = new JLabel("Khởi tạo bởi: --");
        lblCreatedUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblModifiedTime = new JLabel("Sửa đổi gần nhất: --");
        lblModifiedTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblModifiedUser = new JLabel("Sửa đổi bởi: --");
        lblModifiedUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlInfo.add(lblCreatedTime);
        pnlInfo.add(lblCreatedUser);
        pnlInfo.add(lblModifiedTime);
        pnlInfo.add(lblModifiedUser);

        tabInfo.addTab("Thông tin", pnlInfo);
        tabInfo.addTab("Báo giá", createBaoGiaTab());
        tabInfo.addTab("Đặt hàng", createDatHangTab());
        tabInfo.addTab("Đơn hàng", createDonHangTab());
        tabInfo.addTab("Sản xuất", createSanXuatTab());
        tabInfo.addTab("Tăng giảm điểm", createTangGiamDiemTab());
        tabInfo.addTab("Phiếu thu", createPhieuThuTab());
        tabInfo.addTab("Phiếu chi", createPhieuChiTab());
        tabInfo.addTab("Phiếu thu công nợ", createPhieuThuCongNoTab());

        panel.add(tabInfo, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBaoGiaTab() {
        visibleColsBaoGia = new ArrayList<>(defaultColsBaoGia);
        modelBaoGia = createDetailModel(visibleColsBaoGia);
        tableBaoGia = createDetailTable(modelBaoGia, () -> showDetailColumnChooser(
                visibleColsBaoGia, allColsBaoGia, defaultColsBaoGia, cols -> {
                    visibleColsBaoGia = new ArrayList<>(cols);
                    renderBaoGiaTable();
                }));
        return wrapDetailTable(tableBaoGia);
    }

    private JPanel createDatHangTab() {
        visibleColsDatHang = new ArrayList<>(defaultColsDatHang);
        modelDatHang = createDetailModel(visibleColsDatHang);
        tableDatHang = createDetailTable(modelDatHang, () -> showDetailColumnChooser(
                visibleColsDatHang, allColsDatHang, defaultColsDatHang, cols -> {
                    visibleColsDatHang = new ArrayList<>(cols);
                    renderDatHangTable();
                }));
        return wrapDetailTable(tableDatHang);
    }

    private JPanel createDonHangTab() {
        visibleColsDonHang = new ArrayList<>(defaultColsDonHang);
        modelDonHang = createDetailModel(visibleColsDonHang);
        tableDonHang = createDetailTable(modelDonHang, () -> showDetailColumnChooser(
                visibleColsDonHang, allColsDonHang, defaultColsDonHang, cols -> {
                    visibleColsDonHang = new ArrayList<>(cols);
                    renderDonHangTable();
                }));
        return wrapDetailTable(tableDonHang);
    }

    private JPanel createSanXuatTab() {
        visibleColsSanXuat = new ArrayList<>(defaultColsSanXuat);
        modelSanXuat = createDetailModel(visibleColsSanXuat);
        tableSanXuat = createDetailTable(modelSanXuat, () -> showDetailColumnChooser(
                visibleColsSanXuat, allColsSanXuat, defaultColsSanXuat, cols -> {
                    visibleColsSanXuat = new ArrayList<>(cols);
                    renderSanXuatTable();
                }));
        return wrapDetailTable(tableSanXuat);
    }

    private JPanel createTangGiamDiemTab() {
        visibleColsTangGiamDiem = new ArrayList<>(defaultColsTangGiamDiem);
        modelTangGiamDiem = createDetailModel(visibleColsTangGiamDiem);
        tableTangGiamDiem = createDetailTable(modelTangGiamDiem, () -> showDetailColumnChooser(
                visibleColsTangGiamDiem, allColsTangGiamDiem, defaultColsTangGiamDiem, cols -> {
                    visibleColsTangGiamDiem = new ArrayList<>(cols);
                    renderTangGiamDiemTable();
                }));
        return wrapDetailTable(tableTangGiamDiem);
    }

    private JPanel createPhieuThuTab() {
        visibleColsPhieuThu = new ArrayList<>(defaultColsPhieuThu);
        modelPhieuThu = createDetailModel(visibleColsPhieuThu);
        tablePhieuThu = createDetailTable(modelPhieuThu, () -> showDetailColumnChooser(
                visibleColsPhieuThu, allColsPhieuThu, defaultColsPhieuThu, cols -> {
                    visibleColsPhieuThu = new ArrayList<>(cols);
                    renderPhieuThuTable();
                }));
        return wrapDetailTable(tablePhieuThu);
    }

    private JPanel createPhieuChiTab() {
        visibleColsPhieuChi = new ArrayList<>(defaultColsPhieuChi);
        modelPhieuChi = createDetailModel(visibleColsPhieuChi);
        tablePhieuChi = createDetailTable(modelPhieuChi, () -> showDetailColumnChooser(
                visibleColsPhieuChi, allColsPhieuChi, defaultColsPhieuChi, cols -> {
                    visibleColsPhieuChi = new ArrayList<>(cols);
                    renderPhieuChiTable();
                }));
        return wrapDetailTable(tablePhieuChi);
    }

    private JPanel createPhieuThuCongNoTab() {
        visibleColsPhieuThuCongNo = new ArrayList<>(defaultColsPhieuThuCongNo);
        modelPhieuThuCongNo = createDetailModel(visibleColsPhieuThuCongNo);
        tablePhieuThuCongNo = createDetailTable(modelPhieuThuCongNo, () -> showDetailColumnChooser(
                visibleColsPhieuThuCongNo, allColsPhieuThuCongNo, defaultColsPhieuThuCongNo, cols -> {
                    visibleColsPhieuThuCongNo = new ArrayList<>(cols);
                    renderPhieuThuCongNoTable();
                }));
        return wrapDetailTable(tablePhieuThuCongNo);
    }

    private DefaultTableModel createDetailModel(List<String> visibleColumns) {
        List<String> headers = detailHeaders(visibleColumns);
        return new DefaultTableModel(headers.toArray(new String[0]), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private List<String> detailHeaders(List<String> visibleColumns) {
        List<String> headers = new ArrayList<>();
        headers.add("");
        headers.addAll(visibleColumns);
        return headers;
    }

    private JTable createDetailTable(DefaultTableModel model, Runnable chooseColumnsAction) {
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(22);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(225, 232, 240));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        header.setBackground(new Color(230, 238, 248));
        header.setForeground(new Color(30, 30, 30));
        header.setPreferredSize(new Dimension(100, 26));

        MouseAdapter popupListener = new MouseAdapter() {
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem chooseColumns = new JMenuItem("Chọn cột hiển thị...", IconHelper.getIcon("table_column.png", 14, 14));
                    chooseColumns.addActionListener(event -> chooseColumnsAction.run());
                    popup.add(chooseColumns);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override public void mousePressed(MouseEvent e) { showPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { showPopup(e); }
        };
        header.addMouseListener(popupListener);
        table.addMouseListener(popupListener);

        setupDetailTableRenderers(table);
        return table;
    }

    private JPanel wrapDetailTable(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void showDetailColumnChooser(List<String> visibleColumns, List<String> allColumns,
                                         List<String> defaultColumns,
                                         java.util.function.Consumer<List<String>> callback) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChonCotHienThiDialog dialog = new ChonCotHienThiDialog(
                owner, visibleColumns, allColumns, defaultColumns, callback);
        dialog.setVisible(true);
    }

    private void setupDetailTableRenderers(JTable table) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");

        DefaultTableCellRenderer pointerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable target, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        target, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setText(isSelected ? "▶" : "");
                if (!isSelected) label.setBackground(detailRowColor(row));
                return label;
            }
        };

        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable target, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        target, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                if (!isSelected) label.setBackground(detailRowColor(row));
                return label;
            }
        };

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable target, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        target, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) label.setBackground(detailRowColor(row));
                return label;
            }
        };

        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable target, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        target, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    label.setText(decimalFormat.format(((Number) value).doubleValue()));
                }
                if (!isSelected) label.setBackground(detailRowColor(row));
                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            String columnName = table.getColumnName(i);
            TableColumn column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setCellRenderer(pointerRenderer);
                column.setPreferredWidth(28);
                column.setMaxWidth(32);
            } else if (isNumericDetailColumn(columnName)) {
                column.setCellRenderer(numberRenderer);
                column.setPreferredWidth(columnName.startsWith("Tỉ lệ") ? 82 : 105);
            } else if ("Ngày".equals(columnName) || "Số phiếu".equals(columnName)
                    || "Giờ thanh toán".equals(columnName) || "Số khách".equals(columnName)) {
                column.setCellRenderer(centerRenderer);
                column.setPreferredWidth("Ngày".equals(columnName) ? 90 : 105);
            } else {
                column.setCellRenderer(textRenderer);
                if ("Địa chỉ".equals(columnName)) column.setPreferredWidth(170);
                else if (columnName.contains("khách") || columnName.contains("Khách")
                        || columnName.contains("Người") || columnName.contains("Phân loại")) column.setPreferredWidth(145);
                else if ("Email".equals(columnName) || columnName.contains("Ghi chú")
                        || columnName.contains("Lý do") || columnName.contains("Diễn giải")) column.setPreferredWidth(150);
                else column.setPreferredWidth(105);
            }
        }
    }

    private boolean isNumericDetailColumn(String columnName) {
        return columnName != null && (columnName.startsWith("Tiền")
                || columnName.startsWith("Tổng")
                || columnName.startsWith("Tỉ lệ")
                || columnName.startsWith("Điểm")
                || "Giảm giá".equals(columnName)
                || "Đổi trả".equals(columnName)
                || "Số tiền".equals(columnName)
                || "Còn nợ".equals(columnName));
    }

    private Color detailRowColor(int row) {
        return row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255);
    }

    private void setupTableRenderers() {
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
                lbl.setBackground(isSelected ? new Color(210, 230, 250) : (row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255)));
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

        DefaultTableCellRenderer nhomRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setIcon(IconHelper.getIcon("Folder.png", 14, 14));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        };

        DefaultTableCellRenderer staffRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (value != null && !value.toString().trim().isEmpty()) {
                    lbl.setIcon(IconHelper.getIcon("users.png", 14, 14));
                } else {
                    lbl.setIcon(null);
                }
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

        for (int i = 0; i < tableKhachHang.getColumnCount(); i++) {
            String col = tableKhachHang.getColumnName(i);
            if (i == 0 || col.isEmpty()) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(pointerRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(26);
                tableKhachHang.getColumnModel().getColumn(i).setMaxWidth(30);
            } else if ("Mã khách".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(95);
            } else if ("Tên khách hàng".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(180);
            } else if ("Địa chỉ".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(170);
            } else if ("Điện thoại".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(95);
            } else if ("Email".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(100);
            } else if ("Nhóm khách hàng".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(nhomRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(110);
            } else if ("Mã số thuế".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(85);
            } else if ("Nhân viên".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(staffRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(125);
            } else if ("Ngày thành lập/sinh nhật".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(120);
            } else if ("Tỉnh thành".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(110);
            } else if ("Facebook".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(120);
            } else if ("Điểm tích lũy ban đầu".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(120);
            } else if ("Ghi chú".equals(col)) {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(150);
            } else {
                tableKhachHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableKhachHang.getColumnModel().getColumn(i).setPreferredWidth(100);
            }
        }

        // Header Renderer with Sort Arrow
        TableCellRenderer defaultHeaderRenderer = tableKhachHang.getTableHeader().getDefaultRenderer();
        tableKhachHang.getTableHeader().setDefaultRenderer((table, value, isSelected, hasFocus, row, col) -> {
            Component c = defaultHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String colTitle = value != null ? value.toString() : "";
                if (colTitle.equalsIgnoreCase(currentSortField)) {
                    lbl.setText(colTitle + (currentSortAsc ? " ▲" : " ▼"));
                    lbl.setForeground(new Color(0, 70, 160));
                } else {
                    lbl.setText(colTitle);
                    lbl.setForeground(new Color(30, 30, 30));
                }
            }
            return c;
        });
    }

    private JButton createToolButton(String text, String iconName, Color textColor, boolean isBold) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private JButton createMiniToolButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(1, 1, 1, 1));
        return btn;
    }

    private void loadNhomTree() {
        loadNhomTree(null);
    }

    private void loadNhomTree(String selectGroupName) {
        rootTreeNode.removeAllChildren();

        DefaultMutableTreeNode nodeTatCa = new DefaultMutableTreeNode("Tất cả");
        DefaultMutableTreeNode nodeChuaThietLap = new DefaultMutableTreeNode("Chưa thiết lập");
        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");

        rootTreeNode.add(nodeTatCa);
        rootTreeNode.add(nodeChuaThietLap);

        List<NhomKhachHang> groups = nhomKhachHangDAO.getAll();
        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        for (NhomKhachHang g : groups) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(g);
            nodeMap.put(g.getId(), node);
        }

        for (NhomKhachHang g : groups) {
            DefaultMutableTreeNode node = nodeMap.get(g.getId());
            if (g.getParentId() != null && nodeMap.containsKey(g.getParentId())) {
                nodeMap.get(g.getParentId()).add(node);
            } else {
                rootTreeNode.add(node);
            }
        }

        rootTreeNode.add(nodeTrash);
        treeModel.reload();

        for (int i = 0; i < treeNhomKhachHang.getRowCount(); i++) {
            treeNhomKhachHang.expandRow(i);
        }

        if (selectGroupName != null) {
            selectNodeByName(selectGroupName);
        } else {
            treeNhomKhachHang.setSelectionRow(0);
        }
    }

    private void selectNodeByName(String name) {
        for (int i = 0; i < treeNhomKhachHang.getRowCount(); i++) {
            TreePath path = treeNhomKhachHang.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (name.equalsIgnoreCase(node.getUserObject().toString())) {
                treeNhomKhachHang.setSelectionPath(path);
                break;
            }
        }
    }

    private void loadTableData(String tenNhom, String nhomId, String keyword) {
        currentList = khachHangDAO.search(keyword, tenNhom, nhomId);
        applyCurrentSort();
        renderTableData();
    }

    private void loadTableDataByGroupSet(Set<String> ids, Set<String> names, String keyword) {
        currentList = khachHangDAO.searchByGroupSet(keyword, ids, names);
        applyCurrentSort();
        renderTableData();
    }

    private void loadTrashTableData(String keyword) {
        List<KhachHang> trash = khachHangDAO.getTrashItems();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            trash.removeIf(k -> (k.getName() == null || !k.getName().toLowerCase().contains(kw)) &&
                                (k.getMaKhach() == null || !k.getMaKhach().toLowerCase().contains(kw)));
        }
        currentList = trash;
        applyCurrentSort();
        renderTableData();
    }

    private void applyCurrentSort() {
        if (currentList == null) return;
        currentList.sort((a, b) -> {
            int cmp = 0;
            if ("Mã khách".equalsIgnoreCase(currentSortField)) {
                String ca = a.getMaKhach() != null ? a.getMaKhach() : "";
                String cb = b.getMaKhach() != null ? b.getMaKhach() : "";
                cmp = ca.compareToIgnoreCase(cb);
            } else if ("Tên khách hàng".equalsIgnoreCase(currentSortField)) {
                String na = a.getName() != null ? a.getName() : "";
                String nb = b.getName() != null ? b.getName() : "";
                cmp = na.compareToIgnoreCase(nb);
            } else if ("Địa chỉ".equalsIgnoreCase(currentSortField)) {
                String da = a.getDiaChi() != null ? a.getDiaChi() : "";
                String db = b.getDiaChi() != null ? b.getDiaChi() : "";
                cmp = da.compareToIgnoreCase(db);
            } else if ("Nhóm khách hàng".equalsIgnoreCase(currentSortField)) {
                String ga = a.getTenNhomKhachHang() != null ? a.getTenNhomKhachHang() : "";
                String gb = b.getTenNhomKhachHang() != null ? b.getTenNhomKhachHang() : "";
                cmp = ga.compareToIgnoreCase(gb);
            } else if ("Điện thoại".equalsIgnoreCase(currentSortField)) {
                String pa = a.getDienThoai() != null ? a.getDienThoai() : "";
                String pb = b.getDienThoai() != null ? b.getDienThoai() : "";
                cmp = pa.compareToIgnoreCase(pb);
            } else if ("Email".equalsIgnoreCase(currentSortField)) {
                String ea = a.getEmail() != null ? a.getEmail() : "";
                String eb = b.getEmail() != null ? b.getEmail() : "";
                cmp = ea.compareToIgnoreCase(eb);
            } else if ("Mã số thuế".equalsIgnoreCase(currentSortField)) {
                String ma = a.getMaSoThue() != null ? a.getMaSoThue() : "";
                String mb = b.getMaSoThue() != null ? b.getMaSoThue() : "";
                cmp = ma.compareToIgnoreCase(mb);
            } else if ("Nhân viên".equalsIgnoreCase(currentSortField)) {
                String sa = a.getTenNhanVien() != null ? a.getTenNhanVien() : "";
                String sb = b.getTenNhanVien() != null ? b.getTenNhanVien() : "";
                cmp = sa.compareToIgnoreCase(sb);
            } else if ("Ngày thành lập/sinh nhật".equalsIgnoreCase(currentSortField)) {
                long ta = a.getNgaySinhNhat() != null ? a.getNgaySinhNhat().getTime() : 0;
                long tb = b.getNgaySinhNhat() != null ? b.getNgaySinhNhat().getTime() : 0;
                cmp = Long.compare(ta, tb);
            } else if ("Tỉnh thành".equalsIgnoreCase(currentSortField)) {
                String ta = a.getTinhThanh() != null ? a.getTinhThanh() : "";
                String tb = b.getTinhThanh() != null ? b.getTinhThanh() : "";
                cmp = ta.compareToIgnoreCase(tb);
            } else if ("Facebook".equalsIgnoreCase(currentSortField)) {
                String fa = a.getFacebook() != null ? a.getFacebook() : "";
                String fb = b.getFacebook() != null ? b.getFacebook() : "";
                cmp = fa.compareToIgnoreCase(fb);
            } else if ("Điểm tích lũy ban đầu".equalsIgnoreCase(currentSortField)) {
                cmp = Double.compare(a.getDiemTichLuyBanDau(), b.getDiemTichLuyBanDau());
            } else if ("Ghi chú".equalsIgnoreCase(currentSortField)) {
                String ga = a.getGhiChu() != null ? a.getGhiChu() : "";
                String gb = b.getGhiChu() != null ? b.getGhiChu() : "";
                cmp = ga.compareToIgnoreCase(gb);
            }
            return currentSortAsc ? cmp : -cmp;
        });
    }

    private void renderTableData() {
        tableModel.setRowCount(0);
        DecimalFormat dfNumber = new DecimalFormat("#,##0.##");
        int idx = 1;
        for (KhachHang kh : currentList) {
            String dob = "";
            if (kh.getNgaySinhNhat() != null) {
                dob = sdfDate.format(new Date(kh.getNgaySinhNhat().getTime()));
            }
            List<Object> row = new ArrayList<>();
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                String col = tableModel.getColumnName(c);
                if (col == null || col.isEmpty()) {
                    row.add(String.format("%02d", idx));
                } else if ("Mã khách".equals(col)) {
                    row.add(kh.getMaKhach() != null ? kh.getMaKhach() : "");
                } else if ("Tên khách hàng".equals(col)) {
                    row.add(kh.getName() != null ? kh.getName() : "");
                } else if ("Địa chỉ".equals(col)) {
                    row.add(kh.getDiaChi() != null ? kh.getDiaChi() : "");
                } else if ("Điện thoại".equals(col)) {
                    row.add(kh.getDienThoai() != null ? kh.getDienThoai() : "");
                } else if ("Email".equals(col)) {
                    row.add(kh.getEmail() != null ? kh.getEmail() : "");
                } else if ("Nhóm khách hàng".equals(col)) {
                    row.add(kh.getTenNhomKhachHang() != null ? kh.getTenNhomKhachHang() : "");
                } else if ("Mã số thuế".equals(col)) {
                    row.add(kh.getMaSoThue() != null ? kh.getMaSoThue() : "");
                } else if ("Nhân viên".equals(col)) {
                    row.add(kh.getTenNhanVien() != null ? kh.getTenNhanVien() : "");
                } else if ("Ngày thành lập/sinh nhật".equals(col)) {
                    row.add(dob);
                } else if ("Tỉnh thành".equals(col)) {
                    row.add(kh.getTinhThanh() != null ? kh.getTinhThanh() : "");
                } else if ("Facebook".equals(col)) {
                    row.add(kh.getFacebook() != null ? kh.getFacebook() : "");
                } else if ("Điểm tích lũy ban đầu".equals(col)) {
                    row.add(kh.getDiemTichLuyBanDau() != 0 ? dfNumber.format(kh.getDiemTichLuyBanDau()) : "0");
                } else if ("Ghi chú".equals(col)) {
                    row.add(kh.getGhiChu() != null ? kh.getGhiChu() : "");
                } else {
                    row.add("");
                }
            }
            idx++;
            tableModel.addRow(row.toArray());
        }

        if (tableKhachHang.getRowCount() > 0) {
            tableKhachHang.setRowSelectionInterval(0, 0);
        } else {
            updateBottomInfo();
        }
    }

    private void onSearch() {
        String kw = txtSearch.getText().trim();
        if ("Thùng rác".equals(currentSelectedNhom)) {
            loadTrashTableData(kw);
        } else {
            loadTableData(currentSelectedNhom, currentSelectedNhomId, kw);
        }
    }

    private void resetDetailModel(DefaultTableModel model, List<String> visibleColumns) {
        model.setColumnIdentifiers(detailHeaders(visibleColumns).toArray(new String[0]));
        model.setRowCount(0);
    }

    private String formatDetailDate(Date value) {
        return value != null ? sdfDate.format(value) : "";
    }

    private String textOrEmpty(String value) {
        return value != null ? value : "";
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) return first;
        return textOrEmpty(second);
    }

    private void finishDetailRender(JTable table, int rowCount) {
        setupDetailTableRenderers(table);
        if (rowCount > 0) table.setRowSelectionInterval(0, 0);
    }

    private void renderBaoGiaTable() {
        resetDetailModel(modelBaoGia, visibleColsBaoGia);
        List<String> headers = detailHeaders(visibleColsBaoGia);
        for (BaoGia item : currentBaoGiaList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Số phiếu": row.add(textOrEmpty(item.getName())); break;
                    case "Tên khách": row.add(textOrEmpty(item.getTenKhach())); break;
                    case "Địa chỉ": row.add(textOrEmpty(item.getDiaChi())); break;
                    case "Điện thoại": row.add(textOrEmpty(item.getDienThoai())); break;
                    case "Tiền hàng": row.add(item.getTienHang()); break;
                    case "Tiền giảm giá": row.add(item.getTienGiamGia()); break;
                    case "Tổng cộng": row.add(item.getTongCong()); break;
                    case "Tỉ lệ thuế": row.add(item.getTiLeThue()); break;
                    case "Tiền thuế": row.add(item.getTienThue()); break;
                    case "Phí vận chuyển": row.add(item.getPhiVanChuyen()); break;
                    case "Email": row.add(textOrEmpty(item.getEmail())); break;
                    case "Ghi chú": row.add(textOrEmpty(item.getNote())); break;
                    case "Khách hàng": row.add(textOrEmpty(item.getTenKhach())); break;
                    case "Tỉ lệ giảm giá": row.add(item.getTiLeGiamGia()); break;
                    default: row.add("");
                }
            }
            modelBaoGia.addRow(row.toArray());
        }
        finishDetailRender(tableBaoGia, currentBaoGiaList.size());
    }

    private void renderDatHangTable() {
        resetDetailModel(modelDatHang, visibleColsDatHang);
        List<String> headers = detailHeaders(visibleColsDatHang);
        for (KhachDatHang item : currentDatHangList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Số phiếu": row.add(textOrEmpty(item.getName())); break;
                    case "Tên khách": row.add(textOrEmpty(item.getTenKhach())); break;
                    case "Địa chỉ": row.add(textOrEmpty(item.getDiaChi())); break;
                    case "Điện thoại": row.add(textOrEmpty(item.getDienThoai())); break;
                    case "Email": row.add(textOrEmpty(item.getEmail())); break;
                    case "Tổng cộng": row.add(item.getTongCong()); break;
                    case "Tỉ lệ thuế": row.add(item.getTiLeThue()); break;
                    case "Tiền thuế": row.add(item.getTienThue()); break;
                    case "Phí vận chuyển": row.add(item.getPhiVanChuyen()); break;
                    case "Ghi chú": row.add(textOrEmpty(item.getNote())); break;
                    case "Khách hàng": row.add(textOrEmpty(item.getTenKhach())); break;
                    case "Tỉ lệ giảm giá": row.add(item.getTiLeGiamGia()); break;
                    case "Tiền giảm giá": row.add(item.getTienGiamGia()); break;
                    case "Tiền hàng": row.add(item.getTienHang()); break;
                    default: row.add("");
                }
            }
            modelDatHang.addRow(row.toArray());
        }
        finishDetailRender(tableDatHang, currentDatHangList.size());
    }

    private void renderDonHangTable() {
        resetDetailModel(modelDonHang, visibleColsDonHang);
        List<String> headers = detailHeaders(visibleColsDonHang);
        for (DonHang item : currentDonHangList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Số phiếu": row.add(textOrEmpty(item.getName())); break;
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Khách hàng": row.add(textOrEmpty(item.getTenKhach())); break;
                    case "Tiền hàng": row.add(item.getTienHang()); break;
                    case "Giảm giá": row.add(item.getTienGiamGia()); break;
                    case "Đổi trả": row.add(item.getTienDoiTra()); break;
                    case "Tổng cộng": row.add(item.getTongCong()); break;
                    case "Giờ thanh toán": row.add(textOrEmpty(item.getGioThanhToan())); break;
                    case "Thanh toán bởi": row.add(textOrEmpty(item.getNguoiThanhToan())); break;
                    case "Ghi chú": row.add(textOrEmpty(item.getGhiChu())); break;
                    default: row.add("");
                }
            }
            modelDonHang.addRow(row.toArray());
        }
        finishDetailRender(tableDonHang, currentDonHangList.size());
    }

    private void renderSanXuatTable() {
        resetDetailModel(modelSanXuat, visibleColsSanXuat);
        List<String> headers = detailHeaders(visibleColsSanXuat);
        for (DonHang item : currentSanXuatList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Số phiếu": row.add(textOrEmpty(item.getName())); break;
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Tổng cộng": row.add(item.getTongCong()); break;
                    case "Kho thành phẩm": row.add(textOrEmpty(item.getTenKhoThanhPham())); break;
                    case "Kho vật tư": row.add(textOrEmpty(item.getTenKhoVatTu())); break;
                    case "Bàn": row.add(textOrEmpty(item.getBan())); break;
                    case "Số khách": row.add(item.getSoKhach()); break;
                    case "Đặt hàng nhà cung cấp": row.add(textOrEmpty(item.getSoPhieuDatHangNCC())); break;
                    default: row.add("");
                }
            }
            modelSanXuat.addRow(row.toArray());
        }
        finishDetailRender(tableSanXuat, currentSanXuatList.size());
    }

    private void renderTangGiamDiemTable() {
        resetDetailModel(modelTangGiamDiem, visibleColsTangGiamDiem);
        List<String> headers = detailHeaders(visibleColsTangGiamDiem);
        for (TangGiamDiem item : currentTangGiamDiemList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Số phiếu": row.add(textOrEmpty(item.getSoPhieu())); break;
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Khách hàng": row.add(textOrEmpty(item.getTenKhachHang())); break;
                    case "Điểm tăng": row.add(item.getDiemTang()); break;
                    case "Điểm giảm": row.add(item.getDiemGiam()); break;
                    case "Lý do": row.add(textOrEmpty(item.getLyDo())); break;
                    case "Ghi chú": row.add(textOrEmpty(item.getNote())); break;
                    default: row.add("");
                }
            }
            modelTangGiamDiem.addRow(row.toArray());
        }
        finishDetailRender(tableTangGiamDiem, currentTangGiamDiemList.size());
    }

    private void renderPhieuThuTable() {
        resetDetailModel(modelPhieuThu, visibleColsPhieuThu);
        List<String> headers = detailHeaders(visibleColsPhieuThu);
        for (PhieuThu item : currentPhieuThuList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Số phiếu": row.add(textOrEmpty(item.getSoPhieu())); break;
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Phân loại thu": row.add(textOrEmpty(item.getTenPhanLoai())); break;
                    case "Người nộp": row.add(firstNonBlank(item.getTenDoiTuong(), item.getTenKhachHang())); break;
                    case "Địa chỉ": row.add(textOrEmpty(item.getDiaChi())); break;
                    case "Số tiền": row.add(item.getSoTien()); break;
                    case "Lý do thu": row.add(textOrEmpty(item.getLyDoThu())); break;
                    case "Chứng từ gốc": row.add(textOrEmpty(item.getChungTuGoc())); break;
                    case "Ghi chú": row.add(textOrEmpty(item.getGhiChu())); break;
                    default: row.add("");
                }
            }
            modelPhieuThu.addRow(row.toArray());
        }
        finishDetailRender(tablePhieuThu, currentPhieuThuList.size());
    }

    private void renderPhieuChiTable() {
        resetDetailModel(modelPhieuChi, visibleColsPhieuChi);
        List<String> headers = detailHeaders(visibleColsPhieuChi);
        for (PhieuChi item : currentPhieuChiList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Số phiếu": row.add(textOrEmpty(item.getSoPhieu())); break;
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Phân loại chi": row.add(textOrEmpty(item.getTenPhanLoai())); break;
                    case "Người nhận": row.add(firstNonBlank(item.getTenDoiTuong(), item.getTenKhachHang())); break;
                    case "Địa chỉ": row.add(textOrEmpty(item.getDiaChi())); break;
                    case "Số tiền": row.add(item.getSoTien()); break;
                    case "Lý do chi": row.add(textOrEmpty(item.getLyDoChi())); break;
                    case "Chứng từ gốc": row.add(textOrEmpty(item.getChungTuGoc())); break;
                    case "Ghi chú": row.add(textOrEmpty(item.getGhiChu())); break;
                    default: row.add("");
                }
            }
            modelPhieuChi.addRow(row.toArray());
        }
        finishDetailRender(tablePhieuChi, currentPhieuChiList.size());
    }

    private void renderPhieuThuCongNoTable() {
        resetDetailModel(modelPhieuThuCongNo, visibleColsPhieuThuCongNo);
        List<String> headers = detailHeaders(visibleColsPhieuThuCongNo);
        for (ChiTietCongNoKhachHangEntry item : currentCongNoList) {
            List<Object> row = new ArrayList<>();
            for (String column : headers) {
                switch (column) {
                    case "Số phiếu": row.add(textOrEmpty(item.getSoPhieu())); break;
                    case "Ngày": row.add(formatDetailDate(item.getNgay())); break;
                    case "Số tiền":
                        row.add("DONHANG".equals(item.getLoaiChungTu())
                                ? item.getTongCong() - item.getTienThanhToan()
                                : item.getTienThanhToan());
                        break;
                    case "Diễn giải": row.add(textOrEmpty(item.getDienGiai())); break;
                    case "Loại chứng từ": row.add(textOrEmpty(item.getLoaiChungTu())); break;
                    case "Còn nợ": row.add(item.getLuyKe()); break;
                    default: row.add("");
                }
            }
            modelPhieuThuCongNo.addRow(row.toArray());
        }
        finishDetailRender(tablePhieuThuCongNo, currentCongNoList.size());
    }

    private void loadSelectedCustomerDetails(KhachHang customer) {
        currentBaoGiaList = baoGiaDAO.getDanhSachBaoGia(null, null, null, customer.getId());
        currentDatHangList = khachDatHangDAO.getDanhSachKhachDatHang(null, null, null, customer.getId());
        currentDonHangList = donHangDAO.getDonHangByKhachHang(customer.getId(), customer.getMaKhach());
        currentSanXuatList = donHangDAO.getPhieuSanXuatByKhachHang(customer.getId(), customer.getMaKhach());
        currentTangGiamDiemList = tangGiamDiemDAO.getByKhachHangId(customer.getId());
        currentPhieuThuList = phieuThuDAO.getByKhachHangId(customer.getId());
        currentPhieuChiList = phieuChiDAO.getByKhachHangId(customer.getId());
        currentCongNoList = congNoKhachHangDAO.getChiTietCongNo(customer.getId(), null, null);

        renderBaoGiaTable();
        renderDatHangTable();
        renderDonHangTable();
        renderSanXuatTable();
        renderTangGiamDiemTable();
        renderPhieuThuTable();
        renderPhieuChiTable();
        renderPhieuThuCongNoTable();
    }

    private void clearBottomDetails() {
        currentBaoGiaList = new ArrayList<>();
        currentDatHangList = new ArrayList<>();
        currentDonHangList = new ArrayList<>();
        currentSanXuatList = new ArrayList<>();
        currentTangGiamDiemList = new ArrayList<>();
        currentPhieuThuList = new ArrayList<>();
        currentPhieuChiList = new ArrayList<>();
        currentCongNoList = new ArrayList<>();

        if (modelBaoGia != null) renderBaoGiaTable();
        if (modelDatHang != null) renderDatHangTable();
        if (modelDonHang != null) renderDonHangTable();
        if (modelSanXuat != null) renderSanXuatTable();
        if (modelTangGiamDiem != null) renderTangGiamDiemTable();
        if (modelPhieuThu != null) renderPhieuThuTable();
        if (modelPhieuChi != null) renderPhieuChiTable();
        if (modelPhieuThuCongNo != null) renderPhieuThuCongNoTable();
    }

    private void updateBottomInfo() {
        int selected = tableKhachHang.getSelectedRow();
        if (selected >= 0 && selected < currentList.size()) {
            KhachHang kh = currentList.get(selected);
            lblCreatedTime.setText("Khởi tạo: " + (kh.getTimeCreated() != null ? sdfDateTime.format(kh.getTimeCreated()) : "--"));
            lblCreatedUser.setText("Khởi tạo bởi: " + (kh.getUserCreatedName() != null ? kh.getUserCreatedName() : "--"));
            lblModifiedTime.setText("Sửa đổi gần nhất: " + (kh.getTimeModified() != null ? sdfDateTime.format(kh.getTimeModified()) : "--"));
            lblModifiedUser.setText("Sửa đổi bởi: " + (kh.getUserModifiedName() != null ? kh.getUserModifiedName() : "--"));
            loadSelectedCustomerDetails(kh);
        } else {
            lblCreatedTime.setText("Khởi tạo: --");
            lblCreatedUser.setText("Khởi tạo bởi: --");
            lblModifiedTime.setText("Sửa đổi gần nhất: --");
            lblModifiedUser.setText("Sửa đổi bởi: --");
            clearBottomDetails();
        }
    }

    private void onAddNewCustomer() {
        KhachHang newKh = new KhachHang();
        if (currentSelectedNhomId != null && !"Tất cả".equals(currentSelectedNhom) && !"Chưa thiết lập".equals(currentSelectedNhom)) {
            newKh.setNhomKhachHangId(currentSelectedNhomId);
            newKh.setTenNhomKhachHang(currentSelectedNhom);
        }
        KhachHangEditDialog dialog = new KhachHangEditDialog(this, newKh, () -> {
            loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    public KhachHang getSelectedKhachHang() {
        int selected = tableKhachHang.getSelectedRow();
        if (selected >= 0 && selected < currentList.size()) {
            return currentList.get(selected);
        }
        return null;
    }

    public void setOnCustomerDoubleClickListener(java.util.function.Consumer<KhachHang> listener) {
        tableKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    KhachHang kh = getSelectedKhachHang();
                    if (kh != null && listener != null) {
                        listener.accept(kh);
                    }
                }
            }
        });
    }

    private void onEditSelectedCustomer() {
        int selected = tableKhachHang.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhachHang kh = currentList.get(selected);
        KhachHangEditDialog dialog = new KhachHangEditDialog(this, kh, () -> {
            loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void updateToolbarState() {
        boolean isTrash = "Thùng rác".equals(currentSelectedNhom);
        if (isTrash) {
            if (btnAddKhachHang != null) btnAddKhachHang.setEnabled(false);
            if (btnEdit != null) btnEdit.setEnabled(false);
            if (btnDelete != null) {
                btnDelete.setText("Xóa vĩnh viễn");
                btnDelete.setIcon(IconHelper.getIcon("cross.png", 16, 16));
                btnDelete.setEnabled(true);
            }
            if (btnRestore != null) {
                btnRestore.setVisible(true);
                btnRestore.setEnabled(true);
            }
        } else {
            if (btnAddKhachHang != null) btnAddKhachHang.setEnabled(true);
            if (btnEdit != null) btnEdit.setEnabled(true);
            if (btnDelete != null) {
                btnDelete.setText("Xóa (Del)");
                btnDelete.setIcon(IconHelper.getIcon("cross.png", 16, 16));
                btnDelete.setEnabled(true);
            }
            if (btnRestore != null) {
                btnRestore.setVisible(false);
            }
        }
    }

    private void onDeleteSelectedCustomer() {
        int selected = tableKhachHang.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhachHang kh = currentList.get(selected);
        if ("Thùng rác".equals(currentSelectedNhom)) {
            onDeletePermanentlySelectedCustomer();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn chuyển khách hàng '" + kh.getName() + "' vào thùng rác?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                khachHangDAO.moveToTrash(kh.getId());
                loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
            }
        }
    }

    private void onRestoreSelectedCustomer() {
        int selected = tableKhachHang.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần khôi phục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhachHang kh = currentList.get(selected);
        khachHangDAO.restoreFromTrash(kh.getId());
        loadTrashTableData(txtSearch.getText().trim());
        JOptionPane.showMessageDialog(this, "Đã khôi phục khách hàng '" + kh.getName() + "'!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeletePermanentlySelectedCustomer() {
        int selected = tableKhachHang.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa vĩnh viễn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhachHang kh = currentList.get(selected);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa VĨNH VIỄN khách hàng '" + kh.getName() + "'?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            khachHangDAO.deletePermanently(kh.getId());
            loadTrashTableData(txtSearch.getText().trim());
        }
    }

    private void onAddNhom() {
        NhomKhachHangDialog dialog = new NhomKhachHangDialog(this, new NhomKhachHang(), () -> {
            loadNhomTree();
        });
        dialog.setVisible(true);
    }

    private void onEditNhom() {
        TreePath path = treeNhomKhachHang.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomKhachHang) {
            NhomKhachHang nh = (NhomKhachHang) node.getUserObject();
            if (!nh.isSeparator()) {
                NhomKhachHangDialog dialog = new NhomKhachHangDialog(this, nh, () -> {
                    loadNhomTree(nh.getName());
                });
                dialog.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể sửa nhóm hệ thống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDeleteNhom() {
        TreePath path = treeNhomKhachHang.getSelectionPath();
        if (path == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomKhachHang) {
            NhomKhachHang nh = (NhomKhachHang) node.getUserObject();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn chuyển nhóm '" + nh.getName() + "' vào thùng rác?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                nhomKhachHangDAO.moveToTrash(nh.getId());
                loadNhomTree("Tất cả");
            }
        }
    }

    private void onRenameNhom() {
        TreePath path = treeNhomKhachHang.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm khách hàng cần đổi tên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomKhachHang) {
            NhomKhachHang nh = (NhomKhachHang) node.getUserObject();
            if (nh.isSeparator()) return;

            String currentName = nh.getName() != null ? nh.getName() : "";
            String newName = (String) JOptionPane.showInputDialog(
                    this,
                    "Nhập tên mới cho nhóm khách hàng:",
                    "Đổi tên nhóm",
                    JOptionPane.PLAIN_MESSAGE,
                    IconHelper.getIcon("pencil.png", 32, 32),
                    null,
                    currentName
            );

            if (newName != null) {
                newName = newName.trim();
                if (newName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên không được để trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!newName.equals(currentName)) {
                    nh.setName(newName);
                    nhomKhachHangDAO.update(nh);
                    loadNhomTree(newName);
                    loadTableData(newName, nh.getId(), txtSearch.getText().trim());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể đổi tên nhóm hệ thống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onAddNewFolder(boolean isSubFolder) {
        String input = JOptionPane.showInputDialog(this, "Nhập tên thư mục:", isSubFolder ? "Thêm thư mục con" : "Thêm thư mục", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        NhomKhachHang folder = new NhomKhachHang();
        folder.setName(input.trim());
        folder.setFolder(true);
        folder.setIconName("Folder.png");

        if (isSubFolder) {
            TreePath path = treeNhomKhachHang.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof NhomKhachHang) {
                    NhomKhachHang parentObj = (NhomKhachHang) node.getUserObject();
                    folder.setParentId(parentObj.getId());
                }
            }
        }

        nhomKhachHangDAO.insert(folder);
        loadNhomTree(folder.getName());
    }

    private void showTreePopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        TreePath path = treeNhomKhachHang.getPathForLocation(x, y);
        if (path != null) {
            treeNhomKhachHang.setSelectionPath(path);
        }

        JPopupMenu popup = new JPopupMenu();
        JMenuItem miAddGroup = new JMenuItem("Thêm nhóm khách hàng", IconHelper.getIcon("users.png", 16, 16));
        JMenuItem miAddFolder = new JMenuItem("Thêm thư mục", IconHelper.getIcon("Folder.png", 16, 16));
        JMenuItem miAddSubFolder = new JMenuItem("Thêm thư mục con", IconHelper.getIcon("folder-tree.png", 16, 16));
        JMenuItem miRename = new JMenuItem("Đổi tên", IconHelper.getIcon("pencil.png", 16, 16));
        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        JMenuItem miDelete = new JMenuItem("Chuyển vào thùng rác (Del)", IconHelper.getIcon("cross.png", 16, 16));
        JMenuItem miTrash = new JMenuItem("Mở thùng rác...", IconHelper.getIcon("RecycleBin.png", 16, 16));
        JMenuItem miRefresh = new JMenuItem("Làm mới cây nhóm", IconHelper.getIcon("Refresh.png", 16, 16));

        miAddGroup.addActionListener(ev -> onAddNhom());
        miAddFolder.addActionListener(ev -> onAddNewFolder(false));
        miAddSubFolder.addActionListener(ev -> onAddNewFolder(true));
        miRename.addActionListener(ev -> onRenameNhom());
        miEdit.addActionListener(ev -> onEditNhom());
        miDelete.addActionListener(ev -> onDeleteNhom());
        miTrash.addActionListener(ev -> onOpenNhomTrash());
        miRefresh.addActionListener(ev -> loadNhomTree(currentSelectedNhom));

        popup.add(miAddGroup);
        popup.add(miAddFolder);
        popup.add(miAddSubFolder);
        popup.addSeparator();
        popup.add(miRename);
        popup.add(miEdit);
        popup.add(miDelete);
        popup.addSeparator();
        popup.add(miTrash);
        popup.add(miRefresh);

        popup.show(treeNhomKhachHang, x, y);
    }

    private void onOpenNhomTrash() {
        NhomKhachHangTrashDialog dialog = new NhomKhachHangTrashDialog(this, () -> {
            loadNhomTree(currentSelectedNhom);
        });
        dialog.setVisible(true);
    }

    private void onOpenTrash() {
        KhachHangTrashDialog dialog = new KhachHangTrashDialog(this, () -> {
            if ("Thùng rác".equals(currentSelectedNhom)) {
                loadTrashTableData(txtSearch.getText().trim());
            } else {
                loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
            }
        });
        dialog.setVisible(true);
    }

    private void onExportExcel() {
        KhachHangExcelUtil.exportKhachHangList(this, currentList, visibleColumns, "DanhMuc_KhachHang.xlsx");
    }

    private void onOpenThemNhanhExcel() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ThemNhanhKhachHangDialog dialog = new ThemNhanhKhachHangDialog(owner, () -> {
            loadNhomTree(currentSelectedNhom);
            loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void showTablePopup(MouseEvent e) {
        int row = tableKhachHang.rowAtPoint(e.getPoint());
        if (row >= 0) {
            tableKhachHang.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();
        if ("Thùng rác".equals(currentSelectedNhom)) {
            JMenuItem miRestore = new JMenuItem("Khôi phục", IconHelper.getIcon("Undo.png", 16, 16));
            JMenuItem miDeletePermanent = new JMenuItem("Xóa vĩnh viễn", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới", IconHelper.getIcon("Refresh.png", 16, 16));

            miRestore.addActionListener(ev -> onRestoreSelectedCustomer());
            miDeletePermanent.addActionListener(ev -> onDeletePermanentlySelectedCustomer());
            miExport.addActionListener(ev -> onExportExcel());
            miRefresh.addActionListener(ev -> loadTrashTableData(txtSearch.getText().trim()));

            popup.add(miRestore);
            popup.add(miDeletePermanent);
            popup.addSeparator();
            popup.add(miExport);
            popup.add(miRefresh);
        } else {
            JMenuItem miAdd = new JMenuItem("Thêm khách hàng", IconHelper.getIcon("plus.png", 16, 16));
            JMenuItem miEdit = new JMenuItem("Chỉnh sửa", IconHelper.getIcon("pencil.png", 16, 16));
            JMenuItem miDelete = new JMenuItem("Xóa", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
            JMenuItem miChooseCols = new JMenuItem("Cột hiển thị");
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));
            JMenuItem miPrint = new JMenuItem("In danh sách", IconHelper.getIcon("Preview.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới", IconHelper.getIcon("Refresh.png", 16, 16));

            miAdd.addActionListener(ev -> onAddNewCustomer());
            miEdit.addActionListener(ev -> onEditSelectedCustomer());
            miDelete.addActionListener(ev -> onDeleteSelectedCustomer());
            miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
            miChooseCols.addActionListener(ev -> onOpenColumnChooser());
            miExport.addActionListener(ev -> onExportExcel());
            miPrint.addActionListener(ev -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
            miRefresh.addActionListener(ev -> loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim()));

            popup.add(miAdd);
            popup.add(miEdit);
            popup.add(miDelete);
            popup.addSeparator();
            popup.add(miAutoResize);
            popup.add(miChooseCols);
            popup.addSeparator();
            popup.add(miExport);
            popup.add(miPrint);
            popup.add(miRefresh);
        }

        popup.show(tableKhachHang, e.getX(), e.getY());
    }

    private void showTableHeaderPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm Khách hàng", IconHelper.getIcon("plus.png", 16, 16));
        if (miAdd.getIcon() == null) miAdd.setIcon(IconHelper.getIcon("users.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNewCustomer());
        popup.add(miAdd);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miChooseCols = new JMenuItem("Cột hiển thị");
        miChooseCols.addActionListener(ev -> onOpenColumnChooser());
        popup.add(miChooseCols);

        popup.show(tableKhachHang.getTableHeader(), e.getX(), e.getY());
    }

    public List<String> getDefaultVisibleColumns() {
        List<String> list = new ArrayList<>();
        list.add("Mã khách");
        list.add("Tên khách hàng");
        list.add("Địa chỉ");
        list.add("Điện thoại");
        list.add("Email");
        list.add("Nhóm khách hàng");
        list.add("Mã số thuế");
        list.add("Nhân viên");
        list.add("Ngày thành lập/sinh nhật");
        list.add("Tỉnh thành");
        list.add("Facebook");
        return list;
    }

    public List<String> getAllPossibleColumns() {
        List<String> list = new ArrayList<>();
        list.add("Mã khách");
        list.add("Tên khách hàng");
        list.add("Địa chỉ");
        list.add("Điện thoại");
        list.add("Email");
        list.add("Nhóm khách hàng");
        list.add("Mã số thuế");
        list.add("Nhân viên");
        list.add("Ngày thành lập/sinh nhật");
        list.add("Tỉnh thành");
        list.add("Facebook");
        list.add("Điểm tích lũy ban đầu");
        list.add("Ghi chú");
        return list;
    }

    public List<String> getVisibleColumns() {
        return new ArrayList<>(visibleColumns);
    }

    public void rebuildTableColumns(List<String> newColumns) {
        this.visibleColumns = new ArrayList<>(newColumns);
        List<String> fullColList = new ArrayList<>();
        fullColList.add(""); // 0: Pointer
        fullColList.addAll(visibleColumns);

        String[] cols = fullColList.toArray(new String[0]);
        tableModel.setColumnIdentifiers(cols);

        setupTableRenderers();
        renderTableData();
    }

    public void autoResizeColumnWidths() {
        for (int column = 0; column < tableKhachHang.getColumnCount(); column++) {
            if (column == 0) {
                tableKhachHang.getColumnModel().getColumn(0).setPreferredWidth(26);
                tableKhachHang.getColumnModel().getColumn(0).setMaxWidth(30);
                continue;
            }
            TableColumn tableColumn = tableKhachHang.getColumnModel().getColumn(column);
            int preferredWidth = 50;

            // Chiều rộng của Header
            TableCellRenderer headerRenderer = tableKhachHang.getTableHeader().getDefaultRenderer();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        tableKhachHang, tableColumn.getHeaderValue(), false, false, 0, column);
                if (headerComp != null) {
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
                }
            }

            // Chiều rộng của các dòng dữ liệu (tối đa kiểm tra 100 dòng đầu)
            int rowCount = Math.min(tableKhachHang.getRowCount(), 100);
            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer cellRenderer = tableKhachHang.getCellRenderer(row, column);
                if (cellRenderer != null) {
                    Component c = tableKhachHang.prepareRenderer(cellRenderer, row, column);
                    if (c != null) {
                        int width = c.getPreferredSize().width + tableKhachHang.getIntercellSpacing().width + 16;
                        preferredWidth = Math.max(preferredWidth, width);
                    }
                }
            }

            tableColumn.setPreferredWidth(Math.min(preferredWidth, 350));
        }
        tableKhachHang.getTableHeader().repaint();
    }

    private void onOpenColumnChooser() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChonCotHienThiDialog dialog = new ChonCotHienThiDialog(
                owner,
                visibleColumns,
                getAllPossibleColumns(),
                getDefaultVisibleColumns(),
                this::rebuildTableColumns
        );
        dialog.setVisible(true);
    }

    private void setupShortcuts() {
        registerKeyboardAction(e -> onAddNewCustomer(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> onEditSelectedCustomer(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> onDeleteSelectedCustomer(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> txtSearch.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private static class TreeTransferHandler extends TransferHandler {
        private final NhomKhachHangDAO nhomKhachHangDAO;
        private final Runnable onDataChanged;
        private DefaultMutableTreeNode draggedNode;

        public TreeTransferHandler(NhomKhachHangDAO nhomKhachHangDAO, Runnable onDataChanged) {
            this.nhomKhachHangDAO = nhomKhachHangDAO;
            this.onDataChanged = onDataChanged;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath path = tree.getSelectionPath();
            if (path == null) {
                return null;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node == null || !(node.getUserObject() instanceof NhomKhachHang)) {
                return null;
            }
            draggedNode = node;
            NhomKhachHang nh = (NhomKhachHang) node.getUserObject();
            return new StringSelection(nh.getId());
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop() || !support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }

            support.setShowDropLocation(true);
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            TreePath dropPath = dl.getPath();

            if (dropPath == null) {
                return true;
            }

            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
            if (draggedNode == null || draggedNode == targetNode) {
                return false;
            }

            if (draggedNode.isNodeDescendant(targetNode)) {
                return false;
            }

            return true;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                String draggedId = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                if (draggedId == null || draggedId.trim().isEmpty()) {
                    return false;
                }

                JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
                TreePath dropPath = dl.getPath();

                String newParentId = "";
                if (dropPath != null) {
                    DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
                    Object targetUserObj = targetNode.getUserObject();
                    if (targetUserObj instanceof NhomKhachHang) {
                        NhomKhachHang targetItem = (NhomKhachHang) targetUserObj;
                        if (targetItem.isFolder()) {
                            newParentId = targetItem.getId();
                        } else {
                            newParentId = targetItem.getParentId() != null ? targetItem.getParentId() : "";
                        }
                    }
                }

                boolean ok = nhomKhachHangDAO.updateParentAndOrder(draggedId, newParentId, 0);
                if (ok) {
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                    JTree tree = (JTree) support.getComponent();
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < tree.getRowCount(); i++) {
                            TreePath p = tree.getPathForRow(i);
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
                            if (n.getUserObject() instanceof NhomKhachHang) {
                                NhomKhachHang nh = (NhomKhachHang) n.getUserObject();
                                if (draggedId.equals(nh.getId())) {
                                    tree.setSelectionPath(p);
                                    tree.scrollPathToVisible(p);
                                    break;
                                }
                            }
                        }
                    });
                    return true;
                }
            } catch (Exception ignored) {}
            return false;
        }
    }
}
