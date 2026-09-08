package kazuko.ex.report.bill;

import kazuko.ex.model.bill.BillTemplate;
import kazuko.ex.model.bill.BillTemplateDefinition;
import kazuko.ex.util.BillTemplateJson;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BillTemplateRendererTest {
    @Test
    void jsonRoundTripPreservesLayout() {
        BillTemplateDefinition source = BillTemplateDefinition.createDefault80mm();
        String json = BillTemplateJson.toJson(source);
        BillTemplateDefinition restored = BillTemplateJson.fromJson(json);
        assertEquals(source.getSchemaVersion(), restored.getSchemaVersion());
        assertEquals(source.getBlocks().size(), restored.getBlocks().size());
        assertTrue(json.contains("companyName"));
        assertTrue(json.contains("ITEMS"));
    }

    @Test
    void rendersDefault80mmTemplate() throws Exception {
        BillTemplate template = sampleTemplate();
        JasperPrint print = new BillReportRenderer().renderSample(template);
        assertNotNull(print);
        assertFalse(print.getPages().isEmpty());
        assertTrue(print.getPageWidth() >= 220 && print.getPageWidth() <= 230,
                "80mm phải tương đương khoảng 227 point");
    }

    /** Cho phép chạy smoke test riêng khi toàn dự án đang có lỗi ở module khác. */
    public static void main(String[] args) throws Exception {
        BillTemplateRendererTest test = new BillTemplateRendererTest();
        test.jsonRoundTripPreservesLayout();
        test.rendersDefault80mmTemplate();
        System.out.println("BillTemplateRendererTest: OK");
    }

    private static BillTemplate sampleTemplate() {
        BillTemplate template = new BillTemplate();
        template.setId(UUID.randomUUID().toString());
        template.setName("Mẫu in bill 80mm - Java");
        template.setPaperWidth(80);
        template.setDefinition(BillTemplateDefinition.createDefault80mm());
        return template;
    }
}
