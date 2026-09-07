package kazuko.ex.util;

import kazuko.ex.model.danhmuc.KhachHang;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KhachHangExcelUtil {

    public static final List<String> ALL_POSSIBLE_COLUMNS = List.of(
            "Mã khách",
            "Tên khách hàng",
            "Địa chỉ",
            "Điện thoại",
            "Email",
            "Nhóm khách hàng",
            "Mã số thuế",
            "Nhân viên",
            "Ngày thành lập/sinh nhật",
            "Tỉnh thành",
            "Facebook",
            "Ghi chú",
            "Điểm tích lũy ban đầu"
    );

    public static final List<String> DEFAULT_VISIBLE_COLUMNS = List.of(
            "Mã khách",
            "Tên khách hàng",
            "Địa chỉ",
            "Điện thoại",
            "Email",
            "Nhóm khách hàng",
            "Mã số thuế",
            "Nhân viên",
            "Ngày thành lập/sinh nhật",
            "Tỉnh thành",
            "Facebook"
    );

    public static void exportSampleTemplate(Component parent, String defaultFileName, boolean allColumns, List<String> customVisibleCols) {
        List<String> headers = allColumns ? new ArrayList<>(ALL_POSSIBLE_COLUMNS)
                : (customVisibleCols != null && !customVisibleCols.isEmpty() ? new ArrayList<>(customVisibleCols) : new ArrayList<>(DEFAULT_VISIBLE_COLUMNS));

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất file Excel mẫu danh mục khách hàng");
        chooser.setSelectedFile(new File(defaultFileName != null ? defaultFileName : (allColumns ? "Mau_KhachHang_TatCa.xlsx" : "Mau_KhachHang_HienThi.xlsx")));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách khách hàng");

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

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            Row sampleRow1 = sheet.createRow(1);
            sampleRow1.setHeightInPoints(20);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = sampleRow1.createCell(i);
                cell.setCellStyle(textStyle);
                String h = headers.get(i);
                if ("Mã khách".equals(h)) cell.setCellValue("KH00001");
                else if ("Tên khách hàng".equals(h)) cell.setCellValue("Công ty TNHH An Khang");
                else if ("Địa chỉ".equals(h)) cell.setCellValue("123 Nguyễn Huệ, Q.1, TP.HCM");
                else if ("Điện thoại".equals(h)) cell.setCellValue("0901234567");
                else if ("Email".equals(h)) cell.setCellValue("ankhang@example.com");
                else if ("Nhóm khách hàng".equals(h)) cell.setCellValue("Khách VIP");
                else if ("Mã số thuế".equals(h)) cell.setCellValue("0312345678");
                else if ("Nhân viên".equals(h)) cell.setCellValue("Nguyễn Văn A");
                else if ("Ngày thành lập/sinh nhật".equals(h)) cell.setCellValue("15/05/1990");
                else if ("Tỉnh thành".equals(h)) cell.setCellValue("Hồ Chí Minh");
                else if ("Facebook".equals(h)) cell.setCellValue("fb.com/ankhang");
                else if ("Ghi chú".equals(h)) cell.setCellValue("Khách hàng thân thiết");
                else if ("Điểm tích lũy ban đầu".equals(h)) cell.setCellValue(100);
                else cell.setCellValue("");
            }

            Row sampleRow2 = sheet.createRow(2);
            sampleRow2.setHeightInPoints(20);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = sampleRow2.createCell(i);
                cell.setCellStyle(textStyle);
                String h = headers.get(i);
                if ("Mã khách".equals(h)) cell.setCellValue("KH00002");
                else if ("Tên khách hàng".equals(h)) cell.setCellValue("Anh Hòa");
                else if ("Địa chỉ".equals(h)) cell.setCellValue("45 Lê Lợi, TP. Đà Nẵng");
                else if ("Điện thoại".equals(h)) cell.setCellValue("0987654321");
                else if ("Email".equals(h)) cell.setCellValue("hoa.anh@example.com");
                else if ("Nhóm khách hàng".equals(h)) cell.setCellValue("Khách lẻ");
                else if ("Mã số thuế".equals(h)) cell.setCellValue("");
                else if ("Nhân viên".equals(h)) cell.setCellValue("");
                else if ("Ngày thành lập/sinh nhật".equals(h)) cell.setCellValue("20/11/1988");
                else if ("Tỉnh thành".equals(h)) cell.setCellValue("Đà Nẵng");
                else if ("Facebook".equals(h)) cell.setCellValue("");
                else if ("Ghi chú".equals(h)) cell.setCellValue("");
                else if ("Điểm tích lũy ban đầu".equals(h)) cell.setCellValue(0);
                else cell.setCellValue("");
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(currentWidth + 1200, 3500));
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            int open = JOptionPane.showConfirmDialog(
                    parent,
                    "Đã xuất file mẫu thành công! Bạn có muốn mở file ngay không?",
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

    public static void exportKhachHangList(Component parent, List<KhachHang> khachHangList, List<String> exportCols, String defaultFileName) {
        if (khachHangList == null || khachHangList.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Không có dữ liệu khách hàng để xuất Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> headers = (exportCols != null && !exportCols.isEmpty()) ? exportCols : DEFAULT_VISIBLE_COLUMNS;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất danh mục khách hàng ra file Excel");
        chooser.setSelectedFile(new File(defaultFileName != null ? defaultFileName : "DanhMuc_KhachHang.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh mục khách hàng");

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

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.##"));

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            int rIdx = 1;
            for (KhachHang kh : khachHangList) {
                Row row = sheet.createRow(rIdx++);
                row.setHeightInPoints(20);

                for (int c = 0; c < headers.size(); c++) {
                    String h = headers.get(c);
                    Cell cell = row.createCell(c);

                    if ("Mã khách".equals(h)) {
                        cell.setCellValue(kh.getMaKhach() != null ? kh.getMaKhach() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Tên khách hàng".equals(h)) {
                        cell.setCellValue(kh.getName() != null ? kh.getName() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Địa chỉ".equals(h)) {
                        cell.setCellValue(kh.getDiaChi() != null ? kh.getDiaChi() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Điện thoại".equals(h)) {
                        cell.setCellValue(kh.getDienThoai() != null ? kh.getDienThoai() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Email".equals(h)) {
                        cell.setCellValue(kh.getEmail() != null ? kh.getEmail() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Nhóm khách hàng".equals(h)) {
                        cell.setCellValue(kh.getTenNhomKhachHang() != null ? kh.getTenNhomKhachHang() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Mã số thuế".equals(h)) {
                        cell.setCellValue(kh.getMaSoThue() != null ? kh.getMaSoThue() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Nhân viên".equals(h)) {
                        cell.setCellValue(kh.getTenNhanVien() != null ? kh.getTenNhanVien() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Ngày thành lập/sinh nhật".equals(h)) {
                        String dob = kh.getNgaySinhNhat() != null ? sdf.format(new Date(kh.getNgaySinhNhat().getTime())) : "";
                        cell.setCellValue(dob);
                        cell.setCellStyle(textStyle);
                    } else if ("Tỉnh thành".equals(h)) {
                        cell.setCellValue(kh.getTinhThanh() != null ? kh.getTinhThanh() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Facebook".equals(h)) {
                        cell.setCellValue(kh.getFacebook() != null ? kh.getFacebook() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Ghi chú".equals(h)) {
                        cell.setCellValue(kh.getGhiChu() != null ? kh.getGhiChu() : "");
                        cell.setCellStyle(textStyle);
                    } else if ("Điểm tích lũy ban đầu".equals(h)) {
                        cell.setCellValue(kh.getDiemTichLuyBanDau());
                        cell.setCellStyle(numberStyle);
                    } else {
                        cell.setCellValue("");
                        cell.setCellStyle(textStyle);
                    }
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(currentWidth + 1000, 3200));
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            int open = JOptionPane.showConfirmDialog(
                    parent,
                    "Đã xuất danh sách " + khachHangList.size() + " khách hàng thành công! Bạn có muốn mở file ngay không?",
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
}
