package kazuko.ex.ui.baocao;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.report.service.BaoCaoCongNoReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BaoCaoCongNoKhachHangPanel extends JPanel {

    private final BaoCaoCongNoReportService reportService = new BaoCaoCongNoReportService();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private JSpinner spinTuNgay;
    private JSpinner spinDenNgay;
    private JComboBox<ComboItem> cbKhachHang;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public BaoCaoCongNoKhachHangPanel() {
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

        spinTuNgay.addChangeListener(e -> onFilterChanged());
        spinDenNgay.addChangeListener(e -> onFilterChanged());
        cbKhachHang.addActionListener(e -> onFilterChanged());
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

        // Từ ngày
        pnl.add(new JLabel("Từ ngày:"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        spinTuNgay = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        spinTuNgay.setEditor(new JSpinner.DateEditor(spinTuNgay, "dd/MM/yyyy"));
        spinTuNgay.setPreferredSize(new Dimension(105, 26));
        pnl.add(spinTuNgay);

        // Đến ngày
        pnl.add(new JLabel("Đến ngày:"));
        spinDenNgay = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinDenNgay.setEditor(new JSpinner.DateEditor(spinDenNgay, "dd/MM/yyyy"));
        spinDenNgay.setPreferredSize(new Dimension(105, 26));
        pnl.add(spinDenNgay);

        // Khách hàng cần đối chiếu
        pnl.add(new JLabel("Khách hàng:"));
        cbKhachHang = new JComboBox<>();
        cbKhachHang.setPreferredSize(new Dimension(240, 26));
        pnl.add(cbKhachHang);

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
        cbKhachHang.removeAllItems();
        cbKhachHang.addItem(new ComboItem("-- Chọn khách hàng --", null));
        try {
            List<KhachHang> list = khachHangDAO.getAll();
            for (KhachHang kh : list) {
                String ma = kh.getMaKhach() != null && !kh.getMaKhach().trim().isEmpty()
                        ? kh.getMaKhach().trim() + " - " : "";
                String ten = kh.getName() != null ? kh.getName() : "";
                cbKhachHang.addItem(new ComboItem(ma + ten, kh.getId()));
            }
        } catch (Exception ignored) {}
    }

    public void loadReport() {
        Date tuNgay = (Date) spinTuNgay.getValue();
        Date denNgay = (Date) spinDenNgay.getValue();
        ComboItem selectedKhach = (ComboItem) cbKhachHang.getSelectedItem();
        String khachHangId = selectedKhach != null ? selectedKhach.getId() : null;

        if (khachHangId == null || khachHangId.trim().isEmpty()) {
            pnlViewerContainer.removeAll();
            JLabel message = new JLabel("Vui lòng chọn khách hàng cần đối chiếu công nợ.", SwingConstants.CENTER);
            message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            message.setForeground(new Color(75, 90, 110));
            pnlViewerContainer.add(message, BorderLayout.CENTER);
            pnlViewerContainer.revalidate();
            pnlViewerContainer.repaint();
            return;
        }

        btnTaiDuLieu.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateBaoCaoCongNoKhachHang(tuNgay, denNgay, khachHangId);
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
                    JOptionPane.showMessageDialog(BaoCaoCongNoKhachHangPanel.this,
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
