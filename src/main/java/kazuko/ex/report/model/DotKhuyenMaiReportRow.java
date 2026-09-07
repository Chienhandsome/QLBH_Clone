package kazuko.ex.report.model;

public class DotKhuyenMaiReportRow {
    private int stt;
    private String tenDotKhuyenMai;
    private String tuNgay;
    private String denNgay;
    private String loaiHinh;

    public DotKhuyenMaiReportRow() {}

    public DotKhuyenMaiReportRow(int stt, String tenDotKhuyenMai, String tuNgay, String denNgay, String loaiHinh) {
        this.stt = stt;
        this.tenDotKhuyenMai = tenDotKhuyenMai;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.loaiHinh = loaiHinh;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getTenDotKhuyenMai() { return tenDotKhuyenMai; }
    public void setTenDotKhuyenMai(String tenDotKhuyenMai) { this.tenDotKhuyenMai = tenDotKhuyenMai; }

    public String getTuNgay() { return tuNgay; }
    public void setTuNgay(String tuNgay) { this.tuNgay = tuNgay; }

    public String getDenNgay() { return denNgay; }
    public void setDenNgay(String denNgay) { this.denNgay = denNgay; }

    public String getLoaiHinh() { return loaiHinh; }
    public void setLoaiHinh(String loaiHinh) { this.loaiHinh = loaiHinh; }
}