package kazuko.ex.util;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Biên dịch mẫu số phiếu và tìm số thứ tự kế tiếp.
 *
 * <p>Các nhóm được hỗ trợ: (dd), (MM), (yy), (yyyy), (CH) và một nhóm
 * từ 1 đến 9 dấu sao. Dấu ngoặc của các nhóm chữ thông thường chỉ dùng để
 * gom nhóm và không xuất hiện trong số phiếu.</p>
 */
public final class VoucherNumberGenerator {
    private static final Pattern STAR_RUN = Pattern.compile("\\*+");

    private VoucherNumberGenerator() {
    }

    public static CompiledPattern fromConfig(String voucherType, String fallbackPattern, Date date) {
        String configuredPattern = null;
        try {
            SystemConfig config = new SystemConfigDAO().loadConfig();
            if (config != null && config.getVoucherRules() != null) {
                configuredPattern = config.getVoucherRules().get(voucherType);
            }
        } catch (Exception ignored) {
            // Cấu hình không đọc được thì tiếp tục dùng mẫu tương thích cũ.
        }

        String selectedPattern = isBlank(configuredPattern) ? fallbackPattern : configuredPattern.trim();
        try {
            return compile(selectedPattern, date);
        } catch (IllegalArgumentException ex) {
            return compile(fallbackPattern, date);
        }
    }

    public static CompiledPattern compile(String pattern, Date date) {
        return compile(pattern, date, Map.of());
    }

    public static CompiledPattern compile(String pattern, Date date, Map<String, String> values) {
        String error = validate(pattern);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        Date effectiveDate = date != null ? date : new Date();
        Matcher matcher = STAR_RUN.matcher(pattern);
        matcher.find();
        int width = matcher.end() - matcher.start();

        String prefixTemplate = pattern.substring(0, matcher.start());
        String suffixTemplate = pattern.substring(matcher.end());
        String prefix = renderTemplate(prefixTemplate, effectiveDate, values);
        String suffix = renderTemplate(suffixTemplate, effectiveDate, values);

        Pattern numberPattern = Pattern.compile(
                "^" + Pattern.quote(prefix) + "(\\d{" + width + "})" + Pattern.quote(suffix) + "$"
        );
        return new CompiledPattern(pattern, prefix, suffix, width, numberPattern);
    }

    /** @return null nếu hợp lệ, ngược lại là thông báo lỗi cho người dùng. */
    public static String validate(String pattern) {
        if (isBlank(pattern)) {
            return "Mẫu số phiếu không được để trống.";
        }

        int balance = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (ch == '(') balance++;
            if (ch == ')' && --balance < 0) {
                return "Dấu ngoặc trong mẫu số phiếu không hợp lệ.";
            }
        }
        if (balance != 0) {
            return "Dấu ngoặc trong mẫu số phiếu không hợp lệ.";
        }

        Matcher matcher = STAR_RUN.matcher(pattern);
        if (!matcher.find()) {
            return "Mẫu phải có một nhóm dấu sao (*) để sinh số tự tăng.";
        }
        int width = matcher.end() - matcher.start();
        if (width > 9) {
            return "Nhóm số tự tăng chỉ được có tối đa 9 dấu sao.";
        }
        if (matcher.find()) {
            return "Mẫu chỉ được có một nhóm dấu sao liên tiếp.";
        }
        return null;
    }

    private static String renderTemplate(String template, Date date, Map<String, String> values) {
        String rendered = template
                .replace("(yyyy)", new SimpleDateFormat("yyyy").format(date))
                .replace("(yy)", new SimpleDateFormat("yy").format(date))
                .replace("(MM)", new SimpleDateFormat("MM").format(date))
                .replace("(dd)", new SimpleDateFormat("dd").format(date));

        String storeCode = values != null ? values.get("CH") : null;
        rendered = rendered.replace("(CH)", storeCode != null ? storeCode.trim() : "");

        // Theo cú pháp của ứng dụng gốc, ngoặc chỉ dùng để đánh dấu một nhóm.
        return rendered.replace("(", "").replace(")", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class CompiledPattern {
        private final String sourcePattern;
        private final String prefix;
        private final String suffix;
        private final int sequenceWidth;
        private final Pattern numberPattern;

        private CompiledPattern(String sourcePattern, String prefix, String suffix,
                                int sequenceWidth, Pattern numberPattern) {
            this.sourcePattern = sourcePattern;
            this.prefix = prefix;
            this.suffix = suffix;
            this.sequenceWidth = sequenceWidth;
            this.numberPattern = numberPattern;
        }

        public String getSourcePattern() {
            return sourcePattern;
        }

        public String getSqlLikePattern() {
            return prefix + "%";
        }

        public long extractSequence(String voucherNumber) {
            if (voucherNumber == null) return -1;
            Matcher matcher = numberPattern.matcher(voucherNumber.trim());
            if (!matcher.matches()) return -1;
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }

        public String format(long sequence) {
            long max = 1;
            for (int i = 0; i < sequenceWidth; i++) max *= 10;
            max--;
            if (sequence < 1 || sequence > max) {
                throw new IllegalStateException("Dãy số phiếu đã vượt quá " + sequenceWidth + " chữ số.");
            }
            return prefix + String.format("%0" + sequenceWidth + "d", sequence) + suffix;
        }

        public String next(Iterable<String> existingVoucherNumbers) {
            long max = 0;
            if (existingVoucherNumbers != null) {
                for (String value : existingVoucherNumbers) {
                    max = Math.max(max, extractSequence(value));
                }
            }
            return format(max + 1);
        }
    }
}
