package kazuko.ex.ui.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kazuko.ex.dao.SystemConfigDAO;

import java.io.*;
import java.nio.charset.StandardCharsets;

final class TouchCongNoKhachHangConfigDAO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TouchCongNoKhachHangColumnConfig cache;

    private static File file() {
        File parent = SystemConfigDAO.getConfigFile().getParentFile();
        File data = new File(parent == null ? new File(".") : parent, "data");
        if (!data.exists()) data.mkdirs();
        return new File(data, "touch_congnokhachhang_columns.json");
    }

    static synchronized TouchCongNoKhachHangColumnConfig load() {
        if (cache != null) return cache.clone();
        if (file().isFile()) {
            try (Reader r = new InputStreamReader(new FileInputStream(file()), StandardCharsets.UTF_8)) {
                TouchCongNoKhachHangColumnConfig c = GSON.fromJson(r, TouchCongNoKhachHangColumnConfig.class);
                if (c != null && c.getUsedColumns() != null && !c.getUsedColumns().isEmpty()) {
                    cache = c;
                    return c.clone();
                }
            } catch (Exception e) {
                System.err.println("Không thể đọc cấu hình công nợ khách hàng: " + e.getMessage());
            }
        }
        cache = TouchCongNoKhachHangColumnConfig.createDefault();
        save(cache);
        return cache.clone();
    }

    static synchronized boolean save(TouchCongNoKhachHangColumnConfig c) {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file()), StandardCharsets.UTF_8)) {
            GSON.toJson(c, w);
            cache = c.clone();
            return true;
        } catch (Exception e) {
            System.err.println("Không thể lưu cấu hình công nợ khách hàng: " + e.getMessage());
            return false;
        }
    }
}
