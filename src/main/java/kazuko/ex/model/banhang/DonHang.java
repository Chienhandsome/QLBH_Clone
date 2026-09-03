package kazuko.ex.model.banhang;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DonHang {
    private String id;
    private String name; // Số phiếu / Số HĐ (ví dụ: 26000001)
    private Date ngay;
    private String dienGiai;
    private double tienHang = 0.0;
    private double tienGiamGia = 0.0;
    private double tiLeGiamGia = 0.0;
    private double tienDoiTra = 0.0;
    private double tongCong = 0.0;
    private double tienThanhToan = 0.0;
    private double conLai = 0.0;
    private String khoXuatId;
    private String tenKhoXuat;
    private String khoNhapId;
    private String tenKhoNhap;
    private String nhanVienXuatId;
    private String tenNhanVienXuat;
    private String tenNhanVien;
    private String nhanVienNhapId;
    private String tenNhanVienNhap;
    private String khachHangId;
    private String tenKhach;
    private String diaChiGiaoHang;
    private String nhaCungCapId;
    private String tenNhaCungCap;
    private String cuaHangId;
    private String taiKhoanNganHangId;
    private String ghiChu;
    private String loaiGia = "Giá bán";
    private int loai = 0; // 0: Bán hàng, 1: Nhập kho, 2: Xuất khác, 3: Chuyển kho...
    private int status = 1; // 1: Đã lưu, 0: Lưu tạm, -1: Xóa
    private boolean isTam = false;

    private double tiLeThue = 0.0;
    private double tienThue = 0.0;
    private double phiVanChuyen = 0.0;
    private double tiLePhiVanChuyen = 0.0;
    private double tienMat = 0.0;
    private double tienChuyenKhoan = 0.0;
    private double tienTheATM = 0.0;
    private double tienVoucher = 0.0;
    private String maVoucher;
    private double tienTheTraTruoc = 0.0;
    private String maTheTraTruoc;
    private double tienTruTichLuy = 0.0;
    private String gioThanhToan;
    private String nguoiThanhToan;
    private String maKhach;
    private String diaChiKhach;
    private String dienThoaiKhach;
    private Date ngayHuy;
    private String gioHuy;
    private String thuNganHuy;
    private String lyDoHuy;

    private String tenKho;
    private String tenCuaHang;
    private Date timeCreated;
    private String userCreatedId;
    private String tenNguoiTao;
    private Date timeModified;
    private String userModifiedId;
    private String tenNguoiSua;

    private List<DonHangChiTiet> chiTietLayHang = new ArrayList<>();
    private List<DonHangChiTiet> chiTietTraHang = new ArrayList<>();

    public DonHang() {}

    public String getTenKho() { return tenKho; }
    public void setTenKho(String tenKho) { this.tenKho = tenKho; }

    public String getTenCuaHang() { return tenCuaHang; }
    public void setTenCuaHang(String tenCuaHang) { this.tenCuaHang = tenCuaHang; }

    public Date getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Date timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public String getTenNguoiTao() { return tenNguoiTao; }
    public void setTenNguoiTao(String tenNguoiTao) { this.tenNguoiTao = tenNguoiTao; }

    public Date getTimeModified() { return timeModified; }
    public void setTimeModified(Date timeModified) { this.timeModified = timeModified; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    public String getTenNguoiSua() { return tenNguoiSua; }
    public void setTenNguoiSua(String tenNguoiSua) { this.tenNguoiSua = tenNguoiSua; }

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

    public double getTienDoiTra() { return tienDoiTra; }
    public void setTienDoiTra(double tienDoiTra) { this.tienDoiTra = tienDoiTra; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public double getTienThanhToan() { return tienThanhToan; }
    public void setTienThanhToan(double tienThanhToan) { this.tienThanhToan = tienThanhToan; }

    public double getConLai() { return conLai; }
    public void setConLai(double conLai) { this.conLai = conLai; }

    public String getKhoXuatId() { return khoXuatId; }
    public void setKhoXuatId(String khoXuatId) { this.khoXuatId = khoXuatId; }

    public String getTenKhoXuat() { return tenKhoXuat; }
    public void setTenKhoXuat(String tenKhoXuat) { this.tenKhoXuat = tenKhoXuat; }

    public String getKhoNhapId() { return khoNhapId; }
    public void setKhoNhapId(String khoNhapId) { this.khoNhapId = khoNhapId; }

    public String getTenKhoNhap() { return tenKhoNhap; }
    public void setTenKhoNhap(String tenKhoNhap) { this.tenKhoNhap = tenKhoNhap; }

    public String getNhanVienXuatId() { return nhanVienXuatId; }
    public void setNhanVienXuatId(String nhanVienXuatId) { this.nhanVienXuatId = nhanVienXuatId; }

    public String getTenNhanVienXuat() { return tenNhanVienXuat; }
    public void setTenNhanVienXuat(String tenNhanVienXuat) { this.tenNhanVienXuat = tenNhanVienXuat; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getNhanVienNhapId() { return nhanVienNhapId; }
    public void setNhanVienNhapId(String nhanVienNhapId) { this.nhanVienNhapId = nhanVienNhapId; }

    public String getTenNhanVienNhap() { return tenNhanVienNhap; }
    public void setTenNhanVienNhap(String tenNhanVienNhap) { this.tenNhanVienNhap = tenNhanVienNhap; }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }

    public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
    public void setDiaChiGiaoHang(String diaChiGiaoHang) { this.diaChiGiaoHang = diaChiGiaoHang; }

    public String getNhaCungCapId() { return nhaCungCapId; }
    public void setNhaCungCapId(String nhaCungCapId) { this.nhaCungCapId = nhaCungCapId; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public double getTiLeThue() { return tiLeThue; }
    public void setTiLeThue(double tiLeThue) { this.tiLeThue = tiLeThue; }

    public double getTienThue() { return tienThue; }
    public void setTienThue(double tienThue) { this.tienThue = tienThue; }

    public String getCuaHangId() { return cuaHangId; }
    public void setCuaHangId(String cuaHangId) { this.cuaHangId = cuaHangId; }

    public String getTaiKhoanNganHangId() { return taiKhoanNganHangId; }
    public void setTaiKhoanNganHangId(String taiKhoanNganHangId) { this.taiKhoanNganHangId = taiKhoanNganHangId; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getLoaiGia() { return loaiGia; }
    public void setLoaiGia(String loaiGia) { this.loaiGia = loaiGia; }

    public int getLoai() { return loai; }
    public void setLoai(int loai) { this.loai = loai; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isTam() { return isTam; }
    public void setTam(boolean tam) { isTam = tam; }

    public List<DonHangChiTiet> getChiTietLayHang() { return chiTietLayHang; }
    public void setChiTietLayHang(List<DonHangChiTiet> chiTietLayHang) { this.chiTietLayHang = chiTietLayHang; }

    public List<DonHangChiTiet> getChiTietTraHang() { return chiTietTraHang; }
    public void setChiTietTraHang(List<DonHangChiTiet> chiTietTraHang) { this.chiTietTraHang = chiTietTraHang; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTiLePhiVanChuyen() { return tiLePhiVanChuyen; }
    public void setTiLePhiVanChuyen(double tiLePhiVanChuyen) { this.tiLePhiVanChuyen = tiLePhiVanChuyen; }

    public double getTienMat() { return tienMat; }
    public void setTienMat(double tienMat) { this.tienMat = tienMat; }

    public double getTienChuyenKhoan() { return tienChuyenKhoan; }
    public void setTienChuyenKhoan(double tienChuyenKhoan) { this.tienChuyenKhoan = tienChuyenKhoan; }

    public double getTienTheATM() { return tienTheATM; }
    public void setTienTheATM(double tienTheATM) { this.tienTheATM = tienTheATM; }

    public double getTienVoucher() { return tienVoucher; }
    public void setTienVoucher(double tienVoucher) { this.tienVoucher = tienVoucher; }

    public String getMaVoucher() { return maVoucher; }
    public void setMaVoucher(String maVoucher) { this.maVoucher = maVoucher; }

    public double getTienTheTraTruoc() { return tienTheTraTruoc; }
    public void setTienTheTraTruoc(double tienTheTraTruoc) { this.tienTheTraTruoc = tienTheTraTruoc; }

    public String getMaTheTraTruoc() { return maTheTraTruoc; }
    public void setMaTheTraTruoc(String maTheTraTruoc) { this.maTheTraTruoc = maTheTraTruoc; }

    public double getTienTruTichLuy() { return tienTruTichLuy; }
    public void setTienTruTichLuy(double tienTruTichLuy) { this.tienTruTichLuy = tienTruTichLuy; }

    public String getGioThanhToan() { return gioThanhToan; }
    public void setGioThanhToan(String gioThanhToan) { this.gioThanhToan = gioThanhToan; }

    public String getNguoiThanhToan() { return nguoiThanhToan; }
    public void setNguoiThanhToan(String nguoiThanhToan) { this.nguoiThanhToan = nguoiThanhToan; }

    public String getMaKhach() { return maKhach; }
    public void setMaKhach(String maKhach) { this.maKhach = maKhach; }

    public String getDiaChiKhach() { return diaChiKhach; }
    public void setDiaChiKhach(String diaChiKhach) { this.diaChiKhach = diaChiKhach; }

    public String getDienThoaiKhach() { return dienThoaiKhach; }
    public void setDienThoaiKhach(String dienThoaiKhach) { this.dienThoaiKhach = dienThoaiKhach; }

    public Date getNgayHuy() { return ngayHuy; }
    public void setNgayHuy(Date ngayHuy) { this.ngayHuy = ngayHuy; }

    public String getGioHuy() { return gioHuy; }
    public void setGioHuy(String gioHuy) { this.gioHuy = gioHuy; }

    public String getThuNganHuy() { return thuNganHuy; }
    public void setThuNganHuy(String thuNganHuy) { this.thuNganHuy = thuNganHuy; }

    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String lyDoHuy) { this.lyDoHuy = lyDoHuy; }
}
