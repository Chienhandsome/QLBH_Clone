package kazuko.ex.model.nhansu;

public class LyDoThuongPhat {
    private String id;
    private String name;
    private String avatarIcon;
    private String ghiChu;
    private int status = 1; // 1: active, -1: trash
    private boolean isDefault = false;

    public LyDoThuongPhat() {}

    public LyDoThuongPhat(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public LyDoThuongPhat(String id, String name, String avatarIcon, String ghiChu) {
        this.id = id;
        this.name = name;
        this.avatarIcon = avatarIcon;
        this.ghiChu = ghiChu;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarIcon() { return avatarIcon; }
    public void setAvatarIcon(String avatarIcon) { this.avatarIcon = avatarIcon; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
