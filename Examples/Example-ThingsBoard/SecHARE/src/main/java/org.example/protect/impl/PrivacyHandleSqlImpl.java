package org.example.protect.impl;

import org.example.global.PrivacyGlobalValue;
import org.example.dao.sql.DbTemplate;
import org.example.dao.sql.ObjectId;
import org.example.protect.PrivacyHandle;
import org.example.protect.entity.PrivacyMappingEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.example.dao.constants.DbConstants.*;

@Slf4j
public class PrivacyHandleSqlImpl extends PrivacyHandleBase implements PrivacyHandle {

    private final DbTemplate db = PrivacyGlobalValue.get().getSqlDbTemplate();


    @Override
    public String getShadowInfoByDeviceIdAndUserId(String fieldName, String deviceId, String userId) {
        PrivacyMappingEntity pme = selectOneByInfo(fieldName, new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, userId});
        return pme != null ? pme.getShadowInfo() : null;
    }

    @Override
    public String getShadowInfoByRealInfoAndUserId(String fieldName, String realInfo, String userId) {
        PrivacyMappingEntity pme = selectOneByInfo(fieldName, new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, userId});
        return pme != null ? pme.getShadowInfo() : null;
    }

    @Override
    public String getShadowInfoByRealInfoWithNoAuth(String fieldName, String realInfo) {
        PrivacyMappingEntity pme = selectOneByInfo(fieldName, new String[]{REAL_INFO}, new String[]{realInfo});
        return pme != null ? pme.getShadowInfo() : null;
    }

    @Override
    public String getShadowInfoByRealInfoWithAuth(String fieldName, String realInfo) {
        PrivacyMappingEntity pme = selectOneByInfo(fieldName, new String[]{REAL_INFO}, new String[]{realInfo});
        return (pme == null || pme.getUserId() == null) ? null : pme.getShadowInfo();
    }

    @Override
    public String getRealInfoByShadowInfoWithNoAuth(String fieldName, String shadowInfo) {
        PrivacyMappingEntity pme = selectOneByInfo(fieldName, new String[]{SHADOW_INFO}, new String[]{shadowInfo});
        return pme != null ? pme.getRealInfo() : null;
    }

    @Override
    public String getRealInfoByShadowInfoWithAuth(String fieldName, String shadowInfo) {
        PrivacyMappingEntity pme = selectOneByInfo(fieldName, new String[]{SHADOW_INFO}, new String[]{shadowInfo});
        return (pme == null || pme.getUserId() == null) ? null : pme.getRealInfo();
    }

    @Override
    public List<PrivacyMappingEntity> getAllByDeviceId(String fieldName, String deviceId) {
        return selectListByInfo(fieldName, new String[]{DEVICE_ID}, new String[]{deviceId});
    }

    @Override
    public List<PrivacyMappingEntity> getAllByUserId(String fieldName, String userId) {
        return selectListByInfo(fieldName, new String[]{USER_ID}, new String[]{userId});
    }

    @Override
    public PrivacyMappingEntity getAllByDeviceIdAndUserId(String fieldName, String deviceId, String userId) {
        return selectOneByInfo(fieldName, new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, userId});
    }

    @Override
    public List<PrivacyMappingEntity> getAllByRealInfo(String fieldName, String realInfo) {
        return selectListByInfo(fieldName, new String[]{REAL_INFO}, new String[]{realInfo});
    }

    @Override
    public PrivacyMappingEntity getAllByRealInfoAndUserId(String fieldName, String realInfo, String userId) {
        return selectOneByInfo(fieldName, new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, userId});
    }

    @Override
    public PrivacyMappingEntity getAllByShadowInfoWithNoAuth(String fieldName, String shadowInfo) {
        return selectOneByInfo(fieldName, new String[]{SHADOW_INFO}, new String[]{shadowInfo});
    }

    @Override
    public PrivacyMappingEntity getAllByShadowInfoWithAuth(String fieldName, String shadowInfo) {
        PrivacyMappingEntity pme = selectOneByInfo(fieldName, new String[]{SHADOW_INFO}, new String[]{shadowInfo});
        return (pme == null || pme.getUserId() == null) ? null : pme;
    }

    @Override
    public void updateRealInfoByDeviceId(String fieldName, String realInfo, String deviceId) {
        updateElementByInfo(fieldName, new String[]{REAL_INFO}, new String[]{realInfo},
                new String[]{DEVICE_ID}, new String[]{deviceId});
    }

    @Override
    public void updateRealInfoAndDeviceIdByUserId(String fieldName, String realInfo, String deviceId, String userId) {
        updateElementByInfo(fieldName, new String[]{REAL_INFO, DEVICE_ID}, new String[]{realInfo, deviceId},
                new String[]{USER_ID}, new String[]{userId});
    }

    @Override
    public void updateUserIdByDeviceIdAndNullUserId(String fieldName, String userId, String deviceId) {
        updateElementByInfo(fieldName, new String[]{USER_ID}, new String[]{userId},
                new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, null});
    }

    @Override
    public void updateUserIdAndShadowInfoByDeviceIdAndNullUserId(String fieldName, String userId,
                                                                 String shadowInfo, String deviceId) {
        updateElementByInfo(fieldName, new String[]{USER_ID, SHADOW_INFO}, new String[]{userId, shadowInfo},
                new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, null});
    }

    @Override
    public void updateNullUserIdAndShadowInfoByDeviceIdAndUserId(String fieldName, String shadowInfo, String deviceId, String userId) {
        updateElementByInfo(fieldName, new String[]{USER_ID, SHADOW_INFO}, new String[]{null, shadowInfo},
                new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, userId});
    }

    @Override
    public void updateUserIdAndShadowInfoByDeviceId(String fieldName, String userId,
                                                                 String shadowInfo, String deviceId) {
        updateElementByInfo(fieldName, new String[]{USER_ID, SHADOW_INFO}, new String[]{userId, shadowInfo},
                new String[]{DEVICE_ID}, new String[]{deviceId});
    }

    @Override
    public void updateUserIdByRealInfoAndNullUserId(String fieldName, String userId, String realInfo) {
        updateElementByInfo(fieldName, new String[]{USER_ID}, new String[]{userId},
                new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, null});
    }

    @Override
    public void updateUserIdAndShadowInfoByRealInfoAndNullUserId(String fieldName, String userId,
                                                                 String shadowInfo, String realInfo) {
        updateElementByInfo(fieldName, new String[]{USER_ID, SHADOW_INFO}, new String[]{userId, shadowInfo},
                new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, null});
    }

    @Override
    public void updateNullUserIdAndShadowInfoByRealInfoAndUserId(String fieldName, String shadowInfo, String realInfo, String userId) {
        updateElementByInfo(fieldName, new String[]{USER_ID, SHADOW_INFO}, new String[]{null, shadowInfo},
                new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, userId});
    }

    @Override
    public void updateUserIdAndShadowInfoByRealInfo(String fieldName, String userId,
                                                                 String shadowInfo, String realInfo) {
        updateElementByInfo(fieldName, new String[]{USER_ID, SHADOW_INFO}, new String[]{userId, shadowInfo},
                new String[]{REAL_INFO}, new String[]{realInfo});
    }

    @Override
    public void updateRemarksByDeviceIdAndUserId(String fieldName, String remarks, String deviceId, String userId) {
        updateElementByInfo(fieldName, new String[]{REMARKS}, new String[]{remarks},
                new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, userId});
    }

    @Override
    public void updateRemarksByRealInfoAndUserId(String fieldName, String remarks, String realInfo, String userId) {
        updateElementByInfo(fieldName, new String[]{REMARKS}, new String[]{remarks},
                new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, userId});
    }

    @Override
    public void updateShadowInfoByDeviceIdAndUserId(String fieldName, String shadowInfo, String deviceId, String userId) {
        updateElementByInfo(fieldName, new String[]{SHADOW_INFO},
                new String[]{shadowInfo== null ? RandomStringUtils.randomAlphanumeric(20) : shadowInfo},
                new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, userId});
    }

    @Override
    public void updateShadowInfoByRealInfoAndUserId(String fieldName, String shadowInfo, String realInfo, String userId) {
        updateElementByInfo(fieldName, new String[]{SHADOW_INFO},
                new String[]{shadowInfo == null ? RandomStringUtils.randomAlphanumeric(20) : shadowInfo},
                new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, userId});
    }

    @Override
    public void deleteByDeviceId(String fieldName, String deviceId) {
        deleteByInfo(fieldName, deviceId, DEVICE_ID);
    }

    @Override
    public void deleteByUserId(String fieldName, String userId) {
        deleteByInfo(fieldName, userId, USER_ID);
    }

    @Override
    public void deleteByRealInfo(String fieldName, String realInfo) {
        deleteByInfo(fieldName, realInfo, REAL_INFO);
    }

    @Override
    public void deleteByShadowInfo(String fieldName, String shadowInfo) {
        deleteByInfo(fieldName, shadowInfo, SHADOW_INFO);
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
            if(!db.isTableExisted(fieldName)) {
                db.createTable(fieldName);
            }

            if(shadowInfo == null) {
                shadowInfo = RandomStringUtils.randomAlphanumeric(20);
            }

            // ensure unique ①
            if (StringUtils.isNotEmpty(deviceId)) {
                PrivacyMappingEntity pme = getAllByDeviceIdAndUserId(fieldName, deviceId, userId);
                if (pme != null) {
                    updateElementByInfo(fieldName, new String[]{REAL_INFO, SHADOW_INFO, REMARKS},
                            new String[]{realInfo, shadowInfo, remarks},
                            new String[]{DEVICE_ID, USER_ID}, new String[]{deviceId, userId});
                    return shadowInfo;
                }
            }

            // ensure unique ②
            if(StringUtils.isNotEmpty(realInfo)) {
                PrivacyMappingEntity pme = getAllByRealInfoAndUserId(fieldName, realInfo, userId);
                if (pme != null) {
                    if (deviceId == null) {
                        updateElementByInfo(fieldName, new String[]{SHADOW_INFO, REMARKS},
                                new String[]{shadowInfo, remarks},
                                new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, userId});
                    } else {
                        updateElementByInfo(fieldName, new String[]{DEVICE_ID, SHADOW_INFO, REMARKS},
                                new String[]{deviceId, shadowInfo, remarks},
                                new String[]{REAL_INFO, USER_ID}, new String[]{realInfo, userId});
                    }
                    return shadowInfo;
                }
            }

            Map<String, Object> valueMap = new HashMap<>(7);
            valueMap.put(ID, ObjectId.next());
            valueMap.put(DEVICE_ID, deviceId == null ? UUID.randomUUID().toString().trim() : deviceId);
            valueMap.put(CREATE_TIME, (int)(System.currentTimeMillis()/1000L));
            valueMap.put(USER_ID, userId);
            valueMap.put(REAL_INFO, realInfo);
            valueMap.put(SHADOW_INFO, shadowInfo);
            valueMap.put(REMARKS, remarks);

            db.insert(fieldName, valueMap);
            return shadowInfo;
        }catch (Exception e) {
            log.error("Get the {} table failed! ", fieldName, e);
            return null;
        }
    }

    private List<PrivacyMappingEntity> selectListByInfo(String fieldName, String[] infoName, String[] infoValue) {
        try {
            Map<String, Object> whereMap = new HashMap<>(infoName.length);
            for(int i = 0; i < infoName.length; i++) {
                if(infoValue[i] == null) {
                    continue;
                }
                whereMap.put(infoName[i], infoValue[i]);
            }
            List<Map<String, Object>> mapList = db.select(convertFieldName(fieldName), whereMap);
            if(!CollectionUtils.isEmpty(mapList)) {
                List<PrivacyMappingEntity> privacyMappingEntities = new ArrayList<>(mapList.size());
                for (Map<String, Object> m : mapList) {
                    privacyMappingEntities.add(mapToObject(m, PrivacyMappingEntity.class));
                }
                return privacyMappingEntities;
            }
        }catch (Exception e) {
            if (!e.getMessage().contains("privacy_")) {
                log.error("Query failed! {}", e.getMessage(), e);
            }
        }
        return new ArrayList<>(0);
    }

    private PrivacyMappingEntity selectOneByInfo(String fieldName, String[] infoName, String[] infoValue) {
        try {
            Map<String, Object> whereMap = new HashMap<>(infoName.length);
            for(int i = 0; i < infoName.length; i++) {
                if(infoValue[i] == null) {
                    continue;
                }
                whereMap.put(infoName[i], infoValue[i]);
            }
            List<Map<String, Object>> mapList = db.select(convertFieldName(fieldName), whereMap);
            if(!CollectionUtils.isEmpty(mapList)) {
                return mapToObject(mapList.get(0), PrivacyMappingEntity.class);
            }
        }catch (Exception e) {
            if (!e.getMessage().contains("privacy_")) {
                log.error("Query failed! {}", e.getMessage(), e);
            }
        }
        return null;
    }

    private void updateElementByInfo(String fieldName, String[] updateName, String[] updateValue,
                                     String[] whereName, String[] whereValue) {
        try {
            int i;
            Map<String, Object> whereMap = new HashMap<>(whereValue.length);
            for(i = 0; i < whereValue.length; i ++) {
                if (whereValue[i] == null) {
                    continue;
                }
                whereMap.put(whereName[i], whereValue[i]);
            }
            Map<String, Object> valueMap = new HashMap<>(updateValue.length);
            for(i = 0; i < updateValue.length; i++) {
                valueMap.put(updateName[i], updateValue[i]);
            }
            db.update(convertFieldName(fieldName), valueMap, whereMap);
        }catch (Exception e) {
            log.error("Update {} failed! ", Arrays.toString(updateName), e);
        }
    }

    private void deleteByInfo(String fieldName, String infoValue, String infoName) {
        try {
            Map<String, Object> whereMap = new HashMap<>(1);
            whereMap.put(infoName, infoValue);
            db.delete(convertFieldName(fieldName),  whereMap);
        }catch (Exception e) {
            log.error("Delete the {} info failed! ", infoValue, e);
        }
    }

}
