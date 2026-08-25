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
        mh.setGiaUSD(15.5);

        assertEquals("XL - 120x80cm", mh.getKichThuoc());
        assertEquals(15.5, mh.getGiaUSD(), 0.001);
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
}
