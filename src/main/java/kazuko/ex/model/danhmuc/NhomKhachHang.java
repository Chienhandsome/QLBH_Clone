package kazuko.ex.model.danhmuc;

import java.util.ArrayList;
import java.util.List;

public class NhomKhachHang {
    private String id;
    private String name;
    private String iconName = "users.png";
    private double diemTichLuy = 0;
    private double tiLeGiamGia = 0;
    private String ghiChu;
    private String parentId;
    private boolean folder = false;
    private boolean separator = false;
    private int sortOrder = 0;
    private int status = 1; // 1: Active, -1: In trash
    private Integer itemType = 0; // 0: Normal item, 1: Folder, 2: Separator

    private List<NhomKhachHangGiamGia> listGiamGiaNhomHang = new ArrayList<>();

    public NhomKhachHang() {}

    public NhomKhachHang(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public NhomKhachHang(String id, String name, String iconName) {
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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public double getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(double diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public double getTiLeGiamGia() {
        return tiLeGiamGia;
    }

    public void setTiLeGiamGia(double tiLeGiamGia) {
        this.tiLeGiamGia = tiLeGiamGia;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
        if (folder) {
            this.itemType = 1;
            this.iconName = "Folder.png";
        }
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
        if (separator) {
            this.itemType = 2;
        }
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
        if (itemType != null) {
            if (itemType == 1) {
                this.folder = true;
                this.separator = false;
            } else if (itemType == 2) {
                this.separator = true;
                this.folder = false;
            } else {
                this.folder = false;
                this.separator = false;
            }
        }
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<NhomKhachHangGiamGia> getListGiamGiaNhomHang() {
        return listGiamGiaNhomHang;
    }

    public void setListGiamGiaNhomHang(List<NhomKhachHangGiamGia> listGiamGiaNhomHang) {
        this.listGiamGiaNhomHang = listGiamGiaNhomHang != null ? listGiamGiaNhomHang : new ArrayList<>();
    }

    @Override
    public String toString() {
        if (separator) return "----------------------------------------";
        return name != null ? name : "";
    }
}
