package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.util.ExcelExporter;
import kazuko.ex.util.IconHelper;

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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DanhMucKhachHangPanel extends JPanel {
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();

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

    private JButton btnAddKhachHang;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRestore;
    private List<KhachHang> currentList = new ArrayList<>();
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
        btnTrash.addActionListener(e -> onOpenTrash());
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
                            onOpenTrash();
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
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Nhập Excel đang hoàn thiện!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
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
        String[] columns = {"", "Mã khách", "Tên khách hàng", "Địa chỉ", "Điện thoại", "Email", "Nhóm khách hàng", "Mã số thuế", "Nhân viên", "Ngày thành lập/sinh nhật"};

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

        // Click sort header
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
        rightSplit.setResizeWeight(0.85);
        rightSplit.setDividerSize(4);
        rightSplit.setBorder(null);

        rightPanel.add(rightSplit, BorderLayout.CENTER);
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createBottomTabbedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(100, 110));

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
        tabInfo.addTab("Báo giá", new JPanel());
        tabInfo.addTab("Đặt hàng", new JPanel());
        tabInfo.addTab("Đơn hàng", new JPanel());
        tabInfo.addTab("Sản xuất", new JPanel());
        tabInfo.addTab("Tăng giảm điểm", new JPanel());
        tabInfo.addTab("Phiếu thu", new JPanel());
        tabInfo.addTab("Phiếu chi", new JPanel());
        tabInfo.addTab("Phiếu thu công nợ", new JPanel());

        panel.add(tabInfo, BorderLayout.CENTER);
        return panel;
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

        for (int i = 0; i < tableKhachHang.getColumnCount(); i++) {
            String col = tableKhachHang.getColumnName(i);
            if (i == 0) {
                tableKhachHang.getColumnModel().getColumn(0).setCellRenderer(pointerRenderer);
                tableKhachHang.getColumnModel().getColumn(0).setPreferredWidth(26);
                tableKhachHang.getColumnModel().getColumn(0).setMaxWidth(30);
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
            } else if ("Nhóm khách hàng".equalsIgnoreCase(currentSortField)) {
                String ga = a.getTenNhomKhachHang() != null ? a.getTenNhomKhachHang() : "";
                String gb = b.getTenNhomKhachHang() != null ? b.getTenNhomKhachHang() : "";
                cmp = ga.compareToIgnoreCase(gb);
            } else if ("Điện thoại".equalsIgnoreCase(currentSortField)) {
                String pa = a.getDienThoai() != null ? a.getDienThoai() : "";
                String pb = b.getDienThoai() != null ? b.getDienThoai() : "";
                cmp = pa.compareToIgnoreCase(pb);
            } else if ("Nhân viên".equalsIgnoreCase(currentSortField)) {
                String sa = a.getTenNhanVien() != null ? a.getTenNhanVien() : "";
                String sb = b.getTenNhanVien() != null ? b.getTenNhanVien() : "";
                cmp = sa.compareToIgnoreCase(sb);
            }
            return currentSortAsc ? cmp : -cmp;
        });
    }

    private void renderTableData() {
        tableModel.setRowCount(0);
        int idx = 1;
        for (KhachHang kh : currentList) {
            String dob = "";
            if (kh.getNgaySinhNhat() != null) {
                dob = sdfDate.format(new Date(kh.getNgaySinhNhat().getTime()));
            }
            tableModel.addRow(new Object[]{
                    String.format("%02d", idx++),
                    kh.getMaKhach() != null ? kh.getMaKhach() : "",
                    kh.getName() != null ? kh.getName() : "",
                    kh.getDiaChi() != null ? kh.getDiaChi() : "",
                    kh.getDienThoai() != null ? kh.getDienThoai() : "",
                    kh.getEmail() != null ? kh.getEmail() : "",
                    kh.getTenNhomKhachHang() != null ? kh.getTenNhomKhachHang() : "",
                    kh.getMaSoThue() != null ? kh.getMaSoThue() : "",
                    kh.getTenNhanVien() != null ? kh.getTenNhanVien() : "",
                    dob
            });
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

    private void updateBottomInfo() {
        int selected = tableKhachHang.getSelectedRow();
        if (selected >= 0 && selected < currentList.size()) {
            KhachHang kh = currentList.get(selected);
            lblCreatedTime.setText("Khởi tạo: " + (kh.getTimeCreated() != null ? sdfDateTime.format(kh.getTimeCreated()) : "--"));
            lblCreatedUser.setText("Khởi tạo bởi: " + (kh.getUserCreatedName() != null ? kh.getUserCreatedName() : "--"));
            lblModifiedTime.setText("Sửa đổi gần nhất: " + (kh.getTimeModified() != null ? sdfDateTime.format(kh.getTimeModified()) : "--"));
            lblModifiedUser.setText("Sửa đổi bởi: " + (kh.getUserModifiedName() != null ? kh.getUserModifiedName() : "--"));
        } else {
            lblCreatedTime.setText("Khởi tạo: --");
            lblCreatedUser.setText("Khởi tạo bởi: --");
            lblModifiedTime.setText("Sửa đổi gần nhất: --");
            lblModifiedUser.setText("Sửa đổi bởi: --");
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
        miTrash.addActionListener(ev -> onOpenTrash());
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
        ExcelExporter.exportTable(tableKhachHang, "DanhMuc_KhachHang", "Khách hàng", this);
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
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));
            JMenuItem miPrint = new JMenuItem("In danh sách", IconHelper.getIcon("Preview.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới", IconHelper.getIcon("Refresh.png", 16, 16));

            miAdd.addActionListener(ev -> onAddNewCustomer());
            miEdit.addActionListener(ev -> onEditSelectedCustomer());
            miDelete.addActionListener(ev -> onDeleteSelectedCustomer());
            miExport.addActionListener(ev -> onExportExcel());
            miPrint.addActionListener(ev -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
            miRefresh.addActionListener(ev -> loadTableData(currentSelectedNhom, currentSelectedNhomId, txtSearch.getText().trim()));

            popup.add(miAdd);
            popup.add(miEdit);
            popup.add(miDelete);
            popup.addSeparator();
            popup.add(miExport);
            popup.add(miPrint);
            popup.add(miRefresh);
        }

        popup.show(tableKhachHang, e.getX(), e.getY());
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
