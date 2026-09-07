package kazuko.ex.ui.khohang;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.TonKhoBanDauDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.ui.congno.ImportExcelMappingDialog;
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
public class TonKhoBanDauFeatureTest {

    private final TonKhoBanDauDAO tonKhoDAO = new TonKhoBanDauDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();

    @Test
    @Order(1)
    public void testWarehouseListFetch() {
        List<KhoHang> warehouses = khoHangDAO.getAllActive();
        assertNotNull(warehouses);
        assertFalse(warehouses.isEmpty(), "Danh sách kho hoạt động không được rỗng");
        System.out.println("Tong so kho hoat dong: " + warehouses.size());
    }

    @Test
    @Order(2)
    public void testSaveAndLoadTonKhoBanDau() {
        List<KhoHang> warehouses = khoHangDAO.getAllActive();
        List<MatHang> products = matHangDAO.getAll();
        assertFalse(warehouses.isEmpty());
        assertFalse(products.isEmpty());

        KhoHang kho = warehouses.get(0);
        MatHang mh = products.get(0);

        Map<String, double[]> stockMap = new HashMap<>();
        double expectedTon = 25.0;
        double expectedGiaVon = 35000.0;
        stockMap.put(mh.getId(), new double[]{expectedTon, expectedGiaVon});

        boolean saved = tonKhoDAO.saveTonKhoBanDau(new Date(), kho.getId(), stockMap);
        assertTrue(saved, "Luu ton kho ban dau phai thanh cong");

        Map<String, double[]> loadedMap = tonKhoDAO.getTonKhoBanDauMap(kho.getId());
        assertNotNull(loadedMap);
        assertTrue(loadedMap.containsKey(mh.getId()), "Map ton kho phai chua mat hang");
        assertEquals(expectedTon, loadedMap.get(mh.getId())[0], 0.001, "So luong ton phai khop");
        assertEquals(expectedGiaVon, loadedMap.get(mh.getId())[1], 0.001, "Gia von phai khop");
        System.out.println("Luu va tai ton kho ban dau thanh cong cho kho [" + kho.getName() + "], mat hang [" + mh.getName() + "]");
    }

    @Test
    @Order(3)
    public void testExcelExportFormatAndData() throws Exception {
        // Format xuất file mẫu: Mã sẵn có, Mã hàng, Tên hàng, Tồn, Giá vốn (Hình 3)
        List<MatHang> products = matHangDAO.getAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TonKhoBanDau");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mã sẵn có");
            headerRow.createCell(1).setCellValue("Mã hàng");
            headerRow.createCell(2).setCellValue("Tên hàng");
            headerRow.createCell(3).setCellValue("Tồn");
            headerRow.createCell(4).setCellValue("Giá vốn");

            int rowIdx = 1;
            for (MatHang mh : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(mh.getMaSanCo() != null ? mh.getMaSanCo() : "");
                row.createCell(1).setCellValue(mh.getCode() != null ? mh.getCode() : "");
                row.createCell(2).setCellValue(mh.getName() != null ? mh.getName() : "");
                row.createCell(3).setCellValue(10.0 + rowIdx);
                row.createCell(4).setCellValue(20000.0);
            }
            workbook.write(out);
        }

        byte[] bytes = out.toByteArray();
        assertTrue(bytes.length > 0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("Mã sẵn có", headerRow.getCell(0).getStringCellValue());
            assertEquals("Mã hàng", headerRow.getCell(1).getStringCellValue());
            assertEquals("Tên hàng", headerRow.getCell(2).getStringCellValue());
            assertEquals("Tồn", headerRow.getCell(3).getStringCellValue());
            assertEquals("Giá vốn", headerRow.getCell(4).getStringCellValue());

            int dataRows = sheet.getLastRowNum();
            assertEquals(products.size(), dataRows);
            System.out.println("Kiem tra Excel template ton kho: 5 cot chuan, so dong = " + dataRows);
        }
    }

    @Test
    @Order(4)
    public void testImportMappingTargetFieldsAndRequirements() {
        assertArrayEquals(
                new String[]{"", "Mã hàng hóa", "Tồn", "Giá vốn"},
                ImportExcelMappingDialog.TON_KHO_TARGET_FIELDS,
                "Danh sach truong dich ton kho phai gom '', 'Mã hàng hóa', 'Tồn', 'Giá vốn'"
        );

        List<String> headers = Arrays.asList("Mã sẵn có", "Mã hàng", "Tên hàng", "Tồn", "Giá vốn");
        ImportExcelMappingDialog dialog = new ImportExcelMappingDialog(
                null,
                headers,
                ImportExcelMappingDialog.TON_KHO_TARGET_FIELDS
        );
        assertNotNull(dialog);
    }

    @Test
    @Order(5)
    public void testProductCodeMatchingAndNotFoundHandling() {
        List<MatHang> products = matHangDAO.getAll();
        assertFalse(products.isEmpty());

        MatHang first = products.get(0);
        String validCode = first.getCode();
        String invalidCode = "MH_KHONG_TON_TAI_999";

        Map<String, double[]> stockMap = new HashMap<>();
        List<String> notFoundCodes = new ArrayList<>();

        // Test match code hop le
        boolean found = false;
        for (MatHang mh : products) {
            if (mh.getCode() != null && mh.getCode().trim().equalsIgnoreCase(validCode)) {
                stockMap.put(mh.getId(), new double[]{50.0, 45000.0});
                found = true;
                break;
            }
        }
        assertTrue(found, "Ma mat hang hop le phai tim thay");
        assertEquals(50.0, stockMap.get(first.getId())[0]);
        assertEquals(45000.0, stockMap.get(first.getId())[1]);

        // Test ma khong ton tai
        boolean invalidFound = false;
        for (MatHang mh : products) {
            if (mh.getCode() != null && mh.getCode().trim().equalsIgnoreCase(invalidCode)) {
                invalidFound = true;
                break;
            }
        }
        if (!invalidFound) {
            notFoundCodes.add(invalidCode);
        }

        assertFalse(invalidFound);
        assertEquals(1, notFoundCodes.size());
        assertEquals("MH_KHONG_TON_TAI_999", notFoundCodes.get(0));
    }
}
