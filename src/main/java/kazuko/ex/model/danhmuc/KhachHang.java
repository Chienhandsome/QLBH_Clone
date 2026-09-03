package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;

public class KhachHang {
    private String id;
    private String maKhach;
    private String name;
    private String dienThoai;
    private String diaChi;
    private String email;
    private Timestamp ngaySinhNhat;
    private double diemTichLuyBanDau = 0;
    private double diemTichLuy = 0;
    private String giaBanApDung = "Giá theo danh mục";
    private String maSoThue;
    private String nhanVienId;
    private String tenNhanVien;
    private String nhomKhachHangId;
    private String tenNhomKhachHang;
    private String tinhThanh;
    private String facebook;
    private String ghiChu;
    private int status = 1; // 1: Active, -1: Trash

    private Timestamp timeCreated;
    private String userCreatedId;
    private String userCreatedName = "Administrator";
    private Timestamp timeModified;
    private String userModifiedId;
    private String userModifiedName = "Administrator";

    public KhachHang() {}

    public KhachHang(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public KhachHang(String id, String name, String maKhach) {
        this.id = id;
        this.name = name;
        this.maKhach = maKhach;
    }

    public KhachHang(String id, String maKhach, String name, String diaChi, String dienThoai, String tenNhomKhachHang) {
        this.id = id;
        this.maKhach = maKhach;
        this.name = name;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.tenNhomKhachHang = tenNhomKhachHang;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaKhach() { return maKhach; }
    public void setMaKhach(String maKhach) { this.maKhach = maKhach; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Timestamp getNgaySinhNhat() { return ngaySinhNhat; }
    public void setNgaySinhNhat(Timestamp ngaySinhNhat) { this.ngaySinhNhat = ngaySinhNhat; }

    public double getDiemTichLuyBanDau() { return diemTichLuyBanDau; }
    public void setDiemTichLuyBanDau(double diemTichLuyBanDau) { this.diemTichLuyBanDau = diemTichLuyBanDau; }

    public double getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(double diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    public String getGiaBanApDung() { return giaBanApDung; }
    public void setGiaBanApDung(String giaBanApDung) { this.giaBanApDung = giaBanApDung; }
    public void setGiaBanApDung(int index) { setGiaBanIndex(index); }

    public int getGiaBanIndex() {
        if ("Giá bán sỉ".equalsIgnoreCase(giaBanApDung)) return 1;
        if ("Giá bán lẻ".equalsIgnoreCase(giaBanApDung)) return 2;
        if ("Giá 1".equalsIgnoreCase(giaBanApDung)) return 3;
        if ("Giá 2".equalsIgnoreCase(giaBanApDung)) return 4;
        if ("Giá 3".equalsIgnoreCase(giaBanApDung)) return 5;
        if ("Giá 4".equalsIgnoreCase(giaBanApDung)) return 6;
        return 0; // Giá theo danh mục
    }

    public void setGiaBanIndex(int index) {
        switch (index) {
            case 1: this.giaBanApDung = "Giá bán sỉ"; break;
            case 2: this.giaBanApDung = "Giá bán lẻ"; break;
            case 3: this.giaBanApDung = "Giá 1"; break;
            case 4: this.giaBanApDung = "Giá 2"; break;
            case 5: this.giaBanApDung = "Giá 3"; break;
            case 6: this.giaBanApDung = "Giá 4"; break;
            default: this.giaBanApDung = "Giá theo danh mục"; break;
        }
    }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getNhomKhachHangId() { return nhomKhachHangId; }
    public void setNhomKhachHangId(String nhomKhachHangId) { this.nhomKhachHangId = nhomKhachHangId; }

    public String getTenNhomKhachHang() { return tenNhomKhachHang; }
    public void setTenNhomKhachHang(String tenNhomKhachHang) { this.tenNhomKhachHang = tenNhomKhachHang; }

    public String getTinhThanh() { return tinhThanh; }
    public void setTinhThanh(String tinhThanh) { this.tinhThanh = tinhThanh; }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

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
        return (name != null ? name : "") + (dienThoai != null && !dienThoai.isEmpty() ? " (" + dienThoai + ")" : "");
    }
}

