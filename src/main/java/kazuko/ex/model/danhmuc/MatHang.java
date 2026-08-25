package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;

public class MatHang {
    private String id;
    private String code;
    private String name;
    private double giaNhap;
    private double giaBan;
    private double giaVon;
    private double giamGiaPt = 0.0;
    private double giamGiaTien = 0.0;
    private String nhomMatHangId;
    private String donViTinhId;
    private String tenNhom;
    private String tenDonViTinh;
    private String ghiChu;
    private boolean khongInCheBien = false;
    private boolean banNhanh = false;
    private int status = 1;
    private Timestamp timeCreated;
    private String userCreatedId;
    private String userCreatedName = "Administrator";
    private Timestamp timeModified;
    private String userModifiedId;
    private String userModifiedName = "Administrator";

    public MatHang() {}

    public MatHang(String id, String code, String name, double giaBan, double giaNhap) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.giaBan = giaBan;
        this.giaNhap = giaNhap;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    public double getGiamGiaPt() { return giamGiaPt; }
    public void setGiamGiaPt(double giamGiaPt) { this.giamGiaPt = giamGiaPt; }

    public double getGiamGiaTien() { return giamGiaTien; }
    public void setGiamGiaTien(double giamGiaTien) { this.giamGiaTien = giamGiaTien; }

    public String getNhomMatHangId() { return nhomMatHangId; }
    public void setNhomMatHangId(String nhomMatHangId) { this.nhomMatHangId = nhomMatHangId; }

    public String getDonViTinhId() { return donViTinhId; }
    public void setDonViTinhId(String donViTinhId) { this.donViTinhId = donViTinhId; }

    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isKhongInCheBien() { return khongInCheBien; }
    public void setKhongInCheBien(boolean khongInCheBien) { this.khongInCheBien = khongInCheBien; }

    public boolean isBanNhanh() { return banNhanh; }
    public void setBanNhanh(boolean banNhanh) { this.banNhanh = banNhanh; }

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

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
