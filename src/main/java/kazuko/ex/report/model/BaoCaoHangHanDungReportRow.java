package kazuko.ex.report.model;

public class BaoCaoHangHanDungReportRow {
    private int stt;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private double ton;
    private String hanDung;

    public BaoCaoHangHanDungReportRow() {}

    public BaoCaoHangHanDungReportRow(int stt, String maHang, String tenHang, String donViTinh, double ton, String hanDung) {
        this.stt = stt;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.ton = ton;
        this.hanDung = hanDung;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getTon() { return ton; }
    public void setTon(double ton) { this.ton = ton; }

    public String getHanDung() { return hanDung; }
    public void setHanDung(String hanDung) { this.hanDung = hanDung; }
}
