package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;

public class LoaiHinhKhuyenMai {
    private String id;
    private String name;
    private String note;
    private int status = 30; // >= 0: Active, < 0: In trash
    private String iconName = "asterisk-yellow.png";
    private int sortOrder = 0;
    private Integer itemType = 0;
    private String parentId;
    private Timestamp timeCreated;
    private String userCreatedId;
    private Timestamp timeModified;
    private String userModifiedId;

    public LoaiHinhKhuyenMai() {}

    public LoaiHinhKhuyenMai(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public LoaiHinhKhuyenMai(String id, String name, String iconName) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Timestamp getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Timestamp timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getUserCreatedId() {
        return userCreatedId;
    }

    public void setUserCreatedId(String userCreatedId) {
        this.userCreatedId = userCreatedId;
    }

    public Timestamp getTimeModified() {
        return timeModified;
    }

    public void setTimeModified(Timestamp timeModified) {
        this.timeModified = timeModified;
    }

    public String getUserModifiedId() {
        return userModifiedId;
    }

    public void setUserModifiedId(String userModifiedId) {
        this.userModifiedId = userModifiedId;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
