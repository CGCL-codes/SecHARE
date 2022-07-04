package org.example.util;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Modifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectAsmUtil {

    private static Map<Class<?>, MethodAccess> methodMap = new HashMap<>();

    private static Map<String, Integer> methodIndexMap = new HashMap<>();

    private static Map<Class<?>, List<String>> fieldMap = new HashMap<>();

    public static void copyProperties(Object desc, Object origin) {

        MethodAccess descMethodAccess = methodMap.get(desc.getClass());
        if (descMethodAccess == null) {
            descMethodAccess = cache(desc);
        }
        MethodAccess originMethodAccess = methodMap.get(origin.getClass());
        if (originMethodAccess == null) {
            originMethodAccess = cache(origin);
        }

        List<String> fieldList = fieldMap.get(origin.getClass());
        for (String field : fieldList) {
            String getKey = origin.getClass().getName() + "." + "get" + field;
            String setKey = desc.getClass().getName() + "." + "set" + field;
            Integer setIndex = methodIndexMap.get(setKey);
            if (setIndex != null) {
                int getIndex = methodIndexMap.get(getKey);
                descMethodAccess.invoke(desc, setIndex,
                        originMethodAccess.invoke(origin, getIndex));
            }
        }
    }


    private static MethodAccess cache(Object origin) {

        synchronized (origin.getClass()) {
            MethodAccess methodAccess = MethodAccess.get(origin.getClass());
            Field[] fields = origin.getClass().getDeclaredFields();
            List<String> fieldList = new ArrayList<>(fields.length);
            for (Field field : fields) {
                if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                    String fieldName = StringUtils.capitalize(field.getName());
                    int getIndex = methodAccess.getIndex("get" + fieldName);
                    int setIndex = methodAccess.getIndex("set" + fieldName);
                    methodIndexMap.put(origin.getClass().getName() + "." + "get" + fieldName, getIndex);
                    methodIndexMap.put(origin.getClass().getName() + "." + "set"  + fieldName, setIndex);
                    fieldList.add(fieldName);
                }
            }
            fieldMap.put(origin.getClass(), fieldList);
            methodMap.put(origin.getClass(), methodAccess);
            return methodAccess;
        }
    }
}
