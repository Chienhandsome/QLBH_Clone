package kazuko.ex.model.danhmuc;

public class KhachHang {
    private String id;
    private String maKhach;
    private String name;
    private String dienThoai;
    private String diaChi;
    private String email;
    private String nhomKhachHangId;
    private double diemTichLuy;

    public KhachHang() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaKhach() { return maKhach; }
    public void setMaKhach(String maKhach) { this.maKhach = maKhach; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNhomKhachHangId() { return nhomKhachHangId; }
    public void setNhomKhachHangId(String nhomKhachHangId) { this.nhomKhachHangId = nhomKhachHangId; }

    public double getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(double diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    @Override
    public String toString() {
        return name + (dienThoai != null && !dienThoai.isEmpty() ? " (" + dienThoai + ")" : "");
    }
}
