package kazuko.ex.report;

import kazuko.ex.report.service.BaoCaoBanHangReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

public class BaoCaoBanHangReportTest {

    @Test
    public void testBanHangTheoGioReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTheoGio();
        Assertions.assertNotNull(report, "JasperReport BanHangTheoGio không được null");

        JasperPrint print = service.generateBanHangTheoGio(null, null);
        Assertions.assertNotNull(print, "JasperPrint BanHangTheoGio không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Bán hàng theo giờ tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBanHangTheoKichThuocReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTheoKichThuoc();
        Assertions.assertNotNull(report, "JasperReport BanHangTheoKichThuoc không được null");

        JasperPrint print = service.generateBanHangTheoKichThuoc(null, null);
        Assertions.assertNotNull(print, "JasperPrint BanHangTheoKichThuoc không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Bán hàng theo kích thước tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopGiaTriBanTheoThangReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopGiaTri();
        Assertions.assertNotNull(report, "JasperReport TongHopGiaTri không được null");

        Calendar cal = Calendar.getInstance();
        int thang = cal.get(Calendar.MONTH) + 1;
        int nam = cal.get(Calendar.YEAR);

        JasperPrint print = service.generateTongHopGiaTriBanTheoThang(thang, nam);
        Assertions.assertNotNull(print, "JasperPrint TongHopGiaTri không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp giá trị bán theo tháng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopSoLuongBanTheoThangReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopSoLuong();
        Assertions.assertNotNull(report, "JasperReport TongHopSoLuong không được null");

        Calendar cal = Calendar.getInstance();
        int thang = cal.get(Calendar.MONTH) + 1;
        int nam = cal.get(Calendar.YEAR);

        JasperPrint print = service.generateTongHopSoLuongBanTheoThang(thang, nam);
        Assertions.assertNotNull(print, "JasperPrint TongHopSoLuong không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp số lượng bán theo tháng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBanHangTheoNgayReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTheoNgay();
        Assertions.assertNotNull(report, "JasperReport BanHangTheoNgay không được null");

        JasperPrint print = service.generateBanHangTheoNgay(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint BanHangTheoNgay không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Bán hàng theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietBanHangTheoNgayReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportChiTietTheoNgay();
        Assertions.assertNotNull(report, "JasperReport ChiTietBanHangTheoNgay không được null");

        JasperPrint print = service.generateChiTietBanHangTheoNgay(null, null);
        Assertions.assertNotNull(print, "JasperPrint ChiTietBanHangTheoNgay không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Chi tiết bán hàng theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBanHangTheoKhachHangReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTheoKhachHang();
        Assertions.assertNotNull(report, "JasperReport BanHangTheoKhachHang không được null");

        JasperPrint print = service.generateBanHangTheoKhachHang(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint BanHangTheoKhachHang không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Bán hàng theo khách hàng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBanHangTheoNhanVienReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTheoNhanVien();
        Assertions.assertNotNull(report, "JasperReport BanHangTheoNhanVien không được null");

        JasperPrint print = service.generateBanHangTheoNhanVien(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint BanHangTheoNhanVien không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Bán hàng theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangBanTheoKhachHangReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopMatHangTheoKhachHang();
        Assertions.assertNotNull(report, "JasperReport TongHopMatHangTheoKhachHang không được null");

        JasperPrint print = service.generateTongHopMatHangBanTheoKhachHang(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopMatHangTheoKhachHang không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng bán theo khách hàng tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangBanTheoNgayReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopMatHangTheoNgay();
        Assertions.assertNotNull(report, "JasperReport TongHopMatHangTheoNgay không được null");

        JasperPrint print = service.generateTongHopMatHangBanTheoNgay(null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopMatHangTheoNgay không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng bán theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangBanTheoNhanVienReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopMatHangTheoNhanVien();
        Assertions.assertNotNull(report, "JasperReport TongHopMatHangTheoNhanVien không được null");

        JasperPrint print = service.generateTongHopMatHangBanTheoNhanVien(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopMatHangTheoNhanVien không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng bán theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopBanHangTheoNgayReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopBanHangTheoNgay();
        Assertions.assertNotNull(report, "JasperReport TongHopBanHangTheoNgay không được null");

        JasperPrint print = service.generateTongHopBanHangTheoNgay(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopBanHangTheoNgay không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp bán hàng theo ngày tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopBanTheoKhachHangReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopBanTheoKhachHang();
        Assertions.assertNotNull(report, "JasperReport TongHopBanTheoKhachHang không được null");

        JasperPrint print = service.generateTongHopBanTheoKhachHang(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopBanTheoKhachHang không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp bán theo khách hàng tạo thành công! Số trang: " + print.getPages().size());
    }
}