package kazuko.ex.ui.baocao;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.report.service.BaoCaoChuyenKhoReportService;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TongHopMatHangChuyenKhoTheoNhanVienNhanPanel extends JPanel {

    private final BaoCaoChuyenKhoReportService reportService = new BaoCaoChuyenKhoReportService();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JComboBox<ComboItem> cbKhoNhap;
    private JComboBox<ComboItem> cbKhoXuat;
    private JComboBox<ComboItem> cbNvNhap;
    private JComboBox<ComboItem> cbNvXuat;
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

    public TongHopMatHangChuyenKhoTheoNhanVienNhanPanel() {
        initComponents();
        loadDropdownData();
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

        currentViewer = new ReportJRViewer(null);
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

    private void loadDropdownData() {
        cbKhoNhap.removeAllItems();
        cbKhoNhap.addItem(new ComboItem(null, "-- Tất cả kho --"));
        cbKhoXuat.removeAllItems();
        cbKhoXuat.addItem(new ComboItem(null, "-- Tất cả kho --"));
        try {
            List<KhoHang> khos = khoHangDAO.getAll();
            if (khos != null) {
                for (KhoHang k : khos) {
                    cbKhoNhap.addItem(new ComboItem(k.getId(), k.getName()));
                    cbKhoXuat.addItem(new ComboItem(k.getId(), k.getName()));
                }
            }
        } catch (Exception ignored) {}

        cbNvNhap.removeAllItems();
        cbNvNhap.addItem(new ComboItem(null, "-- Tất cả NV --"));
        cbNvXuat.removeAllItems();
        cbNvXuat.addItem(new ComboItem(null, "-- Tất cả NV --"));
        try {
            List<NhanVien> nvs = nhanVienDAO.getAll();
            if (nvs != null) {
                for (NhanVien nv : nvs) {
                    cbNvNhap.addItem(new ComboItem(nv.getId(), nv.getName()));
                    cbNvXuat.addItem(new ComboItem(nv.getId(), nv.getName()));
                }
            }
        } catch (Exception ignored) {}
    }

    private void setupAutoReloadListeners() {
        debounceTimer = new javax.swing.Timer(250, e -> loadReport());
        debounceTimer.setRepeats(false);

        spFromDate.addChangeListener(e -> onFilterChanged());
        spToDate.addChangeListener(e -> onFilterChanged());
        cbKhoNhap.addActionListener(e -> onFilterChanged());
        cbKhoXuat.addActionListener(e -> onFilterChanged());
        cbNvNhap.addActionListener(e -> onFilterChanged());
        cbNvXuat.addActionListener(e -> onFilterChanged());
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
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pnl.setBackground(new Color(245, 248, 253));
        pnl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));

        Calendar cal = Calendar.getInstance();
        Date toDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date fromDate = cal.getTime();

        pnl.add(new JLabel("Từ:"));
        spFromDate = new JSpinner(new SpinnerDateModel(fromDate, null, null, Calendar.DAY_OF_MONTH));
        spFromDate.setEditor(new JSpinner.DateEditor(spFromDate, "dd/MM/yyyy"));
        spFromDate.setPreferredSize(new Dimension(95, 26));
        pnl.add(spFromDate);

        pnl.add(new JLabel("Đến:"));
        spToDate = new JSpinner(new SpinnerDateModel(toDate, null, null, Calendar.DAY_OF_MONTH));
        spToDate.setEditor(new JSpinner.DateEditor(spToDate, "dd/MM/yyyy"));
        spToDate.setPreferredSize(new Dimension(95, 26));
        pnl.add(spToDate);

        pnl.add(new JLabel("Kho nhập:"));
        cbKhoNhap = new JComboBox<>();
        cbKhoNhap.setPreferredSize(new Dimension(110, 26));
        pnl.add(cbKhoNhap);

        pnl.add(new JLabel("Kho xuất:"));
        cbKhoXuat = new JComboBox<>();
        cbKhoXuat.setPreferredSize(new Dimension(110, 26));
        pnl.add(cbKhoXuat);

        pnl.add(new JLabel("NV nhập:"));
        cbNvNhap = new JComboBox<>();
        cbNvNhap.setPreferredSize(new Dimension(110, 26));
        pnl.add(cbNvNhap);

        pnl.add(new JLabel("NV xuất:"));
        cbNvXuat = new JComboBox<>();
        cbNvXuat.setPreferredSize(new Dimension(110, 26));
        pnl.add(cbNvXuat);

        pnl.add(new JLabel("Tìm:"));
        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(85, 26));
        pnl.add(txtTimKiem);

        btnTaiDuLieu = new JButton("Xem báo cáo (F5)");
        btnTaiDuLieu.setIcon(IconHelper.getIcon("refresh", 16, 16));
        btnTaiDuLieu.setBackground(new Color(51, 122, 183));
        btnTaiDuLieu.setForeground(Color.WHITE);
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.setPreferredSize(new Dimension(145, 26));
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        return pnl;
    }

    public void loadReport() {
        btnTaiDuLieu.setEnabled(false);

        Date fromDate = (Date) spFromDate.getValue();
        Date toDate = (Date) spToDate.getValue();

        ComboItem selKhoNhap = (ComboItem) cbKhoNhap.getSelectedItem();
        String khoNhapId = selKhoNhap != null ? selKhoNhap.getId() : null;

        ComboItem selKhoXuat = (ComboItem) cbKhoXuat.getSelectedItem();
        String khoXuatId = selKhoXuat != null ? selKhoXuat.getId() : null;

        ComboItem selNvNhap = (ComboItem) cbNvNhap.getSelectedItem();
        String nvNhapId = selNvNhap != null ? selNvNhap.getId() : null;

        ComboItem selNvXuat = (ComboItem) cbNvXuat.getSelectedItem();
        String nvXuatId = selNvXuat != null ? selNvXuat.getId() : null;

        String keyword = txtTimKiem.getText().trim();

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateTongHopMatHangTheoNhanVienNhanReport(fromDate, toDate, khoNhapId, khoXuatId, nvNhapId, nvXuatId, keyword);
            }

            @Override
            protected void done() {
                try {
                    JasperPrint print = get();
                    pnlViewerContainer.removeAll();
                    currentViewer = new ReportJRViewer(print);
                    pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
                    pnlViewerContainer.revalidate();
                    pnlViewerContainer.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TongHopMatHangChuyenKhoTheoNhanVienNhanPanel.this,
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
