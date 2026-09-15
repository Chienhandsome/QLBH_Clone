package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.MatHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Hộp thoại "CHỌN GIÁ BÁN" theo chuẩn 100% Hình 3.
 * - Header: CHỌN GIÁ BÁN (màu đỏ sẫm)
 * - 4 nút giá bán lớn (Giá đang áp dụng màu Teal #008080, các giá khác màu xám #777777)
 * - Nút Hủy bỏ màu đỏ ở đáy
 */
public class TouchChonGiaBanDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER_BG = new Color(139, 0, 0); // Đỏ sẫm
    private static final Color ACTIVE_PRICE_BG = new Color(0, 128, 128); // Xanh Teal
    private static final Color INACTIVE_PRICE_BG = new Color(119, 119, 119); // Xám
    private static final Color CANCEL_BG = new Color(204, 17, 17); // Đỏ

    private double selectedPrice;
    private boolean confirmed = false;

    public TouchChonGiaBanDialog(Window owner, MatHang matHang, double currentPrice) {
        super(owner, "CHỌN GIÁ BÁN", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        double p1 = (matHang != null && matHang.getGiaBan() > 0) ? matHang.getGiaBan() : currentPrice;
        double p2 = (matHang != null && matHang.getGiaBan2() > 0) ? matHang.getGiaBan2() : p1;
        double p3 = (matHang != null && matHang.getGiaBan3() > 0) ? matHang.getGiaBan3() : p1;
        double p4 = (matHang != null && matHang.getGiaBan4() > 0) ? matHang.getGiaBan4() : p1;

        double[] prices = {p1, p2, p3, p4};

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER_BG, 3));
        root.setPreferredSize(new Dimension(360, 440));

        // 1. Header: CHỌN GIÁ BÁN
        JLabel lblHeader = new JLabel("CHỌN GIÁ BÁN", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(HEADER_BG);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblHeader.setPreferredSize(new Dimension(360, 42));
        root.add(lblHeader, BorderLayout.NORTH);

        // 2. Thân chính: 4 nút giá lớn
        JPanel body = new JPanel(new GridLayout(4, 1, 0, 8));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 12, 8, 12));

        boolean matchedActive = false;
        for (double price : prices) {
            boolean isActive = (!matchedActive && Math.abs(price - currentPrice) < 0.001);
            if (isActive) matchedActive = true;

            JButton btnPrice = createPriceButton(price, isActive);
            btnPrice.addActionListener(e -> {
                selectedPrice = price;
                confirmed = true;
                dispose();
            });
            body.add(btnPrice);
        }
        root.add(body, BorderLayout.CENTER);

        // 3. Nút Hủy bỏ
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 12, 10, 12));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(0, 46));
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(CANCEL_BG);
        btnCancel.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        footer.add(btnCancel, BorderLayout.CENTER);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private JButton createPriceButton(double price, boolean isActive) {
        String text = String.format("%,.0f", price);
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 32));
        btn.setForeground(Color.WHITE);
        btn.setBackground(isActive ? ACTIVE_PRICE_BG : INACTIVE_PRICE_BG);
        btn.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public double getSelectedPrice() {
        return selectedPrice;
    }

    public static Double showDialog(Window owner, MatHang matHang, double currentPrice) {
        TouchChonGiaBanDialog dlg = new TouchChonGiaBanDialog(owner, matHang, currentPrice);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            return dlg.getSelectedPrice();
        }
        return null;
    }
}
