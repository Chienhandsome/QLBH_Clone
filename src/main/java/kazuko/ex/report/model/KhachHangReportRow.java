package kazuko.ex.report.model;

public class KhachHangReportRow {
    private int stt;
    private String groupHeader; // vd: "Nhóm khách hàng: Khách lẻ" hoặc "Nhân viên: Nguyễn Văn A"
    private String maKhach;
    private String tenKhachHang;
    private String diaChi;
    private String dienThoai;
    private String email;

    public KhachHangReportRow() {}

    public KhachHangReportRow(int stt, String groupHeader, String maKhach, String tenKhachHang, String diaChi, String dienThoai, String email) {
        this.stt = stt;
        this.groupHeader = groupHeader;
        this.maKhach = maKhach;
        this.tenKhachHang = tenKhachHang;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.email = email;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupHeader() { return groupHeader; }
    public void setGroupHeader(String groupHeader) { this.groupHeader = groupHeader; }

    public String getMaKhach() { return maKhach; }
    public void setMaKhach(String maKhach) { this.maKhach = maKhach; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}