package kazuko.ex.ui.touch;

import kazuko.ex.dao.KhachDatHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.dathang.KhachDatHangChiTiet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Phân hệ Cảm ứng Khách Đặt Hàng (TouchKhachDatHangPanel)
 * Chuẩn 100% theo Hình 1 (Danh mục đặt hàng) và Hình 4 (Form đặt hàng).
 */
public class TouchKhachDatHangPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color DARK_GREEN = new Color(0, 80, 32);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(211, 84, 0);
    private static final Color OLIVE_BAR = new Color(108, 137, 30);
    private static final Color GRAY_BTN = new Color(135, 145, 150);
    private static final Color YELLOW_FIELD_BG = new Color(254, 255, 204);
    private static final Color LIST_BG = new Color(230, 230, 230); // Nền xám như Hình 1 & 4

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private final KhachDatHangDAO datHangDAO = new KhachDatHangDAO();
    private final Runnable exit;

    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);

    // ==========================================
    // DỮ LIỆU & BỘ LỌC DANH MỤC (HÌNH 1)
    // ==========================================
    private final JTextField txtFilterSoPhieu = createInputBox();
    private final JTextField txtFilterTuNgay = createInputBox();
    private final JTextField txtFilterDenNgay = createInputBox();
    private final JTextField txtFilterKhachHang = createInputBox();
    private String selectedFilterKhachHangId = null;

    private Date filterTuNgay;
    private Date filterDenNgay;

    private List<KhachDatHang> orderList = new ArrayList<>();
    private KhachDatHang selectedOrder = null;
    private int currentOrderIndex = -1;
    private final JPanel orderCardsGrid = new JPanel();

    // ==========================================
    // DỮ LIỆU FORM ĐẶT HÀNG (HÌNH 4)
    // ==========================================
    private KhachDatHang currentOrder = null;
    private List<KhachDatHangChiTiet> orderItems = new ArrayList<>();
    private KhachDatHangChiTiet selectedItem = null;

    // Các trường master bên trái (Form Hình 4)
    private final JTextField txtNgay = createInputBox();
    private final JTextField txtSo = createInputBox();
    private final JTextField txtMaKhach = createInputBox();
    private final JTextField txtTenKhach = createInputBox();
    private final JTextField txtDiaChi = createInputBox();
    private final JTextField txtDienThoai = createInputBox();
    private final JTextField txtEmail = createInputBox();
    private final JTextField txtGhiChu = createInputBox();
    private Date orderDate = new Date();

    // Các trường master bên phải (Form Hình 4)
    private final JLabel lblTienHang = createMoneyDisplayLabel();
    private final JLabel lblGiamGiaPct = createMoneyInputLabel("0");
    private final JLabel lblGiamGiaTien = createMoneyInputLabel("0");
    private final JLabel lblThuePct = createMoneyInputLabel("0");
    private final JLabel lblThueTien = createMoneyDisplayLabel();
    private final JLabel lblPhiVanChuyen = createMoneyInputLabel("0");
    private final JLabel lblTongCong = createMoneyDisplayLabel();
    private final JLabel lblDatTruoc = createMoneyInputLabel("0");

    private double valTienHang = 0.0;
    private double valGiamGiaPct = 0.0;
    private double valGiamGiaTien = 0.0;
    private double valThuePct = 0.0;
    private double valThueTien = 0.0;
    private double valPhiVanChuyen = 0.0;
    private double valTongCong = 0.0;
    private double valDatTruoc = 0.0;

    private final JPanel itemCardsGrid = new JPanel();
    private TouchDatHangItemColumnConfig itemColumnConfig;

    public TouchKhachDatHangPanel(Runnable exit) {
        this.exit = exit;
        this.itemColumnConfig = TouchDatHangItemConfigDAO.loadConfig();

        setLayout(new BorderLayout());
        initDefaultDates();

        screens.add(createCatalogueScreen(), "catalogue");
        screens.add(createFormScreen(), "form");

        add(screens, BorderLayout.CENTER);
        showCatalogueScreen();
    }

    private void initDefaultDates() {
        Calendar cal = Calendar.getInstance();
        filterDenNgay = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        filterTuNgay = cal.getTime();

        txtFilterTuNgay.setText(SDF.format(filterTuNgay));
        txtFilterDenNgay.setText(SDF.format(filterDenNgay));
    }

    // =========================================================================
    // MÀN HÌNH 1: DANH MỤC ĐẶT HÀNG (HÌNH 1)
    // =========================================================================
    private JPanel createCatalogueScreen() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG);

        // 1. CỘT BỘ LỌC TRÁI (LEFT PANEL)
        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(270, 0));
        leftPanel.setBorder(new EmptyBorder(8, 8, 8, 4));

        // Nút đỏ đỉnh: DANH MỤC ĐẶT HÀNG
        JButton btnTitle = new JButton("DANH MỤC ĐẶT HÀNG");
        btnTitle.setPreferredSize(new Dimension(254, 44));
        btnTitle.setBackground(RED);
        btnTitle.setForeground(Color.WHITE);
        btnTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnTitle.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnTitle.setFocusPainted(false);
        leftPanel.add(btnTitle, BorderLayout.NORTH);

        // Khung lọc nội dung (Nền xanh xám bo tròn)
        JPanel filterBox = new JPanel(new GridBagLayout());
        filterBox.setOpaque(true);
        filterBox.setBackground(HEADER);
        filterBox.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 2, 4, 2);
        gbc.weightx = 1.0;

        // Label: Số phiếu
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        filterBox.add(createFilterLabel("Số phiếu"), gbc);

        // Row: txtFilterSoPhieu + Nút bàn phím
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.8;
        filterBox.add(txtFilterSoPhieu, gbc);

        JButton btnKeyboardSoPhieu = createIconButton(TouchIcons.keyboard(24, 24, Color.WHITE), DARK_GREEN);
        btnKeyboardSoPhieu.addActionListener(e -> {
            String val = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), "NHẬP SỐ PHIẾU CẦN TÌM", txtFilterSoPhieu.getText());
            if (val != null) {
                txtFilterSoPhieu.setText(val);
                loadOrderCatalogue();
            }
        });
        gbc.gridx = 1; gbc.weightx = 0.2;
        filterBox.add(btnKeyboardSoPhieu, gbc);

        // Label: Ngày
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        filterBox.add(createFilterLabel("Ngày"), gbc);

        // Khung ngày TỪ / ĐẾN + Nút mũi tên lớn [▼] chọn nhanh ngày
        JPanel dateFilterRow = new JPanel(new BorderLayout(4, 0));
        dateFilterRow.setOpaque(false);

        JPanel datesBox = new JPanel(new GridLayout(2, 1, 0, 4));
        datesBox.setOpaque(false);

        // Row TỪ:
        JPanel rowTu = new JPanel(new BorderLayout(4, 0));
        rowTu.setOpaque(false);
        JLabel lblTu = new JLabel("TỪ:");
        lblTu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTu.setForeground(Color.WHITE);
        rowTu.add(lblTu, BorderLayout.WEST);
        txtFilterTuNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtFilterTuNgay.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { pickFilterTuNgay(); }
        });
        rowTu.add(txtFilterTuNgay, BorderLayout.CENTER);
        datesBox.add(rowTu);

        // Row ĐẾN:
        JPanel rowDen = new JPanel(new BorderLayout(4, 0));
        rowDen.setOpaque(false);
        JLabel lblDen = new JLabel("ĐẾN:");
        lblDen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDen.setForeground(Color.WHITE);
        rowDen.add(lblDen, BorderLayout.WEST);
        txtFilterDenNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtFilterDenNgay.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { pickFilterDenNgay(); }
        });
        rowDen.add(txtFilterDenNgay, BorderLayout.CENTER);
        datesBox.add(rowDen);

        dateFilterRow.add(datesBox, BorderLayout.CENTER);

        // Nút mũi tên lớn [▼] dọc mở TouchChonNhanhNgayDialog (Hình 3)
        JButton btnChonNhanhNgay = new JButton(TouchIcons.downArrow(28, 28, Color.WHITE));
        btnChonNhanhNgay.setPreferredSize(new Dimension(42, 68));
        btnChonNhanhNgay.setBackground(TEAL);
        btnChonNhanhNgay.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnChonNhanhNgay.setFocusPainted(false);
        btnChonNhanhNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChonNhanhNgay.addActionListener(e -> {
            TouchChonNhanhNgayDialog.showDialog(SwingUtilities.getWindowAncestor(this), (from, to) -> {
                filterTuNgay = from;
                filterDenNgay = to;
                txtFilterTuNgay.setText(from != null ? SDF.format(from) : "");
                txtFilterDenNgay.setText(to != null ? SDF.format(to) : "");
                loadOrderCatalogue();
            });
        });
        dateFilterRow.add(btnChonNhanhNgay, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        filterBox.add(dateFilterRow, gbc);

        // Label: Khách hàng
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        filterBox.add(createFilterLabel("Khách hàng"), gbc);

        // Row: txtFilterKhachHang + Nút kính lúp [🔍]
        gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0.8;
        txtFilterKhachHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtFilterKhachHang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openCustomerSearchForFilter(); }
        });
        filterBox.add(txtFilterKhachHang, gbc);

        JButton btnSearchKhach = createIconButton(TouchIcons.search(22, 22, Color.WHITE), TEAL);
        btnSearchKhach.addActionListener(e -> openCustomerSearchForFilter());
        gbc.gridx = 1; gbc.weightx = 0.2;
        filterBox.add(btnSearchKhach, gbc);

        // Khoảng trống co giãn bên dưới bộ lọc
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.weighty = 1.0;
        filterBox.add(Box.createGlue(), gbc);

        leftPanel.add(filterBox, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.WEST);

        // 2. KHU VỰC CHÍNH BÊN PHẢI (MAIN CATALOGUE)
        JPanel mainArea = new JPanel(new BorderLayout(0, 6));
        mainArea.setOpaque(false);
        mainArea.setBorder(new EmptyBorder(8, 4, 8, 8));

        // Thanh thao tác trên đỉnh (Top Action Bar): THÊM MỚI, CHỈNH SỬA, XÓA, REFRESH, TÌM
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 44));

        JButton btnThemMoi = createToolbarButton("THÊM MỚI", GREEN, 115);
        btnThemMoi.addActionListener(e -> createNewOrder());
        topBar.add(btnThemMoi);

        JButton btnChinhSua = createToolbarButton("CHỈNH SỬA", GRAY_BTN, 115);
        btnChinhSua.addActionListener(e -> editSelectedOrder());
        topBar.add(btnChinhSua);

        JButton btnXoa = createToolbarButton("XÓA", GRAY_BTN, 95);
        btnXoa.addActionListener(e -> deleteSelectedOrder());
        topBar.add(btnXoa);

        JButton btnRefresh = createToolbarButton("REFRESH", GREEN, 105);
        btnRefresh.addActionListener(e -> loadOrderCatalogue());
        topBar.add(btnRefresh);

        JButton btnTim = createToolbarButton("TÌM", GREEN, 85);
        btnTim.addActionListener(e -> loadOrderCatalogue());
        topBar.add(btnTim);

        mainArea.add(topBar, BorderLayout.NORTH);

        // Vùng danh sách thẻ phiếu đặt hàng (Nền xám #E6E6E6 như Hình 1)
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setBackground(LIST_BG);
        gridContainer.setBorder(new LineBorder(new Color(170, 180, 185), 2));

        orderCardsGrid.setBackground(LIST_BG);
        orderCardsGrid.setLayout(new GridBagLayout());

        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBackground(LIST_BG);
        cardsWrapper.add(orderCardsGrid, BorderLayout.NORTH);

        JScrollPane spOrders = new JScrollPane(cardsWrapper);
        spOrders.setBackground(LIST_BG);
        spOrders.getViewport().setBackground(LIST_BG);
        spOrders.setBorder(new EmptyBorder(6, 6, 6, 6));
        spOrders.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
        spOrders.getVerticalScrollBar().setUnitIncrement(24);

        gridContainer.add(spOrders, BorderLayout.CENTER);

        // Nút bánh răng góc dưới phải vùng lưới phiếu
        JButton btnGearOrders = new JButton(TouchIcons.gear(22, 22, Color.DARK_GRAY));
        btnGearOrders.setOpaque(false);
        btnGearOrders.setContentAreaFilled(false);
        btnGearOrders.setBorderPainted(false);
        btnGearOrders.setFocusPainted(false);
        btnGearOrders.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGearOrders.addActionListener(e -> {
            new TouchThietLapCotDatHangItemDialog(SwingUtilities.getWindowAncestor(this), this::loadOrderCatalogue).setVisible(true);
        });
        JPanel gearBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        gearBox.setBackground(LIST_BG);
        gearBox.add(btnGearOrders);
        gridContainer.add(gearBox, BorderLayout.SOUTH);

        mainArea.add(gridContainer, BorderLayout.CENTER);

        // Thanh chân trang dưới cùng: THOÁT
        JPanel footerBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footerBar.setOpaque(false);
        footerBar.setPreferredSize(new Dimension(0, 44));

        JButton btnThoat = createToolbarButton("THOÁT", RED, 100);
        btnThoat.addActionListener(e -> {
            if (exit != null) exit.run();
        });
        footerBar.add(btnThoat);

        mainArea.add(footerBar, BorderLayout.SOUTH);

        panel.add(mainArea, BorderLayout.CENTER);
        return panel;
    }

    private void openCustomerSearchForFilter() {
        TouchTimKiemKhachHangDialog.CustomerSearchResult res = TouchTimKiemKhachHangDialog.showDialog(SwingUtilities.getWindowAncestor(this));
        if (res.isConfirmed()) {
            if (res.isCleared()) {
                selectedFilterKhachHangId = null;
                txtFilterKhachHang.setText("");
            } else if (res.getCustomer() != null) {
                selectedFilterKhachHangId = res.getCustomer().getId();
                txtFilterKhachHang.setText(res.getCustomer().getName());
            }
            loadOrderCatalogue();
        }
    }

    private void pickFilterTuNgay() {
        String currentStr = filterTuNgay != null ? SDF.format(filterTuNgay) : "";
        String res = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), currentStr);
        if (res != null) {
            try {
                filterTuNgay = res.isEmpty() ? null : SDF.parse(res);
                txtFilterTuNgay.setText(res);
                loadOrderCatalogue();
            } catch (Exception ignored) {}
        }
    }

    private void pickFilterDenNgay() {
        String currentStr = filterDenNgay != null ? SDF.format(filterDenNgay) : "";
        String res = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), currentStr);
        if (res != null) {
            try {
                filterDenNgay = res.isEmpty() ? null : SDF.parse(res);
                txtFilterDenNgay.setText(res);
                loadOrderCatalogue();
            } catch (Exception ignored) {}
        }
    }

    private void loadOrderCatalogue() {
        String soPhieu = txtFilterSoPhieu.getText().trim();
        try {
            orderList = datHangDAO.getDanhSachKhachDatHang(filterTuNgay, filterDenNgay, soPhieu, selectedFilterKhachHangId);
        } catch (Exception e) {
            orderList = new ArrayList<>();
        }
        renderOrderCards();
    }

    private void renderOrderCards() {
        orderCardsGrid.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;

        int total = orderList.size();
        for (int i = 0; i < total; i++) {
            KhachDatHang dh = orderList.get(i);
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;

            JPanel card = createOrderCard(dh, i);
            orderCardsGrid.add(card, gbc);
        }

        if (total % 2 != 0) {
            gbc.gridx = 1;
            gbc.gridy = total / 2;
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            orderCardsGrid.add(empty, gbc);
        }

        orderCardsGrid.revalidate();
        orderCardsGrid.repaint();
    }

    private JPanel createOrderCard(KhachDatHang dh, int index) {
        JPanel card = new JPanel(new GridLayout(4, 1, 0, 2));
        boolean isSelected = (selectedOrder != null && dh.getId() != null && dh.getId().equals(selectedOrder.getId()));
        card.setBackground(isSelected ? ORANGE : TEAL);
        card.setBorder(new LineBorder(isSelected ? Color.WHITE : new Color(0, 110, 110), 1, true));
        card.setPreferredSize(new Dimension(380, 88));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dòng 1: Số phiếu & Ngày
        String dateStr = dh.getNgay() != null ? SDF.format(dh.getNgay()) : "";
        JLabel lblRow1 = new JLabel((dh.getName() != null ? dh.getName() : "") + "  -  " + dateStr, SwingConstants.LEFT);
        lblRow1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRow1.setForeground(Color.WHITE);
        lblRow1.setBorder(new EmptyBorder(0, 8, 0, 8));
        card.add(lblRow1);

        // Dòng 2: Tên khách hàng
        JLabel lblRow2 = new JLabel(dh.getTenKhach() != null ? dh.getTenKhach() : "(Khách lẻ)", SwingConstants.LEFT);
        lblRow2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRow2.setForeground(new Color(255, 245, 200));
        lblRow2.setBorder(new EmptyBorder(0, 8, 0, 8));
        card.add(lblRow2);

        // Dòng 3: Điện thoại / Địa chỉ
        String info = (dh.getDienThoai() != null && !dh.getDienThoai().isBlank()) ? dh.getDienThoai() : (dh.getDiaChi() != null ? dh.getDiaChi() : "");
        JLabel lblRow3 = new JLabel(info, SwingConstants.LEFT);
        lblRow3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRow3.setForeground(Color.WHITE);
        lblRow3.setBorder(new EmptyBorder(0, 8, 0, 8));
        card.add(lblRow3);

        // Dòng 4: Tổng cộng tiền
        JLabel lblRow4 = new JLabel(String.format("Tổng cộng: %,.0f đ", dh.getTongCong()), SwingConstants.RIGHT);
        lblRow4.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRow4.setForeground(Color.YELLOW);
        lblRow4.setBorder(new EmptyBorder(0, 8, 0, 8));
        card.add(lblRow4);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedOrder = dh;
                currentOrderIndex = index;
                renderOrderCards();
                if (e.getClickCount() >= 2) {
                    editSelectedOrder();
                }
            }
        };
        addRecursiveMouseListener(card, ma);
        return card;
    }

    private void editSelectedOrder() {
        if (selectedOrder == null) {
            TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "VUI LÒNG CHỌN PHIẾU ĐẶT HÀNG ĐỂ CHỈNH SỬA!");
            return;
        }
        openOrderForm(selectedOrder);
    }

    private void deleteSelectedOrder() {
        if (selectedOrder == null) {
            TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "VUI LÒNG CHỌN PHIẾU ĐẶT HÀNG CẦN XÓA!");
            return;
        }
        if (TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "BẠN CÓ CHẮC CHẮN MUỐN XÓA PHIẾU " + selectedOrder.getName() + " KHÔNG?")) {
            if (datHangDAO.deleteKhachDatHang(selectedOrder.getId())) {
                selectedOrder = null;
                currentOrderIndex = -1;
                loadOrderCatalogue();
            } else {
                TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "XÓA PHIẾU ĐẶT HÀNG THẤT BẠI!");
            }
        }
    }

    // =========================================================================
    // MÀN HÌNH 2: FORM ĐẶT HÀNG (HÌNH 4)
    // =========================================================================
    private JPanel createFormScreen() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG);

        // 1. THANH ĐỈNH MÀU Ô LIU: ĐẶT HÀNG + NÚT ĐIỀU HƯỚNG (HÌNH 4)
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(true);
        topBar.setBackground(OLIVE_BAR);
        topBar.setPreferredSize(new Dimension(0, 48));
        topBar.setBorder(new EmptyBorder(4, 12, 4, 12));

        JLabel lblTitle = new JLabel("ĐẶT HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        topBar.add(lblTitle, BorderLayout.WEST);

        JPanel navGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navGroup.setOpaque(false);

        JButton btnTruoc = createHeaderNavButton("TRƯỚC", DARK_GREEN, 85);
        btnTruoc.addActionListener(e -> navigateOrder(-1));
        navGroup.add(btnTruoc);

        JButton btnSau = createHeaderNavButton("SAU", GRAY_BTN, 85);
        btnSau.addActionListener(e -> navigateOrder(1));
        navGroup.add(btnSau);

        JButton btnTaoMoi = createHeaderNavButton("TẠO MỚI", DARK_GREEN, 100);
        btnTaoMoi.addActionListener(e -> createNewOrder());
        navGroup.add(btnTaoMoi);

        JButton btnSaoChep = createHeaderNavButton("SAO CHÉP", GRAY_BTN, 100);
        btnSaoChep.addActionListener(e -> duplicateCurrentOrder());
        navGroup.add(btnSaoChep);

        JButton btnXoaForm = createHeaderNavButton("XÓA", GRAY_BTN, 80);
        btnXoaForm.addActionListener(e -> deleteCurrentOrderInForm());
        navGroup.add(btnXoaForm);

        topBar.add(navGroup, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // 2. KHU VỰC THÂN FORM (MASTER FIELDS + ITEMS GRID)
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 4));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(6, 10, 6, 10));

        // PHẦN TRÊN: MASTER FIELDS (NỀN TEAL TỐI)
        JPanel masterFieldsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        masterFieldsPanel.setOpaque(false);
        masterFieldsPanel.setBorder(new EmptyBorder(0, 0, 4, 0));

        // Nửa trái master fields: NGÀY, SỐ, K. HÀNG, TÊN KHÁCH, ĐỊA CHỈ, ĐIỆN THOẠI, EMAIL, GHI CHÚ
        JPanel leftMaster = new JPanel(new GridBagLayout());
        leftMaster.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 2, 3, 2);

        // Dòng 1: NGÀY [▼] - SỐ
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.18;
        leftMaster.add(createFormLabel("NGÀY"), gbc);

        JPanel dateAndNumberRow = new JPanel(new BorderLayout(8, 0));
        dateAndNumberRow.setOpaque(false);

        JPanel dateBox = new JPanel(new BorderLayout(2, 0));
        dateBox.setOpaque(false);
        txtNgay.setPreferredSize(new Dimension(110, 32));
        txtNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtNgay.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { pickOrderDate(); }
        });
        dateBox.add(txtNgay, BorderLayout.CENTER);

        JButton btnPickDate = createIconButton(TouchIcons.downArrow(18, 18, Color.WHITE), TEAL);
        btnPickDate.setPreferredSize(new Dimension(34, 32));
        btnPickDate.addActionListener(e -> pickOrderDate());
        dateBox.add(btnPickDate, BorderLayout.EAST);
        dateAndNumberRow.add(dateBox, BorderLayout.WEST);

        JPanel numBox = new JPanel(new BorderLayout(4, 0));
        numBox.setOpaque(false);
        JLabel lblSo = new JLabel("SỐ");
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSo.setForeground(Color.WHITE);
        numBox.add(lblSo, BorderLayout.WEST);
        txtSo.setPreferredSize(new Dimension(120, 32));
        numBox.add(txtSo, BorderLayout.CENTER);
        dateAndNumberRow.add(numBox, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.82;
        leftMaster.add(dateAndNumberRow, gbc);

        // Dòng 2: K. HÀNG [🔍]
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.weightx = 0.18;
        leftMaster.add(createFormLabel("K. HÀNG"), gbc);

        JPanel rowKhach = new JPanel(new BorderLayout(4, 0));
        rowKhach.setOpaque(false);
        rowKhach.add(txtMaKhach, BorderLayout.CENTER);
        JButton btnSearchKh = createIconButton(TouchIcons.search(20, 20, Color.WHITE), TEAL);
        btnSearchKh.setPreferredSize(new Dimension(38, 32));
        btnSearchKh.addActionListener(e -> pickCustomerForOrder());
        rowKhach.add(btnSearchKh, BorderLayout.EAST);

        gbc.gridx = 1; gbc.weightx = 0.82;
        leftMaster.add(rowKhach, gbc);

        // Dòng 3: TÊN KHÁCH [⌨]
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0.18;
        leftMaster.add(createFormLabel("TÊN KHÁCH"), gbc);

        JPanel rowTen = new JPanel(new BorderLayout(4, 0));
        rowTen.setOpaque(false);
        txtTenKhach.setBackground(YELLOW_FIELD_BG);
        rowTen.add(txtTenKhach, BorderLayout.CENTER);
        JButton btnKbTen = createIconButton(TouchIcons.keyboard(20, 20, Color.WHITE), TEAL);
        btnKbTen.setPreferredSize(new Dimension(38, 32));
        btnKbTen.addActionListener(e -> promptText(txtTenKhach, "NHẬP TÊN KHÁCH HÀNG"));
        rowTen.add(btnKbTen, BorderLayout.EAST);

        gbc.gridx = 1; gbc.weightx = 0.82;
        leftMaster.add(rowTen, gbc);

        // Dòng 4: ĐỊA CHỈ [⌨]
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.weightx = 0.18;
        leftMaster.add(createFormLabel("ĐỊA CHỈ"), gbc);

        JPanel rowDiaChi = new JPanel(new BorderLayout(4, 0));
        rowDiaChi.setOpaque(false);
        rowDiaChi.add(txtDiaChi, BorderLayout.CENTER);
        JButton btnKbDiaChi = createIconButton(TouchIcons.keyboard(20, 20, Color.WHITE), TEAL);
        btnKbDiaChi.setPreferredSize(new Dimension(38, 32));
        btnKbDiaChi.addActionListener(e -> promptText(txtDiaChi, "NHẬP ĐỊA CHỈ"));
        rowDiaChi.add(btnKbDiaChi, BorderLayout.EAST);

        gbc.gridx = 1; gbc.weightx = 0.82;
        leftMaster.add(rowDiaChi, gbc);

        // Dòng 5: ĐIỆN THOẠI [⌨] - EMAIL [⌨]
        gbc.gridy = 4;
        gbc.gridx = 0; gbc.weightx = 0.18;
        leftMaster.add(createFormLabel("ĐIỆN THOẠI"), gbc);

        JPanel phoneEmailRow = new JPanel(new BorderLayout(4, 0));
        phoneEmailRow.setOpaque(false);

        JPanel phoneBox = new JPanel(new BorderLayout(2, 0));
        phoneBox.setOpaque(false);
        phoneBox.add(txtDienThoai, BorderLayout.CENTER);
        JButton btnKbPhone = createIconButton(TouchIcons.keyboard(18, 18, Color.WHITE), TEAL);
        btnKbPhone.setPreferredSize(new Dimension(34, 32));
        btnKbPhone.addActionListener(e -> promptText(txtDienThoai, "NHẬP SỐ ĐIỆN THOẠI"));
        phoneBox.add(btnKbPhone, BorderLayout.EAST);
        phoneEmailRow.add(phoneBox, BorderLayout.WEST);

        JPanel emailBox = new JPanel(new BorderLayout(4, 0));
        emailBox.setOpaque(false);
        JLabel lblEmail = new JLabel("EMAIL");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEmail.setForeground(Color.WHITE);
        emailBox.add(lblEmail, BorderLayout.WEST);
        emailBox.add(txtEmail, BorderLayout.CENTER);
        JButton btnKbEmail = createIconButton(TouchIcons.keyboard(18, 18, Color.WHITE), TEAL);
        btnKbEmail.setPreferredSize(new Dimension(34, 32));
        btnKbEmail.addActionListener(e -> promptText(txtEmail, "NHẬP EMAIL"));
        emailBox.add(btnKbEmail, BorderLayout.EAST);
        phoneEmailRow.add(emailBox, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.82;
        leftMaster.add(phoneEmailRow, gbc);

        // Dòng 6: GHI CHÚ [⌨]
        gbc.gridy = 5;
        gbc.gridx = 0; gbc.weightx = 0.18;
        leftMaster.add(createFormLabel("GHI CHÚ"), gbc);

        JPanel rowGhiChu = new JPanel(new BorderLayout(4, 0));
        rowGhiChu.setOpaque(false);
        rowGhiChu.add(txtGhiChu, BorderLayout.CENTER);
        JButton btnKbGhiChu = createIconButton(TouchIcons.keyboard(20, 20, Color.WHITE), TEAL);
        btnKbGhiChu.setPreferredSize(new Dimension(38, 32));
        btnKbGhiChu.addActionListener(e -> promptText(txtGhiChu, "NHẬP GHI CHÚ ĐƠN HÀNG"));
        rowGhiChu.add(btnKbGhiChu, BorderLayout.EAST);

        gbc.gridx = 1; gbc.weightx = 0.82;
        leftMaster.add(rowGhiChu, gbc);

        masterFieldsPanel.add(leftMaster);

        // Nửa phải master fields: TIỀN HÀNG, GIẢM GIÁ, THUẾ, PHÍ V. CHUYỂN, TỔNG CỘNG, ĐẶT TRƯỚC
        JPanel rightMaster = new JPanel(new GridBagLayout());
        rightMaster.setOpaque(false);
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.fill = GridBagConstraints.BOTH;
        gbcR.insets = new Insets(3, 2, 3, 2);

        // Dòng 1: TIỀN HÀNG
        gbcR.gridy = 0;
        gbcR.gridx = 0; gbcR.weightx = 0.25;
        rightMaster.add(createFormLabel("TIỀN HÀNG"), gbcR);
        gbcR.gridx = 1; gbcR.weightx = 0.75;
        rightMaster.add(lblTienHang, gbcR);

        // Dòng 2: GIẢM GIÁ (2 ô: Tỉ lệ % [🖩] và Số tiền [🖩])
        gbcR.gridy = 1;
        gbcR.gridx = 0; gbcR.weightx = 0.25;
        rightMaster.add(createFormLabel("GIẢM GIÁ"), gbcR);

        JPanel rowGiamGia = new JPanel(new GridLayout(1, 2, 6, 0));
        rowGiamGia.setOpaque(false);

        JPanel boxGgPct = createCalcInputBox(lblGiamGiaPct, e -> promptGiamGiaPct());
        rowGiamGia.add(boxGgPct);

        JPanel boxGgTien = createCalcInputBox(lblGiamGiaTien, e -> promptGiamGiaTien());
        rowGiamGia.add(boxGgTien);

        gbR(rightMaster, rowGiamGia, gbcR, 1);

        // Dòng 3: THUẾ (2 ô: Tỉ lệ % [🖩] và Tiền thuế)
        gbcR.gridy = 2;
        gbcR.gridx = 0; gbcR.weightx = 0.25;
        rightMaster.add(createFormLabel("THUẾ"), gbcR);

        JPanel rowThue = new JPanel(new GridLayout(1, 2, 6, 0));
        rowThue.setOpaque(false);

        JPanel boxThuePct = createCalcInputBox(lblThuePct, e -> promptThuePct());
        rowThue.add(boxThuePct);
        rowThue.add(lblThueTien);

        gbR(rightMaster, rowThue, gbcR, 1);

        // Dòng 4: PHÍ V. CHUYỂN
        gbcR.gridy = 3;
        gbcR.gridx = 0; gbcR.weightx = 0.25;
        rightMaster.add(createFormLabel("PHÍ V. CHUYỂN"), gbcR);

        JPanel boxPhiVc = createCalcInputBox(lblPhiVanChuyen, e -> promptPhiVanChuyen());
        gbR(rightMaster, boxPhiVc, gbcR, 1);

        // Dòng 5: TỔNG CỘNG
        gbcR.gridy = 4;
        gbcR.gridx = 0; gbcR.weightx = 0.25;
        rightMaster.add(createFormLabel("TỔNG CỘNG"), gbcR);
        gbcR.gridx = 1; gbcR.weightx = 0.75;
        rightMaster.add(lblTongCong, gbcR);

        // Dòng 6: ĐẶT TRƯỚC
        gbcR.gridy = 5;
        gbcR.gridx = 0; gbcR.weightx = 0.25;
        rightMaster.add(createFormLabel("ĐẶT TRƯỚC"), gbcR);

        JPanel boxDatTruoc = createCalcInputBox(lblDatTruoc, e -> promptDatTruoc());
        gbR(rightMaster, boxDatTruoc, gbcR, 1);

        masterFieldsPanel.add(rightMaster);
        bodyPanel.add(masterFieldsPanel, BorderLayout.NORTH);

        // PHẦN DƯỚI: THẺ MẶT HÀNG ĐÃ CHỌN + NÚT THÊM/SỬA/XÓA
        JPanel itemsSection = new JPanel(new BorderLayout(0, 4));
        itemsSection.setOpaque(false);

        // Thanh thao tác mặt hàng: THÊM MẶT HÀNG, SỬA, XÓA (Hình 4)
        JPanel itemsToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        itemsToolbar.setOpaque(false);
        itemsToolbar.setPreferredSize(new Dimension(0, 42));

        JButton btnThemHang = createToolbarButton("THÊM MẶT HÀNG", GREEN, 160);
        btnThemHang.addActionListener(e -> openProductSearchToAddItem());
        itemsToolbar.add(btnThemHang);

        JButton btnSuaHang = createToolbarButton("SỬA", DARK_GREEN, 95);
        btnSuaHang.addActionListener(e -> editSelectedItem());
        itemsToolbar.add(btnSuaHang);

        JButton btnXoaHang = createToolbarButton("XÓA", RED, 95);
        btnXoaHang.addActionListener(e -> deleteSelectedItem());
        itemsToolbar.add(btnXoaHang);

        itemsSection.add(itemsToolbar, BorderLayout.NORTH);

        // Vùng lưới thẻ mặt hàng đã chọn (Nền xám #E6E6E6 như Hình 4)
        JPanel itemsGridWrapper = new JPanel(new BorderLayout());
        itemsGridWrapper.setBackground(LIST_BG);
        itemsGridWrapper.setBorder(new LineBorder(new Color(170, 180, 185), 2));

        itemCardsGrid.setBackground(LIST_BG);
        itemCardsGrid.setLayout(new GridBagLayout());

        JPanel itemsCardsInner = new JPanel(new BorderLayout());
        itemsCardsInner.setBackground(LIST_BG);
        itemsCardsInner.add(itemCardsGrid, BorderLayout.NORTH);

        JScrollPane spItems = new JScrollPane(itemsCardsInner);
        spItems.setBackground(LIST_BG);
        spItems.getViewport().setBackground(LIST_BG);
        spItems.setBorder(new EmptyBorder(6, 6, 6, 6));
        spItems.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
        spItems.getVerticalScrollBar().setUnitIncrement(24);

        itemsGridWrapper.add(spItems, BorderLayout.CENTER);

        // Nút bánh răng cấu hình cột thẻ mặt hàng ở góc dưới phải như Hình 4
        JButton btnGearItems = new JButton(TouchIcons.gear(22, 22, Color.DARK_GRAY));
        btnGearItems.setOpaque(false);
        btnGearItems.setContentAreaFilled(false);
        btnGearItems.setBorderPainted(false);
        btnGearItems.setFocusPainted(false);
        btnGearItems.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGearItems.addActionListener(e -> {
            new TouchThietLapCotDatHangItemDialog(SwingUtilities.getWindowAncestor(this), () -> {
                itemColumnConfig = TouchDatHangItemConfigDAO.loadConfig();
                renderItemCards();
            }).setVisible(true);
        });
        JPanel gearBoxItems = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        gearBoxItems.setBackground(LIST_BG);
        gearBoxItems.add(btnGearItems);
        itemsGridWrapper.add(gearBoxItems, BorderLayout.SOUTH);

        itemsSection.add(itemsGridWrapper, BorderLayout.CENTER);
        bodyPanel.add(itemsSection, BorderLayout.CENTER);

        panel.add(bodyPanel, BorderLayout.CENTER);

        // 3. THANH TÁC VỤ DƯỚI (FOOTER BAR HÌNH 4)
        JPanel footerBar = new JPanel(new BorderLayout(10, 0));
        footerBar.setOpaque(false);
        footerBar.setPreferredSize(new Dimension(0, 48));
        footerBar.setBorder(new EmptyBorder(4, 10, 6, 10));

        // Trái: LƯU & IN, LƯU & XEM IN
        JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        footerLeft.setOpaque(false);

        JButton btnLuuIn = createToolbarButton("LƯU & IN", GREEN, 115);
        btnLuuIn.addActionListener(e -> saveOrder(true, false));
        footerLeft.add(btnLuuIn);

        JButton btnLuuXemIn = createToolbarButton("LƯU & XEM IN", GREEN, 135);
        btnLuuXemIn.addActionListener(e -> saveOrder(false, true));
        footerLeft.add(btnLuuXemIn);

        footerBar.add(footerLeft, BorderLayout.WEST);

        // Phải: LƯU, LƯU & MỚI, LƯU & THOÁT, THOÁT
        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerRight.setOpaque(false);

        JButton btnLuu = createToolbarButton("LƯU", GREEN, 90);
        btnLuu.addActionListener(e -> saveOrder(false, false));
        footerRight.add(btnLuu);

        JButton btnLuuMoi = createToolbarButton("LƯU & MỚI", OLIVE_BAR, 115);
        btnLuuMoi.addActionListener(e -> {
            if (saveOrder(false, false)) {
                createNewOrder();
            }
        });
        footerRight.add(btnLuuMoi);

        JButton btnLuuThoat = createToolbarButton("LƯU & THOÁT", GREEN, 125);
        btnLuuThoat.addActionListener(e -> {
            if (saveOrder(false, false)) {
                showCatalogueScreen();
            }
        });
        footerRight.add(btnLuuThoat);

        JButton btnThoatForm = createToolbarButton("THOÁT", RED, 95);
        btnThoatForm.addActionListener(e -> showCatalogueScreen());
        footerRight.add(btnThoatForm);

        footerBar.add(footerRight, BorderLayout.EAST);
        panel.add(footerBar, BorderLayout.SOUTH);

        return panel;
    }

    private void gbR(JPanel parent, JComponent comp, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 0.75;
        parent.add(comp, gbc);
    }

    private void showCatalogueScreen() {
        cards.show(screens, "catalogue");
        loadOrderCatalogue();
    }

    private void createNewOrder() {
        currentOrder = new KhachDatHang();
        orderDate = new Date();
        currentOrder.setNgay(new Timestamp(orderDate.getTime()));
        currentOrder.setName(datHangDAO.generateNextSoPhieu(orderDate));

        orderItems = new ArrayList<>();
        selectedItem = null;

        txtNgay.setText(SDF.format(orderDate));
        txtSo.setText(currentOrder.getName());
        txtMaKhach.setText("");
        txtTenKhach.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtEmail.setText("");
        txtGhiChu.setText("");

        valTienHang = 0;
        valGiamGiaPct = 0;
        valGiamGiaTien = 0;
        valThuePct = 0;
        valThueTien = 0;
        valPhiVanChuyen = 0;
        valTongCong = 0;
        valDatTruoc = 0;

        recalculateOrderTotals();
        renderItemCards();

        cards.show(screens, "form");
    }

    private void openOrderForm(KhachDatHang dh) {
        this.currentOrder = datHangDAO.getKhachDatHangById(dh.getId());
        if (this.currentOrder == null) this.currentOrder = dh;

        orderDate = currentOrder.getNgay() != null ? new Date(currentOrder.getNgay().getTime()) : new Date();
        txtNgay.setText(SDF.format(orderDate));
        txtSo.setText(currentOrder.getName() != null ? currentOrder.getName() : "");
        txtMaKhach.setText(currentOrder.getdKhachHangId() != null ? currentOrder.getdKhachHangId() : "");
        txtTenKhach.setText(currentOrder.getTenKhach() != null ? currentOrder.getTenKhach() : "");
        txtDiaChi.setText(currentOrder.getDiaChi() != null ? currentOrder.getDiaChi() : "");
        txtDienThoai.setText(currentOrder.getDienThoai() != null ? currentOrder.getDienThoai() : "");
        txtEmail.setText(currentOrder.getEmail() != null ? currentOrder.getEmail() : "");
        txtGhiChu.setText(currentOrder.getNote() != null ? currentOrder.getNote() : "");

        orderItems = datHangDAO.getChiTietByKhachDatHangId(currentOrder.getId(), currentOrder.getName());
        selectedItem = orderItems.isEmpty() ? null : orderItems.get(0);

        valGiamGiaPct = currentOrder.getTiLeGiamGia();
        valGiamGiaTien = currentOrder.getTienGiamGia();
        valThuePct = currentOrder.getTiLeThue();
        valThueTien = currentOrder.getTienThue();
        valPhiVanChuyen = currentOrder.getPhiVanChuyen();
        valDatTruoc = 0.0;

        recalculateOrderTotals();
        renderItemCards();

        cards.show(screens, "form");
    }

    private void duplicateCurrentOrder() {
        if (currentOrder == null) return;
        currentOrder.setId(null);
        orderDate = new Date();
        currentOrder.setNgay(new Timestamp(orderDate.getTime()));
        currentOrder.setName(datHangDAO.generateNextSoPhieu(orderDate));
        txtNgay.setText(SDF.format(orderDate));
        txtSo.setText(currentOrder.getName());

        for (KhachDatHangChiTiet ct : orderItems) {
            ct.setId(null);
            ct.settKhachDatHangId(null);
        }
        TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "ĐÃ SAO CHÉP THÀNH PHIẾU MỚI: " + currentOrder.getName());
    }

    private void deleteCurrentOrderInForm() {
        if (currentOrder == null || currentOrder.getId() == null) {
            createNewOrder();
            return;
        }
        if (TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "BẠN CÓ CHẮC CHẮN MUỐN XÓA PHIẾU NÀY KHÔNG?")) {
            datHangDAO.deleteKhachDatHang(currentOrder.getId());
            showCatalogueScreen();
        }
    }

    private void navigateOrder(int delta) {
        if (orderList.isEmpty()) return;
        currentOrderIndex += delta;
        if (currentOrderIndex < 0) currentOrderIndex = 0;
        if (currentOrderIndex >= orderList.size()) currentOrderIndex = orderList.size() - 1;
        selectedOrder = orderList.get(currentOrderIndex);
        openOrderForm(selectedOrder);
    }

    private void pickCustomerForOrder() {
        TouchTimKiemKhachHangDialog.CustomerSearchResult res = TouchTimKiemKhachHangDialog.showDialog(SwingUtilities.getWindowAncestor(this));
        if (res.isConfirmed()) {
            if (res.isCleared()) {
                txtMaKhach.setText("");
                txtTenKhach.setText("");
                txtDiaChi.setText("");
                txtDienThoai.setText("");
                txtEmail.setText("");
            } else if (res.getCustomer() != null) {
                KhachHang kh = res.getCustomer();
                txtMaKhach.setText(kh.getMaKhach() != null ? kh.getMaKhach() : kh.getId());
                txtTenKhach.setText(kh.getName() != null ? kh.getName() : "");
                txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
                txtDienThoai.setText(kh.getDienThoai() != null ? kh.getDienThoai() : "");
                txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
            }
        }
    }

    private void pickOrderDate() {
        String currentStr = orderDate != null ? SDF.format(orderDate) : "";
        String res = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), currentStr);
        if (res != null && !res.isEmpty()) {
            try {
                orderDate = SDF.parse(res);
                txtNgay.setText(res);
            } catch (Exception ignored) {}
        }
    }

    private void openProductSearchToAddItem() {
        TouchTimKiemMatHangDialog.ProductSearchResult res = TouchTimKiemMatHangDialog.showDialog(SwingUtilities.getWindowAncestor(this));
        if (res.isConfirmed() && res.getProduct() != null) {
            MatHang mh = res.getProduct();

            // Hiển thị bảng số để nhập số lượng theo Hình media_1789444292837.png
            Double qty = TouchNumberInputDialog.showDoubleDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "NHẬP SỐ LƯỢNG",
                    1.0,
                    0.001,
                    999999.0
            );

            if (qty != null && qty > 0) {
                KhachDatHangChiTiet ct = new KhachDatHangChiTiet();
                ct.setdMatHangId(mh.getId());
                ct.setMaHang(mh.getCode());
                ct.setTenHang(mh.getName());
                ct.setdDonViTinhId(mh.getDonViTinhId());
                ct.setTenDonViTinh(mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "Cái");
                ct.setSoLuong(qty);
                ct.setDonGia(mh.getGiaBan() > 0 ? mh.getGiaBan() : (mh.getGiaBan2() > 0 ? mh.getGiaBan2() : 0));
                ct.setTiLeGiamGia(0.0);
                ct.tinhThanhTien();

                orderItems.add(ct);
                selectedItem = ct;
                recalculateOrderTotals();
                renderItemCards();
            }
        }
    }

    private void editSelectedItem() {
        if (selectedItem == null) {
            TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "VUI LÒNG CHỌN MẶT HÀNG CẦN SỬA!");
            return;
        }
        if (TouchDieuChinhMatHangDatDialog.showDialog(SwingUtilities.getWindowAncestor(this), selectedItem)) {
            recalculateOrderTotals();
            renderItemCards();
        }
    }

    private void deleteSelectedItem() {
        if (selectedItem == null) {
            TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "VUI LÒNG CHỌN MẶT HÀNG CẦN XÓA!");
            return;
        }
        orderItems.remove(selectedItem);
        selectedItem = orderItems.isEmpty() ? null : orderItems.get(orderItems.size() - 1);
        recalculateOrderTotals();
        renderItemCards();
    }

    private void renderItemCards() {
        itemCardsGrid.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;

        int total = orderItems.size();
        for (int i = 0; i < total; i++) {
            KhachDatHangChiTiet ct = orderItems.get(i);
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;

            JPanel card = createItemCard(ct);
            itemCardsGrid.add(card, gbc);
        }

        if (total % 2 != 0) {
            gbc.gridx = 1;
            gbc.gridy = total / 2;
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            itemCardsGrid.add(empty, gbc);
        }

        itemCardsGrid.revalidate();
        itemCardsGrid.repaint();
    }

    private JPanel createItemCard(KhachDatHangChiTiet ct) {
        boolean isSelected = (selectedItem != null && ct == selectedItem);
        int lineCount = Math.max(itemColumnConfig.getUsedColumns().size(), 1);

        JPanel card = new JPanel(new GridLayout(lineCount, 1, 0, 2));
        card.setBackground(isSelected ? ORANGE : TEAL);
        card.setBorder(new LineBorder(isSelected ? Color.WHITE : new Color(0, 110, 110), 1, true));

        int cardH = Math.max(54, 22 * lineCount + 14);
        card.setPreferredSize(new Dimension(380, cardH));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        for (TouchDatHangItemColumnConfig.ColumnItem col : itemColumnConfig.getUsedColumns()) {
            String text = TouchDatHangItemColumnConfig.getFormattedText(ct, col, true);
            int align = col.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.CENTER;
            JLabel lbl = new JLabel(text, align);
            lbl.setFont(col.getFont());
            lbl.setForeground(isSelected ? Color.WHITE : col.getColor());
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            card.add(lbl);
        }

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedItem = ct;
                renderItemCards();
                if (e.getClickCount() >= 2) {
                    editSelectedItem();
                }
            }
        };
        addRecursiveMouseListener(card, ma);
        return card;
    }

    private void recalculateOrderTotals() {
        valTienHang = 0;
        for (KhachDatHangChiTiet ct : orderItems) {
            valTienHang += ct.getThanhTien();
        }

        if (valGiamGiaPct > 0) {
            valGiamGiaTien = valTienHang * valGiamGiaPct / 100.0;
        }

        double baseTax = Math.max(0, valTienHang - valGiamGiaTien);
        valThueTien = baseTax * valThuePct / 100.0;

        valTongCong = baseTax + valThueTien + valPhiVanChuyen;

        lblTienHang.setText(String.format("%,.0f", valTienHang));
        lblGiamGiaPct.setText(String.format("%,.0f", valGiamGiaPct));
        lblGiamGiaTien.setText(String.format("%,.0f", valGiamGiaTien));
        lblThuePct.setText(String.format("%,.0f", valThuePct));
        lblThueTien.setText(String.format("%,.0f", valThueTien));
        lblPhiVanChuyen.setText(String.format("%,.0f", valPhiVanChuyen));
        lblTongCong.setText(String.format("%,.0f", valTongCong));
        lblDatTruoc.setText(String.format("%,.0f", valDatTruoc));
    }

    private void promptGiamGiaPct() {
        Integer val = TouchNumberInputDialog.showDialog(SwingUtilities.getWindowAncestor(this), "NHẬP TỈ LỆ GIẢM GIÁ %", (int) Math.round(valGiamGiaPct), 0, 100);
        if (val != null) {
            valGiamGiaPct = val;
            recalculateOrderTotals();
        }
    }

    private void promptGiamGiaTien() {
        Integer val = TouchNumberInputDialog.showDialog(SwingUtilities.getWindowAncestor(this), "NHẬP SỐ TIỀN GIẢM GIÁ", (int) Math.round(valGiamGiaTien), 0, (int) Math.max(valTienHang, 99999999));
        if (val != null) {
            valGiamGiaTien = val;
            valGiamGiaPct = valTienHang > 0 ? (valGiamGiaTien * 100.0 / valTienHang) : 0;
            recalculateOrderTotals();
        }
    }

    private void promptThuePct() {
        Integer val = TouchNumberInputDialog.showDialog(SwingUtilities.getWindowAncestor(this), "NHẬP TỈ LỆ THUẾ %", (int) Math.round(valThuePct), 0, 100);
        if (val != null) {
            valThuePct = val;
            recalculateOrderTotals();
        }
    }

    private void promptPhiVanChuyen() {
        Integer val = TouchNumberInputDialog.showDialog(SwingUtilities.getWindowAncestor(this), "NHẬP PHÍ VẬN CHUYỂN", (int) Math.round(valPhiVanChuyen), 0, 99999999);
        if (val != null) {
            valPhiVanChuyen = val;
            recalculateOrderTotals();
        }
    }

    private void promptDatTruoc() {
        Integer val = TouchNumberInputDialog.showDialog(SwingUtilities.getWindowAncestor(this), "NHẬP SỐ TIỀN ĐẶT TRƯỚC", (int) Math.round(valDatTruoc), 0, 99999999);
        if (val != null) {
            valDatTruoc = val;
            lblDatTruoc.setText(String.format("%,.0f", valDatTruoc));
        }
    }

    private void promptText(JTextField target, String title) {
        String res = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), title, target.getText());
        if (res != null) {
            target.setText(res);
        }
    }

    private boolean saveOrder(boolean doPrint, boolean doPreview) {
        if (txtSo.getText().trim().isEmpty()) {
            TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "SỐ PHIẾU KHÔNG ĐƯỢC ĐỂ TRỐNG!");
            return false;
        }

        if (orderItems.isEmpty()) {
            TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "VUI LÒNG THÊM ÍT NHẤT MỘT MẶT HÀNG VÀO ĐƠN HÀNG!");
            return false;
        }

        currentOrder.setName(txtSo.getText().trim());
        currentOrder.setNgay(new Timestamp(orderDate.getTime()));
        currentOrder.setdKhachHangId(txtMaKhach.getText().trim());
        currentOrder.setTenKhach(txtTenKhach.getText().trim());
        currentOrder.setDiaChi(txtDiaChi.getText().trim());
        currentOrder.setDienThoai(txtDienThoai.getText().trim());
        currentOrder.setEmail(txtEmail.getText().trim());
        currentOrder.setNote(txtGhiChu.getText().trim());

        currentOrder.setTienHang(valTienHang);
        currentOrder.setTiLeGiamGia(valGiamGiaPct);
        currentOrder.setTienGiamGia(valGiamGiaTien);
        currentOrder.setTiLeThue(valThuePct);
        currentOrder.setTienThue(valThueTien);
        currentOrder.setPhiVanChuyen(valPhiVanChuyen);
        currentOrder.setTongCong(valTongCong);

        boolean success;
        if (currentOrder.getId() == null || currentOrder.getId().isBlank()) {
            success = datHangDAO.insertKhachDatHang(currentOrder, orderItems);
        } else {
            success = datHangDAO.updateKhachDatHang(currentOrder, orderItems);
        }

        if (success) {
            if (doPrint) {
                TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "ĐÃ LƯU VÀ GỬI LỆNH IN PHIẾU " + currentOrder.getName());
            } else if (doPreview) {
                TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "ĐÃ LƯU VÀ MỞ XEM IN PHIẾU " + currentOrder.getName());
            } else {
                TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "LƯU PHIẾU ĐẶT HÀNG THÀNH CÔNG!");
            }
            return true;
        } else {
            TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "LƯU PHIẾU ĐẶT HÀNG THẤT BẠI! VUI LÒNG KIỂM TRA LẠI.");
            return false;
        }
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH TẠO GIAO DIỆN CHUẨN CẢM ỨNG
    // =========================================================================
    private JTextField createInputBox() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setBorder(new LineBorder(new Color(180, 200, 200), 1));
        tf.setPreferredSize(new Dimension(0, 32));
        return tf;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JLabel createMoneyDisplayLabel() {
        JLabel lbl = new JLabel("0", SwingConstants.RIGHT);
        lbl.setOpaque(true);
        lbl.setBackground(YELLOW_FIELD_BG);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
        lbl.setPreferredSize(new Dimension(0, 32));
        return lbl;
    }

    private JLabel createMoneyInputLabel(String defaultText) {
        JLabel lbl = new JLabel(defaultText, SwingConstants.RIGHT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Color.BLACK);
        lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
        return lbl;
    }

    private JPanel createCalcInputBox(JLabel displayLabel, java.awt.event.ActionListener onCalcClick) {
        JPanel box = new JPanel(new BorderLayout(2, 0));
        box.setOpaque(true);
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(new Color(180, 200, 200), 1));
        box.setPreferredSize(new Dimension(0, 32));

        box.add(displayLabel, BorderLayout.CENTER);

        JButton btnCalc = createIconButton(TouchIcons.calculator(18, 18, Color.WHITE), TEAL);
        btnCalc.setPreferredSize(new Dimension(34, 32));
        btnCalc.addActionListener(onCalcClick);
        box.add(btnCalc, BorderLayout.EAST);

        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        box.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onCalcClick.actionPerformed(null); }
        });
        return box;
    }

    private JButton createIconButton(Icon icon, Color bg) {
        JButton btn = new JButton(icon);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createToolbarButton(String text, Color bg, int width) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(width, 38));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createHeaderNavButton(String text, Color bg, int width) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(width, 36));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void addRecursiveMouseListener(Component comp, MouseAdapter ma) {
        comp.addMouseListener(ma);
        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                addRecursiveMouseListener(child, ma);
            }
        }
    }
}
