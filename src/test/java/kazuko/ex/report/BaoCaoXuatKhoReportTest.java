package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoXuatKhoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoXuatKhoReportTest {

    @Test
    public void testTongHopMatHangXuatTheoNgay() throws Exception {
        BaoCaoXuatKhoReportService service = new BaoCaoXuatKhoReportService();

        JasperReport report = service.getCompiledReportThMhNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng xuất theo ngày không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNgayReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng xuất theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng xuất theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangXuatTheoNhanVien() throws Exception {
        BaoCaoXuatKhoReportService service = new BaoCaoXuatKhoReportService();

        JasperReport report = service.getCompiledReportThMhNhanVien();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp mặt hàng xuất theo nhân viên không được null");

        JasperPrint print = service.generateTongHopMatHangTheoNhanVienReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp mặt hàng xuất theo nhân viên không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng xuất theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuXuatTheoNhanVien() throws Exception {
        BaoCaoXuatKhoReportService service = new BaoCaoXuatKhoReportService();

        JasperReport report = service.getCompiledReportDsNhanVien();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu xuất theo nhân viên không được null");

        JasperPrint print = service.generateDanhSachTheoNhanVienReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu xuất theo nhân viên không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Báo cáo Danh sách phiếu xuất theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopXuatKhacTheoNgay() throws Exception {
        BaoCaoXuatKhoReportService service = new BaoCaoXuatKhoReportService();

        JasperReport report = service.getCompiledReportThXkNgay();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp xuất khác theo ngày không được null");

        JasperPrint print = service.generateTongHopXuatKhacTheoNgayReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp xuất khác theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp xuất khác theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopXuatKhacTheoNhanVien() throws Exception {
        BaoCaoXuatKhoReportService service = new BaoCaoXuatKhoReportService();

        JasperReport report = service.getCompiledReportThXkNhanVien();
        Assertions.assertNotNull(report, "JasperReport Tổng hợp xuất khác theo nhân viên không được null");

        JasperPrint print = service.generateTongHopXuatKhacTheoNhanVienReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Tổng hợp xuất khác theo nhân viên không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp xuất khác theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testDanhSachPhieuXuatTheoNgay() throws Exception {
        BaoCaoXuatKhoReportService service = new BaoCaoXuatKhoReportService();

        JasperReport report = service.getCompiledReportDsNgay();
        Assertions.assertNotNull(report, "JasperReport Danh sách phiếu xuất theo ngày không được null");

        JasperPrint print = service.generateDanhSachTheoNgayReport(new Date(), new Date(), null, null, null);
        Assertions.assertNotNull(print, "JasperPrint Danh sách phiếu xuất theo ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Danh sách phiếu xuất theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }
}

