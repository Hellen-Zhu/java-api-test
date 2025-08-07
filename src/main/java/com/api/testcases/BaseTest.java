package com.api.testcases;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;

import com.api.entities.TestAPIParameter;
import com.api.utils.DBUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
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
        log.info("parameter: {}", parameter);
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

    @AfterMethod
    public void afterMethod() {
        // 清理数据库上下文
        try {
            DBUtil.clearJpaDb();
            log.debug("Database context cleared after test method");
        } catch (Exception e) {
            log.warn("Failed to clear database context: " + e.getMessage());
        }
        
        // 清理ThreadLocal
        if (testName != null) {
            testName.remove();
        }
    }
    
    @AfterClass
    public void afterClass() {
        log.info("Test class cleanup started");
        try {
            // 确保所有数据库连接都被清理
            DBUtil.clearJpaDb();
            log.info("Final database cleanup completed");
        } catch (Exception e) {
            log.error("Failed during final cleanup: " + e.getMessage());
        }
    }

    @Override
    public String getTestName() {
        String name = "";
        if(testName != null) {
            name = testName.get();
            // 不在这里清理ThreadLocal，留给afterMethod处理
        }
        return name;
    }
}