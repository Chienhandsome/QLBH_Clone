package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;

public class MatHang {
    private String id;
    private String code;
    private String name;
    private String maSanCo;
    private double giaNhap = 0.0;
    private double giaBan = 0.0;
    private double giaVon = 0.0;
    private double giaBan2 = 0.0;
    private double giaBan3 = 0.0;
    private double giaBan4 = 0.0;
    private double giaBanChan = 0.0;
    private double quyDoi = 0.0;
    private double giamGiaPt = 0.0;
    private double giamGiaTien = 0.0;
    private String nhomMatHangId;
    private String donViTinhId;
    private String donViTinhChanId;
    private String tenNhom;
    private String tenDonViTinh;
    private String tenDonViTinhChan;
    private String ghiChu;
    private boolean khongInCheBien = false;
    private boolean banNhanh = false;
    private boolean khoiLuongTheoCan = false;
    private boolean coHanSuDung = false;
    private String doiTacKyGui;
    private String hangSanXuat;
    private String baoHanh;
    private double hoaHong = 0.0;
    private double tonToiThieu = 0.0;
    private double tonToiDa = 0.0;
    private int phuongThucTruKho = 0; // 0: kiem vat tu, 1: dinh luong combo, 2: ban kem, 3: dich vu, 4: vat tu nguyen lieu
    private String imagePath;
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

    public String getMaSanCo() { return maSanCo; }
    public void setMaSanCo(String maSanCo) { this.maSanCo = maSanCo; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    public double getGiaBan2() { return giaBan2; }
    public void setGiaBan2(double giaBan2) { this.giaBan2 = giaBan2; }

    public double getGiaBan3() { return giaBan3; }
    public void setGiaBan3(double giaBan3) { this.giaBan3 = giaBan3; }

    public double getGiaBan4() { return giaBan4; }
    public void setGiaBan4(double giaBan4) { this.giaBan4 = giaBan4; }

    public double getGiaBanChan() { return giaBanChan; }
    public void setGiaBanChan(double giaBanChan) { this.giaBanChan = giaBanChan; }

    public double getQuyDoi() { return quyDoi; }
    public void setQuyDoi(double quyDoi) { this.quyDoi = quyDoi; }

    public double getGiamGiaPt() { return giamGiaPt; }
    public void setGiamGiaPt(double giamGiaPt) { this.giamGiaPt = giamGiaPt; }

    public double getGiamGiaTien() { return giamGiaTien; }
    public void setGiamGiaTien(double giamGiaTien) { this.giamGiaTien = giamGiaTien; }

    public String getNhomMatHangId() { return nhomMatHangId; }
    public void setNhomMatHangId(String nhomMatHangId) { this.nhomMatHangId = nhomMatHangId; }

    public String getDonViTinhId() { return donViTinhId; }
    public void setDonViTinhId(String donViTinhId) { this.donViTinhId = donViTinhId; }

    public String getDonViTinhChanId() { return donViTinhChanId; }
    public void setDonViTinhChanId(String donViTinhChanId) { this.donViTinhChanId = donViTinhChanId; }

    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    public String getTenDonViTinhChan() { return tenDonViTinhChan; }
    public void setTenDonViTinhChan(String tenDonViTinhChan) { this.tenDonViTinhChan = tenDonViTinhChan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isKhongInCheBien() { return khongInCheBien; }
    public void setKhongInCheBien(boolean khongInCheBien) { this.khongInCheBien = khongInCheBien; }

    public boolean isBanNhanh() { return banNhanh; }
    public void setBanNhanh(boolean banNhanh) { this.banNhanh = banNhanh; }

    public boolean isKhoiLuongTheoCan() { return khoiLuongTheoCan; }
    public void setKhoiLuongTheoCan(boolean khoiLuongTheoCan) { this.khoiLuongTheoCan = khoiLuongTheoCan; }

    public boolean isCoHanSuDung() { return coHanSuDung; }
    public void setCoHanSuDung(boolean coHanSuDung) { this.coHanSuDung = coHanSuDung; }

    public String getDoiTacKyGui() { return doiTacKyGui; }
    public void setDoiTacKyGui(String doiTacKyGui) { this.doiTacKyGui = doiTacKyGui; }

    public String getHangSanXuat() { return hangSanXuat; }
    public void setHangSanXuat(String hangSanXuat) { this.hangSanXuat = hangSanXuat; }

    public String getBaoHanh() { return baoHanh; }
    public void setBaoHanh(String baoHanh) { this.baoHanh = baoHanh; }

    public double getHoaHong() { return hoaHong; }
    public void setHoaHong(double hoaHong) { this.hoaHong = hoaHong; }

    public double getTonToiThieu() { return tonToiThieu; }
    public void setTonToiThieu(double tonToiThieu) { this.tonToiThieu = tonToiThieu; }

    public double getTonToiDa() { return tonToiDa; }
    public void setTonToiDa(double tonToiDa) { this.tonToiDa = tonToiDa; }

    public int getPhuongThucTruKho() { return phuongThucTruKho; }
    public void setPhuongThucTruKho(int phuongThucTruKho) { this.phuongThucTruKho = phuongThucTruKho; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

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
