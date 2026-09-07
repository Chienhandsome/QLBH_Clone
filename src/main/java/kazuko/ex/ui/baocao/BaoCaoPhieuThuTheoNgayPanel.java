package kazuko.ex.ui.baocao;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.LyDoThuChi;
import kazuko.ex.report.service.PhieuThuReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BaoCaoPhieuThuTheoNgayPanel extends JPanel {

    private final PhieuThuReportService reportService = new PhieuThuReportService();
    private final LyDoThuChiDAO lyDoThuChiDAO = new LyDoThuChiDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JComboBox<ComboItem> cbLyDoThuChi;
    private JComboBox<ComboItem> cbCuaHang;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;
    private JLabel lblLoading;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public BaoCaoPhieuThuTheoNgayPanel() {
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

        lblLoading = new JLabel("Đang tạo và tải báo cáo, vui lòng chờ...", SwingConstants.CENTER);
        lblLoading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLoading.setForeground(new Color(60, 90, 140));
        pnlViewerContainer.add(lblLoading, BorderLayout.CENTER);

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

        spFromDate.addChangeListener(e -> onFilterChanged());
        spToDate.addChangeListener(e -> onFilterChanged());
        cbLyDoThuChi.addActionListener(e -> onFilterChanged());
        cbCuaHang.addActionListener(e -> onFilterChanged());
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

        // Ngày Từ
        JLabel lblFrom = new JLabel("Ngày  Từ:");
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblFrom);

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = cal.getTime();

        spFromDate = new JSpinner(new SpinnerDateModel(firstDayOfMonth, null, null, Calendar.DAY_OF_MONTH));
        spFromDate.setEditor(new JSpinner.DateEditor(spFromDate, "dd/MM/yyyy"));
        spFromDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spFromDate.setPreferredSize(new Dimension(105, 24));
        pnl.add(spFromDate);

        // Đến
        JLabel lblTo = new JLabel("Đến");
        lblTo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTo);

        spToDate = new JSpinner(new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH));
        spToDate.setEditor(new JSpinner.DateEditor(spToDate, "dd/MM/yyyy"));
        spToDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spToDate.setPreferredSize(new Dimension(105, 24));
        pnl.add(spToDate);

        // Lý do thu chi
        JLabel lblLyDo = new JLabel("Lý do thu chi");
        lblLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblLyDo);

        cbLyDoThuChi = new JComboBox<>();
        cbLyDoThuChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLyDoThuChi.setPreferredSize(new Dimension(160, 24));
        cbLyDoThuChi.setBackground(Color.WHITE);
        pnl.add(cbLyDoThuChi);

        // Cửa hàng
        JLabel lblCuaHang = new JLabel("Cửa hàng");
        lblCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblCuaHang);

        cbCuaHang = new JComboBox<>();
        cbCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCuaHang.setPreferredSize(new Dimension(160, 24));
        cbCuaHang.setBackground(Color.WHITE);
        pnl.add(cbCuaHang);

        // Nút Tải dữ liệu (F5)
        btnTaiDuLieu = new JButton("Tải dữ liệu (F5)");
        btnTaiDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTaiDuLieu.setIcon(IconHelper.getIcon("Refresh.png", 15, 15));
        btnTaiDuLieu.setPreferredSize(new Dimension(125, 26));
        btnTaiDuLieu.setBackground(Color.WHITE);
        btnTaiDuLieu.setFocusPainted(false);
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        return pnl;
    }

    private void loadFilterData() {
        cbLyDoThuChi.removeAllItems();
        cbLyDoThuChi.addItem(new ComboItem("ALL", "-- Tất cả lý do --"));
        List<LyDoThuChi> listLyDo = lyDoThuChiDAO.getAllActive();
        for (LyDoThuChi tc : listLyDo) {
            if (tc.isLaLyDoThu()) {
                cbLyDoThuChi.addItem(new ComboItem(tc.getId(), tc.getName()));
            }
        }

        cbCuaHang.removeAllItems();
        cbCuaHang.addItem(new ComboItem("ALL", "-- Tất cả cửa hàng --"));
        List<CuaHang> listCH = cuaHangDAO.getAllActive();
        for (CuaHang ch : listCH) {
            cbCuaHang.addItem(new ComboItem(ch.getId(), ch.getName()));
        }
    }

    public void loadReport() {
        Date fromDate = (Date) spFromDate.getValue();
        Date toDate = (Date) spToDate.getValue();

        ComboItem itemLyDo = (ComboItem) cbLyDoThuChi.getSelectedItem();
        String lyDoId = itemLyDo != null ? itemLyDo.id : "ALL";

        ComboItem itemCH = (ComboItem) cbCuaHang.getSelectedItem();
        String chId = itemCH != null ? itemCH.id : "ALL";

        btnTaiDuLieu.setEnabled(false);
        pnlViewerContainer.removeAll();
        lblLoading.setText("Đang tải dữ liệu báo cáo...");
        pnlViewerContainer.add(lblLoading, BorderLayout.CENTER);
        pnlViewerContainer.revalidate();
        pnlViewerContainer.repaint();

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateReportPrint(fromDate, toDate, lyDoId, chId);
            }

            @Override
            protected void done() {
                btnTaiDuLieu.setEnabled(true);
                try {
                    JasperPrint print = get();
                    pnlViewerContainer.removeAll();

                    currentViewer = new JRViewer(print);
                    currentViewer.setZoomRatio(1.0f); // 100% zoom
                    pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
                } catch (Exception e) {
                    pnlViewerContainer.removeAll();
                    JLabel lblError = new JLabel("Lỗi tạo báo cáo: " + e.getMessage(), SwingConstants.CENTER);
                    lblError.setForeground(Color.RED);
                    pnlViewerContainer.add(lblError, BorderLayout.CENTER);
                    e.printStackTrace();
                }
                pnlViewerContainer.revalidate();
                pnlViewerContainer.repaint();
            }
        };

        worker.execute();
    }

    private static class ComboItem {
        final String id;
        final String text;

        ComboItem(String id, String text) {
            this.id = id;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
