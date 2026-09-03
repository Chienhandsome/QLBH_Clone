package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.khohang.TheKhoEntry;
import kazuko.ex.model.khohang.TonKhoItem;
import kazuko.ex.ui.common.ChonCotHienThiDialog;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuanLyTonKhoPanel extends JPanel {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

    private final DecimalFormat dfMoney = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Top Filter Bar
    private JComboBox<KhoHangItem> cbKhoHang;
    private JButton btnRefreshTop;
    private JTextField txtTimKiem;
    private JCheckBox chkChiHienThiTonKhacKhong;
    private JButton btnXuatExcel;

    // Left Category Tree
    private JTree treeNhomHang;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private String selectedNhomHangId = null;

    // Center Top Table: Tồn kho
    private JTable tableTonKho;
    private DefaultTableModel modelTonKho;
    private final List<TonKhoItem> listAllTonKho = new ArrayList<>();
    private final List<TonKhoItem> listFilteredTonKho = new ArrayList<>();
    private TonKhoItem currentSelectedTonKho = null;

    // Summary Footer
    private JLabel lblTongTon;
    private JLabel lblTongGiaTriBan;
    private JLabel lblTongGiaTriVon;
    private JLabel lblTongTon2Dvt;

    // Center Bottom Tabs: Thẻ kho & Tồn theo hạn dùng
    private JTabbedPane tabbedBottom;
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JButton btnRefreshTheKho;
    private JTable tableTheKho;
    private DefaultTableModel modelTheKho;

    private JTable tableHanDung;
    private DefaultTableModel modelHanDung;

    public static class KhoHangItem {
        private final String id;
        private final String name;

        public KhoHangItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }

    public QuanLyTonKhoPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initComponents();
        setupEvents();
        loadDanhMucKho();
        loadTreeNhomHang();
        loadDataTonKho();
    }

    private void initComponents() {
        // 1. Top Filter Bar (Hình 1)
        JPanel pnlTop = new JPanel(new BorderLayout(8, 0));
        pnlTop.setBackground(new Color(235, 242, 252));
        pnlTop.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(180, 205, 235)),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JPanel pnlTopLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTopLeft.setOpaque(false);

        JLabel lblKho = new JLabel("Kho hàng:");
        lblKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cbKhoHang = new JComboBox<>();
        cbKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoHang.setPreferredSize(new Dimension(140, 24));

        btnRefreshTop = createMiniButton("Refresh", "arrow_refresh.png");

        JLabel lblTim = new JLabel("Tìm (F3):");
        lblTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimKiem.setPreferredSize(new Dimension(160, 24));

        chkChiHienThiTonKhacKhong = new JCheckBox("Chỉ hiển thị mặt hàng có tồn kho <> 0", false);
        chkChiHienThiTonKhacKhong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChiHienThiTonKhacKhong.setOpaque(false);

        pnlTopLeft.add(lblKho);
        pnlTopLeft.add(cbKhoHang);
        pnlTopLeft.add(btnRefreshTop);
        pnlTopLeft.add(lblTim);
        pnlTopLeft.add(txtTimKiem);
        pnlTopLeft.add(chkChiHienThiTonKhacKhong);

        JPanel pnlTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlTopRight.setOpaque(false);

        btnXuatExcel = new JButton("Xuất excel", IconHelper.getIcon("page_white_excel.png", 16, 16));
        btnXuatExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnXuatExcel.setBackground(Color.WHITE);
        btnXuatExcel.setBorder(new CompoundBorder(new LineBorder(new Color(180, 205, 235), 1), new EmptyBorder(3, 10, 3, 10)));
        btnXuatExcel.setFocusPainted(false);
        btnXuatExcel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlTopRight.add(btnXuatExcel);

        pnlTop.add(pnlTopLeft, BorderLayout.CENTER);
        pnlTop.add(pnlTopRight, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // 2. Main Content Split: Left (Category Tree) | Right (Tables)
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(200);
        splitMain.setDividerSize(4);
        splitMain.setBorder(null);

        // Left Panel: Tree Nhóm hàng
        JPanel pnlLeft = createCategoryTreePanel();
        splitMain.setLeftComponent(pnlLeft);

        // Right Panel: Top (Table Tồn kho + Summary) & Bottom (Tabs Thẻ kho & Hạn dùng)
        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitRight.setDividerLocation(360);
        splitRight.setDividerSize(5);
        splitRight.setBorder(null);

        JPanel pnlTopTableAndSummary = createTopTableAndSummaryPanel();
        JPanel pnlBottomTabs = createBottomTabsPanel();

        splitRight.setTopComponent(pnlTopTableAndSummary);
        splitRight.setBottomComponent(pnlBottomTabs);

        splitMain.setRightComponent(splitRight);
        add(splitMain, BorderLayout.CENTER);
    }

    private JPanel createCategoryTreePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new MatteBorder(0, 0, 0, 1, new Color(210, 225, 240)));

        rootNode = new DefaultMutableTreeNode("Tất cả");
        treeModel = new DefaultTreeModel(rootNode);
        treeNhomHang = new JTree(treeModel);
        treeNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomHang.setRowHeight(20);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconHelper.getIcon("bullet_star.png", 14, 14));
        renderer.setOpenIcon(IconHelper.getIcon("folder_page.png", 14, 14));
        renderer.setClosedIcon(IconHelper.getIcon("folder.png", 14, 14));
        treeNhomHang.setCellRenderer(renderer);

        JScrollPane sp = new JScrollPane(treeNhomHang);
        sp.setBorder(null);
        pnl.add(sp, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createTopTableAndSummaryPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        // Table Tồn kho
        String[] cols = {
                "", "Mã hàng", "Mặt hàng", "Tồn", "ĐVT", "Tồn 2 đvt",
                "Giá bán", "Giá trị bán", "Giá vốn", "Giá trị vốn",
                "ĐVT", "Bảo hành", "Quy đổi", "Hãng sx", "Tồn tối thiểu"
        };

        modelTonKho = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableTonKho = new JTable(modelTonKho);
        styleTable(tableTonKho, true);

        // Attach right-click column chooser
        List<String> colNames = List.of(cols);
        ChonCotHienThiDialog.attachToTableHeader(tableTonKho, colNames);

        JScrollPane sp = new JScrollPane(tableTonKho);
        sp.setBorder(new MatteBorder(0, 0, 1, 0, new Color(210, 225, 240)));
        pnl.add(sp, BorderLayout.CENTER);

        // Summary Bar (Dòng TỔNG Hình 1)
        JPanel pnlSummary = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        pnlSummary.setBackground(new Color(242, 246, 252));
        pnlSummary.setBorder(new MatteBorder(1, 0, 1, 0, new Color(200, 218, 240)));

        JLabel lblTitle = new JLabel("TỔNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(20, 50, 95));

        lblTongTon = createSummaryBox("0");
        lblTongGiaTriBan = createSummaryBox("0");
        lblTongGiaTriVon = createSummaryBox("0");
        lblTongTon2Dvt = createSummaryBox("0");

        pnlSummary.add(lblTitle);
        pnlSummary.add(new JLabel("Tồn:"));
        pnlSummary.add(lblTongTon);
        pnlSummary.add(new JLabel("Tổng GT bán:"));
        pnlSummary.add(lblTongGiaTriBan);
        pnlSummary.add(new JLabel("Tổng GT vốn:"));
        pnlSummary.add(lblTongGiaTriVon);
        pnlSummary.add(new JLabel("Tồn 2 ĐVT:"));
        pnlSummary.add(lblTongTon2Dvt);

        pnl.add(pnlSummary, BorderLayout.SOUTH);
        return pnl;
    }

    private JLabel createSummaryBox(String defaultVal) {
        JLabel lbl = new JLabel(defaultVal);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(15, 60, 140));
        lbl.setBackground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBorder(new CompoundBorder(new LineBorder(new Color(185, 205, 230), 1), new EmptyBorder(2, 8, 2, 8)));
        return lbl;
    }

    private JPanel createBottomTabsPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        tabbedBottom = new JTabbedPane(JTabbedPane.TOP);
        tabbedBottom.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Thẻ kho (Hình 1 & 4)
        JPanel pnlTheKho = createTheKhoTab();
        tabbedBottom.addTab("Thẻ kho", pnlTheKho);

        // Tab 2: Tồn theo hạn dùng (Hình 5)
        JPanel pnlHanDung = createHanDungTab();
        tabbedBottom.addTab("Tồn theo hạn dùng", pnlHanDung);

        pnl.add(tabbedBottom, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createTheKhoTab() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        // Filter Bar: Từ ngày ... Đến ngày ... Refresh
        JPanel pnlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        pnlBar.setBackground(new Color(235, 242, 252));
        pnlBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(200, 218, 240)));

        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dpkTuNgay = new DatePickerField();
        dpkTuNgay.setPreferredSize(new Dimension(110, 23));

        JLabel lblDen = new JLabel("Đến:");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setPreferredSize(new Dimension(110, 23));

        btnRefreshTheKho = createMiniButton("Refresh", "arrow_refresh.png");

        pnlBar.add(lblTu);
        pnlBar.add(dpkTuNgay);
        pnlBar.add(lblDen);
        pnlBar.add(dpkDenNgay);
        pnlBar.add(btnRefreshTheKho);

        pnl.add(pnlBar, BorderLayout.NORTH);

        // Table Thẻ kho
        String[] theKhoCols = {
                "Số phiếu", "Ngày", "Diễn giải", "Đơn giá", "Thành tiền",
                "Giảm giá %", "ĐVT", "Số lượng nhập", "Số lượng xuất", "Tồn", "Đối tượng"
        };

        modelTheKho = new DefaultTableModel(theKhoCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableTheKho = new JTable(modelTheKho);
        styleTable(tableTheKho, false);
        ChonCotHienThiDialog.attachToTableHeader(tableTheKho, List.of(theKhoCols));

        pnl.add(new JScrollPane(tableTheKho), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createHanDungTab() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        String[] cols = {"Hạn dùng", "Tồn"};
        modelHanDung = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableHanDung = new JTable(modelHanDung);
        styleTable(tableHanDung, false);
        ChonCotHienThiDialog.attachToTableHeader(tableHanDung, List.of(cols));

        pnl.add(new JScrollPane(tableHanDung), BorderLayout.CENTER);
        return pnl;
    }

    private void styleTable(JTable table, boolean isTopTable) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 238, 248));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (isTopTable) {
            table.setSelectionBackground(new Color(0, 120, 215)); // Dark blue highlight as in Image 1
        } else {
            table.setSelectionBackground(new Color(210, 230, 252));
            table.setSelectionForeground(Color.BLACK);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(235, 242, 252));
        header.setForeground(new Color(40, 50, 70));
        header.setPreferredSize(new Dimension(100, 24));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        if (isTopTable) {
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                    if (isSelected) {
                        setText("▶ " + (value != null ? value.toString() : ""));
                    } else {
                        setText(value != null ? value.toString() : "");
                    }
                    setHorizontalAlignment(SwingConstants.CENTER);
                    return c;
                }
            });

            table.getColumnModel().getColumn(1).setPreferredWidth(80);
            table.getColumnModel().getColumn(2).setPreferredWidth(220);

            // Right-aligned columns: Tồn, Tồn 2 đvt, Giá bán, Giá trị bán, Giá vốn, Giá trị vốn
            table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);
        }
    }

    private JButton createMiniButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(180, 205, 235), 1), new EmptyBorder(2, 8, 2, 8)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupEvents() {
        // Combo Kho hàng change
        cbKhoHang.addActionListener(e -> loadDataTonKho());

        // Refresh top
        btnRefreshTop.addActionListener(e -> loadDataTonKho());

        // Search text
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        // Filter stock <> 0
        chkChiHienThiTonKhacKhong.addActionListener(e -> applyFilters());

        // Export Excel
        btnXuatExcel.addActionListener(e -> exportToExcel());

        // Tree Category selection
        treeNhomHang.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) treeNhomHang.getLastSelectedPathComponent();
            if (selected == null || selected.isRoot()) {
                selectedNhomHangId = null;
            } else {
                Object userObj = selected.getUserObject();
                if (userObj instanceof NhomHangNode) {
                    selectedNhomHangId = ((NhomHangNode) userObj).getId();
                } else {
                    selectedNhomHangId = null;
                }
            }
            applyFilters();
        });

        // Top Table Selection Change -> Reload bottom tabs (Thẻ kho & Hạn dùng)
        tableTonKho.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableTonKho.getSelectedRow();
                if (row >= 0 && row < listFilteredTonKho.size()) {
                    currentSelectedTonKho = listFilteredTonKho.get(row);
                    loadTheKhoData();
                    loadHanDungData();
                } else {
                    currentSelectedTonKho = null;
                    modelTheKho.setRowCount(0);
                    modelHanDung.setRowCount(0);
                }
            }
        });

        // Refresh Thẻ kho
        btnRefreshTheKho.addActionListener(e -> loadTheKhoData());
    }

    private static class NhomHangNode {
        private final String id;
        private final String name;

        public NhomHangNode(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }

    private void loadDanhMucKho() {
        cbKhoHang.removeAllItems();
        cbKhoHang.addItem(new KhoHangItem(null, "")); // Để trống/tất cả kho như Hình 1
        try {
            List<KhoHang> list = khoHangDAO.getAll();
            for (KhoHang k : list) {
                cbKhoHang.addItem(new KhoHangItem(k.getId(), k.getName()));
            }
        } catch (Exception ex) {
            System.err.println("Lỗi loadDanhMucKho: " + ex.getMessage());
        }
    }

    private void loadTreeNhomHang() {
        rootNode.removeAllChildren();
        try {
            List<NhomMatHang> list = nhomMatHangDAO.getAll();
            for (NhomMatHang n : list) {
                rootNode.add(new DefaultMutableTreeNode(new NhomHangNode(n.getId(), n.getName())));
            }
            treeModel.reload();
            treeNhomHang.expandRow(0);
        } catch (Exception ex) {
            System.err.println("Lỗi loadTreeNhomHang: " + ex.getMessage());
        }
    }

    public void loadDataTonKho() {
        KhoHangItem selectedKho = (KhoHangItem) cbKhoHang.getSelectedItem();
        String khoId = (selectedKho != null) ? selectedKho.getId() : null;

        listAllTonKho.clear();
        listAllTonKho.addAll(donHangDAO.getTonKhoDanhSach(khoId));
        applyFilters();
    }

    private void applyFilters() {
        listFilteredTonKho.clear();
        String query = txtTimKiem.getText().trim().toLowerCase();
        boolean onlyStockNonZero = chkChiHienThiTonKhacKhong.isSelected();

        double totalTon = 0.0;
        double totalGtBan = 0.0;
        double totalGtVon = 0.0;
        double totalTon2Dvt = 0.0;

        for (TonKhoItem item : listAllTonKho) {
            if (onlyStockNonZero && Math.abs(item.getTon()) < 0.0001) {
                continue;
            }
            if (selectedNhomHangId != null && !selectedNhomHangId.trim().isEmpty()) {
                if (item.getNhomHangId() == null || !item.getNhomHangId().trim().equalsIgnoreCase(selectedNhomHangId.trim())) {
                    continue;
                }
            }
            if (!query.isEmpty()) {
                boolean matchMa = item.getMaHang() != null && item.getMaHang().toLowerCase().contains(query);
                boolean matchTen = item.getTenHang() != null && item.getTenHang().toLowerCase().contains(query);
                if (!matchMa && !matchTen) {
                    continue;
                }
            }

            listFilteredTonKho.add(item);
            totalTon += item.getTon();
            totalGtBan += item.getGiaTriBan();
            totalGtVon += item.getGiaTriVon();
            totalTon2Dvt += item.getTon2Dvt();
        }

        // Cập nhật bảng
        modelTonKho.setRowCount(0);
        for (int i = 0; i < listFilteredTonKho.size(); i++) {
            TonKhoItem item = listFilteredTonKho.get(i);
            String stt = String.format("%04d", i + 1);
            modelTonKho.addRow(new Object[]{
                    stt,
                    item.getMaHang(),
                    item.getTenHang(),
                    formatNum(item.getTon()),
                    item.getDonViTinh() != null ? item.getDonViTinh() : "",
                    formatNum(item.getTon2Dvt()),
                    formatNum(item.getGiaBan()),
                    formatNum(item.getGiaTriBan()),
                    formatNum(item.getGiaVon()),
                    formatNum(item.getGiaTriVon()),
                    item.getDonViTinh2() != null ? item.getDonViTinh2() : "",
                    item.getBaoHanh() != null ? item.getBaoHanh() : "",
                    item.getQuyDoi(),
                    item.getHangSx() != null ? item.getHangSx() : "",
                    formatNum(item.getTonToiThieu())
            });
        }

        // Cập nhật dòng TỔNG
        lblTongTon.setText(formatNum(totalTon));
        lblTongGiaTriBan.setText(formatNum(totalGtBan));
        lblTongGiaTriVon.setText(formatNum(totalGtVon));
        lblTongTon2Dvt.setText(formatNum(totalTon2Dvt));

        if (!listFilteredTonKho.isEmpty()) {
            tableTonKho.setRowSelectionInterval(0, 0);
        } else {
            currentSelectedTonKho = null;
            modelTheKho.setRowCount(0);
            modelHanDung.setRowCount(0);
        }
    }

    private String formatNum(double val) {
        if (Math.abs(val - (long) val) < 0.0001) {
            return String.format("%,d", (long) val);
        }
        return dfMoney.format(val);
    }

    private void loadTheKhoData() {
        if (currentSelectedTonKho == null) {
            modelTheKho.setRowCount(0);
            return;
        }

        KhoHangItem selectedKho = (KhoHangItem) cbKhoHang.getSelectedItem();
        String khoId = (selectedKho != null) ? selectedKho.getId() : null;
        Date tuNgay = dpkTuNgay.getDate();
        Date denNgay = dpkDenNgay.getDate();

        List<TheKhoEntry> entries = donHangDAO.getTheKho(currentSelectedTonKho.getMatHangId(), khoId, tuNgay, denNgay);
        modelTheKho.setRowCount(0);

        for (TheKhoEntry e : entries) {
            modelTheKho.addRow(new Object[]{
                    e.getSoPhieu(),
                    e.getNgay() != null ? sdfDate.format(e.getNgay()) : "",
                    e.getDienGiai(),
                    formatNum(e.getDonGia()),
                    formatNum(e.getThanhTien()),
                    formatNum(e.getTiLeGiamGia()),
                    e.getTenDonViTinh() != null ? e.getTenDonViTinh() : "",
                    formatNum(e.getSlNhap()),
                    formatNum(e.getSlXuat()),
                    formatNum(e.getTonLuyKe()),
                    e.getDoiTuong()
            });
        }
    }

    private void loadHanDungData() {
        if (currentSelectedTonKho == null) {
            modelHanDung.setRowCount(0);
            return;
        }

        KhoHangItem selectedKho = (KhoHangItem) cbKhoHang.getSelectedItem();
        String khoId = (selectedKho != null) ? selectedKho.getId() : null;

        List<Object[]> rows = donHangDAO.getTonTheoHanDung(currentSelectedTonKho.getMatHangId(), khoId);
        modelHanDung.setRowCount(0);

        for (Object[] r : rows) {
            modelHanDung.addRow(new Object[]{
                    r[0],
                    formatNum((Double) r[1])
            });
        }
    }

    private void exportToExcel() {
        if (listFilteredTonKho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel/CSV Tồn kho");
        fc.setSelectedFile(new File("BaoCaoTonKho_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(f, StandardCharsets.UTF_8)) {
                pw.write('\ufeff'); // BOM cho tiếng Việt Unicode
                pw.println("STT,Mã hàng,Tên hàng,Tồn kho,ĐVT,Tồn 2 ĐVT,Giá bán,Giá trị bán,Giá vốn,Giá trị vốn");
                for (int i = 0; i < listFilteredTonKho.size(); i++) {
                    TonKhoItem item = listFilteredTonKho.get(i);
                    pw.printf("%d,\"%s\",\"%s\",%.2f,\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f\n",
                            i + 1,
                            item.getMaHang() != null ? item.getMaHang() : "",
                            item.getTenHang() != null ? item.getTenHang().replace("\"", "\"\"") : "",
                            item.getTon(),
                            item.getDonViTinh() != null ? item.getDonViTinh() : "",
                            item.getTon2Dvt(),
                            item.getGiaBan(),
                            item.getGiaTriBan(),
                            item.getGiaVon(),
                            item.getGiaTriVon()
                    );
                }
                JOptionPane.showMessageDialog(this, "Xuất excel thành công vào file:\n" + f.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
