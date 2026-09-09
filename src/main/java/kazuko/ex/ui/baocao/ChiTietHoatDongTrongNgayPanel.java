package kazuko.ex.ui.baocao;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.system.SUser;
import kazuko.ex.report.service.BaoCaoPhanTichKinhDoanhReportService;
import kazuko.ex.ui.common.DatePickerField;
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

/**
 * Màn hình Báo cáo chi tiết hoạt động trong ngày.
 */
public class ChiTietHoatDongTrongNgayPanel extends JPanel {

    private static final Color TOOLBAR_BACKGROUND = new Color(205, 223, 245);

    private final BaoCaoPhanTichKinhDoanhReportService reportService = new BaoCaoPhanTichKinhDoanhReportService();
    private final UserDAO userDAO = new UserDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    private final DatePickerField fromDate = new DatePickerField();
    private final DatePickerField toDate = new DatePickerField();
    private JComboBox<ComboItem> cbThuNgan;
    private JComboBox<StoreOption> cbCuaHang;
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

    public static class StoreOption {
        private final String id;
        private final String name;
        private final Icon icon;

        public StoreOption(String id, String name, Icon icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Icon getIcon() { return icon; }

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
        debounceTimer = new javax.swing.Timer(300, e -> loadReport());
        debounceTimer.setRepeats(false);

        fromDate.addActionListener(e -> onFilterChanged());
        toDate.addActionListener(e -> onFilterChanged());
        cbThuNgan.addActionListener(e -> onFilterChanged());
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
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnl.setBackground(TOOLBAR_BACKGROUND);
        pnl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(150, 180, 215)));

        Calendar cal = Calendar.getInstance();
        Date toD = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date fromD = cal.getTime();

        pnl.add(new JLabel("Từ ngày:"));
        fromDate.setDate(fromD);
        fromDate.setPreferredSize(new Dimension(140, 26));
        pnl.add(fromDate);

        pnl.add(new JLabel("Đến ngày:"));
        toDate.setDate(toD);
        toDate.setPreferredSize(new Dimension(140, 26));
        pnl.add(toDate);

        pnl.add(new JLabel("Thu ngân:"));
        cbThuNgan = new JComboBox<>();
        cbThuNgan.setPreferredSize(new Dimension(140, 26));
        pnl.add(cbThuNgan);

        pnl.add(new JLabel("Cửa hàng:"));
        cbCuaHang = new JComboBox<>();
        cbCuaHang.setPreferredSize(new Dimension(160, 26));
        cbCuaHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StoreOption opt) {
                    setText(opt.getName());
                    if (opt.getIcon() != null) {
                        setIcon(opt.getIcon());
                    } else {
                        setIcon(IconHelper.getIcon("table.png", 14, 14));
                    }
                }
                return this;
            }
        });
        pnl.add(cbCuaHang);

        btnTaiDuLieu = new JButton("Tải dữ liệu (F5)");
        btnTaiDuLieu.setIcon(IconHelper.getIcon("Refresh.png", 16, 16));
        btnTaiDuLieu.setPreferredSize(new Dimension(135, 28));
        btnTaiDuLieu.addActionListener(e -> loadReport());
        pnl.add(btnTaiDuLieu);

        return pnl;
    }

    private void loadFilterData() {
        cbThuNgan.removeAllItems();
        cbThuNgan.addItem(new ComboItem("", "Tất cả"));
        try {
            List<SUser> users = userDAO.getAll();
            if (users != null) {
                for (SUser u : users) {
                    cbThuNgan.addItem(new ComboItem(u.getUsername(), u.getName() != null && !u.getName().isEmpty() ? u.getName() : u.getUsername()));
                }
            }
        } catch (Exception ignored) {}

        cbCuaHang.removeAllItems();
        cbCuaHang.addItem(new StoreOption("", "-- Tất cả cửa hàng --", IconHelper.getIcon("table.png", 14, 14)));
        try {
            List<CuaHang> stores = cuaHangDAO.getRealStores();
            if (stores != null) {
                for (CuaHang store : stores) {
                    Icon icon = IconHelper.getIcon("table.png", 14, 14);
                    cbCuaHang.addItem(new StoreOption(store.getId(), store.getName(), icon));
                }
            }
        } catch (Exception ignored) {}
    }

    public void loadReport() {
        Date fDate = fromDate.getDate();
        Date tDate = toDate.getDate();

        if (fDate == null || tDate == null) return;

        ComboItem itemThuNgan = (ComboItem) cbThuNgan.getSelectedItem();
        String thuNganId = itemThuNgan != null ? itemThuNgan.getId() : "";

        StoreOption itemStore = (StoreOption) cbCuaHang.getSelectedItem();
        String storeId = itemStore != null ? itemStore.getId() : "";

        btnTaiDuLieu.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateChiTietHoatDongTrongNgay(fDate, tDate, thuNganId, storeId);
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
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }
}
