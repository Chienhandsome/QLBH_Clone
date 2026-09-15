package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Column chooser matching the touch sample screen. */
final class TouchMatHangBanSettingsDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(126, 165, 194), TEAL = new Color(0, 79, 76), GREEN = new Color(0, 85, 20), GRAY = new Color(150, 150, 150), RED = new Color(165, 15, 15), ORANGE = new Color(226, 79, 5), PURPLE = new Color(90, 10, 80);
    private final TouchMatHangBanColumnConfig config = TouchMatHangBanConfigDAO.load();
    private final Runnable applied;
    private final JPanel unused = listPanel(), used = listPanel();
    private TouchMatHangBanColumnConfig.Column selectedUnused, selectedUsed;

    TouchMatHangBanSettingsDialog(Window owner, Runnable applied) {
        super(owner, "THIẾT LẬP CỘT HIỂN THỊ", ModalityType.APPLICATION_MODAL); this.applied = applied; setUndecorated(true); setContentPane(content());
        setSize(owner == null ? new Dimension(1200, 760) : owner.getSize()); setLocationRelativeTo(owner); refresh();
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout(0, 8)); root.setBackground(BG); root.setBorder(new EmptyBorder(7, 7, 7, 7));
        JLabel title = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER); title.setOpaque(true); title.setBackground(HEADER); title.setForeground(Color.WHITE); title.setFont(font(26)); title.setPreferredSize(new Dimension(0, 72)); root.add(title, BorderLayout.NORTH);
        JPanel area = new JPanel(new GridBagLayout()); area.setOpaque(false); GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.BOTH; c.weighty = 1; c.insets = new Insets(0, 5, 0, 5);
        c.gridx = 0; c.weightx = .40; area.add(titled("CÁC CỘT KHÔNG SỬ DỤNG", unused), c);
        JPanel transfer = new JPanel(new GridLayout(3, 1, 0, 8)); transfer.setOpaque(false); JButton add = button("THÊM >>", GRAY), remove = button("<< XÓA", GRAY), removeAll = button("<< XÓA HẾT", GREEN); add.addActionListener(e -> addColumn()); remove.addActionListener(e -> removeColumn()); removeAll.addActionListener(e -> removeAllColumns()); transfer.add(add); transfer.add(remove); transfer.add(removeAll); JPanel wrap = new JPanel(new GridBagLayout()); wrap.setOpaque(false); wrap.add(transfer); c.gridx = 1; c.weightx = .14; area.add(wrap, c);
        c.gridx = 2; c.weightx = .36; area.add(titled("CÁC CỘT SỬ DỤNG", used), c);
        JPanel order = new JPanel(new GridLayout(2, 1, 0, 8)); order.setOpaque(false); JButton up = button("LÊN", GRAY), down = button("XUỐNG", GRAY); up.addActionListener(e -> move(-1)); down.addActionListener(e -> move(1)); order.add(up); order.add(down); JPanel orderWrap = new JPanel(new GridBagLayout()); orderWrap.setOpaque(false); orderWrap.add(order); c.gridx = 3; c.weightx = .10; area.add(orderWrap, c); root.add(area, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); JButton format = button("ĐỊNH DẠNG", PURPLE); format.setPreferredSize(new Dimension(235, 70)); format.addActionListener(e -> JOptionPane.showMessageDialog(this, "Màu sắc và cỡ chữ dùng theo giao diện cảm ứng chung.", "ĐỊNH DẠNG", JOptionPane.INFORMATION_MESSAGE)); footer.add(format, BorderLayout.WEST); JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false); JButton save = button("CẬP NHẬT", GREEN), close = button("THOÁT", RED); save.setPreferredSize(new Dimension(270, 70)); close.setPreferredSize(new Dimension(225, 70)); save.addActionListener(e -> save()); close.addActionListener(e -> dispose()); actions.add(save); actions.add(close); footer.add(actions, BorderLayout.EAST); root.add(footer, BorderLayout.SOUTH); return root;
    }

    private void refresh() { unused.removeAll(); for (TouchMatHangBanColumnConfig.Column x : config.getUnused()) unused.add(column(x, false)); unused.add(Box.createVerticalGlue()); used.removeAll(); for (TouchMatHangBanColumnConfig.Column x : config.getUsed()) used.add(column(x, true)); used.add(Box.createVerticalGlue()); unused.revalidate(); used.revalidate(); unused.repaint(); used.repaint(); }
    private JButton column(TouchMatHangBanColumnConfig.Column x, boolean inUse) { JButton b = button(x.title, (inUse ? x == selectedUsed : x == selectedUnused) ? ORANGE : TEAL); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92)); b.setPreferredSize(new Dimension(0, 92)); b.addActionListener(e -> { if (inUse) selectedUsed = x; else selectedUnused = x; refresh(); }); return b; }
    private void addColumn() { if (selectedUnused == null) return; config.getUnused().remove(selectedUnused); config.getUsed().add(selectedUnused); selectedUsed = selectedUnused; selectedUnused = null; refresh(); }
    private void removeColumn() { if (selectedUsed == null) return; config.getUsed().remove(selectedUsed); config.getUnused().add(selectedUsed); selectedUnused = selectedUsed; selectedUsed = null; refresh(); }
    private void removeAllColumns() { config.getUnused().addAll(config.getUsed()); config.getUsed().clear(); selectedUsed = null; refresh(); }
    private void move(int direction) { if (selectedUsed == null) return; int old = config.getUsed().indexOf(selectedUsed), next = old + direction; if (old < 0 || next < 0 || next >= config.getUsed().size()) return; config.getUsed().remove(old); config.getUsed().add(next, selectedUsed); refresh(); }
    private void save() { if (config.getUsed().isEmpty()) { JOptionPane.showMessageDialog(this, "Phải giữ lại ít nhất một cột hiển thị.", "THÔNG BÁO", JOptionPane.WARNING_MESSAGE); return; } if (TouchMatHangBanConfigDAO.save(config)) { applied.run(); dispose(); } }
    private static JPanel listPanel() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(HEADER); p.setBorder(new EmptyBorder(5, 5, 5, 5)); return p; }
    private static JPanel titled(String title, JPanel content) { JPanel p = new JPanel(new BorderLayout(0, 6)); p.setOpaque(false); JLabel l = new JLabel(title); l.setForeground(Color.WHITE); l.setFont(font(18)); p.add(l, BorderLayout.NORTH); JScrollPane s = new JScrollPane(content); s.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); s.getVerticalScrollBar().setUnitIncrement(40); p.add(s); return p; }
    private static JButton button(String text, Color color) { JButton b = TouchThongKeDoanhThuPanel.button(text, color); b.setFont(font(18)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
}
