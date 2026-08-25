package kazuko.ex.model.system;

import java.util.LinkedHashMap;
import java.util.Map;

public class SystemConfig {
    // 1. Thông tin chung
    private String tenCongTy = "Công ty TNHH Ngọc Châu Âu";
    private String diaChi = "455 Sư Vạn Hạnh, P. 12, Q. 10, TPHCM";
    private String logoPath = null;
    private String soDienThoai = "(08) 6264 5788";
    private String email = "info@ngocchauau.vn";
    private String loiCamOn = "Xin cảm ơn Quý khách/ Thank you!";
    private String fax = "";
    private String mauHoaDon = "Mẫu in bill 54mm";
    private boolean luaChonMauKhiIn = true;
    private boolean hienThiTruocKhiIn = true;

    // 2. Máy in
    private Map<String, String> printerSettings = new LinkedHashMap<>();

    // 3. Số phiếu
    private Map<String, String> voucherRules = new LinkedHashMap<>();

    public SystemConfig() {
        initDefaultPrinters();
        initDefaultVoucherRules();
    }

    private void initDefaultPrinters() {
        String[] types = {
                "Báo giá", "Đơn hàng", "Phiếu nhập kho", "Phiếu xuất kho",
                "Phiếu chuyển kho", "Phiếu kiểm kê", "Sản xuất", "Đặt hàng",
                "Phiếu thu", "Phiếu chi", "Phiếu thu công nợ", "Bảng lương", "Đặt hàng nhà cung cấp"
        };
        for (String type : types) {
            printerSettings.put(type, "");
        }
    }

    private void initDefaultVoucherRules() {
        voucherRules.put("Báo giá", "BG(yy)/(*****)");
        voucherRules.put("Đơn hàng", "(CH)(yy)(******)");
        voucherRules.put("Phiếu nhập kho", "PN(yy)/(*****)");
        voucherRules.put("Phiếu xuất kho", "PX(yy)/(*****)");
        voucherRules.put("Phiếu chuyển kho", "PCK(yy)/(*****)");
        voucherRules.put("Phiếu kiểm kê", "PKK(yy)/(*****)");
        voucherRules.put("Sản xuất", "SX(yy)/(****)");
        voucherRules.put("Đặt hàng", "DH(yy)/(*****)");
        voucherRules.put("Phiếu thu", "PT(yy)/(*****)");
        voucherRules.put("Phiếu chi", "PC(yy)/(*****)");
        voucherRules.put("Phiếu thu công nợ", "PTCN(yy)/(*****)");
        voucherRules.put("Bảng lương", ""); // Bảng lương để trống theo yêu cầu
        voucherRules.put("Đặt hàng nhà cung cấp", "DMH(yy)/(******)");
    }

    public String getTenCongTy() { return tenCongTy; }
    public void setTenCongTy(String tenCongTy) { this.tenCongTy = tenCongTy; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLoiCamOn() { return loiCamOn; }
    public void setLoiCamOn(String loiCamOn) { this.loiCamOn = loiCamOn; }

    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }

    public String getMauHoaDon() { return mauHoaDon; }
    public void setMauHoaDon(String mauHoaDon) { this.mauHoaDon = mauHoaDon; }

    public boolean isLuaChonMauKhiIn() { return luaChonMauKhiIn; }
    public void setLuaChonMauKhiIn(boolean luaChonMauKhiIn) { this.luaChonMauKhiIn = luaChonMauKhiIn; }

    public boolean isHienThiTruocKhiIn() { return hienThiTruocKhiIn; }
    public void setHienThiTruocKhiIn(boolean hienThiTruocKhiIn) { this.hienThiTruocKhiIn = hienThiTruocKhiIn; }

    public Map<String, String> getPrinterSettings() { return printerSettings; }
    public void setPrinterSettings(Map<String, String> printerSettings) { this.printerSettings = printerSettings; }

    public Map<String, String> getVoucherRules() { return voucherRules; }
    public void setVoucherRules(Map<String, String> voucherRules) { this.voucherRules = voucherRules; }
}
