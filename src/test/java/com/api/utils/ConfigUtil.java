package com.api.utils;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigUtil {

    private static final Map<String, Object> config = new ConcurrentHashMap<>();

    // 使用静态代码块，在类加载时只读取一次配置文件
    static {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = ConfigUtil.class
                    .getClassLoader()
                    .getResourceAsStream("application.yml");
            if (inputStream == null) {
                throw new IllegalStateException("Cannot find application.yml on the classpath");
            }
            Map<String, Object> loadedConfig = yaml.load(inputStream);
            if (loadedConfig != null) {
                config.putAll(loadedConfig);
            }
        } catch (Exception e) {
            // 在实际项目中，这里应该使用日志框架记录错误
            System.err.println("Failed to load application.yml");
            e.printStackTrace();
        }
    }

    /**
     * 根据点分割的 key 获取属性值，例如 "ehapi.db.lif.uat.driver"
     * @param key 属性键
     * @return 属性值，如果找不到则返回 null
     */
    @SuppressWarnings("unchecked")
    public static String getProperty(String key) {
        String[] parts = key.split("\\.");
        Map<String, Object> currentNode = config;
        for (int i = 0; i < parts.length; i++) {
            Object value = currentNode.get(parts[i]);
            if (value == null) {
                return null; // Key not found
            }
            if (i == parts.length - 1) {
                return value.toString();
            }
            if (value instanceof Map) {
                currentNode = (Map<String, Object>) value;
            } else {
                return null; // Path is not valid
            }
        }
        return null;
    }
}