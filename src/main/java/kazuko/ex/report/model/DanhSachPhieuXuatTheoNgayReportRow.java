package kazuko.ex.report.model;

public class DanhSachPhieuXuatTheoNgayReportRow {
    private int stt;
    private String groupNgay;
    private String soPhieu;
    private String khoXuat;
    private String nhanVienXuat;
    private double tienHang;
    private double tienGiamGia;
    private double phiVanChuyen;
    private double tongCong;

    public DanhSachPhieuXuatTheoNgayReportRow() {}

    public DanhSachPhieuXuatTheoNgayReportRow(int stt, String groupNgay, String soPhieu, String khoXuat, String nhanVienXuat, double tienHang, double tienGiamGia, double phiVanChuyen, double tongCong) {
        this.stt = stt;
        this.groupNgay = groupNgay;
        this.soPhieu = soPhieu;
        this.khoXuat = khoXuat;
        this.nhanVienXuat = nhanVienXuat;
        this.tienHang = tienHang;
        this.tienGiamGia = tienGiamGia;
        this.phiVanChuyen = phiVanChuyen;
        this.tongCong = tongCong;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNgay() { return groupNgay; }
    public void setGroupNgay(String groupNgay) { this.groupNgay = groupNgay; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getKhoXuat() { return khoXuat; }
    public void setKhoXuat(String khoXuat) { this.khoXuat = khoXuat; }

    public String getNhanVienXuat() { return nhanVienXuat; }
    public void setNhanVienXuat(String nhanVienXuat) { this.nhanVienXuat = nhanVienXuat; }

    public double getTienHang() { return tienHang; }
    public void setTienHang(double tienHang) { this.tienHang = tienHang; }

    public double getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(double tienGiamGia) { this.tienGiamGia = tienGiamGia; }

    public double getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(double phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }
}
