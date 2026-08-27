package kazuko.ex.ui.baogia;

import kazuko.ex.dao.BaoGiaDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.baogia.BaoGia;
import kazuko.ex.model.baogia.BaoGiaChiTiet;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.ui.common.DatePickerField;
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

public class BaoGiaEditDialog extends JDialog {

    private final BaoGiaDAO baoGiaDAO = new BaoGiaDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

    private SystemConfig systemConfig;
    private BaoGia currentBaoGia;
    private final List<BaoGia> listForNavigation;
    private int currentNavIndex = -1;
    private final Runnable onSaveCallback;

    // Header Controls
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private JComboBox<KhachHang> cbKhachHang;
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
    private final List<BaoGiaChiTiet> currentChiTietList = new ArrayList<>();

    // Dynamic Columns config
    private boolean useGia2 = false;
    private boolean useGia3 = false;
    private boolean useGia4 = false;
    private String labelGia2 = "Đơn giá 2";
    private String labelGia3 = "Giá bán 3";
    private String labelGia4 = "Giá vốn";

    private boolean isUpdatingFinancials = false;

    public BaoGiaEditDialog(Window parent, BaoGia baoGiaToEdit, List<BaoGia> listForNavigation, int navIndex, Runnable onSaveSuccess) {
        super(parent, (baoGiaToEdit == null || baoGiaToEdit.getId() == null) ? "BÁO GIÁ - THÊM MỚI" : "BÁO GIÁ - CHỈNH SỬA", ModalityType.APPLICATION_MODAL);
        this.currentBaoGia = baoGiaToEdit;
        this.listForNavigation = listForNavigation != null ? listForNavigation : new ArrayList<>();
        this.currentNavIndex = navIndex;
        this.onSaveCallback = onSaveSuccess;

        loadSystemConfig();
        initComponents();
        setupKeyBindings();
        loadCatalogData();

        if (this.currentBaoGia != null && this.currentBaoGia.getId() != null) {
            loadBaoGiaData(this.currentBaoGia);
        } else {
            resetForNewBaoGia();
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
        JLabel lblTitle = new JLabel("Báo giá");
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
        btnTaoMoi.addActionListener(e -> resetForNewBaoGia());

        JButton btnSaoChep = createSubToolButton("Sao chép", "page_copy.png");
        btnSaoChep.addActionListener(e -> onDuplicateCurrentBaoGia());

        pnlToolLeft.add(btnPhimTat);
        pnlToolLeft.add(btnTruoc);
        pnlToolLeft.add(btnSau);
        pnlToolLeft.add(btnTaoMoi);
        pnlToolLeft.add(btnSaoChep);

        JPanel pnlToolRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 3));
        pnlToolRight.setOpaque(false);
        JButton btnXoaPhieu = createSubToolButton("Xóa", "cross.png");
        btnXoaPhieu.setForeground(new Color(180, 20, 20));
        btnXoaPhieu.addActionListener(e -> onDeleteCurrentBaoGia());
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
        cbKhachHang = new JComboBox<>();
        cbKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhachHang.setBackground(Color.WHITE);
        cbKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    KhachHang kh = (KhachHang) value;
                    l.setText(kh.getName() + (kh.getDienThoai() != null && !kh.getDienThoai().isEmpty() ? " - " + kh.getDienThoai() : ""));
                } else if (value == null) {
                    l.setText("-- Chọn khách hàng --");
                }
                return l;
            }
        });
        cbKhachHang.addActionListener(e -> onKhachHangSelected());
        pnlLeftForm.add(cbKhachHang, gbc);
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
        btnNhapExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Tính năng Nhập Excel mặt hàng báo giá", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

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
                    BaoGiaChiTiet ct = currentChiTietList.get(row);
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
            if (saveBaoGia()) {
                openPrintDialog();
            }
        });

        JButton btnLuuVaXemIn = new JButton("Lưu & Xem in", IconHelper.getIcon("Preview.png", 16, 16));
        btnLuuVaXemIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaXemIn.addActionListener(e -> {
            if (saveBaoGia()) {
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
        btnLuu.addActionListener(e -> saveBaoGia());

        JButton btnLuuVaMoi = new JButton("Lưu & Mới");
        btnLuuVaMoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaMoi.addActionListener(e -> {
            if (saveBaoGia()) {
                resetForNewBaoGia();
            }
        });

        JButton btnLuuVaThoat = new JButton("Lưu & thoát");
        btnLuuVaThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLuuVaThoat.addActionListener(e -> {
            if (saveBaoGia()) {
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
            if (saveBaoGia()) resetForNewBaoGia();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl + L: Lưu
        root.registerKeyboardAction(e -> saveBaoGia(),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl + N: Mới
        root.registerKeyboardAction(e -> resetForNewBaoGia(),
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
        popup.add(new JMenuItem("Ctrl + P : In phiếu báo giá"));
        popup.add(new JMenuItem("F10      : Phiếu trước"));
        popup.add(new JMenuItem("F11      : Phiếu sau"));
        popup.add(new JMenuItem("F8       : Xóa dòng chi tiết"));
        popup.add(new JMenuItem("ESC      : Thoát"));
        popup.show(comp, 0, comp.getHeight());
    }

    private void loadCatalogData() {
        // Load Customers into ComboBox
        cbKhachHang.removeAllItems();
        cbKhachHang.addItem(null); // Empty choice
        List<KhachHang> khachList = khachHangDAO.getAll();
        for (KhachHang kh : khachList) {
            cbKhachHang.addItem(kh);
        }

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

    private void onKhachHangSelected() {
        Object item = cbKhachHang.getSelectedItem();
        if (item instanceof KhachHang) {
            KhachHang kh = (KhachHang) item;
            txtTenKhach.setText(kh.getName() != null ? kh.getName() : "");
            txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
            txtDienThoai.setText(kh.getDienThoai() != null ? kh.getDienThoai() : "");
            txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
        }
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
        BaoGiaChiTiet existing = null;
        int existingIndex = -1;
        for (int i = 0; i < currentChiTietList.size(); i++) {
            BaoGiaChiTiet ct = currentChiTietList.get(i);
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
            BaoGiaChiTiet ct = new BaoGiaChiTiet();
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
            for (BaoGiaChiTiet ct : currentChiTietList) {
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

    private void resetForNewBaoGia() {
        currentBaoGia = new BaoGia();
        currentBaoGia.setId(UUID.randomUUID().toString());
        currentBaoGia.setNgay(new Timestamp(System.currentTimeMillis()));

        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(baoGiaDAO.generateNextSoPhieu(new Date()));
        cbKhachHang.setSelectedItem(null);
        txtTenKhach.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtEmail.setText("");
        txtGhiChu.setText("");

        txtGiamGiaPt.setText("0");
        txtThuePt.setText("0");
        txtPhiVanChuyen.setText("0");

        currentChiTietList.clear();
        chiTietModel.setRowCount(0);
        recalculateFinancialTotals();
        setTitle("BÁO GIÁ - THÊM MỚI");
    }

    private void loadBaoGiaData(BaoGia bg) {
        if (bg == null) return;
        this.currentBaoGia = bg;
        setTitle("BÁO GIÁ - CHỈNH SỬA: " + (bg.getName() != null ? bg.getName() : ""));

        if (bg.getNgay() != null) {
            dpkNgay.setDate(new Date(bg.getNgay().getTime()));
        }
        txtSoPhieu.setText(bg.getName() != null ? bg.getName() : "");

        // Select Customer in Combobox
        if (bg.getdKhachHangId() != null) {
            for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
                KhachHang kh = cbKhachHang.getItemAt(i);
                if (kh != null && bg.getdKhachHangId().equals(kh.getId())) {
                    cbKhachHang.setSelectedIndex(i);
                    break;
                }
            }
        }

        txtTenKhach.setText(bg.getTenKhach() != null ? bg.getTenKhach() : "");
        txtDiaChi.setText(bg.getDiaChi() != null ? bg.getDiaChi() : "");
        txtDienThoai.setText(bg.getDienThoai() != null ? bg.getDienThoai() : "");
        txtEmail.setText(bg.getEmail() != null ? bg.getEmail() : "");
        txtGhiChu.setText(bg.getNote() != null ? bg.getNote() : "");

        txtGiamGiaPt.setText(String.valueOf(bg.getTiLeGiamGia()));
        txtThuePt.setText(String.valueOf(bg.getTiLeThue()));
        txtPhiVanChuyen.setText(CurrencyUtil.formatMoney(bg.getPhiVanChuyen()));

        // Load Details
        currentChiTietList.clear();
        chiTietModel.setRowCount(0);
        List<BaoGiaChiTiet> details = baoGiaDAO.getChiTietByBaoGiaId(bg.getId(), bg.getName());
        currentChiTietList.addAll(details);

        for (BaoGiaChiTiet ct : details) {
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

    private boolean saveBaoGia() {
        if (txtSoPhieu.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số phiếu báo giá không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtSoPhieu.requestFocus();
            return false;
        }

        if (currentBaoGia == null) {
            currentBaoGia = new BaoGia();
            currentBaoGia.setId(UUID.randomUUID().toString());
        }

        currentBaoGia.setName(txtSoPhieu.getText().trim());
        Date date = dpkNgay.getDate();
        currentBaoGia.setNgay(date != null ? new Timestamp(date.getTime()) : new Timestamp(System.currentTimeMillis()));

        Object selKh = cbKhachHang.getSelectedItem();
        if (selKh instanceof KhachHang) {
            currentBaoGia.setdKhachHangId(((KhachHang) selKh).getId());
        } else {
            currentBaoGia.setdKhachHangId(null);
        }

        currentBaoGia.setTenKhach(txtTenKhach.getText().trim());
        currentBaoGia.setDiaChi(txtDiaChi.getText().trim());
        currentBaoGia.setDienThoai(txtDienThoai.getText().trim());
        currentBaoGia.setEmail(txtEmail.getText().trim());
        currentBaoGia.setNote(txtGhiChu.getText().trim());

        currentBaoGia.setTienHang(parseDouble(txtTienHang.getText()));
        currentBaoGia.setTiLeGiamGia(parseDouble(txtGiamGiaPt.getText()));
        currentBaoGia.setTienGiamGia(parseDouble(txtTienGiamGia.getText()));
        currentBaoGia.setTiLeThue(parseDouble(txtThuePt.getText()));
        currentBaoGia.setTienThue(parseDouble(txtTienThue.getText()));
        currentBaoGia.setPhiVanChuyen(parseDouble(txtPhiVanChuyen.getText()));
        currentBaoGia.setTongCong(parseDouble(txtTongCong.getText()));
        currentBaoGia.setStatus(1);

        boolean success;
        BaoGia existing = baoGiaDAO.getBaoGiaById(currentBaoGia.getId());
        if (existing == null) {
            success = baoGiaDAO.insertBaoGia(currentBaoGia, currentChiTietList);
        } else {
            success = baoGiaDAO.updateBaoGia(currentBaoGia, currentChiTietList);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu phiếu báo giá thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Lưu phiếu báo giá thất bại. Vui lòng kiểm tra lại kết nối CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void onDuplicateCurrentBaoGia() {
        if (currentBaoGia == null) return;
        currentBaoGia.setId(UUID.randomUUID().toString());
        currentBaoGia.setName(baoGiaDAO.generateNextSoPhieu(new Date()));
        txtSoPhieu.setText(currentBaoGia.getName());
        dpkNgay.setDate(new Date());

        for (BaoGiaChiTiet ct : currentChiTietList) {
            ct.setId(UUID.randomUUID().toString());
            ct.settBaoGiaId(currentBaoGia.getId());
        }

        setTitle("BÁO GIÁ - THÊM MỚI (Bản sao: " + currentBaoGia.getName() + ")");
        JOptionPane.showMessageDialog(this, "Đã nhân bản phiếu thành công với số phiếu mới: " + currentBaoGia.getName() + "!", "Sao chép", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeleteCurrentBaoGia() {
        if (currentBaoGia == null || currentBaoGia.getId() == null) return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu báo giá: " + currentBaoGia.getName() + " không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (baoGiaDAO.deleteBaoGia(currentBaoGia.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu báo giá thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
            BaoGia navBg = listForNavigation.get(currentNavIndex);
            loadBaoGiaData(navBg);
        } else {
            JOptionPane.showMessageDialog(this, direction < 0 ? "Đã ở phiếu đầu tiên!" : "Đã ở phiếu cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openPrintDialog() {
        InLuoiBaoGiaDialog dlg = new InLuoiBaoGiaDialog(this);
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
