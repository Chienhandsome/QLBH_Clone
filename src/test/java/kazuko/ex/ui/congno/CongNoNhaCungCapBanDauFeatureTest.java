package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CongNoNhaCungCapBanDauFeatureTest {

    private final CongNoNhaCungCapDAO congNoDAO = new CongNoNhaCungCapDAO();
    private final NhaCungCapDAO nccDAO = new NhaCungCapDAO();

    @Test
    @Order(1)
    public void testSupplierListFetch() {
        List<NhaCungCap> suppliers = nccDAO.getAll();
        assertNotNull(suppliers);
        System.out.println("Tong so nha cung cap trong he thong: " + suppliers.size());
    }

    @Test
    @Order(2)
    public void testCongNoBanDauNCCSaveAndLoad() {
        List<NhaCungCap> suppliers = nccDAO.getAll();
        if (!suppliers.isEmpty()) {
            NhaCungCap ncc = suppliers.get(0);
            Map<String, Double> debtMap = new HashMap<>();
            double expectedDebt = 3500000.0;
            debtMap.put(ncc.getId(), expectedDebt);

            boolean saved = congNoDAO.saveCongNoBanDau(new Date(), debtMap);
            assertTrue(saved, "Luu cong no ban dau NCC phai thanh cong");

            Map<String, Double> loadedMap = congNoDAO.getCongNoBanDauMap();
            assertNotNull(loadedMap);
            assertTrue(loadedMap.containsKey(ncc.getId()), "Map cong no NCC phai chua ID");
            assertEquals(expectedDebt, loadedMap.get(ncc.getId()), 0.001, "So tien cong no ban dau NCC phai khop");
            System.out.println("Luu va tai cong no ban dau thanh cong cho NCC: " + ncc.getName());
        }
    }

    @Test
    @Order(3)
    public void testExcelExportAndImportRoundTripNCC() throws Exception {
        // 1. Tao file Excel voi format mau (Ma, Ten, Cong no dau)
        List<NhaCungCap> suppliers = nccDAO.getAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("CongNoBanDauNCC");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mã");
            headerRow.createCell(1).setCellValue("Tên");
            headerRow.createCell(2).setCellValue("Công nợ đầu");

            int rowIdx = 1;
            for (NhaCungCap ncc : suppliers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ncc.getMaNhaCungCap() != null ? ncc.getMaNhaCungCap() : "");
                row.createCell(1).setCellValue(ncc.getName() != null ? ncc.getName() : "");
                row.createCell(2).setCellValue(800000.0 + (rowIdx * 20000));
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
            System.out.println("Kiem tra doc lai Excel NCC: Header dung format, so dong du lieu = " + dataRows);
            assertEquals(suppliers.size(), dataRows);
        }
    }

    @Test
    @Order(4)
    public void testSupplierCodeMatchingAndNotFoundHandling() {
        List<NhaCungCap> suppliers = nccDAO.getAll();
        assertFalse(suppliers.isEmpty(), "Can co it nhat 1 nha cung cap de test");

        NhaCungCap firstNcc = suppliers.get(0);
        String validCode = firstNcc.getMaNhaCungCap();
        String invalidCode = "NCC_KHONG_TON_TAI_999";

        Map<String, Double> debtMap = new HashMap<>();
        List<String> notFoundCodes = new ArrayList<>();

        // Test matching code hop le
        boolean matched = false;
        for (NhaCungCap ncc : suppliers) {
            if (ncc.getMaNhaCungCap() != null && ncc.getMaNhaCungCap().trim().equalsIgnoreCase(validCode)) {
                debtMap.put(ncc.getId(), 4200000.0);
                matched = true;
                break;
            }
        }
        assertTrue(matched, "Ma NCC hop le phai tim thay");
        assertEquals(4200000.0, debtMap.get(firstNcc.getId()));

        // Test ma khong ton tai
        boolean invalidFound = false;
        for (NhaCungCap ncc : suppliers) {
            if (ncc.getMaNhaCungCap() != null && ncc.getMaNhaCungCap().trim().equalsIgnoreCase(invalidCode)) {
                invalidFound = true;
                break;
            }
        }
        if (!invalidFound) {
            notFoundCodes.add(invalidCode);
        }

        assertFalse(invalidFound, "Ma khong ton tai khong duoc tim thay");
        assertEquals(1, notFoundCodes.size());
        assertEquals("NCC_KHONG_TON_TAI_999", notFoundCodes.get(0));
    }
}
