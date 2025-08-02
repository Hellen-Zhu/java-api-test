package com.api.entities.fast;

import lombok.Data;

// Suite-> component
// Feature-> serviceName; (testName in xml)
// Scenario-> testClass from DB.description + classJiraKey
// Step-> testMethod
@Data
public class Feature {
    int line;
    Element[] elements;
    String name;
    String description;
    String id;
    String profile;
    String keyword;
    String uri;
    int passedCases;
    int totalCases;
}