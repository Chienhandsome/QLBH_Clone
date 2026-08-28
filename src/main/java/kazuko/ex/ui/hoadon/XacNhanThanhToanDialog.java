package kazuko.ex.ui.hoadon;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class XacNhanThanhToanDialog extends JDialog {

    public enum ResultAction {
        CONFIRM_AND_PRINT,
        CONFIRM_NO_PRINT,
        PRINT_TEMP,
        CANCEL
    }

    private final DonHang donHang;
    private final double tongTien;
    private boolean isUpdatingNumbers = false;

    private JTextField txtTongTien;
    private JTextField txtKhachDua;
    private JTextField txtTienATM;
    private JTextField txtTienTraLai;
    private JCheckBox chkKhachHangNo;

    private ResultAction resultAction = ResultAction.CANCEL;

    public XacNhanThanhToanDialog(Window parent, DonHang donHang) {
        super(parent, "XÁC NHẬN THANH TOÁN", ModalityType.APPLICATION_MODAL);
        this.donHang = donHang;
        this.tongTien = donHang != null ? donHang.getTongCong() : 0.0;

        initComponents();
        recalculateChange();
    }

    private void initComponents() {
        setSize(630, 440);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel pnlMain = new JPanel(null);
        pnlMain.setBackground(new Color(215, 230, 245)); // Main light blue background

        // 1. Header Bar: "BẠN CÓ MUỐN THANH TOÁN KHÔNG?" & "In tạm tính (F8)"
        JLabel lblTitle = new JLabel("BẠN CÓ MUỐN THANH TOÁN KHÔNG?");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 40, 75));
        lblTitle.setBounds(20, 10, 320, 26);
        pnlMain.add(lblTitle);

        JButton btnInTamTinh = new JButton("In tạm tính (F8)");
        btnInTamTinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnInTamTinh.setIcon(IconHelper.getIcon("Preview.png", 16, 16));
        btnInTamTinh.setBackground(Color.WHITE);
        btnInTamTinh.setBounds(460, 10, 140, 26);
        btnInTamTinh.addActionListener(e -> {
            resultAction = ResultAction.PRINT_TEMP;
            dispose();
        });
        pnlMain.add(btnInTamTinh);

        // 2. Leather Wallet Graphic on Left
        JPanel pnlWallet = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Draw green money bills inside
                g2.setColor(new Color(90, 175, 110));
                g2.fillRoundRect(22, 10, 68, 45, 6, 6);
                g2.setColor(new Color(60, 145, 80));
                g2.drawRoundRect(22, 10, 68, 45, 6, 6);

                g2.setColor(new Color(130, 210, 150));
                g2.fillRoundRect(26, 6, 60, 40, 6, 6);

                // Draw leather wallet outer body
                GradientPaint gpLeather = new GradientPaint(10, 15, new Color(195, 135, 60), 75, 75, new Color(130, 80, 25));
                g2.setPaint(gpLeather);
                int[] xPoints = {15, 78, 70, 8};
                int[] yPoints = {35, 18, 85, 72};
                g2.fillPolygon(xPoints, yPoints, 4);

                // Leather fold
                GradientPaint gpFold = new GradientPaint(15, 40, new Color(220, 160, 80), 80, 90, new Color(155, 95, 35));
                g2.setPaint(gpFold);
                g2.fillRoundRect(12, 32, 72, 55, 12, 12);
                g2.setColor(new Color(110, 65, 20));
                g2.drawRoundRect(12, 32, 72, 55, 12, 12);

                // Stitching line
                g2.setColor(new Color(245, 215, 150));
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 3}, 0));
                g2.drawRoundRect(15, 35, 66, 49, 10, 10);

                g2.dispose();
            }
        };
        pnlWallet.setOpaque(false);
        pnlWallet.setBounds(15, 50, 95, 100);
        pnlMain.add(pnlWallet);

        // 3. Payment Rows (Labels & TextFields)
        // Row 1: TỔNG TIỀN
        JLabel lblTongTien = new JLabel("TỔNG TIỀN:");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setForeground(new Color(140, 20, 20));
        lblTongTien.setBounds(125, 52, 150, 28);
        pnlMain.add(lblTongTien);

        txtTongTien = new JTextField(CurrencyUtil.format(tongTien));
        txtTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtTongTien.setForeground(new Color(0, 45, 150));
        txtTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTongTien.setBackground(new Color(226, 248, 250)); // Light cyan
        txtTongTien.setEditable(false);
        txtTongTien.setBounds(280, 50, 320, 32);
        pnlMain.add(txtTongTien);

        // Row 2: Khách đưa
        JLabel lblKhachDua = new JLabel("Khách đưa:");
        lblKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblKhachDua.setForeground(new Color(140, 20, 20));
        lblKhachDua.setBounds(125, 92, 150, 28);
        pnlMain.add(lblKhachDua);

        txtKhachDua = new JTextField(CurrencyUtil.format(tongTien));
        txtKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtKhachDua.setForeground(new Color(0, 45, 150));
        txtKhachDua.setHorizontalAlignment(SwingConstants.RIGHT);
        txtKhachDua.setBackground(Color.WHITE);
        txtKhachDua.setBounds(280, 90, 320, 30);
        txtKhachDua.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { txtKhachDua.selectAll(); }
        });
        pnlMain.add(txtKhachDua);

        // Row 3: Thẻ ATM, Visa
        JLabel lblTheATM = new JLabel("Thẻ ATM, Visa:");
        lblTheATM.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTheATM.setForeground(new Color(140, 20, 20));
        lblTheATM.setBounds(125, 130, 150, 28);
        pnlMain.add(lblTheATM);

        txtTienATM = new JTextField("0");
        txtTienATM.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtTienATM.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTienATM.setBackground(Color.WHITE);
        txtTienATM.setBounds(280, 128, 320, 30);
        txtTienATM.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { txtTienATM.selectAll(); }
        });
        pnlMain.add(txtTienATM);

        // Row 4: Trả lại
        JLabel lblTraLai = new JLabel("Trả lại:");
        lblTraLai.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTraLai.setForeground(new Color(140, 20, 20));
        lblTraLai.setBounds(125, 168, 150, 28);
        pnlMain.add(lblTraLai);

        txtTienTraLai = new JTextField("0");
        txtTienTraLai.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtTienTraLai.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTienTraLai.setBackground(new Color(226, 248, 250)); // Light cyan
        txtTienTraLai.setEditable(false);
        txtTienTraLai.setBounds(280, 166, 320, 30);
        pnlMain.add(txtTienTraLai);

        // DocumentListeners for auto recalculation
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalculateChange(); }
            @Override public void removeUpdate(DocumentEvent e) { recalculateChange(); }
            @Override public void changedUpdate(DocumentEvent e) { recalculateChange(); }
        };
        txtKhachDua.getDocument().addDocumentListener(dl);
        txtTienATM.getDocument().addDocumentListener(dl);

        // 4. Instructions and Checkbox on lower left
        JLabel lblDesc1 = new JLabel("<html>- Bạn chọn <b>\"Đóng bill và in\"</b> để xác nhận thanh toán, in hóa đơn và đóng bill.</html>");
        lblDesc1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc1.setBounds(20, 215, 360, 28);
        pnlMain.add(lblDesc1);

        JLabel lblDesc2 = new JLabel("<html>- Bạn chọn <b>\"Đóng bill không in\"</b> để xác nhận thanh toán, KHÔNG in hóa đơn và đóng bill (tiết kiệm giấy).</html>");
        lblDesc2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc2.setBounds(20, 248, 360, 32);
        pnlMain.add(lblDesc2);

        JLabel lblDesc3 = new JLabel("<html>- Bạn chọn <b>\"Hủy bỏ\"</b> để thoát khỏi cửa sổ.</html>");
        lblDesc3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc3.setBounds(20, 284, 360, 20);
        pnlMain.add(lblDesc3);

        chkKhachHangNo = new JCheckBox("Khách hàng nợ");
        chkKhachHangNo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKhachHangNo.setOpaque(false);
        chkKhachHangNo.setBounds(18, 315, 200, 22);
        pnlMain.add(chkKhachHangNo);

        // 5. Action buttons on lower right
        JButton btnDongBillVaIn = new JButton("Đóng bill và in", IconHelper.getIcon("print-16.png", 16, 16));
        btnDongBillVaIn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDongBillVaIn.setBackground(Color.WHITE);
        btnDongBillVaIn.setBounds(390, 225, 210, 36);
        btnDongBillVaIn.addActionListener(e -> onConfirmAndPrint());
        pnlMain.add(btnDongBillVaIn);

        JButton btnDongBillKhongIn = new JButton("Đóng bill không in (F9)", IconHelper.getIcon("tsbPrint.Image.png", 16, 16));
        btnDongBillKhongIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDongBillKhongIn.setBackground(Color.WHITE);
        btnDongBillKhongIn.setBounds(390, 268, 210, 34);
        btnDongBillKhongIn.addActionListener(e -> onConfirmNoPrint());
        pnlMain.add(btnDongBillKhongIn);

        JButton btnHuyBo = new JButton("Hủy bỏ", IconHelper.getIcon("Cancelled.png", 16, 16));
        btnHuyBo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHuyBo.setBackground(Color.WHITE);
        btnHuyBo.setBounds(390, 310, 210, 34);
        btnHuyBo.addActionListener(e -> dispose());
        pnlMain.add(btnHuyBo);

        add(pnlMain, BorderLayout.CENTER);

        // Key bindings: F8, F9, ESC, ENTER
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), esc, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        getRootPane().registerKeyboardAction(e -> {
            resultAction = ResultAction.PRINT_TEMP;
            dispose();
        }, f8, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        getRootPane().registerKeyboardAction(e -> onConfirmNoPrint(), f9, JComponent.WHEN_IN_FOCUSED_WINDOW);

        txtKhachDua.addActionListener(e -> onConfirmAndPrint());
        txtTienATM.addActionListener(e -> onConfirmAndPrint());
    }

    private double parseNumber(JTextField tf) {
        if (tf == null) return 0.0;
        String text = tf.getText().trim().replace(".", "").replace(",", ".");
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void recalculateChange() {
        if (isUpdatingNumbers) return;
        isUpdatingNumbers = true;
        try {
            double kd = parseNumber(txtKhachDua);
            double atm = parseNumber(txtTienATM);
            double totalPaid = kd + atm;
            double change = totalPaid - tongTien;
            if (change < 0) change = 0;
            txtTienTraLai.setText(CurrencyUtil.format(change));
        } finally {
            isUpdatingNumbers = false;
        }
    }

    private void onConfirmAndPrint() {
        if (!validatePayment()) return;
        resultAction = ResultAction.CONFIRM_AND_PRINT;
        dispose();
    }

    private void onConfirmNoPrint() {
        if (!validatePayment()) return;
        resultAction = ResultAction.CONFIRM_NO_PRINT;
        dispose();
    }

    private boolean validatePayment() {
        double kd = parseNumber(txtKhachDua);
        double atm = parseNumber(txtTienATM);
        double totalPaid = kd + atm;

        if (totalPaid < tongTien && !chkKhachHangNo.isSelected()) {
            int ask = JOptionPane.showConfirmDialog(this,
                    "Số tiền khách thanh toán (" + CurrencyUtil.format(totalPaid) + " đ) chưa đủ so với tổng tiền (" + CurrencyUtil.format(tongTien) + " đ).\nBạn có muốn ghi nhận cho khách hàng nợ không?",
                    "Xác nhận ghi nợ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ask == JOptionPane.YES_OPTION) {
                chkKhachHangNo.setSelected(true);
                return true;
            }
            return false;
        }
        return true;
    }

    public ResultAction getResultAction() { return resultAction; }
    public double getTienKhachDua() { return parseNumber(txtKhachDua); }
    public double getTienATM() { return parseNumber(txtTienATM); }
    public double getTienTraLai() { return parseNumber(txtTienTraLai); }
    public boolean isKhachHangNo() { return chkKhachHangNo.isSelected(); }
}
