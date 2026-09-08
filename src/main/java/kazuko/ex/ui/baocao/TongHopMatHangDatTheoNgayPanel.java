package kazuko.ex.ui.baocao;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.report.service.BaoCaoDatHangReportService;
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

public class TongHopMatHangDatTheoNgayPanel extends JPanel {

    private final BaoCaoDatHangReportService reportService = new BaoCaoDatHangReportService();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JComboBox<ComboItem> cbMatHang;
    private JComboBox<ComboItem> cbNhomMatHang;
    private JComboBox<ComboItem> cbKhachHang;
    private JTextField txtFilter;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public TongHopMatHangDatTheoNgayPanel() {
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
        cbMatHang.addActionListener(e -> onFilterChanged());
        cbNhomMatHang.addActionListener(e -> onFilterChanged());
        cbKhachHang.addActionListener(e -> onFilterChanged());

        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onFilterChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onFilterChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onFilterChanged(); }
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

        pnl.add(new JLabel("Mặt hàng:"));
        cbMatHang = new JComboBox<>();
        cbMatHang.setPreferredSize(new Dimension(140, 26));
        pnl.add(cbMatHang);

        pnl.add(new JLabel("Nhóm mặt hàng:"));
        cbNhomMatHang = new JComboBox<>();
        cbNhomMatHang.setPreferredSize(new Dimension(130, 26));
        pnl.add(cbNhomMatHang);

        pnl.add(new JLabel("Khách hàng:"));
        cbKhachHang = new JComboBox<>();
        cbKhachHang.setPreferredSize(new Dimension(140, 26));
        pnl.add(cbKhachHang);

        pnl.add(new JLabel("Tìm:"));
        txtFilter = new JTextField();
        txtFilter.setPreferredSize(new Dimension(100, 26));
        pnl.add(txtFilter);

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
        cbMatHang.removeAllItems();
        cbMatHang.addItem(new ComboItem("", "-- Tất cả mặt hàng --"));
        try {
            List<MatHang> listMH = matHangDAO.getAll();
            if (listMH != null) {
                for (MatHang m : listMH) {
                    cbMatHang.addItem(new ComboItem(m.getId(), m.getName()));
                }
            }
        } catch (Exception ignored) {}

        cbNhomMatHang.removeAllItems();
        cbNhomMatHang.addItem(new ComboItem("", "-- Tất cả nhóm --"));
        try {
            List<NhomMatHang> listNhom = nhomMatHangDAO.getAll();
            if (listNhom != null) {
                for (NhomMatHang n : listNhom) {
                    cbNhomMatHang.addItem(new ComboItem(n.getId(), n.getName()));
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
    }

    public void loadReport() {
        btnTaiDuLieu.setEnabled(false);

        Date fromDate = (Date) spFromDate.getValue();
        Date toDate = (Date) spToDate.getValue();

        ComboItem selectedMH = (ComboItem) cbMatHang.getSelectedItem();
        String mhId = selectedMH != null ? selectedMH.getId() : "";

        ComboItem selectedNhom = (ComboItem) cbNhomMatHang.getSelectedItem();
        String nhomId = selectedNhom != null ? selectedNhom.getId() : "";

        ComboItem selectedKH = (ComboItem) cbKhachHang.getSelectedItem();
        String khId = selectedKH != null ? selectedKH.getId() : "";

        String filterText = txtFilter.getText();

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateTongHopMatHangDatTheoNgay(fromDate, toDate, mhId, nhomId, khId, filterText);
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
                    JOptionPane.showMessageDialog(TongHopMatHangDatTheoNgayPanel.this,
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
