package kazuko.ex.ui.touch;

import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.model.danhmuc.LyDoThuChi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/** Danh mục lý do thu chi dành cho giao diện cảm ứng. */
final class TouchLyDoThuChiPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 120, 0);
    private static final Color BLUE = new Color(5, 5, 145);
    private static final Color RED = new Color(155, 0, 0);

    private final LyDoThuChiDAO dao = new LyDoThuChiDAO();
    private final Runnable exit;
    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private final JTextField nameField = textField();
    private final JTextField noteField = textField();
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "", "Tiền mặt", "Ngân hàng", "Công nợ", "Nội bộ", "Khác"
    });
    private final JCheckBox incomeCheck = typeCheckBox("LÀ LÝ DO THU");
    private final JCheckBox expenseCheck = typeCheckBox("LÀ LÝ DO CHI");
    private final StringBuilder keyboardBuffer = new StringBuilder();
    private final JLabel keyboardDisplay = new JLabel("", SwingConstants.CENTER);

    private JTextField keyboardTarget;
    private JPanel listScreen;
    private LyDoThuChi editingReason;
    private JLabel editHeader;

    TouchLyDoThuChiPanel(Runnable exit) {
        this.exit = exit;
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        typeCombo.setBackground(Color.WHITE);
        typeCombo.setPreferredSize(new Dimension(700, 52));
        incomeCheck.setSelected(true);

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
        root.add(header("DANH MỤC LÝ DO THU CHI"), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setBackground(new Color(235, 235, 235));
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(6, 6, 6, 6));

        List<LyDoThuChi> reasons = dao.getAllActive();
        for (LyDoThuChi reason : reasons) {
            if (!reason.isFolder() && !reason.isSeparator()) {
                rows.add(createReasonRow(reason));
            }
        }
        rows.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(
                rows,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        scroll.getVerticalScrollBar().setUnitIncrement(45);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 5, 0, 5));

        JButton add = actionButton("THÊM", GREEN, 225, 78);
        add.addActionListener(e -> beginAdd());
        footer.add(add, BorderLayout.WEST);

        JButton close = actionButton("THOÁT", RED, 225, 78);
        close.addActionListener(e -> exit.run());
        footer.add(close, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel createReasonRow(LyDoThuChi reason) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 7, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        row.setPreferredSize(new Dimension(0, 108));

        JLabel name = new JLabel(reason.getName() != null ? reason.getName() : "");
        name.setOpaque(true);
        name.setBackground(GREEN);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 24));
        name.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.add(name, BorderLayout.CENTER);

        // Mỗi item chỉ có XÓA và SỬA. THÊM là hành động chung ở chân màn hình.
        JPanel actions = new JPanel(new GridLayout(1, 2, 6, 0));
        actions.setOpaque(false);
        JButton delete = actionButton("XÓA", RED, 110, 101);
        delete.addActionListener(e -> deleteReason(reason));
        JButton edit = actionButton("SỬA", BLUE, 110, 101);
        edit.addActionListener(e -> beginEdit(reason));
        actions.add(delete);
        actions.add(edit);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JPanel createEditScreen() {
        JPanel root = basePanel();
        editHeader = header("THÊM MỚI LÝ DO THU CHI");
        root.add(editHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addEditRow(body, gbc, 0, "TÊN LÝ DO", nameField, true);
        addEditRow(body, gbc, 1, "LOẠI", typeCombo, false);

        JLabel classificationLabel = editLabel("PHÂN LOẠI");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        body.add(classificationLabel, gbc);

        JPanel classification = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 0));
        classification.setOpaque(false);
        classification.add(incomeCheck);
        classification.add(expenseCheck);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        body.add(classification, gbc);
        gbc.gridwidth = 1;

        addEditRow(body, gbc, 3, "GHI CHÚ", noteField, true);
        gbc.gridy = 4;
        gbc.weighty = 1;
        body.add(Box.createVerticalGlue(), gbc);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 0, 0, 5));
        JButton save = actionButton("CẬP NHẬT", new Color(140, 140, 140), 355, 95);
        save.addActionListener(e -> saveReason());
        JButton close = actionButton("THOÁT", RED, 290, 95);
        close.addActionListener(e -> showList());
        footer.add(save);
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private void addEditRow(JPanel body, GridBagConstraints gbc, int row,
                            String title, JComponent field, boolean showKeyboard) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        body.add(editLabel(title), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        body.add(field, gbc);

        if (showKeyboard && field instanceof JTextField textField) {
            JButton keyboard = actionButton("⌨", new Color(0, 85, 90), 72, 72);
            keyboard.setFont(new Font("Segoe UI Symbol", Font.BOLD, 29));
            keyboard.addActionListener(e -> showKeyboard(textField));
            gbc.gridx = 2;
            gbc.weightx = 0;
            body.add(keyboard, gbc);
        }
    }

    private JPanel createKeyboardScreen() {
        JPanel root = basePanel();
        root.add(header("MỜI BẠN NHẬP THÔNG TIN"), BorderLayout.NORTH);
        keyboardDisplay.setOpaque(true);
        keyboardDisplay.setBackground(Color.WHITE);
        keyboardDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        keyboardDisplay.setPreferredSize(new Dimension(0, 150));

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
        JButton cancel = actionButton("HỦY BỎ", RED, 285, 100);
        cancel.addActionListener(e -> navigation.show(screens, "edit"));
        JButton space = actionButton("", Color.DARK_GRAY, 700, 100);
        space.addActionListener(e -> appendKey(" "));
        JButton accept = actionButton("➜", GREEN, 285, 100);
        accept.setFont(new Font("Segoe UI Symbol", Font.BOLD, 54));
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
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        for (int i = 0; i < keys.length(); i++) {
            String key = String.valueOf(keys.charAt(i));
            JButton button = actionButton(key, new Color(0, 105, 130), 90, 100);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 25));
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
        editingReason = new LyDoThuChi();
        editingReason.setLaLyDoThu(true);
        editingReason.setLaLyDoChi(false);
        editingReason.setAvatarIcon("money_dollar");
        nameField.setText("");
        noteField.setText("");
        typeCombo.setSelectedIndex(0);
        incomeCheck.setSelected(true);
        expenseCheck.setSelected(false);
        editHeader.setText("THÊM MỚI LÝ DO THU CHI");
        navigation.show(screens, "edit");
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private void beginEdit(LyDoThuChi reason) {
        editingReason = reason;
        nameField.setText(reason.getName() != null ? reason.getName() : "");
        noteField.setText(reason.getGhiChu() != null ? reason.getGhiChu() : "");
        typeCombo.setSelectedItem(reason.getLoai() != null ? reason.getLoai() : "");
        incomeCheck.setSelected(reason.isLaLyDoThu());
        expenseCheck.setSelected(reason.isLaLyDoChi());
        editHeader.setText("CHỈNH SỬA LÝ DO THU CHI");
        navigation.show(screens, "edit");
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private void saveReason() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên lý do thu chi!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!incomeCheck.isSelected() && !expenseCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lý do thu hoặc lý do chi!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isNew = editingReason == null
                || editingReason.getId() == null
                || editingReason.getId().isBlank();
        if (editingReason == null) editingReason = new LyDoThuChi();
        editingReason.setName(name);
        editingReason.setLoai(typeCombo.getSelectedItem() != null
                ? typeCombo.getSelectedItem().toString() : "");
        editingReason.setLaLyDoThu(incomeCheck.isSelected());
        editingReason.setLaLyDoChi(expenseCheck.isSelected());
        editingReason.setGhiChu(noteField.getText().trim());
        if (editingReason.getAvatarIcon() == null || editingReason.getAvatarIcon().isBlank()) {
            editingReason.setAvatarIcon("money_dollar");
        }

        boolean saved = isNew ? dao.insert(editingReason) : dao.update(editingReason);
        if (!saved) {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật lý do thu chi!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showList();
    }

    private void deleteReason(LyDoThuChi reason) {
        String reasonName = reason.getName() != null ? reason.getName() : "";
        if (!TouchConfirmDialog.show(
                SwingUtilities.getWindowAncestor(this),
                "BẠN CÓ MUỐN XÓA LÝ DO THU CHI<br>" + reasonName.toUpperCase() + " KHÔNG?"
        )) return;

        if (reason.getId() != null && reason.getId().equals(dao.getDefaultReasonId())) {
            dao.setDefaultReasonId(null);
        }
        if (!dao.moveToTrash(reason.getId())) {
            JOptionPane.showMessageDialog(this, "Không thể xóa lý do thu chi!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
        if (!keyboardBuffer.isEmpty()) {
            keyboardBuffer.deleteCharAt(keyboardBuffer.length() - 1);
        }
        keyboardDisplay.setText(keyboardBuffer.toString());
    }

    private void acceptKeyboard() {
        if (keyboardTarget != null) keyboardTarget.setText(keyboardBuffer.toString());
        navigation.show(screens, "edit");
    }

    private JPanel basePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(5, 5, 15, 5));
        return panel;
    }

    private JLabel header(String title) {
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(HEADER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 25));
        label.setPreferredSize(new Dimension(0, 70));
        label.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));
        return label;
    }

    private static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        field.setPreferredSize(new Dimension(700, 52));
        field.setBorder(new EmptyBorder(4, 10, 4, 10));
        return field;
    }

    private static JCheckBox typeCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setOpaque(false);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Segoe UI", Font.BOLD, 20));
        checkBox.setFocusPainted(false);
        return checkBox;
    }

    private JLabel editLabel(String title) {
        JLabel label = new JLabel(title);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 19));
        label.setPreferredSize(new Dimension(450, 52));
        return label;
    }

    private JButton actionButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new LineBorder(new Color(205, 230, 240), 2, true));
        return button;
    }
}
