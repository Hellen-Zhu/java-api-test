package com.api.data.entities.auto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentProgress {
    String groupId;
    String runId;
    String component;
    String taskStatus;
}