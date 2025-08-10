package com.api.service.impl;

import com.api.service.TestNGService;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.testng.TestNG;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestNGServiceImpl implements TestNGService {

    private static final String XMLFILE = "testng.xml";

    @Override
    @Async("testNgAsyncExecutor")
    public void runWithAsync(JSONObject requestObject) {
        try {
            triggerByCode(requestObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // Implement triggerByCode logic directly
    private void triggerByCode(JSONObject requestObject) {
        setSystemProperties();
        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        testNG.setXmlSuites(fetchDefaultXmlSuite(requestObject));
        testNG.run();
        testNG.getReporters();
    }

    private void setSystemProperties() {
        System.setProperty("spring.application.name", "eh-api");
        System.setProperty("spring.cloud.config.name", "mondo-testng-api-service");
        System.setProperty("server.home", ".");
        System.setProperty("spring.cloud.bootstrap.location", "./eh-bootstrap/config/");
        System.setProperty("spring.profiles.active", "apacuat");
        System.setProperty("javax.net.ssl.trustStore", "cacert/cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
    }

    private List<XmlSuite> fetchDefaultXmlSuite(JSONObject requestObject) {
        Map<String, String> propertiesMap = new HashMap<String, String>() {{
            for (String s : requestObject.keySet()) {
                put(s, requestObject.get(s).toString());
            }
        }};

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(XMLFILE);
        if (inputStream == null) {
            System.out.println("File " + XMLFILE + " not found");
        }

        SuiteXmlParser suiteXmlParser = new SuiteXmlParser();
        List<XmlSuite> suites = new ArrayList<>();
        XmlSuite xmlSuite = suiteXmlParser.parse(XMLFILE, inputStream, true);
        suites.add(xmlSuite);
        xmlSuite.setParameters(propertiesMap);
        return suites;
    }
}