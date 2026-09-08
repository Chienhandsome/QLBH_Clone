package kazuko.ex.report.model;

public class TongHopMatHangDatTheoNgayReportRow {
    private int stt;
    private String groupNgay;
    private String matHang;
    private String dvt;
    private double soLuong;
    private double donGia;
    private double thanhTien;

    public TongHopMatHangDatTheoNgayReportRow() {}

    public TongHopMatHangDatTheoNgayReportRow(int stt, String groupNgay, String matHang, String dvt, double soLuong, double donGia, double thanhTien) {
        this.stt = stt;
        this.groupNgay = groupNgay;
        this.matHang = matHang;
        this.dvt = dvt;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNgay() { return groupNgay; }
    public void setGroupNgay(String groupNgay) { this.groupNgay = groupNgay; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
