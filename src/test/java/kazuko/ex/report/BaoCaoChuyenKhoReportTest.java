package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoChuyenKhoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoChuyenKhoReportTest {

    @Test
    public void testTongHopMatHangChuyenKhoTheoNhanVienNhan() throws Exception {
        BaoCaoChuyenKhoReportService service = new BaoCaoChuyenKhoReportService();

        JasperReport report = service.getCompiledReportThMhNvNhan();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng chuyển kho theo NV nhận không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNhanVienNhanReport(new Date(), new Date(), null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng chuyển kho theo NV nhận không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng chuyển kho theo NV nhận tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangChuyenKhoTheoNhanVienChuyen() throws Exception {
        BaoCaoChuyenKhoReportService service = new BaoCaoChuyenKhoReportService();

        JasperReport report = service.getCompiledReportThMhNvChuyen();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng chuyển kho theo NV chuyển không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNhanVienChuyenReport(new Date(), new Date(), null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng chuyển kho theo NV chuyển không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng chuyển kho theo NV chuyển tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangChuyenKhoTheoNgay() throws Exception {
        BaoCaoChuyenKhoReportService service = new BaoCaoChuyenKhoReportService();

        JasperReport report = service.getCompiledReportThMhNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng chuyển kho theo ngày không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNgayReport(new Date(), new Date(), null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng chuyển kho theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng chuyển kho theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuChuyenKhoTheoNhanVienXuat() throws Exception {
        BaoCaoChuyenKhoReportService service = new BaoCaoChuyenKhoReportService();

        JasperReport report = service.getCompiledReportDsPhieuNvXuat();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu chuyển kho theo NV xuất không được null");

        JasperPrint print = service.generateDanhSachPhieuTheoNhanVienXuatReport(new Date(), new Date(), null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu chuyển kho theo NV xuất không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu chuyển kho theo NV xuất tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuChuyenKhoTheoNhanVienNhan() throws Exception {
        BaoCaoChuyenKhoReportService service = new BaoCaoChuyenKhoReportService();

        JasperReport report = service.getCompiledReportDsPhieuNvNhan();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu chuyển kho theo NV nhận không được null");

        JasperPrint print = service.generateDanhSachPhieuTheoNhanVienNhanReport(new Date(), new Date(), null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu chuyển kho theo NV nhận không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu chuyển kho theo NV nhận tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuChuyenKhoTheoNgay() throws Exception {
        BaoCaoChuyenKhoReportService service = new BaoCaoChuyenKhoReportService();

        JasperReport report = service.getCompiledReportDsPhieuNgay();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu chuyển kho theo ngày không được null");

        JasperPrint print = service.generateDanhSachPhieuTheoNgayReport(new Date(), new Date(), null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu chuyển kho theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu chuyển kho theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }
}
