package kazuko.ex.ui.baocao;

import kazuko.ex.dao.NhomNhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;
import kazuko.ex.report.service.BaoCaoCongNoReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class TongHopCongNoNhaCungCapPanel extends JPanel {

    private final BaoCaoCongNoReportService reportService = new BaoCaoCongNoReportService();
    private final NhomNhaCungCapDAO nhomDAO = new NhomNhaCungCapDAO();

    private JComboBox<ComboItem> cbNhomNhaCungCap;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public TongHopCongNoNhaCungCapPanel() {
        initComponents();
        loadFilterData();
        setupAutoReloadListeners();
        isInitializing = false;
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

        currentViewer = new ReportJRViewer(null);
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

    private void setupAutoReloadListeners() {
        debounceTimer = new javax.swing.Timer(250, e -> loadReport());
        debounceTimer.setRepeats(false);

        cbNhomNhaCungCap.addActionListener(e -> onFilterChanged());
    }

    private void onFilterChanged() {
        if (isInitializing) return;
        if (debounceTimer != null) {
            debounceTimer.restart();
        } else {
            loadReport();
        }
    }

    private JPanel createFilterPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnl.setBackground(new Color(245, 248, 253));
        pnl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));

        // Nhóm nhà cung cấp
        pnl.add(new JLabel("Nhóm nhà cung cấp:"));
        cbNhomNhaCungCap = new JComboBox<>();
        cbNhomNhaCungCap.setPreferredSize(new Dimension(180, 26));
        pnl.add(cbNhomNhaCungCap);

        // Nút Tải dữ liệu
        btnTaiDuLieu = new JButton("Tải dữ liệu (F5)");
        btnTaiDuLieu.setIcon(IconHelper.getIcon("refresh_16.png", 16, 16));
        btnTaiDuLieu.setBackground(new Color(220, 235, 252));
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        return pnl;
    }

    private void loadFilterData() {
        cbNhomNhaCungCap.removeAllItems();
        cbNhomNhaCungCap.addItem(new ComboItem("Tất cả", null));
        try {
            List<NhomNhaCungCap> list = nhomDAO.getAll();
            for (NhomNhaCungCap n : list) {
                cbNhomNhaCungCap.addItem(new ComboItem(n.getName(), n.getId()));
            }
        } catch (Exception ignored) {}
    }

    public void loadReport() {
        ComboItem selectedNhom = (ComboItem) cbNhomNhaCungCap.getSelectedItem();
        String nhomId = selectedNhom != null ? selectedNhom.getId() : null;

        btnTaiDuLieu.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateTongHopCongNoNhaCungCap(nhomId);
            }

            @Override
            protected void done() {
                try {
                    JasperPrint jp = get();
                    pnlViewerContainer.removeAll();
                    if (jp != null) {
                        currentViewer = new ReportJRViewer(jp);
                        currentViewer.setZoomRatio(1.0f);
                        pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
                    }
                    pnlViewerContainer.revalidate();
                    pnlViewerContainer.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TongHopCongNoNhaCungCapPanel.this,
                            "Lỗi tải báo cáo: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnTaiDuLieu.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private static class ComboItem {
        private final String label;
        private final String id;

        public ComboItem(String label, String id) {
            this.label = label;
            this.id = id;
        }

        public String getId() { return id; }
        @Override public String toString() { return label; }
    }
}
