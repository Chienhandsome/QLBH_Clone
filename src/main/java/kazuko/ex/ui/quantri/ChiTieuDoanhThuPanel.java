package kazuko.ex.ui.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import kazuko.ex.ui.danhmuc.ChonCotHienThiDialog;
import kazuko.ex.util.ChiTieuDoanhThuExcel;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ChiTieuDoanhThuPanel extends JPanel {
    private final ChiTieuDoanhThuDAO dao;
    private List<Group> groups = new ArrayList<>();
    private List<ChiTieuDoanhThu> plans = new ArrayList<>();
    private List<ChiTieuDoanhThu> visible = new ArrayList<>();

    // Keep fields for unit test reflection
    private final JTree tree = new JTree(new DefaultMutableTreeNode("Nhóm chỉ tiêu doanh thu"));
    private final JTextField search = new JTextField(12);
    private DefaultTableModel model;
    private final JTable table;
    private DefaultTableModel detailModel;
    private final JTable details;

    private final JLabel lblCreatedTime = new JLabel("Khởi tạo: --");
    private final JLabel lblCreatedUser = new JLabel("Khởi tạo bởi: --");
    private final JLabel lblModifiedTime = new JLabel("Sửa đổi gần nhất: --");
    private final JLabel lblModifiedUser = new JLabel("Sửa đổi bởi: --");

    private final JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
    private final JLabel lblStatus = new JLabel("  Tổng 0 kế hoạch · Tổng chỉ tiêu 12 tháng: 0");

    private boolean rebuilding;
    private String filter = "ALL";
    private String currentSortField = "Năm";
    private boolean currentSortAsc = false;

    private JButton btnAdd;
    private final JButton edit;
    private final JButton delete;
    private final JButton restore;
    private JButton btnImportExcel;
    private JButton btnExportExcel;
    private JButton btnPrint;
    private JButton btnTotal;

    private List<String> visibleColumns = new ArrayList<>(getDefaultVisibleColumns());
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final DecimalFormat dfMoney = new DecimalFormat("#,##0.##");

    public ChiTieuDoanhThuPanel() {
        this(new ChiTieuDoanhThuDAO());
    }

    public ChiTieuDoanhThuPanel(ChiTieuDoanhThuDAO dao) {
        this.dao = dao;

        // Initialize models & tables
        model = createMasterTableModel();
        table = createMasterTable();

        detailModel = createDetailTableModel();
        details = createDetailTable();

        edit = ChiTieuUI.toolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        delete = ChiTieuUI.toolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        restore = ChiTieuUI.toolButton("Khôi phục", "Undo.png", new Color(0, 100, 180), false);
        restore.setVisible(false);

        initUI();
        setupShortcuts();
        reload(null);
    }

    private DefaultTableModel createMasterTableModel() {
        List<String> cols = new ArrayList<>();
        cols.add(""); // Col 0: pointer
        cols.addAll(visibleColumns);
        return new DefaultTableModel(cols.toArray(new String[0]), 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private JTable createMasterTable() {
        JTable t = new JTable(model);
        t.setFont(ChiTieuUI.FONT);
        t.setRowHeight(22);
        t.setShowGrid(true);
        t.setGridColor(ChiTieuUI.COLOR_TABLE_GRID);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader th = t.getTableHeader();
        th.setFont(ChiTieuUI.FONT);
        th.setBackground(ChiTieuUI.COLOR_TABLE_HEADER_BG);
        th.setForeground(new Color(30, 30, 30));
        th.setPreferredSize(new Dimension(100, 26));
        th.setReorderingAllowed(false);

        setupMasterTableRenderers(t);

        th.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int colIndex = t.columnAtPoint(e.getPoint());
                    if (colIndex >= 0) {
                        int modelCol = t.convertColumnIndexToModel(colIndex);
                        String colName = t.getModel().getColumnName(modelCol);
                        if (colName != null && !colName.trim().isEmpty()) {
                            if (colName.equalsIgnoreCase(currentSortField)) {
                                currentSortAsc = !currentSortAsc;
                            } else {
                                currentSortField = colName;
                                currentSortAsc = true;
                            }
                            applySorting();
                            render(selected() != null ? selected().id : null);
                            th.repaint();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    showMasterHeaderPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMasterHeaderPopup(e);
                }
            }
        });

        return t;
    }

    private DefaultTableModel createDetailTableModel() {
        String[] cols = new String[]{"", "Ghi chú", "Chỉ tiêu doanh thu", "Tỉnh thành", "Chỉ tiêu"};
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private JTable createDetailTable() {
        JTable t = new JTable(detailModel);
        t.setFont(ChiTieuUI.FONT);
        t.setRowHeight(22);
        t.setShowGrid(true);
        t.setGridColor(ChiTieuUI.COLOR_TABLE_GRID);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader th = t.getTableHeader();
        th.setFont(ChiTieuUI.FONT);
        th.setBackground(ChiTieuUI.COLOR_TABLE_HEADER_BG);
        th.setForeground(new Color(30, 30, 30));
        th.setPreferredSize(new Dimension(100, 24));
        th.setReorderingAllowed(false);

        setupDetailTableRenderers(t);
        return t;
    }

    private void setupMasterTableRenderers(JTable t) {
        TableCellRenderer customRenderer = new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) defaultRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String colName = tbl.getColumnName(col);

                if (col == 0 || colName.isEmpty()) {
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setText(String.valueOf(row + 1));
                    lbl.setIcon(isSelected ? ChiTieuUI.POINTER_ICON : null);
                } else if ("Năm".equals(colName)) {
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setText(value == null ? "" : value.toString());
                    lbl.setIcon(null);
                } else if ("Phân nhóm".equals(colName) || "Ghi chú".equals(colName)) {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    lbl.setText(value == null ? "" : value.toString());
                    lbl.setIcon(null);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    if (value instanceof Number || value instanceof BigDecimal) {
                        lbl.setText(dfMoney.format(value));
                    } else {
                        lbl.setText(value == null ? "" : value.toString());
                    }
                    lbl.setIcon(null);
                }

                // Authentic coloring matching Screenshots 2 & 4
                if (isSelected) {
                    if (col <= 1) {
                        lbl.setBackground(ChiTieuUI.COLOR_ROW_BROWN); // Brown / Bronze #B85C24
                        lbl.setForeground(Color.WHITE);
                    } else {
                        lbl.setBackground(ChiTieuUI.COLOR_ROW_GREEN); // Green #4E9B28
                        lbl.setForeground(Color.WHITE);
                    }
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : ChiTieuUI.COLOR_TABLE_ROW_ALT);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        };

        for (int i = 0; i < t.getColumnCount(); i++) {
            String colName = t.getColumnName(i);
            TableColumn col = t.getColumnModel().getColumn(i);
            col.setCellRenderer(customRenderer);
            if (i == 0 || colName.isEmpty()) {
                col.setPreferredWidth(32);
                col.setMaxWidth(40);
            } else if ("Năm".equals(colName)) {
                col.setPreferredWidth(65);
            } else if ("Phân nhóm".equals(colName)) {
                col.setPreferredWidth(130);
            } else if ("Ghi chú".equals(colName)) {
                col.setPreferredWidth(140);
            } else {
                col.setPreferredWidth(85);
            }
        }

        // Header renderer with sort arrow
        TableCellRenderer defaultHeaderRenderer = t.getTableHeader().getDefaultRenderer();
        t.getTableHeader().setDefaultRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            Component c = defaultHeaderRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                lbl.setFont(ChiTieuUI.FONT);
                String colTitle = value != null ? value.toString() : "";
                if (colTitle.equalsIgnoreCase(currentSortField)) {
                    lbl.setText(colTitle + (currentSortAsc ? " ▲" : " ▼"));
                    lbl.setForeground(new Color(0, 70, 160));
                } else {
                    lbl.setText(colTitle);
                    lbl.setForeground(new Color(30, 30, 30));
                }
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }
            return c;
        });
    }

    private void setupDetailTableRenderers(JTable t) {
        TableCellRenderer customDetailRenderer = new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) defaultRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String colName = tbl.getColumnName(col);

                if (col == 0 || colName.isEmpty()) {
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setText(String.valueOf(row + 1));
                    lbl.setIcon(isSelected ? ChiTieuUI.POINTER_ICON : null);
                } else if ("Chỉ tiêu doanh thu".equals(colName)) {
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setText(value == null ? "" : value.toString());
                    lbl.setIcon(null);
                } else if ("Chỉ tiêu".equals(colName)) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    if (value instanceof Number || value instanceof BigDecimal) {
                        lbl.setText(dfMoney.format(value));
                    } else {
                        lbl.setText(value == null ? "" : value.toString());
                    }
                    lbl.setIcon(null);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    lbl.setText(value == null ? "" : value.toString());
                    lbl.setIcon(null);
                }

                // Detail row selection matching Screenshots 2 & 4
                if (isSelected) {
                    if (col <= 1) {
                        lbl.setBackground(ChiTieuUI.COLOR_ROW_BROWN);
                        lbl.setForeground(Color.WHITE);
                    } else {
                        lbl.setBackground(ChiTieuUI.COLOR_ROW_LIGHT_GREEN); // Light green #C8E6C9
                        lbl.setForeground(Color.BLACK);
                    }
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : ChiTieuUI.COLOR_TABLE_ROW_ALT);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        };

        for (int i = 0; i < t.getColumnCount(); i++) {
            String colName = t.getColumnName(i);
            TableColumn col = t.getColumnModel().getColumn(i);
            col.setCellRenderer(customDetailRenderer);
            if (i == 0 || colName.isEmpty()) {
                col.setPreferredWidth(32);
                col.setMaxWidth(40);
            } else if ("Ghi chú".equals(colName)) {
                col.setPreferredWidth(160);
            } else if ("Chỉ tiêu doanh thu".equals(colName)) {
                col.setPreferredWidth(120);
            } else if ("Tỉnh thành".equals(colName)) {
                col.setPreferredWidth(160);
            } else if ("Chỉ tiêu".equals(colName)) {
                col.setPreferredWidth(120);
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(ChiTieuUI.COLOR_MAIN_BG);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(230);
        mainSplit.setDividerSize(5);

        // ==================== KHUNG TRÁI: NHÓM CHỈ TIÊU DOANH THU ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 0, new Color(185, 205, 230)));

        // Title Header Khung Trái
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(ChiTieuUI.COLOR_TOOLBAR_BG);
        leftHeader.setBorder(new MatteBorder(0, 0, 1, 0, ChiTieuUI.COLOR_TOOLBAR_BORDER));
        leftHeader.setPreferredSize(new Dimension(240, 28));

        JLabel lblLeftTitle = new JLabel(" Nhóm chỉ tiêu doanh thu");
        lblLeftTitle.setFont(ChiTieuUI.FONT_BOLD);
        lblLeftTitle.setForeground(new Color(25, 55, 105));
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);

        // Toolbar nhỏ cho Nhóm chỉ tiêu (Matching Screenshot 2 & 3)
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        leftToolbar.setOpaque(false);

        JButton btnAddMenu = ChiTieuUI.miniToolButton("plus.png", "Thêm nhóm chỉ tiêu");
        JButton btnEditNhom = ChiTieuUI.miniToolButton("pencil.png", "Sửa nhóm chỉ tiêu");
        JButton btnAddFolder = ChiTieuUI.miniToolButton("Folder.png", "Thêm thư mục");
        JButton btnTrash = ChiTieuUI.miniToolButton("RecycleBin.png", "Thùng rác");
        JButton btnRefreshNhom = ChiTieuUI.miniToolButton("Refresh.png", "Làm mới danh mục nhóm");

        btnAddMenu.addActionListener(e -> editGroup(false, false));
        btnEditNhom.addActionListener(e -> editGroup(true, false));
        btnAddFolder.addActionListener(e -> editGroup(false, true));
        btnTrash.addActionListener(e -> {
            filter = "TRASH";
            reload(null);
        });
        btnRefreshNhom.addActionListener(e -> reload(null));

        leftToolbar.add(btnAddMenu);
        leftToolbar.add(btnEditNhom);
        leftToolbar.add(btnAddFolder);
        leftToolbar.add(btnTrash);
        leftToolbar.add(btnRefreshNhom);
        leftHeader.add(leftToolbar, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Cây nhóm (Matching Screenshot 2 & 3)
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setFont(ChiTieuUI.FONT);
        tree.setRowHeight(22);
        if (!GraphicsEnvironment.isHeadless()) {
            tree.setDragEnabled(true);
        }

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree t, Object v, boolean sel, boolean exp, boolean leaf, int row, boolean focus) {
                JLabel lbl = (JLabel) super.getTreeCellRendererComponent(t, v, sel, exp, leaf, row, focus);
                lbl.setFont(ChiTieuUI.FONT);
                if (v instanceof DefaultMutableTreeNode) {
                    Object item = ((DefaultMutableTreeNode) v).getUserObject();
                    if (item instanceof Filter f) {
                        lbl.setText(f.name());
                        if ("ALL".equals(f.id())) {
                            Icon iconGlobe = IconHelper.getIcon("Globe.png", 16, 16);
                            if (iconGlobe == null) iconGlobe = IconHelper.getIcon("Actions-fill-color-icon.png", 16, 16);
                            lbl.setIcon(iconGlobe != null ? iconGlobe : IconHelper.getIcon("Folder.png", 16, 16));
                            lbl.setFont(ChiTieuUI.FONT_BOLD);
                            lbl.setForeground(new Color(25, 55, 105));
                        } else if ("NONE".equals(f.id())) {
                            Icon iconSun = IconHelper.getIcon("asterisk-yellow.png", 16, 16);
                            if (iconSun == null) iconSun = IconHelper.getIcon("star.png", 16, 16);
                            lbl.setIcon(iconSun != null ? iconSun : IconHelper.getIcon("Folder.png", 16, 16));
                            lbl.setForeground(Color.DARK_GRAY);
                        } else if ("TRASH".equals(f.id())) {
                            lbl.setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                            lbl.setFont(ChiTieuUI.FONT_BOLD);
                            lbl.setForeground(new Color(30, 60, 110));
                        }
                    } else if (item instanceof Group g) {
                        lbl.setText(g.name());
                        if (g.folder()) {
                            lbl.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                            lbl.setFont(ChiTieuUI.FONT_BOLD);
                        } else {
                            lbl.setIcon(null); // Simple text as shown in screenshot (e.g. SALE)
                        }
                    }
                }
                return lbl;
            }
        });

        tree.addTreeSelectionListener(e -> {
            if (rebuilding) return;
            Object value = selectedNode();
            if (value instanceof Group g) filter = g.id();
            else if (value instanceof Filter f) filter = f.id();
            else return;
            updateToolbarState();
            reloadPlans(null);
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    Object val = selectedNode();
                    if (val instanceof Group) {
                        editGroup(true, false);
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

        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object val = selectedNode();
                    if (val instanceof Group g && g.status() >= 0) trashGroup(g, true);
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    editGroup(false, false);
                } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                    editGroup(true, false);
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(null);
        leftPanel.add(treeScroll, BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG KẾ HOẠCH ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Toolbar Khung Phải (Matching Screenshot 2 & 4)
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topBar.setBackground(ChiTieuUI.COLOR_TOOLBAR_BG);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, ChiTieuUI.COLOR_TOOLBAR_BORDER));

        JLabel lblTag = new JLabel("Chỉ tiêu doanh thu");
        lblTag.setFont(ChiTieuUI.FONT_BOLD);
        lblTag.setForeground(new Color(40, 70, 120));
        lblTag.setBorder(new EmptyBorder(0, 4, 0, 6));
        topBar.add(lblTag);

        JLabel lblLoc = new JLabel("Lọc (F3):");
        lblLoc.setFont(ChiTieuUI.FONT);
        topBar.add(lblLoc);

        search.setFont(ChiTieuUI.FONT);
        search.setPreferredSize(new Dimension(130, 24));
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { render(null); }
            public void removeUpdate(DocumentEvent e) { render(null); }
            public void changedUpdate(DocumentEvent e) { render(null); }
        });
        topBar.add(search);

        btnAdd = ChiTieuUI.toolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        btnImportExcel = ChiTieuUI.toolButton("Thêm excel", "page_white_excel.png", Color.BLACK, false);
        btnExportExcel = ChiTieuUI.toolButton("Xuất excel", "export_excel.png", Color.BLACK, false);
        btnPrint = ChiTieuUI.toolButton("In", "Preview.png", Color.BLACK, false);
        btnTotal = ChiTieuUI.toolButton("Σ Tổng", "sum.png", Color.BLACK, false);

        btnAdd.addActionListener(e -> openEditor(false, false));
        edit.addActionListener(e -> openEditor(true, false));
        delete.addActionListener(e -> trashPlan(true));
        restore.addActionListener(e -> trashPlan(false));
        btnImportExcel.addActionListener(e -> importExcel());
        btnExportExcel.addActionListener(e -> exportExcel(false));
        btnPrint.addActionListener(e -> ChiTieuUI.print(this, table, "Chỉ tiêu doanh thu"));
        btnTotal.addActionListener(e -> pnlStatus.setVisible(!pnlStatus.isVisible()));

        topBar.add(btnAdd);
        topBar.add(edit);
        topBar.add(delete);
        topBar.add(restore);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnTotal);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Master Table & Status
        JScrollPane masterScroll = new JScrollPane(table);
        masterScroll.setBorder(null);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !rebuilding) {
                loadDetails();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openEditor(true, false);
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

        pnlStatus.setBackground(new Color(245, 248, 252));
        pnlStatus.setBorder(new MatteBorder(1, 0, 0, 0, new Color(215, 225, 238)));
        lblStatus.setFont(ChiTieuUI.FONT);
        lblStatus.setForeground(new Color(30, 60, 110));
        pnlStatus.add(lblStatus);

        JPanel master = new JPanel(new BorderLayout());
        master.add(masterScroll, BorderLayout.CENTER);
        master.add(pnlStatus, BorderLayout.SOUTH);

        // 3. Bottom Tabs (Matching Screenshot 2 & 4: "Thông tin" and "Chỉ tiêu chi tiết")
        JPanel bottomDetails = createBottomTabbedPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, master, bottomDetails);
        rightSplit.setResizeWeight(0.75);
        rightSplit.setDividerSize(4);
        rightSplit.setBorder(null);

        rightPanel.add(rightSplit, BorderLayout.CENTER);
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createBottomTabbedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(500, 160));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(ChiTieuUI.FONT);

        // Tab 1: Thông tin
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 4));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        lblCreatedTime.setFont(ChiTieuUI.FONT);
        lblCreatedUser.setFont(ChiTieuUI.FONT);
        lblModifiedTime.setFont(ChiTieuUI.FONT);
        lblModifiedUser.setFont(ChiTieuUI.FONT);

        infoPanel.add(lblCreatedTime);
        infoPanel.add(lblCreatedUser);
        infoPanel.add(lblModifiedTime);
        infoPanel.add(lblModifiedUser);
        tabs.addTab("Thông tin", infoPanel);

        // Tab 2: Chỉ tiêu chi tiết (Matching Screenshot 2 & 4)
        JPanel detailContainer = new JPanel(new BorderLayout());
        detailContainer.setBackground(Color.WHITE);
        JScrollPane detailScroll = new JScrollPane(details);
        detailScroll.setBorder(null);
        detailContainer.add(detailScroll, BorderLayout.CENTER);
        tabs.addTab("Chỉ tiêu chi tiết", detailContainer);

        tabs.setSelectedIndex(1);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private void updateToolbarState() {
        boolean isTrashMode = isTrash();
        btnAdd.setEnabled(!isTrashMode);
        edit.setEnabled(!isTrashMode && selected() != null);
        if (isTrashMode) {
            delete.setText("Xóa vĩnh viễn");
            delete.setEnabled(selected() != null);
            restore.setVisible(true);
            restore.setEnabled(selected() != null);
        } else {
            delete.setText("Xóa (Del)");
            delete.setEnabled(selected() != null);
            restore.setVisible(false);
        }
    }

    private record Filter(String id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    public List<String> getDefaultVisibleColumns() {
        List<String> list = new ArrayList<>();
        list.add("Năm");
        list.add("Phân nhóm");
        for (int i = 1; i <= 12; i++) {
            list.add("Tháng " + i);
        }
        return list;
    }

    public List<String> getAllPossibleColumns() {
        List<String> list = new ArrayList<>(getDefaultVisibleColumns());
        list.add("Tổng cộng");
        list.add("Ghi chú");
        return list;
    }

    public void rebuildTableColumns(List<String> newColumns) {
        this.visibleColumns = new ArrayList<>(newColumns);
        List<String> fullColList = new ArrayList<>();
        fullColList.add(""); // 0: Pointer
        fullColList.addAll(visibleColumns);

        String[] cols = fullColList.toArray(new String[0]);
        model.setColumnIdentifiers(cols);

        setupMasterTableRenderers(table);
        render(selected() != null ? selected().id : null);
    }

    public void autoResizeColumnWidths() {
        for (int column = 0; column < table.getColumnCount(); column++) {
            if (column == 0) {
                table.getColumnModel().getColumn(0).setPreferredWidth(32);
                table.getColumnModel().getColumn(0).setMaxWidth(40);
                continue;
            }
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = 50;

            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        table, tableColumn.getHeaderValue(), false, false, 0, column);
                if (headerComp != null) {
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
                }
            }

            int rowCount = Math.min(table.getRowCount(), 100);
            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                if (cellRenderer != null) {
                    Component c = table.prepareRenderer(cellRenderer, row, column);
                    if (c != null) {
                        int width = c.getPreferredSize().width + table.getIntercellSpacing().width + 16;
                        preferredWidth = Math.max(preferredWidth, width);
                    }
                }
            }

            tableColumn.setPreferredWidth(Math.min(preferredWidth, 350));
        }
        table.getTableHeader().repaint();
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

    private void showMasterHeaderPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 16, 16));
        miAdd.addActionListener(ev -> openEditor(false, false));
        popup.add(miAdd);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miChooseCols = new JMenuItem("Cột hiển thị");
        miChooseCols.addActionListener(ev -> onOpenColumnChooser());
        popup.add(miChooseCols);

        popup.show(table.getTableHeader(), e.getX(), e.getY());
    }

    private void showTablePopup(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();
        if (isTrash()) {
            JMenuItem miRestore = new JMenuItem("Khôi phục", IconHelper.getIcon("Undo.png", 16, 16));
            JMenuItem miDeletePermanent = new JMenuItem("Xóa vĩnh viễn", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới", IconHelper.getIcon("Refresh.png", 16, 16));

            miRestore.addActionListener(ev -> trashPlan(false));
            miDeletePermanent.addActionListener(ev -> trashPlan(true));
            miExport.addActionListener(ev -> exportExcel(false));
            miRefresh.addActionListener(ev -> reload(null));

            popup.add(miRestore);
            popup.add(miDeletePermanent);
            popup.addSeparator();
            popup.add(miExport);
            popup.add(miRefresh);
        } else {
            JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 16, 16));
            JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
            JMenuItem miCopy = new JMenuItem("Sao chép", IconHelper.getIcon("Copy.png", 16, 16));
            JMenuItem miDelete = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
            JMenuItem miChooseCols = new JMenuItem("Cột hiển thị");
            JMenuItem miExport = new JMenuItem("Xuất Excel", IconHelper.getIcon("export_excel.png", 16, 16));
            JMenuItem miExportTemplate = new JMenuItem("Tải file Excel mẫu", IconHelper.getIcon("page_white_excel.png", 16, 16));
            JMenuItem miPrint = new JMenuItem("In", IconHelper.getIcon("Preview.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới", IconHelper.getIcon("Refresh.png", 16, 16));

            miAdd.addActionListener(ev -> openEditor(false, false));
            miEdit.addActionListener(ev -> openEditor(true, false));
            miCopy.addActionListener(ev -> openEditor(true, true));
            miDelete.addActionListener(ev -> trashPlan(true));
            miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
            miChooseCols.addActionListener(ev -> onOpenColumnChooser());
            miExport.addActionListener(ev -> exportExcel(false));
            miExportTemplate.addActionListener(ev -> exportExcel(true));
            miPrint.addActionListener(ev -> ChiTieuUI.print(this, table, "Chỉ tiêu doanh thu"));
            miRefresh.addActionListener(ev -> reload(null));

            popup.add(miAdd);
            popup.add(miEdit);
            popup.add(miCopy);
            popup.add(miDelete);
            popup.addSeparator();
            popup.add(miAutoResize);
            popup.add(miChooseCols);
            popup.addSeparator();
            popup.add(miExport);
            popup.add(miExportTemplate);
            popup.add(miPrint);
            popup.add(miRefresh);
        }

        popup.show(table, e.getX(), e.getY());
    }

    private void showTreePopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        TreePath path = tree.getPathForLocation(x, y);
        if (path != null) {
            tree.setSelectionPath(path);
        }

        Object val = selectedNode();
        Group selG = val instanceof Group group ? group : null;
        boolean isTrashNode = (val instanceof Filter f && "TRASH".equals(f.id())) || (selG != null && selG.status() < 0);

        JPopupMenu popup = new JPopupMenu();
        if (isTrashNode || isTrash()) {
            if (selG != null) {
                JMenuItem miRestoreGroup = new JMenuItem("Khôi phục nhóm", IconHelper.getIcon("Undo.png", 16, 16));
                JMenuItem miDeleteGroupPerm = new JMenuItem("Xóa vĩnh viễn nhóm", IconHelper.getIcon("cross.png", 16, 16));
                miRestoreGroup.addActionListener(ev -> trashGroup(selG, false));
                miDeleteGroupPerm.addActionListener(ev -> trashGroup(selG, true));
                popup.add(miRestoreGroup);
                popup.add(miDeleteGroupPerm);
                popup.addSeparator();
            }
            JMenuItem miRefresh = new JMenuItem("Làm mới cây nhóm", IconHelper.getIcon("Refresh.png", 16, 16));
            miRefresh.addActionListener(ev -> reload(null));
            popup.add(miRefresh);
        } else {
            JMenuItem miAddGroupItem = new JMenuItem("Thêm nhóm chỉ tiêu", IconHelper.getIcon("plus.png", 16, 16));
            JMenuItem miAddFolder = new JMenuItem("Thêm thư mục", IconHelper.getIcon("Folder.png", 16, 16));
            JMenuItem miAddSubFolder = new JMenuItem("Thêm thư mục con", IconHelper.getIcon("Folder2.png", 16, 16));
            JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
            JMenuItem miDelete = new JMenuItem("Chuyển vào thùng rác (Del)", IconHelper.getIcon("cross.png", 16, 16));
            JMenuItem miTrash = new JMenuItem("Mở thùng rác...", IconHelper.getIcon("RecycleBin.png", 16, 16));
            JMenuItem miRefresh = new JMenuItem("Làm mới cây nhóm", IconHelper.getIcon("Refresh.png", 16, 16));

            miAddGroupItem.addActionListener(ev -> editGroup(false, false));
            miAddFolder.addActionListener(ev -> editGroup(false, true));
            miAddSubFolder.addActionListener(ev -> editGroup(false, true));
            miEdit.addActionListener(ev -> editGroup(true, false));
            miDelete.addActionListener(ev -> {
                if (selG != null && selG.status() >= 0) trashGroup(selG, true);
            });
            miTrash.addActionListener(ev -> {
                filter = "TRASH";
                reload(null);
            });
            miRefresh.addActionListener(ev -> reload(null));

            popup.add(miAddGroupItem);
            popup.add(miAddFolder);
            popup.add(miAddSubFolder);
            popup.addSeparator();
            popup.add(miEdit);
            popup.add(miDelete);
            popup.addSeparator();
            popup.add(miTrash);
            popup.add(miRefresh);
        }

        popup.show(tree, x, y);
    }

    private Object selectedNode() {
        TreePath p = tree.getSelectionPath();
        return p == null ? null : ((DefaultMutableTreeNode) p.getLastPathComponent()).getUserObject();
    }

    private ChiTieuDoanhThu selected() {
        int row = table.getSelectedRow();
        return row < 0 || row >= visible.size() ? null : visible.get(row);
    }

    private boolean isTrash() {
        return "TRASH".equals(filter) || groups.stream().anyMatch(g -> g.id().equals(filter) && g.status() < 0);
    }

    private void reload(String selectedId) {
        try {
            groups = dao.groups() != null ? new ArrayList<>(dao.groups()) : new ArrayList<>();
            rebuilding = true;

            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Nhóm chỉ tiêu doanh thu");
            DefaultMutableTreeNode nodeTatCa = new DefaultMutableTreeNode(new Filter("ALL", "Tất cả"));
            DefaultMutableTreeNode nodeChuaThietLap = new DefaultMutableTreeNode(new Filter("NONE", "Chưa thiết lập"));
            DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode(new Filter("TRASH", "Thùng rác"));

            root.add(nodeTatCa);
            root.add(nodeChuaThietLap);

            Map<String, DefaultMutableTreeNode> nodes = new LinkedHashMap<>();
            for (Group g : groups) {
                nodes.put(g.id(), new DefaultMutableTreeNode(g));
            }

            for (Group g : groups) {
                DefaultMutableTreeNode node = nodes.get(g.id());
                DefaultMutableTreeNode parent = nodes.get(g.parentId());
                if (g.status() < 0) {
                    nodeTrash.add(node);
                } else if (parent != null && !parent.isNodeAncestor(node) && parent != node && ((Group) parent.getUserObject()).status() >= 0) {
                    parent.add(node);
                } else {
                    root.add(node);
                }
            }

            root.add(nodeTrash);
            tree.setModel(new DefaultTreeModel(root));

            DefaultMutableTreeNode choice = nodes.get(filter);
            if (choice == null) {
                if (!Set.of("ALL", "NONE", "TRASH").contains(filter)) filter = "ALL";
                choice = "TRASH".equals(filter) ? nodeTrash : ("NONE".equals(filter) ? nodeChuaThietLap : nodeTatCa);
            }
            tree.setSelectionPath(new TreePath(choice.getPath()));
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        } catch (Exception e) {
            ChiTieuUI.error(this, e);
        } finally {
            rebuilding = false;
        }

        updateToolbarState();
        reloadPlans(selectedId);
    }

    private void reloadPlans(String id) {
        try {
            plans = dao.list(isTrash()) != null ? new ArrayList<>(dao.list(isTrash())) : new ArrayList<>();
            applySorting();
            render(id);
        } catch (Exception e) {
            plans = new ArrayList<>();
            render(null);
            ChiTieuUI.error(this, e);
        }
    }

    private void applySorting() {
        if (plans == null) return;
        plans.sort((a, b) -> {
            int cmp = 0;
            if ("Năm".equalsIgnoreCase(currentSortField)) {
                cmp = Integer.compare(a.year, b.year);
            } else if ("Phân nhóm".equalsIgnoreCase(currentSortField)) {
                String ga = a.groupName != null ? a.groupName : "";
                String gb = b.groupName != null ? b.groupName : "";
                cmp = ga.compareToIgnoreCase(gb);
            } else if ("Tổng cộng".equalsIgnoreCase(currentSortField)) {
                cmp = a.total().compareTo(b.total());
            } else if ("Ghi chú".equalsIgnoreCase(currentSortField)) {
                String na = a.note != null ? a.note : "";
                String nb = b.note != null ? b.note : "";
                cmp = na.compareToIgnoreCase(nb);
            } else if (currentSortField.startsWith("Tháng ")) {
                try {
                    int mIdx = Integer.parseInt(currentSortField.substring("Tháng ".length()).trim()) - 1;
                    if (mIdx >= 0 && mIdx < 12) {
                        BigDecimal ma = (a.months != null && a.months[mIdx] != null) ? a.months[mIdx] : BigDecimal.ZERO;
                        BigDecimal mb = (b.months != null && b.months[mIdx] != null) ? b.months[mIdx] : BigDecimal.ZERO;
                        cmp = ma.compareTo(mb);
                    }
                } catch (Exception ignored) {}
            }
            return currentSortAsc ? cmp : -cmp;
        });
    }

    private void render(String id) {
        rebuilding = true;
        model.setRowCount(0);
        visible = new ArrayList<>();
        String keyword = search.getText().trim().toLowerCase(Locale.ROOT);

        Set<String> included = new HashSet<>();
        included.add(filter);
        boolean changed;
        do {
            changed = false;
            for (Group g : groups) {
                if (included.contains(g.parentId())) {
                    changed |= included.add(g.id());
                }
            }
        } while (changed);

        BigDecimal sum = BigDecimal.ZERO;
        int selectedRow = -1;

        for (ChiTieuDoanhThu t : plans) {
            if ("NONE".equals(filter) && t.groupId != null && !t.groupId.isBlank()) continue;
            if (!Set.of("ALL", "NONE", "TRASH").contains(filter) && !included.contains(t.groupId)) continue;
            if (!(t.year + " " + ChiTieuUI.text(t.groupName) + " " + ChiTieuUI.text(t.note)).toLowerCase(Locale.ROOT).contains(keyword)) continue;

            visible.add(t);
            List<Object> row = new ArrayList<>();
            for (int c = 0; c < model.getColumnCount(); c++) {
                String colName = model.getColumnName(c);
                if (c == 0 || colName.isEmpty()) {
                    row.add(String.valueOf(visible.size()));
                } else if ("Năm".equals(colName)) {
                    row.add(t.year);
                } else if ("Phân nhóm".equals(colName)) {
                    row.add(t.groupName != null ? t.groupName : "");
                } else if ("Tổng cộng".equals(colName)) {
                    row.add(t.total());
                } else if ("Ghi chú".equals(colName)) {
                    row.add(t.note != null ? t.note : "");
                } else if (colName.startsWith("Tháng ")) {
                    try {
                        int mIdx = Integer.parseInt(colName.substring("Tháng ".length()).trim()) - 1;
                        if (mIdx >= 0 && mIdx < 12) {
                            row.add(t.months[mIdx]);
                        } else {
                            row.add(BigDecimal.ZERO);
                        }
                    } catch (Exception e) {
                        row.add(BigDecimal.ZERO);
                    }
                } else {
                    row.add("");
                }
            }
            model.addRow(row.toArray());
            sum = sum.add(t.total());
            if (Objects.equals(id, t.id)) {
                selectedRow = visible.size() - 1;
            }
        }

        lblStatus.setText("  Tổng " + visible.size() + " kế hoạch · Tổng chỉ tiêu 12 tháng: " + dfMoney.format(sum));
        if (!visible.isEmpty()) {
            int sel = Math.max(0, selectedRow);
            table.setRowSelectionInterval(sel, sel);
        } else {
            table.clearSelection();
        }

        rebuilding = false;
        loadDetails();
        updateToolbarState();
    }

    private void loadDetails() {
        detailModel.setRowCount(0);
        ChiTieuDoanhThu t = selected();
        updateToolbarState();

        if (t == null) {
            lblCreatedTime.setText("Khởi tạo: --");
            lblCreatedUser.setText("Khởi tạo bởi: --");
            lblModifiedTime.setText("Sửa đổi gần nhất: --");
            lblModifiedUser.setText("Sửa đổi bởi: --");
            return;
        }

        lblCreatedTime.setText("Khởi tạo: " + (t.createdAt != null ? sdfDateTime.format(t.createdAt) : "--"));
        lblCreatedUser.setText("Khởi tạo bởi: " + (t.createdBy != null ? t.createdBy : "--"));
        lblModifiedTime.setText("Sửa đổi gần nhất: " + (t.modifiedAt != null ? sdfDateTime.format(t.modifiedAt) : "--"));
        lblModifiedUser.setText("Sửa đổi bởi: " + (t.modifiedBy != null ? t.modifiedBy : "--"));

        try {
            t.provinces.clear();
            List<ProvinceTarget> detailsList = dao.details(t.id);
            if (detailsList != null) {
                t.provinces.addAll(detailsList);
            }
            int n = 1;
            for (ProvinceTarget p : t.provinces) {
                detailModel.addRow(new Object[]{
                        String.valueOf(n++),
                        p.note() != null ? p.note() : "",
                        t.year, // Matching Screenshot 2 & 4: "2026"
                        p.provinceName() != null ? p.provinceName() : "",
                        p.amount()
                });
            }
            if (details.getRowCount() > 0) {
                details.setRowSelectionInterval(0, 0);
            }
        } catch (Exception e) {
            ChiTieuUI.error(this, e);
        }
    }

    private void openEditor(boolean existing, boolean copy) {
        try {
            if (existing && (selected() == null || isTrash())) return;
            ChiTieuDoanhThu t = existing ? selected().copy(copy) : new ChiTieuDoanhThu();
            if (!existing && selectedNode() instanceof Group g && !g.folder() && g.status() >= 0) {
                t.groupId = g.id();
                t.groupName = g.name();
            }
            if (existing) {
                t.provinces.clear();
                t.provinces.addAll(dao.details(selected().id));
            }
            new ChiTieuDoanhThuEditDialog(
                    SwingUtilities.getWindowAncestor(this),
                    dao,
                    t,
                    new ArrayList<>(visible),
                    this::reload
            ).setVisible(true);
        } catch (Exception e) {
            ChiTieuUI.error(this, e);
        }
    }

    private void editGroup(boolean editing, boolean folder) {
        Object value = selectedNode();
        Group g = value instanceof Group group ? group : null;
        if (editing && (g == null || g.status() < 0)) return;
        Group draft = editing ? g : new Group(null, "", "", g != null && g.status() >= 0 ? g.id() : null, folder, null, 30);
        String id = NhomChiTieuDialog.show(this, dao, draft);
        if (id != null) {
            filter = id;
            reload(null);
        }
    }

    private void trashGroup(Group g, boolean trash) {
        if (g == null) return;
        boolean inTrash = (g.status() < 0);
        if (inTrash) {
            String msg = "Bạn có chắc muốn xóa vĩnh viễn nhóm '" + g.name() + "' và tất cả kế hoạch, nhóm con bên trong?";
            if (JOptionPane.showConfirmDialog(this, msg, "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            try {
                dao.deletePermanent(g.id(), true);
                filter = "TRASH";
                reload(null);
            } catch (Exception e) {
                ChiTieuUI.error(this, e);
            }
        } else {
            String msg = (trash ? "Bạn có chắc muốn chuyển nhóm '" : "Khôi phục nhóm '") + g.name() + "' vào thùng rác?";
            if (JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            try {
                dao.setTrash(g.id(), true, trash);
                filter = trash ? "TRASH" : g.id();
                reload(null);
            } catch (Exception e) {
                ChiTieuUI.error(this, e);
            }
        }
    }

    private void trashPlan(boolean trash) {
        ChiTieuDoanhThu t = selected();
        if (t == null) return;
        boolean inTrash = isTrash();
        if (inTrash) {
            String msg = "Bạn có chắc muốn xóa vĩnh viễn kế hoạch năm " + t.year + " - " + ChiTieuUI.text(t.groupName) + "?";
            if (JOptionPane.showConfirmDialog(this, msg, "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            try {
                dao.deletePermanent(t.id, false);
                reload(null);
            } catch (Exception e) {
                ChiTieuUI.error(this, e);
            }
        } else {
            String msg = (trash ? "Chuyển kế hoạch vào thùng rác?" : "Khôi phục kế hoạch?") + "\nNăm " + t.year + " - " + ChiTieuUI.text(t.groupName);
            if (JOptionPane.showConfirmDialog(this, msg, "Chỉ tiêu doanh thu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            try {
                dao.setTrash(t.id, false, trash);
                reload(null);
            } catch (Exception e) {
                ChiTieuUI.error(this, e);
            }
        }
    }

    private JFileChooser excelChooser() {
        JFileChooser c = new JFileChooser();
        c.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx, *.xls)", "xlsx", "xls"));
        return c;
    }

    private void exportExcel(boolean template) {
        JFileChooser chooser = excelChooser();
        chooser.setSelectedFile(new File(template ? "MauChiTieuDoanhThu.xlsx" : "ChiTieuDoanhThu.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = new File(file + ".xlsx");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Ghi đè file " + file.getName() + "?", "Xuất Excel", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            List<ChiTieuDoanhThu> export = new ArrayList<>();
            if (!template) {
                for (ChiTieuDoanhThu value : visible) {
                    ChiTieuDoanhThu t = value.copy(false);
                    t.provinces.clear();
                    t.provinces.addAll(dao.details(t.id));
                    export.add(t);
                }
            }
            try (OutputStream out = new FileOutputStream(file)) {
                ChiTieuDoanhThuExcel.write(out, export);
            }
            JOptionPane.showMessageDialog(this, "Đã xuất " + (template ? "file mẫu" : export.size() + " kế hoạch") + "!\n" + file.getAbsolutePath(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            ChiTieuUI.error(this, e);
        }
    }

    private void importExcel() {
        Object[] actions = {"Chọn file nhập", "Tải file mẫu", "Hủy"};
        int action = JOptionPane.showOptionDialog(this,
                "File gồm 2 sheet: Chỉ tiêu doanh thu và Chỉ tiêu theo tỉnh.\nNhập mới các kế hoạch; nhóm và tỉnh thành phải có sẵn trong danh mục.",
                "Thêm Excel", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, actions, actions[0]);
        if (action == 1) {
            exportExcel(true);
            return;
        }
        if (action != 0) return;

        JFileChooser chooser = excelChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<ChiTieuDoanhThu> imported;
            try (InputStream in = new FileInputStream(chooser.getSelectedFile())) {
                imported = ChiTieuDoanhThuExcel.read(in, dao.groups(), dao.provinces());
            }
            BigDecimal sum = imported.stream().map(ChiTieuDoanhThu::total).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (JOptionPane.showConfirmDialog(this, "Nhập " + imported.size() + " kế hoạch?\nTổng chỉ tiêu 12 tháng: " + dfMoney.format(sum), "Xác nhận nhập Excel", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            dao.saveAll(imported);
            filter = "ALL";
            search.setText("");
            reload(imported.get(0).id);
            JOptionPane.showMessageDialog(this, "Đã nhập thành công " + imported.size() + " kế hoạch!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            ChiTieuUI.error(this, e);
        }
    }

    private void setupShortcuts() {
        ChiTieuUI.bind(this, "INSERT", () -> openEditor(false, false));
        ChiTieuUI.bind(this, "F4", () -> openEditor(true, false));
        ChiTieuUI.bind(this, "DELETE", () -> trashPlan(true));
        ChiTieuUI.bind(this, "F3", () -> search.requestFocusInWindow());
        ChiTieuUI.bind(this, "F5", () -> reload(null));
    }
}
