package kazuko.ex.danhmuc;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.ui.danhmuc.ThemNhanhKhachHangDialog;
import kazuko.ex.ui.danhmuc.ThemTuExcelKhachHangDialog;
import kazuko.ex.util.KhachHangExcelUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.junit.jupiter.api.Assertions.*;

public class KhachHangImportExportFeatureTest {

    @Test
    public void testExcelColumnsDefinition() {
        List<String> allCols = KhachHangExcelUtil.ALL_POSSIBLE_COLUMNS;
        List<String> defaultCols = KhachHangExcelUtil.DEFAULT_VISIBLE_COLUMNS;

        assertEquals(13, allCols.size(), "All possible columns count must be 13");
        assertEquals(11, defaultCols.size(), "Default visible columns count must be 11");

        assertTrue(allCols.contains("Mã khách"));
        assertTrue(allCols.contains("Tên khách hàng"));
        assertTrue(allCols.contains("Địa chỉ"));
        assertTrue(allCols.contains("Điện thoại"));
        assertTrue(allCols.contains("Email"));
        assertTrue(allCols.contains("Nhóm khách hàng"));
        assertTrue(allCols.contains("Mã số thuế"));
        assertTrue(allCols.contains("Nhân viên"));
        assertTrue(allCols.contains("Ngày thành lập/sinh nhật"));
        assertTrue(allCols.contains("Tỉnh thành"));
        assertTrue(allCols.contains("Facebook"));
        assertTrue(allCols.contains("Ghi chú"));
        assertTrue(allCols.contains("Điểm tích lũy ban đầu"));

        assertFalse(defaultCols.contains("Ghi chú"));
        assertFalse(defaultCols.contains("Điểm tích lũy ban đầu"));
    }

    @Test
    public void testThemNhanhKhachHangDialogInitialization() {
        ThemNhanhKhachHangDialog dialog = new ThemNhanhKhachHangDialog(null, () -> {});
        assertNotNull(dialog);
        assertEquals("Thêm nhanh", dialog.getTitle());

        List<Map<String, Object>> sampleRows = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("Mã khách", "KH_TEST_01");
        r1.put("Tên khách hàng", "Khách hàng thử nghiệm 01");
        r1.put("Điện thoại", "0911223344");
        r1.put("Nhóm khách hàng", "Khách VIP");
        sampleRows.add(r1);

        assertDoesNotThrow(() -> dialog.loadParsedRowsToGrid(sampleRows));
    }

    @Test
    public void testThemTuExcelKhachHangAutoMatching() throws Exception {
        File tempFile = File.createTempFile("test_kh_", ".xlsx");
        tempFile.deleteOnExit();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("Mã KH");
            r0.createCell(1).setCellValue("Tên khách");
            r0.createCell(2).setCellValue("SĐT");
            r0.createCell(3).setCellValue("Nhóm khách");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("KH_AUTO_1");
            r1.createCell(1).setCellValue("Nguyễn Văn Test");
            r1.createCell(2).setCellValue("0909090909");
            r1.createCell(3).setCellValue("Nhóm Thử Nghiệm");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                wb.write(fos);
            }
        }

        List<String> headers = List.of("Mã KH", "Tên khách", "SĐT", "Nhóm khách");
        List<Map<String, Object>> resultHolder = new ArrayList<>();

        ThemTuExcelKhachHangDialog dialog = new ThemTuExcelKhachHangDialog(null, tempFile, headers, resultHolder::addAll);
        assertNotNull(dialog);
    }

    @Test
    public void testAutoUpdateExistingCustomerInDAO() {
        KhachHangDAO dao = new KhachHangDAO();
        String testMaKhach = "KH_TEST_UPDATE_" + System.currentTimeMillis();

        KhachHang kh1 = new KhachHang();
        kh1.setMaKhach(testMaKhach);
        kh1.setName("Khách Hàng Ban Đầu");
        kh1.setDiaChi("Địa chỉ ban đầu");
        kh1.setDienThoai("0123456789");

        boolean inserted = dao.insert(kh1);
        assertTrue(inserted, "Phải insert thành công khách hàng mới");

        KhachHang found = dao.findByMaKhach(testMaKhach);
        assertNotNull(found);
        assertEquals("Khách Hàng Ban Đầu", found.getName());

        // Update information
        found.setName("Khách Hàng Đã Cập Nhật Tự Động");
        found.setDiaChi("Địa chỉ mới sau cập nhật");
        boolean updated = dao.update(found);
        assertTrue(updated, "Phải update thành công khách hàng khi trùng mã");

        KhachHang foundAfter = dao.findByMaKhach(testMaKhach);
        assertNotNull(foundAfter);
        assertEquals("Khách Hàng Đã Cập Nhật Tự Động", foundAfter.getName());
        assertEquals("Địa chỉ mới sau cập nhật", foundAfter.getDiaChi());

        // Clean up
        dao.deletePermanently(foundAfter.getId());
    }

    @Test
    public void testDynamicGridColumnVisibilityAndEditableProperties() {
        ThemNhanhKhachHangDialog dialog = new ThemNhanhKhachHangDialog(null, () -> {});
        assertNotNull(dialog);

        // Find table inside dialog
        javax.swing.JTable table = findComponentOfType(dialog, javax.swing.JTable.class);
        assertNotNull(table, "JTable grid must be present in dialog");

        // Initially: Ma khach, Ten khach hang, Dia chi are unticked -> visible on table
        // All other 10 fields are ticked -> hidden from table
        // Total visible columns = 1 (pointer) + 3 (Ma, Ten, Dia chi) = 4
        assertEquals(4, table.getColumnCount(), "Initially table should have 4 columns (Pointer, Mã khách, Tên khách hàng, Địa chỉ)");
        assertEquals("", table.getColumnName(0));
        assertEquals("Mã khách", table.getColumnName(1));
        assertEquals("Tên khách hàng", table.getColumnName(2));
        assertEquals("Địa chỉ", table.getColumnName(3));

        // Find checkboxes
        List<javax.swing.JCheckBox> checkBoxes = findAllComponentsOfType(dialog, javax.swing.JCheckBox.class);
        assertFalse(checkBoxes.isEmpty());

        javax.swing.JCheckBox chkDienThoai = checkBoxes.stream()
                .filter(c -> "Điện thoại:".equals(c.getText()))
                .findFirst().orElse(null);
        assertNotNull(chkDienThoai, "CheckBox Điện thoại must exist");
        assertTrue(chkDienThoai.isSelected(), "CheckBox Điện thoại should be selected initially");

        // Simulate unchecking Dien thoai -> column Dien thoai must appear on table (5 columns)
        chkDienThoai.doClick();
        assertFalse(chkDienThoai.isSelected());
        assertEquals(5, table.getColumnCount(), "Table should now have 5 columns after unchecking Điện thoại");
        assertEquals("Điện thoại", table.getColumnName(4));

        // Simulate checking Dia chi -> column Dia chi must disappear from table
        javax.swing.JCheckBox chkDiaChi = checkBoxes.stream()
                .filter(c -> "Địa chỉ:".equals(c.getText()))
                .findFirst().orElse(null);
        assertNotNull(chkDiaChi, "CheckBox Địa chỉ must exist");
        assertFalse(chkDiaChi.isSelected(), "CheckBox Địa chỉ should be unselected initially");

        chkDiaChi.doClick();
        assertTrue(chkDiaChi.isSelected());
        assertEquals(4, table.getColumnCount(), "Table should have 4 columns after checking Địa chỉ");
        assertEquals("Điện thoại", table.getColumnName(3));
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

    private <T> List<T> findAllComponentsOfType(java.awt.Container container, Class<T> type) {
        List<T> list = new ArrayList<>();
        for (java.awt.Component comp : container.getComponents()) {
            if (type.isInstance(comp)) {
                list.add(type.cast(comp));
            }
            if (comp instanceof java.awt.Container) {
                list.addAll(findAllComponentsOfType((java.awt.Container) comp, type));
            }
        }
        return list;
    }
}
