package kazuko.ex.report.model;

public class DanhSachPhieuNhapTheoNhanVienReportRow {
    private int stt;
    private String groupNhanVien;
    private String ngay;
    private String soPhieu;
    private String dienGiai;
    private Double tienHang;
    private Double tienGiamGia;
    private Double phiVanChuyen;
    private Double tongCong;

    public DanhSachPhieuNhapTheoNhanVienReportRow() {}

    public DanhSachPhieuNhapTheoNhanVienReportRow(int stt, String groupNhanVien, String ngay, String soPhieu,
                                                 String dienGiai, Double tienHang, Double tienGiamGia,
                                                 Double phiVanChuyen, Double tongCong) {
        this.stt = stt;
        this.groupNhanVien = groupNhanVien;
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.dienGiai = dienGiai;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNhanVien() { return groupNhanVien; }
    public void setGroupNhanVien(String groupNhanVien) { this.groupNhanVien = groupNhanVien; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public Double getTienHang() { return tienHang; }
    public void setTienHang(Double tienHang) { this.tienHang = tienHang; }

    public Double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(Double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public Double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(Double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public Double getTongCong() { return tongCong; }
    public void setTongCong(Double tongCong) { this.tongCong = tongCong; }
}
