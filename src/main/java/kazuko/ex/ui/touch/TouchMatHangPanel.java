package kazuko.ex.ui.touch;

import kazuko.ex.dao.DonViTinhDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.DonViTinh;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Touch-first product and product-group catalogue backed by the existing DAOs. */
final class TouchMatHangPanel extends JPanel {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color DARK_GREEN = new Color(0, 75, 38);
    private static final Color TEAL = new Color(0, 79, 76);
    private static final Color BLUE = new Color(5, 5, 145);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final MatHangDAO productDao = new MatHangDAO();
    private final NhomMatHangDAO groupDao = new NhomMatHangDAO();
    private final DonViTinhDAO unitDao = new DonViTinhDAO();
    private final Runnable exit;
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);

    private JPanel catalogueScreen;
    private JPanel groupListScreen;
    private String selectedGroupId;
    private String selectedGroupName = "Tất cả";
    private MatHang selectedProduct;
    private NhomMatHang editingGroup;
    private MatHang editingProduct;
    private int groupIndex;
    private int productIndex;

    private final JTextField groupName = field();
    private final JTextField groupCode = field();
    private final JTextField groupNote = field();
    private final JTextField productCode = field();
    private final JTextField existingCode = field();
    private final JTextField productName = field();
    private final JComboBox<NhomMatHang> productGroup = new JComboBox<>();
    private final JComboBox<DonViTinh> productUnit = new JComboBox<>();
    private final JTextField price1 = field();
    private final JTextField price2 = field();
    private final JTextField price3 = field();
    private final JTextField price4 = field();
    private final JTextField purchasePrice = field();
    private final JTextField manufacturer = field();
    private final JTextField productNote = field();
    private final JCheckBox noKitchenPrint = new JCheckBox("KHÔNG IN CHẾ BIẾN");

    TouchMatHangPanel(Runnable exit) {
        this.exit = exit;
        setLayout(new BorderLayout());
        screens.add(createGroupForm(), "group-form");
        screens.add(createProductForm(), "product-form");
        add(screens, BorderLayout.CENTER);
        showCatalogue();
    }

    private void showCatalogue() {
        if (catalogueScreen != null) screens.remove(catalogueScreen);
        catalogueScreen = createCatalogue();
        screens.add(catalogueScreen, "catalogue");
        cards.show(screens, "catalogue");
        refresh();
    }

    private JPanel createCatalogue() {
        JPanel root = base();
        JPanel groups = new JPanel(new BorderLayout(0, 7));
        groups.setOpaque(false);
        groups.setPreferredSize(new Dimension(375, 0));
        groups.setBorder(new EmptyBorder(7, 8, 6, 8));
        groups.add(button("DANH MỤC MẶT HÀNG", RED), BorderLayout.NORTH);

        JPanel groupRows = new JPanel();
        groupRows.setLayout(new BoxLayout(groupRows, BoxLayout.Y_AXIS));
        groupRows.setBackground(new Color(235, 235, 235));
        addGroupFilter(groupRows, "TẤT CẢ", null);
        addGroupFilter(groupRows, "CHƯA THIẾT LẬP", "__none__");
        for (NhomMatHang group : groupDao.getAll()) addGroupFilter(groupRows, group.getName(), group.getId());
        groupRows.add(Box.createVerticalGlue());
        JScrollPane groupScroll = scroll(groupRows);
        groups.add(groupScroll, BorderLayout.CENTER);
        JButton adjust = button("ĐIỀU CHỈNH NHÓM", DARK_GREEN);
        adjust.addActionListener(e -> showGroupList());
        groups.add(adjust, BorderLayout.SOUTH);
        root.add(groups, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(7, 7));
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(7, 0, 6, 7));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        toolbar.setBackground(HEADER);
        toolbar.setBorder(new EmptyBorder(5, 5, 5, 5));
        JButton add = button("THÊM MỚI", DARK_GREEN);
        JButton edit = button("CHỈNH SỬA", DARK_GREEN);
        JButton delete = button("XÓA", RED);
        JButton refresh = button("REFRESH", DARK_GREEN);
        JButton search = button("TÌM", DARK_GREEN);
        for (JButton b : List.of(add, edit, delete, refresh, search)) {
            b.setPreferredSize(new Dimension(172, 50));
            toolbar.add(b);
        }
        add.addActionListener(e -> beginProduct(null));
        edit.addActionListener(e -> { if (requireProduct()) beginProduct(selectedProduct); });
        delete.addActionListener(e -> { if (requireProduct()) deleteProduct(selectedProduct); });
        refresh.addActionListener(e -> showCatalogue());
        search.addActionListener(e -> searchProducts());
        right.add(toolbar, BorderLayout.NORTH);

        JPanel productRows = new JPanel(new GridLayout(0, 2, 7, 7));
        productRows.setBackground(new Color(235, 235, 235));
        productRows.setBorder(new EmptyBorder(7, 7, 7, 7));
        productRows.setName("productRows");
        right.add(scroll(productRows), BorderLayout.CENTER);
        JButton close = button("THOÁT", RED);
        close.setPreferredSize(new Dimension(150, 58));
        close.addActionListener(e -> exit.run());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(close);
        right.add(footer, BorderLayout.SOUTH);
        root.add(right, BorderLayout.CENTER);
        return root;
    }

    private void addGroupFilter(JPanel parent, String name, String id) {
        JButton item = button(name == null ? "" : name, BLUE);
        boolean selected = (id == null && selectedGroupId == null)
                || (id != null && id.equals(selectedGroupId));
        item.setBackground(selected ? ORANGE : (id == null || "__none__".equals(id) ? BLUE : GREEN));
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        item.setPreferredSize(new Dimension(0, 84));
        item.addActionListener(e -> {
            selectedGroupId = id;
            selectedGroupName = name;
            selectedProduct = null;
            showCatalogue();
        });
        parent.add(item);
        parent.add(Box.createVerticalStrut(7));
    }

    private void refresh() {
        if (catalogueScreen == null) return;
        JPanel rows = findNamedPanel(catalogueScreen, "productRows");
        if (rows == null) return;
        rows.removeAll();
        List<MatHang> products;
        if (selectedGroupId == null) products = productDao.getAll();
        else if ("__none__".equals(selectedGroupId)) products = productDao.getByNhom("Chưa thiết lập", null);
        else products = productDao.getByNhom(selectedGroupName, selectedGroupId);
        for (MatHang product : products) rows.add(productCard(product));
        if ((products.size() & 1) == 1) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            rows.add(filler);
        }
        rows.revalidate();
        rows.repaint();
    }

    private JPanel productCard(MatHang product) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(product == selectedProduct ? ORANGE : TEAL);
        card.setBorder(new EmptyBorder(4, 14, 4, 14));
        card.setPreferredSize(new Dimension(500, 106));
        JLabel title = new JLabel("TÊN: " + safe(product.getName()));
        title.setForeground(new Color(255, 80, 220));
        title.setFont(font(29));
        card.add(title, BorderLayout.NORTH);
        JLabel detail = new JLabel("<html><b>Hãng sản xuất:</b> " + safe(product.getHangSanXuat())
                + "<br><b>MÃ: " + safe(product.getCode()) + "</b></html>");
        detail.setForeground(Color.WHITE);
        detail.setFont(font(20));
        card.add(detail, BorderLayout.CENTER);
        JLabel price = new JLabel("GIÁ: " + MONEY.format(product.getGiaBan()), SwingConstants.RIGHT);
        price.setForeground(Color.YELLOW);
        price.setFont(font(23));
        card.add(price, BorderLayout.EAST);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedProduct = product;
                refresh();
            }
        });
        return card;
    }

    private void searchProducts() {
        String term = JOptionPane.showInputDialog(this, "Nhập mã hoặc tên mặt hàng:", "TÌM MẶT HÀNG", JOptionPane.PLAIN_MESSAGE);
        if (term == null || term.isBlank()) return;
        List<MatHang> found = productDao.search(term.trim(), selectedGroupName, "__none__".equals(selectedGroupId) ? null : selectedGroupId);
        JPanel rows = findNamedPanel(catalogueScreen, "productRows");
        rows.removeAll();
        for (MatHang product : found) rows.add(productCard(product));
        rows.revalidate(); rows.repaint();
    }

    private void showGroupList() {
        if (groupListScreen != null) screens.remove(groupListScreen);
        groupListScreen = base();
        groupListScreen.add(header("DANH MỤC NHÓM MẶT HÀNG"), BorderLayout.NORTH);
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(new Color(235, 235, 235));
        rows.setBorder(new EmptyBorder(6, 6, 6, 6));
        for (NhomMatHang group : groupDao.getAll()) rows.add(groupRow(group));
        rows.add(Box.createVerticalGlue());
        groupListScreen.add(scroll(rows), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false); footer.setBorder(new EmptyBorder(8, 5, 0, 5));
        JButton add = button("THÊM", new Color(0, 145, 0));
        add.setPreferredSize(new Dimension(225, 78));
        add.addActionListener(e -> beginGroup(null));
        JButton close = button("THOÁT", RED);
        close.setPreferredSize(new Dimension(225, 78));
        close.addActionListener(e -> showCatalogue());
        footer.add(add, BorderLayout.WEST); footer.add(close, BorderLayout.EAST);
        groupListScreen.add(footer, BorderLayout.SOUTH);
        screens.add(groupListScreen, "groups");
        cards.show(screens, "groups");
    }

    private JPanel groupRow(NhomMatHang group) {
        JPanel row = new JPanel(new BorderLayout(7, 0));
        row.setOpaque(false); row.setBorder(new EmptyBorder(0, 0, 7, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 106));
        JLabel name = new JLabel(safe(group.getName()));
        name.setOpaque(true); name.setBackground(GREEN); name.setForeground(Color.WHITE);
        name.setFont(font(23)); name.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.add(name, BorderLayout.CENTER);
        // Per-item THÊM is deliberately omitted; the catalogue has one common THÊM button.
        JPanel actions = new JPanel(new GridLayout(1, 2, 7, 0)); actions.setOpaque(false);
        JButton delete = button("XÓA", RED); delete.setPreferredSize(new Dimension(110, 98));
        JButton edit = button("SỬA", BLUE); edit.setPreferredSize(new Dimension(110, 98));
        delete.addActionListener(e -> deleteGroup(group));
        edit.addActionListener(e -> beginGroup(group));
        actions.add(delete); actions.add(edit); row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JPanel createGroupForm() {
        JPanel root = base();
        root.add(header("NHÓM MẶT HÀNG"), BorderLayout.NORTH);
        JPanel topActions = formActions(
                e -> moveGroup(-1), e -> moveGroup(1), e -> beginGroup(null), e -> copyGroup(), e -> deleteGroup(editingGroup));
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false);
        body.add(topActions, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints c = constraints();
        addFieldRow(fields, c, 0, "TÊN NHÓM MẶT HÀNG", groupName);
        addFieldRow(fields, c, 1, "ĐÁNH MÃ SẢN PHẨM", groupCode);
        addFieldRow(fields, c, 2, "GHI CHÚ", groupNote);
        c.gridy = 3; c.weighty = 1; fields.add(Box.createVerticalGlue(), c);
        body.add(fields, BorderLayout.CENTER); root.add(body, BorderLayout.CENTER);
        root.add(saveFooter(() -> saveGroup(false), () -> saveGroup(true), () -> {
            if (!saveGroup(false)) return false;
            showGroupList();
            return true;
        }, this::showGroupList), BorderLayout.SOUTH);
        return root;
    }

    private void beginGroup(NhomMatHang group) {
        editingGroup = group;
        List<NhomMatHang> groups = groupDao.getAll();
        groupIndex = group == null ? -1 : Math.max(0, groups.indexOf(group));
        groupName.setText(group == null ? "" : safe(group.getName()));
        groupCode.setText(group == null ? "" : safe(group.getCode()));
        groupNote.setText(group == null ? "" : safe(group.getGhiChu()));
        cards.show(screens, "group-form");
    }

    private boolean saveGroup(boolean newAfter) {
        if (groupName.getText().trim().isEmpty()) return warn("Vui lòng nhập tên nhóm mặt hàng.");
        NhomMatHang value = editingGroup == null ? new NhomMatHang() : editingGroup;
        value.setName(groupName.getText().trim()); value.setCode(groupCode.getText().trim()); value.setGhiChu(groupNote.getText().trim());
        boolean ok = editingGroup == null ? groupDao.insert(value) : groupDao.update(value);
        if (!ok) return warn("Không thể lưu nhóm mặt hàng.");
        editingGroup = value;
        if (newAfter) beginGroup(null);
        return true;
    }

    private void moveGroup(int delta) {
        List<NhomMatHang> groups = groupDao.getAll();
        if (groups.isEmpty()) return;
        groupIndex = Math.max(0, Math.min(groups.size() - 1, groupIndex + delta));
        beginGroup(groups.get(groupIndex));
    }

    private void copyGroup() {
        groupName.setText(groupName.getText() + " - BẢN SAO"); editingGroup = null;
    }

    private void deleteGroup(NhomMatHang group) {
        if (group == null) return;
        if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this), "BẠN CÓ MUỐN XÓA NHÓM<br>" + safe(group.getName()).toUpperCase() + " KHÔNG?")) return;
        groupDao.moveToTrash(group.getId());
        showGroupList();
    }

    private JPanel createProductForm() {
        JPanel root = base();
        root.add(header("MẶT HÀNG"), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false);
        body.add(formActions(e -> moveProduct(-1), e -> moveProduct(1), e -> beginProduct(null), e -> copyProduct(), e -> deleteProduct(editingProduct)), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints c = constraints();
        addTwoFields(fields, c, 0, "MÃ HÀNG", productCode, "MÃ SẴN CÓ", existingCode);
        addFieldRow(fields, c, 1, "TÊN MẶT HÀNG", productName);
        addComboRow(fields, c, 2, "NHÓM MẶT HÀNG", productGroup);
        addComboRow(fields, c, 3, "ĐƠN VỊ TÍNH", productUnit);
        addTwoFields(fields, c, 4, "GIÁ BÁN", price1, "ĐƠN GIÁ 2", price2);
        addTwoFields(fields, c, 5, "GIÁ BÁN 3", price3, "GIÁ BÁN 4", price4);
        addFieldRow(fields, c, 6, "GIÁ NHẬP", purchasePrice);
        addFieldRow(fields, c, 7, "HÃNG SẢN XUẤT", manufacturer);
        addFieldRow(fields, c, 8, "GHI CHÚ", productNote);
        noKitchenPrint.setOpaque(false); noKitchenPrint.setForeground(Color.WHITE); noKitchenPrint.setFont(font(18));
        c.gridx = 1; c.gridy = 9; c.gridwidth = 3; c.weightx = 1; fields.add(noKitchenPrint, c);
        c.gridy = 10; c.weighty = 1; fields.add(Box.createVerticalGlue(), c);
        body.add(new JScrollPane(fields), BorderLayout.CENTER); root.add(body, BorderLayout.CENTER);
        root.add(saveFooter(() -> saveProduct(false), () -> saveProduct(true), () -> {
            if (!saveProduct(false)) return false;
            showCatalogue();
            return true;
        }, this::showCatalogue), BorderLayout.SOUTH);
        return root;
    }

    private void beginProduct(MatHang product) {
        editingProduct = product;
        List<MatHang> products = productDao.getAll();
        productIndex = product == null ? -1 : Math.max(0, products.indexOf(product));
        productGroup.removeAllItems(); for (NhomMatHang g : groupDao.getAll()) productGroup.addItem(g);
        productUnit.removeAllItems(); for (DonViTinh u : unitDao.getAll()) productUnit.addItem(u);
        productCode.setText(product == null ? productDao.generateNextProductCode(groupPrefix()) : safe(product.getCode()));
        existingCode.setText(product == null ? "" : safe(product.getMaSanCo()));
        productName.setText(product == null ? "" : safe(product.getName()));
        price1.setText(number(product == null ? 0 : product.getGiaBan()));
        price2.setText(number(product == null ? 0 : product.getGiaBan2()));
        price3.setText(number(product == null ? 0 : product.getGiaBan3()));
        price4.setText(number(product == null ? 0 : product.getGiaBan4()));
        purchasePrice.setText(number(product == null ? 0 : product.getGiaNhap()));
        manufacturer.setText(product == null ? "" : safe(product.getHangSanXuat()));
        productNote.setText(product == null ? "" : safe(product.getGhiChu()));
        noKitchenPrint.setSelected(product != null && product.isKhongInCheBien());
        selectById(productGroup, product == null ? selectedGroupId : product.getNhomMatHangId());
        selectById(productUnit, product == null ? null : product.getDonViTinhId());
        cards.show(screens, "product-form");
    }

    private boolean saveProduct(boolean newAfter) {
        String code = productCode.getText().trim();
        if (code.isEmpty() || productName.getText().trim().isEmpty()) return warn("Vui lòng nhập mã và tên mặt hàng.");
        MatHang duplicate = productDao.findByCode(code);
        if (duplicate != null && (editingProduct == null || !duplicate.getId().equals(editingProduct.getId()))) return warn("Mã mặt hàng đã tồn tại.");
        MatHang value = editingProduct == null ? new MatHang() : editingProduct;
        try {
            value.setCode(code); value.setMaSanCo(existingCode.getText().trim()); value.setName(productName.getText().trim());
            value.setGiaBan(parse(price1)); value.setGiaBan2(parse(price2)); value.setGiaBan3(parse(price3)); value.setGiaBan4(parse(price4)); value.setGiaNhap(parse(purchasePrice));
        } catch (NumberFormatException ex) { return warn("Giá tiền không hợp lệ."); }
        NhomMatHang group = (NhomMatHang) productGroup.getSelectedItem();
        DonViTinh unit = (DonViTinh) productUnit.getSelectedItem();
        value.setNhomMatHangId(group == null ? null : group.getId()); value.setTenNhom(group == null ? null : group.getName());
        value.setDonViTinhId(unit == null ? null : unit.getId()); value.setTenDonViTinh(unit == null ? null : unit.getName());
        value.setHangSanXuat(manufacturer.getText().trim()); value.setGhiChu(productNote.getText().trim()); value.setKhongInCheBien(noKitchenPrint.isSelected());
        boolean ok = editingProduct == null ? productDao.insert(value) : productDao.update(value);
        if (!ok) return warn("Không thể lưu mặt hàng vào cơ sở dữ liệu.");
        editingProduct = value; selectedProduct = value;
        if (newAfter) beginProduct(null);
        return true;
    }

    private void moveProduct(int delta) {
        List<MatHang> products = productDao.getAll(); if (products.isEmpty()) return;
        productIndex = Math.max(0, Math.min(products.size() - 1, productIndex + delta)); beginProduct(products.get(productIndex));
    }

    private void copyProduct() {
        editingProduct = null; productCode.setText(productDao.generateNextProductCode(groupPrefix()));
    }

    private void deleteProduct(MatHang product) {
        if (product == null) return;
        DeleteChoice choice = showDeleteChoice();
        if (choice == DeleteChoice.TRASH) productDao.moveToTrash(product.getId());
        else if (choice == DeleteChoice.PERMANENT) productDao.deletePermanently(product.getId());
        else return;
        selectedProduct = null; editingProduct = null; showCatalogue();
    }

    private DeleteChoice showDeleteChoice() {
        final DeleteChoice[] result = {DeleteChoice.CANCEL};
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "XÁC NHẬN THAO TÁC", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel panel = base(); panel.setBorder(new LineBorder(HEADER, 5));
        panel.add(header("XÁC NHẬN THAO TÁC"), BorderLayout.NORTH);
        JLabel text = new JLabel("<html>Xóa vĩnh viễn: Xóa hẳn khỏi hệ thống<br><br>Xóa vào thùng rác: Dữ liệu sẽ được ẩn trong các giao diện nhập liệu<br><br>Hủy bỏ: Dừng hành động</html>");
        text.setForeground(Color.WHITE); text.setFont(font(18)); text.setBorder(new EmptyBorder(18, 18, 18, 18)); panel.add(text, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0)); actions.setOpaque(false); actions.setBorder(new EmptyBorder(0, 8, 8, 8));
        JButton permanent = button("XÓA VĨNH VIỄN", Color.RED), trash = button("THÙNG RÁC", new Color(0, 145, 0)), cancel = button("HỦY BỎ", ORANGE);
        permanent.addActionListener(e -> { result[0] = DeleteChoice.PERMANENT; dialog.dispose(); });
        trash.addActionListener(e -> { result[0] = DeleteChoice.TRASH; dialog.dispose(); });
        cancel.addActionListener(e -> dialog.dispose());
        actions.add(permanent); actions.add(trash); actions.add(cancel); panel.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(panel); dialog.setSize(750, 365); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
        return result[0];
    }

    private JPanel formActions(java.awt.event.ActionListener previous, java.awt.event.ActionListener next,
                               java.awt.event.ActionListener create, java.awt.event.ActionListener copy,
                               java.awt.event.ActionListener delete) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5)); bar.setBackground(new Color(108, 137, 30));
        String[] names = {"TRƯỚC", "SAU", "TẠO MỚI", "SAO CHÉP", "XÓA"};
        java.awt.event.ActionListener[] listeners = {previous, next, create, copy, delete};
        for (int i = 0; i < names.length; i++) { JButton b = button(names[i], i == 4 ? RED : DARK_GREEN); b.setPreferredSize(new Dimension(135, 58)); b.addActionListener(listeners[i]); bar.add(b); }
        return bar;
    }

    private JPanel saveFooter(SaveAction save, SaveAction saveNew, SaveAction saveExit, Runnable close) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)); footer.setOpaque(false);
        JButton b1 = button("LƯU", DARK_GREEN), b2 = button("LƯU & MỚI", DARK_GREEN), b3 = button("LƯU & THOÁT", DARK_GREEN), b4 = button("THOÁT", RED);
        for (JButton b : List.of(b1, b2, b3, b4)) { b.setPreferredSize(new Dimension(185, 68)); footer.add(b); }
        b1.addActionListener(e -> save.run()); b2.addActionListener(e -> saveNew.run()); b3.addActionListener(e -> saveExit.run()); b4.addActionListener(e -> close.run());
        return footer;
    }

    private void addFieldRow(JPanel panel, GridBagConstraints c, int row, String title, JComponent input) {
        JLabel label = label(title); c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0; panel.add(label, c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1; panel.add(input, c);
    }

    private void addComboRow(JPanel panel, GridBagConstraints c, int row, String title, JComboBox<?> input) {
        input.setFont(font(22)); input.setPreferredSize(new Dimension(0, 52)); addFieldRow(panel, c, row, title, input);
    }

    private void addTwoFields(JPanel panel, GridBagConstraints c, int row, String leftTitle, JComponent left, String rightTitle, JComponent right) {
        c.gridwidth = 1; c.gridy = row; c.weightx = 0; c.gridx = 0; panel.add(label(leftTitle), c);
        c.gridx = 1; c.weightx = .55; panel.add(left, c); c.gridx = 2; c.weightx = 0; panel.add(label(rightTitle), c); c.gridx = 3; c.weightx = .45; panel.add(right, c);
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6, 10, 6, 10); c.fill = GridBagConstraints.HORIZONTAL; return c;
    }

    private static JPanel base() { JPanel p = new JPanel(new BorderLayout(0, 7)); p.setBackground(BG); p.setBorder(new EmptyBorder(7, 5, 7, 5)); return p; }
    private static JLabel header(String text) { JLabel l = new JLabel(text, SwingConstants.CENTER); l.setOpaque(true); l.setBackground(HEADER); l.setForeground(Color.WHITE); l.setFont(font(28)); l.setPreferredSize(new Dimension(0, 68)); return l; }
    private static JLabel label(String text) { JLabel l = new JLabel(text); l.setForeground(Color.WHITE); l.setFont(font(18)); l.setPreferredSize(new Dimension(260, 52)); return l; }
    private static JTextField field() { JTextField f = new JTextField(); f.setFont(new Font("Segoe UI", Font.PLAIN, 24)); f.setPreferredSize(new Dimension(0, 52)); return f; }
    private static JButton button(String text, Color color) { JButton b = new JButton(text); b.setFont(font(19)); b.setForeground(Color.WHITE); b.setBackground(color); b.setFocusPainted(false); b.setBorder(new LineBorder(new Color(190, 210, 210), 2, true)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b; }
    private static JScrollPane scroll(Component view) { JScrollPane s = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); s.setBorder(null); s.getVerticalScrollBar().setUnitIncrement(45); return s; }
    private static Font font(int size) { return new Font("Segoe UI", Font.BOLD, size); }
    private static String safe(String value) { return value == null ? "" : value; }
    private static String number(double value) { return value == 0 ? "0" : MONEY.format(value); }
    private static double parse(JTextField field) { String v = field.getText().trim().replace(".", "").replace(",", ""); return v.isEmpty() ? 0 : Double.parseDouble(v); }
    private boolean warn(String message) { JOptionPane.showMessageDialog(this, message, "THÔNG BÁO", JOptionPane.WARNING_MESSAGE); return false; }
    private boolean requireProduct() { return selectedProduct != null || !warn("Vui lòng chọn một mặt hàng."); }
    private String groupPrefix() { NhomMatHang group = (NhomMatHang) productGroup.getSelectedItem(); return group == null ? "MH" : safe(group.getCode()); }

    private static void selectById(JComboBox<?> combo, String id) {
        if (id == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            String itemId = item instanceof NhomMatHang g ? g.getId() : item instanceof DonViTinh u ? u.getId() : null;
            if (id.equals(itemId)) { combo.setSelectedIndex(i); return; }
        }
    }

    private static JPanel findNamedPanel(Container parent, String name) {
        for (Component component : parent.getComponents()) {
            if (component instanceof JPanel panel && name.equals(panel.getName())) return panel;
            if (component instanceof Container child) { JPanel found = findNamedPanel(child, name); if (found != null) return found; }
        }
        return null;
    }

    private enum DeleteChoice { PERMANENT, TRASH, CANCEL }
    @FunctionalInterface private interface SaveAction { boolean run(); }
}
