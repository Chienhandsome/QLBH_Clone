package kazuko.ex.ui.hoadon;

import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChonCotHienThiDialog extends JDialog {

    private DefaultListModel<String> modelHidden;
    private DefaultListModel<String> modelVisible;
    private JList<String> listHidden;
    private JList<String> listVisible;

    private boolean confirmed = false;
    private List<String> resultVisibleColumns = new ArrayList<>();

    public static final List<String> ALL_POSSIBLE_COLUMNS = Arrays.asList(
            "Tên mặt hàng", "Mã hàng", "Đơn giá", "ĐVT", "CK%", "Tiền giảm",
            "Ghi chú", "Bảo hành", "S.lượng", "Thành tiền", "Kích thước", "Hạn sử dụng"
    );

    public static final List<String> DEFAULT_VISIBLE_COLUMNS = Arrays.asList(
            "Tên mặt hàng", "Mã hàng", "Đơn giá", "ĐVT", "CK%", "Tiền giảm",
            "Ghi chú", "Bảo hành", "S.lượng"
    );

    public static final List<String> DEFAULT_ALL_COLUMNS = DEFAULT_VISIBLE_COLUMNS;

    public static final List<String> ALL_POSSIBLE_CATALOG_COLUMNS = Arrays.asList(
            "Mã hàng", "Tên hàng", "ĐVT", "ĐVT chẵn", "Giá bán", "Bảo hành", "Đơn giá 2", "giá blabla", "Quy đổi", "Giá bán chẵn", "Mã vạch", "Tồn kho", "Giá nhập", "Ghi chú"
    );

    public static final List<String> DEFAULT_VISIBLE_CATALOG_COLUMNS = Arrays.asList(
            "Mã hàng", "Tên hàng", "ĐVT", "ĐVT chẵn", "Giá bán", "Bảo hành", "Đơn giá 2", "giá blabla", "Quy đổi", "Giá bán chẵn"
    );

    private List<String> allPossibleCols = ALL_POSSIBLE_COLUMNS;
    private List<String> defaultVisibleCols = DEFAULT_VISIBLE_COLUMNS;

    // --- Quản lý bán hàng: Master Invoice List ---
    public static final List<String> ALL_POSSIBLE_INVOICE_COLUMNS = Arrays.asList(
            "Số phiếu", "Ngày", "Khách hàng", "Tiền hàng", "Giảm giá",
            "Đổi trả", "Tổng cộng", "Giờ thanh toán", "Thanh toán bởi",
            "Ghi chú", "Mã khách", "Địa chỉ", "Điện thoại", "% giảm giá",
            "Diễn giải", "Khách đưa", "Trả lại", "Giao hàng", "Thẻ tt",
            "Trừ tích lũy", "Tiền mặt", "Chuyển khoản", "Số phiếu đặt hàng"
    );

    public static final List<String> DEFAULT_VISIBLE_INVOICE_COLUMNS = Arrays.asList(
            "Số phiếu", "Ngày", "Khách hàng", "Tiền hàng", "Giảm giá",
            "Đổi trả", "Tổng cộng", "Giờ thanh toán", "Thanh toán bởi",
            "Ghi chú", "Mã khách", "Địa chỉ", "Điện thoại"
    );

    // --- Quản lý bán hàng: Detail Items (Lay Hang / Tra Hang) ---
    public static final List<String> ALL_POSSIBLE_DETAIL_COLUMNS = Arrays.asList(
            "Tên mặt hàng", "Mã hàng", "Đơn giá", "ĐVT", "CK%", "Tiền giảm",
            "Ghi chú", "S.lượng", "Thành tiền", "Hạn sử dụng", "Bảo hành"
    );

    public static final List<String> DEFAULT_VISIBLE_DETAIL_COLUMNS = Arrays.asList(
            "Tên mặt hàng", "Mã hàng", "Đơn giá", "ĐVT", "CK%", "Tiền giảm",
            "Ghi chú", "S.lượng", "Thành tiền", "Hạn sử dụng", "Bảo hành"
    );

    public ChonCotHienThiDialog(Window parent, List<String> currentVisibleColumns) {
        this(parent, currentVisibleColumns, ALL_POSSIBLE_COLUMNS, DEFAULT_VISIBLE_COLUMNS);
    }

    public ChonCotHienThiDialog(Window parent, List<String> currentVisibleColumns, List<String> allPossibleCols, List<String> defaultVisibleCols) {
        super(parent, "Chọn cột hiển thị", ModalityType.APPLICATION_MODAL);
        this.allPossibleCols = (allPossibleCols != null) ? allPossibleCols : ALL_POSSIBLE_COLUMNS;
        this.defaultVisibleCols = (defaultVisibleCols != null) ? defaultVisibleCols : DEFAULT_VISIBLE_COLUMNS;
        initComponents(currentVisibleColumns);
    }

    private void initComponents(List<String> currentVisibleColumns) {
        setSize(560, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 4));
        pnlHeader.setBackground(new Color(240, 245, 252));
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 235)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("table.png", 26, 26));
        JPanel pnlHeaderText = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlHeaderText.setOpaque(false);

        JLabel lblTitle = new JLabel("Mời bạn chọn các cột muốn hiển thị trên lưới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(30, 60, 110));

        JLabel lblSub = new JLabel("Chú ý: Bạn có thể nháy đúp vào danh sách cột để thêm hoặc xóa cột");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSub.setForeground(new Color(60, 60, 60));

        pnlHeaderText.add(lblTitle);
        pnlHeaderText.add(lblSub);

        pnlHeader.add(lblIcon, BorderLayout.WEST);
        pnlHeader.add(pnlHeaderText, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // Center Dual List
        JPanel pnlCenter = new JPanel(null);
        pnlCenter.setBackground(new Color(205, 225, 250)); // Light blue like Screenshot 3

        modelHidden = new DefaultListModel<>();
        modelVisible = new DefaultListModel<>();

        List<String> visibleList = (currentVisibleColumns != null && !currentVisibleColumns.isEmpty()) ? new ArrayList<>(currentVisibleColumns) : new ArrayList<>(defaultVisibleCols);
        for (String col : visibleList) {
            modelVisible.addElement(col);
        }
        for (String col : allPossibleCols) {
            if (!visibleList.contains(col)) {
                modelHidden.addElement(col);
            }
        }

        // Left List (Hidden)
        JLabel lblHidden = new JLabel("Danh sách các cột ẩn");
        lblHidden.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHidden.setBounds(15, 8, 180, 20);
        pnlCenter.add(lblHidden);

        listHidden = new JList<>(modelHidden);
        listHidden.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane spHidden = new JScrollPane(listHidden);
        spHidden.setBounds(15, 30, 175, 240);
        pnlCenter.add(spHidden);

        // Middle Buttons (Thêm >>, << Xóa)
        JButton btnAdd = new JButton("Thêm >>");
        btnAdd.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnAdd.setBackground(Color.WHITE);
        btnAdd.setBounds(200, 100, 85, 26);
        btnAdd.addActionListener(e -> moveItems(listHidden, modelHidden, modelVisible));
        pnlCenter.add(btnAdd);

        JButton btnRemove = new JButton("<< Xóa");
        btnRemove.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRemove.setBackground(Color.WHITE);
        btnRemove.setBounds(200, 135, 85, 26);
        btnRemove.addActionListener(e -> moveItems(listVisible, modelVisible, modelHidden));
        pnlCenter.add(btnRemove);

        // Right List (Visible)
        JLabel lblVisible = new JLabel("Danh sách các cột hiển thị trên lưới");
        lblVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVisible.setBounds(295, 8, 200, 20);
        pnlCenter.add(lblVisible);

        listVisible = new JList<>(modelVisible);
        listVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane spVisible = new JScrollPane(listVisible);
        spVisible.setBounds(295, 30, 160, 240);
        pnlCenter.add(spVisible);

        // Move Up / Move Down buttons on right
        JButton btnUp = new JButton("Chuyển lên");
        btnUp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnUp.setBackground(Color.WHITE);
        btnUp.setBounds(462, 70, 82, 26);
        btnUp.addActionListener(e -> moveUpDown(-1));
        pnlCenter.add(btnUp);

        JButton btnDown = new JButton("Chuyển xuống");
        btnDown.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDown.setBackground(Color.WHITE);
        btnDown.setBounds(462, 105, 82, 26);
        btnDown.addActionListener(e -> moveUpDown(1));
        pnlCenter.add(btnDown);

        // Double click handlers
        listHidden.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    moveItems(listHidden, modelHidden, modelVisible);
                }
            }
        });

        listVisible.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    moveItems(listVisible, modelVisible, modelHidden);
                }
            }
        });

        add(pnlCenter, BorderLayout.CENTER);

        // Footer buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlFooter.setBackground(new Color(240, 245, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 215, 235)));

        JButton btnRestore = new JButton("Khôi phục");
        JButton btnAccept = new JButton("Chấp nhận");
        JButton btnCancel = new JButton("Hủy bỏ");

        btnRestore.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAccept.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnRestore.setBackground(Color.WHITE);
        btnAccept.setBackground(Color.WHITE);
        btnCancel.setBackground(Color.WHITE);

        btnRestore.addActionListener(e -> onRestore());
        btnAccept.addActionListener(e -> onAccept());
        btnCancel.addActionListener(e -> dispose());

        pnlFooter.add(btnRestore);
        pnlFooter.add(btnAccept);
        pnlFooter.add(btnCancel);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void moveItems(JList<String> srcList, DefaultListModel<String> srcModel, DefaultListModel<String> destModel) {
        List<String> selected = srcList.getSelectedValuesList();
        for (String item : selected) {
            srcModel.removeElement(item);
            destModel.addElement(item);
        }
    }

    private void moveUpDown(int direction) {
        int index = listVisible.getSelectedIndex();
        if (index < 0) return;
        int target = index + direction;
        if (target >= 0 && target < modelVisible.size()) {
            String item = modelVisible.remove(index);
            modelVisible.add(target, item);
            listVisible.setSelectedIndex(target);
        }
    }

    private void onRestore() {
        modelHidden.clear();
        modelVisible.clear();
        for (String col : defaultVisibleCols) {
            modelVisible.addElement(col);
        }
        for (String col : allPossibleCols) {
            if (!defaultVisibleCols.contains(col)) {
                modelHidden.addElement(col);
            }
        }
    }

    private void onAccept() {
        resultVisibleColumns.clear();
        for (int i = 0; i < modelVisible.size(); i++) {
            resultVisibleColumns.add(modelVisible.getElementAt(i));
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public List<String> getResultVisibleColumns() { return resultVisibleColumns; }
}
