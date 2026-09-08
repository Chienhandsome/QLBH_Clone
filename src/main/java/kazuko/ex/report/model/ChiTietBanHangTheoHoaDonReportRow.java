package kazuko.ex.report.model;

public class ChiTietBanHangTheoHoaDonReportRow {
    private int stt;
    private String soPhieu;
    private double giamGiaPhieu;
    private double tongCongPhieu;
    private String gio;
    private String thuNgan;
    private String nvBan;
    private String tenHang;
    private String dvt;
    private double soLuong;
    private double donGia;
    private double ckPt;
    private double thanhTien;

    public ChiTietBanHangTheoHoaDonReportRow() {}

    public ChiTietBanHangTheoHoaDonReportRow(int stt, String soPhieu, double giamGiaPhieu, double tongCongPhieu,
                                             String gio, String thuNgan, String nvBan, String tenHang,
                                             String dvt, double soLuong, double donGia, double ckPt, double thanhTien) {
        this.stt = stt;
        this.soPhieu = soPhieu;
        this.giamGiaPhieu = giamGiaPhieu;
        this.tongCongPhieu = tongCongPhieu;
        this.gio = gio;
        this.thuNgan = thuNgan;
        this.nvBan = nvBan;
        this.tenHang = tenHang;
        this.dvt = dvt;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.ckPt = ckPt;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public double getGiamGiaPhieu() { return giamGiaPhieu; }
    public void setGiamGiaPhieu(double giamGiaPhieu) { this.giamGiaPhieu = giamGiaPhieu; }

    public double getTongCongPhieu() { return tongCongPhieu; }
    public void setTongCongPhieu(double tongCongPhieu) { this.tongCongPhieu = tongCongPhieu; }

    public String getGio() { return gio; }
    public void setGio(String gio) { this.gio = gio; }

    public String getThuNgan() { return thuNgan; }
    public void setThuNgan(String thuNgan) { this.thuNgan = thuNgan; }

    public String getNvBan() { return nvBan; }
    public void setNvBan(String nvBan) { this.nvBan = nvBan; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getCkPt() { return ckPt; }
    public void setCkPt(double ckPt) { this.ckPt = ckPt; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
