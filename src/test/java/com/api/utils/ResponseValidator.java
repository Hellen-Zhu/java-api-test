package com.api.utils;

import com.alibaba.fastjson2.JSONObject;
import com.api.entities.TestAPIParameter;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.*;
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
                } else {
                    // Compare leaf values
                    if (!String.valueOf(expectedValue).equals(String.valueOf(actualValue))) {
                        throw new AssertionError(String.format("Value mismatch at %s: expected=%s, actual=%s", 
                            newPath, expectedValue, actualValue));
                    }
                    log.info("JSON structure validation passed: {} = {}", newPath, expectedValue);
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
                } else {
                    // Compare leaf values
                    if (!String.valueOf(expectedValue).equals(String.valueOf(actualValue))) {
                        throw new AssertionError(String.format("Value mismatch at %s: expected=%s, actual=%s", 
                            newPath, expectedValue, actualValue));
                    }
                    log.info("JSON structure validation passed: {} = {}", newPath, expectedValue);
                }
            }
        } else {
            // Direct value comparison
            if (!String.valueOf(expectedJson).equals(String.valueOf(actualJson))) {
                throw new AssertionError(String.format("Value mismatch at %s: expected=%s, actual=%s", 
                    currentPath, expectedJson, actualJson));
            }
            log.info("JSON structure validation passed: {} = {}", currentPath, expectedJson);
        }
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