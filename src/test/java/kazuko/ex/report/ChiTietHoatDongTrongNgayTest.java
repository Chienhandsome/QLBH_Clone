package kazuko.ex.report;

import kazuko.ex.report.model.ChiTietHoatDongTrongNgayReportRow;
import kazuko.ex.report.service.BaoCaoPhanTichKinhDoanhReportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChiTietHoatDongTrongNgayTest {

    private final BaoCaoPhanTichKinhDoanhReportService service = new BaoCaoPhanTichKinhDoanhReportService();

    @Test
    void testFetchChiTietHoatDongTrongNgay() {
        Map<String, Object> summary = new HashMap<>();
        List<ChiTietHoatDongTrongNgayReportRow> rows = service.fetchChiTietHoatDongTrongNgay(null, null, "", "", summary);

        assertNotNull(rows);
        assertNotNull(summary);
        assertTrue(summary.containsKey("sumTienMat"));
        assertTrue(summary.containsKey("sumTongCong"));
    }

    @Test
    void testGenerateChiTietHoatDongReport() throws Exception {
        JasperPrint print = service.generateChiTietHoatDongTrongNgay(new Date(), new Date(), "", "");
        assertNotNull(print);
        assertEquals("chi_tiet_hoat_dong_trong_ngay", print.getName());
    }
}
