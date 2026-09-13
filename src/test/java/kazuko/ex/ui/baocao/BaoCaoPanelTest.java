package kazuko.ex.ui.baocao;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaoCaoPanelTest {

    @Test
    void embedsSelectedReportPanelInSharedWorkspace() throws Exception {
        AtomicReference<String> selectedReport = new AtomicReference<>();
        JPanel reportContent = new JPanel();
        AtomicReference<BaoCaoPanel> screen = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            JMenu reportMenu = new JMenu("Báo cáo");
            JMenu folder = new JMenu("BÁO CÁO QUỸ");
            folder.add(new JMenuItem("DANH SÁCH PHIẾU THU THEO NGÀY"));
            reportMenu.add(folder);

            screen.set(new BaoCaoPanel(reportMenu, name -> {
                selectedReport.set(name);
                return reportContent;
            }));
        });

        assertEquals("DANH SÁCH PHIẾU THU THEO NGÀY", selectedReport.get());
        assertTrue(containsComponent(screen.get(), reportContent));
    }

    private boolean containsComponent(Container parent, Component expected) {
        for (Component child : parent.getComponents()) {
            if (child == expected) return true;
            if (child instanceof Container && containsComponent((Container) child, expected)) return true;
        }
        return false;
    }
}
