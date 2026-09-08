package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoNhapKhoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoNhapKhoReportTest {

    @Test
    public void testDanhSachPhieuNhapTheoNgay() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportDsNgay();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu nhập theo ngày không được null");

        JasperPrint print = service.generateDanhSachTheoNgayReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu nhập theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu nhập theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuNhapTheoNhaCungCap() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportDsNcc();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu nhập theo nhà cung cấp không được null");

        JasperPrint print = service.generateDanhSachTheoNhaCungCapReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu nhập theo nhà cung cấp không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu nhập theo nhà cung cấp tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuNhapTheoNhanVien() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportDsNhanVien();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu nhập theo nhân viên không được null");

        JasperPrint print = service.generateDanhSachTheoNhanVienReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu nhập theo nhân viên không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu nhập theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangNhapTheoNhaCungCap() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportThMhNcc();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng phiếu nhập theo NCC không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNhaCungCapReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng phiếu nhập theo NCC không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng phiếu nhập theo NCC tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangNhapTheoNhanVien() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportThMhNhanVien();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng phiếu nhập theo NV không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNhanVienReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng phiếu nhập theo NV không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng phiếu nhập theo NV tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopNhapTheoNgay() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportThNhapNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp nhập theo ngày không được null");

        JasperPrint print = service.generateTongHopNhapTheoNgayReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp nhập theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp nhập theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopNhapTheoNhaCungCap() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportThNhapNcc();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp nhập theo nhà cung cấp không được null");

        JasperPrint print = service.generateTongHopNhapTheoNhaCungCapReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp nhập theo nhà cung cấp không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp nhập theo nhà cung cấp tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopNhapTheoNhanVien() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportThNhapNhanVien();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp nhập theo nhân viên không được null");

        JasperPrint print = service.generateTongHopNhapTheoNhanVienReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp nhập theo nhân viên không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp nhập theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangNhapTheoNgay() throws Exception {
        BaoCaoNhapKhoReportService service = new BaoCaoNhapKhoReportService();

        JasperReport report = service.getCompiledReportThMhNhapNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng nhập kho theo ngày không được null");

        JasperPrint print = service.generateTongHopMatHangNhapTheoNgayReport(new Date(), new Date(), null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng nhập kho theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng nhập kho theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }
}
