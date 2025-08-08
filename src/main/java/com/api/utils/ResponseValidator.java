package com.api.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.api.entities.TestAPIParameter;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.*;
import java.util.List;
import java.util.Map;

/**
 * Response validation utility class
 * Provides dynamic response validation functionality based on test parameters
 */
@Slf4j
public class ResponseValidator {

    /**
     * Validate response based on test parameters
     * @param testParam test parameters
     * @param response HTTP response
     */
    public static void validateResponse(TestAPIParameter testParam, Response response) {
        try {
            // Get validation configuration from checkPointJSONObject
            JSONObject checkPoints = testParam.getCheckPointJSONObject();
            
            if (checkPoints == null || checkPoints.isEmpty()) {
                return;
            }
            
            // Validate status code
            validateStatusCode(response, checkPoints);
            
            // Validate response body
            validateResponseBody(response, checkPoints);
            
            // Validate response headers
            validateResponseHeaders(response, checkPoints);
            
            log.info("Response validation completed - Test ID: {}", testParam.getId());
            
        } catch (Exception e) {
            log.error("Response validation failed - Test ID: {}, Error: {}", testParam.getId(), e.getMessage());
            throw new AssertionError("Response validation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate status code
     */
    private static void validateStatusCode(Response response, JSONObject checkPoints) {
        if (checkPoints.containsKey("expectedStatusCode")) {
            int expectedStatusCode = checkPoints.getIntValue("expectedStatusCode");
            response.then().assertThat().statusCode(expectedStatusCode);
            log.info("Status code validation passed: {}", expectedStatusCode);
        }
    }
    
    /**
     * Validate response body
     */
    private static void validateResponseBody(Response response, JSONObject checkPoints) {
        // Validate JSON paths
        if (checkPoints.containsKey("jsonPath")) {
            JSONObject jsonPathChecks = checkPoints.getJSONObject("jsonPath");
            for (String path : jsonPathChecks.keySet()) {
                Object expectedValue = jsonPathChecks.get(path);
                if (expectedValue != null) {
                    response.then().assertThat().body(path, equalTo(expectedValue));
                    log.info("JSON path validation passed: {} = {}", path, expectedValue);
                }
            }
        }
        
        // Validate non-null fields
        if (checkPoints.containsKey("notNullFields")) {
            Object notNullFieldsObj = checkPoints.get("notNullFields");
            if (notNullFieldsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> notNullFields = (java.util.List<String>) notNullFieldsObj;
                notNullFields.forEach(field -> {
                    response.then().assertThat().body(field, notNullValue());
                    log.info("Non-null field validation passed: {}", field);
                });
            } else if (notNullFieldsObj instanceof String) {
                // If it's a string, try to parse it as JSON array
                try {
                    com.alibaba.fastjson2.JSONArray jsonArray = com.alibaba.fastjson2.JSONArray.parseArray((String) notNullFieldsObj);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        String field = jsonArray.getString(i);
                        response.then().assertThat().body(field, notNullValue());
                        log.info("Non-null field validation passed: {}", field);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse notNullFields: {}", e.getMessage());
                }
            }
        }
        
        // Validate contains text
        if (checkPoints.containsKey("containsText")) {
            JSONObject containsChecks = checkPoints.getJSONObject("containsText");
            for (String field : containsChecks.keySet()) {
                String expectedText = containsChecks.getString(field);
                response.then().assertThat().body(field, containsString(expectedText));
                log.info("Contains text validation passed: {} contains {}", field, expectedText);
            }
        }
        
        // Validate partial JSON structure
        if (checkPoints.containsKey("partialJson")) {
            JSONObject partialJsonChecks = checkPoints.getJSONObject("partialJson");
            validatePartialJsonStructure(response, partialJsonChecks);
        }
    }
    
    /**
     * Validate response headers
     */
    private static void validateResponseHeaders(Response response, JSONObject checkPoints) {
        if (checkPoints.containsKey("headers")) {
            JSONObject headerChecks = checkPoints.getJSONObject("headers");
            for (String headerName : headerChecks.keySet()) {
                String expectedValue = headerChecks.getString(headerName);
                response.then().assertThat().header(headerName, expectedValue);
                log.info("Response header validation passed: {} = {}", headerName, expectedValue);
            }
        }
    }
    

    /**
     * Validate partial JSON structure recursively
     * @param response HTTP response
     * @param expectedStructure Expected JSON structure to validate against
     */
    private static void validatePartialJsonStructure(Response response, JSONObject expectedStructure) {
        try {
            // Get actual response as JSON
            String responseBody = response.getBody().asString();
            JSONObject actualJson = JSONObject.parseObject(responseBody);
            
            // Recursively validate the structure
            validateJsonRecursively("", actualJson, expectedStructure);
            log.info("Partial JSON structure validation completed");
            
        } catch (Exception e) {
            log.error("Partial JSON structure validation failed: {}", e.getMessage());
            throw new AssertionError("Partial JSON structure validation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recursively validate JSON structure
     * @param currentPath Current JSON path being validated
     * @param actualJson Actual JSON response
     * @param expectedJson Expected JSON structure
     */
    private static void validateJsonRecursively(String currentPath, Object actualJson, Object expectedJson) {
        if (expectedJson instanceof JSONObject && actualJson instanceof JSONObject) {
            JSONObject expectedObj = (JSONObject) expectedJson;
            JSONObject actualObj = (JSONObject) actualJson;
            
            for (String key : expectedObj.keySet()) {
                String newPath = currentPath.isEmpty() ? key : currentPath + "." + key;
                
                if (!actualObj.containsKey(key)) {
                    throw new AssertionError("Missing field in response: " + newPath);
                }
                
                Object expectedValue = expectedObj.get(key);
                Object actualValue = actualObj.get(key);
                
                if (expectedValue instanceof JSONObject || expectedValue instanceof Map) {
                    // Recursively validate nested objects
                    validateJsonRecursively(newPath, actualValue, expectedValue);
                } else if (actualValue instanceof JSONArray) {
                    // Array handling
                    if (expectedValue instanceof JSONArray || expectedValue instanceof List) {
                        JSONArray expectedArray = expectedValue instanceof JSONArray
                                ? (JSONArray) expectedValue
                                : new JSONArray((List<?>) expectedValue);
                        JSONArray actualArray = (JSONArray) actualValue;
                        for (int i = 0; i < expectedArray.size(); i++) {
                            Object expectedElement = expectedArray.get(i);
                            boolean matched = tryMatchAnyElement(newPath + "[?]", actualArray, expectedElement);
                            if (!matched) {
                                throw new AssertionError("No array element matches expected structure at " + newPath + " index " + i);
                            }
                        }
                    } else {
                        // Actual is an array while expected is a scalar -> check array contains the scalar
                        validateArrayMatchesAnyElement(newPath, (JSONArray) actualValue, expectedValue);
                    }
                } else {
                    // Compare leaf values with simple operators support
                    assertLeafMatches(newPath, actualValue, expectedValue);
                }
            }
        } else if (expectedJson instanceof Map && actualJson instanceof JSONObject) {
            // Handle Map to JSONObject comparison
            JSONObject actualObj = (JSONObject) actualJson;
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedMap = (Map<String, Object>) expectedJson;
            
            for (String key : expectedMap.keySet()) {
                String newPath = currentPath.isEmpty() ? key : currentPath + "." + key;
                
                if (!actualObj.containsKey(key)) {
                    throw new AssertionError("Missing field in response: " + newPath);
                }
                
                Object expectedValue = expectedMap.get(key);
                Object actualValue = actualObj.get(key);
                
                if (expectedValue instanceof Map || expectedValue instanceof JSONObject) {
                    // Recursively validate nested objects
                    validateJsonRecursively(newPath, actualValue, expectedValue);
                } else if (actualValue instanceof JSONArray) {
                    // Array handling
                    if (expectedValue instanceof JSONArray || expectedValue instanceof List) {
                        JSONArray expectedArray = expectedValue instanceof JSONArray
                                ? (JSONArray) expectedValue
                                : new JSONArray((List<?>) expectedValue);
                        JSONArray actualArray = (JSONArray) actualValue;
                        for (int i = 0; i < expectedArray.size(); i++) {
                            Object expectedElement = expectedArray.get(i);
                            boolean matched = tryMatchAnyElement(newPath + "[?]", actualArray, expectedElement);
                            if (!matched) {
                                throw new AssertionError("No array element matches expected structure at " + newPath + " index " + i);
                            }
                        }
                    } else {
                        // Actual is an array while expected is a scalar -> check array contains the scalar
                        validateArrayMatchesAnyElement(newPath, (JSONArray) actualValue, expectedValue);
                    }
                } else {
                    // Compare leaf values with simple operators support
                    assertLeafMatches(newPath, actualValue, expectedValue);
                }
            }
        } else if (actualJson instanceof JSONArray && (expectedJson instanceof JSONObject || expectedJson instanceof Map)) {
            // Actual is array, expected is a single object -> ensure any element matches
            validateArrayMatchesAnyElement(currentPath, (JSONArray) actualJson, expectedJson);
        } else if (actualJson instanceof JSONArray && (expectedJson instanceof List || expectedJson instanceof JSONArray)) {
            // Actual is array, expected is array -> each expected element must be matched by at least one actual element
            JSONArray actualArray = (JSONArray) actualJson;
            JSONArray expectedArray = expectedJson instanceof JSONArray ? (JSONArray) expectedJson : new JSONArray((List<?>) expectedJson);
            for (int i = 0; i < expectedArray.size(); i++) {
                Object expectedElement = expectedArray.get(i);
                boolean matched = tryMatchAnyElement(currentPath + "[?]", actualArray, expectedElement);
                if (!matched) {
                    throw new AssertionError("No array element matches expected structure at " + currentPath + " index " + i);
                }
            }
        } else {
            // Direct value comparison with operators
            assertLeafMatches(currentPath, actualJson, expectedJson);
        }
    }

    private static void assertLeafMatches(String path, Object actualValue, Object expectedValue) {
        // null handling
        if (expectedValue == null) {
            if (actualValue != null) {
                throw new AssertionError(String.format("Value mismatch at %s: expected=null, actual=%s", path, actualValue));
            }
            log.info("JSON structure validation passed: {} = null", path);
            return;
        }

        if (expectedValue instanceof String) {
            String expectedString = (String) expectedValue;
            // wildcard -> not null
            if ("*".equals(expectedString) || "${any}".equalsIgnoreCase(expectedString) || "${notNull}".equalsIgnoreCase(expectedString)) {
                if (actualValue == null || String.valueOf(actualValue).isEmpty()) {
                    throw new AssertionError("Expected non-null at " + path);
                }
                log.info("JSON structure validation passed: {} is not null", path);
                return;
            }
            // regex:... matching
            if (expectedString.startsWith("regex:")) {
                String pattern = expectedString.substring("regex:".length());
                if (actualValue == null || !String.valueOf(actualValue).matches(pattern)) {
                    throw new AssertionError(String.format("Regex mismatch at %s: expected pattern=%s, actual=%s", path, pattern, actualValue));
                }
                log.info("JSON structure validation passed: {} matches regex", path);
                return;
            }
            // contains:... substring matching
            if (expectedString.startsWith("contains:")) {
                String needle = expectedString.substring("contains:".length());
                if (actualValue == null || !String.valueOf(actualValue).contains(needle)) {
                    throw new AssertionError(String.format("Contains mismatch at %s: expected substring=%s, actual=%s", path, needle, actualValue));
                }
                log.info("JSON structure validation passed: {} contains substring", path);
                return;
            }
        }

        if (!String.valueOf(expectedValue).equals(String.valueOf(actualValue))) {
            throw new AssertionError(String.format("Value mismatch at %s: expected=%s, actual=%s",
                    path, expectedValue, actualValue));
        }
        log.info("JSON structure validation passed: {} = {}", path, expectedValue);
    }

    /**
     * Validate that an array contains at least one element that matches expected
     */
    private static void validateArrayMatchesAnyElement(String currentPath, JSONArray actualArray, Object expectedElement) {
        // If the expected element is a Map/JSONObject, we try to match structure; otherwise compare direct equality
        boolean matched = tryMatchAnyElement(currentPath + "[?]", actualArray, expectedElement);
        if (!matched) {
            throw new AssertionError("No array element matches expected structure at " + currentPath);
        }
        log.info("Array validation passed at {}: found matching element", currentPath);
    }

    private static boolean tryMatchAnyElement(String basePath, JSONArray actualArray, Object expectedElement) {
        for (int i = 0; i < actualArray.size(); i++) {
            Object actualElement = actualArray.get(i);
            try {
                String elementPath = basePath.replace("?", String.valueOf(i));
                if (expectedElement instanceof Map || expectedElement instanceof JSONObject) {
                    // Deep structure check
                    validateJsonRecursively(elementPath, actualElement, expectedElement);
                } else {
                    // Direct value comparison
                    if (!String.valueOf(expectedElement).equals(String.valueOf(actualElement))) {
                        throw new AssertionError("Value mismatch");
                    }
                }
                return true; // If no exception thrown, match succeeded
            } catch (AssertionError ignore) {
                // try next element
            }
        }
        return false;
    }

    /**
     * Validate error response
     */
    public static void validateError(Response response, int expectedStatusCode, String expectedMessage) {
        response.then().assertThat()
                .statusCode(expectedStatusCode)
                .body("message", containsString(expectedMessage));
        log.info("Error response validation passed: {} - {}", expectedStatusCode, expectedMessage);
    }
}