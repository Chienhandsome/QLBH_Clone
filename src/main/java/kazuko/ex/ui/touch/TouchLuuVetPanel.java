package kazuko.ex.ui.touch;

import kazuko.ex.dao.ActivityLogDAO;
import kazuko.ex.model.system.ActivityLog;
import kazuko.ex.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Touch version of the activity audit screen, reusing ActivityLogDAO. */
final class TouchLuuVetPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 85, 20);
    private static final Color RED = new Color(165, 15, 15);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ActivityLogDAO dao = new ActivityLogDAO();
    private final Runnable exit;
    private final JTextField dateField = textField();
    private final JComboBox<String> userField = new JComboBox<>();
    private final JTextField referenceField = textField();
    private final JTextField contentField = textField();
    private final TouchScrollableRowsPanel logRows = new TouchScrollableRowsPanel();
    private List<ActivityLog> currentLogs = new ArrayList<>();
    private ActivityLog selectedLog;

    TouchLuuVetPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout(7, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(7, 7, 7, 7));
        add(createFilters(), BorderLayout.WEST);
        add(createResults(), BorderLayout.CENTER);
        loadUsers();
        dateField.setText(LocalDate.now().format(DATE_FORMAT));
        refresh();
    }

    private JPanel createFilters() {
        JPanel filters = new JPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));
        filters.setOpaque(false);
        filters.setPreferredSize(new Dimension(340, 0));
        addLabel(filters, "NGÀY:");
        JPanel dateRow = row(dateField, "▼", e -> pickDate());
        filters.add(dateRow); filters.add(Box.createVerticalStrut(24));
        addLabel(filters, "TÀI KHOẢN:");
        userField.setEditable(true); userField.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        filters.add(row(userField, "⌕", e -> refresh())); filters.add(Box.createVerticalStrut(24));
        addLabel(filters, "SỐ PHIẾU");
        filters.add(row(referenceField, "⌨", e -> editText(referenceField, "NHẬP SỐ PHIẾU")));
        filters.add(Box.createVerticalStrut(24));
        addLabel(filters, "NỘI DUNG");
        filters.add(row(contentField, "⌨", e -> editText(contentField, "NHẬP NỘI DUNG")));
        filters.add(Box.createVerticalStrut(25));
        JButton refresh = button("REFRESH", GREEN);
        refresh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        refresh.addActionListener(e -> refresh());
        filters.add(refresh); filters.add(Box.createVerticalGlue());
        return filters;
    }

    private JPanel createResults() {
        JPanel result = new JPanel(new BorderLayout(0, 7)); result.setOpaque(false);
        logRows.setLayout(new GridLayout(0, 1, 0, 7));
        logRows.setBackground(new Color(235, 235, 235));
        logRows.setBorder(new EmptyBorder(7, 7, 7, 7));
        JScrollPane scroll = new JScrollPane(logRows, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(45);
        result.add(scroll, BorderLayout.CENTER);

        JButton gear = new JButton(new TouchMatHangPanel.GearIcon(28, Color.WHITE));
        gear.setBackground(BG); gear.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); gear.setFocusPainted(false);
        gear.setPreferredSize(new Dimension(52, 52)); gear.addActionListener(e -> openSettings());
        JPanel gearBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); gearBox.setOpaque(false); gearBox.add(gear);
        scroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, gearBox);

        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false);
        JButton delete = button("XÓA LƯU VẾT", Color.RED); delete.setPreferredSize(new Dimension(200, 65));
        boolean admin = AuthService.getCurrentUser() != null && AuthService.getCurrentUser().isAdminUser();
        delete.setEnabled(admin); delete.addActionListener(e -> deleteSelected());
        JButton close = button("THOÁT", RED); close.setPreferredSize(new Dimension(200, 65)); close.addActionListener(e -> exit.run());
        footer.add(delete, BorderLayout.WEST); footer.add(close, BorderLayout.EAST);
        result.add(footer, BorderLayout.SOUTH);
        return result;
    }

    private void loadUsers() {
        Object selected = userField.getSelectedItem();
        userField.removeAllItems(); userField.addItem("");
        for (String name : dao.getUserNames()) userField.addItem(name);
        if (selected != null) userField.setSelectedItem(selected);
    }

    private void refresh() {
        Date from = null, to = null;
        try {
            LocalDate day = LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
            from = Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
            to = Date.from(day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {}
        Object userValue = userField.getEditor().getItem();
        String user = userValue == null || userValue.toString().isBlank() ? null : userValue.toString().trim();
        currentLogs = dao.search(from, to, user, referenceField.getText(), contentField.getText());
        selectedLog = null;
        renderLogs();
    }

    private void renderLogs() {
        TouchLuuVetColumnConfig config = TouchLuuVetConfigDAO.load();
        int columns = config.getGridColumns();
        logRows.configureGrid(columns, config.getGridRows(), 7, 7);
        logRows.removeAll();
        for (ActivityLog log : currentLogs) logRows.add(logCard(log, config));
        logRows.completeGrid();
        logRows.revalidate(); logRows.repaint();
    }

    private JPanel logCard(ActivityLog log, TouchLuuVetColumnConfig config) {
        boolean selected = selectedLog != null && selectedLog.getId() != null && selectedLog.getId().equals(log.getId());
        JPanel card = new JPanel(new GridLayout(2, 2, 8, 2));
        card.setBackground(selected ? ORANGE : config.getCardBgColor());
        card.setBorder(new EmptyBorder(7, 14, 7, 14));
        int height = Math.max(1, 850 / config.getGridRows());
        card.setPreferredSize(new Dimension(0, height));
        List<TouchLuuVetColumnConfig.ColumnItem> used = config.getUsedColumns();
        for (int i = 0; i < 4; i++) {
            JLabel label = new JLabel();
            if (i < used.size()) {
                TouchLuuVetColumnConfig.ColumnItem item = used.get(i);
                label.setText(TouchLuuVetColumnConfig.value(log, item, config.isShowTitle()));
                label.setForeground(item.getColor()); label.setFont(item.getFont());
                label.setHorizontalAlignment(item.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.LEFT);
            }
            label.setOpaque(false); card.add(label);
        }
        java.awt.event.MouseAdapter listener = new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectedLog = log; renderLogs(); }
        };
        installClick(card, listener);
        return card;
    }

    private void deleteSelected() {
        if (selectedLog == null) { warn("Vui lòng chọn một dòng lưu vết cần xóa."); return; }
        if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "BẠN CÓ MUỐN XÓA DÒNG<br>LƯU VẾT ĐÃ CHỌN KHÔNG?")) return;
        int deleted = dao.deleteByIds(List.of(selectedLog.getId()));
        if (deleted == 0) warn("Không thể xóa dòng lưu vết.");
        refresh();
    }

    private void pickDate() {
        String value = TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this), dateField.getText());
        if (value != null) { dateField.setText(value); refresh(); }
    }

    private void editText(JTextField field, String title) {
        String value = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), title, field.getText());
        if (value != null) field.setText(value);
    }

    private void openSettings() {
        new TouchLuuVetSettingsDialog(SwingUtilities.getWindowAncestor(this), this::renderLogs).setVisible(true);
    }

    private static JPanel row(JComponent field, String icon, java.awt.event.ActionListener listener) {
        JPanel row = new JPanel(new BorderLayout(8, 0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        field.setPreferredSize(new Dimension(0, 52)); row.add(field, BorderLayout.CENTER);
        JButton b = button(icon, new Color(0, 79, 76)); b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28)); b.setPreferredSize(new Dimension(75, 52)); b.addActionListener(listener); row.add(b, BorderLayout.EAST);
        return row;
    }
    private static void addLabel(JPanel p, String text) { JLabel l = new JLabel(text); l.setForeground(Color.WHITE); l.setFont(font(19)); l.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(6)); }
    private static JTextField textField() { JTextField f = new JTextField(); f.setFont(new Font("Segoe UI", Font.PLAIN, 24)); return f; }
    private static JButton button(String text, Color color) { JButton b = new JButton(text); b.setBackground(color); b.setForeground(Color.WHITE); b.setFont(font(19)); b.setFocusPainted(false); b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
    private void warn(String text) { JOptionPane.showMessageDialog(this, text, "THÔNG BÁO", JOptionPane.WARNING_MESSAGE); }
    private static void installClick(Component c, java.awt.event.MouseAdapter l) { c.addMouseListener(l); c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); if (c instanceof Container p) for (Component child : p.getComponents()) installClick(child, l); }
}
