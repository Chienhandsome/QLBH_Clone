package kazuko.ex.report.model;

public class TongHopCongNoNhaCungCapReportRow {
    private Integer stt;
    private String nhomNhaCungCap;
    private String maNhaCungCap;
    private String tenNhaCungCap;
    private String diaChi;
    private String dienThoai;
    private String email;
    private Double tongNo;

    public TongHopCongNoNhaCungCapReportRow() {}

    public TongHopCongNoNhaCungCapReportRow(Integer stt, String nhomNhaCungCap, String maNhaCungCap, String tenNhaCungCap,
                                          String diaChi, String dienThoai, String email, Double tongNo) {
        this.stt = stt;
        this.nhomNhaCungCap = nhomNhaCungCap;
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.email = email;
        this.tongNo = tongNo != null ? tongNo : 0.0;
    }

    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }

    public String getNhomNhaCungCap() { return nhomNhaCungCap; }
    public void setNhomNhaCungCap(String nhomNhaCungCap) { this.nhomNhaCungCap = nhomNhaCungCap; }

    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Double getTongNo() { return tongNo; }
    public void setTongNo(Double tongNo) { this.tongNo = tongNo; }
}
