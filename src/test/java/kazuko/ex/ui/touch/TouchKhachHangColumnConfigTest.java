package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.KhachHang;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class TouchKhachHangColumnConfigTest {

    @Test
    void testDefaultConfig() {
        TouchKhachHangColumnConfig cfg = TouchKhachHangColumnConfig.createDefault();
        assertNotNull(cfg.getUsedColumns());
        assertEquals(3, cfg.getUsedColumns().size(), "Mặc định hiển thị 3 dòng: Tỉnh thành, Mã khách, Tên khách");
        assertEquals("TINH_THANH", cfg.getUsedColumns().get(0).getKey());
        assertEquals("MA_KHACH", cfg.getUsedColumns().get(1).getKey());
        assertEquals("NAME", cfg.getUsedColumns().get(2).getKey());

        assertEquals(4, cfg.getGridColumns(), "Mặc định 4 cột");
        assertEquals(6, cfg.getGridRows(), "Mặc định 6 dòng");
        assertEquals("#004F4C", cfg.getCardBgColorHex());
    }

    @Test
    void testGetRawAndFormattedValue() {
        TouchKhachHangColumnConfig cfg = TouchKhachHangColumnConfig.createDefault();
        KhachHang kh = new KhachHang();
        kh.setMaKhach("KH0012");
        kh.setName("Nguyễn Văn A");
        kh.setTinhThanh("Hà Nội");
        kh.setDienThoai("0987654321");
        kh.setDiemTichLuy(250);

        assertEquals("Hà Nội", TouchKhachHangColumnConfig.getRawValue(kh, "TINH_THANH"));
        assertEquals("KH0012", TouchKhachHangColumnConfig.getRawValue(kh, "MA_KHACH"));
        assertEquals("Nguyễn Văn A", TouchKhachHangColumnConfig.getRawValue(kh, "NAME"));
        assertEquals("0987654321", TouchKhachHangColumnConfig.getRawValue(kh, "DIEN_THOAI"));
        assertEquals("250", TouchKhachHangColumnConfig.getRawValue(kh, "DIEM_TICH_LUY"));

        TouchKhachHangColumnConfig.ColumnItem colPhone = new TouchKhachHangColumnConfig.ColumnItem(
                "DIEN_THOAI", "ĐIỆN THOẠI", "ĐT: ", "#FFFFFF", false, false, false, 14
        );
        assertEquals("ĐT: 0987654321", TouchKhachHangColumnConfig.getFormattedText(kh, colPhone, true));
        assertEquals("0987654321", TouchKhachHangColumnConfig.getFormattedText(kh, colPhone, false));
    }

    @Test
    void testClone() {
        TouchKhachHangColumnConfig cfg = TouchKhachHangColumnConfig.createDefault();
        TouchKhachHangColumnConfig copy = cfg.clone();

        assertEquals(cfg.getGridColumns(), copy.getGridColumns());
        assertEquals(cfg.getGridRows(), copy.getGridRows());
        assertEquals(cfg.getCardBgColorHex(), copy.getCardBgColorHex());

        // Thay đổi bản copy không ảnh hưởng bản gốc
        copy.setGridColumns(5);
        assertEquals(4, cfg.getGridColumns());
        assertEquals(5, copy.getGridColumns());
    }
}
