package kazuko.ex.report.model;

public class BaoCaoTongHopXuatNhapTonReportRow {
    private int stt;
    private String groupName;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private double giaVon;
    private double tonDauSl;
    private double tonDauGt;
    private double nhapSl;
    private double nhapGt;
    private double xuatSl;
    private double xuatGt;
    private double tonCuoiSl;
    private double tonCuoiGt;

    public BaoCaoTongHopXuatNhapTonReportRow() {}

    public BaoCaoTongHopXuatNhapTonReportRow(int stt, String groupName, String maHang, String tenHang, String donViTinh,
                                            double giaVon, double tonDauSl, double tonDauGt,
                                            double nhapSl, double nhapGt, double xuatSl, double xuatGt,
                                            double tonCuoiSl, double tonCuoiGt) {
        this.stt = stt;
        this.groupName = groupName;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.giaVon = giaVon;
        this.tonDauSl = tonDauSl;
        this.tonDauGt = tonDauGt;
        this.nhapSl = nhapSl;
        this.nhapGt = nhapGt;
        this.xuatSl = xuatSl;
        this.xuatGt = xuatGt;
        this.tonCuoiSl = tonCuoiSl;
        this.tonCuoiGt = tonCuoiGt;
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

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    public double getTonDauSl() { return tonDauSl; }
    public void setTonDauSl(double tonDauSl) { this.tonDauSl = tonDauSl; }

    public double getTonDauGt() { return tonDauGt; }
    public void setTonDauGt(double tonDauGt) { this.tonDauGt = tonDauGt; }

    public double getNhapSl() { return nhapSl; }
    public void setNhapSl(double nhapSl) { this.nhapSl = nhapSl; }

    public double getNhapGt() { return nhapGt; }
    public void setNhapGt(double nhapGt) { this.nhapGt = nhapGt; }

    public double getXuatSl() { return xuatSl; }
    public void setXuatSl(double xuatSl) { this.xuatSl = xuatSl; }

    public double getXuatGt() { return xuatGt; }
    public void setXuatGt(double xuatGt) { this.xuatGt = xuatGt; }

    public double getTonCuoiSl() { return tonCuoiSl; }
    public void setTonCuoiSl(double tonCuoiSl) { this.tonCuoiSl = tonCuoiSl; }

    public double getTonCuoiGt() { return tonCuoiGt; }
    public void setTonCuoiGt(double tonCuoiGt) { this.tonCuoiGt = tonCuoiGt; }
}
