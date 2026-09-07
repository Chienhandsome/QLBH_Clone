package kazuko.ex.report.model;

public class TongHopThuChiTheoLyDoReportRow {
    private int stt;
    private String lyDoThuChi;
    private Double soTienChi;
    private Double soTienThu;

    public TongHopThuChiTheoLyDoReportRow() {}

    public TongHopThuChiTheoLyDoReportRow(int stt, String lyDoThuChi, Double soTienChi, Double soTienThu) {
        this.stt = stt;
        this.lyDoThuChi = lyDoThuChi;
        this.soTienChi = soTienChi;
        this.soTienThu = soTienThu;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getLyDoThuChi() { return lyDoThuChi; }
    public void setLyDoThuChi(String lyDoThuChi) { this.lyDoThuChi = lyDoThuChi; }

    public Double getSoTienChi() { return soTienChi; }
    public void setSoTienChi(Double soTienChi) { this.soTienChi = soTienChi; }

    public Double getSoTienThu() { return soTienThu; }
    public void setSoTienThu(Double soTienThu) { this.soTienThu = soTienThu; }
}