package kazuko.ex.model.danhmuc;

public class DotKhuyenMaiMuaTangChiTiet {
    private String id;
    private String dotKhuyenMaiId;

    // Mặt hàng mua
    private String mathangMuaId;
    private String maHangMua;
    private String tenHangMua;
    private String donViTinhMua;
    private double soLuongMua = 1.0;

    // Mặt hàng tặng
    private String mathangTangId;
    private String maHangTang;
    private String tenHangTang;
    private String donViTinhTang;
    private double soLuongTang = 1.0;

    private boolean tangCungLoai = true;

    public DotKhuyenMaiMuaTangChiTiet() {}

    public DotKhuyenMaiMuaTangChiTiet(String mathangMuaId, String maHangMua, String tenHangMua, String donViTinhMua, double soLuongMua,
                                     String mathangTangId, String maHangTang, String tenHangTang, String donViTinhTang, double soLuongTang,
                                     boolean tangCungLoai) {
        this.mathangMuaId = mathangMuaId;
        this.maHangMua = maHangMua;
        this.tenHangMua = tenHangMua;
        this.donViTinhMua = donViTinhMua;
        this.soLuongMua = soLuongMua;
        this.mathangTangId = mathangTangId;
        this.maHangTang = maHangTang;
        this.tenHangTang = tenHangTang;
        this.donViTinhTang = donViTinhTang;
        this.soLuongTang = soLuongTang;
        this.tangCungLoai = tangCungLoai;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDotKhuyenMaiId() {
        return dotKhuyenMaiId;
    }

    public void setDotKhuyenMaiId(String dotKhuyenMaiId) {
        this.dotKhuyenMaiId = dotKhuyenMaiId;
    }

    public String getMathangMuaId() {
        return mathangMuaId;
    }

    public void setMathangMuaId(String mathangMuaId) {
        this.mathangMuaId = mathangMuaId;
    }

    public String getMaHangMua() {
        return maHangMua;
    }

    public void setMaHangMua(String maHangMua) {
        this.maHangMua = maHangMua;
    }

    public String getTenHangMua() {
        return tenHangMua;
    }

    public void setTenHangMua(String tenHangMua) {
        this.tenHangMua = tenHangMua;
    }

    public String getDonViTinhMua() {
        return donViTinhMua;
    }

    public void setDonViTinhMua(String donViTinhMua) {
        this.donViTinhMua = donViTinhMua;
    }

    public double getSoLuongMua() {
        return soLuongMua;
    }

    public void setSoLuongMua(double soLuongMua) {
        this.soLuongMua = soLuongMua;
    }

    public String getMathangTangId() {
        return mathangTangId;
    }

    public void setMathangTangId(String mathangTangId) {
        this.mathangTangId = mathangTangId;
    }

    public String getMaHangTang() {
        return maHangTang;
    }

    public void setMaHangTang(String maHangTang) {
        this.maHangTang = maHangTang;
    }

    public String getTenHangTang() {
        return tenHangTang;
    }

    public void setTenHangTang(String tenHangTang) {
        this.tenHangTang = tenHangTang;
    }

    public String getDonViTinhTang() {
        return donViTinhTang;
    }

    public void setDonViTinhTang(String donViTinhTang) {
        this.donViTinhTang = donViTinhTang;
    }

    public double getSoLuongTang() {
        return soLuongTang;
    }

    public void setSoLuongTang(double soLuongTang) {
        this.soLuongTang = soLuongTang;
    }

    public boolean isTangCungLoai() {
        return tangCungLoai;
    }

    public void setTangCungLoai(boolean tangCungLoai) {
        this.tangCungLoai = tangCungLoai;
    }
}
