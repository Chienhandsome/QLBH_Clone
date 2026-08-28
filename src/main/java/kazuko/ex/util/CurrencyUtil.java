package kazuko.ex.util;

import java.text.DecimalFormat;

public class CurrencyUtil {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final DecimalFormat QTY_FORMAT = new DecimalFormat("#,##0.##");

    public static String formatMoney(double amount) {
        return MONEY_FORMAT.format(amount);
    }

    public static String formatMoney(Double amount) {
        if (amount == null) return "0";
        return MONEY_FORMAT.format(amount);
    }

    public static String format(double amount) {
        return MONEY_FORMAT.format(amount);
    }

    public static String format(Double amount) {
        if (amount == null) return "0";
        return MONEY_FORMAT.format(amount);
    }

    public static String formatQuantity(double quantity) {
        return QTY_FORMAT.format(quantity);
    }
}
