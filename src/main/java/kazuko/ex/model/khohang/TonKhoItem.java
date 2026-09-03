package kazuko.ex.model.khohang;

public class TonKhoItem {
    private String matHangId;
    private String maHang;
    private String tenHang;
    private String nhomHangId;
    private String nhomHangName;
    private double ton;
    private String donViTinh;
    private double ton2Dvt;
    private double giaBan;
    private double giaTriBan;
    private double giaVon;
    private double giaTriVon;
    private String donViTinh2;
    private String baoHanh;
    private double quyDoi = 1.0;
    private String hangSx;
    private double tonToiThieu;

    public TonKhoItem() {}

    public String getMatHangId() { return matHangId; }
    public void setMatHangId(String matHangId) { this.matHangId = matHangId; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getNhomHangId() { return nhomHangId; }
    public void setNhomHangId(String nhomHangId) { this.nhomHangId = nhomHangId; }

    public String getNhomHangName() { return nhomHangName; }
    public void setNhomHangName(String nhomHangName) { this.nhomHangName = nhomHangName; }

    public double getTon() { return ton; }
    public void setTon(double ton) {
        this.ton = ton;
        recalculate();
    }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getTon2Dvt() { return ton2Dvt; }
    public void setTon2Dvt(double ton2Dvt) { this.ton2Dvt = ton2Dvt; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
        recalculate();
    }

    public double getGiaTriBan() { return giaTriBan; }
    public void setGiaTriBan(double giaTriBan) { this.giaTriBan = giaTriBan; }

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) {
        this.giaVon = giaVon;
        recalculate();
    }

    public double getGiaTriVon() { return giaTriVon; }
    public void setGiaTriVon(double giaTriVon) { this.giaTriVon = giaTriVon; }

    public String getDonViTinh2() { return donViTinh2; }
    public void setDonViTinh2(String donViTinh2) { this.donViTinh2 = donViTinh2; }

    public String getBaoHanh() { return baoHanh; }
    public void setBaoHanh(String baoHanh) { this.baoHanh = baoHanh; }

    public double getQuyDoi() { return quyDoi; }
    public void setQuyDoi(double quyDoi) {
        this.quyDoi = quyDoi;
        recalculate();
    }

    public String getHangSx() { return hangSx; }
    public void setHangSx(String hangSx) { this.hangSx = hangSx; }

    public double getTonToiThieu() { return tonToiThieu; }
    public void setTonToiThieu(double tonToiThieu) { this.tonToiThieu = tonToiThieu; }

    public void recalculate() {
        this.giaTriBan = this.ton * this.giaBan;
        this.giaTriVon = this.ton * this.giaVon;
        if (this.quyDoi > 0) {
            this.ton2Dvt = Math.round((this.ton / this.quyDoi) * 100.0) / 100.0;
        } else {
            this.ton2Dvt = 0.0;
        }
    }
}
