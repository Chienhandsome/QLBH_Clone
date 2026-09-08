package kazuko.ex.report.bill;

import kazuko.ex.dao.BillTemplateDAO;
import kazuko.ex.dao.JrxmlTemplateDAO;
import kazuko.ex.dao.STemplateDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.bill.BillTemplate;
import kazuko.ex.model.system.STemplate;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.ui.report.BillTemplateEditorDialog;
import kazuko.ex.util.CurrencyUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.swing.JRViewer;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Điều phối template Java với dữ liệu DonHang cho preview/in/xuất PDF. */
public class BillPrintService {
    private final STemplateDAO legacyTemplateDAO = new STemplateDAO();
    private final BillTemplateDAO billTemplateDAO = new BillTemplateDAO();
    private final JrxmlTemplateDAO jrxmlTemplateDAO = new JrxmlTemplateDAO();
    private final BillReportRenderer renderer = new BillReportRenderer();

    public void preview(Window owner, DonHang order, String templateName) throws Exception {
        JasperPrint print = createPrint(order, templateName);
        JDialog dialog = new JDialog(owner, "XEM TRƯỚC - " + templateName, Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(900, 720);
        dialog.setLocationRelativeTo(owner);
        dialog.add(new JRViewer(print), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    public void print(DonHang order, String templateName, String printerName) throws Exception {
        JasperPrint print = createPrint(order, templateName);
        if (printerName == null || printerName.isBlank() || printerName.startsWith("Máy in mặc định")) {
            JasperPrintManager.printReport(print, true);
            return;
        }
        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();
        HashPrintRequestAttributeSet requestAttributes = new HashPrintRequestAttributeSet();
        HashPrintServiceAttributeSet serviceAttributes = new HashPrintServiceAttributeSet();
        serviceAttributes.add(new PrinterName(printerName, null));
        configuration.setPrintRequestAttributeSet(requestAttributes);
        configuration.setPrintServiceAttributeSet(serviceAttributes);
        configuration.setDisplayPageDialog(false);
        configuration.setDisplayPrintDialog(false);
        exporter.setConfiguration(configuration);
        exporter.exportReport();
    }

    public void exportPdf(Component parent, DonHang order, String templateName) throws Exception {
        JasperPrint print = createPrint(order, templateName);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất hóa đơn PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(safeFileName(order != null ? order.getName() : "hoa-don") + ".pdf"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File output = chooser.getSelectedFile();
        if (!output.getName().toLowerCase().endsWith(".pdf")) {
            output = new File(output.getParentFile(), output.getName() + ".pdf");
        }
        JasperExportManager.exportReportToPdfFile(print, output.getAbsolutePath());
        JOptionPane.showMessageDialog(parent, "Đã xuất PDF:\n" + output.getAbsolutePath(),
                "Xuất PDF", JOptionPane.INFORMATION_MESSAGE);
    }

    public void design(Window owner, String templateName) throws Exception {
        STemplate legacy = legacyTemplateDAO.findByName(templateName);
        if (legacy == null) throw new IllegalArgumentException("Không tìm thấy mẫu '" + templateName + "' trong STEMPLATE");
        BillTemplate template = billTemplateDAO.findOrCreateDraft(legacy.getId(), legacy.getName());
        BillTemplateEditorDialog.open(owner, template, billTemplateDAO);
    }

    public JasperPrint createPrint(DonHang order, String templateName) throws Exception {
        BillTemplate template = loadTemplate(templateName);
        String jrxml = template.getLegacyTemplateId() == null ? null
                : jrxmlTemplateDAO.findByLegacyTemplateId(template.getLegacyTemplateId());
        if (jrxml == null || jrxml.isBlank()) {
            return renderer.render(template, buildParameters(order), buildItems(order));
        }
        JasperReport report = JasperCompileManager.compileReport(
                new ByteArrayInputStream(jrxml.getBytes(StandardCharsets.UTF_8)));
        Map<String, Object> parameters = buildParameters(order);
        for (String binding : BillReportRenderer.SUPPORTED_BINDINGS) parameters.putIfAbsent(binding, "");
        return JasperFillManager.fillReport(report, parameters,
                new JRBeanCollectionDataSource(buildItems(order)));
    }

    private BillTemplate loadTemplate(String templateName) throws Exception {
        STemplate legacy = legacyTemplateDAO.findByName(templateName);
        if (legacy == null) {
            return billTemplateDAO.newForLegacyTemplate(null, templateName);
        }
        return billTemplateDAO.findOrCreateDraft(legacy.getId(), legacy.getName());
    }

    private Map<String, Object> buildParameters(DonHang order) {
        SystemConfig config = new SystemConfigDAO().getConfig();
        DonHang safe = order != null ? order : new DonHang();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("companyName", value(config.getTenCongTy(), "CỬA HÀNG"));
        params.put("companyAddress", value(config.getDiaChi(), ""));
        params.put("companyPhone", value(config.getSoDienThoai(), ""));
        params.put("invoiceNumber", value(safe.getName(), ""));
        params.put("invoiceDate", safe.getNgay() != null
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(safe.getNgay()) : "");
        params.put("customerName", value(safe.getTenKhach(), "Khách lẻ"));
        params.put("subtotal", CurrencyUtil.format(safe.getTienHang()));
        params.put("discount", CurrencyUtil.format(safe.getTienGiamGia()));
        params.put("tax", CurrencyUtil.format(safe.getTienThue()));
        params.put("total", CurrencyUtil.format(safe.getTongCong()));
        params.put("paid", CurrencyUtil.format(safe.getTienThanhToan()));
        return params;
    }

    private List<BillItemRow> buildItems(DonHang order) {
        List<BillItemRow> rows = new ArrayList<>();
        if (order == null || order.getChiTietLayHang() == null) return rows;
        for (DonHangChiTiet item : order.getChiTietLayHang()) {
            rows.add(new BillItemRow(value(item.getTenHang(), value(item.getMaHang(), "Mặt hàng")),
                    item.getSoLuong(), item.getDonGia(), item.getThanhTien()));
        }
        return rows;
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String safeFileName(String value) {
        if (value == null || value.isBlank()) return "hoa-don";
        return value.replaceAll("[\\\\/:*?\"<>|]", "-");
    }
}
