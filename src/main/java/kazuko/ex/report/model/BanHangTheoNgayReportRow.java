package kazuko.ex.report.model;

public class BanHangTheoNgayReportRow {
    private int stt;
    private String groupNgay;
    private String soPhieu;
    private String nhanVienBan;
    private String thuNgan;
    private double tienHang;
    private double doiTra;
    private double giamGia;
    private double tongCong;
    private double tienMat;
    private double the;

    public BanHangTheoNgayReportRow() {}

    public BanHangTheoNgayReportRow(int stt, String groupNgay, String soPhieu, String nhanVienBan, String thuNgan, double tienHang, double doiTra, double giamGia, double tongCong, double tienMat, double the) {
        this.stt = stt;
        this.groupNgay = groupNgay;
        this.soPhieu = soPhieu;
        this.nhanVienBan = nhanVienBan;
        this.thuNgan = thuNgan;
        this.tienHang = tienHang;
        this.doiTra = doiTra;
        this.giamGia = giamGia;
        this.tongCong = tongCong;
        this.tienMat = tienMat;
        this.the = the;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNgay() { return groupNgay; }
    public void setGroupNgay(String groupNgay) { this.groupNgay = groupNgay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getNhanVienBan() { return nhanVienBan; }
    public void setNhanVienBan(String nhanVienBan) { this.nhanVienBan = nhanVienBan; }

    public String getThuNgan() { return thuNgan; }
    public void setThuNgan(String thuNgan) { this.thuNgan = thuNgan; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getDoiTra() { return doiTra; }
    public void setDoiTra(double doiTra) { this.doiTra = doiTra; }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public double getTienMat() { return tienMat; }
    public void setTienMat(double tienMat) { this.tienMat = tienMat; }

    public double getThe() { return the; }
    public void setThe(double the) { this.the = the; }
}