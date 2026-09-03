package kazuko.ex.model.banhang;

public class DonHangChiTiet {
    private String id;
    private String donHangId;
    private String matHangId;
    private String maHang;
    private String tenHang;
    private String donViTinhId;
    private String tenDonViTinh;
    private double donGia;
    private double soLuong = 1.0;
    private double slXuat = 1.0;
    private double slNhap = 0.0;
    private double tiLeGiamGia = 0.0;
    private double tienGiam = 0.0;
    private double thanhTien = 0.0;
    private String khoHangId;
    private String ghiChu;
    private String baoHanh;
    private String kichThuoc;
    private String hanSuDung;
    private boolean isTraHang = false;
    private double slHeThong = 0.0;
    private double slThucTe = 0.0;

    public DonHangChiTiet() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonHangId() { return donHangId; }
    public void setDonHangId(String donHangId) { this.donHangId = donHangId; }

    public String getMatHangId() { return matHangId; }
    public void setMatHangId(String matHangId) { this.matHangId = matHangId; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinhId() { return donViTinhId; }
    public void setDonViTinhId(String donViTinhId) { this.donViTinhId = donViTinhId; }

    public String getTenDonViTinh() { return tenDonViTinh; }
    public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) {
        this.donGia = donGia;
        tinhThanhTien();
    }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) {
        this.soLuong = soLuong;
        if (isTraHang) {
            this.slNhap = soLuong;
        } else if (slHeThong == 0.0 && slThucTe == 0.0 && slNhap == 0.0) {
            this.slXuat = soLuong;
        }
        tinhThanhTien();
    }

    public double getSlXuat() { return slXuat; }
    public void setSlXuat(double slXuat) {
        this.slXuat = slXuat;
        this.soLuong = slXuat;
        tinhThanhTien();
    }

    public double getSlNhap() { return slNhap; }
    public void setSlNhap(double slNhap) {
        this.slNhap = slNhap;
        this.soLuong = slNhap;
        tinhThanhTien();
    }

    public double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(double tiLeGiamGia) {
        this.tiLeGiamGia = tiLeGiamGia;
        tinhThanhTien();
    }

    public double getTienGiam() { return tienGiam; }
    public void setTienGiam(double tienGiam) { this.tienGiam = tienGiam; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public void tinhThanhTien() {
        double gross = this.soLuong * this.donGia;
        this.tienGiam = gross * (this.tiLeGiamGia / 100.0);
        this.thanhTien = gross - this.tienGiam;
    }

    public String getKhoHangId() { return khoHangId; }
    public void setKhoHangId(String khoHangId) { this.khoHangId = khoHangId; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getBaoHanh() { return baoHanh; }
    public void setBaoHanh(String baoHanh) { this.baoHanh = baoHanh; }

    public String getKichThuoc() { return kichThuoc; }
    public void setKichThuoc(String kichThuoc) { this.kichThuoc = kichThuoc; }

    public String getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(String hanSuDung) { this.hanSuDung = hanSuDung; }

    public boolean isTraHang() { return isTraHang; }
    public void setTraHang(boolean traHang) {
        isTraHang = traHang;
        if (traHang) {
            this.slNhap = this.soLuong;
            this.slXuat = 0;
        } else {
            this.slXuat = this.soLuong;
            this.slNhap = 0;
        }
    }

    public double getSlHeThong() { return slHeThong; }
    public void setSlHeThong(double slHeThong) {
        this.slHeThong = slHeThong;
        tinhChenhLechKiemKe();
    }

    public double getSlThucTe() { return slThucTe; }
    public void setSlThucTe(double slThucTe) {
        this.slThucTe = slThucTe;
        this.soLuong = slThucTe;
        tinhChenhLechKiemKe();
    }

    public void tinhChenhLechKiemKe() {
        double delta = slThucTe - slHeThong;
        if (delta > 0) {
            this.slNhap = delta;
            this.slXuat = 0.0;
        } else if (delta < 0) {
            this.slXuat = -delta;
            this.slNhap = 0.0;
        } else {
            this.slNhap = 0.0;
            this.slXuat = 0.0;
        }
    }
}
