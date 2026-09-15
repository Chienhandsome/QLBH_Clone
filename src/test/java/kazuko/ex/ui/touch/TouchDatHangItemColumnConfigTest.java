package kazuko.ex.ui.touch;

import kazuko.ex.model.dathang.KhachDatHangChiTiet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TouchDatHangItemColumnConfigTest {

    @Test
    void testDefaultConfig() {
        TouchDatHangItemColumnConfig cfg = TouchDatHangItemColumnConfig.createDefault();
        assertNotNull(cfg.getUsedColumns());
        assertEquals(5, cfg.getUsedColumns().size(), "Mặc định gồm 5 cột sử dụng như Hình bổ sung 1");
        assertEquals("MAT_HANG", cfg.getUsedColumns().get(0).getKey());
        assertEquals("SO_LUONG", cfg.getUsedColumns().get(1).getKey());
        assertEquals("DVT", cfg.getUsedColumns().get(2).getKey());
        assertEquals("GIAM_GIA", cfg.getUsedColumns().get(3).getKey());
        assertEquals("THANH_TIEN", cfg.getUsedColumns().get(4).getKey());

        assertNotNull(cfg.getUnusedColumns());
        assertEquals(3, cfg.getUnusedColumns().size(), "Mặc định gồm 3 cột chưa dùng như Hình bổ sung 1");
        assertEquals("GHI_CHU", cfg.getUnusedColumns().get(0).getKey());
        assertEquals("MA_HANG", cfg.getUnusedColumns().get(1).getKey());
        assertEquals("DON_GIA", cfg.getUnusedColumns().get(2).getKey());
    }

    @Test
    void testGetRawAndFormattedValue() {
        KhachDatHangChiTiet ct = new KhachDatHangChiTiet();
        ct.setMaHang("05IC-A");
        ct.setTenHang("Bếp từ Sato 05IC");
        ct.setTenDonViTinh("Chiếc");
        ct.setSoLuong(2);
        ct.setDonGia(1083500);
        ct.setTiLeGiamGia(5);
        ct.tinhThanhTien();
        ct.setNote("Giao tận nơi");

        assertEquals("Bếp từ Sato 05IC", TouchDatHangItemColumnConfig.getRawValue(ct, "MAT_HANG"));
        assertEquals("2", TouchDatHangItemColumnConfig.getRawValue(ct, "SO_LUONG"));
        assertEquals("Chiếc", TouchDatHangItemColumnConfig.getRawValue(ct, "DVT"));
        assertEquals("5%", TouchDatHangItemColumnConfig.getRawValue(ct, "GIAM_GIA"));
        assertEquals("2,058,650", TouchDatHangItemColumnConfig.getRawValue(ct, "THANH_TIEN"));
        assertEquals("Giao tận nơi", TouchDatHangItemColumnConfig.getRawValue(ct, "GHI_CHU"));
        assertEquals("05IC-A", TouchDatHangItemColumnConfig.getRawValue(ct, "MA_HANG"));
        assertEquals("1,083,500", TouchDatHangItemColumnConfig.getRawValue(ct, "DON_GIA"));
    }

    @Test
    void testSaveAndLoadConfig() {
        TouchDatHangItemColumnConfig cfg = TouchDatHangItemColumnConfig.createDefault();
        boolean saved = TouchDatHangItemConfigDAO.saveConfig(cfg);
        assertTrue(saved);

        TouchDatHangItemColumnConfig loaded = TouchDatHangItemConfigDAO.loadConfig();
        assertNotNull(loaded);
        assertEquals(5, loaded.getUsedColumns().size());
        assertEquals(3, loaded.getUnusedColumns().size());
    }

    @Test
    void testClone() {
        TouchDatHangItemColumnConfig cfg = TouchDatHangItemColumnConfig.createDefault();
        TouchDatHangItemColumnConfig copy = cfg.clone();

        assertEquals(cfg.getUsedColumns().size(), copy.getUsedColumns().size());
        copy.getUsedColumns().remove(0);
        assertNotEquals(cfg.getUsedColumns().size(), copy.getUsedColumns().size());
    }
}
