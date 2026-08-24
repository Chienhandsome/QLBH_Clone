package kazuko.ex.model.danhmuc;

public class KhoHang {
    private String id;
    private String name;
    private String note;
    private String cuaHangId;
    private int status;

    public KhoHang() {}

    public KhoHang(String id, String name, String cuaHangId) {
        this.id = id;
        this.name = name;
        this.cuaHangId = cuaHangId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCuaHangId() { return cuaHangId; }
    public void setCuaHangId(String cuaHangId) { this.cuaHangId = cuaHangId; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    @Override
    public String toString() {
        return name;
    }
}
