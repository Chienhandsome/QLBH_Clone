package kazuko.ex.report.service;

import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.NhaCungCapReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class BaoCaoNhaCungCapReportService {

    private static JasperReport cachedReportNhom = null;
    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoNhaCungCap-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportNhom == null) {
                try (InputStream in = BaoCaoNhaCungCapReportService.class.getResourceAsStream("/reports/danh_sach_nha_cung_cap_theo_nhom.jrxml")) {
                    if (in != null) cachedReportNhom = ReportTemplateCompiler.compile(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportNhom() throws JRException {
        if (cachedReportNhom != null) return cachedReportNhom;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_nha_cung_cap_theo_nhom.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_nha_cung_cap_theo_nhom.jrxml không tồn tại!");
            cachedReportNhom = ReportTemplateCompiler.compile(in);
            return cachedReportNhom;
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

    public JasperPrint generateNhaCungCapTheoNhom(String nhomNhaCungCapId) throws JRException {
        JasperReport jasperReport = getCompiledReportNhom();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<NhaCungCap> allNcc = nhaCungCapDAO.getAll();
        List<NhaCungCapReportRow> rows = new ArrayList<>();

        for (NhaCungCap ncc : allNcc) {
            if (ncc.getStatus() < 0) continue;

            if (nhomNhaCungCapId != null && !nhomNhaCungCapId.trim().isEmpty()) {
                if (!nhomNhaCungCapId.equals(ncc.getNhomNhaCungCapId())) {
                    continue;
                }
            }

            String groupHeader = "Nhóm nhà cung cấp: " + (ncc.getTenNhomNhaCungCap() != null && !ncc.getTenNhomNhaCungCap().trim().isEmpty() ? ncc.getTenNhomNhaCungCap() : "");
            
            NhaCungCapReportRow row = new NhaCungCapReportRow(
                    0,
                    groupHeader,
                    ncc.getMaNhaCungCap() != null ? ncc.getMaNhaCungCap() : "",
                    ncc.getName() != null ? ncc.getName() : "",
                    ncc.getDiaChi() != null ? ncc.getDiaChi() : "",
                    ncc.getDienThoai() != null ? ncc.getDienThoai() : "",
                    ncc.getEmail() != null ? ncc.getEmail() : ""
            );
            rows.add(row);
        }

        // Sort by groupHeader, then maNhaCungCap
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaNhaCungCap().compareToIgnoreCase(r2.getMaNhaCungCap());
        });

        // Set STT
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setStt(i + 1);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_companyName", sysConfig.getTenCongTy() != null && !sysConfig.getTenCongTy().isEmpty() ? sysConfig.getTenCongTy() : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        parameters.put("p_companyAddress", sysConfig.getDiaChi() != null ? sysConfig.getDiaChi() : "");
        parameters.put("p_companyPhone", sysConfig.getSoDienThoai() != null ? sysConfig.getSoDienThoai() : "");
        parameters.put("p_companyEmail", sysConfig.getEmail() != null ? sysConfig.getEmail() : "");
        parameters.put("p_logoImage", getCompanyLogo(sysConfig));
        parameters.put("p_reportTitle", "DANH SÁCH NHÀ CUNG CẤP THEO NHÓM");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}