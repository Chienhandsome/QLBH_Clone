package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.*;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.*;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.dathang.KhachDatHangChiTiet;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
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
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class HoaDonBanHangPanel extends JPanel {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final DotKhuyenMaiDAO dotKhuyenMaiDAO = new DotKhuyenMaiDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();
    private final KhachDatHangDAO khachDatHangDAO = new KhachDatHangDAO();

    private SystemConfig systemConfig;
    private DonHang currentHoaDon;
    private String currentLoaiGiaName = "Giá bán";

    // Top barcode & quick add bar
    private JTextField txtBarcode;
    private JTextField txtSoLuongBarcode;
    private JButton btnThemBarcode;

    // Left category tree & catalog table
    private JTree treeNhomHang;
    private JTable tableCatalog;
    private DefaultTableModel catalogModel;
    private List<String> visibleCatalogColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_CATALOG_COLUMNS);
    private JPopupMenu popupCatalog;
    private List<MatHang> allMatHangList = new ArrayList<>();
    private List<MatHang> filteredCatalogList = new ArrayList<>();

    // Master Info Controls
    private DatePickerField dpkNgay;
    private JTextField txtSoHD;
    private JComboBox<KhachHang> cbKhachHang;
    private JComboBox<KhoHang> cbKhoXuat;
    private JComboBox<NhanVien> cbNhanVien;
    private JTextField txtDienGiai;
    private JTextField txtDiaChiGiaoHang;
    private JTextField txtGhiChu;

    // Mini Vertical Toolbar
    private JLabel lblTonKho;
    private JButton btnMoDatHang;
    private JButton btnLuuTam;
    private JButton btnMoLai;
    private JButton btnXoaDong;
    private JButton btnTangSL;
    private JButton btnGiamSL;
    private JButton btnDatSL;

    // Details Panel & Tabs
    private JTabbedPane tabbedPaneDetails;
    private JTable tableLayHang;
    private DefaultTableModel layHangModel;
    private JTable tableTraHang;
    private DefaultTableModel traHangModel;
    private List<String> visibleLayHangColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_COLUMNS);

    private final List<DonHangChiTiet> currentLayHangList = new ArrayList<>();
    private final List<DonHangChiTiet> currentTraHangList = new ArrayList<>();

    // Financial Summary Controls
    private JTextField txtTiLeGiamGia;
    private JTextField txtTienHang;
    private JTextField txtTienGiamGia;
    private JTextField txtTienDoiTra;
    private JTextField txtTongCong;
    private boolean isUpdatingFinancials = false;

    // Bottom Action Buttons
    private JButton btnTaoHoaDonMoi;
    private JButton btnThanhToan;
    private JButton btnThongKe;

    public HoaDonBanHangPanel() {
        initComponents();
        setupKeyBindings();
        loadInitialData();
        resetForNewInvoice();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(215, 230, 245));

        // Top Barcode Search Toolbar
        add(createTopBarcodePanel(), BorderLayout.NORTH);

        // Center SplitPane: Left (Category + Catalog) vs Right (Master Header + Details + Totals)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(380);
        mainSplit.setDividerSize(5);

        mainSplit.setLeftComponent(createLeftCatalogPanel());
        mainSplit.setRightComponent(createRightInvoicePanel());

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createTopBarcodePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBackground(new Color(230, 240, 252));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JLabel lblMavach = new JLabel("Mã vạch (F3):");
        lblMavach.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtBarcode = new JTextField(16);
        txtBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblSl = new JLabel("SL:");
        lblSl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSoLuongBarcode = new JTextField("1", 4);
        txtSoLuongBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnThemBarcode = new JButton("Thêm >>");
        btnThemBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThemBarcode.setBackground(Color.WHITE);
        btnThemBarcode.addActionListener(e -> onBarcodeAdd());

        txtBarcode.addActionListener(e -> onBarcodeAdd());
        txtBarcode.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterCatalogByKeyword(); }
            @Override public void removeUpdate(DocumentEvent e) { filterCatalogByKeyword(); }
            @Override public void changedUpdate(DocumentEvent e) { filterCatalogByKeyword(); }
        });

        panel.add(lblMavach);
        panel.add(txtBarcode);
        panel.add(lblSl);
        panel.add(txtSoLuongBarcode);
        panel.add(btnThemBarcode);

        return panel;
    }

    private JPanel createLeftCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(4, 4, 4, 2));

        JSplitPane splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitLeft.setDividerLocation(180);
        splitLeft.setDividerSize(4);

        // Category Tree
        treeNhomHang = new JTree();
        treeNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconHelper.getIcon("folder.png", 16, 16));
        renderer.setOpenIcon(IconHelper.getIcon("Folder.png", 16, 16));
        renderer.setClosedIcon(IconHelper.getIcon("Folder.png", 16, 16));
        treeNhomHang.setCellRenderer(renderer);

        treeNhomHang.addTreeSelectionListener(e -> onTreeSelectionChanged());
        JScrollPane spTree = new JScrollPane(treeNhomHang);
        splitLeft.setTopComponent(spTree);

        // Catalog Table
        tableCatalog = new JTable();
        tableCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCatalog.setRowHeight(22);
        tableCatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCatalog.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableCatalog.getTableHeader().setBackground(new Color(235, 242, 252));

        rebuildCatalogTableColumns();

        // Popup context menu for catalog table columns
        popupCatalog = new JPopupMenu();
        JMenuItem miChonCotCatalog = new JMenuItem("Chọn cột hiển thị...", IconHelper.getIcon("table.png", 16, 16));
        miChonCotCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miChonCotCatalog.addActionListener(e -> openChonCotHienThiCatalogDialog());

        JMenuItem miKhoiPhucCotCatalog = new JMenuItem("Khôi phục cột mặc định");
        miKhoiPhucCotCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miKhoiPhucCotCatalog.addActionListener(e -> {
            visibleCatalogColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_CATALOG_COLUMNS);
            rebuildCatalogTableColumns();
            refreshCatalogTable();
        });

        popupCatalog.add(miChonCotCatalog);
        popupCatalog.add(miKhoiPhucCotCatalog);

        tableCatalog.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openChonCotHienThiCatalogDialog();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        tableCatalog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateTonKhoDisplay();
                if (e.getClickCount() == 2) {
                    onCatalogItemSelected();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableCatalog.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableCatalog.getRowCount()) {
                        tableCatalog.setRowSelectionInterval(r, r);
                        updateTonKhoDisplay();
                    }
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableCatalog.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableCatalog.getRowCount()) {
                        tableCatalog.setRowSelectionInterval(r, r);
                        updateTonKhoDisplay();
                    }
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        tableCatalog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    onCatalogItemSelected();
                }
            }
        });

        JScrollPane spTable = new JScrollPane(tableCatalog);
        splitLeft.setBottomComponent(spTable);

        panel.add(splitLeft, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(new Color(215, 230, 245));

        // 1. Master Header Form
        panel.add(createMasterHeaderForm(), BorderLayout.NORTH);

        // 2. Middle Center: Vertical Mini-Toolbar + Details TabbedPane
        JPanel pnlCenter = new JPanel(new BorderLayout(4, 0));
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(4, 4, 4, 4));

        pnlCenter.add(createMiniVerticalToolbar(), BorderLayout.WEST);
        pnlCenter.add(createDetailsTabbedPane(), BorderLayout.CENTER);

        panel.add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom: Bottom Actions (Left) + Financial Summary (Right)
        JPanel pnlBottom = new JPanel(new BorderLayout(8, 0));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new EmptyBorder(0, 4, 4, 4));

        pnlBottom.add(createBottomActionButtons(), BorderLayout.WEST);
        pnlBottom.add(createFinancialSummaryPanel(), BorderLayout.EAST);

        panel.add(pnlBottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMasterHeaderForm() {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(800, 130));
        panel.setBackground(new Color(215, 230, 245));
        panel.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Navigation Arrows
        JButton btnPrev = new JButton(IconHelper.getIcon("arrow-180.png", 14, 14));
        btnPrev.setBounds(425, 6, 26, 24);
        btnPrev.setBackground(Color.WHITE);
        btnPrev.setToolTipText("Hóa đơn trước");
        btnPrev.addActionListener(e -> onNavigateInvoice(-1));
        panel.add(btnPrev);

        // Header Title in large bold red text
        JLabel lblMainTitle = new JLabel("HÓA ĐƠN BÁN HÀNG", SwingConstants.CENTER);
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMainTitle.setForeground(new Color(160, 25, 25));
        lblMainTitle.setBounds(460, 4, 250, 28);
        panel.add(lblMainTitle);

        JButton btnNext = new JButton(IconHelper.getIcon("arrow-000.png", 14, 14));
        btnNext.setBounds(720, 6, 26, 24);
        btnNext.setBackground(Color.WHITE);
        btnNext.setToolTipText("Hóa đơn sau");
        btnNext.addActionListener(e -> onNavigateInvoice(1));
        panel.add(btnNext);

        // Row 1: Ngày, Số HĐ
        JLabel lblNgay = new JLabel("Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgay.setBounds(10, 8, 50, 22);
        panel.add(lblNgay);

        dpkNgay = new DatePickerField();
        dpkNgay.setBounds(50, 8, 125, 24);
        panel.add(dpkNgay);

        JLabel lblSoHD = new JLabel("Số HĐ");
        lblSoHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoHD.setBounds(185, 8, 45, 22);
        panel.add(lblSoHD);

        txtSoHD = new JTextField();
        txtSoHD.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSoHD.setBackground(new Color(255, 254, 200));
        txtSoHD.setBounds(230, 8, 110, 24);
        panel.add(txtSoHD);

        // Row 2: K. hàng (F6)
        JLabel lblKhach = new JLabel("K. hàng (F6)");
        lblKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKhach.setBounds(10, 36, 75, 22);
        panel.add(lblKhach);

        cbKhachHang = new JComboBox<>();
        cbKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhachHang.setBounds(90, 36, 890, 24);
        cbKhachHang.addActionListener(e -> onCustomerSelected());
        panel.add(cbKhachHang);

        // Row 3: Kho xuất, Nhân viên
        JLabel lblKho = new JLabel("Kho xuất");
        lblKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKho.setBounds(10, 64, 75, 22);
        panel.add(lblKho);

        cbKhoXuat = new JComboBox<>();
        cbKhoXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoXuat.setBackground(new Color(235, 248, 200));
        cbKhoXuat.setBounds(90, 64, 250, 24);
        panel.add(cbKhoXuat);

        JLabel lblNv = new JLabel("Nhân viên");
        lblNv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNv.setBounds(360, 64, 70, 22);
        panel.add(lblNv);

        cbNhanVien = new JComboBox<>();
        cbNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVien.setBounds(435, 64, 545, 24);
        panel.add(cbNhanVien);

        // Row 4: Diễn giải, Đc giao hàng
        JLabel lblDiengiai = new JLabel("Diễn giải");
        lblDiengiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiengiai.setBounds(10, 92, 75, 22);
        panel.add(lblDiengiai);

        txtDienGiai = new JTextField("Xuất bán hàng");
        txtDienGiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienGiai.setBounds(90, 92, 250, 24);
        panel.add(txtDienGiai);

        JLabel lblDcGiao = new JLabel("Đc giao hàng");
        lblDcGiao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDcGiao.setBounds(360, 92, 75, 22);
        panel.add(lblDcGiao);

        txtDiaChiGiaoHang = new JTextField();
        txtDiaChiGiaoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiaChiGiaoHang.setBounds(435, 92, 545, 24);
        panel.add(txtDiaChiGiaoHang);

        return panel;
    }

    private JPanel createMiniVerticalToolbar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(85, 260));

        btnMoDatHang = createMiniButton("Mở đặt hàng");
        btnMoDatHang.addActionListener(e -> onOpenDatHang());

        btnLuuTam = createMiniButton("Lưu tạm (F9)");
        btnLuuTam.addActionListener(e -> onLuuTam());

        btnMoLai = createMiniButton("Mở lại (F11)");
        btnMoLai.addActionListener(e -> onMoLaiTam());

        lblTonKho = new JLabel("TỒN: 0", SwingConstants.CENTER);
        lblTonKho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTonKho.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTonKho.setMaximumSize(new Dimension(85, 22));

        btnXoaDong = createMiniButton("❌ Xóa");
        btnXoaDong.addActionListener(e -> onDeleteCurrentItem());

        btnTangSL = createMiniButton("Tăng sl 1");
        btnTangSL.addActionListener(e -> onAdjustQuantity(1));

        btnGiamSL = createMiniButton("Giảm sl 1");
        btnGiamSL.addActionListener(e -> onAdjustQuantity(-1));

        btnDatSL = createMiniButton("Đặt sl (F2)");
        btnDatSL.addActionListener(e -> onEditQuantityDialog());

        panel.add(btnMoDatHang);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnLuuTam);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnMoLai);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblTonKho);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnXoaDong);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnTangSL);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnGiamSL);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnDatSL);

        return panel;
    }

    private JButton createMiniButton(String text) {
        JButton btn = new JButton("<html><center>" + text + "</center></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setMaximumSize(new Dimension(85, 28));
        btn.setPreferredSize(new Dimension(85, 28));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 2, 2, 2));
        return btn;
    }

    private JTabbedPane createDetailsTabbedPane() {
        tabbedPaneDetails = new JTabbedPane();
        tabbedPaneDetails.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: KHÁCH LẤY HÀNG
        JPanel pnlLayHang = new JPanel(new BorderLayout());
        pnlLayHang.setBackground(Color.WHITE);

        tableLayHang = new JTable();
        tableLayHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableLayHang.setRowHeight(22);
        tableLayHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableLayHang.getTableHeader().setBackground(new Color(235, 242, 252));

        rebuildLayHangTableColumns();

        // Popup context menu for columns and actions
        JPopupMenu popupLayHang = new JPopupMenu();
        JMenuItem miChonCot = new JMenuItem("Chọn cột hiển thị...", IconHelper.getIcon("table.png", 16, 16));
        miChonCot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miChonCot.addActionListener(e -> openChonCotHienThiDialog());

        JMenuItem miKhoiPhucCot = new JMenuItem("Khôi phục cột mặc định");
        miKhoiPhucCot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miKhoiPhucCot.addActionListener(e -> {
            visibleLayHangColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_COLUMNS);
            applyColumnVisibility();
        });

        JMenuItem miDatSL = new JMenuItem("Đặt số lượng (F2)");
        miDatSL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miDatSL.addActionListener(e -> onEditQuantityDialog());

        JMenuItem miXoaDong = new JMenuItem("Xóa dòng (Delete)");
        miXoaDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miXoaDong.addActionListener(e -> onDeleteCurrentItem());

        popupLayHang.add(miChonCot);
        popupLayHang.add(miKhoiPhucCot);
        popupLayHang.addSeparator();
        popupLayHang.add(miDatSL);
        popupLayHang.add(miXoaDong);

        tableLayHang.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openChonCotHienThiDialog();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        tableLayHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditQuantityDialog();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableLayHang.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableLayHang.getRowCount()) {
                        tableLayHang.setRowSelectionInterval(r, r);
                    }
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableLayHang.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableLayHang.getRowCount()) {
                        tableLayHang.setRowSelectionInterval(r, r);
                    }
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane spLayHang = new JScrollPane(tableLayHang);
        pnlLayHang.add(spLayHang, BorderLayout.CENTER);
        tabbedPaneDetails.addTab("KHÁCH LẤY HÀNG", pnlLayHang);

        // Tab 2: KHÁCH TRẢ HÀNG
        JPanel pnlTraHang = new JPanel(new BorderLayout());
        pnlTraHang.setBackground(Color.WHITE);

        JPanel pnlTraHangTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTraHangTop.setOpaque(false);
        JButton btnChonTra = new JButton("Chọn mặt hàng trả", IconHelper.getIcon("table.png", 16, 16));
        btnChonTra.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChonTra.setBackground(Color.WHITE);
        btnChonTra.addActionListener(e -> onChonMatHangTra());
        pnlTraHangTop.add(btnChonTra);
        pnlTraHang.add(pnlTraHangTop, BorderLayout.NORTH);

        String[] headersTraHang = {"STT", "Mặt hàng", "ĐVT", "Mã hàng", "Số lượng", "Đơn giá", "CK%", "Thành tiền", "Ghi chú"};
        traHangModel = new DefaultTableModel(headersTraHang, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableTraHang = new JTable(traHangModel);
        tableTraHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTraHang.setRowHeight(22);
        tableTraHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableTraHang.getTableHeader().setBackground(new Color(235, 242, 252));

        JScrollPane spTraHang = new JScrollPane(tableTraHang);
        pnlTraHang.add(spTraHang, BorderLayout.CENTER);
        tabbedPaneDetails.addTab("KHÁCH TRẢ HÀNG", pnlTraHang);

        return tabbedPaneDetails;
    }

    private JPanel createBottomActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setOpaque(false);

        btnTaoHoaDonMoi = createStyledActionButton("Tạo hóa đơn<br>mới", "btnNew.Image.png", new Color(255, 255, 255), new Color(175, 195, 225), 130, 52);
        btnTaoHoaDonMoi.addActionListener(e -> resetForNewInvoice());

        btnThanhToan = createStyledActionButton("<b style='color:#901515;'>Thanh toán</b><br>(F12)", "coins.png", new Color(255, 250, 205), new Color(225, 200, 120), 135, 52);
        btnThanhToan.addActionListener(e -> onThanhToan());

        btnThongKe = createStyledActionButton("Thống kê<br>(F10)", "Statistic16.png", new Color(255, 255, 255), new Color(175, 195, 225), 120, 52);
        btnThongKe.addActionListener(e -> onThongKe());

        panel.add(btnTaoHoaDonMoi);
        panel.add(btnThanhToan);
        panel.add(btnThongKe);

        return panel;
    }

    private JButton createStyledActionButton(String htmlText, String iconName, Color bg, Color borderCol, int width, int height) {
        JButton btn = new JButton("<html><center>" + htmlText + "</center></html>");
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 20, 20));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(6);
        btn.setBorder(new CompoundBorder(
                new LineBorder(borderCol, 1, true),
                new EmptyBorder(3, 6, 3, 6)
        ));

        final Color normalBg = bg;
        final Color hoverBg = (bg.equals(Color.WHITE)) ? new Color(235, 244, 255) : new Color(255, 244, 180);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normalBg);
            }
        });

        return btn;
    }

    private JPanel createFinancialSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(180, 205, 230)),
                new EmptyBorder(2, 6, 2, 6)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);

        // Tiền hàng
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblTh = new JLabel("Tiền hàng");
        lblTh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblTh, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTienHang = createCyanNumberField();
        pnlForm.add(txtTienHang, gbc);

        // Giảm giá
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel lblGg = new JLabel("Giảm giá");
        lblGg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblGg, gbc);

        gbc.gridx = 1;
        txtTiLeGiamGia = new JTextField("0 %", 4);
        txtTiLeGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTiLeGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlForm.add(txtTiLeGiamGia, gbc);

        gbc.gridx = 2;
        txtTienGiamGia = createCyanNumberField();
        pnlForm.add(txtTienGiamGia, gbc);

        // Đổi trả
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel lblDt = new JLabel("Đổi trả");
        lblDt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblDt, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTienDoiTra = createCyanNumberField();
        pnlForm.add(txtTienDoiTra, gbc);

        // Tổng cộng
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel lblTc = new JLabel("Tổng cộng");
        lblTc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblTc, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTongCong = createCyanNumberField();
        txtTongCong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTongCong.setForeground(new Color(0, 30, 150));
        pnlForm.add(txtTongCong, gbc);

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalculateFinancials(); }
            @Override public void removeUpdate(DocumentEvent e) { recalculateFinancials(); }
            @Override public void changedUpdate(DocumentEvent e) { recalculateFinancials(); }
        };
        txtTiLeGiamGia.getDocument().addDocumentListener(dl);

        panel.add(pnlForm, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createCyanNumberField() {
        JTextField tf = new JTextField("0", 12);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        tf.setBackground(new Color(216, 245, 250));
        tf.setEditable(false);
        return tf;
    }

    private void setupKeyBindings() {
        KeyStroke f3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        registerKeyboardAction(e -> txtBarcode.requestFocusInWindow(), f3, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        registerKeyboardAction(e -> cbKhachHang.requestFocusInWindow(), f6, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        registerKeyboardAction(e -> onLuuTam(), f9, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f11 = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        registerKeyboardAction(e -> onMoLaiTam(), f11, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f12 = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        registerKeyboardAction(e -> onThanhToan(), f12, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f2 = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        registerKeyboardAction(e -> onEditQuantityDialog(), f2, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke del = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        registerKeyboardAction(e -> onDeleteCurrentItem(), del, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadInitialData() {
        try {
            systemConfig = systemConfigDAO.loadConfig();
        } catch (Exception ignored) {}
        if (systemConfig == null) systemConfig = new SystemConfig();

        loadCategoryTree();

        cbKhachHang.removeAllItems();
        KhachHang dummyKhach = new KhachHang();
        dummyKhach.setName("Khách lẻ");
        cbKhachHang.addItem(dummyKhach);
        List<KhachHang> khList = khachHangDAO.getAll();
        for (KhachHang kh : khList) {
            if (kh.getStatus() >= 0) cbKhachHang.addItem(kh);
        }

        cbKhoXuat.removeAllItems();
        List<KhoHang> khoList = khoHangDAO.getAllActive();
        for (KhoHang k : khoList) {
            if (!k.isFolder()) cbKhoXuat.addItem(k);
        }

        cbNhanVien.removeAllItems();
        List<NhanVien> nvList = nhanVienDAO.getAllActive();
        for (NhanVien nv : nvList) {
            if (!nv.isFolder() && !nv.isSeparator()) cbNhanVien.addItem(nv);
        }

        allMatHangList = matHangDAO.getAll();
        filteredCatalogList = new ArrayList<>(allMatHangList);
        refreshCatalogTable();
    }

    private void loadCategoryTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        List<NhomMatHang> groupList = nhomMatHangDAO.getAll();
        for (NhomMatHang grp : groupList) {
            if (grp.getStatus() >= 0) {
                root.add(new DefaultMutableTreeNode(grp));
            }
        }
        treeNhomHang.setModel(new DefaultTreeModel(root));
    }

    private void onTreeSelectionChanged() {
        filterCatalogByKeyword();
    }

    private void filterCatalogByKeyword() {
        String kw = txtBarcode != null ? txtBarcode.getText().trim().toLowerCase() : "";
        DefaultMutableTreeNode selected = (DefaultMutableTreeNode) treeNhomHang.getLastSelectedPathComponent();
        String selectedGroupId = null;
        if (selected != null && !selected.isRoot() && selected.getUserObject() instanceof NhomMatHang) {
            selectedGroupId = ((NhomMatHang) selected.getUserObject()).getId();
        }

        filteredCatalogList = new ArrayList<>();
        for (MatHang m : allMatHangList) {
            if (m == null) continue;
            // 1. Lọc theo nhóm danh mục nếu có chọn
            if (selectedGroupId != null && !selectedGroupId.equals(m.getNhomMatHangId())) {
                continue;
            }
            // 2. Lọc theo từ khóa tìm kiếm (mã hàng, mã sẵn có, tên hàng, đơn vị tính)
            if (!kw.isEmpty()) {
                String code = m.getCode() != null ? m.getCode().toLowerCase() : "";
                String name = m.getName() != null ? m.getName().toLowerCase() : "";
                String maSanCo = m.getMaSanCo() != null ? m.getMaSanCo().toLowerCase() : "";
                String dvt = m.getTenDonViTinh() != null ? m.getTenDonViTinh().toLowerCase() : "";
                if (!code.contains(kw) && !name.contains(kw) && !maSanCo.contains(kw) && !dvt.contains(kw)) {
                    continue;
                }
            }
            filteredCatalogList.add(m);
        }
        refreshCatalogTable();
        if (!filteredCatalogList.isEmpty() && tableCatalog.getRowCount() > 0) {
            tableCatalog.setRowSelectionInterval(0, 0);
            updateTonKhoDisplay();
        }
    }

    private void rebuildCatalogTableColumns() {
        Vector<String> columnIdentifiers = new Vector<>();
        columnIdentifiers.add("STT");
        columnIdentifiers.addAll(visibleCatalogColumns);

        catalogModel = new DefaultTableModel(columnIdentifiers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tableCatalog.setModel(catalogModel);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        if (tableCatalog.getColumnModel().getColumnCount() > 0) {
            tableCatalog.getColumnModel().getColumn(0).setPreferredWidth(35);
            tableCatalog.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        }

        for (int i = 0; i < visibleCatalogColumns.size(); i++) {
            String colName = visibleCatalogColumns.get(i);
            TableColumn tc = tableCatalog.getColumnModel().getColumn(i + 1);
            switch (colName) {
                case "Mã hàng":
                    tc.setPreferredWidth(65);
                    break;
                case "Tên hàng":
                case "Tên mặt hàng":
                    tc.setPreferredWidth(130);
                    break;
                case "ĐVT":
                    tc.setPreferredWidth(45);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "ĐVT chẵn":
                    tc.setPreferredWidth(55);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "Giá bán":
                case "Đơn giá 2":
                case "giá blabla":
                case "Giá bán chẵn":
                case "Giá nhập":
                case "Tồn kho":
                    tc.setPreferredWidth(65);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Bảo hành":
                case "Quy đổi":
                    tc.setPreferredWidth(55);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "Mã vạch":
                    tc.setPreferredWidth(75);
                    break;
                case "Ghi chú":
                    tc.setPreferredWidth(90);
                    break;
                default:
                    tc.setPreferredWidth(65);
                    break;
            }
        }
    }

    private void openChonCotHienThiCatalogDialog() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        ChonCotHienThiDialog dlg = new ChonCotHienThiDialog(
                parentWin,
                visibleCatalogColumns,
                ChonCotHienThiDialog.ALL_POSSIBLE_CATALOG_COLUMNS,
                ChonCotHienThiDialog.DEFAULT_VISIBLE_CATALOG_COLUMNS
        );
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            visibleCatalogColumns = dlg.getResultVisibleColumns();
            rebuildCatalogTableColumns();
            refreshCatalogTable();
        }
    }

    private void refreshCatalogTable() {
        catalogModel.setRowCount(0);
        int stt = 1;
        for (MatHang m : filteredCatalogList) {
            Object[] row = new Object[visibleCatalogColumns.size() + 1];
            row[0] = String.format("%04d", stt++);
            for (int i = 0; i < visibleCatalogColumns.size(); i++) {
                String col = visibleCatalogColumns.get(i);
                switch (col) {
                    case "Mã hàng":
                        row[i + 1] = m.getCode() != null ? m.getCode() : "";
                        break;
                    case "Tên hàng":
                    case "Tên mặt hàng":
                        row[i + 1] = m.getName() != null ? m.getName() : "";
                        break;
                    case "ĐVT":
                        row[i + 1] = m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "";
                        break;
                    case "ĐVT chẵn":
                        row[i + 1] = m.getTenDonViTinhChan() != null ? m.getTenDonViTinhChan() : "";
                        break;
                    case "Giá bán":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBan());
                        break;
                    case "Bảo hành":
                        row[i + 1] = m.getBaoHanh() != null ? m.getBaoHanh() : "";
                        break;
                    case "Đơn giá 2":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBan2());
                        break;
                    case "giá blabla":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBan3());
                        break;
                    case "Quy đổi":
                        row[i + 1] = m.getQuyDoi() > 0 ? String.format("%.0f", m.getQuyDoi()) : "";
                        break;
                    case "Giá bán chẵn":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBanChan());
                        break;
                    case "Mã vạch":
                        row[i + 1] = m.getMaSanCo() != null ? m.getMaSanCo() : "";
                        break;
                    case "Tồn kho":
                        row[i + 1] = "0";
                        break;
                    case "Giá nhập":
                        row[i + 1] = CurrencyUtil.format(m.getGiaNhap());
                        break;
                    case "Ghi chú":
                        row[i + 1] = m.getGhiChu() != null ? m.getGhiChu() : "";
                        break;
                    default:
                        row[i + 1] = "";
                        break;
                }
            }
            catalogModel.addRow(row);
        }
    }

    private double getPriceForMatHang(MatHang m) {
        return getPriceForMatHang(m, 0);
    }

    private double getPriceForMatHang(MatHang m, int priceIndex) {
        if (m == null) return 0.0;
        switch (priceIndex) {
            case 1: return m.getGiaBan2() > 0 ? m.getGiaBan2() : m.getGiaBan();
            case 2: return m.getGiaBan3() > 0 ? m.getGiaBan3() : m.getGiaBan();
            case 3: return m.getGiaBan4() > 0 ? m.getGiaBan4() : m.getGiaBan();
            default: return m.getGiaBan();
        }
    }

    private void onCatalogItemSelected() {
        int row = tableCatalog.getSelectedRow();
        if (row < 0 || row >= filteredCatalogList.size()) return;
        MatHang m = filteredCatalogList.get(row);
        openNhapSoLuongForProduct(m);
    }

    private void onBarcodeAdd() {
        String code = txtBarcode.getText().trim();
        if (code.isEmpty()) {
            int row = tableCatalog.getSelectedRow();
            if (row >= 0 && row < filteredCatalogList.size()) {
                openNhapSoLuongForProduct(filteredCatalogList.get(row));
            }
            return;
        }

        MatHang found = null;
        // Ưu tiên khớp chính xác mã hàng hoặc mã vạch
        for (MatHang m : filteredCatalogList) {
            if (code.equalsIgnoreCase(m.getCode()) || code.equalsIgnoreCase(m.getMaSanCo())) {
                found = m;
                break;
            }
        }
        if (found == null && !filteredCatalogList.isEmpty()) {
            int row = tableCatalog.getSelectedRow();
            if (row >= 0 && row < filteredCatalogList.size()) {
                found = filteredCatalogList.get(row);
            } else {
                found = filteredCatalogList.get(0);
            }
        }
        if (found == null) {
            for (MatHang m : allMatHangList) {
                if (code.equalsIgnoreCase(m.getCode()) || code.equalsIgnoreCase(m.getMaSanCo()) || (m.getName() != null && m.getName().toLowerCase().contains(code.toLowerCase()))) {
                    found = m;
                    break;
                }
            }
        }

        if (found != null) {
            openNhapSoLuongForProduct(found);
            txtBarcode.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mặt hàng với mã: " + code, "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openNhapSoLuongForProduct(MatHang m) {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        ChonLoaiGiaDialog priceDlg = new ChonLoaiGiaDialog(parentWin, systemConfig,
                m.getGiaBan(), m.getGiaBan2(), m.getGiaBan3(), m.getGiaBan4());
        priceDlg.setVisible(true);

        int selectedPriceIndex = priceDlg.getSelectedPriceIndex();
        if (selectedPriceIndex < 0) return;

        double price = getPriceForMatHang(m, selectedPriceIndex);
        double defaultQty = 1.0;
        try {
            defaultQty = Double.parseDouble(txtSoLuongBarcode.getText().trim().replace(",", "."));
        } catch (Exception ignored) {}

        double discount = dotKhuyenMaiDAO.getActiveDiscountPercentForProduct(m.getId(), m.getCode(), m.getNhomMatHangId(), dpkNgay.getDate());
        if (discount <= 0 && m.getGiamGiaPt() > 0) {
            discount = m.getGiamGiaPt();
        }

        NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, price, m.getTenDonViTinh(),
                defaultQty, discount, isDiscountInputAllowed());
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            currentLoaiGiaName = priceDlg.getSelectedPriceName();

            if (priceDlg.isApplyToAll()) {
                applyPriceToAllInvoiceItems(selectedPriceIndex);
            }

            double finalQty = dlg.getResultSoLuong();
            double finalDiscount = dlg.getResultChietKhau();

            DonHangChiTiet existing = null;
            for (DonHangChiTiet ct : currentLayHangList) {
                if (m.getId() != null && m.getId().equals(ct.getMatHangId()) && Math.abs(ct.getDonGia() - price) < 0.001) {
                    existing = ct;
                    break;
                }
            }

            if (existing != null) {
                existing.setSoLuong(existing.getSoLuong() + finalQty);
                existing.setTiLeGiamGia(finalDiscount);
                existing.tinhThanhTien();
            } else {
                DonHangChiTiet ct = new DonHangChiTiet();
                ct.setId(UUID.randomUUID().toString());
                ct.setMatHangId(m.getId());
                ct.setMaHang(m.getCode());
                ct.setTenHang(m.getName());
                ct.setDonViTinhId(m.getDonViTinhId());
                ct.setTenDonViTinh(m.getTenDonViTinh());
                ct.setDonGia(price);
                ct.setSoLuong(finalQty);
                ct.setTiLeGiamGia(finalDiscount);
                ct.setBaoHanh(m.getBaoHanh());
                ct.tinhThanhTien();
                currentLayHangList.add(ct);
            }

            refreshLayHangTable();
            recalculateFinancials();
        }
    }

    private void applyPriceToAllInvoiceItems(int priceIndex) {
        for (DonHangChiTiet ct : currentLayHangList) {
            MatHang mh = matHangDAO.findById(ct.getMatHangId());
            if (mh != null) {
                ct.setDonGia(getPriceForMatHang(mh, priceIndex));
                ct.tinhThanhTien();
            }
        }
    }

    private void onChonMatHangTra() {
        if (systemConfig != null && systemConfig.isBatBuocDoiTraHangTheoDon()) {
            onChonMatHangTraTheoHoaDon();
            return;
        }

        int row = tableCatalog.getSelectedRow();
        if (row < 0 || row >= filteredCatalogList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mặt hàng trong danh mục bên trái để trả hàng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MatHang m = filteredCatalogList.get(row);
        double price = getPriceForMatHang(m);

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, price, m.getTenDonViTinh(),
                1.0, 0.0, isDiscountInputAllowed());
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setId(UUID.randomUUID().toString());
            ct.setMatHangId(m.getId());
            ct.setMaHang(m.getCode());
            ct.setTenHang(m.getName());
            ct.setDonViTinhId(m.getDonViTinhId());
            ct.setTenDonViTinh(m.getTenDonViTinh());
            ct.setDonGia(price);
            ct.setTraHang(true);
            ct.setSoLuong(dlg.getResultSoLuong());
            ct.setTiLeGiamGia(dlg.getResultChietKhau());
            ct.tinhThanhTien();
            currentTraHangList.add(ct);

            refreshTraHangTable();
            recalculateFinancials();
        }
    }

    private void onChonMatHangTraTheoHoaDon() {
        // Thời hạn đổi trả luôn tính theo ngày thực tế, không theo ngày hóa đơn đang nhập
        // để tránh việc đổi ngày chứng từ làm vượt qua chính sách đổi trả.
        Date returnDate = new Date();
        int allowedDays = Math.max(0, systemConfig.getSoNgayChoPhepDoiTra());

        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(SalesConfigSupport.startOfDay(returnDate));
        fromCalendar.add(Calendar.DAY_OF_MONTH, -allowedDays);

        List<DonHang> eligibleInvoices = new ArrayList<>();
        for (DonHang invoice : hoaDonDAO.getDanhSachHoaDon(fromCalendar.getTime(), returnDate, null, null)) {
            if (invoice.getChiTietLayHang() != null
                    && !invoice.getChiTietLayHang().isEmpty()
                    && SalesConfigSupport.isReturnAllowed(invoice.getNgay(), returnDate, allowedDays)) {
                eligibleInvoices.add(invoice);
            }
        }

        if (eligibleInvoices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có hóa đơn đủ điều kiện đổi trả trong " + allowedDays + " ngày gần đây.",
                    "Đổi trả theo hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ReturnInvoiceOption[] invoiceOptions = eligibleInvoices.stream()
                .map(ReturnInvoiceOption::new)
                .toArray(ReturnInvoiceOption[]::new);
        ReturnInvoiceOption selectedInvoice = (ReturnInvoiceOption) JOptionPane.showInputDialog(
                this, "Chọn hóa đơn gốc:", "Đổi trả theo hóa đơn",
                JOptionPane.PLAIN_MESSAGE, null, invoiceOptions, invoiceOptions[0]);
        if (selectedInvoice == null) return;

        ReturnItemOption[] itemOptions = selectedInvoice.invoice.getChiTietLayHang().stream()
                .map(ReturnItemOption::new)
                .toArray(ReturnItemOption[]::new);
        ReturnItemOption selectedItem = (ReturnItemOption) JOptionPane.showInputDialog(
                this, "Chọn mặt hàng cần trả:", "Hóa đơn " + selectedInvoice.invoice.getName(),
                JOptionPane.PLAIN_MESSAGE, null, itemOptions, itemOptions[0]);
        if (selectedItem == null) return;

        DonHangChiTiet source = selectedItem.item;
        MatHang matHang = matHangDAO.findById(source.getMatHangId());
        if (matHang == null) {
            matHang = new MatHang(source.getMatHangId(), source.getMaHang(), source.getTenHang(), source.getDonGia(), 0);
            matHang.setTenDonViTinh(source.getTenDonViTinh());
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        NhapSoLuongDialog quantityDialog = new NhapSoLuongDialog(parentWin, matHang, source.getDonGia(),
                source.getTenDonViTinh(), 1.0, source.getTiLeGiamGia(), isDiscountInputAllowed());
        quantityDialog.setVisible(true);
        if (!quantityDialog.isConfirmed()) return;

        if (quantityDialog.getResultSoLuong() > source.getSoLuong()) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng trả không được vượt quá số lượng đã bán (" + source.getSoLuong() + ").",
                    "Số lượng không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DonHangChiTiet returnedItem = new DonHangChiTiet();
        returnedItem.setId(UUID.randomUUID().toString());
        returnedItem.setMatHangId(source.getMatHangId());
        returnedItem.setMaHang(source.getMaHang());
        returnedItem.setTenHang(source.getTenHang());
        returnedItem.setDonViTinhId(source.getDonViTinhId());
        returnedItem.setTenDonViTinh(source.getTenDonViTinh());
        returnedItem.setDonGia(source.getDonGia());
        returnedItem.setTraHang(true);
        returnedItem.setSoLuong(quantityDialog.getResultSoLuong());
        returnedItem.setTiLeGiamGia(quantityDialog.getResultChietKhau());
        returnedItem.setGhiChu("Trả theo HĐ: " + selectedInvoice.invoice.getName());
        returnedItem.tinhThanhTien();
        currentTraHangList.add(returnedItem);

        selectCustomerById(selectedInvoice.invoice.getKhachHangId());
        refreshTraHangTable();
        recalculateFinancials();
    }

    private void rebuildLayHangTableColumns() {
        Vector<String> columnIdentifiers = new Vector<>();
        columnIdentifiers.add("STT");
        columnIdentifiers.addAll(visibleLayHangColumns);

        layHangModel = new DefaultTableModel(columnIdentifiers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                if (col <= 0 || col > visibleLayHangColumns.size()) return false;
                String colName = visibleLayHangColumns.get(col - 1);
                return "Ghi chú".equals(colName) || "S.lượng".equals(colName) || "CK%".equals(colName);
            }
        };
        tableLayHang.setModel(layHangModel);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        if (tableLayHang.getColumnModel().getColumnCount() > 0) {
            tableLayHang.getColumnModel().getColumn(0).setPreferredWidth(35);
            tableLayHang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        }

        for (int i = 0; i < visibleLayHangColumns.size(); i++) {
            String colName = visibleLayHangColumns.get(i);
            TableColumn tc = tableLayHang.getColumnModel().getColumn(i + 1);
            switch (colName) {
                case "Tên mặt hàng":
                    tc.setPreferredWidth(160);
                    break;
                case "Mã hàng":
                    tc.setPreferredWidth(75);
                    break;
                case "Đơn giá":
                    tc.setPreferredWidth(80);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "ĐVT":
                    tc.setPreferredWidth(50);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "CK%":
                    tc.setPreferredWidth(45);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Tiền giảm":
                    tc.setPreferredWidth(75);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Ghi chú":
                    tc.setPreferredWidth(90);
                    break;
                case "Bảo hành":
                    tc.setPreferredWidth(65);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "S.lượng":
                    tc.setPreferredWidth(55);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Thành tiền":
                    tc.setPreferredWidth(85);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Kích thước":
                    tc.setPreferredWidth(75);
                    break;
                case "Hạn sử dụng":
                    tc.setPreferredWidth(85);
                    tc.setCellRenderer(centerRenderer);
                    break;
                default:
                    tc.setPreferredWidth(80);
                    break;
            }
        }
    }

    private void refreshLayHangTable(int selectRow) {
        layHangModel.setRowCount(0);
        int stt = 1;
        for (DonHangChiTiet ct : currentLayHangList) {
            Vector<Object> rowData = new Vector<>();
            rowData.add(String.format("%04d", stt++));
            for (String colName : visibleLayHangColumns) {
                switch (colName) {
                    case "Tên mặt hàng":
                        rowData.add(ct.getTenHang() != null ? ct.getTenHang() : "");
                        break;
                    case "Mã hàng":
                        rowData.add(ct.getMaHang() != null ? ct.getMaHang() : "");
                        break;
                    case "Đơn giá":
                        rowData.add(CurrencyUtil.format(ct.getDonGia()));
                        break;
                    case "ĐVT":
                        rowData.add(ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "");
                        break;
                    case "CK%":
                        rowData.add(ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0");
                        break;
                    case "Tiền giảm":
                        rowData.add(CurrencyUtil.format(ct.getTienGiam()));
                        break;
                    case "Ghi chú":
                        rowData.add(ct.getGhiChu() != null ? ct.getGhiChu() : "");
                        break;
                    case "Bảo hành":
                        rowData.add(ct.getBaoHanh() != null ? ct.getBaoHanh() : "");
                        break;
                    case "S.lượng":
                        rowData.add(ct.getSoLuong() == (long) ct.getSoLuong() ? String.format("%d", (long) ct.getSoLuong()) : String.valueOf(ct.getSoLuong()));
                        break;
                    case "Thành tiền":
                        rowData.add(CurrencyUtil.format(ct.getThanhTien()));
                        break;
                    case "Kích thước":
                        rowData.add("");
                        break;
                    case "Hạn sử dụng":
                        rowData.add("");
                        break;
                    default:
                        rowData.add("");
                        break;
                }
            }
            layHangModel.addRow(rowData);
        }
        if (selectRow >= 0 && selectRow < tableLayHang.getRowCount()) {
            tableLayHang.setRowSelectionInterval(selectRow, selectRow);
        }
    }

    private void refreshLayHangTable() {
        int curSel = tableLayHang != null ? tableLayHang.getSelectedRow() : -1;
        refreshLayHangTable(curSel);
    }

    private void refreshTraHangTable(int selectRow) {
        traHangModel.setRowCount(0);
        int stt = 1;
        for (DonHangChiTiet ct : currentTraHangList) {
            traHangModel.addRow(new Object[]{
                    String.format("%04d", stt++),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getSoLuong() == (long) ct.getSoLuong() ? String.format("%d", (long) ct.getSoLuong()) : String.valueOf(ct.getSoLuong()),
                    CurrencyUtil.format(ct.getDonGia()),
                    ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0",
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : ""
            });
        }
        if (selectRow >= 0 && selectRow < tableTraHang.getRowCount()) {
            tableTraHang.setRowSelectionInterval(selectRow, selectRow);
        }
    }

    private void refreshTraHangTable() {
        int curSel = tableTraHang != null ? tableTraHang.getSelectedRow() : -1;
        refreshTraHangTable(curSel);
    }

    private void recalculateFinancials() {
        if (isUpdatingFinancials) return;
        isUpdatingFinancials = true;
        try {
            double tienHang = 0.0;
            for (DonHangChiTiet ct : currentLayHangList) {
                tienHang += ct.getThanhTien();
            }

            double tienTra = 0.0;
            for (DonHangChiTiet ct : currentTraHangList) {
                tienTra += ct.getThanhTien();
            }

            double tileGiam = 0.0;
            try {
                String str = txtTiLeGiamGia.getText().replace("%", "").trim().replace(",", ".");
                if (!str.isEmpty()) tileGiam = Double.parseDouble(str);
            } catch (Exception ignored) {}
            tileGiam = Math.max(0, Math.min(100, tileGiam));

            double tienGiam = tienHang * (tileGiam / 100.0);
            double tongCong = SalesConfigSupport.roundMoney(
                    Math.max(0, tienHang - tienGiam - tienTra),
                    systemConfig != null ? systemConfig.getLamTronTien() : null);

            txtTienHang.setText(CurrencyUtil.format(tienHang));
            txtTienGiamGia.setText(CurrencyUtil.format(tienGiam));
            txtTienDoiTra.setText(CurrencyUtil.format(tienTra));
            txtTongCong.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingFinancials = false;
        }
    }

    private void onCustomerSelected() {
        KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
        if (kh != null) {
            txtDiaChiGiaoHang.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
        }
    }

    private void updateTonKhoDisplay() {
        int row = tableCatalog.getSelectedRow();
        if (row >= 0 && row < filteredCatalogList.size()) {
            MatHang m = filteredCatalogList.get(row);
            lblTonKho.setText("TỒN: " + (int) m.getTonToiDa());
        }
    }

    private void onDeleteCurrentItem() {
        int selTab = tabbedPaneDetails.getSelectedIndex();
        if (selTab == 0) {
            int row = tableLayHang.getSelectedRow();
            if (row >= 0 && row < currentLayHangList.size()) {
                currentLayHangList.remove(row);
                int nextRow = Math.min(row, currentLayHangList.size() - 1);
                refreshLayHangTable(nextRow);
                recalculateFinancials();
            }
        } else {
            int row = tableTraHang.getSelectedRow();
            if (row >= 0 && row < currentTraHangList.size()) {
                currentTraHangList.remove(row);
                int nextRow = Math.min(row, currentTraHangList.size() - 1);
                refreshTraHangTable(nextRow);
                recalculateFinancials();
            }
        }
    }

    private void onAdjustQuantity(int delta) {
        int selTab = tabbedPaneDetails.getSelectedIndex();
        if (selTab == 0) {
            int row = tableLayHang.getSelectedRow();
            if (row < 0 && tableLayHang.getRowCount() > 0) {
                row = tableLayHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentLayHangList.size()) {
                DonHangChiTiet ct = currentLayHangList.get(row);
                double newQty = ct.getSoLuong() + delta;
                if (newQty > 0) {
                    ct.setSoLuong(newQty);
                    ct.tinhThanhTien();
                    refreshLayHangTable(row);
                    recalculateFinancials();
                } else if (newQty == 0) {
                    currentLayHangList.remove(row);
                    int nextRow = Math.min(row, currentLayHangList.size() - 1);
                    refreshLayHangTable(nextRow);
                    recalculateFinancials();
                }
            }
        } else {
            int row = tableTraHang.getSelectedRow();
            if (row < 0 && tableTraHang.getRowCount() > 0) {
                row = tableTraHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentTraHangList.size()) {
                DonHangChiTiet ct = currentTraHangList.get(row);
                double newQty = ct.getSoLuong() + delta;
                if (newQty > 0) {
                    ct.setSoLuong(newQty);
                    ct.tinhThanhTien();
                    refreshTraHangTable(row);
                    recalculateFinancials();
                } else if (newQty == 0) {
                    currentTraHangList.remove(row);
                    int nextRow = Math.min(row, currentTraHangList.size() - 1);
                    refreshTraHangTable(nextRow);
                    recalculateFinancials();
                }
            }
        }
    }

    private void onEditQuantityDialog() {
        int selTab = tabbedPaneDetails.getSelectedIndex();
        if (selTab == 0) {
            int row = tableLayHang.getSelectedRow();
            if (row < 0 && tableLayHang.getRowCount() > 0) {
                row = tableLayHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentLayHangList.size()) {
                DonHangChiTiet ct = currentLayHangList.get(row);
                MatHang m = matHangDAO.findById(ct.getMatHangId());
                if (m == null) {
                    m = new MatHang(ct.getMatHangId(), ct.getMaHang(), ct.getTenHang(), ct.getDonGia(), 0);
                }

                Window parentWin = SwingUtilities.getWindowAncestor(this);
                NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, ct.getDonGia(), ct.getTenDonViTinh(),
                        ct.getSoLuong(), ct.getTiLeGiamGia(), isDiscountInputAllowed());
                dlg.setVisible(true);

                if (dlg.isConfirmed()) {
                    ct.setSoLuong(dlg.getResultSoLuong());
                    ct.setTiLeGiamGia(dlg.getResultChietKhau());
                    ct.tinhThanhTien();
                    refreshLayHangTable(row);
                    recalculateFinancials();
                }
            }
        } else {
            int row = tableTraHang.getSelectedRow();
            if (row < 0 && tableTraHang.getRowCount() > 0) {
                row = tableTraHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentTraHangList.size()) {
                DonHangChiTiet ct = currentTraHangList.get(row);
                MatHang m = matHangDAO.findById(ct.getMatHangId());
                if (m == null) {
                    m = new MatHang(ct.getMatHangId(), ct.getMaHang(), ct.getTenHang(), ct.getDonGia(), 0);
                }

                Window parentWin = SwingUtilities.getWindowAncestor(this);
                NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, ct.getDonGia(), ct.getTenDonViTinh(),
                        ct.getSoLuong(), ct.getTiLeGiamGia(), isDiscountInputAllowed());
                dlg.setVisible(true);

                if (dlg.isConfirmed()) {
                    ct.setSoLuong(dlg.getResultSoLuong());
                    ct.setTiLeGiamGia(dlg.getResultChietKhau());
                    ct.tinhThanhTien();
                    refreshTraHangTable(row);
                    recalculateFinancials();
                }
            }
        }
    }

    private void onLuuTam() {
        if (currentLayHangList.isEmpty() && currentTraHangList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có mặt hàng nào để lưu tạm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateRequiredSalesFields()) return;

        buildCurrentHoaDonObject();
        hoaDonDAO.saveTam(currentHoaDon);
        JOptionPane.showMessageDialog(this, "Đã lưu tạm hóa đơn " + currentHoaDon.getName() + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        resetForNewInvoice();
    }

    private void onMoLaiTam() {
        List<DonHang> tamList = hoaDonDAO.getDanhSachTam();
        if (tamList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có hóa đơn lưu tạm nào!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        DanhSachHoaDonLuuTamDialog dlg = new DanhSachHoaDonLuuTamDialog(parentWin, tamList);
        dlg.setVisible(true);

        if (dlg.isConfirmed() && dlg.getSelectedHoaDon() != null) {
            DonHang chosen = dlg.getSelectedHoaDon();
            loadHoaDonIntoUI(chosen);
            hoaDonDAO.removeTam(chosen.getId());
        }
    }

    private void onOpenDatHang() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        TimKiemKhachDatHangDialog dlg = new TimKiemKhachDatHangDialog(parentWin);
        dlg.setVisible(true);

        if (dlg.isConfirmed() && dlg.getSelectedOrder() != null) {
            KhachDatHang chosen = dlg.getSelectedOrder();
            if (chosen.getTenKhach() != null) {
                for (int k = 0; k < cbKhachHang.getItemCount(); k++) {
                    KhachHang kh = cbKhachHang.getItemAt(k);
                    if (kh != null && (chosen.getTenKhach().equalsIgnoreCase(kh.getName()) || (chosen.getdKhachHangId() != null && chosen.getdKhachHangId().equals(kh.getId())))) {
                        cbKhachHang.setSelectedIndex(k);
                        break;
                    }
                }
            }
            txtDiaChiGiaoHang.setText(chosen.getDiaChi() != null ? chosen.getDiaChi() : "");

            List<KhachDatHangChiTiet> details = khachDatHangDAO.getChiTietByKhachDatHangId(chosen.getId(), chosen.getName());
            currentLayHangList.clear();
            for (KhachDatHangChiTiet ct : details) {
                DonHangChiTiet dhct = new DonHangChiTiet();
                dhct.setId(UUID.randomUUID().toString());
                dhct.setMatHangId(ct.getdMatHangId());
                dhct.setMaHang(ct.getMaHang());
                dhct.setTenHang(ct.getTenHang());
                dhct.setDonViTinhId(ct.getdDonViTinhId());
                dhct.setTenDonViTinh(ct.getTenDonViTinh());
                dhct.setDonGia(ct.getDonGia());
                dhct.setSoLuong(ct.getSoLuong());
                dhct.setTiLeGiamGia(ct.getTiLeGiamGia());
                dhct.setGhiChu(ct.getNote());
                dhct.tinhThanhTien();
                currentLayHangList.add(dhct);
            }
            refreshLayHangTable();
            recalculateFinancials();
        }
    }

    private void onThanhToan() {
        if (currentLayHangList.isEmpty() && currentTraHangList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn chưa có mặt hàng nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateRequiredSalesFields()) return;

        buildCurrentHoaDonObject();

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        XacNhanThanhToanDialog dlg = new XacNhanThanhToanDialog(parentWin, currentHoaDon,
                systemConfig == null || systemConfig.isChoPhepInTamTinh());
        dlg.setVisible(true);

        XacNhanThanhToanDialog.ResultAction action = dlg.getResultAction();

        if (action == XacNhanThanhToanDialog.ResultAction.PRINT_TEMP) {
            ChonMauInDialog printDlg = new ChonMauInDialog(parentWin, currentHoaDon);
            printDlg.setVisible(true);
            return;
        }

        if (action == XacNhanThanhToanDialog.ResultAction.CONFIRM_AND_PRINT || action == XacNhanThanhToanDialog.ResultAction.CONFIRM_NO_PRINT) {
            double totalPaid = dlg.getTienKhachDua() + dlg.getTienATM();
            currentHoaDon.setTienThanhToan(totalPaid);
            currentHoaDon.setConLai(Math.max(0, currentHoaDon.getTongCong() - totalPaid));
            currentHoaDon.setStatus(1);

            boolean ok = hoaDonDAO.insertHoaDon(currentHoaDon);
            if (ok) {
                if (action == XacNhanThanhToanDialog.ResultAction.CONFIRM_AND_PRINT) {
                    ChonMauInDialog printDlg = new ChonMauInDialog(parentWin, currentHoaDon);
                    printDlg.setVisible(true);
                }
                JOptionPane.showMessageDialog(this, "Thanh toán thành công hóa đơn " + currentHoaDon.getName() + "!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                resetForNewInvoice();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu hóa đơn vào cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onThongKe() {
        JOptionPane.showMessageDialog(this, "Chuyển đến màn hình Thống kê bán hàng...", "Thống kê (F10)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onNavigateInvoice(int direction) {
        String curId = (currentHoaDon != null && currentHoaDon.getId() != null) ? currentHoaDon.getId() : "";
        DonHang target = hoaDonDAO.getHoaDonByOffset(curId, direction);
        if (target != null) {
            loadHoaDonIntoUI(target);
        } else {
            JOptionPane.showMessageDialog(this, direction < 0 ? "Đã ở hóa đơn đầu tiên!" : "Đã ở hóa đơn cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openChonCotHienThiDialog() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        ChonCotHienThiDialog dlg = new ChonCotHienThiDialog(parentWin, visibleLayHangColumns);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            visibleLayHangColumns = dlg.getResultVisibleColumns();
            applyColumnVisibility();
        }
    }

    private void applyColumnVisibility() {
        int curSel = tableLayHang != null ? tableLayHang.getSelectedRow() : -1;
        rebuildLayHangTableColumns();
        refreshLayHangTable(curSel);
    }

    private void resetForNewInvoice() {
        applySalesConfig();
        currentHoaDon = new DonHang();
        currentHoaDon.setId(UUID.randomUUID().toString());
        currentHoaDon.setName(hoaDonDAO.generateNextSoHoaDon(new Date()));
        currentHoaDon.setNgay(new Date());

        dpkNgay.setDate(new Date());
        txtSoHD.setText(currentHoaDon.getName());
        txtDienGiai.setText("Xuất bán hàng");
        txtDiaChiGiaoHang.setText("");
        txtGhiChu = new JTextField();
        int defaultDiscount = systemConfig != null ? systemConfig.getMacDinhGiamGiaPt() : 0;
        defaultDiscount = Math.max(0, Math.min(100, defaultDiscount));
        txtTiLeGiamGia.setText(defaultDiscount + " %");

        currentLayHangList.clear();
        currentTraHangList.clear();
        refreshLayHangTable();
        refreshTraHangTable();
        recalculateFinancials();

        if (cbKhachHang.getItemCount() > 0) cbKhachHang.setSelectedIndex(0);
        if (cbKhoXuat.getItemCount() > 0) cbKhoXuat.setSelectedIndex(0);
        if (cbNhanVien.getItemCount() > 0) cbNhanVien.setSelectedIndex(0);
    }

    private void loadHoaDonIntoUI(DonHang dh) {
        this.currentHoaDon = dh;
        dpkNgay.setDate(dh.getNgay() != null ? dh.getNgay() : new Date());
        txtSoHD.setText(dh.getName() != null ? dh.getName() : "");
        txtDienGiai.setText(dh.getDienGiai() != null ? dh.getDienGiai() : "Xuất bán hàng");
        txtDiaChiGiaoHang.setText(dh.getDiaChiGiaoHang() != null ? dh.getDiaChiGiaoHang() : "");
        txtTiLeGiamGia.setText(String.format("%.0f %%", dh.getTiLeGiamGia()));

        currentLayHangList.clear();
        if (dh.getChiTietLayHang() != null) {
            currentLayHangList.addAll(dh.getChiTietLayHang());
        }
        currentTraHangList.clear();
        if (dh.getChiTietTraHang() != null) {
            currentTraHangList.addAll(dh.getChiTietTraHang());
        }

        refreshLayHangTable();
        refreshTraHangTable();
        recalculateFinancials();
    }

    private void buildCurrentHoaDonObject() {
        if (currentHoaDon == null) {
            currentHoaDon = new DonHang();
            currentHoaDon.setId(UUID.randomUUID().toString());
        }
        currentHoaDon.setName(txtSoHD.getText().trim());
        currentHoaDon.setNgay(dpkNgay.getDate());
        currentHoaDon.setDienGiai(txtDienGiai.getText().trim());
        currentHoaDon.setDiaChiGiaoHang(txtDiaChiGiaoHang.getText().trim());

        KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
        if (kh != null) {
            currentHoaDon.setKhachHangId(kh.getId());
            currentHoaDon.setTenKhach(kh.getName());
        }

        KhoHang kho = (KhoHang) cbKhoXuat.getSelectedItem();
        if (kho != null) {
            currentHoaDon.setKhoXuatId(kho.getId());
        }

        NhanVien nv = (NhanVien) cbNhanVien.getSelectedItem();
        if (nv != null) {
            currentHoaDon.setNhanVienXuatId(nv.getId());
            currentHoaDon.setTenNhanVien(nv.getName());
        }

        currentHoaDon.setLoaiGia(currentLoaiGiaName);
        currentHoaDon.setChiTietLayHang(new ArrayList<>(currentLayHangList));
        currentHoaDon.setChiTietTraHang(new ArrayList<>(currentTraHangList));

        double th = 0.0;
        for (DonHangChiTiet ct : currentLayHangList) th += ct.getThanhTien();
        currentHoaDon.setTienHang(th);

        double tt = 0.0;
        for (DonHangChiTiet ct : currentTraHangList) tt += ct.getThanhTien();
        currentHoaDon.setTienDoiTra(tt);

        double tile = 0.0;
        try {
            tile = Double.parseDouble(txtTiLeGiamGia.getText().replace("%", "").trim().replace(",", "."));
        } catch (Exception ignored) {}
        tile = Math.max(0, Math.min(100, tile));
        currentHoaDon.setTiLeGiamGia(tile);
        currentHoaDon.setTienGiamGia(th * (tile / 100.0));
        currentHoaDon.setTongCong(SalesConfigSupport.roundMoney(
                Math.max(0, th - currentHoaDon.getTienGiamGia() - tt),
                systemConfig != null ? systemConfig.getLamTronTien() : null));
    }

    private void applySalesConfig() {
        if (systemConfig == null) systemConfig = systemConfigDAO.loadConfig();
        boolean allowDiscount = isDiscountInputAllowed();
        txtTiLeGiamGia.setEditable(allowDiscount);
        txtTiLeGiamGia.setEnabled(allowDiscount);
        txtTiLeGiamGia.setToolTipText(allowDiscount
                ? "Nhập giảm giá cho toàn hóa đơn"
                : "Đã tắt nhập giảm giá trong Cấu hình toàn bộ hệ thống");

        boolean allowDateChange = systemConfig == null || systemConfig.isChoPhepThayDoiNgayTrenHoaDon();
        dpkNgay.setEnabled(allowDateChange);
        dpkNgay.setToolTipText(allowDateChange
                ? "Ngày hóa đơn"
                : "Đã khóa thay đổi ngày trong Cấu hình toàn bộ hệ thống");
    }

    public void refreshSalesConfiguration() {
        systemConfig = systemConfigDAO.loadConfig();
        applySalesConfig();
    }

    private boolean isDiscountInputAllowed() {
        return systemConfig == null || systemConfig.isChoPhepNhapGiamGia();
    }

    private boolean validateRequiredSalesFields() {
        if (systemConfig == null) return true;

        KhachHang customer = (KhachHang) cbKhachHang.getSelectedItem();
        if (systemConfig.isBatBuocNhapKhachHang()
                && (customer == null || customer.getId() == null || customer.getId().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trước khi lưu hóa đơn!",
                    "Thiếu khách hàng", JOptionPane.WARNING_MESSAGE);
            cbKhachHang.requestFocusInWindow();
            return false;
        }

        NhanVien employee = (NhanVien) cbNhanVien.getSelectedItem();
        if (systemConfig.isBatBuocNhapNhanVienBanHang()
                && (employee == null || employee.getId() == null || employee.getId().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên bán hàng trước khi lưu hóa đơn!",
                    "Thiếu nhân viên bán hàng", JOptionPane.WARNING_MESSAGE);
            cbNhanVien.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private void selectCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) return;
        for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
            KhachHang customer = cbKhachHang.getItemAt(i);
            if (customer != null && customerId.equals(customer.getId())) {
                cbKhachHang.setSelectedIndex(i);
                return;
            }
        }
    }

    private static final class ReturnInvoiceOption {
        private final DonHang invoice;

        private ReturnInvoiceOption(DonHang invoice) {
            this.invoice = invoice;
        }

        @Override
        public String toString() {
            String customer = invoice.getTenKhach() != null ? invoice.getTenKhach() : "Khách lẻ";
            return invoice.getName() + " - " + DateUtil.formatDate(invoice.getNgay()) + " - " + customer;
        }
    }

    private static final class ReturnItemOption {
        private final DonHangChiTiet item;

        private ReturnItemOption(DonHangChiTiet item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return item.getMaHang() + " - " + item.getTenHang()
                    + " (SL đã bán: " + item.getSoLuong() + ")";
        }
    }
}
