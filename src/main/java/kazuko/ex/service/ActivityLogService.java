package kazuko.ex.service;

import kazuko.ex.dao.ActivityLogDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.ActivityLog;
import kazuko.ex.model.system.SUser;

import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;

public final class ActivityLogService {
    private static final ActivityLogDAO DAO = new ActivityLogDAO();

    private ActivityLogService() {}

    public static void log(String module, String action, String referenceNo, String content) {
        try {
            if (!new SystemConfigDAO().loadConfig().isKichHoatLuuVetHoatDong()) return;

            SUser user = AuthService.getCurrentUser();
            ActivityLog log = new ActivityLog();
            log.setId(UUID.randomUUID().toString());
            log.setEventTime(new Date());
            log.setUserId(user != null ? user.getId() : null);
            log.setUserName(resolveUserName(user));
            log.setAction(limit(action, 50));
            log.setModule(limit(module, 100));
            log.setReferenceNo(limit(referenceNo, 100));
            log.setContent(limit(content, 1000));
            log.setDeviceName(limit(resolveDeviceName(), 100));
            DAO.insert(log);
        } catch (Exception e) {
            // Lưu vết không được phép làm hỏng giao dịch nghiệp vụ chính.
            System.err.println("Lỗi dịch vụ lưu vết: " + e.getMessage());
        }
    }

    static String resolveUserName(SUser user) {
        if (user == null) return "Hệ thống";
        if (user.getName() != null && !user.getName().trim().isEmpty()) return user.getName().trim();
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) return user.getUsername().trim();
        return "Hệ thống";
    }

    static String limit(String value, int maxLength) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private static String resolveDeviceName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            String computerName = System.getenv("COMPUTERNAME");
            return computerName != null ? computerName : "Không xác định";
        }
    }
}
