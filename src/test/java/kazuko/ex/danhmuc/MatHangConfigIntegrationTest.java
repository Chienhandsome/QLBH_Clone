package kazuko.ex.danhmuc;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatHangConfigIntegrationTest {
    private MatHangDAO matHangDAO;

    @BeforeEach
    public void setUp() {
        matHangDAO = new MatHangDAO();
    }

    @Test
    public void testMatHangNewFields() {
        MatHang mh = new MatHang();
        mh.setKichThuoc("XL - 120x80cm");

        assertEquals("XL - 120x80cm", mh.getKichThuoc());
    }

    @Test
    public void testSizeFieldEditableFollowsMatHangCoKichThuoc() {
        SystemConfig config = new SystemConfig();
        config.setMatHangCoKichThuoc(true);
        assertTrue(config.isMatHangCoKichThuoc(), "Size should be editable when MatHangCoKichThuoc is true");

        config.setMatHangCoKichThuoc(false);
        assertFalse(config.isMatHangCoKichThuoc(), "Size should not be editable when MatHangCoKichThuoc is false");
    }

    @Test
    public void testCoHanSuDungFollowsMatHangCoHanSuDung() {
        SystemConfig config = new SystemConfig();
        config.setMatHangCoHanSuDung(true);
        assertTrue(config.isMatHangCoHanSuDung(), "CoHanSuDung should be enabled when MatHangCoHanSuDung is true");

        config.setMatHangCoHanSuDung(false);
        assertFalse(config.isMatHangCoHanSuDung(), "CoHanSuDung should be disabled when MatHangCoHanSuDung is false");
    }

    @Test
    public void testGenerateBarcodeWithChecksumAndLength() {
        SystemConfig config = new SystemConfig();
        config.setTuDongSinhMaVach(true);
        config.setChieuDaiMaVach(12);
        config.setCoKyTuKiemTra(true);
        config.setSuDungTemInBangCanDienTu(false);

        String code = matHangDAO.generateProductCodeOrBarcode(config, "MH", false);
        assertNotNull(code);
        assertEquals(12, code.length());
        // Must end with checksum digit
        char lastChar = code.charAt(code.length() - 1);
        assertTrue(Character.isDigit(lastChar));
    }

    @Test
    public void testGenerateScaleBarcode() {
        SystemConfig config = new SystemConfig();
        config.setTuDongSinhMaVach(true);
        config.setSuDungTemInBangCanDienTu(true);
        config.setChuoiBatDauCanDienTu("29");
        config.setChieuDaiMaVach(13);
        config.setCoKyTuKiemTra(true);

        String code = matHangDAO.generateProductCodeOrBarcode(config, "MH", true);
        assertNotNull(code);
        assertTrue(code.startsWith("29"));
        assertEquals(13, code.length());
    }

    @Test
    public void testDuplicateNameCheck() {
        List<MatHang> all = matHangDAO.getAll();
        assertFalse(all.isEmpty(), "Product list should not be empty");
        MatHang first = all.get(0);

        // Check with different ID -> duplicate detected
        boolean exists = matHangDAO.existsByName(first.getName(), "non-existent-id-99999");
        assertTrue(exists, "Existing product name should be detected");

        // Same product ID -> allowed (self update)
        boolean selfCheck = matHangDAO.existsByName(first.getName(), first.getId());
        assertFalse(selfCheck, "Same product ID should not trigger duplicate name");

        // Random name -> not duplicate
        boolean notExists = matHangDAO.existsByName("Tên Mặt Hàng Hoàn Toàn Mới " + System.currentTimeMillis(), null);
        assertFalse(notExists, "New product name should not be duplicate");
    }

    @Test
    public void testSearchWithConfigTimTheoAndCachTim() {
        List<MatHang> all = matHangDAO.getAll();
        assertFalse(all.isEmpty());
        MatHang sample = all.get(0);

        SystemConfig config = new SystemConfig();

        // 1. Search Contains both Code and Name
        config.setTimTheo("Cả tên hàng và mã hàng...");
        config.setCachTim("Tìm có chứa...");
        List<MatHang> results1 = matHangDAO.searchWithConfig(sample.getCode(), null, null, config);
        assertFalse(results1.isEmpty(), "Should find item by code when searching both");

        // 2. Search StartsWith
        config.setCachTim("Tìm bắt đầu...");
        String prefix = sample.getCode().substring(0, Math.min(2, sample.getCode().length()));
        List<MatHang> results2 = matHangDAO.searchWithConfig(prefix, null, null, config);
        assertFalse(results2.isEmpty(), "Should find items starting with prefix");

        // 3. Search Exact
        config.setCachTim("Tìm chính xác...");
        List<MatHang> results3 = matHangDAO.searchWithConfig(sample.getName(), null, null, config);
        assertFalse(results3.isEmpty(), "Should find exact matching item");
        assertTrue(results3.stream().anyMatch(m -> m.getName().equalsIgnoreCase(sample.getName())));

        // 4. Search Code only
        config.setTimTheo("Mã hàng");
        config.setCachTim("Tìm chính xác...");
        List<MatHang> results4 = matHangDAO.searchWithConfig(sample.getCode(), null, null, config);
        assertFalse(results4.isEmpty());
        assertTrue(results4.stream().anyMatch(m -> m.getCode().equalsIgnoreCase(sample.getCode())));

        // 5. Search Name only - searching non-existent name returns empty
        config.setTimTheo("Tên hàng");
        config.setCachTim("Tìm chính xác...");
        List<MatHang> results5 = matHangDAO.searchWithConfig("CODE_THAT_DOES_NOT_EXIST_AS_NAME_12345", null, null, config);
        assertTrue(results5.isEmpty());
    }

    @Test
    public void testMultiplePriceConfigAndLabels() {
        SystemConfig config = new SystemConfig();
        config.setSuDungGia2(true);
        config.setDienGiaiGia2("Giá bán đại lý");
        config.setSuDungGia3(false);
        config.setDienGiaiGia3("Giá bán VIP");
        config.setSuDungGia4(true);
        config.setDienGiaiGia4("Giá bán sỉ");

        assertTrue(config.isSuDungGia2());
        assertEquals("Giá bán đại lý", config.getDienGiaiGia2());
        assertFalse(config.isSuDungGia3());
        assertEquals("Giá bán VIP", config.getDienGiaiGia3());
        assertTrue(config.isSuDungGia4());
        assertEquals("Giá bán sỉ", config.getDienGiaiGia4());
    }

    @Test
    public void testInsertAndUpdateMatHangAllFields() {
        kazuko.ex.dao.DonViTinhDAO dvtDAO = new kazuko.ex.dao.DonViTinhDAO();
        List<kazuko.ex.model.danhmuc.DonViTinh> dvts = dvtDAO.getAll();
        assertFalse(dvts.isEmpty(), "Need at least 1 unit to test");
        String dvtId = dvts.get(0).getId();
        String dvtChanId = dvts.size() > 1 ? dvts.get(1).getId() : dvts.get(0).getId();

        MatHang mh = new MatHang();
        String code = "TEST_" + System.currentTimeMillis();
        String name = "Mặt hàng kiểm thử " + System.currentTimeMillis();
        mh.setCode(code);
        mh.setName(name);
        mh.setDonViTinhId(dvtId);
        mh.setDonViTinhChanId(dvtChanId);
        mh.setQuyDoi(12.0);
        mh.setGiaBan(50000.0);
        mh.setGiaBanChan(550000.0);
        mh.setGiaBan2(48000.0);
        mh.setGiaBan3(47000.0);
        mh.setGiaBan4(46000.0);
        mh.setGiamGiaPt(5.5);
        mh.setGiamGiaTien(2000.0);
        mh.setGiaNhap(40000.0);
        mh.setKichThuoc("M");
        mh.setBaoHanh("12 tháng");
        mh.setHoaHong(1.5);
        mh.setPhuongThucTruKho(1);

        boolean insertOk = matHangDAO.insert(mh);
        assertTrue(insertOk, "Insert MatHang should succeed");

        List<MatHang> all = matHangDAO.getAll();
        MatHang saved = all.stream().filter(m -> code.equals(m.getCode())).findFirst().orElse(null);
        assertNotNull(saved, "Saved product should be found in DB");
        assertNotNull(saved.getId(), "Saved product should have an ID");
        assertEquals(name, saved.getName());
        assertEquals(dvtChanId, saved.getDonViTinhChanId());
        assertEquals(12.0, saved.getQuyDoi(), 0.001);
        assertEquals(50000.0, saved.getGiaBan(), 0.001);
        assertEquals(550000.0, saved.getGiaBanChan(), 0.001);
        assertEquals(48000.0, saved.getGiaBan2(), 0.001);
        assertEquals(47000.0, saved.getGiaBan3(), 0.001);
        assertEquals(46000.0, saved.getGiaBan4(), 0.001);
        assertEquals(5.5, saved.getGiamGiaPt(), 0.001);
        assertEquals(2000.0, saved.getGiamGiaTien(), 0.001);

        // Update fields
        saved.setName(name + " (Updated)");
        saved.setGiaBan(60000.0);
        saved.setGiaBanChan(650000.0);
        saved.setQuyDoi(24.0);
        saved.setGiaBan2(58000.0);
        saved.setGiaBan3(57000.0);
        saved.setGiaBan4(56000.0);
        saved.setGiamGiaPt(10.0);
        saved.setGiamGiaTien(5000.0);
        saved.setDonViTinhChanId(null); // Clear bulk unit

        boolean updateOk = matHangDAO.update(saved);
        assertTrue(updateOk, "Update MatHang should succeed");

        List<MatHang> allAfterUpdate = matHangDAO.getAll();
        MatHang updated = allAfterUpdate.stream().filter(m -> code.equals(m.getCode())).findFirst().orElse(null);
        assertNotNull(updated, "Updated product should be found");
        assertEquals(name + " (Updated)", updated.getName());
        assertNull(updated.getDonViTinhChanId(), "Bulk unit should be cleared/null");
        assertEquals(24.0, updated.getQuyDoi(), 0.001);
        assertEquals(60000.0, updated.getGiaBan(), 0.001);
        assertEquals(650000.0, updated.getGiaBanChan(), 0.001);
        assertEquals(58000.0, updated.getGiaBan2(), 0.001);
        assertEquals(57000.0, updated.getGiaBan3(), 0.001);
        assertEquals(56000.0, updated.getGiaBan4(), 0.001);
        assertEquals(10.0, updated.getGiamGiaPt(), 0.001);
        assertEquals(5000.0, updated.getGiamGiaTien(), 0.001);

        // Clean up
        matHangDAO.deletePermanently(updated.getId());
    }
}
