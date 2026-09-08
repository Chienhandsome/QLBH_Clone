package kazuko.ex.report.model;

public class DanhSachPhieuKiemKeTheoNgayReportRow {
    private int stt;
    private String groupNgay;
    private String soPhieu;
    private String dienGiai;
    private String ghiChu;

    public DanhSachPhieuKiemKeTheoNgayReportRow() {}

    public DanhSachPhieuKiemKeTheoNgayReportRow(int stt, String groupNgay, String soPhieu, String dienGiai, String ghiChu) {
        this.stt = stt;
        this.groupNgay = groupNgay;
        this.soPhieu = soPhieu;
        this.dienGiai = dienGiai;
        this.ghiChu = ghiChu;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNgay() { return groupNgay; }
    public void setGroupNgay(String groupNgay) { this.groupNgay = groupNgay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
