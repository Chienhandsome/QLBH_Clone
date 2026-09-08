package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoTonKhoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaoCaoTonKhoReportTest {

    @Test
    public void testTonKhoTheoKichThuocReport() throws Exception {
        BaoCaoTonKhoReportService service = new BaoCaoTonKhoReportService();

        JasperReport report = service.getCompiledReportTonKhoTheoKichThuoc();
        Assertions.assertNotNull(report, "JasperReport Báo cáo tồn kho theo kích thước không được null");

        JasperPrint print = service.generateTonKhoTheoKichThuocReport(null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Báo cáo tồn kho theo kích thước không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tồn kho theo kích thước tạo thành công! Số trang: " + print.getPages().size());
    }
}
