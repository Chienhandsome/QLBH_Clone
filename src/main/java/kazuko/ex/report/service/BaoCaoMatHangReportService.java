package kazuko.ex.report.service;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.MatHangReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class BaoCaoMatHangReportService {

    private static JasperReport cachedReportNhom = null;
    private static JasperReport cachedReportHsx = null;
    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoMatHang-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportNhom == null) {
                try (InputStream in = BaoCaoMatHangReportService.class.getResourceAsStream("/reports/danh_sach_mat_hang_theo_nhom.jrxml")) {
                    if (in != null) cachedReportNhom = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportHsx == null) {
                try (InputStream in = BaoCaoMatHangReportService.class.getResourceAsStream("/reports/danh_sach_mat_hang_theo_hang_san_xuat.jrxml")) {
                    if (in != null) cachedReportHsx = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportNhom() throws JRException {
        if (cachedReportNhom != null) return cachedReportNhom;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_mat_hang_theo_nhom.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_mat_hang_theo_nhom.jrxml không tồn tại!");
            cachedReportNhom = JasperCompileManager.compileReport(in);
            return cachedReportNhom;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportHsx() throws JRException {
        if (cachedReportHsx != null) return cachedReportHsx;
        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_mat_hang_theo_hang_san_xuat.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/danh_sach_mat_hang_theo_hang_san_xuat.jrxml không tồn tại!");
            cachedReportHsx = JasperCompileManager.compileReport(in);
            return cachedReportHsx;
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

    public JasperPrint generateMatHangTheoNhom(String nhomMatHangId, String hangSanXuat) throws JRException {
        JasperReport jasperReport = getCompiledReportNhom();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<MatHang> allMh = matHangDAO.getAll();
        List<MatHangReportRow> rows = new ArrayList<>();

        for (MatHang mh : allMh) {
            if (mh.getStatus() < 0) continue;

            if (nhomMatHangId != null && !nhomMatHangId.trim().isEmpty()) {
                if (!nhomMatHangId.equals(mh.getNhomMatHangId())) {
                    continue;
                }
            }

            if (hangSanXuat != null && !hangSanXuat.trim().isEmpty()) {
                if (mh.getHangSanXuat() == null || !hangSanXuat.equalsIgnoreCase(mh.getHangSanXuat().trim())) {
                    continue;
                }
            }

            String groupHeader = "Nhóm mặt hàng: " + (mh.getTenNhom() != null && !mh.getTenNhom().trim().isEmpty() ? mh.getTenNhom() : "");

            MatHangReportRow row = new MatHangReportRow(
                    0,
                    groupHeader,
                    mh.getCode() != null ? mh.getCode() : "",
                    mh.getName() != null ? mh.getName() : "",
                    mh.getGiaBan(),
                    mh.getTonToiDa(),
                    mh.getTonToiThieu()
            );
            rows.add(row);
        }

        // Sort by groupHeader, then code
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaHang().compareToIgnoreCase(r2.getMaHang());
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
        parameters.put("p_reportTitle", "DANH SÁCH MẶT HÀNG THEO NHÓM");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    public JasperPrint generateMatHangTheoHangSanXuat(String nhomMatHangId, String hangSanXuat) throws JRException {
        JasperReport jasperReport = getCompiledReportHsx();
        SystemConfig sysConfig = configDAO.loadConfig();

        List<MatHang> allMh = matHangDAO.getAll();
        List<MatHangReportRow> rows = new ArrayList<>();

        for (MatHang mh : allMh) {
            if (mh.getStatus() < 0) continue;

            if (nhomMatHangId != null && !nhomMatHangId.trim().isEmpty()) {
                if (!nhomMatHangId.equals(mh.getNhomMatHangId())) {
                    continue;
                }
            }

            if (hangSanXuat != null && !hangSanXuat.trim().isEmpty()) {
                if (mh.getHangSanXuat() == null || !hangSanXuat.equalsIgnoreCase(mh.getHangSanXuat().trim())) {
                    continue;
                }
            }

            String groupHeader = "Hãng sản xuất: " + (mh.getHangSanXuat() != null && !mh.getHangSanXuat().trim().isEmpty() ? mh.getHangSanXuat() : "");

            MatHangReportRow row = new MatHangReportRow(
                    0,
                    groupHeader,
                    mh.getCode() != null ? mh.getCode() : "",
                    mh.getName() != null ? mh.getName() : "",
                    mh.getGiaBan(),
                    mh.getTonToiDa(),
                    mh.getTonToiThieu()
            );
            rows.add(row);
        }

        // Sort by groupHeader, then code
        rows.sort((r1, r2) -> {
            int cmp = r1.getGroupHeader().compareToIgnoreCase(r2.getGroupHeader());
            if (cmp != 0) return cmp;
            return r1.getMaHang().compareToIgnoreCase(r2.getMaHang());
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
        parameters.put("p_reportTitle", "DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}