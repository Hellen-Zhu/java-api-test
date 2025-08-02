package com.api.testcases.user;

import com.alibaba.fastjson2.JSONObject;
import com.api.entities.TestAPIParameter;
import com.api.enums.DBEnum;
import com.api.helpers.ParameterHelper;
import com.api.utils.APIUtil;
import com.api.utils.DBUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class Test_login {
    private Response response;
    private JSONObject responseJSON;
    public TestAPIParameter testParameter;
    private String drmsProfile;

    private Test_login(TestAPIParameter testParameter) {
        this.testParameter = testParameter;
    }

    public static Test_login getInstance(TestAPIParameter testParameter) {
        return new Test_login(testParameter);
    }

    @Test(description = "Trigger login api")
    public void trigger_login_API(ITestContext testContext) {
        testParameter.setParameter(ParameterHelper.setParameterForTradeBlotter(testParameter));
        response = APIUtil.requestAPI(testParameter, testContext);
        response.then().assertThat().statusCode(200);
        responseJSON = JSONObject.parseObject(response.body().asString());
        this.drmsProfile = DBUtil.getProfileName(DBEnum.DRMS, testParameter.getProfileName());
    }

    @Test(dependsOnMethods = "trigger_login_API")
    public void verify_response() {
        response.then().contentType(ContentType.JSON);
    }
}
