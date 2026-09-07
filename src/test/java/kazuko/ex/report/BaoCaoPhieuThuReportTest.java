package kazuko.ex.report;

import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.report.service.PhieuThuReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BaoCaoPhieuThuReportTest {

    @Test
    public void testCompileAndGeneratePhieuThuReport() throws Exception {
        PhieuThuReportService service = new PhieuThuReportService();

        // 1. Kiểm tra compile report
        JasperReport report = service.getCompiledReport();
        Assertions.assertNotNull(report, "JasperReport không được null sau khi compile");

        // 2. Kiểm tra truy vấn danh sách phiếu thu theo ngày
        PhieuThuDAO dao = new PhieuThuDAO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = sdf.parse("01/08/2026");
        Date toDate = sdf.parse("30/09/2026");

        List<PhieuThu> list = dao.getReportPhieuThuTheoNgay(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(list, "Danh sách phiếu thu không được null");
        Assertions.assertFalse(list.isEmpty(), "Cần có ít nhất 1 phiếu thu mẫu");

        // 3. Kiểm tra sinh JasperPrint
        JasperPrint print = service.generateReportPrint(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(print, "JasperPrint không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang báo cáo phải >= 1");

        System.out.println("Báo cáo Phiếu thu tạo thành công! Số trang: " + print.getPages().size()
                + ", Tên báo cáo: " + print.getName() + ", Số dòng dữ liệu: " + list.size());
    }

    @Test
    public void testCompileAndGeneratePhieuThuTheoLyDoReport() throws Exception {
        PhieuThuReportService service = new PhieuThuReportService();

        // 1. Kiểm tra compile report theo lý do
        JasperReport report = service.getCompiledReportByReason();
        Assertions.assertNotNull(report, "JasperReport By Reason không được null sau khi compile");

        // 2. Kiểm tra sinh JasperPrint theo lý do
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = sdf.parse("01/08/2026");
        Date toDate = sdf.parse("30/09/2026");

        JasperPrint print = service.generateReportByReasonPrint(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(print, "JasperPrint By Reason không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang báo cáo theo lý do phải >= 1");

        System.out.println("Báo cáo Phiếu thu theo lý do tạo thành công! Số trang: " + print.getPages().size()
                + ", Tên báo cáo: " + print.getName());
    }
}
