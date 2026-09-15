package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Hộp thoại bàn phím số cảm ứng (Touch Numeric Keypad) theo chuẩn Hình giao diện.
 * - Hàng 1: 1, 2, 3
 * - Hàng 2: 4, 5, 6
 * - Hàng 3: 7, 8, 9
 * - Hàng 4: 0, . (chấm thập phân), [▶] (Xác nhận)
 * - Hàng 5: 000, HỦY BỎ
 */
public class TouchNumberInputDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER_BG = new Color(138, 58, 4); // Nâu cam đậm
    private static final Color KEY_BG = new Color(0, 67, 86);
    private static final Color BACKSPACE_BG = new Color(226, 79, 5); // Cam
    private static final Color CONFIRM_BG = new Color(0, 85, 0); // Xanh lá đậm
    private static final Color CANCEL_BG = new Color(155, 0, 0); // Đỏ
    private static final Color BORDER_COLOR = new Color(190, 210, 210);

    private final JLabel displayLabel = new JLabel("0", SwingConstants.CENTER);
    private String currentValue = "0";
    private boolean confirmed = false;
    private int result = 0;
    private double resultDouble = 0.0;

    private double minDouble = 0;
    private double maxDouble = 999999999;

    public TouchNumberInputDialog(Window owner, String title, double initialValue, double min, double max) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.minDouble = min;
        this.maxDouble = max;

        if (initialValue == (long) initialValue) {
            this.currentValue = String.valueOf((long) initialValue);
        } else {
            this.currentValue = String.valueOf(initialValue);
        }

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER_BG, 3));

        // 1. Tiêu đề (Header)
        JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(HEADER_BG);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setPreferredSize(new Dimension(0, 44));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Thân chính gồm Display + Bàn phím số
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(6, 10, 10, 10));

        // Hàng hiển thị giá trị & nút Backspace (◀)
        JPanel displayRow = new JPanel(new BorderLayout(8, 0));
        displayRow.setOpaque(false);
        displayRow.setPreferredSize(new Dimension(0, 58));

        displayLabel.setOpaque(true);
        displayLabel.setBackground(Color.WHITE);
        displayLabel.setForeground(Color.BLACK);
        displayLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        displayLabel.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        displayLabel.setText(currentValue);
        displayRow.add(displayLabel, BorderLayout.CENTER);

        JButton btnBackspace = new JButton();
        btnBackspace.setBackground(BACKSPACE_BG);
        btnBackspace.setFocusPainted(false);
        btnBackspace.setPreferredSize(new Dimension(95, 58));
        btnBackspace.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        btnBackspace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBackspace.setIcon(new ArrowIcon(ArrowIcon.Direction.LEFT, 28, Color.WHITE));
        btnBackspace.addActionListener(e -> backspace());
        displayRow.add(btnBackspace, BorderLayout.EAST);

        body.add(displayRow, BorderLayout.NORTH);

        // Lưới bàn phím số
        JPanel keyGrid = new JPanel(new GridBagLayout());
        keyGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Hàng 1: 1, 2, 3
        gbc.gridy = 0;
        gbc.gridx = 0; keyGrid.add(createNumButton("1"), gbc);
        gbc.gridx = 1; keyGrid.add(createNumButton("2"), gbc);
        gbc.gridx = 2; keyGrid.add(createNumButton("3"), gbc);

        // Hàng 2: 4, 5, 6
        gbc.gridy = 1;
        gbc.gridx = 0; keyGrid.add(createNumButton("4"), gbc);
        gbc.gridx = 1; keyGrid.add(createNumButton("5"), gbc);
        gbc.gridx = 2; keyGrid.add(createNumButton("6"), gbc);

        // Hàng 3: 7, 8, 9
        gbc.gridy = 2;
        gbc.gridx = 0; keyGrid.add(createNumButton("7"), gbc);
        gbc.gridx = 1; keyGrid.add(createNumButton("8"), gbc);
        gbc.gridx = 2; keyGrid.add(createNumButton("9"), gbc);

        // Hàng 4: [0], [.], và nút xác nhận [▶]
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.gridwidth = 1;
        keyGrid.add(createNumButton("0"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 1;
        keyGrid.add(createNumButton("."), gbc);

        JButton btnConfirm = new JButton();
        btnConfirm.setBackground(CONFIRM_BG);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        btnConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirm.setIcon(new ArrowIcon(ArrowIcon.Direction.RIGHT, 32, Color.WHITE));
        btnConfirm.addActionListener(e -> {
            try {
                double dVal = Double.parseDouble(currentValue);
                if (dVal < minDouble) dVal = minDouble;
                if (maxDouble > 0 && dVal > maxDouble) dVal = maxDouble;
                resultDouble = dVal;
                result = (int) Math.round(dVal);
                confirmed = true;
                dispose();
            } catch (Exception ex) {
                resultDouble = minDouble;
                result = (int) Math.round(minDouble);
                confirmed = true;
                dispose();
            }
        });
        gbc.gridx = 2; gbc.gridwidth = 1;
        keyGrid.add(btnConfirm, gbc);

        // Hàng 5: [000] và nút [HỦY BỎ]
        gbc.gridy = 4;
        gbc.gridx = 0; gbc.gridwidth = 1;
        keyGrid.add(createNumButton("000"), gbc);

        JButton btnCancel = new JButton("HỦY BỎ");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(CANCEL_BG);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        gbc.gridx = 1; gbc.gridwidth = 2;
        keyGrid.add(btnCancel, gbc);

        body.add(keyGrid, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        setContentPane(root);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setSize(340, 480);
        setLocationRelativeTo(owner);
    }

    public TouchNumberInputDialog(Window owner, String title, int initialValue, int min, int max) {
        this(owner, title, (double) initialValue, (double) min, (double) max);
    }

    private JButton createNumButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, "000".equals(text) ? 24 : 32));
        btn.setForeground(Color.WHITE);
        btn.setBackground(KEY_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> appendInput(text));
        return btn;
    }

    private void appendInput(String str) {
        if (".".equals(str)) {
            if (!currentValue.contains(".")) {
                if (currentValue.isEmpty() || "0".equals(currentValue)) {
                    currentValue = "0.";
                } else {
                    currentValue += ".";
                }
            }
        } else if ("000".equals(str)) {
            if ("0".equals(currentValue) || currentValue.isEmpty()) {
                currentValue = "0";
            } else if (currentValue.length() < 12) {
                currentValue += "000";
            }
        } else {
            if ("0".equals(currentValue) || currentValue.isEmpty()) {
                currentValue = str;
            } else {
                if (currentValue.length() < 12) {
                    currentValue += str;
                }
            }
        }
        displayLabel.setText(currentValue);
    }

    private void backspace() {
        if (currentValue.length() > 1) {
            currentValue = currentValue.substring(0, currentValue.length() - 1);
        } else {
            currentValue = "0";
        }
        displayLabel.setText(currentValue);
    }

    public static Integer show(Window owner, String title, int initialValue, int min, int max) {
        TouchNumberInputDialog dialog = new TouchNumberInputDialog(owner, title, initialValue, min, max);
        dialog.setVisible(true);
        if (dialog.confirmed) {
            return dialog.result;
        }
        return null;
    }

    public static Integer showDialog(Window owner, String title, int initialValue) {
        return show(owner, title, initialValue, 0, 999999);
    }

    public static Integer showDialog(Window owner, String title, int initialValue, int min, int max) {
        return show(owner, title, initialValue, min, max);
    }

    public static Double showDoubleDialog(Window owner, String title, double initialValue, double min, double max) {
        TouchNumberInputDialog dialog = new TouchNumberInputDialog(owner, title, initialValue, min, max);
        dialog.setVisible(true);
        if (dialog.confirmed) {
            return dialog.resultDouble;
        }
        return null;
    }

    public static Double showDoubleDialog(Window owner, String title, double initialValue) {
        return showDoubleDialog(owner, title, initialValue, 0.0, 999999999.0);
    }

    /** Vẽ mũi tên tam giác cho nút Backspace (LEFT) và Confirm (RIGHT). */
    static class ArrowIcon implements Icon {
        enum Direction { LEFT, RIGHT }
        private final Direction direction;
        private final int size;
        private final Color color;

        ArrowIcon(Direction direction, int size, Color color) {
            this.direction = direction;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            int cx = x + (getIconWidth() - size) / 2;
            int cy = y + (getIconHeight() - size) / 2;

            int[] xPoints;
            int[] yPoints;

            if (direction == Direction.LEFT) {
                // Trỏ sang trái: (cx, cy + size/2), (cx + size, cy), (cx + size, cy + size)
                xPoints = new int[]{cx + size, cx, cx + size};
                yPoints = new int[]{cy, cy + size / 2, cy + size};
            } else {
                // Trỏ sang phải: (cx, cy), (cx + size, cy + size/2), (cx, cy + size)
                xPoints = new int[]{cx, cx + size, cx};
                yPoints = new int[]{cy, cy + size / 2, cy + size};
            }

            g2.fillPolygon(xPoints, yPoints, 3);
            g2.dispose();
        }

        @Override public int getIconWidth() { return size + 10; }
        @Override public int getIconHeight() { return size + 6; }
    }
}
