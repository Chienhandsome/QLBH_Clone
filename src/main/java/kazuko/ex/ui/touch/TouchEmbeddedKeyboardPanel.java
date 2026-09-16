package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bàn phím ảo QWERTY nhúng (Embedded QWERTY Keyboard)
 * chuẩn 100% theo Hình 2 (Tìm kiếm khách hàng) và Hình 5 (Tìm kiếm mặt hàng).
 */
public class TouchEmbeddedKeyboardPanel extends JPanel {
    private static final Color KEY_BG = new Color(0, 95, 120);
    private static final Color KEY_BORDER = new Color(0, 145, 180);
    private static final Color SPECIAL_KEY_BG = new Color(60, 65, 70);
    private static final Color SHIFT_ACTIVE_COLOR = new Color(255, 235, 0); // Vàng sáng như hình

    private boolean isShift = true; // Mặc định chữ hoa theo Hình 2 & Hình 5
    private final Consumer<String> onKeyPress;
    private final Runnable onBackspace;
    private final Runnable onSpace;
    private final boolean stretchKeys;

    private final List<JButton> alphabetButtons = new ArrayList<>();
    private JButton btnShift;

    public TouchEmbeddedKeyboardPanel(Consumer<String> onKeyPress, Runnable onBackspace, Runnable onSpace) {
        this(onKeyPress, onBackspace, onSpace, false);
    }

    public TouchEmbeddedKeyboardPanel(Consumer<String> onKeyPress, Runnable onBackspace,
                                      Runnable onSpace, boolean stretchKeys) {
        this.onKeyPress = onKeyPress;
        this.onBackspace = onBackspace;
        this.onSpace = onSpace;
        this.stretchKeys = stretchKeys;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(4, 6, 6, 6));

        initKeyboardRows();
    }

    private void initKeyboardRows() {
        // Hàng 1: 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, -, =, ◄
        String[] r1 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "="};
        JPanel row1 = createRowPanel();
        for (String k : r1) {
            row1.add(createCharButton(k, false));
        }
        JButton btnBksp = createSpecialButton("◄", SPECIAL_KEY_BG,
                new Dimension(stretchKeys ? 100 : 70, stretchKeys ? 58 : 38));
        btnBksp.setForeground(Color.WHITE);
        btnBksp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnBksp.addActionListener(e -> {
            if (onBackspace != null) onBackspace.run();
        });
        row1.add(btnBksp);
        add(row1);
        add(Box.createVerticalStrut(4));

        // Hàng 2: Q, W, E, R, T, Y, U, I, O, P, \, @
        String[] r2 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "\\", "@"};
        JPanel row2 = createRowPanel();
        for (String k : r2) {
            boolean isAlpha = Character.isLetter(k.charAt(0));
            row2.add(createCharButton(k, isAlpha));
        }
        add(row2);
        add(Box.createVerticalStrut(4));

        // Hàng 3: A, S, D, F, G, H, J, K, L, ;, '
        String[] r3 = {"A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'"};
        JPanel row3 = createRowPanel();
        for (String k : r3) {
            boolean isAlpha = Character.isLetter(k.charAt(0));
            row3.add(createCharButton(k, isAlpha));
        }
        add(row3);
        add(Box.createVerticalStrut(4));

        // Hàng 4: Z, X, C, V, B, N, M, ,, ., ▲ (Shift)
        String[] r4 = {"Z", "X", "C", "V", "B", "N", "M", ",", "."};
        JPanel row4 = createRowPanel();
        for (String k : r4) {
            boolean isAlpha = Character.isLetter(k.charAt(0));
            row4.add(createCharButton(k, isAlpha));
        }

        btnShift = createSpecialButton("▲", SPECIAL_KEY_BG, new Dimension(68, 38));
        btnShift.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnShift.setForeground(SHIFT_ACTIVE_COLOR);
        btnShift.addActionListener(e -> toggleShift());
        row4.add(btnShift);
        add(row4);
        add(Box.createVerticalStrut(4));

        // Hàng 5: Space (thanh dài ở giữa như Hình 2)
        JPanel row5 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        row5.setOpaque(false);
        JButton btnSpace = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 50, 50), 0, getHeight(), new Color(80, 80, 80));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(110, 110, 110));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        btnSpace.setPreferredSize(new Dimension(stretchKeys ? 720 : 340, stretchKeys ? 54 : 36));
        btnSpace.setFocusPainted(false);
        btnSpace.setContentAreaFilled(false);
        btnSpace.setBorderPainted(false);
        btnSpace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSpace.addActionListener(e -> {
            if (onSpace != null) onSpace.run();
        });
        row5.add(btnSpace);
        add(row5);
    }

    private JPanel createRowPanel() {
        JPanel pnl = stretchKeys
                ? new JPanel(new GridLayout(1, 0, 6, 0))
                : new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        pnl.setOpaque(false);
        if (stretchKeys) {
            pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            pnl.setPreferredSize(new Dimension(0, 58));
        }
        return pnl;
    }

    private JButton createCharButton(String text, boolean isAlpha) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(stretchKeys ? 100 : 56, stretchKeys ? 58 : 38));
        btn.setFont(new Font("Segoe UI", Font.BOLD, stretchKeys ? 22 : 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(KEY_BG);
        btn.setBorder(new LineBorder(KEY_BORDER, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (isAlpha) {
            alphabetButtons.add(btn);
        }

        btn.addActionListener(e -> {
            String val = btn.getText();
            if (onKeyPress != null) {
                onKeyPress.accept(val);
            }
        });
        return btn;
    }

    private JButton createSpecialButton(String text, Color bg, Dimension dim) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(dim);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(new Color(100, 110, 115), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void toggleShift() {
        isShift = !isShift;
        btnShift.setForeground(isShift ? SHIFT_ACTIVE_COLOR : Color.LIGHT_GRAY);
        for (JButton b : alphabetButtons) {
            String current = b.getText();
            b.setText(isShift ? current.toUpperCase() : current.toLowerCase());
        }
    }

    public boolean isShift() {
        return isShift;
    }
}
