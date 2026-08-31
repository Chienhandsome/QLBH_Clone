package kazuko.ex.model.nhansu;

import java.util.Date;

public class BangLuong {
    private String id;
    private String soPhieu;
    private int thang;
    private int nam;
    private String ghiChu;
    private int status = 1; // 1: active, -1: trash
    private Date timeCreated;
    private String userCreatedId;

    public BangLuong() {}

    public BangLuong(String id, String soPhieu, int thang, int nam) {
        this.id = id;
        this.soPhieu = soPhieu;
        this.thang = thang;
        this.nam = nam;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Date getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Date timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    @Override
    public String toString() {
        return soPhieu != null ? soPhieu : ("Tháng " + thang + "/" + nam);
    }
}
