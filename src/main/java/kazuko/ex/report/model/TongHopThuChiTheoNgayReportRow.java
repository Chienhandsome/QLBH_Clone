package kazuko.ex.report.model;

public class TongHopThuChiTheoNgayReportRow {
    private int stt;
    private String ngay;
    private Double soTienChi;
    private Double soTienThu;

    public TongHopThuChiTheoNgayReportRow() {}

    public TongHopThuChiTheoNgayReportRow(int stt, String ngay, Double soTienChi, Double soTienThu) {
        this.stt = stt;
        this.ngay = ngay;
        this.soTienChi = soTienChi;
        this.soTienThu = soTienThu;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public Double getSoTienChi() { return soTienChi; }
    public void setSoTienChi(Double soTienChi) { this.soTienChi = soTienChi; }

    public Double getSoTienThu() { return soTienThu; }
    public void setSoTienThu(Double soTienThu) { this.soTienThu = soTienThu; }
}