package kazuko.ex.ui.touch;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.quy.PhieuChi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Màn hình danh mục phiếu chi dành cho giao diện cảm ứng (Touch).
 * Hỗ trợ lọc theo ngày, cửa hàng, thêm mới mở TouchPhieuChiPanel, chỉnh sửa, xóa và cấu hình cột.
 */
public final class TouchDanhMucPhieuChiPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color GREEN = new Color(0, 90, 20);
    private static final Color DARK_GREEN = new Color(0, 75, 38);
    private static final Color RED = new Color(165, 10, 10);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final Color GRAY = new Color(145, 145, 145);

    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final CuaHangDAO storeDAO = new CuaHangDAO();
    private final Runnable exit;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final JTextField fromField = createField();
    private final JTextField toField = createField();
    private final JTextField storeField = createField();

    private final CardLayout rootCardLayout = new CardLayout();
    private final JPanel rootScreens = new JPanel(rootCardLayout);
    private final JPanel formContainer = new JPanel(new BorderLayout());

    private final TouchScrollableRowsPanel grid = new TouchScrollableRowsPanel();
    private final JButton editButton = button("CHỈNH SỬA", GRAY);
    private final JButton deleteButton = button("XÓA", GRAY);
    private final List<PhieuChi> expenses = new ArrayList<>();

    private CuaHang selectedStore;
    private PhieuChi selectedExpense;
    private String keyword = "";
    private TouchPhieuChiColumnConfig config;

    public TouchDanhMucPhieuChiPanel(Runnable exit) {
        this.exit = exit;
        dateFormat.setLenient(false);
        setLayout(new BorderLayout());

        rootScreens.add(createCatalogueScreen(), "catalogue");
        rootScreens.add(formContainer, "form");
        rootCardLayout.show(rootScreens, "catalogue");
        add(rootScreens, BorderLayout.CENTER);

        setDefaultDates();
        load();
    }

    private JPanel createCatalogueScreen() {
        JPanel root = base();

        // 1. Thanh tiêu đề & thanh công cụ trên đỉnh
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        JLabel title = label("DANH MỤC PHIẾU CHI", RED, SwingConstants.CENTER, 20);
        title.setPreferredSize(new Dimension(340, 58));
        top.add(title, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton addButton = button("THÊM MỚI", GREEN);
        addButton.addActionListener(e -> openCreateExpense());

        editButton.addActionListener(e -> {
            if (selectedExpense != null) openEditExpense(selectedExpense);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu chi cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });

        deleteButton.addActionListener(e -> deleteSelected());

        JButton refreshButton = button("REFRESH", GREEN);
        refreshButton.addActionListener(e -> {
            keyword = "";
            selectedStore = null;
            storeField.setText("");
            setDefaultDates();
            load();
        });

        JButton findButton = button("TÌM", DARK_GREEN);
        findButton.addActionListener(e -> search());

        for (JButton b : new JButton[]{addButton, editButton, deleteButton, refreshButton, findButton}) {
            b.setPreferredSize(new Dimension(145, 58));
            toolbar.add(b);
        }
        top.add(toolbar, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        // 2. Vùng trung tâm: Khung lọc bên trái + Lưới danh sách bên phải
        JPanel center = new JPanel(new BorderLayout(10, 0));
        center.setOpaque(false);
        center.add(createFiltersPanel(), BorderLayout.WEST);

        JPanel gridArea = new JPanel(new BorderLayout());
        gridArea.setBackground(new Color(232, 232, 232));
        gridArea.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));

        grid.setBackground(new Color(232, 232, 232));
        JScrollPane scroll = new JScrollPane(grid,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
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
        gearButton.addActionListener(e -> new TouchPhieuChiSettingsDialog(
                SwingUtilities.getWindowAncestor(this), this::reloadConfig).setVisible(true));
        gearBox.add(gearButton);
        gridArea.add(gearBox, BorderLayout.SOUTH);

        center.add(gridArea, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // 3. Chân trang: Nút THOÁT góc phải
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        JButton close = button("THOÁT", RED);
        close.setPreferredSize(new Dimension(180, 58));
        close.addActionListener(e -> {
            if (exit != null) exit.run();
        });
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private JPanel createFiltersPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(HEADER);
        p.setPreferredSize(new Dimension(340, 0));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 0, 3, 0);

        int y = 0;

        // Tiêu đề Ngày
        JLabel dayLabel = new JLabel("Ngày");
        dayLabel.setForeground(Color.WHITE);
        dayLabel.setFont(font(18));
        c.gridy = y++;
        p.add(dayLabel, c);

        // Khung ngày TỪ / ĐẾN + Nút mũi tên chọn nhanh [▼]
        JPanel dates = new JPanel(new GridBagLayout());
        dates.setOpaque(false);
        GridBagConstraints d = new GridBagConstraints();
        d.insets = new Insets(2, 2, 2, 2);
        d.fill = GridBagConstraints.HORIZONTAL;

        // Row TỪ:
        d.gridx = 0;
        d.gridy = 0;
        d.weightx = 0;
        dates.add(small("TỪ:"), d);
        d.gridx = 1;
        d.weightx = 1;
        fromField.setEditable(false);
        fromField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fromField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { pickFromDate(); }
        });
        dates.add(fromField, d);

        // Row ĐẾN:
        d.gridx = 0;
        d.gridy = 1;
        d.weightx = 0;
        dates.add(small("ĐẾN:"), d);
        d.gridx = 1;
        d.weightx = 1;
        toField.setEditable(false);
        toField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { pickToDate(); }
        });
        dates.add(toField, d);

        // Nút mũi tên lớn [▼] chọn nhanh ngày
        JButton calendar = button("▼", TEAL);
        calendar.setFont(font(30));
        calendar.setPreferredSize(new Dimension(65, 102));
        calendar.addActionListener(e -> chooseQuickDate());
        d.gridx = 2;
        d.gridy = 0;
        d.gridheight = 2;
        d.weightx = 0;
        dates.add(calendar, d);

        c.gridy = y++;
        p.add(dates, c);

        // Cửa hàng
        c.insets = new Insets(10, 0, 2, 0);
        JLabel storeLabel = new JLabel("Cửa hàng");
        storeLabel.setForeground(Color.WHITE);
        storeLabel.setFont(font(18));
        c.gridy = y++;
        p.add(storeLabel, c);

        c.insets = new Insets(2, 0, 3, 0);
        JPanel storeRow = new JPanel(new BorderLayout(6, 0));
        storeRow.setOpaque(false);
        storeField.setEditable(false);
        storeField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        storeField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { chooseStore(); }
        });

        JButton searchStoreBtn = button("⌕", TEAL);
        searchStoreBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 34));
        searchStoreBtn.setPreferredSize(new Dimension(65, 50));
        searchStoreBtn.addActionListener(e -> chooseStore());

        storeRow.add(storeField, BorderLayout.CENTER);
        storeRow.add(searchStoreBtn, BorderLayout.EAST);
        c.gridy = y++;
        p.add(storeRow, c);

        // Khoảng co giãn dưới cùng
        c.gridy = y++;
        c.weighty = 1;
        p.add(Box.createVerticalGlue(), c);

        return p;
    }

    private void setDefaultDates() {
        Calendar cal = Calendar.getInstance();
        toField.setText(dateFormat.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_MONTH, -30);
        fromField.setText(dateFormat.format(cal.getTime()));
    }

    private void pickFromDate() {
        String picked = TouchDatePickerDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), fromField.getText().trim());
        if (picked != null) {
            fromField.setText(picked);
            load();
        }
    }

    private void pickToDate() {
        String picked = TouchDatePickerDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), toField.getText().trim());
        if (picked != null) {
            toField.setText(picked);
            load();
        }
    }

    private void chooseQuickDate() {
        TouchChonNhanhNgayDialog.showDialog(SwingUtilities.getWindowAncestor(this), (from, to) -> {
            if (from != null) fromField.setText(dateFormat.format(from));
            if (to != null) toField.setText(dateFormat.format(to));
            load();
        });
    }

    private void chooseStore() {
        CuaHang chosen = TouchChonCuaHangDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), storeDAO, selectedStore);
        if (TouchChonCuaHangDialog.wasAccepted()) {
            selectedStore = chosen;
            storeField.setText(chosen != null ? safe(chosen.getName()) : "");
            load();
        }
    }

    private Date parseDate(String text) {
        try {
            return dateFormat.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }

    public void load() {
        expenses.clear();
        Date dFrom = parseDate(fromField.getText().trim());
        Date dTo = parseDate(toField.getText().trim());
        String storeId = selectedStore != null ? selectedStore.getId() : null;

        List<PhieuChi> list = phieuChiDAO.getReportPhieuChiTheoNgay(dFrom, dTo, null, storeId);
        if (!keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            list = list.stream().filter(pc ->
                    safe(pc.getSoPhieu()).toLowerCase().contains(lower)
                            || safe(pc.getTenDoiTuong()).toLowerCase().contains(lower)
                            || safe(pc.getTenPhanLoai()).toLowerCase().contains(lower)
                            || safe(pc.getLyDoChi()).toLowerCase().contains(lower)
                            || safe(pc.getGhiChu()).toLowerCase().contains(lower)
            ).toList();
        }

        expenses.addAll(list);
        selectedExpense = null;
        config = TouchPhieuChiConfigDAO.load();
        render();
        updateActions();
    }

    private void reloadConfig() {
        config = TouchPhieuChiConfigDAO.load();
        render();
    }

    private void render() {
        if (config == null) config = TouchPhieuChiConfigDAO.load();
        grid.removeAll();
        grid.configureGrid(config.getGridColumns(), config.getGridRows(), 7, 7);
        grid.setBorder(new EmptyBorder(7, 7, 7, 7));

        int height = Math.max(1, 790 / config.getGridRows());
        for (PhieuChi pc : expenses) {
            grid.add(createCard(pc, height));
        }
        grid.completeGrid();
        grid.revalidate();
        grid.repaint();
    }

    private JPanel createCard(PhieuChi expense, int height) {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 0));
        p.setBackground(expense == selectedExpense ? ORANGE : config.getCardBgColor());
        p.setBorder(new LineBorder(new Color(220, 235, 235), 2, true));
        p.setPreferredSize(new Dimension(350, height));

        for (var item : config.getUsedColumns()) {
            String text = TouchPhieuChiColumnConfig.value(expense, item, config.isShowTitle());
            if (text.isBlank()) continue;
            JLabel l = new JLabel(text, item.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setForeground(item.getColor());
            l.setFont(item.getFont());
            l.setBorder(new EmptyBorder(2, 10, 2, 10));
            p.add(l);
        }

        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedExpense = expense;
                render();
                updateActions();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditExpense(expense);
                }
            }
        };

        p.addMouseListener(listener);
        for (Component c : p.getComponents()) c.addMouseListener(listener);
        return p;
    }

    private void updateActions() {
        boolean hasSelected = selectedExpense != null;
        editButton.setBackground(hasSelected ? GREEN : GRAY);
        deleteButton.setBackground(hasSelected ? RED : GRAY);
    }

    private void search() {
        String q = TouchTextKeyboardDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), "TÌM KIẾM PHIẾU CHI", keyword);
        if (q != null) {
            keyword = q.trim();
            load();
        }
    }

    private void openCreateExpense() {
        formContainer.removeAll();
        TouchPhieuChiPanel form = new TouchPhieuChiPanel(this::returnToCatalogue);
        formContainer.add(form, BorderLayout.CENTER);
        formContainer.revalidate();
        formContainer.repaint();
        rootCardLayout.show(rootScreens, "form");
    }

    private void openEditExpense(PhieuChi expense) {
        formContainer.removeAll();
        TouchPhieuChiPanel form = new TouchPhieuChiPanel(this::returnToCatalogue, expense);
        formContainer.add(form, BorderLayout.CENTER);
        formContainer.revalidate();
        formContainer.repaint();
        rootCardLayout.show(rootScreens, "form");
    }

    private void returnToCatalogue() {
        rootCardLayout.show(rootScreens, "catalogue");
        load();
    }

    private void deleteSelected() {
        if (selectedExpense == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu chi cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this),
                "BẠN CÓ MUỐN XÓA PHIẾU CHI<br>" + safe(selectedExpense.getSoPhieu()).toUpperCase() + " KHÔNG?")) {
            return;
        }
        if (phieuChiDAO.delete(selectedExpense.getId())) {
            load();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xóa phiếu chi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel base() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(7, 7, 7, 7));
        return p;
    }

    private static JTextField createField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        f.setPreferredSize(new Dimension(170, 48));
        f.setBorder(new EmptyBorder(4, 7, 4, 7));
        return f;
    }

    private static JLabel small(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        l.setFont(font(17));
        return l;
    }

    private static JLabel label(String t, Color bg, int align, int size) {
        JLabel l = new JLabel(t, align);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(Color.WHITE);
        l.setFont(font(size));
        l.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        return l;
    }

    private static JButton button(String t, Color bg) {
        JButton b = new JButton(t);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(font(18));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        return b;
    }

    private static Font font(int s) { return new Font("Segoe UI", Font.BOLD, s); }
    private static String safe(String v) { return v == null ? "" : v; }
}
