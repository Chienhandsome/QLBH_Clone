package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.DotKhuyenMaiDAO;
import kazuko.ex.dao.LoaiHinhKhuyenMaiDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.DotKhuyenMai;
import kazuko.ex.model.danhmuc.LoaiHinhKhuyenMai;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DotKhuyenMaiEditDialog extends JDialog {
    private final DotKhuyenMaiDAO dotKhuyenMaiDAO = new DotKhuyenMaiDAO();
    private final LoaiHinhKhuyenMaiDAO loaiHinhKhuyenMaiDAO = new LoaiHinhKhuyenMaiDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private DotKhuyenMai currentDotKM;
    private final Runnable onSaveCallback;
    private List<DotKhuyenMai> allPromotions;
    private int currentIndex = -1;

    // Components
    private JTextField txtName;
    private JComboBox<LoaiHinhKhuyenMai> cbLoaiHinhKM;
    private DatePickerField dpTuNgay;
    private DatePickerField dpDenNgay;
    private JSpinner spTuGio;
    private JSpinner spDenGio;
    private JTextField txtTiLeGiamGia;
    private JCheckBox chkNgungApDung;
    private JTextField txtNote;

    // Cửa hàng áp dụng
    private JTable tableCuaHang;
    private DefaultTableModel modelCuaHang;
    private List<CuaHang> allStores = new ArrayList<>();

    // Bảng chi tiết nhóm hàng (Giảm giá theo nhóm)
    private JTable tableNhomChiTiet;
    private DefaultTableModel modelNhomChiTiet;
    private List<NhomMatHang> allItemGroups = new ArrayList<>();

    // Components cho "Giảm giá theo sản phẩm"
    private JTree treeNhomHangSP;
    private DefaultTreeModel treeModelSP;
    private JTable tableSanPhamNguon;
    private DefaultTableModel modelSanPhamNguon;
    private JTextField txtSearchSanPham;
    private List<MatHang> allProducts = new ArrayList<>();
    private List<MatHang> filteredSourceProducts = new ArrayList<>();

    private JTable tableSanPhamChiTiet;
    private DefaultTableModel modelSanPhamChiTiet;
    private JTextField txtQuickRateSP;
    private JButton btnApplyQuickRateSP;
    private String currentSelectedNhomSP = "Tất cả";
    private String currentSelectedNhomIdSP = null;
    private static final DecimalFormat DF_CURRENCY = new DecimalFormat("#,##0");

    private static final Color COLOR_HEADER_BG = new Color(255, 254, 230);
    private static final Color COLOR_HEADER_BORDER = new Color(225, 220, 185);
    private static final Color COLOR_TOOLBAR_BG = new Color(210, 225, 244);
    private static final Color COLOR_TOOLBAR_BORDER = new Color(175, 198, 226);
    private static final Color COLOR_FORM_BG = new Color(190, 211, 236);
    private static final Color COLOR_TEXT_DARK = new Color(20, 45, 85);
    private static final Color COLOR_REQUIRED_FIELD = new Color(255, 255, 204);

    public DotKhuyenMaiEditDialog(Component parent, DotKhuyenMai dotKM, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), "", ModalityType.APPLICATION_MODAL);
        this.currentDotKM = dotKM != null ? dotKM : new DotKhuyenMai();
        this.onSaveCallback = onSaveCallback;

        this.allPromotions = dotKhuyenMaiDAO.getAll();
        findCurrentIndex();

        initUI();
        loadFormData();
        setupShortcuts();
        pack();
        setLocationRelativeTo(parent);
    }

    private boolean isSanPhamPromotion() {
        String typeName = currentDotKM != null ? currentDotKM.getTenLoaiHinhKhuyenMai() : "";
        if ((typeName == null || typeName.isEmpty()) && currentDotKM != null && currentDotKM.getdLoaiHinhKhuyenMaiId() != null) {
            LoaiHinhKhuyenMai l = loaiHinhKhuyenMaiDAO.findById(currentDotKM.getdLoaiHinhKhuyenMaiId());
            if (l != null) typeName = l.getName();
        }
        return typeName != null && (typeName.toLowerCase().contains("sản phẩm") || typeName.toLowerCase().contains("san pham"));
    }

    private boolean isNhomPromotion() {
        String typeName = currentDotKM != null ? currentDotKM.getTenLoaiHinhKhuyenMai() : "";
        if ((typeName == null || typeName.isEmpty()) && currentDotKM != null && currentDotKM.getdLoaiHinhKhuyenMaiId() != null) {
            LoaiHinhKhuyenMai l = loaiHinhKhuyenMaiDAO.findById(currentDotKM.getdLoaiHinhKhuyenMaiId());
            if (l != null) typeName = l.getName();
        }
        return typeName != null && (typeName.toLowerCase().contains("nhóm") || typeName.toLowerCase().contains("nhom"));
    }

    private void findCurrentIndex() {
        if (currentDotKM != null && currentDotKM.getId() != null) {
            for (int i = 0; i < allPromotions.size(); i++) {
                if (currentDotKM.getId().equals(allPromotions.get(i).getId())) {
                    currentIndex = i;
                    break;
                }
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        boolean isSanPham = isSanPhamPromotion();
        boolean isNhom = isNhomPromotion();
        if (isSanPham) {
            setPreferredSize(new Dimension(1020, 680));
            setResizable(true);
            setMinimumSize(new Dimension(850, 580));
        } else if (isNhom) {
            setPreferredSize(new Dimension(780, 590));
            setResizable(false);
        } else {
            setPreferredSize(new Dimension(540, 570));
            setResizable(false);
        }

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_HEADER_BORDER));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 52));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftHeader.setOpaque(false);

        JLabel lblIcon = new JLabel(IconHelper.getIcon("1282529873_objects.png", 34, 34));
        JLabel lblHeaderTitle = new JLabel("Đợt khuyến mại");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(COLOR_TEXT_DARK);

        leftHeader.add(lblIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        JLabel lblShortcutsHint = new JLabel("(Ctrl+S: Lưu&Mới, Ctrl+L: Lưu, Ctrl+N: Mới, Ctrl+P: In, ESC: Thoát)  ");
        lblShortcutsHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblShortcutsHint.setForeground(new Color(60, 80, 110));
        headerPanel.add(lblShortcutsHint, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Middle Container: Ribbon Toolbar + Form Body
        JPanel centerContainer = new JPanel(new BorderLayout());

        // 2.1 Sub Toolbar
        JPanel ribbonBar = new JPanel(new BorderLayout());
        ribbonBar.setBackground(COLOR_TOOLBAR_BG);
        ribbonBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_TOOLBAR_BORDER));

        JPanel leftRibbon = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        leftRibbon.setOpaque(false);

        JButton btnShortcuts = createRibbonButton("Phím tắt ▾", "btnConfig.Icon.png");
        JButton btnPrev = createRibbonButton("Trước (F10)", "arrow-180-medium.png");
        JButton btnNext = createRibbonButton("Sau (F11)", "arrow-000-medium.png");
        JButton btnCreateNew = createRibbonButton("Tạo mới", "btnNew.Image.png");
        JButton btnCopy = createRibbonButton("Sao chép", "Copy.png");

        btnShortcuts.addActionListener(e -> showShortcutMenu(btnShortcuts));
        btnPrev.addActionListener(e -> navigatePrevious());
        btnNext.addActionListener(e -> navigateNext());
        btnCreateNew.addActionListener(e -> onNewPromotion());
        btnCopy.addActionListener(e -> onCopyPromotion());

        leftRibbon.add(btnShortcuts);
        leftRibbon.add(btnPrev);
        leftRibbon.add(btnNext);
        leftRibbon.add(btnCreateNew);
        leftRibbon.add(btnCopy);

        JPanel rightRibbon = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 3));
        rightRibbon.setOpaque(false);
        JButton btnDelete = createRibbonButton("Xóa", "cross.png");
        btnDelete.addActionListener(e -> onDeletePromotion());
        rightRibbon.add(btnDelete);

        ribbonBar.add(leftRibbon, BorderLayout.WEST);
        ribbonBar.add(rightRibbon, BorderLayout.EAST);
        centerContainer.add(ribbonBar, BorderLayout.NORTH);

        if (isSanPham) {
            buildSanPhamPromotionForm(centerContainer);
        } else if (isNhom) {
            buildNhomPromotionForm(centerContainer);
        } else {
            buildGeneralPromotionForm(centerContainer);
        }

        add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footerPanel.setBackground(new Color(235, 242, 252));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        JButton btnSave = createActionButton("Lưu", "Save1.png");
        JButton btnSaveNew = createActionButton("Lưu & Mới", null);
        JButton btnSaveExit = createActionButton("Lưu & thoát", null);
        JButton btnExit = createActionButton("Thoát", null);

        btnSave.addActionListener(e -> onSave(false, false));
        btnSaveNew.addActionListener(e -> onSave(true, false));
        btnSaveExit.addActionListener(e -> onSave(false, true));
        btnExit.addActionListener(e -> dispose());

        footerPanel.add(btnSave);
        footerPanel.add(btnSaveNew);
        footerPanel.add(btnSaveExit);
        footerPanel.add(btnExit);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void buildSanPhamPromotionForm(JPanel centerContainer) {
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setPreferredSize(new Dimension(1020, 160));
        formPanel.setBorder(new EmptyBorder(6, 12, 6, 12));

        int rowH = 24;
        int gap = 5;
        int y = 8;

        // ===== CỘT TRÁI =====
        JLabel lblName = new JLabel("Tên đợt khuyến mại");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(12, y, 130, rowH);
        formPanel.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBackground(COLOR_REQUIRED_FIELD);
        txtName.setBounds(145, y, 240, rowH);
        formPanel.add(txtName);

        y += rowH + gap;
        JLabel lblLoaiHinh = new JLabel("Loại hình khuyến mại");
        lblLoaiHinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoaiHinh.setBounds(12, y, 130, rowH);
        formPanel.add(lblLoaiHinh);

        cbLoaiHinhKM = new JComboBox<>();
        cbLoaiHinhKM.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLoaiHinhKM.setBounds(145, y, 240, rowH);
        populateLoaiHinhKMComboBox();
        formPanel.add(cbLoaiHinhKM);

        y += rowH + gap;
        JLabel lblTuNgay = new JLabel("Từ ngày");
        lblTuNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTuNgay.setBounds(12, y, 60, rowH);
        formPanel.add(lblTuNgay);

        dpTuNgay = new DatePickerField();
        dpTuNgay.setBounds(75, y, 110, rowH);
        formPanel.add(dpTuNgay);

        JLabel lblDenNgay = new JLabel("Đến ngày");
        lblDenNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDenNgay.setBounds(195, y, 60, rowH);
        formPanel.add(lblDenNgay);

        dpDenNgay = new DatePickerField();
        dpDenNgay.setBounds(265, y, 120, rowH);
        formPanel.add(dpDenNgay);

        y += rowH + gap;
        JLabel lblTuGio = new JLabel("Từ giờ");
        lblTuGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTuGio.setBounds(12, y, 60, rowH);
        formPanel.add(lblTuGio);

        spTuGio = new JSpinner(new SpinnerDateModel());
        spTuGio.setEditor(new JSpinner.DateEditor(spTuGio, "HH:mm"));
        spTuGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spTuGio.setBounds(75, y, 80, rowH);
        formPanel.add(spTuGio);

        JLabel lblDenGio = new JLabel("Đến giờ");
        lblDenGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDenGio.setBounds(195, y, 60, rowH);
        formPanel.add(lblDenGio);

        spDenGio = new JSpinner(new SpinnerDateModel());
        spDenGio.setEditor(new JSpinner.DateEditor(spDenGio, "HH:mm"));
        spDenGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spDenGio.setBounds(265, y, 80, rowH);
        formPanel.add(spDenGio);

        y += rowH + gap;
        chkNgungApDung = new JCheckBox("Ngừng áp dụng");
        chkNgungApDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkNgungApDung.setOpaque(false);
        chkNgungApDung.setBounds(75, y, 160, rowH);
        formPanel.add(chkNgungApDung);

        // ===== CỘT PHẢI =====
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNote.setBounds(415, 8, 55, rowH);
        formPanel.add(lblNote);

        txtNote = new JTextField();
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNote.setBounds(475, 8, 520, rowH);
        formPanel.add(txtNote);

        JLabel lblStores = new JLabel("Cửa hàng áp dụng");
        lblStores.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStores.setBounds(415, 36, 110, rowH);
        formPanel.add(lblStores);

        String[] storeCols = {"Cửa hàng", "Áp dụng"};
        modelCuaHang = new DefaultTableModel(storeCols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        tableCuaHang = new JTable(modelCuaHang);
        tableCuaHang.setRowHeight(20);
        tableCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCuaHang.getColumnModel().getColumn(0).setPreferredWidth(300);
        tableCuaHang.getColumnModel().getColumn(1).setPreferredWidth(80);

        JScrollPane scrollStores = new JScrollPane(tableCuaHang);
        scrollStores.setBounds(530, 36, 465, 110);
        scrollStores.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        formPanel.add(scrollStores);

        // ===== BOTTOM 3-LISTS CONTAINER =====
        JPanel bottomContainer = new JPanel(new GridBagLayout());
        bottomContainer.setBorder(new EmptyBorder(4, 8, 4, 8));
        bottomContainer.setBackground(new Color(240, 243, 248));

        // 1. LIST 1: Nhóm mặt hàng
        JPanel panelList1 = new JPanel(new BorderLayout(0, 4));
        panelList1.setBackground(new Color(240, 243, 248));

        JPanel titlePanel1 = createListTitlePanel("1. Nhóm mặt hàng", "Folder.png");
        panelList1.add(titlePanel1, BorderLayout.NORTH);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tất cả");
        treeModelSP = new DefaultTreeModel(rootNode);
        treeNhomHangSP = new JTree(treeModelSP);
        treeNhomHangSP.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomHangSP.setRowHeight(22);
        treeNhomHangSP.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if ("Tất cả".equals(userObj)) {
                        lbl.setText("Tất cả nhóm hàng");
                        lbl.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                    } else if (userObj instanceof NhomMatHang) {
                        NhomMatHang n = (NhomMatHang) userObj;
                        lbl.setText(n.getName());
                        String icon = n.isFolder() ? "Folder.png" : (n.getIconName() != null ? n.getIconName() : "1282529873_objects.png");
                        lbl.setIcon(IconHelper.getIcon(icon, 16, 16));
                    }
                }
                return lbl;
            }
        });

        treeNhomHangSP.addTreeSelectionListener(e -> {
            TreePath path = treeNhomHangSP.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObj = node.getUserObject();
                if ("Tất cả".equals(userObj)) {
                    currentSelectedNhomSP = "Tất cả";
                    currentSelectedNhomIdSP = null;
                } else if (userObj instanceof NhomMatHang) {
                    NhomMatHang n = (NhomMatHang) userObj;
                    currentSelectedNhomSP = n.getName();
                    currentSelectedNhomIdSP = n.getId();
                }
                filterSourceProducts();
            }
        });

        JScrollPane scrollList1 = new JScrollPane(treeNhomHangSP);
        scrollList1.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        panelList1.add(scrollList1, BorderLayout.CENTER);

        // 2. LIST 2: Danh sách mặt hàng nguồn
        JPanel panelList2 = new JPanel(new BorderLayout(0, 4));
        panelList2.setBackground(new Color(240, 243, 248));

        JPanel topList2 = new JPanel(new BorderLayout(4, 0));
        topList2.setOpaque(false);
        JPanel titlePanel2 = createListTitlePanel("2. Danh sách mặt hàng", "1282529873_objects.png");
        topList2.add(titlePanel2, BorderLayout.WEST);

        txtSearchSanPham = new JTextField();
        txtSearchSanPham.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearchSanPham.setPreferredSize(new Dimension(130, 24));
        txtSearchSanPham.setToolTipText("Nhập mã hoặc tên mặt hàng để lọc nhanh");
        txtSearchSanPham.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterSourceProducts(); }
            @Override public void removeUpdate(DocumentEvent e) { filterSourceProducts(); }
            @Override public void changedUpdate(DocumentEvent e) { filterSourceProducts(); }
        });
        JPanel searchBox = new JPanel(new BorderLayout(2, 0));
        searchBox.setOpaque(false);
        searchBox.add(new JLabel(IconHelper.getIcon("Find.png", 14, 14)), BorderLayout.WEST);
        searchBox.add(txtSearchSanPham, BorderLayout.CENTER);
        topList2.add(searchBox, BorderLayout.CENTER);
        panelList2.add(topList2, BorderLayout.NORTH);

        String[] srcCols = {"STT", "Mã hàng", "Tên mặt hàng", "ĐVT", "Giá bán", "_ID"};
        modelSanPhamNguon = new DefaultTableModel(srcCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableSanPhamNguon = new JTable(modelSanPhamNguon);
        tableSanPhamNguon.setRowHeight(22);
        tableSanPhamNguon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableSanPhamNguon.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableSanPhamNguon.removeColumn(tableSanPhamNguon.getColumnModel().getColumn(5)); // hide _ID

        tableSanPhamNguon.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableSanPhamNguon.getColumnModel().getColumn(0).setMaxWidth(40);
        tableSanPhamNguon.getColumnModel().getColumn(1).setPreferredWidth(75);
        tableSanPhamNguon.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableSanPhamNguon.getColumnModel().getColumn(3).setPreferredWidth(45);
        tableSanPhamNguon.getColumnModel().getColumn(4).setPreferredWidth(75);

        tableSanPhamNguon.getColumnModel().getColumn(0).setCellRenderer(createCenterRenderer());
        tableSanPhamNguon.getColumnModel().getColumn(4).setCellRenderer(createRightRenderer());

        tableSanPhamNguon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onAddSelectedProducts();
                }
            }
        });

        JScrollPane scrollList2 = new JScrollPane(tableSanPhamNguon);
        scrollList2.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        panelList2.add(scrollList2, BorderLayout.CENTER);

        // Nút chuyển giữa List 2 và List 3
        JPanel panelTransfer = new JPanel();
        panelTransfer.setLayout(new BoxLayout(panelTransfer, BoxLayout.Y_AXIS));
        panelTransfer.setOpaque(false);
        panelTransfer.setBorder(new EmptyBorder(60, 4, 10, 4));

        JButton btnThem = createMiniActionButton(">", "Thêm mặt hàng đang chọn vào đợt khuyến mại");
        JButton btnThemAll = createMiniActionButton(">>", "Thêm toàn bộ mặt hàng trong danh sách");
        JButton btnXoa = createMiniActionButton("<", "Bỏ mặt hàng đang chọn khỏi đợt khuyến mại");
        JButton btnXoaAll = createMiniActionButton("<<", "Bỏ toàn bộ mặt hàng đã chọn");

        btnThem.addActionListener(e -> onAddSelectedProducts());
        btnThemAll.addActionListener(e -> onAddAllProducts());
        btnXoa.addActionListener(e -> onRemoveSelectedProducts());
        btnXoaAll.addActionListener(e -> onRemoveAllProducts());

        panelTransfer.add(btnThem);
        panelTransfer.add(Box.createVerticalStrut(6));
        panelTransfer.add(btnThemAll);
        panelTransfer.add(Box.createVerticalStrut(14));
        panelTransfer.add(btnXoa);
        panelTransfer.add(Box.createVerticalStrut(6));
        panelTransfer.add(btnXoaAll);

        // 3. LIST 3: Mặt hàng áp dụng & Tỉ lệ giảm
        JPanel panelList3 = new JPanel(new BorderLayout(0, 4));
        panelList3.setBackground(new Color(240, 243, 248));

        JPanel topList3 = new JPanel(new BorderLayout(4, 0));
        topList3.setOpaque(false);
        JPanel titlePanel3 = createListTitlePanel("3. Mặt hàng áp dụng & Tỉ lệ giảm", "asterisk-yellow.png");
        topList3.add(titlePanel3, BorderLayout.WEST);

        JPanel quickRatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        quickRatePanel.setOpaque(false);
        JLabel lblQuickRate = new JLabel("Tỉ lệ (%):");
        lblQuickRate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtQuickRateSP = new JTextField("10", 3);
        txtQuickRateSP.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtQuickRateSP.setHorizontalAlignment(JTextField.RIGHT);
        btnApplyQuickRateSP = new JButton("Gán %");
        btnApplyQuickRateSP.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnApplyQuickRateSP.setMargin(new Insets(1, 4, 1, 4));
        btnApplyQuickRateSP.setFocusPainted(false);
        btnApplyQuickRateSP.addActionListener(e -> onApplyQuickRate());

        quickRatePanel.add(lblQuickRate);
        quickRatePanel.add(txtQuickRateSP);
        quickRatePanel.add(btnApplyQuickRateSP);
        topList3.add(quickRatePanel, BorderLayout.CENTER);
        panelList3.add(topList3, BorderLayout.NORTH);

        String[] appliedCols = {"STT", "Mã hàng", "Tên mặt hàng", "ĐVT", "Giá bán", "Tỉ lệ giảm (%)", "_ID"};
        modelSanPhamChiTiet = new DefaultTableModel(appliedCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Tỉ lệ giảm is editable
            }
        };

        tableSanPhamChiTiet = new JTable(modelSanPhamChiTiet);
        tableSanPhamChiTiet.setRowHeight(22);
        tableSanPhamChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableSanPhamChiTiet.removeColumn(tableSanPhamChiTiet.getColumnModel().getColumn(6)); // hide _ID

        tableSanPhamChiTiet.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableSanPhamChiTiet.getColumnModel().getColumn(0).setMaxWidth(40);
        tableSanPhamChiTiet.getColumnModel().getColumn(1).setPreferredWidth(75);
        tableSanPhamChiTiet.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableSanPhamChiTiet.getColumnModel().getColumn(3).setPreferredWidth(45);
        tableSanPhamChiTiet.getColumnModel().getColumn(4).setPreferredWidth(75);
        tableSanPhamChiTiet.getColumnModel().getColumn(5).setPreferredWidth(85);

        tableSanPhamChiTiet.getColumnModel().getColumn(0).setCellRenderer(createCenterRenderer());
        tableSanPhamChiTiet.getColumnModel().getColumn(4).setCellRenderer(createRightRenderer());

        // Highlight editable column 5
        DefaultTableCellRenderer rateRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSelected) {
                    lbl.setBackground(new Color(255, 255, 225));
                }
                return lbl;
            }
        };
        tableSanPhamChiTiet.getColumnModel().getColumn(5).setCellRenderer(rateRenderer);

        tableSanPhamChiTiet.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    onRemoveSelectedProducts();
                }
            }
        });

        JScrollPane scrollList3 = new JScrollPane(tableSanPhamChiTiet);
        scrollList3.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        panelList3.add(scrollList3, BorderLayout.CENTER);

        // Put lists into bottomContainer with GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // 1. Nhóm hàng (22%)
        gbc.gridx = 0;
        gbc.weightx = 0.22;
        gbc.insets = new Insets(0, 0, 0, 4);
        bottomContainer.add(panelList1, gbc);

        // 2. Mặt hàng nguồn (36%)
        gbc.gridx = 1;
        gbc.weightx = 0.36;
        gbc.insets = new Insets(0, 0, 0, 2);
        bottomContainer.add(panelList2, gbc);

        // Buttons chuyển (Fixed)
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 2);
        bottomContainer.add(panelTransfer, gbc);

        // 3. Mặt hàng áp dụng (42%)
        gbc.gridx = 3;
        gbc.weightx = 0.42;
        gbc.insets = new Insets(0, 0, 0, 0);
        bottomContainer.add(panelList3, gbc);

        centerContainer.add(formPanel, BorderLayout.NORTH);
        centerContainer.add(bottomContainer, BorderLayout.CENTER);
    }

    private JPanel createListTitlePanel(String title, String iconName) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setBackground(new Color(225, 236, 248));
        p.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_DARK);
        if (iconName != null) {
            lbl.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        p.add(lbl);
        return p;
    }

    private JButton createMiniActionButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(42, 26));
        btn.setMaximumSize(new Dimension(42, 26));
        btn.setMinimumSize(new Dimension(42, 26));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 2, 2, 2));
        return btn;
    }

    private DefaultTableCellRenderer createCenterRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    private DefaultTableCellRenderer createRightRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        return r;
    }

    private void loadTreeNhomHangSP() {
        if (treeModelSP == null) return;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        List<NhomMatHang> allGroups = nhomMatHangDAO.getAll();
        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();
        List<NhomMatHang> remaining = new ArrayList<>();

        for (NhomMatHang n : allGroups) {
            if (n.isSeparator()) continue;
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(n);
            nodeMap.put(n.getId(), node);
            if (n.getParentId() == null || n.getParentId().trim().isEmpty() || "0".equals(n.getParentId())) {
                root.add(node);
            } else {
                remaining.add(n);
            }
        }

        for (NhomMatHang n : remaining) {
            DefaultMutableTreeNode parentNode = nodeMap.get(n.getParentId());
            DefaultMutableTreeNode currentNode = nodeMap.get(n.getId());
            if (parentNode != null && currentNode != null) {
                parentNode.add(currentNode);
            } else if (currentNode != null) {
                root.add(currentNode);
            }
        }

        treeModelSP.setRoot(root);
        treeModelSP.reload();

        for (int i = 0; i < treeNhomHangSP.getRowCount(); i++) {
            treeNhomHangSP.expandRow(i);
        }
        treeNhomHangSP.setSelectionRow(0);
    }

    private void filterSourceProducts() {
        if (modelSanPhamNguon == null) return;
        modelSanPhamNguon.setRowCount(0);
        filteredSourceProducts.clear();

        List<MatHang> productsByGroup;
        if (currentSelectedNhomSP == null || "Tất cả".equalsIgnoreCase(currentSelectedNhomSP)) {
            productsByGroup = allProducts != null && !allProducts.isEmpty() ? allProducts : matHangDAO.getAll();
        } else if (currentSelectedNhomIdSP != null) {
            NhomMatHang selGroup = nhomMatHangDAO.findById(currentSelectedNhomIdSP);
            if (selGroup != null && selGroup.isFolder()) {
                Set<String> descIds = nhomMatHangDAO.getAllDescendantIds(selGroup.getId());
                Set<String> descNames = new HashSet<>();
                for (NhomMatHang g : nhomMatHangDAO.getAll()) {
                    if (descIds.contains(g.getId()) && g.getName() != null) descNames.add(g.getName());
                }
                productsByGroup = matHangDAO.getByNhomIds(descIds, descNames);
            } else {
                productsByGroup = matHangDAO.getByNhom(currentSelectedNhomSP, currentSelectedNhomIdSP);
            }
        } else {
            productsByGroup = matHangDAO.getByNhom(currentSelectedNhomSP, null);
        }

        String search = txtSearchSanPham != null ? txtSearchSanPham.getText().trim().toLowerCase() : "";
        int stt = 1;
        for (MatHang mh : productsByGroup) {
            if (!search.isEmpty()) {
                boolean matchCode = mh.getCode() != null && mh.getCode().toLowerCase().contains(search);
                boolean matchName = mh.getName() != null && mh.getName().toLowerCase().contains(search);
                if (!matchCode && !matchName) continue;
            }
            filteredSourceProducts.add(mh);
            modelSanPhamNguon.addRow(new Object[]{
                    String.format("%02d", stt++),
                    mh.getCode() != null ? mh.getCode() : "",
                    mh.getName() != null ? mh.getName() : "",
                    mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "",
                    DF_CURRENCY.format(mh.getGiaBan()),
                    mh.getId()
            });
        }
    }

    private void onAddSelectedProducts() {
        if (tableSanPhamNguon == null || modelSanPhamChiTiet == null) return;
        int[] selectedRows = tableSanPhamNguon.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một mặt hàng từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double defaultRate = 0.0;
        if (txtQuickRateSP != null && !txtQuickRateSP.getText().trim().isEmpty()) {
            try {
                defaultRate = Double.parseDouble(txtQuickRateSP.getText().trim().replace(",", "."));
            } catch (Exception ignored) {}
        }
        String rateStr = defaultRate > 0 ? (defaultRate == (long) defaultRate ? String.valueOf((long) defaultRate) : String.valueOf(defaultRate)) : "";

        Set<String> existingIds = new HashSet<>();
        for (int r = 0; r < modelSanPhamChiTiet.getRowCount(); r++) {
            Object id = modelSanPhamChiTiet.getValueAt(r, 6);
            if (id != null) existingIds.add(id.toString());
        }

        for (int r : selectedRows) {
            if (r >= 0 && r < filteredSourceProducts.size()) {
                MatHang mh = filteredSourceProducts.get(r);
                if (!existingIds.contains(mh.getId())) {
                    existingIds.add(mh.getId());
                    modelSanPhamChiTiet.addRow(new Object[]{
                            String.format("%02d", modelSanPhamChiTiet.getRowCount() + 1),
                            mh.getCode() != null ? mh.getCode() : "",
                            mh.getName() != null ? mh.getName() : "",
                            mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "",
                            DF_CURRENCY.format(mh.getGiaBan()),
                            rateStr,
                            mh.getId()
                    });
                }
            }
        }
        renumberSTT(modelSanPhamChiTiet);
    }

    private void onAddAllProducts() {
        if (filteredSourceProducts == null || modelSanPhamChiTiet == null) return;
        double defaultRate = 0.0;
        if (txtQuickRateSP != null && !txtQuickRateSP.getText().trim().isEmpty()) {
            try {
                defaultRate = Double.parseDouble(txtQuickRateSP.getText().trim().replace(",", "."));
            } catch (Exception ignored) {}
        }
        String rateStr = defaultRate > 0 ? (defaultRate == (long) defaultRate ? String.valueOf((long) defaultRate) : String.valueOf(defaultRate)) : "";

        Set<String> existingIds = new HashSet<>();
        for (int r = 0; r < modelSanPhamChiTiet.getRowCount(); r++) {
            Object id = modelSanPhamChiTiet.getValueAt(r, 6);
            if (id != null) existingIds.add(id.toString());
        }

        for (MatHang mh : filteredSourceProducts) {
            if (!existingIds.contains(mh.getId())) {
                existingIds.add(mh.getId());
                modelSanPhamChiTiet.addRow(new Object[]{
                        String.format("%02d", modelSanPhamChiTiet.getRowCount() + 1),
                        mh.getCode() != null ? mh.getCode() : "",
                        mh.getName() != null ? mh.getName() : "",
                        mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "",
                        DF_CURRENCY.format(mh.getGiaBan()),
                        rateStr,
                        mh.getId()
                });
            }
        }
        renumberSTT(modelSanPhamChiTiet);
    }

    private void onRemoveSelectedProducts() {
        if (tableSanPhamChiTiet == null || modelSanPhamChiTiet == null) return;
        int[] selectedRows = tableSanPhamChiTiet.getSelectedRows();
        if (selectedRows.length == 0) return;
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            modelSanPhamChiTiet.removeRow(selectedRows[i]);
        }
        renumberSTT(modelSanPhamChiTiet);
    }

    private void onRemoveAllProducts() {
        if (modelSanPhamChiTiet == null) return;
        modelSanPhamChiTiet.setRowCount(0);
    }

    private void renumberSTT(DefaultTableModel model) {
        for (int r = 0; r < model.getRowCount(); r++) {
            model.setValueAt(String.format("%02d", r + 1), r, 0);
        }
    }

    private void onApplyQuickRate() {
        if (txtQuickRateSP == null || modelSanPhamChiTiet == null) return;
        String rateText = txtQuickRateSP.getText().trim();
        if (rateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tỉ lệ giảm giá (%)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtQuickRateSP.requestFocus();
            return;
        }
        try {
            double rate = Double.parseDouble(rateText.replace(",", "."));
            String rateStr = (rate == (long) rate) ? String.valueOf((long) rate) : String.valueOf(rate);
            int[] selRows = tableSanPhamChiTiet.getSelectedRows();
            if (selRows.length > 0) {
                for (int r : selRows) {
                    modelSanPhamChiTiet.setValueAt(rateStr, r, 5);
                }
            } else {
                for (int r = 0; r < modelSanPhamChiTiet.getRowCount(); r++) {
                    modelSanPhamChiTiet.setValueAt(rateStr, r, 5);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tỉ lệ giảm giá không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildNhomPromotionForm(JPanel centerContainer) {
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setPreferredSize(new Dimension(780, 160));
        formPanel.setBorder(new EmptyBorder(6, 12, 6, 12));

        int rowH = 24;
        int gap = 5;
        int y = 8;

        // ===== CỘT TRÁI =====
        // Row 1: Tên đợt khuyến mại
        JLabel lblName = new JLabel("Tên đợt khuyến mại");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(12, y, 130, rowH);
        formPanel.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBackground(COLOR_REQUIRED_FIELD);
        txtName.setBounds(145, y, 220, rowH);
        formPanel.add(txtName);

        // Row 2: Loại hình khuyến mại
        y += rowH + gap;
        JLabel lblLoaiHinh = new JLabel("Loại hình khuyến mại");
        lblLoaiHinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoaiHinh.setBounds(12, y, 130, rowH);
        formPanel.add(lblLoaiHinh);

        cbLoaiHinhKM = new JComboBox<>();
        cbLoaiHinhKM.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLoaiHinhKM.setBounds(145, y, 220, rowH);
        populateLoaiHinhKMComboBox();
        formPanel.add(cbLoaiHinhKM);

        // Row 3: Từ ngày - Đến ngày
        y += rowH + gap;
        JLabel lblTuNgay = new JLabel("Từ ngày");
        lblTuNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTuNgay.setBounds(12, y, 60, rowH);
        formPanel.add(lblTuNgay);

        dpTuNgay = new DatePickerField();
        dpTuNgay.setBounds(75, y, 100, rowH);
        formPanel.add(dpTuNgay);

        JLabel lblDenNgay = new JLabel("Đến ngày");
        lblDenNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDenNgay.setBounds(185, y, 60, rowH);
        formPanel.add(lblDenNgay);

        dpDenNgay = new DatePickerField();
        dpDenNgay.setBounds(250, y, 100, rowH);
        formPanel.add(dpDenNgay);

        // Row 4: Từ giờ - Đến giờ
        y += rowH + gap;
        JLabel lblTuGio = new JLabel("Từ giờ");
        lblTuGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTuGio.setBounds(12, y, 60, rowH);
        formPanel.add(lblTuGio);

        spTuGio = new JSpinner(new SpinnerDateModel());
        spTuGio.setEditor(new JSpinner.DateEditor(spTuGio, "HH:mm"));
        spTuGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spTuGio.setBounds(75, y, 70, rowH);
        formPanel.add(spTuGio);

        JLabel lblDenGio = new JLabel("Đến giờ");
        lblDenGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDenGio.setBounds(185, y, 60, rowH);
        formPanel.add(lblDenGio);

        spDenGio = new JSpinner(new SpinnerDateModel());
        spDenGio.setEditor(new JSpinner.DateEditor(spDenGio, "HH:mm"));
        spDenGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spDenGio.setBounds(250, y, 70, rowH);
        formPanel.add(spDenGio);

        // Row 5: Ngừng áp dụng
        y += rowH + gap;
        chkNgungApDung = new JCheckBox("Ngừng áp dụng");
        chkNgungApDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkNgungApDung.setOpaque(false);
        chkNgungApDung.setBounds(75, y, 160, rowH);
        formPanel.add(chkNgungApDung);

        // ===== CỘT PHẢI =====
        // Row 1: Ghi chú
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNote.setBounds(385, 8, 55, rowH);
        formPanel.add(lblNote);

        txtNote = new JTextField();
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNote.setBounds(445, 8, 305, rowH);
        formPanel.add(txtNote);

        // Row 2..5: Cửa hàng áp dụng Table
        JLabel lblStores = new JLabel("Cửa hàng áp dụng");
        lblStores.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStores.setBounds(385, 36, 110, rowH);
        formPanel.add(lblStores);

        String[] storeCols = {"Cửa hàng", "Áp dụng"};
        modelCuaHang = new DefaultTableModel(storeCols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        tableCuaHang = new JTable(modelCuaHang);
        tableCuaHang.setRowHeight(20);
        tableCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCuaHang.getColumnModel().getColumn(0).setPreferredWidth(160);
        tableCuaHang.getColumnModel().getColumn(1).setPreferredWidth(70);

        JScrollPane scrollStores = new JScrollPane(tableCuaHang);
        scrollStores.setBounds(500, 36, 250, 110);
        scrollStores.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        formPanel.add(scrollStores);

        // ===== BẢNG CHI TIẾT NHÓM HÀNG DƯỚI =====
        String[] groupCols = {"", "Nhóm hàng", "Tỉ lệ giảm (%)", "_ID"};
        modelNhomChiTiet = new DefaultTableModel(groupCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only Tỉ lệ giảm is editable
            }
        };

        tableNhomChiTiet = new JTable(modelNhomChiTiet);
        tableNhomChiTiet.setRowHeight(22);
        tableNhomChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableNhomChiTiet.setShowGrid(true);
        tableNhomChiTiet.setGridColor(new Color(225, 232, 240));

        // Hide ID column
        tableNhomChiTiet.removeColumn(tableNhomChiTiet.getColumnModel().getColumn(3));

        tableNhomChiTiet.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableNhomChiTiet.getColumnModel().getColumn(0).setMaxWidth(40);
        tableNhomChiTiet.getColumnModel().getColumn(1).setPreferredWidth(450);
        tableNhomChiTiet.getColumnModel().getColumn(2).setPreferredWidth(120);

        // STT renderer
        tableNhomChiTiet.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(245, 248, 252));
                if (row == table.getSelectedRow()) {
                    lbl.setIcon(IconHelper.getIcon("navigation-000-frame.png", 10, 10));
                } else {
                    lbl.setIcon(null);
                }
                return lbl;
            }
        });

        // Right aligned for %
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableNhomChiTiet.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JTableHeader th = tableNhomChiTiet.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(230, 238, 248));
        th.setPreferredSize(new Dimension(100, 26));

        JScrollPane scrollGroups = new JScrollPane(tableNhomChiTiet);
        scrollGroups.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        centerContainer.add(formPanel, BorderLayout.NORTH);
        centerContainer.add(scrollGroups, BorderLayout.CENTER);
    }

    private void buildGeneralPromotionForm(JPanel centerContainer) {
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        int y = 10;
        int labelWidth = 140;
        int fieldX = 155;
        int fieldWidth = 345;
        int rowHeight = 24;
        int gap = 6;

        // Row 1: Tên đợt khuyến mại
        JLabel lblName = new JLabel("Tên đợt khuyến mại");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBackground(COLOR_REQUIRED_FIELD);
        txtName.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtName);

        // Row 2: Loại hình khuyến mại
        y += rowHeight + gap;
        JLabel lblLoaiHinh = new JLabel("Loại hình khuyến mại");
        lblLoaiHinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoaiHinh.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblLoaiHinh);

        cbLoaiHinhKM = new JComboBox<>();
        cbLoaiHinhKM.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLoaiHinhKM.setBounds(fieldX, y, fieldWidth, rowHeight);
        populateLoaiHinhKMComboBox();
        formPanel.add(cbLoaiHinhKM);

        // Row 3: Từ ngày - Đến ngày
        y += rowHeight + gap;
        JLabel lblTuNgay = new JLabel("Từ ngày");
        lblTuNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTuNgay.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblTuNgay);

        dpTuNgay = new DatePickerField();
        dpTuNgay.setBounds(fieldX, y, 105, rowHeight);
        formPanel.add(dpTuNgay);

        JLabel lblDenNgay = new JLabel("Đến ngày");
        lblDenNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDenNgay.setBounds(275, y, 60, rowHeight);
        formPanel.add(lblDenNgay);

        dpDenNgay = new DatePickerField();
        dpDenNgay.setBounds(345, y, 105, rowHeight);
        formPanel.add(dpDenNgay);

        // Row 4: Từ giờ - Đến giờ
        y += rowHeight + gap;
        JLabel lblTuGio = new JLabel("Từ giờ");
        lblTuGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTuGio.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblTuGio);

        spTuGio = new JSpinner(new SpinnerDateModel());
        spTuGio.setEditor(new JSpinner.DateEditor(spTuGio, "HH:mm"));
        spTuGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spTuGio.setBounds(fieldX, y, 75, rowHeight);
        formPanel.add(spTuGio);

        JLabel lblDenGio = new JLabel("Đến giờ");
        lblDenGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDenGio.setBounds(275, y, 60, rowHeight);
        formPanel.add(lblDenGio);

        spDenGio = new JSpinner(new SpinnerDateModel());
        spDenGio.setEditor(new JSpinner.DateEditor(spDenGio, "HH:mm"));
        spDenGio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spDenGio.setBounds(345, y, 75, rowHeight);
        formPanel.add(spDenGio);

        // Row 5: Tỉ lệ giảm giá % | Ngừng áp dụng
        y += rowHeight + gap;
        JLabel lblTiLe = new JLabel("Tỉ lệ giảm giá");
        lblTiLe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTiLe.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblTiLe);

        txtTiLeGiamGia = new JTextField("0");
        txtTiLeGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTiLeGiamGia.setBounds(fieldX, y, 75, rowHeight);
        formPanel.add(txtTiLeGiamGia);

        JLabel lblPercent = new JLabel("%");
        lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPercent.setBounds(fieldX + 80, y, 20, rowHeight);
        formPanel.add(lblPercent);

        chkNgungApDung = new JCheckBox("Ngừng áp dụng");
        chkNgungApDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkNgungApDung.setOpaque(false);
        chkNgungApDung.setBounds(275, y, 160, rowHeight);
        formPanel.add(chkNgungApDung);

        // Row 6: Ghi chú
        y += rowHeight + gap;
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblGhiChu);

        txtNote = new JTextField();
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNote.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtNote);

        // Row 7: Cửa hàng áp dụng (Table)
        y += rowHeight + gap + 4;
        JLabel lblStores = new JLabel("Cửa hàng áp dụng:");
        lblStores.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStores.setForeground(new Color(25, 55, 105));
        lblStores.setBounds(15, y, 200, 20);
        formPanel.add(lblStores);

        y += 24;
        String[] storeCols = {"Chọn", "Tên cửa hàng", "Địa chỉ"};
        modelCuaHang = new DefaultTableModel(storeCols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        tableCuaHang = new JTable(modelCuaHang);
        tableCuaHang.setRowHeight(22);
        tableCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCuaHang.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableCuaHang.getColumnModel().getColumn(0).setMaxWidth(50);
        tableCuaHang.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableCuaHang.getColumnModel().getColumn(2).setPreferredWidth(230);

        JScrollPane scrollStores = new JScrollPane(tableCuaHang);
        scrollStores.setBounds(15, y, fieldWidth + fieldX - 15, 130);
        scrollStores.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        formPanel.add(scrollStores);

        centerContainer.add(formPanel, BorderLayout.CENTER);
    }

    private void populateLoaiHinhKMComboBox() {
        cbLoaiHinhKM.removeAllItems();
        List<LoaiHinhKhuyenMai> list = loaiHinhKhuyenMaiDAO.getAll();
        for (LoaiHinhKhuyenMai l : list) {
            cbLoaiHinhKM.addItem(l);
        }
        cbLoaiHinhKM.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoaiHinhKhuyenMai) {
                    LoaiHinhKhuyenMai l = (LoaiHinhKhuyenMai) value;
                    lbl.setText(l.getName());
                    lbl.setIcon(IconHelper.getIcon(l.getIconName() != null ? l.getIconName() : "asterisk-yellow.png", 14, 14));
                }
                return lbl;
            }
        });
    }

    private void loadFormData() {
        allStores = cuaHangDAO.getRealStores();
        List<String> selectedStoreIds = currentDotKM.getId() != null ? dotKhuyenMaiDAO.getStoresForPromotion(currentDotKM.getId()) : new ArrayList<>();

        modelCuaHang.setRowCount(0);
        boolean isSanPham = isSanPhamPromotion();
        boolean isNhom = isNhomPromotion();
        for (CuaHang ch : allStores) {
            boolean checked = selectedStoreIds.contains(ch.getId()) || (currentDotKM.getId() == null);
            if (isSanPham || isNhom) {
                modelCuaHang.addRow(new Object[]{ch.getName(), checked});
            } else {
                modelCuaHang.addRow(new Object[]{checked, ch.getName(), ch.getDiaChi() != null ? ch.getDiaChi() : ""});
            }
        }

        // Always disable cbLoaiHinhKM as promotion type cannot be edited
        cbLoaiHinhKM.setEnabled(false);

        if (currentDotKM.getId() != null) {
            setTitle("ĐỢT KHUYẾN MẠI - SỬA");
            txtName.setText(currentDotKM.getName() != null ? currentDotKM.getName() : "");
            txtNote.setText(currentDotKM.getNote() != null ? currentDotKM.getNote() : "");
            if (txtTiLeGiamGia != null) {
                txtTiLeGiamGia.setText(String.valueOf(currentDotKM.getTiLeGiamGia()));
            }
            chkNgungApDung.setSelected(currentDotKM.isNgungApDung());

            if (currentDotKM.getTuNgay() != null) {
                dpTuNgay.setDate(currentDotKM.getTuNgay());
            } else {
                dpTuNgay.setText("");
            }

            if (currentDotKM.getDenNgay() != null) {
                dpDenNgay.setDate(currentDotKM.getDenNgay());
            } else {
                dpDenNgay.setText("");
            }

            if (currentDotKM.getTuGio() != null) {
                spTuGio.setValue(new Date(currentDotKM.getTuGio().getTime()));
            } else {
                setSpinnerTime(spTuGio, 0, 0);
            }

            if (currentDotKM.getDenGio() != null) {
                spDenGio.setValue(new Date(currentDotKM.getDenGio().getTime()));
            } else {
                setSpinnerTime(spDenGio, 23, 59);
            }

            // Chọn Loại hình khuyến mãi
            if (currentDotKM.getdLoaiHinhKhuyenMaiId() != null) {
                for (int i = 0; i < cbLoaiHinhKM.getItemCount(); i++) {
                    LoaiHinhKhuyenMai l = cbLoaiHinhKM.getItemAt(i);
                    if (currentDotKM.getdLoaiHinhKhuyenMaiId().equals(l.getId())) {
                        cbLoaiHinhKM.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            setTitle("ĐỢT KHUYẾN MẠI - THÊM MỚI");
            txtName.setText("");
            txtNote.setText("");
            if (txtTiLeGiamGia != null) {
                txtTiLeGiamGia.setText("0");
            }
            chkNgungApDung.setSelected(false);
            dpTuNgay.setDate(new Date());
            dpDenNgay.setDate(new Date());
            setSpinnerTime(spTuGio, 0, 0);
            setSpinnerTime(spDenGio, 23, 59);
            if (currentDotKM.getdLoaiHinhKhuyenMaiId() != null) {
                for (int i = 0; i < cbLoaiHinhKM.getItemCount(); i++) {
                    LoaiHinhKhuyenMai l = cbLoaiHinhKM.getItemAt(i);
                    if (currentDotKM.getdLoaiHinhKhuyenMaiId().equals(l.getId())) {
                        cbLoaiHinhKM.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        // Tải chi tiết nhóm hàng nếu là khuyến mãi theo nhóm
        if (isNhom && modelNhomChiTiet != null) {
            allItemGroups = nhomMatHangDAO.getAll();
            Map<String, Double> groupDiscounts = currentDotKM.getId() != null ? dotKhuyenMaiDAO.getGroupDiscounts(currentDotKM.getId()) : currentDotKM.getMapNhomGiamGia();
            modelNhomChiTiet.setRowCount(0);
            int idx = 1;
            for (NhomMatHang n : allItemGroups) {
                if (n.isSeparator()) continue;
                Double rate = groupDiscounts != null ? groupDiscounts.get(n.getId()) : null;
                String rateStr = "";
                if (rate != null && rate > 0) {
                    rateStr = (rate == (long) rate.doubleValue()) ? String.valueOf((long) rate.doubleValue()) : String.valueOf(rate);
                }
                modelNhomChiTiet.addRow(new Object[]{
                        String.format("%02d", idx++),
                        n.getName(),
                        rateStr,
                        n.getId()
                });
            }
        }

        // Tải chi tiết sản phẩm nếu là khuyến mãi theo sản phẩm
        if (isSanPham && modelSanPhamChiTiet != null) {
            allProducts = matHangDAO.getAll();
            loadTreeNhomHangSP();
            filterSourceProducts();

            Map<String, Double> productDiscounts = currentDotKM.getId() != null ? dotKhuyenMaiDAO.getProductDiscounts(currentDotKM.getId()) : currentDotKM.getMapSanPhamGiamGia();
            modelSanPhamChiTiet.setRowCount(0);
            if (productDiscounts != null && !productDiscounts.isEmpty()) {
                int idx = 1;
                for (Map.Entry<String, Double> entry : productDiscounts.entrySet()) {
                    String mhId = entry.getKey();
                    Double rate = entry.getValue();
                    MatHang mh = null;
                    if (allProducts != null) {
                        for (MatHang p : allProducts) {
                            if (mhId.equals(p.getId())) {
                                mh = p;
                                break;
                            }
                        }
                    }
                    if (mh == null) mh = matHangDAO.findById(mhId);
                    if (mh != null) {
                        String rateStr = "";
                        if (rate != null && rate > 0) {
                            rateStr = (rate == (long) rate.doubleValue()) ? String.valueOf((long) rate.doubleValue()) : String.valueOf(rate);
                        }
                        modelSanPhamChiTiet.addRow(new Object[]{
                                String.format("%02d", idx++),
                                mh.getCode() != null ? mh.getCode() : "",
                                mh.getName() != null ? mh.getName() : "",
                                mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "",
                                DF_CURRENCY.format(mh.getGiaBan()),
                                rateStr,
                                mh.getId()
                        });
                    }
                }
            }
        }
    }

    private void setSpinnerTime(JSpinner spinner, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        spinner.setValue(c.getTime());
    }

    private void onSave(boolean isSaveAndNew, boolean isSaveAndExit) {
        // Stop any active table cell editing
        if (tableSanPhamChiTiet != null && tableSanPhamChiTiet.isEditing()) {
            tableSanPhamChiTiet.getCellEditor().stopCellEditing();
        }
        if (tableNhomChiTiet != null && tableNhomChiTiet.isEditing()) {
            tableNhomChiTiet.getCellEditor().stopCellEditing();
        }
        if (tableCuaHang != null && tableCuaHang.isEditing()) {
            tableCuaHang.getCellEditor().stopCellEditing();
        }

        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đợt khuyến mại không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        LoaiHinhKhuyenMai selectedLoai = (LoaiHinhKhuyenMai) cbLoaiHinhKM.getSelectedItem();
        if (selectedLoai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại hình khuyến mại!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cbLoaiHinhKM.requestFocus();
            return;
        }

        currentDotKM.setName(name);
        currentDotKM.setdLoaiHinhKhuyenMaiId(selectedLoai.getId());
        currentDotKM.setTenLoaiHinhKhuyenMai(selectedLoai.getName());
        currentDotKM.setNote(txtNote.getText().trim());
        currentDotKM.setNgungApDung(chkNgungApDung.isSelected());

        Date tuNgay = dpTuNgay.getDate();
        currentDotKM.setTuNgay(tuNgay != null ? new Timestamp(tuNgay.getTime()) : null);

        Date denNgay = dpDenNgay.getDate();
        currentDotKM.setDenNgay(denNgay != null ? new Timestamp(denNgay.getTime()) : null);

        boolean isSanPham = isSanPhamPromotion();
        boolean isNhom = isNhomPromotion();
        if (isSanPham) {
            Map<String, Double> mapDiscounts = new HashMap<>();
            double maxTiLe = 0;
            if (modelSanPhamChiTiet != null) {
                for (int r = 0; r < modelSanPhamChiTiet.getRowCount(); r++) {
                    Object idVal = modelSanPhamChiTiet.getValueAt(r, 6);
                    Object rateVal = modelSanPhamChiTiet.getValueAt(r, 5);
                    if (idVal != null) {
                        double d = 0;
                        if (rateVal != null) {
                            String rStr = rateVal.toString().trim().replace(",", ".");
                            if (!rStr.isEmpty()) {
                                try {
                                    d = Double.parseDouble(rStr);
                                } catch (Exception ignored) {}
                            }
                        }
                        mapDiscounts.put(idVal.toString(), d);
                        if (d > maxTiLe) maxTiLe = d;
                    }
                }
            }
            currentDotKM.setMapSanPhamGiamGia(mapDiscounts);
            currentDotKM.setTiLeGiamGia(maxTiLe);
        } else if (isNhom) {
            Map<String, Double> mapDiscounts = new HashMap<>();
            double maxTiLe = 0;
            if (modelNhomChiTiet != null) {
                for (int r = 0; r < modelNhomChiTiet.getRowCount(); r++) {
                    Object idVal = modelNhomChiTiet.getValueAt(r, 3);
                    Object rateVal = modelNhomChiTiet.getValueAt(r, 2);
                    if (idVal != null && rateVal != null) {
                        String rStr = rateVal.toString().trim().replace(",", ".");
                        if (!rStr.isEmpty()) {
                            try {
                                double d = Double.parseDouble(rStr);
                                if (d > 0) {
                                    mapDiscounts.put(idVal.toString(), d);
                                    if (d > maxTiLe) maxTiLe = d;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
            currentDotKM.setMapNhomGiamGia(mapDiscounts);
            currentDotKM.setTiLeGiamGia(maxTiLe);
        } else {
            try {
                double tiLe = Double.parseDouble(txtTiLeGiamGia.getText().trim().replace(",", "."));
                currentDotKM.setTiLeGiamGia(tiLe);
            } catch (Exception e) {
                currentDotKM.setTiLeGiamGia(0);
            }
        }

        Date tuGio = (Date) spTuGio.getValue();
        currentDotKM.setTuGio(tuGio != null ? new Timestamp(tuGio.getTime()) : null);

        Date denGio = (Date) spDenGio.getValue();
        currentDotKM.setDenGio(denGio != null ? new Timestamp(denGio.getTime()) : null);

        // Selected stores
        List<String> chosenStoreIds = new ArrayList<>();
        int checkCol = (isSanPham || isNhom) ? 1 : 0;
        for (int r = 0; r < modelCuaHang.getRowCount(); r++) {
            Boolean checked = (Boolean) modelCuaHang.getValueAt(r, checkCol);
            if (Boolean.TRUE.equals(checked) && r < allStores.size()) {
                chosenStoreIds.add(allStores.get(r).getId());
            }
        }
        currentDotKM.setListCuaHangApDungIds(chosenStoreIds);

        boolean success;
        if (currentDotKM.getId() != null) {
            success = dotKhuyenMaiDAO.update(currentDotKM);
        } else {
            success = dotKhuyenMaiDAO.insert(currentDotKM);
        }

        if (success) {
            if (onSaveCallback != null) onSaveCallback.run();
            if (isSaveAndExit) {
                dispose();
            } else if (isSaveAndNew) {
                onNewPromotion();
            } else {
                JOptionPane.showMessageDialog(this, "Lưu thông tin đợt khuyến mại thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu đợt khuyến mại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onNewPromotion() {
        DotKhuyenMai newKm = new DotKhuyenMai();
        if (currentDotKM != null) {
            newKm.setdLoaiHinhKhuyenMaiId(currentDotKM.getdLoaiHinhKhuyenMaiId());
            newKm.setTenLoaiHinhKhuyenMai(currentDotKM.getTenLoaiHinhKhuyenMai());
        }
        this.currentDotKM = newKm;
        this.currentIndex = -1;
        loadFormData();
        txtName.requestFocus();
    }

    private void onCopyPromotion() {
        DotKhuyenMai copy = new DotKhuyenMai();
        copy.setName(txtName.getText().trim() + " (Bản sao)");
        copy.setNote(txtNote.getText().trim());
        LoaiHinhKhuyenMai selectedLoai = (LoaiHinhKhuyenMai) cbLoaiHinhKM.getSelectedItem();
        if (selectedLoai != null) {
            copy.setdLoaiHinhKhuyenMaiId(selectedLoai.getId());
            copy.setTenLoaiHinhKhuyenMai(selectedLoai.getName());
        }
        Date tuNgay = dpTuNgay.getDate();
        copy.setTuNgay(tuNgay != null ? new Timestamp(tuNgay.getTime()) : null);
        Date denNgay = dpDenNgay.getDate();
        copy.setDenNgay(denNgay != null ? new Timestamp(denNgay.getTime()) : null);
        copy.setTuGio(new Timestamp(((Date) spTuGio.getValue()).getTime()));
        copy.setDenGio(new Timestamp(((Date) spDenGio.getValue()).getTime()));
        copy.setNgungApDung(chkNgungApDung.isSelected());

        if (isSanPhamPromotion()) {
            Map<String, Double> copiedDiscounts = new HashMap<>();
            if (modelSanPhamChiTiet != null) {
                for (int r = 0; r < modelSanPhamChiTiet.getRowCount(); r++) {
                    Object idVal = modelSanPhamChiTiet.getValueAt(r, 6);
                    Object rateVal = modelSanPhamChiTiet.getValueAt(r, 5);
                    if (idVal != null) {
                        double d = 0;
                        if (rateVal != null && !rateVal.toString().trim().isEmpty()) {
                            try {
                                d = Double.parseDouble(rateVal.toString().trim().replace(",", "."));
                            } catch (Exception ignored) {}
                        }
                        copiedDiscounts.put(idVal.toString(), d);
                    }
                }
            }
            copy.setMapSanPhamGiamGia(copiedDiscounts);
        } else if (isNhomPromotion()) {
            Map<String, Double> copiedDiscounts = new HashMap<>();
            if (modelNhomChiTiet != null) {
                for (int r = 0; r < modelNhomChiTiet.getRowCount(); r++) {
                    Object idVal = modelNhomChiTiet.getValueAt(r, 3);
                    Object rateVal = modelNhomChiTiet.getValueAt(r, 2);
                    if (idVal != null && rateVal != null && !rateVal.toString().trim().isEmpty()) {
                        try {
                            copiedDiscounts.put(idVal.toString(), Double.parseDouble(rateVal.toString().trim().replace(",", ".")));
                        } catch (Exception ignored) {}
                    }
                }
            }
            copy.setMapNhomGiamGia(copiedDiscounts);
        } else if (txtTiLeGiamGia != null) {
            try {
                copy.setTiLeGiamGia(Double.parseDouble(txtTiLeGiamGia.getText().trim().replace(",", ".")));
            } catch (Exception e) {
                copy.setTiLeGiamGia(0);
            }
        }

        this.currentDotKM = copy;
        this.currentIndex = -1;
        loadFormData();
    }

    private void onDeletePromotion() {
        if (currentDotKM.getId() == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn chuyển đợt khuyến mại '" + currentDotKM.getName() + "' vào thùng rác?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dotKhuyenMaiDAO.moveToTrash(currentDotKM.getId());
            if (onSaveCallback != null) onSaveCallback.run();
            dispose();
        }
    }

    private void navigatePrevious() {
        if (allPromotions.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = allPromotions.size() - 1;
        }
        this.currentDotKM = allPromotions.get(currentIndex);
        loadFormData();
    }

    private void navigateNext() {
        if (allPromotions.isEmpty()) return;
        if (currentIndex < allPromotions.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        this.currentDotKM = allPromotions.get(currentIndex);
        loadFormData();
    }

    private void showShortcutMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("Ctrl + L: Lưu"));
        menu.add(new JMenuItem("Ctrl + S: Lưu & Mới"));
        menu.add(new JMenuItem("Ctrl + Enter: Lưu & Thoát"));
        menu.add(new JMenuItem("Ctrl + N: Tạo mới"));
        menu.add(new JMenuItem("F10: Xem đợt trước"));
        menu.add(new JMenuItem("F11: Xem đợt sau"));
        menu.add(new JMenuItem("ESC: Thoát"));
        menu.show(invoker, 0, invoker.getHeight());
    }

    private JButton createRibbonButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(text.length() > 6 ? 100 : 75, 26));
        btn.setBackground(Color.WHITE);
        return btn;
    }

    private void setupShortcuts() {
        getRootPane().registerKeyboardAction(e -> onSave(false, true), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> onSave(false, false), KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> onSave(true, false), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> onNewPromotion(), KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> onNewPromotion(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigatePrevious(), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigateNext(), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
