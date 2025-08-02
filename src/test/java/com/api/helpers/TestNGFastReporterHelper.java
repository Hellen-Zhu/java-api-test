package com.api.helpers;

import com.api.entities.fast.Element;
import com.api.entities.fast.Feature;
import com.api.entities.fast.Result;
import com.api.entities.fast.FastStep;
import com.api.entities.testng.Step;
import com.api.entities.testng.Scenario;
import com.api.entities.testng.Suite;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TestNGFastReporterHelper {

    public static final String STATUS_PASSED = "passed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_SKIPPED = "skipped";
    public static final String DEFAULT_STEP_NAME = "Default";

    public static Feature[] fetchFeatures(Suite suite, boolean ifDebug) {
        List<Feature> featureList = new ArrayList<>();
        suite.getFeatureScenarioMap().forEach((featureName, xmlSuiteResultSet) -> {
            Feature feature = new Feature();
            feature.setName(featureName);
            feature.setElements(fetchScenarios(xmlSuiteResultSet, ifDebug));
            feature.setPassedCases(xmlSuiteResultSet.stream().filter(xmlSuiteResult -> xmlSuiteResult.getFailedTestResults() == null).collect(Collectors.toList()).size());
            feature.setTotalCases(xmlSuiteResultSet.size());
            featureList.add(feature);
        });
        Collections.sort(featureList, (o1, o2) -> {
            return o1.getName().compareTo(o2.getName());
        });
        return featureList.toArray(new Feature[0]);
    }

    private static Element[] fetchScenarios(Set<Scenario> scenarioSet, boolean ifDebug) {
        List<Element> elementList = new ArrayList<>();
        // Group by scenario and merge steps
        Map<String, Scenario> scenarioXmlSuiteResultMap = new HashMap<>();
        scenarioSet.forEach(scenario -> {
            String scenarioName = scenario.getScenarioName();
            if (scenarioXmlSuiteResultMap.containsKey(scenarioName)) {
                if (scenario.getPassedTestResults() != null)
                    scenarioXmlSuiteResultMap.get(scenarioName).getPassedTestResults().addAll(scenario.getPassedTestResults());
                if (scenario.getFailedTestResults() != null)
                    scenarioXmlSuiteResultMap.get(scenarioName).getFailedTestResults().addAll(scenario.getFailedTestResults());
                if (scenario.getSkippedTestResults() != null)
                    scenarioXmlSuiteResultMap.get(scenarioName).getSkippedTestResults().addAll(scenario.getSkippedTestResults());
            } else {
                scenarioXmlSuiteResultMap.put(scenarioName, scenario);
            }
        });

        scenarioXmlSuiteResultMap.forEach((scenarioName, scenario) -> {
            Element element = new Element();
            element.setName(scenarioName);
            element.setType("Scenario");
            element.setId(String.valueOf(scenarioName.hashCode()));
            long[] runTime = new long[]{0L, 0L};
            getStartAndEndTimeOfTestResultSet(runTime, scenario.getPassedTestResults());
            getStartAndEndTimeOfTestResultSet(runTime, scenario.getFailedTestResults());
            getStartAndEndTimeOfTestResultSet(runTime, scenario.getSkippedTestResults());
            element.setStartRuntime(runTime[0]);
            element.setEndRuntime(runTime[1]);
            element.setDuration(element.getEndRuntime() > element.getStartRuntime() ? (element.getEndRuntime() - element.getStartRuntime()) * 1000000 : 0);
            FastStep[] steps = fetchSteps(scenario, ifDebug);
            element.setFastSteps(steps);
            element.setPassedSteps(fetchPassStepNumber(steps));
            elementList.add(element);
        });

        return elementList.toArray(new Element[0]);
    }

    private static FastStep[] fetchSteps(Scenario scenario, boolean ifDebug) {
        List<Step> allTestResults = new ArrayList<>();
        if (scenario.getPassedTestResults() != null) allTestResults.addAll(scenario.getPassedTestResults());
        if (scenario.getFailedTestResults() != null) allTestResults.addAll(scenario.getFailedTestResults());
        if (scenario.getSkippedTestResults() != null) allTestResults.addAll(scenario.getSkippedTestResults());
        Map<Integer, List<Step>> stepTestMethodMap = allTestResults.stream().collect(Collectors.groupingBy(Step::getStepId));
        List<Step> finalTestResult = sortTestInStep(stepTestMethodMap);
        List<FastStep> stepList = new ArrayList<>();
        for (int i = 0; i < finalTestResult.size(); i++) {
            FastStep step = new FastStep();
            step.setSeqNumber(i + 1);
            String testMethodDescription = StringUtils.isEmpty(finalTestResult.get(i).getTestMethodDescription()) ?
                    finalTestResult.get(i).getTestMethodName() : finalTestResult.get(i).getTestMethodDescription();
            step.setName(testMethodDescription);
            step.setOutput(fetchStepOutput(finalTestResult.get(i), ifDebug));
            step.setResult(fetchResult(finalTestResult.get(i)));
            stepList.add(step);
        }
        return stepList.toArray(new FastStep[0]);
    }

    private static List<Step> sortTestInStep(Map<Integer, List<Step>> stepTestMethodMap) {
        List<Step> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Step>> entry : stepTestMethodMap.entrySet()) {
            List<Step> testResults = entry.getValue();
            List<Step> sorted = testResults.stream()
                    .sorted(Comparator.comparing(Step::getStartMillis))
                    .collect(Collectors.toList());
            entry.setValue(sorted);
            result.addAll(sorted);
        }
        return result;
    }

    private static Result fetchResult(Step testResult) {
        Result result = new Result();
        result.setDuration(testResult.getEndMillis() > testResult.getStartMillis() ? (testResult.getEndMillis() - testResult.getStartMillis()) * 1000000 : 0);
        int status = testResult.getStatus();
        switch (status) {
            case 1:
                result.setStatus(STATUS_PASSED);
                break;
            case 2:
                result.setStatus(STATUS_FAILED);
                break;
            default:
                result.setStatus(STATUS_SKIPPED);
                break;
        }
        String errorMessage = "";
        result.setError_message(errorMessage);
        return result;
    }

    private static String[] fetchStepOutput(Step testResult, boolean ifDebug) {
        List<String> result = new ArrayList<>();
        List<String> debugInfo = testResult.getOutput().getDebugInfo();
        List<String> reportInfo = testResult.getOutput().getReportInfo();

        if (reportInfo != null) result.addAll(reportInfo);
        if (ifDebug && debugInfo != null) result.addAll(debugInfo);

        return result.toArray(new String[0]);
    }

    private static Feature buildFailedFeature(List<Feature> featureList) {
        Feature result = new Feature();
        result.setName("Failed Scenario Set");
        result.setPassedCases(0);
        List<Element> elements = new ArrayList<>();
        for (Feature feature : featureList) {
            for (Element element : feature.getElements()) {
                if (element.getPassedSteps() != element.getFastSteps().length) {
                    Element newElement = element.clone();
                    newElement.setName("[" + feature.getName() + "] " + element.getName());
                    elements.add(newElement);
                }
            }
        }
        result.setElements(elements.toArray(new Element[0]));
        result.setTotalCases(elements.size());
        return result;
    }

    private static int fetchPassStepNumber(FastStep[] steps) {
        int count = 0;
        for (FastStep step : steps) {
            if (step.getResult().getStatus().equalsIgnoreCase("passed")) {
                count++;
            }
        }
        return count;
    }

    private static void getStartAndEndTimeOfTestResultSet(long[] runTime, Set<Step> stepSet) {
        if (stepSet != null) {
            stepSet.forEach(step -> {
                long startTimeOfSet = step.getStartMillis();
                long endTimeOfSet = step.getEndMillis();
                runTime[0] = runTime[0] == 0 ? startTimeOfSet : Long.min(startTimeOfSet, runTime[0]);
                runTime[1] = runTime[1] == 0 ? endTimeOfSet : Long.max(endTimeOfSet, runTime[1]);
            });
        }
    }
}