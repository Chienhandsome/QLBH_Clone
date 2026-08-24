package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
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

        // Chia đôi màn hình: Trái (Bộ lọc & Cây kho) - Phải (Bảng dữ liệu phiếu xuất)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(230);
        splitPane.setContinuousLayout(true);

        // ==================== KHUNG TRÁI: BỘ LỌC KHO ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel filterBox = new JPanel(new GridBagLayout());
        filterBox.setBorder(new TitledBorder("Kho hàng"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        gbc.gridx = 0; gbc.gridy = 0;
        filterBox.add(new JLabel("Từ:"), gbc);
        JTextField txtTuNgay = new JTextField(DateUtil.formatDate(new Date(System.currentTimeMillis() - 30L*24*3600*1000)));
        gbc.gridx = 1; gbc.gridy = 0;
        filterBox.add(txtTuNgay, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        filterBox.add(new JLabel("Đến:"), gbc);
        JTextField txtDenNgay = new JTextField(DateUtil.formatDate(new Date()));
        gbc.gridx = 1; gbc.gridy = 1;
        filterBox.add(txtDenNgay, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        filterBox.add(new JLabel("Kho xuất:"), gbc);
        cbKhoXuat = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 2;
        filterBox.add(cbKhoXuat, gbc);

        leftPanel.add(filterBox, BorderLayout.NORTH);

        // Cây danh sách kho hàng (TreeView)
        treeKho = new JTree();
        treeKho.setRootVisible(true);
        JScrollPane treeScroll = new JScrollPane(treeKho);
        leftPanel.add(treeScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG PHIẾU XUẤT ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Thanh công cụ thao tác trên đầu bảng (Toolbar)
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolBar.setBackground(new Color(240, 243, 248));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(210, 215, 225)));

        toolBar.add(new JLabel("Lọc (F3):"));
        txtLoc = new JTextField(12);
        toolBar.add(txtLoc);

        JButton btnAdd = new JButton("➕ Thêm mới (Insert)");
        JButton btnEdit = new JButton("✏️ Chỉnh sửa (F4)");
        JButton btnDelete = new JButton("❌ Xóa (Del)");
        JButton btnExcel = new JButton("📊 Xuất excel");
        JButton btnPrint = new JButton("🖨️ In");
        JButton btnTong = new JButton("∑ Tổng");

        btnAdd.setForeground(new Color(0, 120, 60));
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));

        toolBar.add(btnAdd);
        toolBar.add(btnEdit);
        toolBar.add(btnDelete);
        toolBar.add(btnExcel);
        toolBar.add(btnPrint);
        toolBar.add(btnTong);

        rightPanel.add(toolBar, BorderLayout.NORTH);

        // Bảng dữ liệu
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

        // Căn phải các cột số tiền
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tablePhieuXuat.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);
        tablePhieuXuat.getColumnModel().getColumn(10).setCellRenderer(rightRenderer);

        JScrollPane tableScroll = new JScrollPane(tablePhieuXuat);
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        // Tab thông tin chi tiết ở đáy bảng
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

        // Sự kiện chọn cây kho
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
        // Nạp cây kho
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("📁 Tất cả");
        List<KhoHang> listKho = khoHangDAO.getAll();

        cbKhoXuat.removeAllItems();
        cbKhoXuat.addItem(new KhoHang("", "-- Tất cả kho --", ""));

        for (KhoHang kho : listKho) {
            root.add(new DefaultMutableTreeNode(kho));
            cbKhoXuat.addItem(kho);
        }
        root.add(new DefaultMutableTreeNode("🗑️ Thùng rác"));
        treeKho.setModel(new DefaultTreeModel(root));

        // Nạp phiếu xuất
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
