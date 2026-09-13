package kazuko.ex.ui.hoadon;

import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.system.SystemConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesConfigSupportTest {

    @Test
    void parsesAndAppliesConfiguredMoneyRounding() {
        assertEquals(1000, SalesConfigSupport.parseRoundingUnit("1,000"));
        assertEquals(12_000, SalesConfigSupport.roundMoney(12_499, "1,000"));
        assertEquals(13_000, SalesConfigSupport.roundMoney(12_500, "1,000"));
        assertEquals(12_345, SalesConfigSupport.roundMoney(12_345, "0"));
        assertEquals(12_345, SalesConfigSupport.roundMoney(12_345, "không hợp lệ"));
    }

    @Test
    void calculatesTaxShippingAndReturnsInTheRightOrder() {
        double grossItems = 1_000_000d;
        double itemDiscount = 100_000d;
        double invoiceDiscount = (grossItems - itemDiscount) * 0.10d;
        double taxable = grossItems - itemDiscount - invoiceDiscount;
        double tax = SalesConfigSupport.calculateTax(taxable, 8d);

        assertEquals(64_800d, tax, 0.001);
        assertEquals(924_800d, SalesConfigSupport.calculateInvoiceTotal(
                taxable, tax, 100_000d, 50_000d, "1"), 0.001);
    }

    @Test
    void enforcesReturnWindowByCalendarDay() {
        Date returnDate = date(2026, 9, 9);

        assertTrue(SalesConfigSupport.isReturnAllowed(date(2026, 9, 3), returnDate, 6));
        assertFalse(SalesConfigSupport.isReturnAllowed(date(2026, 9, 2), returnDate, 6));
        assertFalse(SalesConfigSupport.isReturnAllowed(date(2026, 9, 10), returnDate, 6));
    }

    @Test
    void resolvesEveryCustomerPriceCodeAndFallsBackToCatalogPrice() {
        assertEquals(100_000d, customerPrice(KhachHang.GIA_THEO_DANH_MUC, 80_000d), 0.001);
        assertEquals(90_000d, customerPrice(KhachHang.GIA_BAN_2, 80_000d), 0.001);
        assertEquals(85_000d, customerPrice(KhachHang.GIA_BAN_3, 80_000d), 0.001);
        assertEquals(82_000d, customerPrice(KhachHang.GIA_BAN_4, 80_000d), 0.001);
        assertEquals(70_000d, customerPrice(KhachHang.GIA_VON, 80_000d), 0.001);
        assertEquals(80_000d, customerPrice(KhachHang.GIA_GAN_NHAT, 80_000d), 0.001);

        assertEquals(100_000d, customerPrice(KhachHang.GIA_BAN_2, 80_000d, 0d), 0.001);
        assertEquals(100_000d, customerPrice(KhachHang.GIA_GAN_NHAT, null), 0.001);
    }

    @Test
    void usesConfiguredCustomerPriceNames() {
        SystemConfig config = new SystemConfig();
        config.setDienGiaiGia2("Giá đại lý");
        config.setDienGiaiGia3("Giá VIP");

        assertEquals("Giá đại lý", SalesConfigSupport.customerPriceName(KhachHang.GIA_BAN_2, config));
        assertEquals("Giá VIP", SalesConfigSupport.customerPriceName(KhachHang.GIA_BAN_3, config));
        assertEquals("Giá vốn", SalesConfigSupport.customerPriceName(KhachHang.GIA_VON, config));
        assertEquals("Giá gần nhất", SalesConfigSupport.customerPriceName(KhachHang.GIA_GAN_NHAT, config));
    }

    private double customerPrice(int priceCode, Double latestPrice) {
        return customerPrice(priceCode, latestPrice, 90_000d);
    }

    private double customerPrice(int priceCode, Double latestPrice, double price2) {
        return SalesConfigSupport.resolveCustomerPrice(priceCode,
                100_000d, price2, 85_000d, 82_000d, 70_000d, latestPrice);
    }

    private Date date(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
