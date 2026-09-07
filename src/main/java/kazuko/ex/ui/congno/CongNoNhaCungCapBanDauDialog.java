package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
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

public class CongNoNhaCungCapBanDauDialog extends JDialog {

    private final CongNoNhaCungCapDAO congNoDAO = new CongNoNhaCungCapDAO();
    private final NhaCungCapDAO nccDAO = new NhaCungCapDAO();

    private DatePickerField dpNgayChot;
    private JTextField txtFilter;
    private JButton btnImportExcel;
    private JButton btnExportExcel;
    private JButton btnGhi;
    private JButton btnThoat;

    private JTable table;
    private DefaultTableModel tableModel;

    private List<NhaCungCap> allSuppliers = new ArrayList<>();
    private final Map<String, Double> debtMap = new HashMap<>(); // nccId -> soTien

    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        NUMBER_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    public CongNoNhaCungCapBanDauDialog(Window parent) {
        super(parent, "CÔNG NỢ BAN ĐẦU", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadData();
        setSize(920, 580);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Top Panel: Banner + Filter Bar
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setBackground(Color.WHITE);

        // 1.1 Banner
        JPanel pnlBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlBanner.setBackground(new Color(254, 252, 235));
        pnlBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 222, 179), 1),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel lblBannerIcon = new JLabel();
        ImageIcon cashuIcon = IconHelper.getIcon("calculator.png", 28, 28);
        if (cashuIcon != null) {
            lblBannerIcon.setIcon(cashuIcon);
        }
        JLabel lblBannerText = new JLabel("Cập nhật thông tin công nợ ban đầu khi sử dụng phần mềm");
        lblBannerText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBannerText.setForeground(new Color(50, 50, 50));

        pnlBanner.add(lblBannerIcon);
        pnlBanner.add(lblBannerText);
        pnlNorth.add(pnlBanner, BorderLayout.NORTH);

        // 1.2 Controls Bar (Ngày chốt + Lọc F3 + Import + Export)
        JPanel pnlControls = new JPanel(new BorderLayout(8, 0));
        pnlControls.setBackground(new Color(218, 232, 247));
        pnlControls.setBorder(new EmptyBorder(6, 10, 6, 10));

        // Left Controls: Ngày chốt công nợ
        JPanel pnlDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlDate.setOpaque(false);
        JLabel lblNgayChot = new JLabel("Ngày chốt công nợ:");
        lblNgayChot.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        dpNgayChot = new DatePickerField();
        dpNgayChot.setPreferredSize(new Dimension(120, 24));
        dpNgayChot.setDate(new Date());

        pnlDate.add(lblNgayChot);
        pnlDate.add(dpNgayChot);

        // Center / Action Controls: Lọc F3 + Buttons
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlActions.setOpaque(false);

        JLabel lblFilter = new JLabel("Lọc (F3)");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilter = new JTextField(15);
        txtFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilter.setPreferredSize(new Dimension(150, 24));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterRows(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterRows(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterRows(); }
        });

        // F3 Global Hotkey to focus search box
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
        btnImportExcel.setPreferredSize(new Dimension(130, 24));
        btnImportExcel.addActionListener(e -> importFromExcel());

        btnExportExcel = new JButton("Export file mẫu");
        btnExportExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExportExcel.setIcon(IconHelper.getIcon("page_white_excel.png", 16, 16));
        btnExportExcel.setBackground(Color.WHITE);
        btnExportExcel.setFocusPainted(false);
        btnExportExcel.setPreferredSize(new Dimension(130, 24));
        btnExportExcel.addActionListener(e -> exportTemplateExcel());

        pnlActions.add(lblFilter);
        pnlActions.add(txtFilter);
        pnlActions.add(btnImportExcel);
        pnlActions.add(btnExportExcel);

        pnlControls.add(pnlDate, BorderLayout.WEST);
        pnlControls.add(pnlActions, BorderLayout.CENTER);
        pnlNorth.add(pnlControls, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);

        // 2. Center: Table
        String[] columnNames = {"STT", "Mã NCC", "Tên NCC", "Địa chỉ", "Điện thoại", "Số tiền"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Chỉ cho phép sửa cột Số tiền
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Double.class;
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
        table.getColumnModel().getColumn(0).setPreferredWidth(45);  // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Mã NCC
        table.getColumnModel().getColumn(2).setPreferredWidth(220); // Tên NCC
        table.getColumnModel().getColumn(3).setPreferredWidth(230); // Địa chỉ
        table.getColumnModel().getColumn(4).setPreferredWidth(110); // Điện thoại
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Số tiền

        // Alignment renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(leftRenderer);

        // Number Renderer & Editor cho Cột Số tiền
        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    lbl.setText(NUMBER_FORMAT.format(((Number) value).doubleValue()));
                } else if (value != null && !value.toString().isEmpty()) {
                    try {
                        double d = Double.parseDouble(value.toString().replace(",", ""));
                        lbl.setText(NUMBER_FORMAT.format(d));
                    } catch (Exception e) {
                        lbl.setText(value.toString());
                    }
                } else {
                    lbl.setText("0");
                }
                return lbl;
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(numberRenderer);

        // Editor cho Cột Số tiền
        JTextField txtNumberEditor = new JTextField();
        txtNumberEditor.setHorizontalAlignment(SwingConstants.RIGHT);
        txtNumberEditor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        DefaultCellEditor numberEditor = new DefaultCellEditor(txtNumberEditor) {
            @Override
            public Object getCellEditorValue() {
                String val = txtNumberEditor.getText().replace(",", "").trim();
                try {
                    return Double.parseDouble(val);
                } catch (Exception e) {
                    return 0.0;
                }
            }

            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
                if (value instanceof Number) {
                    txtNumberEditor.setText(NUMBER_FORMAT.format(((Number) value).doubleValue()));
                } else {
                    txtNumberEditor.setText(value != null ? value.toString() : "0");
                }
                txtNumberEditor.selectAll();
                return txtNumberEditor;
            }
        };
        table.getColumnModel().getColumn(5).setCellEditor(numberEditor);

        // Listener để cập nhật debtMap khi người dùng chỉnh sửa cột Số tiền
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 5 && row >= 0 && row < tableModel.getRowCount()) {
                String maNCC = (String) tableModel.getValueAt(row, 1);
                Object val = tableModel.getValueAt(row, 5);
                double amt = 0.0;
                if (val instanceof Number) {
                    amt = ((Number) val).doubleValue();
                } else if (val != null) {
                    try {
                        amt = Double.parseDouble(val.toString().replace(",", ""));
                    } catch (Exception ignored) {}
                }
                for (NhaCungCap ncc : allSuppliers) {
                    if (ncc.getMaNhaCungCap() != null && ncc.getMaNhaCungCap().equalsIgnoreCase(maNCC)) {
                        debtMap.put(ncc.getId(), amt);
                        break;
                    }
                }
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

        // 3. Bottom Panel: Nút Ghi dữ liệu, Thoát
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setBackground(new Color(240, 245, 252));
        pnlBottom.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 225, 240), 1),
                new EmptyBorder(2, 10, 2, 10)
        ));

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

        pnlBottom.add(btnGhi);
        pnlBottom.add(btnThoat);
        add(pnlBottom, BorderLayout.SOUTH);

        // ESC hotkey to close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void loadData() {
        allSuppliers = nccDAO.getAll();
        Map<String, Double> existingDebts = congNoDAO.getCongNoBanDauMap();
        debtMap.clear();
        for (NhaCungCap ncc : allSuppliers) {
            double amt = existingDebts.getOrDefault(ncc.getId(), 0.0);
            debtMap.put(ncc.getId(), amt);
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

        for (NhaCungCap ncc : allSuppliers) {
            String ma = (ncc.getMaNhaCungCap() != null) ? ncc.getMaNhaCungCap() : "";
            String ten = (ncc.getName() != null) ? ncc.getName() : "";
            String diaChi = (ncc.getDiaChi() != null) ? ncc.getDiaChi() : "";
            String sdt = (ncc.getDienThoai() != null) ? ncc.getDienThoai() : "";
            double soTien = debtMap.getOrDefault(ncc.getId(), 0.0);

            if (!filter.isEmpty()) {
                String fullText = (ma + " " + ten + " " + diaChi + " " + sdt).toLowerCase();
                String fullTextUnaccented = removeAccent(fullText);
                if (!fullText.contains(filter) && !fullTextUnaccented.contains(filterUnaccented)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    String.format("%02d", stt++),
                    ma,
                    ten,
                    diaChi,
                    sdt,
                    soTien
            });
        }
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
        if (ngayChot == null) {
            ngayChot = new Date();
        }

        boolean success = congNoDAO.saveCongNoBanDau(ngayChot, debtMap);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ghi nhận dữ liệu công nợ ban đầu nhà cung cấp thành công!",
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
     * Xuất Excel file mẫu theo format: Mã, Tên, Công nợ đầu
     */
    private void exportTemplateExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel mẫu công nợ ban đầu nhà cung cấp");
        fc.setSelectedFile(new File("CongNoNhaCungCapBanDau_Template.xlsx"));
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
            Sheet sheet = workbook.createSheet("Công nợ ban đầu NCC");

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

            for (NhaCungCap ncc : allSuppliers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ncc.getMaNhaCungCap() != null ? ncc.getMaNhaCungCap() : "");
                row.createCell(1).setCellValue(ncc.getName() != null ? ncc.getName() : "");

                Cell cNo = row.createCell(2);
                double amt = debtMap.getOrDefault(ncc.getId(), 0.0);
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
     * Nhập dữ liệu công nợ ban đầu từ file Excel
     */
    private void importFromExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file Excel công nợ nhà cung cấp");
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

            // Hiển thị hộp thoại Thêm từ excel
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

            if (colIdxMa == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng ánh xạ cột 'Mã đối tác' để đối chiếu nhà cung cấp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (colIdxCongNo == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng ánh xạ cột 'Công nợ đầu'!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int countMatched = 0;
            List<String> notFoundCodes = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String maDoiTac = getCellValueAsString(row.getCell(colIdxMa)).trim();
                double soTien = getCellValueAsDouble(row.getCell(colIdxCongNo));

                if (maDoiTac.isEmpty()) {
                    continue;
                }

                boolean found = false;
                for (NhaCungCap ncc : allSuppliers) {
                    if (ncc.getMaNhaCungCap() != null && ncc.getMaNhaCungCap().trim().equalsIgnoreCase(maDoiTac)) {
                        debtMap.put(ncc.getId(), soTien);
                        countMatched++;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (!notFoundCodes.contains(maDoiTac)) {
                        notFoundCodes.add(maDoiTac);
                    }
                }
            }

            filterRows();

            if (!notFoundCodes.isEmpty()) {
                String notFoundMsg;
                if (notFoundCodes.size() == 1) {
                    notFoundMsg = "Không tìm thấy nhà cung cấp có mã " + notFoundCodes.get(0);
                } else {
                    notFoundMsg = "Không tìm thấy các nhà cung cấp có mã sau trong hệ thống:\n" + String.join(", ", notFoundCodes);
                }
                JOptionPane.showMessageDialog(this, notFoundMsg, "Thông báo", JOptionPane.WARNING_MESSAGE);
            }

            if (countMatched > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã nạp công nợ thành công cho " + countMatched + " nhà cung cấp từ file Excel!\nVui lòng kiểm tra lại và nhấn 'Ghi dữ liệu' để lưu.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else if (notFoundCodes.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy dòng dữ liệu công nợ hợp lệ nào trong file Excel!",
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
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
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
