package kazuko.ex.model.system;

public class SUser {
    private String id;
    private String username;
    private String name;
    private String password;
    private String email;
    private Integer isAdmin;
    private String groupUserId;

    private String groupUserName;
    private String dNhanVienId;
    private String dNhanVienName;
    private String userId;
    private String cardCode;
    private String note;
    private String simageId;
    private Integer status = 30;
    private java.sql.Timestamp timeCreated;
    private java.sql.Timestamp timeModified;
    private String userCreatedId;
    private String userModifiedId;

    public SUser() {}

    public SUser(String id, String username, String name, String password, String email, Integer isAdmin, String groupUserId) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
        this.groupUserId = groupUserId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Integer isAdmin) { this.isAdmin = isAdmin; }

    public String getGroupUserId() { return groupUserId; }
    public void setGroupUserId(String groupUserId) { this.groupUserId = groupUserId; }

    public String getGroupUserName() { return groupUserName; }
    public void setGroupUserName(String groupUserName) { this.groupUserName = groupUserName; }

    public String getdNhanVienId() { return dNhanVienId; }
    public void setdNhanVienId(String dNhanVienId) { this.dNhanVienId = dNhanVienId; }

    public String getdNhanVienName() { return dNhanVienName; }
    public void setdNhanVienName(String dNhanVienName) { this.dNhanVienName = dNhanVienName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getSimageId() { return simageId; }
    public void setSimageId(String simageId) { this.simageId = simageId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public java.sql.Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(java.sql.Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public java.sql.Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(java.sql.Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    public boolean isAdminUser() {
        return (isAdmin != null && isAdmin == 1) || "Admin".equalsIgnoreCase(username);
    }

    @Override
    public String toString() {
        return name != null && !name.isEmpty() ? name : (username != null ? username : "");
    }
}
