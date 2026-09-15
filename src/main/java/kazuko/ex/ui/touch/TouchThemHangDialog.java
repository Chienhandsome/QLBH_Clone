package kazuko.ex.ui.touch;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.model.danhmuc.MatHang;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Hộp thoại "TÌM KIẾM" / THÊM HÀNG tích hợp bàn phím ảo cảm ứng (Hình 4).
 */
public class TouchThemHangDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color CARD_BG = new Color(0, 79, 76);
    private static final Color CARD_SELECTED = new Color(211, 84, 0);
    private static final Color KEY_BG = new Color(30, 95, 125);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color PURPLE = new Color(106, 13, 84);

    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final BiConsumer<MatHang, Double> onItemSelected;

    private final List<MatHang> allProducts = new ArrayList<>();
    private final List<MatHang> filteredProducts = new ArrayList<>();
    private MatHang selectedProduct = null;

    private final JPanel productGridPanel = new JPanel();
    private final StringBuilder searchBuffer = new StringBuilder();
    private final JLabel lblSearchQuery = new JLabel();

    public TouchThemHangDialog(Window owner, BiConsumer<MatHang, Double> onItemSelected) {
        super(owner, "Tìm kiếm mặt hàng", ModalityType.APPLICATION_MODAL);
        this.onItemSelected = onItemSelected;

        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        initComponents();

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        loadProducts();

        setSize(1080, 640);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 4));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER, 3));

        // 1. Tiêu đề hộp thoại
        JLabel lblTitle = new JLabel("TÌM KIẾM", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(HEADER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setPreferredSize(new Dimension(0, 42));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Thân chính: Nửa trên là Lưới sản phẩm, Nửa dưới là Bàn phím ảo
        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Nửa trên: Lưới sản phẩm (2 cột x 4 dòng hiển thị)
        JPanel upperWrapper = new JPanel(new BorderLayout());
        upperWrapper.setOpaque(false);
        upperWrapper.setPreferredSize(new Dimension(0, 260));

        productGridPanel.setLayout(new GridLayout(0, 2, 8, 8));
        productGridPanel.setOpaque(false);

        JScrollPane spProducts = new JScrollPane(productGridPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spProducts.setOpaque(false);
        spProducts.getViewport().setOpaque(false);
        spProducts.setBorder(null);
        spProducts.getVerticalScrollBar().setUnitIncrement(35);
        upperWrapper.add(spProducts, BorderLayout.CENTER);

        // Nút bánh răng ở góc dưới phải của lưới sản phẩm (Hình 4)
        JPanel gearRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        gearRow.setOpaque(false);
        lblSearchQuery.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblSearchQuery.setForeground(new Color(254, 255, 204));
        gearRow.add(lblSearchQuery);

        JButton btnGear = new JButton("⚙");
        btnGear.setPreferredSize(new Dimension(36, 32));
        btnGear.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        btnGear.setForeground(Color.WHITE);
        btnGear.setBackground(HEADER);
        btnGear.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnGear.setFocusPainted(false);
        btnGear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gearRow.add(btnGear);
        upperWrapper.add(gearRow, BorderLayout.SOUTH);

        centerPanel.add(upperWrapper, BorderLayout.CENTER);

        // Nửa dưới: Bàn phím ảo cảm ứng QWERTY (Hình 4)
        JPanel keyboardPanel = createTouchKeyboard();
        centerPanel.add(keyboardPanel, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);

        // 3. Thanh nút đáy (Hình 4: THÊM MỚI, CHỈNH SỬA bên trái; XÓA TRỐNG, THOÁT bên phải)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setPreferredSize(new Dimension(0, 52));
        bottomBar.setBorder(new EmptyBorder(4, 8, 8, 8));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton btnThemMoi = createActionBtn("THÊM MỚI", GREEN);
        btnThemMoi.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Chức năng thêm mới mặt hàng", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        leftActions.add(btnThemMoi);

        JButton btnChinhSua = createActionBtn("CHỈNH SỬA", GREEN);
        btnChinhSua.addActionListener(e -> {
            if (selectedProduct != null) {
                JOptionPane.showMessageDialog(this, "Chỉnh sửa: " + selectedProduct.getName(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });
        leftActions.add(btnChinhSua);

        bottomBar.add(leftActions, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnXoaTrong = createActionBtn("XÓA TRỐNG", PURPLE);
        btnXoaTrong.addActionListener(e -> {
            searchBuffer.setLength(0);
            updateFilter();
        });
        rightActions.add(btnXoaTrong);

        JButton btnThoat = createActionBtn("THOÁT", RED);
        btnThoat.addActionListener(e -> dispose());
        rightActions.add(btnThoat);

        bottomBar.add(rightActions, BorderLayout.EAST);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createTouchKeyboard() {
        JPanel pnl = new JPanel(new GridLayout(5, 1, 0, 4));
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(0, 240));

        // Hàng 1: 1 2 3 4 5 6 7 8 9 0 - = [◄]
        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r1.setOpaque(false);
        String[] k1 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "="};
        for (String k : k1) r1.add(createKey(k, 66, 42));
        JButton btnBk = createKey("◄", 85, 42);
        btnBk.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        btnBk.setBackground(new Color(60, 60, 60));
        btnBk.addActionListener(e -> {
            if (searchBuffer.length() > 0) {
                searchBuffer.deleteCharAt(searchBuffer.length() - 1);
                updateFilter();
            }
        });
        r1.add(btnBk);
        pnl.add(r1);

        // Hàng 2: Q W E R T Y U I O P \ @
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r2.setOpaque(false);
        String[] k2 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "\\", "@"};
        for (String k : k2) r2.add(createKey(k, 75, 42));
        pnl.add(r2);

        // Hàng 3: A S D F G H J K L ; '
        JPanel r3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r3.setOpaque(false);
        String[] k3 = {"A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'"};
        for (String k : k3) r3.add(createKey(k, 78, 42));
        pnl.add(r3);

        // Hàng 4: Z X C V B N M , . [▲]
        JPanel r4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r4.setOpaque(false);
        String[] k4 = {"Z", "X", "C", "V", "B", "N", "M", ",", "."};
        for (String k : k4) r4.add(createKey(k, 78, 42));
        JButton btnEnter = createKey("▲", 78, 42);
        btnEnter.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        btnEnter.setBackground(new Color(50, 50, 50));
        btnEnter.setForeground(Color.YELLOW);
        btnEnter.addActionListener(e -> {
            if (selectedProduct != null) {
                promptQuantityAndSelect(selectedProduct);
            } else if (!filteredProducts.isEmpty()) {
                promptQuantityAndSelect(filteredProducts.get(0));
            }
        });
        r4.add(btnEnter);
        pnl.add(r4);

        // Hàng 5: Spacebar
        JPanel r5 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r5.setOpaque(false);
        JButton btnSpace = new JButton();
        btnSpace.setPreferredSize(new Dimension(380, 40));
        btnSpace.setBackground(new Color(55, 65, 75));
        btnSpace.setBorder(new LineBorder(new Color(100, 130, 150), 1, true));
        btnSpace.setFocusPainted(false);
        btnSpace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSpace.addActionListener(e -> {
            searchBuffer.append(" ");
            updateFilter();
        });
        r5.add(btnSpace);
        pnl.add(r5);

        return pnl;
    }

    private JButton createKey(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(KEY_BG);
        btn.setBorder(new LineBorder(new Color(110, 155, 180), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            searchBuffer.append(text);
            updateFilter();
        });
        return btn;
    }

    private JButton createActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(130, 42));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(new LineBorder(Color.WHITE, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadProducts() {
        allProducts.clear();
        allProducts.addAll(matHangDAO.getAll());
        updateFilter();
    }

    private void updateFilter() {
        String query = searchBuffer.toString().trim().toLowerCase();
        if (query.isEmpty()) {
            lblSearchQuery.setText("");
        } else {
            lblSearchQuery.setText("Tìm: \"" + searchBuffer.toString() + "\" (" + filteredProducts.size() + " kết quả)");
        }

        filteredProducts.clear();
        for (MatHang mh : allProducts) {
            if (query.isEmpty()) {
                filteredProducts.add(mh);
            } else {
                String code = mh.getCode() != null ? mh.getCode().toLowerCase() : "";
                String name = mh.getName() != null ? mh.getName().toLowerCase() : "";
                if (code.contains(query) || name.contains(query)) {
                    filteredProducts.add(mh);
                }
            }
        }

        if (selectedProduct == null || !filteredProducts.contains(selectedProduct)) {
            selectedProduct = filteredProducts.isEmpty() ? null : filteredProducts.get(0);
        }

        renderProductGrid();
    }

    private void renderProductGrid() {
        productGridPanel.removeAll();
        for (MatHang mh : filteredProducts) {
            JPanel card = createProductCard(mh);
            productGridPanel.add(card);
        }

        int rows = Math.max(4, (filteredProducts.size() + 1) / 2);
        productGridPanel.setPreferredSize(new Dimension(0, rows * 62));

        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    private JPanel createProductCard(MatHang mh) {
        boolean isSelected = (mh == selectedProduct);
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 2));
        card.setOpaque(true);
        card.setBackground(isSelected ? CARD_SELECTED : CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(isSelected ? Color.WHITE : new Color(0, 120, 120), isSelected ? 2 : 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dòng trên: Mã hàng ở giữa
        JLabel lblCode = new JLabel(mh.getCode() != null ? mh.getCode() : "", SwingConstants.CENTER);
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCode.setForeground(Color.WHITE);
        card.add(lblCode);

        // Dòng dưới: Tên hàng ở giữa
        JLabel lblName = new JLabel(mh.getName() != null ? mh.getName() : "", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(Color.WHITE);
        card.add(lblName);

        attachClickListener(card, () -> {
            selectedProduct = mh;
            renderProductGrid();
            promptQuantityAndSelect(mh);
        });

        return card;
    }

    private void attachClickListener(Component comp, Runnable onClick) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onClick.run();
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                attachClickListener(child, onClick);
            }
        }
    }

    private void promptQuantityAndSelect(MatHang mh) {
        if (mh == null) return;
        String title = "NHẬP SỐ LƯỢNG - " + (mh.getName() != null ? mh.getName() : "");
        TouchNumberInputDialog numDlg = new TouchNumberInputDialog(this, title, 1.0, 0.01, 999999);
        numDlg.setVisible(true);

        if (numDlg.isConfirmed() && numDlg.getResultDouble() > 0) {
            double qty = numDlg.getResultDouble();
            if (onItemSelected != null) {
                onItemSelected.accept(mh, qty);
            }
            dispose();
        }
    }
}