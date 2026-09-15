package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại "THIẾT LẬP CỘT HIỂN THỊ" và Định dạng cho màn hình mặt hàng cảm ứng.
 * Hỗ trợ chọn cột, sắp xếp, căn lề, đậm, nghiêng, cỡ chữ, màu chữ, màu ô, số cột, số dòng.
 */
public class TouchThietLapCotDialog extends JDialog {
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

    private final TouchMatHangColumnConfig config;
    private final Runnable onUpdated;

    private TouchMatHangColumnConfig.ColumnItem selectedUnused = null;
    private TouchMatHangColumnConfig.ColumnItem selectedUsed = null;

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
    private final JButton btnSoCot = button("SỐ CỘT: 2", OLIVE);
    private final JButton btnSoDong = button("SỐ DÒNG: 8", OLIVE);
    private final JButton btnMauO = button("MÀU Ô", OLIVE);
    private final JButton btnHienTieuDe = button("HIỆN TIÊU ĐỀ", ORANGE);
    private final JButton btnCapNhat = button("CẬP NHẬT", GREEN);
    private final JButton btnThoat = button("THOÁT", RED);

    private final JButton btnMoveUp = button("LÊN", GRAY_BTN);
    private final JButton btnMoveDown = button("XUỐNG", GRAY_BTN);

    private boolean isFormattingOpen = false;

    public TouchThietLapCotDialog(Window owner, Runnable onUpdated) {
        super(owner, "Thiết lập cột hiển thị", ModalityType.APPLICATION_MODAL);
        this.onUpdated = onUpdated;
        this.config = TouchMatHangConfigDAO.loadConfig().clone();

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
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setPreferredSize(new Dimension(0, 50));
        root.add(title, BorderLayout.NORTH);

        // 2. Thân chính: 2 danh sách cột & các nút thao tác
        JPanel mainBody = new JPanel(new BorderLayout(0, 8));
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel columnTransferPanel = createColumnTransferPanel();
        mainBody.add(columnTransferPanel, BorderLayout.CENTER);

        // 3. Khung định dạng bên dưới (Hình 3 & 5)
        createFormatPanel();
        formatContainer.setVisible(false);
        mainBody.add(formatContainer, BorderLayout.SOUTH);

        root.add(mainBody, BorderLayout.CENTER);

        // 4. Thanh nút bấm dưới cùng
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

        // 1. Ô xem trước (Mô phỏng ô mặt hàng) ở bên trái
        previewCard.setPreferredSize(new Dimension(280, 115));
        previewCard.setBorder(new LineBorder(Color.WHITE, 2, true));
        previewCard.setOpaque(true);
        previewCard.setBackground(config.getCardBgColor());
        formatContainer.add(previewCard, BorderLayout.WEST);

        // 2. Cụm nút định dạng
        JPanel controls = new JPanel(new GridLayout(2, 3, 10, 10));
        controls.setOpaque(false);

        btnAlignLeft.setPreferredSize(new Dimension(100, 48));
        btnAlignRight.setPreferredSize(new Dimension(100, 48));
        btnBold.setPreferredSize(new Dimension(100, 48));
        btnItalic.setPreferredSize(new Dimension(100, 48));
        btnFontSize.setPreferredSize(new Dimension(100, 48));
        btnTextColor.setPreferredSize(new Dimension(100, 48));

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
                Integer val = TouchNumberInputDialog.show(this, "NHẬP CỠ CHỮ", selectedUsed.getFontSize(), 8, 72);
                if (val != null && val > 0) {
                    selectedUsed.setFontSize(val);
                    updateFormatControls();
                    updatePreview();
                }
            }
        });

        btnTextColor.addActionListener(e -> {
            if (selectedUsed != null) {
                Color picked = TouchChonMauDialog.show(this, selectedUsed.getColor());
                if (picked != null) {
                    selectedUsed.setColor(picked);
                    updatePreview();
                }
            }
        });

        // Bố trí 2 hàng nút
        // Hàng 1: CĂN TRÁI, CĂN PHẢI, (trống/filler)
        // Hàng 2: ĐẬM, NGHIÊNG, CỠ CHỮ, MÀU CHỮ
        JPanel ctrlGrid = new JPanel(new GridBagLayout());
        ctrlGrid.setOpaque(false);
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets = new Insets(3, 4, 3, 4);
        cc.fill = GridBagConstraints.BOTH;

        cc.gridx = 0; cc.gridy = 0; ctrlGrid.add(btnAlignLeft, cc);
        cc.gridx = 1; cc.gridy = 0; ctrlGrid.add(btnAlignRight, cc);

        cc.gridx = 0; cc.gridy = 1; ctrlGrid.add(btnBold, cc);
        cc.gridx = 1; cc.gridy = 1; ctrlGrid.add(btnItalic, cc);
        cc.gridx = 2; cc.gridy = 1; ctrlGrid.add(btnFontSize, cc);
        cc.gridx = 3; cc.gridy = 1; ctrlGrid.add(btnTextColor, cc);

        formatContainer.add(ctrlGrid, BorderLayout.CENTER);
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
            Integer val = TouchNumberInputDialog.show(this, "NHẬP SỐ CỘT HIỂN THỊ TRÊN LƯỚI", config.getGridColumns(), 1, 10);
            if (val != null && val > 0) {
                config.setGridColumns(val);
                updateBottomButtons();
            }
        });

        btnSoDong.setPreferredSize(new Dimension(125, 52));
        btnSoDong.addActionListener(e -> {
            Integer val = TouchNumberInputDialog.show(this, "NHẬP SỐ DÒNG HIỂN THỊ TRÊN LƯỚI", config.getGridRows(), 1, 50);
            if (val != null && val > 0) {
                config.setGridRows(val);
                updateBottomButtons();
            }
        });

        btnMauO.setPreferredSize(new Dimension(115, 52));
        btnMauO.addActionListener(e -> {
            Color picked = TouchChonMauDialog.show(this, config.getCardBgColor());
            if (picked != null) {
                config.setCardBgColor(picked);
                updatePreview();
            }
        });

        btnHienTieuDe.setPreferredSize(new Dimension(155, 52));
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

        btnCapNhat.setPreferredSize(new Dimension(160, 52));
        btnCapNhat.addActionListener(e -> saveAndApply());

        btnThoat.setPreferredSize(new Dimension(140, 52));
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

        if (isFormattingOpen) {
            setSize(1080, 780);
        } else {
            setSize(1080, 620);
        }
        setLocationRelativeTo(getOwner());
        revalidate();
        repaint();
    }

    private void refreshLists() {
        // 1. Cột chưa sử dụng
        unusedPanel.removeAll();
        for (TouchMatHangColumnConfig.ColumnItem item : config.getUnusedColumns()) {
            JButton b = columnButton(item.getTitle(), item == selectedUnused ? ORANGE : TEAL);
            b.addActionListener(e -> {
                selectedUnused = item;
                refreshLists();
            });
            unusedPanel.add(b);
            unusedPanel.add(Box.createVerticalStrut(6));
        }
        unusedPanel.add(Box.createVerticalGlue());

        // 2. Cột đang sử dụng
        usedPanel.removeAll();
        for (TouchMatHangColumnConfig.ColumnItem item : config.getUsedColumns()) {
            boolean isSel = (item == selectedUsed);
            JButton b = columnButton(item.getTitle(), isSel ? ORANGE : TEAL);
            b.addActionListener(e -> {
                selectedUsed = item;
                refreshLists();
                updateFormatControls();
                updatePreview();
            });
            usedPanel.add(b);
            usedPanel.add(Box.createVerticalStrut(6));
        }
        usedPanel.add(Box.createVerticalGlue());

        // Cập nhật trạng thái nút LÊN, XUỐNG
        boolean canMove = selectedUsed != null;
        int idx = selectedUsed == null ? -1 : config.getUsedColumns().indexOf(selectedUsed);
        btnMoveUp.setEnabled(canMove && idx > 0);
        btnMoveUp.setBackground(canMove && idx > 0 ? GREEN : GRAY_BTN);
        btnMoveDown.setEnabled(canMove && idx >= 0 && idx < config.getUsedColumns().indexOf(selectedUsed) + 1 && idx < config.getUsedColumns().size() - 1);
        btnMoveDown.setBackground(btnMoveDown.isEnabled() ? GREEN : GRAY_BTN);

        unusedPanel.revalidate(); unusedPanel.repaint();
        usedPanel.revalidate(); usedPanel.repaint();
    }

    private void updateFormatControls() {
        boolean hasSel = (selectedUsed != null);

        btnAlignLeft.setEnabled(hasSel);
        btnAlignRight.setEnabled(hasSel);
        btnBold.setEnabled(hasSel);
        btnItalic.setEnabled(hasSel);
        btnFontSize.setEnabled(hasSel);
        btnTextColor.setEnabled(hasSel);

        if (!hasSel) {
            btnAlignLeft.setBackground(GRAY_BTN);
            btnAlignRight.setBackground(GRAY_BTN);
            btnBold.setBackground(GRAY_BTN);
            btnItalic.setBackground(GRAY_BTN);
            btnFontSize.setBackground(GRAY_BTN);
            btnFontSize.setText("CỠ CHỮ");
            btnTextColor.setBackground(GRAY_BTN);
            return;
        }

        // Đang chọn 1 cột -> cập nhật màu và trạng thái của nút
        btnAlignLeft.setBackground(!selectedUsed.isRightAligned() ? ORANGE : GRAY_BTN);
        btnAlignRight.setBackground(selectedUsed.isRightAligned() ? ORANGE : GRAY_BTN);
        btnBold.setBackground(selectedUsed.isBold() ? ORANGE : GRAY_BTN);
        btnItalic.setBackground(selectedUsed.isItalic() ? ORANGE : GRAY_BTN);
        btnFontSize.setBackground(GRAY_BTN);
        btnFontSize.setText("CỠ CHỮ: " + selectedUsed.getFontSize());
        btnTextColor.setBackground(GRAY_BTN);
    }

    private void updateBottomButtons() {
        btnSoCot.setText("SỐ CỘT: " + config.getGridColumns());
        btnSoDong.setText("SỐ DÒNG: " + config.getGridRows());
        btnHienTieuDe.setBackground(config.isShowTitle() ? ORANGE : OLIVE);

        // Ẩn các nút chi tiết nếu chưa bấm Định Dạng
        btnSoCot.setVisible(isFormattingOpen);
        btnSoDong.setVisible(isFormattingOpen);
        btnMauO.setVisible(isFormattingOpen);
        btnHienTieuDe.setVisible(isFormattingOpen);
    }

    private void updatePreview() {
        previewCard.removeAll();
        previewCard.setBackground(config.getCardBgColor());

        if (config.getUsedColumns().isEmpty()) {
            JLabel empty = new JLabel("Chưa chọn cột hiển thị", SwingConstants.CENTER);
            empty.setForeground(Color.LIGHT_GRAY);
            empty.setFont(font(16));
            previewCard.add(empty, BorderLayout.CENTER);
            previewCard.revalidate();
            previewCard.repaint();
            return;
        }

        // Phân nhóm hiển thị:
        // Cột đầu tiên (nếu căn trái): làm tiêu đề lớn phía trên
        // Các cột còn lại: chia sang 2 bên Trái và Phải
        TouchMatHangColumnConfig.ColumnItem topItem = null;
        List<TouchMatHangColumnConfig.ColumnItem> leftItems = new ArrayList<>();
        List<TouchMatHangColumnConfig.ColumnItem> rightItems = new ArrayList<>();

        for (int i = 0; i < config.getUsedColumns().size(); i++) {
            TouchMatHangColumnConfig.ColumnItem col = config.getUsedColumns().get(i);
            if (i == 0 && !col.isRightAligned()) {
                topItem = col;
            } else if (col.isRightAligned()) {
                rightItems.add(col);
            } else {
                leftItems.add(col);
            }
        }

        if (topItem != null) {
            String txt = getPreviewText(topItem);
            JLabel lblTop = new JLabel(txt);
            lblTop.setForeground(topItem.getColor());
            int fSize = Math.max(16, (int) (topItem.getFontSize() * 0.78));
            int style = (topItem.isBold() ? Font.BOLD : Font.PLAIN) | (topItem.isItalic() ? Font.ITALIC : Font.PLAIN);
            lblTop.setFont(new Font("Segoe UI", style, fSize));
            lblTop.setBorder(new EmptyBorder(3, 8, 2, 8));
            previewCard.add(lblTop, BorderLayout.NORTH);
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 8, 4, 8));

        // Trái
        if (!leftItems.isEmpty()) {
            JPanel pLeft = new JPanel();
            pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
            pLeft.setOpaque(false);
            for (TouchMatHangColumnConfig.ColumnItem col : leftItems) {
                String txt = getPreviewText(col);
                JLabel lbl = new JLabel(txt);
                lbl.setForeground(col.getColor());
                int fSize = Math.max(13, (int) (col.getFontSize() * 0.72));
                int style = (col.isBold() ? Font.BOLD : Font.PLAIN) | (col.isItalic() ? Font.ITALIC : Font.PLAIN);
                lbl.setFont(new Font("Segoe UI", style, fSize));
                pLeft.add(lbl);
            }
            body.add(pLeft, BorderLayout.WEST);
        }

        // Phải
        if (!rightItems.isEmpty()) {
            JPanel pRight = new JPanel();
            pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));
            pRight.setOpaque(false);
            for (TouchMatHangColumnConfig.ColumnItem col : rightItems) {
                String txt = getPreviewText(col);
                JLabel lbl = new JLabel(txt, SwingConstants.RIGHT);
                lbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
                lbl.setForeground(col.getColor());
                int fSize = Math.max(13, (int) (col.getFontSize() * 0.72));
                int style = (col.isBold() ? Font.BOLD : Font.PLAIN) | (col.isItalic() ? Font.ITALIC : Font.PLAIN);
                lbl.setFont(new Font("Segoe UI", style, fSize));
                pRight.add(lbl);
            }
            body.add(pRight, BorderLayout.EAST);
        }

        previewCard.add(body, BorderLayout.CENTER);
        previewCard.revalidate();
        previewCard.repaint();
    }

    private String getPreviewText(TouchMatHangColumnConfig.ColumnItem col) {
        String sampleValue = switch (col.getKey()) {
            case "NAME" -> "Tên mặt hàng";
            case "HANG_SAN_XUAT" -> "Hãng sản xuất";
            case "CODE" -> "Mã hàng";
            case "GIA_BAN" -> "Giá bán";
            case "GHI_CHU" -> "Ghi chú";
            case "NHOM_MAT_HANG" -> "Nhóm mặt hàng";
            case "DON_VI_TINH" -> "Cái";
            case "GIA_NHAP" -> "Giá nhập";
            case "TON_TOI_THIEU" -> "Tồn min";
            case "TON_TOI_DA" -> "Tồn max";
            case "BAO_HANH" -> "Bảo hành";
            case "HOA_HONG" -> "Hoa hồng";
            case "GIA_BAN_2" -> "Giá 2";
            case "GIA_BAN_3" -> "Giá 3";
            case "GIA_BAN_4" -> "Giá 4";
            case "MA_SAN_CO" -> "Mã phụ";
            case "TON_KHO" -> "Tồn kho";
            default -> col.getTitle();
        };
        if (config.isShowTitle() && col.getPrefix() != null && !col.getPrefix().isBlank()) {
            return col.getPrefix() + sampleValue;
        }
        return sampleValue;
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
        int idx = config.getUsedColumns().indexOf(selectedUsed);
        if (idx < 0) return;
        int target = idx + delta;
        if (target < 0 || target >= config.getUsedColumns().size()) return;

        config.getUsedColumns().remove(idx);
        config.getUsedColumns().add(target, selectedUsed);
        refreshLists();
        updatePreview();
    }

    private void saveAndApply() {
        TouchMatHangConfigDAO.saveConfig(config);
        if (onUpdated != null) {
            onUpdated.run();
        }
        dispose();
    }

    private JButton columnButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(font(18));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(190, 210, 210), 2, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        b.setPreferredSize(new Dimension(0, 54));
        return b;
    }

    private static JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(font(17));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(190, 210, 210), 2, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static Font font(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }
}
