package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoTonKhoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

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

    @Test
    public void testHangTheoHanDungReport() throws Exception {
        BaoCaoTonKhoReportService service = new BaoCaoTonKhoReportService();

        JasperReport report = service.getCompiledReportHangTheoHanDung();
        Assertions.assertNotNull(report, "JasperReport Báo cáo hàng theo hạn dùng không được null");

        JasperPrint print = service.generateHangTheoHanDungReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Báo cáo hàng theo hạn dùng không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Hàng theo hạn dùng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testHangDaHetHanReport() throws Exception {
        BaoCaoTonKhoReportService service = new BaoCaoTonKhoReportService();

        JasperReport report = service.getCompiledReportHangDaHetHan();
        Assertions.assertNotNull(report, "JasperReport Báo cáo hàng đã hết hạn không được null");

        JasperPrint print = service.generateHangDaHetHanReport(new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Báo cáo hàng đã hết hạn không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Hàng đã hết hạn tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testHangTonKhoHanDungReport() throws Exception {
        BaoCaoTonKhoReportService service = new BaoCaoTonKhoReportService();

        JasperReport report = service.getCompiledReportHangTonKhoHanDung();
        Assertions.assertNotNull(report, "JasperReport Báo cáo hàng tồn kho có hạn dùng không được null");

        JasperPrint print = service.generateHangTonKhoHanDungReport(new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Báo cáo hàng tồn kho có hạn dùng không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Hàng tồn kho có hạn dùng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testHangTonKhoReport() throws Exception {
        BaoCaoTonKhoReportService service = new BaoCaoTonKhoReportService();

        JasperReport report = service.getCompiledReportHangTonKho();
        Assertions.assertNotNull(report, "JasperReport Báo cáo hàng tồn kho không được null");

        JasperPrint print = service.generateHangTonKhoReport(null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Báo cáo hàng tồn kho không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Hàng tồn kho tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopXuatNhapTonReport() throws Exception {
        BaoCaoTonKhoReportService service = new BaoCaoTonKhoReportService();

        JasperReport report = service.getCompiledReportTongHopXuatNhapTon();
        Assertions.assertNotNull(report, "JasperReport Báo cáo tổng hợp xuất nhập tồn không được null");

        JasperPrint print = service.generateTongHopXuatNhapTonReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Báo cáo tổng hợp xuất nhập tồn không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp xuất nhập tồn tạo thành công! Số trang: " + print.getPages().size());
    }
}
