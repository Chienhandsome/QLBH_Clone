package kazuko.ex.config;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AppThemeDataSurfaceTest {

    @Test
    void dataComponentsStayLightWithoutChangingOuterPanelColor() {
        String[] keys = {
                "Table.background", "Table.foreground", "TableHeader.background",
                "Tree.background", "List.background", "TextField.background",
                "ComboBox.background", "Viewport.background", "Panel.background"
        };
        Map<String, Object> previous = new LinkedHashMap<>();
        for (String key : keys) previous.put(key, UIManager.get(key));

        Color outerMarker = new Color(44, 44, 47);
        UIManager.put("Panel.background", outerMarker);
        try {
            AppTheme.applyLightDataSurfaceDefaults();

            assertEquals(Color.WHITE, UIManager.getColor("Table.background"));
            assertEquals(Color.WHITE, UIManager.getColor("Tree.background"));
            assertEquals(Color.WHITE, UIManager.getColor("List.background"));
            assertEquals(Color.WHITE, UIManager.getColor("TextField.background"));
            assertEquals(Color.WHITE, UIManager.getColor("ComboBox.background"));
            assertEquals(Color.WHITE, UIManager.getColor("Viewport.background"));
            assertEquals(outerMarker, UIManager.getColor("Panel.background"));
            assertNotEquals(UIManager.getColor("Table.background"),
                    UIManager.getColor("Panel.background"));
        } finally {
            for (Map.Entry<String, Object> entry : previous.entrySet()) {
                if (entry.getValue() == null) UIManager.getDefaults().remove(entry.getKey());
                else UIManager.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
