package kazuko.ex.report;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.report.service.BaoCaoCongNoReportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BaoCaoCongNoReportTest {

    private final BaoCaoCongNoReportService service = new BaoCaoCongNoReportService();

    @Test
    public void testDoiTraCapNhatCongNoTheoPhanChenhLech() {
        double hangTraGiaTriCao = 10_000_000d;
        double hangDoiGiaTriThap = 7_000_000d;
        double tangNoKhiDoiHangThap = BaoCaoCongNoReportService.calculateOrderIncrease(
                0, hangTraGiaTriCao, hangDoiGiaTriThap, 0, 0, 0);
        assertEquals(-3_000_000d, tangNoKhiDoiHangThap - hangTraGiaTriCao, 0.001,
                "Đổi sang hàng giá thấp hơn phải giảm công nợ theo phần chênh lệch");

        double hangTraGiaTriThap = 7_000_000d;
        double hangDoiGiaTriCao = 10_000_000d;
        double tangNoKhiDoiHangCao = BaoCaoCongNoReportService.calculateOrderIncrease(
                3_000_000d, hangTraGiaTriThap, hangDoiGiaTriCao, 0, 0, 0);
        assertEquals(3_000_000d, tangNoKhiDoiHangCao - hangTraGiaTriThap, 0.001,
                "Đổi sang hàng giá cao hơn phải tăng công nợ theo phần chênh lệch");

        double giaTriHangMoiSauGiamGia = BaoCaoCongNoReportService.calculateOrderIncrease(
                0, 561_440d, 571_120d, 57_112d, 0, 0);
        assertEquals(514_008d, giaTriHangMoiSauGiamGia, 0.001,
                "Giảm giá hóa đơn phải được áp dụng cho hàng đổi mới");
        assertEquals(-47_432d, giaTriHangMoiSauGiamGia - 561_440d, 0.001,
                "Đổi trả mẫu phải làm giảm đúng phần công nợ chênh lệch sau giảm giá");
    }

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

        List<KhachHang> customers = new KhachHangDAO().getAll();
        assertFalse(customers.isEmpty(), "Cần có ít nhất một khách hàng để kiểm tra đối chiếu công nợ");

        JasperPrint jp = service.generateBaoCaoCongNoKhachHang(
                tuNgay, denNgay, customers.get(0).getId());
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
