package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại "THIẾT LẬP CỘT HIỂN THỊ" cho màn hình Khách hàng thân thiết cảm ứng (Hình 2 & 3).
 */
public class TouchThietLapCotKhachHangThanThietDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(211, 84, 0);
    private static final Color PURPLE = new Color(106, 13, 84);
    private static final Color OLIVE = new Color(85, 107, 47);
    private static final Color GRAY_BTN = new Color(135, 145, 150);
    private static final Color LIST_BG = new Color(90, 125, 143);

    private final TouchKhachHangThanThietColumnConfig config;
    private final Runnable onUpdated;

    private TouchKhachHangThanThietColumnConfig.ColumnItem selectedUnused = null;
    private TouchKhachHangThanThietColumnConfig.ColumnItem selectedUsed = null;

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
    private final JButton btnSoCot = button("SỐ CỘT: 1", OLIVE);
    private final JButton btnSoDong = button("SỐ DÒNG: 8", OLIVE);
    private final JButton btnMauO = button("MÀU Ô", OLIVE);
    private final JButton btnHienTieuDe = button("HIỆN TIÊU ĐỀ", ORANGE);
    private final JButton btnCapNhat = button("CẬP NHẬT", GREEN);
    private final JButton btnThoat = button("THOÁT", RED);

    private final JButton btnMoveUp = button("LÊN", GRAY_BTN);
    private final JButton btnMoveDown = button("XUỐNG", GRAY_BTN);

    private boolean isFormattingOpen = false;

    public TouchThietLapCotKhachHangThanThietDialog(Window owner, Runnable onUpdated) {
        super(owner, "Thiết lập cột hiển thị", ModalityType.APPLICATION_MODAL);
        this.onUpdated = onUpdated;
        this.config = TouchKhachHangThanThietConfigDAO.loadConfig().clone();

        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER, 4));

        // 1. Tiêu đề hộp thoại
        JLabel title = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(HEADER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(0, 48));
        root.add(title, BorderLayout.NORTH);

        // 2. Thân chính: Danh sách cột & Bảng định dạng
        JPanel mainBody = new JPanel(new BorderLayout(0, 8));
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel columnTransferPanel = createColumnTransferPanel();
        mainBody.add(columnTransferPanel, BorderLayout.CENTER);

        // Khung định dạng bên dưới (Hình 3)
        createFormatPanel();
        formatContainer.setVisible(false);
        mainBody.add(formatContainer, BorderLayout.SOUTH);

        root.add(mainBody, BorderLayout.CENTER);

        // 3. Thanh nút bấm dưới cùng
        JPanel bottomBar = createBottomBar();
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        refreshLists();
        updateFormatControls();
        updateBottomButtons();
        updatePreview();

        setSize(1080, 620);
        setLocationRelativeTo(owner);
    }

    private JPanel createColumnTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 4, 0, 4);

        // CỘT TRÁI: CÁC CỘT KHÔNG SỬ DỤNG
        JPanel leftBox = new JPanel(new BorderLayout(0, 5));
        leftBox.setOpaque(false);
        JLabel lblLeft = new JLabel("CÁC CỘT KHÔNG SỬ DỤNG");
        lblLeft.setForeground(Color.WHITE);
        lblLeft.setFont(font(15));
        leftBox.add(lblLeft, BorderLayout.NORTH);

        unusedPanel.setLayout(new BoxLayout(unusedPanel, BoxLayout.Y_AXIS));
        unusedPanel.setBackground(BG);
        JScrollPane scrollUnused = new JScrollPane(unusedPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollUnused.setBorder(new LineBorder(HEADER, 1, true));
        scrollUnused.getVerticalScrollBar().setUnitIncrement(35);
        leftBox.add(scrollUnused, BorderLayout.CENTER);

        c.gridx = 0; c.gridy = 0; c.weightx = 0.44; c.weighty = 1.0;
        panel.add(leftBox, c);

        // CỘT GIỮA: THÊM >>, << XÓA, << XÓA HẾT
        JPanel midBox = new JPanel();
        midBox.setLayout(new BoxLayout(midBox, BoxLayout.Y_AXIS));
        midBox.setOpaque(false);
        midBox.setBorder(new EmptyBorder(30, 6, 10, 6));

        JButton btnAdd = button("THÊM >>", GRAY_BTN);
        btnAdd.setPreferredSize(new Dimension(135, 50));
        btnAdd.setMaximumSize(new Dimension(135, 50));
        btnAdd.addActionListener(e -> addColumn());

        JButton btnRemove = button("<< XÓA", GRAY_BTN);
        btnRemove.setPreferredSize(new Dimension(135, 50));
        btnRemove.setMaximumSize(new Dimension(135, 50));
        btnRemove.addActionListener(e -> removeColumn());

        JButton btnRemoveAll = button("<< XÓA HẾT", GREEN);
        btnRemoveAll.setPreferredSize(new Dimension(135, 50));
        btnRemoveAll.setMaximumSize(new Dimension(135, 50));
        btnRemoveAll.addActionListener(e -> removeAllColumns());

        midBox.add(Box.createVerticalGlue());
        midBox.add(btnAdd);
        midBox.add(Box.createVerticalStrut(12));
        midBox.add(btnRemove);
        midBox.add(Box.createVerticalStrut(12));
        midBox.add(btnRemoveAll);
        midBox.add(Box.createVerticalGlue());

        c.gridx = 1; c.gridy = 0; c.weightx = 0.12; c.weighty = 1.0;
        panel.add(midBox, c);

        // CỘT PHẢI: CÁC CỘT SỬ DỤNG
        JPanel rightBox = new JPanel(new BorderLayout(0, 5));
        rightBox.setOpaque(false);
        JLabel lblRight = new JLabel("CÁC CỘT SỬ DỤNG");
        lblRight.setForeground(Color.WHITE);
        lblRight.setFont(font(15));
        rightBox.add(lblRight, BorderLayout.NORTH);

        usedPanel.setLayout(new BoxLayout(usedPanel, BoxLayout.Y_AXIS));
        usedPanel.setBackground(LIST_BG);
        usedPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane scrollUsed = new JScrollPane(usedPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollUsed.setBorder(new LineBorder(HEADER, 1, true));
        scrollUsed.getViewport().setBackground(LIST_BG);
        scrollUsed.getVerticalScrollBar().setUnitIncrement(35);
        rightBox.add(scrollUsed, BorderLayout.CENTER);

        c.gridx = 2; c.gridy = 0; c.weightx = 0.44; c.weighty = 1.0;
        panel.add(rightBox, c);

        // CỘT NGOÀI CÙNG BÊN PHẢI: LÊN, XUỐNG
        JPanel orderBox = new JPanel();
        orderBox.setLayout(new BoxLayout(orderBox, BoxLayout.Y_AXIS));
        orderBox.setOpaque(false);
        orderBox.setBorder(new EmptyBorder(30, 6, 10, 0));

        btnMoveUp.setPreferredSize(new Dimension(115, 50));
        btnMoveUp.setMaximumSize(new Dimension(115, 50));
        btnMoveUp.addActionListener(e -> moveColumnOrder(-1));

        btnMoveDown.setPreferredSize(new Dimension(115, 50));
        btnMoveDown.setMaximumSize(new Dimension(115, 50));
        btnMoveDown.addActionListener(e -> moveColumnOrder(1));

        orderBox.add(Box.createVerticalGlue());
        orderBox.add(btnMoveUp);
        orderBox.add(Box.createVerticalStrut(12));
        orderBox.add(btnMoveDown);
        orderBox.add(Box.createVerticalGlue());

        c.gridx = 3; c.gridy = 0; c.weightx = 0.1; c.weighty = 1.0;
        panel.add(orderBox, c);

        return panel;
    }

    private void createFormatPanel() {
        formatContainer.setOpaque(false);
        formatContainer.setBorder(new EmptyBorder(10, 4, 6, 4));

        // 1. Ô xem trước ở bên trái
        previewCard.setPreferredSize(new Dimension(280, 85));
        previewCard.setBorder(new LineBorder(Color.WHITE, 2, true));
        previewCard.setOpaque(true);
        previewCard.setBackground(config.getCardBgColor());
        formatContainer.add(previewCard, BorderLayout.WEST);

        // 2. Cụm nút định dạng
        JPanel centerFormat = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        centerFormat.setOpaque(false);

        JPanel ctrlGrid = new JPanel(new GridBagLayout());
        ctrlGrid.setOpaque(false);
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets = new Insets(3, 4, 3, 4);
        cc.fill = GridBagConstraints.BOTH;

        btnAlignLeft.setPreferredSize(new Dimension(95, 42));
        btnAlignRight.setPreferredSize(new Dimension(95, 42));
        btnBold.setPreferredSize(new Dimension(58, 42));
        btnItalic.setPreferredSize(new Dimension(75, 42));
        btnFontSize.setPreferredSize(new Dimension(65, 42));
        btnTextColor.setPreferredSize(new Dimension(65, 42));

        btnAlignLeft.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setRightAligned(false);
                updateFormatControls();
                updatePreview();
            }
        });

        btnAlignRight.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setRightAligned(true);
                updateFormatControls();
                updatePreview();
            }
        });

        btnBold.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setBold(!selectedUsed.isBold());
                updateFormatControls();
                updatePreview();
            }
        });

        btnItalic.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setItalic(!selectedUsed.isItalic());
                updateFormatControls();
                updatePreview();
            }
        });

        btnFontSize.addActionListener(e -> {
            if (selectedUsed != null) {
                Integer val = TouchNumberInputDialog.showDialog(this, "NHẬP CỠ CHỮ", selectedUsed.getFontSize());
                if (val != null && val > 0) {
                    selectedUsed.setFontSize(val);
                    updateFormatControls();
                    updatePreview();
                }
            }
        });

        btnTextColor.addActionListener(e -> {
            if (selectedUsed != null) {
                Color picked = TouchChonMauDialog.showDialog(this, selectedUsed.getColor());
                if (picked != null) {
                    selectedUsed.setColor(picked);
                    updatePreview();
                }
            }
        });

        cc.gridx = 0; cc.gridy = 0; ctrlGrid.add(btnAlignLeft, cc);
        cc.gridx = 1; cc.gridy = 0; ctrlGrid.add(btnAlignRight, cc);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row2.setOpaque(false);
        row2.add(btnBold);
        row2.add(btnItalic);
        row2.add(btnFontSize);
        row2.add(btnTextColor);

        cc.gridx = 0; cc.gridy = 1; cc.gridwidth = 2;
        ctrlGrid.add(row2, cc);

        centerFormat.add(ctrlGrid);
        formatContainer.add(centerFormat, BorderLayout.CENTER);
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(6, 12, 10, 12));

        // Trái: Nút ĐỊNH DẠNG + SỐ CỘT + SỐ DÒNG + MÀU Ô + HIỆN TIÊU ĐỀ
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        btnDinhDang.setPreferredSize(new Dimension(145, 52));
        btnDinhDang.addActionListener(e -> toggleFormatting());
        leftActions.add(btnDinhDang);

        btnSoCot.setPreferredSize(new Dimension(125, 52));
        btnSoCot.addActionListener(e -> {
            Integer val = TouchNumberInputDialog.showDialog(this, "NHẬP SỐ CỘT", config.getGridColumns());
            if (val != null && val > 0 && val <= 10) {
                config.setGridColumns(val);
                updateBottomButtons();
            }
        });

        btnSoDong.setPreferredSize(new Dimension(125, 52));
        btnSoDong.addActionListener(e -> {
            Integer val = TouchNumberInputDialog.showDialog(this, "NHẬP SỐ DÒNG", config.getGridRows());
            if (val != null && val > 0 && val <= 50) {
                config.setGridRows(val);
                updateBottomButtons();
            }
        });

        btnMauO.setPreferredSize(new Dimension(115, 52));
        btnMauO.addActionListener(e -> {
            Color picked = TouchChonMauDialog.showDialog(this, config.getCardBgColor());
            if (picked != null) {
                config.setCardBgColor(picked);
                updatePreview();
            }
        });

        btnHienTieuDe.setPreferredSize(new Dimension(160, 52));
        btnHienTieuDe.addActionListener(e -> {
            config.setShowTitle(!config.isShowTitle());
            updateBottomButtons();
            updatePreview();
        });

        leftActions.add(btnSoCot);
        leftActions.add(btnSoDong);
        leftActions.add(btnMauO);
        leftActions.add(btnHienTieuDe);

        bar.add(leftActions, BorderLayout.WEST);

        // Phải: CẬP NHẬT, THOÁT
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        btnCapNhat.setPreferredSize(new Dimension(135, 52));
        btnCapNhat.addActionListener(e -> {
            TouchKhachHangThanThietConfigDAO.saveConfig(config);
            if (onUpdated != null) onUpdated.run();
            dispose();
        });

        btnThoat.setPreferredSize(new Dimension(135, 52));
        btnThoat.addActionListener(e -> dispose());

        rightActions.add(btnCapNhat);
        rightActions.add(btnThoat);
        bar.add(rightActions, BorderLayout.EAST);

        return bar;
    }

    private void toggleFormatting() {
        isFormattingOpen = !isFormattingOpen;
        formatContainer.setVisible(isFormattingOpen);
        btnDinhDang.setBackground(isFormattingOpen ? ORANGE : PURPLE);

        btnSoCot.setVisible(isFormattingOpen);
        btnSoDong.setVisible(isFormattingOpen);
        btnMauO.setVisible(isFormattingOpen);
        btnHienTieuDe.setVisible(isFormattingOpen);

        revalidate();
        repaint();
    }

    private void addColumn() {
        if (selectedUnused == null) return;
        config.getUnusedColumns().remove(selectedUnused);
        config.getUsedColumns().add(selectedUnused);
        selectedUsed = selectedUnused;
        selectedUnused = null;
        refreshLists();
        updateFormatControls();
        updatePreview();
    }

    private void removeColumn() {
        if (selectedUsed == null) return;
        config.getUsedColumns().remove(selectedUsed);
        config.getUnusedColumns().add(selectedUsed);
        selectedUnused = selectedUsed;
        selectedUsed = null;
        refreshLists();
        updateFormatControls();
        updatePreview();
    }

    private void removeAllColumns() {
        config.getUnusedColumns().addAll(config.getUsedColumns());
        config.getUsedColumns().clear();
        selectedUsed = null;
        selectedUnused = null;
        refreshLists();
        updateFormatControls();
        updatePreview();
    }

    private void moveColumnOrder(int delta) {
        if (selectedUsed == null) return;
        List<TouchKhachHangThanThietColumnConfig.ColumnItem> used = config.getUsedColumns();
        int idx = used.indexOf(selectedUsed);
        if (idx < 0) return;
        int target = idx + delta;
        if (target >= 0 && target < used.size()) {
            used.remove(idx);
            used.add(target, selectedUsed);
            refreshLists();
            updatePreview();
        }
    }

    private void refreshLists() {
        // Danh sách chưa dùng
        unusedPanel.removeAll();
        for (TouchKhachHangThanThietColumnConfig.ColumnItem item : config.getUnusedColumns()) {
            JButton btn = makeItemButton(item.getTitle(), item == selectedUnused);
            btn.addActionListener(e -> {
                selectedUnused = item;
                refreshLists();
            });
            unusedPanel.add(btn);
            unusedPanel.add(Box.createVerticalStrut(6));
        }
        unusedPanel.revalidate();
        unusedPanel.repaint();

        // Danh sách đang dùng
        usedPanel.removeAll();
        for (TouchKhachHangThanThietColumnConfig.ColumnItem item : config.getUsedColumns()) {
            JButton btn = makeItemButton(item.getTitle(), item == selectedUsed);
            btn.addActionListener(e -> {
                selectedUsed = item;
                refreshLists();
                updateFormatControls();
                updatePreview();
            });
            usedPanel.add(btn);
            usedPanel.add(Box.createVerticalStrut(6));
        }
        usedPanel.revalidate();
        usedPanel.repaint();
    }

    private JButton makeItemButton(String text, boolean isSelected) {
        JButton btn = new JButton(text);
        btn.setFont(font(15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(isSelected ? ORANGE : new Color(0, 79, 76));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(Color.BLACK, 1));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setPreferredSize(new Dimension(0, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void updateFormatControls() {
        boolean enabled = selectedUsed != null;
        btnAlignLeft.setEnabled(enabled);
        btnAlignRight.setEnabled(enabled);
        btnBold.setEnabled(enabled);
        btnItalic.setEnabled(enabled);
        btnFontSize.setEnabled(enabled);
        btnTextColor.setEnabled(enabled);

        if (enabled) {
            btnAlignLeft.setBackground(selectedUsed.isRightAligned() ? GRAY_BTN : ORANGE);
            btnAlignRight.setBackground(selectedUsed.isRightAligned() ? ORANGE : GRAY_BTN);
            btnBold.setBackground(selectedUsed.isBold() ? ORANGE : GRAY_BTN);
            btnItalic.setBackground(selectedUsed.isItalic() ? ORANGE : GRAY_BTN);
            btnFontSize.setBackground(GRAY_BTN);
            btnTextColor.setBackground(GRAY_BTN);
        } else {
            btnAlignLeft.setBackground(GRAY_BTN);
            btnAlignRight.setBackground(GRAY_BTN);
            btnBold.setBackground(GRAY_BTN);
            btnItalic.setBackground(GRAY_BTN);
            btnFontSize.setBackground(GRAY_BTN);
            btnTextColor.setBackground(GRAY_BTN);
        }
    }

    private void updateBottomButtons() {
        btnSoCot.setText("SỐ CỘT: " + config.getGridColumns());
        btnSoDong.setText("SỐ DÒNG: " + config.getGridRows());
        btnHienTieuDe.setText(config.isShowTitle() ? "ẨN TIÊU ĐỀ" : "HIỆN TIÊU ĐỀ");

        btnSoCot.setVisible(isFormattingOpen);
        btnSoDong.setVisible(isFormattingOpen);
        btnMauO.setVisible(isFormattingOpen);
        btnHienTieuDe.setVisible(isFormattingOpen);
    }

    private void updatePreview() {
        previewCard.removeAll();
        previewCard.setBackground(config.getCardBgColor());
        previewCard.setLayout(new BorderLayout(8, 4));
        previewCard.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel pLeft = new JPanel();
        pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
        pLeft.setOpaque(false);

        JPanel pRight = new JPanel();
        pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));
        pRight.setOpaque(false);

        for (TouchKhachHangThanThietColumnConfig.ColumnItem item : config.getUsedColumns()) {
            String sampleVal = switch (item.getKey()) {
                case "NAME" -> "Tên khách hàng";
                case "DIEM_TICH_LUY" -> "150";
                case "GHI_CHU" -> "Ghi chú mẫu";
                case "DIA_CHI" -> "Hà Nội";
                case "DIEN_THOAI" -> "0901234567";
                case "EMAIL" -> "khach@gmail.com";
                case "DOANH_SO" -> "2,500,000";
                case "SO_HOA_DON" -> "5";
                default -> "Dữ liệu mẫu";
            };
            String text = config.isShowTitle() && item.getPrefix() != null ? item.getPrefix() + sampleVal : sampleVal;

            JLabel lbl = new JLabel(text);
            lbl.setFont(item.getFont());
            lbl.setForeground(item.getColor());

            if (item == selectedUsed) {
                lbl.setBorder(new LineBorder(Color.YELLOW, 1));
            }

            if (item.isRightAligned()) {
                lbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
                pRight.add(lbl);
            } else {
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                pLeft.add(lbl);
            }
        }

        previewCard.add(pLeft, BorderLayout.WEST);
        previewCard.add(pRight, BorderLayout.EAST);

        previewCard.revalidate();
        previewCard.repaint();
    }

    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(font(15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg.brighter(), 1));
        return btn;
    }

    private static Font font(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }
}
