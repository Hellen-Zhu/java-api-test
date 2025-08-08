package com.api.utils;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigUtil {

    private static final Map<String, Object> config = new ConcurrentHashMap<>();

    // Use a static block to load configuration once when the class is loaded
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
            // In production, prefer logging framework for errors
            System.err.println("Failed to load application.yml");
            e.printStackTrace();
        }
    }

    /**
     * Get property value by dot-separated key, e.g. "ehapi.db.lif.uat.driver"
     * @param key property key
     * @return property value, or null if not found
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