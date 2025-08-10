package com.api.testcases.order_svc;

import com.api.data.entities.TestAPIParameter;
import com.alibaba.fastjson2.JSONObject;
import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import com.api.common.utils.APIUtil;
import com.api.common.utils.ResponseValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test_queryOrder {
    private Response response;
    private JSONObject responseJSON;
    public TestAPIParameter testParameter;

    private Test_queryOrder(TestAPIParameter testParameter) {
        this.testParameter = testParameter;
    }

    public static Test_queryOrder getInstance(TestAPIParameter testParameter) {
        return new Test_queryOrder(testParameter);
    }

    @Test(description = "Trigger query order API with authorization token")
    public void trigger_query_order_API(ITestContext testContext) {
        response = APIUtil.requestAPI(testParameter, testContext);
        
        // Validate dynamically with ResponseValidator
        ResponseValidator.validateResponse(testParameter, response);
        
        responseJSON = JSONObject.parseObject(response.asString());
        log.info("responseJSON = {}", responseJSON);
    }

    @Test(dependsOnMethods = "trigger_query_order_API")
    public void verify_response() {
        
    }
    
}
