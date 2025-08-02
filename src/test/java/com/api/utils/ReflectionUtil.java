package com.api.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
// Logger and JexlEngine imports would be required here
import org.apache.commons.jexl3.*;
import java.util.Map;

import org.testng.log4testng.Logger;

public class ReflectionUtil {
     private static final Logger logger = Logger.getLogger(ReflectionUtil.class);

    public static Object fetchValueByCodeExpression(String jexlExp, Map<String, Object> map) {
        JexlEngine jexl = new JexlBuilder().create();
        JexlExpression e = jexl.createExpression(jexlExp);
        JexlContext jc = new MapContext();
        for (String key : map.keySet()) {
            jc.set(key, map.get(key));
        }

        if (null == e.evaluate(jc)) {
            return "";
        }
        return e.evaluate(jc);
    }

    public static Class<?> fetchClassByClassName(String className) {
        Class<?> tclass = null;
        try {
            tclass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.warn("Warn " + className + " does not exist!");
        }
        return tclass;
    }

    public static Object fetchMethodInvokeBaseOnClass(Class<?> clazz, String methodName, Class<?> paramClazz, Object paramObject) {
        Object methodInvoke = null;
        try {
            Method method = clazz.getMethod(methodName, paramClazz);
            methodInvoke = method.invoke(null, paramObject);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return methodInvoke;
    }
}