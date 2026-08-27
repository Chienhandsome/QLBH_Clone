package kazuko.ex.model.danhmuc;

public class KhachHangLoyaltySummary {
    private KhachHang khachHang;
    private double doanhSo;
    private int soHoaDon;
    private double diemTichLuy;

    public KhachHangLoyaltySummary() {}

    public KhachHangLoyaltySummary(KhachHang khachHang, double doanhSo, int soHoaDon, double diemTichLuy) {
        this.khachHang = khachHang;
        this.doanhSo = doanhSo;
        this.soHoaDon = soHoaDon;
        this.diemTichLuy = diemTichLuy;
    }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public double getDoanhSo() { return doanhSo; }
    public void setDoanhSo(double doanhSo) { this.doanhSo = doanhSo; }

    public int getSoHoaDon() { return soHoaDon; }
    public void setSoHoaDon(int soHoaDon) { this.soHoaDon = soHoaDon; }

    public double getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(double diemTichLuy) { this.diemTichLuy = diemTichLuy; }
}
