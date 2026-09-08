package kazuko.ex.report.model;

public class BanHangTheoThuNganReportRow {
    private int stt;
    private String groupThuNgan;
    private String ngay;
    private String soPhieu;
    private String khachHang;
    private double tienHang;
    private double doiTra;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double tongCong;

    public BanHangTheoThuNganReportRow() {}

    public BanHangTheoThuNganReportRow(int stt, String groupThuNgan, String ngay, String soPhieu, String khachHang, double tienHang, double doiTra, double tienGiamGia, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.groupThuNgan = groupThuNgan;
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.khachHang = khachHang;
        this.tienHang = tienHang;
        this.doiTra = doiTra;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupThuNgan() { return groupThuNgan; }
    public void setGroupThuNgan(String groupThuNgan) { this.groupThuNgan = groupThuNgan; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }

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
