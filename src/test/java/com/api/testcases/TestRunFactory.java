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

//    这个工厂的核心思想是：收集 testng.xml 中定义的所有筛选条件，
//    将它们打包，然后交给一个专门的“数据提供者”(StaticDataProvider)去智能地创建最终要执行的测试用例。
    @Factory
    @Parameters({"component", "sanityOnly", "scenario", "ids", "runId", "labels"}) // from xmlTest.parameter
    public Object[] createInstances(String component, String sanityOnly, String scenario, String ids, String runId, @Optional("") String labels) {
        try {
            String[] idArray = StringUtils.isEmpty(ids) ? new String[0] : ids.split(",");
            String[] labelArray = StringUtils.isEmpty(labels) ? new String[0] : labels.split(",");

            Map<String, Object> automationParamsMap = new HashMap<>();
            automationParamsMap.put(XmlSuiteDetailAttribute.COMPONENT.getName(), component);
            automationParamsMap.put(XmlSuiteDetailAttribute.SCENARIO.getName(), scenario);
            automationParamsMap.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
            automationParamsMap.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(), sanityOnly);

            if(idArray.length != 0) automationParamsMap.put(XmlSuiteDetailAttribute.ID_LIST.getName(), idArray);
            if(labelArray.length != 0) automationParamsMap.put(XmlSuiteDetailAttribute.LABEL_LIST.getName(), labelArray);

            return StaticDataProvider.getTestNGObjectArrayForFactoryByService(automationParamsMap);

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return null;
    }
}