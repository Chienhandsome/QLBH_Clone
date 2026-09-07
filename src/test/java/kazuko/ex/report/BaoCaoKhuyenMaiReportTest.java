package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoKhuyenMaiReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaoCaoKhuyenMaiReportTest {

    @Test
    public void testDotKhuyenMaiReport() throws Exception {
        BaoCaoKhuyenMaiReportService service = new BaoCaoKhuyenMaiReportService();

        JasperReport report = service.getCompiledReportKm();
        Assertions.assertNotNull(report, "JasperReport Đợt khuyến mại không được null");

        JasperPrint print = service.generateDotKhuyenMai();
        Assertions.assertNotNull(print, "JasperPrint Đợt khuyến mại không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Đợt khuyến mại tạo thành công! Số trang: " + print.getPages().size());
    }
}