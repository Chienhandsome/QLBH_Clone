package kazuko.ex.ui.baocao;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.report.service.BaoCaoTonKhoReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class BaoCaoTonKhoTheoKichThuocPanel extends JPanel {

    private final BaoCaoTonKhoReportService reportService = new BaoCaoTonKhoReportService();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

    private JComboBox<ComboItem> cbNhomHang;
    private JComboBox<ComboItem> cbKhoHang;
    private JTextField txtTimKiem;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public static class ComboItem {
        private final String id;
        private final String name;

        public ComboItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }

    public BaoCaoTonKhoTheoKichThuocPanel() {
        initComponents();
        loadDropdownData();
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

    private void loadDropdownData() {
        cbNhomHang.removeAllItems();
        cbNhomHang.addItem(new ComboItem(null, "-- Tất cả nhóm hàng --"));
        try {
            List<NhomMatHang> nhoms = nhomMatHangDAO.getAll();
            if (nhoms != null) {
                for (NhomMatHang n : nhoms) {
                    cbNhomHang.addItem(new ComboItem(n.getId(), n.getName()));
                }
            }
        } catch (Exception ignored) {}

        cbKhoHang.removeAllItems();
        cbKhoHang.addItem(new ComboItem(null, "-- Tất cả kho hàng --"));
        try {
            List<KhoHang> khos = khoHangDAO.getAll();
            if (khos != null) {
                for (KhoHang k : khos) {
                    cbKhoHang.addItem(new ComboItem(k.getId(), k.getName()));
                }
            }
        } catch (Exception ignored) {}
    }

    private void setupAutoReloadListeners() {
        debounceTimer = new javax.swing.Timer(250, e -> loadReport());
        debounceTimer.setRepeats(false);

        cbNhomHang.addActionListener(e -> onFilterChanged());
        cbKhoHang.addActionListener(e -> onFilterChanged());
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onFilterChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onFilterChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onFilterChanged(); }
        });
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

        pnl.add(new JLabel("Nhóm hàng:"));
        cbNhomHang = new JComboBox<>();
        cbNhomHang.setPreferredSize(new Dimension(160, 26));
        pnl.add(cbNhomHang);

        pnl.add(new JLabel("Kho hàng:"));
        cbKhoHang = new JComboBox<>();
        cbKhoHang.setPreferredSize(new Dimension(150, 26));
        pnl.add(cbKhoHang);

        pnl.add(new JLabel("Tìm kiếm:"));
        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(140, 26));
        pnl.add(txtTimKiem);

        // Nút Tải lại / Xem báo cáo
        btnTaiDuLieu = new JButton("Xem báo cáo (F5)");
        btnTaiDuLieu.setIcon(IconHelper.getIcon("refresh", 16, 16));
        btnTaiDuLieu.setBackground(new Color(51, 122, 183));
        btnTaiDuLieu.setForeground(Color.WHITE);
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.setPreferredSize(new Dimension(150, 26));
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        return pnl;
    }

    public void loadReport() {
        btnTaiDuLieu.setEnabled(false);

        ComboItem selNhom = (ComboItem) cbNhomHang.getSelectedItem();
        String nhomId = selNhom != null ? selNhom.getId() : null;

        ComboItem selKho = (ComboItem) cbKhoHang.getSelectedItem();
        String khoId = selKho != null ? selKho.getId() : null;

        String keyword = txtTimKiem.getText().trim();

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateTonKhoTheoKichThuocReport(nhomId, khoId, keyword);
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
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BaoCaoTonKhoTheoKichThuocPanel.this,
                            "Lỗi tải báo cáo: " + ex.getMessage(),
                            "Lỗi Báo Cáo", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnTaiDuLieu.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
