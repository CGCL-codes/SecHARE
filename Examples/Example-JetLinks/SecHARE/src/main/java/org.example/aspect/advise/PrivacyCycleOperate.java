package org.example.aspect.advise;

import org.example.aspect.entity.PrivacyElemEntity;
import org.example.aspect.entity.PrivacySensitiveInfoEntity;
import org.example.global.PrivacyGlobalValue;
import org.example.protect.PrivacyHandle;
import org.example.protect.entity.PrivacyMappingEntity;
import org.example.util.ClassMethodUtil;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Array;
import java.util.*;

import static org.example.aspect.advise.PrivacyBaseAdvice.*;
import static org.example.aspect.advise.PrivacyBaseAdvice.getRealInfoOfObjectByAuto;

@Slf4j
public class PrivacyCycleOperate {

    private static PrivacyHandle privacyHandle = null;

    private static PrivacySensitiveInfoEntity[] pseArray = null;

    private static final String OBJECT = "object";

    public static void init(PrivacyHandle privacyHandle1, PrivacySensitiveInfoEntity[] pseArray1) {
        privacyHandle = privacyHandle1;
        pseArray = pseArray1;
    }

    /**
     * addDevice -> generate shadowInfo / save deviceId / save userId / save realInfo (possible)
     */
    public static void handleAddDevice(Object o, String idMethodName) {
        // userId
        String userId = getUserId();
        // Object array / extends
        ArrayList<Object> objectArray = checkObjectArray(o);
        // id pretreatment
        String[] deviceIdArray;
        if (objectArray == null) {
            String deviceId = (String) getInfoOfObject(o, idMethodName);
            deviceIdArray = new String[]{deviceId};
        } else {
            deviceIdArray = new String[objectArray.size()];
            for(int i = 0; i < objectArray.size(); i++) {
                deviceIdArray[i] = (String) getInfoOfObject(objectArray.get(i), idMethodName);
            }
        }

        // multiple sensitive info
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            String fieldName = pse.getSensitiveInfo().getFieldInfo();
            if (objectArray == null) {
                String deviceId = deviceIdArray[0];
                String realInfo = getRealInfoOfObjectByAuto(o, pse.getSensitiveInfo()).getInfo();
                privacyHandle.generateShadowInfo(fieldName, deviceId, userId, realInfo);
            } else {
                for(int i = 0; i < objectArray.size(); i++) {
                    String deviceId = deviceIdArray[i];
                    String realInfo = getRealInfoOfObjectByAuto(objectArray.get(i), pse.getSensitiveInfo()).getInfo();
                    privacyHandle.generateShadowInfo(fieldName, deviceId, userId, realInfo);
                }
            }
        } // for
    }

    /**
     * delete device -> gain id value(List...)
     */
    public static void handleDelDevice(Object o, String idMethodName) {
        // gain id
        List<Object> ids = getInfoListOfObject(o, idMethodName);
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            for (Object id : ids) {
                privacyHandle.deleteByDeviceId(pse.getSensitiveInfo().getFieldInfo(), id.toString());
            }
        }
    }

    /**
     * delete user -> gain userId value
     */
    public static void handleDelUser(Object o, String idMethodName) {
        // gain id
        List<Object> userIds = getInfoListOfObject(o, idMethodName);
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            for (Object id : userIds) {
                privacyHandle.deleteByUserId(pse.getSensitiveInfo().getFieldInfo(), id.toString());
            }
        }
    }

    /**
     * update info -> gain id value and sensitiveField value from returned object
     */
    public static void handleUpdateInfo(Object idObj, String idMethodName, Object realObj, String realMethodName,
                                           String sensitiveFieldName) {
        // id
        Object deviceId = getInfoOfListObject(idObj, idMethodName);
        // realInfo
        Object realInfo = getInfoOfListObject(realObj, realMethodName);
        if (deviceId != null && realInfo != null) {
            // Map Handle
            String realInfoStr = mapHandle(realInfo, sensitiveFieldName);
            privacyHandle.updateRealInfoByDeviceId(sensitiveFieldName, realInfoStr, deviceId.toString());
        }
    }

    /**
     * grant Or Revoke Auth
     */
    public static void grantOrRevokeAuth(Object userObj, String userMethodName, Object dvPmObj, String dvPmMethodName,
                                         PrivacyElemEntity sensitivePee, Boolean isGrant) {
        // userId deviceId/permissionId getMethodName
        String userId;
        if (dvPmObj == null) {
            dvPmObj = userObj;
            dvPmMethodName = userMethodName;
            userId = null;
        } else {
            // userId
            userId = (String) getInfoOfListObject(userObj, userMethodName);
        }
        // deviceId, permissionId
        String permissionValue = getBracketsContent(dvPmMethodName);
        String deviceId = null;
        int permissionGrant = -1;
        if(permissionValue == null) {
            deviceId = (String) getInfoOfListObject(dvPmObj, dvPmMethodName);
        } else {
            String perMethodName = dvPmMethodName.substring(0, dvPmMethodName.indexOf("("));
            List<Object> permissionIdList = getInfoListOfObject(dvPmObj, perMethodName);
            permissionGrant = permissionIdList.contains(permissionValue) ? 1 : 0;
        }
        // realInfo value
        String realInfoValue = getRealInfoOfObjectByAuto(userObj, sensitivePee).getInfo();
        grantOrRevokeOperation(isGrant, userId, deviceId, permissionGrant, realInfoValue, sensitivePee.getFieldInfo());
    }
    private static void grantOrRevokeOperation(boolean isGrant, String userId, String deviceId, int permissionGrant,
                                               String realInfoValue, String senInfoFieldName) {
        // UserId(null-- Unauthorized)
        String userIdAuth = isGrant? userId : null;
        // select: grant by null, revoke by userId
        String userIdFind = isGrant? null : userId;

        // grant auth need userId
        if (isGrant && userId == null) {
            return;
        }
        String shadowInfo = RandomStringUtils.randomAlphanumeric(20);

        // device
        if (deviceId != null) {
            // ① revoke and userId null -> no userId/permissionId, deviceId only
            if (!isGrant && userId == null) {
                List<PrivacyMappingEntity> pmeList = privacyHandle.getAllByDeviceId(senInfoFieldName, deviceId);
                if (pmeList.isEmpty()) {
                    privacyHandle.generateShadowInfo(senInfoFieldName, deviceId, null, realInfoValue);
                } else {
                    privacyHandle.updateUserIdAndShadowInfoByDeviceId(senInfoFieldName, null, shadowInfo, deviceId);
                }
                return;
            }

            PrivacyMappingEntity pme = privacyHandle.getAllByDeviceIdAndUserId(senInfoFieldName, deviceId, userIdFind);
            if (pme == null) {
                // grant->userId  revoke->null userId
                privacyHandle.generateShadowInfo(senInfoFieldName, deviceId, userIdAuth, realInfoValue);
                return;
            }
            if (isGrant) {
                // grant: update userId and shadowInfo
                privacyHandle.updateUserIdAndShadowInfoByDeviceIdAndNullUserId(
                        senInfoFieldName, userIdAuth, shadowInfo, deviceId);
            } else {
                // revoke: update userId(null) and shadowInfo
                privacyHandle.updateNullUserIdAndShadowInfoByDeviceIdAndUserId(
                        senInfoFieldName, shadowInfo, deviceId, userId);
            }
        } else if(permissionGrant != -1) {
            // permissionGrant 1:grant 0:revoke
            if(permissionGrant == 1) {
                //  1:grant -> generate
                privacyHandle.generateShadowInfo(senInfoFieldName, null, userId, realInfoValue);
            } else {
                //  0:revoke -> delete
                privacyHandle.deleteByUserId(senInfoFieldName, userId);
            }
        } else if (realInfoValue != null) {
            if (!isGrant && userId == null) {
                // revoke
                List<PrivacyMappingEntity> pmeList = privacyHandle.getAllByRealInfo(senInfoFieldName, realInfoValue);
                if (pmeList.isEmpty()) {
                    privacyHandle.generateShadowInfo(senInfoFieldName, null, null, realInfoValue);
                } else {
                    privacyHandle.updateUserIdAndShadowInfoByRealInfo(senInfoFieldName, null, shadowInfo, realInfoValue);
                }
                return;
            }

            PrivacyMappingEntity pme = privacyHandle.getAllByRealInfoAndUserId(senInfoFieldName, realInfoValue, userIdFind);
            if (pme == null) {
                privacyHandle.generateShadowInfo(senInfoFieldName, null, userIdAuth, realInfoValue);
                return;
            }
            if (isGrant) {
                privacyHandle.updateUserIdAndShadowInfoByRealInfoAndNullUserId(
                        senInfoFieldName, userIdAuth, shadowInfo, realInfoValue);
            } else {
                privacyHandle.updateNullUserIdAndShadowInfoByRealInfoAndUserId(
                        senInfoFieldName, shadowInfo, realInfoValue, userId);
            }
        }
    }

    /**
     * get info -> id real info to shadow info
     */
    public static Object handleGetInfo(String methodName, Object o, boolean isExecuted) {
        if (isExecuted) {
            return o;
        }
        // Authority handle
        String authority;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            authority = JSONArray.toJSON(authorities.iterator().next()).toString();
            String userId = getUserId();
            return handleGetInfo(methodName, o, authority, userId);
        } catch (Exception e) {
            log.error("[PrivacyTool] get Authentication error! Because {}", e.getLocalizedMessage());
        }
        return o;
    }
    public static Object handleGetInfo(String methodName, Object o, String authority, String userId) {
        String[] authArray = PrivacyGlobalValue.get().getPlcEntity().getAuthorityName();
        if(ArrayUtils.isEmpty(authArray) || authority == null) {
            return o;
        }
        boolean isAdmin = true;
        for (String auth : authArray) {
            if(auth.contains("!")) {
                auth = auth.replace("!", "");
                // admin
                if(authority.contains(auth)) {
                    isAdmin = true;
                    break;
                } else {
                    isAdmin = false;
                }
            } else if (authority.contains(auth)) {
                // find not admin
                isAdmin = false;
                break;
            }
        }
        if (isAdmin) {
            return o;
        }
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            PrivacyElemEntity[] getInfoPeeArray = pse.getGetProtectedInfo();
            String sensitiveFieldName = pse.getSensitiveInfo().getFieldInfo();
            // It was measured before
//            if(ArrayUtils.isEmpty(getInfoPeeArray)) {
//                return;
//            }
            for (PrivacyElemEntity pee : getInfoPeeArray) {
                // filter
                if (!checkMethodName(methodName, pee.getMethodOrClassPath())) {
                    continue;
                }
                // Object o is array, collection, map?
                Class<?> objClass = o.getClass();
                if (objClass.isArray()){
                    for (int i = 0; i < Array.getLength(o); i++) {
                        Object obj = setGetInfoSingleObject(Array.get(o, i), pee.getFieldInfo(), sensitiveFieldName, userId);
                        Array.set(o, i, obj);
                    }
                }else if (Collection.class.isAssignableFrom(objClass)) {
                    for (Object obj : (Collection<?>)o) {
                        setGetInfoSingleObject(obj, pee.getFieldInfo(), sensitiveFieldName, userId);
                    }
                } else {
                    return setGetInfoSingleObject(o, pee.getFieldInfo(), sensitiveFieldName, userId);
                }
                break;
            } // for
        } // for
        return o;
    }

    @SuppressWarnings("unchecked")
    private static Object setGetInfoSingleObject(Object o, String getMethodName, String sensitiveFieldName, String userId) {
        // id/realInfo getMethodName
        String[] idAndRealInfo = getMethodName.split("-");
        String realInfoMethodName = idAndRealInfo[0];
        String idMethodName = idAndRealInfo[1];

        Object deviceId = getInfoOfObject(o, idMethodName);

        Object realInfo;
        // Map handle
        Map<Object, Object> map = null;
        String mapKey = sensitiveFieldName;
        if(sensitiveFieldName.contains("_")) {
            String[] mapGetMethod = sensitiveFieldName.split("_");
            mapKey = mapGetMethod[1];
            map = (Map<Object, Object>) getInfoOfObjectByAndSymbol(o, realInfoMethodName);
            realInfo = map.getOrDefault(mapKey, null);
        } else {
            realInfo = getInfoOfObject(o, realInfoMethodName);
        }

        if (deviceId != null && realInfo != null) {
            String shadowInfo = null;

            List<PrivacyMappingEntity> pmeList = privacyHandle.getAllByUserId(sensitiveFieldName, userId);
            boolean isUpdated = false;
            boolean isAuth = true;
            if(pmeList != null) {
                for(PrivacyMappingEntity pme : pmeList) {
                    if(StringUtils.isEmpty(pme.getRealInfo())) {
                        privacyHandle.updateRealInfoAndDeviceIdByUserId(sensitiveFieldName,
                                realInfo.toString(), deviceId.toString(), userId);
                        shadowInfo = pme.getShadowInfo();
                        isUpdated = true;
                        break;
                    } else if(pme.getRealInfo().equals(realInfo)){
                        shadowInfo = pme.getShadowInfo();
                        isUpdated = true;
                        break;
                    }
                }
            } else {
                // determine the auth by whether the userid is empty
                isAuth = false;
            }
            if(!isUpdated) {
                PrivacyMappingEntity pme = privacyHandle.getAllByDeviceIdAndUserId(sensitiveFieldName, deviceId.toString(), userId);
                if (pme != null) {
                    if (StringUtils.isEmpty(pme.getRealInfo()) || !pme.getRealInfo().equals(realInfo)) {
                        privacyHandle.updateRealInfoByDeviceId(sensitiveFieldName, realInfo.toString(), deviceId.toString());
                    }
                    shadowInfo = pme.getShadowInfo();
                } else {
                    if(!isAuth) {
                        userId = null;
                    }
                    shadowInfo = privacyHandle.generateShadowInfo(sensitiveFieldName, deviceId.toString(), userId, realInfo.toString(), "f");
                }
            }

            if(map != null) {
                map.put(mapKey, shadowInfo);
                return setInfoOfObjectByAndSymbol(o, realInfoMethodName, map);
            } else {
                return setInfoOfObjectByAndSymbol(o, realInfoMethodName, shadowInfo);
            }
        }
        return o;
    }

    /**
     *  shadow info[Object[] parameters] -> real info
     *  handleVerifySenInfoBefore
     */
    public static Object setVerifyOfObject(Object o, String getMethodName, String senInfoFieldName) {
        // Object o is array, collection, map?
        Class<?> objClass = o.getClass();
        if (objClass.isArray()){
            for (int i = 0; i < Array.getLength(o); i++) {
                Object obj = setVerifyInfoSingleObject(Array.get(o, i), getMethodName, senInfoFieldName);
                Array.set(o, i, obj);
            }
        }else if (Collection.class.isAssignableFrom(objClass)) {
            for (Object obj : (Collection<?>)o) {
                setVerifyInfoSingleObject(obj, getMethodName, senInfoFieldName);
            }
        } else {
            setVerifyInfoSingleObject(o, getMethodName, senInfoFieldName);
        }
        return o;
    }

    /**
     *  shadow info[returned obj] -> real info
     */
    public static Object handleVerifySenInfo(String methodName, Object o) {
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            PrivacyElemEntity[] verifySenPeeArray = pse.getVerifySenInfo();
            String senInfoFieldName = pse.getSensitiveInfo().getFieldInfo();

            if (ArrayUtils.isEmpty(verifySenPeeArray) || StringUtils.isEmpty(senInfoFieldName)) {
                return o;
            }

            for (PrivacyElemEntity pee : verifySenPeeArray) {
                String protectedInfo = pee.getFieldInfo();
                if (!checkMethodName(methodName, pee.getMethodOrClassPath())) {
                    continue;
                }
                // Object o is array, collection?
                Class<?> objClass = o.getClass();
                if (objClass.isArray()){
                    for (int i = 0; i < Array.getLength(o); i++) {
                        Object obj = setVerifyInfoSingleObject(Array.get(o, i), protectedInfo, senInfoFieldName);
                        Array.set(o, i, obj);
                    }
                }else if (Collection.class.isAssignableFrom(objClass)) {
                    for (Object obj : (Collection<?>)o) {
                        setVerifyInfoSingleObject(obj, protectedInfo, senInfoFieldName);
                    }
                } else {
                    return setVerifyInfoSingleObject(o, protectedInfo, senInfoFieldName);
                }
            } // for
        }
        return o;
    }
    @SuppressWarnings("unchecked")
    private static Object setVerifyInfoSingleObject(Object o, String getMethodName, String senInfoFieldName) {
        Object realInfShadowInfo;
        // shadow->real? real->shadow?
        boolean shadow2Real = true;
        if(getMethodName.contains("(")) {
            String realShadowStr = getBracketsContent(getMethodName);
            if(realShadowStr != null && realShadowStr.contains("r-s")) {
                shadow2Real = false;
            }
            getMethodName = getMethodName.substring(0, getMethodName.indexOf("("));
        }
        // getMethod, setMethod
        String setMethodName = null;
        if(getMethodName.contains("@")) {
            String[] getSetMethodArray =getMethodName.split("@");
            getMethodName = getSetMethodArray[0];
            setMethodName = getSetMethodArray[1];
        }
        // obj
        boolean isBasic = ClassMethodUtil.isBasicType(o) || getMethodName.toLowerCase(Locale.ROOT).contains(OBJECT);
        // Map handle
        Map<Object, Object> map = null;
        String mapKey = senInfoFieldName;
        if(senInfoFieldName.contains("_")) {
            String[] mapGetMethod = senInfoFieldName.split("_");
            mapKey = mapGetMethod[1];
            map = isBasic ? (Map<Object, Object>) o : (Map<Object, Object>) getInfoOfObjectByAndSymbol(o, getMethodName);
            realInfShadowInfo = map.getOrDefault(mapKey, null);
        } else {
            realInfShadowInfo = isBasic ? o : getInfoOfObject(o, getMethodName);
        }
        if (realInfShadowInfo != null) {
            String realInfo;
            if (shadow2Real) {
                // realInfShadowInfo is shadowInfo
                realInfo = PrivacyGlobalValue.get().isNeedAuth() ?
                        privacyHandle.getRealInfoByShadowInfoWithAuth(senInfoFieldName, realInfShadowInfo.toString()) :
                        privacyHandle.getRealInfoByShadowInfoWithNoAuth(senInfoFieldName, realInfShadowInfo.toString());
            } else {
                // realInfShadowInfo is realInfo
                realInfo = PrivacyGlobalValue.get().isNeedAuth() ?
                        privacyHandle.getShadowInfoByRealInfoWithAuth(senInfoFieldName, realInfShadowInfo.toString()) :
                        privacyHandle.getShadowInfoByRealInfoWithNoAuth(senInfoFieldName, realInfShadowInfo.toString());
            }
            if (realInfo != null) {
                if(setMethodName != null) {
                    getMethodName = setMethodName;
                }
                if(map != null) {
                    map.put(mapKey, realInfo);
                    o = setInfoOfObjectByAndSymbol(o, getMethodName, map);
                } else if(isBasic) {
                    o = realInfo;
                } else {
                    o = setInfoOfObjectByAndSymbol(o, getMethodName, realInfo);
                }
            }
        }
        return o;
    }

}
