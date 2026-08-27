package kazuko.ex.model.danhmuc;

public class NhanVien {
    private String id;
    private String name;
    private String diaChi;
    private String dienThoai;
    private String avatarIcon = "user_1";
    private int cachTinhLuong = 2; // 0: Theo ca, 1: Tháng theo ca, 2: Tháng theo ngày
    private double luongCa = 0.0;
    private double luongThang = 0.0;
    private boolean nghiThu7 = false;
    private boolean nghiChuNhat = false;
    private String ghiChu;
    private String parentId;
    private boolean folder = false;
    private boolean separator = false;
    private boolean isDefault = false;
    private int status = 1; // 1: Active, -1: In trash
    private int sortOrder = 0;

    public NhanVien() {}

    public NhanVien(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public NhanVien(String id, String name, String avatarIcon) {
        this.id = id;
        this.name = name;
        this.avatarIcon = avatarIcon;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getAvatarIcon() { return avatarIcon; }
    public void setAvatarIcon(String avatarIcon) { this.avatarIcon = avatarIcon; }

    public int getCachTinhLuong() { return cachTinhLuong; }
    public void setCachTinhLuong(int cachTinhLuong) { this.cachTinhLuong = cachTinhLuong; }

    public double getLuongCa() { return luongCa; }
    public void setLuongCa(double luongCa) { this.luongCa = luongCa; }

    public double getLuongThang() { return luongThang; }
    public void setLuongThang(double luongThang) { this.luongThang = luongThang; }

    public boolean isNghiThu7() { return nghiThu7; }
    public void setNghiThu7(boolean nghiThu7) { this.nghiThu7 = nghiThu7; }

    public boolean isNghiChuNhat() { return nghiChuNhat; }
    public void setNghiChuNhat(boolean nghiChuNhat) { this.nghiChuNhat = nghiChuNhat; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

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
