package com.api.helpers;

import com.api.utils.DateUtil;
import com.api.utils.ReflectionUtil;
import com.api.entities.TestAPIParameter;
import com.api.utils.DBUtil;
import com.api.utils.MemoryCacheUtil;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Slf4j
public class ParameterHelper {

    private static final String TEST_DEFAULT_CLASSNAME = "Test_default";
    private static final String TEST_DEFAULT_SERVICE_NAME = "eh_default";

    public static Map<String, String> buildParamsForSelectByString(String sql) {
        return new HashMap<>() {{
            put("sql", sql);
        }};
    }

    public static void getFinalRequestDetails(TestAPIParameter apiParameter) {
        JSONObject requestJSONObject = apiParameter.getRequestJSONObject();
        for (String key : requestJSONObject.keySet()) {
            Object result = traverseJsonForObject(key, requestJSONObject.get(key), apiParameter);
            apiParameter.getRequestJSONObject().put(key, result);
        }
        apiParameter.setCaseHelpJSONObject((JSONObject) traverseJsonForObject("caseHelp", apiParameter.getCaseHelpJSONObject(), apiParameter));
    }

    public static void fillBasicParameterDetails(TestAPIParameter parameter) {
        try {
            ParameterHelper.getFinalRequestDetails(parameter);
        } catch (Exception e) {
            log.error("[Exception] case: id=" + parameter.getId() + " " + parameter.getServiceName() + " " + parameter.getClassName() + " is not valid.");
        }
    }

    public static String setParameterForTradeBlotter(TestAPIParameter testParameter) {
        JSONObject parameterJSON = testParameter.getRequestJSONObject().getJSONObject("params");
        parameterJSON.put("userSoeid", "hz82340");
        parameterJSON.put("source", "ELIT");
        return parameterJSON.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<TestAPIParameter> getFinalTestParameterList(List<TestAPIParameter> parameters, String scenario_runId) {
        List<TestAPIParameter> result = new ArrayList<>();
        List<TestAPIParameter> autoEndpoints = (List<TestAPIParameter>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_endpoint", null);
        List<TestAPIParameter> autoBaseUrls = (List<TestAPIParameter>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_baseurl", null);

        for (TestAPIParameter testAPIParameter : parameters) {
                result.addAll(buildTestAPIParameterList(testAPIParameter, autoEndpoints, autoBaseUrls, scenario_runId));
            }
        return result;
    }

    private static List<TestAPIParameter> buildTestAPIParameterList(
            TestAPIParameter testAPIParameter,
            List<TestAPIParameter> autoEndpoints,
            List<TestAPIParameter> autoBaseUrls,
            String scenario_runId) {
        return new ArrayList<>(buildTestAPIParameterSingle(testAPIParameter, autoEndpoints, autoBaseUrls, scenario_runId));
    }

    private static List<TestAPIParameter> buildTestAPIParameterSingle(
            TestAPIParameter profileParameter,
            List<TestAPIParameter> autoEndpoints,
            List<TestAPIParameter> autoBaseUrls,
            String scenario_runId) {

        List<TestAPIParameter> result = new ArrayList<>();
        JSONObject parameterJSONObject = JSONObject.parseObject(profileParameter.getParameter());

        int id = profileParameter.getId();
        boolean isE2E = profileParameter.isE2E();
        boolean sanityOnly = profileParameter.isSanity();
        String suite = profileParameter.getSuite();
        String scenario = profileParameter.getScenario();
        String issueKey = profileParameter.getIssueKey();
        String testCaseDescription = profileParameter.getTestCaseDescription();
        String label = profileParameter.getLabel();

        int totalStepCount = parameterJSONObject.containsKey("variables")
                ? parameterJSONObject.size() - 1
                : parameterJSONObject.size();
        Map<String, String> stepIdDescriptionMap = new HashMap<>();
        for (int i = 1; i < totalStepCount; i++) {
            int index = i + 1;
            stepIdDescriptionMap.put(i + "", scenario_runId + "_" + id + "_" + index + " -> ifRun");
        }

        Object variableResult = null;
        if (isE2E) {
            if (parameterJSONObject.containsKey("caseIdList")) {
                parameterJSONObject.remove("caseIdList");
            }
            variableResult = InitE2eLocalVariable(parameterJSONObject, profileParameter, scenario_runId);
            InitE2eCase(parameterJSONObject);
        }

        try {
            for (String key : parameterJSONObject.keySet()) {
                JSONObject object = (JSONObject) parameterJSONObject.get(key);
                String profileName = StringUtils.isNotEmpty(object.getString("profileName"))
                        ? object.getString("profileName") : profileParameter.getProfileName();

                String regionEnv = System.getenv("REGION");
                if (regionEnv != null && !profileName.contains(regionEnv.toLowerCase())) {
                    continue;
                }

                TestAPIParameter parameter = new TestAPIParameter();
                parameter.setId(id);
                parameter.setRunId(scenario_runId);
                parameter.setE2E(isE2E);
                parameter.setSanity(sanityOnly);
                parameter.setSuite(suite);
                parameter.setScenario(scenario);
                parameter.setIssueKey(issueKey);
                parameter.setTestCaseDescription(testCaseDescription);
                parameter.setLabel(label);
                parameter.setStepId(Integer.parseInt(key));
                parameter.setPath(object.getString("path"));
                parameter.setMethod(object.getString("method"));
                parameter.setProfileName(profileName);

                if (object.containsKey("caseHelp")) {
                    parameter.setCaseHelpJSONObject(object.getJSONObject("caseHelp"));
                }
                if (object.containsKey("checkpoints")) {
                    parameter.setCheckPointJSONObject(object.getJSONObject("checkpoints"));
                }
                if (object.containsKey("beforeStep")) {
                    parameter.setBeforeStepJSONObject(object.getJSONObject("beforeStep"));
                }
                if (!object.containsKey("afterStep")) {
                    object.put("afterStep", new JSONObject());
                }

                addStepRelationshipFlagForE2E(key, totalStepCount, object,
                        (JSONObject) variableResult, stepIdDescriptionMap, parameter);

                parameter.setAfterStepJSONObject(object.getJSONObject("afterStep"));
                parameter.setRequestJSONObject(object.getJSONObject("request"));
                parameter.setTestStepDescription(object.getString("description"));
                String serviceName = object.containsKey("serviceName")
                        ? object.getString("serviceName") : TEST_DEFAULT_SERVICE_NAME;
                parameter.setServiceName(serviceName);
                parameter.setClassName(TEST_DEFAULT_CLASSNAME);
                parameter.setBaseURL(StringUtils.EMPTY);

                // For non-default service names, fetch className from autoEndpoint
                if (!StringUtils.equalsIgnoreCase(serviceName, TEST_DEFAULT_SERVICE_NAME)) {
                    parameter.setClassName(
                            autoEndpoints.stream()
                                    .filter(x -> StringUtils.equalsIgnoreCase(x.getServiceName(), serviceName)
                                            && StringUtils.equalsIgnoreCase(x.getMethod(), object.getString("method"))
                                            && StringUtils.equalsIgnoreCase(x.getPath(), object.getString("path")))
                                    .findFirst()
                                    .map(TestAPIParameter::getClassName)
                                    .orElse(TEST_DEFAULT_CLASSNAME)
                    );
                    parameter.setBaseURL(
                            autoBaseUrls.stream()
                                    .filter(x -> StringUtils.equalsIgnoreCase(
                                            x.getServiceName().replace("_", "-"),
                                            serviceName.replace("_", "-"))
                                            && StringUtils.equalsIgnoreCase(x.getProfileName(), profileName))
                                    .findFirst()
                                    .map(TestAPIParameter::getBaseURL)
                                    .orElse(StringUtils.EMPTY)
                    );
                }
                result.add(parameter);
            }
        } catch (Exception e) {
            log.error("[Exception] id " + profileParameter.getId() + " case is invalid.");
        }

        return result;
    }

    public static Object InitE2eLocalVariable(JSONObject parameterJSONObject, TestAPIParameter profileParameter, String scenario_runId) {
        Object variableResult = null;
        if (parameterJSONObject.containsKey("variables")) {
            variableResult = traverseJsonForObject("variables", parameterJSONObject.get("variables"), profileParameter);
            for (String key : ((JSONObject) variableResult).keySet()) {
                if (((JSONObject) variableResult).get(key) instanceof JSONObject &&
                        ((JSONObject) ((JSONObject) variableResult).get(key)).containsKey("afterStep")) {

                    JSONObject afterStep = ((JSONObject) ((JSONObject) variableResult).get(key)).getJSONObject("afterStep");
                    String stepCheckKey = scenario_runId + profileParameter.getId() + "->step" + key;

                    if (MemoryCacheUtil.getInstance().getIfPresent(stepCheckKey) == null) {
                        MemoryCacheUtil.getInstance().put(stepCheckKey, "@{" + scenario_runId + profileParameter.getId() + key + "}");
                    }
                } else {
                    String stepCheckKey = scenario_runId + profileParameter.getId() + key;
                    String stepCheckValue = (String) MemoryCacheUtil.getInstance().get(stepCheckKey);
                    MemoryCacheUtil.getInstance().put(stepCheckKey, stepCheckValue + "@{" + scenario_runId + profileParameter.getId() + key + "}");
                }

                MemoryCacheUtil.getInstance().put("@{" + scenario_runId + profileParameter.getId() + key + "}", ((JSONObject) variableResult).get(key));
            }

            parameterJSONObject.remove("variables");
        }
        return variableResult;
    }

    public static void InitE2eCase(JSONObject parameterJSONObject) {
        for (String stepKey : parameterJSONObject.keySet()) {
            JSONObject stepJSONObject = parameterJSONObject.getJSONObject(stepKey);
            if (stepJSONObject.containsKey("caseId")) {
                String stepCaseParameterString = ((List<TestAPIParameter>) DBUtil.doSqlSessionByEnvironment(
                        "postgresql_lif", "auto_case", Map.of("id", new ArrayList<>(Arrays.asList(stepJSONObject.get("caseId"))))))
                        .get(0).getParameter();

                JSONObject stepCaseParameter = JSONObject.parseObject(stepCaseParameterString).getJSONObject("1");

                if (stepJSONObject.containsKey("refactor")) {
                    JSONObject refactorJSONObject = stepJSONObject.getJSONObject("refactor");
                    for (String refactorKey : refactorJSONObject.keySet()) {
                        stepCaseParameter.put(refactorKey, refactorJSONObject.get(refactorKey));
                    }
                }
                parameterJSONObject.put(stepKey, stepCaseParameter);
            }
        }
    }

    private static JsonNode parseJsonNode(JsonNode jsonNode, String fieldName) {
        if (NumberUtils.isCreatable(fieldName)) {
            return jsonNode.path(Integer.parseInt(fieldName));
        }
        return jsonNode.path(fieldName);
    }

    public static String parseJsonNodeFromPaths(String response, String[] paths) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(response);
        } catch (JsonProcessingException e) {
            return "";
        }

        Iterator<String> pathIterator = Arrays.stream(paths).iterator();
        while (pathIterator.hasNext()) {
            jsonNode = parseJsonNode(jsonNode, pathIterator.next());
        }

        switch (jsonNode.getNodeType()) {
            case OBJECT:
                log.info("OBJECT: " + jsonNode.toPrettyString());
                return jsonNode.toString();
            case ARRAY:
                log.info("ARRAY: " + jsonNode.toPrettyString());
                return jsonNode.toString();
            default:
                log.info("String: " + jsonNode.asText());
                return jsonNode.asText();
        }
    }

    private static void addStepRelationshipFlagForE2E(
            String key,
            int totalStepCount,
            JSONObject object,
            JSONObject variableJSONObject,
            Map<String, String> stepIdDescriptionMap,
            TestAPIParameter profileParameter) {

        // has multi-step
        if (totalStepCount > 1) {
            // if the last step
            if (key.equalsIgnoreCase(totalStepCount + "")) {
                JSONArray keysInCacheArray = new JSONArray();
                for (Map.Entry<String, String> entry : stepIdDescriptionMap.entrySet()) {
                    keysInCacheArray.add(entry.getValue());
                }

                if (variableJSONObject != null) {
                    variableJSONObject.keySet().forEach(variableKey -> {
                        if (variableJSONObject.get(variableKey) instanceof JSONObject &&
                                variableJSONObject.getJSONObject(variableKey).containsKey("afterStep")) {

                            int afterStep = variableJSONObject.getJSONObject(variableKey).getInteger("afterStep");
                            String stepCheckKey = profileParameter.getRunId()
                                    + profileParameter.getId()
                                    + "->step" + variableKey + afterStep;

                            if (!keysInCacheArray.contains(stepCheckKey)) {
                                keysInCacheArray.add(stepCheckKey);
                            }
                        }
                        keysInCacheArray.add("@{" + profileParameter.getRunId() + profileParameter.getId() + variableKey + "}");
                    });
                }

                if (object.getJSONObject("afterStep").containsKey("memoryCache")) {
                    if (object.getJSONObject("afterStep").getJSONObject("memoryCache").containsKey("remove")) {
                        stepIdDescriptionMap.forEach((id, value) -> {
                            object.getJSONObject("afterStep")
                                    .getJSONObject("memoryCache")
                                    .getJSONArray("remove")
                                    .add(value);
                        });

                        if (variableJSONObject != null) {
                            variableJSONObject.keySet().forEach(variableKey -> {
                                if (variableJSONObject.get(variableKey) instanceof JSONObject &&
                                        variableJSONObject.getJSONObject(variableKey).containsKey("afterStep")) {

                                    int afterStep = variableJSONObject.getJSONObject(variableKey).getInteger("afterStep");
                                    String stepCheckKey = profileParameter.getRunId()
                                            + profileParameter.getId()
                                            + "->step" + variableKey + afterStep;

                                    if (!object.getJSONObject("afterStep")
                                            .getJSONObject("memoryCache")
                                            .getJSONArray("remove")
                                            .contains(stepCheckKey)) {

                                        object.getJSONObject("afterStep")
                                                .getJSONObject("memoryCache")
                                                .getJSONArray("remove")
                                                .add(stepCheckKey);
                                    }
                                }

                                object.getJSONObject("afterStep")
                                        .getJSONObject("memoryCache")
                                        .getJSONArray("remove")
                                        .add("@{" + profileParameter.getId() + variableKey + "}");
                            });
                        }

                    } else {
                        JSONObject removeObject = new JSONObject();
                        removeObject.put("remove", keysInCacheArray);
                        object.getJSONObject("afterStep").put("memoryCache", removeObject);
                    }
                } else {
                    JSONObject removeObject = new JSONObject();
                    removeObject.put("remove", keysInCacheArray);
                    object.getJSONObject("afterStep").put("memoryCache", removeObject);
                }

            } else {
                JSONObject statusObject = new JSONObject();
                statusObject.put("response", "StatusCode");
                statusObject.put("keyInCache", stepIdDescriptionMap.get(key));

                JSONArray array = new JSONArray();
                array.add(statusObject);
                JSONObject addObject = new JSONObject();
                addObject.put("add", array);

                if (object.getJSONObject("afterStep").containsKey("memoryCache")) {
                    if (object.getJSONObject("afterStep").getJSONObject("memoryCache").containsKey("add")) {
                        object.getJSONObject("afterStep")
                                .getJSONObject("memoryCache")
                                .getJSONArray("add")
                                .add(statusObject);
                    } else {
                        object.getJSONObject("afterStep")
                                .getJSONObject("memoryCache")
                                .put("add", array);
                    }
                } else {
                    object.getJSONObject("afterStep")
                            .put("memoryCache", addObject);
                }
            }
        }
    }
    public static Object traverseJsonForObject(String jsonKey, Object objJson, TestAPIParameter apiParameter) {
        if (objJson instanceof JSONArray) {
            JSONArray result = new JSONArray();
            JSONArray jsonArray = (JSONArray) objJson;
            if (jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.get(i) instanceof JSONObject) {
                        result.add(traverseJsonForObject("", jsonArray.getJSONObject(i), apiParameter));
                    } else if (jsonArray.get(i) instanceof JSONArray) {
                        result.add(traverseJsonForObject("", jsonArray.getJSONArray(i), apiParameter));
                    } else {
                        result.add(jsonArray.get(i));
                    }
                }
            }
            return result;
        } else if (objJson instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) objJson;

            if (jsonObject.containsKey("afterStep")) {
                return jsonObject;
            }

            if (jsonObject.containsKey("response")) {
                return queryFromResponse(jsonObject, apiParameter);
            }

            if (jsonObject.containsKey("db")) {
                return queryFromDb(jsonKey, jsonObject, apiParameter);
            }



            if (jsonObject.containsKey("code")) {
                return queryFromCode(jsonObject);
            }

            if (jsonObject.containsKey("memoryCache")) {
                return queryFromMemoryCache(jsonObject);
            }

            if (jsonObject.containsKey("JSONArray")) {
                return queryFromJSONArray(jsonObject, apiParameter);
            }

            JSONObject result = new JSONObject();
            Set<String> keySet = jsonObject.keySet();
            for (String key : keySet) {
                Object value = jsonObject.get(key);
                try {
                    JSONObject keyJsonType = JSONObject.parseObject(key.replace("$", "\""));
                    String targetKey = (String) traverseJsonForObject("", keyJsonType, apiParameter);
                    jsonObject.remove(key);
                    jsonObject.put(targetKey, value);
                    key = targetKey;
                } catch (JSONException e) {
                    // not JSON, keep key unchanged
                }

                if (value instanceof JSONArray) {
                    JSONArray innerArr = (JSONArray) value;
                    result.put(key, traverseJsonForObject(key, innerArr, apiParameter));
                } else if (value instanceof JSONObject) {
                    result.put(key, traverseJsonForObject(key, (JSONObject) value, apiParameter));
                } else if (value instanceof String) {
                    // global variable
                    if (Pattern.compile("@\\{([^}]+)}").matcher((String) value).find()) {
                        Pattern pattern = Pattern.compile("@\\{([^}]+)}");
                        Matcher matcher = pattern.matcher((String) value);
                        while (matcher.find()) {
                            Object globalValue = ListenerHelper.fetchGlobalVariable(matcher.group(1));
                            result.put(key, globalValue);
                        }
                    }
                    // e2e workflow variable
                    else if (Pattern.compile("(?<!@)@\\{([^}]+)}").matcher((String) value).find()) {
                        StringBuilder stringBuilder = new StringBuilder((String) value);
                        stringBuilder.insert(2, apiParameter.getRunId() + apiParameter.getId());
                        result.put(key, MemoryCacheUtil.getInstance().get(stringBuilder.toString()));
                    } else {
                        result.put(key, value);
                    }
                } else {
                    result.put(key, value);
                }
            }
            return result;
        } else {
            return objJson;
        }
    }
    protected static Object queryFromCode(JSONObject valueObject) {
        JSONObject mapObject = valueObject.getJSONObject("map");
        Map<String, Object> map = new HashMap<>();
        for (String key : mapObject.keySet()) {
            map.put(key, ReflectionUtil.fetchClassByClassName(mapObject.getString(key)));
        }
        String expression = valueObject.getString("code");
        return ReflectionUtil.fetchValueByCodeExpression(expression, map);
    }


    public static Object parseFromResponse(String[] attributes, JSONObject responseObject) {
        Object resultStr = new Object();
        for (String attribute : attributes) {
            if (attribute.startsWith("{JSONArray}")) {
                String attr = attribute.replaceFirst("\\{JSONArray}", "");
                resultStr = responseObject.get(attr) instanceof JSONArray ? responseObject.getJSONArray(attr) : new JSONArray();
                continue;
            }
            if (attribute.startsWith("{JSONObject}")) {
                String attr = attribute.replaceFirst("\\{JSONObject}", "");
                resultStr = responseObject.getJSONObject(attr);
                continue;
            }
            if (attribute.startsWith("{StringList}")) {
                if (resultStr instanceof JSONArray) {
                    List<String> list = new ArrayList<>();
                    ((JSONArray) resultStr).forEach(s -> list.add(s.toString()));
                    resultStr = StringUtils.join(list, ",");
                }
                continue;
            }
            resultStr = responseObject.get(attribute);
        }
        return resultStr;
    }

    protected static Object queryFromResponse(JSONObject valueObject, TestAPIParameter apiParameter) {
        Response response = apiParameter.getResponse();
        String responseString = response == null ? "" : response.asString();

        Object result = null;
        if (StringUtils.equalsIgnoreCase(valueObject.getString("response"), "JSONObject")) {
            JSONObject responseObject = JSONObject.parseObject(responseString);
            if (valueObject.containsKey("attribute")) {
                String[] attributes = valueObject.getString("attribute").split("_");
                result = parseFromResponse(attributes, responseObject).toString();
            }
            if (valueObject.containsKey("jsonNode")) {
                String[] paths = valueObject.getString("jsonNode").split(",");
                result = parseJsonNodeFromPaths(responseString, paths);
            }
        } else if (StringUtils.equalsIgnoreCase(valueObject.getString("response"), "JSONArray")) {
            JSONArray responseArray = JSONArray.parseArray(responseString);
            if (valueObject.containsKey("attribute") && valueObject.containsKey("concat")) {
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < responseArray.size(); i++) {
                    list.add(responseArray.getJSONObject(i).get(valueObject.getString("attribute")));
                }
                result = StringUtils.join(list, valueObject.getString("concat"));
            }
        }

        return result;
    }

    protected static Object queryFromJSONArray(JSONObject valueObject, TestAPIParameter apiParameter) {
        JSONObject details = (JSONObject) traverseJsonForObject("details", valueObject.getJSONObject("details"), apiParameter);
        String resultType = valueObject.getString("JSONArray");
        JSONArray result = new JSONArray();
        switch (resultType.toLowerCase()) {
            case "string":
                String[] splitBy = details.getString(valueObject.getString("splitBy")).split(",");
                Collections.addAll(result, splitBy);
                break;
            case "jsonobject":
                JSONArray splitByArray = valueObject.getJSONArray("splitBy"); // id,version
                int size = details.getString(splitByArray.getString(0)).split(",").length;
                for (int i = 0; i < size; i++) {
                    JSONObject object = (JSONObject) details.clone();
                    for (int j = 0; j < splitByArray.size(); j++) {
                        String key = splitByArray.getString(j);
                        String value = details.getString(splitByArray.getString(j)).split(",")[i];
                        object.remove(key);
                        object.put(key, value);
                    }
                    result.add(object);
                }
                break;
            default:
                break;
        }

        return result;
    }

    protected static Object queryFromMemoryCache(JSONObject valueObject) {
        return MemoryCacheUtil.getInstance().get(valueObject.getString("memoryCache"));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected static Object queryFromDb(String key, JSONObject valueObject, TestAPIParameter apiParameter) {
        String caseProfile = valueObject.containsKey("profileName") ? valueObject.getString("profileName") : apiParameter.getProfileName();
        String result = "";

        // drms / ignite / nam_aut_drms
        String profile_name = fetchProfileName(valueObject, caseProfile);
        String sql = fetchSqlAndProcess(valueObject, apiParameter);
        if (sql == null) return result;
        Map<String, String> params = new HashMap<>(); // ###key###!!!

        // explain the sql with e2e workflow variable and global variable
        sql = parseSqlWithE2eVariable(sql, apiParameter);
        sql = parseSqlWithGlobalVariable(sql, apiParameter);

        params.put("sql", sql.replaceAll("\\$", "").replaceAll("#!", "").replaceAll("#!", ""));
        if (sql.contains("smartResponse")) {
            return ppcSqlQuery(profile_name, params);
        } else {
            List<LinkedHashMap> list = (List<LinkedHashMap>) DBUtil.doSqlSessionByEnvironment(profile_name, "selectBySQL", params);
            if (valueObject.containsKey("returnType")) {
                return specificReturnTypeSqlQuery(list, valueObject);
            }

            if (valueObject.containsKey("concat")) {
                return concatenateQueryResult(list, (String) valueObject.get("concat"));
            } else {
                return defaultSqlQuery(key, list);
            }
        }
    }
    public static boolean isSqlContainsConfig(String sql) {
        return Pattern.compile("&\\{\\{([\\s\\S]+?)\\}\\}").matcher(sql).matches();
    }

    public static String parseSqlWithE2eVariable(String sql, TestAPIParameter apiParameter) {
        Pattern pattern = Pattern.compile("(?<!@)@\\{\\{([\\s\\S]+?)\\}\\}");
        Matcher matcher = pattern.matcher(sql);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = "@{" + apiParameter.getRunId() + apiParameter.getId() + matcher.group(1) + "}";
            String replacement = MemoryCacheUtil.getInstance().get(key).toString();
            if (replacement != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String parseSqlWithGlobalVariable(String sql, TestAPIParameter apiParameter) {
        Pattern pattern = Pattern.compile("@@\\{\\{([\\s\\S]+?)\\}\\}");
        Matcher matcher = pattern.matcher(sql);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            Object value = ListenerHelper.fetchGlobalVariable(matcher.group(1));
            String replacement = value.toString();
            if (replacement != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    protected static String defaultSqlQuery(String key, List<LinkedHashMap> list) {
        // if the column contains date and the target table don't have any records, the default value should be current date -7.
        String defaultValueForDate = key.contains("Date")? DateUtil.getTargetDateWithFormat(-7,"yyyyMMdd") : "";
        return list.size() == 0 || list.get(0) == null? defaultValueForDate : list.get(0).toString().replaceAll("}", "").split("=")[1];
    }

    protected static String ppcSqlQuery(String profile_name, Map<String, String> params){
        List<String> list = (List<String>) DBUtil.doSqlSessionByEnvironment(profile_name, "selectStringListBySQL", params);
        return list.get(0) == null ? "" : list.get(0).split("conversationId\":\"")[1].split("\"")[0];
    }

    protected static Object specificReturnTypeSqlQuery(List<LinkedHashMap> list, JSONObject valueObject){
        if(StringUtils.containsIgnoreCase(valueObject.getString("returnType"), "int")){
            return list.size() == 0 || list.get(0) == null ? 0 : Integer.parseInt(list.get(0).toString().replaceAll("}", "").split("=")[1]);
        }
        return null;
    }
    protected static String fetchSqlAndProcess(JSONObject valueObject, TestAPIParameter apiParameter){
        String sql = valueObject.getString("sql");
        if(sql == null){
            log.info("[No SQL] IN ");
            return null;
        }
        if (valueObject.containsKey("replace")) {
            valueObject.put("replace", traverseJsonForObject("replace", valueObject.getJSONObject("replace"), apiParameter));
            for (String key : valueObject.getJSONObject("replace").keySet()) {
                sql = sql.replaceAll("#" + key + "#", valueObject.getJSONObject("replace").getString(key));
            }
        }
        return sql;
    }

    protected static String fetchProfileName(JSONObject valueObject, String caseProfile){
        if(valueObject.getString("db").equalsIgnoreCase("drms")){
            return caseProfile + "_drms";
        } else if(valueObject.getString("db").equalsIgnoreCase("ignite")){
            return caseProfile + "_ignite";
        } else{
            return valueObject.getString("db");
        }
    }

    protected static String concatenateQueryResult(List<LinkedHashMap> list, String connectors){
        StringBuilder result = new StringBuilder();
        for(LinkedHashMap map: list){
            for(Object o: map.values()){
                if(o instanceof Integer){
                    result.append(o);
                } else if (o instanceof String){
                    result.append((String) o);
                }
            }
            result.append(connectors);
        }
        return result.toString().length() == 0 ? "" : result.substring(0,result.length()-1);
    }



    public static String getEliteRefDataApi(String baseUrl, String url, String paramName, String paramValue) {
        return given()
                .baseUri(baseUrl)
                .header("SM_USER", "hz82340")
                .param(paramName, paramValue)
                .when()
                .get(url)
                .then()
                .extract()
                .body().asString();
    }
}