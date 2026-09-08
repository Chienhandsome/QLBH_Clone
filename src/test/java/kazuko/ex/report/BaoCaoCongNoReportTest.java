package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoCongNoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class BaoCaoCongNoReportTest {

    private final BaoCaoCongNoReportService service = new BaoCaoCongNoReportService();

    @Test
    public void testBaoCaoCongNoNhaCungCap() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0);
        Date tuNgay = cal.getTime();
        cal.set(2026, Calendar.SEPTEMBER, 8, 23, 59, 59);
        Date denNgay = cal.getTime();

        JasperPrint jp = service.generateBaoCaoCongNoNhaCungCap(tuNgay, denNgay, null);
        assertNotNull(jp, "JasperPrint Báo cáo công nợ nhà cung cấp không được null");
        assertTrue(jp.getPages().size() >= 1, "Báo cáo phải có ít nhất 1 trang");
        System.out.println("Báo cáo công nợ NCC tạo thành công! Số trang: " + jp.getPages().size());
    }

    @Test
    public void testTongHopCongNoNhaCungCap() throws Exception {
        JasperPrint jp = service.generateTongHopCongNoNhaCungCap(null);
        assertNotNull(jp, "JasperPrint Tổng hợp công nợ nhà cung cấp không được null");
        assertTrue(jp.getPages().size() >= 1, "Báo cáo phải có ít nhất 1 trang");
        System.out.println("Tổng hợp công nợ NCC tạo thành công! Số trang: " + jp.getPages().size());
    }

    @Test
    public void testBaoCaoCongNoKhachHang() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0);
        Date tuNgay = cal.getTime();
        cal.set(2026, Calendar.SEPTEMBER, 8, 23, 59, 59);
        Date denNgay = cal.getTime();

        JasperPrint jp = service.generateBaoCaoCongNoKhachHang(tuNgay, denNgay, null);
        assertNotNull(jp, "JasperPrint Báo cáo công nợ khách hàng không được null");
        assertTrue(jp.getPages().size() >= 1, "Báo cáo phải có ít nhất 1 trang");
        System.out.println("Báo cáo công nợ Khách hàng tạo thành công! Số trang: " + jp.getPages().size());
    }

    @Test
    public void testTongHopCongNoKhachHang() throws Exception {
        JasperPrint jp = service.generateTongHopCongNoKhachHang(null);
        assertNotNull(jp, "JasperPrint Tổng hợp công nợ khách hàng không được null");
        assertTrue(jp.getPages().size() >= 1, "Báo cáo phải có ít nhất 1 trang");
        System.out.println("Tổng hợp công nợ Khách hàng tạo thành công! Số trang: " + jp.getPages().size());
    }
}
