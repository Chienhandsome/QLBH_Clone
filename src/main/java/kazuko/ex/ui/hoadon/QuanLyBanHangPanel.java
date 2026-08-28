package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class QuanLyBanHangPanel extends JPanel {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    // Top Filter Toolbar
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JTextField txtLoc;
    private JComboBox<CuaHang> cbCuaHangFilter;

    // Left Table: Master Invoice List
    private JTable tableInvoices;
    private DefaultTableModel invoicesModel;
    private List<DonHang> allInvoicesList = new ArrayList<>();
    private List<DonHang> filteredInvoicesList = new ArrayList<>();
    private DonHang currentSelectedInvoice = null;

    // Right Form Components
    private JButton btnDanhSachBillHuy;
    private DatePickerField dpkNgay;
    private JTextField txtSoHD;
    private JButton btnLoaiGia;
    private JComboBox<KhachHang> cbKhachHang;
    private JComboBox<NhanVien> cbNhanVien;
    private JComboBox<KhoHang> cbKhoXuat;
    private JTextField txtGiaoHang;
    private JTextField txtDienGiai;
    private JTextField txtGhiChu;

    // Center Details Grid
    private JTabbedPane tabbedDetails;
    private JTable tableLayHang;
    private DefaultTableModel layHangModel;
    private JTable tableTraHang;
    private DefaultTableModel traHangModel;
    private final List<DonHangChiTiet> currentLayHangList = new ArrayList<>();
    private final List<DonHangChiTiet> currentTraHangList = new ArrayList<>();

    // Bottom Summary with Vertical Tabs
    private CardLayout cardLayoutBottom;
    private JPanel pnlCardBottom;
    private JButton btnTabTongCong;
    private JButton btnTabThanhToan;

    // Summary View 1: TỔNG CỘNG
    private JButton btnHuyPhieu;
    private JButton btnLuuDuLieu;
    private JButton btnLuuVaInLaiBill;
    private JTextField txtTienHang;
    private JTextField txtTiLeGiamGia;
    private JTextField txtTienGiamGia;
    private JTextField txtTiLePhiVC;
    private JTextField txtPhiVanChuyen;
    private JTextField txtTienDoiTra;
    private JTextField txtTongCong;
    private JTextField txtTienThanhToan;

    // Summary View 2: T.TOÁN
    private JTextField txtTienMat;
    private JTextField txtChuyenKhoan;
    private JComboBox<String> cbTaiKhoanNganHang;
    private JTextField txtTheATM;
    private JTextField txtVoucher;
    private JTextField txtMaVoucher;
    private JTextField txtTheTraTruoc;
    private JTextField txtMaThe;
    private JTextField txtTruTichLuy;

    public QuanLyBanHangPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(230, 240, 252));

        initComponents();
        loadDropdownData();
        loadInvoices();
    }

    private void initComponents() {
        // 1. Top Filter Toolbar
        add(createTopFilterToolbar(), BorderLayout.NORTH);

        // 2. Center SplitPane (Left Invoices List vs Right Invoice Editor)
        JSplitPane splitCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitCenter.setDividerLocation(520);
        splitCenter.setDividerSize(6);

        splitCenter.setLeftComponent(createLeftInvoicesPanel());
        splitCenter.setRightComponent(createRightDetailsPanel());

        add(splitCenter, BorderLayout.CENTER);

        // F5 shortcut
        KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        registerKeyboardAction(e -> loadInvoices(), f5, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel createTopFilterToolbar() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnl.setBackground(new Color(215, 230, 248));
        pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 205, 235)));

        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTu);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        dpkTuNgay = new DatePickerField();
        dpkTuNgay.setDate(cal.getTime());
        dpkTuNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadInvoices(); }
            @Override public void removeUpdate(DocumentEvent e) { loadInvoices(); }
            @Override public void changedUpdate(DocumentEvent e) { loadInvoices(); }
        });
        pnl.add(dpkTuNgay);

        JLabel lblDen = new JLabel("Đến:");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblDen);

        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(new Date());
        dpkDenNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadInvoices(); }
            @Override public void removeUpdate(DocumentEvent e) { loadInvoices(); }
            @Override public void changedUpdate(DocumentEvent e) { loadInvoices(); }
        });
        pnl.add(dpkDenNgay);

        JButton btnRefresh = new JButton("Refresh (F5)", IconHelper.getIcon("Refresh.png", 16, 16));
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadInvoices());
        pnl.add(btnRefresh);

        JLabel lblLoc = new JLabel("Lọc:");
        lblLoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblLoc);

        txtLoc = new JTextField(12);
        txtLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLoc.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterInvoicesLocally(); }
            @Override public void removeUpdate(DocumentEvent e) { filterInvoicesLocally(); }
            @Override public void changedUpdate(DocumentEvent e) { filterInvoicesLocally(); }
        });
        pnl.add(txtLoc);

        JLabel lblCuaHang = new JLabel("Cửa hàng:");
        lblCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblCuaHang);

        cbCuaHangFilter = new JComboBox<>();
        cbCuaHangFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCuaHangFilter.setPreferredSize(new Dimension(160, 24));
        cbCuaHangFilter.addActionListener(e -> loadInvoices());
        pnl.add(cbCuaHangFilter);

        return pnl;
    }

    private JPanel createLeftInvoicesPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        String[] headers = {
                "STT", "Số phiếu", "Ngày", "Khách hàng", "Tiền hàng", "Giảm giá",
                "Đổi trả", "Tổng cộng", "Giờ thanh toán", "Thanh toán bởi",
                "Ghi chú", "Mã khách", "Địa chỉ", "Điện thoại"
        };

        invoicesModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableInvoices = new JTable(invoicesModel);
        tableInvoices.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableInvoices.setRowHeight(22);
        tableInvoices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableInvoices.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableInvoices.getTableHeader().setBackground(new Color(230, 240, 252));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        tableInvoices.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableInvoices.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableInvoices.getColumnModel().getColumn(1).setPreferredWidth(95);
        tableInvoices.getColumnModel().getColumn(2).setPreferredWidth(85);
        tableInvoices.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableInvoices.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableInvoices.getColumnModel().getColumn(4).setPreferredWidth(85);
        tableInvoices.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableInvoices.getColumnModel().getColumn(5).setPreferredWidth(75);
        tableInvoices.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableInvoices.getColumnModel().getColumn(6).setPreferredWidth(75);
        tableInvoices.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableInvoices.getColumnModel().getColumn(7).setPreferredWidth(90);
        tableInvoices.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tableInvoices.getColumnModel().getColumn(8).setPreferredWidth(80);
        tableInvoices.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        tableInvoices.getColumnModel().getColumn(9).setPreferredWidth(100);
        tableInvoices.getColumnModel().getColumn(10).setPreferredWidth(100);
        tableInvoices.getColumnModel().getColumn(11).setPreferredWidth(90);
        tableInvoices.getColumnModel().getColumn(12).setPreferredWidth(120);
        tableInvoices.getColumnModel().getColumn(13).setPreferredWidth(95);

        tableInvoices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelectInvoiceFromTable();
            }
        });

        tableInvoices.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane sp = new JScrollPane(tableInvoices);
        pnl.add(sp, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createRightDetailsPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setBackground(new Color(230, 240, 252));
        pnl.setBorder(new EmptyBorder(4, 4, 4, 4));

        // 1. Top Header Form
        pnl.add(createInvoiceHeaderForm(), BorderLayout.NORTH);

        // 2. Center Grid (KHÁCH LẤY HÀNG / KHÁCH TRẢ HÀNG)
        pnl.add(createDetailsTabbedPane(), BorderLayout.CENTER);

        // 3. Bottom Financial & Payment Breakdown
        pnl.add(createBottomSummaryContainer(), BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createInvoiceHeaderForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        // Top Action Bar: DANH SÁCH BILL HỦY & HÓA ĐƠN BÁN HÀNG Title
        JPanel pnlTitleBar = new JPanel(new BorderLayout());
        pnlTitleBar.setOpaque(false);

        btnDanhSachBillHuy = new JButton("DANH SÁCH BILL HỦY");
        btnDanhSachBillHuy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDanhSachBillHuy.setForeground(new Color(200, 20, 20));
        btnDanhSachBillHuy.setBackground(Color.WHITE);
        btnDanhSachBillHuy.setBorder(new CompoundBorder(new LineBorder(new Color(220, 100, 100), 1), new EmptyBorder(4, 10, 4, 10)));
        btnDanhSachBillHuy.setFocusPainted(false);
        btnDanhSachBillHuy.addActionListener(e -> onOpenDanhSachBillHuy());
        pnlTitleBar.add(btnDanhSachBillHuy, BorderLayout.WEST);

        JLabel lblTitle = new JLabel("HÓA ĐƠN BÁN HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0, 45, 140));
        pnlTitleBar.add(lblTitle, BorderLayout.CENTER);

        panel.add(pnlTitleBar, BorderLayout.NORTH);

        // Form Fields
        JPanel pnlForm = new JPanel(null);
        pnlForm.setOpaque(false);
        pnlForm.setPreferredSize(new Dimension(500, 132));

        // Row 1: Ngày, Số HĐ, Loại giá (F7)
        JLabel lblNgay = new JLabel("Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgay.setBounds(5, 5, 45, 22);
        pnlForm.add(lblNgay);

        dpkNgay = new DatePickerField();
        dpkNgay.setDate(new Date());
        dpkNgay.setBounds(55, 5, 125, 24);
        pnlForm.add(dpkNgay);

        JLabel lblSoHD = new JLabel("Số HĐ");
        lblSoHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoHD.setBounds(188, 5, 40, 22);
        pnlForm.add(lblSoHD);

        txtSoHD = new JTextField();
        txtSoHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoHD.setBackground(new Color(230, 245, 255));
        txtSoHD.setBounds(230, 5, 120, 24);
        pnlForm.add(txtSoHD);

        JLabel lblLoaiGia = new JLabel("Loại giá (F7):");
        lblLoaiGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoaiGia.setBounds(358, 5, 75, 22);
        pnlForm.add(lblLoaiGia);

        btnLoaiGia = new JButton("Giá bán");
        btnLoaiGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLoaiGia.setBackground(Color.WHITE);
        btnLoaiGia.setBounds(435, 5, 100, 24);
        pnlForm.add(btnLoaiGia);

        // Row 2: K. hàng, Nhân viên
        JLabel lblKhach = new JLabel("K. hàng");
        lblKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKhach.setBounds(5, 32, 45, 22);
        pnlForm.add(lblKhach);

        cbKhachHang = new JComboBox<>();
        cbKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhachHang.setBounds(55, 32, 295, 24);
        pnlForm.add(cbKhachHang);

        JLabel lblNV = new JLabel("Nhân viên:");
        lblNV.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNV.setBounds(358, 32, 75, 22);
        pnlForm.add(lblNV);

        cbNhanVien = new JComboBox<>();
        cbNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVien.setBounds(435, 32, 180, 24);
        pnlForm.add(cbNhanVien);

        // Row 3: Kho xuất, Giao hàng
        JLabel lblKho = new JLabel("Kho xuất");
        lblKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKho.setBounds(5, 59, 50, 22);
        pnlForm.add(lblKho);

        cbKhoXuat = new JComboBox<>();
        cbKhoXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoXuat.setBounds(55, 59, 295, 24);
        pnlForm.add(cbKhoXuat);

        JLabel lblGiaoHang = new JLabel("Giao hàng");
        lblGiaoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaoHang.setBounds(358, 59, 75, 22);
        pnlForm.add(lblGiaoHang);

        txtGiaoHang = new JTextField();
        txtGiaoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiaoHang.setBounds(435, 59, 180, 24);
        pnlForm.add(txtGiaoHang);

        // Row 4: Diễn giải
        JLabel lblDienGiai = new JLabel("Diễn giải");
        lblDienGiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDienGiai.setBounds(5, 86, 50, 22);
        pnlForm.add(lblDienGiai);

        txtDienGiai = new JTextField("Xuất bán hàng");
        txtDienGiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienGiai.setBounds(55, 86, 560, 24);
        pnlForm.add(txtDienGiai);

        // Row 5: Ghi chú
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setBounds(5, 112, 50, 20);
        pnlForm.add(lblGhiChu);

        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setBounds(55, 112, 560, 22);
        pnlForm.add(txtGhiChu);

        panel.add(pnlForm, BorderLayout.CENTER);
        return panel;
    }

    private JTabbedPane createDetailsTabbedPane() {
        tabbedDetails = new JTabbedPane();
        tabbedDetails.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: KHÁCH LẤY HÀNG
        JPanel pnlLayHang = new JPanel(new BorderLayout());
        pnlLayHang.setBackground(Color.WHITE);

        // Toolbar on top of grid
        JPanel tbLayHang = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        tbLayHang.setBackground(new Color(240, 246, 254));

        JButton btnThem = new JButton("Thêm", IconHelper.getIcon("btnNew.Image.png", 14, 14));
        btnThem.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnThem.setBackground(Color.WHITE);
        btnThem.addActionListener(e -> onThemMatHang());

        JButton btnXoa = new JButton("Xóa", IconHelper.getIcon("Cancelled.png", 14, 14));
        btnXoa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnXoa.setBackground(Color.WHITE);
        btnXoa.addActionListener(e -> onXoaMatHang());

        tbLayHang.add(btnThem);
        tbLayHang.add(btnXoa);
        pnlLayHang.add(tbLayHang, BorderLayout.NORTH);

        String[] headers = {"STT", "Tên mặt hàng", "Mã hàng", "Đơn giá", "ĐVT", "CK%", "Tiền giảm", "Thành tiền", "Ghi chú"};
        layHangModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableLayHang = new JTable(layHangModel);
        tableLayHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableLayHang.setRowHeight(22);
        tableLayHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableLayHang.getTableHeader().setBackground(new Color(230, 240, 252));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        tableLayHang.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableLayHang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableLayHang.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableLayHang.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableLayHang.getColumnModel().getColumn(3).setPreferredWidth(85);
        tableLayHang.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableLayHang.getColumnModel().getColumn(4).setPreferredWidth(55);
        tableLayHang.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableLayHang.getColumnModel().getColumn(5).setPreferredWidth(50);
        tableLayHang.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableLayHang.getColumnModel().getColumn(6).setPreferredWidth(75);
        tableLayHang.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableLayHang.getColumnModel().getColumn(7).setPreferredWidth(85);
        tableLayHang.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tableLayHang.getColumnModel().getColumn(8).setPreferredWidth(90);

        JScrollPane spLayHang = new JScrollPane(tableLayHang);
        pnlLayHang.add(spLayHang, BorderLayout.CENTER);
        tabbedDetails.addTab("KHÁCH LẤY HÀNG", pnlLayHang);

        // Tab 2: KHÁCH TRẢ HÀNG
        JPanel pnlTraHang = new JPanel(new BorderLayout());
        pnlTraHang.setBackground(Color.WHITE);

        traHangModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableTraHang = new JTable(traHangModel);
        tableTraHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTraHang.setRowHeight(22);
        tableTraHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableTraHang.getTableHeader().setBackground(new Color(230, 240, 252));

        tableTraHang.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableTraHang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableTraHang.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableTraHang.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableTraHang.getColumnModel().getColumn(3).setPreferredWidth(85);
        tableTraHang.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableTraHang.getColumnModel().getColumn(4).setPreferredWidth(55);
        tableTraHang.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableTraHang.getColumnModel().getColumn(5).setPreferredWidth(50);
        tableTraHang.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableTraHang.getColumnModel().getColumn(6).setPreferredWidth(75);
        tableTraHang.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableTraHang.getColumnModel().getColumn(7).setPreferredWidth(85);
        tableTraHang.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tableTraHang.getColumnModel().getColumn(8).setPreferredWidth(90);

        JScrollPane spTraHang = new JScrollPane(tableTraHang);
        pnlTraHang.add(spTraHang, BorderLayout.CENTER);
        tabbedDetails.addTab("KHÁCH TRẢ HÀNG", pnlTraHang);

        return tabbedDetails;
    }

    private JPanel createBottomSummaryContainer() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 205, 235)));

        // Left Vertical Tabs: TỔNG CỘNG vs T.TOÁN
        JPanel pnlVerticalTabs = new JPanel(new GridLayout(2, 1, 0, 0));
        pnlVerticalTabs.setPreferredSize(new Dimension(28, 140));
        pnlVerticalTabs.setBackground(new Color(215, 230, 245));

        btnTabTongCong = createVerticalTabButton("TỔNG CỘNG", true);
        btnTabThanhToan = createVerticalTabButton("T.TOÁN", false);

        btnTabTongCong.addActionListener(e -> switchBottomTab("TONG_CONG"));
        btnTabThanhToan.addActionListener(e -> switchBottomTab("THANH_TOAN"));

        pnlVerticalTabs.add(btnTabTongCong);
        pnlVerticalTabs.add(btnTabThanhToan);
        pnl.add(pnlVerticalTabs, BorderLayout.WEST);

        // Center Card Panels
        cardLayoutBottom = new CardLayout();
        pnlCardBottom = new JPanel(cardLayoutBottom);
        pnlCardBottom.setOpaque(false);

        pnlCardBottom.add(createTongCongView(), "TONG_CONG");
        pnlCardBottom.add(createThanhToanView(), "THANH_TOAN");

        pnl.add(pnlCardBottom, BorderLayout.CENTER);
        return pnl;
    }

    private JButton createVerticalTabButton(String text, boolean active) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(active ? new Color(255, 250, 200) : new Color(225, 235, 248));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(175, 195, 225));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                g2.setColor(new Color(20, 45, 90));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));

                FontMetrics fm = g2.getFontMetrics();
                int totalHeight = text.length() * 12;
                int startY = (getHeight() - totalHeight) / 2 + fm.getAscent();

                for (int i = 0; i < text.length(); i++) {
                    String ch = String.valueOf(text.charAt(i));
                    int chW = fm.stringWidth(ch);
                    int chX = (getWidth() - chW) / 2;
                    g2.drawString(ch, chX, startY + (i * 12));
                }
                g2.dispose();
            }
        };
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(null);
        return btn;
    }

    private void switchBottomTab(String tabName) {
        cardLayoutBottom.show(pnlCardBottom, tabName);
        btnTabTongCong.repaint();
        btnTabThanhToan.repaint();
    }

    private JPanel createTongCongView() {
        JPanel pnl = new JPanel(new BorderLayout(8, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Left Action Buttons: HỦY PHIẾU, LƯU DỮ LIỆU, Lưu & In lại bill
        JPanel pnlActions = new JPanel(new GridLayout(3, 1, 0, 6));
        pnlActions.setOpaque(false);
        pnlActions.setPreferredSize(new Dimension(160, 130));

        btnHuyPhieu = new JButton("HỦY PHIẾU", IconHelper.getIcon("Cancelled.png", 16, 16));
        btnHuyPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHuyPhieu.setForeground(new Color(200, 20, 20));
        btnHuyPhieu.setBackground(Color.WHITE);
        btnHuyPhieu.addActionListener(e -> onHuyPhieu());

        btnLuuDuLieu = new JButton("LƯU DỮ LIỆU", IconHelper.getIcon("Save1.png", 16, 16));
        btnLuuDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuDuLieu.setBackground(Color.WHITE);
        btnLuuDuLieu.addActionListener(e -> onLuuDuLieu());

        btnLuuVaInLaiBill = new JButton("Lưu & In lại bill", IconHelper.getIcon("print-16.png", 16, 16));
        btnLuuVaInLaiBill.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaInLaiBill.setBackground(Color.WHITE);
        btnLuuVaInLaiBill.addActionListener(e -> onLuuVaInLaiBill());

        pnlActions.add(btnHuyPhieu);
        pnlActions.add(btnLuuDuLieu);
        pnlActions.add(btnLuuVaInLaiBill);
        pnl.add(pnlActions, BorderLayout.WEST);

        // Right Financial Summary Fields
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);

        // Row 1: Tiền hàng
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblTh = new JLabel("Tiền hàng");
        lblTh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFields.add(lblTh, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTienHang = createCyanField();
        pnlFields.add(txtTienHang, gbc);

        // Row 2: Giảm giá
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel lblGg = new JLabel("Giảm giá");
        lblGg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFields.add(lblGg, gbc);

        gbc.gridx = 1;
        txtTiLeGiamGia = new JTextField("0 %", 4);
        txtTiLeGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlFields.add(txtTiLeGiamGia, gbc);

        gbc.gridx = 2;
        txtTienGiamGia = createCyanField();
        pnlFields.add(txtTienGiamGia, gbc);

        // Row 3: Phí vận chuyển
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel lblVc = new JLabel("Phí vận chuyển");
        lblVc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFields.add(lblVc, gbc);

        gbc.gridx = 1;
        txtTiLePhiVC = new JTextField("0 %", 4);
        txtTiLePhiVC.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlFields.add(txtTiLePhiVC, gbc);

        gbc.gridx = 2;
        txtPhiVanChuyen = createCyanField();
        pnlFields.add(txtPhiVanChuyen, gbc);

        // Row 4: Đổi trả
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel lblDt = new JLabel("Đổi trả");
        lblDt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFields.add(lblDt, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTienDoiTra = createCyanField();
        pnlFields.add(txtTienDoiTra, gbc);

        // Row 5: Tổng cộng
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel lblTc = new JLabel("Tổng cộng");
        lblTc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlFields.add(lblTc, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTongCong = createCyanField();
        txtTongCong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlFields.add(txtTongCong, gbc);

        // Row 6: Tiền thanh toán
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JLabel lblTt = new JLabel("Tiền thanh toán");
        lblTt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlFields.add(lblTt, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTienThanhToan = createCyanField();
        txtTienThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlFields.add(txtTienThanhToan, gbc);

        pnl.add(pnlFields, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createThanhToanView() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(4, 12, 4, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 6, 2, 6);

        // Row 1: Tiền mặt
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblTm = new JLabel("Tiền mặt");
        lblTm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTm, gbc);

        gbc.gridx = 1;
        txtTienMat = createNumberField("0");
        pnl.add(txtTienMat, gbc);

        // Row 2: Chuyển khoản & Tài khoản ngân hàng
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblCk = new JLabel("Chuyển khoản");
        lblCk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblCk, gbc);

        gbc.gridx = 1;
        txtChuyenKhoan = createNumberField("0");
        pnl.add(txtChuyenKhoan, gbc);

        gbc.gridx = 2;
        JLabel lblTk = new JLabel("Tài khoản ngân hàng");
        lblTk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTk, gbc);

        gbc.gridx = 3;
        cbTaiKhoanNganHang = new JComboBox<>();
        cbTaiKhoanNganHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbTaiKhoanNganHang.setPreferredSize(new Dimension(160, 22));
        pnl.add(cbTaiKhoanNganHang, gbc);

        // Row 3: Thẻ ATM
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblAtm = new JLabel("Thẻ ATM");
        lblAtm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblAtm, gbc);

        gbc.gridx = 1;
        txtTheATM = createNumberField("0");
        pnl.add(txtTheATM, gbc);

        // Row 4: Voucher & Mã voucher
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblVc = new JLabel("Voucher");
        lblVc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblVc, gbc);

        gbc.gridx = 1;
        txtVoucher = createNumberField("0");
        pnl.add(txtVoucher, gbc);

        gbc.gridx = 2;
        JLabel lblMvc = new JLabel("Mã voucher");
        lblMvc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblMvc, gbc);

        gbc.gridx = 3;
        txtMaVoucher = new JTextField(12);
        txtMaVoucher.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(txtMaVoucher, gbc);

        // Row 5: Thẻ trả trước & Mã thẻ
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblTt = new JLabel("Thẻ trả trước");
        lblTt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTt, gbc);

        gbc.gridx = 1;
        txtTheTraTruoc = createNumberField("0");
        pnl.add(txtTheTraTruoc, gbc);

        gbc.gridx = 2;
        JLabel lblMt = new JLabel("Mã thẻ");
        lblMt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblMt, gbc);

        gbc.gridx = 3;
        txtMaThe = new JTextField(12);
        txtMaThe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(txtMaThe, gbc);

        // Row 6: Trừ tích lũy
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblTl = new JLabel("Trừ tích lũy");
        lblTl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTl, gbc);

        gbc.gridx = 1;
        txtTruTichLuy = createNumberField("0");
        pnl.add(txtTruTichLuy, gbc);

        return pnl;
    }

    private JTextField createCyanField() {
        JTextField tf = new JTextField("0", 9);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        tf.setBackground(new Color(225, 248, 250));
        tf.setEditable(false);
        return tf;
    }

    private JTextField createNumberField(String defVal) {
        JTextField tf = new JTextField(defVal, 10);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        tf.setBackground(Color.WHITE);
        return tf;
    }

    private void loadDropdownData() {
        // Cửa hàng
        cbCuaHangFilter.removeAllItems();
        CuaHang allCh = new CuaHang();
        allCh.setName("-- Tất cả cửa hàng --");
        cbCuaHangFilter.addItem(allCh);
        for (CuaHang ch : cuaHangDAO.getAll()) {
            cbCuaHangFilter.addItem(ch);
        }

        // Khách hàng
        cbKhachHang.removeAllItems();
        for (KhachHang kh : khachHangDAO.getAll()) {
            if (kh.getStatus() >= 0) cbKhachHang.addItem(kh);
        }

        // Nhân viên
        cbNhanVien.removeAllItems();
        for (NhanVien nv : nhanVienDAO.getAll()) {
            if (nv.getStatus() >= 0) cbNhanVien.addItem(nv);
        }

        // Kho xuất
        cbKhoXuat.removeAllItems();
        for (KhoHang kh : khoHangDAO.getAll()) {
            if (kh.getStatus() >= 0) cbKhoXuat.addItem(kh);
        }
    }

    private void loadInvoices() {
        Date tu = dpkTuNgay != null ? dpkTuNgay.getDate() : null;
        Date den = dpkDenNgay != null ? dpkDenNgay.getDate() : null;
        String cuaHangId = null;
        if (cbCuaHangFilter != null && cbCuaHangFilter.getSelectedIndex() > 0) {
            CuaHang ch = (CuaHang) cbCuaHangFilter.getSelectedItem();
            if (ch != null) cuaHangId = ch.getId();
        }

        allInvoicesList = hoaDonDAO.getDanhSachHoaDon(tu, den, cuaHangId, null);
        filterInvoicesLocally();
    }

    private void filterInvoicesLocally() {
        String kw = txtLoc != null ? txtLoc.getText().trim().toLowerCase() : "";
        filteredInvoicesList = new ArrayList<>();
        for (DonHang dh : allInvoicesList) {
            if (kw.isEmpty()) {
                filteredInvoicesList.add(dh);
                continue;
            }
            String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
            String khach = dh.getTenKhach() != null ? dh.getTenKhach().toLowerCase() : "";
            String dt = dh.getDienThoaiKhach() != null ? dh.getDienThoaiKhach().toLowerCase() : "";
            String dg = dh.getDienGiai() != null ? dh.getDienGiai().toLowerCase() : "";
            if (name.contains(kw) || khach.contains(kw) || dt.contains(kw) || dg.contains(kw)) {
                filteredInvoicesList.add(dh);
            }
        }
        populateInvoicesTable();
    }

    private void populateInvoicesTable() {
        invoicesModel.setRowCount(0);
        int stt = 1;
        for (DonHang dh : filteredInvoicesList) {
            String ngayStr = dh.getNgay() != null ? DateUtil.formatDate(dh.getNgay()) : "";
            String gioStr = dh.getGioThanhToan() != null ? dh.getGioThanhToan() : "11:30";

            invoicesModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    dh.getName() != null ? dh.getName() : "",
                    ngayStr,
                    dh.getTenKhach() != null ? dh.getTenKhach() : "",
                    CurrencyUtil.format(dh.getTienHang()),
                    CurrencyUtil.format(dh.getTienGiamGia()),
                    CurrencyUtil.format(dh.getTienDoiTra()),
                    CurrencyUtil.format(dh.getTongCong()),
                    gioStr,
                    dh.getNguoiThanhToan() != null ? dh.getNguoiThanhToan() : "Administrator",
                    dh.getGhiChu() != null ? dh.getGhiChu() : "",
                    dh.getMaKhach() != null ? dh.getMaKhach() : "",
                    dh.getDiaChiKhach() != null ? dh.getDiaChiKhach() : "",
                    dh.getDienThoaiKhach() != null ? dh.getDienThoaiKhach() : ""
            });
        }

        if (tableInvoices.getRowCount() > 0) {
            tableInvoices.setRowSelectionInterval(0, 0);
            onSelectInvoiceFromTable();
        } else {
            clearRightDetails();
        }
    }

    private void onSelectInvoiceFromTable() {
        int row = tableInvoices.getSelectedRow();
        if (row < 0 || row >= filteredInvoicesList.size()) {
            clearRightDetails();
            return;
        }

        currentSelectedInvoice = filteredInvoicesList.get(row);
        displayInvoiceInRightPanel(currentSelectedInvoice);
    }

    private void displayInvoiceInRightPanel(DonHang dh) {
        if (dh == null) {
            clearRightDetails();
            return;
        }

        dpkNgay.setDate(dh.getNgay() != null ? dh.getNgay() : new Date());
        txtSoHD.setText(dh.getName() != null ? dh.getName() : "");
        txtGiaoHang.setText(dh.getDiaChiGiaoHang() != null ? dh.getDiaChiGiaoHang() : "");
        txtDienGiai.setText(dh.getDienGiai() != null ? dh.getDienGiai() : "Xuất bán hàng");
        txtGhiChu.setText(dh.getGhiChu() != null ? dh.getGhiChu() : "");

        if (dh.getKhachHangId() != null) {
            for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
                KhachHang kh = cbKhachHang.getItemAt(i);
                if (kh != null && dh.getKhachHangId().equals(kh.getId())) {
                    cbKhachHang.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (dh.getNhanVienXuatId() != null) {
            for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                NhanVien nv = cbNhanVien.getItemAt(i);
                if (nv != null && dh.getNhanVienXuatId().equals(nv.getId())) {
                    cbNhanVien.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (dh.getKhoXuatId() != null) {
            for (int i = 0; i < cbKhoXuat.getItemCount(); i++) {
                KhoHang kh = cbKhoXuat.getItemAt(i);
                if (kh != null && dh.getKhoXuatId().equals(kh.getId())) {
                    cbKhoXuat.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Details
        currentLayHangList.clear();
        if (dh.getChiTietLayHang() != null) currentLayHangList.addAll(dh.getChiTietLayHang());

        currentTraHangList.clear();
        if (dh.getChiTietTraHang() != null) currentTraHangList.addAll(dh.getChiTietTraHang());

        refreshLayHangTable();
        refreshTraHangTable();

        // Financials
        txtTienHang.setText(CurrencyUtil.format(dh.getTienHang()));
        txtTiLeGiamGia.setText(String.format("%.0f %%", dh.getTiLeGiamGia()));
        txtTienGiamGia.setText(CurrencyUtil.format(dh.getTienGiamGia()));
        txtTiLePhiVC.setText(String.format("%.0f %%", dh.getTiLePhiVanChuyen()));
        txtPhiVanChuyen.setText(CurrencyUtil.format(dh.getPhiVanChuyen()));
        txtTienDoiTra.setText(CurrencyUtil.format(dh.getTienDoiTra()));
        txtTongCong.setText(CurrencyUtil.format(dh.getTongCong()));
        txtTienThanhToan.setText(CurrencyUtil.format(dh.getTienThanhToan()));

        // Payments
        txtTienMat.setText(CurrencyUtil.format(dh.getTienMat() > 0 ? dh.getTienMat() : dh.getTienThanhToan()));
        txtChuyenKhoan.setText(CurrencyUtil.format(dh.getTienChuyenKhoan()));
        txtTheATM.setText(CurrencyUtil.format(dh.getTienTheATM()));
        txtVoucher.setText(CurrencyUtil.format(dh.getTienVoucher()));
        txtMaVoucher.setText(dh.getMaVoucher() != null ? dh.getMaVoucher() : "");
        txtTheTraTruoc.setText(CurrencyUtil.format(dh.getTienTheTraTruoc()));
        txtMaThe.setText(dh.getMaTheTraTruoc() != null ? dh.getMaTheTraTruoc() : "");
        txtTruTichLuy.setText(CurrencyUtil.format(dh.getTienTruTichLuy()));
    }

    private void refreshLayHangTable() {
        layHangModel.setRowCount(0);
        int stt = 1;
        for (DonHangChiTiet ct : currentLayHangList) {
            layHangModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    CurrencyUtil.format(ct.getDonGia()),
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0",
                    CurrencyUtil.format(ct.getTienGiam()),
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : ""
            });
        }
    }

    private void refreshTraHangTable() {
        traHangModel.setRowCount(0);
        int stt = 1;
        for (DonHangChiTiet ct : currentTraHangList) {
            traHangModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    CurrencyUtil.format(ct.getDonGia()),
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0",
                    CurrencyUtil.format(ct.getTienGiam()),
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : ""
            });
        }
    }

    private void clearRightDetails() {
        txtSoHD.setText("");
        txtGiaoHang.setText("");
        txtDienGiai.setText("");
        txtGhiChu.setText("");
        currentLayHangList.clear();
        currentTraHangList.clear();
        refreshLayHangTable();
        refreshTraHangTable();
        txtTienHang.setText("0");
        txtTienGiamGia.setText("0");
        txtPhiVanChuyen.setText("0");
        txtTienDoiTra.setText("0");
        txtTongCong.setText("0");
        txtTienThanhToan.setText("0");
        txtTienMat.setText("0");
    }

    private void onOpenDanhSachBillHuy() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        DanhMucBillHuyDialog dlg = new DanhMucBillHuyDialog(parentWin);
        dlg.setVisible(true);
    }

    private void onHuyPhieu() {
        if (currentSelectedInvoice == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn để hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "Nhập lý do hủy phiếu " + currentSelectedInvoice.getName() + ":",
                "HỦY PHIẾU BÁN HÀNG", JOptionPane.QUESTION_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            boolean ok = hoaDonDAO.cancelHoaDon(currentSelectedInvoice.getId(), reason.trim(), "Administrator");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã hủy thành công phiếu " + currentSelectedInvoice.getName() + "!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadInvoices();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi hủy phiếu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onLuuDuLieu() {
        if (currentSelectedInvoice == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn để lưu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentSelectedInvoice.setNgay(dpkNgay.getDate());
        currentSelectedInvoice.setDienGiai(txtDienGiai.getText().trim());
        currentSelectedInvoice.setGhiChu(txtGhiChu.getText().trim());
        currentSelectedInvoice.setDiaChiGiaoHang(txtGiaoHang.getText().trim());

        if (cbKhachHang.getSelectedItem() instanceof KhachHang) {
            KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
            currentSelectedInvoice.setKhachHangId(kh.getId());
            currentSelectedInvoice.setTenKhach(kh.getName());
        }
        if (cbNhanVien.getSelectedItem() instanceof NhanVien) {
            NhanVien nv = (NhanVien) cbNhanVien.getSelectedItem();
            currentSelectedInvoice.setNhanVienXuatId(nv.getId());
            currentSelectedInvoice.setTenNhanVien(nv.getName());
        }
        if (cbKhoXuat.getSelectedItem() instanceof KhoHang) {
            KhoHang kh = (KhoHang) cbKhoXuat.getSelectedItem();
            currentSelectedInvoice.setKhoXuatId(kh.getId());
        }

        boolean ok = hoaDonDAO.updateHoaDon(currentSelectedInvoice);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu hóa đơn " + currentSelectedInvoice.getName() + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadInvoices();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onThemMatHang() {
        if (currentSelectedInvoice == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn cần thêm mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        TimKiemMatHangDialog dlg = new TimKiemMatHangDialog(parentWin);
        dlg.setVisible(true);

        if (dlg.isConfirmed() && dlg.getSelectedMatHang() != null) {
            MatHang m = dlg.getSelectedMatHang();
            double price = m.getGiaBan() > 0 ? m.getGiaBan() : (m.getGiaBan2() > 0 ? m.getGiaBan2() : 0.0);

            NhapSoLuongDialog qtyDlg = new NhapSoLuongDialog(parentWin, m, price, m.getTenDonViTinh(), 1.0, 0.0);
            qtyDlg.setVisible(true);

            if (qtyDlg.isConfirmed()) {
                DonHangChiTiet ct = new DonHangChiTiet();
                ct.setId(UUID.randomUUID().toString());
                ct.setDonHangId(currentSelectedInvoice.getId());
                ct.setMatHangId(m.getId());
                ct.setMaHang(m.getCode());
                ct.setTenHang(m.getName());
                ct.setDonViTinhId(m.getDonViTinhId());
                ct.setTenDonViTinh(m.getTenDonViTinh());
                ct.setDonGia(price);
                ct.setSoLuong(qtyDlg.getResultSoLuong());
                ct.setTiLeGiamGia(qtyDlg.getResultChietKhau());
                ct.tinhThanhTien();

                int activeTab = tabbedDetails.getSelectedIndex();
                if (activeTab == 1) { // KHÁCH TRẢ HÀNG
                    ct.setTraHang(true);
                    currentTraHangList.add(ct);
                    refreshTraHangTable();
                } else { // KHÁCH LẤY HÀNG
                    ct.setTraHang(false);
                    currentLayHangList.add(ct);
                    refreshLayHangTable();
                }
                recalculateFinancials();
            }
        }
    }

    private void onXoaMatHang() {
        int activeTab = tabbedDetails.getSelectedIndex();
        if (activeTab == 1) { // KHÁCH TRẢ HÀNG
            int row = tableTraHang.getSelectedRow();
            if (row >= 0 && row < currentTraHangList.size()) {
                currentTraHangList.remove(row);
                refreshTraHangTable();
                recalculateFinancials();
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn mặt hàng cần xóa trong bảng Khách trả hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } else { // KHÁCH LẤY HÀNG
            int row = tableLayHang.getSelectedRow();
            if (row >= 0 && row < currentLayHangList.size()) {
                currentLayHangList.remove(row);
                refreshLayHangTable();
                recalculateFinancials();
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn mặt hàng cần xóa trong bảng Khách lấy hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void recalculateFinancials() {
        if (currentSelectedInvoice == null) return;

        double tienHang = 0.0;
        double tienGiamGia = 0.0;
        for (DonHangChiTiet ct : currentLayHangList) {
            tienHang += ct.getSoLuong() * ct.getDonGia();
            tienGiamGia += ct.getTienGiam();
        }

        double tienDoiTra = 0.0;
        for (DonHangChiTiet ct : currentTraHangList) {
            tienDoiTra += ct.getThanhTien();
        }

        double tongCong = Math.max(0, (tienHang - tienGiamGia) - tienDoiTra);

        currentSelectedInvoice.setChiTietLayHang(new ArrayList<>(currentLayHangList));
        currentSelectedInvoice.setChiTietTraHang(new ArrayList<>(currentTraHangList));
        currentSelectedInvoice.setTienHang(tienHang);
        currentSelectedInvoice.setTienGiamGia(tienGiamGia);
        currentSelectedInvoice.setTienDoiTra(tienDoiTra);
        currentSelectedInvoice.setTongCong(tongCong);
        currentSelectedInvoice.setTienThanhToan(tongCong);

        txtTienHang.setText(CurrencyUtil.format(tienHang));
        txtTienGiamGia.setText(CurrencyUtil.format(tienGiamGia));
        txtTienDoiTra.setText(CurrencyUtil.format(tienDoiTra));
        txtTongCong.setText(CurrencyUtil.format(tongCong));
        txtTienThanhToan.setText(CurrencyUtil.format(tongCong));
        txtTienMat.setText(CurrencyUtil.format(tongCong));
    }

    private void onLuuVaInLaiBill() {
        onLuuDuLieu();
        if (currentSelectedInvoice != null) {
            Window parentWin = SwingUtilities.getWindowAncestor(this);
            ChonMauInDialog printDlg = new ChonMauInDialog(parentWin, currentSelectedInvoice);
            printDlg.setVisible(true);
        }
    }
}
