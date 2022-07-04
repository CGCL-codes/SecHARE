package org.example.protect.impl;

import org.example.global.PrivacyGlobalValue;
import org.example.dao.nosql.mongo.MongoDbTemplate;
import org.example.dao.nosql.mongo.entity.MappingTable;
import org.example.protect.PrivacyHandle;
import org.example.protect.entity.PrivacyMappingEntity;
import org.example.util.ReflectAsmUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;

import static org.example.dao.constants.DbConstants.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
public class PrivacyHandleMongoImpl extends PrivacyHandleBase implements PrivacyHandle {

    private final MongoDbTemplate db = PrivacyGlobalValue.get().getMongoDbTemplate();

    @Override
    public String getShadowInfoByDeviceIdAndUserId(String fieldName, String deviceId, String userId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(userId));
        MappingTable m = db.findOne(dbQuery, convertFieldName(fieldName));
        return m == null ? null : m.getShadowInfo();
    }

    @Override
    public String getShadowInfoByRealInfoAndUserId(String fieldName, String realInfo, String userId) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(userId));
        MappingTable m = db.findOne(dbQuery, convertFieldName(fieldName));
        return m == null ? null : m.getShadowInfo();
    }

    @Override
    public String getShadowInfoByRealInfoWithNoAuth(String fieldName, String realInfo) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo));
        MappingTable m = db.findOne(dbQuery, convertFieldName(fieldName));
        return m == null ? null : m.getShadowInfo();
    }

    @Override
    public String getShadowInfoByRealInfoWithAuth(String fieldName, String realInfo) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo));
        MappingTable m = db.findOne(dbQuery, convertFieldName(fieldName));
        return (m == null || m.getUserId() == null) ? null : m.getShadowInfo();
    }

    @Override
    public String getRealInfoByShadowInfoWithNoAuth(String fieldName, String shadowInfo) {
        Query dbQuery = query(where(SHADOW_INFO).is(shadowInfo));
        MappingTable m = db.findOne(dbQuery, convertFieldName(fieldName));
        return m == null ? null : m.getRealInfo();
    }

    @Override
    public String getRealInfoByShadowInfoWithAuth(String fieldName, String shadowInfo) {
        Query dbQuery = query(where(SHADOW_INFO).is(shadowInfo));
        MappingTable m = db.findOne(dbQuery, convertFieldName(fieldName));
        return (m == null || m.getUserId() == null) ? null : m.getRealInfo();
    }

    @Override
    public List<PrivacyMappingEntity> getAllByDeviceId(String fieldName, String deviceId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId));
        return toPmeList(db.find(dbQuery, convertFieldName(fieldName)));
    }

    @Override
    public List<PrivacyMappingEntity> getAllByUserId(String fieldName, String userId) {
        Query dbQuery = query(where(USER_ID).is(userId));
        return toPmeList(db.find(dbQuery, convertFieldName(fieldName)));
    }

    @Override
    public PrivacyMappingEntity getAllByDeviceIdAndUserId(String fieldName, String deviceId, String userId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(userId));
        return toPme(db.findOne(dbQuery, convertFieldName(fieldName)));
    }

    @Override
    public List<PrivacyMappingEntity> getAllByRealInfo(String fieldName, String realInfo) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo));
        return toPmeList(db.find(dbQuery, convertFieldName(fieldName)));
    }

    @Override
    public PrivacyMappingEntity getAllByRealInfoAndUserId(String fieldName, String realInfo, String userId) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(userId));
        return toPme(db.findOne(dbQuery, convertFieldName(fieldName)));
    }

    @Override
    public PrivacyMappingEntity getAllByShadowInfoWithNoAuth(String fieldName, String shadowInfo) {
        Query dbQuery = query(where(SHADOW_INFO).is(shadowInfo));
        return toPme(db.findOne(dbQuery, convertFieldName(fieldName)));
    }

    @Override
    public PrivacyMappingEntity getAllByShadowInfoWithAuth(String fieldName, String shadowInfo) {
        Query dbQuery = query(where(SHADOW_INFO).is(shadowInfo));
        PrivacyMappingEntity pme = toPme(db.findOne(dbQuery, convertFieldName(fieldName)));
        return (pme == null || pme.getUserId() == null) ? null : pme;
    }

    @Override
    public void updateRealInfoByDeviceId(String fieldName, String realInfo, String deviceId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId));
        Update dbUpdate = Update.update(REAL_INFO, realInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateRealInfoAndDeviceIdByUserId(String fieldName, String realInfo, String deviceId, String userId) {
        Query dbQuery = query(where(USER_ID).is(userId));
        Update dbUpdate = Update.update(REAL_INFO, realInfo).set(DEVICE_ID, deviceId);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateUserIdByDeviceIdAndNullUserId(String fieldName, String userId, String deviceId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(null));
        Update dbUpdate = Update.update(USER_ID, userId);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateUserIdAndShadowInfoByDeviceIdAndNullUserId(String fieldName, String userId,
                                                                 String shadowInfo, String deviceId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(null));
        Update dbUpdate = Update.update(USER_ID, userId).set(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateNullUserIdAndShadowInfoByDeviceIdAndUserId(String fieldName, String shadowInfo, String deviceId, String userId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(userId));
        Update dbUpdate = Update.update(USER_ID, null).set(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateUserIdAndShadowInfoByDeviceId(String fieldName, String userId, String shadowInfo, String deviceId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId));
        Update dbUpdate = Update.update(USER_ID, userId).set(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateUserIdByRealInfoAndNullUserId(String fieldName, String userId, String realInfo) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(null));
        Update dbUpdate = Update.update(USER_ID, userId);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateUserIdAndShadowInfoByRealInfoAndNullUserId(String fieldName, String userId,
                                                                 String shadowInfo, String realInfo) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(null));
        Update dbUpdate = Update.update(USER_ID, userId).set(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateNullUserIdAndShadowInfoByRealInfoAndUserId(String fieldName, String shadowInfo, String realInfo, String userId) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(userId));
        Update dbUpdate = Update.update(USER_ID, null).set(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateUserIdAndShadowInfoByRealInfo(String fieldName, String userId, String shadowInfo, String realInfo) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo));
        Update dbUpdate = Update.update(USER_ID, userId).set(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateRemarksByDeviceIdAndUserId(String fieldName, String remarks, String deviceId, String userId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(userId));
        Update dbUpdate = Update.update(REMARKS, remarks);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateRemarksByRealInfoAndUserId(String fieldName, String remarks, String realInfo, String userId) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(userId));
        Update dbUpdate = Update.update(REMARKS, remarks);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateShadowInfoByDeviceIdAndUserId(String fieldName, String shadowInfo, String deviceId, String userId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(userId));
        Update dbUpdate = Update.update(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void updateShadowInfoByRealInfoAndUserId(String fieldName, String shadowInfo, String realInfo, String userId) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(userId));
        Update dbUpdate = Update.update(SHADOW_INFO, shadowInfo);
        db.updateMulti(dbQuery, dbUpdate, convertFieldName(fieldName));
    }

    @Override
    public void deleteByDeviceId(String fieldName, String deviceId) {
        Query dbQuery = query(where(DEVICE_ID).is(deviceId));
        db.remove(dbQuery, convertFieldName(fieldName));
    }

    @Override
    public void deleteByUserId(String fieldName, String userId) {
        Query dbQuery = query(where(USER_ID).is(userId));
        db.remove(dbQuery, convertFieldName(fieldName));
    }

    @Override
    public void deleteByRealInfo(String fieldName, String realInfo) {
        Query dbQuery = query(where(REAL_INFO).is(realInfo));
        db.remove(dbQuery, convertFieldName(fieldName));
    }

    @Override
    public void deleteByShadowInfo(String fieldName, String shadowInfo) {
        Query dbQuery = query(where(SHADOW_INFO).is(shadowInfo));
        db.remove(dbQuery, convertFieldName(fieldName));
    }

    @Override
    public String generateShadowInfo(String fieldName, String deviceId, String realInfo) {
        return generateShadowInfo(fieldName, deviceId, null, realInfo);
    }

    @Override
    public String generateShadowInfo(String fieldName, String deviceId, String userId, String realInfo) {
        return generateShadowInfo(fieldName, deviceId, userId, realInfo, null);
    }

    @Override
    public String generateShadowInfo(String fieldName, String deviceId, String userId, String realInfo, String remarks) {
        return generateShadowInfo(fieldName, deviceId, userId, realInfo, null, remarks);
    }

    @Override
    public String generateShadowInfo(String fieldName, String deviceId, String userId, String realInfo, String shadowInfo, String remarks) {
        return saveShadowInfo(fieldName, deviceId, userId, realInfo, shadowInfo, remarks);
    }

    private String saveShadowInfo(String fieldName, String deviceId, String userId, String realInfo, String shadowInfo, String remarks) {
        try {
            fieldName = convertFieldName(fieldName);
            db.createCollection(fieldName);

            if(shadowInfo == null) {
                shadowInfo = RandomStringUtils.randomAlphanumeric(20);
            }

            // ensure unique ①
            if (StringUtils.isNotEmpty(deviceId)) {
                PrivacyMappingEntity pme = getAllByDeviceIdAndUserId(fieldName, deviceId, userId);
                if (pme != null) {
                    Query dbQuery = query(where(DEVICE_ID).is(deviceId)).addCriteria(where(USER_ID).is(userId));
                    Update dbUpdate = Update.update(REAL_INFO, realInfo).set(SHADOW_INFO, shadowInfo)
                            .set(REMARKS, remarks);
                    db.updateFirst(dbQuery, dbUpdate, fieldName);
                    return shadowInfo;
                }

            }

            // ensure unique ②
            if(StringUtils.isNotEmpty(realInfo)) {
                PrivacyMappingEntity pme = getAllByRealInfoAndUserId(fieldName, realInfo, userId);
                if (pme != null) {
                    Query dbQuery = query(where(REAL_INFO).is(realInfo)).addCriteria(where(USER_ID).is(userId));
                    Update dbUpdate = Update.update(SHADOW_INFO, shadowInfo).set(REMARKS, remarks);
                    if (deviceId != null) {
                        dbUpdate.set(DEVICE_ID, deviceId);
                    }
                    db.updateFirst(dbQuery, dbUpdate, fieldName);
                    return shadowInfo;
                }
            }

            PrivacyMappingEntity pme = new PrivacyMappingEntity(
                    deviceId == null ? UUID.randomUUID().toString().trim() : deviceId,
                    Long.toString(System.currentTimeMillis()/1000L),
                    userId,
                    realInfo,
                    shadowInfo,
                    remarks
                    );

            db.save(pme, fieldName);
            return shadowInfo;
        }catch (Exception e) {
            log.error("Get the {} table failed! ", fieldName, e);
            return null;
        }
    }

    private List<PrivacyMappingEntity> toPmeList(List<MappingTable> mList) {
        if (mList == null) {
            return new ArrayList<>(0);
        }
        List<PrivacyMappingEntity> result = new ArrayList<>(mList.size());
        for (MappingTable m : mList) {
            PrivacyMappingEntity p = new PrivacyMappingEntity();
            ReflectAsmUtil.copyProperties(p, m);
            result.add(p);
        }
        return result;
    }

    private PrivacyMappingEntity toPme(MappingTable m) {
        if (m == null) {
            return null;
        }
        PrivacyMappingEntity p = new PrivacyMappingEntity();
        ReflectAsmUtil.copyProperties(p, m);
        return p;
    }

}
