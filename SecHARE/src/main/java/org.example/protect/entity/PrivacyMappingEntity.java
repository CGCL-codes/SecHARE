package org.example.protect.entity;

import java.io.Serializable;

public class PrivacyMappingEntity implements Serializable {

    private static final long serialVersionUID = 4793296986403052115L;

    private String id;
    private String deviceId;
    private String createTime;
    private String userId;
    private String realInfo;
    private String shadowInfo;
    private String remarks;

    public PrivacyMappingEntity() {}

    public PrivacyMappingEntity(String deviceId, String createTime, String userId, String realInfo,
                                String shadowInfo, String remarks) {
        this.deviceId = deviceId;
        this.createTime = createTime;
        this.userId = userId;
        this.realInfo = realInfo;
        this.shadowInfo = shadowInfo;
        this.remarks = remarks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealInfo() {
        return realInfo;
    }

    public void setRealInfo(String realInfo) {
        this.realInfo = realInfo;
    }

    public String getShadowInfo() {
        return shadowInfo;
    }

    public void setShadowInfo(String shadowInfo) {
        this.shadowInfo = shadowInfo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
