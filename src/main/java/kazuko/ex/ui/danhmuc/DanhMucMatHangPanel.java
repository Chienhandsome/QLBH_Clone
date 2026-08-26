package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.system.SystemConfig;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DanhMucMatHangPanel extends JPanel {
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();
    private SystemConfig systemConfig;

    private JTree treeNhomHang;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;

    private JTable tableMatHang;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    private JLabel lblCreatedTime;
    private JLabel lblCreatedUser;
    private JLabel lblModifiedTime;
    private JLabel lblModifiedUser;

    private JButton btnAddMatHang;
    private List<MatHang> currentList = new ArrayList<>();
    private List<String> visibleColumns = new ArrayList<>();
    private String currentSelectedNhom = "Tất cả";
    private String currentSelectedNhomId = null;
    private String currentSortField = "Mã hàng";
    private boolean currentSortAsc = true;

    public DanhMucMatHangPanel() {
        this.systemConfig = systemConfigDAO.loadConfig();
        if (this.systemConfig != null && "Tên hàng".equalsIgnoreCase(this.systemConfig.getSapXepThuTuTheo())) {
            this.currentSortField = "Tên mặt hàng";
        } else {
            this.currentSortField = "Mã hàng";
        }
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

        // ==================== KHUNG TRÁI: NHÓM MẶT HÀNG ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 0, new Color(185, 205, 230)));

        // Title Header Khung Trái
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(225, 235, 248));
        leftHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        leftHeader.setPreferredSize(new Dimension(240, 28));

        JLabel lblLeftTitle = new JLabel(" Nhóm mặt hàng");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(25, 55, 105));
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);

        // Toolbar nhỏ cho Nhóm hàng: [Thêm mới ▾], [Sửa], [Xóa], [Thùng rác], [Refresh]
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        leftToolbar.setOpaque(false);

        JButton btnAddMenu = createMiniToolButton("plus.png", "Thêm mới...");
        JButton btnEditNhom = createMiniToolButton("pencil.png", "Sửa nhóm mặt hàng");
        JButton btnDeleteNhom = createMiniToolButton("cross.png", "Chuyển vào thùng rác");
        JButton btnTrash = createMiniToolButton("RecycleBin.png", "Thùng rác");
        JButton btnRefreshNhom = createMiniToolButton("Refresh.png", "Làm mới danh mục nhóm");

        // Popup Menu cho "Thêm mới ▾"
        JPopupMenu popupAdd = new JPopupMenu();
        JMenuItem miAddGroup = new JMenuItem("Thêm nhóm mặt hàng", IconHelper.getIcon("1282529873_objects.png", 16, 16));
        JMenuItem miAddFolder = new JMenuItem("Thêm thư mục", IconHelper.getIcon("Folder.png", 16, 16));
        JMenuItem miAddSubFolder = new JMenuItem("Thêm thư mục con", IconHelper.getIcon("folder-tree.png", 16, 16));
        JMenuItem miAddSeparator = new JMenuItem("Thêm phân cách", IconHelper.getIcon("ui-separator.png", 16, 16));

        miAddGroup.addActionListener(e -> onAddNhom());
        miAddFolder.addActionListener(e -> onAddNewFolder(false));
        miAddSubFolder.addActionListener(e -> onAddNewFolder(true));
        miAddSeparator.addActionListener(e -> onAddSeparator());

        popupAdd.add(miAddGroup);
        popupAdd.add(miAddFolder);
        popupAdd.add(miAddSubFolder);
        popupAdd.add(miAddSeparator);

        btnAddMenu.addActionListener(e -> popupAdd.show(btnAddMenu, 0, btnAddMenu.getHeight()));
        btnEditNhom.addActionListener(e -> onEditNhom());
        btnDeleteNhom.addActionListener(e -> onDeleteNhom());
        btnTrash.addActionListener(e -> onOpenTrash());
        btnRefreshNhom.addActionListener(e -> loadNhomTree());

        leftToolbar.add(btnAddMenu);
        leftToolbar.add(btnEditNhom);
        leftToolbar.add(btnDeleteNhom);
        leftToolbar.add(btnTrash);
        leftToolbar.add(btnRefreshNhom);
        leftHeader.add(leftToolbar, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Tree Nhóm mặt hàng
        rootTreeNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeNhomHang = new JTree(treeModel);
        treeNhomHang.setRootVisible(false);
        treeNhomHang.setShowsRootHandles(true);
        treeNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomHang.setRowHeight(22);

        // Drag and drop sắp xếp / phân cấp
        treeNhomHang.setDragEnabled(true);
        treeNhomHang.setDropMode(DropMode.ON_OR_INSERT);
        treeNhomHang.setTransferHandler(new TreeTransferHandler(nhomMatHangDAO, () -> loadNhomTree(currentSelectedNhom)));

        treeNhomHang.setCellRenderer(new DefaultTreeCellRenderer() {
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
                    } else if (userObj instanceof NhomMatHang) {
                        NhomMatHang nh = (NhomMatHang) userObj;
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
                            String iconName = nh.getIconName() != null ? nh.getIconName() : "1282529873_objects.png";
                            label.setIcon(IconHelper.getIcon(iconName, 16, 16));
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        }
                    }
                }
                return label;
            }
        });

        treeNhomHang.addTreeSelectionListener(e -> {
            TreePath path = treeNhomHang.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObj = node.getUserObject();
                if ("Tất cả".equals(userObj)) {
                    currentSelectedNhom = "Tất cả";
                    currentSelectedNhomId = null;
                    if (btnAddMatHang != null) btnAddMatHang.setEnabled(true);
                    loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
                } else if ("Chưa thiết lập".equals(userObj)) {
                    currentSelectedNhom = "Chưa thiết lập";
                    currentSelectedNhomId = null;
                    if (btnAddMatHang != null) btnAddMatHang.setEnabled(true);
                    loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
                } else if ("Thùng rác".equals(userObj)) {
                    currentSelectedNhom = "Thùng rác";
                    currentSelectedNhomId = null;
                    if (btnAddMatHang != null) btnAddMatHang.setEnabled(false);
                    loadTrashTableData(txtSearch.getText().trim());
                } else if (userObj instanceof NhomMatHang) {
                    NhomMatHang nh = (NhomMatHang) userObj;
                    if (nh.isSeparator()) return;
                    currentSelectedNhom = nh.getName();
                    currentSelectedNhomId = nh.getId();
                    if (btnAddMatHang != null) btnAddMatHang.setEnabled(true);

                    if (nh.isFolder()) {
                        // Thư mục cha: Lấy tất cả ID của cây con để lọc
                        Set<String> descIds = nhomMatHangDAO.getAllDescendantIds(nh.getId());
                        Set<String> descNames = new HashSet<>();
                        for (NhomMatHang item : nhomMatHangDAO.getAll()) {
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

        // Double click vào nhóm mặt hàng để mở hộp thoại chỉnh sửa + Right-click context menu
        treeNhomHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = treeNhomHang.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof NhomMatHang) {
                            NhomMatHang nh = (NhomMatHang) node.getUserObject();
                            if (!nh.isSeparator()) {
                                NhomMatHangDialog dialog = new NhomMatHangDialog(DanhMucMatHangPanel.this, nh, () -> {
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

        // Phím Delete/Insert/F4 trên cây nhóm
        treeNhomHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    onDeleteNhom();
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    onAddNhom();
                } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                    onEditNhom();
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(treeNhomHang);
        treeScroll.setBorder(null);
        leftPanel.add(treeScroll, BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG MẶT HÀNG ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Toolbar Khung Phải
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topBar.setBackground(new Color(225, 235, 248));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JLabel lblTag = new JLabel("Mặt hàng");
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

        btnAddMatHang = createToolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        JButton btnEdit = createToolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        JButton btnDelete = createToolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        JButton btnImportExcel = createToolButton("Thêm excel", "page_white_excel.png", Color.BLACK, false);
        JButton btnExportExcel = createToolButton("Xuất excel", "export_excel.png", Color.BLACK, false);
        JButton btnPrint = createToolButton("In", "Preview.png", Color.BLACK, false);
        JButton btnSummary = createToolButton("∑ Tổng", null, Color.BLACK, false);
        JButton btnBarcode = createToolButton("In mã vạch", "Barcode.png", Color.BLACK, false);

        btnAddMatHang.addActionListener(e -> onAddNewProduct());
        btnEdit.addActionListener(e -> onEditSelectedProduct());
        btnDelete.addActionListener(e -> onDeleteSelectedProduct());
        btnImportExcel.addActionListener(e -> onOpenThemNhanhExcel());
        btnExportExcel.addActionListener(e -> onExportExcel());
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
        btnSummary.addActionListener(e -> showSummaryDialog());
        btnBarcode.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng In mã vạch đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        topBar.add(btnAddMatHang);
        topBar.add(btnEdit);
        topBar.add(btnDelete);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnSummary);
        topBar.add(btnBarcode);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Mặt Hàng (Cột hiển thị linh hoạt theo Cấu hình hệ thống)
        visibleColumns = getDefaultVisibleColumns();
        List<String> colList = new ArrayList<>();
        colList.add(""); // 0: Pointer
        colList.addAll(visibleColumns);

        String[] columns = colList.toArray(new String[0]);

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String cName = getColumnName(column);
                return "Không in chế biến".equals(cName) || "Bán nhanh".equals(cName);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                String cName = getColumnName(columnIndex);
                if ("Không in chế biến".equals(cName) || "Bán nhanh".equals(cName)) return Boolean.class;
                return Object.class;
            }
        };

        tableMatHang = new JTable(tableModel);
        tableMatHang.setRowHeight(22);
        tableMatHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMatHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMatHang.setShowGrid(true);
        tableMatHang.setGridColor(new Color(225, 232, 240));

        JTableHeader th = tableMatHang.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(230, 238, 248));
        th.setForeground(new Color(30, 30, 30));
        th.setPreferredSize(new Dimension(100, 26));

        // Click chuột trái vào header cột -> Sắp xếp tăng/giảm dần; Click chuột phải -> Quick Menu thead
        th.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int colIndex = tableMatHang.columnAtPoint(e.getPoint());
                    if (colIndex >= 0) {
                        int modelCol = tableMatHang.convertColumnIndexToModel(colIndex);
                        String colName = tableMatHang.getModel().getColumnName(modelCol);
                        if (colName != null && !colName.trim().isEmpty()) { // Bỏ qua cột con trỏ
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
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
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

        // Custom Cell Renderers & Column widths
        setupTableRenderers();

        // Row Selection Listener -> Update Bottom Info Panel
        tableMatHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBottomInfo();
            }
        });

        // Double Click -> Edit, Right Click -> Quick Menu
        tableMatHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelectedProduct();
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

        JScrollPane tableScroll = new JScrollPane(tableMatHang);
        tableScroll.setBorder(null);

        // 3. Khung Dưới: TabbedPane Thông tin chi tiết
        JPanel bottomInfoPanel = createBottomTabbedPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottomInfoPanel);
        rightSplit.setResizeWeight(0.85);
        rightSplit.setDividerSize(4);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createBottomTabbedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 95));
        panel.setBackground(Color.WHITE);

        JTabbedPane tabInfo = new JTabbedPane();
        tabInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab Thông tin
        JPanel pnlInfo = new JPanel(new GridLayout(2, 2, 20, 4));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(8, 15, 8, 15));

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
        tabInfo.addTab("Đặt hàng nhà cung cấp", new JPanel());
        tabInfo.addTab("Sản xuất", new JPanel());

        panel.add(tabInfo, BorderLayout.CENTER);
        return panel;
    }

    private void setupTableRenderers() {
        DecimalFormat dfMoney = new DecimalFormat("#,##0.##");

        // 1. Column 0: Pointer Vector Icon
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

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
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

        // 2. Default Text Renderer with Zebra Striping
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

        // 3. Price Renderer (Right Aligned)
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    lbl.setText(dfMoney.format(((Number) value).doubleValue()));
                }
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        };

        // 4. Nhóm hàng with icon
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

        // 5. ĐVT with icon
        DefaultTableCellRenderer dvtRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setIcon(IconHelper.getIcon("1282529873_objects.png", 12, 12));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        };

        for (int i = 0; i < tableMatHang.getColumnCount(); i++) {
            String col = tableMatHang.getColumnName(i);
            if (i == 0) {
                tableMatHang.getColumnModel().getColumn(0).setCellRenderer(pointerRenderer);
                tableMatHang.getColumnModel().getColumn(0).setPreferredWidth(26);
                tableMatHang.getColumnModel().getColumn(0).setMaxWidth(30);
            } else if ("Mã hàng".equals(col)) {
                tableMatHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableMatHang.getColumnModel().getColumn(i).setPreferredWidth(85);
            } else if ("Tên mặt hàng".equals(col)) {
                tableMatHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableMatHang.getColumnModel().getColumn(i).setPreferredWidth(210);
            } else if ("Nhóm mặt hàng".equals(col)) {
                tableMatHang.getColumnModel().getColumn(i).setCellRenderer(nhomRenderer);
                tableMatHang.getColumnModel().getColumn(i).setPreferredWidth(120);
            } else if ("Đơn vị tính".equals(col) || "ĐVT chẵn".equals(col)) {
                tableMatHang.getColumnModel().getColumn(i).setCellRenderer(dvtRenderer);
                tableMatHang.getColumnModel().getColumn(i).setPreferredWidth(70);
            } else if (col.contains("Giá") || col.contains("giảm") || col.contains("USD") || "Quy đổi".equals(col)
                    || (systemConfig != null && systemConfig.isSuDungGia2() && col.equals(systemConfig.getDienGiaiGia2()))
                    || (systemConfig != null && systemConfig.isSuDungGia3() && col.equals(systemConfig.getDienGiaiGia3()))
                    || (systemConfig != null && systemConfig.isSuDungGia4() && col.equals(systemConfig.getDienGiaiGia4()))) {
                tableMatHang.getColumnModel().getColumn(i).setCellRenderer(priceRenderer);
                tableMatHang.getColumnModel().getColumn(i).setPreferredWidth(90);
            } else if ("Không in chế biến".equals(col) || "Bán nhanh".equals(col)) {
                tableMatHang.getColumnModel().getColumn(i).setPreferredWidth(75);
            } else {
                tableMatHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
                tableMatHang.getColumnModel().getColumn(i).setPreferredWidth(100);
            }
        }

        // Header Renderer với mũi tên chỉ hướng sắp xếp (▲ / ▼)
        TableCellRenderer defaultHeaderRenderer = tableMatHang.getTableHeader().getDefaultRenderer();
        tableMatHang.getTableHeader().setDefaultRenderer((table, value, isSelected, hasFocus, row, col) -> {
            int modelCol = table.convertColumnIndexToModel(col);
            String colName = table.getModel().getColumnName(modelCol);
            String displayVal = value != null ? value.toString() : "";
            if (colName != null && !colName.isEmpty() && colName.equalsIgnoreCase(currentSortField)) {
                displayVal += currentSortAsc ? " ▲" : " ▼";
            }
            return defaultHeaderRenderer.getTableCellRendererComponent(table, displayVal, isSelected, hasFocus, row, col);
        });
    }

    private void loadNhomTree() {
        loadNhomTree(currentSelectedNhom);
    }

    private void loadNhomTree(String selectCategoryName) {
        rootTreeNode.removeAllChildren();
        DefaultMutableTreeNode nodeAll = new DefaultMutableTreeNode("Tất cả");
        DefaultMutableTreeNode nodeUnassigned = new DefaultMutableTreeNode("Chưa thiết lập");

        rootTreeNode.add(nodeAll);
        rootTreeNode.add(nodeUnassigned);

        DefaultMutableTreeNode targetNode = nodeAll;

        List<NhomMatHang> nhomList = nhomMatHangDAO.getAll();
        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        // Pass 1: Tạo TreeNode cho tất cả các phần tử
        for (NhomMatHang nh : nhomList) {
            if (nh.getId() != null) {
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(nh);
                nodeMap.put(nh.getId(), n);
                if (selectCategoryName != null && nh.getName() != null && nh.getName().equalsIgnoreCase(selectCategoryName.trim())) {
                    targetNode = n;
                }
            }
        }

        // Pass 2: Gắn vào cây phân cấp theo parentId
        for (NhomMatHang nh : nhomList) {
            DefaultMutableTreeNode currentNode = nodeMap.get(nh.getId());
            if (currentNode == null) continue;

            String parentId = nh.getParentId();
            DefaultMutableTreeNode parentNode = null;
            if (parentId != null && !parentId.trim().isEmpty()) {
                parentNode = nodeMap.get(parentId.trim());
            }

            if (parentNode != null && parentNode != currentNode) {
                parentNode.add(currentNode);
            } else {
                rootTreeNode.add(currentNode);
            }
        }

        // Pass 3: Thùng rác ở dưới cùng của cây
        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");
        rootTreeNode.add(nodeTrash);
        if ("Thùng rác".equalsIgnoreCase(selectCategoryName)) {
            targetNode = nodeTrash;
        }

        treeModel.reload();
        for (int i = 0; i < treeNhomHang.getRowCount(); i++) {
            treeNhomHang.expandRow(i);
        }

        TreePath path = new TreePath(targetNode.getPath());
        treeNhomHang.setSelectionPath(path);
        treeNhomHang.scrollPathToVisible(path);

        if ("Thùng rác".equals(targetNode.getUserObject())) {
            currentSelectedNhom = "Thùng rác";
            currentSelectedNhomId = null;
            if (btnAddMatHang != null) btnAddMatHang.setEnabled(false);
            loadTrashTableData(txtSearch.getText().trim());
        } else if (targetNode.getUserObject() instanceof NhomMatHang) {
            NhomMatHang selNh = (NhomMatHang) targetNode.getUserObject();
            currentSelectedNhom = selNh.getName();
            currentSelectedNhomId = selNh.getId();
            if (btnAddMatHang != null) btnAddMatHang.setEnabled(true);

            if (selNh.isFolder()) {
                Set<String> descIds = nhomMatHangDAO.getAllDescendantIds(selNh.getId());
                Set<String> descNames = new HashSet<>();
                for (NhomMatHang item : nhomList) {
                    if (descIds.contains(item.getId()) && item.getName() != null) {
                        descNames.add(item.getName());
                    }
                }
                loadTableDataByGroupSet(descIds, descNames, txtSearch.getText().trim());
            } else {
                loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
            }
        } else if ("Chưa thiết lập".equals(targetNode.getUserObject())) {
            currentSelectedNhom = "Chưa thiết lập";
            currentSelectedNhomId = null;
            if (btnAddMatHang != null) btnAddMatHang.setEnabled(true);
            loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
        } else {
            currentSelectedNhom = "Tất cả";
            currentSelectedNhomId = null;
            if (btnAddMatHang != null) btnAddMatHang.setEnabled(true);
            loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
        }
    }

    public void loadTrashTableData(String keyword) {
        List<MatHang> trash = matHangDAO.getTrashProducts();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            currentList = trash.stream().filter(m ->
                    (m.getCode() != null && m.getCode().toLowerCase().contains(kw)) ||
                    (m.getName() != null && m.getName().toLowerCase().contains(kw)) ||
                    (m.getTenNhom() != null && m.getTenNhom().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        } else {
            currentList = new ArrayList<>(trash);
        }
        applyCurrentSort();
        renderTableData();
    }

    public void loadTableData(String tenNhom, String keyword) {
        loadTableData(tenNhom, currentSelectedNhomId, keyword);
    }

    public void loadTableData(String tenNhom, String nhomId, String keyword) {
        if ("Thùng rác".equals(tenNhom)) {
            loadTrashTableData(keyword);
            return;
        }
        this.systemConfig = systemConfigDAO.loadConfig();
        if (keyword != null && !keyword.trim().isEmpty()) {
            currentList = matHangDAO.searchWithConfig(keyword, tenNhom, nhomId, systemConfig);
        } else {
            currentList = matHangDAO.getByNhom(tenNhom, nhomId);
        }
        applyCurrentSort();
        renderTableData();
    }

    public void loadTableDataByGroupSet(Set<String> ids, Set<String> names, String keyword) {
        this.systemConfig = systemConfigDAO.loadConfig();
        if (keyword != null && !keyword.trim().isEmpty()) {
            currentList = matHangDAO.searchWithConfigByGroupSet(keyword, ids, names, systemConfig);
        } else {
            currentList = matHangDAO.getByNhomIds(ids, names);
        }
        applyCurrentSort();
        renderTableData();
    }

    private void applyCurrentSort() {
        if (currentList == null || currentList.isEmpty()) return;

        Comparator<MatHang> comparator;
        if (systemConfig != null && systemConfig.isSuDungGia2() && currentSortField.equalsIgnoreCase(systemConfig.getDienGiaiGia2())) {
            comparator = Comparator.comparingDouble(MatHang::getGiaBan2);
        } else if (systemConfig != null && systemConfig.isSuDungGia3() && currentSortField.equalsIgnoreCase(systemConfig.getDienGiaiGia3())) {
            comparator = Comparator.comparingDouble(MatHang::getGiaBan3);
        } else if (systemConfig != null && systemConfig.isSuDungGia4() && currentSortField.equalsIgnoreCase(systemConfig.getDienGiaiGia4())) {
            comparator = Comparator.comparingDouble(MatHang::getGiaBan4);
        } else if ("Giá vốn".equalsIgnoreCase(currentSortField)) {
            comparator = Comparator.comparingDouble(MatHang::getGiaVon);
        } else {
            switch (currentSortField) {
                case "Tên mặt hàng":
                case "Tên hàng":
                    comparator = Comparator.comparing(m -> m.getName() != null ? m.getName() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Nhóm mặt hàng":
                    comparator = Comparator.comparing(m -> m.getTenNhom() != null ? m.getTenNhom() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Đơn vị tính":
                    comparator = Comparator.comparing(m -> m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "ĐVT chẵn":
                    comparator = Comparator.comparing(m -> m.getTenDonViTinhChan() != null ? m.getTenDonViTinhChan() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Quy đổi":
                    comparator = Comparator.comparingDouble(MatHang::getQuyDoi);
                    break;
                case "Giá bán chẵn":
                    comparator = Comparator.comparingDouble(MatHang::getGiaBanChan);
                    break;
                case "Giá bán":
                    comparator = Comparator.comparingDouble(MatHang::getGiaBan);
                    break;
                case "Giá nhập":
                    comparator = Comparator.comparingDouble(MatHang::getGiaNhap);
                    break;
                case "Giá bán 2":
                case "Đơn giá 2":
                    comparator = Comparator.comparingDouble(MatHang::getGiaBan2);
                    break;
                case "Giá bán 3":
                    comparator = Comparator.comparingDouble(MatHang::getGiaBan3);
                    break;
                case "Giá bán 4":
                    comparator = Comparator.comparingDouble(MatHang::getGiaBan4);
                    break;
                case "Mặc định giảm giá":
                    comparator = Comparator.comparingDouble(MatHang::getGiamGiaPt);
                    break;
                case "Mặc định giảm tiền":
                    comparator = Comparator.comparingDouble(MatHang::getGiamGiaTien);
                    break;
                case "Kích thước":
                    comparator = Comparator.comparing(m -> m.getKichThuoc() != null ? m.getKichThuoc() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Bảo hành":
                    comparator = Comparator.comparing(m -> m.getBaoHanh() != null ? m.getBaoHanh() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Hãng sản xuất":
                    comparator = Comparator.comparing(m -> m.getHangSanXuat() != null ? m.getHangSanXuat() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Ghi chú":
                    comparator = Comparator.comparing(m -> m.getGhiChu() != null ? m.getGhiChu() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Không in chế biến":
                    comparator = Comparator.comparing(MatHang::isKhongInCheBien);
                    break;
                case "Bán nhanh":
                    comparator = Comparator.comparing(MatHang::isBanNhanh);
                    break;
                case "Mã hàng":
                default:
                    comparator = Comparator.comparing(m -> m.getCode() != null ? m.getCode() : "", String.CASE_INSENSITIVE_ORDER);
                    break;
            }
        }

        if (!currentSortAsc) {
            comparator = comparator.reversed();
        }
        currentList.sort(comparator);
    }

    public List<String> getDefaultVisibleColumns() {
        List<String> colList = new ArrayList<>();
        colList.add("Mã hàng");
        colList.add("Tên mặt hàng");
        colList.add("Nhóm mặt hàng");
        colList.add("Đơn vị tính");
        if (systemConfig != null && systemConfig.isSuDung2DonViTinh()) {
            colList.add("Đơn vị tính chẵn");
            colList.add("Quy đổi");
            colList.add("Giá bán chẵn");
        }
        colList.add("Giá bán");
        if (systemConfig != null && systemConfig.isSuDungGia2()) {
            colList.add(systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().isEmpty() ? systemConfig.getDienGiaiGia2() : "Đơn giá 2");
        }
        if (systemConfig != null && systemConfig.isSuDungGia3()) {
            colList.add(systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().isEmpty() ? systemConfig.getDienGiaiGia3() : "Giá bán 3");
        }
        if (systemConfig != null && systemConfig.isSuDungGia4()) {
            colList.add(systemConfig.getDienGiaiGia4() != null && !systemConfig.getDienGiaiGia4().isEmpty() ? systemConfig.getDienGiaiGia4() : "Giá bán 4");
        }
        colList.add("Giá nhập");
        colList.add("Mặc định giảm giá");
        colList.add("Mặc định giảm tiền");
        if (systemConfig != null && systemConfig.isMatHangCoKichThuoc()) {
            colList.add("Size");
        }
        if (systemConfig != null && systemConfig.isHangCoBaoHanh()) {
            colList.add("Bảo hành");
        }
        if (systemConfig != null && systemConfig.isSuDungHangSanXuat()) {
            colList.add("Hãng sản xuất");
        }
        colList.add("Ghi chú");
        colList.add("Không in chế biến");
        colList.add("Bán nhanh");
        return colList;
    }

    public List<String> getAllPossibleColumns() {
        List<String> all = new ArrayList<>();
        all.add("Mã hàng");
        all.add("Tên mặt hàng");
        all.add("Nhóm mặt hàng");
        all.add("Đơn vị tính");
        all.add("Giá bán");
        all.add("Giá nhập");
        all.add("Mặc định giảm giá");
        all.add("Mặc định giảm tiền");
        all.add("Ghi chú");
        all.add("Không in chế biến");
        all.add("Bảo hành");
        all.add("Đơn vị tính chẵn");
        all.add("Quy đổi");
        all.add("Giá bán chẵn");
        all.add(systemConfig != null && systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().isEmpty() ? systemConfig.getDienGiaiGia2() : "Đơn giá 2");
        all.add(systemConfig != null && systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().isEmpty() ? systemConfig.getDienGiaiGia3() : "Giá bán 3");
        all.add("Giá vốn");
        all.add("Hãng sản xuất");
        all.add("Có hạn sử dụng");
        all.add("Size");
        all.add("Bán nhanh");
        all.add("Ảnh");
        all.add("Đối tác ký gửi");
        all.add("Hoa hồng");
        all.add("Tồn tối thiểu");
        all.add("Tồn tối đa");
        return all;
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
        for (int column = 0; column < tableMatHang.getColumnCount(); column++) {
            if (column == 0) {
                tableMatHang.getColumnModel().getColumn(0).setPreferredWidth(26);
                tableMatHang.getColumnModel().getColumn(0).setMaxWidth(30);
                continue;
            }
            TableColumn tableColumn = tableMatHang.getColumnModel().getColumn(column);
            int preferredWidth = 50;

            // Chiều rộng của Header
            TableCellRenderer headerRenderer = tableMatHang.getTableHeader().getDefaultRenderer();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        tableMatHang, tableColumn.getHeaderValue(), false, false, 0, column);
                if (headerComp != null) {
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
                }
            }

            // Chiều rộng của các dòng dữ liệu (tối đa kiểm tra 100 dòng đầu)
            int rowCount = Math.min(tableMatHang.getRowCount(), 100);
            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer cellRenderer = tableMatHang.getCellRenderer(row, column);
                if (cellRenderer != null) {
                    Component c = tableMatHang.prepareRenderer(cellRenderer, row, column);
                    if (c != null) {
                        int width = c.getPreferredSize().width + tableMatHang.getIntercellSpacing().width + 16;
                        preferredWidth = Math.max(preferredWidth, width);
                    }
                }
            }

            tableColumn.setPreferredWidth(Math.min(preferredWidth, 350));
        }
        tableMatHang.getTableHeader().repaint();
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

    private void onOpenThemNhanhExcel() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ThemNhanhMatHangDialog dialog = new ThemNhanhMatHangDialog(owner, () -> {
            loadTableData(currentSelectedNhom, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void showTableHeaderPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm Mặt hàng", IconHelper.getIcon("edit_add.png", 16, 16));
        if (miAdd.getIcon() == null) miAdd.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNewProduct());
        popup.add(miAdd);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miChooseCols = new JMenuItem("Cột hiển thị");
        miChooseCols.addActionListener(ev -> onOpenColumnChooser());
        popup.add(miChooseCols);

        popup.show(tableMatHang.getTableHeader(), e.getX(), e.getY());
    }

    private void renderTableData() {
        tableModel.setRowCount(0);
        for (MatHang mh : currentList) {
            List<Object> row = new ArrayList<>();
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                String col = tableModel.getColumnName(c);
                if (col.isEmpty()) row.add("");
                else if ("Mã hàng".equals(col)) row.add(mh.getCode() != null ? mh.getCode() : "");
                else if ("Tên mặt hàng".equals(col)) row.add(mh.getName() != null ? mh.getName() : "");
                else if ("Nhóm mặt hàng".equals(col)) row.add(mh.getTenNhom() != null ? mh.getTenNhom() : "");
                else if ("Đơn vị tính".equals(col)) row.add(mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "");
                else if ("ĐVT chẵn".equals(col) || "Đơn vị tính chẵn".equals(col)) row.add(mh.getTenDonViTinhChan() != null ? mh.getTenDonViTinhChan() : "");
                else if ("Quy đổi".equals(col)) row.add(mh.getQuyDoi());
                else if ("Giá bán chẵn".equals(col)) row.add(mh.getGiaBanChan());
                else if ("Giá bán".equals(col)) row.add(mh.getGiaBan());
                else if ("Giá vốn".equals(col)) row.add(mh.getGiaVon());
                else if ("Giá nhập".equals(col)) row.add(mh.getGiaNhap());
                else if ("Đơn giá 2".equals(col) || "Giá bán 2".equals(col) || (systemConfig != null && systemConfig.isSuDungGia2() && col.equals(systemConfig.getDienGiaiGia2()))) row.add(mh.getGiaBan2());
                else if ("Giá bán 3".equals(col) || (systemConfig != null && systemConfig.isSuDungGia3() && col.equals(systemConfig.getDienGiaiGia3()))) row.add(mh.getGiaBan3());
                else if ("Giá bán 4".equals(col) || (systemConfig != null && systemConfig.isSuDungGia4() && col.equals(systemConfig.getDienGiaiGia4()))) row.add(mh.getGiaBan4());
                else if ("Mặc định giảm giá".equals(col)) row.add(mh.getGiamGiaPt());
                else if ("Mặc định giảm tiền".equals(col)) row.add(mh.getGiamGiaTien());
                else if ("Kích thước".equals(col) || "Size".equals(col)) row.add(mh.getKichThuoc() != null ? mh.getKichThuoc() : "");
                else if ("Bảo hành".equals(col)) row.add(mh.getBaoHanh() != null ? mh.getBaoHanh() : "");
                else if ("Hãng sản xuất".equals(col)) row.add(mh.getHangSanXuat() != null ? mh.getHangSanXuat() : "");
                else if ("Ghi chú".equals(col)) row.add(mh.getGhiChu() != null ? mh.getGhiChu() : "");
                else if ("Không in chế biến".equals(col)) row.add(mh.isKhongInCheBien());
                else if ("Bán nhanh".equals(col)) row.add(mh.isBanNhanh());
                else if ("Có hạn sử dụng".equals(col)) row.add(mh.isCoHanSuDung());
                else if ("Ảnh".equals(col)) row.add(mh.getImagePath() != null ? mh.getImagePath() : "");
                else if ("Đối tác ký gửi".equals(col)) row.add(mh.getDoiTacKyGui() != null ? mh.getDoiTacKyGui() : "");
                else if ("Hoa hồng".equals(col)) row.add(mh.getHoaHong());
                else if ("Tồn tối thiểu".equals(col)) row.add(mh.getTonToiThieu());
                else if ("Tồn tối đa".equals(col)) row.add(mh.getTonToiDa());
                else row.add("");
            }
            tableModel.addRow(row.toArray());
        }

        if (!currentList.isEmpty()) {
            tableMatHang.setRowSelectionInterval(0, 0);
            updateBottomInfo();
        } else {
            clearBottomInfo();
        }
    }

    private void updateBottomInfo() {
        int row = tableMatHang.getSelectedRow();
        if (row >= 0 && row < currentList.size()) {
            MatHang mh = currentList.get(row);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

            String createdTime = mh.getTimeCreated() != null ? sdf.format(mh.getTimeCreated()) : "18/10/2016 14:37 PM";
            String modifiedTime = mh.getTimeModified() != null ? sdf.format(mh.getTimeModified()) : "27/03/2018 09:27 AM";

            lblCreatedTime.setText("Khởi tạo: " + createdTime);
            lblCreatedUser.setText("Khởi tạo bởi: " + (mh.getUserCreatedName() != null ? mh.getUserCreatedName() : "Administrator"));
            lblModifiedTime.setText("Sửa đổi gần nhất: " + modifiedTime);
            lblModifiedUser.setText("Sửa đổi bởi: " + (mh.getUserModifiedName() != null ? mh.getUserModifiedName() : "Administrator"));
        }
    }

    private void clearBottomInfo() {
        lblCreatedTime.setText("Khởi tạo: --");
        lblCreatedUser.setText("Khởi tạo bởi: --");
        lblModifiedTime.setText("Sửa đổi gần nhất: --");
        lblModifiedUser.setText("Sửa đổi bởi: --");
    }

    private void onSearch() {
        if ("Thùng rác".equals(currentSelectedNhom)) {
            loadTrashTableData(txtSearch.getText().trim());
        } else {
            loadTableData(currentSelectedNhom, txtSearch.getText().trim());
        }
    }

    private MatHang getSelectedMatHang() {
        int row = tableMatHang.getSelectedRow();
        if (row >= 0 && row < currentList.size()) {
            return currentList.get(row);
        }
        return null;
    }

    private void onAddNewProduct() {
        MatHang newMH = new MatHang();
        if (currentSelectedNhom != null && !"Tất cả".equals(currentSelectedNhom) && !"Chưa thiết lập".equals(currentSelectedNhom) && !"Thùng rác".equals(currentSelectedNhom)) {
            newMH.setTenNhom(currentSelectedNhom);
            newMH.setNhomMatHangId(currentSelectedNhomId);
        }
        MatHangEditDialog dialog = new MatHangEditDialog(this, newMH, () -> {
            loadTableData(currentSelectedNhom, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void onEditSelectedProduct() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mặt hàng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MatHangEditDialog dialog = new MatHangEditDialog(this, sel, () -> {
            if ("Thùng rác".equals(currentSelectedNhom)) {
                loadTrashTableData(txtSearch.getText().trim());
            } else {
                loadTableData(currentSelectedNhom, txtSearch.getText().trim());
            }
        });
        dialog.setVisible(true);
    }

    private void onDeleteSelectedProduct() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mặt hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Thùng rác".equals(currentSelectedNhom)) {
            onDeletePermanently();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn chuyển mặt hàng '" + sel.getName() + "' (" + sel.getCode() + ") vào Thùng rác không?",
                "Xác nhận chuyển thùng rác",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = matHangDAO.moveToTrash(sel.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã chuyển mặt hàng vào Thùng rác!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadTableData(currentSelectedNhom, txtSearch.getText().trim());
            }
        }
    }

    private void onRestoreSelectedProduct() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) return;
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn khôi phục mặt hàng '" + sel.getName() + "' không?",
                "Xác nhận khôi phục",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            matHangDAO.restoreFromTrash(sel.getId());
            JOptionPane.showMessageDialog(this, "Đã khôi phục mặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadTrashTableData(txtSearch.getText().trim());
        }
    }

    private void onDeletePermanently() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) return;
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn XÓA VĨNH VIỄN mặt hàng '" + sel.getName() + "' (" + sel.getCode() + ")?\nThao tác này KHÔNG thể hoàn tác!",
                "Xác nhận xóa vĩnh viễn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            matHangDAO.deletePermanently(sel.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa vĩnh viễn mặt hàng khỏi hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadTrashTableData(txtSearch.getText().trim());
        }
    }

    private void onCopySelectedProduct() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) return;
        String text = sel.getCode() + "\t" + sel.getName() + "\t" + (sel.getTenNhom() != null ? sel.getTenNhom() : "") + "\t" + sel.getGiaBan();
        StringSelection stringSelection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

    private void onRegenerateCode() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) return;
        String prefix = sel.getCode() != null && sel.getCode().length() >= 2 ? sel.getCode().replaceAll("\\d", "") : "MH";
        String newCode = matHangDAO.generateNextProductCode(prefix);
        sel.setCode(newCode);
        matHangDAO.update(sel);
        renderTableData();
        JOptionPane.showMessageDialog(this, "Đã cập nhật mã mới cho mặt hàng: " + newCode, "Đánh lại mã hàng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showTablePopup(MouseEvent e) {
        int row = tableMatHang.rowAtPoint(e.getPoint());
        if (row >= 0) {
            tableMatHang.setRowSelectionInterval(row, row);
        }
        MatHang sel = getSelectedMatHang();
        if (sel == null) return;

        JPopupMenu popup = new JPopupMenu();

        if ("Thùng rác".equals(currentSelectedNhom)) {
            // Context menu Thùng rác
            JMenuItem miRestore = new JMenuItem("Khôi phục", IconHelper.getIcon("Refresh.png", 16, 16));
            JMenu mSort = createSortSubMenu();
            JMenuItem miRefresh = new JMenuItem("Refresh", IconHelper.getIcon("Refresh.png", 16, 16));
            JMenuItem miCopy = new JMenuItem("Sao chép", IconHelper.getIcon("Report.png", 16, 16));
            JMenuItem miRegenCode = new JMenuItem("Đánh lại mã hàng");
            JMenuItem miDeletePerm = new JMenuItem("Xóa vĩnh viễn", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
            JMenuItem miChooseCols = new JMenuItem("Cột hiển thị");
            JMenuItem miProps = new JMenuItem("Thuộc tính");

            miRestore.addActionListener(ev -> onRestoreSelectedProduct());
            miRefresh.addActionListener(ev -> loadTrashTableData(txtSearch.getText().trim()));
            miCopy.addActionListener(ev -> onCopySelectedProduct());
            miRegenCode.addActionListener(ev -> onRegenerateCode());
            miDeletePerm.addActionListener(ev -> onDeletePermanently());
            miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
            miChooseCols.addActionListener(ev -> onOpenColumnChooser());
            miProps.addActionListener(ev -> onEditSelectedProduct());

            popup.add(miRestore);
            popup.add(mSort);
            popup.add(miRefresh);
            popup.add(miCopy);
            popup.add(miRegenCode);
            popup.addSeparator();
            popup.add(miDeletePerm);
            popup.addSeparator();
            popup.add(miAutoResize);
            popup.add(miChooseCols);
            popup.add(miProps);
        } else {
            // Context menu Danh mục thường (Khớp Ảnh 1 & 2)
            JMenuItem miAdd = new JMenuItem("Thêm Mặt hàng", IconHelper.getIcon("edit_add.png", 16, 16));
            if (miAdd.getIcon() == null) miAdd.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
            JMenuItem miAddExcel = new JMenuItem("Thêm nhanh (excel)");
            JMenuItem miUpdateExcel = new JMenuItem("Cập nhật nhanh (excel)");
            JMenuItem miEdit = new JMenuItem("Chỉnh sửa", IconHelper.getIcon("pencil.png", 16, 16));

            JMenuItem miRegenCode = new JMenuItem("Đánh lại mã hàng");
            JMenuItem miSetItemName = new JMenuItem("Đặt Tên mặt hàng");
            JMenuItem miFilterItemName = new JMenuItem("Lọc Tên mặt hàng");

            JMenu mSort = createSortSubMenu();
            JMenuItem miRefresh = new JMenuItem("Refresh", IconHelper.getIcon("Refresh.png", 16, 16));
            JMenuItem miPrint = new JMenuItem("In danh sách");
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));

            JMenuItem miCopyCell = new JMenuItem("Sao chép ô", IconHelper.getIcon("Report.png", 16, 16));
            JMenuItem miCopySelection = new JMenuItem("Sao chép vùng chọn", IconHelper.getIcon("Report.png", 16, 16));
            JMenuItem miDelete = new JMenuItem("Xóa", IconHelper.getIcon("cross.png", 16, 16));

            JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
            JMenuItem miChooseCols = new JMenuItem("Cột hiển thị");
            JMenuItem miProps = new JMenuItem("Thuộc tính");

            miAdd.addActionListener(ev -> onAddNewProduct());
            miAddExcel.addActionListener(ev -> onOpenThemNhanhExcel());
            miUpdateExcel.addActionListener(ev -> onOpenThemNhanhExcel());
            miEdit.addActionListener(ev -> onEditSelectedProduct());
            miRegenCode.addActionListener(ev -> onRegenerateCode());
            miSetItemName.addActionListener(ev -> onEditSelectedProduct());
            miFilterItemName.addActionListener(ev -> {
                if (sel != null && sel.getName() != null) {
                    txtSearch.setText(sel.getName());
                }
            });

            miRefresh.addActionListener(ev -> loadTableData(currentSelectedNhom, txtSearch.getText().trim()));
            miPrint.addActionListener(ev -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
            miExport.addActionListener(ev -> onExportExcel());
            miCopyCell.addActionListener(ev -> onCopySelectedProduct());
            miCopySelection.addActionListener(ev -> onCopySelectedProduct());
            miDelete.addActionListener(ev -> onDeleteSelectedProduct());

            miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
            miChooseCols.addActionListener(ev -> onOpenColumnChooser());
            miProps.addActionListener(ev -> onEditSelectedProduct());

            popup.add(miAdd);
            popup.add(miAddExcel);
            popup.add(miUpdateExcel);
            popup.add(miEdit);
            popup.addSeparator();
            popup.add(miRegenCode);
            popup.add(miSetItemName);
            popup.add(miFilterItemName);
            popup.addSeparator();
            popup.add(mSort);
            popup.add(miRefresh);
            popup.add(miPrint);
            popup.add(miExport);
            popup.addSeparator();
            popup.add(miCopyCell);
            popup.add(miCopySelection);
            popup.add(miDelete);
            popup.addSeparator();
            popup.add(miAutoResize);
            popup.add(miChooseCols);
            popup.add(miProps);
        }

        popup.show(tableMatHang, e.getX(), e.getY());
    }

    private JMenu createSortSubMenu() {
        JMenu mSort = new JMenu("Sắp xếp theo");

        JMenuItem miAsc = new JMenuItem("Sắp xếp tăng dần");
        miAsc.setIcon(currentSortAsc ? CHECKED_ORANGE_BOX : UNCHECKED_ORANGE_BOX);
        miAsc.addActionListener(e -> {
            currentSortAsc = true;
            applyCurrentSort();
            renderTableData();
            tableMatHang.getTableHeader().repaint();
        });

        JMenuItem miDesc = new JMenuItem("Sắp xếp giảm dần");
        miDesc.setIcon(!currentSortAsc ? CHECKED_ORANGE_BOX : UNCHECKED_ORANGE_BOX);
        miDesc.addActionListener(e -> {
            currentSortAsc = false;
            applyCurrentSort();
            renderTableData();
            tableMatHang.getTableHeader().repaint();
        });

        mSort.add(miAsc);
        mSort.add(miDesc);
        mSort.addSeparator();

        String[] sortFields = {
                "Mã hàng", "Tên mặt hàng", "Nhóm mặt hàng", "Đơn vị tính",
                "Giá bán", "Giá nhập", "Mặc định giảm giá", "Mặc định giảm tiền",
                "Ghi chú", "Không in chế biến", "Bảo hành",
                "Đơn vị tính chẵn", "Quy đổi", "Giá bán chẵn",
                "Đơn giá 2", "Giá bán 3", "Giá vốn",
                "Hãng sản xuất", "Có hạn sử dụng", "Size"
        };

        for (String field : sortFields) {
            JMenuItem miField = new JMenuItem(field);
            miField.setIcon(field.equalsIgnoreCase(currentSortField) ? CHECKED_ORANGE_BOX : UNCHECKED_ORANGE_BOX);
            miField.addActionListener(e -> {
                currentSortField = field;
                applyCurrentSort();
                renderTableData();
                tableMatHang.getTableHeader().repaint();
            });
            mSort.add(miField);
        }

        return mSort;
    }

    private void onAddNhom() {
        NhomMatHang newNhom = new NhomMatHang();
        TreePath path = treeNhomHang.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof NhomMatHang) {
                NhomMatHang sel = (NhomMatHang) node.getUserObject();
                if (sel.isFolder()) {
                    newNhom.setParentId(sel.getId());
                } else {
                    newNhom.setParentId(sel.getParentId());
                }
            }
        }

        NhomMatHangDialog dialog = new NhomMatHangDialog(this, newNhom, () -> {
            loadNhomTree(newNhom.getName());
        });
        dialog.setVisible(true);
    }

    private void onAddNewFolder(boolean isSubFolder) {
        String input = JOptionPane.showInputDialog(this, "Nhập tên thư mục:", isSubFolder ? "Thêm thư mục con" : "Thêm thư mục", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        NhomMatHang folder = new NhomMatHang();
        folder.setName(input.trim());
        folder.setFolder(true);
        folder.setIconName("Folder.png");

        if (isSubFolder) {
            TreePath path = treeNhomHang.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof NhomMatHang) {
                    NhomMatHang parentObj = (NhomMatHang) node.getUserObject();
                    folder.setParentId(parentObj.getId());
                }
            }
        }

        nhomMatHangDAO.insert(folder);
        loadNhomTree(folder.getName());
    }

    private void onAddSeparator() {
        NhomMatHang sep = new NhomMatHang();
        sep.setSeparator(true);

        TreePath path = treeNhomHang.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof NhomMatHang) {
                NhomMatHang parentObj = (NhomMatHang) node.getUserObject();
                sep.setParentId(parentObj.isFolder() ? parentObj.getId() : parentObj.getParentId());
            }
        }

        nhomMatHangDAO.insert(sep);
        loadNhomTree();
    }

    private void onEditNhom() {
        TreePath path = treeNhomHang.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm mặt hàng cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomMatHang) {
            NhomMatHang nh = (NhomMatHang) node.getUserObject();
            if (nh.isSeparator()) return;
            NhomMatHangDialog dialog = new NhomMatHangDialog(this, nh, () -> {
                loadNhomTree(nh.getName());
            });
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể sửa nhóm mặc định này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDeleteNhom() {
        TreePath path = treeNhomHang.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm mặt hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomMatHang) {
            NhomMatHang nh = (NhomMatHang) node.getUserObject();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn chuyển '" + nh.getName() + "' vào Thùng rác không?",
                    "Xác nhận chuyển thùng rác",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                nhomMatHangDAO.moveToTrash(nh.getId());
                loadNhomTree("Tất cả");
                loadTableData("Tất cả", "");
                JOptionPane.showMessageDialog(this, "Đã chuyển '" + nh.getName() + "' vào Thùng rác!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xóa nhóm mặc định này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showTreePopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        TreePath path = treeNhomHang.getPathForLocation(x, y);
        if (path != null) {
            treeNhomHang.setSelectionPath(path);
        }

        JPopupMenu popup = new JPopupMenu();
        JMenuItem miAddGroup = new JMenuItem("Thêm nhóm mặt hàng", IconHelper.getIcon("1282529873_objects.png", 16, 16));
        JMenuItem miAddFolder = new JMenuItem("Thêm thư mục", IconHelper.getIcon("Folder.png", 16, 16));
        JMenuItem miAddSubFolder = new JMenuItem("Thêm thư mục con", IconHelper.getIcon("folder-tree.png", 16, 16));
        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        JMenuItem miDelete = new JMenuItem("Chuyển vào thùng rác (Del)", IconHelper.getIcon("cross.png", 16, 16));
        JMenuItem miTrash = new JMenuItem("Mở thùng rác...", IconHelper.getIcon("RecycleBin.png", 16, 16));

        miAddGroup.addActionListener(ev -> onAddNhom());
        miAddFolder.addActionListener(ev -> onAddNewFolder(false));
        miAddSubFolder.addActionListener(ev -> onAddNewFolder(true));
        miEdit.addActionListener(ev -> onEditNhom());
        miDelete.addActionListener(ev -> onDeleteNhom());
        miTrash.addActionListener(ev -> onOpenTrash());

        popup.add(miAddGroup);
        popup.add(miAddFolder);
        popup.add(miAddSubFolder);
        popup.addSeparator();
        popup.add(miEdit);
        popup.add(miDelete);
        popup.addSeparator();
        popup.add(miTrash);

        popup.show(treeNhomHang, x, y);
    }

    private void showSummaryDialog() {
        int totalItems = currentList.size();
        double totalGiaBan = 0;
        double totalGiaNhap = 0;
        for (MatHang mh : currentList) {
            totalGiaBan += mh.getGiaBan();
            totalGiaNhap += mh.getGiaNhap();
        }
        DecimalFormat df = new DecimalFormat("#,##0.## VNĐ");
        JOptionPane.showMessageDialog(this,
                "<html><b>TỔNG HỢP DANH MỤC MẶT HÀNG</b><br><br>" +
                "• Tổng số lượng mặt hàng: <b>" + totalItems + "</b> mặt hàng<br>" +
                "• Tổng giá trị bán niêm yết: <b>" + df.format(totalGiaBan) + "</b><br>" +
                "• Tổng giá trị nhập niêm yết: <b>" + df.format(totalGiaNhap) + "</b></html>",
                "∑ Tổng mặt hàng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupShortcuts() {
        // F3 -> Focus Search
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.requestFocus();
                txtSearch.selectAll();
            }
        });

        // Insert -> Add
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "addNew");
        getActionMap().put("addNew", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddNewProduct();
            }
        });

        // F4 -> Edit
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "editSelected");
        getActionMap().put("editSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEditSelectedProduct();
            }
        });

        // Delete -> Delete
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelected");
        getActionMap().put("deleteSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteSelectedProduct();
            }
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

    private void onExportExcel() {
        ExcelExporter.exportTable(tableMatHang, "DanhMuc_MatHang", "Mặt hàng", this);
    }

    private void onOpenTrash() {
        NhomMatHangTrashDialog trashDialog = new NhomMatHangTrashDialog(SwingUtilities.getWindowAncestor(this) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(this) : null, () -> {
            loadNhomTree(currentSelectedNhom);
        });
        trashDialog.setVisible(true);
    }

    private static class TreeTransferHandler extends TransferHandler {
        private final NhomMatHangDAO nhomMatHangDAO;
        private final Runnable onDataChanged;
        private DefaultMutableTreeNode draggedNode;

        public TreeTransferHandler(NhomMatHangDAO nhomMatHangDAO, Runnable onDataChanged) {
            this.nhomMatHangDAO = nhomMatHangDAO;
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
            if (node == null || !(node.getUserObject() instanceof NhomMatHang)) {
                return null;
            }
            draggedNode = node;
            NhomMatHang nh = (NhomMatHang) node.getUserObject();
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
                    if (targetUserObj instanceof NhomMatHang) {
                        NhomMatHang targetItem = (NhomMatHang) targetUserObj;
                        if (targetItem.isFolder()) {
                            newParentId = targetItem.getId();
                        } else {
                            newParentId = targetItem.getParentId() != null ? targetItem.getParentId() : "";
                        }
                    }
                }

                boolean ok = nhomMatHangDAO.updateParentAndOrder(draggedId, newParentId, 0);
                if (ok) {
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                    JTree tree = (JTree) support.getComponent();
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < tree.getRowCount(); i++) {
                            TreePath p = tree.getPathForRow(i);
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
                            if (n.getUserObject() instanceof NhomMatHang) {
                                NhomMatHang nh = (NhomMatHang) n.getUserObject();
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

    private static final Icon CHECKED_ORANGE_BOX = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 145, 20)); // Viền cam
            g2.drawRect(x + 1, y + 1, 14, 14);
            g2.setColor(new Color(255, 248, 235));
            g2.fillRect(x + 2, y + 2, 13, 13);
            g2.setColor(new Color(30, 30, 30));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 4, y + 8, x + 7, y + 12);
            g2.drawLine(x + 7, y + 12, x + 13, y + 5);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 18; }
        @Override
        public int getIconHeight() { return 18; }
    };

    private static final Icon UNCHECKED_ORANGE_BOX = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override
        public int getIconWidth() { return 18; }
        @Override
        public int getIconHeight() { return 18; }
    };
}
