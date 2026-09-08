package kazuko.ex.report.model;

public class TongHopMatHangBanTheoThuNganReportRow {
    private int stt;
    private String groupThuNgan;
    private String matHang;
    private String donViTinh;
    private double soLuongBan;
    private double soLuongTra;
    private double donGia;
    private double thanhTien;

    public TongHopMatHangBanTheoThuNganReportRow() {}

    public TongHopMatHangBanTheoThuNganReportRow(int stt, String groupThuNgan, String matHang, String donViTinh, double soLuongBan, double soLuongTra, double donGia, double thanhTien) {
        this.stt = stt;
        this.groupThuNgan = groupThuNgan;
        this.matHang = matHang;
        this.donViTinh = donViTinh;
        this.soLuongBan = soLuongBan;
        this.soLuongTra = soLuongTra;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupThuNgan() { return groupThuNgan; }
    public void setGroupThuNgan(String groupThuNgan) { this.groupThuNgan = groupThuNgan; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getSoLuongBan() { return soLuongBan; }
    public void setSoLuongBan(double soLuongBan) { this.soLuongBan = soLuongBan; }

    public double getSoLuongTra() { return soLuongTra; }
    public void setSoLuongTra(double soLuongTra) { this.soLuongTra = soLuongTra; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
