package com.api.utils;

import com.api.entities.TestAPIParameter;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

@Slf4j
public class ConfigUtil {
    private static Map<String, String> propertyMap;
    private static final List<String> configUrlList = new ArrayList<>();

    static {
        initconfigUrlList();
        initDataSourceFromEhConfigSvc();
    }

    private static void initconfigUrlList() {
        configUrlList.add("http://lnyeqelaap4u.nam.nsroot.net:8903/mondo-testng-api-service/namuat");
        configUrlList.add("http://lnyeqelaap4u.nam.nsroot.net:8903/config/getConfigurations/mondo-testng-api-service/namuat");
        configUrlList.add("http://lnyeqelaap3u.nam.nsroot.net:8903/config/getConfigurations/mondo-testng-api-service/namuat");
        configUrlList.add("http://lhkeqelaap3u.apac.nsroot.net:8903/config/getConfigurations/mondo-testng-api-service/apacuat");
        configUrlList.add("http://lhkeqelaap4u.apac.nsroot.net:8903/config/getConfigurations/mondo-testng-api-service/apacuat");
        configUrlList.add("http://lrdeqelaap3u.eur.nsroot.net:8903/config/getConfigurations/mondo-testng-api-service/emeauat");
        configUrlList.add("http://lrdeqelaap4u.eur.nsroot.net:8903/config/getConfigurations/mondo-testng-api-service/emeauat");
    }

    @SuppressWarnings("unchecked")
    private static void initServiceUrl() {
        ((List<TestAPIParameter>) DBUtil.doSqlSessionByEnvironment("postgresql_lif", "auto_baseurl", null))
                .forEach(
                        testAPIParameter -> propertyMap.put(
                                testAPIParameter.getServiceName() + "_" + testAPIParameter.getProfileName(),
                                testAPIParameter.getBaseURL()
                        )
                );
    }

    public static Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    public static void initDataSourceFromEhConfigSvc() {
        Map<String, Object> innerMap = Maps.newHashMap();
        for (String entry : configUrlList) {
            try {
                Response response = given()
                        .get(entry)
                        .then().extract().response();

                JSON.parseObject(response.asString())
                        .getJSONArray("propertySources")
                        .toJavaList(JSONObject.class)
                        .stream()
                        .forEach(jsonObject -> {
                            if (jsonObject.get("name").toString().equalsIgnoreCase("mondo-testng-api-service-namuat")) {
                                for (Map.Entry<String, Object> entry1 : jsonObject.getJSONObject("source").entrySet()) {
                                    innerMap.put(entry1.getKey(), entry1.getValue());
                                }
                            }
                        });

                log.info("ConfigUtil connect url:" + entry + " success");
                break;
            } catch (Exception ignored) {
                log.error("ConfigUtil connect url:" + entry + " failed");
            }
        }

        if (innerMap.isEmpty()) {
            throw new IllegalStateException("failed to get configs from EH configuration service");
        }

        propertyMap = innerMap.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> String.valueOf(entry.getValue())
                        )
                );

        initServiceUrl();
    }
}