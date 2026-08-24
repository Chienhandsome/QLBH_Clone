package kazuko.ex.model.danhmuc;

public class MatHang {
    private String id;
    private String code;
    private String name;
    private double giaNhap;
    private double giaBan;
    private double giaVon;
    private String nhomMatHangId;
    private String donViTinhId;
    private String tenNhom;
    private String tenDonViTinh;

    public MatHang() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    public String getNhomMatHangId() { return nhomMatHangId; }
    public void setNhomMatHangId(String nhomMatHangId) { this.nhomMatHangId = nhomMatHangId; }

    public String getDonViTinhId() { return donViTinhId; }
    public void setDonViTinhId(String donViTinhId) { this.donViTinhId = donViTinhId; }

    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
