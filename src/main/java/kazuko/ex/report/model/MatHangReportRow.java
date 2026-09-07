package kazuko.ex.report.model;

public class MatHangReportRow {
    private int stt;
    private String groupHeader;
    private String maHang;
    private String tenMatHang;
    private double giaBan;
    private double tonToiDa;
    private double tonToiThieu;

    public MatHangReportRow() {}

    public MatHangReportRow(int stt, String groupHeader, String maHang, String tenMatHang, double giaBan, double tonToiDa, double tonToiThieu) {
        this.stt = stt;
        this.groupHeader = groupHeader;
        this.maHang = maHang;
        this.tenMatHang = tenMatHang;
        this.giaBan = giaBan;
        this.tonToiDa = tonToiDa;
        this.tonToiThieu = tonToiThieu;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupHeader() { return groupHeader; }
    public void setGroupHeader(String groupHeader) { this.groupHeader = groupHeader; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenMatHang() { return tenMatHang; }
    public void setTenMatHang(String tenMatHang) { this.tenMatHang = tenMatHang; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public double getTonToiDa() { return tonToiDa; }
    public void setTonToiDa(double tonToiDa) { this.tonToiDa = tonToiDa; }

    public double getTonToiThieu() { return tonToiThieu; }
    public void setTonToiThieu(double tonToiThieu) { this.tonToiThieu = tonToiThieu; }
}