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

    @Test
    public void testTongHopBanTheoNhanVienBanReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopBanTheoNhanVienBan();
        Assertions.assertNotNull(report, "JasperReport TongHopBanTheoNhanVienBan không được null");

        JasperPrint print = service.generateTongHopBanTheoNhanVienBan(null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopBanTheoNhanVienBan không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp bán theo nhân viên bán tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopBanTheoThuNganReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopBanTheoThuNgan();
        Assertions.assertNotNull(report, "JasperReport TongHopBanTheoThuNgan không được null");

        JasperPrint print = service.generateTongHopBanTheoThuNgan(null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopBanTheoThuNgan không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp bán theo thu ngân tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietBanHangTheoThuNganReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportChiTietTheoThuNgan();
        Assertions.assertNotNull(report, "JasperReport ChiTietBanHangTheoThuNgan không được null");

        JasperPrint print = service.generateChiTietBanHangTheoThuNgan(null, null, null);
        Assertions.assertNotNull(print, "JasperPrint ChiTietBanHangTheoThuNgan không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Chi tiết bán hàng theo thu ngân tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testBanHangTheoThuNganReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportBanHangTheoThuNgan();
        Assertions.assertNotNull(report, "JasperReport BanHangTheoThuNgan không được null");

        JasperPrint print = service.generateBanHangTheoThuNgan(null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint BanHangTheoThuNgan không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Bán hàng theo thu ngân tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopMatHangBanTheoThuNganReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopMatHangTheoThuNgan();
        Assertions.assertNotNull(report, "JasperReport TongHopMatHangTheoThuNgan không được null");

        JasperPrint print = service.generateTongHopMatHangBanTheoThuNgan(null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopMatHangTheoThuNgan không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp mặt hàng bán theo thu ngân tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopTienVanChuyenTheoNhanVienReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTienVanChuyenTheoNhanVien();
        Assertions.assertNotNull(report, "JasperReport TienVanChuyenTheoNhanVien không được null");

        JasperPrint print = service.generateTongHopTienVanChuyenTheoNhanVien(null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TienVanChuyenTheoNhanVien không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp tiền vận chuyển theo nhân viên tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietHangBanTheoDoiTacReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportChiTietHangBanTheoDoiTac();
        Assertions.assertNotNull(report, "JasperReport ChiTietHangBanTheoDoiTac không được null");

        JasperPrint print = service.generateChiTietHangBanTheoDoiTac(null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint ChiTietHangBanTheoDoiTac không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Chi tiết hàng bán theo đối tác tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testTongHopHoaHongTheoNVKDReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportTongHopHoaHongTheoNVKD();
        Assertions.assertNotNull(report, "JasperReport TongHopHoaHongTheoNVKD không được null");

        JasperPrint print = service.generateTongHopHoaHongTheoNVKD(null, null, null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint TongHopHoaHongTheoNVKD không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Tổng hợp hoa hồng theo NVKD tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietBanHangTheoHoaDonReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportChiTietBanHangTheoHoaDon();
        Assertions.assertNotNull(report, "JasperReport ChiTietBanHangTheoHoaDon không được null");

        JasperPrint print = service.generateChiTietBanHangTheoHoaDon(null, null, null);
        Assertions.assertNotNull(print, "JasperPrint ChiTietBanHangTheoHoaDon không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Chi tiết bán hàng theo hóa đơn tạo thành công! Số trang: " + print.getPages().size());
    }

    @Test
    public void testChiTietHangKhuyenMaiReport() throws Exception {
        BaoCaoBanHangReportService service = new BaoCaoBanHangReportService();

        JasperReport report = service.getCompiledReportChiTietHangKhuyenMai();
        Assertions.assertNotNull(report, "JasperReport ChiTietHangKhuyenMai không được null");

        JasperPrint print = service.generateChiTietHangKhuyenMai(null, null, null, null);
        Assertions.assertNotNull(print, "JasperPrint ChiTietHangKhuyenMai không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang phải >= 1");

        System.out.println("Báo cáo Chi tiết hàng khuyến mại tạo thành công! Số trang: " + print.getPages().size());
    }
}