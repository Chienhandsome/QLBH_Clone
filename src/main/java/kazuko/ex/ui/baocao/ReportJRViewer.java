package kazuko.ex.ui.baocao;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerController;
import net.sf.jasperreports.swing.JRViewerEvent;
import net.sf.jasperreports.swing.JRViewerToolbar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** JRViewer với toolbar đồng nhất theo giao diện báo cáo của phần mềm POS. */
public final class ReportJRViewer extends JRViewer {

    public ReportJRViewer(JasperPrint jasperPrint) {
        super(jasperPrint);
        setBackground(new Color(74, 74, 74));
    }

    @Override
    protected JRViewerToolbar createToolbar() {
        return new PosReportToolbar(viewerContext);
    }

    private static final class PosReportToolbar extends JRViewerToolbar {
        private final JLabel pageCountLabel = new JLabel("/0");

        private PosReportToolbar(JRViewerController viewerContext) {
            super(viewerContext);
            rebuildToolbar();
        }

        private void rebuildToolbar() {
            removeAll();
            setLayout(new BorderLayout());
            setBackground(new Color(157, 187, 217));
            setBorder(new EmptyBorder(8, 10, 8, 8));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.setOpaque(false);

            stylePrimary(btnPrint, "IN");
            stylePrimary(btnSave, "XUẤT");
            styleNavigation(btnPrevious);
            styleNavigation(btnNext);
            styleNavigation(btnZoomOut);
            styleNavigation(btnZoomIn);

            txtGoTo.setHorizontalAlignment(SwingConstants.CENTER);
            txtGoTo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            txtGoTo.setPreferredSize(new Dimension(68, 50));
            pageCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            pageCountLabel.setForeground(Color.BLACK);

            actions.add(btnPrint);
            actions.add(btnSave);
            actions.add(btnPrevious);
            actions.add(txtGoTo);
            actions.add(pageCountLabel);
            actions.add(btnNext);
            actions.add(btnZoomOut);
            actions.add(btnZoomIn);
            add(actions, BorderLayout.WEST);

            JButton close = new JButton("THOÁT");
            close.setBackground(new Color(103, 0, 0));
            close.setForeground(Color.WHITE);
            close.setFont(new Font("Segoe UI", Font.BOLD, 20));
            close.setFocusPainted(false);
            close.setPreferredSize(new Dimension(140, 50));
            close.addActionListener(e -> {
                ReportScreenPanel screen = (ReportScreenPanel) SwingUtilities.getAncestorOfClass(
                        ReportScreenPanel.class, PosReportToolbar.this);
                if (screen != null) screen.closeReport();
            });
            add(close, BorderLayout.EAST);
            updatePageCount();
        }

        private void stylePrimary(AbstractButton button, String text) {
            button.setText(text);
            button.setBackground(new Color(91, 0, 91));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 20));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(105, 50));
        }

        private void styleNavigation(AbstractButton button) {
            button.setBackground(new Color(91, 91, 0));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(72, 50));
        }

        private void updatePageCount() {
            if (pageCountLabel != null) {
                int pageCount = viewerContext.getJasperPrint() == null
                        ? 0
                        : Math.max(0, viewerContext.getPageCount());
                pageCountLabel.setText("/" + pageCount);
            }
        }

        @Override
        protected void reportLoaded() {
            super.reportLoaded();
            updatePageCount();
        }

        @Override
        protected void pageChanged() {
            super.pageChanged();
            updatePageCount();
        }

        @Override
        public void viewerEvent(JRViewerEvent event) {
            super.viewerEvent(event);
            updatePageCount();
        }
    }
}
