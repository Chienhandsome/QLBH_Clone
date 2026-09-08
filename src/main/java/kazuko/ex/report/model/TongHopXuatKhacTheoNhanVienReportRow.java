package kazuko.ex.report.model;

public class TongHopXuatKhacTheoNhanVienReportRow {
    private int stt;
    private String nhanVienXuat;
    private double tienHang;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double tongCong;

    public TongHopXuatKhacTheoNhanVienReportRow() {}

    public TongHopXuatKhacTheoNhanVienReportRow(int stt, String nhanVienXuat, double tienHang, double tienGiamGia, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.nhanVienXuat = nhanVienXuat;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getNhanVienXuat() { return nhanVienXuat; }
    public void setNhanVienXuat(String nhanVienXuat) { this.nhanVienXuat = nhanVienXuat; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }
}
