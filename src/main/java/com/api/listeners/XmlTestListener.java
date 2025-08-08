package com.api.listeners;

import com.api.enums.AutomationRunStatus;
import com.api.helpers.ListenerHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class XmlTestListener implements ITestListener {

    private final Map<String, int[]> caseInstanceAndTestMap = new HashMap<>();

    @Override
    public void onStart(ITestContext res) {
        synchronized (this) {
            ListenerHelper.initialCaseInstanceMap(res, this.caseInstanceAndTestMap);
        }
    }

    @Override
    public void onFinish(ITestContext res) {
        // No-op
    }

//    It prepares the details of the test case (prepareTestCaseDetailForMainTestMethod)
//    and changes the test name with the test case description (changeTestNameWithTestCaseDescription)
//    to make the report more readable.
    @SneakyThrows
    @Override
    public void onTestStart(ITestResult res) {
        ListenerHelper.prepareTestCaseDetailForMainTestMethod(res);
        ListenerHelper.changeTestNameWithTestCaseDescription(res);
    }

//    Regardless of the result, it fills logs (fillLogIntoTestResultAttribute)
//    and other information (fillOtherIntoTestResultAttribute) into the test result,
//    and updates test case progress (addTestCaseProgress).
    @SneakyThrows
    @Override
    public void onTestSuccess(ITestResult res) {
        ListenerHelper.fillLogIntoTestResultAttribute(res);
        ListenerHelper.fillOtherIntoTestResultAttribute(res);
        ListenerHelper.addTestCaseProgress(res, this, this.caseInstanceAndTestMap, true);
    }

    @SneakyThrows
    @Override
    public void onTestFailure(ITestResult res) {
        ListenerHelper.fillLogIntoTestResultAttribute(res);
        ListenerHelper.fillOtherIntoTestResultAttribute(res);
        ListenerHelper.addTestCaseProgress(res, this, this.caseInstanceAndTestMap, false);
    }

    @SneakyThrows
    @Override
    public void onTestSkipped(ITestResult res) {
        ListenerHelper.fillLogIntoTestResultAttribute(res);
        ListenerHelper.fillOtherIntoTestResultAttribute(res);
        ListenerHelper.resetSkipReasonForNoNeedCheckByDependOnMethod(res);

        String skipCauseReason = res.getThrowable() != null ? res.getThrowable().toString() : "";
        boolean passedFlag = skipCauseReason.contains(AutomationRunStatus.SKIPPED_METHOD_NO_NEED.name()) ||
                skipCauseReason.contains(AutomationRunStatus.SKIPPED_STEP_NO_NEED.name());

        ListenerHelper.addTestCaseProgress(res, this, this.caseInstanceAndTestMap, passedFlag);
    }
}