package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoBieuDoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoBieuDoReportTest {

    private final BaoCaoBieuDoReportService service = new BaoCaoBieuDoReportService();

    @Test
    public void testBieuDoDoanhSoTheoNhomReport() throws Exception {
        JasperPrint print = service.generateBieuDoDoanhSoTheoNhom(new Date(), new Date());
        Assertions.assertNotNull(print, "JasperPrint Biểu đồ doanh số theo nhóm không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Biểu đồ Doanh số theo nhóm tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBieuDoDoanhThuNgayTrongThangReport() throws Exception {
        JasperPrint print = service.generateBieuDoDoanhThuNgayTrongThang(2026);
        Assertions.assertNotNull(print, "JasperPrint Biểu đồ doanh thu ngày trong tháng không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Biểu đồ Doanh thu ngày trong tháng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBieuDoDoanhThuThangTrongNamReport() throws Exception {
        JasperPrint print = service.generateBieuDoDoanhThuThangTrongNam(2026);
        Assertions.assertNotNull(print, "JasperPrint Biểu đồ doanh thu tháng trong năm không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Biểu đồ Doanh thu tháng trong năm tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBieuDoDoanhSoTheoNvkdReport() throws Exception {
        JasperPrint print = service.generateBieuDoDoanhSoTheoNvkd(new Date(), new Date());
        Assertions.assertNotNull(print, "JasperPrint Biểu đồ doanh số theo nhân viên kinh doanh không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Biểu đồ Doanh số theo nhân viên kinh doanh tạo thành công! Số trang: " + print.getPages().size());
    }
}
