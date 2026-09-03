package kazuko.ex.model.dathang;

import java.sql.Timestamp;

public class DatHangNCCChiTiet {
    private String id;
    private String datHangNCCId;
    private String matHangId;
    private String maHang;
    private String tenHang;
    private String donViTinhId;
    private String tenDonViTinh;
    private String baoHanh;
    private double soLuong = 1.0;
    private double donGia = 0.0;
    private double tiLeGiamGia = 0.0;
    private double thanhTien = 0.0;
    private double giaMuaGanNhat = 0.0;
    private String note;
    private int status = 1;

    private Timestamp timeCreated;
    private String userCreatedId;
    private Timestamp timeModified;
    private String userModifiedId;

    public DatHangNCCChiTiet() {}

    public void tinhThanhTien() {
        double subtotal = soLuong * donGia;
        double discount = subtotal * (tiLeGiamGia / 100.0);
        this.thanhTien = subtotal - discount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDatHangNCCId() { return datHangNCCId; }
    public void setDatHangNCCId(String datHangNCCId) { this.datHangNCCId = datHangNCCId; }

    public String getMatHangId() { return matHangId; }
    public void setMatHangId(String matHangId) { this.matHangId = matHangId; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinhId() { return donViTinhId; }
    public void setDonViTinhId(String donViTinhId) { this.donViTinhId = donViTinhId; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    public String getBaoHanh() { return baoHanh; }
    public void setBaoHanh(String baoHanh) { this.baoHanh = baoHanh; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(double tiLeGiamGia) { this.tiLeGiamGia = tiLeGiamGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public double getGiaMuaGanNhat() { return giaMuaGanNhat; }
    public void setGiaMuaGanNhat(double giaMuaGanNhat) { this.giaMuaGanNhat = giaMuaGanNhat; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }
}
