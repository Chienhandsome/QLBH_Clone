package kazuko.ex.report.model;

public class BieuDoDoanhSoTheoNhomReportRow {
    private String nhomHang;
    private Double doanhSo;
    private Double tyLe;

    public BieuDoDoanhSoTheoNhomReportRow() {}

    public BieuDoDoanhSoTheoNhomReportRow(String nhomHang, Double doanhSo, Double tyLe) {
        this.nhomHang = nhomHang;
        this.doanhSo = doanhSo;
        this.tyLe = tyLe;
    }

    public String getNhomHang() { return nhomHang; }
    public void setNhomHang(String nhomHang) { this.nhomHang = nhomHang; }

    public Double getDoanhSo() { return doanhSo; }
    public void setDoanhSo(Double doanhSo) { this.doanhSo = doanhSo; }

    public Double getTyLe() { return tyLe; }
    public void setTyLe(Double tyLe) { this.tyLe = tyLe; }
}
