package kazuko.ex.ui.touch;

import kazuko.ex.dao.CaLamViecDAO;
import kazuko.ex.model.danhmuc.CaLamViec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Màn hình cảm ứng DANH MỤC CA LÀM VIỆC (TouchCaLamViecPanel).
 * Chuẩn hóa giao diện theo thiết kế thực tế:
 * - Dòng ca làm việc dạng thanh ngang màu xanh lá nổi bật
 * - Bên phải mỗi dòng có 2 nút: [XÓA] và [SỬA] (không có nút Thêm trên dòng)
 * - Chân trang có nút [THÊM] bên trái và [THOÁT] bên phải
 * - Tích hợp màn hình chỉnh sửa và bàn phím ảo cảm ứng.
 */
public final class TouchCaLamViecPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 120, 0);
    private static final Color BRIGHT_GREEN = new Color(0, 130, 32);
    private static final Color BLUE = new Color(5, 5, 145);
    private static final Color RED = new Color(155, 0, 0);

    private final CaLamViecDAO dao = new CaLamViecDAO();
    private final Runnable exit;
    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);

    private final JTextField nameField = textField();
    private final JTextField tiLeLuongField = textField();
    private final JTextField noteField = textField();

    private final StringBuilder keyboardBuffer = new StringBuilder();
    private final JLabel keyboardDisplay = new JLabel("", SwingConstants.CENTER);
    private JTextField keyboardTarget;

    private JPanel listScreen;
    private CaLamViec editingShift;
    private JLabel editHeader;

    public TouchCaLamViecPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout());
        screens.add(createEditScreen(), "edit");
        screens.add(createKeyboardScreen(), "keyboard");
        add(screens, BorderLayout.CENTER);
        showList();
    }

    private void showList() {
        if (listScreen != null) screens.remove(listScreen);
        listScreen = createListScreen();
        screens.add(listScreen, "list");
        navigation.show(screens, "list");
        screens.revalidate();
        screens.repaint();
    }

    private JPanel createListScreen() {
        JPanel root = basePanel();
        root.add(header("DANH MỤC CA LÀM VIỆC"), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setBackground(new Color(235, 235, 235));
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(6, 6, 6, 6));

        List<CaLamViec> shifts = dao.getAllActive();
        for (CaLamViec ca : shifts) {
            if (ca.isFolder() || ca.isSeparator()) continue;
            rows.add(createShiftRow(ca));
        }
        rows.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(rows,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        scroll.getVerticalScrollBar().setUnitIncrement(45);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 5, 0, 5));

        JButton add = actionButton("THÊM", BRIGHT_GREEN, 200, 72);
        add.addActionListener(e -> beginAdd());
        footer.add(add, BorderLayout.WEST);

        JButton close = actionButton("THOÁT", RED, 200, 72);
        close.addActionListener(e -> {
            if (exit != null) exit.run();
        });
        footer.add(close, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel createShiftRow(CaLamViec ca) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 7, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setPreferredSize(new Dimension(0, 68));

        // Thanh tên ca làm việc màu xanh lá
        JLabel name = new JLabel(ca.getName() != null ? ca.getName() : "");
        name.setOpaque(true);
        name.setBackground(GREEN);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 23));
        name.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        row.add(name, BorderLayout.CENTER);

        // 2 nút chức năng bên phải: [XÓA] và [SỬA] (không có nút Thêm trên dòng)
        JPanel actions = new JPanel(new GridLayout(1, 2, 6, 0));
        actions.setOpaque(false);

        JButton delete = actionButton("XÓA", RED, 105, 66);
        delete.addActionListener(e -> deleteShift(ca));

        JButton edit = actionButton("SỬA", BLUE, 105, 66);
        edit.addActionListener(e -> beginEdit(ca));

        actions.add(delete);
        actions.add(edit);
        row.add(actions, BorderLayout.EAST);

        return row;
    }

    private JPanel createEditScreen() {
        JPanel root = basePanel();
        editHeader = header("THÊM MỚI CA LÀM VIỆC");
        root.add(editHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addEditRow(body, gbc, 0, "TÊN CA LÀM VIỆC:", nameField);
        addEditRow(body, gbc, 1, "TỈ LỆ LƯƠNG (%):", tiLeLuongField);
        addEditRow(body, gbc, 2, "GHI CHÚ:", noteField);

        gbc.gridy = 3;
        gbc.weighty = 1;
        body.add(Box.createVerticalGlue(), gbc);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 0, 0, 5));

        JButton save = actionButton("CẬP NHẬT", new Color(140, 140, 140), 280, 80);
        save.addActionListener(e -> saveShift());

        JButton close = actionButton("THOÁT", RED, 220, 80);
        close.addActionListener(e -> showList());

        footer.add(save);
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private void addEditRow(JPanel body, GridBagConstraints gbc, int row,
                            String title, JTextField field) {
        JLabel label = new JLabel(title);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 19));
        label.setPreferredSize(new Dimension(300, 52));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        body.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        body.add(field, gbc);

        JButton keyboard = actionButton("⌨", new Color(0, 85, 90), 72, 58);
        keyboard.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));
        keyboard.addActionListener(e -> showKeyboard(field));
        gbc.gridx = 2;
        gbc.weightx = 0;
        body.add(keyboard, gbc);
    }

    private JPanel createKeyboardScreen() {
        JPanel root = basePanel();
        root.add(header("MỜI BẠN NHẬP THÔNG TIN"), BorderLayout.NORTH);

        keyboardDisplay.setOpaque(true);
        keyboardDisplay.setBackground(Color.WHITE);
        keyboardDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 34));
        keyboardDisplay.setPreferredSize(new Dimension(0, 120));
        keyboardDisplay.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(keyboardDisplay, BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        addKeyboardRow(rows, "1234567890-=⌫");
        addKeyboardRow(rows, "QWERTYUIOP\\@");
        addKeyboardRow(rows, "ASDFGHJKL;'");
        addKeyboardRow(rows, "ZXCVBNM,.");
        center.add(rows, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        JButton cancel = actionButton("HỦY BỎ", RED, 240, 85);
        cancel.addActionListener(e -> navigation.show(screens, "edit"));
        JButton space = actionButton("", Color.DARK_GRAY, 600, 85);
        space.addActionListener(e -> appendKey(" "));
        JButton accept = actionButton("➜", GREEN, 240, 85);
        accept.setFont(new Font("Segoe UI Symbol", Font.BOLD, 48));
        accept.addActionListener(e -> acceptKeyboard());
        actions.add(cancel);
        actions.add(space);
        actions.add(accept);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void addKeyboardRow(JPanel rows, String keys) {
        JPanel row = new JPanel(new GridLayout(1, keys.length(), 6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        for (int i = 0; i < keys.length(); i++) {
            String key = String.valueOf(keys.charAt(i));
            JButton button = actionButton(key, new Color(0, 105, 130), 80, 85);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 23));
            button.addActionListener(e -> {
                if ("⌫".equals(key)) backspaceKey();
                else appendKey(key);
            });
            row.add(button);
        }
        rows.add(row);
        rows.add(Box.createVerticalStrut(6));
    }

    private void beginAdd() {
        editingShift = new CaLamViec();
        nameField.setText("");
        tiLeLuongField.setText("100");
        noteField.setText("");
        editHeader.setText("THÊM MỚI CA LÀM VIỆC");
        navigation.show(screens, "edit");
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private void beginEdit(CaLamViec ca) {
        editingShift = ca;
        nameField.setText(ca.getName() != null ? ca.getName() : "");
        DecimalFormat df = new DecimalFormat("#.##");
        tiLeLuongField.setText(df.format(ca.getTiLeLuong() > 0 ? ca.getTiLeLuong() : 100.0));
        noteField.setText(ca.getGhiChu() != null ? ca.getGhiChu() : "");
        editHeader.setText("CHỈNH SỬA CA LÀM VIỆC");
        navigation.show(screens, "edit");
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private void saveShift() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên ca làm việc!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double tiLe = 100.0;
        try {
            tiLe = Double.parseDouble(tiLeLuongField.getText().trim().replace(",", "."));
            if (tiLe <= 0) tiLe = 100.0;
        } catch (Exception ignored) {
            tiLe = 100.0;
        }

        boolean isNew = editingShift == null || editingShift.getId() == null || editingShift.getId().isBlank();
        if (editingShift == null) editingShift = new CaLamViec();
        editingShift.setName(name);
        editingShift.setTiLeLuong(tiLe);
        editingShift.setGhiChu(noteField.getText().trim());

        boolean saved = isNew ? dao.insert(editingShift) : dao.update(editingShift);
        if (!saved) {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật ca làm việc!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                (isNew ? "Thêm mới ca làm việc [" : "Cập nhật ca làm việc [") + name + "] thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        showList();
    }

    private void deleteShift(CaLamViec ca) {
        if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this),
                "BẠN CÓ MUỐN XÓA CA LÀM VIỆC<br>" + (ca.getName() != null ? ca.getName().toUpperCase() : "") + " KHÔNG?")) {
            return;
        }
        dao.moveToTrash(ca.getId());
        showList();
    }

    private void showKeyboard(JTextField target) {
        keyboardTarget = target;
        keyboardBuffer.setLength(0);
        keyboardBuffer.append(target.getText());
        keyboardDisplay.setText(keyboardBuffer.toString());
        navigation.show(screens, "keyboard");
    }

    private void appendKey(String key) {
        keyboardBuffer.append(key);
        keyboardDisplay.setText(keyboardBuffer.toString());
    }

    private void backspaceKey() {
        if (!keyboardBuffer.isEmpty()) keyboardBuffer.deleteCharAt(keyboardBuffer.length() - 1);
        keyboardDisplay.setText(keyboardBuffer.toString());
    }

    private void acceptKeyboard() {
        if (keyboardTarget != null) keyboardTarget.setText(keyboardBuffer.toString());
        navigation.show(screens, "edit");
    }

    private JPanel basePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(5, 5, 12, 5));
        return panel;
    }

    private JLabel header(String title) {
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(HEADER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 25));
        label.setPreferredSize(new Dimension(0, 68));
        label.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));
        return label;
    }

    private static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        field.setPreferredSize(new Dimension(650, 52));
        field.setBorder(new EmptyBorder(4, 10, 4, 10));
        return field;
    }

    private JButton actionButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new LineBorder(new Color(205, 230, 240), 2, true));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Package-private helpers for automated tests
    CaLamViecDAO getDao() {
        return dao;
    }

    void testBeginAdd() {
        beginAdd();
    }

    void testBeginEdit(CaLamViec ca) {
        beginEdit(ca);
    }

    String getEditHeaderText() {
        return editHeader != null ? editHeader.getText() : "";
    }

    JTextField getNameField() {
        return nameField;
    }

    JTextField getTiLeLuongField() {
        return tiLeLuongField;
    }
}
