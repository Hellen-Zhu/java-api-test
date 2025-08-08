package com.api.helpers;

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
    
    // Add shutdown hook to ensure resources are released
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeAllSessions();
        }));
    }
    
    public static void closeAllSessions() {
        System.out.println("Starting database cleanup process...");
        if (SqlSessionManager_postgresql_lif != null) {
            try {
                if (SqlSessionManager_postgresql_lif.isManagedSessionStarted()) {
                    SqlSessionManager_postgresql_lif.close();
                    System.out.println("Active database session closed successfully");
                } else {
                    System.out.println("No active database sessions to close");
                }
                
                // Force clearing resource references
                try {
                    if (SqlSessionManager_postgresql_lif.getConfiguration().getEnvironment().getDataSource() instanceof AutoCloseable) {
                        ((AutoCloseable) SqlSessionManager_postgresql_lif.getConfiguration().getEnvironment().getDataSource()).close();
                        System.out.println("DataSource closed successfully");
                    }
                } catch (Exception dsEx) {
                    System.err.println("Warning: Failed to close DataSource: " + dsEx.getMessage());
                }
                
            } catch (Exception e) {
                System.err.println("Failed to close database connections: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Ensure thread-local resources are cleaned
                try {
                    SqlSessionManager_postgresql_lif = null;
                    System.out.println("SessionFactory cleanup completed");
                } catch (Exception e) {
                    System.err.println("Warning during final cleanup: " + e.getMessage());
                }
            }
        } else {
            System.out.println("No SessionFactory instances to clean up");
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
        // Note: we read plain password from yml now; RSA decryption is not needed
        properties.setProperty("password", ConfigUtil.getProperty(prefix + "password"));

        // Validate that critical properties are not null
        if (properties.getProperty("url") == null) {
            throw new IllegalStateException("Database URL is null for prefix: " + prefix);
        }

        return properties;
    }
}