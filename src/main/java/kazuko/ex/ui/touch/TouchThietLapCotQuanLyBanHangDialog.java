package kazuko.ex.ui.touch;

import kazuko.ex.model.banhang.DonHang;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * Hộp thoại "THIẾT LẬP CỘT HIỂN THỊ" cho thẻ hóa đơn màn hình Quản lý bán hàng cảm ứng (Hình 2).
 */
public class TouchThietLapCotQuanLyBanHangDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(211, 84, 0);
    private static final Color PURPLE = new Color(106, 13, 84);
    private static final Color OLIVE = new Color(85, 107, 47);
    private static final Color GRAY_BTN = new Color(135, 145, 150);
    private static final Color LIST_BG = new Color(110, 145, 165);

    private final TouchQuanLyBanHangBillColumnConfig config;
    private final Runnable onUpdated;

    private TouchQuanLyBanHangBillColumnConfig.ColumnItem selectedUnused = null;
    private TouchQuanLyBanHangBillColumnConfig.ColumnItem selectedUsed = null;

    private final JPanel unusedPanel = new JPanel();
    private final JPanel usedPanel = new JPanel();
    private final JPanel formatContainer = new JPanel(new BorderLayout(15, 0));
    private final JPanel previewCard = new JPanel();

    // Nút định dạng
    private final JButton btnAlignLeft = button("CĂN TRÁI", GRAY_BTN);
    private final JButton btnAlignRight = button("CĂN PHẢI", GRAY_BTN);
    private final JButton btnBold = button("ĐẬM", GRAY_BTN);
    private final JButton btnItalic = button("NGHIÊNG", GRAY_BTN);
    private final JButton btnFontSize = button("CỠ CHỮ", GRAY_BTN);
    private final JButton btnTextColor = button("MÀU CHỮ", GRAY_BTN);

    private final JTextField txtPrefix = new JTextField();
    private final JButton btnPrefixKb = new JButton("⌨");

    // Nút điều khiển thanh dưới (Hình 2)
    private final JButton btnDinhDang = button("ĐỊNH DẠNG", PURPLE);
    private final JButton btnSoCot = button("SỐ CỘT: 2", OLIVE);
    private final JButton btnSoDong = button("SỐ DÒNG: 6", OLIVE);
    private final JButton btnMauO = button("MÀU Ô", OLIVE);
    private final JButton btnHienTieuDe = button("HIỆN TIÊU ĐỀ", OLIVE);
    private final JButton btnCapNhat = button("CẬP NHẬT", GREEN);
    private final JButton btnThoat = button("THOÁT", RED);

    private final JButton btnMoveUp = button("LÊN", GRAY_BTN);
    private final JButton btnMoveDown = button("XUỐNG", GRAY_BTN);

    private boolean isFormattingOpen = false;

    public TouchThietLapCotQuanLyBanHangDialog(Window owner, Runnable onUpdated) {
        super(owner, "Thiết lập cột hiển thị hóa đơn", ModalityType.APPLICATION_MODAL);
        this.config = TouchQuanLyBanHangBillConfigDAO.loadConfig().clone();
        this.onUpdated = onUpdated;

        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (!config.getUsedColumns().isEmpty()) {
            selectedUsed = config.getUsedColumns().get(0);
        }
        refreshUnusedList();
        refreshUsedList();
        updateFormattingButtonsState();
        updateBottomButtons();
        updatePreviewCard();

        setSize(1080, 620);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER, 3));

        // 1. Tiêu đề hộp thoại
        JLabel lblTitle = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(HEADER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setPreferredSize(new Dimension(0, 44));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Thân chính: Danh sách cột & Bảng định dạng
        JPanel mainBody = new JPanel(new BorderLayout(0, 8));
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(8, 12, 4, 12));

        JPanel columnTransferPanel = createColumnTransferPanel();
        mainBody.add(columnTransferPanel, BorderLayout.CENTER);

        // Khung định dạng bên dưới (Hình 2)
        initFormatContainer();
        formatContainer.setVisible(false);
        mainBody.add(formatContainer, BorderLayout.SOUTH);

        root.add(mainBody, BorderLayout.CENTER);

        // 3. Thanh nút đáy
        JPanel bottomBar = createBottomBar();
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createColumnTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 6, 0, 6);

        // CỘT TRÁI: CÁC CỘT KHÔNG SỬ DỤNG
        JPanel leftBox = new JPanel(new BorderLayout(0, 6));
        leftBox.setOpaque(false);
        JLabel lblLeft = new JLabel("CÁC CỘT KHÔNG SỬ DỤNG");
        lblLeft.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLeft.setForeground(Color.WHITE);
        leftBox.add(lblLeft, BorderLayout.NORTH);

        unusedPanel.setLayout(new BoxLayout(unusedPanel, BoxLayout.Y_AXIS));
        unusedPanel.setOpaque(true);
        unusedPanel.setBackground(LIST_BG);
        unusedPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane spLeft = new JScrollPane(unusedPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spLeft.setBorder(new LineBorder(HEADER, 1, true));
        spLeft.getViewport().setBackground(LIST_BG);
        spLeft.getVerticalScrollBar().setUnitIncrement(35);
        leftBox.add(spLeft, BorderLayout.CENTER);

        c.gridx = 0; c.gridy = 0; c.weightx = 0.44; c.weighty = 1.0;
        panel.add(leftBox, c);

        // CỘT GIỮA: THÊM >>, << XÓA, << XÓA HẾT
        JPanel midBox = new JPanel();
        midBox.setLayout(new BoxLayout(midBox, BoxLayout.Y_AXIS));
        midBox.setOpaque(false);
        midBox.setBorder(new EmptyBorder(25, 4, 10, 4));

        JButton btnAdd = button("THÊM >>", GRAY_BTN);
        btnAdd.setPreferredSize(new Dimension(130, 46));
        btnAdd.setMaximumSize(new Dimension(130, 46));
        btnAdd.addActionListener(e -> onAddColumn());

        JButton btnRemove = button("<< XÓA", GRAY_BTN);
        btnRemove.setPreferredSize(new Dimension(130, 46));
        btnRemove.setMaximumSize(new Dimension(130, 46));
        btnRemove.addActionListener(e -> onRemoveColumn());

        JButton btnRemoveAll = button("<< XÓA HẾT", GREEN);
        btnRemoveAll.setPreferredSize(new Dimension(130, 46));
        btnRemoveAll.setMaximumSize(new Dimension(130, 46));
        btnRemoveAll.addActionListener(e -> onRemoveAllColumns());

        midBox.add(Box.createVerticalGlue());
        midBox.add(btnAdd);
        midBox.add(Box.createVerticalStrut(10));
        midBox.add(btnRemove);
        midBox.add(Box.createVerticalStrut(10));
        midBox.add(btnRemoveAll);
        midBox.add(Box.createVerticalGlue());

        c.gridx = 1; c.gridy = 0; c.weightx = 0.12; c.weighty = 1.0;
        panel.add(midBox, c);

        // CỘT PHẢI: CÁC CỘT SỬ DỤNG
        JPanel rightBox = new JPanel(new BorderLayout(0, 6));
        rightBox.setOpaque(false);
        JLabel lblRight = new JLabel("CÁC CỘT SỬ DỤNG");
        lblRight.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRight.setForeground(Color.WHITE);
        rightBox.add(lblRight, BorderLayout.NORTH);

        usedPanel.setLayout(new BoxLayout(usedPanel, BoxLayout.Y_AXIS));
        usedPanel.setOpaque(true);
        usedPanel.setBackground(LIST_BG);
        usedPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane spRight = new JScrollPane(usedPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spRight.setBorder(new LineBorder(HEADER, 1, true));
        spRight.getViewport().setBackground(LIST_BG);
        spRight.getVerticalScrollBar().setUnitIncrement(35);
        rightBox.add(spRight, BorderLayout.CENTER);

        c.gridx = 2; c.gridy = 0; c.weightx = 0.44; c.weighty = 1.0;
        panel.add(rightBox, c);

        // CỘT NGOÀI CÙNG BÊN PHẢI: LÊN, XUỐNG
        JPanel reorderBox = new JPanel();
        reorderBox.setLayout(new BoxLayout(reorderBox, BoxLayout.Y_AXIS));
        reorderBox.setOpaque(false);
        reorderBox.setBorder(new EmptyBorder(25, 4, 10, 0));

        btnMoveUp.setPreferredSize(new Dimension(105, 46));
        btnMoveUp.setMaximumSize(new Dimension(105, 46));
        btnMoveUp.addActionListener(e -> onMoveUp());

        btnMoveDown.setPreferredSize(new Dimension(105, 46));
        btnMoveDown.setMaximumSize(new Dimension(105, 46));
        btnMoveDown.addActionListener(e -> onMoveDown());

        reorderBox.add(Box.createVerticalGlue());
        reorderBox.add(btnMoveUp);
        reorderBox.add(Box.createVerticalStrut(10));
        reorderBox.add(btnMoveDown);
        reorderBox.add(Box.createVerticalGlue());

        c.gridx = 3; c.gridy = 0; c.weightx = 0.10; c.weighty = 1.0;
        panel.add(reorderBox, c);

        return panel;
    }

    private void initFormatContainer() {
        formatContainer.setOpaque(false);
        formatContainer.setBorder(new EmptyBorder(8, 6, 4, 6));

        // 1. Thẻ xem trước (previewCard) ở bên trái
        previewCard.setPreferredSize(new Dimension(280, 85));
        previewCard.setBorder(new CompoundBorder(
                new LineBorder(Color.WHITE, 2, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        previewCard.setOpaque(true);
        previewCard.setBackground(config.getCardBgColor());
        formatContainer.add(previewCard, BorderLayout.WEST);

        // 2. Cụm nút định dạng và Tiêu đề viết tắt bên phải
        JPanel centerFormat = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        centerFormat.setOpaque(false);

        JPanel ctrlGrid = new JPanel(new GridBagLayout());
        ctrlGrid.setOpaque(false);
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets = new Insets(3, 4, 3, 4);
        cc.fill = GridBagConstraints.BOTH;

        btnAlignLeft.setPreferredSize(new Dimension(95, 38));
        btnAlignRight.setPreferredSize(new Dimension(95, 38));
        btnBold.setPreferredSize(new Dimension(60, 38));
        btnItalic.setPreferredSize(new Dimension(75, 38));
        btnFontSize.setPreferredSize(new Dimension(80, 38));
        btnTextColor.setPreferredSize(new Dimension(80, 38));

        btnAlignLeft.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setRightAligned(false);
                updateFormattingButtonsState();
                updatePreviewCard();
            }
        });
        btnAlignRight.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setRightAligned(true);
                updateFormattingButtonsState();
                updatePreviewCard();
            }
        });
        btnBold.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setBold(!selectedUsed.isBold());
                updateFormattingButtonsState();
                updatePreviewCard();
            }
        });
        btnItalic.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setItalic(!selectedUsed.isItalic());
                updateFormattingButtonsState();
                updatePreviewCard();
            }
        });
        btnFontSize.addActionListener(e -> {
            if (selectedUsed != null) {
                Integer sz = TouchNumberInputDialog.showDialog(this, "CỠ CHỮ CỘT HIỂN THỊ", selectedUsed.getFontSize(), 8, 36);
                if (sz != null && sz > 0) {
                    selectedUsed.setFontSize(sz);
                    updateFormattingButtonsState();
                    updatePreviewCard();
                }
            }
        });
        btnTextColor.addActionListener(e -> {
            if (selectedUsed != null) {
                Color picked = TouchChonMauDialog.showDialog(this, selectedUsed.getColor());
                if (picked != null) {
                    selectedUsed.setColor(picked);
                    updateFormattingButtonsState();
                    updatePreviewCard();
                }
            }
        });

        // Hàng 1: CĂN TRÁI, CĂN PHẢI
        cc.gridx = 0; cc.gridy = 0; cc.gridwidth = 1; ctrlGrid.add(btnAlignLeft, cc);
        cc.gridx = 1; cc.gridy = 0; cc.gridwidth = 1; ctrlGrid.add(btnAlignRight, cc);

        // Hàng 2: ĐẬM, NGHIÊNG, CỠ CHỮ, MÀU CHỮ
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row2.setOpaque(false);
        row2.add(btnBold);
        row2.add(btnItalic);
        row2.add(btnFontSize);
        row2.add(btnTextColor);

        cc.gridx = 0; cc.gridy = 1; cc.gridwidth = 2;
        ctrlGrid.add(row2, cc);

        centerFormat.add(ctrlGrid);

        // Cụm Tiêu đề viết tắt (Hình 2)
        JPanel prefixBox = new JPanel();
        prefixBox.setLayout(new BoxLayout(prefixBox, BoxLayout.Y_AXIS));
        prefixBox.setOpaque(false);

        JLabel lblPrefix = new JLabel("Tiêu đề viết tắt");
        lblPrefix.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrefix.setForeground(Color.WHITE);
        prefixBox.add(lblPrefix);
        prefixBox.add(Box.createVerticalStrut(4));

        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlInput.setOpaque(false);
        txtPrefix.setPreferredSize(new Dimension(110, 38));
        txtPrefix.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPrefix.addActionListener(e -> {
            if (selectedUsed != null) {
                selectedUsed.setPrefix(txtPrefix.getText().trim().isEmpty() ? "" : txtPrefix.getText().trim() + ": ");
                updatePreviewCard();
            }
        });

        btnPrefixKb.setPreferredSize(new Dimension(42, 38));
        btnPrefixKb.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPrefixKb.setBackground(new Color(240, 240, 240));
        btnPrefixKb.setFocusPainted(false);
        btnPrefixKb.addActionListener(e -> {
            if (selectedUsed != null) {
                String cur = selectedUsed.getPrefix() != null ? selectedUsed.getPrefix().replace(":", "").trim() : "";
                String res = TouchTextKeyboardDialog.showDialog(this, "NHẬP TIÊU ĐỀ VIẾT TẮT", cur);
                if (res != null) {
                    txtPrefix.setText(res);
                    selectedUsed.setPrefix(res.isEmpty() ? "" : res + ": ");
                    updatePreviewCard();
                }
            }
        });

        pnlInput.add(txtPrefix);
        pnlInput.add(btnPrefixKb);
        prefixBox.add(pnlInput);

        centerFormat.add(prefixBox);
        formatContainer.add(centerFormat, BorderLayout.CENTER);
    }

    private JPanel createBottomBar() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setPreferredSize(new Dimension(0, 56));
        bottomBar.setBorder(new EmptyBorder(4, 15, 10, 15));

        // Trái: ĐỊNH DẠNG + SỐ CỘT + SỐ DÒNG + MÀU Ô + HIỆN TIÊU ĐỀ (Hình 2)
        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBottom.setOpaque(false);

        btnDinhDang.setPreferredSize(new Dimension(135, 46));
        btnDinhDang.addActionListener(e -> toggleFormattingMode());
        leftBottom.add(btnDinhDang);

        btnSoCot.setPreferredSize(new Dimension(115, 46));
        TouchGridSettingsSupport.bind(this, btnSoCot, btnSoDong, config, this::updateBottomButtons);
        leftBottom.add(btnSoCot);

        btnSoDong.setPreferredSize(new Dimension(115, 46));
        leftBottom.add(btnSoDong);

        btnMauO.setPreferredSize(new Dimension(105, 46));
        btnMauO.addActionListener(e -> {
            Color picked = TouchChonMauDialog.showDialog(this, config.getCardBgColor());
            if (picked != null) {
                config.setCardBgColor(picked);
                previewCard.setBackground(picked);
                updatePreviewCard();
            }
        });
        leftBottom.add(btnMauO);

        btnHienTieuDe.setPreferredSize(new Dimension(145, 46));
        btnHienTieuDe.addActionListener(e -> {
            config.setShowTitle(!config.isShowTitle());
            updateBottomButtons();
            updatePreviewCard();
        });
        leftBottom.add(btnHienTieuDe);

        bottomBar.add(leftBottom, BorderLayout.WEST);

        // Phải: CẬP NHẬT, THOÁT
        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBottom.setOpaque(false);

        btnCapNhat.setPreferredSize(new Dimension(130, 46));
        btnCapNhat.addActionListener(e -> onSaveAndClose());
        rightBottom.add(btnCapNhat);

        btnThoat.setPreferredSize(new Dimension(110, 46));
        btnThoat.addActionListener(e -> dispose());
        rightBottom.add(btnThoat);

        bottomBar.add(rightBottom, BorderLayout.EAST);
        return bottomBar;
    }

    void toggleFormattingMode() {
        isFormattingOpen = !isFormattingOpen;
        formatContainer.setVisible(isFormattingOpen);
        btnDinhDang.setBackground(isFormattingOpen ? ORANGE : PURPLE);
        revalidate();
        repaint();
    }

    private void updateBottomButtons() {
        TouchGridSettingsSupport.refreshLabels(btnSoCot, btnSoDong, config);
        btnHienTieuDe.setBackground(config.isShowTitle() ? ORANGE : OLIVE);
    }

    private void refreshUnusedList() {
        unusedPanel.removeAll();
        for (TouchQuanLyBanHangBillColumnConfig.ColumnItem item : config.getUnusedColumns()) {
            JButton btn = createColumnButton(item.getTitle(), item == selectedUnused);
            btn.addActionListener(e -> {
                selectedUnused = item;
                refreshUnusedList();
            });
            unusedPanel.add(btn);
            unusedPanel.add(Box.createVerticalStrut(6));
        }
        unusedPanel.revalidate();
        unusedPanel.repaint();
    }

    private void refreshUsedList() {
        usedPanel.removeAll();
        for (TouchQuanLyBanHangBillColumnConfig.ColumnItem item : config.getUsedColumns()) {
            JButton btn = createColumnButton(item.getTitle(), item == selectedUsed);
            btn.addActionListener(e -> {
                selectedUsed = item;
                refreshUsedList();
                updateFormattingButtonsState();
            });
            usedPanel.add(btn);
            usedPanel.add(Box.createVerticalStrut(6));
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
        txtPrefix.setEnabled(hasSel);
        btnPrefixKb.setEnabled(hasSel);

        if (hasSel) {
            btnAlignLeft.setBackground(selectedUsed.isRightAligned() ? GRAY_BTN : ORANGE);
            btnAlignRight.setBackground(selectedUsed.isRightAligned() ? ORANGE : GRAY_BTN);
            btnBold.setBackground(selectedUsed.isBold() ? ORANGE : GRAY_BTN);
            btnItalic.setBackground(selectedUsed.isItalic() ? ORANGE : GRAY_BTN);
            btnFontSize.setText("CỠ CHỮ: " + selectedUsed.getFontSize());
            txtPrefix.setText(selectedUsed.getPrefix() != null ? selectedUsed.getPrefix().replace(":", "").trim() : "");
        } else {
            btnAlignLeft.setBackground(GRAY_BTN);
            btnAlignRight.setBackground(GRAY_BTN);
            btnBold.setBackground(GRAY_BTN);
            btnItalic.setBackground(GRAY_BTN);
            btnFontSize.setText("CỠ CHỮ");
            txtPrefix.setText("");
        }
    }

    private void updatePreviewCard() {
        previewCard.removeAll();
        previewCard.setLayout(new GridLayout(2, 2, 8, 4));
        previewCard.setBackground(config.getCardBgColor());

        DonHang sample = new DonHang();
        sample.setName("0126000001");
        sample.setTongCong(1668000);
        sample.setTenKhach("Khách lẻ");
        sample.setNguoiThanhToan("Administrator");
        sample.setNgay(new Date());
        sample.setTienHang(1668000);

        List<TouchQuanLyBanHangBillColumnConfig.ColumnItem> used = config.getUsedColumns();
        for (int i = 0; i < 4; i++) {
            if (i < used.size()) {
                TouchQuanLyBanHangBillColumnConfig.ColumnItem col = used.get(i);
                String val = TouchQuanLyBanHangBillColumnConfig.getFormattedText(sample, col, config.isShowTitle());
                JLabel lbl = new JLabel(val, col.isRightAligned() ? SwingConstants.RIGHT : SwingConstants.LEFT);
                lbl.setFont(col.getFont());
                lbl.setForeground(col.getColor());
                previewCard.add(lbl);
            } else {
                previewCard.add(new JLabel(""));
            }
        }

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
            updateFormattingButtonsState();
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
        TouchQuanLyBanHangBillConfigDAO.saveConfig(config);
        if (onUpdated != null) {
            onUpdated.run();
        }
        dispose();
    }

    private JButton createColumnButton(String text, boolean isSelected) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setPreferredSize(new Dimension(280, 46));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(isSelected ? ORANGE : new Color(0, 79, 76));
        btn.setBorder(new LineBorder(isSelected ? Color.WHITE : new Color(0, 110, 110), isSelected ? 2 : 1, true));
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
