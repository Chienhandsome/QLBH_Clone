package kazuko.ex.model.system;

import java.sql.Timestamp;

public class SGroupRole {
    public static final int MASK_LOCK = 0;
    public static final int MASK_VIEW = 16;   // 0x10
    public static final int MASK_ADD = 32;    // 0x20
    public static final int MASK_EDIT = 64;   // 0x40
    public static final int MASK_DELETE = 128;// 0x80
    public static final int MASK_ALL = 240;   // 16 + 32 + 64 + 128 = 240 (0xF0)

    private String id;
    private String groupUserId;
    private String functionId;
    private Integer mode = 0;
    private Integer status = 30;
    private Timestamp timeCreated;
    private Timestamp timeModified;
    private String userCreatedId;
    private String userModifiedId;

    public SGroupRole() {}

    public SGroupRole(String id, String groupUserId, String functionId, Integer mode) {
        this.id = id;
        this.groupUserId = groupUserId;
        this.functionId = functionId;
        this.mode = mode != null ? mode : 0;
        this.status = 30;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupUserId() { return groupUserId; }
    public void setGroupUserId(String groupUserId) { this.groupUserId = groupUserId; }

    public String getFunctionId() { return functionId; }
    public void setFunctionId(String functionId) { this.functionId = functionId; }

    public Integer getMode() { return mode != null ? mode : 0; }
    public void setMode(Integer mode) { this.mode = mode != null ? mode : 0; }

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

    // Bitmask helpers
    public boolean isLock() {
        return getMode() == 0;
    }

    public void setLock(boolean lock) {
        if (lock) {
            this.mode = 0;
        } else if (this.mode == 0) {
            this.mode = MASK_VIEW;
        }
    }

    public boolean isView() {
        return (getMode() & MASK_VIEW) != 0;
    }

    public void setView(boolean val) {
        if (val) {
            this.mode = getMode() | MASK_VIEW;
        } else {
            this.mode = getMode() & ~MASK_VIEW;
            // If view is removed, remove add, edit, delete as well
            this.mode = this.mode & ~MASK_ADD & ~MASK_EDIT & ~MASK_DELETE;
        }
    }

    public boolean isAdd() {
        return (getMode() & MASK_ADD) != 0;
    }

    public void setAdd(boolean val) {
        if (val) {
            this.mode = (getMode() | MASK_ADD) | MASK_VIEW;
        } else {
            this.mode = getMode() & ~MASK_ADD;
        }
    }

    public boolean isEdit() {
        return (getMode() & MASK_EDIT) != 0;
    }

    public void setEdit(boolean val) {
        if (val) {
            this.mode = (getMode() | MASK_EDIT) | MASK_VIEW;
        } else {
            this.mode = getMode() & ~MASK_EDIT;
        }
    }

    public boolean isDelete() {
        return (getMode() & MASK_DELETE) != 0;
    }

    public void setDelete(boolean val) {
        if (val) {
            this.mode = (getMode() | MASK_DELETE) | MASK_VIEW;
        } else {
            this.mode = getMode() & ~MASK_DELETE;
        }
    }

    public boolean isAll() {
        return isView() && isAdd() && isEdit() && isDelete();
    }

    public void setAll(boolean val) {
        if (val) {
            this.mode = MASK_ALL;
        } else {
            this.mode = 0;
        }
    }
}
