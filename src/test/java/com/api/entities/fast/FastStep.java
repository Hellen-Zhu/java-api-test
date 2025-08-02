package com.api.entities.fast;

import lombok.Data;

@Data
public class FastStep {
    Result result;
    int line;
    String name;
    // Match match;
    String keyword;
    int[] matchedColumns;
    int seqNumber;

    @SuppressWarnings("unused")
    String debugInfo;
    String[] output;
}