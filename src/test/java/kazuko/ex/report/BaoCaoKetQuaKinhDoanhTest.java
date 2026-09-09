package kazuko.ex.report;

import kazuko.ex.report.model.BaoCaoKetQuaKinhDoanhReportRow;
import kazuko.ex.report.service.BaoCaoPhanTichKinhDoanhReportService;
import kazuko.ex.ui.baocao.BaoCaoKetQuaKinhDoanhPanel;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaoCaoKetQuaKinhDoanhTest {

    private final BaoCaoPhanTichKinhDoanhReportService service = new BaoCaoPhanTichKinhDoanhReportService();

    @Test
    void testFetchKetQuaKinhDoanh() {
        List<BaoCaoKetQuaKinhDoanhReportRow> rows = service.fetchKetQuaKinhDoanh(new Date(), new Date(), null);
        assertNotNull(rows);
        assertFalse(rows.isEmpty());

        // Check for sections: I. DOANH THU, II. CHI PHÍ, III. LÃI/LỖ
        boolean hasDoanhThu = rows.stream().anyMatch(r -> "DOANH THU".equalsIgnoreCase(r.getTenMuc()) && "I.".equals(r.getStt()));
        boolean hasChiPhi = rows.stream().anyMatch(r -> "CHI PHÍ".equalsIgnoreCase(r.getTenMuc()) && "II.".equals(r.getStt()));
        boolean hasLaiLo = rows.stream().anyMatch(r -> "LÃI/LỖ".equalsIgnoreCase(r.getTenMuc()) && "III.".equals(r.getStt()));

        assertTrue(hasDoanhThu, "Phải có mục I. DOANH THU");
        assertTrue(hasChiPhi, "Phải có mục II. CHI PHÍ");
        assertTrue(hasLaiLo, "Phải có mục III. LÃI/LỖ");

        // Check for sub-groups under II. CHI PHÍ
        boolean hasDinhPhi = rows.stream().anyMatch(r -> "Định phí".equalsIgnoreCase(r.getTenMuc()) && "A.".equals(r.getStt()));
        boolean hasBienPhi = rows.stream().anyMatch(r -> "Biến phí".equalsIgnoreCase(r.getTenMuc()) && "B.".equals(r.getStt()));
        boolean hasChiKhac = rows.stream().anyMatch(r -> "Chi khác".equalsIgnoreCase(r.getTenMuc()) && "C.".equals(r.getStt()));

        assertTrue(hasDinhPhi, "Phải có mục A. Định phí");
        assertTrue(hasBienPhi, "Phải có mục B. Biến phí");
        assertTrue(hasChiKhac, "Phải có mục C. Chi khác");
    }

    @Test
    void testGenerateKetQuaKinhDoanhReport() throws Exception {
        JasperPrint print = service.generateKetQuaKinhDoanh(new Date(), new Date(), null);
        assertNotNull(print);
        assertEquals("bao_cao_ket_qua_kinh_doanh", print.getName());
        assertFalse(print.getPages().isEmpty(), "Report must contain at least 1 page");
    }

    @Test
    void testBaoCaoKetQuaKinhDoanhPanelCreation() {
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            BaoCaoKetQuaKinhDoanhPanel panel = new BaoCaoKetQuaKinhDoanhPanel();
            assertNotNull(panel);
        }
    }
}
