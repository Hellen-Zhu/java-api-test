package com.api.helpers;

import com.api.enums.DSEnum;

public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<DSEnum> threadLocal = new ThreadLocal<>();

    public static void setDataSourceContext(DSEnum DSEnum) {
        threadLocal.set(DSEnum);
    }

    public static DSEnum getDataSourceContext() {
        return threadLocal.get();
    }

    public static void clearDataSourceContext() {
        threadLocal.remove();
    }
}