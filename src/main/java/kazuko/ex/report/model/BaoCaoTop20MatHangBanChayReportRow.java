package kazuko.ex.report.model;

public class BaoCaoTop20MatHangBanChayReportRow {
    private Integer stt;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private Double soLuong;
    private Double giaBan;

    public BaoCaoTop20MatHangBanChayReportRow() {}

    public BaoCaoTop20MatHangBanChayReportRow(Integer stt, String maHang, String tenHang, String donViTinh, Double soLuong, Double giaBan) {
        this.stt = stt;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.giaBan = giaBan;
    }

    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Double getSoLuong() { return soLuong; }
    public void setSoLuong(Double soLuong) { this.soLuong = soLuong; }

    public Double getGiaBan() { return giaBan; }
    public void setGiaBan(Double giaBan) { this.giaBan = giaBan; }
}
