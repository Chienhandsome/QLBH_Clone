package kazuko.ex.model.system;

/** Cấu hình hiển thị một field trong mẫu báo cáo Jasper. */
public class ReportColumnSetting {
    private String reportKey;
    private String templateName;
    private String fieldName;
    private String displayName;
    private boolean visible;
    private int groupLevel;
    private boolean totalGroup1;
    private boolean totalGroup2;
    private boolean grandTotal;
    private String alignment;
    private int sortOrder;

    public ReportColumnSetting() {
    }

    public ReportColumnSetting(String reportKey, String templateName, String fieldName,
                               String displayName, boolean visible, String alignment, int sortOrder) {
        this(reportKey, templateName, fieldName, displayName, visible,
                0, false, false, false, alignment, sortOrder);
    }

    public ReportColumnSetting(String reportKey, String templateName, String fieldName,
                               String displayName, boolean visible, int groupLevel,
                               boolean totalGroup1, boolean totalGroup2, boolean grandTotal,
                               String alignment, int sortOrder) {
        this.reportKey = reportKey;
        this.templateName = templateName;
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.visible = visible;
        this.groupLevel = groupLevel;
        this.totalGroup1 = totalGroup1;
        this.totalGroup2 = totalGroup2;
        this.grandTotal = grandTotal;
        this.alignment = alignment;
        this.sortOrder = sortOrder;
    }

    public String getReportKey() { return reportKey; }
    public void setReportKey(String reportKey) { this.reportKey = reportKey; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public int getGroupLevel() { return groupLevel; }
    public void setGroupLevel(int groupLevel) { this.groupLevel = groupLevel; }
    public boolean isTotalGroup1() { return totalGroup1; }
    public void setTotalGroup1(boolean totalGroup1) { this.totalGroup1 = totalGroup1; }
    public boolean isTotalGroup2() { return totalGroup2; }
    public void setTotalGroup2(boolean totalGroup2) { this.totalGroup2 = totalGroup2; }
    public boolean isGrandTotal() { return grandTotal; }
    public void setGrandTotal(boolean grandTotal) { this.grandTotal = grandTotal; }
    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
