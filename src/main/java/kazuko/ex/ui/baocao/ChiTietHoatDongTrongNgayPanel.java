package kazuko.ex.ui.baocao;

import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SUser;
import kazuko.ex.report.service.BaoCaoPhanTichKinhDoanhReportService;
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

public class ChiTietHoatDongTrongNgayPanel extends JPanel {

    private final BaoCaoPhanTichKinhDoanhReportService reportService = new BaoCaoPhanTichKinhDoanhReportService();
    private final UserDAO userDAO = new UserDAO();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JComboBox<ComboItem> cbThuNgan;
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

    public ChiTietHoatDongTrongNgayPanel() {
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
        cbThuNgan.addActionListener(e -> onFilterChanged());
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
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date fromDate = cal.getTime();

        pnl.add(new JLabel("Từ ngày:"));
        spFromDate = new JSpinner(new SpinnerDateModel(fromDate, null, null, Calendar.DAY_OF_MONTH));
        spFromDate.setEditor(new JSpinner.DateEditor(spFromDate, "dd/MM/yyyy HH:mm"));
        spFromDate.setPreferredSize(new Dimension(130, 26));
        pnl.add(spFromDate);

        pnl.add(new JLabel("Đến ngày:"));
        spToDate = new JSpinner(new SpinnerDateModel(toDate, null, null, Calendar.DAY_OF_MONTH));
        spToDate.setEditor(new JSpinner.DateEditor(spToDate, "dd/MM/yyyy HH:mm"));
        spToDate.setPreferredSize(new Dimension(130, 26));
        pnl.add(spToDate);

        pnl.add(new JLabel("Thu ngân:"));
        cbThuNgan = new JComboBox<>();
        cbThuNgan.setPreferredSize(new Dimension(140, 26));
        pnl.add(cbThuNgan);

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
        cbThuNgan.removeAllItems();
        cbThuNgan.addItem(new ComboItem(null, "Tất cả"));
        try {
            List<SUser> users = userDAO.getAll();
            if (users != null) {
                for (SUser u : users) {
                    cbThuNgan.addItem(new ComboItem(u.getUsername(), u.getName() != null ? u.getName() : u.getUsername()));
                }
            }
        } catch (Exception ignored) {}
    }

    public void loadReport() {
        Date fromDate = (Date) spFromDate.getValue();
        Date toDate = (Date) spToDate.getValue();

        ComboItem itemThuNgan = (ComboItem) cbThuNgan.getSelectedItem();
        String thuNganId = itemThuNgan != null ? itemThuNgan.getId() : null;

        btnTaiDuLieu.setEnabled(false);

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateChiTietHoatDongTrongNgay(fromDate, toDate, thuNganId, null);
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
                    JOptionPane.showMessageDialog(ChiTietHoatDongTrongNgayPanel.this,
                            "Lỗi tải báo cáo: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnTaiDuLieu.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
