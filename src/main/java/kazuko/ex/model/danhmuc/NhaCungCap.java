package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;

public class NhaCungCap {
    private String id;
    private String maNhaCungCap;
    private String name;
    private String diaChi;
    private String dienThoai;
    private String email;
    private String website;
    private String nhomNhaCungCapId;
    private String tenNhomNhaCungCap;
    private String note;
    private int status = 1; // 1, 30: Active, -1: Trash

    private Timestamp timeCreated;
    private String userCreatedId;
    private String userCreatedName = "Administrator";
    private Timestamp timeModified;
    private String userModifiedId;
    private String userModifiedName = "Administrator";

    public NhaCungCap() {}

    public NhaCungCap(String id, String maNhaCungCap, String name, String diaChi, String dienThoai, String tenNhomNhaCungCap) {
        this.id = id;
        this.maNhaCungCap = maNhaCungCap;
        this.name = name;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.tenNhomNhaCungCap = tenNhomNhaCungCap;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getNhomNhaCungCapId() { return nhomNhaCungCapId; }
    public void setNhomNhaCungCapId(String nhomNhaCungCapId) { this.nhomNhaCungCapId = nhomNhaCungCapId; }

    public String getTenNhomNhaCungCap() { return tenNhomNhaCungCap; }
    public void setTenNhomNhaCungCap(String tenNhomNhaCungCap) { this.tenNhomNhaCungCap = tenNhomNhaCungCap; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public String getUserCreatedName() { return userCreatedName; }
    public void setUserCreatedName(String userCreatedName) { this.userCreatedName = userCreatedName; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    public String getUserModifiedName() { return userModifiedName; }
    public void setUserModifiedName(String userModifiedName) { this.userModifiedName = userModifiedName; }

    @Override
    public String toString() {
        return (name != null ? name : "") + (dienThoai != null && !dienThoai.isEmpty() ? " (" + dienThoai + ")" : "");
    }
}
