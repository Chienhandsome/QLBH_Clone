package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.ThuongPhatDAO;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.nhansu.ThuongPhat;
import kazuko.ex.ui.danhmuc.NhanVienDialog;
import kazuko.ex.ui.danhmuc.NhanVienEditDialog;
import kazuko.ex.ui.danhmuc.NhanVienTrashDialog;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.ExcelExporter;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ThuongPhatPanel extends JPanel {

    private final ThuongPhatDAO thuongPhatDAO = new ThuongPhatDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    // Khung Trái: Cây Nhân viên
    private JTree treeNhanVien;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootTreeNode;
    private NhanVien currentSelectedEmployee = null;
    private String currentSelectedTreeCategory = "Tất cả";

    // Khung Phải: Toolbar & Bảng Thưởng phạt
    private JTextField txtSearch;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnImportExcel;
    private JButton btnExportExcel;
    private JButton btnPrint;
    private JButton btnTotal;
    private JButton btnAnalytics;

    private JTable tableThuongPhat;
    private DefaultTableModel tableModel;
    private List<ThuongPhat> currentDisplayedList = new ArrayList<>();
    private ThuongPhat currentSelectedThuongPhat = null;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public ThuongPhatPanel() {
        initComponents();
        loadTreeData();
        loadTableData();
        setupShortcuts();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 252));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(220);
        mainSplit.setDividerSize(5);

        // ==================== KHUNG TRÁI: NHÂN VIÊN ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 0, new Color(185, 205, 230)));

        // Header Trái: "Nhân viên" + Icon bánh răng
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(225, 235, 248));
        leftHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        leftHeader.setPreferredSize(new Dimension(220, 28));

        JLabel lblLeftTitle = new JLabel(" Nhân viên");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(25, 55, 105));
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);

        JButton btnSetting = new JButton(IconHelper.getIcon("Settings.png", 16, 16));
        btnSetting.setToolTipText("Quản lý danh mục nhân viên");
        btnSetting.setBorderPainted(false);
        btnSetting.setContentAreaFilled(false);
        btnSetting.setFocusPainted(false);
        btnSetting.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSetting.addActionListener(e -> {
            NhanVienDialog dlg = new NhanVienDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            loadTreeData();
            loadTableData();
        });
        leftHeader.add(btnSetting, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Mini Toolbar Trái (+, Sửa, Xóa, Làm mới)
        JPanel leftToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 4));
        leftToolBar.setBackground(new Color(242, 247, 253));
        leftToolBar.setBorder(new EmptyBorder(4, 4, 4, 4));

        JButton btnThemNV = createMiniToolButton("plus.png", "Thêm mới nhân viên");
        JButton btnSuaNV = createMiniToolButton("pencil.png", "Sửa nhân viên");
        JButton btnXoaNV = createMiniToolButton("cross.png", "Chuyển nhân viên vào thùng rác");
        JButton btnRefreshTree = createMiniToolButton("Refresh.png", "Tải lại danh sách nhân viên");

        btnThemNV.addActionListener(e -> onAddNewEmployee());
        btnSuaNV.addActionListener(e -> onEditSelectedEmployee());
        btnXoaNV.addActionListener(e -> onDeleteSelectedEmployee());
        btnRefreshTree.addActionListener(e -> loadTreeData());

        leftToolBar.add(btnThemNV);
        leftToolBar.add(btnSuaNV);
        leftToolBar.add(btnXoaNV);
        leftToolBar.add(btnRefreshTree);

        // Cây Nhân viên
        rootTreeNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeNhanVien = new JTree(treeModel);
        treeNhanVien.setRootVisible(false);
        treeNhanVien.setShowsRootHandles(true);
        treeNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhanVien.setRowHeight(22);
        treeNhanVien.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if ("Tất cả".equals(userObj)) {
                        label.setText("Tất cả");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        label.setForeground(new Color(25, 55, 105));
                    } else if ("Chưa thiết lập".equals(userObj)) {
                        label.setText("Chưa thiết lập");
                        label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        label.setForeground(Color.DARK_GRAY);
                    } else if ("Thùng rác".equals(userObj)) {
                        label.setText("Thùng rác");
                        label.setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        label.setForeground(new Color(30, 60, 110));
                    } else if (userObj instanceof NhanVien) {
                        NhanVien nv = (NhanVien) userObj;
                        label.setText(nv.getName());
                        String iconName = nv.getAvatarIcon();
                        if (iconName == null || iconName.trim().isEmpty()) {
                            iconName = "user_female_1";
                        }
                        label.setIcon(IconHelper.getIcon(iconName, 16, 16));
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }
                return label;
            }
        });

        treeNhanVien.addTreeSelectionListener(e -> {
            TreePath path = treeNhanVien.getSelectionPath();
            if (path == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObj = node.getUserObject();

            if ("Tất cả".equals(userObj)) {
                currentSelectedTreeCategory = "Tất cả";
                currentSelectedEmployee = null;
            } else if ("Chưa thiết lập".equals(userObj)) {
                currentSelectedTreeCategory = "Chưa thiết lập";
                currentSelectedEmployee = null;
            } else if ("Thùng rác".equals(userObj)) {
                currentSelectedTreeCategory = "Thùng rác";
                currentSelectedEmployee = null;
            } else if (userObj instanceof NhanVien) {
                currentSelectedTreeCategory = "NhanVien";
                currentSelectedEmployee = (NhanVien) userObj;
            }
            loadTableData();
        });

        // Double click vào 1 nhân viên trong list bên trái -> Mở dialog chỉnh sửa nhân viên
        treeNhanVien.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    if (currentSelectedEmployee != null) {
                        onEditSelectedEmployee();
                    }
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(treeNhanVien);
        treeScroll.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 225, 240)));

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.add(leftToolBar, BorderLayout.NORTH);
        leftContainer.add(treeScroll, BorderLayout.CENTER);
        leftPanel.add(leftContainer, BorderLayout.CENTER);

        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: THƯỞNG PHẠT ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // 1. Top Bar: Search + Action Buttons
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        topBar.setBackground(new Color(225, 238, 252));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JLabel lblSearch = new JLabel("Lọc (F3):");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSearch.setForeground(new Color(20, 40, 80));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(140, 24));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { loadTableData(); }
            @Override
            public void removeUpdate(DocumentEvent e) { loadTableData(); }
            @Override
            public void changedUpdate(DocumentEvent e) { loadTableData(); }
        });

        btnAdd = createTopBarButton("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 14, 14));
        btnEdit = createTopBarButton("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 14, 14));
        btnDelete = createTopBarButton("Xóa (Del)", IconHelper.getIcon("cross.png", 14, 14));
        btnImportExcel = createTopBarButton("Thêm excel", IconHelper.getIcon("document-excel-table.png", 14, 14));
        btnExportExcel = createTopBarButton("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        btnPrint = createTopBarButton("In", IconHelper.getIcon("printer.png", 14, 14));
        btnTotal = createTopBarButton("Tổng", IconHelper.getIcon("sum.png", 14, 14));
        btnAnalytics = createTopBarButton("P.tích", IconHelper.getIcon("chart.png", 14, 14));

        btnAdd.addActionListener(e -> onAddNewThuongPhat());
        btnEdit.addActionListener(e -> onEditSelectedThuongPhat());
        btnDelete.addActionListener(e -> onDeleteSelectedThuongPhat());
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Thêm từ Excel đang được hỗ trợ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnExportExcel.addActionListener(e -> onExportExcel());
        btnPrint.addActionListener(e -> onPrint());
        btnTotal.addActionListener(e -> showTotalSummary());
        btnAnalytics.addActionListener(e -> showAnalyticsSummary());

        topBar.add(lblSearch);
        topBar.add(txtSearch);
        topBar.add(btnAdd);
        topBar.add(btnEdit);
        topBar.add(btnDelete);
        topBar.add(btnImportExcel);
        topBar.add(btnExportExcel);
        topBar.add(btnPrint);
        topBar.add(btnTotal);
        topBar.add(btnAnalytics);

        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2. Table Thưởng phạt
        String[] columns = {"", "Số phiếu", "Ngày", "Nhân viên", "Thưởng", "Phạt", "Lý do thưởng phạt", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableThuongPhat = new JTable(tableModel);
        tableThuongPhat.setRowHeight(22);
        tableThuongPhat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableThuongPhat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableThuongPhat.setShowGrid(true);
        tableThuongPhat.setGridColor(new Color(225, 232, 240));
        tableThuongPhat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Setup widths
        int[] widths = {35, 110, 95, 150, 100, 80, 160, 160};
        for (int i = 0; i < widths.length; i++) {
            if (i < tableThuongPhat.getColumnModel().getColumnCount()) {
                tableThuongPhat.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }

        // Table Header
        JTableHeader header = tableThuongPhat.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(230, 240, 252));
        header.setForeground(new Color(30, 60, 110));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 26));

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }
        });

        // Custom Cell Renderer
        tableThuongPhat.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    // STT kèm con trỏ ▶
                    JLabel lbl = (JLabel) c;
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    if (isSelected) {
                        lbl.setText("▶ " + (row + 1));
                        lbl.setBackground(new Color(220, 235, 255));
                        lbl.setForeground(new Color(20, 50, 120));
                    } else {
                        lbl.setText(String.valueOf(row + 1));
                        lbl.setBackground(new Color(245, 248, 252));
                        lbl.setForeground(Color.DARK_GRAY);
                    }
                    return lbl;
                }

                if (column == 1 && isSelected) {
                    // Số phiếu khi chọn màu cam đất
                    c.setBackground(new Color(188, 104, 52));
                    c.setForeground(Color.WHITE);
                    return c;
                }

                if (column == 4 || column == 5) {
                    // Thưởng / Phạt căn phải
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                } else if (column == 2) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (isSelected) {
                    // Highlight green matching screenshot row selection
                    c.setBackground(new Color(46, 125, 50)); // Dark green highlight matching screenshot row 1
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 254));
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        tableThuongPhat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableThuongPhat.getSelectedRow();
                if (row >= 0 && row < currentDisplayedList.size()) {
                    currentSelectedThuongPhat = currentDisplayedList.get(row);
                } else {
                    currentSelectedThuongPhat = null;
                }
            }
        });

        tableThuongPhat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelectedThuongPhat();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableThuongPhat);
        tableScroll.setBorder(null);
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private JButton createMiniToolButton(String iconName, String toolTip) {
        JButton btn = new JButton(IconHelper.getIcon(iconName, 14, 14));
        btn.setToolTipText(toolTip);
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(190, 210, 235), 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createTopBarButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void loadTreeData() {
        rootTreeNode.removeAllChildren();

        DefaultMutableTreeNode nodeTatCa = new DefaultMutableTreeNode("Tất cả");
        rootTreeNode.add(nodeTatCa);

        DefaultMutableTreeNode nodeChuaThietLap = new DefaultMutableTreeNode("Chưa thiết lập");
        rootTreeNode.add(nodeChuaThietLap);

        List<NhanVien> all = nhanVienDAO.getAllActive();
        for (NhanVien nv : all) {
            if (nv.getStatus() >= 0 && !nv.isFolder() && !nv.isSeparator()) {
                DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(nv);
                rootTreeNode.add(itemNode);
            }
        }

        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");
        rootTreeNode.add(nodeTrash);

        treeModel.reload();
        treeNhanVien.setSelectionPath(new TreePath(nodeTatCa.getPath()));
    }

    public void loadTableData() {
        List<ThuongPhat> all = "Thùng rác".equals(currentSelectedTreeCategory) ?
                thuongPhatDAO.getTrashItems() : thuongPhatDAO.getAll();

        List<ThuongPhat> filtered = all.stream().filter(tp -> {
            // Lọc theo cây Nhân viên
            if ("NhanVien".equals(currentSelectedTreeCategory) && currentSelectedEmployee != null) {
                if (tp.getNhanVienId() != null && !tp.getNhanVienId().equals(currentSelectedEmployee.getId())) {
                    if (tp.getTenNhanVien() == null || !tp.getTenNhanVien().equalsIgnoreCase(currentSelectedEmployee.getName())) {
                        return false;
                    }
                }
            } else if ("Chưa thiết lập".equals(currentSelectedTreeCategory)) {
                if (tp.getNhanVienId() != null && !tp.getNhanVienId().trim().isEmpty()) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());

        // Lọc theo từ khóa tìm kiếm nhanh
        String searchKey = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        if (!searchKey.isEmpty()) {
            filtered = filtered.stream().filter(tp -> {
                String sp = tp.getSoPhieu() != null ? tp.getSoPhieu().toLowerCase() : "";
                String nv = tp.getTenNhanVien() != null ? tp.getTenNhanVien().toLowerCase() : "";
                String ld = tp.getTenLyDo() != null ? tp.getTenLyDo().toLowerCase() : "";
                String gc = tp.getGhiChu() != null ? tp.getGhiChu().toLowerCase() : "";
                return sp.contains(searchKey) || nv.contains(searchKey) || ld.contains(searchKey) || gc.contains(searchKey);
            }).collect(Collectors.toList());
        }

        currentDisplayedList = filtered;

        tableModel.setRowCount(0);
        for (ThuongPhat tp : currentDisplayedList) {
            String ngayStr = tp.getNgay() != null ? sdfDate.format(tp.getNgay()) : "";
            String thuongStr = CurrencyUtil.format(tp.getTienThuong());
            String phatStr = CurrencyUtil.format(tp.getTienPhat());

            tableModel.addRow(new Object[]{
                    "",
                    tp.getSoPhieu() != null ? tp.getSoPhieu() : "",
                    ngayStr,
                    tp.getTenNhanVien() != null ? tp.getTenNhanVien() : "",
                    thuongStr,
                    phatStr,
                    tp.getTenLyDo() != null ? tp.getTenLyDo() : "",
                    tp.getGhiChu() != null ? tp.getGhiChu() : ""
            });
        }

        if (!currentDisplayedList.isEmpty()) {
            tableThuongPhat.setRowSelectionInterval(0, 0);
        }
    }

    private void onAddNewEmployee() {
        NhanVienEditDialog dlg = new NhanVienEditDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                nhanVienDAO.getAllActive(),
                this::loadTreeData
        );
        dlg.setVisible(true);
    }

    private void onEditSelectedEmployee() {
        if (currentSelectedEmployee == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên trên cây để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        NhanVienEditDialog dlg = new NhanVienEditDialog(
                SwingUtilities.getWindowAncestor(this),
                currentSelectedEmployee,
                nhanVienDAO.getAllActive(),
                this::loadTreeData
        );
        dlg.setVisible(true);
    }

    private void onDeleteSelectedEmployee() {
        if (currentSelectedEmployee == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên trên cây để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn chuyển nhân viên [" + currentSelectedEmployee.getName() + "] vào thùng rác không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            nhanVienDAO.moveToTrash(currentSelectedEmployee.getId());
            currentSelectedEmployee = null;
            loadTreeData();
            loadTableData();
        }
    }

    private void onAddNewThuongPhat() {
        NhanVien preSelected = currentSelectedEmployee;
        TaoThuongPhatDialog dlg = new TaoThuongPhatDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                preSelected,
                this::loadTableData
        );
        dlg.setVisible(true);
    }

    private void onEditSelectedThuongPhat() {
        if (currentSelectedThuongPhat == null) {
            int row = tableThuongPhat.getSelectedRow();
            if (row >= 0 && row < currentDisplayedList.size()) {
                currentSelectedThuongPhat = currentDisplayedList.get(row);
            }
        }
        if (currentSelectedThuongPhat == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu thưởng phạt để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaoThuongPhatDialog dlg = new TaoThuongPhatDialog(
                SwingUtilities.getWindowAncestor(this),
                currentSelectedThuongPhat,
                this::loadTableData
        );
        dlg.setVisible(true);
    }

    private void onDeleteSelectedThuongPhat() {
        if (currentSelectedThuongPhat == null) {
            int row = tableThuongPhat.getSelectedRow();
            if (row >= 0 && row < currentDisplayedList.size()) {
                currentSelectedThuongPhat = currentDisplayedList.get(row);
            }
        }
        if (currentSelectedThuongPhat == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu thưởng phạt để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phiếu thưởng phạt [" + currentSelectedThuongPhat.getSoPhieu() + "] không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            thuongPhatDAO.delete(currentSelectedThuongPhat.getId());
            loadTableData();
            JOptionPane.showMessageDialog(this, "Đã xóa phiếu thưởng phạt thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onExportExcel() {
        ExcelExporter.exportTable(tableThuongPhat, "Danh_Muc_Thuong_Phat", "Thưởng phạt", this);
    }

    private void onPrint() {
        if (tableThuongPhat.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu thưởng phạt để in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean complete = tableThuongPhat.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "In danh sách hoàn tất!", "In ấn", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTotalSummary() {
        double totalThuong = currentDisplayedList.stream().mapToDouble(ThuongPhat::getTienThuong).sum();
        double totalPhat = currentDisplayedList.stream().mapToDouble(ThuongPhat::getTienPhat).sum();
        int totalCount = currentDisplayedList.size();

        JOptionPane.showMessageDialog(
                this,
                "<html><b>TỔNG KẾT THƯỞNG PHẠT:</b><br>" +
                        "- Tổng số phiếu: <b>" + totalCount + "</b><br>" +
                        "- Tổng tiền thưởng: <b style='color:green; font-size:13px;'>" + CurrencyUtil.format(totalThuong) + " đ</b><br>" +
                        "- Tổng tiền phạt: <b style='color:red; font-size:13px;'>" + CurrencyUtil.format(totalPhat) + " đ</b></html>",
                "Tổng kết thưởng phạt",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAnalyticsSummary() {
        Map<String, Double> byEmployeeThuong = new HashMap<>();
        Map<String, Double> byEmployeePhat = new HashMap<>();

        for (ThuongPhat tp : currentDisplayedList) {
            String name = tp.getTenNhanVien() != null ? tp.getTenNhanVien() : "Khác";
            byEmployeeThuong.put(name, byEmployeeThuong.getOrDefault(name, 0.0) + tp.getTienThuong());
            byEmployeePhat.put(name, byEmployeePhat.getOrDefault(name, 0.0) + tp.getTienPhat());
        }

        StringBuilder sb = new StringBuilder("<html><b>PHÂN TÍCH THEO NHÂN VIÊN:</b><br><br>");
        Set<String> allNames = new HashSet<>();
        allNames.addAll(byEmployeeThuong.keySet());
        allNames.addAll(byEmployeePhat.keySet());

        for (String name : allNames) {
            double th = byEmployeeThuong.getOrDefault(name, 0.0);
            double ph = byEmployeePhat.getOrDefault(name, 0.0);
            sb.append("• <b>").append(name).append("</b>: Thưởng <b>").append(CurrencyUtil.format(th)).append(" đ</b> | Phạt <b>")
              .append(CurrencyUtil.format(ph)).append(" đ</b><br>");
        }
        sb.append("</html>");

        JOptionPane.showMessageDialog(this, sb.toString(), "Phân tích thưởng phạt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void autoResizeColumnWidths() {
        for (int column = 0; column < tableThuongPhat.getColumnCount(); column++) {
            TableColumn tableColumn = tableThuongPhat.getColumnModel().getColumn(column);
            int preferredWidth = 40;

            TableCellRenderer headerRenderer = tableThuongPhat.getTableHeader().getDefaultRenderer();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        tableThuongPhat, tableColumn.getHeaderValue(), false, false, 0, column);
                if (headerComp != null) {
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
                }
            }

            int rowCount = Math.min(tableThuongPhat.getRowCount(), 100);
            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer cellRenderer = tableThuongPhat.getCellRenderer(row, column);
                if (cellRenderer != null) {
                    Component c = tableThuongPhat.prepareRenderer(cellRenderer, row, column);
                    if (c != null) {
                        int width = c.getPreferredSize().width + tableThuongPhat.getIntercellSpacing().width + 16;
                        preferredWidth = Math.max(preferredWidth, width);
                    }
                }
            }

            tableColumn.setPreferredWidth(Math.min(preferredWidth, 350));
        }
        tableThuongPhat.getTableHeader().repaint();
    }

    private void showTablePopup(MouseEvent e) {
        int row = tableThuongPhat.rowAtPoint(e.getPoint());
        if (row >= 0 && row < tableThuongPhat.getRowCount()) {
            tableThuongPhat.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNewThuongPhat());
        popup.add(miAdd);

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        miEdit.addActionListener(ev -> onEditSelectedThuongPhat());
        popup.add(miEdit);

        JMenuItem miDel = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 16, 16));
        miDel.addActionListener(ev -> onDeleteSelectedThuongPhat());
        popup.add(miDel);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miExcel = new JMenuItem("Xuất Excel", IconHelper.getIcon("export-excel.png", 16, 16));
        miExcel.addActionListener(ev -> onExportExcel());
        popup.add(miExcel);

        JMenuItem miPrint = new JMenuItem("In danh sách", IconHelper.getIcon("printer.png", 16, 16));
        miPrint.addActionListener(ev -> onPrint());
        popup.add(miPrint);

        popup.addSeparator();

        JMenuItem miRefresh = new JMenuItem("Làm mới (F5)", IconHelper.getIcon("Refresh.png", 16, 16));
        miRefresh.addActionListener(ev -> loadTableData());
        popup.add(miRefresh);

        popup.show(tableThuongPhat, e.getX(), e.getY());
    }

    private void setupShortcuts() {
        // F3: Lọc
        registerKeyboardAction(e -> {
            txtSearch.requestFocusInWindow();
            txtSearch.selectAll();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Insert: Thêm mới
        registerKeyboardAction(e -> onAddNewThuongPhat(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F4: Chỉnh sửa
        registerKeyboardAction(e -> onEditSelectedThuongPhat(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Delete: Xóa
        registerKeyboardAction(e -> onDeleteSelectedThuongPhat(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F5: Tải lại
        registerKeyboardAction(e -> {
            loadTreeData();
            loadTableData();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
