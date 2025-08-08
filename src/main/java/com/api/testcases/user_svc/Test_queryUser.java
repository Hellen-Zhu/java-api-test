package com.api.testcases.user_svc;

import com.alibaba.fastjson2.JSONObject;
import com.api.entities.TestAPIParameter;
import com.api.utils.APIUtil;
import com.api.utils.MemoryCacheUtil;
import com.api.utils.ResponseValidator;
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

    @Test(description = "Trigger query user api with authorization token")
    public void trigger_query_user_API(ITestContext testContext) {
        // 从缓存中获取之前存储的token（优先 token，回退 ADMIN_TOKEN）
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

        // 使用包含 accessToken 的重载方法，让 APIUtil 走 initAuthBearerRequest 自动加 Bearer
        response = APIUtil.requestAPI(testParameter, testContext, accessToken);

        // 响应校验与日志
        ResponseValidator.validateResponse(testParameter, response);
        responseJSON = JSONObject.parseObject(response.asString());
        log.info("Query user response: {}", responseJSON);
    }

    @Test(dependsOnMethods = "trigger_query_user_API")
    public void verify_response() {
        response.then().contentType(ContentType.JSON);
    }
}
