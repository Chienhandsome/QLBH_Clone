package kazuko.ex.ui.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kazuko.ex.dao.SystemConfigDAO;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * DAO lưu trữ cấu hình hiển thị cột thẻ mặt hàng bán hàng cảm ứng dạng JSON.
 */
public class TouchBanHangProductConfigDAO {
    private static final String CONFIG_FILE_NAME = "touch_banhang_product_columns.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TouchBanHangProductColumnConfig cachedConfig = null;

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

    public static synchronized TouchBanHangProductColumnConfig loadConfig() {
        if (cachedConfig != null) {
            return cachedConfig.clone();
        }

        File file = getConfigFile();
        if (file.exists() && file.isFile()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                TouchBanHangProductColumnConfig config = GSON.fromJson(reader, TouchBanHangProductColumnConfig.class);
                if (config != null && config.getUsedColumns() != null && !config.getUsedColumns().isEmpty()) {
                    cachedConfig = config;
                    return config.clone();
                }
            } catch (Exception e) {
                System.err.println("Lỗi đọc cấu hình cột bán hàng: " + e.getMessage());
            }
        }

        TouchBanHangProductColumnConfig def = TouchBanHangProductColumnConfig.createDefault();
        cachedConfig = def;
        saveConfig(def);
        return def.clone();
    }

    public static synchronized boolean saveConfig(TouchBanHangProductColumnConfig config) {
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
            System.err.println("Lỗi lưu cấu hình cột bán hàng: " + e.getMessage());
            return false;
        }
    }
}
