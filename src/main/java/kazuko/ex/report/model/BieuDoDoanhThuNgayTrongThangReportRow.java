package kazuko.ex.report.model;

public class BieuDoDoanhThuNgayTrongThangReportRow {
    private String thang;
    private String ngay;
    private Double doanhThu;

    public BieuDoDoanhThuNgayTrongThangReportRow() {}

    public BieuDoDoanhThuNgayTrongThangReportRow(String thang, String ngay, Double doanhThu) {
        this.thang = thang;
        this.ngay = ngay;
        this.doanhThu = doanhThu;
    }

    public String getThang() { return thang; }
    public void setThang(String thang) { this.thang = thang; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public Double getDoanhThu() { return doanhThu; }
    public void setDoanhThu(Double doanhThu) { this.doanhThu = doanhThu; }
}
