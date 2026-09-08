package kazuko.ex.report.model;

public class TienVanChuyenTheoNhanVienReportRow {
    private int stt;
    private String ngay;
    private String name;
    private double tongCong;
    private double trichNhanVien;

    public TienVanChuyenTheoNhanVienReportRow() {}

    public TienVanChuyenTheoNhanVienReportRow(int stt, String ngay, String name, double tongCong, double trichNhanVien) {
        this.stt = stt;
        this.ngay = ngay;
        this.name = name;
        this.tongCong = tongCong;
        this.trichNhanVien = trichNhanVien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public double getTrichNhanVien() { return trichNhanVien; }
    public void setTrichNhanVien(double trichNhanVien) { this.trichNhanVien = trichNhanVien; }
}
