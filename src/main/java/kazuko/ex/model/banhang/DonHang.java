package kazuko.ex.model.banhang;

import java.util.Date;

public class DonHang {
    private String id;
    private String name; // Số phiếu / Mã đơn hàng
    private Date ngay;
    private String dienGiai;
    private double tienHang;
    private double tienGiamGia;
    private double tiLeGiamGia;
    private double tongCong;
    private double tienThanhToan;
    private double conLai;
    private String khoXuatId;
    private String khoNhapId;
    private String nhanVienXuatId;
    private String khachHangId;
    private String nhaCungCapId;
    private String cuaHangId;
    private String taiKhoanNganHangId;
    private int loai; // 0: Bán hàng, 1: Nhập kho, 2: Xuất khác, 3: Chuyển kho...
    private int status;

    public DonHang() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(double tiLeGiamGia) { this.tiLeGiamGia = tiLeGiamGia; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public double getTienThanhToan() { return tienThanhToan; }
    public void setTienThanhToan(double tienThanhToan) { this.tienThanhToan = tienThanhToan; }

    public double getConLai() { return conLai; }
    public void setConLai(double conLai) { this.conLai = conLai; }

    public String getKhoXuatId() { return khoXuatId; }
    public void setKhoXuatId(String khoXuatId) { this.khoXuatId = khoXuatId; }

    public String getKhoNhapId() { return khoNhapId; }
    public void setKhoNhapId(String khoNhapId) { this.khoNhapId = khoNhapId; }

    public String getNhanVienXuatId() { return nhanVienXuatId; }
    public void setNhanVienXuatId(String nhanVienXuatId) { this.nhanVienXuatId = nhanVienXuatId; }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public String getNhaCungCapId() { return nhaCungCapId; }
    public void setNhaCungCapId(String nhaCungCapId) { this.nhaCungCapId = nhaCungCapId; }

    public String getCuaHangId() { return cuaHangId; }
    public void setCuaHangId(String cuaHangId) { this.cuaHangId = cuaHangId; }

    public String getTaiKhoanNganHangId() { return taiKhoanNganHangId; }
    public void setTaiKhoanNganHangId(String taiKhoanNganHangId) { this.taiKhoanNganHangId = taiKhoanNganHangId; }

    public int getLoai() { return loai; }
    public void setLoai(int loai) { this.loai = loai; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
