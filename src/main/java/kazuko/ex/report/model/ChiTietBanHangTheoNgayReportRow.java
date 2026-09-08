package kazuko.ex.report.model;

public class ChiTietBanHangTheoNgayReportRow {
    private String ngay;
    private String soPhieu;
    private String khachHang;
    private String matHangBan;
    private double soLuong;
    private double donGia;
    private double ptChietKhau;
    private double tienGiam;
    private double thanhTien;
    private double tienHang;
    private double giamGia;
    private double tongCong;
    private double thanhToan;

    public ChiTietBanHangTheoNgayReportRow() {}

    public ChiTietBanHangTheoNgayReportRow(String ngay, String soPhieu, String khachHang, String matHangBan, double soLuong, double donGia, double ptChietKhau, double tienGiam, double thanhTien, double tienHang, double giamGia, double tongCong, double thanhToan) {
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.khachHang = khachHang;
        this.matHangBan = matHangBan;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.ptChietKhau = ptChietKhau;
        this.tienGiam = tienGiam;
        this.thanhTien = thanhTien;
        this.tienHang = tienHang;
        this.giamGia = giamGia;
        this.tongCong = tongCong;
        this.thanhToan = thanhToan;
    }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }

    public String getMatHangBan() { return matHangBan; }
    public void setMatHangBan(String matHangBan) { this.matHangBan = matHangBan; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getPtChietKhau() { return ptChietKhau; }
    public void setPtChietKhau(double ptChietKhau) { this.ptChietKhau = ptChietKhau; }

    public double getTienGiam() { return tienGiam; }
    public void setTienGiam(double tienGiam) { this.tienGiam = tienGiam; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public double getThanhToan() { return thanhToan; }
    public void setThanhToan(double thanhToan) { this.thanhToan = thanhToan; }
}