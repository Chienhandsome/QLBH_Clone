package kazuko.ex.ui.touch;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.service.AuthService;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Phân hệ Quản lý bán hàng cảm ứng POS (TouchQuanLyBanHangPanel)
 * Chuẩn 100% theo Hình 1 (Màn hình chính) và Hình 5 (Tab Giao hàng).
 */
public class TouchQuanLyBanHangPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(211, 84, 0);
    private static final Color PURPLE = new Color(106, 13, 84);
    private static final Color GRAY_BTN = new Color(135, 145, 150);
    private static final Color FIELD_GREEN = new Color(200, 247, 197); // Xanh nhạt như Hình 5
    private static final Color FIELD_GRAY = new Color(210, 215, 220);  // Xám nhạt như Hình 1
    private static final Color TAB_INACTIVE = new Color(90, 115, 130);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final Runnable exit;

    private TouchQuanLyBanHangBillColumnConfig config;

    private Date tuNgay;
    private Date denNgay;

    private List<DonHang> billsList = new ArrayList<>();
    private DonHang currentBill = null;

    // Bộ lọc ngày
    private final JTextField txtTuNgay = new JTextField();
    private final JTextField txtDenNgay = new JTextField();

    // Lưới thẻ hóa đơn
    private final JPanel billsGridPanel = new JPanel();

    // Tab bên phải
    private final JButton btnTabHoaDon = new JButton("HÓA ĐƠN BÁN HÀNG");
    private final JButton btnTabGiaoHang = new JButton("GIAO HÀNG");
    private final JButton btnTabMini = new JButton("MINI");
    private final CardLayout cardHeaderRight = new CardLayout();
    private final JPanel pnlHeaderRightCards = new JPanel(cardHeaderRight);
    private boolean isGiaoHangTab = false;

    // Header fields - Hóa đơn bán hàng
    private final JTextField txtNgay = createRoundedField(FIELD_GRAY);
    private final JTextField txtSo = createRoundedField(FIELD_GRAY);
    private final JTextField txtKhachHang = createRoundedField(FIELD_GRAY);
    private final JTextField txtNhanVien = createRoundedField(FIELD_GRAY);

    // Header fields - Giao hàng
    private final JTextField txtGiaoHang = createRoundedField(FIELD_GREEN);
    private final JTextField txtNhanVienGiao = createRoundedField(FIELD_GREEN);
    private final JTextField txtTrichNhanVien = createRoundedField(FIELD_GREEN);
    private final JTextField txtGhiChuGiaoHang = createRoundedField(FIELD_GREEN);

    // Danh sách mặt hàng bên phải (Hình 5)
    private final JPanel itemsListPanel = new JPanel();

    // Tổng kết
    private final JLabel lblTienHang = new JLabel("0", SwingConstants.RIGHT);
    private final JLabel lblTiLeGiamGia = new JLabel("0 %", SwingConstants.CENTER);
    private final JLabel lblTienGiamGia = new JLabel("0", SwingConstants.RIGHT);
    private final JLabel lblTongCong = new JLabel("0", SwingConstants.RIGHT);
    private final JLabel lblThanhToan = new JLabel("0", SwingConstants.RIGHT);

    public TouchQuanLyBanHangPanel(Runnable exit) {
        this.exit = exit;
        this.config = TouchQuanLyBanHangBillConfigDAO.loadConfig();

        setLayout(new BorderLayout());
        setBackground(BG);

        Calendar cal = Calendar.getInstance();
        denNgay = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        tuNgay = cal.getTime();

        initComponents();

        loadBills();
    }

    private void initComponents() {
        // 1. Header Bar (Hình 1)
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(HEADER);
        headerBar.setPreferredSize(new Dimension(0, 48));

        JLabel lblTitle = new JLabel("QUẢN LÝ BÁN HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(Color.WHITE);
        headerBar.add(lblTitle, BorderLayout.CENTER);

        // 3 Tabs bên phải
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        tabsPanel.setOpaque(false);

        styleTabButton(btnTabHoaDon, true);
        styleTabButton(btnTabGiaoHang, false);
        styleTabButton(btnTabMini, false);

        btnTabHoaDon.addActionListener(e -> switchTab(false));
        btnTabGiaoHang.addActionListener(e -> switchTab(true));

        tabsPanel.add(btnTabHoaDon);
        tabsPanel.add(btnTabGiaoHang);
        tabsPanel.add(btnTabMini);
        headerBar.add(tabsPanel, BorderLayout.EAST);

        add(headerBar, BorderLayout.NORTH);

        // 2. Thân chính: Left Panel (Hóa đơn) và Right Panel (Chi tiết)
        JPanel body = new JPanel(new BorderLayout(8, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(6, 8, 6, 8));

        // BÊN TRÁI: LƯỚI HÓA ĐƠN
        JPanel leftPanel = createLeftPanel();
        body.add(leftPanel, BorderLayout.CENTER);

        // BÊN PHẢI: CHI TIẾT HÓA ĐƠN & THANH TOÁN
        JPanel rightPanel = createRightPanel();
        rightPanel.setPreferredSize(new Dimension(480, 0));
        body.add(rightPanel, BorderLayout.EAST);

        add(body, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(false);

        // Toolbar trên: TỪ: [ date ]  ĐẾN: [ date ]  [ ▼ ]
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterRow.setOpaque(false);

        JLabel lblTu = new JLabel("TỪ:");
        lblTu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTu.setForeground(Color.WHITE);
        filterRow.add(lblTu);

        txtTuNgay.setPreferredSize(new Dimension(105, 36));
        txtTuNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTuNgay.setHorizontalAlignment(SwingConstants.CENTER);
        txtTuNgay.setEditable(false);
        txtTuNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtTuNgay.setText(SDF.format(tuNgay));
        txtTuNgay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String picked = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(TouchQuanLyBanHangPanel.this), txtTuNgay.getText());
                if (picked != null && !picked.trim().isEmpty()) {
                    try {
                        tuNgay = SDF.parse(picked);
                        txtTuNgay.setText(picked);
                        loadBills();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        filterRow.add(txtTuNgay);

        JLabel lblDen = new JLabel("ĐẾN:");
        lblDen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDen.setForeground(Color.WHITE);
        filterRow.add(lblDen);

        txtDenNgay.setPreferredSize(new Dimension(105, 36));
        txtDenNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtDenNgay.setHorizontalAlignment(SwingConstants.CENTER);
        txtDenNgay.setEditable(false);
        txtDenNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtDenNgay.setText(SDF.format(denNgay));
        txtDenNgay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String picked = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(TouchQuanLyBanHangPanel.this), txtDenNgay.getText());
                if (picked != null && !picked.trim().isEmpty()) {
                    try {
                        denNgay = SDF.parse(picked);
                        txtDenNgay.setText(picked);
                        loadBills();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        filterRow.add(txtDenNgay);

        JButton btnQuickDate = new JButton("▼");
        btnQuickDate.setPreferredSize(new Dimension(38, 36));
        btnQuickDate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickDate.setBackground(new Color(0, 67, 86));
        btnQuickDate.setForeground(Color.WHITE);
        btnQuickDate.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnQuickDate.setFocusPainted(false);
        btnQuickDate.addActionListener(e -> {
            TouchChonNhanhNgayDialog.showDialog(SwingUtilities.getWindowAncestor(this), (start, end) -> {
                if (start != null && end != null) {
                    tuNgay = start;
                    denNgay = end;
                    txtTuNgay.setText(SDF.format(tuNgay));
                    txtDenNgay.setText(SDF.format(denNgay));
                    loadBills();
                }
            });
        });
        filterRow.add(btnQuickDate);
        pnl.add(filterRow, BorderLayout.NORTH);

        // Khu vực lưới thẻ hóa đơn
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);

        billsGridPanel.setLayout(new GridLayout(0, config.getGridColumns(), 8, 8));
        billsGridPanel.setOpaque(false);

        JScrollPane spBills = new JScrollPane(billsGridPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spBills.setOpaque(false);
        spBills.getViewport().setOpaque(false);
        spBills.setBorder(null);
        spBills.getVerticalScrollBar().setUnitIncrement(35);
        gridWrapper.add(spBills, BorderLayout.CENTER);

        // Nút bánh răng ở góc dưới phải của danh sách hóa đơn (Hình 1)
        JPanel gearBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        gearBar.setOpaque(false);
        JButton btnGear = new JButton("⚙");
        btnGear.setPreferredSize(new Dimension(38, 34));
        btnGear.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        btnGear.setForeground(Color.WHITE);
        btnGear.setBackground(HEADER);
        btnGear.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnGear.setFocusPainted(false);
        btnGear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGear.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            TouchThietLapCotQuanLyBanHangDialog dlg = new TouchThietLapCotQuanLyBanHangDialog(win, () -> {
                this.config = TouchQuanLyBanHangBillConfigDAO.loadConfig();
                renderBillsGrid();
            });
            dlg.setVisible(true);
        });
        gearBar.add(btnGear);
        gridWrapper.add(gearBar, BorderLayout.SOUTH);

        pnl.add(gridWrapper, BorderLayout.CENTER);

        // Thanh nút đáy bên trái (Hình 1: HỦY PHIẾU, DANH SÁCH BILL HỦY, REFRESH, THÊM HÀNG, TRẢ HÀNG)
        JPanel bottomBarLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bottomBarLeft.setOpaque(false);
        bottomBarLeft.setPreferredSize(new Dimension(0, 48));

        JButton btnHuyPhieu = createBottomBtn("HỦY PHIẾU", GRAY_BTN);
        btnHuyPhieu.addActionListener(e -> onCancelBill());
        bottomBarLeft.add(btnHuyPhieu);

        JButton btnDanhSachBillHuy = createBottomBtn("DANH SÁCH<br>BILL HỦY", PURPLE);
        btnDanhSachBillHuy.addActionListener(e -> {
            TouchDanhSachBillHuyDialog dlg = new TouchDanhSachBillHuyDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
        });
        bottomBarLeft.add(btnDanhSachBillHuy);

        JButton btnRefresh = createBottomBtn("REFRESH", GREEN);
        btnRefresh.addActionListener(e -> loadBills());
        bottomBarLeft.add(btnRefresh);

        JButton btnThemHang = createBottomBtn("THÊM<br>HÀNG", GRAY_BTN);
        btnThemHang.addActionListener(e -> onOpenThemHang(false));
        bottomBarLeft.add(btnThemHang);

        JButton btnTraHang = createBottomBtn("TRẢ<br>HÀNG", GRAY_BTN);
        btnTraHang.addActionListener(e -> onOpenThemHang(true));
        bottomBarLeft.add(btnTraHang);

        pnl.add(bottomBarLeft, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel createRightPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 205, 220), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        // Header trên: CardLayout chuyển giữa Hóa đơn bán hàng & Giao hàng
        pnlHeaderRightCards.setOpaque(false);
        pnlHeaderRightCards.add(createHoaDonHeaderFields(), "hoadon");
        pnlHeaderRightCards.add(createGiaoHangHeaderFields(), "giaohang");
        pnl.add(pnlHeaderRightCards, BorderLayout.NORTH);

        // Danh sách mặt hàng (Hình 5)
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));
        itemsListPanel.setBackground(Color.WHITE);
        JScrollPane spItems = new JScrollPane(itemsListPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spItems.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        spItems.getViewport().setBackground(Color.WHITE);
        spItems.getVerticalScrollBar().setUnitIncrement(30);
        pnl.add(spItems, BorderLayout.CENTER);

        // Khung tổng kết & Thanh nút bên phải
        JPanel bottomArea = new JPanel(new BorderLayout(0, 6));
        bottomArea.setOpaque(false);

        JPanel totalsPanel = createTotalsPanel();
        bottomArea.add(totalsPanel, BorderLayout.CENTER);

        // Nút đáy: LƯU DỮ LIỆU, LƯU & IN, THOÁT
        JPanel bottomBarRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bottomBarRight.setOpaque(false);
        bottomBarRight.setPreferredSize(new Dimension(0, 48));

        JButton btnLuu = createBottomBtn("LƯU DỮ LIỆU", GRAY_BTN);
        btnLuu.addActionListener(e -> onSaveBill());
        bottomBarRight.add(btnLuu);

        JButton btnLuuIn = createBottomBtn("LƯU & IN", GREEN);
        btnLuuIn.addActionListener(e -> onSaveAndPrintBill());
        bottomBarRight.add(btnLuuIn);

        JButton btnThoat = createBottomBtn("THOÁT", RED);
        btnThoat.addActionListener(e -> {
            if (exit != null) exit.run();
        });
        bottomBarRight.add(btnThoat);

        bottomArea.add(bottomBarRight, BorderLayout.SOUTH);
        pnl.add(bottomArea, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createHoaDonHeaderFields() {
        JPanel pnl = new JPanel(new GridLayout(3, 1, 0, 4));
        pnl.setOpaque(false);

        // Hàng 1: NGÀY [  ]   SỐ [  ]
        JPanel r1 = new JPanel(new BorderLayout(8, 0));
        r1.setOpaque(false);
        JLabel lblNgay = new JLabel("NGÀY");
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        r1.add(lblNgay, BorderLayout.WEST);
        r1.add(txtNgay, BorderLayout.CENTER);

        JPanel r1Right = new JPanel(new BorderLayout(6, 0));
        r1Right.setOpaque(false);
        JLabel lblSo = new JLabel("SỐ");
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        r1Right.add(lblSo, BorderLayout.WEST);
        txtSo.setPreferredSize(new Dimension(140, 32));
        r1Right.add(txtSo, BorderLayout.CENTER);
        r1.add(r1Right, BorderLayout.EAST);
        pnl.add(r1);

        // Hàng 2: K.HÀNG [  ]
        JPanel r2 = new JPanel(new BorderLayout(8, 0));
        r2.setOpaque(false);
        JLabel lblKhach = new JLabel("K.HÀNG");
        lblKhach.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblKhach.setPreferredSize(new Dimension(55, 0));
        r2.add(lblKhach, BorderLayout.WEST);
        r2.add(txtKhachHang, BorderLayout.CENTER);
        pnl.add(r2);

        // Hàng 3: N.VIÊN [  ]
        JPanel r3 = new JPanel(new BorderLayout(8, 0));
        r3.setOpaque(false);
        JLabel lblNv = new JLabel("N.VIÊN");
        lblNv.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNv.setPreferredSize(new Dimension(55, 0));
        r3.add(lblNv, BorderLayout.WEST);
        r3.add(txtNhanVien, BorderLayout.CENTER);
        pnl.add(r3);

        return pnl;
    }

    private JPanel createGiaoHangHeaderFields() {
        JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 4));
        pnl.setOpaque(false);

        // Hàng 1: GIAO HÀNG [  ]
        JPanel r1 = new JPanel(new BorderLayout(8, 0));
        r1.setOpaque(false);
        JLabel lblGh = new JLabel("GIAO HÀNG");
        lblGh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGh.setPreferredSize(new Dimension(110, 0));
        r1.add(lblGh, BorderLayout.WEST);
        r1.add(txtGiaoHang, BorderLayout.CENTER);
        pnl.add(r1);

        // Hàng 2: N.VIÊN GIAO [  ]
        JPanel r2 = new JPanel(new BorderLayout(8, 0));
        r2.setOpaque(false);
        JLabel lblNvGiao = new JLabel("N.VIÊN GIAO");
        lblNvGiao.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNvGiao.setPreferredSize(new Dimension(110, 0));
        r2.add(lblNvGiao, BorderLayout.WEST);
        r2.add(txtNhanVienGiao, BorderLayout.CENTER);
        pnl.add(r2);

        // Hàng 3: TRÍCH N.VIÊN [ 0 ]
        JPanel r3 = new JPanel(new BorderLayout(8, 0));
        r3.setOpaque(false);
        JLabel lblTrich = new JLabel("TRÍCH N.VIÊN");
        lblTrich.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTrich.setPreferredSize(new Dimension(110, 0));
        r3.add(lblTrich, BorderLayout.WEST);
        r3.add(txtTrichNhanVien, BorderLayout.CENTER);
        pnl.add(r3);

        // Hàng 4: GHI CHÚ [  ]
        JPanel r4 = new JPanel(new BorderLayout(8, 0));
        r4.setOpaque(false);
        JLabel lblGc = new JLabel("GHI CHÚ");
        lblGc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGc.setPreferredSize(new Dimension(110, 0));
        r4.add(lblGc, BorderLayout.WEST);
        r4.add(txtGhiChuGiaoHang, BorderLayout.CENTER);
        pnl.add(r4);

        return pnl;
    }

    private JPanel createTotalsPanel() {
        JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 4));
        pnl.setOpaque(false);
        pnl.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                new EmptyBorder(6, 4, 4, 4)
        ));

        // 1. TIỀN HÀNG
        JPanel r1 = new JPanel(new BorderLayout());
        r1.setOpaque(false);
        JLabel l1 = new JLabel("TIỀN HÀNG");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        r1.add(l1, BorderLayout.WEST);
        lblTienHang.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTienHang.setForeground(Color.BLACK);
        r1.add(lblTienHang, BorderLayout.EAST);
        pnl.add(r1);

        // 2. TỈ LỆ GIẢM GIÁ
        JPanel r2 = new JPanel(new BorderLayout(8, 0));
        r2.setOpaque(false);
        JLabel l2 = new JLabel("TỈ LỆ GIẢM GIÁ");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        r2.add(l2, BorderLayout.WEST);

        JPanel pnlGiam = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlGiam.setOpaque(false);

        lblTiLeGiamGia.setPreferredSize(new Dimension(65, 26));
        lblTiLeGiamGia.setOpaque(true);
        lblTiLeGiamGia.setBackground(FIELD_GREEN);
        lblTiLeGiamGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTiLeGiamGia.setBorder(new LineBorder(new Color(150, 210, 150), 1, true));
        pnlGiam.add(lblTiLeGiamGia);

        lblTienGiamGia.setPreferredSize(new Dimension(110, 26));
        lblTienGiamGia.setOpaque(true);
        lblTienGiamGia.setBackground(FIELD_GREEN);
        lblTienGiamGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienGiamGia.setBorder(new LineBorder(new Color(150, 210, 150), 1, true));
        pnlGiam.add(lblTienGiamGia);

        r2.add(pnlGiam, BorderLayout.EAST);
        pnl.add(r2);

        // 3. TỔNG CỘNG
        JPanel r3 = new JPanel(new BorderLayout());
        r3.setOpaque(false);
        JLabel l3 = new JLabel("TỔNG CỘNG");
        l3.setFont(new Font("Segoe UI", Font.BOLD, 15));
        r3.add(l3, BorderLayout.WEST);
        lblTongCong.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongCong.setForeground(new Color(190, 0, 0));
        r3.add(lblTongCong, BorderLayout.EAST);
        pnl.add(r3);

        // 4. THANH TOÁN
        JPanel r4 = new JPanel(new BorderLayout());
        r4.setOpaque(false);
        JLabel l4 = new JLabel("THANH TOÁN");
        l4.setFont(new Font("Segoe UI", Font.BOLD, 14));
        r4.add(l4, BorderLayout.WEST);

        lblThanhToan.setPreferredSize(new Dimension(200, 32));
        lblThanhToan.setOpaque(true);
        lblThanhToan.setBackground(FIELD_GREEN);
        lblThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblThanhToan.setBorder(new LineBorder(new Color(150, 210, 150), 1, true));
        r4.add(lblThanhToan, BorderLayout.EAST);
        pnl.add(r4);

        return pnl;
    }

    private JTextField createRoundedField(Color bg) {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 32));
        tf.setBackground(bg);
        tf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 190, 200), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        return tf;
    }

    private void styleTabButton(JButton btn, boolean active) {
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(active ? ORANGE : TAB_INACTIVE);
        btn.setBorder(new LineBorder(Color.WHITE, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JButton createBottomBtn(String htmlText, Color bg) {
        JButton btn = new JButton("<html><center>" + htmlText + "</center></html>");
        btn.setPreferredSize(new Dimension(100, 44));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    void switchTab(boolean toGiaoHang) {
        this.isGiaoHangTab = toGiaoHang;
        styleTabButton(btnTabHoaDon, !toGiaoHang);
        styleTabButton(btnTabGiaoHang, toGiaoHang);
        cardHeaderRight.show(pnlHeaderRightCards, toGiaoHang ? "giaohang" : "hoadon");
    }

    void switchTabForTest(boolean toGiaoHang) {
        switchTab(toGiaoHang);
    }

    void setSampleBillsForTest(java.util.List<DonHang> list) {
        this.billsList = list;
        if (list != null && !list.isEmpty()) {
            this.currentBill = list.get(0);
        }
        renderBillsGrid();
        renderCurrentBillDetails();
    }

    DonHang getCurrentBillForTest() {
        return currentBill;
    }

    JPanel getBillsGridPanelForTest() {
        return billsGridPanel;
    }

    private void loadBills() {
        billsList = hoaDonDAO.getDanhSachHoaDon(tuNgay, denNgay, null, null);
        if (!billsList.isEmpty()) {
            if (currentBill == null) {
                currentBill = billsList.get(0);
            } else {
                DonHang match = null;
                for (DonHang d : billsList) {
                    if ((currentBill.getId() != null && currentBill.getId().equals(d.getId()))
                            || (currentBill.getName() != null && currentBill.getName().equals(d.getName()))) {
                        match = d;
                        break;
                    }
                }
                currentBill = (match != null) ? match : billsList.get(0);
            }
        } else {
            currentBill = null;
        }
        renderBillsGrid();
        renderCurrentBillDetails();
    }

    private void selectBill(DonHang dh) {
        this.currentBill = dh;
        renderBillsGrid();
        renderCurrentBillDetails();
    }

    private void renderBillsGrid() {
        billsGridPanel.removeAll();
        billsGridPanel.setLayout(new GridLayout(0, config.getGridColumns(), 8, 8));

        if (billsList.isEmpty()) {
            JLabel lblEmpty = new JLabel("(Không có hóa đơn trong khoảng ngày này)", SwingConstants.CENTER);
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblEmpty.setForeground(Color.LIGHT_GRAY);
            billsGridPanel.add(lblEmpty);
        } else {
            for (DonHang dh : billsList) {
                JPanel card = createBillCard(dh);
                billsGridPanel.add(card);
            }
        }

        int totalRows = Math.max(config.getGridRows(), (billsList.size() + config.getGridColumns() - 1) / config.getGridColumns());
        billsGridPanel.setPreferredSize(new Dimension(0, totalRows * 85));

        billsGridPanel.revalidate();
        billsGridPanel.repaint();
    }

    private JPanel createBillCard(DonHang dh) {
        boolean isSelected = (currentBill != null && (dh == currentBill
                || (dh.getId() != null && dh.getId().equals(currentBill.getId()))
                || (dh.getName() != null && dh.getName().equals(currentBill.getName()))));
        JPanel card = new JPanel(new GridLayout(2, 2, 6, 2));
        card.setOpaque(true);
        card.setBackground(isSelected ? ORANGE : config.getCardBgColor());
        card.setBorder(new CompoundBorder(
                new LineBorder(isSelected ? Color.WHITE : new Color(0, 110, 110), isSelected ? 2 : 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        List<TouchQuanLyBanHangBillColumnConfig.ColumnItem> used = config.getUsedColumns();
        for (int i = 0; i < 4; i++) {
            if (i < used.size()) {
                TouchQuanLyBanHangBillColumnConfig.ColumnItem col = used.get(i);
                String val = TouchQuanLyBanHangBillColumnConfig.getFormattedText(dh, col, config.isShowTitle());
                JLabel lbl = new JLabel(val, col.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.LEFT);
                lbl.setFont(col.getFont());
                lbl.setForeground(col.getColor());
                card.add(lbl);
            } else {
                card.add(new JLabel(""));
            }
        }

        attachClickListener(card, () -> selectBill(dh));

        return card;
    }

    private void attachClickListener(Component comp, Runnable onClick) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onClick.run();
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                attachClickListener(child, onClick);
            }
        }
    }

    private void renderCurrentBillDetails() {
        itemsListPanel.removeAll();
        if (currentBill == null) {
            txtNgay.setText("");
            txtSo.setText("");
            txtKhachHang.setText("");
            txtNhanVien.setText("");
            txtGiaoHang.setText("");
            txtNhanVienGiao.setText("");
            txtTrichNhanVien.setText("0");
            txtGhiChuGiaoHang.setText("");
            lblTienHang.setText("0");
            lblTiLeGiamGia.setText("0 %");
            lblTienGiamGia.setText("0");
            lblTongCong.setText("0");
            lblThanhToan.setText("0");
            itemsListPanel.revalidate();
            itemsListPanel.repaint();
            return;
        }

        txtNgay.setText(currentBill.getNgay() != null ? SDF.format(currentBill.getNgay()) : "");
        txtSo.setText(currentBill.getName() != null ? currentBill.getName() : "");
        txtKhachHang.setText(currentBill.getTenKhach() != null ? currentBill.getTenKhach() : "Khách lẻ");
        txtNhanVien.setText(currentBill.getNguoiThanhToan() != null ? currentBill.getNguoiThanhToan() : (currentBill.getTenNhanVien() != null ? currentBill.getTenNhanVien() : "Administrator"));

        txtGiaoHang.setText(currentBill.getDiaChiKhach() != null ? currentBill.getDiaChiKhach() : (currentBill.getDiaChiGiaoHang() != null ? currentBill.getDiaChiGiaoHang() : ""));
        txtNhanVienGiao.setText(currentBill.getNhanVienXuatId() != null ? currentBill.getNhanVienXuatId() : "");
        txtTrichNhanVien.setText("0");
        txtGhiChuGiaoHang.setText(currentBill.getGhiChu() != null ? currentBill.getGhiChu() : "");

        // Render các mặt hàng đúng style Hình 5
        int stt = 1;
        if (currentBill.getChiTietLayHang() != null) {
            for (DonHangChiTiet ct : currentBill.getChiTietLayHang()) {
                JPanel row = createItemRow(stt++, ct);
                itemsListPanel.add(row);
                itemsListPanel.add(Box.createVerticalStrut(4));
            }
        }

        lblTienHang.setText(CurrencyUtil.format(currentBill.getTienHang()));
        lblTiLeGiamGia.setText(String.format("%.0f %%", currentBill.getTiLeGiamGia()));
        lblTienGiamGia.setText(CurrencyUtil.format(currentBill.getTienGiamGia()));
        lblTongCong.setText(CurrencyUtil.format(currentBill.getTongCong()));
        lblThanhToan.setText(CurrencyUtil.format(currentBill.getTienThanhToan()));

        itemsListPanel.revalidate();
        itemsListPanel.repaint();
    }

    private JPanel createItemRow(int index, DonHangChiTiet ct) {
        JPanel row = new JPanel(new BorderLayout(4, 2));
        row.setOpaque(true);
        row.setBackground(new Color(245, 245, 245));
        row.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(6, 8, 6, 8)
        ));

        // Dòng 1: [STT]. [Tên hàng] (trái, bold)   [Thành tiền] (phải, đỏ đậm)
        JPanel r1 = new JPanel(new BorderLayout());
        r1.setOpaque(false);
        String name = index + ". " + (ct.getTenHang() != null ? ct.getTenHang() : "");
        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.BLACK);
        r1.add(lblName, BorderLayout.WEST);

        JLabel lblTotal = new JLabel(CurrencyUtil.format(ct.getThanhTien()), SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(new Color(180, 0, 0));
        r1.add(lblTotal, BorderLayout.EAST);
        row.add(r1, BorderLayout.NORTH);

        // Dòng 2: [Số lượng] x [Đơn giá] (trái, xanh lá, bold)
        JPanel r2 = new JPanel(new BorderLayout());
        r2.setOpaque(false);
        String sub = String.format("%.0f x %s", ct.getSoLuong(), CurrencyUtil.format(ct.getDonGia()));
        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSub.setForeground(new Color(0, 110, 40));
        r2.add(lblSub, BorderLayout.WEST);
        row.add(r2, BorderLayout.SOUTH);

        return row;
    }

    private void onCancelBill() {
        if (currentBill == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reason = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), "LÝ DO HỦY PHIẾU", "Khách đổi ý");
        if (reason == null) return; // người dùng hủy bỏ

        boolean ok = hoaDonDAO.cancelHoaDon(currentBill.getId(), reason, AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUsername() : "Admin");
        if (ok) {
            JOptionPane.showMessageDialog(this, "Hủy hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadBills();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể hủy hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onOpenThemHang(boolean isTraHang) {
        if (currentBill == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc tạo hóa đơn trước khi thêm hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window win = SwingUtilities.getWindowAncestor(this);
        TouchThemHangDialog dlg = new TouchThemHangDialog(win, (mh, qty) -> {
            if (mh != null && qty > 0) {
                DonHangChiTiet ct = new DonHangChiTiet();
                ct.setMatHangId(mh.getId());
                ct.setMaHang(mh.getCode());
                ct.setTenHang(mh.getName());
                ct.setTenDonViTinh(mh.getTenDonViTinh());
                ct.setDonGia(mh.getGiaBan());
                ct.setSoLuong(qty);
                ct.setThanhTien(qty * mh.getGiaBan());
                ct.setTraHang(isTraHang);

                if (isTraHang) {
                    if (currentBill.getChiTietTraHang() == null) currentBill.setChiTietTraHang(new ArrayList<>());
                    currentBill.getChiTietTraHang().add(ct);
                } else {
                    if (currentBill.getChiTietLayHang() == null) currentBill.setChiTietLayHang(new ArrayList<>());
                    currentBill.getChiTietLayHang().add(ct);
                }

                // Tính toán lại tổng tiền
                recalcBillTotals(currentBill);
                renderCurrentBillDetails();
            }
        });
        dlg.setVisible(true);
    }

    private void recalcBillTotals(DonHang dh) {
        double tienHang = 0;
        if (dh.getChiTietLayHang() != null) {
            for (DonHangChiTiet c : dh.getChiTietLayHang()) {
                tienHang += c.getThanhTien();
            }
        }
        dh.setTienHang(tienHang);
        double giam = tienHang * (dh.getTiLeGiamGia() / 100.0);
        dh.setTienGiamGia(giam);
        double tong = tienHang - giam + dh.getPhiVanChuyen();
        dh.setTongCong(tong);
        dh.setTienThanhToan(tong);
    }

    private void onSaveBill() {
        if (currentBill == null) return;
        currentBill.setGhiChu(txtGhiChuGiaoHang.getText().trim());
        boolean ok = hoaDonDAO.updateHoaDon(currentBill);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadBills();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSaveAndPrintBill() {
        onSaveBill();
        JOptionPane.showMessageDialog(this, "Đang gửi lệnh in bill tới máy in nhiệt POS...", "In hóa đơn", JOptionPane.INFORMATION_MESSAGE);
    }
}
