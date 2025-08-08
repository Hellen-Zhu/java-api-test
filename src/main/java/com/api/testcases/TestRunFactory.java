package com.api.testcases;

import com.api.entities.testng.XmlSuiteDetailAttribute;
import com.api.helpers.StaticDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Listeners(MethodInterceptor.class)
public class TestRunFactory {

//    Core idea of this factory: collect all filter conditions defined in testng.xml,
//    package them, and delegate to a dedicated data provider (StaticDataProvider)
//    to intelligently create the final set of test cases to execute.
    @Factory
    @Parameters({"component", "sanityOnly", "scenario", "ids", "runId", "labels"}) // from xmlTest.parameter
    public Object[] createInstances(String component, String sanityOnly, String scenario, String ids, String runId, @Optional("") String labels) {
        try {
            log.info("TestRunFactory.createInstances - received labels parameter: '{}'", labels);
            
            String[] idArray = StringUtils.isEmpty(ids) ? new String[0] : ids.split(",");
            String[] labelArray = StringUtils.isEmpty(labels) ? new String[0] : labels.split(",");
            
            log.info("TestRunFactory.createInstances - labelArray after split: {}", (Object) labelArray);

            Map<String, Object> automationParamsMap = new HashMap<>();
            automationParamsMap.put(XmlSuiteDetailAttribute.COMPONENT.getName(), component);
            automationParamsMap.put(XmlSuiteDetailAttribute.SCENARIO.getName(), scenario);
            automationParamsMap.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
            automationParamsMap.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(), sanityOnly);

            log.info("automationParamsMap: {}", automationParamsMap);

            if(idArray.length != 0) automationParamsMap.put(XmlSuiteDetailAttribute.ID_LIST.getName(), idArray);
            if(labelArray.length != 0) automationParamsMap.put(XmlSuiteDetailAttribute.LABEL_LIST.getName(), labelArray);
            
            log.info("TestRunFactory.createInstances - final automationParamsMap: {}", automationParamsMap);

            return StaticDataProvider.getTestNGObjectArrayForFactoryByService(automationParamsMap);

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return null;
    }
}