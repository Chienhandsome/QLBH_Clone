package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoMatHangReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaoCaoMatHangReportTest {

    @Test
    public void testMatHangTheoNhomReport() throws Exception {
        BaoCaoMatHangReportService service = new BaoCaoMatHangReportService();

        JasperReport report = service.getCompiledReportNhom();
        Assertions.assertNotNull(report, "JasperReport Mặt hàng theo nhóm không được null");

        JasperPrint print = service.generateMatHangTheoNhom(null, null);
        Assertions.assertNotNull(print, "JasperPrint Mặt hàng theo nhóm không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Mặt hàng theo nhóm tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testMatHangTheoHangSanXuatReport() throws Exception {
        BaoCaoMatHangReportService service = new BaoCaoMatHangReportService();

        JasperReport report = service.getCompiledReportHsx();
        Assertions.assertNotNull(report, "JasperReport Mặt hàng theo HSX không được null");

        JasperPrint print = service.generateMatHangTheoHangSanXuat(null, null);
        Assertions.assertNotNull(print, "JasperPrint Mặt hàng theo HSX không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Mặt hàng theo hãng sản xuất tạo thành công! Số trang: " + print.getPages().size());
    }
}