package kazuko.ex.model.banhang;

public class ThongKeMatHangBanRow {
    private int stt;
    private String maHang;
    private String tenMatHang;
    private double soLuong;
    private String donViTinh;
    private double donGia;
    private double thanhTienBan;
    private double giaVon;
    private double giamGia;
    private double thanhTienNhap;
    private double tiLeLai;
    private double lai;
    private String nhomHangId;
    private String nhomHangName;

    public ThongKeMatHangBanRow() {}

    public ThongKeMatHangBanRow(int stt, String maHang, String tenMatHang, double soLuong, String donViTinh,
                               double donGia, double thanhTienBan, double giaVon, double giamGia,
                               double thanhTienNhap, double tiLeLai, double lai,
                               String nhomHangId, String nhomHangName) {
        this.stt = stt;
        this.maHang = maHang;
        this.tenMatHang = tenMatHang;
        this.soLuong = soLuong;
        this.donViTinh = donViTinh;
        this.donGia = donGia;
        this.thanhTienBan = thanhTienBan;
        this.giaVon = giaVon;
        this.giamGia = giamGia;
        this.thanhTienNhap = thanhTienNhap;
        this.tiLeLai = tiLeLai;
        this.lai = lai;
        this.nhomHangId = nhomHangId;
        this.nhomHangName = nhomHangName;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenMatHang() { return tenMatHang; }
    public void setTenMatHang(String tenMatHang) { this.tenMatHang = tenMatHang; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTienBan() { return thanhTienBan; }
    public void setThanhTienBan(double thanhTienBan) { this.thanhTienBan = thanhTienBan; }

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public double getThanhTienNhap() { return thanhTienNhap; }
    public void setThanhTienNhap(double thanhTienNhap) { this.thanhTienNhap = thanhTienNhap; }

    public double getTiLeLai() { return tiLeLai; }
    public void setTiLeLai(double tiLeLai) { this.tiLeLai = tiLeLai; }

    public double getLai() { return lai; }
    public void setLai(double lai) { this.lai = lai; }

    public String getNhomHangId() { return nhomHangId; }
    public void setNhomHangId(String nhomHangId) { this.nhomHangId = nhomHangId; }

    public String getNhomHangName() { return nhomHangName; }
    public void setNhomHangName(String nhomHangName) { this.nhomHangName = nhomHangName; }
}
