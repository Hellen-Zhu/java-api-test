package com.api;

import com.alibaba.fastjson2.JSONObject;
import org.testng.TestNG;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiAutoTestApplicationTests {
    static String XMLFILE = "testng.xml";

    public static void main(String[] args) {
        runTestNG();
    }


    private static void runTestNG() {
        triggerByCode(new JSONObject());
    }

    public static void triggerByCode(JSONObject requestObject) {
        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        testNG.setXmlSuites(fetchDefaultXmlSuite(requestObject));
        testNG.run();
        testNG.getReporters();
    }

    private static List<XmlSuite> fetchDefaultXmlSuite(JSONObject requestObject) {
        Map<String, String> propertiesMap = new HashMap<>() {{
            for (String s : requestObject.keySet()) {
                put(s, requestObject.get(s).toString());
            }
        }};

        InputStream inputStream = ApiAutoTestApplicationTests.class.getClassLoader().getResourceAsStream(XMLFILE);
        SuiteXmlParser suiteXmlParser = new SuiteXmlParser();

        List<XmlSuite> suites = new ArrayList<>();
        XmlSuite xmlSuite = suiteXmlParser.parse(XMLFILE, inputStream, true);
        suites.add(xmlSuite);
        xmlSuite.setParameters(propertiesMap);
        return suites;
    }
}