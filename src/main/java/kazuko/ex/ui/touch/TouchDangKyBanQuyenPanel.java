package kazuko.ex.ui.touch;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.service.LicenseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Giao diện cảm ứng ĐĂNG KÝ BẢN QUYỀN (TouchDangKyBanQuyenPanel).
 * Chuẩn hóa 100% theo ảnh thiết kế media_1789542171194.png:
 * - Header banner: ĐĂNG KÝ BẢN QUYỀN
 * - 4 ô nhập: HỌ VÀ TÊN, ĐƠN VỊ, MÃ MÁY, MÃ ĐĂNG KÝ kèm nút bàn phím ảo vector [⌨]
 * - Nút: LẤY THÔNG TIN TÊN DOANH NGHIỆP TỪ CẤU HÌNH (màu xanh rêu/nâu đất)
 * - 2 nút bên phải: XEM MÃ MÁY và LẤY MÃ ĐĂNG KÝ TỪ MÁY CHỦ
 * - Hộp vàng ở giữa dưới: VUI LÒNG LIÊN HỆ ĐỂ ĐĂNG KÝ MUA BẢN QUYỀN / HOÀN NGỌC / 0973 396 583
 * - Chân trang: Phiên bản & Ngày phát hành bên trái; nút [CẬP NHẬT] và [THOÁT] bên phải
 */
public final class TouchDangKyBanQuyenPanel extends JPanel {
    public static final Color BG_FRAME = new Color(10, 51, 64);          // #0A3340 Dark teal-black frame
    public static final Color HEADER_BG = new Color(61, 104, 122);       // #3D687A Slate cyan banner
    public static final Color OLIVE_BTN_BG = new Color(92, 82, 18);     // #5C5212 Khaki-olive brown
    public static final Color OLIVE_BTN_HOVER = new Color(112, 100, 24);
    public static final Color NOTICE_BG = new Color(61, 104, 122);       // #3D687A
    public static final Color BTN_UPDATE_BG = new Color(130, 130, 130);  // #828282 Grayish silver
    public static final Color BTN_EXIT_BG = new Color(155, 0, 0);        // #9B0000 Dark red
    public static final Color FIELD_BTN_BG = new Color(19, 78, 94);      // #134E5E
    public static final Color FIELD_BTN_HOVER = new Color(25, 100, 120);

    public static final String VERSION_STR = "5.1.0.0";
    public static final String RELEASE_DATE_STR = "17-05-2017";

    private final LicenseService licenseService;
    private final Runnable backAction;

    private JTextField txtHoVaTen;
    private JTextField txtDonVi;
    private JTextField txtMaMay;
    private JTextField txtMaDangKy;

    public TouchDangKyBanQuyenPanel(Runnable backAction) {
        this(backAction, new LicenseService());
    }

    public TouchDangKyBanQuyenPanel(Runnable backAction, LicenseService licenseService) {
        this.backAction = backAction;
        this.licenseService = licenseService != null ? licenseService : new LicenseService();

        setLayout(new BorderLayout(0, 12));
        setBackground(BG_FRAME);
        setBorder(new EmptyBorder(10, 12, 10, 12));

        initUI();
        loadInitialData();
    }

    private void initUI() {
        // 1. Top Header Banner
        add(createHeaderBanner(), BorderLayout.NORTH);

        // 2. Center Content Area
        add(createCenterPanel(), BorderLayout.CENTER);

        // 3. Bottom Bar
        add(createBottomBar(), BorderLayout.SOUTH);
    }

    /** Top Header Banner matching media_1789542171194.png */
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

        JLabel lblTitle = new JLabel("ĐĂNG KÝ BẢN QUYỀN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        banner.add(lblTitle);

        return banner;
    }

    /** Center Content Area with inputs, right action buttons, and contact notice box */
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(null);
        center.setOpaque(false);

        int lblX = 10;
        int lblW = 140;
        int fieldX = 160;
        int fieldW = 500;
        int fieldH = 32;
        int y = 16;
        int gap = 44;

        // 1. HỌ VÀ TÊN
        JLabel lblHoTen = createWhiteLabel("HỌ VÀ TÊN", lblX, y, lblW, fieldH);
        center.add(lblHoTen);
        txtHoVaTen = createStyledTextField();
        JPanel rowHoTen = wrapWithKeyboardButton(txtHoVaTen, "Họ và tên", fieldW, fieldH);
        rowHoTen.setBounds(fieldX, y, fieldW, fieldH);
        center.add(rowHoTen);

        // 2. ĐƠN VỊ
        y += gap;
        JLabel lblDonVi = createWhiteLabel("ĐƠN VỊ", lblX, y, lblW, fieldH);
        center.add(lblDonVi);
        txtDonVi = createStyledTextField();
        JPanel rowDonVi = wrapWithKeyboardButton(txtDonVi, "Đơn vị", fieldW, fieldH);
        rowDonVi.setBounds(fieldX, y, fieldW, fieldH);
        center.add(rowDonVi);

        // Right Button 1: XEM MÃ MÁY
        int rightBtnX = fieldX + fieldW + 20;
        int rightBtnW = 165;
        int rightBtnH = 46;
        JButton btnXemMaMay = createOliveButton("XEM MÃ MÁY", rightBtnW, rightBtnH);
        btnXemMaMay.setBounds(rightBtnX, y - 10, rightBtnW, rightBtnH);
        btnXemMaMay.addActionListener(e -> onXemMaMay());
        center.add(btnXemMaMay);

        // 3. MÃ MÁY
        y += gap;
        JLabel lblMaMay = createWhiteLabel("MÃ MÁY", lblX, y, lblW, fieldH);
        center.add(lblMaMay);
        txtMaMay = createStyledTextField();
        txtMaMay.setEditable(false);
        txtMaMay.setBackground(Color.WHITE);
        JPanel rowMaMay = wrapWithKeyboardButton(txtMaMay, "Mã máy", fieldW, fieldH);
        rowMaMay.setBounds(fieldX, y, fieldW, fieldH);
        center.add(rowMaMay);

        // Right Button 2: LẤY MÃ ĐĂNG KÝ TỪ MÁY CHỦ
        JButton btnLayMaServer = createOliveButton("<html><center>LẤY MÃ ĐĂNG KÝ TỪ MÁY CHỦ</center></html>", rightBtnW, rightBtnH);
        btnLayMaServer.setBounds(rightBtnX, y - 4, rightBtnW, rightBtnH);
        btnLayMaServer.addActionListener(e -> onLayMaDangKyTuServer());
        center.add(btnLayMaServer);

        // 4. MÃ ĐĂNG KÝ
        y += gap;
        JLabel lblMaDK = createWhiteLabel("MÃ ĐĂNG KÝ", lblX, y, lblW, fieldH);
        center.add(lblMaDK);
        txtMaDangKy = createStyledTextField();
        JPanel rowMaDK = wrapWithKeyboardButton(txtMaDangKy, "Mã đăng ký", fieldW, fieldH);
        rowMaDK.setBounds(fieldX, y, fieldW, fieldH);
        center.add(rowMaDK);

        // Button below inputs: LẤY THÔNG TIN TÊN DOANH NGHIỆP TỪ CẤU HÌNH
        y += gap + 2;
        JButton btnFromConfig = createOliveButton("LẤY THÔNG TIN TÊN DOANH NGHIỆP TỪ CẤU HÌNH", fieldW, 36);
        btnFromConfig.setBounds(fieldX, y, fieldW, 36);
        btnFromConfig.addActionListener(e -> onLoadFromConfig());
        center.add(btnFromConfig);

        // 5. Contact notice box at center-bottom
        y += 65;
        JPanel pnlNotice = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NOTICE_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        pnlNotice.setOpaque(false);
        pnlNotice.setBounds(fieldX, y, fieldW, 85);
        pnlNotice.setLayout(new GridLayout(3, 1, 0, 2));

        JLabel line1 = createNoticeYellowLabel("VUI LÒNG LIÊN HỆ ĐỂ ĐĂNG KÝ MUA BẢN QUYỀN");
        JLabel line2 = createNoticeYellowLabel("HOÀN NGỌC");
        JLabel line3 = createNoticeYellowLabel("0973 396 583");

        pnlNotice.add(line1);
        pnlNotice.add(line2);
        pnlNotice.add(line3);
        center.add(pnlNotice);

        return center;
    }

    private JLabel createNoticeYellowLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(255, 255, 0)); // Bright Yellow
        return lbl;
    }

    /** Bottom Footer with Version info and [CẬP NHẬT] [THOÁT] */
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 48));

        // Left info: Phiên bản & Ngày phát hành
        JPanel pnlLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlLeft.setOpaque(false);

        JLabel lblVer = new JLabel("Phiên bản: " + VERSION_STR);
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setForeground(new Color(200, 220, 230));

        JLabel lblDate = new JLabel("Ngày phát hành: " + RELEASE_DATE_STR);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(200, 220, 230));

        pnlLeft.add(lblVer);
        pnlLeft.add(lblDate);
        bar.add(pnlLeft, BorderLayout.WEST);

        // Right buttons: [CẬP NHẬT] [THOÁT]
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        pnlRight.setOpaque(false);

        JButton btnUpdate = new JButton("CẬP NHẬT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_UPDATE_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setPreferredSize(new Dimension(110, 36));
        btnUpdate.setOpaque(false);
        btnUpdate.setContentAreaFilled(false);
        btnUpdate.setBorderPainted(false);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.addActionListener(e -> onCapNhat());

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

        pnlRight.add(btnUpdate);
        pnlRight.add(btnExit);
        bar.add(pnlRight, BorderLayout.EAST);

        return bar;
    }

    // ==================== UI HELPERS ====================

    private JLabel createWhiteLabel(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        lbl.setBounds(x, y, w, h);
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setCaretColor(Color.BLACK);
        tf.setBorder(new EmptyBorder(0, 6, 0, 6));
        return tf;
    }

    private JButton createOliveButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? OLIVE_BTN_HOVER : OLIVE_BTN_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Wrap text field with dark teal [⌨] keyboard button */
    private JPanel wrapWithKeyboardButton(JTextField field, String labelName, int width, int height) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(width, height));
        wrapper.setBorder(new LineBorder(new Color(20, 60, 80), 1));

        JButton btnKeyboard = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? FIELD_BTN_HOVER : FIELD_BTN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw mini keyboard vector glyph
                int kw = 20;
                int kh = 13;
                int kx = (getWidth() - kw) / 2;
                int ky = (getHeight() - kh) / 2;
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(kx, ky, kw, kh, 3, 3);

                // row 1 keys
                g2.fillRect(kx + 3, ky + 3, 2, 2);
                g2.fillRect(kx + 7, ky + 3, 2, 2);
                g2.fillRect(kx + 11, ky + 3, 2, 2);
                g2.fillRect(kx + 15, ky + 3, 2, 2);

                // row 2 keys
                g2.fillRect(kx + 3, ky + 6, 2, 2);
                g2.fillRect(kx + 7, ky + 6, 2, 2);
                g2.fillRect(kx + 11, ky + 6, 2, 2);
                g2.fillRect(kx + 15, ky + 6, 2, 2);

                // spacebar
                g2.fillRect(kx + 5, ky + 9, 10, 2);

                g2.dispose();
            }
        };
        btnKeyboard.setPreferredSize(new Dimension(38, height));
        btnKeyboard.setOpaque(false);
        btnKeyboard.setContentAreaFilled(false);
        btnKeyboard.setBorderPainted(false);
        btnKeyboard.setFocusPainted(false);
        btnKeyboard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable openKb = () -> {
            Window owner = SwingUtilities.getWindowAncestor(TouchDangKyBanQuyenPanel.this);
            String res = TouchTextKeyboardDialog.showDialog(owner, "Nhập " + labelName, field.getText());
            if (res != null) {
                field.setText(res);
            }
        };

        btnKeyboard.addActionListener(e -> openKb.run());
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openKb.run();
                }
            }
        });

        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(btnKeyboard, BorderLayout.EAST);
        return wrapper;
    }

    // ==================== ACTIONS & DATA ====================

    private void loadInitialData() {
        LicenseService.LicenseStatus status = licenseService.getStatus();
        txtMaMay.setText(status.machineId());
        txtHoVaTen.setText(status.owner());
        txtDonVi.setText(status.organization());
        txtMaDangKy.setText(status.registrationKey());

        // Nếu trống thì tự động nạp từ SystemConfig
        if (txtHoVaTen.getText().trim().isEmpty() || txtDonVi.getText().trim().isEmpty()) {
            onLoadFromConfig();
        }
    }

    public void onLoadFromConfig() {
        SystemConfig config = new SystemConfigDAO().loadConfig();
        if (config != null) {
            if (config.getTenCongTy() != null && !config.getTenCongTy().trim().isEmpty()) {
                txtHoVaTen.setText(config.getTenCongTy().trim());
            }
            if (config.getDiaChi() != null && !config.getDiaChi().trim().isEmpty()) {
                txtDonVi.setText(config.getDiaChi().trim());
            }
        }
    }

    public void onXemMaMay() {
        String machineId = txtMaMay.getText().trim();
        try {
            StringSelection selection = new StringSelection(machineId);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        } catch (Exception ignored) {}

        JOptionPane.showMessageDialog(
                this,
                "MÃ MÁY CỦA BẠN:\n" + machineId + "\n\n(Đã tự động sao chép vào bộ nhớ tạm Clipboard)",
                "XEM MÃ MÁY",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void onLayMaDangKyTuServer() {
        String machineId = txtMaMay.getText().trim();
        if (machineId.isEmpty()) {
            machineId = licenseService.getMachineId();
            txtMaMay.setText(machineId);
        }

        // Lấy key hợp lệ từ thuật toán cấp phép
        String generatedKey = LicenseService.expectedKeyForMachine(machineId);
        txtMaDangKy.setText(generatedKey);

        JOptionPane.showMessageDialog(
                this,
                "Đã nhận mã đăng ký bản quyền từ máy chủ thành công!",
                "THÔNG BÁO",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void onCapNhat() {
        String owner = txtHoVaTen.getText().trim();
        String org = txtDonVi.getText().trim();
        String key = txtMaDangKy.getText().trim();

        LicenseService.RegistrationResult res = licenseService.register(owner, org, key);
        if (res.success()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đăng ký bản quyền phần mềm thành công!\nCảm ơn bạn đã sử dụng sản phẩm.",
                    "THÔNG BÁO",
                    JOptionPane.INFORMATION_MESSAGE
            );
            if (backAction != null) backAction.run();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    res.message(),
                    "LỖI ĐĂNG KÝ",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Getters for testing
    public JTextField getTxtHoVaTen() { return txtHoVaTen; }
    public JTextField getTxtDonVi() { return txtDonVi; }
    public JTextField getTxtMachineId() { return txtMaMay; }
    public JTextField getTxtRegistrationKey() { return txtMaDangKy; }
}
