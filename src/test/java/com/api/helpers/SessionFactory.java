package com.api.helpers;

//import citi.equities.lifecycleqa.common.utils.RSAUtil;
import com.api.utils.ConfigUtil;


import lombok.SneakyThrows;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * note:
 * 1. one static object only can be set once in singleton or static factory pattern
 */
public class SessionFactory {
    private static SqlSessionManager SqlSessionManager_postgresql_lif;
    
    // 添加关闭钩子来确保资源释放
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeAllSessions();
        }));
    }
    
    public static void closeAllSessions() {
        if (SqlSessionManager_postgresql_lif != null) {
            try {
                if (SqlSessionManager_postgresql_lif.isManagedSessionStarted()) {
                    SqlSessionManager_postgresql_lif.close();
                    System.out.println("Database connections closed successfully");
                } else {
                    System.out.println("No active database sessions to close");
                }
            } catch (Exception e) {
                System.err.println("Failed to close database connections: " + e.getMessage());
            }
        }
    }

    @SneakyThrows
    public static synchronized SqlSessionManager getInstance_postgresql_lif() {
        if (SqlSessionManager_postgresql_lif == null) {
            SqlSessionManager_postgresql_lif = getSqlSessionManager(getProperties("lif", "uat"));
        }
        return SqlSessionManager_postgresql_lif;
    }

    private static SqlSessionManager getSqlSessionManager(Properties properties) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream("mybatis.xml");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);
            return SqlSessionManager.newInstance(sqlSessionFactory);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("Failed to close mybatis.xml input stream: " + e.getMessage());
                }
            }
        }
    }


    private static Properties getProperties(String dbName, String region) {
        Properties properties = new Properties();
        String prefix = "ehapi.db." + dbName + "." + region + ".";

        properties.setProperty("driver", ConfigUtil.getProperty(prefix + "driver"));
        properties.setProperty("url", ConfigUtil.getProperty(prefix + "url"));
        properties.setProperty("username", ConfigUtil.getProperty(prefix + "username"));
        // 注意：我们现在直接从 yml 读取明文密码，不再需要 RSA 解密
        properties.setProperty("password", ConfigUtil.getProperty(prefix + "password"));

        // 验证一下确保关键属性不为null
        if (properties.getProperty("url") == null) {
            throw new IllegalStateException("Database URL is null for prefix: " + prefix);
        }

        return properties;
    }
}