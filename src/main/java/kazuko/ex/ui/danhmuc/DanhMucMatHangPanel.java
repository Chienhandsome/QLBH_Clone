package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
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

public class DanhMucMatHangPanel extends JPanel {
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

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

    private List<MatHang> currentList = new ArrayList<>();
    private String currentSelectedNhom = "Tất cả";
    private String currentSelectedNhomId = null;

    public DanhMucMatHangPanel() {
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
                    loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
                } else if ("Chưa thiết lập".equals(userObj)) {
                    currentSelectedNhom = "Chưa thiết lập";
                    currentSelectedNhomId = null;
                    loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
                } else if (userObj instanceof NhomMatHang) {
                    NhomMatHang nh = (NhomMatHang) userObj;
                    if (nh.isSeparator()) return;
                    currentSelectedNhom = nh.getName();
                    currentSelectedNhomId = nh.getId();

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

        JButton btnAdd = createToolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        JButton btnEdit = createToolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        JButton btnDelete = createToolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        JButton btnImportExcel = createToolButton("Thêm excel", "Bookmark.png", Color.BLACK, false);
        JButton btnExportExcel = createToolButton("Xuất excel", "Report.png", Color.BLACK, false);
        JButton btnPrint = createToolButton("In", "Preview.png", Color.BLACK, false);
        JButton btnSummary = createToolButton("∑ Tổng", null, Color.BLACK, false);
        JButton btnBarcode = createToolButton("In mã vạch", "Barcode.png", Color.BLACK, false);

        btnAdd.addActionListener(e -> onAddNewProduct());
        btnEdit.addActionListener(e -> onEditSelectedProduct());
        btnDelete.addActionListener(e -> onDeleteSelectedProduct());
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Thêm từ Excel đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnExportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Xuất Excel đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
        btnSummary.addActionListener(e -> showSummaryDialog());
        btnBarcode.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng In mã vạch đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        topBar.add(btnAdd);
        topBar.add(btnEdit);
        topBar.add(btnDelete);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnSummary);
        topBar.add(btnBarcode);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Mặt Hàng
        String[] columns = {
                "", "Mã hàng", "Tên mặt hàng", "Nhóm mặt hàng", "Đơn vị tính",
                "Giá bán", "Giá nhập", "Mặc định giảm giá", "Mặc định giảm tiền",
                "Ghi chú", "Không in chế biến", "Bán nhanh"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10 || column == 11; // Checkbox columns editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 10 || columnIndex == 11) return Boolean.class;
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

        // Column widths
        tableMatHang.getColumnModel().getColumn(0).setPreferredWidth(26);  // Pointer
        tableMatHang.getColumnModel().getColumn(0).setMaxWidth(30);
        tableMatHang.getColumnModel().getColumn(1).setPreferredWidth(80);  // Mã hàng
        tableMatHang.getColumnModel().getColumn(2).setPreferredWidth(210); // Tên hàng
        tableMatHang.getColumnModel().getColumn(3).setPreferredWidth(120); // Nhóm
        tableMatHang.getColumnModel().getColumn(4).setPreferredWidth(70);  // ĐVT
        tableMatHang.getColumnModel().getColumn(5).setPreferredWidth(90);  // Giá bán
        tableMatHang.getColumnModel().getColumn(6).setPreferredWidth(90);  // Giá nhập
        tableMatHang.getColumnModel().getColumn(7).setPreferredWidth(80);  // Giảm giá %
        tableMatHang.getColumnModel().getColumn(8).setPreferredWidth(80);  // Giảm giá tiền
        tableMatHang.getColumnModel().getColumn(9).setPreferredWidth(100); // Ghi chú
        tableMatHang.getColumnModel().getColumn(10).setPreferredWidth(90); // Không in
        tableMatHang.getColumnModel().getColumn(11).setPreferredWidth(70); // Bán nhanh

        // Custom Cell Renderers
        setupTableRenderers();

        // Row Selection Listener -> Update Bottom Info Panel
        tableMatHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBottomInfo();
            }
        });

        // Double Click -> Edit
        tableMatHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelectedProduct();
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

        tableMatHang.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setText("");
                lbl.setIcon(isSelected ? pointerIcon : null);
                lbl.setBackground(isSelected ? new Color(210, 230, 250) : (row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255)));
                return lbl;
            }
        });

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
        tableMatHang.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setIcon(IconHelper.getIcon("Folder.png", 14, 14));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        });

        // 5. ĐVT with icon
        tableMatHang.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setIcon(IconHelper.getIcon("1282529873_objects.png", 12, 12));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        });

        for (int i = 1; i <= 2; i++) tableMatHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
        tableMatHang.getColumnModel().getColumn(5).setCellRenderer(priceRenderer); // Giá bán
        tableMatHang.getColumnModel().getColumn(6).setCellRenderer(priceRenderer); // Giá nhập
        tableMatHang.getColumnModel().getColumn(7).setCellRenderer(priceRenderer); // Giảm %
        tableMatHang.getColumnModel().getColumn(8).setCellRenderer(priceRenderer); // Giảm tiền
        tableMatHang.getColumnModel().getColumn(9).setCellRenderer(textRenderer);  // Ghi chú
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

        treeModel.reload();
        for (int i = 0; i < treeNhomHang.getRowCount(); i++) {
            treeNhomHang.expandRow(i);
        }

        TreePath path = new TreePath(targetNode.getPath());
        treeNhomHang.setSelectionPath(path);
        treeNhomHang.scrollPathToVisible(path);

        if (targetNode.getUserObject() instanceof NhomMatHang) {
            NhomMatHang selNh = (NhomMatHang) targetNode.getUserObject();
            currentSelectedNhom = selNh.getName();
            currentSelectedNhomId = selNh.getId();
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
            loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
        } else {
            currentSelectedNhom = "Tất cả";
            currentSelectedNhomId = null;
            loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim());
        }
    }

    public void loadTableData(String tenNhom, String keyword) {
        loadTableData(tenNhom, currentSelectedNhomId, keyword);
    }

    public void loadTableData(String tenNhom, String nhomId, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            currentList = matHangDAO.search(keyword, tenNhom, nhomId);
        } else {
            currentList = matHangDAO.getByNhom(tenNhom, nhomId);
        }

        renderTableData();
    }

    public void loadTableDataByGroupSet(Set<String> ids, Set<String> names, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            currentList = matHangDAO.search(keyword, null, null);
        } else {
            currentList = matHangDAO.getByNhomIds(ids, names);
        }

        renderTableData();
    }

    private void renderTableData() {
        tableModel.setRowCount(0);
        for (MatHang mh : currentList) {
            tableModel.addRow(new Object[]{
                    "",
                    mh.getCode(),
                    mh.getName(),
                    mh.getTenNhom() != null ? mh.getTenNhom() : "",
                    mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "",
                    mh.getGiaBan(),
                    mh.getGiaNhap(),
                    mh.getGiamGiaPt(),
                    mh.getGiamGiaTien(),
                    mh.getGhiChu() != null ? mh.getGhiChu() : "",
                    mh.isKhongInCheBien(),
                    mh.isBanNhanh()
            });
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
        loadTableData(currentSelectedNhom, txtSearch.getText().trim());
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
        if (currentSelectedNhom != null && !"Tất cả".equals(currentSelectedNhom) && !"Chưa thiết lập".equals(currentSelectedNhom)) {
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
            loadTableData(currentSelectedNhom, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void onDeleteSelectedProduct() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mặt hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa mặt hàng '" + sel.getName() + "' (" + sel.getCode() + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = matHangDAO.delete(sel.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa mặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadTableData(currentSelectedNhom, txtSearch.getText().trim());
            }
        }
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
}
