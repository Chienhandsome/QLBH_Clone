package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.ExcelExporter;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PhieuXuatKhoDialog extends JDialog {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private DonHang currentDonHang;
    private final Runnable onDataSaved;

    // Master Form fields
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private JComboBox<KhoHang> cbKhoXuat;
    private JComboBox<NhanVien> cbNhanVienXuat;
    private JComboBox<KhachHang> cbNhaCungCap;
    private JTextField txtDienGiai;
    private JTextField txtGhiChu;

    // Quick add controls
    private JTextField txtTimF3;
    private JSpinner spSoLuong;
    private JButton btnThem;

    // Top Right Summary
    private JLabel lblTienHangVal;
    private JTextField txtTiLeGiamGia;
    private JTextField txtTienGiamGia;
    private JLabel lblTongCongVal;
    private JTextField txtTienThanhToan;
    private boolean isUpdatingTotals = false;

    // Left table: Catalog
    private JTable tableCatalog;
    private DefaultTableModel catalogModel;
    private List<MatHang> allCatalogItems = new ArrayList<>();
    private List<MatHang> filteredCatalogItems = new ArrayList<>();

    // Right table: Line items
    private JTable tableChiTiet;
    private DefaultTableModel chiTietModel;
    private final List<DonHangChiTiet> lineItems = new ArrayList<>();

    private final DecimalFormat df = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public PhieuXuatKhoDialog(Window parent, Runnable onDataSaved) {
        this(parent, null, onDataSaved);
    }

    public PhieuXuatKhoDialog(Window parent, DonHang donHang, Runnable onDataSaved) {
        super(parent, donHang == null ? "PHIẾU XUẤT KHO - THÊM MỚI" : "PHIẾU XUẤT KHO - CHỈNH SỬA", ModalityType.APPLICATION_MODAL);
        this.currentDonHang = donHang;
        this.onDataSaved = onDataSaved;

        initComponents();
        setupKeyBindings();
        loadDropdownData();
        loadCatalogData();

        if (currentDonHang != null) {
            populateExistingData();
        } else {
            resetForNew();
        }
    }

    private void initComponents() {
        setSize(1240, 720);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(230, 240, 252));

        // ==================== 1. TOP HEADER BANNER ====================
        JPanel topHeaderPanel = new JPanel(new BorderLayout());
        topHeaderPanel.setBackground(new Color(230, 240, 252));

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Color.WHITE);
        banner.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 235)));
        banner.setPreferredSize(new Dimension(1000, 38));

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlTitle.setOpaque(false);
        JLabel lblIcon = new JLabel(IconHelper.getIcon("navigation-180-frame.png", 22, 22));
        if (lblIcon.getIcon() == null) {
            lblIcon.setIcon(IconHelper.getIcon("btnCard.Icon.png", 22, 22));
        }
        JLabel lblTitle = new JLabel("Phiếu xuất kho");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(20, 45, 85));
        pnlTitle.add(lblIcon);
        pnlTitle.add(lblTitle);

        JLabel lblShortcuts = new JLabel("(Ctrl+S: Lưu&Mới, Ctrl+L: Lưu, Ctrl+N: Mới, Ctrl+P: In, ESC: Thoát)  ");
        lblShortcuts.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblShortcuts.setForeground(new Color(110, 125, 145));

        banner.add(pnlTitle, BorderLayout.WEST);
        banner.add(lblShortcuts, BorderLayout.EAST);
        topHeaderPanel.add(banner, BorderLayout.NORTH);

        // Navigation & Action mini toolbar
        JPanel pnlNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        pnlNav.setBackground(new Color(238, 244, 252));
        pnlNav.setBorder(new MatteBorder(0, 0, 1, 0, new Color(210, 225, 240)));

        JButton btnPhimTat = createNavButton("Phím tắt ▾", null);
        JButton btnPrev = createNavButton("◂ Trước (F10)", "report--pencil.png");
        JButton btnNext = createNavButton("Sau (F11) ▸", "report--pencil.png");
        JButton btnTaoMoi = createNavButton("+ Tạo mới", "plus.png");
        JButton btnSaoChep = createNavButton("Sao chép", "copy.png");
        JButton btnXoa = createNavButton("Xóa", "cross.png");
        btnXoa.setForeground(new Color(190, 40, 40));

        btnPrev.addActionListener(e -> navigateOffset(-1));
        btnNext.addActionListener(e -> navigateOffset(1));
        btnTaoMoi.addActionListener(e -> resetForNew());
        btnXoa.addActionListener(e -> deleteCurrentVoucher());

        pnlNav.add(btnPhimTat);
        pnlNav.add(btnPrev);
        pnlNav.add(btnNext);
        pnlNav.add(btnTaoMoi);
        pnlNav.add(btnSaoChep);
        pnlNav.add(btnXoa);
        topHeaderPanel.add(pnlNav, BorderLayout.SOUTH);

        // ==================== 2. MASTER FORM & FINANCIAL SUMMARY ====================
        JPanel centerTopContainer = new JPanel(new BorderLayout(10, 0));
        centerTopContainer.setBackground(new Color(230, 240, 252));
        centerTopContainer.setBorder(new EmptyBorder(8, 10, 6, 10));

        // Form fields (Left & Center)
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Ngày & Số phiếu
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Ngày:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        dpkNgay = new DatePickerField();
        dpkNgay.setDate(new Date());
        dpkNgay.setPreferredSize(new Dimension(140, 24));
        pnlForm.add(dpkNgay, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Số phiếu:"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.65;
        txtSoPhieu = new JTextField();
        txtSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoPhieu.setPreferredSize(new Dimension(160, 24));
        pnlForm.add(txtSoPhieu, gbc);

        // Row 1: Kho xuất & Nhân viên xuất
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblKhoXuatTitle = new JLabel("Kho xuất (*):");
        lblKhoXuatTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKhoXuatTitle.setForeground(new Color(30, 60, 110));
        pnlForm.add(lblKhoXuatTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        cbKhoXuat = new JComboBox<>();
        cbKhoXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoXuat.setPreferredSize(new Dimension(140, 24));
        pnlForm.add(cbKhoXuat, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Nhân viên xuất:"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.65;
        cbNhanVienXuat = new JComboBox<>();
        cbNhanVienXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVienXuat.setPreferredSize(new Dimension(160, 24));
        pnlForm.add(cbNhanVienXuat, gbc);

        // Row 2: Nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Nhà cung cấp:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        cbNhaCungCap = new JComboBox<>();
        cbNhaCungCap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhaCungCap.setPreferredSize(new Dimension(320, 24));
        pnlForm.add(cbNhaCungCap, gbc);
        gbc.gridwidth = 1;

        // Row 3: Diễn giải
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Diễn giải:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtDienGiai = new JTextField("Xuất khác");
        txtDienGiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienGiai.setBackground(new Color(254, 253, 210)); // Đặc trưng nền vàng kem theo ảnh
        txtDienGiai.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 160), 1), new EmptyBorder(2, 4, 2, 4)));
        txtDienGiai.setPreferredSize(new Dimension(320, 24));
        pnlForm.add(txtDienGiai, gbc);
        gbc.gridwidth = 1;

        // Row 4: Ghi chú
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Ghi chú:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setPreferredSize(new Dimension(320, 24));
        pnlForm.add(txtGhiChu, gbc);
        gbc.gridwidth = 1;

        // Row 5: Tìm (F3) & Số lượng & Thêm
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Tìm (F3):"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        txtTimF3 = new JTextField();
        txtTimF3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimF3.setPreferredSize(new Dimension(140, 24));
        txtTimF3.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterCatalog(); }
            @Override public void removeUpdate(DocumentEvent e) { filterCatalog(); }
            @Override public void changedUpdate(DocumentEvent e) { filterCatalog(); }
        });
        txtTimF3.addActionListener(e -> onQuickAddProduct());
        pnlForm.add(txtTimF3, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Số lượng:"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.65;
        JPanel pnlQtyAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlQtyAdd.setOpaque(false);
        spSoLuong = new JSpinner(new SpinnerNumberModel(1.0, 0.001, 1000000.0, 1.0));
        spSoLuong.setPreferredSize(new Dimension(65, 24));
        pnlQtyAdd.add(spSoLuong);

        btnThem = new JButton("Thêm", IconHelper.getIcon("plus.png", 14, 14));
        btnThem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThem.setBackground(Color.WHITE);
        btnThem.setFocusPainted(false);
        btnThem.addActionListener(e -> onQuickAddProduct());
        pnlQtyAdd.add(btnThem);
        pnlForm.add(pnlQtyAdd, gbc);

        centerTopContainer.add(pnlForm, BorderLayout.CENTER);

        // Top Right Financial Summary Card (Khung Tổng cộng)
        JPanel pnlSummaryCard = new JPanel(new GridBagLayout());
        pnlSummaryCard.setBackground(new Color(245, 249, 255));
        pnlSummaryCard.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(190, 210, 235), 1), "Tổng cộng", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(30, 60, 110)),
                new EmptyBorder(4, 8, 4, 8)
        ));
        pnlSummaryCard.setPreferredSize(new Dimension(300, 175));

        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.insets = new Insets(3, 4, 3, 4);
        gbcS.fill = GridBagConstraints.HORIZONTAL;

        // Tiền hàng
        gbcS.gridx = 0; gbcS.gridy = 0; gbcS.weightx = 0.0;
        pnlSummaryCard.add(new JLabel("Tiền hàng"), gbcS);

        gbcS.gridx = 1; gbcS.weightx = 1.0;
        lblTienHangVal = createYellowBoxLabel("0");
        pnlSummaryCard.add(lblTienHangVal, gbcS);

        // Tỉ lệ giảm giá & Tiền giảm giá
        gbcS.gridx = 0; gbcS.gridy = 1; gbcS.weightx = 0.0;
        pnlSummaryCard.add(new JLabel("Tỉ lệ giảm giá"), gbcS);

        gbcS.gridx = 1; gbcS.weightx = 1.0;
        JPanel pnlGiam = new JPanel(new BorderLayout(4, 0));
        pnlGiam.setOpaque(false);
        txtTiLeGiamGia = new JTextField("0");
        txtTiLeGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTiLeGiamGia.setPreferredSize(new Dimension(45, 22));
        txtTienGiamGia = new JTextField("0");
        txtTienGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlGiam.add(txtTiLeGiamGia, BorderLayout.WEST);
        pnlGiam.add(txtTienGiamGia, BorderLayout.CENTER);
        pnlSummaryCard.add(pnlGiam, gbcS);

        txtTiLeGiamGia.addActionListener(e -> onDiscountPercentChanged());
        txtTiLeGiamGia.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { onDiscountPercentChanged(); }
        });
        txtTienGiamGia.addActionListener(e -> onDiscountAmountChanged());
        txtTienGiamGia.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { onDiscountAmountChanged(); }
        });

        // Tổng cộng
        gbcS.gridx = 0; gbcS.gridy = 2; gbcS.weightx = 0.0;
        pnlSummaryCard.add(new JLabel("Tổng cộng"), gbcS);

        gbcS.gridx = 1; gbcS.weightx = 1.0;
        lblTongCongVal = createYellowBoxLabel("0");
        lblTongCongVal.setForeground(new Color(160, 40, 20));
        pnlSummaryCard.add(lblTongCongVal, gbcS);

        // Tiền thanh toán
        gbcS.gridx = 0; gbcS.gridy = 3; gbcS.weightx = 0.0;
        pnlSummaryCard.add(new JLabel("Tiền thanh toán"), gbcS);

        gbcS.gridx = 1; gbcS.weightx = 1.0;
        txtTienThanhToan = new JTextField("0");
        txtTienThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTienThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtTienThanhToan.setPreferredSize(new Dimension(100, 24));
        pnlSummaryCard.add(txtTienThanhToan, gbcS);

        centerTopContainer.add(pnlSummaryCard, BorderLayout.EAST);

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(topHeaderPanel, BorderLayout.NORTH);
        northWrapper.add(centerTopContainer, BorderLayout.CENTER);
        add(northWrapper, BorderLayout.NORTH);

        // ==================== 3. CENTER SPLIT: CATALOG (LEFT) & EXPORT DETAILS (RIGHT) ====================
        JSplitPane splitCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitCenter.setDividerLocation(360);
        splitCenter.setDividerSize(6);

        // --- Left Table: Catalog ---
        JPanel pnlLeftCatalog = new JPanel(new BorderLayout());
        pnlLeftCatalog.setBackground(Color.WHITE);
        pnlLeftCatalog.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, new Color(195, 215, 238)),
                new EmptyBorder(2, 2, 2, 2)
        ));

        String[] catCols = {"", "Mã hàng", "Tên hàng", "ĐVT", "Giá bán", "Bảo hành"};
        catalogModel = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCatalog = new JTable(catalogModel);
        tableCatalog.setRowHeight(22);
        tableCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCatalog.setShowGrid(true);
        tableCatalog.setGridColor(new Color(230, 235, 245));

        int[] catWidths = {35, 80, 120, 50, 75, 50};
        for (int i = 0; i < catWidths.length; i++) {
            tableCatalog.getColumnModel().getColumn(i).setPreferredWidth(catWidths[i]);
        }
        DefaultTableCellRenderer rightCat = new DefaultTableCellRenderer();
        rightCat.setHorizontalAlignment(SwingConstants.RIGHT);
        tableCatalog.getColumnModel().getColumn(4).setCellRenderer(rightCat);

        tableCatalog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    addSelectedCatalogItem();
                }
            }
        });

        JScrollPane catScroll = new JScrollPane(tableCatalog);
        catScroll.setBorder(null);
        pnlLeftCatalog.add(catScroll, BorderLayout.CENTER);
        splitCenter.setLeftComponent(pnlLeftCatalog);

        // --- Right Table: Chi tiết xuất kho ---
        JPanel pnlRightDetails = new JPanel(new BorderLayout());
        pnlRightDetails.setBackground(Color.WHITE);

        // Action bar above right table
        JPanel pnlDetailBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlDetailBar.setBackground(new Color(240, 246, 254));
        pnlDetailBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));

        JButton btnXoaDong = new JButton("Xóa dòng (F8)", IconHelper.getIcon("cross.png", 14, 14));
        btnXoaDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnXoaDong.setBackground(Color.WHITE);
        btnXoaDong.setForeground(new Color(180, 40, 40));
        btnXoaDong.addActionListener(e -> deleteSelectedLineItem());

        JButton btnNhapExcel = new JButton("Nhập từ excel", IconHelper.getIcon("export-excel.png", 14, 14));
        btnNhapExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNhapExcel.setBackground(Color.WHITE);

        JButton btnXuatExcel = new JButton("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        btnXuatExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnXuatExcel.setBackground(Color.WHITE);
        btnXuatExcel.addActionListener(e -> ExcelExporter.exportTable(tableChiTiet, "Chi_Tiet_Xuat_Kho", "Chi tiết", this));

        JButton btnLoc = new JButton("Lọc", IconHelper.getIcon("Filter.png", 14, 14));
        btnLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLoc.setBackground(Color.WHITE);

        pnlDetailBar.add(btnXoaDong);
        pnlDetailBar.add(btnNhapExcel);
        pnlDetailBar.add(btnXuatExcel);
        pnlDetailBar.add(btnLoc);
        pnlRightDetails.add(pnlDetailBar, BorderLayout.NORTH);

        String[] detailCols = {
                "", "Mã hàng", "Tên mặt hàng", "ĐVT", "Số lượng", "Đơn giá", "Giảm giá %", "Thành tiền", "Bảo hành", "Ghi chú", "Hạn sử dụng"
        };
        chiTietModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Editable: Số lượng (4), Đơn giá (5), Giảm giá % (6), Bảo hành (8), Ghi chú (9), Hạn sử dụng (10)
                return col == 4 || col == 5 || col == 6 || col == 8 || col == 9 || col == 10;
            }
        };

        tableChiTiet = new JTable(chiTietModel);
        tableChiTiet.setRowHeight(22);
        tableChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableChiTiet.setShowGrid(true);
        tableChiTiet.setGridColor(new Color(225, 235, 245));

        int[] dtWidths = {35, 80, 180, 60, 65, 85, 75, 95, 70, 90, 85};
        for (int i = 0; i < dtWidths.length; i++) {
            if (i < tableChiTiet.getColumnModel().getColumnCount()) {
                tableChiTiet.getColumnModel().getColumn(i).setPreferredWidth(dtWidths[i]);
            }
        }

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableChiTiet.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        chiTietModel.addTableModelListener(e -> {
            int col = e.getColumn();
            int row = e.getFirstRow();
            if (row >= 0 && row < lineItems.size()) {
                if (col == 4 || col == 5 || col == 6) {
                    updateLineItemFromTable(row);
                } else if (col == 8) {
                    lineItems.get(row).setBaoHanh((String) chiTietModel.getValueAt(row, 8));
                } else if (col == 9) {
                    lineItems.get(row).setGhiChu((String) chiTietModel.getValueAt(row, 9));
                } else if (col == 10) {
                    lineItems.get(row).setHanSuDung((String) chiTietModel.getValueAt(row, 10));
                }
            }
        });

        JScrollPane detailScroll = new JScrollPane(tableChiTiet);
        detailScroll.setBorder(null);
        pnlRightDetails.add(detailScroll, BorderLayout.CENTER);
        splitCenter.setRightComponent(pnlRightDetails);

        add(splitCenter, BorderLayout.CENTER);

        // ==================== 4. BOTTOM ACTION BUTTONS ====================
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(230, 240, 252));
        bottomBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(195, 215, 235)),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JPanel pnlBottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlBottomLeft.setOpaque(false);
        JButton btnLuuIn = createActionButton("Lưu & In", "printer.png");
        JButton btnLuuXemIn = createActionButton("Lưu & Xem in", "Preview-icon.png");
        btnLuuIn.addActionListener(e -> saveAndPrint(true));
        btnLuuXemIn.addActionListener(e -> saveAndPrint(false));
        pnlBottomLeft.add(btnLuuIn);
        pnlBottomLeft.add(btnLuuXemIn);

        JPanel pnlBottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBottomRight.setOpaque(false);
        JButton btnLuu = createActionButton("Lưu", "save.png");
        JButton btnLuuMoi = createActionButton("Lưu & Mới", "save.png");
        JButton btnLuuThoat = createActionButton("Lưu & thoát", "save.png");
        JButton btnThoat = createActionButton("Thoát", "Cancel.png");

        btnLuu.addActionListener(e -> saveVoucher(false, false));
        btnLuuMoi.addActionListener(e -> saveVoucher(true, false));
        btnLuuThoat.addActionListener(e -> saveVoucher(false, true));
        btnThoat.addActionListener(e -> dispose());

        pnlBottomRight.add(btnLuu);
        pnlBottomRight.add(btnLuuMoi);
        pnlBottomRight.add(btnLuuThoat);
        pnlBottomRight.add(btnThoat);

        bottomBar.add(pnlBottomLeft, BorderLayout.WEST);
        bottomBar.add(pnlBottomRight, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JLabel createYellowBoxLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.RIGHT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(254, 253, 210));
        lbl.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 160), 1), new EmptyBorder(2, 6, 2, 6)));
        lbl.setPreferredSize(new Dimension(120, 24));
        return lbl;
    }

    private JButton createNavButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(200, 215, 235), 1), new EmptyBorder(3, 8, 3, 8)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(4, 12, 4, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();

        // ESC -> Thoát
        root.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Ctrl+S -> Lưu & Mới
        root.registerKeyboardAction(e -> saveVoucher(true, false), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Ctrl+L -> Lưu
        root.registerKeyboardAction(e -> saveVoucher(false, false), KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Ctrl+N -> Mới
        root.registerKeyboardAction(e -> resetForNew(), KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Ctrl+P -> In
        root.registerKeyboardAction(e -> saveAndPrint(true), KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // F3 -> Focus ô tìm kiếm
        root.registerKeyboardAction(e -> txtTimF3.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // F8 -> Xóa dòng
        root.registerKeyboardAction(e -> deleteSelectedLineItem(), KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // F10 -> Trước
        root.registerKeyboardAction(e -> navigateOffset(-1), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // F11 -> Sau
        root.registerKeyboardAction(e -> navigateOffset(1), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadDropdownData() {
        // Kho xuất
        cbKhoXuat.removeAllItems();
        List<KhoHang> khoList = khoHangDAO.getAll();
        for (KhoHang k : khoList) {
            cbKhoXuat.addItem(k);
        }
        cbKhoXuat.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhoHang) {
                    setText(((KhoHang) value).getName());
                    setIcon(IconHelper.getIcon("1282529873_objects.png", 14, 14));
                }
                return this;
            }
        });

        // Nhân viên xuất
        cbNhanVienXuat.removeAllItems();
        List<NhanVien> nvList = nhanVienDAO.getAll();
        for (NhanVien nv : nvList) {
            cbNhanVienXuat.addItem(nv);
        }
        cbNhanVienXuat.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien) {
                    setText(((NhanVien) value).getName());
                }
                return this;
            }
        });

        // Nhà cung cấp / Đối tác
        cbNhaCungCap.removeAllItems();
        KhachHang khEmpty = new KhachHang();
        khEmpty.setId("");
        khEmpty.setName("-- Chọn nhà cung cấp / đối tác --");
        cbNhaCungCap.addItem(khEmpty);
        List<KhachHang> khList = khachHangDAO.getAll();
        for (KhachHang kh : khList) {
            cbNhaCungCap.addItem(kh);
        }
        cbNhaCungCap.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    setText(((KhachHang) value).getName());
                }
                return this;
            }
        });
    }

    private void loadCatalogData() {
        allCatalogItems = matHangDAO.getAll();
        filterCatalog();
    }

    private void filterCatalog() {
        String keyword = txtTimF3.getText().trim().toLowerCase();
        filteredCatalogItems = allCatalogItems.stream().filter(m -> {
            if (keyword.isEmpty()) return true;
            String code = m.getCode() != null ? m.getCode().toLowerCase() : "";
            String name = m.getName() != null ? m.getName().toLowerCase() : "";
            return code.contains(keyword) || name.contains(keyword);
        }).collect(Collectors.toList());

        catalogModel.setRowCount(0);
        for (int i = 0; i < filteredCatalogItems.size(); i++) {
            MatHang m = filteredCatalogItems.get(i);
            catalogModel.addRow(new Object[]{
                    String.format("%04d", i + 1),
                    m.getCode(),
                    m.getName(),
                    m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "",
                    CurrencyUtil.format(m.getGiaBan()),
                    m.getBaoHanh() != null ? m.getBaoHanh() : ""
            });
        }
    }

    private void onQuickAddProduct() {
        int sel = tableCatalog.getSelectedRow();
        if (sel >= 0 && sel < filteredCatalogItems.size()) {
            addSelectedCatalogItem();
            return;
        }

        String kw = txtTimF3.getText().trim();
        if (!kw.isEmpty() && !filteredCatalogItems.isEmpty()) {
            tableCatalog.setRowSelectionInterval(0, 0);
            addSelectedCatalogItem();
        }
    }

    private void addSelectedCatalogItem() {
        int sel = tableCatalog.getSelectedRow();
        if (sel < 0 || sel >= filteredCatalogItems.size()) return;

        MatHang m = filteredCatalogItems.get(sel);
        double qty = ((Number) spSoLuong.getValue()).doubleValue();
        if (qty <= 0) qty = 1.0;

        // Check if already exists in lineItems
        DonHangChiTiet existing = null;
        for (DonHangChiTiet ct : lineItems) {
            if (m.getId() != null && m.getId().equals(ct.getMatHangId())) {
                existing = ct;
                break;
            } else if (m.getCode() != null && m.getCode().equals(ct.getMaHang())) {
                existing = ct;
                break;
            }
        }

        if (existing != null) {
            existing.setSoLuong(existing.getSoLuong() + qty);
            existing.tinhThanhTien();
        } else {
            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setMatHangId(m.getId());
            ct.setMaHang(m.getCode());
            ct.setTenHang(m.getName());
            ct.setDonViTinhId(m.getDonViTinhId());
            ct.setTenDonViTinh(m.getTenDonViTinh());
            ct.setSoLuong(qty);
            ct.setDonGia(m.getGiaBan());
            ct.setTiLeGiamGia(0.0);
            ct.tinhThanhTien();
            ct.setBaoHanh(m.getBaoHanh() != null ? m.getBaoHanh() : "");
            lineItems.add(ct);
        }

        refreshDetailTable();
        recalculateTotals();

        // Select the added/updated row in right table
        int targetRow = existing != null ? lineItems.indexOf(existing) : lineItems.size() - 1;
        if (targetRow >= 0 && targetRow < tableChiTiet.getRowCount()) {
            tableChiTiet.setRowSelectionInterval(targetRow, targetRow);
        }
    }

    private void deleteSelectedLineItem() {
        int row = tableChiTiet.getSelectedRow();
        if (row >= 0 && row < lineItems.size()) {
            lineItems.remove(row);
            refreshDetailTable();
            recalculateTotals();
        }
    }

    private void updateLineItemFromTable(int row) {
        if (row < 0 || row >= lineItems.size()) return;
        DonHangChiTiet ct = lineItems.get(row);

        try {
            Object valQty = chiTietModel.getValueAt(row, 4);
            double qty = parseDouble(valQty, ct.getSoLuong());
            ct.setSoLuong(qty);

            Object valPrice = chiTietModel.getValueAt(row, 5);
            double price = parseDouble(valPrice, ct.getDonGia());
            ct.setDonGia(price);

            Object valDisc = chiTietModel.getValueAt(row, 6);
            double disc = parseDouble(valDisc, ct.getTiLeGiamGia());
            ct.setTiLeGiamGia(disc);

            ct.tinhThanhTien();

            chiTietModel.setValueAt(CurrencyUtil.format(ct.getThanhTien()), row, 7);
            recalculateTotals();
        } catch (Exception ignored) {}
    }

    private double parseDouble(Object val, double fallback) {
        if (val == null) return fallback;
        if (val instanceof Number) return ((Number) val).doubleValue();
        String s = val.toString().replaceAll("[^0-9.-]", "").trim();
        if (s.isEmpty()) return fallback;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void refreshDetailTable() {
        chiTietModel.setRowCount(0);
        for (int i = 0; i < lineItems.size(); i++) {
            DonHangChiTiet ct = lineItems.get(i);
            chiTietModel.addRow(new Object[]{
                    (i + 1),
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    df.format(ct.getSoLuong()),
                    CurrencyUtil.format(ct.getDonGia()),
                    df.format(ct.getTiLeGiamGia()),
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getBaoHanh() != null ? ct.getBaoHanh() : "",
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    ct.getHanSuDung() != null ? ct.getHanSuDung() : ""
            });
        }
    }

    private void recalculateTotals() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = 0.0;
            for (DonHangChiTiet ct : lineItems) {
                totalTienHang += ct.getThanhTien();
            }

            double tiLeGiam = parseDouble(txtTiLeGiamGia.getText(), 0.0);
            double tienGiam = totalTienHang * (tiLeGiam / 100.0);
            double tongCong = totalTienHang - tienGiam;

            lblTienHangVal.setText(CurrencyUtil.format(totalTienHang));
            txtTienGiamGia.setText(CurrencyUtil.format(tienGiam));
            lblTongCongVal.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingTotals = false;
        }
    }

    private void onDiscountPercentChanged() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = 0.0;
            for (DonHangChiTiet ct : lineItems) totalTienHang += ct.getThanhTien();
            double tiLe = parseDouble(txtTiLeGiamGia.getText(), 0.0);
            double tienGiam = totalTienHang * (tiLe / 100.0);
            txtTienGiamGia.setText(CurrencyUtil.format(tienGiam));
            lblTongCongVal.setText(CurrencyUtil.format(totalTienHang - tienGiam));
        } finally {
            isUpdatingTotals = false;
        }
    }

    private void onDiscountAmountChanged() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = 0.0;
            for (DonHangChiTiet ct : lineItems) totalTienHang += ct.getThanhTien();
            double tienGiam = parseDouble(txtTienGiamGia.getText(), 0.0);
            double tiLe = totalTienHang > 0 ? (tienGiam / totalTienHang) * 100.0 : 0.0;
            txtTiLeGiamGia.setText(df.format(tiLe));
            lblTongCongVal.setText(CurrencyUtil.format(totalTienHang - tienGiam));
        } finally {
            isUpdatingTotals = false;
        }
    }

    public void resetForNew() {
        currentDonHang = null;
        setTitle("PHIẾU XUẤT KHO - THÊM MỚI");
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(donHangDAO.generateNextSoPhieuXuat(new Date()));
        txtDienGiai.setText("Xuất khác");
        txtGhiChu.setText("");
        txtTimF3.setText("");
        spSoLuong.setValue(1.0);
        lineItems.clear();
        refreshDetailTable();

        txtTiLeGiamGia.setText("0");
        txtTienGiamGia.setText("0");
        txtTienThanhToan.setText("0");
        recalculateTotals();

        // Default warehouse selection
        if (cbKhoXuat.getItemCount() > 0) {
            for (int i = 0; i < cbKhoXuat.getItemCount(); i++) {
                KhoHang k = cbKhoXuat.getItemAt(i);
                if (k.isDefault() || "KHO BÁN HÀNG".equalsIgnoreCase(k.getName()) || "KHO TỔNG".equalsIgnoreCase(k.getName())) {
                    cbKhoXuat.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void populateExistingData() {
        if (currentDonHang == null) return;
        setTitle("PHIẾU XUẤT KHO - CHỈNH SỬA [" + currentDonHang.getName() + "]");
        dpkNgay.setDate(currentDonHang.getNgay());
        txtSoPhieu.setText(currentDonHang.getName());
        txtDienGiai.setText(currentDonHang.getDienGiai() != null ? currentDonHang.getDienGiai() : "Xuất khác");
        txtGhiChu.setText(currentDonHang.getGhiChu() != null ? currentDonHang.getGhiChu() : "");

        // Select Kho
        if (currentDonHang.getKhoXuatId() != null) {
            for (int i = 0; i < cbKhoXuat.getItemCount(); i++) {
                KhoHang k = cbKhoXuat.getItemAt(i);
                if (currentDonHang.getKhoXuatId().equals(k.getId())) {
                    cbKhoXuat.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select NhanVien
        if (currentDonHang.getNhanVienXuatId() != null) {
            for (int i = 0; i < cbNhanVienXuat.getItemCount(); i++) {
                NhanVien nv = cbNhanVienXuat.getItemAt(i);
                if (currentDonHang.getNhanVienXuatId().equals(nv.getId())) {
                    cbNhanVienXuat.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select KhachHang
        if (currentDonHang.getKhachHangId() != null) {
            for (int i = 0; i < cbNhaCungCap.getItemCount(); i++) {
                KhachHang kh = cbNhaCungCap.getItemAt(i);
                if (currentDonHang.getKhachHangId().equals(kh.getId())) {
                    cbNhaCungCap.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Load details
        lineItems.clear();
        List<DonHangChiTiet> details = currentDonHang.getChiTietLayHang();
        if (details == null || details.isEmpty()) {
            details = donHangDAO.getChiTietPhieuXuat(currentDonHang.getId());
        }
        lineItems.addAll(details);
        refreshDetailTable();

        txtTiLeGiamGia.setText(df.format(currentDonHang.getTiLeGiamGia()));
        txtTienGiamGia.setText(CurrencyUtil.format(currentDonHang.getTienGiamGia()));
        txtTienThanhToan.setText(CurrencyUtil.format(currentDonHang.getTienThanhToan()));
        recalculateTotals();
    }

    private boolean saveVoucher(boolean createNewAfter, boolean closeAfter) {
        // Validation: Kho xuất không được rỗng
        KhoHang selKho = (KhoHang) cbKhoXuat.getSelectedItem();
        if (selKho == null || selKho.getId() == null || selKho.getId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Kho xuất (ô kho xuất không được rỗng)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            cbKhoXuat.requestFocus();
            return false;
        }

        // Validation: Chi tiết không được rỗng
        if (lineItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu xuất kho phải có ít nhất một mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTimF3.requestFocus();
            return false;
        }

        // Prepare DonHang object
        boolean isNew = (currentDonHang == null || currentDonHang.getId() == null);
        DonHang dh = isNew ? new DonHang() : currentDonHang;

        dh.setName(txtSoPhieu.getText().trim());
        dh.setNgay(dpkNgay.getDate() != null ? dpkNgay.getDate() : new Date());
        dh.setKhoXuatId(selKho.getId());
        dh.setTenKho(selKho.getName());

        NhanVien selNv = (NhanVien) cbNhanVienXuat.getSelectedItem();
        if (selNv != null) {
            dh.setNhanVienXuatId(selNv.getId());
            dh.setTenNhanVien(selNv.getName());
        }

        KhachHang selKh = (KhachHang) cbNhaCungCap.getSelectedItem();
        if (selKh != null && selKh.getId() != null && !selKh.getId().isEmpty()) {
            dh.setKhachHangId(selKh.getId());
            dh.setTenKhach(selKh.getName());
        } else {
            dh.setKhachHangId(null);
            dh.setTenKhach(null);
        }

        dh.setDienGiai(txtDienGiai.getText().trim());
        dh.setGhiChu(txtGhiChu.getText().trim());

        double totalTienHang = 0.0;
        for (DonHangChiTiet ct : lineItems) totalTienHang += ct.getThanhTien();
        double tiLeGiam = parseDouble(txtTiLeGiamGia.getText(), 0.0);
        double tienGiam = parseDouble(txtTienGiamGia.getText(), 0.0);
        double tongCong = totalTienHang - tienGiam;
        double thanhToan = parseDouble(txtTienThanhToan.getText(), 0.0);
        double conLai = tongCong - thanhToan;

        dh.setTienHang(totalTienHang);
        dh.setTiLeGiamGia(tiLeGiam);
        dh.setTienGiamGia(tienGiam);
        dh.setTongCong(tongCong);
        dh.setTienThanhToan(thanhToan);
        dh.setConLai(conLai);
        dh.setChiTietLayHang(new ArrayList<>(lineItems));
        dh.setLoai(2); // 2: Xuất kho

        boolean success;
        if (isNew) {
            success = donHangDAO.insertPhieuXuatKho(dh);
        } else {
            success = donHangDAO.updatePhieuXuatKho(dh);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu phiếu xuất kho thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();

            if (createNewAfter) {
                resetForNew();
            } else if (closeAfter) {
                dispose();
            } else {
                currentDonHang = dh;
                setTitle("PHIẾU XUẤT KHO - CHỈNH SỬA [" + dh.getName() + "]");
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu phiếu xuất kho. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveAndPrint(boolean directPrint) {
        if (saveVoucher(false, false)) {
            try {
                if (directPrint) {
                    tableChiTiet.print();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "<html><b>Xem in phiếu: " + txtSoPhieu.getText() + "</b><br>" +
                                    "Kho xuất: " + (cbKhoXuat.getSelectedItem() != null ? cbKhoXuat.getSelectedItem().toString() : "") + "<br>" +
                                    "Tổng cộng: " + lblTongCongVal.getText() + " đ</html>",
                            "Xem trước in phiếu xuất kho", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi in phiếu: " + e.getMessage(), "Lỗi in", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCurrentVoucher() {
        if (currentDonHang == null || currentDonHang.getId() == null) {
            JOptionPane.showMessageDialog(this, "Phiếu xuất chưa được lưu trên hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu xuất kho: " + currentDonHang.getName() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (donHangDAO.deletePhieuXuatKho(currentDonHang.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu xuất kho thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                if (onDataSaved != null) onDataSaved.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa phiếu xuất kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void navigateOffset(int offset) {
        if (currentDonHang == null || currentDonHang.getId() == null) return;
        DonHang target = donHangDAO.getPhieuXuatOffset(currentDonHang.getId(), offset);
        if (target != null) {
            currentDonHang = target;
            populateExistingData();
        } else {
            JOptionPane.showMessageDialog(this, offset < 0 ? "Đã ở phiếu đầu tiên!" : "Đã ở phiếu cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
