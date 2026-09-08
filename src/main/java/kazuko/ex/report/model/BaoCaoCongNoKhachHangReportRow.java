package kazuko.ex.report.model;

public class BaoCaoCongNoKhachHangReportRow {
    private Integer stt;
    private String nhomKhachHang;
    private String maKhachHang;
    private String tenKhachHang;
    private String dienThoai;
    private Double noDau;
    private Double mua;
    private Double thanhToan;
    private Double noCuoi;

    public BaoCaoCongNoKhachHangReportRow() {}

    public BaoCaoCongNoKhachHangReportRow(Integer stt, String nhomKhachHang, String maKhachHang, String tenKhachHang,
                                         String dienThoai, Double noDau, Double mua, Double thanhToan, Double noCuoi) {
        this.stt = stt;
        this.nhomKhachHang = nhomKhachHang;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.dienThoai = dienThoai;
        this.noDau = noDau != null ? noDau : 0.0;
        this.mua = mua != null ? mua : 0.0;
        this.thanhToan = thanhToan != null ? thanhToan : 0.0;
        this.noCuoi = noCuoi != null ? noCuoi : 0.0;
    }

    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }

    public String getNhomKhachHang() { return nhomKhachHang; }
    public void setNhomKhachHang(String nhomKhachHang) { this.nhomKhachHang = nhomKhachHang; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public Double getNoDau() { return noDau; }
    public void setNoDau(Double noDau) { this.noDau = noDau; }

    public Double getMua() { return mua; }
    public void setMua(Double mua) { this.mua = mua; }

    public Double getThanhToan() { return thanhToan; }
    public void setThanhToan(Double thanhToan) { this.thanhToan = thanhToan; }

    public Double getNoCuoi() { return noCuoi; }
    public void setNoCuoi(Double noCuoi) { this.noCuoi = noCuoi; }
}
