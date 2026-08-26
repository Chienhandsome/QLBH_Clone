package kazuko.ex.ui.danhmuc;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class XuatMauVaChonExcelDialog extends JDialog {
    private final List<String> visibleColumns;
    private final List<String> allColumns;
    private final Consumer<List<List<Object>>> onImportSuccess;

    public XuatMauVaChonExcelDialog(Window owner, List<String> visibleColumns, List<String> allColumns, Consumer<List<List<Object>>> onImportSuccess) {
        super(owner, "Chọn file excel hoặc xuất file mẫu", ModalityType.APPLICATION_MODAL);
        this.visibleColumns = visibleColumns != null ? visibleColumns : new ArrayList<>();
        this.allColumns = allColumns != null ? allColumns : new ArrayList<>();
        this.onImportSuccess = onImportSuccess;

        initUI();
        setSize(460, 260);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(205, 222, 245));
        contentPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        // 1. Phần xuất mẫu
        JLabel lblExport = new JLabel("Nếu bạn chưa có file excel, click vào \"Xuất file mẫu\"");
        lblExport.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblExport);
        contentPanel.add(Box.createVerticalStrut(8));

        JButton btnExportVisible = new JButton("Xuất file mẫu các cột đang hiển thị trong danh sách");
        btnExportVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExportVisible.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExportVisible.setMaximumSize(new Dimension(360, 26));
        btnExportVisible.setPreferredSize(new Dimension(360, 26));
        btnExportVisible.setBackground(Color.WHITE);
        btnExportVisible.addActionListener(e -> exportTemplate(visibleColumns, "Mau_MatHang_HienThi.xlsx"));
        contentPanel.add(btnExportVisible);
        contentPanel.add(Box.createVerticalStrut(6));

        JButton btnExportAll = new JButton("Xuất file mẫu tất cả các cột (bao gồm cả cột ẩn)");
        btnExportAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExportAll.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExportAll.setMaximumSize(new Dimension(360, 26));
        btnExportAll.setPreferredSize(new Dimension(360, 26));
        btnExportAll.setBackground(Color.WHITE);
        btnExportAll.addActionListener(e -> exportTemplate(allColumns, "Mau_MatHang_TatCa.xlsx"));
        contentPanel.add(btnExportAll);
        contentPanel.add(Box.createVerticalStrut(14));

        // 2. Phần chọn file excel
        JLabel lblImport = new JLabel("Nếu đã có mẫu, click \"Chọn file excel\"");
        lblImport.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblImport.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblImport);
        contentPanel.add(Box.createVerticalStrut(8));

        JButton btnChooseExcel = new JButton("Chọn file excel");
        btnChooseExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChooseExcel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnChooseExcel.setMaximumSize(new Dimension(360, 26));
        btnChooseExcel.setPreferredSize(new Dimension(360, 26));
        btnChooseExcel.setBackground(Color.WHITE);
        btnChooseExcel.addActionListener(e -> onChooseExcelFile());
        contentPanel.add(btnChooseExcel);
        contentPanel.add(Box.createVerticalStrut(6));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCancel.setMaximumSize(new Dimension(360, 26));
        btnCancel.setPreferredSize(new Dimension(360, 26));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.addActionListener(e -> dispose());
        contentPanel.add(btnCancel);

        setContentPane(contentPanel);
    }

    private void exportTemplate(List<String> columnsToExport, String defaultFileName) {
        if (columnsToExport == null || columnsToExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có cột nào để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file mẫu Excel");
        chooser.setSelectedFile(new File(defaultFileName));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh mục mặt hàng");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Data Style
            CellStyle dataStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font dataFont = workbook.createFont();
            dataStyle.setFont(dataFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            int colIdx = 0;
            for (String colName : columnsToExport) {
                if (colName.trim().isEmpty()) continue;
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(colName);
                cell.setCellStyle(headerStyle);
            }

            // Sample row
            Row sampleRow = sheet.createRow(1);
            sampleRow.setHeightInPoints(20);
            colIdx = 0;
            for (String colName : columnsToExport) {
                if (colName.trim().isEmpty()) continue;
                Cell cell = sampleRow.createCell(colIdx++);
                cell.setCellStyle(dataStyle);
                if ("Mã hàng".equals(colName)) cell.setCellValue("MH001");
                else if ("Tên mặt hàng".equals(colName)) cell.setCellValue("Áo thun nam Cotton");
                else if ("Nhóm mặt hàng".equals(colName)) cell.setCellValue("Thời trang nam");
                else if ("Đơn vị tính".equals(colName)) cell.setCellValue("Chiếc");
                else if ("Giá bán".equals(colName)) cell.setCellValue(150000);
                else if ("Giá nhập".equals(colName)) cell.setCellValue(95000);
                else if ("Giá vốn".equals(colName)) cell.setCellValue(95000);
                else if ("Mặc định giảm giá".equals(colName)) cell.setCellValue(0);
                else if ("Mặc định giảm tiền".equals(colName)) cell.setCellValue(0);
                else if ("Ghi chú".equals(colName)) cell.setCellValue("Hàng loại 1");
                else if ("Quy đổi".equals(colName)) cell.setCellValue(1);
                else if ("Không in chế biến".equals(colName)) cell.setCellValue(false);
                else if ("Bán nhanh".equals(colName)) cell.setCellValue(false);
                else if ("Có hạn sử dụng".equals(colName)) cell.setCellValue(false);
                else cell.setCellValue("");
            }

            // Auto-size columns
            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3500));
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            int open = JOptionPane.showConfirmDialog(
                    this,
                    "Đã xuất file mẫu thành công tại:\n" + file.getAbsolutePath() + "\n\nBạn có muốn mở file ngay không?",
                    "Xuất file mẫu thành công",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file mẫu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onChooseExcelFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel chứa danh sách mặt hàng");
        chooser.setFileFilter(new FileNameExtensionFilter("File Excel (*.xlsx, *.xls)", "xlsx", "xls"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (InputStream in = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 0) {
                JOptionPane.showMessageDialog(this, "File Excel trống hoặc không đúng định dạng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Đọc Header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tiêu đề cột trong file Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> fileHeaders = new ArrayList<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                fileHeaders.add(getCellValueAsString(cell).trim());
            }

            // Đọc Data rows
            List<List<Object>> importedRows = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                boolean hasData = false;
                List<Object> rowData = new ArrayList<>();
                for (int c = 0; c < fileHeaders.size(); c++) {
                    Cell cell = row.getCell(c);
                    String val = cell != null ? formatter.formatCellValue(cell).trim() : "";
                    if (!val.isEmpty()) hasData = true;
                    rowData.add(val);
                }

                if (hasData) {
                    importedRows.add(rowData);
                }
            }

            if (importedRows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dòng dữ liệu nào trong file Excel!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (onImportSuccess != null) {
                onImportSuccess.accept(importedRows);
            }
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc file Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) return String.valueOf((long) num);
                return String.valueOf(num);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default: return "";
        }
    }
}
