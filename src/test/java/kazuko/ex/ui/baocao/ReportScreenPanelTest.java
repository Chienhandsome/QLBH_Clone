package kazuko.ex.ui.baocao;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportScreenPanelTest {

    @Test
    void filterToggleOnlyChangesVisibility() throws Exception {
        AtomicInteger loadCount = new AtomicInteger();
        ReportScreenPanel[] holder = new ReportScreenPanel[1];

        SwingUtilities.invokeAndWait(() -> {
            JPanel legacy = new JPanel(new BorderLayout());
            JPanel filter = new JPanel(new FlowLayout());
            filter.add(new JLabel("Ngày"));
            filter.add(new JTextField("01/09/2026"));
            JButton load = new JButton("Tải dữ liệu (F5)");
            load.addActionListener(e -> loadCount.incrementAndGet());
            filter.add(load);
            legacy.add(filter, BorderLayout.NORTH);
            legacy.add(new JPanel(), BorderLayout.CENTER);

            holder[0] = new ReportScreenPanel("BÁO CÁO KIỂM THỬ", legacy);
            JButton toggle = findButton(holder[0], "LỌC  THAM SỐ");
            assertTrue(holder[0].isFilterVisible());
            toggle.doClick();
        });

        assertFalse(holder[0].isFilterVisible());
        assertEquals(0, loadCount.get(), "Ẩn bộ lọc không được tải lại báo cáo");

        SwingUtilities.invokeAndWait(() -> findButton(holder[0], "LỌC  THAM SỐ").doClick());
        assertTrue(holder[0].isFilterVisible());
        assertEquals(0, loadCount.get(), "Hiện bộ lọc không được tải lại báo cáo");
    }

    @Test
    void closeReportRemovesOnlyContainingTab() throws Exception {
        JTabbedPane[] tabs = new JTabbedPane[1];
        ReportScreenPanel[] screen = new ReportScreenPanel[1];

        SwingUtilities.invokeAndWait(() -> {
            tabs[0] = new JTabbedPane();
            screen[0] = new ReportScreenPanel("BÁO CÁO", new JPanel(new BorderLayout()));
            tabs[0].addTab("Khác", new JPanel());
            tabs[0].addTab("Báo cáo", screen[0]);
            screen[0].closeReport();
        });

        assertEquals(1, tabs[0].getTabCount());
        assertEquals("Khác", tabs[0].getTitleAt(0));
    }

    @Test
    void legacyParameterChangesDoNotAutoReloadAfterWrapping() throws Exception {
        LegacyAutoReloadPanel[] legacy = new LegacyAutoReloadPanel[1];

        SwingUtilities.invokeAndWait(() -> {
            legacy[0] = new LegacyAutoReloadPanel();
            new ReportScreenPanel("BÁO CÁO", legacy[0]);
            legacy[0].spinner.setValue(2027);
        });

        assertEquals(0, legacy[0].automaticLoads.get());
    }

    private static JButton findButton(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JButton button && text.equals(button.getText())) return button;
            if (child instanceof Container container) {
                try {
                    return findButton(container, text);
                } catch (AssertionError ignored) {
                    // continue in the next branch
                }
            }
        }
        throw new AssertionError("Không tìm thấy nút: " + text);
    }

    private static final class LegacyAutoReloadPanel extends JPanel {
        private boolean isInitializing;
        private final AtomicInteger automaticLoads = new AtomicInteger();
        private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(2026, 2000, 2100, 1));

        private LegacyAutoReloadPanel() {
            super(new BorderLayout());
            JPanel filter = new JPanel();
            filter.add(spinner);
            add(filter, BorderLayout.NORTH);
            add(new JPanel(), BorderLayout.CENTER);
            spinner.addChangeListener(e -> {
                if (!isInitializing) automaticLoads.incrementAndGet();
            });
        }
    }
}
