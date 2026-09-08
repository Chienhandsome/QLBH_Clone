package kazuko.ex.report.bill;

import kazuko.ex.dao.BillTemplateDAO;
import kazuko.ex.model.bill.BillTemplate;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JrxmlEditorCompatibilityTest {
    @Test
    void generatedJrxmlCanBeLoadedAndCompiledByCurrentJasperReports() throws Exception {
        BillTemplate template = new BillTemplateDAO().newForLegacyTemplate("test", "Mẫu bill 80mm");
        String jrxml = JrxmlEditorServer.createInitialJrxml(template);

        assertTrue(jrxml.contains("<jasperReport"));
        JasperDesign design = JRXmlLoader.load(
                new ByteArrayInputStream(jrxml.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(JasperCompileManager.compileReport(design));
    }
}
