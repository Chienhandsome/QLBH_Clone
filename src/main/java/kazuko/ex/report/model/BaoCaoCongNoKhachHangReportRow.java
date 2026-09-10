package kazuko.ex.report.model;

/**
 * Một dòng trên báo cáo đối chiếu công nợ của một khách hàng.
 * Các giá trị số được phép null để JasperReports in ô trống ở dòng chứng từ.
 */
public class BaoCaoCongNoKhachHangReportRow {
    private String ngay;
    private String tenHangHoa;
    private Double soLuong;
    private Double donGia;
    private Double thanhTien;
    private Double tongGiaTriDonHang;
    private Double thanhToan;
    private Double conNo;
    private String ghiChu;
    private String loaiDong;

    public BaoCaoCongNoKhachHangReportRow() {}

    public BaoCaoCongNoKhachHangReportRow(String ngay, String tenHangHoa,
                                          Double soLuong, Double donGia, Double thanhTien,
                                          Double tongGiaTriDonHang, Double thanhToan, Double conNo,
                                          String ghiChu, String loaiDong) {
        this.ngay = ngay;
        this.tenHangHoa = tenHangHoa;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.tongGiaTriDonHang = tongGiaTriDonHang;
        this.thanhToan = thanhToan;
        this.conNo = conNo;
        this.ghiChu = ghiChu;
        this.loaiDong = loaiDong;
    }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }
    public String getTenHangHoa() { return tenHangHoa; }
    public void setTenHangHoa(String tenHangHoa) { this.tenHangHoa = tenHangHoa; }
    public Double getSoLuong() { return soLuong; }
    public void setSoLuong(Double soLuong) { this.soLuong = soLuong; }
    public Double getDonGia() { return donGia; }
    public void setDonGia(Double donGia) { this.donGia = donGia; }
    public Double getThanhTien() { return thanhTien; }
    public void setThanhTien(Double thanhTien) { this.thanhTien = thanhTien; }
    public Double getTongGiaTriDonHang() { return tongGiaTriDonHang; }
    public void setTongGiaTriDonHang(Double tongGiaTriDonHang) { this.tongGiaTriDonHang = tongGiaTriDonHang; }
    public Double getThanhToan() { return thanhToan; }
    public void setThanhToan(Double thanhToan) { this.thanhToan = thanhToan; }
    public Double getConNo() { return conNo; }
    public void setConNo(Double conNo) { this.conNo = conNo; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getLoaiDong() { return loaiDong; }
    public void setLoaiDong(String loaiDong) { this.loaiDong = loaiDong; }
}
