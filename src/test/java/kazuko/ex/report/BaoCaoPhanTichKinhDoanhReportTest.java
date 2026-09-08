package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoPhanTichKinhDoanhReportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class BaoCaoPhanTichKinhDoanhReportTest {

    private final BaoCaoPhanTichKinhDoanhReportService service = new BaoCaoPhanTichKinhDoanhReportService();

    @Test
    public void testKetQuaKinhDoanhReport() throws Exception {
        JasperPrint print = service.generateKetQuaKinhDoanh(new Date(), new Date(), null);
        Assertions.assertNotNull(print, "JasperPrint Báo cáo kết quả kinh doanh không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Báo cáo Kết quả kinh doanh tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietHoatDongTrongNgayReport() throws Exception {
        JasperPrint print = service.generateChiTietHoatDongTrongNgay(new Date(), new Date(), null, null);
        Assertions.assertNotNull(print, "JasperPrint Chi tiết hoạt động trong ngày không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Báo cáo Chi tiết hoạt động trong ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTop20MatHangBanChayReport() throws Exception {
        JasperPrint print = service.generateTop20MatHangBanChay(new Date(), new Date(), null);
        Assertions.assertNotNull(print, "JasperPrint Top 20 mặt hàng bán chạy nhất không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Báo cáo Top 20 mặt hàng bán chạy nhất tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBangTongHopThiPhanReport() throws Exception {
        JasperPrint print = service.generateBangTongHopThiPhan(2026);
        Assertions.assertNotNull(print, "JasperPrint Bảng tổng hợp thị phần không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Báo cáo Bảng tổng hợp thị phần tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testPhanTichTinhHinhBanHangReport() throws Exception {
        JasperPrint print = service.generatePhanTichTinhHinhBanHang(new Date(), new Date());
        Assertions.assertNotNull(print, "JasperPrint Báo cáo phân tích tình hình bán hàng không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");
        System.out.println("Báo cáo Phân tích tình hình bán hàng tạo thành công! Số trang: " + print.getPages().size());
    }
}
