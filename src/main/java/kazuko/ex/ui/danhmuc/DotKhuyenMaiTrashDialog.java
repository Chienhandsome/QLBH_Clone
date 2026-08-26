package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.DotKhuyenMaiDAO;
import kazuko.ex.model.danhmuc.DotKhuyenMai;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class DotKhuyenMaiTrashDialog extends JDialog {
    private final DotKhuyenMaiDAO dotKhuyenMaiDAO = new DotKhuyenMaiDAO();
    private final Runnable onUpdateCallback;

    private JTable tableTrash;
    private DefaultTableModel modelTrash;
    private List<DotKhuyenMai> trashList;
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public DotKhuyenMaiTrashDialog(Frame parent, Runnable onUpdateCallback) {
        super(parent, "Thùng rác - Đợt khuyến mại", true);
        this.onUpdateCallback = onUpdateCallback;

        initUI();
        loadTrashData();
        setSize(750, 420);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        headerPanel.setBackground(new Color(245, 248, 252));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(200, 215, 235)));

        JLabel lblTitle = new JLabel("Danh sách đợt khuyến mại trong Thùng rác");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setIcon(IconHelper.getIcon("RecycleBin.png", 20, 20));
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"STT", "Tên đợt khuyến mại", "Loại hình khuyến mại", "Từ ngày", "Đến ngày", "Tỉ lệ giảm", "Ghi chú"};
        modelTrash = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTrash = new JTable(modelTrash);
        tableTrash.setRowHeight(22);
        tableTrash.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTrash.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTrash.setShowGrid(true);
        tableTrash.setGridColor(new Color(225, 232, 240));

        tableTrash.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableTrash.getColumnModel().getColumn(1).setPreferredWidth(180);
        tableTrash.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableTrash.getColumnModel().getColumn(3).setPreferredWidth(85);
        tableTrash.getColumnModel().getColumn(4).setPreferredWidth(85);
        tableTrash.getColumnModel().getColumn(5).setPreferredWidth(80);
        tableTrash.getColumnModel().getColumn(6).setPreferredWidth(140);

        JScrollPane scrollTable = new JScrollPane(tableTrash);
        scrollTable.setBorder(null);
        add(scrollTable, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footerPanel.setBackground(new Color(235, 242, 252));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        JButton btnRestore = new JButton("Khôi phục");
        btnRestore.setIcon(IconHelper.getIcon("Undo.png", 16, 16));
        btnRestore.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRestore.setBackground(Color.WHITE);

        JButton btnDelete = new JButton("Xóa vĩnh viễn");
        btnDelete.setIcon(IconHelper.getIcon("cross.png", 16, 16));
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDelete.setBackground(Color.WHITE);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.setBackground(Color.WHITE);

        btnRestore.addActionListener(e -> onRestore());
        btnDelete.addActionListener(e -> onDeletePermanently());
        btnClose.addActionListener(e -> dispose());

        footerPanel.add(btnRestore);
        footerPanel.add(btnDelete);
        footerPanel.add(btnClose);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadTrashData() {
        trashList = dotKhuyenMaiDAO.getTrashItems();
        modelTrash.setRowCount(0);
        int idx = 1;
        for (DotKhuyenMai km : trashList) {
            String tuNgay = km.getTuNgay() != null ? sdfDate.format(km.getTuNgay()) : "";
            String denNgay = km.getDenNgay() != null ? sdfDate.format(km.getDenNgay()) : "";
            modelTrash.addRow(new Object[]{
                    idx++,
                    km.getName(),
                    km.getTenLoaiHinhKhuyenMai() != null ? km.getTenLoaiHinhKhuyenMai() : "",
                    tuNgay,
                    denNgay,
                    km.getTiLeGiamGia(),
                    km.getNote() != null ? km.getNote() : ""
            });
        }
    }

    private void onRestore() {
        int selected = tableTrash.getSelectedRow();
        if (selected < 0 || selected >= trashList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đợt khuyến mại cần khôi phục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DotKhuyenMai km = trashList.get(selected);
        dotKhuyenMaiDAO.restoreFromTrash(km.getId());
        loadTrashData();
        if (onUpdateCallback != null) onUpdateCallback.run();
        JOptionPane.showMessageDialog(this, "Đã khôi phục đợt khuyến mại '" + km.getName() + "'!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDeletePermanently() {
        int selected = tableTrash.getSelectedRow();
        if (selected < 0 || selected >= trashList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đợt khuyến mại cần xóa vĩnh viễn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DotKhuyenMai km = trashList.get(selected);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa VĨNH VIỄN đợt khuyến mại '" + km.getName() + "'?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dotKhuyenMaiDAO.deletePermanently(km.getId());
            loadTrashData();
            if (onUpdateCallback != null) onUpdateCallback.run();
        }
    }
}
