package kazuko.ex.model.nhansu;

import java.util.Date;

public class ThuongPhat {
    private String id;
    private String soPhieu;
    private Date ngay;
    private String nhanVienId;
    private String tenNhanVien;
    private double tienThuong;
    private double tienPhat;
    private String lyDoId;
    private String tenLyDo;
    private String ghiChu;
    private int status = 1; // 1: active, -1: trash
    private Date timeCreated;
    private String userCreatedId;

    public ThuongPhat() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public Date getNgay() { return ngay; }
    public void setNgay(Date ngay) { this.ngay = ngay; }

    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public double getTienThuong() { return tienThuong; }
    public void setTienThuong(double tienThuong) { this.tienThuong = tienThuong; }

    public double getTienPhat() { return tienPhat; }
    public void setTienPhat(double tienPhat) { this.tienPhat = tienPhat; }

    public String getLyDoId() { return lyDoId; }
    public void setLyDoId(String lyDoId) { this.lyDoId = lyDoId; }

    public String getTenLyDo() { return tenLyDo; }
    public void setTenLyDo(String tenLyDo) { this.tenLyDo = tenLyDo; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Date getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Date timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }
}
