package kazuko.ex.ui.baocao;

import kazuko.ex.report.service.BaoCaoBieuDoReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.Year;

public class BieuDoDoanhThuNgayTrongThangPanel extends JPanel {

    private final BaoCaoBieuDoReportService reportService = new BaoCaoBieuDoReportService();

    private JSpinner spNam;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public BieuDoDoanhThuNgayTrongThangPanel() {
        initComponents();
        setupAutoReloadListeners();
        isInitializing = false;
        loadReport();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 245, 252));

        JPanel pnlTop = createFilterPanel();
        add(pnlTop, BorderLayout.NORTH);

        pnlViewerContainer = new JPanel(new BorderLayout());
        pnlViewerContainer.setBackground(new Color(215, 228, 242));

        currentViewer = new JRViewer(null);
        pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);

        add(pnlViewerContainer, BorderLayout.CENTER);

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

        spNam.addChangeListener(e -> onFilterChanged());
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
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnl.setBackground(new Color(245, 248, 253));
        pnl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));

        int currentYear = Year.now().getValue();

        pnl.add(new JLabel("Năm:"));
        spNam = new JSpinner(new SpinnerNumberModel(currentYear, 2000, 2100, 1));
        spNam.setEditor(new JSpinner.NumberEditor(spNam, "####"));
        spNam.setPreferredSize(new Dimension(80, 26));
        pnl.add(spNam);

        btnTaiDuLieu = new JButton("Tải dữ liệu (F5)");
        btnTaiDuLieu.setIcon(IconHelper.getIcon("refresh", 16, 16));
        btnTaiDuLieu.setBackground(new Color(51, 122, 183));
        btnTaiDuLieu.setForeground(Color.WHITE);
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.setPreferredSize(new Dimension(140, 26));
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        return pnl;
    }

    public void loadReport() {
        int nam = (Integer) spNam.getValue();

        btnTaiDuLieu.setEnabled(false);

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateBieuDoDoanhThuNgayTrongThang(nam);
            }

            @Override
            protected void done() {
                try {
                    JasperPrint print = get();
                    pnlViewerContainer.removeAll();
                    currentViewer = new JRViewer(print);
                    pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
                    pnlViewerContainer.revalidate();
                    pnlViewerContainer.repaint();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BieuDoDoanhThuNgayTrongThangPanel.this,
                            "Lỗi tải biểu đồ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnTaiDuLieu.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
