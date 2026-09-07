package kazuko.ex.ui.quantri;

import kazuko.ex.util.IconHelper;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;

final class ChiTieuUI {
    static final Font FONT = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    static final Color COLOR_MAIN_BG = new Color(240, 243, 248);
    static final Color COLOR_HEADER_BG = Color.WHITE;
    static final Color COLOR_HEADER_BORDER = new Color(210, 220, 235);
    static final Color COLOR_TOOLBAR_BG = new Color(225, 235, 248);
    static final Color COLOR_TOOLBAR_BORDER = new Color(180, 200, 225);
    static final Color COLOR_RIBBON_BG = new Color(210, 225, 244);
    static final Color COLOR_RIBBON_BORDER = new Color(175, 198, 226);
    static final Color COLOR_FORM_BG = new Color(200, 218, 238);
    static final Color COLOR_FOOTER_BG = new Color(235, 242, 252);
    static final Color COLOR_FOOTER_BORDER = new Color(185, 205, 230);
    static final Color COLOR_TEXT_DARK = new Color(20, 45, 85);

    // Row selection colors matching screenshots
    static final Color COLOR_ROW_BROWN = new Color(184, 92, 36);      // #B85C24
    static final Color COLOR_ROW_GREEN = new Color(78, 155, 40);       // #4E9B28
    static final Color COLOR_ROW_LIGHT_GREEN = new Color(200, 230, 201);// #C8E6C9

    static final Color COLOR_TABLE_HEADER_BG = new Color(230, 238, 248);
    static final Color COLOR_TABLE_GRID = new Color(215, 225, 235);
    static final Color COLOR_TABLE_ROW_ALT = new Color(248, 251, 255);
    static final Color COLOR_TABLE_SELECTED = new Color(210, 230, 250);

    // Backward-compatibility
    static final Color BLUE = COLOR_FORM_BG;

    private ChiTieuUI() {}

    static JButton toolButton(String text, String iconName, Color textColor, boolean isBold) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(isBold ? FONT_BOLD : FONT);
        btn.setForeground(textColor != null ? textColor : Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    static JButton ribbonButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(FONT);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 8, 2, 8));
        return btn;
    }

    static JButton actionButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setPreferredSize(new Dimension(text.length() > 8 ? 105 : 85, 26));
        return btn;
    }

    static JButton miniToolButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(1, 1, 1, 1));
        return btn;
    }

    static JButton button(String text, String icon, Runnable action) {
        JButton b = toolButton(text, icon, Color.BLACK, false);
        if (action != null) b.addActionListener(e -> action.run());
        return b;
    }

    static void bind(JComponent c, String key, Runnable action) {
        c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key), key);
        c.getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); }
        });
    }

    static void error(Component parent, Exception e) {
        if (e != null) e.printStackTrace();
        if (GraphicsEnvironment.isHeadless() || parent == null || !parent.isShowing()) {
            return;
        }
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Không thể thực hiện", JOptionPane.ERROR_MESSAGE);
    }

    static final Icon POINTER_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            int cx = x + 2;
            int cy = y + 2;
            int[] xPoints = {cx, cx + 6, cx};
            int[] yPoints = {cy, cy + 4, cy + 8};
            g2.fillPolygon(xPoints, yPoints, 3);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 10; }
        @Override public int getIconHeight() { return 12; }
    };

    static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT);
        t.setRowHeight(22);
        t.setShowGrid(true);
        t.setGridColor(COLOR_TABLE_GRID);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader th = t.getTableHeader();
        th.setFont(FONT);
        th.setBackground(COLOR_TABLE_HEADER_BG);
        th.setForeground(new Color(30, 30, 30));
        th.setPreferredSize(new Dimension(100, 24));
        th.setReorderingAllowed(false);

        DecimalFormat numbers = new DecimalFormat("#,##0.##");

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                if (column == 0) {
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setText(String.valueOf(row + 1));
                    lbl.setIcon(selected ? POINTER_ICON : null);
                } else if (value instanceof Number || (value instanceof BigDecimal)) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setText(value instanceof BigDecimal ? numbers.format(value) : numbers.format(value));
                    lbl.setIcon(null);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    lbl.setText(value == null ? "" : value.toString());
                    lbl.setIcon(null);
                }

                if (selected) {
                    if (column <= 1) {
                        lbl.setBackground(COLOR_ROW_BROWN);
                        lbl.setForeground(Color.WHITE);
                    } else {
                        lbl.setBackground(COLOR_ROW_GREEN);
                        lbl.setForeground(Color.WHITE);
                    }
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_TABLE_ROW_ALT);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        });

        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 30 : 100);
        }
        return t;
    }

    static BigDecimal amount(Object value) {
        String text = value == null ? "" : value.toString().trim().replace(",", "");
        if (text.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(text); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + text + ". Nhập số, dùng dấu chấm cho phần thập phân.");
        }
    }

    static void print(Component parent, JTable table, String title) {
        try {
            table.print(JTable.PrintMode.FIT_WIDTH, new java.text.MessageFormat(title), new java.text.MessageFormat("Trang {0}"));
        } catch (Exception e) {
            error(parent, e);
        }
    }

    static String text(Object value) {
        return value == null ? "" : value.toString();
    }
}
