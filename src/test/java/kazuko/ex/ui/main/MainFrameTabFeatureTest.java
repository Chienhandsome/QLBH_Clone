package kazuko.ex.ui.main;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MainFrameTabFeatureTest {

    @Test
    void closeButtonRemovesOnlyItsOwnTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTabbedPane tabs = new JTabbedPane();
            JPanel firstContent = new JPanel();
            JPanel secondContent = new JPanel();

            tabs.addTab("Tab một", firstContent);
            MainFrame.CloseableTabComponent firstHeader =
                    new MainFrame.CloseableTabComponent(tabs, firstContent, "Tab một");
            tabs.setTabComponentAt(0, firstHeader);

            tabs.addTab("Tab hai", secondContent);
            MainFrame.CloseableTabComponent secondHeader =
                    new MainFrame.CloseableTabComponent(tabs, secondContent, "Tab hai");
            tabs.setTabComponentAt(1, secondHeader);

            JButton secondCloseButton = (JButton) secondHeader.getComponent(1);
            secondCloseButton.doClick();

            assertEquals(1, tabs.getTabCount());
            assertSame(firstContent, tabs.getComponentAt(0));
        });
    }
}
