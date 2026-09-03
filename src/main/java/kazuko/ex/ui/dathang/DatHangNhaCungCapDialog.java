package kazuko.ex.ui.dathang;

import kazuko.ex.dao.DatHangNCCDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.dathang.DatHangNCC;
import kazuko.ex.model.dathang.DatHangNCCChiTiet;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.khohang.PhieuNhapKhoDialog;
import kazuko.ex.ui.quy.TaoPhieuChiDialog;
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

public class DatHangNhaCungCapDialog extends JDialog {

    private final DatHangNCCDAO datHangNCCDAO = new DatHangNCCDAO();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private DatHangNCC currentOrder;
    private final Runnable onDataSaved;

    // Form fields
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private JComboBox<NhaCungCap> cbNhaCungCap;
    private JTextField txtGhiChu;

    // Special action buttons
    private JButton btnDatTienTruoc;
    private JButton btnTaoPhieuNhap;
    private JButton btnLayHangDuoiMucTon;

    // Quick add controls
    private JTextField txtTimF3;
    private JSpinner spnSoLuong;
    private JButton btnThemQuick;

    // Financial Summary fields (top right)
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

    // Order Details Table (Right)
    private JTable tableChiTiet;
    private DefaultTableModel chiTietModel;
    private final List<DatHangNCCChiTiet> lineItems = new ArrayList<>();

    private final DecimalFormat df = new DecimalFormat("#,##0.##");
    private final DecimalFormat dfStt = new DecimalFormat("0000");
    private boolean isUpdatingTotals = false;

    public DatHangNhaCungCapDialog(Window parent, Runnable onDataSaved) {
        this(parent, null, onDataSaved);
    }

    public DatHangNhaCungCapDialog(Window parent, DatHangNCC order, Runnable onDataSaved) {
        super(parent, order == null ? "ĐẶT HÀNG NHÀ CUNG CẤP - THÊM MỚI" : "ĐẶT HÀNG NHÀ CUNG CẤP - CHỈNH SỬA", ModalityType.APPLICATION_MODAL);
        this.currentOrder = order;
        this.onDataSaved = onDataSaved;

        initComponents();
        loadDropdownData();
        loadCatalogData();

        if (order != null) {
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

        JLabel lblTitle = new JLabel("Đặt hàng nhà cung cấp");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(20, 60, 120));
        lblTitle.setIcon(IconHelper.getIcon("report--pencil.png", 18, 18));

        JLabel lblHints = new JLabel("(Ctrl+S: Lưu&Mới, Ctrl+L: Lưu, Ctrl+N: Mới, Ctrl+P: In, ESC: Thoát)");
        lblHints.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHints.setForeground(new Color(90, 100, 115));

        pnl.add(lblTitle, BorderLayout.WEST);
        pnl.add(lblHints, BorderLayout.EAST);
        return pnl;
    }

    private JPanel createTopActionBar() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnl.setBackground(new Color(225, 237, 250));
        pnl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(195, 215, 238)));

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

        JButton btnDelete = createToolButton("Xóa", "cross.png");
        btnDelete.addActionListener(e -> deleteCurrentOrder());

        pnl.add(btnShortcuts);
        pnl.add(new JSeparator(SwingConstants.VERTICAL));
        pnl.add(btnPrev);
        pnl.add(btnNext);
        pnl.add(new JSeparator(SwingConstants.VERTICAL));
        pnl.add(btnNew);
        pnl.add(btnCopy);
        pnl.add(btnDelete);

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

        // Row 2: Nhà cung cấp + Nút Đặt tiền trước
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnl.add(new JLabel("Nhà cung cấp"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 0.8;
        JPanel pnlNccLine = new JPanel(new BorderLayout(6, 0));
        pnlNccLine.setOpaque(false);

        cbNhaCungCap = new JComboBox<>();
        cbNhaCungCap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlNccLine.add(cbNhaCungCap, BorderLayout.CENTER);

        btnDatTienTruoc = new JButton("Đặt tiền trước", IconHelper.getIcon("money_dollar.png", 14, 14));
        btnDatTienTruoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDatTienTruoc.setBackground(new Color(245, 250, 255));
        btnDatTienTruoc.setBorder(new CompoundBorder(new LineBorder(new Color(150, 185, 225), 1), new EmptyBorder(3, 8, 3, 8)));
        btnDatTienTruoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDatTienTruoc.addActionListener(e -> openDatTienTruocPhieuChi());
        pnlNccLine.add(btnDatTienTruoc, BorderLayout.EAST);

        pnl.add(pnlNccLine, gbc);
        gbc.gridwidth = 1;

        // Row 3: Ghi chú + Nút Tạo phiếu nhập
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnl.add(new JLabel("Ghi chú"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 0.8;
        JPanel pnlGhiChuLine = new JPanel(new BorderLayout(6, 0));
        pnlGhiChuLine.setOpaque(false);

        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlGhiChuLine.add(txtGhiChu, BorderLayout.CENTER);

        btnTaoPhieuNhap = new JButton("Tạo phiếu nhập", IconHelper.getIcon("navigation-000-frame.png", 14, 14));
        btnTaoPhieuNhap.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTaoPhieuNhap.setBackground(new Color(245, 250, 255));
        btnTaoPhieuNhap.setBorder(new CompoundBorder(new LineBorder(new Color(150, 185, 225), 1), new EmptyBorder(3, 8, 3, 8)));
        btnTaoPhieuNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTaoPhieuNhap.addActionListener(e -> openTaoPhieuNhapKho());
        pnlGhiChuLine.add(btnTaoPhieuNhap, BorderLayout.EAST);

        pnl.add(pnlGhiChuLine, gbc);
        gbc.gridwidth = 1;

        // Row 4: Nút "Lấy các mặt hàng dưới mức tồn tối thiểu để đặt hàng"
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1.0;
        btnLayHangDuoiMucTon = new JButton("Lấy các mặt hàng dưới mức tồn tối thiểu để đặt hàng", IconHelper.getIcon("package.png", 14, 14));
        btnLayHangDuoiMucTon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLayHangDuoiMucTon.setBackground(Color.WHITE);
        btnLayHangDuoiMucTon.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(4, 10, 4, 10)));
        btnLayHangDuoiMucTon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLayHangDuoiMucTon.addActionListener(e -> fetchLowStockItems());
        pnl.add(btnLayHangDuoiMucTon, gbc);
        gbc.gridwidth = 1;

        // Row 5: Quick Add F3
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
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

        // Giảm giá
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        pnl.add(new JLabel("Giảm giá"), gbc);

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
        txtTienGiamGia.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { onDiscountAmountChanged(); }
        });
        pnlGiam.add(txtTiLeGiamGia, BorderLayout.WEST);
        pnlGiam.add(txtTienGiamGia, BorderLayout.CENTER);
        pnl.add(pnlGiam, gbc);

        // Thuế
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.4;
        pnl.add(new JLabel("Thuế"), gbc);

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
        split.setDividerLocation(430);
        split.setResizeWeight(0.38);
        split.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Left Panel: Product Catalog
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.setBackground(Color.WHITE);
        pnlLeft.setBorder(new LineBorder(new Color(190, 210, 235), 1));

        // Catalog Columns matching Image 2: [STT], Tên hàng, ĐVT, Mã hàng, Giá nhập, Bảo hành, ĐVT chẵn
        String[] catCols = {"", "Tên hàng", "ĐVT", "Mã hàng", "Giá nhập", "Bảo hành", "ĐVT chẵn"};
        catalogModel = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCatalog = new JTable(catalogModel);
        styleTable(tableCatalog);
        tableCatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCatalog.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableCatalog.getColumnModel().getColumn(1).setPreferredWidth(135);
        tableCatalog.getColumnModel().getColumn(2).setPreferredWidth(50);
        tableCatalog.getColumnModel().getColumn(3).setPreferredWidth(75);
        tableCatalog.getColumnModel().getColumn(4).setPreferredWidth(75);
        tableCatalog.getColumnModel().getColumn(5).setPreferredWidth(60);
        tableCatalog.getColumnModel().getColumn(6).setPreferredWidth(65);

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

        // Right Panel: Order Details Table
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
        btnExportExcel.addActionListener(e -> ExcelExporter.exportTable(tableChiTiet, "Chi_Tiet_Dat_Hang", "Đặt hàng NCC", this));

        pnlDetailBar.add(btnDelRow);
        pnlDetailBar.add(btnImportExcel);
        pnlDetailBar.add(btnExportExcel);
        pnlRight.add(pnlDetailBar, BorderLayout.NORTH);

        // Line items columns matching Image 2: [STT], Mã hàng, Tên hàng, Ghi chú, ĐVT, Giảm giá %, Đơn giá, Thành tiền, Số lượng
        String[] detailCols = {
                "", "Mã hàng", "Tên hàng", "Ghi chú", "ĐVT",
                "Giảm giá %", "Đơn giá", "Thành tiền", "Số lượng"
        };
        chiTietModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Cho phép sửa Ghi chú (3), Giảm giá % (5), Đơn giá (6), Số lượng (8)
                return c == 3 || c == 5 || c == 6 || c == 8;
            }
        };
        tableChiTiet = new JTable(chiTietModel);
        styleTable(tableChiTiet);
        tableChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {40, 75, 140, 80, 50, 70, 80, 90, 65};
        for (int i = 0; i < widths.length && i < tableChiTiet.getColumnCount(); i++) {
            tableChiTiet.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        chiTietModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row >= 0 && (col == 5 || col == 6 || col == 8)) {
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
        cbNhaCungCap.removeAllItems();
        cbNhaCungCap.addItem(new NhaCungCap("", "", "-- Chọn nhà cung cấp --", "", "", ""));
        List<NhaCungCap> nccList = nhaCungCapDAO.getAll();
        for (NhaCungCap ncc : nccList) {
            cbNhaCungCap.addItem(ncc);
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
            if (m.getCode() != null && m.getCode().toLowerCase().startsWith(kw)) {
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
                    m.getName() != null ? m.getName() : "",
                    m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "",
                    m.getCode() != null ? m.getCode() : "",
                    CurrencyUtil.format(m.getGiaNhap()),
                    m.getBaoHanh() != null ? m.getBaoHanh() : "",
                    ""
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
        for (DatHangNCCChiTiet existing : lineItems) {
            if (existing.getMatHangId() != null && existing.getMatHangId().equals(m.getId())) {
                existing.setSoLuong(existing.getSoLuong() + qty);
                existing.tinhThanhTien();
                found = true;
                break;
            }
        }

        if (!found) {
            DatHangNCCChiTiet ct = new DatHangNCCChiTiet();
            ct.setMatHangId(m.getId());
            ct.setMaHang(m.getCode());
            ct.setTenHang(m.getName());
            ct.setDonViTinhId(m.getDonViTinhId());
            ct.setTenDonViTinh(m.getTenDonViTinh());
            ct.setSoLuong(qty);
            ct.setDonGia(m.getGiaNhap());
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
        DatHangNCCChiTiet ct = lineItems.get(row);

        try {
            Object valDisc = chiTietModel.getValueAt(row, 5);
            double disc = parseDouble(valDisc, ct.getTiLeGiamGia());
            ct.setTiLeGiamGia(disc);

            Object valPrice = chiTietModel.getValueAt(row, 6);
            double price = parseDouble(valPrice, ct.getDonGia());
            ct.setDonGia(price);

            Object valQty = chiTietModel.getValueAt(row, 8);
            double qty = parseDouble(valQty, ct.getSoLuong());
            ct.setSoLuong(qty);

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
            DatHangNCCChiTiet ct = lineItems.get(i);
            chiTietModel.addRow(new Object[]{
                    (i + 1),
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getNote() != null ? ct.getNote() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    df.format(ct.getTiLeGiamGia()),
                    CurrencyUtil.format(ct.getDonGia()),
                    CurrencyUtil.format(ct.getThanhTien()),
                    df.format(ct.getSoLuong())
            });
        }
    }

    private void recalculateTotals() {
        if (isUpdatingTotals) return;
        isUpdatingTotals = true;
        try {
            double totalTienHang = 0.0;
            for (DatHangNCCChiTiet ct : lineItems) {
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

    private void fetchLowStockItems() {
        int addedCount = 0;
        for (MatHang m : allCatalogItems) {
            // Giả lập hoặc lấy các mặt hàng có số lượng tồn <= tồn tối thiểu (hoặc 0)
            if (m.getTonKho() <= m.getTonToiThieu() || m.getTonKho() == 0) {
                boolean exists = false;
                for (DatHangNCCChiTiet ct : lineItems) {
                    if (ct.getMatHangId() != null && ct.getMatHangId().equals(m.getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    DatHangNCCChiTiet ct = new DatHangNCCChiTiet();
                    ct.setMatHangId(m.getId());
                    ct.setMaHang(m.getCode());
                    ct.setTenHang(m.getName());
                    ct.setDonViTinhId(m.getDonViTinhId());
                    ct.setTenDonViTinh(m.getTenDonViTinh());
                    ct.setSoLuong(m.getTonToiThieu() > 0 ? m.getTonToiThieu() : 1.0);
                    ct.setDonGia(m.getGiaNhap());
                    ct.setBaoHanh(m.getBaoHanh());
                    ct.tinhThanhTien();
                    lineItems.add(ct);
                    addedCount++;
                    if (addedCount >= 15) break; // Lấy tối đa 15 mặt hàng đề xuất
                }
            }
        }

        if (addedCount > 0) {
            refreshDetailTable();
            recalculateTotals();
            JOptionPane.showMessageDialog(this, "Đã thêm " + addedCount + " mặt hàng dưới mức tồn tối thiểu vào đơn đặt hàng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Hiện tại không có mặt hàng nào dưới mức tồn tối thiểu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openDatTienTruocPhieuChi() {
        double amount = parseDouble(lblTongCongVal.getText(), 0.0);
        NhaCungCap selNcc = (NhaCungCap) cbNhaCungCap.getSelectedItem();

        PhieuChi pc = new PhieuChi();
        pc.setNgay(dpkNgay.getDate() != null ? dpkNgay.getDate() : new Date());
        pc.setSoPhieu(new kazuko.ex.dao.PhieuChiDAO().generateNextSoPhieu(pc.getNgay()));
        pc.setSoTien(amount);
        pc.setLyDoChi("Đặt tiền mua trước số phiếu đặt hàng: " + txtSoPhieu.getText());
        pc.setLoaiDoiTuong("Đối tượng khác");
        pc.setTenCuaHang("TEST");

        if (selNcc != null && selNcc.getId() != null && !selNcc.getId().isEmpty()) {
            pc.setNhaCungCapId(selNcc.getId());
            pc.setTenNhaCungCap(selNcc.getName());
            pc.setTenDoiTuong(selNcc.getName());
            pc.setDiaChi(selNcc.getDiaChi());
        }

        TaoPhieuChiDialog dlg = new TaoPhieuChiDialog(this, pc, null);
        dlg.setVisible(true);
    }

    private void openTaoPhieuNhapKho() {
        NhaCungCap selNcc = (NhaCungCap) cbNhaCungCap.getSelectedItem();

        DonHang dhNhap = new DonHang();
        dhNhap.setNgay(dpkNgay.getDate() != null ? dpkNgay.getDate() : new Date());
        dhNhap.setDienGiai("Nhập mua hàng");
        dhNhap.setGhiChu(txtGhiChu.getText());

        if (selNcc != null && selNcc.getId() != null && !selNcc.getId().isEmpty()) {
            dhNhap.setNhaCungCapId(selNcc.getId());
            dhNhap.setTenNhaCungCap(selNcc.getName());
            dhNhap.setKhachHangId(selNcc.getId());
            dhNhap.setTenKhach(selNcc.getName());
        }

        List<DonHangChiTiet> nhapChiTiet = new ArrayList<>();
        for (DatHangNCCChiTiet ct : lineItems) {
            DonHangChiTiet item = new DonHangChiTiet();
            item.setMatHangId(ct.getMatHangId());
            item.setMaHang(ct.getMaHang());
            item.setTenHang(ct.getTenHang());
            item.setDonViTinhId(ct.getDonViTinhId());
            item.setTenDonViTinh(ct.getTenDonViTinh());
            item.setSoLuong(ct.getSoLuong());
            item.setDonGia(ct.getDonGia());
            item.setTiLeGiamGia(ct.getTiLeGiamGia());
            item.setThanhTien(ct.getThanhTien());
            item.setBaoHanh(ct.getBaoHanh());
            item.setGhiChu(ct.getNote());
            nhapChiTiet.add(item);
        }
        dhNhap.setChiTietLayHang(nhapChiTiet);

        dhNhap.setTienHang(parseDouble(lblTienHangVal.getText(), 0.0));
        dhNhap.setTiLeGiamGia(parseDouble(txtTiLeGiamGia.getText(), 0.0));
        dhNhap.setTienGiamGia(parseDouble(txtTienGiamGia.getText(), 0.0));
        dhNhap.setTiLeThue(parseDouble(txtTiLeThue.getText(), 0.0));
        dhNhap.setTienThue(parseDouble(txtTienThue.getText(), 0.0));
        dhNhap.setPhiVanChuyen(parseDouble(txtPhiVanChuyen.getText(), 0.0));
        dhNhap.setTongCong(parseDouble(lblTongCongVal.getText(), 0.0));
        dhNhap.setConLai(dhNhap.getTongCong());

        PhieuNhapKhoDialog dlg = new PhieuNhapKhoDialog(this, dhNhap, null);
        dlg.setVisible(true);
    }

    public void resetForNew() {
        currentOrder = null;
        setTitle("ĐẶT HÀNG NHÀ CUNG CẤP - THÊM MỚI");
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(datHangNCCDAO.generateNextSoPhieu(new Date()));
        txtGhiChu.setText("");
        if (cbNhaCungCap.getItemCount() > 0) cbNhaCungCap.setSelectedIndex(0);

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
        if (currentOrder == null) return;
        setTitle("ĐẶT HÀNG NHÀ CUNG CẤP - CHỈNH SỬA [" + currentOrder.getName() + "]");
        dpkNgay.setDate(currentOrder.getNgay());
        txtSoPhieu.setText(currentOrder.getName());
        txtGhiChu.setText(currentOrder.getNote() != null ? currentOrder.getNote() : "");

        if (currentOrder.getNhaCungCapId() != null) {
            for (int i = 0; i < cbNhaCungCap.getItemCount(); i++) {
                NhaCungCap ncc = cbNhaCungCap.getItemAt(i);
                if (currentOrder.getNhaCungCapId().equals(ncc.getId())) {
                    cbNhaCungCap.setSelectedIndex(i);
                    break;
                }
            }
        }

        lineItems.clear();
        List<DatHangNCCChiTiet> details = currentOrder.getChiTiet();
        if (details == null || details.isEmpty()) {
            details = datHangNCCDAO.getChiTiet(currentOrder.getId());
        }
        lineItems.addAll(details);
        refreshDetailTable();

        txtTiLeGiamGia.setText(df.format(currentOrder.getTiLeGiamGia()));
        txtTienGiamGia.setText(CurrencyUtil.format(currentOrder.getTienGiamGia()));
        txtTiLeThue.setText(df.format(currentOrder.getTiLeThue()));
        txtTienThue.setText(CurrencyUtil.format(currentOrder.getTienThue()));
        txtPhiVanChuyen.setText(CurrencyUtil.format(currentOrder.getPhiVanChuyen()));
        recalculateTotals();
    }

    private boolean saveOrder(boolean createNewAfter, boolean closeAfter) {
        if (lineItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu đặt hàng phải có ít nhất một mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTimF3.requestFocus();
            return false;
        }

        boolean isNew = (currentOrder == null || currentOrder.getId() == null);
        DatHangNCC order = isNew ? new DatHangNCC() : currentOrder;

        order.setName(txtSoPhieu.getText().trim());
        order.setNgay(dpkNgay.getDate() != null ? dpkNgay.getDate() : new Date());

        NhaCungCap selNcc = (NhaCungCap) cbNhaCungCap.getSelectedItem();
        if (selNcc != null && selNcc.getId() != null && !selNcc.getId().isEmpty()) {
            order.setNhaCungCapId(selNcc.getId());
            order.setTenNhaCungCap(selNcc.getName());
        } else {
            order.setNhaCungCapId(null);
            order.setTenNhaCungCap(null);
        }

        order.setNote(txtGhiChu.getText().trim());

        double totalTienHang = 0.0;
        for (DatHangNCCChiTiet ct : lineItems) totalTienHang += ct.getThanhTien();

        double tiLeGiam = parseDouble(txtTiLeGiamGia.getText(), 0.0);
        double tienGiam = parseDouble(txtTienGiamGia.getText(), 0.0);
        double tiLeThue = parseDouble(txtTiLeThue.getText(), 0.0);
        double tienThue = parseDouble(txtTienThue.getText(), 0.0);
        double phiVc = parseDouble(txtPhiVanChuyen.getText(), 0.0);
        double tongCong = (totalTienHang - tienGiam) + tienThue + phiVc;

        order.setTienHang(totalTienHang);
        order.setTiLeGiamGia(tiLeGiam);
        order.setTienGiamGia(tienGiam);
        order.setTiLeThue(tiLeThue);
        order.setTienThue(tienThue);
        order.setPhiVanChuyen(phiVc);
        order.setTongCong(tongCong);
        order.setChiTiet(new ArrayList<>(lineItems));

        boolean success;
        if (isNew) {
            success = datHangNCCDAO.insert(order);
        } else {
            success = datHangNCCDAO.update(order);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu đơn đặt hàng nhà cung cấp thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();

            if (createNewAfter) {
                resetForNew();
            } else if (closeAfter) {
                dispose();
            } else {
                currentOrder = order;
                setTitle("ĐẶT HÀNG NHÀ CUNG CẤP - CHỈNH SỬA [" + order.getName() + "]");
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu đơn đặt hàng. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveAndPrint(boolean directPrint) {
        if (saveOrder(false, false)) {
            String title = directPrint ? "In đơn đặt hàng" : "Xem trước đơn đặt hàng";
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================\n");
            sb.append("            ĐƠN ĐẶT HÀNG NHÀ CUNG CẤP                   \n");
            sb.append("========================================================\n");
            sb.append("Số phiếu: ").append(txtSoPhieu.getText()).append("    Ngày: ").append(dpkNgay.getText()).append("\n");
            sb.append("Nhà cung cấp: ").append(cbNhaCungCap.getSelectedItem() != null ? cbNhaCungCap.getSelectedItem() : "").append("\n");
            sb.append("Ghi chú: ").append(txtGhiChu.getText()).append("\n");
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("%-5s %-20s %-8s %-12s %-12s\n", "STT", "Mặt hàng", "SL", "Đơn giá", "Thành tiền"));
            for (int i = 0; i < lineItems.size(); i++) {
                DatHangNCCChiTiet ct = lineItems.get(i);
                sb.append(String.format("%-5d %-20s %-8.1f %-12s %-12s\n",
                        (i + 1),
                        ct.getTenHang() != null ? (ct.getTenHang().length() > 20 ? ct.getTenHang().substring(0, 18) + ".." : ct.getTenHang()) : "",
                        ct.getSoLuong(),
                        CurrencyUtil.format(ct.getDonGia()),
                        CurrencyUtil.format(ct.getThanhTien())
                ));
            }
            sb.append("--------------------------------------------------------\n");
            sb.append("Tiền hàng: ").append(lblTienHangVal.getText()).append("\n");
            sb.append("Giảm giá:  ").append(txtTienGiamGia.getText()).append(" (").append(txtTiLeGiamGia.getText()).append("%)\n");
            sb.append("Thuế VAT:  ").append(txtTienThue.getText()).append(" (").append(txtTiLeThue.getText()).append("%)\n");
            sb.append("Phí VC:    ").append(txtPhiVanChuyen.getText()).append("\n");
            sb.append("Tổng cộng: ").append(lblTongCongVal.getText()).append("\n");
            sb.append("========================================================\n");

            JTextArea ta = new JTextArea(sb.toString(), 20, 48);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void copyCurrentOrder() {
        if (currentOrder == null) return;
        currentOrder = null;
        setTitle("ĐẶT HÀNG NHÀ CUNG CẤP - THÊM MỚI (BẢN SAO)");
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(datHangNCCDAO.generateNextSoPhieu(new Date()));
        JOptionPane.showMessageDialog(this, "Đã sao chép đơn đặt hàng sang số phiếu mới. Vui lòng kiểm tra và lưu lại!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteCurrentOrder() {
        if (currentOrder == null || currentOrder.getId() == null) return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đơn đặt hàng [" + currentOrder.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (datHangNCCDAO.delete(currentOrder.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa đơn đặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                if (onDataSaved != null) onDataSaved.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa đơn đặt hàng này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void navigateRecord(int offset) {
        if (currentOrder == null || currentOrder.getId() == null) return;
        DatHangNCC target = datHangNCCDAO.getOffset(currentOrder.getId(), offset);
        if (target != null) {
            currentOrder = target;
            populateExistingData();
        } else {
            JOptionPane.showMessageDialog(this, offset < 0 ? "Đã ở đơn đặt hàng đầu tiên!" : "Đã ở đơn đặt hàng cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
