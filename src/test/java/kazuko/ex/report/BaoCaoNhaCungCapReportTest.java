package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoNhaCungCapReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaoCaoNhaCungCapReportTest {

    @Test
    public void testNhaCungCapTheoNhomReport() throws Exception {
        BaoCaoNhaCungCapReportService service = new BaoCaoNhaCungCapReportService();

        JasperReport report = service.getCompiledReportNhom();
        Assertions.assertNotNull(report, "JasperReport NCC Nhom không được null");

        JasperPrint print = service.generateNhaCungCapTheoNhom(null);
        Assertions.assertNotNull(print, "JasperPrint NCC Nhom không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Nhà cung cấp theo nhóm tạo thành công! Số trang: " + print.getPages().size());
    }
}