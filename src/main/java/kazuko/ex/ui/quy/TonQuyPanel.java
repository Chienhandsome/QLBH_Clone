package kazuko.ex.ui.quy;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.dao.TaiKhoanNganHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.TaiKhoanNganHang;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.ExcelExporter;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TonQuyPanel extends JPanel {

    public static class TransactionItem {
        private String id;
        private String soPhieu;
        private Date ngay;
        private String dienGiai;
        private double thu;
        private double chi;
        private String loaiNguonTien; // "Tiền mặt", "Quẹt thẻ", "Ngân hàng"
        private String taiKhoanNganHangId;
        private String tenTaiKhoanNganHang;
        private String cuaHangId;
        private String type; // "HOA_DON", "PHIEU_THU", "PHIEU_CHI"
        private Object rawObject;

        public TransactionItem() {}

        public TransactionItem(String id, String soPhieu, Date ngay, String dienGiai, double thu, double chi,
                               String loaiNguonTien, String taiKhoanNganHangId, String tenTaiKhoanNganHang,
                               String cuaHangId, String type, Object rawObject) {
            this.id = id;
            this.soPhieu = soPhieu;
            this.ngay = ngay;
            this.dienGiai = dienGiai;
            this.thu = thu;
            this.chi = chi;
            this.loaiNguonTien = loaiNguonTien;
            this.taiKhoanNganHangId = taiKhoanNganHangId;
            this.tenTaiKhoanNganHang = tenTaiKhoanNganHang;
            this.cuaHangId = cuaHangId;
            this.type = type;
            this.rawObject = rawObject;
        }

        public String getId() { return id; }
        public String getSoPhieu() { return soPhieu; }
        public Date getNgay() { return ngay; }
        public String getDienGiai() { return dienGiai; }
        public double getThu() { return thu; }
        public double getChi() { return chi; }
        public String getLoaiNguonTien() { return loaiNguonTien; }
        public String getTaiKhoanNganHangId() { return taiKhoanNganHangId; }
        public String getTenTaiKhoanNganHang() { return tenTaiKhoanNganHang; }
        public String getCuaHangId() { return cuaHangId; }
        public String getType() { return type; }
        public Object getRawObject() { return rawObject; }
    }

    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final TaiKhoanNganHangDAO taiKhoanNganHangDAO = new TaiKhoanNganHangDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    // In-memory sample invoices for accurate cash book demonstration matching screenshot
    private static final List<TransactionItem> SAMPLE_INVOICE_TRANSACTIONS = new ArrayList<>();
    static {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            SAMPLE_INVOICE_TRANSACTIONS.add(new TransactionItem("hd-001", "0126000001", sdf.parse("27/08/2026 10:15"), "Xuất bán hàng", 187000.0, 0.0, "Tiền mặt", null, null, null, "HOA_DON", null));
            SAMPLE_INVOICE_TRANSACTIONS.add(new TransactionItem("hd-002", "0126000002", sdf.parse("28/08/2026 09:30"), "Xuất bán hàng", 192000.0, 0.0, "Tiền mặt", null, null, null, "HOA_DON", null));
            SAMPLE_INVOICE_TRANSACTIONS.add(new TransactionItem("hd-003", "0126000003", sdf.parse("28/08/2026 11:45"), "Xuất bán hàng", 403000.0, 0.0, "Tiền mặt", null, null, null, "HOA_DON", null));
        } catch (Exception ignored) {}
    }

    // Controls
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JComboBox<Object> cbCuaHangFilter;
    private JButton btnRefresh;

    private JTree treeNguonTien;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;
    private String currentSelectedSourceType = "Tất cả";
    private String currentSelectedBankId = null;

    private JTable tableTonQuy;
    private DefaultTableModel tableModel;
    private List<TransactionItem> currentDisplayedTransactions = new ArrayList<>();

    // 4 Summary Value Labels
    private JLabel lblTonDauValue;
    private JLabel lblTongThuValue;
    private JLabel lblTongChiValue;
    private JLabel lblTonQuyValue;

    private double baseTonDau = 3907000.0; // Default initial balance matching screenshot
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public TonQuyPanel() {
        initComponents();
        loadStoreFilterData();
        loadTreeData();
        loadTonQuyData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 252));

        // ==================== 1. TOP HEADER BANNER & FILTER TOOLBAR ====================
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title Banner with Coins Icon
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlTitle.setBackground(Color.WHITE);
        pnlTitle.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 235)));

        JLabel lblCoinsIcon = new JLabel(IconHelper.getIcon("btnCard.Icon.png", 24, 24));
        if (lblCoinsIcon.getIcon() == null) {
            lblCoinsIcon.setIcon(IconHelper.getIcon("1282529873_objects.png", 24, 24));
        }
        JLabel lblTitleText = new JLabel("Tồn quỹ bao gồm tất cả thu, chi, thanh toán tiền mua hàng, thu tiền hóa đơn, đặt hàng...");
        lblTitleText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitleText.setForeground(new Color(30, 40, 60));

        pnlTitle.add(lblCoinsIcon);
        pnlTitle.add(lblTitleText);
        topContainer.add(pnlTitle);

        // Filter Bar (Từ ngày, Đến ngày, Cửa hàng, Refresh)
        JPanel pnlFilter = new JPanel(new BorderLayout());
        pnlFilter.setBackground(new Color(225, 238, 252));
        pnlFilter.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        pnlFilter.setPreferredSize(new Dimension(1000, 36));

        JPanel pnlLeftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlLeftFilter.setOpaque(false);

        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        dpkTuNgay = new DatePickerField();
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        dpkTuNgay.setDate(calStart.getTime());
        dpkTuNgay.setPreferredSize(new Dimension(110, 24));
        dpkTuNgay.addPropertyChangeListener("date", evt -> loadTonQuyData());

        JLabel lblDen = new JLabel("Đến");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(null); // Optional / blank as seen in screenshot
        dpkDenNgay.setPreferredSize(new Dimension(110, 24));
        dpkDenNgay.addPropertyChangeListener("date", evt -> loadTonQuyData());

        JLabel lblCuaHang = new JLabel("Cửa hàng:");
        lblCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cbCuaHangFilter = new JComboBox<>();
        cbCuaHangFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCuaHangFilter.setPreferredSize(new Dimension(180, 24));
        cbCuaHangFilter.addActionListener(e -> loadTonQuyData());

        pnlLeftFilter.add(lblTu);
        pnlLeftFilter.add(dpkTuNgay);
        pnlLeftFilter.add(lblDen);
        pnlLeftFilter.add(dpkDenNgay);
        pnlLeftFilter.add(lblCuaHang);
        pnlLeftFilter.add(cbCuaHangFilter);

        JPanel pnlRightFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlRightFilter.setOpaque(false);

        btnRefresh = new JButton("Refresh", IconHelper.getIcon("Refresh.png", 14, 14));
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 10, 3, 10)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            loadTreeData();
            loadTonQuyData();
        });

        pnlRightFilter.add(btnRefresh);

        pnlFilter.add(pnlLeftFilter, BorderLayout.WEST);
        pnlFilter.add(pnlRightFilter, BorderLayout.EAST);
        topContainer.add(pnlFilter);

        add(topContainer, BorderLayout.NORTH);

        // ==================== 2. CENTER SPLIT: TREE (LEFT) & TABLE (RIGHT) ====================
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setDividerLocation(180);
        centerSplit.setDividerSize(5);

        // Left Tree Panel: Nguồn tiền
        JPanel leftTreePanel = new JPanel(new BorderLayout());
        leftTreePanel.setBackground(Color.WHITE);
        leftTreePanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(190, 210, 235)));

        rootTreeNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeNguonTien = new JTree(treeModel);
        treeNguonTien.setRootVisible(false);
        treeNguonTien.setShowsRootHandles(true);
        treeNguonTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNguonTien.setRowHeight(22);
        treeNguonTien.setCellRenderer(new DefaultTreeCellRenderer() {
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
                    } else if ("Tiền mặt".equals(userObj)) {
                        label.setText("Tiền mặt");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    } else if ("Quẹt thẻ".equals(userObj)) {
                        label.setText("Quẹt thẻ");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    } else if ("Ngân hàng".equals(userObj)) {
                        label.setText("Ngân hàng");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    } else if (userObj instanceof TaiKhoanNganHang) {
                        TaiKhoanNganHang tk = (TaiKhoanNganHang) userObj;
                        label.setText(tk.getName());
                        label.setIcon(IconHelper.getIcon("report--pencil.png", 14, 14));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }
                return label;
            }
        });

        treeNguonTien.addTreeSelectionListener(e -> {
            TreePath path = treeNguonTien.getSelectionPath();
            if (path == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObj = node.getUserObject();

            if ("Tất cả".equals(userObj)) {
                currentSelectedSourceType = "Tất cả";
                currentSelectedBankId = null;
            } else if ("Tiền mặt".equals(userObj)) {
                currentSelectedSourceType = "Tiền mặt";
                currentSelectedBankId = null;
            } else if ("Quẹt thẻ".equals(userObj)) {
                currentSelectedSourceType = "Quẹt thẻ";
                currentSelectedBankId = null;
            } else if ("Ngân hàng".equals(userObj)) {
                currentSelectedSourceType = "Ngân hàng";
                currentSelectedBankId = null;
            } else if (userObj instanceof TaiKhoanNganHang) {
                currentSelectedSourceType = "TaiKhoan";
                currentSelectedBankId = ((TaiKhoanNganHang) userObj).getId();
            }
            loadTonQuyData();
        });

        JScrollPane treeScroll = new JScrollPane(treeNguonTien);
        treeScroll.setBorder(null);
        leftTreePanel.add(treeScroll, BorderLayout.CENTER);
        centerSplit.setLeftComponent(leftTreePanel);

        // Right Table Panel
        String[] columns = {"", "Số phiếu", "Ngày", "Diễn giải", "Thu", "Chi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTonQuy = new JTable(tableModel);
        tableTonQuy.setRowHeight(22);
        tableTonQuy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTonQuy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTonQuy.setShowGrid(true);
        tableTonQuy.setGridColor(new Color(225, 232, 240));
        tableTonQuy.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Column widths
        int[] widths = {35, 120, 110, 260, 120, 120};
        for (int i = 0; i < widths.length; i++) {
            if (i < tableTonQuy.getColumnModel().getColumnCount()) {
                tableTonQuy.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }

        // Table Header Styling & Quick Menu
        JTableHeader header = tableTonQuy.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(230, 240, 252));
        header.setForeground(new Color(30, 60, 110));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 26));

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }
        });

        // Custom Cell Renderer
        tableTonQuy.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    // STT kèm con trỏ ▶
                    JLabel lbl = (JLabel) c;
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    if (isSelected) {
                        lbl.setText("▶ " + (row + 1));
                        lbl.setBackground(new Color(220, 235, 255));
                        lbl.setForeground(new Color(20, 50, 120));
                    } else {
                        lbl.setText(String.valueOf(row + 1));
                        lbl.setBackground(new Color(245, 248, 252));
                        lbl.setForeground(Color.DARK_GRAY);
                    }
                    return lbl;
                }

                if (column == 1 && isSelected) {
                    // Cột Số phiếu khi được chọn có màu cam đất đặc trưng
                    c.setBackground(new Color(188, 104, 52));
                    c.setForeground(Color.WHITE);
                    return c;
                }

                if (column == 4 || column == 5) {
                    // Thu, Chi căn phải
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                } else if (column == 2) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (isSelected) {
                    c.setBackground(new Color(220, 235, 255));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 254));
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        tableTonQuy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onOpenTransactionDetail();
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

        JScrollPane tableScroll = new JScrollPane(tableTonQuy);
        tableScroll.setBorder(null);
        centerSplit.setRightComponent(tableScroll);

        add(centerSplit, BorderLayout.CENTER);

        // ==================== 3. BOTTOM SUMMARY FOOTER (4 YELLOW BOXES) ====================
        JPanel bottomContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 6));
        bottomContainer.setBackground(new Color(225, 238, 252));
        bottomContainer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(190, 210, 235)));
        bottomContainer.setPreferredSize(new Dimension(1000, 52));

        JPanel pnlTonDau = createSummaryBox("Tồn đầu", "3,907,000", Color.BLACK);
        lblTonDauValue = (JLabel) pnlTonDau.getComponent(1);

        JPanel pnlTongThu = createSummaryBox("Tổng thu", "792,000", Color.BLACK);
        lblTongThuValue = (JLabel) pnlTongThu.getComponent(1);

        JPanel pnlTongChi = createSummaryBox("Tổng chi", "10,134", Color.BLACK);
        lblTongChiValue = (JLabel) pnlTongChi.getComponent(1);

        JPanel pnlTonQuy = createSummaryBox("Tồn quỹ", "4,688,866", Color.BLACK);
        lblTonQuyValue = (JLabel) pnlTonQuy.getComponent(1);

        bottomContainer.add(pnlTonDau);
        bottomContainer.add(pnlTongThu);
        bottomContainer.add(pnlTongChi);
        bottomContainer.add(pnlTonQuy);

        add(bottomContainer, BorderLayout.SOUTH);
    }

    private JPanel createSummaryBox(String title, String initialValue, Color textColor) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(30, 40, 60));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pnlValue = new JPanel(new BorderLayout());
        pnlValue.setBackground(new Color(254, 253, 210)); // Đặc trưng màu vàng kem như hình
        pnlValue.setBorder(new LineBorder(new Color(200, 200, 160), 1));
        pnlValue.setPreferredSize(new Dimension(140, 24));
        pnlValue.setMaximumSize(new Dimension(140, 24));

        JLabel lblValue = new JLabel(initialValue, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(textColor);

        pnlValue.add(lblValue, BorderLayout.CENTER);

        box.add(lblTitle);
        box.add(Box.createVerticalStrut(2));
        box.add(pnlValue);

        return box;
    }

    private void loadStoreFilterData() {
        cbCuaHangFilter.removeAllItems();
        cbCuaHangFilter.addItem("Tất cả cửa hàng");
        List<CuaHang> stores = cuaHangDAO.getAll();
        for (CuaHang ch : stores) {
            cbCuaHangFilter.addItem(ch);
        }
        cbCuaHangFilter.setSelectedIndex(0);
    }

    public void loadTreeData() {
        rootTreeNode.removeAllChildren();

        DefaultMutableTreeNode nodeTatCa = new DefaultMutableTreeNode("Tất cả");
        rootTreeNode.add(nodeTatCa);

        DefaultMutableTreeNode nodeTienMat = new DefaultMutableTreeNode("Tiền mặt");
        rootTreeNode.add(nodeTienMat);

        DefaultMutableTreeNode nodeQuetThe = new DefaultMutableTreeNode("Quẹt thẻ");
        rootTreeNode.add(nodeQuetThe);

        DefaultMutableTreeNode nodeNganHang = new DefaultMutableTreeNode("Ngân hàng");
        List<TaiKhoanNganHang> bankAccounts = taiKhoanNganHangDAO.getAllActive();
        for (TaiKhoanNganHang tk : bankAccounts) {
            nodeNganHang.add(new DefaultMutableTreeNode(tk));
        }
        rootTreeNode.add(nodeNganHang);

        treeModel.reload();
        treeNguonTien.setSelectionPath(new TreePath(nodeTatCa.getPath()));
    }

    public void loadTonQuyData() {
        List<TransactionItem> allTransactions = new ArrayList<>();

        // 1. Load sample invoice transactions
        allTransactions.addAll(SAMPLE_INVOICE_TRANSACTIONS);

        // 2. Load live receipts from PhieuThuDAO
        for (PhieuThu pt : phieuThuDAO.getAll()) {
            String source = pt.isChuyenVaoTaiKhoan() ? "Ngân hàng" : "Tiền mặt";
            String note = (pt.getLyDoThu() != null && !pt.getLyDoThu().trim().isEmpty()) ? pt.getLyDoThu() :
                    (pt.getTenPhanLoai() != null ? pt.getTenPhanLoai() : "Thu tiền");
            allTransactions.add(new TransactionItem(
                    pt.getId(),
                    pt.getSoPhieu(),
                    pt.getNgay(),
                    note,
                    pt.getSoTien(),
                    0.0,
                    source,
                    pt.getTaiKhoanNganHangId(),
                    pt.getTenTaiKhoanNganHang(),
                    pt.getCuaHangId(),
                    "PHIEU_THU",
                    pt
            ));
        }

        // 3. Load live payment vouchers from PhieuChiDAO
        for (PhieuChi pc : phieuChiDAO.getAll()) {
            String source = pc.isChuyenTuTaiKhoan() ? "Ngân hàng" : "Tiền mặt";
            String note = (pc.getLyDoChi() != null && !pc.getLyDoChi().trim().isEmpty()) ? pc.getLyDoChi() :
                    (pc.getTenPhanLoai() != null ? pc.getTenPhanLoai() : "Chi tiền");
            allTransactions.add(new TransactionItem(
                    pc.getId(),
                    pc.getSoPhieu(),
                    pc.getNgay(),
                    note,
                    0.0,
                    pc.getSoTien(),
                    source,
                    pc.getTaiKhoanNganHangId(),
                    pc.getTenTaiKhoanNganHang(),
                    pc.getCuaHangId(),
                    "PHIEU_CHI",
                    pc
            ));
        }

        // 4. Sort transactions by Date Ascending
        allTransactions.sort(Comparator.comparing(TransactionItem::getNgay, Comparator.nullsLast(Comparator.naturalOrder())));

        // 5. Date filter & Store filter
        Date tuNgay = dpkTuNgay.getDate() != null ? getStartOfDay(dpkTuNgay.getDate()) : null;
        Date denNgay = dpkDenNgay.getDate() != null ? getEndOfDay(dpkDenNgay.getDate()) : null;

        Object selectedStoreObj = cbCuaHangFilter.getSelectedItem();
        String selectedStoreId = (selectedStoreObj instanceof CuaHang) ? ((CuaHang) selectedStoreObj).getId() : null;

        List<TransactionItem> filtered = allTransactions.stream().filter(item -> {
            // Lọc ngày
            if (tuNgay != null && item.getNgay() != null && item.getNgay().before(tuNgay)) return false;
            if (denNgay != null && item.getNgay() != null && item.getNgay().after(denNgay)) return false;

            // Lọc cửa hàng
            if (selectedStoreId != null && item.getCuaHangId() != null && !selectedStoreId.equals(item.getCuaHangId())) {
                return false;
            }

            // Lọc theo Nguồn tiền
            if ("Tiền mặt".equals(currentSelectedSourceType)) {
                return "Tiền mặt".equalsIgnoreCase(item.getLoaiNguonTien());
            } else if ("Quẹt thẻ".equals(currentSelectedSourceType)) {
                return "Quẹt thẻ".equalsIgnoreCase(item.getLoaiNguonTien());
            } else if ("Ngân hàng".equals(currentSelectedSourceType)) {
                return "Ngân hàng".equalsIgnoreCase(item.getLoaiNguonTien());
            } else if ("TaiKhoan".equals(currentSelectedSourceType) && currentSelectedBankId != null) {
                return currentSelectedBankId.equals(item.getTaiKhoanNganHangId());
            }

            return true;
        }).collect(Collectors.toList());

        currentDisplayedTransactions = filtered;

        // 6. Populate Table
        tableModel.setRowCount(0);
        double totalThu = 0.0;
        double totalChi = 0.0;

        for (TransactionItem item : currentDisplayedTransactions) {
            String ngayStr = item.getNgay() != null ? sdfDate.format(item.getNgay()) : "";
            totalThu += item.getThu();
            totalChi += item.getChi();

            tableModel.addRow(new Object[]{
                    "",
                    item.getSoPhieu() != null ? item.getSoPhieu() : "",
                    ngayStr,
                    item.getDienGiai() != null ? item.getDienGiai() : "",
                    CurrencyUtil.format(item.getThu()),
                    CurrencyUtil.format(item.getChi())
            });
        }

        if (!currentDisplayedTransactions.isEmpty()) {
            tableTonQuy.setRowSelectionInterval(0, 0);
        }

        // 7. Calculate Summary Totals
        double tonDau = baseTonDau;
        double tonCuoi = tonDau + totalThu - totalChi;

        lblTonDauValue.setText(CurrencyUtil.format(tonDau));
        lblTongThuValue.setText(CurrencyUtil.format(totalThu));
        lblTongChiValue.setText(CurrencyUtil.format(totalChi));
        lblTonQuyValue.setText(CurrencyUtil.format(tonCuoi));
    }

    private void onOpenTransactionDetail() {
        int row = tableTonQuy.getSelectedRow();
        if (row < 0 || row >= currentDisplayedTransactions.size()) return;
        TransactionItem item = currentDisplayedTransactions.get(row);

        if ("PHIEU_THU".equals(item.getType()) && item.getRawObject() instanceof PhieuThu) {
            TaoPhieuThuDialog dlg = new TaoPhieuThuDialog(SwingUtilities.getWindowAncestor(this), (PhieuThu) item.getRawObject(), this::loadTonQuyData);
            dlg.setVisible(true);
        } else if ("PHIEU_CHI".equals(item.getType()) && item.getRawObject() instanceof PhieuChi) {
            TaoPhieuChiDialog dlg = new TaoPhieuChiDialog(SwingUtilities.getWindowAncestor(this), (PhieuChi) item.getRawObject(), this::loadTonQuyData);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "<html><b>Chứng từ: " + item.getSoPhieu() + "</b><br>" +
                            "Ngày: " + (item.getNgay() != null ? sdfDate.format(item.getNgay()) : "") + "<br>" +
                            "Diễn giải: " + item.getDienGiai() + "<br>" +
                            "Số tiền: " + CurrencyUtil.format(item.getThu() > 0 ? item.getThu() : item.getChi()) + " đ</html>",
                    "Chi tiết giao dịch", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showTablePopup(MouseEvent e) {
        int row = tableTonQuy.rowAtPoint(e.getPoint());
        if (row >= 0 && row < tableTonQuy.getRowCount()) {
            tableTonQuy.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem miDetail = new JMenuItem("Xem chi tiết chứng từ", IconHelper.getIcon("report--pencil.png", 16, 16));
        miDetail.addActionListener(ev -> onOpenTransactionDetail());
        popup.add(miDetail);

        popup.addSeparator();

        JMenuItem miExcel = new JMenuItem("Xuất Excel", IconHelper.getIcon("export-excel.png", 16, 16));
        miExcel.addActionListener(ev -> ExcelExporter.exportTable(tableTonQuy, "So_Quy_Ton_Quy", "Tồn quỹ", this));
        popup.add(miExcel);

        JMenuItem miPrint = new JMenuItem("In sổ quỹ", IconHelper.getIcon("printer.png", 16, 16));
        miPrint.addActionListener(ev -> {
            try {
                tableTonQuy.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        popup.add(miPrint);

        popup.addSeparator();

        JMenuItem miRefresh = new JMenuItem("Làm mới (F5)", IconHelper.getIcon("Refresh.png", 16, 16));
        miRefresh.addActionListener(ev -> {
            loadTreeData();
            loadTonQuyData();
        });
        popup.add(miRefresh);

        popup.show(tableTonQuy, e.getX(), e.getY());
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
