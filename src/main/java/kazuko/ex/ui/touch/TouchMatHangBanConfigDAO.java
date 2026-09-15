package kazuko.ex.ui.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kazuko.ex.dao.SystemConfigDAO;

import java.io.*;
import java.nio.charset.StandardCharsets;

final class TouchMatHangBanConfigDAO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TouchMatHangBanColumnConfig cache;
    private static File file() { File parent = SystemConfigDAO.getConfigFile().getParentFile(); File data = new File(parent == null ? new File(".") : parent, "data"); if (!data.exists()) data.mkdirs(); return new File(data, "touch_mat_hang_ban_columns.json"); }
    static synchronized TouchMatHangBanColumnConfig load() { if (cache != null) return cache.clone(); File f = file(); if (f.isFile()) try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) { TouchMatHangBanColumnConfig c = GSON.fromJson(r, TouchMatHangBanColumnConfig.class); if (c != null && c.getUsed() != null && !c.getUsed().isEmpty() && c.getUnused() != null) { cache = c; return c.clone(); } } catch (Exception e) { System.err.println("Không thể đọc cấu hình mặt hàng bán: " + e.getMessage()); } cache = TouchMatHangBanColumnConfig.defaults(); save(cache); return cache.clone(); }
    static synchronized boolean save(TouchMatHangBanColumnConfig c) { try (Writer w = new OutputStreamWriter(new FileOutputStream(file()), StandardCharsets.UTF_8)) { GSON.toJson(c, w); cache = c.clone(); return true; } catch (Exception e) { System.err.println("Không thể lưu cấu hình mặt hàng bán: " + e.getMessage()); return false; } }
}
