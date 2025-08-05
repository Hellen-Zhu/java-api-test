package com.api.controller;

//import citi.lifqa.common.entities.testng.XmlSuiteDetailAttribute;
import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.testng.TestNG;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("automation")
@Slf4j
public class APIController {

    static String XMLFILE = "testng.xml";

    @PostMapping("/module")
    public void runBasedOnModule(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On Module");
        if (apiAutomationRequest.getModule() == null) {
            log.error("Miss Field value for Module");
        }
    }

    @PostMapping("/component")
    public void runBasedOnComponent(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On Component");
        if (apiAutomationRequest.getComponent() == null) {
            log.error("Miss Field value for Component");
        }
    }

    @PostMapping("/feature")
    public void runBasedOnFeature(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On Feature");
        if (apiAutomationRequest.getFeature() == null) {
            log.error("Miss Field value for Feature");
        }
    }

    @PostMapping("/scenario")
    public void runBasedOnScenario(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On Scenario");
        if (apiAutomationRequest.getScenario() == null) {
            log.error("Miss Field value for Scenario");
        }
    }

    @PostMapping("/service")
    public void runBasedOnService(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On Service");
        if (apiAutomationRequest.getService() == null) {
            log.error("Miss Field value for Service");
        }
    }

    @PostMapping("/label")
    public void runBasedOnLabel(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On Label");
        if (apiAutomationRequest.getLabel() == null) {
            log.error("Miss Field value for Label");
        }
    }

    @PostMapping("/issueKey")
    public void runBasedOnIssueKey(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On IssueKey");
        if (apiAutomationRequest.getIssueKey() == null) {
            log.error("Miss Field value for IssueKey");
        }
    }

    @PostMapping("/id")
    public void runBasedOnId(@RequestBody APIAutomationRequest apiAutomationRequest) {
        log.info("Start to run based On Id");
        if (apiAutomationRequest.getId() == null) {
            log.error("Miss Field value for Id");
        }

        // 构建 XML 执行配置参数
        JSONObject requestObject = new JSONObject();
//        requestObject.put(XmlSuiteDetailAttribute.ID_LIST.getName(), apiAutomationRequest.getId());
//        requestObject.put(XmlSuiteDetailAttribute.IS_DEBUG.getName(), true);
//        requestObject.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(), Boolean.FALSE.toString());
//        requestObject.put(XmlSuiteDetailAttribute.RUN_ID.getName(), UlidCreator.getUlid().toString());

        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        testNG.setXmlSuites(fetchDefaultXmlSuite(requestObject));
        testNG.run();
        testNG.getReporters();
    }

    private static List<XmlSuite> fetchDefaultXmlSuite(JSONObject requestObject) {
        Map<String, String> propertiesMap = new HashMap<String, String>() {{
            for (String s : requestObject.keySet()) {
                put(s, requestObject.get(s).toString());
            }
        }};

        InputStream inputStream = APIController.class.getClassLoader().getResourceAsStream(XMLFILE);
        if (inputStream == null) log.info("File " + XMLFILE + " not found");

        SuiteXmlParser suiteXmlParser = new SuiteXmlParser();
        List<XmlSuite> suites = new ArrayList<>();
        XmlSuite xmlSuite = suiteXmlParser.parse(XMLFILE, inputStream, true);
        suites.add(xmlSuite);
        xmlSuite.setParameters(propertiesMap);

        return suites;
    }
}