package kazuko.ex.dao;

import kazuko.ex.model.system.*;
import kazuko.ex.service.PermissionService;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserPermissionTest {

    @Test
    public void testGroupUserDAO() {
        GroupUserDAO dao = new GroupUserDAO();
        List<SGroupUser> groups = dao.getAllGroups();
        assertNotNull(groups);
        System.out.println("Groups count: " + groups.size());
        for (SGroupUser g : groups) {
            System.out.println("  - Group: " + g.getName() + " (ID=" + g.getId() + ")");
        }
    }

    @Test
    public void testFunctionAndRoles() {
        FunctionDAO fDao = new FunctionDAO();
        List<SFunction> funcs = fDao.getAllFunctions();
        assertNotNull(funcs);
        assertFalse(funcs.isEmpty());
        System.out.println("Functions count: " + funcs.size());

        List<String> groupNames = fDao.getDistinctGroupNames();
        System.out.println("Function Groups: " + groupNames);

        GroupRoleDAO roleDao = new GroupRoleDAO();
        GroupUserDAO gDao = new GroupUserDAO();
        List<SGroupUser> groups = gDao.getAllGroups();
        if (!groups.isEmpty()) {
            String testGroupId = groups.get(0).getId();
            Map<String, SGroupRole> roleMap = roleDao.getRolesMapByGroupId(testGroupId);
            System.out.println("Roles map size for group " + groups.get(0).getName() + ": " + roleMap.size());
        }
    }

    @Test
    public void testReportAndRoles() {
        ReportDAO repDao = new ReportDAO();
        List<SReport> reports = repDao.getAllReports();
        assertNotNull(reports);
        System.out.println("Reports count: " + reports.size());

        ReportRoleDAO repRoleDao = new ReportRoleDAO();
        GroupUserDAO gDao = new GroupUserDAO();
        List<SGroupUser> groups = gDao.getAllGroups();
        if (!groups.isEmpty()) {
            String testGroupId = groups.get(0).getId();
            Map<String, SReportRole> repRoleMap = repRoleDao.getReportRolesMapByGroupId(testGroupId);
            System.out.println("Report roles map size: " + repRoleMap.size());
        }
    }

    @Test
    public void testUserDAO() {
        UserDAO userDao = new UserDAO();
        List<SUser> nonAdminUsers = userDao.getAllNonAdminUsers();
        assertNotNull(nonAdminUsers);
        System.out.println("Non-admin users count: " + nonAdminUsers.size());
        for (SUser u : nonAdminUsers) {
            System.out.println("  - User: " + u.getUsername() + " | " + u.getName() + " | Group=" + u.getGroupUserName());
            List<UserStoreAccess> access = userDao.getUserStoreAccess(u.getId());
            System.out.println("    Store access count: " + access.size());
        }
    }

    @Test
    public void testFullCrudAndCascadeDelete() {
        GroupUserDAO groupDao = new GroupUserDAO();
        UserDAO userDao = new UserDAO();
        GroupRoleDAO roleDao = new GroupRoleDAO();
        ReportRoleDAO reportRoleDao = new ReportRoleDAO();
        FunctionDAO funcDao = new FunctionDAO();
        ReportDAO repDao = new ReportDAO();

        // 1. Create a test group
        SGroupUser testGroup = new SGroupUser();
        testGroup.setName("Nhóm Test Phân Quyền " + System.currentTimeMillis());
        testGroup.setNote("Ghi chú nhóm test");
        testGroup.setSimageId("star");
        boolean groupCreated = groupDao.insertGroup(testGroup);
        assertTrue(groupCreated, "Insert group should succeed");
        assertNotNull(testGroup.getId());

        String groupId = testGroup.getId();

        // 2. Set function roles for test group
        List<SFunction> funcs = funcDao.getAllFunctions();
        assertFalse(funcs.isEmpty());
        String funcId1 = funcs.get(0).getId();
        boolean roleSaved = roleDao.saveOrUpdateRole(groupId, funcId1, SGroupRole.MASK_ALL);
        assertTrue(roleSaved, "Save function role should succeed");

        Map<String, SGroupRole> roles = roleDao.getRolesMapByGroupId(groupId);
        assertTrue(roles.containsKey(funcId1));
        assertEquals(SGroupRole.MASK_ALL, roles.get(funcId1).getMode());

        // 3. Set report role for test group
        List<SReport> reports = repDao.getAllReports();
        assertFalse(reports.isEmpty());
        String reportId1 = reports.get(0).getId();
        boolean reportRoleSaved = reportRoleDao.saveOrUpdateReportRole(groupId, reportId1, SReportRole.MODE_ALLOWED);
        assertTrue(reportRoleSaved, "Save report role should succeed");

        Map<String, SReportRole> repRoles = reportRoleDao.getReportRolesMapByGroupId(groupId);
        assertTrue(repRoles.containsKey(reportId1));
        assertEquals(SReportRole.MODE_ALLOWED, repRoles.get(reportId1).getMode());

        // 4. Create a test user in this group
        SUser testUser = new SUser();
        testUser.setUsername("testuser_" + System.currentTimeMillis() % 10000);
        testUser.setName("Người Dùng Test");
        testUser.setPassword("123456");
        testUser.setEmail("testuser@example.com");
        testUser.setGroupUserId(groupId);
        testUser.setUserId("99");
        testUser.setCardCode("99");

        List<UserStoreAccess> allStores = userDao.getUserStoreAccess(null);
        List<String> storeIds = new ArrayList<>();
        if (!allStores.isEmpty()) {
            storeIds.add(allStores.get(0).getStoreId());
        }

        boolean userCreated = userDao.insertUser(testUser, storeIds);
        assertTrue(userCreated, "Insert user should succeed");
        assertNotNull(testUser.getId());

        String userId = testUser.getId();

        // Verify user and store access
        SUser loadedUser = userDao.findById(userId);
        assertNotNull(loadedUser);
        assertEquals("Người Dùng Test", loadedUser.getName());

        List<UserStoreAccess> userStores = userDao.getUserStoreAccess(userId);
        assertFalse(userStores.isEmpty());
        assertTrue(userStores.get(0).isAccessed());

        // 5. Test Cascade Delete Group: should delete user, store mappings, group roles and report roles
        boolean deleted = groupDao.deleteGroupCascade(groupId);
        assertTrue(deleted, "Cascade delete group should succeed");

        // Verify group is deleted
        assertNull(groupDao.getGroupById(groupId));

        // Verify user is deleted
        assertNull(userDao.findById(userId));

        // Verify roles are deleted
        Map<String, SGroupRole> rolesAfter = roleDao.getRolesMapByGroupId(groupId);
        assertTrue(rolesAfter.isEmpty(), "Group roles should be empty after cascade delete");

        Map<String, SReportRole> repRolesAfter = reportRoleDao.getReportRolesMapByGroupId(groupId);
        assertTrue(repRolesAfter.isEmpty(), "Report roles should be empty after cascade delete");

        System.out.println(">> Full CRUD & Cascade Delete Integration Test passed successfully!");
    }

    @Test
    public void testPermissionServiceHidingLogic() {
        GroupUserDAO groupDao = new GroupUserDAO();
        UserDAO userDao = new UserDAO();
        GroupRoleDAO roleDao = new GroupRoleDAO();
        ReportRoleDAO reportRoleDao = new ReportRoleDAO();
        FunctionDAO funcDao = new FunctionDAO();
        ReportDAO repDao = new ReportDAO();

        // 1. Admin user has full access to everything
        SUser admin = new SUser();
        admin.setUsername("Admin");
        admin.setIsAdmin(1);
        PermissionService.getInstance().initPermissions(admin);
        assertTrue(PermissionService.getInstance().isFunctionAllowed("Hóa đơn bán hàng"));
        assertTrue(PermissionService.getInstance().isFunctionAllowed("Tồn kho"));
        assertTrue(PermissionService.getInstance().isFunctionAllowed("Quản lý người dùng"));
        assertTrue(PermissionService.getInstance().isReportAllowed("BÁO CÁO TỒN QUỸ"));

        // 2. Create a test group with specific locked & allowed roles
        SGroupUser testGroup = new SGroupUser();
        testGroup.setName("Nhóm Test Khóa Tính Năng " + System.currentTimeMillis());
        testGroup.setNote("Ghi chú");
        assertTrue(groupDao.insertGroup(testGroup));
        String groupId = testGroup.getId();

        try {
            List<SFunction> funcs = funcDao.getAllFunctions();
            SFunction funcHoaDon = null;
            SFunction funcTonKho = null;
            for (SFunction f : funcs) {
                if ("Hóa đơn bán hàng".equalsIgnoreCase(f.getName())) {
                    funcHoaDon = f;
                } else if ("Tồn kho".equalsIgnoreCase(f.getName())) {
                    funcTonKho = f;
                }
            }
            assertNotNull(funcHoaDon, "Function 'Hóa đơn bán hàng' should exist");
            assertNotNull(funcTonKho, "Function 'Tồn kho' should exist");

            // Lock 'Hóa đơn bán hàng' (mode = 0) and allow 'Tồn kho' (mode = MASK_ALL)
            roleDao.saveOrUpdateRole(groupId, funcHoaDon.getId(), 0);
            roleDao.saveOrUpdateRole(groupId, funcTonKho.getId(), SGroupRole.MASK_ALL);

            // Report testing
            List<SReport> reports = repDao.getAllReports();
            assertFalse(reports.isEmpty());
            SReport testReport = reports.get(0);
            reportRoleDao.saveOrUpdateReportRole(groupId, testReport.getId(), SReportRole.MODE_LOCKED);

            // Create non-admin user in this group
            SUser user = new SUser();
            user.setUsername("locked_test_" + System.currentTimeMillis() % 10000);
            user.setName("Nhân viên Bị Khóa Tính Năng");
            user.setPassword("123456");
            user.setGroupUserId(groupId);
            user.setIsAdmin(0);
            assertTrue(userDao.insertUser(user, Collections.emptyList()));

            // Initialize permissions for this user
            PermissionService.getInstance().initPermissions(user);

            // 3. Verify locked functions are NOT allowed
            assertFalse(PermissionService.getInstance().isFunctionAllowed("Hóa đơn bán hàng"),
                    "Locked function 'Hóa đơn bán hàng' must NOT be allowed");
            assertFalse(PermissionService.getInstance().isFunctionAllowed(funcHoaDon.getId()),
                    "Locked function ID must NOT be allowed");

            // 4. Verify permitted function IS allowed
            assertTrue(PermissionService.getInstance().isFunctionAllowed("Tồn kho"),
                    "Permitted function 'Tồn kho' must be allowed");
            assertTrue(PermissionService.getInstance().isFunctionAllowed(funcTonKho.getId()),
                    "Permitted function ID must be allowed");

            // 5. Verify locked report is NOT allowed
            assertFalse(PermissionService.getInstance().isReportAllowed(testReport.getId()),
                    "Locked report ID must NOT be allowed");
            assertFalse(PermissionService.getInstance().isReportAllowed(testReport.getName()),
                    "Locked report name must NOT be allowed");

            // 6. Verify alias matching (e.g. 'nguoidungvaphanquyen', 'nhaphangvaokho')
            assertNotNull(PermissionService.normalize("Người Dùng Và Phân Quyền"));
            assertEquals("nguoidungvaphanquyen", PermissionService.normalize("Người Dùng Và Phân Quyền"));

            System.out.println(">> Permission hiding logic test passed successfully!");
        } finally {
            groupDao.deleteGroupCascade(groupId);
        }
    }
}
