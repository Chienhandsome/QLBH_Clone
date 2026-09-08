package kazuko.ex.report.model;

public class ChiTietBanHangTheoThuNganReportRow {
    private int stt;
    private String maHang;
    private String tenHang;
    private String ngayGioBan;
    private double donGia;
    private double soLuong;
    private double phanTramCK;
    private double thanhTien;

    public ChiTietBanHangTheoThuNganReportRow() {}

    public ChiTietBanHangTheoThuNganReportRow(int stt, String maHang, String tenHang, String ngayGioBan, double donGia, double soLuong, double phanTramCK, double thanhTien) {
        this.stt = stt;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.ngayGioBan = ngayGioBan;
        this.donGia = donGia;
        this.soLuong = soLuong;
        this.phanTramCK = phanTramCK;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getNgayGioBan() { return ngayGioBan; }
    public void setNgayGioBan(String ngayGioBan) { this.ngayGioBan = ngayGioBan; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getPhanTramCK() { return phanTramCK; }
    public void setPhanTramCK(double phanTramCK) { this.phanTramCK = phanTramCK; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
