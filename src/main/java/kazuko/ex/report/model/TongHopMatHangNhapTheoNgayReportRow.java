package kazuko.ex.report.model;

public class TongHopMatHangNhapTheoNgayReportRow {
    private int stt;
    private String groupNgay;
    private String maHang;
    private String matHang;
    private String donViTinh;
    private Double soLuong;
    private Double donGia;
    private Double thanhTien;

    public TongHopMatHangNhapTheoNgayReportRow() {}

    public TongHopMatHangNhapTheoNgayReportRow(int stt, String groupNgay, String maHang, String matHang,
                                             String donViTinh, Double soLuong, Double donGia, Double thanhTien) {
        this.stt = stt;
        this.groupNgay = groupNgay;
        this.maHang = maHang;
        this.matHang = matHang;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNgay() { return groupNgay; }
    public void setGroupNgay(String groupNgay) { this.groupNgay = groupNgay; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Double getSoLuong() { return soLuong; }
    public void setSoLuong(Double soLuong) { this.soLuong = soLuong; }

    public Double getDonGia() { return donGia; }
    public void setDonGia(Double donGia) { this.donGia = donGia; }

    public Double getThanhTien() { return thanhTien; }
    public void setThanhTien(Double thanhTien) { this.thanhTien = thanhTien; }
}
