package kazuko.ex.model.system;

import java.sql.Timestamp;

public class UserStoreAccess {
    private String id;
    private String userId;
    private String storeId;
    private String storeName;
    private String storeCode;
    private boolean accessed = false;
    private Integer status = 30;
    private String note;
    private Timestamp timeCreated;
    private Timestamp timeModified;
    private String userCreatedId;
    private String userModifiedId;

    public UserStoreAccess() {}

    public UserStoreAccess(String storeId, String storeName, String storeCode, boolean accessed) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeCode = storeCode;
        this.accessed = accessed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public boolean isAccessed() { return accessed; }
    public void setAccessed(boolean accessed) { this.accessed = accessed; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }
}
