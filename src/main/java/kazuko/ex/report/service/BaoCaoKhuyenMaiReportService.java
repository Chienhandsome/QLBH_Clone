package kazuko.ex.report.service;

import kazuko.ex.dao.DotKhuyenMaiDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.DotKhuyenMai;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.DotKhuyenMaiReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoKhuyenMaiReportService {

    private static JasperReport cachedReportKm = null;
    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final DotKhuyenMaiDAO kmDAO = new DotKhuyenMaiDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoKhuyenMai-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportKm == null) {
                try (InputStream in = BaoCaoKhuyenMaiReportService.class.getResourceAsStream("/reports/danh_sach_dot_khuyen_mai.jrxml")) {
                    if (in != null) cachedReportKm = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportKm() throws JRException {
        if (cachedReportKm != null) return cachedReportKm;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_dot_khuyen_mai.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_dot_khuyen_mai.jrxml không tồn tại!");
            cachedReportKm = JasperCompileManager.compileReport(in);
            return cachedReportKm;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    private Object getCompanyLogo(SystemConfig sysConfig) {
        String logoPath = sysConfig.getLogoPath();
        Object logoImage = cachedLogoImage;
        if (logoPath != null && !logoPath.trim().isEmpty()) {
            if (!logoPath.equals(cachedLogoPath)) {
                File f = new File(logoPath);
                if (f.exists() && f.isFile()) {
                    try {
                        logoImage = ImageIO.read(f);
                        cachedLogoImage = logoImage;
                        cachedLogoPath = logoPath;
                    } catch (Exception ignored) {}
                }
            }
        }
        if (logoImage == null) {
            if (cachedLogoImage != null) {
                logoImage = cachedLogoImage;
            } else {
                try {
                    InputStream logoStream = getClass().getResourceAsStream("/logo.png");
                    if (logoStream != null) {
                        logoImage = ImageIO.read(logoStream);
                        cachedLogoImage = logoImage;
                    }
                } catch (Exception ignored) {}
            }
        }
        return logoImage;
    }

    public JasperPrint generateDotKhuyenMai() throws JRException {
        JasperReport jasperReport = getCompiledReportKm();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<DotKhuyenMai> listKm = kmDAO.getAll();
        List<DotKhuyenMaiReportRow> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        int stt = 1;
        for (DotKhuyenMai km : listKm) {
            if (km.getStatus() < 0) continue;

            String tuNgayStr = km.getTuNgay() != null ? sdf.format(km.getTuNgay()) : "";
            String denNgayStr = km.getDenNgay() != null ? sdf.format(km.getDenNgay()) : "";
            String loaiHinhStr = km.getTenLoaiHinhKhuyenMai() != null ? km.getTenLoaiHinhKhuyenMai() : "";

            DotKhuyenMaiReportRow row = new DotKhuyenMaiReportRow(
                    stt++,
                    km.getName() != null ? km.getName() : "",
                    tuNgayStr,
                    denNgayStr,
                    loaiHinhStr
            );
            rows.add(row);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "DANH SÁCH ĐỢT KHUYẾN MẠI");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}