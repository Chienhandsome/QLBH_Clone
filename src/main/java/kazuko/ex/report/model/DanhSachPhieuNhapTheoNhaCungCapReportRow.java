package kazuko.ex.report.model;

public class DanhSachPhieuNhapTheoNhaCungCapReportRow {
    private int stt;
    private String groupNhaCungCap;
    private String ngay;
    private String soPhieu;
    private String khoNhap;
    private String nhanVienNhap;
    private String dienGiai;
    private Double tienHang;
    private Double tienGiamGia;
    private Double phiVanChuyen;
    private Double tongCong;

    public DanhSachPhieuNhapTheoNhaCungCapReportRow() {}

    public DanhSachPhieuNhapTheoNhaCungCapReportRow(int stt, String groupNhaCungCap, String ngay, String soPhieu,
                                                   String khoNhap, String nhanVienNhap, String dienGiai,
                                                   Double tienHang, Double tienGiamGia, Double phiVanChuyen,
                                                   Double tongCong) {
        this.stt = stt;
        this.groupNhaCungCap = groupNhaCungCap;
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.khoNhap = khoNhap;
        this.nhanVienNhap = nhanVienNhap;
        this.dienGiai = dienGiai;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNhaCungCap() { return groupNhaCungCap; }
    public void setGroupNhaCungCap(String groupNhaCungCap) { this.groupNhaCungCap = groupNhaCungCap; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getKhoNhap() { return khoNhap; }
    public void setKhoNhap(String khoNhap) { this.khoNhap = khoNhap; }

    public String getNhanVienNhap() { return nhanVienNhap; }
    public void setNhanVienNhap(String nhanVienNhap) { this.nhanVienNhap = nhanVienNhap; }

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
