package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoDatHangReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoDatHangReportTest {

    @Test
    public void testDanhSachDatHangTheoNgay() throws Exception {
        BaoCaoDatHangReportService service = new BaoCaoDatHangReportService();

        JasperReport report = service.getCompiledReportNgay();
        Assertions.assertNotNull(report, "JasperReport Danh sách đặt hàng theo ngày không được null");

        JasperPrint print = service.generateDanhSachDatHangTheoNgay(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách đặt hàng theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách đặt hàng theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachDatHangTheoKhachHang() throws Exception {
        BaoCaoDatHangReportService service = new BaoCaoDatHangReportService();

        JasperReport report = service.getCompiledReportKhachHang();
        Assertions.assertNotNull(report, "JasperReport Danh sách đặt hàng theo khách hàng không được null");

        JasperPrint print = service.generateDanhSachDatHangTheoKhachHang(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách đặt hàng theo khách hàng không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách đặt hàng theo khách hàng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopDatHangTheoNgay() throws Exception {
        BaoCaoDatHangReportService service = new BaoCaoDatHangReportService();

        JasperReport report = service.getCompiledReportThNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp đặt hàng theo ngày không được null");

        JasperPrint print = service.generateTongHopDatHangTheoNgay(new Date(), new Date(), null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp đặt hàng theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp đặt hàng theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopDatHangTheoKhachHang() throws Exception {
        BaoCaoDatHangReportService service = new BaoCaoDatHangReportService();

        JasperReport report = service.getCompiledReportThKhachHang();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp đặt hàng theo khách hàng không được null");

        JasperPrint print = service.generateTongHopDatHangTheoKhachHang(new Date(), new Date(), null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp đặt hàng theo khách hàng không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp đặt hàng theo khách hàng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangDatTheoKhachHang() throws Exception {
        BaoCaoDatHangReportService service = new BaoCaoDatHangReportService();

        JasperReport report = service.getCompiledReportThMhKhachHang();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng đặt theo khách hàng không được null");

        JasperPrint print = service.generateTongHopMatHangDatTheoKhachHang(new Date(), new Date(), null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng đặt theo khách hàng không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng đặt theo khách hàng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangDatTheoNgay() throws Exception {
        BaoCaoDatHangReportService service = new BaoCaoDatHangReportService();

        JasperReport report = service.getCompiledReportThMhNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng đặt theo ngày không được null");

        JasperPrint print = service.generateTongHopMatHangDatTheoNgay(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng đặt theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng đặt theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }
}
