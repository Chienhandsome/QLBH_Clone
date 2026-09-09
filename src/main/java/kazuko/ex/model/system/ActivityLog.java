package kazuko.ex.model.system;

import java.util.Date;

public class ActivityLog {
    private String id;
    private Date eventTime;
    private String userId;
    private String userName;
    private String action;
    private String module;
    private String referenceNo;
    private String content;
    private String deviceName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Date getEventTime() { return eventTime; }
    public void setEventTime(Date eventTime) { this.eventTime = eventTime; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
}
