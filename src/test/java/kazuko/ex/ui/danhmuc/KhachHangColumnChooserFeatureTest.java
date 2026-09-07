package kazuko.ex.ui.danhmuc;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class KhachHangColumnChooserFeatureTest {

    @Test
    public void testDefaultAndPossibleColumns() {
        DanhMucKhachHangPanel panel = new DanhMucKhachHangPanel();

        List<String> defaultVisible = panel.getDefaultVisibleColumns();
        List<String> allPossible = panel.getAllPossibleColumns();

        assertNotNull(defaultVisible);
        assertNotNull(allPossible);

        assertEquals(11, defaultVisible.size(), "Default visible columns count should be 11");
        assertEquals(13, allPossible.size(), "All possible columns count should be 13");

        // Verify default visible list
        assertEquals("Mã khách", defaultVisible.get(0));
        assertEquals("Tên khách hàng", defaultVisible.get(1));
        assertEquals("Địa chỉ", defaultVisible.get(2));
        assertEquals("Điện thoại", defaultVisible.get(3));
        assertEquals("Email", defaultVisible.get(4));
        assertEquals("Nhóm khách hàng", defaultVisible.get(5));
        assertEquals("Mã số thuế", defaultVisible.get(6));
        assertEquals("Nhân viên", defaultVisible.get(7));
        assertEquals("Ngày thành lập/sinh nhật", defaultVisible.get(8));
        assertEquals("Tỉnh thành", defaultVisible.get(9));
        assertEquals("Facebook", defaultVisible.get(10));

        // Verify hidden default columns
        assertTrue(allPossible.contains("Điểm tích lũy ban đầu"));
        assertTrue(allPossible.contains("Ghi chú"));
        assertFalse(defaultVisible.contains("Điểm tích lũy ban đầu"));
        assertFalse(defaultVisible.contains("Ghi chú"));
    }

    @Test
    public void testRebuildTableColumnsAndRendering() {
        DanhMucKhachHangPanel panel = new DanhMucKhachHangPanel();

        List<String> customCols = List.of(
                "Mã khách",
                "Tên khách hàng",
                "Điểm tích lũy ban đầu",
                "Ghi chú",
                "Facebook"
        );

        panel.rebuildTableColumns(customCols);

        assertEquals(customCols, panel.getVisibleColumns());

        // autoResizeColumnWidths should run smoothly
        assertDoesNotThrow(panel::autoResizeColumnWidths);
    }

    @Test
    public void testChonCotHienThiDialogCallback() {
        DanhMucKhachHangPanel panel = new DanhMucKhachHangPanel();
        List<String> defaultVisible = panel.getDefaultVisibleColumns();
        List<String> allPossible = panel.getAllPossibleColumns();

        AtomicReference<List<String>> resultHolder = new AtomicReference<>();

        ChonCotHienThiDialog dialog = new ChonCotHienThiDialog(
                null,
                defaultVisible,
                allPossible,
                defaultVisible,
                resultHolder::set
        );

        assertNotNull(dialog);
    }
}

