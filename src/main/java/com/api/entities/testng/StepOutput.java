package com.api.entities.testng;

import lombok.Data;

import java.util.List;

@Data
public class StepOutput {

    List<String> reportInfo;
    List<String> debugInfo;

}