package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.banhang.ThongKeMatHangBanRow;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.report.service.BaoCaoBanHangReportService;
import kazuko.ex.report.service.BaoCaoQuanTriReportService;
import kazuko.ex.service.ActivityLogService;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Màn hình Thống kê mặt hàng bán (báo cáo doanh số và lãi gộp theo mặt hàng & nhóm hàng).
 */
public class ThongKeMatHangBanPanel extends JPanel {

    public static final List<String> ALL_COLUMNS = List.of(
            "Mã hàng", "Tên mặt hàng", "Số lượng", "ĐVT", "Đơn giá",
            "Thành tiền bán", "Giá vốn", "Giảm giá %", "Thành tiền nhập",
            "Tỉ lệ lãi", "Lãi"
    );

    public static final List<String> DEFAULT_COLUMNS = List.of(
            "Mã hàng", "Tên mặt hàng", "Số lượng", "ĐVT", "Đơn giá",
            "Thành tiền bán", "Giá vốn", "Giảm giá %", "Thành tiền nhập",
            "Tỉ lệ lãi", "Lãi"
    );

    private static final Color TOOLBAR_BACKGROUND = new Color(205, 223, 245);
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.##");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final BaoCaoBanHangReportService reportService = new BaoCaoBanHangReportService();
    private final BaoCaoQuanTriReportService quanTriReportService = new BaoCaoQuanTriReportService();

    private final DatePickerField fromDate = new DatePickerField();
    private final DatePickerField toDate = new DatePickerField();
    private final JComboBox<StoreOption> storeCombo = new JComboBox<>();
    private final JButton refreshButton = new JButton("Refresh (F5)");
    private final JButton printButton = new JButton("In báo cáo");

    private JTree categoryTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    private final JTable salesTable = new JTable();
    private List<String> visibleColumns = new ArrayList<>(DEFAULT_COLUMNS);

    // Footer summary labels
    private final JLabel lblTongTienBanVal = new JLabel("0");
    private final JLabel lblTongTienNhapVal = new JLabel("0");
    private final JLabel lblTongLaiBanHangVal = new JLabel("0");
    private final JLabel lblTongGiamGiaVal = new JLabel("");
    private final JLabel lblTongLaiVal = new JLabel("0");
    private final JLabel statusLabel = new JLabel(" Sẵn sàng");

    private List<ThongKeMatHangBanRow> allRows = new ArrayList<>();
    private String selectedCategoryName = "Tất cả";
    private String selectedCategoryId = null;

    private SwingWorker<List<ThongKeMatHangBanRow>, Void> loadWorker;
    private long loadSequence;

    public ThongKeMatHangBanPanel() {
        initializeUi();
        loadStores();
        loadCategories();
        reloadData();
    }

    private void initializeUi() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        Date today = startOfDay(new Date());
        fromDate.setDate(today);
        toDate.setDate(today);
        fromDate.setPreferredSize(new Dimension(140, 26));
        toDate.setPreferredSize(new Dimension(140, 26));

        // 1. Top toolbar
        add(createFilterBar(), BorderLayout.NORTH);

        // 2. Left category tree panel
        JPanel pnlCategory = createCategoryPanel();

        // 3. Right table panel with footer
        JPanel pnlRight = createRightTablePanel();

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlCategory, pnlRight);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // Shortcut F5
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refreshSalesStats");
        getActionMap().put("refreshSalesStats", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reloadData();
            }
        });
    }

    private JPanel createFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBackground(TOOLBAR_BACKGROUND);
        panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(150, 180, 215)));

        panel.add(new JLabel("Từ:"));
        panel.add(fromDate);
        panel.add(new JLabel("Đến:"));
        panel.add(toDate);

        JButton quickDateButton = new JButton("▼");
        quickDateButton.setMargin(new Insets(1, 5, 1, 5));
        quickDateButton.setFocusable(false);
        quickDateButton.setToolTipText("Chọn nhanh khoảng ngày");
        quickDateButton.addActionListener(e -> showQuickDateMenu(quickDateButton));
        panel.add(quickDateButton);

        panel.add(new JLabel("Cửa hàng:"));
        storeCombo.setPreferredSize(new Dimension(170, 26));
        panel.add(storeCombo);

        refreshButton.setIcon(IconHelper.getIcon("Refresh.png", 16, 16));
        refreshButton.setPreferredSize(new Dimension(125, 28));
        refreshButton.addActionListener(e -> reloadData());
        panel.add(refreshButton);

        printButton.setIcon(IconHelper.getIcon("print-16.png", 16, 16));
        printButton.setPreferredSize(new Dimension(120, 28));
        printButton.addActionListener(e -> previewReport());
        panel.add(printButton);

        return panel;
    }

    private void showQuickDateMenu(Component owner) {
        JPopupMenu menu = new JPopupMenu();
        addQuickDateItem(menu, "Hôm nay", 0);
        addQuickDateItem(menu, "Hôm qua", -1);
        JMenuItem sevenDays = new JMenuItem("7 ngày gần đây");
        sevenDays.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            toDate.setDate(startOfDay(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, -6);
            fromDate.setDate(startOfDay(cal.getTime()));
            reloadData();
        });
        menu.add(sevenDays);
        JMenuItem currentMonth = new JMenuItem("Tháng này");
        currentMonth.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            toDate.setDate(startOfDay(cal.getTime()));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            fromDate.setDate(startOfDay(cal.getTime()));
            reloadData();
        });
        menu.add(currentMonth);
        menu.show(owner, 0, owner.getHeight());
    }

    private void addQuickDateItem(JPopupMenu menu, String title, int dayOffset) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, dayOffset);
            Date selected = startOfDay(cal.getTime());
            fromDate.setDate(selected);
            toDate.setDate(selected);
            reloadData();
        });
        menu.add(item);
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(200, 215, 235)));

        rootNode = new DefaultMutableTreeNode(new CategoryNodeItem("ALL", "Tất cả"));
        treeModel = new DefaultTreeModel(rootNode);
        categoryTree = new JTree(treeModel);
        categoryTree.setRootVisible(true);
        categoryTree.setShowsRootHandles(true);
        categoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        categoryTree.setRowHeight(22);

        ImageIcon starIcon = IconHelper.getIcon("asterisk-yellow.png", 14, 14);
        if (starIcon == null) starIcon = IconHelper.getIcon("Tree.png", 14, 14);
        ImageIcon folderIcon = IconHelper.getIcon("Folder.png", 14, 14);

        final ImageIcon finalStarIcon = starIcon;
        final ImageIcon finalFolderIcon = folderIcon;

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode node) {
                    if (node.isRoot()) {
                        setIcon(finalFolderIcon);
                    } else {
                        setIcon(finalStarIcon);
                    }
                }
                return this;
            }
        };
        categoryTree.setCellRenderer(renderer);

        categoryTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof CategoryNodeItem item) {
                selectedCategoryName = item.name;
                selectedCategoryId = item.id;
            } else {
                selectedCategoryName = "Tất cả";
                selectedCategoryId = null;
            }
            filterTableRows();
        });

        JScrollPane treeScroll = new JScrollPane(categoryTree);
        treeScroll.setBorder(null);
        treeScroll.getViewport().setBackground(Color.WHITE);
        panel.add(treeScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        configureTable(salesTable);
        rebuildTableHeaders();
        installColumnChooser(salesTable);

        JScrollPane tableScroll = new JScrollPane(salesTable);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(Color.WHITE);
        panel.add(tableScroll, BorderLayout.CENTER);

        JPanel pnlFooter = createFooterSummaryPanel();
        panel.add(pnlFooter, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFooterSummaryPanel() {
        JPanel mainFooter = new JPanel(new BorderLayout());
        mainFooter.setBackground(new Color(245, 248, 253));
        mainFooter.setBorder(new MatteBorder(1, 0, 0, 0, new Color(190, 210, 235)));

        // Summary Content Panel
        JPanel pnlSummary = new JPanel();
        pnlSummary.setLayout(new BoxLayout(pnlSummary, BoxLayout.Y_AXIS));
        pnlSummary.setBackground(new Color(245, 248, 253));
        pnlSummary.setBorder(new EmptyBorder(8, 15, 8, 25));

        // Line 1: Tổng tiền bán | Tổng tiền nhập | [val] (Tổng tiền lãi bán hàng)
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 2));
        row1.setOpaque(false);

        JLabel lblBanTitle = new JLabel("Tổng tiền bán:");
        lblBanTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTongTienBanVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongTienBanVal.setForeground(Color.BLACK);

        JLabel lblNhapTitle = new JLabel("Tổng tiền nhập:");
        lblNhapTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTongTienNhapVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongTienNhapVal.setForeground(Color.BLACK);

        lblTongLaiBanHangVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongLaiBanHangVal.setForeground(Color.BLACK);
        JLabel lblLaiBanHangTitle = new JLabel("(Tổng tiền lãi bán hàng)");
        lblLaiBanHangTitle.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        row1.add(lblBanTitle);
        row1.add(lblTongTienBanVal);
        row1.add(Box.createHorizontalStrut(15));
        row1.add(lblNhapTitle);
        row1.add(lblTongTienNhapVal);
        row1.add(Box.createHorizontalStrut(15));
        row1.add(lblTongLaiBanHangVal);
        row1.add(lblLaiBanHangTitle);

        // Line 2: (Tổng tiền giảm giá)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        row2.setOpaque(false);
        lblTongGiamGiaVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTongGiamGiaVal.setForeground(new Color(100, 100, 100));
        JLabel lblGiamGiaTitle = new JLabel("(Tổng tiền giảm giá)");
        lblGiamGiaTitle.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        row2.add(lblTongGiamGiaVal);
        row2.add(lblGiamGiaTitle);

        // Separator line
        JPanel rowSep = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 2));
        rowSep.setOpaque(false);
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setPreferredSize(new Dimension(320, 2));
        sep.setForeground(new Color(120, 120, 120));
        rowSep.add(sep);

        // Line 3: [Val] (Tổng tiền lãi) in bold red
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 2));
        row3.setOpaque(false);
        lblTongLaiVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongLaiVal.setForeground(new Color(180, 0, 0)); // Dark red

        JLabel lblTongLaiTitle = new JLabel("(Tổng tiền lãi)");
        lblTongLaiTitle.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        row3.add(lblTongLaiVal);
        row3.add(lblTongLaiTitle);

        pnlSummary.add(row1);
        pnlSummary.add(row2);
        pnlSummary.add(rowSep);
        pnlSummary.add(row3);

        mainFooter.add(pnlSummary, BorderLayout.CENTER);

        // Bottom status label
        statusLabel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(210, 220, 235)),
                new EmptyBorder(3, 8, 3, 8)
        ));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(70, 90, 115));
        mainFooter.add(statusLabel, BorderLayout.SOUTH);

        return mainFooter;
    }

    private void configureTable(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(195, 212, 232));
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void loadStores() {
        storeCombo.removeAllItems();
        storeCombo.addItem(new StoreOption("", "-- Tất cả cửa hàng --"));
        try {
            for (CuaHang store : cuaHangDAO.getRealStores()) {
                storeCombo.addItem(new StoreOption(store.getId(), store.getName()));
            }
        } catch (Exception ignored) {}
    }

    private void loadCategories() {
        rootNode.removeAllChildren();
        try {
            List<NhomMatHang> list = nhomMatHangDAO.getAll();
            if (list != null) {
                // Sắp xếp theo tên
                list.sort(Comparator.comparing(NhomMatHang::getName, String.CASE_INSENSITIVE_ORDER));
                for (NhomMatHang nhom : list) {
                    rootNode.add(new DefaultMutableTreeNode(new CategoryNodeItem(nhom.getId(), nhom.getName())));
                }
            }
        } catch (Exception ignored) {}
        treeModel.reload();
        categoryTree.setSelectionPath(new TreePath(rootNode.getPath()));
    }

    public void reloadData() {
        Date from = fromDate.getDate();
        Date to = toDate.getDate();
        if (from == null || to == null) {
            showWarning("Ngày bắt đầu và ngày kết thúc phải đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (from.after(to)) {
            showWarning("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
            return;
        }

        if (loadWorker != null && !loadWorker.isDone()) loadWorker.cancel(true);
        long sequence = ++loadSequence;
        String storeId = selectedStoreId();
        setLoading(true, "Đang tải dữ liệu thống kê mặt hàng bán...");

        loadWorker = new SwingWorker<>() {
            @Override
            protected List<ThongKeMatHangBanRow> doInBackground() {
                return calculateSalesStatistics(startOfDay(from), startOfDay(to), storeId);
            }

            @Override
            protected void done() {
                if (sequence != loadSequence) return;
                try {
                    allRows = get();
                    filterTableRows();
                } catch (CancellationException ignored) {
                } catch (Exception ex) {
                    allRows = new ArrayList<>();
                    filterTableRows();
                    statusLabel.setText("Không thể tải dữ liệu thống kê");
                    showError("Không thể tải thống kê mặt hàng bán", ex);
                } finally {
                    setLoading(false, statusLabel.getText());
                }
            }
        };
        loadWorker.execute();
    }

    private List<ThongKeMatHangBanRow> calculateSalesStatistics(Date tuNgay, Date denNgay, String storeId) {
        List<DonHang> invoices = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, storeId, null);
        Map<String, MatHang> productMap = new HashMap<>();
        Map<String, String> categoryMap = new HashMap<>();

        try {
            for (MatHang m : matHangDAO.getAll()) {
                if (m.getId() != null) productMap.put(m.getId(), m);
                if (m.getCode() != null) productMap.put(m.getCode().toLowerCase(), m);
            }
        } catch (Exception ignored) {}

        try {
            for (NhomMatHang n : nhomMatHangDAO.getAll()) {
                if (n.getId() != null) categoryMap.put(n.getId(), n.getName());
            }
        } catch (Exception ignored) {}

        // Map key -> Aggregated Item
        Map<String, AggregatedItem> aggMap = new LinkedHashMap<>();

        for (DonHang dh : invoices) {
            if (dh.getStatus() < 0) continue;

            // Xuất bán
            if (dh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                    processDetail(ct, aggMap, productMap, categoryMap, 1.0);
                }
            }

            // Trả hàng
            if (dh.getChiTietTraHang() != null) {
                for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                    processDetail(ct, aggMap, productMap, categoryMap, -1.0);
                }
            }
        }

        // Convert AggregatedItem map to Rows
        List<ThongKeMatHangBanRow> rows = new ArrayList<>();
        int stt = 1;
        for (AggregatedItem item : aggMap.values()) {
            double soLuong = item.soLuong;
            double donGia = item.donGia;
            double thanhTienBan = item.thanhTienBan;
            double giaVon = item.giaVon;
            double giamGia = item.giamGia;

            // Thành tiền nhập = Số lượng * Giá vốn
            double thanhTienNhap = soLuong * giaVon;

            // Lãi = Thành tiền bán - Thành tiền nhập
            double lai = thanhTienBan - thanhTienNhap;

            // Tỉ lệ lãi %
            double tiLeLai = 0.0;
            if (giaVon == 0.0 && thanhTienBan > 0.0) {
                tiLeLai = 100.0;
            } else if (thanhTienBan != 0.0) {
                tiLeLai = (lai / thanhTienBan) * 100.0;
            }

            ThongKeMatHangBanRow row = new ThongKeMatHangBanRow(
                    stt++,
                    item.maHang,
                    item.tenHang,
                    soLuong,
                    item.donViTinh,
                    donGia,
                    thanhTienBan,
                    giaVon,
                    giamGia,
                    thanhTienNhap,
                    tiLeLai,
                    lai,
                    item.nhomHangId,
                    item.nhomHangName
            );
            rows.add(row);
        }

        return rows;
    }

    private void processDetail(DonHangChiTiet ct, Map<String, AggregatedItem> aggMap,
                               Map<String, MatHang> productMap, Map<String, String> categoryMap, double sign) {
        String code = ct.getMaHang() != null ? ct.getMaHang().trim() : "";
        String name = ct.getTenHang() != null ? ct.getTenHang().trim() : "";
        String mhId = ct.getMatHangId();

        MatHang mh = null;
        if (mhId != null) mh = productMap.get(mhId);
        if (mh == null && !code.isEmpty()) mh = productMap.get(code.toLowerCase());

        String key = mhId != null ? mhId : (!code.isEmpty() ? code : name);
        if (key.isEmpty()) return;

        AggregatedItem item = aggMap.get(key);
        if (item == null) {
            item = new AggregatedItem();
            item.maHang = !code.isEmpty() ? code : (mh != null ? mh.getCode() : "");
            item.tenHang = !name.isEmpty() ? name : (mh != null ? mh.getName() : "");
            item.donViTinh = ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : (mh != null ? mh.getTenDonViTinh() : "");
            item.donGia = ct.getDonGia();
            item.giamGia = ct.getTiLeGiamGia();

            double cost = 0.0;
            if (mh != null) {
                cost = mh.getGiaVon() > 0 ? mh.getGiaVon() : mh.getGiaNhap();
            }
            item.giaVon = cost;

            String nId = mh != null ? mh.getNhomMatHangId() : null;
            item.nhomHangId = nId;
            item.nhomHangName = (mh != null && mh.getTenNhom() != null) ? mh.getTenNhom() : (nId != null ? categoryMap.get(nId) : "");
            aggMap.put(key, item);
        }

        double sl = (ct.getSoLuong() > 0 ? ct.getSoLuong() : 1.0) * sign;
        double tt = (ct.getThanhTien() > 0 ? ct.getThanhTien() : (Math.abs(sl) * ct.getDonGia())) * sign;

        item.soLuong += sl;
        item.thanhTienBan += tt;
        if (ct.getDonGia() > 0) item.donGia = ct.getDonGia();
    }

    private void filterTableRows() {
        List<ThongKeMatHangBanRow> filtered = new ArrayList<>();
        boolean isAll = selectedCategoryName == null || "Tất cả".equalsIgnoreCase(selectedCategoryName.trim()) || "ALL".equalsIgnoreCase(selectedCategoryId);

        for (ThongKeMatHangBanRow r : allRows) {
            if (isAll) {
                filtered.add(r);
            } else {
                boolean matchName = r.getNhomHangName() != null && r.getNhomHangName().equalsIgnoreCase(selectedCategoryName);
                boolean matchId = selectedCategoryId != null && selectedCategoryId.equalsIgnoreCase(r.getNhomHangId());
                if (matchName || matchId) {
                    filtered.add(r);
                }
            }
        }

        rebuildTableData(filtered);
    }

    private void rebuildTableData(List<ThongKeMatHangBanRow> displayRows) {
        String[] headers = withRowNumber(visibleColumns);
        ReadOnlyTableModel model = new ReadOnlyTableModel(headers);

        double totalBan = 0.0;
        double totalNhap = 0.0;
        double totalLai = 0.0;

        for (int i = 0; i < displayRows.size(); i++) {
            ThongKeMatHangBanRow row = displayRows.get(i);
            row.setStt(i + 1);

            Object[] rowData = new Object[headers.length];
            rowData[0] = i + 1;
            for (int col = 0; col < visibleColumns.size(); col++) {
                rowData[col + 1] = getRowColumnValue(row, visibleColumns.get(col));
            }
            model.addRow(rowData);

            totalBan += row.getThanhTienBan();
            totalNhap += row.getThanhTienNhap();
            totalLai += row.getLai();
        }

        salesTable.setModel(model);
        configureWidthsAndRenderers(salesTable);

        // Update footer summary
        lblTongTienBanVal.setText(MONEY_FORMAT.format(totalBan));
        lblTongTienNhapVal.setText(MONEY_FORMAT.format(totalNhap));
        lblTongLaiBanHangVal.setText(MONEY_FORMAT.format(totalLai));
        lblTongLaiVal.setText(MONEY_FORMAT.format(totalLai));

        statusLabel.setText(displayRows.size() + " mặt hàng - Tổng tiền bán: " + MONEY_FORMAT.format(totalBan)
                + " đ | Tổng tiền nhập: " + MONEY_FORMAT.format(totalNhap)
                + " đ | Lãi: " + MONEY_FORMAT.format(totalLai) + " đ");
    }

    private Object getRowColumnValue(ThongKeMatHangBanRow row, String colName) {
        return switch (colName) {
            case "Mã hàng" -> text(row.getMaHang());
            case "Tên mặt hàng" -> text(row.getTenMatHang());
            case "Số lượng" -> row.getSoLuong();
            case "ĐVT" -> text(row.getDonViTinh());
            case "Đơn giá" -> row.getDonGia();
            case "Thành tiền bán" -> row.getThanhTienBan();
            case "Giá vốn" -> row.getGiaVon();
            case "Giảm giá %" -> row.getGiamGia();
            case "Thành tiền nhập" -> row.getThanhTienNhap();
            case "Tỉ lệ lãi" -> row.getTiLeLai();
            case "Lãi" -> row.getLai();
            default -> "";
        };
    }

    private void rebuildTableHeaders() {
        String[] headers = withRowNumber(visibleColumns);
        salesTable.setModel(new ReadOnlyTableModel(headers));
        configureWidthsAndRenderers(salesTable);
    }

    private void configureWidthsAndRenderers(JTable table) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer number = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number num) {
                    setText(NUMBER_FORMAT.format(num.doubleValue()));
                } else {
                    setText(text(value));
                }
            }
        };
        number.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            String name = text(tc.getHeaderValue());
            if (i == 0) {
                tc.setPreferredWidth(45);
                tc.setCellRenderer(center);
            } else if ("Mã hàng".equals(name)) {
                tc.setPreferredWidth(85);
                tc.setCellRenderer(left);
            } else if ("Tên mặt hàng".equals(name)) {
                tc.setPreferredWidth(230);
                tc.setCellRenderer(left);
            } else if ("ĐVT".equals(name)) {
                tc.setPreferredWidth(55);
                tc.setCellRenderer(center);
            } else if ("Số lượng".equals(name)) {
                tc.setPreferredWidth(65);
                tc.setCellRenderer(number);
            } else if ("Đơn giá".equals(name) || "Thành tiền bán".equals(name) || "Giá vốn".equals(name)
                    || "Thành tiền nhập".equals(name) || "Lãi".equals(name)) {
                tc.setPreferredWidth(95);
                tc.setCellRenderer(number);
            } else if ("Giảm giá %".equals(name) || "Tỉ lệ lãi".equals(name)) {
                tc.setPreferredWidth(70);
                tc.setCellRenderer(number);
            } else {
                tc.setPreferredWidth(100);
            }
        }
    }

    private void installColumnChooser(JTable table) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem chooseItem = new JMenuItem("Chọn cột hiển thị...");
        chooseItem.setIcon(IconHelper.getIcon("table_column.png", 14, 14));
        chooseItem.addActionListener(e -> openColumnChooser());
        popup.add(chooseItem);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            private void show(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }
        });
    }

    private void openColumnChooser() {
        ChonCotHienThiDialog dialog = new ChonCotHienThiDialog(
                SwingUtilities.getWindowAncestor(this),
                visibleColumns,
                ALL_COLUMNS,
                DEFAULT_COLUMNS
        );
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) return;
        if (dialog.getResultVisibleColumns().isEmpty()) {
            showWarning("Phải giữ lại ít nhất một cột hiển thị.");
            return;
        }

        visibleColumns = new ArrayList<>(dialog.getResultVisibleColumns());
        filterTableRows();
    }

    private void previewReport() {
        Date from = fromDate.getDate();
        Date to = toDate.getDate();
        if (from == null || to == null || from.after(to)) {
            showWarning("Vui lòng chọn khoảng ngày hợp lệ trước khi in báo cáo.");
            return;
        }

        String storeId = selectedStoreId();
        String nhomId = selectedCategoryId;

        printButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                // Generate Jasper report for sales statistics
                return reportService.generateTongHopMatHangBanTheoNgay(startOfDay(from), startOfDay(to), null, null);
            }

            @Override
            protected void done() {
                try {
                    JasperPrint print = get();
                    JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(ThongKeMatHangBanPanel.this),
                            "Báo cáo thống kê mặt hàng bán", Dialog.ModalityType.MODELESS);
                    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    dialog.setSize(1000, 750);
                    dialog.setLocationRelativeTo(ThongKeMatHangBanPanel.this);
                    dialog.getContentPane().add(new JRViewer(print));
                    dialog.setVisible(true);

                    ActivityLogService.log("Thống kê mặt hàng bán", "XEM/IN BÁO CÁO", "",
                            "Từ " + new SimpleDateFormat("dd/MM/yyyy").format(from)
                                    + " đến " + new SimpleDateFormat("dd/MM/yyyy").format(to)
                                    + " (" + selectedCategoryName + ")");
                } catch (Exception ex) {
                    showError("Không thể tạo báo cáo in", ex);
                } finally {
                    printButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private String selectedStoreId() {
        StoreOption option = (StoreOption) storeCombo.getSelectedItem();
        return option != null ? option.id : "";
    }

    private void setLoading(boolean loading, String message) {
        refreshButton.setEnabled(!loading);
        fromDate.setEnabled(!loading);
        toDate.setEnabled(!loading);
        storeCombo.setEnabled(!loading);
        categoryTree.setEnabled(!loading);
        statusLabel.setText(message);
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Thống kê mặt hàng bán", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(this,
                message + (ex != null && ex.getMessage() != null ? ": " + ex.getMessage() : ""),
                "Thống kê mặt hàng bán", JOptionPane.ERROR_MESSAGE);
    }

    private static String[] withRowNumber(List<String> columns) {
        String[] arr = new String[columns.size() + 1];
        arr[0] = "";
        for (int i = 0; i < columns.size(); i++) arr[i + 1] = columns.get(i);
        return arr;
    }

    private static Date startOfDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    // Helper classes
    private static class AggregatedItem {
        String maHang = "";
        String tenHang = "";
        String donViTinh = "";
        double soLuong = 0.0;
        double donGia = 0.0;
        double thanhTienBan = 0.0;
        double giaVon = 0.0;
        double giamGia = 0.0;
        String nhomHangId;
        String nhomHangName;
    }

    private static class CategoryNodeItem {
        final String id;
        final String name;

        CategoryNodeItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class StoreOption {
        final String id;
        final String name;

        StoreOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ReadOnlyTableModel extends DefaultTableModel {
        ReadOnlyTableModel(Object[] columnNames) {
            super(columnNames, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
