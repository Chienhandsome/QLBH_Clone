package kazuko.ex.report.model;

public class KhachHangSinhNhatReportRow {
    private int stt;
    private String tenKhachHang;
    private String maKhach;
    private String diaChi;
    private String dienThoai;
    private String email;

    public KhachHangSinhNhatReportRow() {}

    public KhachHangSinhNhatReportRow(int stt, String tenKhachHang, String maKhach, String diaChi, String dienThoai, String email) {
        this.stt = stt;
        this.tenKhachHang = tenKhachHang;
        this.maKhach = maKhach;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.email = email;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getMaKhach() { return maKhach; }
    public void setMaKhach(String maKhach) { this.maKhach = maKhach; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}