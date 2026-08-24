package kazuko.ex.ui.main;

import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class QuickSearchDialog extends JDialog {
    private final Consumer<String> onActionSelected;
    private JTextField txtSearch;
    private JList<String> listFunctions;
    private DefaultListModel<String> listModel;

    private static final List<String> ALL_FUNCTIONS = Arrays.asList(
            "BẢNG TỔNG HỢP THỊ PHẦN",
            "BÁO CÁO 20 MẶT HÀNG BÁN CHẠY NHẤT",
            "BÁO CÁO BÁN HÀNG THEO GIỜ",
            "BÁO CÁO BÁN HÀNG THEO KHÁCH HÀNG",
            "BÁO CÁO BÁN HÀNG THEO KÍCH THƯỚC",
            "BÁO CÁO BÁN HÀNG THEO NGÀY",
            "BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN KINH DOANH",
            "BÁO CÁO BÁN HÀNG THEO THU NGÂN",
            "BÁO CÁO CHI TIẾT BÁN HÀNG THEO NGÀY",
            "BÁO CÁO CHI TIẾT BÁN HÀNG THEO THU NGÂN",
            "BÁO CÁO CHI TIẾT HÀNG KHUYẾN MẠI",
            "BÁO CÁO BÁN HÀNG",
            "BÁO CÁO KHO HÀNG",
            "BÁO CÁO QUỸ",
            "TỔNG HỢP KẾT QUẢ KINH DOANH (KQKD)",
            "HÓA ĐƠN BÁN HÀNG",
            "QUẢN LÝ BÁN HÀNG",
            "BÁO GIÁ KHÁCH HÀNG",
            "KHÁCH ĐẶT HÀNG",
            "KHÁCH HÀNG THÂN THIẾT",
            "GỬI TIN NHẮN KHÁCH HÀNG",
            "LƯU VẾT HOẠT ĐỘNG",
            "THỐNG KÊ DOANH THU",
            "THỐNG KÊ MẶT HÀNG BÁN",
            "CHI TIẾT HOẠT ĐỘNG NGÀY",
            "DANH MỤC CỬA HÀNG",
            "DANH MỤC MẶT HÀNG",
            "DANH MỤC KHÁCH HÀNG",
            "DANH MỤC ĐỢT KHUYẾN MÃI",
            "DANH MỤC KHO HÀNG",
            "DANH MỤC NHÀ CUNG CẤP",
            "ĐẶT HÀNG NHÀ CUNG CẤP",
            "NHẬP HÀNG VÀO KHO",
            "QUẢN LÝ XUẤT KHO",
            "XUẤT KHO",
            "CHUYỂN KHO",
            "KIỂM KÊ KHO",
            "TÍNH LẠI GIÁ VỐN",
            "TỒN KHO",
            "TỒN NHIỀU KHO",
            "DANH MỤC LÝ DO THU CHI",
            "DANH MỤC TÀI KHOẢN NGÂN HÀNG",
            "TẠO PHIẾU THU",
            "TẠO PHIẾU CHI",
            "DANH MỤC PHIẾU THU",
            "DANH MỤC PHIẾU CHI",
            "TỒN QUỸ",
            "CÔNG NỢ KHÁCH HÀNG",
            "CÔNG NỢ NHÀ CUNG CẤP",
            "DANH MỤC NHÂN VIÊN",
            "DANH MỤC CA LÀM VIỆC",
            "TẠM ỨNG LƯƠNG",
            "THƯỞNG PHẠT",
            "CHẤM CÔNG",
            "TÍNH LƯƠNG",
            "THƯ VIỆN ẢNH",
            "CÔNG NỢ KHÁCH HÀNG BAN ĐẦU",
            "CÔNG NỢ NHÀ CUNG CẤP BAN ĐẦU",
            "TỒN KHO BAN ĐẦU",
            "CHỈ TIÊU DOANH THU",
            "XÓA DỮ LIỆU",
            "CÔNG CỤ NHÀ PHÁT TRIỂN",
            "NGƯỜI DÙNG VÀ PHÂN QUYỀN",
            "CẤU HÌNH TOÀN HỆ THỐNG",
            "CƠ SỞ DỮ LIỆU - TẠO MỚI",
            "CƠ SỞ DỮ LIỆU - SAO LƯU",
            "CƠ SỞ DỮ LIỆU - PHỤC HỒI",
            "CẤU HÌNH CƠ SỞ DỮ LIỆU",
            "ĐỔI MẬT KHẨU",
            "ĐĂNG XUẤT",
            "THOÁT",
            "ĐĂNG KÍ BẢN QUYỀN",
            "THÔNG TIN PHẦN MỀM"
    );

    public QuickSearchDialog(Frame parent, Consumer<String> onActionSelected) {
        super(parent, "TÌM KIẾM MENU", true);
        this.onActionSelected = onActionSelected;
        initComponents();
        setSize(480, 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 244, 250));

        // Top Panel: Tiêu đề hướng dẫn & Ô tìm kiếm
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(12, 14, 10, 14));
        topPanel.setOpaque(false);

        JLabel lblGuide = new JLabel("Sử dụng phím nóng Ctrl + G để mở nhanh giao diện này");
        lblGuide.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGuide.setForeground(new Color(20, 20, 20));
        lblGuide.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(lblGuide);

        topPanel.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchRow.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(200, 26));
        searchRow.add(txtSearch, BorderLayout.CENTER);

        topPanel.add(searchRow);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Danh sách chức năng
        listModel = new DefaultListModel<>();
        ALL_FUNCTIONS.forEach(listModel::addElement);

        listFunctions = new JList<>(listModel);
        listFunctions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listFunctions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listFunctions.setCellRenderer(new FunctionListCellRenderer());
        if (listModel.size() > 0) {
            listFunctions.setSelectedIndex(0);
        }

        JScrollPane scrollPane = new JScrollPane(listFunctions);
        scrollPane.setBorder(new LineBorder(new Color(180, 200, 225), 1));
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 14, 10, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel: Nút Mở & Hủy bỏ
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(new Color(225, 235, 248));
        bottomPanel.setBorder(new LineBorder(new Color(190, 210, 230), 1, true));

        JButton btnOpen = new JButton("Mở", IconHelper.getIcon("btnOpen.Image.png", 14, 14));
        JButton btnCancel = new JButton("Hủy bỏ", IconHelper.getIcon("cross.png", 14, 14));

        btnOpen.setPreferredSize(new Dimension(85, 28));
        btnCancel.setPreferredSize(new Dimension(85, 28));

        btnOpen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnOpen.setBackground(AppColors.WHITE);
        btnCancel.setBackground(AppColors.WHITE);

        btnOpen.addActionListener(e -> executeSelection());
        btnCancel.addActionListener(e -> dispose());

        bottomPanel.add(btnOpen);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Sự kiện gõ tìm kiếm real-time
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterList(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterList(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterList(); }
        });

        // Điều hướng phím mũi tên Lên/Xuống từ ô tìm kiếm sang JList
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int nextIdx = Math.min(listFunctions.getSelectedIndex() + 1, listModel.getSize() - 1);
                    if (nextIdx >= 0) {
                        listFunctions.setSelectedIndex(nextIdx);
                        listFunctions.ensureIndexIsVisible(nextIdx);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int prevIdx = Math.max(listFunctions.getSelectedIndex() - 1, 0);
                    if (prevIdx < listModel.getSize()) {
                        listFunctions.setSelectedIndex(prevIdx);
                        listFunctions.ensureIndexIsVisible(prevIdx);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeSelection();
                }
            }
        });

        // Double click hoặc Enter trên danh sách
        listFunctions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    executeSelection();
                }
            }
        });

        listFunctions.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeSelection();
                }
            }
        });

        // Phím ESC đóng hộp thoại
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Enter kích hoạt nút Mở
        getRootPane().setDefaultButton(btnOpen);
    }

    private void filterList() {
        String query = removeAccent(txtSearch.getText().trim().toLowerCase(Locale.ROOT));
        listModel.clear();
        for (String func : ALL_FUNCTIONS) {
            String normFunc = removeAccent(func.toLowerCase(Locale.ROOT));
            if (normFunc.contains(query)) {
                listModel.addElement(func);
            }
        }
        if (listModel.size() > 0) {
            listFunctions.setSelectedIndex(0);
        }
    }

    private void executeSelection() {
        String selected = listFunctions.getSelectedValue();
        if (selected != null && !selected.isEmpty()) {
            dispose();
            if (onActionSelected != null) {
                onActionSelected.accept(selected);
            }
        }
    }

    private static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private static class FunctionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(4, 6, 4, 6));
            label.setIcon(IconHelper.getIcon("1282529873_objects.png", 14, 14));
            label.setText(" " + value.toString());

            if (isSelected) {
                label.setBackground(new Color(0, 120, 215));
                label.setForeground(Color.WHITE);
            } else {
                if (index % 2 == 0) {
                    label.setBackground(new Color(242, 248, 255));
                } else {
                    label.setBackground(Color.WHITE);
                }
                label.setForeground(new Color(30, 30, 30));
            }
            return label;
        }
    }
}
