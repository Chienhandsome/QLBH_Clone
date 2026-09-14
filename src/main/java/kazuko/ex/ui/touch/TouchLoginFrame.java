package kazuko.ex.ui.touch;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.service.AuthService.AuthResult;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/** Touch-first login shell with password, numeric-code and card modes. */
public final class TouchLoginFrame extends JFrame {
    private enum Mode { CODE, PASSWORD, CARD }

    private static final Color BACKGROUND = new Color(48, 12, 11);
    private static final Color BANNER = new Color(211, 0, 0);
    private static final Color KEY_TOP = new Color(0, 67, 82);
    private static final Color KEY_BOTTOM = new Color(43, 133, 163);
    private static final Color ORANGE = new Color(255, 139, 0);
    private static final Color GREEN = new Color(0, 119, 9);

    private final AuthService authService = new AuthService();
    private final Consumer<SUser> successHandler;
    private final CardLayout screenLayout = new CardLayout();
    private final JPanel screens = new JPanel(screenLayout);
    private final CardLayout modeLayout = new CardLayout();
    private final JPanel modeCards = new JPanel(modeLayout);

    private final JTextField username = new JTextField("admin");
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField pinCode = new JPasswordField();
    private final JTextField cardCode = new JTextField();
    private final JButton codeModeButton = createModeButton("#");
    private final JButton passwordModeButton = createModeButton("**|");
    private final JButton cardModeButton = createModeButton("▰");

    private JTextField keyboardTarget;
    private boolean keyboardPassword;
    private final JTextField keyboardDisplay = new JTextField();
    private final StringBuilder keyboardBuffer = new StringBuilder();
    private boolean keyboardUppercase = true;

    public TouchLoginFrame(Consumer<SUser> successHandler) {
        this.successHandler = successHandler;
        setTitle("TAP Shop TS - Đăng nhập");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1024, 700));

        screens.add(createLoginScreen(), "login");
        screens.add(createKeyboardScreen(), "keyboard");
        setContentPane(screens);
        installActions();
        loadDefaultUsername();
        selectMode(Mode.PASSWORD);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private JPanel createLoginScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(BACKGROUND);
        right.setBorder(new EmptyBorder(18, 12, 12, 12));
        right.setPreferredSize(new Dimension(790, 0));

        JLabel banner = new JLabel("MỜI BẠN NHẬP THÔNG TIN ĐỂ ĐĂNG NHẬP", SwingConstants.CENTER);
        banner.setOpaque(true);
        banner.setBackground(BANNER);
        banner.setForeground(Color.WHITE);
        banner.setFont(new Font("Segoe UI", Font.BOLD, 21));
        banner.setBorder(new LineBorder(new Color(135, 0, 0), 2, true));
        banner.setPreferredSize(new Dimension(0, 58));
        right.add(banner, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(34, 0));
        body.setBackground(BACKGROUND);
        body.setBorder(new EmptyBorder(72, 0, 0, 0));
        JPanel modes = new JPanel(new GridLayout(3, 1, 0, 24));
        modes.setOpaque(false);
        modes.setPreferredSize(new Dimension(140, 360));
        modes.add(codeModeButton);
        modes.add(passwordModeButton);
        modes.add(cardModeButton);
        body.add(modes, BorderLayout.WEST);

        modeCards.setOpaque(false);
        modeCards.add(createPasswordCard(), Mode.PASSWORD.name());
        modeCards.add(createCodeCard(), Mode.CODE.name());
        modeCards.add(createCardReaderCard(), Mode.CARD.name());
        body.add(modeCards, BorderLayout.CENTER);
        right.add(body, BorderLayout.CENTER);
        right.add(createFooter(), BorderLayout.SOUTH);

        root.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);
        return root;
    }

    private JPanel createPasswordCard() {
        JPanel panel = transparentPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        styleCredentialField(username);
        styleCredentialField(password);
        JButton userKeyboard = createSmallButton("⌨");
        JButton passwordKeyboard = createSmallButton("⌨");
        userKeyboard.addActionListener(e -> showKeyboard(username, false));
        passwordKeyboard.addActionListener(e -> showKeyboard(password, true));

        addCredentialRow(panel, gbc, 0, "TÀI KHOẢN:", username, userKeyboard);
        addCredentialRow(panel, gbc, 1, "MẬT KHẨU:", password, passwordKeyboard);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        JCheckBox remember = createTouchCheckBox("Ghi nhớ mật khẩu");
        panel.add(remember, gbc);
        gbc.gridy = 3;
        panel.add(createTouchCheckBox("Tự động đăng nhập"), gbc);

        gbc.gridy = 4;
        JLabel hint = new JLabel("(Mật khẩu mặc định: để trống)");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        hint.setForeground(new Color(190, 190, 190));
        panel.add(hint, gbc);

        gbc.gridy = 5;
        gbc.gridwidth = 1;
        JButton login = createActionButton("Đăng nhập", GREEN, 190, 58);
        login.addActionListener(e -> submitPassword());
        panel.add(login, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JButton reset = createActionButton("Nhập lại", new Color(86, 0, 94), 190, 58);
        reset.addActionListener(e -> {
            password.setText("");
            username.selectAll();
            username.requestFocusInWindow();
        });
        panel.add(reset, gbc);
        return panel;
    }

    private JPanel createCodeCard() {
        JPanel outer = transparentPanel(new BorderLayout(0, 12));
        pinCode.setHorizontalAlignment(SwingConstants.CENTER);
        pinCode.setFont(new Font("Segoe UI", Font.BOLD, 30));
        pinCode.setEditable(false);
        pinCode.setPreferredSize(new Dimension(0, 64));
        pinCode.setToolTipText("Mã đăng nhập mặc định: " + AuthService.DEFAULT_TOUCH_CODE);
        outer.add(pinCode, BorderLayout.NORTH);

        JPanel keypad = transparentPanel(new GridLayout(4, 3, 9, 9));
        for (String key : new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"}) {
            keypad.add(createKey(key, e -> append(pinCode, key)));
        }
        keypad.add(createActionButton("◀", new Color(224, 70, 0), 150, 100));
        ((JButton) keypad.getComponent(9)).addActionListener(e -> backspace(pinCode));
        keypad.add(createKey("0", e -> append(pinCode, "0")));
        JButton submit = createActionButton("▶", GREEN, 150, 100);
        submit.addActionListener(e -> submitCode());
        keypad.add(submit);
        outer.add(keypad, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createCardReaderCard() {
        JPanel panel = transparentPanel(new BorderLayout(0, 18));
        JLabel card = new JLabel("▰", SwingConstants.CENTER);
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setForeground(BACKGROUND);
        card.setFont(new Font("Segoe UI Symbol", Font.BOLD, 180));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(card, BorderLayout.CENTER);
        cardCode.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        cardCode.setHorizontalAlignment(SwingConstants.CENTER);
        cardCode.setPreferredSize(new Dimension(0, 55));
        cardCode.setToolTipText("Quét thẻ hoặc nhập mã thẻ rồi nhấn Enter");
        panel.add(cardCode, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = transparentPanel(new BorderLayout(10, 0));
        JPanel info = transparentPanel(new GridLayout(2, 1, 0, 5));
        info.add(createFooterLabel("PHIÊN BẢN 5.1.0.0"));
        info.add(createFooterLabel("CƠ SỞ DỮ LIỆU: " + databaseName()));
        footer.add(info, BorderLayout.CENTER);
        JButton power = createActionButton("⏻", BACKGROUND.brighter(), 100, 90);
        power.setFont(new Font("Segoe UI Symbol", Font.BOLD, 50));
        power.addActionListener(e -> exitApplication());
        footer.add(power, BorderLayout.EAST);
        return footer;
    }

    private JPanel createKeyboardScreen() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(78, 139, 169));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel title = new JLabel("MỜI BẠN NHẬP TÀI KHOẢN ĐĂNG NHẬP", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(new Color(79, 139, 169));
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setPreferredSize(new Dimension(0, 120));
        panel.add(title, BorderLayout.NORTH);

        keyboardDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 38));
        keyboardDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        keyboardDisplay.setEditable(false);
        keyboardDisplay.setPreferredSize(new Dimension(0, 180));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(keyboardDisplay, BorderLayout.NORTH);
        center.add(createKeyboardKeys(), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel actions = transparentPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton cancel = createActionButton("Hủy bỏ", Color.RED.darker(), 285, 130);
        cancel.addActionListener(e -> screenLayout.show(screens, "login"));
        JButton space = createActionButton("", Color.DARK_GRAY, 720, 130);
        space.addActionListener(e -> appendKeyboard(" "));
        JButton accept = createActionButton("➜", GREEN, 285, 130);
        accept.setFont(new Font("Segoe UI Symbol", Font.BOLD, 60));
        accept.addActionListener(e -> acceptKeyboard());
        actions.add(cancel);
        actions.add(space);
        actions.add(accept);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createKeyboardKeys() {
        JPanel rows = transparentPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        addKeyboardRow(rows, "1234567890-=⌫");
        addKeyboardRow(rows, "QWERTYUIOP\\@");
        addKeyboardRow(rows, "ASDFGHJKL;'");
        addKeyboardRow(rows, "ZXCVBNM,.");
        return rows;
    }

    private void addKeyboardRow(JPanel rows, String keys) {
        JPanel row = transparentPanel(new GridLayout(1, keys.length(), 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));
        for (int i = 0; i < keys.length(); i++) {
            String key = String.valueOf(keys.charAt(i));
            JButton button = createKey(key, e -> {
                if ("⌫".equals(key)) backspaceKeyboard();
                else appendKeyboard(keyboardUppercase ? key : key.toLowerCase());
            });
            row.add(button);
        }
        rows.add(row);
        rows.add(Box.createVerticalStrut(6));
    }

    private void installActions() {
        codeModeButton.addActionListener(e -> selectMode(Mode.CODE));
        passwordModeButton.addActionListener(e -> selectMode(Mode.PASSWORD));
        cardModeButton.addActionListener(e -> selectMode(Mode.CARD));
        username.addActionListener(e -> password.requestFocusInWindow());
        password.addActionListener(e -> submitPassword());
        cardCode.addActionListener(e -> submitCard());
    }

    private void selectMode(Mode mode) {
        modeLayout.show(modeCards, mode.name());
        codeModeButton.setBackground(mode == Mode.CODE ? ORANGE : BACKGROUND.brighter());
        passwordModeButton.setBackground(mode == Mode.PASSWORD ? ORANGE : BACKGROUND.brighter());
        cardModeButton.setBackground(mode == Mode.CARD ? ORANGE : BACKGROUND.brighter());
        if (mode == Mode.CODE) {
            pinCode.setText("");
        } else if (mode == Mode.CARD) {
            SwingUtilities.invokeLater(cardCode::requestFocusInWindow);
        }
    }

    private void submitPassword() {
        submitResult(authService.authenticate(username.getText(), new String(password.getPassword())));
    }

    private void submitCode() {
        submitResult(authService.authenticateByTouchCode(new String(pinCode.getPassword())));
        pinCode.setText("");
    }

    private void submitCard() {
        submitResult(authService.authenticateByCardCode(cardCode.getText()));
        cardCode.setText("");
    }

    private void submitResult(AuthResult result) {
        if (!result.isSuccess() || result.getUser() == null) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
        SwingUtilities.invokeLater(() -> successHandler.accept(result.getUser()));
    }

    private void showKeyboard(JTextField target, boolean passwordTarget) {
        keyboardTarget = target;
        keyboardPassword = passwordTarget;
        keyboardBuffer.setLength(0);
        keyboardBuffer.append(target.getText());
        refreshKeyboardDisplay();
        screenLayout.show(screens, "keyboard");
    }

    private void acceptKeyboard() {
        if (keyboardTarget != null) keyboardTarget.setText(keyboardBuffer.toString());
        screenLayout.show(screens, "login");
        if (keyboardPassword) password.requestFocusInWindow();
        else username.requestFocusInWindow();
    }

    private void appendKeyboard(String value) {
        keyboardBuffer.append(value);
        refreshKeyboardDisplay();
    }

    private void backspaceKeyboard() {
        if (!keyboardBuffer.isEmpty()) {
            keyboardBuffer.deleteCharAt(keyboardBuffer.length() - 1);
            refreshKeyboardDisplay();
        }
    }

    private void refreshKeyboardDisplay() {
        keyboardDisplay.setText(keyboardPassword
                ? "•".repeat(keyboardBuffer.length())
                : keyboardBuffer.toString());
    }

    private void append(JTextField field, String value) {
        field.setText(field.getText() + value);
    }

    private void backspace(JTextField field) {
        String value = field.getText();
        if (!value.isEmpty()) field.setText(value.substring(0, value.length() - 1));
    }

    private void loadDefaultUsername() {
        List<SUser> users = authService.getAllUsers();
        for (SUser user : users) {
            if (user.isAdminUser()) {
                username.setText(user.getUsername());
                return;
            }
        }
        if (!users.isEmpty() && users.get(0).getUsername() != null) {
            username.setText(users.get(0).getUsername());
        }
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this, "Thoát TAP Shop TS?", "TAP Shop TS",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) dispose();
    }

    private void addCredentialRow(JPanel panel, GridBagConstraints gbc, int row,
                                  String label, JComponent field, JButton keyboard) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel text = new JLabel(label);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(text, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(keyboard, gbc);
    }

    private void styleCredentialField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        field.setPreferredSize(new Dimension(330, 52));
        field.setBackground(Color.WHITE);
    }

    private JCheckBox createTouchCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setOpaque(false);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Segoe UI", Font.BOLD, 17));
        checkBox.setIconTextGap(10);
        return checkBox;
    }

    private JButton createModeButton(String text) {
        JButton button = createActionButton(text, BACKGROUND.brighter(), 140, 105);
        button.setFont(new Font("Segoe UI Symbol", Font.BOLD, 52));
        return button;
    }

    private JButton createSmallButton(String text) {
        JButton button = createActionButton(text, BACKGROUND.brighter(), 72, 52);
        button.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));
        return button;
    }

    private JButton createKey(String text, ActionListener action) {
        JButton button = new GradientButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 30));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(new Color(200, 220, 228), 2, true));
        button.addActionListener(action);
        return button;
    }

    private JButton createActionButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 19));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 225, 230), 2, true),
                new EmptyBorder(4, 8, 4, 8)));
        return button;
    }

    private JPanel transparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private JLabel createFooterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(new Color(69, 36, 34));
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setBorder(new EmptyBorder(3, 8, 3, 8));
        return label;
    }

    private String databaseName() {
        try {
            String configured = DatabaseConnection.getConfig().getDatabasePath();
            String fileName = Path.of(configured).getFileName().toString();
            int extension = fileName.lastIndexOf('.');
            return extension > 0 ? fileName.substring(0, extension) : fileName;
        } catch (Exception ignored) {
            return "DEMO";
        }
    }

    private static final class GradientButton extends JButton {
        private GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, KEY_TOP, 0, getHeight(), KEY_BOTTOM));
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
            } finally {
                g2.dispose();
            }
            super.paintComponent(graphics);
        }
    }
}
