package kazuko.ex.report.model;

import java.io.Serializable;

public class TongHopMatHangChuyenKhoReportRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int stt;
    private String groupName;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private Double soLuong;
    private Double donGia;
    private Double thanhTien;

    public TongHopMatHangChuyenKhoReportRow() {
    }

    public TongHopMatHangChuyenKhoReportRow(int stt, String groupName, String maHang, String tenHang, String donViTinh, Double soLuong, Double donGia, Double thanhTien) {
        this.stt = stt;
        this.groupName = groupName;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public Double getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Double soLuong) {
        this.soLuong = soLuong;
    }

    public Double getDonGia() {
        return donGia;
    }

    public void setDonGia(Double donGia) {
        this.donGia = donGia;
    }

    public Double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Double thanhTien) {
        this.thanhTien = thanhTien;
    }
}
