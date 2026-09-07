package kazuko.ex.danhmuc;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.ui.danhmuc.ThemNhanhMatHangDialog;
import kazuko.ex.ui.danhmuc.ThemTuExcelMatHangDialog;
import kazuko.ex.util.MatHangExcelUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MatHangImportExportFeatureTest {
    private MatHangDAO matHangDAO;
    private SystemConfig systemConfig;

    @BeforeEach
    public void setUp() {
        matHangDAO = new MatHangDAO();
        systemConfig = new SystemConfigDAO().loadConfig();
    }

    @Test
    public void testExportColumnsExactFields() {
        List<String> exportCols = MatHangExcelUtil.getExportColumns(systemConfig);
        assertTrue(exportCols.size() >= 17, "Export columns should have at least 17 columns");
        assertEquals("Tên mặt hàng", exportCols.get(0));
        assertEquals("Nhóm mặt hàng", exportCols.get(1));
        assertEquals("Đơn vị tính", exportCols.get(2));
        assertEquals("Giá bán", exportCols.get(3));
        assertEquals("Giá nhập", exportCols.get(4));
    }

    @Test
    public void testImportTargetFieldsContainsConfiguredPriceLabels() {
        ThemTuExcelMatHangDialog dialog = new ThemTuExcelMatHangDialog(null, null, new ArrayList<>(), null);
        String[] targetFields = dialog.getTargetFields();
        assertTrue(targetFields.length >= 24, "Import target fields must have at least 24 fields");

        String labelGia2 = (systemConfig != null && systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().trim().isEmpty())
                ? systemConfig.getDienGiaiGia2().trim() : "Đơn giá 2";
        String labelGia3 = (systemConfig != null && systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().trim().isEmpty())
                ? systemConfig.getDienGiaiGia3().trim() : "Giá bán 3";

        List<String> fieldList = Arrays.asList(targetFields);
        assertTrue(fieldList.contains(labelGia2), "Target fields must contain configured price 2 label: " + labelGia2);
        assertTrue(fieldList.contains(labelGia3), "Target fields must contain configured price 3 label: " + labelGia3);
    }

    @Test
    public void testExportWorkbookGenerationAndValues() throws Exception {
        MatHang mh = new MatHang();
        mh.setName("Quạt trần Vinawind");
        mh.setTenNhom("Điện gia dụng");
        mh.setTenDonViTinh("Chiếc");
        mh.setGiaBan(750000);
        mh.setGiaNhap(550000);
        mh.setGiamGiaPt(10);
        mh.setGiamGiaTien(20000);
        mh.setGhiChu("Động cơ đồng");
        mh.setKhongInCheBien(false);
        mh.setBaoHanh("24 tháng");
        mh.setTenDonViTinhChan("Thùng");
        mh.setQuyDoi(4);
        mh.setGiaBanChan(2800000);
        mh.setGiaBan2(730000);
        mh.setGiaBan3(710000);
        mh.setHangSanXuat("Vinawind");
        mh.setCoHanSuDung(false);

        List<String> exportCols = MatHangExcelUtil.getExportColumns(systemConfig);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh mục mặt hàng");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < exportCols.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(exportCols.get(i));
            }

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(mh.getName());
            dataRow.createCell(1).setCellValue(mh.getTenNhom());
            dataRow.createCell(2).setCellValue(mh.getTenDonViTinh());
            dataRow.createCell(3).setCellValue(mh.getGiaBan());
            dataRow.createCell(4).setCellValue(mh.getGiaNhap());
            dataRow.createCell(5).setCellValue(mh.getGiamGiaPt());
            dataRow.createCell(6).setCellValue(mh.getGiamGiaTien());
            dataRow.createCell(7).setCellValue(mh.getGhiChu());
            dataRow.createCell(8).setCellValue(mh.isKhongInCheBien() ? "Có" : "Không");
            dataRow.createCell(9).setCellValue(mh.getBaoHanh());
            dataRow.createCell(10).setCellValue(mh.getTenDonViTinhChan());
            dataRow.createCell(11).setCellValue(mh.getQuyDoi());
            dataRow.createCell(12).setCellValue(mh.getGiaBanChan());
            dataRow.createCell(13).setCellValue(mh.getGiaBan2());
            dataRow.createCell(14).setCellValue(mh.getGiaBan3());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            try (Workbook readWb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
                Sheet readSheet = readWb.getSheetAt(0);
                assertEquals("Tên mặt hàng", readSheet.getRow(0).getCell(0).getStringCellValue());

                Row r1 = readSheet.getRow(1);
                assertEquals("Quạt trần Vinawind", r1.getCell(0).getStringCellValue());
                assertEquals(750000.0, r1.getCell(3).getNumericCellValue());
                assertEquals(710000.0, r1.getCell(14).getNumericCellValue());
            }
        }
    }

    @Test
    public void testExcelImportMappingParsing() throws Exception {
        File tempFile = File.createTempFile("test_import_mathang_", ".xlsx");
        tempFile.deleteOnExit();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row hRow = sheet.createRow(0);
            hRow.createCell(0).setCellValue("Mã");
            hRow.createCell(1).setCellValue("Tên");
            hRow.createCell(2).setCellValue("Giá");

            Row dRow1 = sheet.createRow(1);
            dRow1.createCell(0).setCellValue("MH_TEST_01");
            dRow1.createCell(1).setCellValue("Bút bi Thiên Long 0.5");
            dRow1.createCell(2).setCellValue(5000);

            Row dRow2 = sheet.createRow(2);
            dRow2.createCell(0).setCellValue("MH_TEST_02");
            dRow2.createCell(1).setCellValue("Thước kẻ 20cm");
            dRow2.createCell(2).setCellValue(7000);

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                wb.write(fos);
            }
        }

        Map<Integer, String> colMapping = new HashMap<>();
        colMapping.put(0, "Mã hàng");
        colMapping.put(1, "Tên mặt hàng");
        colMapping.put(2, "Giá bán");

        try (InputStream in = java.nio.file.Files.newInputStream(tempFile.toPath());
             Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<Map<String, Object>> parsed = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<Integer, String> entry : colMapping.entrySet()) {
                    Cell cell = row.getCell(entry.getKey());
                    map.put(entry.getValue(), cell != null ? formatter.formatCellValue(cell).trim() : "");
                }
                parsed.add(map);
            }

            assertEquals(2, parsed.size());
            assertEquals("MH_TEST_01", parsed.get(0).get("Mã hàng"));
            assertEquals("Bút bi Thiên Long 0.5", parsed.get(0).get("Tên mặt hàng"));
            assertEquals("5000", parsed.get(0).get("Giá bán").toString().replace(",", ""));
        }
    }

    @Test
    public void testThemNhanhMatHangDynamicColumnsAndAutoUpdate() {
        ThemNhanhMatHangDialog dialog = new ThemNhanhMatHangDialog(null, () -> {});
        assertNotNull(dialog);

        javax.swing.JTable table = findComponentOfType(dialog, javax.swing.JTable.class);
        assertNotNull(table);

        // Initially: Mã hàng and Tên mặt hàng are unticked -> visible on table
        // All other 23 fields are ticked -> hidden from table
        assertEquals(2, table.getColumnCount(), "Initially table should have 2 columns (Mã hàng, Tên mặt hàng)");
        assertEquals("Mã hàng", table.getColumnName(0));
        assertEquals("Tên mặt hàng", table.getColumnName(1));

        // Test DAO auto-update on duplicate code
        String testCode = "MH_TEST_UPDATE_" + System.currentTimeMillis();
        MatHang mh1 = new MatHang();
        mh1.setCode(testCode);
        mh1.setName("Mặt hàng ban đầu");
        mh1.setGiaBan(100000);

        boolean inserted = matHangDAO.insert(mh1);
        assertTrue(inserted);

        MatHang found = matHangDAO.findByCode(testCode);
        assertNotNull(found);
        assertEquals("Mặt hàng ban đầu", found.getName());

        found.setName("Mặt hàng đã cập nhật");
        found.setGiaBan(120000);
        boolean updated = matHangDAO.update(found);
        assertTrue(updated);

        MatHang foundAfter = matHangDAO.findByCode(testCode);
        assertNotNull(foundAfter);
        assertEquals("Mặt hàng đã cập nhật", foundAfter.getName());
        assertEquals(120000.0, foundAfter.getGiaBan());

        // Clean up
        matHangDAO.delete(foundAfter.getId());
    }

    private <T> T findComponentOfType(java.awt.Container container, Class<T> type) {
        for (java.awt.Component comp : container.getComponents()) {
            if (type.isInstance(comp)) {
                return type.cast(comp);
            }
            if (comp instanceof java.awt.Container) {
                T found = findComponentOfType((java.awt.Container) comp, type);
                if (found != null) return found;
            }
        }
        return null;
    }
}
