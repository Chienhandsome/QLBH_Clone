package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Touch column and formatting editor for supplier cards. */
final class TouchNhaCungCapSettingsDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(126, 165, 194),
            TEAL = new Color(0, 79, 76), GREEN = new Color(0, 85, 20), GRAY = new Color(145, 145, 145),
            RED = new Color(165, 15, 15), PURPLE = new Color(90, 10, 80), ORANGE = new Color(226, 79, 5), OLIVE = new Color(90, 85, 0);
    private final Runnable applied;
    private final TouchNhaCungCapColumnConfig config = TouchNhaCungCapConfigDAO.load();
    private final JPanel unused = listPanel(), used = listPanel(), formattingPanel = new JPanel(new BorderLayout(12, 0));
    private final JButton format = button("ĐỊNH DẠNG", PURPLE), columns = button("SỐ CỘT: 2", OLIVE),
            rows = button("SỐ DÒNG: 7", OLIVE), cell = button("MÀU Ô", OLIVE), title = button("HIỆN TIÊU ĐỀ", OLIVE);
    private TouchNhaCungCapColumnConfig.ColumnItem selectedUnused, selectedUsed;
    private boolean formatting;

    TouchNhaCungCapSettingsDialog(Window owner, Runnable applied) {
        super(owner, "THIẾT LẬP CỘT HIỂN THỊ", ModalityType.APPLICATION_MODAL);
        this.applied = applied;
        setUndecorated(true); setContentPane(content()); setSize(1200, 740); setLocationRelativeTo(owner);
        refresh(); updateFooter();
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout(0, 7)); root.setBackground(BG); root.setBorder(new EmptyBorder(5, 5, 7, 5));
        JLabel heading = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER); heading.setOpaque(true); heading.setBackground(HEADER); heading.setForeground(Color.WHITE); heading.setFont(font(27)); heading.setPreferredSize(new Dimension(0, 74)); root.add(heading, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(10, 5)); center.setOpaque(false); center.add(lists());
        formattingPanel.setOpaque(false); formattingPanel.setBorder(new EmptyBorder(5, 0, 0, 0)); formattingPanel.add(preview(), BorderLayout.WEST); formattingPanel.add(formatControls()); formattingPanel.setVisible(false); center.add(formattingPanel, BorderLayout.SOUTH); root.add(center);
        root.add(footer(), BorderLayout.SOUTH); return root;
    }

    private JPanel lists() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(0, 5, 0, 5); c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.gridx = 0; c.weightx = .42; p.add(titled("CÁC CỘT KHÔNG SỬ DỤNG", unused), c);
        JPanel transfer = new JPanel(new GridLayout(3, 1, 0, 8)); transfer.setOpaque(false); JButton add = button("THÊM >>", GRAY), remove = button("<< XÓA", GRAY), all = button("<< XÓA HẾT", GREEN); add.addActionListener(e -> addColumn()); remove.addActionListener(e -> removeColumn()); all.addActionListener(e -> removeAllColumns()); transfer.add(add); transfer.add(remove); transfer.add(all); JPanel tw = new JPanel(new GridBagLayout()); tw.setOpaque(false); tw.add(transfer); c.gridx = 1; c.weightx = .14; p.add(tw, c);
        c.gridx = 2; c.weightx = .34; p.add(titled("CÁC CỘT SỬ DỤNG", used), c);
        JPanel order = new JPanel(new GridLayout(2, 1, 0, 8)); order.setOpaque(false); JButton up = button("LÊN", GRAY), down = button("XUỐNG", GRAY); up.addActionListener(e -> move(-1)); down.addActionListener(e -> move(1)); order.add(up); order.add(down); JPanel ow = new JPanel(new GridBagLayout()); ow.setOpaque(false); ow.add(order); c.gridx = 3; c.weightx = .10; p.add(ow, c); return p;
    }

    private JPanel preview() {
        JPanel p = new JPanel(new GridLayout(1, 1)); p.setBackground(config.getCardBgColor()); p.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); p.setPreferredSize(new Dimension(470, 135)); JLabel l = new JLabel("Tên nhà cung cấp", SwingConstants.CENTER); l.setForeground(Color.WHITE); l.setFont(font(18)); p.add(l); return p;
    }

    private JPanel formatControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); p.setOpaque(false);
        JButton left = button("CĂN TRÁI", GRAY), right = button("CĂN PHẢI", GRAY), bold = button("ĐẬM", GRAY), italic = button("NGHIÊNG", GRAY), size = button("CỠ CHỮ", GRAY), color = button("MÀU CHỮ", GRAY), prefix = button("TIÊU ĐỀ VIẾT TẮT", GRAY);
        for (JButton b : new JButton[]{left, right, bold, italic, size, color, prefix}) { b.setPreferredSize(new Dimension(135, 58)); p.add(b); }
        left.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setRightAligned(false); }); right.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setRightAligned(true); });
        bold.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setBold(!selectedUsed.isBold()); }); italic.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setItalic(!selectedUsed.isItalic()); });
        size.addActionListener(e -> { if (selectedUsed != null) { Integer v = TouchNumberInputDialog.showDialog(this, "NHẬP CỠ CHỮ", selectedUsed.getFontSize(), 10, 50); if (v != null) selectedUsed.setFontSize(v); } });
        color.addActionListener(e -> { if (selectedUsed != null) { Color v = TouchChonMauDialog.show(this, selectedUsed.getColor()); if (v != null) selectedUsed.setColor(v); } });
        prefix.addActionListener(e -> { if (selectedUsed != null) { String v = TouchTextKeyboardDialog.showDialog(this, "NHẬP TIÊU ĐỀ VIẾT TẮT", selectedUsed.getPrefix()); if (v != null) selectedUsed.setPrefix(v); } }); return p;
    }

    private JPanel footer() {
        JPanel p = new JPanel(new BorderLayout(8, 0)); p.setOpaque(false); p.setBorder(new EmptyBorder(5, 8, 0, 8)); JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); left.setOpaque(false);
        for (JButton b : new JButton[]{format, columns, rows, cell, title}) { b.setPreferredSize(new Dimension(190, 58)); left.add(b); }
        format.addActionListener(e -> toggleFormat()); columns.addActionListener(e -> { Integer v = TouchNumberInputDialog.showDialog(this, "NHẬP SỐ CỘT", config.getGridColumns(), 1, 10); if (v != null) { config.setGridColumns(v); updateFooter(); } }); rows.addActionListener(e -> { Integer v = TouchNumberInputDialog.showDialog(this, "NHẬP SỐ DÒNG", config.getGridRows(), 1, 50); if (v != null) { config.setGridRows(v); updateFooter(); } }); cell.addActionListener(e -> { Color v = TouchChonMauDialog.show(this, config.getCardBgColor()); if (v != null) config.setCardBgColor(v); }); title.addActionListener(e -> { config.setShowTitle(!config.isShowTitle()); updateFooter(); }); p.add(left, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false); JButton save = button("CẬP NHẬT", GREEN), close = button("THOÁT", RED); save.setPreferredSize(new Dimension(200, 58)); close.setPreferredSize(new Dimension(190, 58)); save.addActionListener(e -> save()); close.addActionListener(e -> dispose()); actions.add(save); actions.add(close); p.add(actions, BorderLayout.EAST); return p;
    }

    private void refresh() { unused.removeAll(); for (var x : config.getUnusedColumns()) unused.add(column(x, false)); unused.add(Box.createVerticalGlue()); used.removeAll(); for (var x : config.getUsedColumns()) used.add(column(x, true)); used.add(Box.createVerticalGlue()); unused.revalidate(); used.revalidate(); unused.repaint(); used.repaint(); }
    private JButton column(TouchNhaCungCapColumnConfig.ColumnItem x, boolean inUse) { JButton b = button(x.getTitle(), (inUse ? x == selectedUsed : x == selectedUnused) ? ORANGE : TEAL); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); b.setPreferredSize(new Dimension(0, 80)); b.addActionListener(e -> { if (inUse) selectedUsed = x; else selectedUnused = x; refresh(); }); return b; }
    private void addColumn() { if (selectedUnused == null) return; config.getUnusedColumns().remove(selectedUnused); config.getUsedColumns().add(selectedUnused); selectedUsed = selectedUnused; selectedUnused = null; refresh(); }
    private void removeColumn() { if (selectedUsed == null) return; config.getUsedColumns().remove(selectedUsed); config.getUnusedColumns().add(selectedUsed); selectedUnused = selectedUsed; selectedUsed = null; refresh(); }
    private void removeAllColumns() { config.getUnusedColumns().addAll(config.getUsedColumns()); config.getUsedColumns().clear(); selectedUsed = null; refresh(); }
    private void move(int d) { if (selectedUsed == null) return; int old = config.getUsedColumns().indexOf(selectedUsed), next = old + d; if (old < 0 || next < 0 || next >= config.getUsedColumns().size()) return; config.getUsedColumns().remove(old); config.getUsedColumns().add(next, selectedUsed); refresh(); }
    private void save() { if (config.getUsedColumns().isEmpty()) { JOptionPane.showMessageDialog(this, "Phải giữ lại ít nhất một cột hiển thị.", "THÔNG BÁO", JOptionPane.WARNING_MESSAGE); return; } if (TouchNhaCungCapConfigDAO.save(config)) { applied.run(); dispose(); } }
    private void toggleFormat() { formatting = !formatting; formattingPanel.setVisible(formatting); format.setBackground(formatting ? ORANGE : PURPLE); setSize(1200, formatting ? 860 : 740); setLocationRelativeTo(getOwner()); updateFooter(); revalidate(); }
    private void updateFooter() { columns.setText("SỐ CỘT: " + config.getGridColumns()); rows.setText("SỐ DÒNG: " + config.getGridRows()); title.setBackground(config.isShowTitle() ? ORANGE : OLIVE); columns.setVisible(formatting); rows.setVisible(formatting); cell.setVisible(formatting); title.setVisible(formatting); }
    private static JPanel listPanel() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(HEADER); p.setBorder(new EmptyBorder(5, 5, 5, 5)); return p; }
    private static JPanel titled(String text, JPanel content) { JPanel p = new JPanel(new BorderLayout(0, 5)); p.setOpaque(false); JLabel l = new JLabel(text); l.setForeground(Color.WHITE); l.setFont(font(18)); p.add(l, BorderLayout.NORTH); JScrollPane s = new JScrollPane(content); s.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); s.getVerticalScrollBar().setUnitIncrement(40); p.add(s); return p; }
    private static JButton button(String text, Color color) { JButton b = new JButton(text); b.setBackground(color); b.setForeground(Color.WHITE); b.setFont(font(18)); b.setFocusPainted(false); b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
}
