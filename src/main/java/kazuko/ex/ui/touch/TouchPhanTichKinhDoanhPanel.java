package kazuko.ex.ui.touch;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.system.SUser;
import kazuko.ex.report.service.BaoCaoPhanTichKinhDoanhReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Shared touch shell for the business-result and daily-activity Jasper reports. */
final class TouchPhanTichKinhDoanhPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(79, 139, 169), ORANGE = new Color(195, 60, 0);
    private static final Color GREEN = new Color(0, 85, 20), TEAL = new Color(0, 79, 76), RED = new Color(130, 15, 15);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final BaoCaoPhanTichKinhDoanhReportService reportService = new BaoCaoPhanTichKinhDoanhReportService();
    private final CuaHangDAO storeDAO = new CuaHangDAO();
    private final UserDAO userDAO = new UserDAO();
    private final boolean dailyActivity;
    private final Runnable exit;
    private final JTextField fromField = field(), toField = field(), cashierField = field(), storeField = field();
    private final JPanel viewerBox = new JPanel(new BorderLayout());
    private final JButton loadButton = button("TẢI DỮ LIỆU", GREEN);
    private CuaHang selectedStore;
    private Cashier selectedCashier;
    private JasperPrint currentPrint;

    TouchPhanTichKinhDoanhPanel(boolean dailyActivity, Runnable exit) {
        this.dailyActivity = dailyActivity; this.exit = exit;
        setLayout(new BorderLayout(7, 0)); setBackground(BG); setBorder(new EmptyBorder(7, 7, 7, 7));
        add(filters(), BorderLayout.WEST); add(reportArea(), BorderLayout.CENTER);
        LocalDate now = LocalDate.now(); fromField.setText(now.withDayOfMonth(1).format(DATE_FORMAT)); toField.setText(now.format(DATE_FORMAT));
        cashierField.setEditable(false); storeField.setEditable(false); SwingUtilities.invokeLater(this::loadReport);
    }

    private JPanel filters() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false); panel.setPreferredSize(new Dimension(355, 0));
        JLabel heading = new JLabel("LỌC THAM SỐ", SwingConstants.CENTER); heading.setOpaque(true); heading.setBackground(ORANGE); heading.setForeground(Color.WHITE); heading.setFont(font(24)); heading.setPreferredSize(new Dimension(0, 63)); panel.add(heading, BorderLayout.NORTH);
        JPanel fields = new JPanel(); fields.setOpaque(false); fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS)); fields.setBorder(new EmptyBorder(8, 8, 0, 8));
        fields.add(label("TỪ NGÀY")); fields.add(Box.createVerticalStrut(6)); fields.add(fieldRow(fromField, "▼", () -> chooseDate(fromField))); fields.add(Box.createVerticalStrut(14));
        fields.add(label("ĐẾN NGÀY")); fields.add(Box.createVerticalStrut(6)); fields.add(fieldRow(toField, "▼", () -> chooseDate(toField))); fields.add(Box.createVerticalStrut(14));
        if (dailyActivity) { fields.add(label("THU NGÂN")); fields.add(Box.createVerticalStrut(6)); fields.add(fieldRow(cashierField, "⌕", this::chooseCashier)); fields.add(Box.createVerticalStrut(14)); }
        fields.add(label("CỬA HÀNG")); fields.add(Box.createVerticalStrut(6)); fields.add(fieldRow(storeField, "⌕", this::chooseStore)); fields.add(Box.createVerticalStrut(25));
        loadButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64)); loadButton.setAlignmentX(Component.LEFT_ALIGNMENT); loadButton.addActionListener(e -> loadReport()); fields.add(loadButton); fields.add(Box.createVerticalGlue()); panel.add(fields, BorderLayout.CENTER); return panel;
    }

    private JPanel reportArea() {
        JPanel p = new JPanel(new BorderLayout(0, 8)); p.setOpaque(false);
        String title = dailyActivity ? "CHI TIẾT HOẠT ĐỘNG TRONG NGÀY" : "BÁO CÁO KẾT QUẢ KINH DOANH";
        JPanel headingBar = new JPanel(new BorderLayout(8, 0)); headingBar.setBackground(HEADER); headingBar.setPreferredSize(new Dimension(0, 63));
        JLabel heading = new JLabel(title, SwingConstants.CENTER); heading.setForeground(Color.WHITE); heading.setFont(font(25)); headingBar.add(heading, BorderLayout.CENTER);
        JButton fixedExit = button("THOÁT", RED); fixedExit.setPreferredSize(new Dimension(145, 53)); fixedExit.addActionListener(e -> exit.run());
        JPanel exitBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5)); exitBox.setOpaque(false); exitBox.add(fixedExit); headingBar.add(exitBox, BorderLayout.EAST);
        p.add(headingBar, BorderLayout.NORTH);
        viewerBox.setBackground(new Color(225, 225, 225)); JLabel wait = new JLabel("ĐANG TẢI BÁO CÁO...", SwingConstants.CENTER); wait.setFont(font(22)); wait.setForeground(BG); viewerBox.add(wait); p.add(viewerBox, BorderLayout.CENTER); return p;
    }

    private void loadReport() {
        Date from = parse(fromField.getText()), to = parse(toField.getText()); if (from == null || to == null || from.after(to)) { warn("Khoảng ngày không hợp lệ."); return; }
        String storeId = selectedStore == null ? "" : selectedStore.getId(), cashierId = selectedCashier == null ? "" : selectedCashier.id;
        setBusy(true); new SwingWorker<JasperPrint, Void>() {
            @Override protected JasperPrint doInBackground() throws Exception { return dailyActivity ? reportService.generateChiTietHoatDongTrongNgay(from, to, cashierId, storeId) : reportService.generateKetQuaKinhDoanh(from, to, storeId); }
            @Override protected void done() { try { currentPrint = get(); showViewer(currentPrint); } catch (Exception ex) { warn("Không thể tải báo cáo: " + rootMessage(ex)); } finally { setBusy(false); } }
        }.execute();
    }

    private void showViewer(JasperPrint print) {
        viewerBox.removeAll(); JRViewer viewer = new JRViewer(print); viewer.setBackground(HEADER); customizeToolbar(viewer); viewerBox.add(viewer); viewerBox.revalidate(); viewerBox.repaint();
    }

    private void customizeToolbar(Container root) {
        for (Component child : root.getComponents()) {
            if (child instanceof JToolBar toolbar) {
                toolbar.setBackground(HEADER); toolbar.setBorder(new EmptyBorder(5, 5, 5, 5));
                for (Component item : toolbar.getComponents()) if (item instanceof JButton button) { button.setPreferredSize(new Dimension(62, 50)); button.setBackground(new Color(82, 75, 0)); button.setForeground(Color.WHITE); button.setFont(font(15)); }
                JButton close = button("THOÁT", RED); close.setPreferredSize(new Dimension(140, 50)); close.addActionListener(e -> exit.run()); toolbar.add(Box.createHorizontalGlue()); toolbar.add(close);
            } else if (child instanceof Container container) customizeToolbar(container);
        }
    }

    private void chooseDate(JTextField target) { String value = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), target.getText()); if (value != null) target.setText(value); }
    private void chooseStore() { CuaHang value = TouchChonCuaHangDialog.showDialog(SwingUtilities.getWindowAncestor(this), storeDAO, selectedStore); if (TouchChonCuaHangDialog.wasAccepted()) { selectedStore = value; storeField.setText(value == null ? "" : value.getName()); } }
    private void chooseCashier() {
        List<Cashier> choices = new ArrayList<>(); choices.add(new Cashier("", "TẤT CẢ"));
        try { for (SUser user : userDAO.getAll()) choices.add(new Cashier(user.getUsername(), user.getName() == null || user.getName().isBlank() ? user.getUsername() : user.getName())); } catch (Exception ignored) {}
        Object value = JOptionPane.showInputDialog(this, "Chọn thu ngân", "THU NGÂN", JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), selectedCashier == null ? choices.get(0) : selectedCashier);
        if (value instanceof Cashier cashier) { selectedCashier = cashier.id.isBlank() ? null : cashier; cashierField.setText(selectedCashier == null ? "" : selectedCashier.name); }
    }

    private JPanel fieldRow(JTextField field, String icon, Runnable action) { JPanel p = new JPanel(new BorderLayout(8, 0)); p.setOpaque(false); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52)); field.setPreferredSize(new Dimension(0, 52)); p.add(field); JButton b = button(icon, TEAL); b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 30)); b.setPreferredSize(new Dimension(77, 52)); b.addActionListener(e -> action.run()); p.add(b, BorderLayout.EAST); return p; }
    private void setBusy(boolean busy) { loadButton.setEnabled(!busy); setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR)); }
    private static JTextField field() { JTextField f = new JTextField(); f.setFont(new Font("Segoe UI", Font.PLAIN, 25)); return f; }
    private static JLabel label(String value) { JLabel l = new JLabel(value); l.setForeground(Color.WHITE); l.setFont(font(18)); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }
    private static JButton button(String value, Color bg) { JButton b = TouchThongKeDoanhThuPanel.button(value, bg); b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
    private static Date parse(String value) { try { LocalDate d = LocalDate.parse(value.trim(), DATE_FORMAT); return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()); } catch (Exception e) { return null; } }
    private static String rootMessage(Throwable error) { Throwable cause = error; while (cause.getCause() != null) cause = cause.getCause(); return cause.getMessage(); }
    private void warn(String value) { JOptionPane.showMessageDialog(this, value, dailyActivity ? "CHI TIẾT HOẠT ĐỘNG" : "KẾT QUẢ KINH DOANH", JOptionPane.WARNING_MESSAGE); }
    private record Cashier(String id, String name) { @Override public String toString() { return name; } }
}
