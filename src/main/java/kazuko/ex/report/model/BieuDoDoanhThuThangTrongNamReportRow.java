package kazuko.ex.report.model;

public class BieuDoDoanhThuThangTrongNamReportRow {
    private String nam;
    private String thang;
    private Double doanhThu;

    public BieuDoDoanhThuThangTrongNamReportRow() {}

    public BieuDoDoanhThuThangTrongNamReportRow(String nam, String thang, Double doanhThu) {
        this.nam = nam;
        this.thang = thang;
        this.doanhThu = doanhThu;
    }

    public String getNam() { return nam; }
    public void setNam(String nam) { this.nam = nam; }

    public String getThang() { return thang; }
    public void setThang(String thang) { this.thang = thang; }

    public Double getDoanhThu() { return doanhThu; }
    public void setDoanhThu(Double doanhThu) { this.doanhThu = doanhThu; }
}
