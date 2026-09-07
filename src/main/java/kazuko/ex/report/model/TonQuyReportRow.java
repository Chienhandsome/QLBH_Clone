package kazuko.ex.report.model;

public class TonQuyReportRow {
    private String tt;
    private String soPhieu;
    private String ngay;
    private String dienGiai;
    private String taoBoi;
    private String thoiGian;
    private Double thu;
    private Double chi;
    private Double ton;
    private boolean isHeaderRow;

    public TonQuyReportRow() {}

    public TonQuyReportRow(String tt, String soPhieu, String ngay, String dienGiai, String taoBoi, String thoiGian, Double thu, Double chi, Double ton, boolean isHeaderRow) {
        this.tt = tt;
        this.soPhieu = soPhieu;
        this.ngay = ngay;
        this.dienGiai = dienGiai;
        this.taoBoi = taoBoi;
        this.thoiGian = thoiGian;
        this.thu = thu;
        this.chi = chi;
        this.ton = ton;
        this.isHeaderRow = isHeaderRow;
    }

    public String getTt() { return tt; }
    public void setTt(String tt) { this.tt = tt; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public String getTaoBoi() { return taoBoi; }
    public void setTaoBoi(String taoBoi) { this.taoBoi = taoBoi; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public Double getThu() { return thu; }
    public void setThu(Double thu) { this.thu = thu; }

    public Double getChi() { return chi; }
    public void setChi(Double chi) { this.chi = chi; }

    public Double getTon() { return ton; }
    public void setTon(Double ton) { this.ton = ton; }

    public boolean isHeaderRow() { return isHeaderRow; }
    public void setHeaderRow(boolean headerRow) { isHeaderRow = headerRow; }
}