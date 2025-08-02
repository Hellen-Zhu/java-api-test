package com.api.listeners;

import com.api.helpers.ListenerHelper;
import lombok.extern.slf4j.Slf4j;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;
import java.util.Map;

@Slf4j
public class AlterListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        Map<String, String> propertiesMap = suites.get(0).getParameters();

        log.info("Initial Testng Run Parameters : " + propertiesMap);

        // clear xmlSuite
        XmlTest xmlTest = suites.get(0).getTests().get(0);
        suites.clear();

        // fetch final parameters by default + database + system properties
        Map<String, Map<String, String>> suiteAndFinalSuiteParameterMap =
                ListenerHelper.buildSuiteAndFinalSuiteParameterMapBetweenOriginalAndAutoConfiguration(propertiesMap);

        ListenerHelper.createXmlSuites(suites, xmlTest, suiteAndFinalSuiteParameterMap);

        log.info("Pending to run suite List : " + suiteAndFinalSuiteParameterMap.keySet());
    }
}