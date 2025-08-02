package com.api.helpers;

import com.api.entities.lif.AutoSystemVariable;
import com.api.entities.testng.XmlSuiteDetailAttribute;
import com.api.entities.TestAPIParameter;
import com.api.utils.DBUtil;
import com.api.utils.ReflectionUtil;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class StaticDataProvider {

    public static final String PBC_LATEST = "latest";
    public static final String PBC_V2 = "v2";
    public static final String PBC_V3 = "v3";
    public static final String PBC_V4 = "v4";
    public static final String[] PBC_VERSIONS = new String[]{PBC_V2, PBC_LATEST, PBC_V3, PBC_V4};
    public static final String PBC_COMPONENT = "TC - Preventative Booking Control Service";

    // #region new
    public static Object[] getTestNGObjectArrayForFactoryByService(Map<String, Object> automationParamsMap) {
        Map<String, Object> params = new HashMap(automationParamsMap);
        List<TestAPIParameter> testParameters = fetchTestParameterList(params).stream()
                .distinct()
                .collect(Collectors.toList());
        return getFinalTestParameterArray(testParameters);
    }

    @SuppressWarnings("unchecked")
    private static List<TestAPIParameter> fetchTestParameterList(Map<String, Object> map) {
        List<TestAPIParameter> parameters = (List<TestAPIParameter>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_case", map);
        String component = parameters.size() > 0 ? parameters.get(0).getComponent() : "";

        if (component.equalsIgnoreCase(PBC_COMPONENT) && !component.equals(XmlSuiteDetailAttribute.ID_LIST.getName())) {
            parameters = filterPBCCaseForComponentRun(parameters, map.get(XmlSuiteDetailAttribute.LABEL_LIST.getName()));
        }
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
        return resultArray;
    }

    @SuppressWarnings("unchecked")
    private static List<TestAPIParameter> filterPBCCaseForComponentRun(List<TestAPIParameter> parameters, Object labelObject) {
        String version = Arrays.stream(PBC_VERSIONS)
                .filter(ver -> StringUtils.containsIgnoreCase(parameters.get(0).getScenario(), ver))
                .findFirst()
                .orElse("none");

        Map<String, String> params = new HashMap<>();
        params.put(XmlSuiteDetailAttribute.COMPONENT.getName(), PBC_COMPONENT);
        params.put("componentLike", parameters.get(0).getComponentLike());
        params.put("configKey", "pbc." + version + ".enableLabel");

        List<AutoSystemVariable> autoSystemVariables = (List<AutoSystemVariable>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_system_variable", params);
        if (CollectionUtils.isEmpty(autoSystemVariables)) {
            return parameters;
        }
        return doFilterFinalCaseForPBC(parameters, autoSystemVariables, labelObject);
    }

    private static List<TestAPIParameter> doFilterFinalCaseForPBC(List<TestAPIParameter> parameters, List<AutoSystemVariable> autoSystemVariables, Object labelObject) {
        Optional<AutoSystemVariable> variableFilteredByDefault = autoSystemVariables.stream()
                .filter(x -> StringUtils.equalsIgnoreCase(x.getProfile(), "default"))
                .findFirst();

        List<String> profileList = parameters.stream()
                .map(TestAPIParameter::getProfileName)
                .distinct()
                .collect(Collectors.toList());

        List<TestAPIParameter> result = Lists.newArrayList();
        for (String profileName : profileList) {
            Optional<AutoSystemVariable> variableFilteredByProfile = autoSystemVariables.stream()
                    .filter(x -> StringUtils.equalsIgnoreCase(x.getProfile(), profileName))
                    .findFirst();

            AutoSystemVariable variable = variableFilteredByProfile.orElseGet(variableFilteredByDefault::get);

            List<String> labels = Arrays.stream(variable.getValue().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            if (labelObject != null) {
                labels.retainAll(Arrays.asList((String[]) labelObject));
            }

            result.addAll(parameters.stream()
                    .filter(x -> StringUtils.equalsIgnoreCase(x.getProfileName(), profileName)
                            && labels.contains(x.getLabel()))
                    .collect(Collectors.toList()));
        }
        return result;
    }
}