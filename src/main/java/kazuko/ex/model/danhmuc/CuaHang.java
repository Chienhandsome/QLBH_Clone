package kazuko.ex.model.danhmuc;

public class CuaHang {
    private String id;
    private String code;
    private String name;
    private String diaChi;
    private String dienThoai;
    private String khoHangId;

    public CuaHang() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getKhoHangId() { return khoHangId; }
    public void setKhoHangId(String khoHangId) { this.khoHangId = khoHangId; }

    @Override
    public String toString() {
        return name;
    }
}
