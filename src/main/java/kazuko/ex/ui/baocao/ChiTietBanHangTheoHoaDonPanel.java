package kazuko.ex.ui.baocao;

import kazuko.ex.report.service.BaoCaoBanHangReportService;
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

public class ChiTietBanHangTheoHoaDonPanel extends JPanel {

    private final BaoCaoBanHangReportService reportService = new BaoCaoBanHangReportService();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JTextField txtFilter;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public ChiTietBanHangTheoHoaDonPanel() {
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

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "focusFilter");
        getActionMap().put("focusFilter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtFilter.requestFocusInWindow();
                txtFilter.selectAll();
            }
        });
    }

    private void setupAutoReloadListeners() {
        debounceTimer = new javax.swing.Timer(250, e -> loadReport());
        debounceTimer.setRepeats(false);

        spFromDate.addChangeListener(e -> onFilterChanged());
        spToDate.addChangeListener(e -> onFilterChanged());

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

        pnl.add(new JLabel("Từ ngày:"));
        spFromDate = new JSpinner(new SpinnerDateModel(fromDate, null, null, Calendar.DAY_OF_MONTH));
        spFromDate.setEditor(new JSpinner.DateEditor(spFromDate, "dd/MM/yyyy"));
        spFromDate.setPreferredSize(new Dimension(100, 24));
        pnl.add(spFromDate);

        pnl.add(new JLabel("Đến ngày:"));
        spToDate = new JSpinner(new SpinnerDateModel(toDate, null, null, Calendar.DAY_OF_MONTH));
        spToDate.setEditor(new JSpinner.DateEditor(spToDate, "dd/MM/yyyy"));
        spToDate.setPreferredSize(new Dimension(100, 24));
        pnl.add(spToDate);

        btnTaiDuLieu = new JButton("Tải dữ liệu (F5)", IconHelper.getIcon("arrow_refresh.png"));
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        pnl.add(Box.createHorizontalStrut(10));
        JLabel lblFilter = new JLabel("Lọc (F3):", IconHelper.getIcon("find.png"), JLabel.LEFT);
        pnl.add(lblFilter);

        txtFilter = new JTextField();
        txtFilter.setPreferredSize(new Dimension(150, 24));
        pnl.add(txtFilter);

        return pnl;
    }

    private void loadReport() {
        Date fromDate = (Date) spFromDate.getValue();
        Date toDate = (Date) spToDate.getValue();
        String filterText = txtFilter.getText();

        Calendar cal = Calendar.getInstance();
        if (fromDate != null) {
            cal.setTime(fromDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fromDate = cal.getTime();
        }
        if (toDate != null) {
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            toDate = cal.getTime();
        }

        final Date fDate = fromDate;
        final Date tDate = toDate;

        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateChiTietBanHangTheoHoaDon(fDate, tDate, filterText);
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
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}
