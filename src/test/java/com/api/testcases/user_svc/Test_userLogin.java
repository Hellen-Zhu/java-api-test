package com.api.testcases.user_svc;

import com.alibaba.fastjson2.JSONObject;
import com.api.entities.TestAPIParameter;
import com.api.utils.APIUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.Assert;

import static org.hamcrest.Matchers.*;

@Slf4j
public class Test_userLogin {
    private Response response;
    private JSONObject responseJSON;
    public TestAPIParameter testParameter;
    private String drmsProfile;

    private Test_userLogin(TestAPIParameter testParameter) {
        this.testParameter = testParameter;
    }

    public static Test_userLogin getInstance(TestAPIParameter testParameter) {
        return new Test_userLogin(testParameter);
    }

    @Test(description = "Trigger login api")
    public void trigger_login_API(ITestContext testContext) {
        response = APIUtil.requestAPI(testParameter, testContext);
        response.then().assertThat().statusCode(200);
        response.then()
                .assertThat()
                .body("data.token",notNullValue())
                .body("data.token", not(emptyString()));

        responseJSON = JSONObject.parseObject(response.asString());
        log.info("responseJSON = {}", responseJSON);

    }

    @Test(dependsOnMethods = "trigger_login_API")
    public void verify_response() {
        response.then().contentType(ContentType.JSON);
    }
}
