package kazuko.ex.report.model;

public class DanhSachPhieuChuyenKhoReportRow {
    private int stt;
    private String groupKey;
    private String soPhieu;
    private String ngay;
    private String khoXuat;
    private String khoNhap;
    private String nhanVienXuat;
    private String nhanVienNhap;
    private String dienGiai;
    private double tongCong;
    private String ghiChu;

    public DanhSachPhieuChuyenKhoReportRow() {}

    public DanhSachPhieuChuyenKhoReportRow(int stt, String groupKey, String soPhieu, String ngay, String khoXuat, String khoNhap, String nhanVienXuat, String nhanVienNhap, String dienGiai, double tongCong, String ghiChu) {
        this.stt = stt;
        this.groupKey = groupKey;
        this.soPhieu = soPhieu;
        this.ngay = ngay;
        this.khoXuat = khoXuat;
        this.khoNhap = khoNhap;
        this.nhanVienXuat = nhanVienXuat;
        this.nhanVienNhap = nhanVienNhap;
        this.dienGiai = dienGiai;
        this.tongCong = tongCong;
        this.ghiChu = ghiChu;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getKhoXuat() { return khoXuat; }
    public void setKhoXuat(String khoXuat) { this.khoXuat = khoXuat; }

    public String getKhoNhap() { return khoNhap; }
    public void setKhoNhap(String khoNhap) { this.khoNhap = khoNhap; }

    public String getNhanVienXuat() { return nhanVienXuat; }
    public void setNhanVienXuat(String nhanVienXuat) { this.nhanVienXuat = nhanVienXuat; }

    public String getNhanVienNhap() { return nhanVienNhap; }
    public void setNhanVienNhap(String nhanVienNhap) { this.nhanVienNhap = nhanVienNhap; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
