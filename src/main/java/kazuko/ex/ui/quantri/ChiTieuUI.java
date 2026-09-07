package kazuko.ex.ui.quantri;

import kazuko.ex.util.IconHelper;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;

final class ChiTieuUI {
    static final Color BLUE = new Color(187, 207, 231);
    static final Font FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private ChiTieuUI() {}

    static JButton button(String text, String icon, Runnable action) {
        JButton b = new JButton(text, IconHelper.getIcon(icon, 16, 16));
        b.setFont(FONT);
        b.addActionListener(e -> action.run());
        return b;
    }

    static void bind(JComponent c, String key, Runnable action) {
        c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key), key);
        c.getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); }
        });
    }

    static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Không thể thực hiện", JOptionPane.ERROR_MESSAGE);
    }

    static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT); t.setRowHeight(25); t.setShowGrid(true);
        t.setGridColor(new Color(210, 220, 230));
        t.setSelectionBackground(new Color(210, 232, 204));
        t.setSelectionForeground(Color.BLACK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.getTableHeader().setFont(FONT);
        t.getTableHeader().setPreferredSize(new Dimension(100, 32));
        t.getTableHeader().setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final DecimalFormat numbers = new DecimalFormat("#,##0.##");
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                                     boolean focus, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                setHorizontalAlignment(value instanceof Number ? RIGHT : LEFT);
                setText(value == null ? "" : value instanceof BigDecimal ? numbers.format(value) : value.toString());
                if (!selected) setBackground(row % 2 == 0 ? Color.WHITE : new Color(235, 247, 251));
                return this;
            }
        });
        for (int i=0; i<t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setPreferredWidth(i==0?40:110);
        return t;
    }

    static BigDecimal amount(Object value) {
        String text = value == null ? "" : value.toString().trim();
        if (text.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(text); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + text + ". Nhập số, dùng dấu chấm cho phần thập phân.");
        }
    }

    static void print(Component parent, JTable table, String title) {
        try { table.print(JTable.PrintMode.FIT_WIDTH, new java.text.MessageFormat(title), new java.text.MessageFormat("Trang {0}")); }
        catch (Exception e) { error(parent, e); }
    }

    static String text(Object value) { return value == null ? "" : value.toString(); }
}
