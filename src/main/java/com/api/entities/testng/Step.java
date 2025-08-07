package com.api.entities.testng;

import lombok.Data;

import java.util.Map;
//Step
@Data
public class Step {

    String testClassName;
    String testMethodName;
    int status;
    int stepId;
    String testClassDescription;
    String testMethodDescription;
    StepThrowable throwable;
    Map<String, String> testMethodAttributes;
    StepOutput output;
    long startMillis;
    long endMillis;

}