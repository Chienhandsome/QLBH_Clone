package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class KhachHangTrashDialog extends JDialog {
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final Runnable onDataChanged;

    private JTable tableTrash;
    private DefaultTableModel tableModel;
    private List<KhachHang> trashList;

    public KhachHangTrashDialog(Component parent, Runnable onDataChanged) {
        super(SwingUtilities.getWindowAncestor(parent), "Thùng rác - Danh mục khách hàng", ModalityType.APPLICATION_MODAL);
        this.onDataChanged = onDataChanged;

        initComponents();
        loadTrashData();
        setSize(750, 480);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 205, 235)));
        headerPanel.setPreferredSize(new Dimension(750, 56));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        leftHeader.setOpaque(false);
        JLabel lblTrashIcon = new JLabel(IconHelper.getIcon("RecycleBin.png", 32, 32));
        JLabel lblHeaderTitle = new JLabel("Danh sách khách hàng đã xóa trong thùng rác");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderTitle.setForeground(new Color(30, 30, 30));

        leftHeader.add(lblTrashIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolBar.setBackground(new Color(218, 230, 245));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnRestore = createToolButton("Khôi phục", "Refresh.png");
        JButton btnDeletePerm = createToolButton("Xóa vĩnh viễn", "cross.png");

        btnRestore.addActionListener(e -> onRestoreSelected());
        btnDeletePerm.addActionListener(e -> onDeletePermanently());

        toolBar.add(btnRestore);
        toolBar.add(btnDeletePerm);
        centerPanel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"STT", "Mã khách", "Tên khách hàng", "Điện thoại", "Địa chỉ", "Nhóm khách hàng"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTrash = new JTable(tableModel);
        tableTrash.setRowHeight(22);
        tableTrash.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTrash.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableTrash.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableTrash.getColumnModel().getColumn(1).setPreferredWidth(90);
        tableTrash.getColumnModel().getColumn(2).setPreferredWidth(180);
        tableTrash.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableTrash.getColumnModel().getColumn(4).setPreferredWidth(180);
        tableTrash.getColumnModel().getColumn(5).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(tableTrash);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        JButton btnClose = new JButton("Thoát");
        btnClose.setPreferredSize(new Dimension(85, 26));
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createToolButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private void loadTrashData() {
        tableModel.setRowCount(0);
        trashList = khachHangDAO.getTrashItems();
        int stt = 1;
        for (KhachHang kh : trashList) {
            tableModel.addRow(new Object[]{
                    String.format("%02d", stt++),
                    kh.getMaKhach() != null ? kh.getMaKhach() : "",
                    kh.getName() != null ? kh.getName() : "",
                    kh.getDienThoai() != null ? kh.getDienThoai() : "",
                    kh.getDiaChi() != null ? kh.getDiaChi() : "",
                    kh.getTenNhomKhachHang() != null ? kh.getTenNhomKhachHang() : ""
            });
        }
    }

    private void onRestoreSelected() {
        int selected = tableTrash.getSelectedRow();
        if (selected < 0 || selected >= trashList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần khôi phục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhachHang kh = trashList.get(selected);
        khachHangDAO.restoreFromTrash(kh.getId());
        loadTrashData();
        if (onDataChanged != null) onDataChanged.run();
    }

    private void onDeletePermanently() {
        int selected = tableTrash.getSelectedRow();
        if (selected < 0 || selected >= trashList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa vĩnh viễn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhachHang kh = trashList.get(selected);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa vĩnh viễn khách hàng '" + kh.getName() + "'?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            khachHangDAO.deletePermanently(kh.getId());
            loadTrashData();
            if (onDataChanged != null) onDataChanged.run();
        }
    }
}
