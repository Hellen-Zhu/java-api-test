package com.api.common.listeners;

import com.api.common.annotations.CommentAnnotation;
import com.api.common.enums.AutomationRunStatus;
import com.api.common.enums.XmlSuiteDetailAttribute;
import com.api.data.entities.testng.*;
import com.api.common.utils.DataTypeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

@Slf4j
public class TestNGXmlSuiteListener implements ISuiteListener {
    public static final String SCENARIO_NAME = "scenarioName";

    @Override
    public void onFinish(ISuite suite) {
        try {
            Set<Suite> suiteSet = buildXmlSuiteDetailSet(suite);
            Set<JSONObject> xmlSuiteDetailObjectSet = buildXmlSuiteDetailObjectSet(suiteSet);
            log.info("xmlSuiteDetailObjectSet = {}", xmlSuiteDetailObjectSet);
        } catch (Exception e) {
            System.err.println("Error in TestNG suite finish: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure the test process can end normally
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

        // TEST_CYCLE logic removed as it's no longer needed

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