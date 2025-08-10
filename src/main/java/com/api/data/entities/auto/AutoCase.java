package com.api.data.entities.auto;

import com.alibaba.fastjson2.JSONObject;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import lombok.Data;
import java.lang.reflect.Field;
import java.util.List;

@Data
public class AutoCase {

    //Basic
    int id;
    String serviceName;
    String profileName;
    String baseURL;
    String method;
    String path;
    String className;
    String component;
    String description;
    Integer order;
    String label;
    String issueKey;
    String testType;
    String caseHelp;
    String body;
    String header;
    String param;
    String pathParam;
    String cookie;
    String e2eservice;

    //Others
    PreemptiveBasicAuthScheme authScheme;
    private JSONObject paramJSONObject;
    private JSONObject pathParamJSONObject;
    private Object bodyObject;
    private JSONObject headerJSONObject;
    private JSONObject caseHelpJSONObject;
    private JSONObject cookieJSONObject;

    public void replaceNullsWithEmptyStrings() {
        Field[] fields = AutoCase.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.get(this) == null) {
                    if (field.getType() == String.class) {
                        field.set(this, "");
                    }
                    if (field.getType() == Integer.class) {
                        field.set(this, 0);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static AutoCase[] getParameterArray(List<AutoCase> maps) {
        AutoCase[] result = new AutoCase[maps.size()];
        for (int i = 0; i < maps.size(); i++) {
            result[i] = maps.get(i);
        }
        return result;
    }
}