package kazuko.ex.model.dathang;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatHangNCC {
    private String id;
    private String name; // Số phiếu: DMH26/00001
    private Date ngay = new Date();
    private String nhaCungCapId;
    private String tenNhaCungCap;
    private int giamTheoTien = 0;
    private int loai = 0;

    private double tienHang = 0.0;
    private double tiLeGiamGia = 0.0;
    private double tienGiamGia = 0.0;
    private double tiLeThue = 0.0;
    private double tienThue = 0.0;
    private double phiVanChuyen = 0.0;
    private double tongCong = 0.0;
    private String note;
    private int status = 1;

    private Timestamp timeCreated;
    private String userCreatedId;
    private String userCreatedName = "Administrator";
    private Timestamp timeModified;
    private String userModifiedId;
    private String userModifiedName = "Administrator";

    private List<DatHangNCCChiTiet> chiTiet = new ArrayList<>();

    public DatHangNCC() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public String getNhaCungCapId() { return nhaCungCapId; }
    public void setNhaCungCapId(String nhaCungCapId) { this.nhaCungCapId = nhaCungCapId; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public int getGiamTheoTien() { return giamTheoTien; }
    public void setGiamTheoTien(int giamTheoTien) { this.giamTheoTien = giamTheoTien; }

    public int getLoai() { return loai; }
    public void setLoai(int loai) { this.loai = loai; }

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

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public String getUserCreatedName() { return userCreatedName; }
    public void setUserCreatedName(String userCreatedName) { this.userCreatedName = userCreatedName; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    public String getUserModifiedName() { return userModifiedName; }
    public void setUserModifiedName(String userModifiedName) { this.userModifiedName = userModifiedName; }

    public List<DatHangNCCChiTiet> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<DatHangNCCChiTiet> chiTiet) { this.chiTiet = chiTiet; }
}
