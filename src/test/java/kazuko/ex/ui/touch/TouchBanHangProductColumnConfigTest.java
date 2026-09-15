package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.MatHang;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TouchBanHangProductColumnConfigTest {

    @Test
    void testDefaultConfig() {
        TouchBanHangProductColumnConfig cfg = TouchBanHangProductColumnConfig.createDefault();
        assertNotNull(cfg.getUsedColumns());
        assertEquals(2, cfg.getUsedColumns().size(), "Mặc định gồm 2 cột sử dụng như Hình 4: TÊN HÀNG, GIÁ BÁN");
        assertEquals("TEN_HANG", cfg.getUsedColumns().get(0).getKey());
        assertEquals("GIA_BAN", cfg.getUsedColumns().get(1).getKey());

        assertNotNull(cfg.getUnusedColumns());
        assertEquals(4, cfg.getUnusedColumns().size(), "Mặc định gồm 4 cột chưa dùng như Hình 4");
        assertEquals("DVT", cfg.getUnusedColumns().get(0).getKey());
        assertEquals("MA_HANG", cfg.getUnusedColumns().get(1).getKey());
        assertEquals("BAO_HANH", cfg.getUnusedColumns().get(2).getKey());
        assertEquals("GHI_CHU", cfg.getUnusedColumns().get(3).getKey());
    }

    @Test
    void testGetRawAndFormattedValue() {
        MatHang mh = new MatHang();
        mh.setCode("02B");
        mh.setName("Bếp ga 02B123");
        mh.setTenDonViTinh("Bịch");
        mh.setGiaBan(63800002);
        mh.setBaoHanh("12 Tháng");
        mh.setGhiChu("Hàng mẫu");

        assertEquals("Bếp ga 02B123", TouchBanHangProductColumnConfig.getRawValue(mh, "TEN_HANG"));
        assertEquals("63,800,002", TouchBanHangProductColumnConfig.getRawValue(mh, "GIA_BAN"));
        assertEquals("Bịch", TouchBanHangProductColumnConfig.getRawValue(mh, "DVT"));
        assertEquals("02B", TouchBanHangProductColumnConfig.getRawValue(mh, "MA_HANG"));
        assertEquals("12 Tháng", TouchBanHangProductColumnConfig.getRawValue(mh, "BAO_HANH"));
        assertEquals("Hàng mẫu", TouchBanHangProductColumnConfig.getRawValue(mh, "GHI_CHU"));
    }

    @Test
    void testSaveAndLoadConfig() {
        TouchBanHangProductColumnConfig cfg = TouchBanHangProductColumnConfig.createDefault();
        boolean saved = TouchBanHangProductConfigDAO.saveConfig(cfg);
        assertTrue(saved);

        TouchBanHangProductColumnConfig loaded = TouchBanHangProductConfigDAO.loadConfig();
        assertNotNull(loaded);
        assertEquals(2, loaded.getUsedColumns().size());
        assertEquals(4, loaded.getUnusedColumns().size());
    }
}
