package kazuko.ex.model.danhmuc;

public class NhomKhachHangGiamGia {
    private String nhomMatHangId;
    private String tenNhomMatHang;
    private double tiLeGiam;

    public NhomKhachHangGiamGia() {}

    public NhomKhachHangGiamGia(String nhomMatHangId, String tenNhomMatHang, double tiLeGiam) {
        this.nhomMatHangId = nhomMatHangId;
        this.tenNhomMatHang = tenNhomMatHang;
        this.tiLeGiam = tiLeGiam;
    }

    public String getNhomMatHangId() {
        return nhomMatHangId;
    }

    public void setNhomMatHangId(String nhomMatHangId) {
        this.nhomMatHangId = nhomMatHangId;
    }

    public String getTenNhomMatHang() {
        return tenNhomMatHang;
    }

    public void setTenNhomMatHang(String tenNhomMatHang) {
        this.tenNhomMatHang = tenNhomMatHang;
    }

    public double getTiLeGiam() {
        return tiLeGiam;
    }

    public void setTiLeGiam(double tiLeGiam) {
        this.tiLeGiam = tiLeGiam;
    }
}
