package com.api.data.entities.testng;

import lombok.Data;
import org.testng.IMethodInstance;

@Data
public class IOrderedMethod {
    IMethodInstance method;
    int caseId;
    int stepId;
}