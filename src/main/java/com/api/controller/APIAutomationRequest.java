package com.api.controller;
import lombok.Data;
import java.util.List;

@Data
public class APIAutomationRequest {
    // upload Dashboard : default from auto_configuration
    private String group;
    private String suite;
    private String runBy;
    private String testType = "Regression";
    private String env;
    private List<String> regions;
    private String testScope = "Regression";
    private String releaseVersion;

    private String module;
    private String component;
    private boolean sanityOnly = true;

    private String scenario;
    private String service;
    private String feature;
    private String label;
    private String issueKey;

    private Integer id;
}
