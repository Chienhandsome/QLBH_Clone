package kazuko.ex.report.model;

public class ChiTietHangKhuyenMaiReportRow {
    private int stt;
    private String soPhieu;
    private String ngay;
    private String tenKhachHang;
    private String khuyenMai;
    private double soLuong;
    private String thanhToanBoi;

    public ChiTietHangKhuyenMaiReportRow() {}

    public ChiTietHangKhuyenMaiReportRow(int stt, String soPhieu, String ngay, String tenKhachHang,
                                         String khuyenMai, double soLuong, String thanhToanBoi) {
        this.stt = stt;
        this.soPhieu = soPhieu;
        this.ngay = ngay;
        this.tenKhachHang = tenKhachHang;
        this.khuyenMai = khuyenMai;
        this.soLuong = soLuong;
        this.thanhToanBoi = thanhToanBoi;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(String khuyenMai) { this.khuyenMai = khuyenMai; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public String getThanhToanBoi() { return thanhToanBoi; }
    public void setThanhToanBoi(String thanhToanBoi) { this.thanhToanBoi = thanhToanBoi; }
}
