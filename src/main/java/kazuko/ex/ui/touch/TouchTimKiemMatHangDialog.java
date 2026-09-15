package kazuko.ex.ui.touch;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.model.danhmuc.MatHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Hộp thoại Tìm kiếm Mặt Hàng cảm ứng theo chuẩn 100% Hình 5.
 * - Lưới 2 cột thẻ mặt hàng: Dòng 1 Mã hàng, Dòng 2 Tên hàng
 * - Nhãn vàng hiển thị từ khóa tìm kiếm trực tiếp
 * - Bàn phím ảo QWERTY cảm ứng tích hợp
 * - Chân trang: THÊM MỚI, CHỈNH SỬA, XÓA TRỐNG, THOÁT
 */
public class TouchTimKiemMatHangDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER_BG = new Color(79, 139, 169);
    private static final Color CARD_BG = new Color(0, 79, 76);
    private static final Color CARD_SELECTED_BG = new Color(211, 84, 0); // Cam khi được chọn
    private static final Color CARD_BORDER = new Color(0, 110, 110);
    private static final Color GREEN_BTN = new Color(0, 105, 0);
    private static final Color PURPLE_BTN = new Color(142, 21, 132);
    private static final Color RED_BTN = new Color(155, 0, 0);

    private final MatHangDAO matHangDAO = new MatHangDAO();
    private List<MatHang> allProducts = new ArrayList<>();
    private List<MatHang> filteredProducts = new ArrayList<>();

    private MatHang selectedProduct = null;
    private boolean confirmed = false;
    private boolean cleared = false;

    private final StringBuilder searchBuffer = new StringBuilder();
    private final JLabel lblSearchBadge = new JLabel("", SwingConstants.CENTER);
    private final JPanel cardsPanel = new JPanel();
    private final JScrollPane scrollPane;

    public TouchTimKiemMatHangDialog(Window owner) {
        super(owner, "TÌM KIẾM", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER_BG, 2));
        root.setPreferredSize(new Dimension(880, 560));

        // 1. Header
        JLabel lblHeader = new JLabel("TÌM KIẾM", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(HEADER_BG);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setPreferredSize(new Dimension(880, 36));
        root.add(lblHeader, BorderLayout.NORTH);

        // 2. Thân giữa: Lưới thẻ mặt hàng + Huy hiệu tìm kiếm + Bàn phím QWERTY
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setOpaque(false);

        // Lưới thẻ 2 cột
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new GridBagLayout());

        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setOpaque(false);
        cardsWrapper.add(cardsPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(cardsWrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new EmptyBorder(6, 10, 4, 6));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel cardsContainer = new JPanel(new BorderLayout());
        cardsContainer.setOpaque(false);
        cardsContainer.setPreferredSize(new Dimension(880, 240));
        cardsContainer.add(scrollPane, BorderLayout.CENTER);

        // Nút bánh răng ở góc dưới phải vùng thẻ như Hình 5
        JButton btnGear = new JButton(TouchIcons.gear(22, 22, Color.WHITE));
        btnGear.setOpaque(false);
        btnGear.setContentAreaFilled(false);
        btnGear.setBorderPainted(false);
        btnGear.setFocusPainted(false);
        btnGear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGear.addActionListener(e -> {
            new TouchThietLapCotDialog(this, this::loadProducts).setVisible(true);
        });
        JPanel gearBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        gearBox.setOpaque(false);
        gearBox.add(btnGear);
        cardsContainer.add(gearBox, BorderLayout.SOUTH);

        centerPanel.add(cardsContainer, BorderLayout.NORTH);

        // Phần bàn phím + Huy hiệu từ khóa tìm kiếm
        JPanel keyboardContainer = new JPanel(new BorderLayout(0, 2));
        keyboardContainer.setOpaque(false);

        // Huy hiệu tìm kiếm màu vàng chữ đỏ đậm ở giữa như Hình 5
        lblSearchBadge.setOpaque(true);
        lblSearchBadge.setBackground(new Color(255, 235, 0));
        lblSearchBadge.setForeground(Color.RED);
        lblSearchBadge.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSearchBadge.setBorder(new LineBorder(Color.ORANGE, 1, true));
        lblSearchBadge.setVisible(false);

        JPanel searchBadgeWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        searchBadgeWrapper.setOpaque(false);
        searchBadgeWrapper.add(lblSearchBadge);
        keyboardContainer.add(searchBadgeWrapper, BorderLayout.NORTH);

        // Bàn phím QWERTY nhúng
        TouchEmbeddedKeyboardPanel keyboard = new TouchEmbeddedKeyboardPanel(
                this::onKeyInput,
                this::onBackspace,
                () -> onKeyInput(" ")
        );
        keyboardContainer.add(keyboard, BorderLayout.CENTER);

        centerPanel.add(keyboardContainer, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        // 3. Thanh tác vụ chân trang (Footer Bar)
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(880, 48));
        footer.setBorder(new EmptyBorder(4, 10, 6, 10));

        // Trái: THÊM MỚI, CHỈNH SỬA
        JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        footerLeft.setOpaque(false);

        JButton btnThemMoi = createFooterButton("THÊM MỚI", GREEN_BTN, 110);
        btnThemMoi.addActionListener(e -> {
            String newName = TouchTextKeyboardDialog.showDialog(this, "NHẬP TÊN MẶT HÀNG MỚI", searchBuffer.toString());
            if (newName != null && !newName.trim().isEmpty()) {
                MatHang mh = new MatHang();
                mh.setName(newName.trim());
                mh.setCode("MH" + System.currentTimeMillis() % 10000);
                if (matHangDAO.insert(mh)) {
                    loadProducts();
                    searchBuffer.setLength(0);
                    searchBuffer.append(newName.trim());
                    updateSearchFilter();
                }
            }
        });
        footerLeft.add(btnThemMoi);

        JButton btnChinhSua = createFooterButton("CHỈNH SỬA", GREEN_BTN, 110);
        btnChinhSua.addActionListener(e -> {
            if (selectedProduct != null) {
                String edited = TouchTextKeyboardDialog.showDialog(this, "CHỈNH SỬA TÊN MẶT HÀNG", selectedProduct.getName());
                if (edited != null && !edited.trim().isEmpty()) {
                    selectedProduct.setName(edited.trim());
                    matHangDAO.update(selectedProduct);
                    loadProducts();
                }
            } else {
                TouchConfirmDialog.show(this, "VUI LÒNG CHỌN MẶT HÀNG CẦN SỬA!");
            }
        });
        footerLeft.add(btnChinhSua);
        footer.add(footerLeft, BorderLayout.WEST);

        // Phải: XÓA TRỐNG, THOÁT
        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerRight.setOpaque(false);

        JButton btnXoaTrong = createFooterButton("XÓA TRỐNG", PURPLE_BTN, 120);
        btnXoaTrong.addActionListener(e -> {
            cleared = true;
            confirmed = true;
            selectedProduct = null;
            dispose();
        });
        footerRight.add(btnXoaTrong);

        JButton btnThoat = createFooterButton("THOÁT", RED_BTN, 100);
        btnThoat.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        footerRight.add(btnThoat);

        footer.add(footerRight, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);

        loadProducts();
    }

    private JButton createFooterButton(String text, Color bg, int width) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(width, 38));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(new Color(200, 220, 220), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadProducts() {
        try {
            allProducts = matHangDAO.getAll();
        } catch (Exception e) {
            allProducts = new ArrayList<>();
        }
        updateSearchFilter();
    }

    private void onKeyInput(String str) {
        searchBuffer.append(str);
        updateSearchFilter();
    }

    private void onBackspace() {
        if (searchBuffer.length() > 0) {
            searchBuffer.deleteCharAt(searchBuffer.length() - 1);
            updateSearchFilter();
        }
    }

    private void updateSearchFilter() {
        String kw = searchBuffer.toString().trim();
        if (kw.isEmpty()) {
            lblSearchBadge.setVisible(false);
            filteredProducts = new ArrayList<>(allProducts);
        } else {
            lblSearchBadge.setText(" " + kw.toUpperCase() + " ");
            lblSearchBadge.setVisible(true);
            String normKw = removeAccent(kw.toLowerCase(Locale.ROOT));
            filteredProducts = new ArrayList<>();
            for (MatHang mh : allProducts) {
                String name = removeAccent((mh.getName() == null ? "" : mh.getName()).toLowerCase(Locale.ROOT));
                String code = removeAccent((mh.getCode() == null ? "" : mh.getCode()).toLowerCase(Locale.ROOT));
                String barcode = removeAccent((mh.getMaSanCo() == null ? "" : mh.getMaSanCo()).toLowerCase(Locale.ROOT));
                if (name.contains(normKw) || code.contains(normKw) || barcode.contains(normKw)) {
                    filteredProducts.add(mh);
                }
            }
        }
        renderProductCards();
    }

    private void renderProductCards() {
        cardsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;

        int total = filteredProducts.size();
        for (int i = 0; i < total; i++) {
            MatHang mh = filteredProducts.get(i);
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;

            JPanel card = createProductCard(mh);
            cardsPanel.add(card, gbc);
        }

        // Fill remaining cell if odd count
        if (total % 2 != 0) {
            gbc.gridx = 1;
            gbc.gridy = total / 2;
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            cardsPanel.add(empty, gbc);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createProductCard(MatHang mh) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 2));
        boolean isSelected = (selectedProduct != null && mh.getId() != null && mh.getId().equals(selectedProduct.getId()));
        card.setBackground(isSelected ? CARD_SELECTED_BG : CARD_BG);
        card.setBorder(new LineBorder(isSelected ? Color.WHITE : CARD_BORDER, 1, true));
        card.setPreferredSize(new Dimension(410, 52));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dòng 1: Mã hàng (như Hình 5: 02B, 02E, 05IC-A, 1, 1000001, ...)
        String code = mh.getCode() != null && !mh.getCode().isBlank() ? mh.getCode() : "";
        JLabel lblCode = new JLabel(code, SwingConstants.CENTER);
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCode.setForeground(Color.WHITE);
        card.add(lblCode);

        // Dòng 2: Tên hàng (Bếp ga 02B, Bếp từ Sato 05IC, ...)
        String name = mh.getName() != null ? mh.getName() : "";
        JLabel lblName = new JLabel(name, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setForeground(Color.WHITE);
        card.add(lblName);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedProduct = mh;
                renderProductCards();
                if (e.getClickCount() >= 2) {
                    confirmed = true;
                    dispose();
                }
            }
        };
        card.addMouseListener(ma);
        lblCode.addMouseListener(ma);
        lblName.addMouseListener(ma);

        return card;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isCleared() {
        return cleared;
    }

    public MatHang getSelectedProduct() {
        return selectedProduct;
    }

    private static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    public static class ProductSearchResult {
        private final boolean confirmed;
        private final boolean cleared;
        private final MatHang product;

        public ProductSearchResult(boolean confirmed, boolean cleared, MatHang product) {
            this.confirmed = confirmed;
            this.cleared = cleared;
            this.product = product;
        }

        public boolean isConfirmed() { return confirmed; }
        public boolean isCleared() { return cleared; }
        public MatHang getProduct() { return product; }
    }

    public static ProductSearchResult showDialog(Window owner) {
        TouchTimKiemMatHangDialog dlg = new TouchTimKiemMatHangDialog(owner);
        dlg.setVisible(true);
        return new ProductSearchResult(dlg.isConfirmed(), dlg.isCleared(), dlg.getSelectedProduct());
    }
}
