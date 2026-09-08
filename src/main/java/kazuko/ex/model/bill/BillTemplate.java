package kazuko.ex.model.bill;

import java.util.Date;

/**
 * Mẫu bill do ứng dụng Java quản lý. STEMPLATEID chỉ dùng để liên kết với mẫu
 * legacy đang hiển thị trên màn hình cấu hình; không ghi đè XML FastReport.
 */
public class BillTemplate {
    private String id;
    private String legacyTemplateId;
    private String name;
    private int paperWidth = 80;
    private BillTemplateDefinition definition;
    private int status = 30;
    private int versionNo = 1;
    private Date timeCreated;
    private Date timeModified;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLegacyTemplateId() { return legacyTemplateId; }
    public void setLegacyTemplateId(String legacyTemplateId) { this.legacyTemplateId = legacyTemplateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPaperWidth() { return paperWidth; }
    public void setPaperWidth(int paperWidth) { this.paperWidth = paperWidth; }

    public BillTemplateDefinition getDefinition() { return definition; }
    public void setDefinition(BillTemplateDefinition definition) { this.definition = definition; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getVersionNo() { return versionNo; }
    public void setVersionNo(int versionNo) { this.versionNo = versionNo; }

    public Date getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Date timeCreated) { this.timeCreated = timeCreated; }

    public Date getTimeModified() { return timeModified; }
    public void setTimeModified(Date timeModified) { this.timeModified = timeModified; }
}
