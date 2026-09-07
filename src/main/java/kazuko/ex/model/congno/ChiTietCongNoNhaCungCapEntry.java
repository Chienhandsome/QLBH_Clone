package kazuko.ex.model.congno;

import java.util.Date;

public class ChiTietCongNoNhaCungCapEntry {
    private String soPhieu;
    private Date ngay;
    private double tongCong;
    private String dienGiai;
    private double tienThanhToan;
    private double luyKe;
    private String loaiChungTu; // "NHAPKHO" hoặc "PHIEUCHI"

    public ChiTietCongNoNhaCungCapEntry() {}

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public double getTienThanhToan() { return tienThanhToan; }
    public void setTienThanhToan(double tienThanhToan) { this.tienThanhToan = tienThanhToan; }

    public double getLuyKe() { return luyKe; }
    public void setLuyKe(double luyKe) { this.luyKe = luyKe; }

    public String getLoaiChungTu() { return loaiChungTu; }
    public void setLoaiChungTu(String loaiChungTu) { this.loaiChungTu = loaiChungTu; }
}
