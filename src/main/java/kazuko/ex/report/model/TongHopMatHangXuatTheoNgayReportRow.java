package kazuko.ex.report.model;

public class TongHopMatHangXuatTheoNgayReportRow {
    private int stt;
    private String groupSoLuong;
    private String matHang;
    private String dvt;
    private double donGia;
    private double thanhTien;

    public TongHopMatHangXuatTheoNgayReportRow() {}

    public TongHopMatHangXuatTheoNgayReportRow(int stt, String groupSoLuong, String matHang, String dvt, double donGia, double thanhTien) {
        this.stt = stt;
        this.groupSoLuong = groupSoLuong;
        this.matHang = matHang;
        this.dvt = dvt;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupSoLuong() { return groupSoLuong; }
    public void setGroupSoLuong(String groupSoLuong) { this.groupSoLuong = groupSoLuong; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
