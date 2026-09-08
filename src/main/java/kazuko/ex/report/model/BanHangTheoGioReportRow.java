package kazuko.ex.report.model;

public class BanHangTheoGioReportRow {
    private int stt;
    private String groupHeader;
    private String maHang;
    private String tenHang;
    private double soLuong;
    private double donGia;

    public BanHangTheoGioReportRow() {}

    public BanHangTheoGioReportRow(int stt, String groupHeader, String maHang, String tenHang, double soLuong, double donGia) {
        this.stt = stt;
        this.groupHeader = groupHeader;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupHeader() { return groupHeader; }
    public void setGroupHeader(String groupHeader) { this.groupHeader = groupHeader; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}