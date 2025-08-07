package com.api.testcases;

import com.api.entities.testng.IOrderedMethod;
import com.api.entities.TestAPIParameter;
import lombok.SneakyThrows;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MethodInterceptor implements IMethodInterceptor {
    @SneakyThrows
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IOrderedMethod> result = new ArrayList<>();
        List<IOrderedMethod> orderedMethods = new ArrayList<>();
        for(IMethodInstance methodInstance: methods){
            int[] idAndStepId = getCaseIdAndStepId(methodInstance);
            IOrderedMethod iOrderedMethod = new IOrderedMethod();
            iOrderedMethod.setCaseId(idAndStepId[0]);
            iOrderedMethod.setStepId(idAndStepId[1]);
            iOrderedMethod.setMethod(methodInstance);
            orderedMethods.add(iOrderedMethod);
        }

        Map<Integer, List<IOrderedMethod>> iOrderedMethodMap = orderedMethods.stream().collect(Collectors.groupingBy(IOrderedMethod::getCaseId));
        Set<Integer> caseIdSet = iOrderedMethodMap.keySet();
        Integer[] caseIds = caseIdSet.toArray(new Integer[0]);
        Arrays.sort(caseIds);
        for(Integer caseId: caseIds){
            result.addAll(iOrderedMethodMap.get(caseId).stream().sorted(Comparator.comparing(IOrderedMethod::getStepId)).collect(Collectors.toList()));
        }
        return result.stream().map(IOrderedMethod::getMethod).collect(Collectors.toList());
    }

    private int[] getCaseIdAndStepId(IMethodInstance methodInstance) {
        int[] result = new int[]{0, 0};
        try {
            //order
            Field testAPIParameterField = methodInstance.getMethod().getRealClass().getField("testParameter");
            TestAPIParameter testAPIParameter = (TestAPIParameter) testAPIParameterField.get(methodInstance.getInstance());
            result[0] = testAPIParameter.getId();
            result[1] = testAPIParameter.getStepId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}