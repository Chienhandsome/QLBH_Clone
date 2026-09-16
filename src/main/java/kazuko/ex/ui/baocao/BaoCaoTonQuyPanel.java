package kazuko.ex.ui.baocao;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.TaiKhoanNganHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.TaiKhoanNganHang;
import kazuko.ex.report.service.BaoCaoQuyReportService;
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

public class BaoCaoTonQuyPanel extends JPanel {

    private final BaoCaoQuyReportService reportService = new BaoCaoQuyReportService();
    private final TaiKhoanNganHangDAO taiKhoanDAO = new TaiKhoanNganHangDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    private JSpinner spFromDate;
    private JSpinner spToDate;
    private JComboBox<ComboItem> cbLoaiQuy;
    private JComboBox<ComboItem> cbTaiKhoan;
    private JComboBox<ComboItem> cbCuaHang;
    private JButton btnTaiDuLieu;

    private JPanel pnlViewerContainer;
    private JRViewer currentViewer;

    private boolean isInitializing = true;
    private javax.swing.Timer debounceTimer;

    public BaoCaoTonQuyPanel() {
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

        spFromDate.addChangeListener(e -> onFilterChanged());
        spToDate.addChangeListener(e -> onFilterChanged());
        cbLoaiQuy.addActionListener(e -> onFilterChanged());
        cbTaiKhoan.addActionListener(e -> onFilterChanged());
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

        // Từ ngày
        JLabel lblFrom = new JLabel("Từ ngày");
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

        // Đến ngày
        JLabel lblTo = new JLabel("Đến ngày");
        lblTo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTo);

        spToDate = new JSpinner(new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH));
        spToDate.setEditor(new JSpinner.DateEditor(spToDate, "dd/MM/yyyy"));
        spToDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spToDate.setPreferredSize(new Dimension(105, 24));
        pnl.add(spToDate);

        // Loại quỹ
        JLabel lblLoaiQuy = new JLabel("Loại quỹ");
        lblLoaiQuy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblLoaiQuy);

        cbLoaiQuy = new JComboBox<>();
        cbLoaiQuy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLoaiQuy.setPreferredSize(new Dimension(110, 24));
        cbLoaiQuy.setBackground(Color.WHITE);
        cbLoaiQuy.addItem(new ComboItem("ALL", "-- Tất cả --"));
        cbLoaiQuy.addItem(new ComboItem("TIEN_MAT", "Tiền mặt"));
        cbLoaiQuy.addItem(new ComboItem("NGAN_HANG", "Ngân hàng"));
        pnl.add(cbLoaiQuy);

        // Tài khoản
        JLabel lblTaiKhoan = new JLabel("Tài khoản");
        lblTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblTaiKhoan);

        cbTaiKhoan = new JComboBox<>();
        cbTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbTaiKhoan.setPreferredSize(new Dimension(140, 24));
        cbTaiKhoan.setBackground(Color.WHITE);
        pnl.add(cbTaiKhoan);

        // Cửa hàng
        JLabel lblCuaHang = new JLabel("Cửa hàng");
        lblCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblCuaHang);

        cbCuaHang = new JComboBox<>();
        cbCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCuaHang.setPreferredSize(new Dimension(140, 24));
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
        cbTaiKhoan.removeAllItems();
        cbTaiKhoan.addItem(new ComboItem("ALL", "-- Tất cả tài khoản --"));
        List<TaiKhoanNganHang> listTK = taiKhoanDAO.getAllActive();
        for (TaiKhoanNganHang tk : listTK) {
            cbTaiKhoan.addItem(new ComboItem(tk.getId(), tk.getName()));
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

        ComboItem itemLoaiQuy = (ComboItem) cbLoaiQuy.getSelectedItem();
        String loaiQuy = itemLoaiQuy != null ? itemLoaiQuy.id : "ALL";

        ComboItem itemTK = (ComboItem) cbTaiKhoan.getSelectedItem();
        String tkId = itemTK != null ? itemTK.id : "ALL";

        ComboItem itemCH = (ComboItem) cbCuaHang.getSelectedItem();
        String chId = itemCH != null ? itemCH.id : "ALL";

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<JasperPrint, Void> worker = new SwingWorker<>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return reportService.generateBaoCaoTonQuy(fromDate, toDate, loaiQuy, tkId, chId);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    JasperPrint print = get();
                    pnlViewerContainer.removeAll();
                    currentViewer = new ReportJRViewer(print);
                    currentViewer.setZoomRatio(1.0f);
                    pnlViewerContainer.add(currentViewer, BorderLayout.CENTER);
                    pnlViewerContainer.revalidate();
                    pnlViewerContainer.repaint();
                } catch (Exception e) {
                    pnlViewerContainer.removeAll();
                    JLabel lblError = new JLabel("Lỗi tạo báo cáo: " + e.getMessage(), SwingConstants.CENTER);
                    lblError.setForeground(Color.RED);
                    pnlViewerContainer.add(lblError, BorderLayout.CENTER);
                    pnlViewerContainer.revalidate();
                    pnlViewerContainer.repaint();
                    e.printStackTrace();
                }
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