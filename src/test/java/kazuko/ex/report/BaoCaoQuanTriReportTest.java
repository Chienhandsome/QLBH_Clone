package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoQuanTriReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoQuanTriReportTest {

    @Test
    public void testTongHopLaiGopGiaVonReport() throws Exception {
        BaoCaoQuanTriReportService service = new BaoCaoQuanTriReportService();

        JasperReport report = service.getCompiledReportTongHopLaiGopGiaVon();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp lãi gộp theo mặt hàng (giá vốn) không được null");

        JasperPrint print = service.generateTongHopLaiGopGiaVon(new Date(), new Date(), null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp lãi gộp theo mặt hàng (giá vốn) không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp lãi gộp theo mặt hàng (giá vốn) tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopLaiGopGiaNhapReport() throws Exception {
        BaoCaoQuanTriReportService service = new BaoCaoQuanTriReportService();

        JasperReport report = service.getCompiledReportTongHopLaiGopGiaNhap();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp lãi gộp theo mặt hàng (giá nhập) không được null");

        JasperPrint print = service.generateTongHopLaiGopGiaNhap(new Date(), new Date(), null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp lãi gộp theo mặt hàng (giá nhập) không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp lãi gộp theo mặt hàng (giá nhập) tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietLaiHoaDonGiaVonReport() throws Exception {
        BaoCaoQuanTriReportService service = new BaoCaoQuanTriReportService();

        JasperReport report = service.getCompiledReportChiTietLaiHoaDonGiaVon();
        Assertions.assertNotNull(report, "JasperReport Chi tiết lãi theo hóa đơn (giá vốn) không được null");

        JasperPrint print = service.generateChiTietLaiHoaDonGiaVon(new Date(), new Date());
        Assertions.assertNotNull(print, "JasperPrint Chi tiết lãi theo hóa đơn (giá vốn) không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Chi tiết lãi theo hóa đơn (giá vốn) tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietLaiHoaDonGiaNhapReport() throws Exception {
        BaoCaoQuanTriReportService service = new BaoCaoQuanTriReportService();

        JasperReport report = service.getCompiledReportChiTietLaiHoaDonGiaNhap();
        Assertions.assertNotNull(report, "JasperReport Chi tiết lãi theo hóa đơn (giá nhập) không được null");

        JasperPrint print = service.generateChiTietLaiHoaDonGiaNhap(new Date(), new Date());
        Assertions.assertNotNull(print, "JasperPrint Chi tiết lãi theo hóa đơn (giá nhập) không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Chi tiết lãi theo hóa đơn (giá nhập) tạo thành công! Số trang: " + print.getPages().size());
    }
}
