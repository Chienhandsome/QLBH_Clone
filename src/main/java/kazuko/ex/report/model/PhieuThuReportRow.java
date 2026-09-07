package kazuko.ex.report.model;

public class PhieuThuReportRow {
    private int stt;
    private String soPhieu;
    private String ngayGroup; // vd: "Ngày: 07/09/2026"
    private String tenDoiTuong;
    private String diaChi;
    private String lyDoThuChi;
    private String dienGiai;
    private String chungTuGoc;
    private Double soTien;

    public PhieuThuReportRow() {}

    public PhieuThuReportRow(int stt, String soPhieu, String ngayGroup, String tenDoiTuong, String diaChi, String lyDoThuChi, String dienGiai, String chungTuGoc, Double soTien) {
        this.stt = stt;
        this.soPhieu = soPhieu;
        this.ngayGroup = ngayGroup;
        this.tenDoiTuong = tenDoiTuong;
        this.diaChi = diaChi;
        this.lyDoThuChi = lyDoThuChi;
        this.dienGiai = dienGiai;
        this.chungTuGoc = chungTuGoc;
        this.soTien = soTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getNgayGroup() { return ngayGroup; }
    public void setNgayGroup(String ngayGroup) { this.ngayGroup = ngayGroup; }

    public String getTenDoiTuong() { return tenDoiTuong; }
    public void setTenDoiTuong(String tenDoiTuong) { this.tenDoiTuong = tenDoiTuong; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getLyDoThuChi() { return lyDoThuChi; }
    public void setLyDoThuChi(String lyDoThuChi) { this.lyDoThuChi = lyDoThuChi; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public String getChungTuGoc() { return chungTuGoc; }
    public void setChungTuGoc(String chungTuGoc) { this.chungTuGoc = chungTuGoc; }

    public Double getSoTien() { return soTien; }
    public void setSoTien(Double soTien) { this.soTien = soTien; }
}
