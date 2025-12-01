package com.api.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.api.data.entities.TestWebSocketParameter;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WebSocket response validation utility
 * Provides message validation, sequence validation, and performance metrics
 */
@Slf4j
public class WebSocketResponseValidator {

    /**
     * Validate a single WebSocket message
     */
    public static void validateMessage(TestWebSocketParameter testParam, String actualMessage) {
        try {
            JSONObject actualJson = JSON.parseObject(actualMessage);
            JSONObject expectedMessages = testParam.getExpectedMessages().get(0).getJSONObject("content");

            if (expectedMessages != null) {
                validateJsonContent(actualJson, expectedMessages);
            }

            // Validate against schema if enabled
            if (testParam.isValidateSchema() && testParam.getExpectedSchema() != null) {
                validateAgainstSchema(actualJson, testParam.getExpectedSchema());
            }

            log.info("WebSocket message validation passed - Test ID: {}", testParam.getId());

        } catch (Exception e) {
            log.error("WebSocket message validation failed - Test ID: {}, Error: {}",
                     testParam.getId(), e.getMessage());
            throw new AssertionError("WebSocket message validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate multiple messages in sequence
     */
    public static void validateMessageSequence(TestWebSocketParameter testParam, List<String> actualMessages) {
        List<JSONObject> expectedMessages = testParam.getExpectedMessages();

        if (testParam.isValidateMessageOrder()) {
            // Strict order validation
            if (actualMessages.size() < expectedMessages.size()) {
                throw new AssertionError(String.format(
                    "Expected %d messages but received only %d",
                    expectedMessages.size(), actualMessages.size()
                ));
            }

            for (int i = 0; i < expectedMessages.size(); i++) {
                JSONObject expected = expectedMessages.get(i).getJSONObject("content");
                JSONObject actual = JSON.parseObject(actualMessages.get(i));
                validateJsonContent(actual, expected);
                log.info("Message sequence validation passed for message #{}", i + 1);
            }
        } else {
            // Unordered validation - each expected message must appear somewhere
            for (JSONObject expectedMsg : expectedMessages) {
                JSONObject expected = expectedMsg.getJSONObject("content");
                boolean found = false;

                for (String actualMsg : actualMessages) {
                    try {
                        JSONObject actual = JSON.parseObject(actualMsg);
                        validateJsonContent(actual, expected);
                        found = true;
                        break;
                    } catch (AssertionError e) {
                        // Try next message
                    }
                }

                if (!found) {
                    throw new AssertionError("Expected message not found in sequence: " + expected.toJSONString());
                }
            }
        }

        log.info("WebSocket message sequence validation completed - Test ID: {}", testParam.getId());
    }

    /**
     * Validate JSON content recursively
     */
    public static void validateJsonContent(JSONObject actual, JSONObject expected) {
        for (String key : expected.keySet()) {
            if (!actual.containsKey(key)) {
                throw new AssertionError("Missing field in WebSocket message: " + key);
            }

            Object expectedValue = expected.get(key);
            Object actualValue = actual.get(key);

            if (expectedValue instanceof JSONObject && actualValue instanceof JSONObject) {
                // Recursive validation for nested objects
                validateJsonContent((JSONObject) actualValue, (JSONObject) expectedValue);
            } else if (expectedValue instanceof JSONArray && actualValue instanceof JSONArray) {
                // Array validation
                validateJsonArray((JSONArray) actualValue, (JSONArray) expectedValue);
            } else if (expectedValue instanceof String) {
                String expectedStr = (String) expectedValue;

                // Support special validation patterns
                if ("${any}".equals(expectedStr) || "${notNull}".equals(expectedStr)) {
                    if (actualValue == null) {
                        throw new AssertionError("Expected non-null value for field: " + key);
                    }
                } else if (expectedStr.startsWith("${regex:")) {
                    String pattern = expectedStr.substring(8, expectedStr.length() - 1);
                    if (!String.valueOf(actualValue).matches(pattern)) {
                        throw new AssertionError(String.format(
                            "Value does not match pattern for field %s: expected pattern=%s, actual=%s",
                            key, pattern, actualValue
                        ));
                    }
                } else if (!String.valueOf(expectedValue).equals(String.valueOf(actualValue))) {
                    throw new AssertionError(String.format(
                        "Value mismatch for field %s: expected=%s, actual=%s",
                        key, expectedValue, actualValue
                    ));
                }
            } else if (!Objects.equals(expectedValue, actualValue)) {
                throw new AssertionError(String.format(
                    "Value mismatch for field %s: expected=%s, actual=%s",
                    key, expectedValue, actualValue
                ));
            }
        }
    }

    /**
     * Validate JSON arrays
     */
    private static void validateJsonArray(JSONArray actual, JSONArray expected) {
        if (expected.size() > actual.size()) {
            throw new AssertionError(String.format(
                "Array size mismatch: expected at least %d elements, got %d",
                expected.size(), actual.size()
            ));
        }

        for (int i = 0; i < expected.size(); i++) {
            Object expectedElement = expected.get(i);
            boolean found = false;

            for (int j = 0; j < actual.size(); j++) {
                try {
                    Object actualElement = actual.get(j);
                    if (expectedElement instanceof JSONObject && actualElement instanceof JSONObject) {
                        validateJsonContent((JSONObject) actualElement, (JSONObject) expectedElement);
                    } else if (Objects.equals(expectedElement, actualElement)) {
                        // Direct comparison for primitive types
                    } else {
                        continue;
                    }
                    found = true;
                    break;
                } catch (AssertionError e) {
                    // Try next element
                }
            }

            if (!found) {
                throw new AssertionError("Expected array element not found: " + expectedElement);
            }
        }
    }

    /**
     * Validate message against JSON schema
     */
    private static void validateAgainstSchema(JSONObject message, JSONObject schema) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema jsonSchema = factory.getSchema(schema.toJSONString());

            // Convert JSONObject to Jackson JsonNode for validation
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(message.toJSONString());

            Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);

            if (!errors.isEmpty()) {
                String errorMessages = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining(", "));
                throw new AssertionError("Schema validation failed: " + errorMessages);
            }

            log.info("JSON schema validation passed");
        } catch (Exception e) {
            throw new AssertionError("Schema validation error: " + e.getMessage(), e);
        }
    }

    /**
     * Validate subscription response
     */
    public static void validateSubscriptionResponse(String actualMessage, List<String> expectedChannels) {
        try {
            JSONObject response = JSON.parseObject(actualMessage);

            // Check for subscription confirmation
            String method = response.getString("method");
            if (!"subscribe".equals(method) && !"subscription".equals(method)) {
                throw new AssertionError("Expected subscription response but got: " + method);
            }

            // Validate subscribed channels
            JSONObject result = response.getJSONObject("result");
            if (result != null) {
                JSONArray channels = result.getJSONArray("channels");
                if (channels == null) {
                    channels = response.getJSONObject("params").getJSONArray("channels");
                }

                for (String expectedChannel : expectedChannels) {
                    if (!channels.contains(expectedChannel)) {
                        throw new AssertionError("Channel not in subscription response: " + expectedChannel);
                    }
                }
            }

            log.info("Subscription validation passed for channels: {}", expectedChannels);

        } catch (Exception e) {
            log.error("Subscription validation failed: {}", e.getMessage());
            throw new AssertionError("Subscription validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate heartbeat response
     */
    public static void validateHeartbeatResponse(String actualMessage) {
        try {
            JSONObject response = JSON.parseObject(actualMessage);

            String method = response.getString("method");
            if (!"public/heartbeat".equals(method) && !"heartbeat".equals(method)) {
                throw new AssertionError("Invalid heartbeat response method: " + method);
            }

            Integer code = response.getInteger("code");
            if (code == null || code != 0) {
                throw new AssertionError("Heartbeat response code is not 0: " + code);
            }

            log.info("Heartbeat validation passed");

        } catch (Exception e) {
            log.error("Heartbeat validation failed: {}", e.getMessage());
            throw new AssertionError("Heartbeat validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Performance metrics validation
     */
    public static class PerformanceMetrics {
        private final List<Long> latencies = new ArrayList<>();
        private long messageCount = 0;
        private long startTime = System.currentTimeMillis();

        public void recordMessage(long latency) {
            latencies.add(latency);
            messageCount++;
        }

        public double getAverageLatency() {
            return latencies.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        }

        public long getMaxLatency() {
            return latencies.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        }

        public long getMinLatency() {
            return latencies.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);
        }

        public double getThroughput() {
            long duration = System.currentTimeMillis() - startTime;
            return duration > 0 ? (messageCount * 1000.0) / duration : 0;
        }

        public void validate(TestWebSocketParameter testParam) {
            if (!testParam.isCollectMetrics()) {
                return;
            }

            long maxLatency = getMaxLatency();
            if (maxLatency > testParam.getMaxLatency()) {
                throw new AssertionError(String.format(
                    "Maximum latency exceeded: expected <= %dms, actual = %dms",
                    testParam.getMaxLatency(), maxLatency
                ));
            }

            double throughput = getThroughput();
            if (throughput < testParam.getMinThroughput()) {
                throw new AssertionError(String.format(
                    "Minimum throughput not met: expected >= %.2f msg/s, actual = %.2f msg/s",
                    (double) testParam.getMinThroughput(), throughput
                ));
            }

            log.info("Performance metrics validation passed - Avg latency: {:.2f}ms, Throughput: {:.2f} msg/s",
                    getAverageLatency(), throughput);
        }
    }

    /**
     * Validate connection limit test results
     */
    public static void validateConnectionLimit(Map<String, Object> testResults, int expectedMax) {
        Boolean maxReached = (Boolean) testResults.get("maxConnectionsReached");
        Integer connectedCount = (Integer) testResults.get("connectedCount");
        Boolean exceedSuccess = (Boolean) testResults.get("exceedLimitSuccess");
        Boolean reconnectSuccess = (Boolean) testResults.get("reconnectAfterCloseSuccess");

        if (!maxReached || connectedCount != expectedMax) {
            throw new AssertionError(String.format(
                "Failed to reach maximum connections: expected=%d, actual=%d",
                expectedMax, connectedCount
            ));
        }

        if (exceedSuccess != null && exceedSuccess) {
            throw new AssertionError("Should not be able to exceed connection limit");
        }

        if (reconnectSuccess != null && !reconnectSuccess) {
            throw new AssertionError("Should be able to reconnect after closing a connection");
        }

        log.info("Connection limit validation passed - Max connections: {}", expectedMax);
    }
}