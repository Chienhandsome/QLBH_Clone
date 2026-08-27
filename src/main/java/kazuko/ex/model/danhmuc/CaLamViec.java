package kazuko.ex.model.danhmuc;

public class CaLamViec {
    private String id;
    private String name;
    private double tiLeLuong = 100.0;
    private String ghiChu;
    private String avatarIcon = "chart_donut";
    private String parentId;
    private boolean folder = false;
    private boolean separator = false;
    private boolean isDefault = false;
    private int status = 1; // 1: Active, -1: In trash
    private int sortOrder = 0;

    public CaLamViec() {}

    public CaLamViec(String id, String name) {
        this.id = id;
        this.name = name;
        this.tiLeLuong = 100.0;
    }

    public CaLamViec(String id, String name, double tiLeLuong) {
        this.id = id;
        this.name = name;
        this.tiLeLuong = tiLeLuong;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTiLeLuong() { return tiLeLuong; }
    public void setTiLeLuong(double tiLeLuong) { this.tiLeLuong = tiLeLuong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getAvatarIcon() { return avatarIcon; }
    public void setAvatarIcon(String avatarIcon) { this.avatarIcon = avatarIcon; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public boolean isFolder() { return folder; }
    public void setFolder(boolean folder) { this.folder = folder; }

    public boolean isSeparator() { return separator; }
    public void setSeparator(boolean separator) { this.separator = separator; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    @Override
    public String toString() {
        if (separator) return "----------------------------------------";
        return name != null ? name : "";
    }
}
