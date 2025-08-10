package com.api.data.entities.testng;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class Suite implements Cloneable {

    Map<String, String> suiteParameters;
    String suiteName;
    Map<String, Set<Scenario>> featureScenarioMap = new HashMap<>();

    @Override
    public Suite clone() {
        try {
            return (Suite) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}