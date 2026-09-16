package kazuko.ex.dao;

import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.service.ActivityLogService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SystemConfigDAO {
    private static final String CONFIG_FILE_NAME = "system_config.properties";
    private static SystemConfig cachedConfig = null;

    public static void clearCache() {
        cachedConfig = null;
    }

    public static File getConfigFile() {
        // 1. Current working directory
        File f1 = new File(CONFIG_FILE_NAME);
        if (f1.exists()) {
            return f1;
        }
        // 2. QLBH_Clone subfolder
        File f2 = new File("QLBH_Clone", CONFIG_FILE_NAME);
        if (f2.exists()) {
            return f2;
        }
        // 3. Parent directory
        File f3 = new File("..", CONFIG_FILE_NAME);
        if (f3.exists()) {
            return f3;
        }
        // 4. Relative to application directory parent (e.g. when run from target/ or outside project)
        try {
            java.nio.file.Path appDir = kazuko.ex.config.EmbeddedFirebird.getApplicationDirectory();
            if (appDir != null && appDir.getParent() != null) {
                File appParentCfg = appDir.getParent().resolve(CONFIG_FILE_NAME).toFile();
                if (appParentCfg.exists()) {
                    return appParentCfg;
                }
            }
        } catch (Exception ignored) {
        }
        // Fallback: If QLBH_Clone directory exists, use QLBH_Clone/system_config.properties
        if (new File("QLBH_Clone").isDirectory()) {
            return f2;
        }
        return f1;
    }

    public static String resolveLogoPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty() || "null".equalsIgnoreCase(rawPath.trim())) {
            return null;
        }
        String clean = rawPath.trim();
        // 1. Direct file check
        File f1 = new File(clean);
        if (f1.exists() && f1.isFile()) {
            return f1.getAbsolutePath();
        }
        // 2. Relative to config file directory
        File cfgDir = getConfigFile().getParentFile();
        if (cfgDir != null) {
            File f2 = new File(cfgDir, clean);
            if (f2.exists() && f2.isFile()) {
                return f2.getAbsolutePath();
            }
        }
        // 3. Relative to QLBH_Clone
        File f3 = new File("QLBH_Clone", clean);
        if (f3.exists() && f3.isFile()) {
            return f3.getAbsolutePath();
        }
        // 4. Relative to current dir
        File f4 = new File(clean);
        if (f4.exists() && f4.isFile()) {
            return f4.getAbsolutePath();
        }
        return clean;
    }

    public SystemConfig getConfig() {
        return loadConfig();
    }

    public synchronized SystemConfig loadConfig() {
        if (cachedConfig != null) {
            return cachedConfig;
        }

        SystemConfig config = new SystemConfig();
        File file = getConfigFile();
        if (file.exists()) {
            Properties props = new Properties();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                props.load(reader);

                config.setTenCongTy(props.getProperty("company.name", config.getTenCongTy()));
                config.setDiaChi(props.getProperty("company.address", config.getDiaChi()));

                String logoVal = props.getProperty("company.logo");
                if (logoVal != null && !logoVal.trim().isEmpty() && !"null".equalsIgnoreCase(logoVal.trim())) {
                    config.setLogoPath(resolveLogoPath(logoVal.trim()));
                } else {
                    config.setLogoPath(null);
                }
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

                // Load Mặt hàng
                config.setSuDungMaSanCo(Boolean.parseBoolean(props.getProperty("item.useExistingCode", String.valueOf(config.isSuDungMaSanCo()))));
                config.setSuDungMaHang(Boolean.parseBoolean(props.getProperty("item.useItemCode", String.valueOf(config.isSuDungMaHang()))));
                config.setTuDongSinhMaVach(Boolean.parseBoolean(props.getProperty("item.autoGenerateBarcode", String.valueOf(config.isTuDongSinhMaVach()))));
                config.setChieuDaiMaVach(Integer.parseInt(props.getProperty("item.barcodeLength", String.valueOf(config.getChieuDaiMaVach()))));
                config.setCoKyTuKiemTra(Boolean.parseBoolean(props.getProperty("item.hasChecksumChar", String.valueOf(config.isCoKyTuKiemTra()))));
                config.setSuDungTemInBangCanDienTu(Boolean.parseBoolean(props.getProperty("item.useScaleLabel", String.valueOf(config.isSuDungTemInBangCanDienTu()))));
                config.setChuoiBatDauCanDienTu(props.getProperty("item.scalePrefix", config.getChuoiBatDauCanDienTu()));
                config.setTimTheo(props.getProperty("item.searchBy", config.getTimTheo()));
                config.setCachTim(props.getProperty("item.searchMethod", config.getCachTim()));
                config.setChoPhepTrungTenMatHang(Boolean.parseBoolean(props.getProperty("item.allowDupItemName", String.valueOf(config.isChoPhepTrungTenMatHang()))));
                config.setHangCoBaoHanh(Boolean.parseBoolean(props.getProperty("item.hasWarranty", String.valueOf(config.isHangCoBaoHanh()))));
                config.setSuDungHangSanXuat(Boolean.parseBoolean(props.getProperty("item.useManufacturer", String.valueOf(config.isSuDungHangSanXuat()))));
                config.setSuDungGia2(Boolean.parseBoolean(props.getProperty("item.usePrice2", String.valueOf(config.isSuDungGia2()))));
                config.setDienGiaiGia2(props.getProperty("item.price2Desc", config.getDienGiaiGia2()));
                config.setSuDungGia3(Boolean.parseBoolean(props.getProperty("item.usePrice3", String.valueOf(config.isSuDungGia3()))));
                config.setDienGiaiGia3(props.getProperty("item.price3Desc", config.getDienGiaiGia3()));
                config.setSuDungGia4(Boolean.parseBoolean(props.getProperty("item.usePrice4", String.valueOf(config.isSuDungGia4()))));
                config.setDienGiaiGia4(props.getProperty("item.price4Desc", config.getDienGiaiGia4()));
                config.setHienThiDeLuaChonNeuCoNhieuGia(Boolean.parseBoolean(props.getProperty("item.showMultiPriceChoice", String.valueOf(config.isHienThiDeLuaChonNeuCoNhieuGia()))));
                config.setSapXepThuTuTheo(props.getProperty("item.sortBy", config.getSapXepThuTuTheo()));
                config.setMatHangCoHanSuDung(Boolean.parseBoolean(props.getProperty("item.hasExpiryDate", String.valueOf(config.isMatHangCoHanSuDung()))));
                config.setMatHangCoKichThuoc(Boolean.parseBoolean(props.getProperty("item.hasDimensions", String.valueOf(config.isMatHangCoKichThuoc()))));
                config.setMatHangCoVatTuDinhLuong(Boolean.parseBoolean(props.getProperty("item.hasBOM", String.valueOf(config.isMatHangCoVatTuDinhLuong()))));

                // Load Cây hiển thị giá
                config.setCoCayHienThiGia(Boolean.parseBoolean(props.getProperty("customerDisplay.enabled", String.valueOf(config.isCoCayHienThiGia()))));
                config.setCayHienThiGiaMotDong(Boolean.parseBoolean(props.getProperty("customerDisplay.singleLine", String.valueOf(config.isCayHienThiGiaMotDong()))));
                config.setCongCayHienThiGia(props.getProperty("customerDisplay.port", config.getCongCayHienThiGia()));
                config.setCoManHinhPhu(Boolean.parseBoolean(props.getProperty("customerDisplay.secondaryScreen", String.valueOf(config.isCoManHinhPhu()))));
                String secondaryLogoPath = props.getProperty("customerDisplay.secondaryLogo", "").trim();
                config.setLogoManHinhPhuPath(secondaryLogoPath.isEmpty() ? null : resolveLogoPath(secondaryLogoPath));
                config.setManHinhPhuSuDungGiaoDienThuong(Boolean.parseBoolean(props.getProperty("customerDisplay.secondaryScreenUseStandardUi", String.valueOf(config.isManHinhPhuSuDungGiaoDienThuong()))));

                // Load Cảnh báo
                config.setHienThiCanhBao(Boolean.parseBoolean(props.getProperty("alert.showAlert", String.valueOf(config.isHienThiCanhBao()))));
                config.setThongBaoHangDuoiMucAnToan(Boolean.parseBoolean(props.getProperty("alert.notifyBelowSafeStock", String.valueOf(config.isThongBaoHangDuoiMucAnToan()))));
                config.setThongBaoKhachDenNgaySinhNhat(Boolean.parseBoolean(props.getProperty("alert.notifyCustomerBirthday", String.valueOf(config.isThongBaoKhachDenNgaySinhNhat()))));
                config.setThongBaoHangHetHanDung(Boolean.parseBoolean(props.getProperty("alert.notifyExpiredItem", String.valueOf(config.isThongBaoHangHetHanDung()))));
                config.setThongBaoHangSapHetHanDung(Boolean.parseBoolean(props.getProperty("alert.notifyNearExpiryItem", String.valueOf(config.isThongBaoHangSapHetHanDung()))));
                config.setThongBaoTruocNgay(Integer.parseInt(props.getProperty("alert.notifyBeforeDays", String.valueOf(config.getThongBaoTruocNgay()))));

                // Load Cảm ứng
                config.setInPhaCheKhiInHoaDon(Boolean.parseBoolean(props.getProperty("touch.printKitchenOnInvoice", String.valueOf(config.isInPhaCheKhiInHoaDon()))));
                config.setMauInPhaChe(props.getProperty("touch.kitchenTemplate", config.getMauInPhaChe()));
                config.setMayInPhaChe(props.getProperty("touch.kitchenPrinter", config.getMayInPhaChe()));
                config.setInPhaCheKhiLuuTam(Boolean.parseBoolean(props.getProperty("touch.printKitchenOnDraft", String.valueOf(config.isInPhaCheKhiLuuTam()))));
                config.setMauInPhaCheLuuTam(props.getProperty("touch.kitchenDraftTemplate", config.getMauInPhaCheLuuTam()));

                // Load Quản trị
                config.setPhieuGanNhatNamPhiaDuoi(Boolean.parseBoolean(props.getProperty("admin.recentVoucherAtBottom", String.valueOf(config.isPhieuGanNhatNamPhiaDuoi()))));
                config.setPhimNhapLieuTrenLuoi(props.getProperty("admin.gridInputKey", config.getPhimNhapLieuTrenLuoi()));
                config.setTuDongSaoLuuDuLieu(Boolean.parseBoolean(props.getProperty("admin.autoBackupData", String.valueOf(config.isTuDongSaoLuuDuLieu()))));
                config.setDuongDanSaoLuu(props.getProperty("admin.backupPath", config.getDuongDanSaoLuu()));
                config.setSoNgaySaoLuu(Integer.parseInt(props.getProperty("admin.backupDays", String.valueOf(config.getSoNgaySaoLuu()))));
                config.setChucNangMacDinh(props.getProperty("admin.defaultFeature", config.getChucNangMacDinh()));
                config.setLocDuLieuBoKhoangTrong(Boolean.parseBoolean(props.getProperty("admin.filterIgnoreWhitespace", String.valueOf(config.isLocDuLieuBoKhoangTrong()))));
                config.setHienThiXuongDongNeuDoRongCotNho(Boolean.parseBoolean(props.getProperty("admin.wrapTextIfColSmall", String.valueOf(config.isHienThiXuongDongNeuDoRongCotNho()))));
                config.setDoToChuHienThi(props.getProperty("admin.fontSizeScale", config.getDoToChuHienThi()));
                config.setGiaoDienChuongTrinh(props.getProperty("admin.uiTheme", config.getGiaoDienChuongTrinh()));
                config.setTuDongTaiLaiDuLieuKhiChuyenTab(Boolean.parseBoolean(props.getProperty("admin.autoReloadOnTabChange", String.valueOf(config.isTuDongTaiLaiDuLieuKhiChuyenTab()))));
                config.setKichHoatLuuVetHoatDong(Boolean.parseBoolean(props.getProperty("admin.enableAuditTrail", String.valueOf(config.isKichHoatLuuVetHoatDong()))));
                config.setHeThongChayNhieuMayTram(Boolean.parseBoolean(props.getProperty("admin.multiWorkstations", String.valueOf(config.isHeThongChayNhieuMayTram()))));

                // Load Cân điện tử
                config.setKichHoatSuDungCanDienTu(Boolean.parseBoolean(props.getProperty("scale.enabled", String.valueOf(config.isKichHoatSuDungCanDienTu()))));
                config.setCongComCanDienTu(props.getProperty("scale.comPort", config.getCongComCanDienTu()));
                config.setBaudRateCanDienTu(props.getProperty("scale.baudRate", config.getBaudRateCanDienTu()));
                config.setDatabitCanDienTu(props.getProperty("scale.databit", config.getDatabitCanDienTu()));
                config.setParityCanDienTu(props.getProperty("scale.parity", config.getParityCanDienTu()));
                config.setStopBitsCanDienTu(props.getProperty("scale.stopBits", config.getStopBitsCanDienTu()));
                try { config.setTimeoutCanDienTu(Integer.parseInt(props.getProperty("scale.timeout", String.valueOf(config.getTimeoutCanDienTu())))); } catch (Exception ignored) {}
                try { config.setViTriCanDienTu(Integer.parseInt(props.getProperty("scale.position", String.valueOf(config.getViTriCanDienTu())))); } catch (Exception ignored) {}
                try { config.setDoDaiCanDienTu(Integer.parseInt(props.getProperty("scale.length", String.valueOf(config.getDoDaiCanDienTu())))); } catch (Exception ignored) {}
                config.setTuDongNhanDangCanDienTu(Boolean.parseBoolean(props.getProperty("scale.autoDetect", String.valueOf(config.isTuDongNhanDangCanDienTu()))));
                config.setDonViTrenCan(props.getProperty("scale.unit", config.getDonViTrenCan()));

            } catch (Exception ignored) {}
        }

        cachedConfig = config;
        return cachedConfig;
    }

    public synchronized boolean saveConfig(SystemConfig config) {
        if (config == null) return false;

        // Persist logo file to data/ directory if a custom logo is specified
        if (config.getLogoPath() != null && !config.getLogoPath().trim().isEmpty()) {
            File srcLogo = new File(config.getLogoPath().trim());
            if (srcLogo.exists() && srcLogo.isFile()) {
                try {
                    File cfgFile = getConfigFile();
                    File baseDir = cfgFile.getParentFile();
                    File targetDir = baseDir != null ? new File(baseDir, "data") : new File("data");
                    if (!targetDir.exists()) {
                        targetDir.mkdirs();
                    }
                    String ext = "png";
                    int dotIdx = srcLogo.getName().lastIndexOf('.');
                    if (dotIdx > 0) {
                        ext = srcLogo.getName().substring(dotIdx + 1).toLowerCase();
                    }
                    File destLogo = new File(targetDir, "company_logo." + ext);
                    if (!destLogo.getCanonicalPath().equalsIgnoreCase(srcLogo.getCanonicalPath())) {
                        java.nio.file.Files.copy(srcLogo.toPath(), destLogo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        config.setLogoPath(destLogo.getAbsolutePath());
                    }
                } catch (Exception ignored) {}
            }
        }

        // Keep the secondary-display logo available even when the selected source is temporary.
        if (config.getLogoManHinhPhuPath() != null && !config.getLogoManHinhPhuPath().trim().isEmpty()) {
            File srcLogo = new File(config.getLogoManHinhPhuPath().trim());
            if (srcLogo.exists() && srcLogo.isFile()) {
                try {
                    File cfgFile = getConfigFile();
                    File baseDir = cfgFile.getParentFile();
                    File targetDir = baseDir != null ? new File(baseDir, "data") : new File("data");
                    if (!targetDir.exists()) {
                        targetDir.mkdirs();
                    }
                    String ext = "png";
                    int dotIdx = srcLogo.getName().lastIndexOf('.');
                    if (dotIdx > 0) {
                        ext = srcLogo.getName().substring(dotIdx + 1).toLowerCase();
                    }
                    File destLogo = new File(targetDir, "secondary_display_logo." + ext);
                    if (!destLogo.getCanonicalPath().equalsIgnoreCase(srcLogo.getCanonicalPath())) {
                        java.nio.file.Files.copy(srcLogo.toPath(), destLogo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        config.setLogoManHinhPhuPath(destLogo.getAbsolutePath());
                    }
                } catch (Exception ignored) {}
            }
        }

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

        // Save Mặt hàng
        props.setProperty("item.useExistingCode", String.valueOf(config.isSuDungMaSanCo()));
        props.setProperty("item.useItemCode", String.valueOf(config.isSuDungMaHang()));
        props.setProperty("item.autoGenerateBarcode", String.valueOf(config.isTuDongSinhMaVach()));
        props.setProperty("item.barcodeLength", String.valueOf(config.getChieuDaiMaVach()));
        props.setProperty("item.hasChecksumChar", String.valueOf(config.isCoKyTuKiemTra()));
        props.setProperty("item.useScaleLabel", String.valueOf(config.isSuDungTemInBangCanDienTu()));
        props.setProperty("item.scalePrefix", config.getChuoiBatDauCanDienTu() != null ? config.getChuoiBatDauCanDienTu() : "29");
        props.setProperty("item.searchBy", config.getTimTheo() != null ? config.getTimTheo() : "Cả tên hàng và mã hàng");
        props.setProperty("item.searchMethod", config.getCachTim() != null ? config.getCachTim() : "Tìm có chứa");
        props.setProperty("item.allowDupItemName", String.valueOf(config.isChoPhepTrungTenMatHang()));
        props.setProperty("item.hasWarranty", String.valueOf(config.isHangCoBaoHanh()));
        props.setProperty("item.useManufacturer", String.valueOf(config.isSuDungHangSanXuat()));
        props.setProperty("item.usePrice2", String.valueOf(config.isSuDungGia2()));
        props.setProperty("item.price2Desc", config.getDienGiaiGia2() != null ? config.getDienGiaiGia2() : "Đơn giá 2");
        props.setProperty("item.usePrice3", String.valueOf(config.isSuDungGia3()));
        props.setProperty("item.price3Desc", config.getDienGiaiGia3() != null ? config.getDienGiaiGia3() : "Giá bán 3");
        props.setProperty("item.usePrice4", String.valueOf(config.isSuDungGia4()));
        props.setProperty("item.price4Desc", config.getDienGiaiGia4() != null ? config.getDienGiaiGia4() : "Giá bán 4");
        props.setProperty("item.showMultiPriceChoice", String.valueOf(config.isHienThiDeLuaChonNeuCoNhieuGia()));
        props.setProperty("item.sortBy", config.getSapXepThuTuTheo() != null ? config.getSapXepThuTuTheo() : "Mã hàng");
        props.setProperty("item.hasExpiryDate", String.valueOf(config.isMatHangCoHanSuDung()));
        props.setProperty("item.hasDimensions", String.valueOf(config.isMatHangCoKichThuoc()));
        props.setProperty("item.hasBOM", String.valueOf(config.isMatHangCoVatTuDinhLuong()));

        // Save Cây hiển thị giá
        props.setProperty("customerDisplay.enabled", String.valueOf(config.isCoCayHienThiGia()));
        props.setProperty("customerDisplay.singleLine", String.valueOf(config.isCayHienThiGiaMotDong()));
        props.setProperty("customerDisplay.port", config.getCongCayHienThiGia() != null ? config.getCongCayHienThiGia() : "");
        props.setProperty("customerDisplay.secondaryScreen", String.valueOf(config.isCoManHinhPhu()));
        props.setProperty("customerDisplay.secondaryLogo", config.getLogoManHinhPhuPath() != null ? config.getLogoManHinhPhuPath() : "");
        props.setProperty("customerDisplay.secondaryScreenUseStandardUi", String.valueOf(config.isManHinhPhuSuDungGiaoDienThuong()));

        // Save Cảnh báo
        props.setProperty("alert.showAlert", String.valueOf(config.isHienThiCanhBao()));
        props.setProperty("alert.notifyBelowSafeStock", String.valueOf(config.isThongBaoHangDuoiMucAnToan()));
        props.setProperty("alert.notifyCustomerBirthday", String.valueOf(config.isThongBaoKhachDenNgaySinhNhat()));
        props.setProperty("alert.notifyExpiredItem", String.valueOf(config.isThongBaoHangHetHanDung()));
        props.setProperty("alert.notifyNearExpiryItem", String.valueOf(config.isThongBaoHangSapHetHanDung()));
        props.setProperty("alert.notifyBeforeDays", String.valueOf(config.getThongBaoTruocNgay()));

        // Save Cảm ứng
        props.setProperty("touch.printKitchenOnInvoice", String.valueOf(config.isInPhaCheKhiInHoaDon()));
        props.setProperty("touch.kitchenTemplate", config.getMauInPhaChe() != null ? config.getMauInPhaChe() : "Mẫu in chế biến");
        props.setProperty("touch.kitchenPrinter", config.getMayInPhaChe() != null ? config.getMayInPhaChe() : "Microsoft XPS Document Writer");
        props.setProperty("touch.printKitchenOnDraft", String.valueOf(config.isInPhaCheKhiLuuTam()));
        props.setProperty("touch.kitchenDraftTemplate", config.getMauInPhaCheLuuTam() != null ? config.getMauInPhaCheLuuTam() : "");

        // Save Quản trị
        props.setProperty("admin.recentVoucherAtBottom", String.valueOf(config.isPhieuGanNhatNamPhiaDuoi()));
        props.setProperty("admin.gridInputKey", config.getPhimNhapLieuTrenLuoi() != null ? config.getPhimNhapLieuTrenLuoi() : "Tab để sang ngang, Enter để xuống dòng");
        props.setProperty("admin.autoBackupData", String.valueOf(config.isTuDongSaoLuuDuLieu()));
        props.setProperty("admin.backupPath", config.getDuongDanSaoLuu() != null ? config.getDuongDanSaoLuu() : "");
        props.setProperty("admin.backupDays", String.valueOf(config.getSoNgaySaoLuu()));
        props.setProperty("admin.defaultFeature", config.getChucNangMacDinh() != null ? config.getChucNangMacDinh() : "Hóa đơn bán hàng");
        props.setProperty("admin.filterIgnoreWhitespace", String.valueOf(config.isLocDuLieuBoKhoangTrong()));
        props.setProperty("admin.wrapTextIfColSmall", String.valueOf(config.isHienThiXuongDongNeuDoRongCotNho()));
        props.setProperty("admin.fontSizeScale", config.getDoToChuHienThi() != null ? config.getDoToChuHienThi() : "Nhỏ (110%)");
        props.setProperty("admin.uiTheme", config.getGiaoDienChuongTrinh() != null ? config.getGiaoDienChuongTrinh() : "Office 2010 - Blue");
        props.setProperty("admin.autoReloadOnTabChange", String.valueOf(config.isTuDongTaiLaiDuLieuKhiChuyenTab()));
        props.setProperty("admin.enableAuditTrail", String.valueOf(config.isKichHoatLuuVetHoatDong()));
        props.setProperty("admin.multiWorkstations", String.valueOf(config.isHeThongChayNhieuMayTram()));

        // Save Cân điện tử
        props.setProperty("scale.enabled", String.valueOf(config.isKichHoatSuDungCanDienTu()));
        props.setProperty("scale.comPort", config.getCongComCanDienTu() != null ? config.getCongComCanDienTu() : "");
        props.setProperty("scale.baudRate", config.getBaudRateCanDienTu() != null ? config.getBaudRateCanDienTu() : "1200");
        props.setProperty("scale.databit", config.getDatabitCanDienTu() != null ? config.getDatabitCanDienTu() : "0");
        props.setProperty("scale.parity", config.getParityCanDienTu() != null ? config.getParityCanDienTu() : "None");
        props.setProperty("scale.stopBits", config.getStopBitsCanDienTu() != null ? config.getStopBitsCanDienTu() : "");
        props.setProperty("scale.timeout", String.valueOf(config.getTimeoutCanDienTu()));
        props.setProperty("scale.position", String.valueOf(config.getViTriCanDienTu()));
        props.setProperty("scale.length", String.valueOf(config.getDoDaiCanDienTu()));
        props.setProperty("scale.autoDetect", String.valueOf(config.isTuDongNhanDangCanDienTu()));
        props.setProperty("scale.unit", config.getDonViTrenCan() != null ? config.getDonViTrenCan() : "Gram");

        File primaryFile = getConfigFile();
        try {
            if (primaryFile.getParentFile() != null && !primaryFile.getParentFile().exists()) {
                primaryFile.getParentFile().mkdirs();
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(primaryFile), StandardCharsets.UTF_8)) {
                props.store(writer, "Cấu hình hệ thống QLBH");
            }

            // Also synchronize to alternative location if existing
            File altFile = new File("QLBH_Clone", CONFIG_FILE_NAME);
            if (!primaryFile.getCanonicalPath().equalsIgnoreCase(altFile.getCanonicalPath()) && altFile.exists()) {
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(altFile), StandardCharsets.UTF_8)) {
                    props.store(writer, "Cấu hình hệ thống QLBH");
                } catch (Exception ignored) {}
            }
            File rootFile = new File(CONFIG_FILE_NAME);
            if (!primaryFile.getCanonicalPath().equalsIgnoreCase(rootFile.getCanonicalPath()) && rootFile.exists()) {
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(rootFile), StandardCharsets.UTF_8)) {
                    props.store(writer, "Cấu hình hệ thống QLBH");
                } catch (Exception ignored) {}
            }
            ActivityLogService.log("Cấu hình hệ thống", "SỬA", "",
                    "Cập nhật cấu hình toàn bộ hệ thống");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
