package kazuko.ex.ui.help;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.service.LicenseService;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.net.URI;

public class RegistrationDialog extends JDialog {
    private static final Color BLUE_BACKGROUND = new Color(207, 222, 241);
    private static final Color ACCENT = new Color(188, 91, 0);
    private final LicenseService licenseService;
    private final JTextField txtOwner = new JTextField();
    private final JTextField txtOrganization = new JTextField();
    private final JTextField txtMachineId = new JTextField();
    private final JTextField txtRegistrationKey = new JTextField();
    private boolean registrationChanged;

    public RegistrationDialog(Window owner) {
        this(owner, new LicenseService());
    }

    RegistrationDialog(Window owner, LicenseService licenseService) {
        super(owner, "Đăng ký", ModalityType.APPLICATION_MODAL);
        this.licenseService = licenseService;
        initUI();
        loadCurrentValues();
    }

    public boolean isRegistrationChanged() {
        return registrationChanged;
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(520, 420);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BLUE_BACKGROUND);
        setContentPane(root);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(2, 8, 2, 8));
        JLabel title = new JLabel("Đăng ký bản quyền", IconHelper.getIcon("btnCard.Icon.png", 34, 34), SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titlePanel.add(title);
        root.add(titlePanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 6));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(6, 14, 4, 14));

        JPanel versionPanel = new JPanel(new BorderLayout());
        versionPanel.setOpaque(false);
        JLabel version = new JLabel("Phiên bản: " + HelpInfo.VERSION);
        JLabel release = new JLabel("Ngày phát hành: " + HelpInfo.RELEASE_DATE, SwingConstants.RIGHT);
        for (JLabel label : new JLabel[]{version, release}) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(ACCENT);
        }
        versionPanel.add(version, BorderLayout.WEST);
        versionPanel.add(release, BorderLayout.EAST);
        center.add(versionPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("Đăng ký bản quyền", createRegistrationPanel());
        tabs.addTab("Mua key thế nào", createPurchasePanel());
        center.add(tabs, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        footer.setOpaque(false);
        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setPreferredSize(new Dimension(92, 32));
        btnUpdate.addActionListener(e -> register());
        JButton btnClose = new JButton("Thoát");
        btnClose.setPreferredSize(new Dimension(92, 32));
        btnClose.addActionListener(e -> dispose());
        footer.add(btnUpdate);
        footer.add(btnClose);
        root.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnUpdate);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        panel.add(new JLabel("Mời bạn điền thông tin nhận được từ đơn vị cung cấp"), gbc);

        addField(panel, gbc, 1, "Họ và tên:", txtOwner, null);
        addField(panel, gbc, 2, "Đơn vị:", txtOrganization, null);
        txtMachineId.setEditable(false);
        JButton btnCopy = new JButton("Sao chép");
        btnCopy.setToolTipText("Sao chép mã máy");
        btnCopy.addActionListener(e -> copyMachineId());
        addField(panel, gbc, 3, "Mã máy:", txtMachineId, btnCopy);

        JButton btnPaste = new JButton("Dán");
        btnPaste.setToolTipText("Dán mã đăng ký từ bộ nhớ tạm");
        btnPaste.addActionListener(e -> pasteRegistrationKey());
        addField(panel, gbc, 4, "Mã đăng ký:", txtRegistrationKey, btnPaste);

        JButton btnFromConfig = createLinkButton("Lấy thông tin từ cấu hình");
        btnFromConfig.addActionListener(e -> loadCompanyConfig());
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnFromConfig, gbc);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText,
                          JTextField field, JButton actionButton) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label, gbc);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        if (actionButton != null) {
            actionButton.setPreferredSize(new Dimension(76, 25));
            panel.add(actionButton, gbc);
        } else {
            panel.add(Box.createHorizontalStrut(76), gbc);
        }
    }

    private JPanel createPurchasePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 16, 12, 16));
        panel.add(leftLabel("Bạn vào đường dẫn:"));
        JButton website = createLinkButton(HelpInfo.WEBSITE);
        website.setAlignmentX(Component.LEFT_ALIGNMENT);
        website.addActionListener(e -> openUri(HelpInfo.WEBSITE));
        panel.add(website);
        panel.add(Box.createVerticalStrut(12));
        panel.add(leftLabel("Hoặc liên lạc trực tiếp với chúng tôi qua:"));
        panel.add(Box.createVerticalStrut(8));
        JButton email = createLinkButton(HelpInfo.SUPPORT_EMAIL);
        email.setAlignmentX(Component.LEFT_ALIGNMENT);
        email.addActionListener(e -> openUri("mailto:" + HelpInfo.SUPPORT_EMAIL));
        panel.add(email);
        panel.add(Box.createVerticalStrut(10));
        JLabel phone = leftLabel("Điện thoại: " + HelpInfo.SUPPORT_PHONE);
        phone.setForeground(new Color(40, 70, 200));
        panel.add(phone);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JLabel leftLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton("<html><u>" + text + "</u></html>");
        button.setForeground(new Color(30, 65, 210));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadCurrentValues() {
        LicenseService.LicenseStatus status = licenseService.getStatus();
        txtMachineId.setText(status.machineId());
        txtRegistrationKey.setText(status.registrationKey());
        txtOwner.setText(status.owner());
        txtOrganization.setText(status.organization());
        if (txtOwner.getText().isBlank() && txtOrganization.getText().isBlank()) {
            loadCompanyConfig();
        }
    }

    private void loadCompanyConfig() {
        SystemConfig config = new SystemConfigDAO().loadConfig();
        txtOwner.setText(config.getTenCongTy() != null ? config.getTenCongTy() : "");
        txtOrganization.setText(config.getDiaChi() != null ? config.getDiaChi() : "");
    }

    private void copyMachineId() {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(txtMachineId.getText()), null);
            JOptionPane.showMessageDialog(this, "Đã sao chép mã máy.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể sao chép mã máy.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pasteRegistrationKey() {
        try {
            Object value = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (value != null) txtRegistrationKey.setText(value.toString().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Bộ nhớ tạm không chứa mã đăng ký.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void register() {
        LicenseService.RegistrationResult result = licenseService.register(
                txtOwner.getText(), txtOrganization.getText(), txtRegistrationKey.getText());
        JOptionPane.showMessageDialog(this, result.message(), result.success() ? "Thông báo" : "Đăng ký không thành công",
                result.success() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        if (result.success()) {
            registrationChanged = true;
            dispose();
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
