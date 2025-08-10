package com.api.common.utils;
import com.api.common.enums.APIStatus;
import com.api.common.enums.AutomationRunStatus;
import com.api.data.entities.TestAPIParameter;
import com.api.common.helpers.ParameterHelper;
import org.testng.SkipException;

public class SkipTestUtil {
    public static void ifSkipStepForDependOn(TestAPIParameter parameter) {
        String runId = parameter.getRunId();
        String key = runId + "_" + parameter.getId() + "_" + parameter.getStepId() + " -> ifRun";
        if (MemoryCacheUtil.getInstance().getIfPresent(key) != null && MemoryCacheUtil.getInstance().get(key).equals(APIStatus.FAIL)) {
            throw new SkipException(AutomationRunStatus.SKIPPED_STEP_NO_NEED.name());
        } else if (MemoryCacheUtil.getInstance().getIfPresent(key) != null && MemoryCacheUtil.getInstance().get(key).equals(APIStatus.NONE)) {
            throw new SkipException(AutomationRunStatus.SKIPPED_METHOD_NO_NEED.name());
        } else {
            APIUtil.beforeStep(parameter);
            ParameterHelper.fillBasicParameterDetails(parameter);
        }
    }

    public static void ifSkipMethodForNoNeed(boolean ifSkip) {
        if (ifSkip) {
            throw new SkipException(AutomationRunStatus.SKIPPED_METHOD_NO_NEED.name());
        }
    }
}