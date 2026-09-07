package kazuko.ex.model.khohang;

import java.util.HashMap;
import java.util.Map;

public class TonNhieuKhoItem {
    private String matHangId;
    private String maHang;
    private String tenHang;
    private String nhomHangId;
    private String nhomHangName;
    private String donViTinh;
    private final Map<String, Double> tonTheoKho = new HashMap<>();
    private double tongTon;
    private double ton2Dvt;
    private String donViTinh2;
    private String baoHanh;
    private double quyDoi = 1.0;
    private String hangSx;
    private double tonToiThieu;

    public TonNhieuKhoItem() {}

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

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Map<String, Double> getTonTheoKho() { return tonTheoKho; }

    public double getTonKho(String khoId) {
        if (khoId == null) return 0.0;
        return tonTheoKho.getOrDefault(khoId.trim(), 0.0);
    }

    public void setTonKho(String khoId, double ton) {
        if (khoId != null) {
            tonTheoKho.put(khoId.trim(), ton);
        }
    }

    public double getTongTon() { return tongTon; }
    public void setTongTon(double tongTon) {
        this.tongTon = tongTon;
        recalculate();
    }

    public double getTon2Dvt() { return ton2Dvt; }
    public void setTon2Dvt(double ton2Dvt) { this.ton2Dvt = ton2Dvt; }

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
        if (this.quyDoi > 0) {
            this.ton2Dvt = Math.round((this.tongTon / this.quyDoi) * 100.0) / 100.0;
        } else {
            this.ton2Dvt = 0.0;
        }
    }
}
