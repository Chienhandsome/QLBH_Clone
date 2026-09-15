package kazuko.ex.ui.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kazuko.ex.dao.SystemConfigDAO;

import java.io.*;
import java.nio.charset.StandardCharsets;

final class TouchLuuVetConfigDAO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TouchLuuVetColumnConfig cache;

    private static File file() {
        File parent = SystemConfigDAO.getConfigFile().getParentFile();
        File data = new File(parent == null ? new File(".") : parent, "data");
        if (!data.exists()) data.mkdirs();
        return new File(data, "touch_luuvet_columns.json");
    }

    static synchronized TouchLuuVetColumnConfig load() {
        if (cache != null) return cache.clone();
        File file = file();
        if (file.isFile()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                TouchLuuVetColumnConfig value = GSON.fromJson(reader, TouchLuuVetColumnConfig.class);
                if (value != null && value.getUsedColumns() != null && !value.getUsedColumns().isEmpty()) {
                    cache = value; return value.clone();
                }
            } catch (Exception e) { System.err.println("Không thể đọc cấu hình lưu vết: " + e.getMessage()); }
        }
        cache = TouchLuuVetColumnConfig.createDefault();
        save(cache);
        return cache.clone();
    }

    static synchronized boolean save(TouchLuuVetColumnConfig value) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file()), StandardCharsets.UTF_8)) {
            GSON.toJson(value, writer); cache = value.clone(); return true;
        } catch (Exception e) { System.err.println("Không thể lưu cấu hình lưu vết: " + e.getMessage()); return false; }
    }
}
