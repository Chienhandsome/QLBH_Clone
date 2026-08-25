package kazuko.ex.dao;

import kazuko.ex.model.system.SystemConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SystemConfigDAO {
    private static final String CONFIG_FILE = "system_config.properties";
    private static SystemConfig cachedConfig = null;

    public synchronized SystemConfig loadConfig() {
        if (cachedConfig != null) {
            return cachedConfig;
        }

        SystemConfig config = new SystemConfig();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            Properties props = new Properties();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                props.load(reader);

                config.setTenCongTy(props.getProperty("company.name", config.getTenCongTy()));
                config.setDiaChi(props.getProperty("company.address", config.getDiaChi()));
                config.setLogoPath(props.getProperty("company.logo", config.getLogoPath()));
                config.setSoDienThoai(props.getProperty("company.phone", config.getSoDienThoai()));
                config.setEmail(props.getProperty("company.email", config.getEmail()));
                config.setLoiCamOn(props.getProperty("company.thanks", config.getLoiCamOn()));
                config.setFax(props.getProperty("company.fax", config.getFax()));
                config.setMauHoaDon(props.getProperty("print.invoiceTemplate", config.getMauHoaDon()));
                config.setLuaChonMauKhiIn(Boolean.parseBoolean(props.getProperty("print.selectTemplate", String.valueOf(config.isLuaChonMauKhiIn()))));
                config.setHienThiTruocKhiIn(Boolean.parseBoolean(props.getProperty("print.previewBeforePrint", String.valueOf(config.isHienThiTruocKhiIn()))));

                // Load Printers
                for (String key : config.getPrinterSettings().keySet()) {
                    String val = props.getProperty("printer." + key);
                    if (val != null) {
                        config.getPrinterSettings().put(key, val);
                    }
                }

                // Load Voucher Rules
                for (String key : config.getVoucherRules().keySet()) {
                    String val = props.getProperty("voucher." + key);
                    if (val != null) {
                        config.getVoucherRules().put(key, val);
                    }
                }

                // Load Bán hàng
                config.setChoPhepTrungTenKhachHang(Boolean.parseBoolean(props.getProperty("sale.allowDupCustomerName", String.valueOf(config.isChoPhepTrungTenKhachHang()))));
                config.setChoPhepNhapGiamGia(Boolean.parseBoolean(props.getProperty("sale.allowDiscountInput", String.valueOf(config.isChoPhepNhapGiamGia()))));
                config.setMacDinhGiamGiaPt(Integer.parseInt(props.getProperty("sale.defaultDiscountPt", String.valueOf(config.getMacDinhGiamGiaPt()))));
                config.setBanHangDungDauDocMaVach(Boolean.parseBoolean(props.getProperty("sale.useBarcodeScanner", String.valueOf(config.isBanHangDungDauDocMaVach()))));
                config.setSoLanIn(Integer.parseInt(props.getProperty("sale.printCount", String.valueOf(config.getSoLanIn()))));
                config.setHienThiCuaSoNhapSoLuongKhiQuet(Boolean.parseBoolean(props.getProperty("sale.showQtyDialogOnScan", String.valueOf(config.isHienThiCuaSoNhapSoLuongKhiQuet()))));
                config.setChoPhepThayDoiNgayTrenHoaDon(Boolean.parseBoolean(props.getProperty("sale.allowChangeDateOnInvoice", String.valueOf(config.isChoPhepThayDoiNgayTrenHoaDon()))));
                config.setCoPhiVanChuyen(Boolean.parseBoolean(props.getProperty("sale.hasShippingFee", String.valueOf(config.isCoPhiVanChuyen()))));
                config.setCoThueSuat(Boolean.parseBoolean(props.getProperty("sale.hasTaxRate", String.valueOf(config.isCoThueSuat()))));
                config.setMacDinhThueSuat(Integer.parseInt(props.getProperty("sale.defaultTaxRate", String.valueOf(config.getMacDinhThueSuat()))));
                config.setBatBuocNhapKhachHang(Boolean.parseBoolean(props.getProperty("sale.requireCustomer", String.valueOf(config.isBatBuocNhapKhachHang()))));
                config.setBatBuocNhapNhanVienBanHang(Boolean.parseBoolean(props.getProperty("sale.requireSalesStaff", String.valueOf(config.isBatBuocNhapNhanVienBanHang()))));
                config.setChoPhepInTamTinh(Boolean.parseBoolean(props.getProperty("sale.allowProvisionalPrint", String.valueOf(config.isChoPhepInTamTinh()))));
                config.setBatBuocDoiTraHangTheoDon(Boolean.parseBoolean(props.getProperty("sale.requireReturnByInvoice", String.valueOf(config.isBatBuocDoiTraHangTheoDon()))));
                config.setSoNgayChoPhepDoiTra(Integer.parseInt(props.getProperty("sale.returnAllowedDays", String.valueOf(config.getSoNgayChoPhepDoiTra()))));
                config.setHienThiAnhSanPhamTrongGiaoDien(Boolean.parseBoolean(props.getProperty("sale.showProductImageInSalesUI", String.valueOf(config.isHienThiAnhSanPhamTrongGiaoDien()))));
                config.setKichHoatKhuyenMaiTuDong(Boolean.parseBoolean(props.getProperty("sale.autoPromotion", String.valueOf(config.isKichHoatKhuyenMaiTuDong()))));
                config.setHienThiMatHangKhuyenMaiKhiThanhToan(Boolean.parseBoolean(props.getProperty("sale.showPromoItemOnCheckout", String.valueOf(config.isHienThiMatHangKhuyenMaiKhiThanhToan()))));
                config.setCoGiaoHang(Boolean.parseBoolean(props.getProperty("sale.hasDelivery", String.valueOf(config.isCoGiaoHang()))));
                config.setSuDungGiaTheoKhach(Boolean.parseBoolean(props.getProperty("sale.usePriceByCustomer", String.valueOf(config.isSuDungGiaTheoKhach()))));
                config.setSuDungGiamGiaMacDinhTheoMatHang(Boolean.parseBoolean(props.getProperty("sale.useDefaultDiscountByItem", String.valueOf(config.isSuDungGiamGiaMacDinhTheoMatHang()))));
                config.setLamTronTien(props.getProperty("sale.roundMoney", config.getLamTronTien()));

                // Load Thanh toán
                config.setCoThanhToanVoucher(Boolean.parseBoolean(props.getProperty("pay.hasVoucherPay", String.valueOf(config.isCoThanhToanVoucher()))));
                config.setChoPhepKhachNo(Boolean.parseBoolean(props.getProperty("pay.allowDebt", String.valueOf(config.isChoPhepKhachNo()))));
                config.setLuaChonVoucherTuDanhSach(Boolean.parseBoolean(props.getProperty("pay.selectVoucherFromList", String.valueOf(config.isLuaChonVoucherTuDanhSach()))));
                config.setSuDungTheTraTruoc(Boolean.parseBoolean(props.getProperty("pay.usePrepaidCard", String.valueOf(config.isSuDungTheTraTruoc()))));
                config.setSuDungDiemTichLuyDeThanhToan(Boolean.parseBoolean(props.getProperty("pay.usePointPay", String.valueOf(config.isSuDungDiemTichLuyDeThanhToan()))));
                config.setQuyDoi1DiemSangTien(props.getProperty("pay.pointToMoney", config.getQuyDoi1DiemSangTien()));
                config.setCoThanhToanThe(Boolean.parseBoolean(props.getProperty("pay.hasCardPay", String.valueOf(config.isCoThanhToanThe()))));
                config.setCoThanhToanChuyenKhoan(Boolean.parseBoolean(props.getProperty("pay.hasTransferPay", String.valueOf(config.isCoThanhToanChuyenKhoan()))));

                // Load Tích điểm
                config.setDoanhSoTuongUng1Diem(props.getProperty("point.salesPerPoint", config.getDoanhSoTuongUng1Diem()));
                config.setCachTinhDiem(props.getProperty("point.calcMethod", config.getCachTinhDiem()));
                config.setTuDongNangCapThanhVien(Boolean.parseBoolean(props.getProperty("point.autoUpgradeMember", String.valueOf(config.isTuDongNangCapThanhVien()))));
                config.setHienThiDiemKhachHangTrenHoaDon(Boolean.parseBoolean(props.getProperty("point.showPointOnInvoice", String.valueOf(config.isHienThiDiemKhachHangTrenHoaDon()))));
                config.setKhongTichDiemNeuCoKhuyenMai(Boolean.parseBoolean(props.getProperty("point.noPointIfPromo", String.valueOf(config.isKhongTichDiemNeuCoKhuyenMai()))));

                // Load Kho hàng
                config.setChoPhepNhapMotMatHangNhieuLan(Boolean.parseBoolean(props.getProperty("stock.allowDupItemInVoucher", String.valueOf(config.isChoPhepNhapMotMatHangNhieuLan()))));
                config.setBatBuocChonNhanVienNhapKho(Boolean.parseBoolean(props.getProperty("stock.requireStaffInImport", String.valueOf(config.isBatBuocChonNhanVienNhapKho()))));
                config.setBatBuocChonNhaCungCapNhapKho(Boolean.parseBoolean(props.getProperty("stock.requireSupplierInImport", String.valueOf(config.isBatBuocChonNhaCungCapNhapKho()))));
                config.setSuDung2DonViTinh(Boolean.parseBoolean(props.getProperty("stock.use2Units", String.valueOf(config.isSuDung2DonViTinh()))));
                config.setTuDongTinhGiaVon(Boolean.parseBoolean(props.getProperty("stock.autoCalcCostPrice", String.valueOf(config.isTuDongTinhGiaVon()))));
                config.setNhapKhoBangDauDocMaVach(Boolean.parseBoolean(props.getProperty("stock.useBarcodeScanner", String.valueOf(config.isNhapKhoBangDauDocMaVach()))));
                config.setGiaSuDungKhiChuyenXuatKho(props.getProperty("stock.transferExportPriceType", config.getGiaSuDungKhiChuyenXuatKho()));
                config.setSuDungChucNangSanXuat(Boolean.parseBoolean(props.getProperty("stock.useManufactureFeature", String.valueOf(config.isSuDungChucNangSanXuat()))));

            } catch (Exception ignored) {}
        }

        cachedConfig = config;
        return cachedConfig;
    }

    public synchronized boolean saveConfig(SystemConfig config) {
        if (config == null) return false;
        cachedConfig = config;

        Properties props = new Properties();
        props.setProperty("company.name", config.getTenCongTy() != null ? config.getTenCongTy() : "");
        props.setProperty("company.address", config.getDiaChi() != null ? config.getDiaChi() : "");
        props.setProperty("company.logo", config.getLogoPath() != null ? config.getLogoPath() : "");
        props.setProperty("company.phone", config.getSoDienThoai() != null ? config.getSoDienThoai() : "");
        props.setProperty("company.email", config.getEmail() != null ? config.getEmail() : "");
        props.setProperty("company.thanks", config.getLoiCamOn() != null ? config.getLoiCamOn() : "");
        props.setProperty("company.fax", config.getFax() != null ? config.getFax() : "");
        props.setProperty("print.invoiceTemplate", config.getMauHoaDon() != null ? config.getMauHoaDon() : "");
        props.setProperty("print.selectTemplate", String.valueOf(config.isLuaChonMauKhiIn()));
        props.setProperty("print.previewBeforePrint", String.valueOf(config.isHienThiTruocKhiIn()));

        // Save Printers
        for (var entry : config.getPrinterSettings().entrySet()) {
            props.setProperty("printer." + entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }

        // Save Voucher Rules
        for (var entry : config.getVoucherRules().entrySet()) {
            props.setProperty("voucher." + entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }

        // Save Bán hàng
        props.setProperty("sale.allowDupCustomerName", String.valueOf(config.isChoPhepTrungTenKhachHang()));
        props.setProperty("sale.allowDiscountInput", String.valueOf(config.isChoPhepNhapGiamGia()));
        props.setProperty("sale.defaultDiscountPt", String.valueOf(config.getMacDinhGiamGiaPt()));
        props.setProperty("sale.useBarcodeScanner", String.valueOf(config.isBanHangDungDauDocMaVach()));
        props.setProperty("sale.printCount", String.valueOf(config.getSoLanIn()));
        props.setProperty("sale.showQtyDialogOnScan", String.valueOf(config.isHienThiCuaSoNhapSoLuongKhiQuet()));
        props.setProperty("sale.allowChangeDateOnInvoice", String.valueOf(config.isChoPhepThayDoiNgayTrenHoaDon()));
        props.setProperty("sale.hasShippingFee", String.valueOf(config.isCoPhiVanChuyen()));
        props.setProperty("sale.hasTaxRate", String.valueOf(config.isCoThueSuat()));
        props.setProperty("sale.defaultTaxRate", String.valueOf(config.getMacDinhThueSuat()));
        props.setProperty("sale.requireCustomer", String.valueOf(config.isBatBuocNhapKhachHang()));
        props.setProperty("sale.requireSalesStaff", String.valueOf(config.isBatBuocNhapNhanVienBanHang()));
        props.setProperty("sale.allowProvisionalPrint", String.valueOf(config.isChoPhepInTamTinh()));
        props.setProperty("sale.requireReturnByInvoice", String.valueOf(config.isBatBuocDoiTraHangTheoDon()));
        props.setProperty("sale.returnAllowedDays", String.valueOf(config.getSoNgayChoPhepDoiTra()));
        props.setProperty("sale.showProductImageInSalesUI", String.valueOf(config.isHienThiAnhSanPhamTrongGiaoDien()));
        props.setProperty("sale.autoPromotion", String.valueOf(config.isKichHoatKhuyenMaiTuDong()));
        props.setProperty("sale.showPromoItemOnCheckout", String.valueOf(config.isHienThiMatHangKhuyenMaiKhiThanhToan()));
        props.setProperty("sale.hasDelivery", String.valueOf(config.isCoGiaoHang()));
        props.setProperty("sale.usePriceByCustomer", String.valueOf(config.isSuDungGiaTheoKhach()));
        props.setProperty("sale.useDefaultDiscountByItem", String.valueOf(config.isSuDungGiamGiaMacDinhTheoMatHang()));
        props.setProperty("sale.roundMoney", config.getLamTronTien() != null ? config.getLamTronTien() : "1,000");

        // Save Thanh toán
        props.setProperty("pay.hasVoucherPay", String.valueOf(config.isCoThanhToanVoucher()));
        props.setProperty("pay.allowDebt", String.valueOf(config.isChoPhepKhachNo()));
        props.setProperty("pay.selectVoucherFromList", String.valueOf(config.isLuaChonVoucherTuDanhSach()));
        props.setProperty("pay.usePrepaidCard", String.valueOf(config.isSuDungTheTraTruoc()));
        props.setProperty("pay.usePointPay", String.valueOf(config.isSuDungDiemTichLuyDeThanhToan()));
        props.setProperty("pay.pointToMoney", config.getQuyDoi1DiemSangTien() != null ? config.getQuyDoi1DiemSangTien() : "1,000");
        props.setProperty("pay.hasCardPay", String.valueOf(config.isCoThanhToanThe()));
        props.setProperty("pay.hasTransferPay", String.valueOf(config.isCoThanhToanChuyenKhoan()));

        // Save Tích điểm
        props.setProperty("point.salesPerPoint", config.getDoanhSoTuongUng1Diem() != null ? config.getDoanhSoTuongUng1Diem() : "20,000");
        props.setProperty("point.calcMethod", config.getCachTinhDiem() != null ? config.getCachTinhDiem() : "Điểm được tính trên từng hóa đơn");
        props.setProperty("point.autoUpgradeMember", String.valueOf(config.isTuDongNangCapThanhVien()));
        props.setProperty("point.showPointOnInvoice", String.valueOf(config.isHienThiDiemKhachHangTrenHoaDon()));
        props.setProperty("point.noPointIfPromo", String.valueOf(config.isKhongTichDiemNeuCoKhuyenMai()));

        // Save Kho hàng
        props.setProperty("stock.allowDupItemInVoucher", String.valueOf(config.isChoPhepNhapMotMatHangNhieuLan()));
        props.setProperty("stock.requireStaffInImport", String.valueOf(config.isBatBuocChonNhanVienNhapKho()));
        props.setProperty("stock.requireSupplierInImport", String.valueOf(config.isBatBuocChonNhaCungCapNhapKho()));
        props.setProperty("stock.use2Units", String.valueOf(config.isSuDung2DonViTinh()));
        props.setProperty("stock.autoCalcCostPrice", String.valueOf(config.isTuDongTinhGiaVon()));
        props.setProperty("stock.useBarcodeScanner", String.valueOf(config.isNhapKhoBangDauDocMaVach()));
        props.setProperty("stock.transferExportPriceType", config.getGiaSuDungKhiChuyenXuatKho() != null ? config.getGiaSuDungKhiChuyenXuatKho() : "Giá vốn");
        props.setProperty("stock.useManufactureFeature", String.valueOf(config.isSuDungChucNangSanXuat()));

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            props.store(writer, "Cấu hình hệ thống QLBH");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
