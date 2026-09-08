package kazuko.ex.report.bill;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Jrxml7To6ConverterTest {
    @Test
    void convertsCompactBillElementsToJasper6Syntax() throws Exception {
        String jr7 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
                    name="bill_poc" pageWidth="227" pageHeight="850" columnWidth="209"
                    leftMargin="9" rightMargin="9" topMargin="9" bottomMargin="9">
                  <parameter name="total" class="java.lang.String"/>
                  <field name="itemName" class="java.lang.String"/>
                  <title height="40">
                    <element kind="staticText" x="0" y="0" width="209" height="20"
                        fontName="Tahoma" fontSize="13" bold="true" hTextAlign="Center">
                      <text><![CDATA[HÓA ĐƠN BÁN HÀNG - POC]]></text>
                    </element>
                    <element kind="line" x="0" y="25" width="209" height="1"/>
                  </title>
                  <detail><band height="20">
                    <element kind="textField" blankWhenNull="true" x="0" y="0" width="209" height="20">
                      <expression><![CDATA[$F{itemName}]]></expression>
                    </element>
                  </band></detail>
                  <summary height="20">
                    <element kind="textField" x="0" y="0" width="209" height="20" hTextAlign="Right">
                      <expression><![CDATA[$P{total}]]></expression>
                    </element>
                  </summary>
                </jasperReport>
                """;

        String jr6 = Jrxml7To6Converter.convert(jr7);
        assertTrue(jr6.contains("<band height=\"40\""));
        assertTrue(jr6.contains("<staticText>"));
        assertTrue(jr6.contains("<textFieldExpression>"));
        assertFalse(jr6.contains("kind=\"staticText\""));

        JasperDesign design = JRXmlLoader.load(
                new ByteArrayInputStream(jr6.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(JasperCompileManager.compileReport(design));
    }
}
