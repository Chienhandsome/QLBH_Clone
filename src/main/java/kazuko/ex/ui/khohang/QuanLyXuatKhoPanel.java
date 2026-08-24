package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class QuanLyXuatKhoPanel extends JPanel {
    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();

    private JTree treeKho;
    private JTable tablePhieuXuat;
    private DefaultTableModel tableModel;
    private JTextField txtLoc;
    private JComboBox<KhoHang> cbKhoXuat;

    public QuanLyXuatKhoPanel() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);

        // ==================== KHUNG TRÁI: CÂY KHO HÀNG ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Kho hàng"));

        treeKho = new JTree();
        treeKho.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if ("Tất cả".equals(userObj)) {
                        label.setText("Tất cả");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                    } else if ("Thùng rác".equals(userObj)) {
                        label.setText("Thùng rác");
                        label.setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                    } else if (userObj instanceof KhoHang) {
                        label.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                    }
                }
                return label;
            }
        });
        JScrollPane treeScroll = new JScrollPane(treeKho);
        leftPanel.add(treeScroll, BorderLayout.CENTER);

        // Khung lọc ngày & kho xuất
        JPanel filterPanel = new JPanel(new GridLayout(4, 1, 4, 4));
        filterPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        cbKhoXuat = new JComboBox<>();
        filterPanel.add(new JLabel("Kho xuất:"));
        filterPanel.add(cbKhoXuat);

        JButton btnLoc = new JButton("Xem dữ liệu", IconHelper.getIcon("Search43.png", 16, 16));
        btnLoc.addActionListener(e -> {
            KhoHang sel = (KhoHang) cbKhoXuat.getSelectedItem();
            loadPhieuXuat(sel != null ? sel.getId() : null);
        });
        filterPanel.add(btnLoc);

        leftPanel.add(filterPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG PHIẾU XUẤT ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolBar.setBackground(AppColors.TOOLBAR_BG);
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.BORDER_BLUE));

        toolBar.add(new JLabel("Lọc (F3):"));
        txtLoc = new JTextField(12);
        toolBar.add(txtLoc);

        JButton btnAdd = new JButton("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 16, 16));
        JButton btnEdit = new JButton("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        JButton btnDelete = new JButton("Xóa (Del)", IconHelper.getIcon("cross.png", 16, 16));
        JButton btnExcel = new JButton("Xuất excel", IconHelper.getIcon("export_excel.png", 16, 16));
        JButton btnPrint = new JButton("In", IconHelper.getIcon("print-16.png", 16, 16));
        JButton btnTong = new JButton("Tổng", IconHelper.getIcon("Statistic16.png", 16, 16));

        btnAdd.setForeground(AppColors.SUCCESS_GREEN);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setForeground(AppColors.DANGER_RED);

        toolBar.add(btnAdd);
        toolBar.add(btnEdit);
        toolBar.add(btnDelete);
        toolBar.add(btnExcel);
        toolBar.add(btnPrint);
        toolBar.add(btnTong);

        rightPanel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {
                "Ngày", "Kho xuất", "Nhân viên xuất", "Tổng cộng", "Số phiếu",
                "Tiền giảm giá", "Tỉ lệ giảm", "Tiền hàng", "Cửa hàng", "Còn lại", "Thanh toán", "Tài khoản NH"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePhieuXuat = new JTable(tableModel);
        tablePhieuXuat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tablePhieuXuat.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(10).setCellRenderer(rightRenderer);

        JScrollPane tableScroll = new JScrollPane(tablePhieuXuat);
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.setPreferredSize(new Dimension(100, 140));
        bottomTabs.addTab("Thông tin", new JPanel());
        bottomTabs.addTab("Phiếu thu", new JPanel());
        bottomTabs.addTab("Phiếu chi", new JPanel());
        bottomTabs.addTab("Phiếu thu công nợ", new JPanel());
        bottomTabs.addTab("Xuất kho chi tiết", new JPanel());
        rightPanel.add(bottomTabs, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        treeKho.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeKho.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof KhoHang) {
                KhoHang selectedKho = (KhoHang) node.getUserObject();
                loadPhieuXuat(selectedKho.getId());
            } else {
                loadPhieuXuat(null);
            }
        });
    }

    public void loadData() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        List<KhoHang> listKho = khoHangDAO.getAll();

        cbKhoXuat.removeAllItems();
        cbKhoXuat.addItem(new KhoHang("", "-- Tất cả kho --", ""));

        for (KhoHang kho : listKho) {
            root.add(new DefaultMutableTreeNode(kho));
            cbKhoXuat.addItem(kho);
        }
        root.add(new DefaultMutableTreeNode("Thùng rác"));
        treeKho.setModel(new DefaultTreeModel(root));

        loadPhieuXuat(null);
    }

    private void loadPhieuXuat(String khoId) {
        tableModel.setRowCount(0);
        List<DonHang> list = donHangDAO.getPhieuXuatKho(null, null, khoId);
        for (DonHang dh : list) {
            tableModel.addRow(new Object[]{
                    DateUtil.formatDate(dh.getNgay()),
                    "KHO BÁN HÀNG",
                    "Administrator",
                    CurrencyUtil.formatMoney(dh.getTongCong()),
                    dh.getName(),
                    CurrencyUtil.formatMoney(dh.getTienGiamGia()),
                    dh.getTiLeGiamGia() + "%",
                    CurrencyUtil.formatMoney(dh.getTienHang()),
                    "TRỤ SỞ CHÍNH",
                    CurrencyUtil.formatMoney(dh.getConLai()),
                    CurrencyUtil.formatMoney(dh.getTienThanhToan()),
                    ""
            });
        }
    }
}
