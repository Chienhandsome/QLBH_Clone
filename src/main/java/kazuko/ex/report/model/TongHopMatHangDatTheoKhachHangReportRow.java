package kazuko.ex.report.model;

public class TongHopMatHangDatTheoKhachHangReportRow {
    private int stt;
    private String groupKhachHang;
    private String tenMatHang;
    private double soLuong;
    private double donGia;
    private double thanhTien;

    public TongHopMatHangDatTheoKhachHangReportRow() {}

    public TongHopMatHangDatTheoKhachHangReportRow(int stt, String groupKhachHang, String tenMatHang, double soLuong, double donGia, double thanhTien) {
        this.stt = stt;
        this.groupKhachHang = groupKhachHang;
        this.tenMatHang = tenMatHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupKhachHang() { return groupKhachHang; }
    public void setGroupKhachHang(String groupKhachHang) { this.groupKhachHang = groupKhachHang; }

    public String getTenMatHang() { return tenMatHang; }
    public void setTenMatHang(String tenMatHang) { this.tenMatHang = tenMatHang; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
