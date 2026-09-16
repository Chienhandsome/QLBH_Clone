package kazuko.ex.report.service;

import kazuko.ex.dao.ReportColumnSettingDAO;
import kazuko.ex.model.system.ReportColumnSetting;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRElementGroup;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JRSortField;
import net.sf.jasperreports.engine.JRVariable;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignGroup;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignSortField;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JRDesignVariable;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.CalculationEnum;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ResetTypeEnum;
import net.sf.jasperreports.engine.type.SortFieldTypeEnum;
import net.sf.jasperreports.engine.type.SortOrderEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;

/** Biên dịch JRXML và áp dụng thiết lập cột đang lưu trong database. */
public final class ReportTemplateCompiler {
    private static final ReportColumnSettingDAO SETTING_DAO = new ReportColumnSettingDAO();

    private ReportTemplateCompiler() {
    }

    public static JasperReport compile(InputStream input) throws JRException {
        JasperDesign design = JRXmlLoader.load(input);
        // Báo cáo không có dữ liệu vẫn phải hiển thị tiêu đề, header cột,
        // tổng cộng và chữ ký thay vì trả về một trang trắng/không có trang.
        // Điều này loại bỏ nhu cầu chèn dữ liệu minh họa vào luồng production.
        design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
        try {
            List<ReportColumnSetting> settings = SETTING_DAO.findByReportKey(design.getName());
            if (!settings.isEmpty()) {
                applySettings(design, settings);
            }
        } catch (SQLException e) {
            // Không làm hỏng luồng xem báo cáo nếu database cấu hình tạm thời không truy cập được.
            System.err.println("Không áp dụng được thiết lập cột cho " + design.getName() + ": " + e.getMessage());
        }
        return JasperCompileManager.compileReport(design);
    }

    static void applySettings(JasperDesign design, List<ReportColumnSetting> settings) {
        Map<String, ReportColumnSetting> byField = new HashMap<>();
        for (ReportColumnSetting setting : settings) {
            byField.put(setting.getFieldName(), setting);
        }

        List<JRElement> detailElements = new ArrayList<>();
        for (JRBand detailBand : design.getDetailSection().getBands()) {
            collectElements(detailBand, detailElements);
        }

        Map<String, ColumnBounds> boundsByField = new HashMap<>();
        for (JRElement element : detailElements) {
            String fieldName = referencedField(element, byField);
            if (fieldName == null) continue;
            boundsByField.putIfAbsent(fieldName,
                    new ColumnBounds(element.getX(), element.getWidth()));
            applyToDataElement(element, byField.get(fieldName));
        }

        // Field dùng làm nhóm (ví dụ ngàyGroup) có thể không nằm trong detail band.
        List<JRElement> reportElements = collectReportElements(design);
        for (JRElement element : reportElements) {
            String fieldName = referencedField(element, byField);
            if (fieldName != null) {
                boundsByField.putIfAbsent(fieldName,
                        new ColumnBounds(element.getX(), element.getWidth()));
                applyToDataElement(element, byField.get(fieldName));
            }
        }

        JRBand columnHeader = design.getColumnHeader();
        if (columnHeader != null) {
            List<JRElement> headerElements = new ArrayList<>();
            collectElements(columnHeader, headerElements);
            for (JRElement header : headerElements) {
                ReportColumnSetting setting = settingAtBounds(header, settings, boundsByField);
                if (setting != null && (!setting.isVisible() || setting.getGroupLevel() > 0)) {
                    hide(header);
                }
            }
        }
        rebuildGroupsAndTotals(design, settings, boundsByField);
    }

    private static void applyToDataElement(JRElement element, ReportColumnSetting setting) {
        if (!setting.isVisible() || setting.getGroupLevel() > 0) {
            hide(element);
            return;
        }
        if (element instanceof JRDesignTextElement) {
            ((JRDesignTextElement) element).setHorizontalTextAlign(toAlignment(setting.getAlignment()));
        }
    }

    private static void hide(JRElement element) {
        if (element instanceof JRDesignElement) {
            ((JRDesignElement) element).setPrintWhenExpression(new JRDesignExpression("Boolean.FALSE"));
        }
    }

    private static ReportColumnSetting settingAtBounds(JRElement element,
                                                        List<ReportColumnSetting> settings,
                                                        Map<String, ColumnBounds> boundsByField) {
        for (ReportColumnSetting setting : settings) {
            ColumnBounds bounds = boundsByField.get(setting.getFieldName());
            if (bounds != null && bounds.x == element.getX() && bounds.width == element.getWidth()) {
                return setting;
            }
        }
        return null;
    }

    private static String referencedField(JRElement element,
                                          Map<String, ReportColumnSetting> settings) {
        if (!(element instanceof JRTextField)) return null;
        JRTextField textField = (JRTextField) element;
        if (textField.getExpression() == null || textField.getExpression().getText() == null) return null;
        String expression = textField.getExpression().getText();
        for (String fieldName : settings.keySet()) {
            if (expression.contains("$F{" + fieldName + "}")) {
                return fieldName;
            }
        }
        return null;
    }

    private static void collectElements(JRElementGroup group, List<JRElement> target) {
        if (group == null) return;
        for (JRElement element : group.getElements()) {
            target.add(element);
            if (element instanceof JRElementGroup) {
                collectElements((JRElementGroup) element, target);
            }
        }
    }

    private static List<JRElement> collectReportElements(JasperDesign design) {
        List<JRElement> elements = new ArrayList<>();
        collectElements(design.getBackground(), elements);
        collectElements(design.getTitle(), elements);
        collectElements(design.getPageHeader(), elements);
        collectElements(design.getColumnHeader(), elements);
        for (JRBand band : design.getDetailSection().getBands()) collectElements(band, elements);
        collectElements(design.getColumnFooter(), elements);
        collectElements(design.getPageFooter(), elements);
        collectElements(design.getLastPageFooter(), elements);
        collectElements(design.getSummary(), elements);
        collectElements(design.getNoData(), elements);
        for (JRGroup group : design.getGroups()) {
            collectSection(group.getGroupHeaderSection(), elements);
            collectSection(group.getGroupFooterSection(), elements);
        }
        return elements;
    }

    private static void collectSection(JRSection section, List<JRElement> target) {
        if (section == null) return;
        for (JRBand band : section.getBands()) collectElements(band, target);
    }

    private static HorizontalTextAlignEnum toAlignment(String alignment) {
        if ("CENTER".equalsIgnoreCase(alignment) || "Giữa".equalsIgnoreCase(alignment)) {
            return HorizontalTextAlignEnum.CENTER;
        }
        if ("RIGHT".equalsIgnoreCase(alignment) || "Phải".equalsIgnoreCase(alignment)) {
            return HorizontalTextAlignEnum.RIGHT;
        }
        return HorizontalTextAlignEnum.LEFT;
    }

    private static void rebuildGroupsAndTotals(JasperDesign design,
                                                List<ReportColumnSetting> settings,
                                                Map<String, ColumnBounds> boundsByField) {
        for (JRGroup group : design.getGroups().clone()) {
            design.removeGroup(group);
        }
        for (JRVariable variable : design.getVariables().clone()) {
            if (variable.getResetTypeValue() == ResetTypeEnum.GROUP) {
                design.removeVariable(variable);
            }
        }

        ReportColumnSetting group1Setting = groupSetting(settings, 1);
        ReportColumnSetting group2Setting = groupSetting(settings, 2);
        try {
            for (JRSortField sortField : new ArrayList<>(design.getSortFieldsList())) {
                design.removeSortField(sortField);
            }
            if (group1Setting != null) addAscendingSort(design, group1Setting.getFieldName());
            if (group2Setting != null) addAscendingSort(design, group2Setting.getFieldName());

            JRDesignGroup group1 = group1Setting == null ? null
                    : addGroup(design, group1Setting, 1);
            JRDesignGroup group2 = group2Setting == null ? null
                    : addGroup(design, group2Setting, 2);
            if (group1 != null) {
                addGroupTotals(design, group1, 1, settings, boundsByField);
            }
            if (group2 != null) {
                addGroupTotals(design, group2, 2, settings, boundsByField);
            }
            configureGrandTotals(design, settings, boundsByField);
        } catch (JRException e) {
            throw new IllegalStateException("Không tạo được nhóm động cho báo cáo " + design.getName(), e);
        }
    }

    private static void addAscendingSort(JasperDesign design, String fieldName) throws JRException {
        design.addSortField(new JRDesignSortField(
                fieldName, SortFieldTypeEnum.FIELD, SortOrderEnum.ASCENDING));
    }

    private static JRDesignGroup addGroup(JasperDesign design, ReportColumnSetting setting,
                                          int level) throws JRException {
        JRDesignGroup group = new JRDesignGroup();
        group.setName("CfgGroup" + level);
        group.setExpression(new JRDesignExpression("$F{" + setting.getFieldName() + "}"));

        JRDesignBand header = new JRDesignBand();
        header.setHeight(22);
        JRDesignTextField label = new JRDesignTextField();
        label.setX(0);
        label.setY(0);
        label.setWidth(design.getColumnWidth());
        label.setHeight(22);
        label.setBold(true);
        label.setVerticalTextAlign(net.sf.jasperreports.engine.type.VerticalTextAlignEnum.MIDDLE);
        String caption = escapeJava(setting.getDisplayName()) + ": ";
        label.setExpression(new JRDesignExpression(
                "\"" + caption + "\" + ($F{" + setting.getFieldName() + "} == null ? \"\" : " +
                        "String.valueOf($F{" + setting.getFieldName() + "}))"));
        header.addElement(label);
        ((JRDesignSection) group.getGroupHeaderSection()).addBand(header);
        design.addGroup(group);
        return group;
    }

    private static void addGroupTotals(JasperDesign design, JRDesignGroup group, int level,
                                       List<ReportColumnSetting> settings,
                                       Map<String, ColumnBounds> boundsByField) throws JRException {
        List<ReportColumnSetting> totals = new ArrayList<>();
        for (ReportColumnSetting setting : settings) {
            if (level == 1 ? setting.isTotalGroup1() : setting.isTotalGroup2()) {
                totals.add(setting);
            }
        }
        if (totals.isEmpty()) return;
        totals.sort(Comparator.comparingInt(s -> boundsByField
                .getOrDefault(s.getFieldName(), new ColumnBounds(design.getColumnWidth() - 120, 120)).x));

        JRDesignBand footer = new JRDesignBand();
        footer.setHeight(22);
        int firstTotalX = totalBounds(totals.get(0), boundsByField, design).x;
        footer.addElement(createTotalLabel(level == 1 ? "Tổng nhóm 1" : "Tổng nhóm 2", firstTotalX));

        for (ReportColumnSetting setting : totals) {
            JRDesignVariable variable = createSumVariable(design, group, setting,
                    "CfgTotalG" + level + "_" + safeIdentifier(setting.getFieldName()), true);
            footer.addElement(createTotalValue(variable.getName(), setting, boundsByField, design));
        }
        ((JRDesignSection) group.getGroupFooterSection()).addBand(footer);
    }

    private static void configureGrandTotals(JasperDesign design,
                                             List<ReportColumnSetting> settings,
                                             Map<String, ColumnBounds> boundsByField) throws JRException {
        List<ReportColumnSetting> totals = new ArrayList<>();
        for (ReportColumnSetting setting : settings) {
            if (setting.isGrandTotal()) totals.add(setting);
        }

        ExistingGrandTotals existing = configureExistingGrandTotalRows(design, settings);
        totals.removeIf(setting -> existing.handledFields.contains(setting.getFieldName()));
        if (totals.isEmpty()) return;
        totals.sort(Comparator.comparingInt(s -> totalBounds(s, boundsByField, design).x));

        JRDesignBand summary;
        if (design.getSummary() instanceof JRDesignBand) {
            summary = (JRDesignBand) design.getSummary();
        } else {
            summary = new JRDesignBand();
            design.setSummary(summary);
        }
        int y;
        if (existing.rowY != null) {
            y = existing.rowY;
        } else {
            y = 0;
            for (JRElement element : summary.getElements()) {
                if (element instanceof JRDesignElement) {
                    ((JRDesignElement) element).setY(element.getY() + 22);
                }
            }
            summary.setHeight(summary.getHeight() + 22);
            int firstTotalX = totalBounds(totals.get(0), boundsByField, design).x;
            summary.addElement(createTotalLabel("TỔNG CỘNG", firstTotalX));
        }
        for (ReportColumnSetting setting : totals) {
            JRDesignVariable variable = createSumVariable(design, null, setting,
                    "CfgGrand_" + safeIdentifier(setting.getFieldName()), false);
            JRDesignTextField value = createTotalValue(variable.getName(), setting, boundsByField, design);
            value.setY(y);
            summary.addElement(value);
        }
    }

    private static JRDesignVariable createSumVariable(JasperDesign design, JRDesignGroup resetGroup,
                                                       ReportColumnSetting setting, String name,
                                                       boolean resetByGroup) throws JRException {
        JRDesignVariable variable = new JRDesignVariable();
        variable.setName(name);
        Class<?> valueClass = design.getFieldsMap().get(setting.getFieldName()).getValueClass();
        variable.setValueClass(valueClass);
        variable.setCalculation(CalculationEnum.SUM);
        variable.setExpression(new JRDesignExpression("$F{" + setting.getFieldName() + "}"));
        variable.setResetType(resetByGroup ? ResetTypeEnum.GROUP : ResetTypeEnum.REPORT);
        if (resetByGroup) variable.setResetGroup(resetGroup);
        design.addVariable(variable);
        return variable;
    }

    private static JRDesignStaticText createTotalLabel(String text, int totalX) {
        JRDesignStaticText label = new JRDesignStaticText();
        label.setX(0);
        label.setY(0);
        label.setWidth(Math.max(1, totalX));
        label.setHeight(22);
        label.setText(text);
        label.setBold(true);
        label.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        label.setVerticalTextAlign(net.sf.jasperreports.engine.type.VerticalTextAlignEnum.MIDDLE);
        return label;
    }

    private static JRDesignTextField createTotalValue(String variableName,
                                                      ReportColumnSetting setting,
                                                      Map<String, ColumnBounds> boundsByField,
                                                      JasperDesign design) {
        ColumnBounds bounds = totalBounds(setting, boundsByField, design);
        JRDesignTextField value = new JRDesignTextField();
        value.setX(bounds.x);
        value.setY(0);
        value.setWidth(bounds.width);
        value.setHeight(22);
        value.setBold(true);
        value.setBlankWhenNull(true);
        value.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        value.setVerticalTextAlign(net.sf.jasperreports.engine.type.VerticalTextAlignEnum.MIDDLE);
        value.setExpression(new JRDesignExpression("$V{" + variableName + "}"));
        return value;
    }

    private static ColumnBounds totalBounds(ReportColumnSetting setting,
                                            Map<String, ColumnBounds> boundsByField,
                                            JasperDesign design) {
        return boundsByField.getOrDefault(setting.getFieldName(),
                new ColumnBounds(Math.max(0, design.getColumnWidth() - 120), 120));
    }

    private static ExistingGrandTotals configureExistingGrandTotalRows(
            JasperDesign design, List<ReportColumnSetting> settings) {
        ExistingGrandTotals result = new ExistingGrandTotals();
        if (design.getSummary() == null) return result;
        Map<String, ReportColumnSetting> byField = new HashMap<>();
        for (ReportColumnSetting setting : settings) byField.put(setting.getFieldName(), setting);
        Map<String, String> sumVariableFields = new HashMap<>();
        for (JRVariable variable : design.getVariables()) {
            if (variable.getCalculationValue() != CalculationEnum.SUM
                    || variable.getExpression() == null) continue;
            for (String field : byField.keySet()) {
                if (variable.getExpression().getText().contains("$F{" + field + "}")) {
                    sumVariableFields.put(variable.getName(), field);
                }
            }
        }
        Map<Integer, List<GrandTotalElement>> rows = new LinkedHashMap<>();
        for (JRElement element : design.getSummary().getElements()) {
            if (!(element instanceof JRTextField)) continue;
            String expression = ((JRTextField) element).getExpression().getText();
            for (Map.Entry<String, String> variable : sumVariableFields.entrySet()) {
                String variableName = variable.getKey();
                if (expression.contains("$V{" + variableName + "}")) {
                    rows.computeIfAbsent(element.getY(), ignored -> new ArrayList<>())
                            .add(new GrandTotalElement(element, variable.getValue()));
                }
            }
        }
        for (Map.Entry<Integer, List<GrandTotalElement>> row : rows.entrySet()) {
            boolean rowHasSelectedTotal = false;
            for (GrandTotalElement total : row.getValue()) {
                ReportColumnSetting setting = byField.get(total.fieldName);
                if (setting != null && setting.isGrandTotal()) {
                    rowHasSelectedTotal = true;
                    result.handledFields.add(total.fieldName);
                    result.rowY = row.getKey();
                } else {
                    hide(total.element);
                }
            }
            if (!rowHasSelectedTotal) {
                int height = row.getValue().get(0).element.getHeight();
                hideElementsOnRow(design.getSummary(), row.getKey(), height);
            }
        }
        return result;
    }

    private static void hideElementsOnRow(JRBand band, int y, int height) {
        int bottom = y + height;
        for (JRElement element : band.getElements()) {
            if (element.getY() < bottom && element.getY() + element.getHeight() > y) {
                hide(element);
            }
        }
    }

    private static ReportColumnSetting groupSetting(List<ReportColumnSetting> settings, int level) {
        for (ReportColumnSetting setting : settings) {
            if (setting.getGroupLevel() == level) return setting;
        }
        return null;
    }

    private static String safeIdentifier(String value) {
        return value == null ? "field" : value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String escapeJava(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record ColumnBounds(int x, int width) {
    }

    private static final class ExistingGrandTotals {
        private final Set<String> handledFields = new HashSet<>();
        private Integer rowY;
    }

    private record GrandTotalElement(JRElement element, String fieldName) {
    }
}
