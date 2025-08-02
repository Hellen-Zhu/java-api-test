package com.api.listeners;

import com.api.entities.testng.XmlSuiteDetailAttribute;
import com.api.helpers.ListenerHelper;
import com.api.utils.MemoryCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.testng.ISuite;

@Slf4j
public class XmlSuiteListener extends TestNGXmlSuiteListener {

    @Override
    public void onStart(ISuite suite) {
        log.info("[TestNG] suite " + suite.getParameter(XmlSuiteDetailAttribute.COMPONENT.getName()) + " starts to run.");
        ListenerHelper.initialAutoProgressForSuiteRun(suite);
        ListenerHelper.initialAutoTestNGResultForSingleIdRun(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        MemoryCacheUtil.getInstance().invalidateAll();
        ListenerHelper.endAutoProgressForSuiteRun(suite);
        ListenerHelper.sendCompleteSignalForModule(suite);
        ListenerHelper.endAutoTestNGResultForSingleIdRun(suite, buildFeatureScenarioMap(suite.getResults()));
        super.onFinish(suite);
    }
}