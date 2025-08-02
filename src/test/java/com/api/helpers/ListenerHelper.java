package com.api.helpers;

import com.api.listeners.XmlTestListener;
import com.api.utils.DBUtil;
import com.api.utils.MemoryCacheUtil;
import com.api.utils.SkipTestUtil;
import com.api.entities.TestAPIParameter;
import com.api.entities.auto.ComponentProgress;
import com.api.entities.lif.AutoProgressStatus;
import com.api.entities.lif.AutoSystemVariable;
import com.api.entities.testng.ReportAttributeInfo;
import com.api.entities.testng.Scenario;
import com.api.entities.testng.Suite;
import com.api.entities.testng.XmlSuiteDetailAttribute;
import com.api.enums.AutomationRunStatus;
import com.alibaba.fastjson2.JSON;
import com.github.f4b6a3.ulid.UlidCreator;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.ITestContext;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;
import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ListenerHelper {

    public static void createXmlSuites(List<XmlSuite> suites, XmlTest xmlTest,
                                       Map<String, Map<String, String>> suiteAndFinalSuiteParameterMap) {
        suiteAndFinalSuiteParameterMap.forEach((suiteName, suiteParameters) -> {
            String component = suiteParameters.get(XmlSuiteDetailAttribute.COMPONENT.getName());
            XmlSuite newSuite = new XmlSuite();
            newSuite.setName(component);
            newSuite.setParameters(suiteParameters);
            newSuite.setTests(createXmlTests(xmlTest, suiteParameters));
            newSuite.setParallel(XmlSuite.ParallelMode.TESTS);
            newSuite.setThreadCount(5);
            newSuite.setTimeOut("9000000"); // 2.5 hours
            suites.add(newSuite);
        });
    }

    public static Object fetchGlobalVariable(String variable) {
        List<AutoSystemVariable> globalVariables = (List<AutoSystemVariable>)
                DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_system_variable",
                        Map.of("config_key", variable));

        Map<String, Object> globalVariablesMap = new HashMap<>();
        globalVariables.forEach(globalVariable ->
                globalVariablesMap.put(globalVariable.getConfigKey(), globalVariable.getValue()));

        Object value = null;
        for (Map.Entry<String, Object> entry : globalVariablesMap.entrySet()) {
            String key = entry.getKey();
            value = switch (key) {
                case "currentTime" -> LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                case "currentTime-7" -> LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                case "currentYear" -> LocalDate.now().getYear();
                case "currentMonth" -> LocalDate.now().getMonthValue();
                case "currentDay" -> LocalDate.now().getDayOfMonth();
                default -> entry.getValue();
            };
        }
        MemoryCacheUtil.getInstance().put("@@" + variable, value);
        return value;
    }

    public static List<XmlTest> createXmlTests(XmlTest xmlTest, Map<String, String> paramsMap) {
        String[] scenarios = paramsMap.get(XmlSuiteDetailAttribute.SCENARIO_LIST.getName()).split(",");
        List<XmlTest> result = new ArrayList<>();
        Arrays.asList(scenarios).forEach(xmlTestName -> {
            Map<String, String> xmlTestParameterMap = new HashMap<>();
            xmlTestParameterMap.put(XmlSuiteDetailAttribute.RUN_ID.getName(), paramsMap.get(XmlSuiteDetailAttribute.RUN_ID.getName()));
            xmlTestParameterMap.put(XmlSuiteDetailAttribute.COMPONENT.getName(), paramsMap.get(XmlSuiteDetailAttribute.COMPONENT.getName()));
            xmlTestParameterMap.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(), paramsMap.get(XmlSuiteDetailAttribute.SANITY_ONLY.getName()));
            xmlTestParameterMap.put(XmlSuiteDetailAttribute.ID_LIST.getName(), paramsMap.get(XmlSuiteDetailAttribute.ID_LIST.getName()));
            xmlTestParameterMap.put(XmlSuiteDetailAttribute.SCENARIO.getName(), xmlTestName);
            xmlTestParameterMap.put(XmlSuiteDetailAttribute.LABEL_LIST.getName(), paramsMap.get(XmlSuiteDetailAttribute.LABEL_LIST.getName()));

            XmlTest newXmlTest = (XmlTest) xmlTest.clone();
            newXmlTest.setName(xmlTestName);
            newXmlTest.setParameters(xmlTestParameterMap);
            newXmlTest.setPreserveOrder(false);
            result.add(newXmlTest);
        });
        return result;
    }

    public static void prepareTestCaseDetailForMainTestMethod(ITestResult res) {
        if (res.getMethod().getMethodsDependedUpon().length != 0) {
            Object obj = res.getInstance();
            TestAPIParameter testAPIParameter = null;
            try {
                testAPIParameter = (TestAPIParameter) res.getTestClass()
                        .getRealClass()
                        .getField("testParameter")
                        .get(obj);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                log.error(String.format("%s %s occur Exception", res.getTestClass(), res.getTestName()));
            }
            assert testAPIParameter != null;
            SkipTestUtil.ifSkipStepForDependOn(testAPIParameter);
        }
    }

    public static Map<String, Map<String, String>> buildSuiteAndFinalSuiteParameterMapBetweenOriginalAndAutoConfiguration(
            Map<String, String> originalParams) {

        Map<String, Map<String, String>> result = new HashMap<>();

        Map<String, Object> params = fetchSelectParams(originalParams);

        List<ReportAttributeInfo> componentScenarioList = (List<ReportAttributeInfo>)
                DBUtil.doSqlSessionByEnvironment("postgresql_lif", "fetchXmlSuiteDetailParameters", params);

        String automationToolSVCUrl = ((List<String>) DBUtil.doSqlSessionByEnvironment(
                "postgresql_lif", "selectStringListBySQL",
                Map.of("sql", "select value from auto_system_variable where config_key = 'automation-tool.service.url'"))).get(0);


        componentScenarioList.forEach(item -> {
            result.put(item.getSuite(),
                    buildFinalSuiteParamMap(item, originalParams, automationToolSVCUrl));
        });

        return result;
    }

    private static Map<String, Object> fetchSelectParams(Map<String, String> originalParams) {
        String sanityOnly = originalParams.get(XmlSuiteDetailAttribute.SANITY_ONLY.getName()).toLowerCase();
        String module = originalParams.get(XmlSuiteDetailAttribute.MODULE.getName()).toLowerCase();
        String labelStr = originalParams.containsKey("label") ? originalParams.get("label") : originalParams.get(XmlSuiteDetailAttribute.LABEL_LIST.getName());
        String[] components = StringUtils.splitByWholeSeparator(originalParams.get(XmlSuiteDetailAttribute.COMPONENT.getName()).toLowerCase(), ",");
        String[] scenarios = StringUtils.splitByWholeSeparator(originalParams.get(XmlSuiteDetailAttribute.SCENARIO_LIST.getName()).toLowerCase(), ",");
        String[] ids = StringUtils.splitByWholeSeparator(originalParams.get(XmlSuiteDetailAttribute.ID_LIST.getName()).toLowerCase(), ",");
        String[] labels = StringUtils.splitByWholeSeparator(labelStr, ",");

        Map<String, Object> params = new HashMap<>();
        if (module != null) params.put(XmlSuiteDetailAttribute.MODULE.getName(), module);
        if (components != null) params.put("components", components);
        if (scenarios != null) params.put(XmlSuiteDetailAttribute.SCENARIO_LIST.getName(), scenarios);
        if (ids != null) params.put(XmlSuiteDetailAttribute.ID_LIST.getName(), ids);
        if (labels != null) params.put(XmlSuiteDetailAttribute.LABEL_LIST.getName(), labels);
        if (sanityOnly != null) params.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(), sanityOnly);

        return params;
    }
    private static Map<String, String> buildFinalSuiteParamMap(
            ReportAttributeInfo reportAttributeInfo,
            Map<String, String> originalParams,
            String automationToolSVCUrl) {

        Map<String, String> result = new HashMap<>();

        // from original
        String isDebug = originalParams.get(XmlSuiteDetailAttribute.IS_DEBUG.getName());
        String releaseVersions = originalParams.get(XmlSuiteDetailAttribute.RELEASE_VERSION_LIST.getName());
        String sanityOnly = originalParams.get(XmlSuiteDetailAttribute.SANITY_ONLY.getName());
        String labels = originalParams.containsKey("label") ? originalParams.get(XmlSuiteDetailAttribute.LABEL_LIST.getName()) : "";

        result.put(XmlSuiteDetailAttribute.IS_DEBUG.getName(),
                Objects.isNull(isDebug) || StringUtils.isEmpty(isDebug) ? "true" : isDebug);
        result.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(),
                Objects.isNull(sanityOnly) || StringUtils.isEmpty(sanityOnly) ? "false" : sanityOnly);

        // from reportAttributeInfo
        String dailyRunVersion = reportAttributeInfo.getDailyRunVersion();
        String actualVersion = Objects.isNull(releaseVersions) || StringUtils.isEmpty(releaseVersions)
                ? dailyRunVersion : releaseVersions;
        String isRelease = Objects.isNull(releaseVersions) || StringUtils.isEmpty(releaseVersions)
                ? "false" : "true";

        result.put(XmlSuiteDetailAttribute.DAILYRUN_VERSION.getName(), dailyRunVersion);
        result.put(XmlSuiteDetailAttribute.ACTUAL_FIXVERSION.getName(), actualVersion);
        result.put(XmlSuiteDetailAttribute.IS_RELEASE.getName(), isRelease);
        result.put(XmlSuiteDetailAttribute.ENABLE_EMAIL.getName(),
                String.valueOf(reportAttributeInfo.isEnableEmail()));
        result.put(XmlSuiteDetailAttribute.SUITE.getName(), reportAttributeInfo.getSuite());
        result.put(XmlSuiteDetailAttribute.COMPONENT.getName(), reportAttributeInfo.getComponent());
        result.put(XmlSuiteDetailAttribute.SCENARIO_LIST.getName(), reportAttributeInfo.getScenarios());
        result.put(XmlSuiteDetailAttribute.ENABLE_FAST.getName(),
                String.valueOf(reportAttributeInfo.isEnableFast()));
        result.put(XmlSuiteDetailAttribute.FAST_DASHBOARD_ENV.getName(), reportAttributeInfo.getFastDashboardEnv());
        result.put(XmlSuiteDetailAttribute.FAST_PROJECT.getName(), reportAttributeInfo.getFastProject());
        result.put(XmlSuiteDetailAttribute.JIRA_KEY.getName(), reportAttributeInfo.getJiraKey());
        result.put(XmlSuiteDetailAttribute.FAST_KEY.getName(), reportAttributeInfo.getFastKey());
        result.put(XmlSuiteDetailAttribute.PROJECT_KEY.getName(), reportAttributeInfo.getProjectKey());
        result.put(XmlSuiteDetailAttribute.ENABLE_JIRA.getName(),
                String.valueOf(reportAttributeInfo.isEnableJira()));
        result.put(XmlSuiteDetailAttribute.TEST_CYCLE.getName(), reportAttributeInfo.getTestCycle());

        // from both
        String ccAddress = originalParams.get(XmlSuiteDetailAttribute.COPY_ADDRESS.getName());
        String toAddress = originalParams.get(XmlSuiteDetailAttribute.RECEIVER_ADDRESS.getName());
        String copyAddress = Objects.isNull(ccAddress) || StringUtils.isEmpty(ccAddress)
                ? reportAttributeInfo.getCopyAddress() : ccAddress;
        String receiverAddress = Objects.isNull(toAddress) || StringUtils.isEmpty(toAddress)
                ? reportAttributeInfo.getReceiverAddress() : toAddress;
        String mondoFlag = originalParams.get(XmlSuiteDetailAttribute.ENABLE_MONDO.getName());
        String enableMondo = Objects.isNull(mondoFlag) || StringUtils.isEmpty(mondoFlag)
                ? String.valueOf(reportAttributeInfo.isEnableMondo()) : mondoFlag;

        result.put(XmlSuiteDetailAttribute.COPY_ADDRESS.getName(), copyAddress);
        result.put(XmlSuiteDetailAttribute.RECEIVER_ADDRESS.getName(), receiverAddress);
        result.put(XmlSuiteDetailAttribute.ENABLE_MONDO.getName(), enableMondo);

        // others
        String run_id = StringUtils.isNotBlank(originalParams.get(XmlSuiteDetailAttribute.RUN_ID.getName()))
                ? originalParams.get(XmlSuiteDetailAttribute.RUN_ID.getName())
                : UlidCreator.getUlid().toString();
        String runBy = Objects.isNull(originalParams.get(XmlSuiteDetailAttribute.RUN_BY.getName())) ||
                StringUtils.isEmpty(originalParams.get(XmlSuiteDetailAttribute.RUN_BY.getName()))
                ? "local-debug" : originalParams.get(XmlSuiteDetailAttribute.RUN_BY.getName());

        result.put(XmlSuiteDetailAttribute.AUTOMATION_TOOL_URL.getName(), automationToolSVCUrl);
        result.put(XmlSuiteDetailAttribute.ID_LIST.getName(),
                originalParams.get(XmlSuiteDetailAttribute.ID_LIST.getName()) == null ? "" :
                        originalParams.get(XmlSuiteDetailAttribute.ID_LIST.getName()));
        result.put(XmlSuiteDetailAttribute.LABEL_LIST.getName(), labels);
        result.put(XmlSuiteDetailAttribute.GROUP.getName(), "Regression");
        result.put(XmlSuiteDetailAttribute.RUN_BY.getName(), runBy);
        result.put(XmlSuiteDetailAttribute.TEST_SCOPE.getName(), "Regression");
        result.put(XmlSuiteDetailAttribute.TEST_TYPE.getName(), "Regression");
        result.put(XmlSuiteDetailAttribute.BUILD.getName(), "");
        result.put(XmlSuiteDetailAttribute.REGION.getName(), "nam");
        result.put(XmlSuiteDetailAttribute.RUN_ID.getName(), run_id);

        return result;
    }
    public static void endAutoTestNGResultForSingleIdRun(ISuite suite, Map<String, Set<Scenario>> featureScenarioMap) {
        if (StringUtils.isNotBlank(suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.ID_LIST.getName()))) {
            Suite xmlSuiteDetail = new Suite();
            xmlSuiteDetail.setSuiteParameters(suite.getXmlSuite().getParameters());
            xmlSuiteDetail.setFeatureScenarioMap(featureScenarioMap);
            LocalDateTime insertDateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));

            Map<String, Object> map = new HashMap<>();
            map.put(XmlSuiteDetailAttribute.RUN_ID.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.RUN_ID.getName()));
            map.put(XmlSuiteDetailAttribute.COMPONENT.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.COMPONENT.getName()));
            map.put("suiteResult", JSON.toJSONString(xmlSuiteDetail));
            map.put("insertDateTime", insertDateTime);
            DBUtil.doSqlSessionByEnvironment("postgresql_lif", "updateAutoResultTable", map);
        }
    }

    public static void endAutoProgressForSuiteRun(ISuite suite) {
        Map<String, String> suiteParameters = suite.getXmlSuite().getParameters();
        boolean isDebug = Boolean.parseBoolean(suiteParameters.get(XmlSuiteDetailAttribute.IS_DEBUG.getName()));

        if (!isDebug && suite.getAllMethods().size() != 0) {
            String runId = suiteParameters.get(XmlSuiteDetailAttribute.RUN_ID.getName());
            String component = suiteParameters.get(XmlSuiteDetailAttribute.COMPONENT.getName());
            LocalDateTime endTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));

            if (suite.getResults().size() == 0) {
                Map<String, String> updateMap = new HashMap<>();
                updateMap.put("condition", "task_status = '" + AutoProgressStatus.TIMEOUT + "', end_time = '" + endTime + "'");
                updateMap.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
                updateMap.put(XmlSuiteDetailAttribute.COMPONENT.getName(), component);
                DBUtil.doSqlSessionByEnvironment("postgresql_lif", "updateAutoProgress", updateMap);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put("condition", "task_status = case when total_cases = passes+failures+skips then '" +
                        AutoProgressStatus.COMPLETED + "' else '" + AutoProgressStatus.ERROR + "' end, end_time = '" + endTime + "'");
                map.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
                map.put(XmlSuiteDetailAttribute.COMPONENT.getName(), component);
                DBUtil.doSqlSessionByEnvironment("postgresql_lif", "updateAutoProgress", map);
            }
        }
    }

    public static void sendCompleteSignalForModule(ISuite suite) {
        Map<String, String> suiteParameters = suite.getXmlSuite().getParameters();
        boolean isDebug = Boolean.parseBoolean(suiteParameters.get(XmlSuiteDetailAttribute.IS_DEBUG.getName()));

        if (isDebug && suite.getAllMethods().size() != 0) {
            // 在 Debug 模式下且有测试方法时，不执行此逻辑
            return;
        }

        String runId = suite.getXmlSuite().getParameters().get(XmlSuiteDetailAttribute.RUN_ID.getName());
        List<ComponentProgress> componentProgresses = (List<ComponentProgress>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "queryComponentGroupByModule",
                Map.of(XmlSuiteDetailAttribute.RUN_ID.getName(), runId));

        if (componentProgresses.size() > 0) {
            boolean ifAllCompleted = componentProgresses.stream().allMatch(x -> x.getTaskStatus() != null && StringUtils.equalsIgnoreCase(x.getTaskStatus(), "COMPLETED"));

            if (ifAllCompleted) {
                given().contentType(ContentType.JSON)
                        .queryParam("group_id", componentProgresses.get(0).getGroupId())
                        .when()
                        .post("http://mondo.nam.nsroot.net:8080/api/automation/email/{group_id}")
                        .then()
                        .log()
                        .body();
            }
        }
    }

    public static void initialAutoProgressForSuiteRun(ISuite suite) {
        Map<String, String> suiteParameters = suite.getXmlSuite().getParameters();
        boolean isDebug = Boolean.parseBoolean(suiteParameters.get(XmlSuiteDetailAttribute.IS_DEBUG.getName()));

        if (!isDebug && suite.getAllMethods().size() != 0) {
            String[] fixVersions = StringUtils.splitByWholeSeparator(suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.ACTUAL_FIXVERSION.getName()), ",");
            String runId = suiteParameters.get(XmlSuiteDetailAttribute.RUN_ID.getName());
            String component = suiteParameters.get(XmlSuiteDetailAttribute.COMPONENT.getName());

            try {
                for (String fixVersion : fixVersions) {
                    Set<String> caseInstanceName = new HashSet<>();
                    for (ITestNGMethod iTestNGMethod : suite.getAllMethods()) {
                        Object obj = iTestNGMethod.getInstance();
                        TestAPIParameter testAPIParameter = (TestAPIParameter) iTestNGMethod.getRealClass().getField("testParameter").get(obj);
                        caseInstanceName.add(runId + "case" + testAPIParameter.getId());
                    }

                    List<String> versionIdList = (List<String>) DBUtil.doSqlSessionByEnvironment(
                            "postgresql_lif", "selectStringListBySQL",
                            Map.of("sql", "select value from auto_system_variable where config_key = '" + fixVersion.toLowerCase() + ".versionId'")
                    );

                    Map<String, Comparable> autoProgressParam = new HashMap<>();
                    autoProgressParam.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
                    autoProgressParam.put(XmlSuiteDetailAttribute.VERSION_ID.getName(), versionIdList.size() > 0 ? versionIdList.get(0) : "");
                    autoProgressParam.put(XmlSuiteDetailAttribute.ACTUAL_FIXVERSION.getName(), fixVersion);
                    autoProgressParam.put(XmlSuiteDetailAttribute.COMPONENT.getName(), component);
                    autoProgressParam.put(XmlSuiteDetailAttribute.RUN_BY.getName(), suiteParameters.get(XmlSuiteDetailAttribute.RUN_BY.getName()));
                    autoProgressParam.put("beginTime", LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
                    autoProgressParam.put("totalCases", caseInstanceName.size());
                    autoProgressParam.put("taskStatus", AutoProgressStatus.PROCESSING);
                    autoProgressParam.put("labels", suiteParameters.get(XmlSuiteDetailAttribute.LABEL_LIST.getName()));

                    DBUtil.doSqlSessionByEnvironment("postgresql_lif", "insertIntoAutoProgress", autoProgressParam);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SneakyThrows
    public static void changeTestNameWithTestCaseDescription(ITestResult result) {
        TestAPIParameter testAPIParameter = (TestAPIParameter) result.getMethod().getRealClass().getField("testParameter").get(result.getMethod().getInstance());
        int id = testAPIParameter.getId();
        int stepId = testAPIParameter.getStepId();
        String methodName = result.getMethod().getMethodName();
        result.setTestName(id + " -> " + stepId + " -> " + methodName);
    }

    @SneakyThrows
    public static void initialCaseInstanceMap(ITestContext res, Map<String, int[]> caseInstanceAndTestMap) {
        String runId = res.getSuite().getParameter(XmlSuiteDetailAttribute.RUN_ID.getName());
        ITestNGMethod[] allTestMethods = res.getAllTestMethods();
        for (ITestNGMethod testNGMethod : allTestMethods) {
            Object obj = testNGMethod.getInstance();
            TestAPIParameter testAPIParameter = (TestAPIParameter) testNGMethod.getRealClass().getField("testParameter").get(obj);
            String caseInstanceName = runId + "case" + testAPIParameter.getId();
            if (!caseInstanceAndTestMap.containsKey(caseInstanceName)) {
                caseInstanceAndTestMap.put(caseInstanceName, new int[]{1, 0, 1});
            } else {
                caseInstanceAndTestMap.get(caseInstanceName)[0]++;
            }
        }
    }

    public static void initialAutoTestNGResultForSingleIdRun(ISuite suite) {
        if (StringUtils.isNotBlank(suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.ID_LIST.getName()))) {
            DBUtil.doSqlSessionByEnvironment("postgresql_lif", "insertIntoAutoResultTable",
                    Map.of(
                            XmlSuiteDetailAttribute.RUN_ID.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.RUN_ID.getName()),
                            XmlSuiteDetailAttribute.COMPONENT.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.COMPONENT.getName())
                    ));
        }
    }
    @SneakyThrows
    public static void addTestCaseProgress(ITestResult res, XmlTestListener xmlTestListener,
                                           Map<String, int[]> caseInstanceAndTestMap, boolean passedFlag) {
        synchronized (xmlTestListener) {
            String runId = res.getTestContext().getSuite().getParameter(XmlSuiteDetailAttribute.RUN_ID.getName());
            String component = res.getTestContext().getSuite().getParameter(XmlSuiteDetailAttribute.COMPONENT.getName());
            Object obj = res.getInstance();
            TestAPIParameter testParameter = (TestAPIParameter) res.getTestClass().getRealClass().getField("testParameter").get(obj);
            String caseInstance = runId + "case" + testParameter.getId();
            caseInstanceAndTestMap.get(caseInstance)[1]++;
            if (!passedFlag) {
                caseInstanceAndTestMap.get(caseInstance)[2] = 0;
            }

            int[] testNums = caseInstanceAndTestMap.get(caseInstance);
            if (testNums[1] == testNums[0]) {
                if (caseInstanceAndTestMap.get(caseInstance)[2] == 1) {
                    updateAutoProgress(runId, component, "passes = passes + 1");
                } else {
                    updateAutoProgress(runId, component, "failures = failures + 1");
                }
            }
        }
    }

    public static void appendAttributeValueIntoTestContextByAttributeName(ITestContext testContext, String attributeName, String value) {
        Object originValue = testContext.getAttribute(attributeName);
        if (originValue == null) {
            testContext.setAttribute(attributeName, value);
        } else {
            testContext.removeAttribute(attributeName);
            testContext.setAttribute(attributeName, originValue + "\n" + value);
        }
    }

    public static void updateAutoProgress(String runId, String component, String condition) {
        Map<String, String> map = new HashMap<>();
        map.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
        map.put(XmlSuiteDetailAttribute.COMPONENT.getName(), component);
        map.put("condition", condition);
        DBUtil.doSqlSessionByEnvironment("postgresql_lif", "updateAutoProgress", map);
    }

    public static void fillLogIntoTestResultAttribute(ITestResult res) {
        appendAttributeValueIntoTestResultByAttributeName(res, XmlSuiteDetailAttribute.DEBUG_LOG.getName());
        appendAttributeValueIntoTestResultByAttributeName(res, XmlSuiteDetailAttribute.REPORT_LOG.getName());
    }

    public static void resetSkipReasonForNoNeedCheckByDependOnMethod(ITestResult res) {
        Set<String> dependMethodNames = new HashSet<>() {{
            String[] methods = res.getMethod().getMethodsDependedUpon();
            for (String method : methods) {
                String[] arr = method.split("\\.");
                add(arr[arr.length - 1]);
            }
        }};

        res.getTestContext().getSkippedTests().getAllResults().forEach(iTestResult -> {
            String tsNameOriginal = iTestResult.getName().split("] ").length == 1
                    ? iTestResult.getName().split("] ")[0]
                    : iTestResult.getName().split("] ")[1];
            String[] arr = tsNameOriginal.split("->");
            String tsName = arr.length == 1 ? arr[0].trim() : arr[arr.length - 1].trim();

            if (dependMethodNames.contains(tsName)) {
                String skipCauseReason = iTestResult.getThrowable() != null
                        ? iTestResult.getThrowable().toString()
                        : "";
                if (skipCauseReason.contains(AutomationRunStatus.SKIPPED_METHOD_NO_NEED.name())) {
                    iTestResult.setThrowable(new Throwable(AutomationRunStatus.SKIPPED_METHOD_NO_NEED.name()));
                }
                if (skipCauseReason.contains(AutomationRunStatus.SKIPPED_STEP_NO_NEED.name())) {
                    iTestResult.setThrowable(new Throwable(AutomationRunStatus.SKIPPED_STEP_NO_NEED.name()));
                }
            }
        });
    }

    // #region
    private static void appendAttributeValueIntoTestResultByAttributeName(ITestResult testResult, String attributeName) {
        TestAPIParameter parameter = (TestAPIParameter) getFieldFromTestResult(testResult, "testParameter");
        String key = parameter.getId() + "::" + parameter.getStepId() + "::" + testResult.getMethod().getMethodName() + "::" + attributeName;

        Object objectInfo = testResult.getTestContext().getAttribute(key);
        if (objectInfo != null) {
            if (testResult.getAttribute(key) == null) {
                testResult.setAttribute(key, testResult.getTestContext().getAttribute(key));
            } else {
                Object originValue = testResult.getAttribute(key);
                testResult.removeAttribute(key);
                testResult.setAttribute(key, originValue + "\n" + objectInfo);
            }
        }
        testResult.getTestContext().removeAttribute(key);
    }

    @SneakyThrows
    public static void fillOtherIntoTestResultAttribute(ITestResult res) {
        TestAPIParameter testAPIParameter = (TestAPIParameter) getFieldFromTestResult(res, "testParameter");
        res.setAttribute("scenarioName", testAPIParameter.getIssueKey() + " " + testAPIParameter.getTestCaseDescription());
        res.setAttribute("id", testAPIParameter.getId());
        res.setAttribute("stepId", testAPIParameter.getStepId());
        res.setAttribute("testClassDescription", testAPIParameter.getTestCaseDescription());
        res.setAttribute("testStepDescription", testAPIParameter.getTestStepDescription());
    }

    private static Object getFieldFromTestResult(ITestResult res, String fieldName) {
        try {
            return res.getMethod().getRealClass().getField(fieldName).get(res.getMethod().getInstance());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
// #endregion
}
