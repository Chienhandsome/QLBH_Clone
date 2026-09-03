package kazuko.ex.model.khohang;

import java.util.Date;

public class TheKhoEntry {
    private String soPhieu;
    private Date ngay;
    private String dienGiai;
    private double donGia;
    private double thanhTien;
    private double tiLeGiamGia;
    private String tenDonViTinh;
    private double slNhap;
    private double slXuat;
    private double tonLuyKe;
    private String doiTuong;

    public TheKhoEntry() {}

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(double tiLeGiamGia) { this.tiLeGiamGia = tiLeGiamGia; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    public double getSlNhap() { return slNhap; }
    public void setSlNhap(double slNhap) { this.slNhap = slNhap; }

    public double getSlXuat() { return slXuat; }
    public void setSlXuat(double slXuat) { this.slXuat = slXuat; }

    public double getTonLuyKe() { return tonLuyKe; }
    public void setTonLuyKe(double tonLuyKe) { this.tonLuyKe = tonLuyKe; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }
}
