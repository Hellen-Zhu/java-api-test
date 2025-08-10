package com.api.data.entities.testng;

import lombok.Data;

import java.util.Map;

@Data
public class TestSuite {

    String tests;
    String name;
    String time;
    String timestamp;
    Map<String, TestCase> testCaseMap;

}