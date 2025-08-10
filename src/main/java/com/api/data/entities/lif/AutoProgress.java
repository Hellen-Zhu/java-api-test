package com.api.data.entities.lif;

import com.api.common.enums.AutoProgressStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AutoProgress {

    String runId;
    String versionId;
    String releaseVersion;
    String component;
    LocalDateTime beginTime;
    AutoProgressStatus taskStatus;
    int totalCases;
    int passes;
    int failures;
    int skips;
    LocalDateTime endTime;

}