package kazuko.ex.ui.hoadon;

import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NhapSoLuongDialog extends JDialog {

    private final MatHang matHang;
    private final double donGia;
    private final String donViTinh;
    private final double chietKhauMacDinh;
    private final double soLuongMacDinh;

    private JTextField txtSoLuong;
    private JTextField txtChietKhau;
    private boolean confirmed = false;
    private double resultSoLuong = 1.0;
    private double resultChietKhau = 0.0;

    public NhapSoLuongDialog(Window parent, MatHang matHang, double donGia, String donViTinh, double soLuongMacDinh, double chietKhauMacDinh) {
        super(parent, "Nhập số lượng", ModalityType.APPLICATION_MODAL);
        this.matHang = matHang;
        this.donGia = donGia;
        this.donViTinh = donViTinh != null ? donViTinh : (matHang.getTenDonViTinh() != null ? matHang.getTenDonViTinh() : "");
        this.soLuongMacDinh = soLuongMacDinh > 0 ? soLuongMacDinh : 1.0;
        this.chietKhauMacDinh = chietKhauMacDinh >= 0 ? chietKhauMacDinh : 0.0;

        initComponents();
    }

    private void initComponents() {
        setSize(420, 290);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 235)),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblTitle = new JLabel("Nhập số lượng cho mặt hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(40, 40, 40));
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Body Panel
        JPanel pnlBody = new JPanel(null);
        pnlBody.setBackground(new Color(205, 225, 250)); // Light blue background like Screenshot 2

        // Product Name & Price in large blue text
        String codeAndName = (matHang != null ? (matHang.getCode() != null ? matHang.getCode() : "") + " - " + (matHang.getName() != null ? matHang.getName() : "") : "");
        JLabel lblProdName = new JLabel(codeAndName);
        lblProdName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblProdName.setForeground(new Color(15, 35, 120));
        lblProdName.setBounds(25, 15, 360, 25);
        pnlBody.add(lblProdName);

        JLabel lblPrice = new JLabel(CurrencyUtil.format(donGia));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblPrice.setForeground(new Color(15, 35, 120));
        lblPrice.setBounds(25, 45, 360, 25);
        pnlBody.add(lblPrice);

        // Field Số lượng
        JLabel lblSl = new JLabel("Số lượng:");
        lblSl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSl.setBounds(25, 85, 80, 25);
        pnlBody.add(lblSl);

        txtSoLuong = new JTextField(String.valueOf(soLuongMacDinh == (long) soLuongMacDinh ? String.format("%d", (long) soLuongMacDinh) : String.valueOf(soLuongMacDinh)));
        txtSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSoLuong.setBounds(110, 85, 80, 25);
        pnlBody.add(txtSoLuong);

        JLabel lblDvt = new JLabel(donViTinh);
        lblDvt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDvt.setBounds(198, 85, 80, 25);
        pnlBody.add(lblDvt);

        // Field CK (%)
        JLabel lblCk = new JLabel("CK (%):");
        lblCk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCk.setBounds(25, 120, 80, 25);
        pnlBody.add(lblCk);

        txtChietKhau = new JTextField(String.valueOf(chietKhauMacDinh == (long) chietKhauMacDinh ? String.format("%d", (long) chietKhauMacDinh) : String.valueOf(chietKhauMacDinh)));
        txtChietKhau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtChietKhau.setBounds(110, 120, 80, 25);
        pnlBody.add(txtChietKhau);

        // Buttons in body (Ghi dữ liệu, Thoát)
        JButton btnGhi = new JButton("Ghi dữ liệu");
        btnGhi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGhi.setBackground(Color.WHITE);
        btnGhi.setBounds(110, 160, 95, 26);
        btnGhi.addActionListener(e -> onConfirm());
        pnlBody.add(btnGhi);

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setBackground(Color.WHITE);
        btnThoat.setBounds(215, 160, 80, 26);
        btnThoat.addActionListener(e -> dispose());
        pnlBody.add(btnThoat);

        add(pnlBody, BorderLayout.CENTER);

        // Default focus and enter key
        SwingUtilities.invokeLater(() -> {
            txtSoLuong.requestFocusInWindow();
            txtSoLuong.selectAll();
        });

        // Key bindings: ENTER to confirm, ESC to cancel
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        getRootPane().registerKeyboardAction(e -> onConfirm(), enterKey, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void onConfirm() {
        try {
            double sl = Double.parseDouble(txtSoLuong.getText().trim().replace(",", "."));
            if (sl <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                txtSoLuong.requestFocusInWindow();
                return;
            }
            resultSoLuong = sl;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtSoLuong.requestFocusInWindow();
            return;
        }

        try {
            String ckStr = txtChietKhau.getText().trim().replace(",", ".");
            if (!ckStr.isEmpty()) {
                resultChietKhau = Double.parseDouble(ckStr);
            } else {
                resultChietKhau = 0.0;
            }
        } catch (Exception e) {
            resultChietKhau = 0.0;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public double getResultSoLuong() { return resultSoLuong; }
    public double getResultChietKhau() { return resultChietKhau; }
}
