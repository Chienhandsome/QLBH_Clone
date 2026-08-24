package kazuko.ex.model.banhang;

public class DonHangChiTiet {
    private String id;
    private String donHangId;
    private String matHangId;
    private String maHang;
    private String tenHang;
    private String donViTinhId;
    private String tenDonViTinh;
    private double donGia;
    private double slXuat;
    private double slNhap;
    private double tiLeGiamGia;
    private double thanhTien;
    private String khoHangId;

    public DonHangChiTiet() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonHangId() { return donHangId; }
    public void setDonHangId(String donHangId) { this.donHangId = donHangId; }

    public String getMatHangId() { return matHangId; }
    public void setMatHangId(String matHangId) { this.matHangId = matHangId; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinhId() { return donViTinhId; }
    public void setDonViTinhId(String donViTinhId) { this.donViTinhId = donViTinhId; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getSlXuat() { return slXuat; }
    public void setSlXuat(double slXuat) { this.slXuat = slXuat; }

    public double getSlNhap() { return slNhap; }
    public void setSlNhap(double slNhap) { this.slNhap = slNhap; }

    public double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(double tiLeGiamGia) { this.tiLeGiamGia = tiLeGiamGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public String getKhoHangId() { return khoHangId; }
    public void setKhoHangId(String khoHangId) { this.khoHangId = khoHangId; }
}
