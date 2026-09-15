package kazuko.ex.ui.touch;

import kazuko.ex.model.dathang.KhachDatHangChiTiet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Hộp thoại "ĐIỀU CHỈNH" mặt hàng đặt theo chuẩn 100% Hình bổ sung 2.
 * - Mặt hàng: nhãn vàng nhạt, chỉ đọc
 * - Số lượng: ô trắng + nút máy tính [🖩]
 * - ĐVT: nhãn vàng nhạt, chỉ đọc
 * - Giảm giá %: ô trắng + nút máy tính [🖩]
 * - Thành tiền: ô vàng nhạt, tự động tính toán
 * - Nút: CHẤP NHẬN, THOÁT
 */
public class TouchDieuChinhMatHangDatDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER_BG = new Color(79, 139, 169);
    private static final Color YELLOW_BG = new Color(254, 255, 204); // Vàng nhạt như hình
    private static final Color GREEN_BTN = new Color(0, 105, 0);
    private static final Color RED_BTN = new Color(155, 0, 0);
    private static final Color TEAL_BTN = new Color(0, 79, 76);

    private final KhachDatHangChiTiet chiTiet;
    private double currentSoLuong;
    private double currentDonGia;
    private double currentGiamGia;
    private double currentThanhTien;

    private final JLabel lblTenHang = new JLabel();
    private final JLabel lblSoLuong = new JLabel("", SwingConstants.RIGHT);
    private final JLabel lblDVT = new JLabel();
    private final JLabel lblGiamGia = new JLabel("", SwingConstants.RIGHT);
    private final JLabel lblThanhTien = new JLabel("", SwingConstants.RIGHT);

    private boolean confirmed = false;

    public TouchDieuChinhMatHangDatDialog(Window owner, KhachDatHangChiTiet chiTiet) {
        super(owner, "ĐIỀU CHỈNH", ModalityType.APPLICATION_MODAL);
        this.chiTiet = chiTiet;
        this.currentSoLuong = chiTiet != null ? Math.max(1, chiTiet.getSoLuong()) : 1;
        this.currentDonGia = chiTiet != null ? chiTiet.getDonGia() : 0;
        this.currentGiamGia = chiTiet != null ? chiTiet.getTiLeGiamGia() : 0;
        recalcThanhTien();

        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        updateValuesDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER_BG, 2));
        root.setPreferredSize(new Dimension(820, 440));

        // 1. Header
        JLabel lblTitle = new JLabel("ĐIỀU CHỈNH", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(HEADER_BG);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setPreferredSize(new Dimension(820, 36));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Body form (GridBagLayout các dòng đúng chuẩn Hình bổ sung 2)
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.BOTH;

        // Dòng 1: Mặt hàng
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.15;
        form.add(createFieldLabel("Mặt hàng"), gbc);

        lblTenHang.setOpaque(true);
        lblTenHang.setBackground(YELLOW_BG);
        lblTenHang.setForeground(Color.BLACK);
        lblTenHang.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTenHang.setBorder(new EmptyBorder(0, 8, 0, 8));
        lblTenHang.setPreferredSize(new Dimension(0, 38));
        gbc.gridx = 1; gbc.weightx = 0.85;
        form.add(lblTenHang, gbc);

        // Dòng 2: Số lượng
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.weightx = 0.15;
        form.add(createFieldLabel("Số lượng"), gbc);

        JPanel rowSoLuong = new JPanel(new BorderLayout(4, 0));
        rowSoLuong.setOpaque(true);
        rowSoLuong.setBackground(Color.WHITE);
        rowSoLuong.setBorder(new LineBorder(new Color(180, 200, 200), 1));
        rowSoLuong.setPreferredSize(new Dimension(0, 38));

        lblSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSoLuong.setForeground(Color.BLACK);
        lblSoLuong.setBorder(new EmptyBorder(0, 8, 0, 8));
        rowSoLuong.add(lblSoLuong, BorderLayout.CENTER);

        JButton btnCalcSoLuong = createCalcButton();
        btnCalcSoLuong.addActionListener(e -> promptSoLuong());
        rowSoLuong.add(btnCalcSoLuong, BorderLayout.EAST);

        // Chạm vào ô cũng mở bàn phím số
        rowSoLuong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblSoLuong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblSoLuong.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { promptSoLuong(); }
        });

        gbc.gridx = 1; gbc.weightx = 0.85;
        form.add(rowSoLuong, gbc);

        // Dòng 3: ĐVT
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0.15;
        form.add(createFieldLabel("ĐVT"), gbc);

        lblDVT.setOpaque(true);
        lblDVT.setBackground(YELLOW_BG);
        lblDVT.setForeground(Color.BLACK);
        lblDVT.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDVT.setBorder(new EmptyBorder(0, 8, 0, 8));
        lblDVT.setPreferredSize(new Dimension(0, 38));
        gbc.gridx = 1; gbc.weightx = 0.85;
        form.add(lblDVT, gbc);

        // Dòng 4: Giảm giá %
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.weightx = 0.15;
        form.add(createFieldLabel("Giảm giá %"), gbc);

        JPanel rowGiamGia = new JPanel(new BorderLayout(4, 0));
        rowGiamGia.setOpaque(true);
        rowGiamGia.setBackground(Color.WHITE);
        rowGiamGia.setBorder(new LineBorder(new Color(180, 200, 200), 1));
        rowGiamGia.setPreferredSize(new Dimension(0, 38));

        lblGiamGia.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGiamGia.setForeground(Color.BLACK);
        lblGiamGia.setBorder(new EmptyBorder(0, 8, 0, 8));
        rowGiamGia.add(lblGiamGia, BorderLayout.CENTER);

        JButton btnCalcGiamGia = createCalcButton();
        btnCalcGiamGia.addActionListener(e -> promptGiamGia());
        rowGiamGia.add(btnCalcGiamGia, BorderLayout.EAST);

        rowGiamGia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblGiamGia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblGiamGia.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { promptGiamGia(); }
        });

        gbc.gridx = 1; gbc.weightx = 0.85;
        form.add(rowGiamGia, gbc);

        // Dòng 5: Thành tiền
        gbc.gridy = 4;
        gbc.gridx = 0; gbc.weightx = 0.15;
        form.add(createFieldLabel("Thành tiền"), gbc);

        lblThanhTien.setOpaque(true);
        lblThanhTien.setBackground(YELLOW_BG);
        lblThanhTien.setForeground(Color.BLACK);
        lblThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblThanhTien.setBorder(new EmptyBorder(0, 8, 0, 8));
        lblThanhTien.setPreferredSize(new Dimension(0, 38));
        gbc.gridx = 1; gbc.weightx = 0.85;
        form.add(lblThanhTien, gbc);

        root.add(form, BorderLayout.CENTER);

        // 3. Footer buttons: CHẤP NHẬN, THOÁT (bên phải như hình)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(820, 52));
        footer.setBorder(new EmptyBorder(0, 10, 10, 15));

        JButton btnChapNhan = new JButton("CHẤP NHẬN");
        btnChapNhan.setPreferredSize(new Dimension(130, 42));
        btnChapNhan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnChapNhan.setForeground(Color.WHITE);
        btnChapNhan.setBackground(GREEN_BTN);
        btnChapNhan.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btnChapNhan.setFocusPainted(false);
        btnChapNhan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChapNhan.addActionListener(e -> onConfirm());
        footer.add(btnChapNhan);

        JButton btnThoat = new JButton("THOÁT");
        btnThoat.setPreferredSize(new Dimension(110, 42));
        btnThoat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThoat.setForeground(Color.WHITE);
        btnThoat.setBackground(RED_BTN);
        btnThoat.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btnThoat.setFocusPainted(false);
        btnThoat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThoat.addActionListener(e -> dispose());
        footer.add(btnThoat);

        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JButton createCalcButton() {
        JButton btn = new JButton(TouchIcons.calculator(26, 26, Color.WHITE));
        btn.setPreferredSize(new Dimension(42, 36));
        btn.setBackground(TEAL_BTN);
        btn.setBorder(new LineBorder(new Color(0, 110, 110), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void recalcThanhTien() {
        currentThanhTien = currentSoLuong * currentDonGia * (1.0 - (currentGiamGia / 100.0));
    }

    private void updateValuesDisplay() {
        if (chiTiet != null) {
            String name = chiTiet.getTenHang() != null ? chiTiet.getTenHang() : chiTiet.getMaHang();
            lblTenHang.setText(name != null ? name : "");
            lblDVT.setText(chiTiet.getTenDonViTinh() != null ? chiTiet.getTenDonViTinh() : "");
        }
        lblSoLuong.setText(currentSoLuong == (long) currentSoLuong
                ? String.valueOf((long) currentSoLuong)
                : String.format("%,.2f", currentSoLuong));
        lblGiamGia.setText(currentGiamGia == (long) currentGiamGia
                ? String.valueOf((long) currentGiamGia)
                : String.format("%,.1f", currentGiamGia));
        lblThanhTien.setText(String.format("%,.0f", currentThanhTien));
    }

    private void promptSoLuong() {
        Double val = TouchNumberInputDialog.showDoubleDialog(this, "NHẬP SỐ LƯỢNG", currentSoLuong, 0.001, 999999.0);
        if (val != null && val > 0) {
            currentSoLuong = val;
            recalcThanhTien();
            updateValuesDisplay();
        }
    }

    private void promptGiamGia() {
        Integer val = TouchNumberInputDialog.showDialog(this, "NHẬP TỈ LỆ GIẢM GIÁ %", (int) Math.round(currentGiamGia), 0, 100);
        if (val != null) {
            currentGiamGia = val;
            recalcThanhTien();
            updateValuesDisplay();
        }
    }

    private void onConfirm() {
        if (chiTiet != null) {
            chiTiet.setSoLuong(currentSoLuong);
            chiTiet.setTiLeGiamGia(currentGiamGia);
            chiTiet.tinhThanhTien();
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static boolean showDialog(Window owner, KhachDatHangChiTiet chiTiet) {
        TouchDieuChinhMatHangDatDialog dlg = new TouchDieuChinhMatHangDatDialog(owner, chiTiet);
        dlg.setVisible(true);
        return dlg.isConfirmed();
    }
}
