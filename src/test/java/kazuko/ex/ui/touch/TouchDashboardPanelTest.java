package kazuko.ex.ui.touch;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TouchDashboardPanelTest {

    @Test
    void reportMainMenuShowsReportGroupsInsteadOfOpeningUnknownModule() throws Exception {
        List<String> openedModules = new ArrayList<>();
        TouchDashboardPanel[] dashboard = new TouchDashboardPanel[1];

        SwingUtilities.invokeAndWait(() -> {
            dashboard[0] = new TouchDashboardPanel(null, () -> {}, () -> {}, openedModules::add);
            JButton reportMenu = findButton(dashboard[0], "BÁO CÁO");
            assertNotNull(reportMenu);
            reportMenu.doClick();
        });

        assertEquals(List.of(), openedModules);
        assertNotNull(findButton(dashboard[0], "BÁO CÁO QUỸ"));
        assertNotNull(findButton(dashboard[0], "BÁO CÁO BIỂU ĐỒ"));
    }

    private static JButton findButton(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container container) {
                JButton found = findButton(container, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
