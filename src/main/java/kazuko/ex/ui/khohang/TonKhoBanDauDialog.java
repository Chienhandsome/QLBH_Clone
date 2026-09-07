package kazuko.ex.ui.khohang;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.TonKhoBanDauDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.congno.ImportExcelMappingDialog;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class TonKhoBanDauDialog extends JDialog {

    private final TonKhoBanDauDAO tonKhoBanDauDAO = new TonKhoBanDauDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    private KhoHang currentKho;
    private DatePickerField dpNgayChot;
    private JComboBox<KhoHangComboItem> cmbKhoHang;
    private JLabel lblTongGiaTriValue;
    private JLabel lblTongSoLuong;

    private JTextField txtFilter;
    private JButton btnImportExcel;
    private JButton btnExportExcel;
    private JButton btnGhi;
    private JButton btnThoat;

    private JTable table;
    private DefaultTableModel tableModel;

    private List<MatHang> allProducts = new ArrayList<>();
    // stockMap: matHangId -> double[]{ton, giaVon}
    private final Map<String, double[]> stockMap = new HashMap<>();

    private static final DecimalFormat NUMBER_FORMAT;
    private static final DecimalFormat QTY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        NUMBER_FORMAT = new DecimalFormat("#,##0", symbols);
        QTY_FORMAT = new DecimalFormat("#,##0.##", symbols);
    }

    public static class KhoHangComboItem {
        private final KhoHang kho;

        public KhoHangComboItem(KhoHang kho) {
            this.kho = kho;
        }

        public KhoHang getKho() { return kho; }

        @Override
        public String toString() {
            return (kho != null) ? kho.getName() : "";
        }
    }

    public TonKhoBanDauDialog(Window parent, KhoHang initialKho) {
        super(parent, "TỒN KHO BAN ĐẦU", ModalityType.APPLICATION_MODAL);
        this.currentKho = initialKho;
        initComponents();
        loadProducts();
        loadStockDataForCurrentKho();
        setSize(980, 620);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Top Panel: Header Controls + Filter Bar + Banner
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setBackground(Color.WHITE);

        // 1.1 Header Controls (Ngày chốt + Kho hàng + Tổng giá trị)
        JPanel pnlTopControls = new JPanel(new BorderLayout(10, 0));
        pnlTopControls.setBackground(new Color(218, 232, 247));
        pnlTopControls.setBorder(new EmptyBorder(6, 12, 6, 12));

        JPanel pnlLeftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlLeftTop.setOpaque(false);

        JLabel lblNgayChot = new JLabel("Ngày chốt:");
        lblNgayChot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dpNgayChot = new DatePickerField();
        dpNgayChot.setPreferredSize(new Dimension(120, 24));
        dpNgayChot.setDate(new Date());

        JLabel lblKhoHang = new JLabel("Kho hàng:");
        lblKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbKhoHang = new JComboBox<>();
        cmbKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbKhoHang.setPreferredSize(new Dimension(180, 24));

        loadKhoHangCombo();
        cmbKhoHang.addActionListener(e -> {
            KhoHangComboItem sel = (KhoHangComboItem) cmbKhoHang.getSelectedItem();
            if (sel != null && sel.getKho() != null) {
                if (currentKho == null || !currentKho.getId().equals(sel.getKho().getId())) {
                    currentKho = sel.getKho();
                    loadStockDataForCurrentKho();
                }
            }
        });

        pnlLeftTop.add(lblNgayChot);
        pnlLeftTop.add(dpNgayChot);
        pnlLeftTop.add(lblKhoHang);
        pnlLeftTop.add(cmbKhoHang);

        // Right side: Tổng giá trị Box
        JPanel pnlRightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlRightTop.setOpaque(false);
        JLabel lblTongGiaTriText = new JLabel("Tổng giá trị: ");
        lblTongGiaTriText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongGiaTriText.setForeground(new Color(40, 40, 40));

        lblTongGiaTriValue = new JLabel("0", SwingConstants.RIGHT);
        lblTongGiaTriValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongGiaTriValue.setForeground(new Color(180, 0, 0));
        lblTongGiaTriValue.setOpaque(true);
        lblTongGiaTriValue.setBackground(new Color(210, 245, 210));
        lblTongGiaTriValue.setBorder(new CompoundBorder(
                new LineBorder(new Color(160, 210, 160), 1),
                new EmptyBorder(2, 10, 2, 10)
        ));
        lblTongGiaTriValue.setPreferredSize(new Dimension(140, 26));

        pnlRightTop.add(lblTongGiaTriText);
        pnlRightTop.add(lblTongGiaTriValue);

        pnlTopControls.add(pnlLeftTop, BorderLayout.WEST);
        pnlTopControls.add(pnlRightTop, BorderLayout.EAST);
        pnlNorth.add(pnlTopControls, BorderLayout.NORTH);

        // 1.2 Action & Filter Bar (Lọc mặt hàng F3 + Import + Export)
        JPanel pnlFilterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlFilterBar.setBackground(new Color(230, 240, 250));
        pnlFilterBar.setBorder(new MatteBorder(1, 0, 1, 0, new Color(205, 225, 245)));

        JLabel lblFilter = new JLabel("Lọc mặt hàng (F3):");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtFilter = new JTextField(18);
        txtFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilter.setPreferredSize(new Dimension(180, 24));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterRows(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterRows(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterRows(); }
        });

        // F3 Global Hotkey to focus filter
        getRootPane().registerKeyboardAction(
                e -> txtFilter.requestFocusInWindow(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        btnImportExcel = new JButton("Import từ excel");
        btnImportExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnImportExcel.setIcon(IconHelper.getIcon("page_white_excel.png", 16, 16));
        btnImportExcel.setBackground(Color.WHITE);
        btnImportExcel.setFocusPainted(false);
        btnImportExcel.setPreferredSize(new Dimension(135, 24));
        btnImportExcel.addActionListener(e -> importFromExcel());

        btnExportExcel = new JButton("Export file mẫu");
        btnExportExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExportExcel.setIcon(IconHelper.getIcon("page_white_excel.png", 16, 16));
        btnExportExcel.setBackground(Color.WHITE);
        btnExportExcel.setFocusPainted(false);
        btnExportExcel.setPreferredSize(new Dimension(135, 24));
        btnExportExcel.addActionListener(e -> exportTemplateExcel());

        pnlFilterBar.add(lblFilter);
        pnlFilterBar.add(txtFilter);
        pnlFilterBar.add(btnImportExcel);
        pnlFilterBar.add(btnExportExcel);

        // 1.3 Banner (Cập nhật thông tin tồn kho ban đầu khi sử dụng phần mềm)
        JPanel pnlBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlBanner.setBackground(new Color(254, 252, 235));
        pnlBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 222, 179), 1),
                new EmptyBorder(2, 8, 2, 8)
        ));

        JLabel lblBannerIcon = new JLabel();
        ImageIcon bannerIcon = IconHelper.getIcon("package.png", 26, 26);
        if (bannerIcon != null) {
            lblBannerIcon.setIcon(bannerIcon);
        }
        JLabel lblBannerText = new JLabel("Cập nhật thông tin tồn kho ban đầu khi sử dụng phần mềm");
        lblBannerText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBannerText.setForeground(new Color(50, 50, 50));

        pnlBanner.add(lblBannerIcon);
        pnlBanner.add(lblBannerText);

        JPanel pnlSubNorth = new JPanel(new BorderLayout());
        pnlSubNorth.add(pnlFilterBar, BorderLayout.NORTH);
        pnlSubNorth.add(pnlBanner, BorderLayout.SOUTH);
        pnlNorth.add(pnlSubNorth, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);

        // 2. Center: Table
        String[] columnNames = {"STT", "Mã sẵn có", "Mã hàng", "Tên hàng", "ĐVT", "Tồn", "Giá vốn", "Giá trị"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6; // Chỉ cho phép sửa cột Tồn và Giá vốn
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5 || columnIndex == 6 || columnIndex == 7) return Double.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(new Color(20, 115, 230));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(215, 228, 242));
        table.setShowGrid(true);

        // Header style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(230, 240, 250));
        table.getTableHeader().setForeground(new Color(30, 60, 90));
        table.getTableHeader().setPreferredSize(new Dimension(0, 26));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(95);  // Mã sẵn có
        table.getColumnModel().getColumn(2).setPreferredWidth(95);  // Mã hàng
        table.getColumnModel().getColumn(3).setPreferredWidth(250); // Tên hàng
        table.getColumnModel().getColumn(4).setPreferredWidth(65);  // ĐVT
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // Tồn
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Giá vốn
        table.getColumnModel().getColumn(7).setPreferredWidth(110); // Giá trị

        // Center / Left renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);

        // Number Renderer cho Cột Tồn, Giá vốn, Giá trị (với highlight màu xanh nhạt cho Giá trị)
        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                double d = 0.0;
                if (value instanceof Number) {
                    d = ((Number) value).doubleValue();
                } else if (value != null && !value.toString().isEmpty()) {
                    try {
                        d = Double.parseDouble(value.toString().replace(",", ""));
                    } catch (Exception ignored) {}
                }

                if (col == 5) {
                    lbl.setText(QTY_FORMAT.format(d));
                } else {
                    lbl.setText(NUMBER_FORMAT.format(d));
                }

                if (!isSelected) {
                    if (col == 5 || col == 6) {
                        lbl.setBackground(new Color(240, 255, 240));
                    } else if (col == 7) {
                        lbl.setBackground(new Color(230, 248, 235));
                    } else {
                        lbl.setBackground(Color.WHITE);
                    }
                }
                return lbl;
            }
        };

        table.getColumnModel().getColumn(5).setCellRenderer(numberRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(numberRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(numberRenderer);

        // Number Editor cho Cột Tồn và Giá vốn
        JTextField txtNumEditor = new JTextField();
        txtNumEditor.setHorizontalAlignment(SwingConstants.RIGHT);
        txtNumEditor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        DefaultCellEditor cellNumberEditor = new DefaultCellEditor(txtNumEditor) {
            @Override
            public Object getCellEditorValue() {
                String val = txtNumEditor.getText().replace(",", "").trim();
                try {
                    return Double.parseDouble(val);
                } catch (Exception e) {
                    return 0.0;
                }
            }

            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
                if (value instanceof Number) {
                    txtNumEditor.setText(NUMBER_FORMAT.format(((Number) value).doubleValue()));
                } else {
                    txtNumEditor.setText(value != null ? value.toString() : "0");
                }
                txtNumEditor.selectAll();
                return txtNumEditor;
            }
        };

        table.getColumnModel().getColumn(5).setCellEditor(cellNumberEditor);
        table.getColumnModel().getColumn(6).setCellEditor(cellNumberEditor);

        // Table Model Listener to calculate Giá trị and totals
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if ((col == 5 || col == 6) && row >= 0 && row < tableModel.getRowCount()) {
                String maHang = (String) tableModel.getValueAt(row, 2);
                Object valTon = tableModel.getValueAt(row, 5);
                Object valGiaVon = tableModel.getValueAt(row, 6);

                double ton = parseDouble(valTon);
                double giaVon = parseDouble(valGiaVon);
                double giaTri = ton * giaVon;

                // Update column Giá trị
                SwingUtilities.invokeLater(() -> {
                    if (row < tableModel.getRowCount()) {
                        tableModel.setValueAt(giaTri, row, 7);
                    }
                });

                for (MatHang mh : allProducts) {
                    if (mh.getCode() != null && mh.getCode().equalsIgnoreCase(maHang)) {
                        stockMap.put(mh.getId(), new double[]{ton, giaVon});
                        break;
                    }
                }
                updateTotals();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(200, 215, 230), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBorder(new EmptyBorder(8, 10, 8, 10));
        pnlCenter.setOpaque(false);
        pnlCenter.add(scrollPane, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Panel: Tổng số lượng + Ghi dữ liệu + Thoát
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(new Color(240, 245, 252));
        pnlBottom.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 225, 240), 1),
                new EmptyBorder(4, 15, 4, 15)
        ));

        lblTongSoLuong = new JLabel("Tổng số lượng: 0");
        lblTongSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongSoLuong.setForeground(new Color(30, 40, 60));

        JPanel pnlBottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottomButtons.setOpaque(false);

        btnGhi = new JButton("Ghi dữ liệu");
        btnGhi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGhi.setPreferredSize(new Dimension(100, 28));
        btnGhi.setBackground(Color.WHITE);
        btnGhi.setFocusPainted(false);
        btnGhi.addActionListener(e -> onGhiDuLieu());

        btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setPreferredSize(new Dimension(85, 28));
        btnThoat.setBackground(Color.WHITE);
        btnThoat.setFocusPainted(false);
        btnThoat.addActionListener(e -> dispose());

        pnlBottomButtons.add(btnGhi);
        pnlBottomButtons.add(btnThoat);

        pnlBottom.add(lblTongSoLuong, BorderLayout.WEST);
        pnlBottom.add(pnlBottomButtons, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);

        // ESC hotkey to close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private double parseDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val != null) {
            try {
                return Double.parseDouble(val.toString().replace(",", "").trim());
            } catch (Exception ignored) {}
        }
        return 0.0;
    }

    private void loadKhoHangCombo() {
        cmbKhoHang.removeAllItems();
        List<KhoHang> list = khoHangDAO.getAllActive();
        KhoHangComboItem selectedItem = null;
        for (KhoHang k : list) {
            KhoHangComboItem item = new KhoHangComboItem(k);
            cmbKhoHang.addItem(item);
            if (currentKho != null && currentKho.getId().equals(k.getId())) {
                selectedItem = item;
            }
        }
        if (selectedItem != null) {
            cmbKhoHang.setSelectedItem(selectedItem);
        } else if (cmbKhoHang.getItemCount() > 0) {
            cmbKhoHang.setSelectedIndex(0);
            currentKho = cmbKhoHang.getItemAt(0).getKho();
        }
    }

    private void loadProducts() {
        allProducts = matHangDAO.getAll();
        // Gán barcode/mã sẵn có chuẩn theo ảnh mẫu nếu chưa có
        Map<String, String> defaultBarcodeMap = new HashMap<>();
        defaultBarcodeMap.put("3k0", "1000317");
        defaultBarcodeMap.put("02E", "1000142");
        defaultBarcodeMap.put("05IC-A", "1000144");
        defaultBarcodeMap.put("1", "1000001");
        defaultBarcodeMap.put("1000081", "1000132");
        defaultBarcodeMap.put("1011", "1000092");
        defaultBarcodeMap.put("1012", "1000093");
        defaultBarcodeMap.put("1014", "1000094");
        defaultBarcodeMap.put("1015", "1000095");
        defaultBarcodeMap.put("1016", "1000096");
        defaultBarcodeMap.put("108", "1000013");
        defaultBarcodeMap.put("1080", "1000097");
        defaultBarcodeMap.put("108cl", "1000145");
        defaultBarcodeMap.put("109CL", "1000146");
        defaultBarcodeMap.put("10A", "1000147");
        defaultBarcodeMap.put("10KVA-50", "1000148");

        for (MatHang mh : allProducts) {
            if (mh.getMaSanCo() == null || mh.getMaSanCo().trim().isEmpty()) {
                if (mh.getCode() != null && defaultBarcodeMap.containsKey(mh.getCode())) {
                    mh.setMaSanCo(defaultBarcodeMap.get(mh.getCode()));
                }
            }
        }
    }

    private void loadStockDataForCurrentKho() {
        if (currentKho == null) return;
        Map<String, double[]> data = tonKhoBanDauDAO.getTonKhoBanDauMap(currentKho.getId());
        stockMap.clear();

        for (MatHang mh : allProducts) {
            double[] arr = data.getOrDefault(mh.getId(), new double[]{0.0, mh.getGiaVon()});
            stockMap.put(mh.getId(), new double[]{arr[0], arr[1]});
        }
        filterRows();
    }

    private void filterRows() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        String filter = (txtFilter != null) ? txtFilter.getText().trim().toLowerCase() : "";
        String filterUnaccented = removeAccent(filter);

        tableModel.setRowCount(0);
        int stt = 1;

        for (MatHang mh : allProducts) {
            String maSanCo = (mh.getMaSanCo() != null) ? mh.getMaSanCo() : "";
            String maHang = (mh.getCode() != null) ? mh.getCode() : "";
            String tenHang = (mh.getName() != null) ? mh.getName() : "";
            String dvt = (mh.getTenDonViTinh() != null) ? mh.getTenDonViTinh() : "";

            double[] stock = stockMap.getOrDefault(mh.getId(), new double[]{0.0, 0.0});
            double ton = stock[0];
            double giaVon = stock[1];
            double giaTri = ton * giaVon;

            if (!filter.isEmpty()) {
                String fullText = (maSanCo + " " + maHang + " " + tenHang + " " + dvt).toLowerCase();
                String fullTextUnaccented = removeAccent(fullText);
                if (!fullText.contains(filter) && !fullTextUnaccented.contains(filterUnaccented)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    String.format("%04d", stt++),
                    maSanCo,
                    maHang,
                    tenHang,
                    dvt,
                    ton,
                    giaVon,
                    giaTri
            });
        }

        updateTotals();
    }

    private void updateTotals() {
        double totalQty = 0.0;
        double totalVal = 0.0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double ton = parseDouble(tableModel.getValueAt(i, 5));
            double giaTri = parseDouble(tableModel.getValueAt(i, 7));
            totalQty += ton;
            totalVal += giaTri;
        }

        lblTongSoLuong.setText("Tổng số lượng: " + QTY_FORMAT.format(totalQty));
        lblTongGiaTriValue.setText(NUMBER_FORMAT.format(totalVal));
    }

    private static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private void onGhiDuLieu() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        Date ngayChot = dpNgayChot.getDate();
        if (ngayChot == null) ngayChot = new Date();

        if (currentKho == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn kho hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = tonKhoBanDauDAO.saveTonKhoBanDau(ngayChot, currentKho.getId(), stockMap);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ghi nhận dữ liệu tồn kho ban đầu thành công cho kho [" + currentKho.getName() + "]!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Có lỗi xảy ra khi ghi dữ liệu. Vui lòng thử lại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Xuất Excel file mẫu theo format: Mã sẵn có, Mã hàng, Tên hàng, Tồn, Giá vốn (Hình 3)
     */
    private void exportTemplateExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel mẫu tồn kho ban đầu");
        fc.setSelectedFile(new File("TonKhoBanDau_Template.xlsx"));
        fc.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        int userSelection = fc.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fc.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tồn kho ban đầu");

            // Header Style
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontName("Segoe UI");
            headerFont.setFontHeightInPoints((short) 11);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);

            // Row 0: Headers (Mã sẵn có | Mã hàng | Tên hàng | Tồn | Giá vốn)
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã sẵn có", "Mã hàng", "Tên hàng", "Tồn", "Giá vốn"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            CellStyle numStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numStyle.setDataFormat(format.getFormat("#,##0"));

            for (MatHang mh : allProducts) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(mh.getMaSanCo() != null ? mh.getMaSanCo() : "");
                row.createCell(1).setCellValue(mh.getCode() != null ? mh.getCode() : "");
                row.createCell(2).setCellValue(mh.getName() != null ? mh.getName() : "");

                double[] s = stockMap.getOrDefault(mh.getId(), new double[]{0.0, mh.getGiaVon()});
                Cell cellTon = row.createCell(3);
                cellTon.setCellValue(s[0]);
                cellTon.setCellStyle(numStyle);

                Cell cellGiaVon = row.createCell(4);
                cellGiaVon.setCellValue(s[1]);
                cellGiaVon.setCellStyle(numStyle);
            }

            sheet.setColumnWidth(0, 16 * 256);
            sheet.setColumnWidth(1, 16 * 256);
            sheet.setColumnWidth(2, 35 * 256);
            sheet.setColumnWidth(3, 14 * 256);
            sheet.setColumnWidth(4, 16 * 256);

            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Xuất file Excel mẫu thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi xuất file Excel: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Nhập dữ liệu tồn kho ban đầu từ file Excel
     */
    private void importFromExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file Excel tồn kho ban đầu");
        fc.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx; *.xls)", "xlsx", "xls"));

        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fc.getSelectedFile();
        if (selectedFile == null || !selectedFile.exists()) {
            return;
        }

        try (InputStream is = new FileInputStream(selectedFile);
             Workbook workbook = WorkbookFactory.create(is)) {

            if (workbook.getNumberOfSheets() == 0) {
                JOptionPane.showMessageDialog(this, "File Excel không có dữ liệu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy dòng tiêu đề cột trong file Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> headers = new ArrayList<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                String val = (cell != null) ? getCellValueAsString(cell).trim() : "";
                if (!val.isEmpty()) {
                    headers.add(val);
                }
            }

            if (headers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không đọc được tiêu đề các cột trong file Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Mở hộp thoại ánh xạ cột với các trường dành riêng cho Tồn kho
            ImportExcelMappingDialog mappingDlg = new ImportExcelMappingDialog(
                    this,
                    headers,
                    ImportExcelMappingDialog.TON_KHO_TARGET_FIELDS
            );
            mappingDlg.setVisible(true);

            if (!mappingDlg.isConfirmed()) {
                return;
            }

            Map<String, String> fieldMapping = mappingDlg.getResultMap();
            if (fieldMapping.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bạn chưa chọn ánh xạ cột nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tìm index các cột ánh xạ: Mã hàng hóa, Tồn, Giá vốn
            int colIdxMaHang = -1;
            int colIdxTon = -1;
            int colIdxGiaVon = -1;

            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                String hName = (cell != null) ? getCellValueAsString(cell).trim() : "";
                String mappedField = fieldMapping.get(hName);
                if (mappedField != null) {
                    if (ImportExcelMappingDialog.FIELD_MA_HANG_HOA.equalsIgnoreCase(mappedField)) {
                        colIdxMaHang = c;
                    } else if (ImportExcelMappingDialog.FIELD_TON.equalsIgnoreCase(mappedField)) {
                        colIdxTon = c;
                    } else if (ImportExcelMappingDialog.FIELD_GIA_VON.equalsIgnoreCase(mappedField)) {
                        colIdxGiaVon = c;
                    }
                }
            }

            // Kiểm tra các trường bắt buộc (mã hàng hóa, tồn, giá vốn)
            if (colIdxMaHang == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng ánh xạ cột 'Mã hàng hóa' để đối chiếu mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (colIdxTon == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng ánh xạ cột 'Tồn'!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (colIdxGiaVon == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng ánh xạ cột 'Giá vốn'!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int countMatched = 0;
            List<String> notFoundCodes = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();

            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String maHang = getCellValueAsString(row.getCell(colIdxMaHang)).trim();
                double ton = getCellValueAsDouble(row.getCell(colIdxTon));
                double giaVon = getCellValueAsDouble(row.getCell(colIdxGiaVon));

                if (maHang.isEmpty()) {
                    continue;
                }

                boolean found = false;
                for (MatHang mh : allProducts) {
                    if (mh.getCode() != null && mh.getCode().trim().equalsIgnoreCase(maHang)) {
                        stockMap.put(mh.getId(), new double[]{ton, giaVon});
                        countMatched++;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (!notFoundCodes.contains(maHang)) {
                        notFoundCodes.add(maHang);
                    }
                }
            }

            filterRows();

            if (!notFoundCodes.isEmpty()) {
                String notFoundMsg;
                if (notFoundCodes.size() == 1) {
                    notFoundMsg = "Không tìm thấy mặt hàng có mã " + notFoundCodes.get(0);
                } else {
                    notFoundMsg = "Không tìm thấy các mặt hàng có mã sau trong hệ thống:\n" + String.join(", ", notFoundCodes);
                }
                JOptionPane.showMessageDialog(this, notFoundMsg, "Thông báo", JOptionPane.WARNING_MESSAGE);
            }

            if (countMatched > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã nạp tồn kho và giá vốn thành công cho " + countMatched + " mặt hàng từ file Excel!\nVui lòng kiểm tra lại và nhấn 'Ghi dữ liệu' để lưu.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else if (notFoundCodes.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy dòng dữ liệu tồn kho hợp lệ nào trong file Excel!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi đọc file Excel: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.format("%.0f", num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ignored) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    String str = cell.getStringCellValue().replace(",", "").trim();
                    return Double.parseDouble(str);
                } catch (Exception e) {
                    return 0.0;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
}
