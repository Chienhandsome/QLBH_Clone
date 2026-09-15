package kazuko.ex.ui.touch;

import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.NhomNhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Full-screen touch supplier catalogue backed by NhaCungCapDAO. */
final class TouchNhaCungCapPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(79, 139, 169),
            TEAL = new Color(0, 79, 76), GREEN = new Color(0, 90, 20), RED = new Color(165, 10, 10),
            ORANGE = new Color(226, 79, 5), GRAY = new Color(145, 145, 145), PURPLE = new Color(90, 10, 80);
    private final NhaCungCapDAO dao = new NhaCungCapDAO();
    private final NhomNhaCungCapDAO groupDao = new NhomNhaCungCapDAO();
    private final Runnable exit;
    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private final JPanel cardGrid = new JPanel();
    private final JLabel detailTitle = new JLabel("NHÀ CUNG CẤP");
    private final JTextField code = field(), name = field(), address = field(), phone = field(),
            email = field(), group = field(), website = field(), note = field();
    private final JButton editButton = button("CHỈNH SỬA", GRAY), deleteButton = button("XÓA", GRAY);
    private final List<NhaCungCap> suppliers = new ArrayList<>();
    private TouchNhaCungCapColumnConfig config;
    private NhaCungCap selected, editing;
    private String searchText = "";

    TouchNhaCungCapPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout());
        screens.add(listScreen(), "list");
        screens.add(detailScreen(), "detail");
        add(screens);
        reload();
    }

    private JPanel listScreen() {
        JPanel root = base();
        JPanel top = new JPanel(new BorderLayout(10, 0)); top.setOpaque(false);
        JLabel catalogue = label("DANH MỤC NHÀ CUNG CẤP", RED, SwingConstants.CENTER, 20); catalogue.setPreferredSize(new Dimension(375, 58));
        top.add(catalogue, BorderLayout.WEST);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); toolbar.setOpaque(false);
        JButton add = button("THÊM MỚI", GREEN), refresh = button("REFRESH", GREEN), find = button("TÌM", GREEN);
        for (JButton b : new JButton[]{add, editButton, deleteButton, refresh, find}) { b.setPreferredSize(new Dimension(170, 58)); toolbar.add(b); }
        add.addActionListener(e -> beginAdd()); editButton.addActionListener(e -> { if (selected != null) beginEdit(selected); }); deleteButton.addActionListener(e -> deleteSelected()); refresh.addActionListener(e -> { searchText = ""; reload(); }); find.addActionListener(e -> search());
        top.add(toolbar); root.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 0)); center.setOpaque(false);
        JPanel sidebar = new JPanel(new BorderLayout()); sidebar.setBackground(HEADER); sidebar.setPreferredSize(new Dimension(375, 0)); center.add(sidebar, BorderLayout.WEST);
        cardGrid.setBackground(new Color(232, 232, 232));
        JScrollPane scroll = new JScrollPane(cardGrid, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(50); scroll.setBorder(new EmptyBorder(0, 0, 0, 0)); center.add(scroll);
        root.add(center);

        JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false); bottom.setBorder(new EmptyBorder(0, 380, 0, 0));
        JButton settings = button("", BG); settings.setIcon(new TouchMatHangPanel.GearIcon(38, Color.WHITE)); settings.setPreferredSize(new Dimension(70, 62)); settings.addActionListener(e -> new TouchNhaCungCapSettingsDialog(SwingUtilities.getWindowAncestor(this), this::reloadConfig).setVisible(true));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false); right.add(settings); JButton close = button("THOÁT", RED); close.setPreferredSize(new Dimension(225, 62)); close.addActionListener(e -> exit.run()); right.add(close); bottom.add(right, BorderLayout.EAST); root.add(bottom, BorderLayout.SOUTH);
        return root;
    }

    private JPanel detailScreen() {
        JPanel root = base();
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(new Color(105, 140, 25));
        detailTitle.setForeground(Color.WHITE); detailTitle.setFont(font(25)); detailTitle.setBorder(new EmptyBorder(0, 5, 0, 0)); top.add(detailTitle);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5)); actions.setOpaque(false);
        JButton previous = button("TRƯỚC", GREEN), next = button("SAU", GRAY), create = button("TẠO MỚI", GREEN), copy = button("SAO CHÉP", GRAY), remove = button("XÓA", GRAY);
        for (JButton b : new JButton[]{previous, next, create, copy, remove}) { b.setPreferredSize(new Dimension(145, 60)); actions.add(b); }
        previous.addActionListener(e -> offset(-1)); next.addActionListener(e -> offset(1)); create.addActionListener(e -> beginAdd()); copy.addActionListener(e -> copyCurrent()); remove.addActionListener(e -> deleteEditing()); top.add(actions, BorderLayout.EAST); root.add(top, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout()); body.setOpaque(false); GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(8, 12, 8, 8);
        addRow(body, c, 0, "MÃ NHÀ CUNG CẤP", code, false); addRow(body, c, 1, "TÊN NHÀ CUNG CẤP", name, false); addRow(body, c, 2, "ĐỊA CHỈ", address, false); addRow(body, c, 3, "ĐIỆN THOẠI", phone, false); addRow(body, c, 4, "EMAIL", email, false); addRow(body, c, 5, "NHÓM NHÀ CUNG CẤP", group, true); addRow(body, c, 6, "WEBSITE", website, false); addRow(body, c, 7, "GHI CHÚ", note, false); c.gridy = 8; c.weighty = 1; body.add(Box.createVerticalGlue(), c); root.add(body);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); footer.setOpaque(false); JButton save = button("LƯU", GRAY), saveNew = button("LƯU & MỚI", GRAY), saveClose = button("LƯU & THOÁT", GRAY), close = button("THOÁT", RED);
        for (JButton b : new JButton[]{save, saveNew, saveClose, close}) { b.setPreferredSize(new Dimension(190, 68)); footer.add(b); }
        save.addActionListener(e -> persist(0)); saveNew.addActionListener(e -> persist(1)); saveClose.addActionListener(e -> persist(2)); close.addActionListener(e -> { reload(); navigation.show(screens, "list"); }); root.add(footer, BorderLayout.SOUTH); return root;
    }

    private void addRow(JPanel panel, GridBagConstraints c, int y, String title, JTextField input, boolean groupPicker) {
        JLabel l = new JLabel(title); l.setForeground(Color.WHITE); l.setFont(font(17)); l.setPreferredSize(new Dimension(245, 48)); c.gridx = 0; c.gridy = y; c.weightx = 0; panel.add(l, c);
        c.gridx = 1; c.weightx = 1; panel.add(input, c);
        JButton action = button(groupPicker ? "⌕" : "⌨", TEAL); action.setFont(new Font("Segoe UI Symbol", Font.BOLD, groupPicker ? 36 : 27)); action.setPreferredSize(new Dimension(72, 52));
        if (groupPicker) action.addActionListener(e -> chooseGroup()); else action.addActionListener(e -> openKeyboard(input, title)); c.gridx = 2; c.weightx = 0; panel.add(action, c);
    }

    private void reloadConfig() { config = TouchNhaCungCapConfigDAO.load(); renderCards(); }
    private void reload() {
        suppliers.clear(); suppliers.addAll(dao.getAll()); selected = null; config = TouchNhaCungCapConfigDAO.load(); renderCards(); updateActions();
    }

    private void renderCards() {
        if (config == null) config = TouchNhaCungCapConfigDAO.load(); cardGrid.removeAll(); cardGrid.setLayout(new GridLayout(0, config.getGridColumns(), 7, 7)); cardGrid.setBorder(new EmptyBorder(7, 7, 7, 7));
        String q = searchText.toLowerCase(Locale.ROOT);
        int height = Math.max(95, 790 / config.getGridRows());
        for (NhaCungCap supplier : suppliers) {
            if (!q.isBlank() && !(safe(supplier.getName()) + " " + safe(supplier.getMaNhaCungCap()) + " " + safe(supplier.getDienThoai())).toLowerCase(Locale.ROOT).contains(q)) continue;
            cardGrid.add(supplierCard(supplier, height));
        }
        cardGrid.revalidate(); cardGrid.repaint();
    }

    private JPanel supplierCard(NhaCungCap supplier, int height) {
        JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBackground(supplier == selected ? ORANGE : config.getCardBgColor()); card.setBorder(new LineBorder(new Color(220, 235, 235), 2, true)); card.setPreferredSize(new Dimension(400, height)); card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        for (var item : config.getUsedColumns()) { String text = TouchNhaCungCapColumnConfig.value(supplier, item, config.isShowTitle()); JLabel l = new JLabel(text, item.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.LEFT); l.setForeground(item.getColor()); l.setFont(item.getFont()); l.setBorder(new EmptyBorder(4, 12, 4, 12)); l.setAlignmentX(Component.LEFT_ALIGNMENT); l.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(35, height / Math.max(1, config.getUsedColumns().size())))); card.add(l); }
        var listener = new java.awt.event.MouseAdapter() { @Override public void mousePressed(java.awt.event.MouseEvent e) { selected = supplier; renderCards(); updateActions(); } @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) beginEdit(supplier); } };
        card.addMouseListener(listener); for (Component child : card.getComponents()) child.addMouseListener(listener); return card;
    }

    private void updateActions() { boolean enabled = selected != null; editButton.setBackground(enabled ? GREEN : GRAY); deleteButton.setBackground(enabled ? RED : GRAY); }
    private void search() { String value = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), "TÌM NHÀ CUNG CẤP", searchText); if (value != null) { searchText = value.trim(); renderCards(); } }

    private void beginAdd() { editing = new NhaCungCap(); clearFields(); detailTitle.setText("NHÀ CUNG CẤP"); navigation.show(screens, "detail"); }
    private void beginEdit(NhaCungCap supplier) { editing = supplier; fillFields(supplier); detailTitle.setText("NHÀ CUNG CẤP"); navigation.show(screens, "detail"); }
    private void fillFields(NhaCungCap s) { code.setText(safe(s.getMaNhaCungCap())); name.setText(safe(s.getName())); address.setText(safe(s.getDiaChi())); phone.setText(safe(s.getDienThoai())); email.setText(safe(s.getEmail())); group.setText(safe(s.getTenNhomNhaCungCap())); group.putClientProperty("groupId", s.getNhomNhaCungCapId()); website.setText(safe(s.getWebsite())); note.setText(safe(s.getNote())); }
    private void clearFields() { for (JTextField f : new JTextField[]{code, name, address, phone, email, group, website, note}) f.setText(""); group.putClientProperty("groupId", null); }
    private void openKeyboard(JTextField input, String title) { String value = TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this), title, input.getText()); if (value != null) input.setText(value); }

    private void chooseGroup() {
        List<NhomNhaCungCap> groups = groupDao.getAll(); Object[] choices = new Object[groups.size() + 1]; choices[0] = "(Không thiết lập)"; for (int i = 0; i < groups.size(); i++) choices[i + 1] = groups.get(i);
        Object value = JOptionPane.showInputDialog(this, "Chọn nhóm nhà cung cấp", "NHÓM NHÀ CUNG CẤP", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]); if (value == null) return;
        if (value instanceof NhomNhaCungCap x) { group.setText(safe(x.getName())); group.putClientProperty("groupId", x.getId()); } else { group.setText(""); group.putClientProperty("groupId", ""); }
    }

    private boolean persist(int then) {
        if (name.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhà cung cấp!", "THÔNG BÁO", JOptionPane.WARNING_MESSAGE); return false; }
        if (editing == null) editing = new NhaCungCap(); boolean isNew = editing.getId() == null || editing.getId().isBlank();
        if (code.getText().trim().isEmpty()) code.setText("NCC" + (System.currentTimeMillis() % 1_000_000));
        editing.setMaNhaCungCap(code.getText().trim()); editing.setName(name.getText().trim()); editing.setDiaChi(address.getText().trim()); editing.setDienThoai(phone.getText().trim()); editing.setEmail(email.getText().trim()); editing.setWebsite(website.getText().trim()); editing.setNote(note.getText().trim());
        Object id = group.getClientProperty("groupId"); editing.setNhomNhaCungCapId(id == null ? editing.getNhomNhaCungCapId() : (id.toString().isBlank() ? null : id.toString())); editing.setTenNhomNhaCungCap(group.getText().trim());
        boolean ok = isNew ? dao.insert(editing) : dao.update(editing); if (!ok) { JOptionPane.showMessageDialog(this, "Không thể cập nhật nhà cung cấp!", "LỖI", JOptionPane.ERROR_MESSAGE); return false; }
        if (then == 1) beginAdd(); else { reload(); if (then == 2) navigation.show(screens, "list"); else { NhaCungCap saved = dao.getById(editing.getId()); if (saved != null) beginEdit(saved); } } return true;
    }

    private void offset(int amount) { if (editing == null || editing.getId() == null) return; NhaCungCap target = dao.getOffset(editing.getId(), amount); if (target != null) beginEdit(target); }
    private void copyCurrent() { if (editing == null) return; NhaCungCap copy = new NhaCungCap(); copy.setName(name.getText().trim()); copy.setDiaChi(address.getText().trim()); copy.setDienThoai(phone.getText().trim()); copy.setEmail(email.getText().trim()); copy.setWebsite(website.getText().trim()); copy.setNote(note.getText().trim()); copy.setNhomNhaCungCapId(editing.getNhomNhaCungCapId()); copy.setTenNhomNhaCungCap(group.getText().trim()); editing = copy; code.setText(""); detailTitle.setText("SAO CHÉP NHÀ CUNG CẤP"); }
    private void deleteEditing() { if (editing == null || editing.getId() == null) return; selected = editing; if (deleteSelected()) navigation.show(screens, "list"); }
    private boolean deleteSelected() { if (selected == null) return false; if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "BẠN CÓ MUỐN XÓA NHÀ CUNG CẤP<br>" + safe(selected.getName()).toUpperCase() + " KHÔNG?")) return false; boolean ok = dao.delete(selected.getId()); if (!ok) JOptionPane.showMessageDialog(this, "Không thể xóa nhà cung cấp!", "LỖI", JOptionPane.ERROR_MESSAGE); else reload(); return ok; }

    private static JPanel base() { JPanel p = new JPanel(new BorderLayout(0, 8)); p.setBackground(BG); p.setBorder(new EmptyBorder(7, 7, 7, 7)); return p; }
    private static JLabel label(String text, Color bg, int align, int size) { JLabel l = new JLabel(text, align); l.setOpaque(true); l.setBackground(bg); l.setForeground(Color.WHITE); l.setFont(font(size)); l.setBorder(new LineBorder(new Color(210, 230, 235), 2, true)); return l; }
    private static JTextField field() { JTextField f = new JTextField(); f.setFont(new Font("Segoe UI", Font.PLAIN, 23)); f.setPreferredSize(new Dimension(700, 50)); f.setBorder(new EmptyBorder(4, 8, 4, 8)); return f; }
    private static JButton button(String text, Color bg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFont(font(18)); b.setFocusPainted(false); b.setBorder(new LineBorder(new Color(210, 230, 235), 2, true)); return b; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
    private static String safe(String value) { return value == null ? "" : value; }
}
