package com.api.entities.testng;

import lombok.Data;

@Data
public class ReportAttributeInfo {
    boolean sanityOnly;
    boolean isDebug;
    boolean isRelease;
    String module;
    String component;
    String scenarios;
    String scenario;
    String ids;
    String labels;
    String versionId;
    String group;
    String suite;
    String testScope;
    String testType;
    String region;
    String qa;
    String runBy;
    String build;
    String dailyRunVersion;
    String releaseVersionList;
    String actualFixVersion;
    String runId;

}