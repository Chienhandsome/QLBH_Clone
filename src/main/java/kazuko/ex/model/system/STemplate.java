package kazuko.ex.model.system;

import java.io.Serializable;

public class STemplate implements Serializable {
    private String id;
    private String name;
    private String note;
    private int status = 30;
    private String sortOrder;
    private int itemType = 0;
    private int autoId;
    private String sImageId;
    private String template;
    private String style;
    private Integer reportBase = 30;

    public STemplate() {
    }

    public STemplate(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public STemplate(String id, String name, String template) {
        this.id = id;
        this.name = name;
        this.template = template;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getAutoId() {
        return autoId;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public String getSImageId() {
        return sImageId;
    }

    public void setSImageId(String sImageId) {
        this.sImageId = sImageId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Integer getReportBase() {
        return reportBase;
    }

    public void setReportBase(Integer reportBase) {
        this.reportBase = reportBase;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
