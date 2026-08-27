package kazuko.ex.model.danhmuc;

public enum TieuChiPhanLoai {
    NHOM_MAT_HANG("Nhóm mặt hàng", "Folder.png"),
    DON_VI_TINH("Đơn vị tính", "1282529873_objects.png"),
    HANG_SAN_XUAT("Hãng sản xuất", "1282529873_objects.png"),
    DON_VI_TINH_CHAN("Đơn vị tính chẵn", "1282529873_objects.png"),
    DOI_TAC_KY_GUI("Đối tác ký gửi", "users.png");

    private final String title;
    private final String iconName;

    TieuChiPhanLoai(String title, String iconName) {
        this.title = title;
        this.iconName = iconName;
    }

    public String getTitle() {
        return title;
    }

    public String getIconName() {
        return iconName;
    }

    @Override
    public String toString() {
        return title;
    }
}
