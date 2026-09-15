package kazuko.ex.ui.touch;

import javax.swing.*;
import java.awt.*;

/**
 * Panel hỗ trợ cuộn mượt mà và co giãn theo chiều rộng cho các lưới danh mục cảm ứng.
 */
public class TouchScrollableRowsPanel extends JPanel implements Scrollable {

    public TouchScrollableRowsPanel() {
        super();
    }

    public TouchScrollableRowsPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 45;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 150;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
