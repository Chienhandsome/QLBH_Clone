package kazuko.ex.dao;

import kazuko.ex.model.system.ActivityLog;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityLogDAOIntegrationTest {

    @Test
    void createsStoresSearchesAndDeletesAnAuditEntry() {
        ActivityLogDAO dao = new ActivityLogDAO();
        Date now = new Date();
        String id = UUID.randomUUID().toString();
        String reference = "AUDIT-TEST-" + id.substring(0, 8);

        ActivityLog log = new ActivityLog();
        log.setId(id);
        log.setEventTime(now);
        log.setUserId("test-user");
        log.setUserName("Kiểm thử");
        log.setAction("KIỂM TRA");
        log.setModule("Lưu vết hoạt động");
        log.setReferenceNo(reference);
        log.setContent("Kiểm tra ghi và tìm nhật ký");
        log.setDeviceName("JUnit");

        assertTrue(dao.insert(log));
        try {
            List<ActivityLog> matches = dao.search(
                    new Date(now.getTime() - 60_000), new Date(now.getTime() + 60_000),
                    "Kiểm thử", reference, "ghi và tìm");
            assertEquals(1, matches.size());
            assertEquals(id, matches.get(0).getId());
        } finally {
            assertEquals(1, dao.deleteByIds(List.of(id)));
        }
    }
}
