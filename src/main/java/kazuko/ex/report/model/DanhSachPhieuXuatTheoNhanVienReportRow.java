package kazuko.ex.report.model;

public class DanhSachPhieuXuatTheoNhanVienReportRow {
    private int stt;
    private String ngay;
    private String soPhieu;
    private double tienHang;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double tongCong;
    private String ghiChu;

    public DanhSachPhieuXuatTheoNhanVienReportRow() {}

    public DanhSachPhieuXuatTheoNhanVienReportRow(int stt, String ngay, String soPhieu, double tienHang, double tienGiamGia, double phiVanChuyen, double tongCong, String ghiChu) {
        this.stt = stt;
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
        this.ghiChu = ghiChu;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
