package com.api.entities;

import lombok.Data;

@Data
public class AutoConfiguration {
    String fastProject;
    String suite;
    String component;
    String runId;
    String label;
    String group;
    String runBy;
    String testType;
    String projectKey;
    String fastDashboardEnv;
    String testScope;
    String releaseVersion;
    String toEmail;
    String ccEmail;
    boolean enableJira;
    boolean enableEmail;
    boolean enableDash;
    String region;
    String env;
    String build;
    String module;

    @Override
    public String toString() {
        return "AutoConfiguration{" +
                "fastProject='" + fastProject + '\'' +
                ", suite='" + suite + '\'' +
                ", component='" + component + '\'' +
                ", runId='" + runId + '\'' +
                '}';
    }
}