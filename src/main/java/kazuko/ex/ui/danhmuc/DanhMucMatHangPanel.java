package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DanhMucMatHangPanel extends JPanel {
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

    private JTree treeNhomHang;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;

    private JTable tableMatHang;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    private JLabel lblCreatedTime;
    private JLabel lblCreatedUser;
    private JLabel lblModifiedTime;
    private JLabel lblModifiedUser;

    private List<MatHang> currentList = new ArrayList<>();
    private String currentSelectedNhom = "Tất cả";

    public DanhMucMatHangPanel() {
        initUI();
        loadNhomTree();
        loadTableData("Tất cả", "");
        setupShortcuts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 243, 248));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(220);
        mainSplit.setDividerSize(5);

        // ==================== KHUNG TRÁI: NHÓM MẶT HÀNG ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 0, new Color(185, 205, 230)));

        // Title Header Khung Trái
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(225, 235, 248));
        leftHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        leftHeader.setPreferredSize(new Dimension(220, 28));

        JLabel lblLeftTitle = new JLabel(" Nhóm mặt hàng");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(25, 55, 105));
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);

        // Toolbar nhỏ cho Nhóm hàng: [+], [Sửa], [Xóa], [Refresh]
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        leftToolbar.setOpaque(false);

        JButton btnAddNhom = createMiniToolButton("plus.png", "Thêm nhóm mặt hàng");
        JButton btnEditNhom = createMiniToolButton("pencil.png", "Sửa nhóm mặt hàng");
        JButton btnDeleteNhom = createMiniToolButton("cross.png", "Xóa nhóm mặt hàng");
        JButton btnRefreshNhom = createMiniToolButton("Refresh.png", "Làm mới danh mục nhóm");

        btnAddNhom.addActionListener(e -> onAddNhom());
        btnEditNhom.addActionListener(e -> onEditNhom());
        btnDeleteNhom.addActionListener(e -> onDeleteNhom());
        btnRefreshNhom.addActionListener(e -> loadNhomTree());

        leftToolbar.add(btnAddNhom);
        leftToolbar.add(btnEditNhom);
        leftToolbar.add(btnDeleteNhom);
        leftToolbar.add(btnRefreshNhom);
        leftHeader.add(leftToolbar, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Tree Nhóm mặt hàng
        rootTreeNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeNhomHang = new JTree(treeModel);
        treeNhomHang.setRootVisible(false);
        treeNhomHang.setShowsRootHandles(true);
        treeNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomHang.setRowHeight(22);

        treeNhomHang.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if ("Tất cả".equals(userObj)) {
                        label.setText("Tất cả");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Chưa thiết lập".equals(userObj)) {
                        label.setText("Chưa thiết lập");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    } else if (userObj instanceof NhomMatHang) {
                        NhomMatHang nh = (NhomMatHang) userObj;
                        label.setText(nh.getName());
                        String iconName = nh.getIconName() != null ? nh.getIconName() : "Folder.png";
                        label.setIcon(IconHelper.getIcon(iconName, 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }
                return label;
            }
        });

        treeNhomHang.addTreeSelectionListener(e -> {
            TreePath path = treeNhomHang.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObj = node.getUserObject();
                if ("Tất cả".equals(userObj)) {
                    currentSelectedNhom = "Tất cả";
                } else if ("Chưa thiết lập".equals(userObj)) {
                    currentSelectedNhom = "Chưa thiết lập";
                } else if (userObj instanceof NhomMatHang) {
                    currentSelectedNhom = ((NhomMatHang) userObj).getName();
                }
                loadTableData(currentSelectedNhom, txtSearch.getText().trim());
            }
        });

        JScrollPane treeScroll = new JScrollPane(treeNhomHang);
        treeScroll.setBorder(null);
        leftPanel.add(treeScroll, BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG MẶT HÀNG ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Toolbar Khung Phải
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topBar.setBackground(new Color(225, 235, 248));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JLabel lblTag = new JLabel("Mặt hàng");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTag.setForeground(new Color(40, 70, 120));
        lblTag.setBorder(new EmptyBorder(0, 4, 0, 6));
        topBar.add(lblTag);

        JLabel lblLoc = new JLabel("Lọc (F3):");
        lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topBar.add(lblLoc);

        txtSearch = new JTextField(12);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setPreferredSize(new Dimension(140, 24));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onSearch(); }
        });
        topBar.add(txtSearch);

        JButton btnAdd = createToolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        JButton btnEdit = createToolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        JButton btnDelete = createToolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        JButton btnImportExcel = createToolButton("Thêm excel", "Bookmark.png", Color.BLACK, false);
        JButton btnExportExcel = createToolButton("Xuất excel", "Report.png", Color.BLACK, false);
        JButton btnPrint = createToolButton("In", "Preview.png", Color.BLACK, false);
        JButton btnSummary = createToolButton("∑ Tổng", null, Color.BLACK, false);
        JButton btnBarcode = createToolButton("In mã vạch", "Barcode.png", Color.BLACK, false);

        btnAdd.addActionListener(e -> onAddNewProduct());
        btnEdit.addActionListener(e -> onEditSelectedProduct());
        btnDelete.addActionListener(e -> onDeleteSelectedProduct());
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Thêm từ Excel đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnExportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Xuất Excel đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã gửi dữ liệu sang máy in!", "In ấn", JOptionPane.INFORMATION_MESSAGE));
        btnSummary.addActionListener(e -> showSummaryDialog());
        btnBarcode.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng In mã vạch đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        topBar.add(btnAdd);
        topBar.add(btnEdit);
        topBar.add(btnDelete);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnSummary);
        topBar.add(btnBarcode);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Mặt Hàng
        String[] columns = {
                "", "Mã hàng", "Tên mặt hàng", "Nhóm mặt hàng", "Đơn vị tính",
                "Giá bán", "Giá nhập", "Mặc định giảm giá", "Mặc định giảm tiền",
                "Ghi chú", "Không in chế biến", "Bán nhanh"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10 || column == 11; // Checkbox columns editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 10 || columnIndex == 11) return Boolean.class;
                return Object.class;
            }
        };

        tableMatHang = new JTable(tableModel);
        tableMatHang.setRowHeight(22);
        tableMatHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMatHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMatHang.setShowGrid(true);
        tableMatHang.setGridColor(new Color(225, 232, 240));

        JTableHeader th = tableMatHang.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(230, 238, 248));
        th.setForeground(new Color(30, 30, 30));
        th.setPreferredSize(new Dimension(100, 26));

        // Column widths
        tableMatHang.getColumnModel().getColumn(0).setPreferredWidth(26);  // Pointer
        tableMatHang.getColumnModel().getColumn(0).setMaxWidth(30);
        tableMatHang.getColumnModel().getColumn(1).setPreferredWidth(80);  // Mã hàng
        tableMatHang.getColumnModel().getColumn(2).setPreferredWidth(210); // Tên hàng
        tableMatHang.getColumnModel().getColumn(3).setPreferredWidth(120); // Nhóm
        tableMatHang.getColumnModel().getColumn(4).setPreferredWidth(70);  // ĐVT
        tableMatHang.getColumnModel().getColumn(5).setPreferredWidth(90);  // Giá bán
        tableMatHang.getColumnModel().getColumn(6).setPreferredWidth(90);  // Giá nhập
        tableMatHang.getColumnModel().getColumn(7).setPreferredWidth(80);  // Giảm giá %
        tableMatHang.getColumnModel().getColumn(8).setPreferredWidth(80);  // Giảm giá tiền
        tableMatHang.getColumnModel().getColumn(9).setPreferredWidth(100); // Ghi chú
        tableMatHang.getColumnModel().getColumn(10).setPreferredWidth(90); // Không in
        tableMatHang.getColumnModel().getColumn(11).setPreferredWidth(70); // Bán nhanh

        // Custom Cell Renderers
        setupTableRenderers();

        // Row Selection Listener -> Update Bottom Info Panel
        tableMatHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBottomInfo();
            }
        });

        // Double Click -> Edit
        tableMatHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelectedProduct();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableMatHang);
        tableScroll.setBorder(null);

        // 3. Khung Dưới: TabbedPane Thông tin chi tiết
        JPanel bottomInfoPanel = createBottomTabbedPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottomInfoPanel);
        rightSplit.setResizeWeight(0.85);
        rightSplit.setDividerSize(4);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createBottomTabbedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 95));
        panel.setBackground(Color.WHITE);

        JTabbedPane tabInfo = new JTabbedPane();
        tabInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab Thông tin
        JPanel pnlInfo = new JPanel(new GridLayout(2, 2, 20, 4));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(8, 15, 8, 15));

        lblCreatedTime = new JLabel("Khởi tạo: --");
        lblCreatedTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblCreatedUser = new JLabel("Khởi tạo bởi: --");
        lblCreatedUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblModifiedTime = new JLabel("Sửa đổi gần nhất: --");
        lblModifiedTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblModifiedUser = new JLabel("Sửa đổi bởi: --");
        lblModifiedUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlInfo.add(lblCreatedTime);
        pnlInfo.add(lblCreatedUser);
        pnlInfo.add(lblModifiedTime);
        pnlInfo.add(lblModifiedUser);

        tabInfo.addTab("Thông tin", pnlInfo);
        tabInfo.addTab("Đặt hàng nhà cung cấp", new JPanel());
        tabInfo.addTab("Sản xuất", new JPanel());

        panel.add(tabInfo, BorderLayout.CENTER);
        return panel;
    }

    private void setupTableRenderers() {
        DecimalFormat dfMoney = new DecimalFormat("#,##0.##");

        // 1. Column 0: Pointer ▶
        tableMatHang.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lbl.setText(isSelected ? "▶" : "");
                lbl.setForeground(new Color(40, 80, 160));
                lbl.setBackground(isSelected ? new Color(210, 230, 250) : (row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255)));
                return lbl;
            }
        });

        // 2. Default Text Renderer with Zebra Striping
        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        };

        // 3. Price Renderer (Right Aligned)
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    lbl.setText(dfMoney.format(((Number) value).doubleValue()));
                }
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        };

        // 4. Nhóm hàng with icon
        tableMatHang.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setIcon(IconHelper.getIcon("Folder.png", 14, 14));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        });

        // 5. ĐVT with icon
        tableMatHang.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setIcon(IconHelper.getIcon("1282529873_objects.png", 12, 12));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                return lbl;
            }
        });

        for (int i = 1; i <= 2; i++) tableMatHang.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
        tableMatHang.getColumnModel().getColumn(5).setCellRenderer(priceRenderer); // Giá bán
        tableMatHang.getColumnModel().getColumn(6).setCellRenderer(priceRenderer); // Giá nhập
        tableMatHang.getColumnModel().getColumn(7).setCellRenderer(priceRenderer); // Giảm %
        tableMatHang.getColumnModel().getColumn(8).setCellRenderer(priceRenderer); // Giảm tiền
        tableMatHang.getColumnModel().getColumn(9).setCellRenderer(textRenderer);  // Ghi chú
    }

    private void loadNhomTree() {
        rootTreeNode.removeAllChildren();
        DefaultMutableTreeNode nodeAll = new DefaultMutableTreeNode("Tất cả");
        DefaultMutableTreeNode nodeUnassigned = new DefaultMutableTreeNode("Chưa thiết lập");

        rootTreeNode.add(nodeAll);
        rootTreeNode.add(nodeUnassigned);

        List<NhomMatHang> nhomList = nhomMatHangDAO.getAll();
        for (NhomMatHang nh : nhomList) {
            rootTreeNode.add(new DefaultMutableTreeNode(nh));
        }

        treeModel.reload();
        for (int i = 0; i < treeNhomHang.getRowCount(); i++) {
            treeNhomHang.expandRow(i);
        }
        treeNhomHang.setSelectionRow(0); // Select "Tất cả"
    }

    public void loadTableData(String tenNhom, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            currentList = matHangDAO.search(keyword, tenNhom);
        } else {
            currentList = matHangDAO.getByNhom(tenNhom, null);
        }

        tableModel.setRowCount(0);
        for (MatHang mh : currentList) {
            tableModel.addRow(new Object[]{
                    "",
                    mh.getCode(),
                    mh.getName(),
                    mh.getTenNhom() != null ? mh.getTenNhom() : "",
                    mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "",
                    mh.getGiaBan(),
                    mh.getGiaNhap(),
                    mh.getGiamGiaPt(),
                    mh.getGiamGiaTien(),
                    mh.getGhiChu() != null ? mh.getGhiChu() : "",
                    mh.isKhongInCheBien(),
                    mh.isBanNhanh()
            });
        }

        if (!currentList.isEmpty()) {
            tableMatHang.setRowSelectionInterval(0, 0);
            updateBottomInfo();
        } else {
            clearBottomInfo();
        }
    }

    private void updateBottomInfo() {
        int row = tableMatHang.getSelectedRow();
        if (row >= 0 && row < currentList.size()) {
            MatHang mh = currentList.get(row);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

            String createdTime = mh.getTimeCreated() != null ? sdf.format(mh.getTimeCreated()) : "18/10/2016 14:37 PM";
            String modifiedTime = mh.getTimeModified() != null ? sdf.format(mh.getTimeModified()) : "27/03/2018 09:27 AM";

            lblCreatedTime.setText("Khởi tạo: " + createdTime);
            lblCreatedUser.setText("Khởi tạo bởi: " + (mh.getUserCreatedName() != null ? mh.getUserCreatedName() : "Administrator"));
            lblModifiedTime.setText("Sửa đổi gần nhất: " + modifiedTime);
            lblModifiedUser.setText("Sửa đổi bởi: " + (mh.getUserModifiedName() != null ? mh.getUserModifiedName() : "Administrator"));
        }
    }

    private void clearBottomInfo() {
        lblCreatedTime.setText("Khởi tạo: --");
        lblCreatedUser.setText("Khởi tạo bởi: --");
        lblModifiedTime.setText("Sửa đổi gần nhất: --");
        lblModifiedUser.setText("Sửa đổi bởi: --");
    }

    private void onSearch() {
        loadTableData(currentSelectedNhom, txtSearch.getText().trim());
    }

    private MatHang getSelectedMatHang() {
        int row = tableMatHang.getSelectedRow();
        if (row >= 0 && row < currentList.size()) {
            return currentList.get(row);
        }
        return null;
    }

    private void onAddNewProduct() {
        MatHang newMH = new MatHang();
        if (!"Tất cả".equalsIgnoreCase(currentSelectedNhom) && !"Chưa thiết lập".equalsIgnoreCase(currentSelectedNhom)) {
            newMH.setTenNhom(currentSelectedNhom);
        }
        MatHangEditDialog dialog = new MatHangEditDialog(this, newMH, () -> {
            loadTableData(currentSelectedNhom, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void onEditSelectedProduct() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mặt hàng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MatHangEditDialog dialog = new MatHangEditDialog(this, sel, () -> {
            loadTableData(currentSelectedNhom, txtSearch.getText().trim());
        });
        dialog.setVisible(true);
    }

    private void onDeleteSelectedProduct() {
        MatHang sel = getSelectedMatHang();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mặt hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa mặt hàng '" + sel.getName() + "' (" + sel.getCode() + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = matHangDAO.delete(sel.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa mặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadTableData(currentSelectedNhom, txtSearch.getText().trim());
            }
        }
    }

    private void onAddNhom() {
        NhomMatHang newNhom = new NhomMatHang();
        NhomMatHangDialog dialog = new NhomMatHangDialog(this, newNhom, () -> loadNhomTree());
        dialog.setVisible(true);
    }

    private void onEditNhom() {
        TreePath path = treeNhomHang.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm mặt hàng cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomMatHang) {
            NhomMatHang nh = (NhomMatHang) node.getUserObject();
            NhomMatHangDialog dialog = new NhomMatHangDialog(this, nh, () -> loadNhomTree());
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể sửa nhóm mặc định này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDeleteNhom() {
        TreePath path = treeNhomHang.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm mặt hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomMatHang) {
            NhomMatHang nh = (NhomMatHang) node.getUserObject();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn xóa nhóm mặt hàng '" + nh.getName() + "'?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                nhomMatHangDAO.delete(nh.getId());
                loadNhomTree();
            }
        }
    }

    private void showSummaryDialog() {
        int totalItems = currentList.size();
        double totalGiaBan = 0;
        double totalGiaNhap = 0;
        for (MatHang mh : currentList) {
            totalGiaBan += mh.getGiaBan();
            totalGiaNhap += mh.getGiaNhap();
        }
        DecimalFormat df = new DecimalFormat("#,##0.## VNĐ");
        JOptionPane.showMessageDialog(this,
                "<html><b>TỔNG HỢP DANH MỤC MẶT HÀNG</b><br><br>" +
                "• Tổng số lượng mặt hàng: <b>" + totalItems + "</b> mặt hàng<br>" +
                "• Tổng giá trị bán niêm yết: <b>" + df.format(totalGiaBan) + "</b><br>" +
                "• Tổng giá trị nhập niêm yết: <b>" + df.format(totalGiaNhap) + "</b></html>",
                "∑ Tổng mặt hàng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupShortcuts() {
        // F3 -> Focus Search
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.requestFocus();
                txtSearch.selectAll();
            }
        });

        // Insert -> Add
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "addNew");
        getActionMap().put("addNew", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddNewProduct();
            }
        });

        // F4 -> Edit
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "editSelected");
        getActionMap().put("editSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEditSelectedProduct();
            }
        });

        // Delete -> Delete
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelected");
        getActionMap().put("deleteSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteSelectedProduct();
            }
        });
    }

    private JButton createToolButton(String text, String iconName, Color textColor, boolean isBold) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private JButton createMiniToolButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(1, 1, 1, 1));
        return btn;
    }
}
