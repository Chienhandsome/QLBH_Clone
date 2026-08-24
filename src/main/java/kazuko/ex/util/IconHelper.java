package kazuko.ex.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconHelper {
    private static final Map<String, ImageIcon> iconCache = new ConcurrentHashMap<>();

    public static ImageIcon getIcon(String resourceName) {
        return getIcon(resourceName, -1, -1);
    }

    public static ImageIcon getIcon(String resourceName, int width, int height) {
        if (resourceName == null || resourceName.trim().isEmpty()) return null;
        String trimmed = resourceName.trim();
        String key = trimmed + "_" + width + "x" + height;
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }

        String path = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        URL url = IconHelper.class.getResource(path);
        if (url == null) {
            return null;
        }

        ImageIcon icon = new ImageIcon(url);
        if (width > 0 && height > 0 && (icon.getIconWidth() != width || icon.getIconHeight() != height)) {
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        }
        iconCache.put(key, icon);
        return icon;
    }
}
