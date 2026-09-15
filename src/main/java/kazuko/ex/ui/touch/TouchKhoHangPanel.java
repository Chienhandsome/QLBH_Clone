package kazuko.ex.ui.touch;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Touch warehouse catalogue. Its interaction mirrors the store catalogue. */
final class TouchKhoHangPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 120, 0);
    private static final Color BLUE = new Color(5, 5, 145);
    private static final Color RED = new Color(155, 0, 0);

    private final KhoHangDAO dao = new KhoHangDAO();
    private final Runnable exit;
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private final JTextField name = field();
    private final JTextField note = field();
    private JLabel formTitle;
    private JPanel listScreen;
    private KhoHang editing;

    TouchKhoHangPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout());
        screens.add(createForm(), "form");
        add(screens);
        showList();
    }

    private void showList() {
        if (listScreen != null) screens.remove(listScreen);
        listScreen = base();
        listScreen.add(header("DANH MỤC KHO HÀNG"), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setBackground(new Color(235, 235, 235));
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(6, 6, 6, 6));
        for (KhoHang warehouse : dao.getAllActive()) rows.add(row(warehouse));
        rows.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(rows, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        scroll.getVerticalScrollBar().setUnitIncrement(45);
        listScreen.add(scroll);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 5, 0, 5));
        JButton add = button("THÊM", GREEN, 225, 78);
        add.addActionListener(e -> beginAdd());
        JButton close = button("THOÁT", RED, 225, 78);
        close.addActionListener(e -> exit.run());
        footer.add(add, BorderLayout.WEST);
        footer.add(close, BorderLayout.EAST);
        listScreen.add(footer, BorderLayout.SOUTH);

        screens.add(listScreen, "list");
        cards.show(screens, "list");
        screens.revalidate();
        screens.repaint();
    }

    private JPanel row(KhoHang warehouse) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 7, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        row.setPreferredSize(new Dimension(0, 108));
        JLabel title = new JLabel(safe(warehouse.getName()).toUpperCase());
        title.setOpaque(true);
        title.setBackground(GREEN);
        title.setForeground(Color.WHITE);
        title.setFont(font(24));
        title.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.add(title);
        JPanel actions = new JPanel(new GridLayout(1, 2, 6, 0));
        actions.setOpaque(false);
        JButton delete = button("XÓA", RED, 110, 101);
        JButton edit = button("SỬA", BLUE, 110, 101);
        delete.addActionListener(e -> delete(warehouse));
        edit.addActionListener(e -> beginEdit(warehouse));
        actions.add(delete);
        actions.add(edit);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JPanel createForm() {
        JPanel root = base();
        formTitle = header("THÊM MỚI KHO HÀNG");
        root.add(formTitle, BorderLayout.NORTH);
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(14, 10, 14, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        addField(body, c, 0, "TÊN KHO HÀNG", name);
        addField(body, c, 1, "GHI CHÚ:", note);
        c.gridy = 2; c.weighty = 1;
        body.add(Box.createVerticalGlue(), c);
        root.add(body);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        footer.setOpaque(false);
        JButton save = button("CẬP NHẬT", new Color(140, 140, 140), 355, 95);
        JButton close = button("THOÁT", RED, 290, 95);
        save.addActionListener(e -> save());
        close.addActionListener(e -> showList());
        footer.add(save); footer.add(close);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private void addField(JPanel panel, GridBagConstraints c, int y, String text, JTextField target) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(font(19));
        label.setPreferredSize(new Dimension(450, 52));
        c.gridx = 0; c.gridy = y; c.weightx = 0; panel.add(label, c);
        c.gridx = 1; c.weightx = 1; panel.add(target, c);
        JButton keyboard = button("⌨", new Color(0, 85, 90), 72, 72);
        keyboard.setFont(new Font("Segoe UI Symbol", Font.BOLD, 29));
        keyboard.addActionListener(e -> target.setText(TouchTextKeyboardDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), text, target.getText())));
        c.gridx = 2; c.weightx = 0; panel.add(keyboard, c);
    }

    private void beginAdd() {
        editing = new KhoHang();
        name.setText(""); note.setText("");
        formTitle.setText("THÊM MỚI KHO HÀNG");
        cards.show(screens, "form");
    }

    private void beginEdit(KhoHang warehouse) {
        editing = warehouse;
        name.setText(safe(warehouse.getName()));
        note.setText(safe(warehouse.getNote()));
        formTitle.setText("CHỈNH SỬA KHO HÀNG");
        cards.show(screens, "form");
    }

    private void save() {
        if (name.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên kho hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (editing == null) editing = new KhoHang();
        boolean isNew = editing.getId() == null || editing.getId().isBlank();
        editing.setName(name.getText().trim());
        editing.setNote(note.getText().trim());
        boolean ok = isNew ? dao.insert(editing) : dao.update(editing);
        if (!ok) JOptionPane.showMessageDialog(this, "Không thể cập nhật kho hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        else showList();
    }

    private void delete(KhoHang warehouse) {
        if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this),
                "BẠN CÓ MUỐN XÓA KHO HÀNG<br>" + safe(warehouse.getName()).toUpperCase() + " KHÔNG?")) return;
        if (warehouse.getId() != null && warehouse.getId().equals(dao.getDefaultWarehouseId())) dao.setDefaultWarehouseId(null);
        dao.moveToTrash(warehouse.getId());
        showList();
    }

    private static JPanel base() { JPanel p = new JPanel(new BorderLayout(0, 8)); p.setBackground(BG); p.setBorder(new EmptyBorder(5, 5, 15, 5)); return p; }
    private static JLabel header(String text) { JLabel l = new JLabel(text, SwingConstants.CENTER); l.setOpaque(true); l.setBackground(HEADER); l.setForeground(Color.WHITE); l.setFont(font(25)); l.setPreferredSize(new Dimension(0, 70)); l.setBorder(new LineBorder(new Color(45, 103, 130), 2, true)); return l; }
    private static JTextField field() { JTextField f = new JTextField(); f.setFont(new Font("Segoe UI", Font.PLAIN, 24)); f.setPreferredSize(new Dimension(700, 52)); f.setBorder(new EmptyBorder(4, 10, 4, 10)); return f; }
    private static JButton button(String text, Color color, int w, int h) { JButton b = new JButton(text); b.setFont(font(20)); b.setForeground(Color.WHITE); b.setBackground(color); b.setFocusPainted(false); b.setPreferredSize(new Dimension(w, h)); b.setBorder(new LineBorder(new Color(205, 230, 240), 2, true)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
    private static String safe(String value) { return value == null ? "" : value; }
}
