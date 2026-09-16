package kazuko.ex.ui.touch;

import javax.swing.*;
import java.awt.*;

/**
 * Panel hỗ trợ cuộn mượt mà và co giãn theo chiều rộng cho các lưới danh mục cảm ứng.
 */
public class TouchScrollableRowsPanel extends JPanel implements Scrollable {

    private int gridColumns = 1;
    private int visibleRows = 1;
    private int verticalGap;

    public TouchScrollableRowsPanel() {
        super();
    }

    public TouchScrollableRowsPanel(LayoutManager layout) {
        super(layout);
        if (layout instanceof GridLayout grid) {
            gridColumns = Math.max(1, grid.getColumns());
            verticalGap = grid.getVgap();
        }
    }

    /**
     * Configures the number of columns and rows that must fit in the viewport.
     * Additional rows remain available through vertical scrolling.
     */
    public void configureGrid(int columns, int rows, int hgap, int vgap) {
        gridColumns = Math.max(1, columns);
        visibleRows = Math.max(1, rows);
        verticalGap = Math.max(0, vgap);
        setLayout(new GridLayout(0, gridColumns, Math.max(0, hgap), verticalGap));
    }

    /**
     * Completes the last row and reserves transparent cells up to the configured
     * viewport capacity. This keeps a short result set from stretching its cards.
     */
    public void completeGrid() {
        int minimumCells = gridColumns * visibleRows;
        int requiredCells = Math.max(minimumCells,
                ((getComponentCount() + gridColumns - 1) / gridColumns) * gridColumns);
        while (getComponentCount() < requiredCells) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            add(filler);
        }
    }

    int getConfiguredColumns() {
        return gridColumns;
    }

    int getConfiguredVisibleRows() {
        return visibleRows;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        if (!(getLayout() instanceof GridLayout) || getComponentCount() == 0) {
            return preferred;
        }

        int actualRows = Math.max(visibleRows,
                (getComponentCount() + gridColumns - 1) / gridColumns);
        int viewportHeight = getParent() instanceof JViewport viewport
                ? viewport.getExtentSize().height : 0;
        if (viewportHeight <= 0) {
            return preferred;
        }

        Insets insets = getInsets();
        int viewportInnerHeight = Math.max(1,
                viewportHeight - insets.top - insets.bottom);
        int preferredInnerHeight = (int) Math.round(
                (viewportInnerHeight + verticalGap) * (actualRows / (double) visibleRows)
                        - verticalGap);
        int preferredHeight = insets.top + insets.bottom
                + Math.max(1, preferredInnerHeight);
        return new Dimension(preferred.width, preferredHeight);
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
