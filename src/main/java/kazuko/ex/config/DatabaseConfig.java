package kazuko.ex.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DatabaseConfig {
    private boolean embeddedMode = true;
    private String serverIp = "localhost";
    private int port = 3050;
    private String databasePath;
    private String username = "SYSDBA";
    private String password = "masterkey";
    private String charset = "UTF8";

    public DatabaseConfig() {
        this.databasePath = EmbeddedFirebird.prepareDatabase().toString();
        loadConfig();

        // The local database always belongs to the current Windows user. This
        // prevents an old path from another machine leaking into a new install.
        if (embeddedMode) {
            this.databasePath = EmbeddedFirebird.prepareDatabase().toString();
        }
    }

    public void loadConfig() {
        Path configFile = EmbeddedFirebird.getConfigFile();
        if (Files.notExists(configFile)) {
            return;
        }

        try (InputStream in = Files.newInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(in);
            this.embeddedMode = Boolean.parseBoolean(
                    prop.getProperty("db.embedded", String.valueOf(this.embeddedMode)));
            this.serverIp = prop.getProperty("db.serverIp", this.serverIp);
            this.port = Integer.parseInt(prop.getProperty("db.port", String.valueOf(this.port)));
            this.databasePath = prop.getProperty("db.databasePath", this.databasePath);
            this.username = prop.getProperty("db.username", this.username);
            this.password = prop.getProperty("db.password", this.password);
            this.charset = prop.getProperty("db.charset", this.charset);
        } catch (Exception e) {
            System.err.println("Lỗi nạp cấu hình: " + e.getMessage());
        }
    }

    public void saveConfig() {
        Path configFile = EmbeddedFirebird.getConfigFile();
        try {
            Files.createDirectories(configFile.getParent());
            try (OutputStream out = Files.newOutputStream(configFile)) {
                Properties prop = new Properties();
                prop.setProperty("db.embedded", String.valueOf(this.embeddedMode));
                prop.setProperty("db.serverIp", this.serverIp);
                prop.setProperty("db.port", String.valueOf(this.port));
                prop.setProperty("db.databasePath", this.databasePath);
                prop.setProperty("db.username", this.username);
                prop.setProperty("db.password", this.password);
                prop.setProperty("db.charset", this.charset);
                prop.store(out, "QLBH Clone DB Config");
            }
        } catch (IOException e) {
            System.err.println("Lỗi lưu cấu hình: " + e.getMessage());
        }
    }

    public String getJdbcUrl() {
        String normalizedPath = databasePath.replace("\\", "/");
        if (embeddedMode) {
            return "jdbc:firebirdsql:embedded:" + normalizedPath + "?charSet=" + charset;
        }
        return "jdbc:firebirdsql://" + serverIp + ":" + port + "/"
                + normalizedPath + "?charSet=" + charset;
    }

    public boolean isEmbeddedMode() { return embeddedMode; }
    public void setEmbeddedMode(boolean embeddedMode) { this.embeddedMode = embeddedMode; }

    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = serverIp; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabasePath() { return databasePath; }
    public void setDatabasePath(String databasePath) { this.databasePath = databasePath; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCharset() { return charset; }
    public void setCharset(String charset) { this.charset = charset; }
}
