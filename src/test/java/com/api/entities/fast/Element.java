package com.api.entities.fast;

import lombok.Data;

import java.util.Map;

@Data
public class Element implements Cloneable {
    int line;
    String name;
    String start_timestamp; // 2024-06-28T08:53:24.854Z
    String description;
    String type;
    String keyword;
    FastStep[] fastSteps;
    String id;
    long startRuntime;
    long endRuntime;
    int passedSteps;
    long duration;
    Map details;

    @Override
    public Element clone() {
        try {
            return (Element) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}