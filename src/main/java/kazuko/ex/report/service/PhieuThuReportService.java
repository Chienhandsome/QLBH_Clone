package kazuko.ex.report.service;

import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.PhieuThuReportRow;
import kazuko.ex.util.IconHelper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class PhieuThuReportService {

    private static JasperReport cachedJasperReport = null;
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    public synchronized JasperReport getCompiledReport() throws JRException {
        if (cachedJasperReport != null) {
            return cachedJasperReport;
        }

        try (InputStream in = getClass().getResourceAsStream("/reports/danh_sach_phieu_thu_theo_ngay.jrxml")) {
            if (in == null) {
                throw new JRException("Không tìm thấy file mẫu báo cáo /reports/danh_sach_phieu_thu_theo_ngay.jrxml trong resources!");
            }
            cachedJasperReport = JasperCompileManager.compileReport(in);
            return cachedJasperReport;
        } catch (Exception e) {
            throw new JRException("Lỗi khi biên dịch mẫu báo cáo JRXML: " + e.getMessage(), e);
        }
    }

    public JasperPrint generateReportPrint(Date fromDate, Date toDate, String lyDoThuChiId, String cuaHangId) throws JRException {
        JasperReport jasperReport = getCompiledReport();

        // 1. Lấy thông tin công ty và Logo từ Cấu hình hệ thống -> Thông tin chung
        SystemConfig sysConfig = configDAO.loadConfig();
        String companyName = sysConfig.getTenCongTy();
        String companyAddress = sysConfig.getDiaChi();
        String companyPhone = sysConfig.getSoDienThoai();
        String companyEmail = sysConfig.getEmail();
        String logoPath = sysConfig.getLogoPath();

        Object logoImage = null;
        if (logoPath != null && !logoPath.trim().isEmpty()) {
            File f = new File(logoPath);
            if (f.exists() && f.isFile()) {
                try {
                    logoImage = ImageIO.read(f);
                } catch (Exception ignored) {}
            }
        }
        if (logoImage == null) {
            // Thử nạp logo từ resource
            try {
                InputStream logoStream = getClass().getResourceAsStream("/logo.png");
                if (logoStream != null) {
                    logoImage = ImageIO.read(logoStream);
                }
            } catch (Exception ignored) {}
        }

        // 2. Truy vấn dữ liệu phiếu thu theo ngày và điều kiện lọc
        List<PhieuThu> phieuThuList = phieuThuDAO.getReportPhieuThuTheoNgay(fromDate, toDate, lyDoThuChiId, cuaHangId);

        // 3. Map sang danh sách dòng dữ liệu PhieuThuReportRow
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        List<PhieuThuReportRow> rows = new ArrayList<>();
        int stt = 1;
        for (PhieuThu pt : phieuThuList) {
            String dateGroupStr = "Ngày: " + (pt.getNgay() != null ? sdfDate.format(pt.getNgay()) : "");
            String soPhieu = pt.getSoPhieu() != null ? pt.getSoPhieu() : "";
            String tenDoiTuong = pt.getTenDoiTuong() != null ? pt.getTenDoiTuong() : "";
            String diaChi = pt.getDiaChi() != null ? pt.getDiaChi() : "";
            String lyDoThuChi = (pt.getTenPhanLoai() != null && !pt.getTenPhanLoai().isEmpty()) ? pt.getTenPhanLoai() : (pt.getLyDoThu() != null ? pt.getLyDoThu() : "");
            String dienGiai = pt.getGhiChu() != null ? pt.getGhiChu() : "";
            String chungTuGoc = pt.getChungTuGoc() != null ? pt.getChungTuGoc() : "";
            Double soTien = pt.getSoTien();

            PhieuThuReportRow row = new PhieuThuReportRow(stt++, soPhieu, dateGroupStr, tenDoiTuong, diaChi, lyDoThuChi, dienGiai, chungTuGoc, soTien);
            rows.add(row);
        }

        // 4. Thiết lập tham số Report Parameters
        Map<String, Object> params = new HashMap<>();
        params.put("p_companyName", (companyName != null && !companyName.isEmpty()) ? companyName : "CÔNG TY TNHH NGỌC CHÂU ÂU");
        params.put("p_companyAddress", (companyAddress != null && !companyAddress.isEmpty()) ? companyAddress : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
        params.put("p_companyPhone", (companyPhone != null && !companyPhone.isEmpty()) ? companyPhone : "(08) 6264 5788");
        params.put("p_companyEmail", companyEmail != null ? companyEmail : "info@ngocchauau.vn");
        params.put("p_logoImage", logoImage);

        String fromDateStr = fromDate != null ? sdfDate.format(fromDate) : "";
        String toDateStr = toDate != null ? sdfDate.format(toDate) : "";
        params.put("p_dateRangeText", "Ngày từ " + fromDateStr + " đến " + toDateStr);
        params.put("p_reportTitle", "DANH SÁCH PHIẾU THU THEO NGÀY");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        return JasperFillManager.fillReport(jasperReport, params, dataSource);
    }
}
