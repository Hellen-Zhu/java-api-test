package com.api.entities;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.api.enums.DBEnum;
import com.api.enums.DSEnum;

import io.restassured.response.Response;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;
@Data
public class TestAPIParameter implements Cloneable {
    //basic
    int id;
    boolean isE2E;
    boolean isSanity;
    boolean isGlobal;
    boolean isEnable;
    String runId;
    String region;

    // from auto_case.component || component_like
    String suite;
    String scenario;
    String issueKey;

    //auto_case.description, depend on issue_key
    String testCaseDescription;
    String label;
    String parameter;

    // reflection
    String serviceName;
    String className;

    //from JSONObject.parseObject(parameter)
    int stepId = 1;
    String component;
    String componentLike;
    String path;
    String method;

    // eg: nam_uat
    String profileName;
    String baseURL;
    String testStepDescription;
    JSONObject variableJSONObject;
    JSONObject caseHelpJSONObject;
    JSONObject requestJSONObject;
    JSONObject beforeStepJSONObject;
    JSONObject afterStepJSONObject;

    //Others
    JSONObject checkPointJSONObject;
    Response response; // 假设 Response 是一个自定义类或来自像RestAssured这样的库
    String currentMethodName;

    @Override
    public TestAPIParameter clone() {
        try {
            return (TestAPIParameter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getHeader() {
        return this.requestJSONObject.getJSONObject("headers");
    }

    public HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        this.getHeader().forEach((key, value) -> httpHeaders.add(key, String.valueOf(value)));
        return httpHeaders;
    }

    public Map<String, String> getHeadersMap() {
        Map<String, String> headerMap = new HashMap<>();
        this.getHeader().forEach((key, value) -> headerMap.put(key, String.valueOf(value)));
        return headerMap;
    }

    public JSONObject getPathParams() {
        return this.requestJSONObject.getJSONObject("pathParams");
    }

    public JSONObject getBodyObject() {
        return this.requestJSONObject.getJSONObject("body");
    }

    public JSONArray getBodyArray() {
        return this.requestJSONObject.getJSONArray("body");
    }

    public JSONObject getParams() {
        return this.requestJSONObject.getJSONObject("params");
    }

    public DSEnum getDsEnum(DBEnum type) {
        return DSEnum.fromValue("getInstance_" + this.profileName + "_" + type.name().toLowerCase());
    }

    public String getLocalRegion() {
        return StringUtils.containsIgnoreCase(this.profileName, "NAM") ? "NAM" :
                (StringUtils.containsIgnoreCase(this.profileName, "APAC") ? "APAC" : "EMEA");
    }
}