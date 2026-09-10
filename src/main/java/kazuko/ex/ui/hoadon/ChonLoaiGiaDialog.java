package kazuko.ex.ui.hoadon;

import kazuko.ex.model.banhang.GiaBanGanDayKhachHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChonLoaiGiaDialog extends JDialog {

    private final SystemConfig config;
    private final double gia1;
    private final double gia2;
    private final double gia3;
    private final double gia4;
    private final List<GiaBanGanDayKhachHang> recentPrices;

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");

    private int selectedPriceIndex = -1; // 0: Giá 1, 1: Giá 2, 2: Giá 3, 3: Giá 4
    private Double selectedPrice = null;
    private String selectedPriceName = null;
    private boolean applyToAll = false;
    private JCheckBox chkApplyToAll;

    public ChonLoaiGiaDialog(Window parent, SystemConfig config, double gia1, double gia2, double gia3, double gia4) {
        this(parent, config, gia1, gia2, gia3, gia4, Collections.emptyList());
    }

    public ChonLoaiGiaDialog(Window parent, SystemConfig config,
                            double gia1, double gia2, double gia3, double gia4,
                            List<GiaBanGanDayKhachHang> recentPrices) {
        super(parent, "Chọn loại giá bán", ModalityType.APPLICATION_MODAL);
        this.config = config;
        this.gia1 = normalizePrice(gia1);
        this.gia2 = normalizePrice(gia2);
        this.gia3 = normalizePrice(gia3);
        this.gia4 = normalizePrice(gia4);
        this.recentPrices = recentPrices != null ? new ArrayList<>(recentPrices) : Collections.emptyList();

        initComponents();
    }

    private void initComponents() {
        setSize(390, 340);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(new Color(205, 225, 250)); // Match Screenshot 4 background
        pnlMain.setBorder(new EmptyBorder(15, 18, 15, 18));

        String nameGia1 = "Giá bán";
        String nameGia2 = (config != null && config.getDienGiaiGia2() != null && !config.getDienGiaiGia2().trim().isEmpty()) ? config.getDienGiaiGia2().trim() : "Đơn giá 2";
        String nameGia3 = (config != null && config.getDienGiaiGia3() != null && !config.getDienGiaiGia3().trim().isEmpty()) ? config.getDienGiaiGia3().trim() : "Giá bán 3";
        String nameGia4 = (config != null && config.getDienGiaiGia4() != null && !config.getDienGiaiGia4().trim().isEmpty()) ? config.getDienGiaiGia4().trim() : "Giá bán 4";

        boolean enableGia2 = (config == null || config.isSuDungGia2()) && gia2 > 0;
        boolean enableGia3 = (config == null || config.isSuDungGia3()) && gia3 > 0;
        boolean enableGia4 = (config == null || config.isSuDungGia4()) && gia4 > 0;

        List<GiaBanGanDayKhachHang> displayRecentPrices = filterRecentPrices(recentPrices);

        JButton btnGia1 = createCatalogPriceButton(nameGia1, gia1, true, 0);
        JButton btnGia2 = createCatalogPriceButton(nameGia2, gia2, enableGia2, 1);
        JButton btnGia3 = createCatalogPriceButton(nameGia3, gia3, enableGia3, 2);
        JButton btnGia4 = createCatalogPriceButton(nameGia4, gia4, enableGia4, 3);
        JButton btnHuy = createCancelButton("Hủy bỏ");

        pnlMain.add(btnGia1);
        pnlMain.add(Box.createVerticalStrut(8));
        pnlMain.add(btnGia2);
        pnlMain.add(Box.createVerticalStrut(8));
        pnlMain.add(btnGia3);
        pnlMain.add(Box.createVerticalStrut(8));
        pnlMain.add(btnGia4);

        if (!displayRecentPrices.isEmpty()) {
            pnlMain.add(Box.createVerticalStrut(12));
            JLabel recentTitle = new JLabel("Giá khách đã mua gần đây");
            recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            recentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlMain.add(recentTitle);
            pnlMain.add(Box.createVerticalStrut(7));
            for (GiaBanGanDayKhachHang recentPrice : displayRecentPrices) {
                pnlMain.add(createRecentPriceButton(recentPrice));
                pnlMain.add(Box.createVerticalStrut(7));
            }
        }

        chkApplyToAll = new JCheckBox("Áp dụng cho tất cả mặt hàng trong đơn");
        chkApplyToAll.setOpaque(false);
        chkApplyToAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkApplyToAll.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(chkApplyToAll);

        pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(btnHuy);

        pnlMain.add(Box.createVerticalStrut(6));

        JLabel lblHint = new JLabel("<html><center><i>(Để kích hoạt các giá 2, 3, 4 bạn vào menu 'Quản trị / Cấu hình toàn hệ thống', tab 'Giá mặt hàng')</i></center></html>");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(new Color(60, 60, 60));
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblHint);

        setSize(390, Math.min(560, 340 + displayRecentPrices.size() * 38));
        setLocationRelativeTo(getParent());

        add(pnlMain, BorderLayout.CENTER);

        // ESC key to close
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JButton createCatalogPriceButton(String name, double price, boolean enabled, int priceIndex) {
        JButton btn = new JButton(name + ": " + PRICE_FORMAT.format(price));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setMaximumSize(new Dimension(310, 30));
        btn.setPreferredSize(new Dimension(310, 30));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setEnabled(enabled);

        if (enabled) {
            btn.addActionListener(e -> {
                selectedPriceIndex = priceIndex;
                selectedPrice = price;
                selectedPriceName = name;
                applyToAll = chkApplyToAll.isSelected();
                dispose();
            });
        }
        return btn;
    }

    private JButton createRecentPriceButton(GiaBanGanDayKhachHang recentPrice) {
        String invoice = recentPrice.getSoHoaDon() != null && !recentPrice.getSoHoaDon().trim().isEmpty()
                ? " - HĐ " + recentPrice.getSoHoaDon().trim() : "";
        String date = recentPrice.getNgayHoaDon() != null
                ? ", " + DateUtil.formatDate(recentPrice.getNgayHoaDon()) : "";
        JButton btn = new JButton(PRICE_FORMAT.format(recentPrice.getDonGia()) + invoice + date);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setMaximumSize(new Dimension(340, 30));
        btn.setPreferredSize(new Dimension(340, 30));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(245, 252, 240));
        btn.addActionListener(e -> {
            selectedPriceIndex = -1;
            selectedPrice = recentPrice.getDonGia();
            selectedPriceName = "Giá khách mua gần nhất";
            applyToAll = false;
            dispose();
        });
        return btn;
    }

    static List<GiaBanGanDayKhachHang> filterRecentPrices(List<GiaBanGanDayKhachHang> prices) {
        List<GiaBanGanDayKhachHang> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        if (prices == null) return result;
        for (GiaBanGanDayKhachHang price : prices) {
            if (price == null || price.getDonGia() <= 0 || !seen.add(priceKey(price.getDonGia()))) continue;
            result.add(price);
            if (result.size() == 3) break;
        }
        return result;
    }

    private static long priceKey(double price) {
        return Math.round(price);
    }

    private static double normalizePrice(double price) {
        return Math.round(price);
    }

    private JButton createCancelButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setMaximumSize(new Dimension(310, 30));
        btn.setPreferredSize(new Dimension(310, 30));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.addActionListener(e -> dispose());
        return btn;
    }

    public int getSelectedPriceIndex() { return selectedPriceIndex; }
    public double getSelectedPrice() { return selectedPrice != null ? selectedPrice : 0.0; }
    public boolean isConfirmed() { return selectedPrice != null; }
    public String getSelectedPriceName() { return selectedPriceName; }
    public boolean isApplyToAll() { return applyToAll; }
}
