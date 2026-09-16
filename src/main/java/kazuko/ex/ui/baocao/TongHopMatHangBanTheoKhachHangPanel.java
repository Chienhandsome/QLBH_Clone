package kazuko.ex.ui.baocao;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomKhachHang;
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

public class TongHopMatHangBanTheoKhachHangPanel extends JPanel {

    private final BaoCaoBanHangReportService reportService = new BaoCaoBanHangReportService();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JComboBox<ComboItem> cbNhomKhach;
    private JComboBox<ComboItem> cbKhachHang;
    private JComboBox<ComboItem> cbMatHang;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public TongHopMatHangBanTheoKhachHangPanel() {
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

    private void setupAutoReloadListeners() {
        debounceTimer = new javax.swing.Timer(250, e -> loadReport());
        debounceTimer.setRepeats(false);

        spFromDate.addChangeListener(e -> onFilterChanged());
        spToDate.addChangeListener(e -> onFilterChanged());
        cbNhomKhach.addActionListener(e -> onFilterChanged());
        cbKhachHang.addActionListener(e -> onFilterChanged());
        cbMatHang.addActionListener(e -> onFilterChanged());
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

        pnl.add(new JLabel("Nhóm KH:"));
        cbNhomKhach = new JComboBox<>();
        cbNhomKhach.setPreferredSize(new Dimension(140, 26));
        pnl.add(cbNhomKhach);

        pnl.add(new JLabel("Khách hàng:"));
        cbKhachHang = new JComboBox<>();
        cbKhachHang.setPreferredSize(new Dimension(150, 26));
        pnl.add(cbKhachHang);

        pnl.add(new JLabel("Mặt hàng:"));
        cbMatHang = new JComboBox<>();
        cbMatHang.setPreferredSize(new Dimension(150, 26));
        pnl.add(cbMatHang);

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
        cbNhomKhach.removeAllItems();
        cbNhomKhach.addItem(new ComboItem("", "-- Tất cả nhóm --"));
        try {
            List<NhomKhachHang> listNKH = nhomKhachHangDAO.getAll();
            if (listNKH != null) {
                for (NhomKhachHang nkh : listNKH) {
                    cbNhomKhach.addItem(new ComboItem(nkh.getId(), nkh.getName()));
                }
            }
        } catch (Exception ignored) {}

        cbKhachHang.removeAllItems();
        cbKhachHang.addItem(new ComboItem("", "-- Tất cả khách hàng --"));
        try {
            List<KhachHang> listKH = khachHangDAO.getAll();
            if (listKH != null) {
                for (KhachHang kh : listKH) {
                    cbKhachHang.addItem(new ComboItem(kh.getId(), kh.getName()));
                }
            }
        } catch (Exception ignored) {}

        cbMatHang.removeAllItems();
        cbMatHang.addItem(new ComboItem("", "-- Tất cả mặt hàng --"));
        try {
            List<MatHang> listMH = matHangDAO.getAll();
            if (listMH != null) {
                for (MatHang mh : listMH) {
                    cbMatHang.addItem(new ComboItem(mh.getId() != null ? mh.getId() : mh.getCode(), mh.getName()));
                }
            }
        } catch (Exception ignored) {}
    }

    public void loadReport() {
        btnTaiDuLieu.setEnabled(false);

        Date fromDate = (Date) spFromDate.getValue();
        Date toDate = (Date) spToDate.getValue();

        ComboItem selectedNKH = (ComboItem) cbNhomKhach.getSelectedItem();
        String nkhId = selectedNKH != null ? selectedNKH.getId() : "";

        ComboItem selectedKH = (ComboItem) cbKhachHang.getSelectedItem();
        String khId = selectedKH != null ? selectedKH.getId() : "";

        ComboItem selectedMH = (ComboItem) cbMatHang.getSelectedItem();
        String mhId = selectedMH != null ? selectedMH.getId() : "";

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateTongHopMatHangBanTheoKhachHang(fromDate, toDate, nkhId, khId, mhId);
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
                    JOptionPane.showMessageDialog(TongHopMatHangBanTheoKhachHangPanel.this,
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
