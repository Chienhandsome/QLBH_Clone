package kazuko.ex.model.danhmuc;

public class NhomMatHang {
    private String id;
    private String code;
    private String name;
    private String ghiChu;
    private String parentId;
    private String iconName = "Folder.png";
    private int sortOrder = 0;
    private int status = 1;
    private boolean khongChoPhepGiamGia = false;

    public NhomMatHang() {}

    public NhomMatHang(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public NhomMatHang(String id, String code, String name, String iconName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.iconName = iconName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isKhongChoPhepGiamGia() { return khongChoPhepGiamGia; }
    public void setKhongChoPhepGiamGia(boolean khongChoPhepGiamGia) { this.khongChoPhepGiamGia = khongChoPhepGiamGia; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
