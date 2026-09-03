package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.NhomNhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DanhMucNhaCungCapPanel extends JPanel {

    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final NhomNhaCungCapDAO nhomNhaCungCapDAO = new NhomNhaCungCapDAO();

    // Left components
    private JTree treeNhom;
    private DefaultTreeModel treeModel;

    // Right top toolbar
    private JTextField txtLoc;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnAddExcel;
    private JButton btnExportExcel;
    private JButton btnPrint;
    private JButton btnSummary;

    // Master table
    private JTable tableMaster;
    private DefaultTableModel masterModel;
    private final List<NhaCungCap> allSuppliers = new ArrayList<>();
    private final List<NhaCungCap> filteredSuppliers = new ArrayList<>();
    private NhaCungCap selectedSupplier = null;

    // Bottom tabbed pane
    private JTabbedPane bottomTabs;
    private JLabel lblAuditKhoiTao;
    private JLabel lblAuditNguoiTao;
    private JLabel lblAuditSuaDoi;
    private JLabel lblAuditNguoiSua;

    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
    private final DecimalFormat dfStt = new DecimalFormat("00");

    public JTable getTableMaster() { return tableMaster; }
    public DefaultTableModel getMasterModel() { return masterModel; }
    public List<NhaCungCap> getAllSuppliers() { return allSuppliers; }
    public List<NhaCungCap> getFilteredSuppliers() { return filteredSuppliers; }

    public DanhMucNhaCungCapPanel() {
        setLayout(new BorderLayout(0, 0));
        initComponents();
        loadTreeData();
        loadData();
        setupKeyBindings();
    }

    private void initComponents() {
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(180);
        splitMain.setDividerSize(4);
        splitMain.setBorder(null);

        // ==========================================
        // 1. LEFT PANEL: Nhóm nhà cung cấp
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(new Color(245, 248, 253));
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(195, 215, 238)));

        // Header "Nhóm nhà cung cấp"
        JPanel pnlLeftHeader = new JPanel(new BorderLayout());
        pnlLeftHeader.setBackground(new Color(230, 240, 252));
        pnlLeftHeader.setBorder(new EmptyBorder(4, 8, 4, 6));

        JLabel lblLeftTitle = new JLabel("Nhóm nhà cung cấp");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeftTitle.setForeground(new Color(30, 60, 110));

        JButton btnConfigNhom = new JButton(IconHelper.getIcon("table_tab.png", 14, 14));
        btnConfigNhom.setPreferredSize(new Dimension(20, 20));
        btnConfigNhom.setFocusPainted(false);
        btnConfigNhom.setBorderPainted(false);
        btnConfigNhom.setContentAreaFilled(false);
        btnConfigNhom.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        pnlLeftHeader.add(lblLeftTitle, BorderLayout.WEST);
        pnlLeftHeader.add(btnConfigNhom, BorderLayout.EAST);
        leftPanel.add(pnlLeftHeader, BorderLayout.NORTH);

        // Mini action toolbar (+, Trash, Refresh, Layout)
        JPanel pnlMiniToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        pnlMiniToolbar.setOpaque(false);
        pnlMiniToolbar.setBorder(new EmptyBorder(2, 4, 2, 4));

        JButton btnMiniAdd = createMiniIconButton("plus.png");
        btnMiniAdd.addActionListener(e -> promptAddNewGroup());

        JButton btnMiniTrash = createMiniIconButton("RecycleBin.png");
        btnMiniTrash.addActionListener(e -> selectTrashNode());

        JButton btnMiniRefresh = createMiniIconButton("Refresh.png");
        btnMiniRefresh.addActionListener(e -> {
            loadTreeData();
            loadData();
        });

        JButton btnMiniLayout = createMiniIconButton("edit-column.png");

        pnlMiniToolbar.add(btnMiniAdd);
        pnlMiniToolbar.add(btnMiniTrash);
        pnlMiniToolbar.add(btnMiniRefresh);
        pnlMiniToolbar.add(btnMiniLayout);

        // Group Tree
        treeNhom = new JTree();
        treeNhom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhom.setRowHeight(22);
        treeNhom.setRootVisible(true);
        treeNhom.setShowsRootHandles(true);

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof NhomNhaCungCap) {
                        NhomNhaCungCap n = (NhomNhaCungCap) userObj;
                        setText(n.getName());
                        setIcon(IconHelper.getIcon("tag.png", 16, 16));
                    } else if (userObj instanceof String) {
                        String s = (String) userObj;
                        setText(s);
                        if ("Tất cả".equalsIgnoreCase(s)) {
                            setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        } else if ("Thùng rác".equalsIgnoreCase(s)) {
                            setIcon(IconHelper.getIcon("RecycleBin.png", 16, 16));
                        } else {
                            setIcon(IconHelper.getIcon("bullet_yellow.png", 16, 16));
                        }
                    }
                }
                return this;
            }
        };
        treeNhom.setCellRenderer(treeRenderer);
        treeNhom.addTreeSelectionListener(e -> loadData());

        JPanel pnlTreeHolder = new JPanel(new BorderLayout());
        pnlTreeHolder.setOpaque(false);
        pnlTreeHolder.add(pnlMiniToolbar, BorderLayout.NORTH);
        pnlTreeHolder.add(new JScrollPane(treeNhom), BorderLayout.CENTER);

        leftPanel.add(pnlTreeHolder, BorderLayout.CENTER);
        splitMain.setLeftComponent(leftPanel);

        // ==========================================
        // 2. RIGHT PANEL: Toolbar, Master Table, Bottom Tabs
        // ==========================================
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(Color.WHITE);

        // Right Top Toolbar
        JPanel pnlRightTop = new JPanel(new BorderLayout(0, 0));
        pnlRightTop.setBackground(new Color(238, 244, 252));
        pnlRightTop.setBorder(new MatteBorder(0, 0, 1, 0, new Color(195, 215, 238)));

        // Tab indicator "Nhà cung cấp"
        JPanel pnlTabTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTabTitle.setBackground(Color.WHITE);
        pnlTabTitle.setBorder(new MatteBorder(1, 1, 0, 1, new Color(195, 215, 238)));
        JLabel lblTabTitle = new JLabel("Nhà cung cấp");
        lblTabTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTabTitle.setForeground(new Color(20, 60, 120));
        pnlTabTitle.add(lblTabTitle);

        // Toolbar Buttons
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        pnlToolbar.setOpaque(false);

        pnlToolbar.add(new JLabel("Lọc (F3):"));
        txtLoc = new JTextField(12);
        txtLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLoc.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        pnlToolbar.add(txtLoc);

        btnAdd = createToolbarButton("Thêm mới (Insert)", "plus.png");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setForeground(new Color(0, 120, 50));
        btnAdd.addActionListener(e -> openAddDialog());

        btnEdit = createToolbarButton("Chỉnh sửa (F4)", "pencil.png");
        btnEdit.addActionListener(e -> openEditDialog());

        btnDelete = createToolbarButton("Xóa (Del)", "cross.png");
        btnDelete.addActionListener(e -> deleteSelectedSupplier());

        btnAddExcel = createToolbarButton("Thêm excel", "page_excel.png");
        btnAddExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng thêm từ Excel đang chuẩn bị!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        btnExportExcel = createToolbarButton("Xuất excel", "export-excel.png");
        btnExportExcel.addActionListener(e -> ExcelExporter.exportTable(tableMaster, "Danh_Muc_Nha_Cung_Cap", "Nhà cung cấp", this));

        btnPrint = createToolbarButton("In", "printer.png");
        btnPrint.addActionListener(e -> printTable());

        btnSummary = createToolbarButton("∑ Tổng", "calculator.png");
        btnSummary.addActionListener(e -> showSummaryDialog());

        pnlToolbar.add(btnAdd);
        pnlToolbar.add(btnEdit);
        pnlToolbar.add(btnDelete);
        pnlToolbar.add(btnAddExcel);
        pnlToolbar.add(btnExportExcel);
        pnlToolbar.add(btnPrint);
        pnlToolbar.add(btnSummary);

        pnlRightTop.add(pnlTabTitle, BorderLayout.WEST);
        pnlRightTop.add(pnlToolbar, BorderLayout.CENTER);
        rightPanel.add(pnlRightTop, BorderLayout.NORTH);

        // Center Split: Master Table on top, Bottom Tabs below
        JSplitPane splitCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitCenter.setResizeWeight(0.65);
        splitCenter.setDividerSize(4);
        splitCenter.setBorder(null);

        // Master Table Panel
        JPanel pnlMasterTable = new JPanel(new BorderLayout());
        pnlMasterTable.setBackground(Color.WHITE);

        String[] cols = {
                "", "Mã nhà cung cấp", "Tên nhà cung cấp", "Địa chỉ",
                "Điện thoại", "Email", "Website", "Ghi chú", "Nhóm nhà cung cấp"
        };
        masterModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMaster = new JTable(masterModel);
        tableMaster.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMaster.setRowHeight(22);
        tableMaster.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableMaster.setShowGrid(true);
        tableMaster.setGridColor(new Color(230, 238, 248));
        tableMaster.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMaster.setSelectionBackground(new Color(214, 238, 218)); // Nền xanh nhạt cho các ô dữ liệu
        tableMaster.setSelectionForeground(Color.BLACK);

        JTableHeader masterHeader = tableMaster.getTableHeader();
        masterHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        masterHeader.setBackground(new Color(245, 248, 253));
        masterHeader.setForeground(new Color(40, 50, 70));
        masterHeader.setPreferredSize(new Dimension(100, 24));

        int[] widths = {45, 110, 180, 150, 90, 90, 90, 90, 110};
        for (int i = 0; i < widths.length && i < tableMaster.getColumnCount(); i++) {
            tableMaster.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom Renderer cho cột 0: STT với chỉ báo dòng được chọn màu cam như Hình 1
        tableMaster.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    setBackground(new Color(195, 95, 45)); // Nền màu cam đậm (Image 1)
                    setForeground(Color.WHITE);
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setText("▶ " + dfStt.format(row + 1));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(70, 70, 70));
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    setText(dfStt.format(row + 1));
                }
                return this;
            }
        });

        tableMaster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });

        tableMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableMaster.getSelectedRow();
                if (row >= 0 && row < filteredSuppliers.size()) {
                    selectedSupplier = filteredSuppliers.get(row);
                    onSupplierSelected(selectedSupplier);
                }
            }
        });

        setupMasterContextMenu();
        pnlMasterTable.add(new JScrollPane(tableMaster), BorderLayout.CENTER);
        splitCenter.setTopComponent(pnlMasterTable);

        // Bottom JTabbedPane (11 Tabs matching Image 1)
        bottomTabs = new JTabbedPane();
        bottomTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomTabs.setPreferredSize(new Dimension(100, 150));

        // Tab 1: Thông tin (Audit Info)
        bottomTabs.addTab("Thông tin", createAuditInfoTab());
        // 10 Tabs khác
        bottomTabs.addTab("Đặt hàng nhà cung cấp", createEmptyTab("Chưa có đơn đặt hàng cho nhà cung cấp này"));
        bottomTabs.addTab("Đơn hàng", createEmptyTab("Chưa có đơn hàng"));
        bottomTabs.addTab("Phiếu nhập kho", createEmptyTab("Chưa có phiếu nhập kho"));
        bottomTabs.addTab("Phiếu xuất kho", createEmptyTab("Chưa có phiếu xuất kho"));
        bottomTabs.addTab("Phiếu chuyển kho", createEmptyTab("Chưa có phiếu chuyển kho"));
        bottomTabs.addTab("Phiếu kiểm kê", createEmptyTab("Chưa có phiếu kiểm kê"));
        bottomTabs.addTab("Sản xuất", createEmptyTab("Chưa có thông tin sản xuất"));
        bottomTabs.addTab("Phiếu thu", createEmptyTab("Chưa có phiếu thu"));
        bottomTabs.addTab("Phiếu chi", createEmptyTab("Chưa có phiếu chi"));
        bottomTabs.addTab("Phiếu thu công nợ", createEmptyTab("Chưa có phiếu thu công nợ"));

        splitCenter.setBottomComponent(bottomTabs);
        rightPanel.add(splitCenter, BorderLayout.CENTER);

        splitMain.setRightComponent(rightPanel);
        add(splitMain, BorderLayout.CENTER);
    }

    private JPanel createAuditInfoTab() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        pnl.setBackground(new Color(248, 251, 255));

        JPanel pnlBox1 = new JPanel(new GridLayout(2, 1, 2, 4));
        pnlBox1.setOpaque(false);
        lblAuditKhoiTao = new JLabel("Khởi tạo: 15/04/2015 10:25 AM");
        lblAuditKhoiTao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAuditNguoiTao = new JLabel("Khởi tạo bởi: Administrator");
        lblAuditNguoiTao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBox1.add(lblAuditKhoiTao);
        pnlBox1.add(lblAuditNguoiTao);

        JPanel pnlBox2 = new JPanel(new GridLayout(2, 1, 2, 4));
        pnlBox2.setOpaque(false);
        lblAuditSuaDoi = new JLabel("Sửa đổi gần nhất: --/--/---- --:--");
        lblAuditSuaDoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAuditNguoiSua = new JLabel("Sửa đổi bởi: --");
        lblAuditNguoiSua.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBox2.add(lblAuditSuaDoi);
        pnlBox2.add(lblAuditNguoiSua);

        pnl.add(pnlBox1);
        pnl.add(new JSeparator(SwingConstants.VERTICAL));
        pnl.add(pnlBox2);
        return pnl;
    }

    private JPanel createEmptyTab(String msg) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl.setForeground(Color.GRAY);
        pnl.add(lbl);
        return pnl;
    }

    private JButton createToolbarButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMiniIconButton(String iconName) {
        JButton btn = new JButton(IconHelper.getIcon(iconName, 14, 14));
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(190, 210, 235), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupMasterContextMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Thêm mới (Insert)", IconHelper.getIcon("plus.png", 14, 14));
        miAdd.addActionListener(e -> openAddDialog());

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 14, 14));
        miEdit.addActionListener(e -> openEditDialog());

        JMenuItem miDelete = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 14, 14));
        miDelete.addActionListener(e -> deleteSelectedSupplier());

        JMenuItem miExcel = new JMenuItem("Xuất excel", IconHelper.getIcon("export-excel.png", 14, 14));
        miExcel.addActionListener(e -> ExcelExporter.exportTable(tableMaster, "Danh_Muc_Nha_Cung_Cap", "Nhà cung cấp", this));

        JMenuItem miPrint = new JMenuItem("In", IconHelper.getIcon("printer.png", 14, 14));
        miPrint.addActionListener(e -> printTable());

        popup.add(miAdd);
        popup.add(miEdit);
        popup.add(miDelete);
        popup.addSeparator();
        popup.add(miExcel);
        popup.add(miPrint);

        tableMaster.setComponentPopupMenu(popup);
    }

    private void loadTreeData() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        DefaultMutableTreeNode nodeUnset = new DefaultMutableTreeNode("Chưa thiết lập");
        root.add(nodeUnset);

        List<NhomNhaCungCap> nhomList = nhomNhaCungCapDAO.getAll();
        for (NhomNhaCungCap n : nhomList) {
            root.add(new DefaultMutableTreeNode(n));
        }

        DefaultMutableTreeNode nodeTrash = new DefaultMutableTreeNode("Thùng rác");
        root.add(nodeTrash);

        treeModel = new DefaultTreeModel(root);
        treeNhom.setModel(treeModel);
        treeNhom.setSelectionRow(0);
    }

    private void selectTrashNode() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            if ("Thùng rác".equalsIgnoreCase(String.valueOf(child.getUserObject()))) {
                treeNhom.setSelectionPath(new javax.swing.tree.TreePath(child.getPath()));
                break;
            }
        }
    }

    private void promptAddNewGroup() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên nhóm nhà cung cấp mới:", "Thêm nhóm nhà cung cấp", JOptionPane.QUESTION_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            NhomNhaCungCap n = new NhomNhaCungCap();
            n.setName(name.trim());
            if (nhomNhaCungCapDAO.insert(n)) {
                loadTreeData();
                JOptionPane.showMessageDialog(this, "Thêm nhóm nhà cung cấp thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể thêm nhóm nhà cung cấp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadData() {
        allSuppliers.clear();

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeNhom.getLastSelectedPathComponent();
        if (selectedNode == null) {
            allSuppliers.addAll(nhaCungCapDAO.getAll());
        } else {
            Object obj = selectedNode.getUserObject();
            if (obj instanceof NhomNhaCungCap) {
                allSuppliers.addAll(nhaCungCapDAO.getByNhom(((NhomNhaCungCap) obj).getId()));
            } else if (obj instanceof String) {
                String s = (String) obj;
                if ("Tất cả".equalsIgnoreCase(s)) {
                    allSuppliers.addAll(nhaCungCapDAO.getAll());
                } else if ("Chưa thiết lập".equalsIgnoreCase(s)) {
                    allSuppliers.addAll(nhaCungCapDAO.getByNhom("UNSET"));
                } else if ("Thùng rác".equalsIgnoreCase(s)) {
                    allSuppliers.addAll(nhaCungCapDAO.getTrash());
                } else {
                    allSuppliers.addAll(nhaCungCapDAO.getAll());
                }
            } else {
                allSuppliers.addAll(nhaCungCapDAO.getAll());
            }
        }

        applyFilter();
    }

    private void applyFilter() {
        String kw = txtLoc.getText().trim().toLowerCase();
        filteredSuppliers.clear();

        for (NhaCungCap n : allSuppliers) {
            if (kw.isEmpty()) {
                filteredSuppliers.add(n);
            } else {
                String ma = n.getMaNhaCungCap() != null ? n.getMaNhaCungCap().toLowerCase() : "";
                String ten = n.getName() != null ? n.getName().toLowerCase() : "";
                String dc = n.getDiaChi() != null ? n.getDiaChi().toLowerCase() : "";
                String dt = n.getDienThoai() != null ? n.getDienThoai().toLowerCase() : "";
                String email = n.getEmail() != null ? n.getEmail().toLowerCase() : "";
                if (ma.contains(kw) || ten.contains(kw) || dc.contains(kw) || dt.contains(kw) || email.contains(kw)) {
                    filteredSuppliers.add(n);
                }
            }
        }

        populateMasterTable();
    }

    private void populateMasterTable() {
        masterModel.setRowCount(0);
        int selIndex = -1;

        for (int r = 0; r < filteredSuppliers.size(); r++) {
            NhaCungCap n = filteredSuppliers.get(r);
            Vector<Object> rowData = new Vector<>();
            rowData.add(String.valueOf(r + 1));
            rowData.add(n.getMaNhaCungCap() != null ? n.getMaNhaCungCap() : "");
            rowData.add(n.getName() != null ? n.getName() : "");
            rowData.add(n.getDiaChi() != null ? n.getDiaChi() : "");
            rowData.add(n.getDienThoai() != null ? n.getDienThoai() : "");
            rowData.add(n.getEmail() != null ? n.getEmail() : "");
            rowData.add(n.getWebsite() != null ? n.getWebsite() : "");
            rowData.add(n.getNote() != null ? n.getNote() : "");
            rowData.add(n.getTenNhomNhaCungCap() != null ? n.getTenNhomNhaCungCap() : "");

            masterModel.addRow(rowData);

            if (selectedSupplier != null && selectedSupplier.getId() != null && selectedSupplier.getId().equals(n.getId())) {
                selIndex = r;
            }
        }

        if (selIndex >= 0 && selIndex < tableMaster.getRowCount()) {
            tableMaster.setRowSelectionInterval(selIndex, selIndex);
        } else if (tableMaster.getRowCount() > 0) {
            tableMaster.setRowSelectionInterval(0, 0);
        } else {
            selectedSupplier = null;
            onSupplierSelected(null);
        }
    }

    private void onSupplierSelected(NhaCungCap ncc) {
        if (ncc == null) {
            lblAuditKhoiTao.setText("Khởi tạo: --/--/---- --:--");
            lblAuditNguoiTao.setText("Khởi tạo bởi: --");
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
            return;
        }

        String timeCreatedStr = ncc.getTimeCreated() != null ? sdfDateTime.format(ncc.getTimeCreated()) : "--/--/---- --:--";
        lblAuditKhoiTao.setText("Khởi tạo: " + timeCreatedStr);
        lblAuditNguoiTao.setText("Khởi tạo bởi: " + (ncc.getUserCreatedName() != null ? ncc.getUserCreatedName() : "Administrator"));

        if (ncc.getTimeModified() != null) {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: " + sdfDateTime.format(ncc.getTimeModified()));
            lblAuditNguoiSua.setText("Sửa đổi bởi: " + (ncc.getUserModifiedName() != null ? ncc.getUserModifiedName() : "--"));
        } else {
            lblAuditSuaDoi.setText("Sửa đổi gần nhất: --/--/---- --:--");
            lblAuditNguoiSua.setText("Sửa đổi bởi: --");
        }
    }

    private void openAddDialog() {
        NhaCungCapDialog dlg = new NhaCungCapDialog(SwingUtilities.getWindowAncestor(this), () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog() {
        if (selectedSupplier == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần chỉnh sửa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        NhaCungCap full = nhaCungCapDAO.getById(selectedSupplier.getId());
        NhaCungCapDialog dlg = new NhaCungCapDialog(SwingUtilities.getWindowAncestor(this), full != null ? full : selectedSupplier, () -> {
            loadData();
        });
        dlg.setVisible(true);
    }

    private void deleteSelectedSupplier() {
        if (selectedSupplier == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhà cung cấp [" + selectedSupplier.getName() + "] không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (nhaCungCapDAO.delete(selectedSupplier.getId())) {
                JOptionPane.showMessageDialog(this, "Đã xóa nhà cung cấp thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa nhà cung cấp này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printTable() {
        try {
            tableMaster.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("DANH MỤC NHÀ CUNG CẤP"),
                    new java.text.MessageFormat("Trang {0}"));
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSummaryDialog() {
        String msg = String.format("Tổng số nhà cung cấp đang hiển thị: %d", filteredSuppliers.size());
        JOptionPane.showMessageDialog(this, msg, "Tổng kết", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupKeyBindings() {
        registerKeyboardAction(e -> openAddDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> openEditDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> deleteSelectedSupplier(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> txtLoc.requestFocus(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(e -> loadData(), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
