package kazuko.ex.report.model;

public class BaoCaoCongNoNhaCungCapReportRow {
    private Integer stt;
    private String nhomNhaCungCap;
    private String maNhaCungCap;
    private String tenNhaCungCap;
    private String dienThoai;
    private Double noDau;
    private Double mua;
    private Double thanhToan;
    private Double noCuoi;

    public BaoCaoCongNoNhaCungCapReportRow() {}

    public BaoCaoCongNoNhaCungCapReportRow(Integer stt, String nhomNhaCungCap, String maNhaCungCap, String tenNhaCungCap,
                                          String dienThoai, Double noDau, Double mua, Double thanhToan, Double noCuoi) {
        this.stt = stt;
        this.nhomNhaCungCap = nhomNhaCungCap;
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.dienThoai = dienThoai;
        this.noDau = noDau != null ? noDau : 0.0;
        this.mua = mua != null ? mua : 0.0;
        this.thanhToan = thanhToan != null ? thanhToan : 0.0;
        this.noCuoi = noCuoi != null ? noCuoi : 0.0;
    }

    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }

    public String getNhomNhaCungCap() { return nhomNhaCungCap; }
    public void setNhomNhaCungCap(String nhomNhaCungCap) { this.nhomNhaCungCap = nhomNhaCungCap; }

    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

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
