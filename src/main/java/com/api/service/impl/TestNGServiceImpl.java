package com.api.service.impl;
import com.api.service.TestNGService;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;

@Service
public class TestNGServiceImpl implements TestNGService {

    @Override
    @Async("testNgAsyncExecutor")
    public void runWithAsync(JSONObject requestObject) {
        try {
            Class<?> defaultCl = Class.forName(requestObject.getString("mainClass"));
            defaultCl.getMethod("triggerByCode", JSONObject.class).invoke(null, requestObject);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void runWithoutAsync(JSONObject requestObject) {
        try {
            Class<?> defaultCl = Class.forName(requestObject.getString("mainClass"));
            defaultCl.getMethod("triggerByCode", JSONObject.class).invoke(null, requestObject);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
}