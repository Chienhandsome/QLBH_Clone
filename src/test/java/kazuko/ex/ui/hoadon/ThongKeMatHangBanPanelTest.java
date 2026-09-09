package kazuko.ex.ui.hoadon;

import kazuko.ex.model.banhang.ThongKeMatHangBanRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThongKeMatHangBanPanelTest {

    @Test
    void testColumnsConfiguration() {
        assertEquals(11, ThongKeMatHangBanPanel.ALL_COLUMNS.size());
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Mã hàng"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Tên mặt hàng"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Số lượng"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("ĐVT"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Đơn giá"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Thành tiền bán"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Giá vốn"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Giảm giá %"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Thành tiền nhập"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Tỉ lệ lãi"));
        assertTrue(ThongKeMatHangBanPanel.ALL_COLUMNS.contains("Lãi"));
    }

    @Test
    void testRowModelCalculation() {
        ThongKeMatHangBanRow row = new ThongKeMatHangBanRow(
                1, "4k9", "4K9", 1.0, "Chiếc", 0.0, 0.0, 87120.0, 0.0, 87120.0, 0.0, -87120.0, "1", "Sopoka"
        );
        assertEquals("4k9", row.getMaHang());
        assertEquals("4K9", row.getTenMatHang());
        assertEquals(1.0, row.getSoLuong());
        assertEquals("Chiếc", row.getDonViTinh());
        assertEquals(87120.0, row.getGiaVon());
        assertEquals(87120.0, row.getThanhTienNhap());
        assertEquals(-87120.0, row.getLai());
        assertEquals(0.0, row.getTiLeLai());
    }

    @Test
    void testRowModelProfitCalculation() {
        ThongKeMatHangBanRow row = new ThongKeMatHangBanRow(
                2, "1300002", "ALYNA BB Cream", 1.0, "Hộp", 120000.0, 120000.0, 0.0, 0.0, 0.0, 100.0, 120000.0, "2", "Công ty"
        );
        assertEquals(120000.0, row.getThanhTienBan());
        assertEquals(0.0, row.getThanhTienNhap());
        assertEquals(120000.0, row.getLai());
        assertEquals(100.0, row.getTiLeLai());
    }
}
