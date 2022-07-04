package org.example.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class ClassMethodUtil {

    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }

    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }

    public static String connectMethodStr(String fieldName, String getSet) {
        return getSet + ClassMethodUtil.toUpperCaseFirstOne(fieldName);
    }

    public static Class<?> getClassByName(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static Object setValueOfObject(Object o, String methodName, Object... args) {
        Class<?> objClass = o.getClass();
        Method objMethod = null;
        for (Method method : objClass.getMethods()) {
            if (methodName.equals(method.getName())) {
                objMethod = method;
                break;
            }
        }
        if (objMethod == null) {
            return false;
        }

        return callObjectMethod(o, objMethod, args);

    }

    public static Object callObjectMethod(Object o, Method method, Object[] args){
        try {
            if (isBasicType(o)) {
                return false;
            }
            method.setAccessible(true);
            return method.invoke(o, args);
        } catch (Exception e) {
            log.error("[PrivacyTool] Call Object Method error: {}", e.getMessage());
            return false;
        }
    }

    public static Object callObjectMethod(Object o, String methodName) {
        try {
            if (isBasicType(o)) {
                return false;
            }
            Method method = getDeclaredMethodWithParent(o.getClass(), methodName);
            method.setAccessible(true);
            return method.invoke(o);
        } catch (Exception e) {
            return false;
        }
    }

    public static Object callObjectMethod(Object o, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method method = getDeclaredMethodWithParent(o.getClass(), methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(o, args);
        } catch (Exception e) {
            log.error("[PrivacyTool] Call Object Method error: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     *  solve extends problem
     */
    public static Method getDeclaredMethodWithParent(Class<?> targetClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        boolean doneFlag = false;
        Class<?> curClass = targetClass;
        Method declaredMethod = null;
        while (!doneFlag && curClass != Object.class){
            try {
                declaredMethod = curClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                doneFlag = false;
                curClass = curClass.getSuperclass();
                continue;
            }
            doneFlag = true;
        }
        if(declaredMethod == null){
            throw new NoSuchMethodException(targetClass.getName() + "." + methodName + Arrays.toString(parameterTypes));
        }
        return declaredMethod;

    }

    public static boolean isBasicType (Object o) {
        return
        o.getClass().isAssignableFrom(String.class) ||
                o.getClass().isAssignableFrom(Integer.class) ||
                o.getClass().isAssignableFrom(Long.class) ||
                o.getClass().isAssignableFrom(Character.class) ||
                o.getClass().isAssignableFrom(Double.class);
    }

}
