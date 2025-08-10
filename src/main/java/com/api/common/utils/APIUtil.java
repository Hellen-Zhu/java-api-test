package com.api.common.utils;

import com.api.data.entities.lif.AutoSystemVariable;
import com.api.data.entities.TestAPIParameter;
import com.api.common.helpers.Constants;
import com.api.common.helpers.ParameterHelper;
import com.alibaba.fastjson2.JSONObject;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.ITestContext;

import java.util.*;
import static io.restassured.RestAssured.given;

@Slf4j
public class APIUtil {

    public static Response requestAPI(TestAPIParameter parameter, ITestContext testContext) throws RuntimeException {
        return requestAPI(parameter, testContext, null);
    }

    public static Response requestAPI(TestAPIParameter parameter, ITestContext testContext, String accessToken) throws RuntimeException {
        String baseurl = checkBaseURLStatus(parameter);

        RequestSpecification request = addRequestParameter(
                StringUtils.isEmpty(accessToken) ? initRequest(baseurl) : initAuthBearerRequest(baseurl, accessToken), parameter);

        Response response = sendRequestByHttpMethodAndPath(request, parameter);

        dealWithResponseAndSaveInCache(parameter, response, testContext);

        return response;
    }

    private static RequestSpecification initAuthBearerRequest(String baseUrl, String accessToken) {
        RestAssuredConfig restAssuredConfig = new RestAssuredConfig();
        restAssuredConfig.httpClient(HttpClientConfig.httpClientConfig().setParam("http.socket.timeout", 7200000)
                .setParam("http.connection.timeout", 600000));
        return given().baseUri(baseUrl).header("Authorization", "Bearer " + accessToken).config(restAssuredConfig);
    }

    private static void dealWithResponseAndSaveInCache(TestAPIParameter parameter, Response response, ITestContext testContext) {
        String debugOutput = debugLogForRequest(parameter);
        String reportOutput = DateUtil.getCurrentTimeLog() + "\n";
        String errMsg = "Error" + "\n";
        if (response == null) {
            reportOutput += errMsg;
            Assert.fail("Response is null");
        } else {
            try {
                reportOutput += "StatusCode : " + response.getStatusCode() + "\n";
                reportOutput += "ResponseMsg : " + StringUtils.abbreviate(response.then().extract().asString(), 2048) + "\n";
            } catch (Exception e) {
                reportOutput += "ResponseMsg : " + errMsg + "\n";
            }
        }
        log.info(debugOutput);
        log.info(reportOutput);
        ReporterUtil.debug(testContext, parameter, debugOutput);
        ReporterUtil.report(testContext, parameter, reportOutput);
    }

    private static RequestSpecification initRequest(String baseUrl) {
        RestAssuredConfig restAssuredConfig = new RestAssuredConfig();
        restAssuredConfig.httpClient(HttpClientConfig.httpClientConfig().setParam("http.socket.timeout", 7200000)
                .setParam("http.connection.timeout", 600000));
        return given().baseUri(baseUrl).config(restAssuredConfig);
    }
    private static String checkBaseURLStatus(TestAPIParameter parameter) {
        String baseUrls = parameter.getBaseURL().equalsIgnoreCase("") && parameter.getPath() != null ? parameter.getPath() : parameter.getBaseURL();
        String[] baseUrlLists = baseUrls.split(";");
        String selectBaseUrl = "";
        for (String baseUrl : baseUrlLists) {
            try {
                RestAssured.given().relaxedHTTPSValidation().get(baseUrl);
                log.info("[" + baseUrl + "] " + "connect success");
                selectBaseUrl = baseUrl;
                break;
            } catch (Exception ex) {
                log.info("[" + baseUrl + "] " + ex.getMessage());
            }
        }
        if(selectBaseUrl.equals("")){
            throw new IllegalStateException("failed to connect to server");
        }
        parameter.setBaseURL(selectBaseUrl);
        return selectBaseUrl;
    }

    public static Map<String, Object> fetchResponseMapForAfterStep(Response response, boolean statusCodeFlag) {
        String responseString = response != null ? response.asString() : "";
        int statusCode;
        if (statusCodeFlag) {
            assert response != null;
            statusCode = response.getStatusCode();
        } else {
            statusCode = 1000;
        }
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("responseString", responseString);
        responseMap.put("statusCode", statusCode);
        return responseMap;
    }

    public static void beforeStep(TestAPIParameter parameter) {
        doExtraStepWork(parameter.getBeforeStepJSONObject(), parameter, null);
    }

    public static void afterStep(TestAPIParameter parameter, Map<String, Object> response) {
        operateE2eVariableInAfterStep(parameter);
        doExtraStepWork(parameter.getAfterStepJSONObject(), parameter, response);
    }

    public static void operateE2eVariableInAfterStep(TestAPIParameter testParameter){
        String stepCheckKey = testParameter.getRunId()+testParameter.getId()+"->step"+testParameter.getStepId();
        if (MemoryCacheUtil.getInstance().get(stepCheckKey) != null){
            String variableKeys = (String)MemoryCacheUtil.getInstance().get(stepCheckKey);
            List<String> variableKeyList = Arrays.asList(variableKeys.split(","));

            variableKeyList.forEach(variableKey->{
                JSONObject variableJSONObject = (JSONObject)MemoryCacheUtil.getInstance().get(variableKey);
                String action = variableJSONObject.getString("action");

                //remove action and afterStep
                variableJSONObject.remove("afterStep");
                variableJSONObject.remove("action");

                switch (action){
                    case "add":
                    case "update":
                        Object result = ParameterHelper.traverseJsonForObject("variable",variableJSONObject,testParameter);
                        MemoryCacheUtil.getInstance().put(variableKey,result);
                        break;
                    case "remove":
                        MemoryCacheUtil.getInstance().invalidate(variableKey);
                        break;
                }
            });
        }
    }

    private static void doExtraStepWork(JSONObject extraStepJSONObject, TestAPIParameter testParameter, Map<String, Object> response) {
        if (extraStepJSONObject != null) {
            if (extraStepJSONObject.containsKey("memoryCache")) {
                JSONObject memoryCacheObject = extraStepJSONObject.getJSONObject("memoryCache");
                if (memoryCacheObject.containsKey("add")) {
                    MemoryCacheUtil.addInMemoryCache(memoryCacheObject.getJSONArray("add"), response);
                }
                if (memoryCacheObject.containsKey("remove")) {
                    MemoryCacheUtil.removeInMemoryCache(memoryCacheObject.getJSONArray("remove"));
                }
            }

            if (extraStepJSONObject.containsKey("sleepInMillis")) {
                try {
                    Thread.sleep(Long.parseLong(extraStepJSONObject.getString("sleepInMillis").replace("L", "")));
                } catch (InterruptedException e) {
                    log.error("something wrong");
                }
            }
        }
    }

    private static String debugLogForRequest(TestAPIParameter parameter) {
        String result = "";
        result += "\n[APIUtil] [INFO] " + DateUtil.getCurrentTimeLog() + "[" + parameter.getProfileName() + "] " + generateURL(parameter) + "\n";
        result += "[APIUtil] [INFO] " + DateUtil.getCurrentTimeLog() + "[method] " + parameter.getMethod() + "\n";
        if (parameter.getBaseURL() == null) {
            result += "[APIUtil] [Warning] " + parameter.getServiceName() + " is down!" + "\n";
            Assert.fail();
        }

        if (parameter.getRequestJSONObject().containsKey("headers")) {
            result += "[APIUtil] [INFO] " + DateUtil.getCurrentTimeLog() + "[headers] " + parameter.getRequestJSONObject().get("headers") + "\n";
        }
        if (parameter.getRequestJSONObject().containsKey("cookie")) {
            result += "[APIUtil][cookie] " + parameter.getRequestJSONObject().get("cookie") + "\n";
        }
        if (parameter.getRequestJSONObject().containsKey("body")) {
            result += "[APIUtil] [RequestBody] " + parameter.getRequestJSONObject().get("body") + "\n";
        }
        result += "[APIUtil] [Description] " + parameter.getTestStepDescription() + "\n";
        result += "[APIUtil] [step] " + parameter.getStepId() + "\n";
        result += "[APIUtil] [id] " + parameter.getId() + "\n";
        return result;
    }

    @SuppressWarnings("unchecked")
    public static String fetchEliteConfigParameterByServiceName() {
        return ((List<AutoSystemVariable>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_system_variable", Map.of("config_key", "EliteToken"))).get(0).getValue();
    }

    private static RequestSpecification addRequestParameter(RequestSpecification request, TestAPIParameter parameter) {
        JSONObject parameterJSON = parameter.getRequestJSONObject();
        String method = parameter.getMethod();

        if (Constants.SERVICES_SSLIGNORE.contains(parameter.getServiceName().toLowerCase())
                || Constants.ENDPOINTS_SSLIGNORE.contains(parameter.getClassName())) {
            request = request.relaxedHTTPSValidation();
        }

        if (parameterJSON.containsKey("cookie")) {
            request = request.cookie("SMSESSION", fetchEliteConfigParameterByServiceName());
        }
        if (parameterJSON.containsKey("headers")) {
            request = request.headers(DataTypeUtil.jsonObjectToHashMap(parameterJSON.getJSONObject("headers")));
        }
        if (parameterJSON.containsKey("body")) {
            request = request.request().contentType(ContentType.JSON).body(parameterJSON.get("body").toString());
        }
        if (parameterJSON.containsKey("params")) {
            if (!parameterJSON.containsKey("body") && method.equalsIgnoreCase("get")) {
                request = request.params(DataTypeUtil.jsonObjectToHashMap(parameterJSON.getJSONObject("params")));
            } else {
                request = request.queryParams(DataTypeUtil.jsonObjectToHashMap(parameterJSON.getJSONObject("params")));
            }
        }
        if (parameterJSON.containsKey("pathParams")) {
            request = request.pathParams(DataTypeUtil.jsonObjectToHashMap(parameterJSON.getJSONObject("pathParams")));
        }
        return request;
    }

    private static Response sendRequestByHttpMethodAndPath(RequestSpecification request, TestAPIParameter parameter) {
        Response response = null;
        String method = parameter.getMethod();
        String path = parameter.getPath();
        try {
            switch (method.toLowerCase()) {
                case "post":
                    response = request.when().post(path);
                    break;
                case "get":
                    response = request.when().get(path);
                    break;
                case "put":
                    response = request.when().put(path);
                    break;
                case "delete":
                    response = request.when().delete(path);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return response;
    }




    private static String  (TestAPIParameter testAPIParameter) {
        StringBuilder url = Optional.ofNullable(testAPIParameter.getBaseURL()).map(StringBuilder::new).orElse(null);
        StringBuilder path = new StringBuilder();

            if (testAPIParameter.getRequestJSONObject().containsKey("pathParams")) {
            JSONObject pathParams = testAPIParameter.getRequestJSONObject().getJSONObject("pathParams");
            // Assume pathParamsItems are derived from a field in testAPIParameter
            String[] pathParamsItems = testAPIParameter.getPath().split("/");
            for (int i = 0; i < pathParamsItems.length; i++) {
                if (!pathParamsItems[i].contains("{")) {
                    path.append(pathParamsItems[i]);
                } else {
                    path.append("/").append(pathParams.get(pathParamsItems[i].substring(1, pathParamsItems[i].length() - 1)));
                }
            }
        } else {
            path = Optional.ofNullable(testAPIParameter.getPath()).map(StringBuilder::new).orElse(null);
        }

        url = (url == null ? new StringBuilder("null") : url).append(path);

        if (testAPIParameter.getRequestJSONObject().containsKey("params")) {
            url.append("?");
            JSONObject params = testAPIParameter.getRequestJSONObject().getJSONObject("params");
            for (String key : params.keySet()) {
                url.append(key).append("=").append(params.get(key)).append("&");
            }
            // Remove the trailing '&'
            url = new StringBuilder(url.substring(0, url.length() - 1));
        }

        return url.toString();
    }

}
