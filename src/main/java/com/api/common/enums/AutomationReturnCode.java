package com.api.common.enums;

public enum AutomationReturnCode {

    SUCCESS(200),
    FAIL(400);

    int code;

    AutomationReturnCode(Integer code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}