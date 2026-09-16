package kazuko.ex.ui.touch;

import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.dao.NhomNhaCungCapDAO;
import kazuko.ex.model.congno.CongNoNhaCungCapItem;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;
import kazuko.ex.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình cảm ứng CÔNG NỢ NHÀ CUNG CẤP (TouchCongNoNhaCungCapPanel).
 * Đồng bộ chuẩn xác 100% với phong cách Công nợ khách hàng:
 * - Header banner kèm nút [X] XEM TẤT CẢ và nhãn TỔNG NỢ
 * - Sidebar phân loại nhóm nhà cung cấp (Tất cả, các nhóm NCC...)
 * - Lưới thẻ nhà cung cấp 2 cột cảm ứng bo góc, nút cài đặt cột ⚙
 * - Thanh công cụ chân trang: REFRESH, CHI TIẾT, THANH TOÁN, IN BÁO CÁO, TÌM, THOÁT
 * - Bấm [THANH TOÁN] mở Tạo phiếu chi (TouchPhieuChiPanel) chọn sẵn nhà cung cấp.
 */
public final class TouchCongNoNhaCungCapPanel extends JPanel {
    private static final Color BG_DARK = new Color(0, 67, 86);
    private static final Color HEADER_BLUE = new Color(79, 139, 169);
    private static final Color SIDEBAR_BLUE = new Color(74, 124, 152);
    private static final Color GRID_BG = SIDEBAR_BLUE;
    private static final Color GREEN = new Color(0, 90, 20);
    private static final Color BRIGHT_GREEN = new Color(0, 130, 32);
    private static final Color PURPLE = new Color(90, 10, 80);
    private static final Color RED = new Color(165, 10, 10);
    private static final Color ORANGE = new Color(226, 79, 5);

    private final CongNoNhaCungCapDAO congNoDAO = new CongNoNhaCungCapDAO();
    private final NhomNhaCungCapDAO nhomNhaCungCapDAO = new NhomNhaCungCapDAO();
    private final Runnable exit;

    private final JButton btnToggleAll = new JButton("[X] XEM TẤT CẢ");
    private final JLabel lblTitle = new JLabel("CÔNG NỢ NHÀ CUNG CẤP - TỔNG: 0", SwingConstants.CENTER);

    private final JPanel sidebarButtons = new JPanel();
    private final List<JButton> groupButtons = new ArrayList<>();
    private final TouchScrollableRowsPanel grid = new TouchScrollableRowsPanel();
    private final CardLayout rootCardLayout = new CardLayout();
    private final JPanel rootScreens = new JPanel(rootCardLayout);
    private final JPanel formContainer = new JPanel(new BorderLayout());

    private final List<CongNoNhaCungCapItem> allSuppliers = new ArrayList<>();
    private final List<CongNoNhaCungCapItem> filteredSuppliers = new ArrayList<>();
    private CongNoNhaCungCapItem selectedSupplier;

    private boolean showAll = true; // Bật: xem tất cả; Tắt: chỉ xem NCC còn nợ > 0
    private String selectedGroupId = null; // null: Tất cả
    private String keyword = "";
    private TouchCongNoNhaCungCapColumnConfig config;

    public TouchCongNoNhaCungCapPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);

        rootScreens.setOpaque(false);
        rootScreens.add(createDebtScreen(), "debt");
        rootScreens.add(formContainer, "form");
        rootCardLayout.show(rootScreens, "debt");
        add(rootScreens, BorderLayout.CENTER);

        loadGroups();
        loadData();
    }

    private JPanel createDebtScreen() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);
        p.add(createHeader(), BorderLayout.NORTH);
        p.add(createCenter(), BorderLayout.CENTER);
        p.add(createFooter(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(HEADER_BLUE);
        header.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));
        header.setPreferredSize(new Dimension(0, 58));

        // Nút toggle [X] XEM TẤT CẢ bên trái
        btnToggleAll.setFont(font(17));
        btnToggleAll.setForeground(Color.WHITE);
        btnToggleAll.setBackground(new Color(0, 79, 76));
        btnToggleAll.setFocusPainted(false);
        btnToggleAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggleAll.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        btnToggleAll.setPreferredSize(new Dimension(175, 46));
        btnToggleAll.addActionListener(e -> toggleShowAll());

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        leftBox.setOpaque(false);
        leftBox.add(btnToggleAll);
        header.add(leftBox, BorderLayout.WEST);

        // Tiêu đề căn giữa: CÔNG NỢ NHÀ CUNG CẤP - TỔNG: ...
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(lblTitle, BorderLayout.CENTER);

        // Khoảng trống cân bằng bên phải
        JPanel rightDummy = new JPanel();
        rightDummy.setOpaque(false);
        rightDummy.setPreferredSize(new Dimension(175, 46));
        header.add(rightDummy, BorderLayout.EAST);

        return header;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);

        // 1. Sidebar phân nhóm bên trái
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BLUE);
        sidebar.setPreferredSize(new Dimension(170, 0));
        sidebar.setBorder(new EmptyBorder(6, 6, 6, 6));

        sidebarButtons.setLayout(new BoxLayout(sidebarButtons, BoxLayout.Y_AXIS));
        sidebarButtons.setOpaque(false);
        sidebar.add(sidebarButtons, BorderLayout.NORTH);
        center.add(sidebar, BorderLayout.WEST);

        // 2. Vùng lưới danh sách thẻ ở giữa
        JPanel gridArea = new JPanel(new BorderLayout());
        gridArea.setBackground(GRID_BG);
        gridArea.setBorder(new EmptyBorder(0, 0, 0, 0));

        grid.setBackground(GRID_BG);
        JScrollPane scroll = new JScrollPane(grid,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(GRID_BG);
        scroll.setBackground(GRID_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(50);
        gridArea.add(scroll, BorderLayout.CENTER);

        // Nút bánh răng cấu hình góc dưới phải trong vùng lưới
        JPanel gearBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        gearBox.setOpaque(false);
        JButton gearButton = new JButton(TouchIcons.gear(30, 30, Color.DARK_GRAY));
        gearButton.setOpaque(false);
        gearButton.setContentAreaFilled(false);
        gearButton.setBorderPainted(false);
        gearButton.setFocusPainted(false);
        gearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gearButton.addActionListener(e -> new TouchCongNoNhaCungCapSettingsDialog(
                SwingUtilities.getWindowAncestor(this), this::reloadConfig).setVisible(true));
        gearBox.add(gearButton);
        gridArea.add(gearBox, BorderLayout.SOUTH);

        center.add(gridArea, BorderLayout.CENTER);
        return center;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 6, 8, 6));

        // 5 nút chức năng bên trái
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton btnRefresh = actionButton("REFRESH", GREEN, 120);
        btnRefresh.addActionListener(e -> {
            keyword = "";
            selectedGroupId = null;
            updateGroupButtonsState();
            loadData();
        });

        JButton btnChiTiet = actionButton("CHI TIẾT", GREEN, 125);
        btnChiTiet.addActionListener(e -> openDetail());

        JButton btnThanhToan = actionButton("THANH TOÁN", PURPLE, 150);
        btnThanhToan.addActionListener(e -> openPayment());

        JButton btnInBaoCao = actionButton("IN BÁO CÁO", PURPLE, 150);
        btnInBaoCao.addActionListener(e -> openReport());

        JButton btnTim = actionButton("TÌM", BRIGHT_GREEN, 110);
        btnTim.addActionListener(e -> search());

        left.add(btnRefresh);
        left.add(btnChiTiet);
        left.add(btnThanhToan);
        left.add(btnInBaoCao);
        left.add(btnTim);
        footer.add(left, BorderLayout.WEST);

        // Nút THOÁT bên phải
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        JButton btnExit = actionButton("THOÁT", RED, 130);
        btnExit.addActionListener(e -> {
            if (exit != null) exit.run();
        });
        right.add(btnExit);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    private void toggleShowAll() {
        showAll = !showAll;
        btnToggleAll.setText(showAll ? "[X] XEM TẤT CẢ" : "[  ] XEM TẤT CẢ");
        applyFilters();
    }

    private void loadGroups() {
        sidebarButtons.removeAll();
        groupButtons.clear();

        // Nút TẤT CẢ
        JButton btnTatCa = createGroupButton("TẤT CẢ", null);
        sidebarButtons.add(btnTatCa);
        sidebarButtons.add(Box.createVerticalStrut(6));
        groupButtons.add(btnTatCa);

        // Nạp nhóm từ cơ sở dữ liệu
        try {
            List<NhomNhaCungCap> groups = nhomNhaCungCapDAO.getAll();
            for (NhomNhaCungCap n : groups) {
                JButton b = createGroupButton(n.getName(), n.getId());
                sidebarButtons.add(b);
                sidebarButtons.add(Box.createVerticalStrut(6));
                groupButtons.add(b);
            }
        } catch (Exception ignored) {}

        // Nhóm mặc định nếu rỗng
        if (groupButtons.size() == 1) {
            for (String gName : new String[]{"Nhà cung cấp chính", "Đại lý", "Cung ứng vật tư"}) {
                JButton b = createGroupButton(gName, gName);
                sidebarButtons.add(b);
                sidebarButtons.add(Box.createVerticalStrut(6));
                groupButtons.add(b);
            }
        }

        updateGroupButtonsState();
    }

    private JButton createGroupButton(String text, String groupId) {
        JButton b = new JButton(text);
        b.setFont(font(17));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        b.setPreferredSize(new Dimension(155, 52));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        b.addActionListener(e -> {
            selectedGroupId = groupId;
            updateGroupButtonsState();
            applyFilters();
        });
        return b;
    }

    private void updateGroupButtonsState() {
        for (int i = 0; i < groupButtons.size(); i++) {
            JButton b = groupButtons.get(i);
            boolean active;
            if (i == 0) {
                active = (selectedGroupId == null);
            } else {
                active = (selectedGroupId != null && (b.getText().equalsIgnoreCase(selectedGroupId)
                        || b.getText().equals(selectedGroupId)));
            }
            b.setBackground(active ? ORANGE : GREEN);
        }
    }

    public void loadData() {
        allSuppliers.clear();
        try {
            List<CongNoNhaCungCapItem> list = congNoDAO.getDanhSachCongNoNCC(null, null, false);
            allSuppliers.addAll(list);
        } catch (Exception ignored) {}

        applyFilters();
    }

    private void applyFilters() {
        filteredSuppliers.clear();
        String kw = keyword.toLowerCase().trim();
        double totalDebt = 0.0;

        for (CongNoNhaCungCapItem item : allSuppliers) {
            // Lọc theo xem tất cả hay chỉ còn nợ
            if (!showAll && Math.abs(item.getConNo()) < 0.0001) {
                continue;
            }

            // Lọc theo nhóm NCC
            if (selectedGroupId != null && !selectedGroupId.isBlank()) {
                boolean matchGroup = (item.getNhomNCCId() != null && item.getNhomNCCId().equalsIgnoreCase(selectedGroupId))
                        || (item.getTenNhomNCC() != null && item.getTenNhomNCC().equalsIgnoreCase(selectedGroupId));
                if (!matchGroup) continue;
            }

            // Lọc theo từ khóa
            if (!kw.isEmpty()) {
                boolean match = safe(item.getTenNCC()).toLowerCase().contains(kw)
                        || safe(item.getMaNCC()).toLowerCase().contains(kw)
                        || safe(item.getDienThoai()).toLowerCase().contains(kw)
                        || safe(item.getDiaChi()).toLowerCase().contains(kw);
                if (!match) continue;
            }

            filteredSuppliers.add(item);
            totalDebt += item.getConNo();
        }

        // Cập nhật tiêu đề tổng nợ
        String debtStr = Math.abs(totalDebt) < 0.0001 ? "0" : CurrencyUtil.format(totalDebt);
        lblTitle.setText("CÔNG NỢ NHÀ CUNG CẤP - TỔNG: " + debtStr);

        selectedSupplier = filteredSuppliers.isEmpty() ? null : filteredSuppliers.get(0);
        config = TouchCongNoNhaCungCapConfigDAO.load();
        render();
    }

    private void reloadConfig() {
        config = TouchCongNoNhaCungCapConfigDAO.load();
        render();
    }

    private void render() {
        if (config == null) config = TouchCongNoNhaCungCapConfigDAO.load();
        grid.removeAll();
        grid.configureGrid(config.getGridColumns(), config.getGridRows(), 6, 6);
        grid.setBorder(new EmptyBorder(6, 6, 6, 6));

        int height = Math.max(1, 620 / config.getGridRows());
        for (CongNoNhaCungCapItem item : filteredSuppliers) {
            grid.add(createCard(item, height));
        }
        grid.completeGrid();
        grid.revalidate();
        grid.repaint();
    }

    private JPanel createCard(CongNoNhaCungCapItem item, int height) {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 1));
        p.setBackground(item == selectedSupplier ? ORANGE : config.getCardBgColor());
        p.setBorder(new LineBorder(new Color(230, 240, 240), 1, true));
        p.setPreferredSize(new Dimension(380, height));

        for (var col : config.getUsedColumns()) {
            String text = TouchCongNoNhaCungCapColumnConfig.value(item, col, config.isShowTitle());
            if (text.isBlank()) continue;
            JLabel l = new JLabel(text, col.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setForeground(col.getColor());
            l.setFont(col.getFont());
            l.setBorder(new EmptyBorder(1, 8, 1, 8));
            p.add(l);
        }

        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedSupplier = item;
                render();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openDetail();
                }
            }
        };

        p.addMouseListener(listener);
        for (Component c : p.getComponents()) c.addMouseListener(listener);
        return p;
    }

    private void openDetail() {
        if (selectedSupplier == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TouchChiTietCongNoNhaCungCapDialog dlg = new TouchChiTietCongNoNhaCungCapDialog(
                SwingUtilities.getWindowAncestor(this), selectedSupplier);
        dlg.setVisible(true);
    }

    private void openPayment() {
        if (selectedSupplier == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần thanh toán nợ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        formContainer.removeAll();
        TouchPhieuChiPanel form = new TouchPhieuChiPanel(
                this::returnToDebtScreen,
                selectedSupplier.getNhaCungCapId(),
                selectedSupplier.getTenNCC(),
                selectedSupplier.getDiaChi(),
                selectedSupplier.getConNo()
        );
        formContainer.add(form, BorderLayout.CENTER);
        formContainer.revalidate();
        formContainer.repaint();
        rootCardLayout.show(rootScreens, "form");
    }

    private void returnToDebtScreen() {
        rootCardLayout.show(rootScreens, "debt");
        loadData();
    }

    private void openReport() {
        JOptionPane.showMessageDialog(this,
                "Báo cáo tổng hợp công nợ nhà cung cấp hiện có " + filteredSuppliers.size() + " nhà cung cấp.\nĐang chuẩn bị bản in.",
                "In báo cáo công nợ", JOptionPane.INFORMATION_MESSAGE);
    }

    private void search() {
        String q = TouchTextKeyboardDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), "TÌM KIẾM NHÀ CUNG CẤP", keyword);
        if (q != null) {
            keyword = q.trim();
            applyFilters();
        }
    }

    private static JButton actionButton(String text, Color bg, int width) {
        JButton b = new JButton(text);
        b.setFont(font(18));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(width, 50));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        return b;
    }

    private static Font font(int s) { return new Font("Segoe UI", Font.BOLD, s); }
    private static String safe(String v) { return v == null ? "" : v; }
}
