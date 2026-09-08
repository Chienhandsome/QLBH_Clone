package kazuko.ex.report.model;

public class TongHopNhapTheoNhanVienReportRow {
    private int stt;
    private String tenNhanVien;
    private Double tienHang;
    private Double tienGiamGia;
    private Double phiVanChuyen;
    private Double tongCong;

    public TongHopNhapTheoNhanVienReportRow() {}

    public TongHopNhapTheoNhanVienReportRow(int stt, String tenNhanVien, Double tienHang, Double tienGiamGia,
                                          Double phiVanChuyen, Double tongCong) {
        this.stt = stt;
        this.tenNhanVien = tenNhanVien;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public Double getTienHang() { return tienHang; }
    public void setTienHang(Double tienHang) { this.tienHang = tienHang; }

    public Double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(Double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public Double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(Double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public Double getTongCong() { return tongCong; }
    public void setTongCong(Double tongCong) { this.tongCong = tongCong; }
}
