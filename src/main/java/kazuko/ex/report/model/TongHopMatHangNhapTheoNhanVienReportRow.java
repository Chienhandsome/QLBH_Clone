package kazuko.ex.report.model;

public class TongHopMatHangNhapTheoNhanVienReportRow {
    private int stt;
    private String groupNhanVien;
    private String maHang;
    private String matHang;
    private String donViTinh;
    private Double soLuong;
    private Double donGia;
    private Double thanhTien;

    public TongHopMatHangNhapTheoNhanVienReportRow() {}

    public TongHopMatHangNhapTheoNhanVienReportRow(int stt, String groupNhanVien, String maHang, String matHang,
                                                 String donViTinh, Double soLuong, Double donGia, Double thanhTien) {
        this.stt = stt;
        this.groupNhanVien = groupNhanVien;
        this.maHang = maHang;
        this.matHang = matHang;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNhanVien() { return groupNhanVien; }
    public void setGroupNhanVien(String groupNhanVien) { this.groupNhanVien = groupNhanVien; }

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
