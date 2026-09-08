package kazuko.ex.report.model;

public class TongHopDatHangTheoNgayReportRow {
    private int stt;
    private String ngay;
    private double tienHang;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double tongCong;

    public TongHopDatHangTheoNgayReportRow() {}

    public TongHopDatHangTheoNgayReportRow(int stt, String ngay, double tienHang, double tienGiamGia, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.ngay = ngay;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }
}
