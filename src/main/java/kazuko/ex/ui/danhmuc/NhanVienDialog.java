package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SUser;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

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

        // Header Panel: Icon & Mô tả
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        headerPanel.setBackground(AppColors.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.BORDER_GRAY));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("Folder2.png", 32, 32));
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
        JButton btnAdd = new JButton("Thêm mới ▾", IconHelper.getIcon("plus.png", 16, 16));
        JButton btnDelete = new JButton("Xóa", IconHelper.getIcon("cross.png", 16, 16));
        JButton btnEdit = new JButton("Chỉnh sửa", IconHelper.getIcon("pencil.png", 16, 16));
        JButton btnTrash = new JButton("Thùng rác", IconHelper.getIcon("RecycleBin.png", 16, 16));

        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setForeground(AppColors.SUCCESS_GREEN);
        btnDelete.setForeground(AppColors.DANGER_RED);

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

        // Danh sách nhân viên (JList có icon users.png)
        listModel = new DefaultListModel<>();
        listNhanVien = new JList<>(listModel);
        listNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listNhanVien.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SUser) {
                    SUser u = (SUser) value;
                    label.setIcon(IconHelper.getIcon("users.png", 16, 16));
                    label.setText(" " + u.getName());
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
