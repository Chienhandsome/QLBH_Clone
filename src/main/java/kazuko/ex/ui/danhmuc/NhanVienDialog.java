package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class NhanVienDialog extends JDialog {
    private DefaultListModel<SUser> listModel;
    private JList<SUser> listNhanVien;
    private final UserDAO userDAO = new UserDAO();

    public NhanVienDialog(Frame parent) {
        super(parent, "Nhân viên", true);
        initComponents();
        loadData();
        setSize(420, 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Panel: Icon & Mô tả (giống ảnh 2)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JLabel lblIcon = new JLabel("📝");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        JLabel lblDesc = new JLabel("<html><b>Cho phép bạn quản lý và chỉnh sửa danh mục nhân viên</b></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        headerPanel.add(lblIcon);
        headerPanel.add(lblDesc);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Toolbar + ComboBox + List
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        // Toolbar: Thêm mới, Xóa, Chỉnh sửa, Thùng rác
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnAdd = new JButton("➕ Thêm mới ▾");
        JButton btnDelete = new JButton("❌ Xóa");
        JButton btnEdit = new JButton("✏️ Chỉnh sửa");
        JButton btnTrash = new JButton("🗑️ Thùng rác");

        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setForeground(new Color(0, 120, 60));

        toolBar.add(btnAdd);
        toolBar.add(btnDelete);
        toolBar.add(btnEdit);
        toolBar.add(btnTrash);

        // Sub Header: Giá trị mặc định
        JPanel defaultValPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        defaultValPanel.add(new JLabel("Giá trị mặc định:"));
        JComboBox<String> cbDefault = new JComboBox<>(new String[]{"", "Hoàng Giang", "Phạm Quế Chi", "Phạm Huyền Trang"});
        cbDefault.setPreferredSize(new Dimension(200, 25));
        defaultValPanel.add(cbDefault);

        JPanel topBox = new JPanel(new BorderLayout());
        topBox.add(toolBar, BorderLayout.NORTH);
        topBox.add(defaultValPanel, BorderLayout.SOUTH);
        centerPanel.add(topBox, BorderLayout.NORTH);

        // Danh sách nhân viên (JList có icon)
        listModel = new DefaultListModel<>();
        listNhanVien = new JList<>(listModel);
        listNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listNhanVien.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SUser) {
                    SUser u = (SUser) value;
                    label.setText("  👤  " + u.getName());
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(listNhanVien);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom: Nút Thoát
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnClose = new JButton("Thoát");
        btnClose.setPreferredSize(new Dimension(90, 30));
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        listModel.clear();
        List<SUser> list = userDAO.getAll();
        for (SUser u : list) {
            listModel.addElement(u);
        }
        if (!listModel.isEmpty()) {
            listNhanVien.setSelectedIndex(0);
        }
    }
}
