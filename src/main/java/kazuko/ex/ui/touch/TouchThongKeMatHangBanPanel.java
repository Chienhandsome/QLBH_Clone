package kazuko.ex.ui.touch;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.banhang.ThongKeMatHangBanRow;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.report.service.BaoCaoBanHangReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/** Touch-first sold-products statistics screen. */
final class TouchThongKeMatHangBanPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(79, 139, 169), TEAL = new Color(0, 79, 76);
    private static final Color GREEN = new Color(0, 110, 5), ORANGE = new Color(226, 79, 5), NAVY = new Color(0, 0, 100), RED = new Color(165, 15, 15), PALE = new Color(255, 255, 190);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0.##");
    private final HoaDonDAO invoiceDAO = new HoaDonDAO();
    private final MatHangDAO productDAO = new MatHangDAO();
    private final NhomMatHangDAO categoryDAO = new NhomMatHangDAO();
    private final CuaHangDAO storeDAO = new CuaHangDAO();
    private final BaoCaoBanHangReportService reportService = new BaoCaoBanHangReportService();
    private final Runnable exit;
    private final JTextField fromField = field(), toField = field(), storeField = field();
    private final JPanel categories = new JPanel(), resultRows = new JPanel();
    private final JLabel totalSale = valueLabel(), totalCost = valueLabel(), grossProfit = valueLabel(), totalDiscount = valueLabel(), netProfit = valueLabel();
    private final JButton refresh = TouchThongKeDoanhThuPanel.button("REFRESH", GREEN), print = TouchThongKeDoanhThuPanel.button("IN BÁO CÁO", GREEN);
    private List<ThongKeMatHangBanRow> allRows = new ArrayList<>(), visibleRows = new ArrayList<>();
    private CuaHang selectedStore; private String selectedCategoryId; private ThongKeMatHangBanRow selectedRow;

    TouchThongKeMatHangBanPanel(Runnable exit) {
        this.exit = exit; setLayout(new BorderLayout(7, 7)); setBackground(BG); setBorder(new EmptyBorder(8, 8, 8, 8));
        add(header(), BorderLayout.NORTH); add(body(), BorderLayout.CENTER);
        String today = LocalDate.now().format(DATE_FORMAT); fromField.setText(today); toField.setText(today); storeField.setEditable(false);
        loadCategories(); SwingUtilities.invokeLater(this::reload);
    }

    private JPanel header() { JPanel p = new JPanel(new BorderLayout()); p.setBackground(HEADER); p.setPreferredSize(new Dimension(0, 62)); JLabel l = new JLabel("THỐNG KÊ MẶT HÀNG BÁN", SwingConstants.CENTER); l.setForeground(Color.WHITE); l.setFont(TouchThongKeDoanhThuPanel.font(26)); p.add(l); return p; }

    private JPanel body() {
        JPanel p = new JPanel(new BorderLayout(7, 0)); p.setOpaque(false); p.add(leftPanel(), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(0, 7)); right.setOpaque(false);
        resultRows.setLayout(new GridLayout(0, 2, 7, 7)); resultRows.setBackground(new Color(232, 232, 232)); resultRows.setBorder(new EmptyBorder(7, 7, 7, 7));
        JScrollPane scroll = new JScrollPane(resultRows, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(45);
        JButton gear = new JButton(new TouchMatHangPanel.GearIcon(27, Color.WHITE)); gear.setBackground(BG); gear.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); gear.addActionListener(e -> new TouchMatHangBanSettingsDialog(SwingUtilities.getWindowAncestor(this), this::renderRows).setVisible(true)); scroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, gear);
        right.add(scroll, BorderLayout.CENTER); right.add(footer(), BorderLayout.SOUTH); p.add(right, BorderLayout.CENTER); return p;
    }

    private JPanel leftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 7)); left.setBackground(HEADER); left.setPreferredSize(new Dimension(340, 0));
        JPanel filters = new JPanel(); filters.setOpaque(false); filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS)); filters.setBorder(new EmptyBorder(0, 4, 0, 4));
        filters.add(dateRow("TỪ:", fromField)); filters.add(Box.createVerticalStrut(5)); filters.add(dateRow("ĐẾN:", toField)); filters.add(Box.createVerticalStrut(7));
        JLabel storeTitle = whiteLabel("CỬA HÀNG"); filters.add(storeTitle); filters.add(Box.createVerticalStrut(4));
        JPanel storeRow = new JPanel(new BorderLayout(7, 0)); storeRow.setOpaque(false); storeField.setPreferredSize(new Dimension(0, 50)); storeRow.add(storeField);
        JButton find = TouchThongKeDoanhThuPanel.button("⌕", TEAL); find.setFont(new Font("Segoe UI Symbol", Font.BOLD, 34)); find.setPreferredSize(new Dimension(72, 50)); find.addActionListener(e -> chooseStore()); storeRow.add(find, BorderLayout.EAST); filters.add(storeRow);
        left.add(filters, BorderLayout.NORTH);
        categories.setLayout(new BoxLayout(categories, BoxLayout.Y_AXIS)); categories.setBackground(HEADER); categories.setBorder(new EmptyBorder(0, 5, 5, 5)); JScrollPane catScroll = new JScrollPane(categories); catScroll.setBorder(null); catScroll.getVerticalScrollBar().setUnitIncrement(40); left.add(catScroll, BorderLayout.CENTER); return left;
    }

    private JPanel dateRow(String title, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(7, 0)); p.setOpaque(false); JLabel l = whiteLabel(title); l.setPreferredSize(new Dimension(58, 50)); p.add(l, BorderLayout.WEST); field.setPreferredSize(new Dimension(0, 50)); p.add(field);
        JButton pick = TouchThongKeDoanhThuPanel.button("▼", TEAL); pick.setPreferredSize(new Dimension(72, 50)); pick.setFont(TouchThongKeDoanhThuPanel.font(25)); pick.addActionListener(e -> pickDate(field)); p.add(pick, BorderLayout.EAST); return p;
    }

    private JPanel footer() {
        JPanel footer = new JPanel(new BorderLayout(12, 0)); footer.setOpaque(false); footer.setPreferredSize(new Dimension(0, 165));
        JPanel primary = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 28)); primary.setOpaque(false);
        primary.add(summaryBlock("TIỀN BÁN", totalSale, 285)); primary.add(summaryBlock("TIỀN NHẬP", totalCost, 285));
        footer.add(primary, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 7)); right.setOpaque(false); right.setPreferredSize(new Dimension(750, 0));
        JPanel profit = new JPanel(new GridLayout(3, 1, 0, 5)); profit.setOpaque(false);
        profit.add(summaryLine("LÃI HÀNG BÁN (BÁN - NHẬP)", grossProfit));
        profit.add(summaryLine("TIỀN GIẢM GIÁ", totalDiscount));
        profit.add(summaryLine("TIỀN LÃI", netProfit)); right.add(profit, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0)); actions.setOpaque(false); actions.setPreferredSize(new Dimension(0, 66));
        refresh.addActionListener(e -> reload()); print.addActionListener(e -> openPrint()); JButton close = TouchThongKeDoanhThuPanel.button("THOÁT", RED); close.addActionListener(e -> exit.run());
        actions.add(refresh); actions.add(print); actions.add(close); right.add(actions, BorderLayout.SOUTH); footer.add(right, BorderLayout.EAST); return footer;
    }

    private JPanel summaryBlock(String title, JLabel value, int width) {
        JPanel p = new JPanel(new BorderLayout(10, 0)); p.setOpaque(false); p.setPreferredSize(new Dimension(width, 43)); JLabel l = whiteLabel(title); l.setHorizontalAlignment(SwingConstants.RIGHT); p.add(l, BorderLayout.CENTER); value.setPreferredSize(new Dimension(165, 38)); p.add(value, BorderLayout.EAST); return p;
    }

    private JPanel summaryLine(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(10, 0)); p.setOpaque(false); JLabel l = whiteLabel(title); l.setHorizontalAlignment(SwingConstants.RIGHT); p.add(l, BorderLayout.CENTER); value.setPreferredSize(new Dimension(210, 34)); p.add(value, BorderLayout.EAST); return p;
    }

    private void loadCategories() {
        categories.removeAll(); addCategory(null, "TẤT CẢ", NAVY);
        try { List<NhomMatHang> list = categoryDAO.getAll(); list.sort(Comparator.comparing(NhomMatHang::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))); for (NhomMatHang n : list) if (!n.isFolder() && !n.isSeparator()) addCategory(n.getId(), n.getName(), GREEN); } catch (Exception ignored) {}
        categories.revalidate(); categories.repaint();
    }

    private void addCategory(String id, String name, Color base) {
        boolean active = Objects.equals(id, selectedCategoryId); JButton b = TouchThongKeDoanhThuPanel.button(name, active && id != null ? ORANGE : base); b.setHorizontalAlignment(SwingConstants.LEFT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82)); b.setPreferredSize(new Dimension(0, 82));
        b.addActionListener(e -> { selectedCategoryId = id; loadCategories(); filterRows(); }); categories.add(b); categories.add(Box.createVerticalStrut(7));
    }

    private void reload() {
        Date from = parse(fromField.getText()), to = parse(toField.getText()); if (from == null || to == null || from.after(to)) { warn("Khoảng ngày không hợp lệ."); return; }
        setBusy(true); String storeId = selectedStore == null ? "" : selectedStore.getId(); new SwingWorker<List<ThongKeMatHangBanRow>, Void>() {
            @Override protected List<ThongKeMatHangBanRow> doInBackground() { return calculate(from, to, storeId); }
            @Override protected void done() { try { allRows = get(); } catch (Exception ex) { allRows = new ArrayList<>(); warn("Không thể tải thống kê: " + rootMessage(ex)); } selectedRow = null; filterRows(); setBusy(false); }
        }.execute();
    }

    private List<ThongKeMatHangBanRow> calculate(Date from, Date to, String storeId) {
        Map<String, MatHang> products = new HashMap<>(); for (MatHang m : productDAO.getAll()) { if (m.getId() != null) products.put(m.getId(), m); if (m.getCode() != null) products.put(m.getCode().toLowerCase(), m); }
        Map<String, String> categoryNames = new HashMap<>(); for (NhomMatHang n : categoryDAO.getAll()) if (n.getId() != null) categoryNames.put(n.getId(), n.getName());
        Map<String, Aggregate> map = new LinkedHashMap<>();
        for (DonHang d : invoiceDAO.getDanhSachHoaDon(from, to, storeId, null)) { if (d.getStatus() < 0) continue; process(d.getChiTietLayHang(), 1, map, products, categoryNames); process(d.getChiTietTraHang(), -1, map, products, categoryNames); }
        List<ThongKeMatHangBanRow> result = new ArrayList<>(); int i = 1; for (Aggregate a : map.values()) { double costTotal = a.quantity * a.cost, profit = a.sale - costTotal, rate = a.sale == 0 ? 0 : profit / a.sale * 100; result.add(new ThongKeMatHangBanRow(i++, a.code, a.name, a.quantity, a.unit, a.price, a.sale, a.cost, a.discountRate, costTotal, rate, profit, a.categoryId, a.categoryName)); } return result;
    }

    private void process(List<DonHangChiTiet> details, double sign, Map<String, Aggregate> map, Map<String, MatHang> products, Map<String, String> categoriesById) {
        if (details == null) return; for (DonHangChiTiet d : details) { String code = text(d.getMaHang()).trim(), name = text(d.getTenHang()).trim(); MatHang product = d.getMatHangId() == null ? null : products.get(d.getMatHangId()); if (product == null && !code.isEmpty()) product = products.get(code.toLowerCase()); String key = d.getMatHangId() != null ? d.getMatHangId() : (!code.isEmpty() ? code : name); if (key.isEmpty()) continue;
            Aggregate a = map.computeIfAbsent(key, k -> new Aggregate()); if (a.name == null) { a.code = !code.isEmpty() ? code : product == null ? "" : text(product.getCode()); a.name = !name.isEmpty() ? name : product == null ? "" : text(product.getName()); a.unit = d.getTenDonViTinh() != null ? d.getTenDonViTinh() : product == null ? "" : text(product.getTenDonViTinh()); a.price = d.getDonGia(); a.cost = product == null ? 0 : (product.getGiaVon() > 0 ? product.getGiaVon() : product.getGiaNhap()); a.discountRate = d.getTiLeGiamGia(); a.categoryId = product == null ? null : product.getNhomMatHangId(); a.categoryName = product == null ? "" : (product.getTenNhom() != null ? product.getTenNhom() : categoriesById.get(a.categoryId)); }
            double qty = (d.getSoLuong() > 0 ? d.getSoLuong() : 1) * sign; a.quantity += qty; a.sale += (d.getThanhTien() > 0 ? d.getThanhTien() : Math.abs(qty) * d.getDonGia()) * sign; if (d.getDonGia() > 0) a.price = d.getDonGia(); }
    }

    private void filterRows() { visibleRows = allRows.stream().filter(r -> selectedCategoryId == null || selectedCategoryId.equals(r.getNhomHangId())).toList(); renderRows(); updateTotals(); }
    private void renderRows() { TouchMatHangBanColumnConfig config = TouchMatHangBanConfigDAO.load(); resultRows.removeAll(); for (ThongKeMatHangBanRow r : visibleRows) { boolean active = r == selectedRow; int fields = Math.max(1, config.getUsed().size()); int rows = (fields + 1) / 2; JPanel card = new JPanel(new GridLayout(rows, 2, 8, 2)); card.setBackground(active ? ORANGE : TEAL); card.setBorder(new EmptyBorder(9, 14, 9, 14)); card.setPreferredSize(new Dimension(0, Math.max(118, rows * 43 + 18)));
            for (int i = 0; i < fields; i++) { TouchMatHangBanColumnConfig.Column column = config.getUsed().get(i); card.add(cardLabel(config.value(r, column, NUMBER), i % 2 == 0 ? SwingConstants.LEFT : SwingConstants.RIGHT)); } if (fields % 2 != 0) card.add(new JLabel()); java.awt.event.MouseAdapter click = new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectedRow = r; renderRows(); }}; installClick(card, click); resultRows.add(card); }
        if (visibleRows.size() % 2 != 0) { JPanel filler = new JPanel(); filler.setOpaque(false); resultRows.add(filler); } resultRows.revalidate(); resultRows.repaint(); }
    private void updateTotals() { double sale = 0, cost = 0, discount = 0; for (ThongKeMatHangBanRow r : visibleRows) { sale += r.getThanhTienBan(); cost += r.getThanhTienNhap(); discount += Math.max(0, Math.abs(r.getSoLuong() * r.getDonGia()) - Math.abs(r.getThanhTienBan())); } totalSale.setText(NUMBER.format(sale)); totalCost.setText(NUMBER.format(cost)); grossProfit.setText(NUMBER.format(sale - cost)); totalDiscount.setText(NUMBER.format(discount)); netProfit.setText(NUMBER.format(sale - cost - discount)); }

    private void chooseStore() { CuaHang result = TouchChonCuaHangDialog.showDialog(SwingUtilities.getWindowAncestor(this), storeDAO, selectedStore); if (TouchChonCuaHangDialog.wasAccepted()) { selectedStore = result; storeField.setText(result == null ? "" : result.getName()); reload(); } }
    private void pickDate(JTextField target) { String v = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), target.getText()); if (v != null) { target.setText(v); reload(); } }
    private void openPrint() { Date from = parse(fromField.getText()), to = parse(toField.getText()); if (from == null || to == null || from.after(to)) { warn("Vui lòng chọn khoảng ngày hợp lệ trước khi in."); return; } new TouchDoanhThuPrintDialog(SwingUtilities.getWindowAncestor(this), () -> reportService.generateTongHopMatHangBanTheoNgay(from, to, null, null)).setVisible(true); }
    private void setBusy(boolean busy) { refresh.setEnabled(!busy); print.setEnabled(!busy); setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR)); }
    private static Date parse(String value) { try { return Date.from(LocalDate.parse(value.trim(), DATE_FORMAT).atStartOfDay(ZoneId.systemDefault()).toInstant()); } catch (Exception e) { return null; } }
    private static JTextField field() { JTextField f = new JTextField(); f.setFont(new Font("Segoe UI", Font.PLAIN, 24)); return f; }
    private static JLabel whiteLabel(String s) { JLabel l = new JLabel(s); l.setForeground(Color.WHITE); l.setFont(TouchThongKeDoanhThuPanel.font(17)); return l; }
    private static JLabel valueLabel() { JLabel l = new JLabel("0", SwingConstants.CENTER); l.setOpaque(true); l.setBackground(PALE); l.setForeground(new Color(150, 0, 0)); l.setFont(TouchThongKeDoanhThuPanel.font(17)); return l; }
    private static JLabel cardLabel(String s, int align) { JLabel l = new JLabel(s, align); l.setForeground(Color.WHITE); l.setFont(TouchThongKeDoanhThuPanel.font(17)); return l; }
    private static String text(Object o) { return o == null ? "" : o.toString(); }
    private static void installClick(Component c, java.awt.event.MouseAdapter l) { c.addMouseListener(l); c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); if (c instanceof Container p) for (Component child : p.getComponents()) installClick(child, l); }
    private static String rootMessage(Throwable t) { Throwable c = t; while (c.getCause() != null) c = c.getCause(); return c.getMessage(); }
    private void warn(String s) { JOptionPane.showMessageDialog(this, s, "THỐNG KÊ MẶT HÀNG BÁN", JOptionPane.WARNING_MESSAGE); }
    private static final class Aggregate { String code, name, unit, categoryId, categoryName; double quantity, price, sale, cost, discountRate; }
}
