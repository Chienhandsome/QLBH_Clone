package kazuko.ex.report.model;

public class BanHangTheoNhanVienReportRow {
    private int stt;
    private String groupNhanVien;
    private String ngay;
    private String soPhieu;
    private double tienHang;
    private double tienGiamGia;
    private double doiTra;
    private double phiVanChuyen;
    private double tongCong;

    public BanHangTheoNhanVienReportRow() {}

    public BanHangTheoNhanVienReportRow(int stt, String groupNhanVien, String ngay, String soPhieu, double tienHang, double tienGiamGia, double doiTra, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.groupNhanVien = groupNhanVien;
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.doiTra = doiTra;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNhanVien() { return groupNhanVien; }
    public void setGroupNhanVien(String groupNhanVien) { this.groupNhanVien = groupNhanVien; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getDoiTra() { return doiTra; }
    public void setDoiTra(double doiTra) { this.doiTra = doiTra; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }
}