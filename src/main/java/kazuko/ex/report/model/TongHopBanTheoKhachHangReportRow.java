package kazuko.ex.report.model;

public class TongHopBanTheoKhachHangReportRow {
    private int stt;
    private String maKhach;
    private String tenKhachHang;
    private String diaChi;
    private double tienHang;
    private double tienGiamGia;
    private double doiTra;
    private double phiVanChuyen;
    private double tongCong;

    public TongHopBanTheoKhachHangReportRow() {}

    public TongHopBanTheoKhachHangReportRow(int stt, String maKhach, String tenKhachHang, String diaChi, double tienHang, double tienGiamGia, double doiTra, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.maKhach = maKhach;
        this.tenKhachHang = tenKhachHang;
        this.diaChi = diaChi;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.doiTra = doiTra;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getMaKhach() { return maKhach; }
    public void setMaKhach(String maKhach) { this.maKhach = maKhach; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

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
