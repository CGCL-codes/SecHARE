package org.example.protect.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class PrivacyHandleBase {

    public String convertFieldName(String fieldName) {
        fieldName = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
        if(fieldName.startsWith("_")) {
            fieldName = fieldName.substring(1);
        }
        return fieldName.contains("privacy_") ? fieldName : "privacy_" + fieldName;
    }

    public  <T>T mapToObject(Map<?, ?> map, Class<T> objectClass) throws Exception {
        Map<String, Object> mapTmp = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if(entry.getKey().toString().contains("_")){
                String[] arr = entry.getKey().toString().split("_");
                StringBuilder sb = new StringBuilder();
                sb.append(arr[0]);
                for (int i = 1; i < arr.length; i++){
                    arr[i]=arr[i].substring(0, 1).toUpperCase() + arr[i].substring(1);
                    sb.append(arr[i]);
                }
                String key = sb.toString();
                Object value = entry.getValue();
                mapTmp.put(key, value);
            }else {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                mapTmp.put(key, value);
            }
        }

        T obj = objectClass.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field field : fields){
            int mod = field.getModifiers();
            if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){
                continue;
            }
            field.setAccessible(true);
            field.set(obj, mapTmp.get(field.getName()));
        }
        return obj;
    }

}
