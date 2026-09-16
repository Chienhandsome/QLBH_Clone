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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BaoCaoTongHopXuatNhapTonPanel extends JPanel {

    private final BaoCaoTonKhoReportService reportService = new BaoCaoTonKhoReportService();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

    private JSpinner spTuNgay;
    private JSpinner spDenNgay;
    private JComboBox<ComboItem> cbKhoHang;
    private JComboBox<ComboItem> cbNhomHang;
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

    public BaoCaoTongHopXuatNhapTonPanel() {
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
        cbKhoHang.removeAllItems();
        cbKhoHang.addItem(new ComboItem(null, "Tất cả"));
        try {
            List<KhoHang> khos = khoHangDAO.getAll();
            if (khos != null) {
                for (KhoHang k : khos) {
                    cbKhoHang.addItem(new ComboItem(k.getId(), k.getName()));
                }
            }
        } catch (Exception ignored) {}

        cbNhomHang.removeAllItems();
        cbNhomHang.addItem(new ComboItem(null, "Tất cả"));
        try {
            List<NhomMatHang> nhoms = nhomMatHangDAO.getAll();
            if (nhoms != null) {
                for (NhomMatHang n : nhoms) {
                    cbNhomHang.addItem(new ComboItem(n.getId(), n.getName()));
                }
            }
        } catch (Exception ignored) {}
    }

    private void setupAutoReloadListeners() {
        debounceTimer = new javax.swing.Timer(250, e -> loadReport());
        debounceTimer.setRepeats(false);

        spTuNgay.addChangeListener(e -> onFilterChanged());
        spDenNgay.addChangeListener(e -> onFilterChanged());
        cbKhoHang.addActionListener(e -> onFilterChanged());
        cbNhomHang.addActionListener(e -> onFilterChanged());
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
        Date denNgay = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date tuNgay = cal.getTime();

        pnl.add(new JLabel("Từ ngày:"));
        spTuNgay = new JSpinner(new SpinnerDateModel(tuNgay, null, null, Calendar.DAY_OF_MONTH));
        spTuNgay.setEditor(new JSpinner.DateEditor(spTuNgay, "dd/MM/yyyy"));
        spTuNgay.setPreferredSize(new Dimension(95, 26));
        pnl.add(spTuNgay);

        pnl.add(new JLabel("Đến:"));
        spDenNgay = new JSpinner(new SpinnerDateModel(denNgay, null, null, Calendar.DAY_OF_MONTH));
        spDenNgay.setEditor(new JSpinner.DateEditor(spDenNgay, "dd/MM/yyyy"));
        spDenNgay.setPreferredSize(new Dimension(95, 26));
        pnl.add(spDenNgay);

        pnl.add(new JLabel("Kho hàng:"));
        cbKhoHang = new JComboBox<>();
        cbKhoHang.setPreferredSize(new Dimension(130, 26));
        pnl.add(cbKhoHang);

        pnl.add(new JLabel("Nhóm hàng:"));
        cbNhomHang = new JComboBox<>();
        cbNhomHang.setPreferredSize(new Dimension(130, 26));
        pnl.add(cbNhomHang);

        btnTaiDuLieu = new JButton("Tải dữ liệu (F5)");
        btnTaiDuLieu.setIcon(IconHelper.getIcon("refresh", 16, 16));
        btnTaiDuLieu.setBackground(new Color(51, 122, 183));
        btnTaiDuLieu.setForeground(Color.WHITE);
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.setPreferredSize(new Dimension(130, 26));
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        pnl.add(new JLabel("Lọc (F3):"));
        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(100, 26));
        pnl.add(txtTimKiem);

        return pnl;
    }

    public void loadReport() {
        Date tuNgay = (Date) spTuNgay.getValue();
        Date denNgay = (Date) spDenNgay.getValue();

        ComboItem itemKho = (ComboItem) cbKhoHang.getSelectedItem();
        String khoId = itemKho != null ? itemKho.getId() : null;

        ComboItem itemNhom = (ComboItem) cbNhomHang.getSelectedItem();
        String nhomId = itemNhom != null ? itemNhom.getId() : null;

        String keyword = txtTimKiem.getText();

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateTongHopXuatNhapTonReport(tuNgay, denNgay, khoId, nhomId, keyword);
            }

            @Override
            protected void done() {
                try {
                    JasperPrint jp = get();
                    pnlViewerContainer.removeAll();
                    if (jp != null) {
                        currentViewer = new ReportJRViewer(jp);
                        pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
                    }
                    pnlViewerContainer.revalidate();
                    pnlViewerContainer.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
