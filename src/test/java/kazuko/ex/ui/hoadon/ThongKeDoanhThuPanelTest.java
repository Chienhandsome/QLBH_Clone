package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThongKeDoanhThuPanelTest {
    @Test
    void columnConfigurationMatchesRevenueScreen() {
        assertTrue(ThongKeDoanhThuPanel.DEFAULT_INVOICE_COLUMNS.contains("Tổng cộng"));
        assertTrue(ThongKeDoanhThuPanel.DEFAULT_INVOICE_COLUMNS.contains("Thanh toán bởi"));
        assertTrue(ThongKeDoanhThuPanel.ALL_INVOICE_COLUMNS.containsAll(List.of("Địa chỉ", "Điện thoại")));
        assertFalse(ThongKeDoanhThuPanel.DEFAULT_INVOICE_COLUMNS.contains("Địa chỉ"));
        assertEquals(List.of("Mã hàng", "Tên mặt hàng", "ĐVT", "Đơn giá", "CK%", "Thành tiền",
                "Bảo hành", "S.lượng"), ThongKeDoanhThuPanel.DEFAULT_DETAIL_COLUMNS);
    }

    @Test
    void returnedItemsUseNegativeRevenueAndQuantity() {
        DonHangChiTiet detail = new DonHangChiTiet();
        detail.setSoLuong(2);
        detail.setDonGia(50_000);

        assertEquals(-2.0, ThongKeDoanhThuPanel.detailValue(detail, "S.lượng", true));
        assertEquals(-100_000.0, ThongKeDoanhThuPanel.detailValue(detail, "Thành tiền", true));
        assertEquals(100_000.0, ThongKeDoanhThuPanel.detailValue(detail, "Thành tiền", false));
    }

    @Test
    void revenueDaoLoadsInvoicesWithDetails() {
        List<DonHang> invoices = new HoaDonDAO().getDanhSachHoaDon(null, null, "", null);
        assertNotNull(invoices);
        assertFalse(invoices.isEmpty(), "DEMO.FDB phải có hóa đơn mẫu để xác nhận truy vấn thống kê");
        for (DonHang invoice : invoices) {
            assertNotNull(invoice.getChiTietLayHang());
            assertNotNull(invoice.getChiTietTraHang());
        }
    }
}
