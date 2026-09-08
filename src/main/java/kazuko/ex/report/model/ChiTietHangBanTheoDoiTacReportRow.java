package kazuko.ex.report.model;

public class ChiTietHangBanTheoDoiTacReportRow {
    private int stt;
    private String doiTac;
    private String matHang;
    private String dvt;
    private double soLuong;
    private double donGia;
    private double giamGia;
    private double thanhTien;
    private double hoaHong;
    private double traDoiTac;

    public ChiTietHangBanTheoDoiTacReportRow() {}

    public ChiTietHangBanTheoDoiTacReportRow(int stt, String doiTac, String matHang, String dvt,
                                             double soLuong, double donGia, double giamGia,
                                             double thanhTien, double hoaHong, double traDoiTac) {
        this.stt = stt;
        this.doiTac = doiTac;
        this.matHang = matHang;
        this.dvt = dvt;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.giamGia = giamGia;
        this.thanhTien = thanhTien;
        this.hoaHong = hoaHong;
        this.traDoiTac = traDoiTac;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getDoiTac() { return doiTac; }
    public void setDoiTac(String doiTac) { this.doiTac = doiTac; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public double getHoaHong() { return hoaHong; }
    public void setHoaHong(double hoaHong) { this.hoaHong = hoaHong; }

    public double getTraDoiTac() { return traDoiTac; }
    public void setTraDoiTac(double traDoiTac) { this.traDoiTac = traDoiTac; }
}
