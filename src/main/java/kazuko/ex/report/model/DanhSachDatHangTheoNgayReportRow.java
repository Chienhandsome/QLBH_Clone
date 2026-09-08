package kazuko.ex.report.model;

public class DanhSachDatHangTheoNgayReportRow {
    private int stt;
    private String soPhieu;
    private String khachHang;
    private String diaChi;
    private String dienThoai;
    private double tienHang;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double tongCong;

    public DanhSachDatHangTheoNgayReportRow() {}

    public DanhSachDatHangTheoNgayReportRow(int stt, String soPhieu, String khachHang, String diaChi,
                                           String dienThoai, double tienHang, double tienGiamGia,
                                           double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.soPhieu = soPhieu;
        this.khachHang = khachHang;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }
}
