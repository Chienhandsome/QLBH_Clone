package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.ui.common.DatePickerField;
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
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CongNoKhachHangBanDauDialog extends JDialog {

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final CongNoKhachHangDAO congNoDAO = new CongNoKhachHangDAO();

    private DatePickerField datePicker;
    private JTextField txtFilter;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    // Danh sách toàn bộ khách hàng và công nợ hiện thời
    private final List<KhachHang> allCustomers = new ArrayList<>();
    private final Map<String, Double> debtMap = new HashMap<>(); // khachId -> soTien

    private static final DecimalFormat NUM_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(',');
        NUM_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    public CongNoKhachHangBanDauDialog(Window parent) {
        super(parent, "CÔNG NỢ BAN ĐẦU", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadData();
        setSize(1000, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(245, 248, 253));

        // 1. TOP CONTAINER: Banner + Date picker + Toolbar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(240, 245, 252));
        topContainer.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));

        // 1.1 Banner
        JPanel pnlBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlBanner.setOpaque(false);

        JLabel lblBannerIcon = new JLabel(IconHelper.getIcon("btnCard.Icon.png", 22, 22));
        JLabel lblBannerText = new JLabel("Cập nhật thông tin công nợ ban đầu khi sử dụng phần mềm");
        lblBannerText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBannerText.setForeground(new Color(30, 30, 30));

        pnlBanner.add(lblBannerIcon);
        pnlBanner.add(lblBannerText);
        topContainer.add(pnlBanner);

        // 1.2 Date Picker Row
        JPanel pnlDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlDate.setOpaque(false);

        JLabel lblNgayChot = new JLabel("Ngày chốt công nợ:");
        lblNgayChot.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        datePicker = new DatePickerField();
        datePicker.setPreferredSize(new Dimension(130, 24));
        datePicker.setDate(new Date());

        pnlDate.add(lblNgayChot);
        pnlDate.add(datePicker);
        topContainer.add(pnlDate);

        // 1.3 Toolbar: Lọc (F3) + Import + Export
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));

        txtFilter = new JTextField();
        txtFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilter.setPreferredSize(new Dimension(160, 25));
        txtFilter.setBorder(new CompoundBorder(
                new LineBorder(new Color(175, 195, 225), 1),
                new EmptyBorder(1, 5, 1, 5)
        ));
        txtFilter.setToolTipText("Lọc nhanh theo Mã khách, Tên khách, SĐT (Phím tắt F3)");

        // Prompt text / Placeholder cho ô Lọc (F3)
        txtFilter.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtFilter.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                txtFilter.repaint();
            }
        });

        JButton btnImport = new JButton("Import từ excel", IconHelper.getIcon("page_white_excel.png", 16, 16));
        btnImport.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnImport.setBackground(Color.WHITE);
        btnImport.setFocusPainted(false);
        btnImport.addActionListener(e -> onImportExcel());

        JButton btnExport = new JButton("Export file mẫu", IconHelper.getIcon("export_excel.png", 16, 16));
        btnExport.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExport.setBackground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> onExportExcelTemplate());

        pnlToolbar.add(new JLabel(" "));
        pnlToolbar.add(txtFilter);
        pnlToolbar.add(btnImport);
        pnlToolbar.add(btnExport);

        topContainer.add(pnlToolbar);
        add(topContainer, BorderLayout.NORTH);

        // 2. CENTER: JTable
        String[] columns = {" ", "Mã khách", "Tên khách", "Địa chỉ", "Điện thoại", "Số tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Chỉ cho phép nhập số tiền ở cột 5
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Double.class;
                return String.class;
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dataTable.setRowHeight(22);
        dataTable.setSelectionBackground(new Color(0, 120, 215));
        dataTable.setSelectionForeground(Color.WHITE);
        dataTable.setGridColor(new Color(225, 235, 245));
        dataTable.setShowGrid(true);

        // Header Style
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        dataTable.getTableHeader().setBackground(new Color(240, 245, 252));
        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 26));

        // Column widths
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        dataTable.getColumnModel().getColumn(0).setMaxWidth(60);
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(230);
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(260);
        dataTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        // Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        dataTable.getColumnModel().getColumn(0).setCellRenderer(new RowIndexRenderer());

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    l.setText(NUM_FORMAT.format(((Number) value).doubleValue()));
                } else if (value != null) {
                    try {
                        double d = Double.parseDouble(value.toString().replace(",", "").trim());
                        l.setText(NUM_FORMAT.format(d));
                    } catch (Exception ignored) {
                        l.setText(value.toString());
                    }
                } else {
                    l.setText("0");
                }
                l.setBorder(new EmptyBorder(0, 4, 0, 6));
                return l;
            }
        };
        dataTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        // Editor cho cột Số tiền
        JTextField txtEditor = new JTextField();
        txtEditor.setHorizontalAlignment(SwingConstants.RIGHT);
        txtEditor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dataTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(txtEditor) {
            @Override
            public Object getCellEditorValue() {
                String val = txtEditor.getText().replace(",", "").trim();
                try {
                    return Double.parseDouble(val);
                } catch (Exception e) {
                    return 0.0;
                }
            }
        });

        // Lắng nghe chỉnh sửa ô Số tiền
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 5 && e.getFirstRow() >= 0) {
                int row = e.getFirstRow();
                String maKhach = (String) tableModel.getValueAt(row, 1);
                Object val = tableModel.getValueAt(row, 5);
                double amount = 0.0;
                if (val instanceof Number) {
                    amount = ((Number) val).doubleValue();
                } else if (val != null) {
                    try {
                        amount = Double.parseDouble(val.toString().replace(",", "").trim());
                    } catch (Exception ignored) {}
                }
                if (maKhach != null) {
                    for (KhachHang kh : allCustomers) {
                        if (maKhach.equalsIgnoreCase(kh.getMaKhach())) {
                            debtMap.put(kh.getId(), amount);
                            break;
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, new Color(185, 205, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBorder(new EmptyBorder(6, 6, 6, 6));
        pnlCenter.setOpaque(false);
        pnlCenter.add(scrollPane, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. BOTTOM: Ghi dữ liệu + Thoát
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setBackground(new Color(240, 245, 252));
        pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(195, 215, 235)));

        JButton btnSave = new JButton("Ghi dữ liệu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setPreferredSize(new Dimension(100, 28));
        btnSave.setBackground(AppColors.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> onSaveData());

        JButton btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setPreferredSize(new Dimension(85, 28));
        btnExit.setBackground(AppColors.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.addActionListener(e -> dispose());

        pnlBottom.add(btnSave);
        pnlBottom.add(btnExit);
        add(pnlBottom, BorderLayout.SOUTH);

        // Filter events
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterRows(); }
            @Override public void removeUpdate(DocumentEvent e) { filterRows(); }
            @Override public void changedUpdate(DocumentEvent e) { filterRows(); }
        });

        // Hotkeys
        getRootPane().registerKeyboardAction(
                e -> txtFilter.requestFocusInWindow(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void loadData() {
        allCustomers.clear();
        allCustomers.addAll(khachHangDAO.getAll());

        debtMap.clear();
        debtMap.putAll(congNoDAO.getCongNoBanDauMap());

        filterRows();
    }

    private void filterRows() {
        // Dừng cell editing nếu đang edit
        if (dataTable.isEditing()) {
            dataTable.getCellEditor().stopCellEditing();
        }

        String kw = txtFilter.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        int idx = 1;
        for (KhachHang kh : allCustomers) {
            String ma = (kh.getMaKhach() != null) ? kh.getMaKhach() : "";
            String ten = (kh.getName() != null) ? kh.getName() : "";
            String diaChi = (kh.getDiaChi() != null) ? kh.getDiaChi() : "";
            String sdt = (kh.getDienThoai() != null) ? kh.getDienThoai() : "";
            double soTien = debtMap.getOrDefault(kh.getId(), 0.0);

            if (!kw.isEmpty()) {
                boolean match = ma.toLowerCase().contains(kw) ||
                        ten.toLowerCase().contains(kw) ||
                        diaChi.toLowerCase().contains(kw) ||
                        sdt.toLowerCase().contains(kw);
                if (!match) continue;
            }

            String strIdx = String.format("%02d", idx++);
            tableModel.addRow(new Object[]{strIdx, ma, ten, diaChi, sdt, soTien});
        }
    }

    private void onSaveData() {
        if (dataTable.isEditing()) {
            dataTable.getCellEditor().stopCellEditing();
        }

        Date ngayChot = datePicker.getDate();
        boolean ok = congNoDAO.saveCongNoBanDau(ngayChot, debtMap);
        if (ok) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ghi dữ liệu công nợ ban đầu thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Có lỗi xảy ra trong quá trình ghi dữ liệu!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Xuất file Excel mẫu chứa danh sách khách hàng và công nợ đầu (Đúng định dạng Hình 3)
     */
    private void onExportExcelTemplate() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel mẫu công nợ ban đầu");
        fc.setSelectedFile(new File("CongNoKhachHangBanDau_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx"));
        fc.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fc.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fc.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Công nợ ban đầu");

            // Style header
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontName("Segoe UI");
            headerFont.setFontHeightInPoints((short) 11);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);

            // Row 0: Headers
            Row headerRow = sheet.createRow(0);
            Cell cellA = headerRow.createCell(0);
            cellA.setCellValue("Mã");
            cellA.setCellStyle(headerStyle);

            Cell cellB = headerRow.createCell(1);
            cellB.setCellValue("Tên");
            cellB.setCellStyle(headerStyle);

            Cell cellC = headerRow.createCell(2);
            cellC.setCellValue("Công nợ đầu");
            cellC.setCellStyle(headerStyle);

            // Data rows
            int rowIdx = 1;
            CellStyle numStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numStyle.setDataFormat(format.getFormat("#,##0"));

            for (KhachHang kh : allCustomers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(kh.getMaKhach() != null ? kh.getMaKhach() : "");
                row.createCell(1).setCellValue(kh.getName() != null ? kh.getName() : "");

                Cell cNo = row.createCell(2);
                double amt = debtMap.getOrDefault(kh.getId(), 0.0);
                cNo.setCellValue(amt);
                cNo.setCellStyle(numStyle);
            }

            sheet.setColumnWidth(0, 15 * 256);
            sheet.setColumnWidth(1, 30 * 256);
            sheet.setColumnWidth(2, 18 * 256);

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
     * Nhập công nợ từ file Excel qua hộp thoại mapping (Đúng luồng Hình 2)
     */
    private void onImportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file Excel công nợ ban đầu");
        fc.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));

        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fc.getSelectedFile();
        if (selectedFile == null || !selectedFile.exists()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = WorkbookFactory.create(fis)) {

            if (workbook.getNumberOfSheets() == 0) {
                JOptionPane.showMessageDialog(this, "File Excel không có dữ liệu sheet!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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

            // Hiển thị hộp thoại Thêm từ excel (Hình 2)
            ImportExcelMappingDialog mappingDlg = new ImportExcelMappingDialog(this, headers);
            mappingDlg.setVisible(true);

            if (!mappingDlg.isConfirmed()) {
                return;
            }

            Map<String, String> fieldMapping = mappingDlg.getResultMap();
            if (fieldMapping.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bạn chưa chọn ánh xạ cột nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tìm index cột Mã đối tác và Công nợ đầu
            int colIdxMa = -1;
            int colIdxCongNo = -1;

            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                String hName = (cell != null) ? getCellValueAsString(cell).trim() : "";
                String mappedField = fieldMapping.get(hName);
                if (mappedField != null) {
                    if (ImportExcelMappingDialog.FIELD_MA_DOI_TAC.equalsIgnoreCase(mappedField)) {
                        colIdxMa = c;
                    } else if (ImportExcelMappingDialog.FIELD_CONG_NO_DAU.equalsIgnoreCase(mappedField)) {
                        colIdxCongNo = c;
                    }
                }
            }

            if (colIdxMa == -1 && colIdxCongNo == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng ánh xạ ít nhất cột 'Mã đối tác' hoặc 'Công nợ đầu'!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int countMatched = 0;
            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String maDoiTac = (colIdxMa >= 0) ? getCellValueAsString(row.getCell(colIdxMa)).trim() : "";
                double soTien = 0.0;
                if (colIdxCongNo >= 0) {
                    soTien = getCellValueAsDouble(row.getCell(colIdxCongNo));
                }

                if (!maDoiTac.isEmpty()) {
                    for (KhachHang kh : allCustomers) {
                        if (kh.getMaKhach() != null && kh.getMaKhach().trim().equalsIgnoreCase(maDoiTac)) {
                            debtMap.put(kh.getId(), soTien);
                            countMatched++;
                            break;
                        }
                    }
                }
            }

            filterRows();

            JOptionPane.showMessageDialog(
                    this,
                    "Đã nạp công nợ thành công cho " + countMatched + " khách hàng từ file Excel!\nVui lòng kiểm tra lại và nhấn 'Ghi dữ liệu' để lưu.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );

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
                    return String.valueOf(cell.getNumericCellValue());
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
                    return Double.parseDouble(cell.getStringCellValue().replace(",", "").trim());
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

    private static class RowIndexRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                l.setText("▸ " + (value != null ? value.toString() : ""));
                l.setBackground(new Color(230, 240, 255));
                l.setForeground(new Color(0, 100, 200));
                l.setFont(l.getFont().deriveFont(Font.BOLD));
            } else {
                l.setText(value != null ? value.toString() : "");
                l.setBackground(new Color(245, 248, 252));
                l.setForeground(new Color(90, 110, 130));
                l.setFont(l.getFont().deriveFont(Font.PLAIN));
            }
            return l;
        }
    }
}
