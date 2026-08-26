package kazuko.ex.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelExporterTest {

    @Test
    public void testExportTableWithFilteredRows(@TempDir Path tempDir) throws Exception {
        String[] columns = {"", "Mã hàng", "Tên mặt hàng", "Nhóm mặt hàng", "Đơn vị tính", "Giá bán", "Không in chế biến"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Giả lập chỉ 3 dòng hiển thị trên table (sau khi người dùng lọc "bàn")
        model.addRow(new Object[]{"", "1100001", "Bàn chải mủ hình chân", "Bàn chải", "Cái", 7000.0, false});
        model.addRow(new Object[]{"", "1100002", "Bàn chải mủ rẻ", "Bàn chải", "Cái", 4000.0, true});
        model.addRow(new Object[]{"", "1100003", "Bàn chải ủi", "Bàn chải", "Cái", 7000.0, false});

        JTable table = new JTable(model);
        File exportFile = tempDir.resolve("MatHang_Filtered.xlsx").toFile();

        int exportedRows = ExcelExporter.exportTableToFile(table, "Mặt hàng", exportFile);
        assertEquals(3, exportedRows, "Phải xuất đúng 3 dòng đang hiển thị");
        assertTrue(exportFile.exists() && exportFile.length() > 0, "File excel phải được tạo thành công");

        // Đọc lại file Excel để kiểm tra nội dung chi tiết
        try (FileInputStream fis = new FileInputStream(exportFile);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet("Mặt hàng");
            assertNotNull(sheet, "Sheet 'Mặt hàng' phải tồn tại");

            // Kiểm tra header
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("STT", headerRow.getCell(0).getStringCellValue());
            assertEquals("Mã hàng", headerRow.getCell(1).getStringCellValue());
            assertEquals("Tên mặt hàng", headerRow.getCell(2).getStringCellValue());
            assertEquals("Nhóm mặt hàng", headerRow.getCell(3).getStringCellValue());
            assertEquals("Đơn vị tính", headerRow.getCell(4).getStringCellValue());
            assertEquals("Giá bán", headerRow.getCell(5).getStringCellValue());
            assertEquals("Không in chế biến", headerRow.getCell(6).getStringCellValue());

            // Kiểm tra dòng 1
            Row row1 = sheet.getRow(1);
            assertEquals(1, (int) row1.getCell(0).getNumericCellValue());
            assertEquals("1100001", row1.getCell(1).getStringCellValue());
            assertEquals("Bàn chải mủ hình chân", row1.getCell(2).getStringCellValue());
            assertEquals("Bàn chải", row1.getCell(3).getStringCellValue());
            assertEquals("Cái", row1.getCell(4).getStringCellValue());
            assertEquals(7000.0, row1.getCell(5).getNumericCellValue(), 0.001);
            assertEquals("Không", row1.getCell(6).getStringCellValue());

            // Kiểm tra dòng 2
            Row row2 = sheet.getRow(2);
            assertEquals(2, (int) row2.getCell(0).getNumericCellValue());
            assertEquals("1100002", row2.getCell(1).getStringCellValue());
            assertEquals("Bàn chải mủ rẻ", row2.getCell(2).getStringCellValue());
            assertEquals(4000.0, row2.getCell(5).getNumericCellValue(), 0.001);
            assertEquals("Có", row2.getCell(6).getStringCellValue());

            // Kiểm tra dòng 3
            Row row3 = sheet.getRow(3);
            assertEquals(3, (int) row3.getCell(0).getNumericCellValue());
            assertEquals("1100003", row3.getCell(1).getStringCellValue());
            assertEquals("Bàn chải ủi", row3.getCell(2).getStringCellValue());
            assertEquals(7000.0, row3.getCell(5).getNumericCellValue(), 0.001);

            // Đảm bảo không có dòng thứ 4
            assertNull(sheet.getRow(4), "Không được có thêm dòng ngoài danh sách lọc");
        }
    }

    @Test
    public void testExportKhachHangTable(@TempDir Path tempDir) throws Exception {
        String[] columns = {"", "Mã khách", "Tên khách hàng", "Địa chỉ", "Điện thoại", "Email", "Nhóm khách hàng", "Mã số thuế", "Nhân viên", "Ngày thành lập/sinh nhật"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        model.addRow(new Object[]{"01", "KH001", "Công ty TNHH Ánh Dương", "123 Lê Lợi, Q1", "0901234567", "info@anhduong.vn", "Khách VIP", "0312345678", "Hoàng Giang", "15/08/1990"});
        model.addRow(new Object[]{"02", "KH002", "Cửa hàng Minh Khang", "456 Nguyễn Huệ", "0912345678", "minhkhang@gmail.com", "Đại lý", "0398765432", "Phạm Quế Chi", "20/11/1985"});

        JTable table = new JTable(model);
        File exportFile = tempDir.resolve("KhachHang.xlsx").toFile();

        int count = ExcelExporter.exportTableToFile(table, "Khách hàng", exportFile);
        assertEquals(2, count);

        try (FileInputStream fis = new FileInputStream(exportFile);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet("Khách hàng");
            assertNotNull(sheet);

            Row r1 = sheet.getRow(1);
            assertEquals("KH001", r1.getCell(1).getStringCellValue());
            assertEquals("Công ty TNHH Ánh Dương", r1.getCell(2).getStringCellValue());
            assertEquals("0901234567", r1.getCell(4).getStringCellValue());

            Row r2 = sheet.getRow(2);
            assertEquals("KH002", r2.getCell(1).getStringCellValue());
            assertEquals("Cửa hàng Minh Khang", r2.getCell(2).getStringCellValue());
        }
    }
}
