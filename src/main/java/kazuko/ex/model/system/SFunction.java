package kazuko.ex.model.system;

import java.sql.Timestamp;

public class SFunction {
    private String id;
    private String name;
    private String note;
    private Integer status = 30;
    private Integer sortOrder;
    private String groupName;
    private String parentId;
    private String parentDir;
    private Integer itemType;
    private String simageId;
    private Timestamp timeCreated;
    private Timestamp timeModified;
    private String userCreatedId;
    private String userModifiedId;

    public SFunction() {}

    public SFunction(String id, String name, String groupName, String note, Integer sortOrder) {
        this.id = id;
        this.name = name;
        this.groupName = groupName;
        this.note = note;
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

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getParentDir() { return parentDir; }
    public void setParentDir(String parentDir) { this.parentDir = parentDir; }

    public Integer getItemType() { return itemType; }
    public void setItemType(Integer itemType) { this.itemType = itemType; }

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

    public boolean isViewOnly() {
        return "View".equalsIgnoreCase(note != null ? note.trim() : "");
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
