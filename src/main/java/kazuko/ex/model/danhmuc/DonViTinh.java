package kazuko.ex.model.danhmuc;

public class DonViTinh {
    private String id;
    private String code;
    private String name;
    private String ghiChu;
    private int status = 1;

    public DonViTinh() {}

    public DonViTinh(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
