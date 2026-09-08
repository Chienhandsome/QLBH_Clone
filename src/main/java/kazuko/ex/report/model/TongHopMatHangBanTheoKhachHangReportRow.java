package kazuko.ex.report.model;

public class TongHopMatHangBanTheoKhachHangReportRow {
    private int stt;
    private String groupKhachHang;
    private String maHang;
    private String matHang;
    private String donViTinh;
    private double soLuongTra;
    private double soLuongBan;
    private double donGia;
    private double thanhTien;

    public TongHopMatHangBanTheoKhachHangReportRow() {}

    public TongHopMatHangBanTheoKhachHangReportRow(int stt, String groupKhachHang, String maHang, String matHang, String donViTinh, double soLuongTra, double soLuongBan, double donGia, double thanhTien) {
        this.stt = stt;
        this.groupKhachHang = groupKhachHang;
        this.maHang = maHang;
        this.matHang = matHang;
        this.donViTinh = donViTinh;
        this.soLuongTra = soLuongTra;
        this.soLuongBan = soLuongBan;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupKhachHang() { return groupKhachHang; }
    public void setGroupKhachHang(String groupKhachHang) { this.groupKhachHang = groupKhachHang; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getSoLuongTra() { return soLuongTra; }
    public void setSoLuongTra(double soLuongTra) { this.soLuongTra = soLuongTra; }

    public double getSoLuongBan() { return soLuongBan; }
    public void setSoLuongBan(double soLuongBan) { this.soLuongBan = soLuongBan; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
