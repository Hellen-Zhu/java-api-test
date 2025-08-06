package com.api.utils;

import com.api.helpers.DynamicDataSourceContextHolder;

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

    @SuppressWarnings("unchecked")
    public static Object doSqlSessionByEnvironment(String environment, String statement, Map<String, ?> parameter) {
        Object result;
        SqlSessionManager sqlSessionManager = null;
        boolean sessionOpened = false;
        try {
            String methodName = "getInstance_" + environment;
            Method method = threadClazz.getMethod(methodName);
            sqlSessionManager = (SqlSessionManager) method.invoke(null);
            if (!sqlSessionManager.isManagedSessionStarted()) {
                sqlSessionManager.openSession();
                sessionOpened = true;
            }
            result = sqlSessionManager.selectList(statement, parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (sqlSessionManager != null && sessionOpened) {
                try {
                    if (sqlSessionManager.isManagedSessionStarted()) {
                        sqlSessionManager.close();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to close SQL session: " + e.getMessage());
                }
            }
        }
        return ((List<Object>) result).size() == 0 ? new ArrayList<>() : result;
    }
    @SuppressWarnings("unchecked")
    public static Object executeSql(DSEnum DSEnum, String statement, Map<String, ?> parameter) {
        Object result;
        SqlSessionManager sqlSessionManager = null;
        boolean sessionOpened = false;
        try {
            Method method = threadClazz.getMethod(DSEnum.getSqlSessionManager());
            sqlSessionManager = (SqlSessionManager) method.invoke(null);
            if (!sqlSessionManager.isManagedSessionStarted()) {
                sqlSessionManager.openSession();
                sessionOpened = true;
            }
            result = sqlSessionManager.selectList(statement, parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (sqlSessionManager != null && sessionOpened) {
                try {
                    if (sqlSessionManager.isManagedSessionStarted()) {
                        sqlSessionManager.close();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to close SQL session: " + e.getMessage());
                }
            }
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