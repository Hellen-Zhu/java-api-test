package com.api.testcases.user;

import com.api.enums.DBEnum;
import com.api.entities.TestAPIParameter;
import com.api.helpers.ParameterHelper;
import com.api.utils.DBUtil;
import com.api.utils.APIUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import com.alibaba.fastjson2.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * //-1 means DB has no value
 */
public class Test_addUser {

    // private
    private Response response;
    private JSONObject responseJSON;
    public TestAPIParameter testParameter;
    private String drmsProfile;

    public static Test_addUser getInstance(TestAPIParameter testParameter) {
        return new Test_addUser(testParameter);
    }

    private Test_addUser(TestAPIParameter testParameter) {
        this.testParameter = testParameter;
    }

    @Test
    public void trigger_API(ITestContext testContext) {
        testParameter.setParameter(ParameterHelper.setParameterForTradeBlotter(testParameter));
        response = APIUtil.requestAPI(testParameter, testContext);
        response.then().assertThat().statusCode(200);
        responseJSON = JSONObject.parseObject(response.body().asString());
        this.drmsProfile = DBUtil.getProfileName(DBEnum.DRMS, testParameter.getProfileName());
    }

    @Test(dependsOnMethods = "trigger_API")
    public void verify_response() {
        response.then().contentType(ContentType.JSON);
    }

    @Test(dependsOnMethods = "trigger_API")
    public void verify_database(ITestContext testContext) {
        Map<String, Object> params = new HashMap<>();
        params.put("secIds", testParameter.getRequestJSONObject().getJSONObject("params").get("securityIds").toString());
        params.put("posIds", testParameter.getRequestJSONObject().getJSONObject("params").get("positionIds").toString());
        params.put("tradeIds", testParameter.getRequestJSONObject().getJSONObject("params").get("tradeIds").toString());

    }
}