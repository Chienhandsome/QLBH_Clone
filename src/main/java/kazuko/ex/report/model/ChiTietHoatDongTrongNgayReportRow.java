package kazuko.ex.report.model;

public class ChiTietHoatDongTrongNgayReportRow {
    private Integer sectionType; // 1: BanHang, 2: MatHang, 3: MatHangSummary
    private Integer stt;
    
    // Ban Hang fields
    private String soHd;
    private String gioTt;
    private String khachHang;
    private Double tongCong;
    private Double tienMat;
    private Double chuyenKhoan;
    private Double theAtm;
    private Double conNo;

    // Mat Hang fields
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private Double soLuong;
    private Double donGia;
    private Double tiLeGiamGia;
    private Double thanhTien;

    // Mat Hang Summary label
    private String summaryLabel;

    public ChiTietHoatDongTrongNgayReportRow() {}

    // Factory for Ban Hang
    public static ChiTietHoatDongTrongNgayReportRow createBanHangRow(int stt, String soHd, String gioTt, String khachHang, double tongCong, double tienMat, double chuyenKhoan, double theAtm, double conNo) {
        ChiTietHoatDongTrongNgayReportRow r = new ChiTietHoatDongTrongNgayReportRow();
        r.setSectionType(1);
        r.setStt(stt);
        r.setSoHd(soHd);
        r.setGioTt(gioTt);
        r.setKhachHang(khachHang);
        r.setTongCong(tongCong);
        r.setTienMat(tienMat);
        r.setChuyenKhoan(chuyenKhoan);
        r.setTheAtm(theAtm);
        r.setConNo(conNo);
        return r;
    }

    // Factory for Mat Hang
    public static ChiTietHoatDongTrongNgayReportRow createMatHangRow(int stt, String maHang, String tenHang, String donViTinh, double soLuong, double donGia, double tiLeGiamGia, double thanhTien) {
        ChiTietHoatDongTrongNgayReportRow r = new ChiTietHoatDongTrongNgayReportRow();
        r.setSectionType(2);
        r.setStt(stt);
        r.setMaHang(maHang);
        r.setTenHang(tenHang);
        r.setDonViTinh(donViTinh);
        r.setSoLuong(soLuong);
        r.setDonGia(donGia);
        r.setTiLeGiamGia(tiLeGiamGia);
        r.setThanhTien(thanhTien);
        return r;
    }

    // Factory for Mat Hang Summary
    public static ChiTietHoatDongTrongNgayReportRow createSummaryRow(String summaryLabel, Double soLuong, Double thanhTien) {
        ChiTietHoatDongTrongNgayReportRow r = new ChiTietHoatDongTrongNgayReportRow();
        r.setSectionType(3);
        r.setSummaryLabel(summaryLabel);
        r.setSoLuong(soLuong);
        r.setThanhTien(thanhTien);
        return r;
    }

    public Integer getSectionType() { return sectionType; }
    public void setSectionType(Integer sectionType) { this.sectionType = sectionType; }

    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }

    public String getSoHd() { return soHd; }
    public void setSoHd(String soHd) { this.soHd = soHd; }

    public String getGioTt() { return gioTt; }
    public void setGioTt(String gioTt) { this.gioTt = gioTt; }

    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }

    public Double getTongCong() { return tongCong; }
    public void setTongCong(Double tongCong) { this.tongCong = tongCong; }

    public Double getTienMat() { return tienMat; }
    public void setTienMat(Double tienMat) { this.tienMat = tienMat; }

    public Double getChuyenKhoan() { return chuyenKhoan; }
    public void setChuyenKhoan(Double chuyenKhoan) { this.chuyenKhoan = chuyenKhoan; }

    public Double getTheAtm() { return theAtm; }
    public void setTheAtm(Double theAtm) { this.theAtm = theAtm; }

    public Double getConNo() { return conNo; }
    public void setConNo(Double conNo) { this.conNo = conNo; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Double getSoLuong() { return soLuong; }
    public void setSoLuong(Double soLuong) { this.soLuong = soLuong; }

    public Double getDonGia() { return donGia; }
    public void setDonGia(Double donGia) { this.donGia = donGia; }

    public Double getTiLeGiamGia() { return tiLeGiamGia; }
    public void setTiLeGiamGia(Double tiLeGiamGia) { this.tiLeGiamGia = tiLeGiamGia; }

    public Double getThanhTien() { return thanhTien; }
    public void setThanhTien(Double thanhTien) { this.thanhTien = thanhTien; }

    public String getSummaryLabel() { return summaryLabel; }
    public void setSummaryLabel(String summaryLabel) { this.summaryLabel = summaryLabel; }
}
