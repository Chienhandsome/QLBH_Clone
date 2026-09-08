package kazuko.ex.report.model;

public class BaoCaoLaiGopTheoMatHangReportRow {
    private int stt;
    private String groupName;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private double soLuong;
    private double doiTra;
    private double giaBanDonGia;
    private double giaBanThanhTien;
    private double giaBanGiamGia;
    private double giaBanTongCong;
    private double giaVonDonGia;
    private double giaVonGiaTri;
    private double loiNhuan;

    public BaoCaoLaiGopTheoMatHangReportRow() {}

    public BaoCaoLaiGopTheoMatHangReportRow(int stt, String groupName, String maHang, String tenHang, String donViTinh,
                                           double soLuong, double doiTra, double giaBanDonGia, double giaBanThanhTien,
                                           double giaBanGiamGia, double giaBanTongCong, double giaVonDonGia,
                                           double giaVonGiaTri, double loiNhuan) {
        this.stt = stt;
        this.groupName = groupName;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.doiTra = doiTra;
        this.giaBanDonGia = giaBanDonGia;
        this.giaBanThanhTien = giaBanThanhTien;
        this.giaBanGiamGia = giaBanGiamGia;
        this.giaBanTongCong = giaBanTongCong;
        this.giaVonDonGia = giaVonDonGia;
        this.giaVonGiaTri = giaVonGiaTri;
        this.loiNhuan = loiNhuan;
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

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDoiTra() { return doiTra; }
    public void setDoiTra(double doiTra) { this.doiTra = doiTra; }

    public double getGiaBanDonGia() { return giaBanDonGia; }
    public void setGiaBanDonGia(double giaBanDonGia) { this.giaBanDonGia = giaBanDonGia; }

    public double getGiaBanThanhTien() { return giaBanThanhTien; }
    public void setGiaBanThanhTien(double giaBanThanhTien) { this.giaBanThanhTien = giaBanThanhTien; }

    public double getGiaBanGiamGia() { return giaBanGiamGia; }
    public void setGiaBanGiamGia(double giaBanGiamGia) { this.giaBanGiamGia = giaBanGiamGia; }

    public double getGiaBanTongCong() { return giaBanTongCong; }
    public void setGiaBanTongCong(double giaBanTongCong) { this.giaBanTongCong = giaBanTongCong; }

    public double getGiaVonDonGia() { return giaVonDonGia; }
    public void setGiaVonDonGia(double giaVonDonGia) { this.giaVonDonGia = giaVonDonGia; }

    public double getGiaVonGiaTri() { return giaVonGiaTri; }
    public void setGiaVonGiaTri(double giaVonGiaTri) { this.giaVonGiaTri = giaVonGiaTri; }

    public double getLoiNhuan() { return loiNhuan; }
    public void setLoiNhuan(double loiNhuan) { this.loiNhuan = loiNhuan; }
}
