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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhieuKiemKeDialog extends JDialog {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private DonHang currentDonHang;
    private final Runnable onDataSaved;

    // Master form controls
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private JComboBox<KhoHang> cbKhoHang;
    private JComboBox<NhanVien> cbNhanVien;
    private JTextField txtDienGiai;
    private JTextField txtGhiChu;

    // Action / Quick Add controls
    private JTextField txtTimF3;
    private JSpinner spnSoLuongThucTe;
    private JButton btnThemQuick;
    private JCheckBox chkKiemTrucTiepKGD;
    private JButton btnDuLieuMayKGD;
    private JButton btnMatHangChuaKiemKe;

    // Catalog Table (Left)
    private JTable tableCatalog;
    private DefaultTableModel catalogModel;
    private List<MatHang> allCatalogItems = new ArrayList<>();
    private List<MatHang> filteredCatalogItems = new ArrayList<>();

    // Detail Table (Right)
    private JTable tableChiTiet;
    private DefaultTableModel chiTietModel;
    private final List<DonHangChiTiet> lineItems = new ArrayList<>();
    private JTextField txtFilterDetail;

    private final DecimalFormat df = new DecimalFormat("#,##0.##");
    private final DecimalFormat dfStt = new DecimalFormat("0000");

    public PhieuKiemKeDialog(Window parent, Runnable onDataSaved) {
        this(parent, null, onDataSaved);
    }

    public PhieuKiemKeDialog(Window parent, DonHang donHang, Runnable onDataSaved) {
        super(parent, donHang == null ? "PHIẾU KIỂM KÊ - THÊM MỚI" : "PHIẾU KIỂM KÊ - SỬA", ModalityType.APPLICATION_MODAL);
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
        setSize(1220, 720);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(235, 242, 250));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 0));
        pnlTop.setOpaque(false);

        pnlTop.add(createHeaderBanner(), BorderLayout.NORTH);
        pnlTop.add(createTopActionBar(), BorderLayout.CENTER);
        pnlTop.add(createFormPanel(), BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);
        add(createCenterSplitPane(), BorderLayout.CENTER);
        add(createBottomButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderBanner() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(214, 232, 250));
        pnl.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel lblTitle = new JLabel("Phiếu kiểm kê");
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

    private JPanel createFormPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(6, 8, 4, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Ngày, Số phiếu, Kho hàng, Nhân viên
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnl.add(new JLabel("Ngày"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.18;
        dpkNgay = new DatePickerField();
        dpkNgay.setDate(new Date());
        pnl.add(dpkNgay, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        pnl.add(new JLabel("Số phiếu"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.22;
        txtSoPhieu = new JTextField();
        txtSoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(txtSoPhieu, gbc);

        gbc.gridx = 4; gbc.weightx = 0.0;
        pnl.add(new JLabel("Kho hàng"), gbc);

        gbc.gridx = 5; gbc.weightx = 0.3;
        cbKhoHang = new JComboBox<>();
        cbKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoHang.addActionListener(e -> onWarehouseChanged());
        pnl.add(cbKhoHang, gbc);

        gbc.gridx = 6; gbc.weightx = 0.0;
        pnl.add(new JLabel("Nhân viên"), gbc);

        gbc.gridx = 7; gbc.weightx = 0.3;
        cbNhanVien = new JComboBox<>();
        cbNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(cbNhanVien, gbc);

        // Row 2: Diễn giải, Ghi chú
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnl.add(new JLabel("Diễn giải"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 0.4;
        txtDienGiai = new JTextField("Kiểm kê");
        txtDienGiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(txtDienGiai, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 4; gbc.weightx = 0.0;
        pnl.add(new JLabel("Ghi chú"), gbc);

        gbc.gridx = 5; gbc.gridwidth = 3; gbc.weightx = 0.6;
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(txtGhiChu, gbc);
        gbc.gridwidth = 1;

        // Row 3: Quick Action Bar (Tìm F3, Số lượng thực tế, Thêm, Máy không dây, Mặt hàng chưa kiểm kê)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnl.add(new JLabel("Tìm (F3):"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 7; gbc.weightx = 1.0;
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

        spnSoLuongThucTe = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 999999.0, 1.0));
        spnSoLuongThucTe.setPreferredSize(new Dimension(50, 24));

        btnThemQuick = new JButton("Thêm", IconHelper.getIcon("plus.png", 14, 14));
        btnThemQuick.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThemQuick.addActionListener(e -> addSelectedCatalogItem());

        chkKiemTrucTiepKGD = new JCheckBox("Kiểm trực tiếp bằng máy không dây", false);
        chkKiemTrucTiepKGD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKiemTrucTiepKGD.setOpaque(false);

        btnDuLieuMayKGD = createToolButton("Dữ liệu từ máy không dây", "transmit.png");
        btnDuLieuMayKGD.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng máy kiểm kê không dây đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        btnMatHangChuaKiemKe = createToolButton("Mặt hàng chưa kiểm kê", "table_tab.png");
        btnMatHangChuaKiemKe.addActionListener(e -> loadMatHangChuaKiemKe());

        pnlQuick.add(txtTimF3);
        pnlQuick.add(new JLabel("Số lượng thực tế:"));
        pnlQuick.add(spnSoLuongThucTe);
        pnlQuick.add(btnThemQuick);
        pnlQuick.add(new JSeparator(SwingConstants.VERTICAL));
        pnlQuick.add(chkKiemTrucTiepKGD);
        pnlQuick.add(btnDuLieuMayKGD);
        pnlQuick.add(btnMatHangChuaKiemKe);

        pnl.add(pnlQuick, gbc);
        gbc.gridwidth = 1;

        return pnl;
    }

    private JSplitPane createCenterSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(360);
        split.setResizeWeight(0.3);
        split.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Left Panel: Catalog Table
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.setBackground(Color.WHITE);
        pnlLeft.setBorder(new LineBorder(new Color(190, 210, 235), 1));

        // Columns matching Image 2: STT/Mã hàng, Tên hàng, ĐVT
        String[] catCols = {"Mã hàng", "Tên hàng", "ĐVT"};
        catalogModel = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCatalog = new JTable(catalogModel);
        styleTable(tableCatalog);
        tableCatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCatalog.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableCatalog.getColumnModel().getColumn(1).setPreferredWidth(180);
        tableCatalog.getColumnModel().getColumn(2).setPreferredWidth(60);

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

        // Right Panel: Line Items Detail Table
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(new LineBorder(new Color(190, 210, 235), 1));

        // Toolbar: Nhập từ máy kiểm kho, Xóa dòng (F8), Nhập excel, Xuất excel, Lọc
        JPanel pnlDetailBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnlDetailBar.setBackground(new Color(240, 245, 252));

        JButton btnNhapMayKK = createToolButton("Nhập từ máy kiểm kho", "transmit.png");
        btnNhapMayKK.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng máy kiểm kho đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        JButton btnDelRow = createToolButton("Xóa dòng (F8)", "cross.png");
        btnDelRow.addActionListener(e -> deleteSelectedLineItem());

        JButton btnImportExcel = createToolButton("Nhập từ excel", "page_excel.png");
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng nhập từ Excel đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        JButton btnExportExcel = createToolButton("Xuất excel", "export-excel.png");
        btnExportExcel.addActionListener(e -> ExcelExporter.exportTable(tableChiTiet, "Chi_Tiet_Kiem_Ke", "Kiểm kê", this));

        pnlDetailBar.add(btnNhapMayKK);
        pnlDetailBar.add(btnDelRow);
        pnlDetailBar.add(btnImportExcel);
        pnlDetailBar.add(btnExportExcel);

        pnlDetailBar.add(new JLabel("   Lọc:"));
        txtFilterDetail = new JTextField(10);
        txtFilterDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlDetailBar.add(txtFilterDetail);

        pnlRight.add(pnlDetailBar, BorderLayout.NORTH);

        // Columns matching Image 2: [STT], Mã hàng, Tên hàng, ĐVT, Số lượng hệ thống, Số lượng thực tế, Điều chỉnh xuất, Điều chỉnh nhập
        String[] detailCols = {
                "", "Mã hàng", "Tên hàng", "ĐVT",
                "Số lượng hệ thống", "Số lượng thực tế", "Điều chỉnh xuất", "Điều chỉnh nhập"
        };
        chiTietModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Cho phép sửa trực tiếp Số lượng thực tế (cột 5)
                return c == 5;
            }
        };
        tableChiTiet = new JTable(chiTietModel);
        styleTable(tableChiTiet);
        tableChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {40, 90, 220, 70, 110, 110, 100, 100};
        for (int i = 0; i < widths.length && i < tableChiTiet.getColumnCount(); i++) {
            tableChiTiet.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // STT renderer with indicator ▶ 1
        tableChiTiet.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    setText("▶ " + (row + 1));
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else {
                    setText(String.valueOf(row + 1));
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                }
                return this;
            }
        });

        // Numeric alignment
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableChiTiet.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        // Listener for editing Số lượng thực tế in cell
        chiTietModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row >= 0 && col == 5) {
                onActualQuantityEdited(row);
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
        cbKhoHang.removeAllItems();
        cbKhoHang.addItem(new KhoHang("", "-- Chọn kho kiểm kê --"));
        List<KhoHang> khoList = khoHangDAO.getAll();
        for (KhoHang k : khoList) {
            cbKhoHang.addItem(k);
        }

        cbNhanVien.removeAllItems();
        cbNhanVien.addItem(new NhanVien("", "-- Chọn nhân viên --"));
        List<NhanVien> nvList = nhanVienDAO.getAll();
        for (NhanVien nv : nvList) {
            cbNhanVien.addItem(nv);
        }
    }

    private void loadCatalogData() {
        allCatalogItems = matHangDAO.getAll();
        if (allCatalogItems == null) allCatalogItems = new ArrayList<>();
        filteredCatalogItems.clear();
        filteredCatalogItems.addAll(allCatalogItems);
        populateCatalogTable();
    }

    private void populateCatalogTable() {
        catalogModel.setRowCount(0);
        for (int i = 0; i < filteredCatalogItems.size(); i++) {
            MatHang m = filteredCatalogItems.get(i);
            catalogModel.addRow(new Object[]{
                    dfStt.format(i + 1) + " " + (m.getCode() != null ? m.getCode() : ""),
                    m.getName() != null ? m.getName() : "",
                    m.getTenDonViTinh() != null ? m.getTenDonViTinh() : ""
            });
        }
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

    // =========================================================================
    // USER CONSTRAINT: Khi tạo mới bắt buộc chọn kho trước. Khi sửa thì khóa kho
    // =========================================================================
    public void resetForNew() {
        currentDonHang = null;
        setTitle("PHIẾU KIỂM KÊ - THÊM MỚI");
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(donHangDAO.generateNextSoPhieuKiemKe(new Date()));
        txtDienGiai.setText("Kiểm kê");
        txtGhiChu.setText("");

        // Cho phép chọn kho khi tạo mới
        cbKhoHang.setEnabled(true);
        if (cbKhoHang.getItemCount() > 1) {
            cbKhoHang.setSelectedIndex(1);
        } else {
            cbKhoHang.setSelectedIndex(0);
        }

        if (cbNhanVien.getItemCount() > 0) cbNhanVien.setSelectedIndex(0);

        lineItems.clear();
        refreshDetailTable();
    }

    private void populateExistingData() {
        if (currentDonHang == null) return;
        setTitle("PHIẾU KIỂM KÊ - SỬA [" + currentDonHang.getName() + "]");
        dpkNgay.setDate(currentDonHang.getNgay());
        txtSoPhieu.setText(currentDonHang.getName());
        txtDienGiai.setText(currentDonHang.getDienGiai() != null ? currentDonHang.getDienGiai() : "Kiểm kê");
        txtGhiChu.setText(currentDonHang.getGhiChu() != null ? currentDonHang.getGhiChu() : "");

        // Chọn kho tương ứng
        String khoId = currentDonHang.getKhoXuatId() != null ? currentDonHang.getKhoXuatId() : currentDonHang.getKhoNhapId();
        if (khoId != null) {
            for (int i = 0; i < cbKhoHang.getItemCount(); i++) {
                KhoHang k = cbKhoHang.getItemAt(i);
                if (khoId.equals(k.getId())) {
                    cbKhoHang.setSelectedIndex(i);
                    break;
                }
            }
        }

        // USER CONSTRAINT: Khi chỉnh sửa thì field kho kiểm kê KHÔNG cho editable
        cbKhoHang.setEnabled(false);

        // Chọn nhân viên
        String nvId = currentDonHang.getNhanVienXuatId() != null ? currentDonHang.getNhanVienXuatId() : currentDonHang.getNhanVienNhapId();
        if (nvId != null) {
            for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                NhanVien nv = cbNhanVien.getItemAt(i);
                if (nvId.equals(nv.getId())) {
                    cbNhanVien.setSelectedIndex(i);
                    break;
                }
            }
        }

        lineItems.clear();
        List<DonHangChiTiet> details = currentDonHang.getChiTietLayHang();
        if (details == null || details.isEmpty()) {
            details = donHangDAO.getChiTietPhieuKiemKe(currentDonHang.getId());
        }
        lineItems.addAll(details);
        refreshDetailTable();
    }

    private void onWarehouseChanged() {
        KhoHang selKho = (KhoHang) cbKhoHang.getSelectedItem();
        if (selKho != null && selKho.getId() != null && !selKho.getId().isEmpty()) {
            // Khi đổi kho, cập nhật lại SL hệ thống cho các mặt hàng đang có trong bảng chi tiết
            for (DonHangChiTiet ct : lineItems) {
                double slHt = donHangDAO.getTonKhoTheoKho(selKho.getId(), ct.getMatHangId());
                ct.setSlHeThong(slHt);
                ct.tinhChenhLechKiemKe();
            }
            refreshDetailTable();
        }
    }

    private void addSelectedCatalogItem() {
        // USER CONSTRAINT: Bắt buộc chọn kho kiểm kê trước
        KhoHang selKho = (KhoHang) cbKhoHang.getSelectedItem();
        if (selKho == null || selKho.getId() == null || selKho.getId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Kho kiểm kê trước khi thêm mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            cbKhoHang.requestFocus();
            return;
        }

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
        double actualQty = ((Number) spnSoLuongThucTe.getValue()).doubleValue();

        boolean found = false;
        for (DonHangChiTiet existing : lineItems) {
            if (existing.getMatHangId() != null && existing.getMatHangId().equals(m.getId())) {
                existing.setSlThucTe(actualQty);
                existing.tinhChenhLechKiemKe();
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
            ct.setDonGia(m.getGiaVon() > 0 ? m.getGiaVon() : m.getGiaNhap());

            // Lấy SL hệ thống từ CSDL cho kho đã chọn
            double slHeThong = donHangDAO.getTonKhoTheoKho(selKho.getId(), m.getId());
            ct.setSlHeThong(slHeThong);
            ct.setSlThucTe(actualQty);
            ct.tinhChenhLechKiemKe();

            lineItems.add(ct);
        }

        refreshDetailTable();

        int lastIdx = lineItems.size() - 1;
        if (lastIdx >= 0) {
            tableChiTiet.setRowSelectionInterval(lastIdx, lastIdx);
            tableChiTiet.scrollRectToVisible(tableChiTiet.getCellRect(lastIdx, 0, true));
        }

        txtTimF3.setText("");
        txtTimF3.requestFocus();
    }

    private void loadMatHangChuaKiemKe() {
        KhoHang selKho = (KhoHang) cbKhoHang.getSelectedItem();
        if (selKho == null || selKho.getId() == null || selKho.getId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Kho kiểm kê trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            cbKhoHang.requestFocus();
            return;
        }

        int addedCount = 0;
        for (MatHang m : allCatalogItems) {
            boolean alreadyInList = false;
            for (DonHangChiTiet ct : lineItems) {
                if (ct.getMatHangId() != null && ct.getMatHangId().equals(m.getId())) {
                    alreadyInList = true;
                    break;
                }
            }
            if (!alreadyInList) {
                double slHeThong = donHangDAO.getTonKhoTheoKho(selKho.getId(), m.getId());
                // Thêm mặt hàng chưa kiểm kê với SL thực tế mặc định bằng SL hệ thống
                DonHangChiTiet ct = new DonHangChiTiet();
                ct.setMatHangId(m.getId());
                ct.setMaHang(m.getCode());
                ct.setTenHang(m.getName());
                ct.setDonViTinhId(m.getDonViTinhId());
                ct.setTenDonViTinh(m.getTenDonViTinh());
                ct.setDonGia(m.getGiaVon() > 0 ? m.getGiaVon() : m.getGiaNhap());
                ct.setSlHeThong(slHeThong);
                ct.setSlThucTe(slHeThong);
                ct.tinhChenhLechKiemKe();
                lineItems.add(ct);
                addedCount++;
            }
        }

        refreshDetailTable();
        JOptionPane.showMessageDialog(this, "Đã nạp " + addedCount + " mặt hàng chưa kiểm kê vào danh sách!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedLineItem() {
        int row = tableChiTiet.getSelectedRow();
        if (row >= 0 && row < lineItems.size()) {
            lineItems.remove(row);
            refreshDetailTable();
        }
    }

    private void onActualQuantityEdited(int row) {
        if (row < 0 || row >= lineItems.size()) return;
        DonHangChiTiet ct = lineItems.get(row);
        Object val = chiTietModel.getValueAt(row, 5);
        double actual = parseDouble(val, ct.getSlThucTe());
        ct.setSlThucTe(actual);
        ct.tinhChenhLechKiemKe();

        chiTietModel.setValueAt(df.format(ct.getSlXuat()), row, 6);
        chiTietModel.setValueAt(df.format(ct.getSlNhap()), row, 7);
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
                    df.format(ct.getSlHeThong()),
                    df.format(ct.getSlThucTe()),
                    df.format(ct.getSlXuat()),
                    df.format(ct.getSlNhap())
            });
        }
    }

    private boolean saveOrder(boolean createNewAfter, boolean closeAfter) {
        // USER CONSTRAINT 1: Bắt buộc chọn kho kiểm kê
        KhoHang selKho = (KhoHang) cbKhoHang.getSelectedItem();
        if (selKho == null || selKho.getId() == null || selKho.getId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bắt buộc chọn Kho kiểm kê trước khi lưu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cbKhoHang.requestFocus();
            return false;
        }

        if (lineItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu kiểm kê phải có ít nhất một mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTimF3.requestFocus();
            return false;
        }

        boolean isNew = (currentDonHang == null || currentDonHang.getId() == null);
        DonHang dh = isNew ? new DonHang() : currentDonHang;

        dh.setName(txtSoPhieu.getText().trim());
        dh.setNgay(dpkNgay.getDate() != null ? dpkNgay.getDate() : new Date());
        dh.setDienGiai(txtDienGiai.getText().trim());
        dh.setGhiChu(txtGhiChu.getText().trim());

        dh.setKhoXuatId(selKho.getId());
        dh.setKhoNhapId(selKho.getId());
        dh.setTenKhoXuat(selKho.getName());
        dh.setTenKhoNhap(selKho.getName());

        NhanVien selNv = (NhanVien) cbNhanVien.getSelectedItem();
        if (selNv != null && selNv.getId() != null && !selNv.getId().isEmpty()) {
            dh.setNhanVienXuatId(selNv.getId());
            dh.setNhanVienNhapId(selNv.getId());
            dh.setTenNhanVien(selNv.getName());
        } else {
            dh.setNhanVienXuatId(null);
            dh.setNhanVienNhapId(null);
            dh.setTenNhanVien(null);
        }

        dh.setLoai(4); // 4: Kiểm kê kho
        dh.setChiTietLayHang(new ArrayList<>(lineItems));

        boolean success;
        if (isNew) {
            success = donHangDAO.insertPhieuKiemKe(dh);
        } else {
            success = donHangDAO.updatePhieuKiemKe(dh);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu phiếu kiểm kê thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();

            if (createNewAfter) {
                resetForNew();
            } else if (closeAfter) {
                dispose();
            } else {
                currentDonHang = dh;
                setTitle("PHIẾU KIỂM KÊ - SỬA [" + dh.getName() + "]");
                cbKhoHang.setEnabled(false); // Khóa trường kho kiểm kê khi đã lưu
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu phiếu kiểm kê. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveAndPrint(boolean directPrint) {
        if (saveOrder(false, false)) {
            String title = directPrint ? "In phiếu kiểm kê" : "Xem trước phiếu kiểm kê";
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================================\n");
            sb.append("                          PHIẾU KIỂM KÊ                                 \n");
            sb.append("========================================================================\n");
            sb.append("Số phiếu: ").append(txtSoPhieu.getText()).append("    Ngày: ").append(dpkNgay.getText()).append("\n");
            sb.append("Kho hàng: ").append(cbKhoHang.getSelectedItem() != null ? cbKhoHang.getSelectedItem() : "").append("\n");
            sb.append("Nhân viên: ").append(cbNhanVien.getSelectedItem() != null ? cbNhanVien.getSelectedItem() : "").append("\n");
            sb.append("Diễn giải: ").append(txtDienGiai.getText()).append("\n");
            sb.append("------------------------------------------------------------------------\n");
            sb.append(String.format("%-4s %-12s %-24s %-6s %-10s %-10s %-8s %-8s\n", "STT", "Mã hàng", "Tên hàng", "ĐVT", "SL Hệ thống", "SL Thực tế", "Đ/C Xuất", "Đ/C Nhập"));
            for (int i = 0; i < lineItems.size(); i++) {
                DonHangChiTiet ct = lineItems.get(i);
                sb.append(String.format("%-4d %-12s %-24s %-6s %-10.1f %-10.1f %-8.1f %-8.1f\n",
                        (i + 1),
                        ct.getMaHang() != null ? ct.getMaHang() : "",
                        ct.getTenHang() != null ? (ct.getTenHang().length() > 24 ? ct.getTenHang().substring(0, 22) + ".." : ct.getTenHang()) : "",
                        ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                        ct.getSlHeThong(),
                        ct.getSlThucTe(),
                        ct.getSlXuat(),
                        ct.getSlNhap()
                ));
            }
            sb.append("========================================================================\n");

            JTextArea ta = new JTextArea(sb.toString(), 22, 60);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void copyCurrentOrder() {
        if (currentDonHang == null) return;
        currentDonHang = null;
        setTitle("PHIẾU KIỂM KÊ - THÊM MỚI (BẢN SAO)");
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(donHangDAO.generateNextSoPhieuKiemKe(new Date()));
        cbKhoHang.setEnabled(true);
        JOptionPane.showMessageDialog(this, "Đã sao chép phiếu kiểm kê sang số phiếu mới. Vui lòng kiểm tra và lưu lại!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteCurrentOrder() {
        if (currentDonHang == null || currentDonHang.getId() == null) return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu kiểm kê [" + currentDonHang.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (donHangDAO.deletePhieuKiemKe(currentDonHang.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phiếu kiểm kê thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                if (onDataSaved != null) onDataSaved.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa phiếu kiểm kê này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void navigateRecord(int offset) {
        if (currentDonHang == null || currentDonHang.getId() == null) return;
        DonHang target = donHangDAO.getPhieuKiemKeOffset(currentDonHang.getId(), offset);
        if (target != null) {
            currentDonHang = target;
            populateExistingData();
        } else {
            JOptionPane.showMessageDialog(this, offset < 0 ? "Đã ở phiếu kiểm kê đầu tiên!" : "Đã ở phiếu kiểm kê cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
