package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoKhachHangReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaoCaoKhachHangReportTest {

    @Test
    public void testKhachHangTheoNhomReport() throws Exception {
        BaoCaoKhachHangReportService service = new BaoCaoKhachHangReportService();

        JasperReport report = service.getCompiledReportNhom();
        Assertions.assertNotNull(report, "JasperReport Nhom không được null");

        JasperPrint print = service.generateKhachHangTheoNhom(null, null);
        Assertions.assertNotNull(print, "JasperPrint Nhom không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Khách hàng theo nhóm tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testKhachHangTheoNhanVienReport() throws Exception {
        BaoCaoKhachHangReportService service = new BaoCaoKhachHangReportService();

        JasperReport report = service.getCompiledReportNhanVien();
        Assertions.assertNotNull(report, "JasperReport NhanVien không được null");

        JasperPrint print = service.generateKhachHangTheoNhanVien(null, null);
        Assertions.assertNotNull(print, "JasperPrint NhanVien không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Khách hàng theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testKhachHangSinhNhatReport() throws Exception {
        BaoCaoKhachHangReportService service = new BaoCaoKhachHangReportService();

        JasperReport report = service.getCompiledReportSinhNhat();
        Assertions.assertNotNull(report, "JasperReport SinhNhat không được null");

        JasperPrint print = service.generateKhachHangSinhNhat(null);
        Assertions.assertNotNull(print, "JasperPrint SinhNhat không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Khách hàng sinh nhật tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testKhachHangThanThietReport() throws Exception {
        BaoCaoKhachHangReportService service = new BaoCaoKhachHangReportService();

        JasperReport report = service.getCompiledReportThanThiet();
        Assertions.assertNotNull(report, "JasperReport ThanThiet không được null");

        JasperPrint print = service.generateKhachHangThanThiet(null, null, null);
        Assertions.assertNotNull(print, "JasperPrint ThanThiet không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Khách hàng thân thiết tạo thành công! Số trang: " + print.getPages().size());
    }
}