package kazuko.ex.report.model;

public class TongHopHoaHongTheoNVKDReportRow {
    private int stt;
    private String nhanVien;
    private String maHang;
    private String matHang;
    private String dvt;
    private double slBan;
    private double slTra;
    private double hoaHong;
    private double tongHoaHong;

    public TongHopHoaHongTheoNVKDReportRow() {}

    public TongHopHoaHongTheoNVKDReportRow(int stt, String nhanVien, String maHang, String matHang,
                                           String dvt, double slBan, double slTra,
                                           double hoaHong, double tongHoaHong) {
        this.stt = stt;
        this.nhanVien = nhanVien;
        this.maHang = maHang;
        this.matHang = matHang;
        this.dvt = dvt;
        this.slBan = slBan;
        this.slTra = slTra;
        this.hoaHong = hoaHong;
        this.tongHoaHong = tongHoaHong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getNhanVien() { return nhanVien; }
    public void setNhanVien(String nhanVien) { this.nhanVien = nhanVien; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getSlBan() { return slBan; }
    public void setSlBan(double slBan) { this.slBan = slBan; }

    public double getSlTra() { return slTra; }
    public void setSlTra(double slTra) { this.slTra = slTra; }

    public double getHoaHong() { return hoaHong; }
    public void setHoaHong(double hoaHong) { this.hoaHong = hoaHong; }

    public double getTongHoaHong() { return tongHoaHong; }
    public void setTongHoaHong(double tongHoaHong) { this.tongHoaHong = tongHoaHong; }
}
