package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoKiemKeReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoKiemKeReportTest {

    @Test
    public void testDanhSachPhieuKiemKeTheoNgayReport() throws Exception {
        BaoCaoKiemKeReportService service = new BaoCaoKiemKeReportService();

        JasperReport report = service.getCompiledReportDsNgay();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu kiểm kê theo ngày không được null");

        JasperPrint print = service.generateDanhSachTheoNgayReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu kiểm kê theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu kiểm kê theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangKiemKeTheoNgayReport() throws Exception {
        BaoCaoKiemKeReportService service = new BaoCaoKiemKeReportService();

        JasperReport report = service.getCompiledReportThMhNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng phiếu kiểm kê theo ngày không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNgayReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng phiếu kiểm kê theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng phiếu kiểm kê theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangKiemKeTheoNhanVienReport() throws Exception {
        BaoCaoKiemKeReportService service = new BaoCaoKiemKeReportService();

        JasperReport report = service.getCompiledReportThMhNhanVien();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng phiếu kiểm kê theo nhân viên không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNhanVienReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng phiếu kiểm kê theo nhân viên không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng phiếu kiểm kê theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuKiemKeTheoNhanVienReport() throws Exception {
        BaoCaoKiemKeReportService service = new BaoCaoKiemKeReportService();

        JasperReport report = service.getCompiledReportDsNhanVien();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu kiểm kê theo nhân viên không được null");

        JasperPrint print = service.generateDanhSachTheoNhanVienReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu kiểm kê theo nhân viên không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu kiểm kê theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }
}
