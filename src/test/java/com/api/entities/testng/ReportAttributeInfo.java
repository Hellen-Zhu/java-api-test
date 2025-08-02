package com.api.entities.testng;

import lombok.Data;

@Data
public class ReportAttributeInfo {

    boolean enableFast;
    boolean enableMondo;
    boolean enableJira;
    boolean enableEmail;
    boolean sanityOnly;
    boolean isDebug;
    boolean isRelease;
    String module;
    String component;
    String scenarios;
    String scenario;
    String ids;
    String labels;
    String projectKey;
    String jiraKey;
    String fastKey;
    String fastProject;
    String versionId;
    String group;
    String suite;
    String fastDashboardEnv;
    String testScope;
    String testType;
    String region;
    String qa;
    String runBy;
    String build;
    String receiverAddress;
    String copyAddress;
    String dailyRunVersion;
    String releaseVersionList;
    String actualFixVersion;
    String testCycle;
    String jiraToken;
    String username;
    String password;
    String runId;

}