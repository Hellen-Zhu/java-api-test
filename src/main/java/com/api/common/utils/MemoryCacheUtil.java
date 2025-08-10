package com.api.common.utils;

import com.api.common.enums.APIStatus;
import com.api.common.helpers.ParameterHelper;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.api.common.utils.ReflectionUtil.fetchClassByClassName;

/**
 * Singleton In Memory Cache(Caffeine)
 */
@Slf4j
public class MemoryCacheUtil {
    private final static MemoryCacheUtil MEMORY_CACHE_UTIL = new MemoryCacheUtil(2, 50, 1000);

    private final LoadingCache<String, Object> cache;

    private MemoryCacheUtil(int expireAfterWrite, int initialCapacity, int maxItems) {
        this.cache = Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maxItems)
                .expireAfterWrite(expireAfterWrite, TimeUnit.HOURS)
                .build(key -> "");
    }

    public static MemoryCacheUtil getInstance() { return MEMORY_CACHE_UTIL; }

    public Object get(String s) {
        if (getIfPresent(s) == null) {
            return null;
        } else return cache.get(s);
    }

    public static void removeInMemoryCache(JSONArray removeArray) {
        for (Object object : removeArray) {
            MemoryCacheUtil.getInstance().invalidate(object.toString().trim());
        }
    }

    public static void addInMemoryCache(JSONArray addArray, Map<String, Object> response) {
        for (Object object : addArray) {
            JSONObject addObject = (JSONObject) object;
            if (addObject.containsKey("code")) {
                JSONObject mapObject = addObject.getJSONObject("map");
                Map<String, Object> map = new HashMap<>();
                for (String key : mapObject.keySet()) {
                    map.put(key, fetchClassByClassName(mapObject.getString(key)));
                }

                String expression = addObject.getString("code");
                getInstance().put(addObject.getString("keyInCache"), ReflectionUtil.fetchValueByCodeExpression(expression, map));
            } else if (StringUtils.equalsIgnoreCase(addObject.getString("response"), "JSONObject")) {
                JSONObject responseObject = JSONObject.parseObject(response.get("responseString").toString());
                if (addObject.containsKey("attribute")) {
                    String[] attributes = addObject.getString("attribute").split("_");
                    getInstance().put(addObject.getString("keyInCache"), ParameterHelper.parseFromResponse(attributes, responseObject).toString());
                }

                if (addObject.containsKey("jsonNode")) {
                    String[] paths = addObject.getString("jsonNode").split(",");
                    getInstance().put(addObject.getString("keyInCache"), ParameterHelper.parseJsonNodeFromPaths(response.get("responseString").toString(), paths));
                }
            } else if (StringUtils.equalsIgnoreCase(addObject.getString("response"), "JSONArray")) {
                JSONArray responseArray = JSONArray.parseArray(response.get("responseString").toString());
                if (addObject.containsKey("attribute") && addObject.containsKey("concat")) {
                    List<Object> list = new ArrayList<>();
                    for (int i = 0; i < responseArray.size(); i++) {
                        list.add(responseArray.getJSONObject(i).get(addObject.getString("attribute")));
                    }
                    getInstance().put(addObject.getString("keyInCache"), StringUtils.join(list, addObject.getString("concat")));
                }
            } else if (StringUtils.equalsIgnoreCase(addObject.getString("response"), "StatusCode")) {
                int statusCode = (Integer) response.get("statusCode");
                if (statusCode >= 200 && statusCode <= 201) {
                    getInstance().put(addObject.getString("keyInCache"), APIStatus.SUCCESS);
                } else if (statusCode >= 1000) {
                    getInstance().put(addObject.getString("keyInCache"), APIStatus.NONE);
                } else getInstance().put(addObject.getString("keyInCache"), APIStatus.FAIL);
            }
        }
    }

    public @Nullable Object getIfPresent(String s) { return cache.getIfPresent(s); }

    public @PolyNull Object get(String s, Function<? super String, ?> function) { return cache.get(s, function); }

    public void put(String s, Object o) { cache.put(s, o); }

    public void invalidate(String s) { cache.invalidate(s); }

    public void invalidateAll() { cache.invalidateAll(); }
}
