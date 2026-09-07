package kazuko.ex.model.congno;

import java.util.Date;

public class ChiTietCongNoKhachHangEntry {
    private String soPhieu;
    private Date ngay;
    private double tongCong;
    private double tienThanhToan;
    private String dienGiai;
    private double luyKe;
    private String loaiChungTu; // "DONHANG" hoặc "PHIEUTHU"

    public ChiTietCongNoKhachHangEntry() {}

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public double getTienThanhToan() { return tienThanhToan; }
    public void setTienThanhToan(double tienThanhToan) { this.tienThanhToan = tienThanhToan; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public double getLuyKe() { return luyKe; }
    public void setLuyKe(double luyKe) { this.luyKe = luyKe; }

    public String getLoaiChungTu() { return loaiChungTu; }
    public void setLoaiChungTu(String loaiChungTu) { this.loaiChungTu = loaiChungTu; }
}
