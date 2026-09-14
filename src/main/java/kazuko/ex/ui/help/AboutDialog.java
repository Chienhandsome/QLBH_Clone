package kazuko.ex.ui.help;

import kazuko.ex.service.LicenseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URL;

public class AboutDialog extends JDialog {
    private static final Color BLUE_BACKGROUND = new Color(207, 222, 241);
    private static final Color ACCENT = new Color(230, 75, 20);
    private final LicenseService licenseService = new LicenseService();
    private final JLabel lblOwnerValue = new JLabel();
    private final JLabel lblOrganizationValue = new JLabel();
    private final JLabel lblLicenseValue = new JLabel();
    private final JLabel lblEditionValue = new JLabel();
    private final JLabel lblSupportValue = new JLabel();

    public AboutDialog(Window owner) {
        super(owner, "Thông tin phần mềm", ModalityType.APPLICATION_MODAL);
        initUI();
        refreshLicenseStatus();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(525, 445);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BLUE_BACKGROUND);
        setContentPane(root);

        JLabel banner = new JLabel();
        banner.setOpaque(true);
        banner.setBackground(Color.WHITE);
        URL bannerUrl = getClass().getResource("/banner.png");
        if (bannerUrl != null) {
            Image image = new ImageIcon(bannerUrl).getImage().getScaledInstance(525, 89, Image.SCALE_SMOOTH);
            banner.setIcon(new ImageIcon(image));
        } else {
            banner.setText(HelpInfo.PRODUCT_NAME);
            banner.setHorizontalAlignment(SwingConstants.CENTER);
        }
        root.add(banner, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 18, 4, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel version = accentLabel("Phiên bản: " + HelpInfo.VERSION);
        JLabel release = accentLabel("Ngày phát hành: " + HelpInfo.RELEASE_DATE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        content.add(version, gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        content.add(release, gbc);

        JLabel runtime = accentLabel("Phiên bản nền: Java " + System.getProperty("java.version", ""));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        content.add(runtime, gbc);

        JLabel registrationTitle = new JLabel("Thông tin đăng ký");
        registrationTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registrationTitle.setBorder(new MatteBorder(0, 0, 1, 0, new Color(160, 160, 160)));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 3, 8, 3);
        content.add(registrationTitle, gbc);

        gbc.insets = new Insets(3, 3, 3, 3);
        addInfoRow(content, gbc, 3, "Họ và tên:", lblOwnerValue);
        addInfoRow(content, gbc, 4, "Đơn vị:", lblOrganizationValue);
        addInfoRow(content, gbc, 5, "Mã đăng ký:", lblLicenseValue);
        addInfoRow(content, gbc, 6, "Phiên bản:", lblEditionValue);
        addInfoRow(content, gbc, 7, "Bảo hành:", lblSupportValue);

        JButton register = linkButton("Đăng ký");
        register.addActionListener(e -> {
            RegistrationDialog dialog = new RegistrationDialog(this);
            dialog.setVisible(true);
            if (dialog.isRegistrationChanged()) refreshLicenseStatus();
        });
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        content.add(register, gbc);
        root.add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(7, 18, 10, 12));
        JLabel copyright = new JLabel("© QLBH Clone");
        JButton email = linkButton("Email: " + HelpInfo.SUPPORT_EMAIL);
        email.addActionListener(e -> openUri("mailto:" + HelpInfo.SUPPORT_EMAIL));
        JButton close = new JButton("Đóng");
        close.setPreferredSize(new Dimension(90, 32));
        close.addActionListener(e -> dispose());
        footer.add(copyright, BorderLayout.WEST);
        footer.add(email, BorderLayout.CENTER);
        footer.add(close, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(close);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JLabel accentLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(ACCENT);
        return label;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String title, JLabel value) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(value, gbc);
    }

    private JButton linkButton(String text) {
        JButton button = new JButton("<html><u>" + text + "</u></html>");
        button.setForeground(new Color(25, 65, 210));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void refreshLicenseStatus() {
        LicenseService.LicenseStatus status = licenseService.getStatus();
        if (status.registered()) {
            lblOwnerValue.setText(status.owner());
            lblOrganizationValue.setText(status.organization());
            lblLicenseValue.setText(status.registrationKey());
            lblLicenseValue.setForeground(new Color(0, 140, 40));
            lblEditionValue.setText("Đã đăng ký");
            lblSupportValue.setText("Bản quyền vĩnh viễn");
            lblSupportValue.setForeground(new Color(30, 30, 30));
        } else {
            lblOwnerValue.setText("Chưa đăng ký");
            lblOrganizationValue.setText("Chưa đăng ký");
            lblLicenseValue.setText("Bạn còn " + status.remainingTrialDays() + " ngày dùng thử.");
            lblLicenseValue.setForeground(new Color(30, 30, 30));
            lblEditionValue.setText("Dùng thử");
            lblSupportValue.setText("Chưa đăng ký");
            lblSupportValue.setForeground(new Color(25, 65, 210));
        }
    }

    private void openUri(String uri) {
        try {
            URI target = URI.create(uri);
            Desktop.Action action = "mailto".equalsIgnoreCase(target.getScheme())
                    ? Desktop.Action.MAIL : Desktop.Action.BROWSE;
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(action)) {
                throw new UnsupportedOperationException();
            }
            if (action == Desktop.Action.MAIL) {
                Desktop.getDesktop().mail(target);
            } else {
                Desktop.getDesktop().browse(target);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở liên kết:\n" + uri,
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}
