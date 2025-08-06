package com.api.listeners;

import com.api.annotations.CommentAnnotation;
import com.api.entities.fast.Feature;
import com.api.enums.AutomationRunStatus;
import com.api.entities.testng.*;
import com.api.helpers.TestNGFastReporterHelper;
import com.api.utils.DataTypeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;


import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.reporters.XMLConstants;
import org.testng.reporters.XMLStringBuffer;
import org.testng.util.TimeUtils;
import org.testng.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;

@Slf4j
public class TestNGXmlSuiteListener implements ISuiteListener {
    public static final String SCENARIO_NAME = "scenarioName";

    @Override
    public void onFinish(ISuite suite) {
        try {
            Set<Suite> suiteSet = buildXmlSuiteDetailSet(suite);
            Set<JSONObject> xmlSuiteDetailObjectSet = buildXmlSuiteDetailObjectSet(suiteSet);
//            boolean isDebug = suite.getXmlSuite().getAllParameters().get(XmlSuiteDetailAttribute.IS_DEBUG.getName()) == null ?
//                    false : Boolean.parseBoolean(suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.IS_DEBUG.getName()));
//            if (!isDebug) {
//                String baseUrl = suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.AUTOMATION_TOOL_URL.getName());
//
//                // 添加超时处理的网络请求，避免进程挂起
//                try {
//                    uploadDashboard(baseUrl, xmlSuiteDetailObjectSet);
//                    uploadTestCycle(baseUrl, suite);
//                } catch (Exception networkEx) {
//                    System.err.println("Warning: Network upload failed, but test will continue: " + networkEx.getMessage());
//                    // 不阻止测试完成，只记录错误
//                }
//            }
            log.info("xmlSuiteDetailObjectSet = {}", xmlSuiteDetailObjectSet);
        } catch (Exception e) {
            System.err.println("Error in TestNG suite finish: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 确保测试进程能够正常结束
            System.out.println("TestNG suite finish processing completed");
        }
    }

    @CommentAnnotation(description = "Convert ISuite to XmlSuiteDetail, and separate for releaseVersions")
    protected Set<Suite> buildXmlSuiteDetailSet(ISuite suite) {
        // fetch basic XmlSuiteDetail
        Suite xmlSuiteDetail = new Suite();
        xmlSuiteDetail.setSuiteName(suite.getName());
        xmlSuiteDetail.setSuiteParameters(suite.getXmlSuite().getParameters());
        xmlSuiteDetail.setFeatureScenarioMap(buildFeatureScenarioMap(suite.getResults()));
        // build result
        return buildFinalXmlSuiteDetailSet(xmlSuiteDetail);
    }

    protected Set<JSONObject> buildXmlSuiteDetailObjectSet(Set<Suite> suiteSet) {
        Set<JSONObject> result = new HashSet<>();
        suiteSet.forEach(x -> result.add(JSON.parseObject(filterOffNonBreakSpaceFromHTML(JSON.toJSONString(x)))));
        return result;
    }

    protected Set<Suite> buildFinalXmlSuiteDetailSet(Suite suite) {
        Map<String, String> suiteParameters = suite.getSuiteParameters();
        Set<Suite> result = new HashSet<>();
        String[] releaseVersionSet = suiteParameters.get(XmlSuiteDetailAttribute.ACTUAL_FIXVERSION.getName()).split(",");

        if (suiteParameters.get(XmlSuiteDetailAttribute.TEST_CYCLE.getName()) == null) {
            suiteParameters.put(XmlSuiteDetailAttribute.TEST_CYCLE.getName(), suiteParameters.get(XmlSuiteDetailAttribute.SUITE.getName()));
        } else {
            if (suiteParameters.get(XmlSuiteDetailAttribute.TEST_CYCLE.getName()).isEmpty()) {
                suiteParameters.put(XmlSuiteDetailAttribute.TEST_CYCLE.getName(), suiteParameters.get(XmlSuiteDetailAttribute.SUITE.getName()));
            } else {
                suiteParameters.put(XmlSuiteDetailAttribute.TEST_CYCLE.getName(), suiteParameters.get(XmlSuiteDetailAttribute.TEST_CYCLE.getName()));
            }
        }

        for (String s : releaseVersionSet) {
            Suite detail = suite.clone();
            Map<String, String> sp = new HashMap<>(suite.getSuiteParameters());
            sp.put(XmlSuiteDetailAttribute.ACTUAL_FIXVERSION.getName(), s);
            detail.setSuiteParameters(sp);
            result.add(detail);
        }
        return result;
    }

    protected Map<String, Set<Scenario>> buildFeatureScenarioMap(Map<String, ISuiteResult> results) {
        Map<String, Set<Scenario>> result = new HashMap<>();
        results.forEach((featureName, iSuiteResult) -> {
            if (iSuiteResult.getTestContext().getPassedTests().size() == 0
                    && iSuiteResult.getTestContext().getFailedTests().size() == 0
                    && iSuiteResult.getTestContext().getSkippedTests().size() == 0) {
                // do nothing
            }
            Set<Scenario> scenarioSet = getScenarioSet(iSuiteResult.getTestContext());
            result.put(featureName, scenarioSet);
        });
        return result;
    }

    protected Set<Scenario> getScenarioSet(ITestContext iTestContext) {
        Set<Scenario> result = new HashSet<>();
        Map<String, Scenario> scenarioNameMap = new HashMap<>();

        // merge for passCases when caseName is the same
        iTestContext.getPassedTests().getAllResults().forEach(ts -> {
            Step step = buildXmlTestResult(ts);
            String scenarioName = ts.getAttribute(SCENARIO_NAME) == null ?
                    step.getTestClassName() + "->" + step.getTestMethodName() : ts.getAttribute(SCENARIO_NAME).toString();
            if (scenarioNameMap.containsKey(scenarioName)) {
                if (scenarioNameMap.get(scenarioName).getPassedTestResults() != null) {
                    scenarioNameMap.get(scenarioName).getPassedTestResults().add(step);
                } else {
                    Set<Step> passSet = new HashSet<>();
                    passSet.add(step);
                    scenarioNameMap.get(scenarioName).setPassedTestResults(passSet);
                }
            } else {
                Set<Step> passSet = new HashSet<>();
                passSet.add(step);
                Scenario scenario = new Scenario();
                scenario.setPassedTestResults(passSet);
                scenarioNameMap.put(scenarioName, scenario);
            }
        });

        // merge for failedCases when caseName is the same
        iTestContext.getFailedTests().getAllResults().forEach(ts -> {
            Step step = buildXmlTestResult(ts);
            String scenarioName = ts.getAttribute(SCENARIO_NAME) == null ?
                    step.getTestClassName() + "->" + step.getTestMethodName() : ts.getAttribute(SCENARIO_NAME).toString();
            if (scenarioNameMap.containsKey(scenarioName)) {
                if (scenarioNameMap.get(scenarioName).getFailedTestResults() != null) {
                    scenarioNameMap.get(scenarioName).getFailedTestResults().add(step);
                } else {
                    Set<Step> failSet = new HashSet<>();
                    failSet.add(step);
                    scenarioNameMap.get(scenarioName).setFailedTestResults(failSet);
                }
            } else {
                Set<Step> failSet = new HashSet<>();
                failSet.add(step);
                Scenario scenario = new Scenario();
                scenario.setFailedTestResults(failSet);
                scenarioNameMap.put(scenarioName, scenario);
            }
        });

        // merge for skipCases when caseName is the same
        iTestContext.getSkippedTests().getAllResults().forEach(ts -> {
            Step step = buildXmlTestResult(ts);
            if(step.getThrowable()!=null && StringUtils.containsIgnoreCase(step.getThrowable().getMessage(),AutomationRunStatus.SKIPPED_METHOD_NO_NEED.name())){
                String scenarioName = ts.getAttribute(SCENARIO_NAME) == null ?
                        step.getTestClassName() + "->" + step.getTestMethodName() : ts.getAttribute(SCENARIO_NAME).toString();
                if(scenarioNameMap.containsKey(scenarioName)){
                    if(scenarioNameMap.get(scenarioName).getSkippedTestResults() != null) {
                        scenarioNameMap.get(scenarioName).getSkippedTestResults().add(step);
                    } else {
                        Set<Step> skipSet = new HashSet<>();
                        skipSet.add(step);
                        scenarioNameMap.get(scenarioName).setSkippedTestResults(skipSet);
                    }
                } else {
                    Set<Step> skipSet = new HashSet<>();
                    skipSet.add(step);
                    Scenario scenario = new Scenario();
                    scenario.setSkippedTestResults(skipSet);
                    scenarioNameMap.put(scenarioName,scenario);
                }
            }
        });

        // build result
        for (Map.Entry<String, Scenario> entry : scenarioNameMap.entrySet()) {
            Scenario scenario = entry.getValue();
            scenario.setScenarioName(entry.getKey());
            result.add(scenario);
        }
        return result;
    }

    protected Step buildXmlTestResult(ITestResult ts) {
        Step result = new Step();
        String[] testClassPath = ts.getInstance().toString().split("@");
        StepOutput output = new StepOutput();
        String debugKey = ts.getAttribute("id") + "::" + ts.getAttribute("stepId") + "::" + ts.getMethod().getMethodName() + "::" + XmlSuiteDetailAttribute.DEBUG_LOG.getName();
        String reportKey = ts.getAttribute("id") + "::" + ts.getAttribute("stepId") + "::" + ts.getMethod().getMethodName() + "::" + XmlSuiteDetailAttribute.REPORT_LOG.getName();
        if (ts.getAttribute(debugKey) != null) output.setDebugInfo(DataTypeUtil.splitStringToListByPunctuation(ts.getAttribute(debugKey).toString(), "\n"));
        if (ts.getAttribute(reportKey) != null) output.setReportInfo(DataTypeUtil.splitStringToListByPunctuation(ts.getAttribute(reportKey).toString(), "\n"));

        result.setTestClassName(testClassPath[testClassPath.length - 1].split("@")[0]);
        result.setTestMethodName(ts.getName());
        result.setStatus(ts.getStatus());
        result.setStepId(ts.getAttribute("stepId") == null ? 1 : Integer.parseInt(ts.getAttribute("stepId").toString()));
        String testCaseDescription = ts.getAttribute("testCaseDescription") != null ? ts.getAttribute("testCaseDescription").toString() : "";
        String testMethodDescription = ts.getAttribute("testStepDescription") != null ? ts.getAttribute("testStepDescription").toString() + "->" + ts.getMethod().getMethodName() : ts.getMethod().getMethodName();
        result.setTestClassDescription(testCaseDescription);
        result.setTestMethodDescription(testMethodDescription);
        result.setOutput(output);
        result.setThrowable(new StepThrowable(ts.getThrowable()));
        result.setStartMillis(ts.getStartMillis());
        result.setEndMillis(ts.getEndMillis());
        return result;
    }

    protected void uploadDashboard(String baseUrl, Set<JSONObject> suiteObjectSet) {
        suiteObjectSet.forEach(suiteObject -> {
            try {
                Suite suiteDetail = JSON.parseObject(suiteObject.toString(), Suite.class);
                Feature[] features = TestNGFastReporterHelper.fetchFeatures(suiteDetail, Boolean.parseBoolean(suiteDetail.getSuiteParameters().get(XmlSuiteDetailAttribute.IS_DEBUG.getName())));
                JSONObject request = new JSONObject();
                request.put("features", features);
                request.put("config", suiteDetail.getSuiteParameters());
                saveTempOnLocal(suiteDetail.getSuiteName(), request);
                
                // 添加超时设置，避免网络请求挂起
                given().contentType(ContentType.JSON)
                        .config(RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", 30000)
                                .setParam("http.socket.timeout", 30000)))
                        .baseUri(baseUrl)
                        .body(request)
                        .when()
                        .post("/automation/dashboard/uploadByConfigAndFeatureArray");
            } catch (Exception e) {
                System.err.println("Failed to upload dashboard for suite: " + e.getMessage());
                // 继续处理下一个，不中断整个流程
            }
        });
    }

    protected void uploadTestCycle(String baseUrl, ISuite suite) {
        try {
            String xmlContent = generateReportByTestSuite(suite.getName(), suite.getResults());
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put(XmlSuiteDetailAttribute.PROJECT_KEY.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.JIRA_KEY.getName()));
            paramMap.put(XmlSuiteDetailAttribute.ACTUAL_FIXVERSION.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.ACTUAL_FIXVERSION.getName()));
            paramMap.put(XmlSuiteDetailAttribute.TEST_CYCLE.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.TEST_CYCLE.getName()));
            paramMap.put("automationTool", "Junit");
            paramMap.put(XmlSuiteDetailAttribute.JIRA_TOKEN.getName(), suite.getXmlSuite().getParameter(XmlSuiteDetailAttribute.JIRA_TOKEN.getName()));
            paramMap.put("testResult", xmlContent);
            
            // 添加超时设置，避免网络请求挂起
            given().contentType(ContentType.JSON)
                    .config(RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                            .setParam("http.connection.timeout", 30000)
                            .setParam("http.socket.timeout", 30000)))
                    .baseUri(baseUrl)
                    .body(paramMap)
                    .when()
                    .post("/automation/testcycle/uploadTestAutomation");
        } catch (Exception e) {
            System.err.println("Failed to upload test cycle: " + e.getMessage());
            // 不阻止测试完成，只记录错误
        }
    }

    protected String generateReportByTestSuite(String suiteName, Map<String, ISuiteResult> context) {
        TestSuite testSuite = mergeIntoOneTestSuite(suiteName, context);
        XMLStringBuffer document = new XMLStringBuffer();
        document.addComment("Generated by " + getClass().getName());
        Properties attrs = new Properties();
        attrs.setProperty(XMLConstants.ATTR_NAME, suiteName);
        attrs.setProperty(XMLConstants.ATTR_TESTS, testSuite.getTests());
        attrs.setProperty(XMLConstants.ATTR_TIME, testSuite.getTime());
        attrs.setProperty(XMLConstants.ATTR_TIMESTAMP, formattedTime());
        document.push(XMLConstants.TESTSUITE, attrs);
        testSuite.getTestCaseMap().forEach((name, testCase) -> {
            Properties atr = new Properties();
            atr.setProperty(XMLConstants.ATTR_NAME, name);
            atr.setProperty(XMLConstants.ATTR_CLASSNAME, testCase.getClassName());
            atr.setProperty(XMLConstants.ATTR_TIME, Double.toString((double) testCase.getTime() / 1000));
            if (testCase.isIfPass()) {
                document.addEmptyElement(XMLConstants.TESTCASE, atr);
            } else {
                document.push(XMLConstants.TESTCASE, atr);
                document.addEmptyElement(XMLConstants.FAILURE);
                document.pop();
            }
        });
        document.pop();
        return document.toXML();
    }

    protected TestSuite mergeIntoOneTestSuite(String suiteName, Map<String, ISuiteResult> context) {
        Set<TestSuite> testSuiteSet = new HashSet<>();
        context.forEach((name, suiteResult) -> testSuiteSet.add(genrateTestSuite(suiteResult.getTestContext())));
        AtomicInteger testCount = new AtomicInteger();
        AtomicReference<Double> suiteTime = new AtomicReference<>(0.0);
        Map<String, TestCase> testCaseMap = new HashMap<>();
        AtomicReference<String> timeStamp = new AtomicReference<>("");
        testSuiteSet.forEach(ts -> {
            testCount.addAndGet(Integer.parseInt(ts.getTests()));
            suiteTime.updateAndGet(v -> v + Double.parseDouble(ts.getTime()));
            testCaseMap.putAll(ts.getTestCaseMap());
            timeStamp.set(ts.getTimestamp());
        });

        TestSuite testSuite = new TestSuite();
        testSuite.setTests(testCount.get() + "");
        testSuite.setName(suiteName);
        testSuite.setTime(suiteTime + "");
        testSuite.setTimestamp(timeStamp.get());
        testSuite.setTestCaseMap(testCaseMap);
        return testSuite;
    }

    protected TestSuite genrateTestSuite(ITestContext context) {
        TestSuite testSuite = new TestSuite();
        Map<String, TestCase> testCaseMap = new HashMap<>();
        Set<ITestResult> allTestResultSet = new HashSet<>();
        if (context.getSkippedTests() != null) allTestResultSet.addAll(context.getSkippedTests().getAllResults());
        if (context.getPassedTests() != null) allTestResultSet.addAll(context.getPassedTests().getAllResults());
        if (context.getFailedTests() != null) allTestResultSet.addAll(context.getFailedTests().getAllResults());
        // ... Logic to fill testCaseMap from allTestResultSet would go here
        testSuite.setTimestamp(formattedTime());
        testSuite.setTime(Double.toString((context.getEndDate().getTime() - context.getStartDate().getTime()) / 1000.0));
        testSuite.setTestCaseMap(testCaseMap);
        testSuite.setName(context.getCurrentXmlTest().getName());
        testSuite.setTests(Integer.toString(testCaseMap.size()));
        return testSuite;
    }

    protected void saveTempOnLocal(String suiteName, JSONObject request) {
        File targetDir = new File("test-output");
        String fileName = suiteName + TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(System.currentTimeMillis(), "_yyyyMMdd_HHmmss");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        File requestFile = new File(targetDir, fileName + ".json");
        try (FileWriter fileWriter = new FileWriter(requestFile)) {
            fileWriter.write(request.toJSONString());
        } catch (IOException e) {
            System.err.println("Failed to save request object to file " + e.getMessage());
        }
    }

    protected String formattedTime() {
        return TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(System.currentTimeMillis(), "yyyy-MM-dd'T'HH:mm:ss z");
    }

    protected void fillTestCaseMap(Map<String, TestCase> testCaseMap, Set<ITestResult> testResultSet) {
        for (ITestResult tr : testResultSet) {
            long elapsedTimeMillis = tr.getEndMillis() - tr.getStartMillis();
            String testCaseDescription = tr.getName().split("@")[0];
            String realClassName = tr.getMethod().getRealClass().getName();
            String className = realClassName.substring(realClassName.lastIndexOf(".") + 1);
            boolean ifPass = (tr.getStatus() == ITestResult.SUCCESS || (
                    tr.getStatus() == ITestResult.SKIP &&
                            (tr.getThrowable().getMessage().equalsIgnoreCase(AutomationRunStatus.SKIPPED_METHOD_NO_NEED.name()) ||
                                    tr.getThrowable().getMessage().equalsIgnoreCase(AutomationRunStatus.SKIPPED_STEP_NO_NEED.name()))
            ));

            if (testCaseMap.containsKey(testCaseDescription)) {
                long originTime = testCaseMap.get(testCaseDescription).getTime();
                boolean status = testCaseMap.get(testCaseDescription).isIfPass() && ifPass;
                testCaseMap.get(testCaseDescription).setClassName(className);
                testCaseMap.get(testCaseDescription).setTime(originTime + elapsedTimeMillis);
                testCaseMap.get(testCaseDescription).setIfPass(status);
            } else {
                TestCase testCase = new TestCase();
                testCase.setClassName(className);
                testCase.setName(testCaseDescription);
                testCase.setTime(elapsedTimeMillis);
                testCase.setIfPass(ifPass);
                testCaseMap.put(testCaseDescription, testCase);
            }
        }
    }

    protected String filterOffNonBreakSpaceFromHTML(String json) {
        // remove NO-BREAK SPACE C2 A0
        String noBreakSpace = new String(json.getBytes(), StandardCharsets.UTF_8);
        byte[] bytes3 = new byte[2];
        bytes3[0] = (byte) 0xC2;
        bytes3[1] = (byte) 0xA0;
        String c2a0space = new String(bytes3, StandardCharsets.UTF_8);
        Pattern p = Pattern.compile(c2a0space);
        Matcher m = p.matcher(noBreakSpace);
        noBreakSpace = m.replaceAll("");
        return noBreakSpace.trim();
    }
}