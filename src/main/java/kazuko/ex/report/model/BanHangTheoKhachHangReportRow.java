package kazuko.ex.report.model;

public class BanHangTheoKhachHangReportRow {
    private int stt;
    private String groupKhachHang;
    private String ngay;
    private String soPhieu;
    private double tienHang;
    private double doiTra;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double tongCong;

    public BanHangTheoKhachHangReportRow() {}

    public BanHangTheoKhachHangReportRow(int stt, String groupKhachHang, String ngay, String soPhieu, double tienHang, double doiTra, double tienGiamGia, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.groupKhachHang = groupKhachHang;
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.tienHang = tienHang;
        this.doiTra = doiTra;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupKhachHang() { return groupKhachHang; }
    public void setGroupKhachHang(String groupKhachHang) { this.groupKhachHang = groupKhachHang; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getDoiTra() { return doiTra; }
    public void setDoiTra(double doiTra) { this.doiTra = doiTra; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }
}