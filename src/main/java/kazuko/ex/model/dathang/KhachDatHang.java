package kazuko.ex.model.dathang;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class KhachDatHang {
    private String id;
    private String name; // Số phiếu đặt hàng (ví dụ: DH26/00001)
    private Timestamp ngay;
    private String dKhachHangId;
    private String tenKhach;
    private String diaChi;
    private String dienThoai;
    private String email;
    private String note;
    private double tienHang = 0.0;
    private double tiLeGiamGia = 0.0;
    private double tienGiamGia = 0.0;
    private double tiLeThue = 0.0;
    private double tienThue = 0.0;
    private double phiVanChuyen = 0.0;
    private double tongCong = 0.0;
    private int loai = 0;
    private int giamTheoTien = 0;
    private int status = 1;
    private String userCreatedId;
    private Timestamp timeCreated;
    private String userModifiedId;
    private Timestamp timeModified;

    private List<KhachDatHangChiTiet> chiTietList = new ArrayList<>();

    public KhachDatHang() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Timestamp getNgay() { return ngay; }
    public void setNgay(Timestamp ngay) { this.ngay = ngay; }

    public String getdKhachHangId() { return dKhachHangId; }
    public void setdKhachHangId(String dKhachHangId) { this.dKhachHangId = dKhachHangId; }

    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(double tiLeGiamGia) { this.tiLeGiamGia = tiLeGiamGia; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getTiLeThue() { return tiLeThue; }
    public void setTiLeThue(double tiLeThue) { this.tiLeThue = tiLeThue; }

    public double getTienThue() { return tienThue; }
    public void setTienThue(double tienThue) { this.tienThue = tienThue; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public int getLoai() { return loai; }
    public void setLoai(int loai) { this.loai = loai; }

    public int getGiamTheoTien() { return giamTheoTien; }
    public void setGiamTheoTien(int giamTheoTien) { this.giamTheoTien = giamTheoTien; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public List<KhachDatHangChiTiet> getChiTietList() { return chiTietList; }
    public void setChiTietList(List<KhachDatHangChiTiet> chiTietList) { this.chiTietList = chiTietList; }
}
