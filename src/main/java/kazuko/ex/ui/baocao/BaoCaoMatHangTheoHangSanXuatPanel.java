package kazuko.ex.ui.baocao;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.report.service.BaoCaoMatHangReportService;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class BaoCaoMatHangTheoHangSanXuatPanel extends JPanel {

    private final BaoCaoMatHangReportService reportService = new BaoCaoMatHangReportService();
    private final NhomMatHangDAO nhomDAO = new NhomMatHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private JComboBox<ComboItem> cbNhomMatHang;
    private JComboBox<String> cbHangSanXuat;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public BaoCaoMatHangTheoHangSanXuatPanel() {
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

    private void setupAutoReloadListeners() {
        debounceTimer = new javax.swing.Timer(250, e -> loadReport());
        debounceTimer.setRepeats(false);

        cbNhomMatHang.addActionListener(e -> onFilterChanged());
        cbHangSanXuat.addActionListener(e -> onFilterChanged());
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

        pnl.add(new JLabel("Nhóm mặt hàng:"));
        cbNhomMatHang = new JComboBox<>();
        cbNhomMatHang.setPreferredSize(new Dimension(180, 26));
        pnl.add(cbNhomMatHang);

        pnl.add(new JLabel("Hãng sản xuất:"));
        cbHangSanXuat = new JComboBox<>();
        cbHangSanXuat.setPreferredSize(new Dimension(160, 26));
        pnl.add(cbHangSanXuat);

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
        cbNhomMatHang.removeAllItems();
        cbNhomMatHang.addItem(new ComboItem("", "-- Tất cả nhóm mặt hàng --"));
        List<NhomMatHang> listNhom = nhomDAO.getAll();
        if (listNhom != null) {
            for (NhomMatHang nhom : listNhom) {
                cbNhomMatHang.addItem(new ComboItem(nhom.getId(), nhom.getName()));
            }
        }

        cbHangSanXuat.removeAllItems();
        cbHangSanXuat.addItem("-- Tất cả hãng sản xuất --");
        List<String> listHsx = matHangDAO.getDistinctHangSanXuat();
        if (listHsx != null) {
            for (String hsx : listHsx) {
                cbHangSanXuat.addItem(hsx);
            }
        }
    }

    private void loadReport() {
        btnTaiDuLieu.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        ComboItem itemNhom = (ComboItem) cbNhomMatHang.getSelectedItem();
        String nhomId = itemNhom != null ? itemNhom.getId() : "";

        String selectedHsx = (String) cbHangSanXuat.getSelectedItem();
        String hsx = (selectedHsx != null && !selectedHsx.startsWith("--")) ? selectedHsx : "";

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateMatHangTheoHangSanXuat(nhomId, hsx);
            }

            @Override
            protected void done() {
                try {
                    JasperPrint print = get();
                    updateReportViewer(print);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BaoCaoMatHangTheoHangSanXuatPanel.this,
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
            currentViewer = new JRViewer(print);
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