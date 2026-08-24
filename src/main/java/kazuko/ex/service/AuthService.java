package kazuko.ex.service;

import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SUser;

public class AuthService {
    private static SUser currentUser = null;
    private final UserDAO userDAO = new UserDAO();

    public SUser login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        SUser user = userDAO.findByUsername(username.trim());
        if (user != null) {
            // Kiểm tra mật khẩu (Nếu user chưa có pass hoặc khớp)
            String dbPass = user.getPassword() != null ? user.getPassword().trim() : "";
            String inputPass = password != null ? password.trim() : "";

            if (dbPass.isEmpty() || dbPass.equals(inputPass)) {
                currentUser = user;
                return user;
            }
        }
        return null;
    }

    public static SUser getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}
