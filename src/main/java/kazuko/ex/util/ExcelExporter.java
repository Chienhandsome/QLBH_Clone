package kazuko.ex.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelExporter {

    /**
     * Xuất toàn bộ các hàng và cột hiện đang hiển thị trên JTable ra file Excel (.xlsx).
     *
     * @param table           JTable chứa dữ liệu cần xuất
     * @param defaultBaseName Tên file mặc định (không kèm extension)
     * @param sheetTitle      Tên sheet trong Excel
     * @param parent          Component cha để định vị dialog
     */
    public static void exportTable(JTable table, String defaultBaseName, String sheetTitle, Component parent) {
        if (table == null || table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "Không có dữ liệu hiển thị trên bảng để xuất Excel!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultFileName = defaultBaseName + "_" + timeStamp + ".xlsx";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất dữ liệu ra file Excel");
        chooser.setSelectedFile(new File(defaultFileName));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(parent,
                    "File '" + file.getName() + "' đã tồn tại. Bạn có muốn ghi đè không?",
                    "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            int totalRows = exportTableToFile(table, sheetTitle, file);

            // Dialog thông báo kết quả và cho phép mở file
            final File finalFile = file;
            Object[] options = {"Mở file", "Mở thư mục chứa file", "Đóng"};
            int chosen = JOptionPane.showOptionDialog(parent,
                    "Xuất dữ liệu Excel thành công!\n\n" +
                    "• Số dòng đã xuất: " + totalRows + " dòng\n" +
                    "• Tệp tin: " + file.getAbsolutePath(),
                    "Xuất Excel thành công",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    IconHelper.getIcon("export_excel.png", 32, 32),
                    options,
                    options[0]
            );

            if (chosen == 0) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(finalFile);
                }
            } else if (chosen == 1) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(finalFile.getParentFile());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Có lỗi xảy ra khi xuất file Excel:\n" + ex.getMessage(),
                    "Lỗi xuất Excel",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xuất dữ liệu JTable trực tiếp ra File chỉ định (phục vụ cả headless/test và runtime).
     *
     * @param table      JTable chứa dữ liệu
     * @param sheetTitle Tên Sheet
     * @param file       File đích
     * @return Số dòng dữ liệu đã xuất
     * @throws Exception khi xảy ra lỗi I/O hoặc POI
     */
    public static int exportTableToFile(JTable table, String sheetTitle, File file) throws Exception {
        if (table == null || table.getRowCount() == 0) {
            return 0;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetTitle != null && !sheetTitle.trim().isEmpty() ? sheetTitle : "Danh sách");
            sheet.setDisplayGridlines(true);

            DataFormat dataFormat = workbook.createDataFormat();
            short numberFormatCode = dataFormat.getFormat("#,##0");
            short decimalFormatCode = dataFormat.getFormat("#,##0.##");

            // Fonts
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);

            Font dataFont = workbook.createFont();
            dataFont.setFontHeightInPoints((short) 10);

            // Header Style: Xanh dương chuẩn Office
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(headerStyle);

            // Data Style - Normal Left
            CellStyle dataStyleLeft = workbook.createCellStyle();
            dataStyleLeft.setFont(dataFont);
            dataStyleLeft.setAlignment(HorizontalAlignment.LEFT);
            dataStyleLeft.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(dataStyleLeft);

            // Data Style - Normal Center
            CellStyle dataStyleCenter = workbook.createCellStyle();
            dataStyleCenter.setFont(dataFont);
            dataStyleCenter.setAlignment(HorizontalAlignment.CENTER);
            dataStyleCenter.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(dataStyleCenter);

            // Data Style - Normal Right Number
            CellStyle dataStyleNumber = workbook.createCellStyle();
            dataStyleNumber.setFont(dataFont);
            dataStyleNumber.setAlignment(HorizontalAlignment.RIGHT);
            dataStyleNumber.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyleNumber.setDataFormat(numberFormatCode);
            setBorders(dataStyleNumber);

            // Data Style - Normal Right Decimal
            CellStyle dataStyleDecimal = workbook.createCellStyle();
            dataStyleDecimal.setFont(dataFont);
            dataStyleDecimal.setAlignment(HorizontalAlignment.RIGHT);
            dataStyleDecimal.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyleDecimal.setDataFormat(decimalFormatCode);
            setBorders(dataStyleDecimal);

            // Alternate Row Styles (Zebra striping #F2F6FB)
            CellStyle altStyleLeft = workbook.createCellStyle();
            altStyleLeft.cloneStyleFrom(dataStyleLeft);
            altStyleLeft.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            altStyleLeft.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle altStyleCenter = workbook.createCellStyle();
            altStyleCenter.cloneStyleFrom(dataStyleCenter);
            altStyleCenter.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            altStyleCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle altStyleNumber = workbook.createCellStyle();
            altStyleNumber.cloneStyleFrom(dataStyleNumber);
            altStyleNumber.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            altStyleNumber.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle altStyleDecimal = workbook.createCellStyle();
            altStyleDecimal.cloneStyleFrom(dataStyleDecimal);
            altStyleDecimal.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            altStyleDecimal.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int totalCols = table.getColumnCount();
            int totalRows = table.getRowCount();

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(26);

            for (int col = 0; col < totalCols; col++) {
                String colName = table.getColumnName(col);
                if (col == 0 && (colName == null || colName.trim().isEmpty())) {
                    colName = "STT";
                }
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(colName != null ? colName : "");
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int r = 0; r < totalRows; r++) {
                Row dataRow = sheet.createRow(r + 1);
                dataRow.setHeightInPoints(20);
                boolean isAlt = (r % 2 == 1);

                for (int col = 0; col < totalCols; col++) {
                    Cell cell = dataRow.createCell(col);
                    Object val = table.getValueAt(r, col);
                    String colName = table.getColumnName(col);

                    if (col == 0) {
                        // Cột con trỏ / STT đầu tiên
                        cell.setCellValue(r + 1);
                        cell.setCellStyle(isAlt ? altStyleCenter : dataStyleCenter);
                    } else if (val == null) {
                        cell.setCellValue("");
                        cell.setCellStyle(isAlt ? altStyleLeft : dataStyleLeft);
                    } else if (val instanceof Number) {
                        double dVal = ((Number) val).doubleValue();
                        cell.setCellValue(dVal);
                        if (dVal == Math.floor(dVal) && !Double.isInfinite(dVal)) {
                            cell.setCellStyle(isAlt ? altStyleNumber : dataStyleNumber);
                        } else {
                            cell.setCellStyle(isAlt ? altStyleDecimal : dataStyleDecimal);
                        }
                    } else if (val instanceof Boolean) {
                        boolean bVal = (Boolean) val;
                        cell.setCellValue(bVal ? "Có" : "Không");
                        cell.setCellStyle(isAlt ? altStyleCenter : dataStyleCenter);
                    } else {
                        String strVal = val.toString().trim();
                        Double parsedNum = tryParseNumber(strVal);
                        if (parsedNum != null && isNumericColumn(colName)) {
                            cell.setCellValue(parsedNum);
                            if (parsedNum == Math.floor(parsedNum) && !Double.isInfinite(parsedNum)) {
                                cell.setCellStyle(isAlt ? altStyleNumber : dataStyleNumber);
                            } else {
                                cell.setCellStyle(isAlt ? altStyleDecimal : dataStyleDecimal);
                            }
                        } else {
                            cell.setCellValue(strVal);
                            if (isCenterColumn(colName)) {
                                cell.setCellStyle(isAlt ? altStyleCenter : dataStyleCenter);
                            } else {
                                cell.setCellStyle(isAlt ? altStyleLeft : dataStyleLeft);
                            }
                        }
                    }
                }
            }

            // Auto-filter
            if (totalCols > 0 && totalRows > 0) {
                sheet.setAutoFilter(new CellRangeAddress(0, totalRows, 0, totalCols - 1));
            }

            // Auto size columns with padding
            for (int col = 0; col < totalCols; col++) {
                sheet.autoSizeColumn(col);
                int width = sheet.getColumnWidth(col);
                int minWidth = 10 * 256;
                int maxWidth = 45 * 256;
                if (width < minWidth) sheet.setColumnWidth(col, minWidth);
                else if (width > maxWidth) sheet.setColumnWidth(col, maxWidth);
                else sheet.setColumnWidth(col, width + 1200);
            }

            // Ghi file
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            return totalRows;
        }
    }

    private static void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
    }

    private static Double tryParseNumber(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            String clean = str.replace(",", "").replace(" ", "").trim();
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isNumericColumn(String colName) {
        if (colName == null) return false;
        String lower = colName.toLowerCase();
        return lower.contains("giá") || lower.contains("tiền") || lower.contains("điểm")
                || lower.contains("tồn") || lower.contains("quy đổi") || lower.contains("hoa hồng")
                || lower.contains("số lượng") || lower.contains("thành tiền") || lower.contains("tỉ lệ")
                || lower.contains("chiết khấu");
    }

    private static boolean isCenterColumn(String colName) {
        if (colName == null) return false;
        String lower = colName.toLowerCase();
        return lower.contains("đơn vị tính") || lower.contains("đvt") || lower.contains("mã số thuế")
                || lower.contains("điện thoại") || lower.contains("ngày") || lower.contains("size")
                || lower.contains("bảo hành") || lower.contains("mã khách") || lower.contains("mã hàng");
    }
}
