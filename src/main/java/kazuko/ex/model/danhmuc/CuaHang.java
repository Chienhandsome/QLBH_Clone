package kazuko.ex.model.danhmuc;

public class CuaHang {
    private String id;
    private String code;
    private String name;
    private String diaChi;
    private String dienThoai;
    private String khoHangId;
    private String khoHangName;
    private String mauHoaDon = "Mẫu 54 mm x 2 dòng";
    private String ghiChu;
    private String parentId;
    private boolean folder = false;
    private boolean separator = false;
    private String iconName = "branch"; // "branch", "headquarter", "folder"
    private boolean isDefault = false;
    private int status = 1; // 1: Active, -1: In trash
    private int sortOrder = 0;

    public CuaHang() {}

    public CuaHang(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getKhoHangId() { return khoHangId; }
    public void setKhoHangId(String khoHangId) { this.khoHangId = khoHangId; }

    public String getKhoHangName() { return khoHangName; }
    public void setKhoHangName(String khoHangName) { this.khoHangName = khoHangName; }

    public String getMauHoaDon() { return mauHoaDon; }
    public void setMauHoaDon(String mauHoaDon) { this.mauHoaDon = mauHoaDon; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public boolean isFolder() { return folder; }
    public void setFolder(boolean folder) { this.folder = folder; }

    public boolean isSeparator() { return separator; }
    public void setSeparator(boolean separator) { this.separator = separator; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

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
