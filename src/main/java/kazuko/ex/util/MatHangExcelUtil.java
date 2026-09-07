package kazuko.ex.util;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MatHangExcelUtil {

    public static List<String> getExportColumns(SystemConfig config) {
        if (config == null) {
            config = new SystemConfigDAO().loadConfig();
        }

        String labelGia2 = (config != null && config.getDienGiaiGia2() != null && !config.getDienGiaiGia2().trim().isEmpty())
                ? config.getDienGiaiGia2().trim() : "Đơn giá 2";
        String labelGia3 = (config != null && config.getDienGiaiGia3() != null && !config.getDienGiaiGia3().trim().isEmpty())
                ? config.getDienGiaiGia3().trim() : "Giá bán 3";
        String labelGia4 = (config != null && config.getDienGiaiGia4() != null && !config.getDienGiaiGia4().trim().isEmpty())
                ? config.getDienGiaiGia4().trim() : "Giá bán 4";

        List<String> cols = new ArrayList<>();
        cols.add("Tên mặt hàng");
        cols.add("Nhóm mặt hàng");
        cols.add("Đơn vị tính");
        cols.add("Giá bán");
        cols.add("Giá nhập");
        cols.add("Mặc định giảm giá");
        cols.add("Mặc định giảm tiền");
        cols.add("Ghi chú");
        cols.add("Không in chế biến");
        cols.add("Bảo hành");
        cols.add("Đơn vị tính chẵn");
        cols.add("Quy đổi");
        cols.add("Giá bán chẵn");
        cols.add(labelGia2);
        cols.add(labelGia3);
        if (config != null && config.isSuDungGia4()) {
            cols.add(labelGia4);
        }
        cols.add("Hãng sản xuất");
        cols.add("Có hạn sử dụng");
        return cols;
    }

    public static void exportMatHangList(Component parent, List<MatHang> matHangList, String defaultFileName) {
        if (matHangList == null || matHangList.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Không có dữ liệu mặt hàng để xuất Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SystemConfig config = new SystemConfigDAO().loadConfig();
        List<String> exportCols = getExportColumns(config);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất danh mục mặt hàng ra file Excel");
        chooser.setSelectedFile(new File(defaultFileName != null ? defaultFileName : "DanhMuc_MatHang.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
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
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Style - Text
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            // Data Style - Number
            CellStyle numberStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0.##"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < exportCols.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(exportCols.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Create Data Rows
            int rowIdx = 1;
            for (MatHang mh : matHangList) {
                if (mh == null) continue;
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);

                int c = 0;
                // 0: Tên mặt hàng
                Cell c0 = row.createCell(c++);
                c0.setCellValue(mh.getName() != null ? mh.getName() : "");
                c0.setCellStyle(textStyle);

                // 1: Nhóm mặt hàng
                Cell c1 = row.createCell(c++);
                c1.setCellValue(mh.getTenNhom() != null ? mh.getTenNhom() : "");
                c1.setCellStyle(textStyle);

                // 2: Đơn vị tính
                Cell c2 = row.createCell(c++);
                c2.setCellValue(mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "");
                c2.setCellStyle(textStyle);

                // 3: Giá bán
                Cell c3 = row.createCell(c++);
                c3.setCellValue(mh.getGiaBan());
                c3.setCellStyle(numberStyle);

                // 4: Giá nhập
                Cell c4 = row.createCell(c++);
                c4.setCellValue(mh.getGiaNhap());
                c4.setCellStyle(numberStyle);

                // 5: Mặc định giảm giá
                Cell c5 = row.createCell(c++);
                c5.setCellValue(mh.getGiamGiaPt());
                c5.setCellStyle(numberStyle);

                // 6: Mặc định giảm tiền
                Cell c6 = row.createCell(c++);
                c6.setCellValue(mh.getGiamGiaTien());
                c6.setCellStyle(numberStyle);

                // 7: Ghi chú
                Cell c7 = row.createCell(c++);
                c7.setCellValue(mh.getGhiChu() != null ? mh.getGhiChu() : "");
                c7.setCellStyle(textStyle);

                // 8: Không in chế biến
                Cell c8 = row.createCell(c++);
                c8.setCellValue(mh.isKhongInCheBien() ? "Có" : "Không");
                c8.setCellStyle(textStyle);

                // 9: Bảo hành
                Cell c9 = row.createCell(c++);
                c9.setCellValue(mh.getBaoHanh() != null ? mh.getBaoHanh() : "");
                c9.setCellStyle(textStyle);

                // 10: Đơn vị tính chẵn
                Cell c10 = row.createCell(c++);
                c10.setCellValue(mh.getTenDonViTinhChan() != null ? mh.getTenDonViTinhChan() : "");
                c10.setCellStyle(textStyle);

                // 11: Quy đổi
                Cell c11 = row.createCell(c++);
                c11.setCellValue(mh.getQuyDoi());
                c11.setCellStyle(numberStyle);

                // 12: Giá bán chẵn
                Cell c12 = row.createCell(c++);
                c12.setCellValue(mh.getGiaBanChan());
                c12.setCellStyle(numberStyle);

                // 13: Giá 2
                Cell c13 = row.createCell(c++);
                c13.setCellValue(mh.getGiaBan2());
                c13.setCellStyle(numberStyle);

                // 14: Giá 3
                Cell c14 = row.createCell(c++);
                c14.setCellValue(mh.getGiaBan3());
                c14.setCellStyle(numberStyle);

                // 15: Giá 4 (nếu có)
                if (config != null && config.isSuDungGia4()) {
                    Cell cGia4 = row.createCell(c++);
                    cGia4.setCellValue(mh.getGiaBan4());
                    cGia4.setCellStyle(numberStyle);
                }

                // Hãng sản xuất
                Cell cHsx = row.createCell(c++);
                cHsx.setCellValue(mh.getHangSanXuat() != null ? mh.getHangSanXuat() : "");
                cHsx.setCellStyle(textStyle);

                // Có hạn sử dụng
                Cell cCoHan = row.createCell(c++);
                cCoHan.setCellValue(mh.isCoHanSuDung() ? "Có" : "Không");
                cCoHan.setCellStyle(textStyle);
            }

            // Auto-size columns
            for (int i = 0; i < exportCols.size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1000, 3500));
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            int open = JOptionPane.showConfirmDialog(
                    parent,
                    "Đã xuất file Excel thành công tại:\n" + file.getAbsolutePath() + "\n\nBạn có muốn mở file ngay không?",
                    "Xuất Excel thành công",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Lỗi khi xuất file Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void exportSampleTemplate(Component parent, String defaultFileName) {
        SystemConfig config = new SystemConfigDAO().loadConfig();
        List<String> exportCols = getExportColumns(config);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file mẫu Excel danh mục mặt hàng");
        chooser.setSelectedFile(new File(defaultFileName != null ? defaultFileName : "Mau_DanhMuc_MatHang.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
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
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Style - Text
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            // Data Style - Number
            CellStyle numberStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0.##"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < exportCols.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(exportCols.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Auto-size columns
            for (int i = 0; i < exportCols.size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1000, 3500));
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            int open = JOptionPane.showConfirmDialog(
                    parent,
                    "Đã xuất file mẫu thành công tại:\n" + file.getAbsolutePath() + "\n\nBạn có muốn mở file ngay không?",
                    "Xuất file mẫu thành công",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Lỗi khi xuất file mẫu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
