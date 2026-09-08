package kazuko.ex.report.model;

public class DanhSachPhieuKiemKeTheoNhanVienReportRow {
    private int stt;
    private String groupNhanVien;
    private String ngay;
    private String soPhieu;
    private String dienGiai;
    private String ghiChu;

    public DanhSachPhieuKiemKeTheoNhanVienReportRow() {}

    public DanhSachPhieuKiemKeTheoNhanVienReportRow(int stt, String groupNhanVien, String ngay, String soPhieu, String dienGiai, String ghiChu) {
        this.stt = stt;
        this.groupNhanVien = groupNhanVien;
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.dienGiai = dienGiai;
        this.ghiChu = ghiChu;
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

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
