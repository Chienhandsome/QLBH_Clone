package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;

public class DiemHoaDon {
    private String id;
    private Timestamp ngay;
    private String soHoaDon;
    private double doanhSo;
    private double diemTichLuy;
    private String ghiChu;

    public DiemHoaDon() {}

    public DiemHoaDon(String id, Timestamp ngay, String soHoaDon, double doanhSo, double diemTichLuy, String ghiChu) {
        this.id = id;
        this.ngay = ngay;
        this.soHoaDon = soHoaDon;
        this.doanhSo = doanhSo;
        this.diemTichLuy = diemTichLuy;
        this.ghiChu = ghiChu;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Timestamp getNgay() { return ngay; }
    public void setNgay(Timestamp ngay) { this.ngay = ngay; }

    public String getSoHoaDon() { return soHoaDon; }
    public void setSoHoaDon(String soHoaDon) { this.soHoaDon = soHoaDon; }

    public double getDoanhSo() { return doanhSo; }
    public void setDoanhSo(double doanhSo) { this.doanhSo = doanhSo; }

    public double getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(double diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
