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
public class Test_userLogin {
    private Response response;
    private JSONObject responseJSON;
    public TestAPIParameter testParameter;
    MemoryCacheUtil memoryCacheUtil = MemoryCacheUtil.getInstance();

    private Test_userLogin(TestAPIParameter testParameter) {
        this.testParameter = testParameter;
    }

    public static Test_userLogin getInstance(TestAPIParameter testParameter) {
        return new Test_userLogin(testParameter);
    }

    @Test(description = "Trigger login api")
    public void trigger_login_API(ITestContext testContext) {
        response = APIUtil.requestAPI(testParameter, testContext);
        
        // Validate dynamically with ResponseValidator
        ResponseValidator.validateResponse(testParameter, response);
        
        responseJSON = JSONObject.parseObject(response.asString());
        log.info("responseJSON = {}", responseJSON);

        // Extract token from response and store it in cache
        if (responseJSON != null && responseJSON.containsKey("data")) {
            JSONObject data = responseJSON.getJSONObject("data");
            if (data != null && data.containsKey("token")) {
                String token = data.getString("token");
                memoryCacheUtil.put("token", token);
                log.info("Token stored in cache: {}", token);
            } else {
                log.warn("Token not found in response data");
            }
        } else {
            log.warn("Response data is null or does not contain 'data' field");
        }
    }

    @Test(dependsOnMethods = "trigger_login_API")
    public void verify_response() {
        response.then().contentType(ContentType.JSON);
    }
}
