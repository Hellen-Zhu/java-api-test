package com.api.data.entities;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket test parameter entity
 * Extends TestAPIParameter to reuse common fields and add WebSocket-specific configuration
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestWebSocketParameter extends TestAPIParameter {

    // WebSocket specific fields
    private String wsUrl;
    private String protocol = "websocket";
    private Map<String, String> wsHeaders = new HashMap<>();
    private List<String> channels = new ArrayList<>();

    // Connection configuration
    private int connectionTimeout = 30000; // 30 seconds
    private int messageTimeout = 5000; // 5 seconds
    private int heartbeatInterval = 30000; // 30 seconds
    private int maxReconnectAttempts = 3;
    private boolean autoReconnect = false;

    // Message handling
    private JSONArray messages = new JSONArray(); // Array of message configurations
    private JSONObject heartbeatMessage;
    private JSONObject subscribeMessage;
    private JSONObject unsubscribeMessage;

    // Validation configuration
    private boolean validateMessageOrder = false;
    private boolean validateSchema = false;
    private JSONObject expectedSchema;
    private List<JSONObject> expectedMessages = new ArrayList<>();

    // Connection pool testing
    private int connectionCount = 1; // Number of connections to create
    private int maxConnections = 10; // Maximum allowed connections (for limit testing)
    private boolean testConnectionLimit = false;

    // Performance metrics
    private boolean collectMetrics = false;
    private long maxLatency = 1000; // Maximum acceptable latency in ms
    private long minThroughput = 100; // Minimum messages per second

    /**
     * Get message by type (send/expect)
     */
    public List<JSONObject> getMessagesByType(String type) {
        List<JSONObject> filteredMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            JSONObject msg = messages.getJSONObject(i);
            if (type.equals(msg.getString("type"))) {
                filteredMessages.add(msg);
            }
        }
        return filteredMessages;
    }

    /**
     * Get all send messages
     */
    public List<JSONObject> getSendMessages() {
        return getMessagesByType("send");
    }

    /**
     * Get all expected messages
     */
    public List<JSONObject> getExpectedMessages() {
        return getMessagesByType("expect");
    }

    /**
     * Add a message to send
     */
    public void addSendMessage(JSONObject content) {
        JSONObject message = new JSONObject();
        message.put("type", "send");
        message.put("content", content);
        messages.add(message);
    }

    /**
     * Add an expected message
     */
    public void addExpectedMessage(JSONObject content, long timeout) {
        JSONObject message = new JSONObject();
        message.put("type", "expect");
        message.put("content", content);
        message.put("timeout", timeout);
        messages.add(message);
    }

    /**
     * Parse and set WebSocket configuration from parameter JSON
     */
    public void parseWebSocketConfig(JSONObject parameterJson) {
        if (parameterJson == null) {
            return;
        }

        // Parse WebSocket URL
        if (parameterJson.containsKey("wsUrl")) {
            this.wsUrl = parameterJson.getString("wsUrl");
        }

        // Parse headers
        if (parameterJson.containsKey("wsHeaders")) {
            JSONObject headers = parameterJson.getJSONObject("wsHeaders");
            headers.forEach((key, value) -> this.wsHeaders.put(key, String.valueOf(value)));
        }

        // Parse channels
        if (parameterJson.containsKey("channels")) {
            JSONArray channelArray = parameterJson.getJSONArray("channels");
            for (int i = 0; i < channelArray.size(); i++) {
                this.channels.add(channelArray.getString(i));
            }
        }

        // Parse messages
        if (parameterJson.containsKey("messages")) {
            this.messages = parameterJson.getJSONArray("messages");
        }

        // Parse heartbeat configuration
        if (parameterJson.containsKey("heartbeat")) {
            JSONObject heartbeat = parameterJson.getJSONObject("heartbeat");
            this.heartbeatMessage = heartbeat.getJSONObject("message");
            if (heartbeat.containsKey("interval")) {
                this.heartbeatInterval = heartbeat.getIntValue("interval");
            }
        }

        // Parse connection configuration
        if (parameterJson.containsKey("connection")) {
            JSONObject connection = parameterJson.getJSONObject("connection");
            if (connection.containsKey("timeout")) {
                this.connectionTimeout = connection.getIntValue("timeout");
            }
            if (connection.containsKey("autoReconnect")) {
                this.autoReconnect = connection.getBooleanValue("autoReconnect");
            }
            if (connection.containsKey("maxReconnectAttempts")) {
                this.maxReconnectAttempts = connection.getIntValue("maxReconnectAttempts");
            }
        }

        // Parse validation configuration
        if (parameterJson.containsKey("validation")) {
            JSONObject validation = parameterJson.getJSONObject("validation");
            if (validation.containsKey("messageOrder")) {
                this.validateMessageOrder = validation.getBooleanValue("messageOrder");
            }
            if (validation.containsKey("schemaValidation")) {
                this.validateSchema = validation.getBooleanValue("schemaValidation");
            }
            if (validation.containsKey("schema")) {
                this.expectedSchema = validation.getJSONObject("schema");
            }
        }

        // Parse connection limit testing
        if (parameterJson.containsKey("connectionTest")) {
            JSONObject connTest = parameterJson.getJSONObject("connectionTest");
            if (connTest.containsKey("connectionCount")) {
                this.connectionCount = connTest.getIntValue("connectionCount");
            }
            if (connTest.containsKey("maxConnections")) {
                this.maxConnections = connTest.getIntValue("maxConnections");
            }
            if (connTest.containsKey("testLimit")) {
                this.testConnectionLimit = connTest.getBooleanValue("testLimit");
            }
        }

        // Parse performance metrics
        if (parameterJson.containsKey("metrics")) {
            JSONObject metrics = parameterJson.getJSONObject("metrics");
            this.collectMetrics = true;
            if (metrics.containsKey("maxLatency")) {
                this.maxLatency = metrics.getLongValue("maxLatency");
            }
            if (metrics.containsKey("minThroughput")) {
                this.minThroughput = metrics.getLongValue("minThroughput");
            }
        }
    }

    @Override
    public TestWebSocketParameter clone() {
        return (TestWebSocketParameter) super.clone();
    }
}