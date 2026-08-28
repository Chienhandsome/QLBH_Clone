package kazuko.ex.model.dathang;

import java.sql.Timestamp;

public class KhachDatHangChiTiet {
    private String id;
    private String tKhachDatHangId;
    private String dMatHangId;
    private String maHang;
    private String tenHang;
    private String dDonViTinhId;
    private String tenDonViTinh;
    private double soLuong = 1.0;
    private double donGia = 0.0;
    private double tiLeGiamGia = 0.0;
    private double thanhTien = 0.0;
    private String note;
    private int status = 1;
    private String userCreatedId;
    private Timestamp timeCreated;
    private String userModifiedId;
    private Timestamp timeModified;

    // Trường phụ trợ hiển thị
    private String soPhieuDatHang;

    public KhachDatHangChiTiet() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String gettKhachDatHangId() { return tKhachDatHangId; }
    public void settKhachDatHangId(String tKhachDatHangId) { this.tKhachDatHangId = tKhachDatHangId; }

    public String getdMatHangId() { return dMatHangId; }
    public void setdMatHangId(String dMatHangId) { this.dMatHangId = dMatHangId; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getdDonViTinhId() { return dDonViTinhId; }
    public void setdDonViTinhId(String dDonViTinhId) { this.dDonViTinhId = dDonViTinhId; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) {
        this.soLuong = soLuong;
        tinhThanhTien();
    }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) {
        this.donGia = donGia;
        tinhThanhTien();
    }

    public double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(double tiLeGiamGia) {
        this.tiLeGiamGia = tiLeGiamGia;
        tinhThanhTien();
    }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public void tinhThanhTien() {
        this.thanhTien = this.soLuong * this.donGia * (1.0 - (this.tiLeGiamGia / 100.0));
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

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

    public String getSoPhieuDatHang() { return soPhieuDatHang; }
    public void setSoPhieuDatHang(String soPhieuDatHang) { this.soPhieuDatHang = soPhieuDatHang; }
}
