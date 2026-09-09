package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.report.service.BaoCaoBanHangReportService;
import kazuko.ex.service.ActivityLogService;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;

/** Màn hình tra cứu doanh thu và chi tiết hàng hóa theo hóa đơn. */
public class ThongKeDoanhThuPanel extends JPanel {
    static final List<String> ALL_INVOICE_COLUMNS = List.of(
            "Số phiếu", "Ngày", "Giờ thanh toán", "Khách hàng", "Tiền hàng",
            "Tiền giảm giá", "Đổi trả", "Tổng cộng", "Thanh toán bởi", "Ghi chú",
            "Mã khách", "Tỉ lệ giảm giá", "Diễn giải", "Khách đưa", "Trả lại",
            "Số phiếu đặt hàng", "Giao hàng", "Nhân viên", "Phí vận chuyển", "Thẻ",
            "Mã thẻ trả trước", "Trừ tích lũy", "Địa chỉ", "Điện thoại"
    );
    static final List<String> DEFAULT_INVOICE_COLUMNS = List.of(
            "Số phiếu", "Ngày", "Giờ thanh toán", "Khách hàng", "Tiền hàng",
            "Tiền giảm giá", "Đổi trả", "Tổng cộng", "Thanh toán bởi", "Ghi chú",
            "Mã khách", "Tỉ lệ giảm giá", "Diễn giải", "Khách đưa", "Trả lại",
            "Số phiếu đặt hàng", "Giao hàng", "Nhân viên", "Phí vận chuyển", "Thẻ",
            "Mã thẻ trả trước", "Trừ tích lũy"
    );
    static final List<String> ALL_DETAIL_COLUMNS = List.of(
            "Mã hàng", "Tên mặt hàng", "ĐVT", "Đơn giá", "CK%", "Thành tiền",
            "Bảo hành", "S.lượng", "Ghi chú"
    );
    static final List<String> DEFAULT_DETAIL_COLUMNS = List.of(
            "Mã hàng", "Tên mặt hàng", "ĐVT", "Đơn giá", "CK%", "Thành tiền",
            "Bảo hành", "S.lượng"
    );

    private static final Color TOOLBAR_BACKGROUND = new Color(205, 223, 245);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.##");

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final BaoCaoBanHangReportService reportService = new BaoCaoBanHangReportService();

    private final DatePickerField fromDate = new DatePickerField();
    private final DatePickerField toDate = new DatePickerField();
    private final JComboBox<StoreOption> storeCombo = new JComboBox<>();
    private final JButton refreshButton = new JButton("Refresh (F5)");
    private final JButton printButton = new JButton("In báo cáo");
    private final JLabel statusLabel = new JLabel(" ");

    private final JTable invoiceTable = new JTable();
    private final JTable detailTable = new JTable();
    private List<String> visibleInvoiceColumns = new ArrayList<>(DEFAULT_INVOICE_COLUMNS);
    private List<String> visibleDetailColumns = new ArrayList<>(DEFAULT_DETAIL_COLUMNS);
    private List<DonHang> invoices = new ArrayList<>();
    private SwingWorker<List<DonHang>, Void> loadWorker;
    private long loadSequence;

    public ThongKeDoanhThuPanel() {
        initializeUi();
        loadStores();
        reloadData();
    }

    private void initializeUi() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        Date today = startOfDay(new Date());
        fromDate.setDate(today);
        toDate.setDate(today);
        fromDate.setPreferredSize(new Dimension(145, 26));
        toDate.setPreferredSize(new Dimension(145, 26));

        add(createFilterBar(), BorderLayout.NORTH);
        configureTable(invoiceTable, true);
        configureTable(detailTable, false);
        rebuildInvoiceTable();
        rebuildDetailTable(null);
        installColumnChooser(invoiceTable, true);
        installColumnChooser(detailTable, false);

        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelectedInvoiceDetails();
        });

        JScrollPane invoiceScroll = new JScrollPane(invoiceTable);
        JScrollPane detailScroll = new JScrollPane(detailTable);
        invoiceScroll.getViewport().setBackground(Color.WHITE);
        detailScroll.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, invoiceScroll, detailScroll);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(7);
        splitPane.setBorder(null);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.55));

        JPanel center = new JPanel(new BorderLayout());
        center.add(splitPane, BorderLayout.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        statusLabel.setForeground(new Color(60, 80, 105));
        center.add(statusLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refreshRevenue");
        getActionMap().put("refreshRevenue", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { reloadData(); }
        });
    }

    private JPanel createFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBackground(TOOLBAR_BACKGROUND);
        panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(150, 180, 215)));
        panel.add(new JLabel("Từ:"));
        panel.add(fromDate);
        panel.add(new JLabel("Đến:"));
        panel.add(toDate);

        JButton quickDateButton = new JButton("▼");
        quickDateButton.setMargin(new Insets(1, 6, 1, 6));
        quickDateButton.setToolTipText("Chọn nhanh khoảng ngày");
        quickDateButton.addActionListener(e -> showQuickDateMenu(quickDateButton));
        panel.add(quickDateButton);

        panel.add(new JLabel("Cửa hàng:"));
        storeCombo.setPreferredSize(new Dimension(180, 26));
        panel.add(storeCombo);

        refreshButton.setIcon(IconHelper.getIcon("Refresh.png", 16, 16));
        refreshButton.setPreferredSize(new Dimension(132, 28));
        refreshButton.addActionListener(e -> reloadData());
        panel.add(refreshButton);

        printButton.setIcon(IconHelper.getIcon("print-16.png", 16, 16));
        printButton.setPreferredSize(new Dimension(122, 28));
        printButton.addActionListener(e -> previewReport());
        panel.add(printButton);
        return panel;
    }

    private void showQuickDateMenu(Component owner) {
        JPopupMenu menu = new JPopupMenu();
        addQuickDateItem(menu, "Hôm nay", 0);
        addQuickDateItem(menu, "Hôm qua", -1);
        JMenuItem sevenDays = new JMenuItem("7 ngày gần đây");
        sevenDays.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            toDate.setDate(startOfDay(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, -6);
            fromDate.setDate(startOfDay(cal.getTime()));
            reloadData();
        });
        menu.add(sevenDays);
        JMenuItem currentMonth = new JMenuItem("Tháng này");
        currentMonth.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            toDate.setDate(startOfDay(cal.getTime()));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            fromDate.setDate(startOfDay(cal.getTime()));
            reloadData();
        });
        menu.add(currentMonth);
        menu.show(owner, 0, owner.getHeight());
    }

    private void addQuickDateItem(JPopupMenu menu, String title, int dayOffset) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, dayOffset);
            Date selected = startOfDay(cal.getTime());
            fromDate.setDate(selected);
            toDate.setDate(selected);
            reloadData();
        });
        menu.add(item);
    }

    private void configureTable(JTable table, boolean master) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(180, 200, 220));
        table.setAutoCreateRowSorter(master);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void loadStores() {
        storeCombo.removeAllItems();
        storeCombo.addItem(new StoreOption("", "-- Tất cả cửa hàng --"));
        for (CuaHang store : cuaHangDAO.getRealStores()) {
            storeCombo.addItem(new StoreOption(store.getId(), store.getName()));
        }
    }

    public void reloadData() {
        Date from = fromDate.getDate();
        Date to = toDate.getDate();
        if (from == null || to == null) {
            showWarning("Ngày bắt đầu và ngày kết thúc phải đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (from.after(to)) {
            showWarning("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
            return;
        }

        if (loadWorker != null && !loadWorker.isDone()) loadWorker.cancel(true);
        long sequence = ++loadSequence;
        String storeId = selectedStoreId();
        setLoading(true, "Đang tải dữ liệu doanh thu...");

        loadWorker = new SwingWorker<>() {
            @Override protected List<DonHang> doInBackground() {
                return hoaDonDAO.getDanhSachHoaDon(startOfDay(from), startOfDay(to), storeId, null);
            }

            @Override protected void done() {
                if (sequence != loadSequence) return;
                try {
                    invoices = get();
                    rebuildInvoiceTable();
                    if (!invoices.isEmpty()) invoiceTable.setRowSelectionInterval(0, 0);
                    else rebuildDetailTable(null);
                    statusLabel.setText(invoices.size() + " hóa đơn - Tổng doanh thu: "
                            + NUMBER_FORMAT.format(invoices.stream().mapToDouble(DonHang::getTongCong).sum()));
                } catch (CancellationException ignored) {
                    return;
                } catch (Exception ex) {
                    invoices = new ArrayList<>();
                    rebuildInvoiceTable();
                    rebuildDetailTable(null);
                    statusLabel.setText("Không thể tải dữ liệu doanh thu");
                    showError("Không thể tải thống kê doanh thu", ex);
                } finally {
                    setLoading(false, statusLabel.getText());
                }
            }
        };
        loadWorker.execute();
    }

    private void rebuildInvoiceTable() {
        String[] names = withRowNumber(visibleInvoiceColumns);
        ReadOnlyTableModel model = new ReadOnlyTableModel(names);
        for (int i = 0; i < invoices.size(); i++) {
            DonHang invoice = invoices.get(i);
            Object[] row = new Object[names.length];
            row[0] = i + 1;
            for (int column = 0; column < visibleInvoiceColumns.size(); column++) {
                row[column + 1] = invoiceValue(invoice, visibleInvoiceColumns.get(column));
            }
            model.addRow(row);
        }
        invoiceTable.setModel(model);
        configureWidthsAndRenderers(invoiceTable);
    }

    private Object invoiceValue(DonHang invoice, String column) {
        return switch (column) {
            case "Số phiếu" -> text(invoice.getName());
            case "Ngày" -> invoice.getNgay() != null ? DATE_FORMAT.format(invoice.getNgay()) : "";
            case "Giờ thanh toán" -> text(invoice.getGioThanhToan());
            case "Khách hàng" -> text(invoice.getTenKhach());
            case "Tiền hàng" -> invoice.getTienHang();
            case "Tiền giảm giá" -> invoice.getTienGiamGia();
            case "Đổi trả" -> invoice.getTienDoiTra();
            case "Tổng cộng" -> invoice.getTongCong();
            case "Thanh toán bởi" -> text(invoice.getNguoiThanhToan());
            case "Ghi chú" -> text(invoice.getGhiChu());
            case "Mã khách" -> text(invoice.getMaKhach());
            case "Tỉ lệ giảm giá" -> invoice.getTiLeGiamGia();
            case "Diễn giải" -> text(invoice.getDienGiai());
            case "Khách đưa" -> invoice.getKhachDua();
            case "Trả lại" -> invoice.getTraLai();
            case "Số phiếu đặt hàng" -> text(invoice.getSoPhieuDatHang());
            case "Giao hàng" -> text(invoice.getDiaChiGiaoHang());
            case "Nhân viên" -> text(invoice.getTenNhanVien());
            case "Phí vận chuyển" -> invoice.getPhiVanChuyen();
            case "Thẻ" -> invoice.getTienTheATM();
            case "Mã thẻ trả trước" -> text(invoice.getMaTheTraTruoc());
            case "Trừ tích lũy" -> invoice.getTienTruTichLuy();
            case "Địa chỉ" -> text(invoice.getDiaChiKhach());
            case "Điện thoại" -> text(invoice.getDienThoaiKhach());
            default -> "";
        };
    }

    private void showSelectedInvoiceDetails() {
        int viewRow = invoiceTable.getSelectedRow();
        if (viewRow < 0) {
            rebuildDetailTable(null);
            return;
        }
        int modelRow = invoiceTable.convertRowIndexToModel(viewRow);
        rebuildDetailTable(modelRow >= 0 && modelRow < invoices.size() ? invoices.get(modelRow) : null);
    }

    private void rebuildDetailTable(DonHang invoice) {
        String[] names = withRowNumber(visibleDetailColumns);
        ReadOnlyTableModel model = new ReadOnlyTableModel(names);
        int rowNumber = 1;
        if (invoice != null) {
            rowNumber = addDetails(model, invoice.getChiTietLayHang(), rowNumber, false);
            addDetails(model, invoice.getChiTietTraHang(), rowNumber, true);
        }
        detailTable.setModel(model);
        configureWidthsAndRenderers(detailTable);
    }

    private int addDetails(DefaultTableModel model, List<DonHangChiTiet> details, int rowNumber, boolean returned) {
        if (details == null) return rowNumber;
        for (DonHangChiTiet detail : details) {
            Object[] row = new Object[visibleDetailColumns.size() + 1];
            row[0] = rowNumber++;
            for (int column = 0; column < visibleDetailColumns.size(); column++) {
                row[column + 1] = detailValue(detail, visibleDetailColumns.get(column), returned);
            }
            model.addRow(row);
        }
        return rowNumber;
    }

    static Object detailValue(DonHangChiTiet detail, String column, boolean returned) {
        double sign = returned ? -1.0 : 1.0;
        return switch (column) {
            case "Mã hàng" -> text(detail.getMaHang());
            case "Tên mặt hàng" -> text(detail.getTenHang());
            case "ĐVT" -> text(detail.getTenDonViTinh());
            case "Đơn giá" -> detail.getDonGia();
            case "CK%" -> detail.getTiLeGiamGia();
            case "Thành tiền" -> sign * detail.getThanhTien();
            case "Bảo hành" -> text(detail.getBaoHanh());
            case "S.lượng" -> sign * detail.getSoLuong();
            case "Ghi chú" -> returned ? "[TRẢ HÀNG] " + text(detail.getGhiChu()) : text(detail.getGhiChu());
            default -> "";
        };
    }

    private void configureWidthsAndRenderers(JTable table) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer number = new DefaultTableCellRenderer() {
            @Override protected void setValue(Object value) {
                setText(value instanceof Number ? NUMBER_FORMAT.format(((Number) value).doubleValue()) : text(value));
            }
        };
        number.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            String name = text(tc.getHeaderValue());
            if (i == 0) {
                tc.setPreferredWidth(48);
                tc.setCellRenderer(center);
            } else if (isNumericColumn(name)) {
                tc.setPreferredWidth(name.contains("Tỉ lệ") || "CK%".equals(name) || "S.lượng".equals(name) ? 72 : 105);
                tc.setCellRenderer(number);
            } else if ("Ngày".equals(name) || "Giờ thanh toán".equals(name) || "ĐVT".equals(name)) {
                tc.setPreferredWidth("Ngày".equals(name) ? 95 : 90);
                tc.setCellRenderer(center);
            } else if ("Tên mặt hàng".equals(name) || "Khách hàng".equals(name) || "Diễn giải".equals(name) || "Ghi chú".equals(name)) {
                tc.setPreferredWidth(160);
            } else {
                tc.setPreferredWidth(115);
            }
        }
    }

    private static boolean isNumericColumn(String name) {
        return List.of("Tiền hàng", "Tiền giảm giá", "Đổi trả", "Tổng cộng", "Tỉ lệ giảm giá",
                "Khách đưa", "Trả lại", "Phí vận chuyển", "Thẻ", "Trừ tích lũy",
                "Đơn giá", "CK%", "Thành tiền", "S.lượng").contains(name);
    }

    private void installColumnChooser(JTable table, boolean invoiceChooser) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem chooseItem = new JMenuItem("Chọn cột hiển thị...");
        chooseItem.setIcon(IconHelper.getIcon("table_column.png", 14, 14));
        chooseItem.addActionListener(e -> openColumnChooser(invoiceChooser));
        popup.add(chooseItem);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            private void show(MouseEvent e) { popup.show(e.getComponent(), e.getX(), e.getY()); }
            @Override public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) show(e); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) show(e); }
        });
    }

    private void openColumnChooser(boolean invoiceChooser) {
        List<String> current = invoiceChooser ? visibleInvoiceColumns : visibleDetailColumns;
        List<String> all = invoiceChooser ? ALL_INVOICE_COLUMNS : ALL_DETAIL_COLUMNS;
        List<String> defaults = invoiceChooser ? DEFAULT_INVOICE_COLUMNS : DEFAULT_DETAIL_COLUMNS;
        ChonCotHienThiDialog dialog = new ChonCotHienThiDialog(
                SwingUtilities.getWindowAncestor(this), current, all, defaults);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) return;
        if (dialog.getResultVisibleColumns().isEmpty()) {
            showWarning("Phải giữ lại ít nhất một cột hiển thị.");
            return;
        }
        if (invoiceChooser) {
            visibleInvoiceColumns = new ArrayList<>(dialog.getResultVisibleColumns());
            rebuildInvoiceTable();
            if (!invoices.isEmpty()) invoiceTable.setRowSelectionInterval(0, 0);
        } else {
            visibleDetailColumns = new ArrayList<>(dialog.getResultVisibleColumns());
            showSelectedInvoiceDetails();
        }
    }

    private void previewReport() {
        Date from = fromDate.getDate();
        Date to = toDate.getDate();
        if (from == null || to == null || from.after(to)) {
            showWarning("Vui lòng chọn khoảng ngày hợp lệ trước khi in báo cáo.");
            return;
        }
        String storeId = selectedStoreId();
        printButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<JasperPrint, Void>() {
            @Override protected JasperPrint doInBackground() throws Exception {
                return reportService.generateBanHangTheoNgay(startOfDay(from), startOfDay(to), null, null, storeId);
            }

            @Override protected void done() {
                try {
                    JasperPrint print = get();
                    JDialog preview = new JDialog(SwingUtilities.getWindowAncestor(ThongKeDoanhThuPanel.this),
                            "Báo cáo thống kê doanh thu", Dialog.ModalityType.MODELESS);
                    preview.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    preview.setLayout(new BorderLayout());
                    preview.add(new JRViewer(print), BorderLayout.CENTER);
                    preview.setSize(1050, 720);
                    preview.setLocationRelativeTo(ThongKeDoanhThuPanel.this);
                    preview.setVisible(true);
                    ActivityLogService.log("Thống kê doanh thu", "XEM/IN BÁO CÁO", "",
                            "Từ " + DATE_FORMAT.format(from) + " đến " + DATE_FORMAT.format(to));
                } catch (Exception ex) {
                    showError("Không thể tạo báo cáo doanh thu", ex);
                } finally {
                    printButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading, String text) {
        refreshButton.setEnabled(!loading);
        storeCombo.setEnabled(!loading);
        fromDate.setEnabled(!loading);
        toDate.setEnabled(!loading);
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        statusLabel.setText(text != null ? text : " ");
    }

    private String selectedStoreId() {
        StoreOption selected = (StoreOption) storeCombo.getSelectedItem();
        return selected != null ? selected.id : "";
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Thống kê doanh thu", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        JOptionPane.showMessageDialog(this, message + ": " + cause.getMessage(),
                "Thống kê doanh thu", JOptionPane.ERROR_MESSAGE);
    }

    private static Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static String[] withRowNumber(List<String> columns) {
        String[] result = new String[columns.size() + 1];
        result[0] = "";
        for (int i = 0; i < columns.size(); i++) result[i + 1] = columns.get(i);
        return result;
    }

    private static String text(Object value) {
        return value != null ? value.toString() : "";
    }

    private static final class StoreOption {
        private final String id;
        private final String name;

        private StoreOption(String id, String name) {
            this.id = id != null ? id : "";
            this.name = name != null ? name : "";
        }

        @Override public String toString() { return name; }
    }

    private static final class ReadOnlyTableModel extends DefaultTableModel {
        private ReadOnlyTableModel(String[] columns) { super(columns, 0); }
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Integer.class;
            return isNumericColumn(getColumnName(columnIndex)) ? Double.class : String.class;
        }
    }
}
