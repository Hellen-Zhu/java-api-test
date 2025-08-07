package com.api.controller;

import com.api.entities.testng.XmlSuiteDetailAttribute;
import com.api.service.TestNGService;
import com.api.mapper.LIFMapper;
import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Tag(name = "[EH.API] Automation Testing")
@RestController
public class EHAPIController {

    @Autowired
    private LIFMapper lifMapper;

    @Autowired
    private TestNGService testNGService;

    @Operation(summary = "Custom Automation")
    @PostMapping(value = "/runAutomationBaseOnIdList", produces = "application/json;charset=UTF-8")
    public Object runAutomationBaseOnIdList(@RequestBody List<Integer> idList) {
        log.info("Start to run automation based on Id with list: " + idList);
        Map<String, String> result = new HashMap<>();
        Map<Integer, String> idToRunIdMap = new HashMap<>();
        Set<String> runIdSet = new HashSet<>();

        idList.forEach(id -> {
            String runId = UlidCreator.getUlid().toString();
            runIdSet.add(runId);
            idToRunIdMap.put(id, runId);
            JSONObject requestObject = new JSONObject();
            requestObject.put(XmlSuiteDetailAttribute.ID_LIST.getName(), id);
            requestObject.put(XmlSuiteDetailAttribute.IS_DEBUG.getName(), false);
            requestObject.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(), Boolean.FALSE.toString());
            requestObject.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
            requestObject.put(XmlSuiteDetailAttribute.RUN_BY.getName(), "api-run");
            testNGService.runWithAsync(requestObject);
        });

        runIdSet.forEach(runId -> {
            List<LinkedHashMap> suiteResults = lifMapper.fetchAutoTestngresult(runId);
            if (!suiteResults.isEmpty()) {
                LinkedHashMap suiteResult = suiteResults.get(0);
                result.put(suiteResult.get("component").toString(), runId);
            }
        });
        
        // 创建包含每个ID对应runId的响应对象
        JSONObject responseObject = new JSONObject();
        responseObject.put("idToRunIdMap", idToRunIdMap);
        
        return responseObject;
    }
    @Operation(summary = "Custom Automation")
    @PostMapping(value = "/runAutomationBaseOnComponent", produces = "application/json;charset=UTF-8")
    public Map<String, String> runAutomationBaseOnComponent(@RequestBody JSONObject requestBody) throws Exception {
        log.info("Start to run automation based on Component with parameter: " + requestBody);
        String componentStr = "components";
        String[] components = requestBody.containsKey(componentStr) ? requestBody.getString(componentStr).split(",") : null;
        if (components == null) throw new Exception("Field 'components' is necessary !");

        // prepare componentRunIdMap
        Map<String, String> componentRunIdMap = new HashMap<>();
        for (String s : components) {
            componentRunIdMap.put(s, UlidCreator.getUlid().toString());
        }

        // trigger automation
        componentRunIdMap.forEach((component, runId) -> {
            JSONObject requestObject = new JSONObject();
            requestObject.put(XmlSuiteDetailAttribute.COMPONENT.getName(), component);
            requestObject.put(XmlSuiteDetailAttribute.RUN_ID.getName(), runId);
            buildFinalRequestRunForComponentRun(requestBody, requestObject);
            testNGService.runWithAsync(requestObject);
        });

        return Map.of("componentRunIdMap", componentRunIdMap.toString());
    }

    @Operation(summary = "Custom Automation")
    @PostMapping(value = "/runAutomationBaseOnLabel", produces = "application/json;charset=UTF-8")
    public Object runAutomationBaseOnLabel(@RequestBody JSONObject requestBody) {
        log.info("Start to run automation based on label with parameter: " + requestBody);
        StringBuilder message = new StringBuilder();
        Set<JSONObject> finalRequestBodySet = buildFinalRequestBodySet(requestBody, message);
        if (!message.toString().isEmpty()) return message;

        for (JSONObject finalRequestBody : finalRequestBodySet) {
            log.info("buildFinalRequestBodySet - finalRequestBody before buildFinalRequestRunForComponentRun: {}", finalRequestBody);
            buildFinalRequestRunForComponentRun(requestBody, finalRequestBody);
            log.info("buildFinalRequestBodySet - finalRequestBody after buildFinalRequestRunForComponentRun: {}", finalRequestBody);
            testNGService.runWithAsync(finalRequestBody);
        }

        return finalRequestBodySet;
    }

    private void buildFinalLabel(String label, StringBuilder message) {
        if (label != null && !label.isEmpty()) {
            List<LinkedHashMap> testcases = lifMapper.fetchAutoCaseByLabel(label);
            if (testcases.isEmpty()) {
                message.append("Label \"" + label + "\" is not existed in AUTO_CASE!");
            }
        } else {
            message.append("For the field 'label'. please check your request body!");
        }
    }

    private Set<String> buildFinalRequestComponentArr(String components, String label) {
        Set<String> resultSet = new HashSet<>();
        if (components == null || components.isEmpty()) {
            List<LinkedHashMap> result = lifMapper.fetchAutoCaseByDynamicCondition("distinct component", "label = '" + label + "'");
            for (LinkedHashMap linkedHashMap : result) {
                resultSet.add(linkedHashMap.get("component").toString());
            }
        }
        return resultSet;
    }

    private Set<JSONObject> buildFinalRequestBodySet(JSONObject requestBody, StringBuilder message) {
        Set<JSONObject> resultSet = new HashSet<>();
        JSONObject resultObject = new JSONObject();
        String label = requestBody.getString("label");
        String components = requestBody.getString("components");

        buildFinalLabel(label, message);
        if (!message.toString().isEmpty()) return new HashSet<>();

        resultObject.put(XmlSuiteDetailAttribute.LABEL_LIST.getName(), label);
        Set<String> componentSet = buildFinalRequestComponentArr(components, label);
        if (componentSet.size() == 0) {
            message.append("There are no related Test Cases for component [\"+components+\"]!");
            return resultSet;
        } else {
            componentSet.forEach(x -> {
                JSONObject object = (JSONObject) resultObject.clone();
                object.put(XmlSuiteDetailAttribute.COMPONENT.getName(), x);
                object.put(XmlSuiteDetailAttribute.RUN_ID.getName(), UlidCreator.getUlid().toString());
                resultSet.add(object);
            });
        }
        return resultSet;
    }

    private void buildFinalRequestRunForComponentRun(JSONObject requestBody, JSONObject requestObject) {
        String sanityOnly = requestBody.containsKey(XmlSuiteDetailAttribute.SANITY_ONLY.getName()) ?
                requestBody.getString(XmlSuiteDetailAttribute.SANITY_ONLY.getName()).toLowerCase() : Boolean.FALSE.toString();

        // 设置isDebug参数，如果请求中有isDebug则使用请求中的值，否则默认为false
        if (requestBody.containsKey("isDebug")) {
            requestObject.put(XmlSuiteDetailAttribute.IS_DEBUG.getName(), requestBody.getString("isDebug"));
        } else {
            requestObject.put(XmlSuiteDetailAttribute.IS_DEBUG.getName(), Boolean.FALSE.toString());
        }
        
        requestObject.put(XmlSuiteDetailAttribute.SANITY_ONLY.getName(), sanityOnly);

        if (requestBody.containsKey("runBy")) {
            requestObject.put(XmlSuiteDetailAttribute.RUN_BY.getName(), requestBody.getString("runBy"));
        } else {
            requestObject.put(XmlSuiteDetailAttribute.RUN_BY.getName(), "api-run");
        }

        if (requestBody.containsKey("scenarios")) {
            requestObject.put(XmlSuiteDetailAttribute.SCENARIO_LIST.getName(), requestBody.getString("scenarios"));
        }
        if (requestBody.containsKey("fixVersion")) {
            requestObject.put(XmlSuiteDetailAttribute.RELEASE_VERSION_LIST.getName(), requestBody.getString("fixVersion"));
        }
        if (requestBody.containsKey("versionId")) {
            requestObject.put(XmlSuiteDetailAttribute.VERSION_ID.getName(), requestBody.getString("versionId"));
        }
        // 注意：这里不需要再次设置label，因为在buildFinalRequestBodySet中已经设置了
        // 如果requestObject中已经有label，就保持；如果没有，则从requestBody中获取
        if (!requestObject.containsKey(XmlSuiteDetailAttribute.LABEL_LIST.getName()) && requestBody.containsKey("label")) {
            requestObject.put(XmlSuiteDetailAttribute.LABEL_LIST.getName(), requestBody.getString("label"));
        }
    }
}