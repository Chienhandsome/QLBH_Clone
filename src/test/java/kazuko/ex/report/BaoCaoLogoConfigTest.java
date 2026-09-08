package kazuko.ex.report;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.service.BaoCaoBanHangReportService;
import kazuko.ex.report.service.BaoCaoKhachHangReportService;
import kazuko.ex.report.service.PhieuThuReportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class BaoCaoLogoConfigTest {

    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private SystemConfig originalConfig;
    private File tempLogoFile;

    @BeforeEach
    public void setUp() throws Exception {
        originalConfig = configDAO.loadConfig();

        // Tạo một file ảnh tạm thời đại diện cho logo mới do người dùng chọn
        tempLogoFile = File.createTempFile("test_custom_logo_", ".png");
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, 100, 50);
        g2.dispose();
        ImageIO.write(img, "png", tempLogoFile);
    }

    @AfterEach
    public void tearDown() {
        if (originalConfig != null) {
            configDAO.saveConfig(originalConfig);
        }
        if (tempLogoFile != null && tempLogoFile.exists()) {
            tempLogoFile.delete();
        }
    }

    @Test
    public void testCustomLogoAppliesToReports() throws Exception {
        // 1. Cập nhật logoPath trong cấu hình hệ thống
        SystemConfig config = configDAO.loadConfig();
        config.setLogoPath(tempLogoFile.getAbsolutePath());
        configDAO.saveConfig(config);

        // 2. Kiểm tra BaoCaoBanHangReportService
        BaoCaoBanHangReportService banHangService = new BaoCaoBanHangReportService();
        JasperPrint printBanHang = banHangService.generateBanHangTheoNgay(null, null, null, null, null);
        Assertions.assertNotNull(printBanHang, "Báo cáo bán hàng theo ngày phải tạo thành công");
        Assertions.assertTrue(printBanHang.getPages().size() >= 1);

        // 3. Kiểm tra PhieuThuReportService
        PhieuThuReportService phieuThuService = new PhieuThuReportService();
        JasperPrint printPhieuThu = phieuThuService.generateReportPrint(null, null, null, null);
        Assertions.assertNotNull(printPhieuThu, "Báo cáo phiếu thu phải tạo thành công");
        Assertions.assertTrue(printPhieuThu.getPages().size() >= 1);

        // 4. Kiểm tra BaoCaoKhachHangReportService
        BaoCaoKhachHangReportService khachHangService = new BaoCaoKhachHangReportService();
        JasperPrint printKhachHang = khachHangService.generateKhachHangTheoNhom(null, null);
        Assertions.assertNotNull(printKhachHang, "Báo cáo khách hàng theo nhóm phải tạo thành công");
        Assertions.assertTrue(printKhachHang.getPages().size() >= 1);
    }

    @Test
    public void testLogoPersistenceAcrossRestarts() throws Exception {
        // 1. Cập nhật tên công ty và logoPath
        SystemConfig config = configDAO.loadConfig();
        config.setTenCongTy("Công ty Kiểm Tra Persistence");
        config.setLogoPath(tempLogoFile.getAbsolutePath());
        boolean saved = configDAO.saveConfig(config);
        Assertions.assertTrue(saved, "Lưu cấu hình phải thành công");

        // 2. Giả lập khởi động lại ứng dụng: Xóa toàn bộ bộ nhớ cache static
        SystemConfigDAO.clearCache();

        // 3. Khởi tạo một DAO mới và tải lại cấu hình từ file trên đĩa
        SystemConfigDAO freshDAO = new SystemConfigDAO();
        SystemConfig reloadedConfig = freshDAO.loadConfig();

        // 4. Xác minh thông tin cấu hình và logo vẫn tồn tại chính xác
        Assertions.assertNotNull(reloadedConfig, "Cấu hình tải lại không được null");
        Assertions.assertEquals("Công ty Kiểm Tra Persistence", reloadedConfig.getTenCongTy());
        Assertions.assertNotNull(reloadedConfig.getLogoPath(), "Đường dẫn logo sau khi khởi động lại không được null");
        File persistedLogo = new File(reloadedConfig.getLogoPath());
        Assertions.assertTrue(persistedLogo.exists(), "File logo được lưu phải tồn tại trên đĩa: " + reloadedConfig.getLogoPath());
    }
}
