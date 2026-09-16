package kazuko.ex.ui.baocao;

import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.report.service.BaoCaoKhachHangReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class BaoCaoKhachHangTheoNhomPanel extends JPanel {

    private final BaoCaoKhachHangReportService reportService = new BaoCaoKhachHangReportService();
    private final NhomKhachHangDAO nhomDAO = new NhomKhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private JComboBox<ComboItem> cbNhomKhachHang;
    private JComboBox<ComboItem> cbNhanVien;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public BaoCaoKhachHangTheoNhomPanel() {
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

        cbNhomKhachHang.addActionListener(e -> onFilterChanged());
        cbNhanVien.addActionListener(e -> onFilterChanged());
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

        // Nhóm khách hàng
        pnl.add(new JLabel("Nhóm khách hàng:"));
        cbNhomKhachHang = new JComboBox<>();
        cbNhomKhachHang.setPreferredSize(new Dimension(180, 26));
        pnl.add(cbNhomKhachHang);

        // Nhân viên
        pnl.add(new JLabel("Nhân viên:"));
        cbNhanVien = new JComboBox<>();
        cbNhanVien.setPreferredSize(new Dimension(160, 26));
        pnl.add(cbNhanVien);

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
        cbNhomKhachHang.removeAllItems();
        cbNhomKhachHang.addItem(new ComboItem("", "-- Tất cả nhóm khách hàng --"));
        List<NhomKhachHang> listNhom = nhomDAO.getAll();
        if (listNhom != null) {
            for (NhomKhachHang nhom : listNhom) {
                cbNhomKhachHang.addItem(new ComboItem(nhom.getId(), nhom.getName()));
            }
        }

        cbNhanVien.removeAllItems();
        cbNhanVien.addItem(new ComboItem("", "-- Tất cả nhân viên --"));
        List<NhanVien> listNV = nhanVienDAO.getAll();
        if (listNV != null) {
            for (NhanVien nv : listNV) {
                cbNhanVien.addItem(new ComboItem(nv.getId(), nv.getName()));
            }
        }
    }

    private void loadReport() {
        btnTaiDuLieu.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        ComboItem itemNhom = (ComboItem) cbNhomKhachHang.getSelectedItem();
        ComboItem itemNV = (ComboItem) cbNhanVien.getSelectedItem();

        String nhomId = itemNhom != null ? itemNhom.getId() : "";
        String nhanVienId = itemNV != null ? itemNV.getId() : "";

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateKhachHangTheoNhom(nhomId, nhanVienId);
            }

            @Override
            protected void done() {
                try {
                    JasperPrint print = get();
                    updateReportViewer(print);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BaoCaoKhachHangTheoNhomPanel.this,
                            "Lỗi khi nạp dữ liệu báo cáo: " + ex.getMessage(),
                            "Lỗi Báo Cáo", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnTaiDuLieu.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void updateReportViewer(JasperPrint print) {
        pnlViewerContainer.removeAll();
        if (print != null) {
            currentViewer = new ReportJRViewer(print);
            pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
        } else {
            pnlViewerContainer.add(new JLabel("Không có dữ liệu báo cáo", SwingConstants.CENTER), BorderLayout.CENTER);
        }
        pnlViewerContainer.revalidate();
        pnlViewerContainer.repaint();
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