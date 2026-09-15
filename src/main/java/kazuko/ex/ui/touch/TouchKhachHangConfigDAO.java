package kazuko.ex.ui.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kazuko.ex.dao.SystemConfigDAO;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** DAO quản lý lưu và đọc cấu hình cột khách hàng cảm ứng dạng JSON. */
public class TouchKhachHangConfigDAO {
    private static final String CONFIG_FILE_NAME = "touch_khachhang_columns.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TouchKhachHangColumnConfig cachedConfig = null;

    public static File getConfigFile() {
        File cfgDir = SystemConfigDAO.getConfigFile().getParentFile();
        if (cfgDir != null) {
            File dataDir = new File(cfgDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            return new File(dataDir, CONFIG_FILE_NAME);
        }
        File f1 = new File("data", CONFIG_FILE_NAME);
        if (f1.getParentFile() != null && !f1.getParentFile().exists()) f1.getParentFile().mkdirs();
        return f1;
    }

    public static synchronized TouchKhachHangColumnConfig loadConfig() {
        if (cachedConfig != null) {
            return cachedConfig.clone();
        }

        File file = getConfigFile();
        if (file.exists() && file.isFile()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                TouchKhachHangColumnConfig config = GSON.fromJson(reader, TouchKhachHangColumnConfig.class);
                if (config != null && config.getUsedColumns() != null && !config.getUsedColumns().isEmpty()) {
                    cachedConfig = config;
                    return config.clone();
                }
            } catch (Exception e) {
                System.err.println("Lỗi đọc cấu hình cột khách hàng cảm ứng: " + e.getMessage());
            }
        }

        TouchKhachHangColumnConfig def = TouchKhachHangColumnConfig.createDefault();
        cachedConfig = def;
        saveConfig(def);
        return def.clone();
    }

    public static synchronized boolean saveConfig(TouchKhachHangColumnConfig config) {
        if (config == null) return false;
        File file = getConfigFile();
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
            cachedConfig = config.clone();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi lưu cấu hình cột khách hàng cảm ứng: " + e.getMessage());
            return false;
        }
    }
}
