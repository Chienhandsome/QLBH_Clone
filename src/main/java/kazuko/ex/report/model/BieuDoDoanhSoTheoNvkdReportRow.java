package kazuko.ex.report.model;

public class BieuDoDoanhSoTheoNvkdReportRow {
    private String nhanVien;
    private Double doanhSo;
    private Double tyLe;

    public BieuDoDoanhSoTheoNvkdReportRow() {}

    public BieuDoDoanhSoTheoNvkdReportRow(String nhanVien, Double doanhSo, Double tyLe) {
        this.nhanVien = nhanVien;
        this.doanhSo = doanhSo;
        this.tyLe = tyLe;
    }

    public String getNhanVien() { return nhanVien; }
    public void setNhanVien(String nhanVien) { this.nhanVien = nhanVien; }

    public Double getDoanhSo() { return doanhSo; }
    public void setDoanhSo(Double doanhSo) { this.doanhSo = doanhSo; }

    public Double getTyLe() { return tyLe; }
    public void setTyLe(Double tyLe) { this.tyLe = tyLe; }
}
