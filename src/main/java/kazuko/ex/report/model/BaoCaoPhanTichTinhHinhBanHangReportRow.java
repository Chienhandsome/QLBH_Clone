package kazuko.ex.report.model;

public class BaoCaoPhanTichTinhHinhBanHangReportRow {
    // Bảng 1: Theo mặt hàng
    private String b1TenHang;
    private Double b1GiaBan;
    private Double b1SoLuong;
    private Double b1ThanhTien;

    // Bảng 2: Theo doanh số mặt hàng & Tỷ lệ
    private String b2TenHang;
    private Double b2SoLuong;
    private Double b2ThanhTien;
    private Double b2TyLe;

    // Bảng 3: Theo khoảng đơn giá & Tỷ lệ
    private Double b3DonGia;
    private Double b3SoLuong;
    private Double b3ThanhTien;
    private Double b3TyLe;

    private Boolean isTotal;

    public BaoCaoPhanTichTinhHinhBanHangReportRow() {}

    public String getB1TenHang() { return b1TenHang; }
    public void setB1TenHang(String b1TenHang) { this.b1TenHang = b1TenHang; }

    public Double getB1GiaBan() { return b1GiaBan; }
    public void setB1GiaBan(Double b1GiaBan) { this.b1GiaBan = b1GiaBan; }

    public Double getB1SoLuong() { return b1SoLuong; }
    public void setB1SoLuong(Double b1SoLuong) { this.b1SoLuong = b1SoLuong; }

    public Double getB1ThanhTien() { return b1ThanhTien; }
    public void setB1ThanhTien(Double b1ThanhTien) { this.b1ThanhTien = b1ThanhTien; }

    public String getB2TenHang() { return b2TenHang; }
    public void setB2TenHang(String b2TenHang) { this.b2TenHang = b2TenHang; }

    public Double getB2SoLuong() { return b2SoLuong; }
    public void setB2SoLuong(Double b2SoLuong) { this.b2SoLuong = b2SoLuong; }

    public Double getB2ThanhTien() { return b2ThanhTien; }
    public void setB2ThanhTien(Double b2ThanhTien) { this.b2ThanhTien = b2ThanhTien; }

    public Double getB2TyLe() { return b2TyLe; }
    public void setB2TyLe(Double b2TyLe) { this.b2TyLe = b2TyLe; }

    public Double getB3DonGia() { return b3DonGia; }
    public void setB3DonGia(Double b3DonGia) { this.b3DonGia = b3DonGia; }

    public Double getB3SoLuong() { return b3SoLuong; }
    public void setB3SoLuong(Double b3SoLuong) { this.b3SoLuong = b3SoLuong; }

    public Double getB3ThanhTien() { return b3ThanhTien; }
    public void setB3ThanhTien(Double b3ThanhTien) { this.b3ThanhTien = b3ThanhTien; }

    public Double getB3TyLe() { return b3TyLe; }
    public void setB3TyLe(Double b3TyLe) { this.b3TyLe = b3TyLe; }

    public Boolean getIsTotal() { return isTotal != null ? isTotal : Boolean.FALSE; }
    public void setIsTotal(Boolean total) { isTotal = total; }
}
