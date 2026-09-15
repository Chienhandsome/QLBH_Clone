package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Hộp thoại "THIẾT LẬP CỘT HIỂN THỊ" cho thẻ mặt hàng màn hình bán hàng theo chuẩn 100% Hình 4.
 */
public class TouchThietLapCotBanHangDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(211, 84, 0);
    private static final Color PURPLE = new Color(106, 13, 84);
    private static final Color GRAY_BTN = new Color(135, 145, 150);
    private static final Color LIST_BG = new Color(110, 145, 165);

    private final TouchBanHangProductColumnConfig config;
    private final Runnable onUpdated;

    private TouchBanHangProductColumnConfig.ColumnItem selectedUnused = null;
    private TouchBanHangProductColumnConfig.ColumnItem selectedUsed = null;

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

    // Nút điều khiển thanh dưới (Hình 4: ĐỊNH DẠNG, CẬP NHẬT, THOÁT)
    private final JButton btnDinhDang = button("ĐỊNH DẠNG", PURPLE);
    private final JButton btnCapNhat = button("CẬP NHẬT", GREEN);
    private final JButton btnThoat = button("THOÁT", RED);

    private final JButton btnMoveUp = button("LÊN", GRAY_BTN);
    private final JButton btnMoveDown = button("XUỐNG", GRAY_BTN);

    private JPanel rightBox;
    private final JPanel rightBoxWrapper = new JPanel(new BorderLayout(6, 0));
    private boolean isFormattingOpen = false;

    public TouchThietLapCotBanHangDialog(Window owner, Runnable onUpdated) {
        super(owner, "Thiết lập hiển thị hàng hóa", ModalityType.APPLICATION_MODAL);
        this.config = TouchBanHangProductConfigDAO.loadConfig();
        this.onUpdated = onUpdated;

        setUndecorated(true);
        initComponents();
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
        root.setPreferredSize(new Dimension(860, 500));

        // 1. Header
        JLabel lblTitle = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(HEADER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setPreferredSize(new Dimension(860, 36));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Thân giữa: Lưới 2 cột (Chưa dùng - Chuyển - Sử dụng)
        JPanel centerWrapper = new JPanel(new BorderLayout(10, 0));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 15, 0, 15));

        JPanel normalColumnsPanel = new JPanel(new BorderLayout(10, 0));
        normalColumnsPanel.setOpaque(false);

        // Cột trái: CÁC CỘT KHÔNG SỬ DỤNG
        JPanel leftBox = new JPanel(new BorderLayout(0, 6));
        leftBox.setOpaque(false);
        JLabel lblLeft = new JLabel("CÁC CỘT KHÔNG SỬ DỤNG");
        lblLeft.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLeft.setForeground(Color.WHITE);
        leftBox.add(lblLeft, BorderLayout.NORTH);

        unusedPanel.setLayout(new BoxLayout(unusedPanel, BoxLayout.Y_AXIS));
        unusedPanel.setOpaque(true);
        unusedPanel.setBackground(LIST_BG);
        unusedPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane spLeft = new JScrollPane(unusedPanel);
        spLeft.setBorder(new LineBorder(new Color(140, 160, 170), 1));
        leftBox.add(spLeft, BorderLayout.CENTER);
        leftBox.setPreferredSize(new Dimension(310, 360));
        normalColumnsPanel.add(leftBox, BorderLayout.WEST);

        // Cột giữa: Nút chuyển (THÊM >>, << XÓA, << XÓA HẾT)
        JPanel midBox = new JPanel(new GridBagLayout());
        midBox.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JButton btnAdd = button("THÊM >>", GRAY_BTN);
        btnAdd.setPreferredSize(new Dimension(105, 42));
        btnAdd.addActionListener(e -> onAddColumn());
        gbc.gridy = 0; midBox.add(btnAdd, gbc);

        JButton btnRemove = button("<< XÓA", GRAY_BTN);
        btnRemove.setPreferredSize(new Dimension(105, 42));
        btnRemove.addActionListener(e -> onRemoveColumn());
        gbc.gridy = 1; midBox.add(btnRemove, gbc);

        JButton btnRemoveAll = button("<< XÓA HẾT", GREEN);
        btnRemoveAll.setPreferredSize(new Dimension(105, 42));
        btnRemoveAll.addActionListener(e -> onRemoveAllColumns());
        gbc.gridy = 2; midBox.add(btnRemoveAll, gbc);

        normalColumnsPanel.add(midBox, BorderLayout.CENTER);

        // Cột phải: CÁC CỘT SỬ DỤNG
        rightBox = new JPanel(new BorderLayout(0, 6));
        rightBox.setOpaque(false);
        JLabel lblRight = new JLabel("CÁC CỘT SỬ DỤNG");
        lblRight.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRight.setForeground(Color.WHITE);
        rightBox.add(lblRight, BorderLayout.NORTH);

        usedPanel.setLayout(new BoxLayout(usedPanel, BoxLayout.Y_AXIS));
        usedPanel.setOpaque(true);
        usedPanel.setBackground(LIST_BG);
        usedPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane spRight = new JScrollPane(usedPanel);
        spRight.setBorder(new LineBorder(new Color(140, 160, 170), 1));
        rightBox.add(spRight, BorderLayout.CENTER);
        rightBox.setPreferredSize(new Dimension(310, 360));

        // Nút LÊN, XUỐNG ngoài cùng bên phải
        JPanel reorderBox = new JPanel(new GridBagLayout());
        reorderBox.setOpaque(false);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(6, 4, 6, 4);

        btnMoveUp.setPreferredSize(new Dimension(90, 42));
        btnMoveUp.addActionListener(e -> onMoveUp());
        gbc2.gridy = 0; reorderBox.add(btnMoveUp, gbc2);

        btnMoveDown.setPreferredSize(new Dimension(90, 42));
        btnMoveDown.addActionListener(e -> onMoveDown());
        gbc2.gridy = 1; reorderBox.add(btnMoveDown, gbc2);

        rightBoxWrapper.setOpaque(false);
        rightBoxWrapper.add(rightBox, BorderLayout.CENTER);
        rightBoxWrapper.add(reorderBox, BorderLayout.EAST);
        normalColumnsPanel.add(rightBoxWrapper, BorderLayout.EAST);

        // Xây dựng panel định dạng
        initFormatContainer();

        centerWrapper.add(normalColumnsPanel, BorderLayout.CENTER);
        root.add(centerWrapper, BorderLayout.CENTER);

        // 3. Thanh nút đáy: ĐỊNH DẠNG bên trái, CẬP NHẬT & THOÁT bên phải
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setPreferredSize(new Dimension(860, 52));
        bottomBar.setBorder(new EmptyBorder(0, 15, 10, 15));

        // Trái: ĐỊNH DẠNG
        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBottom.setOpaque(false);
        btnDinhDang.setPreferredSize(new Dimension(130, 44));
        btnDinhDang.addActionListener(e -> toggleFormattingMode());
        leftBottom.add(btnDinhDang);
        bottomBar.add(leftBottom, BorderLayout.WEST);

        // Phải: CẬP NHẬT, THOÁT
        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBottom.setOpaque(false);

        btnCapNhat.setPreferredSize(new Dimension(130, 44));
        btnCapNhat.addActionListener(e -> onSaveAndClose());
        rightBottom.add(btnCapNhat);

        btnThoat.setPreferredSize(new Dimension(110, 44));
        btnThoat.addActionListener(e -> dispose());
        rightBottom.add(btnThoat);

        bottomBar.add(rightBottom, BorderLayout.EAST);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void initFormatContainer() {
        formatContainer.setOpaque(false);

        // Nửa trái: các nút thuộc tính định dạng
        JPanel toolsGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        toolsGrid.setOpaque(false);

        btnAlignLeft.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setRightAligned(false);
                updateFormattingButtonsState();
                refreshUsedList();
            }
        });
        btnAlignRight.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setRightAligned(true);
                updateFormattingButtonsState();
                refreshUsedList();
            }
        });
        btnBold.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setBold(!selectedUsed.isBold());
                updateFormattingButtonsState();
                refreshUsedList();
            }
        });
        btnItalic.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setItalic(!selectedUsed.isItalic());
                updateFormattingButtonsState();
                refreshUsedList();
            }
        });
        btnFontSize.addActionListener(e -> {
            if (selectedUsed != null) {
                Integer sz = TouchNumberInputDialog.showDialog(this, "CỠ CHỮ CỘT HIỂN THỊ", selectedUsed.getFontSize(), 8, 36);
                if (sz != null) {
                    selectedUsed.setFontSize(sz);
                    btnFontSize.setText("CỠ CHỮ: " + sz);
                    refreshUsedList();
                }
            }
        });
        btnTextColor.addActionListener(e -> {
            if (selectedUsed != null) {
                Color picked = TouchChonMauDialog.showDialog(this, selectedUsed.getColor());
                if (picked != null) {
                    selectedUsed.setColor(picked);
                    refreshUsedList();
                }
            }
        });

        toolsGrid.add(btnAlignLeft);
        toolsGrid.add(btnAlignRight);
        toolsGrid.add(btnBold);
        toolsGrid.add(btnItalic);
        toolsGrid.add(btnFontSize);
        toolsGrid.add(btnTextColor);

        formatContainer.add(toolsGrid, BorderLayout.WEST);

        // Nửa phải: Card xem trước (Preview Card)
        previewCard.setOpaque(true);
        previewCard.setBackground(TEAL);
        previewCard.setBorder(new LineBorder(Color.WHITE, 2, true));
        previewCard.setPreferredSize(new Dimension(280, 180));
        formatContainer.add(previewCard, BorderLayout.CENTER);
    }

    private void toggleFormattingMode() {
        isFormattingOpen = !isFormattingOpen;
        rightBoxWrapper.remove(rightBox);
        if (isFormattingOpen) {
            btnDinhDang.setText("DANH SÁCH");
            btnDinhDang.setBackground(ORANGE);
            rightBoxWrapper.add(formatContainer, BorderLayout.CENTER);
            btnMoveUp.setEnabled(false);
            btnMoveDown.setEnabled(false);
        } else {
            btnDinhDang.setText("ĐỊNH DẠNG");
            btnDinhDang.setBackground(PURPLE);
            rightBoxWrapper.add(rightBox, BorderLayout.CENTER);
            btnMoveUp.setEnabled(true);
            btnMoveDown.setEnabled(true);
        }
        rightBoxWrapper.revalidate();
        rightBoxWrapper.repaint();
        updateFormattingButtonsState();
    }

    private void refreshUnusedList() {
        unusedPanel.removeAll();
        for (TouchBanHangProductColumnConfig.ColumnItem item : config.getUnusedColumns()) {
            JButton btn = createColumnButton(item.getTitle(), item == selectedUnused);
            btn.addActionListener(e -> {
                selectedUnused = item;
                refreshUnusedList();
            });
            unusedPanel.add(btn);
            unusedPanel.add(Box.createVerticalStrut(4));
        }
        unusedPanel.revalidate();
        unusedPanel.repaint();
    }

    private void refreshUsedList() {
        usedPanel.removeAll();
        for (TouchBanHangProductColumnConfig.ColumnItem item : config.getUsedColumns()) {
            JButton btn = createColumnButton(item.getTitle(), item == selectedUsed);
            btn.addActionListener(e -> {
                selectedUsed = item;
                refreshUsedList();
                updateFormattingButtonsState();
            });
            usedPanel.add(btn);
            usedPanel.add(Box.createVerticalStrut(4));
        }
        usedPanel.revalidate();
        usedPanel.repaint();
        updatePreviewCard();
    }

    private void updateFormattingButtonsState() {
        boolean hasSel = (selectedUsed != null);
        btnAlignLeft.setEnabled(hasSel);
        btnAlignRight.setEnabled(hasSel);
        btnBold.setEnabled(hasSel);
        btnItalic.setEnabled(hasSel);
        btnFontSize.setEnabled(hasSel);
        btnTextColor.setEnabled(hasSel);

        if (hasSel) {
            btnAlignLeft.setBackground(selectedUsed.isRightAligned() ? GRAY_BTN : ORANGE);
            btnAlignRight.setBackground(selectedUsed.isRightAligned() ? ORANGE : GRAY_BTN);
            btnBold.setBackground(selectedUsed.isBold() ? ORANGE : GRAY_BTN);
            btnItalic.setBackground(selectedUsed.isItalic() ? ORANGE : GRAY_BTN);
            btnFontSize.setText("CỠ CHỮ: " + selectedUsed.getFontSize());
        } else {
            btnAlignLeft.setBackground(GRAY_BTN);
            btnAlignRight.setBackground(GRAY_BTN);
            btnBold.setBackground(GRAY_BTN);
            btnItalic.setBackground(GRAY_BTN);
            btnFontSize.setText("CỠ CHỮ");
        }
    }

    private void updatePreviewCard() {
        previewCard.removeAll();
        previewCard.setLayout(new BorderLayout(8, 0));
        previewCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblName = new JLabel("ALYNA Hair Coat 80ml", SwingConstants.LEFT);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);
        previewCard.add(lblName, BorderLayout.WEST);

        JLabel lblPrice = new JLabel("35,000", SwingConstants.RIGHT);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(Color.WHITE);
        previewCard.add(lblPrice, BorderLayout.EAST);

        previewCard.revalidate();
        previewCard.repaint();
    }

    private void onAddColumn() {
        if (selectedUnused != null) {
            config.getUnusedColumns().remove(selectedUnused);
            config.getUsedColumns().add(selectedUnused);
            selectedUsed = selectedUnused;
            selectedUnused = null;
            refreshUnusedList();
            refreshUsedList();
        }
    }

    private void onRemoveColumn() {
        if (selectedUsed != null) {
            config.getUsedColumns().remove(selectedUsed);
            config.getUnusedColumns().add(selectedUsed);
            selectedUnused = selectedUsed;
            selectedUsed = null;
            refreshUnusedList();
            refreshUsedList();
            updateFormattingButtonsState();
        }
    }

    private void onRemoveAllColumns() {
        config.getUnusedColumns().addAll(config.getUsedColumns());
        config.getUsedColumns().clear();
        selectedUsed = null;
        selectedUnused = null;
        refreshUnusedList();
        refreshUsedList();
        updateFormattingButtonsState();
    }

    private void onMoveUp() {
        if (selectedUsed != null) {
            int idx = config.getUsedColumns().indexOf(selectedUsed);
            if (idx > 0) {
                config.getUsedColumns().remove(idx);
                config.getUsedColumns().add(idx - 1, selectedUsed);
                refreshUsedList();
            }
        }
    }

    private void onMoveDown() {
        if (selectedUsed != null) {
            int idx = config.getUsedColumns().indexOf(selectedUsed);
            if (idx >= 0 && idx < config.getUsedColumns().size() - 1) {
                config.getUsedColumns().remove(idx);
                config.getUsedColumns().add(idx + 1, selectedUsed);
                refreshUsedList();
            }
        }
    }

    private void onSaveAndClose() {
        TouchBanHangProductConfigDAO.saveConfig(config);
        if (onUpdated != null) {
            onUpdated.run();
        }
        dispose();
    }

    private JButton createColumnButton(String text, boolean isSelected) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(280, 44));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(isSelected ? ORANGE : TEAL);
        btn.setBorder(new LineBorder(Color.WHITE, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
