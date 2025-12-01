package com.api.testcases;

import com.alibaba.fastjson2.JSONObject;
import com.api.common.utils.WebSocketResponseValidator;
import com.api.common.utils.WebSocketUtil;
import com.api.common.utils.WebSocketUtil.WebSocketConnection;
import com.api.data.entities.TestAPIParameter;
import com.api.data.entities.TestWebSocketParameter;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for WebSocket tests
 * Provides common functionality for WebSocket connection management and testing
 */
@Slf4j
public abstract class BaseWebSocketTest extends BaseTest {

    protected WebSocketConnection primaryConnection;
    protected List<WebSocketConnection> connections = new ArrayList<>();
    protected TestWebSocketParameter wsParameter;
    protected WebSocketResponseValidator.PerformanceMetrics metrics;
    protected TestAPIParameter testParameter;  // Add this field for compatibility with BaseTest

    @BeforeClass
    public void setupWebSocket() {
        log.info("Setting up WebSocket test environment");
        metrics = new WebSocketResponseValidator.PerformanceMetrics();
    }

    @BeforeMethod
    public void beforeWebSocketMethod() {
        // Initialize WebSocket parameter if test parameter is WebSocket type
        if (testParameter instanceof TestWebSocketParameter) {
            wsParameter = (TestWebSocketParameter) testParameter;
        }
    }

    /**
     * Connect to WebSocket server
     */
    protected WebSocketConnection connectWebSocket(TestWebSocketParameter param) throws Exception {
        log.info("Connecting to WebSocket: {}", param.getWsUrl());

        WebSocketConnection connection = WebSocketUtil.createConnection(
            param.getWsUrl(),
            param.getWsHeaders()
        );

        connections.add(connection);

        // Start heartbeat if configured
        if (param.getHeartbeatMessage() != null) {
            connection.startHeartbeat(param.getHeartbeatMessage(), param.getHeartbeatInterval());
            log.info("Heartbeat started with interval: {}ms", param.getHeartbeatInterval());
        }

        // Set as primary connection if it's the first one
        if (primaryConnection == null) {
            primaryConnection = connection;
        }

        return connection;
    }

    /**
     * Connect multiple WebSocket connections
     */
    protected List<WebSocketConnection> connectMultipleWebSockets(TestWebSocketParameter param, int count) throws Exception {
        log.info("Creating {} WebSocket connections", count);

        List<WebSocketConnection> newConnections = WebSocketUtil.createConnections(
            count,
            param.getWsUrl(),
            param.getWsHeaders()
        );

        connections.addAll(newConnections);

        // Set primary connection
        if (primaryConnection == null && !newConnections.isEmpty()) {
            primaryConnection = newConnections.get(0);
        }

        return newConnections;
    }

    /**
     * Execute WebSocket test based on configuration
     */
    protected void executeWebSocketTest(ITestContext testContext) throws Exception {
        // Parse WebSocket configuration
        if (wsParameter.getParameter() != null) {
            JSONObject paramJson = JSONObject.parseObject(wsParameter.getParameter());
            wsParameter.parseWebSocketConfig(paramJson);
        }

        // Test connection limit if configured
        if (wsParameter.isTestConnectionLimit()) {
            testConnectionLimit();
            return;
        }

        // Create connections
        if (wsParameter.getConnectionCount() > 1) {
            connectMultipleWebSockets(wsParameter, wsParameter.getConnectionCount());
        } else {
            connectWebSocket(wsParameter);
        }

        // Process messages
        processMessages();
    }

    /**
     * Process send and expect messages
     */
    protected void processMessages() throws Exception {
        for (int i = 0; i < wsParameter.getMessages().size(); i++) {
            JSONObject message = wsParameter.getMessages().getJSONObject(i);
            String type = message.getString("type");

            if ("send".equals(type)) {
                processSendMessage(message);
            } else if ("expect".equals(type)) {
                processExpectMessage(message);
            } else if ("subscribe".equals(type)) {
                processSubscribe(message);
            } else if ("unsubscribe".equals(type)) {
                processUnsubscribe(message);
            }
        }
    }

    /**
     * Process send message
     */
    protected void processSendMessage(JSONObject message) {
        JSONObject content = message.getJSONObject("content");
        String connectionId = message.getString("connectionId");

        WebSocketConnection conn = connectionId != null ?
            WebSocketUtil.getConnection(connectionId) : primaryConnection;

        if (conn != null) {
            long startTime = System.currentTimeMillis();
            conn.sendJsonMessage(content);

            if (wsParameter.isCollectMetrics()) {
                long latency = System.currentTimeMillis() - startTime;
                metrics.recordMessage(latency);
            }

            log.info("Sent message: {}", content.toJSONString());
        }
    }

    /**
     * Process expect message
     */
    protected void processExpectMessage(JSONObject message) throws Exception {
        JSONObject expectedContent = message.getJSONObject("content");
        Long timeout = message.getLong("timeout");
        if (timeout == null) {
            timeout = (long) wsParameter.getMessageTimeout();
        }

        String connectionId = message.getString("connectionId");
        WebSocketConnection conn = connectionId != null ?
            WebSocketUtil.getConnection(connectionId) : primaryConnection;

        if (conn != null) {
            String actualMessage = conn.waitForMessage(timeout);
            if (actualMessage == null) {
                throw new AssertionError("No message received within timeout: " + timeout + "ms");
            }

            // Validate message
            if (message.containsKey("validate")) {
                JSONObject validation = message.getJSONObject("validate");
                JSONObject actualJson = JSONObject.parseObject(actualMessage);
                WebSocketResponseValidator.validateJsonContent(actualJson, validation);
            } else if (expectedContent != null) {
                JSONObject actualJson = JSONObject.parseObject(actualMessage);
                WebSocketResponseValidator.validateJsonContent(actualJson, expectedContent);
            }

            log.info("Expected message received and validated");
        }
    }

    /**
     * Process subscribe message
     */
    protected void processSubscribe(JSONObject message) throws Exception {
        JSONObject content = message.getJSONObject("content");
        List<String> channels = new ArrayList<>();

        if (content.containsKey("params")) {
            JSONObject params = content.getJSONObject("params");
            if (params.containsKey("channels")) {
                params.getJSONArray("channels").forEach(ch -> channels.add(String.valueOf(ch)));
            }
        }

        // Send subscription request
        primaryConnection.sendJsonMessage(content);
        log.info("Subscription request sent for channels: {}", channels);

        // Wait for subscription confirmation
        if (message.containsKey("expectConfirmation") && message.getBoolean("expectConfirmation")) {
            String response = primaryConnection.waitForMessage(wsParameter.getMessageTimeout());
            WebSocketResponseValidator.validateSubscriptionResponse(response, channels);
        }
    }

    /**
     * Process unsubscribe message
     */
    protected void processUnsubscribe(JSONObject message) {
        JSONObject content = message.getJSONObject("content");
        primaryConnection.sendJsonMessage(content);
        log.info("Unsubscribe request sent");
    }

    /**
     * Test connection limit
     */
    protected void testConnectionLimit() {
        log.info("Testing connection limit: max={}", wsParameter.getMaxConnections());

        Map<String, Object> results = WebSocketUtil.testConnectionLimit(
            wsParameter.getWsUrl(),
            wsParameter.getMaxConnections(),
            wsParameter.getWsHeaders()
        );

        WebSocketResponseValidator.validateConnectionLimit(results, wsParameter.getMaxConnections());
    }

    /**
     * Subscribe to channels
     */
    protected CompletableFuture<JSONObject> subscribeToChannel(String channel) throws Exception {
        JSONObject subscribeMessage = new JSONObject();
        subscribeMessage.put("id", System.currentTimeMillis());
        subscribeMessage.put("method", "subscribe");

        JSONObject params = new JSONObject();
        params.put("channels", new String[]{channel});
        subscribeMessage.put("params", params);

        return primaryConnection.sendRequestAndWaitForResponse(subscribeMessage, wsParameter.getMessageTimeout());
    }

    /**
     * Send heartbeat
     */
    protected void sendHeartbeat() {
        if (wsParameter.getHeartbeatMessage() != null) {
            primaryConnection.sendJsonMessage(wsParameter.getHeartbeatMessage());
            log.info("Heartbeat sent");
        }
    }

    /**
     * Wait for and validate messages
     */
    protected List<String> waitForMessages(int count, long timeout) throws Exception {
        List<String> messages = primaryConnection.waitForMessages(count, timeout);

        if (wsParameter.isValidateMessageOrder() || !wsParameter.getExpectedMessages().isEmpty()) {
            WebSocketResponseValidator.validateMessageSequence(wsParameter, messages);
        }

        return messages;
    }

    /**
     * Get connection statistics
     */
    protected Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalConnections", connections.size());
        stats.put("activeConnections", WebSocketUtil.getActiveConnectionCount());
        stats.put("messageHistory", primaryConnection != null ? primaryConnection.getMessageHistory() : new ArrayList<>());

        if (wsParameter.isCollectMetrics()) {
            stats.put("avgLatency", metrics.getAverageLatency());
            stats.put("maxLatency", metrics.getMaxLatency());
            stats.put("throughput", metrics.getThroughput());
        }

        return stats;
    }

    @AfterMethod
    public void afterWebSocketMethod() {
        // Validate performance metrics if enabled
        if (wsParameter != null && wsParameter.isCollectMetrics()) {
            try {
                metrics.validate(wsParameter);
            } catch (AssertionError e) {
                log.error("Performance validation failed: {}", e.getMessage());
                throw e;
            }
        }

        // Log connection statistics
        Map<String, Object> stats = getConnectionStats();
        log.info("WebSocket test statistics: {}", stats);
    }

    @AfterClass
    public void cleanupWebSocket() {
        log.info("Cleaning up WebSocket connections");

        // Close all connections
        for (WebSocketConnection conn : connections) {
            try {
                WebSocketUtil.closeConnection(conn.getConnectionId());
            } catch (Exception e) {
                log.warn("Failed to close connection {}: {}", conn.getConnectionId(), e.getMessage());
            }
        }

        connections.clear();
        primaryConnection = null;

        // Ensure all connections are closed
        WebSocketUtil.closeAllConnections();
    }
}