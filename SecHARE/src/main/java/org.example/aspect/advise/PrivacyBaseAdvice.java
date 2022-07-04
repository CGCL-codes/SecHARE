package org.example.aspect.advise;

import org.example.aspect.entity.ObjectInfoEntity;
import org.example.aspect.entity.PrivacyElemEntity;
import org.example.global.PrivacyGlobalValue;
import org.example.protect.PrivacyHandle;
import org.example.protect.impl.PrivacyHandleMongoImpl;
import org.example.protect.impl.PrivacyHandleSqlImpl;
import org.example.util.ClassMethodUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PrivacyBaseAdvice {

    public final static String DEFAULT_USER_ID = "default_user_id";
    public final static String DEFAULT_DEVICE_ID = "default_device_id";

    /**
     *  gain db
     *
     * @return PrivacyHandle
     */
    public static PrivacyHandle getPrivacyHandle() {
        switch (PrivacyGlobalValue.get().getDbType()) {
            case MONGODB:
                return new PrivacyHandleMongoImpl();
            case CASSANDRA:
                return null;
            default:
                return new PrivacyHandleSqlImpl();
        }
    }

    public static String getUserId() {
        String userId = null;
        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object userInfo = authentication.getPrincipal();
            userId = (userInfo == null) ? null : obj2String(getInfoOfObject(userInfo, DEFAULT_USER_ID));
        }
        return userId;
    }

    public static String mapHandle(Object realInfo, String fieldName) {
        // Map handle
        if(fieldName.contains("_")) {
            String[] mapGetMethod = fieldName.split("_");
            String mapKey = mapGetMethod[1];
            Map<?, ?> map = (Map<?, ?>) realInfo;
            return (String) map.getOrDefault(mapKey, null);
        }
        return realInfo == null ? null : realInfo.toString();
    }

    public static boolean isNumeric(String str){
        String numericRegx = "[0-9]*";
        Pattern pattern = Pattern.compile(numericRegx);
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static String obj2String(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public static String getBracketsContent(String content) {
        String regex = "(?<=\\()[^\\)]+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    public static boolean checkMethodName(String methodName, String packageAndClassName) {
        // methodName.contains(packageAndClassName) || packageAndClassName.contains(methodName)
        return methodName.toLowerCase(Locale.ROOT).equals(packageAndClassName.toLowerCase(Locale.ROOT));
    }

    public static ArrayList<Object> checkObjectArray(Object obj) {
        ArrayList<Object> objArrayList = new ArrayList<>();
        Class<?> objClass = obj.getClass();
        if (objClass.isArray()){
            for (int i = 0; i < Array.getLength(obj); i++) {
                objArrayList.add(Array.get(obj, i));
            }
        }else if (Collection.class.isAssignableFrom(objClass)) {
            objArrayList.addAll((Collection<?>) obj);
        } else {
            return null;
        }
        return objArrayList;
    }

    public static ObjectInfoEntity getRealInfoOfObjectByAuto(Object o, PrivacyElemEntity sensitivePee) {
        String fieldName = sensitivePee.getFieldInfo();
        String mapKey = null;
        if(fieldName.contains("_")) {
            String[] mapGetMethod = fieldName.split("_");
            fieldName = mapGetMethod[0];
            mapKey = mapGetMethod[1];
        }
        return getRealInfoOfObjectByAuto(o, fieldName, sensitivePee.getMethodOrClassPath(), mapKey);
    }

    public static ObjectInfoEntity getRealInfoOfObjectByAuto(Object o, String fieldName, String fieldClassPath, String mapKey) {
        // Map handle
        String className = "";
        // reflect
        try {
            Class<?> clz = ClassMethodUtil.getClassByName(fieldClassPath);
            if (clz != null) {
                className = clz.getSimpleName();
                if (clz.isInstance(o)) {
                    // handle Map List Set Array
                    Object realObj = ClassMethodUtil.callObjectMethod(o,
                            ClassMethodUtil.connectMethodStr(fieldName, "get"));
                    if (!realObj.equals(Boolean.FALSE)) {
                        if(mapKey != null) {
                            Map<?, ?> map = (Map<?, ?>) realObj;
                            Object realInfo = map.getOrDefault(mapKey, null);
                            if(realInfo != null) {
                                return new ObjectInfoEntity(realObj.toString(), fieldName);
                            }
                            return new ObjectInfoEntity();
                        }
                        return new ObjectInfoEntity(realObj.toString(), fieldName);
                    }
                }
            }
        } catch (Exception ignored) {}

        // Json handle
        JSONObject json;
        try {
            json = (JSONObject) JSONObject.toJSON(o);
        }catch (Exception e) {
            return new ObjectInfoEntity();
        }

        Set<String> attributeNameSet;
        // id handle -- id can hide other object
        if ("id".equals(fieldName)) {
            attributeNameSet = getSimilarlyAttributeArray(className, fieldName, false);

        } else {
            attributeNameSet = getSimilarlyAttributeArray(className, fieldName, true);
        }
        for (String attributeName : attributeNameSet) {
            attributeName = ClassMethodUtil.toLowerCaseFirstOne(attributeName);
            // current class
            if (json.containsKey(attributeName)) {
                Object resJson = json.get(attributeName);
                if(mapKey != null) {
                    Map<?, ?> map = (Map<?, ?>) resJson;
                    Object realInfo = map.getOrDefault(mapKey, null);
                    return realInfo != null ? new ObjectInfoEntity(realInfo.toString(), fieldName) :
                            new ObjectInfoEntity();
                }
                return new ObjectInfoEntity(resJson.toString(), attributeName);
            }
            // parent class
            Object realObj = ClassMethodUtil.callObjectMethod(o,
                    ClassMethodUtil.connectMethodStr(attributeName, "get"));
            if (!realObj.equals(Boolean.FALSE)) {
                if(mapKey != null) {
                    Map<?, ?> map = (Map<?, ?>) realObj;
                    Object realInfo = map.getOrDefault(mapKey, null);
                    return realInfo != null ? new ObjectInfoEntity(realInfo.toString(), fieldName) :
                            new ObjectInfoEntity();
                }
                return new ObjectInfoEntity(realObj.toString(), attributeName);
            }
        }

        // attribute and similar fieldName
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            Object value = entry.getValue();

            // skip array
            if (value == null || checkObjectArray(value) != null || ClassMethodUtil.isBasicType(value)) {
                continue;
            }
            ObjectInfoEntity temp = getRealInfoOfObjectByAuto(value, fieldName, fieldClassPath, mapKey);
            if (temp.getInfo() == null) {
                continue;
            }
            return temp;
        }
        return new ObjectInfoEntity();
    }

    public static Object getInfoOfListObject(Object o, String methodName) {
        List<Object> res = getInfoListOfObject(o, methodName);
        return res.isEmpty() ? null : res.get(0);
    }

    public static List<Object> getInfoListOfObject(Object o, String methodName) {
        List<Object> res = new ArrayList<>();
        if (StringUtils.isEmpty(methodName)) {
            return res;
        }
        // object is protectedInfo
        if (methodName.toLowerCase(Locale.ROOT).contains("object")) {
            res.add(o.toString());
            return res;
        }
        // returned obj
        // collection handle
        List<Object> objectList = checkObjectArray(o);
        if(objectList == null) {
            res.add(getInfoOfObject(o, methodName));
            return res;
        }
        for(Object obj : objectList) {
            res.add(getInfoOfObject(obj, methodName));
        }
        return res;
    }

    /**
     *  gain id in Object
     *  handle '& connected idInfo
     *
     * @param o Object
     * @param methodName methodName
     * @return id String
     */
    public static Object getInfoOfObject(Object o, String methodName) {
        Object idObject;
        if ("object".equals(methodName)) {
            return o;
        }
        if (ClassMethodUtil.isBasicType(o)) {
            return null;
        }
        if (DEFAULT_DEVICE_ID.equals(methodName)) {
            idObject = ClassMethodUtil.callObjectMethod(o, "getDeviceId");
            if (idObject.equals(Boolean.FALSE)) {
                idObject = ClassMethodUtil.callObjectMethod(o, "getId");
            }
        } else if (DEFAULT_USER_ID.equals(methodName)) {
            idObject = ClassMethodUtil.callObjectMethod(o, "getUserId");
            if (idObject.equals(Boolean.FALSE)) {
                idObject = ClassMethodUtil.callObjectMethod(o, "getCustomerId");
            }
            if (idObject.equals(Boolean.FALSE)) {
                idObject = ClassMethodUtil.callObjectMethod(o, "getId");
            }
        } else {
            idObject = getInfoOfObjectByAndSymbol(o, methodName);
        }

        return idObject.equals(Boolean.FALSE) ? null : idObject;
    }

    public static Object getInfoOfObjectByAndSymbol(Object o, String methodName) {
        String andSymbol = "&";
        if (methodName.contains(andSymbol)) {
            String[] methodNameArray = methodName.split(andSymbol);
            Object obj = o;
            for (String mName : methodNameArray) {
                obj = ClassMethodUtil.callObjectMethod(obj, mName);
            }
            return obj;
        } else {
            return ClassMethodUtil.callObjectMethod(o, methodName);
        }
    }

    public static Object setInfoOfObjectByAndSymbol(Object o, String methodName, Object info) {
        String andSymbol = "&";
        if (methodName.contains(andSymbol)) {
            String[] methodNameArray = methodName.split(andSymbol);
            Object getObj = getInfoOfObjectByAndSymbol(o, methodNameArray[0]);
            return setInfoOfObjectByAndSymbol(getObj, methodName.replace(methodNameArray[0] + andSymbol, ""), info);
        } else {
            return ClassMethodUtil.setValueOfObject(o, methodName.replace("get", "set"), info);
        }
    }

    private static Set<String> getSimilarlyAttributeArray(String className, String fieldName, boolean isAll) {
        fieldName = ClassMethodUtil.toUpperCaseFirstOne(fieldName);
        Set<String> attributeNameSet = new HashSet<>();
        attributeNameSet.add(className + fieldName);
        String[] classNameArray = className.replaceAll("[A-Z]", " $0").split(" ");
        StringBuilder tempName = new StringBuilder();

        if (isAll) {
            attributeNameSet.add(fieldName);
        }
        for (int i = 0; i < classNameArray.length - 2; i++) {
            if (StringUtils.isEmpty(classNameArray[i])) {
                continue;
            }
            tempName.append(classNameArray[i]);

            attributeNameSet.add(className.replace(tempName.toString(), "") + fieldName);
            if (isAll) {
                attributeNameSet.add(tempName + fieldName);
            }
        }
        return attributeNameSet;
    }

}
