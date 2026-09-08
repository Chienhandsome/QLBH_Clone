package kazuko.ex.report.model;

public class TongHopMatHangXuatTheoNhanVienReportRow {
    private int stt;
    private String groupNhanVien;
    private String matHang;
    private String dvt;
    private double soLuongXuat;
    private double donGia;
    private double thanhTien;

    public TongHopMatHangXuatTheoNhanVienReportRow() {}

    public TongHopMatHangXuatTheoNhanVienReportRow(int stt, String groupNhanVien, String matHang, String dvt, double soLuongXuat, double donGia, double thanhTien) {
        this.stt = stt;
        this.groupNhanVien = groupNhanVien;
        this.matHang = matHang;
        this.dvt = dvt;
        this.soLuongXuat = soLuongXuat;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNhanVien() { return groupNhanVien; }
    public void setGroupNhanVien(String groupNhanVien) { this.groupNhanVien = groupNhanVien; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getSoLuongXuat() { return soLuongXuat; }
    public void setSoLuongXuat(double soLuongXuat) { this.soLuongXuat = soLuongXuat; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
