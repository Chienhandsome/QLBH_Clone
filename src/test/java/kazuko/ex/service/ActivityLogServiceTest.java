package kazuko.ex.service;

import kazuko.ex.model.system.SUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivityLogServiceTest {

    @Test
    void resolvesDisplayNameThenUsernameThenSystem() {
        SUser user = new SUser();
        user.setName("  Nguyễn Văn A  ");
        user.setUsername("nva");
        assertEquals("Nguyễn Văn A", ActivityLogService.resolveUserName(user));

        user.setName(" ");
        assertEquals("nva", ActivityLogService.resolveUserName(user));
        assertEquals("Hệ thống", ActivityLogService.resolveUserName(null));
    }

    @Test
    void trimsAndLimitsDatabaseText() {
        assertEquals("nội dung", ActivityLogService.limit("  nội dung  ", 20));
        assertEquals("12345", ActivityLogService.limit("123456789", 5));
        assertEquals("", ActivityLogService.limit(null, 10));
    }
}
