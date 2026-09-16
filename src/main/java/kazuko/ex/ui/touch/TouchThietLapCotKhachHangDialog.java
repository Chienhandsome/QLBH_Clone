package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại "THIẾT LẬP CỘT HIỂN THỊ" cho màn hình Khách hàng cảm ứng.
 */
public class TouchThietLapCotKhachHangDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(211, 84, 0);
    private static final Color PURPLE = new Color(106, 13, 84);
    private static final Color OLIVE = new Color(85, 107, 47);
    private static final Color GRAY_BTN = new Color(135, 145, 150);
    private static final Color LIST_BG = new Color(90, 125, 143);

    private final TouchKhachHangColumnConfig config;
    private final Runnable onUpdated;

    private TouchKhachHangColumnConfig.ColumnItem selectedUnused = null;
    private TouchKhachHangColumnConfig.ColumnItem selectedUsed = null;

    private final JPanel unusedPanel = new JPanel();
    private final JPanel usedPanel = new JPanel();
    private final JPanel formatContainer = new JPanel(new BorderLayout(15, 0));
    private final JPanel previewCard = new JPanel(new BorderLayout(5, 5));

    // Nút định dạng
    private final JButton btnAlignLeft = button("CĂN TRÁI", GRAY_BTN);
    private final JButton btnAlignRight = button("CĂN PHẢI", GRAY_BTN);
    private final JButton btnBold = button("ĐẬM", GRAY_BTN);
    private final JButton btnItalic = button("NGHIÊNG", GRAY_BTN);
    private final JButton btnFontSize = button("CỠ CHỮ", GRAY_BTN);
    private final JButton btnTextColor = button("MÀU CHỮ", GRAY_BTN);

    // Nút điều khiển thanh dưới
    private final JButton btnDinhDang = button("ĐỊNH DẠNG", PURPLE);
    private final JButton btnSoCot = button("SỐ CỘT: 4", OLIVE);
    private final JButton btnSoDong = button("SỐ DÒNG: 6", OLIVE);
    private final JButton btnMauO = button("MÀU Ô", OLIVE);
    private final JButton btnHienTieuDe = button("HIỆN TIÊU ĐỀ", ORANGE);
    private final JButton btnCapNhat = button("CẬP NHẬT", GREEN);
    private final JButton btnThoat = button("THOÁT", RED);

    private final JButton btnMoveUp = button("LÊN", GRAY_BTN);
    private final JButton btnMoveDown = button("XUỐNG", GRAY_BTN);

    private JPanel rightBox;
    private final JPanel rightBoxWrapper = new JPanel(new BorderLayout(6, 0));

    private boolean isFormattingOpen = false;

    public TouchThietLapCotKhachHangDialog(Window owner, Runnable onUpdated) {
        super(owner, "Thiết lập hiển thị khách hàng", ModalityType.APPLICATION_MODAL);
        this.config = TouchKhachHangConfigDAO.loadConfig();
        this.onUpdated = onUpdated;

        setUndecorated(true);
        initComponents();
        updateBottomLabels();
        refreshUnusedList();
        refreshUsedList();
        updateFormattingButtonsState();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER, 2));
        root.setPreferredSize(new Dimension(860, 520));

        // 1. Header
        JLabel lblTitle = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(HEADER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setPreferredSize(new Dimension(860, 36));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Center Panel: 2 lists or List + Formatting
        JPanel centerWrapper = new JPanel(new BorderLayout(10, 0));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 15, 0, 15));

        // Normal 2 columns view (Unused / Arrows / Used)
        JPanel normalColumnsPanel = new JPanel(new BorderLayout(10, 0));
        normalColumnsPanel.setOpaque(false);

        // Left: Cột chưa sử dụng
        JPanel leftBox = createGroupBox("CỘT CHƯA SỬ DỤNG", unusedPanel);
        normalColumnsPanel.add(leftBox, BorderLayout.WEST);

        // Center Arrows: >> and <<
        JPanel arrowsBox = new JPanel(new GridBagLayout());
        arrowsBox.setOpaque(false);
        arrowsBox.setPreferredSize(new Dimension(65, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(6, 0, 6, 0);

        JButton btnAdd = button(">>", TEAL);
        btnAdd.setPreferredSize(new Dimension(55, 40));
        btnAdd.addActionListener(e -> moveToUsed());
        arrowsBox.add(btnAdd, gbc);

        gbc.gridy = 1;
        JButton btnRemove = button("<<", TEAL);
        btnRemove.setPreferredSize(new Dimension(55, 40));
        btnRemove.addActionListener(e -> moveToUnused());
        arrowsBox.add(btnRemove, gbc);

        normalColumnsPanel.add(arrowsBox, BorderLayout.CENTER);

        // Right: Cột đang sử dụng + Lên/Xuống
        rightBoxWrapper.setOpaque(false);

        rightBox = createGroupBox("CỘT ĐANG SỬ DỤNG", usedPanel);
        rightBoxWrapper.add(rightBox, BorderLayout.CENTER);

        // Up / Down buttons
        JPanel orderBtns = new JPanel(new GridBagLayout());
        orderBtns.setOpaque(false);
        orderBtns.setPreferredSize(new Dimension(75, 0));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.insets = new Insets(6, 0, 6, 0);

        btnMoveUp.setPreferredSize(new Dimension(65, 40));
        btnMoveUp.addActionListener(e -> moveItemOrder(-1));
        orderBtns.add(btnMoveUp, gbc2);

        gbc2.gridy = 1;
        btnMoveDown.setPreferredSize(new Dimension(65, 40));
        btnMoveDown.addActionListener(e -> moveItemOrder(1));
        orderBtns.add(btnMoveDown, gbc2);

        rightBoxWrapper.add(orderBtns, BorderLayout.EAST);
        normalColumnsPanel.add(rightBoxWrapper, BorderLayout.EAST);

        // Setup formatContainer
        setupFormattingPanel();

        centerWrapper.add(normalColumnsPanel, BorderLayout.CENTER);
        root.add(centerWrapper, BorderLayout.CENTER);

        // 3. Bottom Toolbar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(0, 5, 6, 5));

        btnDinhDang.addActionListener(e -> toggleFormattingView(centerWrapper, normalColumnsPanel));
        TouchGridSettingsSupport.bind(this, btnSoCot, btnSoDong, config, this::updateBottomLabels);
        btnMauO.addActionListener(e -> changeCardColor());
        btnHienTieuDe.addActionListener(e -> toggleShowTitle());
        btnCapNhat.addActionListener(e -> saveAndClose());
        btnThoat.addActionListener(e -> dispose());

        bottomBar.add(btnDinhDang);
        bottomBar.add(btnSoCot);
        bottomBar.add(btnSoDong);
        bottomBar.add(btnMauO);
        bottomBar.add(btnHienTieuDe);
        bottomBar.add(btnCapNhat);
        bottomBar.add(btnThoat);

        root.add(bottomBar, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel createGroupBox(String title, JPanel contentList) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(320, 0));

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        box.add(lbl, BorderLayout.NORTH);

        contentList.setLayout(new BoxLayout(contentList, BoxLayout.Y_AXIS));
        contentList.setBackground(LIST_BG);
        contentList.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane scroll = new JScrollPane(contentList);
        scroll.getViewport().setBackground(LIST_BG);
        scroll.setBorder(new LineBorder(new Color(50, 90, 110), 1));
        box.add(scroll, BorderLayout.CENTER);

        return box;
    }

    private void setupFormattingPanel() {
        formatContainer.setOpaque(false);

        // Right of format view: Nút định dạng + Preview
        JPanel rightFormatPanel = new JPanel(new BorderLayout(0, 12));
        rightFormatPanel.setOpaque(false);

        // Grid 2x3 for 6 formatting buttons
        JPanel btnsGrid = new JPanel(new GridLayout(2, 3, 8, 8));
        btnsGrid.setOpaque(false);
        btnsGrid.setPreferredSize(new Dimension(0, 90));

        btnAlignLeft.addActionListener(e -> setAlign(false));
        btnAlignRight.addActionListener(e -> setAlign(true));
        btnBold.addActionListener(e -> toggleBold());
        btnItalic.addActionListener(e -> toggleItalic());
        btnFontSize.addActionListener(e -> changeFontSize());
        btnTextColor.addActionListener(e -> changeTextColor());

        btnsGrid.add(btnAlignLeft);
        btnsGrid.add(btnAlignRight);
        btnsGrid.add(btnBold);
        btnsGrid.add(btnItalic);
        btnsGrid.add(btnFontSize);
        btnsGrid.add(btnTextColor);

        rightFormatPanel.add(btnsGrid, BorderLayout.NORTH);

        // Card Preview
        JPanel previewWrapper = new JPanel(new BorderLayout());
        previewWrapper.setOpaque(false);
        previewWrapper.setBorder(new LineBorder(new Color(50, 90, 110), 1));

        previewCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        previewWrapper.add(previewCard, BorderLayout.CENTER);
        rightFormatPanel.add(previewWrapper, BorderLayout.CENTER);

        formatContainer.add(rightFormatPanel, BorderLayout.CENTER);
    }

    private void toggleFormattingView(JPanel centerWrapper, JPanel normalPanel) {
        isFormattingOpen = !isFormattingOpen;
        centerWrapper.removeAll();
        if (isFormattingOpen) {
            btnDinhDang.setText("CỘT HIỂN THỊ");
            btnDinhDang.setBackground(ORANGE);
            formatContainer.add(rightBox, BorderLayout.WEST);
            centerWrapper.add(formatContainer, BorderLayout.CENTER);
            updatePreview();
        } else {
            btnDinhDang.setText("ĐỊNH DẠNG");
            btnDinhDang.setBackground(PURPLE);
            rightBoxWrapper.add(rightBox, BorderLayout.CENTER);
            normalPanel.add(rightBoxWrapper, BorderLayout.EAST);
            centerWrapper.add(normalPanel, BorderLayout.CENTER);
        }
        centerWrapper.revalidate();
        centerWrapper.repaint();
    }

    private void updateFormattingButtonsState() {
        boolean enabled = (selectedUsed != null);
        btnAlignLeft.setEnabled(enabled);
        btnAlignRight.setEnabled(enabled);
        btnBold.setEnabled(enabled);
        btnItalic.setEnabled(enabled);
        btnFontSize.setEnabled(enabled);
        btnTextColor.setEnabled(enabled);

        if (enabled) {
            btnAlignLeft.setBackground(selectedUsed.isRightAligned() ? GRAY_BTN : TEAL);
            btnAlignRight.setBackground(selectedUsed.isRightAligned() ? TEAL : GRAY_BTN);
            btnBold.setBackground(selectedUsed.isBold() ? TEAL : GRAY_BTN);
            btnItalic.setBackground(selectedUsed.isItalic() ? TEAL : GRAY_BTN);
            btnFontSize.setBackground(TEAL);
            btnTextColor.setBackground(TEAL);
        } else {
            btnAlignLeft.setBackground(GRAY_BTN);
            btnAlignRight.setBackground(GRAY_BTN);
            btnBold.setBackground(GRAY_BTN);
            btnItalic.setBackground(GRAY_BTN);
            btnFontSize.setBackground(GRAY_BTN);
            btnTextColor.setBackground(GRAY_BTN);
        }
    }

    private void refreshUnusedList() {
        unusedPanel.removeAll();
        for (TouchKhachHangColumnConfig.ColumnItem item : config.getUnusedColumns()) {
            JButton btn = makeItemButton(item.getTitle(), item == selectedUnused);
            btn.addActionListener(e -> {
                selectedUnused = item;
                refreshUnusedList();
            });
            unusedPanel.add(btn);
            unusedPanel.add(Box.createVerticalStrut(3));
        }
        unusedPanel.revalidate();
        unusedPanel.repaint();
    }

    private void refreshUsedList() {
        usedPanel.removeAll();
        for (TouchKhachHangColumnConfig.ColumnItem item : config.getUsedColumns()) {
            JButton btn = makeItemButton(item.getTitle(), item == selectedUsed);
            btn.addActionListener(e -> {
                selectedUsed = item;
                refreshUsedList();
                updateFormattingButtonsState();
                updatePreview();
            });
            usedPanel.add(btn);
            usedPanel.add(Box.createVerticalStrut(3));
        }
        usedPanel.revalidate();
        usedPanel.repaint();
    }

    private JButton makeItemButton(String text, boolean isSelected) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(isSelected ? ORANGE : TEAL);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(Color.BLACK, 1));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void updatePreview() {
        previewCard.removeAll();
        previewCard.setBackground(config.getCardBgColor());
        previewCard.setLayout(new BoxLayout(previewCard, BoxLayout.Y_AXIS));

        for (TouchKhachHangColumnConfig.ColumnItem item : config.getUsedColumns()) {
            String sampleVal = switch (item.getKey()) {
                case "TINH_THANH" -> "Hà Nội";
                case "MA_KHACH" -> "KH0001";
                case "NAME" -> "Nguyễn Văn A";
                case "DIEN_THOAI" -> "0901234567";
                case "DIA_CHI" -> "123 Phố Huế";
                case "EMAIL" -> "khach@gmail.com";
                case "NHOM_KHACH" -> "VIP";
                case "MA_SO_THUE" -> "0102030405";
                case "DIEM_TICH_LUY" -> "150";
                default -> "Dữ liệu mẫu";
            };
            String text = config.isShowTitle() && item.getPrefix() != null ? item.getPrefix() + sampleVal : sampleVal;

            JLabel lbl = new JLabel(text);
            lbl.setFont(item.getFont());
            lbl.setForeground(item.getColor());
            lbl.setAlignmentX(item.isRightAligned() ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

            if (item == selectedUsed) {
                lbl.setBorder(new LineBorder(Color.YELLOW, 1));
            }
            previewCard.add(lbl);
            previewCard.add(Box.createVerticalStrut(4));
        }

        previewCard.revalidate();
        previewCard.repaint();
    }

    private void setAlign(boolean right) {
        if (selectedUsed == null) return;
        selectedUsed.setRightAligned(right);
        updateFormattingButtonsState();
        updatePreview();
    }

    private void toggleBold() {
        if (selectedUsed == null) return;
        selectedUsed.setBold(!selectedUsed.isBold());
        updateFormattingButtonsState();
        updatePreview();
    }

    private void toggleItalic() {
        if (selectedUsed == null) return;
        selectedUsed.setItalic(!selectedUsed.isItalic());
        updateFormattingButtonsState();
        updatePreview();
    }

    private void changeFontSize() {
        if (selectedUsed == null) return;
        Integer size = TouchNumberInputDialog.showDialog(this, "NHẬP CỠ CHỮ", selectedUsed.getFontSize());
        if (size != null && size >= 8 && size <= 48) {
            selectedUsed.setFontSize(size);
            updatePreview();
        }
    }

    private void changeTextColor() {
        if (selectedUsed == null) return;
        Color c = TouchChonMauDialog.showDialog(this, selectedUsed.getColor());
        if (c != null) {
            selectedUsed.setColor(c);
            updatePreview();
        }
    }

    private void changeCardColor() {
        Color c = TouchChonMauDialog.showDialog(this, config.getCardBgColor());
        if (c != null) {
            config.setCardBgColor(c);
            updatePreview();
        }
    }

    private void toggleShowTitle() {
        config.setShowTitle(!config.isShowTitle());
        updateBottomLabels();
        updatePreview();
    }

    private void updateBottomLabels() {
        TouchGridSettingsSupport.refreshLabels(btnSoCot, btnSoDong, config);
        btnHienTieuDe.setText(config.isShowTitle() ? "ẨN TIÊU ĐỀ" : "HIỆN TIÊU ĐỀ");
    }

    private void moveToUsed() {
        if (selectedUnused == null) return;
        config.getUnusedColumns().remove(selectedUnused);
        config.getUsedColumns().add(selectedUnused);
        selectedUsed = selectedUnused;
        selectedUnused = null;
        refreshUnusedList();
        refreshUsedList();
        updateFormattingButtonsState();
    }

    private void moveToUnused() {
        if (selectedUsed == null) return;
        config.getUsedColumns().remove(selectedUsed);
        config.getUnusedColumns().add(selectedUsed);
        selectedUnused = selectedUsed;
        selectedUsed = null;
        refreshUnusedList();
        refreshUsedList();
        updateFormattingButtonsState();
    }

    private void moveItemOrder(int dir) {
        if (selectedUsed == null) return;
        int idx = config.getUsedColumns().indexOf(selectedUsed);
        if (idx < 0) return;
        int target = idx + dir;
        if (target >= 0 && target < config.getUsedColumns().size()) {
            config.getUsedColumns().remove(idx);
            config.getUsedColumns().add(target, selectedUsed);
            refreshUsedList();
        }
    }

    private void saveAndClose() {
        TouchKhachHangConfigDAO.saveConfig(config);
        if (onUpdated != null) onUpdated.run();
        dispose();
    }

    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg.brighter(), 1));
        btn.setPreferredSize(new Dimension(110, 42));
        return btn;
    }
}
