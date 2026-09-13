package kazuko.ex.report.service;

import net.sf.jasperreports.engine.JasperReport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Xóa report đã compile để thiết lập mẫu vừa lưu có hiệu lực ngay. */
public final class ReportCacheInvalidator {
    private static final String[] SERVICE_CLASSES = {
            "BaoCaoBanHangReportService", "BaoCaoBieuDoReportService",
            "BaoCaoChuyenKhoReportService", "BaoCaoCongNoReportService",
            "BaoCaoDatHangReportService", "BaoCaoKhachHangReportService",
            "BaoCaoKhuyenMaiReportService", "BaoCaoKiemKeReportService",
            "BaoCaoMatHangReportService", "BaoCaoNhaCungCapReportService",
            "BaoCaoNhapKhoReportService", "BaoCaoPhanTichKinhDoanhReportService",
            "BaoCaoQuanTriReportService", "BaoCaoQuyReportService",
            "BaoCaoTonKhoReportService", "BaoCaoXuatKhoReportService",
            "PhieuChiReportService", "PhieuThuReportService"
    };

    private ReportCacheInvalidator() {
    }

    public static void clearAll() {
        for (String simpleName : SERVICE_CLASSES) {
            try {
                Class<?> serviceClass = Class.forName(
                        "kazuko.ex.report.service." + simpleName, false,
                        ReportCacheInvalidator.class.getClassLoader());
                for (Field field : serviceClass.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())
                            && JasperReport.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        field.set(null, null);
                    }
                }
            } catch (ReflectiveOperationException e) {
                System.err.println("Không xóa được cache " + simpleName + ": " + e.getMessage());
            }
        }
    }
}
