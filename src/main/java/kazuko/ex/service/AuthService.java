package kazuko.ex.service;

import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SUser;

import java.sql.SQLException;
import java.util.List;

public class AuthService {
    public static final String DEFAULT_TOUCH_CODE = "123";
    private static SUser currentUser = null;
    private final UserDAO userDAO = new UserDAO();

    public static class AuthResult {
        private final boolean success;
        private final SUser user;
        private final String message;

        public AuthResult(boolean success, SUser user, String message) {
            this.success = success;
            this.user = user;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public SUser getUser() { return user; }
        public String getMessage() { return message; }
    }

    public AuthResult authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, null, "Vui lòng nhập tên người dùng!");
        }

        String trimmedUser = username.trim();
        String inputPass = password != null ? password.trim() : "";

        try {
            SUser user = userDAO.findByUsernameChecked(trimmedUser);
            if (user != null) {
                String dbPass = user.getPassword() != null ? user.getPassword().trim() : "";
                if (dbPass.isEmpty() || dbPass.equals(inputPass)) {
                    currentUser = user;
                    PermissionService.getInstance().initPermissions(user);
                    ActivityLogService.log("Hệ thống", "ĐĂNG NHẬP", trimmedUser,
                            "Đăng nhập hệ thống thành công");
                    return new AuthResult(true, user, "Đăng nhập thành công!");
                } else {
                    return new AuthResult(false, null, "Mật khẩu không chính xác!");
                }
            } else {
                // Fallback cho tài khoản Admin nếu CSDL chưa có dữ liệu tài khoản
                if ("admin".equalsIgnoreCase(trimmedUser)) {
                    List<SUser> allUsers = userDAO.getAll();
                    if (allUsers.isEmpty()) {
                        SUser defaultAdmin = new SUser();
                        defaultAdmin.setId("1");
                        defaultAdmin.setUsername("Admin");
                        defaultAdmin.setName("Administrator");
                        defaultAdmin.setIsAdmin(1);
                        currentUser = defaultAdmin;
                        PermissionService.getInstance().initPermissions(defaultAdmin);
                        ActivityLogService.log("Hệ thống", "ĐĂNG NHẬP", trimmedUser,
                                "Đăng nhập bằng tài khoản quản trị mặc định");
                        return new AuthResult(true, defaultAdmin, "Đăng nhập với tài khoản Admin mặc định!");
                    }
                }
                return new AuthResult(false, null, "Tài khoản '" + trimmedUser + "' không tồn tại trong hệ thống!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL khi xác thực: " + e.getMessage());
            return new AuthResult(false, null, "Không thể kết nối đến cơ sở dữ liệu!\nVui lòng kiểm tra lại cấu hình CSDL hoặc máy chủ Firebird.");
        }
    }

    public SUser login(String username, String password) {
        AuthResult result = authenticate(username, password);
        return result.isSuccess() ? result.getUser() : null;
    }

    /**
     * Authenticates the compact numeric code used by the touch login screen.
     * A configured CARDCODE always wins. Code 123 is the initial Admin fallback
     * requested for terminals whose database has not configured touch codes.
     */
    public AuthResult authenticateByTouchCode(String code) {
        String inputCode = code != null ? code.trim() : "";
        if (inputCode.isEmpty()) {
            return new AuthResult(false, null, "Vui lòng nhập mã đăng nhập!");
        }

        List<SUser> users = userDAO.getAll();
        for (SUser user : users) {
            if (user.getCardCode() != null && inputCode.equals(user.getCardCode().trim())) {
                return establishSession(user, "MÃ CẢM ỨNG");
            }
        }

        if (DEFAULT_TOUCH_CODE.equals(inputCode)) {
            try {
                SUser admin = userDAO.findByUsernameChecked("Admin");
                if (admin != null) {
                    return establishSession(admin, "MÃ MẶC ĐỊNH");
                }
                SUser defaultAdmin = new SUser();
                defaultAdmin.setId("1");
                defaultAdmin.setUsername("Admin");
                defaultAdmin.setName("Administrator");
                defaultAdmin.setIsAdmin(1);
                return establishSession(defaultAdmin, "MÃ MẶC ĐỊNH");
            } catch (SQLException e) {
                return new AuthResult(false, null,
                        "Không thể kết nối đến cơ sở dữ liệu để xác thực mã đăng nhập!");
            }
        }
        return new AuthResult(false, null, "Mã đăng nhập không chính xác!");
    }

    public AuthResult authenticateByCardCode(String cardCode) {
        String inputCode = cardCode != null ? cardCode.trim() : "";
        if (inputCode.isEmpty()) {
            return new AuthResult(false, null, "Vui lòng quét hoặc nhập mã thẻ!");
        }
        for (SUser user : userDAO.getAll()) {
            if (user.getCardCode() != null && inputCode.equals(user.getCardCode().trim())) {
                return establishSession(user, "THẺ CẢM ỨNG");
            }
        }
        return new AuthResult(false, null, "Thẻ đăng nhập không hợp lệ!");
    }

    private AuthResult establishSession(SUser user, String method) {
        currentUser = user;
        PermissionService.getInstance().initPermissions(user);
        ActivityLogService.log("Hệ thống", "ĐĂNG NHẬP", user.getUsername(),
                "Đăng nhập hệ thống bằng " + method.toLowerCase());
        return new AuthResult(true, user, "Đăng nhập thành công!");
    }

    public List<SUser> getAllUsers() {
        return userDAO.getAll();
    }

    public boolean changePassword(SUser user, String oldPassword, String newPassword) {
        if (user == null) return false;
        String currentPass = user.getPassword() != null ? user.getPassword().trim() : "";
        String inputOld = oldPassword != null ? oldPassword.trim() : "";
        if (!currentPass.isEmpty() && !currentPass.equals(inputOld)) {
            return false;
        }
        boolean ok = userDAO.updatePassword(user.getId(), newPassword != null ? newPassword.trim() : "");
        if (ok) {
            user.setPassword(newPassword != null ? newPassword.trim() : "");
            ActivityLogService.log("Tài khoản", "ĐỔI MẬT KHẨU", user.getUsername(),
                    "Đổi mật khẩu tài khoản");
        }
        return ok;
    }

    public static SUser getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        SUser user = currentUser;
        if (user != null) {
            ActivityLogService.log("Hệ thống", "ĐĂNG XUẤT", user.getUsername(),
                    "Đăng xuất hệ thống");
        }
        currentUser = null;
        PermissionService.getInstance().initPermissions(null);
    }
}
