package kazuko.ex.model.system;

import java.sql.Timestamp;

public class SQuickNote {
    private String id;
    private Integer status = 30;
    private String userCreatedId;
    private Timestamp timeCreated;
    private String userModifiedId;
    private Timestamp timeModified;
    private Integer sortOrder;
    private byte[] noteData;
    private String content; // text / rtf representation

    public SQuickNote() {}

    public SQuickNote(String id, Integer sortOrder, byte[] noteData) {
        this.id = id;
        this.sortOrder = sortOrder;
        this.noteData = noteData;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getUserCreatedId() { return userCreatedId; }
    public void setUserCreatedId(String userCreatedId) { this.userCreatedId = userCreatedId; }

    public Timestamp getTimeCreated() { return timeCreated; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    public String getUserModifiedId() { return userModifiedId; }
    public void setUserModifiedId(String userModifiedId) { this.userModifiedId = userModifiedId; }

    public Timestamp getTimeModified() { return timeModified; }
    public void setTimeModified(Timestamp timeModified) { this.timeModified = timeModified; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public byte[] getNoteData() { return noteData; }
    public void setNoteData(byte[] noteData) { this.noteData = noteData; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
