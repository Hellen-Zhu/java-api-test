package com.api.common.utils;

import com.api.common.enums.XmlSuiteDetailAttribute;
import com.api.data.entities.TestAPIParameter;
import com.api.common.helpers.ListenerHelper;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestContext;

public class ReporterUtil {

    public static void debug(ITestContext testContext, TestAPIParameter parameter, String info) {
        ListenerHelper.appendAttributeValueIntoTestContextByAttributeName(testContext, buildLogKey(parameter, XmlSuiteDetailAttribute.DEBUG_LOG.getName()),
                StringUtils.abbreviate(info, 2048));
    }

    public static void report(ITestContext testContext, TestAPIParameter parameter, String info) {
        ListenerHelper.appendAttributeValueIntoTestContextByAttributeName(testContext, buildLogKey(parameter, XmlSuiteDetailAttribute.REPORT_LOG.getName()),
                StringUtils.abbreviate(info, 2048));
    }

    public static String buildLogKey(TestAPIParameter parameter, String typeName) {
        // typeName = XmlSuiteDetailAttribute.DEBUG_LOG.getName();
        return parameter.getId() + "::" + parameter.getStepId() + "::" + parameter.getCurrentMethodName() + "::" + typeName;
    }
}