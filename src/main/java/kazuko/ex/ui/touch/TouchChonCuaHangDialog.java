package kazuko.ex.ui.touch;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/** Store picker used by touch reports. */
final class TouchChonCuaHangDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 110, 5), ORANGE = new Color(255, 130, 0), RED = new Color(165, 15, 15);
    private static boolean accepted;
    private final CuaHangDAO dao;
    private final JPanel rows = new JPanel();
    private CuaHang selected;

    private TouchChonCuaHangDialog(Window owner, CuaHangDAO dao, CuaHang initial) {
        super(owner, "CHỌN CỬA HÀNG", ModalityType.APPLICATION_MODAL);
        this.dao = dao; this.selected = initial; accepted = false;
        setUndecorated(true); setLayout(new BorderLayout(8, 8)); getContentPane().setBackground(BG);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));
        add(header(), BorderLayout.NORTH);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS)); rows.setBackground(new Color(232, 232, 232)); rows.setBorder(new EmptyBorder(7, 7, 7, 7));
        JScrollPane scroll = new JScrollPane(rows); scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(45); add(scroll, BorderLayout.CENTER);
        add(footer(), BorderLayout.SOUTH); reload();
        setSize(owner == null ? Toolkit.getDefaultToolkit().getScreenSize() : owner.getSize());
        setLocationRelativeTo(owner);
    }

    static CuaHang showDialog(Window owner, CuaHangDAO dao, CuaHang initial) {
        TouchChonCuaHangDialog dialog = new TouchChonCuaHangDialog(owner, dao, initial);
        dialog.setVisible(true); return dialog.selected;
    }

    static boolean wasAccepted() { return accepted; }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(HEADER); p.setPreferredSize(new Dimension(0, 68));
        JLabel l = new JLabel("CHỌN CỬA HÀNG", SwingConstants.CENTER); l.setForeground(Color.WHITE); l.setFont(TouchThongKeDoanhThuPanel.font(25)); p.add(l); return p;
    }

    private JPanel footer() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); left.setOpaque(false);
        JButton add = button("THÊM", GREEN), edit = button("SỬA", GREEN); add.addActionListener(e -> editStore(null)); edit.addActionListener(e -> editStore(selected));
        left.add(add); left.add(edit); p.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        JButton clear = button("XÓA TRỐNG", new Color(160, 15, 150)), close = button("THOÁT", RED);
        clear.addActionListener(e -> { selected = null; accepted = true; dispose(); });
        close.addActionListener(e -> { accepted = true; dispose(); }); right.add(clear); right.add(close); p.add(right, BorderLayout.EAST);
        return p;
    }

    private JButton button(String text, Color color) { JButton b = TouchThongKeDoanhThuPanel.button(text, color); b.setPreferredSize(new Dimension(225, 78)); return b; }

    private void reload() {
        rows.removeAll(); List<CuaHang> stores = dao.getRealStores();
        for (CuaHang store : stores) {
            boolean active = selected != null && selected.getId() != null && selected.getId().equals(store.getId());
            JButton row = TouchThongKeDoanhThuPanel.button(store.getName(), active ? ORANGE : GREEN);
            row.setHorizontalAlignment(SwingConstants.LEFT); row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY, 2, true), new EmptyBorder(0, 10, 0, 0)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 102)); row.setPreferredSize(new Dimension(0, 102));
            row.addActionListener(e -> { selected = store; reload(); }); rows.add(row); rows.add(Box.createVerticalStrut(7));
        }
        rows.revalidate(); rows.repaint();
    }

    private void editStore(CuaHang store) {
        JTextField name = new JTextField(store == null ? "" : store.getName()); name.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JTextField note = new JTextField(store == null ? "" : store.getGhiChu()); note.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5)); form.add(new JLabel("TÊN CỬA HÀNG")); form.add(name); form.add(new JLabel("GHI CHÚ")); form.add(note);
        int choice = JOptionPane.showConfirmDialog(this, form, store == null ? "THÊM MỚI CỬA HÀNG" : "CHỈNH SỬA CỬA HÀNG", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION || name.getText().trim().isEmpty()) return;
        if (store == null) {
            CuaHang created = new CuaHang(UUID.randomUUID().toString(), name.getText().trim(), name.getText().trim()); created.setGhiChu(note.getText().trim()); dao.insert(created); selected = created;
        } else {
            store.setName(name.getText().trim()); store.setGhiChu(note.getText().trim()); dao.update(store); selected = store;
        }
        reload();
    }
}
