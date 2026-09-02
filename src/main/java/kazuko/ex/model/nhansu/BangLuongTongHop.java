package kazuko.ex.model.nhansu;

public class BangLuongTongHop {
    private String id;
    private String bangLuongId;
    private String nhanVienId;
    private String tenNhanVien;
    private int cachTinhLuong = 30; // 30: theo ca, 60: theo tháng, 0: không tính
    private double luongCa = 0.0;
    private double luongThang = 0.0;
    private double tongLuong = 0.0;
    private double thuong = 0.0;
    private double phat = 0.0;
    private double tamUng = 0.0;
    private double thucNhan = 0.0;
    private double daNhan = 0.0;
    private String soPhieuChi;
    private int status = 1;

    public BangLuongTongHop() {}

    public BangLuongTongHop(String bangLuongId, String nhanVienId, String tenNhanVien) {
        this.bangLuongId = bangLuongId;
        this.nhanVienId = nhanVienId;
        this.tenNhanVien = tenNhanVien;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBangLuongId() { return bangLuongId; }
    public void setBangLuongId(String bangLuongId) { this.bangLuongId = bangLuongId; }

    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public int getCachTinhLuong() { return cachTinhLuong; }
    public void setCachTinhLuong(int cachTinhLuong) { this.cachTinhLuong = cachTinhLuong; }

    public double getLuongCa() { return luongCa; }
    public void setLuongCa(double luongCa) { this.luongCa = luongCa; }

    public double getLuongThang() { return luongThang; }
    public void setLuongThang(double luongThang) { this.luongThang = luongThang; }

    public double getTongLuong() { return tongLuong; }
    public void setTongLuong(double tongLuong) { this.tongLuong = tongLuong; }

    public double getThuong() { return thuong; }
    public void setThuong(double thuong) { this.thuong = thuong; }

    public double getPhat() { return phat; }
    public void setPhat(double phat) { this.phat = phat; }

    public double getTamUng() { return tamUng; }
    public void setTamUng(double tamUng) { this.tamUng = tamUng; }

    public double getThucNhan() { return thucNhan; }
    public void setThucNhan(double thucNhan) { this.thucNhan = thucNhan; }

    public double getDaNhan() { return daNhan; }
    public void setDaNhan(double daNhan) { this.daNhan = daNhan; }

    public String getSoPhieuChi() { return soPhieuChi; }
    public void setSoPhieuChi(String soPhieuChi) { this.soPhieuChi = soPhieuChi; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public double getLuongCoBan() {
        if (cachTinhLuong == 60) {
            return luongThang;
        } else if (cachTinhLuong == 30) {
            return luongCa;
        }
        return 0.0;
    }
}
