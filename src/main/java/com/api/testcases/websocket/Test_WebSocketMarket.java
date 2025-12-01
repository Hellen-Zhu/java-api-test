package com.api.testcases.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.api.common.utils.MemoryCacheUtil;
import com.api.common.utils.WebSocketResponseValidator;
import com.api.common.utils.WebSocketUtil.WebSocketConnection;
import com.api.data.entities.TestWebSocketParameter;
import com.api.testcases.BaseWebSocketTest;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * WebSocket market data test cases
 * Tests for wss://uat-stream.3ona.co/exchange/v1/market
 */
@Slf4j
public class Test_WebSocketMarket extends BaseWebSocketTest {

    private TestWebSocketParameter testParameter;
    private MemoryCacheUtil memoryCacheUtil = MemoryCacheUtil.getInstance();

    private Test_WebSocketMarket(TestWebSocketParameter testParameter) {
        this.testParameter = testParameter;
        this.wsParameter = testParameter;
    }

    public static Test_WebSocketMarket getInstance(TestWebSocketParameter testParameter) {
        return new Test_WebSocketMarket(testParameter);
    }

    /**
     * Test WebSocket heartbeat functionality
     */
    @Test(description = "Test WebSocket heartbeat")
    public void test_heartbeat(ITestContext testContext) {
        try {
            // Execute the WebSocket test based on configuration
            executeWebSocketTest(testContext);

            // Send heartbeat
            JSONObject heartbeat = new JSONObject();
            heartbeat.put("id", System.currentTimeMillis());
            heartbeat.put("method", "public/heartbeat");
            heartbeat.put("code", 0);

            primaryConnection.sendJsonMessage(heartbeat);
            log.info("Heartbeat sent: {}", heartbeat.toJSONString());

            // Wait for heartbeat response
            String response = primaryConnection.waitForMessage(5000);
            Assert.assertNotNull(response, "No heartbeat response received");

            // Validate heartbeat response
            WebSocketResponseValidator.validateHeartbeatResponse(response);
            log.info("Heartbeat test passed");

        } catch (Exception e) {
            log.error("Heartbeat test failed: {}", e.getMessage(), e);
            Assert.fail("Heartbeat test failed: " + e.getMessage());
        }
    }

    /**
     * Test user.order channel subscription
     */
    @Test(description = "Test user.order channel subscription")
    public void test_user_order_subscription(ITestContext testContext) {
        try {
            executeWebSocketTest(testContext);

            // Subscribe to user.order channel
            JSONObject subscribeMsg = new JSONObject();
            subscribeMsg.put("id", 1);
            subscribeMsg.put("method", "subscribe");

            JSONObject params = new JSONObject();
            params.put("channels", new String[]{"user.order"});
            subscribeMsg.put("params", params);
            subscribeMsg.put("nonce", System.currentTimeMillis());

            CompletableFuture<JSONObject> future = primaryConnection.sendRequestAndWaitForResponse(
                subscribeMsg,
                wsParameter.getMessageTimeout()
            );

            JSONObject response = future.get();
            Assert.assertNotNull(response, "No subscription response received");

            // Validate subscription response
            List<String> expectedChannels = List.of("user.order");
            WebSocketResponseValidator.validateSubscriptionResponse(response.toJSONString(), expectedChannels);

            log.info("User order subscription test passed");

            // Wait for order updates
            List<String> messages = primaryConnection.waitForMessages(3, 10000);
            log.info("Received {} order update messages", messages.size());

        } catch (Exception e) {
            log.error("User order subscription test failed: {}", e.getMessage(), e);
            Assert.fail("User order subscription test failed: " + e.getMessage());
        }
    }

    /**
     * Test user.balance channel subscription
     */
    @Test(description = "Test user.balance channel subscription")
    public void test_user_balance_subscription(ITestContext testContext) {
        try {
            executeWebSocketTest(testContext);

            // Subscribe to user.balance channel
            JSONObject subscribeMsg = new JSONObject();
            subscribeMsg.put("id", 1);
            subscribeMsg.put("method", "subscribe");

            JSONObject params = new JSONObject();
            params.put("channels", new String[]{"user.balance"});
            subscribeMsg.put("params", params);

            CompletableFuture<JSONObject> future = primaryConnection.sendRequestAndWaitForResponse(
                subscribeMsg,
                wsParameter.getMessageTimeout()
            );

            JSONObject response = future.get();
            Assert.assertNotNull(response, "No subscription response received");

            // Validate subscription response
            List<String> expectedChannels = List.of("user.balance");
            WebSocketResponseValidator.validateSubscriptionResponse(response.toJSONString(), expectedChannels);

            log.info("User balance subscription test passed");

            // Store subscription status in cache for other tests
            memoryCacheUtil.put("ws_balance_subscribed", true);

        } catch (Exception e) {
            log.error("User balance subscription test failed: {}", e.getMessage(), e);
            Assert.fail("User balance subscription test failed: " + e.getMessage());
        }
    }

    /**
     * Test multiple channel subscriptions
     */
    @Test(description = "Test multiple channel subscriptions")
    public void test_multiple_channel_subscription(ITestContext testContext) {
        try {
            executeWebSocketTest(testContext);

            // Subscribe to multiple channels
            JSONObject subscribeMsg = new JSONObject();
            subscribeMsg.put("id", System.currentTimeMillis());
            subscribeMsg.put("method", "subscribe");

            JSONObject params = new JSONObject();
            params.put("channels", new String[]{"user.order", "user.balance"});
            subscribeMsg.put("params", params);

            primaryConnection.sendJsonMessage(subscribeMsg);
            log.info("Multi-channel subscription request sent");

            // Wait for subscription confirmations
            List<String> responses = primaryConnection.waitForMessages(2, 10000);
            Assert.assertTrue(responses.size() >= 1, "No subscription confirmations received");

            log.info("Multiple channel subscription test passed with {} confirmations", responses.size());

        } catch (Exception e) {
            log.error("Multiple channel subscription test failed: {}", e.getMessage(), e);
            Assert.fail("Multiple channel subscription test failed: " + e.getMessage());
        }
    }

    /**
     * Test WebSocket connection limit (max 10 connections)
     */
    @Test(description = "Test WebSocket connection limit")
    public void test_connection_limit(ITestContext testContext) {
        try {
            // Set up test for connection limit
            wsParameter.setTestConnectionLimit(true);
            wsParameter.setMaxConnections(10);

            executeWebSocketTest(testContext);

            // Results are validated in the base class testConnectionLimit() method
            log.info("Connection limit test completed successfully");

        } catch (Exception e) {
            log.error("Connection limit test failed: {}", e.getMessage(), e);
            Assert.fail("Connection limit test failed: " + e.getMessage());
        }
    }

    /**
     * Test WebSocket performance with metrics collection
     */
    @Test(description = "Test WebSocket performance")
    public void test_performance(ITestContext testContext) {
        try {
            // Enable metrics collection
            wsParameter.setCollectMetrics(true);
            wsParameter.setMaxLatency(1000);
            wsParameter.setMinThroughput(10);

            executeWebSocketTest(testContext);

            // Subscribe to high-frequency channel
            CompletableFuture<JSONObject> future = subscribeToChannel("user.order");
            future.get();

            // Receive multiple messages to test throughput
            List<String> messages = primaryConnection.waitForMessages(20, 30000);
            log.info("Received {} messages for performance test", messages.size());

            // Metrics validation happens in afterWebSocketMethod()
            log.info("Performance test completed");

        } catch (Exception e) {
            log.error("Performance test failed: {}", e.getMessage(), e);
            Assert.fail("Performance test failed: " + e.getMessage());
        }
    }

    /**
     * Test WebSocket reconnection
     */
    @Test(description = "Test WebSocket reconnection")
    public void test_reconnection(ITestContext testContext) {
        try {
            wsParameter.setAutoReconnect(true);
            wsParameter.setMaxReconnectAttempts(3);

            executeWebSocketTest(testContext);

            // Initial connection should be established
            Assert.assertTrue(primaryConnection.isConnected(), "Initial connection failed");

            String connectionId = primaryConnection.getConnectionId();
            log.info("Initial connection established: {}", connectionId);

            // Force disconnect
            primaryConnection.close();
            Thread.sleep(1000);

            // Try to reconnect
            WebSocketConnection newConnection = connectWebSocket(wsParameter);
            Assert.assertTrue(newConnection.isConnected(), "Reconnection failed");

            log.info("Reconnection test passed");

        } catch (Exception e) {
            log.error("Reconnection test failed: {}", e.getMessage(), e);
            Assert.fail("Reconnection test failed: " + e.getMessage());
        }
    }

    /**
     * Test message sequence validation
     */
    @Test(description = "Test message sequence validation")
    public void test_message_sequence(ITestContext testContext) {
        try {
            wsParameter.setValidateMessageOrder(true);

            executeWebSocketTest(testContext);

            // Send multiple requests in sequence
            for (int i = 1; i <= 3; i++) {
                JSONObject heartbeat = new JSONObject();
                heartbeat.put("id", i);
                heartbeat.put("method", "public/heartbeat");
                heartbeat.put("code", 0);
                primaryConnection.sendJsonMessage(heartbeat);
                Thread.sleep(100);
            }

            // Receive and validate sequence
            List<String> messages = primaryConnection.waitForMessages(3, 5000);

            // Verify message order
            for (int i = 0; i < messages.size(); i++) {
                JSONObject msg = JSONObject.parseObject(messages.get(i));
                Assert.assertEquals(msg.getIntValue("id"), i + 1,
                    "Message sequence out of order");
            }

            log.info("Message sequence validation test passed");

        } catch (Exception e) {
            log.error("Message sequence test failed: {}", e.getMessage(), e);
            Assert.fail("Message sequence test failed: " + e.getMessage());
        }
    }
}