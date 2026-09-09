package kazuko.ex.report.bill;

import kazuko.ex.model.bill.BillTemplate;
import kazuko.ex.model.bill.BillTemplateDefinition;
import kazuko.ex.model.bill.BillTemplateDefinition.Block;
import kazuko.ex.model.bill.BillTemplateDefinition.BlockType;
import kazuko.ex.model.bill.BillTemplateDefinition.Section;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.TextAdjustEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;

import java.util.*;

/** Tạo JasperDesign trực tiếp từ JSON block definition của mẫu bill. */
public class BillReportRenderer {
    public static final List<String> SUPPORTED_BINDINGS = List.of(
            "companyName", "companyAddress", "companyPhone",
            "invoiceNumber", "invoiceDate", "customerName", "customerAddress", "customerPhone",
            "cashier", "note", "subtotal", "discountRate", "discount", "taxRate", "tax",
            "shipping", "returnAmount", "total", "paid", "change", "debt",
            "cash", "bankTransfer", "card", "voucher"
    );
    public static final List<String> SUPPORTED_ITEM_FIELDS = List.of(
            "itemCode", "itemName", "unit", "quantity", "unitPrice",
            "discountRate", "discountAmount", "amount", "note"
    );

    public JasperPrint render(BillTemplate template, Map<String, Object> parameters,
                              List<BillItemRow> items) throws JRException {
        JasperDesign design = createDesign(template);
        JasperReport report = JasperCompileManager.compileReport(design);
        Map<String, Object> safeParams = new HashMap<>();
        if (parameters != null) safeParams.putAll(parameters);
        for (String binding : SUPPORTED_BINDINGS) safeParams.putIfAbsent(binding, "");
        List<BillItemRow> safeItems = items == null ? List.of() : items;
        return JasperFillManager.fillReport(report, safeParams, new JRBeanCollectionDataSource(safeItems));
    }

    public JasperPrint renderSample(BillTemplate template) throws JRException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("companyName", "CỬA HÀNG TÂN AN PHÁT");
        params.put("companyAddress", "123 Nguyễn Văn A, TP. Hồ Chí Minh");
        params.put("companyPhone", "0967 041 111");
        params.put("invoiceNumber", "HD26/000123");
        params.put("invoiceDate", "08/09/2026 11:30");
        params.put("customerName", "Khách lẻ");
        params.put("customerAddress", "");
        params.put("customerPhone", "");
        params.put("cashier", "Thu ngân");
        params.put("note", "");
        params.put("subtotal", "32.000");
        params.put("discountRate", "0");
        params.put("discount", "0");
        params.put("taxRate", "0");
        params.put("tax", "0");
        params.put("shipping", "0");
        params.put("returnAmount", "0");
        params.put("total", "32.000");
        params.put("paid", "32.000");
        params.put("change", "0");
        params.put("debt", "0");
        params.put("cash", "32.000");
        params.put("bankTransfer", "0");
        params.put("card", "0");
        params.put("voucher", "0");
        List<BillItemRow> items = List.of(
                new BillItemRow("Coca Cola", 2, 10_000, 20_000),
                new BillItemRow("Pepsi", 1, 12_000, 12_000)
        );
        return render(template, params, items);
    }

    public JasperDesign createDesign(BillTemplate template) throws JRException {
        if (template == null || template.getDefinition() == null) {
            throw new JRException("Mẫu bill chưa có định nghĩa layout");
        }
        BillTemplateDefinition definition = template.getDefinition();
        JasperDesign design = new JasperDesign();
        design.setName("bill_" + safeName(template.getId()));
        int pageWidth = mmToPoint(template.getPaperWidth());
        int margin = Math.max(3, mmToPoint(definition.getMarginMm()));
        int contentWidth = Math.max(120, pageWidth - margin * 2);
        design.setPageWidth(pageWidth);
        design.setPageHeight(mmToPoint(300));
        design.setLeftMargin(margin);
        design.setRightMargin(margin);
        design.setTopMargin(margin);
        design.setBottomMargin(margin);
        design.setColumnWidth(contentWidth);
        design.setIgnorePagination(true);

        addParameters(design);
        addItemFields(design);

        List<Block> headerBlocks = visibleBlocks(definition, Section.HEADER, false);
        List<Block> summaryBlocks = visibleBlocks(definition, Section.SUMMARY, false);
        List<Block> footerBlocks = visibleBlocks(definition, Section.FOOTER, false);
        boolean showItems = definition.getBlocks().stream()
                .anyMatch(b -> b.isVisible() && b.getType() == BlockType.ITEMS);

        JRDesignBand title = buildBlockBand(headerBlocks, contentWidth, definition.getFontName());
        if (title.getHeight() > 0) design.setTitle(title);

        if (showItems) {
            design.setColumnHeader(buildItemHeader(contentWidth, definition.getFontName()));
            ((JRDesignSection) design.getDetailSection()).addBand(buildItemDetail(contentWidth, definition.getFontName()));
        } else {
            JRDesignBand emptyDetail = new JRDesignBand();
            emptyDetail.setHeight(1);
            ((JRDesignSection) design.getDetailSection()).addBand(emptyDetail);
        }

        List<Block> tail = new ArrayList<>(summaryBlocks);
        tail.addAll(footerBlocks);
        JRDesignBand summary = buildBlockBand(tail, contentWidth, definition.getFontName());
        if (summary.getHeight() > 0) design.setSummary(summary);
        return design;
    }

    private static void addParameters(JasperDesign design) throws JRException {
        for (String name : SUPPORTED_BINDINGS) {
            JRDesignParameter p = new JRDesignParameter();
            p.setName(name);
            p.setValueClass(String.class);
            design.addParameter(p);
        }
    }

    private static void addItemFields(JasperDesign design) throws JRException {
        addField(design, "itemCode", String.class);
        addField(design, "itemName", String.class);
        addField(design, "unit", String.class);
        addField(design, "quantity", Double.class);
        addField(design, "unitPrice", Double.class);
        addField(design, "discountRate", Double.class);
        addField(design, "discountAmount", Double.class);
        addField(design, "amount", Double.class);
        addField(design, "note", String.class);
    }

    private static void addField(JasperDesign design, String name, Class<?> type) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(type);
        design.addField(field);
    }

    private static JRDesignBand buildBlockBand(List<Block> blocks, int width, String fontName) {
        JRDesignBand band = new JRDesignBand();
        int y = 0;
        for (Block block : blocks) {
            if (block.getType() == BlockType.SEPARATOR) {
                JRDesignLine line = new JRDesignLine();
                line.setX(0);
                line.setY(y + 4);
                line.setWidth(width);
                line.setHeight(1);
                band.addElement(line);
                y += 9;
                continue;
            }
            int height = Math.max(14, block.getFontSize() + 7);
            if (block.getType() == BlockType.TEXT) {
                JRDesignStaticText text = new JRDesignStaticText();
                configureText(text, block, fontName, width, y, height);
                text.setText(block.getText() == null ? "" : block.getText());
                band.addElement(text);
                y += height;
            } else if (block.getType() == BlockType.FIELD && SUPPORTED_BINDINGS.contains(block.getBinding())) {
                JRDesignTextField field = new JRDesignTextField();
                configureText(field, block, fontName, width, y, height);
                field.setBlankWhenNull(true);
                field.setExpression(new JRDesignExpression(fieldExpression(block)));
                band.addElement(field);
                y += height;
            }
        }
        band.setHeight(Math.max(0, y));
        return band;
    }

    private static JRDesignBand buildItemHeader(int width, String fontName) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(18);
        int quantityWidth = Math.max(24, width * 12 / 100);
        int priceWidth = Math.max(42, width * 22 / 100);
        int amountWidth = Math.max(46, width * 25 / 100);
        int nameWidth = width - quantityWidth - priceWidth - amountWidth;
        addStatic(band, "Mặt hàng", 0, 0, nameWidth, 18, fontName, 8, true, HorizontalTextAlignEnum.LEFT);
        addStatic(band, "SL", nameWidth, 0, quantityWidth, 18, fontName, 8, true, HorizontalTextAlignEnum.RIGHT);
        addStatic(band, "Giá", nameWidth + quantityWidth, 0, priceWidth, 18, fontName, 8, true, HorizontalTextAlignEnum.RIGHT);
        addStatic(band, "T.tiền", nameWidth + quantityWidth + priceWidth, 0, amountWidth, 18, fontName, 8, true, HorizontalTextAlignEnum.RIGHT);
        JRDesignLine line = new JRDesignLine();
        line.setX(0); line.setY(17); line.setWidth(width); line.setHeight(1);
        band.addElement(line);
        return band;
    }

    private static JRDesignBand buildItemDetail(int width, String fontName) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(24);
        int quantityWidth = Math.max(24, width * 12 / 100);
        int priceWidth = Math.max(42, width * 22 / 100);
        int amountWidth = Math.max(46, width * 25 / 100);
        int nameWidth = width - quantityWidth - priceWidth - amountWidth;
        addFieldText(band, "$F{itemName}", null, 0, 0, nameWidth, 24, fontName, HorizontalTextAlignEnum.LEFT);
        addFieldText(band, "$F{quantity}", "#,##0.##", nameWidth, 0, quantityWidth, 24, fontName, HorizontalTextAlignEnum.RIGHT);
        addFieldText(band, "$F{unitPrice}", "#,##0", nameWidth + quantityWidth, 0, priceWidth, 24, fontName, HorizontalTextAlignEnum.RIGHT);
        addFieldText(band, "$F{amount}", "#,##0", nameWidth + quantityWidth + priceWidth, 0, amountWidth, 24, fontName, HorizontalTextAlignEnum.RIGHT);
        return band;
    }

    private static void configureText(JRDesignTextElement text, Block block, String fontName,
                                      int width, int y, int height) {
        text.setX(0);
        text.setY(y);
        text.setWidth(width);
        text.setHeight(height);
        text.setFontName(fontName == null || fontName.isBlank() ? "Tahoma" : fontName);
        text.setFontSize((float) Math.max(6, block.getFontSize()));
        text.setBold(block.isBold());
        text.setHorizontalTextAlign(alignment(block.getAlignment()));
        text.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
    }

    private static void addStatic(JRDesignBand band, String value, int x, int y, int width, int height,
                                  String fontName, int fontSize, boolean bold, HorizontalTextAlignEnum align) {
        JRDesignStaticText text = new JRDesignStaticText();
        text.setX(x); text.setY(y); text.setWidth(width); text.setHeight(height);
        text.setText(value); text.setFontName(fontName); text.setFontSize((float) fontSize); text.setBold(bold);
        text.setHorizontalTextAlign(align); text.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        band.addElement(text);
    }

    private static void addFieldText(JRDesignBand band, String expression, String pattern,
                                     int x, int y, int width, int height, String fontName,
                                     HorizontalTextAlignEnum align) {
        JRDesignTextField text = new JRDesignTextField();
        text.setX(x); text.setY(y); text.setWidth(width); text.setHeight(height);
        text.setFontName(fontName); text.setFontSize(8f);
        text.setHorizontalTextAlign(align); text.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
        text.setBlankWhenNull(true); text.setPositionType(PositionTypeEnum.FLOAT);
        text.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        if (pattern != null) text.setPattern(pattern);
        text.setExpression(new JRDesignExpression(expression));
        band.addElement(text);
    }

    private static List<Block> visibleBlocks(BillTemplateDefinition definition, Section section,
                                             boolean includeItems) {
        return definition.getBlocks().stream()
                .filter(Block::isVisible)
                .filter(b -> b.getSection() == section)
                .filter(b -> includeItems || b.getType() != BlockType.ITEMS)
                .toList();
    }

    private static String fieldExpression(Block block) {
        String label = block.getLabel() == null ? "" : block.getLabel();
        return "\"" + escapeJava(label) + "\" + ($P{" + block.getBinding() + "} == null ? \"\" : $P{" + block.getBinding() + "})";
    }

    private static String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
    }

    private static HorizontalTextAlignEnum alignment(String value) {
        if ("CENTER".equalsIgnoreCase(value)) return HorizontalTextAlignEnum.CENTER;
        if ("RIGHT".equalsIgnoreCase(value)) return HorizontalTextAlignEnum.RIGHT;
        return HorizontalTextAlignEnum.LEFT;
    }

    private static int mmToPoint(double mm) {
        return Math.max(1, (int) Math.round(mm * 72.0 / 25.4));
    }

    private static String safeName(String value) {
        if (value == null) return "preview";
        return value.replaceAll("[^A-Za-z0-9_]", "_");
    }
}
