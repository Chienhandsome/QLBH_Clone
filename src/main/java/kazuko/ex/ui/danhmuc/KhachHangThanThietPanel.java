package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.dao.TangGiamDiemDAO;
import kazuko.ex.model.danhmuc.DiemHoaDon;
import kazuko.ex.model.danhmuc.KhachHangLoyaltySummary;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.model.danhmuc.TangGiamDiem;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class KhachHangThanThietPanel extends JPanel {
    private final TangGiamDiemDAO tangGiamDiemDAO = new TangGiamDiemDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    private double doanhSo1Diem = 20000.0;

    // UI Components
    private JTextField txtSearch;
    private JButton btnRefresh;
    private JButton btnXoaDiem;

    private JTree treeNhomKhach;
    private DefaultTreeModel treeModel;

    // Top table: Customers
    private JTable tableKhachHang;
    private DefaultTableModel modelKhachHang;
    private List<KhachHangLoyaltySummary> allSummaries = new ArrayList<>();
    private List<KhachHangLoyaltySummary> filteredSummaries = new ArrayList<>();
    private String selectedGroupId = null;

    // Bottom Tab 1: Tặng điểm - Tặng quà
    private JTable tableTangGiamDiem;
    private DefaultTableModel modelTangGiamDiem;
    private List<TangGiamDiem> currentTangGiamList = new ArrayList<>();
    private JButton btnThemTangGiam;
    private JButton btnSuaTangGiam;
    private JButton btnXoaTangGiam;

    // Bottom Tab 2: Điểm theo hóa đơn
    private JTable tableDiemHoaDon;
    private DefaultTableModel modelDiemHoaDon;
    private List<DiemHoaDon> currentHoaDonList = new ArrayList<>();

    private static final DecimalFormat DF_CURRENCY = new DecimalFormat("#,##0");
    private static final DecimalFormat DF_POINTS = new DecimalFormat("#,##0.##");
    private static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("dd/MM/yyyy");

    // Colors
    private static final Color COLOR_PANEL_BG = new Color(227, 239, 255);
    private static final Color COLOR_TOOLBAR_BG = new Color(208, 224, 245);
    private static final Color COLOR_BORDER = new Color(170, 195, 225);
    private static final Color COLOR_SELECTED_ROW = new Color(56, 142, 60); // Green row
    private static final Color COLOR_SELECTED_DATE_CELL = new Color(184, 93, 25); // Brownish date cell

    public KhachHangThanThietPanel() {
        loadConfig();
        initUI();
        loadTreeNhomKhach();
        loadData();
    }

    private void loadConfig() {
        try {
            SystemConfig config = configDAO.loadConfig();
            if (config != null && config.getDoanhSoTuongUng1Diem() != null) {
                String dsStr = config.getDoanhSoTuongUng1Diem().trim().replace(",", "").replace(".", "");
                double ds = Double.parseDouble(dsStr);
                if (ds > 0) doanhSo1Diem = ds;
            }
        } catch (Exception e) {
            doanhSo1Diem = 20000.0;
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_PANEL_BG);

        // 1. Top Search & Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_TOOLBAR_BG);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));
        topBar.setPreferredSize(new Dimension(getWidth(), 34));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        leftTop.setOpaque(false);

        JLabel lblSearch = new JLabel("Lọc (F3):");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTableKhachHang();
            }
        });

        btnRefresh = new JButton("Refresh");
        btnRefresh.setIcon(IconHelper.getIcon("arrow-circle-double-135.png", 14, 14));
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            loadConfig();
            loadTreeNhomKhach();
            loadData();
        });

        leftTop.add(lblSearch);
        leftTop.add(txtSearch);
        leftTop.add(btnRefresh);
        topBar.add(leftTop, BorderLayout.WEST);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        rightTop.setOpaque(false);

        btnXoaDiem = new JButton("Xóa điểm");
        btnXoaDiem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoaDiem.setBackground(new Color(245, 248, 255));
        btnXoaDiem.setFocusPainted(false);
        btnXoaDiem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXoaDiem.addActionListener(e -> onXoaDiem());

        rightTop.add(btnXoaDiem);
        topBar.add(rightTop, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // 2. Center Content (Split between Left Tree and Right Main Area)
        JSplitPane splitLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitLeftRight.setDividerLocation(180);
        splitLeftRight.setDividerSize(4);
        splitLeftRight.setBorder(null);

        // 2.1 Left Tree Nhóm khách hàng
        JPanel leftTreePanel = new JPanel(new BorderLayout());
        leftTreePanel.setBackground(Color.WHITE);
        leftTreePanel.setBorder(new MatteBorder(0, 0, 0, 1, COLOR_BORDER));

        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Tất cả"));
        treeNhomKhach = new JTree(treeModel);
        treeNhomKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomKhach.setRowHeight(20);
        treeNhomKhach.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
                JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    if (node.getUserObject() instanceof NhomKhachHang) {
                        NhomKhachHang n = (NhomKhachHang) node.getUserObject();
                        lbl.setText(n.getName());
                        lbl.setIcon(IconHelper.getIcon("folder-horizontal.png", 16, 16));
                    } else if ("Tất cả".equals(node.getUserObject())) {
                        lbl.setText("Tất cả");
                        lbl.setIcon(IconHelper.getIcon("globe-green.png", 16, 16));
                    }
                }
                return lbl;
            }
        });

        treeNhomKhach.addTreeSelectionListener(e -> {
            TreePath path = treeNhomKhach.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof NhomKhachHang) {
                    selectedGroupId = ((NhomKhachHang) node.getUserObject()).getId();
                } else {
                    selectedGroupId = null; // Tất cả
                }
                filterTableKhachHang();
            }
        });

        JScrollPane scrollTree = new JScrollPane(treeNhomKhach);
        scrollTree.setBorder(null);
        leftTreePanel.add(scrollTree, BorderLayout.CENTER);
        splitLeftRight.setLeftComponent(leftTreePanel);

        // 2.2 Right Area (Vertical Split: Top Table Khách hàng & Bottom Tabbed Details)
        JSplitPane splitTopBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitTopBottom.setDividerLocation(360);
        splitTopBottom.setDividerSize(5);
        splitTopBottom.setBorder(null);

        // Top Table: Customers
        String[] colKhach = {"STT", "Tên khách hàng", "Địa chỉ", "Điện thoại", "Email", "Doanh số", "Số hóa đơn", "Điểm tích lũy", "Ghi chú", "_ID"};
        modelKhachHang = new DefaultTableModel(colKhach, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableKhachHang = new JTable(modelKhachHang);
        tableKhachHang.setRowHeight(22);
        tableKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableKhachHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableKhachHang.getTableHeader().setReorderingAllowed(false);
        tableKhachHang.removeColumn(tableKhachHang.getColumnModel().getColumn(9)); // Hide _ID

        // Column widths
        tableKhachHang.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableKhachHang.getColumnModel().getColumn(0).setMaxWidth(45);
        tableKhachHang.getColumnModel().getColumn(1).setPreferredWidth(170);
        tableKhachHang.getColumnModel().getColumn(2).setPreferredWidth(210);
        tableKhachHang.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableKhachHang.getColumnModel().getColumn(4).setPreferredWidth(110);
        tableKhachHang.getColumnModel().getColumn(5).setPreferredWidth(95);
        tableKhachHang.getColumnModel().getColumn(6).setPreferredWidth(75);
        tableKhachHang.getColumnModel().getColumn(7).setPreferredWidth(90);
        tableKhachHang.getColumnModel().getColumn(8).setPreferredWidth(120);

        // Column Alignments
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        tableKhachHang.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tableKhachHang.getColumnModel().getColumn(3).setCellRenderer(centerRender);

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(JLabel.RIGHT);
        tableKhachHang.getColumnModel().getColumn(5).setCellRenderer(rightRender);
        tableKhachHang.getColumnModel().getColumn(6).setCellRenderer(rightRender);
        tableKhachHang.getColumnModel().getColumn(7).setCellRenderer(rightRender);

        tableKhachHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onCustomerSelectionChanged();
            }
        });

        JScrollPane scrollKhach = new JScrollPane(tableKhachHang);
        scrollKhach.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));
        splitTopBottom.setTopComponent(scrollKhach);

        // Bottom Area: JTabbedPane
        JTabbedPane bottomTabbedPane = new JTabbedPane();
        bottomTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: Tặng điểm - Tặng quà
        JPanel pnlTab1 = buildTabTangGiamDiem();
        bottomTabbedPane.addTab("Tặng điểm - tặng quà", pnlTab1);

        // Tab 2: Điểm theo hóa đơn
        JPanel pnlTab2 = buildTabDiemHoaDon();
        bottomTabbedPane.addTab("Điểm theo hóa đơn", pnlTab2);

        splitTopBottom.setBottomComponent(bottomTabbedPane);
        splitLeftRight.setRightComponent(splitTopBottom);

        add(splitLeftRight, BorderLayout.CENTER);

        setupKeyboardShortcuts();
    }

    private JPanel buildTabTangGiamDiem() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL_BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        toolbar.setBackground(COLOR_TOOLBAR_BG);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));

        btnThemTangGiam = createToolbarButton("Thêm mới", "btnNew.Image.png");
        btnSuaTangGiam = createToolbarButton("Sửa", "Edit.png");
        btnXoaTangGiam = createToolbarButton("Xóa", "cross.png");

        btnThemTangGiam.addActionListener(e -> onAddNewTangGiam());
        btnSuaTangGiam.addActionListener(e -> onEditTangGiam());
        btnXoaTangGiam.addActionListener(e -> onDeleteTangGiam());

        toolbar.add(btnThemTangGiam);
        toolbar.add(btnSuaTangGiam);
        toolbar.add(btnXoaTangGiam);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"STT", "Ngày", "Số phiếu", "Ghi chú", "Điểm tăng", "Điểm giảm", "Lý do", "_ID"};
        modelTangGiamDiem = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableTangGiamDiem = new JTable(modelTangGiamDiem);
        tableTangGiamDiem.setRowHeight(22);
        tableTangGiamDiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTangGiamDiem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTangGiamDiem.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableTangGiamDiem.removeColumn(tableTangGiamDiem.getColumnModel().getColumn(7)); // Hide _ID

        tableTangGiamDiem.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableTangGiamDiem.getColumnModel().getColumn(0).setMaxWidth(45);
        tableTangGiamDiem.getColumnModel().getColumn(1).setPreferredWidth(95);
        tableTangGiamDiem.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableTangGiamDiem.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableTangGiamDiem.getColumnModel().getColumn(4).setPreferredWidth(75);
        tableTangGiamDiem.getColumnModel().getColumn(5).setPreferredWidth(75);
        tableTangGiamDiem.getColumnModel().getColumn(6).setPreferredWidth(350);

        // Custom Highlight Renderer for Selected Row
        tableTangGiamDiem.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 0 || column == 1 || column == 2) {
                    lbl.setHorizontalAlignment(JLabel.CENTER);
                } else if (column == 4 || column == 5) {
                    lbl.setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                }

                if (isSelected) {
                    if (column == 1) { // Date column brown/orange highlight
                        lbl.setBackground(COLOR_SELECTED_DATE_CELL);
                    } else {
                        lbl.setBackground(COLOR_SELECTED_ROW);
                    }
                    lbl.setForeground(Color.WHITE);
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 255));
                    lbl.setForeground(Color.BLACK);
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
                return lbl;
            }
        });

        tableTangGiamDiem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditTangGiam();
                }
            }
        });

        tableTangGiamDiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    onDeleteTangGiam();
                }
            }
        });

        JScrollPane scrollTangGiam = new JScrollPane(tableTangGiamDiem);
        scrollTangGiam.setBorder(null);
        panel.add(scrollTangGiam, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildTabDiemHoaDon() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL_BG);

        String[] cols = {"STT", "Ngày", "Số hóa đơn", "Doanh số", "Điểm tích lũy", "Ghi chú"};
        modelDiemHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableDiemHoaDon = new JTable(modelDiemHoaDon);
        tableDiemHoaDon.setRowHeight(22);
        tableDiemHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDiemHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDiemHoaDon.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tableDiemHoaDon.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableDiemHoaDon.getColumnModel().getColumn(0).setMaxWidth(45);
        tableDiemHoaDon.getColumnModel().getColumn(1).setPreferredWidth(95);
        tableDiemHoaDon.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableDiemHoaDon.getColumnModel().getColumn(3).setPreferredWidth(110);
        tableDiemHoaDon.getColumnModel().getColumn(4).setPreferredWidth(110);
        tableDiemHoaDon.getColumnModel().getColumn(5).setPreferredWidth(250);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        tableDiemHoaDon.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tableDiemHoaDon.getColumnModel().getColumn(1).setCellRenderer(centerRender);
        tableDiemHoaDon.getColumnModel().getColumn(2).setCellRenderer(centerRender);

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(JLabel.RIGHT);
        tableDiemHoaDon.getColumnModel().getColumn(3).setCellRenderer(rightRender);
        tableDiemHoaDon.getColumnModel().getColumn(4).setCellRenderer(rightRender);

        JScrollPane scrollHoaDon = new JScrollPane(tableDiemHoaDon);
        scrollHoaDon.setBorder(null);
        panel.add(scrollHoaDon, BorderLayout.CENTER);

        return panel;
    }

    private void loadTreeNhomKhach() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        List<NhomKhachHang> groups = nhomKhachHangDAO.getAll();
        for (NhomKhachHang g : groups) {
            root.add(new DefaultMutableTreeNode(g));
        }
        treeModel.setRoot(root);
        treeModel.reload();
        treeNhomKhach.expandRow(0);
        treeNhomKhach.setSelectionRow(0);
    }

    public void loadData() {
        allSummaries = tangGiamDiemDAO.getLoyaltySummaryList(doanhSo1Diem);
        filterTableKhachHang();
    }

    private void filterTableKhachHang() {
        modelKhachHang.setRowCount(0);
        filteredSummaries.clear();

        String keyword = txtSearch.getText().trim().toLowerCase();

        int stt = 1;
        for (KhachHangLoyaltySummary s : allSummaries) {
            if (s.getKhachHang() == null) continue;

            // Group filter
            if (selectedGroupId != null && !selectedGroupId.equals(s.getKhachHang().getNhomKhachHangId())) {
                continue;
            }

            // Keyword search filter
            if (!keyword.isEmpty()) {
                String name = s.getKhachHang().getName() != null ? s.getKhachHang().getName().toLowerCase() : "";
                String code = s.getKhachHang().getMaKhach() != null ? s.getKhachHang().getMaKhach().toLowerCase() : "";
                String phone = s.getKhachHang().getDienThoai() != null ? s.getKhachHang().getDienThoai().toLowerCase() : "";
                String addr = s.getKhachHang().getDiaChi() != null ? s.getKhachHang().getDiaChi().toLowerCase() : "";
                String email = s.getKhachHang().getEmail() != null ? s.getKhachHang().getEmail().toLowerCase() : "";

                if (!name.contains(keyword) && !code.contains(keyword) && !phone.contains(keyword) && !addr.contains(keyword) && !email.contains(keyword)) {
                    continue;
                }
            }

            filteredSummaries.add(s);
            modelKhachHang.addRow(new Object[]{
                    String.format("%02d", stt++),
                    s.getKhachHang().getName() != null ? s.getKhachHang().getName() : "",
                    s.getKhachHang().getDiaChi() != null ? s.getKhachHang().getDiaChi() : "",
                    s.getKhachHang().getDienThoai() != null ? s.getKhachHang().getDienThoai() : "",
                    s.getKhachHang().getEmail() != null ? s.getKhachHang().getEmail() : "",
                    DF_CURRENCY.format(s.getDoanhSo()),
                    s.getSoHoaDon(),
                    DF_POINTS.format(s.getDiemTichLuy()),
                    s.getKhachHang().getGhiChu() != null ? s.getKhachHang().getGhiChu() : "",
                    s.getKhachHang().getId()
            });
        }

        if (tableKhachHang.getRowCount() > 0) {
            tableKhachHang.setRowSelectionInterval(0, 0);
        } else {
            modelTangGiamDiem.setRowCount(0);
            modelDiemHoaDon.setRowCount(0);
        }
    }

    private void onCustomerSelectionChanged() {
        int selectedRow = tableKhachHang.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < filteredSummaries.size()) {
            KhachHangLoyaltySummary sel = filteredSummaries.get(selectedRow);
            String khId = sel.getKhachHang().getId();
            loadBottomDetails(khId);
        } else {
            modelTangGiamDiem.setRowCount(0);
            modelDiemHoaDon.setRowCount(0);
        }
    }

    private void loadBottomDetails(String khachHangId) {
        // 1. Tab TangGiamDiem
        modelTangGiamDiem.setRowCount(0);
        currentTangGiamList = tangGiamDiemDAO.getByKhachHangId(khachHangId);
        int sttTG = 1;
        for (TangGiamDiem tg : currentTangGiamList) {
            String ngayStr = tg.getNgay() != null ? SDF_DATE.format(tg.getNgay()) : "";
            modelTangGiamDiem.addRow(new Object[]{
                    String.valueOf(sttTG++),
                    ngayStr,
                    tg.getSoPhieu() != null ? tg.getSoPhieu() : "",
                    tg.getNote() != null ? tg.getNote() : "",
                    formatPointNumber(tg.getDiemTang()),
                    formatPointNumber(tg.getDiemGiam()),
                    tg.getLyDo() != null ? tg.getLyDo() : "",
                    tg.getId()
            });
        }
        if (tableTangGiamDiem.getRowCount() > 0) {
            tableTangGiamDiem.setRowSelectionInterval(0, 0);
        }

        // 2. Tab DiemHoaDon
        modelDiemHoaDon.setRowCount(0);
        currentHoaDonList = tangGiamDiemDAO.getHoaDonByKhachHangId(khachHangId, doanhSo1Diem);
        int sttHD = 1;
        for (DiemHoaDon hd : currentHoaDonList) {
            String ngayStr = hd.getNgay() != null ? SDF_DATE.format(hd.getNgay()) : "";
            modelDiemHoaDon.addRow(new Object[]{
                    String.valueOf(sttHD++),
                    ngayStr,
                    hd.getSoHoaDon() != null ? hd.getSoHoaDon() : "",
                    DF_CURRENCY.format(hd.getDoanhSo()),
                    formatPointNumber(hd.getDiemTichLuy()),
                    hd.getGhiChu() != null ? hd.getGhiChu() : ""
            });
        }
    }

    private String formatPointNumber(double pt) {
        if (pt == (long) pt) {
            return String.valueOf((long) pt);
        }
        return String.valueOf(pt);
    }

    private void onAddNewTangGiam() {
        String khId = getSelectedKhachHangId();
        TangGiamDiemEditDialog dialog = new TangGiamDiemEditDialog(this, null, khId, () -> {
            loadData();
            reselectCustomer(khId);
        });
        dialog.setVisible(true);
    }

    private void onEditTangGiam() {
        int row = tableTangGiamDiem.getSelectedRow();
        if (row < 0 || row >= currentTangGiamList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bản ghi tăng giảm điểm để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TangGiamDiem item = currentTangGiamList.get(row);
        String khId = item.getKhachHangId();
        TangGiamDiemEditDialog dialog = new TangGiamDiemEditDialog(this, item, khId, () -> {
            loadData();
            reselectCustomer(khId);
        });
        dialog.setVisible(true);
    }

    private void onDeleteTangGiam() {
        int row = tableTangGiamDiem.getSelectedRow();
        if (row < 0 || row >= currentTangGiamList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi tăng giảm điểm cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TangGiamDiem item = currentTangGiamList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu tăng giảm điểm này không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String khId = item.getKhachHangId();
            tangGiamDiemDAO.delete(item.getId());
            loadData();
            reselectCustomer(khId);
        }
    }

    private void onXoaDiem() {
        int row = tableKhachHang.getSelectedRow();
        if (row < 0 || row >= filteredSummaries.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để xóa điểm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KhachHangLoyaltySummary sel = filteredSummaries.get(row);
        double currentPoints = sel.getDiemTichLuy();
        if (currentPoints <= 0) {
            JOptionPane.showMessageDialog(this, "Khách hàng này hiện không có điểm tích lũy nào để xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showOptionDialog(this,
                "Hệ thống thực hiện xóa điểm tích lũy của khách hàng về 0\nBạn có thực hiện tiếp không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Yes", "No"},
                "Yes");

        if (choice == JOptionPane.YES_OPTION) {
            String khId = sel.getKhachHang().getId();
            tangGiamDiemDAO.resetDiem(khId, currentPoints);
            loadData();
            reselectCustomer(khId);
        }
    }

    private void reselectCustomer(String khachHangId) {
        if (khachHangId == null) return;
        for (int r = 0; r < filteredSummaries.size(); r++) {
            if (khachHangId.equals(filteredSummaries.get(r).getKhachHang().getId())) {
                tableKhachHang.setRowSelectionInterval(r, r);
                tableKhachHang.scrollRectToVisible(tableKhachHang.getCellRect(r, 0, true));
                break;
            }
        }
    }

    private String getSelectedKhachHangId() {
        int row = tableKhachHang.getSelectedRow();
        if (row >= 0 && row < filteredSummaries.size()) {
            return filteredSummaries.get(row).getKhachHang().getId();
        }
        return null;
    }

    private void setupKeyboardShortcuts() {
        // F3 to focus search box
        registerKeyboardAction(e -> {
            txtSearch.requestFocusInWindow();
            txtSearch.selectAll();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private JButton createToolbarButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
