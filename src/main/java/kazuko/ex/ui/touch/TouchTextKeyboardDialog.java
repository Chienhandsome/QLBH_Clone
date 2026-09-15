package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Hộp thoại bàn phím ảo cảm ứng (Touch Virtual Keyboard) theo Hình 3.
 * Khi bấm nút [⌨] trên form khách hàng, hiển thị bàn phím này để nhập văn bản.
 */
public class TouchTextKeyboardDialog extends JDialog {
    private static final Color BG = new Color(55, 95, 120);
    private static final Color KEY_BG = new Color(0, 80, 105);
    private static final Color KEY_BORDER = new Color(0, 110, 140);
    private static final Color SPECIAL_KEY_BG = new Color(50, 50, 50);
    private static final Color RED_BTN = new Color(185, 20, 20);
    private static final Color GREEN_BTN = new Color(0, 130, 20);

    private final JTextField txtInput = new JTextField();
    private boolean isShift = true; // Mặc định chữ hoa
    private boolean confirmed = false;
    private String resultText = null;

    private final JButton btnShift = new JButton("▲");
    private final java.util.List<JButton> alphabetKeys = new java.util.ArrayList<>();

    public TouchTextKeyboardDialog(Window owner, String title, String initialText) {
        super(owner, title != null ? title : "BÀN PHÍM CẢM ỨNG", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        this.resultText = initialText != null ? initialText : "";
        initComponents();
        txtInput.setText(this.resultText);
        txtInput.setCaretPosition(txtInput.getText().length());
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new LineBorder(new Color(30, 60, 80), 3));
        root.setPreferredSize(new Dimension(720, 360));

        // 1. Display text field at top
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(12, 14, 2, 14));

        txtInput.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtInput.setPreferredSize(new Dimension(690, 48));
        txtInput.setBackground(Color.WHITE);
        txtInput.setForeground(new Color(20, 20, 20));
        txtInput.setBorder(new LineBorder(new Color(180, 180, 180), 2));
        top.add(txtInput, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        // 2. Keyboard rows in center
        JPanel keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));
        keyboardPanel.setOpaque(false);
        keyboardPanel.setBorder(new EmptyBorder(0, 14, 10, 14));

        // Row 1: 1 2 3 4 5 6 7 8 9 0 - ⌫
        String[] row1 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-"};
        JPanel pnlRow1 = createKeyRow();
        for (String k : row1) {
            pnlRow1.add(createCharKey(k, false));
        }
        JButton btnBackspace = createKey("◄", SPECIAL_KEY_BG, new Dimension(72, 46));
        btnBackspace.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnBackspace.addActionListener(e -> handleBackspace());
        pnlRow1.add(btnBackspace);
        keyboardPanel.add(pnlRow1);
        keyboardPanel.add(Box.createVerticalStrut(6));

        // Row 2: Q W E R T Y U I O P
        String[] row2 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        JPanel pnlRow2 = createKeyRow();
        for (String k : row2) {
            pnlRow2.add(createCharKey(k, true));
        }
        keyboardPanel.add(pnlRow2);
        keyboardPanel.add(Box.createVerticalStrut(6));

        // Row 3: A S D F G H J K L : '
        String[] row3 = {"A", "S", "D", "F", "G", "H", "J", "K", "L", ":", "'"};
        JPanel pnlRow3 = createKeyRow();
        for (String k : row3) {
            pnlRow3.add(createCharKey(k, !k.equals(":") && !k.equals("'")));
        }
        keyboardPanel.add(pnlRow3);
        keyboardPanel.add(Box.createVerticalStrut(6));

        // Row 4: Z X C V B N M , . Shift
        String[] row4 = {"Z", "X", "C", "V", "B", "N", "M", ",", "."};
        JPanel pnlRow4 = createKeyRow();
        for (String k : row4) {
            pnlRow4.add(createCharKey(k, !k.equals(",") && !k.equals(".")));
        }
        btnShift.setPreferredSize(new Dimension(68, 46));
        btnShift.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnShift.setBackground(SPECIAL_KEY_BG);
        btnShift.setForeground(Color.YELLOW);
        btnShift.setFocusPainted(false);
        btnShift.setBorder(new LineBorder(new Color(80, 80, 80), 1));
        btnShift.addActionListener(e -> toggleShift());
        pnlRow4.add(btnShift);
        keyboardPanel.add(pnlRow4);
        keyboardPanel.add(Box.createVerticalStrut(8));

        // Row 5: Hủy bỏ | Spacebar | ➔ (Chấp nhận)
        JPanel pnlRow5 = new JPanel(new BorderLayout(10, 0));
        pnlRow5.setOpaque(false);
        pnlRow5.setPreferredSize(new Dimension(690, 48));

        JButton btnCancel = createKey("Hủy bỏ", RED_BTN, new Dimension(130, 48));
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        pnlRow5.add(btnCancel, BorderLayout.WEST);

        JButton btnSpace = createKey(" ", SPECIAL_KEY_BG, new Dimension(400, 48));
        btnSpace.addActionListener(e -> handleChar(" "));
        pnlRow5.add(btnSpace, BorderLayout.CENTER);

        JButton btnAccept = createKey("➔", GREEN_BTN, new Dimension(130, 48));
        btnAccept.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnAccept.addActionListener(e -> handleAccept());
        pnlRow5.add(btnAccept, BorderLayout.EAST);

        keyboardPanel.add(pnlRow5);
        root.add(keyboardPanel, BorderLayout.CENTER);

        setContentPane(root);

        // Enter to accept, Esc to cancel
        txtInput.addActionListener(e -> handleAccept());
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel createKeyRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);
        return row;
    }

    private JButton createCharKey(String text, boolean isAlphabet) {
        JButton btn = createKey(text, KEY_BG, new Dimension(54, 46));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        if (isAlphabet) {
            alphabetKeys.add(btn);
        }
        btn.addActionListener(e -> handleChar(btn.getText()));
        return btn;
    }

    private JButton createKey(String text, Color bg, Dimension size) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(KEY_BORDER, 1));
        btn.setPreferredSize(size);
        return btn;
    }

    private void handleChar(String ch) {
        int pos = txtInput.getCaretPosition();
        String current = txtInput.getText();
        String next = current.substring(0, pos) + ch + current.substring(pos);
        txtInput.setText(next);
        txtInput.setCaretPosition(pos + ch.length());
        txtInput.requestFocusInWindow();
    }

    private void handleBackspace() {
        int pos = txtInput.getCaretPosition();
        String current = txtInput.getText();
        if (pos > 0 && !current.isEmpty()) {
            String next = current.substring(0, pos - 1) + current.substring(pos);
            txtInput.setText(next);
            txtInput.setCaretPosition(pos - 1);
        }
        txtInput.requestFocusInWindow();
    }

    private void toggleShift() {
        isShift = !isShift;
        btnShift.setForeground(isShift ? Color.YELLOW : Color.LIGHT_GRAY);
        for (JButton b : alphabetKeys) {
            String t = b.getText();
            b.setText(isShift ? t.toUpperCase() : t.toLowerCase());
        }
    }

    private void handleAccept() {
        confirmed = true;
        resultText = txtInput.getText();
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getResultText() {
        return resultText;
    }

    /**
     * Mở hộp thoại bàn phím ảo cảm ứng và trả về chuỗi text đã nhập.
     * @return Văn bản mới hoặc null nếu bấm Hủy bỏ.
     */
    public static String showDialog(Window owner, String title, String initialText) {
        TouchTextKeyboardDialog dlg = new TouchTextKeyboardDialog(owner, title, initialText);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            return dlg.getResultText();
        }
        return null;
    }
}
