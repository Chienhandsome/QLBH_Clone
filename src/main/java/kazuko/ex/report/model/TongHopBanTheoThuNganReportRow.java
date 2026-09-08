package kazuko.ex.report.model;

public class TongHopBanTheoThuNganReportRow {
    private int stt;
    private String thuNgan;
    private double tienHang;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double doiTra;
    private double tongCong;

    public TongHopBanTheoThuNganReportRow() {}

    public TongHopBanTheoThuNganReportRow(int stt, String thuNgan, double tienHang, double tienGiamGia, double phiVanChuyen, double doiTra, double tongCong) {
        this.stt = stt;
        this.thuNgan = thuNgan;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.doiTra = doiTra;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getThuNgan() { return thuNgan; }
    public void setThuNgan(String thuNgan) { this.thuNgan = thuNgan; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getDoiTra() { return doiTra; }
    public void setDoiTra(double doiTra) { this.doiTra = doiTra; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }
}
