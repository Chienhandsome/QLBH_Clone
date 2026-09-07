package kazuko.ex.report.model;

public class NhaCungCapReportRow {
    private int stt;
    private String groupHeader;
    private String maNhaCungCap;
    private String tenNhaCungCap;
    private String diaChi;
    private String dienThoai;
    private String email;

    public NhaCungCapReportRow() {}

    public NhaCungCapReportRow(int stt, String groupHeader, String maNhaCungCap, String tenNhaCungCap, String diaChi, String dienThoai, String email) {
        this.stt = stt;
        this.groupHeader = groupHeader;
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.email = email;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupHeader() { return groupHeader; }
    public void setGroupHeader(String groupHeader) { this.groupHeader = groupHeader; }

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
}