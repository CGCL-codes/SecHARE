package org.example.dao.nosql.mongo.entity;

import org.example.protect.entity.PrivacyMappingEntity;
import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.example.dao.constants.DbConstants.*;

@Document
public class MappingTable implements Serializable {
    private static final long serialVersionUID = -2738374699172219071L;

    @Id
    @JSONField(name = "_id")
    private String id;

    @Field(SHADOW_INFO)
    @JSONField(name = SHADOW_INFO)
    private String shadowInfo;

    @Field(CREATE_TIME)
    @JSONField(name = CREATE_TIME)
    private String createTime;

    @Field(DEVICE_ID)
    @JSONField(name = DEVICE_ID)
    private String deviceId;

    @Field(USER_ID)
    @JSONField(name = USER_ID)
    private String userId;

    @Field(REAL_INFO)
    @JSONField(name = REAL_INFO)
    private String realInfo;

    @Field(REMARKS)
    @JSONField(name = REMARKS)
    private String remarks;

    public MappingTable() {}

    public MappingTable(PrivacyMappingEntity dto) {
        this.id = dto.getId();
        this.shadowInfo = dto.getShadowInfo();
        this.createTime = dto.getCreateTime();
        this.deviceId = dto.getDeviceId();
        this.userId = dto.getUserId();
        this.realInfo = dto.getRealInfo();
        this.remarks = dto.getRemarks();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShadowInfo() {
        return shadowInfo;
    }

    public void setShadowInfo(String shadowInfo) {
        this.shadowInfo = shadowInfo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
