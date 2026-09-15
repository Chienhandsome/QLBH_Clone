package kazuko.ex.ui.touch;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.service.AuthService;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.hoadon.ChonMauInDialog;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Màn hình cảm ứng "THỐNG KÊ - TÀI KHOẢN: <TÊN_TÀI_KHOẢN>" chuẩn 100% Hình 1.
 * - Header: Tiêu đề thống kê theo tài khoản đang đăng nhập, ô ngày cảm ứng NGÀY dd/MM/yyyy.
 * - Các chỉ số tài chính dạng pill vàng nhạt:
 *   Hàng 1: TIỀN MẶT, CHUYỂN KHOẢN, TRỪ TÍCH LŨY, VOUCHER
 *   Hàng 2: THU CHI, ATM, THẺ TRẢ TRƯỚC, CÔNG NỢ
 *   Hàng 3: 3 Tab (HÓA ĐƠN, THU CHI, MẶT HÀNG) và ô TỔNG
 * - Vùng danh sách trung tâm (khung bo góc xám nhạt) + nút bánh răng [⚙] góc dưới phải.
 * - Thanh công cụ đáy: IN BÁO CÁO KẾT CA (đỏ sẫm), IN LẠI (xám), XEM HÓA ĐƠN ĐANG CHỌN (xám), THOÁT (đỏ).
 */
public class TouchThongKeBanHangDialog extends JDialog {

    private static final Color BG_DARK = new Color(8, 61, 86);         // #083D56
    private static final Color HEADER_BG = new Color(74, 130, 158);     // #4A829E
    private static final Color PILL_BG = new Color(255, 253, 224);      // #FFFDE0
    private static final Color TAB_ACTIVE_BG = new Color(0, 136, 255);  // #0088FF
    private static final Color TAB_INACTIVE_BG = new Color(92, 119, 133); // #5C7785
    private static final Color CENTER_CONTAINER_BG = new Color(227, 230, 232); // #E3E6E8

    private static final Color BTN_PRINT_SHIFT_BG = new Color(90, 16, 16);  // #5A1010
    private static final Color BTN_GRAY_BG = new Color(140, 155, 165);       // #8C9BA5
    private static final Color BTN_EXIT_BG = new Color(139, 0, 0);          // #8B0000

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();

    private Date selectedDate = new Date();

    // Financial Metric Labels
    private JLabel lblTienMat;
    private JLabel lblChuyenKhoan;
    private JLabel lblTruTichLuy;
    private JLabel lblVoucher;
    private JLabel lblThuChi;
    private JLabel lblATM;
    private JLabel lblTheTraTruoc;
    private JLabel lblCongNo;
    private JLabel lblTong;

    private JLabel lblDateValue;

    // Tabs
    private JButton btnTabHoaDon;
    private JButton btnTabThuChi;
    private JButton btnTabMatHang;
    private CardLayout centerCardLayout;
    private JPanel centerCardPanel;

    // Tables
    private JTable tableHoaDon;
    private DefaultTableModel modelHoaDon;
    private JTable tableThuChi;
    private DefaultTableModel modelThuChi;
    private JTable tableMatHang;
    private DefaultTableModel modelMatHang;

    private List<DonHang> currentHoaDonList = new ArrayList<>();
    private List<PhieuThu> currentPhieuThuList = new ArrayList<>();
    private List<PhieuChi> currentPhieuChiList = new ArrayList<>();

    public TouchThongKeBanHangDialog(Window parent) {
        super(parent, "THỐNG KÊ", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        initComponents();
        loadDataForDate(selectedDate);
    }

    private void initComponents() {
        setSize(1024, 720);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(BG_DARK);
        root.setBorder(new LineBorder(HEADER_BG, 3));

        // 1. Header: THỐNG KÊ - TÀI KHOẢN: ... + Ô NGÀY
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Thân chính: Bảng metrics + Khung trung tâm
        JPanel pnlBody = new JPanel(new BorderLayout(0, 8));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(0, 14, 8, 14));

        pnlBody.add(createMetricsAndTabsPanel(), BorderLayout.NORTH);
        pnlBody.add(createCenterContentPanel(), BorderLayout.CENTER);

        root.add(pnlBody, BorderLayout.CENTER);

        // 3. Thanh tác vụ đáy
        root.add(createBottomBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    /**
     * Header Slate Blue có tên tài khoản và ô chọn ngày
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(1024, 46));
        header.setBorder(new EmptyBorder(4, 16, 4, 16));

        String username = AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getName() != null
                ? AuthService.getCurrentUser().getName().toUpperCase()
                : "ADMINISTRATOR";

        JLabel lblTitle = new JLabel("THỐNG KÊ - TÀI KHOẢN: " + username, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // Góc phải: Nhãn NGÀY + Ô pill vàng
        JPanel pnlDate = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        pnlDate.setOpaque(false);

        JLabel lblNgay = new JLabel("NGÀY");
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNgay.setForeground(Color.WHITE);
        pnlDate.add(lblNgay);

        lblDateValue = new JLabel(DateUtil.formatDate(selectedDate), SwingConstants.CENTER);
        lblDateValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDateValue.setForeground(new Color(30, 30, 30));
        lblDateValue.setBackground(PILL_BG);
        lblDateValue.setOpaque(true);
        lblDateValue.setPreferredSize(new Dimension(120, 30));
        lblDateValue.setBorder(new LineBorder(new Color(200, 195, 150), 1, true));
        lblDateValue.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblDateValue.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onPickDate();
            }
        });
        pnlDate.add(lblDateValue);

        header.add(pnlDate, BorderLayout.EAST);

        return header;
    }

    /**
     * Bảng các chỉ số tài chính (2 hàng pill) + Hàng 3 (3 tab bên trái, ô TỔNG bên phải)
     */
    private JPanel createMetricsAndTabsPanel() {
        JPanel pnl = new JPanel(new GridLayout(3, 1, 0, 6));
        pnl.setOpaque(false);

        // Hàng 1: TIỀN MẶT, CHUYỂN KHOẢN, TRỪ TÍCH LŨY, VOUCHER
        JPanel row1 = new JPanel(new GridLayout(1, 4, 12, 0));
        row1.setOpaque(false);
        lblTienMat = createMetricPill(row1, "TIỀN MẶT");
        lblChuyenKhoan = createMetricPill(row1, "CHUYỂN KHOẢN");
        lblTruTichLuy = createMetricPill(row1, "TRỪ TÍCH LŨY");
        lblVoucher = createMetricPill(row1, "VOUCHER");
        pnl.add(row1);

        // Hàng 2: THU CHI, ATM, THẺ TRẢ TRƯỚC, CÔNG NỢ
        JPanel row2 = new JPanel(new GridLayout(1, 4, 12, 0));
        row2.setOpaque(false);
        lblThuChi = createMetricPill(row2, "THU CHI");
        lblATM = createMetricPill(row2, "ATM");
        lblTheTraTruoc = createMetricPill(row2, "THẺ TRẢ TRƯỚC");
        lblCongNo = createMetricPill(row2, "CÔNG NỢ");
        pnl.add(row2);

        // Hàng 3: 3 Tabs (Trái) + TỔNG (Phải - thẳng 2 cột cuối)
        JPanel row3 = new JPanel(new BorderLayout(12, 0));
        row3.setOpaque(false);

        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTabs.setOpaque(false);

        btnTabHoaDon = createTabButton("HÓA ĐƠN", true);
        btnTabThuChi = createTabButton("THU CHI", false);
        btnTabMatHang = createTabButton("MẶT HÀNG", false);

        btnTabHoaDon.addActionListener(e -> switchTab("HOA_DON"));
        btnTabThuChi.addActionListener(e -> switchTab("THU_CHI"));
        btnTabMatHang.addActionListener(e -> switchTab("MAT_HANG"));

        pnlTabs.add(btnTabHoaDon);
        pnlTabs.add(btnTabThuChi);
        pnlTabs.add(btnTabMatHang);
        row3.add(pnlTabs, BorderLayout.WEST);

        // Ô TỔNG bên phải
        JPanel pnlTong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTong.setOpaque(false);

        JLabel lblLblTong = new JLabel("TỔNG");
        lblLblTong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLblTong.setForeground(Color.WHITE);
        pnlTong.add(lblLblTong);

        lblTong = new JLabel("0", SwingConstants.CENTER);
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTong.setForeground(new Color(170, 0, 0));
        lblTong.setBackground(PILL_BG);
        lblTong.setOpaque(true);
        lblTong.setPreferredSize(new Dimension(110, 28));
        lblTong.setBorder(new LineBorder(new Color(200, 195, 150), 1, true));
        pnlTong.add(lblTong);

        row3.add(pnlTong, BorderLayout.EAST);

        pnl.add(row3);

        return pnl;
    }

    private JLabel createMetricPill(JPanel parent, String title) {
        JPanel item = new JPanel(new BorderLayout(6, 0));
        item.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.WHITE);
        item.add(lblTitle, BorderLayout.WEST);

        JLabel lblValue = new JLabel("0", SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValue.setForeground(new Color(170, 0, 0));
        lblValue.setBackground(PILL_BG);
        lblValue.setOpaque(true);
        lblValue.setPreferredSize(new Dimension(95, 28));
        lblValue.setBorder(new LineBorder(new Color(200, 195, 150), 1, true));
        item.add(lblValue, BorderLayout.CENTER);

        parent.add(item);
        return lblValue;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(active ? TAB_ACTIVE_BG : TAB_INACTIVE_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 18, 6, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Khung trung tâm bo góc xám nhạt với 3 view CardLayout + nút bánh răng [⚙]
     */
    private JPanel createCenterContentPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(CENTER_CONTAINER_BG);
        container.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 190, 200), 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));

        centerCardLayout = new CardLayout();
        centerCardPanel = new JPanel(centerCardLayout);
        centerCardPanel.setOpaque(false);

        // 1. View Hóa đơn
        centerCardPanel.add(createHoaDonView(), "HOA_DON");

        // 2. View Thu chi
        centerCardPanel.add(createThuChiView(), "THU_CHI");

        // 3. View Mặt hàng
        centerCardPanel.add(createMatHangView(), "MAT_HANG");

        container.add(centerCardPanel, BorderLayout.CENTER);

        // Góc dưới phải: Nút bánh răng [⚙]
        JPanel pnlGear = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        pnlGear.setOpaque(false);

        JButton btnGear = createGearButton();
        btnGear.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Cấu hình giao diện thống kê bán hàng và máy in kết ca.",
                "Cấu hình", JOptionPane.INFORMATION_MESSAGE));
        pnlGear.add(btnGear);

        container.add(pnlGear, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createHoaDonView() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        String[] cols = {"STT", "Số HĐ", "Giờ", "Khách hàng", "Tiền hàng", "Giảm giá", "Tổng cộng", "Đã trả", "Còn nợ", "Hình thức TT"};
        modelHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tableHoaDon = new JTable(modelHoaDon);
        setupTouchTableStyle(tableHoaDon);

        JScrollPane scroll = new JScrollPane(tableHoaDon);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createThuChiView() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        String[] cols = {"STT", "Số phiếu", "Loại", "Ngày", "Đối tượng", "Số tiền", "Lý do thu / chi", "Người lập"};
        modelThuChi = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tableThuChi = new JTable(modelThuChi);
        setupTouchTableStyle(tableThuChi);

        JScrollPane scroll = new JScrollPane(tableThuChi);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createMatHangView() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        String[] cols = {"STT", "Mã hàng", "Tên mặt hàng", "ĐVT", "Số lượng bán", "Đơn giá TB", "Doanh số"};
        modelMatHang = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tableMatHang = new JTable(modelMatHang);
        setupTouchTableStyle(tableMatHang);

        JScrollPane scroll = new JScrollPane(tableMatHang);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private void setupTouchTableStyle(JTable table) {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(200, 230, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(225, 230, 235));
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(240, 243, 246));
        header.setForeground(new Color(30, 50, 70));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    /**
     * Thanh tác vụ chân trang
     */
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(4, 14, 10, 14));

        // Trái: IN BÁO CÁO KẾT CA, IN LẠI, XEM HÓA ĐƠN ĐANG CHỌN
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);

        JButton btnKetCa = createActionButton("IN BÁO CÁO KẾT CA", BTN_PRINT_SHIFT_BG, e -> onPrintShiftReport());
        JButton btnInLai = createActionButton("IN LẠI", BTN_GRAY_BG, e -> onRePrintInvoice());
        JButton btnXemHD = createActionButton("XEM HÓA ĐƠN ĐANG CHỌN", BTN_GRAY_BG, e -> onViewSelectedInvoice());

        pnlLeft.add(btnKetCa);
        pnlLeft.add(btnInLai);
        pnlLeft.add(btnXemHD);

        bar.add(pnlLeft, BorderLayout.WEST);

        // Phải: THOÁT
        JButton btnThoat = createActionButton("THOÁT", BTN_EXIT_BG, e -> dispose());
        bar.add(btnThoat, BorderLayout.EAST);

        return bar;
    }

    private JButton createActionButton(String text, Color bg, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    private void switchTab(String tabKey) {
        btnTabHoaDon.setBackground("HOA_DON".equals(tabKey) ? TAB_ACTIVE_BG : TAB_INACTIVE_BG);
        btnTabThuChi.setBackground("THU_CHI".equals(tabKey) ? TAB_ACTIVE_BG : TAB_INACTIVE_BG);
        btnTabMatHang.setBackground("MAT_HANG".equals(tabKey) ? TAB_ACTIVE_BG : TAB_INACTIVE_BG);

        centerCardLayout.show(centerCardPanel, tabKey);
    }

    private void onPickDate() {
        String input = JOptionPane.showInputDialog(this, "Nhập ngày thống kê (dd/MM/yyyy):", DateUtil.formatDate(selectedDate));
        if (input != null && !input.trim().isEmpty()) {
            Date d = DateUtil.parseDate(input.trim());
            if (d != null) {
                selectedDate = d;
                lblDateValue.setText(DateUtil.formatDate(selectedDate));
                loadDataForDate(selectedDate);
            } else {
                JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ (cần dd/MM/yyyy)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadDataForDate(Date date) {
        this.selectedDate = date != null ? date : new Date();
        lblDateValue.setText(DateUtil.formatDate(this.selectedDate));

        // Start & end of day
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.selectedDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDay = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDay = cal.getTime();

        // 1. Tải Hóa đơn
        currentHoaDonList = hoaDonDAO.getDanhSachHoaDon(startDay, endDay, null, null);
        if (currentHoaDonList == null) currentHoaDonList = new ArrayList<>();

        double sumTienMat = 0;
        double sumChuyenKhoan = 0;
        double sumTruTichLuy = 0;
        double sumVoucher = 0;
        double sumATM = 0;
        double sumTheTraTruoc = 0;
        double sumCongNo = 0;
        double sumTongCong = 0;

        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        modelHoaDon.setRowCount(0);
        int stt = 1;
        for (DonHang dh : currentHoaDonList) {
            double tm = dh.getTienMat();
            double ck = dh.getTienChuyenKhoan();
            double atm = dh.getTienTheATM();
            double vc = dh.getTienVoucher();
            double ttt = dh.getTienTheTraTruoc();
            double ttl = dh.getTienTruTichLuy();
            double no = dh.getConLai();
            double tong = dh.getTongCong();

            // Nếu tiền mặt = 0 nhưng có thanh toán và không phải qua thẻ/ck:
            if (tm == 0 && dh.getTienThanhToan() > 0 && atm == 0 && ck == 0) {
                tm = dh.getTienThanhToan();
            }

            sumTienMat += tm;
            sumChuyenKhoan += ck;
            sumATM += atm;
            sumVoucher += vc;
            sumTheTraTruoc += ttt;
            sumTruTichLuy += ttl;
            sumCongNo += no;
            sumTongCong += tong;

            String pttt = "Tiền mặt";
            if (ck > 0) pttt = "Chuyển khoản";
            else if (atm > 0) pttt = "Thẻ ATM/Visa";

            String timeStr = dh.getGioThanhToan() != null ? dh.getGioThanhToan()
                    : (dh.getTimeCreated() != null ? sdfTime.format(dh.getTimeCreated()) : "");

            modelHoaDon.addRow(new Object[]{
                    stt++,
                    dh.getName(),
                    timeStr,
                    dh.getTenKhach() != null ? dh.getTenKhach() : "Khách lẻ",
                    CurrencyUtil.formatMoney(dh.getTienHang()),
                    CurrencyUtil.formatMoney(dh.getTienGiamGia()),
                    CurrencyUtil.formatMoney(tong),
                    CurrencyUtil.formatMoney(dh.getTienThanhToan()),
                    CurrencyUtil.formatMoney(no),
                    pttt
            });
        }

        // 2. Tải Thu chi
        currentPhieuThuList = phieuThuDAO.getAll();
        currentPhieuChiList = phieuChiDAO.getAll();
        modelThuChi.setRowCount(0);
        int sttTC = 1;
        double sumThu = 0;
        double sumChi = 0;

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        String curDateStr = sdfDate.format(selectedDate);

        if (currentPhieuThuList != null) {
            for (PhieuThu pt : currentPhieuThuList) {
                if (pt.getNgay() != null && curDateStr.equals(sdfDate.format(pt.getNgay()))) {
                    sumThu += pt.getSoTien();
                    modelThuChi.addRow(new Object[]{
                            sttTC++,
                            pt.getSoPhieu(),
                            "Phiếu Thu",
                            DateUtil.formatDate(pt.getNgay()),
                            pt.getTenDoiTuong() != null ? pt.getTenDoiTuong() : pt.getTenKhachHang(),
                            "+" + CurrencyUtil.formatMoney(pt.getSoTien()),
                            pt.getLyDoThu(),
                            pt.getUserCreatedId() != null ? pt.getUserCreatedId() : "Admin"
                    });
                }
            }
        }
        if (currentPhieuChiList != null) {
            for (PhieuChi pc : currentPhieuChiList) {
                if (pc.getNgay() != null && curDateStr.equals(sdfDate.format(pc.getNgay()))) {
                    sumChi += pc.getSoTien();
                    modelThuChi.addRow(new Object[]{
                            sttTC++,
                            pc.getSoPhieu(),
                            "Phiếu Chi",
                            DateUtil.formatDate(pc.getNgay()),
                            pc.getTenDoiTuong() != null ? pc.getTenDoiTuong() : pc.getTenNhaCungCap(),
                            "-" + CurrencyUtil.formatMoney(pc.getSoTien()),
                            pc.getLyDoChi(),
                            pc.getUserCreatedId() != null ? pc.getUserCreatedId() : "Admin"
                    });
                }
            }
        }

        double netThuChi = sumThu - sumChi;

        // 3. Tải Mặt hàng bán trong ngày
        modelMatHang.setRowCount(0);
        Map<String, ItemAgg> aggMap = new LinkedHashMap<>();
        for (DonHang dh : currentHoaDonList) {
            DonHang fullDh = hoaDonDAO.getHoaDonById(dh.getId());
            if (fullDh != null && fullDh.getChiTietLayHang() != null) {
                for (DonHangChiTiet ct : fullDh.getChiTietLayHang()) {
                    String key = ct.getMatHangId() != null ? ct.getMatHangId() : ct.getTenHang();
                    ItemAgg agg = aggMap.computeIfAbsent(key, k -> new ItemAgg(ct.getMaHang(), ct.getTenHang(), ct.getTenDonViTinh()));
                    agg.soLuong += ct.getSoLuong();
                    agg.thanhTien += ct.getThanhTien();
                }
            }
        }
        int sttMH = 1;
        for (ItemAgg item : aggMap.values()) {
            double avgPrice = item.soLuong > 0 ? (item.thanhTien / item.soLuong) : 0;
            modelMatHang.addRow(new Object[]{
                    sttMH++,
                    item.maHang,
                    item.tenHang,
                    item.dvt,
                    CurrencyUtil.formatQuantity(item.soLuong),
                    CurrencyUtil.formatMoney(avgPrice),
                    CurrencyUtil.formatMoney(item.thanhTien)
            });
        }

        // Cập nhật các nhãn pill
        lblTienMat.setText(CurrencyUtil.formatMoney(sumTienMat));
        lblChuyenKhoan.setText(CurrencyUtil.formatMoney(sumChuyenKhoan));
        lblTruTichLuy.setText(CurrencyUtil.formatMoney(sumTruTichLuy));
        lblVoucher.setText(CurrencyUtil.formatMoney(sumVoucher));
        lblThuChi.setText(CurrencyUtil.formatMoney(netThuChi));
        lblATM.setText(CurrencyUtil.formatMoney(sumATM));
        lblTheTraTruoc.setText(CurrencyUtil.formatMoney(sumTheTraTruoc));
        lblCongNo.setText(CurrencyUtil.formatMoney(sumCongNo));
        lblTong.setText(CurrencyUtil.formatMoney(sumTongCong));
    }

    private void onPrintShiftReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BÁO CÁO KẾT CA BÁN HÀNG ===\n");
        sb.append("Ngày: ").append(DateUtil.formatDate(selectedDate)).append("\n");
        sb.append("Nhân viên: ").append(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getName() : "Administrator").append("\n");
        sb.append("Số hóa đơn: ").append(currentHoaDonList.size()).append("\n");
        sb.append("--------------------------------\n");
        sb.append("Tiền mặt: ").append(lblTienMat.getText()).append("\n");
        sb.append("Chuyển khoản: ").append(lblChuyenKhoan.getText()).append("\n");
        sb.append("ATM/Visa: ").append(lblATM.getText()).append("\n");
        sb.append("Thu chi chênh lệch: ").append(lblThuChi.getText()).append("\n");
        sb.append("Công nợ: ").append(lblCongNo.getText()).append("\n");
        sb.append("TỔNG DOANH THU: ").append(lblTong.getText()).append("\n");
        sb.append("================================\n");

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "In báo cáo kết ca", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onRePrintInvoice() {
        int sel = tableHoaDon.getSelectedRow();
        if (sel < 0 || sel >= currentHoaDonList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn cần in lại!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DonHang dh = currentHoaDonList.get(sel);
        DonHang fullDh = hoaDonDAO.getHoaDonById(dh.getId());
        if (fullDh == null) fullDh = dh;

        ChonMauInDialog printDlg = new ChonMauInDialog(this, fullDh);
        printDlg.setVisible(true);
    }

    private void onViewSelectedInvoice() {
        int sel = tableHoaDon.getSelectedRow();
        if (sel < 0 || sel >= currentHoaDonList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DonHang dh = currentHoaDonList.get(sel);
        DonHang fullDh = hoaDonDAO.getHoaDonById(dh.getId());
        if (fullDh == null) fullDh = dh;

        StringBuilder sb = new StringBuilder();
        sb.append("Số HĐ: ").append(fullDh.getName()).append("\n");
        sb.append("Khách hàng: ").append(fullDh.getTenKhach()).append("\n");
        sb.append("Ngày: ").append(DateUtil.formatDate(fullDh.getNgay())).append("\n");
        sb.append("Tổng tiền: ").append(CurrencyUtil.formatMoney(fullDh.getTongCong())).append("\n");
        sb.append("----------------------------------------------------\n");
        if (fullDh.getChiTietLayHang() != null) {
            for (DonHangChiTiet ct : fullDh.getChiTietLayHang()) {
                sb.append(String.format("- %s (x%s %s): %s\n",
                        ct.getTenHang(),
                        CurrencyUtil.formatQuantity(ct.getSoLuong()),
                        ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                        CurrencyUtil.formatMoney(ct.getThanhTien())));
            }
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Chi tiết hóa đơn " + fullDh.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createGearButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(new Color(50, 70, 90));
                int numTeeth = 8;
                int rOuter = 10;
                int rInner = 7;
                int hole = 4;
                for (int i = 0; i < numTeeth; i++) {
                    double angle = i * 2 * Math.PI / numTeeth;
                    int tx = cx + (int) (rOuter * Math.cos(angle));
                    int ty = cy + (int) (rOuter * Math.sin(angle));
                    g2.fillRect(tx - 2, ty - 2, 4, 4);
                }
                g2.fillOval(cx - rInner, cy - rInner, rInner * 2, rInner * 2);
                g2.setColor(getBackground());
                g2.fillOval(cx - hole, cy - hole, hole * 2, hole * 2);
                g2.dispose();
            }
        };
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(190, 200, 210), 1, true));
        btn.setPreferredSize(new Dimension(34, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class ItemAgg {
        String maHang;
        String tenHang;
        String dvt;
        double soLuong = 0;
        double thanhTien = 0;

        ItemAgg(String maHang, String tenHang, String dvt) {
            this.maHang = maHang != null ? maHang : "";
            this.tenHang = tenHang != null ? tenHang : "";
            this.dvt = dvt != null ? dvt : "";
        }
    }
}