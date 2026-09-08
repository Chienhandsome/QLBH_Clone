package kazuko.ex.report.model;

public class BaoCaoHangTonKhoReportRow {
    private int stt;
    private String groupName;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private double ton;
    private double tonToiThieu;
    private double tonToiDa;
    private double giaVon;
    private double giaTriVon;
    private double giaBan;
    private double giaTriBan;

    public BaoCaoHangTonKhoReportRow() {}

    public BaoCaoHangTonKhoReportRow(int stt, String groupName, String maHang, String tenHang, String donViTinh,
                                    double ton, double tonToiThieu, double tonToiDa,
                                    double giaVon, double giaTriVon, double giaBan, double giaTriBan) {
        this.stt = stt;
        this.groupName = groupName;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.ton = ton;
        this.tonToiThieu = tonToiThieu;
        this.tonToiDa = tonToiDa;
        this.giaVon = giaVon;
        this.giaTriVon = giaTriVon;
        this.giaBan = giaBan;
        this.giaTriBan = giaTriBan;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getTon() { return ton; }
    public void setTon(double ton) { this.ton = ton; }

    public double getTonToiThieu() { return tonToiThieu; }
    public void setTonToiThieu(double tonToiThieu) { this.tonToiThieu = tonToiThieu; }

    public double getTonToiDa() { return tonToiDa; }
    public void setTonToiDa(double tonToiDa) { this.tonToiDa = tonToiDa; }

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    public double getGiaTriVon() { return giaTriVon; }
    public void setGiaTriVon(double giaTriVon) { this.giaTriVon = giaTriVon; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public double getGiaTriBan() { return giaTriBan; }
    public void setGiaTriBan(double giaTriBan) { this.giaTriBan = giaTriBan; }
}
