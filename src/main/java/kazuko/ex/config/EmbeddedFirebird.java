package kazuko.ex.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Locates the bundled Firebird Embedded runtime and prepares the per-user
 * database. The installed application directory is read-only, so the seed
 * database is copied to LOCALAPPDATA the first time the application starts.
 */
public final class EmbeddedFirebird {
    private static final String APP_NAME = "QLBH Clone";
    private static final String DATABASE_NAME = "DEMO.FDB";
    private static final String EMBEDDED_LIBRARY = "fbembed.dll";

    private EmbeddedFirebird() {
    }

    public static Path getApplicationDirectory() {
        try {
            Path location = Path.of(
                    EmbeddedFirebird.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).toAbsolutePath().normalize();

            return Files.isRegularFile(location) ? location.getParent() : location;
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Không xác định được thư mục ứng dụng", e);
        }
    }

    public static Path getDataDirectory() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path baseDirectory;
        if (localAppData == null || localAppData.isBlank()) {
            baseDirectory = Path.of(System.getProperty("user.home"), ".qlbh-clone");
        } else {
            baseDirectory = Path.of(localAppData, APP_NAME);
        }
        return baseDirectory.resolve("data").toAbsolutePath().normalize();
    }

    public static Path getConfigFile() {
        return getDataDirectory().getParent().resolve("db_config.properties");
    }

    public static Path prepareDatabase() {
        Path dataDirectory = getDataDirectory();
        Path targetDatabase = dataDirectory.resolve(DATABASE_NAME);

        try {
            Files.createDirectories(dataDirectory);
            if (Files.notExists(targetDatabase)) {
                Path seedDatabase = findSeedDatabase();
                Files.copy(seedDatabase, targetDatabase, StandardCopyOption.COPY_ATTRIBUTES);
            }
            return targetDatabase;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Không thể chuẩn bị CSDL cục bộ tại " + targetDatabase, e);
        }
    }

    public static Path findFirebirdDirectory() {
        for (Path candidate : firebirdCandidates()) {
            if (Files.isRegularFile(candidate.resolve(EMBEDDED_LIBRARY))) {
                return candidate.toAbsolutePath().normalize();
            }
        }

        throw new IllegalStateException(
                "Không tìm thấy " + EMBEDDED_LIBRARY
                        + ". Hãy cài lại ứng dụng bằng bộ cài đầy đủ.");
    }

    public static void configureNativeLibrary() {
        Path firebirdDirectory = findFirebirdDirectory();
        String currentPath = System.getProperty("jna.library.path", "");
        String nativePath = firebirdDirectory.toString();
        if (!currentPath.isBlank()) {
            nativePath += System.getProperty("path.separator") + currentPath;
        }
        System.setProperty("jna.library.path", nativePath);
    }

    private static Path findSeedDatabase() throws IOException {
        Path applicationDirectory = getApplicationDirectory();
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();

        candidates.add(applicationDirectory.resolve("seed").resolve(DATABASE_NAME));
        candidates.add(applicationDirectory.resolve("data").resolve(DATABASE_NAME));
        if (applicationDirectory.getParent() != null) {
            candidates.add(applicationDirectory.getParent().resolve("seed").resolve(DATABASE_NAME));
            candidates.add(applicationDirectory.getParent().resolve("data").resolve(DATABASE_NAME));
            candidates.add(applicationDirectory.getParent().resolve("installer-input").resolve("seed").resolve(DATABASE_NAME));
            candidates.add(applicationDirectory.getParent().resolve("installer-input-embedded").resolve("seed").resolve(DATABASE_NAME));
        }
        candidates.add(workingDirectory.resolve("installer-input").resolve("seed").resolve(DATABASE_NAME));
        candidates.add(workingDirectory.resolve("installer-input-embedded").resolve("seed").resolve(DATABASE_NAME));
        candidates.add(workingDirectory.resolve("data").resolve(DATABASE_NAME));
        candidates.add(workingDirectory.resolve("QLBH_Clone").resolve("installer-input").resolve("seed").resolve(DATABASE_NAME));
        candidates.add(workingDirectory.resolve("QLBH_Clone").resolve("installer-input-embedded").resolve("seed").resolve(DATABASE_NAME));
        candidates.add(workingDirectory.resolve("QLBH_Clone").resolve("data").resolve(DATABASE_NAME));
        candidates.add(Path.of("D:/THUCTAP/QLBH_Clone/data", DATABASE_NAME));

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }

        throw new IOException("Không tìm thấy CSDL mẫu " + DATABASE_NAME);
    }

    private static List<Path> firebirdCandidates() {
        Path applicationDirectory = getApplicationDirectory();
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();

        addFirebirdCandidates(candidates, applicationDirectory.resolve("firebird"));
        if (applicationDirectory.getParent() != null) {
            addFirebirdCandidates(candidates, applicationDirectory.getParent().resolve("firebird"));
            addFirebirdCandidates(candidates, applicationDirectory.getParent().resolve("packaging").resolve("firebird"));
            addFirebirdCandidates(candidates, applicationDirectory.getParent().resolve("installer-input").resolve("firebird"));
            addFirebirdCandidates(candidates, applicationDirectory.getParent().resolve("installer-input-embedded").resolve("firebird"));
        }
        addFirebirdCandidates(candidates, workingDirectory.resolve("packaging").resolve("firebird"));
        addFirebirdCandidates(candidates, workingDirectory.resolve("installer-input").resolve("firebird"));
        addFirebirdCandidates(candidates, workingDirectory.resolve("installer-input-embedded").resolve("firebird"));
        addFirebirdCandidates(candidates, workingDirectory.resolve("firebird"));
        addFirebirdCandidates(candidates, workingDirectory.resolve("QLBH_Clone").resolve("packaging").resolve("firebird"));
        addFirebirdCandidates(candidates, workingDirectory.resolve("QLBH_Clone").resolve("installer-input").resolve("firebird"));
        addFirebirdCandidates(candidates, workingDirectory.resolve("QLBH_Clone").resolve("installer-input-embedded").resolve("firebird"));
        addFirebirdCandidates(candidates, Path.of("D:/THUCTAP/QLBH_Clone/installer-input/firebird"));
        addFirebirdCandidates(candidates, Path.of("D:/THUCTAP/QLBH_Clone/packaging/firebird"));
        return candidates;
    }

    private static void addFirebirdCandidates(List<Path> candidates, Path root) {
        if (root == null) return;
        candidates.add(root);
        candidates.add(root.resolve("Firebird-2.5.9.27139-0_x64_embed"));
    }
}
