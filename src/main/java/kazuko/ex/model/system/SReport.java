package kazuko.ex.model.system;

import java.sql.Timestamp;

public class SReport {
    private String id;
    private String name;
    private String note;
    private Integer status = 30;
    private String sortOrder;
    private String parentId;
    private String parentDir;
    private Integer itemType; // 1: Folder, 0: Report leaf
    private String simageId;
    private Timestamp timeCreated;
    private Timestamp timeModified;
    private String userCreatedId;
    private String userModifiedId;

    public SReport() {}

    public SReport(String id, String name, String parentId, Integer itemType, String sortOrder) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.itemType = itemType;
        this.sortOrder = sortOrder;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getParentDir() { return parentDir; }
    public void setParentDir(String parentDir) { this.parentDir = parentDir; }

    public Integer getItemType() { return itemType; }
    public void setItemType(Integer itemType) { this.itemType = itemType; }

    public boolean isFolder() {
        return itemType != null && itemType == 1;
    }

    public String getSimageId() { return simageId; }
    public void setSimageId(String simageId) { this.simageId = simageId; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
