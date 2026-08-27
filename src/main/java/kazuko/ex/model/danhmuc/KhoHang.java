package kazuko.ex.model.danhmuc;

public class KhoHang {
    private String id;
    private String name;
    private String note;
    private String cuaHangId;
    private boolean choPhepAmKho = false;
    private String avatarIcon = "warehouse";
    private String parentId;
    private boolean folder = false;
    private boolean separator = false;
    private boolean isDefault = false;
    private int status = 1; // 1: Active, -1: In trash
    private int sortOrder = 0;

    public KhoHang() {}

    public KhoHang(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public KhoHang(String id, String name, String cuaHangId) {
        this.id = id;
        this.name = name;
        this.cuaHangId = cuaHangId;
    }

    public KhoHang(String id, String name, String cuaHangId, String avatarIcon) {
        this.id = id;
        this.name = name;
        this.cuaHangId = cuaHangId;
        this.avatarIcon = avatarIcon;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCuaHangId() { return cuaHangId; }
    public void setCuaHangId(String cuaHangId) { this.cuaHangId = cuaHangId; }

    public boolean isChoPhepAmKho() { return choPhepAmKho; }
    public void setChoPhepAmKho(boolean choPhepAmKho) { this.choPhepAmKho = choPhepAmKho; }

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
