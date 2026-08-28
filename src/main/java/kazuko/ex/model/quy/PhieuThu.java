package kazuko.ex.model.quy;

import java.util.Date;

public class PhieuThu {
    private String id;
    private String soPhieu;
    private Date ngay;
    private String phanLoaiId;
    private String tenPhanLoai;
    private String lyDoThu;
    private String chungTuGoc;
    private String loaiDoiTuong = "Đối tượng khác";
    private String tenDoiTuong;
    private String diaChi;
    private String nhanVienId;
    private String tenNhanVien;
    private String khachHangId;
    private String tenKhachHang;
    private String nhaCungCapId;
    private String tenNhaCungCap;
    private double soTien = 0.0;
    private boolean chuyenVaoTaiKhoan = false;
    private String taiKhoanNganHangId;
    private String tenTaiKhoanNganHang;
    private String cuaHangId;
    private String tenCuaHang;
    private boolean khongThayDoiCongNo = false;
    private String ghiChu;
    private int status = 1; // 1: Da luu, -1: Da huy/xoa
    private Date timeCreated;
    private String userCreatedId;

    public PhieuThu() {
        this.ngay = new Date();
        this.timeCreated = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public String getPhanLoaiId() { return phanLoaiId; }
    public void setPhanLoaiId(String phanLoaiId) { this.phanLoaiId = phanLoaiId; }

    public String getTenPhanLoai() { return tenPhanLoai; }
    public void setTenPhanLoai(String tenPhanLoai) { this.tenPhanLoai = tenPhanLoai; }

    public String getLyDoThu() { return lyDoThu; }
    public void setLyDoThu(String lyDoThu) { this.lyDoThu = lyDoThu; }

    public String getChungTuGoc() { return chungTuGoc; }
    public void setChungTuGoc(String chungTuGoc) { this.chungTuGoc = chungTuGoc; }

    public String getLoaiDoiTuong() { return loaiDoiTuong; }
    public void setLoaiDoiTuong(String loaiDoiTuong) { this.loaiDoiTuong = loaiDoiTuong; }

    public String getTenDoiTuong() { return tenDoiTuong; }
    public void setTenDoiTuong(String tenDoiTuong) { this.tenDoiTuong = tenDoiTuong; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getNhaCungCapId() { return nhaCungCapId; }
    public void setNhaCungCapId(String nhaCungCapId) { this.nhaCungCapId = nhaCungCapId; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public double getSoTien() { return soTien; }
    public void setSoTien(double soTien) { this.soTien = soTien; }

    public boolean isChuyenVaoTaiKhoan() { return chuyenVaoTaiKhoan; }
    public void setChuyenVaoTaiKhoan(boolean chuyenVaoTaiKhoan) { this.chuyenVaoTaiKhoan = chuyenVaoTaiKhoan; }

    public String getTaiKhoanNganHangId() { return taiKhoanNganHangId; }
    public void setTaiKhoanNganHangId(String taiKhoanNganHangId) { this.taiKhoanNganHangId = taiKhoanNganHangId; }

    public String getTenTaiKhoanNganHang() { return tenTaiKhoanNganHang; }
    public void setTenTaiKhoanNganHang(String tenTaiKhoanNganHang) { this.tenTaiKhoanNganHang = tenTaiKhoanNganHang; }

    public String getCuaHangId() { return cuaHangId; }
    public void setCuaHangId(String cuaHangId) { this.cuaHangId = cuaHangId; }

    public String getTenCuaHang() { return tenCuaHang; }
    public void setTenCuaHang(String tenCuaHang) { this.tenCuaHang = tenCuaHang; }

    public boolean isKhongThayDoiCongNo() { return khongThayDoiCongNo; }
    public void setKhongThayDoiCongNo(boolean khongThayDoiCongNo) { this.khongThayDoiCongNo = khongThayDoiCongNo; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Date getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Date timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }
}