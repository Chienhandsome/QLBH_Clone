package kazuko.ex.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/** Stores and validates the local workstation license used by the Help dialogs. */
public class LicenseService {
    public static final int TRIAL_DAYS = 20;
    private static final String KEY_SEED = "QLBH-CLONE-LICENSE-V1";
    private final Path licenseFile;

    public LicenseService() {
        this(Path.of("license.properties"));
    }

    public LicenseService(Path licenseFile) {
        this.licenseFile = licenseFile;
    }

    public LicenseStatus getStatus() {
        Properties properties = loadProperties();
        LocalDate trialStartedOn = parseDate(properties.getProperty("trial.startedOn"), LocalDate.now());
        if (!properties.containsKey("trial.startedOn")) {
            properties.setProperty("trial.startedOn", trialStartedOn.toString());
            savePropertiesQuietly(properties);
        }

        String machineId = getMachineId();
        String registrationKey = properties.getProperty("registration.key", "").trim();
        boolean registered = machineId.equals(properties.getProperty("registration.machineId", ""))
                && isValidRegistrationKey(machineId, registrationKey);
        long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(trialStartedOn, LocalDate.now()));
        int remainingDays = Math.max(0, TRIAL_DAYS - (int) elapsedDays);

        return new LicenseStatus(
                registered,
                properties.getProperty("registration.owner", ""),
                properties.getProperty("registration.organization", ""),
                machineId,
                registrationKey,
                remainingDays
        );
    }

    public RegistrationResult register(String owner, String organization, String registrationKey) {
        String normalizedOwner = owner == null ? "" : owner.trim();
        String normalizedOrganization = organization == null ? "" : organization.trim();
        String normalizedKey = normalizeKey(registrationKey);
        if (normalizedOwner.isEmpty()) {
            return new RegistrationResult(false, "Vui lòng nhập họ và tên.");
        }
        if (normalizedOrganization.isEmpty()) {
            return new RegistrationResult(false, "Vui lòng nhập đơn vị.");
        }
        String machineId = getMachineId();
        if (!isValidRegistrationKey(machineId, normalizedKey)) {
            return new RegistrationResult(false, "Mã đăng ký không đúng với máy này.");
        }

        Properties properties = loadProperties();
        properties.putIfAbsent("trial.startedOn", LocalDate.now().toString());
        properties.setProperty("registration.owner", normalizedOwner);
        properties.setProperty("registration.organization", normalizedOrganization);
        properties.setProperty("registration.machineId", machineId);
        properties.setProperty("registration.key", normalizedKey);
        properties.setProperty("registration.registeredOn", LocalDate.now().toString());
        try {
            saveProperties(properties);
            return new RegistrationResult(true, "Đăng ký bản quyền thành công.");
        } catch (IOException ex) {
            return new RegistrationResult(false, "Không thể lưu thông tin đăng ký: " + ex.getMessage());
        }
    }

    public String getMachineId() {
        StringBuilder source = new StringBuilder();
        source.append(System.getenv().getOrDefault("COMPUTERNAME", "unknown"));
        source.append('|').append(System.getProperty("os.name", ""));
        source.append('|').append(System.getProperty("os.arch", ""));
        source.append('|').append(System.getProperty("user.name", ""));
        try {
            List<String> hardwareAddresses = new ArrayList<>();
            for (NetworkInterface network : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null && mac.length > 0 && !network.isLoopback()) {
                    StringBuilder address = new StringBuilder();
                    for (byte value : mac) {
                        address.append(String.format(Locale.ROOT, "%02X", value));
                    }
                    hardwareAddresses.add(address.toString());
                }
            }
            Collections.sort(hardwareAddresses);
            hardwareAddresses.forEach(address -> source.append('|').append(address));
        } catch (Exception ignored) {}

        byte[] digest = sha256(source.toString());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int value = ((digest[i * 2] & 0xff) << 8) | (digest[i * 2 + 1] & 0xff);
            if (i > 0) result.append('-');
            result.append(String.format(Locale.ROOT, "%05d", value));
        }
        return result.toString();
    }

    public boolean isValidRegistrationKey(String machineId, String registrationKey) {
        return expectedKeyForMachine(machineId).equals(normalizeKey(registrationKey));
    }

    public static String expectedKeyForMachine(String machineId) {
        byte[] digest = sha256(KEY_SEED + '|' + machineId);
        StringBuilder raw = new StringBuilder("QLBH");
        for (int i = 0; i < 10; i++) {
            raw.append(String.format(Locale.ROOT, "%02X", digest[i]));
        }
        String value = raw.toString();
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < value.length(); i += 4) {
            if (i > 0) formatted.append('-');
            formatted.append(value, i, Math.min(i + 4, value.length()));
        }
        return formatted.toString();
    }

    private static String normalizeKey(String key) {
        if (key == null) return "";
        String compact = key.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < compact.length(); i += 4) {
            if (i > 0) formatted.append('-');
            formatted.append(compact, i, Math.min(i + 4, compact.length()));
        }
        return formatted.toString();
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        if (Files.isRegularFile(licenseFile)) {
            try (InputStream input = Files.newInputStream(licenseFile)) {
                properties.load(input);
            } catch (IOException ignored) {}
        }
        return properties;
    }

    private void savePropertiesQuietly(Properties properties) {
        try {
            saveProperties(properties);
        } catch (IOException ignored) {}
    }

    private void saveProperties(Properties properties) throws IOException {
        Path absoluteFile = licenseFile.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path tempFile = Files.createTempFile(parent, "license-", ".tmp");
        try (OutputStream output = Files.newOutputStream(tempFile)) {
            properties.store(output, "QLBH local license");
        }
        try {
            Files.move(tempFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Files.move(tempFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static LocalDate parseDate(String value, LocalDate fallback) {
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo mã nhận dạng máy", ex);
        }
    }

    public record LicenseStatus(boolean registered, String owner, String organization,
                                String machineId, String registrationKey, int remainingTrialDays) {}

    public record RegistrationResult(boolean success, String message) {}
}
