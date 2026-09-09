package kazuko.ex.ui.hoadon;

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
    void enforcesReturnWindowByCalendarDay() {
        Date returnDate = date(2026, 9, 9);

        assertTrue(SalesConfigSupport.isReturnAllowed(date(2026, 9, 3), returnDate, 6));
        assertFalse(SalesConfigSupport.isReturnAllowed(date(2026, 9, 2), returnDate, 6));
        assertFalse(SalesConfigSupport.isReturnAllowed(date(2026, 9, 10), returnDate, 6));
    }

    private Date date(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
