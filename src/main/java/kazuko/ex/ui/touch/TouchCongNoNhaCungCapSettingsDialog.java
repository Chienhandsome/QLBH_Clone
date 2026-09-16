package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Dialog thiết lập cột hiển thị cho màn hình Công nợ nhà cung cấp Touch. */
final class TouchCongNoNhaCungCapSettingsDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(126, 165, 194);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color GREEN = new Color(0, 85, 20);
    private static final Color GRAY = new Color(145, 145, 145);
    private static final Color RED = new Color(165, 15, 15);
    private static final Color PURPLE = new Color(90, 10, 80);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final Color OLIVE = new Color(90, 85, 0);

    private final TouchCongNoNhaCungCapColumnConfig config = TouchCongNoNhaCungCapConfigDAO.load();
    private final Runnable applied;
    private final JPanel unused = listPanel(), used = listPanel(), formatPanel = new JPanel(new BorderLayout(10, 0));
    private final JButton format = button("ĐỊNH DẠNG", PURPLE);
    private final JButton columns = button("SỐ CỘT: 2", OLIVE);
    private final JButton rows = button("SỐ DÒNG: 7", OLIVE);
    private final JButton cell = button("MÀU Ô", OLIVE);
    private final JButton title = button("HIỆN TIÊU ĐỀ", OLIVE);
    private TouchCongNoNhaCungCapColumnConfig.ColumnItem selectedUnused, selectedUsed;
    private boolean expanded;

    TouchCongNoNhaCungCapSettingsDialog(Window owner, Runnable applied) {
        super(owner, "THIẾT LẬP CỘT HIỂN THỊ", ModalityType.APPLICATION_MODAL);
        this.applied = applied;
        setUndecorated(true);
        setContentPane(content());
        setSize(1200, 740);
        setLocationRelativeTo(owner);
        refresh();
        updateFooter();
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout(0, 7));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(5, 5, 7, 5));
        JLabel h = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER);
        h.setOpaque(true);
        h.setBackground(HEADER);
        h.setForeground(Color.WHITE);
        h.setFont(font(27));
        h.setPreferredSize(new Dimension(0, 74));
        root.add(h, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 5));
        center.setOpaque(false);
        center.add(lists(), BorderLayout.CENTER);
        formatPanel.setOpaque(false);
        formatPanel.add(preview(), BorderLayout.WEST);
        formatPanel.add(formatControls(), BorderLayout.CENTER);
        formatPanel.setVisible(false);
        center.add(formatPanel, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);
        root.add(footer(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel lists() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 0.45;
        c.gridx = 0;
        p.add(columnBox("CỘT KHÔNG SỬ DỤNG", unused), c);

        JPanel mid = new JPanel(new GridLayout(4, 1, 0, 10));
        mid.setOpaque(false);
        mid.setBorder(new EmptyBorder(40, 10, 40, 10));
        JButton addAll = button(">>", TEAL);
        JButton addOne = button(">", TEAL);
        JButton removeOne = button("<", TEAL);
        JButton removeAll = button("<<", TEAL);
        addAll.addActionListener(e -> moveAll(config.getUnusedColumns(), config.getUsedColumns()));
        addOne.addActionListener(e -> moveSelected(config.getUnusedColumns(), config.getUsedColumns(), selectedUnused));
        removeOne.addActionListener(e -> moveSelected(config.getUsedColumns(), config.getUnusedColumns(), selectedUsed));
        removeAll.addActionListener(e -> moveAll(config.getUsedColumns(), config.getUnusedColumns()));
        mid.add(addAll);
        mid.add(addOne);
        mid.add(removeOne);
        mid.add(removeAll);
        c.weightx = 0.1;
        c.gridx = 1;
        p.add(mid, c);

        c.weightx = 0.45;
        c.gridx = 2;
        p.add(columnBox("CỘT SỬ DỤNG", used), c);
        return p;
    }

    private JPanel columnBox(String title, JPanel list) {
        JPanel b = new JPanel(new BorderLayout(0, 5));
        b.setOpaque(false);
        JLabel l = new JLabel(title, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(HEADER);
        l.setForeground(Color.WHITE);
        l.setFont(font(20));
        l.setPreferredSize(new Dimension(0, 48));
        b.add(l, BorderLayout.NORTH);
        JScrollPane s = new JScrollPane(list);
        s.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        s.getViewport().setBackground(Color.WHITE);
        b.add(s, BorderLayout.CENTER);
        return b;
    }

    private JPanel listPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        return p;
    }

    private JPanel preview() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(config.getCardBgColor());
        p.setPreferredSize(new Dimension(280, 140));
        p.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        JLabel t = new JLabel("Nhà cung cấp Ánh Dương", SwingConstants.CENTER);
        t.setForeground(Color.WHITE);
        t.setFont(font(17));
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    private JPanel formatControls() {
        JPanel p = new JPanel(new GridLayout(2, 3, 8, 8));
        p.setOpaque(false);
        JButton textColor = button("MÀU CHỮ", PURPLE);
        textColor.addActionListener(e -> {
            if (selectedUsed == null) return;
            Color c = JColorChooser.showDialog(this, "Chọn màu chữ", selectedUsed.getColor());
            if (c != null) {
                selectedUsed.setColor(c);
                refresh();
            }
        });
        JButton bold = button("IN ĐẬM", PURPLE);
        bold.addActionListener(e -> {
            if (selectedUsed == null) return;
            selectedUsed.setBold(!selectedUsed.isBold());
            refresh();
        });
        JButton italic = button("IN NGHIÊNG", PURPLE);
        italic.addActionListener(e -> {
            if (selectedUsed == null) return;
            selectedUsed.setItalic(!selectedUsed.isItalic());
            refresh();
        });
        JButton align = button("CANH PHẢI", PURPLE);
        align.addActionListener(e -> {
            if (selectedUsed == null) return;
            selectedUsed.setRightAligned(!selectedUsed.isRightAligned());
            refresh();
        });
        JButton fontInc = button("CỠ CHỮ +", PURPLE);
        fontInc.addActionListener(e -> {
            if (selectedUsed == null) return;
            selectedUsed.setFontSize(selectedUsed.getFontSize() + 1);
            refresh();
        });
        JButton fontDec = button("CỠ CHỮ -", PURPLE);
        fontDec.addActionListener(e -> {
            if (selectedUsed == null) return;
            selectedUsed.setFontSize(Math.max(12, selectedUsed.getFontSize() - 1));
            refresh();
        });
        p.add(textColor);
        p.add(bold);
        p.add(italic);
        p.add(align);
        p.add(fontInc);
        p.add(fontDec);
        return p;
    }

    private JPanel footer() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        f.setOpaque(false);
        format.addActionListener(e -> {
            expanded = !expanded;
            formatPanel.setVisible(expanded);
            revalidate();
            repaint();
        });
        columns.addActionListener(e -> {
            int c = config.getGridColumns() == 2 ? 3 : (config.getGridColumns() == 3 ? 1 : 2);
            config.setGridColumns(c);
            updateFooter();
        });
        rows.addActionListener(e -> {
            int r = config.getGridRows() >= 10 ? 5 : config.getGridRows() + 1;
            config.setGridRows(r);
            updateFooter();
        });
        cell.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Chọn màu nền ô", config.getCardBgColor());
            if (c != null) config.setCardBgColor(c);
        });
        title.addActionListener(e -> {
            config.setShowTitle(!config.isShowTitle());
            updateFooter();
        });

        JButton def = button("MẶC ĐỊNH", GREEN);
        def.addActionListener(e -> {
            TouchCongNoNhaCungCapColumnConfig d = TouchCongNoNhaCungCapColumnConfig.createDefault();
            config.getUsedColumns().clear();
            config.getUsedColumns().addAll(d.getUsedColumns());
            config.getUnusedColumns().clear();
            config.getUnusedColumns().addAll(d.getUnusedColumns());
            config.setCardBgColor(d.getCardBgColor());
            config.setGridColumns(d.getGridColumns());
            config.setGridRows(d.getGridRows());
            config.setShowTitle(d.isShowTitle());
            refresh();
            updateFooter();
        });
        JButton save = button("LƯU", GRAY);
        save.addActionListener(e -> {
            TouchCongNoNhaCungCapConfigDAO.save(config);
            if (applied != null) applied.run();
            dispose();
        });
        JButton close = button("THOÁT", RED);
        close.addActionListener(e -> dispose());

        f.add(format);
        f.add(columns);
        f.add(rows);
        f.add(cell);
        f.add(title);
        f.add(def);
        f.add(save);
        f.add(close);
        return f;
    }

    private void updateFooter() {
        columns.setText("SỐ CỘT: " + config.getGridColumns());
        rows.setText("SỐ DÒNG: " + config.getGridRows());
        title.setText(config.isShowTitle() ? "ẨN TIÊU ĐỀ" : "HIỆN TIÊU ĐỀ");
    }

    private void refresh() {
        unused.removeAll();
        used.removeAll();
        for (var item : config.getUnusedColumns()) {
            unused.add(row(item, item == selectedUnused, () -> {
                selectedUnused = item;
                selectedUsed = null;
                refresh();
            }));
        }
        for (var item : config.getUsedColumns()) {
            used.add(row(item, item == selectedUsed, () -> {
                selectedUsed = item;
                selectedUnused = null;
                refresh();
            }));
        }
        unused.revalidate();
        unused.repaint();
        used.revalidate();
        used.repaint();
    }

    private JPanel row(TouchCongNoNhaCungCapColumnConfig.ColumnItem item, boolean sel, Runnable onClick) {
        JPanel r = new JPanel(new BorderLayout());
        r.setBackground(sel ? ORANGE : Color.WHITE);
        r.setBorder(new EmptyBorder(6, 12, 6, 12));
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        JLabel l = new JLabel(item.getTitle());
        l.setForeground(sel ? Color.WHITE : Color.BLACK);
        l.setFont(item.getFont());
        r.add(l, BorderLayout.WEST);
        r.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        r.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { onClick.run(); }
        });
        return r;
    }

    private void moveAll(java.util.List<TouchCongNoNhaCungCapColumnConfig.ColumnItem> from,
                         java.util.List<TouchCongNoNhaCungCapColumnConfig.ColumnItem> to) {
        to.addAll(from);
        from.clear();
        selectedUnused = null;
        selectedUsed = null;
        refresh();
    }

    private void moveSelected(java.util.List<TouchCongNoNhaCungCapColumnConfig.ColumnItem> from,
                              java.util.List<TouchCongNoNhaCungCapColumnConfig.ColumnItem> to,
                              TouchCongNoNhaCungCapColumnConfig.ColumnItem item) {
        if (item == null) return;
        from.remove(item);
        to.add(item);
        selectedUnused = null;
        selectedUsed = null;
        refresh();
    }

    private static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(font(17));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(135, 52));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        return b;
    }

    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
}
