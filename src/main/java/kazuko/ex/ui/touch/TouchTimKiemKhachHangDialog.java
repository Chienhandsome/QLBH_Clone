package kazuko.ex.ui.touch;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;

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
 * Hộp thoại Tìm kiếm Khách hàng cảm ứng theo chuẩn Hình 2.
 * - Lưới 2 cột thẻ khách hàng (nền Teal #004F4C, khi chọn chuyển màu cam #D35400)
 * - Nhãn vàng hiển thị từ khóa tìm kiếm trực tiếp
 * - Bàn phím ảo QWERTY cảm ứng tích hợp
 * - Chân trang: THÊM MỚI, CHỈNH SỬA, XÓA TRỐNG, THOÁT
 */
public class TouchTimKiemKhachHangDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER_BG = new Color(79, 139, 169);
    private static final Color CARD_BG = new Color(0, 79, 76);
    private static final Color CARD_SELECTED_BG = new Color(211, 84, 0); // Cam khi được chọn
    private static final Color CARD_BORDER = new Color(0, 110, 110);
    private static final Color GREEN_BTN = new Color(0, 105, 0);
    private static final Color PURPLE_BTN = new Color(142, 21, 132);
    private static final Color RED_BTN = new Color(155, 0, 0);

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private List<KhachHang> allCustomers = new ArrayList<>();
    private List<KhachHang> filteredCustomers = new ArrayList<>();

    private KhachHang selectedCustomer = null;
    private boolean confirmed = false;
    private boolean cleared = false;

    private final StringBuilder searchBuffer = new StringBuilder();
    private final JLabel lblSearchBadge = new JLabel("", SwingConstants.CENTER);
    private final JPanel cardsPanel = new JPanel();
    private final JScrollPane scrollPane;

    public TouchTimKiemKhachHangDialog(Window owner) {
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

        // 2. Thân giữa: Lưới thẻ khách hàng + Huy hiệu tìm kiếm + Bàn phím QWERTY
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

        // Nút bánh răng ở góc dưới phải vùng thẻ như Hình 2
        JButton btnGear = new JButton(TouchIcons.gear(22, 22, Color.WHITE));
        btnGear.setOpaque(false);
        btnGear.setContentAreaFilled(false);
        btnGear.setBorderPainted(false);
        btnGear.setFocusPainted(false);
        btnGear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGear.addActionListener(e -> {
            new TouchThietLapCotKhachHangDialog(this, this::loadCustomers).setVisible(true);
        });
        JPanel gearBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        gearBox.setOpaque(false);
        gearBox.add(btnGear);
        cardsContainer.add(gearBox, BorderLayout.SOUTH);

        centerPanel.add(cardsContainer, BorderLayout.NORTH);

        // Phần bàn phím + Huy hiệu từ khóa tìm kiếm
        JPanel keyboardContainer = new JPanel(new BorderLayout(0, 2));
        keyboardContainer.setOpaque(false);

        // Huy hiệu tìm kiếm màu vàng chữ đỏ đậm ở giữa như Hình 2
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
            // Cho phép mở nhanh tạo khách hàng hoặc báo
            String newName = TouchTextKeyboardDialog.showDialog(this, "NHẬP TÊN KHÁCH HÀNG MỚI", searchBuffer.toString());
            if (newName != null && !newName.trim().isEmpty()) {
                KhachHang kh = new KhachHang();
                kh.setName(newName.trim());
                if (khachHangDAO.insert(kh)) {
                    loadCustomers();
                    searchBuffer.setLength(0);
                    searchBuffer.append(newName.trim());
                    updateSearchFilter();
                }
            }
        });
        footerLeft.add(btnThemMoi);

        JButton btnChinhSua = createFooterButton("CHỈNH SỬA", GREEN_BTN, 110);
        btnChinhSua.addActionListener(e -> {
            if (selectedCustomer != null) {
                String edited = TouchTextKeyboardDialog.showDialog(this, "CHỈNH SỬA TÊN KHÁCH HÀNG", selectedCustomer.getName());
                if (edited != null && !edited.trim().isEmpty()) {
                    selectedCustomer.setName(edited.trim());
                    khachHangDAO.update(selectedCustomer);
                    loadCustomers();
                }
            } else {
                TouchConfirmDialog.show(this, "VUI LÒNG CHỌN KHÁCH HÀNG CẦN SỬA!");
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
            selectedCustomer = null;
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

        loadCustomers();
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

    private void loadCustomers() {
        try {
            allCustomers = khachHangDAO.getAll();
        } catch (Exception e) {
            allCustomers = new ArrayList<>();
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
            filteredCustomers = new ArrayList<>(allCustomers);
        } else {
            lblSearchBadge.setText(" " + kw.toUpperCase() + " ");
            lblSearchBadge.setVisible(true);
            String normKw = removeAccent(kw.toLowerCase(Locale.ROOT));
            filteredCustomers = new ArrayList<>();
            for (KhachHang kh : allCustomers) {
                String name = removeAccent((kh.getName() == null ? "" : kh.getName()).toLowerCase(Locale.ROOT));
                String code = removeAccent((kh.getMaKhach() == null ? "" : kh.getMaKhach()).toLowerCase(Locale.ROOT));
                String phone = removeAccent((kh.getDienThoai() == null ? "" : kh.getDienThoai()).toLowerCase(Locale.ROOT));
                if (name.contains(normKw) || code.contains(normKw) || phone.contains(normKw)) {
                    filteredCustomers.add(kh);
                }
            }
        }
        renderCustomerCards();
    }

    private void renderCustomerCards() {
        cardsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;

        int total = filteredCustomers.size();
        for (int i = 0; i < total; i++) {
            KhachHang kh = filteredCustomers.get(i);
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;

            JPanel card = createCustomerCard(kh);
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

    private JPanel createCustomerCard(KhachHang kh) {
        JPanel card = new JPanel(new BorderLayout());
        boolean isSelected = (selectedCustomer != null && kh.getId() != null && kh.getId().equals(selectedCustomer.getId()));
        card.setBackground(isSelected ? CARD_SELECTED_BG : CARD_BG);
        card.setBorder(new LineBorder(isSelected ? Color.WHITE : CARD_BORDER, 1, true));
        card.setPreferredSize(new Dimension(410, 48));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblName = new JLabel(kh.getName() != null ? kh.getName() : "", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(Color.WHITE);
        card.add(lblName, BorderLayout.CENTER);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedCustomer = kh;
                renderCustomerCards();
                if (e.getClickCount() >= 2) {
                    confirmed = true;
                    dispose();
                }
            }
        };
        card.addMouseListener(ma);
        lblName.addMouseListener(ma);

        return card;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isCleared() {
        return cleared;
    }

    public KhachHang getSelectedCustomer() {
        return selectedCustomer;
    }

    private static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    public static class CustomerSearchResult {
        private final boolean confirmed;
        private final boolean cleared;
        private final KhachHang customer;

        public CustomerSearchResult(boolean confirmed, boolean cleared, KhachHang customer) {
            this.confirmed = confirmed;
            this.cleared = cleared;
            this.customer = customer;
        }

        public boolean isConfirmed() { return confirmed; }
        public boolean isCleared() { return cleared; }
        public KhachHang getCustomer() { return customer; }
    }

    public static CustomerSearchResult showDialog(Window owner) {
        TouchTimKiemKhachHangDialog dlg = new TouchTimKiemKhachHangDialog(owner);
        dlg.setVisible(true);
        return new CustomerSearchResult(dlg.isConfirmed(), dlg.isCleared(), dlg.getSelectedCustomer());
    }
}
