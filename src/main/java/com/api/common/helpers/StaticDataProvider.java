package com.api.common.helpers;

import com.api.common.enums.XmlSuiteDetailAttribute;
import com.api.data.entities.TestAPIParameter;
import com.api.data.entities.TestWebSocketParameter;
import com.api.common.utils.DBUtil;
import com.api.common.utils.ReflectionUtil;

import lombok.extern.slf4j.Slf4j;


import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class StaticDataProvider {
    // #region new
    public static Object[] getTestNGObjectArrayForFactoryByService(Map<String, Object> automationParamsMap) {
        log.info("StaticDataProvider.getTestNGObjectArrayForFactoryByService - input automationParamsMap: {}", automationParamsMap);
        
        Map<String, Object> params = new HashMap(automationParamsMap);
        log.info("StaticDataProvider.getTestNGObjectArrayForFactoryByService - params for fetchTestParameterList: {}", params);
        
        List<TestAPIParameter> testParameters = fetchTestParameterList(params).stream()
                .distinct()
                .collect(Collectors.toList());
        log.info("testParameters: {}", testParameters);
        return getFinalTestParameterArray(testParameters);
    }

    @SuppressWarnings("unchecked")
    private static List<TestAPIParameter> fetchTestParameterList(Map<String, Object> map) {
        List<TestAPIParameter> parameters = (List<TestAPIParameter>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_case", map);
        return ParameterHelper.getFinalTestParameterList(parameters, map.get(XmlSuiteDetailAttribute.RUN_ID.getName()).toString());
    }

    // Convert the test case information fetched from the database (List<TestAPIParameter>)
    // into the actual array of test objects
    private static Object[] getFinalTestParameterArray(List<TestAPIParameter> testAPIParameters) {
        Object[] resultArray = new Object[testAPIParameters.size()];
        for (int i = 0; i < testAPIParameters.size(); i++) {
            TestAPIParameter testParameter = testAPIParameters.get(i);

            // Check if this is a WebSocket test
            boolean isWebSocket = "websocket".equalsIgnoreCase(testParameter.getMethod()) ||
                                  "WEBSOCKET".equalsIgnoreCase(testParameter.getMethod());

            String className;
            Class<?> parameterClass;

            if (isWebSocket) {
                // For WebSocket tests, convert to TestWebSocketParameter
                TestWebSocketParameter wsParameter = convertToWebSocketParameter(testParameter);
                className = Constants.TESTCASE_BASEPATH + "websocket." + testParameter.getClassName();
                parameterClass = TestWebSocketParameter.class;
                testParameter = wsParameter; // Replace with WebSocket parameter
            } else {
                // Regular HTTP test
                className = Constants.TESTCASE_BASEPATH + testParameter.getServiceName() + "." + testParameter.getClassName();
                parameterClass = TestAPIParameter.class;
            }

            Class<?> clazz = ReflectionUtil.fetchClassByClassName(className);
            if (clazz == null) {
                if (isWebSocket) {
                    // Try default WebSocket test class
                    clazz = ReflectionUtil.fetchClassByClassName(Constants.TESTCASE_BASEPATH + "websocket.Test_WebSocketMarket");
                }
                if (clazz == null) {
                    // Fall back to default test class
                    clazz = ReflectionUtil.fetchClassByClassName(Constants.TESTCASE_BASEPATH + "eh_default.Test_default");
                }
            }
            resultArray[i] = ReflectionUtil.fetchMethodInvokeBaseOnClass(clazz, "getInstance", parameterClass, testParameter);
        }
        log.info("resultArray: {}", resultArray);
        return resultArray;
    }

    /**
     * Convert TestAPIParameter to TestWebSocketParameter for WebSocket tests
     */
    private static TestWebSocketParameter convertToWebSocketParameter(TestAPIParameter apiParameter) {
        TestWebSocketParameter wsParameter = new TestWebSocketParameter();

        // Copy common fields
        wsParameter.setId(apiParameter.getId());
        wsParameter.setE2E(apiParameter.isE2E());
        wsParameter.setSanity(apiParameter.isSanity());
        wsParameter.setGlobal(apiParameter.isGlobal());
        wsParameter.setEnable(apiParameter.isEnable());
        wsParameter.setRunId(apiParameter.getRunId());
        wsParameter.setRegion(apiParameter.getRegion());
        wsParameter.setSuite(apiParameter.getSuite());
        wsParameter.setScenario(apiParameter.getScenario());
        wsParameter.setIssueKey(apiParameter.getIssueKey());
        wsParameter.setTestCaseDescription(apiParameter.getTestCaseDescription());
        wsParameter.setLabel(apiParameter.getLabel());
        wsParameter.setParameter(apiParameter.getParameter());
        wsParameter.setServiceName(apiParameter.getServiceName());
        wsParameter.setClassName(apiParameter.getClassName());
        wsParameter.setComponent(apiParameter.getComponent());
        wsParameter.setComponentLike(apiParameter.getComponentLike());
        wsParameter.setPath(apiParameter.getPath());
        wsParameter.setMethod(apiParameter.getMethod());
        wsParameter.setProfileName(apiParameter.getProfileName());

        // Parse WebSocket specific configuration from parameter JSON
        if (apiParameter.getParameter() != null) {
            com.alibaba.fastjson2.JSONObject paramJson = com.alibaba.fastjson2.JSONObject.parseObject(apiParameter.getParameter());
            wsParameter.parseWebSocketConfig(paramJson);
        }

        return wsParameter;
    }
}