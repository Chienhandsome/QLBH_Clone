package kazuko.ex.ui.baocao;

import kazuko.ex.report.service.BaoCaoKhuyenMaiReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class BaoCaoDotKhuyenMaiPanel extends JPanel {

    private final BaoCaoKhuyenMaiReportService reportService = new BaoCaoKhuyenMaiReportService();
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    public BaoCaoDotKhuyenMaiPanel() {
        initComponents();
        loadReport();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 245, 252));

        // 1. Top Filter Panel
        JPanel pnlTop = createFilterPanel();
        add(pnlTop, BorderLayout.NORTH);

        // 2. Center Report Viewer Container
        pnlViewerContainer = new JPanel(new BorderLayout());
        pnlViewerContainer.setBackground(new Color(215, 228, 242));

        currentViewer = new JRViewer(null);
        pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);

        add(pnlViewerContainer, BorderLayout.CENTER);

        // F5 hotkey to refresh
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refreshReport");
        getActionMap().put("refreshReport", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadReport();
            }
        });
    }

    private JPanel createFilterPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnl.setBackground(new Color(245, 248, 253));
        pnl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));

        btnTaiDuLieu = new JButton("Tải dữ liệu (F5)");
        btnTaiDuLieu.setIcon(IconHelper.getIcon("refresh_16.png", 16, 16));
        btnTaiDuLieu.setBackground(new Color(220, 235, 252));
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        return pnl;
    }

    private void loadReport() {
        btnTaiDuLieu.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateDotKhuyenMai();
            }

            @Override
            protected void done() {
                try {
                    JasperPrint print = get();
                    updateReportViewer(print);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BaoCaoDotKhuyenMaiPanel.this,
                            "Lỗi khi nạp dữ liệu báo cáo: " + ex.getMessage(),
                            "Lỗi Báo Cáo", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnTaiDuLieu.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void updateReportViewer(JasperPrint print) {
        pnlViewerContainer.removeAll();
        if (print != null) {
            currentViewer = new JRViewer(print);
            pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
        } else {
            pnlViewerContainer.add(new JLabel("Không có dữ liệu báo cáo", SwingConstants.CENTER), BorderLayout.CENTER);
        }
        pnlViewerContainer.revalidate();
        pnlViewerContainer.repaint();
    }
}