package kazuko.ex.model.system;

import java.sql.Timestamp;

public class SReportRole {
    public static final int MODE_LOCKED = 0;
    public static final int MODE_ALLOWED = 30;

    private String id;
    private String groupUserId;
    private String reportId;
    private Integer mode = MODE_ALLOWED;
    private Integer status = 30;
    private Timestamp timeCreated;
    private Timestamp timeModified;
    private String userCreatedId;
    private String userModifiedId;

    public SReportRole() {}

    public SReportRole(String id, String groupUserId, String reportId, Integer mode) {
        this.id = id;
        this.groupUserId = groupUserId;
        this.reportId = reportId;
        this.mode = mode != null ? mode : MODE_ALLOWED;
        this.status = 30;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupUserId() { return groupUserId; }
    public void setGroupUserId(String groupUserId) { this.groupUserId = groupUserId; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public Integer getMode() { return mode != null ? mode : MODE_ALLOWED; }
    public void setMode(Integer mode) { this.mode = mode != null ? mode : MODE_ALLOWED; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    public boolean isAllowed() {
        return getMode() == MODE_ALLOWED || getMode() > 0;
    }

    public void setAllowed(boolean allowed) {
        this.mode = allowed ? MODE_ALLOWED : MODE_LOCKED;
    }
}
