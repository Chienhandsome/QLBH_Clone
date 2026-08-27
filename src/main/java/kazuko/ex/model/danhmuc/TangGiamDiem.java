package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;

public class TangGiamDiem {
    private String id;
    private String name; // Số phiếu (ví dụ: TG26/00001)
    private String khachHangId;
    private String tenKhachHang;
    private Timestamp ngay;
    private double diemTang;
    private double diemGiam;
    private String lyDo;
    private String note;
    private int status = 30; // 30: Active, -1: Trash / Deleted
    private Timestamp timeCreated;
    private String userCreatedId;
    private Timestamp timeModified;
    private String userModifiedId;

    public TangGiamDiem() {}

    public TangGiamDiem(String id, String name, String khachHangId, Timestamp ngay, double diemTang, double diemGiam, String lyDo, String note) {
        this.id = id;
        this.name = name;
        this.khachHangId = khachHangId;
        this.ngay = ngay;
        this.diemTang = diemTang;
        this.diemGiam = diemGiam;
        this.lyDo = lyDo;
        this.note = note;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSoPhieu() { return name; }
    public void setSoPhieu(String soPhieu) { this.name = soPhieu; }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public Timestamp getNgay() { return ngay; }
    public void setNgay(Timestamp ngay) { this.ngay = ngay; }

    public double getDiemTang() { return diemTang; }
    public void setDiemTang(double diemTang) { this.diemTang = diemTang; }

    public double getDiemGiam() { return diemGiam; }
    public void setDiemGiam(double diemGiam) { this.diemGiam = diemGiam; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }
}
