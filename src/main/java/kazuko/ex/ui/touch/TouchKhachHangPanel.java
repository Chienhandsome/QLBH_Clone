package kazuko.ex.ui.touch;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.danhmuc.NhomKhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Màn hình cảm ứng Quản lý Khách Hàng (TouchKhachHangPanel)
 * Đồng bộ style 100% với Danh mục Mặt hàng (TouchMatHangPanel).
 */
public class TouchKhachHangPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color DARK_GREEN = new Color(0, 75, 38);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color BLUE = new Color(5, 5, 145);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final Color OLIVE_BAR = new Color(108, 137, 30);
    private static final Color BORDER_LIGHT = new Color(190, 210, 210);
    private static final Color YELLOW_FIELD_BG = new Color(255, 255, 204);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final Runnable exit;

    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);

    private JPanel catalogueScreen;
    private JPanel groupListScreen;
    private String selectedGroupId = null; // null: Tất cả, "__none__": Chưa thiết lập, "__trash__": Thùng rác
    private String selectedGroupName = "Tất cả";
    private KhachHang selectedCustomer = null;
    private KhachHang editingCustomer = null;
    private int customerIndex = -1;
    private List<KhachHang> currentCustomerList = new ArrayList<>();

    // Tìm kiếm live QWERTY
    private final StringBuilder searchBuffer = new StringBuilder();
    private boolean isSearchOpen = false;
    private JButton btnSearch;
    private JLabel lblSearchTerm;
    private JPanel searchKeyboardWrapper;

    // Các trường form khách hàng (Hình 2 từ phiên trước)
    private final JTextField txtMaKhach = createField(false);
    private final JTextField txtName = createField(true);
    private final JTextField txtDiaChi = createField(false);
    private final JTextField txtDienThoai = createField(false);
    private final JTextField txtEmail = createField(false);
    private final JTextField txtNhomKhachHang = createField(false);
    private final JTextField txtMaSoThue = createField(false);
    private final JTextField txtNhanVien = createField(false);
    private final JTextField txtDiemBanDau = createField(false);
    private final JTextField txtNgaySinhNhat = createField(false);
    private final JComboBox<String> cbGiaBan = new JComboBox<>(new String[]{
            "Giá theo danh mục", "Giá bán 2", "Giá bán 3", "Giá bán 4", "Giá vốn", "Giá gần nhất"
    });
    private final JTextField txtGhiChu = createField(false);

    private NhomKhachHang formSelectedGroup = null;
    private NhanVien formSelectedNhanVien = null;

    // Các trường form nhóm khách hàng (Hình 2 của user: TÊN NHÓM, TỈ LỆ GIẢM GIÁ, ĐIỂM TÍCH LŨY, GHI CHÚ)
    private final JTextField txtGroupName = createField(false);
    private final JTextField txtGroupDiscount = createField(false);
    private final JTextField txtGroupPoints = createField(false);
    private final JTextField txtGroupNote = createField(false);
    private NhomKhachHang editingGroup = null;
    private int groupIndex = -1;

    // Nút toolbar danh mục
    private JButton btnEdit;
    private JButton btnDelete;
    private TouchScrollableRowsPanel cardsGridPanel;

    public TouchKhachHangPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout());
        screens.add(createCustomerForm(), "customer-form");
        screens.add(createGroupForm(), "group-form");
        add(screens, BorderLayout.CENTER);
        showCatalogue();
    }

    private void showCatalogue() {
        if (catalogueScreen != null) screens.remove(catalogueScreen);
        catalogueScreen = createCatalogue();
        screens.add(catalogueScreen, "catalogue");
        cards.show(screens, "catalogue");
        refresh();
    }

    private JPanel createCatalogue() {
        JPanel root = basePanel();

        // 1. Left Sidebar: Groups
        JPanel groups = new JPanel(new BorderLayout(0, 7));
        groups.setOpaque(false);
        groups.setPreferredSize(new Dimension(300, 0));
        groups.setBorder(new EmptyBorder(7, 8, 6, 8));

        // Tiêu đề danh mục khách hàng
        JButton btnHeader = button("DANH MỤC KHÁCH HÀNG", RED);
        btnHeader.setFont(font(17));
        btnHeader.setPreferredSize(new Dimension(0, 58));
        groups.add(btnHeader, BorderLayout.NORTH);

        // Danh sách các nhóm
        JPanel groupRows = new JPanel();
        groupRows.setLayout(new BoxLayout(groupRows, BoxLayout.Y_AXIS));
        groupRows.setBackground(new Color(235, 235, 235));

        addGroupFilter(groupRows, "TẤT CẢ", null, BLUE);
        addGroupFilter(groupRows, "CHƯA THIẾT LẬP", "__none__", BLUE);

        List<NhomKhachHang> dynamicGroups = nhomKhachHangDAO.getAll();
        for (NhomKhachHang grp : dynamicGroups) {
            addGroupFilter(groupRows, grp.getName(), grp.getId(), GREEN);
        }

        addGroupFilter(groupRows, "THÙNG RÁC", "__trash__", BLUE);

        JScrollPane scrollGroups = new JScrollPane(groupRows);
        scrollGroups.getViewport().setBackground(new Color(235, 235, 235));
        scrollGroups.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        scrollGroups.getVerticalScrollBar().setUnitIncrement(35);
        groups.add(scrollGroups, BorderLayout.CENTER);

        // Nút ĐIỀU CHỈNH NHÓM dưới cùng của cột trái
        JButton btnManageGroups = button("ĐIỀU CHỈNH NHÓM", DARK_GREEN);
        btnManageGroups.setPreferredSize(new Dimension(300, 58));
        btnManageGroups.addActionListener(e -> showGroupList());
        groups.add(btnManageGroups, BorderLayout.SOUTH);

        root.add(groups, BorderLayout.WEST);

        // 2. Right Center: Toolbar + Cards Grid + Bottom exit/setting
        JPanel main = new JPanel(new BorderLayout(0, 8));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(7, 0, 6, 8));

        // Top Toolbar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);

        JButton btnAdd = button("THÊM MỚI", GREEN);
        btnAdd.setPreferredSize(new Dimension(140, 58));
        btnAdd.addActionListener(e -> openCustomerForm(null));

        btnEdit = button("CHỈNH SỬA", new Color(135, 145, 150));
        btnEdit.setPreferredSize(new Dimension(140, 58));
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> {
            if (selectedCustomer != null) {
                openCustomerForm(selectedCustomer);
            }
        });

        btnDelete = button("XÓA", new Color(135, 145, 150));
        btnDelete.setPreferredSize(new Dimension(120, 58));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> handleDeleteCustomer());

        JButton btnRefresh = button("REFRESH", GREEN);
        btnRefresh.setPreferredSize(new Dimension(130, 58));
        btnRefresh.addActionListener(e -> refresh());

        btnSearch = button("TÌM", GREEN);
        btnSearch.setPreferredSize(new Dimension(110, 58));
        btnSearch.addActionListener(e -> toggleSearchKeyboard());

        topBar.add(btnAdd);
        topBar.add(btnEdit);
        topBar.add(btnDelete);
        topBar.add(btnRefresh);
        topBar.add(btnSearch);

        // Search badge
        lblSearchTerm = new JLabel();
        lblSearchTerm.setFont(font(15));
        lblSearchTerm.setForeground(Color.BLACK);
        lblSearchTerm.setBackground(new Color(255, 240, 150));
        lblSearchTerm.setOpaque(true);
        lblSearchTerm.setBorder(new EmptyBorder(6, 12, 6, 12));
        lblSearchTerm.setVisible(false);
        topBar.add(lblSearchTerm);

        main.add(topBar, BorderLayout.NORTH);

        // Center: Customer Cards Grid in ScrollPane
        cardsGridPanel = new TouchScrollableRowsPanel();
        cardsGridPanel.setOpaque(false);

        JScrollPane scrollGrid = new JScrollPane(cardsGridPanel);
        scrollGrid.getViewport().setBackground(BG);
        scrollGrid.setBorder(null);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(45);

        // Wrap with Search Keyboard at bottom if open
        searchKeyboardWrapper = new JPanel(new BorderLayout());
        searchKeyboardWrapper.setOpaque(false);
        searchKeyboardWrapper.add(scrollGrid, BorderLayout.CENTER);

        main.add(searchKeyboardWrapper, BorderLayout.CENTER);

        // Bottom right: Setting ⚙ (Vẽ icon vector gear sắc nét, không bị ô vuông) và THOÁT
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

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
            new TouchThietLapCotKhachHangDialog(SwingUtilities.getWindowAncestor(this), this::refresh).setVisible(true);
        });

        JButton btnExit = button("THOÁT", RED);
        btnExit.setPreferredSize(new Dimension(140, 58));
        btnExit.addActionListener(e -> {
            if (exit != null) exit.run();
        });

        rightActions.add(btnConfig);
        rightActions.add(btnExit);
        bottomBar.add(rightActions, BorderLayout.EAST);

        main.add(bottomBar, BorderLayout.SOUTH);
        root.add(main, BorderLayout.CENTER);

        return root;
    }

    private void addGroupFilter(JPanel container, String title, String groupId, Color baseColor) {
        JButton btn = new JButton(title);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(font(15));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 14, 0, 14));

        boolean isCurrent = (groupId == null && selectedGroupId == null) ||
                (groupId != null && groupId.equals(selectedGroupId));
        btn.setBackground(isCurrent ? ORANGE : baseColor);

        btn.addActionListener(e -> {
            selectedGroupId = groupId;
            selectedGroupName = title;
            selectedCustomer = null;
            showCatalogue();
        });

        container.add(btn);
        container.add(Box.createVerticalStrut(3));
    }

    public void refresh() {
        if (cardsGridPanel == null) return;
        cardsGridPanel.removeAll();

        // Load customers by group
        List<KhachHang> all;
        if ("__trash__".equals(selectedGroupId)) {
            all = khachHangDAO.getTrashItems();
        } else if ("__none__".equals(selectedGroupId)) {
            all = new ArrayList<>();
            for (KhachHang kh : khachHangDAO.getAll()) {
                if (kh.getNhomKhachHangId() == null || kh.getNhomKhachHangId().trim().isEmpty()) {
                    all.add(kh);
                }
            }
        } else if (selectedGroupId != null) {
            all = new ArrayList<>();
            for (KhachHang kh : khachHangDAO.getAll()) {
                if (selectedGroupId.equals(kh.getNhomKhachHangId())) {
                    all.add(kh);
                }
            }
        } else {
            all = khachHangDAO.getAll();
        }

        // Apply search filter if any
        String search = searchBuffer.toString().trim().toLowerCase();
        currentCustomerList = new ArrayList<>();
        for (KhachHang kh : all) {
            if (search.isEmpty()) {
                currentCustomerList.add(kh);
            } else {
                String name = kh.getName() != null ? kh.getName().toLowerCase() : "";
                String code = kh.getMaKhach() != null ? kh.getMaKhach().toLowerCase() : "";
                String phone = kh.getDienThoai() != null ? kh.getDienThoai().toLowerCase() : "";
                String addr = kh.getDiaChi() != null ? kh.getDiaChi().toLowerCase() : "";
                if (name.contains(search) || code.contains(search) || phone.contains(search) || addr.contains(search)) {
                    currentCustomerList.add(kh);
                }
            }
        }

        // Config layout
        TouchKhachHangColumnConfig cfg = TouchKhachHangConfigDAO.loadConfig();
        int cols = cfg.getGridColumns();

        cardsGridPanel.configureGrid(cols, cfg.getGridRows(), 8, 8);

        for (KhachHang kh : currentCustomerList) {
            JPanel card = createCustomerCard(kh, cfg);
            cardsGridPanel.add(card);
        }

        cardsGridPanel.completeGrid();

        updateActionButtons();
        cardsGridPanel.revalidate();
        cardsGridPanel.repaint();
    }

    private JPanel createCustomerCard(KhachHang kh, TouchKhachHangColumnConfig cfg) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        boolean isSelected = selectedCustomer != null && kh.getId() != null
                && kh.getId().equals(selectedCustomer.getId());

        card.setBorder(new EmptyBorder(8, 8, 8, 8));
        card.setBackground(isSelected ? ORANGE : cfg.getCardBgColor());
        card.setPreferredSize(new Dimension(160, 96));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        List<TouchKhachHangColumnConfig.ColumnItem> usedCols = cfg.getUsedColumns();
        for (TouchKhachHangColumnConfig.ColumnItem col : usedCols) {
            String text = TouchKhachHangColumnConfig.getFormattedText(kh, col, cfg.isShowTitle());
            if (!text.isEmpty()) {
                JLabel lbl = new JLabel(text, SwingConstants.CENTER);
                lbl.setFont(col.getFont());
                lbl.setForeground(col.getColor());
                lbl.setAlignmentX(col.isRightAligned() ? Component.RIGHT_ALIGNMENT : Component.CENTER_ALIGNMENT);
                card.add(lbl);
                card.add(Box.createVerticalStrut(2));
            }
        }

        java.awt.event.MouseAdapter selector = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectedCustomer = kh;
                    openCustomerForm(kh);
                } else {
                    selectedCustomer = kh;
                    refresh();
                }
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

    private void updateActionButtons() {
        boolean hasSelected = (selectedCustomer != null);
        btnEdit.setEnabled(hasSelected);
        btnEdit.setBackground(hasSelected ? HEADER : new Color(135, 145, 150));

        btnDelete.setEnabled(hasSelected);
        btnDelete.setBackground(hasSelected ? RED : new Color(135, 145, 150));
    }

    private void handleDeleteCustomer() {
        if (selectedCustomer == null) return;
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng " + selectedCustomer.getName() + " (" + selectedCustomer.getMaKhach() + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            khachHangDAO.delete(selectedCustomer.getId());
            selectedCustomer = null;
            refresh();
        }
    }

    // ==========================================================
    // 2. LIVE SEARCH QWERTY KEYBOARD
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
        }
        searchKeyboardWrapper.revalidate();
        searchKeyboardWrapper.repaint();
    }

    private void updateSearchLabel() {
        if (searchBuffer.length() == 0) {
            lblSearchTerm.setText(" [GÕ ĐỂ TÌM KIẾM...] ");
        } else {
            lblSearchTerm.setText(" TÌM: " + searchBuffer.toString() + "  [✕] ");
        }
    }

    private JPanel createLiveSearchKeyboard() {
        JPanel kb = new JPanel();
        kb.setLayout(new BoxLayout(kb, BoxLayout.Y_AXIS));
        kb.setOpaque(false);
        kb.setBorder(new EmptyBorder(6, 0, 0, 0));

        String[] r1 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        String[] r2 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        String[] r3 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        String[] r4 = {"Z", "X", "C", "V", "B", "N", "M"};

        kb.add(createSearchKeyRow(r1, true));
        kb.add(Box.createVerticalStrut(3));
        kb.add(createSearchKeyRow(r2, false));
        kb.add(Box.createVerticalStrut(3));
        kb.add(createSearchKeyRow(r3, false));
        kb.add(Box.createVerticalStrut(3));

        // Row 4 with Space & Clear
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row4.setOpaque(false);
        for (String k : r4) row4.add(createSearchCharButton(k));

        JButton btnSpace = createSearchButton("CÁCH", new Dimension(140, 42));
        btnSpace.addActionListener(e -> {
            searchBuffer.append(" ");
            updateSearchLabel();
            refresh();
        });
        row4.add(btnSpace);

        JButton btnClear = createSearchButton("XÓA HẾT", new Dimension(100, 42));
        btnClear.setBackground(RED);
        btnClear.addActionListener(e -> {
            searchBuffer.setLength(0);
            updateSearchLabel();
            refresh();
        });
        row4.add(btnClear);

        kb.add(row4);
        return kb;
    }

    private JPanel createSearchKeyRow(String[] keys, boolean hasBackspace) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);
        for (String k : keys) {
            row.add(createSearchCharButton(k));
        }
        if (hasBackspace) {
            JButton btnBack = createSearchButton("⌫", new Dimension(68, 42));
            btnBack.addActionListener(e -> {
                if (searchBuffer.length() > 0) {
                    searchBuffer.deleteCharAt(searchBuffer.length() - 1);
                    updateSearchLabel();
                    refresh();
                }
            });
            row.add(btnBack);
        }
        return row;
    }

    private JButton createSearchCharButton(String text) {
        JButton btn = createSearchButton(text, new Dimension(54, 42));
        btn.addActionListener(e -> {
            searchBuffer.append(text);
            updateSearchLabel();
            refresh();
        });
        return btn;
    }

    private JButton createSearchButton(String text, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(font(15));
        btn.setBackground(new Color(0, 90, 115));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(BORDER_LIGHT, 1, true));
        btn.setPreferredSize(size);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==========================================================
    // 3. DANH MỤC NHÓM KHÁCH HÀNG (HÌNH 1 CỦA USER: BỎ NÚT THÊM TRÊN MỖI ITEM)
    // ==========================================================
    private void showGroupList() {
        if (groupListScreen != null) screens.remove(groupListScreen);
        groupListScreen = basePanel();

        // Header
        JLabel headerLabel = new JLabel("DANH MỤC NHÓM KHÁCH HÀNG", SwingConstants.CENTER);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(font(26));
        headerLabel.setPreferredSize(new Dimension(0, 68));
        groupListScreen.add(headerLabel, BorderLayout.NORTH);

        // Rows container
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(new Color(235, 235, 235));
        rows.setBorder(new EmptyBorder(6, 6, 6, 6));

        for (NhomKhachHang group : nhomKhachHangDAO.getAll()) {
            rows.add(groupRow(group));
        }
        rows.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(45);
        groupListScreen.add(scroll, BorderLayout.CENTER);

        // Footer: THÊM ở góc trái, THOÁT ở góc phải
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 5, 4, 5));

        JButton btnAddGroup = button("THÊM", new Color(0, 145, 0));
        btnAddGroup.setPreferredSize(new Dimension(225, 68));
        btnAddGroup.addActionListener(e -> beginGroup(null));

        JButton btnCloseGroup = button("THOÁT", RED);
        btnCloseGroup.setPreferredSize(new Dimension(225, 68));
        btnCloseGroup.addActionListener(e -> showCatalogue());

        footer.add(btnAddGroup, BorderLayout.WEST);
        footer.add(btnCloseGroup, BorderLayout.EAST);
        groupListScreen.add(footer, BorderLayout.SOUTH);

        screens.add(groupListScreen, "groups");
        cards.show(screens, "groups");
    }

    /**
     * Dòng nhóm khách hàng: Tên bên trái, chỉ có 2 nút [XÓA] và [SỬA] bên phải.
     * Nút [THÊM] trên mỗi dòng đã được loại bỏ theo đúng yêu cầu người dùng.
     */
    private JPanel groupRow(NhomKhachHang group) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel name = new JLabel(group.getName() != null ? group.getName() : "");
        name.setOpaque(true);
        name.setBackground(GREEN);
        name.setForeground(Color.WHITE);
        name.setFont(font(22));
        name.setBorder(new EmptyBorder(8, 16, 8, 16));
        row.add(name, BorderLayout.CENTER);

        // Chỉ có 2 nút: XÓA (đỏ) và SỬA (xanh dương)
        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);

        JButton btnDel = button("XÓA", RED);
        btnDel.setPreferredSize(new Dimension(110, 80));
        btnDel.addActionListener(e -> deleteGroup(group));

        JButton btnEditGroup = button("SỬA", BLUE);
        btnEditGroup.setPreferredSize(new Dimension(110, 80));
        btnEditGroup.addActionListener(e -> beginGroup(group));

        actions.add(btnDel);
        actions.add(btnEditGroup);
        row.add(actions, BorderLayout.EAST);

        return row;
    }

    private void beginGroup(NhomKhachHang group) {
        this.editingGroup = group;
        List<NhomKhachHang> groups = nhomKhachHangDAO.getAll();
        groupIndex = group == null ? -1 : Math.max(0, groups.indexOf(group));

        txtGroupName.setText(group == null ? "" : (group.getName() != null ? group.getName() : ""));
        txtGroupDiscount.setText(group == null ? "0" : String.valueOf((long) group.getTiLeGiamGia()));
        txtGroupPoints.setText(group == null ? "0" : String.valueOf((long) group.getDiemTichLuy()));
        txtGroupNote.setText(group == null ? "" : (group.getGhiChu() != null ? group.getGhiChu() : ""));

        cards.show(screens, "group-form");
    }

    private void moveGroup(int delta) {
        List<NhomKhachHang> groups = nhomKhachHangDAO.getAll();
        if (groups.isEmpty()) return;
        groupIndex = Math.max(0, Math.min(groups.size() - 1, groupIndex + delta));
        beginGroup(groups.get(groupIndex));
    }

    private void copyGroup() {
        txtGroupName.setText(txtGroupName.getText() + " - BẢN SAO");
        editingGroup = null;
    }

    private void deleteGroup(NhomKhachHang group) {
        if (group == null) return;
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhóm " + group.getName() + " không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            nhomKhachHangDAO.moveToTrash(group.getId());
            showGroupList();
        }
    }

    private void saveGroup(boolean andNew, boolean andExit) {
        String name = txtGroupName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhóm khách hàng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtGroupName.requestFocus();
            return;
        }

        NhomKhachHang value = editingGroup == null ? new NhomKhachHang() : editingGroup;
        value.setName(name);
        try {
            value.setTiLeGiamGia(Double.parseDouble(txtGroupDiscount.getText().trim()));
        } catch (Exception e) {
            value.setTiLeGiamGia(0);
        }
        try {
            value.setDiemTichLuy(Double.parseDouble(txtGroupPoints.getText().trim()));
        } catch (Exception e) {
            value.setDiemTichLuy(0);
        }
        value.setGhiChu(txtGroupNote.getText().trim());

        if (value.getId() == null || value.getId().trim().isEmpty()) {
            value.setId(UUID.randomUUID().toString());
            nhomKhachHangDAO.insert(value);
        } else {
            nhomKhachHangDAO.update(value);
        }

        editingGroup = value;

        if (andNew) {
            beginGroup(null);
        } else if (andExit) {
            showGroupList();
        } else {
            JOptionPane.showMessageDialog(this, "Đã lưu nhóm khách hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==========================================================
    // 4. FORM NHÓM KHÁCH HÀNG (HÌNH 2 CỦA USER)
    // ==========================================================
    private JPanel createGroupForm() {
        JPanel root = basePanel();

        // Top Toolbar Banner
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(OLIVE_BAR);
        topHeader.setPreferredSize(new Dimension(0, 68));
        topHeader.setBorder(new EmptyBorder(5, 12, 5, 12));

        // Green Title Badge
        JLabel lblTitle = new JLabel("NHÓM KHÁCH HÀNG", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(DARK_GREEN);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(font(22));
        lblTitle.setPreferredSize(new Dimension(240, 58));
        lblTitle.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        topHeader.add(lblTitle, BorderLayout.WEST);

        // Top Nav buttons: TRƯỚC | SAU | TẠO MỚI | SAO CHÉP | XÓA
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topNav.setOpaque(false);

        JButton btnPrev = button("TRƯỚC", DARK_GREEN);
        btnPrev.setPreferredSize(new Dimension(135, 58));
        btnPrev.addActionListener(e -> moveGroup(-1));

        JButton btnNext = button("SAU", DARK_GREEN);
        btnNext.setPreferredSize(new Dimension(135, 58));
        btnNext.addActionListener(e -> moveGroup(1));

        JButton btnNew = button("TẠO MỚI", DARK_GREEN);
        btnNew.setPreferredSize(new Dimension(135, 58));
        btnNew.addActionListener(e -> beginGroup(null));

        JButton btnCopy = button("SAO CHÉP", DARK_GREEN);
        btnCopy.setPreferredSize(new Dimension(135, 58));
        btnCopy.addActionListener(e -> copyGroup());

        JButton btnDel = button("XÓA", RED);
        btnDel.setPreferredSize(new Dimension(135, 58));
        btnDel.addActionListener(e -> deleteGroup(editingGroup));

        topNav.add(btnPrev);
        topNav.add(btnNext);
        topNav.add(btnNew);
        topNav.add(btnCopy);
        topNav.add(btnDel);

        topHeader.add(topNav, BorderLayout.EAST);
        root.add(topHeader, BorderLayout.NORTH);

        // Fields: TÊN NHÓM KHÁCH HÀNG, TỈ LỆ GIẢM GIÁ, ĐIỂM TÍCH LŨY, GHI CHÚ
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(new EmptyBorder(12, 15, 12, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // 1. TÊN NHÓM KHÁCH HÀNG
        addSingleRow(fields, c, 0, "TÊN NHÓM KHÁCH HÀNG", txtGroupName, TouchIcons.keyboard(28, 24, Color.WHITE),
                () -> openKeyboard(txtGroupName, "TÊN NHÓM KHÁCH HÀNG"));

        // 2. TỈ LỆ GIẢM GIÁ (Căn phải 0)
        txtGroupDiscount.setHorizontalAlignment(SwingConstants.RIGHT);
        addSingleRow(fields, c, 1, "TỈ LỆ GIẢM GIÁ", txtGroupDiscount, TouchIcons.calculator(26, 26, Color.WHITE),
                () -> openNumpadField(txtGroupDiscount, "TỈ LỆ GIẢM GIÁ"));

        // 3. ĐIỂM TÍCH LŨY (Căn phải 0)
        txtGroupPoints.setHorizontalAlignment(SwingConstants.RIGHT);
        addSingleRow(fields, c, 2, "ĐIỂM TÍCH LŨY", txtGroupPoints, TouchIcons.calculator(26, 26, Color.WHITE),
                () -> openNumpadField(txtGroupPoints, "ĐIỂM TÍCH LŨY"));

        // 4. GHI CHÚ
        addSingleRow(fields, c, 3, "GHI CHÚ", txtGroupNote, TouchIcons.keyboard(28, 24, Color.WHITE),
                () -> openKeyboard(txtGroupNote, "GHI CHÚ"));

        // Glue
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.weighty = 1.0;
        fields.add(Box.createVerticalGlue(), c);

        JScrollPane formScroll = new JScrollPane(fields);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(true);
        formScroll.getViewport().setBackground(BG);
        formScroll.setBorder(null);
        root.add(formScroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 10, 8, 10));

        JButton btnSave = button("LƯU", DARK_GREEN);
        btnSave.setPreferredSize(new Dimension(175, 68));
        btnSave.addActionListener(e -> saveGroup(false, false));

        JButton btnSaveNew = button("LƯU & MỚI", new Color(150, 95, 20));
        btnSaveNew.setPreferredSize(new Dimension(175, 68));
        btnSaveNew.addActionListener(e -> saveGroup(true, false));

        JButton btnSaveExit = button("LƯU & THOÁT", DARK_GREEN);
        btnSaveExit.setPreferredSize(new Dimension(185, 68));
        btnSaveExit.addActionListener(e -> saveGroup(false, true));

        JButton btnCancel = button("THOÁT", RED);
        btnCancel.setPreferredSize(new Dimension(145, 68));
        btnCancel.addActionListener(e -> showGroupList());

        footer.add(btnSave);
        footer.add(btnSaveNew);
        footer.add(btnSaveExit);
        footer.add(btnCancel);

        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private void openNumpadField(JTextField field, String title) {
        int initial = 0;
        try {
            initial = (int) Double.parseDouble(field.getText().trim());
        } catch (Exception ignored) {}
        Integer res = TouchNumberInputDialog.showDialog(SwingUtilities.getWindowAncestor(this), title, initial);
        if (res != null) {
            field.setText(String.valueOf(res));
        }
    }

    // ==========================================================
    // 5. CUSTOMER FORM (HÌNH 2 TỪ PHIÊN TRƯỚC)
    // ==========================================================
    private JPanel createCustomerForm() {
        JPanel root = basePanel();

        // Top Toolbar Banner
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(OLIVE_BAR);
        topHeader.setPreferredSize(new Dimension(0, 68));
        topHeader.setBorder(new EmptyBorder(5, 12, 5, 12));

        // Green Title Badge
        JLabel lblTitle = new JLabel("KHÁCH HÀNG", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(DARK_GREEN);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(font(22));
        lblTitle.setPreferredSize(new Dimension(200, 58));
        lblTitle.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        topHeader.add(lblTitle, BorderLayout.WEST);

        // Top Nav buttons: TRƯỚC | SAU | TẠO MỚI | SAO CHÉP | XÓA
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topNav.setOpaque(false);

        JButton btnPrev = button("TRƯỚC", DARK_GREEN);
        btnPrev.setPreferredSize(new Dimension(135, 58));
        btnPrev.addActionListener(e -> navigateCustomer(-1));

        JButton btnNext = button("SAU", DARK_GREEN);
        btnNext.setPreferredSize(new Dimension(135, 58));
        btnNext.addActionListener(e -> navigateCustomer(1));

        JButton btnNew = button("TẠO MỚI", DARK_GREEN);
        btnNew.setPreferredSize(new Dimension(135, 58));
        btnNew.addActionListener(e -> openCustomerForm(null));

        JButton btnCopy = button("SAO CHÉP", DARK_GREEN);
        btnCopy.setPreferredSize(new Dimension(135, 58));
        btnCopy.addActionListener(e -> handleDuplicateCustomer());

        JButton btnDel = button("XÓA", RED);
        btnDel.setPreferredSize(new Dimension(135, 58));
        btnDel.addActionListener(e -> handleDeleteCustomerOnForm());

        topNav.add(btnPrev);
        topNav.add(btnNext);
        topNav.add(btnNew);
        topNav.add(btnCopy);
        topNav.add(btnDel);

        topHeader.add(topNav, BorderLayout.EAST);
        root.add(topHeader, BorderLayout.NORTH);

        // Center Form Fields Container using GridBagLayout
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(new EmptyBorder(10, 15, 10, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 10, 6, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // 1. MÃ KHÁCH
        addSingleRow(fields, c, 0, "MÃ KHÁCH", txtMaKhach, TouchIcons.keyboard(28, 24, Color.WHITE),
                () -> openKeyboard(txtMaKhach, "MÃ KHÁCH"));

        // 2. TÊN KHÁCH HÀNG (Nền vàng)
        addSingleRow(fields, c, 1, "TÊN KHÁCH HÀNG", txtName, TouchIcons.keyboard(28, 24, Color.WHITE),
                () -> openKeyboard(txtName, "TÊN KHÁCH HÀNG"));

        // 3. ĐỊA CHỈ
        addSingleRow(fields, c, 2, "ĐỊA CHỈ", txtDiaChi, TouchIcons.keyboard(28, 24, Color.WHITE),
                () -> openKeyboard(txtDiaChi, "ĐỊA CHỈ"));

        // 4. ĐIỆN THOẠI & EMAIL (2 cột trên 1 dòng)
        addDoubleRow(fields, c, 3,
                "ĐIỆN THOẠI", txtDienThoai, TouchIcons.keyboard(28, 24, Color.WHITE), () -> openKeyboard(txtDienThoai, "ĐIỆN THOẠI"),
                "EMAIL", txtEmail, TouchIcons.keyboard(28, 24, Color.WHITE), () -> openKeyboard(txtEmail, "EMAIL"));

        // 5. NHÓM KHÁCH HÀNG
        addSingleRow(fields, c, 4, "NHÓM KHÁCH HÀNG", txtNhomKhachHang, TouchIcons.search(26, 26, Color.WHITE),
                this::pickGroup);

        // 6. MÃ SỐ THUẾ
        addSingleRow(fields, c, 5, "MÃ SỐ THUẾ", txtMaSoThue, TouchIcons.keyboard(28, 24, Color.WHITE),
                () -> openKeyboard(txtMaSoThue, "MÃ SỐ THUẾ"));

        // 7. NHÂN VIÊN
        addSingleRow(fields, c, 6, "NHÂN VIÊN", txtNhanVien, TouchIcons.search(26, 26, Color.WHITE),
                this::pickEmployee);

        // 8. ĐIỂM TÍCH LŨY BAN ĐẦU & NGÀY THÀNH LẬP / SINH NHẬT (2 cột trên 1 dòng)
        addDoubleRow(fields, c, 7,
                "ĐIỂM TÍCH LŨY BAN ĐẦU", txtDiemBanDau, TouchIcons.calculator(26, 26, Color.WHITE), this::openNumpadForPoints,
                "NGÀY THÀNH LẬP / SINH NHẬT", txtNgaySinhNhat, TouchIcons.downArrow(24, 22, Color.WHITE), this::openDatePicker);

        // 9. GIÁ BÁN (ComboBox)
        addComboRow(fields, c, 8, "GIÁ BÁN", cbGiaBan);

        // 10. GHI CHÚ
        addSingleRow(fields, c, 9, "GHI CHÚ", txtGhiChu, TouchIcons.keyboard(28, 24, Color.WHITE),
                () -> openKeyboard(txtGhiChu, "GHI CHÚ"));

        // Glue
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 4;
        c.weighty = 1.0;
        fields.add(Box.createVerticalGlue(), c);

        JScrollPane formScroll = new JScrollPane(fields,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(true);
        formScroll.getViewport().setBackground(BG);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(45);
        root.add(formScroll, BorderLayout.CENTER);

        // Bottom Save Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 10, 8, 10));

        JButton btnSave = button("LƯU", DARK_GREEN);
        btnSave.setPreferredSize(new Dimension(175, 68));
        btnSave.addActionListener(e -> saveCustomer(false, false));

        JButton btnSaveNew = button("LƯU & MỚI", new Color(150, 95, 20));
        btnSaveNew.setPreferredSize(new Dimension(175, 68));
        btnSaveNew.addActionListener(e -> saveCustomer(true, false));

        JButton btnSaveExit = button("LƯU & THOÁT", DARK_GREEN);
        btnSaveExit.setPreferredSize(new Dimension(185, 68));
        btnSaveExit.addActionListener(e -> saveCustomer(false, true));

        JButton btnCancel = button("THOÁT", RED);
        btnCancel.setPreferredSize(new Dimension(145, 68));
        btnCancel.addActionListener(e -> showCatalogue());

        footer.add(btnSave);
        footer.add(btnSaveNew);
        footer.add(btnSaveExit);
        footer.add(btnCancel);

        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private void addSingleRow(JPanel panel, GridBagConstraints c, int row, String labelText,
                              JTextField field, Icon actionIcon, Runnable action) {
        c.gridy = row;
        c.gridwidth = 1;

        // Label
        c.gridx = 0;
        c.weightx = 0;
        panel.add(formLabel(labelText), c);

        // Field + Button
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1.0;

        JPanel fieldWithBtn = new JPanel(new BorderLayout(4, 0));
        fieldWithBtn.setOpaque(false);
        fieldWithBtn.add(field, BorderLayout.CENTER);

        JButton btn = createActionIconButton(actionIcon);
        if (action != null) btn.addActionListener(e -> action.run());
        fieldWithBtn.add(btn, BorderLayout.EAST);

        panel.add(fieldWithBtn, c);
    }

    private void addDoubleRow(JPanel panel, GridBagConstraints c, int row,
                              String l1, JTextField f1, Icon icon1, Runnable act1,
                              String l2, JTextField f2, Icon icon2, Runnable act2) {
        c.gridy = row;
        c.gridwidth = 1;

        // Part 1: Label 1
        c.gridx = 0;
        c.weightx = 0;
        panel.add(formLabel(l1), c);

        // Part 1: Field 1 + Button
        c.gridx = 1;
        c.weightx = 0.5;
        JPanel fb1 = new JPanel(new BorderLayout(4, 0));
        fb1.setOpaque(false);
        fb1.add(f1, BorderLayout.CENTER);
        JButton b1 = createActionIconButton(icon1);
        if (act1 != null) b1.addActionListener(e -> act1.run());
        fb1.add(b1, BorderLayout.EAST);
        panel.add(fb1, c);

        // Part 2: Label 2
        c.gridx = 2;
        c.weightx = 0;
        JLabel lbl2 = formLabel(l2);
        lbl2.setPreferredSize(new Dimension(280, 52));
        panel.add(lbl2, c);

        // Part 2: Field 2 + Button
        c.gridx = 3;
        c.weightx = 0.5;
        JPanel fb2 = new JPanel(new BorderLayout(4, 0));
        fb2.setOpaque(false);
        fb2.add(f2, BorderLayout.CENTER);
        JButton b2 = createActionIconButton(icon2);
        if (act2 != null) b2.addActionListener(e -> act2.run());
        fb2.add(b2, BorderLayout.EAST);
        panel.add(fb2, c);
    }

    private void addComboRow(JPanel panel, GridBagConstraints c, int row, String labelText, JComboBox<?> combo) {
        c.gridy = row;
        c.gridwidth = 1;

        c.gridx = 0;
        c.weightx = 0;
        panel.add(formLabel(labelText), c);

        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1.0;

        combo.setFont(font(22));
        combo.setPreferredSize(new Dimension(0, 52));
        combo.setBackground(Color.WHITE);
        panel.add(combo, c);
    }

    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font(18));
        lbl.setForeground(Color.WHITE);
        lbl.setPreferredSize(new Dimension(260, 52));
        return lbl;
    }

    private JTextField createField(boolean isYellow) {
        JTextField f = new JTextField();
        f.setFont(font(22));
        f.setBackground(isYellow ? YELLOW_FIELD_BG : Color.WHITE);
        f.setForeground(new Color(20, 20, 20));
        f.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        f.setPreferredSize(new Dimension(0, 52));
        return f;
    }

    private JButton createActionIconButton(Icon icon) {
        JButton btn = new JButton(icon);
        btn.setBackground(new Color(0, 85, 110));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        btn.setPreferredSize(new Dimension(54, 52));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void openKeyboard(JTextField targetField, String title) {
        String res = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), title, targetField.getText());
        if (res != null) {
            targetField.setText(res);
        }
    }

    private void openNumpadForPoints() {
        int initial = 0;
        try {
            initial = (int) Double.parseDouble(txtDiemBanDau.getText().trim());
        } catch (Exception ignored) {}
        Integer res = TouchNumberInputDialog.showDialog(SwingUtilities.getWindowAncestor(this), "ĐIỂM TÍCH LŨY BAN ĐẦU", initial);
        if (res != null) {
            txtDiemBanDau.setText(String.valueOf(res));
        }
    }

    private void openDatePicker() {
        String res = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), txtNgaySinhNhat.getText().trim());
        if (res != null) {
            txtNgaySinhNhat.setText(res);
        }
    }

    private void pickGroup() {
        NhomKhachHang grp = TouchChonNhomKhachHangDialog.showDialog(SwingUtilities.getWindowAncestor(this), formSelectedGroup);
        formSelectedGroup = grp;
        txtNhomKhachHang.setText(grp != null ? grp.getName() : "");
    }

    private void pickEmployee() {
        NhanVien nv = TouchChonNhanVienDialog.showDialog(SwingUtilities.getWindowAncestor(this), formSelectedNhanVien);
        formSelectedNhanVien = nv;
        txtNhanVien.setText(nv != null ? nv.getName() : "");
    }

    private void openCustomerForm(KhachHang kh) {
        this.editingCustomer = kh;
        if (kh == null) {
            // New Customer
            this.customerIndex = -1;
            txtMaKhach.setText(khachHangDAO.getNextMaKhach());
            txtName.setText("");
            txtDiaChi.setText("");
            txtDienThoai.setText("");
            txtEmail.setText("");
            txtNhomKhachHang.setText("");
            txtMaSoThue.setText("");
            txtNhanVien.setText("");
            txtDiemBanDau.setText("0");
            txtNgaySinhNhat.setText("");
            cbGiaBan.setSelectedIndex(0);
            txtGhiChu.setText("");
            formSelectedGroup = null;
            formSelectedNhanVien = null;
        } else {
            // Existing Customer
            this.customerIndex = currentCustomerList.indexOf(kh);
            txtMaKhach.setText(kh.getMaKhach() != null ? kh.getMaKhach() : "");
            txtName.setText(kh.getName() != null ? kh.getName() : "");
            txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
            txtDienThoai.setText(kh.getDienThoai() != null ? kh.getDienThoai() : "");
            txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
            txtNhomKhachHang.setText(kh.getTenNhomKhachHang() != null ? kh.getTenNhomKhachHang() : "");
            txtMaSoThue.setText(kh.getMaSoThue() != null ? kh.getMaSoThue() : "");
            txtNhanVien.setText(kh.getTenNhanVien() != null ? kh.getTenNhanVien() : "");
            txtDiemBanDau.setText(String.valueOf((long) kh.getDiemTichLuyBanDau()));
            txtNgaySinhNhat.setText(kh.getNgaySinhNhat() != null ? SDF.format(kh.getNgaySinhNhat()) : "");
            cbGiaBan.setSelectedItem(kh.getGiaBanApDung());
            txtGhiChu.setText(kh.getGhiChu() != null ? kh.getGhiChu() : "");

            if (kh.getNhomKhachHangId() != null) {
                formSelectedGroup = nhomKhachHangDAO.findById(kh.getNhomKhachHangId());
            } else {
                formSelectedGroup = null;
            }

            if (kh.getNhanVienId() != null) {
                formSelectedNhanVien = nhanVienDAO.findById(kh.getNhanVienId());
            } else {
                formSelectedNhanVien = null;
            }
        }
        cards.show(screens, "customer-form");
    }

    private void navigateCustomer(int delta) {
        if (currentCustomerList.isEmpty()) return;
        int next = customerIndex + delta;
        if (next >= 0 && next < currentCustomerList.size()) {
            openCustomerForm(currentCustomerList.get(next));
        }
    }

    private void handleDuplicateCustomer() {
        if (editingCustomer == null) return;
        KhachHang copy = new KhachHang();
        copy.setMaKhach(khachHangDAO.getNextMaKhach());
        copy.setName(editingCustomer.getName() + " - BẢN SAO");
        copy.setDiaChi(editingCustomer.getDiaChi());
        copy.setDienThoai(editingCustomer.getDienThoai());
        copy.setEmail(editingCustomer.getEmail());
        copy.setNhomKhachHangId(editingCustomer.getNhomKhachHangId());
        copy.setTenNhomKhachHang(editingCustomer.getTenNhomKhachHang());
        copy.setMaSoThue(editingCustomer.getMaSoThue());
        copy.setNhanVienId(editingCustomer.getNhanVienId());
        copy.setTenNhanVien(editingCustomer.getTenNhanVien());
        copy.setDiemTichLuyBanDau(editingCustomer.getDiemTichLuyBanDau());
        copy.setNgaySinhNhat(editingCustomer.getNgaySinhNhat());
        copy.setGiaBanApDung(editingCustomer.getGiaBanApDung());
        copy.setGhiChu(editingCustomer.getGhiChu());

        openCustomerForm(copy);
        this.editingCustomer = null; // As new record
    }

    private void handleDeleteCustomerOnForm() {
        if (editingCustomer == null || editingCustomer.getId() == null) {
            openCustomerForm(null);
            return;
        }
        int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            khachHangDAO.delete(editingCustomer.getId());
            showCatalogue();
        }
    }

    private void saveCustomer(boolean andNew, boolean andExit) {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        KhachHang kh = editingCustomer != null ? editingCustomer : new KhachHang();
        kh.setMaKhach(txtMaKhach.getText().trim());
        kh.setName(name);
        kh.setDiaChi(txtDiaChi.getText().trim());
        kh.setDienThoai(txtDienThoai.getText().trim());
        kh.setEmail(txtEmail.getText().trim());
        kh.setMaSoThue(txtMaSoThue.getText().trim());
        kh.setGiaBanApDung((String) cbGiaBan.getSelectedItem());
        kh.setGhiChu(txtGhiChu.getText().trim());

        if (formSelectedGroup != null) {
            kh.setNhomKhachHangId(formSelectedGroup.getId());
            kh.setTenNhomKhachHang(formSelectedGroup.getName());
        } else {
            kh.setNhomKhachHangId(null);
            kh.setTenNhomKhachHang(null);
        }

        if (formSelectedNhanVien != null) {
            kh.setNhanVienId(formSelectedNhanVien.getId());
            kh.setTenNhanVien(formSelectedNhanVien.getName());
        } else {
            kh.setNhanVienId(null);
            kh.setTenNhanVien(null);
        }

        try {
            double diem = Double.parseDouble(txtDiemBanDau.getText().trim());
            kh.setDiemTichLuyBanDau(diem);
            kh.setDiemTichLuy(diem);
        } catch (Exception e) {
            kh.setDiemTichLuyBanDau(0);
            kh.setDiemTichLuy(0);
        }

        String dobStr = txtNgaySinhNhat.getText().trim();
        if (!dobStr.isEmpty()) {
            try {
                Date parsed = SDF.parse(dobStr);
                kh.setNgaySinhNhat(new Timestamp(parsed.getTime()));
            } catch (ParseException ignored) {}
        } else {
            kh.setNgaySinhNhat(null);
        }

        if (kh.getId() == null || kh.getId().trim().isEmpty()) {
            kh.setId(UUID.randomUUID().toString());
            khachHangDAO.insert(kh);
        } else {
            khachHangDAO.update(kh);
        }

        selectedCustomer = kh;

        if (andNew) {
            openCustomerForm(null);
        } else if (andExit) {
            showCatalogue();
        } else {
            editingCustomer = kh;
            JOptionPane.showMessageDialog(this, "Đã lưu thông tin khách hàng!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel basePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 7));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(7, 5, 7, 5));
        return p;
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(font(19));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static Font font(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }
}
