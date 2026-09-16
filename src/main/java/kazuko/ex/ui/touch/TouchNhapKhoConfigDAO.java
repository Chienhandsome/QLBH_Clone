package kazuko.ex.ui.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kazuko.ex.dao.SystemConfigDAO;
import java.io.*;
import java.nio.charset.StandardCharsets;

final class TouchNhapKhoConfigDAO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TouchNhapKhoColumnConfig cache;
    private static File file() { File parent = SystemConfigDAO.getConfigFile().getParentFile(); File data = new File(parent == null ? new File(".") : parent, "data"); if (!data.exists()) data.mkdirs(); return new File(data, "touch_nhapkho_columns.json"); }
    static synchronized TouchNhapKhoColumnConfig load() { if (cache != null) return cache.clone(); if (file().isFile()) try (Reader r = new InputStreamReader(new FileInputStream(file()), StandardCharsets.UTF_8)) { TouchNhapKhoColumnConfig c = GSON.fromJson(r, TouchNhapKhoColumnConfig.class); if (c != null && c.getUsedColumns() != null && !c.getUsedColumns().isEmpty()) { cache = c; return c.clone(); } } catch (Exception e) { System.err.println("Không thể đọc cấu hình nhập kho: " + e.getMessage()); } cache = TouchNhapKhoColumnConfig.createDefault(); save(cache); return cache.clone(); }
    static synchronized boolean save(TouchNhapKhoColumnConfig c) { try (Writer w = new OutputStreamWriter(new FileOutputStream(file()), StandardCharsets.UTF_8)) { GSON.toJson(c, w); cache = c.clone(); return true; } catch (Exception e) { System.err.println("Không thể lưu cấu hình nhập kho: " + e.getMessage()); return false; } }
}
