package kazuko.ex.ui.hoadon;

import java.util.Calendar;
import java.util.Date;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** Các quy tắc bán hàng thuần, tách khỏi Swing để dùng thống nhất và dễ kiểm thử. */
final class SalesConfigSupport {
    private SalesConfigSupport() {}

    static long parseRoundingUnit(String configuredValue) {
        if (configuredValue == null) return 0L;
        String digits = configuredValue.trim().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0L;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    static double roundMoney(double amount, String configuredValue) {
        long unit = parseRoundingUnit(configuredValue);
        if (unit <= 0 || amount <= 0) return Math.max(0, amount);
        return Math.round(amount / unit) * (double) unit;
    }

    static boolean isReturnAllowed(Date invoiceDate, Date returnDate, int allowedDays) {
        if (invoiceDate == null || returnDate == null) return false;
        ZoneId zone = ZoneId.systemDefault();
        long elapsedDays = ChronoUnit.DAYS.between(
                invoiceDate.toInstant().atZone(zone).toLocalDate(),
                returnDate.toInstant().atZone(zone).toLocalDate());
        return elapsedDays >= 0 && elapsedDays <= Math.max(0, allowedDays);
    }

    static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
