package kazuko.ex.ui.touch;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Hộp thoại cảm ứng "XÁC NHẬN THANH TOÁN" chuẩn 100% Hình 2.
 * - Cột trái: 7 phím tiền mặt nhanh xanh lá đậm (10k, 20k, 50k, 100k, 200k, 500k, 1,000k)
 * - Cột giữa: TỔNG CỘNG (đỏ), KHÁCH ĐƯA (trắng + nút xóa lùi cam [ ◄ ]), THẺ ATM, VISA (trắng), TRẢ LẠI (xanh dương nhạt)
 * - Cột phải: Bàn phím số Numpad cảm ứng (1-9, 0, 000) và 4 nút tác vụ:
 *   + IN TẠM TÍNH (nâu/ô liu)
 *   + ĐÓNG BILL VÀ IN (tím sẫm)
 *   + ĐÓNG BILL KHÔNG IN (xanh lá đậm)
 *   + HỦY BỎ (đỏ sẫm)
 */
public class TouchXacNhanThanhToanDialog extends JDialog {

    public enum ResultAction {
        CONFIRM_AND_PRINT,
        CONFIRM_NO_PRINT,
        PRINT_TEMP,
        CANCEL
    }

    private enum ActiveField {
        KHACH_DUA,
        THE_ATM
    }

    private static final Color BG_DARK = new Color(8, 61, 86);         // #083D56
    private static final Color HEADER_BG = new Color(74, 130, 158);     // #4A829E
    private static final Color CARD_BG = new Color(84, 125, 150);       // #547D96
    private static final Color QUICK_CASH_BG = new Color(0, 80, 32);    // #005020
    private static final Color TOTAL_BG = new Color(255, 0, 0);         // #FF0000
    private static final Color CHANGE_BG = new Color(0, 191, 255);      // #00BFFF
    private static final Color BACKSPACE_BG = new Color(255, 140, 0);   // #FF8C00
    private static final Color NUMPAD_BG = new Color(26, 72, 95);       // #1A485F
    private static final Color NUMPAD_BORDER = new Color(50, 102, 128); // #326680

    private static final Color BTN_PRINT_TEMP_BG = new Color(97, 91, 28);    // #615B1C
    private static final Color BTN_CONFIRM_PRINT_BG = new Color(74, 11, 78); // #4A0B4E
    private static final Color BTN_CONFIRM_NO_PRINT_BG = new Color(15, 78, 31); // #0F4E1F
    private static final Color BTN_CANCEL_BG = new Color(138, 14, 14);       // #8A0E0E

    private final DonHang donHang;
    private final double tongTien;
    private final boolean choPhepInTamTinh;

    private double tienKhachDua;
    private double tienATM;
    private double tienTraLai;

    private JLabel lblTongCong;
    private JLabel lblKhachDua;
    private JLabel lblTheATM;
    private JLabel lblTraLai;

    private JPanel pnlKhachDuaContainer;
    private JPanel pnlTheATMContainer;

    private ActiveField activeField = ActiveField.KHACH_DUA;
    private boolean isFirstInputKhachDua = true;
    private boolean isFirstInputATM = true;

    private ResultAction resultAction = ResultAction.CANCEL;

    public TouchXacNhanThanhToanDialog(Window parent, DonHang donHang, boolean choPhepInTamTinh) {
        super(parent, "XÁC NHẬN THANH TOÁN", ModalityType.APPLICATION_MODAL);
        this.donHang = donHang;
        this.tongTien = donHang != null ? donHang.getTongCong() : 0.0;
        this.choPhepInTamTinh = choPhepInTamTinh;
        this.tienKhachDua = this.tongTien;
        this.tienATM = 0.0;

        setUndecorated(true);
        initComponents();
        updateActiveFieldHighlight();
        recalculate();
    }

    private void initComponents() {
        setSize(980, 560);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(BG_DARK);
        root.setBorder(new LineBorder(HEADER_BG, 3));

        // 1. Header: XÁC NHẬN THANH TOÁN
        JLabel lblHeader = new JLabel("XÁC NHẬN THANH TOÁN", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(HEADER_BG);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblHeader.setPreferredSize(new Dimension(980, 42));
        root.add(lblHeader, BorderLayout.NORTH);

        // 2. Thân chính 3 cột
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(4, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);

        // Cột 1: 7 Phím tiền mặt nhanh (Trái)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.16;
        gbc.weighty = 1.0;
        pnlBody.add(createQuickCashColumn(), gbc);

        // Cột 2: Bảng số tiền (Giữa)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.44;
        gbc.weighty = 1.0;
        pnlBody.add(createAmountDetailsColumn(), gbc);

        // Cột 3: Numpad & Nút hành động (Phải)
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.40;
        gbc.weighty = 1.0;
        pnlBody.add(createNumpadAndActionsColumn(), gbc);

        root.add(pnlBody, BorderLayout.CENTER);
        setContentPane(root);
    }

    /**
     * Cột 1: 7 Phím tiền mặt nhanh xanh lá đậm
     */
    private JPanel createQuickCashColumn() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 0, 8));
        panel.setOpaque(false);

        double[] denominations = {10000, 20000, 50000, 100000, 200000, 500000, 1000000};
        for (double val : denominations) {
            JButton btn = new JButton(CurrencyUtil.formatMoney(val));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 17));
            btn.setForeground(Color.WHITE);
            btn.setBackground(QUICK_CASH_BG);
            btn.setFocusPainted(false);
            btn.setBorder(new LineBorder(new Color(40, 140, 60), 1));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> onQuickCashClicked(val));
            panel.add(btn);
        }
        return panel;
    }

    /**
     * Cột 2: Bảng số tiền trung tâm
     */
    private JPanel createAmountDetailsColumn() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. TỔNG CỘNG (Đỏ)
        JPanel rowTongCong = new JPanel(new BorderLayout(8, 0));
        rowTongCong.setOpaque(false);
        JLabel lblLblTongCong = new JLabel("TỔNG CỘNG");
        lblLblTongCong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLblTongCong.setForeground(Color.WHITE);
        lblLblTongCong.setPreferredSize(new Dimension(120, 40));
        rowTongCong.add(lblLblTongCong, BorderLayout.WEST);

        lblTongCong = new JLabel(CurrencyUtil.formatMoney(tongTien), SwingConstants.RIGHT);
        lblTongCong.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTongCong.setForeground(Color.WHITE);
        lblTongCong.setBackground(TOTAL_BG);
        lblTongCong.setOpaque(true);
        lblTongCong.setBorder(new EmptyBorder(0, 12, 0, 16));
        rowTongCong.add(lblTongCong, BorderLayout.CENTER);
        panel.add(rowTongCong);

        // 2. KHÁCH ĐƯA (Trắng + Xóa lùi cam)
        JPanel rowKhachDua = new JPanel(new BorderLayout(8, 0));
        rowKhachDua.setOpaque(false);
        JLabel lblLblKhachDua = new JLabel("KHÁCH ĐƯA");
        lblLblKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLblKhachDua.setForeground(Color.WHITE);
        lblLblKhachDua.setPreferredSize(new Dimension(120, 40));
        rowKhachDua.add(lblLblKhachDua, BorderLayout.WEST);

        JPanel pnlKhachDuaWithBackspace = new JPanel(new BorderLayout(6, 0));
        pnlKhachDuaWithBackspace.setOpaque(false);

        pnlKhachDuaContainer = new JPanel(new BorderLayout());
        pnlKhachDuaContainer.setBackground(Color.WHITE);
        pnlKhachDuaContainer.setBorder(new LineBorder(Color.WHITE, 2));

        lblKhachDua = new JLabel(CurrencyUtil.formatMoney(tienKhachDua), SwingConstants.RIGHT);
        lblKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblKhachDua.setForeground(Color.BLACK);
        lblKhachDua.setOpaque(false);
        lblKhachDua.setBorder(new EmptyBorder(0, 10, 0, 14));
        lblKhachDua.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblKhachDua.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activeField = ActiveField.KHACH_DUA;
                updateActiveFieldHighlight();
            }
        });
        pnlKhachDuaContainer.add(lblKhachDua, BorderLayout.CENTER);
        pnlKhachDuaWithBackspace.add(pnlKhachDuaContainer, BorderLayout.CENTER);

        JButton btnBackspace = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;
                int[] x = {cx - 14, cx + 12, cx + 12};
                int[] y = {cy, cy - 14, cy + 14};
                g2.fillPolygon(x, y, 3);
                g2.dispose();
            }
        };
        btnBackspace.setBackground(BACKSPACE_BG);
        btnBackspace.setFocusPainted(false);
        btnBackspace.setBorder(null);
        btnBackspace.setPreferredSize(new Dimension(65, 40));
        btnBackspace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBackspace.addActionListener(e -> onBackspaceClicked());
        pnlKhachDuaWithBackspace.add(btnBackspace, BorderLayout.EAST);

        rowKhachDua.add(pnlKhachDuaWithBackspace, BorderLayout.CENTER);
        panel.add(rowKhachDua);

        // 3. THẺ ATM, VISA (Trắng)
        JPanel rowTheATM = new JPanel(new BorderLayout(8, 0));
        rowTheATM.setOpaque(false);
        JLabel lblLblTheATM = new JLabel("THẺ ATM, VISA");
        lblLblTheATM.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLblTheATM.setForeground(Color.WHITE);
        lblLblTheATM.setPreferredSize(new Dimension(120, 40));
        rowTheATM.add(lblLblTheATM, BorderLayout.WEST);

        pnlTheATMContainer = new JPanel(new BorderLayout());
        pnlTheATMContainer.setBackground(Color.WHITE);
        pnlTheATMContainer.setBorder(new LineBorder(Color.WHITE, 2));

        lblTheATM = new JLabel(CurrencyUtil.formatMoney(tienATM), SwingConstants.RIGHT);
        lblTheATM.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTheATM.setForeground(Color.BLACK);
        lblTheATM.setOpaque(false);
        lblTheATM.setBorder(new EmptyBorder(0, 10, 0, 14));
        lblTheATM.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblTheATM.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activeField = ActiveField.THE_ATM;
                updateActiveFieldHighlight();
            }
        });
        pnlTheATMContainer.add(lblTheATM, BorderLayout.CENTER);
        rowTheATM.add(pnlTheATMContainer, BorderLayout.CENTER);
        panel.add(rowTheATM);

        // 4. TRẢ LẠI (Xanh dương nhạt)
        JPanel rowTraLai = new JPanel(new BorderLayout(8, 0));
        rowTraLai.setOpaque(false);
        JLabel lblLblTraLai = new JLabel("TRẢ LẠI");
        lblLblTraLai.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLblTraLai.setForeground(Color.WHITE);
        lblLblTraLai.setPreferredSize(new Dimension(120, 40));
        rowTraLai.add(lblLblTraLai, BorderLayout.WEST);

        lblTraLai = new JLabel("0", SwingConstants.RIGHT);
        lblTraLai.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTraLai.setForeground(Color.BLACK);
        lblTraLai.setBackground(CHANGE_BG);
        lblTraLai.setOpaque(true);
        lblTraLai.setBorder(new EmptyBorder(0, 12, 0, 16));
        rowTraLai.add(lblTraLai, BorderLayout.CENTER);
        panel.add(rowTraLai);

        return panel;
    }

    /**
     * Cột 3: Numpad cảm ứng + 4 Nút hành động
     */
    private JPanel createNumpadAndActionsColumn() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Numpad 4 hàng
        JPanel pnlNumpad = new JPanel(new GridLayout(4, 3, 6, 6));
        pnlNumpad.setOpaque(false);
        pnlNumpad.setPreferredSize(new Dimension(340, 240));

        // Hàng 1: 1, 2, 3
        pnlNumpad.add(createNumpadButton("1"));
        pnlNumpad.add(createNumpadButton("2"));
        pnlNumpad.add(createNumpadButton("3"));

        // Hàng 2: 4, 5, 6
        pnlNumpad.add(createNumpadButton("4"));
        pnlNumpad.add(createNumpadButton("5"));
        pnlNumpad.add(createNumpadButton("6"));

        // Hàng 3: 7, 8, 9
        pnlNumpad.add(createNumpadButton("7"));
        pnlNumpad.add(createNumpadButton("8"));
        pnlNumpad.add(createNumpadButton("9"));

        // Hàng 4: 0, 000 (0 chiếm 1 ô, 000 có thể đặt 2 ô hoặc gridbag)
        pnlNumpad.add(createNumpadButton("0"));
        
        JButton btn000 = createNumpadButton("000");
        // Để 000 rộng chiếm 2 cột trong grid 3 cột:
        // Ta tạo panel con cho hàng 4
        panel.remove(pnlNumpad);

        JPanel pnlNumpadCustom = new JPanel(new GridBagLayout());
        pnlNumpadCustom.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[][] gridKeys = {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"}
        };
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                gbc.gridx = c;
                gbc.gridy = r;
                gbc.gridwidth = 1;
                pnlNumpadCustom.add(createNumpadButton(gridKeys[r][c]), gbc);
            }
        }
        // Hàng 4: 0 (1 cột), 000 (2 cột)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        pnlNumpadCustom.add(createNumpadButton("0"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        pnlNumpadCustom.add(createNumpadButton("000"), gbc);

        panel.add(pnlNumpadCustom, BorderLayout.NORTH);

        // 4 Nút thanh toán xếp dọc
        JPanel pnlActions = new JPanel(new GridLayout(4, 1, 0, 6));
        pnlActions.setOpaque(false);

        JButton btnPrintTemp = createActionButton("IN TẠM TÍNH", BTN_PRINT_TEMP_BG, e -> {
            resultAction = ResultAction.PRINT_TEMP;
            dispose();
        });
        btnPrintTemp.setVisible(choPhepInTamTinh);

        JButton btnConfirmAndPrint = createActionButton("ĐÓNG BILL VÀ IN", BTN_CONFIRM_PRINT_BG, e -> {
            resultAction = ResultAction.CONFIRM_AND_PRINT;
            dispose();
        });

        JButton btnConfirmNoPrint = createActionButton("ĐÓNG BILL KHÔNG IN", BTN_CONFIRM_NO_PRINT_BG, e -> {
            resultAction = ResultAction.CONFIRM_NO_PRINT;
            dispose();
        });

        JButton btnCancel = createActionButton("HỦY BỎ", BTN_CANCEL_BG, e -> {
            resultAction = ResultAction.CANCEL;
            dispose();
        });

        pnlActions.add(btnPrintTemp);
        pnlActions.add(btnConfirmAndPrint);
        pnlActions.add(btnConfirmNoPrint);
        pnlActions.add(btnCancel);

        panel.add(pnlActions, BorderLayout.CENTER);

        return panel;
    }

    private JButton createNumpadButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btn.setForeground(Color.WHITE);
        btn.setBackground(NUMPAD_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(NUMPAD_BORDER, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(80, 52));
        btn.addActionListener(e -> onNumpadClicked(text));
        return btn;
    }

    private JButton createActionButton(String text, Color bg, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(null);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(340, 44));
        btn.addActionListener(listener);
        return btn;
    }

    private void updateActiveFieldHighlight() {
        if (activeField == ActiveField.KHACH_DUA) {
            pnlKhachDuaContainer.setBorder(new LineBorder(new Color(0, 220, 255), 2));
            pnlTheATMContainer.setBorder(new LineBorder(Color.WHITE, 2));
        } else {
            pnlTheATMContainer.setBorder(new LineBorder(new Color(0, 220, 255), 2));
            pnlKhachDuaContainer.setBorder(new LineBorder(Color.WHITE, 2));
        }
    }

    private void onQuickCashClicked(double amount) {
        if (activeField == ActiveField.KHACH_DUA) {
            if (isFirstInputKhachDua) {
                tienKhachDua = amount;
                isFirstInputKhachDua = false;
            } else {
                tienKhachDua += amount;
            }
            lblKhachDua.setText(CurrencyUtil.formatMoney(tienKhachDua));
        } else {
            if (isFirstInputATM) {
                tienATM = amount;
                isFirstInputATM = false;
            } else {
                tienATM += amount;
            }
            lblTheATM.setText(CurrencyUtil.formatMoney(tienATM));
        }
        recalculate();
    }

    private void onNumpadClicked(String digit) {
        if (activeField == ActiveField.KHACH_DUA) {
            String curStr = isFirstInputKhachDua ? "" : String.valueOf((long) tienKhachDua);
            if (curStr.equals("0")) curStr = "";
            curStr += digit;
            try {
                tienKhachDua = Double.parseDouble(curStr);
            } catch (NumberFormatException ignored) {}
            isFirstInputKhachDua = false;
            lblKhachDua.setText(CurrencyUtil.formatMoney(tienKhachDua));
        } else {
            String curStr = isFirstInputATM ? "" : String.valueOf((long) tienATM);
            if (curStr.equals("0")) curStr = "";
            curStr += digit;
            try {
                tienATM = Double.parseDouble(curStr);
            } catch (NumberFormatException ignored) {}
            isFirstInputATM = false;
            lblTheATM.setText(CurrencyUtil.formatMoney(tienATM));
        }
        recalculate();
    }

    private void onBackspaceClicked() {
        if (activeField == ActiveField.KHACH_DUA) {
            String curStr = String.valueOf((long) tienKhachDua);
            if (curStr.length() > 1) {
                curStr = curStr.substring(0, curStr.length() - 1);
                tienKhachDua = Double.parseDouble(curStr);
            } else {
                tienKhachDua = 0;
            }
            isFirstInputKhachDua = false;
            lblKhachDua.setText(CurrencyUtil.formatMoney(tienKhachDua));
        } else {
            String curStr = String.valueOf((long) tienATM);
            if (curStr.length() > 1) {
                curStr = curStr.substring(0, curStr.length() - 1);
                tienATM = Double.parseDouble(curStr);
            } else {
                tienATM = 0;
            }
            isFirstInputATM = false;
            lblTheATM.setText(CurrencyUtil.formatMoney(tienATM));
        }
        recalculate();
    }

    private void recalculate() {
        double totalPaid = tienKhachDua + tienATM;
        tienTraLai = totalPaid - tongTien;
        if (tienTraLai < 0) {
            lblTraLai.setText("0");
        } else {
            lblTraLai.setText(CurrencyUtil.formatMoney(tienTraLai));
        }
    }

    public ResultAction getResultAction() {
        return resultAction;
    }

    public double getTienKhachDua() {
        return tienKhachDua;
    }

    public double getTienATM() {
        return tienATM;
    }

    public double getTienTraLai() {
        return tienTraLai;
    }
}