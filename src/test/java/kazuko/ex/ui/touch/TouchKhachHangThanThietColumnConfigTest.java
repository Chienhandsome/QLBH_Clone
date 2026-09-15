package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhachHangLoyaltySummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TouchKhachHangThanThietColumnConfigTest {

    @Test
    void testDefaultConfig() {
        TouchKhachHangThanThietColumnConfig cfg = TouchKhachHangThanThietColumnConfig.createDefault();
        assertNotNull(cfg.getUsedColumns());
        assertEquals(2, cfg.getUsedColumns().size(), "Mặc định hiển thị 2 cột: Tên khách hàng, Điểm tích lũy");
        assertEquals("NAME", cfg.getUsedColumns().get(0).getKey());
        assertEquals("DIEM_TICH_LUY", cfg.getUsedColumns().get(1).getKey());

        assertEquals(1, cfg.getGridColumns(), "Mặc định 1 cột");
        assertEquals(8, cfg.getGridRows(), "Mặc định 8 dòng");
        assertEquals("#004F4C", cfg.getCardBgColorHex());
    }

    @Test
    void testGetRawAndFormattedValue() {
        KhachHang kh = new KhachHang();
        kh.setMaKhach("KH0012");
        kh.setName("An Khang");
        kh.setDienThoai("0987654321");
        kh.setDiaChi("123 Phố Huế");

        KhachHangLoyaltySummary summary = new KhachHangLoyaltySummary(kh, 1500000.0, 3, 75.0);

        assertEquals("An Khang", TouchKhachHangThanThietColumnConfig.getRawValue(summary, "NAME"));
        assertEquals("75", TouchKhachHangThanThietColumnConfig.getRawValue(summary, "DIEM_TICH_LUY"));
        assertEquals("0987654321", TouchKhachHangThanThietColumnConfig.getRawValue(summary, "DIEN_THOAI"));
        assertEquals("123 Phố Huế", TouchKhachHangThanThietColumnConfig.getRawValue(summary, "DIA_CHI"));
        assertEquals("1,500,000", TouchKhachHangThanThietColumnConfig.getRawValue(summary, "DOANH_SO"));
        assertEquals("3", TouchKhachHangThanThietColumnConfig.getRawValue(summary, "SO_HOA_DON"));

        TouchKhachHangThanThietColumnConfig.ColumnItem colName = new TouchKhachHangThanThietColumnConfig.ColumnItem(
                "NAME", "TÊN KHÁCH HÀNG", "KH: ", "#FFFFFF", false, false, false, 15
        );
        assertEquals("KH: An Khang", TouchKhachHangThanThietColumnConfig.getFormattedText(summary, colName, true));
        assertEquals("An Khang", TouchKhachHangThanThietColumnConfig.getFormattedText(summary, colName, false));
    }

    @Test
    void testClone() {
        TouchKhachHangThanThietColumnConfig cfg = TouchKhachHangThanThietColumnConfig.createDefault();
        TouchKhachHangThanThietColumnConfig copy = cfg.clone();

        assertEquals(cfg.getGridColumns(), copy.getGridColumns());
        assertEquals(cfg.getGridRows(), copy.getGridRows());
        assertEquals(cfg.getCardBgColorHex(), copy.getCardBgColorHex());

        copy.setGridColumns(2);
        assertEquals(1, cfg.getGridColumns());
        assertEquals(2, copy.getGridColumns());
    }
}
