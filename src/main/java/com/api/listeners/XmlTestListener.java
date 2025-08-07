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

//    它会准备测试用例的详情 (prepareTestCaseDetailForMainTestMethod)，
//    并且用测试用例的描述来改变测试的名称 (changeTestNameWithTestCaseDescription)。这让你的测试报告更具可读性。
    @SneakyThrows
    @Override
    public void onTestStart(ITestResult res) {
        ListenerHelper.prepareTestCaseDetailForMainTestMethod(res);
        ListenerHelper.changeTestNameWithTestCaseDescription(res);
    }

//    无论测试结果如何，它都会把日志 (fillLogIntoTestResultAttribute)
//    和其他信息 (fillOtherIntoTestResultAttribute) 填充到测试结果中，并更新测试用例的进度 (addTestCaseProgress)。
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