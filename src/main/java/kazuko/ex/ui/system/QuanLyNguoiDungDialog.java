package kazuko.ex.ui.system;

import kazuko.ex.dao.*;
import kazuko.ex.model.system.*;
import kazuko.ex.util.AppColors;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class QuanLyNguoiDungDialog extends JDialog {
    private final GroupUserDAO groupUserDAO = new GroupUserDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FunctionDAO functionDAO = new FunctionDAO();
    private final GroupRoleDAO groupRoleDAO = new GroupRoleDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final ReportRoleDAO reportRoleDAO = new ReportRoleDAO();

    // Data Cache
    private List<SGroupUser> groupList = new ArrayList<>();
    private List<SUser> currentUsersList = new ArrayList<>();
    private SGroupUser selectedGroup = null;
    private SUser selectedUser = null;

    private List<SFunction> allFunctions = new ArrayList<>();
    private Map<String, SGroupRole> currentGroupRoles = new HashMap<>();

    private List<SReport> allReports = new ArrayList<>();
    private Map<String, SReportRole> currentReportRoles = new HashMap<>();

    // Left Panel Components
    private JTree treeGroups;
    private DefaultTreeModel treeModelGroups;
    private DefaultMutableTreeNode rootNodeGroups;

    private JTable tableUsers;
    private UserTableModel modelUsers;

    // Right Panel - Tab 1 Components (Quyền sử dụng)
    private JTree treeFuncCategories;
    private DefaultTreeModel treeModelFunc;
    private DefaultMutableTreeNode rootNodeFunc;
    private JTextField txtFuncFilter;
    private JCheckBox chkFuncLockAll;
    private JCheckBox chkFuncAllowAll;
    private JTable tableFuncRoles;
    private FunctionRoleTableModel modelFuncRoles;
    private String selectedFuncCategory = null;

    // Right Panel - Tab 2 Components (Quyền xem báo cáo)
    private JTree treeReports;
    private DefaultTreeModel treeModelReports;
    private DefaultMutableTreeNode rootNodeReports;
    private JTextField txtReportFilter;
    private JCheckBox chkReportLockAll;
    private JCheckBox chkReportAllowAll;
    private JTable tableReportRoles;
    private ReportRoleTableModel modelReportRoles;
    private SReport selectedReportNode = null;

    public QuanLyNguoiDungDialog(Frame parent) {
        super(parent, "Quản lý người sử dụng", true);
        initDialog(parent);
    }

    public QuanLyNguoiDungDialog(Dialog parent) {
        super(parent, "Quản lý người sử dụng", true);
        initDialog(parent);
    }

    private void initDialog(Window parent) {
        setSize(1100, 680);
        setMinimumSize(new Dimension(950, 580));
        setLocationRelativeTo(parent);
        initComponents();
        loadAllData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(310);
        mainSplit.setDividerSize(5);

        // Left Panel: Groups (Top) + Users (Bottom)
        JPanel leftPanel = createLeftPanel();
        mainSplit.setLeftComponent(leftPanel);

        // Right Panel: Tabbed Pane (Quyền sử dụng + Quyền xem báo cáo)
        JPanel rightPanel = createRightPanel();
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. LEFT PANEL (Nhóm người dùng & Danh sách tài khoản)
    // =========================================================================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setDividerLocation(260);
        leftSplit.setDividerSize(5);

        // 1.1 Top: Nhóm người dùng
        JPanel pnlGroups = new JPanel(new BorderLayout());
        pnlGroups.setBorder(BorderFactory.createTitledBorder(
                new MatteBorder(1, 1, 1, 1, new Color(190, 210, 235)), "Nhóm người dùng",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(40, 60, 90)));

        JPanel tbGroups = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        tbGroups.setBackground(new Color(225, 235, 248));
        tbGroups.setBorder(new MatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JButton btnAddGroup = createToolBtn("+ Thêm nhóm", "plus.png", new Color(0, 120, 50));
        JButton btnEditGroup = createToolBtn("Sửa", "pencil.png", Color.BLACK);
        JButton btnDeleteGroup = createToolBtn("Xóa", "cross.png", new Color(180, 20, 20));

        btnAddGroup.addActionListener(e -> onAddGroup());
        btnEditGroup.addActionListener(e -> onEditGroup());
        btnDeleteGroup.addActionListener(e -> onDeleteGroup());

        tbGroups.add(btnAddGroup);
        tbGroups.add(btnEditGroup);
        tbGroups.add(btnDeleteGroup);
        pnlGroups.add(tbGroups, BorderLayout.NORTH);

        rootNodeGroups = new DefaultMutableTreeNode("Tất cả");
        treeModelGroups = new DefaultTreeModel(rootNodeGroups);
        treeGroups = new JTree(treeModelGroups);
        treeGroups.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeGroups.setRowHeight(22);
        treeGroups.setCellRenderer(new GroupTreeCellRenderer());
        treeGroups.addTreeSelectionListener(e -> onGroupSelected());

        pnlGroups.add(new JScrollPane(treeGroups), BorderLayout.CENTER);
        leftSplit.setTopComponent(pnlGroups);

        // 1.2 Bottom: Danh sách tài khoản
        JPanel pnlUsers = new JPanel(new BorderLayout());
        pnlUsers.setBorder(BorderFactory.createTitledBorder(
                new MatteBorder(1, 1, 1, 1, new Color(190, 210, 235)), "Danh sách tài khoản",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(40, 60, 90)));

        JPanel tbUsers = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        tbUsers.setBackground(new Color(225, 235, 248));
        tbUsers.setBorder(new MatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JButton btnAddUser = createToolBtn("+ Thêm tài khoản", "plus.png", new Color(0, 120, 50));
        JButton btnEditUser = createToolBtn("Sửa", "pencil.png", Color.BLACK);
        JButton btnDeleteUser = createToolBtn("Xóa", "cross.png", new Color(180, 20, 20));

        btnAddUser.addActionListener(e -> onAddUser());
        btnEditUser.addActionListener(e -> onEditUser());
        btnDeleteUser.addActionListener(e -> onDeleteUser());

        tbUsers.add(btnAddUser);
        tbUsers.add(btnEditUser);
        tbUsers.add(btnDeleteUser);
        pnlUsers.add(tbUsers, BorderLayout.NORTH);

        modelUsers = new UserTableModel();
        tableUsers = new JTable(modelUsers);
        tableUsers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableUsers.setRowHeight(22);
        tableUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableUsers.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableUsers.getTableHeader().setBackground(new Color(235, 240, 248));
        tableUsers.setSelectionBackground(new Color(184, 92, 36)); // Brown highlight #B85C24
        tableUsers.setSelectionForeground(Color.WHITE);

        tableUsers.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableUsers.getColumnModel().getColumn(0).setMaxWidth(55);
        tableUsers.getColumnModel().getColumn(1).setPreferredWidth(200);

        tableUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditUser();
                }
            }
        });

        pnlUsers.add(new JScrollPane(tableUsers), BorderLayout.CENTER);

        // Admin notice footer
        JPanel footerNotice = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        footerNotice.setBackground(new Color(245, 247, 250));
        footerNotice.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));
        JLabel lblNotice = new JLabel("Chú ý: Tài khoản hệ thống Admin không được hiển thị trong danh sách");
        lblNotice.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNotice.setForeground(new Color(90, 100, 115));
        lblNotice.setIcon(IconHelper.getIcon("property.png", 14, 14));
        footerNotice.add(lblNotice);
        pnlUsers.add(footerNotice, BorderLayout.SOUTH);

        leftSplit.setBottomComponent(pnlUsers);

        panel.add(leftSplit, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 2. RIGHT PANEL (TabbedPane: Quyền sử dụng & Quyền xem báo cáo)
    // =========================================================================
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: Quyền sử dụng
        JPanel tabFunc = createFunctionPermissionTab();
        tabbedPane.addTab("Quyền sử dụng", IconHelper.getIcon("property.png", 16, 16), tabFunc);

        // Tab 2: Quyền xem báo cáo
        JPanel tabReport = createReportPermissionTab();
        tabbedPane.addTab("Quyền xem báo cáo", IconHelper.getIcon("Report.png", 16, 16), tabReport);

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 1: Quyền sử dụng ---
    private JPanel createFunctionPermissionTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(180);
        split.setDividerSize(4);

        // Left Tree: 11 Categories
        rootNodeFunc = new DefaultMutableTreeNode("Tất cả");
        treeModelFunc = new DefaultTreeModel(rootNodeFunc);
        treeFuncCategories = new JTree(treeModelFunc);
        treeFuncCategories.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeFuncCategories.setRowHeight(22);
        treeFuncCategories.addTreeSelectionListener(e -> onFuncCategorySelected());

        JPanel pnlTree = new JPanel(new BorderLayout());
        pnlTree.setBorder(new MatteBorder(0, 0, 0, 1, new Color(190, 210, 235)));
        pnlTree.add(new JScrollPane(treeFuncCategories), BorderLayout.CENTER);
        split.setLeftComponent(pnlTree);

        // Right: Filter bar + Table
        JPanel pnlContent = new JPanel(new BorderLayout());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(new Color(240, 244, 250));
        filterBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JLabel lblFilter = new JLabel("Lọc:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtFuncFilter = new JTextField(15);
        txtFuncFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFuncFilter.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterFunctionTable(); }
            public void removeUpdate(DocumentEvent e) { filterFunctionTable(); }
            public void changedUpdate(DocumentEvent e) { filterFunctionTable(); }
        });

        chkFuncLockAll = new JCheckBox("Khóa tất cả");
        chkFuncLockAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkFuncLockAll.setOpaque(false);
        chkFuncLockAll.addActionListener(e -> onFuncLockAllToggle());

        chkFuncAllowAll = new JCheckBox("Cho phép tất cả");
        chkFuncAllowAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkFuncAllowAll.setOpaque(false);
        chkFuncAllowAll.addActionListener(e -> onFuncAllowAllToggle());

        filterBar.add(lblFilter);
        filterBar.add(txtFuncFilter);
        filterBar.add(chkFuncLockAll);
        filterBar.add(chkFuncAllowAll);
        pnlContent.add(filterBar, BorderLayout.NORTH);

        modelFuncRoles = new FunctionRoleTableModel();
        tableFuncRoles = new JTable(modelFuncRoles);
        tableFuncRoles.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableFuncRoles.setRowHeight(23);
        tableFuncRoles.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableFuncRoles.getTableHeader().setBackground(new Color(235, 240, 248));

        // Column settings
        tableFuncRoles.getColumnModel().getColumn(0).setPreferredWidth(230); // Chức năng
        for (int i = 1; i <= 6; i++) {
            tableFuncRoles.getColumnModel().getColumn(i).setPreferredWidth(60);
            tableFuncRoles.getColumnModel().getColumn(i).setMaxWidth(75);
        }

        tableFuncRoles.setDefaultRenderer(Boolean.class, new FunctionRoleCheckBoxRenderer());

        pnlContent.add(new JScrollPane(tableFuncRoles), BorderLayout.CENTER);
        split.setRightComponent(pnlContent);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 2: Quyền xem báo cáo ---
    private JPanel createReportPermissionTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(200);
        split.setDividerSize(4);

        // Left Tree: Hierarchical Reports
        rootNodeReports = new DefaultMutableTreeNode("Tất cả");
        treeModelReports = new DefaultTreeModel(rootNodeReports);
        treeReports = new JTree(treeModelReports);
        treeReports.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeReports.setRowHeight(22);
        treeReports.addTreeSelectionListener(e -> onReportCategorySelected());

        JPanel pnlTree = new JPanel(new BorderLayout());
        pnlTree.setBorder(new MatteBorder(0, 0, 0, 1, new Color(190, 210, 235)));
        pnlTree.add(new JScrollPane(treeReports), BorderLayout.CENTER);
        split.setLeftComponent(pnlTree);

        // Right: Filter bar + Table
        JPanel pnlContent = new JPanel(new BorderLayout());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(new Color(240, 244, 250));
        filterBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JLabel lblFilter = new JLabel("Lọc:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtReportFilter = new JTextField(15);
        txtReportFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtReportFilter.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterReportTable(); }
            public void removeUpdate(DocumentEvent e) { filterReportTable(); }
            public void changedUpdate(DocumentEvent e) { filterReportTable(); }
        });

        chkReportLockAll = new JCheckBox("Khóa tất cả");
        chkReportLockAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkReportLockAll.setOpaque(false);
        chkReportLockAll.addActionListener(e -> onReportLockAllToggle());

        chkReportAllowAll = new JCheckBox("Cho phép tất cả");
        chkReportAllowAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkReportAllowAll.setOpaque(false);
        chkReportAllowAll.addActionListener(e -> onReportAllowAllToggle());

        filterBar.add(lblFilter);
        filterBar.add(txtReportFilter);
        filterBar.add(chkReportLockAll);
        filterBar.add(chkReportAllowAll);
        pnlContent.add(filterBar, BorderLayout.NORTH);

        modelReportRoles = new ReportRoleTableModel();
        tableReportRoles = new JTable(modelReportRoles);
        tableReportRoles.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableReportRoles.setRowHeight(23);
        tableReportRoles.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableReportRoles.getTableHeader().setBackground(new Color(235, 240, 248));

        tableReportRoles.getColumnModel().getColumn(0).setPreferredWidth(300); // Báo cáo
        tableReportRoles.getColumnModel().getColumn(1).setPreferredWidth(100); // Không được xem
        tableReportRoles.getColumnModel().getColumn(2).setPreferredWidth(90);  // Được phép

        pnlContent.add(new JScrollPane(tableReportRoles), BorderLayout.CENTER);
        split.setRightComponent(pnlContent);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JButton createToolBtn(String text, String iconName, Color textColor) {
        JButton b = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setForeground(textColor);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setBorder(new CompoundBorder(new LineBorder(new Color(180, 200, 225)), new EmptyBorder(3, 7, 3, 7)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // =========================================================================
    // 3. DATA LOADING & BINDING
    // =========================================================================
    private void loadAllData() {
        loadGroupsTree();
        loadFunctions();
        loadReports();
    }

    private void loadGroupsTree() {
        groupList = groupUserDAO.getAllGroups();
        rootNodeGroups.removeAllChildren();

        for (SGroupUser g : groupList) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(g);
            rootNodeGroups.add(node);
        }
        treeModelGroups.reload();
        treeGroups.expandPath(new TreePath(rootNodeGroups.getPath()));

        // Select first group if available
        if (rootNodeGroups.getChildCount() > 0) {
            DefaultMutableTreeNode first = (DefaultMutableTreeNode) rootNodeGroups.getChildAt(0);
            treeGroups.setSelectionPath(new TreePath(first.getPath()));
        } else {
            treeGroups.setSelectionPath(new TreePath(rootNodeGroups.getPath()));
        }
    }

    private void onGroupSelected() {
        TreePath path = treeGroups.getSelectionPath();
        if (path == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

        if (node == rootNodeGroups) {
            selectedGroup = null;
            currentUsersList = userDAO.getAllNonAdminUsers();
            modelUsers.setData(currentUsersList);
            currentGroupRoles = new HashMap<>();
            currentReportRoles = new HashMap<>();
            modelFuncRoles.setData(new ArrayList<>(), currentGroupRoles, null);
            modelReportRoles.setData(new ArrayList<>(), currentReportRoles, null);
        } else if (node.getUserObject() instanceof SGroupUser) {
            selectedGroup = (SGroupUser) node.getUserObject();
            currentUsersList = userDAO.getUsersByGroupId(selectedGroup.getId());
            modelUsers.setData(currentUsersList);
            loadRolesForCurrentGroup();
        }
    }

    private void loadRolesForCurrentGroup() {
        if (selectedGroup == null) return;
        currentGroupRoles = groupRoleDAO.getRolesMapByGroupId(selectedGroup.getId());
        currentReportRoles = reportRoleDAO.getReportRolesMapByGroupId(selectedGroup.getId());

        filterFunctionTable();
        filterReportTable();
    }

    private void loadFunctions() {
        allFunctions = functionDAO.getAllFunctions();
        List<String> categories = functionDAO.getDistinctGroupNames();

        rootNodeFunc.removeAllChildren();
        for (String cat : categories) {
            rootNodeFunc.add(new DefaultMutableTreeNode(cat));
        }
        treeModelFunc.reload();
        treeFuncCategories.expandPath(new TreePath(rootNodeFunc.getPath()));
        treeFuncCategories.setSelectionPath(new TreePath(rootNodeFunc.getPath()));
    }

    private void onFuncCategorySelected() {
        TreePath path = treeFuncCategories.getSelectionPath();
        if (path == null) {
            selectedFuncCategory = null;
        } else {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node == rootNodeFunc) {
                selectedFuncCategory = null;
            } else {
                selectedFuncCategory = node.getUserObject().toString();
            }
        }
        filterFunctionTable();
    }

    private void filterFunctionTable() {
        if (selectedGroup == null) {
            modelFuncRoles.setData(new ArrayList<>(), currentGroupRoles, null);
            return;
        }

        String search = txtFuncFilter != null ? txtFuncFilter.getText().trim().toLowerCase() : "";
        List<SFunction> filtered = new ArrayList<>();

        for (SFunction f : allFunctions) {
            if (selectedFuncCategory != null && !selectedFuncCategory.equalsIgnoreCase(f.getGroupName())) {
                continue;
            }
            if (!search.isEmpty() && (f.getName() == null || !f.getName().toLowerCase().contains(search))) {
                continue;
            }
            filtered.add(f);
        }

        modelFuncRoles.setData(filtered, currentGroupRoles, selectedGroup.getId());
    }

    private void loadReports() {
        allReports = reportDAO.getAllReports();

        rootNodeReports.removeAllChildren();
        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        // Add root items first
        for (SReport r : allReports) {
            if (r.getParentId() == null || r.getParentId().trim().isEmpty()) {
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(r);
                rootNodeReports.add(n);
                nodeMap.put(r.getId(), n);
            }
        }

        // Add children
        for (SReport r : allReports) {
            if (r.getParentId() != null && !r.getParentId().trim().isEmpty()) {
                DefaultMutableTreeNode parent = nodeMap.get(r.getParentId());
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(r);
                if (parent != null) {
                    parent.add(child);
                } else {
                    rootNodeReports.add(child);
                }
                nodeMap.put(r.getId(), child);
            }
        }

        treeModelReports.reload();
        treeReports.expandPath(new TreePath(rootNodeReports.getPath()));
        treeReports.setSelectionPath(new TreePath(rootNodeReports.getPath()));
    }

    private void onReportCategorySelected() {
        TreePath path = treeReports.getSelectionPath();
        if (path == null) {
            selectedReportNode = null;
        } else {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node == rootNodeReports) {
                selectedReportNode = null;
            } else if (node.getUserObject() instanceof SReport) {
                selectedReportNode = (SReport) node.getUserObject();
            }
        }
        filterReportTable();
    }

    private void filterReportTable() {
        if (selectedGroup == null) {
            modelReportRoles.setData(new ArrayList<>(), currentReportRoles, null);
            return;
        }

        String search = txtReportFilter != null ? txtReportFilter.getText().trim().toLowerCase() : "";
        List<SReport> filtered = new ArrayList<>();

        for (SReport r : allReports) {
            if (selectedReportNode != null) {
                // If selected node is folder, include descendants or exact match
                if (!r.getId().equals(selectedReportNode.getId()) && !isDescendantOf(r, selectedReportNode.getId())) {
                    continue;
                }
            }
            if (!search.isEmpty() && (r.getName() == null || !r.getName().toLowerCase().contains(search))) {
                continue;
            }
            filtered.add(r);
        }

        modelReportRoles.setData(filtered, currentReportRoles, selectedGroup.getId());
    }

    private boolean isDescendantOf(SReport r, String parentId) {
        if (r == null || parentId == null) return false;
        String curParent = r.getParentId();
        while (curParent != null && !curParent.trim().isEmpty()) {
            if (curParent.equals(parentId)) return true;
            SReport pReport = findReportById(curParent);
            curParent = pReport != null ? pReport.getParentId() : null;
        }
        return false;
    }

    private SReport findReportById(String id) {
        for (SReport r : allReports) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }

    // =========================================================================
    // 4. ACTION HANDLERS (CRUD GROUPS, USERS & PERMISSIONS)
    // =========================================================================
    private void onAddGroup() {
        NhomNguoiDungDialog dlg = new NhomNguoiDungDialog(this, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadGroupsTree();
        }
    }

    private void onEditGroup() {
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm người dùng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        NhomNguoiDungDialog dlg = new NhomNguoiDungDialog(this, selectedGroup);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadGroupsTree();
        }
    }

    private void onDeleteGroup() {
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm người dùng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhóm '" + selectedGroup.getName() + "' cùng tất cả tài khoản và phân quyền thuộc nhóm này không?",
                "Xác nhận xóa nhóm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = groupUserDAO.deleteGroupCascade(selectedGroup.getId());
            if (ok) {
                selectedGroup = null;
                loadGroupsTree();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa nhóm người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAddUser() {
        String defGroupId = selectedGroup != null ? selectedGroup.getId() : null;
        TaiKhoanNguoiDungDialog dlg = new TaiKhoanNguoiDungDialog(this, null, defGroupId, currentUsersList);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            onGroupSelected();
        }
    }

    private void onEditUser() {
        int row = tableUsers.getSelectedRow();
        if (row < 0 || row >= currentUsersList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SUser u = currentUsersList.get(row);
        TaiKhoanNguoiDungDialog dlg = new TaiKhoanNguoiDungDialog(this, u, u.getGroupUserId(), currentUsersList);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            onGroupSelected();
        }
    }

    private void onDeleteUser() {
        int row = tableUsers.getSelectedRow();
        if (row < 0 || row >= currentUsersList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SUser u = currentUsersList.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa tài khoản '" + u.getUsername() + "' (" + u.getName() + ") không?",
                "Xác nhận xóa tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = userDAO.deleteUser(u.getId());
            if (ok) {
                onGroupSelected();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onFuncLockAllToggle() {
        if (selectedGroup == null) return;
        if (chkFuncLockAll.isSelected()) {
            chkFuncAllowAll.setSelected(false);
            List<String> ids = modelFuncRoles.getDisplayedFunctionIds();
            groupRoleDAO.setAllRolesForGroup(selectedGroup.getId(), 0, ids);
            for (String id : ids) {
                SGroupRole r = currentGroupRoles.computeIfAbsent(id, k -> new SGroupRole(null, selectedGroup.getId(), id, 0));
                r.setMode(0);
            }
            modelFuncRoles.fireTableDataChanged();
        }
    }

    private void onFuncAllowAllToggle() {
        if (selectedGroup == null) return;
        if (chkFuncAllowAll.isSelected()) {
            chkFuncLockAll.setSelected(false);
            for (SFunction f : modelFuncRoles.getDisplayedFunctions()) {
                int mode = f.isViewOnly() ? SGroupRole.MASK_VIEW : SGroupRole.MASK_ALL;
                groupRoleDAO.saveOrUpdateRole(selectedGroup.getId(), f.getId(), mode);
                SGroupRole r = currentGroupRoles.computeIfAbsent(f.getId(), k -> new SGroupRole(null, selectedGroup.getId(), f.getId(), mode));
                r.setMode(mode);
            }
            modelFuncRoles.fireTableDataChanged();
        }
    }

    private void onReportLockAllToggle() {
        if (selectedGroup == null) return;
        if (chkReportLockAll.isSelected()) {
            chkReportAllowAll.setSelected(false);
            List<String> ids = modelReportRoles.getDisplayedReportIds();
            reportRoleDAO.setAllReportRoles(selectedGroup.getId(), SReportRole.MODE_LOCKED, ids);
            for (String id : ids) {
                SReportRole r = currentReportRoles.computeIfAbsent(id, k -> new SReportRole(null, selectedGroup.getId(), id, SReportRole.MODE_LOCKED));
                r.setMode(SReportRole.MODE_LOCKED);
            }
            modelReportRoles.fireTableDataChanged();
        }
    }

    private void onReportAllowAllToggle() {
        if (selectedGroup == null) return;
        if (chkReportAllowAll.isSelected()) {
            chkReportLockAll.setSelected(false);
            List<String> ids = modelReportRoles.getDisplayedReportIds();
            reportRoleDAO.setAllReportRoles(selectedGroup.getId(), SReportRole.MODE_ALLOWED, ids);
            for (String id : ids) {
                SReportRole r = currentReportRoles.computeIfAbsent(id, k -> new SReportRole(null, selectedGroup.getId(), id, SReportRole.MODE_ALLOWED));
                r.setMode(SReportRole.MODE_ALLOWED);
            }
            modelReportRoles.fireTableDataChanged();
        }
    }

    // =========================================================================
    // 5. TABLE MODELS & CUSTOM RENDERERS
    // =========================================================================

    // Group Tree Renderer
    private static class GroupTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() instanceof SGroupUser) {
                SGroupUser g = (SGroupUser) node.getUserObject();
                l.setText(g.getName());
                String iconCode = g.getSimageId() != null ? g.getSimageId().toLowerCase() : "";
                if (iconCode.contains("star")) {
                    l.setIcon(IconHelper.getIcon("asterisk-yellow.png", 16, 16));
                } else if (iconCode.contains("ticket") || iconCode.contains("card")) {
                    l.setIcon(IconHelper.getIcon("btnCard.Icon.png", 16, 16));
                } else if (iconCode.contains("package") || iconCode.contains("folder")) {
                    l.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                } else if (iconCode.contains("admin") || iconCode.contains("property")) {
                    l.setIcon(IconHelper.getIcon("property.png", 16, 16));
                } else {
                    l.setIcon(IconHelper.getIcon("users.png", 16, 16));
                }
            } else {
                l.setText(value.toString());
                l.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
            }
            return l;
        }
    }

    // User Table Model
    private static class UserTableModel extends DefaultTableModel {
        private final String[] columns = {"STT", "Tài khoản"};
        private List<SUser> list = new ArrayList<>();

        public void setData(List<SUser> data) {
            this.list = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return list != null ? list.size() : 0; }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int column) { return columns[column]; }
        @Override
        public boolean isCellEditable(int row, int column) { return false; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SUser u = list.get(rowIndex);
            if (columnIndex == 0) return "▶ " + (rowIndex + 1);
            if (columnIndex == 1) return u.getUsername() != null ? u.getUsername() : "";
            return null;
        }
    }

    // Function Role Table Model with Auto-Save
    private class FunctionRoleTableModel extends DefaultTableModel {
        private final String[] columns = {"Chức năng", "Khóa", "Xem", "Thêm", "Sửa", "Xóa", "Tất cả"};
        private List<SFunction> functions = new ArrayList<>();
        private Map<String, SGroupRole> rolesMap = new HashMap<>();
        private String groupId;

        public void setData(List<SFunction> functions, Map<String, SGroupRole> rolesMap, String groupId) {
            this.functions = functions != null ? functions : new ArrayList<>();
            this.rolesMap = rolesMap != null ? rolesMap : new HashMap<>();
            this.groupId = groupId;
            fireTableDataChanged();
        }

        public List<SFunction> getDisplayedFunctions() { return functions; }
        public List<String> getDisplayedFunctionIds() {
            List<String> res = new ArrayList<>();
            for (SFunction f : functions) res.add(f.getId());
            return res;
        }

        @Override
        public int getRowCount() { return functions != null ? functions.size() : 0; }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int column) { return columns[column]; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? String.class : Boolean.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (groupId == null) return false;
            if (columnIndex == 0) return false;
            SFunction f = functions.get(rowIndex);
            if (f.isViewOnly()) {
                // For ViewOnly functions, only "Khóa" (col 1) and "Xem" (col 2) are editable
                return columnIndex == 1 || columnIndex == 2;
            }
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SFunction f = functions.get(rowIndex);
            SGroupRole r = rolesMap.get(f.getId());
            int mode = (r != null) ? r.getMode() : 0;

            if (columnIndex == 0) return f.getName();
            if (columnIndex == 1) return mode == 0; // Khóa
            if (columnIndex == 2) return (mode & SGroupRole.MASK_VIEW) != 0; // Xem

            if (f.isViewOnly()) {
                return Boolean.FALSE;
            }

            if (columnIndex == 3) return (mode & SGroupRole.MASK_ADD) != 0; // Thêm
            if (columnIndex == 4) return (mode & SGroupRole.MASK_EDIT) != 0; // Sửa
            if (columnIndex == 5) return (mode & SGroupRole.MASK_DELETE) != 0; // Xóa
            if (columnIndex == 6) return (mode & SGroupRole.MASK_ALL) == SGroupRole.MASK_ALL; // Tất cả

            return Boolean.FALSE;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (groupId == null || !(aValue instanceof Boolean)) return;
            boolean checked = (Boolean) aValue;
            SFunction f = functions.get(rowIndex);
            SGroupRole r = rolesMap.computeIfAbsent(f.getId(), k -> new SGroupRole(null, groupId, f.getId(), 0));

            int currentMode = r.getMode();
            int newMode = currentMode;

            switch (columnIndex) {
                case 1: // Khóa
                    if (checked) {
                        newMode = 0;
                    } else if (newMode == 0) {
                        newMode = SGroupRole.MASK_VIEW;
                    }
                    break;
                case 2: // Xem
                    if (checked) {
                        newMode = newMode | SGroupRole.MASK_VIEW;
                    } else {
                        newMode = 0; // Removing view locks the function
                    }
                    break;
                case 3: // Thêm
                    if (checked) {
                        newMode = newMode | SGroupRole.MASK_ADD | SGroupRole.MASK_VIEW;
                    } else {
                        newMode = newMode & ~SGroupRole.MASK_ADD;
                    }
                    break;
                case 4: // Sửa
                    if (checked) {
                        newMode = newMode | SGroupRole.MASK_EDIT | SGroupRole.MASK_VIEW;
                    } else {
                        newMode = newMode & ~SGroupRole.MASK_EDIT;
                    }
                    break;
                case 5: // Xóa
                    if (checked) {
                        newMode = newMode | SGroupRole.MASK_DELETE | SGroupRole.MASK_VIEW;
                    } else {
                        newMode = newMode & ~SGroupRole.MASK_DELETE;
                    }
                    break;
                case 6: // Tất cả
                    if (checked) {
                        newMode = SGroupRole.MASK_ALL;
                    } else {
                        newMode = 0;
                    }
                    break;
            }

            r.setMode(newMode);
            fireTableRowsUpdated(rowIndex, rowIndex);

            // Auto-save immediately to database
            final int finalMode = newMode;
            SwingUtilities.invokeLater(() -> groupRoleDAO.saveOrUpdateRole(groupId, f.getId(), finalMode));
        }
    }

    // Custom Cell Renderer for Function Role Grid
    private class FunctionRoleCheckBoxRenderer extends JCheckBox implements TableCellRenderer {
        public FunctionRoleCheckBoxRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                setForeground(table.getForeground());
            }

            setSelected(value instanceof Boolean && (Boolean) value);

            SFunction f = modelFuncRoles.getDisplayedFunctions().get(row);
            if (f.isViewOnly() && column >= 3) {
                setEnabled(false);
                setVisible(false);
            } else {
                setEnabled(true);
                setVisible(true);
            }

            return this;
        }
    }

    // Report Role Table Model with Auto-Save
    private class ReportRoleTableModel extends DefaultTableModel {
        private final String[] columns = {"Báo cáo", "Không được xem", "Được phép"};
        private List<SReport> reports = new ArrayList<>();
        private Map<String, SReportRole> rolesMap = new HashMap<>();
        private String groupId;

        public void setData(List<SReport> reports, Map<String, SReportRole> rolesMap, String groupId) {
            this.reports = reports != null ? reports : new ArrayList<>();
            this.rolesMap = rolesMap != null ? rolesMap : new HashMap<>();
            this.groupId = groupId;
            fireTableDataChanged();
        }

        public List<String> getDisplayedReportIds() {
            List<String> res = new ArrayList<>();
            for (SReport r : reports) res.add(r.getId());
            return res;
        }

        @Override
        public int getRowCount() { return reports != null ? reports.size() : 0; }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int column) { return columns[column]; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? String.class : Boolean.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return groupId != null && columnIndex > 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SReport r = reports.get(rowIndex);
            SReportRole rr = rolesMap.get(r.getId());
            int mode = (rr != null) ? rr.getMode() : SReportRole.MODE_ALLOWED;

            if (columnIndex == 0) return r.getName();
            if (columnIndex == 1) return mode == SReportRole.MODE_LOCKED; // Không được xem
            if (columnIndex == 2) return mode == SReportRole.MODE_ALLOWED; // Được phép
            return Boolean.FALSE;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (groupId == null || !(aValue instanceof Boolean)) return;
            boolean checked = (Boolean) aValue;
            if (!checked) return; // Radio behavior: only react when checked

            SReport r = reports.get(rowIndex);
            SReportRole rr = rolesMap.computeIfAbsent(r.getId(), k -> new SReportRole(null, groupId, r.getId(), SReportRole.MODE_ALLOWED));

            int newMode = (columnIndex == 1) ? SReportRole.MODE_LOCKED : SReportRole.MODE_ALLOWED;
            rr.setMode(newMode);
            fireTableRowsUpdated(rowIndex, rowIndex);

            // Auto-save immediately
            final int finalMode = newMode;
            SwingUtilities.invokeLater(() -> reportRoleDAO.saveOrUpdateReportRole(groupId, r.getId(), finalMode));
        }
    }
}

