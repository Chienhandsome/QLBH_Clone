package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.DotKhuyenMaiDAO;
import kazuko.ex.dao.LoaiHinhKhuyenMaiDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.DotKhuyenMai;
import kazuko.ex.model.danhmuc.LoaiHinhKhuyenMai;
import kazuko.ex.util.ExcelExporter;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DanhMucDotKhuyenMaiPanel extends JPanel {
    private final DotKhuyenMaiDAO dotKhuyenMaiDAO = new DotKhuyenMaiDAO();
    private final LoaiHinhKhuyenMaiDAO loaiHinhKhuyenMaiDAO = new LoaiHinhKhuyenMaiDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    // Tree Loại hình khuyến mại
    private JTree treeLoaiHinhKM;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;
    private String currentSelectedLoai = "Tất cả";
    private String currentSelectedLoaiId = null;

    // Table Đợt khuyến mại
    private JTable tableDotKhuyenMai;
    private DefaultTableModel tableModel;
    private List<DotKhuyenMai> currentList = new ArrayList<>();
    private JTextField txtSearch;
    private JButton btnAddDotKM;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRestore;
    private JButton btnSummary;
    private JPanel summaryPanel;
    private boolean isSummaryVisible = false;

    // Bottom info tabs
    private JLabel lblCreatedInfo;
    private JLabel lblModifiedInfo;
    private JTable tableCuaHangApDung;
    private DefaultTableModel modelCuaHangApDung;

    // Sắp xếp
    private String currentSortField = "Tên đợt khuyến mại";
    private boolean currentSortAsc = true;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public DanhMucDotKhuyenMaiPanel() {
        initComponents();
        loadLoaiTree();
        loadTableData("Tất cả", null, "");
        setupShortcuts();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 245, 250));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(220);
        mainSplit.setDividerSize(4);
        mainSplit.setBorder(null);

        // ==================== KHUNG TRÁI: LOẠI HÌNH KHUYẾN MẠI ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 1, new Color(185, 205, 230)));

        // Header Khung Trái
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(225, 235, 248));
        leftHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        leftHeader.setPreferredSize(new Dimension(200, 28));

        JLabel lblLeftTitle = new JLabel("Loại hình khuyến mại");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(30, 60, 110));
        lblLeftTitle.setBorder(new EmptyBorder(0, 8, 0, 0));
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);

        // Toolbar nhỏ cho Loại hình khuyến mãi: Chỉ Sửa, Thùng rác, Làm mới (Không có Thêm/Xóa)
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        leftToolbar.setOpaque(false);

        JButton btnEditLoai = createMiniToolButton("pencil.png", "Sửa loại hình khuyến mãi (F4)");
        JButton btnTrash = createMiniToolButton("RecycleBin.png", "Mở thùng rác");
        JButton btnRefreshLoai = createMiniToolButton("Refresh.png", "Làm mới danh mục loại hình");

        btnEditLoai.addActionListener(e -> onEditLoaiHinh());
        btnTrash.addActionListener(e -> onOpenTrash());
        btnRefreshLoai.addActionListener(e -> loadLoaiTree());

        leftToolbar.add(btnEditLoai);
        leftToolbar.add(btnTrash);
        leftToolbar.add(btnRefreshLoai);
        leftHeader.add(leftToolbar, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Tree Loại hình khuyến mại
        rootTreeNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeLoaiHinhKM = new JTree(treeModel);
        treeLoaiHinhKM.setRootVisible(false);
        treeLoaiHinhKM.setShowsRootHandles(true);
        treeLoaiHinhKM.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeLoaiHinhKM.setRowHeight(22);

        treeLoaiHinhKM.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if ("Tất cả".equals(userObj)) {
                        label.setText("Tất cả");
                        label.setIcon(IconHelper.getIcon("globe.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        label.setForeground(new Color(25, 55, 105));
                    } else if ("Thùng rác".equals(userObj)) {
                        label.setText("Thùng rác");
                        label.setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        label.setForeground(new Color(30, 60, 110));
                    } else if (userObj instanceof LoaiHinhKhuyenMai) {
                        LoaiHinhKhuyenMai l = (LoaiHinhKhuyenMai) userObj;
                        label.setText(l.getName());
                        String iconName = l.getIconName() != null ? l.getIconName() : "asterisk-yellow.png";
                        label.setIcon(IconHelper.getIcon(iconName, 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }
                return label;
            }
        });

        treeLoaiHinhKM.addTreeSelectionListener(e -> {
            TreePath path = treeLoaiHinhKM.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObj = node.getUserObject();
                if ("Tất cả".equals(userObj)) {
                    currentSelectedLoai = "Tất cả";
                    currentSelectedLoaiId = null;
                    updateToolbarState();
                    loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim());
                } else if ("Thùng rác".equals(userObj)) {
                    currentSelectedLoai = "Thùng rác";
                    currentSelectedLoaiId = null;
                    updateToolbarState();
                    loadTrashTableData(txtSearch.getText().trim());
                } else if (userObj instanceof LoaiHinhKhuyenMai) {
                    LoaiHinhKhuyenMai l = (LoaiHinhKhuyenMai) userObj;
                    currentSelectedLoai = l.getName();
                    currentSelectedLoaiId = l.getId();
                    updateToolbarState();
                    loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim());
                }
            }
        });

        // Double click để sửa + Right-click context menu
        treeLoaiHinhKM.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    TreePath path = treeLoaiHinhKM.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if ("Thùng rác".equals(node.getUserObject())) {
                            onOpenTrash();
                        } else if (node.getUserObject() instanceof LoaiHinhKhuyenMai) {
                            onEditLoaiHinh();
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

        treeLoaiHinhKM.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F4) {
                    onEditLoaiHinh();
                } else if (e.getKeyCode() == KeyEvent.VK_F2) {
                    onRenameLoaiHinh();
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(treeLoaiHinhKM);
        treeScroll.setBorder(null);
        leftPanel.add(treeScroll, BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG ĐỢT KHUYẾN MẠI ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Toolbar Khung Phải
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topBar.setBackground(new Color(225, 235, 248));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JLabel lblTag = new JLabel("Đợt khuyến mại");
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
            @Override public void insertUpdate(DocumentEvent e) { onSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearch(); }
        });
        topBar.add(txtSearch);

        btnAddDotKM = createToolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        btnAddDotKM.setEnabled(false);
        btnEdit = createToolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        btnDelete = createToolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        btnRestore = createToolButton("Khôi phục", "Undo.png", new Color(0, 100, 180), false);
        btnRestore.setVisible(false);
        JButton btnImportExcel = createToolButton("Thêm excel", "page_white_excel.png", Color.BLACK, false);
        JButton btnExportExcel = createToolButton("Xuất excel", "export_excel.png", Color.BLACK, false);
        JButton btnPrint = createToolButton("In", "Preview.png", Color.BLACK, false);
        btnSummary = createToolButton("∑ Tổng", null, Color.BLACK, false);

        btnAddDotKM.addActionListener(e -> onAddNewPromotion());
        btnEdit.addActionListener(e -> onEditSelectedPromotion());
        btnDelete.addActionListener(e -> onDeleteSelectedPromotion());
        btnRestore.addActionListener(e -> onRestoreSelectedPromotion());
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Nhập Excel đang hoàn thiện!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnExportExcel.addActionListener(e -> onExportExcel());
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
        btnSummary.addActionListener(e -> toggleTableSummary());

        topBar.add(btnAddDotKM);
        topBar.add(btnEdit);
        topBar.add(btnDelete);
        topBar.add(btnRestore);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnSummary);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Đợt khuyến mại
        String[] columns = {"", "Tên đợt khuyến mại", "Loại hình khuyến mại", "Từ ngày", "Đến ngày", "Ngừng áp dụng", "Ghi chú", "Từ giờ", "Đến giờ"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class;
                return Object.class;
            }
        };

        tableDotKhuyenMai = new JTable(tableModel);
        tableDotKhuyenMai.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableDotKhuyenMai.setRowHeight(22);
        tableDotKhuyenMai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDotKhuyenMai.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDotKhuyenMai.setShowGrid(true);
        tableDotKhuyenMai.setGridColor(new Color(225, 232, 240));

        JTableHeader th = tableDotKhuyenMai.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(230, 238, 248));
        th.setForeground(new Color(30, 30, 30));
        th.setPreferredSize(new Dimension(100, 26));

        setupTableRenderers();

        // Sort on header click
        th.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int colIndex = tableDotKhuyenMai.columnAtPoint(e.getPoint());
                    if (colIndex >= 0) {
                        int modelCol = tableDotKhuyenMai.convertColumnIndexToModel(colIndex);
                        String colName = tableDotKhuyenMai.getModel().getColumnName(modelCol);
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

        tableDotKhuyenMai.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBottomInfo();
            }
        });

        tableDotKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelectedPromotion();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableDotKhuyenMai);
        tableScroll.setBorder(null);

        // Summary footer row
        summaryPanel = createTableSummaryPanel(tableScroll);
        summaryPanel.setVisible(false);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(tableScroll, BorderLayout.CENTER);
        tableContainer.add(summaryPanel, BorderLayout.SOUTH);

        // 3. Bottom Panel: Tabs "Thông tin" & "Cửa hàng áp dụng"
        JPanel bottomContainer = createBottomInfoPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableContainer, bottomContainer);
        rightSplit.setDividerLocation(380);
        rightSplit.setDividerSize(4);
        rightSplit.setBorder(null);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void setupTableRenderers() {
        tableDotKhuyenMai.getColumnModel().getColumn(0).setPreferredWidth(30);
        tableDotKhuyenMai.getColumnModel().getColumn(0).setMaxWidth(35);
        tableDotKhuyenMai.getColumnModel().getColumn(1).setPreferredWidth(190);
        tableDotKhuyenMai.getColumnModel().getColumn(2).setPreferredWidth(160);
        tableDotKhuyenMai.getColumnModel().getColumn(3).setPreferredWidth(90);
        tableDotKhuyenMai.getColumnModel().getColumn(4).setPreferredWidth(90);
        tableDotKhuyenMai.getColumnModel().getColumn(5).setPreferredWidth(95);
        tableDotKhuyenMai.getColumnModel().getColumn(6).setPreferredWidth(180);
        tableDotKhuyenMai.getColumnModel().getColumn(7).setPreferredWidth(65);
        tableDotKhuyenMai.getColumnModel().getColumn(8).setPreferredWidth(65);

        // Pointer renderer for col 0
        tableDotKhuyenMai.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(245, 248, 252));
                if (row == table.getSelectedRow()) {
                    lbl.setIcon(IconHelper.getIcon("navigation-000-frame.png", 10, 10));
                } else {
                    lbl.setIcon(null);
                }
                return lbl;
            }
        });

        // Center alignment renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        tableDotKhuyenMai.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableDotKhuyenMai.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableDotKhuyenMai.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        tableDotKhuyenMai.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
    }

    private JPanel createBottomInfoPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 245, 250));
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Thông tin
        JPanel pnlInfo = new JPanel(new GridLayout(2, 2, 10, 6));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblCreatedInfo = new JLabel("Khởi tạo: --");
        lblCreatedInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel lblCreatedBy = new JLabel("Khởi tạo bởi: Administrator");
        lblCreatedBy.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblModifiedInfo = new JLabel("Sửa đổi gần nhất: --");
        lblModifiedInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel lblModifiedBy = new JLabel("Sửa đổi bởi: --");
        lblModifiedBy.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlInfo.add(lblCreatedInfo);
        pnlInfo.add(lblCreatedBy);
        pnlInfo.add(lblModifiedInfo);
        pnlInfo.add(lblModifiedBy);

        // Tab 2: Cửa hàng áp dụng
        JPanel pnlStores = new JPanel(new BorderLayout());
        pnlStores.setBackground(Color.WHITE);

        String[] storeCols = {"Mã cửa hàng", "Tên cửa hàng", "Địa chỉ"};
        modelCuaHangApDung = new DefaultTableModel(storeCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableCuaHangApDung = new JTable(modelCuaHangApDung);
        tableCuaHangApDung.setRowHeight(20);
        tableCuaHangApDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollStores = new JScrollPane(tableCuaHangApDung);
        scrollStores.setBorder(null);
        pnlStores.add(scrollStores, BorderLayout.CENTER);

        tabs.addTab("Thông tin", pnlInfo);
        tabs.addTab("Cửa hàng áp dụng", pnlStores);

        bottomPanel.add(tabs, BorderLayout.CENTER);
        return bottomPanel;
    }

    private JPanel createTableSummaryPanel(JScrollPane tableScroll) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int panelW = getWidth();
                int panelH = getHeight();

                // Background gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 250, 255), 0, panelH, new Color(225, 238, 252));
                g2.setPaint(gp);
                g2.fillRect(0, 0, panelW, panelH);

                g2.setColor(new Color(180, 205, 235));
                g2.drawLine(0, 0, panelW, 0);

                if (tableDotKhuyenMai == null || tableModel == null) {
                    g2.dispose();
                    return;
                }

                int scrollX = 0;
                if (tableScroll != null && tableScroll.getViewport() != null) {
                    scrollX = tableScroll.getViewport().getViewPosition().x;
                }

                DecimalFormat df = new DecimalFormat("#,##0.##");

                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(new Color(20, 20, 20));
                FontMetrics fmBold = g2.getFontMetrics();
                int textY = (panelH - fmBold.getHeight()) / 2 + fmBold.getAscent();
                g2.drawString("TỔNG", 10, textY);

                int viewportW = panelW;
                if (tableScroll != null && tableScroll.getViewport() != null) {
                    viewportW = tableScroll.getViewport().getWidth();
                }
                g2.clipRect(0, 0, viewportW, panelH);

                int colCount = tableDotKhuyenMai.getColumnCount();
                for (int c = 0; c < colCount; c++) {
                    TableColumn tableCol = tableDotKhuyenMai.getColumnModel().getColumn(c);
                    int modelCol = tableCol.getModelIndex();
                    if (modelCol != 7) continue; // Col 7: Tỉ lệ giảm giá

                    double sum = 0;
                    int rowCount = tableModel.getRowCount();
                    for (int r = 0; r < rowCount; r++) {
                        Object val = tableModel.getValueAt(r, modelCol);
                        if (val instanceof Number) {
                            sum += ((Number) val).doubleValue();
                        } else if (val != null) {
                            try {
                                sum += Double.parseDouble(val.toString().replace(",", ".").trim());
                            } catch (Exception ignored) {}
                        }
                    }

                    String sumText = (sum == (long) sum) ? String.format("%,d", (long) sum) : df.format(sum);

                    Rectangle headerRect = tableDotKhuyenMai.getTableHeader() != null ? tableDotKhuyenMai.getTableHeader().getHeaderRect(c) : new Rectangle(0, 0, tableCol.getWidth(), 26);
                    int colX = headerRect.x - scrollX;
                    int colW = headerRect.width;

                    int boxX = colX + 2;
                    int boxY = 3;
                    int boxW = Math.max(10, colW - 4);
                    int boxH = panelH - 6;

                    g2.setColor(Color.WHITE);
                    g2.fillRect(boxX, boxY, boxW, boxH);
                    g2.setColor(new Color(185, 205, 230));
                    g2.drawRect(boxX, boxY, boxW, boxH);

                    g2.setColor(new Color(20, 20, 20));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    int numW = fm.stringWidth(sumText);
                    int numX = boxX + boxW - numW - 4;
                    int numY = boxY + (boxH - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(sumText, numX, numY);
                }

                g2.dispose();
            }
        };

        panel.setPreferredSize(new Dimension(100, 28));
        panel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        tableDotKhuyenMai.getColumnModel().addColumnModelListener(new javax.swing.event.TableColumnModelListener() {
            @Override public void columnAdded(javax.swing.event.TableColumnModelEvent e) { panel.repaint(); }
            @Override public void columnRemoved(javax.swing.event.TableColumnModelEvent e) { panel.repaint(); }
            @Override public void columnMoved(javax.swing.event.TableColumnModelEvent e) { panel.repaint(); }
            @Override public void columnMarginChanged(javax.swing.event.ChangeEvent e) { panel.repaint(); }
            @Override public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {}
        });

        if (tableScroll != null && tableScroll.getViewport() != null) {
            tableScroll.getViewport().addChangeListener(e -> panel.repaint());
        }

        return panel;
    }

    private void toggleTableSummary() {
        isSummaryVisible = !isSummaryVisible;
        summaryPanel.setVisible(isSummaryVisible);
        if (isSummaryVisible) {
            btnSummary.setBackground(new Color(255, 235, 140));
            summaryPanel.repaint();
        } else {
            btnSummary.setBackground(Color.WHITE);
        }
        revalidate();
    }

    private void loadLoaiTree() {
        loadLoaiTree(null);
    }

    private void loadLoaiTree(String selectName) {
        rootTreeNode.removeAllChildren();

        DefaultMutableTreeNode nodeTatCa = new DefaultMutableTreeNode("Tất cả");
        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");

        rootTreeNode.add(nodeTatCa);

        List<LoaiHinhKhuyenMai> list = loaiHinhKhuyenMaiDAO.getAll();
        for (LoaiHinhKhuyenMai l : list) {
            rootTreeNode.add(new DefaultMutableTreeNode(l));
        }

        rootTreeNode.add(nodeTrash);
        treeModel.reload();

        for (int i = 0; i < treeLoaiHinhKM.getRowCount(); i++) {
            treeLoaiHinhKM.expandRow(i);
        }

        if (selectName != null) {
            selectNodeByName(selectName);
        } else {
            treeLoaiHinhKM.setSelectionRow(0);
        }
    }

    private void selectNodeByName(String name) {
        for (int i = 0; i < treeLoaiHinhKM.getRowCount(); i++) {
            TreePath path = treeLoaiHinhKM.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (name.equalsIgnoreCase(node.getUserObject().toString())) {
                treeLoaiHinhKM.setSelectionPath(path);
                break;
            }
        }
    }

    private void loadTableData(String loaiName, String loaiId, String keyword) {
        currentList = dotKhuyenMaiDAO.search(keyword, loaiName, loaiId);
        applyCurrentSort();
        renderTableData();
    }

    private void loadTrashTableData(String keyword) {
        List<DotKhuyenMai> trash = dotKhuyenMaiDAO.getTrashItems();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            trash.removeIf(k -> (k.getName() == null || !k.getName().toLowerCase().contains(kw)) &&
                                (k.getTenLoaiHinhKhuyenMai() == null || !k.getTenLoaiHinhKhuyenMai().toLowerCase().contains(kw)));
        }
        currentList = trash;
        applyCurrentSort();
        renderTableData();
    }

    private void applyCurrentSort() {
        if (currentList == null) return;
        currentList.sort((a, b) -> {
            int cmp = 0;
            if ("Tên đợt khuyến mại".equalsIgnoreCase(currentSortField)) {
                String na = a.getName() != null ? a.getName() : "";
                String nb = b.getName() != null ? b.getName() : "";
                cmp = na.compareToIgnoreCase(nb);
            } else if ("Loại hình khuyến mại".equalsIgnoreCase(currentSortField)) {
                String la = a.getTenLoaiHinhKhuyenMai() != null ? a.getTenLoaiHinhKhuyenMai() : "";
                String lb = b.getTenLoaiHinhKhuyenMai() != null ? b.getTenLoaiHinhKhuyenMai() : "";
                cmp = la.compareToIgnoreCase(lb);
            } else if ("Từ ngày".equalsIgnoreCase(currentSortField)) {
                long ta = a.getTuNgay() != null ? a.getTuNgay().getTime() : 0;
                long tb = b.getTuNgay() != null ? b.getTuNgay().getTime() : 0;
                cmp = Long.compare(ta, tb);
            } else if ("Đến ngày".equalsIgnoreCase(currentSortField)) {
                long da = a.getDenNgay() != null ? a.getDenNgay().getTime() : 0;
                long db = b.getDenNgay() != null ? b.getDenNgay().getTime() : 0;
                cmp = Long.compare(da, db);
            } else if ("Tỉ lệ giảm giá".equalsIgnoreCase(currentSortField)) {
                cmp = Double.compare(a.getTiLeGiamGia(), b.getTiLeGiamGia());
            }
            return currentSortAsc ? cmp : -cmp;
        });
    }

    private void renderTableData() {
        tableModel.setRowCount(0);
        int idx = 1;
        for (DotKhuyenMai km : currentList) {
            String tuNgay = km.getTuNgay() != null ? sdfDate.format(km.getTuNgay()) : "";
            String denNgay = km.getDenNgay() != null ? sdfDate.format(km.getDenNgay()) : "";
            String tuGio = km.getTuGio() != null ? sdfTime.format(km.getTuGio()) : "00:00";
            String denGio = km.getDenGio() != null ? sdfTime.format(km.getDenGio()) : "23:59";

            tableModel.addRow(new Object[]{
                    String.valueOf(idx++),
                    km.getName() != null ? km.getName() : "",
                    km.getTenLoaiHinhKhuyenMai() != null ? km.getTenLoaiHinhKhuyenMai() : "",
                    tuNgay,
                    denNgay,
                    km.isNgungApDung(),
                    km.getNote() != null ? km.getNote() : "",
                    tuGio,
                    denGio
            });
        }
        if (!currentList.isEmpty()) {
            tableDotKhuyenMai.setRowSelectionInterval(0, 0);
        }
        updateBottomInfo();
        if (summaryPanel != null && isSummaryVisible) {
            summaryPanel.repaint();
        }
    }

    private void onSearch() {
        if ("Thùng rác".equals(currentSelectedLoai)) {
            loadTrashTableData(txtSearch.getText().trim());
        } else {
            loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim());
        }
    }

    private void updateBottomInfo() {
        int selected = tableDotKhuyenMai.getSelectedRow();
        if (selected >= 0 && selected < currentList.size()) {
            DotKhuyenMai km = currentList.get(selected);
            String created = km.getTimeCreated() != null ? sdfDateTime.format(km.getTimeCreated()) : "--";
            String modified = km.getTimeModified() != null ? sdfDateTime.format(km.getTimeModified()) : "--";

            lblCreatedInfo.setText("Khởi tạo: " + created);
            lblModifiedInfo.setText("Sửa đổi gần nhất: " + modified);

            // Load applied stores
            List<String> storeIds = dotKhuyenMaiDAO.getStoresForPromotion(km.getId());
            modelCuaHangApDung.setRowCount(0);
            List<CuaHang> allStores = cuaHangDAO.getRealStores();
            for (CuaHang ch : allStores) {
                if (storeIds.contains(ch.getId())) {
                    modelCuaHangApDung.addRow(new Object[]{ch.getId(), ch.getName(), ch.getDiaChi() != null ? ch.getDiaChi() : ""});
                }
            }
        } else {
            lblCreatedInfo.setText("Khởi tạo: --");
            lblModifiedInfo.setText("Sửa đổi gần nhất: --");
            modelCuaHangApDung.setRowCount(0);
        }
    }

    private void onAddNewPromotion() {
        if (currentSelectedLoaiId == null || "Tất cả".equals(currentSelectedLoai) || "Thùng rác".equals(currentSelectedLoai)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một loại hình khuyến mại cụ thể trên cây bên trái trước khi thêm mới!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DotKhuyenMai newKm = new DotKhuyenMai();
        newKm.setdLoaiHinhKhuyenMaiId(currentSelectedLoaiId);
        newKm.setTenLoaiHinhKhuyenMai(currentSelectedLoai);
        DotKhuyenMaiEditDialog dialog = new DotKhuyenMaiEditDialog(this, newKm, () -> {
            loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void onEditSelectedPromotion() {
        int selected = tableDotKhuyenMai.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đợt khuyến mại cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DotKhuyenMai km = currentList.get(selected);
        DotKhuyenMaiEditDialog dialog = new DotKhuyenMaiEditDialog(this, km, () -> {
            loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void updateToolbarState() {
        boolean isTrash = "Thùng rác".equals(currentSelectedLoai);
        if (isTrash) {
            if (btnAddDotKM != null) btnAddDotKM.setEnabled(false);
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
            boolean isLoaiSelected = currentSelectedLoaiId != null && !"Tất cả".equals(currentSelectedLoai);
            if (btnAddDotKM != null) btnAddDotKM.setEnabled(isLoaiSelected);
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

    private void onDeleteSelectedPromotion() {
        int selected = tableDotKhuyenMai.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đợt khuyến mại cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DotKhuyenMai km = currentList.get(selected);

        if ("Thùng rác".equals(currentSelectedLoai)) {
            onDeletePermanentlySelectedPromotion();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn chuyển đợt khuyến mại '" + km.getName() + "' vào thùng rác?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dotKhuyenMaiDAO.moveToTrash(km.getId());
                loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim());
            }
        }
    }

    private void onRestoreSelectedPromotion() {
        int selected = tableDotKhuyenMai.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đợt khuyến mại cần khôi phục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DotKhuyenMai km = currentList.get(selected);
        dotKhuyenMaiDAO.restoreFromTrash(km.getId());
        loadTrashTableData(txtSearch.getText().trim());
        JOptionPane.showMessageDialog(this, "Đã khôi phục đợt khuyến mại '" + km.getName() + "'!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeletePermanentlySelectedPromotion() {
        int selected = tableDotKhuyenMai.getSelectedRow();
        if (selected < 0 || selected >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đợt khuyến mại cần xóa vĩnh viễn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DotKhuyenMai km = currentList.get(selected);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa VĨNH VIỄN đợt khuyến mại '" + km.getName() + "'?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dotKhuyenMaiDAO.deletePermanently(km.getId());
            loadTrashTableData(txtSearch.getText().trim());
        }
    }

    private void onEditLoaiHinh() {
        TreePath path = treeLoaiHinhKM.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại hình khuyến mãi cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof LoaiHinhKhuyenMai) {
            LoaiHinhKhuyenMai l = (LoaiHinhKhuyenMai) node.getUserObject();
            LoaiHinhKhuyenMaiDialog dialog = new LoaiHinhKhuyenMaiDialog(this, l, () -> {
                loadLoaiTree(l.getName());
                loadTableData(l.getName(), l.getId(), txtSearch.getText().trim());
            });
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể sửa mục hệ thống này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onRenameLoaiHinh() {
        TreePath path = treeLoaiHinhKM.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại hình khuyến mãi cần đổi tên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof LoaiHinhKhuyenMai) {
            LoaiHinhKhuyenMai l = (LoaiHinhKhuyenMai) node.getUserObject();
            String currentName = l.getName() != null ? l.getName() : "";
            String newName = (String) JOptionPane.showInputDialog(
                    this,
                    "Nhập tên mới cho loại hình khuyến mãi:",
                    "Đổi tên loại hình khuyến mãi",
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
                    l.setName(newName);
                    loaiHinhKhuyenMaiDAO.update(l);
                    loadLoaiTree(newName);
                    loadTableData(newName, l.getId(), txtSearch.getText().trim());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể đổi tên mục hệ thống này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showTreePopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        TreePath path = treeLoaiHinhKM.getPathForLocation(x, y);
        if (path != null) {
            treeLoaiHinhKM.setSelectionPath(path);
        }

        JPopupMenu popup = new JPopupMenu();
        JMenuItem miRename = new JMenuItem("Đổi tên (F2)", IconHelper.getIcon("pencil.png", 16, 16));
        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        JMenuItem miTrash = new JMenuItem("Mở thùng rác...", IconHelper.getIcon("RecycleBin.png", 16, 16));
        JMenuItem miRefresh = new JMenuItem("Làm mới cây nhóm", IconHelper.getIcon("Refresh.png", 16, 16));

        miRename.addActionListener(ev -> onRenameLoaiHinh());
        miEdit.addActionListener(ev -> onEditLoaiHinh());
        miTrash.addActionListener(ev -> onOpenTrash());
        miRefresh.addActionListener(ev -> loadLoaiTree(currentSelectedLoai));

        popup.add(miRename);
        popup.add(miEdit);
        popup.addSeparator();
        popup.add(miTrash);
        popup.add(miRefresh);

        popup.show(treeLoaiHinhKM, x, y);
    }

    private void onOpenTrash() {
        DotKhuyenMaiTrashDialog dialog = new DotKhuyenMaiTrashDialog((Frame) SwingUtilities.getWindowAncestor(this), () -> {
            if ("Thùng rác".equals(currentSelectedLoai)) {
                loadTrashTableData(txtSearch.getText().trim());
            } else {
                loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim());
            }
        });
        dialog.setVisible(true);
    }

    private void onExportExcel() {
        ExcelExporter.exportTable(tableDotKhuyenMai, "DanhMuc_DotKhuyenMai", "Đợt khuyến mại", this);
    }

    private void showTablePopup(MouseEvent e) {
        int row = tableDotKhuyenMai.rowAtPoint(e.getPoint());
        if (row >= 0) {
            tableDotKhuyenMai.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();
        if ("Thùng rác".equals(currentSelectedLoai)) {
            JMenuItem miRestore = new JMenuItem("Khôi phục", IconHelper.getIcon("Undo.png", 16, 16));
            JMenuItem miDeletePermanent = new JMenuItem("Xóa vĩnh viễn", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới", IconHelper.getIcon("Refresh.png", 16, 16));

            miRestore.addActionListener(ev -> onRestoreSelectedPromotion());
            miDeletePermanent.addActionListener(ev -> onDeletePermanentlySelectedPromotion());
            miExport.addActionListener(ev -> onExportExcel());
            miRefresh.addActionListener(ev -> loadTrashTableData(txtSearch.getText().trim()));

            popup.add(miRestore);
            popup.add(miDeletePermanent);
            popup.addSeparator();
            popup.add(miExport);
            popup.add(miRefresh);
        } else {
            JMenuItem miAdd = new JMenuItem("Thêm đợt khuyến mại", IconHelper.getIcon("plus.png", 16, 16));
            JMenuItem miEdit = new JMenuItem("Chỉnh sửa", IconHelper.getIcon("pencil.png", 16, 16));
            JMenuItem miDelete = new JMenuItem("Xóa", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));
            JMenuItem miPrint = new JMenuItem("In danh sách", IconHelper.getIcon("Preview.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới", IconHelper.getIcon("Refresh.png", 16, 16));

            miAdd.addActionListener(ev -> onAddNewPromotion());
            miEdit.addActionListener(ev -> onEditSelectedPromotion());
            miDelete.addActionListener(ev -> onDeleteSelectedPromotion());
            miExport.addActionListener(ev -> onExportExcel());
            miPrint.addActionListener(ev -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
            miRefresh.addActionListener(ev -> loadTableData(currentSelectedLoai, currentSelectedLoaiId, txtSearch.getText().trim()));

            popup.add(miAdd);
            popup.add(miEdit);
            popup.add(miDelete);
            popup.addSeparator();
            popup.add(miExport);
            popup.add(miPrint);
            popup.add(miRefresh);
        }

        popup.show(tableDotKhuyenMai, e.getX(), e.getY());
    }

    private void setupShortcuts() {
        registerKeyboardAction(e -> onAddNewPromotion(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> onEditSelectedPromotion(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> onDeleteSelectedPromotion(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(e -> txtSearch.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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
}
