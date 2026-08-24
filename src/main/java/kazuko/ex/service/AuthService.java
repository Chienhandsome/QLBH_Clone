package kazuko.ex.service;

import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SUser;

import java.sql.SQLException;
import java.util.List;

public class AuthService {
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
        }
        return ok;
    }

    public static SUser getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}
