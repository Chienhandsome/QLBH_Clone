package kazuko.ex.ui.touch;

import kazuko.ex.service.LicenseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Giao diện cảm ứng THÔNG TIN PHẦN MỀM (TouchThongTinPhanMemPanel).
 * Chuẩn hóa 100% theo ảnh thiết kế media_1789542181771.png:
 * - Header banner: THÔNG TIN PHẦN MỀM
 * - Dòng phụ: Phiên bản: 5.1.0.0 (trái) và Ngày phát hành: 17-05-2017 (phải)
 * - Thẻ trắng xám bo góc ở giữa:
 *   + Tiêu đề đỏ: THÔNG TIN ĐĂNG KÝ
 *   + HỌ VÀ TÊN: Chưa đăng ký (hoặc tên người dùng)
 *   + ĐƠN VỊ: Chưa đăng ký (hoặc tên đơn vị)
 *   + MÃ ĐĂNG KÝ: Bạn còn X ngày dùng thử. (hoặc mã key)
 *   + PHIÊN BẢN: Dùng thử (hoặc Bản quyền chính thức)
 *   + Dưới cùng thẻ: (C) by HN và Email: hoanngoc@techpos.vn
 * - Chân trang: Nút [ĐĂNG KÝ] góc trái dưới và nút [THOÁT] góc phải dưới
 */
public final class TouchThongTinPhanMemPanel extends JPanel {
    public static final Color BG_FRAME = new Color(10, 51, 64);          // #0A3340 Dark teal-black frame
    public static final Color HEADER_BG = new Color(61, 104, 122);       // #3D687A Slate cyan banner
    public static final Color CARD_BG = new Color(221, 226, 229);        // #DDE2E5 Light-gray card
    public static final Color TITLE_RED = new Color(123, 0, 0);          // #7B0000 Dark red title
    public static final Color BTN_REGISTER_BG = new Color(236, 236, 236);// #ECECEC Light gray
    public static final Color BTN_EXIT_BG = new Color(155, 0, 0);        // #9B0000 Dark red

    public static final String VERSION_STR = "5.1.0.0";
    public static final String RELEASE_DATE_STR = "17-05-2017";
    public static final String SUPPORT_EMAIL = "hoanngoc@techpos.vn";

    private final Runnable backAction;
    private final Runnable openRegisterAction;
    private final LicenseService licenseService;

    private JLabel lblOwnerVal;
    private JLabel lblOrgVal;
    private JLabel lblKeyVal;
    private JLabel lblEditionVal;

    public TouchThongTinPhanMemPanel(Runnable backAction, Runnable openRegisterAction) {
        this(backAction, openRegisterAction, new LicenseService());
    }

    public TouchThongTinPhanMemPanel(Runnable backAction, Runnable openRegisterAction, LicenseService licenseService) {
        this.backAction = backAction;
        this.openRegisterAction = openRegisterAction;
        this.licenseService = licenseService != null ? licenseService : new LicenseService();

        setLayout(new BorderLayout(0, 10));
        setBackground(BG_FRAME);
        setBorder(new EmptyBorder(10, 12, 10, 12));

        initUI();
        refreshLicenseData();
    }

    private void initUI() {
        // 1. Top Section (Banner + Sub-header info)
        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(createHeaderBanner(), BorderLayout.NORTH);
        topSection.add(createSubHeader(), BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        // 2. Center Card
        add(createCenterCard(), BorderLayout.CENTER);

        // 3. Bottom Footer Bar
        add(createFooterBar(), BorderLayout.SOUTH);
    }

    /** Header Banner */
    private JPanel createHeaderBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HEADER_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(0, 36));
        banner.setLayout(new GridBagLayout());

        JLabel lblTitle = new JLabel("THÔNG TIN PHẦN MỀM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        banner.add(lblTitle);

        return banner;
    }

    /** Sub-header: Phiên bản: 5.1.0.0 (trái) & Ngày phát hành: 17-05-2017 (phải) */
    private JPanel createSubHeader() {
        JPanel sub = new JPanel(new BorderLayout());
        sub.setOpaque(false);
        sub.setBorder(new EmptyBorder(0, 8, 2, 8));

        JLabel lblVer = new JLabel("Phiên bản: " + VERSION_STR);
        lblVer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblVer.setForeground(Color.WHITE);

        JLabel lblDate = new JLabel("Ngày phát hành: " + RELEASE_DATE_STR);
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDate.setForeground(Color.WHITE);

        sub.add(lblVer, BorderLayout.WEST);
        sub.add(lblDate, BorderLayout.EAST);
        return sub;
    }

    /** Center Light-gray Card matching media_1789542181771.png */
    private JPanel createCenterCard() {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(4, 0, 4, 0));

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Card Content
        JPanel cardContent = new JPanel(null);
        cardContent.setOpaque(false);

        // Section Title: THÔNG TIN ĐĂNG KÝ
        JLabel lblSectionTitle = new JLabel("THÔNG TIN ĐĂNG KÝ");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSectionTitle.setForeground(TITLE_RED);
        lblSectionTitle.setBounds(10, 5, 300, 26);
        cardContent.add(lblSectionTitle);

        // 4 Rows
        int lblX = 100;
        int lblW = 160;
        int valX = 270;
        int valW = 480;
        int y = 45;
        int gap = 38;

        // 1. HỌ VÀ TÊN:
        cardContent.add(createRowLabel("HỌ VÀ TÊN:", lblX, y, lblW, 26));
        lblOwnerVal = createRowValue("Chưa đăng ký", valX, y, valW, 26);
        cardContent.add(lblOwnerVal);

        // 2. ĐƠN VỊ:
        y += gap;
        cardContent.add(createRowLabel("ĐƠN VỊ:", lblX, y, lblW, 26));
        lblOrgVal = createRowValue("Chưa đăng ký", valX, y, valW, 26);
        cardContent.add(lblOrgVal);

        // 3. MÃ ĐĂNG KÝ:
        y += gap;
        cardContent.add(createRowLabel("MÃ ĐĂNG KÝ:", lblX, y, lblW, 26));
        lblKeyVal = createRowValue("Bạn còn 18 ngày dùng thử.", valX, y, valW, 26);
        cardContent.add(lblKeyVal);

        // 4. PHIÊN BẢN:
        y += gap;
        cardContent.add(createRowLabel("PHIÊN BẢN:", lblX, y, lblW, 26));
        lblEditionVal = createRowValue("Dùng thử", valX, y, valW, 26);
        cardContent.add(lblEditionVal);

        card.add(cardContent, BorderLayout.CENTER);

        // Bottom of Card: (C) by HN & Email: hoanngoc@techpos.vn
        JPanel cardFooter = new JPanel(new BorderLayout());
        cardFooter.setOpaque(false);
        cardFooter.setBorder(new EmptyBorder(10, 10, 2, 10));

        JLabel lblCopyright = new JLabel("(C) by HN");
        lblCopyright.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCopyright.setForeground(new Color(20, 20, 20));

        JLabel lblEmail = new JLabel("Email: " + SUPPORT_EMAIL);
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmail.setForeground(new Color(20, 20, 20));

        cardFooter.add(lblCopyright, BorderLayout.WEST);
        cardFooter.add(lblEmail, BorderLayout.EAST);
        card.add(cardFooter, BorderLayout.SOUTH);

        cardWrapper.add(card, BorderLayout.CENTER);
        return cardWrapper;
    }

    private JLabel createRowLabel(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(20, 20, 20));
        lbl.setBounds(x, y, w, h);
        return lbl;
    }

    private JLabel createRowValue(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(20, 20, 20));
        lbl.setBounds(x, y, w, h);
        return lbl;
    }

    /** Bottom Footer Bar with [ĐĂNG KÝ] (left) and [THOÁT] (right) */
    private JPanel createFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(0, 48));

        // Left button: ĐĂNG KÝ
        JButton btnRegister = new JButton("ĐĂNG KÝ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_REGISTER_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setForeground(TITLE_RED);
        btnRegister.setPreferredSize(new Dimension(140, 36));
        btnRegister.setOpaque(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> {
            if (openRegisterAction != null) openRegisterAction.run();
        });

        // Right button: THOÁT
        JButton btnExit = new JButton("THOÁT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_EXIT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExit.setForeground(Color.WHITE);
        btnExit.setPreferredSize(new Dimension(100, 36));
        btnExit.setOpaque(false);
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> {
            if (backAction != null) backAction.run();
        });

        footer.add(btnRegister, BorderLayout.WEST);
        footer.add(btnExit, BorderLayout.EAST);
        return footer;
    }

    public void refreshLicenseData() {
        LicenseService.LicenseStatus status = licenseService.getStatus();
        if (status.registered()) {
            lblOwnerVal.setText(status.owner().isEmpty() ? "Bản quyền chính thức" : status.owner());
            lblOrgVal.setText(status.organization().isEmpty() ? "Bản quyền chính thức" : status.organization());
            lblKeyVal.setText(status.registrationKey());
            lblEditionVal.setText("Bản quyền chính thức");
        } else {
            lblOwnerVal.setText(status.owner().isEmpty() ? "Chưa đăng ký" : status.owner());
            lblOrgVal.setText(status.organization().isEmpty() ? "Chưa đăng ký" : status.organization());
            lblKeyVal.setText("Bạn còn " + status.remainingTrialDays() + " ngày dùng thử.");
            lblEditionVal.setText("Dùng thử");
        }
    }

    public JLabel getLblOwnerVal() { return lblOwnerVal; }
    public JLabel getLblOrgVal() { return lblOrgVal; }
    public JLabel getLblKeyVal() { return lblKeyVal; }
    public JLabel getLblEditionVal() { return lblEditionVal; }
}
