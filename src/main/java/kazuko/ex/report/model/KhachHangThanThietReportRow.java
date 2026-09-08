package kazuko.ex.report.model;

public class KhachHangThanThietReportRow {
    private int stt;
    private String groupHeader;
    private String tenKhachHang;
    private String diaChi;
    private String dienThoai;
    private double doanhSo;
    private double diemTang;
    private double diemGiam;
    private double diemTichLuy;

    public KhachHangThanThietReportRow() {}

    public KhachHangThanThietReportRow(int stt, String groupHeader, String tenKhachHang, String diaChi, String dienThoai, double doanhSo, double diemTang, double diemGiam, double diemTichLuy) {
        this.stt = stt;
        this.groupHeader = groupHeader;
        this.tenKhachHang = tenKhachHang;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.doanhSo = doanhSo;
        this.diemTang = diemTang;
        this.diemGiam = diemGiam;
        this.diemTichLuy = diemTichLuy;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupHeader() { return groupHeader; }
    public void setGroupHeader(String groupHeader) { this.groupHeader = groupHeader; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public double getDoanhSo() { return doanhSo; }
    public void setDoanhSo(double doanhSo) { this.doanhSo = doanhSo; }

    public double getDiemTang() { return diemTang; }
    public void setDiemTang(double diemTang) { this.diemTang = diemTang; }

    public double getDiemGiam() { return diemGiam; }
    public void setDiemGiam(double diemGiam) { this.diemGiam = diemGiam; }

    public double getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(double diemTichLuy) { this.diemTichLuy = diemTichLuy; }
}