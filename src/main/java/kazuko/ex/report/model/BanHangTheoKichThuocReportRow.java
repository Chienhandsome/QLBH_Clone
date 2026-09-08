package kazuko.ex.report.model;

public class BanHangTheoKichThuocReportRow {
    private int stt;
    private String groupHeader;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private double soLuong;
    private String chiTietSizeMau;
    private double donGia;
    private double thanhTien;

    public BanHangTheoKichThuocReportRow() {}

    public BanHangTheoKichThuocReportRow(int stt, String groupHeader, String maHang, String tenHang, String donViTinh, double soLuong, String chiTietSizeMau, double donGia, double thanhTien) {
        this.stt = stt;
        this.groupHeader = groupHeader;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.chiTietSizeMau = chiTietSizeMau;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupHeader() { return groupHeader; }
    public void setGroupHeader(String groupHeader) { this.groupHeader = groupHeader; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public String getChiTietSizeMau() { return chiTietSizeMau; }
    public void setChiTietSizeMau(String chiTietSizeMau) { this.chiTietSizeMau = chiTietSizeMau; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}