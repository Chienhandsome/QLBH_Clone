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

    // 4. Bán hàng
    private boolean choPhepTrungTenKhachHang = true;
    private boolean choPhepNhapGiamGia = true;
    private int macDinhGiamGiaPt = 0;
    private boolean banHangDungDauDocMaVach = true;
    private int soLanIn = 1;
    private boolean hienThiCuaSoNhapSoLuongKhiQuet = false;
    private boolean choPhepThayDoiNgayTrenHoaDon = true;
    private boolean coPhiVanChuyen = false;
    private boolean coThueSuat = false;
    private int macDinhThueSuat = 0;
    private boolean batBuocNhapKhachHang = false;
    private boolean batBuocNhapNhanVienBanHang = false;
    private boolean choPhepInTamTinh = true;
    private boolean batBuocDoiTraHangTheoDon = true;
    private int soNgayChoPhepDoiTra = 7;
    private boolean hienThiAnhSanPhamTrongGiaoDien = true;
    private boolean kichHoatKhuyenMaiTuDong = true;
    private boolean hienThiMatHangKhuyenMaiKhiThanhToan = true;
    private boolean coGiaoHang = false;
    private boolean suDungGiaTheoKhach = false;
    private boolean suDungGiamGiaMacDinhTheoMatHang = true;
    private String lamTronTien = "1,000";

    // 5. Thanh toán
    private boolean coThanhToanVoucher = false;
    private boolean choPhepKhachNo = false;
    private boolean luaChonVoucherTuDanhSach = false;
    private boolean suDungTheTraTruoc = false;
    private boolean suDungDiemTichLuyDeThanhToan = false;
    private String quyDoi1DiemSangTien = "1,000";
    private boolean coThanhToanThe = true;
    private boolean coThanhToanChuyenKhoan = false;

    // 6. Tích điểm
    private String doanhSoTuongUng1Diem = "20,000";
    private String cachTinhDiem = "Điểm được tính trên từng hóa đơn";
    private boolean tuDongNangCapThanhVien = true;
    private boolean hienThiDiemKhachHangTrenHoaDon = true;
    private boolean khongTichDiemNeuCoKhuyenMai = false;

    // 7. Kho hàng
    private boolean choPhepNhapMotMatHangNhieuLan = false;
    private boolean batBuocChonNhanVienNhapKho = false;
    private boolean batBuocChonNhaCungCapNhapKho = false;
    private boolean suDung2DonViTinh = false;
    private boolean tuDongTinhGiaVon = true;
    private boolean nhapKhoBangDauDocMaVach = true;
    private String storageLocation = "Giá vốn";
    private String giaSuDungKhiChuyenXuatKho = "Giá vốn";
    private boolean suDungChucNangSanXuat = false;

    // 8. Mặt hàng
    private boolean suDungMaSanCo = false;
    private boolean suDungMaHang = true;
    private boolean tuDongSinhMaVach = true;
    private int chieuDaiMaVach = 7;
    private boolean coKyTuKiemTra = false;
    private boolean suDungTemInBangCanDienTu = true;
    private String chuoiBatDauCanDienTu = "29";
    private String timTheo = "Cả tên hàng và mã hàng";
    private String cachTim = "Tìm có chứa";
    private boolean choPhepTrungTenMatHang = true;
    private boolean hangCoBaoHanh = true;
    private boolean suDungHangSanXuat = true;
    private boolean suDungGia2 = false;
    private String dienGiaiGia2 = "Đơn giá 2";
    private boolean suDungGia3 = false;
    private String dienGiaiGia3 = "Giá bán 3";
    private boolean suDungGia4 = false;
    private String dienGiaiGia4 = "Giá bán 4";
    private boolean hienThiDeLuaChonNeuCoNhieuGia = true;
    private String sapXepThuTuTheo = "Mã hàng";
    private boolean matHangCoHanSuDung = false;
    private boolean matHangCoKichThuoc = false;
    private boolean matHangCoVatTuDinhLuong = true;
    private boolean giaBanLeUSD = true;

    // 9. Cảnh báo
    private boolean hienThiCanhBao = true;
    private boolean thongBaoHangDuoiMucAnToan = false;
    private boolean thongBaoKhachDenNgaySinhNhat = false;
    private boolean thongBaoHangHetHanDung = false;
    private boolean thongBaoHangSapHetHanDung = false;
    private int thongBaoTruocNgay = 3;

    // 10. Cảm ứng
    private boolean inPhaCheKhiInHoaDon = false;
    private String mauInPhaChe = "Mẫu in chế biến";
    private String mayInPhaChe = "Microsoft XPS Document Writer";
    private boolean inPhaCheKhiLuuTam = false;
    private String mauInPhaCheLuuTam = "";

    // 11. Quản trị
    private boolean phieuGanNhatNamPhiaDuoi = false;
    private String phimNhapLieuTrenLuoi = "Tab để sang ngang, Enter để xuống dòng";
    private boolean tuDongSaoLuuDuLieu = true;
    private String duongDanSaoLuu = "";
    private int soNgaySaoLuu = 1;
    private String chucNangMacDinh = "Hóa đơn bán hàng";
    private boolean locDuLieuBoKhoangTrong = false;
    private boolean hienThiXuongDongNeuDoRongCotNho = false;
    private String doToChuHienThi = "Nhỏ (110%)";
    private String giaoDienChuongTrinh = "Office 2010 - Blue";
    private boolean tuDongTaiLaiDuLieuKhiChuyenTab = true;
    private boolean kichHoatLuuVetHoatDong = true;
    private boolean heThongChayNhieuMayTram = true;

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

    // Getters & Setters Bán hàng
    public boolean isChoPhepTrungTenKhachHang() { return choPhepTrungTenKhachHang; }
    public void setChoPhepTrungTenKhachHang(boolean choPhepTrungTenKhachHang) { this.choPhepTrungTenKhachHang = choPhepTrungTenKhachHang; }

    public boolean isChoPhepNhapGiamGia() { return choPhepNhapGiamGia; }
    public void setChoPhepNhapGiamGia(boolean choPhepNhapGiamGia) { this.choPhepNhapGiamGia = choPhepNhapGiamGia; }

    public int getMacDinhGiamGiaPt() { return macDinhGiamGiaPt; }
    public void setMacDinhGiamGiaPt(int macDinhGiamGiaPt) { this.macDinhGiamGiaPt = macDinhGiamGiaPt; }

    public boolean isBanHangDungDauDocMaVach() { return banHangDungDauDocMaVach; }
    public void setBanHangDungDauDocMaVach(boolean banHangDungDauDocMaVach) { this.banHangDungDauDocMaVach = banHangDungDauDocMaVach; }

    public int getSoLanIn() { return soLanIn; }
    public void setSoLanIn(int soLanIn) { this.soLanIn = soLanIn; }

    public boolean isHienThiCuaSoNhapSoLuongKhiQuet() { return hienThiCuaSoNhapSoLuongKhiQuet; }
    public void setHienThiCuaSoNhapSoLuongKhiQuet(boolean hienThiCuaSoNhapSoLuongKhiQuet) { this.hienThiCuaSoNhapSoLuongKhiQuet = hienThiCuaSoNhapSoLuongKhiQuet; }

    public boolean isChoPhepThayDoiNgayTrenHoaDon() { return choPhepThayDoiNgayTrenHoaDon; }
    public void setChoPhepThayDoiNgayTrenHoaDon(boolean choPhepThayDoiNgayTrenHoaDon) { this.choPhepThayDoiNgayTrenHoaDon = choPhepThayDoiNgayTrenHoaDon; }

    public boolean isCoPhiVanChuyen() { return coPhiVanChuyen; }
    public void setCoPhiVanChuyen(boolean coPhiVanChuyen) { this.coPhiVanChuyen = coPhiVanChuyen; }

    public boolean isCoThueSuat() { return coThueSuat; }
    public void setCoThueSuat(boolean coThueSuat) { this.coThueSuat = coThueSuat; }

    public int getMacDinhThueSuat() { return macDinhThueSuat; }
    public void setMacDinhThueSuat(int macDinhThueSuat) { this.macDinhThueSuat = macDinhThueSuat; }

    public boolean isBatBuocNhapKhachHang() { return batBuocNhapKhachHang; }
    public void setBatBuocNhapKhachHang(boolean batBuocNhapKhachHang) { this.batBuocNhapKhachHang = batBuocNhapKhachHang; }

    public boolean isBatBuocNhapNhanVienBanHang() { return batBuocNhapNhanVienBanHang; }
    public void setBatBuocNhapNhanVienBanHang(boolean batBuocNhapNhanVienBanHang) { this.batBuocNhapNhanVienBanHang = batBuocNhapNhanVienBanHang; }

    public boolean isChoPhepInTamTinh() { return choPhepInTamTinh; }
    public void setChoPhepInTamTinh(boolean choPhepInTamTinh) { this.choPhepInTamTinh = choPhepInTamTinh; }

    public boolean isBatBuocDoiTraHangTheoDon() { return batBuocDoiTraHangTheoDon; }
    public void setBatBuocDoiTraHangTheoDon(boolean batBuocDoiTraHangTheoDon) { this.batBuocDoiTraHangTheoDon = batBuocDoiTraHangTheoDon; }

    public int getSoNgayChoPhepDoiTra() { return soNgayChoPhepDoiTra; }
    public void setSoNgayChoPhepDoiTra(int soNgayChoPhepDoiTra) { this.soNgayChoPhepDoiTra = soNgayChoPhepDoiTra; }

    public boolean isHienThiAnhSanPhamTrongGiaoDien() { return hienThiAnhSanPhamTrongGiaoDien; }
    public void setHienThiAnhSanPhamTrongGiaoDien(boolean hienThiAnhSanPhamTrongGiaoDien) { this.hienThiAnhSanPhamTrongGiaoDien = hienThiAnhSanPhamTrongGiaoDien; }

    public boolean isKichHoatKhuyenMaiTuDong() { return kichHoatKhuyenMaiTuDong; }
    public void setKichHoatKhuyenMaiTuDong(boolean kichHoatKhuyenMaiTuDong) { this.kichHoatKhuyenMaiTuDong = kichHoatKhuyenMaiTuDong; }

    public boolean isHienThiMatHangKhuyenMaiKhiThanhToan() { return hienThiMatHangKhuyenMaiKhiThanhToan; }
    public void setHienThiMatHangKhuyenMaiKhiThanhToan(boolean hienThiMatHangKhuyenMaiKhiThanhToan) { this.hienThiMatHangKhuyenMaiKhiThanhToan = hienThiMatHangKhuyenMaiKhiThanhToan; }

    public boolean isCoGiaoHang() { return coGiaoHang; }
    public void setCoGiaoHang(boolean coGiaoHang) { this.coGiaoHang = coGiaoHang; }

    public boolean isSuDungGiaTheoKhach() { return suDungGiaTheoKhach; }
    public void setSuDungGiaTheoKhach(boolean suDungGiaTheoKhach) { this.suDungGiaTheoKhach = suDungGiaTheoKhach; }

    public boolean isSuDungGiamGiaMacDinhTheoMatHang() { return suDungGiamGiaMacDinhTheoMatHang; }
    public void setSuDungGiamGiaMacDinhTheoMatHang(boolean suDungGiamGiaMacDinhTheoMatHang) { this.suDungGiamGiaMacDinhTheoMatHang = suDungGiamGiaMacDinhTheoMatHang; }

    public String getLamTronTien() { return lamTronTien; }
    public void setLamTronTien(String lamTronTien) { this.lamTronTien = lamTronTien; }

    // Getters & Setters Thanh toán
    public boolean isCoThanhToanVoucher() { return coThanhToanVoucher; }
    public void setCoThanhToanVoucher(boolean coThanhToanVoucher) { this.coThanhToanVoucher = coThanhToanVoucher; }

    public boolean isChoPhepKhachNo() { return choPhepKhachNo; }
    public void setChoPhepKhachNo(boolean choPhepKhachNo) { this.choPhepKhachNo = choPhepKhachNo; }

    public boolean isLuaChonVoucherTuDanhSach() { return luaChonVoucherTuDanhSach; }
    public void setLuaChonVoucherTuDanhSach(boolean luaChonVoucherTuDanhSach) { this.luaChonVoucherTuDanhSach = luaChonVoucherTuDanhSach; }

    public boolean isSuDungTheTraTruoc() { return suDungTheTraTruoc; }
    public void setSuDungTheTraTruoc(boolean suDungTheTraTruoc) { this.suDungTheTraTruoc = suDungTheTraTruoc; }

    public boolean isSuDungDiemTichLuyDeThanhToan() { return suDungDiemTichLuyDeThanhToan; }
    public void setSuDungDiemTichLuyDeThanhToan(boolean suDungDiemTichLuyDeThanhToan) { this.suDungDiemTichLuyDeThanhToan = suDungDiemTichLuyDeThanhToan; }

    public String getQuyDoi1DiemSangTien() { return quyDoi1DiemSangTien; }
    public void setQuyDoi1DiemSangTien(String quyDoi1DiemSangTien) { this.quyDoi1DiemSangTien = quyDoi1DiemSangTien; }

    public boolean isCoThanhToanThe() { return coThanhToanThe; }
    public void setCoThanhToanThe(boolean coThanhToanThe) { this.coThanhToanThe = coThanhToanThe; }

    public boolean isCoThanhToanChuyenKhoan() { return coThanhToanChuyenKhoan; }
    public void setCoThanhToanChuyenKhoan(boolean coThanhToanChuyenKhoan) { this.coThanhToanChuyenKhoan = coThanhToanChuyenKhoan; }

    // Getters & Setters Tích điểm
    public String getDoanhSoTuongUng1Diem() { return doanhSoTuongUng1Diem; }
    public void setDoanhSoTuongUng1Diem(String doanhSoTuongUng1Diem) { this.doanhSoTuongUng1Diem = doanhSoTuongUng1Diem; }

    public String getCachTinhDiem() { return cachTinhDiem; }
    public void setCachTinhDiem(String cachTinhDiem) { this.cachTinhDiem = cachTinhDiem; }

    public boolean isTuDongNangCapThanhVien() { return tuDongNangCapThanhVien; }
    public void setTuDongNangCapThanhVien(boolean tuDongNangCapThanhVien) { this.tuDongNangCapThanhVien = tuDongNangCapThanhVien; }

    public boolean isHienThiDiemKhachHangTrenHoaDon() { return hienThiDiemKhachHangTrenHoaDon; }
    public void setHienThiDiemKhachHangTrenHoaDon(boolean hienThiDiemKhachHangTrenHoaDon) { this.hienThiDiemKhachHangTrenHoaDon = hienThiDiemKhachHangTrenHoaDon; }

    public boolean isKhongTichDiemNeuCoKhuyenMai() { return khongTichDiemNeuCoKhuyenMai; }
    public void setKhongTichDiemNeuCoKhuyenMai(boolean khongTichDiemNeuCoKhuyenMai) { this.khongTichDiemNeuCoKhuyenMai = khongTichDiemNeuCoKhuyenMai; }

    // Getters & Setters Kho hàng
    public boolean isChoPhepNhapMotMatHangNhieuLan() { return choPhepNhapMotMatHangNhieuLan; }
    public void setChoPhepNhapMotMatHangNhieuLan(boolean choPhepNhapMotMatHangNhieuLan) { this.choPhepNhapMotMatHangNhieuLan = choPhepNhapMotMatHangNhieuLan; }

    public boolean isBatBuocChonNhanVienNhapKho() { return batBuocChonNhanVienNhapKho; }
    public void setBatBuocChonNhanVienNhapKho(boolean batBuocChonNhanVienNhapKho) { this.batBuocChonNhanVienNhapKho = batBuocChonNhanVienNhapKho; }

    public boolean isBatBuocChonNhaCungCapNhapKho() { return batBuocChonNhaCungCapNhapKho; }
    public void setBatBuocChonNhaCungCapNhapKho(boolean batBuocChonNhaCungCapNhapKho) { this.batBuocChonNhaCungCapNhapKho = batBuocChonNhaCungCapNhapKho; }

    public boolean isSuDung2DonViTinh() { return suDung2DonViTinh; }
    public void setSuDung2DonViTinh(boolean suDung2DonViTinh) { this.suDung2DonViTinh = suDung2DonViTinh; }

    public boolean isTuDongTinhGiaVon() { return tuDongTinhGiaVon; }
    public void setTuDongTinhGiaVon(boolean tuDongTinhGiaVon) { this.tuDongTinhGiaVon = tuDongTinhGiaVon; }

    public boolean isNhapKhoBangDauDocMaVach() { return nhapKhoBangDauDocMaVach; }
    public void setNhapKhoBangDauDocMaVach(boolean nhapKhoBangDauDocMaVach) { this.nhapKhoBangDauDocMaVach = nhapKhoBangDauDocMaVach; }

    public String getGiaSuDungKhiChuyenXuatKho() { return giaSuDungKhiChuyenXuatKho; }
    public void setGiaSuDungKhiChuyenXuatKho(String giaSuDungKhiChuyenXuatKho) { this.giaSuDungKhiChuyenXuatKho = giaSuDungKhiChuyenXuatKho; }

    public boolean isSuDungChucNangSanXuat() { return suDungChucNangSanXuat; }
    public void setSuDungChucNangSanXuat(boolean suDungChucNangSanXuat) { this.suDungChucNangSanXuat = suDungChucNangSanXuat; }

    // Getters & Setters Mặt hàng
    public boolean isSuDungMaSanCo() { return suDungMaSanCo; }
    public void setSuDungMaSanCo(boolean suDungMaSanCo) { this.suDungMaSanCo = suDungMaSanCo; }

    public boolean isSuDungMaHang() { return suDungMaHang; }
    public void setSuDungMaHang(boolean suDungMaHang) { this.suDungMaHang = suDungMaHang; }

    public boolean isTuDongSinhMaVach() { return tuDongSinhMaVach; }
    public void setTuDongSinhMaVach(boolean tuDongSinhMaVach) { this.tuDongSinhMaVach = tuDongSinhMaVach; }

    public int getChieuDaiMaVach() { return chieuDaiMaVach; }
    public void setChieuDaiMaVach(int chieuDaiMaVach) { this.chieuDaiMaVach = chieuDaiMaVach; }

    public boolean isCoKyTuKiemTra() { return coKyTuKiemTra; }
    public void setCoKyTuKiemTra(boolean coKyTuKiemTra) { this.coKyTuKiemTra = coKyTuKiemTra; }

    public boolean isSuDungTemInBangCanDienTu() { return suDungTemInBangCanDienTu; }
    public void setSuDungTemInBangCanDienTu(boolean suDungTemInBangCanDienTu) { this.suDungTemInBangCanDienTu = suDungTemInBangCanDienTu; }

    public String getChuoiBatDauCanDienTu() { return chuoiBatDauCanDienTu; }
    public void setChuoiBatDauCanDienTu(String chuoiBatDauCanDienTu) { this.chuoiBatDauCanDienTu = chuoiBatDauCanDienTu; }

    public String getTimTheo() { return timTheo; }
    public void setTimTheo(String timTheo) { this.timTheo = timTheo; }

    public String getCachTim() { return cachTim; }
    public void setCachTim(String cachTim) { this.cachTim = cachTim; }

    public boolean isChoPhepTrungTenMatHang() { return choPhepTrungTenMatHang; }
    public void setChoPhepTrungTenMatHang(boolean choPhepTrungTenMatHang) { this.choPhepTrungTenMatHang = choPhepTrungTenMatHang; }

    public boolean isHangCoBaoHanh() { return hangCoBaoHanh; }
    public void setHangCoBaoHanh(boolean hangCoBaoHanh) { this.hangCoBaoHanh = hangCoBaoHanh; }

    public boolean isSuDungHangSanXuat() { return suDungHangSanXuat; }
    public void setSuDungHangSanXuat(boolean suDungHangSanXuat) { this.suDungHangSanXuat = suDungHangSanXuat; }

    public boolean isSuDungGia2() { return suDungGia2; }
    public void setSuDungGia2(boolean suDungGia2) { this.suDungGia2 = suDungGia2; }

    public String getDienGiaiGia2() { return dienGiaiGia2; }
    public void setDienGiaiGia2(String dienGiaiGia2) { this.dienGiaiGia2 = dienGiaiGia2; }

    public boolean isSuDungGia3() { return suDungGia3; }
    public void setSuDungGia3(boolean suDungGia3) { this.suDungGia3 = suDungGia3; }

    public String getDienGiaiGia3() { return dienGiaiGia3; }
    public void setDienGiaiGia3(String dienGiaiGia3) { this.dienGiaiGia3 = dienGiaiGia3; }

    public boolean isSuDungGia4() { return suDungGia4; }
    public void setSuDungGia4(boolean suDungGia4) { this.suDungGia4 = suDungGia4; }

    public String getDienGiaiGia4() { return dienGiaiGia4; }
    public void setDienGiaiGia4(String dienGiaiGia4) { this.dienGiaiGia4 = dienGiaiGia4; }

    public boolean isHienThiDeLuaChonNeuCoNhieuGia() { return hienThiDeLuaChonNeuCoNhieuGia; }
    public void setHienThiDeLuaChonNeuCoNhieuGia(boolean hienThiDeLuaChonNeuCoNhieuGia) { this.hienThiDeLuaChonNeuCoNhieuGia = hienThiDeLuaChonNeuCoNhieuGia; }

    public String getSapXepThuTuTheo() { return sapXepThuTuTheo; }
    public void setSapXepThuTuTheo(String sapXepThuTuTheo) { this.sapXepThuTuTheo = sapXepThuTuTheo; }

    public boolean isMatHangCoHanSuDung() { return matHangCoHanSuDung; }
    public void setMatHangCoHanSuDung(boolean matHangCoHanSuDung) { this.matHangCoHanSuDung = matHangCoHanSuDung; }

    public boolean isMatHangCoKichThuoc() { return matHangCoKichThuoc; }
    public void setMatHangCoKichThuoc(boolean matHangCoKichThuoc) { this.matHangCoKichThuoc = matHangCoKichThuoc; }

    public boolean isMatHangCoVatTuDinhLuong() { return matHangCoVatTuDinhLuong; }
    public void setMatHangCoVatTuDinhLuong(boolean matHangCoVatTuDinhLuong) { this.matHangCoVatTuDinhLuong = matHangCoVatTuDinhLuong; }

    public boolean isGiaBanLeUSD() { return giaBanLeUSD; }
    public void setGiaBanLeUSD(boolean giaBanLeUSD) { this.giaBanLeUSD = giaBanLeUSD; }

    // Getters & Setters Cảnh báo
    public boolean isHienThiCanhBao() { return hienThiCanhBao; }
    public void setHienThiCanhBao(boolean hienThiCanhBao) { this.hienThiCanhBao = hienThiCanhBao; }

    public boolean isThongBaoHangDuoiMucAnToan() { return thongBaoHangDuoiMucAnToan; }
    public void setThongBaoHangDuoiMucAnToan(boolean thongBaoHangDuoiMucAnToan) { this.thongBaoHangDuoiMucAnToan = thongBaoHangDuoiMucAnToan; }

    public boolean isThongBaoKhachDenNgaySinhNhat() { return thongBaoKhachDenNgaySinhNhat; }
    public void setThongBaoKhachDenNgaySinhNhat(boolean thongBaoKhachDenNgaySinhNhat) { this.thongBaoKhachDenNgaySinhNhat = thongBaoKhachDenNgaySinhNhat; }

    public boolean isThongBaoHangHetHanDung() { return thongBaoHangHetHanDung; }
    public void setThongBaoHangHetHanDung(boolean thongBaoHangHetHanDung) { this.thongBaoHangHetHanDung = thongBaoHangHetHanDung; }

    public boolean isThongBaoHangSapHetHanDung() { return thongBaoHangSapHetHanDung; }
    public void setThongBaoHangSapHetHanDung(boolean thongBaoHangSapHetHanDung) { this.thongBaoHangSapHetHanDung = thongBaoHangSapHetHanDung; }

    public int getThongBaoTruocNgay() { return thongBaoTruocNgay; }
    public void setThongBaoTruocNgay(int thongBaoTruocNgay) { this.thongBaoTruocNgay = thongBaoTruocNgay; }

    // Getters & Setters Cảm ứng
    public boolean isInPhaCheKhiInHoaDon() { return inPhaCheKhiInHoaDon; }
    public void setInPhaCheKhiInHoaDon(boolean inPhaCheKhiInHoaDon) { this.inPhaCheKhiInHoaDon = inPhaCheKhiInHoaDon; }

    public String getMauInPhaChe() { return mauInPhaChe; }
    public void setMauInPhaChe(String mauInPhaChe) { this.mauInPhaChe = mauInPhaChe; }

    public String getMayInPhaChe() { return mayInPhaChe; }
    public void setMayInPhaChe(String mayInPhaChe) { this.mayInPhaChe = mayInPhaChe; }

    public boolean isInPhaCheKhiLuuTam() { return inPhaCheKhiLuuTam; }
    public void setInPhaCheKhiLuuTam(boolean inPhaCheKhiLuuTam) { this.inPhaCheKhiLuuTam = inPhaCheKhiLuuTam; }

    public String getMauInPhaCheLuuTam() { return mauInPhaCheLuuTam; }
    public void setMauInPhaCheLuuTam(String mauInPhaCheLuuTam) { this.mauInPhaCheLuuTam = mauInPhaCheLuuTam; }

    // Getters & Setters Quản trị
    public boolean isPhieuGanNhatNamPhiaDuoi() { return phieuGanNhatNamPhiaDuoi; }
    public void setPhieuGanNhatNamPhiaDuoi(boolean phieuGanNhatNamPhiaDuoi) { this.phieuGanNhatNamPhiaDuoi = phieuGanNhatNamPhiaDuoi; }

    public String getPhimNhapLieuTrenLuoi() { return phimNhapLieuTrenLuoi; }
    public void setPhimNhapLieuTrenLuoi(String phimNhapLieuTrenLuoi) { this.phimNhapLieuTrenLuoi = phimNhapLieuTrenLuoi; }

    public boolean isTuDongSaoLuuDuLieu() { return tuDongSaoLuuDuLieu; }
    public void setTuDongSaoLuuDuLieu(boolean tuDongSaoLuuDuLieu) { this.tuDongSaoLuuDuLieu = tuDongSaoLuuDuLieu; }

    public String getDuongDanSaoLuu() { return duongDanSaoLuu; }
    public void setDuongDanSaoLuu(String duongDanSaoLuu) { this.duongDanSaoLuu = duongDanSaoLuu; }

    public int getSoNgaySaoLuu() { return soNgaySaoLuu; }
    public void setSoNgaySaoLuu(int soNgaySaoLuu) { this.soNgaySaoLuu = soNgaySaoLuu; }

    public String getChucNangMacDinh() { return chucNangMacDinh; }
    public void setChucNangMacDinh(String chucNangMacDinh) { this.chucNangMacDinh = chucNangMacDinh; }

    public boolean isLocDuLieuBoKhoangTrong() { return locDuLieuBoKhoangTrong; }
    public void setLocDuLieuBoKhoangTrong(boolean locDuLieuBoKhoangTrong) { this.locDuLieuBoKhoangTrong = locDuLieuBoKhoangTrong; }

    public boolean isHienThiXuongDongNeuDoRongCotNho() { return hienThiXuongDongNeuDoRongCotNho; }
    public void setHienThiXuongDongNeuDoRongCotNho(boolean hienThiXuongDongNeuDoRongCotNho) { this.hienThiXuongDongNeuDoRongCotNho = hienThiXuongDongNeuDoRongCotNho; }

    public String getDoToChuHienThi() { return doToChuHienThi; }
    public void setDoToChuHienThi(String doToChuHienThi) { this.doToChuHienThi = doToChuHienThi; }

    public String getGiaoDienChuongTrinh() { return giaoDienChuongTrinh; }
    public void setGiaoDienChuongTrinh(String giaoDienChuongTrinh) { this.giaoDienChuongTrinh = giaoDienChuongTrinh; }

    public boolean isTuDongTaiLaiDuLieuKhiChuyenTab() { return tuDongTaiLaiDuLieuKhiChuyenTab; }
    public void setTuDongTaiLaiDuLieuKhiChuyenTab(boolean tuDongTaiLaiDuLieuKhiChuyenTab) { this.tuDongTaiLaiDuLieuKhiChuyenTab = tuDongTaiLaiDuLieuKhiChuyenTab; }

    public boolean isKichHoatLuuVetHoatDong() { return kichHoatLuuVetHoatDong; }
    public void setKichHoatLuuVetHoatDong(boolean kichHoatLuuVetHoatDong) { this.kichHoatLuuVetHoatDong = kichHoatLuuVetHoatDong; }

    public boolean isHeThongChayNhieuMayTram() { return heThongChayNhieuMayTram; }
    public void setHeThongChayNhieuMayTram(boolean heThongChayNhieuMayTram) { this.heThongChayNhieuMayTram = heThongChayNhieuMayTram; }
}
