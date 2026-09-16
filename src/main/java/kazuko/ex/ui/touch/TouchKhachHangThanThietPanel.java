package kazuko.ex.ui.touch;

import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.dao.TangGiamDiemDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhachHangLoyaltySummary;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.model.system.SystemConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình cảm ứng Khách Hàng Thân Thiết (TouchKhachHangThanThietPanel - Hình 1).
 */
public class TouchKhachHangThanThietPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final Color BORDER_LIGHT = new Color(190, 210, 210);

    private final TangGiamDiemDAO tangGiamDiemDAO = new TangGiamDiemDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final Runnable exit;

    private double doanhSo1Diem = 20000.0;

    private String selectedGroupId = null; // null: Tất cả
    private String selectedGroupName = "TẤT CẢ";
    private KhachHangLoyaltySummary selectedSummary = null;

    private List<KhachHangLoyaltySummary> allSummaries = new ArrayList<>();
    private List<KhachHangLoyaltySummary> filteredSummaries = new ArrayList<>();

    private final JPanel groupButtonsPanel = new JPanel();
    private TouchScrollableRowsPanel cardsGridPanel;
    private JScrollPane scrollGrid;
    private JPanel searchKeyboardWrapper;

    // Tìm kiếm live QWERTY
    private final StringBuilder searchBuffer = new StringBuilder();
    private boolean isSearchOpen = false;
    private JButton btnSearch;
    private JLabel lblSearchTerm;

    public TouchKhachHangThanThietPanel(Runnable exit) {
        this.exit = exit;
        loadSystemConfig();
        initUI();
        loadData();
    }

    private void loadSystemConfig() {
        try {
            SystemConfig cfg = configDAO.loadConfig();
            if (cfg != null && cfg.getDoanhSoTuongUng1Diem() != null) {
                String dsStr = cfg.getDoanhSoTuongUng1Diem().trim().replace(",", "").replace(".", "");
                double ds = Double.parseDouble(dsStr);
                if (ds > 0) doanhSo1Diem = ds;
            }
        } catch (Exception e) {
            doanhSo1Diem = 20000.0;
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG);

        // 1. Header Bar: "KHÁCH HÀNG THÂN THIẾT"
        JLabel lblHeader = new JLabel("KHÁCH HÀNG THÂN THIẾT", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(HEADER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(font(26));
        lblHeader.setPreferredSize(new Dimension(0, 68));
        add(lblHeader, BorderLayout.NORTH);

        // 2. Center Content: Left Groups + Right Cards List
        JPanel centerContent = new JPanel(new BorderLayout(8, 0));
        centerContent.setOpaque(false);
        centerContent.setBorder(new EmptyBorder(8, 8, 8, 8));

        // 2.1 Cột trái: Danh mục nhóm khách hàng
        JPanel leftGroups = new JPanel(new BorderLayout());
        leftGroups.setOpaque(false);
        leftGroups.setPreferredSize(new Dimension(280, 0));

        groupButtonsPanel.setLayout(new BoxLayout(groupButtonsPanel, BoxLayout.Y_AXIS));
        groupButtonsPanel.setOpaque(false);

        renderGroupButtons();

        JScrollPane scrollGroups = new JScrollPane(groupButtonsPanel);
        scrollGroups.getViewport().setBackground(BG);
        scrollGroups.setBorder(new LineBorder(HEADER, 1));
        scrollGroups.getVerticalScrollBar().setUnitIncrement(35);
        leftGroups.add(scrollGroups, BorderLayout.CENTER);

        centerContent.add(leftGroups, BorderLayout.WEST);

        // 2.2 Khu vực chính bên phải: Lưới thẻ khách hàng + Thanh tìm kiếm + Thanh nút dưới
        JPanel rightMain = new JPanel(new BorderLayout(0, 6));
        rightMain.setOpaque(false);

        // Search term badge if searching
        lblSearchTerm = new JLabel();
        lblSearchTerm.setFont(font(15));
        lblSearchTerm.setForeground(Color.BLACK);
        lblSearchTerm.setBackground(new Color(255, 240, 150));
        lblSearchTerm.setOpaque(true);
        lblSearchTerm.setBorder(new EmptyBorder(6, 12, 6, 12));
        lblSearchTerm.setVisible(false);
        rightMain.add(lblSearchTerm, BorderLayout.NORTH);

        // Grid cards
        cardsGridPanel = new TouchScrollableRowsPanel();
        cardsGridPanel.setOpaque(false);

        scrollGrid = new JScrollPane(cardsGridPanel);
        scrollGrid.getViewport().setBackground(BG);
        scrollGrid.setBorder(new LineBorder(new Color(50, 90, 110), 1));
        scrollGrid.getVerticalScrollBar().setUnitIncrement(45);

        searchKeyboardWrapper = new JPanel(new BorderLayout());
        searchKeyboardWrapper.setOpaque(false);
        searchKeyboardWrapper.add(scrollGrid, BorderLayout.CENTER);

        rightMain.add(searchKeyboardWrapper, BorderLayout.CENTER);

        // 2.3 Thanh công cụ dưới cùng (Hình 1: TÌM, REFRESH, RESET bên trái; [⚙], THOÁT bên phải)
        JPanel bottomToolbar = new JPanel(new BorderLayout());
        bottomToolbar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        btnSearch = button("TÌM", GREEN);
        btnSearch.setPreferredSize(new Dimension(110, 58));
        btnSearch.addActionListener(e -> toggleSearchKeyboard());

        JButton btnRefresh = button("REFRESH", GREEN);
        btnRefresh.setPreferredSize(new Dimension(130, 58));
        btnRefresh.addActionListener(e -> loadData());

        JButton btnReset = button("RESET", GREEN);
        btnReset.setPreferredSize(new Dimension(120, 58));
        btnReset.addActionListener(e -> handleResetPoints());

        leftActions.add(btnSearch);
        leftActions.add(btnRefresh);
        leftActions.add(btnReset);
        bottomToolbar.add(leftActions, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnConfig = new JButton();
        btnConfig.setIcon(TouchIcons.gear(34, 34, Color.WHITE));
        btnConfig.setBackground(new Color(0, 90, 115));
        btnConfig.setFocusPainted(false);
        btnConfig.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        btnConfig.setPreferredSize(new Dimension(65, 58));
        btnConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfig.addActionListener(e -> {
            new TouchThietLapCotKhachHangThanThietDialog(SwingUtilities.getWindowAncestor(this), this::refreshGrid).setVisible(true);
        });

        JButton btnExit = button("THOÁT", RED);
        btnExit.setPreferredSize(new Dimension(140, 58));
        btnExit.addActionListener(e -> {
            if (exit != null) exit.run();
        });

        rightActions.add(btnConfig);
        rightActions.add(btnExit);
        bottomToolbar.add(rightActions, BorderLayout.EAST);

        rightMain.add(bottomToolbar, BorderLayout.SOUTH);

        centerContent.add(rightMain, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);
    }

    private void renderGroupButtons() {
        groupButtonsPanel.removeAll();

        // Nút "TẤT CẢ"
        addGroupButton("TẤT CẢ", null);

        // Các nhóm động
        List<NhomKhachHang> dynamicGroups = nhomKhachHangDAO.getAll();
        for (NhomKhachHang grp : dynamicGroups) {
            addGroupButton(grp.getName(), grp.getId());
        }

        groupButtonsPanel.revalidate();
        groupButtonsPanel.repaint();
    }

    private void addGroupButton(String title, String groupId) {
        JButton btn = new JButton(title);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        btn.setPreferredSize(new Dimension(0, 74));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(font(16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 16, 0, 16));

        boolean isCurrent = (groupId == null && selectedGroupId == null) ||
                (groupId != null && groupId.equals(selectedGroupId));
        btn.setBackground(isCurrent ? ORANGE : GREEN);

        btn.addActionListener(e -> {
            selectedGroupId = groupId;
            selectedGroupName = title;
            selectedSummary = null;
            renderGroupButtons();
            filterData();
            refreshGrid();
        });

        groupButtonsPanel.add(btn);
        groupButtonsPanel.add(Box.createVerticalStrut(4));
    }

    public void loadData() {
        allSummaries = tangGiamDiemDAO.getLoyaltySummaryList(doanhSo1Diem);
        filterData();
        refreshGrid();
    }

    private void filterData() {
        filteredSummaries = new ArrayList<>();
        String search = searchBuffer.toString().trim().toLowerCase();

        for (KhachHangLoyaltySummary s : allSummaries) {
            KhachHang kh = s.getKhachHang();
            if (kh == null) continue;

            // Lọc theo nhóm
            if (selectedGroupId != null) {
                String gId = kh.getNhomKhachHangId();
                String gName = kh.getTenNhomKhachHang();
                boolean matches = selectedGroupId.equals(gId)
                        || (selectedGroupName != null && selectedGroupName.equalsIgnoreCase(gName));
                if (!matches) {
                    continue;
                }
            }

            // Lọc theo từ khóa tìm kiếm
            if (!search.isEmpty()) {
                String name = kh.getName() != null ? kh.getName().toLowerCase() : "";
                String code = kh.getMaKhach() != null ? kh.getMaKhach().toLowerCase() : "";
                String phone = kh.getDienThoai() != null ? kh.getDienThoai().toLowerCase() : "";
                String addr = kh.getDiaChi() != null ? kh.getDiaChi().toLowerCase() : "";
                String email = kh.getEmail() != null ? kh.getEmail().toLowerCase() : "";
                if (!name.contains(search) && !code.contains(search) && !phone.contains(search) && !addr.contains(search) && !email.contains(search)) {
                    continue;
                }
            }

            filteredSummaries.add(s);
        }
    }

    private void refreshGrid() {
        if (cardsGridPanel == null) return;
        cardsGridPanel.removeAll();

        TouchKhachHangThanThietColumnConfig cfg = TouchKhachHangThanThietConfigDAO.loadConfig();
        int cols = Math.max(1, cfg.getGridColumns());
        cardsGridPanel.configureGrid(cols, cfg.getGridRows(), 8, 8);

        for (KhachHangLoyaltySummary s : filteredSummaries) {
            JPanel card = createLoyaltyCard(s, cfg);
            cardsGridPanel.add(card);
        }

        cardsGridPanel.completeGrid();

        cardsGridPanel.revalidate();
        cardsGridPanel.repaint();
        if (scrollGrid != null) {
            scrollGrid.revalidate();
            scrollGrid.repaint();
        }
    }

    private JPanel createLoyaltyCard(KhachHangLoyaltySummary summary, TouchKhachHangThanThietColumnConfig cfg) {
        JPanel card = new JPanel(new BorderLayout(8, 4));

        boolean isSelected = selectedSummary != null && summary.getKhachHang() != null
                && selectedSummary.getKhachHang() != null
                && summary.getKhachHang().getId() != null
                && summary.getKhachHang().getId().equals(selectedSummary.getKhachHang().getId());

        card.setBackground(isSelected ? ORANGE : cfg.getCardBgColor());
        card.setBorder(new EmptyBorder(6, 12, 6, 12));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        List<TouchKhachHangThanThietColumnConfig.ColumnItem> usedCols = cfg.getUsedColumns();

        JPanel pLeft = new JPanel();
        pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
        pLeft.setOpaque(false);

        JPanel pRight = new JPanel();
        pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));
        pRight.setOpaque(false);

        for (TouchKhachHangThanThietColumnConfig.ColumnItem col : usedCols) {
            String text = TouchKhachHangThanThietColumnConfig.getFormattedText(summary, col, cfg.isShowTitle());
            if (!text.isEmpty()) {
                JLabel lbl = new JLabel(text);
                lbl.setFont(col.getFont());
                lbl.setForeground(col.getColor());

                if (col.isRightAligned()) {
                    lbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
                    pRight.add(lbl);
                    pRight.add(Box.createVerticalStrut(2));
                } else {
                    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                    pLeft.add(lbl);
                    pLeft.add(Box.createVerticalStrut(2));
                }
            }
        }

        card.add(pLeft, BorderLayout.CENTER);
        if (pRight.getComponentCount() > 0) {
            card.add(pRight, BorderLayout.EAST);
        }

        int colCount = cfg.getGridColumns();
        int leftCount = pLeft.getComponentCount() / 2;
        int rightCount = pRight.getComponentCount() / 2;
        int maxLines = Math.max(1, Math.max(leftCount, rightCount));

        int cardHeight = colCount == 1 ?
                Math.max(54, 28 * maxLines + 14) :
                Math.max(90, 26 * Math.max(maxLines, (usedCols.size() + 1) / 2) + 14);

        card.setPreferredSize(new Dimension(160, cardHeight));
        card.setMinimumSize(new Dimension(60, cardHeight));

        java.awt.event.MouseAdapter selector = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedSummary = summary;
                refreshGrid();
            }
        };
        installCardSelector(card, selector);

        return card;
    }

    private static void installCardSelector(Component component, java.awt.event.MouseAdapter selector) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(selector);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                installCardSelector(child, selector);
            }
        }
    }

    private void handleResetPoints() {
        if (selectedSummary == null || selectedSummary.getKhachHang() == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một khách hàng để xóa điểm!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double currentPoints = selectedSummary.getDiemTichLuy();
        if (currentPoints <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Khách hàng này hiện không có điểm tích lũy nào để xóa!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Hệ thống thực hiện xóa điểm tích lũy của khách hàng " +
                        selectedSummary.getKhachHang().getName() + " (" + selectedSummary.getKhachHang().getMaKhach() + ") về 0.\nBạn có muốn thực hiện không?",
                "Xác nhận xóa điểm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            String khId = selectedSummary.getKhachHang().getId();
            tangGiamDiemDAO.resetDiem(khId, currentPoints);
            loadData();
        }
    }

    // ==========================================================
    // LIVE SEARCH QWERTY KEYBOARD
    // ==========================================================
    private void toggleSearchKeyboard() {
        isSearchOpen = !isSearchOpen;
        if (isSearchOpen) {
            btnSearch.setBackground(ORANGE);
            searchKeyboardWrapper.add(createLiveSearchKeyboard(), BorderLayout.SOUTH);
            lblSearchTerm.setVisible(true);
            updateSearchLabel();
        } else {
            btnSearch.setBackground(GREEN);
            Component south = ((BorderLayout) searchKeyboardWrapper.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            if (south != null) searchKeyboardWrapper.remove(south);
            lblSearchTerm.setVisible(false);
            searchBuffer.setLength(0);
            filterData();
            refreshGrid();
        }
        searchKeyboardWrapper.revalidate();
        searchKeyboardWrapper.repaint();
    }

    private void updateSearchLabel() {
        if (searchBuffer.length() == 0) {
            lblSearchTerm.setText(" [GÕ ĐỂ TÌM KIẾM THEO TÊN / SĐT / ĐỊA CHỈ...] ");
        } else {
            lblSearchTerm.setText(" TÌM: " + searchBuffer.toString() + "  [✕] ");
        }
    }

    private JPanel createLiveSearchKeyboard() {
        JPanel kb = new JPanel(new GridLayout(4, 1, 3, 3));
        kb.setBackground(new Color(30, 45, 55));
        kb.setBorder(new EmptyBorder(6, 6, 6, 6));

        String[][] layout = {
                {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "ĐÓNG"},
                {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "XÓA HẾT"},
                {"A", "S", "D", "F", "G", "H", "J", "K", "L", "BACKSPACE"},
                {"Z", "X", "C", "V", "B", "N", "M", "SPACE"}
        };

        for (String[] row : layout) {
            JPanel rowP = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            rowP.setOpaque(false);
            for (String key : row) {
                JButton b = new JButton(key);
                b.setFont(font(15));
                b.setForeground(Color.WHITE);
                b.setFocusPainted(false);
                b.setPreferredSize(new Dimension(
                        key.equals("SPACE") ? 220 :
                                (key.equals("BACKSPACE") || key.equals("XÓA HẾT") || key.equals("ĐÓNG") ? 115 : 62),
                        46
                ));
                if (key.equals("ĐÓNG") || key.equals("XÓA HẾT") || key.equals("BACKSPACE")) {
                    b.setBackground(RED);
                } else if (key.equals("SPACE")) {
                    b.setBackground(new Color(0, 90, 115));
                } else {
                    b.setBackground(new Color(50, 75, 90));
                }

                b.addActionListener(e -> {
                    switch (key) {
                        case "ĐÓNG" -> toggleSearchKeyboard();
                        case "XÓA HẾT" -> {
                            searchBuffer.setLength(0);
                            updateSearchLabel();
                            filterData();
                            refreshGrid();
                        }
                        case "BACKSPACE" -> {
                            if (searchBuffer.length() > 0) {
                                searchBuffer.deleteCharAt(searchBuffer.length() - 1);
                                updateSearchLabel();
                                filterData();
                                refreshGrid();
                            }
                        }
                        case "SPACE" -> {
                            searchBuffer.append(" ");
                            updateSearchLabel();
                            filterData();
                            refreshGrid();
                        }
                        default -> {
                            searchBuffer.append(key);
                            updateSearchLabel();
                            filterData();
                            refreshGrid();
                        }
                    }
                });
                rowP.add(b);
            }
            kb.add(rowP);
        }

        return kb;
    }

    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(font(16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg.brighter(), 1));
        return btn;
    }

    private static Font font(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }
}
