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
        design(owner, templateName, null);
    }

    public void design(Window owner, String templateName, DonHang previewOrder) throws Exception {
        STemplate legacy = legacyTemplateDAO.findByName(templateName);
        if (legacy == null) throw new IllegalArgumentException("Không tìm thấy mẫu '" + templateName + "' trong STEMPLATE");
        BillTemplate template = billTemplateDAO.findOrCreateDraft(legacy.getId(), legacy.getName());
        JrxmlEditorServer.open(template, previewOrder);
    }

    public JasperPrint createPrint(DonHang order, String templateName) throws Exception {
        BillTemplate template = loadTemplate(templateName);
        String jrxml = template.getLegacyTemplateId() == null ? null
                : jrxmlTemplateDAO.findPublishedByLegacyTemplateId(template.getLegacyTemplateId());
        if (jrxml == null || jrxml.isBlank()) {
            return renderer.render(template, buildParameters(order), buildItems(order));
        }
        return createPrintFromJrxml(order, jrxml);
    }

    public JasperPrint createPrintFromJrxml(DonHang order, String jrxml) throws Exception {
        // Chuẩn hóa cả mẫu đã xuất bản từ các phiên bản editor cũ. Nếu text chứa
        // xuống dòng định dạng XML ở đầu, Jasper sẽ in dòng trắng và cắt mất title.
        String normalizedJrxml = Jrxml7To6Converter.convert(jrxml);
        JasperReport report = JasperCompileManager.compileReport(
                new ByteArrayInputStream(normalizedJrxml.getBytes(StandardCharsets.UTF_8)));
        DonHang effectiveOrder = order != null ? order : createSampleOrder();
        Map<String, Object> parameters = buildParameters(effectiveOrder);
        for (String binding : BillReportRenderer.SUPPORTED_BINDINGS) parameters.putIfAbsent(binding, "");
        return JasperFillManager.fillReport(report, parameters,
                new JRBeanCollectionDataSource(buildItems(effectiveOrder)));
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
        params.put("customerAddress", value(safe.getDiaChiKhach(), value(safe.getDiaChiGiaoHang(), "")));
        params.put("customerPhone", value(safe.getDienThoaiKhach(), ""));
        params.put("cashier", value(safe.getNguoiThanhToan(), value(safe.getTenNhanVien(), "")));
        params.put("note", value(safe.getGhiChu(), ""));
        params.put("subtotal", CurrencyUtil.format(safe.getTienHang()));
        params.put("discountRate", formatNumber(safe.getTiLeGiamGia()));
        params.put("discount", CurrencyUtil.format(safe.getTienGiamGia()));
        params.put("taxRate", formatNumber(safe.getTiLeThue()));
        params.put("tax", CurrencyUtil.format(safe.getTienThue()));
        params.put("shipping", CurrencyUtil.format(safe.getPhiVanChuyen()));
        params.put("returnAmount", CurrencyUtil.format(safe.getTienDoiTra()));
        params.put("total", CurrencyUtil.format(safe.getTongCong()));
        params.put("paid", CurrencyUtil.format(safe.getTienThanhToan()));
        params.put("change", CurrencyUtil.format(Math.max(0, safe.getTienThanhToan() - safe.getTongCong())));
        params.put("debt", CurrencyUtil.format(Math.max(0, safe.getTongCong() - safe.getTienThanhToan())));
        params.put("cash", CurrencyUtil.format(safe.getTienMat()));
        params.put("bankTransfer", CurrencyUtil.format(safe.getTienChuyenKhoan()));
        params.put("card", CurrencyUtil.format(safe.getTienTheATM()));
        params.put("voucher", CurrencyUtil.format(safe.getTienVoucher()));
        return params;
    }

    private List<BillItemRow> buildItems(DonHang order) {
        List<BillItemRow> rows = new ArrayList<>();
        if (order == null || order.getChiTietLayHang() == null) return rows;
        for (DonHangChiTiet item : order.getChiTietLayHang()) {
            rows.add(new BillItemRow(item.getMaHang(),
                    value(item.getTenHang(), value(item.getMaHang(), "Mặt hàng")),
                    item.getTenDonViTinh(), item.getSoLuong(), item.getDonGia(),
                    item.getTiLeGiamGia(), item.getTienGiam(), item.getThanhTien(), item.getGhiChu()));
        }
        return rows;
    }

    private static DonHang createSampleOrder() {
        DonHang order = new DonHang();
        order.setName("HD26/000123");
        order.setNgay(new java.util.Date());
        order.setTenKhach("Khách lẻ");
        order.setNguoiThanhToan("Thu ngân");
        order.setTienHang(32_000);
        order.setTongCong(32_000);
        order.setTienThanhToan(32_000);
        order.setTienMat(32_000);
        DonHangChiTiet cola = new DonHangChiTiet();
        cola.setMaHang("COCA");
        cola.setTenHang("Coca Cola");
        cola.setTenDonViTinh("Lon");
        cola.setSoLuong(2);
        cola.setDonGia(10_000);
        DonHangChiTiet pepsi = new DonHangChiTiet();
        pepsi.setMaHang("PEPSI");
        pepsi.setTenHang("Pepsi");
        pepsi.setTenDonViTinh("Lon");
        pepsi.setSoLuong(1);
        pepsi.setDonGia(12_000);
        order.setChiTietLayHang(List.of(cola, pepsi));
        return order;
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value)) return Long.toString(Math.round(value));
        return Double.toString(value);
    }

    private static String safeFileName(String value) {
        if (value == null || value.isBlank()) return "hoa-don";
        return value.replaceAll("[\\\\/:*?\"<>|]", "-");
    }
}
