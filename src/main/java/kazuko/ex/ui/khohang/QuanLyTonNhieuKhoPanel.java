package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.khohang.TheKhoEntry;
import kazuko.ex.model.khohang.TonNhieuKhoItem;
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
import java.util.*;
import java.util.List;

public class QuanLyTonNhieuKhoPanel extends JPanel {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

    private final DecimalFormat dfMoney = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Top Filter Bar
    private JButton btnRefreshTop;
    private JTextField txtTimKiem;
    private JCheckBox chkChiHienThiTonKhacKhong;
    private JButton btnXuatExcel;

    // Left Category Tree
    private JTree treeNhomHang;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private String selectedNhomHangId = null;

    // Center Top Table: Tồn nhiều kho (Matrix)
    private JTable tableTonNhieuKho;
    private DefaultTableModel modelTonNhieuKho;
    private final List<KhoHang> listKhoHang = new ArrayList<>();
    private final List<TonNhieuKhoItem> listAllItems = new ArrayList<>();
    private final List<TonNhieuKhoItem> listFilteredItems = new ArrayList<>();
    private TonNhieuKhoItem currentSelectedTonKho = null;

    // Dynamic Summary Footer
    private JPanel pnlSummary;

    // Center Bottom Tab: Thẻ kho
    private JTabbedPane tabbedBottom;
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JButton btnRefreshTheKho;
    private JTable tableTheKho;
    private DefaultTableModel modelTheKho;

    public QuanLyTonNhieuKhoPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initComponents();
        setupEvents();
        loadTreeNhomHang();
        loadDataTonNhieuKho();
    }

    private void initComponents() {
        // 1. Top Filter Bar (Khớp ảnh Tồn nhiều kho)
        JPanel pnlTop = new JPanel(new BorderLayout(8, 0));
        pnlTop.setBackground(new Color(235, 242, 252));
        pnlTop.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(180, 205, 235)),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JPanel pnlTopLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTopLeft.setOpaque(false);

        btnRefreshTop = createMiniButton("Refresh", "arrow_refresh.png");

        JLabel lblTim = new JLabel("Tìm (F3):");
        lblTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimKiem.setPreferredSize(new Dimension(160, 24));

        chkChiHienThiTonKhacKhong = new JCheckBox("Chỉ hiển thị mặt hàng có tồn kho <> 0", true); // Checked by default in Image
        chkChiHienThiTonKhacKhong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChiHienThiTonKhacKhong.setOpaque(false);

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

        // 2. Main Split: Left (Category Tree) | Right (Matrix Table + Bottom Tab)
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(200);
        splitMain.setDividerSize(4);
        splitMain.setBorder(null);

        // Left Panel: Category Tree
        JPanel pnlLeft = createCategoryTreePanel();
        splitMain.setLeftComponent(pnlLeft);

        // Right Panel: Top (Matrix Table + Summary) & Bottom (Thẻ kho)
        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitRight.setDividerLocation(380);
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

        modelTonNhieuKho = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableTonNhieuKho = new JTable(modelTonNhieuKho);
        tableTonNhieuKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTonNhieuKho.setRowHeight(22);
        tableTonNhieuKho.setShowGrid(true);
        tableTonNhieuKho.setGridColor(new Color(230, 238, 248));
        tableTonNhieuKho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = tableTonNhieuKho.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(235, 242, 252));
        header.setForeground(new Color(40, 50, 70));
        header.setPreferredSize(new Dimension(100, 26));

        JScrollPane sp = new JScrollPane(tableTonNhieuKho);
        sp.setBorder(new MatteBorder(0, 0, 1, 0, new Color(210, 225, 240)));
        pnl.add(sp, BorderLayout.CENTER);

        // Summary Bar
        pnlSummary = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        pnlSummary.setBackground(new Color(242, 246, 252));
        pnlSummary.setBorder(new MatteBorder(1, 0, 1, 0, new Color(200, 218, 240)));
        pnl.add(pnlSummary, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createBottomTabsPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        tabbedBottom = new JTabbedPane(JTabbedPane.TOP);
        tabbedBottom.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Thẻ kho (Khớp ảnh)
        JPanel pnlTheKho = createTheKhoTab();
        tabbedBottom.addTab("Thẻ kho", pnlTheKho);

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
        tableTheKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTheKho.setRowHeight(22);
        tableTheKho.setShowGrid(true);
        tableTheKho.setGridColor(new Color(230, 238, 248));
        tableTheKho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTheKho.setSelectionBackground(new Color(210, 230, 252));
        tableTheKho.setSelectionForeground(Color.BLACK);

        JTableHeader th = tableTheKho.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(235, 242, 252));
        th.setForeground(new Color(40, 50, 70));
        th.setPreferredSize(new Dimension(100, 24));

        ChonCotHienThiDialog.attachToTableHeader(tableTheKho, List.of(theKhoCols));
        pnl.add(new JScrollPane(tableTheKho), BorderLayout.CENTER);

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
        btnRefreshTop.addActionListener(e -> loadDataTonNhieuKho());

        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilters(); }
        });

        chkChiHienThiTonKhacKhong.addActionListener(e -> applyFilters());
        btnXuatExcel.addActionListener(e -> exportToExcel());

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

        tableTonNhieuKho.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableTonNhieuKho.getSelectedRow();
                if (row >= 0 && row < listFilteredItems.size()) {
                    currentSelectedTonKho = listFilteredItems.get(row);
                    loadTheKhoData();
                } else {
                    currentSelectedTonKho = null;
                    modelTheKho.setRowCount(0);
                }
            }
        });

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

        @Override public String toString() { return name; }
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

    public void loadDataTonNhieuKho() {
        listKhoHang.clear();
        listKhoHang.addAll(khoHangDAO.getAllActive());

        listAllItems.clear();
        listAllItems.addAll(donHangDAO.getTonNhieuKhoMatrix());

        buildTableColumns();
        applyFilters();
    }

    private void buildTableColumns() {
        List<String> cols = new ArrayList<>();
        cols.add("");
        cols.add("Mã hàng");
        cols.add("Mặt hàng");
        cols.add("ĐVT");

        // Dynamic warehouse columns
        for (KhoHang k : listKhoHang) {
            cols.add(k.getName());
        }

        cols.add("Tổng tồn");
        cols.add("Tồn 2 đvt");
        cols.add("ĐVT");
        cols.add("Bảo hành");
        cols.add("Quy đổi");

        modelTonNhieuKho.setColumnIdentifiers(cols.toArray());

        // Custom Cell Renderer: Tô màu hồng cho dòng tồn âm (Ảnh 1), highlight xanh cho dòng chọn
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);

                TonNhieuKhoItem item = (row >= 0 && row < listFilteredItems.size()) ? listFilteredItems.get(row) : null;
                boolean isNegative = (item != null && item.getTongTon() < 0);

                if (isSelected) {
                    c.setBackground(new Color(0, 120, 215)); // Highlight xanh dương đậm
                    c.setForeground(Color.WHITE);
                } else if (isNegative) {
                    c.setBackground(new Color(255, 205, 215)); // Màu hồng nhạt cảnh báo âm kho (Ảnh 1)
                    c.setForeground(new Color(130, 20, 30));
                } else if (row % 2 == 1) {
                    c.setBackground(new Color(245, 250, 255));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                // Cột STT
                if (col == 0) {
                    if (isSelected) {
                        setText("▶ " + (value != null ? value.toString() : ""));
                    } else {
                        setText(value != null ? value.toString() : "");
                    }
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (col >= 4 && col <= 4 + listKhoHang.size()) {
                    // Cột số lượng (Tồn từng kho và Tổng tồn)
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 4 + listKhoHang.size() + 1 || col == 4 + listKhoHang.size() + 4) {
                    // Tồn 2 đvt, Quy đổi
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < tableTonNhieuKho.getColumnCount(); i++) {
            tableTonNhieuKho.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        tableTonNhieuKho.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableTonNhieuKho.getColumnModel().getColumn(1).setPreferredWidth(85);
        tableTonNhieuKho.getColumnModel().getColumn(2).setPreferredWidth(210);
        tableTonNhieuKho.getColumnModel().getColumn(3).setPreferredWidth(55);

        ChonCotHienThiDialog.attachToTableHeader(tableTonNhieuKho, cols);
    }

    private void applyFilters() {
        listFilteredItems.clear();
        String query = txtTimKiem.getText().trim().toLowerCase();
        boolean onlyStockNonZero = chkChiHienThiTonKhacKhong.isSelected();

        Map<String, Double> sumByKho = new HashMap<>();
        double sumTongTon = 0.0;
        double sumTon2Dvt = 0.0;

        for (TonNhieuKhoItem item : listAllItems) {
            if (onlyStockNonZero) {
                boolean hasStock = Math.abs(item.getTongTon()) > 0.0001;
                if (!hasStock) {
                    for (KhoHang k : listKhoHang) {
                        if (Math.abs(item.getTonKho(k.getId())) > 0.0001) {
                            hasStock = true;
                            break;
                        }
                    }
                }
                if (!hasStock) continue;
            }

            if (selectedNhomHangId != null && !selectedNhomHangId.trim().isEmpty()) {
                if (item.getNhomHangId() == null || !item.getNhomHangId().trim().equalsIgnoreCase(selectedNhomHangId.trim())) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                boolean matchMa = item.getMaHang() != null && item.getMaHang().toLowerCase().contains(query);
                boolean matchTen = item.getTenHang() != null && item.getTenHang().toLowerCase().contains(query);
                if (!matchMa && !matchTen) continue;
            }

            listFilteredItems.add(item);
            sumTongTon += item.getTongTon();
            sumTon2Dvt += item.getTon2Dvt();

            for (KhoHang k : listKhoHang) {
                double val = item.getTonKho(k.getId());
                sumByKho.put(k.getId(), sumByKho.getOrDefault(k.getId(), 0.0) + val);
            }
        }

        // Fill model data
        modelTonNhieuKho.setRowCount(0);
        for (int i = 0; i < listFilteredItems.size(); i++) {
            TonNhieuKhoItem item = listFilteredItems.get(i);
            String stt = String.format("%02d", i + 1);

            List<Object> rowData = new ArrayList<>();
            rowData.add(stt);
            rowData.add(item.getMaHang());
            rowData.add(item.getTenHang());
            rowData.add(item.getDonViTinh() != null ? item.getDonViTinh() : "");

            // Warehouse stock values
            for (KhoHang k : listKhoHang) {
                double val = item.getTonKho(k.getId());
                rowData.add(formatNum(val));
            }

            rowData.add(formatNum(item.getTongTon()));
            rowData.add(formatNum(item.getTon2Dvt()));
            rowData.add(item.getDonViTinh2() != null ? item.getDonViTinh2() : "");
            rowData.add(item.getBaoHanh() != null ? item.getBaoHanh() : "");
            rowData.add(item.getQuyDoi() > 0 ? formatNum(item.getQuyDoi()) : "");

            modelTonNhieuKho.addRow(rowData.toArray());
        }

        // Update Summary Bar
        updateSummaryBar(sumByKho, sumTongTon, sumTon2Dvt);

        if (!listFilteredItems.isEmpty()) {
            tableTonNhieuKho.setRowSelectionInterval(0, 0);
        } else {
            currentSelectedTonKho = null;
            modelTheKho.setRowCount(0);
        }
    }

    private void updateSummaryBar(Map<String, Double> sumByKho, double sumTongTon, double sumTon2Dvt) {
        pnlSummary.removeAll();

        JLabel lblTitle = new JLabel("TỔNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(20, 50, 95));
        pnlSummary.add(lblTitle);

        for (KhoHang k : listKhoHang) {
            double total = sumByKho.getOrDefault(k.getId(), 0.0);
            pnlSummary.add(new JLabel(k.getName() + ":"));
            pnlSummary.add(createSummaryBox(formatNum(total)));
        }

        pnlSummary.add(new JLabel("Tổng tồn:"));
        pnlSummary.add(createSummaryBox(formatNum(sumTongTon)));

        pnlSummary.add(new JLabel("Tồn 2 ĐVT:"));
        pnlSummary.add(createSummaryBox(formatNum(sumTon2Dvt)));

        pnlSummary.revalidate();
        pnlSummary.repaint();
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

        Date tuNgay = dpkTuNgay.getDate();
        Date denNgay = dpkDenNgay.getDate();

        List<TheKhoEntry> entries = donHangDAO.getTheKho(currentSelectedTonKho.getMatHangId(), null, tuNgay, denNgay);
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

    private void exportToExcel() {
        if (listFilteredItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel/CSV Tồn nhiều kho");
        fc.setSelectedFile(new File("BaoCaoTonNhieuKho_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(f, StandardCharsets.UTF_8)) {
                pw.write('\ufeff'); // BOM cho tiếng Việt Unicode

                // Header
                StringBuilder sbHdr = new StringBuilder("STT,Mã hàng,Tên hàng,ĐVT");
                for (KhoHang k : listKhoHang) {
                    sbHdr.append(",\"").append(k.getName()).append("\"");
                }
                sbHdr.append(",Tổng tồn,Tồn 2 ĐVT");
                pw.println(sbHdr);

                // Rows
                for (int i = 0; i < listFilteredItems.size(); i++) {
                    TonNhieuKhoItem item = listFilteredItems.get(i);
                    StringBuilder sbRow = new StringBuilder();
                    sbRow.append(i + 1).append(",");
                    sbRow.append("\"").append(item.getMaHang() != null ? item.getMaHang() : "").append("\",");
                    sbRow.append("\"").append(item.getTenHang() != null ? item.getTenHang().replace("\"", "\"\"") : "").append("\",");
                    sbRow.append("\"").append(item.getDonViTinh() != null ? item.getDonViTinh() : "").append("\"");

                    for (KhoHang k : listKhoHang) {
                        double val = item.getTonKho(k.getId());
                        sbRow.append(",").append(String.format(Locale.US, "%.2f", val));
                    }

                    sbRow.append(",").append(String.format(Locale.US, "%.2f", item.getTongTon()));
                    sbRow.append(",").append(String.format(Locale.US, "%.2f", item.getTon2Dvt()));

                    pw.println(sbRow);
                }

                JOptionPane.showMessageDialog(this, "Xuất excel thành công vào file:\n" + f.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
