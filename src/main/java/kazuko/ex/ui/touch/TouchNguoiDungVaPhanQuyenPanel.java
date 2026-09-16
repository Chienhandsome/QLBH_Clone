package kazuko.ex.ui.touch;

import kazuko.ex.dao.FunctionDAO;
import kazuko.ex.dao.GroupRoleDAO;
import kazuko.ex.dao.GroupUserDAO;
import kazuko.ex.dao.ReportDAO;
import kazuko.ex.dao.ReportRoleDAO;
import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SFunction;
import kazuko.ex.model.system.SGroupRole;
import kazuko.ex.model.system.SGroupUser;
import kazuko.ex.model.system.SReport;
import kazuko.ex.model.system.SReportRole;
import kazuko.ex.model.system.SUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Màn hình cảm ứng NGƯỜI DÙNG VÀ PHÂN QUYỀN (TouchNguoiDungVaPhanQuyenPanel).
 * Chuẩn hóa giao diện khớp 100% hai ảnh thiết kế:
 * - media_1789534697121.png: Phân quyền sử dụng (6 nút: KHÓA, XEM, THÊM, SỬA, XÓA, TẤT CẢ)
 * - media_1789534757584.png: Phân quyền báo cáo (2 nút: KHÓA, XEM)
 */
public final class TouchNguoiDungVaPhanQuyenPanel extends JPanel {
    private static final Color BG_DARK = new Color(0, 67, 86);
    private static final Color NAVY = new Color(0, 0, 70);
    private static final Color ORANGE = new Color(226, 79, 5);
    private static final Color GREEN_NORMAL = new Color(0, 96, 0);
    private static final Color DARK_GREEN_HEADER = new Color(0, 80, 32);
    private static final Color BADGE_BROWN = new Color(128, 48, 48);
    private static final Color ACTIVE_RED = new Color(220, 0, 0);
    private static final Color INACTIVE_GRAY = new Color(130, 134, 138);
    private static final Color EXIT_RED = new Color(192, 0, 0);

    private final GroupUserDAO groupUserDAO = new GroupUserDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FunctionDAO functionDAO = new FunctionDAO();
    private final GroupRoleDAO groupRoleDAO = new GroupRoleDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final ReportRoleDAO reportRoleDAO = new ReportRoleDAO();

    private final Runnable backAction;

    // State
    private List<SGroupUser> groupList = new ArrayList<>();
    private SGroupUser selectedGroup = null; // null = TẤT CẢ
    private List<SUser> userList = new ArrayList<>();
    private SUser selectedUser = null;

    private boolean isReportTab = false;
    private String selectedFuncCategory = null; // null = TẤT CẢ
    private String selectedReportCategory = null; // null = TẤT CẢ

    private List<SFunction> allFunctions = new ArrayList<>();
    private List<SReport> allReports = new ArrayList<>();
    private final Map<String, SGroupRole> groupRolesMap = new HashMap<>();
    private final Map<String, SReportRole> reportRolesMap = new HashMap<>();

    // UI Panels
    private JPanel pnlGroupList;
    private JPanel pnlUsersList;
    private JPanel pnlCategoryList;
    private JPanel pnlPermissionsList;
    private JButton btnTabFunc;
    private JButton btnTabReport;

    public TouchNguoiDungVaPhanQuyenPanel(Runnable backAction) {
        this.backAction = backAction;
        setLayout(new BorderLayout(0, 6));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(5, 6, 8, 6));

        loadInitialData();
        initUI();
        refreshAllViews();
    }

    private void loadInitialData() {
        groupList = groupUserDAO.getAllGroups();
        if (!groupList.isEmpty()) {
            // Mặc định chọn nhóm "Thu ngân" nếu có
            selectedGroup = groupList.stream()
                    .filter(g -> g.getName() != null && g.getName().toLowerCase().contains("thu ngân"))
                    .findFirst().orElse(groupList.get(0));
        }
        allFunctions = functionDAO.getAllFunctions();
        allReports = reportDAO.getAllReports();
        loadRolesForCurrentGroup();
    }

    private void loadRolesForCurrentGroup() {
        groupRolesMap.clear();
        reportRolesMap.clear();
        if (selectedGroup != null && selectedGroup.getId() != null) {
            groupRolesMap.putAll(groupRoleDAO.getRolesMapByGroupId(selectedGroup.getId()));
            reportRolesMap.putAll(reportRoleDAO.getReportRolesMapByGroupId(selectedGroup.getId()));
        }
    }

    private void initUI() {
        // 1. Top Toolbar
        add(createTopToolbar(), BorderLayout.NORTH);

        // Center: Upper (Groups + Users) and Lower (Categories + Permissions)
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);

        // Upper section: Height ~180
        center.add(createUpperSection(), BorderLayout.NORTH);

        // Middle & Lower section
        JPanel lowerContainer = new JPanel(new BorderLayout(0, 6));
        lowerContainer.setOpaque(false);
        lowerContainer.add(createMiddleTabBar(), BorderLayout.NORTH);
        lowerContainer.add(createLowerSection(), BorderLayout.CENTER);

        center.add(lowerContainer, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createTopToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 48));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        // Group 1: [NHÓM NGƯỜI DÙNG] [QUẢN LÝ]
        JLabel lblGroupBadge = createBadge("NHÓM NGƯỜI DÙNG", 180);
        JButton btnManageGroup = createHeaderButton("QUẢN LÝ", DARK_GREEN_HEADER);
        btnManageGroup.addActionListener(e -> manageGroupsDialog());

        left.add(lblGroupBadge);
        left.add(btnManageGroup);
        left.add(Box.createHorizontalStrut(6));

        // Group 2: [NGƯỜI DÙNG] [THÊM MỚI] [CHỈNH SỬA] [XÓA]
        JLabel lblUserBadge = createBadge("NGƯỜI DÙNG", 135);
        JButton btnAddUser = createHeaderButton("THÊM MỚI", DARK_GREEN_HEADER);
        btnAddUser.addActionListener(e -> addUserDialog());

        JButton btnEditUser = createHeaderButton("CHỈNH SỬA", DARK_GREEN_HEADER);
        btnEditUser.addActionListener(e -> editUserDialog());

        JButton btnDeleteUser = createHeaderButton("XÓA", EXIT_RED);
        btnDeleteUser.addActionListener(e -> deleteUserAction());

        left.add(lblUserBadge);
        left.add(btnAddUser);
        left.add(btnEditUser);
        left.add(btnDeleteUser);

        bar.add(left, BorderLayout.WEST);

        // Right: [THOÁT]
        JButton btnExit = new JButton("THOÁT");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnExit.setForeground(Color.WHITE);
        btnExit.setBackground(EXIT_RED);
        btnExit.setFocusPainted(false);
        btnExit.setPreferredSize(new Dimension(130, 44));
        btnExit.setBorder(new LineBorder(new Color(240, 150, 150), 1, true));
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> {
            if (backAction != null) backAction.run();
        });
        bar.add(btnExit, BorderLayout.EAST);

        return bar;
    }

    private JPanel createUpperSection() {
        JPanel upper = new JPanel(new BorderLayout(6, 0));
        upper.setOpaque(false);
        upper.setPreferredSize(new Dimension(0, 180));

        // Left sidebar: Nhóm người dùng (Width ~190)
        JPanel leftBox = new JPanel(new BorderLayout());
        leftBox.setOpaque(false);
        leftBox.setPreferredSize(new Dimension(190, 0));

        pnlGroupList = new JPanel();
        pnlGroupList.setLayout(new BoxLayout(pnlGroupList, BoxLayout.Y_AXIS));
        pnlGroupList.setBackground(new Color(0, 52, 68));
        pnlGroupList.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane scrollGroups = new JScrollPane(pnlGroupList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGroups.setBorder(new LineBorder(new Color(60, 110, 130), 1, true));
        scrollGroups.getVerticalScrollBar().setUnitIncrement(30);
        leftBox.add(scrollGroups, BorderLayout.CENTER);
        upper.add(leftBox, BorderLayout.WEST);

        // Right area: Danh sách người dùng
        JPanel rightBox = new JPanel(new BorderLayout());
        rightBox.setBackground(new Color(236, 238, 240));
        rightBox.setBorder(new LineBorder(new Color(60, 110, 130), 2, true));

        pnlUsersList = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlUsersList.setOpaque(false);

        JScrollPane scrollUsers = new JScrollPane(pnlUsersList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollUsers.setOpaque(false);
        scrollUsers.getViewport().setOpaque(false);
        scrollUsers.setBorder(BorderFactory.createEmptyBorder());
        rightBox.add(scrollUsers, BorderLayout.CENTER);

        // Settings gear button at bottom right
        JPanel pnlGear = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        pnlGear.setOpaque(false);
        JButton btnGear = createGearButton();
        pnlGear.add(btnGear);
        rightBox.add(pnlGear, BorderLayout.SOUTH);

        upper.add(rightBox, BorderLayout.CENTER);
        return upper;
    }

    private JPanel createMiddleTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 38));

        btnTabFunc = new JButton("QUYỀN SỬ DỤNG");
        btnTabFunc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnTabFunc.setFocusPainted(false);
        btnTabFunc.setPreferredSize(new Dimension(170, 36));
        btnTabFunc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTabFunc.addActionListener(e -> {
            isReportTab = false;
            updateTabStyles();
            refreshCategoryList();
            refreshPermissionsList();
        });

        btnTabReport = new JButton("QUYỀN BÁO CÁO");
        btnTabReport.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnTabReport.setFocusPainted(false);
        btnTabReport.setPreferredSize(new Dimension(170, 36));
        btnTabReport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTabReport.addActionListener(e -> {
            isReportTab = true;
            updateTabStyles();
            refreshCategoryList();
            refreshPermissionsList();
        });

        updateTabStyles();
        bar.add(btnTabFunc);
        bar.add(btnTabReport);
        return bar;
    }

    private void updateTabStyles() {
        if (!isReportTab) {
            btnTabFunc.setBackground(ORANGE);
            btnTabFunc.setForeground(Color.WHITE);
            btnTabFunc.setBorder(new LineBorder(Color.WHITE, 1, true));

            btnTabReport.setBackground(new Color(40, 58, 68));
            btnTabReport.setForeground(new Color(210, 225, 230));
            btnTabReport.setBorder(new LineBorder(new Color(80, 110, 125), 1, true));
        } else {
            btnTabReport.setBackground(ORANGE);
            btnTabReport.setForeground(Color.WHITE);
            btnTabReport.setBorder(new LineBorder(Color.WHITE, 1, true));

            btnTabFunc.setBackground(new Color(40, 58, 68));
            btnTabFunc.setForeground(new Color(210, 225, 230));
            btnTabFunc.setBorder(new LineBorder(new Color(80, 110, 125), 1, true));
        }
    }

    private JPanel createLowerSection() {
        JPanel lower = new JPanel(new BorderLayout(6, 0));
        lower.setOpaque(false);

        // Left sidebar: Danh mục (Width ~190)
        JPanel leftBox = new JPanel(new BorderLayout());
        leftBox.setOpaque(false);
        leftBox.setPreferredSize(new Dimension(190, 0));

        pnlCategoryList = new JPanel();
        pnlCategoryList.setLayout(new BoxLayout(pnlCategoryList, BoxLayout.Y_AXIS));
        pnlCategoryList.setBackground(new Color(0, 52, 68));
        pnlCategoryList.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane scrollCat = new JScrollPane(pnlCategoryList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCat.setBorder(new LineBorder(new Color(60, 110, 130), 1, true));
        scrollCat.getVerticalScrollBar().setUnitIncrement(30);
        leftBox.add(scrollCat, BorderLayout.CENTER);
        lower.add(leftBox, BorderLayout.WEST);

        // Right area: Ma trận phân quyền
        JPanel rightBox = new JPanel(new BorderLayout());
        rightBox.setBackground(new Color(0, 52, 68));
        rightBox.setBorder(new LineBorder(new Color(60, 110, 130), 2, true));

        pnlPermissionsList = new JPanel();
        pnlPermissionsList.setLayout(new BoxLayout(pnlPermissionsList, BoxLayout.Y_AXIS));
        pnlPermissionsList.setOpaque(false);
        pnlPermissionsList.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane scrollPerm = new JScrollPane(pnlPermissionsList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPerm.setOpaque(false);
        scrollPerm.getViewport().setOpaque(false);
        scrollPerm.setBorder(BorderFactory.createEmptyBorder());
        scrollPerm.getVerticalScrollBar().setUnitIncrement(35);
        rightBox.add(scrollPerm, BorderLayout.CENTER);

        // Settings gear button at bottom right
        JPanel pnlGear = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        pnlGear.setOpaque(false);
        JButton btnGear = createGearButton();
        pnlGear.add(btnGear);
        rightBox.add(pnlGear, BorderLayout.SOUTH);

        lower.add(rightBox, BorderLayout.CENTER);
        return lower;
    }

    private void refreshAllViews() {
        refreshGroupList();
        refreshUsersList();
        refreshCategoryList();
        refreshPermissionsList();
    }

    private void refreshGroupList() {
        pnlGroupList.removeAll();

        // 1. Button [TẤT CẢ]
        JButton btnAll = createSidebarButton("TẤT CẢ", selectedGroup == null ? ORANGE : NAVY);
        btnAll.addActionListener(e -> {
            selectedGroup = null;
            loadRolesForCurrentGroup();
            refreshGroupList();
            refreshUsersList();
            refreshPermissionsList();
        });
        pnlGroupList.add(btnAll);
        pnlGroupList.add(Box.createVerticalStrut(6));

        // 2. Dynamic groups
        for (SGroupUser g : groupList) {
            boolean isSel = selectedGroup != null && g.getId() != null && g.getId().equals(selectedGroup.getId());
            JButton btn = createSidebarButton(g.getName() != null ? g.getName() : "", isSel ? ORANGE : GREEN_NORMAL);
            btn.addActionListener(e -> {
                selectedGroup = g;
                loadRolesForCurrentGroup();
                refreshGroupList();
                refreshUsersList();
                refreshPermissionsList();
            });
            pnlGroupList.add(btn);
            pnlGroupList.add(Box.createVerticalStrut(6));
        }

        pnlGroupList.add(Box.createVerticalGlue());
        pnlGroupList.revalidate();
        pnlGroupList.repaint();
    }

    private void refreshUsersList() {
        pnlUsersList.removeAll();
        if (selectedGroup == null) {
            userList = userDAO.getAllNonAdminUsers();
        } else {
            userList = userDAO.getUsersByGroupId(selectedGroup.getId());
        }

        if (userList.isEmpty()) {
            selectedUser = null;
        } else if (selectedUser == null || userList.stream().noneMatch(u -> u.getId().equals(selectedUser.getId()))) {
            selectedUser = userList.get(0);
        }

        for (SUser u : userList) {
            boolean isSel = selectedUser != null && u.getId().equals(selectedUser.getId());
            JButton btnUser = new JButton(u.getUsername() != null ? u.getUsername() : "");
            btnUser.setFont(new Font("Segoe UI", Font.BOLD, 17));
            btnUser.setForeground(Color.WHITE);
            btnUser.setBackground(isSel ? ORANGE : GREEN_NORMAL);
            btnUser.setFocusPainted(false);
            btnUser.setPreferredSize(new Dimension(280, 48));
            btnUser.setBorder(new LineBorder(Color.WHITE, 1, true));
            btnUser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnUser.addActionListener(e -> {
                selectedUser = u;
                refreshUsersList();
            });
            pnlUsersList.add(btnUser);
        }

        pnlUsersList.revalidate();
        pnlUsersList.repaint();
    }

    private void refreshCategoryList() {
        pnlCategoryList.removeAll();

        if (!isReportTab) {
            // Function categories
            List<String> categories = functionDAO.getDistinctGroupNames();
            JButton btnAll = createSidebarButton("TẤT CẢ", selectedFuncCategory == null ? ORANGE : NAVY);
            btnAll.addActionListener(e -> {
                selectedFuncCategory = null;
                refreshCategoryList();
                refreshPermissionsList();
            });
            pnlCategoryList.add(btnAll);
            pnlCategoryList.add(Box.createVerticalStrut(6));

            for (String cat : categories) {
                boolean isSel = cat.equalsIgnoreCase(selectedFuncCategory);
                JButton btn = createSidebarButton(cat, isSel ? ORANGE : GREEN_NORMAL);
                btn.addActionListener(e -> {
                    selectedFuncCategory = cat;
                    refreshCategoryList();
                    refreshPermissionsList();
                });
                pnlCategoryList.add(btn);
                pnlCategoryList.add(Box.createVerticalStrut(6));
            }
        } else {
            // Report categories
            JButton btnAll = createSidebarButton("TẤT CẢ", selectedReportCategory == null ? ORANGE : NAVY);
            btnAll.addActionListener(e -> {
                selectedReportCategory = null;
                refreshCategoryList();
                refreshPermissionsList();
            });
            pnlCategoryList.add(btnAll);
            pnlCategoryList.add(Box.createVerticalStrut(6));

            // Default categories in screenshot: BÁO CÁO QUỸ, BÁO CÁO DANH MỤC, BÁO CÁO BÁN HÀNG, BÁO CÁO ĐẶT HÀNG
            List<String> reportCats = new ArrayList<>();
            reportCats.add("BÁO CÁO QUỸ");
            reportCats.add("BÁO CÁO DANH MỤC");
            reportCats.add("BÁO CÁO BÁN HÀNG");
            reportCats.add("BÁO CÁO ĐẶT HÀNG");

            for (String cat : reportCats) {
                boolean isSel = cat.equalsIgnoreCase(selectedReportCategory);
                JButton btn = createSidebarButton(cat, isSel ? ORANGE : GREEN_NORMAL);
                btn.addActionListener(e -> {
                    selectedReportCategory = cat;
                    refreshCategoryList();
                    refreshPermissionsList();
                });
                pnlCategoryList.add(btn);
                pnlCategoryList.add(Box.createVerticalStrut(6));
            }
        }

        pnlCategoryList.add(Box.createVerticalGlue());
        pnlCategoryList.revalidate();
        pnlCategoryList.repaint();
    }

    private void refreshPermissionsList() {
        pnlPermissionsList.removeAll();

        if (!isReportTab) {
            // Function permissions
            List<SFunction> filtered = allFunctions.stream()
                    .filter(f -> selectedFuncCategory == null || (f.getGroupName() != null && f.getGroupName().equalsIgnoreCase(selectedFuncCategory)))
                    .collect(Collectors.toList());

            for (SFunction f : filtered) {
                pnlPermissionsList.add(createFunctionRoleRow(f));
                pnlPermissionsList.add(Box.createVerticalStrut(6));
            }
        } else {
            // Report permissions
            List<SReport> filtered = allReports.stream()
                    .filter(r -> {
                        if (selectedReportCategory == null) return true;
                        String name = r.getName() != null ? r.getName().toUpperCase() : "";
                        String note = r.getNote() != null ? r.getNote().toUpperCase() : "";
                        if (selectedReportCategory.contains("BÁN HÀNG")) {
                            return name.contains("BÁN HÀNG") || note.contains("BÁN HÀNG");
                        } else if (selectedReportCategory.contains("QUỸ")) {
                            return name.contains("QUỸ") || name.contains("THU") || name.contains("CHI");
                        } else if (selectedReportCategory.contains("ĐẶT HÀNG")) {
                            return name.contains("ĐẶT HÀNG");
                        } else {
                            return true;
                        }
                    })
                    .collect(Collectors.toList());

            for (SReport r : filtered) {
                pnlPermissionsList.add(createReportRoleRow(r));
                pnlPermissionsList.add(Box.createVerticalStrut(6));
            }
        }

        pnlPermissionsList.add(Box.createVerticalGlue());
        pnlPermissionsList.revalidate();
        pnlPermissionsList.repaint();
    }

    private JPanel createFunctionRoleRow(SFunction f) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setPreferredSize(new Dimension(0, 44));

        // Left: Function Name
        JLabel lblName = new JLabel(f.getName() != null ? f.getName().toUpperCase() : "");
        lblName.setOpaque(true);
        lblName.setBackground(GREEN_NORMAL);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblName.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        row.add(lblName, BorderLayout.CENTER);

        // Right buttons
        SGroupRole role = groupRolesMap.get(f.getId());
        int mode = role != null && role.getMode() != null ? role.getMode() : 0;

        boolean isViewOnly = f.isViewOnly();
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);

        // [KHÓA] button
        boolean isLock = (mode == 0);
        JButton btnLock = createPermissionPill("KHÓA", isLock);
        btnLock.addActionListener(e -> updateFunctionRole(f, 0));
        actions.add(btnLock);

        // [XEM] button
        boolean isView = (mode & SGroupRole.MASK_VIEW) != 0;
        JButton btnView = createPermissionPill("XEM", isView);
        btnView.addActionListener(e -> {
            int newMode = isView ? (mode & ~SGroupRole.MASK_ALL) : (mode | SGroupRole.MASK_VIEW);
            updateFunctionRole(f, newMode);
        });
        actions.add(btnView);

        if (!isViewOnly) {
            // [THÊM]
            boolean isAdd = (mode & SGroupRole.MASK_ADD) != 0;
            JButton btnAdd = createPermissionPill("THÊM", isAdd);
            btnAdd.addActionListener(e -> {
                int newMode = isAdd ? (mode & ~SGroupRole.MASK_ADD) : (mode | SGroupRole.MASK_ADD | SGroupRole.MASK_VIEW);
                updateFunctionRole(f, newMode);
            });
            actions.add(btnAdd);

            // [SỬA]
            boolean isEdit = (mode & SGroupRole.MASK_EDIT) != 0;
            JButton btnEdit = createPermissionPill("SỬA", isEdit);
            btnEdit.addActionListener(e -> {
                int newMode = isEdit ? (mode & ~SGroupRole.MASK_EDIT) : (mode | SGroupRole.MASK_EDIT | SGroupRole.MASK_VIEW);
                updateFunctionRole(f, newMode);
            });
            actions.add(btnEdit);

            // [XÓA]
            boolean isDel = (mode & SGroupRole.MASK_DELETE) != 0;
            JButton btnDel = createPermissionPill("XÓA", isDel);
            btnDel.addActionListener(e -> {
                int newMode = isDel ? (mode & ~SGroupRole.MASK_DELETE) : (mode | SGroupRole.MASK_DELETE | SGroupRole.MASK_VIEW);
                updateFunctionRole(f, newMode);
            });
            actions.add(btnDel);

            // [TẤT CẢ]
            boolean isAll = (mode & SGroupRole.MASK_ALL) == SGroupRole.MASK_ALL;
            JButton btnAll = createPermissionPill("TẤT CẢ", isAll);
            btnAll.addActionListener(e -> {
                int newMode = isAll ? 0 : SGroupRole.MASK_ALL;
                updateFunctionRole(f, newMode);
            });
            actions.add(btnAll);
        }

        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JPanel createReportRoleRow(SReport r) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setPreferredSize(new Dimension(0, 44));

        // Left: Report Name
        JLabel lblName = new JLabel(r.getName() != null ? r.getName().toUpperCase() : "");
        lblName.setOpaque(true);
        lblName.setBackground(GREEN_NORMAL);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblName.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        row.add(lblName, BorderLayout.CENTER);

        // Right buttons: KHÓA, XEM
        SReportRole role = reportRolesMap.get(r.getId());
        int mode = role != null && role.getMode() != null ? role.getMode() : SReportRole.MODE_LOCKED;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);

        boolean isLock = (mode == SReportRole.MODE_LOCKED);
        JButton btnLock = createPermissionPill("KHÓA", isLock);
        btnLock.addActionListener(e -> updateReportRole(r, SReportRole.MODE_LOCKED));
        actions.add(btnLock);

        boolean isView = (mode == SReportRole.MODE_ALLOWED || mode > 0);
        JButton btnView = createPermissionPill("XEM", isView);
        btnView.addActionListener(e -> updateReportRole(r, SReportRole.MODE_ALLOWED));
        actions.add(btnView);

        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private void updateFunctionRole(SFunction f, int newMode) {
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhóm người dùng để thiết lập phân quyền!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        groupRoleDAO.saveOrUpdateRole(selectedGroup.getId(), f.getId(), newMode);
        SGroupRole role = groupRolesMap.computeIfAbsent(f.getId(), k -> new SGroupRole(UUID.randomUUID().toString(), selectedGroup.getId(), f.getId(), newMode));
        role.setMode(newMode);
        refreshPermissionsList();
    }

    private void updateReportRole(SReport r, int newMode) {
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhóm người dùng để thiết lập phân quyền!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        reportRoleDAO.saveOrUpdateReportRole(selectedGroup.getId(), r.getId(), newMode);
        SReportRole role = reportRolesMap.computeIfAbsent(r.getId(), k -> new SReportRole(UUID.randomUUID().toString(), selectedGroup.getId(), r.getId(), newMode));
        role.setMode(newMode);
        refreshPermissionsList();
    }

    // Modal dialogs
    private void manageGroupsDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "QUẢN LÝ NHÓM NGƯỜI DÙNG", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(0, 8));
        dlg.getContentPane().setBackground(BG_DARK);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lbl = new JLabel("DANH SÁCH NHÓM NGƯỜI DÙNG");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        content.add(lbl);
        content.add(Box.createVerticalStrut(10));

        DefaultListModel<SGroupUser> listModel = new DefaultListModel<>();
        for (SGroupUser g : groupUserDAO.getAllGroups()) listModel.addElement(g);
        JList<SGroupUser> groupJList = new JList<>(listModel);
        groupJList.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        groupJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SGroupUser g) l.setText(g.getName() != null ? g.getName() : "");
                l.setBorder(new EmptyBorder(4, 8, 4, 8));
                return l;
            }
        });
        JScrollPane sp = new JScrollPane(groupJList);
        sp.setPreferredSize(new Dimension(380, 200));
        content.add(sp);
        dlg.add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = createHeaderButton("THÊM NHÓM", DARK_GREEN_HEADER);
        btnAdd.addActionListener(e -> {
            TouchTextKeyboardDialog kb = new TouchTextKeyboardDialog(dlg, "TÊN NHÓM MỚI", "");
            kb.setVisible(true);
            if (kb.isConfirmed() && kb.getResultText() != null && !kb.getResultText().trim().isEmpty()) {
                SGroupUser g = new SGroupUser();
                g.setName(kb.getResultText().trim());
                groupUserDAO.insertGroup(g);
                groupList = groupUserDAO.getAllGroups();
                listModel.addElement(g);
                refreshGroupList();
            }
        });

        JButton btnClose = createHeaderButton("ĐÓNG", EXIT_RED);
        btnClose.addActionListener(e -> dlg.dispose());

        btnPanel.add(btnAdd);
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void addUserDialog() {
        openUserEditDialog(null);
    }

    private void editUserDialog() {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần chỉnh sửa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        openUserEditDialog(selectedUser);
    }

    private void openUserEditDialog(SUser user) {
        boolean isNew = user == null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "THÊM MỚI NGƯỜI DÙNG" : "CHỈNH SỬA NGƯỜI DÙNG", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(0, 10));
        dlg.getContentPane().setBackground(BG_DARK);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 18, 10, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtUsername = new JTextField(isNew ? "" : (user.getUsername() != null ? user.getUsername() : ""), 15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtUsername.setEnabled(isNew);

        JTextField txtFullName = new JTextField(isNew ? "" : (user.getName() != null ? user.getName() : ""), 15);
        txtFullName.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JPasswordField txtPass = new JPasswordField(isNew ? "" : (user.getPassword() != null ? user.getPassword() : ""), 15);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JComboBox<SGroupUser> cbGroup = new JComboBox<>(groupList.toArray(new SGroupUser[0]));
        cbGroup.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        if (selectedGroup != null) {
            for (int i = 0; i < cbGroup.getItemCount(); i++) {
                if (cbGroup.getItemAt(i).getId().equals(selectedGroup.getId())) {
                    cbGroup.setSelectedIndex(i);
                    break;
                }
            }
        }

        addFormRow(form, gbc, 0, "TÊN ĐĂNG NHẬP:", txtUsername, dlg);
        addFormRow(form, gbc, 1, "TÊN ĐẦY ĐỦ:", txtFullName, dlg);
        addFormRow(form, gbc, 2, "MẬT KHẨU:", txtPass, dlg);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblG = new JLabel("NHÓM:");
        lblG.setForeground(Color.WHITE);
        lblG.setFont(new Font("Segoe UI", Font.BOLD, 17));
        form.add(lblG, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(cbGroup, gbc);

        dlg.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setOpaque(false);

        JButton btnSave = createHeaderButton("LƯU", DARK_GREEN_HEADER);
        btnSave.setPreferredSize(new Dimension(130, 48));
        btnSave.addActionListener(e -> {
            String uName = txtUsername.getText().trim();
            if (uName.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên đăng nhập!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SGroupUser selectedG = (SGroupUser) cbGroup.getSelectedItem();
            String gId = selectedG != null ? selectedG.getId() : (selectedGroup != null ? selectedGroup.getId() : null);

            if (isNew) {
                SUser newUser = new SUser();
                newUser.setUsername(uName);
                newUser.setName(txtFullName.getText().trim());
                newUser.setPassword(new String(txtPass.getPassword()).trim());
                newUser.setGroupUserId(gId);
                newUser.setIsAdmin(0);
                userDAO.insertUser(newUser, null);
            } else {
                user.setName(txtFullName.getText().trim());
                user.setPassword(new String(txtPass.getPassword()).trim());
                user.setGroupUserId(gId);
                userDAO.updateUser(user, null);
            }

            refreshUsersList();
            dlg.dispose();
        });

        JButton btnCancel = createHeaderButton("HỦY", EXIT_RED);
        btnCancel.setPreferredSize(new Dimension(130, 48));
        btnCancel.addActionListener(e -> dlg.dispose());

        footer.add(btnSave);
        footer.add(btnCancel);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String title, JTextField field, JDialog parent) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(title);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);

        JButton btnKb = new JButton("⌨");
        btnKb.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        btnKb.setForeground(Color.WHITE);
        btnKb.setBackground(new Color(0, 75, 85));
        btnKb.setPreferredSize(new Dimension(48, 38));
        btnKb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnKb.addActionListener(e -> {
            TouchTextKeyboardDialog kb = new TouchTextKeyboardDialog(parent, title, field.getText());
            kb.setVisible(true);
            if (kb.isConfirmed() && kb.getResultText() != null) {
                field.setText(kb.getResultText());
            }
        });
        gbc.gridx = 2; gbc.weightx = 0;
        form.add(btnKb, gbc);
    }

    private void deleteUserAction() {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần xóa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("admin".equalsIgnoreCase(selectedUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản Quản trị viên mặc định!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this),
                "BẠN CÓ CHẮC CHẮN MUỐN XÓA NGƯỜI DÙNG<br>" + selectedUser.getUsername().toUpperCase() + " KHÔNG?")) {
            return;
        }

        userDAO.deleteUser(selectedUser.getId());
        refreshUsersList();
    }

    // Helper UI creators
    private static JLabel createBadge(String text, int width) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(BADGE_BROWN);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setPreferredSize(new Dimension(width, 42));
        l.setBorder(new LineBorder(new Color(190, 110, 110), 1, true));
        return l;
    }

    private static JButton createHeaderButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(110, 42));
        b.setBorder(new LineBorder(new Color(180, 220, 200), 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton createSidebarButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setPreferredSize(new Dimension(175, 44));
        b.setBorder(new LineBorder(Color.WHITE, 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton createPermissionPill(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setForeground(Color.WHITE);
        b.setBackground(active ? ACTIVE_RED : INACTIVE_GRAY);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(54, 38));
        b.setBorder(new LineBorder(Color.WHITE, 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton createGearButton() {
        JButton b = new JButton("⚙");
        b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        b.setForeground(new Color(0, 60, 80));
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(38, 38));
        b.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Package-private helpers for tests
    List<SGroupUser> getGroupList() {
        return groupList;
    }

    SGroupUser getSelectedGroup() {
        return selectedGroup;
    }

    void setSelectedGroup(SGroupUser g) {
        this.selectedGroup = g;
        loadRolesForCurrentGroup();
        refreshAllViews();
    }

    void setReportTab(boolean reportTab) {
        this.isReportTab = reportTab;
        updateTabStyles();
        refreshCategoryList();
        refreshPermissionsList();
    }

    boolean isReportTab() {
        return isReportTab;
    }

    Map<String, SGroupRole> getGroupRolesMap() {
        return groupRolesMap;
    }

    Map<String, SReportRole> getReportRolesMap() {
        return reportRolesMap;
    }
}
