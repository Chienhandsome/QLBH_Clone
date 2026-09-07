package kazuko.ex.report;

import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.report.service.PhieuChiReportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BaoCaoPhieuChiReportTest {

    @Test
    public void testCompileAndGeneratePhieuChiReport() throws Exception {
        PhieuChiReportService service = new PhieuChiReportService();

        // 1. Kiểm tra compile report theo ngày
        JasperReport report = service.getCompiledReport();
        Assertions.assertNotNull(report, "JasperReport PhieuChi không được null sau khi compile");

        // 2. Kiểm tra truy vấn danh sách phiếu chi theo ngày
        PhieuChiDAO dao = new PhieuChiDAO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = sdf.parse("01/08/2026");
        Date toDate = sdf.parse("30/09/2026");

        List<PhieuChi> list = dao.getReportPhieuChiTheoNgay(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(list, "Danh sách phiếu chi không được null");
        Assertions.assertFalse(list.isEmpty(), "Cần có ít nhất 1 phiếu chi mẫu");

        // 3. Kiểm tra sinh JasperPrint
        JasperPrint print = service.generateReportPrint(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(print, "JasperPrint PhieuChi không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang báo cáo phải >= 1");

        System.out.println("Báo cáo Phiếu chi theo ngày tạo thành công! Số trang: " + print.getPages().size()
                + ", Tên báo cáo: " + print.getName() + ", Số dòng: " + list.size());
    }

    @Test
    public void testCompileAndGeneratePhieuChiTheoLyDoReport() throws Exception {
        PhieuChiReportService service = new PhieuChiReportService();

        // 1. Kiểm tra compile report theo lý do
        JasperReport report = service.getCompiledReportByReason();
        Assertions.assertNotNull(report, "JasperReport PhieuChi By Reason không được null");

        // 2. Kiểm tra sinh JasperPrint theo lý do
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = sdf.parse("01/08/2026");
        Date toDate = sdf.parse("30/09/2026");

        JasperPrint print = service.generateReportByReasonPrint(fromDate, toDate, "ALL", "ALL");
        Assertions.assertNotNull(print, "JasperPrint PhieuChi By Reason không được null");
        Assertions.assertTrue(print.getPages().size() >= 1, "Số trang báo cáo theo lý do phải >= 1");

        System.out.println("Báo cáo Phiếu chi theo lý do tạo thành công! Số trang: " + print.getPages().size()
                + ", Tên báo cáo: " + print.getName());
    }
}