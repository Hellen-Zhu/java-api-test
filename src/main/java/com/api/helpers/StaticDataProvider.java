package com.api.helpers;

import com.api.entities.testng.XmlSuiteDetailAttribute;
import com.api.entities.TestAPIParameter;
import com.api.utils.DBUtil;
import com.api.utils.ReflectionUtil;

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

    // 负责将从数据库查出来的测试用例信息（List<TestAPIParameter>），转换成真正的测试对象数组
    private static Object[] getFinalTestParameterArray(List<TestAPIParameter> testAPIParameters) {
        Object[] resultArray = new Object[testAPIParameters.size()];
        for (int i = 0; i < testAPIParameters.size(); i++) {
            TestAPIParameter testParameter = testAPIParameters.get(i);
            String className = Constants.TESTCASE_BASEPATH + testParameter.getServiceName() + "." + testParameter.getClassName();
            Class<?> clazz = ReflectionUtil.fetchClassByClassName(className);
            if (clazz == null) {
                clazz = ReflectionUtil.fetchClassByClassName(Constants.TESTCASE_BASEPATH + "eh_default.Test_default");
            }
            resultArray[i] = ReflectionUtil.fetchMethodInvokeBaseOnClass(clazz, "getInstance", TestAPIParameter.class, testParameter);
        }
        log.info("resultArray: {}", resultArray);
        return resultArray;
    }
}