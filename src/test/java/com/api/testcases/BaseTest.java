package com.api.testcases;

import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;

import com.api.entities.TestAPIParameter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BaseTest implements ITest {
    private static ThreadLocal<String> testName = null;

    @BeforeMethod
    public void beforeMethod(Method method, Object[] testData, ITestContext context){
        Field parameterField = null;
        TestAPIParameter parameter = null;
        try {
            parameterField = method.getDeclaringClass().getDeclaredField("testParameter");
            parameter = (TestAPIParameter)parameterField.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert parameter != null;
        realUpdateTestName(method,parameter);
        realUpdateMethodName(method,parameter,parameterField);
    }

    private void realUpdateTestName(Method method,TestAPIParameter parameter) {
        testName = new ThreadLocal<>();
        int id = parameter.getId();
        int stepId = parameter.getStepId();
        String methodName = method.getName();
        testName.set(id + " -> " + stepId + " -> " + methodName);
    }
    private void realUpdateMethodName(Method method,TestAPIParameter parameter, Field parameterField) {
        try {
            parameter.setCurrentMethodName(method.getName());
            parameterField.set(this,parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTestName() {
        String name = "";
        if(testName != null) {
            name = testName.get();
            testName.remove();
        }
        return name;
    }
}