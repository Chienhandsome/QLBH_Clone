package kazuko.ex.ui.touch;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.khohang.TheKhoEntry;
import kazuko.ex.model.khohang.TonKhoItem;
import kazuko.ex.util.ExcelExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Full-screen touch stock catalogue backed by the existing stock DAO. */
final class TouchTonKhoPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(79, 139, 169),
            TEAL = new Color(0, 79, 76), GREEN = new Color(0, 105, 10), RED = new Color(165, 10, 10),
            ORANGE = new Color(255, 128, 0), BLUE = new Color(0, 0, 105), OLIVE = new Color(145, 135, 0),
            GRAY = new Color(145, 145, 145), LIGHT = new Color(232, 232, 232);
    private final DonHangDAO dao = new DonHangDAO();
    private final KhoHangDAO warehouseDao = new KhoHangDAO();
    private final NhomMatHangDAO groupDao = new NhomMatHangDAO();
    private final Runnable exit;
    private final JPanel warehouseList = verticalList(), groupList = verticalList();
    private final TouchScrollableRowsPanel cardGrid = new TouchScrollableRowsPanel();
    private final JScrollPane cardScroll = new JScrollPane(cardGrid, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private final JButton detail = button("CHI TIẾT", GRAY), nonZero = button("☒  TỒN <> 0", BG);
    private final List<TonKhoItem> stocks = new ArrayList<>(), filtered = new ArrayList<>();
    private TouchTonKhoColumnConfig config;
    private String warehouseId, groupId, keyword = "";
    private TonKhoItem selected;
    private boolean nonZeroOnly = true;

    TouchTonKhoPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout(0, 7)); setBackground(BG); setBorder(new EmptyBorder(7, 7, 7, 7));
        JLabel header = label("TỒN KHO", HEADER, SwingConstants.CENTER, 27); header.setPreferredSize(new Dimension(0, 65)); add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(8, 0)); center.setOpaque(false); center.add(sidebar(), BorderLayout.WEST);
        cardGrid.setBackground(LIGHT); cardScroll.setBorder(new LineBorder(new Color(210, 230, 235), 2, true)); cardScroll.getVerticalScrollBar().setUnitIncrement(50); center.add(cardScroll); add(center);
        add(footer(), BorderLayout.SOUTH); reloadAll();
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(); side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS)); side.setBackground(HEADER); side.setPreferredSize(new Dimension(285, 0));
        side.add(section("KHO HÀNG")); JScrollPane ws = scroll(warehouseList); ws.setPreferredSize(new Dimension(0, 230)); ws.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260)); side.add(ws);
        side.add(Box.createVerticalStrut(7)); side.add(section("NHÓM HÀNG")); JScrollPane gs = scroll(groupList); gs.setPreferredSize(new Dimension(0, 430)); side.add(gs);
        nonZero.setHorizontalAlignment(SwingConstants.LEFT); nonZero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55)); nonZero.setPreferredSize(new Dimension(0, 55)); nonZero.setBorder(new EmptyBorder(4, 8, 4, 8)); nonZero.addActionListener(e -> { nonZeroOnly = !nonZeroOnly; nonZero.setText((nonZeroOnly ? "☒" : "☐") + "  TỒN <> 0"); applyFilters(); }); side.add(nonZero);
        return side;
    }

    private JPanel footer() {
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); footer.setBorder(new EmptyBorder(0, 292, 0, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0)); left.setOpaque(false);
        JButton refresh = button("REFRESH", GREEN), excel = button("XUẤT EXCEL", GREEN), find = button("TÌM", GREEN);
        for (JButton b : new JButton[]{refresh, detail, excel, find}) { b.setPreferredSize(new Dimension(155, 60)); left.add(b); }
        refresh.addActionListener(e -> { keyword = ""; reloadStock(); }); detail.addActionListener(e -> showDetail()); excel.addActionListener(e -> exportExcel()); find.addActionListener(e -> search()); footer.add(left);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0)); right.setOpaque(false); JButton gear = button("", BG); gear.setIcon(new TouchMatHangPanel.GearIcon(38, Color.WHITE)); gear.setPreferredSize(new Dimension(65, 60)); gear.addActionListener(e -> new TouchTonKhoSettingsDialog(SwingUtilities.getWindowAncestor(this), this::reloadConfig).setVisible(true)); right.add(gear); JButton close = button("THOÁT", RED); close.setPreferredSize(new Dimension(190, 60)); close.addActionListener(e -> exit.run()); right.add(close); footer.add(right, BorderLayout.EAST); return footer;
    }

    private void reloadAll() { config = TouchTonKhoConfigDAO.load(); renderWarehouses(); renderGroups(); reloadStock(); }
    private void reloadConfig() { config = TouchTonKhoConfigDAO.load(); renderCards(); }
    private void reloadStock() { stocks.clear(); stocks.addAll(dao.getTonKhoDanhSach(warehouseId)); selected = null; applyFilters(); }

    private void renderWarehouses() {
        warehouseList.removeAll(); warehouseList.add(choice("TẤT CẢ", warehouseId == null, BLUE, () -> { warehouseId = null; renderWarehouses(); reloadStock(); }));
        for (KhoHang x : warehouseDao.getAllActive()) warehouseList.add(choice(safe(x.getName()), x.getId() != null && x.getId().equals(warehouseId), GREEN, () -> { warehouseId = x.getId(); renderWarehouses(); reloadStock(); }));
        warehouseList.add(Box.createVerticalGlue()); refresh(warehouseList);
    }

    private void renderGroups() {
        groupList.removeAll(); groupList.add(choice("TẤT CẢ", groupId == null, BLUE, () -> { groupId = null; renderGroups(); applyFilters(); }));
        for (NhomMatHang x : groupDao.getAll()) { if (x.isSeparator() || x.isFolder()) continue; groupList.add(choice(safe(x.getName()), x.getId() != null && x.getId().equals(groupId), GREEN, () -> { groupId = x.getId(); renderGroups(); applyFilters(); })); }
        groupList.add(Box.createVerticalGlue()); refresh(groupList);
    }

    private JButton choice(String text, boolean active, Color normal, Runnable action) {
        JButton b = button(text, active ? ORANGE : normal); b.setHorizontalAlignment(SwingConstants.LEFT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74)); b.setPreferredSize(new Dimension(0, 74)); b.setBorder(new EmptyBorder(6, 12, 6, 8)); b.addActionListener(e -> action.run()); return b;
    }

    private void applyFilters() {
        filtered.clear(); String q = keyword.trim().toLowerCase(Locale.ROOT);
        for (TonKhoItem x : stocks) {
            if (nonZeroOnly && Math.abs(x.getTon()) < .0001) continue;
            if (groupId != null && (x.getNhomHangId() == null || !groupId.equalsIgnoreCase(x.getNhomHangId()))) continue;
            if (!q.isEmpty() && !(safe(x.getMaHang()) + " " + safe(x.getTenHang()) + " " + safe(x.getHangSx())).toLowerCase(Locale.ROOT).contains(q)) continue;
            filtered.add(x);
        }
        if (selected != null && !filtered.contains(selected)) selected = null; renderCards(); updateActions();
    }

    private void renderCards() {
        if (config == null) config = TouchTonKhoConfigDAO.load(); cardGrid.removeAll(); cardGrid.configureGrid(config.getGridColumns(), config.getGridRows(), 7, 7); cardGrid.setBorder(new EmptyBorder(7, 7, 7, 7));
        int viewportHeight = Math.max(1, cardScroll.getViewport().getHeight()); int cardHeight = Math.max(1, (viewportHeight - (config.getGridRows() + 1) * 7) / config.getGridRows());
        for (TonKhoItem stock : filtered) cardGrid.add(stockCard(stock, cardHeight));
        cardGrid.completeGrid(); refresh(cardGrid);
    }

    private JPanel stockCard(TonKhoItem stock, int height) {
        JPanel card = new JPanel(); card.setLayout(new GridBagLayout()); card.setBackground(stock == selected ? ORANGE : config.getCardBgColor()); card.setBorder(new LineBorder(new Color(220, 235, 235), 2, true)); card.setPreferredSize(new Dimension(380, height)); card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int count = Math.max(1, config.getUsedColumns().size()); int row = 0;
        for (var item : config.getUsedColumns()) {
            String value = TouchTonKhoColumnConfig.value(stock, item, config.isShowTitle());
            JLabel l = new JLabel(value, item.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.LEFT); l.setForeground(item.getColor()); l.setFont(item.getFont()); l.setBorder(new EmptyBorder(2, 12, 2, 12));
            GridBagConstraints c = new GridBagConstraints(); c.gridx = 0; c.gridy = row++; c.weightx = 1; c.weighty = 1d / count; c.fill = GridBagConstraints.BOTH; card.add(l, c);
        }
        var listener = new java.awt.event.MouseAdapter() { @Override public void mousePressed(java.awt.event.MouseEvent e) { selected = stock; renderCards(); updateActions(); } @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) showDetail(); } };
        card.addMouseListener(listener); for (Component child : card.getComponents()) child.addMouseListener(listener); return card;
    }

    private void updateActions() { detail.setBackground(selected == null ? GRAY : GREEN); }
    private void search() { String value = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), "TÌM MẶT HÀNG TỒN KHO", keyword); if (value != null) { keyword = value.trim(); applyFilters(); } }
    private void showDetail() { if (selected != null) new TouchTonKhoDetailDialog(SwingUtilities.getWindowAncestor(this), selected, warehouseId, dao).setVisible(true); }

    private void exportExcel() {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"Mã hàng", "Mặt hàng", "Nhóm hàng", "Tồn", "ĐVT", "Tồn 2 ĐVT", "Giá bán", "Giá vốn", "Bảo hành", "Hãng SX", "Tồn tối thiểu"}, 0);
        for (TonKhoItem x : filtered) m.addRow(new Object[]{x.getMaHang(), x.getTenHang(), x.getNhomHangName(), x.getTon(), x.getDonViTinh(), x.getTon2Dvt(), x.getGiaBan(), x.getGiaVon(), x.getBaoHanh(), x.getHangSx(), x.getTonToiThieu()});
        ExcelExporter.exportTable(new JTable(m), "Ton_Kho", "Tồn kho", this);
    }

    private static JPanel verticalList() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(HEADER); p.setBorder(new EmptyBorder(4, 4, 4, 4)); return p; }
    private static JScrollPane scroll(JPanel p) { JScrollPane s = new JScrollPane(p, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); s.setBorder(null); s.getVerticalScrollBar().setUnitIncrement(40); return s; }
    private static JLabel section(String text) { JLabel l = label(text, OLIVE, SwingConstants.CENTER, 17); l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32)); l.setPreferredSize(new Dimension(0, 32)); return l; }
    private static JLabel label(String text, Color bg, int align, int size) { JLabel l = new JLabel(text, align); l.setOpaque(true); l.setBackground(bg); l.setForeground(Color.WHITE); l.setFont(font(size)); l.setBorder(new LineBorder(new Color(210, 230, 235), 2, true)); return l; }
    private static JButton button(String text, Color bg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFont(font(18)); b.setFocusPainted(false); b.setBorder(new LineBorder(new Color(210, 230, 235), 2, true)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
    private static String safe(String value) { return value == null ? "" : value; }
    private static void refresh(JComponent c) { c.revalidate(); c.repaint(); }
}

/** Touch-friendly inventory movement and expiry details for one item. */
final class TouchTonKhoDetailDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(79, 139, 169), RED = new Color(165, 10, 10), LIGHT = new Color(232, 232, 232);
    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0.##");
    TouchTonKhoDetailDialog(Window owner, TonKhoItem stock, String warehouseId, DonHangDAO dao) {
        super(owner, "CHI TIẾT TỒN KHO", ModalityType.APPLICATION_MODAL); setUndecorated(true); setContentPane(content(stock, warehouseId, dao));
        GraphicsConfiguration gc = owner == null ? null : owner.getGraphicsConfiguration(); Rectangle bounds = gc == null ? GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds() : gc.getBounds(); setBounds(bounds); setLocationRelativeTo(owner);
    }
    private JPanel content(TonKhoItem stock, String warehouseId, DonHangDAO dao) {
        JPanel root = new JPanel(new BorderLayout(0, 8)); root.setBackground(BG); root.setBorder(new EmptyBorder(7, 7, 7, 7));
        JLabel h = new JLabel("CHI TIẾT TỒN KHO - " + safe(stock.getTenHang()).toUpperCase(), SwingConstants.CENTER); h.setOpaque(true); h.setBackground(HEADER); h.setForeground(Color.WHITE); h.setFont(new Font("Segoe UI", Font.BOLD, 25)); h.setPreferredSize(new Dimension(0, 68)); root.add(h, BorderLayout.NORTH);
        JPanel summary = new JPanel(new GridLayout(2, 4, 7, 7)); summary.setOpaque(false); summary.setBorder(new EmptyBorder(0, 0, 5, 0));
        summary.add(info("MÃ HÀNG", stock.getMaHang())); summary.add(info("NHÓM HÀNG", stock.getNhomHangName())); summary.add(info("ĐƠN VỊ TÍNH", stock.getDonViTinh())); summary.add(info("TỒN", NUMBER.format(stock.getTon()))); summary.add(info("GIÁ BÁN", NUMBER.format(stock.getGiaBan()))); summary.add(info("GIÁ VỐN", NUMBER.format(stock.getGiaVon()))); summary.add(info("BẢO HÀNH", stock.getBaoHanh())); summary.add(info("HÃNG SX", stock.getHangSx()));
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("Segoe UI", Font.BOLD, 18)); tabs.addTab("THẺ KHO", movementTable(stock, warehouseId, dao)); tabs.addTab("TỒN THEO HẠN DÙNG", expiryTable(stock, warehouseId, dao));
        JPanel center = new JPanel(new BorderLayout(0, 7)); center.setBackground(LIGHT); center.add(summary, BorderLayout.NORTH); center.add(tabs); root.add(center);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); foot.setOpaque(false); JButton close = new JButton("THOÁT"); close.setBackground(RED); close.setForeground(Color.WHITE); close.setFont(new Font("Segoe UI", Font.BOLD, 19)); close.setPreferredSize(new Dimension(210, 62)); close.addActionListener(e -> dispose()); foot.add(close); root.add(foot, BorderLayout.SOUTH); return root;
    }
    private JScrollPane movementTable(TonKhoItem stock, String warehouseId, DonHangDAO dao) {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"Số phiếu", "Ngày", "Diễn giải", "Đơn giá", "Thành tiền", "% giảm", "ĐVT", "Nhập", "Xuất", "Tồn", "Đối tượng"}, 0);
        java.util.Date from = new java.util.Date(0), to = new java.util.Date(System.currentTimeMillis() + 86_400_000L); SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        for (TheKhoEntry x : dao.getTheKho(stock.getMatHangId(), warehouseId, from, to)) m.addRow(new Object[]{x.getSoPhieu(), x.getNgay() == null ? "" : date.format(x.getNgay()), x.getDienGiai(), x.getDonGia(), x.getThanhTien(), x.getTiLeGiamGia(), x.getTenDonViTinh(), x.getSlNhap(), x.getSlXuat(), x.getTonLuyKe(), x.getDoiTuong()});
        return table(m);
    }
    private JScrollPane expiryTable(TonKhoItem stock, String warehouseId, DonHangDAO dao) { DefaultTableModel m = new DefaultTableModel(new Object[]{"Hạn dùng", "Tồn"}, 0); for (Object[] x : dao.getTonTheoHanDung(stock.getMatHangId(), warehouseId)) m.addRow(new Object[]{x[0], x[1]}); return table(m); }
    private static JScrollPane table(DefaultTableModel m) { JTable t = new JTable(m); t.setRowHeight(38); t.setFont(new Font("Segoe UI", Font.PLAIN, 16)); t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16)); return new JScrollPane(t); }
    private static JPanel info(String title, String value) { JPanel p = new JPanel(new BorderLayout()); p.setBackground(new Color(0, 79, 76)); p.setBorder(new EmptyBorder(7, 12, 7, 12)); JLabel a = new JLabel(title); a.setForeground(new Color(210, 230, 235)); a.setFont(new Font("Segoe UI", Font.BOLD, 14)); JLabel b = new JLabel(safe(value)); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI", Font.BOLD, 19)); p.add(a, BorderLayout.NORTH); p.add(b); return p; }
    private static String safe(String s) { return s == null ? "" : s; }
}
