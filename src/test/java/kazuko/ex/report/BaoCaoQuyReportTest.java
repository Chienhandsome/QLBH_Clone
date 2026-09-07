package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoQuyReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaoCaoQuyReportTest {

    @Test
    public void testTongHopThuChiTheoNgayReport() throws Exception {
        BaoCaoQuyReportService service = new BaoCaoQuyReportService();

        JasperReport report = service.getCompiledReportNgay();
        Assertions.assertNotNull(report, "JasperReport TongHopNgay không được null");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = sdf.parse("01/08/2026");
        Date toDate = sdf.parse("30/09/2026");

        JasperPrint print = service.generateTongHopThuChiTheoNgay(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(print, "JasperPrint TongHopNgay không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp thu chi theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopThuChiTheoLyDoReport() throws Exception {
        BaoCaoQuyReportService service = new BaoCaoQuyReportService();

        JasperReport report = service.getCompiledReportLyDo();
        Assertions.assertNotNull(report, "JasperReport TongHopLyDo không được null");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = sdf.parse("01/08/2026");
        Date toDate = sdf.parse("30/09/2026");

        JasperPrint print = service.generateTongHopThuChiTheoLyDo(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(print, "JasperPrint TongHopLyDo không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp thu chi theo lý do tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBaoCaoTonQuyReport() throws Exception {
        BaoCaoQuyReportService service = new BaoCaoQuyReportService();

        JasperReport report = service.getCompiledReportTonQuy();
        Assertions.assertNotNull(report, "JasperReport TonQuy không được null");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = sdf.parse("01/09/2026");
        Date toDate = sdf.parse("07/09/2026");

        JasperPrint print = service.generateBaoCaoTonQuy(fromDate, toDate, "ALL", "ALL", "ALL");
        Assertions.assertNotNull(print, "JasperPrint TonQuy không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tồn quỹ tạo thành công! Số trang: " + print.getPages().size());
    }
}