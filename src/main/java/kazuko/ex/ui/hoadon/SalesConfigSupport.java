package kazuko.ex.ui.hoadon;

import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.system.SystemConfig;

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

    static double calculateTax(double taxableAmount, double taxRate) {
        double safeBase = Math.max(0, taxableAmount);
        double safeRate = Math.max(0, Math.min(100, taxRate));
        return safeBase * safeRate / 100.0;
    }

    static double calculateInvoiceTotal(double taxableAmount, double taxAmount,
                                        double shippingFee, double returnAmount,
                                        String roundingUnit) {
        double amount = Math.max(0, taxableAmount)
                + Math.max(0, taxAmount)
                + Math.max(0, shippingFee)
                - Math.max(0, returnAmount);
        return roundMoney(Math.max(0, amount), roundingUnit);
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

    static double resolveCustomerPrice(int priceCode, double catalogPrice,
                                       double price2, double price3, double price4,
                                       double costPrice, Double latestPrice) {
        double selected;
        switch (priceCode) {
            case KhachHang.GIA_BAN_2: selected = price2; break;
            case KhachHang.GIA_BAN_3: selected = price3; break;
            case KhachHang.GIA_BAN_4: selected = price4; break;
            case KhachHang.GIA_VON: selected = costPrice; break;
            case KhachHang.GIA_GAN_NHAT: selected = latestPrice != null ? latestPrice : 0; break;
            default: selected = catalogPrice; break;
        }
        return Math.round(selected > 0 ? selected : Math.max(0, catalogPrice));
    }

    static String customerPriceName(int priceCode, SystemConfig config) {
        switch (priceCode) {
            case KhachHang.GIA_BAN_2:
                return configuredName(config != null ? config.getDienGiaiGia2() : null, "Đơn giá 2");
            case KhachHang.GIA_BAN_3:
                return configuredName(config != null ? config.getDienGiaiGia3() : null, "Giá bán 3");
            case KhachHang.GIA_BAN_4:
                return configuredName(config != null ? config.getDienGiaiGia4() : null, "Giá bán 4");
            case KhachHang.GIA_VON: return "Giá vốn";
            case KhachHang.GIA_GAN_NHAT: return "Giá gần nhất";
            default: return "Giá bán";
        }
    }

    private static String configuredName(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }
}
