package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhieuChuyenKhoDialog extends JDialog {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private DonHang currentDonHang;
    private final Runnable onDataSaved;

    // Form fields
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private JComboBox<KhoHang> cbKhoXuat;
    private JComboBox<NhanVien> cbNhanVienXuat;
    private JComboBox<KhoHang> cbKhoNhap;
    private JComboBox<NhanVien> cbNhanVienNhap;
    private JTextField txtDienGiai;
    private JTextField txtGhiChu;

    // Quick add controls
    private JTextField txtTimF3;
    private JSpinner spnSoLuong;
    private JButton btnThemQuick;

    // Summary fields (Top right)
    private JLabel lblTienHangVal;
    private JTextField txtTiLeGiamGia;
    private JTextField txtTienGiamGia;
    private JTextField txtTiLeThue;
    private JTextField txtTienThue;
    private JTextField txtPhiVanChuyen;
    private JLabel lblTongCongVal;

    // Catalog Table (Left)
    private JTable tableCatalog;
    private DefaultTableModel catalogModel;
    private List<MatHang> allCatalogItems = new ArrayList<>();
    private List<MatHang> filteredCatalogItems = new ArrayList<>();

    // Detail Table (Right)
    private JTable tableChiTiet;
    private DefaultTableModel chiTietModel;
    private final List<DonHangChiTiet> lineItems = new ArrayList<>();

    private final DecimalFormat df = new DecimalFormat("#,##0.##");
    private final DecimalFormat dfStt = new DecimalFormat("0000");
    private boolean isUpdatingTotals = false;

    public PhieuChuyenKhoDialog(Window parent, Runnable onDataSaved) {
        this(parent, null, onDataSaved);
    }

    public PhieuChuyenKhoDialog(Window parent, DonHang donHang, Runnable onDataSaved) {
        super(parent, donHang == null ? "PHIẾU CHUYỂN KHO - THÊM MỚI" : "PHIẾU CHUYỂN KHO - SỬA", ModalityType.APPLICATION_MODAL);
        this.currentDonHang = donHang;
        this.onDataSaved = onDataSaved;

        initComponents();
        loadDropdownData();
        loadCatalogData();

        if (donHang != null) {
            populateExistingData();
        } else {
            resetForNew();
        }

        setupKeyBindings();
        setSize(1200, 720);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(235, 242, 250));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 0));
        pnlTop.setOpaque(false);

        pnlTop.add(createHeaderBanner(), BorderLayout.NORTH);
        pnlTop.add(createTopActionBar(), BorderLayout.CENTER);
        pnlTop.add(createFormAndSummaryPanel(), BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);
        add(createCenterTablesSplitPane(), BorderLayout.CENTER);
        add(createBottomButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderBanner() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(214, 232, 250));
        pnl.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel lblTitle = new JLabel("Phiếu chuyển kho");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(20, 60, 120));
        lblTitle.setIcon(IconHelper.getIcon("table_tab.png", 18, 18));

        JLabel lblHints = new JLabel("(Ctrl+S: Lưu&Mới, Ctrl+L: Lưu, Ctrl+N: Mới, Ctrl+P: In, ESC: Thoát)");
        lblHints.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHints.setForeground(new Color(90, 100, 115));

        pnl.add(lblTitle, BorderLayout.WEST);
        pnl.add(lblHints, BorderLayout.EAST);
        return pnl;
    }

    private JPanel createTopActionBar() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(225, 237, 250));
        pnl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(195, 215, 238)));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnlLeft.setOpaque(false);

        JButton btnShortcuts = createToolButton("Phím tắt", "help.png");
        btnShortcuts.addActionListener(e -> showShortcutHelp());

        JButton btnPrev = createToolButton("◀ Trước (F10)", "resultset_previous.png");
        btnPrev.addActionListener(e -> navigateRecord(-1));

        JButton btnNext = createToolButton("Sau (F11) ▶", "resultset_next.png");
        btnNext.addActionListener(e -> navigateRecord(1));

        JButton btnNew = createToolButton("+ Tạo mới", "plus.png");
        btnNew.addActionListener(e -> resetForNew());

        JButton btnCopy = createToolButton("Sao chép", "page_copy.png");
        btnCopy.addActionListener(e -> copyCurrentOrder());

        pnlLeft.add(btnShortcuts);
        pnlLeft.add(new JSeparator(SwingConstants.VERTICAL));
        pnlLeft.add(btnPrev);
        pnlLeft.add(btnNext);
        pnlLeft.add(new JSeparator(SwingConstants.VERTICAL));
        pnlLeft.add(btnNew);
        pnlLeft.add(btnCopy);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        pnlRight.setOpaque(false);
        JButton btnDelete = new JButton("Xóa", IconHelper.getIcon("cross.png", 14, 14));
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setForeground(new Color(180, 0, 0));
        btnDelete.setFocusPainted(false);
        btnDelete.setBackground(Color.WHITE);
        btnDelete.setBorder(new CompoundBorder(new LineBorder(new Color(220, 180, 180), 1), new EmptyBorder(3, 8, 3, 8)));
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteCurrentOrder());
        pnlRight.add(btnDelete);

        pnl.add(pnlLeft, BorderLayout.CENTER);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    private JPanel createFormAndSummaryPanel() {
        JPanel pnl = new JPanel(new BorderLayout(8, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(6, 8, 4, 8));

        pnl.add(createInputFormFields(), BorderLayout.CENTER);
        pnl.add(createSummaryBox(), BorderLayout.EAST);

        return pnl;
    }

    private JPanel createInputFormFields() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Ngày, Số phiếu
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnl.add(new JLabel("Ngày"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        dpkNgay = new DatePickerField();
        dpkNgay.setDate(new Date());
        pnl.add(dpkNgay, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        pnl.add(new JLabel("Số phiếu"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.45;
        txtSoPhieu = new JTextField();
        txtSoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(txtSoPhieu, gbc);

        // Row 2: Kho xuất, Nhân viên xuất
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnl.add(new JLabel("Kho xuất"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        cbKhoXuat = new JComboBox<>();
        cbKhoXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(cbKhoXuat, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        pnl.add(new JLabel("Nhân viên xuất"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.45;
        cbNhanVienXuat = new JComboBox<>();
        cbNhanVienXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(cbNhanVienXuat, gbc);

        // Row 3: Kho nhập, Nhân viên nhập
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnl.add(new JLabel("Kho nhập"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        cbKhoNhap = new JComboBox<>();
        cbKhoNhap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(cbKhoNhap, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        pnl.add(new JLabel("Nhân viên nhập"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.45;
        cbNhanVienNhap = new JComboBox<>();
        cbNhanVienNhap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(cbNhanVienNhap, gbc);

        // Row 4: Diễn giải
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnl.add(new JLabel("Diễn giải"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtDienGiai = new JTextField("Chuyển kho nội bộ");
        txtDienGiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(txtDienGiai, gbc);
        gbc.gridwidth = 1;

        // Row 5: Ghi chú
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        pnl.add(new JLabel("Ghi chú"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(txtGhiChu, gbc);
        gbc.gridwidth = 1;

        // Row 6: Quick Add F3
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        pnl.add(new JLabel("Tìm (F3):"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        JPanel pnlQuick = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        pnlQuick.setOpaque(false);

        txtTimF3 = new JTextField(12);
        txtTimF3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimF3.addActionListener(e -> addSelectedCatalogItem());
        txtTimF3.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterCatalogFromQuick(); }
            @Override public void removeUpdate(DocumentEvent e) { filterCatalogFromQuick(); }
            @Override public void changedUpdate(DocumentEvent e) { filterCatalogFromQuick(); }
        });

        spnSoLuong = new JSpinner(new SpinnerNumberModel(1.0, 0.001, 999999.0, 1.0));
        spnSoLuong.setPreferredSize(new Dimension(50, 24));

        btnThemQuick = new JButton("Thêm", IconHelper.getIcon("plus.png", 14, 14));
        btnThemQuick.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThemQuick.addActionListener(e -> addSelectedCatalogItem());

        pnlQuick.add(txtTimF3);
        pnlQuick.add(new JLabel("Số lượng:"));
        pnlQuick.add(spnSoLuong);
        pnlQuick.add(btnThemQuick);

        pnl.add(pnlQuick, gbc);
        gbc.gridwidth = 1;

        return pnl;
    }

    private JPanel createSummaryBox() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(new Color(245, 248, 253));
        pnl.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(new LineBorder(new Color(185, 205, 230), 1), "Tổng cộng", 0, 0, new Font("Segoe UI", Font.BOLD, 12)),
                new EmptyBorder(2, 6, 4, 6)
        ));
        pnl.setPreferredSize(new Dimension(280, 175));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tiền hàng
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        pnl.add(new JLabel("Tiền hàng"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        lblTienHangVal = new JLabel("0", SwingConstants.RIGHT);
        lblTienHangVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTienHangVal.setOpaque(true);
        lblTienHangVal.setBackground(new Color(254, 253, 210)); // Nền vàng kem
        lblTienHangVal.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 170)));
        pnl.add(lblTienHangVal, gbc);

        // Tỉ lệ giảm giá
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        pnl.add(new JLabel("Tỉ lệ giảm giá"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        JPanel pnlGiam = new JPanel(new BorderLayout(4, 0));
        pnlGiam.setOpaque(false);
        txtTiLeGiamGia = new JTextField("0", 3);
        txtTiLeGiamGia.setHorizontalAlignment(JTextField.RIGHT);
        txtTiLeGiamGia.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { onDiscountPercentChanged(); }
        });
        txtTienGiamGia = new JTextField("0", 7);
        txtTienGiamGia.setHorizontalAlignment(JTextField.RIGHT);
        txtTienGiamGia.setBackground(new Color(254, 253, 210));
        txtTienGiamGia.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { onDiscountAmountChanged(); }
        });
        pnlGiam.add(txtTiLeGiamGia, BorderLayout.WEST);
        pnlGiam.add(txtTienGiamGia, BorderLayout.CENTER);
        pnl.add(pnlGiam, gbc);

        // Tỉ lệ thuế
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.4;
        pnl.add(new JLabel("Tỉ lệ thuế"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        JPanel pnlThue = new JPanel(new BorderLayout(4, 0));
        pnlThue.setOpaque(false);
        txtTiLeThue = new JTextField("0", 3);
        txtTiLeThue.setHorizontalAlignment(JTextField.RIGHT);
        txtTiLeThue.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { onTaxPercentChanged(); }
        });
        txtTienThue = new JTextField("0", 7);
        txtTienThue.setHorizontalAlignment(JTextField.RIGHT);
        txtTienThue.setBackground(new Color(254, 253, 210));
        txtTienThue.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { onTaxAmountChanged(); }
        });
        pnlThue.add(txtTiLeThue, BorderLayout.WEST);
        pnlThue.add(txtTienThue, BorderLayout.CENTER);
        pnl.add(pnlThue, gbc);

        // Phí vận chuyển
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.4;
        pnl.add(new JLabel("Phí vận chuyển"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        txtPhiVanChuyen = new JTextField("0");
        txtPhiVanChuyen.setHorizontalAlignment(JTextField.RIGHT);
        txtPhiVanChuyen.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { recalculateTotals(); }
        });
        pnl.add(txtPhiVanChuyen, gbc);

        // Tổng cộng
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.4;
        JLabel lblTc = new JLabel("Tổng cộng");
        lblTc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblTc, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        lblTongCongVal = new JLabel("0", SwingConstants.RIGHT);
        lblTongCongVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongCongVal.setForeground(new Color(180, 0, 0));
        lblTongCongVal.setOpaque(true);
        lblTongCongVal.setBackground(new Color(254, 253, 210));
        lblTongCongVal.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 170)));
        pnl.add(lblTongCongVal, gbc);

        return pnl;
    }

    private JSplitPane createCenterTablesSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(380);
        split.setResizeWeight(0.32);
        split.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Left Panel: Catalog Table
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.setBackground(Color.WHITE);
        pnlLeft.setBorder(new LineBorder(new Color(190, 210, 235), 1));

        // Columns matching Image 2: STT, Mã hàng, Tên hàng, ĐVT, Giá bán, Bảo hành
        String[] catCols = {"STT", "Mã hàng", "Tên hàng", "ĐVT", "Giá bán", "Bảo hành"};
        catalogModel = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCatalog = new JTable(catalogModel);
        styleTable(tableCatalog);
        tableCatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCatalog.getColumnModel().getColumn(0).setPreferredWidth(55);
        tableCatalog.getColumnModel().getColumn(1).setPreferredWidth(75);
        tableCatalog.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableCatalog.getColumnModel().getColumn(3).setPreferredWidth(50);
        tableCatalog.getColumnModel().getColumn(4).setPreferredWidth(75);
        tableCatalog.getColumnModel().getColumn(5).setPreferredWidth(60);

        tableCatalog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedCatalogItem();
                }
            }
        });

        pnlLeft.add(new JScrollPane(tableCatalog), BorderLayout.CENTER);
        split.setLeftComponent(pnlLeft);

        // Right Panel: Line Items Table
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(new LineBorder(new Color(190, 210, 235), 1));

        // Toolbar: Xóa dòng (F8), Nhập từ excel, Xuất excel, Lọc
        JPanel pnlDetailBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnlDetailBar.setBackground(new Color(240, 245, 252));

        JButton btnDelRow = createToolButton("Xóa dòng (F8)", "cross.png");
        btnDelRow.addActionListener(e -> deleteSelectedLineItem());

        JButton btnImportExcel = createToolButton("Nhập từ excel", "page_excel.png");
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng nhập từ Excel đang được chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        JButton btnExportExcel = createToolButton("Xuất excel", "export-excel.png");
        btnExportExcel.addActionListener(e -> ExcelExporter.exportTable(tableChiTiet, "Chi_Tiet_Chuyen_Kho", "Chuyển kho", this));

        pnlDetailBar.add(btnDelRow);
        pnlDetailBar.add(btnImportExcel);
        pnlDetailBar.add(btnExportExcel);
        pnlRight.add(pnlDetailBar, BorderLayout.NORTH);

        // Columns matching Image 2: [STT], Ghi chú, Mã hàng, Tên mặt hàng, ĐVT, Số lượng, Đơn giá, Giảm giá %, Thành tiền, Bảo hành, Hạn sử dụng
        String[] detailCols = {
                "", "Ghi chú", "Mã hàng", "Tên mặt hàng", "ĐVT",
                "Số lượng", "Đơn giá", "Giảm giá %", "Thành tiền", "Bảo hành", "Hạn sử dụng"
        };
        chiTietModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Cho phép sửa Ghi chú (1), Số lượng (5), Đơn giá (6), Giảm giá % (7)
                return c == 1 || c == 5 || c == 6 || c == 7;
            }
        };
        tableChiTiet = new JTable(chiTietModel);
        styleTable(tableChiTiet);
        tableChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {40, 80, 75, 140, 50, 65, 80, 70, 90, 65, 80};
        for (int i = 0; i < widths.length && i < tableChiTiet.getColumnCount(); i++) {
            tableChiTiet.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        chiTietModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row >= 0 && (col == 5 || col == 6 || col == 7)) {
                updateLineItemFromTable(row);
            }
        });

        pnlRight.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);
        split.setRightComponent(pnlRight);

        return split;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 238, 248));
        table.setSelectionBackground(new Color(200, 230, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(232, 242, 254));
        header.setForeground(new Color(40, 50, 70));
        header.setPreferredSize(new Dimension(100, 24));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    }

    private JPanel createBottomButtonBar() {
        JPanel pnl = new JPanel(new BorderLayout(8, 0));
        pnl.setBackground(new Color(225, 237, 250));
        pnl.setBorder(new EmptyBorder(6, 12, 6, 12));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlLeft.setOpaque(false);
        JButton btnLuuIn = createPrimaryButton("Lưu & In", "printer.png");
        btnLuuIn.addActionListener(e -> saveAndPrint(true));

        JButton btnLuuXemIn = createPrimaryButton("Lưu & Xem in", "page_white_magnify.png");
        btnLuuXemIn.addActionListener(e -> saveAndPrint(false));

        pnlLeft.add(btnLuuIn);
        pnlLeft.add(btnLuuXemIn);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlRight.setOpaque(false);

        JButton btnLuu = createPrimaryButton("Lưu", "disk.png");
        btnLuu.addActionListener(e -> saveOrder(false, false));

        JButton btnLuuMoi = createPrimaryButton("Lưu & Mới", "add.png");
        btnLuuMoi.addActionListener(e -> saveOrder(true, false));

        JButton btnLuuThoat = createPrimaryButton("Lưu & thoát", "door_in.png");
        btnLuuThoat.addActionListener(e -> saveOrder(false, true));

        JButton btnThoat = createSecondaryButton("Thoát", "cancel.png");
        btnThoat.addActionListener(e -> dispose());

        pnlRight.add(btnLuu);
        pnlRight.add(btnLuuMoi);
        pnlRight.add(btnLuuThoat);
        pnlRight.add(btnThoat);

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    private JButton createToolButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPrimaryButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(245, 250, 255));
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(150, 185, 225), 1), new EmptyBorder(4, 10, 4, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSecondaryButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(4, 8, 4, 8)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDropdownData() {
        // Kho xuất
        cbKhoXuat.removeAllItems();
        cbKhoXuat.addItem(new KhoHang("", "-- Chọn kho xuất --"));
        List<KhoHang> khoList = khoHangDAO.getAll();
        for (KhoHang k : khoList) {
            cbKhoXuat.addItem(k);
        }

        // Kho nhập
        cbKhoNhap.removeAllItems();
        cbKhoNhap.addItem(new KhoHang("", "-- Chọn kho nhập --"));
        for (KhoHang k : khoList) {
            cbKhoNhap.addItem(k);
        }

        // Nhân viên xuất
        cbNhanVienXuat.removeAllItems();
        cbNhanVienXuat.addItem(new NhanVien("", "-- Chọn nhân viên --"));
        List<NhanVien> nvList = nhanVienDAO.getAll();
        for (NhanVien nv : nvList) {
            cbNhanVienXuat.addItem(nv);
        }

        // Nhân viên nhập
        cbNhanVienNhap.removeAllItems();
        cbNhanVienNhap.addItem(new NhanVien("", "-- Chọn nhân viên --"));
        for (NhanVien nv : nvList) {
            cbNhanVienNhap.addItem(nv);
        }
    }

    private void loadCatalogData() {
        allCatalogItems = matHangDAO.getAll();
        if (allCatalogItems == null) allCatalogItems = new ArrayList<>();
        filterCatalog();
    }

    private void filterCatalog() {
        filteredCatalogItems.clear();
        filteredCatalogItems.addAll(allCatalogItems);
        populateCatalogTable();
    }

    private void filterCatalogFromQuick() {
        String kw = txtTimF3.getText().trim().toLowerCase();
        if (kw.isEmpty()) return;
        for (int i = 0; i < filteredCatalogItems.size(); i++) {
            MatHang m = filteredCatalogItems.get(i);
            if ((m.getCode() != null && m.getCode().toLowerCase().startsWith(kw)) ||
                (m.getName() != null && m.getName().toLowerCase().contains(kw))) {
                tableCatalog.setRowSelectionInterval(i, i);
                tableCatalog.scrollRectToVisible(tableCatalog.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private void populateCatalogTable() {
        catalogModel.setRowCount(0);
        for (int i = 0; i < filteredCatalogItems.size(); i++) {
            MatHang m = filteredCatalogItems.get(i);
            catalogModel.addRow(new Object[]{
                    dfStt.format(i + 1),
                    m.getCode() != null ? m.getCode() : "",
                    m.getName() != null ? m.getName() : "",
                    m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "",
                    CurrencyUtil.format(m.getGiaBan()),
                    m.getBaoHanh() != null ? m.getBaoHanh() : ""
            });
        }
    }

    private void addSelectedCatalogItem() {
        int row = tableCatalog.getSelectedRow();
        if (row < 0 && !filteredCatalogItems.isEmpty()) {
            row = 0;
            tableCatalog.setRowSelectionInterval(0, 0);
        }
        if (row < 0 || row >= filteredCatalogItems.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mặt hàng từ bảng danh mục bên trái!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MatHang m = filteredCatalogItems.get(row);
        double qty = ((Number) spnSoLuong.getValue()).doubleValue();

        boolean found = false;
        for (DonHangChiTiet existing : lineItems) {
            if (existing.getMatHangId() != null && existing.getMatHangId().equals(m.getId())) {
                existing.setSoLuong(existing.getSoLuong() + qty);
                existing.tinhThanhTien();
                found = true;
                break;
            }
        }

        if (!found) {
            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setMatHangId(m.getId());
            ct.setMaHang(m.getCode());
            ct.setTenHang(m.getName());
            ct.setDonViTinhId(m.getDonViTinhId());
            ct.setTenDonViTinh(m.getTenDonViTinh());
            ct.setSoLuong(qty);
            ct.setDonGia(m.getGiaNhap() > 0 ? m.getGiaNhap() : 0.0);
            ct.setTiLeGiamGia(0.0);
            ct.setBaoHanh(m.getBaoHanh());
            ct.tinhThanhTien();
            lineItems.add(ct);
        }

        refreshDetailTable();
        recalculateTotals();

        int lastIdx = lineItems.size() - 1;
        if (lastIdx >= 0) {
            tableChiTiet.setRowSelectionInterval(lastIdx, lastIdx);
            tableChiTiet.scrollRectToVisible(tableChiTiet.getCellRect(lastIdx, 0, true));
        }

        txtTimF3.setText("");
        txtTimF3.requestFocus();
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
            Object valNote = chiTietModel.getValueAt(row, 1);
            ct.setGhiChu(valNote != null ? valNote.toString() : "");

            Object valQty = chiTietModel.getValueAt(row, 5);
            double qty = parseDouble(valQty, ct.getSoLuong());
            ct.setSoLuong(qty);

            Object valPrice = chiTietModel.getValueAt(row, 6);
            double price = parseDouble(valPrice, ct.getDonGia());
            ct.setDonGia(price);

            Object valDisc = chiTietModel.getValueAt(row, 7);
            double disc = parseDouble(valDisc, ct.getTiLeGiamGia());
            ct.setTiLeGiamGia(disc);

            ct.tinhThanhTien();

            chiTietModel.setValueAt(CurrencyUtil.format(ct.getThanhTien()), row, 8);
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
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    df.format(ct.getSoLuong()),
                    ct.getDonGia() > 0 ? CurrencyUtil.format(ct.getDonGia()) : "",
                    ct.getTiLeGiamGia() > 0 ? df.format(ct.getTiLeGiamGia()) : "",
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getBaoHanh() != null ? ct.getBaoHanh() : "",
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

            double subtotal = totalTienHang - tienGiam;
            double tiLeThue = parseDouble(txtTiLeThue.getText(), 0.0);
            double tienThue = subtotal * (tiLeThue / 100.0);

            double phiVc = parseDouble(txtPhiVanChuyen.getText(), 0.0);
            double tongCong = subtotal + tienThue + phiVc;

            lblTienHangVal.setText(CurrencyUtil.format(totalTienHang));
            txtTienGiamGia.setText(CurrencyUtil.format(tienGiam));
            txtTienThue.setText(CurrencyUtil.format(tienThue));
            lblTongCongVal.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingTotals = false;
        }
    }

    private void onDiscountPercentChanged() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = parseDouble(lblTienHangVal.getText(), 0.0);
            double pct = parseDouble(txtTiLeGiamGia.getText(), 0.0);
            double tienGiam = totalTienHang * (pct / 100.0);
            txtTienGiamGia.setText(CurrencyUtil.format(tienGiam));

            double subtotal = totalTienHang - tienGiam;
            double tiLeThue = parseDouble(txtTiLeThue.getText(), 0.0);
            double tienThue = subtotal * (tiLeThue / 100.0);
            txtTienThue.setText(CurrencyUtil.format(tienThue));

            double phiVc = parseDouble(txtPhiVanChuyen.getText(), 0.0);
            double tongCong = subtotal + tienThue + phiVc;
            lblTongCongVal.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingTotals = false;
        }
    }

    private void onDiscountAmountChanged() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = parseDouble(lblTienHangVal.getText(), 0.0);
            double tienGiam = parseDouble(txtTienGiamGia.getText(), 0.0);
            double pct = totalTienHang > 0 ? (tienGiam / totalTienHang) * 100.0 : 0.0;
            txtTiLeGiamGia.setText(df.format(pct));

            double subtotal = totalTienHang - tienGiam;
            double tiLeThue = parseDouble(txtTiLeThue.getText(), 0.0);
            double tienThue = subtotal * (tiLeThue / 100.0);
            txtTienThue.setText(CurrencyUtil.format(tienThue));

            double phiVc = parseDouble(txtPhiVanChuyen.getText(), 0.0);
            double tongCong = subtotal + tienThue + phiVc;
            lblTongCongVal.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingTotals = false;
        }
    }

    private void onTaxPercentChanged() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = parseDouble(lblTienHangVal.getText(), 0.0);
            double pct = parseDouble(txtTiLeThue.getText(), 0.0);
            double tienGiam = parseDouble(txtTienGiamGia.getText(), 0.0);
            double subtotal = totalTienHang - tienGiam;
            double tienThue = subtotal * (pct / 100.0);
            txtTienThue.setText(CurrencyUtil.format(tienThue));

            double phiVc = parseDouble(txtPhiVanChuyen.getText(), 0.0);
            double tongCong = subtotal + tienThue + phiVc;
            lblTongCongVal.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingTotals = false;
        }
    }

    private void onTaxAmountChanged() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = parseDouble(lblTienHangVal.getText(), 0.0);
            double tienGiam = parseDouble(txtTienGiamGia.getText(), 0.0);
            double subtotal = totalTienHang - tienGiam;
            double tienThue = parseDouble(txtTienThue.getText(), 0.0);
            double pct = subtotal > 0 ? (tienThue / subtotal) * 100.0 : 0.0;
            txtTiLeThue.setText(df.format(pct));

            double phiVc = parseDouble(txtPhiVanChuyen.getText(), 0.0);
            double tongCong = subtotal + tienThue + phiVc;
            lblTongCongVal.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingTotals = false;
        }
    }

    public void resetForNew() {
        currentDonHang = null;
        setTitle("PHIẾU CHUYỂN KHO - THÊM MỚI");
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(donHangDAO.generateNextSoPhieuChuyenKho(new Date()));
        txtDienGiai.setText("Chuyển kho nội bộ");
        txtGhiChu.setText("");

        if (cbKhoXuat.getItemCount() > 1) cbKhoXuat.setSelectedIndex(1);
        if (cbKhoNhap.getItemCount() > 2) cbKhoNhap.setSelectedIndex(2);
        else if (cbKhoNhap.getItemCount() > 1) cbKhoNhap.setSelectedIndex(1);

        if (cbNhanVienXuat.getItemCount() > 0) cbNhanVienXuat.setSelectedIndex(0);
        if (cbNhanVienNhap.getItemCount() > 0) cbNhanVienNhap.setSelectedIndex(0);

        lineItems.clear();
        refreshDetailTable();

        txtTiLeGiamGia.setText("0");
        txtTienGiamGia.setText("0");
        txtTiLeThue.setText("0");
        txtTienThue.setText("0");
        txtPhiVanChuyen.setText("0");
        recalculateTotals();
    }

    private void populateExistingData() {
        if (currentDonHang == null) return;
        setTitle("PHIẾU CHUYỂN KHO - SỬA [" + currentDonHang.getName() + "]");
        dpkNgay.setDate(currentDonHang.getNgay());
        txtSoPhieu.setText(currentDonHang.getName());
        txtDienGiai.setText(currentDonHang.getDienGiai() != null ? currentDonHang.getDienGiai() : "Chuyển kho nội bộ");
        txtGhiChu.setText(currentDonHang.getGhiChu() != null ? currentDonHang.getGhiChu() : "");

        // Select Kho xuất
        if (currentDonHang.getKhoXuatId() != null) {
            for (int i = 0; i < cbKhoXuat.getItemCount(); i++) {
                KhoHang k = cbKhoXuat.getItemAt(i);
                if (currentDonHang.getKhoXuatId().equals(k.getId())) {
                    cbKhoXuat.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select Kho nhập
        if (currentDonHang.getKhoNhapId() != null) {
            for (int i = 0; i < cbKhoNhap.getItemCount(); i++) {
                KhoHang k = cbKhoNhap.getItemAt(i);
                if (currentDonHang.getKhoNhapId().equals(k.getId())) {
                    cbKhoNhap.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select Nhân viên xuất
        if (currentDonHang.getNhanVienXuatId() != null) {
            for (int i = 0; i < cbNhanVienXuat.getItemCount(); i++) {
                NhanVien nv = cbNhanVienXuat.getItemAt(i);
                if (currentDonHang.getNhanVienXuatId().equals(nv.getId())) {
                    cbNhanVienXuat.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select Nhân viên nhập
        if (currentDonHang.getNhanVienNhapId() != null) {
            for (int i = 0; i < cbNhanVienNhap.getItemCount(); i++) {
                NhanVien nv = cbNhanVienNhap.getItemAt(i);
                if (currentDonHang.getNhanVienNhapId().equals(nv.getId())) {
                    cbNhanVienNhap.setSelectedIndex(i);
                    break;
                }
            }
        }

        lineItems.clear();
        List<DonHangChiTiet> details = currentDonHang.getChiTietLayHang();
        if (details == null || details.isEmpty()) {
            details = donHangDAO.getChiTietPhieuNhap(currentDonHang.getId());
        }
        lineItems.addAll(details);
        refreshDetailTable();

        txtTiLeGiamGia.setText(df.format(currentDonHang.getTiLeGiamGia()));
        txtTienGiamGia.setText(CurrencyUtil.format(currentDonHang.getTienGiamGia()));
        txtTiLeThue.setText(df.format(currentDonHang.getTiLeThue()));
        txtTienThue.setText(CurrencyUtil.format(currentDonHang.getTienThue()));
        txtPhiVanChuyen.setText(CurrencyUtil.format(currentDonHang.getPhiVanChuyen()));
        recalculateTotals();
    }

    private boolean saveOrder(boolean createNewAfter, boolean closeAfter) {
        // Validation Constraint 1: Kho xuất và Kho nhập bắt buộc chọn
        KhoHang khoXuat = (KhoHang) cbKhoXuat.getSelectedItem();
        if (khoXuat == null || khoXuat.getId() == null || khoXuat.getId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kho xuất bắt buộc chọn! Vui lòng chọn kho xuất.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cbKhoXuat.requestFocus();
            return false;
        }

        KhoHang khoNhap = (KhoHang) cbKhoNhap.getSelectedItem();
        if (khoNhap == null || khoNhap.getId() == null || khoNhap.getId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kho nhập bắt buộc chọn! Vui lòng chọn kho nhập.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cbKhoNhap.requestFocus();
            return false;
        }

        // Validation Constraint 2: Kho xuất và Kho nhập không trùng nhau
        if (khoXuat.getId().trim().equalsIgnoreCase(khoNhap.getId().trim())) {
            JOptionPane.showMessageDialog(this, "Kho xuất và kho nhập không được trùng nhau! Vui lòng chọn kho khác.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cbKhoNhap.requestFocus();
            return false;
        }

        if (lineItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu chuyển kho phải có ít nhất một mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTimF3.requestFocus();
            return false;
        }

        boolean isNew = (currentDonHang == null || currentDonHang.getId() == null);
        DonHang dh = isNew ? new DonHang() : currentDonHang;

        dh.setName(txtSoPhieu.getText().trim());
        dh.setNgay(dpkNgay.getDate() != null ? dpkNgay.getDate() : new Date());
        dh.setDienGiai(txtDienGiai.getText().trim());
        dh.setGhiChu(txtGhiChu.getText().trim());

        dh.setKhoXuatId(khoXuat.getId());
        dh.setTenKhoXuat(khoXuat.getName());

        dh.setKhoNhapId(khoNhap.getId());
        dh.setTenKhoNhap(khoNhap.getName());

        NhanVien nvx = (NhanVien) cbNhanVienXuat.getSelectedItem();
        if (nvx != null && nvx.getId() != null && !nvx.getId().isEmpty()) {
            dh.setNhanVienXuatId(nvx.getId());
            dh.setTenNhanVienXuat(nvx.getName());
        } else {
            dh.setNhanVienXuatId(null);
            dh.setTenNhanVienXuat(null);
        }

        NhanVien nvn = (NhanVien) cbNhanVienNhap.getSelectedItem();
        if (nvn != null && nvn.getId() != null && !nvn.getId().isEmpty()) {
            dh.setNhanVienNhapId(nvn.getId());
            dh.setTenNhanVienNhap(nvn.getName());
        } else {
            dh.setNhanVienNhapId(null);
            dh.setTenNhanVienNhap(null);
        }

        double totalTienHang = 0.0;
        for (DonHangChiTiet ct : lineItems) totalTienHang += ct.getThanhTien();

        double tiLeGiam = parseDouble(txtTiLeGiamGia.getText(), 0.0);
        double tienGiam = parseDouble(txtTienGiamGia.getText(), 0.0);
        double tiLeThue = parseDouble(txtTiLeThue.getText(), 0.0);
        double tienThue = parseDouble(txtTienThue.getText(), 0.0);
        double phiVc = parseDouble(txtPhiVanChuyen.getText(), 0.0);
        double tongCong = (totalTienHang - tienGiam) + tienThue + phiVc;

        dh.setTienHang(totalTienHang);
        dh.setTiLeGiamGia(tiLeGiam);
        dh.setTienGiamGia(tienGiam);
        dh.setTiLeThue(tiLeThue);
        dh.setTienThue(tienThue);
        dh.setPhiVanChuyen(phiVc);
        dh.setTongCong(tongCong);
        dh.setConLai(tongCong);
        dh.setChiTietLayHang(new ArrayList<>(lineItems));

        boolean success;
        if (isNew) {
            success = donHangDAO.insertPhieuChuyenKho(dh);
        } else {
            success = donHangDAO.updatePhieuChuyenKho(dh);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu phiếu chuyển kho thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();

            if (createNewAfter) {
                resetForNew();
            } else if (closeAfter) {
                dispose();
            } else {
                currentDonHang = dh;
                setTitle("PHIẾU CHUYỂN KHO - SỬA [" + dh.getName() + "]");
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu phiếu chuyển kho. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveAndPrint(boolean directPrint) {
        if (saveOrder(false, false)) {
            String title = directPrint ? "In phiếu chuyển kho" : "Xem trước phiếu chuyển kho";
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================\n");
            sb.append("                 PHIẾU CHUYỂN KHO                       \n");
            sb.append("========================================================\n");
            sb.append("Số phiếu: ").append(txtSoPhieu.getText()).append("    Ngày: ").append(dpkNgay.getText()).append("\n");
            sb.append("Kho xuất: ").append(cbKhoXuat.getSelectedItem() != null ? cbKhoXuat.getSelectedItem() : "").append("\n");
            sb.append("Kho nhập: ").append(cbKhoNhap.getSelectedItem() != null ? cbKhoNhap.getSelectedItem() : "").append("\n");
            sb.append("Diễn giải: ").append(txtDienGiai.getText()).append("\n");
            sb.append("Ghi chú:   ").append(txtGhiChu.getText()).append("\n");
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("%-5s %-20s %-8s %-12s %-12s\n", "STT", "Mặt hàng", "SL", "Đơn giá", "Thành tiền"));
            for (int i = 0; i < lineItems.size(); i++) {
                DonHangChiTiet ct = lineItems.get(i);
                sb.append(String.format("%-5d %-20s %-8.1f %-12s %-12s\n",
                        (i + 1),
                        ct.getTenHang() != null ? (ct.getTenHang().length() > 20 ? ct.getTenHang().substring(0, 18) + ".." : ct.getTenHang()) : "",
                        ct.getSoLuong(),
                        CurrencyUtil.format(ct.getDonGia()),
                        CurrencyUtil.format(ct.getThanhTien())
                ));
            }
            sb.append("--------------------------------------------------------\n");
            sb.append("Tổng cộng: ").append(lblTongCongVal.getText()).append("\n");
            sb.append("========================================================\n");

            JTextArea ta = new JTextArea(sb.toString(), 20, 48);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void copyCurrentOrder() {
        if (currentDonHang == null) return;
        currentDonHang = null;
        setTitle("PHIẾU CHUYỂN KHO - THÊM MỚI (BẢN SAO)");
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(donHangDAO.generateNextSoPhieuChuyenKho(new Date()));
        JOptionPane.showMessageDialog(this, "Đã sao chép phiếu chuyển kho sang số phiếu mới. Vui lòng kiểm tra và lưu lại!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteCurrentOrder() {
        if (currentDonHang == null || currentDonHang.getId() == null) return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu chuyển kho [" + currentDonHang.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (donHangDAO.delete(currentDonHang.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu chuyển kho thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                if (onDataSaved != null) onDataSaved.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa phiếu chuyển kho này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void navigateRecord(int offset) {
        if (currentDonHang == null || currentDonHang.getId() == null) return;
        DonHang target = donHangDAO.getPhieuChuyenKhoOffset(currentDonHang.getId(), offset);
        if (target != null) {
            currentDonHang = target;
            populateExistingData();
        } else {
            JOptionPane.showMessageDialog(this, offset < 0 ? "Đã ở phiếu chuyển kho đầu tiên!" : "Đã ở phiếu chuyển kho cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showShortcutHelp() {
        String msg = "DANH SÁCH PHÍM TẮT:\n\n" +
                "• Ctrl + S: Lưu & Tạo mới\n" +
                "• Ctrl + L: Lưu phiếu\n" +
                "• Ctrl + N: Tạo mới\n" +
                "• Ctrl + P: Lưu & In phiếu\n" +
                "• F3: Tìm kiếm mặt hàng\n" +
                "• F8: Xóa dòng đang chọn\n" +
                "• F10: Xem phiếu trước\n" +
                "• F11: Xem phiếu sau\n" +
                "• ESC: Đóng màn hình";
        JOptionPane.showMessageDialog(this, msg, "Hướng dẫn phím tắt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupKeyBindings() {
        JRootPane rp = getRootPane();
        rp.registerKeyboardAction(e -> saveOrder(true, false), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> saveOrder(false, false), KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> resetForNew(), KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> saveAndPrint(true), KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> txtTimF3.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> deleteSelectedLineItem(), KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> navigateRecord(-1), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> navigateRecord(1), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
