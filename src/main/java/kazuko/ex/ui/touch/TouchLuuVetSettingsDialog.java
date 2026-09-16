package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Touch column/format editor for activity-log cards. */
final class TouchLuuVetSettingsDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color GREEN = new Color(0, 85, 20);
    private static final Color GRAY = new Color(145, 145, 145);
    private static final Color RED = new Color(165, 15, 15);
    private static final Color PURPLE = new Color(90, 10, 80);
    private static final Color ORANGE = new Color(226, 79, 5);

    private final Runnable applied;
    private final TouchLuuVetColumnConfig config = TouchLuuVetConfigDAO.load();
    private final JPanel unusedPanel = listPanel();
    private final JPanel usedPanel = listPanel();
    private final JPanel formatPanel = new JPanel(new BorderLayout(12, 0));
    private final JButton formatButton = button("ĐỊNH DẠNG", PURPLE);
    private final JButton columnsButton = button("SỐ CỘT: 1", new Color(90, 85, 0));
    private final JButton rowsButton = button("SỐ DÒNG: 10", new Color(90, 85, 0));
    private final JButton cellColorButton = button("MÀU Ô", new Color(90, 85, 0));
    private final JButton titleButton = button("HIỆN TIÊU ĐỀ", new Color(90, 85, 0));
    private TouchLuuVetColumnConfig.ColumnItem selectedUnused;
    private TouchLuuVetColumnConfig.ColumnItem selectedUsed;
    private boolean formatting;

    TouchLuuVetSettingsDialog(Window owner, Runnable applied) {
        super(owner, "THIẾT LẬP CỘT HIỂN THỊ", ModalityType.APPLICATION_MODAL);
        this.applied = applied;
        setUndecorated(true);
        setContentPane(createContent());
        setSize(1200, 740);
        setLocationRelativeTo(owner);
        refreshLists();
        updateBottomButtons();
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(0, 7)); root.setBackground(BG); root.setBorder(new LineBorder(BG, 5));
        JLabel header = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER); header.setOpaque(true); header.setBackground(HEADER); header.setForeground(Color.WHITE); header.setFont(font(27)); header.setPreferredSize(new Dimension(0, 74)); root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 5)); center.setOpaque(false);
        center.add(createLists(), BorderLayout.CENTER);
        formatPanel.setOpaque(false); formatPanel.setBorder(new EmptyBorder(5, 0, 0, 0)); formatPanel.add(createPreview(), BorderLayout.WEST); formatPanel.add(createFormatControls(), BorderLayout.CENTER); formatPanel.setVisible(false);
        center.add(formatPanel, BorderLayout.SOUTH); root.add(center, BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel createLists() {
        JPanel area = new JPanel(new GridBagLayout()); area.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(0, 5, 0, 5); c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        JPanel unusedBox = titled("CÁC CỘT KHÔNG SỬ DỤNG", unusedPanel); c.gridx = 0; c.weightx = .42; area.add(unusedBox, c);
        JPanel transfer = new JPanel(new GridLayout(3, 1, 0, 8)); transfer.setOpaque(false);
        JButton add = button("THÊM >>", GRAY), remove = button("<< XÓA", GRAY), removeAll = button("<< XÓA HẾT", GREEN);
        add.addActionListener(e -> addColumn()); remove.addActionListener(e -> removeColumn()); removeAll.addActionListener(e -> removeAllColumns()); transfer.add(add); transfer.add(remove); transfer.add(removeAll);
        JPanel transferWrap = new JPanel(new GridBagLayout()); transferWrap.setOpaque(false); transferWrap.add(transfer); c.gridx = 1; c.weightx = .14; area.add(transferWrap, c);
        JPanel usedBox = titled("CÁC CỘT SỬ DỤNG", usedPanel); c.gridx = 2; c.weightx = .34; area.add(usedBox, c);
        JPanel order = new JPanel(new GridLayout(2, 1, 0, 8)); order.setOpaque(false);
        JButton up = button("LÊN", GRAY), down = button("XUỐNG", GRAY); up.addActionListener(e -> move(-1)); down.addActionListener(e -> move(1)); order.add(up); order.add(down);
        JPanel orderWrap = new JPanel(new GridBagLayout()); orderWrap.setOpaque(false); orderWrap.add(order); c.gridx = 3; c.weightx = .10; area.add(orderWrap, c);
        return area;
    }

    private JPanel createPreview() {
        JPanel preview = new JPanel(new GridLayout(2, 2, 8, 8)); preview.setBackground(config.getCardBgColor()); preview.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); preview.setPreferredSize(new Dimension(470, 135));
        String[] text = {"Số đơn hàng", "Tài khoản", "Nội dung", "Giờ"};
        for (int i = 0; i < text.length; i++) { JLabel l = new JLabel(text[i], i % 2 == 1 ? SwingConstants.RIGHT : SwingConstants.LEFT); l.setForeground(Color.WHITE); l.setFont(font(16)); preview.add(l); }
        return preview;
    }

    private JPanel createFormatControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); controls.setOpaque(false);
        JButton left = button("CĂN TRÁI", GRAY), right = button("CĂN PHẢI", GRAY), bold = button("ĐẬM", GRAY), italic = button("NGHIÊNG", GRAY), size = button("CỠ CHỮ", GRAY), color = button("MÀU CHỮ", GRAY), prefix = button("TIÊU ĐỀ VIẾT TẮT", GRAY);
        for (JButton b : new JButton[]{left, right, bold, italic, size, color, prefix}) { b.setPreferredSize(new Dimension(135, 58)); controls.add(b); }
        left.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setRightAligned(false); });
        right.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setRightAligned(true); });
        bold.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setBold(!selectedUsed.isBold()); });
        italic.addActionListener(e -> { if (selectedUsed != null) selectedUsed.setItalic(!selectedUsed.isItalic()); });
        size.addActionListener(e -> { if (selectedUsed != null) { Integer v = TouchNumberInputDialog.showDialog(this, "NHẬP CỠ CHỮ", selectedUsed.getFontSize(), 10, 50); if (v != null) selectedUsed.setFontSize(v); } });
        color.addActionListener(e -> { if (selectedUsed != null) { Color v = TouchChonMauDialog.show(this, selectedUsed.getColor()); if (v != null) selectedUsed.setColor(v); } });
        prefix.addActionListener(e -> { if (selectedUsed != null) { String v = TouchTextKeyboardDialog.showDialog(this, "NHẬP TIÊU ĐỀ VIẾT TẮT", selectedUsed.getPrefix()); if (v != null) selectedUsed.setPrefix(v); } });
        return controls;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 0)); footer.setOpaque(false); footer.setBorder(new EmptyBorder(5, 8, 7, 8));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); left.setOpaque(false);
        for (JButton b : new JButton[]{formatButton, columnsButton, rowsButton, cellColorButton, titleButton}) { b.setPreferredSize(new Dimension(190, 58)); left.add(b); }
        formatButton.addActionListener(e -> toggleFormatting());
        TouchGridSettingsSupport.bind(this, columnsButton, rowsButton, config, this::updateBottomButtons);
        cellColorButton.addActionListener(e -> { Color v = TouchChonMauDialog.show(this, config.getCardBgColor()); if (v != null) config.setCardBgColor(v); });
        titleButton.addActionListener(e -> { config.setShowTitle(!config.isShowTitle()); updateBottomButtons(); });
        footer.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        JButton save = button("CẬP NHẬT", GREEN), close = button("THOÁT", RED); save.setPreferredSize(new Dimension(200, 58)); close.setPreferredSize(new Dimension(190, 58));
        save.addActionListener(e -> { if (TouchLuuVetConfigDAO.save(config)) { applied.run(); dispose(); } }); close.addActionListener(e -> dispose()); right.add(save); right.add(close); footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void refreshLists() {
        unusedPanel.removeAll();
        for (TouchLuuVetColumnConfig.ColumnItem item : config.getUnusedColumns()) unusedPanel.add(columnButton(item, false));
        unusedPanel.add(Box.createVerticalGlue());
        usedPanel.removeAll();
        for (TouchLuuVetColumnConfig.ColumnItem item : config.getUsedColumns()) usedPanel.add(columnButton(item, true));
        usedPanel.add(Box.createVerticalGlue());
        unusedPanel.revalidate(); usedPanel.revalidate(); unusedPanel.repaint(); usedPanel.repaint();
    }

    private JButton columnButton(TouchLuuVetColumnConfig.ColumnItem item, boolean used) {
        boolean selected = used ? item == selectedUsed : item == selectedUnused;
        JButton b = button(item.getTitle(), selected ? ORANGE : TEAL); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); b.setPreferredSize(new Dimension(0, 80));
        b.addActionListener(e -> { if (used) selectedUsed = item; else selectedUnused = item; refreshLists(); }); return b;
    }

    private void addColumn() { if (selectedUnused == null) return; config.getUnusedColumns().remove(selectedUnused); config.getUsedColumns().add(selectedUnused); selectedUsed = selectedUnused; selectedUnused = null; refreshLists(); }
    private void removeColumn() { if (selectedUsed == null) return; config.getUsedColumns().remove(selectedUsed); config.getUnusedColumns().add(selectedUsed); selectedUnused = selectedUsed; selectedUsed = null; refreshLists(); }
    private void removeAllColumns() { config.getUnusedColumns().addAll(config.getUsedColumns()); config.getUsedColumns().clear(); selectedUsed = null; refreshLists(); }
    private void move(int delta) { if (selectedUsed == null) return; int old = config.getUsedColumns().indexOf(selectedUsed), next = old + delta; if (old < 0 || next < 0 || next >= config.getUsedColumns().size()) return; config.getUsedColumns().remove(old); config.getUsedColumns().add(next, selectedUsed); refreshLists(); }
    private void toggleFormatting() { formatting = !formatting; formatPanel.setVisible(formatting); formatButton.setBackground(formatting ? ORANGE : PURPLE); columnsButton.setVisible(formatting); rowsButton.setVisible(formatting); cellColorButton.setVisible(formatting); titleButton.setVisible(formatting); setSize(1200, formatting ? 860 : 740); setLocationRelativeTo(getOwner()); revalidate(); }
    private void updateBottomButtons() { TouchGridSettingsSupport.refreshLabels(columnsButton, rowsButton, config); titleButton.setBackground(config.isShowTitle() ? ORANGE : new Color(90, 85, 0)); columnsButton.setVisible(formatting); rowsButton.setVisible(formatting); cellColorButton.setVisible(formatting); titleButton.setVisible(formatting); }

    private static JPanel listPanel() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(HEADER); p.setBorder(new EmptyBorder(5, 5, 5, 5)); return p; }
    private static JPanel titled(String title, JPanel content) { JPanel p = new JPanel(new BorderLayout(0, 5)); p.setOpaque(false); JLabel l = new JLabel(title); l.setForeground(Color.WHITE); l.setFont(font(18)); p.add(l, BorderLayout.NORTH); JScrollPane s = new JScrollPane(content); s.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); s.getVerticalScrollBar().setUnitIncrement(40); p.add(s, BorderLayout.CENTER); return p; }
    private static JButton button(String text, Color color) { JButton b = new JButton(text); b.setBackground(color); b.setForeground(Color.WHITE); b.setFont(font(18)); b.setFocusPainted(false); b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
}
