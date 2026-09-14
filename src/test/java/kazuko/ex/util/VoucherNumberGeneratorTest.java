package kazuko.ex.util;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoucherNumberGeneratorTest {

    @Test
    void rendersDateTokensAndFindsNextSequence() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.SEPTEMBER, 14);

        VoucherNumberGenerator.CompiledPattern pattern = VoucherNumberGenerator.compile(
                "PX(yyyy)(MM)(dd)/(****)", calendar.getTime());

        assertEquals("PX20260914/%", pattern.getSqlLikePattern());
        assertEquals("PX20260914/0013", pattern.next(List.of(
                "PX20260914/0002",
                "PX20260914/0012",
                "PX20260913/9999",
                "không hợp lệ"
        )));
    }

    @Test
    void supportsSequenceInTheMiddleAndLiteralGroups() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.JANUARY, 1);

        VoucherNumberGenerator.CompiledPattern pattern = VoucherNumberGenerator.compile(
                "(BG)-(***)-(yy)", calendar.getTime());

        assertEquals("BG-008-26", pattern.next(List.of("BG-007-26", "BG-999-25")));
    }

    @Test
    void supportsStoreCodeTokenWhenProvided() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.JANUARY, 1);

        VoucherNumberGenerator.CompiledPattern pattern = VoucherNumberGenerator.compile(
                "(CH)(yy)(******)", calendar.getTime(), Map.of("CH", "01"));

        assertEquals("0126000002", pattern.next(List.of("0126000001")));
    }

    @Test
    void validatesSequenceRules() {
        assertNull(VoucherNumberGenerator.validate("PT(yy)/(*****)"));
        assertNotNull(VoucherNumberGenerator.validate("PT(yy)/00001"));
        assertNotNull(VoucherNumberGenerator.validate("PT(**)-(**)"));
        assertNotNull(VoucherNumberGenerator.validate("PT(**********)"));
        assertNotNull(VoucherNumberGenerator.validate("PT(yy/(*****)"));
        assertThrows(IllegalArgumentException.class,
                () -> VoucherNumberGenerator.compile("PT", null));
    }
}
