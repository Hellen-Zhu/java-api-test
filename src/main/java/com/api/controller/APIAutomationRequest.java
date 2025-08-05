package com.api.controller;
import lombok.Data;
import java.util.List;

@Data
public class APIAutomationRequest {
    // upload Dashboard : default from auto_configuration
    private String fastProject;
    private String group;
    private String suite;
    private String runBy;
    private String testType = "Regression";
    private String projectKey;
    private String env;
    private List<String> regions;
    private String fastDashboardEnv;
    private String testScope = "Regression";
    private String releaseVersion;

    private boolean enableDash;
    private boolean enableJira;
    private boolean enableEmail;

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
