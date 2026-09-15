package kazuko.ex.ui.touch;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.report.service.BaoCaoBanHangReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Touch-first revenue statistics screen. */
final class TouchThongKeDoanhThuPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final Color GREEN = new Color(0, 85, 20);
    private static final Color RED = new Color(165, 15, 15);
    private static final Color PALE = new Color(255, 255, 190);
    private static final DateTimeFormatter DATE_INPUT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final SimpleDateFormat DATE_VIEW = new SimpleDateFormat("dd/MM/yyyy");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.##");

    private final HoaDonDAO invoiceDAO = new HoaDonDAO();
    private final CuaHangDAO storeDAO = new CuaHangDAO();
    private final BaoCaoBanHangReportService reportService = new BaoCaoBanHangReportService();
    private final Runnable exit;
    private final JTextField fromField = field();
    private final JTextField toField = field();
    private final JTextField storeField = field();
    private final JPanel invoiceRows = new JPanel();
    private final JPanel detailRows = new JPanel();
    private final JLabel[] totals = new JLabel[9];
    private final JButton refreshButton = button("REFRESH", GREEN);
    private final JButton reportButton = button("IN BÁO CÁO", GREEN);
    private List<DonHang> invoices = new ArrayList<>();
    private DonHang selectedInvoice;
    private CuaHang selectedStore;

    TouchThongKeDoanhThuPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout(7, 7));
        setBackground(BG);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        add(header("THỐNG KÊ DOANH THU", 62), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        LocalDate today = LocalDate.now();
        fromField.setText(today.format(DATE_INPUT));
        toField.setText(today.format(DATE_INPUT));
        storeField.setEditable(false);
        SwingUtilities.invokeLater(this::reload);
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 7)); body.setOpaque(false);
        body.add(createFilters(), BorderLayout.NORTH);

        invoiceRows.setLayout(new BoxLayout(invoiceRows, BoxLayout.Y_AXIS));
        invoiceRows.setBackground(PALE);
        detailRows.setLayout(new BoxLayout(detailRows, BoxLayout.Y_AXIS));
        detailRows.setBackground(new Color(232, 232, 232));
        JScrollPane left = scroll(invoiceRows), right = scroll(detailRows);
        left.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        right.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        installGear(left, "Thiết lập danh sách hóa đơn");
        installGear(right, "Thiết lập chi tiết hóa đơn");

        JPanel lists = new JPanel(new GridBagLayout()); lists.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.gridy = 0; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.gridx = 0; c.weightx = 2; lists.add(left, c);
        c.gridx = 1; c.weightx = 1; c.insets = new Insets(0, 7, 0, 0); lists.add(right, c);
        body.add(lists, BorderLayout.CENTER);
        body.add(createFooter(), BorderLayout.SOUTH);
        return body;
    }

    private JPanel createFilters() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.gridy = 0; c.insets = new Insets(0, 5, 0, 5); c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 0; p.add(label("TỪ:"), c);
        c.gridx++; fromField.setPreferredSize(new Dimension(195, 48)); p.add(fromField, c);
        c.gridx++; p.add(label("ĐẾN:"), c);
        c.gridx++; toField.setPreferredSize(new Dimension(195, 48)); p.add(toField, c);
        c.gridx++; JButton dates = button("▼", TEAL); dates.setPreferredSize(new Dimension(70, 48)); dates.setFont(font(25));
        dates.addActionListener(e -> chooseQuickDate(dates)); p.add(dates, c);
        c.gridx++; p.add(label("CỬA HÀNG"), c);
        c.gridx++; c.weightx = 1; storeField.setPreferredSize(new Dimension(300, 48)); p.add(storeField, c);
        c.gridx++; c.weightx = 0; JButton stores = button("⌕", TEAL); stores.setFont(new Font("Segoe UI Symbol", Font.BOLD, 34)); stores.setPreferredSize(new Dimension(75, 48));
        stores.addActionListener(e -> chooseStore()); p.add(stores, c);
        fromField.addMouseListener(click(this::pickFrom));
        toField.addMouseListener(click(this::pickTo));
        return p;
    }

    private JPanel createFooter() {
        JPanel p = new JPanel(new BorderLayout(10, 0)); p.setOpaque(false);
        JPanel summary = new JPanel(new GridLayout(2, 5, 8, 5)); summary.setOpaque(false);
        String[] names = {"TIỀN HÀNG", "GIẢM GIÁ", "THUẾ", "VẬN CHUYỂN", "TỔNG CỘNG", "NỢ", "TIỀN MẶT", "TIỀN THẺ", "C. KHOẢN"};
        for (int i = 0; i < 10; i++) {
            JPanel box = new JPanel(new BorderLayout()); box.setOpaque(false);
            if (i < names.length) {
                JLabel title = new JLabel(names[i], SwingConstants.CENTER); title.setForeground(Color.WHITE); title.setFont(font(16));
                totals[i] = new JLabel("0", SwingConstants.CENTER); totals[i].setOpaque(true); totals[i].setBackground(PALE); totals[i].setForeground(new Color(150, 0, 0)); totals[i].setFont(font(17));
                box.add(title, BorderLayout.NORTH); box.add(totals[i], BorderLayout.CENTER);
            }
            summary.add(box);
        }
        summary.setPreferredSize(new Dimension(840, 92)); p.add(summary, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 27)); actions.setOpaque(false);
        refreshButton.setPreferredSize(new Dimension(150, 64)); refreshButton.addActionListener(e -> reload());
        reportButton.setPreferredSize(new Dimension(180, 64)); reportButton.addActionListener(e -> openPrintDialog());
        JButton close = button("THOÁT", RED); close.setPreferredSize(new Dimension(180, 64)); close.addActionListener(e -> exit.run());
        actions.add(refreshButton); actions.add(reportButton); actions.add(close); p.add(actions, BorderLayout.CENTER);
        return p;
    }

    private void reload() {
        Date from = parse(fromField.getText()), to = parse(toField.getText());
        if (from == null || to == null || from.after(to)) { warn("Khoảng ngày không hợp lệ."); return; }
        setBusy(true);
        String storeId = selectedStore == null ? "" : selectedStore.getId();
        new SwingWorker<List<DonHang>, Void>() {
            @Override protected List<DonHang> doInBackground() { return invoiceDAO.getDanhSachHoaDon(from, to, storeId, null); }
            @Override protected void done() {
                try { invoices = get(); } catch (Exception ex) { invoices = new ArrayList<>(); warn("Không thể tải dữ liệu doanh thu: " + rootMessage(ex)); }
                selectedInvoice = invoices.isEmpty() ? null : invoices.get(0);
                renderInvoices(); renderDetails(); updateTotals(); setBusy(false);
            }
        }.execute();
    }

    private void renderInvoices() {
        invoiceRows.removeAll();
        for (DonHang invoice : invoices) {
            boolean selected = invoice == selectedInvoice;
            JPanel card = new JPanel(new GridLayout(2, 2, 8, 2));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 98)); card.setPreferredSize(new Dimension(0, 98));
            card.setBackground(selected ? ORANGE : TEAL); card.setBorder(new EmptyBorder(9, 14, 9, 14));
            card.add(cardLabel(text(invoice.getName()), SwingConstants.LEFT));
            card.add(cardLabel(MONEY.format(invoice.getTongCong()), SwingConstants.RIGHT));
            card.add(cardLabel(text(invoice.getTenKhach()), SwingConstants.LEFT));
            card.add(cardLabel(invoice.getNgay() == null ? "" : DATE_VIEW.format(invoice.getNgay()), SwingConstants.RIGHT));
            java.awt.event.MouseAdapter listener = new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectedInvoice = invoice; renderInvoices(); renderDetails(); }};
            installClick(card, listener); invoiceRows.add(card); invoiceRows.add(Box.createVerticalStrut(6));
        }
        invoiceRows.revalidate(); invoiceRows.repaint();
    }

    private void renderDetails() {
        detailRows.removeAll();
        if (selectedInvoice != null) {
            addDetails(selectedInvoice.getChiTietLayHang(), false);
            addDetails(selectedInvoice.getChiTietTraHang(), true);
        }
        detailRows.revalidate(); detailRows.repaint();
    }

    private void addDetails(List<DonHangChiTiet> rows, boolean returned) {
        if (rows == null) return;
        for (DonHangChiTiet row : rows) {
            double sign = returned ? -1 : 1;
            JPanel card = new JPanel(new GridLayout(2, 2, 8, 2)); card.setBackground(TEAL);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 98)); card.setPreferredSize(new Dimension(0, 98)); card.setBorder(new EmptyBorder(9, 14, 9, 14));
            card.add(cardLabel((returned ? "[TRẢ] " : "") + text(row.getTenHang()), SwingConstants.LEFT));
            card.add(cardLabel(MONEY.format(sign * row.getSoLuong()), SwingConstants.RIGHT));
            card.add(cardLabel(MONEY.format(row.getDonGia()), SwingConstants.LEFT));
            card.add(cardLabel(MONEY.format(sign * row.getThanhTien()), SwingConstants.RIGHT));
            detailRows.add(card); detailRows.add(Box.createVerticalStrut(6));
        }
    }

    private void updateTotals() {
        double[] sum = new double[9];
        for (DonHang d : invoices) {
            sum[0] += d.getTienHang(); sum[1] += d.getTienGiamGia(); sum[2] += d.getTienThue(); sum[3] += d.getPhiVanChuyen();
            sum[4] += d.getTongCong(); sum[5] += Math.max(0, d.getConLai()); sum[6] += d.getTienMat(); sum[7] += d.getTienTheATM(); sum[8] += d.getTienChuyenKhoan();
        }
        for (int i = 0; i < totals.length; i++) totals[i].setText(MONEY.format(sum[i]));
    }

    private void chooseStore() {
        CuaHang result = TouchChonCuaHangDialog.showDialog(SwingUtilities.getWindowAncestor(this), storeDAO, selectedStore);
        if (TouchChonCuaHangDialog.wasAccepted()) {
            selectedStore = result; storeField.setText(result == null ? "" : result.getName()); reload();
        }
    }

    private void pickFrom() { String v = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), fromField.getText()); if (v != null) { fromField.setText(v); reload(); } }
    private void pickTo() { String v = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), toField.getText()); if (v != null) { toField.setText(v); reload(); } }

    private void chooseQuickDate(Component owner) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem today = new JMenuItem("Hôm nay"); today.addActionListener(e -> setRange(LocalDate.now(), LocalDate.now())); menu.add(today);
        JMenuItem week = new JMenuItem("7 ngày gần đây"); week.addActionListener(e -> setRange(LocalDate.now().minusDays(6), LocalDate.now())); menu.add(week);
        JMenuItem month = new JMenuItem("Tháng này"); month.addActionListener(e -> setRange(LocalDate.now().withDayOfMonth(1), LocalDate.now())); menu.add(month);
        menu.show(owner, 0, owner.getHeight());
    }

    private void setRange(LocalDate from, LocalDate to) { fromField.setText(from.format(DATE_INPUT)); toField.setText(to.format(DATE_INPUT)); reload(); }

    private void openPrintDialog() {
        Date from = parse(fromField.getText()), to = parse(toField.getText());
        if (from == null || to == null || from.after(to)) { warn("Vui lòng chọn khoảng ngày hợp lệ trước khi in."); return; }
        String storeId = selectedStore == null ? "" : selectedStore.getId();
        new TouchDoanhThuPrintDialog(SwingUtilities.getWindowAncestor(this), reportService, from, to, storeId).setVisible(true);
    }

    private void setBusy(boolean busy) { refreshButton.setEnabled(!busy); reportButton.setEnabled(!busy); setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR)); }
    private static Date parse(String value) { try { LocalDate d = LocalDate.parse(value.trim(), DATE_INPUT); return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()); } catch (Exception e) { return null; } }
    private static JScrollPane scroll(Component view) { JScrollPane s = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); s.getVerticalScrollBar().setUnitIncrement(40); return s; }
    private static void installGear(JScrollPane scroll, String text) { JButton b = new JButton(new TouchMatHangPanel.GearIcon(27, Color.WHITE)); b.setBackground(BG); b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); b.setToolTipText(text); b.addActionListener(e -> JOptionPane.showMessageDialog(scroll, text + " đang dùng bố cục cảm ứng mặc định.", "THIẾT LẬP", JOptionPane.INFORMATION_MESSAGE)); scroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, b); }
    private static JPanel header(String text, int height) { JPanel p = new JPanel(new BorderLayout()); p.setBackground(HEADER); p.setPreferredSize(new Dimension(0, height)); p.setBorder(new LineBorder(new Color(120, 160, 180), 2, true)); JLabel l = new JLabel(text, SwingConstants.CENTER); l.setForeground(Color.WHITE); l.setFont(font(26)); p.add(l); return p; }
    private static JTextField field() { JTextField f = new JTextField(); f.setFont(new Font("Segoe UI", Font.PLAIN, 25)); return f; }
    private static JLabel label(String text) { JLabel l = new JLabel(text); l.setForeground(Color.WHITE); l.setFont(font(18)); return l; }
    private static JLabel cardLabel(String text, int align) { JLabel l = new JLabel(text, align); l.setForeground(Color.WHITE); l.setFont(font(18)); return l; }
    static JButton button(String text, Color color) { JButton b = new JButton(text); b.setBackground(color); b.setForeground(Color.WHITE); b.setFont(font(19)); b.setFocusPainted(false); b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); return b; }
    static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
    private static java.awt.event.MouseAdapter click(Runnable action) { return new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { action.run(); }}; }
    private static void installClick(Component c, java.awt.event.MouseAdapter l) { c.addMouseListener(l); c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); if (c instanceof Container p) for (Component child : p.getComponents()) installClick(child, l); }
    private static String text(Object value) { return value == null ? "" : value.toString(); }
    private static String rootMessage(Throwable t) { Throwable c = t; while (c.getCause() != null) c = c.getCause(); return c.getMessage(); }
    private void warn(String text) { JOptionPane.showMessageDialog(this, text, "THỐNG KÊ DOANH THU", JOptionPane.WARNING_MESSAGE); }
}
