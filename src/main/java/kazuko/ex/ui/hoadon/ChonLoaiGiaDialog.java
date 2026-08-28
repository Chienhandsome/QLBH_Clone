package kazuko.ex.ui.hoadon;

import kazuko.ex.model.system.SystemConfig;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ChonLoaiGiaDialog extends JDialog {

    private final SystemConfig config;
    private final boolean hasGia1;
    private final boolean hasGia2;
    private final boolean hasGia3;
    private final boolean hasGia4;

    private int selectedPriceIndex = -1; // 0: Giá 1, 1: Giá 2, 2: Giá 3, 3: Giá 4
    private String selectedPriceName = null;
    private boolean applyToAll = false;
    private JCheckBox chkApplyToAll;

    public ChonLoaiGiaDialog(Window parent, SystemConfig config, boolean hasGia1, boolean hasGia2, boolean hasGia3, boolean hasGia4) {
        super(parent, "Chọn loại giá bán", ModalityType.APPLICATION_MODAL);
        this.config = config;
        this.hasGia1 = hasGia1;
        this.hasGia2 = hasGia2;
        this.hasGia3 = hasGia3;
        this.hasGia4 = hasGia4;

        initComponents();
    }

    private void initComponents() {
        setSize(360, 340);
        setLocationRelativeTo(getParent());
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

        boolean enableGia2 = (config == null || config.isSuDungGia2()) && hasGia2;
        boolean enableGia3 = (config == null || config.isSuDungGia3()) && hasGia3;
        boolean enableGia4 = (config == null || config.isSuDungGia4()) && hasGia4;

        JButton btnGia1 = createPriceButton(nameGia1, true, 0);
        JButton btnGia2 = createPriceButton(nameGia2, enableGia2, 1);
        JButton btnGia3 = createPriceButton(nameGia3, enableGia3, 2);
        JButton btnGia4 = createPriceButton(nameGia4, enableGia4, 3);
        JButton btnHuy = createCancelButton("Hủy bỏ");

        pnlMain.add(btnGia1);
        pnlMain.add(Box.createVerticalStrut(8));
        pnlMain.add(btnGia2);
        pnlMain.add(Box.createVerticalStrut(8));
        pnlMain.add(btnGia3);
        pnlMain.add(Box.createVerticalStrut(8));
        pnlMain.add(btnGia4);
        pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(btnHuy);
        pnlMain.add(Box.createVerticalStrut(12));

        chkApplyToAll = new JCheckBox("Áp dụng cho tất cả mặt hàng trong đơn");
        chkApplyToAll.setOpaque(false);
        chkApplyToAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkApplyToAll.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(chkApplyToAll);

        pnlMain.add(Box.createVerticalStrut(6));

        JLabel lblHint = new JLabel("<html><center><i>(Để kích hoạt các giá 2, 3, 4 bạn vào menu 'Quản trị / Cấu hình toàn hệ thống', tab 'Giá mặt hàng')</i></center></html>");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(new Color(60, 60, 60));
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblHint);

        add(pnlMain, BorderLayout.CENTER);

        // ESC key to close
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JButton createPriceButton(String text, boolean enabled, int priceIndex) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setMaximumSize(new Dimension(310, 30));
        btn.setPreferredSize(new Dimension(310, 30));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setEnabled(enabled);

        if (enabled) {
            btn.addActionListener(e -> {
                selectedPriceIndex = priceIndex;
                selectedPriceName = text;
                applyToAll = chkApplyToAll.isSelected();
                dispose();
            });
        }
        return btn;
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
    public String getSelectedPriceName() { return selectedPriceName; }
    public boolean isApplyToAll() { return applyToAll; }
}
