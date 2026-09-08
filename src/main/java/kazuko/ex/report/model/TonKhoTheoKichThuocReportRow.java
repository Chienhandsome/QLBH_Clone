package kazuko.ex.report.model;

public class TonKhoTheoKichThuocReportRow {
    private int stt;
    private String groupNhom;
    private String maHang;
    private String tenHang;
    private String dvt;
    private double ton;
    private double chiTiet;

    public TonKhoTheoKichThuocReportRow() {}

    public TonKhoTheoKichThuocReportRow(int stt, String groupNhom, String maHang, String tenHang, String dvt, double ton, double chiTiet) {
        this.stt = stt;
        this.groupNhom = groupNhom;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.dvt = dvt;
        this.ton = ton;
        this.chiTiet = chiTiet;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNhom() { return groupNhom; }
    public void setGroupNhom(String groupNhom) { this.groupNhom = groupNhom; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getTon() { return ton; }
    public void setTon(double ton) { this.ton = ton; }

    public double getChiTiet() { return chiTiet; }
    public void setChiTiet(double chiTiet) { this.chiTiet = chiTiet; }
}
