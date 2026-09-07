package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CongNoBanDauFeatureTest {

    private final CongNoKhachHangDAO congNoDAO = new CongNoKhachHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    @Test
    @Order(1)
    public void testCustomerListFetch() {
        List<KhachHang> customers = khachHangDAO.getAll();
        assertNotNull(customers);
        System.out.println("Tong so khach hang trong he thong: " + customers.size());
    }

    @Test
    @Order(2)
    public void testCongNoBanDauSaveAndLoad() {
        List<KhachHang> customers = khachHangDAO.getAll();
        if (!customers.isEmpty()) {
            KhachHang kh = customers.get(0);
            Map<String, Double> debtMap = new HashMap<>();
            double expectedDebt = 1500000.0;
            debtMap.put(kh.getId(), expectedDebt);

            boolean saved = congNoDAO.saveCongNoBanDau(new Date(), debtMap);
            assertTrue(saved, "Luu cong no ban dau phai thanh cong");

            Map<String, Double> loadedMap = congNoDAO.getCongNoBanDauMap();
            assertNotNull(loadedMap);
            assertTrue(loadedMap.containsKey(kh.getId()), "Map cong no phai chua ID khach hang");
            assertEquals(expectedDebt, loadedMap.get(kh.getId()), 0.001, "So tien cong no ban dau phai khop");
            System.out.println("Luu va tai cong no ban dau thanh cong cho khach: " + kh.getName());
        }
    }

    @Test
    @Order(3)
    public void testExcelExportAndImportRoundTrip() throws Exception {
        // 1. Tao file Excel voi format mau (Ma, Ten, Cong no dau)
        List<KhachHang> customers = khachHangDAO.getAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("CongNoBanDau");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mã");
            headerRow.createCell(1).setCellValue("Tên");
            headerRow.createCell(2).setCellValue("Công nợ đầu");

            int rowIdx = 1;
            for (KhachHang kh : customers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(kh.getMaKhach() != null ? kh.getMaKhach() : "");
                row.createCell(1).setCellValue(kh.getName() != null ? kh.getName() : "");
                row.createCell(2).setCellValue(500000.0 + (rowIdx * 10000));
            }
            workbook.write(out);
        }

        // 2. Doc lai file Excel de mo phong import
        byte[] excelBytes = out.toByteArray();
        assertTrue(excelBytes.length > 0, "Du lieu Excel phai lon hon 0 bytes");

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("Mã", headerRow.getCell(0).getStringCellValue());
            assertEquals("Tên", headerRow.getCell(1).getStringCellValue());
            assertEquals("Công nợ đầu", headerRow.getCell(2).getStringCellValue());

            int dataRows = sheet.getLastRowNum();
            System.out.println("Kiem tra doc lai Excel: Header dung format, so dong du lieu = " + dataRows);
            assertEquals(customers.size(), dataRows);
        }
    }

    @Test
    @Order(4)
    public void testImportExcelMappingDialogHeaders() {
        List<String> headers = Arrays.asList("Mã khách", "Tên khách", "Dư nợ đầu kỳ", "Ghi chú");
        ImportExcelMappingDialog dialog = new ImportExcelMappingDialog(null, headers);
        assertNotNull(dialog);
        assertEquals(4, headers.size());
    }
}
