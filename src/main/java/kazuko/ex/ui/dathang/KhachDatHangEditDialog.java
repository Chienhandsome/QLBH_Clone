package kazuko.ex.ui.dathang;

import kazuko.ex.dao.KhachDatHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.dathang.KhachDatHangChiTiet;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.danhmuc.DanhMucKhachHangPanel;
import kazuko.ex.ui.danhmuc.KhachHangEditDialog;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class KhachDatHangEditDialog extends JDialog {

    private final KhachDatHangDAO khachDatHangDAO = new KhachDatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

    private SystemConfig systemConfig;
    private KhachDatHang currentKhachDatHang;
    private final List<KhachDatHang> listForNavigation;
    private int currentNavIndex = -1;
    private final Runnable onSaveCallback;

    // Header Controls
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;

    // Customer Lookup Controls
    private JTextField txtKhachHang;
    private JButton btnKhachHangDropdown;
    private JPopupMenu popupKhachHang;
    private JTable tableLookupKhachHang;
    private DefaultTableModel lookupKhachHangModel;
    private List<KhachHang> allKhachHangList = new ArrayList<>();
    private List<KhachHang> filteredLookupKhachHangList = new ArrayList<>();
    private KhachHang selectedKhachHang = null;
    private boolean isUpdatingCustomerText = false;

    private JTextField txtTenKhach;
    private JTextField txtDiaChi;
    private JTextField txtDienThoai;
    private JTextField txtEmail;
    private JTextField txtGhiChu;

    // Financial Summary Controls
    private JTextField txtTienHang;
    private JTextField txtGiamGiaPt;
    private JTextField txtTienGiamGia;
    private JTextField txtThuePt;
    private JTextField txtTienThue;
    private JTextField txtPhiVanChuyen;
    private JTextField txtTongCong;

    // Catalog Panel (Left)
    private JTextField txtTimMatHang;
    private JTextField txtSoLuongThem;
    private JButton btnThem;
    private JTable tableCatalog;
    private DefaultTableModel catalogModel;
    private List<MatHang> allMatHangList = new ArrayList<>();
    private List<MatHang> filteredCatalogList = new ArrayList<>();

    // Details Panel (Right)
    private JTable tableChiTiet;
    private DefaultTableModel chiTietModel;
    private JTextField txtLocChiTiet;
    private final List<KhachDatHangChiTiet> currentChiTietList = new ArrayList<>();

    // Dynamic Columns config
    private boolean useGia2 = false;
    private boolean useGia3 = false;
    private boolean useGia4 = false;
    private String labelGia2 = "Đơn giá 2";
    private String labelGia3 = "Giá bán 3";
    private String labelGia4 = "Giá vốn";

    private boolean isUpdatingFinancials = false;

    public KhachDatHangEditDialog(Window parent, KhachDatHang khachDatHangToEdit, List<KhachDatHang> listForNavigation, int navIndex, Runnable onSaveSuccess) {
        super(parent, (khachDatHangToEdit == null || khachDatHangToEdit.getId() == null) ? "KHÁCH ĐẶT HÀNG - THÊM MỚI" : "KHÁCH ĐẶT HÀNG - SỬA", ModalityType.APPLICATION_MODAL);
        this.currentKhachDatHang = khachDatHangToEdit;
        this.listForNavigation = listForNavigation != null ? listForNavigation : new ArrayList<>();
        this.currentNavIndex = navIndex;
        this.onSaveCallback = onSaveSuccess;

        loadSystemConfig();
        initComponents();
        setupKeyBindings();
        loadCatalogData();

        if (this.currentKhachDatHang != null && this.currentKhachDatHang.getId() != null) {
            loadKhachDatHangData(this.currentKhachDatHang);
        } else {
            resetForNewKhachDatHang();
        }
    }

    private void loadSystemConfig() {
        try {
            systemConfig = systemConfigDAO.loadConfig();
            if (systemConfig != null) {
                useGia2 = systemConfig.isSuDungGia2();
                useGia3 = systemConfig.isSuDungGia3();
                useGia4 = systemConfig.isSuDungGia4();

                if (systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().trim().isEmpty()) {
                    labelGia2 = systemConfig.getDienGiaiGia2().trim();
                }
                if (systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().trim().isEmpty()) {
                    labelGia3 = systemConfig.getDienGiaiGia3().trim();
                }
                if (systemConfig.getDienGiaiGia4() != null && !systemConfig.getDienGiaiGia4().trim().isEmpty()) {
                    labelGia4 = systemConfig.getDienGiaiGia4().trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp cấu hình hệ thống: " + e.getMessage());
        }
    }

    private void initComponents() {
        setSize(1180, 720);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // 1. Top Header Banner
        JPanel pnlHeaderBanner = new JPanel(new BorderLayout());
        pnlHeaderBanner.setBackground(new Color(240, 245, 252));
        pnlHeaderBanner.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 235)),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JPanel pnlTitleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTitleLeft.setOpaque(false);
        JLabel lblIcon = new JLabel(IconHelper.getIcon("table.png", 22, 22));
        JLabel lblTitle = new JLabel("Khách đặt hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(25, 50, 95));
        pnlTitleLeft.add(lblIcon);
        pnlTitleLeft.add(lblTitle);

        JLabel lblShortcuts = new JLabel("(Ctrl+S: Lưu&Mới, Ctrl+L: Lưu, Ctrl+N: Mới, Ctrl+P: In, ESC: Thoát)");
        lblShortcuts.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblShortcuts.setForeground(new Color(80, 80, 80));

        pnlHeaderBanner.add(pnlTitleLeft, BorderLayout.WEST);
        pnlHeaderBanner.add(lblShortcuts, BorderLayout.EAST);

        // 2. Sub-Toolbar
        JPanel pnlSubToolbar = new JPanel(new BorderLayout());
        pnlSubToolbar.setBackground(new Color(230, 238, 248));
        pnlSubToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 235)));

        JPanel pnlToolLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlToolLeft.setOpaque(false);

        JButton btnPhimTat = createSubToolButton("Phím tắt ▾", null);
        btnPhimTat.addActionListener(e -> showShortcutsPopup(btnPhimTat));

        JButton btnTruoc = createSubToolButton("Trước (F10)", "arrow_left.png");
        btnTruoc.addActionListener(e -> navigateRecord(-1));

        JButton btnSau = createSubToolButton("Sau (F11)", "arrow_right.png");
        btnSau.addActionListener(e -> navigateRecord(1));

        JButton btnTaoMoi = createSubToolButton("Tạo mới", "page_add.png");
        btnTaoMoi.addActionListener(e -> resetForNewKhachDatHang());

        JButton btnSaoChep = createSubToolButton("Sao chép", "page_copy.png");
        btnSaoChep.addActionListener(e -> onDuplicateCurrentKhachDatHang());

        pnlToolLeft.add(btnPhimTat);
        pnlToolLeft.add(btnTruoc);
        pnlToolLeft.add(btnSau);
        pnlToolLeft.add(btnTaoMoi);
        pnlToolLeft.add(btnSaoChep);

        JPanel pnlToolRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 3));
        pnlToolRight.setOpaque(false);
        JButton btnXoaPhieu = createSubToolButton("Xóa", "cross.png");
        btnXoaPhieu.setForeground(new Color(180, 20, 20));
        btnXoaPhieu.addActionListener(e -> onDeleteCurrentKhachDatHang());
        pnlToolRight.add(btnXoaPhieu);

        pnlSubToolbar.add(pnlToolLeft, BorderLayout.WEST);
        pnlSubToolbar.add(pnlToolRight, BorderLayout.EAST);

        JPanel pnlNorthGroup = new JPanel(new BorderLayout());
        pnlNorthGroup.add(pnlHeaderBanner, BorderLayout.NORTH);
        pnlNorthGroup.add(pnlSubToolbar, BorderLayout.SOUTH);
        add(pnlNorthGroup, BorderLayout.NORTH);

        // 3. Center Section: Master info + 2 Tables
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 4));
        pnlCenter.setBorder(new EmptyBorder(6, 8, 4, 8));

        // 3.1. Master Form & Financial Summary (Top Center)
        JPanel pnlMasterContainer = createMasterAndFinancialPanel();
        pnlCenter.add(pnlMasterContainer, BorderLayout.NORTH);

        // 3.2. Split Pane: Left Catalog & Right Details
        JSplitPane splitTables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitTables.setDividerLocation(520);
        splitTables.setDividerSize(6);
        splitTables.setLeftComponent(createCatalogPanel());
        splitTables.setRightComponent(createDetailsPanel());

        pnlCenter.add(splitTables, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 4. Footer Buttons
        JPanel pnlFooter = createFooterPanel();
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JButton createSubToolButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(185, 200, 220), 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return btn;
    }

    private JPanel createMasterAndFinancialPanel() {
        JPanel pnlContainer = new JPanel(new BorderLayout(10, 0));
        pnlContainer.setBackground(new Color(245, 248, 253));
        pnlContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 220, 240), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        // LEFT: Form Thông tin phiếu
        JPanel pnlLeftForm = new JPanel(new GridBagLayout());
        pnlLeftForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Ngày + Số phiếu
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblNgay = new JLabel("Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblNgay, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.4;
        dpkNgay = new DatePickerField();
        dpkNgay.setDate(new Date());
        pnlLeftForm.add(dpkNgay, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblSoPhieu = new JLabel("Số phiếu");
        lblSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblSoPhieu, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.6;
        txtSoPhieu = new JTextField();
        txtSoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSoPhieu.setBackground(new Color(235, 245, 255));
        pnlLeftForm.add(txtSoPhieu, gbc);

        // Row 1: Khách hàng
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblKhach = new JLabel("Khách hàng");
        lblKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblKhach, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        JPanel pnlKhachLookup = createKhachHangLookupPanel();
        pnlLeftForm.add(pnlKhachLookup, gbc);
        gbc.gridwidth = 1;

        // Row 2: Tên khách
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblTenKhach = new JLabel("Tên khách");
        lblTenKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblTenKhach, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtTenKhach = new JTextField();
        txtTenKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTenKhach.setBackground(new Color(255, 255, 220)); // Highlight yellow tint as in image
        pnlLeftForm.add(txtTenKhach, gbc);
        gbc.gridwidth = 1;

        // Row 3: Địa chỉ
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblDiaChi = new JLabel("Địa chỉ");
        lblDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblDiaChi, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtDiaChi = new JTextField();
        txtDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(txtDiaChi, gbc);
        gbc.gridwidth = 1;

        // Row 4: Điện thoại
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        JLabel lblDienThoai = new JLabel("Điện thoại");
        lblDienThoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblDienThoai, gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtDienThoai = new JTextField();
        txtDienThoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(txtDienThoai, gbc);
        gbc.gridwidth = 1;

        // Row 5: Email
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(txtEmail, gbc);
        gbc.gridwidth = 1;

        // Row 6: Ghi chú
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(lblGhiChu, gbc);

        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeftForm.add(txtGhiChu, gbc);
        gbc.gridwidth = 1;

        pnlContainer.add(pnlLeftForm, BorderLayout.CENTER);

        // RIGHT: Financial Summary Box
        JPanel pnlFinancial = new JPanel(new GridBagLayout());
        pnlFinancial.setBackground(new Color(245, 248, 253));
        pnlFinancial.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 215, 235), 1),
                "Tổng cộng", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(40, 70, 120)
        ));
        pnlFinancial.setPreferredSize(new Dimension(320, 180));

        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.insets = new Insets(2, 4, 2, 4);
        fgbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Tiền hàng
        fgbc.gridx = 0; fgbc.gridy = 0; fgbc.weightx = 0;
        JLabel lblTienHang = new JLabel("Tiền hàng");
        lblTienHang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlFinancial.add(lblTienHang, fgbc);

        fgbc.gridx = 1; fgbc.gridy = 0; fgbc.gridwidth = 3; fgbc.weightx = 1.0;
        txtTienHang = createMoneyField(true);
        pnlFinancial.add(txtTienHang, fgbc);
        fgbc.gridwidth = 1;

        // 2. Giảm giá: [%] + [Tiền]
        fgbc.gridx = 0; fgbc.gridy = 1; fgbc.weightx = 0;
        JLabel lblGiamGia = new JLabel("Giảm giá");
        lblGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFinancial.add(lblGiamGia, fgbc);

        fgbc.gridx = 1; fgbc.gridy = 1; fgbc.weightx = 0.3;
        txtGiamGiaPt = new JTextField("0");
        txtGiamGiaPt.setHorizontalAlignment(JTextField.RIGHT);
        txtGiamGiaPt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFinancial.add(txtGiamGiaPt, fgbc);

        fgbc.gridx = 2; fgbc.gridy = 1; fgbc.weightx = 0;
        JLabel lblPercent1 = new JLabel("%");
        lblPercent1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFinancial.add(lblPercent1, fgbc);

        fgbc.gridx = 3; fgbc.gridy = 1; fgbc.weightx = 0.7;
        txtTienGiamGia = createMoneyField(false);
        pnlFinancial.add(txtTienGiamGia, fgbc);

        // 3. Thuế: [%] + [Tiền]
        fgbc.gridx = 0; fgbc.gridy = 2; fgbc.weightx = 0;
        JLabel lblThue = new JLabel("Thuế");
        lblThue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFinancial.add(lblThue, fgbc);

        fgbc.gridx = 1; fgbc.gridy = 2; fgbc.weightx = 0.3;
        txtThuePt = new JTextField("0");
        txtThuePt.setHorizontalAlignment(JTextField.RIGHT);
        txtThuePt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFinancial.add(txtThuePt, fgbc);

        fgbc.gridx = 2; fgbc.gridy = 2; fgbc.weightx = 0;
        JLabel lblPercent2 = new JLabel("%");
        lblPercent2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFinancial.add(lblPercent2, fgbc);

        fgbc.gridx = 3; fgbc.gridy = 2; fgbc.weightx = 0.7;
        txtTienThue = createMoneyField(false);
        pnlFinancial.add(txtTienThue, fgbc);

        // 4. Phí vận chuyển
        fgbc.gridx = 0; fgbc.gridy = 3; fgbc.weightx = 0;
        JLabel lblPhiShip = new JLabel("Phí vận chuyển");
        lblPhiShip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFinancial.add(lblPhiShip, fgbc);

        fgbc.gridx = 1; fgbc.gridy = 3; fgbc.gridwidth = 3; fgbc.weightx = 1.0;
        txtPhiVanChuyen = createMoneyField(false);
        pnlFinancial.add(txtPhiVanChuyen, fgbc);
        fgbc.gridwidth = 1;

        // 5. Tổng cộng
        fgbc.gridx = 0; fgbc.gridy = 4; fgbc.weightx = 0;
        JLabel lblTongCong = new JLabel("Tổng cộng");
        lblTongCong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlFinancial.add(lblTongCong, fgbc);

        fgbc.gridx = 1; fgbc.gridy = 4; fgbc.gridwidth = 3; fgbc.weightx = 1.0;
        txtTongCong = createMoneyField(true);
        txtTongCong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTongCong.setForeground(new Color(0, 50, 180));
        pnlFinancial.add(txtTongCong, fgbc);

        // DocumentListeners for Financial auto-calculating
        setupFinancialListeners();

        pnlContainer.add(pnlFinancial, BorderLayout.EAST);
        return pnlContainer;
    }

    private JTextField createMoneyField(boolean readOnly) {
        JTextField txt = new JTextField("0");
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txt.setHorizontalAlignment(JTextField.RIGHT);
        if (readOnly) {
            txt.setEditable(false);
            txt.setBackground(new Color(255, 255, 204)); // Yellow
        } else {
            txt.setBackground(Color.WHITE);
        }
        return txt;
    }

    private void setupFinancialListeners() {
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onFinancialInputChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onFinancialInputChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onFinancialInputChanged(); }
        };

        txtGiamGiaPt.getDocument().addDocumentListener(dl);
        txtThuePt.getDocument().addDocumentListener(dl);
        txtPhiVanChuyen.getDocument().addDocumentListener(dl);
    }

    private JPanel createCatalogPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new LineBorder(new Color(205, 220, 240), 1));

        // Top Search Bar
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlSearch.setBackground(new Color(240, 245, 252));
        pnlSearch.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 225, 240)));

        pnlSearch.add(new JLabel("Tìm:"));
        txtTimMatHang = new JTextField(12);
        txtTimMatHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimMatHang.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterCatalog(); }
            @Override public void removeUpdate(DocumentEvent e) { filterCatalog(); }
            @Override public void changedUpdate(DocumentEvent e) { filterCatalog(); }
        });
        txtTimMatHang.addActionListener(e -> addSelectedCatalogItemToDetails());
        pnlSearch.add(txtTimMatHang);

        pnlSearch.add(new JLabel("Số lượng:"));
        txtSoLuongThem = new JTextField("1", 4);
        txtSoLuongThem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoLuongThem.setHorizontalAlignment(JTextField.RIGHT);
        txtSoLuongThem.addActionListener(e -> addSelectedCatalogItemToDetails());
        pnlSearch.add(txtSoLuongThem);

        btnThem = new JButton("Thêm");
        btnThem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThem.setBackground(Color.WHITE);
        btnThem.addActionListener(e -> addSelectedCatalogItemToDetails());
        pnlSearch.add(btnThem);

        pnl.add(pnlSearch, BorderLayout.NORTH);

        // Catalog Table
        List<String> colNames = new ArrayList<>();
        colNames.add("STT");
        colNames.add("Mã hàng");
        colNames.add("Tên hàng");
        colNames.add("ĐVT");
        colNames.add("Giá bán");

        if (useGia2) colNames.add(labelGia2);
        if (useGia3) colNames.add(labelGia3);
        if (useGia4) colNames.add(labelGia4);

        catalogModel = new DefaultTableModel(colNames.toArray(), 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableCatalog = new JTable(catalogModel);
        tableCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCatalog.setRowHeight(22);
        tableCatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCatalog.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableCatalog.getTableHeader().setBackground(new Color(235, 242, 252));

        // Format column widths and alignments
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tableCatalog.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableCatalog.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableCatalog.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableCatalog.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableCatalog.getColumnModel().getColumn(3).setPreferredWidth(55);
        tableCatalog.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableCatalog.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        int colIdx = 5;
        if (useGia2) {
            tableCatalog.getColumnModel().getColumn(colIdx).setPreferredWidth(80);
            tableCatalog.getColumnModel().getColumn(colIdx).setCellRenderer(rightRenderer);
            colIdx++;
        }
        if (useGia3) {
            tableCatalog.getColumnModel().getColumn(colIdx).setPreferredWidth(80);
            tableCatalog.getColumnModel().getColumn(colIdx).setCellRenderer(rightRenderer);
            colIdx++;
        }
        if (useGia4) {
            tableCatalog.getColumnModel().getColumn(colIdx).setPreferredWidth(80);
            tableCatalog.getColumnModel().getColumn(colIdx).setCellRenderer(rightRenderer);
        }

        tableCatalog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedCatalogItemToDetails();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableCatalog);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createDetailsPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new LineBorder(new Color(205, 220, 240), 1));

        // Toolbar
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlToolbar.setBackground(new Color(240, 245, 252));
        pnlToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 225, 240)));

        JButton btnXoaDong = new JButton("Xóa dòng (F8)", IconHelper.getIcon("cross.png", 14, 14));
        btnXoaDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnXoaDong.setBackground(Color.WHITE);
        btnXoaDong.setForeground(new Color(180, 20, 20));
        btnXoaDong.addActionListener(e -> deleteSelectedDetailRow());

        JButton btnNhapExcel = new JButton("Nhập từ excel", IconHelper.getIcon("page_white_excel.png", 14, 14));
        btnNhapExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNhapExcel.setBackground(Color.WHITE);
        btnNhapExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Tính năng Nhập Excel mặt hàng đặt hàng", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        JButton btnXuatExcel = new JButton("Xuất excel", IconHelper.getIcon("export_excel.png", 14, 14));
        btnXuatExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnXuatExcel.setBackground(Color.WHITE);
        btnXuatExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Xuất danh sách chi tiết ra Excel thành công!", "Xuất Excel", JOptionPane.INFORMATION_MESSAGE));

        pnlToolbar.add(btnXoaDong);
        pnlToolbar.add(btnNhapExcel);
        pnlToolbar.add(btnXuatExcel);

        pnlToolbar.add(Box.createHorizontalStrut(10));
        pnlToolbar.add(new JLabel("Lọc"));
        txtLocChiTiet = new JTextField(8);
        txtLocChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlToolbar.add(txtLocChiTiet);

        pnl.add(pnlToolbar, BorderLayout.NORTH);

        // Details Table
        String[] colNames = {"Mã hàng", "Mặt hàng", "Số lượng", "Đơn giá", "Thành tiền", "ĐVT", "Giảm giá %", "Ghi chú"};
        chiTietModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho phép sửa: Số lượng (col 2), Đơn giá (col 3), Giảm giá % (col 6), Ghi chú (col 7)
                return column == 2 || column == 3 || column == 6 || column == 7;
            }
        };

        tableChiTiet = new JTable(chiTietModel);
        tableChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableChiTiet.setRowHeight(22);
        tableChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableChiTiet.getTableHeader().setBackground(new Color(235, 242, 252));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tableChiTiet.getColumnModel().getColumn(0).setPreferredWidth(75);
        tableChiTiet.getColumnModel().getColumn(1).setPreferredWidth(140);
        tableChiTiet.getColumnModel().getColumn(2).setPreferredWidth(60);
        tableChiTiet.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableChiTiet.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(4).setPreferredWidth(85);
        tableChiTiet.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(5).setPreferredWidth(50);
        tableChiTiet.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tableChiTiet.getColumnModel().getColumn(6).setPreferredWidth(65);
        tableChiTiet.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(7).setPreferredWidth(100);

        chiTietModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 0 && row < currentChiTietList.size()) {
                    KhachDatHangChiTiet ct = currentChiTietList.get(row);
                    if (col == 2 || col == 3 || col == 6) {
                        try {
                            double sl = parseDouble(chiTietModel.getValueAt(row, 2));
                            double dg = parseDouble(chiTietModel.getValueAt(row, 3));
                            double gg = parseDouble(chiTietModel.getValueAt(row, 6));

                            ct.setSoLuong(sl);
                            ct.setDonGia(dg);
                            ct.setTiLeGiamGia(gg);
                            ct.tinhThanhTien();

                            // Update formatted values back without infinite loop
                            SwingUtilities.invokeLater(() -> {
                                chiTietModel.setValueAt(CurrencyUtil.formatMoney(ct.getThanhTien()), row, 4);
                                recalculateFinancialTotals();
                            });
                        } catch (Exception ignored) {}
                    } else if (col == 7) {
                        ct.setNote((String) chiTietModel.getValueAt(row, 7));
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableChiTiet);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createFooterPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(240, 245, 252));
        pnl.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));

        // Left buttons
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlLeft.setOpaque(false);

        JButton btnLuuVaIn = new JButton("Lưu & In", IconHelper.getIcon("Printer.png", 16, 16));
        btnLuuVaIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaIn.addActionListener(e -> {
            if (saveKhachDatHang()) {
                openPrintDialog();
            }
        });

        JButton btnLuuVaXemIn = new JButton("Lưu & Xem in", IconHelper.getIcon("Preview.png", 16, 16));
        btnLuuVaXemIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaXemIn.addActionListener(e -> {
            if (saveKhachDatHang()) {
                openPrintDialog();
            }
        });

        pnlLeft.add(btnLuuVaIn);
        pnlLeft.add(btnLuuVaXemIn);

        // Right buttons
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        pnlRight.setOpaque(false);

        JButton btnLuu = new JButton("Lưu", IconHelper.getIcon("disk.png", 16, 16));
        btnLuu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuu.addActionListener(e -> saveKhachDatHang());

        JButton btnLuuVaMoi = new JButton("Lưu & Mới");
        btnLuuVaMoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaMoi.addActionListener(e -> {
            if (saveKhachDatHang()) {
                resetForNewKhachDatHang();
            }
        });

        JButton btnLuuVaThoat = new JButton("Lưu & thoát");
        btnLuuVaThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaThoat.addActionListener(e -> {
            if (saveKhachDatHang()) {
                dispose();
            }
        });

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.addActionListener(e -> dispose());

        pnlRight.add(btnLuu);
        pnlRight.add(btnLuuVaMoi);
        pnlRight.add(btnLuuVaThoat);
        pnlRight.add(btnThoat);

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();

        // Ctrl + S: Lưu & Mới
        root.registerKeyboardAction(e -> {
            if (saveKhachDatHang()) resetForNewKhachDatHang();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl + L: Lưu
        root.registerKeyboardAction(e -> saveKhachDatHang(),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl + N: Mới
        root.registerKeyboardAction(e -> resetForNewKhachDatHang(),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl + P: In
        root.registerKeyboardAction(e -> openPrintDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // ESC: Thoát
        root.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F10: Trước
        root.registerKeyboardAction(e -> navigateRecord(-1),
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F11: Sau
        root.registerKeyboardAction(e -> navigateRecord(1),
                KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F8: Xóa dòng chi tiết
        root.registerKeyboardAction(e -> deleteSelectedDetailRow(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void showShortcutsPopup(Component comp) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem("Ctrl + S : Lưu & Tạo mới"));
        popup.add(new JMenuItem("Ctrl + L : Lưu dữ liệu"));
        popup.add(new JMenuItem("Ctrl + N : Tạo mới"));
        popup.add(new JMenuItem("Ctrl + P : In phiếu đặt hàng"));
        popup.add(new JMenuItem("F10      : Phiếu trước"));
        popup.add(new JMenuItem("F11      : Phiếu sau"));
        popup.add(new JMenuItem("F8       : Xóa dòng chi tiết"));
        popup.add(new JMenuItem("ESC      : Thoát"));
        popup.show(comp, 0, comp.getHeight());
    }

    private JPanel createKhachHangLookupPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(new Color(185, 205, 230), 1));

        txtKhachHang = new JTextField();
        txtKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtKhachHang.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 4));

        btnKhachHangDropdown = new JButton("▾");
        btnKhachHangDropdown.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnKhachHangDropdown.setPreferredSize(new Dimension(24, 22));
        btnKhachHangDropdown.setBackground(new Color(245, 248, 253));
        btnKhachHangDropdown.setFocusPainted(false);
        btnKhachHangDropdown.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(185, 205, 230)));

        panel.add(txtKhachHang, BorderLayout.CENTER);
        panel.add(btnKhachHangDropdown, BorderLayout.EAST);

        initKhachHangLookupPopup(panel);

        btnKhachHangDropdown.addActionListener(e -> toggleKhachHangPopup(panel));
        txtKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showKhachHangPopup(panel);
            }
        });

        txtKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onKhachHangTextChanged(panel); }
            @Override public void removeUpdate(DocumentEvent e) { onKhachHangTextChanged(panel); }
            @Override public void changedUpdate(DocumentEvent e) { onKhachHangTextChanged(panel); }
        });

        txtKhachHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!popupKhachHang.isVisible()) {
                        showKhachHangPopup(panel);
                    }
                    tableLookupKhachHang.requestFocusInWindow();
                    if (tableLookupKhachHang.getRowCount() > 0 && tableLookupKhachHang.getSelectedRow() < 0) {
                        tableLookupKhachHang.setRowSelectionInterval(0, 0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupKhachHang.setVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (popupKhachHang.isVisible()) {
                        int sel = tableLookupKhachHang.getSelectedRow();
                        if (sel >= 0 && sel < filteredLookupKhachHangList.size()) {
                            selectCustomer(filteredLookupKhachHangList.get(sel));
                        }
                        popupKhachHang.setVisible(false);
                    }
                }
            }
        });

        return panel;
    }

    private void initKhachHangLookupPopup(JPanel anchor) {
        popupKhachHang = new JPopupMenu();
        popupKhachHang.setFocusable(false);

        JPanel pnlPopup = new JPanel(new BorderLayout(0, 0));
        pnlPopup.setPreferredSize(new Dimension(580, 240));
        pnlPopup.setBackground(Color.WHITE);
        pnlPopup.setBorder(new LineBorder(new Color(175, 195, 225), 1));

        String[] cols = {"Tên khách hàng", "Mã khách", "Địa chỉ", "Điện thoại", "Email"};
        lookupKhachHangModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableLookupKhachHang = new JTable(lookupKhachHangModel);
        tableLookupKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableLookupKhachHang.setRowHeight(22);
        tableLookupKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableLookupKhachHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableLookupKhachHang.getTableHeader().setBackground(new Color(255, 248, 225));
        tableLookupKhachHang.setSelectionBackground(new Color(255, 243, 205));
        tableLookupKhachHang.setSelectionForeground(Color.BLACK);

        tableLookupKhachHang.getColumnModel().getColumn(0).setPreferredWidth(140);
        tableLookupKhachHang.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableLookupKhachHang.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableLookupKhachHang.getColumnModel().getColumn(3).setPreferredWidth(95);
        tableLookupKhachHang.getColumnModel().getColumn(4).setPreferredWidth(115);

        tableLookupKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int sel = tableLookupKhachHang.getSelectedRow();
                if (sel >= 0 && sel < filteredLookupKhachHangList.size()) {
                    selectCustomer(filteredLookupKhachHangList.get(sel));
                    if (e.getClickCount() == 2) {
                        popupKhachHang.setVisible(false);
                    }
                }
            }
        });

        tableLookupKhachHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int sel = tableLookupKhachHang.getSelectedRow();
                    if (sel >= 0 && sel < filteredLookupKhachHangList.size()) {
                        selectCustomer(filteredLookupKhachHangList.get(sel));
                    }
                    popupKhachHang.setVisible(false);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupKhachHang.setVisible(false);
                    txtKhachHang.requestFocusInWindow();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableLookupKhachHang);
        pnlPopup.add(scroll, BorderLayout.CENTER);

        // Bottom toolbar in Popup (3 buttons: + Thêm, Sửa, Tải, Danh mục)
        JPanel pnlBottomToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlBottomToolbar.setBackground(new Color(240, 245, 252));
        pnlBottomToolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));

        JButton btnThem = createPopupMiniButton("+ Thêm", "plus.png", new Color(0, 130, 60));
        JButton btnSua = createPopupMiniButton("Sửa", "pencil.png", Color.BLACK);
        JButton btnTai = createPopupMiniButton("Tải", "Refresh.png", new Color(0, 80, 180));
        JButton btnDanhMuc = createPopupMiniButton("Danh mục", "table.png", Color.BLACK);

        btnThem.addActionListener(e -> {
            popupKhachHang.setVisible(false);
            onAddNewCustomerFromPopup();
        });

        btnSua.addActionListener(e -> {
            int sel = tableLookupKhachHang.getSelectedRow();
            if (sel >= 0 && sel < filteredLookupKhachHangList.size()) {
                popupKhachHang.setVisible(false);
                onEditCustomerFromPopup(filteredLookupKhachHangList.get(sel));
            } else if (selectedKhachHang != null) {
                popupKhachHang.setVisible(false);
                onEditCustomerFromPopup(selectedKhachHang);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnTai.addActionListener(e -> {
            reloadCustomerData();
            filterLookupCustomers(txtKhachHang.getText());
        });

        btnDanhMuc.addActionListener(e -> {
            popupKhachHang.setVisible(false);
            openDanhMucKhachHangDialog();
        });

        pnlBottomToolbar.add(btnThem);
        pnlBottomToolbar.add(btnSua);
        pnlBottomToolbar.add(btnTai);
        pnlBottomToolbar.add(btnDanhMuc);

        pnlPopup.add(pnlBottomToolbar, BorderLayout.SOUTH);
        popupKhachHang.add(pnlPopup);
    }

    private JButton createPopupMiniButton(String text, String iconName, Color fgColor) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(fgColor);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 205, 225), 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return btn;
    }

    private void showKhachHangPopup(JPanel anchor) {
        if (!popupKhachHang.isVisible() && anchor.isShowing()) {
            filterLookupCustomers(txtKhachHang.getText());
            popupKhachHang.show(anchor, 0, anchor.getHeight());
        }
    }

    private void toggleKhachHangPopup(JPanel anchor) {
        if (popupKhachHang.isVisible()) {
            popupKhachHang.setVisible(false);
        } else {
            showKhachHangPopup(anchor);
        }
    }

    private void onKhachHangTextChanged(JPanel anchor) {
        if (isUpdatingCustomerText) return;
        String query = txtKhachHang.getText().trim();
        if (query.isEmpty()) {
            selectedKhachHang = null;
            setCustomerFieldsEditable(true);
        }
        filterLookupCustomers(query);
        if (!popupKhachHang.isVisible() && anchor.isShowing() && txtKhachHang.hasFocus()) {
            popupKhachHang.show(anchor, 0, anchor.getHeight());
        }
    }

    private void filterLookupCustomers(String keyword) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        filteredLookupKhachHangList.clear();
        lookupKhachHangModel.setRowCount(0);

        for (KhachHang kh : allKhachHangList) {
            boolean match = kw.isEmpty() ||
                    (kh.getName() != null && kh.getName().toLowerCase().contains(kw)) ||
                    (kh.getMaKhach() != null && kh.getMaKhach().toLowerCase().contains(kw)) ||
                    (kh.getDienThoai() != null && kh.getDienThoai().contains(kw)) ||
                    (kh.getDiaChi() != null && kh.getDiaChi().toLowerCase().contains(kw)) ||
                    (kh.getEmail() != null && kh.getEmail().toLowerCase().contains(kw));
            if (match) {
                filteredLookupKhachHangList.add(kh);
                lookupKhachHangModel.addRow(new Object[]{
                        kh.getName() != null ? kh.getName() : "",
                        kh.getMaKhach() != null ? kh.getMaKhach() : "",
                        kh.getDiaChi() != null ? kh.getDiaChi() : "",
                        kh.getDienThoai() != null ? kh.getDienThoai() : "",
                        kh.getEmail() != null ? kh.getEmail() : ""
                });
            }
        }
    }

    private void selectCustomer(KhachHang kh) {
        this.selectedKhachHang = kh;
        isUpdatingCustomerText = true;
        try {
            if (kh != null) {
                txtKhachHang.setText(kh.getName() != null ? kh.getName() : "");
                txtTenKhach.setText(kh.getName() != null ? kh.getName() : "");
                txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
                txtDienThoai.setText(kh.getDienThoai() != null ? kh.getDienThoai() : "");
                txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
                setCustomerFieldsEditable(false);
            } else {
                txtKhachHang.setText("");
                setCustomerFieldsEditable(true);
            }
        } finally {
            isUpdatingCustomerText = false;
        }
    }

    private void reloadCustomerData() {
        allKhachHangList = khachHangDAO.getAll();
    }

    private void onAddNewCustomerFromPopup() {
        KhachHangEditDialog dlg = new KhachHangEditDialog(this, null, () -> {
            reloadCustomerData();
            if (!allKhachHangList.isEmpty()) {
                KhachHang newest = allKhachHangList.get(allKhachHangList.size() - 1);
                selectCustomer(newest);
            }
        });
        dlg.setVisible(true);
    }

    private void onEditCustomerFromPopup(KhachHang kh) {
        if (kh == null) return;
        KhachHangEditDialog dlg = new KhachHangEditDialog(this, kh, () -> {
            reloadCustomerData();
            KhachHang refreshed = khachHangDAO.findActiveById(kh.getId());
            if (refreshed != null) {
                selectCustomer(refreshed);
            }
        });
        dlg.setVisible(true);
    }

    private void openDanhMucKhachHangDialog() {
        JDialog dlg = new JDialog(this, "DANH SÁCH KHÁCH HÀNG", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(950, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        DanhMucKhachHangPanel pnl = new DanhMucKhachHangPanel();
        dlg.add(pnl, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setBackground(new Color(240, 245, 252));
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));

        JButton btnChon = new JButton("Chọn khách hàng");
        btnChon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChon.setBackground(new Color(230, 240, 255));
        btnChon.addActionListener(e -> {
            KhachHang sel = pnl.getSelectedKhachHang();
            if (sel != null) {
                selectCustomer(sel);
                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn một khách hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.addActionListener(e -> dlg.dispose());

        pnlBottom.add(btnChon);
        pnlBottom.add(btnThoat);
        dlg.add(pnlBottom, BorderLayout.SOUTH);

        pnl.setOnCustomerDoubleClickListener(kh -> {
            selectCustomer(kh);
            dlg.dispose();
        });

        dlg.setVisible(true);
    }

    private void loadCatalogData() {
        // Load Customers into Lookup List
        reloadCustomerData();

        // Load Products into Catalog Table
        allMatHangList = matHangDAO.getAll();
        filterCatalog();
    }

    private void filterCatalog() {
        String keyword = txtTimMatHang.getText() != null ? txtTimMatHang.getText().trim().toLowerCase() : "";
        filteredCatalogList.clear();
        catalogModel.setRowCount(0);

        int stt = 1;
        for (MatHang mh : allMatHangList) {
            boolean match = keyword.isEmpty() ||
                    (mh.getCode() != null && mh.getCode().toLowerCase().contains(keyword)) ||
                    (mh.getName() != null && mh.getName().toLowerCase().contains(keyword));
            if (match) {
                filteredCatalogList.add(mh);
                List<Object> row = new ArrayList<>();
                row.add(String.format("%04d", stt++));
                row.add(mh.getCode() != null ? mh.getCode() : "");
                row.add(mh.getName() != null ? mh.getName() : "");
                row.add(mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "");
                row.add(CurrencyUtil.formatMoney(mh.getGiaBan()));

                if (useGia2) row.add(CurrencyUtil.formatMoney(mh.getGiaBan2()));
                if (useGia3) row.add(CurrencyUtil.formatMoney(mh.getGiaBan3()));
                if (useGia4) row.add(CurrencyUtil.formatMoney(mh.getGiaVon()));

                catalogModel.addRow(row.toArray());
            }
        }
    }

    private void setCustomerFieldsEditable(boolean editable) {
        txtTenKhach.setEditable(editable);
        txtDiaChi.setEditable(editable);
        txtDienThoai.setEditable(editable);
        txtEmail.setEditable(editable);

        Color bg = editable ? Color.WHITE : new Color(238, 238, 238);
        txtTenKhach.setBackground(bg);
        txtDiaChi.setBackground(bg);
        txtDienThoai.setBackground(bg);
        txtEmail.setBackground(bg);
    }

    private void addSelectedCatalogItemToDetails() {
        int selRow = tableCatalog.getSelectedRow();
        if (selRow < 0) {
            if (filteredCatalogList.size() == 1) {
                selRow = 0;
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một mặt hàng trong danh mục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        MatHang mh = filteredCatalogList.get(selRow);
        double qty = 1.0;
        try {
            qty = Double.parseDouble(txtSoLuongThem.getText().trim().replace(",", ""));
        } catch (Exception ignored) {}
        if (qty <= 0) qty = 1.0;

        // Check if item already exists in details list
        KhachDatHangChiTiet existing = null;
        int existingIndex = -1;
        for (int i = 0; i < currentChiTietList.size(); i++) {
            KhachDatHangChiTiet ct = currentChiTietList.get(i);
            if (ct.getdMatHangId() != null && ct.getdMatHangId().equals(mh.getId())) {
                existing = ct;
                existingIndex = i;
                break;
            }
        }

        if (existing != null) {
            existing.setSoLuong(existing.getSoLuong() + qty);
            existing.tinhThanhTien();
            chiTietModel.setValueAt(CurrencyUtil.formatQuantity(existing.getSoLuong()), existingIndex, 2);
            chiTietModel.setValueAt(CurrencyUtil.formatMoney(existing.getThanhTien()), existingIndex, 4);
        } else {
            KhachDatHangChiTiet ct = new KhachDatHangChiTiet();
            ct.setId(UUID.randomUUID().toString());
            ct.setdMatHangId(mh.getId());
            ct.setMaHang(mh.getCode());
            ct.setTenHang(mh.getName());
            ct.setdDonViTinhId(mh.getDonViTinhId());
            ct.setTenDonViTinh(mh.getTenDonViTinh());
            ct.setSoLuong(qty);
            ct.setDonGia(mh.getGiaBan());
            ct.setTiLeGiamGia(0.0);
            ct.tinhThanhTien();
            ct.setNote("");

            currentChiTietList.add(ct);
            chiTietModel.addRow(new Object[]{
                    ct.getMaHang(),
                    ct.getTenHang(),
                    CurrencyUtil.formatQuantity(ct.getSoLuong()),
                    CurrencyUtil.formatMoney(ct.getDonGia()),
                    CurrencyUtil.formatMoney(ct.getThanhTien()),
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    "0",
                    ""
            });
        }

        recalculateFinancialTotals();
        txtTimMatHang.setText("");
        txtSoLuongThem.setText("1");
        txtTimMatHang.requestFocusInWindow();
    }

    private void deleteSelectedDetailRow() {
        int sel = tableChiTiet.getSelectedRow();
        if (sel >= 0 && sel < currentChiTietList.size()) {
            currentChiTietList.remove(sel);
            chiTietModel.removeRow(sel);
            recalculateFinancialTotals();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng chi tiết cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onFinancialInputChanged() {
        if (isUpdatingFinancials) return;
        recalculateFinancialTotals();
    }

    private void recalculateFinancialTotals() {
        isUpdatingFinancials = true;
        try {
            double totalTienHang = 0;
            for (KhachDatHangChiTiet ct : currentChiTietList) {
                totalTienHang += ct.getThanhTien();
            }

            txtTienHang.setText(CurrencyUtil.formatMoney(totalTienHang));

            double giamGiaPt = parseDouble(txtGiamGiaPt.getText());
            double tienGiamGia = totalTienHang * (giamGiaPt / 100.0);
            txtTienGiamGia.setText(CurrencyUtil.formatMoney(tienGiamGia));

            double thuePt = parseDouble(txtThuePt.getText());
            double tienThue = (totalTienHang - tienGiamGia) * (thuePt / 100.0);
            txtTienThue.setText(CurrencyUtil.formatMoney(tienThue));

            double phiShip = parseDouble(txtPhiVanChuyen.getText());

            double tongCong = totalTienHang - tienGiamGia + tienThue + phiShip;
            txtTongCong.setText(CurrencyUtil.formatMoney(tongCong));
        } finally {
            isUpdatingFinancials = false;
        }
    }

    private void resetForNewKhachDatHang() {
        currentKhachDatHang = new KhachDatHang();
        currentKhachDatHang.setId(UUID.randomUUID().toString());
        currentKhachDatHang.setNgay(new Timestamp(System.currentTimeMillis()));

        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(khachDatHangDAO.generateNextSoPhieu(new Date()));
        selectCustomer(null);
        txtTenKhach.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtEmail.setText("");
        txtGhiChu.setText("");
        setCustomerFieldsEditable(true);

        txtGiamGiaPt.setText("0");
        txtThuePt.setText("0");
        txtPhiVanChuyen.setText("0");

        currentChiTietList.clear();
        chiTietModel.setRowCount(0);
        recalculateFinancialTotals();
        setTitle("KHÁCH ĐẶT HÀNG - THÊM MỚI");
    }

    private void loadKhachDatHangData(KhachDatHang bg) {
        if (bg == null) return;
        this.currentKhachDatHang = bg;
        setTitle("KHÁCH ĐẶT HÀNG - SỬA: " + (bg.getName() != null ? bg.getName() : ""));

        if (bg.getNgay() != null) {
            dpkNgay.setDate(new Date(bg.getNgay().getTime()));
        }
        txtSoPhieu.setText(bg.getName() != null ? bg.getName() : "");

        // Select Customer in Lookup
        if (bg.getdKhachHangId() != null) {
            KhachHang found = null;
            for (KhachHang kh : allKhachHangList) {
                if (kh != null && bg.getdKhachHangId().equals(kh.getId())) {
                    found = kh;
                    break;
                }
            }
            if (found != null) {
                selectCustomer(found);
            } else {
                this.selectedKhachHang = null;
                txtKhachHang.setText(bg.getTenKhach() != null ? bg.getTenKhach() : "");
                txtTenKhach.setText(bg.getTenKhach() != null ? bg.getTenKhach() : "");
                txtDiaChi.setText(bg.getDiaChi() != null ? bg.getDiaChi() : "");
                txtDienThoai.setText(bg.getDienThoai() != null ? bg.getDienThoai() : "");
                txtEmail.setText(bg.getEmail() != null ? bg.getEmail() : "");
                setCustomerFieldsEditable(false);
            }
        } else {
            selectCustomer(null);
            txtTenKhach.setText(bg.getTenKhach() != null ? bg.getTenKhach() : "");
            txtDiaChi.setText(bg.getDiaChi() != null ? bg.getDiaChi() : "");
            txtDienThoai.setText(bg.getDienThoai() != null ? bg.getDienThoai() : "");
            txtEmail.setText(bg.getEmail() != null ? bg.getEmail() : "");
            setCustomerFieldsEditable(true);
        }

        txtGhiChu.setText(bg.getNote() != null ? bg.getNote() : "");

        txtGiamGiaPt.setText(String.valueOf(bg.getTiLeGiamGia()));
        txtThuePt.setText(String.valueOf(bg.getTiLeThue()));
        txtPhiVanChuyen.setText(CurrencyUtil.formatMoney(bg.getPhiVanChuyen()));

        // Load Details
        currentChiTietList.clear();
        chiTietModel.setRowCount(0);
        List<KhachDatHangChiTiet> details = khachDatHangDAO.getChiTietByKhachDatHangId(bg.getId(), bg.getName());
        currentChiTietList.addAll(details);

        for (KhachDatHangChiTiet ct : details) {
            chiTietModel.addRow(new Object[]{
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    CurrencyUtil.formatQuantity(ct.getSoLuong()),
                    CurrencyUtil.formatMoney(ct.getDonGia()),
                    CurrencyUtil.formatMoney(ct.getThanhTien()),
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    String.valueOf(ct.getTiLeGiamGia()),
                    ct.getNote() != null ? ct.getNote() : ""
            });
        }

        recalculateFinancialTotals();
    }

    private boolean saveKhachDatHang() {
        if (txtSoPhieu.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số phiếu đặt hàng không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtSoPhieu.requestFocus();
            return false;
        }

        if (currentKhachDatHang == null) {
            currentKhachDatHang = new KhachDatHang();
            currentKhachDatHang.setId(UUID.randomUUID().toString());
        }

        currentKhachDatHang.setName(txtSoPhieu.getText().trim());
        Date date = dpkNgay.getDate();
        currentKhachDatHang.setNgay(date != null ? new Timestamp(date.getTime()) : new Timestamp(System.currentTimeMillis()));

        if (selectedKhachHang != null) {
            currentKhachDatHang.setdKhachHangId(selectedKhachHang.getId());
        } else {
            currentKhachDatHang.setdKhachHangId(null);
        }

        currentKhachDatHang.setTenKhach(txtTenKhach.getText().trim());
        currentKhachDatHang.setDiaChi(txtDiaChi.getText().trim());
        currentKhachDatHang.setDienThoai(txtDienThoai.getText().trim());
        currentKhachDatHang.setEmail(txtEmail.getText().trim());
        currentKhachDatHang.setNote(txtGhiChu.getText().trim());

        currentKhachDatHang.setTienHang(parseDouble(txtTienHang.getText()));
        currentKhachDatHang.setTiLeGiamGia(parseDouble(txtGiamGiaPt.getText()));
        currentKhachDatHang.setTienGiamGia(parseDouble(txtTienGiamGia.getText()));
        currentKhachDatHang.setTiLeThue(parseDouble(txtThuePt.getText()));
        currentKhachDatHang.setTienThue(parseDouble(txtTienThue.getText()));
        currentKhachDatHang.setPhiVanChuyen(parseDouble(txtPhiVanChuyen.getText()));
        currentKhachDatHang.setTongCong(parseDouble(txtTongCong.getText()));
        currentKhachDatHang.setStatus(1);

        boolean success;
        KhachDatHang existing = khachDatHangDAO.getKhachDatHangById(currentKhachDatHang.getId());
        if (existing == null) {
            success = khachDatHangDAO.insertKhachDatHang(currentKhachDatHang, currentChiTietList);
        } else {
            success = khachDatHangDAO.updateKhachDatHang(currentKhachDatHang, currentChiTietList);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu phiếu đặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Lưu phiếu đặt hàng thất bại. Vui lòng kiểm tra lại kết nối CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void onDuplicateCurrentKhachDatHang() {
        if (currentKhachDatHang == null) return;
        currentKhachDatHang.setId(UUID.randomUUID().toString());
        currentKhachDatHang.setName(khachDatHangDAO.generateNextSoPhieu(new Date()));
        txtSoPhieu.setText(currentKhachDatHang.getName());
        dpkNgay.setDate(new Date());

        for (KhachDatHangChiTiet ct : currentChiTietList) {
            ct.setId(UUID.randomUUID().toString());
            ct.settKhachDatHangId(currentKhachDatHang.getId());
        }

        setTitle("KHÁCH ĐẶT HÀNG - THÊM MỚI (Bản sao: " + currentKhachDatHang.getName() + ")");
        JOptionPane.showMessageDialog(this, "Đã nhân bản phiếu thành công với số phiếu mới: " + currentKhachDatHang.getName() + "!", "Sao chép", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeleteCurrentKhachDatHang() {
        if (currentKhachDatHang == null || currentKhachDatHang.getId() == null) return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu đặt hàng: " + currentKhachDatHang.getName() + " không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (khachDatHangDAO.deleteKhachDatHang(currentKhachDatHang.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu đặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                if (onSaveCallback != null) onSaveCallback.run();
                dispose();
            }
        }
    }

    private void navigateRecord(int direction) {
        if (listForNavigation.isEmpty()) return;
        int newIndex = currentNavIndex + direction;
        if (newIndex >= 0 && newIndex < listForNavigation.size()) {
            currentNavIndex = newIndex;
            KhachDatHang navBg = listForNavigation.get(currentNavIndex);
            loadKhachDatHangData(navBg);
        } else {
            JOptionPane.showMessageDialog(this, direction < 0 ? "Đã ở phiếu đầu tiên!" : "Đã ở phiếu cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openPrintDialog() {
        InLuoiDatHangDialog dlg = new InLuoiDatHangDialog(this);
        dlg.setVisible(true);
    }

    private double parseDouble(Object val) {
        if (val == null) return 0.0;
        try {
            String str = val.toString().replace(",", "").replace(" ", "").trim();
            if (str.isEmpty()) return 0.0;
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
