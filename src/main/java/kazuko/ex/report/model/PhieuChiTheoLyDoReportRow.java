package kazuko.ex.report.model;

public class PhieuChiTheoLyDoReportRow {
    private int stt;
    private String soPhieu;
    private String ngay;
    private String lyDoGroup; // vd: "Lý do thu chi: Tạm ứng lương"
    private String tenDoiTuong;
    private String diaChi;
    private String dienGiai;
    private String chungTuGoc;
    private Double soTien;

    public PhieuChiTheoLyDoReportRow() {}

    public PhieuChiTheoLyDoReportRow(int stt, String soPhieu, String ngay, String lyDoGroup, String tenDoiTuong, String diaChi, String dienGiai, String chungTuGoc, Double soTien) {
        this.stt = stt;
        this.soPhieu = soPhieu;
        this.ngay = ngay;
        this.lyDoGroup = lyDoGroup;
        this.tenDoiTuong = tenDoiTuong;
        this.diaChi = diaChi;
        this.dienGiai = dienGiai;
        this.chungTuGoc = chungTuGoc;
        this.soTien = soTien;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getLyDoGroup() { return lyDoGroup; }
    public void setLyDoGroup(String lyDoGroup) { this.lyDoGroup = lyDoGroup; }

    public String getTenDoiTuong() { return tenDoiTuong; }
    public void setTenDoiTuong(String tenDoiTuong) { this.tenDoiTuong = tenDoiTuong; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienGiai() { return dienGiai; }
    public void setDienGiai(String dienGiai) { this.dienGiai = dienGiai; }

    public String getChungTuGoc() { return chungTuGoc; }
    public void setChungTuGoc(String chungTuGoc) { this.chungTuGoc = chungTuGoc; }

    public Double getSoTien() { return soTien; }
    public void setSoTien(Double soTien) { this.soTien = soTien; }
}