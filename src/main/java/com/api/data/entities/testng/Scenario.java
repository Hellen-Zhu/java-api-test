package com.api.data.entities.testng;

import lombok.Data;

import java.util.Set;

@Data
public class Scenario {

    String scenarioName;
    Set<Step> passedTestResults;
    Set<Step> failedTestResults;
    Set<Step> skippedTestResults;

}