package kazuko.ex.report.model;

public class DanhSachPhieuNhapTheoNgayReportRow {
    private int stt;
    private String groupNgay;
    private String soPhieu;
    private String nhaCungCap;
    private String khoNhap;
    private String dienGiai;
    private Double tienHang;
    private Double tienGiamGia;
    private Double phiVanChuyen;
    private Double tongCong;

    public DanhSachPhieuNhapTheoNgayReportRow() {}

    public DanhSachPhieuNhapTheoNgayReportRow(int stt, String groupNgay, String soPhieu, String nhaCungCap,
                                             String khoNhap, String dienGiai, Double tienHang,
                                             Double tienGiamGia, Double phiVanChuyen, Double tongCong) {
        this.stt = stt;
        this.groupNgay = groupNgay;
        this.soPhieu = soPhieu;
        this.nhaCungCap = nhaCungCap;
        this.khoNhap = khoNhap;
        this.dienGiai = dienGiai;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNgay() { return groupNgay; }
    public void setGroupNgay(String groupNgay) { this.groupNgay = groupNgay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    public String getKhoNhap() { return khoNhap; }
    public void setKhoNhap(String khoNhap) { this.khoNhap = khoNhap; }

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
