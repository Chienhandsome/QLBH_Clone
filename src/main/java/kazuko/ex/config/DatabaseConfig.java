package kazuko.ex.config;

import java.io.*;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "db_config.properties";

    private String serverIp = "localhost";
    private int port = 3050;
    private String databasePath = "D:/THUCTAP/QLBH_Clone/data/DEMO.FDB";
    private String username = "SYSDBA";
    private String password = "masterkey";
    private String charset = "UTF8";

    public DatabaseConfig() {
        loadConfig();
    }

    public void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                Properties prop = new Properties();
                prop.load(in);
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
    }

    public void saveConfig() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.setProperty("db.serverIp", this.serverIp);
            prop.setProperty("db.port", String.valueOf(this.port));
            prop.setProperty("db.databasePath", this.databasePath);
            prop.setProperty("db.username", this.username);
            prop.setProperty("db.password", this.password);
            prop.setProperty("db.charset", this.charset);
            prop.store(out, "Thanh Tools DB Config");
        } catch (Exception e) {
            System.err.println("Lỗi lưu cấu hình: " + e.getMessage());
        }
    }

    public String getJdbcUrl() {
        return "jdbc:firebirdsql://" + serverIp + ":" + port + "/" + databasePath + "?charSet=" + charset;
    }

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
