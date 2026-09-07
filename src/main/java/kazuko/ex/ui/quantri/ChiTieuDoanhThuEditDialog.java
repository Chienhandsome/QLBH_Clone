package kazuko.ex.ui.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import kazuko.ex.ui.common.DropdownWithActionBar;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

final class ChiTieuDoanhThuEditDialog extends JDialog {
    private final ChiTieuDoanhThuDAO dao;
    private final Consumer<String> onSaved;
    private final List<ChiTieuDoanhThu> navigation;
    private ChiTieuDoanhThu target;

    private final JTextField year = new JTextField(6);
    private final DropdownWithActionBar<Group> groups = new DropdownWithActionBar<>();
    private final JTextField note = new JTextField(16);
    private final JTextField[] months = new JTextField[12];

    private final DefaultTableModel provinceModel;
    private final JTable provinceTable;

    private final Map<String, String> allProvinces = new LinkedHashMap<>();
    private String baseline;
    private final DecimalFormat dfMoney = new DecimalFormat("#,##0.##");

    ChiTieuDoanhThuEditDialog(Window owner, ChiTieuDoanhThuDAO dao, ChiTieuDoanhThu target,
                              List<ChiTieuDoanhThu> navigation, Consumer<String> onSaved) throws Exception {
        super(owner, target.id == null ? "CHỈ TIÊU DOANH THU - THÊM MỚI" : "CHỈ TIÊU DOANH THU - CHỈNH SỬA", ModalityType.APPLICATION_MODAL);
        this.dao = dao;
        this.target = target;
        this.navigation = navigation == null ? List.of() : navigation;
        this.onSaved = onSaved;

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(820, 580));

        // 1. Header Banner (Matching Screenshots 1 & 4)
        JPanel banner = new JPanel(new BorderLayout(10, 0));
        banner.setBackground(Color.WHITE);
        banner.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(205, 215, 228)),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JPanel bannerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        bannerLeft.setOpaque(false);

        JLabel iconBanner = new JLabel(IconHelper.getIcon("1282529873_objects.png", 32, 32));
        if (iconBanner.getIcon() == null) {
            iconBanner.setIcon(IconHelper.getIcon("Plugin.png", 32, 32));
        }
        bannerLeft.add(iconBanner);

        JLabel lblTitle = new JLabel("Chỉ tiêu doanh thu");
        lblTitle.setFont(ChiTieuUI.FONT_TITLE);
        lblTitle.setForeground(new Color(20, 45, 85));
        bannerLeft.add(lblTitle);
        banner.add(bannerLeft, BorderLayout.WEST);

        JLabel lblShortcuts = new JLabel("(Ctrl+S: Lưu&Mới, Ctrl+L: Lưu, Ctrl+N: Mới, Ctrl+P: In, ESC: Thoát)");
        lblShortcuts.setFont(ChiTieuUI.FONT_SMALL);
        lblShortcuts.setForeground(new Color(70, 70, 70));
        banner.add(lblShortcuts, BorderLayout.EAST);

        // 2. Ribbon Toolbar (Matching Screenshots 1 & 4)
        JPanel ribbon = new JPanel(new BorderLayout());
        ribbon.setBackground(ChiTieuUI.COLOR_RIBBON_BG);
        ribbon.setBorder(new MatteBorder(0, 0, 1, 0, ChiTieuUI.COLOR_RIBBON_BORDER));
        ribbon.setPreferredSize(new Dimension(800, 28));

        JPanel ribbonLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        ribbonLeft.setOpaque(false);

        JButton btnShortcuts = ChiTieuUI.ribbonButton("Phím tắt ▾", null);
        btnShortcuts.setFont(ChiTieuUI.FONT_SMALL);
        JButton btnPrev = ChiTieuUI.ribbonButton("Trước (F10)", "arrow-180-medium.png");
        JButton btnNext = ChiTieuUI.ribbonButton("Sau (F11)", "arrow-000-medium.png");
        JButton btnNew = ChiTieuUI.ribbonButton("Tạo mới", "plus.png");
        JButton btnCopy = ChiTieuUI.ribbonButton("Sao chép", "Copy.png");

        btnShortcuts.addActionListener(e -> showShortcutMenu(btnShortcuts));
        btnPrev.addActionListener(e -> navigate(-1));
        btnNext.addActionListener(e -> navigate(1));
        btnNew.addActionListener(e -> resetForm(new ChiTieuDoanhThu()));
        btnCopy.addActionListener(e -> resetForm(this.target.copy(true)));

        ribbonLeft.add(btnShortcuts);
        ribbonLeft.add(btnPrev);
        ribbonLeft.add(btnNext);
        ribbonLeft.add(btnNew);
        ribbonLeft.add(btnCopy);
        ribbon.add(ribbonLeft, BorderLayout.WEST);

        JPanel ribbonRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        ribbonRight.setOpaque(false);
        JButton btnDelete = ChiTieuUI.ribbonButton("Xóa", "cross.png");
        btnDelete.setForeground(new Color(180, 20, 20));
        btnDelete.setEnabled(target.id != null);
        btnDelete.addActionListener(e -> deleteCurrent());
        ribbonRight.add(btnDelete);
        ribbon.add(ribbonRight, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(banner, BorderLayout.NORTH);
        topContainer.add(ribbon, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 3. Form Body (Matching Screenshots 1 & 4)
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setBackground(ChiTieuUI.COLOR_FORM_BG);
        body.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Top info + 12 Months
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Năm, Phân nhóm, Ghi chú
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblYear = new JLabel("Năm");
        lblYear.setFont(ChiTieuUI.FONT);
        pnlFields.add(lblYear, gbc);

        gbc.gridx = 1; gbc.weightx = 0.15;
        year.setFont(ChiTieuUI.FONT);
        year.setBackground(new Color(235, 248, 255));
        year.setBorder(new CompoundBorder(new LineBorder(new Color(160, 190, 220), 1), new EmptyBorder(2, 4, 2, 4)));
        pnlFields.add(year, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblGroup = new JLabel("Phân nhóm");
        lblGroup.setFont(ChiTieuUI.FONT);
        pnlFields.add(lblGroup, gbc);

        gbc.gridx = 3; gbc.weightx = 0.35;
        groups.setPreferredSize(new Dimension(160, 24));
        groups.setDisplayConverter(g -> g != null ? g.name() : "");
        groups.setOnThem(() -> {
            Group draft = new Group(null, "", "", null, false, null, 30);
            String id = NhomChiTieuDialog.show(this, dao, draft);
            reloadGroups(id);
        });
        groups.setOnTai(() -> reloadGroups(groups.getSelectedItem() != null ? groups.getSelectedItem().id() : null));
        groups.setOnDanhMuc(() -> {
            Group sel = groups.getSelectedItem();
            if (sel != null) {
                NhomChiTieuDialog.show(this, dao, sel);
                reloadGroups(sel.id());
            }
        });
        pnlFields.add(groups, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(ChiTieuUI.FONT);
        pnlFields.add(lblNote, gbc);

        gbc.gridx = 5; gbc.weightx = 0.5;
        note.setFont(ChiTieuUI.FONT);
        note.setBackground(Color.WHITE);
        note.setBorder(new CompoundBorder(new LineBorder(new Color(160, 190, 220), 1), new EmptyBorder(2, 4, 2, 4)));
        pnlFields.add(note, gbc);

        // 12 Months in 4 columns x 3 rows (Matching Screenshots 1 & 4)
        for (int i = 0; i < 12; i++) {
            months[i] = new JTextField("0", 8);
            months[i].setFont(ChiTieuUI.FONT);
            months[i].setHorizontalAlignment(SwingConstants.RIGHT);
            months[i].setBackground(Color.WHITE);
            months[i].setBorder(new CompoundBorder(new LineBorder(new Color(160, 190, 220), 1), new EmptyBorder(2, 4, 2, 4)));
        }

        for (int col = 0; col < 4; col++) {
            for (int r = 0; r < 3; r++) {
                int mIdx = col * 3 + r;
                gbc.gridy = 1 + r;
                gbc.gridx = col < 2 ? col * 2 : 2 + col * 2;
                gbc.weightx = 0;

                JLabel lblM = new JLabel("Tháng " + (mIdx + 1));
                lblM.setFont(ChiTieuUI.FONT);
                pnlFields.add(lblM, gbc);

                gbc.gridx = gbc.gridx + 1;
                gbc.weightx = 0.25;
                pnlFields.add(months[mIdx], gbc);
            }
        }

        body.add(pnlFields, BorderLayout.NORTH);

        // Province Targets Section (Matching Screenshots 1 & 4)
        JPanel pnlProvinceSection = new JPanel(new BorderLayout(0, 4));
        pnlProvinceSection.setOpaque(false);

        JLabel lblProvTitle = new JLabel("Chỉ tiêu theo tỉnh");
        lblProvTitle.setFont(ChiTieuUI.FONT_BOLD);
        lblProvTitle.setForeground(new Color(25, 55, 105));
        lblProvTitle.setBorder(new EmptyBorder(4, 2, 2, 2));
        pnlProvinceSection.add(lblProvTitle, BorderLayout.NORTH);

        // In-place editable Province Table
        provinceModel = new DefaultTableModel(new String[]{"", "Tỉnh thành", "Chỉ tiêu", "Ghi chú"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3; // Allow editing Chỉ tiêu and Ghi chú
            }
        };

        provinceTable = new JTable(provinceModel);
        provinceTable.setFont(ChiTieuUI.FONT);
        provinceTable.setRowHeight(22);
        provinceTable.setShowGrid(true);
        provinceTable.setGridColor(ChiTieuUI.COLOR_TABLE_GRID);
        provinceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        provinceTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JTableHeader pth = provinceTable.getTableHeader();
        pth.setFont(ChiTieuUI.FONT);
        pth.setBackground(ChiTieuUI.COLOR_TABLE_HEADER_BG);
        pth.setForeground(new Color(30, 30, 30));
        pth.setPreferredSize(new Dimension(100, 24));
        pth.setReorderingAllowed(false);

        setupProvinceTableRenderers();

        JScrollPane scrollProvince = new JScrollPane(provinceTable);
        scrollProvince.setBorder(new LineBorder(new Color(160, 190, 220), 1));
        scrollProvince.setPreferredSize(new Dimension(600, 200));
        pnlProvinceSection.add(scrollProvince, BorderLayout.CENTER);

        body.add(pnlProvinceSection, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // 4. Footer Action Bar (Matching Screenshots 1 & 4)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        footer.setBackground(ChiTieuUI.COLOR_FOOTER_BG);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, ChiTieuUI.COLOR_FOOTER_BORDER));

        JButton btnSave = ChiTieuUI.actionButton("Lưu", "Save1.png");
        JButton btnSaveAndNew = ChiTieuUI.actionButton("Lưu & Mới", null);
        JButton btnSaveAndClose = ChiTieuUI.actionButton("Lưu & thoát", null);
        JButton btnExit = ChiTieuUI.actionButton("Thoát", null);

        btnSave.addActionListener(e -> save(false, false));
        btnSaveAndNew.addActionListener(e -> save(true, false));
        btnSaveAndClose.addActionListener(e -> save(false, true));
        btnExit.addActionListener(e -> checkClose());

        footer.add(btnSave);
        footer.add(btnSaveAndNew);
        footer.add(btnSaveAndClose);
        footer.add(btnExit);
        add(footer, BorderLayout.SOUTH);

        // Load data
        loadProvinces();
        reloadGroups(target.groupId);
        populate(target);

        // Shortcuts
        setupShortcuts(btnSave, btnSaveAndNew, btnSaveAndClose);

        pack();
        setSize(840, 600);
        setLocationRelativeTo(owner);
    }

    private void setupProvinceTableRenderers() {
        DefaultTableCellRenderer pointerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setText(String.valueOf(row + 1));
                lbl.setIcon(isSelected ? ChiTieuUI.POINTER_ICON : null);
                if (isSelected) {
                    lbl.setBackground(new Color(0, 120, 215));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : ChiTieuUI.COLOR_TABLE_ROW_ALT);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        };

        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                if (isSelected) {
                    lbl.setBackground(new Color(0, 120, 215));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(255, 255, 235));
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        };

        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number || value instanceof BigDecimal) {
                    lbl.setText(dfMoney.format(value));
                } else if (value != null && !value.toString().trim().isEmpty()) {
                    try {
                        lbl.setText(dfMoney.format(new BigDecimal(value.toString().trim().replace(",", ""))));
                    } catch (Exception ignored) {}
                }
                if (isSelected) {
                    lbl.setBackground(new Color(220, 220, 220));
                    lbl.setForeground(Color.BLACK);
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : ChiTieuUI.COLOR_TABLE_ROW_ALT);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        };

        DefaultTableCellRenderer noteRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                if (isSelected) {
                    lbl.setBackground(new Color(230, 245, 255));
                    lbl.setForeground(Color.BLACK);
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : ChiTieuUI.COLOR_TABLE_ROW_ALT);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        };

        for (int i = 0; i < provinceTable.getColumnCount(); i++) {
            TableColumn col = provinceTable.getColumnModel().getColumn(i);
            if (i == 0) {
                col.setCellRenderer(pointerRenderer);
                col.setPreferredWidth(32);
                col.setMaxWidth(40);
            } else if (i == 1) {
                col.setCellRenderer(nameRenderer);
                col.setPreferredWidth(260);
            } else if (i == 2) {
                col.setCellRenderer(amountRenderer);
                col.setPreferredWidth(160);
            } else {
                col.setCellRenderer(noteRenderer);
                col.setPreferredWidth(220);
            }
        }
    }

    private void loadProvinces() {
        allProvinces.clear();
        try {
            allProvinces.putAll(dao.provinces());
        } catch (Exception ignored) {}
    }

    private void reloadGroups(String selectGroupId) {
        try {
            List<Group> list = dao.groups();
            List<Group> nonFolders = new ArrayList<>();
            for (Group g : list) {
                if (!g.folder() && g.status() >= 0) nonFolders.add(g);
            }
            groups.setItems(nonFolders);
            if (selectGroupId != null) {
                for (Group g : nonFolders) {
                    if (Objects.equals(g.id(), selectGroupId)) {
                        groups.setSelectedItem(g);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void populate(ChiTieuDoanhThu t) {
        year.setText(String.valueOf(t.year));
        note.setText(ChiTieuUI.text(t.note));
        for (int i = 0; i < 12; i++) {
            months[i].setText(t.months[i] == null ? "0" : dfMoney.format(t.months[i]));
        }

        // Map existing province targets by provinceId
        Map<String, ProvinceTarget> existing = new HashMap<>();
        for (ProvinceTarget p : t.provinces) {
            if (p.provinceId() != null) {
                existing.put(p.provinceId(), p);
            }
        }

        provinceModel.setRowCount(0);
        int idx = 1;
        for (Map.Entry<String, String> entry : allProvinces.entrySet()) {
            String pId = entry.getKey();
            String pName = entry.getValue();
            ProvinceTarget p = existing.get(pId);
            provinceModel.addRow(new Object[]{
                    String.valueOf(idx++),
                    pName,
                    p != null && p.amount() != null && p.amount().compareTo(BigDecimal.ZERO) != 0 ? dfMoney.format(p.amount()) : "",
                    p != null ? ChiTieuUI.text(p.note()) : ""
            });
        }

        if (provinceModel.getRowCount() > 0) {
            provinceTable.setRowSelectionInterval(0, 0);
        }

        baseline = snapshot();
    }

    private String snapshot() {
        StringBuilder sb = new StringBuilder(year.getText() + "|" + (groups.getSelectedItem() == null ? "" : groups.getSelectedItem().id()) + "|" + note.getText());
        for (JTextField m : months) sb.append("|").append(m.getText().trim());
        for (int r = 0; r < provinceModel.getRowCount(); r++) {
            sb.append("|").append(provinceModel.getValueAt(r, 1))
                    .append(":").append(provinceModel.getValueAt(r, 2))
                    .append(":").append(provinceModel.getValueAt(r, 3));
        }
        return sb.toString();
    }

    private void showShortcutMenu(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miSave = new JMenuItem("Lưu (Ctrl+L)", IconHelper.getIcon("Save1.png", 16, 16));
        JMenuItem miSaveNew = new JMenuItem("Lưu & Mới (Ctrl+S)", IconHelper.getIcon("btnNew.Image.png", 16, 16));
        JMenuItem miSaveClose = new JMenuItem("Lưu & Thoát");
        JMenuItem miNew = new JMenuItem("Tạo mới (Ctrl+N)", IconHelper.getIcon("plus.png", 16, 16));
        JMenuItem miPrint = new JMenuItem("In (Ctrl+P)", IconHelper.getIcon("Preview.png", 16, 16));
        JMenuItem miExit = new JMenuItem("Thoát (ESC)", IconHelper.getIcon("cross.png", 16, 16));

        miSave.addActionListener(e -> save(false, false));
        miSaveNew.addActionListener(e -> save(true, false));
        miSaveClose.addActionListener(e -> save(false, true));
        miNew.addActionListener(e -> resetForm(new ChiTieuDoanhThu()));
        miPrint.addActionListener(e -> ChiTieuUI.print(this, provinceTable, "Chỉ tiêu doanh thu " + year.getText()));
        miExit.addActionListener(e -> checkClose());

        popup.add(miSave);
        popup.add(miSaveNew);
        popup.add(miSaveClose);
        popup.addSeparator();
        popup.add(miNew);
        popup.add(miPrint);
        popup.addSeparator();
        popup.add(miExit);

        popup.show(invoker, 0, invoker.getHeight());
    }

    private void navigate(int delta) {
        if (target.id == null || navigation.isEmpty()) return;
        if (!Objects.equals(baseline, snapshot())) {
            int opt = JOptionPane.showConfirmDialog(this, "Dữ liệu đã thay đổi. Bạn có muốn lưu trước khi chuyển?", "Xác nhận", JOptionPane.YES_NO_CANCEL_OPTION);
            if (opt == JOptionPane.CANCEL_OPTION) return;
            if (opt == JOptionPane.YES_OPTION) save(false, false);
        }
        int index = -1;
        for (int i = 0; i < navigation.size(); i++) {
            if (Objects.equals(navigation.get(i).id, target.id)) {
                index = i;
                break;
            }
        }
        int next = index + delta;
        if (next >= 0 && next < navigation.size()) {
            resetForm(navigation.get(next).copy(false));
        }
    }

    private void resetForm(ChiTieuDoanhThu newTarget) {
        this.target = newTarget;
        setTitle(target.id == null ? "CHỈ TIÊU DOANH THU - THÊM MỚI" : "CHỈ TIÊU DOANH THU - CHỈNH SỬA");
        if (target.id != null) {
            try {
                target.provinces.clear();
                target.provinces.addAll(dao.details(target.id));
            } catch (Exception ignored) {}
        }
        populate(target);
    }

    private void deleteCurrent() {
        if (target.id == null) return;
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa kế hoạch năm " + target.year + "?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            dao.setTrash(target.id, false, true);
            if (onSaved != null) onSaved.accept(null);
            dispose();
        } catch (Exception e) {
            ChiTieuUI.error(this, e);
        }
    }

    private void save(boolean createNewAfter, boolean closeAfter) {
        if (provinceTable.isEditing()) {
            provinceTable.getCellEditor().stopCellEditing();
        }

        int yr;
        try {
            yr = Integer.parseInt(year.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập năm hợp lệ (1900 - 9999).", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            year.requestFocusInWindow();
            return;
        }

        Group selGroup = groups.getSelectedItem();
        target.year = yr;
        target.groupId = selGroup == null ? null : selGroup.id();
        target.groupName = selGroup == null ? "" : selGroup.name();
        target.note = note.getText().trim();

        for (int i = 0; i < 12; i++) {
            target.months[i] = ChiTieuUI.amount(months[i].getText());
        }

        // Collect province targets
        target.provinces.clear();
        int rowCount = provinceModel.getRowCount();
        for (int r = 0; r < rowCount; r++) {
            String pName = (String) provinceModel.getValueAt(r, 1);
            Object amtObj = provinceModel.getValueAt(r, 2);
            Object noteObj = provinceModel.getValueAt(r, 3);

            BigDecimal amt = BigDecimal.ZERO;
            if (amtObj != null && !amtObj.toString().trim().isEmpty()) {
                try {
                    amt = ChiTieuUI.amount(amtObj);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ tại tỉnh " + pName + ": " + amtObj, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String pNote = noteObj == null ? "" : noteObj.toString().trim();
            if (amt.compareTo(BigDecimal.ZERO) > 0 || !pNote.isEmpty()) {
                String pId = findProvinceId(pName);
                if (pId != null) {
                    target.provinces.add(new ProvinceTarget(pId, pName, amt, pNote));
                }
            }
        }

        try {
            target.validate();
            dao.save(target);
            String savedId = target.id;
            baseline = snapshot();
            if (onSaved != null) onSaved.accept(savedId);

            if (closeAfter) {
                dispose();
            } else if (createNewAfter) {
                resetForm(new ChiTieuDoanhThu());
                JOptionPane.showMessageDialog(this, "Đã lưu thành công! Tiếp tục thêm kế hoạch mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                setTitle("CHỈ TIÊU DOANH THU - CHỈNH SỬA");
                JOptionPane.showMessageDialog(this, "Đã lưu thành công kế hoạch chỉ tiêu doanh thu năm " + target.year + "!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            ChiTieuUI.error(this, e);
        }
    }

    private String findProvinceId(String provinceName) {
        for (Map.Entry<String, String> entry : allProvinces.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(provinceName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void checkClose() {
        if (!Objects.equals(baseline, snapshot())) {
            int opt = JOptionPane.showConfirmDialog(this, "Dữ liệu đã bị thay đổi. Bạn có muốn thoát mà không lưu?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt != JOptionPane.YES_OPTION) return;
        }
        dispose();
    }

    private void setupShortcuts(JButton btnSave, JButton btnSaveAndNew, JButton btnSaveAndClose) {
        ChiTieuUI.bind(getRootPane(), "ESCAPE", this::checkClose);
        ChiTieuUI.bind(getRootPane(), "F2", () -> save(false, false));
        ChiTieuUI.bind(getRootPane(), "F10", () -> navigate(-1));
        ChiTieuUI.bind(getRootPane(), "F11", () -> navigate(1));
        ChiTieuUI.bind(getRootPane(), "control S", () -> save(true, false));
        ChiTieuUI.bind(getRootPane(), "control L", () -> save(false, false));
        ChiTieuUI.bind(getRootPane(), "control N", () -> resetForm(new ChiTieuDoanhThu()));
        ChiTieuUI.bind(getRootPane(), "control P", () -> ChiTieuUI.print(this, provinceTable, "Chỉ tiêu doanh thu " + year.getText()));
    }
}
