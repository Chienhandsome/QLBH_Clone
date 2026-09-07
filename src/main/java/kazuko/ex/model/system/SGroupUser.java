package kazuko.ex.model.system;

import java.sql.Timestamp;

public class SGroupUser {
    private String id;
    private String name;
    private String note;
    private String simageId;
    private String sortOrder;
    private String parentId;
    private Integer itemType;
    private Integer status = 30;
    private Timestamp timeCreated;
    private Timestamp timeModified;
    private String userCreatedId;
    private String userModifiedId;

    public SGroupUser() {}

    public SGroupUser(String id, String name, String note, String simageId) {
        this.id = id;
        this.name = name;
        this.note = note;
        this.simageId = simageId;
        this.status = 30;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getSimageId() { return simageId; }
    public void setSimageId(String simageId) { this.simageId = simageId; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public Integer getItemType() { return itemType; }
    public void setItemType(Integer itemType) { this.itemType = itemType; }

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

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
