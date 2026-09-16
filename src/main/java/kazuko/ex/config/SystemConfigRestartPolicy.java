package kazuko.ex.config;

import java.util.Objects;

/** Xác định thay đổi cấu hình nào cần khởi động lại ứng dụng. */
public final class SystemConfigRestartPolicy {
    private SystemConfigRestartPolicy() {}

    public static boolean requiresRestart(String previousFontSize,
                                          String previousSystemTheme,
                                          String nextFontSize,
                                          String nextSystemTheme) {
        return !Objects.equals(normalize(previousFontSize), normalize(nextFontSize))
                || !Objects.equals(normalize(previousSystemTheme), normalize(nextSystemTheme));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
