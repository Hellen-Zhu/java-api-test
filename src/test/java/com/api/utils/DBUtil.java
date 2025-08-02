package com.api.utils;

import com.api.helpers.DynamicDataSourceContextHolder;
import com.api.enums.DBEnum;
import com.api.enums.DSEnum;

import org.apache.ibatis.session.SqlSessionManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBUtil {
    private static Class<?> threadClazz;

    static {
        try {
            threadClazz = Class.forName("com.api.helpers.SessionFactory");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // #region Version 2.0
    public static String getProfileName(DBEnum type, String profileName) {
        return type.name().equalsIgnoreCase("drms") ? profileName + "_drms" :
                (type.name().equalsIgnoreCase("ignite") ? profileName + "_ignite" : "");
    }

    public static Object doSqlSessionByEnvironment(String environment, String statement, Map parameter) {
        Object result;
        try {
            String methodName = "getInstance_" + environment;
            Method method = threadClazz.getMethod(methodName);
            SqlSessionManager sqlSessionManager = (SqlSessionManager) method.invoke(null);
            sqlSessionManager.openSession();
            result = sqlSessionManager.selectList(statement, parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return ((List<Object>) result).size() == 0 ? new ArrayList<>() : result;
    }
    public static Object executeSql(DSEnum DSEnum, String statement, Map parameter) {
        Object result;
        try {
            Method method = threadClazz.getMethod(DSEnum.getSqlSessionManager());
            SqlSessionManager sqlSessionManager = (SqlSessionManager) method.invoke(null);
            sqlSessionManager.openSession();
            result = sqlSessionManager.selectList(statement, parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return ((List<Object>) result).size() == 0 ? new ArrayList<>() : result;
    }

    public static void switchJpaDB(DSEnum DSEnum) {
        DynamicDataSourceContextHolder.setDataSourceContext(DSEnum);
    }

    public static void clearJpaDb() {
        DynamicDataSourceContextHolder.clearDataSourceContext();
    }

    public static DSEnum getJpaDb() {
        return DynamicDataSourceContextHolder.getDataSourceContext();
    }
}