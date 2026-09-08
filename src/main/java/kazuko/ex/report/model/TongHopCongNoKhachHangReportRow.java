package kazuko.ex.report.model;

public class TongHopCongNoKhachHangReportRow {
    private Integer stt;
    private String nhomKhachHang;
    private String maKhachHang;
    private String tenKhachHang;
    private String diaChi;
    private String dienThoai;
    private String email;
    private Double tongNo;

    public TongHopCongNoKhachHangReportRow() {}

    public TongHopCongNoKhachHangReportRow(Integer stt, String nhomKhachHang, String maKhachHang, String tenKhachHang,
                                         String diaChi, String dienThoai, String email, Double tongNo) {
        this.stt = stt;
        this.nhomKhachHang = nhomKhachHang;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.email = email;
        this.tongNo = tongNo != null ? tongNo : 0.0;
    }

    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }

    public String getNhomKhachHang() { return nhomKhachHang; }
    public void setNhomKhachHang(String nhomKhachHang) { this.nhomKhachHang = nhomKhachHang; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Double getTongNo() { return tongNo; }
    public void setTongNo(Double tongNo) { this.tongNo = tongNo; }
}
