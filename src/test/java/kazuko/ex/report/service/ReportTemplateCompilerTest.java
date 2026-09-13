package kazuko.ex.report.service;

import kazuko.ex.model.system.ReportColumnSetting;
import kazuko.ex.report.model.PhieuThuReportRow;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportTemplateCompilerTest {
    @Test
    void appliesVisibilityAndAlignmentToReportFields() throws Exception {
        try (InputStream input = getClass().getResourceAsStream(
                "/reports/danh_sach_phieu_thu_theo_ngay.jrxml")) {
            assertNotNull(input);
            JasperDesign design = JRXmlLoader.load(input);

            ReportTemplateCompiler.applySettings(design, List.of(
                    setting("dienGiai", false, "LEFT"),
                    setting("soTien", true, "RIGHT")));

            JRDesignElement hidden = findDetailField(design, "dienGiai");
            assertNotNull(hidden.getPrintWhenExpression());
            assertEquals("Boolean.FALSE", hidden.getPrintWhenExpression().getText());

            JRDesignTextElement amount = (JRDesignTextElement) findDetailField(design, "soTien");
            assertEquals(HorizontalTextAlignEnum.RIGHT, amount.getHorizontalTextAlign());
        }
    }

    @Test
    void buildsTwoNestedGroupsAndTheirTotals() throws Exception {
        try (InputStream input = getClass().getResourceAsStream(
                "/reports/danh_sach_phieu_thu_theo_ngay.jrxml")) {
            assertNotNull(input);
            JasperDesign design = JRXmlLoader.load(input);

            ReportTemplateCompiler.applySettings(design, List.of(
                    fullSetting("ngayGroup", "Ngày", 1, false, false, false),
                    fullSetting("lyDoThuChi", "Lý do thu chi", 2, false, false, false),
                    fullSetting("soTien", "Số tiền thu", 0, true, true, true)));

            assertEquals(2, design.getGroups().length);
            assertEquals("CfgGroup1", design.getGroups()[0].getName());
            assertEquals("CfgGroup2", design.getGroups()[1].getName());
            assertNotNull(design.getVariablesMap().get("CfgTotalG1_soTien"));
            assertNotNull(design.getVariablesMap().get("CfgTotalG2_soTien"));
            assertNotNull(design.getVariablesMap().get("v_grandTotal"));
            assertEquals("ngayGroup", design.getSortFields()[0].getName());
            assertEquals("lyDoThuChi", design.getSortFields()[1].getName());
            JasperReport report = JasperCompileManager.compileReport(design);
            JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(),
                    new JRBeanCollectionDataSource(List.of(
                            row(1, "Ngày: 08/09/2026", "Thu khác", 200_000d),
                            row(2, "Ngày: 07/09/2026", "Thu công nợ", 500_000d),
                            row(3, "Ngày: 07/09/2026", "Thu khác", 300_000d))));
            assertNotNull(print);
            assertEquals(1, print.getPages().size());
        }
    }

    private PhieuThuReportRow row(int stt, String ngay, String lyDo, double amount) {
        return new PhieuThuReportRow(stt, "PT" + stt, ngay, "Đối tượng " + stt,
                "Địa chỉ", lyDo, "Diễn giải", "CT" + stt, amount);
    }

    private ReportColumnSetting setting(String field, boolean visible, String alignment) {
        return new ReportColumnSetting("danh_sach_phieu_thu_theo_ngay", "Mẫu A4",
                field, field, visible, alignment, 0);
    }

    private ReportColumnSetting fullSetting(String field, String display, int groupLevel,
                                            boolean totalGroup1, boolean totalGroup2,
                                            boolean grandTotal) {
        return new ReportColumnSetting("danh_sach_phieu_thu_theo_ngay", "Mẫu A4",
                field, display, true, groupLevel, totalGroup1, totalGroup2,
                grandTotal, "LEFT", 0);
    }

    private JRDesignElement findDetailField(JasperDesign design, String fieldName) {
        for (JRBand band : design.getDetailSection().getBands()) {
            for (JRElement element : band.getElements()) {
                if (element instanceof JRTextField) {
                    JRTextField textField = (JRTextField) element;
                    if (textField.getExpression() != null
                            && textField.getExpression().getText().contains("$F{" + fieldName + "}")) {
                        return (JRDesignElement) element;
                    }
                }
            }
        }
        throw new AssertionError("Không tìm thấy field " + fieldName);
    }
}
