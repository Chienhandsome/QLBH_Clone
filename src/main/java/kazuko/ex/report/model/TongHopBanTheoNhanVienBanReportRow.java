package kazuko.ex.report.model;

public class TongHopBanTheoNhanVienBanReportRow {
    private int stt;
    private String nhanVienBan;
    private double tienHang;
    private double tienGiamGia;
    private double doiTra;
    private double phiVanChuyen;
    private double tongCong;

    public TongHopBanTheoNhanVienBanReportRow() {}

    public TongHopBanTheoNhanVienBanReportRow(int stt, String nhanVienBan, double tienHang, double tienGiamGia, double doiTra, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.nhanVienBan = nhanVienBan;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.doiTra = doiTra;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getNhanVienBan() { return nhanVienBan; }
    public void setNhanVienBan(String nhanVienBan) { this.nhanVienBan = nhanVienBan; }

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
