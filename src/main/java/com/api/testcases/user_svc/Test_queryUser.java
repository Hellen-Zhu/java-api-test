package com.api.testcases.user_svc;

import com.alibaba.fastjson2.JSONObject;
import com.api.data.entities.TestAPIParameter;
import com.api.common.utils.APIUtil;
import com.api.common.utils.MemoryCacheUtil;
import com.api.common.utils.ResponseValidator;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.Test;

@Slf4j
public class Test_queryUser {
    private Response response;
    private JSONObject responseJSON;
    public TestAPIParameter testParameter;
    MemoryCacheUtil memoryCacheUtil = MemoryCacheUtil.getInstance();

    private Test_queryUser(TestAPIParameter testParameter) {
        this.testParameter = testParameter;
    }

    public static Test_queryUser getInstance(TestAPIParameter testParameter) {
        return new Test_queryUser(testParameter);
    }

    @Test(description = "Trigger query user API with authorization token")
    public void trigger_query_user_API(ITestContext testContext) {
        String accessToken = null;
        Object tokenObj = memoryCacheUtil.get("token");
        if (tokenObj != null) {
            accessToken = tokenObj.toString().trim();
            String masked = accessToken.length() > 12
                    ? accessToken.substring(0, 6) + "***" + accessToken.substring(accessToken.length() - 6)
                    : "<short-token>";
            log.info("Retrieved token from cache (masked): {}", masked);
        } else {
            log.warn("No token found in cache ");
        }

        response = APIUtil.requestAPI(testParameter, testContext, accessToken);

        // Validate response and log
        ResponseValidator.validateResponse(testParameter, response);
        responseJSON = JSONObject.parseObject(response.asString());
        log.info("Query user response: {}", responseJSON);
    }

    @Test(dependsOnMethods = "trigger_query_user_API")
    public void verify_response() {
        response.then().contentType(ContentType.JSON);
    }
}
