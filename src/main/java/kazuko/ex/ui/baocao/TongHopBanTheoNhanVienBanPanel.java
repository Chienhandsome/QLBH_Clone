package kazuko.ex.ui.baocao;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.report.service.BaoCaoBanHangReportService;
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

public class TongHopBanTheoNhanVienBanPanel extends JPanel {

    private final BaoCaoBanHangReportService reportService = new BaoCaoBanHangReportService();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JComboBox<ComboItem> cbKhoXuat;
    private JComboBox<ComboItem> cbNhanVienXuat;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public TongHopBanTheoNhanVienBanPanel() {
        initComponents();
        loadFilterData();
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

        spFromDate.addChangeListener(e -> onFilterChanged());
        spToDate.addChangeListener(e -> onFilterChanged());
        cbKhoXuat.addActionListener(e -> onFilterChanged());
        cbNhanVienXuat.addActionListener(e -> onFilterChanged());
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

        Calendar cal = Calendar.getInstance();
        Date toDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date fromDate = cal.getTime();

        pnl.add(new JLabel("Từ:"));
        spFromDate = new JSpinner(new SpinnerDateModel(fromDate, null, null, Calendar.DAY_OF_MONTH));
        spFromDate.setEditor(new JSpinner.DateEditor(spFromDate, "dd/MM/yyyy"));
        spFromDate.setPreferredSize(new Dimension(100, 26));
        pnl.add(spFromDate);

        pnl.add(new JLabel("Đến:"));
        spToDate = new JSpinner(new SpinnerDateModel(toDate, null, null, Calendar.DAY_OF_MONTH));
        spToDate.setEditor(new JSpinner.DateEditor(spToDate, "dd/MM/yyyy"));
        spToDate.setPreferredSize(new Dimension(100, 26));
        pnl.add(spToDate);

        pnl.add(new JLabel("Kho xuất:"));
        cbKhoXuat = new JComboBox<>();
        cbKhoXuat.setPreferredSize(new Dimension(150, 26));
        pnl.add(cbKhoXuat);

        pnl.add(new JLabel("Nhân viên xuất:"));
        cbNhanVienXuat = new JComboBox<>();
        cbNhanVienXuat.setPreferredSize(new Dimension(150, 26));
        pnl.add(cbNhanVienXuat);

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

    private void loadFilterData() {
        cbKhoXuat.removeAllItems();
        cbKhoXuat.addItem(new ComboItem("", "-- Tất cả kho --"));
        try {
            List<KhoHang> listKho = khoHangDAO.getAll();
            if (listKho != null) {
                for (KhoHang k : listKho) {
                    cbKhoXuat.addItem(new ComboItem(k.getId(), k.getName()));
                }
            }
        } catch (Exception ignored) {}

        cbNhanVienXuat.removeAllItems();
        cbNhanVienXuat.addItem(new ComboItem("", "-- Tất cả nhân viên --"));
        try {
            List<NhanVien> listNV = nhanVienDAO.getAll();
            if (listNV != null) {
                for (NhanVien nv : listNV) {
                    cbNhanVienXuat.addItem(new ComboItem(nv.getId(), nv.getName()));
                }
            }
        } catch (Exception ignored) {}
    }

    public void loadReport() {
        btnTaiDuLieu.setEnabled(false);

        Date fromDate = (Date) spFromDate.getValue();
        Date toDate = (Date) spToDate.getValue();

        ComboItem selectedKho = (ComboItem) cbKhoXuat.getSelectedItem();
        String khoId = selectedKho != null ? selectedKho.getId() : "";

        ComboItem selectedNV = (ComboItem) cbNhanVienXuat.getSelectedItem();
        String nvId = selectedNV != null ? selectedNV.getId() : "";

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateTongHopBanTheoNhanVienBan(fromDate, toDate, khoId, nvId);
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
                    JOptionPane.showMessageDialog(TongHopBanTheoNhanVienBanPanel.this,
                            "Lỗi tải báo cáo: " + ex.getMessage(),
                            "Lỗi Báo Cáo", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnTaiDuLieu.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private static class ComboItem {
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
}
