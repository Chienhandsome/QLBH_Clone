package kazuko.ex.model.system;

public class SUser {
    private String id;
    private String username;
    private String name;
    private String password;
    private String email;
    private Integer isAdmin;
    private String groupUserId;

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

    public boolean isAdminUser() {
        return isAdmin != null && isAdmin == 1;
    }

    @Override
    public String toString() {
        return name != null && !name.isEmpty() ? name : username;
    }
}
